# HABITIQ DISCOVERY — CURSOR MASTER IMPLEMENTATION SPEC

Drop this into a Cursor session and say: **Read this first. Inspect the codebase. Then implement. Do not build a new app.**

This is an **agent instruction document**, not a product essay. It encodes the current APK reality (August 2026) plus the approved product direction: two-sided Discovery, looking posts, connection state machine, qualitative trust, and **Flat Health** (how the household actually runs).

---

## 01 — AGENT ROLE

You are working on the **existing Habitiq application** at `C:\garbage`.

You are **not** building a new application.

Complete and correct **Discovery** while preserving:

- Authentication
- Flat management / membership / invite join
- Tasks and rotation
- Expenses and settlements
- Existing navigation and design system (`FigmaColors`, existing sheets/buttons)

All Discovery work must be **additive and reversible**. Feature flags live in `apps/android/.../discover/DiscoverFlags.kt`.

Habitiq is not a generic rental marketplace.

Competitive advantage:

> See not only where you can live, but **how that shared home actually operates**.

---

## 02 — READ BEFORE BUILDING

Inspect, then report, **then** code:

```text
Inspect:
- apps/android/app/.../HabitiqApp.kt (navigation)
- ui/AppShell.kt, PlusActionSheet.kt
- ui/DiscoverBoardScreen.kt
- ui/discover/*
- discover/* (flags, filters, ranking, domain, FlatHealth, LocationIntelligence)
- data/DiscoveryRepository.kt, MessagingRepository.kt
- flat/FlatViewModel.kt
- flats/FlatsRepository.kt, MembersRepository.kt
- backend/firebase/firestore.rules
- seekerProfiles, messages, discoveryConnections, discoveryReports
```

Report format:

```text
CURRENT IMPLEMENTATION
What already exists
What is partial
What is broken
What is missing
What can be reused
What must be added
MISSING DATA DEPENDENCIES (if any)
```

**Hard rule:** Do not invent backend data to satisfy UI.

If a field does not exist:

1. Name the field  
2. Say where it should live  
3. Check whether an existing field can support it  
4. Propose the smallest additive schema  
5. Do **not** implement fake filtering or hardcoded people (`Rahul`, `Microsoft`, `94%`)

---

## 03 — THREE SEPARATE CONCEPTS (NEVER MERGE)

| Concept | Question | Allowed presentation |
|---|---|---|
| **Compatibility** | Could this person fit here? | Checkmarks from overlapping real fields |
| **Trust** | What does verified Habitiq history say about a person? | Tier tag: Unrated / New to Habitiq / Habitiq member. **No %.** Habitiq Reliable / Plus **thresholds are not finalized — do not invent them.** |
| **Flat Health** | How does this household operate? | Headline + explainable signals. **Not a leaderboard. Not Flat Score 9.4.** |

Never:

```text
matchScore = trustScore + profileScore + healthScore
```

---

## 04 — INFORMATION ARCHITECTURE

```text
DISCOVER
│
├── Find Flat / PG     (supply: vacancy on flats/{id}.vacancy)
├── Find Flatmate      (demand: seekerProfiles/{uid})
├── Create Post        (vacancy | looking — different types)
├── My Posts
├── Connections inbox
└── Conversation (in-app only)

FIND FLAT DETAIL
├── Overview (approx location, rent, room — never exact address)
├── Flat Health / Success Matrix
├── Current members (published nicknames only)
├── Vacancy
├── People interested (named list = admin only; others see count)
├── Compatibility
├── Trust / verification
└── Connect → conversation → invite/join (join is NOT automatic)
```

Find Flat and Find Flatmate are **different intents**, not two labels on one search.

---

## 05 — DATA THAT EXISTS TODAY (USE THIS)

### Vacancy (supply) — `flats/{flatId}.vacancy`

`active`, `city`, `area`, `rentPerHead`, `currency`, `bedsAvailable`, `preferredGender`, `about`, `lifestyle[]`, `customTags[]`, `updatedAt`, additive `status`.

Parent flat (authenticated read already allowed): `name`, `adminUid`, `memberCount`, `flatType`, additive **`discoveryPublic`** (Flat Health snapshot).

**Never parse or display street address / lat-lng even if a stray field appears.**

### Looking (demand) — `seekerProfiles/{uid}`

`displayName`, `photoUrl`, `city`, `lookingIn`, `budget`, `bio`, `gender`, `lifestyleTags`, `active`, `createdAt`.

### Chat — `messages/{id}`

`senderId`, `receiverId`, `content`, `timestamp`. No phone.

### Connections — `discoveryConnections/{id}`

`fromUid`, `toUid`, `listingFlatId?`, `seekerId?`, `message`, `status`.

### Reports — `discoveryReports/{id}`

A report **must not** change a trust tag.

### Blocks — `users/{uid}/blocked/{targetId}`

### Members / tasks / expenses

Readable **only to flat members**. Discovery seekers **must not** query `flats/{id}/members` or `/tasks`. That is why Flat Health is a **snapshot** written by the admin onto `flats/{id}.discoveryPublic` at publish time.

---

## 06 — MISSING DATA DEPENDENCIES (DO NOT FAKE)

```text
MISSING DATA DEPENDENCY
Fields: company, college, hometown/village, profession, workplace area
Current schema: not on users, seekerProfiles, or members
Do not implement “same company / same college / same village” filters.
Do not show those rows on profiles.

Proposed additive schema (wait for product approval before shipping filters):
seekerProfiles/{uid}.company
seekerProfiles/{uid}.college
seekerProfiles/{uid}.hometown
seekerProfiles/{uid}.profession
```

```text
MISSING DATA DEPENDENCY
Field: dispute outcomes as a first-class collection
Do not show “no unresolved disputes” unless a real dispute model exists.
```

```text
MISSING DATA DEPENDENCY
Habitiq Reliable / Habitiq Plus numeric thresholds
Use only: Unrated (consent off), New to Habitiq, Habitiq member (listing from an existing flat).
```

---

## 07 — FLAT HEALTH RULES

When an **admin publishes or updates a vacancy**, compute qualitative signals from **that flat’s already-loaded** tasks, settlements, expenses, activity, members. Write `discoveryPublic` on the flat document (admin can update the flat doc).

Allowed headlines (examples of language, not scores):

- Well maintained
- Actively maintained
- New household
- Limited history

Allowed signal labels:

- Tasks: Consistently completed | Some tasks overdue | Not enough history
- Expenses: Regularly settled | Expenses are recorded | Not enough history
- Rotation: Active | Not enough history
- Activity: Active | Quiet lately
- Members: Shared household | Small household

**Forbidden:** 94% on time, 9.4/10, ranking Flat A vs Flat B, using `reliabilityScore` on Discovery.

If `discoveryPublic` is absent: show **Limited history**, never invented percentages.

Current members on Discovery: **nicknames + role** from the snapshot. No emails, no phone, no reliability numbers, no member UIDs required on the public snapshot.

People interested:

- **Admin / own listing:** connection requesters with profile name, compatibility from existing seeker fields, connect state.
- **Everyone else:** count only (“3 people have asked to connect”). Names stay private.

Matching **does not** join the flat. Invite code / join requests remain the membership path.

---

## 08 — CONNECTION STATE MACHINE (REAL DOCS, NOT UI-ONLY)

```text
NONE → REQUEST_SENT → ACCEPTED → CONVERSATION_OPEN
Also: DECLINED, BLOCKED, EXPIRED
```

- Connect sends `discoveryConnections` with a short message.
- Do not open chat until ACCEPTED (grandfather existing `messages` threads).
- Do not put WhatsApp / phone / email on profiles.
- After accept, conversation header must show **why** they are talking (flat name / looking context).
- Safety banner on chat. Report + Block on detail and chat.

---

## 09 — POSTING

`+` on Discover tab → Create Discovery post (not Add Task).

- **Vacancy:** admin of current flat only. Writes `vacancy` + `discoveryPublic`. Keep `active` for web compatibility.
- **Looking:** `DiscoverFlags.LOOKING_POSTS`. Writes `seekerProfiles/{uid}`.

Lifecycle: keep `active` boolean **and** additive `status` (`PUBLISHED`, `PAUSED`, `CLOSED`, …). Do not replace web `active` with status-only.

---

## 10 — SAFETY / PRIVACY

- Approximate location only (city + area).
- In-app communication until mutual accept.
- Report reasons: fake listing, harassment, scam/payment, fake identity, inappropriate, other.
- Report ≠ automatic trust penalty.
- New users are **New to Habitiq** (neutral), never “Low trust”.
- Consent prompt before using activity for person-level trust tags. Opt-out = **Unrated**.
- Admin publishing a vacancy is the consent to snapshot **qualitative** household signals.

---

## 11 — FILTERS (ONLY WIRED FIELDS)

**Find Flat:** city/area search, rent min/max, gender preference, flat type, room type (from `bedsAvailable`), listed-within (`updatedAt`), lifestyle tags.

**Find Flatmate:** city/area, budget min/max, gender, lifestyle tags.

Do not add company/college/village filters until those fields exist.

Ranking: explainable sort (location → budget → room → tag overlap). Trust is a tiny tie-break, never an exclusion.

Persist search/filter state when opening a detail and going back.

---

## 12 — UI / DESIGN SYSTEM

Reuse `FigmaColors`, existing buttons, sheets, chips, typography. Do not create a second Discovery design system.

Every Discovery surface needs, where applicable:

Default, loading, empty, error, success, disabled, permission-denied (e.g. non-admin vacancy post), retry.

---

## 13 — FIREBASE

Rules file: `backend/firebase/firestore.rules`.

Additive collections already specified:

- `discoveryConnections` — participants read; creator creates REQUEST_SENT; participants update `status` + `updatedAt` only
- `discoveryReports` — reporter create/read own
- `users/{uid}/blocked/{targetId}` — owner only
- `flats/{id}` remains readable by any authenticated user (needed for invite codes **and** vacancy + `discoveryPublic`)
- `flats/{id}/members` and `/tasks` stay **member-only**

After rules changes: `firebase deploy --only firestore:rules`

Do not loosen member/task reads “to make Discovery easier.”

---

## 14 — WORKING STYLE

- Small verifiable units.
- Do not refactor unrelated screens.
- Do not replace backend with mocks in production UI.
- After each unit: compile (`apps/android/gradlew.bat :app:compileDebugKotlin`) and report:

```text
What changed → What was tested → What passed → What remains
```

Do not declare complete until success / empty / loading / error exist for that unit.

---

## 15 — ACCEPTANCE (DISCOVERY E2E)

- [ ] Find Flat and Find Flatmate feel like different searches
- [ ] Vacancy card → detail shows health, members, vacancy, compatibility, trust, connect
- [ ] Health has no numeric score and no inter-flat ranking
- [ ] Non-admin cannot publish vacancy; looking post uses seekerProfiles
- [ ] Connect hides phone; chat after accept; report/block work
- [ ] Admin sees named interested people; others see count
- [ ] Join still uses invite/join — match does not auto-join
- [ ] Tasks/expenses/settlements still work
- [ ] Company/college not shown as if they existed
- [ ] Android back from detail restores filters
- [ ] No Discovery commute km, map widget, or “near my office” ranking in this APK

---

## 16 — FUTURE: DISTANCE & COMMUTE INTELLIGENCE (PHASE 2 — DO NOT BUILD NOW)

Google Maps, Google Places, Maps SDK, geocoding APIs and routing APIs are **NOT part of the current Discovery implementation**.

Do **not** add these dependencies for Discovery now.

Do **not** implement placeholder maps or fake `~4.2 km` / `~18 min` copy to make the UI look complete.

Create-flat already has a map picker for **city/area text**. Do not copy that rooftop pin, `pincode`, or `landmark` into Discovery listings. Those are not neighbourhood-safe Discovery coordinates.

### Reserved additive schema (parse if present; do not require; do not destroy)

On the **flat document** (sibling of `vacancy`, not a replacement):

```text
discoveryApproxLocation: {
  city, area,
  areaPlaceId?,
  approxLat?, approxLng?,
  precision: "neighborhood"   // required. Reject any other precision.
}
```

On **seekerProfiles/{uid}** (commute anchors — workplace/college, never home address):

```text
commuteAnchors: {
  workplace: { label, placeId?, lat?, lng? },
  college:   { label, placeId?, lat?, lng? }
}
```

Flag: `DiscoverFlags.DISTANCE_INTELLIGENCE = false`.

Seeker profile writes use **merge** so later `commuteAnchors` are not wiped by bio edits.

Vacancy republish must **not** delete `discoveryApproxLocation` if the current wizard does not send it.

When Phase 2 ships: distance from workplace/college/saved place, commute estimate, “near my office”, distance sort — using **approximate neighbourhood** vs commute anchors, never exact residential address.

Code: `discover/LocationIntelligence.kt`

---

## 17 — CODE MAP (CURRENT)

| Area | Path |
|---|---|
| Flags | `discover/DiscoverFlags.kt` |
| Domain / trust / connections | `discover/DiscoverDomain.kt` |
| Compatibility / ranking | `discover/Compatibility.kt`, `DiscoverRanking.kt` |
| Flat Health | `discover/FlatHealth.kt` |
| Location (Phase 2 reserved) | `discover/LocationIntelligence.kt` |
| Filters | `discover/DiscoverFilters.kt` |
| Repo | `data/DiscoveryRepository.kt` |
| Shell | `ui/DiscoverBoardScreen.kt` |
| Find Flat UI | `ui/discover/UseAFlatDiscover.kt` |
| Find Flatmate UI | `ui/discover/FindFlatmateDiscover.kt` |
| Flat detail | `ui/discover/FlatListingDetailScreen.kt` |
| Post / my posts | `CreateDiscoveryPostScreen.kt`, `MyPostsScreen.kt` |
| Safety sheets | `ConnectionSafetySheets.kt` |
| Rules | `backend/firebase/firestore.rules` |

This is the document Cursor should obey. Product “why” lives in `DISCOVERY_TRUST_LEGAL_SAFETY.md` and `DISCOVERY_WORKING_SPEC.md`.
