# Habitiq Android (`habitiq.app`)

Play-ready native app wired to Firebase project **garbage-f79f7**.

## Build

```powershell
cd C:\garbage\android
.\gradlew assembleDebug
```

Output: `app\build\outputs\apk\debug\app-debug.apk`

Copy to releases:

```powershell
Copy-Item app\build\outputs\apk\debug\app-debug.apk ..\releases\Habitiq-complete-firebase-debug.apk
```

## Firebase

- `app/google-services.json` — project `garbage-f79f7`, package `habitiq.app`
- Firestore field names match the web app (`store/useFlatStore.ts`, `firestore.rules`)

## Google Sign-In (debug SHA)

Debug APKs use `android/debug.keystore` (not the default `~/.android/debug.keystore`).

Register this keystore's **SHA-1** in [Firebase Console](https://console.firebase.google.com/project/garbage-f79f7/settings/general) → Android app `habitiq.app` → Add fingerprint.

To print SHA-1:

```powershell
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

OAuth web client ID (Credential Manager): `strings.xml` → `google_web_client_id`

## Architecture

| Layer | Location |
|-------|----------|
| UI | `app/src/main/kotlin/habitiq/app/ui/` |
| ViewModels | `flat/FlatViewModel.kt`, `home/`, `flats/`, `auth/` |
| Repositories | `data/`, `flats/`, `auth/` |
| Rotation | `lib/RotationEngine.kt` (mirrors `lib/rotationEngine.ts`) |
