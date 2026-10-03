package habitiq.app.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AgentInputMode { Voice, Text }

sealed interface AgentUiState {
    data class Idle(val notice: String? = null) : AgentUiState
    data class Listening(val partial: String) : AgentUiState
    data class Checking(val transcript: String) : AgentUiState
    data class Answer(val transcript: String, val answer: AgentAnswer) : AgentUiState
    data class Preview(val transcript: String, val summary: String, val card: AgentCard) : AgentUiState
    data class Clarify(val transcript: String, val question: String, val options: List<ClarifyOption>) : AgentUiState
    data class NotUnderstood(val transcript: String, val message: String) : AgentUiState
    data class Error(val transcript: String, val message: String) : AgentUiState
}

private const val NOT_YET = "I can't do that one yet. Try \"What do I owe?\", \"What's due today?\" or \"Bought milk 60\"."

/**
 * Drives the agent sheet: Idle → Listening → Checking → Answer / Preview / Clarify, with errors
 * going back to Idle or Error. M1 never writes data; previews are read-only.
 */
class AgentViewModel(
    private val household: () -> HouseholdState,
    private val speech: SpeechSource,
    private val today: () -> LocalDate = { LocalDate.now() },
    private val checkingMs: Long = 400,
    private val listenTimeoutMs: Long = 10_000,
) : ViewModel() {
    private val _state = MutableStateFlow<AgentUiState>(AgentUiState.Idle())
    val state: StateFlow<AgentUiState> = _state.asStateFlow()

    private val _mode = MutableStateFlow(AgentInputMode.Voice)
    val mode: StateFlow<AgentInputMode> = _mode.asStateFlow()

    /** Kept apart from [state] so the listening animation can follow the voice without recomposing the whole sheet. */
    private val _level = MutableStateFlow(0f)
    val level: StateFlow<Float> = _level.asStateFlow()

    private var work: Job? = null
    private var watchdog: Job? = null

    init {
        viewModelScope.launch { speech.events.collect(::onSpeech) }
    }

    fun startListening() {
        work?.cancel()
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
        if (notice != null || _state.value is AgentUiState.Listening) _state.value = AgentUiState.Idle(notice)
    }

    fun useVoiceMode() { _mode.value = AgentInputMode.Voice }

    fun reset() {
        work?.cancel()
        watchdog?.cancel()
        speech.cancel()
        _level.value = 0f
        _state.value = AgentUiState.Idle()
    }

    fun submitText(text: String) {
        val transcript = text.trim()
        if (transcript.isEmpty()) return
        work?.cancel()
        _state.value = AgentUiState.Checking(transcript)
        work = viewModelScope.launch {
            delay(checkingMs) // long enough for "Checking your flat" to read
            ContentGuard.reasonToBlock(transcript)?.let {
                _state.value = AgentUiState.NotUnderstood(transcript, it)
                return@launch
            }
            val home = household()
            _state.value = when (val result = LocalIntentParser.parse(transcript, home, today())) {
                ParseResult.NeedsModel -> AgentUiState.NotUnderstood(transcript, NOT_YET)
                is ParseResult.Confident -> stateFor(transcript, result.plan, home)
            }
        }
    }

    fun choose(option: ClarifyOption) {
        val transcript = (_state.value as? AgentUiState.Clarify)?.transcript ?: return
        _state.value = stateFor(transcript, option.plan, household())
    }

    private fun stateFor(transcript: String, plan: AgentPlan, home: HouseholdState): AgentUiState = when (plan) {
        is AgentPlan.Answer -> AgentUiState.Answer(transcript, QueryResolver.resolve(plan.query, home, today()))
        is AgentPlan.Actions -> AgentUiState.Preview(transcript, plan.summary, cardFor(plan, home))
        is AgentPlan.Clarify -> AgentUiState.Clarify(transcript, plan.question, plan.options)
        is AgentPlan.Unsupported -> AgentUiState.NotUnderstood(transcript, plan.reason)
    }

    private fun onSpeech(event: SpeechEvent) {
        // Results that arrive after the person reset, switched to typing or timed out belong to a dead session.
        if (_state.value !is AgentUiState.Listening) return
        if (event is SpeechEvent.Partial || event is SpeechEvent.Level) armWatchdog() else watchdog?.cancel()
        if (event !is SpeechEvent.Level && event !is SpeechEvent.Partial) _level.value = 0f
        when (event) {
            is SpeechEvent.Level -> if (_state.value is AgentUiState.Listening) _level.value = event.level
            is SpeechEvent.Partial -> if (_state.value is AgentUiState.Listening) _state.value = AgentUiState.Listening(event.text)
            is SpeechEvent.Final -> submitText(event.text)
            is SpeechEvent.Error -> when (event.kind) {
                SpeechErrorKind.NoPermission -> useTextMode("Mic access is off. You can type instead.")
                SpeechErrorKind.Unavailable -> useTextMode("Voice isn't available on this phone. You can type instead.")
                SpeechErrorKind.Network -> _state.value = AgentUiState.Error("", "Voice needs internet on this phone right now. Try typing it.")
                SpeechErrorKind.NoMatch, SpeechErrorKind.Busy, SpeechErrorKind.Other ->
                    _state.value = AgentUiState.Error("", "Didn't catch that. Try again or type it.")
            }
        }
    }

    override fun onCleared() = speech.release()
}
