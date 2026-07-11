package habitiq.app.flats

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.Instant

private const val MAX_MEMBERS = 8
private const val MAX_ID_GENERATION_ATTEMPTS = 5
private const val TRIAL_DURATION_SECONDS = 30L * 24 * 60 * 60

class FlatsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun createFlat(name: String, uid: String, nickname: String, email: String): Result<String> = runCatching {
        var flatId = generateFlatId()
        var attempts = 0
        while (flatExists(flatId) && attempts < MAX_ID_GENERATION_ATTEMPTS) {
            flatId = generateFlatId()
            attempts++
        }

        val flatData = hashMapOf(
            "name" to name,
            "adminUid" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "memberCount" to 1,
            "subscriptionStatus" to "trial",
            "trialEndDate" to Instant.now().plusSeconds(TRIAL_DURATION_SECONDS).toString()
        )
        firestore.collection("flats").document(flatId).set(flatData).await()

        val memberData = hashMapOf(
            "uid" to uid,
            "nickname" to nickname,
            "email" to email,
            "role" to "admin",
            "status" to "available",
            "reliabilityScore" to 100,
            "joinedAt" to Instant.now().toString()
        )
        firestore.collection("flats").document(flatId).collection("members").document(uid)
            .set(memberData).await()

        firestore.collection("users").document(uid).set(
            mapOf(
                "activeFlatId" to flatId,
                "flatIds" to FieldValue.arrayUnion(flatId),
                "email" to email
            ),
            SetOptions.merge()
        ).await()

        flatId
    }.recoverCatching { throw IllegalStateException(mapFlatError(it as Exception), it) }

    suspend fun joinFlat(flatId: String, uid: String, nickname: String, email: String): Result<Unit> = runCatching {
        if (!flatExists(flatId)) throw FlatNotFoundException()

        firestore.runTransaction { transaction ->
            val flatRef = firestore.collection("flats").document(flatId)
            val memberRef = flatRef.collection("members").document(uid)

            val flatSnap = transaction.get(flatRef)
            val memberSnap = transaction.get(memberRef)

            if (!flatSnap.exists()) throw FlatNotFoundException()
            val memberCount = flatSnap.getLong("memberCount") ?: 0L
            if (memberCount >= MAX_MEMBERS) throw FlatFullException()
            if (memberSnap.exists()) throw AlreadyMemberException()

            val memberData = hashMapOf(
                "uid" to uid,
                "nickname" to nickname,
                "email" to email,
                "role" to "member",
                "status" to "available",
                "reliabilityScore" to 100,
                "joinedAt" to Instant.now().toString()
            )
            transaction.set(memberRef, memberData)
            transaction.update(flatRef, "memberCount", FieldValue.increment(1))

            val userRef = firestore.collection("users").document(uid)
            transaction.set(
                userRef,
                mapOf(
                    "activeFlatId" to flatId,
                    "flatIds" to FieldValue.arrayUnion(flatId),
                    "email" to email
                ),
                SetOptions.merge()
            )

            null
        }.await()

        Unit
    }.recoverCatching { throw IllegalStateException(mapFlatError(it as Exception), it) }

    suspend fun getFlat(flatId: String): Result<FlatInfo> = runCatching {
        val snap = firestore.collection("flats").document(flatId).get().await()
        if (!snap.exists()) throw FlatNotFoundException()
        FlatInfo(
            id = flatId,
            name = snap.getString("name") ?: flatId,
            adminUid = snap.getString("adminUid").orEmpty(),
            memberCount = (snap.getLong("memberCount") ?: 0L).toInt()
        )
    }.recoverCatching { throw IllegalStateException(mapFlatError(it as Exception), it) }

    private suspend fun flatExists(flatId: String): Boolean {
        val snap = firestore.collection("flats").document(flatId).get().await()
        return snap.exists()
    }
}
