package habitiq.app.ui

import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqManageSwitch
import habitiq.app.ui.components.HqPageHeader
import habitiq.app.lib.formatTimeAgo
import androidx.compose.foundation.layout.Box
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.components.HqTextButton
import habitiq.app.lib.formatActivityTime
import habitiq.app.lib.activityActionLabel
import habitiq.app.lib.formatInr
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import habitiq.app.flat.FlatViewModel
import habitiq.app.lib.currentMonthKey
import habitiq.app.lib.computeMonthNetBalances
import habitiq.app.lib.daysOverdue
import habitiq.app.lib.effectiveTaskStatus
import habitiq.app.lib.pairwisePersonalBalances
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import kotlin.math.abs

/**
 * Manage Flat -- the operational hub for the current flat (master spec sections 16-20, 60-64).
 * Tasks and Expenses are NOT separate root destinations; they're areas within this one hub, and
 * Monthly Bills lives inside Expenses so the hub keeps only two clear primary choices. Not to be confused with [FlatSettingsScreen] (flat name / join
 * mode / Discover vacancy listing), which is an admin settings screen reached from Profile.
 */
enum class ManageFlatArea { HUB, TASKS, EXPENSES }

@Composable
fun ManageFlatHub(
    viewModel: FlatViewModel,
    area: ManageFlatArea,
    onAreaChange: (ManageFlatArea) -> Unit,
    onOpenGoingAway: () -> Unit,
    onOpenTaskDetail: (String) -> Unit,
    onOpenCreateTask: () -> Unit,
    onReviewSwaps: () -> Unit,
    onOpenBills: () -> Unit,
    onOpenActivity: () -> Unit
) {
    // Figma Manage: one screen with a Tasks / Expenses switch under the page header. HUB is the legacy
    // landing value and now simply means Tasks, so existing callers and saved state keep working.
    val onExpenses = area == ManageFlatArea.EXPENSES
    val header: @Composable () -> Unit = {
        Column {
            HqPageHeader(title = "Manage Flat", subtitle = "Everything your home needs, in one place.")
            HqManageSwitch(
                options = listOf("Tasks" to HqIcons.Check, "Expenses" to HqIcons.Receipt),
                selectedIndex = if (onExpenses) 1 else 0,
                onSelect = { onAreaChange(if (it == 1) ManageFlatArea.EXPENSES else ManageFlatArea.TASKS) },
                modifier = Modifier.padding(bottom = 23.dp),
            )
        }
    }
    if (onExpenses) {
        ExpensesScreen(
            viewModel,
            onBack = null,
            onOpenBills = onOpenBills,
            modifier = Modifier.fillMaxSize(),
            header = header,
        )
    } else {
        TasksScreen(
            viewModel = viewModel,
            onBack = null,
            onOpenGoingAway = onOpenGoingAway,
            onOpenTaskDetail = onOpenTaskDetail,
            onOpenCreateTask = onOpenCreateTask,
            onReviewSwaps = onReviewSwaps,
            modifier = Modifier.fillMaxSize(),
            header = header,
        )
    }
}

@Composable
private fun ManageFlatLanding(
    viewModel: FlatViewModel,
    onAreaChange: (ManageFlatArea) -> Unit,
    onOpenTaskDetail: (String) -> Unit,
    onReviewSwaps: () -> Unit,
    onOpenActivity: () -> Unit
) {
    val c = LocalHqColors.current
    val flatInfo by viewModel.flatInfo.collectAsStateWithLifecycleCompat()
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val tasks by viewModel.tasks.collectAsStateWithLifecycleCompat()
    val expenses by viewModel.expenses.collectAsStateWithLifecycleCompat()
    val settlements by viewModel.settlements.collectAsStateWithLifecycleCompat()
    val swapRequests by viewModel.swapRequests.collectAsStateWithLifecycleCompat()
    val activity by viewModel.activity.collectAsStateWithLifecycleCompat()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    val uid = currentUser?.uid.orEmpty()

    val model = remember(flatInfo, members, tasks, expenses, settlements, swapRequests, activity, uid) {
        buildManageModel(
            flatName = flatInfo?.name.orEmpty(),
            memberCount = members.size.takeIf { it > 0 } ?: flatInfo?.memberCount ?: 0,
            tasks = tasks,
            balances = pairwisePersonalBalances(expenses, settlements, uid),
            pendingSwapsForMe = swapRequests.count { it.status == "pending" && it.toUserId == uid },
            waitingSwaps = swapRequests.count { it.status == "pending" && it.fromUserId == uid },
            activity = activity,
            members = members,
            uid = uid,
        )
    }

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        ManageContent(
            model = model,
            onOpenTasks = { onAreaChange(ManageFlatArea.TASKS) },
            onOpenExpenses = { onAreaChange(ManageFlatArea.EXPENSES) },
            onAttention = { item ->
                when (item.kind) {
                    ManageAttention.Kind.OverdueTask -> item.taskId?.let(onOpenTaskDetail)
                    ManageAttention.Kind.Balance -> {
                        viewModel.showBalancesTrigger.value = true
                        onAreaChange(ManageFlatArea.EXPENSES)
                    }
                    ManageAttention.Kind.Swaps -> onReviewSwaps()
                }
            },
            onOpenActivity = onOpenActivity,
        )
    }
}

internal fun buildManageModel(
    flatName: String,
    memberCount: Int,
    tasks: List<habitiq.app.data.FlatTask>,
    balances: Map<String, Double>,
    pendingSwapsForMe: Int,
    waitingSwaps: Int,
    activity: List<habitiq.app.data.FlatActivity>,
    members: List<habitiq.app.flats.Member>,
    uid: String,
): ManageUiModel {
    fun plural(n: Int, one: String, many: String = one + "s") = "$n ${if (n == 1) one else many}"
    val mine = tasks.filter { it.currentAssignedUserId == uid && effectiveTaskStatus(it) != "completed" }
    val overdueMine = mine.filter { effectiveTaskStatus(it) == "overdue" }.sortedByDescending { daysOverdue(it.dueDate) }
    val owe = balances.filter { it.value < 0 }
    val owed = balances.filter { it.value > 0 }
    val totalOwe = owe.values.sumOf { abs(it) }
    val totalOwed = owed.values.sum()

    val tasksLines = buildList {
        if (mine.isEmpty()) add(TileLine("Nothing assigned to you"))
        else {
            add(TileLine("${mine.size} assigned"))
            if (overdueMine.isNotEmpty()) add(TileLine("${overdueMine.size} overdue", urgent = true))
        }
    }
    // Both directions are named; opposite balances are never reported as settled.
    val expensesLines = buildList {
        if (totalOwe > 0) add(TileLine("You owe ${formatInr(totalOwe)}"))
        if (totalOwed > 0) add(TileLine("You're owed ${formatInr(totalOwed)}"))
        if (totalOwe <= 0 && totalOwed <= 0) add(TileLine("All settled"))
    }

    val attention = buildList {
        overdueMine.firstOrNull()?.let { task ->
            val days = daysOverdue(task.dueDate)
            add(
                ManageAttention(
                    ManageAttention.Kind.OverdueTask,
                    if (overdueMine.size > 1) "${overdueMine.size} overdue tasks" else "Overdue task",
                    "${task.name} · " + (if (days <= 1L) "was due yesterday" else "was due $days days ago"),
                    task.taskId,
                )
            )
        }
        if (totalOwe > 0) {
            add(ManageAttention(ManageAttention.Kind.Balance, "Balance to settle", "You owe ${formatInr(totalOwe)} to ${plural(owe.size, "person", "people")}"))
        }
        if (pendingSwapsForMe + waitingSwaps > 0) {
            add(
                ManageAttention(
                    ManageAttention.Kind.Swaps,
                    if (pendingSwapsForMe + waitingSwaps == 1) "Swap request" else "Swap requests",
                    if (pendingSwapsForMe > 0) "${pendingSwapsForMe} waiting for your answer" else "Waiting on a flatmate",
                )
            )
        }
    }

    return ManageUiModel(
        flatContext = if (flatName.isNotBlank()) "$flatName · ${plural(memberCount, "member")}" else "",
        tasksLines = tasksLines,
        expensesLines = expensesLines,
        attention = attention,
        activity = activity.take(3).map { entry ->
            val who = if (entry.userId == uid) "You" else members.find { it.uid == entry.userId }?.nickname?.ifBlank { null } ?: "A flatmate"
            HomeActivityItem("$who ${entry.details}", formatTimeAgo(entry.timestamp))
        },
    )
}
