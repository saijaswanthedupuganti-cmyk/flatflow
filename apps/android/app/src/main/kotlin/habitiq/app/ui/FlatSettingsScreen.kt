package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import habitiq.app.flat.FlatViewModel
import habitiq.app.lib.formatInr
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqGroup
import habitiq.app.ui.components.HqRow
import habitiq.app.ui.components.HqRowChevron
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * Flat admin settings: rename the flat, set join mode, and open the room listing.
 * Reached from Profile -- distinct from [ManageFlatHub], the bottom-nav "Manage" operational hub
 * (Tasks/Expenses/Bills/attention items). Renamed from ManageFlatScreen to stop the two being
 * confused with each other (master spec section 4/57: one name per concept).
 *
 * The room listing is edited in the one "List a room" wizard (Discover), never in a separate form here,
 * so every field the wizard stores is kept when the listing is changed.
 */
@Composable
fun FlatSettingsScreen(viewModel: FlatViewModel, onBack: () -> Unit, onListRoom: () -> Unit = {}) {
    val c = LocalHqColors.current
    val flatInfo by viewModel.flatInfo.collectAsStateWithLifecycleCompat()
    val vacancy = flatInfo?.vacancy
    var flatName by remember(flatInfo?.name) { mutableStateOf(flatInfo?.name.orEmpty()) }
    var joinMode by remember(flatInfo?.joinMode) { mutableStateOf(flatInfo?.joinMode ?: "auto") }

    Column(Modifier.fillMaxSize().background(c.canvas)) {
    HqBackAppBar(title = "Flat settings", onBack = onBack)
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.xl).padding(bottom = HqSpacing.xl)) {
        Spacer(Modifier.height(HqSpacing.lg))
        HqTextField(
            value = flatName,
            onValueChange = { flatName = it },
            label = "Flat name",
            placeholder = "e.g. Sunrise Apartments 4B",
            imeAction = ImeAction.Done,
            onImeAction = if (flatName.isNotBlank()) ({ viewModel.renameFlat(flatName) }) else null
        )
        Spacer(Modifier.height(HqSpacing.sm))
        HqButton(text = "Save name", onClick = { viewModel.renameFlat(flatName) }, variant = HqButtonVariant.Secondary)
        Spacer(Modifier.height(HqSpacing.lg))
        Text("Join mode", style = HqType.titleSmall, color = c.textPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            HqChip(label = "Auto join", selected = joinMode == "auto", onClick = { joinMode = "auto"; viewModel.setJoinMode("auto") })
            HqChip(label = "Approval required", selected = joinMode == "approval", onClick = { joinMode = "approval"; viewModel.setJoinMode("approval") })
        }
        Spacer(Modifier.height(HqSpacing.xxl))
        Text("Room listing", style = HqType.titleSmall, color = c.textPrimary)
        Spacer(Modifier.height(HqSpacing.sm))
        HqGroup {
            HqRow(
                title = if (vacancy == null || vacancy.city.isBlank()) "List a room" else "Edit room listing",
                supporting = when {
                    vacancy == null || vacancy.city.isBlank() -> "Find a flatmate for a free room in Discover"
                    else -> listOfNotNull(
                        if (vacancy.active) "Live on Discover" else "Not live",
                        vacancy.rentPerHead?.let { "${formatInr(it)}/mo" },
                        vacancy.area.ifBlank { null },
                    ).joinToString(" · ")
                },
                trailing = { HqRowChevron() },
                onClick = onListRoom,
            )
        }
        flatInfo?.let {
            Spacer(Modifier.height(HqSpacing.lg))
            Text("Invite code: ${it.id}", style = HqType.bodySmall, color = c.textSecondary)
        }
    }
    }
}
