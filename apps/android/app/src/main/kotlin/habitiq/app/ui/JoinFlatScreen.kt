package habitiq.app.ui

import habitiq.app.ui.components.HqButtonVariant
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.draw.clip
import habitiq.app.ui.theme.HqIconSize
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import habitiq.app.R
import habitiq.app.flats.FlatUiState
import habitiq.app.flats.JoinFlatViewModel
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqInlineError
import habitiq.app.ui.components.HqInlineLoading
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.components.HqBackAppBar
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

@Composable
fun JoinFlatScreen(
    viewModel: JoinFlatViewModel,
    onJoined: (flatId: String) -> Unit,
    onBack: () -> Unit = {}
) {
    val c = LocalHqColors.current
    var code by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val joinedFlatId by viewModel.joinedFlatId.collectAsStateWithLifecycleCompat()
    val preview by viewModel.preview.collectAsStateWithLifecycleCompat()
    val pendingApproval by viewModel.pendingApproval.collectAsStateWithLifecycleCompat()

    LaunchedEffect(state, joinedFlatId) {
        val flatId = joinedFlatId
        if (state is FlatUiState.Success && flatId != null) {
            onJoined(flatId)
        }
    }

    val loading = state is FlatUiState.Loading
    val found = preview
    Column(Modifier.fillMaxSize().background(c.canvas).navigationBarsPadding()) {
        HqBackAppBar(title = "Join a flat", onBack = onBack)
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.screenHorizontal)
        ) {
            Text(
                "Ask your flatmate for the invite code.", style = HqType.bodyLarge, color = c.textSecondary,
                modifier = Modifier.padding(bottom = HqSpacing.xl)
            )
            HqTextField(
                value = code,
                onValueChange = {
                    code = it
                    if (preview != null) viewModel.clearPreview()
                },
                label = "Invite code",
                placeholder = "K7QM2P",
                leadingIcon = Icons.Filled.VpnKey,
                keyboardType = KeyboardType.Ascii,
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
                onImeAction = if (found == null && code.isNotBlank() && !loading) {
                    { viewModel.lookupFlat(code) }
                } else null
            )
            if (found != null) {
                Row(
                    Modifier.padding(top = HqSpacing.lg).fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(habitiq.app.ui.theme.HqRadius.card))
                        .background(c.surfaceSubtle).padding(HqSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(HqSpacing.md), verticalAlignment = Alignment.CenterVertically,
                ) {
                    habitiq.app.ui.components.HqIconTile(habitiq.app.ui.components.HqIcons.Home, habitiq.app.ui.components.HqTileTone.Teal)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                        Text("Flat found", style = HqType.labelMedium, color = c.textSecondary)
                        Text(found.name, color = c.textPrimary, style = HqType.titleSmall2)
                        Text("${found.memberCount} ${if (found.memberCount == 1) "member" else "members"}", color = c.textSecondary, style = HqType.bodyMedium)
                    }
                    if (found.memberCount >= 8) habitiq.app.ui.components.HqBadge("Full", habitiq.app.ui.components.HqBadgeTone.Warning)
                }
                Text(
                    when {
                        found.memberCount >= 8 -> "This flat has 8 members, which is the most a flat can have."
                        found.joinMode == "approval" -> "The flat admin approves new members. You'll join once they approve."
                        else -> "You'll join this flat straight away."
                    },
                    color = c.textSecondary,
                    style = HqType.bodyMedium,
                    modifier = Modifier.padding(top = HqSpacing.md)
                )
            }
            when (val current = state) {
                is FlatUiState.Loading -> HqInlineLoading(
                    label = if (found == null) "Finding flat…" else "Joining…",
                    modifier = Modifier.padding(top = HqSpacing.md)
                )
                is FlatUiState.Error -> HqInlineError(
                    current.message,
                    modifier = Modifier.padding(top = HqSpacing.md)
                )
                is FlatUiState.Success -> if (pendingApproval) {
                    Text(
                        "Request sent. You'll join once the flat admin approves.",
                        color = c.statusSuccessFg,
                        style = HqType.bodyMedium,
                        modifier = Modifier.padding(top = HqSpacing.md).semantics { liveRegion = LiveRegionMode.Polite }
                    )
                }
                else -> {}
            }
            Spacer(Modifier.height(HqSpacing.xxl))
        }
        // Main action stays at the bottom, in the thumb zone.
        Column(
            Modifier.padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.md),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)
        ) {
            if (found == null) {
                HqButton(
                    text = "Find flat",
                    onClick = { viewModel.lookupFlat(code) },
                    enabled = !loading,
                    loading = loading
                )
            } else {
                HqButton(
                    text = when {
                        found.memberCount >= 8 -> "This flat is full"
                        found.joinMode == "approval" -> "Send request to join"
                        else -> "Join flat"
                    },
                    onClick = { viewModel.joinFlat(code) },
                    enabled = !loading && found.memberCount < 8,
                    loading = loading
                )
                HqTextButton(
                    text = "Use another code", onClick = { viewModel.clearPreview() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
