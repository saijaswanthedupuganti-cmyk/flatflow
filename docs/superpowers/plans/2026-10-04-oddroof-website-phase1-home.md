# Oddroof Website — Phase 1 (Foundation + Home) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the Habitiq landing at `/` with the Oddroof Home page (golden-hour + glass, coded animated mockups, APK download) on a site foundation the later pages reuse.

**Architecture:** A `(site)` route group nested inside the existing root layout holds the marketing pages with their own layout (fonts, tokens, smooth scroll, navbar, footer). Shared primitives live in `components/site/`, phone mockups in `components/site/mockups/`, release data in `lib/site/`. Styling is CSS Modules plus one scoped token file under `.or-site`, so nothing reaches the dashboard.

**Tech Stack:** Next.js 16.3.8 (App Router), React 19.2, framer-motion 12.40, Lenis (new), qrcode (new), lucide-react, Playwright.

**Spec:** `docs/superpowers/specs/2026-10-04-oddroof-website-design.md`

## Global Constraints

- Work only in `apps/web`. Do not modify anything under `app/(auth)/`, `app/onboarding/`, `app/dashboard/`, `store/`, `contexts/`, or dashboard components. Allowed shared edits: `app/layout.tsx` (metadata/JSON-LD only), `components/PWAInstallPrompt.tsx` (site-route guard only), `proxy.ts` (preview-host allowance only), `package.json`, `.gitignore`.
- Brand name **Oddroof**; contact **hello@habitiq.app**; canonical `https://habitiq.app`.
- No star ratings, reviews, user counts, pricing, or features the app lacks. Trust claims limited to: contact details hidden until a connection is accepted · approximate location only · member vacancies are admin-approved · delete your account and data any time · built for India's DPDP Act.
- No emoji as icons — lucide-react only.
- Colours: `--or-blue #2F6BFF`, `--or-navy #1C2A40`, `--or-amber #F5A524`, `--or-coral #FF6B5A`, `--or-teal #14B8A6`, `--or-violet #8B5CF6`, `--or-canvas #F7F9FC`, `--or-text #0F172A`, `--or-text-2 #475569`. Text contrast ≥ 4.5:1.
- Fonts: Plus Jakarta Sans 700–800 (headings), Inter (body, already loaded as `--font-inter`).
- Motion: animate `transform`/`opacity` only; ease `[0.22, 1, 0.36, 1]`; entrances 0.6–0.8 s; hover 0.15–0.25 s; stagger 0.06–0.08 s; loops only while in view; `prefers-reduced-motion: reduce` ⇒ no motion, final state rendered.
- Breakpoints tested: 375, 768, 1024, 1440. No horizontal scroll at any of them.
- APK binaries are never committed (`apps/web/public/downloads/*.apk` is git-ignored); the button reads its URL from `version.json`.
- Every Playwright command runs from `apps/web`: `npx playwright test tests/site.spec.ts --project=chromium-desktop`.

## Review Focus

1. **Visitor on a phone that lacks `backdrop-filter`** (older Android WebView) — glass panels must still be readable solid surfaces, not transparent text on a photo. Pinned by the `@supports` fallback test in Task 2.
2. **Visitor with reduced motion enabled** — every section's heading and copy must be visible without scrolling animations completing. Pinned by the reduced-motion test in Task 11.
3. **The PWA "install this web app" prompt appearing on the marketing site** — it must never render on site routes, while still working on `/dashboard`. Pinned by the prompt test in Task 1.
4. **Narrow 375 px phone** — the pinned story, hero phone and navbar must not overflow sideways. Pinned by the no-horizontal-scroll matrix in Task 11.
5. **Download link pointing nowhere** — the button `href` must equal `version.json.url` everywhere it appears (hero, navbar, final CTA, QR). Pinned by the download-consistency test in Task 2/Task 10.

## File Structure

```
apps/web/
  app/page.tsx                         DELETE (old Habitiq landing)
  app/landing.module.css               DELETE (only used by old landing)
  app/layout.tsx                       MODIFY metadata + JSON-LD to Oddroof
  app/(site)/layout.tsx                CREATE site shell
  app/(site)/page.tsx                  CREATE Home (composes sections)
  components/PWAInstallPrompt.tsx      MODIFY guard on site paths
  proxy.ts                             MODIFY allow Netlify preview hosts
  lib/site/routes.ts                   CREATE site paths + nav links
  lib/site/release.ts                  CREATE typed release data
  public/downloads/version.json        CREATE release metadata
  components/site/tokens.css           CREATE tokens, glass tiers, fallbacks
  components/site/motion.ts            CREATE easing/durations/variants
  components/site/useLoopStep.ts       CREATE in-view loop hook
  components/site/SmoothScroll.tsx     CREATE Lenis provider
  components/site/Glass.tsx            CREATE glass panel
  components/site/SiteButton.tsx       CREATE link button
  components/site/Section.tsx          CREATE section shell + Reveal
  components/site/DownloadButton.tsx   CREATE APK button + meta
  components/site/DownloadQr.tsx       CREATE server QR code
  components/site/Navbar.tsx           CREATE floating glass navbar
  components/site/Footer.tsx           CREATE footer
  components/site/site.module.css      CREATE styles for primitives/nav/footer
  components/site/home/*.tsx           CREATE one file per Home section
  components/site/home/home.module.css CREATE section styles
  components/site/mockups/*.tsx        CREATE phone frame + screens + orb + cards
  components/site/mockups/mockups.module.css CREATE mockup styles
  public/site/                         CREATE photo folder (owner photos)
  tests/site.spec.ts                   CREATE Playwright suite for the site
```

---

### Task 1: Site shell, routing guards, root metadata

**Files:**
- Create: `apps/web/lib/site/routes.ts`, `apps/web/app/(site)/layout.tsx`, `apps/web/app/(site)/page.tsx`, `apps/web/components/site/tokens.css`, `apps/web/components/site/SmoothScroll.tsx`, `apps/web/tests/site.spec.ts`
- Modify: `apps/web/components/PWAInstallPrompt.tsx:8-40`, `apps/web/app/layout.tsx` (metadata + JSON-LD), `apps/web/proxy.ts:9-20`, `apps/web/package.json`
- Delete: `apps/web/app/page.tsx`, `apps/web/app/landing.module.css`

**Interfaces:**
- Produces: `SITE_PATHS: readonly string[]`, `isSitePath(pathname: string | null): boolean`, `PRODUCT_LINKS: { href: string; label: string; blurb: string }[]`, `NAV_LINKS: { href: string; label: string }[]` from `@/lib/site/routes`; CSS class `.or-site` root and all `--or-*` variables; `<SmoothScroll>{children}</SmoothScroll>`.

- [ ] **Step 1: Install dependencies**

Run (from `apps/web`): `npm install lenis@^1.3 qrcode@^1.5 && npm install -D @types/qrcode@^1.5`
Expected: both added to `package.json`, no peer warnings about React 19 that block install.

- [ ] **Step 2: Write the failing tests**

Create `apps/web/tests/site.spec.ts`:

```ts
import { test, expect } from '@playwright/test'

test.describe('site shell', () => {
  test('home renders the Oddroof site root', async ({ page }) => {
    await page.goto('/')
    await expect(page.locator('.or-site')).toBeVisible()
    await expect(page).toHaveTitle(/Oddroof/)
  })

  test('PWA install prompt never renders on site routes', async ({ page }) => {
    // Make the browser look install-eligible so the prompt WOULD show without the guard.
    await page.addInitScript(() => {
      window.addEventListener('load', () => {
        const e = new Event('beforeinstallprompt') as Event & { prompt?: () => Promise<void>; userChoice?: Promise<unknown> }
        e.prompt = async () => {}
        e.userChoice = Promise.resolve({ outcome: 'dismissed' })
        window.dispatchEvent(e)
      })
    })
    await page.goto('/')
    await page.waitForTimeout(2500) // prompt appears after 1.5 s when eligible
    await expect(page.getByRole('button', { name: /install/i })).toHaveCount(0)
  })
})
```

- [ ] **Step 3: Run tests to verify they fail**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop`
Expected: FAIL — `.or-site` not found; title contains "Habitiq"; the prompt test fails because the synthetic `beforeinstallprompt` makes `PWAContext` eligible.

- [ ] **Step 4: Create routes**

`apps/web/lib/site/routes.ts`:

```ts
export const PRODUCT_LINKS = [
  { href: '/flat-manager', label: 'Flat manager', blurb: 'Tasks, expenses, bills and away mode' },
  { href: '/discover', label: 'Discover', blurb: 'Find a flat or a flatmate' },
  { href: '/voice', label: 'Voice assistant', blurb: 'Just say it' },
] as const

export const NAV_LINKS = [
  { href: '/privacy-and-safety', label: 'Privacy' },
  { href: '/about', label: 'About' },
] as const

export const SITE_PATHS = [
  '/', '/flat-manager', '/discover', '/voice', '/privacy-and-safety', '/about',
  '/privacy', '/terms', '/safety',
] as const

export function isSitePath(pathname: string | null): boolean {
  if (!pathname) return false
  const clean = pathname.length > 1 ? pathname.replace(/\/+$/, '') : pathname
  return (SITE_PATHS as readonly string[]).includes(clean)
}
```

- [ ] **Step 5: Guard the PWA prompt**

In `apps/web/components/PWAInstallPrompt.tsx` add imports and replace the existing `if (!show || isInstalled) return null` line (keep all hooks above it unchanged):

```tsx
import { usePathname } from 'next/navigation'
import { isSitePath } from '@/lib/site/routes'
```

```tsx
  const pathname = usePathname()
  // The marketing site promotes the Android APK; the web-app install prompt belongs to the dashboard only.
  if (!show || isInstalled || isSitePath(pathname)) return null
```

Place `const pathname = usePathname()` directly after the existing `useState` lines so hook order is stable.

- [ ] **Step 6: Tokens**

`apps/web/components/site/tokens.css`:

```css
.or-site {
  --or-blue: #2F6BFF;
  --or-blue-deep: #1F4FD1;
  --or-navy: #1C2A40;
  --or-amber: #F5A524;
  --or-coral: #FF6B5A;
  --or-teal: #14B8A6;
  --or-violet: #8B5CF6;
  --or-canvas: #F7F9FC;
  --or-text: #0F172A;
  --or-text-2: #475569;
  --or-line: #E2E8F0;
  --or-radius-lg: 28px;
  --or-radius-md: 20px;
  --or-radius-sm: 14px;
  --or-ease: cubic-bezier(0.22, 1, 0.36, 1);
  --or-max: 1200px;
  --or-gutter: clamp(16px, 4vw, 40px);
  --or-font-display: var(--font-jakarta), var(--font-inter), system-ui, sans-serif;
  --or-font-body: var(--font-inter), system-ui, sans-serif;

  background: var(--or-canvas);
  color: var(--or-text);
  font-family: var(--or-font-body);
  font-size: 17px;
  line-height: 1.6;
  overflow-x: clip;
  min-height: 100vh;
  width: 100%;
}
.or-site h1, .or-site h2, .or-site h3 {
  font-family: var(--or-font-display);
  letter-spacing: -0.02em;
  line-height: 1.08;
  color: inherit;
}
.or-site :focus-visible {
  outline: 3px solid var(--or-blue);
  outline-offset: 3px;
  border-radius: 8px;
}
/* Golden-hour fallback when no photo is present yet, also used as photo tint. */
.or-golden {
  background:
    radial-gradient(120% 80% at 80% 10%, rgba(245,165,36,.55), transparent 60%),
    radial-gradient(90% 70% at 10% 90%, rgba(255,107,90,.35), transparent 60%),
    linear-gradient(160deg, #2A2140 0%, #6B3F3A 45%, #C9773A 80%, #F2B565 100%);
}
@media (prefers-reduced-motion: reduce) {
  .or-site *, .or-site *::before, .or-site *::after {
    animation: none !important;
    transition: none !important;
    scroll-behavior: auto !important;
  }
}
```

- [ ] **Step 7: Smooth scroll provider**

`apps/web/components/site/SmoothScroll.tsx`:

```tsx
'use client'
import { ReactLenis } from 'lenis/react'
import 'lenis/dist/lenis.css'
import { useReducedMotion } from 'framer-motion'

export default function SmoothScroll({ children }: { children: React.ReactNode }) {
  const reduce = useReducedMotion()
  if (reduce) return <>{children}</>
  return (
    <ReactLenis root options={{ lerp: 0.1, smoothWheel: true, syncTouch: false }}>
      {children}
    </ReactLenis>
  )
}
```

- [ ] **Step 8: Site layout and temporary Home**

`apps/web/app/(site)/layout.tsx`:

```tsx
import type { Metadata } from 'next'
import { Plus_Jakarta_Sans } from 'next/font/google'
import '@/components/site/tokens.css'
import SmoothScroll from '@/components/site/SmoothScroll'

const jakarta = Plus_Jakarta_Sans({ subsets: ['latin'], weight: ['600', '700', '800'], variable: '--font-jakarta', display: 'swap' })

export const metadata: Metadata = {
  title: { default: 'Oddroof — Your flat, sorted', template: '%s · Oddroof' },
  description: 'Oddroof keeps a shared flat running: fair task rotation, shared expenses and bills, away mode, a voice assistant, and safe flat and flatmate discovery.',
}

export default function SiteLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className={`or-site ${jakarta.variable}`}>
      <SmoothScroll>{children}</SmoothScroll>
    </div>
  )
}
```

`apps/web/app/(site)/page.tsx` (replaced in Task 5):

```tsx
export default function HomePage() {
  return <main><h1>Your flat, sorted.</h1></main>
}
```

Delete `apps/web/app/page.tsx` and `apps/web/app/landing.module.css` (confirm first: `grep -rn "landing.module" apps/web --include=*.tsx` returns only `app/page.tsx`).

- [ ] **Step 9: Root metadata and JSON-LD to Oddroof**

In `apps/web/app/layout.tsx` replace the `metadata` object with:

```tsx
export const metadata: Metadata = {
  metadataBase: new URL("https://habitiq.app"),
  title: "Oddroof — Your flat, sorted",
  description: "Oddroof keeps a shared flat running: fair task rotation, shared expenses and bills, away mode, a voice assistant, and safe flat and flatmate discovery. Android app, free.",
  manifest: "/manifest.json",
  keywords: ["shared flat app", "flatmate app India", "chore rotation app", "split expenses with flatmates", "PG expense manager", "find a flatmate", "find a flat India"],
  authors: [{ name: "Oddroof" }],
  creator: "Oddroof",
  publisher: "Oddroof",
  robots: { index: true, follow: true, googleBot: { index: true, follow: true } },
  icons: {
    icon: [
      { url: "/api/pwa-icon/32", sizes: "32x32", type: "image/png" },
      { url: "/api/pwa-icon/192", sizes: "192x192", type: "image/png" },
    ],
    apple: "/api/pwa-icon/180",
  },
  openGraph: {
    title: "Oddroof — Your flat, sorted",
    description: "Fair task rotation, shared expenses, bills, a voice assistant and safe flatmate discovery for shared flats in India.",
    siteName: "Oddroof",
    type: "website",
    url: "https://habitiq.app",
    images: [{ url: "https://habitiq.app/api/pwa-icon/512", width: 512, height: 512, alt: "Oddroof" }],
  },
  twitter: { card: "summary_large_image", title: "Oddroof — Your flat, sorted", images: ["https://habitiq.app/api/pwa-icon/512"] },
  alternates: { canonical: "https://habitiq.app" },
};
```

Replace both `application/ld+json` scripts with one truthful block:

```tsx
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{
            __html: JSON.stringify({
              "@context": "https://schema.org",
              "@type": "MobileApplication",
              "name": "Oddroof",
              "url": "https://habitiq.app",
              "operatingSystem": "Android 8.0+",
              "applicationCategory": "LifestyleApplication",
              "description": "Shared flat app: fair task rotation, shared expenses and bills, away mode, a voice assistant, and flat and flatmate discovery.",
              "offers": { "@type": "Offer", "price": "0", "priceCurrency": "INR" },
              "countryOfOrigin": "IN",
            }),
          }}
        />
```

Change `<meta name="apple-mobile-web-app-title" content="Habitiq" />` to `content="Oddroof"`. Leave everything else in the root layout unchanged.

- [ ] **Step 10: Allow Netlify preview hosts in the proxy**

In `apps/web/proxy.ts`, after the localhost check:

```ts
  // Netlify deploy previews / branch deploys (e.g. deploy-preview-12--flatsflow.netlify.app)
  // stay on their own host so the owner can review a change before it reaches habitiq.app.
  if (/--flatsflow\.netlify\.app$/.test(host)) {
    return NextResponse.next()
  }
```

- [ ] **Step 11: Run tests to verify they pass**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop`
Expected: 2 passed.

Run the existing navigation suite to prove the dashboard is unaffected: `npx playwright test tests/navigation.spec.ts --project=chromium-desktop`
Expected: same pass/fail as before this task (record the baseline before Step 1 with the same command).

- [ ] **Step 12: Commit**

```bash
git add apps/web/lib/site apps/web/app/\(site\) apps/web/components/site apps/web/components/PWAInstallPrompt.tsx apps/web/app/layout.tsx apps/web/proxy.ts apps/web/package.json apps/web/package-lock.json apps/web/tests/site.spec.ts
git rm apps/web/app/page.tsx apps/web/app/landing.module.css
git commit -m "feat(site): Oddroof site shell, tokens, smooth scroll; root metadata to Oddroof"
```

---

### Task 2: Primitives — motion, Glass, buttons, Section, release + download

**Files:**
- Create: `apps/web/components/site/motion.ts`, `apps/web/components/site/useLoopStep.ts`, `apps/web/components/site/Glass.tsx`, `apps/web/components/site/SiteButton.tsx`, `apps/web/components/site/Section.tsx`, `apps/web/components/site/site.module.css`, `apps/web/lib/site/release.ts`, `apps/web/public/downloads/version.json`, `apps/web/components/site/DownloadButton.tsx`, `apps/web/components/site/DownloadQr.tsx`
- Modify: `apps/web/.gitignore`, `apps/web/app/(site)/page.tsx` (temporary test harness content), `apps/web/tests/site.spec.ts`

**Interfaces:**
- Consumes: `.or-site` tokens (Task 1).
- Produces:
  - `EASE: [number, number, number, number]`, `DUR = { enter: 0.7, quick: 0.2 }`, `fadeUp: Variants`, `staggerKids(gap?: number): Variants` from `@/components/site/motion`.
  - `useLoopStep(steps: number, intervalMs: number): { ref: React.RefObject<HTMLDivElement | null>; step: number }` — advances only while in view; returns `steps - 1` (final state) under reduced motion.
  - `<Glass tier="photo" | "canvas" | "dark" className? as? = "div">`.
  - `<SiteButton href variant="primary" | "ghost" | "light" icon?: React.ReactNode download?: boolean>`.
  - `<Section id? tone="canvas" | "dark" | "white" eyebrow? title? lede? children>` and `<Reveal delay?>` (fade-up on enter).
  - `type Release = { version: string; versionCode: number; sizeMb: number; releasedOn: string; minAndroid: string; url: string }`, `release: Release`, `releaseMeta(r?: Release): string`.
  - `<DownloadButton variant?: "primary" | "light" showMeta?: boolean label?: string>` (server component), `<DownloadQr size?: number />` (async server component).

- [ ] **Step 1: Write the failing tests**

Add this import at the top of `apps/web/tests/site.spec.ts`, under the `@playwright/test` import:

```ts
import version from '../public/downloads/version.json'
```

Then append:

```ts
test.describe('download', () => {
  test('every download button points at version.json url', async ({ page }) => {
    await page.goto('/')
    const links = page.locator('[data-download]')
    expect(await links.count()).toBeGreaterThan(0)
    for (const href of await links.evaluateAll(els => els.map(e => e.getAttribute('href')))) {
      expect(href).toBe(version.url)
    }
  })

  test('meta line shows version, size and Android floor', async ({ page }) => {
    await page.goto('/')
    await expect(page.locator('[data-download-meta]').first())
      .toHaveText(`Android ${version.minAndroid}+ · ${version.sizeMb} MB · free · v${version.version}`)
  })
})

test('glass falls back to a solid surface without backdrop-filter', async ({ page }) => {
  await page.addInitScript(() => {
    const orig = CSS.supports.bind(CSS)
    // @ts-expect-error test override
    CSS.supports = (a: string, b?: string) => (String(a).includes('backdrop-filter') ? false : orig(a, b))
  })
  await page.goto('/')
  const bg = await page.locator('[data-glass="canvas"]').first().evaluate(el => getComputedStyle(el).backgroundColor)
  // Solid-ish: alpha ≥ 0.9 when blur is unavailable (checked via the .no-blur class set by Glass)
  const alpha = bg.startsWith('rgba') ? parseFloat(bg.split(',')[3]) : 1
  expect(alpha).toBeGreaterThanOrEqual(0.9)
})
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop`
Expected: FAIL — cannot resolve `../public/downloads/version.json`.

- [ ] **Step 3: Release data**

`apps/web/public/downloads/version.json` (values for the current build; Task 11/Phase 4 replace with the signed release):

```json
{
  "version": "1.10",
  "versionCode": 13,
  "sizeMb": 31,
  "releasedOn": "2026-10-04",
  "minAndroid": "8",
  "url": "/downloads/oddroof-latest.apk"
}
```

Append to `apps/web/.gitignore`:

```
# APK binaries are hosted, never committed
/public/downloads/*.apk
```

`apps/web/lib/site/release.ts`:

```ts
import data from '@/public/downloads/version.json'

export type Release = {
  version: string
  versionCode: number
  sizeMb: number
  releasedOn: string
  minAndroid: string
  url: string
}

export const release: Release = data

export function releaseMeta(r: Release = release): string {
  return `Android ${r.minAndroid}+ · ${r.sizeMb} MB · free · v${r.version}`
}
```

- [ ] **Step 4: Motion helpers**

`apps/web/components/site/motion.ts`:

```ts
import type { Variants } from 'framer-motion'

export const EASE: [number, number, number, number] = [0.22, 1, 0.36, 1]
export const DUR = { enter: 0.7, quick: 0.2 } as const

export const fadeUp: Variants = {
  hidden: { opacity: 0, y: 24 },
  show: { opacity: 1, y: 0, transition: { duration: DUR.enter, ease: EASE } },
}

export function staggerKids(gap = 0.07): Variants {
  return { hidden: {}, show: { transition: { staggerChildren: gap } } }
}
```

`apps/web/components/site/useLoopStep.ts`:

```ts
'use client'
import { useEffect, useRef, useState } from 'react'
import { useInView, useReducedMotion } from 'framer-motion'

/** Cycles 0..steps-1 every intervalMs while the element is on screen; final step when motion is reduced. */
export function useLoopStep(steps: number, intervalMs: number) {
  const ref = useRef<HTMLDivElement | null>(null)
  const inView = useInView(ref, { amount: 0.3 })
  const reduce = useReducedMotion()
  const [step, setStep] = useState(0)

  useEffect(() => {
    if (reduce || !inView || steps < 2) return
    const id = window.setInterval(() => setStep(s => (s + 1) % steps), intervalMs)
    return () => window.clearInterval(id)
  }, [reduce, inView, steps, intervalMs])

  return { ref, step: reduce ? steps - 1 : step }
}
```

- [ ] **Step 5: Glass, SiteButton, Section, styles**

`apps/web/components/site/site.module.css`:

```css
.glass { border-radius: var(--or-radius-lg); position: relative; isolation: isolate; }
.glass[data-glass="photo"] {
  background: rgba(255,255,255,.16);
  -webkit-backdrop-filter: blur(18px) saturate(140%);
  backdrop-filter: blur(18px) saturate(140%);
  border: 1px solid rgba(255,255,255,.30);
  box-shadow: 0 20px 60px rgba(16,12,30,.28), inset 0 1px 0 rgba(255,255,255,.35);
  color: #fff;
}
.glass[data-glass="canvas"] {
  background: rgba(255,255,255,.76);
  -webkit-backdrop-filter: blur(14px);
  backdrop-filter: blur(14px);
  border: 1px solid var(--or-line);
  box-shadow: 0 12px 40px rgba(15,23,42,.08);
}
.glass[data-glass="dark"] {
  background: rgba(28,42,64,.55);
  -webkit-backdrop-filter: blur(20px);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,.12);
  box-shadow: inset 0 0 40px rgba(47,107,255,.18);
  color: #fff;
}
/* No blur support (set by Glass.tsx via CSS.supports) or reduced transparency → solid. */
.noBlur[data-glass="photo"] { background: rgba(40,30,50,.92); }
.noBlur[data-glass="canvas"] { background: rgba(255,255,255,.98); }
.noBlur[data-glass="dark"] { background: rgba(28,42,64,.96); }
@media (prefers-reduced-transparency: reduce) {
  .glass[data-glass="photo"] { background: rgba(40,30,50,.92); }
  .glass[data-glass="canvas"] { background: rgba(255,255,255,.98); }
  .glass[data-glass="dark"] { background: rgba(28,42,64,.96); }
}

.btn {
  display: inline-flex; align-items: center; justify-content: center; gap: 10px;
  min-height: 52px; padding: 0 24px; border-radius: 999px;
  font-weight: 700; font-size: 16px; text-decoration: none; cursor: pointer;
  transition: transform .2s var(--or-ease), background-color .2s var(--or-ease), box-shadow .2s var(--or-ease);
}
.btn:hover { transform: translateY(-1px); }
.primary { background: var(--or-blue); color: #fff; box-shadow: 0 10px 30px rgba(47,107,255,.35); }
.primary:hover { background: var(--or-blue-deep); }
.ghost { background: rgba(255,255,255,.14); color: #fff; border: 1px solid rgba(255,255,255,.4); }
.light { background: #fff; color: var(--or-navy); }
.meta { margin-top: 10px; font-size: 13px; opacity: .85; }

.section { padding: clamp(72px, 10vw, 140px) var(--or-gutter); }
.toneCanvas { background: var(--or-canvas); }
.toneWhite { background: #fff; }
.toneDark { background: var(--or-navy); color: #fff; }
.inner { max-width: var(--or-max); margin: 0 auto; }
.eyebrow { font-size: 13px; font-weight: 700; letter-spacing: .14em; text-transform: uppercase; color: var(--or-blue); }
.toneDark .eyebrow { color: #9DB8FF; }
.title { font-size: clamp(32px, 5vw, 56px); margin: 12px 0 0; max-width: 18ch; }
.lede { margin-top: 18px; max-width: 60ch; color: var(--or-text-2); font-size: clamp(17px, 1.6vw, 19px); }
.toneDark .lede { color: #C9D4E8; }
```

`apps/web/components/site/Glass.tsx`:

```tsx
'use client'
import { useEffect, useState } from 'react'
import s from './site.module.css'

type Tier = 'photo' | 'canvas' | 'dark'

export default function Glass({ tier, className = '', as: Tag = 'div', children, ...rest }:
  { tier: Tier; className?: string; as?: 'div' | 'section' | 'article' | 'header' | 'nav' } & React.HTMLAttributes<HTMLElement>) {
  const [noBlur, setNoBlur] = useState(false)
  useEffect(() => {
    setNoBlur(!(CSS.supports('backdrop-filter', 'blur(1px)') || CSS.supports('-webkit-backdrop-filter', 'blur(1px)')))
  }, [])
  return (
    <Tag data-glass={tier} className={`${s.glass} ${noBlur ? s.noBlur : ''} ${className}`} {...rest}>
      {children}
    </Tag>
  )
}
```

`apps/web/components/site/SiteButton.tsx`:

```tsx
import Link from 'next/link'
import s from './site.module.css'

export default function SiteButton({ href, variant = 'primary', icon, download, children, ...rest }:
  { href: string; variant?: 'primary' | 'ghost' | 'light'; icon?: React.ReactNode; download?: boolean } & Omit<React.AnchorHTMLAttributes<HTMLAnchorElement>, 'href'>) {
  const cls = `${s.btn} ${s[variant]}`
  if (download || href.startsWith('http') || href.endsWith('.apk')) {
    return <a href={href} className={cls} download={download ? '' : undefined} {...rest}>{icon}{children}</a>
  }
  return <Link href={href} className={cls} {...rest}>{icon}{children}</Link>
}
```

`apps/web/components/site/Section.tsx`:

```tsx
'use client'
import { motion, useReducedMotion } from 'framer-motion'
import { fadeUp } from './motion'
import s from './site.module.css'

const tones = { canvas: s.toneCanvas, white: s.toneWhite, dark: s.toneDark }

export function Reveal({ children, delay = 0, className }: { children: React.ReactNode; delay?: number; className?: string }) {
  const reduce = useReducedMotion()
  // framer-motion's reducedMotion only skips transforms; opacity would still start at 0. Render the final state instead.
  if (reduce) return <div className={className}>{children}</div>
  return (
    <motion.div className={className} variants={fadeUp} initial="hidden" whileInView="show"
      viewport={{ once: true, amount: 0.25 }} transition={{ delay }}>
      {children}
    </motion.div>
  )
}

export default function Section({ id, tone = 'canvas', eyebrow, title, lede, children, className = '' }:
  { id?: string; tone?: keyof typeof tones; eyebrow?: string; title?: string; lede?: string; children?: React.ReactNode; className?: string }) {
  return (
    <section id={id} className={`${s.section} ${tones[tone]} ${className}`}>
      <div className={s.inner}>
        {(eyebrow || title || lede) && (
          <Reveal>
            {eyebrow && <p className={s.eyebrow}>{eyebrow}</p>}
            {title && <h2 className={s.title}>{title}</h2>}
            {lede && <p className={s.lede}>{lede}</p>}
          </Reveal>
        )}
        {children}
      </div>
    </section>
  )
}
```

Note: `Reveal` renders a plain `div` under reduced motion; Step 6 adds `MotionConfig reducedMotion="user"` so every other transform animation is skipped too.

- [ ] **Step 6: MotionConfig in the site layout**

In `apps/web/components/site/SmoothScroll.tsx` wrap both return branches:

```tsx
import { MotionConfig, useReducedMotion } from 'framer-motion'
// ...
  const content = reduce ? <>{children}</> : (
    <ReactLenis root options={{ lerp: 0.1, smoothWheel: true, syncTouch: false }}>{children}</ReactLenis>
  )
  return <MotionConfig reducedMotion="user">{content}</MotionConfig>
```

- [ ] **Step 7: Download button and QR**

`apps/web/components/site/DownloadButton.tsx`:

```tsx
import { Download } from 'lucide-react'
import SiteButton from './SiteButton'
import { release, releaseMeta } from '@/lib/site/release'
import s from './site.module.css'

export default function DownloadButton({ variant = 'primary', showMeta = false, label = 'Download for Android' }:
  { variant?: 'primary' | 'light'; showMeta?: boolean; label?: string }) {
  return (
    <div>
      <SiteButton href={release.url} variant={variant} download data-download=""
        icon={<Download size={20} aria-hidden />} aria-label={`${label}, ${releaseMeta()}`}>
        {label}
      </SiteButton>
      {showMeta && <p className={s.meta} data-download-meta="">{releaseMeta()}</p>}
    </div>
  )
}
```

`apps/web/components/site/DownloadQr.tsx`:

```tsx
import QRCode from 'qrcode'
import { release } from '@/lib/site/release'

/** Server-rendered QR so a desktop visitor can scan the APK link with their phone. */
export default async function DownloadQr({ size = 132 }: { size?: number }) {
  const abs = new URL(release.url, 'https://habitiq.app').toString()
  const svg = await QRCode.toString(abs, { type: 'svg', margin: 1, color: { dark: '#1C2A40', light: '#FFFFFF' } })
  return (
    <figure style={{ margin: 0, width: size }} data-download-qr={abs}>
      <div style={{ width: size, height: size, borderRadius: 16, overflow: 'hidden', background: '#fff' }}
        role="img" aria-label="QR code to download Oddroof for Android"
        dangerouslySetInnerHTML={{ __html: svg }} />
      <figcaption style={{ fontSize: 12, marginTop: 8, opacity: .85 }}>Scan to download on your phone</figcaption>
    </figure>
  )
}
```

- [ ] **Step 8: Temporary harness on Home**

Replace `apps/web/app/(site)/page.tsx` (Task 5 replaces it again):

```tsx
import Glass from '@/components/site/Glass'
import DownloadButton from '@/components/site/DownloadButton'
export default function HomePage() {
  return (
    <main>
      <h1>Your flat, sorted.</h1>
      <Glass tier="canvas"><DownloadButton showMeta /></Glass>
    </main>
  )
}
```

- [ ] **Step 9: Run tests to verify they pass**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop`
Expected: all passed (5).

- [ ] **Step 10: Commit**

```bash
git add apps/web/components/site apps/web/lib/site apps/web/public/downloads/version.json apps/web/.gitignore apps/web/app/\(site\)/page.tsx apps/web/tests/site.spec.ts
git commit -m "feat(site): motion helpers, glass tiers with fallback, buttons, sections, APK download + QR"
```

---

### Task 3: Navbar and Footer

**Files:**
- Create: `apps/web/components/site/Navbar.tsx`, `apps/web/components/site/Footer.tsx`
- Modify: `apps/web/components/site/site.module.css` (append), `apps/web/app/(site)/layout.tsx`, `apps/web/tests/site.spec.ts`

**Interfaces:**
- Consumes: `PRODUCT_LINKS`, `NAV_LINKS` (Task 1); `Glass`, `DownloadButton`, `SiteButton` (Task 2).
- Produces: `<Navbar />` (client), `<Footer />` (server), both rendered by the site layout around `{children}`.

- [ ] **Step 1: Write the failing tests**

Append to `apps/web/tests/site.spec.ts`:

```ts
test.describe('navigation', () => {
  test('navbar shows products, privacy, about and download', async ({ page }) => {
    await page.goto('/')
    const nav = page.getByRole('navigation', { name: 'Main' })
    await expect(nav.getByRole('button', { name: 'Products' })).toBeVisible()
    await nav.getByRole('button', { name: 'Products' }).click()
    await expect(nav.getByRole('link', { name: /Flat manager/ })).toBeVisible()
    await expect(nav.getByRole('link', { name: /Discover/ })).toBeVisible()
    await expect(nav.getByRole('link', { name: /Voice assistant/ })).toBeVisible()
    await expect(nav.getByRole('link', { name: 'Privacy' })).toBeVisible()
    await expect(nav.getByRole('link', { name: 'About' })).toBeVisible()
  })

  test('mobile menu opens and closes @mobile', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 800 })
    await page.goto('/')
    await page.getByRole('button', { name: 'Open menu' }).click()
    const sheet = page.getByRole('dialog', { name: 'Menu' })
    await expect(sheet.getByRole('link', { name: /Discover/ })).toBeVisible()
    await page.keyboard.press('Escape')
    await expect(sheet).toHaveCount(0)
  })

  test('footer has legal links and contact', async ({ page }) => {
    await page.goto('/')
    const footer = page.getByRole('contentinfo')
    for (const name of ['Privacy & safety', 'Privacy policy', 'Terms', 'Safety', 'About']) {
      await expect(footer.getByRole('link', { name })).toBeVisible()
    }
    await expect(footer.getByRole('link', { name: 'hello@habitiq.app' })).toHaveAttribute('href', 'mailto:hello@habitiq.app')
  })
})
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop -g navigation`
Expected: FAIL — navigation "Main" not found.

- [ ] **Step 3: Styles**

Append to `apps/web/components/site/site.module.css`:

```css
.navWrap { position: fixed; top: 14px; left: 14px; right: 14px; z-index: 50; display: flex; justify-content: center; pointer-events: none; }
.nav {
  pointer-events: auto; width: 100%; max-width: var(--or-max);
  display: flex; align-items: center; gap: 12px; padding: 10px 10px 10px 20px; border-radius: 999px;
  transition: background-color .25s var(--or-ease), box-shadow .25s var(--or-ease);
}
.navScrolled { background: rgba(255,255,255,.82) !important; color: var(--or-text) !important; box-shadow: 0 10px 40px rgba(15,23,42,.12) !important; }
.logo { display: inline-flex; align-items: center; gap: 8px; font-family: var(--or-font-display); font-weight: 800; font-size: 20px; color: inherit; text-decoration: none; }
.navLinks { display: none; align-items: center; gap: 4px; margin-left: auto; }
.navLink, .navTrigger { padding: 10px 14px; border-radius: 999px; color: inherit; text-decoration: none; font-weight: 600; font-size: 15px; background: none; border: 0; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; min-height: 44px; }
.navLink:hover, .navTrigger:hover { background: rgba(127,127,127,.14); }
.menu { position: absolute; top: calc(100% + 10px); min-width: 300px; padding: 8px; border-radius: 20px; }
.menuItem { display: block; padding: 12px 14px; border-radius: 14px; color: var(--or-text); text-decoration: none; }
.menuItem:hover { background: var(--or-canvas); }
.menuItem small { display: block; color: var(--or-text-2); font-size: 13px; }
.navCta { display: none; }
.burger { margin-left: auto; width: 44px; height: 44px; border-radius: 999px; border: 0; background: rgba(127,127,127,.18); color: inherit; display: grid; place-items: center; cursor: pointer; }
.sheet { position: fixed; inset: 0; z-index: 60; background: rgba(15,23,42,.45); display: flex; justify-content: flex-end; }
.sheetPanel { width: min(360px, 100%); height: 100%; background: #fff; color: var(--or-text); padding: 24px; display: flex; flex-direction: column; gap: 6px; overflow-y: auto; }
.sheetPanel a { padding: 14px 6px; color: var(--or-text); text-decoration: none; font-weight: 600; border-bottom: 1px solid var(--or-line); }
@media (min-width: 900px) {
  .navLinks, .navCta { display: flex; }
  .burger { display: none; }
}

.footer { background: var(--or-navy); color: #C9D4E8; padding: 64px var(--or-gutter) 40px; }
.footerGrid { max-width: var(--or-max); margin: 0 auto; display: grid; gap: 32px; grid-template-columns: 1fr; }
.footer h3 { color: #fff; font-size: 14px; letter-spacing: .12em; text-transform: uppercase; margin: 0 0 12px; }
.footer a { color: #C9D4E8; text-decoration: none; display: inline-block; padding: 6px 0; min-height: 32px; }
.footer a:hover { color: #fff; }
.footerBottom { max-width: var(--or-max); margin: 40px auto 0; padding-top: 24px; border-top: 1px solid rgba(255,255,255,.1); font-size: 14px; }
@media (min-width: 768px) { .footerGrid { grid-template-columns: 2fr 1fr 1fr 1fr; } }
```

- [ ] **Step 4: Navbar**

`apps/web/components/site/Navbar.tsx`:

```tsx
'use client'
import Link from 'next/link'
import { useEffect, useRef, useState } from 'react'
import { ChevronDown, Menu, X, Download, Home } from 'lucide-react'
import { AnimatePresence, motion, useMotionValueEvent, useScroll } from 'framer-motion'
import Glass from './Glass'
import { NAV_LINKS, PRODUCT_LINKS } from '@/lib/site/routes'
import { release } from '@/lib/site/release'
import s from './site.module.css'
import { EASE } from './motion'

export default function Navbar() {
  const { scrollY } = useScroll()
  const [scrolled, setScrolled] = useState(false)
  const [productsOpen, setProductsOpen] = useState(false)
  const [sheetOpen, setSheetOpen] = useState(false)
  const productsRef = useRef<HTMLDivElement>(null)
  useMotionValueEvent(scrollY, 'change', v => setScrolled(v > 40))

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') { setProductsOpen(false); setSheetOpen(false) } }
    const onClick = (e: MouseEvent) => { if (!productsRef.current?.contains(e.target as Node)) setProductsOpen(false) }
    window.addEventListener('keydown', onKey)
    window.addEventListener('mousedown', onClick)
    return () => { window.removeEventListener('keydown', onKey); window.removeEventListener('mousedown', onClick) }
  }, [])

  return (
    <div className={s.navWrap}>
      <Glass tier="photo" as="nav" aria-label="Main" className={`${s.nav} ${scrolled ? s.navScrolled : ''}`}>
        <Link href="/" className={s.logo} aria-label="Oddroof home"><Home size={22} aria-hidden />Oddroof</Link>
        <div className={s.navLinks}>
          <div ref={productsRef} style={{ position: 'relative' }}>
            <button className={s.navTrigger} aria-expanded={productsOpen} onClick={() => setProductsOpen(o => !o)}>
              Products <ChevronDown size={16} aria-hidden />
            </button>
            <AnimatePresence>
              {productsOpen && (
                <motion.div initial={{ opacity: 0, y: -6 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -6 }}
                  transition={{ duration: 0.2, ease: EASE }}>
                  <Glass tier="canvas" className={s.menu}>
                    {PRODUCT_LINKS.map(p => (
                      <Link key={p.href} href={p.href} className={s.menuItem} onClick={() => setProductsOpen(false)}>
                        {p.label}<small>{p.blurb}</small>
                      </Link>
                    ))}
                  </Glass>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
          {NAV_LINKS.map(l => <Link key={l.href} href={l.href} className={s.navLink}>{l.label}</Link>)}
        </div>
        <a href={release.url} download className={`${s.btn} ${s.primary} ${s.navCta}`} data-download="">
          <Download size={18} aria-hidden />Download
        </a>
        <button className={s.burger} aria-label="Open menu" onClick={() => setSheetOpen(true)}><Menu size={22} aria-hidden /></button>
      </Glass>

      <AnimatePresence>
        {sheetOpen && (
          <motion.div className={s.sheet} style={{ pointerEvents: 'auto' }} initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            onClick={() => setSheetOpen(false)}>
            <motion.div role="dialog" aria-modal="true" aria-label="Menu" className={s.sheetPanel}
              initial={{ x: 40 }} animate={{ x: 0 }} exit={{ x: 40 }} transition={{ duration: 0.25, ease: EASE }}
              onClick={e => e.stopPropagation()}>
              <button className={s.burger} style={{ marginLeft: 'auto' }} aria-label="Close menu" onClick={() => setSheetOpen(false)}><X size={22} aria-hidden /></button>
              {PRODUCT_LINKS.map(p => <Link key={p.href} href={p.href} onClick={() => setSheetOpen(false)}>{p.label}</Link>)}
              {NAV_LINKS.map(l => <Link key={l.href} href={l.href} onClick={() => setSheetOpen(false)}>{l.label}</Link>)}
              <a href={release.url} download className={`${s.btn} ${s.primary}`} style={{ marginTop: 16, border: 0 }} data-download="">
                <Download size={18} aria-hidden />Download for Android
              </a>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}
```

- [ ] **Step 5: Footer**

`apps/web/components/site/Footer.tsx`:

```tsx
import Link from 'next/link'
import { PRODUCT_LINKS } from '@/lib/site/routes'
import s from './site.module.css'

export default function Footer() {
  return (
    <footer className={s.footer}>
      <div className={s.footerGrid}>
        <div>
          <p className={s.logo} style={{ color: '#fff' }}>Oddroof</p>
          <p style={{ maxWidth: '36ch', marginTop: 12 }}>A calmer shared flat: fair tasks, clear money, and safe ways to find your next flatmate.</p>
        </div>
        <div>
          <h3>Products</h3>
          {PRODUCT_LINKS.map(p => <div key={p.href}><Link href={p.href}>{p.label}</Link></div>)}
        </div>
        <div>
          <h3>Trust</h3>
          <div><Link href="/privacy-and-safety">Privacy &amp; safety</Link></div>
          <div><Link href="/privacy">Privacy policy</Link></div>
          <div><Link href="/terms">Terms</Link></div>
          <div><Link href="/safety">Safety</Link></div>
        </div>
        <div>
          <h3>Company</h3>
          <div><Link href="/about">About</Link></div>
          <div><a href="mailto:hello@habitiq.app">hello@habitiq.app</a></div>
        </div>
      </div>
      <p className={s.footerBottom}>© 2026 Oddroof. Made in India.</p>
    </footer>
  )
}
```

- [ ] **Step 6: Mount in the site layout**

In `apps/web/app/(site)/layout.tsx` replace the return body:

```tsx
import Navbar from '@/components/site/Navbar'
import Footer from '@/components/site/Footer'
// ...
    <div className={`or-site ${jakarta.variable}`}>
      <SmoothScroll>
        <Navbar />
        {children}
        <Footer />
      </SmoothScroll>
    </div>
```

- [ ] **Step 7: Run tests to verify they pass**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop` and `npx playwright test tests/site.spec.ts --project=mobile-chrome`
Expected: all passed.

- [ ] **Step 8: Commit**

```bash
git add apps/web/components/site apps/web/app/\(site\)/layout.tsx apps/web/tests/site.spec.ts
git commit -m "feat(site): floating glass navbar with products menu and mobile sheet; footer"
```

---

### Task 4: Mockup kit — phone frame, Home screen, voice orb

**Files:**
- Create: `apps/web/components/site/mockups/PhoneFrame.tsx`, `apps/web/components/site/mockups/HomeScreen.tsx`, `apps/web/components/site/mockups/VoiceOrb.tsx`, `apps/web/components/site/mockups/mockups.module.css`
- Modify: `apps/web/app/(site)/page.tsx` (harness), `apps/web/tests/site.spec.ts`

**Interfaces:**
- Consumes: `useLoopStep` (Task 2), `EASE` (Task 2).
- Produces: `<PhoneFrame label: string className?>{screen}</PhoneFrame>` (renders `role="img"` with `aria-label={label}`, 9:19.5 ratio, width set by parent); `<HomeScreen />` (Oddroof Home mock with a looping task tick); `<VoiceOrb size?: number listening?: boolean />`.

- [ ] **Step 1: Write the failing test**

Append to `apps/web/tests/site.spec.ts`:

```ts
test('home phone mockup is labelled and never wider than the viewport @mobile', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 800 })
  await page.goto('/')
  const phone = page.getByRole('img', { name: /Oddroof home screen/ })
  await expect(phone).toBeVisible()
  const box = await phone.boundingBox()
  expect(box!.width).toBeLessThanOrEqual(375 - 32)
})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop -g "phone mockup"`
Expected: FAIL — no img named "Oddroof home screen".

- [ ] **Step 3: Styles**

`apps/web/components/site/mockups/mockups.module.css`:

```css
.phone {
  position: relative; width: 100%; aspect-ratio: 9 / 19.5; border-radius: 13% / 6%;
  background: #0B0F1A; padding: 3.2%; box-shadow: 0 40px 90px rgba(10,14,30,.45), inset 0 0 0 2px rgba(255,255,255,.08);
}
.screen { position: relative; width: 100%; height: 100%; border-radius: 10.5% / 4.9%; overflow: hidden; background: #F4F7FB; font-size: clamp(9px, 2.6cqw, 13px); container-type: inline-size; }
.notch { position: absolute; top: 1.6%; left: 50%; transform: translateX(-50%); width: 30%; height: 2.6%; border-radius: 999px; background: #0B0F1A; z-index: 3; }

.homeHero { height: 46%; padding: 14% 7% 0; color: #fff; position: relative; }
.homeHero::after { content: ''; position: absolute; inset: 0; background: linear-gradient(180deg, rgba(10,8,20,.25), rgba(10,8,20,.05) 40%, rgba(244,247,251,0) 75%, #F4F7FB); z-index: 0; }
.homeHero > * { position: relative; z-index: 1; }
.brandRow { display: flex; align-items: center; gap: 6px; font-weight: 800; font-size: 1.25em; }
.date { margin-top: 12%; font-size: .7em; letter-spacing: .18em; opacity: .9; }
.greet { font-size: 1.9em; font-weight: 800; line-height: 1.05; margin-top: 3%; font-family: var(--or-font-display); }
.glassCard { margin: 4% 5% 0; padding: 4.5% 5%; border-radius: 16px; background: rgba(255,255,255,.2); border: 1px solid rgba(255,255,255,.35); backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px); color: #fff; display: flex; align-items: center; gap: 4%; position: relative; z-index: 1; }
.tile { width: 2.6em; height: 2.6em; border-radius: 12px; display: grid; place-items: center; flex-shrink: 0; }
.panel { margin: 5% 4% 0; background: #fff; border-radius: 18px; padding: 5%; box-shadow: 0 6px 20px rgba(15,23,42,.06); color: #0F172A; }
.panelTitle { font-weight: 800; font-size: 1.15em; display: flex; justify-content: space-between; }
.taskRow { display: flex; align-items: center; gap: 4%; padding: 3.5% 0; border-bottom: 1px solid #EEF2F7; }
.check { width: 1.6em; height: 1.6em; border-radius: 999px; border: 2px solid #CBD5E1; display: grid; place-items: center; transition: background-color .3s var(--or-ease), border-color .3s var(--or-ease); }
.checkOn { background: var(--or-teal); border-color: var(--or-teal); color: #fff; }
.muted { color: #64748B; font-size: .8em; }
.navBar { position: absolute; left: 4%; right: 4%; bottom: 3%; height: 9%; border-radius: 20px; background: rgba(255,255,255,.9); box-shadow: 0 8px 24px rgba(15,23,42,.12); display: flex; align-items: center; justify-content: space-around; color: #94A3B8; font-size: .7em; }
.navMic { width: 3.4em; height: 3.4em; margin-top: -12%; border-radius: 999px; background: radial-gradient(circle at 35% 30%, #6F9BFF, var(--or-blue) 55%, #1C46C4); box-shadow: 0 8px 24px rgba(47,107,255,.5); display: grid; place-items: center; color: #fff; }

.orb { position: relative; border-radius: 999px; display: grid; place-items: center; color: #fff;
  background: radial-gradient(circle at 35% 28%, #A8C4FF 0%, #5B8CFF 28%, var(--or-blue) 55%, #1739A8 100%);
  box-shadow: 0 0 60px rgba(47,107,255,.55), inset 0 -10px 30px rgba(10,30,120,.45), inset 0 8px 20px rgba(255,255,255,.35); }
.orbRing { position: absolute; inset: -14%; border-radius: 999px; border: 1px solid rgba(140,175,255,.45); }
.bars { display: flex; gap: 6%; align-items: center; height: 34%; }
.bar { width: 7%; min-width: 3px; height: 100%; border-radius: 999px; background: #fff; transform-origin: center; }
```

- [ ] **Step 4: PhoneFrame**

`apps/web/components/site/mockups/PhoneFrame.tsx`:

```tsx
import m from './mockups.module.css'

export default function PhoneFrame({ label, className = '', children }: { label: string; className?: string; children: React.ReactNode }) {
  return (
    <div role="img" aria-label={label} className={`${m.phone} ${className}`}>
      <div className={m.screen} aria-hidden>
        <div className={m.notch} />
        {children}
      </div>
    </div>
  )
}
```

- [ ] **Step 5: VoiceOrb**

`apps/web/components/site/mockups/VoiceOrb.tsx`:

```tsx
'use client'
import { motion, useReducedMotion } from 'framer-motion'
import m from './mockups.module.css'

const HEIGHTS = [0.45, 0.8, 1, 0.7, 0.5]

export default function VoiceOrb({ size = 120, listening = true }: { size?: number; listening?: boolean }) {
  const reduce = useReducedMotion()
  const live = listening && !reduce
  return (
    <motion.div className={m.orb} style={{ width: size, height: size }}
      animate={live ? { scale: [1, 1.04, 1] } : undefined}
      transition={live ? { duration: 2.4, repeat: Infinity, ease: 'easeInOut' } : undefined}>
      <span className={m.orbRing} />
      <div className={m.bars} style={{ width: '42%' }}>
        {HEIGHTS.map((h, i) => (
          <motion.span key={i} className={m.bar} style={{ scaleY: h }}
            animate={live ? { scaleY: [h, Math.max(0.25, 1.2 - h), h] } : undefined}
            transition={live ? { duration: 0.9 + i * 0.12, repeat: Infinity, ease: 'easeInOut' } : undefined} />
        ))}
      </div>
    </motion.div>
  )
}
```

- [ ] **Step 6: HomeScreen**

`apps/web/components/site/mockups/HomeScreen.tsx`:

```tsx
'use client'
import { Home, Check, IndianRupee, Users, AudioLines } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import m from './mockups.module.css'

const TASKS = [
  { name: 'Take out the trash', who: 'You · today' },
  { name: 'Buy drinking water', who: 'Ravi · today' },
  { name: 'Clean the kitchen', who: 'Meera · tomorrow' },
]

export default function HomeScreen() {
  const { ref, step } = useLoopStep(TASKS.length + 1, 1800)
  return (
    <div ref={ref} style={{ height: '100%' }}>
      <div className={`${m.homeHero} or-golden`}>
        <div className={m.brandRow}><Home size="1.1em" aria-hidden />Oddroof</div>
        <div className={m.date}>SAT, 3 OCTOBER</div>
        <div className={m.greet}>Good evening,<br />Sai</div>
      </div>
      <div className={m.glassCard} style={{ marginTop: '-22%' }}>
        <span className={m.tile} style={{ background: 'rgba(20,184,166,.85)' }}><Users size="1.2em" aria-hidden /></span>
        <div><div className={m.muted} style={{ color: 'rgba(255,255,255,.8)' }}>YOUR FLAT</div><b>Sai flat</b></div>
        <span style={{ marginLeft: 'auto', fontSize: '.8em' }}>5 members</span>
      </div>
      <div className={m.glassCard}>
        <span className={m.tile} style={{ background: 'rgba(255,255,255,.25)' }}><IndianRupee size="1.2em" aria-hidden /></span>
        <div><b>All settled</b><div style={{ fontSize: '.8em', opacity: .85 }}>No dues</div></div>
      </div>
      <div className={m.panel}>
        <div className={m.panelTitle}><span>Today&apos;s tasks</span><span style={{ color: 'var(--or-teal)', fontSize: '.8em' }}>See all</span></div>
        {TASKS.map((t, i) => (
          <div key={t.name} className={m.taskRow}>
            <span className={`${m.check} ${step > i ? m.checkOn : ''}`}>{step > i && <Check size="0.9em" aria-hidden />}</span>
            <div><div style={{ fontWeight: 600, textDecoration: step > i ? 'line-through' : 'none' }}>{t.name}</div><div className={m.muted}>{t.who}</div></div>
          </div>
        ))}
      </div>
      <div className={m.navBar}>
        <span>Home</span><span>Tasks</span>
        <span className={m.navMic}><AudioLines size="1.3em" aria-hidden /></span>
        <span>Discover</span><span>Profile</span>
      </div>
    </div>
  )
}
```

- [ ] **Step 7: Harness on Home**

Update `apps/web/app/(site)/page.tsx` to also render (Task 5 replaces the whole file):

```tsx
import PhoneFrame from '@/components/site/mockups/PhoneFrame'
import HomeScreen from '@/components/site/mockups/HomeScreen'
// inside <main>:
<div style={{ width: 'min(320px, calc(100vw - 48px))', margin: '120px auto' }}>
  <PhoneFrame label="Oddroof home screen showing today's tasks being completed"><HomeScreen /></PhoneFrame>
</div>
```

- [ ] **Step 8: Run tests to verify they pass**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop` and `--project=mobile-chrome`
Expected: all passed.

- [ ] **Step 9: Commit**

```bash
git add apps/web/components/site/mockups apps/web/app/\(site\)/page.tsx apps/web/tests/site.spec.ts
git commit -m "feat(site): phone frame, live Home screen mockup, breathing voice orb"
```

---

### Task 5: Hero section and Home composition

**Files:**
- Create: `apps/web/components/site/home/Hero.tsx`, `apps/web/components/site/home/home.module.css`
- Modify: `apps/web/app/(site)/page.tsx` (final composition, sections added by later tasks), `apps/web/tests/site.spec.ts`

**Interfaces:**
- Consumes: `Glass`, `DownloadButton`, `DownloadQr`, `SiteButton`, `PhoneFrame`, `HomeScreen`, `VoiceOrb`, `EASE`.
- Produces: `<Hero />`; Home page file that later tasks extend by importing their section and adding it in order. Hero photo path constant `HERO_PHOTO = '/site/hero-golden-hour.jpg'` exported from `Hero.tsx`.

- [ ] **Step 1: Write the failing tests**

Append to `apps/web/tests/site.spec.ts`:

```ts
test.describe('hero', () => {
  test('headline, download with meta, and see-how link', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByRole('heading', { level: 1, name: 'Your flat, sorted.' })).toBeVisible()
    await expect(page.locator('#hero [data-download]')).toBeVisible()
    await expect(page.locator('#hero [data-download-meta]')).toBeVisible()
    await expect(page.getByRole('link', { name: 'See how it works' })).toHaveAttribute('href', '#how-it-works')
  })

  test('QR shows on desktop only', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 })
    await page.goto('/')
    await expect(page.locator('#hero [data-download-qr]')).toBeVisible()
    await page.setViewportSize({ width: 375, height: 800 })
    await expect(page.locator('#hero [data-download-qr]')).toBeHidden()
  })
})
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop -g hero`
Expected: FAIL — no `#hero`.

- [ ] **Step 3: Styles**

`apps/web/components/site/home/home.module.css`:

```css
.hero { position: relative; min-height: 100svh; color: #fff; overflow: hidden; display: flex; align-items: center; padding: 120px var(--or-gutter) 72px; }
.heroBg { position: absolute; inset: 0; z-index: 0; }
.heroBg img { object-fit: cover; }
.heroScrim { position: absolute; inset: 0; background: linear-gradient(100deg, rgba(14,10,28,.72) 0%, rgba(14,10,28,.45) 45%, rgba(14,10,28,.1) 75%); z-index: 1; }
.heroInner { position: relative; z-index: 2; width: 100%; max-width: var(--or-max); margin: 0 auto; display: grid; gap: 48px; grid-template-columns: 1fr; align-items: center; }
.heroCopy h1 { font-size: clamp(40px, 7vw, 76px); margin: 0; max-width: 11ch; }
.heroCopy p.sub { margin-top: 20px; font-size: clamp(17px, 1.8vw, 21px); max-width: 34ch; color: rgba(255,255,255,.9); }
.ctaRow { display: flex; flex-wrap: wrap; gap: 14px; margin-top: 32px; align-items: flex-start; }
.qr { display: none; margin-top: 28px; }
.heroPhone { position: relative; width: min(320px, calc(100vw - 64px)); margin: 0 auto; }
.heroOrb { position: absolute; right: -14%; bottom: 16%; z-index: 3; }
.floatCard { padding: 12px 16px; font-size: 14px; display: flex; gap: 10px; align-items: center; white-space: nowrap; }
.floatA { position: absolute; z-index: 3; left: -18%; top: 18%; }
.floatB { position: absolute; z-index: 3; right: -10%; top: 46%; }
@media (max-width: 767px) { .floatA, .floatB, .heroOrb { display: none; } }
@media (min-width: 1024px) {
  .heroInner { grid-template-columns: 1.1fr .9fr; }
  .qr { display: block; }
}
```

- [ ] **Step 4: Hero**

`apps/web/components/site/home/Hero.tsx`:

```tsx
'use client'
import Image from 'next/image'
import { useRef } from 'react'
import { motion, useScroll, useTransform, useReducedMotion } from 'framer-motion'
import { CheckCircle2, IndianRupee } from 'lucide-react'
import Glass from '../Glass'
import SiteButton from '../SiteButton'
import PhoneFrame from '../mockups/PhoneFrame'
import HomeScreen from '../mockups/HomeScreen'
import VoiceOrb from '../mockups/VoiceOrb'
import { EASE, fadeUp, staggerKids } from '../motion'
import h from './home.module.css'

export const HERO_PHOTO = '/site/hero-golden-hour.jpg'

export default function Hero({ download, qr, hasPhoto }: { download: React.ReactNode; qr: React.ReactNode; hasPhoto: boolean }) {
  const ref = useRef<HTMLElement>(null)
  const reduce = useReducedMotion()
  const { scrollYProgress } = useScroll({ target: ref, offset: ['start start', 'end start'] })
  const tilt = useTransform(scrollYProgress, [0, 1], [0, reduce ? 0 : -8])
  const lift = useTransform(scrollYProgress, [0, 1], [0, reduce ? 0 : -60])

  return (
    <section id="hero" ref={ref} className={h.hero}>
      <div className={`${h.heroBg} or-golden`}>
        {hasPhoto && <Image src={HERO_PHOTO} alt="" fill priority sizes="100vw" />}
      </div>
      <div className={h.heroScrim} />
      <div className={h.heroInner}>
        <motion.div className={h.heroCopy} variants={staggerKids(0.08)} initial="hidden" animate="show">
          <motion.h1 variants={fadeUp}>Your flat, sorted.</motion.h1>
          <motion.p variants={fadeUp} className={h.sub}>
            Fair turns for chores, clear money between flatmates, and a voice assistant that just gets it done.
          </motion.p>
          <motion.div variants={fadeUp} className={h.ctaRow}>
            {download}
            <SiteButton href="#how-it-works" variant="ghost">See how it works</SiteButton>
          </motion.div>
          <motion.div variants={fadeUp} className={h.qr}>{qr}</motion.div>
        </motion.div>

        {/* Outer layer follows scroll (tilt/lift); inner layer plays the one-time entrance, so the two y values never fight. */}
        <motion.div className={h.heroPhone} style={{ rotate: tilt, y: lift }}>
          <motion.div initial={{ opacity: 0, y: 40 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.9, ease: EASE, delay: 0.2 }}>
            <PhoneFrame label="Oddroof home screen showing today's tasks being completed"><HomeScreen /></PhoneFrame>
          </motion.div>
          <motion.div className={h.floatA}
            initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.7, ease: EASE, delay: 0.7 }}>
            <Glass tier="photo" className={h.floatCard}><CheckCircle2 size={18} aria-hidden />Trash: Ravi&apos;s turn next</Glass>
          </motion.div>
          <motion.div className={h.floatB}
            initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.7, ease: EASE, delay: 0.85 }}>
            <Glass tier="photo" className={h.floatCard}><IndianRupee size={18} aria-hidden />₹480 split 4 ways</Glass>
          </motion.div>
          <div className={h.heroOrb}><VoiceOrb size={96} /></div>
        </motion.div>
      </div>
    </section>
  )
}
```

Note: the `.floatA`/`.floatB` wrappers own the absolute position and are hidden below 768 px, so the floating cards cannot cause sideways scroll on phones.

- [ ] **Step 5: Compose Home**

Replace `apps/web/app/(site)/page.tsx`:

```tsx
import fs from 'node:fs'
import path from 'node:path'
import Hero, { HERO_PHOTO } from '@/components/site/home/Hero'
import DownloadButton from '@/components/site/DownloadButton'
import DownloadQr from '@/components/site/DownloadQr'

const hasPhoto = (p: string) => fs.existsSync(path.join(process.cwd(), 'public', p))

export default function HomePage() {
  return (
    <main>
      <Hero download={<DownloadButton showMeta />} qr={<DownloadQr />} hasPhoto={hasPhoto(HERO_PHOTO)} />
      {/* Sections from Tasks 6–10 are inserted here in order: Problem, PinnedStory, Voice, Discover, Trust, Install, FinalCta */}
    </main>
  )
}
```

(The JSX comment documents insertion order for Tasks 6–10; each of those tasks adds exactly one line here.)

- [ ] **Step 6: Run tests to verify they pass**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop` and `--project=mobile-chrome`
Expected: all passed.

- [ ] **Step 7: Commit**

```bash
git add apps/web/components/site/home apps/web/app/\(site\)/page.tsx apps/web/tests/site.spec.ts
git commit -m "feat(site): golden-hour hero with live phone, floating glass cards, orb and scroll tilt"
```

---

### Task 6: Problem section — chat bubbles fold into one card

**Files:**
- Create: `apps/web/components/site/mockups/ChatBubbles.tsx`, `apps/web/components/site/home/Problem.tsx`
- Modify: `apps/web/components/site/home/home.module.css` (append), `apps/web/app/(site)/page.tsx`, `apps/web/tests/site.spec.ts`

**Interfaces:**
- Consumes: `Section`, `Glass`, `EASE`.
- Produces: `<ChatBubbles />` (scroll-linked fold), `<Problem />` section with `id="problem"`.

- [ ] **Step 1: Write the failing test**

```ts
test('problem section names the WhatsApp chaos and resolves it', async ({ page }) => {
  await page.goto('/')
  const sec = page.locator('#problem')
  await sec.scrollIntoViewIfNeeded()
  await expect(sec.getByRole('heading', { name: /WhatsApp group is doing too much/ })).toBeVisible()
  await expect(sec.getByText('Who bought milk?')).toBeAttached()
  await expect(sec.getByText('One place for the whole flat')).toBeVisible()
})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop -g "problem section"`
Expected: FAIL — `#problem` missing.

- [ ] **Step 3: Styles**

Append to `apps/web/components/site/home/home.module.css`:

```css
.problemStage { position: relative; margin-top: 48px; min-height: 420px; display: grid; place-items: center; }
.bubble { position: absolute; padding: 12px 16px; border-radius: 18px 18px 18px 6px; background: #fff; box-shadow: 0 8px 24px rgba(15,23,42,.08); font-size: 15px; max-width: 220px; border: 1px solid var(--or-line); }
.bubbleMine { background: #DCF8C6; border-radius: 18px 18px 6px 18px; }
.resolved { position: relative; z-index: 2; width: min(420px, 100%); padding: 24px; }
.resolvedRow { display: flex; gap: 12px; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--or-line); }
```

- [ ] **Step 4: ChatBubbles**

`apps/web/components/site/mockups/ChatBubbles.tsx`:

```tsx
'use client'
import { useRef } from 'react'
import { motion, useScroll, useTransform, useReducedMotion, type MotionValue } from 'framer-motion'
import { CheckCircle2, IndianRupee, Receipt } from 'lucide-react'
import Glass from '../Glass'
import h from '../home/home.module.css'

const BUBBLES = [
  { text: 'Who bought milk?', x: '-38%', y: '-150%', mine: false },
  { text: 'Whose turn for trash today??', x: '30%', y: '-170%', mine: true },
  { text: 'Wifi bill ₹1,200, pay me pls', x: '-44%', y: '60%', mine: false },
  { text: 'I cleaned the kitchen last time', x: '36%', y: '90%', mine: true },
  { text: 'Who owes who now?', x: '-6%', y: '-230%', mine: false },
]

function Bubble({ b, progress, i }: { b: typeof BUBBLES[number]; progress: MotionValue<number>; i: number }) {
  const reduce = useReducedMotion()
  const opacity = useTransform(progress, [0, 0.45 + i * 0.03, 0.7], [1, 1, reduce ? 1 : 0])
  const scale = useTransform(progress, [0.4, 0.7], [1, reduce ? 1 : 0.6])
  return (
    <motion.div className={`${h.bubble} ${b.mine ? h.bubbleMine : ''}`}
      style={{ left: '50%', top: '50%', translateX: b.x, translateY: b.y, opacity, scale }}>
      {b.text}
    </motion.div>
  )
}

export default function ChatBubbles() {
  const ref = useRef<HTMLDivElement>(null)
  const reduce = useReducedMotion()
  const { scrollYProgress } = useScroll({ target: ref, offset: ['start end', 'end start'] })
  const cardOpacity = useTransform(scrollYProgress, [0.45, 0.65], [reduce ? 1 : 0, 1])
  const cardY = useTransform(scrollYProgress, [0.45, 0.65], [reduce ? 0 : 30, 0])
  return (
    <div ref={ref} className={h.problemStage}>
      {BUBBLES.map((b, i) => <Bubble key={b.text} b={b} progress={scrollYProgress} i={i} />)}
      <motion.div style={{ opacity: cardOpacity, y: cardY }} className={h.resolved}>
        <Glass tier="canvas" style={{ padding: 24 }}>
          <p style={{ fontWeight: 800, fontSize: 18, margin: 0 }}>One place for the whole flat</p>
          <div className={h.resolvedRow}><CheckCircle2 color="var(--or-teal)" aria-hidden />Trash today: <b>Ravi</b></div>
          <div className={h.resolvedRow}><IndianRupee color="var(--or-blue)" aria-hidden />Milk ₹60 · split 4 ways</div>
          <div className={h.resolvedRow} style={{ borderBottom: 0 }}><Receipt color="var(--or-amber)" aria-hidden />Wifi ₹1,200 · ₹300 each</div>
        </Glass>
      </motion.div>
    </div>
  )
}
```

- [ ] **Step 5: Problem section**

`apps/web/components/site/home/Problem.tsx`:

```tsx
import Section from '../Section'
import ChatBubbles from '../mockups/ChatBubbles'

export default function Problem() {
  return (
    <Section id="problem" tone="white" eyebrow="The problem"
      title="Your flat's WhatsApp group is doing too much."
      lede="Chores, money and reminders get lost between memes and forwards. Oddroof gives each of them a proper home.">
      <ChatBubbles />
    </Section>
  )
}
```

In `apps/web/app/(site)/page.tsx` import `Problem` and render `<Problem />` directly after `<Hero ... />`.

- [ ] **Step 6: Run tests to verify they pass**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop` and `--project=mobile-chrome`
Expected: all passed.

- [ ] **Step 7: Commit**

```bash
git add apps/web/components/site apps/web/app/\(site\)/page.tsx apps/web/tests/site.spec.ts
git commit -m "feat(site): problem section with chat bubbles folding into one flat card"
```

---

### Task 7: Pinned phone story — tasks, expenses, bills, away

**Files:**
- Create: `apps/web/components/site/mockups/TasksScreen.tsx`, `apps/web/components/site/mockups/ExpenseScreen.tsx`, `apps/web/components/site/mockups/BillsScreen.tsx`, `apps/web/components/site/mockups/AwayScreen.tsx`, `apps/web/components/site/home/PinnedStory.tsx`
- Modify: `apps/web/components/site/mockups/mockups.module.css` (append), `apps/web/components/site/home/home.module.css` (append), `apps/web/app/(site)/page.tsx`, `apps/web/tests/site.spec.ts`

**Interfaces:**
- Consumes: `PhoneFrame`, `useLoopStep`, `Section`, `EASE`.
- Produces: four screen components with no props; `<PinnedStory />` with `id="how-it-works"` and one `<article data-story-step>` per step.

- [ ] **Step 1: Write the failing tests**

```ts
test.describe('pinned story', () => {
  test('four steps in order', async ({ page }) => {
    await page.goto('/')
    const steps = page.locator('#how-it-works [data-story-step] h3')
    await expect(steps).toHaveText(['Tasks rotate fairly', 'Expenses split themselves', 'Bills and month close', 'Away mode'])
  })

  test('phone screen follows the step on desktop', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 })
    await page.goto('/')
    await page.locator('#how-it-works [data-story-step]').nth(1).scrollIntoViewIfNeeded()
    await expect(page.locator('#how-it-works [data-active-screen]')).toHaveAttribute('data-active-screen', '1', { timeout: 4000 })
  })
})
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop -g "pinned story"`
Expected: FAIL — `#how-it-works` missing.

- [ ] **Step 3: Styles**

Append to `apps/web/components/site/mockups/mockups.module.css`:

```css
.appBar { padding: 16% 6% 4%; font-weight: 800; font-size: 1.35em; font-family: var(--or-font-display); color: #0F172A; }
.list { padding: 0 4%; display: grid; gap: 3%; }
.card { background: #fff; border-radius: 16px; padding: 5%; box-shadow: 0 4px 14px rgba(15,23,42,.06); display: flex; align-items: center; gap: 4%; color: #0F172A; }
.chip { margin-left: auto; padding: .3em .7em; border-radius: 999px; font-size: .75em; font-weight: 700; }
.avatar { width: 2.2em; height: 2.2em; border-radius: 999px; display: grid; place-items: center; font-weight: 800; color: #fff; flex-shrink: 0; font-size: .85em; }
.big { font-size: 2.2em; font-weight: 800; font-family: var(--or-font-display); }
.split { display: grid; gap: 2%; margin-top: 4%; }
.splitRow { display: flex; justify-content: space-between; padding: 3% 0; border-bottom: 1px solid #EEF2F7; }
.toggle { margin-left: auto; width: 2.8em; height: 1.6em; border-radius: 999px; background: #CBD5E1; position: relative; transition: background-color .3s var(--or-ease); }
.toggle::after { content: ''; position: absolute; top: .2em; left: .2em; width: 1.2em; height: 1.2em; border-radius: 999px; background: #fff; transition: transform .3s var(--or-ease); }
.toggleOn { background: var(--or-violet); }
.toggleOn::after { transform: translateX(1.2em); }
```

Append to `apps/web/components/site/home/home.module.css`:

```css
.story { display: grid; gap: 40px; margin-top: 56px; }
.storyPhoneCol { display: none; }
.storySteps { display: grid; gap: 24px; }
.step { padding: 28px; }
.step h3 { font-size: clamp(22px, 2.4vw, 30px); margin: 10px 0 8px; }
.step p { color: var(--or-text-2); margin: 0; }
.stepPhone { width: min(280px, 100%); margin: 24px auto 0; }
.stepDot { display: inline-grid; place-items: center; width: 40px; height: 40px; border-radius: 12px; color: #fff; }
@media (min-width: 1024px) {
  .story { grid-template-columns: 1fr 1fr; gap: 64px; }
  .storyPhoneCol { display: block; }
  .storyPhoneSticky { position: sticky; top: 14vh; width: 320px; margin: 0 auto; }
  .storySteps { gap: 0; }
  .step { min-height: 72vh; display: flex; flex-direction: column; justify-content: center; background: none !important; border: 0 !important; box-shadow: none !important; }
  .stepPhone { display: none; }
}
```

- [ ] **Step 4: Four screens**

`apps/web/components/site/mockups/TasksScreen.tsx`:

```tsx
'use client'
import { RefreshCw } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import m from './mockups.module.css'

const PEOPLE = [{ n: 'Sai', c: '#2F6BFF' }, { n: 'Ravi', c: '#14B8A6' }, { n: 'Meera', c: '#8B5CF6' }, { n: 'Arjun', c: '#F5A524' }]
const DAYS = ['Mon', 'Tue', 'Wed', 'Thu']

export default function TasksScreen() {
  const { ref, step } = useLoopStep(PEOPLE.length, 1600)
  return (
    <div ref={ref}>
      <div className={m.appBar}>Trash rotation</div>
      <div className={m.list}>
        {DAYS.map((d, i) => {
          const p = PEOPLE[(i + step) % PEOPLE.length]
          return (
            <div key={d} className={m.card}>
              <span className={m.avatar} style={{ background: p.c, transition: 'background-color .4s' }}>{p.n[0]}</span>
              <div><b>{p.n}</b><div style={{ fontSize: '.8em', color: '#64748B' }}>{d}</div></div>
              {i === 0 && <span className={m.chip} style={{ background: '#E6FAF6', color: '#0B7A6C' }}>Today</span>}
            </div>
          )
        })}
        <div className={m.card} style={{ color: '#475569', fontSize: '.85em' }}><RefreshCw size="1.1em" aria-hidden />Rotates every day, skips anyone away</div>
      </div>
    </div>
  )
}
```

`apps/web/components/site/mockups/ExpenseScreen.tsx`:

```tsx
'use client'
import { useLoopStep } from '../useLoopStep'
import m from './mockups.module.css'

const SHARES = [{ n: 'Sai (paid)', v: 120 }, { n: 'Ravi', v: 120 }, { n: 'Meera', v: 120 }, { n: 'Arjun', v: 120 }]

export default function ExpenseScreen() {
  const { ref, step } = useLoopStep(SHARES.length + 1, 900)
  return (
    <div ref={ref}>
      <div className={m.appBar}>Groceries</div>
      <div className={m.list}>
        <div className={m.card} style={{ display: 'block' }}>
          <div style={{ color: '#64748B', fontSize: '.8em' }}>Paid by Sai</div>
          <div className={m.big}>₹480</div>
          <div className={m.split}>
            {SHARES.map((s, i) => (
              <div key={s.n} className={m.splitRow} style={{ opacity: step > i ? 1 : 0.25, transition: 'opacity .3s' }}>
                <span>{s.n}</span><b>₹{s.v}</b>
              </div>
            ))}
          </div>
        </div>
        <div className={m.card} style={{ fontSize: '.85em' }}>Ravi, Meera and Arjun each owe Sai ₹120</div>
      </div>
    </div>
  )
}
```

`apps/web/components/site/mockups/BillsScreen.tsx`:

```tsx
'use client'
import { Zap, Wifi, Home as HomeIcon } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import m from './mockups.module.css'

const BILLS = [
  { n: 'Rent', a: '₹48,000', icon: HomeIcon, c: '#8B5CF6' },
  { n: 'Electricity', a: '₹2,140', icon: Zap, c: '#F5A524' },
  { n: 'Wifi', a: '₹1,200', icon: Wifi, c: '#2F6BFF' },
]

export default function BillsScreen() {
  const { ref, step } = useLoopStep(BILLS.length + 1, 1300)
  return (
    <div ref={ref}>
      <div className={m.appBar}>October bills</div>
      <div className={m.list}>
        {BILLS.map((b, i) => {
          const paid = step > i
          const Icon = b.icon
          return (
            <div key={b.n} className={m.card}>
              <span className={m.avatar} style={{ background: b.c }}><Icon size="1em" aria-hidden /></span>
              <div><b>{b.n}</b><div style={{ fontSize: '.8em', color: '#64748B' }}>{b.a}</div></div>
              <span className={m.chip} style={{ background: paid ? '#E6FAF6' : '#FFF4E0', color: paid ? '#0B7A6C' : '#9A5B00' }}>{paid ? 'Paid' : 'Due 5th'}</span>
            </div>
          )
        })}
        <div className={m.card} style={{ fontSize: '.85em' }}>{step >= BILLS.length ? 'Month closed. Everyone is settled.' : 'Close the month when all bills are in'}</div>
      </div>
    </div>
  )
}
```

`apps/web/components/site/mockups/AwayScreen.tsx`:

```tsx
'use client'
import { Plane } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import m from './mockups.module.css'

export default function AwayScreen() {
  const { ref, step } = useLoopStep(2, 2200)
  const away = step === 1
  return (
    <div ref={ref}>
      <div className={m.appBar}>Going home for Diwali</div>
      <div className={m.list}>
        <div className={m.card}>
          <span className={m.avatar} style={{ background: '#8B5CF6' }}><Plane size="1em" aria-hidden /></span>
          <div><b>I&apos;m away</b><div style={{ fontSize: '.8em', color: '#64748B' }}>Oct 28 – Nov 3</div></div>
          <span className={`${m.toggle} ${away ? m.toggleOn : ''}`} />
        </div>
        <div className={m.card} style={{ fontSize: '.85em', opacity: away ? 1 : .35, transition: 'opacity .4s' }}>Your trash turns move to Ravi and Meera</div>
        <div className={m.card} style={{ fontSize: '.85em', opacity: away ? 1 : .35, transition: 'opacity .4s' }}>Flatmates see you&apos;re away, no chasing</div>
      </div>
    </div>
  )
}
```

- [ ] **Step 5: PinnedStory**

`apps/web/components/site/home/PinnedStory.tsx`:

```tsx
'use client'
import { useEffect, useRef, useState } from 'react'
import { AnimatePresence, motion, useInView } from 'framer-motion'
import { RefreshCw, IndianRupee, Receipt, Plane } from 'lucide-react'
import Section from '../Section'
import Glass from '../Glass'
import PhoneFrame from '../mockups/PhoneFrame'
import TasksScreen from '../mockups/TasksScreen'
import ExpenseScreen from '../mockups/ExpenseScreen'
import BillsScreen from '../mockups/BillsScreen'
import AwayScreen from '../mockups/AwayScreen'
import { EASE } from '../motion'
import h from './home.module.css'

const STEPS = [
  { title: 'Tasks rotate fairly', body: 'Set a chore once. Oddroof hands it to the next person every time, and skips whoever is away.', icon: RefreshCw, color: 'var(--or-teal)', Screen: TasksScreen, label: 'Trash rotation moving between flatmates' },
  { title: 'Expenses split themselves', body: 'Add what you paid. Everyone sees their share and who owes whom, without a spreadsheet.', icon: IndianRupee, color: 'var(--or-blue)', Screen: ExpenseScreen, label: 'A ₹480 grocery expense split four ways' },
  { title: 'Bills and month close', body: 'Rent, electricity and wifi in one list. Mark them paid and close the month with everyone settled.', icon: Receipt, color: 'var(--or-amber)', Screen: BillsScreen, label: 'Monthly bills being marked paid' },
  { title: 'Away mode', body: 'Going home for a festival? Switch on away and your turns pass on until you are back.', icon: Plane, color: 'var(--or-violet)', Screen: AwayScreen, label: 'Away mode switched on, turns passed to flatmates' },
] as const

function Step({ i, onActive, children }: { i: number; onActive: (i: number) => void; children: React.ReactNode }) {
  const ref = useRef<HTMLElement>(null)
  const inView = useInView(ref, { amount: 0.6 })
  useEffect(() => { if (inView) onActive(i) }, [inView, i, onActive])
  return <article ref={ref} data-story-step={i}>{children}</article>
}

export default function PinnedStory() {
  const [active, setActive] = useState(0)
  const Current = STEPS[active].Screen
  return (
    <Section id="how-it-works" tone="canvas" eyebrow="How it works"
      title="Everything a shared flat argues about, settled."
      lede="Four everyday jobs, handled the same fair way for everyone in the flat.">
      <div className={h.story}>
        <div className={h.storySteps}>
          {STEPS.map((s, i) => {
            const Icon = s.icon
            return (
              <Step key={s.title} i={i} onActive={setActive}>
                <Glass tier="canvas" className={h.step}>
                  <span className={h.stepDot} style={{ background: s.color }}><Icon size={20} aria-hidden /></span>
                  <h3>{s.title}</h3>
                  <p>{s.body}</p>
                  <div className={h.stepPhone}><PhoneFrame label={s.label}><s.Screen /></PhoneFrame></div>
                </Glass>
              </Step>
            )
          })}
        </div>
        <div className={h.storyPhoneCol}>
          <div className={h.storyPhoneSticky} data-active-screen={String(active)}>
            <PhoneFrame label={STEPS[active].label}>
              <AnimatePresence mode="wait">
                <motion.div key={active} style={{ height: '100%' }}
                  initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -16 }}
                  transition={{ duration: 0.35, ease: EASE }}>
                  <Current />
                </motion.div>
              </AnimatePresence>
            </PhoneFrame>
          </div>
        </div>
      </div>
    </Section>
  )
}
```

In `apps/web/app/(site)/page.tsx` import `PinnedStory` and render `<PinnedStory />` after `<Problem />`.

- [ ] **Step 6: Run tests to verify they pass**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop` and `--project=mobile-chrome`
Expected: all passed.

- [ ] **Step 7: Commit**

```bash
git add apps/web/components/site apps/web/app/\(site\)/page.tsx apps/web/tests/site.spec.ts
git commit -m "feat(site): pinned phone story for tasks, expenses, bills and away mode"
```

---

### Task 8: Voice section — orb, waveform, spoken-request demo

**Files:**
- Create: `apps/web/components/site/mockups/VoiceDemo.tsx`, `apps/web/components/site/home/Voice.tsx`
- Modify: `apps/web/components/site/home/home.module.css` (append), `apps/web/app/(site)/page.tsx`, `apps/web/tests/site.spec.ts`

**Interfaces:**
- Consumes: `VoiceOrb`, `Glass`, `Section`, `useLoopStep`.
- Produces: `<VoiceDemo />` (types a phrase, then shows a confirm card), `<Voice />` with `id="voice"`.

- [ ] **Step 1: Write the failing test**

```ts
test('voice section shows the demo request and confirm card', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' }) // final state, deterministic
  await page.goto('/')
  const sec = page.locator('#voice')
  await sec.scrollIntoViewIfNeeded()
  await expect(sec.getByRole('heading', { name: 'Just say it.' })).toBeVisible()
  await expect(sec.getByText('paid Ravi 300 for the cylinder')).toBeVisible()
  await expect(sec.getByText('Record ₹300 you paid Ravi')).toBeVisible()
  await expect(sec.getByText(/Hindi or Telugu/)).toBeVisible()
})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop -g "voice section"`
Expected: FAIL — `#voice` missing.

- [ ] **Step 3: Styles**

Append to `apps/web/components/site/home/home.module.css`:

```css
.voiceGrid { display: grid; gap: 48px; margin-top: 48px; align-items: center; }
.voiceStage { display: grid; justify-items: center; gap: 28px; position: relative; padding: 32px 0; }
.voiceGlow { position: absolute; inset: 10% 20%; background: radial-gradient(closest-side, rgba(47,107,255,.35), transparent); filter: blur(30px); z-index: 0; }
.voiceStage > * { position: relative; z-index: 1; }
.transcript { font-size: clamp(20px, 2.4vw, 28px); font-weight: 700; min-height: 1.4em; text-align: center; }
.caret { display: inline-block; width: 2px; height: 1em; background: #9DB8FF; margin-left: 2px; vertical-align: -.1em; animation: blink 1s steps(1) infinite; }
@keyframes blink { 50% { opacity: 0; } }
.confirm { width: min(380px, 100%); padding: 20px; display: grid; gap: 14px; }
.confirmBtns { display: flex; gap: 10px; }
.confirmBtns span { flex: 1; text-align: center; padding: 10px; border-radius: 999px; font-weight: 700; font-size: 14px; }
.voicePoints { display: grid; gap: 16px; }
.voicePoint { padding: 18px 20px; display: flex; gap: 14px; align-items: flex-start; }
@media (min-width: 1024px) { .voiceGrid { grid-template-columns: 1.1fr .9fr; } }
```

- [ ] **Step 4: VoiceDemo**

`apps/web/components/site/mockups/VoiceDemo.tsx`:

```tsx
'use client'
import { AnimatePresence, motion } from 'framer-motion'
import Glass from '../Glass'
import VoiceOrb from './VoiceOrb'
import { useLoopStep } from '../useLoopStep'
import { EASE } from '../motion'
import h from '../home/home.module.css'

const PHRASE = 'paid Ravi 300 for the cylinder'
const TYPE_STEPS = PHRASE.length
// steps: 0..TYPE_STEPS typing, then hold with card; last step = full phrase + card (reduced-motion final state)
const TOTAL = TYPE_STEPS + 12

export default function VoiceDemo() {
  const { ref, step } = useLoopStep(TOTAL, 70)
  const typed = PHRASE.slice(0, Math.min(step, TYPE_STEPS))
  const showCard = step >= TYPE_STEPS
  return (
    <div ref={ref} className={h.voiceStage}>
      <div className={h.voiceGlow} />
      <VoiceOrb size={150} listening={!showCard} />
      <p className={h.transcript} aria-live="off">
        {typed}{!showCard && <span className={h.caret} aria-hidden />}
      </p>
      <AnimatePresence>
        {showCard && (
          <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0 }}
            transition={{ duration: 0.4, ease: EASE }} style={{ width: '100%', display: 'grid', justifyItems: 'center' }}>
            <Glass tier="photo" className={h.confirm}>
              <b>Record ₹300 you paid Ravi</b>
              <span style={{ opacity: .8, fontSize: 14 }}>Say “yes” or tap Save</span>
              <div className={h.confirmBtns}>
                <span style={{ background: 'rgba(255,255,255,.15)' }}>Cancel</span>
                <span style={{ background: 'var(--or-blue)' }}>Save</span>
              </div>
            </Glass>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}
```

Note: `useLoopStep` returns `TOTAL - 1` under reduced motion, which is ≥ `TYPE_STEPS`, so the full phrase and the card render (the test relies on this).

- [ ] **Step 5: Voice section**

`apps/web/components/site/home/Voice.tsx`:

```tsx
import { Languages, ShieldCheck, Zap } from 'lucide-react'
import Section, { Reveal } from '../Section'
import Glass from '../Glass'
import VoiceDemo from '../mockups/VoiceDemo'
import h from './home.module.css'

const POINTS = [
  { icon: Languages, title: 'Talk the way you talk', body: 'English mixed with Hindi or Telugu works. “Kirana 450 kharcha” is fine.' },
  { icon: ShieldCheck, title: 'Nothing saves without you', body: 'Every change shows as a card first. Say yes or tap Save.' },
  { icon: Zap, title: 'Answers in a second', body: 'Ask what you owe, whose turn it is, or what is due today.' },
]

export default function Voice() {
  return (
    <Section id="voice" tone="dark" eyebrow="Voice assistant" title="Just say it."
      lede="Tap the orb and tell Oddroof what happened. It understands, shows you the result, and saves when you agree.">
      <div className={h.voiceGrid}>
        <VoiceDemo />
        <div className={h.voicePoints}>
          {POINTS.map((p, i) => {
            const Icon = p.icon
            return (
              <Reveal key={p.title} delay={i * 0.08}>
                <Glass tier="dark" className={h.voicePoint}>
                  <Icon size={22} color="#9DB8FF" aria-hidden />
                  <div><b>{p.title}</b><p style={{ margin: '4px 0 0', color: '#C9D4E8' }}>{p.body}</p></div>
                </Glass>
              </Reveal>
            )
          })}
        </div>
      </div>
    </Section>
  )
}
```

In `apps/web/app/(site)/page.tsx` import `Voice` and render `<Voice />` after `<PinnedStory />`.

- [ ] **Step 6: Run tests to verify they pass**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop` and `--project=mobile-chrome`
Expected: all passed.

- [ ] **Step 7: Commit**

```bash
git add apps/web/components/site apps/web/app/\(site\)/page.tsx apps/web/tests/site.spec.ts
git commit -m "feat(site): voice section with live orb, typed request and confirm card"
```

---

### Task 9: Discover section — listing cards fan out

**Files:**
- Create: `apps/web/components/site/mockups/ListingCard.tsx`, `apps/web/components/site/home/Discover.tsx`
- Modify: `apps/web/components/site/home/home.module.css` (append), `apps/web/app/(site)/page.tsx`, `apps/web/tests/site.spec.ts`

**Interfaces:**
- Consumes: `Section`, `Glass`, `SiteButton`, `EASE`.
- Produces: `type Listing = { area: string; city: string; rent: number; room: string; tags: string[]; tone: string }`, `<ListingCard listing: Listing />`, `<Discover />` with `id="discover"`.

- [ ] **Step 1: Write the failing test**

```ts
test('discover section shows listings, both journeys and the privacy note', async ({ page }) => {
  await page.goto('/')
  const sec = page.locator('#discover')
  await sec.scrollIntoViewIfNeeded()
  await expect(sec.getByRole('heading', { name: /next flat, or your next flatmate/ })).toBeVisible()
  await expect(sec.locator('[data-listing]')).toHaveCount(3)
  await expect(sec.getByText('Approximate location only')).toBeVisible()
  await expect(sec.getByRole('link', { name: 'Explore Discover' })).toHaveAttribute('href', '/discover')
})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop -g "discover section"`
Expected: FAIL — `#discover` missing.

- [ ] **Step 3: Styles**

Append to `apps/web/components/site/home/home.module.css`:

```css
.discoverGrid { display: grid; gap: 48px; margin-top: 48px; align-items: center; }
.fan { display: grid; place-items: center; min-height: 420px; }
.listing { grid-area: 1 / 1; width: min(280px, 70vw); border-radius: 24px; overflow: hidden; background: #fff; box-shadow: 0 24px 60px rgba(15,23,42,.18); }
.listingInner { display: block; }
.listingPhoto { height: 150px; position: relative; }
.listingBody { padding: 16px; display: grid; gap: 6px; }
.listingTags { display: flex; flex-wrap: wrap; gap: 6px; }
.listingTags span { font-size: 12px; padding: 4px 10px; border-radius: 999px; background: var(--or-canvas); color: var(--or-text-2); }
.journeys { display: grid; gap: 14px; }
.journey { padding: 20px; }
.privacyNote { display: flex; gap: 10px; align-items: center; color: var(--or-text-2); font-size: 15px; margin-top: 8px; }
@media (min-width: 1024px) { .discoverGrid { grid-template-columns: 1fr 1fr; } }
```

- [ ] **Step 4: ListingCard**

`apps/web/components/site/mockups/ListingCard.tsx`:

```tsx
import { MapPin, BadgeCheck } from 'lucide-react'
import h from '../home/home.module.css'

export type Listing = { area: string; city: string; rent: number; room: string; tags: string[]; tone: string }

export default function ListingCard({ listing }: { listing: Listing }) {
  return (
    <div className={h.listingInner}>
      <div className={h.listingPhoto} style={{ background: listing.tone }} />
      <div className={h.listingBody}>
        <b style={{ fontSize: 18 }}>₹{listing.rent.toLocaleString('en-IN')}<span style={{ fontWeight: 500, fontSize: 14, color: 'var(--or-text-2)' }}> /head</span></b>
        <span style={{ display: 'flex', gap: 6, alignItems: 'center', color: 'var(--or-text-2)', fontSize: 14 }}><MapPin size={14} aria-hidden />{listing.area}, {listing.city}</span>
        <span style={{ display: 'flex', gap: 6, alignItems: 'center', fontSize: 13, color: '#0B7A6C' }}><BadgeCheck size={14} aria-hidden />{listing.room} · admin approved</span>
        <div className={h.listingTags}>{listing.tags.map(t => <span key={t}>{t}</span>)}</div>
      </div>
    </div>
  )
}
```

- [ ] **Step 5: Discover section**

`apps/web/components/site/home/Discover.tsx`:

```tsx
'use client'
import { motion } from 'framer-motion'
import { Search, UserPlus, Lock } from 'lucide-react'
import Section from '../Section'
import Glass from '../Glass'
import SiteButton from '../SiteButton'
import ListingCard, { type Listing } from '../mockups/ListingCard'
import { EASE } from '../motion'
import h from './home.module.css'

const LISTINGS: Listing[] = [
  { area: 'Gachibowli', city: 'Hyderabad', rent: 8500, room: 'Private room', tags: ['Near metro', 'Vegetarian', 'Quiet'], tone: 'linear-gradient(135deg,#F2B565,#C9773A)' },
  { area: 'Koramangala', city: 'Bengaluru', rent: 11000, room: 'Shared room', tags: ['Balcony', 'Pet friendly'], tone: 'linear-gradient(135deg,#A8C4FF,#5B8CFF)' },
  { area: 'Baner', city: 'Pune', rent: 7200, room: 'Private room', tags: ['Gym nearby', 'Early birds'], tone: 'linear-gradient(135deg,#9BE7DC,#14B8A6)' },
]
const FAN = [{ r: -9, x: -62 }, { r: 0, x: 0 }, { r: 9, x: 62 }]

export default function Discover() {
  return (
    <Section id="discover" tone="white" eyebrow="Discover"
      title="Find your next flat, or your next flatmate."
      lede="Rooms are posted by people who live there, approved by their flat admin. Seekers share what they are looking for. You choose who to talk to.">
      <div className={h.discoverGrid}>
        <div className={h.fan} aria-label="Example flat listings" role="group">
          {LISTINGS.map((l, i) => (
            <motion.div key={l.area} data-listing="" className={h.listing}
              initial={{ rotate: 0, x: 0, opacity: 0 }}
              whileInView={{ rotate: FAN[i].r, x: FAN[i].x, opacity: 1 }}
              viewport={{ once: true, amount: 0.4 }}
              transition={{ duration: 0.8, ease: EASE, delay: i * 0.08 }}
              style={{ zIndex: i === 1 ? 3 : 1 }}>
              <ListingCard listing={l} />
            </motion.div>
          ))}
        </div>
        <div className={h.journeys}>
          <Glass tier="canvas" className={h.journey}><b style={{ display: 'flex', gap: 8, alignItems: 'center' }}><Search size={18} color="var(--or-blue)" aria-hidden />Looking for a room</b><p style={{ margin: '6px 0 0', color: 'var(--or-text-2)' }}>Filter by area, budget and lifestyle. See how a flat runs before you connect.</p></Glass>
          <Glass tier="canvas" className={h.journey}><b style={{ display: 'flex', gap: 8, alignItems: 'center' }}><UserPlus size={18} color="var(--or-teal)" aria-hidden />Filling a room</b><p style={{ margin: '6px 0 0', color: 'var(--or-text-2)' }}>Any flatmate can post the vacancy. It goes live once your admin approves.</p></Glass>
          <p className={h.privacyNote}><Lock size={16} aria-hidden />Approximate location only. Contact details stay hidden until you accept.</p>
          <div><SiteButton href="/discover" variant="primary">Explore Discover</SiteButton></div>
        </div>
      </div>
    </Section>
  )
}
```

In `apps/web/app/(site)/page.tsx` import `Discover` and render `<Discover />` after `<Voice />`.

Note: the `/discover` link resolves in Phase 2; until then the Phase 1 link test only checks the `href`.

- [ ] **Step 6: Run tests to verify they pass**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop` and `--project=mobile-chrome`
Expected: all passed.

- [ ] **Step 7: Commit**

```bash
git add apps/web/components/site apps/web/app/\(site\)/page.tsx apps/web/tests/site.spec.ts
git commit -m "feat(site): discover section with fanned listing cards and both journeys"
```

---

### Task 10: Trust tiles, install steps, final CTA

**Files:**
- Create: `apps/web/components/site/home/Trust.tsx`, `apps/web/components/site/home/Install.tsx`, `apps/web/components/site/home/FinalCta.tsx`
- Modify: `apps/web/components/site/home/home.module.css` (append), `apps/web/app/(site)/page.tsx`, `apps/web/tests/site.spec.ts`

**Interfaces:**
- Consumes: `Section`, `Reveal`, `Glass`, `SiteButton`, `DownloadButton`, `release`, `HERO_PHOTO`-style constant.
- Produces: `<Trust />` (`id="trust"`), `<Install />` (`id="install"`), `<FinalCta hasPhoto: boolean download: React.ReactNode />` (`id="get-oddroof"`), `FINAL_PHOTO = '/site/final-golden-hour.jpg'`.

- [ ] **Step 1: Write the failing tests**

```ts
test.describe('closing sections', () => {
  test('trust tiles carry only the approved claims', async ({ page }) => {
    await page.goto('/')
    const tiles = page.locator('#trust [data-trust-tile] b')
    await expect(tiles).toHaveText([
      'Contact stays private', 'Approximate location only', 'Your data, your call', 'Built for India\'s DPDP Act',
    ])
    await expect(page.locator('#trust').getByRole('link', { name: 'Read how we protect you' })).toHaveAttribute('href', '/privacy-and-safety')
  })

  test('install steps show version and date from version.json', async ({ page }) => {
    await page.goto('/')
    const sec = page.locator('#install')
    await expect(sec.locator('ol > li')).toHaveCount(3)
    await expect(sec.getByText(`Version ${version.version}`)).toBeVisible()
  })

  test('final CTA repeats the download', async ({ page }) => {
    await page.goto('/')
    await expect(page.locator('#get-oddroof [data-download]')).toHaveAttribute('href', version.url)
  })
})
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop -g "closing sections"`
Expected: FAIL — `#trust` missing.

- [ ] **Step 3: Styles**

Append to `apps/web/components/site/home/home.module.css`:

```css
.trustGrid { display: grid; gap: 16px; margin-top: 48px; grid-template-columns: 1fr; }
.trustTile { padding: 24px; display: grid; gap: 10px; }
.trustIcon { width: 44px; height: 44px; border-radius: 14px; display: grid; place-items: center; color: #fff; }
.steps { list-style: none; padding: 0; margin: 40px 0 0; display: grid; gap: 16px; counter-reset: s; }
.steps li { padding: 22px 22px 22px 76px; position: relative; }
.steps li::before { counter-increment: s; content: counter(s); position: absolute; left: 22px; top: 20px; width: 38px; height: 38px; border-radius: 12px; background: var(--or-blue); color: #fff; display: grid; place-items: center; font-weight: 800; }
.installNote { margin-top: 20px; color: var(--or-text-2); font-size: 15px; }
.final { position: relative; overflow: hidden; color: #fff; text-align: center; padding: clamp(96px, 14vw, 180px) var(--or-gutter); }
.final h2 { font-size: clamp(34px, 5.5vw, 64px); margin: 0 auto; max-width: 16ch; position: relative; z-index: 2; }
.finalInner { position: relative; z-index: 2; display: grid; justify-items: center; gap: 24px; }
@media (min-width: 768px) { .trustGrid { grid-template-columns: 1fr 1fr; } .steps { grid-template-columns: repeat(3, 1fr); } }
@media (min-width: 1200px) { .trustGrid { grid-template-columns: repeat(4, 1fr); } }
```

- [ ] **Step 4: Trust**

`apps/web/components/site/home/Trust.tsx`:

```tsx
import { EyeOff, MapPin, Trash2, Scale } from 'lucide-react'
import Section, { Reveal } from '../Section'
import Glass from '../Glass'
import SiteButton from '../SiteButton'
import h from './home.module.css'

const TILES = [
  { icon: EyeOff, color: 'var(--or-blue)', title: 'Contact stays private', body: 'Phone numbers and emails stay hidden until you accept a connection.' },
  { icon: MapPin, color: 'var(--or-teal)', title: 'Approximate location only', body: 'Listings show the area, never the exact address.' },
  { icon: Trash2, color: 'var(--or-coral)', title: 'Your data, your call', body: 'Delete your account and your data from the app at any time.' },
  { icon: Scale, color: 'var(--or-violet)', title: 'Built for India\'s DPDP Act', body: 'A named grievance contact and clear rules on what we keep and why.' },
]

export default function Trust() {
  return (
    <Section id="trust" tone="canvas" eyebrow="Privacy & trust" title="Built to be trusted in your home."
      lede="Sharing a flat means sharing a lot. Oddroof keeps what is private, private.">
      <div className={h.trustGrid}>
        {TILES.map((t, i) => {
          const Icon = t.icon
          return (
            <Reveal key={t.title} delay={i * 0.07}>
              <Glass tier="canvas" className={h.trustTile} data-trust-tile="">
                <span className={h.trustIcon} style={{ background: t.color }}><Icon size={20} aria-hidden /></span>
                <b>{t.title}</b>
                <p style={{ margin: 0, color: 'var(--or-text-2)' }}>{t.body}</p>
              </Glass>
            </Reveal>
          )
        })}
      </div>
      <div style={{ marginTop: 32 }}><SiteButton href="/privacy-and-safety" variant="light" style={{ border: '1px solid var(--or-line)' }}>Read how we protect you</SiteButton></div>
    </Section>
  )
}
```

- [ ] **Step 5: Install**

`apps/web/components/site/home/Install.tsx`:

```tsx
import Section from '../Section'
import { release } from '@/lib/site/release'
import h from './home.module.css'

export default function Install() {
  const released = new Date(release.releasedOn).toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' })
  const steps = [
    { title: 'Download the APK', body: `Tap Download on your Android phone. The file is about ${release.sizeMb} MB.` },
    { title: 'Allow the install', body: 'Android asks once to allow installs from your browser. Tap Settings, allow, then go back.' },
    { title: 'Open Oddroof', body: 'Sign in with Google, then create your flat or join one with an invite code.' },
  ]
  return (
    <Section id="install" tone="white" eyebrow="Install" title="Up and running in a minute."
      lede="Oddroof is free and available as a direct download for Android while our Play Store listing is on its way.">
      <ol className={h.steps}>
        {steps.map(st => (
          <li key={st.title}>
            <b>{st.title}</b>
            <p style={{ margin: '6px 0 0', color: 'var(--or-text-2)' }}>{st.body}</p>
          </li>
        ))}
      </ol>
      <p className={h.installNote}>Version {release.version} · released {released} · Android {release.minAndroid} or newer. Android shows a warning for any app installed outside the Play Store; this is expected for a direct download.</p>
    </Section>
  )
}
```

Append to `home.module.css` so each step reads as canvas glass:

```css
.steps li { background: rgba(255,255,255,.76); border: 1px solid var(--or-line); border-radius: var(--or-radius-md); box-shadow: 0 12px 40px rgba(15,23,42,.08); }
```

- [ ] **Step 6: FinalCta**

`apps/web/components/site/home/FinalCta.tsx`:

```tsx
import Image from 'next/image'
import { Reveal } from '../Section'
import h from './home.module.css'

export const FINAL_PHOTO = '/site/final-golden-hour.jpg'

export default function FinalCta({ hasPhoto, download }: { hasPhoto: boolean; download: React.ReactNode }) {
  return (
    <section id="get-oddroof" className={`${h.final} or-golden`}>
      {hasPhoto && <Image src={FINAL_PHOTO} alt="" fill sizes="100vw" style={{ objectFit: 'cover' }} />}
      <div className={h.heroScrim} style={{ background: 'rgba(14,10,28,.55)' }} />
      <Reveal className={h.finalInner}>
        <h2>Make your flat feel like home.</h2>
        <p style={{ maxWidth: '40ch', margin: 0, color: 'rgba(255,255,255,.9)' }}>Free for every flatmate. Set up your flat in two minutes.</p>
        {download}
      </Reveal>
    </section>
  )
}
```

- [ ] **Step 7: Wire into Home**

Final `apps/web/app/(site)/page.tsx`:

```tsx
import fs from 'node:fs'
import path from 'node:path'
import Hero, { HERO_PHOTO } from '@/components/site/home/Hero'
import Problem from '@/components/site/home/Problem'
import PinnedStory from '@/components/site/home/PinnedStory'
import Voice from '@/components/site/home/Voice'
import Discover from '@/components/site/home/Discover'
import Trust from '@/components/site/home/Trust'
import Install from '@/components/site/home/Install'
import FinalCta, { FINAL_PHOTO } from '@/components/site/home/FinalCta'
import DownloadButton from '@/components/site/DownloadButton'
import DownloadQr from '@/components/site/DownloadQr'

const hasPhoto = (p: string) => fs.existsSync(path.join(process.cwd(), 'public', p))

export default function HomePage() {
  return (
    <main>
      <Hero download={<DownloadButton showMeta />} qr={<DownloadQr />} hasPhoto={hasPhoto(HERO_PHOTO)} />
      <Problem />
      <PinnedStory />
      <Voice />
      <Discover />
      <Trust />
      <Install />
      <FinalCta hasPhoto={hasPhoto(FINAL_PHOTO)} download={<DownloadButton variant="light" />} />
    </main>
  )
}
```

- [ ] **Step 8: Run tests to verify they pass**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop` and `--project=mobile-chrome`
Expected: all passed.

- [ ] **Step 9: Commit**

```bash
git add apps/web/components/site apps/web/app/\(site\)/page.tsx apps/web/tests/site.spec.ts
git commit -m "feat(site): trust tiles, install steps and golden-hour final CTA"
```

---

### Task 11: Photos, responsive matrix, reduced motion, Lighthouse

**Files:**
- Create: `apps/web/public/site/README.md` (photo brief), `apps/web/public/site/hero-golden-hour.jpg` and `final-golden-hour.jpg` (owner-supplied, when available)
- Modify: `apps/web/tests/site.spec.ts`

**Interfaces:**
- Consumes: everything above.
- Produces: the Phase 1 acceptance suite.

- [ ] **Step 1: Write the acceptance tests**

Append to `apps/web/tests/site.spec.ts`:

```ts
const WIDTHS = [375, 768, 1024, 1440]

for (const width of WIDTHS) {
  test(`no horizontal scroll at ${width}px`, async ({ page }) => {
    await page.setViewportSize({ width, height: 900 })
    await page.goto('/')
    // Walk the page so scroll-linked layouts settle at every section.
    for (const id of ['hero', 'problem', 'how-it-works', 'voice', 'discover', 'trust', 'install', 'get-oddroof']) {
      await page.locator(`#${id}`).scrollIntoViewIfNeeded()
      const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth)
      expect(overflow, `overflow in #${id}`).toBeLessThanOrEqual(0)
    }
  })
}

test.describe('reduced motion', () => {
  test.use({ reducedMotion: 'reduce' })
  test('every section heading and lede is visible without animation', async ({ page }) => {
    await page.goto('/')
    for (const id of ['problem', 'how-it-works', 'voice', 'discover', 'trust', 'install', 'get-oddroof']) {
      const sec = page.locator(`#${id}`)
      await sec.scrollIntoViewIfNeeded()
      const heading = sec.getByRole('heading').first()
      await expect(heading).toBeVisible()
      // toBeVisible() treats opacity:0 as visible, so check the effective opacity up the tree.
      const minOpacity = await heading.evaluate(el => {
        let o = 1
        for (let n: Element | null = el; n; n = n.parentElement) o = Math.min(o, parseFloat(getComputedStyle(n).opacity))
        return o
      })
      expect(minOpacity).toBe(1)
    }
  })
})
```

- [ ] **Step 2: Run the suite**

Run: `npx playwright test tests/site.spec.ts --project=chromium-desktop` and `--project=mobile-chrome`
Expected: all passed. If a width overflows, fix the offending section's CSS (most likely `.floatCard`, `.fan`, or `.bubble` offsets) by constraining with `max-width: 100%` / hiding decorative elements below 768 px, re-run, and include the fix in this task's commit.

- [ ] **Step 3: Photo brief**

`apps/web/public/site/README.md`:

```md
# Site photos

Two text-free golden-hour photos. Drop them here with these exact names; the pages pick them up automatically (the warm gradient shows until then).

| File | Use | Size |
|---|---|---|
| `hero-golden-hour.jpg` | Home hero, full-bleed behind headline and phone | 2400×1600, < 600 KB |
| `final-golden-hour.jpg` | Final call to action, full-bleed | 2400×1400, < 500 KB |

Prompt (hero): "Photorealistic interior of a modern Indian city apartment living room at golden hour, warm sunset light through tall windows, city towers softly visible outside, indoor plants, a low sofa with cushions, warm table lamps on, cosy and calm, wide shot, space on the left third for text, no people, no text, no logos, shallow depth of field, 3:2."

Prompt (final): "Photorealistic balcony of a shared flat in an Indian city at dusk, string lights, two chairs and chai cups on a small table, warm amber sky, plants, calm and homely, centred empty space for a headline, no people, no text, no logos, 16:9."
```

- [ ] **Step 4: Lighthouse (mobile)**

Run: `npm run build && npx next start -p 3100` in one terminal, then `npx lighthouse http://localhost:3100/ --preset=perf --form-factor=mobile --only-categories=performance,accessibility --output=json --output-path=./lighthouse-home.json --chrome-flags="--headless"`
Expected: performance ≥ 90, accessibility ≥ 95. Record both numbers in the commit message. If performance < 90, check in order: hero image `priority` + `sizes`, JS on first load (move non-hero sections behind `next/dynamic` with `ssr: true`), and blur radius on mobile (`@media (max-width: 767px) { .glass[data-glass="photo"] { backdrop-filter: blur(10px) } }`). Do not commit `lighthouse-home.json`.

- [ ] **Step 5: Dashboard regression check**

Run: `npx playwright test tests/navigation.spec.ts tests/auth.spec.ts --project=chromium-desktop`
Expected: same results as the baseline recorded in Task 1.

- [ ] **Step 6: Spot-check screenshots for the owner**

Run: `npx playwright screenshot --viewport-size=1440,900 --full-page http://localhost:3100/ home-1440.png` and `npx playwright screenshot --viewport-size=375,812 --full-page http://localhost:3100/ home-375.png` (outside the repo or git-ignored). Share both with the owner.

- [ ] **Step 7: Commit**

```bash
git add apps/web/tests/site.spec.ts apps/web/public/site/README.md
# plus the photos once the owner has supplied them:
git add apps/web/public/site/hero-golden-hour.jpg apps/web/public/site/final-golden-hour.jpg
git commit -m "test(site): responsive matrix, reduced motion, photo brief; Lighthouse perf <n>, a11y <n>"
```

(Replace `<n>` with the measured scores.)

---

## Phase hand-off

After Task 11 the owner reviews Home on a Netlify deploy preview (`deploy-preview-<n>--flatsflow.netlify.app`, now allowed by the proxy). Phase 2 (product pages), Phase 3 (hub, legal restyle, About) and Phase 4 (signed release APK, hosting, Netlify `Content-Type` header, production deploy) each get their own plan.
