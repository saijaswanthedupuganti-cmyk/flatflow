# Habitiq Android releases

## Canonical Firebase

**Only `garbage-f79f7`** (the `C:\garbage` web project). Google for Startups / enterprise subscription lives here. All clients — web, PWA, and Android — must use this project.

**Abandoned / do not ship:** `habitiq-by-jaswanth` and any other Firebase projects. Multi-project was a mistake.

**Verified Aug 8, 2026:** `google-services (14).json` is `garbage-f79f7` with three Android apps (`habitiq.app`, `app.habitiq.mobile`, `app.habitiq.nativedev`). Installed in `C:\Users\user\Downloads\habitiq` for the native sample build.

---

## What to install now

| APK | Status | Firebase | Use |
|-----|--------|----------|-----|
| **`Habitiq-native-garbage-f79f7-debug.apk`** | **Install this** | `garbage-f79f7` ✅ | Real native app — Flat Board, tasks, map search, expenses |
| `Habitiq-0.2.0-web-debug.apk` | Temporary bridge only | `garbage-f79f7` (via habitiq.app) | Fallback if native build unavailable |

```bash
adb install -r C:\garbage\releases\Habitiq-native-garbage-f79f7-debug.apk
```

The WebView APK loads https://habitiq.app. It is a **temporary bridge only** — not the product. Use the native APK above for the real Android experience on `garbage-f79f7`.

---

## Build targets (engineering)

| Path | Role |
|------|------|
| `C:\garbage` | Web source of truth + `firestore.rules` + `garbage-f79f7` config |
| `C:\habitiq_jaswanth` | Native Android implementation workspace (wire to `garbage-f79f7`) |
| `C:\garbage\android` | Native + WebView shell in web monorepo (also `garbage-f79f7`) |
| `C:\Users\user\Downloads\habitiq` | **Feature sample** — Flat Board, map search, tasks UI. Now wired to `garbage-f79f7`. |

**Goal:** Real native APK with sample features (Flat Board, etc.) on `garbage-f79f7` schema (`seekerProfiles`, `discoveryTags` in web `firestore.rules`).

---

## Native build notes (Aug 8, 2026)

- **APK:** `Habitiq-native-garbage-f79f7-debug.apk` (~27 MB)
- **Package:** `habitiq.app`
- **Firebase project:** `garbage-f79f7`
- **Build fixes applied:** Gradle 9.3.1 (AGP 9.1.1), kapt + `kotlin-metadata-jvm:2.2.10` for Room/Kotlin 2.2 compatibility
- **Google Sign-In:** Debug keystore SHA-1 is `65:B5:8F:48:69:8F:06:08:83:C2:74:27:35:C2:C8:DA:BD:B8:52:10`. Register it in Firebase Console → Project Settings → `habitiq.app` Android app → Add fingerprint if Google Sign-In fails.
