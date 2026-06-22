# Habitiq — Master Product Document
**Version:** 2.0 | **Date:** June 2026 | **Status:** Live, Trial Phase

> This is the single source of truth for all Habitiq presentations, pitches, investor conversations, design decisions, and future planning. Keep this document updated as the product evolves.

---

## 1. What Is Habitiq?

Habitiq is a **shared living management platform** that automates duty rotation among flatmates. It gives every flat a transparent, real-time system for chore management — eliminating arguments, forgotten tasks, and the awkward "whose turn is it?" conversation.

**The name:**
- **Habitat** — the shared space where people live together
- **Habit** — the recurring routines of shared living that repeat and rotate
- **IQ** — the intelligence that skips absent members, reassigns overdues, tracks reliability

**Live at:** https://habitiq.app *(moving to habitiq.in / habitiq.app)*

---

## 2. The Problem

Shared living breaks down in predictable ways:

| The Situation | What Actually Happens |
|---|---|
| Verbal chore assignments | Forgotten by Tuesday |
| WhatsApp reminders | Ignored, or causes tension |
| "Your turn" arguments | Nobody agrees on the count |
| Friend going out suddenly | Task skipped, no coverage |
| Group goes out together | Everything piles up, no accountability |
| New person joins the flat | Nobody updates the roster |

**Nobody wants to be the flat manager.** But without a system, someone always ends up being one — resentfully.

---

## 3. Who It's For

### Primary Audience
**Friend groups sharing a flat** — 2 to 8 people, age 18–30, living together in rented accommodation in Indian metro cities (Hyderabad, Bengaluru, Pune, Chennai, Mumbai, Delhi).

### Situations Where Habitiq Is Essential

**The Sudden Absence:** One person gets caught in the rain, travels last minute, or goes out of station for a few days. Without Habitiq, their tasks pile up. With it — the system automatically skips them and reassigns, then puts them back when they return.

**The Group Outing:** Everyone goes out together for a weekend. Tasks due that day get marked, coverage gets requested, no confusion about who owes what when everyone returns.

**The New Joiner:** A friend moves in mid-month. Without a system, nobody knows when they "start" on duties. Habitiq adds them to every rotation queue instantly.

**The Passive Flatmate:** One person never initiates but will follow a clear system. Habitiq makes the system the authority — so no one has to nag.

**The Organised Flatmate:** The person who tracks everything in their head, frustrated that others don't. Habitiq removes their burden and makes fairness visible to everyone.

### Who It Is NOT For
- Hotel or hostel staff systems
- Cleaning services or commercial property management
- Corporate office management

---

## 4. Brand Identity

### Name & Logo
**Habitiq** — house icon with a rotation arrow. One symbol that says: this is your space, and it runs itself.

### Colour Palette
| Role | Colour | Hex |
|------|--------|-----|
| Primary | Violet | `#7c3aed` |
| Primary light | Violet light | `#a78bfa` |
| Background (dark) | Warm deep black | `#0b0a08` |
| Surface | Warm charcoal | `#131110` |
| Text primary | Warm white | `#f0ebe2` |
| Text secondary | Warm gray | `#9a8d7e` |
| Success | Green | `#56b374` |
| Warning | Amber | `#d4a53a` |
| Danger | Red | `#dc5f5f` |

**Design direction:** Warm, not cold. All dark surfaces have a warm amber undertone — not blue-black. This makes the product feel like a home, not a fintech dashboard.

### Typography
**Inter** — clean, legible, modern. No decorative fonts. Strong weight contrast between hierarchy levels (900 for heroes, 400 for body).

### Motion
Orbiting dots on the loading screen — a visual metaphor for rotation, which is the core of what the product does.

### Voice
Direct. Calm. Specific. Not hype, not corporate.
- ✓ "No more 'whose turn is it?'"
- ✓ "Fair duties. Zero arguments."
- ✗ "Revolutionary AI-powered lifestyle synergy platform"

### Taglines
- **Hero:** "Smart living, managed."
- **Feature:** "No more 'whose turn is it?'"
- **Benefit:** "Your shared space, on autopilot."
- **Promise:** "Fair duties. Zero arguments."
- **Emotion:** "Shared living, sorted."

---

## 5. Features — What Exists Today (v0.1.0)

### Authentication & Onboarding
- Google Sign-In (one tap, works on mobile + desktop)
- Email/password login
- Custom auth domain proxy (fixes iOS Safari authentication bug)
- Create a flat → get unique invite code instantly
- Join a flat via invite code
- Flat live and running in under 2 minutes

### Smart Rotation Engine *(The Core Technology)*
The algorithm that makes Habitiq work. Not a simple round-robin — a genuinely smart system.

- Auto-assigns each task to the next person in queue when it comes due
- Skips members who are marked Out of Station (OOS)
- Resumes them at their correct position when they return
- Overdue tasks persist and carry over — nothing gets quietly dropped
- Admin can manually override assignment at any time
- Task frequencies: daily / weekly / fortnightly / monthly
- Priority levels: low / medium / high
- Custom start date per task

### Task Management
- Admin creates, edits, and deletes tasks
- Custom emoji per task
- One-tap mark complete
- Task completion date editing (retroactive)
- Overdue tracking (visual indicators)
- New members auto-added to all rotation queues immediately

### Swap Request System
- Any member can request coverage from any other member
- Formal accept / decline flow (not a chat message)
- Persistent banner on dashboard until resolved
- Side-by-side Received / Sent view (newest first)
- Full history log

### Rotation Order Card
- Every member can see the full queue for every task
- Position badges, including "YOU" marker
- Due date display
- Frequency badge
- Projected turn dates per person

### Admin Controls
- My Tasks view (admin's personal assignments)
- Org View (full flat dashboard, all members, all tasks)
- Invite code panel + copy/share
- Member management page
- Ability to transfer admin role to another member

### Membership Management
- Member can voluntarily leave a flat
- Admin can kick a member
- Admin must transfer role before leaving (unless last member)
- Last member leaving deletes the flat cleanly
- All task queues update automatically on leave/kick
- Kicked users see a clean "you've been removed" message

### Multi-Flat Support
- One account, multiple flats
- Switch flats via dropdown (Gmail-style switcher)
- Instant context switch with no logout

### Analytics & Insights
- Completion grid per member (calendar heatmap style)
- Reliability score per member
- Per-task breakdown
- Flat-level aggregates

### Calendar View
- Full monthly calendar showing all tasks
- Filter by member
- Completed vs pending visual distinction

### Activity Log
- Immutable audit trail of everything that happened in the flat
- Real-time updates
- All members can see all activity (transparency is accountability)
- Bills filter tab added

### Bills & Expenses Module
- Recurring bills with rotating payer (e.g. electricity bill cycles through members)
- One-off expense tracking (e.g. groceries, Netflix)
- Split modes: equal split or custom split
- Multi-currency: INR, USD, EUR, GBP, AED, SGD, AUD
- Balance tracking per member
- Settle Up flow
- Bills audit trail (bill_deleted, bill_updated logged to activity)
- Admin delete (3-step confirm) and admin edit
- Erase All Expense Data — admin Danger Zone, clears all subcollections
- All data cleaned on flat deletion

### Real-Time Sync
- Firestore onSnapshot listeners — every change appears instantly on all devices
- No refresh required
- Graceful offline detection

### Progressive Web App (PWA)
- Installable on Android home screen
- Service worker for offline fallback
- Dynamic app icon generation

### Legal Pages
- Privacy Policy at /privacy — DPDP Act 2023 compliant
- Terms of Service at /terms — India governing law, Hyderabad courts
- Both accessible without login

### Subscription System *(Shipped June 2026)*
- Flat-level subscription (admin buys, all members benefit)
- Three states: trial (30 days) / active (coupon redeemed) / expired
- Write actions gated on expiry: create_task, add_expense, create_bill, create_flat
- View-only access preserved for all expired users
- Coupon-first monetisation — no Stripe/Razorpay in Phase 1
- Built-in coupons: HAB-WELCOME (90d), EARLYBIRD-2026 (90d), HABITIQ-BETA (90d)
- LEGACY_FREE migration for pre-subscription flats (90-day trial from today)
- Premium UI: crown badge on avatar, gold "PREMIUM" pill, crown emoji in greeting
- Max 3 flats on premium, 1 flat on trial/expired

### UI & Experience
- Mobile-first design (bottom navigation on mobile)
- Desktop sidebar layout
- Dark mode / light mode toggle
- Fully responsive
- No app store needed — runs in any browser

### Security (Production-Grade)
- Role-based Firestore security rules (members can only access their own flat's data)
- Admin-only task creation and deletion
- Swap request validation (only the recipient can accept/decline)
- 6 HTTP security headers (CSP, HSTS, X-Frame-Options, etc.)
- Sanitised auth errors (no user enumeration)
- Input validation with max length limits
- `crypto.randomUUID()` for all ID generation (no predictable IDs)
- Firebase API keys protected by domain-locked rules (not secrets)
- Full security audit completed: 18 issues found and fixed across 3 audit rounds

---

## 6. Technical Stack

| Layer | Technology |
|-------|-----------|
| Framework | Next.js 16 (React 19, App Router) |
| Build tool | Turbopack |
| Language | TypeScript (strict mode) |
| Styling | Tailwind CSS v4 + CSS variables |
| State management | Zustand v5 + localStorage persistence |
| Icons | Lucide React |
| Animations | Framer Motion |
| Components | shadcn/ui |
| Database | Firebase Firestore (real-time NoSQL) |
| Authentication | Firebase Auth (Google + email/password) |
| Hosting | Vercel |
| Auth proxy | Next.js rewrites (custom authDomain) |

**Why this stack matters for the pitch:** The entire product was built and shipped by a UI/UX designer with no traditional backend background, using AI-assisted development (Claude Code + Gemini). This demonstrates how lean the build process is — a one-person team shipped a production-ready, security-audited, real-time web app in weeks.

---

## 7. Infrastructure & Cost

### Current (Trial Phase) — ₹0/month

| Service | Plan | Cost |
|---------|------|------|
| Vercel | Hobby (Free) | ₹0 |
| Firebase Auth | Spark (Free) | ₹0 |
| Firestore | Spark (Free) | ₹0 |
| Firebase Storage | Spark (Free) | ₹0 |

**Capacity at ₹0:** Handles ~100 active flats (600 users) comfortably.

### At Scale (Firebase Blaze pay-as-you-go)

| Flats | Estimated Monthly Cost |
|-------|------------------------|
| 100 flats | $0–5/month |
| 500 flats | $10–25/month |
| 2,000 flats | $50–100/month |
| 10,000 flats | $200–400/month |

Infrastructure cost is negligible relative to revenue at every tier. Margins stay high as the product scales.

---

## 8. Competitive Landscape

| Product | What It Does | Why It's Not Habitiq |
|---------|-------------|----------------------|
| Splitwise | Expense tracking | No chore management |
| Todoist / Notion | To-do lists | No rotation, no shared accountability |
| WhatsApp | Messaging | No structure, no audit trail |
| OurHome | Chore tracker (Western) | Not built for India, no PG context |
| Tody | Cleaning tracker | No multi-person rotation |
| Google Calendar | Scheduling | No assignment, no completion tracking |

**Habitiq's position:** The only purpose-built, real-time, rotation-first shared living management tool for India. Splitwise tracks money. Habitiq tracks duties. They are complementary — not competitive.

---

## 9. Revenue Model

### Phase 1 — Coupon-Gated Premium (Current)

No payment integration. Coupons activate 90-day premium access. Goal: validate retention before charging.

| Coupon | Duration | Distributed |
|--------|----------|-------------|
| HAB-WELCOME | 90 days | Auto-shown on new flat creation |
| EARLYBIRD-2026 | 90 days | Shared manually to early adopters |
| HABITIQ-BETA | 90 days | Internal / founder use only |

### Phase 3 — Freemium (Planned)

| Tier | Price | What's Included | Target User |
|------|-------|-----------------|-------------|
| **Free** | ₹0/month | Up to 6 members, 10 tasks, all core features | Student flats, trial users |
| **Pro** | ₹99/flat/month | Unlimited members + tasks, push notifications, photo proof, analytics export | Active flats, working professionals |
| **Power** | ₹249/flat/month | Everything in Pro + priority support, flat announcements, custom branding | Larger households, organised groups |

### Revenue Projections

| Phase | Active Flats | Estimated Monthly Revenue |
|-------|-------------|---------------------------|
| Phase 2 end | 100 flats | ~₹1,000/month |
| Phase 3 start | 500 flats | ~₹7,500/month |
| Phase 3 growth | 2,000 flats | ~₹40,000/month |
| Scale | 10,000 flats | ~₹2,25,000/month |

### Secondary Revenue (Future)
- **Affiliate commissions** — household product recommendations contextual to task types (cleaning supplies when cleaning task is overdue, etc.)
- **Spending data insights** — anonymised aggregate expense category data, relevant to FMCG and D2C brands targeting shared living households
- **API access** — for co-living operators building internal tools (not hostels — branded co-living spaces like Stanza Living, CoLive)

---

## 10. Roadmap

### Phase 1 — Trial (Now → 3 months) — *In progress*

**Core platform (shipped):**
- ✅ Core rotation engine
- ✅ Google + email authentication
- ✅ Real-time Firestore sync
- ✅ Mobile-first UI (dark + light mode)
- ✅ Security audit (18 issues fixed across 3 rounds)
- ✅ Multi-flat support with flat switcher
- ✅ Membership management (leave / kick / transfer)
- ✅ Analytics, calendar, activity log
- ✅ PWA (installable on Android)
- ✅ Bills & Expenses module (recurring + one-off, multi-currency, settle up)

**Recent improvements (June 2026):**
- ✅ Task completion date editing (retroactive)
- ✅ Bills improvements — admin delete, admin edit, dashboard fix
- ✅ Mobile nav overhaul (Swaps deduped, Expenses added)
- ✅ Bills audit trail in activity log
- ✅ Rotation expected dates on dashboard queue
- ✅ Expense delete enabled for creators and admins
- ✅ Settlements collapsed by default (compact divider, tap to expand)
- ✅ Fixed bills dashboard race condition (writeBatch for atomicity)
- ✅ Erase All Expense Data (admin Danger Zone)
- ✅ Privacy Policy and Terms of Service pages (DPDP Act 2023)
- ✅ Subscription system (trial / active / expired, coupon-gated, view-only on expiry)
- ✅ Premium UI (crown badge, gold pill, multi-flat warning)
- ✅ LEGACY_FREE migration (90-day trial for pre-subscription flats)

**Design work (June 2026):**
- ✅ Member home screen redesigned (see Section 11 — Mobile UI Design)
- ✅ Navigation redesigned (Home · Duties · Log · Money · Nest)
- ✅ Task bottom sheet interaction pattern
- ✅ Bills / Money separation implemented in design
- ✅ Design system tokens documented

**In progress:**
- 🔄 Collecting user feedback, fixing bugs, tracking metrics

### Phase 2 — Growth (3–6 months)
- Push notifications (Firebase Cloud Messaging)
- WhatsApp integration for task reminders
- Photo proof on task completion (upload image)
- Guest invite by link (not just flat code)
- Flat name and member nickname editing
- Password reset UI
- Task history archive (beyond 30 days)
- Flat announcements (admin pinned message)
- Task templates (preset task packs: cooking rotation, cleaning rotation, etc.)
- Member home screen shipped to production (currently prototype at C:\garbage\member-home.html)
- Admin home screen redesign

### Phase 3 — Scale & Monetisation (6–18 months)
- Razorpay / Stripe payment integration (Pro tier billing)
- Automated reminders (email + push + WhatsApp)
- Reliability score rewards (badges, streaks, flat leaderboard)
- Analytics export (CSV / PDF)
- API for integrations
- Custom branding (white-label for co-living operators)
- Native mobile app (React Native — iOS + Android)
- Firebase Cloud Functions (server-side jobs, scheduled reminders)
- Offline-first mode
- Flat discovery — members search for flats, flats search for members (Phase 3+ idea)

---

## 11. Mobile UI Design — Member Home Screen

### What This Screen Is

The member home screen is the primary interface for a non-admin flatmate. It is the first thing they see after logging in. Its job: tell a member, at a glance, everything they need to know about their home in one scroll.

This screen is the "OS for a shared home" — not an expense tracker with a chores section, not a task manager with a money tab bolted on. Every decision about what appears here, in what order, and how it looks is guided by a single question: **what does someone actually need when they open this app?**

### Design Philosophy

**Home first.** The first thing a member sees is their home — their flat name, their flatmates, who's home, who's out. Before tasks, before money. The emotional anchor is "this is my home."

**Action first.** The most important interaction on this screen is completing a task. The task name is the largest element on the page — 40px, weight 900. Nothing competes with it. The "Mark as Done" button is always the primary CTA.

**Statistics second.** Reliability scores, open task counts, and review dates exist to inform — not dominate. They sit below the fold, compact and scannable.

**Personality third.** Time-based greetings, member presence indicators, and a warm dark color palette give the screen personality. This is a home app, not a productivity dashboard.

### Section Hierarchy (scroll order)

**1. Home Identity**
- Date displayed in small uppercase label: "WEDNESDAY · 18 JUNE"
- Time-of-day greeting: "Good morning, Sai." — 28px, weight 800
- Contextual sub-text changes by time: "You have 3 tasks this week. Start with kitchen."
- Member avatar stack: all flatmates overlapping, with member count and location
- Live presence indicator: green dot + "Bhanu home" — shows who is currently at home vs out
- Time slots: morning (5am–12pm), afternoon (12–5pm), evening (5–9pm), night (9pm–5am)

**2. Today's Tasks** *(the visual hero)*
- Section label: "Your tasks" + live progress bar: "1 of 3 done ▓░░"
- Violet top border accent (2px) distinguishes this as the primary section
- Primary task shown at 40px/900 — the biggest element on the page:
  - Task name dominates: "Kitchen Cleaning"
  - Status badge: "Due today" (violet) or "Done ✓" (green)
  - Rotation queue: YOUR avatar (full opacity) → next members (faded, with names)
  - CTAs: [Mark as Done] (violet, full) + [Swap] (secondary, right)
- Additional tasks as compact tappable rows, each with:
  - Completion circle (empty = pending, filled = done)
  - Task name + frequency + next person in queue
  - Due date badge
  - Chevron → opens task detail bottom sheet
- Designed to hold 3–4 tasks cleanly

**Completed task state (first task defaulted to done in demo):**
- Task name gets subtle strikethrough + muted color
- Badge switches from "Due today" → green "Done ✓"
- CTA area replaced by: [✓ Completed today] row in green background
- Entire section gets very subtle green background tint `rgba(86,179,116,0.04)`
- Progress bar advances: "1 of 3 done"

**3. Home Health**
- Three inline stats — no cards, no borders, just numbers
- Reliability score (87%) with a 3px progress bar beneath
- Open tasks count (flat-wide, not just yours)
- Days to next flat review
- "#1 this week" shown in green under reliability if applicable

**4. Bills — Conditional Section**
- **Appears only when a bill is within ~7 days of its due date**
- Completely separate from expense splits — never mixed
- Shows: bill name, exact due date, total amount, split method, [Pay] button
- Red "1 pending" urgency badge in section header
- When no bills are near due: this entire section disappears
- Rationale: bills are not a permanent presence on the home screen. They appear when action is required and vanish when resolved. A month-end electricity bill should not live next to a grocery expense split from Tuesday — they are different financial objects with different urgency and different actions.

**5. Money — Expense Splits Only**
- This section never contains bills
- Shows: total owed to you ("₹2,000"), from how many people, with label "expense splits"
- Individual rows: avatar + name + context + green amount
- [Settle up →] link
- Subtext "from 2 people · expense splits" makes the scope explicit

**6. Activity Feed**
- What happened in the flat recently
- Each item: avatar + natural language sentence + timestamp
- Events: expense additions, task completions, swap acceptances, bill splits
- [See all] → full activity log screen

**7. Upcoming Rotations**
- Horizontal scrollable carousel
- Each slot: date label + avatar + member name
- YOUR turns: violet date label, violet avatar border, "YOU" label below
- Others: muted gray
- No task names shown — just who + when (clean, scannable)
- Designed for 6–8 cards visible with scroll

### Task Detail — Bottom Sheet

When any task row is tapped, a bottom sheet slides up from the bottom of the screen. This is the mobile interaction pattern for task detail — not a new page, not a dialog box.

**Sheet design:**
- Drag handle at top (36px wide, 4px tall)
- Semi-transparent dark backdrop — tap backdrop to dismiss
- Slides up with spring physics: `cubic-bezier(0.32, 0.72, 0, 1)`, 320ms

**Sheet content:**
- Task name (34px, 900 weight) — with strikethrough if completed
- Status badge (violet "Due today" / green "Done ✓")
- Frequency + assignment: "Weekly rotation · assigned to you"
- Section label: "Rotation order"
- Full rotation queue: all member avatars with names, YOU highlighted in violet
- Historical note: who last completed this and when
- CTAs:
  - Pending: [Mark as Done] (violet primary) + [Swap] (gray secondary)
  - Completed: [Completed today ✓] (green, inactive — no action needed)

**Three task types handled:**
- Kitchen Cleaning (completed today — completed state)
- Garbage Duty (due tomorrow — pending state with full CTAs)
- Bathroom (due 23 Jun — pending state with full CTAs)

### Bills vs Money — The Separation Rule

This is a product principle, not just a design decision.

**Bills** = recurring obligations on a cycle (electricity, internet, rent, subscriptions). They have a due date. They need to be paid. They create a shared liability. They are temporary — you pay them and they go away until next cycle.

**Money / Expense Splits** = one-off shared costs that need to be tracked and settled (groceries, a dinner, cleaning supplies). They create a balance that needs reconciliation.

These two financial concepts must always be displayed separately because:
1. They have different urgency (a due bill needs action today; a split can wait)
2. They have different resolution (pay a bill; settle up with a person)
3. Mixing them creates a confusing, finance-app-like experience that contradicts the "home-first" philosophy

**Implementation rule:**
- The Bills section appears only when at least one bill is due within ~7 days
- When no bills are approaching due, the Bills section is hidden entirely
- The Money section always shows only expense splits, never bill items
- Activity feed can reference both (it's a log, not an action panel)

### Navigation — Home · Duties · Log · Money · Nest

The navigation was redesigned from scratch based on actual user behaviour.

| Tab | Purpose |
|-----|---------|
| **Home** | The dashboard — flat pulse, today's tasks, all sections |
| **Duties** | My personal task queue, rotation history, swap requests |
| **Log** (FAB) | Quick actions: add expense, mark task done, request swap |
| **Money** | Expense splits, balances, settlement flow |
| **Nest** | Flat identity, members, settings, reliability rankings |

**Why "Nest" not "Profile":** The last tab is about the flat, not just the user. Settings, member management, invite codes, and flat identity live here. "Nest" ties to the flat name convention (Falcon Nest, etc.) and reinforces the home-first philosophy.

**Why "Duties" not "Tasks" or "Chores":** "Duties" implies personal responsibility and rotation — more accurate to what the tab contains. Tasks is too generic. Chores is too domestic.

**Why "Log" not "+":** The FAB is specifically for logging something — an expense, a completion, a note. "Log" is specific. "+" is meaningless.

**Active state:** The active tab has a violet pill background `rgba(124,58,237,0.12)` behind its icon and label. Inactive tabs show muted gray icon and label with no background.

---

## 12. Mobile Design System

### Colour Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `--bg` | `#0b0a08` | Page background |
| `--s1` | `#131110` | Card surface (default) |
| `--s2` | `#1a1815` | Elevated elements, sheet backgrounds |
| `--s3` | `#232118` | Pill backgrounds, inner badges |
| `--bd` | `rgba(255,244,220,0.07)` | Card borders (subtle) |
| `--bd2` | `rgba(255,244,220,0.13)` | Visible borders, row dividers |
| `--bdv` | `rgba(124,58,237,0.22)` | Violet-tinted borders |
| `--t1` | `#f0ebe2` | Primary text (warm white) |
| `--t2` | `#9a8d7e` | Secondary text (warm gray) |
| `--t3` | `#574f44` | Muted text, section labels |
| `--vi` | `#7c3aed` | Brand violet — actions, CTA, FAB, active nav |
| `--vl` | `#a78bfa` | Violet light — accents, labels, badges |
| `--gr` | `#56b374` | Completion, positive balances, success |
| `--re` | `#dc5f5f` | Overdue, urgent bills, errors |
| `--am` | `#d4a53a` | Warning, amber states |

### Typography Scale

| Element | Size | Weight | Notes |
|---------|------|--------|-------|
| Primary task name | 40–44px | 900 | Largest element on page — the hero |
| Money amount | 36–38px | 800 | Always with ₹ currency symbol |
| Greeting | 28–30px | 800 | Changes by time of day |
| Health stats | 32px | 800 | Three-column inline, no cards |
| Secondary task name | 15px | 600 | In task list rows |
| Body text | 13–14px | 400–500 | Activity items, metadata |
| Meta text | 11–12px | 400–500 | Dates, counts, secondary info |
| Section labels | 9px | 700 | ALL CAPS, 0.18em letter spacing |

### Card Component

All sections are discrete cards. No continuous scroll with dividers.

```
background:    var(--s1)
border-radius: 22px
border:        1px solid var(--bd)
overflow:      hidden
gap between:   10px
page padding:  14px horizontal
```

**Primary task card (special treatment):**
```
same as above PLUS:
border-top: 2px solid var(--vi)   ← violet top accent signals primary section
```

All other cards use 1px uniform border.

### Avatar System

| Size | Use |
|------|-----|
| 18px | Tiny — inline queue pills |
| 22px | Small — faded queue track |
| 26px | Medium — rotation carousel faded others |
| 32px | Standard — activity feed, money rows |
| 40px | Large — rotation carousel featured slots |

**Avatar colours (dark palette for warm dark backgrounds):**
- Violet member (Sai): `#3c1472`
- Blue member (Bhanu): `#1b3357`
- Green member (Kiran): `#053d24`
- Amber member (Rohin): `#6b3310`

**Stacking (overlapping avatars):** each avatar gets `border: 2px solid var(--s1)` (matches card surface, not page background).

### Component Patterns

**Completion circle (pending task):**
- `width: 22px; height: 22px; border-radius: 50%`
- Empty: `border: 2px solid var(--bd2)`
- Completed: filled green with white checkmark inside

**Row dividers within a card:**
- `border-top: 1px solid var(--bd)` — always inside the card
- Never use `<hr>` between cards — use the 10px gap

**Rotation queue track:**
- Active member (YOU): full opacity, 26–32px avatar, violet border ring
- Next members: 0.38 opacity, smaller avatars, names below
- Arrow separator between YOU and others

**Section label pattern:**
- `font-size: 9px; font-weight: 700; letter-spacing: 0.18em; text-transform: uppercase; color: var(--t3)`

**Bottom sheet:**
- Background: `var(--s2)`
- Border-radius: `24px 24px 0 0`
- Max height: `82vh`
- Animation: `transform: translateY(100%)` → `translateY(0)`, spring curve
- Backdrop: `rgba(8,8,12,0.72)`, opacity transition 280ms

---

## 13. Product Principles

### Core Principles

1. **Fairness is the product.** Every feature exists to make shared living more fair.
2. **Zero friction onboarding.** Flat live in under 2 minutes.
3. **Transparency by default.** All members see everything — visibility is the accountability mechanism.
4. **The system is the authority.** Nobody should have to nag.
5. **Mobile first, always.** Most users open the app standing in the kitchen.
6. **Real-time or nothing.** If changes aren't instant, trust breaks down.

### UI Design Principles

7. **Cards are contexts, not boxes.** Each section has one purpose. Bills ≠ Money ≠ Tasks ≠ Activity. Don't mix contexts within a card.
8. **Typography does the heavy lifting.** The task name is the hero. No hero images, no decorative illustrations — the content itself is the design.
9. **No decorative UI.** Every visual element must carry information or enable action. Remove anything that doesn't.
10. **Separate what is different.** Recurring bills and one-off expense splits are different financial objects. Display them in separate sections, always.
11. **Conditional visibility.** Sections that aren't relevant right now should not appear. Bills only when due soon. Empty states only when genuinely empty.
12. **Warm, not cold.** The app lives in a home. All surface and text colour choices carry a warm undertone — not the cold blue-black of a fintech dashboard.
13. **The home screen is the product.** If the home screen is clear, fast, and actionable, every other screen inherits that trust.

---

## 14. Future Screens to Build

Based on the navigation structure (Home · Duties · Log · Money · Nest), these screens are planned for Phase 2 and Phase 3.

### Duties Screen
- Personal task queue — all tasks assigned to the current member
- Rotation history (past completions, streak tracker)
- Your reliability score card with week-over-week trend
- Swap requests — sent and received tabs
- "All clear" empty state when no pending tasks

### Log Bottom Sheet (FAB)
- Quick action sheet opens from the + button in nav
- Four actions:
  1. **Mark done** — select a task, confirm completion
  2. **Add expense** — amount, description, split method, who's involved
  3. **Request swap** — select task, select person, optional message
  4. **Report issue** — flat-level note for a problem (broken appliance, etc.)

### Money Screen (Full)
- Expense history with filters (all / by member / by category)
- Balance per member — who owes you, who you owe
- Settle up flow — select person, choose amount, confirm
- Bills section (collapsed by default, separate from expense splits)
- Monthly summary card — total flat spend, per-person breakdown

### Nest Screen (Flat + Profile)
- Flat identity card: flat name, location, invite code, member count
- Member roster with avatars, reliability rankings, tasks completed count
- Your profile: name, avatar color, your stats, notification preferences
- Admin section (visible to admin only): add/remove members, edit flat name, transfer role
- Subscription status: current plan, coupon field, upgrade prompt
- Settings: dark/light mode, notification settings, logout, delete account

### Admin Dashboard (separate view)
- Full org view: all members, all tasks, all upcoming rotations
- Flat health metrics: overall reliability, tasks due today, pending swaps
- Task management: create, edit, delete, reassign
- Member management: OOS toggle, kick, invite management
- Bill management: create recurring bills, edit cycles, delete
- Analytics panel: member performance comparison, trend charts

### Member Onboarding Flow
- Invite link lands on a preview screen (flat name, member count, who's in it)
- One-tap join with Google
- Welcome screen: "You've joined Falcon Nest. Here's how it works."
- First-time tutorial: 3 swipe cards covering tasks, swaps, and expenses
- Direct to home screen

### Flat Creation Flow (Admin)
- Flat name input
- Location/city (optional)
- Member invite: share code, send invite link, or add by email
- Set first round of tasks (task templates or custom)
- Welcome modal: HAB-WELCOME coupon pre-filled, one-tap activate

---

## 15. Trial Phase Metrics (Targets)

| Metric | Target |
|--------|--------|
| Active flats | 5–20 |
| Member logins per flat per week | ≥ 3 |
| Task completion rate | ≥ 80% |
| Overdue rate | < 20% |
| Swap requests per flat per week | 1–3 |
| 30-day flat retention | > 60% |
| Average session length | > 2 minutes |

---

## 16. Go-to-Market Strategy

### Phase 1 — Organic
- Founder shares directly with personal networks
- Early adopters share invite codes to other flats
- LinkedIn posts (build-in-public)
- College alumni groups, flat-hunting Facebook groups

### Phase 2 — Content & Community
- User testimonials and case studies
- College housing partnerships (accommodation portals)
- Micro-influencer outreach (student lifestyle creators)

### Phase 3 — Paid & Partnerships
- Meta/Instagram ads (18–28 age, metro city targeting)
- Google Search ads ("roommate chore tracker India", "flatmate duty app")
- Direct partnership outreach to co-living brands (Stanza Living, NestAway, CoLive)
- App Store listing (iOS + Android native)

---

## 17. How Habitiq Was Built

**Method:** AI-Assisted Product Development

**The team:**
- **Venkata Sai Jaswanth E** *(Founder)* — Product vision, user flows, design decisions, feature prioritisation, business logic, brand identity, UI design
- **Claude Code (Anthropic)** — Full implementation: Next.js architecture, Firestore schema design, rotation engine, auth flows, security rules, deployment, mobile UI prototyping
- **Gemini** — Architecture pressure-testing, edge case exploration

**Why this matters:** A UI/UX designer with no backend background shipped a production-ready, security-audited, real-time web application in weeks. The product went from concept to live users without a traditional engineering team. This is the new model for early-stage product development.

---

## 18. The Team

### Venkata Sai Jaswanth E — Product & Design
- **Role:** UI/UX Designer, Founder
- **Location:** Hyderabad (open to Bangalore, Chennai, Mumbai)
- **Current role:** UI/UX Designer Trainee, MyClassboard (Jan 2026–Present)
- **Education:** B.Tech IT, Vishnu Institute of Technology (2023, CGPA 7.21)
- **Certifications:** Google UX Certificate, AI-Powered UI/UX Certificate
- **Skills:** Figma, FigJam, IA, User Flows, Wireframing, Prototyping, Design Systems, Design Tokens, Enterprise UX, WCAG
- **Email:** saijaswanthedupuganti@gmail.com
- **Portfolio:** https://jaswanth-portfolio-three.vercel.app/
- **LinkedIn:** https://www.linkedin.com/in/venkata-sai-jaswanth-edupuganti-787251199
- **Philosophy:** "I think in systems before I think in screens — and I build what I design."

### Upputuri Bhanu Kalyan — Engineering, Co-Founder

---

## 19. Key Links & References

| Resource | Link / Location |
|----------|----------------|
| Live app | https://habitiq.app |
| Target domain | habitiq.in / habitiq.app |
| Codebase | C:\garbage |
| Obsidian vault | C:\garbage\project_1 |
| Member home screen prototype | C:\garbage\member-home.html |
| Session log (Google Sheet) | ID: 1Z6EoxX0x5tO25HsQuUsNckoV3Zfhsv8JPmfOXC_yG0s |
| Bug/feature tracker | ID: 1zzRYfTGuJaGLES8hr65rnDCRN2x7WnT81qG09K3Dryw |
| Google Doc (this file) | ID: 1cb48MYbbXNfrpUZgrvLjVnF6W6YFGYjmsqCVGzPuJoU |
| Vercel project | https://vercel.com/saijaswanthedupuganti-cmyks-projects/garbage |

---

## 20. Presentation Talking Points

### For a VC / Investor Audience
- "Splitwise tells you who owes money. Habitiq tells you who owes dishes. The market is the same 50M+ shared living households in India."
- "We are free today. Infrastructure costs us zero. When we flip the switch on Pro billing, every flat we've already retained becomes revenue."
- "One designer built a production app used by real people — that's not a story about a developer. It's a story about what the next generation of product building looks like."

### For a Design/Tech Conference
- "The product was designed in Figma and shipped by Claude. I never wrote a backend line in my life. The rotation engine, Firestore rules, auth proxy — all of it. This is what AI-assisted product development looks like when a designer is driving."

### For a Startup Competition
- "India has 50M+ people in shared rented accommodation. They all have the same problem. Nobody has built the right tool for them, in their context, for their price point. Habitiq is that tool."

### For a Flat/Housing Context Demo
- "Open the app. Add your flatmates. Add your tasks. The system handles the rest. If someone goes out or travels — one tap, their tasks route to the next person. When they're back — one tap, they're in rotation again."
- "Bills and expenses stay separate — bills show up only when they're due, then disappear when paid. Your money view is always clean."

---

*Last updated: 2026-06-18 | Version: 2.0 | Maintained by: Venkata Sai Jaswanth E + Claude Code*
