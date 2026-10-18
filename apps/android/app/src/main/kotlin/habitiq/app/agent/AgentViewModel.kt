package habitiq.app.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AgentInputMode { Voice, Text }

/** What the mic button under a notice does: ask for the permission again, or open app settings (permanently denied). */
enum class MicAction { Ask, Settings }

sealed interface AgentUiState {
    data class Idle(val notice: String? = null, val micAction: MicAction? = null) : AgentUiState
    data class Listening(val partial: String) : AgentUiState
    data class Checking(val transcript: String) : AgentUiState
    /** [say] is Gemini's own human reply, shown above the card; null in basic mode. */
    /** [say] is Gemini's line above the card; [aiText] is whatever Gemini wrote here (reportable), null if the app wrote it all. */
    data class Answer(val transcript: String, val answer: AgentAnswer, val say: String? = null, val aiText: String? = say) : AgentUiState
    /** Something to save. [awaitingVoice]: listening for a spoken yes/no; [saving]: the write is in flight. */
    data class Preview(
        val transcript: String, val summary: String, val card: AgentCard,
        val awaitingVoice: Boolean = false, val saving: Boolean = false, val say: String? = null,
    ) : AgentUiState
    data class Clarify(val transcript: String, val question: String, val options: List<ClarifyOption>) : AgentUiState
    data class Done(val transcript: String, val message: String) : AgentUiState
    data class NotUnderstood(val transcript: String, val message: String) : AgentUiState
    data class Error(val transcript: String, val message: String) : AgentUiState
}

/** Performs approved steps through the app's existing write paths. Returns false when a write failed. */
interface AgentActions {
    suspend fun run(step: AgentStep): Boolean

    companion object {
        val None = object : AgentActions { override suspend fun run(step: AgentStep) = false }
    }
}

/** Speaks replies. [onDone] runs on the main thread once speech has finished (or immediately if muted). */
interface AgentSpeaker {
    fun speak(text: String, onDone: () -> Unit = {})
    fun stop()

    companion object {
        val Silent = object : AgentSpeaker {
            override fun speak(text: String, onDone: () -> Unit) = onDone()
            override fun stop() {}
        }
    }
}

/** Understands requests the rule-based parser can't (Gemini). Returns null when it can't help. */
interface AgentPlanner {
    suspend fun plan(text: String, state: HouseholdState, today: LocalDate): AgentPlan?
}

private const val NOT_YET = "Hmm, I didn't quite get that. Try “I spent 500 on groceries”, “What do I owe?” or “Rooms in Kondapur under 10k”."
const val CAUGHT_NOTHING = "Sorry, I didn't catch that. Say it again, or type it."
const val MIC_OFF = "Mic access is off. Allow it, or just type to me."
const val MIC_BLOCKED = "Mic is blocked for Oddroof. Turn it on in Settings, or just type to me."
private const val SAVE_FAILED = "That didn't save. Check your internet and try once more."

/** Requests that may be about finding a place, so listings must be loaded first. */
private val FLAT_TALK = Regex("""\b(?:flats?|rooms?|pgs?|rent|budget|vacanc(?:y|ies)|flatmates?|roommates?|stay|accommodation|hostel|bhk|near|nearby|nearest|area|locality)\b""")

private val YES = Regex("""\b(?:yes|yeah|yep|yup|sure|ok|okay|save|confirm|do it|go ahead|correct|haan|han|ha|avunu|sari|right)\b""")
private val NO = Regex("""\b(?:no|nope|cancel|dont|don't|stop|wait|nahi|nahin|vaddu|leave it)\b""")

/**
 * Drives the voice experience: Listening → Checking → Answer / Preview / Clarify / Done. Anything that
 * changes data is shown as a card first and saved only on Save or a spoken "yes". Requests that came
 * by voice are answered out loud.
 */
class AgentViewModel(
    private val household: () -> HouseholdState,
    private val speech: SpeechSource,
    private val today: () -> LocalDate = { LocalDate.now() },
    private val checkingMs: Long = 400,
    private val listenTimeoutMs: Long = 10_000,
    private val actions: AgentActions = AgentActions.None,
    private val speaker: AgentSpeaker = AgentSpeaker.Silent,
    private val planner: AgentPlanner? = null,
    /**
     * Makes sure flat listings are loaded before a flat-search request is answered. Discover loads
     * lazily, so a quick typed "rooms in Kondapur" could otherwise see an empty list.
     */
    private val prepareFlatSearch: suspend () -> Unit = {},
    /** Sends a "this AI reply was offensive / wrong" report (request, reply) for review. */
    private val reportAi: suspend (transcript: String, reply: String) -> Unit = { _, _ -> },
) : ViewModel() {
    private val _state = MutableStateFlow<AgentUiState>(AgentUiState.Idle())
    val state: StateFlow<AgentUiState> = _state.asStateFlow()

    private val _mode = MutableStateFlow(AgentInputMode.Voice)
    val mode: StateFlow<AgentInputMode> = _mode.asStateFlow()

    /** Kept apart from [state] so the listening animation can follow the voice without recomposing the whole sheet. */
    private val _level = MutableStateFlow(0f)
    val level: StateFlow<Float> = _level.asStateFlow()

    /** Set while Gemini can't be reached, with the reason, so the person knows answers come from basic rules. */
    private val _smartNote = MutableStateFlow<String?>(null)
    val smartNote: StateFlow<String?> = _smartNote.asStateFlow()

    /** One-shot "take the person to this screen" events. */
    private val _navigation = MutableSharedFlow<AgentLink>(extraBufferCapacity = 4)
    val navigation: SharedFlow<AgentLink> = _navigation.asSharedFlow()

    private var work: Job? = null
    private var watchdog: Job? = null
    private var pending: AgentPlan.Actions? = null
    /** True while the current conversation turn came by voice, so replies are spoken. */
    private var viaVoice = false

    init {
        viewModelScope.launch { speech.events.collect(::onSpeech) }
    }

    fun startListening() {
        work?.cancel()
        speaker.stop()
        pending = null
        _mode.value = AgentInputMode.Voice
        _state.value = AgentUiState.Listening("")
        speech.start()
        armWatchdog()
    }

    /** Some recognisers bind and then never call back; never leave the person stuck on "Listening…". */
    private fun armWatchdog() {
        watchdog?.cancel()
        watchdog = viewModelScope.launch {
            delay(listenTimeoutMs)
            if (_state.value is AgentUiState.Listening) {
                speech.cancel()
                _level.value = 0f
                _state.value = AgentUiState.Error("", "Your mic went quiet on me. Tap to try again, or just type it.")
            }
        }
    }

    fun stopListening() = speech.stop()

    fun useTextMode(notice: String? = null, micAction: MicAction? = null) {
        watchdog?.cancel()
        speech.cancel()
        _mode.value = AgentInputMode.Text
        val s = _state.value
        if (s is AgentUiState.Preview && s.awaitingVoice) _state.value = s.copy(awaitingVoice = false)
        if (notice != null || s is AgentUiState.Listening) _state.value = AgentUiState.Idle(notice, micAction)
    }

    fun useVoiceMode() { _mode.value = AgentInputMode.Voice }

    fun reset() {
        work?.cancel()
        watchdog?.cancel()
        speech.cancel()
        speaker.stop()
        pending = null
        _level.value = 0f
        _state.value = AgentUiState.Idle()
    }

    /** Typed or tapped input. */
    fun submitText(text: String) = submit(text, voice = false)

    private fun submit(text: String, voice: Boolean) {
        val transcript = text.trim()
        if (transcript.isEmpty()) return
        work?.cancel()
        viaVoice = voice
        pending = null
        _state.value = AgentUiState.Checking(transcript)
        work = viewModelScope.launch {
            ContentGuard.reasonToBlock(transcript)?.let {
                delay(checkingMs)
                show(AgentUiState.NotUnderstood(transcript, it), it)
                return@launch
            }
            var home = household()
            var parsed = LocalIntentParser.parse(transcript, home, today())
            // Only a flat search (or something the rules can't place) waits for listings; "paid rent 9000" never does.
            val needsListings = parsed == ParseResult.NeedsModel || (parsed as? ParseResult.Confident)?.plan.let {
                it is AgentPlan.Answer && (it.query is AgentQuery.FindFlats || it.query is AgentQuery.AreaRent)
            }
            if (needsListings && FLAT_TALK.containsMatchIn(normalizeUtterance(transcript))) {
                prepareFlatSearch()
                home = household()
                parsed = LocalIntentParser.parse(transcript, home, today())
            }
            val rules = (parsed as? ParseResult.Confident)?.plan
            // Gemini first: it understands the request the way a person would and answers in its own words.
            // The rules are the safety net for when it's off, offline or out of quota, so nothing simple ever breaks.
            val smart: AgentPlan? = if (planner == null) null else try {
                planner.plan(transcript, home, today()).also { _smartNote.value = null }
            } catch (e: PlannerUnavailable) {
                _smartNote.value = "Basic mode: ${e.reason}"
                if (rules == null) {
                    val message = "${e.reason} Simple requests like “Spent 500 on groceries” still work."
                    show(AgentUiState.NotUnderstood(transcript, message), message)
                    return@launch
                }
                null
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            // A garbled or over-cautious model answer never beats a confident rule ("Spent 500" must just work).
            val plan = when {
                smart != null && !(smart is AgentPlan.Unsupported && rules != null) -> smart
                rules != null -> {
                    if (planner == null) delay(checkingMs) // instant answers still let "Checking your flat" read
                    rules
                }
                else -> null
            }
            if (plan == null) show(AgentUiState.NotUnderstood(transcript, NOT_YET), NOT_YET)
            else present(transcript, plan, home)
        }
    }

    fun choose(option: ClarifyOption) {
        val transcript = (_state.value as? AgentUiState.Clarify)?.transcript ?: return
        present(transcript, option.plan, household())
    }

    /** Save the card on screen. */
    fun confirm() {
        val preview = _state.value as? AgentUiState.Preview ?: return
        val plan = pending ?: return
        if (preview.saving) return
        speech.cancel()
        _state.value = preview.copy(awaitingVoice = false, saving = true)
        work = viewModelScope.launch {
            val home = household()
            val ok = plan.steps.all { step -> runCatching { actions.run(step) }.getOrDefault(false) }
            pending = null
            if (ok) {
                val message = doneMessage(plan.steps.first(), home)
                show(AgentUiState.Done(preview.transcript, message), message)
            } else {
                show(AgentUiState.Error(preview.transcript, SAVE_FAILED), SAVE_FAILED)
            }
        }
    }

    fun cancelPreview() {
        speech.cancel()
        pending = null
        _state.value = AgentUiState.Idle()
        if (viaVoice) speaker.speak("No problem, I won't save it.")
    }

    private fun present(transcript: String, plan: AgentPlan, home: HouseholdState) {
        when (plan) {
            is AgentPlan.Answer -> {
                val answer = QueryResolver.resolve(plan.query, home, today())
                val say = safeSay(plan.say, transcript, answer.headline, *answer.lines.flatMap { listOf(it.label, it.value) }.toTypedArray())
                show(AgentUiState.Answer(transcript, answer, say), say ?: spokenFor(answer))
            }
            is AgentPlan.Reply -> show(
                AgentUiState.Answer(transcript, plan.answer, aiText = plan.answer.headline.takeIf { plan.byAi }),
                spokenFor(plan.answer),
            )
            is AgentPlan.Actions -> {
                pending = plan
                val say = safeSay(plan.say, transcript, plan.summary)
                _state.value = AgentUiState.Preview(transcript, plan.summary, cardFor(plan, home), say = say)
                if (viaVoice) {
                    // Ask out loud, then listen for "yes" / "no" while the card stays on screen.
                    val ask = say?.let { if (it.trimEnd().endsWith("?")) it else "$it Shall I save it?" } ?: "${plan.summary}. Shall I save it?"
                    speaker.speak(ask) {
                        val s = _state.value
                        if (s is AgentUiState.Preview && !s.saving && pending === plan) {
                            _state.value = s.copy(awaitingVoice = true)
                            speech.start()
                        }
                    }
                }
            }
            is AgentPlan.Clarify -> show(AgentUiState.Clarify(transcript, plan.question, plan.options), plan.question)
            is AgentPlan.Unsupported -> show(AgentUiState.NotUnderstood(transcript, plan.reason), plan.reason)
            is AgentPlan.Navigate -> {
                show(AgentUiState.Done(transcript, plan.say), plan.say)
                _navigation.tryEmit(plan.link)
            }
        }
    }

    private fun show(state: AgentUiState, spoken: String) {
        _state.value = state
        if (viaVoice) speaker.speak(spoken)
    }

    private fun onSpeech(event: SpeechEvent) {
        val current = _state.value
        // A spoken yes/no while a card waits for confirmation.
        if (current is AgentUiState.Preview && current.awaitingVoice) {
            when (event) {
                is SpeechEvent.Final -> {
                    val said = normalizeUtterance(event.text)
                    when {
                        NO.containsMatchIn(said) -> cancelPreview()
                        YES.containsMatchIn(said) -> confirm()
                        else -> submit(event.text, voice = true) // they said something new instead
                    }
                }
                is SpeechEvent.Error -> _state.value = current.copy(awaitingVoice = false)
                else -> Unit
            }
            return
        }
        // Results that arrive after the person reset, switched to typing or timed out belong to a dead session.
        if (current !is AgentUiState.Listening) return
        if (event is SpeechEvent.Partial || event is SpeechEvent.Level) armWatchdog() else watchdog?.cancel()
        if (event !is SpeechEvent.Level && event !is SpeechEvent.Partial) _level.value = 0f
        when (event) {
            is SpeechEvent.Level -> _level.value = event.level
            is SpeechEvent.Partial -> _state.value = AgentUiState.Listening(event.text)
            is SpeechEvent.Final -> submit(event.text, voice = true)
            is SpeechEvent.Error -> when (event.kind) {
                SpeechErrorKind.NoPermission -> useTextMode(MIC_OFF, MicAction.Ask)
                SpeechErrorKind.Unavailable -> useTextMode("Voice isn't available on this phone. You can type instead.")
                SpeechErrorKind.Network -> _state.value = AgentUiState.Error("", "Voice needs internet on this phone right now. Typing works offline.")
                SpeechErrorKind.NoMatch, SpeechErrorKind.Busy, SpeechErrorKind.Other ->
                    _state.value = AgentUiState.Error("", CAUGHT_NOTHING)
            }
        }
    }

    /**
     * Report what Gemini said on screen right now (Play's AI-generated content policy: people must be able
     * to flag offensive AI output from inside the app). Best effort; returns false if there's nothing to report.
     */
    fun reportReply(): Boolean {
        val (transcript, reply) = when (val s = _state.value) {
            is AgentUiState.Answer -> s.transcript to (s.aiText ?: return false)
            is AgentUiState.Preview -> s.transcript to (s.say ?: return false)
            else -> return false
        }
        viewModelScope.launch {
            try { reportAi(transcript.take(300), reply.take(500)) } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) { }
        }
        return true
    }

    /** The app went to the background: never keep the mic open or keep talking behind the person's back. */
    fun onBackground() {
        watchdog?.cancel()
        speech.cancel()
        speaker.stop()
        _level.value = 0f
        when (val s = _state.value) {
            is AgentUiState.Listening -> _state.value = AgentUiState.Idle()
            is AgentUiState.Preview -> if (s.awaitingVoice) _state.value = s.copy(awaitingVoice = false)
            else -> Unit
        }
    }

    override fun onCleared() {
        speech.release()
        speaker.stop()
    }
}

/** What the agent says after a save. */
internal fun doneMessage(step: AgentStep, home: HouseholdState): String {
    fun name(uid: String) = home.members.firstOrNull { it.uid == uid }?.firstName ?: "them"
    fun rupees(p: Long) = habitiq.app.lib.formatInr(p / 100.0)
    return when (step) {
        is AgentStep.AddExpense -> "Saved ${rupees(step.amountPaise)} for ${step.title}."
        is AgentStep.Settle -> if (step.fromUid == home.myUid) "Recorded ${rupees(step.amountPaise)} paid to ${name(step.toUid)}."
            else "Recorded ${rupees(step.amountPaise)} from ${name(step.fromUid)}."
        is AgentStep.CompleteTask -> "Nice. Marked ${step.taskName} as done."
        is AgentStep.CreateTask -> "Added the task ${step.name}."
        is AgentStep.SetAway -> if (step.away) "Done. Your flatmates will see you're away." else "Welcome back. You're marked as available."
    }
}

/**
 * Gemini's reply line, kept only if every number in it also appears in what the app itself computed
 * (or what the person said). The model may phrase things, but it never gets to state a figure.
 */
internal fun safeSay(say: String?, vararg truth: String): String? {
    val text = say?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    if (ContentGuard.reasonToBlock(text) != null) return null
    fun numbers(s: String) = Regex("""\d[\d,]*(?:\.\d+)?""").findAll(s).map { it.value.replace(",", "").removeSuffix(".0").removeSuffix(".00") }.toSet()
    val allowed = truth.flatMap { numbers(it) }.toSet() +
        truth.flatMap { t -> findAmounts(normalizeUtterance(t)).map { (it.paise / 100).toString() } }
    return text.takeIf { numbers(it).all { n -> n in allowed } }
}

/** A short spoken version of an answer: the headline and up to two lines. */
internal fun spokenFor(answer: AgentAnswer): String {
    val head = answer.headline.trimEnd('.', '!') + "."
    // Card text reads well but sounds odd: "·" and "/head" are for eyes, not ears.
    fun ear(t: String) = t.replace(" · ", ", ").replace("/head", " a head")
    val lines = answer.lines.take(2).joinToString(" ") { "${ear(it.label)}, ${ear(it.value)}." }
    return listOf(head, lines).filter { it.isNotBlank() }.joinToString(" ")
}
