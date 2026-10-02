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
import habitiq.app.ui.components.HqTitleOnlyAppBar
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.canvas)
            .verticalScroll(rememberScrollState())
    ) {
        HqTitleOnlyAppBar(onBack = onBack)
        Column(Modifier.padding(horizontal = HqSpacing.xxl), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("PERSONALISED SETUP", style = HqType.labelSmall, color = c.textBrand, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
            Text("Join a flat", style = HqType.titleLarge2, color = c.textPrimary)
            Text("Enter the invite shared by your flat admin", style = HqType.bodyMedium, color = c.textSecondary)
        }
        Column(modifier = Modifier.fillMaxWidth().padding(HqSpacing.xxl)) {
            HqTextField(
                value = code,
                onValueChange = {
                    code = it
                    if (preview != null) viewModel.clearPreview()
                },
                label = "Invite code",
                placeholder = "FLAT-A3B9",
                leadingIcon = Icons.Filled.VpnKey,
                keyboardType = KeyboardType.Ascii,
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
                onImeAction = if (preview == null && code.isNotBlank() && state !is FlatUiState.Loading) {
                    { viewModel.lookupFlat(code) }
                } else null
            )
            Spacer(Modifier.height(HqSpacing.lg))
            val found = preview
            if (found == null) {
                FlatRoleCallout(
                    accentColor = c.actionPrimaryBg,
                    text = "Ask your flat admin for the invite code."
                )
                Spacer(Modifier.height(HqSpacing.xl))
                HqButton(
                    text = "Continue",
                    onClick = { viewModel.lookupFlat(code) },
                    enabled = state !is FlatUiState.Loading,
                    loading = state is FlatUiState.Loading
                )
            } else {
                Row(
                    Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp)).background(c.surfaceSubtle).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically,
                ) {
                    habitiq.app.ui.components.HqIconTile(habitiq.app.ui.components.HqIcons.Home, habitiq.app.ui.components.HqTileTone.Teal)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("FLAT FOUND", style = HqType.labelSmall, color = c.textMuted, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                        Text(found.name, color = c.textPrimary, style = HqType.titleSmall2)
                        Text("${found.memberCount} members", color = c.textSecondary, style = HqType.bodyMedium)
                    }
                    habitiq.app.ui.components.HqBadge(if (found.memberCount >= 8) "Full" else "Active", if (found.memberCount >= 8) habitiq.app.ui.components.HqBadgeTone.Warning else habitiq.app.ui.components.HqBadgeTone.Success)
                }
                Text(
                    if (found.joinMode == "approval") {
                        "Admin approval is required. You'll join after they accept."
                    } else {
                        "You're joining this flat."
                    },
                    color = c.textSecondary,
                    style = HqType.bodyMedium,
                    modifier = Modifier.padding(top = HqSpacing.md)
                )
                Spacer(Modifier.height(HqSpacing.xl))
                HqButton(
                    text = when {
                        found.memberCount >= 8 -> "This flat is full"
                        found.joinMode == "approval" -> "Request to join"
                        else -> "Join Flat"
                    },
                    onClick = { viewModel.joinFlat(code) },
                    enabled = state !is FlatUiState.Loading && found.memberCount < 8,
                    loading = state is FlatUiState.Loading
                )
                HqButton(text = "Use another code", onClick = { viewModel.clearPreview() }, variant = HqButtonVariant.Secondary)
            }
            when (val current = state) {
                is FlatUiState.Loading -> HqInlineLoading(
                    label = if (preview == null) "Checking…" else "Joining…",
                    modifier = Modifier.padding(top = HqSpacing.md)
                )
                is FlatUiState.Error -> HqInlineError(
                    current.message,
                    modifier = Modifier.padding(top = HqSpacing.md)
                )
                is FlatUiState.Success -> if (pendingApproval) {
                    Text(
                        "Join request sent. The flat admin will approve you.",
                        color = c.statusSuccessFg,
                        style = HqType.bodySmall,
                        modifier = Modifier.padding(top = HqSpacing.md)
                    )
                }
                else -> {}
            }
        }
    }
}
