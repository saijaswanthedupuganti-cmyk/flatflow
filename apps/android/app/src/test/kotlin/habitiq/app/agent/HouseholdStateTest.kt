package habitiq.app.agent

import habitiq.app.data.BillInstance
import habitiq.app.data.FlatTask
import habitiq.app.data.VacancyListing
import habitiq.app.flats.FlatInfo
import habitiq.app.flats.Member
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HouseholdStateTest {
    private val flat = FlatInfo(id = "f1", name = "Lake View", adminUid = "u1", memberCount = 2)
    private val members = listOf(Member("u1", "Sai Jaswanth", "admin"), Member("u2", "  ravi kumar ", "member"))

    @Test fun `first names come from nicknames`() {
        assertEquals("Sai", firstNameOf("Sai Jaswanth"))
        assertEquals("Ravi", firstNameOf("  ravi kumar "))
        assertEquals("Member", firstNameOf("   "))
    }

    @Test fun `builds members tasks bills and vacancies`() {
        val tasks = listOf(FlatTask("t1", "Dishes", currentAssignedUserId = "u1", status = "pending", dueDate = "2026-10-03"))
        val bills = listOf(
            BillInstance(id = "b1", templateId = "x", month = "2026-10", name = "Wifi", amount = 999.0, paidBy = "u1", dueDate = "2026-10-05"),
            BillInstance(id = "b2", templateId = "y", month = "2026-09", name = "Old", paidBy = "u1", dueDate = "2026-09-05"),
        )
        val vacancies = listOf(
            VacancyListing("f9", "Green Nest", true, "Hyderabad", "Gachibowli", 9500.0, "INR", 1, "female", ""),
            VacancyListing("f8", "Closed", false, "Hyderabad", "Kondapur", 7000.0, "INR", 1, "any", ""),
        )
        val s = householdStateOf("u1", flat, members, tasks, mapOf("u1" to -120.5), bills, vacancies, "2026-10")

        assertTrue(s.inFlat)
        assertTrue(s.isAdmin)
        assertEquals(listOf(AgentMember("u1", "Sai", "Sai Jaswanth"), AgentMember("u2", "Ravi", "ravi kumar")), s.members)
        assertEquals(AgentTask("t1", "Dishes", "u1", LocalDate.of(2026, 10, 3), "weekly", "pending"), s.tasks.single())
        assertEquals(listOf("Wifi"), s.bills.map { it.name })
        assertEquals(99_900L, s.bills.single().amountPaise)
        assertEquals(listOf("f9"), s.vacancies.map { it.flatId })
        assertEquals(950_000L, s.vacancies.single().rentPaise)
        assertEquals(-120.5, s.netBalances.getValue("u1"), 0.0)
    }

    @Test fun `no flat means an empty household`() {
        val s = householdStateOf("u1", null, emptyList(), emptyList(), emptyMap(), emptyList(), emptyList(), "2026-10")
        assertFalse(s.inFlat)
        assertFalse(s.isAdmin)
        assertFalse(HouseholdState.Empty.inFlat)
    }
}
