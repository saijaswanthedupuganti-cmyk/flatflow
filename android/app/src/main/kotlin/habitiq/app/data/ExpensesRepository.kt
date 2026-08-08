package habitiq.app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class ExpenseSummary(val totalSpent: Double, val expenseCount: Int)

class ExpensesRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val activityRepository: ActivityRepository = ActivityRepository()
) {
    fun observeExpenses(flatId: String): Flow<List<FlatExpense>> = callbackFlow {
        val registration = firestore.collection("flats").document(flatId)
            .collection("expenses")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().mapNotNull { doc -> doc.toFlatExpense() }
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    fun observeMonthlyExpenses(flatId: String, monthPrefix: String): Flow<ExpenseSummary> = callbackFlow {
        val registration = firestore.collection("flats").document(flatId)
            .collection("expenses")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                var total = 0.0
                var count = 0
                snapshot?.documents.orEmpty().forEach { doc ->
                    val date = doc.getString("date").orEmpty()
                    if (date.startsWith(monthPrefix)) {
                        total += doc.getDouble("amount") ?: 0.0
                        count++
                    }
                }
                trySend(ExpenseSummary(totalSpent = total, expenseCount = count))
            }
        awaitClose { registration.remove() }
    }

    suspend fun addExpense(flatId: String, data: FlatExpense): Result<Unit> = runCatching {
        val expense = data.copy(
            id = data.id.ifEmpty { UUID.randomUUID().toString() },
            createdAt = data.createdAt.ifEmpty { Instant.now().toString() }
        )
        firestore.collection("flats").document(flatId).collection("expenses")
            .document(expense.id).set(expense.toFirestoreMap()).await()
        activityRepository.addActivity(
            flatId = flatId,
            userId = expense.createdBy,
            action = "expense_added",
            details = "added ₹${expense.amount} for \"${expense.description}\"${if (expense.splitAmong.size > 1) ", split ${expense.splitAmong.size} ways" else ""}"
        ).getOrThrow()
    }

    suspend fun deleteExpense(flatId: String, expenseId: String, actorId: String): Result<Unit> = runCatching {
        firestore.collection("flats").document(flatId).collection("expenses").document(expenseId).delete().await()
        activityRepository.addActivity(
            flatId = flatId,
            userId = actorId,
            action = "expense_deleted",
            details = "deleted an expense"
        ).getOrThrow()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toFlatExpense(): FlatExpense? {
        val splitsRaw = get("splits") as? Map<*, *> ?: emptyMap<Any, Any>()
        val splits = splitsRaw.mapNotNull { (k, v) ->
            val key = k?.toString() ?: return@mapNotNull null
            val amount = (v as? Number)?.toDouble() ?: return@mapNotNull null
            key to amount
        }.toMap()
        val splitAmong = (get("splitAmong") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        return FlatExpense(
            id = getString("id") ?: id,
            description = getString("description").orEmpty(),
            amount = getDouble("amount") ?: 0.0,
            currency = getString("currency") ?: "INR",
            paidBy = getString("paidBy").orEmpty(),
            splitAmong = splitAmong,
            splitType = getString("splitType") ?: "equal",
            splits = splits,
            category = getString("category") ?: "other",
            date = getString("date") ?: LocalDate.now().toString(),
            createdBy = getString("createdBy").orEmpty(),
            createdAt = getString("createdAt").orEmpty()
        )
    }
}
