package habitiq.app.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import habitiq.app.ui.components.HqBottomSheet
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/** Global Quick Actions launcher opened from the AppShell's central `+` (design doc section 41). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlusActionSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onAddTask: () -> Unit,
    onAddExpense: () -> Unit,
    onBillsSettlements: () -> Unit,
    onInviteRoommate: () -> Unit,
    onCreateDiscoveryPost: () -> Unit,
    discoverContext: Boolean = false
) {
    if (!visible) return
    val c = LocalHqColors.current
    HqBottomSheet(onDismiss = onDismiss, title = if (discoverContext) "Create a Discovery post" else "Quick Add") {
        if (discoverContext) {
            Text("Vacancy and looking posts are different types.", style = HqType.bodySmall, color = c.textSecondary)
            Spacer(Modifier.height(HqSpacing.lg))
            SheetAction("Post a vacancy or looking post", onCreateDiscoveryPost)
        } else {
            SheetAction("Add Task", onAddTask)
            Spacer(Modifier.height(HqSpacing.sm))
            SheetAction("Add Expense", onAddExpense)
            Spacer(Modifier.height(HqSpacing.sm))
            SheetAction("Bills & Settlements", onBillsSettlements)
            Spacer(Modifier.height(HqSpacing.sm))
            SheetAction("Invite Roommate", onInviteRoommate)
        }
        Spacer(Modifier.height(HqSpacing.xxl))
    }
}

@Composable
private fun SheetAction(label: String, onClick: () -> Unit) =
    HqButton(text = label, onClick = onClick, variant = HqButtonVariant.Secondary)
