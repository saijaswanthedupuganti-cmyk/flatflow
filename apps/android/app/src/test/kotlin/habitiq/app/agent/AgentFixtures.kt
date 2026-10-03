package habitiq.app.agent

import java.time.LocalDate

object Fixtures {
    val today: LocalDate = LocalDate.of(2026, 10, 3) // Saturday
    val sai = AgentMember("u1", "Sai", "Sai Jaswanth")
    val ravi = AgentMember("u2", "Ravi", "Ravi Kumar")
    val priya = AgentMember("u3", "Priya", "Priya")
    val arjun = AgentMember("u4", "Arjun", "Arjun")
    val state = HouseholdState(
        myUid = "u1", inFlat = true, isAdmin = true,
        members = listOf(sai, ravi, priya, arjun),
        tasks = emptyList(), myBalances = emptyMap(), bills = emptyList(), vacancies = emptyList(),
    )
    val allUids = listOf("u1", "u2", "u3", "u4")
}
