# Habitiq Native App — Backend & Storage Architecture Design

**Date:** 2026-07-11
**Status:** Approved by Sai, ready for implementation planning

## Context

Habitiq has an existing Next.js web app (`C:\garbage`, production at habitiq.app) and an Expo/React Native rebuild (`C:\habitiq-mobile`). This design covers a third, separate native Android rebuild at `C:\habitiq_jaswanth`, built fully from scratch — no code is reused from the web app or from the earlier Google AI Studio scaffold at `C:\Users\user\Downloads\habitiq` (that scaffold is kept only as a reference for its build fixes/keystore, not built upon).

Sai (product owner, UX/UI designer, non-engineer) knows the app's intended behavior and use cases deeply but not backend/storage implementation — this design exists so he has a clear, plain-language reference for how the native app's data and logic are structured, and so future sessions don't have to re-derive these decisions.

**Goal:** full feature and business-logic parity with the web app (nothing dropped — task rotation, expense splitting, settlements, permissions), targeting Google Play Store submission. New native-only features (see Discover, below) are explicitly deferred past v1.

## Confirmed foundational decisions (locked in before this design)

- **Firebase project:** connects directly to **production** `habitiq-by-jaswanth` — the same live data the web app uses. Not a separate/dev project. (This was previously reversed once already; reconfirmed 2026-07-11.)
- **Offline mode:** none. Online-only, matching the web app. No local cache as source of truth, no queued writes while offline.
- **Old AI Studio scaffold** (`Downloads\habitiq`): reference only. It has no real backend today — hardcoded fake user, local-only Room database, zero Firestore calls despite having Firebase configured at the Gradle level. Not a starting point for this rebuild.

## 1. Overall architecture

- **Kotlin + Jetpack Compose** for UI.
- **MVVM**: each screen has a ViewModel holding UI state, talking only to Repositories — never directly to Firestore.
- **No custom backend server.** Same pattern as the web app: the app talks straight to Firebase (Auth + Firestore); Firestore Security Rules (already written and live in production, in `C:\garbage\firestore.rules`) are the sole authorization gatekeeper. No Cloud Functions, no API routes exist in the web app today, and none are needed here either.
- **Repository per feature**, each owning both its Firestore calls and its ported business logic:
  - `FlatsRepository`, `MembersRepository`
  - `TasksRepository`
  - `ExpensesRepository`
  - `SettlementsRepository`
  - `RecurringBillsRepository` (covers recurring bills + monthly bill instances + month cycles)
  - `DiscoverRepository`
  - `RewardsRepository`

Rejected alternatives:
- *Thin ViewModel with direct Firestore calls* — faster short-term, but risks duplicated/tangled logic across screens (this is what went wrong in the old AI Studio scaffold). Rejected given the "don't miss a single condition" requirement.
- *Repository + UseCase/Interactor layer* — more rigorous, common in large teams, but unnecessary indirection for a single-source-of-truth, AI-assisted solo build.

## 2. Data layer & schema parity

The native app reads/writes the **same production Firestore data** as the web app — same flats, same members, same tasks, in real time. This is a hard constraint, not a suggestion:

- Collection structure is fixed to match the web app exactly: `flats/{flatId}/tasks`, `/expenses`, `/settlements`, `/recurringBills`, `/billInstances`, `/monthCycles`, `/members`, `/swapRequests`, `/activityLog`, `/joinRequests`, `/npsResponses`, `/behavioralEvents`; plus top-level `users` (+`rewards` subcollection), `seekerProfiles`, `discoveryTags`, `rewardPool`, `coupons`.
- Field names and types must match the web app's documents exactly — no native-only schema changes. (UI-facing feature *names*, like Flatboard → Discover, are just labeling and don't affect stored data.)
- Firestore Security Rules are shared, not duplicated or re-implemented — the native app lives under the same rules the web app already obeys.
- Real-time sync: each repository exposes a Kotlin `Flow` backed by a Firestore snapshot listener (the native equivalent of the web app's `onSnapshot` usage), so a change made on web appears instantly on native and vice versa.
- The Firestore Android SDK's default local disk cache stays enabled (it's a transparent performance/resilience layer) — this does not conflict with "no offline mode," which is about app UX behavior (no editing while disconnected), not about disabling the SDK's built-in cache.

## 3. Business logic porting plan

Each web `lib/*.ts` file is the source of truth for one native repository's logic — a close 1:1 translation, not a reinterpretation:

| Web app source (source of truth)                                              | Native repository                     |
| ------------------------------------------------------------------------------ | -------------------------------------- |
| `flatService.ts`                                                                | `FlatsRepository`, `MembersRepository` |
| `rotationEngine.ts`                                                             | `TasksRepository`                      |
| `expenseUtils.ts`                                                               | `ExpensesRepository`                   |
| `settlementUtils.ts`                                                            | `SettlementsRepository`                |
| `couponService.ts`, `rewardPool.ts`, `rewardSignal.ts`                          | `RewardsRepository`                    |
| `discoveryTagService.ts`, `seekerService.ts`, `trustComputation.ts`, `behavioralEvents.ts` | `DiscoverRepository`            |
| `npsService.ts`                                                                 | folded into `FlatsRepository` (small, low-traffic) |

**Porting order** (each layer depends on the one before it): Flats/Members → Tasks → Expenses → Settlements/RecurringBills → Discover → Rewards.

Each ported file is cross-checked against `C:\garbage\project_1\CONDITIONS.md` / `PERMISSIONS.md` and the Expo app's `CONDITIONS_PARITY.md` / `PERMISSIONS_PARITY.md` (which already went through this exact parity exercise once) so edge cases — admin-vs-member rules, task-due timing checks, recurring/repeated logic — aren't silently dropped.

## 4. Feature scope for v1 + navigation

**v1 feature set** (full parity with the web app):
- Auth: Google + Email/Password login, flat creation/joining (invite code + approval-mode join requests)
- Flat Management (new umbrella, native-only grouping): Tasks (rotation, swap requests, completion, admin create/delete) + Expenses (add/edit/delete, splitting, activity log)
- Settlements: mark paid/received, recurring bills, monthly bill instances, month-cycle open/close (admin)
- **Discover** (renamed from the web app's "Flatboard"): flat/roommate finding, matching web functionality exactly in v1 — the enhanced native-only Discover features are explicitly deferred to a fast-follow release
- Profile: user profile, Rewards Wallet (redeem coupons)
- Admin-only actions gated correctly everywhere: kick member, transfer role, delete flat, edit recurring bills, etc.

**Navigation** (native-app-only; web and Expo apps keep their existing flat 5-tab layout, unchanged):
- Bottom nav: **Dashboard | Flat Management | Discover | Profile**
- "Flat Management" opens to a sub-screen with **Tasks** and **Expenses** as its two areas
- Discover stays a standalone top-level tab, anticipating its native-only enhancements later

## 5. Auth

- Firebase Auth, same two methods as web: native Google Sign-In SDK, and Email/Password.
- Session persists across app restarts via the Firebase Auth SDK — no re-login on every launch.
- First sign-in (Google or email) creates the `users/{uid}` document the same way the web app does, so one account works identically across web and native.

## 6. Error handling & connectivity (online-only)

- Every repository call is wrapped so failures surface as a typed result the ViewModel can render — no silent failures, no crashes.
- No internet → a clear, dedicated "No connection" state per screen — not an infinite spinner, not stale/fake data.
- Firestore permission-denied errors (e.g. a just-kicked member's screen still open) surface as a plain-language message, not a raw error code.

## 7. Non-functional bar — Play Store readiness

Google Play approval is a hard gate, treated as an acceptance criterion throughout the build, not a final checklist:

- **Security**: authorization enforced server-side via Firestore rules (not just in-app checks); no admin/service-account secrets ever embedded in the APK; HTTPS-only (guaranteed by the Firebase SDK).
- **Privacy/compliance**: a Data Safety form (declaring what's collected — email, expense amounts, flat-discovery data — and why) and a linked Privacy Policy are mandatory before Google will approve the app.
- **Permissions minimalism**: request only Android permissions actually used; no broad/unjustified access.
- **User flow quality**: every screen needs a working empty state, loading state, and error state, not just the happy path — both a Play Store review concern and a direct expression of the "don't miss a single condition" requirement.
- **Loading performance**: screens show a skeleton/spinner immediately rather than a blank screen while Firestore data loads; lists paginate rather than pulling an entire collection at once as flats grow.

## 8. Testing

Unit tests on each repository's business logic (rotation math, expense splitting, settlement calculations), run against known good/bad cases drawn from the web app's existing, validated behavior — this is how "missed a condition" bugs get caught before Play Store submission rather than after.

## 9. Explicitly out of scope for v1

Deferred to their own future design pass, not bundled into this one:
- Push notifications (FCM) — v1 relies on in-app real-time updates only, same as the web app today
- Discover's enhanced native-only features (beyond web parity)
- Offline/cached usage
