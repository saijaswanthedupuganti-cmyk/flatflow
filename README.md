# Habitiq monorepo

Habitiq is a shared-living product covering household tasks, expenses, bills, flat membership, and room/roommate discovery.

This repository is organized as a multi-platform monorepo. Android and web are real applications. iOS is reserved for a future native SwiftUI application and is not yet implemented.

## Start here

1. Read the [project master](docs/MASTER.md).
2. Read the [repository structure](docs/architecture/REPOSITORY_STRUCTURE.md).
3. Open the relevant application README:
   - [Web](apps/web/README.md)
   - [Android](apps/android/README.md)
   - [iOS status](apps/ios/README.md)
4. Use the [latest execution evidence](docs/delivery/2026-09-26-implementation/execution-status.md) before making release claims.
5. Review the [repository reorganization record](docs/delivery/2026-10-01-repository-reorganization/README.md) for migrated paths, verification, and known issues.

## Repository structure

```text
habitiq/
├── apps/
│   ├── web/                 Next.js web application and Playwright tests
│   ├── android/             Native Kotlin/Jetpack Compose application
│   └── ios/                 Future native iOS placeholder only
├── backend/
│   └── firebase/            Firestore indexes/rules and Storage rules
├── docs/
│   ├── architecture/        Repository and cross-client architecture
│   ├── product/             Product behavior, flows, discovery, strategy
│   ├── platforms/           Platform-specific guides
│   ├── operations/          Firebase, security, deployment
│   ├── quality/             Test and release gates
│   ├── delivery/            Current dated implementation evidence
│   ├── design-system/       Design-system documents
│   └── archive/             Superseded and historical material
├── artifacts/
│   ├── android/             Local APK candidates, legacy builds, manifests
│   └── audits/              Exported audit documents
├── assets/brand/            Editable cross-platform brand sources
├── tools/archive/           Historical one-off maintenance scripts
├── firebase.json            Firebase CLI configuration
├── package.json             Workspace commands
└── package-lock.json        Single npm lockfile
```

## Common commands

Run from the repository root.

```powershell
npm install
npm run dev
npm run lint
npm run build
npm test
```

Android commands run from its application folder:

```powershell
cd apps/android
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

## Platform status

| Platform | Status | Canonical location |
|---|---|---|
| Web | Existing client; workspace build supported | `apps/web/` |
| Android | Primary native delivery target; debug candidate exists | `apps/android/` |
| iOS | Planned only; no Xcode project or signed build | `apps/ios/` |
| Firebase | Shared backend policy and data contracts | `backend/firebase/` |

## Non-negotiable rules

- Never commit `.env.local`, Android keystores, signing properties, local service backups, or production credentials.
- APK binaries are local artifacts; the tracked manifest records hashes and verification state.
- A successful build is not release approval. Device, backend, auth, offline, accessibility, signing, and installation checks are separate gates.
- Every shared-schema change must account for web, Android, and future iOS readers.
- Historical documents are context, not current truth.

Deployment owners must configure both Vercel’s Root Directory and Netlify’s Package Directory as `apps/web`. See the [deployment guide](docs/operations/DEPLOYMENT.md).
