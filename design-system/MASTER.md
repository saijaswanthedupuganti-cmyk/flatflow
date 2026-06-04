# Habitiq — Brand & Design System Master

> **Version:** 1.1 | **Updated:** June 2026
> **Source of truth for all UI/UX decisions.** Read this before building any screen.

---

## 1. Brand Identity

| | |
|-|-|
| **Name** | Habitiq (Habit + IQ) |
| **Tagline** | Smart living, managed. |
| **Positioning** | Intelligent shared living management for India |
| **Brand voice** | Direct, calm, specific. Speaks like a flatmate. |
| **Target users** | Flatmates, PG residents, co-living spaces — India Tier 1/2 cities |

---

## 2. Color System

### 2a. Palette Philosophy

Habitiq uses a **balanced two-temperature palette** — designed to international standards, gender-neutral, and grounded in color psychology.

| Color | Role | Psychology |
|-------|------|-----------|
| **Indigo** `#4f46e5` | Primary — all interactive UI | Trust · depth · intelligence · fairness |
| **Violet** `#7c3aed` | Gradient end — decorative | Creativity · premium · uniqueness |
| **Orange** `#f97316` | Warm action accent — marketing CTAs | Energy · warmth · community · action |
| **Amber** `#f59e0b` | Status today + warm highlights | Urgency · attention · clarity |

**Why this works:** Indigo (cool, blue-leaning) + Orange (warm) is a classic complementary pair — psychologically it means "smart AND human." Used globally by Stripe, Linear, Vercel, GitHub Copilot. Universally recognized, gender-neutral, appropriate for 18–35 professionals.

**The brand gradient:** `#4f46e5 → #7c3aed` (indigo to violet)
Applied on: logo mark, hero CTA button, step numbers, key highlights. The gradient visually expresses the brand name — a system that is both blue-stable (Habit) and violet-intelligent (IQ).

---

### 2b. Brand Scale — Indigo Primitives

Tailwind utilities: `bg-brand-600`, `text-brand-400`, `border-brand-200`, etc.

| Token | Hex | Usage |
|-------|-----|-------|
| `brand-50`  | `#eef2ff` | Tinted light backgrounds, subtle fills |
| `brand-100` | `#e0e7ff` | Light badge backgrounds |
| `brand-200` | `#c7d2fe` | Borders in light mode |
| `brand-300` | `#a5b4fc` | Subtle text/icons on light |
| `brand-400` | `#818cf8` | Secondary text/icons on dark |
| `brand-500` | `#6366f1` | Hover surfaces, light icons |
| `brand-600` | `#4f46e5` | **Primary — all buttons, active states, CTAs** |
| `brand-700` | `#4338ca` | Hover on brand-600 |
| `brand-800` | `#3730a3` | Pressed/active dark |
| `brand-900` | `#312e81` | Text on brand-tinted backgrounds |
| `brand-950` | `#1e1b4b` | Deep brand, rarely used |

**Gradient end tokens:** `brand-end: #7c3aed` / `brand-end-dark: #4338ca`

---

### 2c. Warm Action Scale — Orange/Amber Primitives

Tailwind utilities: `bg-action-500`, `text-action-400`, `bg-warm-400`, etc.

| Token | Hex | Usage |
|-------|-----|-------|
| `action-400` | `#fb923c` | Light text on dark for orange elements |
| `action-500` | `#f97316` | Landing page CTAs, warm highlights |
| `action-600` | `#ea580c` | Hover on action-500 |
| `warm-300`   | `#fcd34d` | Subtle warm glow |
| `warm-400`   | `#fbbf24` | Warm badge |
| `warm-500`   | `#f59e0b` | **Status: due today** |
| `warm-600`   | `#d97706` | Hover on warm-500 |

---

### 2d. Semantic Tokens — Light Mode

Three-surface system: page canvas → card → input fill.
Cards sit on an off-white canvas — removes harsh reflection, creates professional depth.

| CSS Variable | Value | Usage |
|---|---|---|
| `--background` | `#f5f6fa` | **Page canvas — soft off-white, non-reflective** |
| `--foreground` | `#0f172a` | Primary text |
| `--card` | `#ffffff` | Cards, sidebar — pure white lifts off page bg |
| `--primary` | `#4f46e5` | **Indigo — all interactive elements** |
| `--primary-foreground` | `#ffffff` | Text on primary bg |
| `--secondary` | `#ecedf4` | Hover fills, chip backgrounds |
| `--muted` | `#ecedf4` | Disabled, empty states |
| `--muted-foreground` | `#5e6a82` | Subtext, captions (4.7:1 contrast on white) |
| `--accent` | `#eef2ff` | Indigo-tinted fills |
| `--accent-foreground` | `#312e81` | Text on accent bg |
| `--border` | `#e1e3ee` | All borders — brand-harmonious |
| `--ring` | `#4f46e5` | Focus rings — indigo |
| `--destructive` | `#ef4444` | Error, delete, danger |

**Why this works:**
- `#f5f6fa` is ~12 points below pure white on each channel — barely perceptible but eliminates glare
- `#ffffff` cards on `#f5f6fa` page = the same depth system used by Stripe, GitHub, Linear
- `#ecedf4` secondary is cooler than the old slate-grey, harmonises with the indigo brand

---

### 2e. Semantic Tokens — Dark Mode

Deep-dark aesthetic — `#0a0a0a` base, matching the landing page visual language.

| CSS Variable | Value | Usage |
|---|---|---|
| `--background` | `#0a0a0a` | Page background (true dark) |
| `--foreground` | `#f4f4f5` | Primary text |
| `--card` | `#111111` | Card/sidebar surfaces |
| `--primary` | `#4f46e5` | **Same indigo — consistent across modes** |
| `--secondary` | `#1a1a1a` | Elevated surfaces, hover fills |
| `--muted` | `#1a1a1a` | Disabled/empty states |
| `--muted-foreground` | `#a1a1aa` | Subtext, captions |
| `--accent` | `#0f0d2e` | Indigo-tinted dark fill |
| `--accent-foreground` | `#818cf8` | Text on dark accent bg |
| `--border` | `#242424` | Subtle borders |
| `--ring` | `#4f46e5` | Focus rings |
| `--destructive` | `#7f1d1d` | Muted red for dark mode |
| `--destructive-foreground` | `#fca5a5` | Text on dark destructive |

---

### 2f. Dark Surface Scale

For layering depth in dark mode. Use instead of guessing hex values.

| Token | Hex | Typical use |
|-------|-----|-------------|
| `surface-0` | `#0a0a0a` | Page background (deepest) |
| `surface-1` | `#0f0f0f` | Slightly elevated sections |
| `surface-2` | `#141414` | Topbars, app headers |
| `surface-3` | `#1a1a1a` | Hover fills, secondary cards |
| `surface-4` | `#242424` | Borders, dividers |
| `surface-5` | `#3a3a3a` | Prominent borders, input strokes |

---

### 2g. Status Colors — Task States

| State | Color | Hex | Tailwind built-in |
|-------|-------|-----|-------------------|
| Overdue | Red | `#ef4444` | `red-500` |
| Due today | Amber | `#f59e0b` | `amber-500` |
| Due soon | Indigo | `#4f46e5` | `indigo-600` |
| Done | Emerald | `#10b981` | `emerald-500` |

**Status card pattern:**
```
border-l-[3px] border-l-red-500    bg-red-500/5    rounded-xl  ← overdue
border-l-[3px] border-l-amber-400  bg-amber-400/5              ← today
border-l-[3px] border-l-indigo-500 bg-indigo-500/5             ← soon
border-l-[3px] border-l-emerald-500 bg-emerald-500/5           ← done
```

**Status badge pattern:**
```
text-[10px] font-bold px-2 py-1 rounded-full
text-red-400    bg-red-500/10     ← overdue
text-amber-400  bg-amber-400/10   ← today
text-indigo-400 bg-indigo-500/10  ← soon
text-emerald-400 bg-emerald-500/10 ← done
```

---

## 3. Typography

### 3a. Font Stack

| Role | Font | Variable | Scope |
|------|------|----------|-------|
| **UI / Body** | Inter | `--font-sans` | Dashboard, app, forms, all data UI |
| **Mono** | Geist Mono | `--font-mono` | Invite codes, IDs, code snippets |
| **Marketing / Display** | Plus Jakarta Sans | inline import | Landing page headings only |

**Rule:** Never mix PJS and Inter on the same screen. PJS = marketing. Inter = product.

### 3b. Type Scale

| Name | Size | Use |
|------|------|-----|
| `text-xs` | 12px | Overline labels, badges, timestamps |
| `text-sm` | 14px | Body, nav items, secondary copy |
| `text-base` | 16px | Default body — minimum for mobile |
| `text-lg` | 18px | Card headings, section labels |
| `text-xl` | 20px | Page titles |
| `text-2xl` | 24px | Section headings (dashboard) |
| `text-3xl+` | 30px+ | Landing page only (PJS font) |

### 3c. Text Hierarchy on Dark Backgrounds

| Role | Approach | Opacity |
|------|----------|---------|
| Primary text | `text-foreground` | — |
| Secondary | `text-muted-foreground` | — |
| Caption / meta | `text-white/35` | 35% |
| Overline label | `text-white/25` | 25% |
| Disabled | `text-white/15` | 15% |

**Contrast rule:** Body text minimum 4.5:1. Caption/decorative minimum 3:1.

---

## 4. Spacing & Layout

### 4a. Radius Scale

| Token | Value | Use |
|-------|-------|-----|
| `rounded-sm` | 4px | Very small chips, minimal elements |
| `rounded-md` | 10px | Inline badges, tags |
| `rounded-lg` | 12px | Inputs, small buttons |
| `rounded-xl` | 16px | **Default — all cards, nav items, buttons** |
| `rounded-2xl` | 20px | Large cards, modals, feature panels |
| `rounded-full` | 9999px | Avatars, pill badges, toggles |

### 4b. Layout Breakpoints

| Breakpoint | Width | Layout |
|------------|-------|--------|
| Mobile | < 768px | Bottom nav, stacked, full-width cards |
| Tablet | 768–1023px | Sidebar appears, 2-col grids |
| Desktop | 1024–1440px | Full sidebar, 3-col grids |
| Wide | > 1440px | Max-width capped |

**Content max-widths:** Dashboard `max-w-6xl` · Landing page `max-w-[1300px]`

### 4c. Spacing Rhythm (4px base unit)

| Context | Class |
|---------|-------|
| Icon + label gap | `gap-2` (8px) |
| Nav items vertical | `space-y-0.5` (2px) |
| Card internal padding | `p-6` desktop · `p-4` mobile |
| Section vertical padding | `py-6 px-4` mobile · `p-6 lg:p-8` desktop |
| Between cards in grid | `gap-4` (16px) |

---

## 5. Shadow Tokens

| CSS Variable | Value | Use |
|---|---|---|
| `--shadow-brand` | `0 8px 32px rgba(79,70,229,0.35)` | Primary CTA, logo, key CTAs |
| `--shadow-brand-sm` | `0 4px 16px rgba(79,70,229,0.25)` | Hover states, active cards |
| `--shadow-action` | `0 8px 32px rgba(249,115,22,0.35)` | Orange CTA on landing page |

Dark mode values are slightly stronger (0.45 / 0.30 / 0.40) to compensate for dark backgrounds.

---

## 6. Motion Tokens

| Type | Duration | Easing | Use |
|------|----------|--------|-----|
| Micro | 150ms | ease | Button press, badge appear |
| Component | 200ms | ease | Dropdown, tooltip, toggle |
| Entrance | 300ms | cubic-bezier(.22,1,.36,1) | Cards, modals, drawers |
| Scroll reveal | 750ms | cubic-bezier(.22,1,.36,1) | Landing page sections |

**Accessibility rule:** Always respect `prefers-reduced-motion`. Wrap scroll-triggered animations.

---

## 7. Component Patterns

### 7a. Buttons

| Variant | Pattern | When |
|---------|---------|------|
| **Primary** | `bg-primary text-primary-foreground rounded-xl h-11` | Default CTA in app |
| **Brand gradient** | `bg-gradient-to-r from-brand-600 to-brand-end text-white rounded-2xl` | Hero CTAs (landing only) |
| **Action gradient** | `bg-gradient-to-r from-action-500 to-warm-600 text-white rounded-2xl` | High-energy landing CTA |
| **Ghost** | `border border-border text-foreground/60 rounded-xl` | Secondary action |
| **Destructive** | `bg-destructive text-destructive-foreground rounded-xl` | Delete, leave, danger |

Height rule: `h-11` (44px) minimum — WCAG touch target.

### 7b. Cards

```
bg-card border border-border/60 rounded-xl
```

In dark mode: `bg-card = #111111` automatically — no override needed.
Feature/marketing cards: add `bg-white/[0.025] border border-white/[0.07]`.

### 7c. Nav Items (Dashboard Sidebar)

**Active:**
```
bg-primary text-primary-foreground shadow-sm rounded-xl
```

**Inactive:**
```
text-muted-foreground hover:text-foreground hover:bg-secondary/80 rounded-xl transition-all duration-150
```

**Icon container (active):** `bg-white/20`
**Icon container (inactive):** `bg-{color}-500/10` with matching `text-{color}-500`

### 7d. Labels / Section Overlines

Consistent label style above section headings:
```
text-[11px] font-bold uppercase tracking-[0.12em]
px-3.5 py-1.5 rounded-full
```

Color by context:
- Brand/feature: `bg-brand-500/10 border border-brand-500/25 text-brand-400`
- Problem/alert: `bg-red-500/10 border border-red-500/20 text-red-400`
- Setup/success: `bg-emerald-500/10 border border-emerald-500/20 text-emerald-400`
- Neutral: `bg-white/[0.04] border border-white/[0.08] text-white/30`

### 7e. Form Inputs

```
bg-secondary border border-border rounded-xl px-3 h-11
outline-none focus:border-primary/60 focus:ring-1 focus:ring-ring/30
transition-all duration-200 text-sm
```

### 7f. Avatar / Initials

```
w-8 h-8 rounded-full bg-primary text-primary-foreground
font-bold text-sm flex items-center justify-center
```

---

## 8. Iconography

**Library:** Lucide React — all dashboard and app icons.
**Sizing standard:**
- Sidebar nav icons: `size={15}`
- Inline text icons: `size={16}`
- Action buttons: `size={20}`
- Mobile bottom nav: `size={22}`

**Rule:** No emojis as UI icons. Emojis are acceptable in task names, demo data, and marketing copy only.

---

## 9. Dark vs Light Mode Defaults

| Surface | Mode |
|---------|------|
| Landing page | **Dark only** — brand-critical |
| Login / Onboarding | Dark — matches landing page |
| Dashboard | User-toggled (system pref as default) |

---

## 10. Color Usage Guide — Where Each Color Lives

| Color | In App | On Landing |
|-------|--------|-----------|
| Indigo `#4f46e5` | Active nav, buttons, rings, links, selected states | Gradient start |
| Violet `#7c3aed` | Gradient end in logo/hero only | Gradient end, hero text gradient |
| Orange `#f97316` | — | Hero CTA alternative, warm section accents |
| Amber `#f59e0b` | Status: due today badge | — |
| Red `#ef4444` | Status: overdue, error, destructive | Problem section |
| Emerald `#10b981` | Status: done, success | Setup section |

**Key rule:** Orange is a WARM ACCENT — used for warmth and energy in marketing/landing contexts. In the dashboard app, indigo is the single interactive color. This keeps the UI clean and unambiguous.

---

## 11. Anti-Patterns

| ❌ Don't | ✓ Do |
|---------|------|
| Use blue `#3b82f6` as primary | Use indigo `#4f46e5` (or `bg-primary`) |
| Hardcode `bg-indigo-600` in components | Use `bg-primary` |
| Use slate dark mode (`#0f172a`) | Use deep dark (`#0a0a0a`) + surface scale |
| Use orange in dashboard action buttons | Orange is marketing/landing only |
| Use emojis as navigation icons | Lucide React icons only |
| Different radius per component | Always `rounded-xl` as default |
| Add drop shadows to light mode cards | Border only: `border border-border/60` |
| Mix Plus Jakarta Sans with Inter | PJS on landing, Inter everywhere else |
| Gradients on body/UI text | Gradients on display headings only |

---

*Maintained by: Sai Jaswanth · Version 1.1 · June 2026*
*Source: `C:\garbage\design-system\MASTER.md`*
