package habitiq.app.flats

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import habitiq.app.data.JoinRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class Member(
    val uid: String,
    val nickname: String,
    val role: String,
    val status: String = "available",
    val reliabilityScore: Int = 100
)

class MembersRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeMembers(flatId: String): Flow<List<Member>> = callbackFlow {
        val registration = firestore.collection("flats").document(flatId).collection("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val members = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val uid = doc.getString("uid") ?: return@mapNotNull null
                    Member(
                        uid = uid,
                        nickname = doc.getString("nickname").orEmpty(),
                        role = doc.getString("role") ?: "member",
                        status = doc.getString("status") ?: "available",
                        reliabilityScore = (doc.getLong("reliabilityScore") ?: 100L).toInt()
                    )
                }
                trySend(members)
            }
        awaitClose { registration.remove() }
    }

    fun observeJoinRequests(flatId: String): Flow<List<JoinRequest>> = callbackFlow {
        val reg = firestore.collection("flats").document(flatId).collection("joinRequests")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents.orEmpty().mapNotNull { doc ->
                    JoinRequest(
                        id = doc.getString("id") ?: doc.id,
                        uid = doc.getString("uid").orEmpty(),
                        nickname = doc.getString("nickname").orEmpty(),
                        email = doc.getString("email").orEmpty(),
                        status = doc.getString("status") ?: "pending",
                        createdAt = doc.getString("createdAt").orEmpty()
                    )
                })
            }
        awaitClose { reg.remove() }
    }

    suspend fun updateMemberStatus(flatId: String, uid: String, status: String): Result<Unit> = runCatching {
        firestore.collection("flats").document(flatId).collection("members").document(uid)
            .update("status", status).await()
    }

    suspend fun kickMember(flatId: String, targetUid: String): Result<Unit> = runCatching {
        firestore.runTransaction { transaction ->
            val flatRef = firestore.collection("flats").document(flatId)
            val memberRef = flatRef.collection("members").document(targetUid)
            val memberSnap = transaction.get(memberRef)
            if (memberSnap.exists()) {
                transaction.delete(memberRef)
                transaction.update(flatRef, "memberCount", FieldValue.increment(-1))
            }
            null
        }.await()
    }

    suspend fun transferAdmin(flatId: String, newAdminUid: String, currentAdminUid: String): Result<Unit> = runCatching {
        firestore.runTransaction { transaction ->
            val flatRef = firestore.collection("flats").document(flatId)
            transaction.update(flatRef, "adminUid", newAdminUid)
            transaction.update(flatRef.collection("members").document(newAdminUid), "role", "admin")
            transaction.update(flatRef.collection("members").document(currentAdminUid), "role", "member")
            null
        }.await()
    }

    suspend fun rejectJoinRequest(flatId: String, requestId: String): Result<Unit> = runCatching {
        firestore.collection("flats").document(flatId).collection("joinRequests").document(requestId)
            .update("status", "rejected").await()
    }
}
