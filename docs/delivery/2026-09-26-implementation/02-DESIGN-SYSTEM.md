# One Habitiq design system

Binding color input: user's 26 September palette. Geometry/motion values below are implementation specifications derived from existing tokens where available; they are not measured pixels from an approved render. Reuse owners, never create per-screen palettes.

## Exact color mapping

| User role | Hex | Android semantic destination | Web semantic role |
|---|---|---|---|
| Primary brand | #0F766E | brandPrimary | --primary |
| Primary hover | #0D6B64 | brandPrimaryHover | --primary-hover |
| Primary pressed | #115E59 | brandPrimaryPressed | --primary-pressed |
| Primary dark | #134E4A | brandPrimaryDark (add semantic field) | --primary-dark |
| Primary light | #CCFBF1 | brandPrimaryContainer | --primary-light |
| Primary soft | #F0FDFA | brandPrimarySubtle (add) | --primary-soft |
| Text | #111C1B | textPrimary | --foreground |
| Text secondary | #52615E | textSecondary | --text-secondary |
| Text muted | #6B7A77 | textTertiary | --text-muted |
| Text disabled | #AEB9B6 | textDisabled | --text-disabled |
| Background | #F7FAF9 | background | --background |
| Surface | #FFFFFF | surface | --card / --surface |
| Surface muted | #F3F7F6 | surfaceSubtle | --surface-muted |
| Surface brand | #F0FDFA | surfaceBrand (add) | --surface-brand |
| Border | #E7EFED | borderDefault | --border |
| Border strong | #D8E3E0 | borderStrong | --border-strong |
| Success | #15803D | success | --success |
| Warning | #B45309 | warning | --warning |
| Error | #B91C1C | error | --destructive |
| Info | #0369A1 | info | --info |

Derived role decisions, not additional brand colors: onBrandPrimary/textOnBrand/onError = white; surfaceElevated = white with at most HqElevation.low; surfaceDisabled = surfaceSubtle; borderDisabled = borderDefault; borderFocus = brandPrimary. Status backgrounds = 8% semantic hue composited on white, with semantic hue icon and textPrimary body. Do not use translucent overlays without checking the composed color. Add named container values once to the owner. White splash is fixed even when OS is dark. No automatic alternate theme is approved; maintain a legible unified light experience in both OS appearance settings, and do not claim native dark-mode support.

`HqColorTokens.kt` should become the consuming semantic API. `FigmaColors.kt` and `BrandColors.kt` may temporarily alias the new values to avoid breaking consumers, but must not remain independent palettes or circular dependencies. Replace their outdated coral/violet comments. Trace every import and nested HabitiqTheme call. Audit all Material3 roles including onPrimaryContainer, secondary/tertiary and their on/container roles, onError/onErrorContainer, surface tint, inverse roles and surface container roles available in the installed version. Choose neutral or approved semantic aliases, not untouched purple defaults.

## Contrast constraints

Computed using sRGB relative luminance on opaque colors:

| Pair | Ratio | Use |
|---|---|---|
| White on primary | 5.47:1 | Normal button text passes 4.5:1 |
| Secondary on white | 6.50:1 | Supporting text, placeholders and metadata |
| Secondary on background | 6.19:1 | Supporting text |
| Muted on white | 4.49:1 | Fails normal-text target; do not round up |
| Muted on background | 4.28:1 | Fails normal-text target |
| Disabled on white | 2.02:1 | Disabled-only meaning, never active instructions |
| Border / strong border on white | 1.17 / 1.31:1 | Decorative separators only, not sole control boundary |
| Success / warning on white | about 5.02:1 each | Semantic text |
| Error / info on white | 6.47 / 5.93:1 | Semantic text |

Keep exact requested values. Use secondary instead of muted for normal small readable text. Muted may be used for eligible large text/decorative icons after checking context. A field's pale border cannot be its only visible identifying cue; use label plus an accessible control outline (secondary when needed), brand focus stroke and error text. No color-only selection: selected chip has checkmark/state semantics; overdue has a written label; active tab has filled indicator and weight.

## Geometry and typography

Retain `HqSpacing`: 4, 8, 12, 16, 20, 24, 32, 40, 48, 64dp. Mobile page gutter is existing `screenHorizontal = 16dp`, not whichever margin a screenshot seems to imply. Internal card padding = 16; related items gap = 8; label-to-field = 8; field groups = 16; sections = 24; header-to-body = 16. Use 24dp tablet gutter. Center narrow forms at max 560dp; tablet root content max 960dp as a proposed responsive layout constraint. Do not impose max width on the system/nav background itself.

Radius: inputs/buttons 12dp (`md`); content cards 16dp (`lg`); sheets 24dp top corners (`xl`); metadata pills only use `full`. Navigation/settings/activity rows normally use no outer card, with dividers and padding. Elevation: base 0, minor overlay 2dp, modal 8dp. Shadows are not used to substitute for clear hierarchy.

Use `HqFontFamily` platform default until a licensed Inter font asset is actually bundled. Do not claim Inter is already active. Do not add network font loading for launch. Keep existing type scale for main roles: display 32/40 bold, root title 28/34 semibold, secondary title 18/24 semibold, page subheading 20/26, section 16/22 medium, body-large 16/24, body 14/20, supporting 13/18. Raise essential caption/label-small text from 11sp to 12/16 centrally after regression review. Captions are not used for form instructions. Button label 14/20 medium; minimum 48dp height with flexible height at 200% font scale. No fixed text-containing height that clips scaling. Sentence case section names.

## Screen/inset ownership

Extend the existing HqAppBar family and introduce one shared `HqScreenScaffold` in `U/components` only because current Column/AppShell and isolated TopAppBars do not share ownership. Contract:

1. MainActivity opts into supported edge-to-edge behavior; status/nav icon appearance comes from the light surface. Do not hardcode status bar height.
2. HqScreenScaffold is the one inset owner for each full-screen destination. Apply safe-drawing/cutout padding, consume applied insets once, pass inner padding to scroll content. Explicitly configure nested app bars' insets so the status inset is neither omitted nor doubled.
3. Root tabs use root header and one bottom bar. Secondary details use standard back header; focused create/edit/upload/chat screens hide root navigation, keep their owning root selection for return, and reserve a keyboard-aware action region.
4. Bottom bar proposed content height = 72dp at default font; allow growth for accessibility. OS inset is additional. Four equal items, >=48dp touch regions, 24dp icons and >=12sp labels. Selected item uses primary-soft indicator plus primary icon/text and weight, not underline alone.
5. Header content min-height = 64dp, back target 48dp, back icon 24dp auto-mirrored, title 18/24. Long titles wrap where feasible; avoid forcing overflowing action text into a narrow row. Overflow menu owns secondary actions.
6. IME: focused field scrolls into view, bottom CTA sits above keyboard when feasible; remaining form can scroll on short landscape screens. Back first dismisses keyboard, then follows navigation/discard rules. Last list row never falls behind CTA or navigation.

Use API documentation for exact installed signatures. [Compose inset ownership](https://developer.android.com/develop/ui/compose/system/insets-ui) and [Material inset handling](https://developer.android.com/develop/ui/compose/system/material-insets) explain consumption; do not paste unrelated versions' code blindly.

## Reusable component contract

Reuse HqButton, HqTextButton, HqTextField, HqCard, HqChip, HqAvatar, HqAppBar, HqFeedback, HqOverlays and HqSnackbar. Add a primitive only for a missing semantic function; keep feature content in feature components.

| Component | Required states and interaction | Visual rule |
|---|---|---|
| Button | enabled/pressed/focus/disabled/loading; semantic button role; duplicate taps ignored | Primary teal; secondary neutral surface/outline; tertiary text; destructive error. Stable width when spinner replaces icon/label |
| Text/Icon button | label, focus, disabled; 48dp hit region, including narrow back/close glyphs | One icon family: existing Material icons; no arrow text glyph variants |
| TextField/SearchField | persistent label, keyboard/autofill, helper/error, focus, disabled, IME action, clear search | One outlined field system. Single-line min 56dp; multiline grows. Error is text + border/icon |
| Select/radio/checkbox/switch | native semantics, selected/checked value, disabled, keyboard/TalkBack | Material primitives themed through Hq; wrap rather than draw custom inaccessible controls |
| Filter chip | select/toggle/removal where relevant, selected semantics and checkmark, disabled | Visual min 32dp; actual nonoverlapping hit region >=48dp. FlowRow wrapping, not a long column of pills |
| Tag/status | noninteractive; no fake ripple/action semantics | Compact data label, limited to useful attributes; overflow via “+N” that opens real details |
| Segmented control | single selection, group semantics, disabled option | Two equally sized Discover/Expense modes; no navigation tab substitute |
| Nav/list row | whole row navigates, one chevron, min 56dp with flexible text | Neutral surface; no button-plus-chevron duplication |
| Marketplace card | real photo or honest no-photo fallback; name/locality/rent basis; saved state only if implemented | Image 4:3, 16dp clip, max three summary tags; details disclose rest |
| Person/request card | avatar fallback, identity/context/status and role-allowed actions | Human portrait row, no property hero image or fake match percent |
| Alert/banner | severity label + explanation + one recovery action | Subtle semantic background, restrained icon; no giant error-colored page |
| Skeleton | geometry matches eventual content; not fake data | Static by default; subtle optional shimmer only with motion enabled |
| Dialog/sheet | focus trap/restoration, dismiss policy, system back, busy state | Filter/context sheets; destructive confirmations in dialogs; wizard is full screen |
| Progress | actual step index/count or byte progress, accessible state | One shared subtle step indicator; indeterminate only when work cannot be quantified |

Not every state applies to every primitive: decorative avatars do not get disabled/error buttons. At screen level, distinguish loading, ready, empty, failed and stale/offline; mutation state is independent. Never let a global loading flag erase all content for one action.

## Image and content policy

Use photography for actual property content and real profile photos where supplied. Supplied illustrations are limited to explanatory onboarding/empty-state contexts, never passed off as a listing's photo. Preserve logo fit and intrinsic aspect ratio. Property image uses crop with an appropriate focal point; logo always uses fit. No remote media is downloaded merely to fabricate visual evidence.

No placeholder reviews, “verified” badges without evidence, made-up search history, arbitrary distance or fake match percentage. Do not sanitize/delete genuine user text in production because it looks like test copy; fix seeded/demo fixtures and presentation adapters separately. Dates and backend status keys go through shared formatters; retain original values in persistence.
