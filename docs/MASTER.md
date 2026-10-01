# Habitiq project master

**Role:** canonical project orientation  
**Updated:** 1 October 2026  
**Latest repository verification:** 1 October 2026  
**Latest product execution evidence:** 26 September 2026

Repository migration evidence: [`delivery/2026-10-01-repository-reorganization/README.md`](delivery/2026-10-01-repository-reorganization/README.md).

## Product purpose

Habitiq helps people sharing a home coordinate recurring tasks, expenses, bills, membership, and room/roommate discovery. Its product promise is a calm, transparent system that reduces manual management and makes shared responsibilities visible.

The current primary information architecture is **Home, Discover, Manage, and Profile**. Manage includes tasks and expenses/bills. Messages are reached through Discover. Any change to these roots requires an explicit product decision.

## Applications

| Application | Stack | Location | State |
|---|---|---|---|
| Web | Next.js 16, React 19, TypeScript, Firebase | `apps/web/` | Existing client |
| Android | Kotlin, Jetpack Compose, Firebase | `apps/android/` | Primary native target |
| iOS | Future SwiftUI/Xcode client | `apps/ios/` | Not implemented |

The clients share product rules and Firebase contracts, not UI code. Android APKs do not run on iOS. Future iOS work requires its own Xcode project, Firebase iOS registration, Apple signing, device testing, and TestFlight evidence.

## Shared backend

Firebase configuration is rooted at [`../firebase.json`](../firebase.json); rules and indexes live under [`../backend/firebase/`](../backend/firebase/).

Every schema change must state:

1. Field name, type, and default.
2. Producer and all readers.
3. Rule and permission impact.
4. Compatibility with older records and clients.
5. Migration/backfill and rollback behavior.
6. Cross-client fixtures and verification.

Do not change shared data contracts as an incidental part of visual work.

## Current evidence

The latest repository verification is [`delivery/2026-10-01-repository-reorganization/README.md`](delivery/2026-10-01-repository-reorganization/README.md). The latest product execution record is [`delivery/2026-09-26-implementation/execution-status.md`](delivery/2026-09-26-implementation/execution-status.md).

- On 1 October 2026, the full web lint, TypeScript check, dependency audit, Next.js production build, and 66-test desktop/mobile browser suite passed.
- On 1 October 2026, Android unit tests (33), lint, warning-clean Kotlin compilation, and debug assembly passed.
- Its dated APK is a debug candidate, not a signed release.
- Device/emulator, live Firebase mutation, offline/retry, accessibility, rotation, and final signing gates remained incomplete.
- External deployment health and store publication were not verified.

Terms:

- **Implemented:** code and focused checks exist.
- **Verified:** recorded evidence covers the stated behavior.
- **Release-ready:** every applicable gate passed.
- **Deployed:** the target environment was observed after deployment.

## Documentation authority

When information conflicts, follow this order:

1. Current direct user decision.
2. Current application code and Firebase rules.
3. Newest dated verification evidence.
4. This master and current platform/architecture guides.
5. Product specifications and older plans.
6. Archived documents.

Every status claim needs an evidence date. Do not create another master document; update this file or the appropriate owned document.

## Ownership map

| Subject | Owner |
|---|---|
| Repository conventions | `docs/architecture/REPOSITORY_STRUCTURE.md` |
| Shared Firebase contracts | `docs/architecture/SHARED_BACKEND.md` |
| Product behavior | `docs/product/` |
| Web implementation | `apps/web/README.md` |
| Android implementation | `apps/android/README.md` |
| Future iOS | `apps/ios/README.md`, `docs/platforms/ios/README.md` |
| Environment/deployment | `docs/operations/` |
| Testing/release gates | `docs/quality/README.md` |
| APK inventory | `artifacts/android/README.md` |
| Current implementation evidence | `docs/delivery/2026-09-26-implementation/` |
| Historical material | `docs/archive/` |

## Immediate priorities

1. Complete Android device, live Firebase, offline, accessibility, and signed-release gates.
2. Coordinate any FCM token-to-Firebase Installation ID migration across Android, web/backend consumers, and stored records.
3. Finish the open Android durable-draft and media-reconciliation work in the execution ledger.
4. Confirm external host settings use `apps/web`, then verify the deployed web application.
5. Start iOS only as a separate native project with shared contract fixtures.
