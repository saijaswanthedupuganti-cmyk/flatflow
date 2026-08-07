# Batch 02 Analysis — ClickHouse, Cohere, Coinbase, Composio, Cursor, ElevenLabs, Expo, Ferrari, Figma, Framer

> Distilled patterns relevant to Habitiq's dark-first, violet-primary, mobile-first expense tracker

---

## Brand Snapshots

### 1. ClickHouse
- **Canvas:** Near-black #0a0a0a; electric yellow #faff69 as the single voltage
- **Typography:** Inter 700 display with -1 to -2.5px tracking; 400 body; JetBrains Mono for code; stat numbers in yellow at 56px
- **Buttons:** 8px radius, 40px height, yellow fill + black text; secondary = dark surface card
- **Cards:** Surface-card #1a1a1a (barely lighter than canvas); 12px radius; feature-card-yellow = full accent-fill card for emphasis
- **Depth:** Zero shadows — contrast between canvas (#0a0a0a) and surface-card (#1a1a1a) IS the elevation
- **Stat callouts:** Yellow numbers at 56px/700 inline on canvas — no card surface around them
- **CTA bands:** Full-bleed yellow CTA card with black text = strongest action moment
- **Section rhythm:** 96px
- **Key lesson:** Accent used scarcely on elements BUT abundantly on full-bleed CTA bands — creates voltage surge at the right moments. Stat numbers in accent color on dark canvas (no card background) = bold credibility signal. Code blocks as marketing chrome (show the data, don't illustrate it).

### 2. Cohere
- **Canvas:** White (#fff); deep enterprise green (#003c33) and dark navy (#071829) for product bands
- **Typography:** CohereText (custom, weight 400) for display; Unica77 for body/UI — distinct display/body voice split
- **Buttons:** Pill CTAs (32px radius), near-black fill; secondary = underlined text link (no background)
- **Cards:** Rounded media cards 8-22px; product cards warm stone (#eeece7) surface; coral (#ff7759) for blog taxonomy chips only — never CTA
- **Layout:** White space as trust signal — generous empty intervals separate brand claim, proof, CTA; research pages use full-width rule-separated rows (not cards)
- **Depth:** Flat — depth via surface alternation (white → deep green → stone) and media contrast
- **Key lesson:** Whitespace is a trust signal for financial/enterprise apps. Research/transaction lists work as full-width rule-separated rows (no card boxing needed). Secondary CTAs as text-only underlined = restraint. Enterprise brand = calmer type weights (400), not bolder.

### 3. Coinbase
- **Canvas:** White (#fff); Coinbase Blue #0052ff as single accent
- **Typography:** CoinbaseDisplay weight 400 for heroes (editorial calm, not urgency); CoinbaseSans 400/600 body; CoinbaseMono for all numbers
- **Buttons:** Pill (100px radius), 44px height; primary = blue fill; secondary = soft-gray fill; dark hero = dark-elevated secondary
- **Cards:** Feature cards 24px radius; product-UI-card-dark (#16181c) for dashboard mockups inside dark heroes — layered at slight rotation for depth
- **Hero pattern:** Full-bleed dark hero (#0a0b0d) + floating layered product-UI cards = strongest signature
- **Trading semantics:** Green #05b169 up / Red #cf202f down — text color only, never card fill
- **Section rhythm:** 96px
- **Key lesson:** Display weight 400 signals institutional calm vs weight 700 urgency — Habitiq sits between (weight 600 fits). Layered product-UI cards at slight rotation inside dark hero = shows actual app + creates depth. Monospace for ALL currency numbers is critical for financial credibility. Pill (100px) for ALL CTAs unifies the brand gesture.

### 4. Composio
- **Canvas:** Near-black #0f0f0f; deep electric blue #0007cd
- **Typography:** abcDiatype (Inter substitute) single sans family; display 500, body 400; JetBrains Mono for code
- **Buttons:** 8px radius (developer dialect), 40px; primary = deep blue fill; secondary = surface-card-elevated
- **Hero:** 2×2 grid of dark code/output panels with central blue spotlight glow = terminal-mockup grid
- **Depth:** Brightness-step elevation (not shadows); canvas #0f0f0f → surface-card #181818 → elevated #222222
- **Atmospheric depth:** Radial blue spotlight glow centered behind hero content
- **Section rhythm:** 96px
- **Key lesson:** 2×2 app screen composite grid as hero (show 4 real product views) — adaptable to any dark-first product. Radial spotlight glow in brand color behind hero content = atmosphere without changing the color. Brightness-step elevation (no shadows) confirmed for dark themes.

### 5. Cursor
- **Canvas:** Warm cream #f7f7f4 (NOT white); warm near-black ink #26251e
- **Typography:** CursorGothic weight 400 (magazine editorial voice); display -2.16px tracking; JetBrains Mono for code; AI timeline pastels for stage pills
- **Buttons:** 8px radius, 40px height; Cursor Orange (#f54e00) for primary only (scarce); secondary = white card + hairline; download = ink fill + cream text
- **Cards:** Surface-card #fff (slight contrast on cream canvas); 12px radius; hairline-only depth (no shadows); IDE mockup card as hero element
- **AI timeline pills:** 5 distinct pastel colors for stages: Thinking (peach), Grep (mint), Read (blue), Edit (lavender), Done (gold) — SCOPED to product timeline only
- **Section rhythm:** 80px (tighter — developer product)
- **Key lesson:** Warm canvas tint (#f7f7f4 vs pure white) feels distinctive. Cream canvas with one accent color = most editorial-feeling developer brand. AI/workflow stage visualization with color-coded pills is strong UX pattern — Habitiq can adapt for task states (pending/active/done/skipped). 80px section rhythm for denser utility apps. IDE mockup as page chrome = show real product.

### 6. ElevenLabs
- **Canvas:** Off-white #f5f5f5; near-black ink #0c0a09 as primary CTA (no saturated action color)
- **Typography:** Waldenburg Light weight 300 serif for display (editorial signature); Inter 400/500 for body; body letter-spacing slightly looser (+0.15-0.18px)
- **Buttons:** Pill CTAs; near-black ink pill = primary; transparent outline = secondary; no saturated CTA color
- **Atmospheric orbs:** 5 pastel gradient tokens (mint #a7e5d3, peach #f4c5a8, lavender #c8b8e0, sky #a8c8e8, rose #e8b8c4) — ATMOSPHERE ONLY, never as button fills
- **Cards:** Surface-card #fff; 16px radius feature cards; audio waveform card (specialized product component)
- **Depth:** Hairline + one soft drop shadow `0 4px 16px rgba(0,0,0,0.04)` for hover
- **Section rhythm:** 96px
- **Key lesson:** Atmospheric gradient orbs (radial blooms in brand-adjacent pastels) add visual voltage without competing with UI. Near-black as primary CTA = very premium/restrained. For Habitiq: violet gradient radial glow as atmospheric decoration behind hero (not a CTA color). Slightly looser body tracking (+0.1-0.15px) on dark reads cleaner than default.

### 7. Expo
- **Canvas:** Pure white; sky-blue gradient wash (#cfe7ff → #a8c8e8) ONLY behind hero
- **Typography:** Inter 600 display (single family, no custom); -1.92px tracking at 64px; Inter 400 body; JetBrains Mono code
- **Buttons:** 8px radius, 40px; pure black fill = primary; white + hairline = secondary; no pill on CTAs (pill = badges only)
- **Hero chrome:** MacBook + iPhone composite showing real Expo dev surfaces; sky gradient behind only
- **Cards:** Surface-card #fff; 12px radius; dark feature cards (#171717) for contrast; ecosystem tiles at 64px
- **Section rhythm:** 96px
- **Key lesson:** One atmospheric gradient wash (not a brand color) makes a white hero feel special without committing to a light-mode color. Device mockup composite (desktop + mobile side by side) = powerful landing page chrome — adaptable to Habitiq desktop + mobile side-by-side. Pure black CTA on white works — ultra-minimal. Inter 600 (not 700) for display = confident without bombastic.

### 8. Ferrari
- **Canvas:** Near-black #181818 (NOT pure black; slight warm tone); white-canvas bands only for preowned/pricing
- **Typography:** FerrariSans weight 500 display (editorial); uppercase CTA labels with 1.4px tracking; NEVER bold display
- **Buttons:** 0px radius (sharp corners); 48px height; primary = Rosso Corsa (#da291c); uppercase with tracking
- **Named spacing ladder:** xxxs:4 / xxs:8 / xs:16 / sm:24 / md:32 / lg:48 / xl:64 / xxl:96 / super:128
- **Cards:** 0px radius — sharp = precision/engineered; photography fills full bands
- **Depth:** Photographic + brightness-step; NO drop shadows
- **Cinematic hero:** Full-bleed photo fills viewport; headline floats over bottom of photo
- **Key lesson:** Named spacing token ladder (not just 4/8/12/16/24/32/48/96) is more expressive and intentional. Uppercase + tracked button labels = precision/authority — usable in Habitiq for category labels and metadata. Near-black canvas with SLIGHT warm tone (#181818 vs #000) feels more premium than pure black. Sharp corners CAN work but only with a strong photographic brand; not right for Habitiq's rounded-first design.

### 9. Figma
- **Canvas:** Pure white (#fff) / pure black for inverse; ZERO mid-gray surfaces
- **Typography:** figmaSans variable (weights 320/330/340/480/540/700); figmaMono for taxonomy; tight negative tracking at display (-1.72px at 86px)
- **Buttons:** Pill only (50px) for all text CTAs; full circle for icon buttons
- **Signature pattern:** Oversized pastel color-block sections (lime, lilac, cream, mint, pink, coral, navy) — span full content width, 24px rounded corners, 48px internal padding; returned to white between each
- **Depth device:** Color blocks ARE the depth — no shadows needed; color change = section break
- **Mono labels:** figmaMono uppercase with positive tracking for taxonomy, eyebrows, captions — never for body text
- **Marquee strip:** Thin inverse (black) strip with scrolling customer logos below nav
- **Section rhythm:** 96px between white bands; 48px inside color blocks
- **Key lesson:** Color-block sections (full-width, rounded, saturated, 48px padding) = powerful section variety without changing the core palette. Monochrome chrome makes color blocks feel deliberate (not decorative). Body hierarchy via weight alone (320→540), not opacity or mid-gray text. Marquee strip with customer/partner logos = low-cost trust signal.

### 10. Framer
- **Canvas:** Near-pure black #090909; white as primary CTA color (inverted)
- **Typography:** GT Walsheim Medium with EXTREME negative tracking (-5.5px at 110px, -4.25px at 85px); Inter Variable with OpenType variants for body; tight line-heights everywhere (body: 1.30)
- **Buttons:** White pill (100px) on dark = primary; charcoal pill = secondary
- **Gradient spotlight cards:** Oversized atmospheric cards in violet, magenta, orange, coral — dropped into dark card grids; they are individual cards (NOT section backgrounds)
- **Surface lift:** canvas #090909 → surface-1 #141414 → surface-2 #1c1c1c — 3 steps, no shadows
- **Depth:** Brightness-step elevation + gradient spotlight cards + one subtle drop on floating cards
- **Dark-only brand:** No light-mode marketing; the dark IS the brand
- **Section rhythm:** 96px
- **Key lesson:** White pill on dark canvas = elegant inverted CTA pattern (Habitiq currently uses violet — white could be secondary). Surface lift with 3 named steps (#090909 → #141414 → #1c1c1c) perfectly maps to Habitiq's dark system. Gradient spotlight cards (violet, indigo) as atmospheric tiles in dark card grids = strong hero decoration pattern for Habitiq. Extreme negative tracking only works above 60px — at typical sizes stick to -1.5px.

---

## Cross-Brand Pattern Extraction (for Habitiq)

### Color Architecture
| Pattern | Brands | Apply to Habitiq |
|---------|--------|-----------------|
| Accent element-level scarcity + full-bleed abundance | ClickHouse (yellow stat + yellow CTA bands) | Violet on primary CTAs/stats; full-bleed violet CTA section at page bottom |
| Surface-lift 3-step dark elevation | Composio, Framer (#090909→#141414→#1c1c1c) | Canvas #0a0a0a → Surface-1 #141414 → Surface-2 #1e1e1e → hover #2a2a2a (confirmed) |
| Radial spotlight glow in brand color | Composio (blue glow), Framer (gradient cards), ElevenLabs (orbs) | Violet radial glow behind hero stats section — atmosphere only |
| Semantic financial colors text-only | Coinbase (green/red text, never fills) | Confirmed: income #22c55e / expense #ef4444 text colors only |
| Monospace for all currency numbers | Coinbase (CoinbaseMono), Cursor (JetBrains on code) | Use Inter with `font-variant-numeric: tabular-nums` or monospace for amounts |
| Warm near-black canvas | Ferrari (#181818), Cursor (#f7f7f4 cream) | #0a0a0a with slight warmth — not pure #000 (confirmed from Batch 1) |

### Typography
| Pattern | Brands | Apply to Habitiq |
|---------|--------|-----------------|
| Display weight 400-600 = calm authority | Coinbase (400), Expo (600), Cohere (400) | Inter 600 for hero headlines — confident, not bombastic |
| Display weight 700+ = urgency/trading | ClickHouse (700) | Use 700 only for financial stats and balance numbers |
| Named spacing ladder | Ferrari (xxxs through super) | Adopt: xs:8 sm:12 md:16 lg:24 xl:32 2xl:48 3xl:64 section:96 |
| Uppercase + letter-spacing on labels | Ferrari (1.4px buttons, 0.65px nav), ClickHouse (caption-uppercase) | Category labels, metadata rows: uppercase + 0.8px tracking |
| Monospace for taxonomy/eyebrows | Figma (figmaMono uppercase) | Use Inter mono-style labels (uppercase + tracking) for expense categories |
| Tabular figures for currency | Coinbase (CoinbaseMono), Framer (tnum) | `font-variant-numeric: tabular-nums` on all amounts |

### Button Design
| Pattern | Brands | Habitiq Buttons |
|---------|--------|----------------|
| Pill (100px) = unified brand gesture | Coinbase, ElevenLabs, Figma | Hero CTA + onboarding: pill variant |
| 8px = developer/product dialect | Composio, Cursor, Expo, ClickHouse | App interior buttons: 8px (confirmed) |
| White pill on dark = inverted primary | Framer | Secondary/ghost button: white pill |
| Near-black pill = restrained premium | ElevenLabs | Can use for dark-surface secondary buttons |
| Sharp 0px = engineered/luxury | Ferrari, BMW | Do NOT use for Habitiq (wrong brand voice) |
| Uppercase + tracking on buttons | Ferrari (1.4px) | Category action buttons: uppercase + 0.5px tracking |

### Card Design
| Pattern | Brands | Habitiq Cards |
|---------|--------|--------------|
| Full-bleed accent CTA card | ClickHouse (yellow), Figma (color-block), Airtable | Full-bleed violet section for "Start tracking" CTA |
| Floating product-UI cards in hero | Coinbase (layered at rotation) | Hero: show actual Habitiq screens floating over dark hero |
| 2×2 app screen composite | Composio (terminal grid) | Hero section: 2×2 grid of Habitiq screens (dashboard, expenses, tasks, stats) |
| Color-block sections (full-width) | Figma (pastel blocks), Airtable | Feature section bands with violet-tinted surface |
| Gradient spotlight card in grid | Framer (gradient cards in dark grid) | One violet gradient card in feature grid = standout |
| Atmospheric orbs (no UI content) | ElevenLabs (pastel radial) | Violet/indigo radial glow decoration behind hero stats |
| Research/list rows (no card boxing) | Cohere (research-table) | Transaction list: hairline-separated rows, not individual cards |

### Layout & Spacing
| Pattern | Brands | Habitiq |
|---------|--------|---------|
| 96px section rhythm | 8 of 10 brands | Landing/marketing: 96px between sections |
| 80px for utility-dense | Cursor (80px) | Dashboard inner bands: 64-80px |
| Named spacing token ladder | Ferrari | xs:8 sm:12 md:16 lg:24 xl:32 2xl:48 3xl:64 |
| Marquee strip = trust signal | Figma (customer logos) | Habitiq landing: thin dark strip with "trusted by X users" or partner logos |
| Whitespace = enterprise trust | Cohere | App: don't over-pack cards — generous p-6 content, p-8 feature |
| Rule-separated rows for dense lists | Cohere (research-table) | Expense transaction list: use hairline rows, not individual elevated cards |

### Mobile Patterns
| Pattern | Brands | Habitiq Mobile |
|---------|--------|---------------|
| Color blocks become full-bleed | Figma (rounded corners removed on mobile) | Feature sections: full-bleed on mobile, rounded on desktop |
| Layered cards collapse to single | Coinbase (3 stacked → 1 on mobile) | Hero composite: 2×2 → single screen on mobile |
| Photography reframes per breakpoint | Ferrari (art direction) | App screenshots: choose portrait-optimized crop for mobile |
| Gradient spotlight cards retain radius | Framer (kept at all breakpoints) | Violet glow effect preserved on mobile |
| Device mockup composite | Expo (MacBook + iPhone) | Habitiq landing: desktop sidebar + mobile bottom nav composite |

### Web Layout Patterns
| Pattern | Brands | Habitiq Web |
|---------|--------|------------|
| Full-bleed dark hero + floating cards | Coinbase, Composio | Landing hero: violet-tinted dark band + floating Habitiq screens |
| 2×2 product screen grid | Composio (terminal) | Hero chrome: 4 app screens (expenses, tasks, stats, budget) |
| Research/list table layout | Cohere | Transaction history: full-width rows with date, category, amount columns |
| Color-block section variety | Figma, Airtable | Feature sections: one violet surface block, one dark-elevated block |
| Trust-logo marquee strip | Figma | Pre-footer: "Join X users tracking with Habitiq" + usage stats |

---

## Habitiq-Specific Extractions

### What to steal from each brand:

**From ClickHouse:**
- Balance/stat numbers in violet accent at large size (56-64px, 700 weight) directly on dark canvas — no card wrapper
- Full-bleed violet CTA section at page bottom (the "yellow band" pattern adapted to violet)
- Transaction detail "code window" → Habitiq: expense breakdown card showing actual data rows, not illustrations
- Stat callout pattern: `€2,340` in violet-400 at 48px = credibility signal on dark surface

**From Cohere:**
- Transaction list = full-width rule-separated rows (hairline divider), NOT individual elevated cards
- Whitespace as trust signal — don't pack the dashboard; give breathing room between stat cards
- Enterprise-calm display weight (600 for Habitiq, not the 700 "trading urgency") for non-financial headers
- Coral taxonomy chips → Habitiq: category chips (Food, Transport, etc.) with distinct colors per category

**From Coinbase:**
- Landing hero: full-bleed dark band + floating Habitiq app screens (dashboard card slightly angled)
- CoinbaseMono for numbers → `font-variant-numeric: tabular-nums; font-family: 'JetBrains Mono'` for all currency amounts
- Semantic green/red TEXT ONLY on financial data (confirmed for Batch 1)
- Soft-gray fill for secondary buttons (not white outline) = `#1e1e1e` on dark surface

**From Composio:**
- 2×2 app screen grid as hero chrome: show 4 real Habitiq screens at once
- Violet radial spotlight glow behind hero section (not a UI element — pure atmosphere)
- Brightness-step elevation: Canvas → Surface-1 → Surface-2 (no drop shadows) — CONFIRMED
- Badge-pill for feature labels: uppercase + tracking, dark surface fill

**From Cursor:**
- Task state visualization with color-coded pills: pending (gray), active (violet), done (green), skipped (muted) — scoped to task timeline only
- IDE mockup → Habitiq: show actual app screen as hero chrome
- 80px section rhythm for dashboard interior panels (vs 96px for landing page)
- Warm near-black canvas tint confirmed: #0a0a0a (not #000000)

**From ElevenLabs:**
- Violet/indigo radial gradient orbs behind hero stats = atmospheric voltage without being a CTA color
- Inter body with slightly looser tracking (+0.1px) on dark for readability
- Near-black pill as restrained secondary CTA on light surfaces (if Habitiq ever has light sections)
- Audio waveform → Habitiq: spending trend sparkline card (similar shaped data visualization component)

**From Expo:**
- Desktop + mobile device composite as landing hero: show Habitiq sidebar layout (desktop) + bottom nav layout (mobile) side by side
- Sky-blue gradient → Habitiq: violet/indigo gradient wash ONLY behind hero section, not repeated
- Pure Inter with weight 600 display is sufficient — no custom typeface needed (confirms Habitiq's Inter)
- Dark feature card variant (`surface-dark`) as contrast against light sections

**From Ferrari:**
- Named spacing token ladder (not ad-hoc px): adopt in Tailwind config
- Uppercase + letter-spacing on metadata: `font-size: 11px; font-weight: 600; letter-spacing: 0.8px; text-transform: uppercase` for expense categories, date labels, status badges
- Near-black canvas #181818 vs Habitiq #0a0a0a — Habitiq's is slightly colder, Ferrari's warmer; both valid
- 48px button height for primary CTAs where WCAG AAA needed (increase from 44px for landing page CTAs)

**From Figma:**
- Color-block section for feature area: one full-width violet-tinted section (e.g., "#7c3aed" at 10% opacity over canvas, 16px radius, 48px padding)
- Marquee strip: thin dark strip below top nav with usage stats or partner logos = cheap trust signal
- Body hierarchy via Inter weight alone (no mid-gray text levels): ink → muted = two levels max on dark
- Section eyebrow labels in monospace uppercase = taxonomic clarity for feature sections

**From Framer:**
- Surface lift system CONFIRMED for Habitiq dark theme: #0a0a0a → #141414 → #1e1e1e → hover: #2a2a2a
- Gradient spotlight card in feature grid: one violet-to-indigo gradient card = standout in otherwise dark card grid
- White pill as secondary CTA on very dark hero surfaces (where violet would be the primary)
- Tight line-heights on display (1.0-1.1); generous on body (1.5-1.6) — contrast reinforces scanning speed

---

## Cumulative Pattern Updates (Batch 01 + Batch 02)

### Refined Token Table
```
Background:         #0a0a0a (warm near-black — confirmed × 2 batches)
Surface-1:          #141414 (Framer surface-1 exact match — confirmed)
Surface-2:          #1e1e1e (card base — confirmed)
Surface-3:          #2a2a2a (hover/active state)
Border:             rgba(255,255,255,0.08)
Border-strong:      rgba(255,255,255,0.16)

Primary:            #7c3aed (violet-600)
Primary-active:     #6d28d9 (violet-700)
Primary-disabled:   #3b2066 (violet-950)
On-primary:         #ffffff

Income-positive:    #22c55e (green-500) — text only, NEVER card fill
Expense-negative:   #ef4444 (red-500)   — text only, NEVER card fill
Neutral-stat:       #a78bfa (violet-400)
Stat-large:         #7c3aed at 700 weight — inline on canvas, no card wrapper

Display font:       Inter, weight 600, letterSpacing: -1.5px (hero), -1px (section heads)
Body font:          Inter, weight 400, letterSpacing: +0.1px (on dark for readability)
Label font:         Inter, weight 600, uppercase, letterSpacing: 0.8px, font-size: 11px
Number font:        Inter with font-variant-numeric: tabular-nums (OR JetBrains Mono for currency)

Button radius:      8px (app interior) / pill (hero CTA / onboarding)
Card radius:        12px standard / 16px feature / 24px highlight/summary
Full-bleed section: 16px radius on desktop, full-bleed on mobile
Pill:               9999px (hero CTAs, status badges, category chips)

Button height:      44px (app) / 48px (landing page for AAA)
Section rhythm:     96px (landing page) / 80px (dashboard bands)
Card padding:       24px (p-6) content / 32px (p-8) feature / 48px (full-bleed sections)

Spacing ladder:     xs:8 sm:12 md:16 lg:24 xl:32 2xl:48 3xl:64 section:96

Depth system:       Canvas #0a0a0a → S1 #141414 → S2 #1e1e1e → S3 #2a2a2a (no drop shadows)
Atmospheric:        Violet radial glow (rgba(124,58,237,0.15) → transparent) — hero only
One shadow tier:    0 4px 16px rgba(0,0,0,0.24) — popovers/modals ONLY
```

### New Patterns Confirmed This Batch
1. **Tabular figures for all currency amounts** (Coinbase, Framer) — `font-variant-numeric: tabular-nums`
2. **2×2 app screen composite** as hero chrome (Composio terminal grid pattern)
3. **Full-bleed violet CTA section** at page bottom = strongest conversion pattern
4. **Rule-separated transaction rows** (not individual cards) for expense lists
5. **Status pill colors for workflow states** (Cursor timeline pills) — pending/active/done/skipped
6. **Marquee trust strip** below top nav for social proof
7. **Gradient spotlight card** (one violet card in feature grid) for standout appeal
8. **Uppercase + 0.8px tracking** for all metadata labels, category names, status text
9. **Body tracking: +0.1px** on dark backgrounds improves readability (ElevenLabs)
10. **Device composite hero**: desktop sidebar layout + mobile bottom nav side by side
