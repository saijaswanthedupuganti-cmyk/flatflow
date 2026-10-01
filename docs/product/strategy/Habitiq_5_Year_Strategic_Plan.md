# Habitiq — 5-Year Strategic Plan & Execution Document

**Product:** Habitiq · **Current version:** v0.3.0 (Live trial, real users) **Founders:** Venkata Sai Jaswanth E (Product/Design) · Upputuri Bhanu Kalyan (Full-Stack) **Document purpose:** The single operating plan — vision, roadmap, verifications, test cases, AI content engine, distribution, and decision rules. **Created:** 12 June 2026 · **Review cadence:** Monthly (Section 12\)

---

## 0\. How to Use This Document

This is not a wish list. Every section has a **gate** — a measurable condition that must be true before money or months are spent on the next phase. If a gate fails, the plan says what to do instead. Decisions are made by the gates, not by mood.

---

## 1\. Immediate Corrections (This Week — Before Anything Else)

These are credibility bugs. Cost: \~₹2,000 and one afternoon.

| \# | Issue | Fix | Why it matters |
| :---- | :---- | :---- | :---- |
| 1 | Project folder `C:\garbage`, URL `garbage-liart.vercel.app` | Rename Vercel project to `habitiq`, move folder to `C:\habitiq` | Anyone who sees the URL — user, investor, journalist — associates "garbage" with the brand |
| 2 | Repo named `flatflow` | Rename to `habitiq` (GitHub auto-redirects old links) | Brand consistency, due-diligence hygiene |
| 3 | No owned domain | Buy **habitiq.in** (\~₹700/yr) and **habitiq.app** now; point Vercel to habitiq.in | You have live users on a throwaway URL. Every shared link today is a wasted brand impression |
| 4 | Market size contradiction (10M people vs 50M+ households) | Standardise: "**\~10M+ urban shared-living residents in Tier 1/2 India; 50M+ shared households nationally**" — cite source when pitching | An investor will catch the contradiction in 30 seconds |
| 5 | Price test mismatch (survey ₹49 vs pricing ₹99) | Survey should test the real price: "Would you pay ₹99/flat/month (₹15–25 per person)?" | You're validating a price you don't plan to charge |
| 6 | Duplicate `lib/` block in file structure | Delete the first copy | Doc accuracy |
| 7 | Doc version "1.0" vs product v0.3.0 | Version docs with the product: doc v0.3.x | Traceability |

**Gate 1 passed when:** habitiq.in is live, repo renamed, doc corrected.

---

## 2\. The 5-Year Vision (Honest Version)

**Year 5 ambition:** The default operating system for shared living in India, with paying co-living/PG operators as the revenue engine and consumer flats as the acquisition engine. International presence in 2–3 markets with similar shared-living density (SEA, Gulf).

**The honest math behind "top startup":**

- "Top startup in India" by revenue requires ₹100Cr+ ARR — not realistic for this category in 5 years.  
- "Top startup" by *category ownership* is realistic: be the name people say when asked "how does your flat manage duties?" — the way Splitwise owns expense splitting.  
- Target that is ambitious but defensible: **Year 5 \= 50,000 active flats, ₹3–5 Cr ARR, category leader in India, live in 2 international markets.**

| Year | Theme | Active flats | Revenue (ARR) | Team |
| :---- | :---- | :---- | :---- | :---- |
| Y1 (2026–27) | Validate & retain | 100–300 | ₹0–1L | 2 founders |
| Y2 (2027–28) | Consumer growth \+ first B2B | 1,500–3,000 | ₹8–15L | 2–4 (first hire: growth/support) |
| Y3 (2028–29) | B2B engine \+ native apps | 8,000–12,000 | ₹50L–1Cr | 5–8 |
| Y4 (2029–30) | Category leadership India | 25,000+ | ₹1.5–2.5Cr | 10–15 |
| Y5 (2030–31) | International \+ platform | 50,000+ | ₹3–5Cr | 15–25 |

**Decision rule:** Each year's plan activates only if the previous year's retention gate passed (Section 13). Growth without retention is renting users, not building a startup.

---

## 3\. Product Roadmap — Updates by Phase

### Phase A — Retention Core (Now → Month 6\)

The only goal: a flat that starts using Habitiq is still using it 60 days later.

| Update | Why | Priority |
| :---- | :---- | :---- |
| Push notifications (FCM) | \#1 retention lever — app is invisible without it | P0 |
| WhatsApp reminders (Cloud API) | India-specific: WhatsApp open rate ≫ push; "Your turn: Kitchen, due today" | P0 |
| Password reset in-app | Basic trust/recovery | P1 |
| Multi-currency conversion in balances | Cross-currency flats see broken balances today | P1 |
| Admin flat-wide balance matrix | Admin requested capability, low effort | P1 |
| Settlement double-confirmation | Money features need two-sided truth | P1 |
| Task photo proof (Firebase Storage) | Accountability upgrade; needed for B2B later | P2 |
| Shareable invite link (not just code) | Cuts onboarding friction in the growth loop | P2 |
| Flat announcements | Admin broadcast — replaces one more WhatsApp use case | P2 |

### Phase B — Growth Mechanics (Month 6–18)

| Update | Why |
| :---- | :---- |
| Referral system ("Invite a flat") with tracked attribution | Turns the structural growth loop into a measured channel |
| Streaks, badges, reliability leaderboard | Light gamification — retention for the Passive One persona |
| Onboarding templates (preset task packs: "3BHK student flat", "Working professionals") | 2-minute setup → 30-second setup |
| Native apps (React Native) | App Store presence \= trust \+ discoverability in India |
| Offline mode \+ background sync | Indian network reality |
| Hindi \+ Telugu \+ Tamil UI | Language is a moat international competitors can't copy quickly |
| Cloud Functions for server-side rotation | Removes client-trust problems before scale |

### Phase C — B2B Engine (Month 18–36)

| Update | Why |
| :---- | :---- |
| Operator super-dashboard (all properties, occupancy, duty compliance) | This is what PG owners pay for |
| White-label / custom branding | Co-living operators (Stanza, Colive, Settl) want their name on it |
| CSV/PDF reports, audit exports | Operator compliance and staff management |
| Staff mode (maids/cleaners as task executors, residents as verifiers) | Real PG workflow — residents verify, staff execute |
| Razorpay subscriptions (consumer) \+ invoicing (B2B) | Revenue infrastructure |
| Public API \+ webhooks | Property-management tool integrations |

### Phase D — Platform & International (Year 3–5)

| Update | Why |
| :---- | :---- |
| Flatmate compatibility scoring \+ flat discovery | Your existing expansion vision — only after duty management is dominant |
| Multi-language international (Bahasa, Arabic, Vietnamese) | SEA/Gulf entry |
| Country-specific payment rails (Stripe intl) | Non-UPI markets |
| AI flat assistant (natural-language: "swap my Thursday task with Rahul") | Differentiation once basics are commodity |

**Decision rule for any new feature idea:** It ships only if it improves one of three numbers — Day-60 flat retention, invite conversion rate, or paid conversion. If it improves none, it goes to a "Someday" list, not the roadmap.

---

## 4\. Verification Plan — What Must Be Verified, When

### 4a. Technical verifications (recurring)

| Verification | Frequency | Method |
| :---- | :---- | :---- |
| Firestore security rules | Every rules change \+ quarterly | Rules unit tests (Firebase emulator) — see Section 5 |
| Pending security items from audit: API key restrictions, CSP, App Check | Before 100 flats | Firebase Console \+ next.config.ts |
| Dependency vulnerabilities | Weekly | `npm audit` in CI; Dependabot on repo |
| Backup & restore | Monthly drill | Scheduled Firestore export to GCS; restore test to staging project |
| Uptime & error monitoring | Continuous | Vercel Analytics \+ Sentry (free tier) — you currently have zero error visibility in production |
| Load behaviour at scale | Before 500 flats | Seed-script load test against staging Firestore |

### 4b. Data integrity verifications

| Verification | Why |
| :---- | :---- |
| Rotation determinism: same queue \+ same OOS states → same assignee on every device | Core promise of the product; one inconsistency destroys trust |
| Balance conservation: sum of all pairwise balances in a flat \= 0 | Money math must be provably correct |
| Month-end close idempotency: closing twice cannot double carry-forwards | Financial correctness |
| Settlement ↔ expense ledger consistency | Mark-paid auto-creates expenses — verify no orphans/duplicates |

### 4c. Business & legal verifications (before scale/monetisation)

| Verification | Deadline |
| :---- | :---- |
| Privacy policy \+ Terms of Service published (DPDP Act 2023 compliance — you store personal data of Indian users) | Before public launch push |
| Trademark search \+ filing for "Habitiq" (India, Class 9/42) | Before spending on brand marketing |
| Company incorporation (Pvt Ltd or LLP) \+ founder equity agreement with Bhanu **in writing** | Before any revenue or incubator equity |
| GST registration | When revenue starts |
| App Store / Play Store policy compliance review | Before native app submission |

**The founder agreement is the most important line in this table.** Equity disputes kill more startups than competitors do. Decide the split, vesting (4 years, 1-year cliff is standard), and IP assignment now, while it's easy.

---

## 5\. Test Case Suite

Run before every deploy (automate progressively: Firestore rules tests first, then Playwright E2E).

### 5a. Rotation engine

| ID | Case | Expected |
| :---- | :---- | :---- |
| ROT-01 | Mark done on a daily task | Next active member in queue assigned; due date \+1 day |
| ROT-02 | Assignee goes OOS before due date | Task skips to next active member; OOS member's queue position preserved |
| ROT-03 | OOS member returns | Re-enters rotation at correct position; no double-assignment |
| ROT-04 | All members OOS except one | That member receives all tasks; no crash, no null assignee |
| ROT-05 | Member kicked while holding a task | Task reassigns to next-in-queue; removed from all queues |
| ROT-06 | New member joins mid-cycle | Appended to all queues; doesn't displace current assignee |
| ROT-07 | Two devices mark same task done within 1s | Exactly one completion recorded; queue advances once |
| ROT-08 | Overdue task at midnight | Status flips to overdue; stays with responsible person; reliability score impact recorded |

### 5b. Expenses & settlement

| ID | Case | Expected |
| :---- | :---- | :---- |
| EXP-01 | Equal split ₹1000 / 3 people | Splits sum to exactly 1000.00 (paisa rounding assigned deterministically) |
| EXP-02 | Custom split sums ≠ total | Save blocked with clear error |
| EXP-03 | Partial settlement ₹500 of ₹2000 owed | Balance shows ₹1500; settlement in timeline |
| EXP-04 | Settle more than owed | Blocked or capped — verify which, document it |
| EXP-05 | Delete expense that's partially settled | Balance recomputes correctly; settlement history retained |
| EXP-06 | Close month, then attempt to add expense to closed month | Blocked; expense goes to current month or is rejected |
| EXP-07 | Close month twice (double-tap / two admins) | Single close; carry-forward not duplicated |
| EXP-08 | Variable bill generated without amount entered | Prompted; cannot create instance with null amount |
| EXP-09 | Mark bill paid | Exactly one matching expense auto-created in Daily Splits |
| EXP-10 | Cross-currency balances (INR \+ USD between same pair) | Shown separately, clearly labelled (until conversion ships) |
| EXP-11 | Balance conservation | Σ all pairwise balances in flat \= 0 (automated invariant check) |

### 5c. Auth & access

| ID | Case | Expected |
| :---- | :---- | :---- |
| AUTH-01 | Google login on iOS Safari (real device) | Succeeds via auth proxy; no silent return to login |
| AUTH-02 | Login from unauthorized domain | Clear user-facing error, not silent failure |
| AUTH-03 | Non-admin attempts task create/delete via direct Firestore write | Rejected by rules |
| AUTH-04 | Member resolves a swap not addressed to them (direct write) | Rejected by rules |
| AUTH-05 | Kicked user with stale session attempts read/write | Rejected; redirected to onboarding |
| AUTH-06 | 9th member attempts join (8-cap) | Rejected at rules level with clear message |
| AUTH-07 | Expense edit by non-creator non-admin | Rejected |
| AUTH-08 | Activity log write with spoofed userId | Rejected by rules |

### 5d. Membership & multi-flat

| ID | Case | Expected |
| :---- | :---- | :---- |
| MEM-01 | Admin leaves with members present | Forced role transfer first |
| MEM-02 | Last member leaves | Flat \+ all subcollections deleted |
| MEM-03 | Leave flat with unsettled balances | Warning shown; define and document the policy (block vs warn) |
| MEM-04 | Flat switch | All stores reload; zero data bleed between flats |
| MEM-05 | Invite code reuse after member kicked | Define expected behaviour; test it |

### 5e. PWA, offline, real-time

| ID | Case | Expected |
| :---- | :---- | :---- |
| PWA-01 | Android install prompt → installed app launch | Works standalone |
| PWA-02 | Action attempted offline | Graceful failure message; no fake success |
| RT-01 | Member A marks done; member B's screen | Updates \< 2s without refresh |
| RT-02 | Firestore listener disconnect/reconnect | State reconciles; no duplicate toasts |

### 5f. Pre-release checklist (every deploy)

Lighthouse mobile ≥ 85 · No console errors on core flows · Dark \+ light mode visual pass · 320px-width layout pass · New Firestore rules deployed *before* dependent client code · Rollback plan known.

---

## 6\. The AI Video Ad Engine (Character-Led, ≤30s)

This is your build-once asset: a repeatable pipeline that turns one insight into a finished ≤30s video.

### 6a. Strategy first — where these videos go

Per your own CMO playbook: **no paid ads in Phase 1\.** So the engine serves two stages:

1. **Now → Month 6:** Organic Instagram Reels, YouTube Shorts, LinkedIn video, WhatsApp-forwardable clips. Goal: "this is literally us" shares, not conversions.  
2. **Month 6+ (after retention gate passes):** Same pipeline produces paid creatives for Meta ads (18–28, metro cities). You'll already know which scripts work organically — paid amplifies proven winners only.

**Rule: never pay to amplify an unproven script.** Organic performance is your free A/B test.

### 6b. The recurring character: "Arjun & The Flat"

One consistent AI-generated character cast beats random characters — recognition compounds.

- **Arjun (24, The Organised One)** — tracks everything, tired of being flat manager  
- **Vik (The Passive One)** — well-meaning, forgets everything  
- **Priya (The Travelling One)** — always out of station, feels guilty  
- Setting: a real-looking **Indian 3BHK** — steel utensils, geyser switch, clothes rack on balcony, mosquito racket. Never Western apartments.

Consistency method: generate a character reference sheet (front/side/expressions) once; use image-to-video with the reference frame in Kling/Seedance/Veo so Arjun looks the same across every ad.

### 6c. Production pipeline (per video, \~2–3 hours)

1. **Insight** → pick one system failure from the CMO problem table (e.g., "trash overflowed because everyone assumed someone else would do it")  
2. **Script** → I write it: hook (0–3s), problem (3–10s), mechanism (10–20s), payoff line \+ logo (20–25s). Hard cap 30s; target 20s.  
3. **Shot list** → 3–5 shots, each ≤ 6s (model-friendly clip lengths)  
4. **Generate** → Kling 3/Veo 3/Seedance per your video-shortform workflow; image-to-video from character reference frames  
5. **Assemble** → CapCut/Premiere: cuts, captions (always — 80% watch muted), Habitiq violet end-card, tagline  
6. **Voice** → ElevenLabs Hindi-English mix or on-screen text only (test both)  
7. **Publish** → Reels \+ Shorts \+ LinkedIn; WhatsApp-optimised version (square, \<10MB)

### 6d. The ≤30s structure (every ad follows it)

| Time | Beat | Example |
| :---- | :---- | :---- |
| 0–3s | Hook — visual conflict, no logo | Arjun staring at overflowing trash; Vik gaming in background |
| 3–10s | The system failure | "Nobody agreed whose turn it was. Again." |
| 10–20s | The mechanism (one feature only) | Phone notification: "Vik — Trash, due today." Vik gets up. |
| 20–25s | Payoff \+ brand | "Fair duties. Zero arguments. **Habitiq.**" |

One ad \= one problem \= one feature \= one belief. Never two.

### 6e. First 6 scripts (production order)

1. "Whose turn is it?" — rotation engine (the category-defining ad)  
2. Priya goes home for a week — OOS skip ("go home guilt-free")  
3. The ₹2,000 nobody remembers — balances & settle up  
4. The new flatmate — invite code, live in 2 minutes  
5. The silent flat manager — "nobody should have to nag" (Arjun's origin story)  
6. Exam season — swap requests ("formal swap, not a chat message")

Cadence: 1/week for 6 weeks → review saves, shares, completion rate → double down on top 2 formats.

### 6f. Cost reality

| Item | Monthly |
| :---- | :---- |
| Video gen credits (Kling/Veo tier) | ₹1,500–4,000 |
| ElevenLabs starter | \~₹450 |
| CapCut | Free |
| **Total organic phase** | **\< ₹5,000/month** |
| Paid amplification (Phase 3): start ₹10,000/month on the single best-proven creative; scale only if CAC \< ₹150/flat with retention holding. |  |

---

## 7\. Who to Reach — Distribution Map

### Tier 1: Users (now)

- **College/PG WhatsApp & Telegram groups** in Hyderabad, Bengaluru, Pune, Chennai — your alumni network first (Vishnu Institute → working flats in metros)  
- **Reddit:** r/bangalore, r/hyderabad, r/india flat-hunting threads — answer real "flatmate problems" posts, don't spam  
- **Instagram flat-life/relatable creators** (10k–100k followers) — free Pro tier for their flat in exchange for one honest reel

### Tier 2: Ecosystem (Month 3–12)

- **T-Hub Hyderabad** (your cohort application — primary), **iB Hub, WE Hub** (if applicable), **Headstart Hyderabad** weekend events  
- **Product Hunt \+ Peerlist launch** once retention proven — also serves your personal design brand  
- **Build-in-public on LinkedIn/X** — you're already doing this; the "designer who shipped a production app with AI" story is genuinely differentiated press bait for YourStory/Inc42 later

### Tier 3: B2B (Month 12+, only after retention gate)

- **Local PG owners in Hyderabad first** — 10 in-person meetings before emailing any chain. Learn what they'd actually pay for.  
- **Then:** Settl, Colive, Stanza Living, Zolo — approach with case studies from local PGs, not cold pitches  
- **University hostels** — wardens have the same problem at 10x scale

### Tier 4: Capital (only if needed)

Bootstrapping is viable at your cost structure (₹0 infra now, \<₹5k/month with content engine). Raise only if B2B demand outpaces your ability to serve it. If raising: T-Hub demo days, 100X.VC, Titan Capital, First Cheque — all comfortable with pre-revenue consumer-India.

**Decision rule:** Don't email Stanza Living before you have 90-day retention data from 50+ flats. B2B buyers buy proof, not vision.

---

## 8\. International Expansion (Year 3+, gated)

**Gate:** India unit economics proven (CAC \< 3-month revenue, Day-90 retention \> 50%) AND ₹50L+ ARR. Until then, international is a distraction.

| Market | Why | Entry adaptation |
| :---- | :---- | :---- |
| UAE / Gulf | Massive Indian expat shared-living population; same cultural patterns; high payment willingness | AED pricing (already supported), Arabic later |
| SEA (Vietnam, Philippines, Indonesia) | Dense urban shared living, mobile-first, underserved | Local language UI, local payment rails |
| UK student cities | Indian/intl student houses; OurHome is weak | GBP (supported), term-calendar presets |

Entry method: zero-office, community-led — same playbook as India Tier 1 (student groups, expat forums), localised video ads from the same AI pipeline with localised characters. This is where the character-based ad engine pays off twice.

---

## 9\. Business Model Evolution

| Stage | Model | Note |
| :---- | :---- | :---- |
| Now → retention gate | 100% free | Charging before retention proof poisons the data |
| Month 6–12 | Pro ₹99/flat/month (₹15–25/person — frame it this way everywhere) | Gate features behind Pro only after they're loved free: push customisation, photo proof, exports, history archive |
| Month 12+ | Business ₹499–999/property/month for operators | Real margin lives here; consumer Pro mostly funds infra |
| Year 3+ | Annual plans (₹999/flat/yr), white-label licensing, API tier |  |

Honest projection check: your doc's ₹2.25L/month at 10,000 flats is **fine as a consumer-only floor** — but the model only becomes a real business with B2B. 200 operator properties at ₹750 avg \= ₹1.5L/month from a sales motion two founders can actually run. Plan the B2B motion as the revenue strategy, consumer as the distribution strategy.

---

## 10\. Team & Operating Plan

| Trigger | Hire |
| :---- | :---- |
| 500+ flats, support eating founder time | Part-time support/community (intern-level, Hyderabad) |
| B2B pipeline \> 10 conversations | Founder (Sai) goes full-sales; contract dev backfills product with Bhanu |
| ₹1L+/month revenue | First full-time engineer |
| International launch | Country community manager (contract) |

Founder roles, written down now: **Sai** — product, design, growth, content, B2B sales. **Bhanu** — engineering, infra, security, reliability. Disagreements: whoever owns the domain decides; the other commits.

---

## 11\. Risk Register (the things that actually kill this)

| Risk | Likelihood | Mitigation |
| :---- | :---- | :---- |
| Flats churn after novelty (the real killer) | High | Phase A is 100% retention features; WhatsApp reminders before any growth spend |
| Founder burnout (you have a day job) | High | Fixed weekly hours budget; content engine automates marketing labour; say no to feature requests outside the decision rule |
| Founder dispute (no written agreement) | Medium | Section 4c — written agreement this month |
| WhatsApp/Splitwise adds a "chores" feature | Medium | Speed \+ India-specific depth (OOS, PG workflows, languages) — incumbents won't build for Indian PG owners |
| Firebase costs spike with scale | Low | Cost triggers already mapped (your Section 10); Cloud Functions \+ caching plan ready |
| DPDP non-compliance complaint | Low but severe | Privacy policy \+ consent before public push |

---

## 12\. Operating Cadence — How Decisions Get Made

- **Weekly (30 min, founders):** metrics vs gates, one shipped thing, one learned thing, next week's single priority.  
- **Monthly:** review this document; update gates; kill or promote experiments. One video-ad performance review.  
- **Quarterly:** phase gate decision — advance, hold, or pivot. The gates decide, not enthusiasm.  
- **My role (Claude):** strategy refinement, every script/spec/audit/test plan, Claude Code implementation. **Your role:** every decision with money, equity, users, or partners — and pressing publish.

---

## 13\. The Gates (Master List)

| Gate | Condition | Unlocks |
| :---- | :---- | :---- |
| G1 — Hygiene | Domain live, repo renamed, founder agreement signed, privacy policy up | Public marketing push |
| G2 — Retention | ≥ 50 flats, Day-60 flat retention \> 50%, task completion \> 75% | Monetisation \+ paid ads |
| G3 — Revenue | 100 paying flats OR 5 paying operators | First hire, B2B scale push |
| G4 — B2B proof | 25 operator properties, \< 5% monthly churn | Fundraise decision (optional) |
| G5 — India PMF | 8,000+ flats, ₹50L ARR, CAC \< 3-mo revenue | International entry |

If a gate hasn't passed in its expected window, the response is **diagnose and fix the gate**, never "skip and grow anyway."

---

## 14\. Next 30 Days — Exact Sequence

1. **Week 1:** Section 1 corrections · buy habitiq.in · publish privacy policy \+ ToS · founder agreement draft with Bhanu  
2. **Week 2:** Push notifications (FCM) shipped · Sentry added · character reference sheet for Arjun generated · script 1 written  
3. **Week 3:** WhatsApp reminder MVP (even manual/template-based) · video ad \#1 produced and published · Firestore rules tests for AUTH-03/04/08  
4. **Week 4:** Video ad \#2 · trial feedback survey (corrected ₹99 framing) sent to all active flats · first monthly review against G2 metrics

---

*"Your flat, on autopilot. Built in India. For everyone who has ever had a flatmate."*

**Maintained by:** Sai Jaswanth · **Strategy partner:** Claude · **Doc version:** 0.3.0-strategy.1  
