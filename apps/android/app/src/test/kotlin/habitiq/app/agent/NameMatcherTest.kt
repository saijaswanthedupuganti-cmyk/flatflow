package habitiq.app.agent

import org.junit.Assert.assertEquals
import org.junit.Test

class NameMatcherTest {
    private val ravi = AgentMember("u2", "Ravi", "Ravi Kumar")
    private val priya = AgentMember("u3", "Priya", "Priya")
    private val dave = AgentMember("u5", "Dave", "Dave")
    private val sai = AgentMember("u1", "Sai", "Sai")
    private val all = listOf(sai, ravi, priya, dave)

    @Test fun `edit distance`() {
        assertEquals(0, editDistance("ravi", "ravi"))
        assertEquals(1, editDistance("priya", "priyaa"))
        assertEquals(1, editDistance("dave", "gave"))
        assertEquals(3, editDistance("abc", ""))
    }

    @Test fun `exact match ignores case`() {
        assertEquals(listOf(ravi), matchMembers("RAVI", all, fuzzy = true))
    }

    @Test fun `fuzzy match only for names of four or more letters`() {
        assertEquals(listOf(priya), matchMembers("priyaa", all, fuzzy = true))
        assertEquals(emptyList<AgentMember>(), matchMembers("sia", all, fuzzy = true))
    }

    @Test fun `vocabulary words only match exactly`() {
        assertEquals(emptyList<AgentMember>(), matchMembers("gave", all, fuzzy = false))
        assertEquals(listOf(dave), matchMembers("gave", all, fuzzy = true))
    }

    @Test fun `two members with the same first name both come back`() {
        val raviT = AgentMember("u6", "Ravi", "Ravi Teja")
        assertEquals(listOf(ravi, raviT), matchMembers("ravi", all + raviT, fuzzy = true))
    }
}
