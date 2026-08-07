# Batch 05 Analysis — Brands 31-40
# pinterest, playstation, posthog, raycast, renault, replicate, resend, revolut, runwayml, sanity

---

## Brand Snapshots

### 31. Pinterest
- **Canvas:** White #ffffff + surface-soft #fbfbf9 + surface-card #f6f6f3 (warm cream neutrals)
- **Primary:** Pinterest Red #e60023 — ONLY on Sign-up CTAs, active tab indicator, wordmark. One saturated CTA on white = maximum contrast
- **Font:** Pin Sans (proprietary) — 70px / 600 / -1.2px LS. Sub: Inter or Manrope + -1.2px tracking at display
- **Radius system:** Only 3 values — 16px standard (buttons, pin cards, inputs, feature cards), 32px large (pin-card-large, modals), pill 9999px (search, chips, circular buttons)
- **Pin card:** 0px padding, full-bleed image IS the card. Pin overlay pill (white pill) anchored at image corner
- **Masonry grid:** Column-based, mixed aspect ratios, 8px gutters (extremely tight — imagery "touches")
- **Filter chips:** Default surface-card bg, active inverts to ink fill + white text (full flip on selection)
- **Modal:** Centered 32px radius card, 50% scrim, 16px ambient shadow — the ONLY shadow in the system
- **Section rhythm:** 64px (lighter than most SaaS at 96px)
- **Key lessons:** Full-flip on active filter chip (transparent→ink is more distinct than color tint). Card with zero internal padding where data is the card. Tight 8px masonry gutter = dense browsable content. 16px/32px two-radius vocabulary.

### 32. PlayStation
- **Canvas:** Three-chapter system — pure black (#000) hero/nav, white (#fff) utility, PS Blue (#0070d1) CTA band/footer
- **Primary:** PlayStation Blue #0070d1 — universal pill CTA (48px height, rounded.full). Reserved for primary actions + footer surface
- **Commerce CTA:** #d53b00 orange — ONLY for buy/purchase/store actions. Two-semantic CTA separation
- **Font:** PlayStation SST weight 300 (!!) for ALL display (54/44/35/28/22px) — airy editorial light weight. Inter (400/500) or Roboto Light (300) as sub
- **Cards:** 8px radius (rounded.md) — game tiles, product cards. 4px for inputs
- **Chapter alternation:** Full-bleed band color change IS the section divider — no whitespace between bands
- **PS Plus gold gradient:** 3-stop gold gradient ONLY for PS Plus tier banner — tier-specific asset
- **Commerce link:** `rgba(0,0,0,0.6)` body text on light, `rgba(255,255,255,0.7)` on dark — translucent body text tokens
- **Section rhythm:** 96px hero bands
- **Key lessons:** Light weight 300 at large sizes creates editorial elegance not weakness. Two distinct CTA semantics (brand primary vs commerce/purchase). Full-bleed band chapter system as page structure. Translucent body text tokens (rgba opacity) instead of gray hex = consistent relative contrast across surfaces.

### 33. PostHog
- **Canvas:** Warm cream #eeefe9 (distinctive — olive-tinted, NOT white) — runs uninterrupted top to bottom
- **Primary:** Yellow-orange #f7a501 — the ONLY saturated color. Everything else is olive/cream/white
- **Font:** IBM Plex Sans Variable — 400/500/600/700/800 weights. Weight 800 ONLY for display (24-36px). Body 400/1.5
- **Radius:** 6px (rounded.md) for ALL cards + CTAs, 8px rare larger containers. NO pills except sticky nav CTA
- **Cards:** White #fff on cream canvas — implicit elevation via surface contrast (no shadow, no border — just white card on cream bg)
- **Callout banner family:** 4 semantic pastels — blue-soft/green-soft/red-soft/purple-soft ONLY inside docs. Not marketing
- **Code blocks:** Dark olive (#23251d) on white doc card — most cinematic surface in system
- **Sub-nav strip:** Secondary nav bar (#e5e7e0 bg) directly below primary nav — section anchor links
- **Active tab:** Tab lifts from cream to white card bg (cream→white surface flip is the selection signal)
- **Mascots:** Hedgehog illustrations are the ENTIRE decoration system — no photography, no gradients
- **Section rhythm:** 80px
- **Key lessons:** White card on cream/dark canvas = implicit elevation without shadow. Callout banner 4-color family (semantic tip/warning/success/info) as documentation vocabulary. Sub-nav strip below primary nav for section-specific navigation. Weight 800 only at display sizes.

### 34. Raycast
- **Canvas:** Pure near-black #07080a with 4-step surface ladder: #07080a → #0d0d0d → #101111 → #121212
- **Primary:** White #ffffff — the ONLY CTA color. "Download", "Get Pro", "Install" — white IS the brand action on this canvas
- **Font:** Inter with `font-feature-settings: "calt", "kern", "liga", "ss03"` site-wide. The ss03 alternate 'g' glyph IS the brand typographic signature — without it, the chrome reads as "plain Inter"
- **Radius:** 4px (keycaps/badges), 6px (command rows), 8px (buttons/inputs), 10px (feature cards), 16px (hero container), 9999px (pill tabs). Cards cluster at 6–10px — tight, product-like
- **Elevation:** Pure surface-color ladder, zero drop shadows. Hairline 1px solid #242728 on every card edge
- **Accents:** Yellow/red/green/blue reserved ONLY for extension/feature illustrations — never on chrome
- **Signature:** Red diagonal stripe gradient (#ff5757→#a1131a) ONCE per page at hero top only — no more than 1 per page
- **Keycap:** Subtle gradient physical-key feel at 4px radius, 20px height
- **Section rhythm:** 96px
- **Key lessons:** ss03 Inter as brand signature = zero cost but distinctive rendering. White pill as CTA on near-black = maximum contrast. Saturated accents held to illustrations only. Surface ladder without shadows.

### 35. Renault
- **Canvas:** White #ffffff (browsing) + pure black #000000 (storytelling) — hard chapter switches, no gradients
- **Primary:** Sunlight Yellow #ffed00 — ONLY on primary CTAs, "NEW" badges, one accent promo tile per band. Yellow ALWAYS pairs with black text (never white)
- **Font:** NouvelR (proprietary) — every element including body. Tight 0.95 lineHeight at display for stacked headlines. Sub: Inter Tight, Manrope, HK Grotesk Semi Condensed
- **Radius:** 2px buttons (near-flat), 0px tiles/vehicle cards, 46px pill for sub-nav chips only, full (9999px) for color swatches. Square photography is non-negotiable
- **Chapter system:** White catalogue → Black storytelling → Yellow accent moment → Black footer. Pattern repeated throughout page
- **Vehicle cards:** Full-bleed photography, copy beneath — NEVER overlaid. Square-cornered always
- **Section rhythm:** 80px (collapses to 40px on mobile)
- **Inputs:** Bottom-border-only (top/sides borderless) = catalogue precision feel
- **Key lessons:** Near-flat 2px radius on buttons as automotive brand detail. Three-mode surface system with Yellow as the punctuation. Square-cornered photography as non-negotiable brand rule. Single accent color (yellow) with zero ambiguity.

### 36. Replicate
- **Canvas:** Warm cream #f9f7f3 (NOT white — critical distinction). surface-bone #f3f0e8 for inset cards. White #ffffff only on individual cards
- **Primary:** Orange #ea2804 — ONLY on primary CTA, home hero band, inline links. "Stamp use" — one orange per viewport
- **Font stack:** rb-freigeist-neue (display 30-128px), basier-square (body/UI 14-18px), JetBrains Mono (all code). Three families, three lanes, no overlap
- **Typography:** 128px xxl with -3px tracking, lineHeight 1.0. Display type IS the decoration
- **Radius:** ALL interactive = full pill (9999px): buttons, inputs, badges, avatars. Cards = 10px (md), 16px (lg). Clear two-tier: interactive=pill, content=rounded
- **Code wells:** Dark #202020 background always. "Code is print, not an inline grey box"
- **Elevation:** Cream canvas → surface-bone inset → surface-dark code → surface-deep footer. No drop shadows
- **Contributor mosaic:** Horizontal scroll of circular avatars over textured cream — brand-level community signal
- **Section rhythm:** 96px with 160px band for editorial breathing
- **Key lessons:** Cream canvas as brand temperature (warm ≠ clinical). Three-family stack with strict lane discipline. Interactive=pill, content=rounded as two-tier radius system. Code wells are dark surfaces, never light gray boxes.

### 37. Resend
- **Canvas:** True black #000000
- **Primary:** White #fcfdff — CTA button and hero headlines are the loudest elements. "White IS the brand on black"
- **Font stack:** Domaine Display serif (76-96px hero, ss01/ss04/ss11 features), ABC Favorit (marketing body), Inter (UI labels), Geist Mono (code). Editorial serif on dark = brand voice
- **Accents:** Six atmospheric glow tokens (orange/blue/green/red/yellow) — ONLY as low-opacity radial washes. The solid version of each accent appears ONLY inside code-window traffic lights
- **Radius:** 8px buttons (md), 12px cards/code-wells (lg), pill for pills/avatars. Consistent two-tier
- **Elevation:** Translucent white borders (6% / 14% opacity) replace shadows entirely. No traditional drop shadows
- **Email mockup:** Single white card centered on black canvas — the ONLY intentional light surface, reads as print pull-quote
- **Atmospheric glows:** Each section opener gets ONE glow. Never two glows in same section
- **Section rhythm:** 96px with 128px band for hero
- **Key lessons:** Editorial serif on developer-tool dark = unexpected brand differentiation. Atmospheric glows at section tops instead of photography. White-on-black CTA = maximum contrast as design decision. Six accent colors held to glow-only.

### 38. Revolut
- **Canvas:** True black #000000 (storytelling) + white #ffffff (catalogue) — slam-cut between modes
- **Primary:** White pill on black = the hero CTA. Cobalt violet #494fdf reserved ONLY for featured plan card and brand wordmark glyph
- **Font stack:** Aeonik Pro 500 (display 20-136px), Inter 400/600 (body/UI). Tight 1.0 lineHeight at display with large negative tracking
- **Typography:** 136px xxl with -2.72px tracking. Aeonik Pro 500 is the brand, never for body copy
- **Radius:** ALL buttons/pills = full pill (9999px), feature/plan cards = 20px (lg), inputs = 12px (md)
- **Product mockups:** Phone/card/terminal shown full-bleed in dark sections — "the asset IS the section"
- **Accent palette:** Teal/pink/green/warning-orange/yellow/brown — wide palette ONLY inside product illustrations/mockups, never button surfaces
- **Plan card highlight:** Cobalt violet tile lifts the featured plan. All other plans on #16181a surface elevated
- **Section rhythm:** 88px with 120px band
- **Key lessons:** Canvas-inversion button logic (white pill on black, black pill on white). Product mockup as section = no separate decoration needed. Wide accent palette contained to illustrations. Featured plan = single color tile among dark siblings.

### 39. RunwayML
- **Canvas:** True black #000000 / near-black #030303. Interface IS invisible
- **Primary:** No brand accent — cinematic photography IS the color palette. Interface deliberately colorless
- **Font:** abcNormal — single typeface for EVERYTHING from 48px display to 11px micro labels
- **Weight 450:** Unusual intermediate weight for small uppercase labels — precision craft signal
- **Tight everywhere:** lineHeight 1.0 at display, max 1.30 even for body. Editorial film-production feel
- **Uppercase labels:** `text-transform: uppercase` with 0.35px positive LS for navigational structure — contrasts with tight lowercase content text
- **Radius:** 4-8px only (no pills at brand surface level). Small radius = not playful
- **Zero shadows, minimal borders:** One border color (#27272a) for barely-visible containment. Interface "retreats"
- **Section rhythm:** 48-78px (generous but variable — "cinema-grade breathing")
- **Key lessons:** When visual content IS the brand, interface chrome must be invisible. Single typeface across all sizes = discipline over variety. Weight 450 as typography micro-craft detail. Film-production philosophy: depth from composition, not CSS shadows.

### 40. Sanity
- **Canvas:** Near-black #0b0b0b — NOT pure black, NOT near-black as dark mode, but as primary natural state
- **CTA:** Coral-red #f36458 — the only warm element in an otherwise cool achromatic system
- **Interactive:** Electric blue #0052ef = universal hover/active across ALL interactive elements without exception
- **Font:** waldenburgNormal (display + all UI), IBM Plex Mono (code/technical labels/uppercase tags)
- **Extreme tracking:** -4.48px at 112px, -2.88px at 72px, -1.68px at 48px — machined letterform quality
- **Radius gap:** 3px inputs, 5px ghost buttons, 6px cards, 12px large containers → JUMPS directly to 99999px pill. No middle ground
- **Achromatic neutral:** Pure gray scale — zero warm or cool tinting. Disciplined neutrals
- **Depth:** Pure colorimetric: #0b0b0b → #212121 → #353535 → #ffffff surface ladder. Ring-shadows (0 0 0 Npx) not offset shadows
- **OpenType:** "cv01", "cv11", "cv12", "cv13", "ss07" for display typography
- **Section rhythm:** 64-120px
- **Key lessons:** Universal hover state (one consistent color for all interactions) = product-grade feel. Achromatic neutrals as discipline (no "almost gray" contamination). Radius gap (6px→pill, nothing in between) as decisive design choice. IBM Plex Mono uppercase as technical vocabulary.

---

## 10 New Patterns from Batch 05

| # | Pattern | Source | Habitiq Application |
|---|---------|--------|---------------------|
| #31 | **Inverted ink filter chip** | Pinterest (transparent→ink fill on active) | Expense category filter chips: transparent default, full ink/white active flip |
| #32 | **Two-semantic CTA split** | PlayStation (blue=brand, orange=commerce) | Habitiq: violet=primary actions, green=#22c55e=income add, red=#ef4444=expense add |
| #33 | **Warm cream canvas as brand temp** | Replicate #f9f7f3, PostHog #eeefe9 | Confirmed: Habitiq's #0a0a0a is the warm brand canvas — not clinical #000000 |
| #34 | **ss03 Inter as zero-cost brand signature** | Raycast (ss03 alternate 'g' site-wide) | Add `font-feature-settings: "calt", "kern", "liga", "ss03"` to Habitiq body — costs nothing, differentiates |
| #35 | **Universal hover = single accent color** | Sanity (#0052ef on every interactive element) | Habitiq: all interactive hover states → violet-400 (#a78bfa) as universal hover signal |
| #36 | **Massive negative tracking at display** | Revolut -2.72px/136px, Replicate -3px/128px, Sanity -4.48px/112px | Habitiq stat display (large balance figures): Inter 700 with -0.05em tracking |
| #37 | **Atmospheric accent glow (never solid)** | Resend (6 accent-*-glow tokens as radial washes only) | Habitiq: budget overage warning = violet glow wash at card top, never solid violet surface |
| #38 | **White CTA on dark = max contrast primary** | Raycast + Resend + Revolut (all use white pill on black as THE primary CTA) | Habitiq light-canvas components (e.g. onboarding, landing): white pill with dark text as primary CTA |
| #39 | **Interactive=pill, content=rounded two-tier** | Replicate (buttons/inputs/badges/avatars = full pill; cards = 10/16px) | Habitiq: buttons/chips/avatars = rounded-full; transaction cards/expense cards = rounded-2xl (16px) |
| #40 | **Radius gap as decisive design choice** | Sanity (6px → 99999px, nothing between 12 and pill) | Avoid muddling radius vocabulary — pick 2-3 values and commit. Habitiq: 12px cards, full pill for chips/buttons |

---

## Cumulative Token Table — Additions from Batch 05

These extend the running table from batches 01-04.

| Token / Pattern | Source Brand(s) | Confirmed Habitiq Decision |
|-----------------|-----------------|---------------------------|
| `--font-feature-settings: "calt" "liga" "ss03"` | Raycast | Add to Habitiq body element — Inter ss03 alternate 'g' as signature |
| White pill CTA on dark canvas | Raycast, Resend, Revolut | For any light-surface or onboarding page: white pill + dark text = primary |
| `--color-hover-universal: violet-400 (#a78bfa)` | Sanity pattern | Standardize ALL interactive hover states to single violet |
| Atmospheric glow token (radial, 15-20% opacity) | Resend pattern | Budget warning, overage state = glow wash, NOT solid color |
| Cream canvas temperature (#0a0a0a warmth ≈ Replicate #f9f7f3 warmth) | Replicate, PostHog | Confirmed: Habitiq's slightly warm near-black is intentional, not clinical #000000 |
| Three-lane font stack: display/body/code | Replicate, Resend | Habitiq: Inter (all UI), JetBrains Mono (amount display / transaction codes) |
| Two-radius tier: interactive=pill, content=16-20px | Replicate, Revolut | Buttons/chips/avatars = rounded-full; cards = rounded-2xl |
| Section rhythm 64px for dense/app, 96px for editorial | Pinterest (64px) vs most brands (96px) | Habitiq in-app screens: 64px rhythm. Marketing/landing: 96px |
| Translucent body text rgba() tokens | PlayStation | `rgba(255,255,255,0.7)` for secondary text in dark sections (not gray hex) |
| IBM Plex Mono uppercase for technical metadata | Sanity, Replicate | Habit codes, transaction IDs, dates in secondary positions: Mono font |
| Callout banner 4-color semantic family | PostHog (blue/green/red/purple-soft) | Budget alerts: 4 tinted callout variants (info/success/warning/error) |
| Zero-padding card where content IS the card | Pinterest pin-card | Habitiq chart cards, photo-led reward cards: 0px padding, content fills flush |
| Sub-nav strip pattern below primary nav | PostHog | Transaction list: period/category filter strip below nav bar |
| Yellow accent + black text rule | Renault | Any Habitiq accent-yellow element (badge, label) pairs with black text only |
| Square-cornered photography rule | Renault | Transaction merchant logos / receipts: no rounded corners on photo assets |

---

## Cross-Batch Running Insights (Batch 05 Additions)

### Pattern: CTA Color on Dark Canvas = White (Not Brand Accent)
Raycast, Resend, Revolut — three very different product categories — all arrive at the same conclusion: on a dark canvas, the primary CTA is a **white pill with dark text**, not the brand accent color. The brand accent is reserved for a single "featured" moment (Raycast's stripe, Revolut's featured plan, Resend's links). Habitiq's primary CTAs on dark cards should follow this: white pill, not violet pill.

### Pattern: Accent Scarcity = Impact
Every batch reinforces this. Brands that use their accent on 1-2 elements per viewport (Pinterest's red on one CTA, Renault's yellow on one tile, Replicate's orange on one CTA, Revolut's cobalt on one plan card) generate more impact than brands that spread it. **One accent element per viewport fold = design law across 40 brands.**

### Pattern: The Cream Canvas Club
Five brands out of 40 now use a warm cream/near-cream canvas (Replicate, PostHog, Pinterest light surfaces, clay from Batch 1, and one or two others). These brands share a personality: developer-friendly but human, technical but approachable. The cream warmth communicates this in a way pure white and pure black don't. Habitiq's #0a0a0a (slightly warm dark) is in this family.

### Pattern: Display Typography as Brand Voice
Brands investing in display typography (Resend's Domaine Display, Replicate's freigeist-neue, Revolut's Aeonik Pro, Sanity's waldenburgNormal, Renault's NouvelR) all land display sizes at 72-136px with `lineHeight: 1.0` and aggressive negative tracking. The oversized compressed headline IS the brand expression for these companies. For Habitiq's marketing surfaces, large Inter at weight 800+ with -0.04em to -0.06em tracking will signal this quality tier.

---

> Batch 05 complete — 40 of 73 brands analyzed.
> See PROGRESS.md for batch 06+ status.
