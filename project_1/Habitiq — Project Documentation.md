# Habitiq — Project Documentation

> **Product:** Habitiq
> **Version:** v0.4.0 (Trial Phase — Member UX, Swap Analytics, Bill Collector)
> **Project folder:** C:\garbage
> **Live URL:** https://garbage-liart.vercel.app
> **Repo:** github.com/saijaswanthedupuganti-cmyk/flatflow
> **Target Domain:** habitiq.in / habitiq.app
> **Status:** Live — Active Trial with Real Users
> **Last Updated:** 16 June 2026
> **Founder:** Venkata Sai Jaswanth E (UI/UX) · **Co-founder:** Upputuri Bhanu Kalyan (Full-Stack)

See also: [[About Sai]]

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

### Brand Voice
Direct and confident. Conversational — speaks like a flatmate. Calm, not hype. Specific over vague.

**Sounds like:** "No arguments. No forgotten tasks. Just a fair system that runs itself."
**Does NOT sound like:** "Leverage synergies to optimise household workflows." ✗

### Visual Identity
- Primary colour: Violet/Indigo — #7c3aed to #4338ca
- Background: Deep black #0a0a0a (dark) / clean white (light)
- Logo: House icon + rotation arrow
- Font: Inter
- Motion: Orbiting dots on loading screen (visual metaphor for the rotation engine)

### Positioning
Habitiq owns the operational backbone of co-living — who does what, when, whether it got done. Not a chat app (WhatsApp). Not an expense tracker (Splitwise). Not a to-do app (Todoist). A category it defines: **intelligent shared living management**.

---

## 3. How It Was Built — The Full Story

### Method: Vibe Coding with AI

Entire product built by Sai (UI/UX designer, not a traditional engineer) using AI-assisted development.

- **Sai brought:** product concept, user flows, design decisions, feature prioritisation, business logic, brand identity
- **Claude Code brought:** full implementation — Next.js, Firestore schema, rotation engine, auth flows, security rules, Tailwind, deployment, security audit
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
- Current URL: garbage-liart.vercel.app (Vercel auto-slug — not the final domain)
- Target domain: habitiq.in / habitiq.app

### Hosting: Netlify → Vercel Migration
Moved because Vercel has native Next.js support, 6,000 build minutes/month free vs Netlify's 300, better App Router performance, simpler env var management.

### Critical Bug Fix: Google Login Silent Failure (May 2026)

**Symptom:** Clicking "Continue with Google" — user selects Gmail — app silently returns to login screen.

**Root cause (two-part):**

**Part 1 — Firebase Authorized Domains:**
flatsflow.netlify.app was not added to Firebase Auth → Authorized Domains. Firebase rejects OAuth callbacks from unrecognized domains.
Fix: Manually added domain in Firebase Console.

**Part 2 — Silent redirect error:**
getRedirectResult error handler only called console.error — user saw nothing.
Fix: Added redirectError field to auth store → surfaced on login page → getAuthErrorMessage() handles unauthorized-domain with a clear message.

### Mobile Login: Custom Auth Proxy
Google OAuth on mobile (iOS Safari) fails with Firebase's default authDomain due to third-party cookie restrictions. Fix: Custom authDomain pointing to app's own domain via Next.js rewrites. All OAuth redirects go through app's domain, bypassing cookie issue.

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
| NPS banner | Net Promoter Score survey for user feedback collection |
| Mock mode | Full app runs without Firebase keys — seeded demo data |
| 8-member cap | Enforced at Firestore rules level |
| Recurring Bills module | Admin configures monthly bills; payer auto-rotates; variable amount support |
| Daily Splits (Splitwise) | Ad-hoc expense log — equal or custom split, 7 currencies, banking-style UI |
| Balances & Settle Up | Direct pairwise balance calc; bidirectional settle (pay or mark received) |
| Expense breakdown per person | Expand balance card to see which expenses make up the total |
| Person filter on transactions | Filter full transaction list to show only shared history with one person |
| Month-end close flow | Admin locks a month; carry-forward balances flow to next month |
| Group tasks | Admin creates tasks assigned to multiple members with sub-tasks |
| Temp tasks | One-off tasks outside the rotation queue |

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
│   │   ├── layout.tsx        — Sidebar + nav shell
│   │   ├── page.tsx          — Home: My Tasks + Org View
│   │   ├── analytics/        — Completion grid + reliability scores
│   │   ├── calendar/         — Monthly task calendar
│   │   ├── tasks/            — Task management (admin)
│   │   ├── members/          — Member list
│   │   ├── swaps/            — Swap requests
│   │   ├── activity/         — Full activity log
│   │   ├── settings/         — User settings + leave flat
│   │   └── about/            — About Habitiq
│   └── onboarding/           — Create or join a flat
├── components/
│   ├── AuthProvider.tsx      — Central routing guard
│   ├── FlatSwitcher.tsx      — Multi-flat dropdown
│   ├── GoingOutModal.tsx     — Out-of-station toggle
│   ├── NotificationToast.tsx — Real-time completion toasts
│   └── NPSBanner.tsx         — NPS survey
├── lib/
│   ├── firebase.ts           — Init (with mock fallback)
│   ├── flatService.ts        — Create/join flat
│   ├── rotationEngine.ts     — Smart rotation algorithm
│   └── npsService.ts         — NPS logic
├── store/
│   ├── useAuthStore.ts       — Auth + flat membership
│   └── useFlatStore.ts       — Tasks, members, activity, swaps, expenses, bills, settlements
├── lib/
│   ├── firebase.ts           — Init (with mock fallback)
│   ├── flatService.ts        — Create/join flat, delete, kick, leave
│   ├── rotationEngine.ts     — Smart rotation algorithm
│   ├── npsService.ts         — NPS logic
│   ├── expenseUtils.ts       — Direct pairwise balance computation
│   └── settlementUtils.ts    — Month-end summary, suggested settlements, carry-forward
├── firestore.rules           — Role-based DB security (expenses, bills, settlements)
├── next.config.ts            — HTTP headers + auth proxy rewrites
├── PRODUCT.md                — Business roadmap
├── FLATFLOW_LAUNCH_DOCUMENTATION.md — Full launch doc
├── SECURITY_AUDIT.md         — Audit findings
└── project_1/                — THIS OBSIDIAN VAULT
```

### Firestore Schema

```
/users/{userId}
  email, displayName, photoURL

/flats/{flatId}
  name, createdBy, memberCount, createdAt

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
    collectorId?          — uid who collects member shares; defaults to admin; changeable any month

  /billInstances/{instanceId}
    billId, name, amount, dueDate, status (pending|split_generated|paid|skipped),
    paidBy?, splits{uid→amount}?, participants[], currency, createdAt,
    collectedFrom?{uid→bool} — per-member collection status (set by collector or admin),
    collectorId?          — snapshot from template at generation time

  /monthCycles/{month}   (month = 'YYYY-MM')
    month, status (open|closed), closedAt?, totalBillsINR, totalExpensesINR,
    totalSettledINR, carryForwardOut{balances{uid→amount}}?

  /joinRequests/{requestId}
    uid, displayName, email, requestedAt, status (pending|approved|rejected)
```

### Architecture Decisions

- **Real-time by default** — Firestore onSnapshot; no polling, no stale data
- **Deterministic rotation** — given queue + active members, next assignee is always predictable regardless of which device calculates it
- **Mock mode** — full app without Firebase keys using seeded data; rapid dev without touching production DB
- **Security is structural** — Firestore rules enforce access at DB level, not just UI

---

## 6. Security & Data Integrity Audit

**Original audit:** 2026-05-27 | 8 found, 8 fixed before launch
**Second audit:** 2026-06-03 | 7 more found, all fixed — see below
**Third audit (expense module):** 2026-06-04 | 3 issues found, all fixed — see 6c below

### HIGH — Any Member Could Create/Delete Tasks
File: firestore.rules
Problem: tasks subcollection used allow write for all members — any member could create/delete tasks without being admin.
Fix: Split into allow create, delete (admin only) + allow update (any member, for marking complete).

### HIGH — Any Member Could Accept/Reject ANY Swap Request
File: firestore.rules
Problem: swap update only checked membership, not that user was the intended recipient.
Fix: Added resource.data.toUserId == request.auth.uid — only the person the swap was sent TO can resolve it.

### MEDIUM — No HTTP Security Headers
File: next.config.ts
Problem: No headers set — vulnerable to clickjacking, MIME sniffing, referrer leakage.
Fix: Added 6 headers — X-Frame-Options: DENY, X-Content-Type-Options: nosniff, Referrer-Policy, HSTS, Permissions-Policy, X-XSS-Protection.

### MEDIUM — Auth Errors Exposed Firebase Codes (User Enumeration)
File: app/page.tsx
Problem: Raw Firebase error codes shown to user — attacker could probe to find registered emails.
Fix: getAuthErrorMessage() maps all codes to generic safe messages.

### LOW — Math.random() for ID Generation
File: store/useFlatStore.ts
Problem: Not cryptographically secure — predictable values.
Fix: Replaced with crypto.randomUUID() in all 3 places.

### LOW — Google Icon from External CDN
File: app/page.tsx
Problem: Loaded from svgrepo.com — external dependency, supply chain risk.
Fix: Downloaded and saved locally as /public/google-icon.svg.

### LOW — Activity Log userId Not Validated
File: firestore.rules
Problem: Any member could write activity log with any userId — audit trail untrustworthy.
Fix: Validates request.resource.data.userId == request.auth.uid || userId == 'system'.

### LOW — No Input Length Limits
Files: app/page.tsx, app/onboarding/page.tsx
Problem: No maxLength — extremely long strings could be stored in Firestore.
Fix: Nickname 30, Flat name 50, Email 254, Password 128.

### Still To Do (Not Yet Fixed — Original Audit)
- Firebase API Key Restrictions in Firebase Console
- Password strength UX (show "min 6 chars" hint)
- Content Security Policy (CSP)
- Firebase App Check for rate limiting

---

## 6f. Splitwise Completion & Expenses Redesign — June 2026 (Session 4)

### REDESIGN — Expenses summary card
File: app/dashboard/expenses/page.tsx
Old: Color hero card showing net unsettled balance (balance metric, not a spending metric).
New: Dark summary card (#1A202C) showing total monthly spend with a two-tone progress bar — amber = Fixed Bills, blue = Daily Splits. Two stat tiles show each category amount and its percentage of the total. Admin "Close month" button lives here. Correctly emphasises the two main things: Fixed Bills and Daily Splits.

### REDESIGN — Balance section collapsed by default
File: app/dashboard/expenses/page.tsx
Old: Full per-person balance cards + two "You owe / Owed to you" stat cards always visible above the tab switcher — blocked the main view on mobile.
New: Single compact strip showing net status at a glance ("You owe ₹2,000 · 2 people" orange, or "All balances settled" green). Tap to expand full per-person cards. Two stat cards removed. Balances accessible but no longer in the way.

### FEAT — Bidirectional settlement (Mark Received)
File: app/dashboard/expenses/page.tsx
Previously only the debtor could settle (Settle button on "you owe them" cards only). Added "Mark Received" to "they owe you" cards — creditor can record that the other person paid them (cash/UPI). SettleUpModal handles both directions via `reversed` prop — swaps `fromUserId/toUserId`, shows green accent and "Received from" label, CTA says "Mark Received".

### FEAT — Expense breakdown on balance card expand
File: app/dashboard/expenses/page.tsx
Tapping a balance card expands it to list every expense and bill that makes up that balance — category emoji, description, date, per-item net amount, sorted newest first. Users can now see exactly why they owe a specific amount.

### FEAT — Person filter on transaction list
File: app/dashboard/expenses/page.tsx
"Filter list by person" button inside expanded balance card. Filters the Daily Splits transaction list to show only expenses and settlements shared with that person. Blue active-filter banner at the top of the list with one-tap Clear.

### FEAT — Settle modal improvements
File: app/dashboard/expenses/page.tsx
- Quick-fill chips: "Pay full · ₹X" and "Pay half · ₹Y"
- Remaining-after-payment preview when entering a partial amount
- Input goes red and CTA disables if amount exceeds balance owed
- CTA label changes dynamically ("Pay ₹2,000" for partial, "Mark as Paid" for full)
- try/catch on handleSave — modal never freezes if Firestore fails

### FEAT — Desktop sidebar Swaps badge counts all pending swaps
File: app/dashboard/layout.tsx
Old: `pendingSwaps` counted only swap requests where `toUserId === currentUser`. Showed 0 for admins who had no incoming swaps.
New: counts all pending swaps in the flat regardless of direction. Both admin and member see the total pending count.

### FEAT — Tasks page mobile Swap Requests shortcut
File: app/dashboard/tasks/page.tsx
Mobile-only banner (md:hidden) below the Tasks page header. Shows "Swap Requests" with live pending count badge. Tapping navigates directly to /dashboard/swaps. Replaces the need for a dedicated Swaps slot in the mobile bottom nav.

---

## 6g. Member UX, Swap Analytics & Bill Collector — June 2026 (Session 6)

### FIX — Mobile nav: Profile restored to slot 5 for all users
File: `app/dashboard/layout.tsx`
Previous session incorrectly replaced Profile with Swaps in slot 5 for members. Reverted. Both admin and member now share the same 5-slot layout: Dashboard · Expenses · [FAB] · Tasks · Profile. Swaps are not a separate nav destination for members on mobile — they are accessed via the Swap Requests button on the Tasks page.

### FEAT — Member Tasks page: read-only view with swap bottom sheet
File: `app/dashboard/tasks/page.tsx`
Members see a completely different Tasks page from admin. Early return before the admin view renders:
- Header with overdue badge counter
- **Swap Requests button** at top (violet, shows pending count) — opens a slide-up bottom sheet, NOT page navigation
- Task list: compact cards (emoji + name + YOU badge + overdue status + assignee · due date + expand arrow)
- Tap any card on mobile → expands in-place to show: frequency/priority/due badges, full rotation queue (NOW indicator, OOS markers, position chips), last done date
- No New Task, Edit, Delete, or Override controls visible to members
- Bottom sheet: handle bar + Received/Sent sections with Accept/Decline buttons + empty state

### FIX — Leave flat redirect: always went to /onboarding instead of next flat
File: `app/dashboard/profile/page.tsx`
Root cause: `handleConfirmLeave` and `handleTransferAndLeave` received `nextFlatId` from `leaveFlatService` (which correctly updates Firestore `activeFlatId`) but never called `switchFlat(nextFlatId)` to update the in-memory `useAuthStore` state. So routing to `/dashboard` loaded with the old, now-invalid flat ID.
Fix: Added `await switchFlat(nextFlatId)` + `initFirestoreListeners(nextFlatId)` before `router.push('/dashboard')` in both handlers. Pattern modelled on `handleSwitchFlat` which already did this correctly.

### RENAME — "Fixed Bills" → "Monthly Bills"
Files: `app/dashboard/expenses/page.tsx`
All UI references to "Fixed Bills" renamed to "Monthly Bills" — tab label, breadcrumb, card headers, buttons, empty state. The underlying data model is unchanged.

### FEAT — Payer collection tracking on bill instances
Files: `store/useFlatStore.ts`, `app/dashboard/expenses/page.tsx`, `firestore.rules`
The person who pays the bill upfront (landlord/utility) can now track which members have paid them back their individual share, per bill instance:
- New field: `BillInstance.collectedFrom?: Record<string, boolean>` — uid → received true/false
- New store action: `markBillCollected(instanceId, memberUid, collected)` — optimistic update + Firestore dot-notation write `{ ['collectedFrom.uid']: value }`
- UI: violet header strip "Collection status — mark who has paid you back" visible to payer and admin when `status === 'split_generated'`; per-person toggle button: green "✓ Received" or red "Pending"
- Firestore rule: payer can update `['status', 'paidAt', 'collectedFrom']` on their own instances

### FEAT — Swap page redesign: stat chips + admin All Swaps view
File: `app/dashboard/swaps/page.tsx`
Swaps page now has two major additions:
- **4 stat chips** at top of "My Swaps" view: Sent (blue) · Received (violet, with pending badge) · Accepted (green) · Declined (red) — counts scoped to the current user
- **Admin toggle** top-right: "My Swaps" | "All Swaps" — All Swaps renders a flat list of every swap in the flat with `From → To · Task` layout, Auto/You badges, status badges, and inline Accept/Decline buttons for pending swaps addressed to the admin

### FEAT — Dashboard swap summary widget
File: `app/dashboard/page.tsx`
New clickable card on the home dashboard (only shown when the user has swap history) linking to `/dashboard/swaps`:
- Shows 4 stats: Sent · Received · Accepted · Declined
- Pending count badge in card header if action needed
- Admin sees a footnote: "N total swaps in flat · tap to see all"
- Positioned between Bills & Expenses widget and NPS banner

### FEAT — Collector field on Monthly Bills
Files: `store/useFlatStore.ts`, `app/dashboard/expenses/page.tsx`, `firestore.rules`
Separates the concept of "who pays the owner" (payer rotation) from "who collects money from flatmates" (collector):
- New field: `RecurringBill.collectorId?: string` — persists on the template, carries month-to-month until changed
- New field: `BillInstance.collectorId?: string` — snapshot from template at generation time
- `generateBill` copies `bill.collectorId` to the instance
- `updateRecurringBill` type updated to include `collectorId`
- **UI in bill card header:** "X pays · Y collects · 5th monthly" — collector shown inline; "You collect" in violet when it's the current user
- **Collection tracking strip:** header text changes — "mark who has paid you back" for the collector, "X is collecting" for others; non-collector members are marked `isPayer` (no toggle shown)
- **Firestore rule:** new third branch — `collectorId == request.auth.uid` can update `['collectedFrom']` only (no status/paidAt)

### REDESIGN — MonthlyBillModal: collector picker + amount type selector
File: `app/dashboard/expenses/page.tsx`
Two major modal UI improvements:

**Collector picker** (replaced plain `<select>`):
- Grid of avatar cards — one per flat member
- Each card shows: colored avatar circle with initial, name below, "you" sub-label for self
- Selected state: violet border + violet tint background + blue checkmark badge on avatar
- Tapping selects that person as collector (radio selection)

**Amount type selector** (replaced disconnected bottom toggle):
- The old toggle was at the bottom of the form while the amount input was near the top — broken UX, user reported "not working properly"
- Fix: replaced with a 2-button radio selector directly above the amount input: **Fixed** | **Variable**
- Fixed selected → ₹ amount input appears immediately below with ₹ prefix
- Variable selected → amber info card replaces input: "You'll enter the actual amount each month"
- Modal header corrected: "Add New Fixed Bill" → "Add Monthly Bill"

---

## 6e. Splitwise Overhaul & UI Redesign — June 2026 (Session 3)

### FIX — Wrong people shown in balance (MCF algorithm removed)
File: lib/expenseUtils.ts
Problem: The Minimum Cash Flow algorithm collapsed debt chains — if A owed B and B owed C, MCF told A to pay C directly. Users saw balances with people they had never directly transacted with; the "who owes whom" table was confusing and wrong from the user's perspective.
Fix: Replaced MCF with direct pairwise calculation. For each expense/bill/settlement, we accumulate a net amount between the current user and each counterparty. Users now only see balances with people they actually transacted with. No debt-chain simplification.

### FIX — Deleted expense still visible on dashboard (optimistic delete)
File: store/useFlatStore.ts
Problem: `deleteExpense` only called Firestore `deleteDoc` and waited for `onSnapshot` to propagate (~1–2s lag). The deleted expense remained visible in the balance and transaction list during that window.
Fix: Added optimistic update — `set(s => ({ expenses: s.expenses.filter(e => e.id !== expenseId) }))` before the Firestore call. Balance recomputes immediately.

### REDESIGN — Balance/Settle section (expenses page)
File: app/dashboard/expenses/page.tsx
Problem: The settle flow was buried inside the dark hero card's bottom section — small "Settle" button, hard to spot, no clear "you owe them" vs "they owe you" framing. Settlement was described as "not easy to find" by user.
Fix: Extracted balances out of the hero card entirely. New dedicated section below the stat cards shows per-person balance cards:
- Green card with avatar + "owes you" label + green amount for money owed to you
- Orange card with avatar + "you owe them" label + orange amount + prominent Settle button
- Empty state shows "All balances settled — you're square!"
Hero card simplified to show only total owed / status.

### FIX — Member mobile nav slot 4 → Tasks (not Swaps)
File: app/dashboard/layout.tsx
Problem: After the previous session changed slot 4 to Tasks, the pending-swap badge was still attached — members saw Tasks but the badge counted swap requests. Swap access was via dashboard badge link only.
Fix: Members see Tasks in slot 4 with the pending-swap badge (so they know swaps need attention and can tap Tasks → swap button from there). Dashboard badge still links to the swaps page for direct access.

### UX — Bill cards Mark Paid visible to all payers
File: app/dashboard/expenses/page.tsx
Problem: The "Mark Paid" CTA was inside `{isAdmin && (...)}` so members who were listed as payers couldn't mark their own bills paid.
Fix: Extracted Mark Paid into its own block gated by `isYouPayer || isAdmin`. All payers see the CTA; non-payer members do not.

### UX — Auto-create expense when bill marked paid
File: store/useFlatStore.ts
Fix: `markBillPaid` now creates a corresponding expense in the transactions list (category: 'bills', description from bill name, amount/splits from bill instance) so the split-wise transaction list reflects bill payments automatically.

### UX — Transaction list banking-style redesign
File: app/dashboard/expenses/page.tsx
Fix: `ExpenseRow` redesigned to banking-style layout: category emoji icon (rounded square) on left, bold description + "payer · date" subtitle in center, net amount (green +X if owed, orange -X if you owe) on right. Expanded view shows clean split breakdown with PAID badge and action buttons.

---

## 6d. Bug Fixes & UX Improvements — June 2026 (Session 2)

### FIX — Dismiss banner reappears on Dashboard tab navigation
File: app/dashboard/page.tsx
Problem: `dismissedSwapRef` was `useRef(new Set())` — resets to empty on every component remount. Navigating away from Dashboard and back caused dismissed swap banners to reappear even though `markSwapRequestRead` had been called.
Fix: Lazy-initialize the ref from `sessionStorage` on each mount. Persist dismissed IDs to `sessionStorage` when user clicks dismiss. State survives tab navigation for the duration of the browser session.

### FIX — Editing an expense did not save
Files: store/useFlatStore.ts, firestore.rules, app/dashboard/expenses/page.tsx
Problem 1: No `updateExpense` function existed in the store — only `addExpense` and `deleteExpense`.
Problem 2: Firestore rules had `allow update: if false` for the expenses subcollection.
Problem 3: No edit button existed in the `ExpenseRow` expanded view — no way to trigger editing.
Problem 4: No try/catch in `ExpenseModal.handleSave` or `MonthlyBillModal.handleSave` — any error froze the modal permanently (saving state stuck true).
Fix: Added `updateExpense` to `useFlatStore` (writes to Firestore when flatId exists, optimistic update otherwise). Updated Firestore rule to `allow update: if isMember(flatId) && (resource.data.createdBy == request.auth.uid || isAdmin(flatId))`. Added `Edit` (Pencil) button to `ExpenseRow` expanded view (only shown to creator). Added `editExpense` state and wired the edit modal. Added try/catch to both modal `handleSave` functions with `setSaving(false)` on error. Deployed updated Firestore rules.

### REMOVED — Reliability Score card
File: app/dashboard/page.tsx
The Reliability Score stat card was removed from the Dashboard stats row (it was visible to members and in admin "mine" view). Feature deemed unnecessary for the current trial phase.

### FIX — Swaps missing from mobile navigation
File: app/dashboard/layout.tsx
Problem: Swaps was in the desktop sidebar but absent from the 5-slot mobile bottom nav. Members had no mobile access to the Swaps page.
Fix: For members, slot 4 now shows Swaps (replacing Members). For admins, slot 4 still shows Tasks. Swaps badge (pending swap count) shown in mobile nav when applicable.

---

## 6c. Expense Module Bug Fixes — June 2026 (All Fixed)

### CRITICAL — Firestore rules for expenses/settlements/recurringBills not deployed
The new subcollection rules were written to `firestore.rules` but never deployed. Firestore defaulted to DENY all access → "Missing or insufficient permissions" on every expense write.
Fix: Ran `firebase deploy --only firestore:rules --project garbage-f79f7`. Rules now live in production.

### HIGH — `note: undefined` rejected by Firestore
When adding an expense or settlement with no note, the optional `note` field was explicitly set to `undefined` via `form.note.trim() || undefined`. Firestore rejects explicit undefined values.
Fix: Added `fs()` helper at the top of `useFlatStore.ts` — strips all undefined-valued keys before any Firestore write. Applied to `addExpense`, `addSettlement`, `createRecurringBill`.

### MEDIUM — `createdBy: ''` in `generateBill` caused permissions failure
`generateBill` was passing `createdBy: useAuthStore.getState().user?.uid || ''`. If uid wasn't ready, `createdBy` became `''` which failed the rule `createdBy == request.auth.uid`.
Fix: Read uid once at function start, return early if not present, use directly as `createdBy: uid`.

---

## 6b. Second Audit Findings — June 2026 (All Fixed)

### CRITICAL — joinRequests subcollection had NO Firestore rules
File: firestore.rules
Problem: Firestore denies all access by default when no rule matches. The joinRequests subcollection had zero rules — every read/write was silently denied. The entire approval-mode feature (admin approves/rejects join requests) was completely broken in production.
Fix: Added full joinRequests rule block — admin reads all; requester reads their own; any authenticated user can create for themselves; only admin can update/delete.

### HIGH — deleteEntireFlatService orphaned joinRequests + npsResponses data
File: lib/flatService.ts
Problem: When the last admin deleted a flat, only ['members', 'tasks', 'activityLog', 'swapRequests'] were deleted. The joinRequests and npsResponses subcollections were left as orphaned documents in Firestore — permanently inaccessible dead data that accumulated over time.
Fix: Added 'joinRequests' and 'npsResponses' to the subcollections array in deleteEntireFlatService.

### HIGH — reassignMemberTasks assigned to wrong person on leave/kick
File: lib/flatService.ts
Problem: When a member left or was kicked and they were the current task assignee, the task was assigned to newQueue[0] — the FIRST person in the remaining queue — instead of the correct next person after the leaving member. Example: queue [A,B,C,D], B leaves → task went to A instead of C.
Fix: Captured the leaving member's index in the original queue, then used newQueue[leavingIndex % newQueue.length] to correctly select the next person in rotation.

### HIGH — kickMemberService had non-atomic member delete + counter decrement
File: lib/flatService.ts
Problem: deleteDoc(member) ran first, then updateDoc(memberCount--) separately. If the decrement failed, the flat's memberCount stayed permanently high. A flat at 8 members that kicks someone would still show memberCount=8, locking out all future join attempts with "flat is full."
Fix: Replaced the two sequential calls with a writeBatch commit — both the member deletion and the counter decrement now succeed or fail together atomically.

### MEDIUM — generateFlatId() used Math.random() for invite codes
File: lib/flatService.ts
Problem: Invite codes are 4 characters from a 32-character alphabet (32^4 = ~1M possibilities). Using Math.random() makes them predictable and brute-forceable — an attacker could enumerate all codes to find valid flats and auto-join them.
Fix: Replaced with crypto.getRandomValues() — cryptographically secure randomness, same pattern as the crypto.randomUUID() fix from the first audit.

### MEDIUM — activityLog listener fetched entire collection, sliced client-side
File: store/useFlatStore.ts
Problem: The Firestore listener subscribed to the full activityLog collection and sorted + sliced to 50 entries in JavaScript. Every real-time update triggered a re-read of ALL entries (potentially thousands as the flat aged), billing for every read and slowing load times.
Fix: Changed to query(collection(...), orderBy('timestamp', 'desc'), limit(50)) — Firestore now returns only the 50 most recent entries, sorted server-side. Read cost is O(50) regardless of total log size.

### LOW — fortnightly legacy frequency incorrectly treated as 7 days
File: lib/rotationEngine.ts
Problem: The vault documentation listed 'fortnightly' as a supported frequency, but it was dropped from the TypeScript type at some point. Any old tasks in Firestore with frequency: 'fortnightly' fell into the 'custom' fallback (7-day cycle) instead of the correct 14-day cycle — producing wrong due dates and wrong cycle labels.
Fix: Added explicit fortnightly handling in completeTask (14-day nextDueDate), getTaskDateInfo (1,209,600,000ms cycle), and getTaskUrgency (48-hour warning window, same as weekly).

---

## 7. Complete Feature Set (v0.3.0)

### Auth
Google Sign-In (one-tap) · Email/Password · Custom auth domain proxy (iOS Safari fix) · Session persistence · Minimal data (email + name only)

### Onboarding
Create flat (instant invite code) · Join flat via code · One-time setup · Join/create additional flats without logout · Approval-mode join (admin approves requests)

### Rotation Engine
Auto-assignment · Per-task rotation queue · Skip OOS members · Resume on return · Admin manual override · Custom start date · Frequencies: daily/weekly/fortnightly/monthly · Priority: low/medium/high

### Task Management
Admin creates/deletes/edits tasks · Mark done (single tap) · Overdue tracking + persistence · New member auto-added to all queues · Group tasks (multi-member, sub-task per person) · Temp (one-off) tasks outside rotation

### Swap System
Request swap · Accept/Decline · Persistent dashboard banner · Activity log tracking · Pending-swap badge on desktop sidebar · Swap Requests button on member Tasks page opens bottom sheet · Swap page: 4 stat chips (Sent/Received/Accepted/Declined) + admin All Swaps toggle (flat-wide list with inline accept/decline) · Dashboard swap summary widget (clickable, links to swaps page)

### Rotation Card
Full queue visibility · Position numbers · YOU badge · Due date + frequency display

### Admin Controls
My Tasks view · Org View (flat-wide) · Invite code panel · Member management · Role transfer · Month-end close flow

### Membership Management
Leave flat · Auto flat switch · Admin transfer requirement · Last-member flat deletion · Kick member · Kicked user redirect · Task reassignment on leave/kick (correct next-in-queue logic)

### Multi-Flat
Multiple flat memberships · FlatSwitcher (Gmail-style) · Instant switch · Join/create from inside dashboard

### Expenses & Splitwise (complete)
**Recurring Bills (now called Monthly Bills):**
- Admin configures monthly bills (Rent, WiFi, Water, Electricity, Gas, Maid etc.)
- Fixed or variable amount per month — chosen via 2-button radio selector (Fixed/Variable) in the creation modal
- Payer auto-rotates through a queue each month
- **Collector field**: separate from the payer — designates who collects each member's share. Defaults to admin, changeable any month via avatar card picker in the modal. Shown in bill card header ("X collects")
- Admin generates bills on/after billing date; variable bills prompt for actual amount
- Bill instances tracked with status: pending → split_generated → paid / skipped
- All payers (not just admin) can mark their bills paid
- Marking paid auto-creates a matching expense in the Daily Splits transaction list
- **Collection tracking**: per-member Received/Pending toggles on expanded bill instance (visible to collector + admin); `collectedFrom` map stored on instance; Firestore rule allows collector to update `collectedFrom` without admin role

**Daily Splits:**
- Ad-hoc expense log (like Splitwise) — anyone can add
- Equal split or fully custom per-person amounts
- 7 currencies: INR, USD, EUR, GBP, AED, SGD, AUD
- Banking-style transaction list: category emoji + description + payer·date + net amount
- Edit expense (creator or admin) · Delete (optimistic — instant UI update)
- Deferred expenses (carry to next month)

**Balances & Settlement:**
- Direct pairwise balance calculation — only real transaction pairs shown, no MCF chains
- Per-person balance cards: green (they owe you) · orange (you owe them)
- Expand card to see full breakdown of which expenses make up that balance
- **Settle** button (debtor side): opens settle modal, pay full or partial amount
- **Mark Received** button (creditor side): record that the other person paid you
- Settle modal: quick-fill "Pay full / Pay half" chips · remaining-after-payment preview · partial payment label on CTA · try/catch prevents freeze on error
- Balances collapsed by default on mobile — compact strip shows net status, tap to expand
- Person filter: filter transaction list to show only shared history with one specific person
- Settlement history visible in the combined transaction timeline

**Monthly Summary:**
- Dark summary card showing total monthly spend
- Progress bar: Fixed Bills (amber) vs Daily Splits (blue)
- Two stat tiles with amounts and percentages
- Month-end close: admin locks the month, balances carry forward to next cycle
- Carry-forward balances shown as a notice at top of next month's list

### Analytics
Completion grid · Reliability scores · Per-task breakdown · Flat-level aggregate

### Calendar
Monthly view · Member filtering · Completed vs pending distinction

### Activity Log
Full audit trail · Real-time · Flat-wide visibility

### Real-Time
Firestore onSnapshot on all data · No refresh needed · Offline detection

### UI & Navigation
Dark/light mode · Bottom nav mobile (5-slot with radial FAB: Dashboard · Expenses · [FAB] · Tasks · Profile — same layout for admin and member) + sidebar desktop · Fully responsive · No install required · Turbopack fast loading · Pending badge counts on nav items · Radial Quick-Add FAB (Split expense / Bill / Task for admin; Split only for member) · Member Tasks page has read-only view with compact tap-to-expand cards and swap bottom sheet

---

## 8. User Flows

### Flow 1 — Admin Creates Flat
Open app → Sign in → Onboarding → Create flat → Enter name → Invite code generated → Dashboard → Share code → Create tasks → Done

### Flow 2 — Member Joins
Open app → Sign in → Onboarding → Join flat → Enter invite code → Auto-added to all queues → Dashboard

### Flow 3 — Daily Use
Open app (30–60s) → See "Your Tasks" → Complete task → Tap Mark Done → Next person auto-assigned → Activity log updates for all

### Flow 4 — Swap Request
See task can't do → Request Swap → Select flatmate → Banner appears for all → Flatmate accepts/declines → Task reassigned or stays → Activity log records outcome

### Flow 5 — Admin Org View
Toggle Org View → All tasks all members → See completed/pending/overdue → Manual override → Add/remove tasks → Check member reliability

### Flow 6 — Member Leaves
Settings → Danger Zone → Leave flat → Confirmation
- Not admin: removed → tasks reassigned → switch to next flat or go to onboarding
- Admin with others: must transfer role first → leave as regular member
- Admin last member: warning → confirm → entire flat deleted

### Flow 7 — Admin Kicks Member
Members page → Remove → Confirm → Member removed → Tasks reassigned → Activity log → Kicked user sees onboarding on next open

### Flow 8 — Multi-Flat Switch
Tap FlatSwitcher → Dropdown shows all flats → Tap target flat → All data reloads instantly → No page reload, no logout

### Flow 9 — Add & Split an Expense
Expenses Hub → Daily Splits tab → (+) FAB or Add button → Enter description, amount, category, date → Choose equal or custom split → Select who is included → Save → Appears in transaction list instantly → Balances update immediately

### Flow 10 — Setting Up Monthly Bills
Expenses Hub → Monthly Bills tab → Add Monthly Bill → Enter name, category → Select Fixed or Variable amount type → Enter amount (if fixed) → Set billing day → Choose who splits → Pick Collector (avatar card, default = admin) → Save → Admin generates on billing date → Bill instances created with split amounts → Collector marks each member as Received/Pending as they pay → Each payer marks their bill paid → Auto-logged in Daily Splits

### Flow 11 — Settling a Balance
Expenses Hub → Tap balance strip (compact) → Balances expand → See "You owe Rahul ₹2,000" → Tap "Settle" → Quick-fill "Pay full" or enter partial → Add note (UPI/cash) → Mark as Paid → Balance recomputes instantly

### Flow 12 — Recording Cash Received (Creditor Flow)
Expenses Hub → Balance strip → Expand → "Rahul owes you ₹3,000" → Tap "Mark Received" → Enter amount received → Save → Settlement recorded from Rahul's side → Balance updates

### Flow 13 — Investigating a Balance
Expenses Hub → Balance strip → Expand → Tap "Rahul · owes you · 3 transactions" card → Card expands → See list of 3 expenses with amounts and dates → Tap "Filter list by person" → Daily Splits tab filters to show only shared history with Rahul

---

## 9. Current Status & Known Limitations

### Live and Working
Smart rotation ✅ · Google + email login ✅ · Real-time sync ✅ · Mobile UI ✅ · Security audit complete ✅ · Multi-flat ✅ · Membership management ✅ · 8-member cap ✅ · Analytics ✅ · Calendar ✅ · Activity log ✅ · Swap system ✅ · Dark mode ✅ · NPS banner ✅ · PWA ✅ · Recurring Bills ✅ · Daily Splits / Expenses ✅ · Balances & Settlement ✅ · Month-end close ✅ · Privacy Policy ✅ · Terms of Service ✅

### Known Limitations

| Limitation | Impact | Fix Phase |
|-----------|--------|-----------|
| No push notifications | Must open app to see updates | Phase 2 |
| No offline mode | Needs internet | Phase 3 |
| No native mobile app | Web only | Phase 3 |
| No password reset UI | Firebase default email | Phase 2 |
| No task photo proof | No visual verification | Phase 2 |
| No flat announcements | No admin broadcast | Phase 2 |
| No task history archive | Older than 30 days not viewable | Phase 2 |
| Expense multi-currency balances | Cross-currency balances shown separately, no conversion | Phase 2 |
| No settlement confirmation from recipient | Either side can record a settlement; no double-confirmation | Phase 3 |
| Admin-only flat-wide balance view | Admin cannot see all member-to-member balances in one matrix | Phase 2 |

---

## 10. Roadmap — Future Requirements

### Phase 1 — Trial (Now → 3 Months) CURRENT
Goal: Validate with 5–20 real flats.
- [x] All core features built
- [ ] Collect user feedback (ongoing)
- [ ] Fix bugs from real usage (ongoing)
- [ ] Track trial metrics (ongoing)

---

### Phase 2 — Growth (3–6 Months)
Goal: 100+ active flats. Add features users asked for.

**Done (June 2026 — Session 2026-06-16):**
- ✅ **Mobile nav unified** — Profile in slot 5 for both admin and member. Swaps removed from member mobile nav; accessed via button on Tasks page instead
- ✅ **Member Tasks page** — Read-only view with compact cards (tap to expand rotation details) + Swap Requests bottom sheet button at top
- ✅ **Leave flat redirect bug fixed** — Now correctly switches to next flat (if one exists) instead of always going to /onboarding
- ✅ **"Fixed Bills" → "Monthly Bills"** — All UI labels updated; data model unchanged
- ✅ **Payer collection tracking** — Per-member Received/Pending toggles on bill instances; `collectedFrom` map on `BillInstance`; Firestore rule allows payer to update collection status
- ✅ **Swap page redesign** — 4 stat chips + admin "All Swaps" toggle with flat-wide list
- ✅ **Dashboard swap widget** — Clickable summary card on home page showing sent/received/accepted/declined counts
- ✅ **Bill Collector field** — `collectorId` on `RecurringBill` + `BillInstance`; avatar card picker in modal; collector shown in bill card header; Firestore rule allows collector to update `collectedFrom`
- ✅ **MonthlyBillModal redesign** — 2-button Fixed/Variable selector (replaced disconnected toggle), avatar card collector picker (replaced plain select), header text corrected

**Done (June 2026 — Session 2026-06-13, Part 2):**
- ✅ **Privacy Policy** — `app/privacy/page.tsx`. DPDP Act 2023 compliant. Covers: data collected (email, name, photo URL, tasks, expenses, activity logs), lawful basis per DPDP, third-party processors (Firebase, Vercel, WhatsApp/FCM planned), cross-border data transfer disclosure, user rights (access, correction, erasure, grievance redressal, nomination), Grievance Officer designation. Static page, no auth required.
- ✅ **Terms of Service** — `app/terms/page.tsx`. Covers: eligibility (18+), what Habitiq does and does not do (no payment processing), free service terms, user and admin responsibilities, flat membership/removal rules, dispute framing (Habitiq is not an arbiter), governing law (India, Hyderabad courts), liability cap (₹0 free service), 60-day discontinuation notice. Static page, no auth required.
- ✅ **Public route bypass in AuthProvider** — `components/AuthProvider.tsx`. `/privacy` and `/terms` added to `isPublicPage` check — unauthenticated users can access both pages without being redirected to login.
- ✅ **Landing page footer wired** — `app/page.tsx`. Footer Company links updated: Privacy → `/privacy`, Terms → `/terms`, Contact → `mailto:hello@habitiq.in` (was all `#` placeholders).
- ✅ **Settings page legal links** — `app/dashboard/settings/page.tsx`. Privacy Policy · Terms of Service links added to the About card, visible to all logged-in users.
- ✅ **G1 gate progress** — Privacy Policy and Terms of Service are live at `/privacy` and `/terms`. Remaining G1 items: buy habitiq.in domain, rename Vercel project, founder agreement with Bhanu.

**Done (June 2026 — Session 2026-06-13, Part 1):**
- ✅ **Expense delete enabled** — `canDelete={false}` was hardcoded in the Daily Splits transaction list (`app/dashboard/expenses/page.tsx`). Changed to `canDelete={item.data.createdBy === currentUserId || !!isAdmin}`. Creators and admins can now delete expenses via the "Remove" button in the expanded row. `deleteExpense` updated to accept optional `actorId` so the activity log records WHO deleted the expense. All monthly totals (summary card, dashboard widget) already updated reactively — the only missing piece was the button being hidden.
- ✅ **Settlements collapsed by default** — Settlement rows in the Daily Splits transaction list now collapse into a compact tap-to-expand divider: `── ✓ Settled · ₹5,000 · 3 payments ──` with a chevron icon. Tapping expands to show individual `SettlementRow` entries. Each month group has independent open/close state via `expandedSettlements: Set<string>`. Eliminates excessive scrolling when there are many past settlements.
- ✅ **Fixed bills dashboard persisting after delete** — Two-layer fix: (1) `deleteRecurringBill` now uses `writeBatch` to atomically delete the template + all linked bill instances + all linked expenses in a single Firestore commit, preventing intermediate `onSnapshot` re-additions (race condition). (2) `fixedBillsThisMonth` on the dashboard now cross-references against `activeTemplateIds` — orphaned bill instances from deleted templates are silently ignored.
- ✅ **Erase All Expense Data (admin-only)** — New Danger Zone section at the bottom of the Expenses page. Admin taps "Erase All Expense Data" → confirmation modal listing all 5 data types (daily splits, settlements, fixed bills, bill instances, month close records) → "Erase Everything" button. Implemented as `resetAllExpensesData()` in `useFlatStore`: optimistic local clear first, then `getDocs` + `writeBatch` in 400-doc chunks for each subcollection (`expenses`, `settlements`, `recurringBills`, `billInstances`, `monthCycles`). For test data cleanup before production launch.

**Done (June 2026 — prior sessions):**
- ✅ PWA — manifest.json, service worker, offline fallback, Android install prompt, dynamic PNG icon route
- ✅ Settings IA overhaul — "Your Flats" card, active flat context in heading, Danger Zone names the flat
- ✅ Bills & Expenses module (full Splitwise-class feature):
  - Recurring Bills: rotation payer, fixed/variable amounts, generate flow, mark paid (all payers), auto-creates expense in transaction list on mark paid
  - Daily Splits: ad-hoc expenses, banking-style transaction list, equal/custom split, 7 currencies, edit/delete
  - Balances: direct pairwise algorithm (no MCF chains), per-person cards with expand to see contributing expenses
  - Settlement: bidirectional — debtor settles, creditor marks received; partial payment with quick-fill chips and remaining preview; try/catch on all modals
  - Person filter: filter transaction list to one person's history
  - Monthly summary card: total spend broken into Fixed Bills vs Daily Splits with progress bar
  - Balance strip: collapsed by default on mobile, compact net status, tap to expand full cards
  - Month-end close: admin locks month, balances carry forward
  - Firestore: full subcollection rules deployed; optimistic UI updates throughout
- ✅ Group tasks and Temp tasks added to task management
- ✅ Radial FAB in mobile nav (Quick Add: Split / Bill / Task)
- ✅ Swap badge on desktop sidebar counts all pending flat swaps
- ✅ Tasks page mobile: Swap Requests shortcut banner with live pending count

**High Priority (remaining):**
- Push notifications (Firebase Cloud Messaging)
- WhatsApp integration (task reminders via WhatsApp API)
- Admin flat-wide balance matrix view

**Medium Priority:**
- Task photo proof (upload photo on mark done — needs Firebase Storage)
- Guest invite via shareable link (not just invite code)
- Password reset UI (in-app flow)
- Member nickname editing
- Settlement confirmation from recipient ("I confirm I received this")

**Low Priority:**
- Task history archive (>30 days)
- Flat announcements (admin pinned message)
- Task templates (common preset tasks)
- Expense receipt photo attachment

---

### Phase 3 — Scale & Monetisation (6–18 Months)
Goal: Sustainable business. Serve co-living operators and PG owners.

| Feature | Description |
|---------|-------------|
| Subscription billing | Stripe / Razorpay in-app payment |
| Admin super-dashboard | All properties in one view |
| Automated reminders | Email + push + WhatsApp, configurable |
| Reliability score rewards | Badges, streaks, leaderboards |
| Analytics export | CSV/PDF reports for PG owners |
| API for integrations | Connect to property management tools |
| Custom branding | White-label for co-living operators |
| Native mobile app | React Native iOS + Android |
| Offline mode | Full offline + background sync |
| Firebase Cloud Functions | Business logic server-side |

### Technical Scale Changes Needed

| Trigger | Action |
|---------|--------|
| >100 active flats | Upgrade Firebase to Blaze |
| >300 build mins/month | Upgrade Vercel plan |
| Needing server logic | Add Firebase Cloud Functions |
| 1,000+ flats | Redis/Upstash caching layer |
| User-uploaded photos | Firebase Storage + CDN URLs |
| Abuse prevention | Firebase App Check + rate limiting |

---

## 11. Business Model & Monetisation

### Freemium (Recommended for Phase 3 Launch)

| Tier | Price | Limits | Target |
|------|-------|--------|--------|
| Free | Rs.0/month | 6 members, 10 tasks, core features | Student flats, trial users |
| Pro | Rs.99/flat/month | Unlimited members + tasks, push notifications, photo proof, analytics export | Active flats, working professionals |
| Business | Rs.499/property/month | Multiple flats, super-dashboard, white-label, priority support | PG owners, co-living operators |

### Revenue Projections (Conservative)

| Scale | Flats | Conversion | Monthly Revenue |
|-------|-------|-----------|-----------------|
| Phase 2 end | 100 | 10% Pro | ~Rs.1,000/month |
| Phase 3 start | 500 | 15% Pro | ~Rs.7,500/month |
| Phase 3 growth | 2,000 | 20% Pro + 5 Biz | ~Rs.40,000+/month |
| Scale | 10,000 | 20% Pro + 50 Biz | ~Rs.2,25,000+/month |

### Why Freemium Works
- Network effect: one person joins → invites all 4–6 flatmates
- Each flat is a mini acquisition funnel — one champion recruits the entire flat
- B2B (PG owners) has high willingness to pay

---

## 12. Go-to-Market Strategy

### Phase 1: Organic (Now)
Founders share personally. Ask early adopters to share with one other flat. LinkedIn posts. College alumni groups, PG Facebook groups.
**Goal: 20 active flats in 3 months**

### Phase 2: Content & Community (3–6 Months)
Build-in-public posts. Problem-awareness content. User testimonials. Partner with college housing groups. Micro-influencer outreach (urban living creators).

### Phase 3: Paid & B2B (6+ Months)
Facebook/Instagram ads (18–28, metro cities). Google Search ads ("roommate chore tracker India"). Direct B2B outreach to Stanza Living, NestAway, CoLive. App Store listing.

### The Growth Loop
1. One person joins → invites all flatmates → all sign up
2. Each flatmate lives in other flats → introduces Habitiq there
3. PG owners see what their tenants use → adopt for all properties
**Structural growth loop built into the product.**

---

## 13. Success Metrics

### Trial Phase (Months 1–3)
| Metric | Target |
|--------|--------|
| Active flats | 5–20 |
| Members per flat | ≥3 active logins/week |
| Task completion rate | ≥80% |
| Overdue rate | <20% |
| 30-day flat retention | >60% |
| Avg session length | >2 minutes |

### Growth Phase (Months 3–6)
Total active flats: 100 · Total users: 500+ · Monthly recurring: 70% · NPS: >40 · Referral-acquired flats: >50%

### Scale (Months 6–18)
Active flats: 2,000+ · Monthly revenue: Rs.40,000+ · Pro conversion: ≥15% · B2B clients: 5+ properties

---

## 14. Infrastructure & Costs

### Current: Rs.0/month
| Service | Plan | Capacity |
|---------|------|----------|
| Vercel | Hobby (Free) | 6,000 build mins, global CDN |
| Firebase Auth | Spark (Free) | 10,000 MAU |
| Firestore | Spark (Free) | 50K reads/day, 20K writes/day |
| Firebase Storage | Spark (Free) | 5GB |

Handles ~100 active flats (600 users) for free.

### Firebase Cost at Scale (Blaze)
100 flats: ~$0–5/month · 500 flats: ~$10–25 · 2,000 flats: ~$50–100 · 10,000 flats: ~$200–400

---

## 15. Product Principles

1. **Fairness is the product.** Every feature exists to make shared living more fair.
2. **Zero friction onboarding.** Flat up and running in under 2 minutes.
3. **Transparency by default.** All members see everything — visibility is the accountability mechanism.
4. **The system is the authority.** Nobody should have to nag.
5. **Mobile first, always.** Most users open the app on their phone standing in the kitchen.
6. **Real-time or nothing.** If changes aren't instant, trust breaks down.

---

## 16. Competitive Landscape

| Product | What They Do | Habitiq's Gap |
|---------|-------------|---------------|
| Splitwise | Expense splitting | Doesn't touch duties |
| Todoist/Notion | Individual tasks | No shared rotation, no auto-assignment |
| WhatsApp | Messaging | No accountability, messages buried |
| OurHome | Western chore app | Not in India, not PG context |
| Tody | Cleaning tracker | No multi-person rotation |

**Habitiq: Only purpose-built, real-time, rotation-first shared living tool for India.**

---

## 17. Trial Feedback Template

1. How often do you open Habitiq? (Daily / Few times a week / Weekly / Rarely)
2. Has chore fairness improved in your flat? (Yes / Somewhat / No)
3. What is the one thing you wish Habitiq could do that it currently cannot?
4. Would you recommend Habitiq to another flat? (Yes / Maybe / No)
5. Would you pay Rs.49/month to keep using it? (Yes / Maybe / No)

---

*"Your flat, on autopilot. Built in India. For everyone who has ever had a flatmate."*

**Document version:** 1.0 | **Source:** C:\garbage | **Vault:** C:\garbage\project_1 | **June 2026**
**Maintained by:** [[About Sai]]
