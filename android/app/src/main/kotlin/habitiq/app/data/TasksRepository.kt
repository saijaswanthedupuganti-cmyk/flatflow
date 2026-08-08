package habitiq.app.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class TasksRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeTasks(flatId: String): Flow<List<FlatTask>> = callbackFlow {
        val registration = firestore.collection("flats").document(flatId).collection("tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    FlatTask(
                        taskId = doc.getString("taskId") ?: doc.id,
                        name = doc.getString("name").orEmpty(),
                        currentAssignedUserId = doc.getString("currentAssignedUserId").orEmpty(),
                        status = doc.getString("status") ?: "pending",
                        dueDate = doc.getString("dueDate").orEmpty(),
                        priority = doc.getString("priority") ?: "medium"
                    )
                }
                trySend(tasks)
            }
        awaitClose { registration.remove() }
    }
}
