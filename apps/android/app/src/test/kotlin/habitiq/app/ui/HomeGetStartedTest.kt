package habitiq.app.ui

import habitiq.app.data.FlatTask
import habitiq.app.flats.FlatInfo
import habitiq.app.flats.Member
import habitiq.app.home.HomeDashboardData
import habitiq.app.home.HomeViewMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The Home "Get started" checklist is derived from real flat data and leaves once every step is done. */
class HomeGetStartedTest {

    private val me = Member("me", "Sai", "admin")
    private val task = FlatTask(taskId = "t1", name = "Trash", currentAssignedUserId = "me", status = "completed", dueDate = "2026-10-01")

    private fun data(members: List<Member> = listOf(me), tasks: List<FlatTask> = emptyList(), isAdmin: Boolean = true) =
        HomeDashboardData(
            flat = FlatInfo(id = "F1", name = "Flat", adminUid = "me", memberCount = members.size),
            members = members, tasks = tasks, activity = emptyList(), monthlySpent = 0.0,
            isAdmin = isAdmin, currentUid = "me", displayName = "Sai", viewMode = HomeViewMode.FLAT,
        )

    private fun model(d: HomeDashboardData, hasExpense: Boolean = false, hidden: Boolean = false) =
        buildHomeModel(d, pendingJoins = 0, pendingSwapsForMe = 0, balances = emptyMap(), hasExpense = hasExpense, getStartedHidden = hidden)

    @Test fun newFlatShowsThreeOpenSteps() {
        val steps = model(data()).getStarted
        assertEquals(listOf(HomeGetStartedStep.Kind.Invite, HomeGetStartedStep.Kind.Expense, HomeGetStartedStep.Kind.Task), steps.map { it.kind })
        assertTrue(steps.none { it.done })
    }

    @Test fun stepsTickFromRealData() {
        val steps = model(data(members = listOf(me, Member("b", "Ravi", "member"))), hasExpense = true).getStarted
        assertEquals(listOf(true, true, false), steps.map { it.done })
    }

    @Test fun membersDoNotGetTheTaskStep() {
        assertEquals(2, model(data(isAdmin = false)).getStarted.size)
    }

    @Test fun leavesWhenAllDoneOrHidden() {
        val full = data(members = listOf(me, Member("b", "Ravi", "member")), tasks = listOf(task))
        assertTrue(model(full, hasExpense = true).getStarted.isEmpty())
        assertTrue(model(data(), hidden = true).getStarted.isEmpty())
    }
}
