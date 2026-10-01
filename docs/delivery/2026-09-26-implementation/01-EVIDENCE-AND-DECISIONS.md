# Evidence, decisions, and limitations

Written against `52b4a1a118c7a481e166561e71918d389a55ea5e` plus the captured working tree. This is source tracing and supplied-asset inspection, not a new Android visual certification.

## Input provenance

The explicit request is to prepare the implementation plan, not implement now. Attached QA documents supply requirements and historical observations; their claims about screenshots are not independently reproduced findings. Four distinct briefs are copied into `evidence/01-visual-qa-context.txt` through `04-launch-loader-gate.txt`. The two “FINAL UI CORRECTION” attachments are byte-identical (SHA-256 `588DE5EAC9A47EEBE3401B49A5394C82C1E9D911CFDDB468E0A2DF8454367249`) and counted once. The direct teal palette takes precedence over historical screenshots, comments and coral/violet docs.

`assets/brand/HABITIQAPP.svg` was inspected structurally and its embedded image visually inspected. Root size is 220 × 217; viewBox is 0 0 220 217. It wraps a 1263 × 1246 embedded PNG using a pattern/use transform. It is not a path-based logo. `evidence/supplied-brand.png` is an exact extraction for inspection, not edited/recolored/generated artwork. The word “Code Agent SVG” in the brief does not prove that a second asset exists. Use the supplied file for the branded-loader candidate; confirm role before replacing any separately approved launcher mark. Do not invent a missing second logo.

## Design language

- Audited application: Android Compose shell → root theme → Hq primitives → Discover/posting and household screens. Web checked separately for continuity owners.
- Binding design sources: direct user palette; copied QA/launch briefs; prior four-root product brief. Current source is evidence of behavior, not authority to override the new color request.
- Governing owners: `HabitiqApp.kt` → `HabitiqTheme`/`AppShell`; `Theme.kt` maps `HqColorScheme` to Material3; `HqLightColors` currently reads `FigmaColors`; `HabitiqBrand` is used by startup and legacy surfaces.
- Explicit exceptions: white launch background; original colors/gradients baked into supplied artwork; actual user photos. No new decorative gradient exceptions.

## Three foundational design findings

| # | Problem | Evidence and runtime chain | Required correction | Scope | Confidence |
|---|---|---|---|---|---|
| V1 | Current palettes contradict approved teal | `DS/FigmaColors.kt` defines coral `#E4533D`; `HqLightColors` consumes it; root `HabitiqTheme` supplies it to Hq controls. `HabitiqApp.kt` startup directly uses violet `HabitiqBrand` | Make semantic owners use document 02 values, migrate direct consumers and obsolete comments | Android theme/startup and verified consumers; web separate packet | High, source-proven colors |
| V2 | Headers and insets lack one ownership contract | `U/AppShell.kt` applies bottom navigation padding but no top content inset; `HqAppBarBase` delegates insets to Material TopAppBar; Discover/MyPosts/CreatePost also use manually padded text back controls | P02 establishes one screen scaffold owner, consumes padding once, and routes all headers/back controls through existing HqAppBar | Root and secondary screens | High for mixed ownership; actual overlap/device geometry unverified |
| V3 | Interactive chip state is incomplete | `HqChip.kt` uses clickable Text, labelMedium and 4dp vertical padding; no explicit selected semantics or disabled parameter, while form/filter callers pass selected values | Extend HqChip with semantic variants and 48dp interactive target; metadata tags stay noninteractive | Filters, wizard preferences, tab-like controls | High, source-proven API; rendered target measurements pending |

Improve first: V2 prevents controls colliding with system UI. P01 precedes its final styling so geometry screenshots use the final palette, but do not defer inset diagnosis for cosmetic work.

## Separate functional/state evidence ledger

These are within the user's requested architecture/state scope; they are not presented as visual findings.

| ID | Source anchor | Current fact | Consequence and packet |
|---|---|---|---|
| F01 | `K/data/TaskModels.kt`: VacancyData versus VacancyListing; `DiscoveryRepository.parseVacancyListing` | Writer model has photoUrls, roomType and new fields; public reader lacks them | Saved photos/room choices do not reach marketplace projection. P06 |
| F02 | `U/discover/UseAFlatDiscover.kt`: Image calls around lines 250/364 | Bundled onboard artwork is used in listing-related imagery | A photo count/upload does not prove actual listing imagery. P06 |
| F03 | `DiscoverFilters.matchesRoomType`, `DiscoverRanking`, `Compatibility` | Private/shared inferred from bedsAvailable | Explicit room selection can disagree with search and match reasons. P06 |
| F04 | `DiscoverBoardScreen` request onSend; `FlatViewModel.sendConnectionRequest` | Success name is set immediately after asynchronous dispatch; no success callback | Failed writes can appear sent. P08 |
| F05 | `CreateDiscoveryPostScreen` | Draft fields use remember; only toolbar back opens discard dialog; inner Cancel bypasses it; no draft BackHandler | Process recreation, system back, tab switching and cancel are not proven safe. P02/P07 |
| F06 | `FlatViewModel.publishVacancy` | Random upload UUIDs each attempt, sequential putFile, appended photo URLs, raw exception messages, no progress data | Retry duplicates/orphan files; cover cannot precede existing images; misleading generic progress. P07 |
| F07 | Photo step in `CreateDiscoveryPostScreen` | TakePicturePreview, filename text rather than image thumbnails; picker replaces local list; controls only address new selections | Camera gives preview resolution; saved photos cannot be fully managed. P07 |
| F08 | `FlatViewModel.bindFlat`, `bindDiscoverySocial` | Listener failures emit empty lists/sets; old household arrays are not synchronously cleared in bindFlat | Failure can look empty and prior-flat content can remain while loading. P05/P08 |
| F09 | `IntentChooserScreen` | Goal starts first now, but find branches collect budget/city/room then forward only mode; completion still says “Profile set up” | Inputs are not applied; confirmation overstates saved work. P05 |
| F10 | `MyPostsScreen` | Empty state says create from + without action; one onEdit callback for both types; Close on looking maps to pause | Incomplete contextual entry and ambiguous editing/lifecycle. P07/P08 |
| F11 | `Theme.kt.applyHq` | Only part of Material color scheme overridden | Stock components may inherit default unrelated colors. P01 |
| F12 | `res/values/styles.xml`, `colors.xml` | Window background near-black; launcher background coral | Conflicts with white launch requirement. P04 |
| F13 | `apps/web/app/dashboard/layout.tsx`, `apps/web/app/globals.css` | Web still has its own menu/global creation and coral/dark token overrides | Android fixes are not evidence of web parity. P09 |
| F14 | `HqMotion.hqReduceMotion` | Settings read is remembered once for view; HqFadeUp has delayed entrance | Runtime preference change and reduced-motion exits need verification. P04 |
| F15 | `backend/firebase/firestore.rules` flats read rule | Any signed-in user can read flat documents | Hiding exact address in UI is not backend privacy if sensitive fields coexist. Inspect actual schema; separate authorized public/private projections before claiming protection. P00/P10 |

Anchors are symbols first; line numbers are advisory and drift. Reopen these sources before implementing. Do not overwrite newer fixes blindly.

## Decisions and conflict resolution

| Decision | Status | Instruction |
|---|---|---|
| Teal UI palette | Explicitly approved by user | Exact values in document 02 |
| Four vs six roots | Conflict; four retained pending clarification | No nav expansion without explicit choice |
| New design vs correction | Explicit correction pass | Reuse Hq and screen ownership |
| Gradient logo vs no gradients | Asset exception | Preserve supplied art; add no UI gradients |
| Light vs historical dark onboarding | New unified palette supersedes historical exception | Route-based violet theme removed; do not invent dark theme colors |
| Private/shared vs bed count | Verified schema mismatch | Read explicit field; legacy unknown stays unknown; do not infer a new truth |
| Required photo minimum | No documented mandatory minimum | Optional photos; recommend three but do not block publishing solely for zero |
| More vacancy fields | Already in working tree, not all integrated | Inventory readers/writers; keep compatible optional fields; no further schema invention |
| New-room owner without flat | Unsupported direct listing flow | Existing create-flat then admin vacancy path; independent listing entity remains PROPOSED |
| Seeker intent field coverage | Must reconcile current SeekerProfile | Do not repurpose an unrelated field; propose additive contract only if absent |
| Maps | Deferred | Locality search remains complete without invented distances |
| Surface elevated/status pale fills | Palette lacks exact values | Derived roles proposed in document 02; never let each screen invent them |

## Evidence limits

No current Android screenshot/device session was run in this planning pass. No auth, rules deployment, live upload, cross-device publication, finance concurrency, TalkBack or iOS build was performed. The snapshot inventories 232 files; this is not a claim of reviewing all lines. No environment credentials were inspected or copied. Existing build successes remain historical. The executor must close runtime gaps before declaring release readiness.
