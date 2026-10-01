# Quality and release gates

Run commands from the repository root unless stated otherwise.

## Web

```powershell
npm run lint
npm run build
npm test
```

## Android

```powershell
cd apps/android
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

## Release evidence

A build pass is not release readiness. Record:

- application and version/version code;
- commit or working-tree baseline;
- test command and result;
- device/emulator and OS coverage;
- Firebase/auth, offline, retry, rotation, and background behavior;
- accessibility, large text, and reduced-motion coverage;
- signing/build type and installation result;
- artifact byte size and SHA-256;
- remaining blockers and evidence date.

The newest detailed matrix is [`../delivery/2026-09-26-implementation/06-QA-AND-RELEASE.md`](../delivery/2026-09-26-implementation/06-QA-AND-RELEASE.md).
