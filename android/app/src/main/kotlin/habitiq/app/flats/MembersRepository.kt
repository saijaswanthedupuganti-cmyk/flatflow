package habitiq.app.flats

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class Member(
    val uid: String,
    val nickname: String,
    val role: String
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
                        role = doc.getString("role") ?: "member"
                    )
                }
                trySend(members)
            }
        awaitClose { registration.remove() }
    }
}
