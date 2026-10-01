# Flats/Members Feature — Design

**Date:** 2026-07-11
**Status:** Approved by Sai, ready for implementation planning

## Context

This is the second implementation slice of the native app, following the Foundation & Authentication plan (project scaffold, real Firebase Auth — Google + Email/Password — all done and reviewed). It builds the first real feature: creating a flat, inviting roommates to it, and joining one by code. This is also the first UI beyond the bare Login/Signup/Home screens, so it sets the visual language the rest of the app follows.

## Standing design principle (applies beyond this feature)

**Every screen must be understandable at a glance, through visual hierarchy — not through explanatory text.** A person should be able to tell what a screen does and what to do next from its icons, layout, and one-line labels alone, the way the Figma reference's Home screen does it: each action is an icon + a bold 2-3 word title + one short supporting line ("Create a Flat — Start your own flat community and invite your friends"). No walls of instructional copy, no screens that require reading a paragraph to understand. This is a standing bar for every future screen, not a one-off requirement for Flats/Members.

## Scope

**In scope (v1):**
- Create a flat (name input) — caller becomes admin
- Display the generated invite code, with a **native share button** (Android system share sheet — `Intent.ACTION_SEND`)
- Join an existing flat by entering a code — **instant-join only**, no approval step
- View the flat's member list (nickname, role)

**Explicitly deferred to a follow-up plan** (same pattern as splitting Google Sign-In from email/password in the prior plan):
- Approval-mode joining (admin reviews/approves join requests)
- Leave flat, kick member, transfer admin role
- Multi-flat switching (a user in more than one flat)

## Source of truth

`C:\garbage\lib\flatService.ts` is the web app's complete, already-validated implementation — this is a faithful port, not a redesign. Key logic being ported exactly:

- `generateFlatId()`: a `FLAT-XXXX` code (4 chars from a fixed alphabet `ABCDEFGHJKLMNPQRSTUVWXYZ23456789`, cryptographically random), checked against Firestore for collisions (retry up to 5 times) before use as the flat's document ID. The flat ID **is** the invite code — no separate code field.
- `createFlat`: writes `flats/{flatId}` (name, adminUid, memberCount=1, subscriptionStatus, trialEndDate) and `flats/{flatId}/members/{uid}` (nickname, email, role=admin, status=available, reliabilityScore=100), then updates `users/{uid}` with `activeFlatId`/`flatIds`.
- `joinFlat`: a Firestore transaction — checks the flat exists, checks `memberCount < 8`, checks the user isn't already a member, then atomically creates the member doc, increments `memberCount`, and updates the user's profile. Errors map to exactly three cases: flat not found, flat full (8 members), already a member.
- Both are already covered by the shared production Firestore Security Rules (`C:\garbage\firestore.rules`) — no rule changes needed, this app operates under the same rules as the web app.

## Architecture (same shape as the Auth work)

- `FlatsRepository`: `createFlat(name, uid, nickname, email): Result<String>` (returns the new flatId), `joinFlat(flatId, uid, nickname, email): Result<Unit>`, `getFlat(flatId): Result<FlatInfo>`
- `MembersRepository`: `observeMembers(flatId): Flow<List<Member>>` (real-time listener, matches the web app's live member list)
- Business-logic error mapping (flat not found / full / already a member) lives in the repository layer, same pattern as `AuthErrorMapper` — a small pure function, unit tested.
- ShareLauncher: a thin wrapper around `Intent.ACTION_SEND` / `Intent.createChooser`, same "one function, does one Android-platform thing" shape as `GoogleSignInLauncher`.

## Screens

- **Create Flat**: single text field (flat name) + Create button. On success, navigates to a confirmation screen showing the invite code large and prominent, with a **Share** button (system share sheet, pre-filled message e.g. "Join my flat on Habitiq! Use code FLAT-A3B9").
- **Join with Code**: single text field (code) + Join button. Errors shown inline, matching the three known failure cases above in plain language (not raw Firestore errors).
- **Member list**: simple list, nickname + role badge (Admin/Member), no actions yet (kick/leave deferred).
- All three follow the visual principle above: icon-led, minimal copy, one clear primary action per screen.

## Testing

Unit tests for the pure logic: invite-code generation format/alphabet, and the three-way error classification in `joinFlat`'s Firestore transaction result (not-found / full / already-member). The transaction itself and the share intent are thin platform wrappers, verified via the same on-device manual-testing pattern used for Auth (Task 8), not automated.

## Out of scope reminders

Push notifications, Discover, Tasks, Expenses, Settlements are separate future plans — not touched here.
