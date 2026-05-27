# FlatFlow — Product & Business Documentation

> **Version:** v0.1.0 (Trial / Pilot Phase)  
> **Live URL:** https://flatsflow.netlify.app  
> **Status:** 🟢 Live — Real User Trial  
> **Last Updated:** 2026-05-27

---

## 1. What is FlatFlow?

FlatFlow is a **shared living management platform** that automates household duty rotation among flatmates. It eliminates arguments, forgotten tasks, and unfair workloads by giving every flat a transparent, real-time system for chore management.

### The Problem We Solve

| Problem | Without FlatFlow | With FlatFlow |
|---------|-----------------|---------------|
| Who cleans next? | Arguments every week | Auto-assigned, no discussion |
| Someone is travelling | Task skipped or dumped on others | Smart skip — resumes automatically |
| "I forgot" | No accountability | Overdue tracking + reliability score |
| Admin burden | One person does everything | All members self-manage |
| Swap needed | WhatsApp chaos | In-app swap request + accept/decline |

---

## 2. Target Users (Trial Phase)

### Primary: Bachelor Flats & PG Accommodations
- 3–6 people per flat
- Age 18–30 (college students, young professionals)
- Already using WhatsApp groups for flat coordination
- Pain point: chore fairness and accountability

### Secondary: Co-living Spaces
- Managed properties with multiple rooms
- Admin (property manager) + residents
- Need: structured duty system without manual tracking

### Geographic Focus (Trial)
- India — Tier 1 & Tier 2 cities (Hyderabad, Bengaluru, Pune, Chennai)
- High student + young professional population density
- Strong smartphone penetration

---

## 3. Current Feature Set (v0.1.0)

### Core Features ✅
| Feature | Description | Status |
|---------|-------------|--------|
| Flat creation | Admin creates flat, gets unique invite code | ✅ Live |
| Member joining | Members join via invite code | ✅ Live |
| Smart rotation | Auto-assign duties, skip absent members | ✅ Live |
| Task completion | Mark done → next person auto-assigned | ✅ Live |
| Overdue tracking | Tasks marked overdue, stay with responsible person | ✅ Live |
| Swap requests | Request a member to cover your task | ✅ Live |
| Activity log | Full audit trail of all flat activity | ✅ Live |
| Analytics | Completion grid, reliability scores | ✅ Live |
| Calendar view | Monthly task history per member | ✅ Live |
| Google Sign-In | One-tap login, desktop + mobile | ✅ Live |
| Email/Password | Alternative login method | ✅ Live |
| Dark mode | Full dark/light theme | ✅ Live |
| Mobile responsive | Bottom nav for phones | ✅ Live |
| Real-time sync | All flatmates see updates instantly | ✅ Live |

### Admin Controls ✅
| Control | Description |
|---------|-------------|
| Create tasks | Define name, type, priority, frequency |
| Delete tasks | Remove tasks from rotation |
| Manual override | Assign any task to any member |
| Member status | See all members and their reliability scores |
| Invite code | Share code to add new members |

---

## 4. Trial Phase Goals

### What We're Testing
This is a **real-user pilot** to validate:

1. **Core loop adoption** — Do flatmates actually use the app daily/weekly?
2. **Completion rate** — What % of tasks are marked complete before overdue?
3. **Retention** — Does the flat keep using it after Week 1?
4. **Swap usage** — How often do members swap tasks?
5. **Admin satisfaction** — Is the admin burden reduced?
6. **Mobile vs desktop** — Which device do users prefer?

### Metrics to Track (Per Flat)

| Metric | Target (Trial) | Why |
|--------|---------------|-----|
| Tasks completed / week | ≥ 80% of due tasks | Validates core loop |
| Active members / flat | ≥ 3 logins / week | Adoption signal |
| Swap request rate | 1–3 per flat / week | Feature validation |
| Overdue rate | < 20% of tasks | Reliability signal |
| Flat retention at 30 days | > 60% of flats | Product-market fit signal |
| Average session length | > 2 minutes | Engagement depth |

### How to Collect Feedback
- Direct WhatsApp / chat messages from flatmates
- Watch the Activity Log — it shows real usage patterns
- Ask: "What's missing?" after 2 weeks of use

---

## 5. Expansion Roadmap

### Phase 1 — Trial (NOW → 3 months)
**Goal:** Validate with 5–20 real flats. Learn what works.

- [x] Core rotation engine
- [x] Google + email login
- [x] Real-time Firestore sync
- [x] Mobile-first UI
- [x] Security audit
- [ ] Collect user feedback
- [ ] Fix bugs from real usage
- [ ] Track metrics above

---

### Phase 2 — Growth (3–6 months)
**Goal:** Grow to 100+ flats. Add features users actually asked for.

#### High Priority (based on expected feedback)
- [ ] **Push notifications** — remind members of upcoming/overdue tasks (Firebase Cloud Messaging)
- [ ] **WhatsApp integration** — send task reminders via WhatsApp API
- [ ] **Guest member invite** — invite by phone number or link (not just invite code)
- [ ] **Task photo proof** — member uploads photo when marking task done
- [ ] **Admin remove member** — handle flatmates who move out
- [ ] **Flat name editing** — admin can rename the flat
- [ ] **Member nickname editing** — members can change their own nickname
- [ ] **Task history archive** — view completed tasks older than 30 days
- [ ] **Progressive Web App (PWA)** — installable on home screen (Android + iOS)

#### Medium Priority
- [ ] **Multi-flat switcher** — user can belong to multiple flats (different cities, PG owner managing several flats) and switch between them from their profile — like Gmail account switching. Profile icon → dropdown shows all flats → tap to switch instantly. Also: "Join another flat" and "Create a new flat" options inside dashboard.
- [ ] **Flat announcements** — admin posts a message to all members
- [ ] **Expense splitting (basic)** — track shared expenses alongside duties
- [ ] **Task templates** — common flat task presets (Bathroom, Kitchen, etc.)

---

### Phase 3 — Scale / Monetisation (6–18 months)
**Goal:** Sustainable business. Serve co-living operators and PG owners.

#### Business Model Options

**Option A — Freemium (Recommended for Start)**
| Tier | Price | Limits | Target |
|------|-------|--------|--------|
| Free | ₹0 | Up to 6 members, 10 tasks, basic features | Student flats |
| Pro | ₹99/flat/month | Unlimited members + tasks, push notifications, photo proof | Active flats |
| Business | ₹499/property/month | Multiple flats, admin dashboard, analytics export | PG owners, co-living operators |

**Option B — Pay Per Flat**
- ₹49/flat/month flat rate
- No feature restrictions
- Simpler pricing

**Option C — B2B Only**
- Sell directly to co-living companies
- White-label product
- Higher per-customer revenue

#### Phase 3 Features
- [ ] **Stripe / Razorpay payment integration** — in-app subscription billing
- [ ] **Admin super-dashboard** — property managers see all their flats in one view
- [ ] **Automated reminders** — email + push + WhatsApp at configurable times
- [ ] **Reliability score rewards** — gamification (badges, streaks)
- [ ] **API for integrations** — connect to property management tools
- [ ] **Analytics export** — CSV / PDF reports for PG owners
- [ ] **Custom branding** — white-label for co-living operators
- [ ] **Mobile app (React Native)** — true native iOS + Android app

---

## 6. Technical Scale Plan

### Current Infrastructure (Trial Phase)
| Service | Plan | Limits | Cost |
|---------|------|--------|------|
| Netlify | Free | 300 build minutes/month, 100GB bandwidth | ₹0 |
| Firebase Auth | Free (Spark) | 10,000 MAU | ₹0 |
| Firestore | Free (Spark) | 50,000 reads/day, 20,000 writes/day | ₹0 |
| Firebase Storage | Free (Spark) | 5GB storage | ₹0 |

**Estimate:** Current infrastructure handles ~50–100 active flats (300–600 users) for free.

### When to Upgrade
| Trigger | Action |
|---------|--------|
| > 100 active flats | Upgrade Firebase to Blaze (pay-as-you-go) |
| > 300 build minutes/month | Upgrade Netlify to Pro ($19/month) |
| Needing server-side logic | Add Firebase Cloud Functions |
| > 10,000 MAU | Firebase Auth auto-scales on Blaze |

### Firebase Cost Estimate at Scale (Blaze Plan)
| Scale | Est. Monthly Firebase Cost |
|-------|--------------------------|
| 100 flats (~600 users) | ~$0–5 |
| 500 flats (~3,000 users) | ~$10–25 |
| 2,000 flats (~12,000 users) | ~$50–100 |
| 10,000 flats (~60,000 users) | ~$200–400 |

*Firestore pricing: $0.06/100K reads, $0.18/100K writes, $0.10/GB storage.*

### Architecture Changes for Scale
1. **Cloud Functions** — Move business logic (reliability scores, auto-overdue checks) from client to server
2. **Firestore indexes** — Add composite indexes for analytics queries as data grows
3. **Caching layer** — Cache flat metadata in memory (Redis / Upstash) at 1,000+ flats
4. **CDN assets** — Move user-uploaded photos to Firebase Storage with CDN URLs
5. **Rate limiting** — Add Firebase App Check to prevent abuse

---

## 7. Security Status

Full security audit completed 2026-05-27. See [SECURITY_AUDIT.md](./SECURITY_AUDIT.md).

| Area | Status |
|------|--------|
| Firestore rules | ✅ Hardened (role-based, no anonymous access) |
| HTTP security headers | ✅ 6 headers configured |
| Auth error messages | ✅ Sanitised (no user enumeration) |
| Input validation | ✅ maxLength on all inputs |
| ID generation | ✅ crypto.randomUUID() |
| External dependencies | ✅ Google icon localised |
| Firebase API keys | ✅ Client-side by design, protected by Firestore rules |
| Google OAuth (mobile) | ✅ Custom authDomain proxy via Netlify |

---

## 8. Known Limitations (v0.1.0)

| Limitation | Impact | Phase to Fix |
|-----------|--------|-------------|
| No push notifications | Users must open app to see updates | Phase 2 |
| No member removal | Left flatmates retain read access | Phase 2 |
| No flat deletion | Orphaned flats stay in DB | Phase 2 |
| No password reset UI | Must use Firebase's default email | Phase 2 |
| Single flat per user | Can't be in multiple flats | Phase 2 |
| No offline mode | Needs internet connection | Phase 3 |
| No native mobile app | Web only (but mobile-optimised) | Phase 3 |
| No expense tracking | Duties only, no money | Phase 3 |

---

## 9. Team

| Name | Role |
|------|------|
| Venkata Sai Jaswanth E | UI/UX Designer & Co-founder |
| Upputuri Bhanu Kalyan | Full-Stack Developer & Co-founder |

---

## 10. Trial Feedback Template

When collecting feedback from trial users, ask:

1. How often do you open FlatFlow? (Daily / Few times a week / Weekly / Rarely)
2. Has chore fairness improved in your flat? (Yes / Somewhat / No)
3. What's the #1 thing you wish FlatFlow could do?
4. Would you recommend this to another flat? (Yes / Maybe / No)
5. Would you pay ₹49/month to keep using it? (Yes / Maybe / No)

---

*This document is a living roadmap — update it as user feedback comes in.*
