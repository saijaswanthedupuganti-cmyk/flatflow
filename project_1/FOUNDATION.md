# Habitiq — Foundation (Scale Basement)

> **Status:** Locked — Aug 2026  
> **Owner:** Sai (founder) · AI co-pilot maintains this with the vault  
> **Purpose:** One basement for large-scale building. If this is wrong, future work forks and bugs multiply.

Read this after [[Habitiq — Project Documentation]]. For which file to open for what, see [[DOC_MAP]].

---

## 1. Non-negotiable decisions

| Decision | Rule |
|----------|------|
| **Web is the product brain** | `C:\garbage` (habitiq.app) invents features, business logic, schema, and UX rules. Forever. |
| **One Firebase project** | **`garbage-f79f7` only** (`C:\garbage`). Google for Startups / enterprise subscription lives here. Web, PWA, Android, and future iOS share Auth + Firestore + `firestore.rules`. **`habitiq-by-jaswanth` is abandoned** — never ship an APK or client against it. |
| **No second source of truth** | Native apps **port** from web `lib/*` and vault conditions. They do not invent new rotation, expense, or permission logic. |
| **PWA is the phone bridge** | Users install Habitiq today without stores. Push/polish may improve PWA; PWA does not replace native store strategy. |
| **Android before iOS** | Play Store first (India). iOS only after Android v1 is stable with real users. |
| **Android stack** | Kotlin + Jetpack Compose at `C:\habitiq_jaswanth` (docs under `C:\garbage\android`). Wired to **`garbage-f79f7`** via `google-services.json`. `C:\Users\user\Downloads\habitiq` is a **UX/feature sample only** (Flat Board, map search, tasks UI) — port from it, never its Firebase. Expo/RN abandoned. |
| **Online-only for now** | No offline edit queues. Graceful failure when offline is required; fake success is forbidden. |
| **Live users** | No experimental refactors on production paths without a clear reason. Test before shipping. |

---

## 2. System map

```
┌─────────────────────────────────────────────────────────┐
│  WEB  C:\garbage  →  habitiq.app                        │
│  Next.js 16 · Zustand · lib/* business logic            │
│  SOURCE OF TRUTH                                        │
└──────────────────────────┬──────────────────────────────┘
                           │ same Firebase
           ┌───────────────┼───────────────┐
           ▼               ▼               ▼
     PWA (browser)   Android native     iOS (later)
     manifest+SW     Kotlin Compose     after Android
     install today   Play Store         App Store
```

**Porting order (Android):** Auth/Flats/Members → Tasks/Rotation → Expenses → Settlements/Bills → Discover → Rewards.

**Parity references:** `project_1/CONDITIONS.md` (what happens) · `project_1/PERMISSIONS.md` (who can) · web `lib/*.ts` (implementation).

---

## 3. Repo structure (keep it this clean)

```
C:\garbage\                          ← WEB MONOREPO ROOT (canonical product)
├── app/                             Next.js App Router (pages)
├── components/                      UI components
├── contexts/                        React contexts (PWA, etc.)
├── hooks/                           React hooks
├── lib/                             BUSINESS LOGIC — port from here
├── store/                           Zustand stores
├── public/                          Static + PWA assets (sw.js, icons)
├── design-system/                   Design architecture notes
├── android/                         Native Android docs (+ mirror of Kotlin work)
├── project_1/                       Obsidian vault — product memory
│   ├── Habitiq — Project Documentation.md   CANONICAL
│   ├── FOUNDATION.md                        THIS FILE
│   ├── DOC_MAP.md                           Doc index
│   ├── CONDITIONS.md · PERMISSIONS.md
│   ├── voice/                               Voice specs
│   ├── archive/                             Old session dumps
│   └── design-analysis/                     UI batch notes
├── tests/ · __tests__/              Automated tests
├── firestore.rules                  Security — critical
├── DESIGN.md                        Design tokens / UI spec
├── UI_PLAN.md                       UI implementation roadmap
├── README.md · CLAUDE.md · AGENTS.md
└── package.json · next.config.ts · middleware.ts
```

**Separate checkouts:**  
`C:\habitiq_jaswanth` — native Android implementation (target: `garbage-f79f7`).  
`C:\Users\user\Downloads\habitiq` — feature prototype sample only; **not** a Firebase or ship target.

**Aug 8, 2026 (Sai):** Feature UX source = `Downloads\habitiq` (Flat Board, map search, native screens). Firebase = `garbage-f79f7` only (Google Startups). Target = real native APK with sample features on `garbage-f79f7` — not a WebView of habitiq.app, not `habitiq-by-jaswanth`.

---

## 4. What belongs where

| Kind of work | Put it here |
|--------------|-------------|
| New product feature | Web first (`app/`, `components/`, `lib/`, `store/`) |
| Business rule / edge case | Code in `lib/` + update `CONDITIONS.md` / `PERMISSIONS.md` |
| Design decision | `DESIGN.md` + vault if strategic |
| Session / dump notes | `project_1/archive/` — never leave at repo root |
| Voice specs | `project_1/voice/` |
| Android port | `C:\habitiq_jaswanth` + update `android/docs` when architecture changes |
| One-off scripts, logs, inspect screenshots | **Delete** — never commit |

---

## 5. Documentation hierarchy (anti-chaos)

1. **Canonical product memory:** `project_1/Habitiq — Project Documentation.md`  
2. **Scale basement:** `project_1/FOUNDATION.md` (this file)  
3. **Doc index:** `project_1/DOC_MAP.md`  
4. **Logic contracts:** `CONDITIONS.md` · `PERMISSIONS.md`  
5. **Design:** `DESIGN.md` · `UI_PLAN.md` · `design-system/`  
6. **Android:** `android/docs/superpowers/` + `C:\habitiq_jaswanth\docs`  

Root files like `PRODUCT.md`, `HABITIQ_MASTER_DOCUMENT.md`, `FLATFLOW_LAUNCH_DOCUMENTATION.md` are **legacy snapshots**. Do not update them for new work — update the vault canonical doc instead. See DOC_MAP for status of each.

---

## 6. Engineering rules for scale

1. **Schema lock** — Firestore field names/types match across clients. Native never adds silent fields the web does not understand.  
2. **Rules lock** — Authorization lives in `firestore.rules`, not only in UI.  
3. **Rotation lock** — `lib/rotationEngine.ts` is sacred; port bit-for-bit conditions.  
4. **One voice instance** — Voice assistant is lifted in dashboard layout; do not remount per page.  
5. **Mock mode** — App must keep working without Firebase keys for local verification.  
6. **No third mobile codebase** — Web + Android only until Android proves store readiness.  
7. **Root hygiene** — No patch scripts, logs, or random HTML dumps at repo root.  
8. **Vault end-of-session** — Significant changes update `Habitiq — Project Documentation.md`.  
9. **Firebase client config vs real secrets** — `android/app/google-services.json` (and the sibling at `C:\habitiq_jaswanth\app\google-services.json`) contains Firebase *client* API keys. Committing that file is normal for Android Firebase builds; GitHub secret scanning may flag it — treat as expected client config, not a leaked server secret. Restrict those keys in Google Cloud Console (Android app restriction + package/SHA-1, API restrictions to Firebase/needed Google APIs). **Never** commit service-account JSON, Admin SDK keys, or other server secrets. Do not gitignore `google-services.json` unless CI/local already injects it another way (this project does not).

---

## 7. Mobile path (summary)

| Phase | Goal | Ship signal |
|-------|------|-------------|
| 0 Web | Product truth | Live flats on habitiq.app |
| 1 PWA | Phone without stores | Install + usable standalone |
| 1b APK (bridge) | Full product on Android today | `releases/Habitiq-0.2.0-web-debug.apk` — WebView of habitiq.app on `garbage-f79f7` (temporary) |
| 2 Native APK | Real app with sample features | Kotlin on `garbage-f79f7`; port Flat Board etc. from Downloads sample |
| 3 Play Store | Store trust + discovery | Native parity + approved listing |
| 4 App Store | iOS after proof | After Android v1 stable |

**Current APK strategy (Aug 2026):** **Firebase = `garbage-f79f7` only.** WebView APK is a temporary bridge on the correct Firebase track. The real ship target is a **native app** at `C:\habitiq_jaswanth` (or `C:\garbage\android`) with features ported from the Downloads sample (Flat Board, map search, tasks UI) onto web schema (`seekerProfiles`, `discoveryTags`). Do **not** ship anything wired to `habitiq-by-jaswanth`.

**Skip for v1 brand:** Expo restart, iOS-before-Android, three live product codebases.

**PWA next (when needed):** stronger iOS install guide, FCM push, graceful offline messaging — not a rewrite.

---

## 8. Ready-for-scale checklist

- [x] Web live at habitiq.app with real users  
- [x] PWA manifest + service worker + install UX  
- [x] Firestore rules as shared authz gate  
- [x] CONDITIONS / PERMISSIONS extracted for ports  
- [x] Android foundation (auth + flats) started  
- [x] This FOUNDATION + DOC_MAP locked  
- [ ] Android Tasks/rotation parity  
- [ ] Play Console + keystore + listing assets  
- [ ] Push notifications (web/PWA first or with Android)  
- [ ] Root/legacy doc drift eliminated over time (prefer vault only)

---

## 9. How future sessions start

1. Read `FOUNDATION.md` (this file)  
2. Read `Habitiq — Project Documentation.md`  
3. Read `DOC_MAP.md` only if unsure which file to open  
4. For logic changes: `CONDITIONS.md` + relevant `lib/*`  
5. For Android: port from web; never invent  
6. End: update vault if the decision or feature is significant  

---

*"Web invents. PWA distributes early. Android proves the store. iOS follows proof."*
