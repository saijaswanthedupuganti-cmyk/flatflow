# Habitiq — Project Documentation

> **Product:** Habitiq
> **Version:** v0.5.0 (Voice Assistant + Member Bills + Flat Board + Landing Redesign)
> **Project folder:** C:\garbage
> **Live URL:** https://habitiq.app
> **Repo:** github.com/saijaswanthedupuganti-cmyk/flatflow
> **Domain:** habitiq.app (canonical — live as of 17 June 2026)
> **Status:** Live — Active Trial with Real Users
> **Last Updated:** 8 August 2026 (Foundation lock)
> **Founder:** Venkata Sai Jaswanth E (UI/UX) · **Co-founder:** Upputuri Bhanu Kalyan (Full-Stack)

See also: [[About Sai]] · [[FOUNDATION]] · [[DOC_MAP]]

---

## 1. What Is Habitiq

Shared living management platform that automates household duty rotation among flatmates. Eliminates arguments, forgotten tasks, and unfair workloads by giving every flat a transparent, real-time system for chore management.

### The Core Problem

| Problem | Without Habitiq | With Habitiq |
|---------|-----------------|--------------|
| Who cleans next? | Arguments every week | Auto-assigned, no discussion |
| Someone is travelling | Dumped on others | Smart skip — resumes automatically |
| "I forgot" | No accountability | Overdue tracking + reliability score |
| Admin burden | One person does everything | All members self-manage |
| Swap needed | WhatsApp chaos | In-app swap request + accept/decline |
| Who paid the bills? | WhatsApp, memory | Member-submitted bills + collection tracking |
| Finding a flat | Browse random listings | Flat Board with lifestyle compatibility |

### Existing Solutions and Why They Fail

| Method | Why It Fails |
|--------|-------------|
| WhatsApp group | Messages buried, no accountability, no history |
| Whiteboard | Not visible when out, gets erased, no audit trail |
| Verbal agreement | No record, breaks down immediately |
| Excel / Sheets | Requires maintenance, no notifications |

**Market:** India has ~10 million people in shared accommodation in Tier 1/2 cities. No dominant product exists. Habitiq fills that gap.

---

## 2. Brand & Identity

### Name: Habitiq = Habit + IQ
- **Habitat** — where you live, the shared space
- **Habit** — recurring routines of shared living that repeat and rotate
- **IQ** — the intelligence that skips absent members, reassigns overdues, tracks reliability

"The intelligent system for the habits of shared living." Name scales beyond flats — hostels, co-living, villas, offices. Never needs to change.

### Taglines
- **Primary:** "Smart living, managed."
- "No more 'whose turn is it?'"
- "Your shared space, on autopilot."
- "Fair duties. Zero arguments."
- **New (June 26):** "Find your flat. Run it without drama."

### Brand Voice
Direct and confident. Conversational — speaks like a flatmate. Calm, not hype. Specific over vague.

**Sounds like:** "No arguments. No forgotten tasks. Just a fair system that runs itself."
**Does NOT sound like:** "Leverage synergies to optimise household workflows." ✗

### Visual Identity
- Primary colour: Violet/Indigo — #7c3aed
- Background: Deep warm-dark #0c0b0f (dark canvas with violet tint)
- Logo: New app icon (habitiq-app-icon.png) — used in navbar, mobile menu, footer
- Font: Inter (font-feature-settings: ss03 for friendly digits)
- Motion: Orbiting dots + interlocking panel shards on loading screen

### Positioning
Habitiq owns the operational backbone of co-living — who does what, when, whether it got done, who paid the bills, who your flatmates are. Not a chat app (WhatsApp). Not an expense tracker (Splitwise). Not a to-do app (Todoist). A category it defines: **intelligent shared living management**.

---

## 3. How It Was Built — The Full Story

### Method: Vibe Coding with AI

Entire product built by Sai (UI/UX designer, not a traditional engineer) using AI-assisted development.

- **Sai brought:** product concept, user flows, design decisions, feature prioritisation, business logic, brand identity
- **Claude Code brought:** full implementation — Next.js, Firestore schema, rotation engine, auth flows, security rules, Tailwind, deployment, security audit, voice assistant
- **Gemini used for:** pressure-testing assumptions, exploring edge cases, understanding backend architecture patterns

### Why This Matters

- Product went from concept to live, security-audited, multi-feature app in weeks, not months
- A designer with no backend engineering background shipped a production-ready product
- Zero handoff friction — design decision and implementation happen in the same conversation
- Features that take a week in a traditional team take an afternoon

> "I had the idea. I knew the problem. I knew what it needed to feel like. I described it, directed it, and shipped it — with AI as my engineering co-pilot."
> — Venkata Sai Jaswanth E

---

## 4. What Changed — Build History & Key Decisions

### Project Name History
- Originally named **FlatFlow** (repo: flatflow, early URL: flatsflow.netlify.app)
- Rebranded to **Habitiq** during build
- Previous URL: garbage-liart.vercel.app (Vercel auto-slug — now retired)
- **Live domain: habitiq.app (canonical — live as of 17 June 2026)**

### Hosting: Netlify → Vercel Migration
Moved because Vercel has native Next.js support, 6,000 build minutes/month free vs Netlify's 300, better App Router performance, simpler env var management.

### Critical Bug Fix: Google Login Silent Failure (May 2026)

**Root cause (two-part):**
**Part 1** — flatsflow.netlify.app not added to Firebase Auth → Authorized Domains.
**Part 2** — getRedirectResult error handler only called console.error — user saw nothing.
Fix: Added domain + surfaced redirectError on login page.

### Mobile Login: Custom Auth Proxy
Google OAuth on mobile (iOS Safari) fails with Firebase's default authDomain due to third-party cookie restrictions. Fix: Custom authDomain pointing to app's own domain via Next.js rewrites.

### Features Built (Milestone Order)

| Feature | Notes |
|---------|-------|
| Smart rotation engine | Core algorithm — deterministic queue-based |
| Google + email auth | Both from day one |
| Real-time Firestore sync | onSnapshot listeners on tasks, members, activity |
| Overdue tracking | Tasks past due date stay with responsible person |
| Swap request system | Formal request/accept/decline — replaces WhatsApp negotiation |
| Activity log | Full audit trail |
| Analytics + reliability scores | Completion grid + per-member scoring |
| Calendar view | Monthly task history |
| Dark/light mode | System preference + manual toggle |
| Mobile-first UI | Bottom nav mobile, sidebar desktop |
| Admin Org View | Full flat overview, all tasks, all statuses |
| Multi-flat support | User belongs to multiple flats, switches with one tap |
| Membership management | Leave flat, kick member, transfer admin, delete flat |
| NPS banner | Net Promoter Score survey |
| Mock mode | Full app runs without Firebase keys |
| 8-member cap | Enforced at Firestore rules level |
| Recurring Bills module | Admin configures bills; payer auto-rotates; variable amount |
| Daily Splits (Splitwise) | Ad-hoc expense log — equal or custom split, 7 currencies |
| Balances & Settle Up | Direct pairwise; bidirectional settle |
| Expense breakdown per person | Expand balance card to see contributing expenses |
| Person filter on transactions | Filter transaction list to one person's history |
| Month-end close flow | Admin locks a month; carry-forward balances |
| Group tasks | Admin creates tasks assigned to multiple members |
| Temp tasks | One-off tasks outside the rotation queue |
| Subscription system | Trial / active (coupon) / expired; gates on create_task/expense/bill |
| Rewards Wallet | Earn brand coupons on task completion; dynamic pool from Firestore |
| Voice Assistant | 4 sprints complete — NLU, 11 actions, waveform, Android/iOS |
| Member-submitted bills | Members submit bills they personally paid; admin approves |
| Flat Board (find-members) | Seeker profiles, lifestyle tags, vacancy listings |
| Insights page | Calendar + stats combined (replaces separate analytics/calendar) |
| Manage Flat page | Admin flat settings — invite code, NPS, name, join mode |

---

## 5. Technical Architecture

### Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Next.js 16 (React 19, App Router) |
| Build | Turbopack |
| Language | TypeScript (strict) |
| Styling | Tailwind CSS v4 + CSS variables |
| State | Zustand v5 + localStorage persistence |
| Icons | Lucide React |
| Components | shadcn/ui (button, card, input, label) |
| Animations | Framer Motion |
| Database | Firebase Firestore (real-time) |
| Auth | Firebase Auth (Google + email/password) |
| Hosting | Vercel Hobby (free) |
| Auth Proxy | Next.js rewrites (custom authDomain) |

### File Structure (C:\garbage)

```
C:\garbage\
├── app/
│   ├── (auth)/login/         — Login page (Google + email)
│   ├── (auth)/join/          — Join flat
│   ├── dashboard/
│   │   ├── layout.tsx        — Sidebar + nav shell + Voice system (lifted)
│   │   ├── page.tsx          — Home: My Tasks + Org View + swap widget
│   │   ├── insights/         — Calendar + stats combined (NEW)
│   │   ├── expenses/         — Bills + Daily Splits + Balances
│   │   ├── tasks/            — Task management (admin) / read-only (member)
│   │   ├── swaps/            — Swap requests (stat chips + admin All Swaps)
│   │   ├── members/          — Member list
│   │   ├── find-members/     — Flat Board: seeker profiles, lifestyle tags (NEW)
│   │   ├── manage-flat/      — Admin flat settings (NEW)
│   │   ├── activity/         — Full activity log
│   │   ├── profile/          — Profile + Rewards Wallet
│   │   ├── settings/         — User settings + voice settings
│   │   │   └── voice/        — Voice assistant settings (NEW)
│   │   └── about/            — About Habitiq
│   ├── onboarding/           — Create or join a flat (mobile redesign)
│   ├── privacy/              — Privacy Policy (DPDP Act 2023)
│   └── terms/                — Terms of Service
├── components/
│   ├── AuthProvider.tsx      — Central routing guard + public page bypass
│   ├── FlatSwitcher.tsx      — Multi-flat dropdown
│   ├── MemberAvatar.tsx      — Per-person color avatar (DNA system)
│   ├── GoingOutModal.tsx     — Out-of-station toggle
│   ├── NotificationToast.tsx — Real-time completion toasts
│   ├── NPSBanner.tsx         — NPS survey
│   ├── RewardUnlockModal.tsx — Task reward celebration overlay
│   ├── RewardsWallet.tsx     — Scratch-card reward grid in profile
│   ├── SubscriptionGate.tsx  — Trial/expired gate modals
│   ├── VoiceButton.tsx       — FAB long-press trigger (dumb component)
│   ├── VoiceListeningOverlay.tsx — Fullscreen overlay + waveform
│   ├── VoiceResponseCard.tsx — Slide-up result card, 8s dismiss, 5s undo
│   ├── VoiceFallbackModal.tsx — iOS text-input fallback bottom sheet
│   ├── VoiceSettings.tsx     — Voice settings panel
│   ├── WaveformVisualizer.tsx — Canvas waveform, 24 bars, violet gradient
│   ├── MicPermissionModal.tsx — Mic blocked/guide/unsupported states
│   └── HeroCanvas.tsx        — Landing page canvas animation
├── hooks/
│   ├── useVoiceAssistant.ts  — State machine (idle/listening/processing/responding/error)
│   └── useVoiceProcessor.ts  — NLU → action → response pipeline
├── lib/
│   ├── firebase.ts           — Init (with mock fallback)
│   ├── flatService.ts        — Create/join flat, delete, kick, leave
│   ├── rotationEngine.ts     — Smart rotation algorithm
│   ├── npsService.ts         — NPS logic
│   ├── expenseUtils.ts       — Direct pairwise balance computation
│   ├── settlementUtils.ts    — Month-end summary, carry-forward
│   ├── memberColors.ts       — 8-color member DNA palette
│   ├── rewardPool.ts         — Dynamic coupon pool from Firestore (6h localStorage cache)
│   ├── rewardSignal.ts       — Decoupled signal (avoids useFlatStore↔useRewardsStore circular import)
│   ├── discoveryTypes.ts     — Types for Flat Board (VacancyListing, SeekerProfile, LifestyleTag)
│   ├── discoveryTagService.ts — Firestore discovery tag service
│   ├── seekerService.ts      — Seeker profile + flat listing service
│   └── voice/
│       ├── nlu/
│       │   ├── intentClassifier.ts   — 9 intents, INTENT_PATTERNS, fuzzy + Levenshtein
│       │   ├── entityExtractor.ts    — Amount (6 formats incl. Indian colloquial), member, task
│       │   └── contextResolver.ts    — FlatContext type, ContextCache 30s TTL + write-invalidation
│       ├── actions/
│       │   ├── actionRouter.ts       — Intent → executor switch
│       │   ├── completeTask.ts       — Fuzzy task resolve, markTaskCompleted + cache invalidate
│       │   ├── createExpense.ts      — Creates expense, equal split, canUndo flag
│       │   ├── queryBalance.ts       — Net balance from context.balances
│       │   ├── queryTasks.ts         — Pending tasks, overdue-first sort
│       │   ├── queryStatus.ts        — OOS vs home breakdown
│       │   ├── requestSwap.ts        — Swap request to first available member
│       │   ├── createTask.ts         — Task creation with frequency
│       │   ├── greeting.ts           — Contextual greeting with pending count
│       │   └── unknown.ts            — Rotating suggestion messages
│       ├── response/
│       │   └── responseFormatter.ts  — ActionResult → VoiceResponse + ResponseCard
│       ├── tts/
│       │   └── speechSynthesis.ts    — VoiceSynthesizer singleton, en-IN preference
│       └── permissions.ts            — getMicPermissionState, requestMicPermission
├── store/
│   ├── useAuthStore.ts       — Auth + flat membership + sendPasswordResetEmail
│   ├── useFlatStore.ts       — Tasks, members, activity, swaps, expenses, bills, settlements
│   └── useRewardsStore.ts    — Firestore listener on /users/{uid}/rewards, onSnapshot modal trigger
├── firestore.rules           — Role-based DB security (all subcollections)
├── next.config.ts            — HTTP headers + auth proxy rewrites
├── DESIGN.md                 — Design system v2.0 (71-brand synthesis)
├── UI_PLAN.md                — 10-day implementation roadmap (Days 1-5 done)
├── VOICE.md                  — Voice assistant implementation brief + sprint log
└── project_1/                — THIS OBSIDIAN VAULT
```

### Firestore Schema

```
/users/{userId}
  email, displayName, photoURL

/flats/{flatId}
  name, createdBy, memberCount, createdAt
  trialStartedAt, subscriptionStatus (trial|active|expired)
  couponUsed?, couponExpiresAt?
  joinMode (open|approval)

  /members/{memberId}
    uid, displayName, email, role (admin|member),
    status (active|oos), reliabilityScore, joinedAt

  /tasks/{taskId}
    name, emoji, frequency (daily|weekly|fortnightly|monthly),
    priority (low|medium|high), rotationQueue[], currentAssignee,
    nextDueDate, startDate, status (pending|complete|overdue)

  /swapRequests/{requestId}
    fromUserId, toUserId, taskId, status (pending|accepted|declined)

  /activityLog/{activityId}
    type, userId, userName, message, timestamp

  /npsResponses/{responseId}
    uid, score, createdAt

  /expenses/{expenseId}
    description, amount, currency, category, paidBy, splitAmong[],
    splits{uid→amount}, date, createdBy, createdAt, deferToNextMonth?

  /settlements/{settlementId}
    fromUserId, toUserId, amount, currency, date, note?, createdAt

  /recurringBills/{billId}
    name, category, amount?, billingDay, currency, active, payerMode,
    rotationQueue[], participants[], createdBy, createdAt,
    collectorId?   — uid who collects member shares

  /billInstances/{instanceId}
    billId, name, amount, dueDate, status (pending|split_generated|paid|skipped),
    paidBy?, splits{uid→amount}?, participants[], currency, createdAt,
    collectedFrom?{uid→bool}   — per-member collection status
    collectorId?               — snapshot from template at generation time
    submittedBy?               — uid if member-submitted (pending admin approval)
    approved?: boolean         — admin approval status

  /monthCycles/{month}   (month = 'YYYY-MM')
    month, status (open|closed), closedAt?, totalBillsINR, totalExpensesINR,
    totalSettledINR, carryForwardOut{balances{uid→amount}}?

  /joinRequests/{requestId}
    uid, displayName, email, requestedAt, status (pending|approved|rejected)

/rewardPool/{rewardId}
  brandName, discountCode, description, isActive, expiryDays, category

/users/{userId}/rewards/{rewardId}
  brandName, discountCode, expiryDate, isRedeemed, unlockedAt
```

---

## 6. Voice Assistant — Full Specification (June 2026)

**Status:** Sprints 1–4 complete. Sprint 5 (optimization) pending.
**Full spec:** `VOICE.md`

### Architecture

```
useVoiceAssistant (hook) — state machine (idle/listening/processing/responding/error)
    ↓ transcript
useVoiceProcessor (hook) — NLU → action → response pipeline
    ↓
lib/voice/nlu/intentClassifier   — 9 intents, Levenshtein fuzzy, Hinglish + Telugu-English
lib/voice/nlu/entityExtractor    — Amount (6 formats), member, task, description
lib/voice/nlu/contextResolver    — FlatContext cache (30s TTL, write-invalidation)
    ↓
lib/voice/actions/actionRouter   — Routes intent → executor
    ↓ executes against Zustand store
[completeTask, createExpense, queryBalance, queryTasks, queryStatus, requestSwap,
 createTask, greeting, unknown]
    ↓
lib/voice/response/responseFormatter → VoiceResponse
    ↓
VoiceResponseCard (slide-up, 8s auto-dismiss, 5s undo bar for expenses)
```

### Conditions Covered

| Condition | Handling |
|-----------|----------|
| Browser doesn't support SpeechRecognition | VoiceFallbackModal (iOS text input) |
| Mic blocked at browser level | MicPermissionModal — fix guide, shows how to unblock |
| Mic permission denied mid-session | Differentiated error: blocked banner vs. not-yet-asked |
| Android Chrome gesture context | startListening synchronous, setState AFTER recognition.start() |
| TTS blocking recognition | TTS + callback moved out of setState to preserve async flow |
| Race condition on mic start | Synchronous permission flow + instant overlay on tap |
| Tab blur during listening | Auto-stop after tab blur |
| 10s hard timeout | VoiceAssistant auto-stops after 10 seconds |
| 1.5s silence | Auto-stop on 1.5s of silence |
| Multi-flat safety | ContextResolver uses activeFlatId — operates on current flat only |
| Cache invalidation | ContextCache cleared immediately after any write action |
| Retroactive task completions | completedAt > 2h → no reward issued |
| 1 reward / 24h anti-abuse | localStorage `habitiq_last_reward_at` key |

### NLU Performance (June 22 corpus)
- 1000-entry labeled corpus
- UNKNOWN rate: 13.4% (target < 15%) ✅
- Overall accuracy: 79%
- Per-intent: COMPLETE_TASK 82%, CREATE_EXPENSE 79%, QUERY_BALANCE 82%, GREETING 100%

---

## 6b. Security & Data Integrity Audit (Full History)

**Audit 1:** 2026-05-27 | 8 found, 8 fixed
**Audit 2:** 2026-06-03 | 7 found, 7 fixed
**Audit 3 (expense module):** 2026-06-04 | 3 found, 3 fixed

### Key Issues Fixed

| Severity | Issue | Fix |
|---------|-------|-----|
| HIGH | Any member could create/delete tasks | Firestore rules split: admin-only create/delete, member update |
| HIGH | Any member could accept ANY swap | Added `resource.data.toUserId == request.auth.uid` check |
| HIGH | deleteEntireFlatService orphaned data | Added joinRequests + npsResponses to deletion list |
| HIGH | reassignMemberTasks assigned to wrong person | Fixed index calculation: `newQueue[leavingIndex % newQueue.length]` |
| HIGH | kickMember non-atomic delete + decrement | writeBatch — both succeed or fail together |
| CRITICAL | joinRequests had NO Firestore rules | Added full block — admin reads all, requester reads own |
| CRITICAL | Expense rules not deployed | Ran `firebase deploy --only firestore:rules` |
| HIGH | note: undefined rejected by Firestore | `fs()` helper strips undefined keys before all writes |
| MEDIUM | generateFlatId() used Math.random() | Replaced with crypto.getRandomValues() |
| MEDIUM | activityLog fetched entire collection | Changed to query with limit(50) + orderBy |

### Still To Do
- Firebase API Key Restrictions in Firebase Console
- Content Security Policy (CSP)
- Firebase App Check for rate limiting

---

## 6c. Member Bills — Full Conditions (June 24)

Members can now submit one-time bill instances they personally paid (e.g., internet bill, water bill).

**Flow:**
1. Member taps "I paid a bill" → fills amount, category, who splits it
2. Bill appears in admin's "generate pipeline" as a pending card with member avatar
3. Admin sees inline edit controls — can change amount/payer before approving
4. Admin approves → splits recalculated → enters normal pipeline as split_generated
5. Admin rejects → bill deleted with reason

**Conditions:**
- Only members who personally paid can submit (not admin-only)
- Admin must approve before the bill enters the collection pipeline
- If admin edits amount/payer before approving, splits are recalculated atomically
- Rejected bills never enter the pipeline
- Collection toggle permissions: collector sees all, member sees only themselves, admin (non-collector) sees none

---

## 6d. Flat Board — Full Conditions (June 2026)

New discovery layer. Members searching for flatmates or seekers searching for a flat.

**Data types:**
- `VacancyListing` — a flat with open room(s), lifestyle tags, rent range, location
- `SeekerProfile` — someone looking for a flat: lifestyle tags, budget, move-in timing
- `LifestyleTag` — 25+ tags (IT professional, student, early_bird, vegetarian, no_smoking, etc.)
- `DiscoveryTag` — Firestore document, slugified, fetchable

**Lifestyle tag categories:**
- Work & Schedule (IT/Tech, Finance, Student, Freelancer, WFH, Night shifts, 9-to-5)
- Social & Personality (Early riser, Night owl, Homebody, Social, Fitness freak)
- Diet & Habits (Vegetarian, Jain, Non-veg OK, No smoking, Social drinker, Pet-friendly)
- Sleep & Sound (Quiet hours, Light sleeper)

---

## 7. Complete Feature Set (v0.5.0)

### Auth
Google Sign-In (one-tap) · Email/Password · Custom auth domain proxy (iOS Safari fix) · Session persistence · Minimal data · **Forgot password flow** (email enumeration safe) · Redirect errors surfaced to user

### Onboarding
**Mobile:** Continuity flow — Choose screen → Create Flat (170px hero strip, "Your Flat. Your Rules.") or Join Flat ("Your Crew. Already Here."). Invalid invite codes caught early.
**Desktop:** Premium two-panel layout — full-viewport image + form.
Create flat · Join flat · Join/create additional flats without logout · Approval-mode join

### Rotation Engine
Auto-assignment · Per-task rotation queue · Skip OOS members · Resume on return · Admin manual override · Custom start date · Frequencies: daily/weekly/fortnightly/monthly · Priority: low/medium/high

### Task Management
Admin creates/deletes/edits tasks · Mark done (single tap) · Retroactive date editing · Overdue tracking + persistence · New member auto-added to all queues · Group tasks · Temp tasks

### Swap System
Request swap · Cancel/withdraw swap · Accept/Decline · Persistent dashboard banner · 4 stat chips (Sent/Received/Accepted/Declined) · Admin All Swaps toggle · Dashboard swap summary widget

### Expenses & Bills

**Recurring Bills:**
- Fixed or variable amount per month
- Payer auto-rotates
- Collector field (separate from payer) — avatar card picker
- Admin generates on billing date
- **Member-submitted bills** — member submits → admin approves/edits/rejects → recalculates splits
- Collection tracking per-member (Received/Pending toggles)
- Bill instances: pending → split_generated → paid/skipped
- Future-dated expenses blocked

**Daily Splits:**
- Ad-hoc expense log — equal or custom split, 7 currencies
- Banking-style transaction list: category emoji + net amount
- Edit (creator/admin) · Delete (optimistic) · Deferred expenses

**Balances & Settlement:**
- Direct pairwise (no MCF chains)
- Expand to see contributing expenses
- Settle (debtor) · Mark Received (creditor) · Partial payment
- Person filter on transaction list
- Settlements collapsed by default

**Month-end close:** Admin locks month, carry-forward balances

### Voice Assistant
9 intent types · 11 action executors · NLU accuracy 79% overall, 13.4% UNKNOWN · Hinglish + Telugu-English support · Waveform visualizer · iOS fallback modal · Android Chrome mic handling · 8s response card + 5s undo for expenses · Single instance lifted to layout level (all pages covered)

### Subscription System
Trial (30d from flat creation) · Active (coupon redeemed) · Expired (trial ended)
Gates: create_task, add_expense, create_bill, create_flat (view-only on expiry)
Coupons: HAB-WELCOME (90d), EARLYBIRD-2026 (90d), HABITIQ-BETA (90d)
maxFlats: Premium = 3, trial/expired = 1
Crown badge + PREMIUM pill when active

### Rewards Wallet
Every task completion = reward · Anti-abuse: 1 reward/24h, retroactive completions excluded
Dynamic pool from `/rewardPool` Firestore collection (6h localStorage cache)
Scratch-card grid in Profile · Unlock modal (scale spring + confetti)
Firestore rules: owner read+create, isRedeemed update only

### Flat Board (Find Members)
Seeker profiles with lifestyle tags · Vacancy listings · Join request from discovery
25+ lifestyle tags across 4 categories

### Analytics & Calendar (Insights page)
Calendar view with member filter · Task completion grid · Reliability scores · Monthly/all-time toggle

### Navigation
**Mobile:** 5-slot bottom nav (Dashboard · Expenses · [FAB] · Tasks · Profile) · Radial FAB (Split / Bill / Task for admin; Split only for member) · Voice on FAB tap
**Desktop:** Sidebar with voice pill · Insights, Find Members, Manage Flat in sidebar

### Legal
Privacy Policy (DPDP Act 2023 compliant) · Terms of Service (India governing law) · Both public routes (no auth required)

### Landing Page (June 26)
New hero: "Find your flat. Run it without drama." · FlatFinderScreen mockup (AI compatibility %) · New logo throughout · Redesigned loading animation (panel shards + orbiting dots + spring entrance)

---

## 8. User Flows (all conditions)

### Flow 1 — Admin Creates Flat
Open app → Sign in → Onboarding (Your Flat. Your Rules.) → Create Flat → Invite code generated → AdminWelcomeModal (HAB-WELCOME pre-filled) → Dashboard

### Flow 2 — Member Joins
Open app → Sign in → Onboarding (Your Crew. Already Here.) → Join Flat → Invalid code: immediate error → Valid code: auto-added to all queues → Dashboard

### Flow 3 — Daily Task
Open app → See "Your Tasks" → Mark Done (single tap) → Reward modal (if within 24h window, not retroactive) → Next person auto-assigned

### Flow 4 — Swap Request
Swap Requests button (Tasks page) → Request → Select flatmate → Flatmate accepts/declines → Task reassigned or stays. Admin: swaps page "All Swaps" toggle.

### Flow 5 — Add Expense
FAB → Split expense → Description, amount, category, date → Equal or custom split → Save → Instant balance update

### Flow 6 — Monthly Bill (admin)
Monthly Bills tab → Add Monthly Bill → Fixed or Variable → Set billing day → Choose participants → Pick Collector (avatar card) → Save → Generate on billing date → Collection tracking

### Flow 7 — Member Submits a Bill
I paid this bill → Fill details → Admin sees pending card in generate pipeline → Admin edits if needed → Approves → Splits calculated → Collection begins

### Flow 8 — Voice Command
Long FAB tap (or short tap) → Listening overlay → Say "mark kitchen done" → NLU → action executes → Response card slides up → Auto-dismiss 8s. Expense undo available 5s.

### Flow 9 — Settlement
Balances strip (compact) → Tap to expand → Settle button (you owe) or Mark Received (they owe) → Partial or full → Balance recomputes instantly

### Flow 10 — Member Leaves
Settings → Danger Zone → Leave flat → Not admin: switch to next flat. Admin with others: transfer role first. Last member: flat deleted.

### Flow 11 — Subscription Expired
Trial ends → AdminExpiredModal (dismiss + view-only option). Enter coupon → redeems → PREMIUM. Members see "ask your admin for a coupon" hint.

### Flow 12 — Voice Mic Blocked
Long-press FAB → Mic blocked → MicPermissionModal with fix guide (how to unblock in Chrome/Safari) → User unblocks → Normal voice flow

---

## 9. Current Status & Known Limitations

### Live and Working
Smart rotation ✅ · Google + email login ✅ · Real-time sync ✅ · Mobile UI ✅ · Security audits ✅ · Multi-flat ✅ · Membership management ✅ · Swap system ✅ · Analytics ✅ · Calendar ✅ · Activity log ✅ · Dark mode ✅ · NPS ✅ · PWA ✅ · Recurring Bills ✅ · Member-submitted bills ✅ · Daily Splits ✅ · Balances & Settlement ✅ · Month-end close ✅ · Privacy Policy ✅ · Terms of Service ✅ · Subscription system ✅ · Rewards Wallet ✅ · Voice Assistant (Sprints 1–4) ✅ · Flat Board ✅ · Insights page ✅ · Manage Flat page ✅ · Landing redesign ✅

### Open Items

| Item | Status | Notes |
|------|--------|-------|
| UI Plan Day 6 (forms/modals/sheets) | [ ] Pending | Next session |
| UI Plan Day 7 (toasts/empty states) | [ ] Pending | |
| UI Plan Day 8 (analytics charts) | [ ] Pending | Recharts install needed |
| UI Plan Day 9 (profile/onboarding) | [ ] Pending | |
| UI Plan Day 10 (final polish) | [ ] Pending | |
| Voice Sprint 5 (optimization) | [ ] Pending | Lazy-load, battery, analytics |
| Push notifications | [ ] Phase 2 | Firebase Cloud Messaging |
| Firestore rules redeploy | ⚠️ Check | `firebase deploy --only firestore:rules` if rules changed |
| Firebase API Key Restrictions | [ ] Pending | Firebase Console |
| Content Security Policy | [ ] Pending | next.config.ts |

---

## 10. Roadmap — Future Requirements

> **Architecture basement:** [[FOUNDATION]] — Web invents · PWA bridges · Android (Kotlin) → Play Store · iOS later.

### Phase 1 — Trial (Now) CURRENT
- [x] All core web features built
- [x] PWA installable
- [x] Foundation + DOC_MAP locked (Aug 2026)
- [ ] Collect user feedback (ongoing)
- [ ] Fix bugs from real usage (ongoing)

### Phase 2 — Growth + Android
Push notifications (FCM) · WhatsApp integration · Android Tasks/rotation parity · Play Store closed/open testing · Admin flat-wide balance matrix · Task photo proof · Guest invite link

### Phase 3 — Scale & Monetisation
Play Store production · iOS after Android stable · Stripe/Razorpay billing · Admin super-dashboard · Analytics export · Offline mode (revisit) · Firebase Cloud Functions

---

## 10b. Foundation lock (Aug 2026)

Decisions locked in [[FOUNDATION]]:

- Web (`C:\garbage` / habitiq.app) is the permanent source of truth for product logic
- One Firebase project + shared `firestore.rules` for all clients
- Android = Kotlin/Compose at `C:\habitiq_jaswanth` (Expo abandoned)
- Repo cleaned: one-off patch/fix scripts, debug media, empty folders, third-party design clone removed
- Vault structure: `FOUNDATION.md` · `DOC_MAP.md` · `voice/` · `archive/` · logic contracts

---

## 11. Business Model & Monetisation

### Freemium

| Tier | Price | Limits | Target |
|------|-------|--------|--------|
| Free | ₹0/month | 6 members, 10 tasks, core features | Student flats, trial users |
| Pro | ₹99/flat/month | Unlimited members + tasks, push, photo proof, analytics export | Active flats, professionals |
| Business | ₹499/property/month | Multiple flats, super-dashboard, white-label, priority support | PG owners, co-living operators |

---

## 12. Design System (v2.0) — Quick Reference

**Source:** `C:\garbage\DESIGN.md` and `C:\garbage\app\globals.css`

```
Canvas:    #0c0b0f (page) / #1a1820 (card) / #211f28 (modal)
Hairline:  #2a2635
Ink:       #f4f3f8 (primary) / #a09db0 (soft) / #514e61 (mute)
Primary:   #7c3aed (violet) / #a78bfa (soft) / rgba(124,58,237,0.1) (muted)
Positive:  #22c55e
Warning:   #f59e0b
Negative:  #ef4444
Streak:    #f97316
Reward:    #eab308
Card pad:  20px standard / 24px spacious
Button h:  52px
Input h:   52px
Radius:    12px (card/button) / 8px (input/chip) / 9999px (pill)
```

**Member DNA colors (8-person palette):**
0=amber(#f59e0b) · 1=teal(#14b8a6) · 2=rose(#f43f5e) · 3=sky(#0ea5e9)
4=violet(#8b5cf6) · 5=lime(#84cc16) · 6=orange(#f97316) · 7=cyan(#06b6d4)

---

## 13. Important Rules for Working on This Project

- **Live app with real users** — be careful with changes
- `firestore.rules` is security-critical — always run `firebase deploy --only firestore:rules` after changing rules
- Mock mode exists — app runs fully without Firebase keys using seeded data
- **After significant changes, update this vault doc**
- DESIGN.md is the design spec. UI_PLAN.md is the implementation roadmap. Read both before any UI work.
- Voice assistant is in layout.tsx as a single lifted instance — never instantiate it again in a child page

---

## 14. Why the Vault Exists

Sai's explicit instruction: every new Claude Code session starts cold and loses all context. The vault at `C:\garbage\project_1` is the persistent memory for this project. Reading it at session start eliminates the need to re-read source files or re-explain what was built.

**How to apply:** Always start Habitiq sessions by reading this vault. Always end significant sessions by updating it.

---

*"Your flat, on autopilot. Built in India. For everyone who has ever had a flatmate."*

**Document version:** 2.0 | **Source:** C:\garbage | **Vault:** C:\garbage\project_1 | **June 2026**
**Maintained by:** [[About Sai]]
