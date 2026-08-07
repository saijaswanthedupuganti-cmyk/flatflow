@AGENTS.md

# Habitiq — Project Context for Claude Code

## START HERE — Read This Before Doing Anything

This project has a full documentation vault at `C:\garbage\project_1\`.

**Before starting any task on this project, read these files in order:**

1. `C:\garbage\project_1\FOUNDATION.md` — scale basement: web-first rules, mobile path, repo structure, engineering locks
2. `C:\garbage\project_1\Habitiq — Project Documentation.md` — complete product record (features, schema, roadmap, history)
3. `C:\garbage\project_1\About Sai.md` — who built this and why
4. `C:\garbage\project_1\DOC_MAP.md` — which doc to open for what (skip if you already know)

Reading these gives complete context. Do not ask the user to re-explain the product.

## Quick Reference

| Thing | Value |
|-------|-------|
| Product name | Habitiq |
| Live URL | https://habitiq.app |
| Canonical domain | habitiq.app |
| Stack | Next.js 16, TypeScript, Tailwind v4, Zustand, Firebase Firestore + Auth |
| Hosting | Vercel |
| Auth | Firebase Auth — Google Sign-In + email/password, custom authDomain proxy |
| Database | Firestore (real-time, onSnapshot listeners) |
| State | Zustand v5 + localStorage persistence |
| PWA | Manifest + service worker — installable without stores |
| Android | Kotlin + Compose at `C:\habitiq_jaswanth` (docs under `android/`) |
| Current phase | Live trial + Android native in progress |
| Obsidian vault | `C:\garbage\project_1\` |

## Project Status (as of Aug 2026)

- Core web product live with real users
- PWA installable (Android + iOS Add to Home Screen)
- Security audits completed pre/post launch
- Voice, Flat Board, bills, expenses, multi-flat shipped on web
- Native Android (auth + flats) started — Tasks/rotation is next
- **Web remains source of truth for all future product logic**

## Key Files to Know

| File | Purpose |
|------|---------|
| `project_1/FOUNDATION.md` | Scale basement — read first |
| `lib/rotationEngine.ts` | Core rotation algorithm — the heart of the product |
| `firestore.rules` | Role-based security — always check this when touching data |
| `project_1/CONDITIONS.md` | Business-logic contracts for ports |
| `project_1/PERMISSIONS.md` | Who can trigger what |
| `store/useAuthStore.ts` | Auth + flat membership state |
| `store/useFlatStore.ts` | Tasks, members, activity, swap requests |
| `components/AuthProvider.tsx` | Central routing guard — controls all navigation |
| `lib/firebase.ts` | Firebase init with mock fallback |
| `next.config.ts` | HTTP security headers + auth domain rewrite |
| `DESIGN.md` | Design tokens / UI language |

## Rules for Working on This Project

1. **Read FOUNDATION + vault first.** Do not ask the user to explain the project.
2. **Web invents. Native ports.** Never invent a second business-logic source of truth.
3. **This is a live app with real users.** Test thoroughly. No experimental refactors without asking.
4. **Firestore rules are security-critical.** Review carefully; deploy rules after changes.
5. **Mock mode exists.** App runs without Firebase keys using seeded data — use it for local testing.
6. **Update the vault.** Significant changes update `Habitiq — Project Documentation.md` (and FOUNDATION if architecture decisions change).
7. **Keep the root clean.** No logs, patch scripts, inspect screenshots, or third-party clones at repo root.
