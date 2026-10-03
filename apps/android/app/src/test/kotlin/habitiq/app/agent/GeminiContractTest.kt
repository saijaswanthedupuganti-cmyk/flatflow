package habitiq.app.agent

import habitiq.app.agent.Fixtures.today
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiContractTest {
    private val state = Fixtures.state.copy(
        tasks = listOf(
            AgentTask("taskA", "Wash dishes", "u1", today, "daily", "pending"),
            AgentTask("taskB", "Clean bathroom", "u2", today, "weekly", "pending"),
        ),
        myBalances = mapOf("u2" to -350.0),
    )
    private val ctx = buildPlannerContext(state, today)

    @Test fun `context uses aliases and first names only`() {
        assertTrue(ctx.json.contains("\"m1\""))
        assertTrue(ctx.json.contains("Sai"))
        assertFalse(ctx.json.contains("Ravi Kumar")) // full names never leave the phone
        assertFalse(ctx.json.contains("u2"))         // real ids never leave the phone
        assertEquals("u1", ctx.members["m1"])        // the person is always m1
        assertTrue(ctx.json.length <= 8_000)
    }

    private fun parse(json: String) = parseModelPlan(json, ctx, state, today)

    @Test fun `expense with split and payer`() {
        val plan = parse("""{"type":"expense","title":"Groceries","amount":560,"category":"lifestyle","paidBy":"m1","splitWith":"all"}""") as AgentPlan.Actions
        assertEquals(AgentStep.AddExpense("Groceries", 56_000, "lifestyle", "u1", Fixtures.allUids), plan.steps.single())
        assertEquals("Add ₹560 for Groceries, split 4 ways", plan.summary)
        val m2 = ctx.members.entries.first { it.value == "u3" }.key
        val two = parse("""{"type":"expense","title":"Milk","amount":60,"splitWith":["m1","$m2"]}""") as AgentPlan.Actions
        assertEquals(listOf("u1", "u3"), (two.steps.single() as AgentStep.AddExpense).splitAmongUids)
    }

    @Test fun `settle must involve the person`() {
        val ravi = ctx.members.entries.first { it.value == "u2" }.key
        val plan = parse("""{"type":"settle","from":"m1","to":"$ravi","amount":200}""") as AgentPlan.Actions
        assertEquals(AgentStep.Settle("u1", "u2", 20_000), plan.steps.single())
        val other = ctx.members.entries.first { it.value == "u3" }.key
        assertNull(parse("""{"type":"settle","from":"$ravi","to":"$other","amount":200}"""))
    }

    @Test fun `tasks only my own and admin rules`() {
        val mine = ctx.tasks.entries.first { it.value == "taskA" }.key
        val notMine = ctx.tasks.entries.first { it.value == "taskB" }.key
        assertEquals(AgentStep.CompleteTask("taskA", "Wash dishes"), (parse("""{"type":"task_done","task":"$mine"}""") as AgentPlan.Actions).steps.single())
        assertNull(parse("""{"type":"task_done","task":"$notMine"}"""))
        assertEquals(AgentStep.CreateTask("Clean fridge", "weekly"), (parse("""{"type":"add_task","name":"Clean fridge","frequency":"weekly"}""") as AgentPlan.Actions).steps.single())
        val member = parseModelPlan("""{"type":"add_task","name":"Clean fridge"}""", ctx, state.copy(isAdmin = false), today)
        assertTrue(member is AgentPlan.Unsupported)
    }

    @Test fun `questions navigation and replies`() {
        assertEquals(AgentPlan.Answer(AgentQuery.MyBalance), parse("""{"type":"question","query":"my_balance"}"""))
        assertEquals(AgentPlan.Answer(AgentQuery.FindFlats("kondapur", null, 9000, "female")), parse("""{"type":"find_flats","area":"Kondapur","maxRent":9000,"gender":"female"}"""))
        assertEquals(AgentLink.JOIN_FLAT, (parse("""{"type":"navigate","to":"join_flat"}""") as AgentPlan.Navigate).link)
        assertEquals("Ask your admin for the invite code.", (parse("""{"type":"reply","text":"Ask your admin for the invite code."}""") as AgentPlan.Reply).answer.headline)
        assertEquals(AgentStep.SetAway(true), (parse("""{"type":"away","away":true}""") as AgentPlan.Actions).steps.single())
    }

    @Test fun `bad or unsafe model output is rejected`() {
        assertNull(parse("not json"))
        assertNull(parse("""{"type":"expense","title":"Groceries","amount":0}"""))
        assertNull(parse("""{"type":"expense","title":"Groceries","amount":500000}"""))
        assertNull(parse("""{"type":"expense","title":"Milk","amount":60,"splitWith":["m9"]}"""))
        assertNull(parse("""{"type":"delete_flat"}"""))
        assertEquals(ContentGuard.REFUSAL, (parse("""{"type":"expense","title":"ganja","amount":500}""") as AgentPlan.Unsupported).reason)
        assertEquals(ContentGuard.REFUSAL, (parse("""{"type":"reply","text":"you idiot"}""") as AgentPlan.Unsupported).reason)
    }

    @Test fun `json wrapped in code fences still parses`() {
        val fenced = "```json\n{\"type\":\"question\",\"query\":\"bills\"}\n```"
        assertEquals(AgentPlan.Answer(AgentQuery.Bills), parse(fenced))
    }
}
