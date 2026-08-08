package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.data.FlatTask
import habitiq.app.data.FlatSwapRequest
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.ui.theme.FigmaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: FlatViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycleCompat()
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val swaps by viewModel.swapRequests.collectAsStateWithLifecycleCompat()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycleCompat()
    val currentMember by viewModel.currentMember.collectAsStateWithLifecycleCompat()
    val triggerAdd by viewModel.showAddTaskTrigger.collectAsStateWithLifecycleCompat()

    var showCreate by remember { mutableStateOf(false) }
    var filterTab by remember { mutableStateOf("All") }
    LaunchedEffect(triggerAdd) { if (triggerAdd) { showCreate = true; viewModel.showAddTaskTrigger.value = false } }

    val uid = currentUser?.uid.orEmpty()
    val displayed = remember(tasks, filterTab, uid) {
        when (filterTab) {
            "Mine" -> tasks.filter { it.currentAssignedUserId == uid && it.status != "completed" }
            "Overdue" -> tasks.filter { it.status == "overdue" }
            else -> tasks
        }
    }

  var taskName by remember { mutableStateOf("") }
  var frequency by remember { mutableStateOf("weekly") }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("New Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = taskName, onValueChange = { taskName = it }, label = { Text("Task name") }, singleLine = true)
                    Text("Frequency: $frequency", fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("daily", "weekly", "monthly", "one_time").forEach { f ->
                            FilterChip(selected = frequency == f, onClick = { frequency = f }, label = { Text(f) })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (taskName.isNotBlank()) {
                        viewModel.createTask(taskName, frequency, "medium", members.map { it.uid })
                        taskName = ""
                        showCreate = false
                    }
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } }
        )
    }

    Box(Modifier.fillMaxSize().background(FigmaColors.Background)) {
    Column(Modifier.fillMaxSize()) {
        Text("Tasks", modifier = Modifier.padding(20.dp), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FigmaColors.Ink)
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Mine", "Overdue").forEach { tab ->
                FilterChip(selected = filterTab == tab, onClick = { filterTab = tab }, label = { Text(tab) })
            }
        }
        val isOos = currentMember?.status == "out_of_station"
        Card(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FigmaColors.Surface)
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Flight, null, tint = FigmaColors.Primary)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (isOos) "Out of station" else "Going away?", fontWeight = FontWeight.SemiBold)
                    Text("Toggle to skip your turn in rotation", fontSize = 12.sp, color = FigmaColors.InkSecondary)
                }
                Button(onClick = { viewModel.toggleOutOfStation(!isOos) }) {
                    Text(if (isOos) "Return" else "OOS")
                }
            }
        }
        if (swaps.any { it.status == "pending" && it.toUserId == uid }) {
            Text("Swap requests", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
            swaps.filter { it.status == "pending" && it.toUserId == uid }.forEach { swap ->
                SwapRow(swap, tasks, members, onAccept = { viewModel.respondToSwap(swap.id, true) }, onReject = { viewModel.respondToSwap(swap.id, false) })
            }
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(displayed, key = { it.taskId }) { task ->
                TaskRow(task, members, uid, onComplete = { viewModel.completeTask(task) }, onSwap = { toUid -> viewModel.createSwapRequest(task.taskId, toUid) })
            }
        }
    }
    if (isAdmin) {
        FloatingActionButton(
            onClick = { showCreate = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = FigmaColors.Primary
        ) { Icon(Icons.Filled.Add, "Add task") }
    }
    }
}

@Composable
private fun TaskRow(task: FlatTask, members: List<Member>, uid: String, onComplete: () -> Unit, onSwap: (String) -> Unit) {
    val assignee = members.find { it.uid == task.currentAssignedUserId }?.nickname ?: task.currentAssignedUserId
    Card(colors = CardDefaults.cardColors(containerColor = FigmaColors.Surface)) {
        Column(Modifier.padding(12.dp)) {
            Text(task.name, fontWeight = FontWeight.Bold, color = FigmaColors.Ink)
            Text("Assigned: $assignee · ${task.status}", fontSize = 12.sp, color = FigmaColors.InkSecondary)
            Text("Due: ${task.dueDate.take(10)}", fontSize = 12.sp, color = FigmaColors.InkMuted)
            if (task.currentAssignedUserId == uid && task.status != "completed") {
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onComplete) { Text("Complete") }
                    val swapTarget = members.firstOrNull { it.uid != uid }
                    if (swapTarget != null) {
                        OutlinedButton(onClick = { onSwap(swapTarget.uid) }) { Text("Swap") }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwapRow(swap: FlatSwapRequest, tasks: List<FlatTask>, members: List<Member>, onAccept: () -> Unit, onReject: () -> Unit) {
    val taskName = tasks.find { it.taskId == swap.taskId }?.name ?: "task"
    val from = members.find { it.uid == swap.fromUserId }?.nickname ?: swap.fromUserId
    Card(Modifier.padding(horizontal = 16.dp, vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = FigmaColors.Surface)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("$from wants to swap \"$taskName\"", fontSize = 13.sp)
            }
            Button(onClick = onAccept) { Text("Accept") }
            Spacer(Modifier.width(4.dp))
            OutlinedButton(onClick = onReject) { Text("Decline") }
        }
    }
}
