# Web and future iOS continuity

## Shared product contract, separate platform implementation

Android remains the first target. Responsive browser support does not create a desktop executable; an Android APK does not run on iOS. Share design values, field semantics, permissions, state transitions, validation fixtures and test expectations across clients. Do not try to share Compose rendering code with SwiftUI or Next.js, or claim all clients have the same implemented features.

## Responsive web packet

**Known owners:** `apps/web/app/globals.css`, landing CSS modules, auth/provider components, `apps/web/components/HabitiqLoadingScreen.tsx` and its styles, `apps/web/app/dashboard/layout.tsx`, route pages, `apps/web/lib/discoveryTypes.ts`, existing Firebase adapters, `apps/web/tests/`, `apps/web/playwright.config.ts`. Resolve exact owners before editing. Installed stack at audit is Next 16.2.6 / React 19.2.4; repository instructions require reading relevant guides under `node_modules/next/dist/docs/` before code changes. Read routing/layout, server/client and styling guides relevant to the actual edit rather than assuming older Next conventions.

1. Inventory routes and capabilities. Android Discover exists, but a matching complete web Discover route was not established by this audit. Existing dashboard destinations include tasks, expenses, members, swaps, activity/settings and other modules. Document each route's current role and its proposed IA location.
2. Map exact palette to current CSS theme variables and component consumers; eliminate contradictory root/dark overrides for the requested light experience. Keep media artwork intact. Apply normal-text contrast and focus rules from document 02.
3. Unify landing, authentication and authenticated shell branding. Landing may use the supplied warm photographic composition, but no copied competitor marks, fabricated tenant counts, testimonials, verification or ratings. Use real listings or clearly labelled illustrative previews. Do not change the user's palette to the old orange/blue reference colors.
4. Adapt root navigation responsibly: larger desktop content/sidebar/header, mobile-friendly primary destinations, stable routes. Do not put Android system bars in web UI. For web-only existing functions, retain accessible secondary navigation and deep-link compatibility until a documented replacement exists. A global add-menu removal needs contextual replacements for its actual functions.
5. Keep forms within comfortable readable widths; preserve keyboard focus, error announcements, autofill, browser Back, URL state and refresh behavior. Use HTML semantics rather than copying mobile touch controls literally. Respect reduced motion and visible focus.
6. Update shared data projections for fields Android writes; test legacy and additive fields. Do not let a web edit erase new mobile photo/room fields. Schema/rules migration must coordinate all deployed clients, not only the local checkout.
7. Test actual mobile and desktop renderings and existing protected/auth routes in an isolated environment. A landing screenshot is not proof dashboard continuity.

**Boundary:** matching design-system and existing-flow corrections are P09. Full new web Discover, posting and chat parity requires its own route/API/state inventory and implementation subpackets using document 04, if absent. Do not hide that effort inside “responsive styling,” or declare all-platform feature parity without it.

## Future native iOS handoff

No built, signed or tested iOS app is delivered by this plan. Prepare a native implementation on macOS with a supported Xcode/Swift toolchain and the correct Firebase app registration when that phase begins. Verify current Apple/Firebase documentation then; do not freeze guessed SDK versions into this handoff.

| Shared requirement | iOS adaptation |
|---|---|
| Four-root IA (pending six-root conflict) | Native tab/navigation structure; focused forms/details follow platform navigation conventions |
| Exact teal semantic tokens | Named color assets/semantic theme; all native control tint and status roles explicitly reviewed |
| Safe areas and keyboard | Native safe-area/layout behavior; no copied Android inset offsets or fake status bars |
| Supplied brand | App icon and static launch assets exported from preserved original; app loading follows real state |
| Motion | Respect Reduce Motion; native interactive back/sheet transitions; no transplanted Android animation-scale reader |
| Auth | Firebase-supported iOS providers, secure session persistence and callback handling; add a provider only with functioning configuration |
| Photos | Platform photo picker and camera permission; durable local staging; same order/cover/upload state contract |
| Data | Same field semantics, legacy compatibility, lifecycle transitions, rules and operation reconciliation |
| Accessibility | VoiceOver, Dynamic Type, appropriate native hit areas, contrast and focus/order validation |
| Notifications/deep links | Platform-specific registration and route restore; permission timing grounded in user benefit |

Create the iOS phase as bounded packets: toolchain/configuration; tokens/components/brand; auth/session/root shell; household features; discovery/posting/photos; requests/chat; accessibility/device/security regression; signing/release. Reuse synthetic fixtures and case IDs in document 06, with iOS-specific outcomes. Do not reuse Android screenshots or unit results as iOS evidence.

Required later configuration: Apple developer/team and bundle identity, signing/provisioning, Firebase iOS app configuration, provider URL callbacks, notification capability if used, privacy/permission strings, actual device/simulator access. These are future platform dependencies, not reasons to block current Android plan work or to request credentials now.

## Cross-client compatibility gate

For every schema addition, document field/type/default, producer, all readers, access rule, migration/backfill policy and rollback behavior. Test Android-created records opened/edited by web and vice versa. Future iOS must pass the same fixtures. Avoid destructive schema replacement as a visual task. Identify deployed older clients before removing compatibility fields such as `active`.

Google Maps expansion remains deferred. Existing area text/search must work without promising live maps, geocoding accuracy or distance calculations that are not backed by implemented data.
