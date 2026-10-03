# Oddroof Agent — Milestone 1 (Agent Shell) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A simple mic button in the centre of the bottom nav opens a bottom sheet where the person speaks or types a request. A voice-reactive aura and live transcript show while they speak. Money questions, task questions, bills and flat searches are answered from live app data, and money commands ("I just spent 500", "Paid Ravi 200") show as rich expense or payment cards (read-only in M1). Zero AI cost.

**Architecture:** Pure-Kotlin logic in `habitiq.app.agent` (model, amount/date/name parsing, `LocalIntentParser`, `QueryResolver`, `AgentViewModel`) is fed by an immutable `HouseholdState` built from `FlatViewModel` flows, so all of it is unit-testable without Android. Speech sits behind a `SpeechSource` interface with one Android implementation. The UI is a regular `HqBottomSheet` plus a mic button that replaces the centre `+` (long-press on the mic keeps Quick add).

**Tech Stack:** Kotlin 2.2, Jetpack Compose (BOM 2025.09), AndroidX ViewModel, Android `SpeechRecognizer`, JUnit 4, kotlinx-coroutines-test, Robolectric screenshots.

**Spec:** `docs/superpowers/specs/2026-10-03-oddroof-agent-design.md`

### Scope changes from the spec (agreed with Sai on 2026-10-03)

- **No glass, no orb in M1.** Sai: "everything the same, simply put a mic in the centre". The centre button becomes a plain mic in the existing raised-button style. There is no Haze dependency, no glass tokens and no orb animation. The agent sheet is the existing `HqBottomSheet`. Glass moves to sub-project 3 (whole-app glass), where the screen spacing gets reworked anyway.
- **Money commands are preview-only.** Plan cards with Approve arrive in M2. In M1 an expense or settle command shows its card with an "Open Expenses" button.
- **The agent sheet is where the app feels alive** (Sai, 2026-10-03). The product thesis: people won't fill in sheets or open reports, but they will say "I just spent 500". So the sheet has a voice-reactive aura while listening, the live transcript in large type, and results as rich cards (a receipt-style expense card with a count-up amount and split avatars, and a payment card with two avatars and an arrow). "I spent 500" with no subject records as a plain "Expense" instead of being refused.
- **No model fallback yet.** Requests the parser can't resolve show "I can't do that one yet" with examples. Gemini arrives in M3.

## Global Constraints

- Package names: logic `habitiq.app.agent`, Compose UI `habitiq.app.ui.agent`.
- Android: `minSdk = 26`, `compileSdk = 36`; the app is `apps/android`, package `habitiq.app`.
- Speech locale `en-IN`; prefer on-device recognition (`EXTRA_PREFER_OFFLINE`).
- Inside the agent, amounts are integer **paise** (`Long`).
- Valid agent amounts: ₹1 to ₹1,00,000 inclusive (100 to 10,000,000 paise).
- Name matching: normalised lowercase, edit distance ≤ 1 only for names ≥ 4 chars; two or more matches → `Clarify`, never a guess.
- The parser returns `Confident` only when every required slot is filled unambiguously. **0 wrong `Confident` results** is the target; `NeedsModel` is always allowed.
- No new Firestore write paths. M1 makes **no writes at all**.
- Transcripts are never persisted or logged.
- Accessibility: 48dp minimum targets, mic button labelled "Talk to Oddroof", sheet state changes announced through a polite live region.
- Expense categories stored lowercase, from the existing set: `chores`, `lifestyle`, `bills`, `other`.
- Copy: plain, short, sentence case, contractions OK. No emoji in UI strings except the ✓ tick.
- Commit after every task; message ends with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.

## Review Focus

1. **Question-shaped sentences that contain an amount** ("what did I spend on groceries 500?") must never become an expense. Pinned in Task 5 (`questions never become actions`).
2. **Numbers that are not money** ("2 bhk", "4 oct", "3 days") must not be read as amounts. Pinned in Task 2 (`non-money numbers are skipped`).
3. **Common words that look like names** ("gave" against a member called Dave) must not match by edit distance. Pinned in Task 3 (`vocabulary words only match exactly`).
4. **Signed-out or no-flat users** tapping the mic must get a clear "join a flat first" answer, not a crash or an empty card. Pinned in Task 5 (`not in a flat`) and Task 6 (`answers without a flat`).
5. **Misuse:** illegal goods ("ganja 500"), abuse or threats must be refused before parsing, while look-alike household words ("weeding", "coke", "killed the cockroaches") pass. Pinned in Task 5b and Task 8 (`misuse is refused before parsing`).
6. **Mic permission denied, or no recogniser on the device** must land in text mode with a reason, never a stuck "Listening…". Pinned in Task 8 (`permission error switches to text mode`, `unavailable recogniser switches to text mode`).

## File Structure

| File | Responsibility |
|---|---|
| Create `app/src/main/kotlin/habitiq/app/agent/AgentPlan.kt` | Sealed plan, step, query and clarify model; `ParseResult`; `AgentAnswer` |
| Create `app/src/main/kotlin/habitiq/app/agent/HouseholdState.kt` | Immutable agent view of the flat and the pure builder `householdStateOf` |
| Create `app/src/main/kotlin/habitiq/app/agent/AmountParser.kt` | Finds money amounts in normalised text (digits, ₹/rs, k/lakh, number words) |
| Create `app/src/main/kotlin/habitiq/app/agent/DatePhrases.kt` | today/tomorrow/this week/next week/weekend → date range |
| Create `app/src/main/kotlin/habitiq/app/agent/NameMatcher.kt` | Member matching by first name with edit distance |
| Create `app/src/main/kotlin/habitiq/app/agent/LocalIntentParser.kt` | Text → `ParseResult` (questions, flat search, settle, expense) |
| Create `app/src/main/kotlin/habitiq/app/agent/ContentGuard.kt` | Acceptable-use gate: refuses illegal goods, abuse and threats before parsing |
| Create `app/src/main/kotlin/habitiq/app/agent/QueryResolver.kt` | `AgentQuery` → `AgentAnswer` from live data; `cardFor` builds action cards |
| Create `app/src/main/kotlin/habitiq/app/agent/VoiceInput.kt` | `SpeechSource` interface, `SpeechEvent`, `AndroidVoiceInput`, error mapping |
| Create `app/src/main/kotlin/habitiq/app/agent/AgentViewModel.kt` | UI state machine |
| Create `app/src/main/kotlin/habitiq/app/ui/agent/AgentVisuals.kt` | Listening aura, expense/payment/answer cards, avatars |
| Create `app/src/main/kotlin/habitiq/app/ui/agent/AgentSheet.kt` | Bottom sheet layout for every state |
| Modify `app/src/main/kotlin/habitiq/app/ui/AppShell.kt` | Optional mic button in the centre slot (tap = agent, long-press = Quick add) |
| Modify `app/src/main/kotlin/habitiq/app/HabitiqApp.kt` | Create the `AgentViewModel`, mic permission, open sheet, deep links |
| Modify `app/src/main/AndroidManifest.xml` | `RECORD_AUDIO` permission and recogniser `<queries>` entry |
| Modify `gradle/libs.versions.toml`, `app/build.gradle.kts` | `kotlinx-coroutines-test` (test only) |
| Tests under `app/src/test/kotlin/habitiq/app/agent/` | One test file per logic unit |
| Modify `app/src/test/kotlin/habitiq/app/screenshots/ShellScreenshotTest.kt`; create `AgentSheetScreenshotTest.kt` | Visual checks |

All paths below are relative to `apps/android/` unless they start with `docs/`. Run Gradle from `C:\garbage\apps\android`.

---

### Task 1: Agent model and household state

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/agent/AgentPlan.kt`
- Create: `app/src/main/kotlin/habitiq/app/agent/HouseholdState.kt`
- Test: `app/src/test/kotlin/habitiq/app/agent/HouseholdStateTest.kt`

**Interfaces:**
- Consumes: `habitiq.app.flats.Member(uid, nickname, role, ...)`, `habitiq.app.flats.FlatInfo(id, name, adminUid, ...)`, `habitiq.app.data.FlatTask`, `habitiq.app.data.BillInstance`, `habitiq.app.data.VacancyListing`, `habitiq.app.lib.parseTaskLocalDate(String): LocalDate?`
- Produces: everything in `AgentPlan.kt` exactly as written below; `AgentMember`, `AgentTask`, `AgentBill`, `AgentVacancy`, `HouseholdState`, `HouseholdState.Companion.Empty`, `householdStateOf(...)`, `firstNameOf(String): String`

- [ ] **Step 1: Write the failing test**

```kotlin
package habitiq.app.agent

import habitiq.app.data.BillInstance
import habitiq.app.data.FlatTask
import habitiq.app.data.VacancyListing
import habitiq.app.flats.FlatInfo
import habitiq.app.flats.Member
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HouseholdStateTest {
    private val flat = FlatInfo(id = "f1", name = "Lake View", adminUid = "u1", memberCount = 2)
    private val members = listOf(Member("u1", "Sai Jaswanth", "admin"), Member("u2", "  ravi kumar ", "member"))

    @Test fun `first names come from nicknames`() {
        assertEquals("Sai", firstNameOf("Sai Jaswanth"))
        assertEquals("Ravi", firstNameOf("  ravi kumar "))
        assertEquals("Member", firstNameOf("   "))
    }

    @Test fun `builds members tasks bills and vacancies`() {
        val tasks = listOf(FlatTask("t1", "Dishes", currentAssignedUserId = "u1", status = "pending", dueDate = "2026-10-03"))
        val bills = listOf(
            BillInstance(id = "b1", templateId = "x", month = "2026-10", name = "Wifi", amount = 999.0, paidBy = "u1", dueDate = "2026-10-05"),
            BillInstance(id = "b2", templateId = "y", month = "2026-09", name = "Old", paidBy = "u1", dueDate = "2026-09-05"),
        )
        val vacancies = listOf(
            VacancyListing("f9", "Green Nest", true, "Hyderabad", "Gachibowli", 9500.0, "INR", 1, "female", ""),
            VacancyListing("f8", "Closed", false, "Hyderabad", "Kondapur", 7000.0, "INR", 1, "any", ""),
        )
        val s = householdStateOf("u1", flat, members, tasks, mapOf("u1" to -120.5), bills, vacancies, "2026-10")

        assertTrue(s.inFlat)
        assertTrue(s.isAdmin)
        assertEquals(listOf(AgentMember("u1", "Sai", "Sai Jaswanth"), AgentMember("u2", "Ravi", "ravi kumar")), s.members)
        assertEquals(AgentTask("t1", "Dishes", "u1", LocalDate.of(2026, 10, 3), "weekly", "pending"), s.tasks.single())
        assertEquals(listOf("Wifi"), s.bills.map { it.name })
        assertEquals(99_900L, s.bills.single().amountPaise)
        assertEquals(listOf("f9"), s.vacancies.map { it.flatId })
        assertEquals(950_000L, s.vacancies.single().rentPaise)
        assertEquals(-120.5, s.netBalances.getValue("u1"), 0.0)
    }

    @Test fun `no flat means an empty household`() {
        val s = householdStateOf("u1", null, emptyList(), emptyList(), emptyMap(), emptyList(), emptyList(), "2026-10")
        assertFalse(s.inFlat)
        assertFalse(s.isAdmin)
        assertFalse(HouseholdState.Empty.inFlat)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.HouseholdStateTest"`
Expected: FAIL, compilation errors `Unresolved reference: householdStateOf` / `AgentMember`.

- [ ] **Step 3: Write `AgentPlan.kt`**

```kotlin
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
```

M2 adds `CompleteTask`, `SwapTask`, `CreateTask` and `GoingAway` to `AgentStep`. Leave them out now (YAGNI).

- [ ] **Step 4: Write `HouseholdState.kt`**

```kotlin
package habitiq.app.agent

import habitiq.app.data.BillInstance
import habitiq.app.data.FlatTask
import habitiq.app.data.VacancyListing
import habitiq.app.flats.FlatInfo
import habitiq.app.flats.Member
import habitiq.app.lib.parseTaskLocalDate
import java.time.LocalDate
import kotlin.math.roundToLong

/** [name] is the full nickname. It's used only for on-screen clarify labels and never leaves the device. */
data class AgentMember(val uid: String, val firstName: String, val name: String)

data class AgentTask(
    val id: String,
    val name: String,
    val assigneeUid: String,
    val due: LocalDate?,
    val frequency: String,
    val status: String,
)

data class AgentBill(val name: String, val amountPaise: Long?, val due: LocalDate?, val paidByUid: String, val status: String)

data class AgentVacancy(
    val flatId: String,
    val flatName: String,
    val area: String,
    val city: String,
    val rentPaise: Long?,
    val preferredGender: String,
)

/** The agent's read-only view of the person's flat, rebuilt from live state for every request. */
data class HouseholdState(
    val myUid: String,
    val inFlat: Boolean,
    val isAdmin: Boolean,
    val members: List<AgentMember>,
    val tasks: List<AgentTask>,
    /** Rupees per uid, this month. Positive = the flat owes them; negative = they owe. */
    val netBalances: Map<String, Double>,
    val bills: List<AgentBill>,
    val vacancies: List<AgentVacancy>,
) {
    companion object {
        val Empty = HouseholdState("", false, false, emptyList(), emptyList(), emptyMap(), emptyList(), emptyList())
    }
}

fun firstNameOf(nickname: String): String =
    nickname.trim().substringBefore(' ').ifBlank { "Member" }.replaceFirstChar { it.uppercase() }

private fun Double.toPaise(): Long = (this * 100).roundToLong()

fun householdStateOf(
    myUid: String,
    flat: FlatInfo?,
    members: List<Member>,
    tasks: List<FlatTask>,
    netBalances: Map<String, Double>,
    billInstances: List<BillInstance>,
    vacancies: List<VacancyListing>,
    month: String,
): HouseholdState = HouseholdState(
    myUid = myUid,
    inFlat = flat != null,
    isAdmin = flat != null && flat.adminUid == myUid,
    members = members.map { AgentMember(it.uid, firstNameOf(it.nickname), it.nickname.trim()) },
    tasks = tasks.map { AgentTask(it.taskId, it.name, it.currentAssignedUserId, parseTaskLocalDate(it.dueDate), it.frequency, it.status) },
    netBalances = netBalances,
    bills = billInstances.filter { it.month == month }
        .map { AgentBill(it.name, it.amount?.toPaise(), parseTaskLocalDate(it.dueDate), it.paidBy, it.status) },
    vacancies = vacancies.filter { it.active }
        .map { AgentVacancy(it.flatId, it.flatName, it.area, it.city, it.rentPerHead?.toPaise(), it.preferredGender) },
)
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.HouseholdStateTest"`
Expected: PASS (3 tests).

- [ ] **Step 6: Commit**

```bash
git add apps/android/app/src/main/kotlin/habitiq/app/agent apps/android/app/src/test/kotlin/habitiq/app/agent
git commit -m "feat(agent): plan model and household state"
```

---

### Task 2: Amount parsing

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/agent/AmountParser.kt`
- Test: `app/src/test/kotlin/habitiq/app/agent/AmountParserTest.kt`

**Interfaces:**
- Produces: `fun normalizeUtterance(raw: String): String`, `data class AmountMatch(val paise: Long, val range: IntRange)`, `fun findAmounts(text: String): List<AmountMatch>` (expects normalised text; results ordered by position)

- [ ] **Step 1: Write the failing test**

```kotlin
package habitiq.app.agent

import org.junit.Assert.assertEquals
import org.junit.Test

class AmountParserTest {
    private fun paise(raw: String) = findAmounts(normalizeUtterance(raw)).map { it.paise }

    @Test fun `normalises punctuation currency and apostrophes`() {
        assertEquals("bought milk for ₹60", normalizeUtterance("Bought milk for₹60!"))
        assertEquals("whats due today", normalizeUtterance("What's due today?"))
        assertEquals("rs 560 paid", normalizeUtterance("Rs.560 paid."))
        assertEquals("1,200 and 5.6k", normalizeUtterance("1,200, and 5.6k"))
    }

    @Test fun `digit amounts in every common form`() {
        assertEquals(listOf(56_000L), paise("₹560"))
        assertEquals(listOf(56_000L), paise("560"))
        assertEquals(listOf(56_000L), paise("560 rs"))
        assertEquals(listOf(56_000L), paise("rs. 560"))
        assertEquals(listOf(56_000L), paise("560 rupees"))
        assertEquals(listOf(120_000L), paise("1,200"))
        assertEquals(listOf(560_000L), paise("5.6k"))
        assertEquals(listOf(12_000_000L), paise("1.2 lakh"))
        assertEquals(listOf(4_550L), paise("45.50"))
    }

    @Test fun `number words`() {
        assertEquals(listOf(50_000L), paise("five hundred"))
        assertEquals(listOf(25_000L), paise("two hundred and fifty rupees"))
        assertEquals(listOf(300_000L), paise("three thousand"))
        assertEquals(listOf(6_000L), paise("sixty"))
        assertEquals(emptyList<Long>(), paise("one"))
    }

    @Test fun `non-money numbers are skipped`() {
        assertEquals(emptyList<Long>(), paise("2 bhk"))
        assertEquals(emptyList<Long>(), paise("on 4 oct"))
        assertEquals(emptyList<Long>(), paise("oct 4"))
        assertEquals(emptyList<Long>(), paise("3 days"))
        assertEquals(listOf(1_000_000L), paise("2 bhk under 10k"))
    }

    @Test fun `ranges point at the amount text`() {
        val text = normalizeUtterance("bought milk 60")
        val match = findAmounts(text).single()
        assertEquals("60", text.substring(match.range).trim())
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.AmountParserTest"`
Expected: FAIL, `Unresolved reference: findAmounts`.

- [ ] **Step 3: Write `AmountParser.kt`**

```kotlin
package habitiq.app.agent

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Lowercases and strips punctuation while keeping ₹, decimal points and thousands commas that sit
 * between digits. Apostrophes are dropped so "what's" becomes "whats".
 */
fun normalizeUtterance(raw: String): String = raw.lowercase()
    .replace("₹", " ₹")
    .replace("'", "").replace("’", "")
    .replace(Regex("""(?<!\d)\.|\.(?!\d)"""), " ")
    .replace(Regex("""(?<!\d),|,(?!\d)"""), " ")
    .replace(Regex("""[^a-z0-9₹., ]"""), " ")
    .replace(Regex("""\s+"""), " ")
    .trim()

data class AmountMatch(val paise: Long, val range: IntRange)

private val NUMERIC = Regex("""(?:₹\s*|\b(?:rs|inr)\s*)?(\d[\d,]*(?:\.\d+)?)(?:\s*(k|lakhs?|lacs?|rupees|rs|bucks)\b)?""")

private val MONTHS = setOf(
    "jan", "january", "feb", "february", "mar", "march", "apr", "april", "may", "jun", "june", "jul", "july",
    "aug", "august", "sep", "sept", "september", "oct", "october", "nov", "november", "dec", "december",
)

/** Words after a number that mean it isn't money. */
private val NON_MONEY_UNITS = MONTHS + setOf(
    "am", "pm", "st", "nd", "rd", "th", "day", "days", "week", "weeks", "month", "months", "year", "years",
    "people", "persons", "members", "times", "hrs", "hours", "mins", "minutes", "bhk", "sharing", "beds", "bed",
)

private val UNITS = mapOf(
    "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5, "six" to 6, "seven" to 7, "eight" to 8,
    "nine" to 9, "ten" to 10, "eleven" to 11, "twelve" to 12, "thirteen" to 13, "fourteen" to 14,
    "fifteen" to 15, "sixteen" to 16, "seventeen" to 17, "eighteen" to 18, "nineteen" to 19,
    "twenty" to 20, "thirty" to 30, "forty" to 40, "fifty" to 50, "sixty" to 60, "seventy" to 70,
    "eighty" to 80, "ninety" to 90,
)
private val SCALES = setOf("hundred", "thousand", "lakh", "lakhs")
private val CURRENCY_WORDS = setOf("rupees", "rs", "bucks")

private fun multiplier(suffix: String?): BigDecimal = when {
    suffix == null -> BigDecimal.ONE
    suffix == "k" -> BigDecimal(1_000)
    suffix.startsWith("lakh") || suffix.startsWith("lac") -> BigDecimal(100_000)
    else -> BigDecimal.ONE
}

private fun toPaise(value: BigDecimal): Long = value.movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong()

private fun wordAfter(text: String, end: Int): String? =
    Regex("""[a-z]+""").find(text, end + 1)?.takeIf { text.substring(end + 1, it.range.first).isBlank() }?.value

private fun wordBefore(text: String, start: Int): String? =
    Regex("""[a-z]+""").findAll(text.substring(0, start)).lastOrNull()
        ?.takeIf { text.substring(it.range.last + 1, start).isBlank() }?.value

/** Every money amount in [text] (already passed through [normalizeUtterance]), in reading order. */
fun findAmounts(text: String): List<AmountMatch> {
    val found = mutableListOf<AmountMatch>()
    for (m in NUMERIC.findAll(text)) {
        val digits = m.groupValues[1].replace(",", "")
        val suffix = m.groupValues[2].ifEmpty { null }
        if (suffix == null && (wordAfter(text, m.range.last) in NON_MONEY_UNITS || wordBefore(text, m.range.first) in MONTHS)) continue
        val value = digits.toBigDecimalOrNull() ?: continue
        found += AmountMatch(toPaise(value * multiplier(suffix)), m.range)
    }
    found += wordAmounts(text)
    return found.sortedBy { it.range.first }
}

private fun wordAmounts(text: String): List<AmountMatch> {
    val tokens = Regex("""[a-z]+""").findAll(text).toList()
    val out = mutableListOf<AmountMatch>()
    var i = 0
    while (i < tokens.size) {
        if (tokens[i].value !in UNITS && tokens[i].value !in SCALES) { i++; continue }
        var total = 0L
        var current = 0L
        var j = i
        var last = tokens[i]
        while (j < tokens.size) {
            val w = tokens[j].value
            val adjacent = j == i || text.substring(tokens[j - 1].range.last + 1, tokens[j].range.first).isBlank()
            if (!adjacent) break
            when {
                w in UNITS -> current += UNITS.getValue(w)
                w == "hundred" -> current = (if (current == 0L) 1 else current) * 100
                w == "thousand" -> { total += (if (current == 0L) 1 else current) * 1_000; current = 0 }
                w == "lakh" || w == "lakhs" -> { total += (if (current == 0L) 1 else current) * 100_000; current = 0 }
                w == "and" && j + 1 < tokens.size && tokens[j + 1].value in UNITS -> Unit
                else -> break
            }
            last = tokens[j]
            j++
        }
        var end = last.range.last
        if (j < tokens.size && tokens[j].value in CURRENCY_WORDS &&
            text.substring(last.range.last + 1, tokens[j].range.first).isBlank()) end = tokens[j].range.last
        val value = total + current
        if (value >= 10) out += AmountMatch(value * 100, tokens[i].range.first..end)
        i = j.coerceAtLeast(i + 1)
    }
    return out
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.AmountParserTest"`
Expected: PASS (5 tests). If `normalises punctuation…` fails on `"bought milk for ₹60"`, check that `" ₹"` insertion happens before the whitespace collapse.

- [ ] **Step 5: Commit**

```bash
git add apps/android/app/src/main/kotlin/habitiq/app/agent/AmountParser.kt apps/android/app/src/test/kotlin/habitiq/app/agent/AmountParserTest.kt
git commit -m "feat(agent): amount parsing for digits, k/lakh and number words"
```

---

### Task 3: Date phrases and member name matching

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/agent/DatePhrases.kt`
- Create: `app/src/main/kotlin/habitiq/app/agent/NameMatcher.kt`
- Test: `app/src/test/kotlin/habitiq/app/agent/DatePhrasesTest.kt`, `app/src/test/kotlin/habitiq/app/agent/NameMatcherTest.kt`

**Interfaces:**
- Consumes: `AgentMember` (Task 1)
- Produces: `fun parseDateRange(text: String, today: LocalDate): ClosedRange<LocalDate>?`; `fun editDistance(a: String, b: String): Int`; `fun matchMembers(word: String, members: List<AgentMember>, fuzzy: Boolean): List<AgentMember>`

- [ ] **Step 1: Write the failing tests**

`DatePhrasesTest.kt`:

```kotlin
package habitiq.app.agent

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DatePhrasesTest {
    private val saturday = LocalDate.of(2026, 10, 3)
    private val wednesday = LocalDate.of(2026, 10, 7)
    private fun d(day: Int) = LocalDate.of(2026, 10, day)

    @Test fun `single days`() {
        assertEquals(d(3)..d(3), parseDateRange("my tasks today", saturday))
        assertEquals(d(4)..d(4), parseDateRange("my tasks tomorrow", saturday))
        assertEquals(d(5)..d(5), parseDateRange("day after tomorrow", saturday))
    }

    @Test fun `weeks and weekends`() {
        assertEquals(d(3)..d(4), parseDateRange("this week", saturday))
        assertEquals(d(5)..d(11), parseDateRange("next week", saturday))
        assertEquals(d(3)..d(4), parseDateRange("this weekend", saturday))
        assertEquals(d(10)..d(11), parseDateRange("weekend", wednesday))
        assertEquals(d(7)..d(11), parseDateRange("this week", wednesday))
    }

    @Test fun `no phrase gives null`() {
        assertNull(parseDateRange("my tasks", saturday))
        assertNull(parseDateRange("todays", saturday))
    }
}
```

`NameMatcherTest.kt`:

```kotlin
package habitiq.app.agent

import org.junit.Assert.assertEquals
import org.junit.Test

class NameMatcherTest {
    private val ravi = AgentMember("u2", "Ravi", "Ravi Kumar")
    private val priya = AgentMember("u3", "Priya", "Priya")
    private val dave = AgentMember("u5", "Dave", "Dave")
    private val sai = AgentMember("u1", "Sai", "Sai")
    private val all = listOf(sai, ravi, priya, dave)

    @Test fun `edit distance`() {
        assertEquals(0, editDistance("ravi", "ravi"))
        assertEquals(1, editDistance("priya", "priyaa"))
        assertEquals(1, editDistance("dave", "gave"))
        assertEquals(3, editDistance("abc", ""))
    }

    @Test fun `exact match ignores case`() {
        assertEquals(listOf(ravi), matchMembers("RAVI", all, fuzzy = true))
    }

    @Test fun `fuzzy match only for names of four or more letters`() {
        assertEquals(listOf(priya), matchMembers("priyaa", all, fuzzy = true))
        assertEquals(emptyList<AgentMember>(), matchMembers("sia", all, fuzzy = true))
    }

    @Test fun `vocabulary words only match exactly`() {
        assertEquals(emptyList<AgentMember>(), matchMembers("gave", all, fuzzy = false))
        assertEquals(listOf(dave), matchMembers("gave", all, fuzzy = true))
    }

    @Test fun `two members with the same first name both come back`() {
        val raviT = AgentMember("u6", "Ravi", "Ravi Teja")
        assertEquals(listOf(ravi, raviT), matchMembers("ravi", all + raviT, fuzzy = true))
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.DatePhrasesTest" --tests "habitiq.app.agent.NameMatcherTest"`
Expected: FAIL, unresolved `parseDateRange`, `matchMembers`, `editDistance`.

- [ ] **Step 3: Write `DatePhrases.kt`**

```kotlin
package habitiq.app.agent

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** The date range a phrase in normalised [text] refers to, or null when it names none. M2 adds weekdays and "d MMM". */
fun parseDateRange(text: String, today: LocalDate): ClosedRange<LocalDate>? {
    fun has(pattern: String) = Regex("""\b(?:$pattern)\b""").containsMatchIn(text)
    val sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    return when {
        has("day after tomorrow") -> today.plusDays(2).let { it..it }
        has("tomorrow") -> today.plusDays(1).let { it..it }
        has("today|tonight") -> today..today
        has("next week") -> today.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).let { it..it.plusDays(6) }
        has("this weekend|weekend") -> {
            val saturday = if (today.dayOfWeek == DayOfWeek.SUNDAY) today else today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
            saturday..saturday.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        }
        has("this week") -> today..sunday
        else -> null
    }
}
```

- [ ] **Step 4: Write `NameMatcher.kt`**

```kotlin
package habitiq.app.agent

fun editDistance(a: String, b: String): Int {
    var prev = IntArray(b.length + 1) { it }
    for (i in 1..a.length) {
        val cur = IntArray(b.length + 1)
        cur[0] = i
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            cur[j] = minOf(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + cost)
        }
        prev = cur
    }
    return prev[b.length]
}

/**
 * Members whose first name is [word]. Exact matches win. With [fuzzy], names of 4+ letters also
 * match at edit distance 1. Callers pass fuzzy = false for the parser's own vocabulary, so "gave"
 * never turns into Dave.
 */
fun matchMembers(word: String, members: List<AgentMember>, fuzzy: Boolean): List<AgentMember> {
    val w = word.lowercase()
    val exact = members.filter { it.firstName.lowercase() == w }
    if (exact.isNotEmpty() || !fuzzy || w.length < 4) return exact
    return members.filter { m -> m.firstName.lowercase().let { it.length >= 4 && editDistance(it, w) <= 1 } }
}
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.DatePhrasesTest" --tests "habitiq.app.agent.NameMatcherTest"`
Expected: PASS (8 tests).

- [ ] **Step 6: Commit**

```bash
git add apps/android/app/src/main/kotlin/habitiq/app/agent apps/android/app/src/test/kotlin/habitiq/app/agent
git commit -m "feat(agent): date phrases and member name matching"
```

---

### Task 4: LocalIntentParser — questions and flat search

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/agent/LocalIntentParser.kt`
- Test: `app/src/test/kotlin/habitiq/app/agent/LocalIntentParserQuestionsTest.kt`
- Create: `app/src/test/kotlin/habitiq/app/agent/AgentFixtures.kt` (shared test fixtures)

**Interfaces:**
- Consumes: `normalizeUtterance`, `findAmounts`, `AmountMatch` (Task 2); `parseDateRange` (Task 3); `HouseholdState`, `AgentPlan`, `AgentQuery`, `ParseResult` (Task 1)
- Produces: `object LocalIntentParser { fun parse(raw: String, state: HouseholdState, today: LocalDate): ParseResult }`; test fixtures `Fixtures.state`, `Fixtures.today`

- [ ] **Step 1: Write the shared fixtures**

```kotlin
package habitiq.app.agent

import java.time.LocalDate

object Fixtures {
    val today: LocalDate = LocalDate.of(2026, 10, 3) // Saturday
    val sai = AgentMember("u1", "Sai", "Sai Jaswanth")
    val ravi = AgentMember("u2", "Ravi", "Ravi Kumar")
    val priya = AgentMember("u3", "Priya", "Priya")
    val arjun = AgentMember("u4", "Arjun", "Arjun")
    val state = HouseholdState(
        myUid = "u1", inFlat = true, isAdmin = true,
        members = listOf(sai, ravi, priya, arjun),
        tasks = emptyList(), netBalances = emptyMap(), bills = emptyList(), vacancies = emptyList(),
    )
    val allUids = listOf("u1", "u2", "u3", "u4")
}
```

- [ ] **Step 2: Write the failing test**

```kotlin
package habitiq.app.agent

import habitiq.app.agent.Fixtures.state
import habitiq.app.agent.Fixtures.today
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalIntentParserQuestionsTest {
    private fun answer(q: AgentQuery) = ParseResult.Confident(AgentPlan.Answer(q))
    private fun parse(text: String) = LocalIntentParser.parse(text, state, today)

    @Test fun `balance questions`() {
        listOf("What do I owe?", "how much do i owe", "my balance", "Do I owe anyone", "am I owed anything", "kitna dena hai")
            .forEach { assertEquals(it, answer(AgentQuery.MyBalance), parse(it)) }
    }

    @Test fun `who owes questions`() {
        listOf("who owes money", "Who hasn't paid?", "who has not paid yet", "who all owe", "who owes me")
            .forEach { assertEquals(it, answer(AgentQuery.WhoOwes), parse(it)) }
    }

    @Test fun `due today questions`() {
        listOf("What's due today?", "what is due", "today's tasks", "what do i have today", "anything due today")
            .forEach { assertEquals(it, answer(AgentQuery.DueToday), parse(it)) }
    }

    @Test fun `my duties with a range`() {
        assertEquals(answer(AgentQuery.MyDuties(today, today.plusDays(1))), parse("my tasks this week"))
        assertEquals(answer(AgentQuery.MyDuties(today.plusDays(1), today.plusDays(1))), parse("My chores tomorrow"))
        assertEquals(answer(AgentQuery.MyDuties(today, today.plusDays(6))), parse("what are my duties"))
        // "whose turn" is not "my": it must not be read as my duties.
        assertEquals(ParseResult.NeedsModel, parse("whose turn is it next week"))
    }

    @Test fun `bills questions`() {
        listOf("any bills pending?", "show bills", "which bills are due", "unpaid bills")
            .forEach { assertEquals(it, answer(AgentQuery.Bills), parse(it)) }
    }

    @Test fun `flat search`() {
        assertEquals(answer(AgentQuery.FindFlats("gachibowli", null, 10_000, null)), parse("Flats in Gachibowli under 10k"))
        assertEquals(answer(AgentQuery.FindFlats("madhapur", null, null, "female")), parse("rooms near Madhapur for girls"))
        assertEquals(answer(AgentQuery.FindFlats("kondapur", null, 8_000, "male")), parse("any 2 bhk flat in kondapur below 8000 for boys"))
        assertEquals(answer(AgentQuery.FindFlats(null, null, 12_000, null)), parse("find rooms under 12k"))
    }

    @Test fun `not understood`() {
        listOf("hello", "remind me to call mom", "", "   ", "flats")
            .forEach { assertEquals(it, ParseResult.NeedsModel, parse(it)) }
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.LocalIntentParserQuestionsTest"`
Expected: FAIL, `Unresolved reference: LocalIntentParser`.

- [ ] **Step 4: Write `LocalIntentParser.kt` (questions and flats; Task 5 adds actions)**

```kotlin
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
```

`QUESTION_START` is used in Task 5. Leaving it unused here produces a warning only.

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.LocalIntentParserQuestionsTest"`
Expected: PASS (7 tests). If `"who owes me"` fails, check that `WHO_OWES` is matched before `MY_BALANCE`.

- [ ] **Step 6: Commit**

```bash
git add apps/android/app/src/main/kotlin/habitiq/app/agent/LocalIntentParser.kt apps/android/app/src/test/kotlin/habitiq/app/agent
git commit -m "feat(agent): local parser for balance, task, bill and flat-search questions"
```

---

### Task 5: LocalIntentParser — expenses and settlements

**Files:**
- Modify: `app/src/main/kotlin/habitiq/app/agent/LocalIntentParser.kt`
- Test: `app/src/test/kotlin/habitiq/app/agent/LocalIntentParserActionsTest.kt`

**Interfaces:**
- Consumes: `matchMembers` (Task 3); everything from Task 4
- Produces: the same `LocalIntentParser.parse` signature, now also returning `AgentPlan.Actions` with `AgentStep.AddExpense` / `AgentStep.Settle`, `AgentPlan.Clarify` for ambiguous names, and `AgentPlan.Unsupported` when not in a flat

- [ ] **Step 1: Write the failing test**

```kotlin
package habitiq.app.agent

import habitiq.app.agent.Fixtures.allUids
import habitiq.app.agent.Fixtures.state
import habitiq.app.agent.Fixtures.today
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalIntentParserActionsTest {
    private fun parse(text: String, s: HouseholdState = state) = LocalIntentParser.parse(text, s, today)

    private fun step(text: String): AgentStep {
        val r = parse(text)
        assertTrue("$text -> $r", r is ParseResult.Confident && r.plan is AgentPlan.Actions)
        return ((r as ParseResult.Confident).plan as AgentPlan.Actions).steps.single()
    }

    private fun expense(title: String, paise: Long, category: String, payer: String = "u1", split: List<String> = allUids) =
        AgentStep.AddExpense(title, paise, category, payer, split)

    @Test fun `expenses`() {
        assertEquals(expense("Groceries", 56_000, "lifestyle"), step("Bought groceries for ₹560"))
        assertEquals(expense("Kirana", 45_000, "lifestyle"), step("kirana 450"))
        assertEquals(expense("Swiggy", 120_000, "lifestyle"), step("spent 1.2k on swiggy"))
        assertEquals(expense("Electricity bill", 145_000, "bills"), step("paid electricity bill 1,450"))
        assertEquals(expense("Rent", 800_000, "bills"), step("rent 8000"))
        assertEquals(expense("Milk", 6_000, "lifestyle", payer = "u2"), step("Ravi bought milk 60"))
        assertEquals(expense("Vegetables", 30_000, "lifestyle", split = listOf("u1", "u3")), step("bought vegetables 300 split with priya"))
        assertEquals(expense("Gas cylinder", 110_000, "bills", split = listOf("u1", "u2")), step("gas cylinder 1100 between me and ravi"))
        assertEquals(expense("Detergent", 25_000, "chores"), step("bought detergent for two hundred and fifty rupees"))
        assertEquals(expense("Dinner", 64_000, "lifestyle", payer = "u2"), step("Ravi paid 640 for dinner"))
    }

    @Test fun `lazy spend with no subject still records`() {
        assertEquals(expense("Expense", 50_000, "other"), step("I just spent 500"))
        assertEquals(expense("Expense", 50_000, "other"), step("spent 500"))
        assertEquals(expense("Expense", 20_000, "other"), step("aaj 200 kharcha hua"))
    }

    @Test fun `settlements`() {
        assertEquals(AgentStep.Settle("u1", "u2", 20_000), step("paid ravi 200"))
        assertEquals(AgentStep.Settle("u1", "u3", 15_000), step("Gave Priya ₹150"))
        assertEquals(AgentStep.Settle("u4", "u1", 30_000), step("Arjun paid me 300"))
        assertEquals(AgentStep.Settle("u2", "u1", 50_000), step("got 500 from ravi"))
        assertEquals(AgentStep.Settle("u1", "u3", 25_000), step("sent 250 to priyaa"))
        assertEquals(AgentStep.Settle("u1", "u2", 100_000), step("I paid Ravi back 1k"))
    }

    @Test fun `ambiguous names ask instead of guessing`() {
        val raviT = AgentMember("u5", "Ravi", "Ravi Teja")
        val r = parse("paid ravi 200", state.copy(members = state.members + raviT))
        val clarify = (r as ParseResult.Confident).plan as AgentPlan.Clarify
        assertEquals("Which Ravi?", clarify.question)
        assertEquals(listOf("Ravi Kumar", "Ravi Teja"), clarify.options.map { it.label })
        val chosen = (clarify.options[1].plan as AgentPlan.Actions).steps.single()
        assertEquals(AgentStep.Settle("u1", "u5", 20_000), chosen)
    }

    @Test fun `questions never become actions`() {
        listOf("what did i spend on groceries 500?", "how much is rent 8000", "did ravi pay 200", "show expenses over 500")
            .forEach { assertEquals(it, ParseResult.NeedsModel, parse(it)) }
    }

    @Test fun `incomplete or unclear money commands need the model`() {
        listOf(
            "paid 300",                           // paid what, or whom?
            "bought groceries 200 and milk 50",   // two amounts
            "bought milk 0.5",                    // under ₹1
            "rent 200000",                        // over ₹1,00,000
            "paid 2 days ago 300",                // no subject
            "bought milk 60 split with xyzzy",    // unknown person in split
        ).forEach { assertEquals(it, ParseResult.NeedsModel, parse(it)) }
    }

    @Test fun `not in a flat`() {
        val r = parse("bought milk 60", HouseholdState.Empty.copy(myUid = "u1"))
        assertEquals(ParseResult.Confident(AgentPlan.Unsupported("Join or create a flat first to track money.")), r)
    }

    @Test fun `summaries read plainly`() {
        val r = parse("Bought groceries for ₹560") as ParseResult.Confident
        assertEquals("Add ₹560 for Groceries, split 4 ways", (r.plan as AgentPlan.Actions).summary)
        val s = parse("paid ravi 200") as ParseResult.Confident
        assertEquals("Record ₹200 you paid Ravi", (s.plan as AgentPlan.Actions).summary)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.LocalIntentParserActionsTest"`
Expected: FAIL. Every action phrase returns `NeedsModel`.

- [ ] **Step 3: Replace `parse` and add the action rules in `LocalIntentParser.kt`**

Replace the `parse` function with:

```kotlin
    fun parse(raw: String, state: HouseholdState, today: LocalDate): ParseResult {
        val text = normalizeUtterance(raw)
        if (text.isBlank()) return ParseResult.NeedsModel
        val amounts = findAmounts(text)

        if (amounts.isEmpty()) question(text, today)?.let { return answer(it) }
        findFlats(text, amounts)?.let { return answer(it) }

        val tokens = Regex("""\S+""").findAll(text).toList()
        if (tokens.first().value in QUESTION_START || raw.trim().endsWith("?")) return ParseResult.NeedsModel
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
```

Then add these members inside `object LocalIntentParser`:

```kotlin
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
```

The `"split with ${split.size - 1} other"` summary is only used when splitting with a subset. For `split = [u1, u3]` it reads "split with 1 other". The summary test covers only the default case.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.LocalIntentParserActionsTest" --tests "habitiq.app.agent.LocalIntentParserQuestionsTest"`
Expected: PASS (all). Likely failures and their fixes:
- `"paid electricity bill 1,450"` becomes a settle: check that `c.others(j)` for `"electricity"` is empty (it's in `VOCAB`, so it's exact-only).
- `"Ravi paid 640 for dinner"` title is `"Ravi dinner"`: check that the member-name filter runs before `take(4)`.
- `"bought detergent for two hundred and fifty rupees"` keeps `"two"`: check that `isAmount` covers every token inside the word-amount range (the range runs from `"two"` to `"rupees"`).

- [ ] **Step 5: Commit**

```bash
git add apps/android/app/src/main/kotlin/habitiq/app/agent/LocalIntentParser.kt apps/android/app/src/test/kotlin/habitiq/app/agent/LocalIntentParserActionsTest.kt
git commit -m "feat(agent): parse expenses and settlements with clarify for ambiguous names"
```

---

### Task 5b: Content guard (acceptable use)

Oddroof is for running a shared home. The agent must not become a way to log illegal purchases, abuse a flatmate, or plant offensive text in shared records, because anything it records is visible to the whole flat. The guard runs on every request, voice or typed, **before** parsing. A blocked request gets one calm, non-judgemental line and never becomes a card. M1 makes no writes, but this gate is in place before M2 starts writing.

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/agent/ContentGuard.kt`
- Test: `app/src/test/kotlin/habitiq/app/agent/ContentGuardTest.kt`

**Interfaces:**
- Consumes: `normalizeUtterance` (Task 2)
- Produces: `object ContentGuard { const val REFUSAL: String; fun reasonToBlock(raw: String): String? }`. It returns `REFUSAL` when the request must be refused and null when it's fine. Task 8's `AgentViewModel.submitText` calls it before `LocalIntentParser.parse`.

- [ ] **Step 1: Write the failing test**

```kotlin
package habitiq.app.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContentGuardTest {
    @Test fun `illegal goods and services are refused`() {
        listOf("bought ganja 500", "Weed for 800 split with ravi", "paid 2000 for mdma", "spent 300 on charas",
            "bribe to watchman 500", "hawala 10000", "satta 1000", "bought a pistol 5000")
            .forEach { assertEquals(it, ContentGuard.REFUSAL, ContentGuard.reasonToBlock(it)) }
    }

    @Test fun `harassment and threats are refused`() {
        listOf("ravi is a useless idiot add 200", "I will kill priya", "beat him up 500")
            .forEach { assertEquals(it, ContentGuard.REFUSAL, ContentGuard.reasonToBlock(it)) }
    }

    @Test fun `everyday household words are not blocked`() {
        listOf("bought groceries 560", "paid ravi 200", "weeding the balcony plants 150", "coke and chips 120",
            "gas cylinder 1100", "pest control 900", "killed the cockroaches, bought spray 250", "what do i owe")
            .forEach { assertNull(it, ContentGuard.reasonToBlock(it)) }
    }
}
```

`"killed the cockroaches"` is allowed on purpose: threats are matched as phrases ("will kill", "beat him"), never as a bare verb.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.ContentGuardTest"`
Expected: FAIL, `Unresolved reference: ContentGuard`.

- [ ] **Step 3: Write `ContentGuard.kt`**

```kotlin
package habitiq.app.agent

/**
 * Acceptable-use gate for the agent. Anything the agent records lands in shared flat data, so it
 * refuses requests that log illegal goods or services, or that carry abuse or threats. Matching is
 * whole-word on normalised text, so "weeding" and "coke" stay fine.
 *
 * Keep the lists short and specific: a false refusal of a real chore costs more trust than it saves.
 * Hindi/Telugu abuse terms are maintained by Sai; add them to ABUSE_WORDS (lowercase, romanised).
 */
object ContentGuard {
    const val REFUSAL = "I can't help with that one. Oddroof is for shared home stuff like groceries, bills and chores."

    private val ILLEGAL_WORDS = setOf(
        "ganja", "weed", "charas", "hash", "cocaine", "mdma", "ecstasy", "lsd", "heroin", "meth", "drugs",
        "pistol", "revolver", "gun", "bullets", "ammo",
        "bribe", "hawala", "satta", "matka", "escort", "escorts",
    )
    private val ABUSE_WORDS = setOf(
        "idiot", "stupid", "moron", "bastard", "bitch", "slut", "whore",
    )
    private val THREAT_PHRASES = listOf(
        Regex("""\bwill (?:kill|hurt|beat|slap)\b"""),
        Regex("""\b(?:kill|hurt|beat|slap) (?:him|her|you|them)\b"""),
        Regex("""\bbeat (?:him|her|them) up\b"""),
    )

    fun reasonToBlock(raw: String): String? {
        val text = normalizeUtterance(raw)
        val words = text.split(' ').toSet()
        return when {
            words.any { it in ILLEGAL_WORDS } -> REFUSAL
            words.any { it in ABUSE_WORDS } -> REFUSAL
            THREAT_PHRASES.any { it.containsMatchIn(text) } -> REFUSAL
            else -> null
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.ContentGuardTest"`
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add apps/android/app/src/main/kotlin/habitiq/app/agent/ContentGuard.kt apps/android/app/src/test/kotlin/habitiq/app/agent/ContentGuardTest.kt
git commit -m "feat(agent): acceptable-use guard for illegal goods, abuse and threats"
```

---

### Task 6: QueryResolver and plan previews

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/agent/QueryResolver.kt`
- Test: `app/src/test/kotlin/habitiq/app/agent/QueryResolverTest.kt`

**Interfaces:**
- Consumes: `HouseholdState`, `AgentQuery`, `AgentAnswer`, `AnswerLine`, `AgentLink`, `AgentPlan.Actions`, `AgentStep` (Task 1); `habitiq.app.lib.formatInr(Double)`, `habitiq.app.lib.suggestSettlements(Map<String, Double>): List<SuggestedSettlement>`
- Produces: `object QueryResolver { fun resolve(query: AgentQuery, state: HouseholdState, today: LocalDate): AgentAnswer }`; `fun cardFor(plan: AgentPlan.Actions, state: HouseholdState): AgentCard`

- [ ] **Step 1: Write the failing test**

```kotlin
package habitiq.app.agent

import habitiq.app.agent.Fixtures.state
import habitiq.app.agent.Fixtures.today
import org.junit.Assert.assertEquals
import org.junit.Test

class QueryResolverTest {
    private val owing = state.copy(netBalances = mapOf("u1" to -350.0, "u2" to 500.0, "u3" to -150.0, "u4" to 0.0))
    private fun task(id: String, who: String, due: java.time.LocalDate?, status: String = "pending") =
        AgentTask(id, "Task $id", who, due, "weekly", status)

    @Test fun `my balance when I owe`() {
        val a = QueryResolver.resolve(AgentQuery.MyBalance, owing, today)
        assertEquals("You owe ₹350", a.headline)
        assertEquals(listOf(AnswerLine("Pay Ravi", "₹350")), a.lines)
        assertEquals(AgentLink.BALANCES, a.link)
    }

    @Test fun `my balance when owed and when settled`() {
        assertEquals("You're owed ₹500", QueryResolver.resolve(AgentQuery.MyBalance, owing.copy(myUid = "u2"), today).headline)
        assertEquals("You're all settled", QueryResolver.resolve(AgentQuery.MyBalance, owing.copy(myUid = "u4"), today).headline)
    }

    @Test fun `who owes lists debtors largest first`() {
        val a = QueryResolver.resolve(AgentQuery.WhoOwes, owing, today)
        assertEquals("2 people owe money", a.headline)
        assertEquals(listOf(AnswerLine("You", "₹350"), AnswerLine("Priya", "₹150")), a.lines)
        assertEquals("Nobody owes anything right now", QueryResolver.resolve(AgentQuery.WhoOwes, state, today).headline)
    }

    @Test fun `due today shows my open and overdue tasks`() {
        val s = state.copy(tasks = listOf(
            task("1", "u1", today), task("2", "u1", today.minusDays(1)), task("3", "u2", today),
            task("4", "u1", today, status = "completed"), task("5", "u1", today.plusDays(1)),
        ))
        val a = QueryResolver.resolve(AgentQuery.DueToday, s, today)
        assertEquals("2 things for you today", a.headline)
        assertEquals(listOf(AnswerLine("Task 1", "Due today"), AnswerLine("Task 2", "Overdue")), a.lines)
        assertEquals(AgentLink.TASKS, a.link)
        assertEquals("Nothing due today", QueryResolver.resolve(AgentQuery.DueToday, state, today).headline)
    }

    @Test fun `my duties in a range`() {
        val s = state.copy(tasks = listOf(task("1", "u1", today.plusDays(1)), task("2", "u1", today.plusDays(9)), task("3", "u2", today.plusDays(1))))
        val a = QueryResolver.resolve(AgentQuery.MyDuties(today, today.plusDays(6)), s, today)
        assertEquals("1 task coming up", a.headline)
        assertEquals(listOf(AnswerLine("Task 1", "Sun, 4 Oct")), a.lines)
    }

    @Test fun `bills shows unpaid bills this month`() {
        val s = state.copy(bills = listOf(
            AgentBill("Wifi", 99_900, today.plusDays(2), "u1", "pending"),
            AgentBill("Rent", null, today.plusDays(5), "u2", "split_generated"),
            AgentBill("Gas", 110_000, today, "u1", "paid"),
            AgentBill("Water", 30_000, today, "u1", "skipped"),
        ))
        val a = QueryResolver.resolve(AgentQuery.Bills, s, today)
        assertEquals("2 bills still open", a.headline)
        assertEquals(listOf(AnswerLine("Wifi", "₹999 · Mon, 5 Oct"), AnswerLine("Rent", "Amount not set · Thu, 8 Oct")), a.lines)
        assertEquals(AgentLink.BILLS, a.link)
    }

    @Test fun `find flats filters and sorts by rent`() {
        val s = state.copy(vacancies = listOf(
            AgentVacancy("a", "Green Nest", "Gachibowli", "Hyderabad", 950_000, "female"),
            AgentVacancy("b", "Blue Door", "Gachibowli", "Hyderabad", 800_000, "any"),
            AgentVacancy("c", "Pricey", "Gachibowli", "Hyderabad", 1_500_000, "any"),
            AgentVacancy("d", "Elsewhere", "Kondapur", "Hyderabad", 700_000, "any"),
            AgentVacancy("e", "Boys only", "Gachibowli", "Hyderabad", 700_000, "male"),
        ))
        val a = QueryResolver.resolve(AgentQuery.FindFlats("gachibowli", null, 10_000, "female"), s, today)
        assertEquals("2 places match", a.headline)
        assertEquals(listOf(AnswerLine("Blue Door", "Gachibowli · ₹8,000/head"), AnswerLine("Green Nest", "Gachibowli · ₹9,500/head")), a.lines)
        assertEquals(AgentLink.DISCOVER, a.link)
        assertEquals("No places match yet", QueryResolver.resolve(AgentQuery.FindFlats("ameerpet", null, null, null), s, today).headline)
    }

    @Test fun `answers without a flat`() {
        val none = HouseholdState.Empty.copy(myUid = "u1")
        listOf(AgentQuery.MyBalance, AgentQuery.WhoOwes, AgentQuery.DueToday, AgentQuery.Bills).forEach {
            val a = QueryResolver.resolve(it, none, today)
            assertEquals("Join a flat to see this", a.headline)
            assertEquals(null, a.link)
        }
    }

    @Test fun `builds expense and payment cards`() {
        val exp = AgentPlan.Actions(listOf(AgentStep.AddExpense("Groceries", 56_000, "lifestyle", "u1", Fixtures.allUids)), "x")
        assertEquals(
            AgentCard.Expense("Groceries", 56_000, "lifestyle", "You", listOf("You", "Ravi", "Priya", "Arjun"), everyone = true),
            cardFor(exp, state),
        )
        val part = AgentPlan.Actions(listOf(AgentStep.AddExpense("Milk", 6_000, "lifestyle", "u2", listOf("u1", "u2"))), "x")
        val partCard = cardFor(part, state) as AgentCard.Expense
        assertEquals("Ravi", partCard.paidBy)
        assertEquals(listOf("You", "Ravi"), partCard.splitNames)
        assertEquals(false, partCard.everyone)
        val settle = AgentPlan.Actions(listOf(AgentStep.Settle("u1", "u2", 20_000)), "x")
        assertEquals(AgentCard.Payment("You", "Ravi", 20_000), cardFor(settle, state))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.QueryResolverTest"`
Expected: FAIL, `Unresolved reference: QueryResolver`.

- [ ] **Step 3: Write `QueryResolver.kt`**

```kotlin
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
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.QueryResolverTest"`
Expected: PASS (9 tests). If `my balance when I owe` gets an extra line, check `suggestSettlements` in `lib/SettlementUtils.kt:73`. Ravi is the only creditor, so exactly one line must come back.

- [ ] **Step 5: Commit**

```bash
git add apps/android/app/src/main/kotlin/habitiq/app/agent/QueryResolver.kt apps/android/app/src/test/kotlin/habitiq/app/agent/QueryResolverTest.kt
git commit -m "feat(agent): answer balance, task, bill and flat questions from live data"
```

---

### Task 7: Voice input

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/agent/VoiceInput.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Test: `app/src/test/kotlin/habitiq/app/agent/VoiceInputTest.kt`

**Interfaces:**
- Produces: `sealed interface SpeechEvent { Partial(text: String); Final(text: String); Level(level: Float); Error(kind: SpeechErrorKind) }`; `enum class SpeechErrorKind { NoMatch, NoPermission, Network, Busy, Unavailable, Other }`; `interface SpeechSource { val events: Flow<SpeechEvent>; fun start(); fun stop(); fun release() }`; `fun speechErrorKind(code: Int): SpeechErrorKind`; `fun rmsToLevel(rmsDb: Float): Float`; `class AndroidVoiceInput(context: Context) : SpeechSource`

- [ ] **Step 1: Write the failing test**

```kotlin
package habitiq.app.agent

import android.speech.SpeechRecognizer
import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceInputTest {
    @Test fun `recogniser error codes map to plain kinds`() {
        assertEquals(SpeechErrorKind.NoMatch, speechErrorKind(SpeechRecognizer.ERROR_NO_MATCH))
        assertEquals(SpeechErrorKind.NoMatch, speechErrorKind(SpeechRecognizer.ERROR_SPEECH_TIMEOUT))
        assertEquals(SpeechErrorKind.NoPermission, speechErrorKind(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS))
        assertEquals(SpeechErrorKind.Network, speechErrorKind(SpeechRecognizer.ERROR_NETWORK))
        assertEquals(SpeechErrorKind.Network, speechErrorKind(SpeechRecognizer.ERROR_NETWORK_TIMEOUT))
        assertEquals(SpeechErrorKind.Network, speechErrorKind(SpeechRecognizer.ERROR_SERVER))
        assertEquals(SpeechErrorKind.Busy, speechErrorKind(SpeechRecognizer.ERROR_RECOGNIZER_BUSY))
        assertEquals(SpeechErrorKind.Other, speechErrorKind(-42))
    }

    @Test fun `mic loudness maps to 0 to 1`() {
        assertEquals(0f, rmsToLevel(-2f), 0.001f)
        assertEquals(0f, rmsToLevel(-10f), 0.001f)
        assertEquals(0.5f, rmsToLevel(4f), 0.001f)
        assertEquals(1f, rmsToLevel(10f), 0.001f)
        assertEquals(1f, rmsToLevel(20f), 0.001f)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.VoiceInputTest"`
Expected: FAIL, `Unresolved reference: speechErrorKind`.

- [ ] **Step 3: Write `VoiceInput.kt`**

```kotlin
package habitiq.app.agent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface SpeechEvent {
    data class Partial(val text: String) : SpeechEvent
    data class Final(val text: String) : SpeechEvent
    /** Mic loudness 0..1, for the listening animation. */
    data class Level(val level: Float) : SpeechEvent
    data class Error(val kind: SpeechErrorKind) : SpeechEvent
}

/** SpeechRecognizer reports roughly -2..10 dB; map that to 0..1. */
fun rmsToLevel(rmsDb: Float): Float = ((rmsDb + 2f) / 12f).coerceIn(0f, 1f)

enum class SpeechErrorKind { NoMatch, NoPermission, Network, Busy, Unavailable, Other }

/** Speech-to-text behind an interface so the view model can be tested with a fake. */
interface SpeechSource {
    val events: Flow<SpeechEvent>
    fun start()
    fun stop()
    fun release()
}

fun speechErrorKind(code: Int): SpeechErrorKind = when (code) {
    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechErrorKind.NoMatch
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SpeechErrorKind.NoPermission
    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT, SpeechRecognizer.ERROR_SERVER -> SpeechErrorKind.Network
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> SpeechErrorKind.Busy
    else -> SpeechErrorKind.Other
}

/**
 * Android's recogniser in en-IN, on-device where the phone supports it. Audio stays with the
 * system recogniser; Oddroof only ever sees the text. Call [start] and [stop] on the main thread.
 */
class AndroidVoiceInput(private val context: Context) : SpeechSource {
    private val _events = MutableSharedFlow<SpeechEvent>(extraBufferCapacity = 64)
    override val events: Flow<SpeechEvent> = _events.asSharedFlow()
    private var recognizer: SpeechRecognizer? = null

    private val listener = object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            _events.tryEmit(if (text.isNullOrBlank()) SpeechEvent.Error(SpeechErrorKind.NoMatch) else SpeechEvent.Final(text))
        }
        override fun onPartialResults(partialResults: Bundle?) {
            partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                ?.takeIf { it.isNotBlank() }?.let { _events.tryEmit(SpeechEvent.Partial(it)) }
        }
        override fun onError(error: Int) { _events.tryEmit(SpeechEvent.Error(speechErrorKind(error))) }
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) { _events.tryEmit(SpeechEvent.Level(rmsToLevel(rmsdB))) }
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    override fun start() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _events.tryEmit(SpeechEvent.Error(SpeechErrorKind.Unavailable))
            return
        }
        val r = recognizer ?: SpeechRecognizer.createSpeechRecognizer(context).also {
            it.setRecognitionListener(listener)
            recognizer = it
        }
        r.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        })
    }

    override fun stop() { recognizer?.stopListening() }

    override fun release() {
        recognizer?.destroy()
        recognizer = null
    }
}
```

- [ ] **Step 4: Add the permission and recogniser query to `AndroidManifest.xml`**

Add after the existing `USE_BIOMETRIC` permission line:

```xml
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <!-- Android 11+ package visibility: lets SpeechRecognizer find the system recognition service. -->
    <queries>
        <intent>
            <action android:name="android.speech.RecognitionService" />
        </intent>
    </queries>
```

If the manifest already has a `<queries>` block, add the `<intent>` inside it instead of adding a second block.

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.VoiceInputTest"`
Expected: PASS (2 tests).

- [ ] **Step 6: Commit**

```bash
git add apps/android/app/src/main/kotlin/habitiq/app/agent/VoiceInput.kt apps/android/app/src/main/AndroidManifest.xml apps/android/app/src/test/kotlin/habitiq/app/agent/VoiceInputTest.kt
git commit -m "feat(agent): en-IN speech input behind a SpeechSource interface"
```

---

### Task 8: AgentViewModel state machine

**Files:**
- Modify: `gradle/libs.versions.toml`, `app/build.gradle.kts`
- Create: `app/src/main/kotlin/habitiq/app/agent/AgentViewModel.kt`
- Test: `app/src/test/kotlin/habitiq/app/agent/AgentViewModelTest.kt`

**Interfaces:**
- Consumes: `ContentGuard.reasonToBlock`, `ContentGuard.REFUSAL` (Task 5b); `SpeechSource`, `SpeechEvent`, `SpeechErrorKind` (Task 7); `LocalIntentParser.parse` (Tasks 4–5); `QueryResolver.resolve`, `cardFor`, `AgentCard` (Tasks 1, 6); `HouseholdState`
- Produces:
  - `enum class AgentInputMode { Voice, Text }`
  - `sealed interface AgentUiState` with `Idle(notice: String? = null)`, `Listening(partial: String)`, `Checking(transcript: String)`, `Answer(transcript: String, answer: AgentAnswer)`, `Preview(transcript: String, summary: String, card: AgentCard)`, `Clarify(transcript: String, question: String, options: List<ClarifyOption>)`, `NotUnderstood(transcript: String, message: String)`, `Error(transcript: String, message: String)`
  - `class AgentViewModel(household: () -> HouseholdState, speech: SpeechSource, today: () -> LocalDate = { LocalDate.now() }, checkingMs: Long = 400)` with `state: StateFlow<AgentUiState>`, `mode: StateFlow<AgentInputMode>`, `level: StateFlow<Float>` (mic loudness 0..1 while listening, else 0), `startListening()`, `stopListening()`, `submitText(text: String)`, `choose(option: ClarifyOption)`, `useTextMode(notice: String? = null)`, `useVoiceMode()`, `reset()`

- [ ] **Step 1: Add the test dependency**

In `gradle/libs.versions.toml` under `[libraries]`, add:

```toml
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinxCoroutines" }
```

In `app/build.gradle.kts` after `testImplementation(libs.junit)`, add:

```kotlin
    testImplementation(libs.kotlinx.coroutines.test)
```

- [ ] **Step 2: Write the failing test**

```kotlin
package habitiq.app.agent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeSpeech : SpeechSource {
    override val events = MutableSharedFlow<SpeechEvent>(extraBufferCapacity = 16)
    var started = 0
    var stopped = 0
    var released = false
    override fun start() { started++ }
    override fun stop() { stopped++ }
    override fun release() { released = true }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AgentViewModelTest {
    private val speech = FakeSpeech()
    private var household = Fixtures.state.copy(netBalances = mapOf("u1" to -350.0, "u2" to 350.0))
    private lateinit var vm: AgentViewModel

    @Before fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        vm = AgentViewModel({ household }, speech, { Fixtures.today })
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `voice question flows listening to checking to answer`() = runTest {
        vm.startListening()
        assertEquals(AgentUiState.Listening(""), vm.state.value)
        assertEquals(1, speech.started)
        advanceUntilIdle()
        speech.events.emit(SpeechEvent.Partial("what do"))
        advanceUntilIdle()
        assertEquals(AgentUiState.Listening("what do"), vm.state.value)
        speech.events.emit(SpeechEvent.Final("What do I owe?"))
        advanceUntilIdle()
        val s = vm.state.value as AgentUiState.Answer
        assertEquals("What do I owe?", s.transcript)
        assertEquals("You owe ₹350", s.answer.headline)
    }

    @Test fun `voice level follows the mic only while listening`() = runTest {
        vm.startListening()
        advanceUntilIdle()
        speech.events.emit(SpeechEvent.Level(0.7f))
        advanceUntilIdle()
        assertEquals(0.7f, vm.level.value, 0.001f)
        speech.events.emit(SpeechEvent.Final("what do i owe"))
        advanceUntilIdle()
        assertEquals(0f, vm.level.value, 0.001f)
        speech.events.emit(SpeechEvent.Level(0.9f))
        advanceUntilIdle()
        assertEquals(0f, vm.level.value, 0.001f)
    }

    @Test fun `lazy spend becomes an expense card`() = runTest {
        vm.submitText("I just spent 500")
        advanceUntilIdle()
        val card = (vm.state.value as AgentUiState.Preview).card as AgentCard.Expense
        assertEquals("Expense", card.title)
        assertEquals(50_000L, card.amountPaise)
    }

    @Test fun `typed expense shows a read-only preview`() = runTest {
        vm.submitText("bought milk 60")
        assertEquals(AgentUiState.Checking("bought milk 60"), vm.state.value)
        advanceUntilIdle()
        val s = vm.state.value as AgentUiState.Preview
        assertEquals("Add ₹60 for Milk, split 4 ways", s.summary)
        assertEquals(AgentCard.Expense("Milk", 6_000, "lifestyle", "You", listOf("You", "Ravi", "Priya", "Arjun"), true), s.card)
    }

    @Test fun `clarify then choose`() = runTest {
        household = household.copy(members = household.members + AgentMember("u5", "Ravi", "Ravi Teja"))
        vm.submitText("paid ravi 200")
        advanceUntilIdle()
        val c = vm.state.value as AgentUiState.Clarify
        assertEquals("Which Ravi?", c.question)
        vm.choose(c.options[1])
        val p = vm.state.value as AgentUiState.Preview
        assertEquals("Record ₹200 you paid Ravi", p.summary)
        assertEquals(AgentCard.Payment("You", "Ravi", 20_000), p.card)
    }

    @Test fun `misuse is refused before parsing`() = runTest {
        vm.submitText("bought ganja 500")
        advanceUntilIdle()
        assertEquals(AgentUiState.NotUnderstood("bought ganja 500", ContentGuard.REFUSAL), vm.state.value)
    }

    @Test fun `unknown request is not understood`() = runTest {
        vm.submitText("remind me to call mom")
        advanceUntilIdle()
        assertTrue(vm.state.value is AgentUiState.NotUnderstood)
    }

    @Test fun `unsupported plan shows its reason`() = runTest {
        household = HouseholdState.Empty.copy(myUid = "u1")
        vm.submitText("bought milk 60")
        advanceUntilIdle()
        assertEquals(AgentUiState.NotUnderstood("bought milk 60", "Join or create a flat first to track money."), vm.state.value)
    }

    @Test fun `no match keeps voice mode with a retry message`() = runTest {
        vm.startListening()
        advanceUntilIdle()
        speech.events.emit(SpeechEvent.Error(SpeechErrorKind.NoMatch))
        advanceUntilIdle()
        assertEquals(AgentUiState.Error("", "Didn't catch that. Try again or type it."), vm.state.value)
        assertEquals(AgentInputMode.Voice, vm.mode.value)
    }

    @Test fun `permission error switches to text mode`() = runTest {
        vm.startListening()
        advanceUntilIdle()
        speech.events.emit(SpeechEvent.Error(SpeechErrorKind.NoPermission))
        advanceUntilIdle()
        assertEquals(AgentInputMode.Text, vm.mode.value)
        assertEquals(AgentUiState.Idle("Mic access is off. You can type instead."), vm.state.value)
    }

    @Test fun `unavailable recogniser switches to text mode`() = runTest {
        vm.startListening()
        advanceUntilIdle()
        speech.events.emit(SpeechEvent.Error(SpeechErrorKind.Unavailable))
        advanceUntilIdle()
        assertEquals(AgentInputMode.Text, vm.mode.value)
        assertEquals(AgentUiState.Idle("Voice isn't available on this phone. You can type instead."), vm.state.value)
    }

    @Test fun `blank text does nothing and reset stops listening`() = runTest {
        vm.submitText("   ")
        assertEquals(AgentUiState.Idle(), vm.state.value)
        vm.startListening()
        vm.reset()
        assertEquals(AgentUiState.Idle(), vm.state.value)
        assertEquals(1, speech.stopped)
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.AgentViewModelTest"`
Expected: FAIL, `Unresolved reference: AgentViewModel`.

- [ ] **Step 4: Write `AgentViewModel.kt`**

```kotlin
package habitiq.app.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AgentInputMode { Voice, Text }

sealed interface AgentUiState {
    data class Idle(val notice: String? = null) : AgentUiState
    data class Listening(val partial: String) : AgentUiState
    data class Checking(val transcript: String) : AgentUiState
    data class Answer(val transcript: String, val answer: AgentAnswer) : AgentUiState
    data class Preview(val transcript: String, val summary: String, val card: AgentCard) : AgentUiState
    data class Clarify(val transcript: String, val question: String, val options: List<ClarifyOption>) : AgentUiState
    data class NotUnderstood(val transcript: String, val message: String) : AgentUiState
    data class Error(val transcript: String, val message: String) : AgentUiState
}

private const val NOT_YET = "I can't do that one yet. Try \"What do I owe?\", \"What's due today?\" or \"Bought milk 60\"."

/**
 * Drives the agent sheet: Idle → Listening → Checking → Answer / Preview / Clarify, with errors
 * going back to Idle or Error. M1 never writes data; previews are read-only.
 */
class AgentViewModel(
    private val household: () -> HouseholdState,
    private val speech: SpeechSource,
    private val today: () -> LocalDate = { LocalDate.now() },
    private val checkingMs: Long = 400,
) : ViewModel() {
    private val _state = MutableStateFlow<AgentUiState>(AgentUiState.Idle())
    val state: StateFlow<AgentUiState> = _state.asStateFlow()

    private val _mode = MutableStateFlow(AgentInputMode.Voice)
    val mode: StateFlow<AgentInputMode> = _mode.asStateFlow()

    /** Kept apart from [state] so the listening animation can follow the voice without recomposing the whole sheet. */
    private val _level = MutableStateFlow(0f)
    val level: StateFlow<Float> = _level.asStateFlow()

    private var work: Job? = null

    init {
        viewModelScope.launch { speech.events.collect(::onSpeech) }
    }

    fun startListening() {
        work?.cancel()
        _mode.value = AgentInputMode.Voice
        _state.value = AgentUiState.Listening("")
        speech.start()
    }

    fun stopListening() = speech.stop()

    fun useTextMode(notice: String? = null) {
        speech.stop()
        _mode.value = AgentInputMode.Text
        if (notice != null || _state.value is AgentUiState.Listening) _state.value = AgentUiState.Idle(notice)
    }

    fun useVoiceMode() { _mode.value = AgentInputMode.Voice }

    fun reset() {
        work?.cancel()
        speech.stop()
        _level.value = 0f
        _state.value = AgentUiState.Idle()
    }

    fun submitText(text: String) {
        val transcript = text.trim()
        if (transcript.isEmpty()) return
        work?.cancel()
        _state.value = AgentUiState.Checking(transcript)
        work = viewModelScope.launch {
            delay(checkingMs) // long enough for "Checking your flat" to read
            ContentGuard.reasonToBlock(transcript)?.let {
                _state.value = AgentUiState.NotUnderstood(transcript, it)
                return@launch
            }
            val home = household()
            _state.value = when (val result = LocalIntentParser.parse(transcript, home, today())) {
                ParseResult.NeedsModel -> AgentUiState.NotUnderstood(transcript, NOT_YET)
                is ParseResult.Confident -> stateFor(transcript, result.plan, home)
            }
        }
    }

    fun choose(option: ClarifyOption) {
        val transcript = (_state.value as? AgentUiState.Clarify)?.transcript ?: return
        _state.value = stateFor(transcript, option.plan, household())
    }

    private fun stateFor(transcript: String, plan: AgentPlan, home: HouseholdState): AgentUiState = when (plan) {
        is AgentPlan.Answer -> AgentUiState.Answer(transcript, QueryResolver.resolve(plan.query, home, today()))
        is AgentPlan.Actions -> AgentUiState.Preview(transcript, plan.summary, cardFor(plan, home))
        is AgentPlan.Clarify -> AgentUiState.Clarify(transcript, plan.question, plan.options)
        is AgentPlan.Unsupported -> AgentUiState.NotUnderstood(transcript, plan.reason)
    }

    private fun onSpeech(event: SpeechEvent) {
        if (event !is SpeechEvent.Level && event !is SpeechEvent.Partial) _level.value = 0f
        when (event) {
            is SpeechEvent.Level -> if (_state.value is AgentUiState.Listening) _level.value = event.level
            is SpeechEvent.Partial -> if (_state.value is AgentUiState.Listening) _state.value = AgentUiState.Listening(event.text)
            is SpeechEvent.Final -> submitText(event.text)
            is SpeechEvent.Error -> when (event.kind) {
                SpeechErrorKind.NoPermission -> useTextMode("Mic access is off. You can type instead.")
                SpeechErrorKind.Unavailable -> useTextMode("Voice isn't available on this phone. You can type instead.")
                SpeechErrorKind.Network -> _state.value = AgentUiState.Error("", "Voice needs internet on this phone right now. Try typing it.")
                SpeechErrorKind.NoMatch, SpeechErrorKind.Busy, SpeechErrorKind.Other ->
                    _state.value = AgentUiState.Error("", "Didn't catch that. Try again or type it.")
            }
        }
    }

    override fun onCleared() = speech.release()
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.agent.AgentViewModelTest"`
Expected: PASS (12 tests). If `voice question flows…` stays in `Listening("")`, the collector in `init` hasn't started yet. The `advanceUntilIdle()` right after `startListening()` is there to start it, so keep it.

- [ ] **Step 6: Commit**

```bash
git add apps/android/gradle/libs.versions.toml apps/android/app/build.gradle.kts apps/android/app/src/main/kotlin/habitiq/app/agent/AgentViewModel.kt apps/android/app/src/test/kotlin/habitiq/app/agent/AgentViewModelTest.kt
git commit -m "feat(agent): view model state machine for voice and text requests"
```

---

### Task 9: Mic button in the centre of the nav

**Files:**
- Modify: `app/src/main/kotlin/habitiq/app/ui/AppShell.kt`
- Modify: `app/src/test/kotlin/habitiq/app/screenshots/ShellScreenshotTest.kt`

**Interfaces:**
- Produces: `AppShell(..., onMic: (() -> Unit)? = null, content)`. When `onMic` is non-null the centre slot always shows the mic: tap → `onMic()`, long-press → Quick add (`Menu` toggles the overlay, `Direct` runs its action, `None` does nothing). When `onMic` is null the shell behaves exactly as today.

- [ ] **Step 1: Add the mic case to the screenshot test**

In `ShellScreenshotTest.sheet(...)`, add a fifth row after `"Profile"`. Change the list element type so each row can carry an `onMic` too:

```kotlin
            listOf(
                Triple("Home", AppTab.HOME, ShellCreate.None),
                Triple("Discover", AppTab.DISCOVER, ShellCreate.Direct("Create a Discovery post") {}),
                Triple("Manage", AppTab.TASKS, ShellCreate.Direct("Add task") {}),
                Triple("Profile", AppTab.PROFILE, ShellCreate.None),
                Triple("Home + mic", AppTab.HOME, ShellCreate.Menu(listOf(habitiq.app.ui.CreateOption("Add expense") {}))),
            ).forEach { (label, tab, create) ->
                Text(label, style = HqType.labelSmall, color = c.textSecondary, modifier = Modifier.padding(horizontal = 20.dp))
                Box(Modifier.fillMaxWidth().height(140.dp)) {
                    AppShell(
                        selectedTab = tab, onTabSelected = {}, createAction = create,
                        onMic = if (label.endsWith("mic")) ({}) else null,
                    ) { Box(Modifier.background(c.canvas)) }
                }
            }
```

- [ ] **Step 2: Run it to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.screenshots.ShellScreenshotTest"`
Expected: FAIL, compile error `No parameter with name 'onMic' found`.

- [ ] **Step 3: Add `onMic` to `AppShell` and `ShellBar`, and add `MicButton`**

In `AppShell`'s signature add `onMic: (() -> Unit)? = null,` after `createAction`. Pass it to `ShellBar` and add a matching parameter:

```kotlin
            ShellBar(
                selectedTab = selectedTab,
                onTabSelected = { menuOpen = false; onTabSelected(it) },
                create = createAction,
                menuOpen = menuOpen,
                onMic = onMic?.let { mic -> { menuOpen = false; mic() } },
            ) { menuOpen = !menuOpen }
```

```kotlin
private fun ShellBar(selectedTab: AppTab, onTabSelected: (AppTab) -> Unit, create: ShellCreate, menuOpen: Boolean, onMic: (() -> Unit)?, onOpenMenu: () -> Unit) {
```

Replace the centre-slot `when` with:

```kotlin
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                when {
                    onMic != null -> MicButton(
                        menuOpen = menuOpen,
                        onTap = { if (menuOpen) onOpenMenu() else onMic() },
                        onLongPress = when (create) {
                            ShellCreate.None -> null
                            is ShellCreate.Direct -> create.onClick
                            is ShellCreate.Menu -> onOpenMenu
                        },
                    )
                    create is ShellCreate.Direct -> CreateButton(create.label, false, create.onClick)
                    create is ShellCreate.Menu -> CreateButton(if (menuOpen) "Close quick add" else "Quick add", menuOpen, onOpenMenu)
                    else -> Unit
                }
            }
```

Add the button below `CreateButton`. It uses the same raised rounded square, so the nav looks unchanged apart from the glyph:

```kotlin
/**
 * The agent entry: the same raised button as Quick add with a mic glyph. Tap talks to Oddroof;
 * long-press opens Quick add. While Quick add is open the button shows × and a tap closes it.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun MicButton(menuOpen: Boolean, onTap: () -> Unit, onLongPress: (() -> Unit)?) {
    val c = LocalHqColors.current
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 0.88f else 1f, spring(dampingRatio = 0.5f, stiffness = 900f), label = "micPress")
    val fill by animateColorAsState(if (menuOpen) c.textPrimary else c.actionPrimaryBg, tween(220), label = "micFill")
    val glyph by animateColorAsState(if (menuOpen) c.canvas else c.actionPrimaryFg, tween(220), label = "micGlyph")
    val shape = RoundedCornerShape(17.dp)
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
    Box(
        Modifier
            .offset(y = (-24).dp)
            .graphicsLayer { scaleX = press; scaleY = press }
            .shadow(10.dp, shape, ambientColor = c.brandTeal.copy(alpha = .35f), spotColor = c.brandTeal.copy(alpha = .35f))
            .size(54.dp)
            .clip(shape)
            .background(fill)
            .border(4.dp, c.surfaceBase, shape)
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClickLabel = if (menuOpen) "Close quick add" else "Talk to Oddroof",
                onLongClickLabel = if (onLongPress != null) "Quick add" else null,
                onLongClick = onLongPress?.let { action ->
                    { haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); action() }
                },
                onClick = onTap,
            )
            .semantics { contentDescription = if (menuOpen) "Close quick add" else "Talk to Oddroof" },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (menuOpen) HqIcons.Plus else androidx.compose.material.icons.Icons.Rounded.Mic,
            contentDescription = null,
            tint = glyph,
            modifier = Modifier.size(27.dp).graphicsLayer { rotationZ = if (menuOpen) 45f else 0f },
        )
    }
}
```

Add the imports `androidx.compose.foundation.combinedClickable` and `androidx.compose.material.icons.rounded.Mic`.

- [ ] **Step 4: Run tests and look at the screenshot**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.screenshots.ShellScreenshotTest"`
Expected: PASS. Open `app/build/screenshots/shell-light.png` and `shell-dark.png`: the last row shows the same raised teal button with a mic glyph, and the four tabs haven't moved.

- [ ] **Step 5: Commit**

```bash
git add apps/android/app/src/main/kotlin/habitiq/app/ui/AppShell.kt apps/android/app/src/test/kotlin/habitiq/app/screenshots/ShellScreenshotTest.kt
git commit -m "feat(shell): centre mic button, long-press keeps Quick add"
```

---

### Task 10: Agent sheet UI — live listening, words as you speak, result cards

The rest of the app stays a regular app; this sheet is where the agent feels alive. The product thesis (from Sai's friend's research): people won't fill in sheets or open report screens, but they will say "I just spent 500". So speaking has to feel rewarding:

- **While listening:** a soft teal→coral aura behind a mic that breathes and swells with the voice, and the person's words appearing live in large type.
- **While checking:** the transcript stays and a short "Checking your flat…" line shows.
- **The result is a card, not a text list:** an expense shows as a receipt-style card (category tile, title, the amount counting up, "Paid by", a row of split avatars), and a payment shows two avatars with an arrow and the amount. Cards rise in with a short spring. Questions get an answer card with the headline and lines.
- **System animations off:** everything is static, with no breathing, count-up or rise.

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/ui/agent/AgentVisuals.kt` (aura, cards, avatars)
- Create: `app/src/main/kotlin/habitiq/app/ui/agent/AgentSheet.kt` (sheet layout per state)
- Test: `app/src/test/kotlin/habitiq/app/screenshots/AgentSheetScreenshotTest.kt`

**Interfaces:**
- Consumes: `AgentUiState`, `AgentInputMode`, `AgentCard`, `ClarifyOption`, `AgentLink`, `AnswerLine` (Tasks 1, 6, 8); `HqBottomSheet`, `HqButton`, `HqButtonVariant`, `HqChip`, `HqChipFlow`, `HqTextField`, `HqIconTile`, `HqTileTone`, `HqIcons` (existing components); `hqReduceMotion()`; `formatInr`
- Produces:
  - `@Composable fun AgentSheet(state: AgentUiState, mode: AgentInputMode, level: Float, onDismiss: () -> Unit, onTalk: () -> Unit, onStop: () -> Unit, onSubmit: (String) -> Unit, onChoose: (ClarifyOption) -> Unit, onOpen: (AgentLink) -> Unit, onUseText: () -> Unit, onUseVoice: () -> Unit, onAllowMic: () -> Unit)`
  - `@Composable fun AgentSheetBody(...)` with the same parameters minus `onDismiss`, plus `animate: Boolean = true`. This is the content without the modal window, for screenshots. `animate = false` turns off the infinite aura so Robolectric can go idle.
  - `@Composable fun VoiceAura(level: Float, animate: Boolean, modifier: Modifier = Modifier)`, `@Composable fun ExpenseCard(card: AgentCard.Expense, animate: Boolean)`, `@Composable fun PaymentCard(card: AgentCard.Payment, animate: Boolean)`, `@Composable fun AnswerCard(headline: String, lines: List<AnswerLine>)`

- [ ] **Step 1: Write the screenshot test**

```kotlin
package habitiq.app.screenshots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import habitiq.app.agent.AgentAnswer
import habitiq.app.agent.AgentCard
import habitiq.app.agent.AgentInputMode
import habitiq.app.agent.AgentLink
import habitiq.app.agent.AgentPlan
import habitiq.app.agent.AgentUiState
import habitiq.app.agent.AnswerLine
import habitiq.app.agent.ClarifyOption
import habitiq.app.ui.agent.AgentSheetBody
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AgentSheetScreenshotTest : ScreenshotHarness() {
    private val everyone = listOf("You", "Ravi", "Priya", "Arjun")

    private val voice = listOf(
        AgentUiState.Idle(),
        AgentUiState.Listening("I just spent five hundred on"),
        AgentUiState.Answer("What do I owe?", AgentAnswer("You owe ₹350", listOf(AnswerLine("Pay Ravi", "₹350")), AgentLink.BALANCES)),
        AgentUiState.Clarify("paid ravi 200", "Which Ravi?", listOf(
            ClarifyOption("Ravi Kumar", AgentPlan.Unsupported("")), ClarifyOption("Ravi Teja", AgentPlan.Unsupported("")))),
    )
    private val cards = listOf(
        AgentUiState.Preview("Bought groceries for 560", "Add ₹560 for Groceries, split 4 ways",
            AgentCard.Expense("Groceries", 56_000, "lifestyle", "You", everyone, everyone = true)),
        AgentUiState.Preview("I just spent 500", "Add ₹500 for Expense, split 4 ways",
            AgentCard.Expense("Expense", 50_000, "other", "You", everyone, everyone = true)),
        AgentUiState.Preview("Paid Ravi 200", "Record ₹200 you paid Ravi", AgentCard.Payment("You", "Ravi", 20_000)),
        AgentUiState.Idle("Mic access is off. You can type instead."),
    )

    private fun page(name: String, dark: Boolean, states: List<AgentUiState>, mode: AgentInputMode) = shoot(name, dark = dark) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
            states.forEach { state ->
                AgentSheetBody(state, mode, level = 0.6f, onTalk = {}, onStop = {}, onSubmit = {}, onChoose = {}, onOpen = {},
                    onUseText = {}, onUseVoice = {}, onAllowMic = {}, animate = false)
            }
        }
    }

    @Test fun voiceLight() = page("agent-voice-light", false, voice, AgentInputMode.Voice)
    @Test fun voiceDark() = page("agent-voice-dark", true, voice, AgentInputMode.Voice)
    @Test fun cardsLight() = page("agent-cards-light", false, cards, AgentInputMode.Text)
    @Test fun cardsDark() = page("agent-cards-dark", true, cards, AgentInputMode.Text)
}
```

- [ ] **Step 2: Run it to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.screenshots.AgentSheetScreenshotTest"`
Expected: FAIL, `Unresolved reference: AgentSheetBody`.

- [ ] **Step 3: Write `AgentVisuals.kt`**

```kotlin
package habitiq.app.ui.agent

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import habitiq.app.agent.AgentCard
import habitiq.app.agent.AnswerLine
import habitiq.app.lib.formatInr
import habitiq.app.ui.components.HqIconTile
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqTileTone
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import kotlin.math.roundToLong

/**
 * Three soft teal→coral rings around a mic. They breathe slowly and swell with [level] (0..1).
 * Uses transform-only drawing. With [animate] false it's a still glow.
 */
@Composable
fun VoiceAura(level: Float, animate: Boolean, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    val voice by animateFloatAsState(if (animate) level else 0f, spring(dampingRatio = 0.6f, stiffness = 280f), label = "voice")
    val breath = if (animate) {
        rememberInfiniteTransition(label = "aura").animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "breath",
        ).value
    } else 0f
    Box(modifier.size(148.dp).semantics { contentDescription = "Listening" }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(148.dp)) {
            val base = size.minDimension / 2
            listOf(1f to .14f, .78f to .22f, .58f to .34f).forEachIndexed { i, (scale, alpha) ->
                val swell = 1f + (0.05f * breath + 0.20f * voice) * (3 - i) / 3f
                val r = base * scale * swell.coerceAtMost(1f / scale)
                drawCircle(
                    Brush.radialGradient(
                        listOf(c.brandTeal.copy(alpha = alpha), c.brandCoral.copy(alpha = alpha * .55f), Color.Transparent),
                        center = center, radius = r,
                    ),
                    radius = r,
                )
            }
        }
        Box(
            Modifier.size(60.dp).graphicsLayer { val s = 1f + 0.06f * voice; scaleX = s; scaleY = s }
                .shadow(12.dp, CircleShape, ambientColor = c.brandTeal.copy(alpha = .4f), spotColor = c.brandTeal.copy(alpha = .4f))
                .clip(CircleShape).background(c.actionPrimaryBg),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.Mic, null, tint = c.actionPrimaryFg, modifier = Modifier.size(28.dp)) }
    }
}

/** Cards rise and settle in once. Returns a 0..1 progress; it's 1 immediately when [animate] is false. */
@Composable
private fun rememberEntrance(animate: Boolean): Float {
    val p = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(Unit) { if (animate) p.animateTo(1f, spring(dampingRatio = 0.72f, stiffness = 380f)) }
    return p.value
}

private fun Modifier.entrance(p: Float) = graphicsLayer {
    alpha = p.coerceIn(0f, 1f)
    translationY = (1f - p) * 24.dp.toPx()
    val s = 0.96f + 0.04f * p
    scaleX = s; scaleY = s
}

@Composable
private fun CountUpAmount(paise: Long, animate: Boolean) {
    val c = LocalHqColors.current
    val p = remember(paise) { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(paise) { if (animate) p.animateTo(1f, tween(650, easing = FastOutSlowInEasing)) }
    Text(
        formatInr((paise * p.value).roundToLong() / 100.0),
        style = HqType.titleLarge2, color = c.textPrimary,
        modifier = Modifier.semantics { contentDescription = formatInr(paise / 100.0) },
    )
}

private fun categoryLook(category: String): Pair<ImageVector, HqTileTone> = when (category) {
    "lifestyle" -> HqIcons.Receipt to HqTileTone.Sand
    "bills" -> HqIcons.Clock to HqTileTone.Coral
    "chores" -> HqIcons.Check to HqTileTone.Teal
    else -> HqIcons.Receipt to HqTileTone.Neutral
}

@Composable
private fun CardFrame(p: Float, content: @Composable () -> Unit) {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(22.dp)
    Box(
        Modifier.entrance(p).fillMaxWidth()
            .shadow(10.dp, shape, ambientColor = Color.Black.copy(alpha = .10f), spotColor = Color.Black.copy(alpha = .10f))
            .clip(shape).background(c.surfaceRaised).border(1.dp, c.borderSubtle, shape)
            .padding(18.dp),
    ) { content() }
}

@Composable
private fun Initial(name: String, ring: Color, size: Int = 30) {
    val c = LocalHqColors.current
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(c.selectedBg).border(2.dp, ring, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(name.take(1).uppercase(), style = HqType.labelSmall, color = c.selectedFg, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AvatarStack(names: List<String>) {
    val c = LocalHqColors.current
    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
        names.take(5).forEach { Initial(it, ring = c.surfaceRaised) }
        if (names.size > 5) Initial("+${names.size - 5}", ring = c.surfaceRaised)
    }
}

/** Receipt-style card for an expense the agent understood. */
@Composable
fun ExpenseCard(card: AgentCard.Expense, animate: Boolean) {
    val c = LocalHqColors.current
    val (icon, tone) = categoryLook(card.category)
    CardFrame(rememberEntrance(animate)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HqIconTile(icon, tone, size = 44)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(card.title, style = HqType.rowTitle, color = c.textPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text("Paid by ${card.paidBy}", style = HqType.bodySmall, color = c.textSecondary)
                }
            }
            CountUpAmount(card.amountPaise, animate)
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.borderSubtle))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarStack(card.splitNames)
                Spacer(Modifier.width(10.dp))
                Text(
                    if (card.everyone) "Split with everyone (${card.splitNames.size})" else "Split with ${card.splitNames.joinToString(", ")}",
                    style = HqType.bodySmall, color = c.textSecondary, maxLines = 2,
                )
            }
        }
    }
}

/** Two people and an arrow: who paid whom, and how much. */
@Composable
fun PaymentCard(card: AgentCard.Payment, animate: Boolean) {
    val c = LocalHqColors.current
    CardFrame(rememberEntrance(animate)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Initial(card.from, ring = c.brandTeal, size = 44)
                    Text(card.from, style = HqType.bodySmall, color = c.textSecondary)
                }
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = c.textBrand, modifier = Modifier.size(24.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Initial(card.to, ring = c.brandCoral, size = 44)
                    Text(card.to, style = HqType.bodySmall, color = c.textSecondary)
                }
            }
            CountUpAmount(card.amountPaise, animate)
            Text("Payment", style = HqType.labelSmall, color = c.textMuted)
        }
    }
}

/** Headline and label/value lines for questions. */
@Composable
fun AnswerCard(headline: String, lines: List<AnswerLine>) {
    val c = LocalHqColors.current
    CardFrame(1f) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(headline, style = HqType.titleMedium2, color = c.textPrimary)
            lines.forEach { line ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(line.label, style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.weight(1f))
                    Text(line.value, style = HqType.rowTitle, color = c.textPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
```

Two notes:
- `Arrangement.spacedBy((-8).dp)` overlaps the avatars. If the Compose version rejects negative spacing, use `Box` with `Modifier.offset(x = (i * 22).dp)` per avatar.
- `AnswerCard` passes `1f` to `CardFrame` because answers appear instantly. Only action cards rise in.

- [ ] **Step 4: Write `AgentSheet.kt`**

```kotlin
package habitiq.app.ui.agent

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import habitiq.app.agent.AgentCard
import habitiq.app.agent.AgentInputMode
import habitiq.app.agent.AgentLink
import habitiq.app.agent.AgentUiState
import habitiq.app.agent.ClarifyOption
import habitiq.app.ui.components.HqBottomSheet
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqChipFlow
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import habitiq.app.ui.theme.hqReduceMotion

/** First things to try. Tapping one runs it, so a new user sees a real card in one tap. */
private val STARTERS = listOf("I just spent 500", "What do I owe?", "What's due today?")

/** The agent in a regular bottom sheet over the current screen. */
@Composable
fun AgentSheet(
    state: AgentUiState, mode: AgentInputMode, level: Float, onDismiss: () -> Unit,
    onTalk: () -> Unit, onStop: () -> Unit, onSubmit: (String) -> Unit, onChoose: (ClarifyOption) -> Unit,
    onOpen: (AgentLink) -> Unit, onUseText: () -> Unit, onUseVoice: () -> Unit, onAllowMic: () -> Unit,
) {
    HqBottomSheet(onDismiss = onDismiss) {
        AgentSheetBody(state, mode, level, onTalk, onStop, onSubmit, onChoose, onOpen, onUseText, onUseVoice, onAllowMic,
            animate = !hqReduceMotion())
    }
}

@Composable
fun AgentSheetBody(
    state: AgentUiState, mode: AgentInputMode, level: Float,
    onTalk: () -> Unit, onStop: () -> Unit, onSubmit: (String) -> Unit, onChoose: (ClarifyOption) -> Unit,
    onOpen: (AgentLink) -> Unit, onUseText: () -> Unit, onUseVoice: () -> Unit, onAllowMic: () -> Unit,
    animate: Boolean = true,
) {
    val c = LocalHqColors.current
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp).animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(HqSpacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Oddroof", style = HqType.labelMedium, color = c.textSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = if (mode == AgentInputMode.Voice) onUseText else onUseVoice) {
                Icon(
                    if (mode == AgentInputMode.Voice) Icons.Rounded.Keyboard else Icons.Rounded.Mic,
                    contentDescription = if (mode == AgentInputMode.Voice) "Type instead" else "Talk instead",
                    tint = c.iconDefault,
                )
            }
        }

        Column(
            Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
            verticalArrangement = Arrangement.spacedBy(HqSpacing.sm),
        ) {
            when (state) {
                is AgentUiState.Idle -> {
                    Text("What can I take care of?", style = HqType.titleMedium2, color = c.textPrimary)
                    Text("Just say it, like \"I spent 500 on groceries\". I'll do the forms.", style = HqType.bodyMedium, color = c.textSecondary)
                    state.notice?.let {
                        Text(it, style = HqType.bodyMedium, color = c.statusWarningFg)
                        HqButton("Allow mic", onAllowMic, variant = HqButtonVariant.Tertiary, fullWidth = false)
                    }
                    HqChipFlow { STARTERS.forEach { HqChip(label = it, onClick = { onSubmit(it) }) } }
                }
                is AgentUiState.Listening -> {
                    VoiceAura(level, animate, Modifier.align(Alignment.CenterHorizontally))
                    Text(
                        state.partial.ifEmpty { "Listening…" },
                        style = HqType.titleMedium2,
                        color = if (state.partial.isEmpty()) c.textMuted else c.textPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                is AgentUiState.Checking -> {
                    Transcript(state.transcript)
                    Text("Checking your flat…", style = HqType.bodyMedium, color = c.textSecondary)
                }
                is AgentUiState.Answer -> {
                    Transcript(state.transcript)
                    AnswerCard(state.answer.headline, state.answer.lines)
                    state.answer.link?.let { HqButton(it.label, { onOpen(it) }, variant = HqButtonVariant.Secondary) }
                }
                is AgentUiState.Preview -> {
                    Transcript(state.transcript)
                    when (val card = state.card) {
                        is AgentCard.Expense -> ExpenseCard(card, animate)
                        is AgentCard.Payment -> PaymentCard(card, animate)
                    }
                    Text("Saving from here arrives in the next update.", style = HqType.bodySmall, color = c.textMuted)
                    HqButton(AgentLink.EXPENSES.label, { onOpen(AgentLink.EXPENSES) }, variant = HqButtonVariant.Secondary)
                }
                is AgentUiState.Clarify -> {
                    Transcript(state.transcript)
                    Text(state.question, style = HqType.titleMedium2, color = c.textPrimary)
                    HqChipFlow { state.options.forEach { o -> HqChip(label = o.label, onClick = { onChoose(o) }) } }
                }
                is AgentUiState.NotUnderstood -> {
                    Transcript(state.transcript)
                    Text(state.message, style = HqType.rowTitle, color = c.textPrimary)
                }
                is AgentUiState.Error -> {
                    if (state.transcript.isNotEmpty()) Transcript(state.transcript)
                    Text(state.message, style = HqType.rowTitle, color = c.textPrimary)
                }
            }
        }

        if (mode == AgentInputMode.Text) {
            var text by rememberSaveable { mutableStateOf("") }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                HqTextField(value = text, onValueChange = { text = it }, label = "Ask or tell Oddroof", modifier = Modifier.weight(1f),
                    placeholder = "e.g. I spent 500 on groceries")
                IconButton(onClick = { onSubmit(text); text = "" }, enabled = text.isNotBlank(), modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send", tint = c.textBrand)
                }
            }
        } else if (state is AgentUiState.Listening) {
            HqButton("Done", onStop, variant = HqButtonVariant.Secondary)
        } else {
            HqButton(if (state is AgentUiState.Idle) "Tap to talk" else "Say something else", onTalk, leadingIcon = Icons.Rounded.Mic)
        }
    }
}

@Composable
private fun Transcript(text: String) {
    val c = LocalHqColors.current
    Text("“$text”", style = HqType.rowTitle, color = c.textSecondary)
}
```

- [ ] **Step 5: Run tests and look at the screenshots**

Run: `./gradlew :app:testDebugUnitTest --tests "habitiq.app.screenshots.AgentSheetScreenshotTest"`
Expected: PASS (4 tests). Open `app/build/screenshots/agent-voice-light.png`, `agent-voice-dark.png`, `agent-cards-light.png` and `agent-cards-dark.png` and check:
- The aura is centred, its rings fade out softly with no hard edge, and the live transcript reads in large type.
- The expense card shows the category tile, title, "Paid by You", the full amount (the count-up finished), a divider, and overlapping avatars with "Split with everyone (4)".
- The payment card shows You → Ravi with the amount.
- Everything is readable in dark mode; nothing is clipped at 390dp.

If the test hangs, an infinite animation is running. Check that `animate = false` reaches `VoiceAura`.

- [ ] **Step 6: Commit**

```bash
git add apps/android/app/src/main/kotlin/habitiq/app/ui/agent apps/android/app/src/test/kotlin/habitiq/app/screenshots/AgentSheetScreenshotTest.kt
git commit -m "feat(agent): live listening aura, live transcript and expense/payment cards"
```

---

### Task 11: Wire it into the app, build the APK

**Files:**
- Modify: `app/src/main/kotlin/habitiq/app/HabitiqApp.kt` (the block around the `AppShell(` call, about lines 520–560, and its enclosing composable for state)
- Modify: `docs/superpowers/specs/2026-10-03-oddroof-agent-design.md` (record the M1 scope change)

**Interfaces:**
- Consumes: `AgentViewModel`, `AndroidVoiceInput`, `householdStateOf`, `AgentSheet`, `AgentLink`, `AppShell(onMic = …)`, `FlatViewModel` flows (`currentUser`, `flatInfo`, `members`, `tasks`, `billInstances`, `vacancies`, `computeNetBalances()`, `ensureDiscovery()`, `showBalancesTrigger`), `habitiq.app.lib.currentMonthKey()`

- [ ] **Step 1: Create the view model and sheet state next to the other shell state**

Put this in the same composable scope as `selectedTab`, `manageAreaName` and `openMonthlyBills()`, after `openMonthlyBills` is defined:

```kotlin
val appContext = androidx.compose.ui.platform.LocalContext.current.applicationContext
val agentViewModel: habitiq.app.agent.AgentViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
    habitiq.app.agent.AgentViewModel(
        household = {
            habitiq.app.agent.householdStateOf(
                myUid = flatViewModel.currentUser.value?.uid.orEmpty(),
                flat = flatViewModel.flatInfo.value,
                members = flatViewModel.members.value,
                tasks = flatViewModel.tasks.value,
                netBalances = flatViewModel.computeNetBalances(),
                billInstances = flatViewModel.billInstances.value,
                vacancies = flatViewModel.vacancies.value,
                month = habitiq.app.lib.currentMonthKey(),
            )
        },
        speech = habitiq.app.agent.AndroidVoiceInput(appContext),
    )
}
val agentState by agentViewModel.state.collectAsStateWithLifecycleCompat()
val agentMode by agentViewModel.mode.collectAsStateWithLifecycleCompat()
val agentLevel by agentViewModel.level.collectAsStateWithLifecycleCompat()
var agentOpen by rememberSaveable { mutableStateOf(false) }
val micPermission = androidx.activity.compose.rememberLauncherForActivityResult(
    androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
) { granted ->
    if (granted) agentViewModel.startListening()
    else agentViewModel.useTextMode("Mic access is off. You can type instead.")
}
fun talk() {
    val granted = androidx.core.content.ContextCompat.checkSelfPermission(appContext, android.Manifest.permission.RECORD_AUDIO) ==
        android.content.pm.PackageManager.PERMISSION_GRANTED
    if (granted) agentViewModel.startListening() else micPermission.launch(android.Manifest.permission.RECORD_AUDIO)
}
```

`collectAsStateWithLifecycleCompat` is the helper `ExpensesScreen.kt` already uses. If it isn't visible from `HabitiqApp.kt`, use `androidx.lifecycle.compose.collectAsStateWithLifecycle()` instead. Match whichever `HabitiqApp.kt` already imports for other flows.

- [ ] **Step 2: Pass `onMic` to `AppShell` and show the sheet**

Change the `AppShell(` call to:

```kotlin
AppShell(
    selectedTab = selectedTab,
    createAction = createAction,
    onMic = {
        agentOpen = true
        flatViewModel.ensureDiscovery() // flat-search answers need vacancies loaded
        if (agentMode == habitiq.app.agent.AgentInputMode.Voice) talk()
    },
    onTabSelected = {
        if (it != selectedTab) moneyOverlay = null
        selectedTab = it
    }
) {
```

Right after the `AppShell { … }` block closes (still inside the same `else` branch), add:

```kotlin
if (agentOpen) {
    habitiq.app.ui.agent.AgentSheet(
        state = agentState,
        mode = agentMode,
        level = agentLevel,
        onDismiss = { agentViewModel.reset(); agentOpen = false },
        onTalk = { talk() },
        onStop = { agentViewModel.stopListening() },
        onSubmit = { agentViewModel.submitText(it) },
        onChoose = { agentViewModel.choose(it) },
        onUseText = { agentViewModel.useTextMode() },
        onUseVoice = { agentViewModel.useVoiceMode(); talk() },
        onAllowMic = { micPermission.launch(android.Manifest.permission.RECORD_AUDIO) },
        onOpen = { link ->
            agentViewModel.reset(); agentOpen = false
            when (link) {
                habitiq.app.agent.AgentLink.BALANCES -> {
                    selectedTab = AppTab.TASKS
                    manageAreaName = ManageFlatArea.EXPENSES.name
                    flatViewModel.showBalancesTrigger.value = true
                }
                habitiq.app.agent.AgentLink.EXPENSES -> {
                    selectedTab = AppTab.TASKS
                    manageAreaName = ManageFlatArea.EXPENSES.name
                }
                habitiq.app.agent.AgentLink.TASKS -> {
                    selectedTab = AppTab.TASKS
                    manageAreaName = ManageFlatArea.TASKS.name
                }
                habitiq.app.agent.AgentLink.BILLS -> openMonthlyBills()
                habitiq.app.agent.AgentLink.DISCOVER -> selectedTab = AppTab.DISCOVER
            }
        },
    )
}
```

- [ ] **Step 3: Record the scope change in the spec**

In `docs/superpowers/specs/2026-10-03-oddroof-agent-design.md`, replace milestone 1's bullet in section 6 with:

```markdown
1. **Agent shell:** plain mic button in the nav centre (tap = agent, long-press = Quick add), agent in a regular `HqBottomSheet`, `VoiceInput`, text mode, `LocalIntentParser` for money + questions, `QueryResolver`, Answer cards, read-only previews for money commands. Zero AI cost. *(Changed 2026-10-03: no glass and no orb in M1. Sai wants the app to stay a regular app until the agent is proven; all glass moves to sub-project 3.)*
```

Under section 3.4's heading, add the line: `> Deferred to sub-project 3 (decision 2026-10-03). M1 uses a plain mic button and the standard sheet.`

- [ ] **Step 4: Run the full unit test suite and build the APK**

Run: `./gradlew :app:testDebugUnitTest`
Expected: all tests pass. The pre-existing suite plus the new `habitiq.app.agent.*` and screenshot tests show 0 failures.

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`; the APK is at `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 5: Hand-test checklist for Sai (device; don't drive adb)**

Give Sai the APK path and this list:
1. Tap the mic → permission prompt → allow → the aura breathes and swells as you talk, and your words appear live → say "What do I owe?" → the answer card shows with "Open balances".
1a. Say "I just spent 500" → an expense card rises in, the amount counts up to ₹500, and it's split with everyone.
2. Long-press the mic → Quick add opens as before.
3. Keyboard icon → type "bought groceries 560" → the preview says ₹560, Groceries, split with everyone.
4. Say "paid <flatmate> 200" → the settle preview names that person.
5. Type "flats in <area> under 10k" → up to 5 listings, or "No places match yet".
6. Deny the mic permission (fresh install) → the sheet shows "Mic access is off…" with "Allow mic", and typing still works.
7. Dark mode: the sheet is readable.

- [ ] **Step 6: Commit**

```bash
git add apps/android/app/src/main/kotlin/habitiq/app/HabitiqApp.kt docs/superpowers/specs/2026-10-03-oddroof-agent-design.md
git commit -m "feat(agent): wire mic button and agent sheet into the app shell (M1)"
```

---

## Self-review notes

- **Spec coverage (M1 as amended):** mic in nav with tap and long-press (Task 9), `VoiceInput` (Task 7), text mode (Tasks 8 and 10), parser for money and questions (Tasks 2–5), `QueryResolver` with deep links (Tasks 6 and 11), answer cards (Task 10), the mic permission path (Tasks 8 and 11), "Didn't catch that" (Task 8), and the 48dp, labelled, live-region requirements (Tasks 9 and 10).
- **Deliberately not in M1:** Approve/`PlanExecutor`, TTS, task and going-away parsing and Clarify-for-tasks (M2); Gemini, `HouseholdSnapshot`, App Check, usage guard (M3); privacy copy and Settings toggle (M4); glass (sub-project 3).
- **Lazy-user default:** "spent/kharcha <amount>" with no subject becomes an "Expense" in category `other` (Task 5). A bare "paid 300" still asks for more detail, since it might be a payment to a person.
- **Parser corpus:** M1 tests have about 80 phrases. The spec's 150-phrase target covers the whole parser and is reached in M2 when task and going-away phrases are added.
- **Known simplification:** the payment direction for "<name> paid me" relies on the word "me" right after the verb. "Ravi paid back me 200" falls through to `NeedsModel`, which is acceptable.
