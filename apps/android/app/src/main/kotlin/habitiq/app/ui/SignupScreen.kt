package habitiq.app.ui

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.autofill.ContentType
import habitiq.app.ui.components.HqWordmark
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.credentials.exceptions.GetCredentialException
import habitiq.app.R
import habitiq.app.auth.AuthUiState
import habitiq.app.auth.SignupViewModel
import habitiq.app.auth.launchGoogleSignIn
import habitiq.app.auth.mapAuthError
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqInlineError
import habitiq.app.ui.components.HqInlineLoading
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import kotlinx.coroutines.launch

@Composable
fun SignupScreen(
    viewModel: SignupViewModel,
    onSignedUp: (hasActiveFlat: Boolean) -> Unit,
    onNavigateToLogin: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val webClientId = stringResource(R.string.google_web_client_id)

    LaunchedEffect(state) {
        val success = state
        if (success is AuthUiState.Success) {
            onSignedUp(success.hasActiveFlat)
        }
    }

    val c = LocalHqColors.current
    val hasMinLength = password.length >= 8
    val hasLetterAndNumber = password.any(Char::isLetter) && password.any(Char::isDigit)
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    Column(
            modifier = Modifier
                .fillMaxSize()
                .background(c.canvas)
                .verticalScroll(rememberScrollState())
                .padding(HqSpacing.xxl)
        ) {
            Spacer(Modifier.height(HqSpacing.xl))
            HqWordmark()
            Spacer(Modifier.height(HqSpacing.lg))
            Text("Create your account", style = HqType.headlineLarge, color = c.textPrimary)
            Text(
                "Join people finding a better way to live together.",
                style = HqType.bodyMedium,
                color = c.textSecondary,
                modifier = Modifier.padding(top = HqSpacing.xs, bottom = HqSpacing.xxxl)
            )

            HqButton(
                text = "Continue with Google",
                onClick = {
                    coroutineScope.launch {
                        try {
                            val idToken = launchGoogleSignIn(context, webClientId)
                            if (idToken != null) {
                                viewModel.signInWithGoogleIdToken(idToken)
                            }
                        } catch (e: GetCredentialException) {
                            viewModel.onGoogleSignInFailed(mapAuthError(e))
                        } catch (e: Exception) {
                            if (e is kotlinx.coroutines.CancellationException) throw e
                            viewModel.onGoogleSignInFailed(mapAuthError(e))
                        }
                    }
                },
                variant = HqButtonVariant.Secondary,
                enabled = state !is AuthUiState.Loading,
            )
            Spacer(Modifier.height(HqSpacing.lg))
            Text("or use email", style = HqType.bodySmall, color = c.textSecondary, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(HqSpacing.lg))

            HqTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email address",
                leadingIcon = Icons.Filled.Email,
                keyboardType = KeyboardType.Email,
                contentType = ContentType.EmailAddress,
            )
            Spacer(Modifier.height(HqSpacing.md))
            HqTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                isPassword = true,
                leadingIcon = Icons.Filled.Lock,
                contentType = ContentType.NewPassword,
                imeAction = ImeAction.Done,
                onImeAction = if (state !is AuthUiState.Loading && email.isNotBlank() && hasMinLength && hasLetterAndNumber && hasSpecial) {
                    { viewModel.signUpWithEmail(email, password) }
                } else null,
            )
            Spacer(Modifier.height(HqSpacing.md))
            PasswordRequirement("At least 8 characters", hasMinLength)
            PasswordRequirement("One letter and one number", hasLetterAndNumber)
            PasswordRequirement("One special character", hasSpecial)
            Spacer(Modifier.height(HqSpacing.xl))

            HqButton(
                text = "Sign up",
                onClick = { viewModel.signUpWithEmail(email, password) },
                enabled = state !is AuthUiState.Loading && email.isNotBlank() && hasMinLength && hasLetterAndNumber && hasSpecial,
            )
            Spacer(Modifier.height(HqSpacing.md))
            HqTextButton(
                text = "Already have an account? Log in",
                onClick = onNavigateToLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(HqSpacing.lg))
            when (val current = state) {
                is AuthUiState.Loading -> HqInlineLoading(label = "Creating account…")
                is AuthUiState.Error -> HqInlineError(current.message)
                else -> {}
            }
    }
}

@Composable
private fun PasswordRequirement(label: String, met: Boolean) {
    val c = LocalHqColors.current
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = HqSpacing.xs)
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            null,
            tint = if (met) c.statusSuccessFg else c.textMuted,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.size(HqSpacing.sm))
        Text(label, style = HqType.bodySmall, color = if (met) c.statusSuccessFg else c.textSecondary)
    }
}
