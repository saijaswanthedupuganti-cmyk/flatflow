package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser
import habitiq.app.ui.theme.FigmaColors

@Composable
fun ProfileScreen(user: FirebaseUser?, onOpenSettings: () -> Unit, onSignOut: () -> Unit) {
    Column(Modifier.fillMaxSize().background(FigmaColors.Background).padding(24.dp)) {
        Text("Profile", color = FigmaColors.Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(user?.email ?: "Not signed in", color = FigmaColors.InkSecondary, fontSize = 15.sp)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = FigmaColors.Primary)) {
            Text("Account Settings")
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = FigmaColors.Surface, contentColor = FigmaColors.Ink)) {
            Text("Sign out")
        }
    }
}
