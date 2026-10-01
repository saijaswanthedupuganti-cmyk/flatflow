package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseUser
import habitiq.app.settings.AppPreferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import habitiq.app.settings.DeleteAccountState
import habitiq.app.settings.SettingsViewModel
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqConfirmDialog
import habitiq.app.ui.components.HqInlineError
import habitiq.app.ui.components.HqInlineLoading
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    user: FirebaseUser?,
    viewModel: SettingsViewModel,
    onBack: () -> Unit = {},
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

    val c = LocalHqColors.current
    val deleting = deleteState is DeleteAccountState.Deleting

    Column(Modifier.fillMaxSize().background(c.background)) {
        HqBackAppBar(title = "Preferences", onBack = onBack)
        Column(Modifier.padding(horizontal = HqSpacing.xl)) {
            Text(user?.email ?: "unknown", style = HqType.bodyMedium, color = c.textSecondary)
            Spacer(Modifier.height(HqSpacing.xxl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Biometric app lock", style = HqType.titleSmall, color = c.textPrimary)
                    Text("Require fingerprint/face when returning to app", style = HqType.bodySmall, color = c.textSecondary)
                }
                Switch(
                    checked = biometricEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch { prefs.setBiometricLockEnabled(enabled) }
                    }
                )
            }

            Spacer(Modifier.height(HqSpacing.xxl))
            // Sign out is reversible, unlike account deletion -- keep it visually distinct from
            // the truly destructive action below it instead of both reading equally severe.
            HqButton(text = "Sign out", onClick = onSignOut, variant = HqButtonVariant.Secondary, enabled = !deleting)
            Spacer(Modifier.height(HqSpacing.md))
            HqButton(text = "Delete Account", onClick = { showConfirmDialog = true }, variant = HqButtonVariant.Destructive, enabled = !deleting)
            when (val current = deleteState) {
                is DeleteAccountState.Deleting -> {
                    Spacer(Modifier.height(HqSpacing.md))
                    HqInlineLoading(label = "Deleting your account…")
                }
                is DeleteAccountState.Error -> {
                    Spacer(Modifier.height(HqSpacing.md))
                    HqInlineError(message = current.message)
                }
                else -> {}
            }
        }
    }

    if (showConfirmDialog) {
        HqConfirmDialog(
            title = "Delete your account?",
            message = "This permanently deletes your account. This cannot be undone.",
            confirmLabel = "Delete",
            onConfirm = { showConfirmDialog = false; viewModel.deleteAccount() },
            onDismiss = { showConfirmDialog = false },
        )
    }
}
