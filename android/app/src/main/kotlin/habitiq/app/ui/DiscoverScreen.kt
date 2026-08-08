package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.ui.theme.FigmaColors

@Composable fun DiscoverScreen() = StubScreen(Icons.Filled.Explore, "Discover", "Find flats, roommates, and popular areas near you. Coming soon in native.")
@Composable fun TasksManageScreen() = StubScreen(Icons.Filled.GridView, "Manage", "Tasks, rotation, expenses, and bills will live here. Ported from web lib/ next.")

@Composable
private fun StubScreen(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: String) {
    Column(Modifier.fillMaxSize().background(FigmaColors.Background).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(icon, null, tint = FigmaColors.Primary, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(16.dp))
        Text(title, color = FigmaColors.Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(body, color = FigmaColors.InkSecondary, fontSize = 15.sp, textAlign = TextAlign.Center)
    }
}
