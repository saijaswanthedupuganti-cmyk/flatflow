package habitiq.app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class ExpenseSummary(val totalSpent: Double, val expenseCount: Int)

class ExpensesRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeMonthlyExpenses(flatId: String, monthPrefix: String): Flow<ExpenseSummary> = callbackFlow {
        val registration = firestore.collection("flats").document(flatId)
            .collection("expenses")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(100)
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
}
