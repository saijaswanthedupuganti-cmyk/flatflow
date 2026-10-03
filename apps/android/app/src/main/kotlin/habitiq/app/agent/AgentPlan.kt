package habitiq.app.agent

import java.time.LocalDate

/** What the agent intends to do. Shared by the parser, the resolver, the view model and the sheet. */
sealed interface AgentPlan {
    data class Actions(val steps: List<AgentStep>, val summary: String) : AgentPlan
    data class Answer(val query: AgentQuery) : AgentPlan
    data class Clarify(val question: String, val options: List<ClarifyOption>) : AgentPlan
    data class Unsupported(val reason: String) : AgentPlan
}

/** Choosing an option re-enters the flow with [plan], with no extra parsing. */
data class ClarifyOption(val label: String, val plan: AgentPlan)

sealed interface AgentStep {
    data class AddExpense(
        val title: String,
        val amountPaise: Long,
        val category: String,
        val paidByUid: String,
        val splitAmongUids: List<String>,
    ) : AgentStep

    data class Settle(val fromUid: String, val toUid: String, val amountPaise: Long) : AgentStep
}

sealed interface AgentQuery {
    data object MyBalance : AgentQuery
    data object WhoOwes : AgentQuery
    data object DueToday : AgentQuery
    data class MyDuties(val from: LocalDate, val until: LocalDate) : AgentQuery
    data object Bills : AgentQuery
    data class FindFlats(val area: String?, val city: String?, val maxRent: Long?, val gender: String?) : AgentQuery
}

sealed interface ParseResult {
    data class Confident(val plan: AgentPlan) : ParseResult
    data object NeedsModel : ParseResult
}

/** Where an answer's button takes the person. */
enum class AgentLink(val label: String) {
    BALANCES("Open balances"),
    TASKS("Open tasks"),
    BILLS("Open bills"),
    DISCOVER("Open Discover"),
    EXPENSES("Open Expenses"),
}

data class AnswerLine(val label: String, val value: String)

data class AgentAnswer(val headline: String, val lines: List<AnswerLine>, val link: AgentLink?)

/** What the sheet draws for an action: a receipt-style card the person can take in at a glance. */
sealed interface AgentCard {
    data class Expense(
        val title: String,
        val amountPaise: Long,
        val category: String,
        val paidBy: String,          // "You" or a first name
        val splitNames: List<String>,
        val everyone: Boolean,
    ) : AgentCard

    data class Payment(val from: String, val to: String, val amountPaise: Long) : AgentCard
}
