# Oddroof Android component system

Version 1.0 (19 Oct 2026). Code: `apps/android/app/src/main/kotlin/habitiq/app/ui/components/` and `ui/theme/`.
Gallery: `./gradlew testDebugUnitTest --tests "*GalleryScreenshotTest"` writes `app/build/screenshots/gallery-*.png`
(light, 200% font, and `-dark-future` renders of the dark palette that this release does not ship).

Every screen is built from these components. A component is designed once, here, and looks and behaves the
same everywhere. If a screen needs something that isn't here, extend the component (add a parameter with a
default) instead of building a one-off.

## Ground rules (apply to every screen)

1. **One Primary per screen** (Von Restorff). Everything else is Secondary (tonal), Tertiary (text) or an icon.
2. **Form submit = full-width Large Primary at the bottom** of the form. Don't disable it while the form is
   incomplete; leave it enabled and show inline field errors on press.
3. **Never use raw Material** `Button`, `TextButton`, `OutlinedButton`, `IconButton`, `Switch`, `Checkbox`,
   `RadioButton`, `OutlinedTextField`, `AlertDialog`, `ModalBottomSheet`, `Snackbar`, `TopAppBar` in a screen.
   Use the Hq component below.
4. **Never write a colour, radius, duration or shadow** in a screen. Colours come from `LocalHqColors`
   (semantic names), sizes from `HqSpacing` / `HqRadius` / `HqSize`, motion from `HqMotion`.
5. **48dp minimum touch target** for anything tappable, even when the visual is smaller (chips 32, small
   buttons 40, checkbox 20 sit inside a 48 target).
6. **Feedback within 100ms**: every pressable component already springs on press; don't add your own.
7. **Text grows, containers grow**: no fixed heights around text, no `maxLines = 1` on labels that matter.
   Everything in the gallery is checked at 200% font.
8. **Reduced motion is automatic**: components read `hqReduceMotion()`; screens using `rememberHqTransitions()`
   get the reduced variants for free.
9. **Light only this release.** `HabitiqTheme()` never follows the OS dark setting. Dark token values exist
   for a future release; don't read `isSystemInDarkTheme()` anywhere.

## Tokens

### Colour (palette C, "Navy and teal")
Teal is the only "press me" colour (buttons, switches, checkboxes, selected chips/segments). Navy (`brandInk`)
is for brand surfaces (summary/hero cards, dark headers, the voice overlay). Blue (`link`) is for links and
info only. Status colours (`statusSuccess/Warning/Danger/Info Fg+Bg`) always come with text or an icon.
Overdue uses danger. Coral is retired from UI roles (`warm*` and `brandCoral` now map to danger).

Component roles added in this pass (names only; they default to existing values): `controlOn`,
`onControlOn`, `controlOff`, `controlThumbOff`, `stateLayer`, `stateLayerOnFill`, `segmentSelectedBg`,
`segmentTrack`, `snackbarBg/Fg/Action`, `scrim`, `onImage`, `brandInk`, `onBrandInk`, `link`.
`HqColorContrastTest` checks every text pair at 4.5:1 and every control pair at 3:1.

### Spacing (4dp grid)
`xs 4 · sm 8 · md 12 · lg 16 · xl 20 · xxl 24 · xxxl 32 · huge 40`. Screen gutter 20. Row padding 16 x 12.
Card padding 20. Gap between form fields 20, between sections 32.

### Radius (one scale)
| Step | dp | Token | Used by |
|---|---|---|---|
| xs | 8 | `HqRadius.small` | badges, tags, skeleton blocks |
| sm | 12 | `HqRadius.control` | segmented indicator, snackbar |
| md | 16 | `HqRadius.button`, `HqRadius.field` | every button, every field, segmented track |
| lg | 20 | `HqRadius.card` | cards, groups |
| xl | 28 | `HqRadius.sheet` | sheets (top corners), dialogs |
| pill | full | `HqRadius.pill` | chips, switch, avatars |

Nested corners are concentric: inner = outer - inset (16 track, 4 inset, 12 indicator). Shapes are ready-made
in `HqShapes`.

### Elevation
`Modifier.hqShadow(HqShadow.X, shape)`: tinted with the ink colour, never black, dropped in dark.
`Floating` (cards, selected segment, switch thumb), `Raised` (snackbar), `Overlay` (dialog, sheet). Lists
and rows have no shadow. The Primary button has a faint brand-tinted Floating lift.

### Motion (`HqMotion`)
| Token | Value | Use |
|---|---|---|
| `fast` | 120ms | colour, focus ring, selection |
| `base` | 200ms | small enters, content swaps |
| `slow` | 300ms | screen travel (never longer) |
| `reduced` | 80ms | the only animation under reduced motion (a fade) |
| `pressSpring` | stiffness 800, damping 0.7 | press feedback, switch thumb, checkbox tick |
| `layoutSpring` | stiffness 400, damping 0.85 | segmented indicator, list placement, size changes |
| `sheetSpring` | stiffness 400, damping 0.9 | hand-built sheets/panels |
| `standard` | ease-out (0.23, 1, 0.32, 1) | anything changing on screen |
| `emphasized` | (0.2, 0, 0, 1) | things travelling across the screen |

Press scale: buttons 0.97, cards/rows 0.985 (`hqPressable`). Haptics (`rememberHqHaptics()`): `confirm()` on
a meaningful primary confirm, `tick()` on segment change, `toggle()` on switches. Never on keystrokes.

### Transitions (`rememberHqTransitions()`)
| Member | When |
|---|---|
| `pushEnter/pushExit/popEnter/popExit` | Shared axis X: going deeper (list to detail, wizard step) and back. 30dp slide + fade. |
| `fadeThroughEnter/Exit` | Switching peers with no spatial relation: bottom-nav tabs, segmented content. |
| `sheetEnter/Exit` | Hand-built bottom panels only (Material sheets animate themselves). |
| `contentSwap<S>()` | `AnimatedContent` for a value replaced in place (loading to content, empty to list). |
| `counter()` | `AnimatedContent<Long>` for numbers going up/down. |
| `expandEnter/collapseExit` | `AnimatedVisibility` for a local reveal (inline error, "show more"). |
| `Modifier.hqAnimateItem()` | `LazyColumn` items whose order or membership changes. Not for plain scrolling. |

## Components

### HqButton
`HqButton(text, onClick, variant, size, leadingIcon, trailingIcon, loading, enabled, haptic, fullWidth)`

| Variant | Look | Use for |
|---|---|---|
| `Primary` | teal fill, white label | the one main action |
| `Secondary` | tonal (teal-soft fill, teal label) | a real alternative beside the Primary ("Previous") |
| `Tertiary` | text only | low-emphasis ("Skip for now"), dialog dismiss |
| `Destructive` | red fill | irreversible actions, usually inside a confirm dialog |
| `GhostOnDark` | white outline + label | on navy surfaces and photos only |

Sizes: `Large` 56 (default when `fullWidth`), `Medium` 48 (default otherwise), `Small` 40 visual / 48 target.
Same 16dp radius and 16sp SemiBold label at every size. States: pressed (spring to 97% + state layer + bounded
ripple), focused (2dp focus ring outside the shape), disabled (neutral fill, 38% label, announced disabled),
loading (spinner replaces the label in place, width does not change, TalkBack hears "Loading").

- Do: one Primary; full-width Large at the bottom of forms; side-by-side pairs as two `weight(1f)` buttons with
  the Primary on the end; `haptic = true` on "Complete task", "Record payment".
- Don't: two Primaries; Primary for destructive actions; disable the submit button instead of validating;
  rewrite the label to "Saving..." (use `loading`).

`HqTextButton(text, onClick, color, leadingIcon)`: inline text action in rows and headers ("See all").
`HqIconButton(icon, contentDescription, onClick, style = Standard | Tonal)`: 24 glyph, 40 visual, 48 target.
`contentDescription` names the action and the object ("More options for Kitchen cleanup").

### HqSwitch / HqSwitchRow
52x32 track, 24dp thumb on the press spring with a small squash while held and mid-travel; track colour
follows the thumb; optional `thumbIcon`; Switch role with "On"/"Off". Use `HqSwitchRow(title, checked,
onCheckedChange, supporting)` almost always: the whole row is the target. Only for settings that apply
immediately (no Save button).

### HqCheckbox / HqCheckboxRow, HqRadio / HqRadioRow / HqRadioGroup
Rows are 56dp (72 with supporting text), whole row toggles. Checkbox = independent on/off items in a list.
Radio = one of several, 3+ options or options that need a supporting line (use `HqRadioGroup`). For 2-4 short
options shown inline, use a Choice chip or segmented control instead.

### HqSegmentedControl
`HqSegmentedControl(options, selectedIndex, onSelect, equalWidth = true, haptic = true)`. Sliding pill on the
layout spring, 48dp segments, Tab role with selected state, haptic tick. Use for 2-4 views of the same content
(Tasks / Expenses, Day / Week / Month). `equalWidth = false` for more or longer labels (scrolls). Labels wrap at
large font sizes. Switch the content below with `fadeThroughEnter/Exit`. Not for navigation between screens.

### Chips
`HqChip(label, selected, onClick, kind = Filter | Choice, leadingIcon)`: 32 visual / 48 target.
`Filter` = independent multi-select filters (check mark when on, Checkbox role). `Choice` = one-of-a-few inline
(no check, RadioButton role; wrap in `selectableGroup()`). No `onClick` = read-only label. `HqRemovableChip`
= an applied value the person can remove. Lay chips out with `HqChipFlow`.

### HqTextField / HqAmountField
Label above (never floating), 56dp, 16dp radius, 1dp control border at rest; focus animates to a 2dp teal
border (fast tween); error = red border + icon + message under the field, announced politely; helper text
under the field when there is no error. `prefix`/`suffix`/`trailingContent` for units and pickers.
`HqAmountField` = "₹" prefix, decimal keypad, at most 2 decimals. Set `imeAction = Done` and `onImeAction`
to submit on the last field. Use `HqDateField` for dates.

- Don't: placeholder as label; validate on every keystroke (validate on blur or submit); `enabled = false`
  for read-only values (show them as text).

### HqCard, HqGroup, HqRow
`HqCard` = one radius (20), one elevation (Floating + 1dp subtle border), 20 padding. `Status` variant is flat
and tinted. Cards group one object; don't put cards in cards.
`HqGroup` + `HqRow` + `HqRowDivider` for lists: rows never get their own card. `HqRow(title, supporting,
leading, trailing, onClick)`: fixed 40dp leading column (icon or avatar) so titles align; trailing
`HqRowValue`, `HqRowChevron`, or a control. Divider inset = 16 + 40 + 12 when rows have a leading item.
`HqNavRow` and `HqTaskRow` remain for their specific uses.

### App bar
`HqAppBar(title, onBack, large = false, actions)`: the one pattern. Standard: 48dp back arrow + 18sp title +
actions, 64dp. Large: back/actions row, then a 28sp title on its own line (top-of-hierarchy screens, or where
the title is the content). Actions are `HqIconButton`s or one `HqTextButton`. `HqPageHeader` (in-body header that
pins on scroll) stays for screens already using it. Older `HqBackAppBar`/`HqRootAppBar` map to the large
inline style and should migrate to `HqAppBar`.

### Overlays
`HqBottomSheet(onDismiss, title)`: 28dp top corners, drag handle (sheets only), scrim fades with the sheet,
20dp sides. For details, filters, quick actions; a 10-field form is a full screen instead.
`HqConfirmDialog(title, message, confirmLabel, ...)`: only for destructive or critical decisions. 28dp
corners, Overlay shadow, centred entrance 96% to 100% + fade (no scale under reduced motion), dismiss
(Tertiary) then confirm on the end; buttons wrap at large fonts. `HqDiscardChangesDialog` for leaving a
form with typed input.

### Snackbar
`HqSnackbarHost(state)` in the Scaffold, or `LocalHqSnackbar.current?.show(message, "Undo") { ... }`.
Navy surface, 12dp corners, slides up 24dp on the layout spring, Short = 4s, announced politely, optional
single action. For completed actions only, never for errors that need a decision.

### Feedback
`HqEmptyState(icon, title, message, primaryLabel, onPrimaryClick)`: icon in a teal-soft circle; always says
what's empty and offers the next action. `HqErrorState` for a failed screen load. `HqInlineError/Info/Loading`
for single actions. `HqSkeletonBlock`, `HqCardSkeleton`, `HqListSkeleton(rows)` while content loads (the sweep
stops under reduced motion; announced once as "Loading").
