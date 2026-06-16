# Habitiq — Product & Business Documentation

> **Version:** v0.4.0
> **Live URL:** https://habitiq.app
> **Status:** Live — Active Trial with Real Users
> **Last Updated:** June 2026

---

## 1. What Is Habitiq?

Habitiq is an **intelligent shared living management platform** built for India. It automates household duty rotation among flatmates, splits shared expenses, tracks monthly bills, and manages swap requests — eliminating arguments, forgotten tasks, and unfair workloads.

**The name:** Habitat + Habit + IQ — the intelligence that skips absent members, reassigns overdues, tracks reliability, and runs the shared space on autopilot.

**Category it owns:** Intelligent shared living management. Not a chat app (WhatsApp). Not just expense splitting (Splitwise). Not a to-do app (Todoist). The operational backbone of co-living.

---

## 2. Target Users

### Primary: Shared Flats & PG Residents
- 2–8 people per flat, age 18–30
- Students and young working professionals
- Indian Tier 1 and Tier 2 cities
- Already coordinating on WhatsApp; frustrated by lack of accountability

### Secondary: PG Owners & Co-Living Operators
- Managed properties: 5–50 rooms per property
- Admin (property manager) + rotating residents
- Need structured duty and expense system without manual tracking

### Who It Is NOT For
- Hotel or hostel staff management systems
- Commercial cleaning services
- Corporate office management

---

## 3. Complete Feature Set (v0.4.0)

### Auth & Onboarding
- Google Sign-In (one-tap) + Email/Password
- Create flat → instant invite code → share → done (under 2 minutes)
- Join flat via invite code
- Approval-mode join: admin reviews and approves/rejects join requests
- Multi-flat: belong to multiple flats simultaneously, switch with one tap
- Join or create additional flats from inside the dashboard (no logout)

### Rotation Engine
- Admin creates tasks: name, emoji, frequency (daily/weekly/fortnightly/monthly), priority (low/medium/high), start date, rotation queue order
- Auto-assignment on every cycle — deterministic, consistent across all devices
- Smart skip: out-of-station members bypassed; rotation resumes on return
- Overdue tracking: tasks past due date stay with the responsible person
- Admin manual override: reassign any task at any time
- New members auto-added to all rotation queues
- Group tasks: assigned to multiple members with individual sub-tasks
- Temp tasks: one-off tasks outside the rotation queue

### Swap System
- Any member can request a swap with any flatmate
- Recipient sees dashboard banner + notification
- Accept or decline with one tap — task reassigns automatically
- Swap page: 4 stat chips (Sent / Received / Accepted / Declined) per user
- Admin All Swaps toggle: flat-wide list with inline accept/decline
- Dashboard swap widget: summary card with live pending count
- Mobile: Swap Requests button on Tasks page opens bottom sheet

### Expense Module — Daily Splits
- Anyone logs an expense: description, amount, category, date
- Equal split or fully custom per-person amounts
- 7 currencies: INR, USD, EUR, GBP, AED, SGD, AUD
- Banking-style transaction list: category emoji + description + payer · date + net amount
- Edit expense (creator or admin) · Delete with optimistic instant UI update
- Deferred expenses: carry to next month

### Expense Module — Monthly Bills
- Admin configures recurring bill templates: Rent, WiFi, Electricity, Water, Gas, Maid, etc.
- Fixed amount or Variable (enter actual amount each month when generating)
- Payer rotates through a queue month to month
- **Collector role**: separate from payer — designates who collects each member's share. Shown in bill card header. Changeable any month via avatar card picker.
- Admin generates bill instances on/after billing date
- **Collection tracking**: per-member Received/Pending toggles on bill instances
- Status flow: Pending → Split Generated → Paid / Skipped
- All payers (not just admin) can mark their own bill paid

### Balances & Settlement
- Direct pairwise balance — only real transaction partners shown (no MCF debt-chain simplification)
- Per-person balance cards: green (they owe you) · orange (you owe them)
- Expand any card to see which expenses make up that balance
- **Settle** (debtor): pay full or partial; quick-fill "Pay full / Pay half" chips
- **Mark Received** (creditor): record cash/UPI payment from the other person
- **Person filter**: filter transaction list to shared history with one specific person
- Balance strip collapsed by default on mobile; tap to expand full cards

### Monthly Summary & Close
- Summary card: total monthly spend broken into Monthly Bills (amber) vs Daily Splits (blue)
- Admin month-end close: lock the month; balances carry forward to next cycle
- Carry-forward balances shown as a notice at top of next month's list

### Analytics
- Task completion grid (GitHub contribution-style) per member
- Reliability scores: on-time completion rate per member
- Per-task breakdown: completion rates by task
- Flat-level aggregate view

### Calendar
- Monthly task calendar with all assignments
- Member filter: view one person's tasks
- Completed vs pending distinction

### Activity Log
- Full real-time audit trail of every action in the flat
- Flat-wide visibility; real-time via Firestore `onSnapshot`
- Server-side paginated: 50 most recent entries, O(50) read cost regardless of flat age

### Membership Management
- Leave flat: auto-switch to next flat or redirect to onboarding
- Admin transfer required before leaving (if others remain)
- Last-member flat deletion: full Firestore cleanup including all subcollections
- Kick member: admin removes any member; tasks correctly reassigned to next-in-queue person
- 8-member cap: enforced at Firestore rules level

### Multi-Flat Support
- Belong to multiple flats simultaneously
- FlatSwitcher dropdown (Gmail-style) — all flats listed, one tap to switch
- Instant context switch: all listeners reload for the new flat

### Subscription & Trial
- 30-day free trial per flat
- Trial gates create_task, create_flat, create_bill on expiry; expense adding stays open
- Coupon system: `full_unlock` type activates indefinite access; `trial_extend` adds days
- Max flats: 1 on free/trial; 6 on premium
- Legacy flat backfill: existing flats without subscription field treated as active

### PWA
- Installable on Android and iOS without App Store
- Web App Manifest + Service Worker
- No install friction — works from the browser immediately

### UI & Navigation
- Dark/light mode: system preference + manual toggle
- Mobile bottom nav (5 slots): Dashboard · Expenses · [Radial FAB] · Tasks · Profile
- Radial Quick-Add FAB: Admin → Split Expense / Bill / Task; Member → Split Expense only
- Desktop sidebar: full navigation with pending badge counts
- Member Tasks page: read-only compact cards, tap to expand full rotation queue detail
- Fully responsive — designed mobile-first

### Legal Compliance
- Privacy Policy at `/privacy` — DPDP Act 2023 compliant
- Terms of Service at `/terms` — governing law India (Hyderabad courts)
- Both pages accessible without login

---

## 4. Security

Three independent security audits completed. 18 vulnerabilities found and fixed.

| Severity | Found | Fixed |
|----------|-------|-------|
| CRITICAL | 2 | 2 ✅ |
| HIGH | 5 | 5 ✅ |
| MEDIUM | 4 | 4 ✅ |
| LOW | 7 | 7 ✅ |

Key fixes: role-based Firestore rules (admin/member/payer/collector), atomic batch operations, `crypto.randomUUID()` throughout, sanitised auth errors, 6 HTTP security headers, correct task reassignment on member leave/kick.

See [SECURITY_AUDIT.md](./SECURITY_AUDIT.md) for full details.

**Remaining (Phase 2):** Firebase API Key restrictions, CSP, Firebase App Check rate limiting.

---

## 5. Trial Phase Goals

### What We're Validating
1. **Core loop adoption** — Do flatmates actually use it daily/weekly?
2. **Completion rate** — What % of tasks are marked complete before overdue?
3. **Retention** — Does the flat keep using it after 30 days?
4. **Expense adoption** — Do flats use both duties and expenses, or only one?
5. **Swap frequency** — How often do members swap tasks?
6. **NPS** — Would users recommend to another flat?

### Trial Metrics Targets

| Metric | Target |
|--------|--------|
| Active flats | 5–20 |
| Active members per flat | ≥3 logins/week |
| Task completion rate | ≥80% |
| Overdue rate | <20% |
| 30-day flat retention | >60% |
| Average session length | >2 minutes |
| NPS | >50 |

### Trial Feedback Questions
1. How often do you open Habitiq? (Daily / Few times a week / Weekly / Rarely)
2. Has chore fairness improved? (Yes / Somewhat / No)
3. Do you use the expense splitting? (Yes / Sometimes / No)
4. What is the one thing you wish Habitiq could do?
5. Would you recommend Habitiq to another flat? (Yes / Maybe / No)
6. Would you pay ₹99/flat/month to keep using it? (Yes / Maybe / No)

---

## 6. Roadmap

### Phase 1 — Trial (Now → Month 3) CURRENT

| Item | Status |
|------|--------|
| Smart rotation engine | ✅ Live |
| Google + email auth | ✅ Live |
| Real-time Firestore sync | ✅ Live |
| Swap system (full analytics) | ✅ Live |
| Analytics + reliability scores | ✅ Live |
| Calendar view | ✅ Live |
| Multi-flat + FlatSwitcher | ✅ Live |
| Membership management (leave/kick/transfer/delete) | ✅ Live |
| Full expense module (bills + splits + balances + settle) | ✅ Live |
| Month-end close + carry-forward | ✅ Live |
| Dark/light mode | ✅ Live |
| PWA (installable) | ✅ Live |
| NPS banner | ✅ Live |
| Mock mode (demo without Firebase) | ✅ Live |
| 8-member cap enforcement | ✅ Live |
| Privacy Policy + Terms of Service | ✅ Live |
| Subscription / trial gate system | ✅ Live |
| habitiq.app domain live | ✅ Live |
| Collect user feedback | 🔄 Ongoing |
| Fix bugs from real usage | 🔄 Ongoing |

### Phase 2 — Growth (Month 3–6)

**Goal: 100+ active flats**

High priority:
- [ ] Push notifications (Firebase Cloud Messaging)
- [ ] WhatsApp integration: task reminders via WhatsApp Business API
- [ ] Password reset UI (in-app flow)
- [ ] Admin flat-wide balance matrix (all member-to-member balances in one view)
- [ ] Settlement confirmation from recipient
- [ ] Guest invite via shareable link (not just invite code)
- [ ] Multi-currency balance unification

Medium priority:
- [ ] Task photo proof (Firebase Storage)
- [ ] Member nickname editing
- [ ] Task history archive (>30 days)
- [ ] Flat announcements (admin broadcast)
- [ ] Task templates (common presets)
- [ ] Expense receipt photo attachment

### Phase 3 — Scale & Monetisation (Month 6–18)

**Goal: 2,000+ active flats, first revenue**

| Feature | Description |
|---------|-------------|
| Stripe / Razorpay billing | In-app subscription payments |
| Admin super-dashboard | All properties in one view (B2B tier) |
| Automated reminders | Email + push + WhatsApp, per-task configurable |
| Reliability score rewards | Badges, streaks, leaderboards |
| Analytics export | CSV/PDF reports for PG owners |
| API for integrations | Connect property management tools |
| Custom branding | White-label Habitiq for co-living operators |
| Native mobile app | React Native for iOS + Android |
| Offline mode | Full offline + background sync |
| Firebase Cloud Functions | Server-side business logic + scheduled jobs |

---

## 7. Business Model

### Freemium Tiers (Phase 3)

| Tier | Price | Limits | Target |
|------|-------|--------|--------|
| Free | ₹0/month | 1 flat, 6 members, 10 tasks, core features | Students, casual trial |
| Pro | ₹99/flat/month | Unlimited members + tasks, push notifications, photo proof, analytics export | Working professional flats |
| Business | ₹499/property/month | Multiple flats, super-dashboard, white-label, priority support | PG owners, co-living operators |

### Revenue Projections

| Phase | Active Flats | Conversion | Monthly Revenue |
|-------|-------------|-----------|-----------------|
| Month 3 | 20 | — | ₹0 (free trial) |
| Month 6 | 100 | 10% Pro | ~₹1,000 |
| Month 12 | 500 | 15% Pro | ~₹7,500 |
| Month 18 | 2,000 | 20% Pro + 5 Biz | ~₹42,000 |
| Month 30 | 10,000 | 20% Pro + 50 Biz | ~₹2,25,000+ |

### Growth Loop
One person joins → invites all flatmates → each flatmate lives elsewhere → introduces Habitiq there → PG owners see what tenants use → adopt for entire property. Structural virality built into the product.

---

## 8. Market Opportunity

- ~10 million urban shared-living residents in Tier 1/2 India
- 200,000+ organised PG properties
- No dominant product exists in this category in India
- Market tailwinds: urbanisation, rising co-living sector, digital-first Gen Z

**At 5% penetration:**
- Consumer: ~83,000 Pro flats × ₹99 = ₹82L/month
- B2B: ~10,000 properties × ₹499 = ₹49.9L/month

---

## 9. Competitive Landscape

| Product | What They Do | Why Habitiq Wins |
|---------|-------------|------------------|
| Splitwise | Expense splitting only | Doesn't touch duties, rotation, or assignment |
| WhatsApp | Messaging | No accountability, no rotation logic, messages buried |
| Todoist / Notion | Personal tasks | No shared rotation, no auto-assignment, no flat context |
| OurHome (US/EU) | Western chore app | Not in India, no INR, no PG context |
| Tody | Cleaning tracker | Individual use, no multi-person rotation |

**Habitiq is the only purpose-built, real-time, rotation-first shared living tool for India.**

---

## 10. Infrastructure & Costs

### Current: ₹0/month

| Service | Plan | Capacity |
|---------|------|----------|
| Vercel | Hobby (Free) | 6,000 build mins, global CDN |
| Firebase Auth | Spark (Free) | 10,000 MAU |
| Firestore | Spark (Free) | 50K reads/day, 20K writes/day |
| Firebase Storage | Spark (Free) | 5GB |

Handles ~100 active flats (600 users) for free.

### Firebase Cost at Scale (Blaze)

| Active Flats | Est. Monthly Cost |
|-------------|------------------|
| 100 | $0–5 |
| 500 | $10–25 |
| 2,000 | $50–100 |
| 10,000 | $200–400 |

### Scale Triggers

| Trigger | Action |
|---------|--------|
| >100 active flats | Upgrade Firebase to Blaze |
| >300 build mins/month | Upgrade Vercel plan |
| Need server logic | Add Firebase Cloud Functions |
| 1,000+ flats | Redis/Upstash caching layer |
| User photo uploads | Firebase Storage + CDN |
| Abuse prevention | Firebase App Check |

---

## 11. Team

| Name | Role |
|------|------|
| Venkata Sai Jaswanth E | Product & UI/UX — Co-founder |
| Upputuri Bhanu Kalyan | Full-Stack Engineering — Co-founder |

Built using AI-assisted development (Claude Code). A product going from concept to security-audited, multi-feature, production app in weeks — not months. Zero engineering salary burn in the build phase.

---

*Document version: 0.4.0 | June 2026 | Live at habitiq.app*
