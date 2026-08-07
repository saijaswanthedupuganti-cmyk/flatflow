# Batch 04 Analysis — MiniMax, Mintlify, Miro, Mistral.ai, MongoDB, Nike, Notion, NVIDIA, Ollama, OpenCode.ai

> Distilled patterns relevant to Habitiq's dark-first, violet-primary, mobile-first financial product

---

## Brand Snapshots

### 21. MiniMax
- **Canvas:** White (#ffffff), ink: #0a0a0a, footer: #0a0a0a dark
- **Font:** DM Sans (NOT Inter) — 80px hero / -2px tracking / 1.10 LH; pill buttons universal (rounded.full)
- **Buttons:** Full pill ONLY — black pill primary, outline pill secondary. 9999px on every button/badge
- **Cards:** Two distinct families — vibrant product cards (32px radius "album covers" in coral/magenta/blue/purple) + quiet white documentation cards (16px radius). The 2× radius jump IS the visual signature
- **Product color encoding:** Each model line gets its own saturated color identity (M2.7=coral, Music=magenta, Hailuo=blue, Speech=purple). These colors NEVER appear on buttons or chrome
- **Depth:** Atmospheric depth via internal card radial gradients. Brand-tinted shadow on purple cards: `rgba(44,30,116,0.16) 0 0 15px`
- **Key lesson:** Product identity cards at 32px look like album covers — completely different visual register than utility cards at 16px. Product brand colors live on cards, NOT on CTAs.

### 22. Mintlify
- **Canvas:** White with atmospheric sky-gradient heroes (cream-to-blue, teal-to-mint)
- **Font:** Inter + Geist Mono (code) — the pairing IS the developer-tool brand voice
- **Signature accent:** Mint green (#00d4a4) — used SPARINGLY on hero CTA + featured tier border + active sidebar indicator ONLY
- **Featured pricing tier:** 2px solid mint border + brand-tinted glow `rgba(0,212,164,0.08) 0 8px 24px` — border AND shadow together
- **Sidebar section headers:** 11px / 600 weight / +0.5px tracking / UPPERCASE — creates group-label hierarchy at tiny size
- **Radius:** 12px cards (rounded.lg) universally; pills on all buttons/badges; 8px on compact UI
- **Key lesson:** Brand-tinted shadow on featured card (not just border — soft ambient glow in brand color below the border). Micro-uppercase group labels in sidebar.

### 23. Miro
- **Canvas:** White; Roobert PRO custom geometric typeface (NOT system fonts)
- **Brand yellow (#ffd02f):** ONLY on wordmark, top promo banner, and yellow tag chips — NEVER as button color
- **Pastel card family:** Yellow/rose/coral/teal cards at 28px radius (rounded.xxxl) — echoes sticky-note palette of live product
- **Enterprise pricing card:** Black fill — the most premium tier inverts completely to dark
- **stat-display token:** 64px / 500 / -1.5px tracking — dedicated token for marketing stat callouts ("100M+ users")
- **Section rhythm:** 96px marketing / 64px pricing
- **Key lesson:** stat-display is a distinct token (64px) separate from heading hierarchy. Pastel feature cards at 28px+ read as "product personality cards" not utility cards.

### 24. Mistral.ai
- **Canvas:** White + cream surfaces (#fff8e0) for form panels and footer
- **Typography pairing:** PP Editorial Old (near-serif) for hero/stat displays + Inter for all UI prose — editorial/sans contrast IS the brand voice
- **Buttons:** 8px radius (NOT pills) — sober, editorial geometry signals professional tool not consumer app
- **Cards:** 12px radius consistently. Cream-background cards for form panels, warm-bordered
- **Signature element:** Horizontal sunset stripe band (red→orange→yellow→cream multi-stop gradient) at the bottom of EVERY page — unmistakable brand continuity element
- **Primary CTA:** Saturated orange (#fa520f) — reserved for primary actions only, never background
- **Key lesson:** Display serif + UI sans pairing creates premium editorial personality. 8px rounded rectangle buttons vs pills = "professional tool" signal. Signature page-closing element creates brand rhythm.

### 25. MongoDB
- **Canvas:** White docs/pricing; deep teal hero (#001e2b) + bright green CTA (#00ed64) — the most recognizable CTA in databases
- **Font:** Euclid Circular A — geometric sans, confident and technical
- **Pill buttons:** Full pill everywhere (rounded.full)
- **Featured pricing:** Soft mint bg (#e3fcef) + bright green border + brand-tinted glow
- **Course category tags:** Colored rectangular badges (purple, orange, green, teal) — ONLY place non-brand color appears outside hero
- **Code mockup card:** Terminal-aesthetic dark card embedded IN the hero band — product demo inside the hero
- **Key lesson:** Deep colored hero band + distinctive CTA color = instant brand recognition. Code mockup embedded in hero gives immediate product preview without separate screenshot.

### 26. Nike
- **Canvas:** White + soft-cloud (#f5f5f5) — these two surfaces do 95% of all surface work
- **Typography:** Nike Futura ND 96px / line-height 0.9 (!) / uppercase / 500 weight for campaign display — tightest LH of any brand in this analysis
- **All CTAs:** Black pill (rounded.full) exclusively; OR white pill on dark images
- **Cards:** 0px radius — "product cards are photographs," zero chrome
- **Sale color (#d30005):** ONLY non-neutral in retail chrome — reserved entirely for pricing signal
- **Spacing:** 8px base unit; 48px section rhythm (tighter than SaaS — retail density)
- **Key lesson:** Line-height 0.9 on massive (80-96px) display creates ultra-tight cinematic typography. Photography replaces card chrome entirely. One semantic color reserved exclusively for financial signal.

### 27. Notion
- **Canvas:** White; deep navy hero (#0a1530); signature purple CTA (#5645d4)
- **Font:** Notion Sans (Inter-based custom); 80px hero / -2px tracking / 1.05 LH
- **Buttons:** 8px rounded rectangle (NOT pills) — professional tool geometry
- **Pastel card tints:** 9 tints (peach, rose, mint, lavender, sky, yellow, cream, gray + bold yellow) echoing database property colors
- **Bold yellow card (#f9e79f):** High-emphasis feature banner — the "attention-getting" variant in the card family
- **Hero workspace mockup:** Deep shadow `rgba(15,15,15,0.20) 0 24px 48px -8px` — this makes the product screenshot "float" above the hero
- **Key lesson:** Deep diffuse shadow on product mockup card makes it "float" dramatically above the hero band. Pastel card tint system echoes the live product's color system — brand AND product coherence.

### 28. NVIDIA
- **Canvas:** Black (#000) hero/footer, white (#fff) body — two surfaces only
- **Single accent:** #76b900 (NVIDIA Green) — every CTA, every active state, every link, the wordmark itself. NOTHING else
- **Radius:** 2px (!!) on everything — engineering-grade angular geometry
- **Corner square:** 12px solid green square anchored to one card corner — the system's only decorative motif
- **Zero shadows:** Not a single drop shadow in card/content surfaces. Hairline borders only
- **Dense section rhythm:** 64px sections — engineering documentation density
- **Key lesson:** The corner accent square — a tiny 12×12px brand-color square on a card corner — creates branded recognition without adding complexity. One-accent discipline taken to the extreme.

### 29. Ollama
- **Canvas:** Pure white + soft-cloud (#fafafa); pure black CTA — monochromatic
- **Font:** SF Pro Rounded (headings) + ui-sans-serif (body) + ui-monospace (code) — "stock Apple" feel signals native/trusted
- **Install snippet component:** 48px pill container, code font inside, copy button — the install command IS the primary hero CTA
- **Terminal card:** macOS traffic light dots (red/yellow/green) + code content + 12px radius — product demo component
- **Dark pricing tier:** Single inverted dark card (#171717) for top pricing tier — the only non-white surface in the system
- **Pill geometry universal:** Every button, input, and pill is rounded.full
- **Key lesson:** Terminal card with macOS traffic light dots is an instantly recognizable "developer trust" signal. The install snippet elevated to first-class hero component. Single inverted dark card for premium tier creates maximum contrast.

### 30. OpenCode.ai
- **Canvas:** Warm cream (#fdfcfc) — barely-there warmth vs pure white
- **Typography:** Berkeley Mono ONLY — 100% monospaced for every text role. The font IS the brand
- **ASCII bracket bullets:** [+] [-] [x] as feature list markers — no SVG icons, only text
- **Radius:** 4px only on interactive elements; 0px on all containers
- **Hero TUI mockup:** Dark near-black (#201d1d) full-bleed card showing actual terminal interface with ASCII wordmark and command line
- **Section rhythm:** 96px
- **Semantic ramp:** Full Apple HIG palette (blue/danger/warning/success) exists but confined to in-product TUI only — marketing stays monochrome
- **Key lesson:** The monospace-everything aesthetic signals "built by engineers for engineers" — maximum credibility with technical audience. TUI/CLI mockup as hero = show the real product.

---

## Cross-Brand Pattern Extraction (Batch 4)

### New Patterns Discovered (#21–#30)

| # | Pattern | Source Brands | Habitiq Application |
|---|---------|--------------|---------------------|
| 21 | Brand-tinted glow on featured card | Mintlify, MongoDB | Featured plan/card: `2px solid #7c3aed` + `rgba(124,58,237,0.06) 0 8px 24px` glow |
| 22 | stat-display token (large numbers) | Miro (64px), Mistral (56px stat) | Monthly spend total: 56-64px / 700 / tabular-nums — distinct from heading hierarchy |
| 23 | 8px rect buttons = "professional tool" | Mistral, Notion | Use 8px rect for form/utility buttons; pills only for hero/onboarding CTAs |
| 24 | Product mockup embedded in hero | MongoDB, Ollama, OpenCode | Hero card showing actual Habitiq dashboard chart/data — not illustration |
| 25 | Corner accent square ornament | NVIDIA | 8px violet square on featured/highlight card bottom-left corner |
| 26 | Page-closing signature element | Mistral (sunset stripe) | Consistent violet gradient or brand mark at every section/screen bottom |
| 27 | Line-height 0.9 on 80-96px display | Nike | Habitiq's large stat displays (budget total, month spent) can use LH 0.9 |
| 28 | 24-32px radius on pastel/color cards | Miro (28px), MiniMax (32px) | Category insight/summary cards (colored) → 24px; transaction cards → 12px |
| 29 | Install snippet as hero CTA component | Ollama | Onboarding "connect your account" or "download app" gets styled snippet component |
| 30 | Micro-uppercase group labels (11px) | Mintlify | Transaction category headers, settings groups: 11px / 600 / uppercase / +0.5-0.8px |

### Color Architecture Patterns

| Pattern | Multiple Brands | Habitiq |
|---------|----------------|---------|
| Product-identity cards have own color | MiniMax (coral/magenta/blue) | Expense categories get their own tint (food=amber, transport=blue, etc.) |
| Brand color NOT on buttons | Miro (yellow), MiniMax (coral) | Violet can appear on cards AND buttons — but must be disciplined |
| Single semantic reserve color | Nike (sale red only) | #ef4444 expense red ONLY for negative financial signal — never decorative |
| Deep teal/dark hero band | MongoDB, Notion | Dark band for hero/featured sections using #0a0a0a or deep violet |
| Warm cream canvas | OpenCode (#fdfcfc), Mistral (cream) | Habitiq's #0a0a0a already has slight warm undertone — keep it |

### Typography Discoveries

| Discovery | Source | Habitiq Impact |
|-----------|--------|---------------|
| LH 0.9 on 80-96px = cinematic | Nike | Large budget total display: `font-size: clamp(48px, 8vw, 80px); line-height: 0.9` |
| Display serif creates editorial trust | Mistral (PP Editorial Old) | Could use for landing page — Inter works fine for app |
| stat-display separate from h1 | Miro, Mistral, MongoDB | Add stat-display token: 56px / 700 / tabular-nums / -1px tracking |
| Monospace-everything = max tech trust | OpenCode, Ollama (code) | Transaction amounts: `font-variant-numeric: tabular-nums; font-feature-settings: 'tnum'` |
| Micro-uppercase 11px group labels | Mintlify | Category section headers in expense list: 11px / 600 / UPPERCASE / +0.5px |

### Card Design Synthesis

| Card Family | Radius | Background | Use Case |
|-------------|--------|-----------|---------|
| Transaction card | 12px | surface-1 (#141414) | Individual expense items |
| Category card (colored) | 24px | rgba(accent, 0.12) | Category overview cards |
| Feature/showcase card | 16px | surface-2 (#1e1e1e) | Dashboard section cards |
| Hero/product mockup | 12px | surface-1 + deep shadow | App screenshot in hero |
| Featured/highlighted | 12px | surface-1 + violet border + glow | Best plan, promoted content |
| Stat callout | transparent | — | Large number stat display |

### Button Design — Tool vs Consumer Spectrum
| Type | Radius | Signal | Habitiq Use |
|------|--------|--------|-------------|
| Hero CTA / onboarding | pill (9999px) | "Consumer-friendly, try me" | Get Started, Create Account |
| App primary action | 8px rect | "Professional tool" | Add Expense, Save Budget |
| Form submit | 8px rect | "Utility" | Save, Update, Confirm |
| Badge/status | pill (9999px) | "Label/category" | Income, Paid, Overdue tags |

### Layout & Spacing

| Pattern | Source | Habitiq |
|---------|--------|---------|
| 96px section rhythm (landing) | Miro, OpenCode, MiniMax | Landing/marketing pages: 96px |
| 64px section rhythm (docs/dense) | NVIDIA, Mintlify (docs) | Dashboard inner bands: 80px |
| stat-display with 88-120px hero | Ollama (88px), Mintlify (120px) | App hero: 96px padding |
| Two-surface architecture | NVIDIA (black/white), Ollama (white/dark-card) | Dark canvas + one dark featured card per screen |

---

## Habitiq-Specific Extractions

### What to steal from each brand:

**From MiniMax:**
- "Album cover" product cards at 32px for Habitiq's monthly summary or category highlights
- Each expense category gets its own brand-color tint card identity (food=amber, transport=blue, housing=violet, health=green)
- DM Sans is NOT the right choice — Inter stays for Habitiq

**From Mintlify:**
- Brand-tinted glow on the premium/featured pricing tier card: `rgba(124,58,237,0.06) 0px 8px 24px`
- Micro-uppercase sidebar group labels: category transaction groups, settings section headers
- Inter + code font pairing for any code/technical display in Habitiq

**From Miro:**
- stat-display token: 64px / 500 / -1.5px tracking for monthly total spend callouts
- Pastel category cards at 28px+ radius for feature summary areas
- Enterprise (premium) pricing card goes full dark — black fill = premium signal

**From Mistral:**
- 8px rect buttons for form/utility interactions in Habitiq's expense forms
- Cream-tinted panel for form cards (#1a1a1a with subtle warmth instead of pure #141414)
- Signature consistent element at page/screen bottom — Habitiq's bottom nav already does this ✓

**From MongoDB:**
- Deep colored hero + bright distinct CTA = instant brand recognition
- Embed actual product data/chart in hero/onboarding card
- Brand-tinted featured card glow (border + soft shadow together)

**From Nike:**
- Line-height 0.9 for the monthly budget total (large stat display at 48-64px)
- #ef4444 (expense red) reserved ONLY for negative financial signal — never decorative
- Card chrome restraint: transaction cards have minimal chrome, data is the hero

**From Notion:**
- Deep diffuse shadow `rgba(0,0,0,0.20) 0 24px 48px -8px` on product screenshot mockup in hero
- Pastel tint system for different financial categories (echoing database property colors)
- 8px rect buttons throughout app UI signals "serious productivity tool"

**From NVIDIA:**
- Corner accent square: 8px violet square on Habitiq's featured/highlight cards
- Zero-shadow card approach — relying on brightness-step surfaces (already our pattern)
- Single accent discipline: violet is doing ALL the work

**From Ollama:**
- Terminal card with traffic lights as "product preview" → Habitiq's "app preview card" on landing
- Dark card for premium tier creates maximum contrast against white/light surfaces
- Install snippet as styled hero component — "Connect your first bank account" styled elegantly

**From OpenCode:**
- TUI/product mockup as hero = always show the real product, not illustration
- Warm cream is subtler than stark white — #0a0a0a with slight warm tint is correct
- ASCII-style or monospace amounts for transaction lists (tabular-nums)

---

## Cumulative Token Table (Updated Through Batch 4)

```
=== SURFACES ===
Background:         #0a0a0a (confirmed ×4 batches)
Surface-1:          #141414 (card base — confirmed)
Surface-2:          #1e1e1e (elevated card — confirmed)
Surface-3:          #2a2a2a (hover/active — confirmed)
Border:             rgba(255,255,255,0.08) (confirmed)
Border-strong:      rgba(255,255,255,0.16) (confirmed)

=== BRAND ===
Primary:            #7c3aed (violet-600 — confirmed)
Primary-hover:      #6d28d9 (violet-700 — confirmed)
Primary-subtle:     rgba(124,58,237,0.12) bg + violet text [Kraken pattern]
On-primary:         #f8f5ff (tinted white — confirmed)

=== FINANCIAL SEMANTIC ===
Income-positive:    #22c55e — text only, never card fill
Expense-negative:   #ef4444 — text only, ONLY for financial signal (Nike reinforced)
Neutral-stat:       #a78bfa (violet-400)

=== TYPOGRAPHY ===
Display font:       Inter 700 / hero: -2.5px LS / section: -1.5px LS / card-title: -0.8px LS
Hero LH (large):    0.9 for 48-80px stat/display text [NEW — Nike]
Stat-display:       56-64px / 700 / tabular-nums / -1px LS [NEW — Miro/Mistral/MongoDB]
Body font:          Inter 400 / +0.1px LS [IBM dark readability]
Label font:         Inter 600 / uppercase / +0.8px / 11px
Group header:       Inter 600 / uppercase / 11px / +0.5px [NEW — Mintlify micro-label]
Eyebrow:            Inter 500 / 13px / +0.4px / sentence-case [Linear]
Number font:        font-variant-numeric: tabular-nums (confirmed via multiple financial brands)

=== BUTTONS ===
Hero/onboarding:    9999px pill — "consumer-friendly, try me"
App primary:        8px rect — "professional tool" [NEW — Mistral/Notion pattern]
Button height:      44px app / 48px landing
Button inset:       rgba(255,255,255,0.08) 0px 1px 0px inset [Lovable]
Secondary button:   rgba(124,58,237,0.12) bg + violet text [Kraken 16% tint]

=== INPUT ===
Input focus:        2px solid #7c3aed bottom-rule ONLY [IBM Carbon]

=== CARDS ===
Transaction card:   12px radius / surface-1
Category card:      24px radius / rgba(accent,0.12) bg [NEW — Miro/MiniMax pastel card]
Feature card:       16px radius / surface-2
Featured/promo:     12px radius + 2px violet border + rgba(124,58,237,0.06) 0 8px 24px glow [NEW — Mintlify/MongoDB]
Hero mockup card:   12px radius + rgba(0,0,0,0.20) 0 24px 48px -8px deep shadow [NEW — Notion]
Corner accent:      8px violet square on featured card corner (optional) [NEW — NVIDIA]

=== SPACING ===
Section rhythm:     96px landing / 80px dashboard bands [HP/Meta, confirmed]
Card padding:       16px standard / 24px feature / 32px hero

=== DECORATIVE ===
Ghost watermark:    rgba(124,58,237,0.06) large text behind section headers [Mastercard]
Category tints:     rgba(accent,0.12) icon bg per expense category [HashiCorp + MiniMax reinforced]
Corner accent sq:   8px solid #7c3aed square on featured card corner [NEW — NVIDIA]
```

---

## Batch 4 Quality Summary

**Most impactful discoveries:**
1. **Brand-tinted glow** (Mintlify/MongoDB) — border + soft shadow together on featured card is more expressive than just border
2. **stat-display token** (Miro/Mistral) — 56-64px dedicated to financial stat display with LH 0.9 at max scale
3. **8px rect vs pill button spectrum** (Mistral/Notion) — hero=pill, app=8px rect is the right split for a financial tool
4. **Category card 24px radius** (Miro/MiniMax) — colored category cards get bigger radius than utility cards
5. **Product mockup deep shadow** (Notion) — the floating product screenshot effect is achievable and dramatic

**Brands least relevant to Habitiq:**
- OpenCode (terminal-only aesthetic too alien)
- Nike (commerce/retail patterns; photography-first doesn't apply)
- NVIDIA (engineering-grade 2px radius too cold for financial wellness app)

**Patterns to NOT apply to Habitiq:**
- 0px or 2px radius buttons (too cold/angular for a personal finance app)
- Monospace-only typography (Berkeley Mono is a developer tool signal)
- Sunset stripe / ornamental gradient bands (Habitiq is utility-forward)
