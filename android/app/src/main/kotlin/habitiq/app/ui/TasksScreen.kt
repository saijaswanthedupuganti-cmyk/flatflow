package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.data.FlatTask
import habitiq.app.data.FlatSwapRequest
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.lib.RotationEngine
import habitiq.app.ui.figma.NextUpLabel
import habitiq.app.ui.figma.SwapRequestBanner
import habitiq.app.ui.theme.FigmaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: FlatViewModel,
    onOpenGoingAway: () -> Unit = {},
    onOpenTaskDetail: (String) -> Unit = {},
    onOpenCreateTask: () -> Unit = {},
    onReviewSwaps: () -> Unit = {}
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycleCompat()
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val swaps by viewModel.swapRequests.collectAsStateWithLifecycleCompat()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycleCompat()
    val currentMember by viewModel.currentMember.collectAsStateWithLifecycleCompat()
    val triggerAdd by viewModel.showAddTaskTrigger.collectAsStateWithLifecycleCompat()

    var filterTab by remember { mutableStateOf("All") }
    LaunchedEffect(triggerAdd) { if (triggerAdd) { onOpenCreateTask(); viewModel.showAddTaskTrigger.value = false } }

    val uid = currentUser?.uid.orEmpty()
    val displayed = remember(tasks, filterTab, uid) {
        when (filterTab) {
            "Mine" -> tasks.filter { it.currentAssignedUserId == uid && it.status != "completed" }
            "Overdue" -> tasks.filter { it.status == "overdue" }
            else -> tasks.filter { it.status != "completed" }
        }
    }
    val pendingSwapsForMe = remember(swaps, uid) { swaps.count { it.status == "pending" && it.toUserId == uid } }
    val isOos = currentMember?.status == "out_of_station"

    Box(Modifier.fillMaxSize().background(FigmaColors.Background)) {
        Column(Modifier.fillMaxSize()) {
            Text("Tasks", modifier = Modifier.padding(20.dp), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FigmaColors.Ink)
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Mine", "Overdue").forEach { tab ->
                    FilterChip(selected = filterTab == tab, onClick = { filterTab = tab }, label = { Text(tab) })
                }
            }
            if (pendingSwapsForMe > 0) {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SwapRequestBanner(count = pendingSwapsForMe, onReview = onReviewSwaps)
                }
            }
            GoingAwayCard(isOos = isOos, onClick = onOpenGoingAway)
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(displayed, key = { it.taskId }) { task ->
                    FigmaTaskCard(
                        task = task,
                        members = members,
                        uid = uid,
                        onOpenDetail = { onOpenTaskDetail(task.taskId) },
                        onComplete = { viewModel.completeTask(task) }
                    )
                }
            }
        }
        if (isAdmin) {
            FloatingActionButton(
                onClick = onOpenCreateTask,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                containerColor = FigmaColors.Primary
            ) { Icon(Icons.Filled.Add, "Add task") }
        }
    }
}

@Composable
private fun GoingAwayCard(isOos: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FigmaColors.Surface)
            .border(1.dp, FigmaColors.SurfaceBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Flight, null, tint = FigmaColors.Primary)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(if (isOos) "Out of station" else "Going Away", fontWeight = FontWeight.SemiBold, color = FigmaColors.Ink)
            Text("Assign tasks before you leave", fontSize = 12.sp, color = FigmaColors.InkSecondary)
        }
        Text(if (isOos) "Return" else "Open →", color = FigmaColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
    val assignee = members.find { it.uid == task.currentAssignedUserId }
    val nextUid = RotationEngine.getNextAssignee(task, members)
    val nextName = members.find { it.uid == nextUid }?.nickname?.split(" ")?.firstOrNull() ?: "—"
    val isMine = task.currentAssignedUserId == uid

    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FigmaColors.Surface)
            .border(1.dp, FigmaColors.SurfaceBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onOpenDetail)
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                if (task.status == "overdue") {
                    Box(Modifier.clip(RoundedCornerShape(999.dp)).background(FigmaColors.OverdueBg).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("Overdue", color = FigmaColors.Overdue, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(6.dp))
                }
                Text(task.name, fontWeight = FontWeight.Bold, color = FigmaColors.Ink, fontSize = 17.sp)
                Text(
                    assignee?.nickname ?: "Unassigned",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = FigmaColors.Ink
                )
                Spacer(Modifier.height(4.dp))
                NextUpLabel(nextName)
            }
            Text("›", color = FigmaColors.InkMuted, fontSize = 20.sp)
        }
        if (isMine && task.status != "completed") {
            Spacer(Modifier.height(8.dp))
            Button(onClick = onComplete, colors = ButtonDefaults.buttonColors(containerColor = FigmaColors.Primary)) {
                Text("Complete")
            }
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
    val incoming = swaps.filter { it.status == "pending" && it.toUserId == uid }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = FigmaColors.Background) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Swap Requests", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FigmaColors.Ink)
            incoming.forEach { swap ->
                val taskName = tasks.find { it.taskId == swap.taskId }?.name ?: "task"
                val from = members.find { it.uid == swap.fromUserId }?.nickname ?: "Flatmate"
                Card(colors = CardDefaults.cardColors(containerColor = FigmaColors.Surface)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("$from wants you to cover \"$taskName\"", fontSize = 14.sp, color = FigmaColors.Ink)
                        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onAccept(swap.id) }) { Text("Accept") }
                            OutlinedButton(onClick = { onReject(swap.id) }) { Text("Decline") }
                        }
                    }
                }
            }
            if (incoming.isEmpty()) Text("No pending requests.", color = FigmaColors.InkSecondary)
        }
    }
}
