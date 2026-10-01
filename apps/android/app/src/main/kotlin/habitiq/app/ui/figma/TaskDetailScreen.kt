package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.data.FlatTask
import habitiq.app.flats.Member
import habitiq.app.lib.RotationEngine
import habitiq.app.lib.effectiveTaskStatus
import habitiq.app.lib.formatExpectedDue
import habitiq.app.lib.parseTaskInstant
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class QueueEntry(
    val member: Member,
    val position: Int,
    val state: QueueState,
    val subtitle: String,
    val reliability: Int? = null
)

enum class QueueState { NOW, NEXT, SKIPPED, QUEUED }

@Composable
fun TaskDetailScreen(
    task: FlatTask,
    members: List<Member>,
    currentUid: String,
    onBack: () -> Unit,
    onRequestSwap: (String) -> Unit = {}
) {
    val c = LocalHqColors.current
    val queue = remember(task, members) { buildQueue(task, members) }
    val overdueDays = remember(task) { computeOverdueDays(task) }

    FigmaScreenBackground {
        HqBackAppBar(title = task.name, onBack = onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.xxxl)
        ) {
            TaskSummaryCard(task, overdueDays)
            Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                HqChip(label = task.type.replace('_', ' ').replaceFirstChar { it.uppercase() }, selected = true)
                HqChip(label = task.frequency.replaceFirstChar { it.uppercase() }, selected = false)
                HqChip(label = task.priority.replaceFirstChar { it.uppercase() }, selected = false)
            }
            val next = queue.firstOrNull { it.state == QueueState.NEXT } ?: queue.firstOrNull { it.state == QueueState.NOW }
            if (next != null) {
                HqCard {
                    Text("Next assignee", style = HqType.labelMedium, color = c.textSecondary)
                    Text(next.member.nickname, style = HqType.titleMedium, color = c.textPrimary)
                    Text(next.subtitle, style = HqType.bodySmall, color = c.textSecondary)
                }
            }
            if (queue.isNotEmpty()) {
                Text("Rotation", style = HqType.titleSmall, color = c.textPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    queue.take(6).forEach { entry ->
                        val label = entry.member.nickname.substringBefore(" ").take(1)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(40.dp).clip(CircleShape).background(if (entry.state == QueueState.NOW) c.brandPrimary else c.surfaceSubtle), contentAlignment = Alignment.Center) {
                                Text(label, color = if (entry.state == QueueState.NOW) c.textOnBrand else c.textPrimary, style = HqType.labelLarge)
                            }
                            Text(entry.member.nickname.substringBefore(" "), style = HqType.caption, color = c.textSecondary)
                        }
                    }
                }
            }
            HqCard {
                Text("History", style = HqType.titleSmall, color = c.textPrimary)
                Text(
                    if (task.lastCompletedAt.isBlank()) "No completions yet" else "Last completed ${task.lastCompletedAt}",
                    style = HqType.bodySmall,
                    color = c.textSecondary
                )
            }
            HqCard {
                Text("Reminders", style = HqType.titleSmall, color = c.textPrimary)
                Text("Every ${task.frequency}", style = HqType.bodySmall, color = c.textSecondary)
            }
            HqCard {
                Text("Notes", style = HqType.titleSmall, color = c.textPrimary)
                Text(task.notes.ifBlank { "No notes yet" }, style = HqType.bodySmall, color = c.textSecondary)
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = HqSpacing.xs), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ROTATION QUEUE", color = c.textSecondary, style = HqType.labelMedium, letterSpacing = 1.3.sp)
                Text("${queue.size} Members", color = c.brandPrimary, style = HqType.labelSmall)
            }
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                queue.forEachIndexed { index, entry ->
                    QueueMemberCard(entry, showConnector = index < queue.lastIndex)
                }
            }
            if (task.currentAssignedUserId == currentUid && task.status != "completed") {
                Text("Request swap", style = HqType.titleSmall, color = c.textPrimary)
                members.filter { it.uid != currentUid && it.status != "out_of_station" }.forEach { m ->
                    HqButton(text = "Ask ${m.nickname} to cover", onClick = { onRequestSwap(m.uid) }, variant = HqButtonVariant.Secondary)
                }
            }
            Spacer(Modifier.height(HqSpacing.xxl))
        }
    }
}

@Composable
private fun TaskSummaryCard(task: FlatTask, overdueDays: Int?) {
    val c = LocalHqColors.current
    HqCard(variant = HqCardVariant.Standard, padding = HqSpacing.xxl) {
        Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.lg)) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(HqRadius.lg)).background(c.brandPrimaryContainer), contentAlignment = Alignment.Center) {
                Text("🧹", style = HqType.titleLarge)
            }
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                if (overdueDays != null && overdueDays > 0) {
                    Box(Modifier.clip(RoundedCornerShape(HqRadius.full)).background(c.errorContainer).padding(horizontal = HqSpacing.sm, vertical = 2.dp)) {
                        Text("${overdueDays}d overdue", color = c.error, style = HqType.labelSmall)
                    }
                }
                Text(task.name, color = c.textPrimary, style = HqType.headlineSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.lg), verticalAlignment = Alignment.CenterVertically) {
                    Text("📅 ${task.frequency.replaceFirstChar { it.uppercase() }}", color = c.textSecondary, style = HqType.labelMedium, letterSpacing = 0.26.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(c.warning))
                        Text("${task.priority.replaceFirstChar { it.uppercase() }} Priority", color = c.textSecondary, style = HqType.labelMedium, letterSpacing = 0.26.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueMemberCard(entry: QueueEntry, showConnector: Boolean) {
    val c = LocalHqColors.current
    val isNow = entry.state == QueueState.NOW
    val isSkipped = entry.state == QueueState.SKIPPED
    val (bgColor, borderColor, textColor) = avatarPalette(entry.position - 1)
    val cardBg = if (isSkipped) c.surfaceSubtle else c.surface
    val cardBorder = when (entry.state) {
        QueueState.NOW -> 2.dp to c.brandPrimary
        else -> 1.dp to c.borderDefault
    }
    Box {
        if (showConnector) {
            Box(
                Modifier.align(Alignment.TopStart).offset(x = 24.dp, y = 48.dp)
                    .width(2.dp).height(80.dp).background(c.borderDefault)
            )
        }
        Row(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(HqRadius.xl))
                .background(cardBg)
                .border(cardBorder.first, cardBorder.second, RoundedCornerShape(HqRadius.xl))
                .padding(horizontal = 17.dp, vertical = if (isNow) 18.dp else 17.dp),
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(bgColor).border(2.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(memberInitials(entry.member.nickname), color = textColor, style = HqType.titleMedium)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            entry.member.nickname,
                            color = c.textPrimary.copy(alpha = if (isSkipped) 0.6f else 1f),
                            style = if (isNow) HqType.titleMedium else HqType.bodyMedium,
                        )
                        if (isSkipped) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                                Icon(Icons.Filled.Flight, null, tint = c.warning, modifier = Modifier.size(HqIconSize.xs))
                                Text("Out of Station (Skipped)", color = c.warning, style = HqType.labelSmall)
                            }
                        } else {
                            Text(entry.subtitle, color = c.textSecondary, style = HqType.labelSmall)
                        }
                    }
                    when (entry.state) {
                        QueueState.NOW -> Box(Modifier.clip(RoundedCornerShape(HqRadius.full)).background(c.brandPrimary).padding(horizontal = HqSpacing.md, vertical = HqSpacing.xs)) {
                            Text("NOW", color = c.onBrandPrimary, style = HqType.labelSmall)
                        }
                        QueueState.NEXT -> Text("Next", color = c.brandPrimary, style = HqType.labelSmall)
                        else -> Text("Position ${entry.position}", color = c.textSecondary, style = HqType.labelSmall)
                    }
                }
            }
        }
    }
}

private fun buildQueue(task: FlatTask, members: List<Member>): List<QueueEntry> {
    val order = task.queueOrder.ifEmpty { members.map { it.uid } }
    val currentIndex = order.indexOf(task.currentAssignedUserId).coerceAtLeast(0)
    val nextUid = RotationEngine.getNextAssignee(task, members)
    return order.mapIndexedNotNull { index, uid ->
        val member = members.find { it.uid == uid } ?: return@mapIndexedNotNull null
        val position = index + 1
        val isOos = member.status == "out_of_station" || member.status == "inactive"
        val state = when {
            uid == task.currentAssignedUserId && !isOos -> QueueState.NOW
            uid == nextUid && uid != task.currentAssignedUserId -> QueueState.NEXT
            isOos -> QueueState.SKIPPED
            else -> QueueState.QUEUED
        }
        val subtitle = when (state) {
            QueueState.NOW -> {
                val reliability = "${member.reliabilityScore}% reliability"
                val lastDone = formatLastDone(task.lastCompletedAt)
                if (lastDone.isNotBlank()) "$lastDone · $reliability" else reliability
            }
            QueueState.NEXT -> "Expected: ${formatExpectedDue(task.dueDate)}"
            QueueState.QUEUED -> "Reliability: ${member.reliabilityScore}%"
            QueueState.SKIPPED -> ""
        }
        QueueEntry(member, position, state, subtitle)
    }
}

private fun computeOverdueDays(task: FlatTask): Int? {
    if (effectiveTaskStatus(task) != "overdue") return null
    return runCatching {
        val due = parseTaskInstant(task.dueDate) ?: return@runCatching null
        java.time.Duration.between(due, Instant.now()).toDays().toInt().coerceAtLeast(1)
    }.getOrNull()
}

private fun formatLastDone(iso: String): String = runCatching {
    val dt = Instant.parse(iso).atZone(ZoneId.systemDefault())
    "Last done: ${dt.format(DateTimeFormatter.ofPattern("EEE dd MMM, h:mm a"))}"
}.getOrDefault("Last done: —")
