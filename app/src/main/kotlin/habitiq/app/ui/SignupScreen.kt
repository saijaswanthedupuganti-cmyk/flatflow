package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.auth.AuthUiState
import habitiq.app.auth.SignupViewModel
import habitiq.app.ui.theme.HabitiqBrand

@Composable
fun SignupScreen(
    viewModel: SignupViewModel,
    onSignedUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycleCompat()

    LaunchedEffect(state) {
        if (state is AuthUiState.Success) {
            onSignedUp()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HabitiqBrand.Canvas)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Habitiq", color = HabitiqBrand.Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            "Split expenses. Manage chores. Run your shared home.",
            color = HabitiqBrand.InkMute,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        Text("Create your account", color = HabitiqBrand.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = HabitiqBrand.InputBackground,
                unfocusedContainerColor = HabitiqBrand.InputBackground,
                focusedTextColor = HabitiqBrand.Ink,
                unfocusedTextColor = HabitiqBrand.Ink,
                focusedBorderColor = HabitiqBrand.Primary,
                unfocusedBorderColor = HabitiqBrand.InputBorder,
                focusedLabelColor = HabitiqBrand.Primary,
                unfocusedLabelColor = HabitiqBrand.InkMute,
                cursorColor = HabitiqBrand.Primary
            )
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (min 6 characters)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = HabitiqBrand.InputBackground,
                unfocusedContainerColor = HabitiqBrand.InputBackground,
                focusedTextColor = HabitiqBrand.Ink,
                unfocusedTextColor = HabitiqBrand.Ink,
                focusedBorderColor = HabitiqBrand.Primary,
                unfocusedBorderColor = HabitiqBrand.InputBorder,
                focusedLabelColor = HabitiqBrand.Primary,
                unfocusedLabelColor = HabitiqBrand.InkMute,
                cursorColor = HabitiqBrand.Primary
            )
        )
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { viewModel.signUpWithEmail(email, password) },
            enabled = state !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HabitiqBrand.Primary,
                contentColor = HabitiqBrand.OnPrimary
            )
        ) {
            Text("Sign up")
        }

        Spacer(Modifier.height(16.dp))
        when (val current = state) {
            is AuthUiState.Loading -> Text("Creating account…", color = HabitiqBrand.InkMute)
            is AuthUiState.Error -> Text(current.message, color = HabitiqBrand.Error)
            else -> {}
        }
    }
}
