package habitiq.app.flats

import com.google.firebase.crashlytics.FirebaseCrashlytics
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
    suspend fun createFlat(
        name: String,
        uid: String,
        nickname: String,
        email: String,
        flatType: String? = null,
        city: String? = null,
        area: String? = null,
        pincode: String? = null,
        landmark: String? = null
    ): Result<String> = runCatching {
        var flatId = generateFlatId()
        var attempts = 0
        while (flatExists(flatId) && attempts < MAX_ID_GENERATION_ATTEMPTS) {
            flatId = generateFlatId()
            attempts++
        }

        val flatData = hashMapOf<String, Any?>(
            "name" to name,
            "adminUid" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "memberCount" to 1,
            "subscriptionStatus" to "trial",
            "trialEndDate" to Instant.now().plusSeconds(TRIAL_DURATION_SECONDS).toString()
        )
        flatType?.let { flatData["flatType"] = it }
        city?.let { flatData["city"] = it }
        area?.let { flatData["area"] = it }
        pincode?.let { flatData["pincode"] = it }
        landmark?.let { flatData["landmark"] = it }
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
    }.recoverCatching { throw IllegalStateException(mapFlatError(reported(it)), it) }

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
    }.recoverCatching { throw IllegalStateException(mapFlatError(reported(it)), it) }

    suspend fun getFlat(flatId: String): Result<FlatInfo> = runCatching {
        val snap = firestore.collection("flats").document(flatId).get().await()
        if (!snap.exists()) throw FlatNotFoundException()
        FlatInfo(
            id = flatId,
            name = snap.getString("name") ?: flatId,
            adminUid = snap.getString("adminUid").orEmpty(),
            memberCount = (snap.getLong("memberCount") ?: 0L).toInt(),
            joinMode = snap.getString("joinMode") ?: "auto",
            vacancy = snap.get("vacancy")?.let { parseVacancy(it) }
        )
    }.recoverCatching { throw IllegalStateException(mapFlatError(reported(it)), it) }

    // The user only ever sees the mapped message, so without this the underlying failure would
    // leave no trace anywhere. Returns the failure unchanged so it can still be mapped.
    private fun reported(error: Throwable): Exception {
        val exception = error as Exception
        if (!isExpectedFlatError(exception)) {
            FirebaseCrashlytics.getInstance().recordException(exception)
        }
        return exception
    }

    private suspend fun flatExists(flatId: String): Boolean {
        val snap = firestore.collection("flats").document(flatId).get().await()
        return snap.exists()
    }

    suspend fun getJoinMode(flatId: String): Result<String> = runCatching {
        val snap = firestore.collection("flats").document(flatId).get().await()
        if (!snap.exists()) throw FlatNotFoundException()
        snap.getString("joinMode") ?: "auto"
    }

    suspend fun requestToJoin(
        flatId: String,
        uid: String,
        nickname: String,
        email: String
    ): Result<String> = runCatching {
        if (!flatExists(flatId)) throw FlatNotFoundException()
        val memberSnap = firestore.collection("flats").document(flatId).collection("members").document(uid).get().await()
        if (memberSnap.exists()) throw AlreadyMemberException()
        val requestId = java.util.UUID.randomUUID().toString()
        firestore.collection("flats").document(flatId).collection("joinRequests").document(requestId)
            .set(mapOf(
                "id" to requestId,
                "uid" to uid,
                "nickname" to nickname,
                "email" to email,
                "status" to "pending",
                "createdAt" to Instant.now().toString()
            )).await()
        requestId
    }

    suspend fun renameFlat(flatId: String, newName: String): Result<Unit> = runCatching {
        firestore.collection("flats").document(flatId).update("name", newName.trim()).await()
    }

    suspend fun setJoinMode(flatId: String, mode: String): Result<Unit> = runCatching {
        firestore.collection("flats").document(flatId).update("joinMode", mode).await()
    }

    suspend fun updateVacancy(flatId: String, vacancy: habitiq.app.data.VacancyData): Result<Unit> = runCatching {
        val map = mapOf(
            "active" to vacancy.active,
            "city" to vacancy.city,
            "area" to vacancy.area,
            "rentPerHead" to vacancy.rentPerHead,
            "currency" to vacancy.currency,
            "bedsAvailable" to vacancy.bedsAvailable,
            "preferredGender" to vacancy.preferredGender,
            "about" to vacancy.about,
            "updatedAt" to Instant.now().toString()
        )
        firestore.collection("flats").document(flatId).update("vacancy", map).await()
    }

    private fun parseVacancy(raw: Any): habitiq.app.data.VacancyData? {
        val v = raw as? Map<*, *> ?: return null
        return habitiq.app.data.VacancyData(
            active = v["active"] as? Boolean ?: false,
            city = v["city"]?.toString().orEmpty(),
            area = v["area"]?.toString().orEmpty(),
            rentPerHead = (v["rentPerHead"] as? Number)?.toDouble(),
            currency = v["currency"]?.toString() ?: "INR",
            bedsAvailable = (v["bedsAvailable"] as? Number)?.toInt() ?: 1,
            preferredGender = v["preferredGender"]?.toString() ?: "any",
            about = v["about"]?.toString().orEmpty(),
            updatedAt = v["updatedAt"]?.toString().orEmpty()
        )
    }
}
