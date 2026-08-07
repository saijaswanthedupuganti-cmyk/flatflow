# Batch 03 Analysis — HashiCorp, HP, IBM, Intercom, Kraken, Lamborghini, Linear, Lovable, Mastercard, Meta

> Distilled patterns relevant to Habitiq's dark-first, violet-primary, mobile-first product

---

## Brand Snapshots

### 11. HashiCorp
- **Canvas:** #000000 (pure black) — no warmth, hard developer precision
- **Typography:** hashicorpSans 600/700 display (LH 1.17-1.21), 500 body (LH 1.50-1.71) — tight display + relaxed body = editorial contrast
- **Buttons:** 8px radius, white fill primary, charcoal secondary; 10px×18px padding
- **Cards:** surface-1 (#15181e) charcoal; per-product chromatic variants: Terraform purple #7b42bc, Vault yellow #ffcf25, Consul red #e62b1e, Waypoint cyan #14c6cb
- **Eyebrow:** 12px / 600 / uppercase / 0.6px positive tracking — marks every section
- **Depth:** Canvas (#000) → Surface-1 (#15181e) → Surface-2 (#1f232b) → Surface-3 (#3b3d45); NO shadows
- **Section rhythm:** 96px
- **Key lesson:** Per-product / per-category color accents as identity tokens, not decoration. The eyebrow pattern (small-cap uppercase with positive tracking) is the most reliable section header across all brands surveyed.

### 12. HP
- **Canvas:** #ffffff white; lone action color HP Electric Blue #024ad8
- **Typography:** Forma DJR Micro; weight **500** for display (NOT 700 — deliberate softness); uppercase button labels with 0.7px letter-spacing
- **Buttons:** 4px radius (near-square), 44px height, uppercase labels with 0.7px tracking; cards 16px — 4px vs 16px is the two-tier split
- **Cards:** 16px radius product tiles with Soft Lift shadow `0 2px 8px rgba(26,26,26,0.08)`; light cloud (#f7f7f7) alternating bands
- **Dark close:** Dark ink (#1a1a1a) slabs close every page — testimonial bands + footer
- **Section rhythm:** 80px (utility-commercial density)
- **Key lesson:** Lone signal color = high perceived value. 80px sections for commercial/utility density vs 96px editorial. Two-tier radius (4px buttons, 16px cards) keeps interactive elements precise and containers soft.

### 13. IBM
- **Canvas:** #ffffff + surface-1 #f4f4f4; IBM Blue #0f62fe sole accent
- **Typography:** IBM Plex Sans (free, open source); weight **300** for 42-76px display (the brand's quiet authority); body 400 with `letter-spacing: 0.16px` — Carbon's precision detail
- **Buttons:** **0px radius** (completely square — Carbon's signature); 12px×16px padding
- **Cards:** **0px radius** (flat square tiles with 1px hairline borders); no shadows
- **Focus rings:** 2px IBM Blue underline on inputs (bottom-rule only, not full ring)
- **Section rhythm:** 96px (Carbon 16-column grid base)
- **Key lesson:** Positive letter-spacing (+0.16px) on body in dark contexts aids readability. Square corners signal "engineered" — not wrong for a financial tool, but Habitiq's 8px radius is the friendlier choice. IBM Plex Sans is the best free alternative to Inter for tech products — already in Kraken's fallback stack.

### 14. Intercom
- **Canvas:** cream #f5f1ec (warm — not white, not gray); white cards lift on top
- **Typography:** Saans 500 display; negative tracking scales with size: -2.0px at 72px → -1.4px at 56px → -0.8px at 40px → 0 at body; sentence-case eyebrows
- **Buttons:** 8px radius, charcoal (#111111) primary; Fin Orange (#ff5600) ONLY for AI sub-brand CTAs
- **Cards:** white on cream = cream+white system; 12px standard, 16px product mockup tiles
- **Featured pricing:** inverts to charcoal (#111111 bg) — dark featured tier
- **Depth:** White card on cream canvas = elevation without shadows (surface contrast is depth)
- **Key lesson:** Scaling negative letter-spacing proportionally to type size is the cleanest premium signal. Reserving a secondary accent for a specific sub-product (Fin = AI) is a clean way to differentiate without adding noise. Featured tier = inverted dark card.

### 15. Kraken (crypto exchange — closest to Habitiq domain)
- **Canvas:** white; Kraken Purple #7132f5 primary (closest purple to Habitiq #7c3aed in the dataset)
- **Typography:** Kraken-Brand display 700 / -1px at 48px; Kraken-Product UI; 16px body at 400
- **Buttons:** **12px radius** (NOT pill); purple primary; `rgba(133,91,251,0.16)` subtle variant for secondary
- **Cards:** subtle whisper shadows `rgba(0,0,0,0.03) 0px 4px 24px`; 8-16px radius
- **Semantic:** Green #149e61 for success/positive (financial confirmation states)
- **Subtle secondary:** rgba(133,91,251,0.16) = 16% opacity primary fill for secondary interactive elements
- **Key lesson:** Kraken runs almost the same purple as Habitiq. 12px button radius instead of pill keeps it professional, not consumer. The 16% opacity tinted secondary button is a pattern Habitiq should use for secondary actions (instead of ghost outline). Whisper shadows are appropriate on white surfaces — won't apply to Habitiq's dark canvas.

### 16. Lamborghini
- **Canvas:** #000000 absolute black — no warmth, total void
- **Typography:** LamboType; UPPERCASE everything at display scale; weight 400 (typeface carries identity, not weight); line-height **0.92** at 120px (ultra-compressed block)
- **Buttons:** **0px radius** (sharp rectangles); Lamborghini Gold #FFC000 PRIMARY ONLY; transparent ghost secondary with white 50%-opacity border
- **Depth:** Surface layering: #000→#181818→#202020 (no shadows on black — they'd be invisible)
- **Spacing:** 48-56px section padding (tight for luxury — confidence in form, not space)
- **Key lesson:** "Darkness as whitespace" — empty black BETWEEN content is the premium signal. One accent with absolute restraint (gold used for primary CTA ONLY, never decorative). For Habitiq: violet is sacred — use ONLY for primary actions. Surface ladder confirmed: #000→#181818→#202020 is identical to Habitiq's #0a0a0a→#141414→#1e1e1e pattern.

### 17. Linear
- **Canvas:** #010102 (deepest in collection — pure black with faint blue tint)
- **Typography:** Linear Display 600; -3.0px at 80px → -1.8px at 56px → -1.0px at 40px — most aggressive negative tracking in collection
- **Primary:** Lavender-blue #5e6ad2 — used SCARCELY: brand mark, primary CTA, focus ring, link emphasis
- **Surface ladder:** canvas (#010102) → S1 (#0f1011) → S2 (#141516) → S3 (#18191a) → S4 (#191a1b) — 4-step system
- **Buttons:** 8px radius, compact 8px×14px padding; lavender CTA; white inverse CTA for section openers
- **Cards:** 12px standard (hairline border), 16px product screenshots
- **Eyebrow:** 13px / 500 / +0.4px tracking (slight positive = taxonomy signal against neg-tracked display)
- **Key lesson:** Linear confirms: deepest dark canvas + scarcely-used lavender accent = premium tech brand. The -3.0px tracking at 80px is the most aggressive pattern in 17 brands — apply to Habitiq hero headlines. Eyebrow positive tracking (+0.4px) contrasts against negative display tracking to mark it as taxonomy.

### 18. Lovable
- **Canvas:** cream #f7f4ed (warm parchment — distinct from pure white or gray)
- **Typography:** Camera Plain Variable; -1.5px at 60px, -1.2px at 48px, -0.9px at 36px, 0 at body; weights 400/600 only
- **Buttons:** 6px radius; charcoal (#1c1c1c) primary WITH INSET SHADOW: `rgba(255,255,255,0.2) 0px 0.5px 0px 0px inset, rgba(0,0,0,0.2) 0px 0px 0px 0.5px inset, rgba(0,0,0,0.05) 0px 1px 2px 0px` — tactile pressed-surface feel
- **Cards:** cream bg + `1px solid #eceae4` border, 12px radius — NO shadows; border-only containment
- **Opacity system:** all grays derived from `#1c1c1c` at varying opacity (0.03, 0.04, 0.40, 0.82)
- **Key lesson:** The inset shadow on dark buttons is a signature detail that makes charcoal CTAs feel tactile rather than flat. For Habitiq violet buttons: `rgba(255,255,255,0.1) 0px 1px 0px inset` at top edge gives the same effect. Letter-spacing scaling formula confirmed: -1.5px at 60px ≈ -2.5% of font size — applies cleanly to Inter.

### 19. Mastercard
- **Canvas:** cream #F3F0EE; Ink Black #141413 (NOT pure black — warm undertone)
- **Typography:** MarkForMC 500 display / **450 body** (half-step weight); negative -2% tracking headlines; +4% uppercase eyebrows
- **Buttons:** 20px radius (between card and pill); cream text on dark button (tinted text, not pure white)
- **Extreme radius:** 40px stadium, 50% circle, 999px pill — skips the 8-12px middle ground entirely
- **Cards:** either 40px (hero, stadium) OR 50%/999px (circle/pill) — nothing in between
- **Decorative:** orbital light-orange arcs between service cards; ghost watermark text at section level
- **Key lesson:** Committing to EITHER small (4-8px) OR large (40px+) radius creates stronger visual identity than staying in the middle. For Habitiq: 8px buttons / 12px standard cards / 16px feature / 24px highlight — clean ladder with no muddy middle. The "ghost watermark" pattern (low-opacity primary-color text at large scale behind section content) is a premium depth trick.

### 20. Meta
- **Canvas:** white; dual CTA system: marketing → black pill; commerce → cobalt #0064e0 pill
- **Typography:** Optimistic VF 500 display with ss01/ss02 OT features; 300 for editorial subheads; body -0.14 to -0.16px letter-spacing
- **Buttons:** 100px radius (pill ALWAYS); 14px/700 labels; two-tier color: black marketing + cobalt commerce
- **Cards:** 32px standard feature (xxxl), 40px hero, 16px utility
- **Section rhythm:** 80px commerce, 120px hero sections
- **Semantic badges:** yellow warning, green success, red critical — all pill-shaped
- **Key lesson:** Two-tier CTA system is critical: marketing/secondary CTAs = neutral (black/surface); primary action CTAs = brand color (violet for Habitiq). This preserves the violet's signal value. Pill buttons (100px) as signature for primary actions. 300 weight for editorial REST vs 500 for display ENERGY — creates visual breathing in long-form layouts.

---

## Cross-Brand Pattern Extraction (for Habitiq)

### Color Architecture — Cumulative Confirmations After 20 Brands

| Pattern | Confirming Brands | Habitiq Application |
|---------|------------------|---------------------|
| Single chromatic accent | HashiCorp (per-product), HP (1 blue), IBM (1 blue), Linear (lavender), Lamborghini (gold) | Violet is the ONE chromatic color. Never dilute. |
| Surface ladder (no shadows) | Lamborghini, Linear, HashiCorp, Binance, Framer | Canvas → S1 → S2 → S3; shadows only on modals/popovers |
| Semantic financial green/red | Binance, Kraken | income: #22c55e text-only; expense: #ef4444 text-only |
| 16% opacity primary for secondary | Kraken (`rgba(133,91,251,0.16)`) | Secondary button: `rgba(124,58,237,0.12)` bg + violet text |
| Two-tier CTA (neutral + brand) | Meta, IBM | Secondary actions = surface-1 button; Primary = violet fill |
| Per-category accent tints | HashiCorp per-product, Clay per-feature | Expense category micro-accents (food, transport, etc.) |
| Warm dark tint vs pure black | Linear (#010102 blue tint) | #0a0a0a warm near-black is correct ✓ |

### Typography — Confirmations After 20 Brands

| Pattern | Brands | Habitiq Rule |
|---------|--------|--------------|
| Negative tracking scales with size | Intercom, Linear, Lovable, Mastercard | Hero: -2.5px / Section: -1.5px / Card title: -0.8px / Body: 0 |
| Positive tracking eyebrows | Linear (+0.4px), HashiCorp (+0.6px), HP (+0.7px buttons) | Eyebrow labels: Inter 11px/600/uppercase/+0.8px |
| Body +0.1-0.16px on dark | IBM (0.16px), confirmed benefit | Body on dark: letter-spacing +0.1px for readability |
| Tight display LH, relaxed body | HashiCorp (1.17/1.71), Intercom (1.05/1.50) | Hero LH 1.05-1.10 / Body LH 1.5-1.6 |
| Weight 300 = editorial rest | IBM (display!), Meta (editorial subheads) | Use Inter 300 sparingly for "quiet" intro paragraphs only |
| Inter fallback for all custom faces | HashiCorp (→Inter), Linear (→SF Pro→Inter), Kraken (→IBM Plex) | Inter is the universal fallback — already our font ✓ |

### Button Design — Final Rules After 20 Brands

| Pattern | Brands | Habitiq Button |
|---------|--------|---------------|
| 8px radius universal standard | Airbnb, Cal, Intercom, Linear, HashiCorp | 8px confirmed ✓ |
| 12px max for non-pill rounded | Kraken, Airtable | Feature/pricing CTAs: 12px |
| Pill (9999px) only hero/onboarding | Apple, Binance, Meta | Onboarding hero CTA: pill; App UI: 8px |
| Inset highlight on dark button | Lovable | `rgba(255,255,255,0.08) 0px 1px 0px inset` on violet button |
| 16% opacity secondary | Kraken | Secondary: `rgba(124,58,237,0.12)` bg + violet text + no border |
| Ghost/outline for tertiary | Lamborghini, HP, Framer | Ghost: transparent + `rgba(255,255,255,0.16)` border |
| 44px min height | Universal | 44px ✓ |

### Card Design — Confirmed After 20 Brands

| Pattern | Brands | Habitiq Cards |
|---------|--------|--------------|
| 8px button / 12px card / 16px feature / 24px highlight | Cal, HashiCorp, Intercom, Mastercard | Radius ladder confirmed ✓ |
| Hairline border (no shadow) on dark | Linear, HashiCorp, Lovable | `1px solid rgba(255,255,255,0.08)` on dark cards |
| Inverted dark card = featured tier | Intercom, Cal.com | Featured pricing: surface-2 + violet border accent |
| Ghost watermark at section level | Mastercard | Low-opacity violet text behind section header (landing page only) |
| Category color identity | HashiCorp per-product | Expense categories: subtle category color in icon/badge only |
| Product screenshots inside cards | Linear, Intercom, Cal, Clay | Show real app UI in feature cards — not illustrations |

### Layout & Spacing — After 20 Brands

| Pattern | Brands | Habitiq |
|---------|--------|---------|
| 96px sections (editorial/marketing) | HashiCorp, IBM, Airtable, Cal | Landing pages: 96px between sections |
| 80px sections (utility/commerce) | HP, BMW corporate, Meta | Dashboard inner bands: 80px |
| 64px hero padding | Lamborghini, BMW M, Meta | Hero sections: 64-80px padding |
| 24px card gaps | Universal | Grid gap: 24px (gap-6) ✓ |

### Mobile Patterns — After 20 Brands

| Pattern | Brands | Habitiq Mobile |
|---------|--------|---------------|
| Sticky bottom bar (primary CTA) | Airbnb, Meta (PDP rail) | Bottom nav ✓; action bar for key screens |
| Two-tier typography scaling | All | Hero: 32-36px mobile vs 56-80px desktop |
| Full-bleed product cards mobile | Meta, HP | Cards go full-bleed on mobile (no side margin) |
| Pill nav collapses to hamburger | Linear, Meta, HashiCorp | Sidebar collapses to bottom nav ✓ |

### Web Layout Patterns — After 20 Brands

| Pattern | Brands | Habitiq Web |
|---------|--------|------------|
| 2-tier purchase/action CTA | Meta (marketing black + commerce cobalt) | Dashboard: surface button secondary + violet primary |
| Sticky purchase rail | Meta PDP, Airbnb | Budget summary: sticky right rail on desktop ✓ |
| 4-step surface ladder | Linear (canvas→S1→S2→S3→S4) | Confirmed 4-step system ✓ |
| Tech-specs 2-col table | Meta | Financial breakdown: label/value rows, tabular figures |
| 64px nav bar | Linear (56px), Meta (64px), HashiCorp (64px) | Top nav: 64px ✓ |

---

## Habitiq-Specific Extractions

### What to steal from each brand:

**From HashiCorp:**
- Eyebrow pattern: `Inter 11px / 600 / uppercase / +0.8px tracking` above every section header — creates reliable visual hierarchy
- Per-category identity tints: expense categories (food / transport / shopping / health) could each have a faint color tint for their icon without disrupting the monochrome system
- Line-height contrast: display 1.17 / body 1.60 — the gap creates editorial rhythm in Habitiq's dashboard sections

**From HP:**
- Lone-color philosophy reconfirmed: violet appears AT MOST twice per viewport (one primary CTA + one accent element)
- 80px section rhythm for the dashboard (utility-dense) vs 96px for landing page (editorial)
- Ink slab pattern: deep closing band for key CTAs / plan upgrade prompts (surface-1 with violet headline)

**From IBM:**
- `letter-spacing: +0.1px` on body text in dark contexts (IBM uses 0.16px on light, 0.1px appropriate for dark)
- Bottom-rule focus ring: `2px solid #7c3aed` under input ONLY (not full ring) — cleaner in dense financial forms
- IBM Plex Sans as fallback: if Inter fails, IBM Plex Sans is the best typographic fallback for financial UI

**From Intercom:**
- Negative tracking formula: `-2.5% of font size` px — at 56px hero → -1.4px, at 40px section → -1.0px, at 28px card title → -0.7px
- Dark featured pricing tier: `surface-1 bg + violet text headline + violet border` (not ink-fill — we're already dark)
- Sentence-case eyebrows instead of ALL CAPS — more legible at small sizes on dark backgrounds

**From Kraken (most domain-relevant — crypto finance):**
- **16% opacity primary for secondary buttons** — `rgba(124,58,237,0.12)` bg + violet-300 text — elegant on dark without outline
- 12px button radius option for more formal/professional contexts (pricing, settings)
- Green #149e61 for positive balance confirmations — slightly deeper than #22c55e, test both

**From Lamborghini:**
- Surface ladder identical to Habitiq confirmed: pure black → #181818 → #202020
- Ghost button: `transparent bg + rgba(255,255,255,0.2) border` for destructive/cancel actions
- Violet is sacred — use ONLY for the single most important action per screen, never decoratively

**From Linear (closest design DNA to Habitiq):**
- Aggressive tracking at hero: `-3.0px at 80px` → apply to Habitiq landing hero headlines
- Eyebrow positive tracking (+0.4px) as taxonomy signal: `Inter 13px/500/+0.4px` for filter labels and tab labels
- 4-step surface ladder confirmed (#010102 → #0f1011 → #141516 → #18191a) — very close to Habitiq (#0a0a0a → #141414 → #1e1e1e → #2a2a2a) ✓
- Compact button padding: `8px 14px` for dashboard UI buttons (not landing CTAs)

**From Lovable:**
- Inset highlight on primary button: `rgba(255,255,255,0.08) 0px 1px 0px 0px inset` — adds depth to flat violet button
- Opacity-based gray system: derive all surface variants from #ffffff at opacity levels (already doing via rgba borders) ✓
- Letter-spacing formula confirmed: -1.5px at 60px / -1.2px at 48px / -0.9px at 36px → clean linear scaling

**From Mastercard:**
- Ghost watermark: large violet text (`#7c3aed` at 6% opacity) behind section headers on landing page — premium depth without adding visual noise
- Radius commitment: 8px (buttons/inputs) → 12px (cards) → 16px (feature) → 24px (highlight) → 9999px (pill) — NO values between 8 and 12
- Warm cream button text (#f5f0ff tinted) instead of pure white on violet CTAs — more refined

**From Meta:**
- Two-tier CTA confirmed: surface-1 bg button (secondary/navigation) vs violet fill (primary action) — use consistently throughout app
- Pill exclusively for hero/onboarding CTAs (100px radius); all in-app buttons stay at 8px
- 80px section rhythm for the commerce-like sections (upgrade prompts, plan selection)

---

## Cumulative Token Table (Updated Through 20 Brands)

```
Background:         #0a0a0a (slightly warm near-black — confirmed × 3 batches)
Surface-1:          #141414 (Framer/Linear/Lamborghini exact match region — confirmed)
Surface-2:          #1e1e1e (card base — confirmed)
Surface-3:          #2a2a2a (hover/active state — confirmed)
Border:             rgba(255,255,255,0.08)   [hairline — all dark systems]
Border-strong:      rgba(255,255,255,0.16)   [interactive emphasis]

Primary:            #7c3aed   [violet-600]
Primary-hover:      #6d28d9   [violet-700]
Primary-disabled:   rgba(124,58,237,0.3)
Primary-subtle:     rgba(124,58,237,0.12)    [NEW: Kraken 16% tint — secondary button bg]
Primary-ghost:      rgba(124,58,237,0.06)    [very subtle tint for hover states]
On-primary:         #f8f5ff                  [NEW: Mastercard — tinted white, not #ffffff]

Income-positive:    #22c55e — text only, NEVER card fill (confirmed × 3 batches)
Expense-negative:   #ef4444 — text only, NEVER card fill (confirmed × 3 batches)
Neutral-stat:       #a78bfa (violet-400)
Stat-large:         #7c3aed at 700 weight — inline on canvas, no card wrapper

Display font:       Inter 700 / hero: -2.5px LS / section: -1.5px LS / card-title: -0.8px LS
Body font:          Inter 400 / letter-spacing: +0.1px (dark readability — NEW from IBM)
Label font:         Inter 600 / uppercase / letter-spacing: +0.8px / 11px
Eyebrow:            Inter 500 / 13px / letter-spacing: +0.4px / sentence-case [NEW from Linear]
Number font:        font-variant-numeric: tabular-nums OR JetBrains Mono for currency amounts

Button radius:      8px (app UI) / 12px (formal: pricing, settings) / 9999px (hero/onboarding)
Card radius:        12px standard / 16px feature / 24px highlight
Section containers: 16px radius desktop, full-bleed mobile
Input radius:       8px (matches buttons)
Input focus:        2px solid #7c3aed bottom-rule ONLY [NEW from IBM Carbon]

Button inset:       rgba(255,255,255,0.08) 0px 1px 0px inset [NEW from Lovable]
Button height:      44px app / 48px landing page hero CTAs

Section rhythm:     96px landing / 80px dashboard bands (NEW refined split — HP/Meta)
Card padding:       24px content / 32px feature / 48px full-bleed sections
Spacing ladder:     xs:8 sm:12 md:16 lg:24 xl:32 2xl:48 3xl:64 section:96

Depth system:       Canvas → S1 → S2 → S3 (NO shadows on flat cards)
Shadow:             0 4px 16px rgba(0,0,0,0.24) popovers/modals ONLY
Atmospheric:        radial-gradient(rgba(124,58,237,0.15) → transparent) hero only

Ghost watermark:    rgba(124,58,237,0.06) large-scale text behind section headers [NEW from Mastercard]
Category tints:     rgba(accent,0.12) icon bg per expense category [NEW from HashiCorp per-product]
```

---

## New Patterns Discovered in Batch 3

| # | Pattern | Source | Habitiq Application |
|---|---------|--------|---------------------|
| 11 | Bottom-rule focus ring (not full ring) | IBM Carbon | `border-bottom: 2px solid #7c3aed` on input focus |
| 12 | 16% opacity primary as secondary button | Kraken | `rgba(124,58,237,0.12)` bg secondary = no outline needed |
| 13 | Eyebrow sentence-case +0.4px tracking | Linear | Filter labels, tab labels: Inter 13/500/+0.4px |
| 14 | Inset highlight on primary CTA | Lovable | `rgba(255,255,255,0.08) inset` top edge on violet button |
| 15 | Ghost watermark headline | Mastercard | Low-opacity violet text behind section content |
| 16 | Tinted white button text | Mastercard | `#f8f5ff` (not pure #fff) on violet CTA |
| 17 | Category color identity system | HashiCorp | Expense category icon/badge color per category |
| 18 | +0.1px body tracking for dark | IBM | Inter body: letter-spacing 0.1px on dark canvas |
| 19 | 96px vs 80px section split | HP, Meta | Landing=96px / Dashboard=80px |
| 20 | 4-step surface ladder (confirmed × 4) | Linear, Lamborghini, HashiCorp, Framer | #0a0a0a → #141414 → #1e1e1e → #2a2a2a ✓ |
