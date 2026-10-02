# Habitiq: Figma Make → Android implementation plan

**Source of truth:** Figma Make file `XvM5qZJzfLnSvHyp0osNjZ` (Design-Habitiq-Mobile-App), 19 views, light mode only.
**Target:** `apps/android` (Kotlin / Compose). iOS follows after Android is signed off.
**Scope rule:** UI only. Navigation, back stack, swipe/gesture behavior, ViewModels, repositories, Firestore rules, permissions and role logic are NOT touched. Every phase must keep `./gradlew test` green and the existing BackHandler behavior in `HabitiqApp.kt` intact.
**Dark mode (decided 2 Oct 2026):** light mode first, matching the Figma file exactly, and the app launches in light regardless of the OS setting (`HabitiqTheme` default `dark = false`). Dark mode is a later pass: restore `isSystemInDarkTheme()` in `Theme.kt` and re-derive the dark tokens from the new light ones.

## 0. Known deltas (Figma vs current Android)

| Area | Figma | Android today | Action |
|---|---|---|---|
| Heading font | DM Sans 600-800 | Inter only | Bundle DM Sans (OFL), add `HqFontDisplay`, use for display/title tokens only |
| UI font | Inter 400-800 | Inter 400-700 | Add 800 only if a token needs it; otherwise cap at 700 |
| Primary button | h52, radius 16, teal fill, dark-teal text, soft teal shadow, press scale .98 | h52, radius 12 | Update `HqButton`, `HqRadius.control` usage |
| Bottom nav | Floating pill: inset 10, radius 22, h70, raised 54dp center "+" | Docked bar with center action | Restyle `ShellBar` in `AppShell.kt`; keep tab order and `ShellCreate` logic |
| Card radius | 18-20 | 20 | Verify per component |
| Tokens | `--brand #14b8a6`, `--brand-dark #087f73`, `--coral #ff6b5a`, `--ink #19312f`, `--line #e3e9e7`, `--sand #f3eadc`, `--warning #ba6f29`, `--danger #c4524a`, `--success #25856e` | v2 semantic scheme | Diff each against `HqColorTokens.kt`; adopt the Figma value where it differs, then re-derive dark |

The Figma CSS uses very small type (8-10px for captions/metadata). Android MUST keep the 12sp minimum and font scaling. Where the Figma size is below 12sp, use `labelSmall` and note the deviation.

## Phase 1: Foundation (tokens, type, shapes, motion)

Files: `ui/theme/*`, `res/font/`, `ui/components/HqButton.kt`, `HqCard.kt`, `HqChip.kt`, `HqTextField.kt`.

1. Token diff table (Figma CSS vars → `HqColorScheme` fields). Update values, add missing ones (`sand`, `warningSoft`, `coralSoft`), derive dark pairs, check contrast ≥ 4.5:1 text / 3:1 UI in both themes.
2. Add DM Sans; map `display`, `titleLarge2`, `titleMedium2`, `titleSmall2`, `amount` to it. Body/labels stay Inter.
3. Align radius, elevation (Figma `--shadow: 0 10px 30px rgba(35,65,61,.08)`), press feedback (scale .98, 180ms).
4. Unit tests for token completeness (light/dark expose same keys: already enforced) plus a contrast test.
5. Screenshot gate: a token/component catalog screen (debug only) in light/dark × 1.0/1.3 font scale.

Exit: catalog matches the Figma components side by side; existing screens still compile and render.

## Phase 2: Shared components

Rebuild or restyle, one commit each, matching the Figma Make helpers:

- `Button` (primary / secondary / ghost / danger, disabled, loading) → `HqButton`
- `Avatar` (+ tones teal/sand/coral, stack) → `HqAvatar`
- `Header`, `SectionTitle` → `HqAppBar`, new `HqSectionTitle`
- `Icon` set (home, manage, discover, profile, check, chevron, back, clock, receipt, users, ...) → one `HqIcons` object; port the Figma SVG paths as `ImageVector`s so icons match exactly
- `TaskMotif` → `HqIllustrations`
- Bottom sheet / overlay (filter, away, swap, add) → `HqOverlays` (radius 28 top, handle, scrim)
- Segmented control, toggle, tags/chips, setting row, menu row, request card, balance hero

Exit: every Figma primitive has one Compose equivalent, no screen builds its own.

## Phase 3: Screen pass (per view; the audit decides what is restyle vs rebuild)

| Figma view | Android file(s) | Likely work |
|---|---|---|
| Onboarding (intent, create/join) | `IntentChooserScreen`, `CreateFlatScreen`, `JoinFlatScreen`, `figma/CreateFlat*` | restyle |
| Home | `FigmaHomeScreen`, `HomeContent` | restyle |
| Manage hub | `ManageFlatHub`, `ManageContent` | restyle |
| Tasks, Task detail, Add task, Away, Swap | `TasksContent`, `figma/TaskDetailScreen`, `CreateTask*`, `GoingAwayScreen`, `SwapRequestBanner` | restyle |
| Expenses, Balances, Month close (Bills) | `ExpensesScreen/Content`, `BillsScreen` | restyle |
| Discover (flat + flatmate), Filter sheet | `DiscoverBoardScreen`, `discover/*` | restyle, then split the 1020-line file |
| Flat detail, Wishlist | `FlatListingDetailScreen`, wishlist (verify exists) | restyle / possibly new |
| Workplace map | `figma/MapLocationPicker` (verify it is the same feature) | verify |
| Person detail, Connections, Notifications, Chat | `ConnectionSafetySheets`, `MessagingRepository` UI (verify) | likely new UI over existing data |
| My posts, Create post | `MyPostsScreen`, `CreateDiscoveryPostScreen` | restyle |
| Profile, Edit profile, My flat, Discovery profile, Preferences | `ProfileScreen/Content`, `SettingsScreen`, `FlatSettingsScreen` | restyle |

Process per screen: capture Figma screenshot (via a design-mode `get_design_context` on the matching node) → restyle with Phase 2 components → run previews light/dark × font scale → unit tests → commit.

The Make file is a code prototype, so node-level design context is not available through `/make/` URLs. The React source in `src/App.tsx` and `src/index.css` is the spec. For any visual doubt, you share the frame screenshot.

## Phase 4: Gap audit (do before Phase 3 starts)

For each of the 19 views: does an Android screen exist, is its data layer present, and does it differ visually. Output a table with restyle / rebuild / new. Product-scope conflicts are flagged, not built. Examples to check against MASTER.md section 3: numeric trust scores, any "Explore without a flat" route, and photo upload (current avatars are initials only).

## Phase 5: QA and release gate

- `./gradlew test`, lint, release build with R8
- Screenshot matrix: light/dark, font scale 1.0/1.3, widths 360/390/430
- TalkBack pass on Home, Tasks, Expenses; 48dp targets; focus order
- Back and gesture regression: Manage hub → Tasks → Task detail → back; tab switching preserves scroll
- You test on device (no live adb debugging by me): I hand over the APK path

## Phase 6: iOS (after Android sign-off)

Reuse the same token table and component list; SwiftUI adapters per MASTER.md section 15. Separate plan once Android is done.

## Progress log

- **Phase 1 started (2 Oct 2026):** light tokens now use the Figma values (brand `#14B8A6`, dark-teal button text `#062F2A`, ink/line/sand/coral/warning/danger/success); app forced to light; DM Sans bundled for headings and balance amounts; primary/secondary buttons use radius 16, teal shadow, line-coloured secondary border. 108 unit tests pass, debug APK builds.
- **Deliberate deviations from the Figma values, forced by the project's AA contrast tests** (same hue, slightly darker): muted text `#82918F`→`#5F6F6B`, success `#25856E`→`#1E7560`, warning `#BA6F29`→`#9A5A1C`, danger `#C4524A`→`#B24139`, focus ring and selected border use `#087F73`, selected text `#0F766E`, primary pressed state is lighter (`#5EEAD4`) because dark text on the darker teal fails. Revisit only if you accept lower contrast.
- Button label stays 16sp (Figma uses 13px); captions keep the 12sp floor.

- **Phase 2/3 (2 Oct 2026, same day):** shared components built (icons, avatars with tones, page header, section title, large task card, manage switch, callout/timeline/menu rows, search bar, listing frame). Screens now following Figma: Home, Manage (Tasks/Expenses switch), Task detail (+swap sheet, mark complete, unavailable entry), Expenses and Balances, Monthly Bills and Close month, Profile, Quick-add sheet, Discover (list, filters row, listing and person cards, flat detail, person profile, connections inbox, chat, My Posts), Activity, first-run photo slides and the four-option intent chooser. Figma tab order and the floating nav are in. Light mode only; status bar icons forced dark except over hero photos.
- **Added (2 Oct 2026, later):** Saved flats (heart on flat detail, shortlist screen, stored on the device per account via DataStore), "Find a home near a place" map search (picks a point, converts it to an area/city search because listings only carry approximate areas), Figma-style My flat summary, Edit profile avatar, Privacy & Security (switch + danger zone + sign out).
- **Not done yet:** notifications bell with unread state (no unread store exists; the Activity screen stands in), Figma layouts for create/join flat steps, the going-away screen, create-task forms and the Discovery profile pane (they have the new colors, fields and headers only). Dark mode pass, then iOS.

## Open questions for you

1. Dark mode: derive from the existing dark scheme (recommended), or do you want Figma dark frames designed first?
2. Figma views with no Android screen yet (Wishlist, Connections, Notifications, Chat): build as UI-only over the existing data layer, or hold until the gap audit confirms what exists?
3. The Figma file sets type at 8-10px for captions. Keep the 12sp accessibility floor (recommended)?
