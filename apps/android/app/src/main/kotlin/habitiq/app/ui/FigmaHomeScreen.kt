package habitiq.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import habitiq.app.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import habitiq.app.data.FlatTask
import habitiq.app.flats.HomeFlatStatus
import habitiq.app.flats.HomeViewModel
import habitiq.app.flats.Member
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.launchShareInviteCode
import habitiq.app.home.*
import habitiq.app.lib.effectiveTaskStatus
import habitiq.app.lib.formatDueLabel
import habitiq.app.ui.components.HqAvatar
import habitiq.app.ui.components.HqAvatarSize
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.components.HqEmptyState
import habitiq.app.ui.components.HqErrorState
import habitiq.app.ui.components.HqInlineLoading
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.HqFadeUp
import habitiq.app.ui.theme.LocalHqColors
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun FigmaHomeScreen(
    homeViewModel: HomeViewModel,
    dashboardViewModel: HomeDashboardViewModel,
    flatViewModel: FlatViewModel,
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
    val viewMode by dashboardViewModel.viewMode.collectAsStateWithLifecycleCompat()
    val c = LocalHqColors.current

    Column(Modifier.fillMaxSize().background(c.background)) {
        HomeTopHeader()
        when (flatStatus) {
            is HomeFlatStatus.Loading -> HomeLoading()
            is HomeFlatStatus.NoFlat -> NoFlatContent(onStartOnboarding)
            is HomeFlatStatus.Error -> HomeError((flatStatus as HomeFlatStatus.Error).message) {
                homeViewModel.checkFlatStatus(); dashboardViewModel.load()
            }
            is HomeFlatStatus.InFlat -> when (val dash = dashboardStatus) {
                is HomeDashboardStatus.Loading -> HomeLoading()
                is HomeDashboardStatus.Error -> HomeError(dash.message) { dashboardViewModel.load() }
                is HomeDashboardStatus.Ready -> DashboardContent(
                    dash.data,
                    viewMode,
                    dashboardViewModel::setViewMode,
                    adminHomeMode,
                    onAdminHomeModeChange,
                    flatViewModel,
                    onOpenFlatSwitcher,
                    onOpenBills,
                    onOpenTasks,
                    onOpenTaskDetail,
                    onOpenExpenses,
                    onReviewJoinRequests,
                    onReviewSwapRequests,
                    onOpenDiscover,
                    onOpenMembers,
                    onOpenActivity
                )
                is HomeDashboardStatus.NoFlat -> NoFlatContent(onStartOnboarding)
            }
        }
    }
}

@Composable
private fun HomeTopHeader() {
    val c = LocalHqColors.current
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().background(c.background.copy(alpha = 0.95f)).padding(horizontal = HqSpacing.xl, vertical = HqSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.Top) {
                Text("Habitiq", style = HqType.headlineSmall, color = c.brandPrimary)
                Text("+", style = HqType.labelLarge, color = c.brandPrimary, modifier = Modifier.padding(top = 2.dp))
            }
        }
        Text("Home", style = HqType.labelLarge, color = c.textSecondary)
    }
}

@Composable
private fun DashboardContent(
    data: HomeDashboardData,
    viewMode: HomeViewMode,
    onViewModeChange: (HomeViewMode) -> Unit,
    adminHomeMode: AdminHomeMode,
    onAdminHomeModeChange: (AdminHomeMode) -> Unit,
    flatViewModel: FlatViewModel,
    onOpenFlatSwitcher: () -> Unit,
    onOpenBills: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenTaskDetail: (String) -> Unit,
    onOpenExpenses: () -> Unit,
    onReviewJoinRequests: () -> Unit,
    onReviewSwapRequests: () -> Unit,
    onOpenDiscover: () -> Unit,
    onOpenMembers: () -> Unit,
    onOpenActivity: () -> Unit
) {
    val c = LocalHqColors.current
    val context = LocalContext.current
    val joinPending by flatViewModel.joinRequests.collectAsStateWithLifecycleCompat()
    val swapPending by flatViewModel.swapRequests.collectAsStateWithLifecycleCompat()
    val expenses by flatViewModel.expenses.collectAsStateWithLifecycleCompat()
    val pendingJoins = joinPending.count { it.status == "pending" }
    val pendingSwaps = swapPending.count { it.status == "pending" }
    val overdue = data.tasks.filter { effectiveTaskStatus(it) == "overdue" }
    val settled = expenses.isEmpty() || data.monthlySpent == 0.0
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.xl)) {
        Text("habitiq", style = HqType.labelLarge, color = c.brandPrimary)
        Text(data.flat.name, style = HqType.titleMedium, color = c.textSecondary, modifier = Modifier.clickable(onClick = onOpenFlatSwitcher).padding(bottom = HqSpacing.md))
        HqFadeUp(0) { HomePhotoCard(data.flat.name, data.isAdmin) { launchShareInviteCode(context, data.flat.name, data.flat.id) } }
        Spacer(Modifier.height(HqSpacing.lg))
        Text("${greetingForHour()}, ${data.displayName}", style = HqType.headlineMedium, color = c.textPrimary)
        Text(
            if (overdue.isEmpty()) "Your flat is running smoothly." else "${overdue.size} task${if (overdue.size == 1) "" else "s"} still open.",
            style = HqType.bodyMedium,
            color = c.textSecondary,
            modifier = Modifier.padding(top = HqSpacing.xs, bottom = HqSpacing.md)
        )
        if (overdue.isNotEmpty()) {
            Spacer(Modifier.height(HqSpacing.md))
            HqFadeUp(1) {
                OverviewRow(
                    icon = Icons.Filled.Warning,
                    iconBg = c.errorContainer,
                    iconTint = c.error,
                    title = "${overdue.size} overdue task${if (overdue.size == 1) "" else "s"}",
                    subtitle = overdue.first().name,
                    onClick = { onOpenTaskDetail(overdue.first().taskId) }
                )
            }
        }
        Spacer(Modifier.height(HqSpacing.lg))
        HqFadeUp(2) {
            OverviewRow(
                Icons.Filled.CheckCircle,
                c.brandPrimaryContainer,
                c.brandPrimary,
                "Tasks",
                if (overdue.isEmpty()) "${data.activeTasks.size} open" else "${data.activeTasks.size} assigned · ${overdue.size} overdue",
                onOpenTasks
            )
        }
        HqFadeUp(3) {
            OverviewRow(Icons.Filled.Receipt, c.successContainer, c.success, "Expenses", if (settled) "All balances settled" else "₹${data.monthlySpent.toInt()} this month", onOpenExpenses)
        }
        HqFadeUp(4) {
            OverviewRow(
                Icons.Filled.Group,
                c.surfaceSubtle,
                c.textSecondary,
                "Members",
                "${data.memberCount} member${if (data.memberCount == 1) "" else "s"}" + if (data.isAdmin && pendingJoins > 0) " · $pendingJoins waiting" else "",
                onOpenMembers
            )
        }
        HqFadeUp(5) {
            OverviewRow(Icons.Filled.History, c.surfaceSubtle, c.textSecondary, "Activity", "Recent household activity", onOpenActivity)
        }
        if (data.isAdmin && pendingSwaps > 0) {
            OverviewRow(Icons.Filled.SwapHoriz, c.warningContainer, c.warning, "Swap requests", "$pendingSwaps waiting", onReviewSwapRequests)
        }
        if (viewMode == HomeViewMode.MY) {
            HqTextButton(text = "Show the whole flat", onClick = { onViewModeChange(HomeViewMode.FLAT) })
        } else {
            HqTextButton(text = "Show only my tasks", onClick = { onViewModeChange(HomeViewMode.MY) })
        }
        HqTextButton(text = "Discover a room or a flatmate", onClick = onOpenDiscover)
        if (adminHomeMode == AdminHomeMode.EXPENSES) {
            HqTextButton(text = "Open bills", onClick = onOpenBills)
        }
        Spacer(Modifier.height(HqSpacing.xxxl))
    }
}

@Composable
private fun HomePhotoCard(flatName: String, isAdmin: Boolean, onInvite: () -> Unit) {
    val c = LocalHqColors.current
    Box(
        Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(HqRadius.lg)).clickable(onClick = onInvite)
    ) {
        Image(
            painter = painterResource(R.drawable.onboard_create),
            contentDescription = flatName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)))
            )
        )
        Text(flatName, style = HqType.headlineSmall, color = Color.White, modifier = Modifier.align(Alignment.BottomStart).padding(HqSpacing.lg))
        Row(
            Modifier.align(Alignment.TopEnd).padding(HqSpacing.md).background(c.surface, RoundedCornerShape(HqRadius.full)).padding(horizontal = HqSpacing.md, vertical = HqSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).background(c.success, CircleShape))
            Spacer(Modifier.width(HqSpacing.xs))
            Text(if (isAdmin) "At Flat" else "Home", style = HqType.labelMedium, color = c.textPrimary)
        }
    }
}

@Composable
private fun OverviewRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val c = LocalHqColors.current
    Row(
        Modifier.fillMaxWidth().padding(bottom = HqSpacing.sm)
            .clip(RoundedCornerShape(HqRadius.lg))
            .background(c.surface)
            .border(1.dp, c.borderDefault, RoundedCornerShape(HqRadius.lg))
            .clickable(onClick = onClick)
            .padding(HqSpacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(HqRadius.md)).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(HqIconSize.sm))
        }
        Column(Modifier.weight(1f).padding(horizontal = HqSpacing.md)) {
            Text(title, style = HqType.titleMedium, color = c.textPrimary)
            Text(subtitle, style = HqType.bodySmall, color = c.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = c.textTertiary)
    }
}

@Composable
private fun FlatSelectorRow(flatName: String, isAdmin: Boolean, onFlatClick: () -> Unit, onInvite: () -> Unit) {
    val c = LocalHqColors.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Column(Modifier.clickable(onClick = onFlatClick)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(flatName, style = HqType.headlineSmall, color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(Icons.Filled.KeyboardArrowDown, null, tint = c.textTertiary, modifier = Modifier.padding(start = HqSpacing.xs))
            }
            if (isAdmin) {
                Spacer(Modifier.height(HqSpacing.xs))
                Row(Modifier.background(c.brandPrimary, RoundedCornerShape(HqRadius.sm)).padding(horizontal = HqSpacing.sm, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Shield, null, tint = c.textOnBrand, modifier = Modifier.size(HqIconSize.xs))
                    Spacer(Modifier.width(HqSpacing.xs))
                    Text("Admin", style = HqType.labelMedium, color = c.textOnBrand)
                }
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            Row(
                Modifier.background(c.successContainer, RoundedCornerShape(HqRadius.full))
                    .border(1.dp, c.success.copy(alpha = 0.4f), RoundedCornerShape(HqRadius.full))
                    .padding(horizontal = HqSpacing.md, vertical = HqSpacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(8.dp).background(c.success, CircleShape))
                Spacer(Modifier.width(HqSpacing.xs))
                Text("At Flat", style = HqType.labelMedium, color = c.success)
                Spacer(Modifier.width(HqSpacing.xs))
                Icon(Icons.Filled.Check, null, tint = c.success, modifier = Modifier.size(10.dp))
            }
            HqButton(text = "Invite", onClick = onInvite, variant = HqButtonVariant.Secondary, fullWidth = false, leadingIcon = Icons.Filled.PersonAdd)
        }
    }
}

@Composable
private fun ViewModeToggle(viewMode: HomeViewMode, onViewModeChange: (HomeViewMode) -> Unit) {
    val c = LocalHqColors.current
    Row(Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(HqRadius.lg)).border(1.dp, c.borderDefault, RoundedCornerShape(HqRadius.lg)).padding(5.dp)) {
        ViewModeChip("Flat View", "Overview of entire flat", viewMode == HomeViewMode.FLAT, { onViewModeChange(HomeViewMode.FLAT) }, Modifier.weight(1f))
        ViewModeChip("My View", "Only my tasks & data", viewMode == HomeViewMode.MY, { onViewModeChange(HomeViewMode.MY) }, Modifier.weight(1f))
    }
}

@Composable
private fun ViewModeChip(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val c = LocalHqColors.current
    val bg = if (selected) c.brandPrimary else Color.Transparent
    Column(modifier.clip(RoundedCornerShape(HqRadius.md)).background(bg).clickable(onClick = onClick).padding(vertical = HqSpacing.sm, horizontal = HqSpacing.sm), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = HqType.titleSmall, color = if (selected) c.textOnBrand else c.textPrimary)
        Text(subtitle, style = HqType.caption, color = if (selected) c.textOnBrand.copy(alpha = 0.8f) else c.textTertiary)
    }
}

@Composable
private fun AdminManageToggle(mode: AdminHomeMode, onModeChange: (AdminHomeMode) -> Unit) {
    val c = LocalHqColors.current
    Row(
        Modifier.fillMaxWidth()
            .background(c.surface, RoundedCornerShape(HqRadius.lg))
            .border(1.dp, c.borderDefault, RoundedCornerShape(HqRadius.lg))
            .padding(5.dp)
    ) {
        AdminModeChip("Manage Tasks", mode == AdminHomeMode.TASKS, { onModeChange(AdminHomeMode.TASKS) }, Modifier.weight(1f))
        AdminModeChip("Expenses", mode == AdminHomeMode.EXPENSES, { onModeChange(AdminHomeMode.EXPENSES) }, Modifier.weight(1f))
    }
}

@Composable
private fun AdminModeChip(title: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val c = LocalHqColors.current
    val bg = if (selected) c.info else Color.Transparent
    Column(
        modifier.clip(RoundedCornerShape(HqRadius.md)).background(bg).clickable(onClick = onClick)
            .padding(vertical = HqSpacing.sm, horizontal = HqSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = HqType.titleSmall, color = if (selected) c.textOnBrand else c.textPrimary)
    }
}

@Composable
private fun AdminExpensesSection(
    monthlySpent: Double,
    expenses: List<habitiq.app.data.FlatExpense>,
    members: List<Member>,
    onOpenBills: () -> Unit,
    onAddExpense: () -> Unit
) {
    val c = LocalHqColors.current
    SectionHeader("Expenses", null)
    Spacer(Modifier.height(HqSpacing.md))
    ExpensesOverviewCard(monthlySpent)
    Spacer(Modifier.height(HqSpacing.md))
    Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
        HqButton(text = "Add expense", onClick = onAddExpense, modifier = Modifier.weight(1f))
        HqButton(text = "Bills & settle", onClick = onOpenBills, variant = HqButtonVariant.Secondary, modifier = Modifier.weight(1f))
    }
    if (expenses.isNotEmpty()) {
        Spacer(Modifier.height(HqSpacing.lg))
        Text("Recent", style = HqType.labelLarge, color = c.textSecondary)
        Spacer(Modifier.height(HqSpacing.sm))
        expenses.forEach { expense ->
            val payer = members.find { it.uid == expense.paidBy }?.nickname ?: "Member"
            HqCard(modifier = Modifier.padding(bottom = HqSpacing.sm), padding = HqSpacing.md) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(expense.description, style = HqType.titleSmall, color = c.textPrimary)
                        Text("Paid by $payer", style = HqType.bodySmall, color = c.textSecondary)
                    }
                    Text("₹${expense.amount.toInt()}", style = HqType.titleSmall, color = c.textPrimary)
                }
            }
        }
    }
}

@Composable
private fun WelcomeCard(greeting: String, displayName: String) {
    val c = LocalHqColors.current
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(HqRadius.xl)).background(c.brandPrimaryContainer).padding(HqSpacing.xl)) {
        Column {
            Text("$greeting, $displayName", style = HqType.titleLarge, color = c.textPrimary)
            Text("Your next household actions are below.", style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(top = HqSpacing.xs))
        }
    }
}

@Composable
private fun TodaysTasksSection(
    tasks: List<FlatTask>,
    members: List<Member>,
    currentUid: String,
    weeklyProgress: Int,
    completedCount: Int,
    onOpenTasks: () -> Unit,
    onOpenTaskDetail: (String) -> Unit
) {
    val c = LocalHqColors.current
    SectionHeader("Today's Tasks", "View all", onOpenTasks)
    Spacer(Modifier.height(HqSpacing.md))
    if (tasks.isEmpty()) Text("No tasks due today. Enjoy the break!", style = HqType.bodyMedium, color = c.textSecondary)
    else tasks.take(3).forEach { task ->
        TaskRow(task, memberName(members, task.currentAssignedUserId, currentUid)) { onOpenTaskDetail(task.taskId) }
        Spacer(Modifier.height(HqSpacing.sm))
    }
    Spacer(Modifier.height(HqSpacing.md))
    HqCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { weeklyProgress / 100f }, modifier = Modifier.size(56.dp), color = c.brandPrimary, trackColor = c.borderDefault, strokeWidth = 5.dp)
                Text("$weeklyProgress%", style = HqType.labelMedium, color = c.brandPrimary)
            }
            Spacer(Modifier.width(HqSpacing.lg))
            Column {
                Text("$completedCount tasks completed this week", style = HqType.titleSmall, color = c.textPrimary)
                Text("Weekly household progress", style = HqType.bodySmall, color = c.textSecondary)
            }
        }
    }
}

@Composable
private fun TaskRow(task: FlatTask, assigneeName: String, onClick: () -> Unit) {
    val c = LocalHqColors.current
    val dueLabel = formatDueLabel(task.dueDate)
    HqCard(variant = HqCardVariant.Interactive, onClick = onClick, padding = HqSpacing.md) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CheckCircleOutline, null, tint = c.textTertiary, modifier = Modifier.size(HqIconSize.md))
            Spacer(Modifier.width(HqSpacing.md))
            Column(Modifier.weight(1f)) {
                Text(task.name, style = HqType.titleSmall, color = c.textPrimary)
                Text(assigneeName, style = HqType.bodySmall, color = c.textSecondary)
            }
            Text(dueLabel, style = HqType.labelSmall, color = if (dueLabel == "Due Today") c.error else c.textTertiary)
        }
    }
}

@Composable
private fun PendingRequestsSection(joinCount: Int, swapCount: Int, onReviewJoins: () -> Unit, onReviewSwaps: () -> Unit) {
    val c = LocalHqColors.current
    SectionHeader("Pending", null)
    Spacer(Modifier.height(HqSpacing.md))
    Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
        RequestCard("Join\nRequests", joinCount, c.brandPrimary, onReviewJoins, Modifier.weight(1f))
        RequestCard("Swap\nRequests", swapCount, c.info, onReviewSwaps, Modifier.weight(1f))
    }
}

@Composable
private fun RequestCard(title: String, count: Int, accent: Color, onClick: () -> Unit, modifier: Modifier) {
    val c = LocalHqColors.current
    HqCard(modifier = modifier, variant = HqCardVariant.Interactive, onClick = onClick, padding = HqSpacing.md) {
        Text(count.toString(), style = HqType.headlineSmall, color = accent)
        Text(title, style = HqType.labelMedium, color = c.textPrimary)
        Text(if (count > 0) "Review" else "Nothing pending", style = HqType.labelSmall, color = if (count > 0) accent else c.textTertiary, modifier = Modifier.padding(top = HqSpacing.xs))
    }
}

@Composable
private fun ExpensesOverviewCard(monthlySpent: Double, onOpenExpenses: (() -> Unit)? = null) {
    val c = LocalHqColors.current
    SectionHeader("Expenses Overview", null)
    Spacer(Modifier.height(HqSpacing.md))
    HqCard(variant = if (onOpenExpenses != null) HqCardVariant.Interactive else HqCardVariant.Standard, onClick = onOpenExpenses) {
        Text("You've spent ₹${"%.0f".format(monthlySpent)} this month", style = HqType.titleSmall, color = c.textPrimary)
        Text("Settlements and bills sync from your flat.", style = HqType.bodySmall, color = c.textSecondary, modifier = Modifier.padding(top = HqSpacing.xs))
    }
}

@Composable
private fun RecentActivitySection(activity: List<habitiq.app.data.FlatActivity>, members: List<Member>, currentUid: String) {
    val c = LocalHqColors.current
    SectionHeader("Recent Activity", null)
    Spacer(Modifier.height(HqSpacing.md))
    if (activity.isEmpty()) Text("No recent activity yet.", style = HqType.bodyMedium, color = c.textSecondary)
    else activity.take(5).forEach { item ->
        Row(Modifier.fillMaxWidth().padding(vertical = HqSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(c.brandPrimary, CircleShape))
            Spacer(Modifier.width(HqSpacing.md))
            Column {
                Text(item.details.ifBlank { "${memberName(members, item.userId, currentUid)} — ${item.action}" }, style = HqType.bodySmall, color = c.textPrimary)
                Text(formatTimeAgo(item.timestamp), style = HqType.labelSmall, color = c.textTertiary)
            }
        }
    }
}

@Composable
private fun DiscoverTeaserSection(onOpenDiscover: () -> Unit) {
    val c = LocalHqColors.current
    SectionHeader("Discovery", null)
    Spacer(Modifier.height(HqSpacing.md))
    HqCard(variant = HqCardVariant.Interactive, onClick = onOpenDiscover, statusTint = c.brandPrimaryContainer) {
        Text("Find a place, or the right people", style = HqType.titleSmall, color = c.textPrimary)
        Text(
            "Open Discover to browse real vacancies and looking posts. Listings show approximate area only.",
            style = HqType.bodySmall,
            color = c.textSecondary,
            modifier = Modifier.padding(top = HqSpacing.xs)
        )
    }
}

@Composable
private fun SectionHeader(title: String, action: String?, onAction: (() -> Unit)? = null) {
    val c = LocalHqColors.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = HqType.titleLarge, color = c.textPrimary)
        if (action != null && onAction != null) HqTextButton(action, onAction)
    }
}

@Composable
private fun NoFlatContent(onStartOnboarding: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        HqEmptyState(
            icon = Icons.Filled.Home,
            title = "Let's get you set up",
            message = "Tell us what you want to do first — manage a flat, find a flatmate, or join with a code.",
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

private fun greetingForHour() = when (LocalDateTime.now().hour) { in 0..11 -> "Good morning"; in 12..16 -> "Good afternoon"; else -> "Good evening" }
private fun memberName(members: List<Member>, uid: String, currentUid: String) = if (uid == currentUid) "You" else members.find { it.uid == uid }?.nickname?.ifBlank { "Roommate" } ?: "Roommate"
private fun formatTimeAgo(timestamp: String) = runCatching {
    val dt = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME)
    val hours = ChronoUnit.HOURS.between(dt, LocalDateTime.now())
    when { hours < 1 -> "Just now"; hours < 24 -> "$hours hours ago"; else -> "${ChronoUnit.DAYS.between(dt.toLocalDate(), LocalDate.now())} days ago" }
}.getOrDefault("Recently")
