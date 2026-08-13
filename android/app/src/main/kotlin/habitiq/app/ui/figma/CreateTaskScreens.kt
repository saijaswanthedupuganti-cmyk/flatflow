package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.ui.collectAsStateWithLifecycleCompat
import habitiq.app.ui.theme.FigmaColors

@Composable
fun CreateRecurringTaskScreen(
    viewModel: FlatViewModel,
    onBack: () -> Unit,
    onCreated: () -> Unit,
    taskType: String = "rotating_duty"
) {
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    var taskName by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("weekly") }
    var priority by remember { mutableStateOf("medium") }
    var selected by remember { mutableStateOf(members.map { it.uid }.toSet()) }

    LaunchedEffect(members) {
        if (selected.isEmpty()) selected = members.map { it.uid }.toSet()
    }

    FigmaScreenBackground {
        FigmaBackHeader(
            title = "Recurring Duty",
            onBack = onBack,
            subtitle = "Set up a rotating task for your flat."
        )
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("TASK NAME", color = FigmaColors.InkSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.26.sp)
                FigmaTextField(value = taskName, onValueChange = { taskName = it }, placeholder = "e.g., Sunday Cleaning")
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("FREQUENCY", color = FigmaColors.InkSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.26.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("daily", "weekly", "fortnightly", "monthly").forEach { f ->
                        FilterChip(selected = frequency == f, onClick = { frequency = f }, label = { Text(f) })
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PRIORITY", color = FigmaColors.InkSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.26.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("low", "medium", "high").forEach { p ->
                        FilterChip(selected = priority == p, onClick = { priority = p }, label = { Text(p) })
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ROTATION ORDER", color = FigmaColors.InkSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.26.sp)
                members.forEach { member ->
                    MemberPickerRow(member, selected.contains(member.uid)) {
                        selected = if (selected.contains(member.uid)) selected - member.uid else selected + member.uid
                    }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
        Column(Modifier.fillMaxWidth().background(FigmaColors.Background).navigationBarsPadding().padding(20.dp)) {
            FigmaPrimaryButton(
                text = "Create Task & Start Rotation",
                enabled = taskName.isNotBlank() && selected.isNotEmpty(),
                onClick = {
                    viewModel.createTask(taskName, frequency, priority, selected.toList(), taskType)
                    onCreated()
                }
            )
        }
    }
}

@Composable
fun CreateTempTaskScreen(viewModel: FlatViewModel, onBack: () -> Unit, onCreated: () -> Unit) {
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    var taskName by remember { mutableStateOf("") }
    var assigneeUid by remember { mutableStateOf<String?>(null) }
    var priority by remember { mutableStateOf("medium") }

    FigmaScreenBackground {
        FigmaBackHeader(title = "Temp Task", onBack = onBack, subtitle = "One person, one job. Done once and closed.")
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("TASK", color = FigmaColors.InkSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.26.sp)
                FigmaTextField(value = taskName, onValueChange = { taskName = it }, placeholder = "e.g., Buy vegetables", fontSize = 16)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ASSIGN TO", color = FigmaColors.InkSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.26.sp)
                members.forEach { member ->
                    MemberPickerRow(member, assigneeUid == member.uid) { assigneeUid = member.uid }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PRIORITY", color = FigmaColors.InkSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.26.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("low", "medium", "high").forEach { p ->
                        FilterChip(selected = priority == p, onClick = { priority = p }, label = { Text(p) })
                    }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
        Column(Modifier.fillMaxWidth().background(FigmaColors.Background).navigationBarsPadding().padding(20.dp)) {
            FigmaPrimaryButton(
                text = "Assign Task",
                enabled = taskName.isNotBlank() && assigneeUid != null,
                onClick = {
                    val uid = assigneeUid ?: return@FigmaPrimaryButton
                    viewModel.createTask(taskName, "one_time", priority, listOf(uid))
                    onCreated()
                }
            )
        }
    }
}

@Composable
private fun MemberPickerRow(member: Member, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) FigmaColors.PrimaryLight.copy(0.4f) else FigmaColors.Surface)
            .border(1.dp, if (selected) FigmaColors.Primary else FigmaColors.SurfaceBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MemberInitialAvatar(name = member.nickname, selected = selected)
        Text(member.nickname, color = FigmaColors.Ink, fontSize = 17.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        FigmaChevronDown()
    }
}
