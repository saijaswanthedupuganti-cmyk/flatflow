package habitiq.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import habitiq.app.settings.DeleteAccountState
import habitiq.app.settings.SettingsViewModel

@Composable
fun SettingsScreen(
    user: FirebaseUser?,
    viewModel: SettingsViewModel,
    onSignOut: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycleCompat()

    LaunchedEffect(deleteState) {
        if (deleteState is DeleteAccountState.Deleted) {
            onAccountDeleted()
        }
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Settings")
        Text(user?.email ?: "unknown")
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onSignOut,
            enabled = deleteState !is DeleteAccountState.Deleting
        ) {
            Text("Sign out")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { showConfirmDialog = true },
            enabled = deleteState !is DeleteAccountState.Deleting,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
        ) {
            Text("Delete Account")
        }
        when (val current = deleteState) {
            is DeleteAccountState.Deleting -> Text("Deleting your account…")
            is DeleteAccountState.Error -> Text(current.message)
            else -> {}
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Delete your account?") },
            text = { Text("This permanently deletes your account. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    viewModel.deleteAccount()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
