package habitiq.app.agent

/**
 * Rule-based understanding for everything beyond money: help, navigation (join/create a flat,
 * "open expenses"), tasks (done / add), and going away. Each function returns null when the text
 * isn't its kind of request, so the parser can try the next rule.
 */
internal object CommandRules {
    private fun rx(p: String) = Regex("""\b(?:$p)\b""")

    val HELP = AgentAnswer(
        "Here’s what I can do",
        listOf(
            AnswerLine("Expenses", "“Spent 500 on groceries”"),
            AnswerLine("Payments", "“Paid Ravi 200”"),
            AnswerLine("Tasks", "“Dishes done”, “Add task mop daily”"),
            AnswerLine("Going away", "“Going home till Sunday”"),
            AnswerLine("Questions", "“What do I owe?”"),
            AnswerLine("Flats", "“Flats in Gachibowli under 10k”"),
            AnswerLine("Anywhere", "“Join a flat”, “Open expenses”"),
        ),
        null,
    )

    private val HELP_RX = rx("what can you do|how (?:do|can) i use you|help me|help|what do you do")
    private val JOIN_RX = Regex("""\bjoin\w*\b.*\b(?:flat|flats|home|house|household|room|flight|flights)\b|\binvite code\b""")
    private val CREATE_RX = Regex("""\b(?:create|make|start|set up|setup)\b.*\bflat\b""")
    private val NAV_VERB = rx("open|show|go to|take me to|navigate to|see|view")
    private val FLATMATE_RX = Regex("""\b(?:find|looking for|look for|need|search|search for)\b.*\b(?:flatmates?|roommates?|room mates?)\b""")
    private val NAV_TARGETS = listOf(
        rx("balances?") to AgentLink.BALANCES,
        rx("expenses?|money|spending") to AgentLink.EXPENSES,
        rx("tasks?|chores?|duties|duty") to AgentLink.TASKS,
        rx("bills?") to AgentLink.BILLS,
        rx("discover|listings?") to AgentLink.DISCOVER,
        rx("members|flatmates|roommates") to AgentLink.MEMBERS,
        rx("profile|account|settings") to AgentLink.PROFILE,
        rx("home|dashboard") to AgentLink.HOME,
    )

    fun help(text: String): AgentPlan? = if (HELP_RX.containsMatchIn(text)) AgentPlan.Reply(HELP) else null

    /** Join / create a flat. These work even when the person has no flat yet. */
    fun flatSetup(text: String): AgentPlan? = when {
        JOIN_RX.containsMatchIn(text) ->
            AgentPlan.Navigate(AgentLink.JOIN_FLAT, "Opening join a flat. Ask your flatmate for the invite code.")
        CREATE_RX.containsMatchIn(text) ->
            AgentPlan.Navigate(AgentLink.CREATE_FLAT, "Let’s set up your flat.")
        else -> null
    }

    fun navigate(text: String): AgentPlan? {
        if (FLATMATE_RX.containsMatchIn(text)) {
            return AgentPlan.Navigate(AgentLink.DISCOVER, "Opening Discover so you can find flatmates.")
        }
        if (!NAV_VERB.containsMatchIn(text)) return null
        val link = NAV_TARGETS.firstOrNull { it.first.containsMatchIn(text) }?.second ?: return null
        return AgentPlan.Navigate(link, "Opening ${link.label.removePrefix("Open ").removePrefix("Go ").lowercase()}.")
    }

    private val EXPENSE_PROMPT_RX = Regex("""^(?:i want to |can you |please )?(?:add|log|record|enter|split|new)\s+(?:an?\s+|the\s+)?expenses?$""")
    fun expensePrompt(text: String): AgentPlan? =
        if (EXPENSE_PROMPT_RX.matches(text)) AgentPlan.Reply(AgentAnswer("Tell me the amount and what it was for, like “Groceries 560”.", emptyList(), null))
        else null

    private val AWAY_RX = rx(
        "going home|going to (?:my )?(?:home|hometown|native|village)|out of station|travell?ing|going away|leaving town|on leave|on vacation|on holiday|not (?:be )?(?:here|home|around)"
    )
    private val BACK_RX = rx("im back|i am back|i have returned|im home again|back in the flat")

    fun away(text: String, state: HouseholdState): AgentPlan? {
        val back = BACK_RX.containsMatchIn(text)
        if (!back && !AWAY_RX.containsMatchIn(text)) return null
        if (!state.inFlat) return AgentPlan.Unsupported("Join or create a flat first.")
        return if (back) AgentPlan.Actions(listOf(AgentStep.SetAway(false)), "Mark you as back in the flat")
        else AgentPlan.Actions(listOf(AgentStep.SetAway(true)), "Mark you as away so your flatmates know")
    }

    private val CREATE_TASK_RX = Regex("""^(?:please\s+)?(?:add|create|make|new)\s+(?:a\s+|new\s+)*(?:task|chore|duty)\s+(?:called\s+|named\s+|to\s+)?(.*)$""")
    private val FREQUENCIES = listOf(
        Regex("""\s*\b(?:daily|every day|everyday)$""") to "daily",
        Regex("""\s*\b(?:fortnightly|every two weeks|every 2 weeks)$""") to "fortnightly",
        Regex("""\s*\b(?:weekly|every week)$""") to "weekly",
        Regex("""\s*\b(?:monthly|every month)$""") to "monthly",
    )

    fun createTask(text: String, state: HouseholdState): AgentPlan? {
        val m = CREATE_TASK_RX.matchEntire(text) ?: return null
        if (!state.inFlat) return AgentPlan.Unsupported("Join or create a flat first.")
        if (!state.isAdmin) return AgentPlan.Unsupported("Only the flat admin can add tasks. Ask your admin to add it.")
        var name = m.groupValues[1].trim()
        var frequency = "weekly"
        for ((re, f) in FREQUENCIES) {
            if (re.containsMatchIn(name)) { frequency = f; name = name.replace(re, "").trim(); break }
        }
        if (name.isBlank()) return AgentPlan.Reply(AgentAnswer("What should the task be called?", emptyList(), null))
        if (name.startsWith("for ")) return null
        val title = name.replaceFirstChar { it.uppercase() }
        return AgentPlan.Actions(listOf(AgentStep.CreateTask(title, frequency)), "Add the $frequency task $title")
    }

    private val DONE_RX = rx("done|finished|finish|completed|complete|did|ho gaya|hogaya|ayipoyindi|ayindi|cleared")
    private val TASK_STOP = setOf("the", "a", "an", "out", "up", "of", "to", "and", "for", "my", "with", "in", "on")
    private val CLOSED = setOf("completed", "paused")

    private fun stem(w: String): String = when {
        w.length > 5 && w.endsWith("ing") -> w.dropLast(3)
        w.length > 4 && w.endsWith("es") -> w.dropLast(2)
        w.length > 4 && w.endsWith("ed") -> w.dropLast(2)
        w.length > 3 && w.endsWith("s") -> w.dropLast(1)
        else -> w
    }

    private fun wordsMatch(a: String, b: String): Boolean =
        a == b || (a.length >= 4 && b.length >= 4 && editDistance(a, b) <= 1)

    /** "Dishes done" → complete the person's own open task whose name matches. */
    fun completeTask(text: String, state: HouseholdState): AgentPlan? {
        if (!DONE_RX.containsMatchIn(text) || !state.inFlat) return null
        val said = text.split(' ').filter { it !in TASK_STOP }.map(::stem)
        val mine = state.tasks.filter { it.assigneeUid == state.myUid && it.status !in CLOSED }
        val scored = mine.map { task ->
            val words = normalizeUtterance(task.name).split(' ').filter { it.length >= 3 && it !in TASK_STOP }.map(::stem)
            task to words.count { w -> said.any { wordsMatch(it, w) } }
        }.filter { it.second > 0 }
        if (scored.isEmpty()) return null
        val best = scored.maxOf { it.second }
        val top = scored.filter { it.second == best }.map { it.first }
        fun planFor(t: AgentTask) = AgentPlan.Actions(listOf(AgentStep.CompleteTask(t.id, t.name)), "Mark ${t.name} as done")
        return if (top.size == 1) planFor(top.single())
        else AgentPlan.Clarify("Which task did you finish?", top.map { ClarifyOption(it.name, planFor(it)) })
    }
}
