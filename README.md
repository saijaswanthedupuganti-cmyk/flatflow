# Habitiq

> **Smart living, managed.**

Habitiq is an intelligent shared living management platform for Indian flatmates. It automates duty rotation, tracks expenses, manages bill splitting, and handles swap requests — so no one has to argue about whose turn it is.

[![Live](https://img.shields.io/badge/Live-habitiq.app-brightgreen)](https://habitiq.app)
[![Next.js](https://img.shields.io/badge/Next.js-16-black?logo=next.js)](https://nextjs.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue?logo=typescript)](https://www.typescriptlang.org)
[![Firebase](https://img.shields.io/badge/Firebase-Firestore-orange?logo=firebase)](https://firebase.google.com)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-v4-cyan?logo=tailwind-css)](https://tailwindcss.com)

---

## What It Does

| Module | Description |
|--------|-------------|
| **Rotation Engine** | Auto-assigns duties fairly. Skips out-of-station members, resumes when they return. Deterministic — every device calculates the same next assignee. |
| **Swap System** | Request a flatmate to cover your task. Accept or decline in-app. Full stat tracking per user. |
| **Expense Splits** | Ad-hoc expense log (like Splitwise) — equal or custom split, 7 currencies, banking-style UI. |
| **Monthly Bills** | Rotating payer for rent, WiFi, electricity. Separate collector role. Per-member collection tracking. |
| **Balances & Settlement** | Direct pairwise balance calculation. Settle or mark-received from either side. Month-end close with carry-forward. |
| **Analytics** | Task completion grid, reliability scores, per-member breakdowns. |
| **Multi-Flat** | Users can belong to multiple flats. Gmail-style FlatSwitcher. Instant context switch. |
| **Membership Management** | Leave flat, kick member, transfer admin, flat deletion — all with correct task reassignment. |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Next.js 16 (React 19, App Router) |
| Build | Turbopack |
| Language | TypeScript (strict) |
| Styling | Tailwind CSS v4 + CSS variables |
| State | Zustand v5 + localStorage persistence |
| Database | Firebase Firestore (real-time `onSnapshot`) |
| Auth | Firebase Auth (Google + email/password) |
| Hosting | Vercel (auto-deploy on push to master) |
| Auth Proxy | Next.js rewrites — fixes Google OAuth on iOS Safari |
| PWA | Web App Manifest + Service Worker — installs without App Store |
| Animations | Framer Motion |
| Icons | Lucide React |
| Components | shadcn/ui |

---

## Getting Started

### Prerequisites
- Node.js 18+
- A Firebase project (see [FIREBASE_SETUP.md](./FIREBASE_SETUP.md))

### Install

```bash
git clone https://github.com/saijaswanthedupuganti-cmyk/flatflow.git
cd flatflow
npm install
```

### Configure Environment

```bash
cp .env.local.example .env.local
# Fill in your Firebase config values
```

### Run

```bash
npm run dev
# http://localhost:3000
```

> **No Firebase?** The app runs in **Mock Mode** automatically when Firebase keys are absent — all features work with seeded demo data, nothing writes to a real database.

---

## Project Structure

```
flatflow/
├── app/
│   ├── (auth)/login/         — Login (Google + email)
│   ├── (auth)/join/          — Join flat page
│   ├── dashboard/
│   │   ├── layout.tsx        — Shell: sidebar (desktop) + bottom nav (mobile)
│   │   ├── page.tsx          — Home: My Tasks + Org View + swap widget
│   │   ├── expenses/         — Monthly Bills + Daily Splits + Balances + Settle
│   │   ├── analytics/        — Completion grid + reliability scores
│   │   ├── calendar/         — Monthly task calendar
│   │   ├── tasks/            — Task management (admin create/edit) + member read-only view
│   │   ├── members/          — Member list + management
│   │   ├── swaps/            — Swap requests: stat chips + admin All Swaps view
│   │   ├── activity/         — Real-time flat activity log
│   │   ├── profile/          — User profile + leave flat + Danger Zone
│   │   └── settings/         — App settings + legal links
│   ├── onboarding/           — Create or join a flat
│   ├── privacy/              — Privacy Policy (DPDP Act 2023 compliant)
│   └── terms/                — Terms of Service
├── components/
│   ├── AuthProvider.tsx      — Central routing guard + public route bypass
│   ├── FlatSwitcher.tsx      — Multi-flat dropdown (Gmail-style)
│   ├── GoingOutModal.tsx     — Out-of-station toggle
│   ├── NotificationToast.tsx — Real-time completion toasts
│   ├── NPSBanner.tsx         — NPS survey (once per user)
│   └── TrustTagCard.tsx      — Discovery trust tag (feature-flagged)
├── lib/
│   ├── firebase.ts           — Firebase init + mock fallback
│   ├── flatService.ts        — Create/join/leave/delete flat; kick member; transfer admin
│   ├── rotationEngine.ts     — Deterministic rotation algorithm
│   ├── expenseUtils.ts       — Direct pairwise balance computation
│   ├── settlementUtils.ts    — Month-end summary + carry-forward
│   ├── couponService.ts      — Coupon validation + redemption
│   └── npsService.ts         — NPS logic
├── store/
│   ├── useAuthStore.ts       — Auth state + flat membership + flat switching
│   └── useFlatStore.ts       — Tasks, members, swaps, expenses, bills, settlements, subscription
├── hooks/
│   └── useSubscription.ts    — Trial / active / expired subscription state
├── firestore.rules           — Role-based security (admin vs member; payer; collector)
├── next.config.ts            — HTTP security headers + OAuth proxy rewrites
└── PRODUCT.md                — Business roadmap + full feature set
```

---

## Security

Three audits completed — 18 vulnerabilities found and fixed before and after launch.

| Area | Status |
|------|--------|
| Firestore rules | Role-based: admin / member / payer / collector scopes |
| HTTP headers | X-Frame-Options, HSTS, X-Content-Type-Options, Referrer-Policy, Permissions-Policy, X-XSS-Protection |
| Auth errors | Sanitised — no Firebase codes exposed to users |
| ID generation | `crypto.randomUUID()` + `crypto.getRandomValues()` throughout |
| Atomic writes | Kick + counter decrement in single `writeBatch`; flat deletion cleans all subcollections |
| Activity log | Firestore rule validates `userId == request.auth.uid` — audit trail cannot be spoofed |
| Input limits | All text inputs have `maxLength` constraints |
| OAuth (iOS Safari) | Custom `authDomain` via Next.js rewrites bypasses third-party cookie restriction |

See [SECURITY_AUDIT.md](./SECURITY_AUDIT.md) for full findings.

---

## Feature Flags

| Flag | Effect |
|------|--------|
| `NEXT_PUBLIC_DISCOVERY_ENABLED=true` | Enables Flat Board (find-members) and Trust Tag Card on Profile |

---

## Documentation

| File | Contents |
|------|---------|
| [PRODUCT.md](./PRODUCT.md) | Full feature set, business model, roadmap, market opportunity |
| [FIREBASE_SETUP.md](./FIREBASE_SETUP.md) | How to connect your own Firebase project |
| [SECURITY_AUDIT.md](./SECURITY_AUDIT.md) | All audit findings and fixes |
| [SECURITY.md](./SECURITY.md) | Security model overview |
| [HABITIQ_COFOUNDER_BRIEF.md](./HABITIQ_COFOUNDER_BRIEF.md) | Co-founder onboarding brief |
| [project_1/](./project_1/) | Obsidian vault — full product + strategic documentation |

---

## Team

| Name | Role |
|------|------|
| [Venkata Sai Jaswanth E](https://www.linkedin.com/in/venkata-sai-jaswanth-e/) | Product & UI/UX — Co-founder |
| [Upputuri Bhanu Kalyan](https://www.linkedin.com/in/upputuri-bhanu-kalyan/) | Full-Stack Engineering — Co-founder |

---

## Contact

hello@habitiq.app

---

## License

MIT
