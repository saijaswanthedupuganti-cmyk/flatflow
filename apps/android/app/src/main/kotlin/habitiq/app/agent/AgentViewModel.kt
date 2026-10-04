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

sealed interface AgentUiState {
    data class Idle(val notice: String? = null) : AgentUiState
    data class Listening(val partial: String) : AgentUiState
    data class Checking(val transcript: String) : AgentUiState
    data class Answer(val transcript: String, val answer: AgentAnswer) : AgentUiState
    /** Something to save. [awaitingVoice]: listening for a spoken yes/no; [saving]: the write is in flight. */
    data class Preview(
        val transcript: String, val summary: String, val card: AgentCard,
        val awaitingVoice: Boolean = false, val saving: Boolean = false,
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

private const val NOT_YET = "I didn't get that. Try “Spent 500 on groceries”, “What do I owe?” or “Join a flat”. Say “help” for more."
private const val SAVE_FAILED = "Couldn't save that. Check your connection and try again."

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
) : ViewModel() {
    private val _state = MutableStateFlow<AgentUiState>(AgentUiState.Idle())
    val state: StateFlow<AgentUiState> = _state.asStateFlow()

    private val _mode = MutableStateFlow(AgentInputMode.Voice)
    val mode: StateFlow<AgentInputMode> = _mode.asStateFlow()

    /** Kept apart from [state] so the listening animation can follow the voice without recomposing the whole sheet. */
    private val _level = MutableStateFlow(0f)
    val level: StateFlow<Float> = _level.asStateFlow()

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
                _state.value = AgentUiState.Error("", "Voice isn't responding. Try again or type it.")
            }
        }
    }

    fun stopListening() = speech.stop()

    fun useTextMode(notice: String? = null) {
        watchdog?.cancel()
        speech.cancel()
        _mode.value = AgentInputMode.Text
        val s = _state.value
        if (s is AgentUiState.Preview && s.awaitingVoice) _state.value = s.copy(awaitingVoice = false)
        if (notice != null || s is AgentUiState.Listening) _state.value = AgentUiState.Idle(notice)
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
            val home = household()
            val plan = when (val result = LocalIntentParser.parse(transcript, home, today())) {
                is ParseResult.Confident -> {
                    delay(checkingMs) // instant answers still let "Checking your flat" read
                    result.plan
                }
                // The model call is its own wait, so no extra delay here.
                ParseResult.NeedsModel -> try {
                    planner?.plan(transcript, home, today())
                } catch (e: PlannerUnavailable) {
                    val message = "${e.reason} Simple requests like “Spent 500 on groceries” still work."
                    show(AgentUiState.NotUnderstood(transcript, message), message)
                    return@launch
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    null
                }
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
        if (viaVoice) speaker.speak("Okay, I won't save it.")
    }

    private fun present(transcript: String, plan: AgentPlan, home: HouseholdState) {
        when (plan) {
            is AgentPlan.Answer -> {
                val answer = QueryResolver.resolve(plan.query, home, today())
                show(AgentUiState.Answer(transcript, answer), spokenFor(answer))
            }
            is AgentPlan.Reply -> show(AgentUiState.Answer(transcript, plan.answer), spokenFor(plan.answer))
            is AgentPlan.Actions -> {
                pending = plan
                _state.value = AgentUiState.Preview(transcript, plan.summary, cardFor(plan, home))
                if (viaVoice) {
                    // Ask out loud, then listen for "yes" / "no" while the card stays on screen.
                    speaker.speak("${plan.summary}. Shall I save it?") {
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
                SpeechErrorKind.NoPermission -> useTextMode("Mic access is off. You can type instead.")
                SpeechErrorKind.Unavailable -> useTextMode("Voice isn't available on this phone. You can type instead.")
                SpeechErrorKind.Network -> _state.value = AgentUiState.Error("", "Voice needs internet on this phone right now. Try typing it.")
                SpeechErrorKind.NoMatch, SpeechErrorKind.Busy, SpeechErrorKind.Other ->
                    _state.value = AgentUiState.Error("", "Didn't catch that. Try again or type it.")
            }
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

/** A short spoken version of an answer: the headline and up to two lines. */
internal fun spokenFor(answer: AgentAnswer): String {
    val head = answer.headline.trimEnd('.', '!') + "."
    val lines = answer.lines.take(2).joinToString(" ") { "${it.label}, ${it.value}." }
    return listOf(head, lines).filter { it.isNotBlank() }.joinToString(" ")
}
