package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.data.FlatTask
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.ui.collectAsStateWithLifecycleCompat
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun GoingAwayScreen(viewModel: FlatViewModel, onBack: () -> Unit, onSent: () -> Unit) {
    val c = LocalHqColors.current
    val tasks by viewModel.tasks.collectAsStateWithLifecycleCompat()
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    val uid = currentUser?.uid.orEmpty()

    val myTasks = remember(tasks, uid) {
        tasks.filter { it.currentAssignedUserId == uid && (it.status == "pending" || it.status == "overdue") }
    }
    val availableMembers = remember(members, uid) {
        members.filter { it.uid != uid && it.status != "out_of_station" && it.status != "inactive" }
    }
    var selections by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val allAssigned = myTasks.isNotEmpty() && myTasks.all { selections.containsKey(it.taskId) }

    FigmaScreenBackground {
        HqBackAppBar(title = "Going Away", onBack = onBack)
        Text(
            "Assign your tasks to a flatmate before you leave. Accountability remains with you until they accept.",
            color = c.textSecondary,
            style = HqType.bodyLarge,
            modifier = Modifier.padding(horizontal = HqSpacing.xl).padding(bottom = HqSpacing.sm)
        )
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.xxxl)
        ) {
            StatusCard(pendingCount = myTasks.size)
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                Text("YOUR TASK LIST", color = c.textSecondary, style = HqType.labelMedium, letterSpacing = 0.26.sp)
                if (myTasks.isEmpty()) {
                    Text("No pending tasks assigned to you.", color = c.textSecondary, style = HqType.bodyLarge)
                } else {
                    myTasks.forEach { task ->
                        GoingAwayTaskCard(
                            task = task,
                            members = availableMembers,
                            selectedUid = selections[task.taskId],
                            onSelect = { toUid -> selections = selections + (task.taskId to toUid) }
                        )
                    }
                }
                InfoBanner()
            }
            Spacer(Modifier.height(HqSpacing.xxhuge * 2))
        }
        Column(
            Modifier.fillMaxWidth()
                .background(c.background.copy(alpha = 0.92f))
                .navigationBarsPadding()
                .padding(HqSpacing.xl)
        ) {
            HqButton(
                text = "Send Requests",
                enabled = allAssigned && availableMembers.isNotEmpty(),
                onClick = {
                    viewModel.sendGoingAwayRequests(selections)
                    onSent()
                }
            )
            HqTextButton(text = "Cancel & Return", onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun StatusCard(pendingCount: Int) {
    val c = LocalHqColors.current
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(HqRadius.md))
            .background(c.brandPrimary)
            .padding(HqSpacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("STATUS", color = c.onBrandPrimary.copy(alpha = 0.8f), style = HqType.labelSmall, letterSpacing = 0.55.sp)
            Text("$pendingCount Pending Tasks", color = c.onBrandPrimary, style = HqType.headlineSmall)
        }
        Box(Modifier.size(48.dp).clip(CircleShape).background(c.brandPrimaryHover), contentAlignment = Alignment.Center) {
            Text("✈", style = HqType.headlineSmall)
        }
    }
}

@Composable
private fun GoingAwayTaskCard(
    task: FlatTask,
    members: List<Member>,
    selectedUid: String?,
    onSelect: (String) -> Unit
) {
    val c = LocalHqColors.current
    val isOverdue = task.status == "overdue"
    HqCard(variant = HqCardVariant.Standard, padding = HqSpacing.lg) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                StatusPill(
                    text = if (isOverdue) "Overdue" else dayLabel(task.dueDate),
                    bg = if (isOverdue) c.errorContainer else c.surfaceSubtle,
                    fg = if (isOverdue) c.error else c.textSecondary
                )
                Spacer(Modifier.height(HqSpacing.sm))
                Text(task.name, color = c.textPrimary, style = HqType.titleMedium)
                Text(
                    if (isOverdue) "Was due on ${dayName(task.dueDate)}" else frequencyLabel(task.frequency),
                    color = c.textSecondary,
                    style = HqType.labelSmall
                )
            }
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(HqRadius.sm)).background(c.surfaceSubtle), contentAlignment = Alignment.Center) {
                Text(taskEmoji(task.name), style = HqType.titleMedium)
            }
        }
        Column(Modifier.fillMaxWidth().padding(top = 1.dp)) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.borderDefault))
            Spacer(Modifier.height(HqSpacing.lg))
            Text("Reassign to:", color = c.textSecondary, style = HqType.labelMedium, letterSpacing = 0.26.sp)
            Spacer(Modifier.height(HqSpacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.lg)) {
                members.forEach { member ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSelect(member.uid) }) {
                        // HqAvatar has no built-in selected/checkmark affordance, so this keeps
                        // the shared MemberInitialAvatar primitive (out of this batch's scope).
                        MemberInitialAvatar(name = member.nickname, selected = selectedUid == member.uid)
                        Spacer(Modifier.height(HqSpacing.xs))
                        Text(
                            member.nickname.split(" ").firstOrNull() ?: member.nickname,
                            color = if (selectedUid == member.uid) c.brandPrimary else c.textSecondary,
                            style = HqType.labelSmall,
                            fontWeight = if (selectedUid == member.uid) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBanner() {
    val c = LocalHqColors.current
    // Distinct muted-gray banner background (not a plain card surface) -- HqCard's variants
    // don't expose this tone, so this stays a token-driven custom container instead of HqCard.
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(HqRadius.md))
            .background(c.surfaceDisabled)
            .border(1.dp, c.borderDefault, RoundedCornerShape(HqRadius.md))
            .padding(HqSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)
    ) {
        Icon(Icons.Filled.Info, null, tint = c.borderStrong, modifier = Modifier.size(HqIconSize.sm))
        Text(
            "Requests will be sent to your flatmates. You'll be marked 'Away' only after all tasks are accepted.",
            color = c.borderStrong,
            style = HqType.labelMedium,
            letterSpacing = 0.26.sp
        )
    }
}

@Composable
private fun StatusPill(text: String, bg: Color, fg: Color) {
    Box(Modifier.clip(RoundedCornerShape(HqRadius.full)).background(bg).padding(horizontal = HqSpacing.sm, vertical = 2.dp)) {
        Text(text, color = fg, style = HqType.labelSmall)
    }
}

private fun dayLabel(dueDate: String): String = runCatching {
    val date = LocalDate.parse(dueDate.take(10))
    date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
}.getOrDefault("Upcoming")

private fun dayName(dueDate: String): String = runCatching {
    val date = LocalDate.parse(dueDate.take(10))
    date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
}.getOrDefault("soon")

private fun frequencyLabel(freq: String) = when (freq) {
    "daily" -> "Daily recurring task"
    "monthly" -> "Monthly recurring task"
    "one_time" -> "One-time task"
    else -> "Weekly recurring task"
}

private fun taskEmoji(name: String) = when {
    name.contains("kitchen", true) || name.contains("clean", true) -> "🍳"
    name.contains("trash", true) || name.contains("garbage", true) -> "🗑️"
    else -> "📋"
}
