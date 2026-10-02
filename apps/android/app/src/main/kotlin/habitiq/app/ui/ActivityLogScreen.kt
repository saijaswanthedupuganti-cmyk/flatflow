package habitiq.app.ui

import habitiq.app.lib.formatTimeAgo
import kotlinx.coroutines.launch
import habitiq.app.ui.components.HqTextButton
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import habitiq.app.lib.formatActivityTime
import habitiq.app.lib.activityActionLabel
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
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    val c = LocalHqColors.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = androidx.compose.runtime.remember { habitiq.app.settings.AppPreferences(context) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val uid = currentUser?.uid.orEmpty()
    val seen by prefs.activitySeen(uid).collectAsState(initial = habitiq.app.settings.ActivitySeen(0L, emptySet()))
    val unread = habitiq.app.lib.unreadCount(activity, uid, seen)
    val today = activity.filter { habitiq.app.lib.isToday(it) }
    val earlier = activity.filterNot { habitiq.app.lib.isToday(it) }

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        habitiq.app.ui.components.HqPageHeader(
            title = "Notifications",
            subtitle = if (unread == 0) "You're all caught up" else "$unread ${if (unread == 1) "update needs" else "updates need"} your attention",
            onBack = onBack,
            modifier = Modifier.padding(horizontal = HqSpacing.screenHorizontal).padding(top = HqSpacing.sm),
            action = {
                HqTextButton(
                    text = if (unread == 0) "All read" else "Mark all read",
                    enabled = unread > 0,
                    onClick = { scope.launch { prefs.markAllActivityRead(uid, System.currentTimeMillis()) } },
                )
            },
        )
        LazyColumn(
            contentPadding = PaddingValues(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.sm),
        ) {
            if (today.isNotEmpty()) {
                item { habitiq.app.ui.components.HqListHeading("Today", right = if (unread > 0) "$unread new" else null) }
                items(today, key = { it.id }) { entry ->
                    ActivityRow(entry, unread = habitiq.app.lib.isUnread(entry, uid, seen)) { scope.launch { prefs.markActivityRead(uid, entry.id) } }
                }
            }
            if (earlier.isNotEmpty()) {
                item { habitiq.app.ui.components.HqListHeading("Earlier") }
                items(earlier, key = { it.id }) { entry ->
                    ActivityRow(entry, unread = habitiq.app.lib.isUnread(entry, uid, seen)) { scope.launch { prefs.markActivityRead(uid, entry.id) } }
                }
            }
            item {
                androidx.compose.foundation.layout.Row(
                    Modifier.padding(top = 18.dp).fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp)).background(c.surfaceSubtle).padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    androidx.compose.material3.Icon(habitiq.app.ui.components.HqIcons.Bell, null, tint = c.textSecondary, modifier = Modifier.size(18.dp))
                    Text("Notifications help you stay updated without turning Home into a management feed.", style = HqType.bodyMedium, color = c.textSecondary)
                }
            }
        }
    }
}

/** Figma notification row: tinted icon tile by kind, bold title, support line, small time, unread dot. */
@Composable
private fun ActivityRow(entry: FlatActivity, unread: Boolean, onClick: () -> Unit) {
    val c = LocalHqColors.current
    val kind = activityActionLabel(entry.action).lowercase()
    val (icon, tone) = when {
        "task" in kind || "complete" in kind -> habitiq.app.ui.components.HqIcons.Check to habitiq.app.ui.components.HqTileTone.Teal
        "expense" in kind || "bill" in kind || "settle" in kind -> habitiq.app.ui.components.HqIcons.Receipt to habitiq.app.ui.components.HqTileTone.Sand
        "swap" in kind || "member" in kind || "join" in kind -> habitiq.app.ui.components.HqIcons.Users to habitiq.app.ui.components.HqTileTone.Coral
        else -> habitiq.app.ui.components.HqIcons.Bell to habitiq.app.ui.components.HqTileTone.Neutral
    }
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
    androidx.compose.foundation.layout.Row(
        Modifier.fillMaxWidth().clip(shape)
            .background(if (unread) c.selectedBg.copy(alpha = .45f) else c.surfaceBase)
            .border(1.dp, if (unread) c.selectedBorder.copy(alpha = .35f) else c.borderSubtle, shape)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.Top,
    ) {
        habitiq.app.ui.components.HqIconTile(icon, tone, size = 40)
        androidx.compose.foundation.layout.Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(entry.details, style = HqType.rowTitle, color = c.textPrimary)
            Text("${activityActionLabel(entry.action)} \u00b7 ${formatTimeAgo(entry.timestamp)}", style = HqType.labelSmall, color = c.textMuted)
        }
        if (unread) {
            androidx.compose.foundation.layout.Box(
                Modifier.padding(top = 6.dp).size(9.dp).clip(androidx.compose.foundation.shape.CircleShape).background(c.brandTeal)
                    .semantics { contentDescription = "Unread" },
            )
        }
    }
}
