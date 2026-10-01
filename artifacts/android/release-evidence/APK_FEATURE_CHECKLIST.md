# Habitiq Android APK — Feature Checklist (Web Parity Audit)

**Build:** 0.4.0-native (versionCode 3) · **Package:** `habitiq.app` · **Firebase:** `garbage-f79f7`  
**Audit date:** 13 Aug 2026  
**APK paths:**

| File | Size | Notes |
|------|------|-------|
| `C:\garbage\releases\Habitiq-FINAL.apk` | **4.42 MB** | Signed release (R8 minify + shrink) — complete app |
| `C:\garbage\releases\Habitiq-FINAL-debug.apk` | **25.46 MB** | Unminified debug — same code, full symbols |

> Release ~4 MB is expected with `isMinifyEnabled=true` + `isShrinkResources=true`. Debug ~25 MB confirms full dependency tree.

---

## Android Permissions (all declared)

| Permission | Purpose |
|------------|---------|
| `INTERNET` | Firebase Auth, Firestore, FCM |
| `ACCESS_NETWORK_STATE` | Connectivity checks |
| `POST_NOTIFICATIONS` | FCM push (Android 13+) |
| `ACCESS_COARSE_LOCATION` | Map picker / flat location |
| `ACCESS_FINE_LOCATION` | Map picker / flat location |
| `USE_BIOMETRIC` | App lock (fingerprint / face) |

---

## Navigation

| Item | Status | Notes |
|------|--------|-------|
| Home \| Discover \| Plus \| Tasks \| Profile | ✅ | `AppShell.kt` — center Plus FAB |
| Tasks tab = maintain flat (Chores / Money / Bills) | ✅ | `TasksScreen.kt` sub-tabs |
| Onboarding: create or join flat | ✅ | Wizard + invite code |
| Deep link `https://habitiq.app` | ✅ | Manifest intent-filter |

---

## Auth

| Feature | Status | Notes |
|---------|--------|-------|
| Google Sign-In | ⚠️ | Works in debug; **release needs SHA-1** in Firebase Console |
| Email / password login | ✅ | |
| Sign up | ✅ | |
| Forgot password | ✅ | `LoginScreen` + `AuthRepository.sendPasswordResetEmail` |
| Sign out | ✅ | |
| Delete account | ✅ | Settings |
| Biometric app lock | ✅ | Toggle in Settings; locks on `ON_STOP` |

---

## Discover (Find Flats)

| Feature | Status | Notes |
|---------|--------|-------|
| Vacancy listings (real-time) | ✅ | `DiscoverBoardScreen` — filter by city/area |
| Seeker profiles browse | ✅ | |
| Publish seeker profile | ✅ | `SeekerProfileForm` |
| Direct chat (messages collection) | ⚠️ | UI complete; **Firestore rules must be deployed** |
| Vacancy publish from Android | ✅ | `ManageFlatScreen` (admin) |
| Join approval mode | ✅ | `JoinFlatViewModel` + `MembersScreen` approve/reject |

---

## Flat Management (Maintain Flats)

| Feature | Status | Notes |
|---------|--------|-------|
| Create flat (Figma wizard) | ✅ | Basics + map location |
| Join flat (invite code) | ✅ | Auto + approval modes |
| Multi-flat switcher | ✅ | `FlatSwitcherSheet` — tap flat name on Home |
| Leave flat | ✅ | `ProfileScreen` |
| Rename flat | ✅ | `ManageFlatScreen` |
| Join mode (auto / approval) | ✅ | `ManageFlatScreen` |
| Members list | ✅ | `MembersScreen` |
| Kick member (admin) | ✅ | |
| Join request approve / reject | ✅ | |
| Activity log | ✅ | `ActivityLogScreen` |
| Transfer admin | ❌ | `MembersRepository.transferAdmin` exists; **no UI** |
| Invite roommate (share code) | ✅ | Plus sheet / profile |

---

## Tasks & Rotation

| Feature | Status | Notes |
|---------|--------|-------|
| Task list + filters | ✅ | |
| Complete task (rotation advances) | ✅ | `RotationEngine` |
| Create rotating duty | ✅ | |
| Create group duty (`group_duty`) | ✅ | `CreateTaskTypeScreen` |
| Create temp task | ✅ | `CreateTempTaskScreen` |
| Going away (OOS swap batch) | ✅ | `GoingAwayScreen` |
| Task detail + rotation queue | ✅ | Reliability % shown |
| Swap request (per flatmate) | ✅ | `TaskDetailScreen` |
| Swap accept → **reassign assignee** | ✅ | `transferTask` on accept (web parity) |
| Swap decline | ✅ | |
| Swap review sheet (incoming) | ✅ | `SwapReviewSheet` |
| Admin "All Swaps" view | ❌ | Web has toggle; Android shows incoming only |
| Manual assign (admin) | ⚠️ | `FlatViewModel.manuallyAssignTask` — **no UI** |
| Delete task (admin) | ⚠️ | VM + repo — **no UI** |
| Reliability score display | ✅ | Queue + members list |

---

## Expenses

| Feature | Status | Notes |
|---------|--------|-------|
| Add expense (equal split) | ✅ | |
| Custom split | ⚠️ | VM supports `customSplits` — **UI only equal** |
| Edit expense | ⚠️ | Repo + VM — **no UI** |
| Delete expense | ⚠️ | Repo + VM — **no UI** |
| Balance summary | ✅ | Per-person owes / owed |
| Mark Received (creditor settles) | ⚠️ | `recordMarkReceived` in VM — **no Expenses UI** |
| Expanded balance breakdown | ❌ | Web shows per-expense drill-down |
| Filter transactions by person | ❌ | Web feature |

---

## Bills & Settlements

| Feature | Status | Notes |
|---------|--------|-------|
| Create recurring bill (admin) | ✅ | Variable + fixed amount |
| Generate bill instance | ✅ | |
| Mark bill paid | ✅ | |
| Skip bill instance | ✅ | |
| Payer collection tracking | ✅ | `markBillCollected` checkboxes per member |
| Bill instances tab | ✅ | |
| Suggested settlements | ✅ | `suggestSettlements` |
| Record suggested settlement | ✅ | |
| Manual settle up (debtor pays) | ✅ | `SettleUpScreen` |
| Mark Received settle (creditor) | ⚠️ | VM only — Bills settle UI is debtor-direction |
| Month-end close | ✅ | Admin `closeMonth` + closed badge |
| Carry-forward on close | ✅ | `MonthCycle.carryForwardOut` |
| Edit recurring bill | ⚠️ | `updateRecurringBill` in repo — **no UI** |

---

## Maps

| Feature | Status | Notes |
|---------|--------|-------|
| Map location picker (create flat) | ⚠️ | `MapLocationPicker.kt` — needs `MAPS_API_KEY` in `android/local.properties` |
| Text fallback (city/area fields) | ✅ | Works without key |

---

## Push Notifications (FCM)

| Feature | Status | Notes |
|---------|--------|-------|
| `HabitiqFcmService` registered | ✅ | Manifest |
| Token saved to user doc | ✅ | `MessagingRepository` |
| Topic subscribe `flat_{flatId}` | ✅ | `FcmNotificationHelper` |
| `POST_NOTIFICATIONS` permission | ✅ | |
| Server-side push (Cloud Functions) | ❌ | **No backend sender** — client ready only |

---

## Web Routes NOT Ported (intentional / low priority)

| Web page | Status | Notes |
|----------|--------|-------|
| `/dashboard/analytics` | ❌ | Firebase Analytics events logged; no UI |
| `/dashboard/calendar` | ❌ | |
| `/dashboard/insights` | ❌ | |
| NPS banner | ❌ | |
| Subscription gate / coupon trial | ❌ | Rules exist for coupons; no Android UI |
| Web-only admin analytics widgets | ❌ | |

---

## Firestore Rules (repo vs deployed)

| Collection / rule | In `firestore.rules` | Deployed to `garbage-f79f7` |
|-------------------|----------------------|----------------------------|
| `flats/*` (members, tasks, expenses, bills, swaps…) | ✅ | ✅ (existing) |
| `seekerProfiles/{profileId}` | ✅ **added this session** | ❌ **needs deploy** |
| `messages/{messageId}` | ✅ **added this session** | ❌ **needs deploy** |
| `coupons/{code}` | ✅ | ✅ |

**Deploy command (requires `firebase login --reauth`):**
```powershell
firebase deploy --only firestore:rules --project garbage-f79f7
```

---

## Firebase Console Keys Required (cannot fix in APK alone)

| Item | Why | Action |
|------|-----|--------|
| **Release SHA-1** | Google Sign-In on release APK | Firebase Console → Project Settings → Android app `habitiq.app` → add SHA-1 from `habitiq-release.keystore` |
| **Debug SHA-1** | Google Sign-In on debug APK | Add debug keystore SHA-1 (optional for dev) |
| **MAPS_API_KEY** | Google Maps SDK | Google Cloud Console → enable Maps SDK for Android → restrict to `habitiq.app` → set in `android/local.properties` |
| **Firestore rules deploy** | Discover chat + seeker profiles | `firebase deploy --only firestore:rules` |
| **FCM server key / Cloud Functions** | Actual push delivery | Wire Cloud Function or Admin SDK to send on swap/join events |
| **Web client ID** | Already in project | `1030913049565-1jr9s3371nlk1t693ok533283sif2vgm.apps.googleusercontent.com` |

---

## What Was Added This Session ("add all")

1. **Firestore rules** — `seekerProfiles` + `messages` collections
2. **Discover** — full board: vacancies, seekers, chat, seeker profile form
3. **Manage flat** — rename, join mode, vacancy publish
4. **Members** — list, kick, join request approve/reject
5. **Activity log** screen
6. **Multi-flat switcher** sheet
7. **Swap accept parity** — `transferTask` + activity log on accept
8. **Bills** — collection tracking, month close, month cycle state
9. **Leave flat** + improved `leaveFlat` in UsersRepository
10. **Forgot password** on login
11. **Join approval mode** on join flow
12. **Group + temp tasks** create flows
13. **Task detail** — swap request buttons, reliability display
14. **Profile** — links to members, manage flat, activity, switcher, leave
15. **Home** — real pending join/swap counts, flat name opens switcher
16. Repositories expanded: transferTask, manuallyAssignTask, updateExpense, closeMonth, markBillCollected, join requests, vacancy update, markSwapRead, password reset

---

## Summary Legend

| Symbol | Meaning |
|--------|---------|
| ✅ | Implemented and usable in APK |
| ⚠️ | Partial — backend/VM ready but UI missing or external key needed |
| ❌ | Not implemented on Android |

**Overall:** Core maintain-flat loop (tasks, rotation, swaps with reassign, bills, settlements, month close, expenses, members, discover vacancies/seekers/chat) is **✅**. Remaining gaps are mostly **admin polish UI** (transfer admin, all-swaps view, expense edit/delete/custom split), **web-only analytics pages**, and **Firebase Console / deploy** items for Discover chat rules, Maps key, release SHA-1, and FCM server push.
