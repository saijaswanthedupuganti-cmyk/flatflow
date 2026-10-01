# Habitiq iOS native implementation handoff

Updated 25 September 2026. This handoff prepares a real SwiftUI application that follows the Android-first product contract in [document 00](00-ANDROID-FIRST-MASTER-PLAN.md). No iOS project currently exists in this workspace, and Windows cannot compile or sign an Apple application. Therefore this is an executable architecture contract, not a claim that an iOS binary has been produced.

## Platform and product baseline

- SwiftUI application using the current supported Swift/Xcode toolchain selected when the project is created.
- Swift 6 language mode, complete concurrency checking, Approachable Concurrency, and main-actor default isolation for the app target.
- Firebase Authentication and Firestore configured for a separate iOS app registration in the same intended environment. Never copy Android configuration files into iOS.
- Value-type `struct` models and `enum` state. Use `@Observable` feature models on the main actor. Begin concrete; introduce repository protocols only where tests or multiple implementations require a real substitution boundary.
- `async`/`await` for Firebase operations or adapters. Use structured tasks and cancellation tied to screen lifetime. Do not use detached tasks for ordinary reads.
- `Logger` with private values by default. Never log credentials, invite codes, message bodies, exact private location or financial notes.

## Navigation

Use one `TabView` with Home, Discover, Manage and Profile. Add is a labelled action presented from the root shell rather than a fifth information destination. On iPad, adapt the same destinations to `NavigationSplitView`; do not create a second ownership model.

Each tab owns a `NavigationStack` with typed destinations. A root `AppRoute` resolves authentication, account loading, no-flat/pending membership and the retained internal intent before showing a tab. Universal links for invitations and supported discovery details must enter the same resolver. Validate the destination and preserve its code or stable identifier through authentication.

```text
AppRoute
  launching
  signedOut(retainedIntent?)
  resolvingAccount(retainedIntent?)
  setup(SetupIntent)
  pendingMembership(RequestID)
  member(ActiveFlatID, Tab, Destination?)
  recoverableFailure(message, retry)
```

Never represent a read failure as `setup`. Clear previous-flat screen state before subscribing to the selected flat.

## Feature ownership

| Product owner | SwiftUI root | Required child destinations |
| --- | --- | --- |
| Home | `HomeView` | task detail, expense detail, requests, bills; all route into the owning feature |
| Discover | `DiscoverView` | mode, filters, vacancy/person detail, connection request, inbox, conversation, My posts, editor |
| Manage | `ManageView` | Tasks and Expenses |
| Tasks | `TasksView` | My/All, task detail, create/edit, swap review, away status |
| Expenses | `ExpensesView` | add split, settlement, monthly bills, bill detail, period review/history |
| Profile | `ProfileView` | Account, Discovery profile, My flat, members/invites, settings, lifecycle actions |

Shortcuts navigate to these owners. They do not embed duplicate task, expense or request implementations in Home.

## Suggested module layout

```text
HabitiqApp/
  App/                 HabitiqApp, AppRouter, AppRoute, AppSession
  DesignSystem/        Color, type, spacing, radius, controls, feedback, motion
  Domain/              IDs, value models, closed state enums, money/date rules
  Data/Firebase/       concrete auth, flat, member, task, expense, discovery, message repositories
  Features/Auth/
  Features/Setup/
  Features/Home/
  Features/Discover/
  Features/Manage/Tasks/
  Features/Manage/Expenses/
  Features/Profile/
  Support/              logging, deep links, formatting, test fixtures
HabitiqTests/           Swift Testing for models, reducers/state and repository adapters
HabitiqUITests/         XCTest UI journeys only
```

Do not create a framework per folder initially. Keep a single app module until build time, ownership or reuse demonstrates a boundary.

## Cross-platform contract

Firestore field names, status strings and collection ownership must remain compatible with the currently approved backend. Centralize decoding and validate unknown server states instead of scattering raw strings through views. Use strongly typed IDs in Swift while encoding their existing string representation.

The following rules must have matching Android, web and iOS fixtures:

- task effective status, due-date interpretation, rotation and away behavior;
- expense validation, share rounding, currency separation and exclusions;
- settlement direction and supported overpayment policy;
- monthly bill instance identity, collection and close/carry-forward behavior;
- membership roles, selected-flat validity and join request transitions;
- discovery post and connection transitions, blocking, report behavior and permission failures.

Keep amounts as a currency-aware decimal/minor-unit representation. Do not model financial amounts as display-rounded `Double` values in new iOS domain code. Exact encoding must be reconciled with existing Firestore documents before the first financial write.

## View-state contract

Each feature model exposes one mutually exclusive state such as `loading`, `empty`, `content`, `failed`, plus separate mutation state where needed. A listener error produces `failed` or an inline recoverable warning; it never emits an empty collection as though the operation succeeded.

Use one task for each ordered operation. Re-check selected-flat membership after every suspension before applying results. Cancel listeners/tasks when the route or active flat changes. Prevent duplicate mutation submissions at both UI and persistence boundaries.

## Design adaptation

Use the same Habitiq semantic tokens, content hierarchy and destination names as Android, mapped to Apple platform behavior:

- native NavigationStack titles, sheets, confirmation dialogs and swipe/back behavior;
- minimum 44-point hit regions, Dynamic Type, VoiceOver order and Reduce Motion;
- SF Symbols with a consistent rendering style; official Google mark only for Google authentication;
- safe areas and keyboard-aware forms; do not force Android dimensions into SwiftUI;
- iPad readable widths and split navigation where it improves hierarchy;
- restrained state transitions and no decorative looping animation on daily Home.

The visual reference set from documents 04 and 06 controls brand continuity, but iOS uses native controls where that improves accessibility and expected behavior.

## Maps phase

Ship locality-based Discovery without MapKit distance claims in the first iOS implementation. The future location service can use MapKit/CLGeocoder behind a concrete boundary, feature flag and permission explanation. Approximate public location, text fallback, denied-permission behavior, offline behavior and API/usage constraints must be designed before enabling it. Do not request continuous location for discovery browsing.

## Implementation order

1. Create the Xcode project and app registration on macOS; configure environments without committing service secrets.
2. Add semantic design tokens, shared controls and accessibility previews.
3. Implement AppSession/router, authentication, retained deep links and create/join/pending states.
4. Implement selected-flat lifecycle and Home using read-only repository paths first.
5. Add Manage → Tasks, then Expenses, using shared contract fixtures before writes.
6. Add Discover modes, posts, requests, accepted conversations and safety actions.
7. Add Profile/membership/account lifecycle and iPad adaptations.
8. Run the complete journey matrix from document 00, then TestFlight internal QA.

## Verification gates

- Swift Testing covers domain transitions, decoding failures, money/task fixtures and route resolution.
- Firebase Emulator or controlled test environment covers rules, multi-account joins, connections/messages and financial concurrency.
- XCTest UI covers sign in, retained invite, task completion, expense entry, discovery request and flat switching.
- iPhone small/large sizes and iPad portrait/landscape pass with Dynamic Type, VoiceOver, dark/light mode and Reduce Motion.
- Cold start, process termination/restoration, background/foreground, offline/reconnect and notification/deep-link entry are verified.
- Archive succeeds on macOS, signing is valid, privacy manifests/descriptions match actual SDK/data use, and TestFlight installation is exercised.

Until these gates pass on Apple hardware/tooling, describe iOS as prepared or in development—not ready to ship.
