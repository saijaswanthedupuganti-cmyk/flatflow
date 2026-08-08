# Habitiq Android Release APKs

## Habitiq-complete-firebase-debug.apk

**Path:** `C:\garbage\releases\Habitiq-complete-firebase-debug.apk`

| Field | Value |
|-------|-------|
| Package | `habitiq.app` |
| Firebase project | `garbage-f79f7` (Google Startups) |
| Build | `assembleDebug` from `C:\garbage\android` |
| Version | 0.3.0-native (versionCode 2) |

### What works (Firestore-backed)

- **Login** — Google Sign-In + email/password against Firebase Auth (`garbage-f79f7`)
- **Onboarding** — Create flat / join flat writes `flats`, `members`, and `users` documents
- **Home** — Dashboard reads tasks, members, activity, monthly expenses via real-time listeners
- **Discover** — Lists flats with `vacancy.active == true` from Firestore
- **Tasks** — List, complete (rotation engine), create (admin), OOS toggle, swap requests
- **Expenses** — List, add equal-split expenses, pairwise balance summary
- **Profile** — Load/save `users/{uid}.displayName` in Firestore
- **Settings** — Sign out, account deletion (Firestore cleanup + Auth delete)

### Bottom navigation

Home | Discover | Plus (quick add) | Tasks | Profile

### Google Sign-In (debug)

Debug builds sign with `C:\garbage\android\debug.keystore` (SHA-1 must be registered in Firebase Console for package `habitiq.app`). Web client ID: `1030913049565-1jr9s3371nlk1t693ok533283sif2vgm.apps.googleusercontent.com`.

### Not fully wired in this build

- Recurring bills / bill instances / settlements UI (Firestore rules exist; no native screens yet)
- Seeker profiles / in-app chat (web uses flat `vacancy` embed; no separate seeker collection)
- Push notifications (FCM service not included in this tree)
- Biometric vault UI from the Downloads sample

### Install

```powershell
adb install -r C:\garbage\releases\Habitiq-complete-firebase-debug.apk
```
