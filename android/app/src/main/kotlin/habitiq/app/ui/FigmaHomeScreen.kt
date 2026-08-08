package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser
import habitiq.app.flats.HomeFlatStatus
import habitiq.app.flats.HomeViewModel
import habitiq.app.flats.Member
import habitiq.app.flats.launchShareInviteCode
import habitiq.app.home.HomeDashboardData
import habitiq.app.home.HomeDashboardStatus
import habitiq.app.home.HomeDashboardViewModel
import habitiq.app.home.HomeViewMode
import habitiq.app.ui.theme.FigmaColors
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.min

@Composable
fun FigmaHomeScreen(
    user: FirebaseUser?,
    homeViewModel: HomeViewModel,
    dashboardViewModel: HomeDashboardViewModel,
    onCreateFlat: () -> Unit,
    onJoinFlat: () -> Unit
) {
    val flatStatus by homeViewModel.flatStatus.collectAsStateWithLifecycleCompat()
    val dashboardStatus by dashboardViewModel.status.collectAsStateWithLifecycleCompat()
    val viewMode by dashboardViewModel.viewMode.collectAsStateWithLifecycleCompat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FigmaColors.Background)
    ) {
        HomeTopHeader(user = user)
        when (flatStatus) {
            is HomeFlatStatus.Loading -> HomeLoading()
            is HomeFlatStatus.NoFlat -> NoFlatContent(onCreateFlat, onJoinFlat)
            is HomeFlatStatus.Error -> HomeError(
                message = (flatStatus as HomeFlatStatus.Error).message,
                onRetry = { homeViewModel.checkFlatStatus(); dashboardViewModel.load() }
            )
            is HomeFlatStatus.InFlat -> when (val dash = dashboardStatus) {
                is HomeDashboardStatus.Loading -> HomeLoading()
                is HomeDashboardStatus.Error -> HomeError(
                    message = dash.message,
                    onRetry = { dashboardViewModel.load() }
                )
                is HomeDashboardStatus.Ready -> DashboardContent(
                    data = dash.data,
                    viewMode = viewMode,
                    onViewModeChange = dashboardViewModel::setViewMode
                )
                is HomeDashboardStatus.NoFlat -> NoFlatContent(onCreateFlat, onJoinFlat)
            }
        }
    }
}

@Composable
private fun HomeTopHeader(user: FirebaseUser?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(FigmaColors.Background.copy(alpha = 0.95f))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Menu",
                tint = FigmaColors.Ink,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 12.dp)
            )
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    "Habitiq",
                    color = FigmaColors.Primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    "+",
                    color = FigmaColors.Primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = FigmaColors.InkSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(16.dp)
                        .background(FigmaColors.Error, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("2", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            val initial = user?.displayName?.firstOrNull()?.uppercaseChar()
                ?: user?.email?.firstOrNull()?.uppercaseChar()
                ?: '?'
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(FigmaColors.Primary.copy(alpha = 0.15f))
                    .border(1.dp, FigmaColors.SurfaceBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(initial.toString(), color = FigmaColors.Primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DashboardContent(
    data: HomeDashboardData,
    viewMode: HomeViewMode,
    onViewModeChange: (HomeViewMode) -> Unit
) {
    val context = LocalContext.current
    val scroll = rememberScrollState()
    val greeting = greetingForHour()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        FlatSelectorRow(
            flatName = data.flat.name,
            isAdmin = data.isAdmin,
            onInvite = { launchShareInviteCode(context, data.flat.name, data.flat.id) }
        )
        Spacer(Modifier.height(12.dp))
        ViewModeToggle(viewMode = viewMode, onViewModeChange = onViewModeChange)
        Spacer(Modifier.height(16.dp))
        WelcomeCard(
            greeting = greeting,
            displayName = data.displayName,
            memberCount = data.memberCount,
            tasksToday = data.tasksTodayCount,
            monthlySpent = data.monthlySpent,
            flatHealth = computeFlatHealth(data)
        )
        Spacer(Modifier.height(20.dp))
        TodaysTasksSection(
            tasks = data.todaysTasks,
            members = data.members,
            currentUid = data.currentUid,
            weeklyProgress = data.weeklyProgress,
            completedCount = data.completedThisWeek
        )
        if (data.isAdmin) {
            Spacer(Modifier.height(20.dp))
            PendingRequestsSection()
        }
        Spacer(Modifier.height(20.dp))
        ExpensesOverviewCard(monthlySpent = data.monthlySpent)
        Spacer(Modifier.height(20.dp))
        RecentActivitySection(
            activity = data.activity,
            members = data.members
        )
        Spacer(Modifier.height(20.dp))
        DiscoverTeaserSection()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FlatSelectorRow(flatName: String, isAdmin: Boolean, onInvite: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    flatName,
                    color = FigmaColors.Ink,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = FigmaColors.InkMuted,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            if (isAdmin) {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .background(FigmaColors.Primary, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Shield, null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Text("Admin", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .background(FigmaColors.SuccessBg, RoundedCornerShape(999.dp))
                    .border(1.dp, FigmaColors.SuccessBorder, RoundedCornerShape(999.dp))
                    .padding(horizontal = 11.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(Modifier.size(8.dp).background(FigmaColors.Success, CircleShape))
                Text("At Flat", color = FigmaColors.SuccessText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Filled.Check, null, tint = FigmaColors.SuccessText, modifier = Modifier.size(10.dp))
            }
            OutlinedButton(
                onClick = onInvite,
                shape = RoundedCornerShape(999.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FigmaColors.Primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FigmaColors.Primary),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Filled.PersonAdd, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Invite", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ViewModeToggle(viewMode: HomeViewMode, onViewModeChange: (HomeViewMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FigmaColors.Surface, RoundedCornerShape(16.dp))
            .border(1.dp, FigmaColors.SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(5.dp)
    ) {
        ViewModeChip(
            title = "Flat View",
            subtitle = "Overview of entire flat",
            selected = viewMode == HomeViewMode.FLAT,
            onClick = { onViewModeChange(HomeViewMode.FLAT) },
            modifier = Modifier.weight(1f)
        )
        ViewModeChip(
            title = "My View",
            subtitle = "Only my tasks & data",
            selected = viewMode == HomeViewMode.MY,
            onClick = { onViewModeChange(HomeViewMode.MY) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ViewModeChip(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) FigmaColors.Primary else Color.Transparent
    val titleColor = if (selected) Color.White else FigmaColors.Ink
    val subColor = if (selected) Color.White.copy(alpha = 0.8f) else FigmaColors.InkMuted
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = titleColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(subtitle, color = subColor, fontSize = 10.sp)
    }
}

@Composable
private fun WelcomeCard(
    greeting: String,
    displayName: String,
    memberCount: Int,
    tasksToday: Int,
    monthlySpent: Double,
    flatHealth: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(FigmaColors.Primary, FigmaColors.PrimaryLight)
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                "$greeting, $displayName 👋",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Here's what's happening in your flat today.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatChip("$memberCount", "Members")
                StatChip("$tasksToday", "Tasks Today")
                StatChip("₹${monthlySpent.toInt()}", "This Month")
                StatChip("$flatHealth%", "Flat Health")
            }
        }
    }
}

@Composable
private fun StatChip(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
    }
}

@Composable
private fun TodaysTasksSection(
    tasks: List<habitiq.app.data.FlatTask>,
    members: List<Member>,
    currentUid: String,
    weeklyProgress: Int,
    completedCount: Int
) {
    SectionHeader("Today's Tasks", "View all")
    Spacer(Modifier.height(12.dp))
    if (tasks.isEmpty()) {
        Text(
            "No tasks due today. Enjoy the break!",
            color = FigmaColors.InkSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    } else {
        tasks.take(3).forEach { task ->
            TaskRow(
                task = task,
                assigneeName = memberName(members, task.currentAssignedUserId, currentUid)
            )
            Spacer(Modifier.height(8.dp))
        }
    }
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FigmaColors.Surface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { weeklyProgress / 100f },
                modifier = Modifier.size(56.dp),
                color = FigmaColors.Primary,
                trackColor = FigmaColors.SurfaceBorder,
                strokeWidth = 5.dp
            )
            Text("$weeklyProgress%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FigmaColors.Primary)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                "$completedCount tasks completed this week",
                color = FigmaColors.Ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text("Keep it up! 💪", color = FigmaColors.InkSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun TaskRow(
    task: habitiq.app.data.FlatTask,
    assigneeName: String
) {
    val dueLabel = when (task.dueDate) {
        LocalDate.now().toString() -> "Due Today"
        LocalDate.now().plusDays(1).toString() -> "Due Tomorrow"
        else -> "Due ${task.dueDate}"
    }
    val dueColor = if (dueLabel == "Due Today") FigmaColors.Error else FigmaColors.InkMuted
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FigmaColors.Background, RoundedCornerShape(12.dp))
            .border(1.dp, FigmaColors.SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.CheckCircleOutline,
            contentDescription = null,
            tint = FigmaColors.InkMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(task.name, color = FigmaColors.Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(assigneeName, color = FigmaColors.InkSecondary, fontSize = 12.sp)
        }
        Text(dueLabel, color = dueColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PendingRequestsSection() {
    SectionHeader("Pending Requests", null)
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        RequestCard("Join\nRequests", "2", FigmaColors.Primary, Modifier.weight(1f))
        RequestCard("Expense\nApproval", "1", FigmaColors.Warning, Modifier.weight(1f))
        RequestCard("Task\nReview", "1", FigmaColors.Teal, Modifier.weight(1f))
    }
}

@Composable
private fun RequestCard(title: String, count: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(FigmaColors.Surface, RoundedCornerShape(14.dp))
            .border(1.dp, FigmaColors.SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(count, color = accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(title, color = FigmaColors.Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text("Review →", color = accent, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun ExpensesOverviewCard(monthlySpent: Double) {
    SectionHeader("Expenses Overview", null)
    Spacer(Modifier.height(12.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(FigmaColors.Surface, RoundedCornerShape(16.dp))
            .border(1.dp, FigmaColors.SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            "You've spent ₹${"%.0f".format(monthlySpent)} this month",
            color = FigmaColors.Ink,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            "Settlements and bills sync from your flat.",
            color = FigmaColors.InkSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun RecentActivitySection(
    activity: List<habitiq.app.data.FlatActivity>,
    members: List<Member>
) {
    SectionHeader("Recent Activity", null)
    Spacer(Modifier.height(12.dp))
    if (activity.isEmpty()) {
        Text("No recent activity yet.", color = FigmaColors.InkSecondary, fontSize = 14.sp)
    } else {
        activity.take(5).forEach { item ->
            val name = memberName(members, item.userId, "")
            val timeAgo = formatTimeAgo(item.timestamp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(FigmaColors.Primary, CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(item.details.ifBlank { "$name — ${item.action}" }, color = FigmaColors.Ink, fontSize = 13.sp)
                    Text(timeAgo, color = FigmaColors.InkMuted, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun DiscoverTeaserSection() {
    SectionHeader("Explore popular areas", null)
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AreaCard("Hitech City", "4.6", "1.2K+ Active Flats", Modifier.weight(1f))
        AreaCard("Gachibowli", "4.5", "980+ Active Flats", Modifier.weight(1f))
    }
}

@Composable
private fun AreaCard(name: String, rating: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(FigmaColors.Primary.copy(alpha = 0.08f))
            .padding(14.dp)
    ) {
        Text(name, color = FigmaColors.Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("★ $rating", color = FigmaColors.Warning, fontSize = 12.sp)
        Text(subtitle, color = FigmaColors.InkSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun SectionHeader(title: String, action: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = FigmaColors.Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        if (action != null) {
            Text(action, color = FigmaColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun NoFlatContent(onCreateFlat: () -> Unit, onJoinFlat: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Apartment, null, tint = FigmaColors.Primary, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            "Welcome to Habitiq+",
            color = FigmaColors.Ink,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Create or join a flat to see your dashboard.",
            color = FigmaColors.InkSecondary,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        Button(
            onClick = onCreateFlat,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FigmaColors.Primary)
        ) { Text("Create a Flat") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onJoinFlat,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, FigmaColors.Primary)
        ) { Text("Join with Code", color = FigmaColors.Primary) }
    }
}

@Composable
private fun HomeLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = FigmaColors.Primary)
    }
}

@Composable
private fun HomeError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = FigmaColors.Error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = FigmaColors.Primary)) {
            Text("Retry")
        }
    }
}

private fun greetingForHour(): String = when (LocalDateTime.now().hour) {
    in 0..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}

private fun memberName(members: List<Member>, uid: String, currentUid: String): String {
    if (uid == currentUid) return "You"
    return members.find { it.uid == uid }?.nickname?.ifBlank { "Roommate" } ?: "Roommate"
}

private fun computeFlatHealth(data: HomeDashboardData): Int {
    val total = data.tasks.size.coerceAtLeast(1)
    val completed = data.tasks.count { it.status == "completed" }
    val overdue = data.tasks.count { it.status == "overdue" }
    val base = ((completed.toFloat() / total) * 100).toInt()
    return min(100, (base - overdue * 5).coerceAtLeast(50))
}

private fun formatTimeAgo(timestamp: String): String {
    return runCatching {
        val dt = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME)
        val hours = ChronoUnit.HOURS.between(dt, LocalDateTime.now())
        when {
            hours < 1 -> "Just now"
            hours < 24 -> "$hours hours ago"
            else -> "${ChronoUnit.DAYS.between(dt.toLocalDate(), LocalDate.now())} days ago"
        }
    }.getOrDefault("Recently")
}
