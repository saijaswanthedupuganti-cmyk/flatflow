package habitiq.app.agent

import android.speech.SpeechRecognizer
import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceInputTest {
    @Test fun `recogniser error codes map to plain kinds`() {
        assertEquals(SpeechErrorKind.NoMatch, speechErrorKind(SpeechRecognizer.ERROR_NO_MATCH))
        assertEquals(SpeechErrorKind.NoMatch, speechErrorKind(SpeechRecognizer.ERROR_SPEECH_TIMEOUT))
        assertEquals(SpeechErrorKind.NoPermission, speechErrorKind(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS))
        assertEquals(SpeechErrorKind.Network, speechErrorKind(SpeechRecognizer.ERROR_NETWORK))
        assertEquals(SpeechErrorKind.Network, speechErrorKind(SpeechRecognizer.ERROR_NETWORK_TIMEOUT))
        assertEquals(SpeechErrorKind.Network, speechErrorKind(SpeechRecognizer.ERROR_SERVER))
        assertEquals(SpeechErrorKind.Busy, speechErrorKind(SpeechRecognizer.ERROR_RECOGNIZER_BUSY))
        assertEquals(SpeechErrorKind.Unavailable, speechErrorKind(12)) // ERROR_LANGUAGE_NOT_SUPPORTED
        assertEquals(SpeechErrorKind.Unavailable, speechErrorKind(13)) // ERROR_LANGUAGE_UNAVAILABLE
        assertEquals(SpeechErrorKind.Other, speechErrorKind(-42))
    }

    @Test fun `mic loudness maps to 0 to 1`() {
        assertEquals(0f, rmsToLevel(-2f), 0.001f)
        assertEquals(0f, rmsToLevel(-10f), 0.001f)
        assertEquals(0.5f, rmsToLevel(4f), 0.001f)
        assertEquals(1f, rmsToLevel(10f), 0.001f)
        assertEquals(1f, rmsToLevel(20f), 0.001f)
    }
}
