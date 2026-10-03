# Oddroof Agent — Design Spec

- **Date:** 2026-10-03
- **Status:** Approved; M1 implemented on branch `feat/agent-m1` (2026-10-03)
- **Scope:** Sub-projects 1 + 2 of the agent initiative (agent core + agent UI, with glass on agent surfaces only)
- **Platform:** Android native app, `apps/android` (package `habitiq.app`), Firebase project `garbage-f79f7`

## 1. Intent

Make the microphone the primary way to run a shared home. A flatmate says (or types) what happened or what they need — "Bought groceries for ₹560", "I'm going home tomorrow till Sunday", "What do I owe?", "Flats in Gachibowli under 10k" — and the agent understands it, checks the household, and shows a plan or an answer. Nothing changes until the user taps **Approve**.

Principles (from Sai's research, agreed):

- **One agent, many inputs:** voice, text and touch reach the same agent; existing screens stay.
- **Not a chat app:** the agent is woven into the home product as a sheet over the current screen, never a full-screen chat.
- **Speak → Understand → Check → Plan → Approve → Done** is the signature interaction.
- **Credit-efficient and technically lean:** most requests cost zero AI tokens.

### Decisions locked

| Decision | Choice |
|---|---|
| AI brain | **Gemini via Firebase AI Logic**, billed to Firebase credits. Claude is never an app runtime dependency. |
| Architecture | **Approach A — local-first:** on-device speech + local parser; Gemini only for requests the parser cannot resolve. |
| Languages | English + Indian code-mix (Hinglish/Tenglish words), speech locale `en-IN`. |
| V1 skills | Money (add/split expense, settle), Tasks (done, swap, create), Going away, Questions (incl. Discover flat search). |
| Writes | Only through existing `FlatViewModel` / repository functions, after explicit approval. |

### Out of scope (later sub-projects)

- **Sub-project 3:** glass across the whole app (cards, sheets, screens beyond the agent surfaces).
- **Sub-project 4:** proactive suggestions and Google Calendar integration.
- Destructive actions via the agent (delete expense/task/flat, kick member, leave flat).
- Full Telugu/Hindi-script conversations.

## 2. Architecture

```
 mic / text
    │  VoiceInput (Android SpeechRecognizer, en-IN, on-device where available)
    ▼
 LocalIntentParser ── confident ──▶ AgentPlan ──▶ PlanValidator ──▶ Plan / Answer UI
    │ not confident                    ▲                                   │ Approve
    ▼                                  │                                   ▼
 HouseholdSnapshot ─▶ GeminiPlanner ───┘                     PlanExecutor → FlatViewModel / DiscoveryRepository
                       (1 call, JSON schema)                 QueryResolver → live app state (answers)
```

New package: `habitiq.app.agent` (logic, no Compose) and `habitiq.app.ui.agent` (Compose UI).

### 2.1 Units

Each unit has one purpose, a small interface, and is testable alone.

| Unit | Purpose | Depends on |
|---|---|---|
| `VoiceInput` | Wraps `SpeechRecognizer`. Emits `Partial(text)`, `Final(text)`, `Level(rms)`, `Error(kind)`. Starts/stops on request. | Android framework |
| `LocalIntentParser` | Pure Kotlin. `parse(text, snapshot): ParseResult` → `Confident(AgentPlan)` or `NeedsModel`. | `HouseholdSnapshot` (for name/task matching) |
| `HouseholdSnapshot` | Builds a compact, PII-free view of the current flat for parsing and for the model prompt. | `FlatViewModel` state flows |
| `GeminiPlanner` | One Firebase AI Logic call per request with a strict response schema. Returns `AgentPlan` or a typed failure. | `firebase-ai`, App Check |
| `AgentPlan` | Sealed data model shared by parser, planner, validator, UI and executor. | — |
| `PlanValidator` | Drops or flags any step that references unknown ids, bad amounts/dates, or actions the user's role can't perform. | `HouseholdSnapshot` |
| `PlanExecutor` | Runs approved steps sequentially through existing ViewModel/repository functions; reports per-step result. | `FlatViewModel`, `DiscoveryRepository` |
| `QueryResolver` | Turns a `Query` into an answer card from live data. Numbers never come from the model. | `FlatViewModel`, `DiscoveryRepository` |
| `AgentUsageGuard` | Per-user, per-day Gemini call counter (DataStore). | `AppPreferences` |
| `AgentViewModel` | State machine driving the UI (section 3.2). Orchestrates all of the above. | All above |

### 2.2 `AgentPlan` model

```kotlin
sealed interface AgentPlan {
    data class Actions(val steps: List<AgentStep>, val summary: String) : AgentPlan
    data class Answer(val query: AgentQuery) : AgentPlan
    data class Clarify(val question: String, val options: List<ClarifyOption>) : AgentPlan
    data class Unsupported(val reason: String) : AgentPlan
}

sealed interface AgentStep {
    data class AddExpense(val title: String, val amountPaise: Long, val category: String,
                          val paidByUid: String, val splitAmongUids: List<String>) : AgentStep
    data class Settle(val fromUid: String, val toUid: String, val amountPaise: Long) : AgentStep
    data class CompleteTask(val taskId: String) : AgentStep
    data class SwapTask(val taskId: String, val withUid: String) : AgentStep
    data class CreateTask(val name: String, val frequency: String, val queueUids: List<String>,
                          val startDate: LocalDate?) : AgentStep          // admin only
    data class GoingAway(val from: LocalDate, val until: LocalDate,
                         val handoffTaskIds: List<String>) : AgentStep    // out-of-station + handoff requests
}

sealed interface AgentQuery {
    data object MyBalance : AgentQuery
    data object WhoOwes : AgentQuery
    data object DueToday : AgentQuery
    data class MyDuties(val from: LocalDate, val until: LocalDate) : AgentQuery
    data object Bills : AgentQuery
    data class FindFlats(val area: String?, val city: String?, val maxRent: Long?, val gender: String?) : AgentQuery
}
```

Amounts are integer paise inside the agent to avoid float drift; converted at the executor boundary to whatever the existing functions take.

### 2.3 `LocalIntentParser`

Deterministic, rule-based. Returns `Confident` only when every required slot is filled unambiguously; otherwise `NeedsModel`.

- **Amounts:** `₹560`, `560`, `560 rs`, `rs. 560`, `5.6k`, `1.2 lakh`, `five hundred` (common number words).
- **Expense cues:** bought, paid for, spent, kharcha, kharchu, kirana, groceries, milk, gas, electricity, wifi, rent, swiggy/zomato; default split = all current members, payer = me.
- **Settle cues:** "paid <name> <amount>", "gave <name>", "<name> paid me".
- **Task cues:** "done with / finished / completed / ayipoyindi / ho gaya <task>"; "swap my <task> with <name>"; "add <task> <frequency>" (admin).
- **Going-away cues:** going home, out of station, travelling, not here, "from <date> till/to/until <date>".
- **Questions:** what do I owe / how much do I owe / my balance; who owes / who hasn't paid; what's due today / my tasks; bills; flats/rooms in <area> under <amount>.
- **Dates:** today, tomorrow, day after, weekday names, "this weekend", "next week", `d MMM`, "till <weekday>".
- **Matching:** member first names and task names via normalised lowercase + edit distance ≤ 1 for names ≥ 4 chars. Two or more matches → `Clarify` (no model call).

### 2.4 `HouseholdSnapshot`

Built from current ViewModel state; never fetched separately.

Includes: flat city/area, my uid and role, members as `{shortId, firstName}`, open tasks `{id, name, assigneeShortId, dueDate, frequency}` for the next 14 days, my net balance and per-member net balances (rounded rupees), recurring bill names with due day, today's date and weekday.

Excludes, always: emails, phone numbers, photo URLs, full names beyond first name, message content, invite codes, coordinates.

Hard cap: serialised prompt context ≤ 8,000 characters (~2k tokens); truncate tasks farthest in the future first.

### 2.5 `GeminiPlanner`

- SDK: `com.google.firebase:firebase-ai` (Firebase BoM), backend `GenerativeBackend.googleAI()`.
- Model: latest Flash-Lite available in Firebase AI Logic at build time (`gemini-2.5-flash-lite` as of writing), read from a constant overridable by Remote Config key `agent_model` so it can be swapped without an APK.
- `generationConfig`: `responseMimeType = "application/json"`, `responseSchema` mirroring `AgentPlan` (steps as a discriminated `type` field), `temperature = 0.1`, `maxOutputTokens = 512`.
- System instruction: role, allowed actions only, use only ids from the snapshot, ask a clarify question rather than guess, Indian English/code-mix understanding, never invent amounts.
- One call per user request. One automatic retry on timeout/5xx; then a typed failure.
- Protected by **Firebase App Check** (Play Integrity provider in release, debug provider in debug builds).

### 2.6 `PlanValidator`

Applied to every plan from either source. A step is dropped and reported in a "couldn't do" line when:

- any uid/taskId is not in the snapshot;
- amount < ₹1 or > ₹1,00,000, or split list empty;
- dates in the past for `GoingAway`/`CreateTask`, or `until` before `from`, or range > 60 days;
- `CreateTask` and the user is not admin;
- `SwapTask`/`CompleteTask` on a task not assigned to the user (complete) or not theirs to swap.

If all steps are dropped → `Unsupported` with the reasons.

### 2.7 `PlanExecutor` mapping

| Step | Existing function |
|---|---|
| `AddExpense` | `FlatViewModel.addExpense` |
| `Settle` | `FlatViewModel.recordSettlement` / `recordManualSettlement` |
| `CompleteTask` | `FlatViewModel.completeTask` |
| `SwapTask` | `FlatViewModel.createSwapRequest` |
| `CreateTask` | `FlatViewModel.createTask` |
| `GoingAway` | `FlatViewModel.toggleOutOfStation` + `sendGoingAwayRequests` |

Steps run sequentially; a failure stops later steps and the Done state shows which succeeded. No new Firestore write paths; Firestore rules remain the final authority.

### 2.8 `QueryResolver`

| Query | Source |
|---|---|
| `MyBalance`, `WhoOwes` | `computeNetBalances` |
| `DueToday`, `MyDuties` | tasks flow |
| `Bills` | bill instances flow |
| `FindFlats` | `DiscoveryRepository` vacancy listing filtered by area/city/rent/gender (top 5) |

Each answer card has a deep-link button to the relevant screen.

## 3. Experience

### 3.1 Entry point

- The centre bottom-nav button becomes the **agent orb**. **Tap** = open agent sheet and start listening. **Long-press** = existing `QuickAddOverlay` (unchanged).
- The agent sheet is a glass bottom sheet over the current screen. A keyboard icon switches to text input; the sheet remembers the last mode.

### 3.2 State machine (`AgentViewModel`)

```
Idle ─tap mic─▶ Listening ─final text─▶ Understanding ─▶ Checking ─▶ Plan ──Approve──▶ Executing ─▶ Done
  ▲                │ error/empty           │ (skipped when parser confident)  │ Cancel           │
  └────────────────┴───────────────────────┴──────────── Clarify ◀────────────┘                  │
                                             └────────▶ Answer ──────────────────────────────────┘
```

| State | UI |
|---|---|
| Idle | "What can I take care of?" + 3 contextual chips (e.g. a task due today, my balance, "Add an expense"). |
| Listening | Orb pulses with mic level; live partial transcript. |
| Understanding | Orb gathers inward. Shown only when Gemini is called. |
| Checking | 2–3 ticks ("Your duties ✓ · Balances ✓"), ≥ 400 ms so it reads. |
| Plan | One card per step with editable fields (amount, people, date). Approve / Cancel. |
| Clarify | Question + option chips ("Which Ravi?"). Choosing re-enters Plan without another model call. |
| Answer | Answer card with live numbers + deep-link button. |
| Executing → Done | Progress per step → ✓ animation, success haptic, one-line summary. |
| Error | Plain message + Try again; transcript kept in the text field. |

### 3.3 Voice reply

Android `TextToSpeech`, `en-IN`. Speaks the one-line Done/Answer summary **only when the request came by voice**. Settings toggle "Speak replies" (default on).

### 3.4 Glass (agent surfaces only)

> Deferred to sub-project 3 (decision 2026-10-03). M1 uses a plain mic button and the standard sheet; the agent sheet gets its life from the listening aura, live transcript and result cards instead.

- Library: Haze (`dev.chrisbanes.haze`) for real backdrop blur on API 31+; fallback on < 31 is a translucent surface tint (`surfaceBase` at ~88% alpha) with a hairline border.
- Applied to: agent sheet, bottom navigation bar, top app bar.
- Orb: teal→coral radial glow from existing brand tokens (`brandTeal`, `brandCoral`); morphs between states via shape/scale/alpha only (transform + opacity).
- New tokens added to `HqColorTokens`: `glassTint`, `glassBorder`, `glassHighlight` for light and dark.

### 3.5 Accessibility & standards

48dp minimum targets; every state has a content description and is announced via live region; respects system reduce-motion (static orb, no pulse); light and dark verified; mic button labelled "Talk to Oddroof".

## 4. Errors, limits, privacy

| Situation | Behaviour |
|---|---|
| Mic permission denied | Sheet opens in text mode with a one-line reason and "Allow mic" button. |
| No speech / recogniser error | "Didn't catch that. Try again or type it." |
| Offline | Parser still works; model-needed requests show "Needs internet." |
| Daily Gemini limit (30/user/day) reached | Parser-only; "Smart requests are paused until tomorrow. Simple commands still work." |
| Model timeout after retry | "Couldn't reach the assistant. Try again." Transcript kept. |
| Model returns invalid JSON / unknown action | Treated as `Unsupported`; logged to Crashlytics as non-fatal with no transcript content. |
| Executor step fails | Stop remaining steps; Done shows per-step ✓/✕ with the error message. |

Privacy:

- Mic permission requested on first orb tap with rationale. Audio is processed by Android's recogniser and never uploaded or stored by Oddroof.
- Only the transcript text and the PII-free snapshot are sent to Gemini, and only for parser misses.
- Transcripts are not persisted beyond the current sheet session.
- Privacy Policy gets a "Voice & AI assistant" section; Play data-safety form updated (audio: processed on device, not collected; app activity text: sent to Google for processing, not stored by Oddroof).

### 4.1 Acceptable use and misuse guardrails (must be done before public launch)

Everything the agent records lands in data the whole flat can see, so misuse affects other people, not just the requester. Added 2026-10-03 at Sai's request: "mistakes should not be done… fix before the launching".

| Layer | Guardrail | Milestone |
|---|---|---|
| Input gate | `ContentGuard` refuses requests about illegal goods/services (drugs, weapons, bribes, betting), abuse, and threats, before parsing, with one calm line: "I can't help with that one. Oddroof is for shared home stuff like groceries, bills and chores." Whole-word matching so household words ("weeding", "coke") pass. Sai maintains the romanised Hindi/Telugu abuse list. | M1 |
| Scope gate | The agent can only produce the fixed action types in §2.2. It can't send messages, post to Discover, change roles, or delete anything (§1, out of scope). | M1–M2 |
| Validation | `PlanValidator` (§2.6): amount ₹1–₹1,00,000, known ids only, role checks; Firestore rules remain the final authority. | M2 |
| Approval | Nothing is written without the user tapping **Approve** on the card. | M2 |
| Traceability | Agent-created records keep `createdBy = <uid>` (existing) and add `source: "agent"`, so a flatmate or admin can see who recorded what and how. Edits and deletes go through existing activity logging. | M2 |
| Output gate | `ContentGuard` also runs on titles produced by Gemini before they're shown or saved. | M3 |
| Model safety | Gemini `safetySettings` set to block medium-and-above for harassment, hate speech, sexually explicit and dangerous content; the system instruction tells it to refuse out-of-scope or harmful requests and return `Unsupported`. | M3 |
| Abuse limits | `AgentUsageGuard`: 30 model calls/user/day (§4); plus at most 20 agent-created records/user/hour, enforced in the client and by a Firestore rule rate check. | M2–M3 |
| Reporting | Flatmates can already report and block users (`reportUser`, `blockUser`); agent-created records are covered by the same path. | existing |
| Legal | Terms of Service: an "Acceptable use" clause (no illegal, abusive or harassing content; the user is responsible for what they record; Oddroof may remove content and suspend accounts). An "AI assistant" clause: suggestions may be wrong, the user confirms every action, money records aren't financial or legal advice. The Privacy Policy "Voice & AI assistant" section (§4). | M4, before launch |
| Launch check | Misuse corpus test (≥ 40 phrases: illegal, abusive, threats, plus look-alike household phrases that must pass) runs green; manual red-team pass by Sai in English, Hinglish and Tenglish. | M4 |

## 5. Testing

| Unit | Tests |
|---|---|
| `LocalIntentParser` | Table-driven corpus of ≥ 150 phrases (English, Hinglish, Tenglish, amounts, dates, names, ambiguity) → expected `ParseResult`. Target: 0 wrong `Confident` results; `NeedsModel` allowed. |
| `HouseholdSnapshot` | Never contains emails/phones/photo URLs; stays ≤ 8,000 chars with a large flat. |
| `PlanValidator` | Unknown ids, bad amounts, past/oversized date ranges, non-admin `CreateTask` are all rejected. |
| `GeminiPlanner` | Parses recorded JSON fixtures (valid, partial, malformed); no live calls in unit tests. |
| `PlanExecutor` | Fake ViewModel port: correct function per step, stops on first failure. |
| `AgentViewModel` | State transitions for each path (parser-confident, model, clarify, answer, error, limit). |
| Device | Sai installs APKs per milestone and reports via screenshots. |

## 6. Delivery milestones

Each milestone ends with a building APK and green unit tests.

1. **Agent shell:** plain mic button in the nav centre (tap = agent, long-press = Quick add), agent in a regular `HqBottomSheet` with a voice-reactive listening aura, live transcript, and result cards (expense receipt card, payment card, answer card), `VoiceInput`, text mode, `LocalIntentParser` for money + questions (incl. lazy "I just spent 500"), `ContentGuard`, `QueryResolver`, read-only previews for money commands. Zero AI cost. *(Changed 2026-10-03: no glass and no orb in M1 — Sai wants the app to stay a regular app; the agent sheet is where it feels alive. All glass moves to sub-project 3.)*
2. **Plans & actions:** Plan cards with editing, Approve → `PlanExecutor`, Done/haptics/TTS, task + going-away parsing, Clarify flow.
3. **Gemini:** `HouseholdSnapshot`, `GeminiPlanner` with schema, App Check, `AgentUsageGuard`, Remote Config model key, `FindFlats` via model.
4. **Compliance & polish:** Privacy Policy section (web `apps/web/app/privacy`), Terms "Acceptable use" + "AI assistant" clauses (§4.1), misuse corpus + red-team pass, data-safety notes, Settings toggle, accessibility pass.

### One-time console setup (Sai, at milestone 3)

- Firebase console → AI Logic → enable **Gemini Developer API**.
- Firebase console → App Check → register the Android app with **Play Integrity**; add the debug token printed by the debug build.

## 7. Dependencies added

- `com.google.firebase:firebase-ai` (via existing Firebase BoM)
- `com.google.firebase:firebase-appcheck-playintegrity`, `firebase-appcheck-debug` (debug only)
- `com.google.firebase:firebase-config` (model key)
- `dev.chrisbanes.haze:haze`
- Permission: `android.permission.RECORD_AUDIO`
