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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import habitiq.app.ui.theme.FigmaColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun GoingAwayScreen(viewModel: FlatViewModel, onBack: () -> Unit, onSent: () -> Unit) {
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
        FigmaBackHeader(
            title = "Going Away",
            onBack = onBack,
            subtitle = "Assign your tasks to a flatmate before you leave. Accountability remains with you until they accept."
        )
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            StatusCard(pendingCount = myTasks.size)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "YOUR TASK LIST",
                    color = FigmaColors.InkSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.26.sp
                )
                if (myTasks.isEmpty()) {
                    Text("No pending tasks assigned to you.", color = FigmaColors.InkSecondary, fontSize = 15.sp)
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
            Spacer(Modifier.height(120.dp))
        }
        Column(
            Modifier.fillMaxWidth()
                .background(FigmaColors.Background.copy(0.92f))
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            FigmaPrimaryButton(
                text = "Send Requests",
                enabled = allAssigned && availableMembers.isNotEmpty(),
                onClick = {
                    viewModel.sendGoingAwayRequests(selections)
                    onSent()
                }
            )
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel & Return", color = FigmaColors.Primary, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun StatusCard(pendingCount: Int) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FigmaColors.Primary)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("STATUS", color = FigmaColors.PrimaryMuted.copy(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.55.sp)
            Text("$pendingCount Pending Tasks", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }
        Box(Modifier.size(48.dp).clip(CircleShape).background(FigmaColors.PrimaryDark), contentAlignment = Alignment.Center) {
            Text("✈", fontSize = 20.sp)
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
    val isOverdue = task.status == "overdue"
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FigmaColors.Surface)
            .border(1.dp, FigmaColors.SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                StatusPill(
                    text = if (isOverdue) "Overdue" else dayLabel(task.dueDate),
                    bg = if (isOverdue) FigmaColors.OverdueBg else FigmaColors.BadgeNeutralBg,
                    fg = if (isOverdue) FigmaColors.Overdue else FigmaColors.InkSecondary
                )
                Spacer(Modifier.height(7.dp))
                Text(task.name, color = FigmaColors.Ink, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                Text(
                    if (isOverdue) "Was due on ${dayName(task.dueDate)}" else frequencyLabel(task.frequency),
                    color = FigmaColors.InkSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(FigmaColors.SurfaceMuted), contentAlignment = Alignment.Center) {
                Text(taskEmoji(task.name), fontSize = 18.sp)
            }
        }
        Column(Modifier.fillMaxWidth().padding(top = 1.dp)) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(FigmaColors.SurfaceBorder))
            Spacer(Modifier.height(17.dp))
            Text("Reassign to:", color = FigmaColors.InkSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.26.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                members.forEach { member ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSelect(member.uid) }) {
                        MemberInitialAvatar(
                            name = member.nickname,
                            selected = selectedUid == member.uid,
                            modifier = Modifier.then(if (selectedUid != member.uid && selectedUid != null) Modifier else Modifier)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            member.nickname.split(" ").firstOrNull() ?: member.nickname,
                            color = if (selectedUid == member.uid) FigmaColors.Primary else FigmaColors.InkSecondary,
                            fontSize = 11.sp,
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
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FigmaColors.SurfaceDim)
            .border(1.dp, FigmaColors.SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(17.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Filled.Info, null, tint = FigmaColors.InkDeep, modifier = Modifier.size(20.dp))
        Text(
            "Requests will be sent to your flatmates. You'll be marked 'Away' only after all tasks are accepted.",
            color = FigmaColors.InkDeep,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 21.sp,
            letterSpacing = 0.26.sp
        )
    }
}

@Composable
private fun StatusPill(text: String, bg: Color, fg: Color) {
    Box(Modifier.clip(RoundedCornerShape(999.dp)).background(bg).padding(horizontal = 8.dp, vertical = 2.dp)) {
        Text(text, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Medium)
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
