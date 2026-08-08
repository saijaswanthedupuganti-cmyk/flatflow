package habitiq.app.data

import com.google.firebase.firestore.FirebaseFirestore
import habitiq.app.lib.RotationEngine
import habitiq.app.flats.Member
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.util.UUID

class TasksRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val activityRepository: ActivityRepository = ActivityRepository()
) {
    fun observeTasks(flatId: String): Flow<List<FlatTask>> = callbackFlow {
        val registration = firestore.collection("flats").document(flatId).collection("tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents.orEmpty().mapNotNull { doc -> doc.toFlatTask() }
                trySend(tasks)
            }
        awaitClose { registration.remove() }
    }

    suspend fun completeTask(
        flatId: String,
        task: FlatTask,
        members: List<Member>,
        userId: String
    ): Result<Unit> = runCatching {
        val updated = RotationEngine.completeTask(task, members)
        firestore.collection("flats").document(flatId).collection("tasks")
            .document(task.taskId).set(updated.toFirestoreMap()).await()
        activityRepository.addActivity(
            flatId = flatId,
            userId = userId,
            action = "completed_task",
            details = "completed ${task.name}"
        ).getOrThrow()
    }

    suspend fun createTask(
        flatId: String,
        name: String,
        type: String,
        priority: String,
        frequency: String,
        queueOrder: List<String>,
        dueDate: String,
        adminId: String
    ): Result<Unit> = runCatching {
        val taskId = "t-${UUID.randomUUID()}"
        val assignee = queueOrder.firstOrNull() ?: adminId
        val now = Instant.now().toString()
        val task = FlatTask(
            taskId = taskId,
            name = name.trim(),
            type = type,
            currentAssignedUserId = assignee,
            status = "pending",
            dueDate = dueDate,
            priority = priority,
            frequency = frequency,
            queueOrder = queueOrder,
            lastCompletedAt = now
        )
        firestore.collection("flats").document(flatId).collection("tasks")
            .document(taskId).set(task.toFirestoreMap()).await()
        activityRepository.addActivity(
            flatId = flatId,
            userId = adminId,
            action = "task_created",
            details = "created a new task: ${task.name}"
        ).getOrThrow()
    }

    suspend fun deleteTask(flatId: String, taskId: String, adminId: String, taskName: String): Result<Unit> = runCatching {
        firestore.collection("flats").document(flatId).collection("tasks").document(taskId).delete().await()
        activityRepository.addActivity(
            flatId = flatId,
            userId = adminId,
            action = "task_deleted",
            details = "deleted task: $taskName"
        ).getOrThrow()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toFlatTask(): FlatTask? {
        val taskId = getString("taskId") ?: id
        val queue = (get("queueOrder") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        return FlatTask(
            taskId = taskId,
            name = getString("name").orEmpty(),
            type = getString("type") ?: "rotating_duty",
            currentAssignedUserId = getString("currentAssignedUserId").orEmpty(),
            status = getString("status") ?: "pending",
            dueDate = getString("dueDate").orEmpty(),
            priority = getString("priority") ?: "medium",
            frequency = getString("frequency") ?: "weekly",
            queueOrder = queue,
            lastCompletedAt = getString("lastCompletedAt").orEmpty()
        )
    }
}
