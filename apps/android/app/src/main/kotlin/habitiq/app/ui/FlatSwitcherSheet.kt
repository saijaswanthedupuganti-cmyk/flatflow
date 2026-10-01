package habitiq.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import habitiq.app.flat.FlatViewModel
import habitiq.app.ui.components.HqBottomSheet
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.theme.HqSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlatSwitcherSheet(
    viewModel: FlatViewModel,
    visible: Boolean,
    onDismiss: () -> Unit,
    onCreateFlat: () -> Unit,
    onJoinFlat: () -> Unit
) {
    if (!visible) return
    val profile by viewModel.userProfile.collectAsStateWithLifecycleCompat()
    val activeId by viewModel.flatId.collectAsStateWithLifecycleCompat()
    val flatIds = profile?.flatIds.orEmpty()

    HqBottomSheet(onDismiss = onDismiss, title = "Your flats") {
        Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            flatIds.forEach { id ->
                val selected = id == activeId
                HqButton(
                    text = if (selected) "$id (active)" else id,
                    onClick = { viewModel.switchFlat(id); onDismiss() },
                    variant = if (selected) HqButtonVariant.Primary else HqButtonVariant.Secondary
                )
            }
            HqButton(text = "Create new flat", onClick = { onDismiss(); onCreateFlat() }, variant = HqButtonVariant.Secondary)
            HqButton(text = "Join with code", onClick = { onDismiss(); onJoinFlat() }, variant = HqButtonVariant.Secondary)
        }
    }
}
