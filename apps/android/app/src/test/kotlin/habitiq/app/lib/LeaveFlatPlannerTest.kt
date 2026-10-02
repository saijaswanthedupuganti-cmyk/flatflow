package habitiq.app.lib

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LeaveFlatPlannerTest {
    private fun m(uid: String, role: String = "member", joined: String = "2026-01-01T00:00:00Z") = LeaveMember(uid, role, joined)

    @Test fun `last member closes the flat`() {
        val plan = planLeave("me", listOf(m("me", "admin")), emptyList())
        assertTrue(plan.closeFlat)
        assertNull(plan.promoteUid)
    }

    @Test fun `sole admin hands over to the longest standing member`() {
        val members = listOf(m("me", "admin", "2026-01-01T00:00:00Z"), m("late", joined = "2026-06-01T00:00:00Z"), m("early", joined = "2026-02-01T00:00:00Z"))
        val plan = planLeave("me", members, emptyList())
        assertFalse(plan.closeFlat)
        assertEquals("early", plan.promoteUid)
    }

    @Test fun `no handover when another admin remains or a plain member leaves`() {
        assertNull(planLeave("me", listOf(m("me", "admin"), m("b", "admin")), emptyList()).promoteUid)
        assertNull(planLeave("me", listOf(m("me"), m("b", "admin")), emptyList()).promoteUid)
    }

    @Test fun `members with no join date still get a deterministic successor`() {
        val plan = planLeave("me", listOf(m("me", "admin"), m("zed", joined = ""), m("amy", joined = "")), emptyList())
        assertEquals("amy", plan.promoteUid)
    }

    @Test fun `my task passes to the next person in that rotation and I leave the queue`() {
        val members = listOf(m("me"), m("ana"), m("raj"))
        val tasks = listOf(LeaveTask("t1", assignedUid = "me", queueOrder = listOf("ana", "me", "raj")))
        val change = planLeave("me", members, tasks).taskChanges.single()
        assertEquals("raj", change.newAssignee)
        assertEquals(listOf("ana", "raj"), change.newQueue)
    }

    @Test fun `rotation wraps round when I am last in the queue`() {
        val members = listOf(m("me"), m("ana"), m("raj"))
        val tasks = listOf(LeaveTask("t1", assignedUid = "me", queueOrder = listOf("ana", "raj", "me")))
        assertEquals("ana", planLeave("me", members, tasks).taskChanges.single().newAssignee)
    }

    @Test fun `someone else's task keeps its assignee but I am still removed from its queue`() {
        val members = listOf(m("me"), m("ana"))
        val tasks = listOf(LeaveTask("t1", assignedUid = "ana", queueOrder = listOf("ana", "me")))
        val change = planLeave("me", members, tasks).taskChanges.single()
        assertEquals("ana", change.newAssignee)
        assertEquals(listOf("ana"), change.newQueue)
    }

    @Test fun `tasks that never involved me are untouched`() {
        val tasks = listOf(LeaveTask("t1", assignedUid = "ana", queueOrder = listOf("ana", "raj")))
        assertTrue(planLeave("me", listOf(m("me"), m("ana"), m("raj")), tasks).taskChanges.isEmpty())
    }

    @Test fun `an unqueued task I hold falls back to a remaining member`() {
        val tasks = listOf(LeaveTask("t1", assignedUid = "me", queueOrder = emptyList()))
        assertEquals("ana", planLeave("me", listOf(m("me"), m("ana")), tasks).taskChanges.single().newAssignee)
    }
}

class LeaveFlatConsequencesTest {
    private fun m(uid: String, role: String = "member") = LeaveMember(uid, role, "2026-01-01T00:00:00Z")

    @Test fun `last member is told the flat will close`() {
        val text = leaveFlatConsequences(listOf(m("me", "admin")), "me", hasUnsettledBalances = false)
        assertTrue(text.contains("will be closed"))
        assertFalse(text.contains("invite code"))
    }

    @Test fun `sole admin is told about the handover`() {
        val text = leaveFlatConsequences(listOf(m("me", "admin"), m("b")), "me", hasUnsettledBalances = false)
        assertTrue(text.contains("only admin"))
    }

    @Test fun `plain member is not told about a handover`() {
        val text = leaveFlatConsequences(listOf(m("me"), m("b", "admin")), "me", hasUnsettledBalances = false)
        assertFalse(text.contains("only admin"))
        assertTrue(text.contains("invite code"))
    }

    @Test fun `unsettled balances are mentioned only when they exist`() {
        val members = listOf(m("me"), m("b", "admin"))
        assertTrue(leaveFlatConsequences(members, "me", true).contains("unsettled"))
        assertFalse(leaveFlatConsequences(members, "me", false).contains("unsettled"))
    }
}
