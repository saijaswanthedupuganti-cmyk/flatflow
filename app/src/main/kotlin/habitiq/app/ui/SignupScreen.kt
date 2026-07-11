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
import habitiq.app.auth.SignupViewModel

@Composable
fun SignupScreen(
    viewModel: SignupViewModel,
    onSignedUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycleCompat()

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text("Create your Habitiq account")
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password (min 6 characters)") })
        Button(onClick = { viewModel.signUpWithEmail(email, password) }) {
            Text("Sign up")
        }
        when (val current = state) {
            is AuthUiState.Loading -> Text("Creating account…")
            is AuthUiState.Error -> Text(current.message)
            is AuthUiState.Success -> onSignedUp()
            else -> {}
        }
    }
}
