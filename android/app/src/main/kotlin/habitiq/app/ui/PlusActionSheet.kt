package habitiq.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.ui.theme.FigmaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlusActionSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onAddTask: () -> Unit,
    onAddExpense: () -> Unit,
    onInviteRoommate: () -> Unit
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FigmaColors.Background
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text("Quick Add", color = FigmaColors.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            SheetAction("Add Task", onAddTask)
            Spacer(Modifier.height(8.dp))
            SheetAction("Add Expense", onAddExpense)
            Spacer(Modifier.height(8.dp))
            SheetAction("Invite Roommate", onInviteRoommate)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SheetAction(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FigmaColors.Surface,
            contentColor = FigmaColors.Ink
        )
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}
