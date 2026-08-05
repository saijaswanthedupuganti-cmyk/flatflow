package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import habitiq.app.R
import habitiq.app.flats.FlatUiState
import habitiq.app.flats.JoinFlatViewModel

val JoinFlatAccent = Color(0xFF7B5CFA)

@Composable
fun JoinFlatScreen(
    viewModel: JoinFlatViewModel,
    onJoined: (flatId: String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val joinedFlatId by viewModel.joinedFlatId.collectAsStateWithLifecycleCompat()

    LaunchedEffect(state, joinedFlatId) {
        val flatId = joinedFlatId
        if (state is FlatUiState.Success && flatId != null) {
            onJoined(flatId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FlatOnboardingBackground)
            .verticalScroll(rememberScrollState())
    ) {
        FlatOnboardingHeader(
            accentColor = JoinFlatAccent,
            imageRes = R.drawable.onboard_join,
            titleLine1 = "Join Your",
            titleLine2 = "Crew.",
            subtitle = "Have an invite code? Walk right in. Expenses, chores, bills — already set up and waiting for you.",
            benefits = listOf(
                "Step in instantly — no setup",
                "See balances and shared expenses",
                "Stay synced in real-time"
            )
        )

        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Invite code") },
                placeholder = { Text("FLAT-A3B9") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = JoinFlatAccent,
                    unfocusedBorderColor = Color(0xFF3A3A3A),
                    focusedLabelColor = JoinFlatAccent,
                    unfocusedLabelColor = Color(0xFF9A9A9A)
                )
            )
            Spacer(Modifier.height(16.dp))
            FlatRoleCallout(
                accentColor = JoinFlatAccent,
                text = "You'll join as a member — all expenses and chores are already set up."
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { viewModel.joinFlat(code) },
                enabled = state !is FlatUiState.Loading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = JoinFlatAccent)
            ) {
                Text("Join Flat")
            }
            when (val current = state) {
                is FlatUiState.Loading -> Text(
                    "Joining…",
                    color = Color.White,
                    modifier = Modifier.padding(top = 12.dp)
                )
                is FlatUiState.Error -> Text(
                    current.message,
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier.padding(top = 12.dp)
                )
                else -> {}
            }
        }
    }
}
