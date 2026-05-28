# Habitiq 🏠

> **Shared living, perfectly balanced.**

Habitiq is a smart household duty management app built for flatmates who want a fair, automated, and transparent chore rotation system — no arguments, no forgotten tasks, no excuses.

[![Live](https://img.shields.io/badge/Live-flatsflow.netlify.app-brightgreen)](https://flatsflow.netlify.app)
[![Next.js](https://img.shields.io/badge/Next.js-16-black?logo=next.js)](https://nextjs.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue?logo=typescript)](https://www.typescriptlang.org)
[![Firebase](https://img.shields.io/badge/Firebase-12-orange?logo=firebase)](https://firebase.google.com)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-v4-cyan?logo=tailwind-css)](https://tailwindcss.com)

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🔄 **Smart Rotation** | Auto-assigns duties fairly. Skips absent flatmates, resumes when they return. |
| ⚠️ **Overdue Accountability** | Overdue tasks stay with the responsible person — next person always gets a fresh full cycle. |
| 🔔 **Live Notifications** | Real-time toasts when any flatmate completes a task. |
| 📅 **Calendar Tracking** | Visual history of every completion, filterable by member and month. |
| 🔁 **Swap Requests** | Ask a teammate to cover your duty — accept or decline in-app. |
| 📊 **Analytics** | Monthly completion grids, reliability scores, and per-member breakdowns. |
| 🌙 **Dark Mode** | Full dark/light theme with system preference detection. |
| 📱 **Mobile-first** | Responsive bottom-nav layout optimised for phones. |
| 🔐 **Secure** | Role-based Firestore rules, hardened auth, HTTP security headers. |

---

## 🚀 Getting Started

### Prerequisites
- Node.js 18+
- A Firebase project (see [FIREBASE_SETUP.md](./FIREBASE_SETUP.md))

### Installation

```bash
git clone https://github.com/saijaswanthedupuganti-cmyk/flatflow.git
cd flatflow   # repo name pending rename to habitiq
npm install
```

### Configure Environment

```bash
cp .env.local.example .env.local
# Fill in your Firebase values in .env.local
```

### Run Development Server

```bash
npm run dev
# Open http://localhost:3000
```

> **No Firebase yet?** The app runs in **Local Mock Mode** automatically when Firebase keys are missing — explore all features with seeded demo data.

---

## 🗂️ Project Structure

```
flatflow/
├── app/
│   ├── (auth)/           # Login, Join pages
│   ├── dashboard/        # Main app (layout, home, analytics, calendar, tasks, members, settings, about)
│   └── onboarding/       # Create or join a flat
├── components/
│   ├── ui/               # shadcn/ui base components
│   ├── AuthProvider.tsx  # Central routing guard
│   └── NotificationToast.tsx
├── lib/
│   ├── firebase.ts       # Firebase init (with mock fallback)
│   ├── flatService.ts    # Create/join flat helpers
│   └── rotationEngine.ts # Smart duty rotation logic
└── store/
    ├── useAuthStore.ts   # Auth + flat membership (Zustand + persist)
    └── useFlatStore.ts   # Tasks, members, activity, swap requests
```

---

## 🔧 Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Next.js 16 (App Router, Turbopack) |
| Language | TypeScript (strict) |
| Styling | Tailwind CSS v4 + CSS variables |
| State | Zustand v5 with localStorage persistence |
| Backend | Firebase Auth + Firestore (real-time) |
| Hosting | Netlify (with Next.js plugin) |
| Animations | Framer Motion |
| Icons | Lucide React |
| Components | shadcn/ui |

---

## 🔐 Security

- Firestore rules: role-based access (admin vs member), no anonymous reads
- HTTP security headers: X-Frame-Options, HSTS, CSP, Referrer-Policy
- Google OAuth: custom `authDomain` proxied through Netlify (fixes iOS Safari)
- Auth errors: sanitised to prevent user enumeration
- IDs: `crypto.randomUUID()` throughout

See [SECURITY.md](./SECURITY.md) and [SECURITY_AUDIT.md](./SECURITY_AUDIT.md).

---

## 📋 Documentation

| Document | Description |
|----------|-------------|
| [PRODUCT.md](./PRODUCT.md) | Business roadmap, trial metrics, expansion plan |
| [FIREBASE_SETUP.md](./FIREBASE_SETUP.md) | How to connect your own Firebase project |
| [SECURITY.md](./SECURITY.md) | Security model and key protection |
| [SECURITY_AUDIT.md](./SECURITY_AUDIT.md) | Full audit findings and fixes |

---

## 👥 Team

| Name | Role |
|------|------|
| [Venkata Sai Jaswanth E](https://www.linkedin.com/in/venkata-sai-jaswanth-e/) | UI/UX Designer & Co-founder |
| [Upputuri Bhanu Kalyan](https://www.linkedin.com/in/upputuri-bhanu-kalyan/) | Full-Stack Developer & Co-founder |

---

## 📄 License

MIT — feel free to fork and build your own flat management system.
