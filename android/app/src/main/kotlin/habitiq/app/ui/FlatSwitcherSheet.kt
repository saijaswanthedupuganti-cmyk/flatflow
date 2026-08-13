package habitiq.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import habitiq.app.flat.FlatViewModel
import habitiq.app.ui.theme.FigmaColors

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

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = FigmaColors.Background) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Your flats", fontWeight = FontWeight.Bold, color = FigmaColors.Ink)
            flatIds.forEach { id ->
                val selected = id == activeId
                Button(
                    onClick = { viewModel.switchFlat(id); onDismiss() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) FigmaColors.Primary else FigmaColors.Surface,
                        contentColor = if (selected) androidx.compose.ui.graphics.Color.White else FigmaColors.Ink
                    )
                ) { Text(if (selected) "$id (active)" else id) }
            }
            OutlinedButton(onClick = { onDismiss(); onCreateFlat() }, modifier = Modifier.fillMaxWidth()) { Text("Create new flat") }
            OutlinedButton(onClick = { onDismiss(); onJoinFlat() }, modifier = Modifier.fillMaxWidth()) { Text("Join with code") }
        }
    }
}
