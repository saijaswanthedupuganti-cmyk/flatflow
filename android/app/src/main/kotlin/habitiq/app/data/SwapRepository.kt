package habitiq.app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.util.UUID

class SwapRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeSwapRequests(flatId: String): Flow<List<FlatSwapRequest>> = callbackFlow {
        val registration = firestore.collection("flats").document(flatId)
            .collection("swapRequests")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().map { doc ->
                    FlatSwapRequest(
                        id = doc.id,
                        taskId = doc.getString("taskId").orEmpty(),
                        fromUserId = doc.getString("fromUserId").orEmpty(),
                        toUserId = doc.getString("toUserId").orEmpty(),
                        status = doc.getString("status") ?: "pending",
                        read = doc.getBoolean("read") ?: false,
                        createdAt = doc.getString("createdAt").orEmpty()
                    )
                }
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun createSwapRequest(
        flatId: String,
        taskId: String,
        fromUserId: String,
        toUserId: String
    ): Result<Unit> = runCatching {
        val id = UUID.randomUUID().toString()
        val data = mapOf(
            "taskId" to taskId,
            "fromUserId" to fromUserId,
            "toUserId" to toUserId,
            "status" to "pending",
            "read" to false,
            "createdAt" to Instant.now().toString()
        )
        firestore.collection("flats").document(flatId).collection("swapRequests")
            .document(id).set(data).await()
    }

    suspend fun respondToSwap(
        flatId: String,
        requestId: String,
        accept: Boolean
    ): Result<Unit> = runCatching {
        firestore.collection("flats").document(flatId).collection("swapRequests")
            .document(requestId)
            .update("status", if (accept) "accepted" else "rejected")
            .await()
    }
}
