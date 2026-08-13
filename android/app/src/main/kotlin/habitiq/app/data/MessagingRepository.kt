package habitiq.app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant

class MessagingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeActiveSeekers(): Flow<List<SeekerProfile>> = callbackFlow {
        val reg = firestore.collection("seekerProfiles")
            .whereEqualTo("active", true)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                trySend(snap?.documents.orEmpty().mapNotNull { it.toSeekerProfile() })
            }
        awaitClose { reg.remove() }
    }

    fun observeMessagesForUser(userId: String): Flow<List<ChatMessage>> = callbackFlow {
        val reg = firestore.collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val all = snap?.documents.orEmpty().mapNotNull { it.toChatMessage() }
                trySend(all.filter { it.senderId == userId || it.receiverId == userId })
            }
        awaitClose { reg.remove() }
    }

    fun observeConversation(userId: String, partnerId: String): Flow<List<ChatMessage>> = callbackFlow {
        val reg = firestore.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(100)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val msgs = snap?.documents.orEmpty().mapNotNull { it.toChatMessage() }
                    .filter {
                        (it.senderId == userId && it.receiverId == partnerId) ||
                            (it.senderId == partnerId && it.receiverId == userId)
                    }
                trySend(msgs)
            }
        awaitClose { reg.remove() }
    }

    suspend fun upsertSeekerProfile(profile: SeekerProfile): Result<Unit> = runCatching {
        firestore.collection("seekerProfiles").document(profile.id)
            .set(profile.toFirestoreMap()).await()
    }

    suspend fun sendMessage(message: ChatMessage): Result<Unit> = runCatching {
        firestore.collection("messages").document(message.id)
            .set(message.toFirestoreMap()).await()
    }

    suspend fun saveFcmToken(uid: String, token: String): Result<Unit> = runCatching {
        firestore.collection("users").document(uid)
            .update(mapOf("fcmToken" to token, "fcmTokenUpdatedAt" to Instant.now().toString())).await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toSeekerProfile(): SeekerProfile? = SeekerProfile(
        id = getString("id") ?: id,
        displayName = getString("displayName").orEmpty(),
        photoUrl = getString("photoUrl").orEmpty(),
        city = getString("city").orEmpty(),
        lookingIn = getString("lookingIn").orEmpty(),
        budget = getDouble("budget") ?: 0.0,
        bio = getString("bio").orEmpty(),
        lifestyleTags = getString("lifestyleTags").orEmpty(),
        active = getBoolean("active") ?: true,
        createdAt = getString("createdAt").orEmpty()
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toChatMessage(): ChatMessage? = ChatMessage(
        id = getString("id") ?: id,
        senderId = getString("senderId").orEmpty(),
        receiverId = getString("receiverId").orEmpty(),
        flatId = getString("flatId"),
        content = getString("content").orEmpty(),
        timestamp = getLong("timestamp") ?: 0L,
        isViewingRequest = getBoolean("isViewingRequest") ?: false,
        viewingTime = getLong("viewingTime"),
        viewingStatus = getString("viewingStatus")
    )
}
