package habitiq.app.ui.agent

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import habitiq.app.agent.AgentCard
import habitiq.app.agent.AgentInputMode
import habitiq.app.agent.AgentLink
import habitiq.app.agent.AgentUiState
import habitiq.app.agent.ClarifyOption
import habitiq.app.ui.components.HqBottomSheet
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqChipFlow
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import habitiq.app.ui.theme.hqReduceMotion

/** First things to try. Tapping one runs it, so a new user sees a real card in one tap. */
private val STARTERS = listOf("I just spent 500", "What do I owe?", "What's due today?")

/** The agent in a regular bottom sheet over the current screen. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AgentSheet(
    state: AgentUiState, mode: AgentInputMode, level: Float, onDismiss: () -> Unit,
    onTalk: () -> Unit, onStop: () -> Unit, onSubmit: (String) -> Unit, onChoose: (ClarifyOption) -> Unit,
    onOpen: (AgentLink) -> Unit, onUseText: () -> Unit, onUseVoice: () -> Unit, onAllowMic: () -> Unit,
) {
    HqBottomSheet(onDismiss = onDismiss) {
        AgentSheetBody(state, mode, level, onTalk, onStop, onSubmit, onChoose, onOpen, onUseText, onUseVoice, onAllowMic,
            animate = !hqReduceMotion())
    }
}

@Composable
fun AgentSheetBody(
    state: AgentUiState, mode: AgentInputMode, level: Float,
    onTalk: () -> Unit, onStop: () -> Unit, onSubmit: (String) -> Unit, onChoose: (ClarifyOption) -> Unit,
    onOpen: (AgentLink) -> Unit, onUseText: () -> Unit, onUseVoice: () -> Unit, onAllowMic: () -> Unit,
    animate: Boolean = true,
) {
    val c = LocalHqColors.current
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp).animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(HqSpacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Oddroof", style = HqType.labelMedium, color = c.textSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = if (mode == AgentInputMode.Voice) onUseText else onUseVoice) {
                Icon(
                    if (mode == AgentInputMode.Voice) Icons.Rounded.Keyboard else Icons.Rounded.Mic,
                    contentDescription = if (mode == AgentInputMode.Voice) "Type instead" else "Talk instead",
                    tint = c.iconDefault,
                )
            }
        }

        Column(
            Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
            verticalArrangement = Arrangement.spacedBy(HqSpacing.sm),
        ) {
            when (state) {
                is AgentUiState.Idle -> {
                    Text("What can I take care of?", style = HqType.titleMedium2, color = c.textPrimary)
                    Text("Just say it, like \"I spent 500 on groceries\". I'll do the forms.", style = HqType.bodyMedium, color = c.textSecondary)
                    state.notice?.let {
                        Text(it, style = HqType.bodyMedium, color = c.statusWarningFg)
                        HqButton("Allow mic", onAllowMic, variant = HqButtonVariant.Tertiary, fullWidth = false)
                    }
                    HqChipFlow { STARTERS.forEach { HqChip(label = it, onClick = { onSubmit(it) }) } }
                }
                is AgentUiState.Listening -> {
                    VoiceAura(level, animate, Modifier.align(Alignment.CenterHorizontally))
                    Text(
                        state.partial.ifEmpty { "Listening…" },
                        style = HqType.titleMedium2,
                        color = if (state.partial.isEmpty()) c.textMuted else c.textPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                is AgentUiState.Checking -> {
                    Transcript(state.transcript)
                    Text("Checking your flat…", style = HqType.bodyMedium, color = c.textSecondary)
                }
                is AgentUiState.Answer -> {
                    Transcript(state.transcript)
                    AnswerCard(state.answer.headline, state.answer.lines)
                    state.answer.link?.let { HqButton(it.label, { onOpen(it) }, variant = HqButtonVariant.Secondary) }
                }
                is AgentUiState.Preview -> {
                    Transcript(state.transcript)
                    when (val card = state.card) {
                        is AgentCard.Expense -> ExpenseCard(card, animate)
                        is AgentCard.Payment -> PaymentCard(card, animate)
                    }
                    Text("Saving from here arrives in the next update.", style = HqType.bodySmall, color = c.textMuted)
                    HqButton(AgentLink.EXPENSES.label, { onOpen(AgentLink.EXPENSES) }, variant = HqButtonVariant.Secondary)
                }
                is AgentUiState.Clarify -> {
                    Transcript(state.transcript)
                    Text(state.question, style = HqType.titleMedium2, color = c.textPrimary)
                    HqChipFlow { state.options.forEach { o -> HqChip(label = o.label, onClick = { onChoose(o) }) } }
                }
                is AgentUiState.NotUnderstood -> {
                    Transcript(state.transcript)
                    Text(state.message, style = HqType.rowTitle, color = c.textPrimary)
                }
                is AgentUiState.Error -> {
                    if (state.transcript.isNotEmpty()) Transcript(state.transcript)
                    Text(state.message, style = HqType.rowTitle, color = c.textPrimary)
                }
            }
        }

        if (mode == AgentInputMode.Text) {
            var text by rememberSaveable { mutableStateOf("") }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                HqTextField(value = text, onValueChange = { text = it }, label = "Ask or tell Oddroof", modifier = Modifier.weight(1f),
                    placeholder = "e.g. I spent 500 on groceries")
                IconButton(onClick = { onSubmit(text); text = "" }, enabled = text.isNotBlank(), modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send", tint = c.textBrand)
                }
            }
        } else if (state is AgentUiState.Listening) {
            HqButton("Done", onStop, variant = HqButtonVariant.Secondary)
        } else {
            HqButton(if (state is AgentUiState.Idle) "Tap to talk" else "Say something else", onTalk, leadingIcon = Icons.Rounded.Mic)
        }
    }
}

@Composable
private fun Transcript(text: String) {
    val c = LocalHqColors.current
    Text("“$text”", style = HqType.rowTitle, color = c.textSecondary)
}
