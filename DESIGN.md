---
version: 2.0
name: Habitiq-Design-System
description: >
  Habitiq is built for real people who share real spaces with real flatmates.
  The design system is engineered from 71 world-class brand design systems —
  distilled into one rule: every element should feel like it was made by a
  thoughtful human, for humans who trust each other.

  The system is dark-first, violet-accented, Inter-based, and spring-physics-driven.
  It knows that a task list is also a relationship map. That an expense split is
  a moment of trust. That a completion ring is a small act of fairness.

  Every card, button, input, and animation in this document exists to make
  shared living feel manageable, fair, and even a little satisfying.

stack: "Next.js 16 · React 19 · TypeScript strict · Tailwind CSS v4 · Zustand v5 · Firebase/Firestore · Framer Motion · shadcn/ui · Lucide React"
---


## The Human Contract

> "If the person has read 70 books, he has a knowledge of sound."

We read 71 brand design systems. This is what we heard.

**From Airbnb:** Scarcity makes an accent feel like an invitation, not a decoration.
**From Intercom:** Design is a conversation. Every card speaks.
**From Wise:** A number with 900-weight confidence says *we believe in this*.
**From Mastercard:** Connect people with arcs, not just lists.
**From Starbucks:** Rewards feel earned when the ceremony matches the effort.
**From Stripe:** Financial precision is an act of respect for the user's money.
**From Framer:** Spring physics — not linear easing — feel like the real world.
**From Apple:** Materials have memory. A pressed button should feel pressed.
**From Linear:** Craft density earns trust without a single word.
**From Revolut:** Oversized numbers on dark say: this matters.
**From Slack:** People are the product. Show their faces.
**From Nike:** The moment of completion is the product's highest point.
**From Ferrari:** Restraint is a choice. Every element that remains had to earn its place.

**Our synthesis:** Habitiq should feel like a brilliant, calm flatmate who handles everything without drama. The UI is their voice — precise where money is involved, warm where people are involved, celebratory when fairness is rewarded.

---


## Colors

```yaml
colors:
  # ── Brand ──────────────────────────────────────────────────────────────────
  primary:            "#7c3aed"     # Habitiq violet — accent, progress, active states
  primary-soft:       "#a78bfa"     # Hover states, secondary violet moments
  primary-muted:      "#7c3aed1a"   # 10% violet — atmospheric glow surface
  primary-dark:       "#5b21b6"     # Pressed state
  on-primary:         "#0a0a0a"     # Dark text on violet fill (lit-surface signal)
  on-primary-light:   "#f5f5f5"     # Light text on deep violet

  # ── Canvas (dark-first, warm-tinted) ──────────────────────────────────────
  canvas:             "#0c0b0f"     # Page background — near-black with 1% warm violet push
  canvas-soft:        "#131117"     # Sub-nav, search bars, sticky headers
  canvas-card:        "#1a1820"     # Standard card — warm-dark, not neutral gray
  canvas-elevated:    "#211f28"     # Modals, bottom sheets, drawers
  canvas-raised:      "#2a2733"     # Tooltips, dropdowns, popovers

  # ── Ink (text) ─────────────────────────────────────────────────────────────
  ink:                "#f4f3f8"     # Primary text — warm white, not cold
  ink-soft:           "#a09db0"     # Secondary text, descriptions
  ink-mute:           "#514e61"     # Placeholders, disabled, captions
  ink-on-light:       "#0c0b0f"     # Text on any light surface

  # ── Hairline ───────────────────────────────────────────────────────────────
  hairline:           "#2a2635"     # Card borders, input borders, dividers
  hairline-soft:      "#1e1c28"     # Subtle section separators

  # ── Semantic (financial + habit status) ────────────────────────────────────
  positive:           "#22c55e"     # Paid, done, settled, on-track
  positive-soft:      "#22c55e18"   # Positive surface tint (12% opacity)
  positive-deep:      "#16a34a"     # Pressed positive

  warning:            "#f59e0b"     # Overdue, partial, streak-at-risk
  warning-soft:       "#f59e0b18"   # Warning surface tint
  warning-deep:       "#d97706"     # Pressed warning

  negative:           "#ef4444"     # Missed, debt, overage
  negative-soft:      "#ef444418"   # Negative surface tint
  negative-deep:      "#dc2626"     # Pressed negative

  # ── Warm Accent Secondaries ─────────────────────────────────────────────────
  streak-fire:        "#f97316"     # Orange — streak momentum (warm = in motion)
  reward-gold:        "#eab308"     # Gold — earned achievement
  reward-gold-soft:   "#eab30812"   # Gold surface tint

  # ── Member Colors (8 slots for 8-member cap) ───────────────────────────────
  # Each member is assigned a named color. This color follows them everywhere:
  # their avatar ring, their task assignment chip, their expense line in the
  # transaction list, their balance card. People get color; data gets neutral.
  member-amber:       "#f59e0b"     # Member slot 1
  member-teal:        "#14b8a6"     # Member slot 2
  member-rose:        "#f43f5e"     # Member slot 3
  member-sky:         "#0ea5e9"     # Member slot 4
  member-violet:      "#8b5cf6"     # Member slot 5 (softer violet, distinct from primary)
  member-lime:        "#84cc16"     # Member slot 6
  member-orange:      "#ea580c"     # Member slot 7
  member-cyan:        "#06b6d4"     # Member slot 8
  member-colors: ["#f59e0b","#14b8a6","#f43f5e","#0ea5e9","#8b5cf6","#84cc16","#ea580c","#06b6d4"]

  # ── Category Tints (expense categories get signature surface tints) ─────────
  # Inspired by Airtable's signature cards — each category has a warm color DNA.
  # Applied as a subtle 8% opacity tint on the left-border chip and icon background.
  cat-food:           "#f97316"     # Orange — groceries, dining
  cat-bills:          "#7c3aed"     # Violet — rent, utilities (brand-aligned)
  cat-transport:      "#0ea5e9"     # Sky — travel, rides
  cat-health:         "#22c55e"     # Green — pharmacy, gym
  cat-entertainment:  "#ec4899"     # Pink — OTT, events
  cat-shopping:       "#f59e0b"     # Amber — general purchases
  cat-other:          "#a09db0"     # Neutral — uncategorized
```

### Color Law: One Violet Moment Per Fold

Violet (`#7c3aed`) is the brand's accent. In any single viewport fold, only ONE element uses it:
- The active tab indicator — OR
- A habit completion ring — OR
- A primary CTA button — OR
- A progress bar fill

Never two. The moment it appears twice in the same fold, it stops being an accent and becomes decoration.

---


## Typography

```yaml
typography:
  # Display — Inter Variable with ss03 (curved g,a) + tighter negative tracking
  # Inspired by: Wise (weight 900 for hero numbers), Linear (precision tracking),
  # Revolut (64–80px display for financial confidence)

  display-xl:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 64px               # Up from 48px — Wise/Revolut confidence play
    fontWeight: 800
    lineHeight: 68px
    letterSpacing: -2px
    fontFeatureSettings: '"ss03" 1'

  display-lg:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 44px
    fontWeight: 700
    lineHeight: 48px
    letterSpacing: -1.5px
    fontFeatureSettings: '"ss03" 1'

  display-md:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 32px
    fontWeight: 700
    lineHeight: 36px
    letterSpacing: -0.8px
    fontFeatureSettings: '"ss03" 1'

  display-sm:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 24px
    fontWeight: 700
    lineHeight: 28px
    letterSpacing: -0.5px
    fontFeatureSettings: '"ss03" 1'

  display-xs:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 18px
    fontWeight: 600
    lineHeight: 24px
    letterSpacing: -0.2px
    fontFeatureSettings: '"ss03" 1'

  # Body — Inter, neutral tracking, generous line-height
  body-lg:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 17px
    fontWeight: 400
    lineHeight: 28px             # Increased from 26px — more breathing room
    letterSpacing: 0

  body-md:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 15px
    fontWeight: 400
    lineHeight: 24px             # Increased from 23px
    letterSpacing: 0

  body-sm:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 13px
    fontWeight: 400
    lineHeight: 20px
    letterSpacing: 0

  # Label / caption
  label-md:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 12px
    fontWeight: 600
    lineHeight: 16px
    letterSpacing: 0.6px
    textTransform: uppercase

  label-sm:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 11px
    fontWeight: 600
    lineHeight: 14px
    letterSpacing: 0.8px
    textTransform: uppercase

  caption:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 12px
    fontWeight: 400
    lineHeight: 18px
    letterSpacing: 0

  # Mono — amounts, counts, codes. tnum always.
  mono-lg:
    fontFamily: "JetBrains Mono, ui-monospace, SFMono-Regular, monospace"
    fontSize: 16px
    fontWeight: 600
    lineHeight: 22px
    fontVariantNumeric: tabular-nums

  mono-md:
    fontFamily: "JetBrains Mono, ui-monospace, SFMono-Regular, monospace"
    fontSize: 14px
    fontWeight: 500
    lineHeight: 20px
    fontVariantNumeric: tabular-nums

  mono-sm:
    fontFamily: "JetBrains Mono, ui-monospace, SFMono-Regular, monospace"
    fontSize: 12px
    fontWeight: 400
    lineHeight: 18px
    fontVariantNumeric: tabular-nums

  # Numeric — hero financial displays. The number that matters.
  # At 64px/800, it says: we believe in this figure.
  # At 36px/700, it says: this is important context.
  # At 24px/600, it says: this is relevant.
  numeric-hero:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 64px               # Hero amount — total spend, net balance
    fontWeight: 800
    lineHeight: 68px
    letterSpacing: -2px
    fontVariantNumeric: tabular-nums
    fontFeatureSettings: '"ss03" 1'

  numeric-xl:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 40px
    fontWeight: 700
    lineHeight: 44px
    letterSpacing: -1px
    fontVariantNumeric: tabular-nums

  numeric-lg:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 28px
    fontWeight: 700
    lineHeight: 32px
    letterSpacing: -0.5px
    fontVariantNumeric: tabular-nums

  numeric-md:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 20px
    fontWeight: 600
    lineHeight: 24px
    letterSpacing: 0
    fontVariantNumeric: tabular-nums

  numeric-sm:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 16px
    fontWeight: 600
    lineHeight: 20px
    letterSpacing: 0
    fontVariantNumeric: tabular-nums

  # Button
  button-md:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 15px
    fontWeight: 600
    lineHeight: 20px
    letterSpacing: 0

  button-sm:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: 13px
    fontWeight: 600
    lineHeight: 18px
    letterSpacing: 0
```

### Typography Principles

1. **ss03 globally.** Set `font-feature-settings: "ss03" 1` on `body`. Curved `g` and `a` give Inter warmth — zero-cost brand signature.
2. **tnum everywhere numeric.** Every amount, count, percentage — always `font-variant-numeric: tabular-nums`. Numbers must never shift layout.
3. **Hero numbers deserve hero treatment.** The main number on a screen — total spend, streak count, net balance — uses `numeric-hero` at 64px/800. It should feel like a confident statement.
4. **Negative tracking on display sizes.** Every headline above 18px carries negative letter-spacing. This is the precision signal.
5. **Line-height breathes.** Body text at 1.6× line-height (not 1.5×) creates the conversational warmth of a friendly message, not a dense report.

---


## Spacing

```yaml
spacing:
  xxs:  2px
  xs:   4px
  sm:   8px
  md:   12px
  lg:   16px
  xl:   20px         # Card interior default (up from 16px — more breathing room)
  2xl:  24px         # Generous card, spacious stats
  3xl:  32px
  4xl:  48px
  5xl:  64px
  6xl:  96px
```

### Breathing Room Law

Cards breathe. The minimum interior padding for any card is `{spacing.xl}` (20px), not 16px. This is borrowed from Spotify's album card and Apple's physical-material approach — things that feel real have space inside them.

```
Standard card:          padding: 20px
Stat / hero card:       padding: 24px
Empty state card:       padding: 48px (center content in open air)
Bottom sheet:           padding: 24px (top) / 20px (sides)
Modal:                  padding: 24px
List row:               padding: 14px 20px (vertical / horizontal)
```

---


## Shapes

```yaml
rounded:
  none:   0px
  xs:     4px         # Inline icons, micro badges
  sm:     8px         # Input fields, small chips, icon buttons
  md:     12px        # Canonical — cards, primary buttons
  lg:     16px        # Large section cards, promo panels
  xl:     20px        # Spacious feature cards
  2xl:    24px        # Bottom sheet top corners
  full:   9999px      # Pills — progress bars, badges, filter chips
```

**The 12px canonical radius.** Every card and every primary button uses `{rounded.md}` 12px. This is the brand's shape signature — not too soft (consumer toy), not too sharp (corporate tool). It reads as confident and considered.

**Inner radius harmony.** If a card has 12px radius, elements inside it have 8px or pill. Never let inner elements inherit outer radius — that's what makes "cards inside cards" look nested and stacked, not polished.

---


## Elevation & Depth

```yaml
elevation:
  # Standard cards: hairline only — no shadows (Linear / xAI approach)
  card:
    boxShadow: "none"
    border: "1px solid #2a2635"
    innerGlow: "inset 0 1px 0 rgba(255,255,255,0.04)"   # Subtle top-edge warmth

  # Elevated surfaces: stacked micro-shadow (Vercel pattern)
  modal:
    boxShadow: "0 0 0 1px rgba(0,0,0,0.7), 0 2px 4px rgba(0,0,0,0.5), 0 8px 16px rgba(0,0,0,0.4), 0 24px 48px rgba(0,0,0,0.3)"

  # Bottom sheets: upward lift
  sheet:
    boxShadow: "0 -1px 0 #2a2635, 0 -4px 32px rgba(0,0,0,0.5)"

  # Completion glow — violet wash, reserved for habit completion only
  glow-primary:
    boxShadow: "0 0 0 1px rgba(124,58,237,0.3), 0 0 24px rgba(124,58,237,0.15), 0 0 48px rgba(124,58,237,0.08)"

  # Member card hover — subtle lift signal
  card-hover:
    boxShadow: "0 0 0 1px rgba(124,58,237,0.25), 0 4px 12px rgba(0,0,0,0.3)"
    transform: "translateY(-1px)"
```

**Inner glow on cards.** Borrowed from Resend's atmospheric 6–9% opacity surface technique: every card has `inset 0 1px 0 rgba(255,255,255,0.04)` — a whisper of light at the top edge that makes the card feel like it's softly lit from above. Imperceptible alone, unmistakably polished in aggregate.

**Depth levels:**

| Level | Surface | Treatment |
|-------|---------|-----------|
| 0 — Canvas | `#0c0b0f` | Page background |
| 1 — Card | `#1a1820` + hairline | Standard cards, list rows |
| 2 — Elevated | `#211f28` | Drawers, bottom sheets |
| 3 — Modal | `#211f28` + stacked shadow | Modals, dialogs |
| 4 — Overlay | `#2a2733` | Tooltips, dropdowns |

---


## Motion Language

Spring physics. Not linear easing. Things in the real world have mass.

### Spring Constants

```ts
// Standard — most interactions
const spring = { type: "spring", damping: 28, stiffness: 380, mass: 0.8 }

// Soft — bottom sheets, large panels sliding in
const springSheet = { type: "spring", damping: 32, stiffness: 300, mass: 1.0 }

// Snappy — quick confirmations, badges, small chips
const springSnap = { type: "spring", damping: 22, stiffness: 500, mass: 0.6 }

// Bouncy — celebration moments, completion rings
const springBounce = { type: "spring", damping: 15, stiffness: 300, mass: 0.8 }
```

### Motion Roles

| Interaction | Animation | Duration |
|-------------|-----------|----------|
| Card entrance | `opacity: 0 → 1, y: 8 → 0` | spring, stagger 40ms |
| Button press | `scale: 1 → 0.97` | 80ms ease-out |
| Card press | `scale: 1 → 0.99` | 80ms ease-out |
| Bottom sheet open | slide up + spring | springSheet |
| Toast appear | slide from top + spring | springSnap |
| Completion ring fill | stroke-dashoffset 0→100% | 400ms easeInOut |
| Completion card flash | `backgroundColor` → primary-muted | 200ms |
| Reward unlock | scale 0.72 → 1 | springBounce |
| Tab switch | `x` slide + `opacity` | spring |
| Route change | `opacity: 0 → 1, y: 4 → 0` | 200ms easeOut |

### Motion Laws

1. **Spring or nothing.** If an animation exists, it uses spring physics. The only exception is `opacity` fades (these can use `ease-out` at 200ms).
2. **Completion animations are sacred.** The habit completion moment is the app's highest point. Give it the bouncy spring. Give it color. Give it a pause. It should feel like something.
3. **Everything else is functional.** Navigation, opens, closes — smooth but instant-feeling. 200ms max. The UI should never feel slow to respond.
4. **Respect `prefers-reduced-motion`.** All spring animations fall back to instant opacity fade at 150ms.

---


## Member DNA

**People are the product.** In Habitiq, flatmates are not just names in a list. Each member has a color identity that follows them everywhere.

### Member Color Assignment

```ts
const MEMBER_COLORS = [
  "#f59e0b",  // amber
  "#14b8a6",  // teal
  "#f43f5e",  // rose
  "#0ea5e9",  // sky
  "#8b5cf6",  // soft violet
  "#84cc16",  // lime
  "#ea580c",  // orange
  "#06b6d4",  // cyan
]

// Assign deterministically by member join order (index % 8)
// Once assigned, never changes — the color IS the member's identity
```

### Avatar Component

The avatar is the member's face in the app. Three states:

```
Photo avatar:     Circular photo, 40px (list) / 48px (card) / 32px (chip)
Initial avatar:   Circular, member-color background, white initial letter
                  Background at 25% opacity, letter at full member-color
Empty avatar:     Circular, hairline border, ink-mute background, person icon
```

### Presence Ring

When a member is the current assignee of a task, their avatar gains a `2px solid member-color` ring with `2px offset`. This is the "active" signal. No other element uses a ring — this visual device belongs entirely to assignment state.

### Member Chip

```
Height: 28px
Padding: 4px 10px
Border-radius: full (pill)
Background: member-color at 12% opacity
Border: 1px solid member-color at 30% opacity
Text: member-color at full opacity, label-sm
Avatar: 16px circle, member-color fill

Example: [  🟡 Rahul  ]  (amber chip for Rahul)
```

### Member Color in Financial Data

In the transaction list and balance cards, each member's amounts and name appear in their member color. Rahul's "₹2,400" expense is displayed in Rahul's amber. This makes it instantly readable: you don't read "paid by Rahul" — you *see* amber and know.

---


## Voice & Microcopy

Habitiq speaks like a thoughtful flatmate — direct, warm, never corporate.

### Tone of Voice

| Situation | Wrong | Right |
|-----------|-------|-------|
| Empty task list | "No tasks found." | "No tasks yet — add your first one and Habitiq will handle the rest." |
| Task completed | "Task completed." | "Done! Rahul is up next for Kitchen." |
| Overdue task | "Task overdue." | "Kitchen is 2 days overdue. Rahul, need to pick this up." |
| Settle confirmation | "Settlement recorded." | "Recorded. You're square with Priya on this one." |
| Error saving | "An error occurred." | "Something went wrong — your data is safe, try again." |
| OOS toggled | "Status updated." | "Got it — we'll skip your tasks until you're back." |

### Microcopy Patterns

```
Confirmation dialogs:   Action + consequence, then confirm. Never "Are you sure?"
                        "Leave the flat? Your tasks will be reassigned and you'll
                        lose access. This can't be undone." → [Leave flat] [Stay]

CTA labels:             Verb + object. Never just a noun.
                        "Add task" not "Task"
                        "Record payment" not "Pay"
                        "Skip this month" not "Skip"

Loading states:         Be specific.
                        "Saving your expense..." not "Loading..."
                        "Generating bills for June..." not "Please wait..."

Success states:         Acknowledge + next step.
                        "Bill marked paid. Bhanu has been notified." not "Success."
```

---


## Component System

### Buttons

```yaml
button-primary:
  # The main action. Violet + dark text = lit surface.
  # Source: Revolut (CTA confidence), Raycast (accent inversion)
  backgroundColor:    "#7c3aed"
  textColor:          "#0c0b0f"      # Dark text on violet — the lit-surface signal
  typography:         button-md
  rounded:            12px
  height:             52px           # Up from 48px — more presence
  padding:            "0 24px"
  pressScale:         0.97
  pressTransition:    "80ms ease-out"
  loadingState:       spinner + "disabled" + opacity 0.7
  successState:       checkmark + backgroundColor → positive

button-primary-hover:
  backgroundColor:    "#6d28d9"

button-secondary:
  # The secondary confirm. White-ish fill = trustworthy alternative.
  backgroundColor:    "#f4f3f8"
  textColor:          "#0c0b0f"
  typography:         button-md
  rounded:            12px
  height:             52px
  padding:            "0 24px"

button-ghost:
  # Tertiary. Hairline border, transparent fill.
  backgroundColor:    "transparent"
  textColor:          "#f4f3f8"
  border:             "1px solid #2a2635"
  typography:         button-md
  rounded:            12px
  height:             52px
  padding:            "0 24px"

button-ghost-active:
  backgroundColor:    "#7c3aed1a"
  border:             "1px solid #7c3aed"
  textColor:          "#a78bfa"

button-destructive:
  backgroundColor:    "#ef4444"
  textColor:          "#ffffff"
  typography:         button-md
  rounded:            12px
  height:             52px
  padding:            "0 24px"

button-small:
  # For compact contexts: chips, inline actions
  typography:         button-sm
  rounded:            8px
  height:             36px
  padding:            "0 14px"

button-icon:
  # Icon-only tappable action
  backgroundColor:    "#1a1820"
  border:             "1px solid #2a2635"
  rounded:            8px
  size:               40px
  iconColor:          "#a09db0"
  hoverIconColor:     "#f4f3f8"
```

**Button Law:** Primary button text is always `#0c0b0f` (near-black) on `#7c3aed` (violet). Never white text on violet for primary buttons. The contrast between deep warm black and bright violet is the "lit surface" — it signals energy and confidence. White text on violet is softer and secondary.

---

### Cards

Every card has a shared base. Specialisations layer on top.

```yaml
card-base:
  backgroundColor:    "#1a1820"
  border:             "1px solid #2a2635"
  innerGlow:          "inset 0 1px 0 rgba(255,255,255,0.04)"
  rounded:            12px
  padding:            20px
  pressScale:         0.99           # Cards compress less than buttons
  cursor:             "pointer"      # All tappable cards have cursor-pointer

card-habit:
  extends:            card-base
  # Contains: member chip (assignee) + task name + frequency badge
  #           + due date + completion ring
  leftAccent:         "4px" wide, member-color of assignee
  completedState:
    border:           "1px solid #7c3aed"
    background:       "#7c3aed1a"
    leftAccent:       "#7c3aed"
    innerGlow:        "inset 0 1px 0 rgba(124,58,237,0.12)"

  overdueState:
    border:           "1px solid #f59e0b60"
    leftAccent:       "#f59e0b"
    background:       "#f59e0b08"

  missedState:
    border:           "1px solid #ef444430"
    leftAccent:       "#ef4444"
    background:       "#ef444408"

card-expense:
  extends:            card-base
  # Category emoji icon left + description + payer · date + amount right
  # Amount uses member-color of payer
  categoryIconBackground: "member-color at 12% opacity, 8px radius"
  amountTypography:   numeric-sm

card-bill:
  extends:            card-base
  # Receipt aesthetic: top section (name + payer + amount), bottom section (status stamp)
  # Inspired by Stripe's invoice design
  topSection:
    padding:          "20px 20px 16px"
    background:       "#1a1820"
  divider:
    # Tear-line visual: alternating small triangles or dashed line
    style:            "2px dashed #2a2635"
    margin:           "0 16px"
  bottomSection:
    padding:          "12px 20px 16px"
    background:       "#211f28"
    rounded:          "0 0 12px 12px"
  statusStamp:
    # Circular stamp badge, not pill chip
    shape:            "circle, 52px"
    textTransform:    uppercase
    fontSize:         10px
    fontWeight:       700
    letterSpacing:    1px
    pending:          { border: "2px solid #f59e0b", color: "#f59e0b" }
    paid:             { border: "2px solid #22c55e", color: "#22c55e", background: "#22c55e18" }
    skipped:          { border: "2px solid #514e61", color: "#514e61" }

card-stat:
  extends:            card-base
  padding:            24px           # Spacious for stats
  valueTypography:    numeric-lg
  labelTypography:    label-sm
  labelColor:         "#a09db0"
  trendBadge:         { positive: "▲ green", negative: "▼ red" }

card-member:
  extends:            card-base
  # People card: avatar (48px) + name + role badge + reliability score
  # Inspired by Slack's member card, Clay's relationship card
  avatarSize:         48px
  avatarRing:         "2px solid member-color, 3px offset"
  roleBadge:          { admin: primary-muted bg, member: neutral }
  reliabilityScore:   { typography: numeric-md, color: member-color }

card-reward:
  # Scratch-card aesthetic — earned, precious, not decorative
  # Inspired by Starbucks loyalty card reveal
  background:         "linear-gradient(135deg, #1a1820 0%, #211f28 100%)"
  border:             "1px solid #eab30840"
  innerGlow:          "inset 0 1px 0 rgba(234,179,8,0.08)"
  rounded:            12px
  padding:            20px
  shimmer:            "sweeping highlight animation, 2s loop"
  revealedState:
    border:           "1px solid #eab308"
    background:       "linear-gradient(135deg, #1a1820 0%, #211f28 50%, #1a1514 100%)"

card-empty:
  extends:            card-base
  borderStyle:        "dashed"
  padding:            48px
  textAlign:          center
  textColor:          "#514e61"

card-balance:
  extends:            card-base
  # You owe / They owe: left-border accent signals direction
  # Inspired by Revolut's balance card
  youOwe:
    leftAccent:       "4px solid #f59e0b"
    background:       "#f59e0b08"
  theyOwe:
    leftAccent:       "4px solid #22c55e"
    background:       "#22c55e08"
```

---

### Inputs & Forms

```yaml
text-input:
  # Inspired by Apple's form precision: generous padding, inset shadow
  backgroundColor:    "#1a1820"
  textColor:          "#f4f3f8"
  placeholderColor:   "#514e61"
  border:             "1px solid #2a2635"
  innerShadow:        "inset 0 1px 3px rgba(0,0,0,0.3)"
  rounded:            8px
  padding:            "14px 16px"
  height:             52px
  typography:         body-md

  focusState:
    border:           "1px solid #7c3aed"
    shadow:           "0 0 0 3px rgba(124,58,237,0.15)"

  errorState:
    border:           "1px solid #ef4444"
    shadow:           "0 0 0 3px rgba(239,68,68,0.12)"

  successState:
    border:           "1px solid #22c55e"
    shadow:           "0 0 0 3px rgba(34,197,94,0.12)"

amount-input:
  # The most important input in the app. Hero-sized.
  # Inspired by Wise's full-width amount entry.
  backgroundColor:    "#1a1820"
  border:             "none"
  borderBottom:       "2px solid #2a2635"
  rounded:            "8px 8px 0 0"
  padding:            "20px 20px 16px"
  currencySymbol:     { fontSize: 32px, fontWeight: 700, color: "#514e61", marginRight: 4px }
  amount:             { typography: numeric-hero, color: "#f4f3f8" }
  focusBorderBottom:  "2px solid #7c3aed"

form-label:
  # Labels sit above inputs — not inside. Always visible.
  typography:         label-sm
  color:              "#a09db0"
  marginBottom:       6px

form-error:
  # Inline, below the input. Specific, never generic.
  typography:         caption
  color:              "#ef4444"
  marginTop:          4px
  icon:               "AlertCircle 12px"

form-help:
  typography:         caption
  color:              "#514e61"
  marginTop:          4px

form-section:
  # Groups of related inputs
  marginBottom:       24px
  borderBottom:       "1px solid #2a2635"
  paddingBottom:      24px
```

---

### Navigation

```yaml
tab-bar:
  # Mobile bottom navigation — 5 slots + FAB
  backgroundColor:    "#0c0b0f"
  borderTop:          "1px solid #2a2635"
  height:             84px           # Includes safe-area padding
  padding:            "8px 0 20px"   # Bottom: safe area
  activeColor:        "#7c3aed"
  inactiveColor:      "#514e61"
  badgeColor:         "#7c3aed"
  badgeTextColor:     "#f4f3f8"
  badgeSize:          18px

fab:
  # Radial quick-add FAB
  size:               56px
  backgroundColor:    "#7c3aed"
  iconColor:          "#0c0b0f"
  shadow:             "0 4px 16px rgba(124,58,237,0.4), 0 0 0 1px rgba(124,58,237,0.3)"
  pressScale:         0.94           # FAB compresses more — feels physical

sidebar:
  # Desktop left sidebar
  width:              256px
  backgroundColor:    "#0c0b0f"
  borderRight:        "1px solid #2a2635"
  padding:            "24px 12px"
  navItem:
    padding:          "10px 14px"
    rounded:          8px
    typography:       body-md
    inactiveColor:    "#a09db0"
    activeBackground: "#7c3aed18"
    activeBorder:     "left: 3px solid #7c3aed"
    activeColor:      "#f4f3f8"

page-header:
  # Sticky top header
  backgroundColor:    "#0c0b0f"
  borderBottom:       "1px solid #2a2635"
  height:             56px
  padding:            "0 20px"
  titleTypography:    display-xs
  backButton:         { size: 40px, color: "#a09db0" }
```

---

### Progress & Status

```yaml
progress-bar:
  height:             4px
  trackColor:         "#2a2635"
  fillColor:          "#7c3aed"
  rounded:            full
  animated:           true           # Fill animates in on mount

progress-bar-positive:  fillColor: "#22c55e"
progress-bar-warning:   fillColor: "#f59e0b"
progress-bar-negative:  fillColor: "#ef4444"

habit-completion-ring:
  # Circular progress around the habit/task icon
  size:               48px
  strokeWidth:        3px
  trackColor:         "#2a2635"
  fillColor:          "#7c3aed"
  completedFill:      "#22c55e"
  animationDuration:  400ms

badge-streak:
  backgroundColor:    "#f97316"
  textColor:          "#0c0b0f"
  typography:         label-sm
  rounded:            full
  padding:            "3px 8px"
  icon:               "🔥" # exception: flame emoji is semantic, not decorative

badge-status:
  rounded:            full
  padding:            "3px 10px"
  typography:         label-sm
  pending:   { bg: "#f59e0b18", color: "#f59e0b", border: "1px solid #f59e0b30" }
  complete:  { bg: "#22c55e18", color: "#22c55e", border: "1px solid #22c55e30" }
  overdue:   { bg: "#ef444418", color: "#ef4444", border: "1px solid #ef444430" }
  skipped:   { bg: "#2a2635",   color: "#514e61", border: "1px solid #514e61" }

badge-role:
  admin:     { bg: "#7c3aed18", color: "#a78bfa", border: "1px solid #7c3aed30" }
  member:    { bg: "#2a2635",   color: "#514e61", border: "none" }
```

---

### Sheets, Modals & Overlays

```yaml
bottom-sheet:
  backgroundColor:    "#211f28"
  rounded:            "24px 24px 0 0"
  padding:            "20px 20px 0"
  handleBar:          { width: 36px, height: 4px, color: "#2a2635", rounded: full, margin: "0 auto 16px" }
  elevation:          sheet
  backdropColor:      "rgba(0,0,0,0.75)"
  backdropBlur:       "blur(4px)"
  animation:          springSheet

modal:
  backgroundColor:    "#211f28"
  border:             "1px solid #2a2635"
  rounded:            16px
  padding:            24px
  maxWidth:           480px
  elevation:          modal
  backdropColor:      "rgba(0,0,0,0.75)"
  backdropBlur:       "blur(4px)"
  animation:          spring

toast:
  # Source: Linear's toast pattern — left-accent, no heavy fill
  backgroundColor:    "#211f28"
  border:             "1px solid #2a2635"
  rounded:            8px
  padding:            "12px 16px"
  elevation:          modal
  maxWidth:           360px
  autoDismiss:        3000ms
  animation:          springSnap
  success:  { leftBorder: "3px solid #22c55e" }
  error:    { leftBorder: "3px solid #ef4444" }
  warning:  { leftBorder: "3px solid #f59e0b" }
  info:     { leftBorder: "3px solid #7c3aed" }
```

---

### Specialty Components

```yaml
rotation-orbit:
  # Rotation queue as a horizontal arc of member avatars
  # Inspired by Mastercard's orbital connection metaphor —
  # members are not a list, they're in orbit around the task.
  layout:             "horizontal scroll, connected by thin dashed arc"
  avatarSize:         36px
  spacing:            -8px           # Overlapping avatars (stack)
  connectorLine:      "1px dashed #2a2635"
  current:
    ring:             "2px solid member-color"
    label:            "NOW chip — primary-muted bg, violet text"
    scale:            1.1
  oos:
    opacity:          0.4
    crossIcon:        true
  position-chip:
    size:             18px
    typography:       "10px/600/uppercase"
    color:            ink-mute

hero-stat:
  # Full-width key metric — total spend, streak count, flat reliability
  padding:            "48px 20px"
  valueTypography:    numeric-hero   # 64px/800 — this number deserves presence
  valueColor:         ink
  labelTypography:    label-md
  labelColor:         ink-soft
  accentLine:         "2px solid primary, 24px wide, centered below label"

expense-summary-card:
  # Dark summary showing monthly spend + split visualization
  # Inspired by Revolut's financial card layout
  backgroundColor:    "#1a1820"
  rounded:            12px
  padding:            20px
  heroAmount:
    typography:       numeric-xl
    color:            ink
  progressBar:
    height:           8px
    rounded:          full
    bills-color:      "#f59e0b"     # Amber = bills
    splits-color:     "#7c3aed"    # Violet = daily splits

collection-strip:
  # Compact member collection status — who has paid the collector
  # Shown on bill instances for collector/admin
  avatar:             32px
  status:             { paid: "✓ green", pending: "· orange" }
  layout:             "horizontal, pill group"

nps-banner:
  backgroundColor:    "#1a1820"
  border:             "1px solid #7c3aed30"
  rounded:            12px
  padding:            20px
  scoreButtons:
    size:             40px
    rounded:          8px
    unselected:       { bg: "#2a2635", color: ink-soft }
    selected:         { bg: primary-muted, border: "1px solid primary", color: primary-soft }

onboarding-card:
  # Mobile onboarding hero card — full-width, photo strip at top
  imageStrip:
    height:           200px
    objectFit:        cover
    borderRadius:     "12px 12px 0 0"
  content:
    padding:          24px
    headlineTypography: display-md
    bodyTypography:   body-lg
  checkBenefit:
    iconColor:        positive
    typography:       body-md
```

---


## Celebration System

These are the highest-value moments in the app. They must feel earned.

### Task Completion (3-Stage)

```
Stage 1 — Ring fills (0–400ms):
  SVG stroke-dashoffset animates from 0% to 100%
  Fill color: violet (#7c3aed)
  Easing: easeInOut

Stage 2 — Card flash (400–600ms):
  Card border → violet
  Card background → #7c3aed1a (10% violet wash)
  Inner glow → "inset 0 1px 0 rgba(124,58,237,0.2)"
  Left accent → solid violet
  springBounce

Stage 3 — Settle (600ms+):
  Card transitions to positive state:
  Border → #22c55e
  Background → #22c55e08
  Left accent → #22c55e
  Completion ring → solid green checkmark
  Toast: "Done! [Next member] is up next for [Task]."
```

### Streak Milestone

```
Triggered at: 7, 14, 30, 60, 100 day streaks.

Full-width banner slides down from top:
  Background: linear-gradient(90deg, #f97316 0%, #eab308 100%)
  Text color: #0c0b0f (dark — lit surface)
  Icon: 🔥 large (48px)
  Message: "7-day streak! [Task] done every single day."
  Duration: 4 seconds
  Dismiss: tap or auto
```

### Reward Unlock (Scratch-card reveal)

```
Modal enters with springBounce:
  scale 0.72 → 1.0
  opacity 0 → 1

Center content:
  Brand logo (48px)
  Particle confetti (12 particles, springBounce stagger)
  "🎁 You've earned a reward from [Brand]!"
  Coupon code (mono-lg, partially obscured)
  CTA: "View Reward" → Profile/Wallet
  Auto-dismiss: 8s
```

### Bill Settlement

```
When a balance settles to zero:
  Balance card animates out (scale 0.95, opacity 0)
  Brief: "You're square with [Name]! ✓"
  Positive toast slides in
```

---


## Layout System

### Spacing Grid

```
Base unit: 4px

Mobile horizontal padding:  20px (all screens)
Mobile section gap:         32px (major) / 24px (related)
Mobile card gap:            12px in lists, 16px in grids
Card interior:              20px standard, 24px spacious
```

### Touch Targets

```
Minimum tappable: 44 × 44px (WCAG AA)
Primary buttons:  52px height
List rows:        56px minimum
Tab bar items:    44px minimum touch zone
Icon buttons:     40px (visual) / 44px (touch)
```

### Container

```
Mobile (< 768px):   Single column, max-width 100%, 20px side padding
Tablet (768–1023px): 2-up card grid, max-width 720px centered
Desktop (≥ 1024px): Sidebar nav (256px) + content area, max-width 1200px
```

### Page Anatomy

```
Mobile:
  ┌─────────────────────────┐
  │  56px sticky header     │
  ├─────────────────────────┤
  │                         │
  │  Page content           │
  │  (scrollable)           │
  │                         │
  ├─────────────────────────┤
  │  84px bottom tab bar    │
  └─────────────────────────┘

Desktop:
  ┌──────────┬──────────────┐
  │          │  56px header  │
  │  256px   ├──────────────┤
  │ sidebar  │              │
  │          │  Content     │
  │          │              │
  └──────────┴──────────────┘
```

---


## Pattern Atlas — 71 Brand Synthesis

What we learned. What we applied. What we left behind.

### Applied Patterns

| # | Pattern | Brand Source(s) | Habitiq Application |
|---|---------|-----------------|---------------------|
| 1 | Accent scarcity | Airbnb, Linear | One violet per fold. Never two. |
| 2 | Lit-surface CTA inversion | Raycast, Revolut, Linear | Violet fill + dark (#0c0b0f) text on primary button |
| 3 | tnum tabular figures | Stripe | All amounts, counts, percentages — always |
| 4 | Hero numerics with confidence | Wise (900w), Revolut (80px) | `numeric-hero` at 64px/800 for main financial display |
| 5 | Scale-transform press | Apple, Starbucks | scale(0.97) buttons, scale(0.99) cards |
| 6 | Spring physics | Framer | Every animation uses spring constants, not linear |
| 7 | Inner glow on cards | Resend (6-9% surface glow) | inset 0 1px 0 rgba(255,255,255,0.04) — top-edge warmth |
| 8 | Dark-text on accent fill | Supabase, Voltagent | on-primary (#0c0b0f) on violet — never white |
| 9 | ss03 Inter feature flag | Raycast, Shopify | font-feature-settings globally on body |
| 10 | Stacked micro-shadow | Vercel | 4-layer shadow on modals only |
| 11 | Dark-canvas developer positioning | Linear, xAI, Voltagent | Dark-first architecture, no light mode |
| 12 | Warm color = directional signal | Nintendo (via Starbucks/Nike) | Orange = streak momentum, Gold = achievement |
| 13 | Surface contrast elevation | Notion, Linear | Cards at #1a1820 vs canvas #0c0b0f — no shadows on cards |
| 14 | Member color identity | Slack, Intercom, Clay | Each member gets a named color, used everywhere |
| 15 | People-first information architecture | Intercom, Slack | Member avatar is primary, name is secondary |
| 16 | Orbital visual metaphor | Mastercard | Rotation queue as orbiting member avatars |
| 17 | Signature category tints | Airtable | Expense categories get warm/cool surface tints |
| 18 | Receipt aesthetic for bills | Stripe invoices | Bill cards have tear-line divider + stamp-circle status |
| 19 | Completion ceremony | Starbucks (reward reveal), Nike (achievement) | 3-stage animation: ring → flash → settle |
| 20 | Conversational microcopy | Intercom | Every toast, empty state, CTA sounds like a person |
| 21 | Milestone banners | Starbucks (loyalty), Nike (achievement) | Gradient banner for streak milestones |
| 22 | Reward scratch-card reveal | Starbucks loyalty | SpringBounce modal with confetti |
| 23 | Generous breathing room | Apple, Spotify | Card padding 20px (not 16px) |
| 24 | Floating-label form design | Apple form refinement | Labels above inputs, always visible |
| 25 | Inset shadow on inputs | Apple material feel | inset 0 1px 3px rgba(0,0,0,0.3) |
| 26 | Left-border semantic cards | Linear, Notion | Status shown via left border, not background fill |
| 27 | Status stamps vs pill badges | Stripe invoice status | Bill instances use circular stamp, not pill chip |
| 28 | Backdropblur on overlays | Apple, Framer | blur(4px) on sheet/modal backdrop |
| 29 | Positive/negative line items | Wise, Revolut | +₹ in green, -₹ in orange, payer in member-color |
| 30 | Empty states with invitation | Airbnb, Intercom | "No tasks yet — add your first one, it's quick!" |

### Deliberately Not Applied

| Pattern | Source | Reason |
|---------|--------|--------|
| Photography-led heroes | Tesla, SpaceX, Ferrari | Habitiq has no photography surface — data is the content |
| Gradient decoration | Resend, Cursor, Together.ai | Reads as "AI demo," Habitiq needs utility trust |
| Multi-accent decorative palette | Webflow (5 accents) | Semantic colors only — violet + green/amber/red. Never decorative |
| Ultra-heavy display (weight 900 on headlines) | Wise headers, Vodafone | 700-800 max for display — Habitiq is a daily tool, not a billboard |
| Light mode | Shopify (dual-track) | Dark canvas IS the Habitiq identity. No light mode in v1. |
| Pill-radius buttons (100px) | Meta | 12px is the brand shape — pill reads too casual for a finance tool |
| Sticky-note / illustration decorations | Notion | Too playful for a trust-based shared-living tool |
| Animated background blobs | Together.ai, Framer | Adds visual noise, conflicts with calm utility feel |
| Masonry photo grids | Pinterest | No photography surface in current product |
| Five-stop category chrome | Webflow | Category tints are subtle 8% — never loud chrome |

---


## Do's & Don'ts

### Do
- **One violet per fold.** If the active tab is violet, the ring isn't. If the primary button is violet, the progress bar isn't. Scarcity is the law.
- **tnum everywhere numeric.** Every number that can change uses `font-variant-numeric: tabular-nums`. Amounts do not shift layout.
- **ss03 on body.** `font-feature-settings: "ss03" 1` globally. Warm Inter at zero cost.
- **Scale on press.** scale(0.97) buttons, scale(0.99) cards. Physical feedback on touch.
- **Member color on member data.** Rahul's expense is amber. Priya's balance is teal. People have color; infrastructure does not.
- **Conversational copy everywhere.** "Done! Priya is next for Kitchen." not "Task completed."
- **Spring physics for animations.** Damping 28, stiffness 380. The real world has mass.
- **Left-border for status.** Color the left edge of a card, not the fill. Status is a signal, not a background.
- **Inner glow on cards.** `inset 0 1px 0 rgba(255,255,255,0.04)` on every card. Makes the surface feel lit, not flat.
- **Bill cards look like receipts.** Dashed tear-line, circular stamp status. The form should match the function.
- **Celebrate completion.** 3-stage ring → flash → settle. This is the app's highest point.

### Don't
- **Don't introduce light mode.** Dark canvas is the Habitiq identity. One consistent world.
- **Don't shadow standard cards.** Surface contrast carries elevation. Shadows make dark UI feel heavy.
- **Don't fill cards with semantic color.** Green/red/amber are ink colors, not fill colors. A red card background screams; a red left border informs.
- **Don't mix tnum and proportional in the same column.** Pick one system per column — always tnum for amounts.
- **Don't uppercase display or body copy.** Only label-md and label-sm tokens produce uppercase.
- **Don't use streak-fire orange decoratively.** Orange = streak momentum. Gold = achievement. Used elsewhere, they lose meaning.
- **Don't pill-radius primary buttons.** 12px canonical radius. Pill reads too casual.
- **Don't animate routine interactions.** Animation is earned by reward moments. Navigation should feel instant.
- **Don't use the violet muted fill decoratively.** #7c3aed1a is reserved for completed habit cards and active chips.
- **Don't write generic microcopy.** "Error saving" tells the user nothing. "Something went wrong — your data is safe, try again" tells them everything.
- **Don't skip the completion ceremony.** The completion ring animation is not optional. It's the primary reward loop.

---

*"The best design doesn't look designed. It looks cared for."*

**System version:** 2.0 | **Source:** 71-brand synthesis | **Built for:** Habitiq v0.5.0+
**Maintained by:** Venkata Sai Jaswanth E
