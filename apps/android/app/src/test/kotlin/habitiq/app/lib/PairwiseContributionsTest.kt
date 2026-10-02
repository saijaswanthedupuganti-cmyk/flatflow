package habitiq.app.lib

import habitiq.app.data.FlatExpense
import habitiq.app.data.Settlement
import org.junit.Assert.assertEquals
import org.junit.Test

class PairwiseContributionsTest {
    private fun expense(id: String, paidBy: String, splits: Map<String, Double>, date: String = "2026-10-01") = FlatExpense(
        id = id, description = "Item $id", amount = splits.values.sum(), paidBy = paidBy,
        splitAmong = splits.keys.toList(), splits = splits, date = date, createdBy = paidBy,
    )

    private fun settlement(from: String, to: String, amount: Double) =
        Settlement(id = "s-$from-$to", fromUserId = from, toUserId = to, amount = amount, date = "2026-10-02")

    @Test fun `contributions sum to the headline balance for every counterparty`() {
        val expenses = listOf(
            expense("1", "me", mapOf("me" to 300.0, "ana" to 300.0, "raj" to 300.0)),
            expense("2", "ana", mapOf("me" to 150.0, "ana" to 150.0)),
            expense("3", "raj", mapOf("me" to 90.0, "raj" to 90.0, "ana" to 90.0)),
            expense("4", "ana", mapOf("ana" to 80.0, "raj" to 80.0)), // unrelated to me
        )
        val settlements = listOf(settlement("me", "ana", 100.0), settlement("raj", "me", 40.0))
        val balances = pairwisePersonalBalances(expenses, settlements, "me")
        listOf("ana", "raj").forEach { other ->
            val sum = pairwiseContributions(expenses, settlements, "me", other).sumOf { it.amount }
            assertEquals("balance with $other", balances[other] ?: 0.0, sum, 0.0001)
        }
    }

    @Test fun `unrelated expenses never appear`() {
        val expenses = listOf(expense("4", "ana", mapOf("ana" to 80.0, "raj" to 80.0)))
        assertEquals(emptyList<PairContribution>(), pairwiseContributions(expenses, emptyList(), "me", "ana"))
    }

    @Test fun `settlements are labelled and signed from the viewer's side`() {
        val lines = pairwiseContributions(emptyList(), listOf(settlement("me", "ana", 100.0), settlement("ana", "me", 30.0)), "me", "ana")
        assertEquals(listOf(100.0, -30.0), lines.map { it.amount })
        assertEquals(true, lines.all { it.isSettlement && it.label == "Settlement recorded" })
    }
}
