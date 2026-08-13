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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.data.FlatTask
import habitiq.app.flats.Member
import habitiq.app.lib.RotationEngine
import habitiq.app.ui.theme.FigmaColors
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
    val queue = remember(task, members) { buildQueue(task, members) }
    val overdueDays = remember(task) { computeOverdueDays(task) }

    FigmaScreenBackground {
        FigmaBackHeader(title = task.name, onBack = onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            TaskSummaryCard(task, overdueDays)
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ROTATION QUEUE", color = FigmaColors.InkSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.3.sp)
                Text("${queue.size} Members", color = FigmaColors.Primary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                queue.forEachIndexed { index, entry ->
                    QueueMemberCard(entry, showConnector = index < queue.lastIndex)
                }
            }
            if (task.currentAssignedUserId == currentUid && task.status != "completed") {
                Text("Request swap", fontWeight = FontWeight.SemiBold, color = FigmaColors.Ink)
                members.filter { it.uid != currentUid && it.status != "out_of_station" }.forEach { m ->
                    OutlinedButton(onClick = { onRequestSwap(m.uid) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Ask ${m.nickname} to cover")
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TaskSummaryCard(task: FlatTask, overdueDays: Int?) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(FigmaColors.Surface)
            .border(1.dp, FigmaColors.SurfaceBorder, RoundedCornerShape(24.dp))
            .padding(25.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(FigmaColors.PrimaryLight), contentAlignment = Alignment.Center) {
            Text("🧹", fontSize = 22.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (overdueDays != null && overdueDays > 0) {
                Box(Modifier.clip(RoundedCornerShape(999.dp)).background(FigmaColors.OverdueBg.copy(0.2f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text("${overdueDays}d overdue", color = FigmaColors.Overdue, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
            Text(task.name, color = FigmaColors.Ink, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("📅 ${task.frequency.replaceFirstChar { it.uppercase() }}", color = FigmaColors.InkSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.26.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(FigmaColors.Warning))
                    Text("${task.priority.replaceFirstChar { it.uppercase() }} Priority", color = FigmaColors.InkSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.26.sp)
                }
            }
        }
    }
}

@Composable
private fun QueueMemberCard(entry: QueueEntry, showConnector: Boolean) {
    val isNow = entry.state == QueueState.NOW
    val isSkipped = entry.state == QueueState.SKIPPED
    val (bgColor, borderColor, textColor) = avatarPalette(entry.position - 1)
    val cardBg = if (isSkipped) FigmaColors.SurfaceMuted else FigmaColors.Surface
    val cardBorder = when (entry.state) {
        QueueState.NOW -> 2.dp to FigmaColors.Primary
        else -> 1.dp to FigmaColors.SurfaceBorder
    }
    Box {
        if (showConnector) {
            Box(
                Modifier.align(Alignment.TopStart).offset(x = 24.dp, y = 48.dp)
                    .width(2.dp).height(80.dp).background(FigmaColors.SurfaceBorder)
            )
        }
        Row(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(cardBg)
                .border(cardBorder.first, cardBorder.second, RoundedCornerShape(24.dp))
                .padding(horizontal = 17.dp, vertical = if (isNow) 18.dp else 17.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(bgColor).border(2.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(memberInitials(entry.member.nickname), color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            entry.member.nickname,
                            color = FigmaColors.Ink.copy(alpha = if (isSkipped) 0.6f else 1f),
                            fontSize = if (isNow) 17.sp else 15.sp,
                            fontWeight = if (isNow) FontWeight.Medium else FontWeight.Normal,
                            lineHeight = if (isNow) 25.5.sp else 22.5.sp
                        )
                        if (isSkipped) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Filled.Flight, null, tint = FigmaColors.Warning, modifier = Modifier.size(12.dp))
                                Text("Out of Station (Skipped)", color = FigmaColors.Warning, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        } else {
                            Text(entry.subtitle, color = FigmaColors.InkSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    when (entry.state) {
                        QueueState.NOW -> Box(Modifier.clip(RoundedCornerShape(999.dp)).background(FigmaColors.Primary).padding(horizontal = 12.dp, vertical = 4.dp)) {
                            Text("NOW", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.275).sp)
                        }
                        QueueState.NEXT -> Text("Next", color = FigmaColors.Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        else -> Text("Position ${entry.position}", color = FigmaColors.InkSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
                if (isNow && entry.reliability != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(999.dp)).background(FigmaColors.SurfaceMuted)) {
                            Box(Modifier.fillMaxHeight().fillMaxWidth(entry.reliability / 100f).clip(RoundedCornerShape(999.dp)).background(FigmaColors.Success))
                        }
                        Text("${entry.reliability}% Reliability", color = FigmaColors.Success, fontSize = 11.sp, fontWeight = FontWeight.Medium)
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
            QueueState.NOW -> formatLastDone(task.lastCompletedAt)
            QueueState.NEXT -> "Expected: ${formatExpected(task.dueDate)}"
            QueueState.QUEUED -> "Reliability: ${member.reliabilityScore}%"
            QueueState.SKIPPED -> ""
        }
        QueueEntry(member, position, state, subtitle, if (state == QueueState.NOW) member.reliabilityScore else null)
    }
}

private fun computeOverdueDays(task: FlatTask): Int? {
    if (task.status != "overdue") return null
    return runCatching {
        val due = Instant.parse(task.dueDate)
        java.time.Duration.between(due, Instant.now()).toDays().toInt().coerceAtLeast(1)
    }.getOrNull()
}

private fun formatLastDone(iso: String): String = runCatching {
    val dt = Instant.parse(iso).atZone(ZoneId.systemDefault())
    "Last done: ${dt.format(DateTimeFormatter.ofPattern("EEE dd MMM, h:mm a"))}"
}.getOrDefault("Last done: —")

private fun formatExpected(dueDate: String): String = runCatching {
    val dt = Instant.parse(dueDate).atZone(ZoneId.systemDefault())
    dt.format(DateTimeFormatter.ofPattern("EEE dd MMM"))
}.getOrDefault("soon")
