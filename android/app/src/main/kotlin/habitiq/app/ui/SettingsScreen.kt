package habitiq.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import habitiq.app.settings.AppPreferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import habitiq.app.settings.DeleteAccountState
import habitiq.app.settings.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    user: FirebaseUser?,
    viewModel: SettingsViewModel,
    onSignOut: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }
    val scope = rememberCoroutineScope()
    val biometricEnabled by prefs.isBiometricLockEnabled.collectAsStateWithLifecycle(initialValue = false)

    LaunchedEffect(deleteState) {
        if (deleteState is DeleteAccountState.Deleted) onAccountDeleted()
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Settings")
        Text(user?.email ?: "unknown")
        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text("Biometric app lock")
                Text("Require fingerprint/face when returning to app", style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = biometricEnabled,
                onCheckedChange = { enabled ->
                    scope.launch { prefs.setBiometricLockEnabled(enabled) }
                }
            )
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = onSignOut, enabled = deleteState !is DeleteAccountState.Deleting) {
            Text("Sign out")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { showConfirmDialog = true },
            enabled = deleteState !is DeleteAccountState.Deleting,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
        ) { Text("Delete Account") }
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
                TextButton(onClick = { showConfirmDialog = false; viewModel.deleteAccount() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}
