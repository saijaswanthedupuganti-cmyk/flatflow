package habitiq.app.agent

import habitiq.app.agent.Fixtures.state
import habitiq.app.agent.Fixtures.today
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalIntentParserQuestionsTest {
    private fun answer(q: AgentQuery) = ParseResult.Confident(AgentPlan.Answer(q))
    private fun parse(text: String) = LocalIntentParser.parse(text, state, today)

    @Test fun `balance questions`() {
        listOf("What do I owe?", "how much do i owe", "my balance", "Do I owe anyone", "am I owed anything", "kitna dena hai")
            .forEach { assertEquals(it, answer(AgentQuery.MyBalance), parse(it)) }
    }

    @Test fun `who owes questions`() {
        listOf("who owes money", "Who hasn't paid?", "who has not paid yet", "who all owe", "who owes me")
            .forEach { assertEquals(it, answer(AgentQuery.WhoOwes), parse(it)) }
    }

    @Test fun `due today questions`() {
        listOf("What's due today?", "what is due", "today's tasks", "what do i have today", "anything due today")
            .forEach { assertEquals(it, answer(AgentQuery.DueToday), parse(it)) }
    }

    @Test fun `my duties with a range`() {
        assertEquals(answer(AgentQuery.MyDuties(today, today.plusDays(1))), parse("my tasks this week"))
        assertEquals(answer(AgentQuery.MyDuties(today.plusDays(1), today.plusDays(1))), parse("My chores tomorrow"))
        assertEquals(answer(AgentQuery.MyDuties(today, today.plusDays(6))), parse("what are my duties"))
        // "whose turn" is not "my": it must not be read as my duties.
        assertEquals(ParseResult.NeedsModel, parse("whose turn is it next week"))
    }

    @Test fun `bills questions`() {
        listOf("any bills pending?", "show bills", "which bills are due", "unpaid bills")
            .forEach { assertEquals(it, answer(AgentQuery.Bills), parse(it)) }
    }

    @Test fun `flat search`() {
        assertEquals(answer(AgentQuery.FindFlats("gachibowli", null, 10_000, null)), parse("Flats in Gachibowli under 10k"))
        assertEquals(answer(AgentQuery.FindFlats("madhapur", null, null, "female")), parse("rooms near Madhapur for girls"))
        assertEquals(answer(AgentQuery.FindFlats("kondapur", null, 8_000, "male")), parse("any 2 bhk flat in kondapur below 8000 for boys"))
        assertEquals(answer(AgentQuery.FindFlats(null, null, 12_000, null)), parse("find rooms under 12k"))
    }

    @Test fun `not understood`() {
        listOf("hello", "remind me to call mom", "", "   ", "flats")
            .forEach { assertEquals(it, ParseResult.NeedsModel, parse(it)) }
    }
}
