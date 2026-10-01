# Habitiq Android

Native Kotlin/Jetpack Compose client. Application ID: `habitiq.app`.

## Requirements

- Java 17
- Android SDK compatible with compile/target SDK 36
- Local Firebase Android configuration at `app/google-services.json`
- Optional Maps key in ignored `local.properties`

## Verify

Run from this directory:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

The generated debug APK is under `app/build/outputs/apk/debug/`. For a deliberate test cycle, copy it to `../../artifacts/android/apk/candidates/` using a dated `candidate-debug` filename and update the artifact manifest with its size, SHA-256, checks, and blockers.

## Architecture

| Layer | Location |
|---|---|
| Application/navigation | `app/src/main/kotlin/habitiq/app/HabitiqApp.kt` |
| UI | `app/src/main/kotlin/habitiq/app/ui/` |
| Authentication | `app/src/main/kotlin/habitiq/app/auth/` |
| View models | `flat/`, `home/`, `flats/` and feature packages |
| Repositories/data | `data/`, `flats/` and feature repositories |
| Shared utilities | `lib/` |
| Unit tests | `app/src/test/` |

## Shared backend

The app uses the shared Firebase contracts under [`../../backend/firebase/`](../../backend/firebase/). Data-model or rule changes must remain compatible with the web client in `../web/`.

## Authentication and signing

- `app/google-services.json` is build-critical and currently follows the repository’s explicit tracking policy.
- `debug.keystore`, `habitiq-release.keystore`, `keystore.properties`, `local.properties`, and service-config backups are local-only and ignored.
- Back up the release keystore securely outside the repository. Losing it can prevent future app updates.
- A debug APK is not a signed production release.

Historical authentication cases are under [`../../artifacts/android/release-evidence/AUTH_USE_CASES.md`](../../artifacts/android/release-evidence/AUTH_USE_CASES.md). Revalidate them before treating them as current certification.
