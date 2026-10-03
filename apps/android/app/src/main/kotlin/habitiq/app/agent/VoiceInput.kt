package habitiq.app.agent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface SpeechEvent {
    data class Partial(val text: String) : SpeechEvent
    data class Final(val text: String) : SpeechEvent
    /** Mic loudness 0..1, for the listening animation. */
    data class Level(val level: Float) : SpeechEvent
    data class Error(val kind: SpeechErrorKind) : SpeechEvent
}

/** SpeechRecognizer reports roughly -2..10 dB; map that to 0..1. */
fun rmsToLevel(rmsDb: Float): Float = ((rmsDb + 2f) / 12f).coerceIn(0f, 1f)

enum class SpeechErrorKind { NoMatch, NoPermission, Network, Busy, Unavailable, Other }

/** Speech-to-text behind an interface so the view model can be tested with a fake. */
interface SpeechSource {
    val events: Flow<SpeechEvent>
    fun start()
    fun stop()
    /** Abandon the current session without delivering a result. */
    fun cancel()
    fun release()
}

fun speechErrorKind(code: Int): SpeechErrorKind = when (code) {
    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechErrorKind.NoMatch
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SpeechErrorKind.NoPermission
    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT, SpeechRecognizer.ERROR_SERVER -> SpeechErrorKind.Network
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> SpeechErrorKind.Busy
    // 12/13: language not supported / unavailable (e.g. no offline en-IN pack). Retrying never helps.
    12, 13 -> SpeechErrorKind.Unavailable
    else -> SpeechErrorKind.Other
}

/**
 * Android's recogniser in en-IN, on-device where the phone supports it. Audio stays with the
 * system recogniser; Oddroof only ever sees the text. Call [start] and [stop] on the main thread.
 */
class AndroidVoiceInput(private val context: Context) : SpeechSource {
    private val _events = MutableSharedFlow<SpeechEvent>(extraBufferCapacity = 64)
    override val events: Flow<SpeechEvent> = _events.asSharedFlow()
    private var recognizer: SpeechRecognizer? = null

    private val listener = object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            _events.tryEmit(if (text.isNullOrBlank()) SpeechEvent.Error(SpeechErrorKind.NoMatch) else SpeechEvent.Final(text))
        }
        override fun onPartialResults(partialResults: Bundle?) {
            partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                ?.takeIf { it.isNotBlank() }?.let { _events.tryEmit(SpeechEvent.Partial(it)) }
        }
        override fun onError(error: Int) { _events.tryEmit(SpeechEvent.Error(speechErrorKind(error))) }
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) { _events.tryEmit(SpeechEvent.Level(rmsToLevel(rmsdB))) }
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    override fun start() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _events.tryEmit(SpeechEvent.Error(SpeechErrorKind.Unavailable))
            return
        }
        val r = recognizer ?: SpeechRecognizer.createSpeechRecognizer(context).also {
            it.setRecognitionListener(listener)
            recognizer = it
        }
        r.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        })
    }

    override fun stop() { recognizer?.stopListening() }

    override fun cancel() { recognizer?.cancel() }

    override fun release() {
        recognizer?.destroy()
        recognizer = null
    }
}
