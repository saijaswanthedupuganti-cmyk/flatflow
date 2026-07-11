package habitiq.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.exceptions.GetCredentialException
import habitiq.app.R
import habitiq.app.auth.AuthUiState
import habitiq.app.auth.LoginViewModel
import habitiq.app.auth.mapAuthError
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onSignedIn: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycleCompat()

    // Runs once per distinct `state` value (not on every recomposition), so a successful
    // sign-in triggers exactly one navigation call instead of one per recomposition.
    LaunchedEffect(state) {
        if (state is AuthUiState.Success) {
            onSignedIn()
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text("Log in to Habitiq")
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        Button(onClick = { viewModel.signInWithEmail(email, password) }) {
            Text("Log in")
        }
        Button(onClick = onNavigateToSignup) {
            Text("Need an account? Sign up")
        }
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val webClientId = androidx.compose.ui.res.stringResource(R.string.google_web_client_id)
        Button(onClick = {
            coroutineScope.launch {
                try {
                    val idToken = habitiq.app.auth.launchGoogleSignIn(context, webClientId)
                    if (idToken != null) {
                        viewModel.signInWithGoogleIdToken(idToken)
                    }
                } catch (e: GetCredentialException) {
                    // launchGoogleSignIn already swallows a user-cancelled picker (returns null).
                    // Anything that reaches here is a real failure (no Google account on the
                    // device, outdated Play Services, etc.) that would otherwise crash the app.
                    viewModel.onGoogleSignInFailed(mapAuthError(e))
                }
            }
        }) {
            Text("Sign in with Google")
        }
        when (val current = state) {
            is AuthUiState.Loading -> Text("Signing in…")
            is AuthUiState.Error -> Text(current.message)
            else -> {}
        }
    }
}
