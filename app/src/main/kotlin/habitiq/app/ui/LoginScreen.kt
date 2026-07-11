package habitiq.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import habitiq.app.auth.AuthUiState
import habitiq.app.auth.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onSignedIn: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycleCompat()

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
        when (val current = state) {
            is AuthUiState.Loading -> Text("Signing in…")
            is AuthUiState.Error -> Text(current.message)
            is AuthUiState.Success -> onSignedIn()
            else -> {}
        }
    }
}
