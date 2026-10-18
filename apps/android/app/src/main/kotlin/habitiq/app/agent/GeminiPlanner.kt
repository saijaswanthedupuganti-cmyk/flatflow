package habitiq.app.agent

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ThinkingLevel
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
data class PlannerContext(
    val json: String,
    val members: Map<String, String>,
    val tasks: Map<String, String>,
    /** Listing alias (v1, v2…) -> flat id, for the model's picks. */
    val listings: Map<String, String> = emptyMap(),
)

private const val CONTEXT_LIMIT = 12_000
private const val MAX_LISTINGS = 30
private const val MAX_AREAS = 40

fun buildPlannerContext(state: HouseholdState, today: LocalDate): PlannerContext {
    val ordered = state.members.sortedByDescending { it.uid == state.myUid }
    val memberAlias = ordered.mapIndexed { i, m -> "m${i + 1}" to m.uid }.toMap()
    val aliasOf = memberAlias.entries.associate { (k, v) -> v to k }
    val open = state.tasks.filter { it.status != "completed" && it.status != "paused" }
        .sortedBy { it.due ?: LocalDate.MAX }
    val taskAlias = open.mapIndexed { i, t -> "t${i + 1}" to t.id }.toMap()
    // Public listings only (what Discover shows anyone): area, rent, who it's for, room and tags. Cheapest first.
    val listed = state.vacancies.sortedBy { it.rentPaise ?: Long.MAX_VALUE }
    val listingAlias = listed.mapIndexed { i, v -> "v${i + 1}" to v.flatId }.toMap()
    val areas = listed.groupBy { it.area.trim().lowercase() to it.city.trim().lowercase() }.values
        .sortedByDescending { it.size }.take(MAX_AREAS)
        .map { group ->
            val rents = group.mapNotNull { it.rentPaise?.div(100) }
            JSONObject().put("area", group.first().area.trim()).put("city", group.first().city.trim()).put("listings", group.size)
                .apply { if (rents.isNotEmpty()) put("rentFrom", rents.min()).put("rentTo", rents.max()) }
        }

    fun render(taskCount: Int, listingCount: Int = listed.size.coerceAtMost(MAX_LISTINGS)): String {
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
        if (areas.isNotEmpty()) root.put("listingAreas", JSONArray(areas))
        root.put("listings", JSONArray(listed.take(listingCount).mapIndexed { i, v ->
            JSONObject().put("id", "v${i + 1}").put("area", v.area.trim())
                .put("rentRupees", v.rentPaise?.let { it / 100 } ?: JSONObject.NULL)
                .put("for", v.preferredGender.ifBlank { "any" })
                .apply {
                    if (v.roomType.isNotBlank()) put("room", v.roomType)
                    if (v.furnishing.isNotBlank()) put("furnishing", v.furnishing)
                    if (v.tags.isNotEmpty()) put("tags", JSONArray(v.tags.take(6)))
                }
        }))
        return root.toString()
    }
    // Over budget: drop the priciest listings first, then the furthest-out tasks.
    var listings = listed.size.coerceAtMost(MAX_LISTINGS)
    var count = open.size
    var json = render(count, listings)
    while (json.length > CONTEXT_LIMIT && listings > 0) { listings -= 5; json = render(count, listings.coerceAtLeast(0)) }
    while (json.length > CONTEXT_LIMIT && count > 0) { count -= 5; json = render(count.coerceAtLeast(0), 0) }
    return PlannerContext(json, memberAlias, taskAlias, listingAlias)
}

/** A place name from the model: trimmed, lowercased, sane length, never "null". */
private fun place(raw: String?): String? =
    raw?.trim()?.lowercase()?.takeIf { it.isNotBlank() && it != "null" && it.length <= 40 }

private fun strings(a: JSONArray?, max: Int): List<String> =
    if (a == null) emptyList() else (0 until a.length()).mapNotNull { a.optString(it).trim().take(30).ifBlank { null } }.take(max)

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
    // Gemini's own words for the moment. The view model still checks any numbers in it against the app's.
    val say = o.optString("say").trim().take(220).takeIf { it.isNotEmpty() && it != "null" }
    return when (val plan = planFrom(o, ctx, state, today)) {
        is AgentPlan.Actions -> plan.copy(say = say)
        is AgentPlan.Answer -> plan.copy(say = say)
        is AgentPlan.Navigate -> if (say != null && ContentGuard.reasonToBlock(say) == null) plan.copy(say = say) else plan
        else -> plan
    }
}

private fun planFrom(o: JSONObject, ctx: PlannerContext, state: HouseholdState, today: LocalDate): AgentPlan? {
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
                area = place(o.optString("area")),
                city = place(o.optString("city")),
                maxRent = o.optLong("maxRent", 0L).takeIf { it in 1..1_000_000 },
                gender = o.optString("gender").takeIf { it == "male" || it == "female" },
                nearby = strings(o.optJSONArray("nearby"), 6).mapNotNull(::place),
                wants = strings(o.optJSONArray("wants"), 6).map { it.lowercase() },
                picks = strings(o.optJSONArray("picks"), 5).mapNotNull { ctx.listings[it] },
            ),
        )
        "area_rent" -> {
            val area = place(o.optString("area")) ?: return null
            AgentPlan.Answer(AgentQuery.AreaRent(area, strings(o.optJSONArray("nearby"), 6).mapNotNull(::place)))
        }
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
You are Oddroof, the assistant inside a shared-flat app used in India. You talk like a helpful friend who
lives in the flat: warm, relaxed, quick, never formal or robotic, never preachy. Flatmates use you to track
shared expenses, payments between flatmates, household tasks, bills, going away, and to find flats and rooms.
People talk casually in English, often with Hindi or Telugu words (kharcha, kirana, ho gaya, ayipoyindi,
kitna, kahan, ekkada). Their words often arrive through speech-to-text, so names and places may be misheard:
work out what they meant, don't take the transcript literally.

You get the person's request and a JSON snapshot of their flat. Members and tasks have short ids
(m1 is always the person talking). Reply with ONE JSON object only, no other text, using one of the shapes
below. Every shape except "reply" also takes "say": one short natural sentence, the way a friend would answer
out loud ("Done deal, 500 on groceries for all four of you. Save it?", "Kondapur's got a couple of options for you!").
In "say", only use numbers that appear in the request or the snapshot, never invent one, and use first names.
For anything that saves data, "say" ends by asking if you should save it.
{"type":"expense","title":"<2-4 words>","amount":<rupees>,"category":"lifestyle|bills|chores|other","paidBy":"<member id>","splitWith":"all" or ["<member id>",...]}
{"type":"settle","from":"<member id>","to":"<member id>","amount":<rupees>}   (one side must be m1)
{"type":"task_done","task":"<task id>"}   (only tasks assigned to m1)
{"type":"add_task","name":"<task name>","frequency":"daily|weekly|fortnightly|monthly"}
{"type":"away","away":true|false}
{"type":"question","query":"my_balance|who_owes|due_today|my_duties|bills"}
{"type":"find_flats","area":"<area or null>","city":"<city or null>","nearby":["<area>",...],"maxRent":<rupees or 0>,"gender":"male|female|null","wants":["<wish>",...],"picks":["<listing id>",...]}
{"type":"area_rent","area":"<area>","nearby":["<area>",...]}
{"type":"navigate","to":"join_flat|create_flat|expenses|balances|tasks|bills|discover|members|profile|home"}
{"type":"reply","text":"<one or two short friendly sentences>"}
{"type":"unsupported","reason":"<short friendly reason>"}

Finding flats (the snapshot has "listingAreas" and "listings", the live posts in Discover):
- Match the place by meaning and sound, not spelling. If the person's place sounds like or is a part of an area
  in listingAreas ("punjabpur", "punja gutta" -> "Punjagutta"; "hitech" -> "HITEC City"), use that exact area name.
- "nearby": up to 6 real localities close to the asked place, closest first, from your knowledge of the city
  (near Punjagutta: Somajiguda, Ameerpet, Banjara Hills, Khairatabad). Prefer ones in listingAreas. Use it for
  "near", "nearest", "around", "close to", and whenever the place itself may have few listings.
- A landmark, office, college or metro station is fine as "area": put the localities around it in "nearby".
- "wants": short wishes in plain words, e.g. veg, non veg, wifi, ac, furnished, semi furnished, parking, pets,
  food, maid, gym, washing machine, private room, sharing, non smoker, working professionals, students, quiet, metro.
- "picks": up to 5 listing ids that best fit everything they said, best first. Only ids from "listings".
- "maxRent" is per head per month in rupees: "under 10k" = 10000, "budget 8 to 10k" = 10000.
- "What's the rent / budget / going rate in X", "how much for a room in X": use area_rent.
- "Nearest flats" or "near me" with no place: reply and ask which area or landmark, in a friendly way.

Rules:
- Money said with no reason ("I just spent 500", "kharcha 300") is still an expense: title "Expense", split with all. Never refuse it or ask what it was for.
- "Add 500 on groceries", "groceries 500", "500 for milk" are expenses paid by m1 split with all unless they say otherwise.
- Never invent amounts, people, tasks, listings or rents. Use only ids from the snapshot. If something needed is missing, use "reply" to ask for it.
- "who owes" means who owes m1 money. Use "question" for balances, tasks due and bills so the app shows exact numbers.
- To join someone's flat the person needs an invite code from that flat's admin: use navigate join_flat.
- Use "reply" for small talk, thanks and how-the-app-works questions. Sound like a friend: short, kind, natural,
  mirror their language (a little Hinglish back is fine if they used it), no lists, no "As an AI", no numbers you weren't given.
- Refuse anything illegal, abusive, sexual or harmful with "unsupported", gently. Never delete data, change roles or remove members.
""".trimIndent()

/** The model call failed (AI Logic not enabled, billing, App Check, network…). [reason] is safe to show. */
class PlannerUnavailable(val reason: String, cause: Throwable? = null) : Exception(reason, cause)

/**
 * Gemini through Firebase AI Logic on the Vertex AI backend, so calls bill to the project's Google Cloud
 * account (and its credits). The key stays inside Firebase, never in the app. Flash-Lite with minimal
 * thinking keeps replies around a second; if that model isn't available the next one in [MODELS] is tried.
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
                temperature = 0.2f // a little warmth in replies; the JSON shape is enforced by the parser anyway
                maxOutputTokens = 512
                // Gemini 3+ can't switch thinking off (thinkingBudget = 0 is rejected); MINIMAL is the fastest level.
                thinkingConfig = thinkingConfig { thinkingLevel = ThinkingLevel.MINIMAL }
            },
            systemInstruction = content { text(SYSTEM_PROMPT) },
        )
    }

    override suspend fun plan(text: String, state: HouseholdState, today: LocalDate): AgentPlan? {
        if (!allowedToday(today)) {
            // Past the cap the rules take over (the view model falls back to them on this error).
            throw PlannerUnavailable("Smart replies are paused until tomorrow.")
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
        /**
         * Fastest first. Change here to move to a newer Gemini model. The 2.5 family isn't listed: on Vertex AI
         * it shuts down 2026-10-20 and was only ever open to projects that had already used it.
         * Every model here must accept ThinkingLevel.MINIMAL (3.7/3.8 Flash and Gemini 4 don't).
         */
        val MODELS = listOf("gemini-3.5-flash-lite", "gemini-3.1-flash-lite", "gemini-3.5-flash")
        const val LOCATION = "global"
        const val TIMEOUT_MS = 6_000L
        /** Every request now goes to Gemini first, so the cap is per person per day; rules take over past it. */
        const val DAILY_LIMIT = 200
    }
}

internal fun isModelMissing(error: Throwable?): Boolean {
    val m = (error?.message ?: "").lowercase()
    return "not found" in m || "404" in m || "is not supported" in m || "not available" in m ||
        "limited to" in m || "retired" in m
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
