package habitiq.app.ui

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
            .background(c.background)
            .verticalScroll(rememberScrollState())
    ) {
        HqTitleOnlyAppBar(onBack = onBack)
        FlatOnboardingHeader(
            accentColor = c.brandPrimary,
            imageRes = R.drawable.onboard_join,
            titleLine1 = "Join Your",
            titleLine2 = "Crew.",
            subtitle = "Have an invite code? Walk right in. Expenses, chores, bills — already set up and waiting for you.",
            benefits = listOf(
                "Step in instantly — no setup",
                "See balances and shared expenses",
                "Stay synced in real-time"
            )
        )

        Column(modifier = Modifier.fillMaxWidth().padding(HqSpacing.xxl)) {
            HqTextField(
                value = code,
                onValueChange = {
                    code = it
                    if (preview != null) viewModel.clearPreview()
                },
                label = "Invite code",
                placeholder = "FLAT-A3B9",
                leadingIcon = Icons.Filled.VpnKey
            )
            Spacer(Modifier.height(HqSpacing.lg))
            val found = preview
            if (found == null) {
                FlatRoleCallout(
                    accentColor = c.brandPrimary,
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
                Text("✓ ${found.name}", color = c.textPrimary, style = HqType.titleMedium)
                Text(
                    "${found.memberCount} members",
                    color = c.textSecondary,
                    style = HqType.bodySmall,
                    modifier = Modifier.padding(top = HqSpacing.xs)
                )
                Text(
                    if (found.joinMode == "approval") {
                        "This flat needs admin approval. You'll join after they accept."
                    } else {
                        "You're joining this flat."
                    },
                    color = c.textSecondary,
                    style = HqType.bodySmall,
                    modifier = Modifier.padding(top = HqSpacing.sm)
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
                HqTextButton(text = "Different code", onClick = { viewModel.clearPreview() })
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
                        color = c.success,
                        style = HqType.bodySmall,
                        modifier = Modifier.padding(top = HqSpacing.md)
                    )
                }
                else -> {}
            }
        }
    }
}
