# Deployment configuration

## Web

The deployable Next.js package is `apps/web`.

- **Vercel:** set Project Settings → Root Directory to `apps/web`. Keep the default Next.js build/output settings and configure environment variables in the project dashboard.
- **Netlify:** set Package Directory to `apps/web`; the application-specific `netlify.toml` is stored beside the app.

Changing files in the repository does not update those external dashboard settings automatically.

## Firebase

Run Firebase CLI commands from the repository root. [`../../firebase.json`](../../firebase.json) points to rules and indexes under `backend/firebase/`.

Review diffs and validate rules before deployment. Do not deploy as part of repository organization unless explicitly requested.

## Android

Builds run from `apps/android`. Keystores, `keystore.properties`, and `local.properties` are machine-local. Candidate APKs belong under `artifacts/android/apk/candidates/` and require a manifest entry before sharing.
