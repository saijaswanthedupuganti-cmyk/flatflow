package habitiq.app.data

import habitiq.app.lib.planLeave
import habitiq.app.lib.LeaveTask
import habitiq.app.lib.LeaveMember
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
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

    // Mirrors lib/flatService.ts's getUserFlatProfile: some accounts predate the
    // activeFlatId/flatIds[] schema and only have a legacy single `flatId` field. Without this
    // fallback those existing accounts look flat-less to the native app and get routed through
    // the "fresh account" chooser on every sign-in instead of straight to their flat.
    private suspend fun resolveFlatIds(docRef: DocumentReference, snap: DocumentSnapshot): Pair<String?, List<String>> {
        val active = snap.getString("activeFlatId")
        if (active != null) {
            val ids = (snap.get("flatIds") as? List<*>)?.mapNotNull { it?.toString() }
            return active to (ids?.takeIf { it.isNotEmpty() } ?: listOf(active))
        }
        val legacyFlatId = snap.getString("flatId") ?: return null to emptyList()
        // Migrate on read, same as the web app does.
        docRef.update(mapOf("activeFlatId" to legacyFlatId, "flatIds" to listOf(legacyFlatId))).await()
        return legacyFlatId to listOf(legacyFlatId)
    }

    suspend fun getActiveFlatId(uid: String): Result<String?> = runCatching {
        val docRef = firestore.collection("users").document(uid)
        val snap = docRef.get().await()
        resolveFlatIds(docRef, snap).first
    }.onFailure { recordNonFatal(it) }

    suspend fun getUserProfile(uid: String): Result<UserProfileData> = runCatching {
        val docRef = firestore.collection("users").document(uid)
        val snap = docRef.get().await()
        val (activeFlatId, flatIds) = resolveFlatIds(docRef, snap)
        UserProfileData(
            uid = uid,
            email = snap.getString("email").orEmpty(),
            displayName = snap.getString("displayName").orEmpty(),
            activeFlatId = activeFlatId,
            flatIds = flatIds
        )
    }.onFailure { recordNonFatal(it) }

    suspend fun updateProfile(
        uid: String,
        displayName: String,
        city: String = "",
        gender: String = ""
    ): Result<Unit> = runCatching {
        val fields = mutableMapOf<String, Any>("displayName" to displayName.trim())
        if (city.isNotBlank()) fields["city"] = city.trim()
        if (gender.isNotBlank()) fields["gender"] = gender.trim()
        firestore.collection("users").document(uid).set(fields, com.google.firebase.firestore.SetOptions.merge()).await()
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
        val (_, flatIds) = resolveFlatIds(userRef, userRef.get().await())
        // Leave every flat, not just the active one, so no flat keeps a member record for a deleted account.
        // One failing flat must not strand the rest, so each is attempted and any failure is recorded.
        flatIds.forEach { id -> runCatching { leaveFlat(id, uid) }.onFailure { recordNonFatal(it) } }
        // The seeker profile is public, so take it down now rather than wait for the server.
        // Connections and sent messages can't be deleted from a client (see firestore.rules);
        // the purgeDeletedUser Cloud Function removes them, and re-checks all of this, once
        // the Auth account is gone.
        runCatching { firestore.collection("seekerProfiles").document(uid).delete().await() }
            .onFailure { recordNonFatal(it) }
        runCatching { deleteAll(userRef.collection("blocked").get().await().documents.map { it.reference }) }
            .onFailure { recordNonFatal(it) }
        userRef.delete().await()
        Unit
    }.onFailure { recordNonFatal(it) }

    private suspend fun deleteAll(refs: List<DocumentReference>) {
        // Firestore caps a batch at 500 writes.
        refs.chunked(450).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { batch.delete(it) }
            batch.commit().await()
        }
    }

    suspend fun leaveCurrentFlat(uid: String, flatId: String): Result<String?> = runCatching {
        leaveFlat(flatId, uid)
    }.onFailure { recordNonFatal(it) }

    // Callers surface a generic message or, in the deletion path, drop the failure entirely --
    // without this the underlying Firestore error would leave no trace anywhere.
    private fun recordNonFatal(error: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(error)
    }

    /**
     * Leaves [flatId] without breaking it for the people who stay: this person's tasks pass to the
     * next person in each rotation, a sole admin hands over to the longest-standing member, and the
     * last member closes the flat. All of that runs first, while the leaver still has permission to
     * do it; only then is their own member record removed.
     */
    private suspend fun leaveFlat(flatId: String, uid: String): String? {
        val userRef = firestore.collection("users").document(uid)
        val userSnap = userRef.get().await()
        val flatIds = resolveFlatIds(userRef, userSnap).second
        applyLeavePlan(flatId, uid)
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
            val remaining = flatIds.filter { it != flatId }
            val nextFlat = remaining.firstOrNull()
            val userRef = firestore.collection("users").document(uid)
            transaction.update(userRef, mapOf(
                "activeFlatId" to nextFlat,
                "flatIds" to remaining
            ))
            null
        }.await()
        return flatIds.filter { it != flatId }.firstOrNull()
    }

    private suspend fun applyLeavePlan(flatId: String, uid: String) {
        val flatRef = firestore.collection("flats").document(flatId)
        val members = flatRef.collection("members").get().await().documents.map { doc ->
            LeaveMember(
                uid = doc.getString("uid") ?: doc.id,
                role = doc.getString("role") ?: "member",
                joinedAt = doc.getString("joinedAt").orEmpty(),
            )
        }
        if (members.none { it.uid == uid }) return // already gone, nothing to hand over
        val tasks = flatRef.collection("tasks").get().await().documents.map { doc ->
            LeaveTask(
                taskId = doc.id,
                assignedUid = doc.getString("currentAssignedUserId").orEmpty(),
                queueOrder = (doc.get("queueOrder") as? List<*>)?.mapNotNull { it?.toString() }.orEmpty(),
            )
        }
        val plan = planLeave(uid, members, tasks)
        plan.taskChanges.forEach { change ->
            flatRef.collection("tasks").document(change.taskId).update(
                mapOf("currentAssignedUserId" to change.newAssignee, "queueOrder" to change.newQueue)
            ).await()
        }
        plan.promoteUid?.let { successor ->
            flatRef.collection("members").document(successor).update("role", "admin").await()
            flatRef.update("adminUid", successor).await()
        }
        val leaverIsAdmin = members.any { it.uid == uid && it.role == "admin" }
        if (plan.closeFlat && leaverIsAdmin) {
            // Last member out closes the flat. Only an admin may delete it; a lone non-admin just leaves.
            flatRef.delete().await()
        }
    }
}
