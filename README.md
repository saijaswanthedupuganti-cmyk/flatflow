# FlatFlow 🏠

> **Shared living, perfectly balanced.**

FlatFlow is a smart household duty management app built for flatmates who want a fair, automated, and transparent chore rotation system — no arguments, no forgotten tasks, no excuses.

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

---

## 🚀 Getting Started

### Prerequisites
- Node.js 18+
- A Firebase project (see [FIREBASE_SETUP.md](./FIREBASE_SETUP.md))

### Installation

```bash
git clone https://github.com/YOUR_USERNAME/flatflow.git
cd flatflow
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

> **No Firebase yet?** The app runs in **Local Mock Mode** automatically when Firebase keys are missing — you can explore all features with seeded demo data.

---

## 🗂️ Project Structure

```
flatflow/
├── app/
│   ├── (auth)/           # Login, Join pages
│   ├── dashboard/        # Main app pages (layout, home, analytics, calendar, tasks, members, settings, about)
│   └── onboarding/       # Create or join a flat
├── components/
│   ├── ui/               # shadcn/ui components
│   ├── AuthProvider.tsx  # Central routing guard
│   └── NotificationToast.tsx  # Real-time activity toasts
├── lib/
│   ├── firebase.ts       # Firebase init (with mock fallback)
│   ├── flatService.ts    # Create/join flat helpers
│   └── rotationEngine.ts # Smart duty rotation logic
└── store/
    ├── useAuthStore.ts   # Auth + flat membership state
    └── useFlatStore.ts   # Tasks, members, activity log
```

---

## 🔧 Tech Stack

- **Framework**: Next.js 16 (App Router, Turbopack)
- **Language**: TypeScript (strict)
- **Styling**: Tailwind CSS v4 with CSS variables
- **State**: Zustand v5 with persistence
- **Backend**: Firebase (Auth + Firestore real-time)
- **Animations**: Framer Motion
- **Icons**: Lucide React
- **Components**: shadcn/ui

---

## 🔐 Security

See [SECURITY.md](./SECURITY.md) for details on how Firebase keys are protected.

---

## 👥 Team

| Name | Role |
|------|------|
| [Venkata Sai Jaswanth E](https://www.linkedin.com/in/venkata-sai-jaswanth-e/) | UI/UX Designer & Co-founder |
| [Upputuri Bhanu Kalyan](https://www.linkedin.com/in/upputuri-bhanu-kalyan/) | Full-Stack Developer & Co-founder |

---

## 📄 License

MIT — feel free to fork and build your own flat management system.
