# Habitiq — Production Status Document
**Date:** 26 June 2026  
**Version:** v0.4.3  
**Live URL:** https://habitiq.app  
**Repo:** github.com/saijaswanthedupuganti-cmyk/flatflow  
**Status:** Open Beta — Live with Real Users  

---

## 1. What Is Habitiq

Shared living management platform for flatmates. Automates duty rotation, tracks expenses, handles bill splits, and eliminates the "whose turn is it?" argument.

**Target users:** Students, working professionals, and PG residents in shared accommodation across India.

**Co-founders:**
- Venkata Sai Jaswanth E — UI/UX Design
- Upputuri Bhanu Kalyan — Full-Stack Engineering

---

## 2. Current Production Stack

| Layer | Technology |
|-------|-----------|
| Framework | Next.js 16.2.6 (React 19, App Router, Turbopack) |
| Language | TypeScript (strict mode) |
| Styling | Tailwind CSS v4 |
| Animation | Framer Motion |
| State | Zustand v5 + Firebase Firestore onSnapshot |
| Auth | Firebase Auth — Google Sign-In + Email/Password |
| Database | Firebase Firestore (real-time, subcollection model) |
| Hosting | Vercel — auto-deploy on push to master |
| PWA | Progressive Web App — works without App Store |
| Middleware | Custom proxy.ts for Firebase auth domain (Safari fix) |

---

## 3. Live Features (as of 26 June 2026)

### Authentication
- Google one-tap sign-in
- Email/password sign-in + forgot password flow (email enumeration safe)
- Custom auth domain proxy (fixes iOS Safari popup block)
- Session persistence across reloads

### Onboarding
- Mobile: full continuity flow — Choose screen → Create/Join as separate steps with matching hero images and copy
- Desktop: premium two-panel layout (full-viewport image left, form right)
- Create Flat: flat name only — nickname auto-set from Google profile
- Join Flat: nickname + invite code — invalid code caught before any Firestore call
- Multi-flat: join or create additional flats without logging out
- Approval-mode join: admin approves/rejects join requests before member is added
- Flat capacity cap: 8 members

### Task & Rotation Engine
- Admin creates/edits/deletes tasks with priority (low/medium/high) and frequency (daily/weekly/fortnightly/monthly)
- Auto-assignment via per-task rotation queue
- Smart skip: OOS (out-of-schedule) members skipped; resume automatically on return
- Admin manual override
- Overdue tracking — persists across sessions
- Group tasks: multi-member tasks with per-person sub-tasks
- Temp (one-off) tasks outside normal rotation
- New member auto-added to all existing rotation queues

### Swap System
- Any member can request a swap with a flatmate
- Accept / Decline flow with persistent dashboard banner
- Activity log records every outcome
- Pending swap badge on desktop sidebar (counts all pending flat swaps)
- Mobile: Swap Requests button on Tasks page opens a bottom sheet (no nav slot needed)
- Swap page: 4 stat chips (Sent · Received · Accepted · Declined) + admin "All Swaps" toggle
- Dashboard swap summary widget linking to swaps page

### Membership Management
- Leave flat (member or admin)
- Admin must transfer role before leaving if others remain; last admin deletes entire flat
- Kick member (admin only) — atomic batch: remove member + decrement count
- Task reassignment on leave/kick: correct next-in-queue person (not index 0)
- Kicked user sees onboarding on next open
- Leave flat redirect: correctly switches to next flat if one exists

### Multi-Flat Support
- User can belong to multiple flats simultaneously
- Gmail-style FlatSwitcher in the dashboard — instant data reload on switch
- Join/create additional flats from inside the dashboard without logout

### Expenses & Bill Splitting (Full Splitwise-Class Feature)

**Monthly Bills:**
- Admin configures recurring bills (Rent, WiFi, Water, Electricity, Gas, Maid, etc.)
- Fixed or Variable amount — chosen via radio selector (Fixed shows ₹ input; Variable shows amber info card)
- Payer auto-rotates through a queue each month
- Collector field: separate from payer — avatar card picker in modal; defaults to admin
- Admin generates bills on/after billing date; variable bills prompt for actual amount
- Bill statuses: pending → split_generated → paid / skipped
- Any payer (not just admin) can mark their bill paid
- Collection tracking: per-member Received/Pending toggles visible to collector + admin
- `collectedFrom` map on each bill instance stored in Firestore

**Daily Splits:**
- Ad-hoc expense log — anyone can add
- Equal split or fully custom per-person amounts
- 7 currencies: INR, USD, EUR, GBP, AED, SGD, AUD
- Banking-style transaction list: category emoji + description + payer·date + net amount
- Edit (creator or admin) and delete (optimistic — instant UI update)
- Deferred expenses: carry to next month

**Balances & Settlement:**
- Direct pairwise balance algorithm — no debt-chain simplification (MCF removed)
- Per-person balance cards: green (they owe you) · orange (you owe them)
- Expand card to see full breakdown of contributing expenses
- Settle button (debtor): full or partial payment with quick-fill chips and remaining preview
- Mark Received button (creditor): record cash/UPI payment from the other side
- Balances collapsed by default on mobile; compact strip shows net status, tap to expand
- Person filter: filter transaction list to one person's shared history

**Monthly Summary:**
- Dark summary card showing total monthly spend
- Progress bar: Monthly Bills (amber) vs Daily Splits (blue) with stat tiles
- Month-end close: admin locks month, balances carry forward

### Rewards Wallet
- Every task completion earns a brand coupon (no streak requirement)
- Anti-abuse: retroactive completions excluded (>2h ago); 1 reward per 24h per device
- Dynamic reward pool from Firestore `/rewardPool/{id}` — update coupon codes in Firebase Console to push to all users
- RewardUnlockModal: center-screen celebration overlay with scale spring, confetti, brand name, and CTA
- RewardsWallet in Profile: scratch-card design cards, reveal code on tap, Copy button, redeemed state
- localStorage as primary storage (Firestore sync is best-effort to avoid permission failures)

### Voice Assistant (Beta)
- Launched in production (commit ab212f0)
- NLU pipeline with 11 action handlers
- Works on desktop Chrome; Android Chrome fixes deployed
- TTS audio unlock via user gesture preserved
- Waveform visualizer
- Known: pending production TTS test on mobile Safari

### UI & Navigation
- Dark mode (default) with light mode toggle
- Mobile: 5-slot bottom nav — Dashboard · Expenses · [FAB] · Tasks · Profile (same for admin and member)
- Desktop: full sidebar
- Radial Quick-Add FAB: Split expense / Bill / Task (admin); Split only (member)
- Pending badge counts on nav items (swap badge, task overdue indicator)
- Member Tasks page: read-only with compact tap-to-expand cards and Swap Requests bottom sheet
- Admin: My Tasks view + Org View (flat-wide, all members)

### Analytics & Visibility
- Completion grid per member
- Reliability scores per member
- Per-task breakdown
- Calendar view: monthly, member filtering, completed vs pending
- Activity log: full audit trail, real-time, flat-wide

### Compliance & Legal
- Privacy Policy at `/privacy` (DPDP Act 2023 compliant)
- Terms of Service at `/terms` (India, Hyderabad courts)
- Public route bypass — unauthenticated users can read both
- Contact: hello@habitiq.app
- Settings page includes links to both legal pages

---

## 4. Security Posture

Two full security audits completed. All critical/high issues fixed:

| Severity | Issue | Status |
|----------|-------|--------|
| CRITICAL | Swap Firestore rule: any member could update any swap | Fixed |
| CRITICAL | joinRequests had zero Firestore rules | Fixed |
| HIGH | Wrong payer shown in bill instances (index off-by-one) | Fixed |
| HIGH | kickMember: non-atomic member delete + counter decrement | Fixed |
| HIGH | reassignMemberTasks: assigned to index 0, not next in queue | Fixed |
| HIGH | deleteEntireFlatService: orphaned joinRequests + npsResponses | Fixed |
| HIGH | activityLog listener fetched full collection on every update | Fixed |
| HIGH | Editing an expense had no updateExpense function + no Firestore rule | Fixed |
| MEDIUM | No HTTP security headers | Fixed (6 headers in next.config.ts) |
| MEDIUM | Auth errors exposed Firebase codes (user enumeration) | Fixed |
| MEDIUM | generateFlatId used Math.random() for invite codes | Fixed |
| LOW | Math.random() for ID generation | Fixed (crypto.randomUUID()) |
| LOW | Google icon from external CDN | Fixed (saved locally) |
| LOW | No input length limits | Fixed |

**Still pending (non-blocking):**
- Firebase API Key restrictions in Firebase Console
- Password strength UX hint
- Content Security Policy (CSP)
- Firebase App Check for rate limiting

---

## 5. Firestore Data Model

```
/flats/{flatId}
  ├── memberCount, inviteCode, adminId, name, approvalRequired
  ├── /members/{uid}
  ├── /tasks/{taskId}
  ├── /swapRequests/{swapId}
  ├── /activityLog/{logId}        ← last 50 only, ordered by timestamp desc
  ├── /expenses/{expenseId}
  ├── /settlements/{settlementId}
  ├── /recurringBills/{billId}
  │     └── BillInstance fields: status, paidBy, collectedFrom, collectorId, paidAt
  ├── /billInstances/{instanceId}
  ├── /monthCycles/{cycleId}
  ├── /joinRequests/{requestId}
  └── /npsResponses/{responseId}

/rewardPool/{id}                  ← brandName, discountCode, description, isActive, expiryDays
/users/{uid}/rewards/{rewardId}   ← owner-only read+create; update restricted to isRedeemed
```

---

## 6. Recent Commits (Last 30)

| Hash | Description |
|------|-------------|
| `a8f8089` | feat: landing page redesign — new logo, flat finder, mobile fix, loading animation |
| `7947be8` | fix: Android Chrome mic — differentiate error types and show actionable fix guide |
| `6105b43` | fix: voice assistant — 4 root causes behind no response on mobile/desktop |
| `84f3461` | fix: recalculate splits on member bill approval when admin edits amount/payer |
| `7f6de15` | feat: member-submitted bill cards — inline edit before admin approve |
| `192dcc9` | fix: make startListening fully synchronous to preserve Chrome gesture context |
| `edaf950` | fix: voice mic race condition on Android Chrome |
| `3d309bc` | fix: add missing useVoiceContext and voice analytics modules |
| `1d345b9` | feat: show member-submitted bills inline in the generate pipeline list |
| `ea9390e` | feat: member-submitted bills require admin approval before entering pipeline |
| `60593cc` | fix: block future-dated expenses in Daily Splits form |
| `cf35c17` | fix(bug3): allow members to create one-time bill instances they personally paid |
| `6bfa514` | fix: member bill collection toggle + payer outstanding calculation |
| `4302fd1` | fix: move setState to after recognition.start() to preserve user gesture |
| `102e334` | fix: not-allowed error shows blocked banner only when mic is actually denied |
| `a4f5f9b` | fix: handle browser mic blocked state with visual banner |
| `ff81e35` | fix: voice overlay no longer blocks tasks after response |
| `38ee2ba` | fix: gate subscription coupon form to admin-only |
| `ab212f0` | Fix voice pipeline: move TTS+callback out of setState, fix not-allowed race, always speak responses |
| `b22053d` | Fix voice conversational feedback: always speak responses, fix overlay JSX |
| `5fd80a7` | fix: voice — synchronous permission flow + instant overlay on tap |
| `a388824` | fix: mic permission modal + waveform canvas fallback for older Safari |
| `bf3e97f` | feat: voice assistant — NLU pipeline, 11 action handlers, waveform, context fix |

---

## 7. What Was Shipped This Session (26 June 2026)

### Landing Page Full Redesign
- **New logo everywhere:** `habitiq-app-icon.png` (purple gradient interlocking-H rounded square) in navbar desktop, navbar mobile, footer, and loading screen — replaced old gradient div + text "H"
- **Mobile phone mockup fix:** PhoneFrame fixed at 264px width + CSS `transform: scale()` at 4 breakpoints with compensating negative `margin-bottom` so layout space matches visual height
- **Flat Finder feature added across entire page:** hero badge, headline, marquee, features section (first chapter), FAQ (new Q first), GetStarted bullets, phone screen mockup (`FlatFinderScreen` component)
- **Brand color unified:** all landing CTAs switched from BLUE (#2563EB) to PURPLE (#7C3AED); app screen mockups remain blue (faithful to actual app)
- **Copy overhaul:** hero headline "Find your flat. / Run it without drama.", GetStarted "Find your flat. Then run it right.", FAQ rewritten, professional and concise throughout
- **SCREEN_IDS expanded** from 3 to 4: `['dashboard', 'flatfinder', 'expenses', 'swaps']`

### Loading Screen Redesign
- Logo-inspired CSS keyframe animation: 3 interlocking panel shards animate in from different angles (echoing the app icon design)
- `habitiq-app-icon.png` at 76×76 with spring bounce + one-shot shimmer sweep
- 3 orbiting violet dots at 120° spacing, orbit radius 76px, 2.2s period
- Ambient glow blob + orbit ring + "Getting your flat ready…" tagline

---

## 8. Known Issues in Production

| Issue | Severity | Notes |
|-------|----------|-------|
| JoinRequests listener Firestore permission error | Low | Console error only; joinRequests feature works — the listener fires before auth is fully resolved on some sessions |
| Voice TTS on mobile Safari | Medium | Not yet confirmed in prod — works in Chrome |
| No push notifications | Medium | Phase 2 item; users must open app to see updates |
| Cross-currency balance display | Low | Multi-currency balances shown separately, no FX conversion |

---

## 9. Current Phase

**Phase 1 — Trial (Now → 3 Months)**

Goal: Validate with 5–20 real flats.

- All core features built ✅
- Real users on the platform ✅
- Collecting feedback (ongoing)
- Bug fixing from real usage (ongoing)

---

## 10. Next Priorities (Phase 2)

**High:**
- Push notifications (Firebase Cloud Messaging)
- WhatsApp integration (task reminders)
- Admin flat-wide balance matrix view

**Medium:**
- Task photo proof (Firebase Storage)
- Guest invite via shareable link
- Member nickname editing
- Settlement confirmation from recipient
- Flat Finder / Member Discovery feature (finding flatmates or flats)

**Low:**
- Task history archive (>30 days)
- Flat announcements (admin pinned message)
- Expense receipt photo attachment

---

## 11. Business Model

**Current:** Free — 100% of features available at no cost.

**Planned (Phase 3):**

| Tier | Price | Target |
|------|-------|--------|
| Free | ₹0/month | Student flats, trial users (6 members, 10 tasks) |
| Pro | ₹99/flat/month | Active flats, working professionals (unlimited) |
| Business | ₹499/property/month | PG owners, co-living operators (multi-flat, white-label) |

**Revenue model also includes:** Ad targeting via expense category spend data — the app knows what each user spends on (groceries, utilities, cleaning) enabling high-signal brand partnerships via the Rewards Wallet.

---

## 12. Deployment

- **Hosting:** Vercel (auto-deploy on push to `master` branch)
- **Domain:** `habitiq.app` — canonical; all other domains 301 redirect here
- **Database:** Firebase Firestore (Spark plan; upgrade to Blaze when >100 active flats)
- **Auth:** Firebase Authentication
- **Environment:** `.env.local` (Firebase config keys)
- **PWA:** Manifest + service worker — users can install from browser on Android and iOS, no App Store needed
