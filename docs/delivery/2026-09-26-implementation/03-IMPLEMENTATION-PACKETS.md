# Ordered implementation packets

Read README aliases first. These are instructions for a future implementation run; no product edits were made while preparing them. Resolve paths with repository search before editing, because the baseline is a dirty working tree. Preserve unrelated work. Every packet ends with a checkpoint in `execution-status.md`: changed paths, behavior changed, tests actually run, evidence, blockers, next step.

## P00 — Establish the executable baseline

**Owners:** repository root, Android Gradle configuration, `AGENTS.md`, existing tests, `docs/archive/audits/2026-09-25/`, current evidence snapshot.

1. Compare current hashes against `evidence/source-snapshot.csv`; inspect changed relevant files, not just the commit. Read repository instructions. Record branch, commit, dirty paths, installed tool versions and device availability without printing `.env` contents.
2. Inventory all root/secondary routes and reachable overlays from `K/HabitiqApp.kt`, `U/AppShell.kt`, Discover entry, Profile and auth routes. Identify unused prototypes separately.
3. Establish isolated test accounts: no-flat seeker, household admin, household member, unrelated signed-in user. Use an emulator/test project and synthetic records. Do not create public listings or send messages to real users for QA.
4. Baseline build, existing unit checks and current screenshots. Record existing failures separately from introduced failures. Capture both populated and empty screens, keyboard-open forms and the provided reference states. Identify actual dependency/API versions before choosing implementations.
5. Audit fields actually stored in a flat, seeker, connection and message document. Record field paths/types/reader/writer/access. Specifically inspect whether exact addresses or private member information are in broadly readable flat documents.

**Acceptance:** reproducible commands/environment recorded; source-vs-runtime findings distinguished; privacy exposure and navigation decision listed. No tests marked passed based on older reports.

## P01 — Centralize the exact teal design system

**Owners:** `U/theme/HqColorTokens.kt`, `BrandColors.kt`, `FigmaColors.kt`, `Theme.kt`, spacing/type/radius files; resolve actual filenames before patching. Android `res/values/styles.xml`, `colors.xml` are coordinated with P04.

- Implement document 02 in the existing token owner. Add semantic fields only where required; redirect legacy palette consumers to it. Do not maintain separate equivalent palettes or run an unreviewed hex replacement across artwork, charts and status colors.
- Explicitly populate Material color roles used by the app so default secondary/tertiary purple cannot leak. Keep status colors semantic. Make the requested light scheme consistent on launch, auth, onboarding, app and system bars. No invented dark palette.
- Map typography through the existing owner; remove page-specific copies. Keep money/count alignment readable. Audit hard-coded colors in reachable UI and XML; classify each remaining literal as artwork, semantic datum, or intentional exception.
- Replace small normal text using muted with secondary where contrast requires it. Preserve exact supplied muted value for suitable large/decorative uses. Border and disabled values are not normal-text colors.

**Checks:** compile; token tests for exact values and key Material roles; screenshots of onboarding, primary action, selected/unselected chip, error and disabled state. Contrast calculations in document 02 remain valid for actual composited colors. No duplicate token owner.

## P02 — Fix layout ownership and navigation

**Owners:** `K/MainActivity.kt`, `K/HabitiqApp.kt`, `U/AppShell.kt`, shared app bars/scaffolds and reachable screen wrappers.

- Introduce/adapt one shared scaffold with explicit root and focused-flow modes. Apply inset ownership from document 02; remove redundant per-screen padding only after inspecting its parent. Handle display cutout, gesture/three-button navigation and IME independently.
- Standardize root headers and back headers. Replace text-arrow controls with the existing auto-mirrored icon control. Never insert a fake phone status bar.
- Keep four roots unless the user resolves the pending conflict differently. Focused posting/edit/auth/detail flows hide bottom tabs. Restore originating tab and scroll/filter state on return.
- Define one back handler per active destination: keyboard dismissal handled normally; dirty form then shows discard; clean form returns; modal dismisses before parent navigation. Root-tab switching must not silently discard draft data. Use saved state for destination identity; durable draft strategy comes in P07.
- Standardize sticky action area above keyboard/system navigation; do not add both safe-area spacer and consumed scaffold padding. Content scrolls beneath neither action bar nor navigation.

**Checks:** all four roots, focused form, detail and nested modal with gesture and three-button navigation; rotate, open keyboard, predictive/system back where supported. 48dp controls accessible and bottom final row reachable. Do not claim every screen fixed after checking only Home.

## P03 — Shared controls and states

**Owners:** existing Hq button, chip, text-field, feedback, app-bar and motion components; screen consumers.

- Implement document 02 component matrix before polishing screens individually. Separate interactive filter chips from passive tags; add selection, enabled and accessibility semantics with 48dp interaction area. Flow/wrap instead of squeezing labels.
- Buttons reserve dimensions while busy, expose button role, prevent double submit and communicate loading. Distinguish selection from disabled. Use native date/select/toggle behavior rather than making every field a chip.
- Bind labels, helper/error text, required indicators and keyboard actions. Retain entered values after failure. Do not expose raw backend exception messages; map known failures and log safe technical context.
- Separate loading, empty, error, stale and populated component contracts. Empty state must support an optional action; non-admin users must not receive unavailable admin actions. Errors have a retry that actually reexecutes the failed operation.
- Replace ornamental nested cards with rows/sections where the reference hierarchy warrants it. Keep meaningful flat/listing containers and preserve discoverability of actions.

**Checks:** focused component tests for role/selected/disabled/busy behavior; TalkBack traversal; enlarged fonts and long names; complete error announcement without redundant duplicate announcements.

## P04 — Artwork, launch and motion

**Owners:** `assets/brand/HABITIQAPP.svg`, existing brand renderer, Android manifest/theme/launcher resources, existing Hq motion/loading components. Exact workflow in document 05.

- Preserve supplied artwork and generate appropriate asset exports from it. Do not interpret a raster-wrapping SVG as a path-only Android VectorDrawable.
- Coordinate launcher/adaptive icon, Android system splash and in-app bootstrap surface. Use white launch background and explicit system-bar contrast. Do not create an extra splash activity or artificial startup hold.
- Replace orbiting/glowing loader with the restrained approved artwork treatment. Use actual operation status; timeout/retry/error exits required. Reduce-motion behavior must update when the setting changes.

**Checks:** icon on circle/squircle masks and small sizes; cold/warm/deep-link startup recordings; no dark flash, double splash or indefinite empty surface. Reduced-motion and interrupted transitions verified.

## P05 — Auth, setup and household surfaces

**Owners:** `K/HabitiqApp.kt`, `U/IntentChooserScreen.kt`, auth/profile screens, `U/ManageFlatHub.kt`, Tasks/Expenses/Bills screens and `K/flat/FlatViewModel.kt`.

- Follow document 04 truthfulness rules. Setup completion must reflect actual saved profile and preferences. Persist chosen discovery preferences or clearly label them temporary and pass them through navigation; never discard them while claiming personalization.
- Fix profile-save navigation to wait for success. Preserve Firebase-auth provider behavior; reference six-digit OTP and Apple screens are not evidence those backends exist. Do not add nonfunctional provider buttons.
- Keep Manage limited to Tasks and Expenses, monthly bills nested under Expenses. Make amounts currency-formatted without truncating paise; separate this formatting fix from any ledger migration.
- Render recent activity with human-readable action/date from actual records, no fabricated examples. Derive counts from confirmed state. Differentiate stale cached results from settled balances.
- Fix household switching/load identity: clear or explicitly mask old household state, cancel listeners, guard every response/write callback by active household/generation, and model resource errors independently. A failed expense listener must not produce “all settled.”

**Checks:** no-flat/admin/member states, sign-out/user switch, household switch during slow fetch and pending write, offline then retry, truthful setup, exact money display, existing task/rotation/bill/settlement regression suite. Security is backend-enforced, not just hidden buttons.

## P06 — Discovery projection, filters and cards

**Owners:** `K/data/TaskModels.kt` (resolve), repository vacancy reader/writer, `K/discover/DiscoverFilters.kt`, `DiscoverRanking.kt`, `Compatibility.kt`, `U/discover/UseAFlatDiscover.kt`, listing/detail/preview components; web types in P09.

- Extend the public listing projection deliberately to read persisted photo URLs and room fields already written by VacancyData. Specify missing/legacy values as unknown; do not derive private/shared room from beds available.
- Normalize room type, amenities and preference values at the boundary while preserving legacy records. Update filter and compatibility consumers together. Test unknown values without silently reclassifying them.
- Share presentation models for preview, published card and detail so rent units, availability, cover photo and tags agree. Use actual listing photos with loading/error fallbacks; illustrations are clearly placeholders, never passed off as that room.
- Replace hard-coded recent searches with real local history or remove that section until implemented. Show result counts from actual filtered data. Do not invent verified flags, availability, match percentages, ratings or social proof.
- Model discovery listener failures and real retry; retain filters/search when visiting details and returning. Keep public approximate location consistent with actual access control decision from P00.

**Checks:** reader round-trip for new and legacy records; explicit private/shared vs bed-count fixtures; missing/broken photos; every filter/reset/no-results case; preview-card-detail equality. Treat privacy exposure as a release blocker, not a styling exception.

## P07 — Posting, drafts and photo lifecycle

**Owners:** `U/discover/CreateDiscoveryPostScreen.kt`, `K/flat/FlatViewModel.kt`, repository vacancy/seeker writers, storage adapter, app-private draft persistence, manifest/provider resources if camera capture is supported.

- Implement document 04 transition and data contract. Keep actual supported posting types explicit; creating a household is not automatically an independent property-listing backend.
- Replace transient wizard-only state with a serializable draft scoped to account, household, type and existing post ID. Persist after edits, restore after process death, clear after confirmed publish/discard/logout according to retention policy. Do not persist live Uri permissions without securing access or copying to app-private draft files.
- Add real thumbnails, cover selection and ordering for both remote and pending images. Merge/dedupe new gallery selections without replacing earlier choices; enforce limits consistently. Preserve removal/edit changes in the preview. Full-resolution camera requires proper content URI capture; otherwise omit camera entry honestly instead of uploading tiny preview thumbnails.
- Validate at field, step and final submit. Freeze a validated submission snapshot, disable duplicate submissions and prevent ambiguous navigation while writes are pending. Support cancellation only if the underlying operation can resolve safely.
- Track per-file and aggregate upload progress from bytes; stable upload IDs; reconcile uploaded-but-uncommitted objects; await confirmed document write before success. Distinguish permission, connectivity, invalid file and session-expiry recovery. Preserve edits on failure.
- Update existing vacancy fields without erasing unknown additive fields. Concurrent edit detection or explicit refresh/reapply is required; silently overwriting another admin's changes is not acceptable. Deletion of referenced images occurs only after successful record update and reference check.

**Checks:** no-photo publication if still allowed, max photos, large/invalid input, reordered existing cover, selection replacement regression, denied permission, upload interrupted at each stage, write failure after uploads, duplicate tap, rotation/process death, edit conflict, current-flat switch. Storage rules tested locally before any deployment.

## P08 — Requests, chat and post lifecycle

**Owners:** `U/DiscoverBoardScreen.kt` (including private `ConnectionInboxScreen`), `K/flat/FlatViewModel.kt`, `K/data/DiscoveryRepository.kt`, `K/data/MessagingRepository.kt`, `U/discover/MyPostsScreen.kt`, chat/detail screens and rules tests.

- Success is an explicit repository-confirmed result. Keep request sheet open with pending state; dismiss/show sent only after success. Disable duplicate request and acceptance actions while pending. On failure retain text and allow retry.
- Model per-row request/accept/decline/block status; route back to the correct post/person. Do not turn an error into an empty inbox. Provide empty-state contextual creation action only to authorized users.
- Implement only status transitions supported by repository and rules. Closed/removed records must not expose generic resume merely because they are not published. Do not call seeker close a pause while displaying permanent end copy.
- Allow chat only for authorized accepted relationships. Preserve failed message drafts; avoid duplicate sends. A proposed viewing time in chat is not a confirmed booking/calendar integration.
- Scope edit action by posting type and ID, preserve status and photos, confirm destructive changes with precise consequences. Profile/My posts and Discover must route to the same lifecycle implementation.

**Checks:** success/failure/offline/double-tap for each mutation; sender/recipient/unrelated user permissions; closed and blocked relationships; expired listing; requests arriving during pause/end; navigation from notification/deep link where already supported. No test messages to real users.

## P09 — Responsive web continuity

Implement document 07. Read relevant installed Next.js guides first. Preserve current working routes and auth middleware. Use the same token/state/data contracts while adapting layout to browser conventions. Do not hide unrelated existing features just to make its dashboard look like the mobile screenshot. Any full Discover parity expansion is separately estimated and gated.

**Checks:** production build, lint, existing isolated end-to-end checks, desktop/mobile visual and keyboard QA, stable deep links, agreed route mapping and no unsupported feature claims.

## P10 — Integrated release gate

Execute document 06 against the candidate actually delivered. Fix new failures in the owning packet and rerun affected checks. Attach source identity, artifact hash, device evidence and unresolved platform gaps. A debug APK is a test artifact, not a Play Store/iOS release. Do not mark the whole product complete while a core path is blocked or while only screenshots/build output exist.
