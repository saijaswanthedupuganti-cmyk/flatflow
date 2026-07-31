package habitiq.app.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

fun shouldCreateDocument(existingData: Map<String, Any?>?): Boolean =
    existingData == null || existingData.isEmpty()

class UsersRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun ensureUserDocument(profile: UserProfile): Result<Unit> = runCatching {
        val docRef = firestore.collection("users").document(profile.uid)
        val snapshot = docRef.get().await()
        if (shouldCreateDocument(snapshot.data)) {
            docRef.set(
                mapOf(
                    "email" to profile.email,
                    "displayName" to profile.displayName
                )
            ).await()
        }
        Unit
    }

    suspend fun getActiveFlatId(uid: String): Result<String?> = runCatching {
        val snap = firestore.collection("users").document(uid).get().await()
        snap.getString("activeFlatId")
    }

    // Must run while the user is still signed in -- Firestore rules require request.auth.
    suspend fun deleteUserData(uid: String): Result<Unit> = runCatching {
        val userRef = firestore.collection("users").document(uid)
        val flatId = userRef.get().await().getString("activeFlatId")
        if (flatId != null) {
            leaveFlat(flatId, uid)
        }
        userRef.delete().await()
        Unit
    }

    private suspend fun leaveFlat(flatId: String, uid: String) {
        firestore.runTransaction { transaction ->
            val flatRef = firestore.collection("flats").document(flatId)
            val memberRef = flatRef.collection("members").document(uid)

            val flatSnap = transaction.get(flatRef)
            val memberSnap = transaction.get(memberRef)

            if (memberSnap.exists()) {
                transaction.delete(memberRef)
                if (flatSnap.exists()) {
                    transaction.update(flatRef, "memberCount", FieldValue.increment(-1))
                }
            }

            null
        }.await()
    }
}
