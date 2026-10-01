package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import habitiq.app.data.FlatActivity
import habitiq.app.flat.FlatViewModel
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

@Composable
fun ActivityLogScreen(viewModel: FlatViewModel, onBack: () -> Unit) {
    val activity by viewModel.activity.collectAsStateWithLifecycleCompat()
    val c = LocalHqColors.current

    Column(Modifier.fillMaxSize().background(c.background)) {
        HqBackAppBar(title = "Activity", onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(HqSpacing.lg), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            items(activity, key = { it.id }) { entry -> ActivityRow(entry) }
        }
    }
}

@Composable
private fun ActivityRow(entry: FlatActivity) {
    val c = LocalHqColors.current
    HqCard(padding = HqSpacing.md) {
        Text(entry.details, style = HqType.bodyMedium, color = c.textPrimary)
        Text("${entry.action} · ${entry.timestamp.take(19).replace('T', ' ')}", style = HqType.caption, color = c.textTertiary)
    }
}
