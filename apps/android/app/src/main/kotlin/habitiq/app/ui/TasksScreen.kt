package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import habitiq.app.data.FlatTask
import habitiq.app.data.FlatSwapRequest
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.lib.RotationEngine
import habitiq.app.lib.effectiveTaskStatus
import habitiq.app.lib.formatDueLabel
import habitiq.app.ui.components.HqBottomSheet
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqRootAppBar
import habitiq.app.ui.figma.NextUpLabel
import habitiq.app.ui.figma.SwapRequestBanner
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: FlatViewModel,
    onBack: (() -> Unit)? = null,
    onOpenGoingAway: () -> Unit = {},
    onOpenTaskDetail: (String) -> Unit = {},
    onOpenCreateTask: () -> Unit = {},
    onReviewSwaps: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val c = LocalHqColors.current
    val tasks by viewModel.tasks.collectAsStateWithLifecycleCompat()
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val swaps by viewModel.swapRequests.collectAsStateWithLifecycleCompat()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycleCompat()
    val currentMember by viewModel.currentMember.collectAsStateWithLifecycleCompat()
    val triggerAdd by viewModel.showAddTaskTrigger.collectAsStateWithLifecycleCompat()

    var filterTab by remember { mutableStateOf("All") }
    val activeTab = filterTab
    LaunchedEffect(triggerAdd) { if (triggerAdd) { onOpenCreateTask(); viewModel.showAddTaskTrigger.value = false } }

    val uid = currentUser?.uid.orEmpty()
    val openTasks = remember(tasks) { tasks.filter { effectiveTaskStatus(it) != "completed" } }
    val myCount = openTasks.count { it.currentAssignedUserId == uid }
    val overdueCount = openTasks.count { effectiveTaskStatus(it) == "overdue" }
    val displayed = remember(tasks, activeTab, uid) {
        when (activeTab) {
            "Mine" -> openTasks.filter { it.currentAssignedUserId == uid }
            "Overdue" -> openTasks.filter { effectiveTaskStatus(it) == "overdue" }
            else -> openTasks
        }
    }
    val overdueTasks = displayed.filter { effectiveTaskStatus(it) == "overdue" }
    val todayTasks = displayed.filter { effectiveTaskStatus(it) != "overdue" && formatDueLabel(it.dueDate) == "Due Today" }
    val upcomingTasks = displayed.filter { effectiveTaskStatus(it) != "overdue" && formatDueLabel(it.dueDate) != "Due Today" }
    val completedTasks = remember(tasks, activeTab, uid) {
        val done = tasks.filter { effectiveTaskStatus(it) == "completed" }
        if (activeTab == "Mine") done.filter { it.currentAssignedUserId == uid } else done
    }
    val pendingSwapsForMe = remember(swaps, uid) { swaps.count { it.status == "pending" && it.toUserId == uid } }
    val isOos = currentMember?.status == "out_of_station"

    Box(modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize()) {
            if (onBack != null) HqBackAppBar(title = "Tasks", onBack = onBack) else HqRootAppBar(title = "Tasks")
            Row(Modifier.padding(horizontal = HqSpacing.lg), horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                listOf(
                    "All" to "All (${openTasks.size})",
                    "Mine" to "My tasks ($myCount)",
                    "Overdue" to "Overdue ($overdueCount)"
                ).forEach { (key, label) ->
                    HqChip(label = label, selected = activeTab == key, onClick = { filterTab = key })
                }
            }
            if (pendingSwapsForMe > 0) {
                Box(Modifier.padding(horizontal = HqSpacing.lg, vertical = HqSpacing.sm)) {
                    SwapRequestBanner(count = pendingSwapsForMe, onReview = onReviewSwaps)
                }
            }
            GoingAwayCard(isOos = isOos, onClick = onOpenGoingAway)
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(HqSpacing.lg), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                if (displayed.isEmpty()) {
                    item {
                        Text(
                            if (activeTab == "Mine") "Nothing assigned to you right now."
                            else "No tasks yet. Admins can create a simple rotation for the flat.",
                            color = c.textSecondary,
                            style = HqType.bodyMedium,
                            modifier = Modifier.padding(vertical = HqSpacing.xxl)
                        )
                    }
                }
                if (overdueTasks.isNotEmpty() && activeTab != "Overdue") {
                    item { Text("Overdue", style = HqType.titleMedium, color = c.error) }
                }
                items(if (activeTab == "Overdue") overdueTasks else overdueTasks, key = { "o-${it.taskId}" }) { task ->
                    if (activeTab != "All" && activeTab != "Overdue" && activeTab != "Mine") return@items
                    FigmaTaskCard(task, members, uid, { onOpenTaskDetail(task.taskId) }, { viewModel.completeTask(task) })
                }
                if (todayTasks.isNotEmpty()) {
                    item { Text("Today", style = HqType.titleMedium, color = c.textPrimary, modifier = Modifier.padding(top = HqSpacing.md)) }
                    items(todayTasks, key = { "t-${it.taskId}" }) { task ->
                        FigmaTaskCard(task, members, uid, { onOpenTaskDetail(task.taskId) }, { viewModel.completeTask(task) })
                    }
                }
                if (upcomingTasks.isNotEmpty()) {
                    item { Text("Upcoming", style = HqType.titleMedium, color = c.textPrimary, modifier = Modifier.padding(top = HqSpacing.md)) }
                    items(upcomingTasks, key = { "u-${it.taskId}" }) { task ->
                        FigmaTaskCard(task, members, uid, { onOpenTaskDetail(task.taskId) }, { viewModel.completeTask(task) })
                    }
                }
                if (activeTab != "Overdue" && completedTasks.isNotEmpty()) {
                    item { Text("Completed", style = HqType.titleMedium, color = c.textPrimary, modifier = Modifier.padding(top = HqSpacing.md)) }
                    items(completedTasks, key = { "c-${it.taskId}" }) { task ->
                        FigmaTaskCard(task, members, uid, { onOpenTaskDetail(task.taskId) }, {})
                    }
                }
            }
        }
        if (isAdmin) {
            FloatingActionButton(
                onClick = onOpenCreateTask,
                modifier = Modifier.align(Alignment.BottomEnd).padding(HqSpacing.lg),
                containerColor = c.brandPrimary
            ) { Icon(Icons.Filled.Add, "Add task") }
        }
    }
}

@Composable
private fun GoingAwayCard(isOos: Boolean, onClick: () -> Unit) {
    val c = LocalHqColors.current
    Box(Modifier.padding(horizontal = HqSpacing.lg, vertical = HqSpacing.sm)) {
        HqCard(variant = HqCardVariant.Interactive, onClick = onClick, padding = HqSpacing.md) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Flight, null, tint = c.brandPrimary, modifier = Modifier.size(HqIconSize.md))
                Spacer(Modifier.width(HqSpacing.sm))
                Column(Modifier.weight(1f)) {
                    Text(if (isOos) "You're away" else "I'm away", style = HqType.titleMedium, color = c.textPrimary)
                    Text(
                        if (isOos) "Tasks will skip you temporarily." else "Mark out of station when you go home.",
                        style = HqType.bodySmall,
                        color = c.textSecondary
                    )
                }
                Text(if (isOos) "Return" else "Open →", color = c.brandPrimary, style = HqType.labelLarge)
            }
        }
    }
}

@Composable
private fun FigmaTaskCard(
    task: FlatTask,
    members: List<Member>,
    uid: String,
    onOpenDetail: () -> Unit,
    onComplete: () -> Unit
) {
    val c = LocalHqColors.current
    val status = effectiveTaskStatus(task)
    val assignee = members.find { it.uid == task.currentAssignedUserId }
    val nextUid = RotationEngine.getNextAssignee(task, members)
    val nextName = members.find { it.uid == nextUid }?.nickname?.substringBefore(" ")?.ifBlank { null }
    val isMine = task.currentAssignedUserId == uid
    val showNextUp = nextUid != null && nextUid != task.currentAssignedUserId && nextName != null
    val taskIcon = when (task.type) {
        "group_duty" -> Icons.Filled.Groups
        "temp", "temp_task", "one_time" -> Icons.Filled.Bolt
        else -> Icons.Filled.Repeat
    }
    val iconTint = when (task.type) {
        "group_duty" -> c.info
        "temp", "temp_task", "one_time" -> c.warning
        else -> c.brandPrimary
    }
    val iconBackground = when (task.type) {
        "group_duty" -> c.infoContainer
        "temp", "temp_task", "one_time" -> c.warningContainer
        else -> c.brandPrimaryContainer
    }

    HqCard(variant = HqCardVariant.Interactive, onClick = onOpenDetail, padding = HqSpacing.md) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(HqRadius.md)).background(iconBackground),
                contentAlignment = Alignment.Center
            ) { Icon(taskIcon, null, tint = iconTint, modifier = Modifier.size(HqIconSize.md)) }
            Spacer(Modifier.width(HqSpacing.md))
            Column(Modifier.weight(1f)) {
                if (status == "overdue") {
                    Box(Modifier.clip(RoundedCornerShape(HqRadius.full)).background(c.errorContainer).padding(horizontal = HqSpacing.sm, vertical = 2.dp)) {
                        Text("Overdue", color = c.error, style = HqType.labelSmall)
                    }
                    Spacer(Modifier.height(HqSpacing.xs))
                }
                Text(task.name, style = HqType.titleLarge, color = c.textPrimary)
                Text(
                    if (status == "overdue") "Was due — complete now" else formatDueLabel(task.dueDate),
                    style = HqType.bodySmall,
                    color = if (status == "overdue") c.error else c.textSecondary
                )
                Text(
                    when {
                        isMine -> "YOU"
                        assignee != null -> assignee.nickname.substringBefore(" ")
                        else -> "Unassigned"
                    },
                    style = HqType.labelLarge,
                    color = if (isMine) c.brandPrimary else c.textSecondary
                )
                if (showNextUp) {
                    Spacer(Modifier.height(HqSpacing.xs))
                    NextUpLabel(nextName!!)
                }
            }
            Icon(Icons.Filled.ChevronRight, null, tint = c.textTertiary, modifier = Modifier.size(HqIconSize.md))
        }
        if (isMine && status != "completed") {
            Spacer(Modifier.height(HqSpacing.sm))
            HqButton(
                text = if (status == "overdue") "Complete now" else "Mark complete",
                onClick = onComplete,
                fullWidth = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapReviewSheet(
    swaps: List<FlatSwapRequest>,
    tasks: List<FlatTask>,
    members: List<Member>,
    uid: String,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val c = LocalHqColors.current
    val incoming = swaps.filter { it.status == "pending" && it.toUserId == uid }
    HqBottomSheet(onDismiss = onDismiss, title = "Swap Requests") {
        Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
            incoming.forEach { swap ->
                val taskName = tasks.find { it.taskId == swap.taskId }?.name ?: "task"
                val from = members.find { it.uid == swap.fromUserId }?.nickname ?: "Flatmate"
                HqCard(variant = HqCardVariant.Standard, padding = HqSpacing.md) {
                    Text("$from wants you to cover \"$taskName\"", style = HqType.bodyMedium, color = c.textPrimary)
                    Row(Modifier.padding(top = HqSpacing.sm), horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                        HqButton(text = "Accept", onClick = { onAccept(swap.id) }, fullWidth = false)
                        HqButton(text = "Decline", onClick = { onReject(swap.id) }, variant = HqButtonVariant.Secondary, fullWidth = false)
                    }
                }
            }
            if (incoming.isEmpty()) Text("No pending requests.", style = HqType.bodyMedium, color = c.textSecondary)
        }
    }
}
