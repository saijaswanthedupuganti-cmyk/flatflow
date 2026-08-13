package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.data.FlatActivity
import habitiq.app.flat.FlatViewModel
import habitiq.app.ui.theme.FigmaColors

@Composable
fun ActivityLogScreen(viewModel: FlatViewModel, onBack: () -> Unit) {
    val activity by viewModel.activity.collectAsStateWithLifecycleCompat()

    Column(Modifier.fillMaxSize().background(FigmaColors.Background)) {
        Row(Modifier.padding(16.dp)) {
            TextButton(onClick = onBack) { Text("← Back") }
            Text("Activity", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp))
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(activity, key = { it.id }) { entry -> ActivityRow(entry) }
        }
    }
}

@Composable
private fun ActivityRow(entry: FlatActivity) {
    Card(colors = CardDefaults.cardColors(containerColor = FigmaColors.Surface)) {
        Column(Modifier.padding(12.dp)) {
            Text(entry.details, fontSize = 14.sp, color = FigmaColors.Ink)
            Text("${entry.action} · ${entry.timestamp.take(19).replace('T', ' ')}", fontSize = 11.sp, color = FigmaColors.InkMuted)
        }
    }
}
