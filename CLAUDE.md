@AGENTS.md

# Habitiq — Project Context for Claude Code

## START HERE — Read This Before Doing Anything

This project has a full documentation vault at `C:\garbage\project_1\`.

**Before starting any task on this project, read these two files:**

1. `C:\garbage\project_1\Habitiq — Project Documentation.md` — complete project record: what was built, what changed, every feature, the full roadmap, security audit, tech stack, Firestore schema, business model, and future requirements
2. `C:\garbage\project_1\About Sai.md` — who built this and why (Sai's background, the vibe coding method, the founding story)

Reading these two files gives you complete context. You will not need to re-read source files to understand the project.

## Quick Reference

| Thing | Value |
|-------|-------|
| Product name | Habitiq |
| Live URL | https://garbage-liart.vercel.app |
| Target domain | habitiq.in / habitiq.app |
| Stack | Next.js 16, TypeScript, Tailwind v4, Zustand, Firebase Firestore + Auth |
| Hosting | Vercel (Hobby, free) |
| Auth | Firebase Auth — Google Sign-In + email/password, custom authDomain proxy |
| Database | Firestore (real-time, onSnapshot listeners) |
| State | Zustand v5 + localStorage persistence |
| Current version | v0.1.0 — Trial Phase, live with real users |
| Obsidian vault | C:\garbage\project_1\ |

## Project Status (as of June 2026)

- All core features are live and working
- Security audit completed — 8 issues found and fixed before launch
- Currently in trial phase: validating with real flats
- Phase 2 features (push notifications, WhatsApp, PWA) are planned but not started
- No tests exist — this is a live production app, changes go directly to users

## Key Files to Know

| File | Purpose |
|------|---------|
| `lib/rotationEngine.ts` | Core rotation algorithm — the heart of the product |
| `firestore.rules` | Role-based security — always check this when touching data |
| `store/useAuthStore.ts` | Auth + flat membership state |
| `store/useFlatStore.ts` | Tasks, members, activity, swap requests |
| `components/AuthProvider.tsx` | Central routing guard — controls all navigation |
| `lib/firebase.ts` | Firebase init with mock fallback |
| `next.config.ts` | HTTP security headers + auth domain rewrite |

## Rules for Working on This Project

1. **Read the vault first.** `project_1/Habitiq — Project Documentation.md` has everything. Do not ask the user to explain the project.
2. **This is a live app with real users.** Test thoroughly before suggesting changes. No experimental refactors without asking.
3. **Firestore rules are security-critical.** Any change to `firestore.rules` must be reviewed carefully — the audit found 2 HIGH severity issues that were rule-level.
4. **Mock mode exists.** The app runs fully without Firebase keys using seeded data. Use this for local testing.
5. **Update the vault.** When a significant change is made (new feature, bug fix, architecture decision), update `project_1/Habitiq — Project Documentation.md` so the next session starts with accurate context.
