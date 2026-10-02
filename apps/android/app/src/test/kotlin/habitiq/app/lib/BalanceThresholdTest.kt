package habitiq.app.lib

import habitiq.app.data.FlatExpense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BalanceThresholdTest {
    private fun expense(share: Double) = FlatExpense(
        id = "e", description = "Tea", amount = share, paidBy = "me",
        splitAmong = listOf("me", "ana"), splits = mapOf("me" to 0.0, "ana" to share),
        date = "2026-10-01", createdBy = "me",
    )

    @Test fun `one paisa is a real balance and never reads as settled`() {
        val balances = pairwisePersonalBalances(listOf(expense(0.01)), emptyList(), "me")
        assertEquals(0.01, balances["ana"]!!, 1e-9)
    }

    @Test fun `floating point dust below half a paisa is ignored`() {
        assertTrue(pairwisePersonalBalances(listOf(expense(0.004)), emptyList(), "me").isEmpty())
    }

    @Test fun `forty rupees fifty paise is kept in full`() {
        val balances = pairwisePersonalBalances(listOf(expense(40.50)), emptyList(), "me")
        assertEquals(40.50, balances["ana"]!!, 1e-9)
    }
}
