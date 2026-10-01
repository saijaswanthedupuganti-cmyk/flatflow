package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.ui.collectAsStateWithLifecycleCompat
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqFadeUp
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

@Composable
fun CreateRecurringTaskScreen(
    viewModel: FlatViewModel,
    onBack: () -> Unit,
    onCreated: () -> Unit,
    taskType: String = "rotating_duty"
) {
    val c = LocalHqColors.current
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    var taskName by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("weekly") }
    var priority by remember { mutableStateOf("medium") }
    var startFrom by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf(members.map { it.uid }.toSet()) }

    LaunchedEffect(members) {
        if (selected.isEmpty()) selected = members.map { it.uid }.toSet()
    }

    FigmaScreenBackground {
        HqBackAppBar(title = "Create Recurring Task", onBack = onBack)
        Text(
            "Set up a rotating task for your flat.",
            color = c.textSecondary,
            style = HqType.bodyLarge,
            modifier = Modifier.padding(horizontal = HqSpacing.xl).padding(bottom = HqSpacing.sm)
        )
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.xl)
        ) {
            HqTextField(value = taskName, onValueChange = { taskName = it }, label = "Task name", placeholder = "e.g., Room cleaning", leadingIcon = Icons.Filled.TaskAlt)
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                SectionLabel("FREQUENCY")
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    listOf("daily", "weekly", "fortnightly", "monthly").forEach { f ->
                        HqChip(label = f, selected = frequency == f, onClick = { frequency = f })
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                SectionLabel("ASSIGN TO")
                Text("Rotate between the members you select.", style = HqType.bodySmall, color = c.textSecondary)
                members.forEach { member ->
                    MemberPickerRow(member, selected.contains(member.uid)) {
                        selected = if (selected.contains(member.uid)) selected - member.uid else selected + member.uid
                    }
                }
            }
            HqTextField(value = startFrom, onValueChange = { startFrom = it }, label = "Start from", placeholder = "Today, or YYYY-MM-DD", leadingIcon = Icons.Filled.CalendarMonth)
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                SectionLabel("PRIORITY")
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    listOf("low", "medium", "high").forEach { p ->
                        HqChip(label = p, selected = priority == p, onClick = { priority = p })
                    }
                }
            }
            HqTextField(value = notes, onValueChange = { notes = it }, label = "Notes (optional)", placeholder = "e.g. Include bathroom and common area", leadingIcon = Icons.Filled.EditNote)
            Spacer(Modifier.height(HqSpacing.huge))
        }
        Column(Modifier.fillMaxWidth().background(c.background).navigationBarsPadding().padding(HqSpacing.xl)) {
            saveError?.let { Text(it, color = c.error, style = HqType.bodySmall) }
            HqButton(
                text = if (saving) "Creating…" else "Create task",
                enabled = taskName.isNotBlank() && selected.isNotEmpty() && !saving,
                onClick = {
                    val queue = members.filter { selected.contains(it.uid) }.map { it.uid }
                    val due = startFrom.takeIf { Regex("""\d{4}-\d{2}-\d{2}""").containsMatchIn(it) }
                    saving = true
                    saveError = null
                    viewModel.createTask(taskName, frequency, priority, queue, taskType, dueDate = due, notes = notes) { ok ->
                        saving = false
                        if (ok) onCreated() else saveError = "Couldn't create the task. Try again."
                    }
                }
            )
        }
    }
}

@Composable
fun CreateTempTaskScreen(viewModel: FlatViewModel, onBack: () -> Unit, onCreated: () -> Unit) {
    val c = LocalHqColors.current
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    var taskName by remember { mutableStateOf("") }
    var assigneeUid by remember { mutableStateOf<String?>(null) }
    var priority by remember { mutableStateOf("medium") }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    FigmaScreenBackground {
        HqBackAppBar(title = "Temp Task", onBack = onBack)
        Text(
            "One person, one job. Done once and closed.",
            color = c.textSecondary,
            style = HqType.bodyLarge,
            modifier = Modifier.padding(horizontal = HqSpacing.xl).padding(bottom = HqSpacing.sm)
        )
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.xl)
        ) {
            HqTextField(value = taskName, onValueChange = { taskName = it }, label = "Task", placeholder = "e.g., Buy groceries", leadingIcon = Icons.Filled.TaskAlt)
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                SectionLabel("ASSIGN TO")
                members.forEach { member ->
                    MemberPickerRow(member, assigneeUid == member.uid) { assigneeUid = member.uid }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                SectionLabel("PRIORITY")
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    listOf("low", "medium", "high").forEach { p ->
                        HqChip(label = p, selected = priority == p, onClick = { priority = p })
                    }
                }
            }
            Spacer(Modifier.height(HqSpacing.huge))
        }
        Column(Modifier.fillMaxWidth().background(c.background).navigationBarsPadding().padding(HqSpacing.xl)) {
            saveError?.let { Text(it, color = c.error, style = HqType.bodySmall) }
            HqButton(
                text = if (saving) "Assigning…" else "Assign Task",
                enabled = taskName.isNotBlank() && assigneeUid != null && !saving,
                onClick = {
                    val uid = assigneeUid ?: return@HqButton
                    saving = true
                    saveError = null
                    viewModel.createTask(taskName, "one_time", priority, listOf(uid)) { ok ->
                        saving = false
                        if (ok) onCreated() else saveError = "Couldn't assign the task. Try again."
                    }
                }
            )
        }
    }
}

@Composable
fun TaskCreatedScreen(onViewTask: () -> Unit, onAddAnother: () -> Unit) {
    val c = LocalHqColors.current
    HqFadeUp(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().background(c.background).padding(HqSpacing.xxl),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(88.dp).clip(CircleShape).background(c.successContainer),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.CheckCircle, null, tint = c.success, modifier = Modifier.size(52.dp)) }
            Spacer(Modifier.height(HqSpacing.xl))
            Text("Task created!", style = HqType.headlineMedium, color = c.textPrimary)
            Text(
                "Your task is ready and visible to the flat.",
                style = HqType.bodyLarge,
                color = c.textSecondary,
                modifier = Modifier.padding(top = HqSpacing.sm, bottom = HqSpacing.xl)
            )
            HqButton(text = "View tasks", onClick = onViewTask)
            Spacer(Modifier.height(HqSpacing.md))
            HqButton(text = "Add another task", onClick = onAddAnother, variant = HqButtonVariant.Secondary)
        }
    }
}

/** Uppercase section caption above a field/chip group -- matches the design doc's form-section-label pattern. */
@Composable
private fun SectionLabel(text: String) {
    val c = LocalHqColors.current
    Text(text, color = c.textSecondary, style = HqType.labelMedium, letterSpacing = 0.26.sp)
}

@Composable
private fun MemberPickerRow(member: Member, selected: Boolean, onClick: () -> Unit) {
    val c = LocalHqColors.current
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(HqRadius.md))
            .background(if (selected) c.brandPrimaryContainer.copy(alpha = 0.4f) else c.surface)
            .border(1.dp, if (selected) c.brandPrimary else c.borderDefault, RoundedCornerShape(HqRadius.md))
            .clickable(onClick = onClick)
            .padding(HqSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)
    ) {
        // HqAvatar has no built-in selected/checkmark affordance, so this keeps the shared
        // MemberInitialAvatar primitive (out of this batch's scope) which already renders one.
        MemberInitialAvatar(name = member.nickname, selected = selected)
        Text(member.nickname, color = c.textPrimary, style = HqType.titleMedium, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.KeyboardArrowDown, null, tint = c.textSecondary, modifier = Modifier.size(HqIconSize.sm))
    }
}
