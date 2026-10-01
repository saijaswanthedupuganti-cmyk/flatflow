package habitiq.app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import habitiq.app.lib.computeEqualSplits
import habitiq.app.lib.currentMonthKey
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class BillsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val activityRepository: ActivityRepository = ActivityRepository()
) {
    fun observeRecurringBills(flatId: String): Flow<List<RecurringBill>> = callbackFlow {
        val reg = firestore.collection("flats").document(flatId).collection("recurringBills")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents.orEmpty().mapNotNull { it.toRecurringBill() })
            }
        awaitClose { reg.remove() }
    }

    fun observeBillInstances(flatId: String): Flow<List<BillInstance>> = callbackFlow {
        val reg = firestore.collection("flats").document(flatId).collection("billInstances")
            .orderBy("generatedAt", Query.Direction.DESCENDING).limit(200)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents.orEmpty().mapNotNull { it.toBillInstance() })
            }
        awaitClose { reg.remove() }
    }

    fun observeSettlements(flatId: String): Flow<List<Settlement>> = callbackFlow {
        val reg = firestore.collection("flats").document(flatId).collection("settlements")
            .orderBy("date", Query.Direction.DESCENDING).limit(100)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents.orEmpty().mapNotNull { it.toSettlement() })
            }
        awaitClose { reg.remove() }
    }

    suspend fun createRecurringBill(flatId: String, bill: RecurringBill): Result<Unit> = runCatching {
        firestore.collection("flats").document(flatId).collection("recurringBills")
            .document(bill.id).set(bill.toFirestoreMap()).await()
    }

    suspend fun generateBill(
        flatId: String,
        bill: RecurringBill,
        amount: Double?,
        adminUid: String,
        memberUids: List<String>
    ): Result<Unit> = runCatching {
        val month = currentMonthKey()
        val participants = bill.participants?.takeIf { it.isNotEmpty() } ?: bill.rotationQueue.ifEmpty { memberUids }
        val payer = bill.rotationQueue.getOrElse(bill.currentPayerIndex % bill.rotationQueue.size.coerceAtLeast(1)) { adminUid }
        val finalAmount = if (bill.isVariable) amount else bill.amount
        val splits = if (finalAmount != null && finalAmount > 0) computeEqualSplits(finalAmount, participants) else null
        val status = if (splits != null) "split_generated" else "pending"
        val dueDay = bill.billingDay.coerceIn(1, 28)
        val instance = BillInstance(
            id = UUID.randomUUID().toString(),
            templateId = bill.id,
            month = month,
            name = bill.name,
            category = bill.category,
            currency = bill.currency,
            amount = finalAmount,
            paidBy = payer,
            participants = participants,
            splits = splits,
            status = status,
            dueDate = "$month-${dueDay.toString().padStart(2, '0')}",
            generatedAt = Instant.now().toString(),
            generatedBy = adminUid,
            collectorId = bill.collectorId
        )
        firestore.collection("flats").document(flatId).collection("billInstances")
            .document(instance.id).set(instance.toFirestoreMap()).await()
        val nextIndex = if (bill.rotationQueue.isNotEmpty()) (bill.currentPayerIndex + 1) % bill.rotationQueue.size else 0
        firestore.collection("flats").document(flatId).collection("recurringBills").document(bill.id)
            .update(mapOf("lastGeneratedMonth" to month, "currentPayerIndex" to nextIndex)).await()
        activityRepository.addActivity(flatId, adminUid, "bill_generated", "generated bill \"${bill.name}\" for $month").getOrThrow()
    }

    suspend fun markBillPaid(flatId: String, instanceId: String): Result<Unit> = runCatching {
        firestore.collection("flats").document(flatId).collection("billInstances").document(instanceId)
            .update(mapOf("status" to "paid", "paidAt" to Instant.now().toString())).await()
    }

    suspend fun skipBillInstance(flatId: String, instanceId: String, reason: String = ""): Result<Unit> = runCatching {
        firestore.collection("flats").document(flatId).collection("billInstances").document(instanceId)
            .update(mapOf("status" to "skipped", "skippedReason" to reason)).await()
    }

    suspend fun addSettlement(flatId: String, settlement: Settlement, actorUid: String): Result<Unit> = runCatching {
        val s = settlement.copy(
            id = settlement.id.ifEmpty { UUID.randomUUID().toString() },
            createdAt = settlement.createdAt.ifEmpty { Instant.now().toString() },
            date = settlement.date.ifEmpty { LocalDate.now().toString() }
        )
        firestore.collection("flats").document(flatId).collection("settlements")
            .document(s.id).set(s.toFirestoreMap()).await()
        activityRepository.addActivity(flatId, actorUid, "settlement_recorded", "recorded settlement of ₹${s.amount}").getOrThrow()
    }

    fun observeMonthCycles(flatId: String): Flow<List<MonthCycle>> = callbackFlow {
        val reg = firestore.collection("flats").document(flatId).collection("monthCycles")
            .orderBy("month", Query.Direction.DESCENDING).limit(12)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents.orEmpty().mapNotNull { it.toMonthCycle() })
            }
        awaitClose { reg.remove() }
    }

    suspend fun closeMonth(
        flatId: String,
        cycle: MonthCycle,
        adminUid: String
    ): Result<Unit> = runCatching {
        firestore.collection("flats").document(flatId).collection("monthCycles")
            .document(cycle.id).set(cycle.toFirestoreMap()).await()
        activityRepository.addActivity(
            flatId, adminUid, "settlement_added",
            "closed ${cycle.month} — month-end close recorded"
        ).getOrThrow()
    }

    suspend fun markBillCollected(
        flatId: String,
        instanceId: String,
        memberUid: String,
        collected: Boolean
    ): Result<Unit> = runCatching {
        firestore.collection("flats").document(flatId).collection("billInstances").document(instanceId)
            .update(mapOf("collectedFrom.$memberUid" to collected)).await()
    }

    suspend fun updateRecurringBill(flatId: String, billId: String, updates: Map<String, Any?>): Result<Unit> = runCatching {
        firestore.collection("flats").document(flatId).collection("recurringBills").document(billId)
            .update(updates.filterValues { it != null }).await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toMonthCycle(): MonthCycle? {
        val carryRaw = get("carryForwardOut") as? Map<*, *>
        val balances = (carryRaw?.get("balances") as? Map<*, *>)?.mapNotNull { (k, v) ->
            val key = k?.toString() ?: return@mapNotNull null
            val amt = (v as? Number)?.toDouble() ?: return@mapNotNull null
            key to amt
        }?.toMap()
        return MonthCycle(
            id = getString("id") ?: id,
            month = getString("month").orEmpty(),
            status = getString("status") ?: "open",
            closedAt = getString("closedAt"),
            totalBillsINR = getDouble("totalBillsINR") ?: 0.0,
            totalExpensesINR = getDouble("totalExpensesINR") ?: 0.0,
            totalSettledINR = getDouble("totalSettledINR") ?: 0.0,
            carryForwardOut = balances
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toRecurringBill(): RecurringBill? = RecurringBill(
        id = getString("id") ?: id,
        name = getString("name").orEmpty(),
        category = getString("category") ?: "bills",
        amount = getDouble("amount"),
        currency = getString("currency") ?: "INR",
        billingDay = getLong("billingDay")?.toInt() ?: 1,
        rotationQueue = (get("rotationQueue") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
        currentPayerIndex = getLong("currentPayerIndex")?.toInt() ?: 0,
        payerMode = getString("payerMode") ?: "rotation",
        fixedPayerUid = getString("fixedPayerUid"),
        participants = (get("participants") as? List<*>)?.mapNotNull { it?.toString() },
        splitMethod = getString("splitMethod") ?: "equal",
        isVariable = getBoolean("isVariable") ?: false,
        active = getBoolean("active") ?: true,
        createdBy = getString("createdBy").orEmpty(),
        createdAt = getString("createdAt").orEmpty(),
        lastGeneratedMonth = getString("lastGeneratedMonth").orEmpty(),
        collectorId = getString("collectorId")
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toBillInstance(): BillInstance? {
        val splitsRaw = get("splits") as? Map<*, *>
        val splits = splitsRaw?.mapNotNull { (k, v) ->
            val key = k?.toString() ?: return@mapNotNull null
            val amt = (v as? Number)?.toDouble() ?: return@mapNotNull null
            key to amt
        }?.toMap()
        val collectedRaw = get("collectedFrom") as? Map<*, *>
        val collectedFrom = collectedRaw?.mapNotNull { (k, v) ->
            val key = k?.toString() ?: return@mapNotNull null
            key to (v as? Boolean ?: false)
        }?.toMap()
        return BillInstance(
            id = getString("id") ?: id,
            templateId = getString("templateId").orEmpty(),
            month = getString("month").orEmpty(),
            name = getString("name").orEmpty(),
            category = getString("category") ?: "bills",
            currency = getString("currency") ?: "INR",
            amount = getDouble("amount"),
            paidBy = getString("paidBy").orEmpty(),
            participants = (get("participants") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            splits = splits,
            status = getString("status") ?: "pending",
            dueDate = getString("dueDate").orEmpty(),
            paidAt = getString("paidAt"),
            skippedReason = getString("skippedReason"),
            generatedAt = getString("generatedAt").orEmpty(),
            generatedBy = getString("generatedBy").orEmpty(),
            collectorId = getString("collectorId"),
            collectedFrom = collectedFrom
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toSettlement(): Settlement? = Settlement(
        id = getString("id") ?: id,
        fromUserId = getString("fromUserId").orEmpty(),
        toUserId = getString("toUserId").orEmpty(),
        amount = getDouble("amount") ?: 0.0,
        currency = getString("currency") ?: "INR",
        note = getString("note"),
        date = getString("date").orEmpty(),
        month = getString("month"),
        type = getString("type") ?: "immediate",
        createdAt = getString("createdAt").orEmpty()
    )
}
