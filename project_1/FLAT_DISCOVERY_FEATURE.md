# Flat Discovery Feature — Complete Feature Brief
**Habitiq v0.5.x | Planned Phase 2**
**Document purpose:** Full reference for pitch, planning, and implementation. Covers current state, the problem, the vision, user flows, Firestore schema, tech architecture, UI patterns, and business impact.

---

## 1. Where We Are Today — The Current Join Flow

### How a User Joins a Flat (Today)

The onboarding screen has two panels:

| Panel | Action | How It Works |
|-------|--------|-------------|
| **CREATE** | User starts a flat | Enters flat name + nickname → flat created → receives a 6-character invite code |
| **JOIN** | User joins a flat | Enters invite code shared by admin → instant join OR enters approval queue if admin has approval-mode enabled |

**The invite code path:**
1. Admin creates flat → shares code via WhatsApp/SMS
2. New member receives code out-of-band → enters it in-app
3. If approval mode is ON → admin reviews the join request and approves/rejects from their dashboard

**What's live in the codebase:**
- `app/onboarding/page.tsx` — the two-panel CREATE / JOIN screen
- `lib/flatService.ts` — `joinFlat()`, `createFlat()`, `approveJoinRequest()`
- Firestore: `/flats/{flatId}/joinRequests/{requestId}` — approval queue
- 8-member cap enforced at Firestore rules level

### The Critical Gap

This system only works **if both sides already know each other.**

The invite code is a **closed-network tool** — it requires a pre-existing connection. A person who is:
- New to a city
- Looking for a flat to move into
- Or an admin who has a vacancy and needs a new flatmate

…has **zero path** inside Habitiq to solve that problem. They go back to WhatsApp groups, NoBroker, housing forums, or word-of-mouth. Habitiq loses the moment of new-member acquisition entirely.

---

## 2. The Discovery Problem — Why This Matters

### Two Real Scenarios That Fail Today

**Scenario A — The New Joiner:**
> Ravi moves to Bangalore for a new job. He finds a flat through NoBroker. The flat is already on Habitiq. The admin has to exit Habitiq, go to WhatsApp, share the invite code, wait for Ravi to receive it, and then Ravi has to open Habitiq and enter it. Three context switches. One friction point per new member, forever.

**Scenario B — The Flat With a Vacancy:**
> Priya's flatmate moved out. The flat is on Habitiq. She has a vacancy. She posts on a WhatsApp housing group, gets 20 randoms asking questions. None of them are on Habitiq. If she could list the vacancy inside Habitiq, only people who are already using the platform and aligned with the shared-living model would apply.

### The Business Cost

Every new member acquired through invite code = **zero platform touchpoint at the acquisition moment.** Habitiq has no visibility into whether Ravi was even a real user. He could ghost. He could not install. The platform has no hook.

Discovery fixes this: **the platform becomes the acquisition channel, not just the management layer.**

---

## 3. The Vision — Two-Sided Discovery Marketplace

A **two-sided matching system** embedded inside Habitiq:

| Side | Role | What They Want |
|------|------|---------------|
| **Flat (Admin)** | Lists a vacancy | Find a compatible flatmate who fits their flat's existing culture, location, and living style |
| **Member (Seeker)** | Browses available flats | Find a flat with a vacancy in the right location, at the right price, with compatible people |

This is not a full apartment listing platform (that's NoBroker's job). It is a **flatmate compatibility layer** — specifically for people who want a shared living environment where duties and expenses are managed fairly via Habitiq.

### The Core Promise

> "Already on Habitiq? When you need a new flatmate or a new flat, you don't leave the app."

---

## 4. User Flows — Line by Line

### Flow A: Admin Lists a Vacancy

1. Admin opens their flat settings (Settings page or Members page)
2. Taps **"List Vacancy"** — a new action alongside existing member management
3. Fills in a short **Vacancy Card**:
   - Flat nickname (auto-filled from flat name)
   - City + Area/Locality (text input with autocomplete, e.g. "Koramangala, Bangalore")
   - Vacancy count (1–3 open slots, since cap is 8 members)
   - Monthly rent range (₹ range slider or two inputs)
   - Gender preference (Any / Male / Female / Non-binary) — optional
   - About the flat (3 lines max) — e.g. "Working professionals, clean flat, vegetarian kitchen preferred"
   - Toggle: **"Show current member count"** (shows "4 of 8 slots filled")
   - Toggle: **"Require application before showing contact"** (vs. instant contact reveal)
4. Flat listing goes live → visible in the Discovery directory
5. Admin can **pause** or **close** the listing at any time (vacancy filled, no longer needed)
6. Admin receives **in-app notifications** when seekers express interest / apply

**Admin controls:**
- View applicant list (similar to the existing joinRequests approval view)
- Accept applicant → auto-generates an invite code pre-linked to that seeker
- Decline applicant → optional reason (no notification to seeker beyond "not selected")
- Close listing → removes from directory, archives applicants

---

### Flow B: Member Browses and Applies

1. Member (new or existing Habitiq user) opens the app
2. On the Onboarding screen, sees a **third panel: "Find a Flat"** (alongside Create + Join)
   - Also accessible from inside the dashboard via a "Find Another Flat" prompt in the FlatSwitcher or profile
3. Taps **"Find a Flat"** → opens **Discovery screen**
4. Sees a directory of active listings (card-based layout):
   - Flat name + area
   - Rent range
   - Current occupancy (e.g. "4/8 members")
   - Gender preference badge (if set)
   - Short about blurb
   - "Interested" button
5. Seeker filters listings by:
   - City / Area
   - Rent range
   - Gender preference
   - Available now vs. future date
6. Seeker taps a listing → opens **Flat Profile** (detail view):
   - Full about section
   - Task categories in use (badges showing "Cleaning · Cooking · Groceries")
   - Expense module status ("Uses expense splitting")
   - Member reliability snapshot (optional: admin may show avg reliability score)
   - "Express Interest" button → seeker submits a short note (50 chars max) + their availability
7. If admin has **"Require application"** ON: seeker's interest goes to admin's applicant queue
   - Admin reviews → accepts → seeker receives a one-time invite link (no code needed, auto-join after clicking)
8. If admin has **"Require application"** OFF: seeker sees the flat's contact info (admin phone/WhatsApp) immediately

---

### Flow C: Seeker Profile (What Admins See About You)

When an admin reviews an applicant, they see a **Seeker Card**:
- Display name + profile photo (from Google auth)
- City / Looking in (set by seeker when activating discovery)
- Habitiq tenure ("Member since May 2026")
- Previous flat reliability score (if they have history — optional disclosure toggle)
- Short bio (100 chars max): "Working professional, neat, vegetarian"
- Active in any current flat? (yes/no, without revealing details)

Seeker controls:
- **Opt-in only** — seeker profile is invisible until they activate "Find a Flat" mode
- Toggle off at any time → removed from all admin applicant queues (pending applications voided)
- Reliability score sharing is a separate toggle (many users will opt out)

---

## 5. Firestore Data Model

### New Top-Level Collection: `/flatListings/{listingId}`

```
/flatListings/{listingId}
  flatId: string                     — reference to /flats/{flatId}
  adminUid: string                   — who listed it
  city: string                       — "Bangalore"
  area: string                       — "Koramangala"
  rentMin: number                    — ₹ lower bound
  rentMax: number                    — ₹ upper bound
  vacancyCount: number               — 1–3
  genderPreference: string           — 'any' | 'male' | 'female' | 'other'
  about: string                      — max 200 chars
  showMemberCount: boolean
  requireApplication: boolean
  status: 'active' | 'paused' | 'closed'
  memberCount: number                — snapshot at listing time
  createdAt: Timestamp
  updatedAt: Timestamp

  /applicants/{applicantUid}
    uid: string
    displayName: string
    note: string                     — max 50 chars
    availability: string             — "Immediately" | "Next month" | date string
    reliabilityScore?: number        — only if seeker disclosed it
    status: 'pending' | 'accepted' | 'declined'
    appliedAt: Timestamp
    reviewedAt?: Timestamp
```

### New Field on `/users/{userId}`

```
/users/{userId}
  ... (existing fields)
  discovery?: {
    active: boolean                  — is this user in seeker mode?
    city: string
    lookingIn: string
    bio: string                      — max 100 chars
    shareReliability: boolean
    activatedAt: Timestamp
  }
```

### Firestore Rules for Discovery

```
// flatListings — public readable when active; only admin can write
match /flatListings/{listingId} {
  allow read: if request.auth != null && resource.data.status == 'active';
  allow create: if request.auth != null
    && request.auth.uid == request.resource.data.adminUid
    && isAdmin(request.resource.data.flatId);
  allow update, delete: if request.auth != null
    && request.auth.uid == resource.data.adminUid;

  match /applicants/{applicantUid} {
    allow read: if request.auth != null
      && (request.auth.uid == applicantUid
          || request.auth.uid == get(/databases/$(database)/documents/flatListings/$(listingId)).data.adminUid);
    allow create: if request.auth != null && request.auth.uid == applicantUid;
    allow update: if request.auth != null
      && request.auth.uid == get(/databases/$(database)/documents/flatListings/$(listingId)).data.adminUid;
  }
}

// users — seeker profile readable only if discovery.active == true
// (existing user read rules extended, not replaced)
match /users/{userId} {
  allow read: if request.auth != null
    && (request.auth.uid == userId
        || resource.data.discovery.active == true);
  allow write: if request.auth != null && request.auth.uid == userId;
}
```

---

## 6. Technical Architecture — How It Fits the Existing Stack

### What Already Exists (Reuse These)

| Existing System | How Discovery Reuses It |
|-----------------|------------------------|
| `joinRequests` Firestore flow | Applicants subcollection mirrors this pattern exactly |
| Approval-mode UI (admin reviews, approves, rejects) | Applicant review panel uses the same component pattern |
| `joinFlat()` in flatService.ts | Accepting an applicant calls this under the hood with a pre-generated code |
| `FlatSwitcher` dropdown | "Find Another Flat" entry added here for users already in a flat |
| Onboarding screen two-panel layout | Add a third panel — CSS grid already responsive |
| Firebase Auth user object | Seeker profile is just an extension of the existing `/users/{userId}` doc |

### New Files Needed

```
app/
  discovery/
    page.tsx                         — Directory listing (browse mode)
    [listingId]/page.tsx             — Flat profile detail view
    seeker-profile/page.tsx          — Seeker sets up their discovery profile

components/
  FlatListingCard.tsx                — Card component for directory
  SeekerCard.tsx                     — Applicant card shown to admin
  VacancyModal.tsx                   — Admin creates/edits a vacancy listing
  DiscoveryFilters.tsx               — City/area/rent filter bar

lib/
  discoveryService.ts                — createListing, updateListing, closeListing,
                                       applyToFlat, acceptApplicant, declineApplicant
                                       getSeekerProfile, updateSeekerProfile

store/
  useDiscoveryStore.ts               — listings[], myApplications[], applicantsForMyFlat[]
                                       Firestore onSnapshot listeners for each
```

### Where It Lives in Navigation

**Onboarding screen** (`app/onboarding/page.tsx`):
- Third panel added: "Find a Flat" with a search/compass icon
- Leads to `app/discovery/page.tsx`

**Inside dashboard** (for existing flat members looking for another flat):
- FlatSwitcher footer: "+ Find Another Flat" entry
- Profile page: "Looking for a flat?" toggle that activates seeker mode

**Admin — flat settings or members page:**
- "List a Vacancy" button opens `VacancyModal`
- Active listing card with applicant count badge
- "View Applicants" leads to applicant review panel

---

## 7. UI/UX — Key Screens

### Screen 1: Discovery Directory

- **Layout:** Scrollable card feed (mobile-first, 1 column; tablet 2 columns)
- **Filter bar:** sticky at top — City dropdown · Area text input · Rent range (₹ slider) · Gender pref pills
- **Listing Card:**
  - Top row: flat name (bold) + area + city
  - Middle: about blurb (2 lines, truncated)
  - Bottom row: rent range chip · occupancy chip (e.g. "4/8") · gender pref badge
  - CTA: "View Flat" → opens flat profile
- **Empty state:** "No listings in [city] yet. Be the first to list your flat's vacancy."

### Screen 2: Flat Profile (Detail)

- **Header:** Flat name + area + city + admin's display name ("Listed by Priya S.")
- **Vacancy info:** count, rent range, gender pref
- **About section:** full text
- **Features section:** pills showing which Habitiq modules the flat uses ("Tasks · Expenses · Bills")
- **Members section (optional):** count only, or reliability avg if admin disclosed
- **CTA strip (sticky bottom):**
  - If `requireApplication`: "Express Interest" → opens a small sheet (50-char note + availability picker)
  - If instant contact: shows admin's contact (WhatsApp number or in-app message)

### Screen 3: Admin Applicant Review

- Mirrors the existing joinRequests approval panel
- Applicant cards with: photo · name · bio · reliability score (if disclosed) · note they wrote · availability
- Actions: **Accept** (generates invite link) · **Decline** (soft — no notification)
- Accepted applicant receives a toast + deep link to join the flat directly

### Screen 4: Seeker Profile Setup

- Activated from "Find a Flat" flow before browsing
- Fields: City · Looking in (area) · Short bio (100 chars) · Availability · Share reliability score toggle
- Minimal — can be skipped (browse without a profile, but can't apply without one)

---

## 8. Privacy & Safety Design

| Risk | Mitigation |
|------|-----------|
| Flat admin's personal info exposed | No phone numbers or emails shown in listing card; contact only shown post-match or if admin opts in |
| Seeker stalked via platform | Seeker profile only readable when `discovery.active == true`; off by default; deactivate instantly |
| Fake listings | Listing requires an active flat in the system — can't list without a real flat |
| Spam applications | Max 5 open applications per seeker at a time (enforced at rules or store level) |
| Ghost applicants | Accepted invite links expire after 48 hours |

---

## 9. Business Impact

### Why This Is a Growth Engine

**Virality amplified:**
Current growth loop: one person joins → invites flatmates → flatmates join other flats → spread.
With Discovery: new person searches for a flat → finds one on Habitiq → joins the platform to apply → now a Habitiq user from day one.

**Acquisition at zero cost:**
Every seeker who finds a flat through Discovery is an organically acquired user. No ads, no referral codes. The platform's supply of flats (admins) attracts demand (seekers), who then become users.

**Data flywheel:**
As more flats list vacancies, Discovery becomes more useful, attracting more seekers, which makes flats want to stay on Habitiq (their vacancy gets filled faster). Classic marketplace loop.

**Retention hook for admins:**
An admin who used Discovery to find their current flatmate has a direct incentive to stay on Habitiq — the platform literally found them a person they live with. Churn risk drops sharply.

### Monetisation Angle

| Opportunity | How |
|-------------|-----|
| Featured listing | Paid placement — flat appears at top of city results; ₹49–99 per week |
| Seeker boosts | Seeker profile bumped to top of admin's applicant list; ₹29 one-time |
| Verified badge | ID-verified seeker badge for trust signal; ₹49 one-time |
| B2B: PG operators | PG owners list multiple rooms; Discovery becomes their vacancy board; ₹299/property/month |

None of these require subscriptions. They are **transactional** — paid when there's an immediate job to be done (fill a vacancy, get noticed). Lower resistance to payment.

---

## 10. Phased Implementation Plan

### Phase A — MVP (2–3 weeks)

**Goal:** Admin can list a vacancy. Seeker can browse and apply. Admin accepts. Invite link sent.

Scope:
- `VacancyModal` — admin creates listing (city, area, rent, about, gender pref)
- `/discovery` directory page — browse cards, basic filter by city
- Flat profile page — full detail + "Express Interest" form
- Firestore: `flatListings` collection + `applicants` subcollection
- `discoveryService.ts` — create, apply, accept
- Admin applicant review panel (reuse joinRequests pattern)
- Invite link generation on accept
- Onboarding third panel: "Find a Flat"

**Not in MVP:**
- Seeker profile setup (apply with just a note, no profile)
- Reliability score sharing
- Featured listings / paid tier
- Real-time applicant count badge on listing card

---

### Phase B — Polish (1 week)

- Seeker profile setup screen (bio, availability, reliability toggle)
- Admin sees full seeker card (not just the note)
- Invite link expiry (48hr)
- Max 5 open applications per seeker
- "Find Another Flat" entry in FlatSwitcher inside dashboard
- Push notification to admin when new applicant arrives (when Phase 2 push notifications are built)

---

### Phase C — Discovery Network Effects (Future)

- City-level stats shown on directory ("12 flats in Koramangala")
- Saved/bookmarked listings
- Seeker × flat compatibility score (based on task categories, schedule, budget)
- PG operator multi-room listing
- Paid featured placement and seeker boosts
- In-app messaging between seeker and admin (before accept)

---

## 11. Open Questions for the Planning Session

1. **Scope of Phase A:** Should the MVP require admin to go through the full Vacancy Card form, or start with just city + about + one-tap listing?

2. **Contact model:** Should the default be "apply through Habitiq" (our data) or "show WhatsApp number after match" (simpler, less friction, but we lose the in-app engagement)? Recommend: in-app apply as default, with an opt-in for direct contact reveal.

3. **Discovery entry point:** Third panel on onboarding only, or also prominent inside the dashboard for existing users looking for a second flat? The FlatSwitcher is the natural home for the second case.

4. **Seeker identity:** Should seekers need to fill out a bio before they can apply, or can they apply cold (just a note)? Recommend: cold apply allowed, but admin sees "No profile set" which nudges seekers to fill one out.

5. **8-member cap interaction:** If a flat is at 7/8 and lists a vacancy for 1, and 3 people apply, the admin can only accept 1. The other 2 get auto-declined when cap is hit. Should this be enforced automatically or left to admin?

6. **Trust and safety:** Do we want any ID verification layer from day one (e.g. email-verified only, no anonymous accounts)? Firebase email verification is already supported; we'd just need to gate the apply action behind `emailVerified == true`.

---

## 12. Summary — The One-Line Pitch

> Discovery turns Habitiq from a management tool into a platform — the place where shared living actually starts, not just where it's managed after the fact.

Current state: invite code — closed network, requires pre-existing connection.
With Discovery: open marketplace — Habitiq is where you find your flat and where you manage it.

**This is the feature that makes the growth loop self-sustaining.**

---

*Document: FLAT_DISCOVERY_FEATURE.md*
*Version: 1.0 | June 2026*
*Author: Sai + Claude Code*
*Saved at: C:\garbage\project_1\FLAT_DISCOVERY_FEATURE.md*
