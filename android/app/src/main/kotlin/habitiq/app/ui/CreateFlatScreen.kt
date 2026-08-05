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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import habitiq.app.R
import habitiq.app.flats.CreateFlatViewModel
import habitiq.app.flats.FlatUiState
import habitiq.app.flats.launchShareInviteCode

val CreateFlatAccent = Color(0xFFF97316)

@Composable
fun CreateFlatScreen(
    viewModel: CreateFlatViewModel,
    onDone: () -> Unit
) {
    var flatName by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val createdFlatId by viewModel.createdFlatId.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FlatOnboardingBackground)
            .verticalScroll(rememberScrollState())
    ) {
        FlatOnboardingHeader(
            accentColor = CreateFlatAccent,
            imageRes = R.drawable.onboard_create,
            titleLine1 = "Your Flat.",
            titleLine2 = "Your Rules.",
            subtitle = "Become the admin. Invite your roommates, set up chore rotation and bills — your shared home, on autopilot.",
            benefits = listOf(
                "Invite roommates with a code",
                "Auto-rotate chores fairly",
                "Split and track every expense"
            )
        )

        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            if (createdFlatId == null) {
                OutlinedTextField(
                    value = flatName,
                    onValueChange = { flatName = it },
                    label = { Text("Flat name") },
                    placeholder = { Text("e.g., The Boys Apartment") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CreateFlatAccent,
                        unfocusedBorderColor = Color(0xFF3A3A3A),
                        focusedLabelColor = CreateFlatAccent,
                        unfocusedLabelColor = Color(0xFF9A9A9A)
                    )
                )
                Spacer(Modifier.height(16.dp))
                FlatRoleCallout(
                    accentColor = CreateFlatAccent,
                    text = "You'll be the admin and can manage everything in your flat."
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { viewModel.createFlat(flatName) },
                    enabled = state !is FlatUiState.Loading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CreateFlatAccent)
                ) {
                    Text("Create Flat")
                }
                when (val current = state) {
                    is FlatUiState.Loading -> Text(
                        "Creating…",
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
            } else {
                Text("Your flat is ready!", color = Color.White)
                Text("Invite code: $createdFlatId", color = CreateFlatAccent)
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { launchShareInviteCode(context, flatName, createdFlatId!!) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CreateFlatAccent)
                ) {
                    Text("Share invite code")
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}
