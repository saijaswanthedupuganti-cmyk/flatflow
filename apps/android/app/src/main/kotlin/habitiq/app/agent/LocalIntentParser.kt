package habitiq.app.agent

import java.time.LocalDate

/**
 * Deterministic, rule-based understanding of short requests. Returns [ParseResult.Confident] only
 * when every slot is filled without guessing; anything else is [ParseResult.NeedsModel].
 */
object LocalIntentParser {

    private val QUESTION_START = setOf(
        "what", "whats", "how", "who", "whose", "when", "where", "which", "did", "do", "does", "is", "are", "can",
        "show", "list", "any", "find", "am",
    )

    fun parse(raw: String, state: HouseholdState, today: LocalDate): ParseResult {
        val text = normalizeUtterance(raw)
        if (text.isBlank()) return ParseResult.NeedsModel
        val amounts = findAmounts(text)

        if (amounts.isEmpty()) question(text, today)?.let { return answer(it) }
        findFlats(text, amounts)?.let { return answer(it) }
        return ParseResult.NeedsModel
    }

    private fun answer(q: AgentQuery) = ParseResult.Confident(AgentPlan.Answer(q))

    private fun Regex.inText(text: String) = containsMatchIn(text)
    private fun rx(p: String) = Regex("""\b(?:$p)\b""")

    private val WHO_OWES = rx("who (?:owes|all owe|still owes|hasnt paid|has not paid|havent paid|have not paid)")
    private val MY_BALANCE = rx("(?:what|how much) do i owe|my balance|do i owe|am i owed|kitna dena|how much (?:should|do) i pay|my dues")
    private val DUE_TODAY = rx("due today|whats due|what is due|todays tasks|what do i have today|anything due")
    private val MY_DUTIES = rx("my (?:tasks|task|duties|duty|chores|chore|turn|work)")
    private val BILLS = rx("bills?")
    private val BILLS_ASK = rx("pending|due|unpaid|show|list|any|which|what")

    private fun question(text: String, today: LocalDate): AgentQuery? {
        val range = parseDateRange(text, today)
        return when {
            WHO_OWES.inText(text) -> AgentQuery.WhoOwes
            MY_BALANCE.inText(text) -> AgentQuery.MyBalance
            DUE_TODAY.inText(text) -> AgentQuery.DueToday
            MY_DUTIES.inText(text) -> when {
                range == null -> AgentQuery.MyDuties(today, today.plusDays(6))
                range.start == today && range.endInclusive == today -> AgentQuery.DueToday
                else -> AgentQuery.MyDuties(range.start, range.endInclusive)
            }
            BILLS.inText(text) && BILLS_ASK.inText(text) -> AgentQuery.Bills
            else -> null
        }
    }

    private val FLAT_NOUN = rx("flats?|rooms?|pgs?|vacanc(?:y|ies)|flatmates?|accommodation")
    private val PLACE = Regex("""\b(?:in|near|around|at)\s+([a-z][a-z ]*?)(?=\s+(?:under|below|within|less|upto|up|for|with|max)\b|$)""")
    private val FEMALE = rx("girls?|women|woman|female|ladies")
    private val MALE = rx("boys?|men|man|male|gents")

    private fun findFlats(text: String, amounts: List<AmountMatch>): AgentQuery? {
        if (!FLAT_NOUN.inText(text) || amounts.size > 1) return null
        val area = PLACE.find(text)?.groupValues?.get(1)?.removeSuffix(" area")?.trim()?.ifBlank { null }
        val maxRent = amounts.singleOrNull()?.paise?.div(100)
        if (area == null && maxRent == null) return null
        val gender = when {
            FEMALE.inText(text) -> "female"
            MALE.inText(text) -> "male"
            else -> null
        }
        return AgentQuery.FindFlats(area, null, maxRent, gender)
    }
}
