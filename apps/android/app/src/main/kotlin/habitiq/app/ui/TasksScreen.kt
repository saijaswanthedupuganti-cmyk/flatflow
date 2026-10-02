package habitiq.app.ui

import habitiq.app.ui.components.HqTextButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import habitiq.app.data.FlatSwapRequest
import habitiq.app.data.FlatTask
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.lib.effectiveTaskStatus
import habitiq.app.lib.formatDueLabel
import habitiq.app.lib.formatWasDueLabel
import habitiq.app.lib.isDueToday
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqBottomSheet
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.components.HqRootAppBar
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import kotlinx.coroutines.delay

/**
 * Tasks. Members land on My tasks and admins on All tasks; every member can still read All tasks
 * (the rules allow it) but only authorised people get create or complete controls. Creation lives
 * on the shell's contextual "+" so there is a single create affordance.
 */
@Composable
fun TasksScreen(
    viewModel: FlatViewModel,
    onBack: (() -> Unit)? = null,
    onOpenGoingAway: () -> Unit = {},
    onOpenTaskDetail: (String) -> Unit = {},
    onOpenCreateTask: () -> Unit = {},
    onReviewSwaps: () -> Unit = {},
    modifier: Modifier = Modifier,
    /** Rendered at the top of the scrolling content; when set, the screen has no app bar of its own (Manage). */
    header: (@Composable () -> Unit)? = null,
) {
    val c = LocalHqColors.current
    val tasks by viewModel.tasks.collectAsStateWithLifecycleCompat()
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val swaps by viewModel.swapRequests.collectAsStateWithLifecycleCompat()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycleCompat()
    val currentMember by viewModel.currentMember.collectAsStateWithLifecycleCompat()
    val triggerAdd by viewModel.showAddTaskTrigger.collectAsStateWithLifecycleCompat()
    val haptics = LocalHapticFeedback.current

    var scope by remember(isAdmin) { mutableStateOf(if (isAdmin) TaskScope.All else TaskScope.Mine) }
    var overdueOnly by remember { mutableStateOf(false) }
    var completingId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(triggerAdd) { if (triggerAdd) { onOpenCreateTask(); viewModel.showAddTaskTrigger.value = false } }
    // The completion write is fire-and-forget, so hand the control back after a while if nothing changed.
    LaunchedEffect(completingId) { if (completingId != null) { delay(8_000); completingId = null } }

    val uid = currentUser?.uid.orEmpty()
    val model = remember(tasks, members, swaps, scope, overdueOnly, isAdmin, currentMember, uid) {
        buildTasksModel(tasks, members, swaps, uid, isAdmin, scope, overdueOnly, away = currentMember?.status == "out_of_station")
    }

    Box(modifier.fillMaxSize().background(c.canvas)) {
        Column(Modifier.fillMaxSize()) {
            if (header == null) { if (onBack != null) HqBackAppBar(title = "Tasks", onBack = onBack) else HqRootAppBar(title = "Tasks") }
            TasksContent(
                model = model,
                completingTaskId = completingId,
                onScope = { scope = it; overdueOnly = false },
                onToggleOverdue = { overdueOnly = !overdueOnly },
                onOpenAway = onOpenGoingAway,
                onReviewSwaps = onReviewSwaps,
                onOpenTask = onOpenTaskDetail,
                onCompleteTask = { id ->
                    tasks.find { it.taskId == id }?.let { task ->
                        completingId = id
                        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                        viewModel.completeTask(task)
                    }
                },
                onCreateTask = onOpenCreateTask,
                header = header ?: {},
            )
        }
    }
}

internal fun buildTasksModel(
    tasks: List<FlatTask>,
    members: List<Member>,
    swaps: List<FlatSwapRequest>,
    uid: String,
    isAdmin: Boolean,
    scope: TaskScope,
    overdueOnly: Boolean,
    away: Boolean,
): TasksUiModel {
    val open = tasks.filter { effectiveTaskStatus(it) != "completed" }
    val mineOpen = open.filter { it.currentAssignedUserId == uid }
    val scoped = if (scope == TaskScope.Mine) mineOpen else open
    val overdueInScope = scoped.count { effectiveTaskStatus(it) == "overdue" }
    val shown = if (overdueOnly) scoped.filter { effectiveTaskStatus(it) == "overdue" } else scoped

    fun item(task: FlatTask, completed: Boolean): TaskListItem {
        val mine = task.currentAssignedUserId == uid
        val overdue = !completed && effectiveTaskStatus(task) == "overdue"
        val assignee = members.find { it.uid == task.currentAssignedUserId }
        return TaskListItem(
            id = task.taskId,
            name = task.name,
            assigneeText = when {
                mine -> "You"
                assignee != null -> assignee.nickname.substringBefore(" ").ifBlank { "A flatmate" }
                else -> "Unassigned"
            },
            dueText = when {
                completed -> "Done"
                overdue -> formatWasDueLabel(task.dueDate)
                else -> formatDueLabel(task.dueDate)
            },
            overdue = overdue,
            completed = completed,
            canComplete = mine && !completed,
            kind = when (task.type) {
                "group_duty" -> HomeTaskKind.Group
                "temp", "temp_task", "one_time" -> HomeTaskKind.OneOff
                else -> HomeTaskKind.Rotating
            },
        )
    }

    val overdue = shown.filter { effectiveTaskStatus(it) == "overdue" }
    val today = shown.filter { effectiveTaskStatus(it) != "overdue" && isDueToday(it.dueDate) }
    val upcoming = shown.filter { effectiveTaskStatus(it) != "overdue" && !isDueToday(it.dueDate) }
    // Completed work is quieter: hidden from My tasks and the Overdue filter, listed last in All tasks.
    val completed = if (scope == TaskScope.All && !overdueOnly) tasks.filter { effectiveTaskStatus(it) == "completed" } else emptyList()

    val sections = buildList {
        if (overdue.isNotEmpty()) add(TaskSection("Overdue", overdue.map { item(it, false) }))
        if (today.isNotEmpty()) add(TaskSection("Today", today.map { item(it, false) }))
        if (upcoming.isNotEmpty()) add(TaskSection("Upcoming", upcoming.map { item(it, false) }))
        if (completed.isNotEmpty()) add(TaskSection("Completed", completed.map { item(it, true) }, quiet = true))
    }

    return TasksUiModel(
        scope = scope,
        overdueOnly = overdueOnly,
        mineCount = mineOpen.size,
        allCount = open.size,
        overdueCount = overdueInScope,
        away = away,
        pendingSwapsForMe = swaps.count { it.status == "pending" && it.toUserId == uid },
        isAdmin = isAdmin,
        sections = sections,
    )
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
    HqBottomSheet(onDismiss = onDismiss, title = "Swap requests") {
        Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
            incoming.forEach { swap ->
                val taskName = tasks.find { it.taskId == swap.taskId }?.name ?: "task"
                val from = members.find { it.uid == swap.fromUserId }?.nickname ?: "Flatmate"
                HqCard(variant = HqCardVariant.Standard, padding = HqSpacing.component) {
                    Text("$from wants you to cover \"$taskName\"", style = HqType.bodyLarge, color = c.textPrimary)
                    Row(Modifier.padding(top = HqSpacing.sm), horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                        HqButton(text = "Accept", onClick = { onAccept(swap.id) }, fullWidth = false)
                        HqButton(text = "Decline", onClick = { onReject(swap.id) }, variant = HqButtonVariant.Secondary, fullWidth = false)
                    }
                }
            }
            if (incoming.isEmpty()) Text("No pending requests.", style = HqType.bodyLarge, color = c.textSecondary)
        }
    }
}
