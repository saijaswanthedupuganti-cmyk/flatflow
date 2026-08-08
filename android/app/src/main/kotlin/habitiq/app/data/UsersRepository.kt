package habitiq.app.data

import com.google.firebase.crashlytics.FirebaseCrashlytics
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
    }.onFailure { recordNonFatal(it) }

    suspend fun getActiveFlatId(uid: String): Result<String?> = runCatching {
        val snap = firestore.collection("users").document(uid).get().await()
        snap.getString("activeFlatId")
    }.onFailure { recordNonFatal(it) }

    suspend fun getUserProfile(uid: String): Result<UserProfileData> = runCatching {
        val snap = firestore.collection("users").document(uid).get().await()
        val flatIds = (snap.get("flatIds") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        UserProfileData(
            uid = uid,
            email = snap.getString("email").orEmpty(),
            displayName = snap.getString("displayName").orEmpty(),
            activeFlatId = snap.getString("activeFlatId"),
            flatIds = flatIds
        )
    }.onFailure { recordNonFatal(it) }

    suspend fun updateProfile(uid: String, displayName: String): Result<Unit> = runCatching {
        firestore.collection("users").document(uid)
            .update("displayName", displayName.trim())
            .await()
        Unit
    }.onFailure { recordNonFatal(it) }

    suspend fun setActiveFlat(uid: String, flatId: String): Result<Unit> = runCatching {
        firestore.collection("users").document(uid)
            .update("activeFlatId", flatId)
            .await()
        Unit
    }.onFailure { recordNonFatal(it) }

    // Must run while the user is still signed in -- Firestore rules require request.auth.
    suspend fun deleteUserData(uid: String): Result<Unit> = runCatching {
        val userRef = firestore.collection("users").document(uid)
        val flatId = userRef.get().await().getString("activeFlatId")
        if (flatId != null) {
            leaveFlat(flatId, uid)
        }
        userRef.delete().await()
        Unit
    }.onFailure { recordNonFatal(it) }

    // Callers surface a generic message or, in the deletion path, drop the failure entirely --
    // without this the underlying Firestore error would leave no trace anywhere.
    private fun recordNonFatal(error: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(error)
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
