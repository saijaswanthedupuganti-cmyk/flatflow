package habitiq.app.agent

import java.time.LocalDate

/** What the agent intends to do. Shared by the parser, the resolver, the view model and the sheet. */
sealed interface AgentPlan {
    data class Actions(val steps: List<AgentStep>, val summary: String) : AgentPlan
    data class Answer(val query: AgentQuery) : AgentPlan
    data class Clarify(val question: String, val options: List<ClarifyOption>) : AgentPlan
    data class Unsupported(val reason: String) : AgentPlan
    /** Take the person somewhere in the app ("join a flat", "open expenses"); [say] is the spoken reply. */
    data class Navigate(val link: AgentLink, val say: String) : AgentPlan
    /** A spoken/written reply with nothing to save (help, a prompt for missing details, a model answer). */
    data class Reply(val answer: AgentAnswer) : AgentPlan
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
    data class CompleteTask(val taskId: String, val taskName: String) : AgentStep
    data class CreateTask(val name: String, val frequency: String) : AgentStep        // admin only
    /** Out of station (true) or back (false); flatmates see the status. */
    data class SetAway(val away: Boolean) : AgentStep
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
    JOIN_FLAT("Join a flat"),
    CREATE_FLAT("Create a flat"),
    MEMBERS("Open members"),
    PROFILE("Open profile"),
    HOME("Go home"),
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
    /** Task done / new task / away status. [kind] picks the icon. */
    data class Simple(val kind: Kind, val title: String, val subtitle: String) : AgentCard {
        enum class Kind { TaskDone, NewTask, Away, Back }
    }
}
