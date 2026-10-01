# Habitiq web

Next.js 16 App Router client for Habitiq.

## Run from the monorepo root

```powershell
npm install
npm run dev
npm run lint
npm run build
npm test
```

The root commands forward to this npm workspace. Local environment values belong in `apps/web/.env.local`; copy `.env.local.example` and never commit real values.

Before editing Next.js code, read the relevant version-matched guide under the installed `next/dist/docs/` package as required by the root `AGENTS.md`.
