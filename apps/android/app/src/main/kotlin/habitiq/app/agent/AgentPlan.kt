package habitiq.app.agent

import java.time.LocalDate

/** What the agent intends to do. Shared by the parser, the resolver, the view model and the sheet. */
sealed interface AgentPlan {
    /** [say] is Gemini's own way of putting it ("Got it, 500 on groceries for the four of you. Save it?"). */
    data class Actions(val steps: List<AgentStep>, val summary: String, val say: String? = null) : AgentPlan
    /** [say]: Gemini's friendly line to go with the answer card; the card's numbers always come from the app. */
    data class Answer(val query: AgentQuery, val say: String? = null) : AgentPlan
    data class Clarify(val question: String, val options: List<ClarifyOption>) : AgentPlan
    data class Unsupported(val reason: String) : AgentPlan
    /** Take the person somewhere in the app ("join a flat", "open expenses"); [say] is the spoken reply. */
    data class Navigate(val link: AgentLink, val say: String) : AgentPlan
    /** A spoken/written reply with nothing to save (help, a prompt for missing details, a model answer). */
    /** [byAi]: Gemini wrote the words (so the person can report them), not one of the app's own lines. */
    data class Reply(val answer: AgentAnswer, val byAi: Boolean = false) : AgentPlan
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
    /**
     * [area] is where the person asked about; [nearby] are other areas close to it, closest first (from
     * Gemini's sense of the city), used when [area] itself has nothing. [wants] are wishes like "veg" or
     * "wifi" that rank listings; [picks] are listing flat ids Gemini judged the best fit, in order.
     */
    data class FindFlats(
        val area: String?, val city: String?, val maxRent: Long?, val gender: String?,
        val nearby: List<String> = emptyList(),
        val wants: List<String> = emptyList(),
        val picks: List<String> = emptyList(),
    ) : AgentQuery
    /** "What's the budget in Punjagutta?": typical rent per head from live listings. */
    data class AreaRent(val area: String, val nearby: List<String> = emptyList()) : AgentQuery
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
