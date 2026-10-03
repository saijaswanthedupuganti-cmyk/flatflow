package habitiq.app.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import habitiq.app.agent.AgentAnswer
import habitiq.app.agent.AgentCard
import habitiq.app.agent.AgentInputMode
import habitiq.app.agent.AgentLink
import habitiq.app.agent.AgentUiState
import habitiq.app.agent.AnswerLine
import habitiq.app.ui.agent.VoiceOverlay
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VoiceOverlayScreenshotTest : ScreenshotHarness() {
    /** Stand-in for the blurred app: warm evening photo tones like the Home hero. */
    @Composable
    private fun FakeApp() = Box(
        Modifier.fillMaxSize().blur(28.dp).background(
            Brush.verticalGradient(listOf(Color(0xFF3A2A2A), Color(0xFF6B4A35), Color(0xFF2E3A48), Color(0xFFE9EEF3)))
        )
    )

    private fun shot(name: String, state: AgentUiState, mode: AgentInputMode = AgentInputMode.Voice) = shoot(name, dark = true) {
        FakeApp()
        VoiceOverlay(
            state, mode, level = 0.55f, micCenter = null,
            onClose = {}, onTalk = {}, onStop = {}, onSubmit = {}, onChoose = {}, onOpen = {},
            onUseText = {}, onUseVoice = {}, onAllowMic = {}, animate = false,
        )
    }

    @Test fun listening() = shot("voice-listening", AgentUiState.Listening(""))
    @Test fun transcript() = shot("voice-transcript", AgentUiState.Listening("I just spent five hundred on groceries"))
    @Test fun expense() = shot(
        "voice-expense",
        AgentUiState.Preview("I just spent 500 on groceries", "Add ₹500 for Groceries, split 4 ways",
            AgentCard.Expense("Groceries", 50_000, "lifestyle", "You", listOf("You", "Ravi", "Priya", "Arjun"), everyone = true)),
    )
    @Test fun answer() = shot(
        "voice-answer",
        AgentUiState.Answer("What do I owe?", AgentAnswer("You owe ₹350", listOf(AnswerLine("Pay Ravi", "₹350"), AnswerLine("Priya owes you", "₹150")), AgentLink.BALANCES)),
    )
    @Test fun typing() = shot("voice-typing", AgentUiState.Idle(), AgentInputMode.Text)
}
