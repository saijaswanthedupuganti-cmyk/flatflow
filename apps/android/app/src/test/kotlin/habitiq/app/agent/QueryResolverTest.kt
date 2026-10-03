package habitiq.app.agent

import habitiq.app.agent.Fixtures.state
import habitiq.app.agent.Fixtures.today
import org.junit.Assert.assertEquals
import org.junit.Test

class QueryResolverTest {
    // Same personal, all-time view as the Expenses screen: other uid -> + they owe me, - I owe them.
    private val owing = state.copy(myBalances = mapOf("u2" to -350.0, "u3" to 150.0))
    private fun task(id: String, who: String, due: java.time.LocalDate?, status: String = "pending") =
        AgentTask(id, "Task $id", who, due, "weekly", status)

    @Test fun `my balance when I owe`() {
        val a = QueryResolver.resolve(AgentQuery.MyBalance, owing, today)
        assertEquals("You owe ₹350", a.headline)
        assertEquals(listOf(AnswerLine("Pay Ravi", "₹350"), AnswerLine("Priya owes you", "₹150")), a.lines)
        assertEquals(AgentLink.BALANCES, a.link)
    }

    @Test fun `my balance when owed and when settled`() {
        assertEquals("You're owed ₹150", QueryResolver.resolve(AgentQuery.MyBalance, state.copy(myBalances = mapOf("u3" to 150.0)), today).headline)
        assertEquals("You're all settled", QueryResolver.resolve(AgentQuery.MyBalance, state, today).headline)
    }

    @Test fun `who owes lists people who owe me largest first`() {
        val s = state.copy(myBalances = mapOf("u2" to -350.0, "u3" to 150.0, "u4" to 400.0))
        val a = QueryResolver.resolve(AgentQuery.WhoOwes, s, today)
        assertEquals("2 people owe you", a.headline)
        assertEquals(listOf(AnswerLine("Arjun", "₹400"), AnswerLine("Priya", "₹150")), a.lines)
        assertEquals("Nobody owes you anything right now", QueryResolver.resolve(AgentQuery.WhoOwes, state, today).headline)
    }

    @Test fun `due today shows my open and overdue tasks`() {
        val s = state.copy(tasks = listOf(
            task("1", "u1", today), task("2", "u1", today.minusDays(1)), task("3", "u2", today),
            task("4", "u1", today, status = "completed"), task("5", "u1", today.plusDays(1)),
        ))
        val a = QueryResolver.resolve(AgentQuery.DueToday, s, today)
        assertEquals("2 things for you today", a.headline)
        assertEquals(listOf(AnswerLine("Task 1", "Due today"), AnswerLine("Task 2", "Overdue")), a.lines)
        assertEquals(AgentLink.TASKS, a.link)
        assertEquals("Nothing due today", QueryResolver.resolve(AgentQuery.DueToday, state, today).headline)
    }

    @Test fun `my duties in a range`() {
        val s = state.copy(tasks = listOf(task("1", "u1", today.plusDays(1)), task("2", "u1", today.plusDays(9)), task("3", "u2", today.plusDays(1))))
        val a = QueryResolver.resolve(AgentQuery.MyDuties(today, today.plusDays(6)), s, today)
        assertEquals("1 task coming up", a.headline)
        assertEquals(listOf(AnswerLine("Task 1", "Sun, 4 Oct")), a.lines)
    }

    @Test fun `bills shows unpaid bills this month`() {
        val s = state.copy(bills = listOf(
            AgentBill("Wifi", 99_900, today.plusDays(2), "u1", "pending"),
            AgentBill("Rent", null, today.plusDays(5), "u2", "split_generated"),
            AgentBill("Gas", 110_000, today, "u1", "paid"),
            AgentBill("Water", 30_000, today, "u1", "skipped"),
        ))
        val a = QueryResolver.resolve(AgentQuery.Bills, s, today)
        assertEquals("2 bills still open", a.headline)
        assertEquals(listOf(AnswerLine("Wifi", "₹999 · Mon, 5 Oct"), AnswerLine("Rent", "Amount not set · Thu, 8 Oct")), a.lines)
        assertEquals(AgentLink.BILLS, a.link)
    }

    @Test fun `find flats filters and sorts by rent`() {
        val s = state.copy(vacancies = listOf(
            AgentVacancy("a", "Green Nest", "Gachibowli", "Hyderabad", 950_000, "female"),
            AgentVacancy("b", "Blue Door", "Gachibowli", "Hyderabad", 800_000, "any"),
            AgentVacancy("c", "Pricey", "Gachibowli", "Hyderabad", 1_500_000, "any"),
            AgentVacancy("d", "Elsewhere", "Kondapur", "Hyderabad", 700_000, "any"),
            AgentVacancy("e", "Boys only", "Gachibowli", "Hyderabad", 700_000, "male"),
        ))
        val a = QueryResolver.resolve(AgentQuery.FindFlats("gachibowli", null, 10_000, "female"), s, today)
        assertEquals("2 places match", a.headline)
        assertEquals(listOf(AnswerLine("Blue Door", "Gachibowli · ₹8,000/head"), AnswerLine("Green Nest", "Gachibowli · ₹9,500/head")), a.lines)
        assertEquals(AgentLink.DISCOVER, a.link)
        assertEquals("No places match yet", QueryResolver.resolve(AgentQuery.FindFlats("ameerpet", null, null, null), s, today).headline)
    }

    @Test fun `answers without a flat`() {
        val none = HouseholdState.Empty.copy(myUid = "u1")
        listOf(AgentQuery.MyBalance, AgentQuery.WhoOwes, AgentQuery.DueToday, AgentQuery.Bills).forEach {
            val a = QueryResolver.resolve(it, none, today)
            assertEquals("Join a flat to see this", a.headline)
            assertEquals(null, a.link)
        }
    }

    @Test fun `builds expense and payment cards`() {
        val exp = AgentPlan.Actions(listOf(AgentStep.AddExpense("Groceries", 56_000, "lifestyle", "u1", Fixtures.allUids)), "x")
        assertEquals(
            AgentCard.Expense("Groceries", 56_000, "lifestyle", "You", listOf("You", "Ravi", "Priya", "Arjun"), everyone = true),
            cardFor(exp, state),
        )
        val part = AgentPlan.Actions(listOf(AgentStep.AddExpense("Milk", 6_000, "lifestyle", "u2", listOf("u1", "u2"))), "x")
        val partCard = cardFor(part, state) as AgentCard.Expense
        assertEquals("Ravi", partCard.paidBy)
        assertEquals(listOf("You", "Ravi"), partCard.splitNames)
        assertEquals(false, partCard.everyone)
        val settle = AgentPlan.Actions(listOf(AgentStep.Settle("u1", "u2", 20_000)), "x")
        assertEquals(AgentCard.Payment("You", "Ravi", 20_000), cardFor(settle, state))
    }
}
