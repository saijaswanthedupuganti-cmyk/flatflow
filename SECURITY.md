# Security Policy

## Firebase API Keys

Habitiq uses Firebase for authentication and real-time data. The `NEXT_PUBLIC_FIREBASE_*` environment variables are **client-side keys** — they are intentionally exposed to the browser by Firebase's own design.

However, they are **protected by**:

1. **Firestore Security Rules** (`firestore.rules`) — only authenticated members of a flat can read/write its data. No anonymous access.
2. **Firebase Auth** — Google sign-in required before any data is accessible.
3. **Domain restrictions** — Add your production domain to Firebase Console → Authentication → Authorized Domains so keys only work from your domain.

## What is NOT in this repository

- `.env.local` — your real Firebase credentials. This file is in `.gitignore` and will never be committed.

## Setting Up Your Own Instance

See [`FIREBASE_SETUP.md`](./FIREBASE_SETUP.md) to connect Habitiq to your own Firebase project.

## Reporting a Vulnerability

If you find a security issue, please open a private GitHub issue or email the maintainers directly (see the About page in the app).
