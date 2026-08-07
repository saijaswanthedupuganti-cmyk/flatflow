# Habitiq — Pitch Website Design System
**Simple reference for anyone building the pitch deck website.**
All values are taken directly from the live app's DESIGN.md. The website should feel like an extension of the product — same language, same confidence.

---

## Core Identity

| Property | Value |
|----------|-------|
| **Mood** | Dark. Intelligent. Calm. No hype. |
| **Background** | Near-black with a 1% warm violet push — not pure black |
| **Accent** | Violet — used exactly once per screen fold |
| **Font** | Inter — same as the app |
| **Border radius** | 12px cards · 8px inputs · 20px pills |

---

## Colors

Copy these directly into your CSS variables or Tailwind config.

### Brand
```css
--color-primary:         #7c3aed;   /* Habitiq violet — CTAs, active, progress */
--color-primary-soft:    #a78bfa;   /* Hover, secondary violet moments */
--color-primary-muted:   #7c3aed1a; /* 10% violet glow — card backgrounds, halos */
--color-primary-dark:    #5b21b6;   /* Pressed state */
```

### Page Canvas (Dark-first)
```css
--color-canvas:          #0c0b0f;   /* Page background */
--color-canvas-soft:     #131117;   /* Nav bar, search, sticky header */
--color-canvas-card:     #1a1820;   /* Default card background */
--color-canvas-elevated: #211f28;   /* Modals, bottom sheets */
--color-canvas-raised:   #2a2733;   /* Tooltips, dropdowns */
```

### Text
```css
--color-ink:             #f4f3f8;   /* Primary text — warm white, not cold */
--color-ink-soft:        #a09db0;   /* Descriptions, secondary info */
--color-ink-mute:        #514e61;   /* Placeholders, captions, disabled */
```

### Borders
```css
--color-hairline:        #2a2635;   /* Card borders, input borders, dividers */
--color-hairline-soft:   #1e1c28;   /* Subtle section separators */
```

### Semantic (status colours)
```css
--color-positive:        #22c55e;   /* Success, done, on-track */
--color-positive-soft:   #22c55e18; /* Success surface tint */
--color-warning:         #f59e0b;   /* Overdue, partial */
--color-warning-soft:    #f59e0b18; /* Warning surface tint */
--color-negative:        #ef4444;   /* Missed, debt */
--color-negative-soft:   #ef444418; /* Negative surface tint */
```

### Accent Secondaries
```css
--color-streak:          #f97316;   /* Orange — momentum, energy */
--color-reward:          #eab308;   /* Gold — achievements, earned */
```

### Quick Palette Visual
```
Background  ████  #0c0b0f   — page
Card        ████  #1a1820   — every content card
Violet      ████  #7c3aed   — THE accent (use once per fold)
Text        ████  #f4f3f8   — headings, body
Muted       ████  #a09db0   — subtitles, labels
Green       ████  #22c55e   — positive signals
Amber       ████  #f59e0b   — warnings, streaks
```

---

## Typography

Single font throughout: **Inter**. Load it from Google Fonts.

```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
```

```css
body {
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
}
```

### Type Scale

| Name | Size | Weight | Line Height | Letter Spacing | Use |
|------|------|--------|-------------|----------------|-----|
| **display-xl** | 64px | 800 | 68px | -2px | Hero headline |
| **display-lg** | 44px | 700 | 48px | -1.5px | Section titles |
| **display-md** | 32px | 700 | 36px | -0.8px | Feature headings |
| **display-sm** | 24px | 700 | 28px | -0.5px | Card headings |
| **display-xs** | 18px | 600 | 24px | -0.2px | Sub-headings |
| **body-lg** | 17px | 400 | 28px | 0 | Primary body copy |
| **body-md** | 15px | 400 | 24px | 0 | Secondary body, descriptions |
| **body-sm** | 13px | 400 | 20px | 0 | Captions, hints |
| **label** | 12px | 600 | 16px | +0.6px | All-caps labels, tags |

**For numbers / stats:** Add `font-variant-numeric: tabular-nums` so digits align in columns.

**Font feature settings for headings:** `font-feature-settings: 'ss03' 1` — gives Inter the curved `a` and `g` which makes large headlines feel warmer.

### CSS Snippet
```css
.headline {
  font-size: 64px;
  font-weight: 800;
  line-height: 68px;
  letter-spacing: -2px;
  font-feature-settings: 'ss03' 1;
  color: #f4f3f8;
}

.section-title {
  font-size: 44px;
  font-weight: 700;
  letter-spacing: -1.5px;
  color: #f4f3f8;
}

.body-copy {
  font-size: 17px;
  font-weight: 400;
  line-height: 28px;
  color: #a09db0;
}

.label {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  color: #514e61;
}
```

---

## Spacing

8-point grid. Every margin, padding, and gap should be a multiple of 8.

| Token | Value | Use |
|-------|-------|-----|
| `space-1` | 4px | Icon gap, tight inline |
| `space-2` | 8px | Chip padding, tight card |
| `space-3` | 12px | Input padding vertical |
| `space-4` | 16px | Default card padding |
| `space-5` | 20px | Section inner padding |
| `space-6` | 24px | Card padding (comfortable) |
| `space-8` | 32px | Between cards |
| `space-10` | 40px | Section gap (mobile) |
| `space-16` | 64px | Section gap (desktop) |
| `space-24` | 96px | Hero vertical padding |

---

## Border Radius

```css
--radius-sm:   8px;    /* Inputs, small chips */
--radius-md:  12px;    /* Standard cards — the main radius */
--radius-lg:  16px;    /* Feature cards, large containers */
--radius-xl:  20px;    /* Pills, badges, tags */
--radius-full: 9999px; /* Avatars, circular buttons */
```

---

## Elevation / Shadows

Three levels. All shadows use violet-tinted darkness — never pure black shadow.

```css
/* Level 1 — card resting */
--shadow-card: 0 1px 3px rgba(0,0,0,0.4), 0 1px 2px rgba(0,0,0,0.3);

/* Level 2 — card hover / focused */
--shadow-card-hover: 0 4px 16px rgba(124,58,237,0.12), 0 2px 8px rgba(0,0,0,0.4);

/* Level 3 — modals, floating panels */
--shadow-modal: 0 24px 64px rgba(0,0,0,0.6), 0 4px 16px rgba(124,58,237,0.08);
```

**Hover state on cards:** add a faint violet glow (`box-shadow: 0 0 0 1px #7c3aed40`). This is the Habitiq tell — everything that is interactive subtly glows violet when you reach for it.

---

## Buttons

Two buttons only. Keep it simple.

### Primary (Violet Fill)
```css
.btn-primary {
  background: #7c3aed;
  color: #f4f3f8;
  border-radius: 12px;
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: background 150ms ease, transform 100ms ease;
}
.btn-primary:hover  { background: #6d28d9; }
.btn-primary:active { background: #5b21b6; transform: scale(0.98); }
```

### Secondary (Ghost / Outline)
```css
.btn-secondary {
  background: transparent;
  color: #f4f3f8;
  border: 1px solid #2a2635;
  border-radius: 12px;
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: border-color 150ms ease, background 150ms ease;
}
.btn-secondary:hover {
  border-color: #7c3aed;
  background: #7c3aed1a;
}
```

**Rule:** One primary CTA per section. Never two violet buttons side by side.

---

## Cards

### Standard Card
```css
.card {
  background: #1a1820;
  border: 1px solid #2a2635;
  border-radius: 12px;
  padding: 24px;
  transition: border-color 200ms ease, box-shadow 200ms ease;
}
.card:hover {
  border-color: #7c3aed40;
  box-shadow: 0 4px 16px rgba(124,58,237,0.10);
}
```

### Highlighted / Feature Card (violet glow surface)
```css
.card-featured {
  background: linear-gradient(135deg, #1a1820 0%, #1e1a2e 100%);
  border: 1px solid #7c3aed40;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 0 32px rgba(124,58,237,0.08);
}
```

### Stat / Number Card
```css
.stat-number {
  font-size: 40px;
  font-weight: 700;
  letter-spacing: -1px;
  font-variant-numeric: tabular-nums;
  color: #f4f3f8;
}
.stat-label {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  color: #514e61;
  margin-top: 4px;
}
```

---

## Status Badges / Pills

```css
/* Base */
.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

/* Variants */
.badge-violet  { background: #7c3aed1a; color: #a78bfa; border: 1px solid #7c3aed30; }
.badge-green   { background: #22c55e18; color: #4ade80; border: 1px solid #22c55e30; }
.badge-amber   { background: #f59e0b18; color: #fbbf24; border: 1px solid #f59e0b30; }
.badge-red     { background: #ef444418; color: #f87171; border: 1px solid #ef444430; }
.badge-neutral { background: #2a2635;   color: #a09db0; border: 1px solid #3a3748;   }
```

---

## Dividers / Hairlines

```css
.divider {
  height: 1px;
  background: #2a2635;
  border: none;
  margin: 24px 0;
}

.divider-soft {
  height: 1px;
  background: #1e1c28;
  border: none;
}
```

---

## Gradient Accents (For Hero / Section Backgrounds)

Use sparingly — one per page, behind the hero or a key section.

```css
/* Violet atmospheric glow — hero background halo */
.hero-glow {
  background: radial-gradient(ellipse 60% 40% at 50% 0%, #7c3aed18 0%, transparent 70%);
}

/* Section separator gradient */
.section-fade {
  background: linear-gradient(180deg, transparent 0%, #0c0b0f 100%);
}

/* Violet-to-transparent horizontal rule */
.gradient-rule {
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, #7c3aed60 50%, transparent 100%);
}
```

---

## Animation

All transitions: `150–300ms`. All easing: `ease` or `cubic-bezier(0.22, 1, 0.36, 1)` (spring-like).

```css
/* Page-level elements */
--transition-fast:   150ms ease;
--transition-base:   200ms ease;
--transition-slow:   300ms ease;
--transition-spring: 300ms cubic-bezier(0.22, 1, 0.36, 1);

/* Fade-in (for section reveals) */
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}
.animate-fadeUp {
  animation: fadeUp 400ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

/* Number count-up — trigger via JS when visible */
/* Use CountUp.js or a simple requestAnimationFrame loop */
```

**Rule:** No animation longer than 400ms. No animation that moves an element more than 20px. The app feels "ready", not theatrical.

---

## Navigation Bar (Pitch Site)

```css
.nav {
  position: fixed;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(19, 17, 23, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid #2a2635;
  border-radius: 9999px;
  padding: 10px 24px;
  display: flex;
  align-items: center;
  gap: 32px;
  z-index: 100;
}
```

Floating pill navbar — not edge-to-edge. Matches the "orbital" visual motif from the app's loading screen.

---

## Pitch Page Layout Recommendation

For a pitch website these are the sections, in order:

| # | Section | Headline Pattern | Card Type |
|---|---------|-----------------|-----------|
| 1 | **Hero** | "Smart living, managed." | Violet glow halo behind text |
| 2 | **The Problem** | "Flatmates argue. Nobody wins." | 3 dark cards — each a problem |
| 3 | **The Product** | App screenshot / demo | Featured card with violet border |
| 4 | **Key Stats** | "10M+ shared-living residents in India" | Stat number cards |
| 5 | **Feature Grid** | Bite-sized feature tiles | Bento grid — 6 cards |
| 6 | **Business Model** | 3 pricing tiers | Card trio with violet on Pro |
| 7 | **Traction** | Real user numbers | Large number display cards |
| 8 | **Team** | Sai + Bhanu | Two member cards |
| 9 | **CTA** | "Join the beta" or "Contact us" | Violet gradient section |

---

## Anti-Patterns (Do Not Do)

| ❌ Don't | ✅ Do Instead |
|----------|--------------|
| Use white or light background | Dark canvas `#0c0b0f` only |
| Use two violet CTAs in the same section | One primary CTA per section |
| Use pure black `#000000` | Warm near-black `#0c0b0f` |
| Use cold white `#ffffff` for text | Warm white `#f4f3f8` |
| Use box shadows with pure black | Shadow with violet tint `rgba(124,58,237,0.12)` |
| Animate width/height/position | Animate `opacity` and `transform` only |
| Use emoji as section icons | Use Lucide React SVG icons |
| Use multiple accent colors at once | Violet as the one accent |
| Use a flat header stuck to the top | Floating pill navbar with blur backdrop |

---

*Document: PITCH_DESIGN_SYSTEM.md*
*Version: 1.0 | June 2026*
*Source of truth: C:\garbage\DESIGN.md*
*Live app: habitiq.app*
