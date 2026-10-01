# Habitiq — Master Product Document
**Version:** 1.0 | **Date:** June 2026 | **Status:** Live, Trial Phase

> This is the single source of truth for all Habitiq presentations, pitches, investor conversations, and future planning. Keep this document updated as the product evolves.

---

## 1. What Is Habitiq?

Habitiq is a **shared living management platform** that automates duty rotation among flatmates. It gives every flat a transparent, real-time system for chore management — eliminating arguments, forgotten tasks, and the awkward "whose turn is it?" conversation.

**The name:**
- **Habitat** — the shared space where people live together
- **Habit** — the recurring routines of shared living that repeat and rotate
- **IQ** — the intelligence that skips absent members, reassigns overdues, tracks reliability

**Live at:** https://garbage-liart.vercel.app *(moving to habitiq.in / habitiq.app)*

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
| Primary | Violet → Indigo gradient | `#7c3aed` → `#4338ca` |
| Background (dark) | Deep black | `#0a0a0a` |
| Background (light) | Clean white | `#ffffff` |
| Success | Green | `#16a34a` |
| Warning | Amber | `#d97706` |
| Danger | Red | `#dc2626` |

### Typography
**Inter** — clean, legible, modern. No decorative fonts.

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

### Bills & Expenses Module *(Recently Shipped)*
- Recurring bills with rotating payer (e.g. electricity bill cycles through members)
- One-off expense tracking (e.g. groceries, Netflix)
- Split modes: equal split or custom split
- Multi-currency: INR, USD, EUR, GBP, AED, SGD, AUD
- Balance tracking per member
- Settle Up flow
- All data cleaned on flat deletion

### Real-Time Sync
- Firestore onSnapshot listeners — every change appears instantly on all devices
- No refresh required
- Graceful offline detection

### Progressive Web App (PWA)
- Installable on Android home screen
- Service worker for offline fallback
- Dynamic app icon generation

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

### Recommended Model: Freemium (launching Phase 3)

| Tier | Price | What's Included | Target User |
|------|-------|-----------------|-------------|
| **Free** | ₹0/month | Up to 6 members, 10 tasks, all core features | Student flats, trial users |
| **Pro** | ₹99/flat/month | Unlimited members + tasks, push notifications, photo proof on task completion, analytics export | Active flats, working professionals |
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

---

## 11. Go-to-Market Strategy

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

## 12. Trial Phase Metrics (Targets)

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

## 13. Product Principles

1. **Fairness is the product.** Every feature exists to make shared living more fair.
2. **Zero friction onboarding.** Flat live in under 2 minutes.
3. **Transparency by default.** All members see everything — visibility is the accountability mechanism.
4. **The system is the authority.** Nobody should have to nag.
5. **Mobile first, always.** Most users open the app standing in the kitchen.
6. **Real-time or nothing.** If changes aren't instant, trust breaks down.

---

## 14. How Habitiq Was Built

**Method:** AI-Assisted Product Development

**The team:**
- **Venkata Sai Jaswanth E** *(Founder)* — Product vision, user flows, design decisions, feature prioritisation, business logic, brand identity
- **Claude Code (Anthropic)** — Full implementation: Next.js architecture, Firestore schema design, rotation engine, auth flows, security rules, deployment
- **Gemini** — Architecture pressure-testing, edge case exploration

**Why this matters:** A UI/UX designer with no backend background shipped a production-ready, security-audited, real-time web application in weeks. The product went from concept to live users without a traditional engineering team. This is the new model for early-stage product development.

---

## 15. The Team

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

## 16. Key Links & References

| Resource | Link / Location |
|----------|----------------|
| Live app | https://garbage-liart.vercel.app |
| Target domain | habitiq.in / habitiq.app |
| Codebase | C:\garbage |
| Obsidian vault | C:\garbage\project_1 |
| Session log (Google Sheet) | ID: 1Z6EoxX0x5tO25HsQuUsNckoV3Zfhsv8JPmfOXC_yG0s |
| Bug/feature tracker | ID: 1zzRYfTGuJaGLES8hr65rnDCRN2x7WnT81qG09K3Dryw |
| Vercel project | https://vercel.com/saijaswanthedupuganti-cmyks-projects/garbage |

---

## 17. Presentation Talking Points

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

---

*Last updated: 2026-06-05 | Maintained by: Venkata Sai Jaswanth E + Claude Code*
