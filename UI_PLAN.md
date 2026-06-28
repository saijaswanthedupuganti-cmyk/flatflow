# Habitiq — UI Upgrade Plan
**Goal:** Transform Habitiq into a fully professional, human-touch app.
**Method:** One session per day. Each day has a clear scope, clear output.
**Source of truth:** DESIGN.md (v2.0) — the 71-brand synthesis.

---

## Status Legend
- `[ ]` Not started
- `[→]` In progress
- `[✓]` Done

---

## Day 1 — Color Foundation & Member DNA ✓ COMPLETE
*The canvas, the warmth, the people system. Every other day builds on this.*

- [✓] Apply new canvas colors (`#0c0b0f`, `#1a1820`, `#211f28`) via Tailwind CSS v4 variables
- [✓] Apply new hairline color (`#2a2635`) globally
- [✓] Apply ink colors (`#f4f3f8`, `#a09db0`, `#514e61`)
- [✓] Add `inner-glow` card utility — `inset 0 1px 0 rgba(255,255,255,0.04)`
- [✓] Implement `MEMBER_COLORS` constant (`amber / teal / rose / sky / soft-violet / lime / orange / cyan`)
- [✓] Build `MemberAvatar` component — photo / initial / empty states, color ring on assignment
- [✓] Build `MemberChip` component — pill with member color bg + initial + name
- [✓] Apply `MemberAvatar` to: task cards, expense rows, balance cards, member list, rotation queue
- [✓] Apply `MemberChip` to: task assignment display, swap requests

**Output:** App feels warmer, darker, and every flatmate has a color identity.

---

## Day 2 — Dashboard Redesign ✓ COMPLETE
*The first screen users see. Must communicate everything at a glance.*

- [✓] Dark surface task cards with semantic left-border (overdue=red, warning=amber, ok=violet)
- [✓] MemberAvatar top-right corner of each task card
- [✓] Stats row rebuilt (Health, Tasks, Swaps) — dark cards with icon badges, `numeric-lg`
- [✓] Rotation order redesigned — MemberAvatar per member, violet NOW badge, dark surface rows
- [✓] Completion + swap panels use dark surface styling

**Output:** Dashboard feels like a command center. The numbers matter. The people are visible.

---

## Day 3 — Expenses Page Redesign ✓ COMPLETE
*Where trust is built or broken. Needs to feel like a premium finance app.*

- [✓] Violet primary throughout (replaced all #3525cd with #7c3aed)
- [✓] MemberAvatar in balance rows (Getting back / You'll pay sections)
- [✓] MemberAvatar in expense split breakdown (expanded row)
- [✓] MemberAvatar in Treasurer panel member list
- [✓] MemberAvatar in Resident Breakdown modal
- [✓] MemberAvatar in form member pickers (split-among, who-paid, who-splits)
- [✓] CATEGORY_CONFIG upgraded — each category has `hex` color + `bg-{color}-500/15` class + inner glow
- [✓] Category icon in transaction row uses category color with inner border glow
- [✓] Bill card category icon uses category color
- [✓] Treasurer stat pills — violet/neutral/amber 3-column grid
- [ ] Settle modal — better amount input (`amount-input` style, large numeric display)
- [ ] Empty states — conversational copy, icon, CTA button

**Output:** Expenses page feels like Revolut/Wise. Financial trust through visual precision.

---

## Day 4 — Tasks Page & Completion System ✓ COMPLETE
*Tasks are the core loop. Completion must feel rewarding.*

- [✓] Task card redesign — member color left-border (4px via getMemberColor().hex), freq/priority badges updated to /15 opacity (dark-mode safe)
- [✓] Overdue card state — subtle amber background tint (linear-gradient rgba(245,158,11,0.06) over --card)
- [✓] One-time task assignee display — replaced plain initials div with MemberAvatar + member-colored pill badges (PENDING/DONE)
- [✓] Rotation orbit already complete (MemberAvatar in queue, NOW badge) — from Day 2 session
- [✓] Admin compact cards already use MemberAvatar headers — from Day 2 session
- [✓] Empty state — improved: icon in violet bubble, conversational copy, no card wrapper
- [ ] Completion ring animation — deferred (Framer Motion circular progress, complex)
- [ ] Mark-done 3-stage animation — deferred (ring fill → card flash → settle)
- [ ] Swap button visual redesign — deferred
- [ ] Group task sub-task chips — deferred
- [ ] Temp task dashed border — deferred

**Output:** Task cards identify their assigned member by color at a glance. One-time tasks clearly show who's responsible with their DNA color ring.

---

## Day 5 — Navigation & Global Chrome ✓ PARTIAL
*The frame around everything. Must feel native and polished.*

- [✓] Bottom tab bar — violet pip indicator (3px × 24px rounded bar at -top-2) above active icon + bg-primary/10 bubble on active icon
- [✓] FAB button — already violet (#7c3aed gradient) with rgba(124,58,237,0.55) glow + pulse ring (layout.tsx)
- [✓] Sidebar (desktop) — already uses bg-primary CSS var (violet) for active; icon gets bg-white/20 ring
- [✓] Notification badges — already violet-500 in both NavLink and MobileNavLink
- [ ] Page headers — consistent 56px sticky header, display-xs title, hairline bottom border (deferred)
- [ ] Back button — 40px touch target, ink-soft color (deferred)
- [ ] Flat switcher — cleaner dropdown with member count, active flat check (deferred)

**Output:** Mobile bottom nav has a clear violet indicator for the active tab. Nav feels polished and intentional.

---

## Day 6 — Forms, Modals & Sheets ✓ COMPLETE (June 29)
*Where users take action. Must feel effortless.*

- [✓] All text inputs — `.hiq-input`: `#1a1820` bg, inset shadow, 52px height, 8px radius (globals.css)
- [✓] Input focus state — violet border + `0 0 0 3px rgba(124,58,237,0.15)` glow (globals.css)
- [✓] Amount input — `.hiq-amount-input`: large numeric, border-bottom only, 64px height (globals.css)
- [✓] Form labels — `.hiq-label`: 11px/600/uppercase/letter-spaced, always above input (globals.css)
- [✓] Inline error messages — `.hiq-field-error`: icon + specific text, below input (globals.css)
- [✓] Bottom sheets — `.hiq-sheet`: `#211f28` bg, 24px top radius, `.hiq-sheet-handle` bar (globals.css)
- [✓] Modals — `.hiq-modal`: `#211f28` bg, stacked micro-shadow + inner glow (globals.css)
- [✓] Backdrop — `.hiq-backdrop`: rgba 65% + backdrop-filter blur(6px) (globals.css)
- [✓] Primary buttons — `.hiq-btn .hiq-btn-primary`: 52px height, violet, shadow glow (globals.css)
- [✓] Ghost buttons — `.hiq-btn .hiq-btn-ghost`: hairline border, transparent (globals.css)
- [✓] Destructive buttons — `.hiq-btn .hiq-btn-destructive`: red fill, white text (globals.css)
- [✓] Form section divider — `.hiq-form-section`: border-top + spacing (globals.css)

**Output:** CSS utility classes ready — apply `.hiq-input`, `.hiq-btn-primary`, `.hiq-sheet` etc. to any form component for instant design-system compliance.

---

## Day 7 — Celebration, Toasts & Empty States ✓ COMPLETE (June 29)
*The emotional layer. What makes the app feel alive.*

- [✓] Toast system redesign — `.hiq-toast` with left-border accent, dark bg, icon bubble (globals.css)
- [✓] Toast variants — `.hiq-toast-success/warning/error/info` with semantic border + icon colors (globals.css)
- [✓] Streak milestone banner — `.hiq-streak-banner`: orange-to-amber gradient, dark text (globals.css)
- [✓] Empty state system — `.hiq-empty` + `.hiq-empty-icon` (violet bubble) + title + body (globals.css)
- [✓] Skeleton loaders — `.hiq-skeleton`: sweep animation, 200% bg (globals.css)
- [ ] NotificationToast.tsx — apply `.hiq-toast` classes to live component (next step)
- [ ] Reward unlock modal — springBounce already built, verify confetti + scratch aesthetic
- [ ] Task completion toast — "Done! [Name] is up next" with member color (wire to hiq-toast)
- [ ] Settlement toast — "You're square with [Name]. ✓" (wire to hiq-toast)

**Output:** All toast/empty/skeleton/streak CSS ready. Wire to NotificationToast.tsx next session.

---

## Day 8 — Analytics & Calendar
*Data visualization. Makes the app feel smart.*

- [ ] Completion grid — redesigned with member color fill per cell (not just green/gray)
- [ ] Reliability score — per-member ring chart (Recharts), member color fill
- [ ] Per-task completion bar chart — horizontal bars, violet fill, category chip
- [ ] Calendar view — month grid with member color dots on task days, compact legend
- [ ] Analytics header stat — flat-level completion % at `numeric-hero` size

**Output:** Analytics feels like insights, not just data.

---

## Day 9 — Profile, Settings & Onboarding
*First and last impressions.*

- [ ] Profile page — member avatar (large, 80px), name display-sm, flat name, role badge
- [ ] Rewards wallet — scratch-card grid, shimmer animation, indigo/purple gradient cards
- [ ] Reward card reveal — bottom sheet with full code + copy button
- [ ] Settings page — cleaner section grouping, better Danger Zone styling
- [ ] Onboarding — hero image strip, display-md headline, body-lg copy, check benefits
- [ ] Login page — centered card, Google button with icon, Inter body

**Output:** Profile feels personal. Onboarding feels inviting.

---

## Day 10 — Final Polish & Type Audit
*The last pass. The difference between good and great.*

- [ ] Typography audit — every page uses correct tokens (display-xs for section titles, body-md for descriptions, label-sm for eyebrows)
- [ ] Spacing audit — all cards at 20px padding, sections at 32px gaps
- [ ] Border radius audit — all cards 12px, inputs 8px, pills where appropriate
- [ ] Cursor audit — `cursor-pointer` on every tappable element
- [ ] Touch target audit — 44×44px minimum on all interactive elements
- [ ] Reduced-motion — all animations have `prefers-reduced-motion` fallback
- [ ] WCAG contrast check — all text passes 4.5:1
- [ ] Dark mode consistency — no light surfaces leaking through

**Output:** The app is finished. Every pixel is intentional.

---

## Technology Notes

**Charts:** `recharts` (already likely in project or easy to add — `npm install recharts`)
**Animation:** `framer-motion` (already in project)
**Icons:** `lucide-react` (already in project)
**Fonts:** Inter (already loaded) — add `font-feature-settings: "ss03" 1` to `@layer base` in globals.css
**Member colors:** Define as a constant in `lib/memberColors.ts`, import everywhere

## Quick Reference — Key Design Tokens

```
Canvas:         #0c0b0f (page) / #1a1820 (card) / #211f28 (modal)
Hairline:       #2a2635
Ink:            #f4f3f8 (primary) / #a09db0 (soft) / #514e61 (mute)
Primary:        #7c3aed (violet) / #a78bfa (soft) / #7c3aed1a (muted 10%)
Positive:       #22c55e / #22c55e18
Warning:        #f59e0b / #f59e0b18
Negative:       #ef4444 / #ef444418
Streak:         #f97316
Reward:         #eab308
Card padding:   20px (standard) / 24px (spacious)
Button height:  52px
Input height:   52px
Border radius:  12px (card/button) / 8px (input/chip) / 9999px (pill)
```
