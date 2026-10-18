package habitiq.app.agent

import habitiq.app.lib.formatInr
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
        val aboutListings = query is AgentQuery.FindFlats || query is AgentQuery.AreaRent
        if (!aboutListings && !state.inFlat) return AgentAnswer("Join a flat to see this", emptyList(), null)
        return when (query) {
            AgentQuery.MyBalance -> myBalance(state)
            AgentQuery.WhoOwes -> whoOwes(state)
            AgentQuery.DueToday -> dueToday(state, today)
            is AgentQuery.MyDuties -> myDuties(state, query.from, query.until)
            AgentQuery.Bills -> bills(state)
            is AgentQuery.FindFlats -> findFlats(state, query)
            is AgentQuery.AreaRent -> areaRent(state, query)
        }
    }

    private fun myBalance(s: HouseholdState): AgentAnswer {
        val owe = s.myBalances.filterValues { it <= -SETTLED_RUPEES }.entries.sortedBy { it.value }
        val owed = s.myBalances.filterValues { it >= SETTLED_RUPEES }.entries.sortedByDescending { it.value }
        val lines = owe.map { AnswerLine("Pay ${s.nameOf(it.key)}", formatInr(abs(it.value))) } +
            owed.map { AnswerLine("${s.nameOf(it.key)} owes you", formatInr(it.value)) }
        return when {
            owe.isNotEmpty() -> AgentAnswer("You owe ${formatInr(owe.sumOf { abs(it.value) })}", lines, AgentLink.BALANCES)
            owed.isNotEmpty() -> AgentAnswer("You're owed ${formatInr(owed.sumOf { it.value })}", lines, AgentLink.BALANCES)
            else -> AgentAnswer("You're all settled", emptyList(), AgentLink.BALANCES)
        }
    }

    private fun whoOwes(s: HouseholdState): AgentAnswer {
        val owed = s.myBalances.filterValues { it >= SETTLED_RUPEES }.entries.sortedByDescending { it.value }
        if (owed.isEmpty()) return AgentAnswer("Nobody owes you anything right now", emptyList(), AgentLink.BALANCES)
        val head = if (owed.size == 1) "1 person owes you" else "${owed.size} people owe you"
        return AgentAnswer(head, owed.map { AnswerLine(s.nameOf(it.key), formatInr(it.value)) }, AgentLink.BALANCES)
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
        val primary = q.area ?: q.city
        val places = listOfNotNull(primary) + q.nearby.filter { it.isNotBlank() }
        // 0 = in the place asked about, 1.. = the nearby areas in order, -1 = elsewhere.
        fun rank(v: AgentVacancy) = if (places.isEmpty()) 0 else places.indexOfFirst { FlatSearch.inPlace(v, it) }
        val fits = s.vacancies
            .filter { v -> q.maxRent == null || (v.rentPaise != null && v.rentPaise <= q.maxRent * 100) }
            .filter { v -> q.gender == null || v.preferredGender == "any" || v.preferredGender.isBlank() || v.preferredGender == q.gender }
            .map { it to rank(it) }
            .filter { it.second >= 0 }
        val matches = fits.sortedWith(
            compareBy<Pair<AgentVacancy, Int>>({ it.second })
                .thenByDescending { FlatSearch.wantScore(it.first, q.wants) }
                .thenBy { q.picks.indexOf(it.first.flatId).let { i -> if (i < 0) Int.MAX_VALUE else i } }
                .thenBy { it.first.rentPaise ?: Long.MAX_VALUE },
        ).take(5)

        val where = primary?.let { FlatSearch.displayName(s, it) }
        val budget = q.maxRent?.let { " under ${formatInr(it.toDouble())}" }.orEmpty()
        if (matches.isEmpty()) {
            val head = if (where != null) "Nothing in $where$budget right now" else "Nothing$budget right now"
            return AgentAnswer(head, listOf(AnswerLine("New places get posted often", "Check Discover")), AgentLink.DISCOVER)
        }
        val inPrimary = matches.count { it.second == 0 }
        val n = matches.size
        val places1 = if (n == 1) "1 place" else "$n places"
        val head = when {
            where == null -> "Found $places1$budget"
            inPrimary == n -> "Found $places1 in $where$budget"
            inPrimary == 0 -> "Nothing in $where yet, but $places1 close by"
            else -> "Found $places1 in and around $where"
        }
        return AgentAnswer(head, matches.map { (v, _) ->
            val details = listOfNotNull(
                v.area.takeIf { it.isNotBlank() },
                v.rentPaise?.let { "${rupees(it)}/head" },
                v.roomType.takeIf { it.isNotBlank() }?.let(::prettyRoom),
            )
            AnswerLine(v.flatName, details.joinToString(" · "))
        }, AgentLink.DISCOVER)
    }

    private fun areaRent(s: HouseholdState, q: AgentQuery.AreaRent): AgentAnswer {
        val where = FlatSearch.displayName(s, q.area)
        val priced = s.vacancies.filter { it.rentPaise != null }
        var pool = priced.filter { FlatSearch.inPlace(it, q.area) }
        var around = false
        if (pool.isEmpty()) {
            pool = priced.filter { v -> q.nearby.any { FlatSearch.inPlace(v, it) } }
            around = pool.isNotEmpty()
        }
        if (pool.isEmpty()) {
            return AgentAnswer("No listings in $where yet, so I can't say", listOf(AnswerLine("New places get posted often", "Check Discover")), AgentLink.DISCOVER)
        }
        val rents = pool.mapNotNull { it.rentPaise }.sorted()
        val typical = rents[rents.size / 2]
        val head = if (around) "Around $where, rooms go for about ${rupees(typical)} a head"
            else "In $where, rooms go for about ${rupees(typical)} a head"
        val lines = buildList {
            if (rents.size > 1) {
                add(AnswerLine("Cheapest", rupees(rents.first())))
                add(AnswerLine("Top end", rupees(rents.last())))
            }
            add(AnswerLine("Based on", if (rents.size == 1) "1 live listing" else "${rents.size} live listings"))
        }
        return AgentAnswer(head, lines, AgentLink.DISCOVER)
    }

    private fun prettyRoom(raw: String): String = when (raw.lowercase()) {
        "private", "single", "private_room" -> "Private room"
        else -> "Shared room" // the post form only offers private or shared
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
    is AgentStep.CompleteTask -> AgentCard.Simple(AgentCard.Simple.Kind.TaskDone, step.taskName, "Mark as done")
    is AgentStep.CreateTask -> AgentCard.Simple(
        AgentCard.Simple.Kind.NewTask, step.name,
        "New ${step.frequency} task, rotates between everyone",
    )
    is AgentStep.SetAway -> if (step.away) AgentCard.Simple(AgentCard.Simple.Kind.Away, "Going away", "Flatmates will see you're away")
        else AgentCard.Simple(AgentCard.Simple.Kind.Back, "Back in the flat", "You'll be marked as available")
}
