package habitiq.app.ui

import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import habitiq.app.ui.components.HqArt
import habitiq.app.ui.components.HqGroup
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqIllustration
import habitiq.app.ui.components.HqMenuRow
import habitiq.app.ui.components.HqTileTone
import habitiq.app.ui.components.HqWordmark
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
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
    onOpenActivity: () -> Unit = {},
    onAddTask: () -> Unit = {},
    onAddExpense: () -> Unit = {},
    onAddBill: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onReviewVacancies: () -> Unit = {},
    // No-flat Home shows the four start choices. Until the caller wires each one, they fall back to the
    // first-choice screen (create / join) and Discover (find a flat / find a flatmate).
    onCreateFlat: () -> Unit = onStartOnboarding,
    onJoinFlat: () -> Unit = onStartOnboarding,
    onFindFlat: () -> Unit = onOpenDiscover,
    onFindFlatmate: () -> Unit = onOpenDiscover,
) {
    val flatStatus by homeViewModel.flatStatus.collectAsStateWithLifecycleCompat()
    val dashboardStatus by dashboardViewModel.status.collectAsStateWithLifecycleCompat()
    val c = LocalHqColors.current

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        when (flatStatus) {
            is HomeFlatStatus.Loading -> HomeLoading()
            is HomeFlatStatus.NoFlat -> NoFlatContent(onCreateFlat, onJoinFlat, onFindFlat, onFindFlatmate)
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
                    onAddTask = onAddTask,
                    onAddExpense = onAddExpense,
                    onAddBill = onAddBill,
                    onOpenProfile = onOpenProfile,
                    onReviewVacancies = onReviewVacancies,
                )
                is HomeDashboardStatus.NoFlat -> NoFlatContent(onCreateFlat, onJoinFlat, onFindFlat, onFindFlatmate)
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
    onAddTask: () -> Unit,
    onAddExpense: () -> Unit,
    onAddBill: () -> Unit,
    onOpenProfile: () -> Unit,
    onReviewVacancies: () -> Unit,
) {
    val context = LocalContext.current
    val joinRequests by flatViewModel.joinRequests.collectAsStateWithLifecycleCompat()
    val swapRequests by flatViewModel.swapRequests.collectAsStateWithLifecycleCompat()
    val expenses by flatViewModel.expenses.collectAsStateWithLifecycleCompat()
    val settlements by flatViewModel.settlements.collectAsStateWithLifecycleCompat()
    val vacancyRequests by flatViewModel.vacancyRequests.collectAsStateWithLifecycleCompat()
    val uid = data.currentUid

    var completingTaskId by remember { mutableStateOf<String?>(null) }
    // The completion write is fire-and-forget, so hand the control back after a while if nothing changed.
    LaunchedEffect(completingTaskId) {
        if (completingTaskId != null) { delay(8_000); completingTaskId = null }
    }

    val prefs = remember { habitiq.app.settings.AppPreferences(context) }
    val seen by prefs.activitySeen(uid).collectAsState(initial = habitiq.app.settings.ActivitySeen(0L, emptySet()))
    val unread = habitiq.app.lib.unreadCount(data.activity, uid, seen)

    val model = remember(data, joinRequests, swapRequests, expenses, settlements, unread, vacancyRequests) {
        buildHomeModel(
            data = data,
            pendingJoins = if (data.isAdmin) joinRequests.count { it.status == "pending" } else 0,
            pendingSwapsForMe = swapRequests.count { it.status == "pending" && it.toUserId == uid },
            balances = pairwisePersonalBalances(expenses, settlements, uid),
            pendingVacancies = if (data.isAdmin) vacancyRequests.size else 0,
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
                HomePendingItem.Kind.VacancyRequests -> onReviewVacancies()
            }
        },
        onOpenExpenses = onOpenExpenses,
        onOpenActivity = onOpenActivity,
        onOpenDiscover = onOpenDiscover,
        onOpenMembers = onOpenMembers,
        onAddTask = onAddTask,
        onAddExpense = onAddExpense,
        onAddBill = onAddBill,
        onOpenProfile = onOpenProfile,
    )
}

internal fun buildHomeModel(
    data: HomeDashboardData,
    pendingJoins: Int,
    pendingSwapsForMe: Int,
    balances: Map<String, Double>,
    pendingVacancies: Int = 0,
): HomeUiModel {
    val uid = data.currentUid
    val mine = data.activeTasks
        .filter { it.currentAssignedUserId == uid }
        // Overdue responsibility first, then the soonest due date.
        .sortedWith(compareByDescending<FlatTask> { effectiveTaskStatus(it) == "overdue" }.thenBy { it.dueDate })
    val overdue = mine.count { effectiveTaskStatus(it) == "overdue" }
    val dueToday = mine.count { habitiq.app.lib.isDueToday(it.dueDate) && effectiveTaskStatus(it) != "overdue" }

    val headline = when {
        overdue > 0 -> "You have ${plural(overdue, "overdue task")}."
        dueToday > 0 -> "You have ${plural(dueToday, "task")} due today."
        mine.isNotEmpty() -> "You have ${plural(mine.size, "task")} coming up."
        else -> "You're all caught up."
    }

    val pending = buildList {
        if (pendingJoins > 0) add(HomePendingItem(HomePendingItem.Kind.JoinRequests, "Join requests", "${plural(pendingJoins, "person", "people")} asked to join your flat"))
        if (pendingSwapsForMe > 0) add(HomePendingItem(HomePendingItem.Kind.SwapRequests, "Swap turn", "${plural(pendingSwapsForMe, "flatmate")} asked to swap a turn with you"))
        if (pendingVacancies > 0) add(HomePendingItem(HomePendingItem.Kind.VacancyRequests, "Room listing to review", "${plural(pendingVacancies, "flatmate")} listed a room in Discover"))
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
        memberNames = data.members.sortedByDescending { it.uid == uid }.map { it.nickname },
        isAdmin = data.isAdmin,
        activity = data.activity.take(3).map { entry ->
            HomeActivityItem("${memberName(data.members, entry.userId, uid)} ${entry.details}", formatTimeAgo(entry.timestamp))
        },
    )
}

private fun plural(n: Int, singular: String, pluralForm: String = "${singular}s") = "$n ${if (n == 1) singular else pluralForm}"

private fun memberName(members: List<Member>, uid: String, currentUid: String) =
    if (uid == currentUid) "You" else members.find { it.uid == uid }?.nickname?.ifBlank { "A flatmate" } ?: "A flatmate"

/**
 * Home before the person belongs to a flat: the wordmark, what Oddroof is, and the same four choices and copy as
 * the first-choice screen ([StartChooser]), so there is one way to say it everywhere.
 */
@Composable
internal fun NoFlatContent(
    onCreateFlat: () -> Unit,
    onJoinFlat: () -> Unit,
    onFindFlat: () -> Unit,
    onFindFlatmate: () -> Unit,
) {
    val c = LocalHqColors.current
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HqSpacing.screenHorizontal)
            .padding(top = HqSpacing.xl, bottom = HqSpacing.fabClearance),
    ) {
        HqWordmark(height = 28.dp)
        Text(
            OddroofValueSentence,
            style = HqType.bodyMedium,
            color = c.textSecondary,
            modifier = Modifier.padding(top = HqSpacing.lg, bottom = HqSpacing.xxl),
        )
        StartChooser(
            onFindFlat = onFindFlat,
            onFindPerson = onFindFlatmate,
            onCreateFlat = onCreateFlat,
            onJoinFlat = onJoinFlat,
        )
    }
}

/** Skeleton shaped like Home (hero, section title, two task rows, a card), not a bare spinner. */
@Composable
private fun HomeLoading() {
    val c = LocalHqColors.current
    Column(Modifier.fillMaxSize().semantics { contentDescription = "Loading your flat" }) {
        habitiq.app.ui.components.HqSkeletonBlock(Modifier.fillMaxWidth(), height = 380.dp)
        Column(Modifier.padding(horizontal = HqSpacing.screenHorizontal).padding(top = 28.dp), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp)) {
            habitiq.app.ui.components.HqSkeletonBlock(Modifier.fillMaxWidth(0.45f), height = 20.dp)
            habitiq.app.ui.components.HqSkeletonBlock(Modifier.fillMaxWidth(), height = 68.dp)
            habitiq.app.ui.components.HqSkeletonBlock(Modifier.fillMaxWidth(), height = 68.dp)
            habitiq.app.ui.components.HqSkeletonBlock(Modifier.fillMaxWidth(), height = 120.dp)
        }
    }
}

@Composable
private fun HomeError(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        HqErrorState(message = message, onRetry = onRetry)
    }
}
