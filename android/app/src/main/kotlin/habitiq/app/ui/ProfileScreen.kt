package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser
import habitiq.app.flat.FlatViewModel
import habitiq.app.ui.theme.FigmaColors

@Composable
fun ProfileScreen(
    user: FirebaseUser?,
    flatViewModel: FlatViewModel,
    onOpenSettings: () -> Unit,
    onSignOut: () -> Unit
) {
    val profile by flatViewModel.userProfile.collectAsStateWithLifecycleCompat()
    val flatInfo by flatViewModel.flatInfo.collectAsStateWithLifecycleCompat()
    var displayName by remember(profile?.displayName) { mutableStateOf(profile?.displayName ?: user?.displayName.orEmpty()) }
    var saved by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(FigmaColors.Background).padding(24.dp)) {
        Text("Profile", color = FigmaColors.Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(user?.email ?: "Not signed in", color = FigmaColors.InkSecondary, fontSize = 15.sp)
        flatInfo?.let {
            Text("Flat: ${it.name}", color = FigmaColors.InkSecondary, fontSize = 14.sp)
        }
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it; saved = false },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Display name") },
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                flatViewModel.updateDisplayName(displayName)
                saved = true
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FigmaColors.Primary)
        ) { Text("Save profile") }
        if (saved) {
            Text("Saved to Firestore", color = FigmaColors.Primary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = FigmaColors.Surface, contentColor = FigmaColors.Ink)) {
            Text("Account Settings")
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = FigmaColors.Surface, contentColor = FigmaColors.Ink)) {
            Text("Sign out")
        }
    }
}
