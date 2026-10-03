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
        // auxiliaries: speech recognisers rarely add "?", so these mark a question too
        "has", "have", "had", "should", "could", "would", "will", "shall", "was", "were", "tell", "kya", "why",
    )

    fun parse(raw: String, state: HouseholdState, today: LocalDate): ParseResult {
        val text = normalizeUtterance(raw)
        if (text.isBlank()) return ParseResult.NeedsModel
        val amounts = findAmounts(text)

        if (amounts.isEmpty()) {
            CommandRules.help(text)?.let { return ParseResult.Confident(it) }
            CommandRules.flatSetup(text)?.let { return ParseResult.Confident(it) }
        }
        if (amounts.isEmpty()) question(text, today)?.let { return answer(it) }
        findFlats(text, amounts)?.let { return answer(it) }

        val tokens = Regex("""\S+""").findAll(text).toList()
        // "I will pay…", "Should I buy…": a question or intention in either of the first two words.
        if (amounts.isEmpty()) CommandRules.navigate(text)?.let { return ParseResult.Confident(it) }
        if (tokens.take(2).any { it.value in QUESTION_START } || raw.trim().endsWith("?")) return ParseResult.NeedsModel
        if (amounts.isEmpty()) {
            CommandRules.away(text, state)?.let { return ParseResult.Confident(it) }
            CommandRules.createTask(text, state)?.let { return ParseResult.Confident(it) }
            CommandRules.completeTask(text, state)?.let { return ParseResult.Confident(it) }
            CommandRules.expensePrompt(text)?.let { return ParseResult.Confident(it) }
        }
        if (amounts.size != 1) return ParseResult.NeedsModel
        if (!state.inFlat || state.members.isEmpty()) {
            return ParseResult.Confident(AgentPlan.Unsupported("Join or create a flat first to track money."))
        }
        val amount = amounts.single()
        if (amount.paise < MIN_PAISE || amount.paise > MAX_PAISE) return ParseResult.NeedsModel

        val ctx = Ctx(tokens, amount, state)
        settle(ctx)?.let { return it }
        return expense(ctx) ?: ParseResult.NeedsModel
    }

    private const val MIN_PAISE = 100L
    private const val MAX_PAISE = 10_000_000L

    private data class Category(val title: String, val category: String)

    private val CATEGORIES: Map<String, Category> = buildMap {
        listOf("groceries", "grocery", "kirana", "vegetables", "veggies", "sabzi", "milk", "fruits", "eggs", "bread")
            .forEach { put(it, Category("Groceries", "lifestyle")) }
        listOf("swiggy", "zomato", "food", "dinner", "lunch", "breakfast", "snacks", "pizza", "biryani")
            .forEach { put(it, Category("Food", "lifestyle")) }
        listOf("electricity", "current", "wifi", "internet", "gas", "cylinder", "water", "rent", "maintenance", "recharge", "bill")
            .forEach { put(it, Category("Bill", "bills")) }
        listOf("cleaning", "detergent", "soap", "supplies", "maid", "cook", "dustbin", "mop")
            .forEach { put(it, Category("Supplies", "chores")) }
    }

    private val SPEND_VERBS = setOf("spent", "spend", "kharcha", "kharchu")
    private val EXPENSE_VERBS = setOf("bought", "buy", "spent", "spend", "paid", "ordered", "order", "got", "kharcha", "kharchu", "purchased")
    private val PAY_VERBS = setOf("paid", "gave", "sent", "transferred", "returned", "pay", "give", "send", "return", "transfer")
    private val RECEIVE_VERBS = setOf("received", "got", "receive", "get")
    private val SELF = setOf("i", "me", "myself")
    private val SPLIT_WORDS = setOf("split", "between", "among", "with")
    private val EVERYONE = setOf("everyone", "all", "us", "everybody")
    private val FILLER = setOf(
        "for", "of", "on", "the", "a", "an", "rs", "rupees", "inr", "₹", "worth", "and", "from", "to", "back",
        "at", "in", "my", "our", "just", "total", "k", "lakh", "bucks", "some", "flat", "house", "home", "we",
        "today", "yesterday", "ago", "day", "days", "now", "it", "this", "that", "was", "is", "hai", "ka", "ki",
        // Hinglish/Tenglish glue words that are never what the money was for
        "aaj", "kal", "abhi", "hua", "ho", "gaya", "maine", "ne", "ko", "naa", "nenu", "ivala", "ayyindi",
        // quantity units ("2 kg rice") are never the title
        "kg", "kgs", "g", "gm", "grams", "litre", "litres", "ltr", "ml", "packet", "packets", "pcs", "pieces", "dozen",
    )
    private val VOCAB = QUESTION_START + EXPENSE_VERBS + PAY_VERBS + RECEIVE_VERBS + SELF + SPLIT_WORDS +
        EVERYONE + FILLER + CATEGORIES.keys

    private class Ctx(val tokens: List<MatchResult>, val amount: AmountMatch, val state: HouseholdState) {
        val words = tokens.map { it.value }
        fun isAmount(i: Int) = tokens[i].range.first <= amount.range.last && tokens[i].range.last >= amount.range.first
        fun mention(i: Int): List<AgentMember> =
            if (i !in words.indices || isAmount(i)) emptyList()
            else matchMembers(words[i], state.members, fuzzy = words[i] !in VOCAB)
        fun others(i: Int) = mention(i).filter { it.uid != state.myUid }
        fun name(uid: String) = state.members.firstOrNull { it.uid == uid }?.firstName ?: "someone"
        val money get() = habitiq.app.lib.formatInr(amount.paise / 100.0)
    }

    private fun settle(c: Ctx): ParseResult? {
        val me = c.state.myUid
        val w = c.words
        fun result(candidates: List<AgentMember>, step: (String) -> AgentStep.Settle): ParseResult {
            fun plan(uid: String): AgentPlan.Actions {
                val s = step(uid)
                val summary = if (s.fromUid == me) "Record ${c.money} you paid ${c.name(s.toUid)}"
                    else "Record ${c.money} ${c.name(s.fromUid)} paid you"
                return AgentPlan.Actions(listOf(s), summary)
            }
            if (candidates.size == 1) return ParseResult.Confident(plan(candidates.single().uid))
            return ParseResult.Confident(AgentPlan.Clarify(
                "Which ${candidates.first().firstName}?",
                candidates.map { ClarifyOption(it.name, plan(it.uid)) },
            ))
        }

        // "<name> paid me", "<name> gave me", "<name> sent me"
        for (i in 0 until w.size - 2) {
            if (w[i + 1] in PAY_VERBS && w[i + 2] == "me") {
                val c1 = c.others(i)
                if (c1.isNotEmpty()) return result(c1) { AgentStep.Settle(it, me, c.amount.paise) }
            }
        }
        // "received / got <amount> from <name>"
        val r = w.indexOfFirst { it in RECEIVE_VERBS }
        if (r >= 0) {
            val f = w.indexOf("from").takeIf { it > r }
            if (f != null) {
                val c1 = c.others(f + 1)
                if (c1.isNotEmpty()) return result(c1) { AgentStep.Settle(it, me, c.amount.paise) }
            }
        }
        // "paid / gave / sent <name>", "sent <amount> to <name>"
        val p = w.indexOfFirst { it in PAY_VERBS }
        if (p >= 0) {
            var j = p + 1
            while (j < w.size && (w[j] == "to" || w[j] == "back" || c.isAmount(j))) j++
            val direct = c.others(j)
            if (direct.isNotEmpty()) return result(direct) { AgentStep.Settle(me, it, c.amount.paise) }
            val hasCategory = w.any { it in CATEGORIES }
            val t = w.indexOf("to").takeIf { it > p }
            if (t != null && !hasCategory) {
                val c1 = c.others(t + 1)
                if (c1.isNotEmpty()) return result(c1) { AgentStep.Settle(me, it, c.amount.paise) }
            }
        }
        return null
    }

    private fun expense(c: Ctx): ParseResult? {
        val me = c.state.myUid
        val w = c.words
        val verb = w.indexOfFirst { it in EXPENSE_VERBS }
        val categoryWord = w.firstOrNull { it in CATEGORIES }
        if (verb < 0 && categoryWord == null) return null

        // Payer: "<name> bought ..." / "<name> paid 640 for ..."
        var payer = me
        if (verb > 0) {
            val who = c.others(verb - 1)
            if (who.size > 1) return ParseResult.NeedsModel
            if (who.size == 1) payer = who.single().uid
        }

        // Split: names after split/between/among/with; default everyone.
        val allUids = c.state.members.map { it.uid }
        val splitAt = w.indexOfFirst { it in SPLIT_WORDS }
        val split: List<String> = if (splitAt < 0) allUids else {
            val picked = mutableSetOf(payer)
            var everyone = false
            for (k in splitAt + 1 until w.size) {
                when {
                    w[k] in SELF -> picked += me
                    w[k] in EVERYONE -> everyone = true
                    w[k] in FILLER || w[k] in SPLIT_WORDS || c.isAmount(k) -> Unit   // "split with priya"
                    else -> {
                        val m = c.mention(k)
                        if (m.size != 1) return ParseResult.NeedsModel
                        picked += m.single().uid
                    }
                }
            }
            if (everyone || picked.size == 1) allUids else allUids.filter { it in picked }
        }

        // Title: the content words that are left.
        val content = w.indices
            .filter { i -> !c.isAmount(i) }
            .filter { i -> w[i] !in FILLER && w[i] !in EXPENSE_VERBS && w[i] !in SELF && w[i] !in SPLIT_WORDS && w[i] !in EVERYONE }
            .filter { i -> w[i] in CATEGORIES || c.mention(i).isEmpty() }   // drop member names
            .filter { i -> splitAt < 0 || i < splitAt }                      // names after "split with" are people
            .map { w[it] }
            .filter { word -> word.any(Char::isLetter) }
            .take(4)
        // "I just spent 500" is the core lazy-user case: record it as a plain "Expense" (editable in M2's plan card)
        // rather than refusing. Only spend verbs get this default; a bare "paid 300" still needs more detail.
        val title = content.joinToString(" ").replaceFirstChar { it.uppercase() }.ifBlank {
            categoryWord?.let { CATEGORIES.getValue(it).title }
                ?: if (verb >= 0 && w[verb] in SPEND_VERBS) "Expense" else return ParseResult.NeedsModel
        }
        val category = (content.firstNotNullOfOrNull { CATEGORIES[it] } ?: categoryWord?.let { CATEGORIES[it] })?.category ?: "other"

        val step = AgentStep.AddExpense(title, c.amount.paise, category, payer, split)
        val who = if (split.size == allUids.size) "split ${split.size} ways" else "split with ${split.size - 1} other" + if (split.size > 2) "s" else ""
        return ParseResult.Confident(AgentPlan.Actions(listOf(step), "Add ${c.money} for $title, $who"))
    }

    private fun answer(q: AgentQuery) = ParseResult.Confident(AgentPlan.Answer(q))

    private fun Regex.inText(text: String) = containsMatchIn(text)
    private fun rx(p: String) = Regex("""\b(?:$p)\b""")

    private val WHO_OWES = rx("who (?:owes|all owe|still owes|hasnt paid|has not paid|havent paid|have not paid)")
    private val MY_BALANCE = rx("(?:what|how much) do i owe|my balance|do i owe|am i owed|kitna dena|how much (?:should|do) i pay|my dues")
    private val DUE_TODAY = rx("due today|whats due|what is due|todays tasks|what do i have today|anything due|(?:need|have) to do today")
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
