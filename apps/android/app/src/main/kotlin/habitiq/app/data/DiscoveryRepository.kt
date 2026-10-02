package habitiq.app.data

import com.google.firebase.firestore.FirebaseFirestore
import habitiq.app.discover.ConnectionStatus
import habitiq.app.discover.DiscoveryConnection
import habitiq.app.discover.DiscoveryReport
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.nio.charset.StandardCharsets
import java.util.UUID

internal fun connectionDocumentId(firstUid: String, secondUid: String, context: String): String {
    val participants = listOf(firstUid, secondUid).sorted().joinToString(":")
    return UUID.nameUUIDFromBytes("$participants:$context".toByteArray(StandardCharsets.UTF_8)).toString()
}

private fun parseDiscoveryStringList(raw: Any?): List<String> = when (raw) {
    is List<*> -> raw.mapNotNull { it?.toString()?.trim() }.filter { it.isNotEmpty() }
    is String -> raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    else -> emptyList()
}

/** Pure public-projection parser so legacy and current vacancy documents can be regression tested. */
internal fun parseVacancyListingDocument(flatId: String, data: Map<String, Any?>): VacancyListing? {
    val vacancy = data["vacancy"] as? Map<*, *> ?: return null
    val active = vacancy["active"] as? Boolean ?: false
    if (!active) return null

    return VacancyListing(
        flatId = flatId,
        flatName = data["name"]?.toString() ?: flatId,
        active = true,
        city = vacancy["city"]?.toString().orEmpty(),
        area = vacancy["area"]?.toString().orEmpty(),
        rentPerHead = (vacancy["rentPerHead"] as? Number)?.toDouble(),
        currency = vacancy["currency"]?.toString() ?: "INR",
        bedsAvailable = (vacancy["bedsAvailable"] as? Number)?.toInt() ?: 1,
        preferredGender = vacancy["preferredGender"]?.toString() ?: "any",
        about = vacancy["about"]?.toString().orEmpty(),
        flatType = data["flatType"]?.toString().orEmpty(),
        adminUid = data["adminUid"]?.toString().orEmpty(),
        memberCount = (data["memberCount"] as? Number)?.toInt() ?: 0,
        lifestyle = parseDiscoveryStringList(vacancy["lifestyle"]),
        customTags = parseDiscoveryStringList(vacancy["customTags"]),
        existingMembersGender = vacancy["existingMembersGender"]?.toString(),
        roomType = vacancy["roomType"]?.toString()?.trim()?.lowercase()
            ?.takeIf { it == "private" || it == "shared" },
        securityDeposit = (vacancy["securityDeposit"] as? Number)?.toDouble(),
        availableFrom = vacancy["availableFrom"]?.toString().orEmpty(),
        noticePeriod = vacancy["noticePeriod"]?.toString().orEmpty(),
        furnishing = vacancy["furnishing"]?.toString().orEmpty(),
        amenities = parseDiscoveryStringList(vacancy["amenities"]),
        preferredOccupation = vacancy["preferredOccupation"]?.toString().orEmpty(),
        preferredBudgetMin = (vacancy["preferredBudgetMin"] as? Number)?.toDouble(),
        preferredBudgetMax = (vacancy["preferredBudgetMax"] as? Number)?.toDouble(),
        moveInTiming = vacancy["moveInTiming"]?.toString().orEmpty(),
        approximateLocationOnly = vacancy["approximateLocationOnly"] as? Boolean ?: true,
        photoUrls = parseDiscoveryStringList(vacancy["photoUrls"]),
        updatedAt = vacancy["updatedAt"]?.toString().orEmpty(),
        postStatus = vacancy["status"]?.toString(),
        health = habitiq.app.discover.FlatHealthSnapshot.parse(data["discoveryPublic"]),
        approxLocation = habitiq.app.discover.ApproxNeighborhoodLocation.parse(
            data[habitiq.app.discover.ApproxNeighborhoodLocation.FIELD_ON_FLAT]
        )
    )
}

class DiscoveryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /** Real-time vacancies — client-filters vacancy.active on embedded flat.vacancy map. */
    fun observeActiveVacancies(): Flow<List<VacancyListing>> = callbackFlow {
        val registration = firestore.collection("flats")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val listings = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    parseVacancyListingDocument(doc.id, doc.data ?: return@mapNotNull null)
                }
                trySend(listings)
            }
        awaitClose { registration.remove() }
    }

    fun observeConnections(uid: String): Flow<List<DiscoveryConnection>> {
        val from = observeConnectionsWhere("fromUid", uid)
        val to = observeConnectionsWhere("toUid", uid)
        return combine(from, to) { a, b ->
            (a + b).distinctBy { it.id }
        }
    }

    private fun observeConnectionsWhere(field: String, uid: String): Flow<List<DiscoveryConnection>> = callbackFlow {
        val reg = firestore.collection("discoveryConnections")
            .whereEqualTo(field, uid)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                trySend(snap?.documents.orEmpty().mapNotNull { it.toConnection() })
            }
        awaitClose { reg.remove() }
    }

    fun observeBlockedIds(uid: String): Flow<Set<String>> = callbackFlow {
        val reg = firestore.collection("users").document(uid).collection("blocked")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                trySend(snap?.documents.orEmpty().map { it.id }.toSet())
            }
        awaitClose { reg.remove() }
    }

    suspend fun sendConnectionRequest(
        fromUid: String,
        toUid: String,
        message: String,
        listingFlatId: String?,
        seekerId: String?
    ): Result<String> = runCatching {
        require(fromUid.isNotBlank() && toUid.isNotBlank() && fromUid != toUid) { "Choose another Oddroof member." }
        val context = listingFlatId ?: seekerId ?: "direct"
        val id = connectionDocumentId(fromUid, toUid, context)
        val now = Instant.now().toString()
        val doc = mapOf(
            "id" to id,
            "fromUid" to fromUid,
            "toUid" to toUid,
            "listingFlatId" to listingFlatId,
            "seekerId" to seekerId,
            "message" to message.trim(),
            "status" to ConnectionStatus.REQUEST_SENT.name,
            "createdAt" to now,
            "updatedAt" to now
        )
        val ref = firestore.collection("discoveryConnections").document(id)
        firestore.runTransaction { transaction ->
            if (transaction.get(ref).exists()) {
                throw IllegalStateException("A connection request already exists for this profile.")
            }
            transaction.set(ref, doc)
        }.await()
        id
    }

    suspend fun updateConnectionStatus(connectionId: String, status: ConnectionStatus): Result<Unit> = runCatching {
        firestore.collection("discoveryConnections").document(connectionId)
            .update(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to Instant.now().toString()
                )
            ).await()
    }

    suspend fun submitReport(report: DiscoveryReport): Result<Unit> = runCatching {
        firestore.collection("discoveryReports").document(report.id).set(
            mapOf(
                "id" to report.id,
                "reporterUid" to report.reporterUid,
                "targetUid" to report.targetUid,
                "listingFlatId" to report.listingFlatId,
                "reason" to report.reason,
                "createdAt" to report.createdAt
            )
        ).await()
    }

    suspend fun blockUser(uid: String, targetId: String): Result<Unit> = runCatching {
        firestore.collection("users").document(uid).collection("blocked").document(targetId)
            .set(mapOf("targetId" to targetId, "createdAt" to Instant.now().toString())).await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toConnection(): DiscoveryConnection? {
        val status = ConnectionStatus.entries.find { it.name == getString("status") }
            ?: ConnectionStatus.REQUEST_SENT
        return DiscoveryConnection(
            id = getString("id") ?: id,
            fromUid = getString("fromUid").orEmpty(),
            toUid = getString("toUid").orEmpty(),
            listingFlatId = getString("listingFlatId"),
            seekerId = getString("seekerId"),
            message = getString("message").orEmpty(),
            status = status,
            createdAt = getString("createdAt").orEmpty(),
            updatedAt = getString("updatedAt").orEmpty()
        )
    }
}
