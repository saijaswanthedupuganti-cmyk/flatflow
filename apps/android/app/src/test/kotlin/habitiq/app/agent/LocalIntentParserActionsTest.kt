package habitiq.app.agent

import habitiq.app.agent.Fixtures.allUids
import habitiq.app.agent.Fixtures.state
import habitiq.app.agent.Fixtures.today
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalIntentParserActionsTest {
    private fun parse(text: String, s: HouseholdState = state) = LocalIntentParser.parse(text, s, today)

    private fun step(text: String): AgentStep {
        val r = parse(text)
        assertTrue("$text -> $r", r is ParseResult.Confident && r.plan is AgentPlan.Actions)
        return ((r as ParseResult.Confident).plan as AgentPlan.Actions).steps.single()
    }

    private fun expense(title: String, paise: Long, category: String, payer: String = "u1", split: List<String> = allUids) =
        AgentStep.AddExpense(title, paise, category, payer, split)

    @Test fun `expenses`() {
        assertEquals(expense("Groceries", 56_000, "lifestyle"), step("Bought groceries for ₹560"))
        assertEquals(expense("Kirana", 45_000, "lifestyle"), step("kirana 450"))
        assertEquals(expense("Swiggy", 120_000, "lifestyle"), step("spent 1.2k on swiggy"))
        assertEquals(expense("Electricity bill", 145_000, "bills"), step("paid electricity bill 1,450"))
        assertEquals(expense("Rent", 800_000, "bills"), step("rent 8000"))
        assertEquals(expense("Milk", 6_000, "lifestyle", payer = "u2"), step("Ravi bought milk 60"))
        assertEquals(expense("Vegetables", 30_000, "lifestyle", split = listOf("u1", "u3")), step("bought vegetables 300 split with priya"))
        assertEquals(expense("Gas cylinder", 110_000, "bills", split = listOf("u1", "u2")), step("gas cylinder 1100 between me and ravi"))
        assertEquals(expense("Detergent", 25_000, "chores"), step("bought detergent for two hundred and fifty rupees"))
        assertEquals(expense("Dinner", 64_000, "lifestyle", payer = "u2"), step("Ravi paid 640 for dinner"))
    }

    @Test fun `lazy spend with no subject still records`() {
        assertEquals(expense("Expense", 50_000, "other"), step("I just spent 500"))
        assertEquals(expense("Expense", 50_000, "other"), step("spent 500"))
        assertEquals(expense("Expense", 20_000, "other"), step("aaj 200 kharcha hua"))
    }

    @Test fun `settlements`() {
        assertEquals(AgentStep.Settle("u1", "u2", 20_000), step("paid ravi 200"))
        assertEquals(AgentStep.Settle("u1", "u3", 15_000), step("Gave Priya ₹150"))
        assertEquals(AgentStep.Settle("u4", "u1", 30_000), step("Arjun paid me 300"))
        assertEquals(AgentStep.Settle("u2", "u1", 50_000), step("got 500 from ravi"))
        assertEquals(AgentStep.Settle("u1", "u3", 25_000), step("sent 250 to priyaa"))
        assertEquals(AgentStep.Settle("u1", "u2", 100_000), step("I paid Ravi back 1k"))
    }

    @Test fun `ambiguous names ask instead of guessing`() {
        val raviT = AgentMember("u5", "Ravi", "Ravi Teja")
        val r = parse("paid ravi 200", state.copy(members = state.members + raviT))
        val clarify = (r as ParseResult.Confident).plan as AgentPlan.Clarify
        assertEquals("Which Ravi?", clarify.question)
        assertEquals(listOf("Ravi Kumar", "Ravi Teja"), clarify.options.map { it.label })
        val chosen = (clarify.options[1].plan as AgentPlan.Actions).steps.single()
        assertEquals(AgentStep.Settle("u1", "u5", 20_000), chosen)
    }

    @Test fun `questions never become actions`() {
        listOf("what did i spend on groceries 500?", "how much is rent 8000", "did ravi pay 200", "show expenses over 500")
            .forEach { assertEquals(it, ParseResult.NeedsModel, parse(it)) }
    }

    @Test fun `incomplete or unclear money commands need the model`() {
        listOf(
            "paid 300",                           // paid what, or whom?
            "bought groceries 200 and milk 50",   // two amounts
            "bought milk 0.5",                    // under ₹1
            "rent 200000",                        // over ₹1,00,000
            "paid 2 days ago 300",                // no subject
            "bought milk 60 split with xyzzy",    // unknown person in split
        ).forEach { assertEquals(it, ParseResult.NeedsModel, parse(it)) }
    }

    @Test fun `not in a flat`() {
        val r = parse("bought milk 60", HouseholdState.Empty.copy(myUid = "u1"))
        assertEquals(ParseResult.Confident(AgentPlan.Unsupported("Join or create a flat first to track money.")), r)
    }

    @Test fun `summaries read plainly`() {
        val r = parse("Bought groceries for ₹560") as ParseResult.Confident
        assertEquals("Add ₹560 for Groceries, split 4 ways", (r.plan as AgentPlan.Actions).summary)
        val s = parse("paid ravi 200") as ParseResult.Confident
        assertEquals("Record ₹200 you paid Ravi", (s.plan as AgentPlan.Actions).summary)
    }
}
