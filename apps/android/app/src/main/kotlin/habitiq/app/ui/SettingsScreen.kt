package habitiq.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        habitiq.app.ui.components.HqPageHeader(
            title = "Privacy & Security",
            subtitle = "Manage your account preferences",
            onBack = onBack,
            modifier = Modifier.padding(horizontal = HqSpacing.screenHorizontal).padding(top = HqSpacing.sm),
        )
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.screenHorizontal)) {
            Text(user?.email ?: "unknown", style = HqType.bodyMedium, color = c.textSecondary)
            Spacer(Modifier.height(HqSpacing.lg))
            habitiq.app.ui.components.HqSettingRow(
                title = "Biometric lock",
                support = "Use your fingerprint or face to open Oddroof",
            ) {
                habitiq.app.ui.components.HqSwitch(
                    checked = biometricEnabled,
                    onCheckedChange = { enabled -> scope.launch { prefs.setBiometricLockEnabled(enabled) } },
                    contentDescription = "Biometric lock",
                )
            }

            Spacer(Modifier.height(HqSpacing.xxl))
            habitiq.app.ui.components.HqMenuGroup("Legal & safety") {
                habitiq.app.ui.components.HqMenuRow(
                    title = "Privacy Policy", support = "What we collect and your rights",
                    onClick = { openLegalLink(context, "https://habitiq.app/privacy") },
                )
                habitiq.app.ui.components.HqMenuRow(
                    title = "Terms of Service", support = "Rules for using Oddroof and Discover",
                    onClick = { openLegalLink(context, "https://habitiq.app/terms") },
                )
                habitiq.app.ui.components.HqMenuRow(
                    title = "Safety tips", support = "Avoid scams and stay safe at viewings",
                    onClick = { openLegalLink(context, "https://habitiq.app/safety") },
                )
                habitiq.app.ui.components.HqMenuRow(
                    title = "Contact & grievances", support = "hello@habitiq.app", lastRow = true,
                    onClick = { openLegalLink(context, "mailto:hello@habitiq.app") },
                )
            }

            Spacer(Modifier.height(HqSpacing.xl))
            habitiq.app.ui.components.HqMenuGroup("Danger zone") {
                habitiq.app.ui.components.HqMenuRow(
                    title = "Delete account", support = "Permanently delete your data", danger = true, lastRow = true,
                    onClick = { if (!deleting) showConfirmDialog = true },
                )
            }
            Spacer(Modifier.height(HqSpacing.xl))
            habitiq.app.ui.components.HqDangerSoftButton("Sign out", onSignOut)
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
            message = "This permanently deletes your account and removes you from every flat you're in. Your tasks pass to the next person in each rotation. It can't be undone.",
            confirmLabel = "Delete account",
            onConfirm = { showConfirmDialog = false; viewModel.deleteAccount() },
            onDismiss = { showConfirmDialog = false },
        )
    }
}

private fun openLegalLink(context: android.content.Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}
