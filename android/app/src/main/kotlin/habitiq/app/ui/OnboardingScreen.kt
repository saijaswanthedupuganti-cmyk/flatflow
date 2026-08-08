package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.ui.theme.FigmaColors

@Composable
fun OnboardingScreen(
    userName: String,
    onCreateFlat: () -> Unit,
    onJoinFlat: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(FigmaColors.Background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome, $userName", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = FigmaColors.Ink)
        Spacer(Modifier.height(8.dp))
        Text(
            "Create a flat or join roommates with an invite code. Your chores and expenses sync with the web app on garbage-f79f7.",
            textAlign = TextAlign.Center,
            color = FigmaColors.InkSecondary,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onCreateFlat,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = FigmaColors.Primary)
        ) { Text("Create a flat") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onJoinFlat, modifier = Modifier.fillMaxWidth()) { Text("Join with invite code") }
    }
}
