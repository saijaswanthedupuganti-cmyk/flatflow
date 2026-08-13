# Habitiq Android — Install This APK

## Install ONLY this file

**`C:\garbage\releases\Habitiq-FINAL.apk`**

| | |
|---|---|
| **Package** | `habitiq.app` |
| **Firebase** | `garbage-f79f7` (same backend as web — your login and flat data sync) |
| **Version** | 0.4.0-native (versionCode 3) |
| **Type** | Native Jetpack Compose — **not** a WebView wrapper |
| **Build** | Signed release APK from `C:\garbage\android` |
| **Size** | ~4.4 MB release (minified) · ~25 MB debug (`Habitiq-FINAL-debug.apk`) |
| **Feature audit** | `APK_FEATURE_CHECKLIST.md` in this folder |

### Install on your phone

```powershell
adb install -r C:\garbage\releases\Habitiq-FINAL.apk
```

Or copy `Habitiq-FINAL.apk` to your phone and open it (allow install from unknown sources if prompted).

---

## How to log in

1. Open **Habitiq** on your phone.
2. **Google Sign-In** — tap the Google button (uses Firebase Auth + Credential Manager).
3. **Email/password** — use your existing Habitiq account, or tap Sign up to create one.

Both methods connect to the live `garbage-f79f7` Firebase project. Data you create in the app can appear on the web app too.

---

## What this app does

### Find flats

- **Discover** tab — browse vacancy listings from flats with an open room (`vacancy.active == true` on Firestore).
- Filter by city or area.
- **Seekers** tab — browse roommate seeker profiles; publish your own; **Chat** with other users.
- Vacancies and seekers load from Firebase in real time.

### Maintain flats

After you **create** or **join** a flat (onboarding wizard):

| Area | Where | What you can do |
|------|--------|-----------------|
| **Home** | Home tab | Dashboard: tasks, members, activity, monthly spend |
| **Tasks** | Tasks tab → Chores | List/filter tasks, complete (rotation advances), create tasks, going away, swap requests |
| **Expenses** | Tasks tab → Money | Add expenses, see balances |
| **Bills & settlements** | Tasks tab → Bills, or Plus → Bills & Settlements | Recurring bills, bill instances, settle up |
| **Profile** | Profile tab | Display name, settings, sign out |
| **Settings** | Profile → Account Settings | Biometric app lock, delete account |

**Plus button** (center): quick add task, expense, bills/settlements, invite roommate.

**Onboarding**: Create flat (Figma wizard with map location picker) or Join flat (invite code).

---

## Bottom navigation

**Home** | **Discover** | **Plus** | **Tasks** | **Profile**

---

## Ignore older APK names

Do not install these — they are superseded:

- `Habitiq-final-full-debug.apk`
- `Habitiq-final-full-release.apk`
- `Habitiq-complete-firebase-debug.apk`
- Any other `Habitiq-*.apk` in this folder except **`Habitiq-FINAL.apk`**

---

## Honest notes (Aug 2026)

See **`APK_FEATURE_CHECKLIST.md`** for the full web-parity audit. Quick summary:

| Feature | Status |
|---------|--------|
| Native login (Google + email + forgot password) | ✅ / ⚠️ release Google needs SHA-1 |
| Discover — vacancies + seekers + chat | ✅ UI complete; deploy `firestore.rules` for chat |
| Multi-flat switcher, members, manage flat, activity | ✅ Added |
| Tasks — group/temp, swap accept reassigns assignee | ✅ |
| Bills — collection tracking, month close | ✅ |
| Expenses — equal split | ✅; custom split / edit/delete UI partial |
| Push notifications (FCM) | ⚠️ Client ready; no server sender |
| Biometric lock | ✅ |
| Maps | ⚠️ Needs `MAPS_API_KEY` in `local.properties` |

### Google Sign-In

Debug/release keystores: `android/debug.keystore` (debug) and `android/habitiq-release.keystore` (release). Register each SHA-1 in Firebase Console → Project Settings → Your apps → `habitiq.app`.

Web client ID: `1030913049565-1jr9s3371nlk1t693ok533283sif2vgm.apps.googleusercontent.com`
