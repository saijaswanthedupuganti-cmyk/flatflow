# HABITIQ — ONBOARDING MASTER SPEC

Drop into Cursor: **Audit auth, user docs, `activeFlatId`, create-flat, invite join, and routing before changing UI. Preserve working Firebase logic. Only change presentation and flow. If Firebase behavior conflicts with this spec, report the conflict — do not silently rewrite it.**

This is both a **UX spec** and a **Firebase state/routing spec**. Onboarding is where those meet.

> Do not ask the user to understand Habitiq before they can use it. Ask **what they are here to do**, then route.

Success is not “we collected every profile field.” Success is: **the user reached the right place with the minimum necessary effort** (flat running in under two minutes).

---

## 00 — HARD RULES

1. **Authenticated ≠ has a flat.** A user document can exist with `activeFlatId == null`. That is valid.
2. Route from **Firestore account state**, not leftover Compose navigation.
3. Do **not** invent schema: `company`, `college`, `hometown`, `profession` are **not** on `users` or `seekerProfiles` today (see Discovery spec). Report the gap; do not fake onboarding fields.
4. Do **not** invent task templates. Roadmap lists templates as later. After create-flat, send them to Home / Manage Flat.
5. Do **not** add **I’m just exploring** unless product explicitly allows Discovery **without** a flat. Current product: Find a flatmate **creates a flat first**, with an explanation of why.
6. Do **not** force invite N people or create tasks before Home.
7. Preserve: Google + email/password, `ensureUserDocument`, `createFlat` invite id, `joinFlat` / `requestToJoin`, leave-flat `activeFlatId` rewrite, max 8 members.
8. Friendly errors only — never raw Firebase codes (already `mapFlatError` / `mapAuthError`).
9. Double-tap Create must not create two flats. App killed mid-onboarding must resume from Firebase (`activeFlatId` present → Home).

---

## 01 — AUDIT (23 Aug 2026) — WHAT ALREADY WORKS

Inspected live APK routing. Do not assume a blank slate.

### Auth → user doc

`completeAuthFlow` (`AuthCompletion.kt`):

```text
Firebase Auth success
  → ensureUserDocument (email + displayName only; no activeFlatId)
  → getActiveFlatId
  → AuthUiState.Success(hasActiveFlat)
```

Login / Signup then `navigateAfterAuth(hasActiveFlat)` → `MAIN` or `INTENT_CHOOSER`.

Cold start (`HabitiqApp` `LaunchedEffect`): same `getActiveFlatId` check.

**Gap:** if `getActiveFlatId` **fails** (network), `getOrNull()` is null → user is sent to onboarding as if they had no flat. Treat failure as **retry**, not “no flat.”

### User model (do not extend for this spec)

`users/{uid}`: `email`, `displayName`, `activeFlatId`, `flatIds[]`.

No company / college / hometown / profession.

`seekerProfiles/{uid}`: city, lookingIn, budget, bio, gender, lifestyleTags, active. Still no company/college/hometown.

### Create flat

`FlatsRepository.createFlat` writes `flats/{id}`, admin membership, merge `activeFlatId` + `flatIds`. Invite code **is** the document id.

Wizard: name/type → optional location → success with share. **Household size is not a backend field — do not collect it.**

**Gap:** `createFlat()` does not ignore a second tap while `Loading`. Guard it.

**Gap:** if the user already has `activeFlatId`, accidental onboarding must **not** create another flat.

### Join

`joinFlat` transaction: exists / already member / memberCount ≥ 8 / write member + `activeFlatId`.

`joinMode == "approval"` → `requestToJoin` (pending; **no** `activeFlatId` until admin accepts). This is a real fourth state — keep it.

UI today: type code → Join immediately. Spec wants **lookup → preview → Join**. Use existing `getFlat`; do not change the transaction.

### Intent chooser today (wrong question)

Four cards: Find a flat / Find the person / Create a room / Join a flat.

**Find a flat / Find the person currently navigate to MAIN without a flat.** Home then shows Create/Join. That is exploring-by-accident, not the three-intent model.

Discovery *can* list vacancies without membership (`observeActiveVacancies` binds on auth), but **product direction is: find a person → create a flat first.** Do not ship a fourth “explore” CTA.

### Multi-flat / leave

`leaveFlat` sets `activeFlatId` to the next `flatIds` entry or null. Profile `onNoFlatRemaining` → intent chooser. Keep that.

### Task templates

**Not implemented.** After create: invite skippable → Home. First task lives in Manage Flat.

---

## 02 — STATE MACHINE (SOURCE OF TRUTH)

```text
UNAUTHENTICATED → Login / Signup
AUTHENTICATED + loading profile → spinner (retry on failure)
AUTHENTICATED + activeFlatId set → HOME (MAIN)
AUTHENTICATED + no activeFlatId + no pending-only wait → ONBOARDING INTENT
AUTHENTICATED + pending join request (approval flats) → stay on Join success copy; still no Home
AUTHENTICATED + multiple flatIds → HOME for activeFlatId; switcher for others
```

Never show a blank dashboard. If MAIN loads `NoFlat`, send the user back to the intent chooser (recovery), do not invent a second Create/Join dialect.

---

## 03 — FIRST SCREEN (REPLACE CREATE VS JOIN)

**What brings you to Habitiq?**

Supporting: *Choose what you want to do first. You can change this later.*

Three cards only:

| Intent | Copy | Routes to |
|---|---|---|
| Manage my flat | Organise tasks, expenses and everyday flat life. | Create Flat (existing wizard) → invite skippable → Home |
| Find a flatmate | Find people and flats that fit you. | Explain why a flat is needed → Create Flat → MAIN Discover (Find a person) |
| Join an existing flat | Already have an invite code? Join your flat. | Join (lookup → preview → join/request) → Home |

No carousel. No “Step 1 of 7.” No giant illustration. Visual: HabitiqBrand (deep canvas, violet, Inter, large cards).

Sign out remains a quiet action.

---

## 04 — FIND A FLATMATE (WHY CREATE)

Do **not** dump a Microsoft/college form.

Screen before create:

> **Create your flat first**
>
> Your flat gives people context about who they’re joining and how the household works.

Then the existing create-flat wizard (city/area already collected there — enough for Discovery later).

After create: land on **Discover / Find a person**. Vacancy vs looking posts stay behind existing Discover flags and admin rules. Do not auto-publish a vacancy.

**Schema conflict (report, do not implement):** matching on company / college / hometown / profession requires new fields. Decide later; not this onboarding.

---

## 05 — MANAGE MY FLAT

Existing create wizard is the source of truth (name, type, optional location). Do not add “how many people” unless a field exists.

Success:

- Invite code = flat id
- Share
- **I’ll do this later** → Home (Manage Flat for first task)

Do not insert a fake template checklist.

---

## 06 — JOIN

```text
Normalize uppercase/trim
 → getFlat (preview: name, memberCount, approval vs auto)
 → Join / Request to join  (existing joinFlat / requestToJoin)
```

Copy:

| Case | User sees |
|---|---|
| Valid auto | Preview + Join Flat |
| Valid approval | Preview + request copy |
| Not found | We couldn’t find that flat. Check the code and try again. |
| Full | This flat is full. Ask the admin for help. |
| Already member | You’re already a member of this flat. |
| Network | Couldn’t check the code. Try again. |

Pending approval: do **not** send to Home.

---

## 07 — IMPLEMENTATION ORDER

1. Spec + audit (this file)
2. Intent chooser (3 intents + find-flatmate explanation)
3. Startup: retry on profile/flat-id load failure
4. Guard create (in-flight + existing `activeFlatId`)
5. Join lookup/preview UI wrapping existing join
6. After-create routing (manage → Home; find-flatmate → Discover)
7. MAIN `NoFlat` recovery → intent chooser
8. Compile APK

Do not rebuild Login. Small tagline tweaks are optional.

---

## 08 — ACCEPTANCE

- New Google/email user with no flat → intent screen, never Home
- Existing user with `activeFlatId` → Home, never intent
- Manage my flat → create → skip invite → Home
- Find a flatmate → explanation → create → Discover
- Join valid code → Home; invalid stays on Join
- Approval join → pending message, still no dashboard
- Kill app after create → reopen Home
- Kill app before create → intent, not blank Main
- Double tap create → one flat
- Leave last flat → intent again
- Leave one of many → other flat Home
- No company/college fields collected
- No task template factory
- No “I’m exploring” CTA
