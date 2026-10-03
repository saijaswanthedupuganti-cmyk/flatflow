package habitiq.app.screenshots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import habitiq.app.agent.AgentAnswer
import habitiq.app.agent.AgentCard
import habitiq.app.agent.AgentInputMode
import habitiq.app.agent.AgentLink
import habitiq.app.agent.AgentPlan
import habitiq.app.agent.AgentUiState
import habitiq.app.agent.AnswerLine
import habitiq.app.agent.ClarifyOption
import habitiq.app.ui.agent.AgentSheetBody
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AgentSheetScreenshotTest : ScreenshotHarness() {
    private val everyone = listOf("You", "Ravi", "Priya", "Arjun")

    private val voice = listOf(
        AgentUiState.Idle(),
        AgentUiState.Listening("I just spent five hundred on"),
        AgentUiState.Answer("What do I owe?", AgentAnswer("You owe ₹350", listOf(AnswerLine("Pay Ravi", "₹350")), AgentLink.BALANCES)),
        AgentUiState.Clarify("paid ravi 200", "Which Ravi?", listOf(
            ClarifyOption("Ravi Kumar", AgentPlan.Unsupported("")), ClarifyOption("Ravi Teja", AgentPlan.Unsupported("")))),
    )
    private val cards = listOf(
        AgentUiState.Preview("Bought groceries for 560", "Add ₹560 for Groceries, split 4 ways",
            AgentCard.Expense("Groceries", 56_000, "lifestyle", "You", everyone, everyone = true)),
        AgentUiState.Preview("I just spent 500", "Add ₹500 for Expense, split 4 ways",
            AgentCard.Expense("Expense", 50_000, "other", "You", everyone, everyone = true)),
        AgentUiState.Preview("Paid Ravi 200", "Record ₹200 you paid Ravi", AgentCard.Payment("You", "Ravi", 20_000)),
        AgentUiState.Idle("Mic access is off. You can type instead."),
    )

    private fun page(name: String, dark: Boolean, states: List<AgentUiState>, mode: AgentInputMode) = shoot(name, dark = dark) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
            states.forEach { state ->
                AgentSheetBody(state, mode, level = 0.6f, onTalk = {}, onStop = {}, onSubmit = {}, onChoose = {}, onOpen = {},
                    onUseText = {}, onUseVoice = {}, onAllowMic = {}, animate = false)
            }
        }
    }

    @Test fun voiceLight() = page("agent-voice-light", false, voice, AgentInputMode.Voice)
    @Test fun voiceDark() = page("agent-voice-dark", true, voice, AgentInputMode.Voice)
    @Test fun cardsLight() = page("agent-cards-light", false, cards, AgentInputMode.Text)
    @Test fun cardsDark() = page("agent-cards-dark", true, cards, AgentInputMode.Text)
}
