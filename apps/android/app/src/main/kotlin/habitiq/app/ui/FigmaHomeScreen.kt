package habitiq.app.ui

import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import habitiq.app.data.FlatTask
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.HomeFlatStatus
import habitiq.app.flats.HomeViewModel
import habitiq.app.flats.Member
import habitiq.app.flats.launchShareInviteCode
import habitiq.app.home.AdminHomeMode
import habitiq.app.home.HomeDashboardData
import habitiq.app.home.HomeDashboardStatus
import habitiq.app.home.HomeDashboardViewModel
import habitiq.app.lib.effectiveTaskStatus
import habitiq.app.lib.formatDueLabel
import habitiq.app.lib.formatInr
import habitiq.app.lib.formatTimeAgo
import habitiq.app.lib.formatWasDueLabel
import habitiq.app.lib.pairwisePersonalBalances
import habitiq.app.ui.components.HqEmptyState
import habitiq.app.ui.components.HqErrorState
import habitiq.app.ui.components.HqInlineLoading
import habitiq.app.ui.theme.LocalHqColors
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun FigmaHomeScreen(
    homeViewModel: HomeViewModel,
    dashboardViewModel: HomeDashboardViewModel,
    flatViewModel: FlatViewModel,
    // Kept so existing callers compile; Home no longer has an admin Tasks/Expenses toggle (design doc 3.1).
    adminHomeMode: AdminHomeMode,
    onAdminHomeModeChange: (AdminHomeMode) -> Unit,
    onOpenBills: () -> Unit = {},
    onStartOnboarding: () -> Unit = {},
    onOpenFlatSwitcher: () -> Unit = {},
    onOpenTasks: () -> Unit = {},
    onOpenTaskDetail: (String) -> Unit = {},
    onOpenExpenses: () -> Unit = {},
    onReviewJoinRequests: () -> Unit = {},
    onReviewSwapRequests: () -> Unit = {},
    onOpenDiscover: () -> Unit = {},
    onOpenMembers: () -> Unit = {},
    onOpenActivity: () -> Unit = {}
) {
    val flatStatus by homeViewModel.flatStatus.collectAsStateWithLifecycleCompat()
    val dashboardStatus by dashboardViewModel.status.collectAsStateWithLifecycleCompat()
    val c = LocalHqColors.current

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        when (flatStatus) {
            is HomeFlatStatus.Loading -> HomeLoading()
            is HomeFlatStatus.NoFlat -> NoFlatContent(onStartOnboarding)
            is HomeFlatStatus.Error -> HomeError((flatStatus as HomeFlatStatus.Error).message) {
                homeViewModel.checkFlatStatus(); dashboardViewModel.load()
            }
            is HomeFlatStatus.InFlat -> when (val dash = dashboardStatus) {
                is HomeDashboardStatus.Loading -> HomeLoading()
                is HomeDashboardStatus.Error -> HomeError(dash.message) { dashboardViewModel.load() }
                is HomeDashboardStatus.Ready -> HomeDashboard(
                    data = dash.data,
                    flatViewModel = flatViewModel,
                    onOpenFlatSwitcher = onOpenFlatSwitcher,
                    onOpenTasks = onOpenTasks,
                    onOpenTaskDetail = onOpenTaskDetail,
                    onOpenExpenses = onOpenExpenses,
                    onReviewJoinRequests = onReviewJoinRequests,
                    onReviewSwapRequests = onReviewSwapRequests,
                    onOpenDiscover = onOpenDiscover,
                    onOpenActivity = onOpenActivity,
                    onOpenMembers = onOpenMembers,
                )
                is HomeDashboardStatus.NoFlat -> NoFlatContent(onStartOnboarding)
            }
        }
    }
}

/** Resolves real repository data into [HomeUiModel] and wires each action to its existing flow. */
@Composable
private fun HomeDashboard(
    data: HomeDashboardData,
    flatViewModel: FlatViewModel,
    onOpenFlatSwitcher: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenTaskDetail: (String) -> Unit,
    onOpenExpenses: () -> Unit,
    onReviewJoinRequests: () -> Unit,
    onReviewSwapRequests: () -> Unit,
    onOpenDiscover: () -> Unit,
    onOpenActivity: () -> Unit,
    onOpenMembers: () -> Unit,
) {
    val context = LocalContext.current
    val joinRequests by flatViewModel.joinRequests.collectAsStateWithLifecycleCompat()
    val swapRequests by flatViewModel.swapRequests.collectAsStateWithLifecycleCompat()
    val expenses by flatViewModel.expenses.collectAsStateWithLifecycleCompat()
    val settlements by flatViewModel.settlements.collectAsStateWithLifecycleCompat()
    val uid = data.currentUid

    var completingTaskId by remember { mutableStateOf<String?>(null) }
    // The completion write is fire-and-forget, so hand the control back after a while if nothing changed.
    LaunchedEffect(completingTaskId) {
        if (completingTaskId != null) { delay(8_000); completingTaskId = null }
    }

    val prefs = remember { habitiq.app.settings.AppPreferences(context) }
    val seen by prefs.activitySeen(uid).collectAsState(initial = habitiq.app.settings.ActivitySeen(0L, emptySet()))
    val unread = habitiq.app.lib.unreadCount(data.activity, uid, seen)

    val model = remember(data, joinRequests, swapRequests, expenses, settlements, unread) {
        buildHomeModel(
            data = data,
            pendingJoins = if (data.isAdmin) joinRequests.count { it.status == "pending" } else 0,
            pendingSwapsForMe = swapRequests.count { it.status == "pending" && it.toUserId == uid },
            balances = pairwisePersonalBalances(expenses, settlements, uid),
        ).copy(unreadCount = unread)
    }

    HomeContent(
        model = model,
        completingTaskId = completingTaskId,
        onOpenFlatSwitcher = onOpenFlatSwitcher,
        onInvite = { launchShareInviteCode(context, data.flat.name, data.flat.id) },
        onOpenTasks = onOpenTasks,
        onOpenTask = onOpenTaskDetail,
        onCompleteTask = { id ->
            data.tasks.find { it.taskId == id }?.let { task ->
                completingTaskId = id
                flatViewModel.completeTask(task)
            }
        },
        onReviewPending = { kind ->
            when (kind) {
                HomePendingItem.Kind.JoinRequests -> onReviewJoinRequests()
                HomePendingItem.Kind.SwapRequests -> onReviewSwapRequests()
            }
        },
        onOpenExpenses = onOpenExpenses,
        onOpenActivity = onOpenActivity,
        onOpenDiscover = onOpenDiscover,
        onOpenMembers = onOpenMembers,
    )
}

internal fun buildHomeModel(
    data: HomeDashboardData,
    pendingJoins: Int,
    pendingSwapsForMe: Int,
    balances: Map<String, Double>,
): HomeUiModel {
    val uid = data.currentUid
    val mine = data.activeTasks
        .filter { it.currentAssignedUserId == uid }
        // Overdue responsibility first, then the soonest due date.
        .sortedWith(compareByDescending<FlatTask> { effectiveTaskStatus(it) == "overdue" }.thenBy { it.dueDate })
    val overdue = mine.count { effectiveTaskStatus(it) == "overdue" }
    val dueToday = mine.count { habitiq.app.lib.isDueToday(it.dueDate) && effectiveTaskStatus(it) != "overdue" }

    val headline = when {
        overdue > 0 -> "${plural(overdue, "task")} overdue."
        dueToday > 0 -> "${plural(dueToday, "task")} due today."
        mine.isNotEmpty() -> "${plural(mine.size, "task")} assigned to you."
        else -> "You're all caught up."
    }

    val pending = buildList {
        if (pendingJoins > 0) add(HomePendingItem(HomePendingItem.Kind.JoinRequests, "Join requests", "${plural(pendingJoins, "person", "people")} waiting for approval"))
        if (pendingSwapsForMe > 0) add(HomePendingItem(HomePendingItem.Kind.SwapRequests, "Swap requests", "${plural(pendingSwapsForMe, "request")} waiting for your answer"))
    }

    val owe = balances.filter { it.value < 0 }
    val owed = balances.filter { it.value > 0 }

    return HomeUiModel(
        flatName = data.flat.name,
        roleLabel = if (data.isAdmin) "Admin" else "Member",
        canInvite = true,
        greetingName = data.displayName,
        summaryHeadline = headline,
        assignedCount = mine.size,
        memberCount = data.memberCount,
        tasks = mine.take(3).map { task ->
            val isOverdue = effectiveTaskStatus(task) == "overdue"
            HomeTaskItem(
                id = task.taskId,
                name = task.name,
                assigneeText = "You",
                dueText = if (isOverdue) formatWasDueLabel(task.dueDate) else formatDueLabel(task.dueDate),
                overdue = isOverdue,
                canComplete = true,
                kind = when (task.type) {
                    "group_duty" -> HomeTaskKind.Group
                    "temp", "temp_task", "one_time" -> HomeTaskKind.OneOff
                    else -> HomeTaskKind.Rotating
                },
            )
        },
        pending = pending,
        balance = HomeBalance(
            owe = owe.values.sumOf { abs(it) },
            owed = owed.values.sum(),
            oweCount = owe.size,
            owedCount = owed.size,
        ),
        balanceText = { formatInr(it) },
        activity = data.activity.take(3).map { entry ->
            HomeActivityItem("${memberName(data.members, entry.userId, uid)} ${entry.details}", formatTimeAgo(entry.timestamp))
        },
    )
}

private fun plural(n: Int, singular: String, pluralForm: String = "${singular}s") = "$n ${if (n == 1) singular else pluralForm}"

private fun memberName(members: List<Member>, uid: String, currentUid: String) =
    if (uid == currentUid) "You" else members.find { it.uid == uid }?.nickname?.ifBlank { "A flatmate" } ?: "A flatmate"

@Composable
private fun NoFlatContent(onStartOnboarding: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        HqEmptyState(
            icon = Icons.Filled.Home,
            title = "Let's get you set up",
            message = "Tell us what you want to do first: manage a flat, find a flatmate, or join with a code.",
            primaryLabel = "Continue",
            onPrimaryClick = onStartOnboarding
        )
    }
}

@Composable private fun HomeLoading() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { HqInlineLoading() } }

@Composable
private fun HomeError(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        HqErrorState(message = message, onRetry = onRetry)
    }
}
