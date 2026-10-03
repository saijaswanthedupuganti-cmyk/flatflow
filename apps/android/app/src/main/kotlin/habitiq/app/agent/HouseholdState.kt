package habitiq.app.agent

import habitiq.app.data.BillInstance
import habitiq.app.data.FlatTask
import habitiq.app.data.VacancyListing
import habitiq.app.flats.FlatInfo
import habitiq.app.flats.Member
import habitiq.app.lib.parseTaskLocalDate
import java.time.LocalDate
import kotlin.math.roundToLong

/** [name] is the full nickname. It's used only for on-screen clarify labels and never leaves the device. */
data class AgentMember(val uid: String, val firstName: String, val name: String)

data class AgentTask(
    val id: String,
    val name: String,
    val assigneeUid: String,
    val due: LocalDate?,
    val frequency: String,
    val status: String,
)

data class AgentBill(val name: String, val amountPaise: Long?, val due: LocalDate?, val paidByUid: String, val status: String)

data class AgentVacancy(
    val flatId: String,
    val flatName: String,
    val area: String,
    val city: String,
    val rentPaise: Long?,
    val preferredGender: String,
)

/** The agent's read-only view of the person's flat, rebuilt from live state for every request. */
data class HouseholdState(
    val myUid: String,
    val inFlat: Boolean,
    val isAdmin: Boolean,
    val members: List<AgentMember>,
    val tasks: List<AgentTask>,
    /**
     * The person's own balances with each flatmate, all-time, from the same calculation the Expenses
     * and Home screens use (`pairwisePersonalBalances`): other uid -> rupees, + they owe me, - I owe them.
     */
    val myBalances: Map<String, Double>,
    val bills: List<AgentBill>,
    val vacancies: List<AgentVacancy>,
) {
    companion object {
        val Empty = HouseholdState("", false, false, emptyList(), emptyList(), emptyMap(), emptyList(), emptyList())
    }
}

fun firstNameOf(nickname: String): String =
    nickname.trim().substringBefore(' ').ifBlank { "Member" }.replaceFirstChar { it.uppercase() }

private fun Double.toPaise(): Long = (this * 100).roundToLong()

fun householdStateOf(
    myUid: String,
    flat: FlatInfo?,
    members: List<Member>,
    tasks: List<FlatTask>,
    myBalances: Map<String, Double>,
    billInstances: List<BillInstance>,
    vacancies: List<VacancyListing>,
    month: String,
): HouseholdState = HouseholdState(
    myUid = myUid,
    inFlat = flat != null,
    isAdmin = flat != null && flat.adminUid == myUid,
    members = members.map { AgentMember(it.uid, firstNameOf(it.nickname), it.nickname.trim()) },
    tasks = tasks.map { AgentTask(it.taskId, it.name, it.currentAssignedUserId, parseTaskLocalDate(it.dueDate), it.frequency, it.status) },
    myBalances = myBalances,
    bills = billInstances.filter { it.month == month }
        .map { AgentBill(it.name, it.amount?.toPaise(), parseTaskLocalDate(it.dueDate), it.paidBy, it.status) },
    vacancies = vacancies.filter { it.active }
        .map { AgentVacancy(it.flatId, it.flatName, it.area, it.city, it.rentPerHead?.toPaise(), it.preferredGender) },
)
