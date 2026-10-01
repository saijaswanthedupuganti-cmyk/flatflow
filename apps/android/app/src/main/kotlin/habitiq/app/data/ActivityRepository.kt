package habitiq.app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.util.UUID

class ActivityRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeRecentActivity(flatId: String, limit: Long = 10): Flow<List<FlatActivity>> = callbackFlow {
        val registration = firestore.collection("flats").document(flatId)
            .collection("activityLog")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    if (doc.getBoolean("hidden") == true) return@mapNotNull null
                    FlatActivity(
                        id = doc.getString("id") ?: doc.id,
                        timestamp = doc.getString("timestamp").orEmpty(),
                        userId = doc.getString("userId").orEmpty(),
                        action = doc.getString("action").orEmpty(),
                        details = doc.getString("details").orEmpty()
                    )
                }
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addActivity(
        flatId: String,
        userId: String,
        action: String,
        details: String
    ): Result<Unit> = runCatching {
        val id = UUID.randomUUID().toString()
        val entry = mapOf(
            "id" to id,
            "timestamp" to Instant.now().toString(),
            "userId" to userId,
            "action" to action,
            "details" to details
        )
        firestore.collection("flats").document(flatId).collection("activityLog")
            .document(id).set(entry).await()
    }
}
