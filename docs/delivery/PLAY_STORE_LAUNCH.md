# Oddroof on Google Play — launch guide

Written 18 Oct 2026 for the first Play submission. Plain-language, step by step.
App: **Oddroof** · package `habitiq.app` · v1.18 (code 21) · min Android 8 · target SDK 36.

---

## 0. The one rule that sets the date

A **new personal developer account** cannot publish to everyone straight away. Google requires:

1. A **closed test** with **at least 12 testers**,
2. who stay opted in for **14 days in a row**,
3. then you **apply for production** from the Play Console dashboard (Google reviews it, usually a few days).

So: Day 1 = account + closed test live. Earliest public Play launch ≈ Day 15–20.
Until then, people install from **habitiq.app** (the APK download button).

Tip: if a tester leaves and you drop below 12, the 14-day clock can restart. Recruit 15–20.

---

## 1. Day 1 checklist (in order)

- [ ] Buy the **Personal** developer account ($25) at play.google.com/console, verify identity (ID + phone).
- [ ] **Create app** → name `Oddroof`, default language English (India), App, Free.
- [ ] **Play App Signing**: accept (default). Upload `app-release.aab (v1.18)` (path at the end).
  Your local `habitiq-release.keystore` becomes the *upload key*. **Back it up** (keystore file + passwords) somewhere safe offline. Losing it means a support ticket to reset.
- [ ] After upload, open **Setup → App signing**, copy the **App signing key SHA-1**, and add it in
  **Firebase console → Project settings → Your apps → habitiq.app → Add fingerprint**.
  Also add the **upload key SHA-1** below. Without these, *Continue with Google* fails for Play installs.
- [ ] Fill every section in **App content** (section 3 below).
- [ ] Fill the **Store listing** (section 2) and upload graphics.
- [ ] **Testing → Closed testing** → create track → add testers (email list or Google Group) → roll out → share the opt-in link.

Fingerprints to add in Firebase now (for the APK from habitiq.app):

| Key | SHA-1 |
| --- | --- |
| Release / upload key | `D9:C9:98:A9:59:2D:64:D7:35:9F:E4:E4:22:73:65:55:07:8F:CD:40` |
| Debug key (test builds) | `65:B5:8F:48:69:8F:06:08:83:C2:74:27:35:C2:C8:DA:BD:B8:52:10` |
| Play app signing key | copy from Play Console after the first upload |

---

## 2. Store listing (copy ready to paste)

**App name** (30 max): `Oddroof: Flat, Bills & Chores`

**Short description** (80 max):
`Split expenses, rotate chores and find a flat or flatmate. Just say it.`

**Full description** (4000 max):

```
Oddroof keeps a shared flat running without the group-chat drama.

SPLIT EXPENSES, FAIRLY
• Add an expense in seconds and split it with everyone or just some flatmates
• See who owes whom, with a clear breakdown of every balance
• Record a settlement when someone pays you back
• Oddroof records settlements; it does not move money

CHORES THAT ROTATE THEMSELVES
• Daily, weekly and monthly tasks that pass to the next person automatically
• Going away? Pause your turns and your flatmates are covered
• Swap a task with a flatmate when plans change

MONTHLY BILLS
• Rent, electricity, Wi-Fi and more, with shares worked out for everyone

JUST SAY IT
• Tap the mic and talk the way you'd text: "I just spent 500 on groceries"
• Ask "What do I owe?", "What's due today?" or "Rooms in Kondapur under 10k"
• Nothing is saved until you confirm it. Prefer typing? That works too

FIND A FLAT OR A FLATMATE
• Browse rooms and flats posted by Oddroof members
• Post a vacancy for your flat, or post that you're looking
• Safety first: never pay rent before viewing in person, and report or block anyone

YOUR DATA
• Your expenses, tasks and balances are visible only to your flatmates
• Delete your account any time from the app or at habitiq.app/delete-account

Questions? hello@habitiq.app
```

Check before publishing: every line above must stay true in the build you submit. Remove any feature you hide before launch.

**Category**: House & Home. **Tags**: roommates, expense splitting, chores.
**Contact email**: hello@habitiq.app · **Website**: https://habitiq.app
**Privacy policy URL**: https://habitiq.app/privacy

**Graphics you need**

| Asset | Size | Notes |
| --- | --- | --- |
| App icon | 512 × 512 PNG | ready: `docs/delivery/play-assets/icon-512.png` (the real app icon) |
| Feature graphic | 1024 × 500 PNG | ready: `docs/delivery/play-assets/feature-graphic-1024x500.png` |
| Phone screenshots | 2 to 8, 1080 × 1920 or the app's 1170 × 2532 | Home, Voice (expense card), Expenses, Tasks, Discover |

The app's own renders in `apps/android/app/build/screenshots/` (home-light, voice-*, expenses-*, tasks-*, discover-*) are real app UI and can be used as screenshots (light versions read best).

---

## 3. App content answers

**Privacy policy**: https://habitiq.app/privacy

**Ads**: No, the app has no ads.

**Advertising ID**: *No*, the app does not use the advertising ID (AD_ID permission removed in v1.18).

**App access**: *All or some functionality is restricted* → give the reviewer a working test login:
- Create an email/password account (not Google), e.g. `playreview@…` that you control.
- Put it in a test flat with a few expenses, tasks and a bill so every screen has content.
- Paste email + password + "Open the mic button in the bottom bar to try the assistant" into the instructions.

**Content rating** (IARC questionnaire): category *Utility/Productivity/Communication/Other*.
Answer **Yes** to "users can interact / exchange content" (Discover posts, chat) and to "shares user location?" **No** (we removed location permission). Violence/sex/drugs/gambling: No. Expect a *Teen/12+* type rating.

**Target audience**: **18 and over** only (renting and money). Not designed for children → avoids the Families policy.

**News app**: No. **Government app**: No. **Health**: No.
**Financial features**: *None of these* (Oddroof records shared expenses; it doesn't hold, send or lend money).

**Account deletion**: Yes, users can create accounts.
- Delete-account URL: **https://habitiq.app/delete-account**
- In app: Profile → Privacy & Security → Delete account.

**Data safety** (what to tick):

| Data type | Collected | Shared* | Why | Optional? |
| --- | --- | --- | --- | --- |
| Name (nickname / first name) | Yes | No | App functionality, Account management | Required |
| Email address | Yes | No | Account management | Required |
| Address (flat area / pincode on a Discover post) | Yes | No | App functionality | Optional |
| Other financial info (expense amounts, balances) | Yes | No | App functionality | Required for Expenses |
| Photos (Discover listing photos) | Yes | No | App functionality | Optional |
| Other in-app messages (Discover chat) | Yes | No | App functionality | Optional |
| Other user-generated content (tasks, bills, requests typed/spoken to the assistant) | Yes | No | App functionality | Optional |
| App interactions (analytics) | Yes | No | Analytics | Required |
| Crash logs, Diagnostics | Yes | No | App functionality (stability) | Required |
| Device or other IDs (notification token, Firebase installation ID) | Yes | No | App functionality, Analytics | Required |

\*Google Firebase and Gemini (Google Cloud) process data **on our behalf**, which Play counts as *not shared*.
- Audio: speech is turned into text by the phone's own speech service; the app does not store or send audio files → do **not** tick "Voice or sound recordings".
- Location: **not collected** (no location permission since v1.17).
- Encrypted in transit: **Yes**. Users can request deletion: **Yes**.

**AI-generated content**: the assistant's replies can be reported in-app ("Report this reply" under any AI-written line). Reports land in Firestore `aiReports`; check them weekly.

**User-generated content** (Discover posts, chat): terms acceptance, report and block are in the app; Firestore `discoveryReports` collects reports. Review them weekly.

---

## 4. Closed test plan (the 14 days)

- Invite 15–20 real people (flatmates, friends, college groups). They must **opt in via the link** and install from Play.
- Ask each tester to use it at least twice in the 14 days and leave feedback; Google's production application asks what you learned from testing.
- Ship fixes to the closed track during the 14 days if needed (bump version every upload).
- Day 15: Dashboard → **Apply for production** → answer the questionnaire honestly.

---

## 5. Before every upload

- Bump `versionCode` and `versionName` in `apps/android/app/build.gradle.kts`.
- Build: `.\gradlew.bat testDebugUnitTest lintDebug bundleRelease` from `apps/android`.
- Output: `apps/android/app/build/outputs/bundle/release/app-release.aab`.
- Keep `keystore.properties` and `habitiq-release.keystore` out of git (already ignored) and backed up.

## 6. Still open (owner actions)

- [ ] Deploy Firestore + Storage rules (needs `firebase login --reauth`, then `firebase deploy --only firestore:rules,storage`).
- [ ] Add the SHA-1 fingerprints above in Firebase.
- [ ] In Google Cloud console, restrict the Android API key and the Maps key to package `habitiq.app` + the SHA-1s above.
- [ ] Set a Google Cloud billing budget alert (Gemini usage) since the app has no App Check yet; add App Check (Play Integrity) once the app is installed from Play.
- [ ] Publish the release APK on GitHub so the website Download button works.
