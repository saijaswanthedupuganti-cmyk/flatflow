# Repository structure standard

Habitiq uses a multi-platform monorepo organized by responsibility.

## Rules

1. Deployable clients live under `apps/<platform>`.
2. Shared backend policy and infrastructure live under `backend/`.
3. Human documentation lives under `docs/`; the root contains only entry points and workspace configuration.
4. Generated binaries and exports live under `artifacts/` and are ignored unless a small manifest or report is intentionally tracked.
5. Editable cross-platform source assets live under `assets/`.
6. Repeatable automation lives under `tools/`; one-off historical patches live under `tools/archive/`.
7. Secrets and machine-local files never enter Git.

## Application boundaries

`apps/web` and `apps/android` are independently buildable clients. `apps/ios` becomes independently buildable only after a real Xcode project is created. Shared behavior crosses clients through documented Firebase contracts, not imports between platform UI trees.

## Adding a new file

| File type | Destination |
|---|---|
| Web route/component/test | `apps/web/` |
| Android Kotlin/resource/test | `apps/android/` |
| Future Swift/Xcode resource | `apps/ios/` |
| Firestore/Storage rule or index | `backend/firebase/` |
| Product specification | `docs/product/<area>/` |
| Architecture decision | `docs/architecture/` or a dated delivery packet |
| Current verification evidence | `docs/delivery/<date>/` |
| Superseded plan/evidence | `docs/archive/<category>/<date>/` |
| Local APK | `artifacts/android/apk/candidates/` or `legacy/` |
| Editable brand source | `assets/brand/` |

Avoid vague folders such as `project_1`, `misc`, `new`, or `final`. Use the subject, platform, and date.

## Changes that require coordinated verification

- Moving an application root.
- Changing shared Firebase fields or rules.
- Changing package/application IDs, auth domains, redirects, or deep links.
- Moving deployment configuration.
- Renaming a release artifact.

For these changes, update paths, commands, deployment settings, and documentation in the same change set.
