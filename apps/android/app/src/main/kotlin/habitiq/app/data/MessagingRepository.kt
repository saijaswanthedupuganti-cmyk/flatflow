package habitiq.app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant

class MessagingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeActiveSeekers(): Flow<List<SeekerProfile>> = callbackFlow {
        val reg = firestore.collection("seekerProfiles")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                val profiles = snap?.documents.orEmpty()
                    .mapNotNull { it.toSeekerProfile() }
                    .filter { it.active }
                trySend(profiles)
            }
        awaitClose { reg.remove() }
    }

    fun observeMessagesForUser(userId: String): Flow<List<ChatMessage>> = callbackFlow {
        val reg = firestore.collection("messages")
            .whereEqualTo("senderId", userId)
            .limit(200)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                trySend(snap?.documents.orEmpty().mapNotNull { it.toChatMessage() })
            }
        awaitClose { reg.remove() }
    }.combine(observeReceivedMessages(userId)) { sent, received ->
        (sent + received).distinctBy { it.id }.sortedByDescending { it.timestamp }.take(200)
    }

    private fun observeReceivedMessages(userId: String): Flow<List<ChatMessage>> = callbackFlow {
        val reg = firestore.collection("messages")
            .whereEqualTo("receiverId", userId)
            .limit(200)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                trySend(snap?.documents.orEmpty().mapNotNull { it.toChatMessage() })
            }
        awaitClose { reg.remove() }
    }

    fun observeConversation(userId: String, partnerId: String): Flow<List<ChatMessage>> =
        observeMessagesForUser(userId).let { messages ->
            messages.map { all ->
                all.filter {
                    (it.senderId == userId && it.receiverId == partnerId) ||
                        (it.senderId == partnerId && it.receiverId == userId)
                }.sortedBy { it.timestamp }.takeLast(100)
            }
        }

    suspend fun upsertSeekerProfile(profile: SeekerProfile): Result<Unit> = runCatching {
        firestore.collection("seekerProfiles").document(profile.id)
            .set(profile.toFirestoreMap(), SetOptions.merge()).await()
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
        gender = getString("gender").orEmpty(),
        lifestyleTags = getString("lifestyleTags").orEmpty(),
        active = getBoolean("active") ?: true,
        createdAt = getString("createdAt").orEmpty(),
        commuteAnchors = habitiq.app.discover.CommuteAnchors.parse(get("commuteAnchors"))
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toChatMessage(): ChatMessage? = ChatMessage(
        id = getString("id") ?: id,
        senderId = getString("senderId").orEmpty(),
        receiverId = getString("receiverId").orEmpty(),
        flatId = getString("flatId"),
        content = getString("content").orEmpty(),
        timestamp = getLong("timestamp") ?: 0L,
        connectionId = getString("connectionId").orEmpty(),
        isViewingRequest = getBoolean("isViewingRequest") ?: false,
        viewingTime = getLong("viewingTime"),
        viewingStatus = getString("viewingStatus")
    )
}
