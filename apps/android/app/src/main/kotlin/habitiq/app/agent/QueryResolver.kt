package habitiq.app.agent

import habitiq.app.lib.formatInr
import habitiq.app.lib.suggestSettlements
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val DAY = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH)
private const val SETTLED_RUPEES = 1.0
private val CLOSED_TASK = setOf("completed", "paused")
private val CLOSED_BILL = setOf("paid", "skipped")

private fun rupees(paise: Long) = formatInr(paise / 100.0)
private fun HouseholdState.nameOf(uid: String) =
    if (uid == myUid) "You" else members.firstOrNull { it.uid == uid }?.firstName ?: "Someone"

/** Turns a question into an answer from live data. Numbers always come from app state, never from text. */
object QueryResolver {
    fun resolve(query: AgentQuery, state: HouseholdState, today: LocalDate): AgentAnswer {
        if (query !is AgentQuery.FindFlats && !state.inFlat) return AgentAnswer("Join a flat to see this", emptyList(), null)
        return when (query) {
            AgentQuery.MyBalance -> myBalance(state)
            AgentQuery.WhoOwes -> whoOwes(state)
            AgentQuery.DueToday -> dueToday(state, today)
            is AgentQuery.MyDuties -> myDuties(state, query.from, query.until)
            AgentQuery.Bills -> bills(state)
            is AgentQuery.FindFlats -> findFlats(state, query)
        }
    }

    private fun myBalance(s: HouseholdState): AgentAnswer {
        val net = s.netBalances[s.myUid] ?: 0.0
        val pays = suggestSettlements(s.netBalances).filter { it.fromUserId == s.myUid }
            .map { AnswerLine("Pay ${s.nameOf(it.toUserId)}", formatInr(it.amount)) }
        val gets = suggestSettlements(s.netBalances).filter { it.toUserId == s.myUid }
            .map { AnswerLine("${s.nameOf(it.fromUserId)} pays you", formatInr(it.amount)) }
        return when {
            net <= -SETTLED_RUPEES -> AgentAnswer("You owe ${formatInr(abs(net))}", pays, AgentLink.BALANCES)
            net >= SETTLED_RUPEES -> AgentAnswer("You're owed ${formatInr(net)}", gets, AgentLink.BALANCES)
            else -> AgentAnswer("You're all settled", emptyList(), AgentLink.BALANCES)
        }
    }

    private fun whoOwes(s: HouseholdState): AgentAnswer {
        val debtors = s.netBalances.filterValues { it <= -SETTLED_RUPEES }.entries.sortedBy { it.value }
        if (debtors.isEmpty()) return AgentAnswer("Nobody owes anything right now", emptyList(), AgentLink.BALANCES)
        val head = if (debtors.size == 1) "1 person owes money" else "${debtors.size} people owe money"
        return AgentAnswer(head, debtors.map { AnswerLine(s.nameOf(it.key), formatInr(abs(it.value))) }, AgentLink.BALANCES)
    }

    private fun mine(s: HouseholdState) = s.tasks.filter { it.assigneeUid == s.myUid && it.status !in CLOSED_TASK }

    private fun dueToday(s: HouseholdState, today: LocalDate): AgentAnswer {
        // Today's tasks first, then overdue ones oldest first.
        val due = mine(s).filter { it.due != null && !it.due.isAfter(today) }
            .sortedWith(compareBy({ if (it.due == today) 0 else 1 }, { it.due }))
        if (due.isEmpty()) return AgentAnswer("Nothing due today", emptyList(), AgentLink.TASKS)
        val head = if (due.size == 1) "1 thing for you today" else "${due.size} things for you today"
        return AgentAnswer(head, due.map { AnswerLine(it.name, if (it.due == today) "Due today" else "Overdue") }, AgentLink.TASKS)
    }

    private fun myDuties(s: HouseholdState, from: LocalDate, until: LocalDate): AgentAnswer {
        val list = mine(s).filter { it.due != null && !it.due.isBefore(from) && !it.due.isAfter(until) }.sortedBy { it.due }
        if (list.isEmpty()) return AgentAnswer("Nothing on your list for those days", emptyList(), AgentLink.TASKS)
        val head = if (list.size == 1) "1 task coming up" else "${list.size} tasks coming up"
        return AgentAnswer(head, list.map { AnswerLine(it.name, DAY.format(it.due)) }, AgentLink.TASKS)
    }

    private fun bills(s: HouseholdState): AgentAnswer {
        val open = s.bills.filter { it.status !in CLOSED_BILL }.sortedBy { it.due }
        if (open.isEmpty()) return AgentAnswer("All bills are sorted this month", emptyList(), AgentLink.BILLS)
        val head = if (open.size == 1) "1 bill still open" else "${open.size} bills still open"
        return AgentAnswer(head, open.map { b ->
            val amount = b.amountPaise?.let(::rupees) ?: "Amount not set"
            AnswerLine(b.name, if (b.due != null) "$amount · ${DAY.format(b.due)}" else amount)
        }, AgentLink.BILLS)
    }

    private fun findFlats(s: HouseholdState, q: AgentQuery.FindFlats): AgentAnswer {
        val place = (q.area ?: q.city)?.lowercase()
        val matches = s.vacancies.asSequence()
            .filter { v -> place == null || v.area.lowercase().contains(place) || v.city.lowercase().contains(place) }
            .filter { v -> q.maxRent == null || (v.rentPaise != null && v.rentPaise <= q.maxRent * 100) }
            .filter { v -> q.gender == null || v.preferredGender == "any" || v.preferredGender == q.gender }
            .sortedBy { it.rentPaise ?: Long.MAX_VALUE }
            .take(5).toList()
        if (matches.isEmpty()) return AgentAnswer("No places match yet", emptyList(), AgentLink.DISCOVER)
        val head = if (matches.size == 1) "1 place matches" else "${matches.size} places match"
        return AgentAnswer(head, matches.map { v ->
            AnswerLine(v.flatName, v.area + (v.rentPaise?.let { " · ${rupees(it)}/head" } ?: ""))
        }, AgentLink.DISCOVER)
    }
}

/** The card for an action plan. The M1 parser always produces exactly one step. */
fun cardFor(plan: AgentPlan.Actions, state: HouseholdState): AgentCard = when (val step = plan.steps.first()) {
    is AgentStep.AddExpense -> AgentCard.Expense(
        title = step.title,
        amountPaise = step.amountPaise,
        category = step.category,
        paidBy = state.nameOf(step.paidByUid),
        splitNames = step.splitAmongUids.map { state.nameOf(it) },
        everyone = step.splitAmongUids.size == state.members.size,
    )
    is AgentStep.Settle -> AgentCard.Payment(state.nameOf(step.fromUid), state.nameOf(step.toUid), step.amountPaise)
}
