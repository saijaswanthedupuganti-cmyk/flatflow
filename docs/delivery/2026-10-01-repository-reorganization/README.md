# Repository reorganization — 1 October 2026

## Goal

Convert the mixed root-level project into a universal multi-platform monorepo that a new engineer or coding agent can understand without prior conversation context.

## Structural changes

| Before | After |
|---|---|
| Root Next.js folders/config | `apps/web/` npm workspace |
| `android/` | `apps/android/` |
| No iOS boundary | `apps/ios/` explicit future placeholder |
| Root Firebase rules/indexes | `backend/firebase/` with root CLI mapping |
| `design-plans/` | current evidence in `docs/delivery/`; older audit in `docs/archive/audits/` |
| `design-system/` | `docs/design-system/` |
| `releases/` mixed docs/APKs | `artifacts/android/` candidates, legacy APKs, and evidence |
| Root patch scripts/media | `tools/archive/` and dated documentation archive |
| `project_1/` document dump | classified under `docs/product/` and `docs/archive/` |

The repository root now contains only entry-point documentation, workspace configuration, Firebase CLI configuration, hidden local/tool state, and the seven owned directories.

## Framework migration

- Created the root npm workspace and `@habitiq/web` package.
- Preserved root web commands by forwarding them to the workspace.
- Migrated deprecated `middleware.ts` to Next.js 16 `apps/web/proxy.ts` with the same canonical-host redirect behavior.
- Updated Next.js and its ESLint configuration to 16.3.8, and reconciled Firebase dependencies without forced breaking upgrades.
- Updated the npm lockfile for the workspace.
- Moved the real local web environment file to `apps/web/.env.local`; it remains ignored.

## Security cleanup

- Deleted tracked `.env.prod.check` and `.env.vercel.check` snapshots. They contained a Vercel OIDC token field and must not return to source control.
- Added `.env*.check` to ignore rules.
- Verified local Android keystores, signing properties, local properties, service-config backups, and APK binaries are ignored.
- Existing token values may remain in Git history; rotate/revoke any previously valid Vercel OIDC token.

## Verification

| Check | Result |
|---|---|
| Root npm workspace install and lock reconciliation | Passed |
| Dependency security audit | Passed; 0 known vulnerabilities |
| Full web ESLint | Passed; 0 errors and 0 warnings |
| Web TypeScript `--noEmit` | Passed |
| Next.js 16.3.8 production build from root | Passed; 25 routes generated |
| Web Playwright mock-mode suite | Passed; 66 tests across desktop and mobile |
| Next.js proxy migration | Passed; middleware deprecation warning removed |
| Android `testDebugUnitTest` | Passed; 33 tests, 0 failures/errors/skips |
| Android `lintDebug` | Passed |
| Android Kotlin compilation with all warnings enabled | Passed; 0 warnings |
| Android `assembleDebug` | Passed |
| Firebase config JSON and referenced rule/index files | Passed |
| Current-document relative-link check | Passed; 0 missing links |
| Git whitespace check | Passed |

Generated build/cache output was removed again after verification.

## Remaining release gates

- The Android client intentionally preserves the existing shared FCM-token contract. Its legacy callback is locally documented and warning-scoped until web/backend consumers can be migrated together to Firebase Installation IDs.
- No live deployment, Firebase rule deployment, connected-device test, signed Android release, or iOS build was performed.
- Vercel Root Directory and Netlify Package Directory must be set to `apps/web` in their external dashboards.

## Preservation

All active web and Android source files were moved, not rewritten for organization. Pre/post file counts for the web source directories matched exactly. Android tests, lint, and assembly confirmed that the relocated Gradle project remains functional.
