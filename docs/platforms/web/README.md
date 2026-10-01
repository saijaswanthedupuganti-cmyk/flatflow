# Web platform

The web product is the npm workspace at [`../../../apps/web/`](../../../apps/web/).

| Area | Location |
|---|---|
| Routes and layouts | `apps/web/app/` |
| Components | `apps/web/components/` |
| Data and browser logic | `apps/web/lib/`, `store/`, `contexts/`, `hooks/` |
| Static assets | `apps/web/public/` |
| End-to-end tests | `apps/web/tests/` |
| Framework configuration | `apps/web/next.config.ts`, `proxy.ts`, `tsconfig.json` |

Run workspace commands from the repository root. Before changing Next.js code, read the relevant version-matched guide resolved from the installed `next` package under `node_modules/next/dist/docs/`.

Deployment projects must use `apps/web` as their package/root directory. See [`../../operations/DEPLOYMENT.md`](../../operations/DEPLOYMENT.md).
