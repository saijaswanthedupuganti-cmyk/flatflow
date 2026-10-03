package habitiq.app.agent

import habitiq.app.agent.Fixtures.state
import habitiq.app.agent.Fixtures.today
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalIntentParserCommandsTest {
    private val withTasks = state.copy(
        tasks = listOf(
            AgentTask("t1", "Wash dishes", "u1", today, "daily", "pending"),
            AgentTask("t2", "Take out garbage", "u1", today.plusDays(1), "weekly", "pending"),
            AgentTask("t3", "Clean bathroom", "u2", today, "weekly", "pending"),
            AgentTask("t4", "Water plants", "u1", today, "weekly", "completed"),
        ),
    )

    private fun parse(text: String, s: HouseholdState = withTasks) = LocalIntentParser.parse(text, s, today)
    private fun plan(text: String, s: HouseholdState = withTasks): AgentPlan {
        val r = parse(text, s)
        assertTrue("$text -> $r", r is ParseResult.Confident)
        return (r as ParseResult.Confident).plan
    }
    private fun step(text: String, s: HouseholdState = withTasks) = (plan(text, s) as AgentPlan.Actions).steps.single()

    @Test fun `marking my task done`() {
        assertEquals(AgentStep.CompleteTask("t1", "Wash dishes"), step("done with the dishes"))
        assertEquals(AgentStep.CompleteTask("t1", "Wash dishes"), step("I finished washing dishes"))
        assertEquals(AgentStep.CompleteTask("t2", "Take out garbage"), step("garbage done"))
        assertEquals(AgentStep.CompleteTask("t2", "Take out garbage"), step("mark take out garbage as done"))
        assertEquals(AgentStep.CompleteTask("t1", "Wash dishes"), step("dishes ho gaya"))
    }

    @Test fun `someone else's or finished tasks are not completed by me`() {
        assertEquals(ParseResult.NeedsModel, parse("bathroom done"))       // assigned to Ravi
        assertEquals(ParseResult.NeedsModel, parse("plants done"))         // already completed
        assertEquals(ParseResult.NeedsModel, parse("did I finish the dishes"))
    }

    @Test fun `adding a task`() {
        assertEquals(AgentStep.CreateTask("Clean fridge", "weekly"), step("add a task clean fridge"))
        assertEquals(AgentStep.CreateTask("Mop the floor", "daily"), step("create task mop the floor daily"))
        assertEquals(AgentStep.CreateTask("Pay maid", "monthly"), step("new chore pay maid every month"))
        val notAdmin = plan("add a task clean fridge", withTasks.copy(isAdmin = false))
        assertEquals(AgentPlan.Unsupported("Only the flat admin can add tasks. Ask your admin to add it."), notAdmin)
    }

    @Test fun `going away and coming back`() {
        assertEquals(AgentStep.SetAway(true), step("I'm going home tomorrow"))
        assertEquals(AgentStep.SetAway(true), step("out of station till sunday"))
        assertEquals(AgentStep.SetAway(false), step("I'm back"))
    }

    @Test fun `navigation and joining`() {
        assertEquals(AgentLink.JOIN_FLAT, (plan("I want to join a flat") as AgentPlan.Navigate).link)
        assertEquals(AgentLink.JOIN_FLAT, (plan("join flat") as AgentPlan.Navigate).link)
        assertEquals(AgentLink.CREATE_FLAT, (plan("create a new flat") as AgentPlan.Navigate).link)
        assertEquals(AgentLink.EXPENSES, (plan("open expenses") as AgentPlan.Navigate).link)
        assertEquals(AgentLink.DISCOVER, (plan("take me to discover") as AgentPlan.Navigate).link)
        assertEquals(AgentLink.DISCOVER, (plan("find a flatmate") as AgentPlan.Navigate).link)
        assertEquals(AgentLink.MEMBERS, (plan("show members") as AgentPlan.Navigate).link)
        assertEquals(AgentLink.PROFILE, (plan("go to my profile") as AgentPlan.Navigate).link)
    }

    @Test fun `joining works without a flat`() {
        val none = HouseholdState.Empty.copy(myUid = "u1")
        assertEquals(AgentLink.JOIN_FLAT, (plan("I want to join a flat", none) as AgentPlan.Navigate).link)
    }

    @Test fun `help and half-finished expense`() {
        assertTrue(plan("what can you do") is AgentPlan.Reply)
        assertTrue(plan("help") is AgentPlan.Reply)
        val ask = plan("add an expense") as AgentPlan.Reply
        assertEquals("Tell me the amount and what it was for, like “Groceries 560”.", ask.answer.headline)
    }
}
