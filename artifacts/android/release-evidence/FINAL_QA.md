# Habitiq Android — FINAL QA (23 Aug 2026)

**APK:** `releases/Habitiq-FINAL.apk`  
**Package:** `habitiq.app` · **Firebase:** `garbage-f79f7`  
**Build:** `assembleDebug` · version `0.4.0-native` (versionCode 3)  
**Unit tests:** 24/24 passed

---

## Authentication

| Check | Status | Notes |
|-------|--------|-------|
| Email login | ✅ | Firebase Auth + profile bootstrap via `ensureUserDocument` |
| Email signup | ✅ | Routes to 4-option intent chooser when no flat |
| Google Sign-In | ✅ | Credential Manager + web client ID from `google-services.json` |
| Forgot password | ✅ Fixed | Success now shows info message (not red error) |
| Session persistence | ✅ | `AuthRepository` auth-state listener; cold start routes correctly |
| Returning user with flat | ✅ | Skips chooser → Home |
| Returning user without flat | ✅ | Intent chooser on cold start |
| 4-option chooser | ✅ | Find flat / Find person / Create room / Join |
| Auth error messages | ✅ Fixed | Generic fallback (no raw exception class names) |
| Signup → chooser back stack | ✅ Fixed | Clears login/signup routes after auth |

---

## Discover / Use a flat

| Check | Status | Notes |
|-------|--------|-------|
| Vacancy Firestore listener | ✅ | `DiscoveryRepository.observeActiveVacancies()` on `flats` collection |
| Search + filters | ✅ | City/area, rent, gender, flat type, room type, lifestyle tags |
| Vacancy cards + tags | ✅ | `UseAFlatDiscoverContent` + `VacancyListingCard` |
| Empty / error / loading states | ✅ | Dedicated UI for each |
| Contact poster → chat | ✅ Fixed | Partner name resolves from vacancy admin, not seeker list |
| Find a person | ⚠️ Stub | Intentional "coming soon" (seeker browse not shipped) |

---

## Tasks

| Check | Status | Notes |
|-------|--------|-------|
| Create recurring duty | ✅ | Queue order, frequency, priority |
| Create temp (one-time) task | ✅ | Single assignee |
| Complete + rotation | ✅ | `RotationEngine` matches web queue logic |
| Filters (All / Mine / Overdue) | ✅ | Tasks tab |
| Swap requests | ✅ | Banner + review sheet |
| Going away / OOS | ✅ | Swap requests + member status |
| Task detail queue | ✅ Fixed | NOW row decluttered (reliability in subtitle, no extra bar) |
| Admin Home ↔ Tasks switch | ✅ | `AdminManageToggle` on Home |

---

## Expenses & Bills

| Check | Status | Notes |
|-------|--------|-------|
| Add expense | ✅ | Equal split, categories |
| Expense list | ✅ Fixed | Empty state when no expenses |
| Balances | ✅ Fixed | Includes settlements; aligns with web `expenseUtils` pairwise logic |
| Bills screen | ✅ | Recurring bills, instances, settle-up |
| Plus → Add expense / Bills | ✅ | Opens Home expenses mode or overlay |

---

## Navigation & Other

| Check | Status | Notes |
|-------|--------|-------|
| Join flat back button | ✅ Fixed | No dead end from intent chooser |
| Settings back button | ✅ Fixed | Returns to Profile |
| Leave last flat | ✅ Fixed | Routes back to intent chooser |
| Plus → Invite roommate | ✅ Fixed | Shares flat invite code |
| Create / join flat wizards | ✅ | Back + success routing to Home |
| Profile / members / activity | ✅ | Overlay navigation with back |
| Permissions | ✅ | Internet, network, notifications, location, biometric |

---

## Build verification

```
./gradlew test assembleDebug — BUILD SUCCESSFUL
```

Only installable APK in repo: **`releases/Habitiq-FINAL.apk`**
