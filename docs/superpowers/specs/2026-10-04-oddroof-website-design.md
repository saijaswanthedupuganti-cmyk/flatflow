# Oddroof marketing website — design spec

**Date:** 2026-10-04 · **Status:** approved in conversation, awaiting written-spec review
**Owner:** Sai Jaswanth · **Code:** `apps/web` (Next.js 16, Netlify)

## 1. Goal

Replace the current Habitiq landing page with an Oddroof marketing site that feels like the
Android app: golden-hour photography, frosted glass, the electric-blue voice orb, live animated
mockups, fully responsive. The site's one job is to get people to **download the Android APK**.

**Success means**
- A visitor understands what Oddroof does within the hero and can download the APK in one tap.
- Every page works at 375 / 768 / 1024 / 1440 px with no horizontal scroll.
- Lighthouse mobile performance ≥ 90; LCP < 2.5 s on 4G; CLS < 0.1.
- Motion is smooth on a budget Android phone and absent under `prefers-reduced-motion`.

## 2. Scope

**In scope (this spec)**
| Route | Page |
|---|---|
| `/` | Home landing (built and reviewed first) |
| `/flat-manager` | Product: tasks, expenses, bills, away |
| `/discover` | Product: find a flat / find a flatmate |
| `/voice` | Product: voice assistant |
| `/privacy-and-safety` | Hub: plain-language trust page linking to legal pages |
| `/privacy`, `/terms`, `/safety` | Restyled in the new brand; **legal wording unchanged** |
| `/about` | Mission, team, contact, press kit |

**Explicitly out of scope — do not modify**
- `/login`, `/join` (the `(auth)` group), `/onboarding`, every `/dashboard/*` route, their
  components, stores, contexts and dependencies (including three.js / react-three).
  The owner will shut these down in a later version. No redirects, deletions or shared-style
  changes touch them.
- Netlify auth proxy redirects in `netlify.toml` stay as they are.

**Not on the site:** star ratings, reviews, user counts, pricing tiers, newsletter, blog, or any
feature the app does not have today.

## 3. Brand and content rules

- Name: **Oddroof**. Domain and contact stay **habitiq.app** / **hello@habitiq.app** (live
  grievance inbox) until an Oddroof domain exists. Legal entity text is unchanged.
- Claims must be true of the shipped app. Approved trust claims:
  contact details stay hidden until a connection is accepted · listings show approximate location
  only · vacancies posted by members are admin-approved · delete your account and data any time ·
  built for India's DPDP Act.
- Voice copy may say requests work in English mixed with Hindi or Telugu.
- No emoji as icons (Lucide line icons only).

## 4. Visual system

### 4.1 Colour tokens (scoped to the site)
| Token | Value | Use |
|---|---|---|
| `--or-blue` | `#2F6BFF` | Primary: download button, links, orb, focus ring |
| `--or-navy` | `#1C2A40` | Headings on light, voice section background |
| `--or-amber` | `#F5A524` | Golden-hour highlights (never body text) |
| `--or-coral` | `#FF6B5A` | Small warm accents (never body text) |
| `--or-teal` | `#14B8A6` | Tasks accent |
| `--or-violet` | `#8B5CF6` | Members accent |
| `--or-canvas` | `#F7F9FC` | Light section background |
| `--or-text` | `#0F172A` | Body text |
| `--or-text-2` | `#475569` | Secondary text |

All text meets 4.5:1 against its actual background (photo sections use a scrim behind text).
Accent meanings match the app's quick actions: teal = tasks, blue = expenses, amber = bills,
violet = members.

### 4.2 Glass tiers
| Tier | Where | Recipe |
|---|---|---|
| Photo glass | Hero, final CTA | `rgba(255,255,255,.14–.18)`, `blur(18px) saturate(140%)`, 1px border white/30, top-edge sheen gradient |
| Canvas glass | Feature cards on light | `rgba(255,255,255,.72–.80)`, `blur(14px)`, 1px border `#E2E8F0` |
| Dark glass | Voice section | `rgba(28,42,64,.55)`, `blur(20px)`, inner blue glow |

Fallback: when `backdrop-filter` is unsupported or reduced motion/transparency is requested,
panels render as solid tinted surfaces of the same colour family.

### 4.3 Typography
- Headings: **Plus Jakarta Sans** 700–800, tight tracking. Hero ≈ 64 px desktop / 40 px mobile.
- Body: **Inter** 16–18 px, line-height 1.6, max ~70 characters per line.
- Labels: Inter, uppercase, letter-spaced (as "YOUR FLAT" in the app).
- Loaded with `next/font` (self-hosted, `display: swap`).

### 4.4 Imagery
- Two golden-hour flat interior photos (hero, final CTA), text-free, served as responsive WebP/AVIF
  via `next/image` with explicit dimensions. Hero image is `priority`.
- All phone mockups are coded components (sharp at any size, animatable), never screenshots.

## 5. Motion system

- Libraries: **Motion** (`framer-motion` v12, already a dependency) and **Lenis** (new, smooth
  scroll). No GSAP, no three.js on site pages.
- Animate only `transform` and `opacity`. Never animate layout properties or blur values.
- One easing family (soft settle, e.g. `[0.22, 1, 0.36, 1]`). Entrances 0.6–0.8 s; hover/tap
  0.15–0.25 s; stagger 0.06–0.08 s.
- Scroll-linked (`useScroll`/`useTransform`) for hero phone tilt, pinned story and navbar frosting.
- Mockup loops run only while in view (`useInView`) and pause off-screen.
- < 768 px: pinned story becomes swipeable stacked cards; tilt off; blur reduced.
- `prefers-reduced-motion: reduce`: Lenis disabled, all content rendered in final state, loops off.

## 6. Home page (`/`) sections

1. **Floating glass navbar** — logo · Products ▾ (Flat manager, Discover, Voice) · Privacy ·
   About · **Download APK**. Inset from viewport edges; frost increases with scroll. Mobile: menu
   sheet with the same links and the download button.
2. **Hero** — golden-hour photo; headline "Your flat, sorted."; one supporting line; primary
   **Download APK** with meta "Android 8+ · <size> · free" from `version.json`; secondary
   "See how it works" (scrolls to §4); live Home-screen phone mockup with the blue orb; QR code to
   the APK on ≥ 1024 px. Motion: glass cards settle into the phone, orb breathes, phone tilts on scroll.
3. **Problem** — "Your flat's WhatsApp group is doing too much." Scattered chat bubbles fold into
   one tidy Oddroof card.
4. **Pinned phone story** — sticky phone while four steps scroll: Tasks rotate fairly → Expenses
   split → Bills & month close → Away mode. Screen content swaps per step.
5. **Voice** — dark section around the large orb: "Just say it." Typed demo "paid Ravi 300 for the
   cylinder" becomes a confirm card; note on mixed-language requests. Live waveform.
6. **Discover** — find a flat / find a flatmate; listing cards in the app's style; admin-approved
   vacancies and approximate location. Cards fan out.
7. **Privacy & trust** — four glass tiles with the approved claims (§3); link to the hub.
8. **How to install** — three steps for installing an APK, a plain note about Android's
   "unknown sources" prompt, current version and release date.
9. **Final CTA + footer** — golden-hour photo, "Make your flat feel like home.", download button;
   footer links: Products, Privacy & safety, Privacy, Terms, Safety, About, hello@habitiq.app,
   © 2026 Oddroof.

Product pages, the hub and About reuse the same section shell, glass tiers and mockups; their
section-level layout is designed in a follow-up review after Home is approved live.

## 7. Architecture

```
apps/web/
  app/(site)/
    layout.tsx            # site-only: fonts, tokens, SmoothScroll, Navbar, Footer
    page.tsx              # Home (replaces app/page.tsx)
    flat-manager/page.tsx
    discover/page.tsx
    voice/page.tsx
    privacy-and-safety/page.tsx
    about/page.tsx
    privacy/ terms/ safety/   # moved in, restyled, wording unchanged
  components/site/
    tokens.css            # --or-* variables, glass tiers, scoped under .or-site
    Glass.tsx, Button.tsx, Section.tsx, Navbar.tsx, Footer.tsx
    DownloadButton.tsx    # reads version.json; QR variant
    SmoothScroll.tsx      # Lenis provider, off under reduced motion
    motion.ts             # shared easing, durations, variants
    mockups/
      PhoneFrame.tsx, HomeScreen.tsx, TasksScreen.tsx, ExpenseScreen.tsx,
      BillsScreen.tsx, AwayScreen.tsx, VoiceOrb.tsx, VoiceDemo.tsx,
      ListingCard.tsx, ChatBubbles.tsx
  public/downloads/
    oddroof-latest.apk
    version.json          # { version, versionCode, sizeMb, releasedOn, minAndroid }
```

- Each mockup is self-contained: props in, no data fetching, no Firebase.
- Site styles live under a `.or-site` root class and CSS modules so nothing leaks into the
  dashboard. The root `app/layout.tsx` is changed only if needed for metadata, without affecting
  dashboard rendering.
- Pages are statically generated; no client data fetching.
- SEO: per-page metadata, Open Graph image in the new brand, `sitemap.xml`, `robots.txt`.

## 8. APK distribution

- Add a **release** build type with a dedicated upload keystore (generated once; stored outside the
  repo; owner backs it up — losing it blocks future updates over the same install).
- Release APK copied to `public/downloads/oddroof-latest.apk`; `version.json` updated alongside.
  A release = swap those two files and deploy.
- Download button links directly to the file with `download` attribute and correct
  `application/vnd.android.package-archive` content type (Netlify header rule).

## 9. Testing and acceptance

- **Playwright** (existing setup), every site route at 375 / 768 / 1024 / 1440:
  no horizontal scroll; navbar links resolve (no 404s); download link returns 200 with the APK
  content type; reduced-motion run renders all content visible.
- **Lighthouse** mobile on `/`: performance ≥ 90, accessibility ≥ 95.
- One spot-check screenshot per page for the owner; the owner does final visual QA.
- Existing dashboard Playwright tests must still pass unchanged (proves no regression).

## 10. Delivery phases

1. Foundation + Home (`/`) → owner reviews live preview.
2. Product pages (`/flat-manager`, `/discover`, `/voice`).
3. Privacy hub, restyled legal pages, About.
4. Release APK + `version.json` + Netlify headers, then production deploy on owner's go.

## 11. Open items (owner)

- Golden-hour hero/final photos: owner supplies, or AI-generated text-free images approved by owner.
- About page: team members to list and whether to include photos.
- Release keystore backup location.
