# Habitiq — Co-Founder Brief
**Confidential | Prepared by: Venkata Sai Jaswanth E, Founder — Habitiq | June 2026**

---

## A Note Before You Read

This document is for someone considering joining Habitiq as a co-founder. It covers what we've built, where we're going, what we need, and how we work together — honestly and without fluff.

This is not a pitch deck. It's a working brief. If anything here needs a conversation, let's have it.

---

## 1. What Is Habitiq?

Habitiq is a **shared living management platform** for friend groups and flatmates. It automates duty rotation, tracks expenses, and removes the social friction of managing a shared home.

**The name:**
- **Habitat** — the shared space
- **Habit** — the recurring routines of shared living
- **IQ** — the intelligence that makes it fair and automatic

**Live product:** https://garbage-liart.vercel.app
**Moving to:** habitiq.in / habitiq.app

**In one line:** We are the operating system for shared flats in India.

---

## 2. The Problem We're Solving

Every shared flat in India runs on informal agreements that break down. Verbal chore assignments get forgotten. WhatsApp reminders cause tension. Nobody agrees on "whose turn" it is. When one person goes out of station, tasks pile up. When a new person joins, nobody updates the roster.

**50M+ people live in shared rented accommodation in India.** None of them have a proper tool for this. That is the market we are building for.

---

## 3. Current Status

**Phase:** Live trial with real users
**Version:** v0.1.0

### What Is Built and Working Today

| Area | Status |
|------|--------|
| Google + email authentication | ✅ Live |
| Smart rotation engine (core algorithm) | ✅ Live |
| Task management with overdue tracking | ✅ Live |
| Swap request system (formal accept/decline) | ✅ Live |
| Multi-flat support (one account, multiple flats) | ✅ Live |
| Member management (join, leave, kick, transfer admin) | ✅ Live |
| Analytics and reliability scores per member | ✅ Live |
| Calendar view | ✅ Live |
| Activity log (audit trail) | ✅ Live |
| Bills and expenses module (recurring + one-off, multi-currency) | ✅ Live |
| Progressive Web App (installable on Android) | ✅ Live |
| Dark and light mode | ✅ Live |
| Security audit — 18 issues found and fixed | ✅ Done |

### What the Product Is NOT Yet
- Not on the App Store (web app only)
- No push notifications
- No WhatsApp reminders
- No payment/subscription billing
- No photo proof on task completion

---

## 4. What We Are Building Next

### Phase 2 — Growth (3–6 months from now)
- Push notifications (Firebase Cloud Messaging)
- WhatsApp task reminders
- Photo proof on task completion
- Guest invite by link
- Flat announcements (admin pinned message)
- Task templates (preset packs)
- Password reset UI

### Phase 3 — Scale & Revenue (6–18 months)
- Pro subscription billing (Razorpay/Stripe)
- Automated reminder system (email + push + WhatsApp)
- Reliability score rewards and badges
- Analytics export
- Native mobile app (iOS + Android)
- API for integrations
- White-label for co-living operators

---

## 5. Revenue Model

**Model:** Freemium — free forever for basic use, paid for power features

| Tier | Price | For |
|------|-------|-----|
| Free | ₹0/month | Up to 6 members, 10 tasks, all core features |
| Pro | ₹99/flat/month | Unlimited, push notifications, analytics export |
| Power | ₹249/flat/month | Pro + custom branding, priority support |

**Projections:**
- 500 flats → ~₹7,500/month
- 2,000 flats → ~₹40,000/month
- 10,000 flats → ~₹2,25,000/month

Infrastructure costs stay under $400/month even at 10,000 flats. Margins are strong at every tier.

---

## 6. The Stack (Non-Technical Summary)

The product runs on:
- **Next.js** (web framework) — fast, modern, used by major products
- **Firebase** (Google's backend) — real-time database and authentication
- **Vercel** (hosting) — global CDN, instant deployments

**Why this matters to you:** The stack is industry-standard, well-documented, and easy to hire for. Nothing exotic. Nothing that locks us in.

**Current infrastructure cost:** ₹0/month (all free tier). Scales to ~$400/month at 10,000 active flats.

---

## 7. How This Was Built

The product was built by Sai (product + design) working with Claude Code (AI-assisted development). The entire rotation engine, Firestore schema, auth flows, security rules, and deployment pipeline were built this way.

**What this means for you:**
- You are not inheriting a mess — the codebase is clean, typed (TypeScript strict), and fully documented
- There is a SECURITY_AUDIT.md with all 18 issues found and fixed
- There is a PRODUCT.md with the full business roadmap
- There is a CLAUDE.md with full context for any engineer picking this up

A traditional team would have taken 3–6 months to get here. We got here in weeks.

---

## 8. Who I Am

**Venkata Sai Jaswanth E — Founder, Habitiq**
- UI/UX Designer and product thinker
- Currently: UI/UX Designer Trainee at MyClassboard (Hyderabad)
- Education: B.Tech IT, Vishnu Institute of Technology (2023)
- Certifications: Google UX Certificate, AI-Powered UI/UX Certificate
- I designed and shipped Habitiq from scratch — concept to live, security-audited product
- I think in systems before I think in screens
- Email: saijaswanthedupuganti@gmail.com
- Portfolio: https://jaswanth-portfolio-three.vercel.app/

---

## 9. What I Am Looking For in a Co-Founder

I am looking for someone who complements what I bring. I handle product thinking, design, and the AI-assisted development pipeline. I need someone who can own one or more of the following:

**Technical Co-founder (Engineering-focused):**
- Owns the codebase independently (Next.js / React / TypeScript / Firebase)
- Can build and ship Phase 2 features without hand-holding
- Can set up Firebase Cloud Functions, push notifications, native app
- Reviews and extends security posture
- Can eventually lead an engineering team

**Growth Co-founder (Business-focused):**
- Owns go-to-market: user acquisition, partnerships, content
- Drives the trial phase metrics (flat retention, engagement)
- Builds relationships with co-living brands and student communities
- Can own the subscription billing and conversion funnel
- Can eventually lead sales and partnerships

**What I value in a partner:**
- Ownership mentality — you treat this as yours, not mine
- Bias for doing — we move fast, we learn from users
- Honesty over comfort — if something isn't working, say it
- Long-term thinking — we are building a real company, not a quick exit

---

## 10. Roles and Responsibilities (Proposed)

| Area | Founder (Sai) | Co-founder |
|------|---------------|------------|
| Product vision and roadmap | Lead | Input |
| UI/UX design | Lead | Input |
| AI-assisted development | Lead | Support |
| Engineering (independent) | Support | Lead *(if technical)* |
| User research and feedback | Shared | Shared |
| Go-to-market and growth | Support | Lead *(if growth)* |
| Business development and partnerships | Support | Lead *(if growth)* |
| Revenue and billing | Shared | Shared |

---

## 11. Equity and Legal Framework

*The following is a proposed starting framework. All final terms should be reviewed by a legal advisor and formalised in a Co-Founder Agreement before any binding commitments are made.*

### Proposed Equity Split (Starting Point for Discussion)
| Person | Title | Role | Proposed Equity |
|--------|-------|------|-----------------|
| Venkata Sai Jaswanth E | Founder | Product + Design + AI Dev | 60% |
| TBD | Co-founder | Engineering or Growth | 40% |

### Vesting Schedule (Industry Standard)
- **Cliff:** 12 months — no equity vests before the 1-year mark
- **Total vesting:** 4 years
- **Monthly vesting after cliff:** 1/48th of total equity per month

This protects both parties. If the partnership doesn't work out in the first year, neither side is left holding dead equity.

### What Should Be in the Co-Founder Agreement
1. Equity split (as above)
2. Vesting schedule with cliff
3. Roles and responsibilities (clear ownership areas)
4. Decision-making process (who has final say on product vs. engineering vs. business)
5. IP assignment — all work on Habitiq belongs to the company, not to individuals
6. Non-compete clause (scope and duration — typically 1–2 years within the same category)
7. Departure terms — what happens to equity if a founder exits
8. Founder salary (none until Series A or revenue threshold, suggested: ₹30,000/month survival stipend if needed before that)

### Next Steps Before Formalising
1. Trial period — work together for 4–8 weeks before signing anything. Build something together. See how we collaborate.
2. Legal review — both parties should have the agreement reviewed by a startup lawyer independently
3. Company formation — register as a Private Limited Company (Pvt Ltd) in India under MCA. Recommended: use Razorpay Rize or Startups.in for assisted registration (~₹6,000–15,000)

---

## 12. What Success Looks Like

**In 3 months:** 20+ active flats, users genuinely relying on the product, zero critical bugs

**In 12 months:** Pro tier live, 500+ paying flats, ₹50,000+/month revenue, App Store listing

**In 3 years:** Market leader in shared living management in India, 50,000+ flats, B2B co-living partnerships, Series A conversation

---

## 13. Competitive Position

Splitwise tracks money. Todoist tracks tasks. WhatsApp is chaos. None of them are built for shared living rotation in India. Habitiq is the only product that owns this specific problem, for this specific audience, at this price point.

---

## 14. One Last Thing

This is a real product, with real users, solving a real problem. It is not a side project. It is not a portfolio piece. It is a company in the making, and I am looking for someone who sees the same thing.

If that's you — let's talk.

**Venkata Sai Jaswanth E**
Founder, Habitiq
saijaswanthedupuganti@gmail.com
+91 9491082728

---

*This document is confidential and prepared for co-founder evaluation purposes only. Please do not distribute without permission.*

*Last updated: 2026-06-05*
