# Habitiq Discover — Trust, Legal & Safety (Starting Stage)

**Based on:** Sai's research PDF — *Flatmate & PG Discovery: Market, Trust, Legal, and Safety Landscape (India + Global)*  
**Product scope:** Android APK (`garbage-f79f7`) — **Use a flat** (vacancies) + **Find the person** (seekers) + in-app chat  
**Date:** 13 August 2026  
**Audience:** Sai (founder) — launch decisions for Discover MVP

---

## 1. What still matters from the PDF (ignore the fluff)

The PDF's market-size projections (₹1T co-living, global CAGR, competitor bed counts) are **directional only** and dated. What **still holds for Habitiq today**:

| Theme | Why it matters now |
|-------|-------------------|
| **Informal channels dominate** | WhatsApp/Facebook groups have weak verification → users will compare Habitiq to scam-prone channels. Safety messaging is a differentiator, not optional. |
| **Trust signals drive conversion** | Badges and verification labels on cards increase click-through, especially for women and risk-averse users. Full behavioral scoring can wait; **basic labels** ("Verified email", "Active flat member") cannot. |
| **Common incident types** | Fake listings, pay-before-viewing scams, fake profiles, harassment in chat, unsafe viewings. These map directly to Discover chat + vacancy browse. |
| **Women face elevated risk** | Harassment, sexualized messages, coercive viewings. SpareRoom data: most serious abuse cases involve male perpetrators. Design for this from day one. |
| **DPDP 2023 + Rules 2025** | Purpose-specific consent, data minimization, principal rights (access/correct/delete), grievance redress. Behavioral trust data needs explicit consent if/when launched. |
| **Gender filters in India** | Gender-specific PGs are common and generally tolerated; **Transgender Persons Act 2019** prohibits unfair exclusion of transgender persons. Use standardized options, not free-text discrimination. |
| **Badges > raw scores** | Tiered badges reduce defamation/discrimination risk vs numerical trust scores. Frame as platform opinion, not character judgment. |
| **Keep comms in-app early** | Airbnb, Badi, Roomies pattern: delay phone/WhatsApp exposure; in-app messaging + report tools. |
| **No in-app payments (yet)** | Aligns with PDF best practice and current Terms — but **must** warn users aggressively about off-platform UPI scams. |
| **Mental health / compatibility** | Lifestyle mismatch in shared housing is a documented stress/anxiety driver. Seeker bios + flat context help; trust signals should not shame users. |

**Ignore for now:** Global expansion legal matrices (UK Equality Act, UAE, Singapore), EigenTrust algorithms, PG yield statistics, OYO Life trajectory, Google Business verification adoption rates.

---

## 2. Current product snapshot (APK Discover)

| Area | Built today | Gap vs PDF recommendations |
|------|-------------|---------------------------|
| **Use a flat** (Vacancies tab) | Real-time `vacancy.active` listings; city/area filter; rent, beds, about; admin publishes from Manage Flat | No scam warnings, no report listing, no "verified" label, no chat from vacancy card (only seekers), exact address hidden ✅ |
| **Find the person** (Seekers tab) | Browse profiles, publish seeker profile, city/budget/bio, in-app chat | No report/block, no phone verification badge, no safety tips before first message |
| **Chat** | `messages` collection, sender/receiver only | No moderation, no report button, no block, no "don't share payment details" nudge |
| **Trust signals** | Reliability % inside flat (tasks) — **not shown on Discover** | Behavioral trust badges deferred; web store has hooks for `addBehavioralEvent` but not wired to Discover UI |
| **Verification** | Firebase Auth (email/Google) | No phone OTP, no ID KYC, no Google Business PG verification |
| **Legal pages** | Web: `/privacy`, `/terms` (DPDP, 18+, no payment processing) | APK Profile has **no** Privacy/Terms links; policies **don't mention** Discover, messaging, or seeker profiles |
| **Firestore rules** | `seekerProfiles`, `messages` rules written | Must be **deployed** to `garbage-f79f7` before launch |
| **Gender** | `preferredGender` in data model | Not exposed in Manage Flat UI; no policy text on inclusive use |
| **Age gate** | Terms say 18+ on web | No in-app age acknowledgment on Discover |

---

## 3. Dual-mode mapping: Use a flat vs Find the person

### Use a flat (Vacancies — "I have a room")

**User journey:** Admin lists vacancy → seeker browses → (today) seeker must find poster via Seekers/chat indirectly; **gap:** no direct "Contact about this vacancy" CTA.

| PDF recommendation | MVP action |
|--------------------|------------|
| Approximate location only | ✅ Already area + city; never show flat invite code or full address on board |
| Listing verification | **Defer** Google Business / ownership proof; **NOW:** email-verified poster + "Member of active flat on Habitiq" label |
| Fake listing prevention | **NOW:** safety copy on publish ("Only list if you can show the room"); report listing (can reuse report user) |
| Gender preference (women-only PG etc.) | **NOW:** optional `preferredGender` dropdown when publishing + policy footnote on lawful use |
| Payment scams | **NOW:** banner — "Habitiq never collects rent. Never pay before viewing." |
| Trust badge on card | **NOW (light):** "Active flat · N members" from flat doc; **Defer:** behavioral badge |

### Find the person (Seekers — "I need a place / roommate")

**User journey:** User publishes seeker profile → others browse → tap Chat → in-app messages.

| PDF recommendation | MVP action |
|--------------------|------------|
| Hide contact until mutual trust | ✅ No phone field on profile; keep chat in-app |
| Harassment risk (esp. women) | **NOW:** Report + Block on profile and chat; safety tips before first message |
| Profile authenticity | **NOW:** display name from Auth; **Defer:** ID verification tier |
| Compatibility / wellbeing | **NOW:** bio + budget + city fields; optional lifestyle tags later; surface "Meet in public first" tip |
| Behavioral trust score | **Defer** full badge system; optionally show flat reliability only if user opts in |

---

## 4. Implement NOW (Discover MVP — policies & safety)

Priority order for **real user value** with minimal engineering:

### 4.1 Legal & policy (docs + light product)

1. **Amend Privacy Policy** (`apps/web/app/privacy/page.tsx`) — add sections:
   - Discover data: seeker profiles (city, budget, bio), vacancy listings, direct messages
   - Location: city/area only on public board; precise address stays inside flat management
   - Messaging retention and moderation (we may review reported content)
   - Future behavioral trust: separate opt-in consent before any badge goes live

2. **Amend Terms of Service** (`apps/web/app/terms/page.tsx`) — add sections:
   - Discover is a **listing and introduction tool only** — not a broker, landlord, or guarantor
   - No rent/deposit processing; all payments are between users off-platform
   - Prohibited conduct: fake listings, harassment, discrimination beyond lawful PG norms, soliciting romance via housing ads
   - We may remove listings/profiles and suspend accounts for violations
   - Viewings and physical meetings are at users' own risk

3. **New: Safety tips page** (`/safety` or section in Terms) — short, scannable:
   - Verify identity before paying
   - Visit in person; bring a friend
   - Never send money via UPI/WhatsApp before viewing
   - Red flags: too-good rent, urgency, refusal to video call
   - How to report and block
   - India emergency: **112**; cybercrime: **1930**; women helpline: **181** (state variants)

4. **APK legal links** — Profile or Settings → open `https://habitiq.app/privacy`, `/terms`, `/safety` in browser

5. **Discover onboarding sheet** (one-time): "I'm 18+", link to Safety tips, agree to messaging rules

### 4.2 Safety tooling (product)

6. **Report user** — Firestore `reports` collection + UI on chat header and seeker card  
   - Fields: `reporterId`, `reportedId`, `reason` (enum), `context` (chat/listing/profile), `messageId?`, `createdAt`, `status`  
   - Email alert to `hello@habitiq.app` (manual review at starting stage)

7. **Block user** — `users/{uid}/blocked/{blockedUid}` or array on profile  
   - Hide blocked users from Seekers list; prevent new messages; show "You blocked this user"

8. **In-context warnings**
   - First chat: modal with 3 safety bullets + link to full tips
   - Vacancy publish: checkbox "I confirm this is a real available room"
   - Seeker publish: "Don't share phone or payment details in chat"

9. **Deploy Firestore rules** — `firebase deploy --only firestore:rules` for `seekerProfiles` + `messages`

### 4.3 Trust signals (lightweight)

10. **Card labels (no scoring yet)**
    - Vacancy: "Posted by Habitiq member" + flat name (not invite code)
    - Seeker: "Joined [month]" from `createdAt`
    - **Defer:** "Habitiq Trusted" behavioral badge until consent flow exists

### 4.4 Content moderation (manual MVP)

11. **Report queue** — founder reviews in Firebase Console or simple admin sheet  
12. **Auto-flag** (optional quick win): block messages containing UPI IDs, phone patterns, or obvious slurs client-side with "Are you sure?" or soft block  
13. **Enforcement policy** — document internally: 1 report = review; 2+ harassment reports = suspend; fake listing = remove vacancy + warn

### 4.5 DPDP compliance for Discover

14. **Notice at collection** — when publishing seeker profile or vacancy, inline text: "This will be visible to other Habitiq users. See Privacy Policy."  
15. **Delete path** — user can delete seeker profile (already `delete` rule on own doc); account deletion should cascade messages (add to deletion flow)  
16. **Grievance officer** — already in Privacy Policy; ensure Discover complaints route to same `hello@habitiq.app`

---

## 5. Defer (post-launch or Phase 2)

| Item | Why defer |
|------|-----------|
| Behavioral trust badges (chore/payment/dispute scoring) | Needs DPDP opt-in UX, appeal flow, legal review; web hooks exist but not production-ready |
| Google Business Profile "Verified PG" | Ops-heavy; good for PG supply later |
| Government ID / phone OTP verification | Cost + friction; add when report volume justifies |
| In-app rent/deposit payments | Major legal + compliance scope; PDF says avoid off-platform payments — warnings first |
| AI / risk-based screening | Airbnb-scale infra; manual reports enough at start |
| Photo reverse-image / listing ownership proof | Manual review first |
| Jurisdiction-specific gender filter configs | India-only launch — single policy suffices |
| Automated content moderation (ML) | Manual queue first |
| Trust score appeals portal | Needed only when badges ship |
| PG owner dashboards / occupancy tools | Discovery launch doesn't require |
| Emergency in-app SOS button | Link to 112/181 in safety page is enough for MVP |

---

## 6. Cross-check matrix (PDF → Habitiq)

| PDF section | Recommendation | Use a flat | Find the person | Status |
|-------------|----------------|------------|-----------------|--------|
| §5.1 Fake listings | Verify photos, warn on upfront payment | Primary risk | Lower | ⚠️ Warnings needed |
| §5.2 Harassment | Report, moderation, male-perpetrator awareness | Lower | **Primary risk** | ❌ Not built |
| §5.3 In-app comms | Keep chat in-platform | Partial (no vacancy chat) | ✅ Chat exists | ⚠️ Add vacancy contact |
| §5.4 Listing verification | Ownership proof, Google Business | Future | — | Defer |
| §7.1 Trust badges | Tiered badges, not raw scores | Show on vacancy | Show on seeker | Defer (light labels NOW) |
| §7.2 Safer flows | Approx location, hide contact | ✅ Area only | ✅ No phone | ✅ |
| §7.3 DPDP consent | Purpose-specific for behavioral data | N/A until badges | N/A until badges | ⚠️ Update privacy for Discover |
| §3.2 Gender filters | India: allowed with inclusion caveats | `preferredGender` | — | ⚠️ Policy + UI |
| §4.3 Defamation risk | Badges as opinion, appeal path | Defer badges | Defer badges | Document in Terms |
| §2.4 Conversion | Trust signals on cards | Labels NOW | Labels NOW | ❌ |
| Informal channel scams | Education content | ✅ | ✅ | ❌ Safety page |

---

## 7. Health & wellbeing value (from PDF)

Research cited in the PDF connects shared housing to **mental health outcomes** — worth treating as product value, not just legal cover:

1. **Lifestyle mismatch → stress and anxiety** — Seeker bio + flat "about" fields help set expectations; future lifestyle tags (sleep schedule, guests, smoking) reduce hostile move-ins.

2. **Financial disputes → chronic stress** — Habitiq's flat management (expenses, reliability) already addresses in-flat wellbeing; Discover should **not** promise compatibility but can surface "this poster manages chores on Habitiq."

3. **Harassment → psychological harm** — Women on dating/housing apps face moral policing and sexual harassment (PDF cites India dating-app study). Fast report/block + clear enforcement reduces harm and platform liability.

4. **Unsafe viewings → physical safety** — "Bring a friend", daytime viewings, public first meeting — belong in Safety tips and first-chat modal.

5. **Online-to-offline violence** — PDF cites 73% of women journalists facing online violence, 25% with physical threat links. Minimize early PII exposure; in-app chat audit trail helps if user reports to police.

6. **Reputation attacks** — Defer public numeric scores; when reviews/badges ship, include correction/appeal to avoid wellbeing harm from false labels.

**Product principle:** Discover should help users find **safer, more compatible** living situations — trust tooling and education are wellbeing features, not compliance checkboxes.

---

## 8. Launch checklist — Already have vs Need for Discover APK

| Item | Web | Discover APK | Action |
|------|-----|--------------|--------|
| Privacy Policy (DPDP) | ✅ `/privacy` | ❌ No link | Add links in APK Settings; **extend** policy for Discover |
| Terms of Service | ✅ `/terms` | ❌ No link | Add links; **extend** for listings/messaging |
| Safety tips (housing scams, viewings) | ❌ | ❌ | **Create** `/safety` page + in-app link |
| Age 18+ gate | ✅ In Terms | ❌ | One-time Discover acknowledgment |
| Report user | ❌ | ❌ | **Build** (both platforms benefit) |
| Block user | ❌ | ❌ | **Build** |
| In-app chat (no forced phone leak) | N/A | ✅ | Keep; add warnings |
| Vacancy browse (Use a flat) | Partial (data model) | ✅ Vacancies tab | Rename UI to match Sai's language when polishing |
| Seeker browse (Find the person) | ❌ | ✅ Seekers tab | Same |
| Firestore rules for chat/seekers | Written | ⚠️ Deploy | `firebase deploy --only firestore:rules` |
| Payment disclaimer | ✅ Terms | ❌ Not surfaced in Discover | Banner in Discover + publish flows |
| Gender preference on listings | Data only | ❌ UI | Add dropdown + policy |
| Trust / verification badges | ❌ | ❌ | Light "member" label only for MVP |
| Content moderation process | ❌ | ❌ | Manual report queue + doc |
| Grievance contact | ✅ Privacy | ❌ Not in APK | Link in Settings |
| Behavioral data consent (trust badges) | ❌ | ❌ | Defer until feature ships |
| Biometric app lock | N/A | ✅ | Good for flat data; unrelated to Discover |
| Account deletion | ✅ | ✅ | Ensure seeker profile + messages purged |
| Google Play Data Safety form | N/A | ❌ | Declare messaging, location (coarse), personal info |

---

## 9. Suggested Firestore additions (reference)

```
reports/{reportId}
  reporterId, reportedId, reason, context, details?, messageId?, createdAt, status

users/{uid}/blocked/{blockedUid}
  blockedAt
```

Rules: users write own reports and blocks; only admin reads all reports (console or future admin tool).

---

## 10. Copy-paste disclaimers (for Terms / in-app)

**Discover disclaimer (short):**  
"Habitiq Discover helps you find rooms and flatmates. We do not verify every listing, guarantee availability, or handle rent payments. Always visit in person before paying. Report suspicious users in the app."

**Trust badge (future):**  
"Habitiq labels reflect activity on our platform only. They are not background checks or guarantees of safety. You can request a review of your label at hello@habitiq.app."

**Gender preference:**  
"Listings may indicate a preferred gender for shared accommodation where permitted by local practice. Habitiq does not permit discrimination against transgender persons or other protected groups."

---

## 11. Top 10 must-do for Discover launch (Sai summary)

1. **Extend Privacy Policy + Terms** for Discover (profiles, listings, messages, no brokerage).
2. **Publish Safety tips page** and link from APK + first-time Discover flow.
3. **Report user** flow with manual review queue.
4. **Block user** flow (hide + stop messages).
5. **Deploy Firestore rules** for `seekerProfiles` and `messages` on `garbage-f79f7`.
6. **In-app payment scam warnings** on Discover, chat, and vacancy publish.
7. **Age 18+ acknowledgment** before using Discover.
8. **APK links to Privacy, Terms, Safety** in Settings/Profile.
9. **First-chat safety modal** (meet in public, don't pay early, report harassment).
10. **Light trust labels** on cards ("Habitiq member", join date) — defer behavioral scoring.

---

*Next doc update: when trust badges, ID verification, or `/safety` ships — bump this file and `Habitiq — Project Documentation.md`.*
