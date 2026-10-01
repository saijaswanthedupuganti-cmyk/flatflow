# Habitiq Discovery — working implementation spec (APK)

**Date:** 23 August 2026  
**Cursor agent spec:** `project_1/HABITIQ_DISCOVERY_CURSOR_IMPLEMENTATION.md` (drop-in instructions).

This is the live working spec for the coding agent. Policies (privacy, approximate location, qualitative trust, in-app contact until mutual accept, additive/flagged engineering) match the research and v1.0 Discovery docs. **Open two-sided discovery, looking posts, and mutual-match messaging are now in-scope for the APK** because that is the current product direction — they were later-phase in the original v1.0 document.

## Current stack (inspected, not invented)

| Piece | Where it lives |
|---|---|
| Vacancy (supply) | `flats/{flatId}.vacancy` map. Fields: `active`, `city`, `area`, `rentPerHead`, `currency`, `bedsAvailable`, `preferredGender`, `about`, `lifestyle[]`, `customTags[]`, `existingMembersGender`, `updatedAt`. Additive: `status` (Published/Paused/Closed…). **No street address is stored or parsed.** |
| Looking post (demand) | `seekerProfiles/{uid}`: `displayName`, `photoUrl`, `city`, `lookingIn`, `budget`, `bio`, `gender`, `lifestyleTags`, `active`, `createdAt`. **No company, college, village, phone.** |
| Chat | `messages/{id}` — in-app only. No phone/WhatsApp/email on Discovery surfaces. |
| Connections | **New additive** `discoveryConnections/{id}`: `fromUid`, `toUid`, `listingFlatId?`, `seekerId?`, `message`, `status` (`REQUEST_SENT` → `ACCEPTED` / `DECLINED` / `BLOCKED`). |
| Reports | **New additive** `discoveryReports/{id}`. A report does **not** change a trust tag. |
| Blocks | `users/{uid}/blocked/{targetId}` |
| Membership / join | Unchanged. Match ≠ join. Invite code / join requests stay the source of truth. |
| Tasks / expenses / settlements | Untouched. |

## Product rules encoded in code

1. Find Flat and Find Flatmate are different intents (`DiscoverMode`), not one search with two labels.
2. Vacancy vs looking are different post types (`DiscoveryPostType`).
3. Compatibility (`CompatibilitySignal`) is separate from trust (`TrustTier`). No `matchScore`, no percentages.
4. Trust tags: `Unrated` (consent off), `New to Habitiq` (neutral), `Habitiq member` (listing from an existing flat). **Habitiq Plus / Reliable thresholds are not invented.**
5. Ranking is an explainable weighted sort (location → budget → room → preference overlap). Trust is a tiny tie-break, never an exclusion.
6. Feature flags: `DiscoverFlags` (`ENABLED`, `FIND_FLATMATE`, `LOOKING_POSTS`, `CONNECTION_REQUESTS`, `QUALITATIVE_TRUST`, `DISTANCE_INTELLIGENCE=false`).
7. Exact address is never copied from Firestore vacancy maps even if a stray field exists. Create-flat `pincode` / `landmark` / map pin are not Discovery commute coordinates.

## Surfaces shipped in this basement

Browse (search + grouped filter sheet + chips) → listing/person **detail** → **connect request** (no phone) → **inbox accept/decline** → **in-app conversation** (safety banner, report, block) → **My posts** (pause/resume/close) → **create post** wizard (vacancy admin-only; looking behind `LOOKING_POSTS`).

Plus on the Discover tab opens Create Discovery post, not Add Task.

## Not in this basement (do not fake)

- Company / college / hometown matching (fields do not exist)
- Numerical trust from chores/expenses (no documented formula; consent UI is in place)
- Auto-join on match
- Exact map pin / address reveal
- Viewing scheduler / MATCHED as a required backend event
- AI ranking
- **Distance / commute intelligence (Phase 2)** — reserved fields `discoveryApproxLocation` and `seekerProfiles.commuteAnchors`; no Maps SDK, no fake km in Discovery UI

## Deploy before relying on connections/reports in production

```text
firebase deploy --only firestore:rules
```

Rules additions are additive. Existing Tasks/Expenses/Settlements rules are unchanged.
