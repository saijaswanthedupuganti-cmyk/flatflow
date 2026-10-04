package habitiq.app.agent

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import habitiq.app.lib.formatInr
import java.time.LocalDate
import kotlin.math.roundToLong
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject

/**
 * What Gemini sees about the flat. People and tasks are replaced by short aliases (m1 is always the
 * person talking), and only first names are included: no emails, phones, photos or real ids. The
 * alias maps turn the model's answer back into real ids on the phone.
 */
data class PlannerContext(val json: String, val members: Map<String, String>, val tasks: Map<String, String>)

private const val CONTEXT_LIMIT = 8_000

fun buildPlannerContext(state: HouseholdState, today: LocalDate): PlannerContext {
    val ordered = state.members.sortedByDescending { it.uid == state.myUid }
    val memberAlias = ordered.mapIndexed { i, m -> "m${i + 1}" to m.uid }.toMap()
    val aliasOf = memberAlias.entries.associate { (k, v) -> v to k }
    val open = state.tasks.filter { it.status != "completed" && it.status != "paused" }
        .sortedBy { it.due ?: LocalDate.MAX }
    val taskAlias = open.mapIndexed { i, t -> "t${i + 1}" to t.id }.toMap()

    fun render(taskCount: Int): String {
        val root = JSONObject()
        root.put("today", "$today (${today.dayOfWeek.name.lowercase().replaceFirstChar(Char::uppercase)})")
        root.put("inFlat", state.inFlat)
        root.put("iAmAdmin", state.isAdmin)
        root.put("members", JSONArray(ordered.map { m ->
            JSONObject().put("id", aliasOf.getValue(m.uid)).put("name", m.firstName).put("isMe", m.uid == state.myUid)
        }))
        root.put("openTasks", JSONArray(open.take(taskCount).mapIndexed { i, t ->
            JSONObject().put("id", "t${i + 1}").put("name", t.name).put("assignee", aliasOf[t.assigneeUid] ?: "unknown")
                .put("due", t.due?.toString() ?: "").put("frequency", t.frequency)
        }))
        root.put("myBalancesRupees", JSONObject(state.myBalances.mapNotNull { (uid, v) ->
            aliasOf[uid]?.let { it to v.roundToLong() }
        }.toMap()))
        root.put("bills", JSONArray(state.bills.map { b ->
            JSONObject().put("name", b.name).put("due", b.due?.toString() ?: "").put("status", b.status)
                .put("amountRupees", b.amountPaise?.let { it / 100 } ?: JSONObject.NULL)
        }))
        return root.toString()
    }
    // Drop the furthest-out tasks first if the flat is huge.
    var count = open.size
    var json = render(count)
    while (json.length > CONTEXT_LIMIT && count > 0) { count -= 5; json = render(count.coerceAtLeast(0)) }
    return PlannerContext(json, memberAlias, taskAlias)
}

private val CATEGORIES = setOf("lifestyle", "bills", "chores", "other")
private val FREQUENCIES = setOf("daily", "weekly", "fortnightly", "monthly")
private val LINKS = mapOf(
    "join_flat" to AgentLink.JOIN_FLAT, "create_flat" to AgentLink.CREATE_FLAT, "expenses" to AgentLink.EXPENSES,
    "balances" to AgentLink.BALANCES, "tasks" to AgentLink.TASKS, "bills" to AgentLink.BILLS,
    "discover" to AgentLink.DISCOVER, "members" to AgentLink.MEMBERS, "profile" to AgentLink.PROFILE, "home" to AgentLink.HOME,
)
private val NAV_SAY = mapOf(
    AgentLink.JOIN_FLAT to "Opening join a flat. Ask your flatmate for the invite code.",
    AgentLink.CREATE_FLAT to "Let’s set up your flat.",
)

/**
 * Turns Gemini's JSON into a plan, enforcing the same rules as the rule-based parser. Anything
 * unknown, out of range, about someone else's task, or off-limits returns null or Unsupported.
 */
fun parseModelPlan(raw: String, ctx: PlannerContext, state: HouseholdState, today: LocalDate): AgentPlan? {
    val body = raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
    val o = runCatching { JSONObject(body) }.getOrNull() ?: return null
    val me = state.myUid
    fun uid(alias: String?): String? = alias?.let { ctx.members[it] }
    fun name(uid: String) = if (uid == me) "you" else state.members.firstOrNull { it.uid == uid }?.firstName ?: "them"
    fun paise(key: String): Long? {
        val v = o.optDouble(key, Double.NaN)
        if (v.isNaN()) return null
        val p = (v * 100).roundToLong()
        return p.takeIf { it in 100..10_000_000 }
    }
    fun guarded(text: String): AgentPlan.Unsupported? = ContentGuard.reasonToBlock(text)?.let { AgentPlan.Unsupported(it) }
    fun needFlat(): AgentPlan.Unsupported? = if (!state.inFlat) AgentPlan.Unsupported("Join or create a flat first.") else null

    return when (o.optString("type")) {
        "expense" -> {
            needFlat()?.let { return it }
            val title = o.optString("title").trim().take(40).replaceFirstChar(Char::uppercase)
            if (title.isBlank()) return null
            guarded(title)?.let { return it }
            val amount = paise("amount") ?: return null
            val payer = if (o.has("paidBy")) uid(o.optString("paidBy")) ?: return null else me
            val all = state.members.map { it.uid }
            val split: List<String> = when (val s = o.opt("splitWith")) {
                null, "all", JSONObject.NULL -> all
                is JSONArray -> {
                    val picked = (0 until s.length()).map { uid(s.optString(it)) ?: return null }.toSet() + payer
                    all.filter { it in picked }
                }
                else -> all
            }
            val category = o.optString("category").takeIf { it in CATEGORIES } ?: "other"
            val who = if (split.size == all.size) "split ${split.size} ways" else "split with ${split.size - 1} other" + if (split.size > 2) "s" else ""
            AgentPlan.Actions(
                listOf(AgentStep.AddExpense(title, amount, category, payer, split)),
                "Add ${formatInr(amount / 100.0)} for $title, $who",
            )
        }
        "settle" -> {
            needFlat()?.let { return it }
            val from = uid(o.optString("from")) ?: return null
            val to = uid(o.optString("to")) ?: return null
            if (from == to || (from != me && to != me)) return null
            val amount = paise("amount") ?: return null
            val money = formatInr(amount / 100.0)
            AgentPlan.Actions(
                listOf(AgentStep.Settle(from, to, amount)),
                if (from == me) "Record $money you paid ${name(to)}" else "Record $money ${name(from)} paid you",
            )
        }
        "task_done" -> {
            val id = ctx.tasks[o.optString("task")] ?: return null
            val task = state.tasks.firstOrNull { it.id == id && it.assigneeUid == me } ?: return null
            AgentPlan.Actions(listOf(AgentStep.CompleteTask(task.id, task.name)), "Mark ${task.name} as done")
        }
        "add_task" -> {
            needFlat()?.let { return it }
            if (!state.isAdmin) return AgentPlan.Unsupported("Only the flat admin can add tasks. Ask your admin to add it.")
            val name = o.optString("name").trim().take(40).replaceFirstChar(Char::uppercase)
            if (name.isBlank()) return null
            guarded(name)?.let { return it }
            val frequency = o.optString("frequency").takeIf { it in FREQUENCIES } ?: "weekly"
            AgentPlan.Actions(listOf(AgentStep.CreateTask(name, frequency)), "Add the $frequency task $name")
        }
        "away" -> {
            needFlat()?.let { return it }
            val away = o.optBoolean("away", true)
            AgentPlan.Actions(
                listOf(AgentStep.SetAway(away)),
                if (away) "Mark you as away so your flatmates know" else "Mark you as back in the flat",
            )
        }
        "question" -> when (o.optString("query")) {
            "my_balance" -> AgentPlan.Answer(AgentQuery.MyBalance)
            "who_owes" -> AgentPlan.Answer(AgentQuery.WhoOwes)
            "due_today" -> AgentPlan.Answer(AgentQuery.DueToday)
            "my_duties" -> AgentPlan.Answer(AgentQuery.MyDuties(today, today.plusDays(6)))
            "bills" -> AgentPlan.Answer(AgentQuery.Bills)
            else -> null
        }
        "find_flats" -> AgentPlan.Answer(
            AgentQuery.FindFlats(
                area = o.optString("area").trim().lowercase().ifBlank { null },
                city = null,
                maxRent = o.optLong("maxRent", 0L).takeIf { it > 0 },
                gender = o.optString("gender").takeIf { it == "male" || it == "female" },
            ),
        )
        "navigate" -> {
            val link = LINKS[o.optString("to")] ?: return null
            AgentPlan.Navigate(link, NAV_SAY[link] ?: "Opening ${link.label.removePrefix("Open ").removePrefix("Go ").lowercase()}.")
        }
        "reply" -> {
            val text = o.optString("text").trim().take(400)
            if (text.isBlank()) return null
            guarded(text)?.let { return it }
            AgentPlan.Reply(AgentAnswer(text, emptyList(), null))
        }
        "unsupported" -> AgentPlan.Unsupported(o.optString("reason").trim().take(200).ifBlank { "I can't help with that one." })
        else -> null
    }
}

private val SYSTEM_PROMPT = """
You are Oddroof, the voice assistant inside a shared-flat app used in India. Flatmates use you to track
shared expenses, payments between flatmates, household tasks, bills, going away, and to find flats.
People talk casually in English, often with Hindi or Telugu words (kharcha, kirana, ho gaya, ayipoyindi).

You get the person's request and a JSON snapshot of their flat. Members and tasks have short ids
(m1 is always the person talking). Reply with ONE JSON object only, no other text, using one of:
{"type":"expense","title":"<2-4 words>","amount":<rupees>,"category":"lifestyle|bills|chores|other","paidBy":"<member id>","splitWith":"all" or ["<member id>",...]}
{"type":"settle","from":"<member id>","to":"<member id>","amount":<rupees>}   (one side must be m1)
{"type":"task_done","task":"<task id>"}   (only tasks assigned to m1)
{"type":"add_task","name":"<task name>","frequency":"daily|weekly|fortnightly|monthly"}
{"type":"away","away":true|false}
{"type":"question","query":"my_balance|who_owes|due_today|my_duties|bills"}
{"type":"find_flats","area":"<area or null>","maxRent":<rupees or 0>,"gender":"male|female|null"}
{"type":"navigate","to":"join_flat|create_flat|expenses|balances|tasks|bills|discover|members|profile|home"}
{"type":"reply","text":"<one or two short friendly sentences>"}
{"type":"unsupported","reason":"<short reason>"}

Rules:
- Never invent amounts, people or tasks. Use only ids from the snapshot. If something needed is missing, use "reply" to ask for it.
- "who owes" means who owes m1 money. Use "question" for balances, tasks due and bills so the app shows exact numbers.
- To join someone's flat the person needs an invite code from that flat's admin: use navigate join_flat.
- Use "reply" for general questions about how the app works, kept short and practical.
- Refuse anything illegal, abusive, sexual or harmful with "unsupported". Never delete data, change roles or remove members.
""".trimIndent()

/** The model call failed (AI Logic not enabled, billing, App Check, network…). [reason] is safe to show. */
class PlannerUnavailable(val reason: String, cause: Throwable? = null) : Exception(reason, cause)

/**
 * Gemini through Firebase AI Logic on the Vertex AI backend, so calls bill to the project's Google Cloud
 * account (and its credits). The key stays inside Firebase, never in the app. Flash-Lite with thinking
 * off keeps replies around a second; if that model isn't available the next one in [MODELS] is tried.
 */
class GeminiPlanner(context: Context, private val models: List<String> = MODELS) : AgentPlanner {
    private val prefs = context.applicationContext.getSharedPreferences("oddroof_agent", Context.MODE_PRIVATE)
    private val ai by lazy { Firebase.ai(backend = GenerativeBackend.vertexAI(location = LOCATION)) }
    private val built = mutableMapOf<String, GenerativeModel>()
    /** Index into [models] of the first one that answered; sticks for the session. */
    @Volatile private var modelIndex = 0

    private fun model(name: String): GenerativeModel = built.getOrPut(name) {
        ai.generativeModel(
            modelName = name,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = 0.1f
                maxOutputTokens = 512
                thinkingConfig = thinkingConfig { thinkingBudget = 0 }
            },
            systemInstruction = content { text(SYSTEM_PROMPT) },
        )
    }

    override suspend fun plan(text: String, state: HouseholdState, today: LocalDate): AgentPlan? {
        if (!allowedToday(today)) {
            return AgentPlan.Reply(AgentAnswer("Smart requests are paused until tomorrow. Simple ones like “Spent 500 on groceries” still work.", emptyList(), null))
        }
        val ctx = buildPlannerContext(state, today)
        val prompt = "Flat snapshot:\n${ctx.json}\n\nRequest: \"${text.take(300)}\""
        var lastError: Throwable? = null
        while (modelIndex < models.size) {
            val name = models[modelIndex]
            val started = System.currentTimeMillis()
            val result = withTimeoutOrNull(TIMEOUT_MS) { runCatching { model(name).generateContent(prompt).text } }
            if (result == null) {
                Log.w(TAG, "$name timed out after ${TIMEOUT_MS}ms")
                throw PlannerUnavailable("Smart mode took too long to answer. Check your connection and try again.")
            }
            result.onSuccess { reply ->
                Log.d(TAG, "$name answered in ${System.currentTimeMillis() - started}ms")
                countCall(today)
                return reply?.let { parseModelPlan(it, ctx, state, today) }
            }
            val error = result.exceptionOrNull()!!
            lastError = error
            Log.w(TAG, "$name failed: ${error.javaClass.simpleName}: ${error.message}", error)
            runCatching { FirebaseCrashlytics.getInstance().recordException(error) }
            // A missing or retired model: try the next one. Anything else won't be fixed by switching.
            if (isModelMissing(error)) modelIndex++ else break
        }
        throw PlannerUnavailable(reasonFor(lastError), lastError)
    }

    // At most DAILY_LIMIT model calls per person per day, so a stuck loop or abuse can't burn credits.
    private fun allowedToday(today: LocalDate): Boolean =
        prefs.getString("day", "") != today.toString() || prefs.getInt("calls", 0) < DAILY_LIMIT

    private fun countCall(today: LocalDate) {
        val same = prefs.getString("day", "") == today.toString()
        prefs.edit().putString("day", today.toString()).putInt("calls", if (same) prefs.getInt("calls", 0) + 1 else 1).apply()
    }

    companion object {
        private const val TAG = "OddroofAgent"
        /** Fastest first. Change here to move to a newer Gemini model. */
        val MODELS = listOf("gemini-2.5-flash-lite", "gemini-2.5-flash")
        const val LOCATION = "global"
        const val TIMEOUT_MS = 6_000L
        const val DAILY_LIMIT = 60
    }
}

internal fun isModelMissing(error: Throwable?): Boolean {
    val m = (error?.message ?: "").lowercase()
    return "not found" in m || "404" in m || "was not found" in m || "is not supported" in m
}

/** Short, human reason for the overlay. The full error goes to Logcat and Crashlytics. */
internal fun reasonFor(error: Throwable?): String {
    val m = (error?.message ?: "").lowercase()
    return when {
        "api has not been used" in m || "is disabled" in m || "service_disabled" in m || "firebasevertexai" in m ->
            "Smart mode isn't switched on for this app yet (Firebase AI Logic)."
        "billing" in m -> "Smart mode needs billing turned on for the Firebase project."
        "app check" in m || "appcheck" in m -> "Smart mode was blocked by App Check."
        "permission" in m || "403" in m -> "Smart mode doesn't have permission to run yet."
        "quota" in m || "429" in m || "resource exhausted" in m -> "Smart mode is busy right now. Try again in a minute."
        "unable to resolve host" in m || "network" in m || "timeout" in m -> "No connection to smart mode. Check your internet."
        isModelMissing(error) -> "The smart model isn't available in this project yet."
        else -> "Smart mode couldn't answer right now."
    }
}
