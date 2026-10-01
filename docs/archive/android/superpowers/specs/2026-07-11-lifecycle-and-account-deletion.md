# Technical Fixes & Account Deletion — Design

**Date:** 2026-07-11
**Status:** Approved by Sai, ready for implementation planning

## Context

An external audit (a different AI tool, not from this session) reviewed the codebase and produced four documents (`implementation_plan audit 3`, `implementation_plan4`, `implementation_planadudit`, `security_policy_audit.md`) with a mix of valid findings and findings based on missing project context. This spec covers only the validated subset — see "Explicitly rejected" below for what was deliberately excluded and why.

## In scope

### 1. ViewModel lifecycle fix

**Problem:** every ViewModel in `HabitiqApp.kt`'s nav graph is created via `remember { XViewModel(...) }`, which ties its lifetime to Compose's composition, not to Android's `ViewModelStoreOwner`. On a configuration change (screen rotation), the ViewModel is destroyed and recreated, re-running `init` logic (including Firestore fetches) unnecessarily.

**Fix:** replace `remember { ... }` with `androidx.lifecycle.viewmodel.compose.viewModel()`, using the `viewModelFactory { initializer { ... } }` DSL (needed because these ViewModels take constructor arguments, not a no-arg constructor). Applies to: `LoginViewModel`, `SignupViewModel`, `HomeViewModel`, `CreateFlatViewModel`, `JoinFlatViewModel`, `FlatHomeViewModel` (the last one needs its factory re-created per `flatId`, since a different flat is logically a different ViewModel instance).

### 2. Lifecycle-aware state collection

**Problem:** `collectAsStateWithLifecycleCompat()` (in `ComposeUtil.kt`) is currently a plain `collectAsState()` alias — deliberately flagged with a comment when written ("swap this one function's body" later). Firestore listeners (e.g. `MembersRepository.observeMembers`) keep collecting even when the app is backgrounded, wasting battery/data.

**Fix:** add the `androidx.lifecycle:lifecycle-runtime-compose` dependency and rewrite the function to call the real `androidx.lifecycle.compose.collectAsStateWithLifecycle()`, which pauses collection outside `STARTED` lifecycle state. No call sites change — they all go through this one function.

### 3. Account deletion (Google Play policy requirement)

**Problem:** Google Play requires any app supporting account creation to offer in-app account deletion. Not yet built.

**Fix:**
- `AuthRepository.deleteAccount(): Result<Unit>` — calls `firebaseAuth.currentUser?.delete()`. Maps `FirebaseAuthRecentLoginRequiredException` specifically to "Please sign out and sign back in, then try deleting your account again" (a full re-authentication flow is out of scope for this pass — this is an honest, minimal handling of a real Firebase constraint, not a gap being hidden).
- `UsersRepository.deleteUserData(uid): Result<Unit>` — deletes the `users/{uid}` document. Basic cleanup only; deep cleanup of a deleted user's flat memberships/activity would need Cloud Functions (server-side, since a deleted user's client can't run further Firestore writes as themselves after `delete()` succeeds) and is explicitly out of scope.
- `SettingsScreen.kt` — functional, not redesigned: user's email, a Sign Out button (moved here from `HomeScreen`), and a Delete Account button behind a confirmation dialog (destructive action, must not be one tap away).
- Nav: new `SETTINGS` route; `HomeScreen` gets a top-bar gear icon linking to it, and loses its inline Sign Out button.

## Explicitly rejected (from the external audit, not carried into this plan)

- **A new `firestore.rules` file** — the real, already-deployed production rules exist at `C:\garbage\firestore.rules` (reviewed in depth earlier this session: admin/member checks, 8-member cap enforcement, already shared with and used by the web app). Firestore rules deploy per Firebase *project*, not per client codebase. A second, simplified draft risks eventually being deployed and clobbering the real rules.
- **Approval-mode joining ("instant-join is a security loophole")** — this mischaracterizes a deliberate, already-confirmed v1 scoping decision (instant-join only, approval mode explicitly deferred to a follow-up) as an unintended bug.
- **`FlatHomeScreen` visual redesign into a "modern dashboard"** — belongs to the already-planned professional UX/UI design pass for the roommate-view screens (Profile, Discover, Flat Management, Dashboard, Onboarding), not a technical-fix plan.
- **`minifyEnabled`/`shrinkResources` for release builds** — legitimate but not urgent at this development stage; a pre-launch checklist item, not a task now.

## Testing

Unit tests where there's real pure logic (the `FirebaseAuthRecentLoginRequiredException` → message mapping, matching the `AuthErrorMapper`/`mapFlatError` pattern already established). The lifecycle fix and account-deletion UI flow are verified via the same manual on-device testing pattern used throughout this project (Task 10, which follows this plan).
