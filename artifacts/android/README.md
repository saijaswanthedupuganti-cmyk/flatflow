# Android artifact manifest

APK binaries are separated from application source and ignored by Git. This manifest records what exists locally and what its evidence actually proves.

## Current test candidate

| Field | Value |
|---|---|
| File | `apk/candidates/Habitiq-2026-09-26-candidate-debug.apk` |
| Package | `habitiq.app` |
| Version | `0.4.0-native` (`versionCode 3`) |
| Build type | Debug candidate |
| Size | 28,113,565 bytes |
| SHA-256 | `1F61C86584C3AEFDB921F690E236D9656222D847F1D6519D2CB05AEF7D62BBC7` |
| Recorded checks | 33 unit tests, Android lint, debug assembly |
| Open gates | Device/emulator, live Firebase, offline/retry, accessibility, rotation, signed release |
| Release status | **Not release-ready; not deployed** |

Install from the repository root:

```powershell
adb install -r .\artifacts\android\apk\candidates\Habitiq-2026-09-26-candidate-debug.apk
```

Evidence: [`../../docs/delivery/2026-09-26-implementation/execution-status.md`](../../docs/delivery/2026-09-26-implementation/execution-status.md).

## Legacy local artifacts

| File | Size | SHA-256 | Meaning |
|---|---:|---|---|
| `apk/legacy/Habitiq-FINAL-debug.apk` | 28,353,898 | `CB27FA86BB7BE3C34BCC1298B8EA101D1E911C36D7AF9A6A9F97F5368454D074` | Older debug artifact; not certified by the current gate |
| `apk/legacy/Habitiq-FINAL.apk` | 27,823,228 | `42C62A5CED70BB1DCF4D0AF3F5158C8727C4B65516EE3FB747531D15A41A6EA6` | Older artifact; current signing/release status unverified |

`FINAL` in a historical filename does not establish release readiness.

## Naming and evidence rule

Use `Habitiq-YYYY-MM-DD-candidate-<build-type>.apk`. Never overwrite a dated artifact. Record version, byte size, SHA-256, signing/build type, exact checks, device coverage, blockers, evidence date, and owner.

Historical release notes and test matrices are under [`release-evidence/`](release-evidence/).
