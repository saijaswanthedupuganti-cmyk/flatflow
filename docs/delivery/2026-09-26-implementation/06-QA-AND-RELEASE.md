# QA matrix and release gate

All cases below are PLANNED / NOT RUN in this planning pass. Earlier build/APK reports are historical and do not satisfy this gate. A screen must be checked in its actual parent shell, with real state transitions, not only as a component preview.

## Evidence format

Each record: case ID; packet; source commit and dirty-tree fingerprint; candidate artifact SHA-256; fixture/account role; platform/API/device or browser/viewport; font/display scale; input/actions; expected result; observed result; PASS/FAIL/BLOCKED/NOT RUN; evidence path; remaining issue. Redact credentials, download tokens, real contact information and private conversation data.

Save runtime screenshots/videos under `evidence/runtime/`. Include full-screen unmodified captures for inset checks. Crop comparisons only as additional evidence. Use state names in filenames. A mockup/reference is a target, never execution evidence. Keep a separate issue log with severity, reproduction, owner packet and retest link.

## Environment matrix

| Dimension | Required coverage |
|---|---|
| Android version | Minimum supported API 26, representative pre-12, Android 12+ splash behavior, target API 36; consolidate devices where one covers multiple categories |
| Phone layout | Small ~360dp width, typical ~393dp, large ~430dp; short landscape; record actual dp and resolution |
| Tablet/window | ~800dp width and resizing/multiwindow where supported; bounded form widths, no stretched buttons |
| System UI | Gesture and three-button navigation; cutout; keyboard open/closed; back navigation |
| Accessibility | Font scale 1.0, 1.3 and 2.0; TalkBack; reduced/disabled animation; hardware keyboard where applicable |
| Network | Normal, slow, offline before action, lost connection mid-action, recovery; listener failure separate from no records |
| Identity | Signed out; no-flat user; admin; member; unrelated user; removed member; account/household switching |
| Web | Mobile 360/390px, tablet 768px, desktop 1280/1440px; supported browser engines, keyboard and 200% zoom |

Physical-device gaps must remain visible. Emulator screenshots cannot certify camera hardware, every OEM launcher, biometric behavior or iPhone support. iOS is a separately gated future phase.

## Required cases

| ID | Packet | Scenario and expected observable result |
|---|---|---|
| BASE-01 | P00 | Baseline build/tests recorded; pre-existing failures distinguished |
| VIS-01 | P01 | Exact palette mapped; no accidental coral/violet/default Material roles in reachable light UI |
| VIS-02 | P01/P03 | Normal small text meets contrast; disabled text is not essential copy; input boundary/focus discernible |
| VIS-03 | P02 | Every ledger screen clears status/navigation/IME insets once; no clipped last row or obscured primary action |
| VIS-04 | P03 | Small phone and 2× text: no clipped controls, overlapping labels, truncated error, horizontal body overflow |
| VIS-05 | P03 | Interactive elements have 48dp touch targets and correct roles/selection/disabled states; passive tags not focusable buttons |
| VIS-06 | P03 | Loading/empty/error/content screenshots at actual shell level; illustrations do not impersonate real photos |
| NAV-01 | P02 | Four roots preserve state; focused flow hides tabs; back returns to correct origin |
| NAV-02 | P02/P07 | Dirty form system/header back both prompt; clean back does not; keyboard/modal handled before parent exit |
| BRAND-01 | P04 | Exported supplied artwork unchanged, ratio preserved, no black/blank rendering, all icon masks inspected |
| BRAND-02 | P04 | Cold/warm/resume/deep-link startup has white continuity, no double splash or fixed artificial delay |
| MOT-01 | P04 | Reduced motion switched during app lifetime stops optional motion; operation remains understandable |
| MOT-02 | P04 | Rapid taps/back/background interrupt animations without stuck overlay, duplicate action or layout jump |
| AUTH-01 | P05 | Email/provider success, invalid input, cancellation, offline, expired session retain honest state |
| AUTH-02 | P05 | Profile-save failure remains on form with values; success persists and then navigates |
| AUTH-03 | P05 | All four goals route correctly; chosen discovery preferences actually affect destination; completion claims true |
| HOME-01 | P05 | No-flat/admin/member content and permitted actions correct; invite/join pending gives no unauthorized access |
| STATE-01 | P05 | Switch flat/account during slow read: old data never appears as new household data |
| STATE-02 | P05 | Switch during write: completion updates only original entity; controls and errors recover |
| STATE-03 | P05/P06 | Listener failure produces error/stale state, not empty/settled; Retry performs new work |
| TASK-01 | P05 | Existing recurring/group/temp creation, assignment, edit, completion and rotation pass regression |
| TASK-02 | P05 | Removed assignee, no eligible member, overdue boundary, duplicate completion handled by domain contract |
| MONEY-01 | P05 | ₹1,200.50 renders without truncation; equal/custom split total reconciles; invalid/negative amount rejected |
| MONEY-02 | P05 | Payer edit/removal, settlement retries, duplicate submission and unauthorized write preserve balances |
| BILL-01 | P05 | Month/year boundary, recurrence, collections and payer rotation retain existing correct behavior |
| DISC-01 | P06 | Search/filter/reset/sort reflects actual records and count; empty distinct from failed fetch |
| DISC-02 | P06 | Legacy missing roomType is unknown; bed count does not masquerade as private/shared type |
| DISC-03 | P06 | New persisted fields/photo cover render consistently in preview, list and detail after reload |
| DISC-04 | P06 | Missing/broken image uses honest fallback; actual photo count; no fabricated verification/history/score |
| POST-01 | P07 | Valid no-photo publication succeeds when policy permits; invalid field focuses useful error and retains draft |
| POST-02 | P07 | Gallery append/dedupe/max-8; cancel preserves prior; thumbnails/order/cover reflect existing and new images |
| POST-03 | P07 | Oversize/invalid image, permission denial and unsupported decoder handled without crash or lost form |
| POST-04 | P07 | Full-resolution camera where supported; process death restores readable staged assets and fields |
| POST-05 | P07 | Upload interrupted before/mid/after file completion; retry avoids duplicated files/posts and false success |
| POST-06 | P07 | Record-write failure after upload retains recoverable draft and safe cleanup path; no leaked reference deletion |
| POST-07 | P07 | Double publish and uncertain timeout reconcile stable operation; success only after confirmed write/read-back |
| POST-08 | P07 | Concurrent edit detects conflict; unknown additive fields preserved; account/flat-scoped draft isolation |
| LIFE-01 | P08 | Edit routes correct post type/ID; pause/resume/end labels match actual persisted transition |
| LIFE-02 | P08 | Closed/removed states never offer unsupported resume; empty My posts offers eligible creation action |
| REQ-01 | P08 | Request pending retains sheet; failure retains text; only confirmed success shows sent; duplicate prevented |
| REQ-02 | P08 | Accept/decline/block enforce role and state; per-row busy/error; late response/listing closure handled |
| CHAT-01 | P08 | Accepted participants send/read; unrelated/pending/blocked relationship denied as rules require |
| CHAT-02 | P08 | Failed send retains draft and supports retry without duplicate; proposed viewing not called a confirmed booking |
| SEC-01 | P00/P07/P08 | Rules emulator proves owner/member/unrelated/unauthenticated access for affected collections and storage |
| SEC-02 | P00/P06 | Exact/private information cannot be read through public discovery access, or release is blocked pending migration |
| WEB-01 | P09 | Build/lint and isolated existing E2E checks pass with environment/port proven to be test-only |
| WEB-02 | P09 | Landing/auth/dashboard share identity; existing route deep links preserved; mobile/desktop screenshots and keyboard pass |
| REL-01 | P10 | Installed candidate matches hash tested; final ledger complete, known gaps explicit, release notes accurate |

## Automated checks: meaningful scope

Use current Gradle wrapper/tasks, not assumed commands. From `apps/android`, inspect available variants, then run applicable `testDebugUnitTest`, `lintDebug`, and `assembleDebug`. Instrumented tests require an actual selected device/emulator. Record exact commands and outcomes. A lint baseline must not silently suppress new failures.

Add regression tests at actual boundaries: listing parser round-trip including legacy fields; filter semantics; lifecycle actions; async result ordering; currency formatting/calculation boundaries; upload/write reconciliation; permission/rules tests. Do not fill the suite with assertions that merely restate token constants while omitting failed async branches. Token assertions are small support checks, not product proof.

For web use repository scripts (`npm run lint`, `npm run build`, appropriate Playwright scripts) after reading installed Next guides. Current Playwright configuration can reuse localhost:3000 while fixture behavior depends on missing Firebase keys. Establish an isolated test server/port and explicit test environment first. Never run destructive seeded tests against whichever server already happens to be open. Do not expose `.env` values in logs.

Backend tests must cover denied as well as allowed operations: sender spoofing, unrelated flat update, non-admin photo write/delete, size/MIME failure, foreign connection ID and private-field exposure. Test rule changes with Firebase emulators before deployment. State clearly which configured test project/rule version was exercised without sharing secrets.

## Visual comparison procedure

1. Inventory current screenshots for each flow and state before editing.
2. Use supplied screenshots for hierarchy/flow intent; apply latest teal palette and preserved logo as superseding directions. Do not reproduce sample people, amounts, ratings, counts or fake phone chrome as real product data.
3. Capture deterministic synthetic fixtures at recorded viewport/device dimensions. Compare alignment, spacing, hierarchy, text wrapping, component dimensions, image crop, toolbar/tab active state and system insets. Inspect final rendered files, not only automated diff scores.
4. Record justified differences from reference (actual content, accessibility, native conventions, backend support). Fix unexplained differences rather than declaring subjective similarity sufficient.
5. Recheck affected screens after shared token/scaffold edits. Review motion in recordings and user actions in runtime; still images do not establish working behavior.

## Release criteria and severity

- Block release: incorrect access/privacy promise, lost/corrupted data, wrong household state, false success, broken auth/core posting/tasks/expenses, inaccessible primary action, or irreconcilable upload/write outcome.
- Block the affected feature: unsupported status transitions, broken photo readback, unusable small-screen/keyboard form, request/chat permission mismatch, or blocked required test without alternative evidence.
- Cosmetic issues can remain only with named scope, screenshots and an explicit release decision. Do not silently downgrade unreadable text or unreachable controls as cosmetic.
- Every required applicable case must be PASS, or have an explicit accepted exception; NOT RUN/BLOCKED is not PASS. Do not ask for blanket approval before producing concrete evidence and fixes already authorized.

Deliver a dated test APK, source identity, SHA-256, completed case ledger, screenshots/recordings, known limitations, and deployment/signing configuration still required. Signing, Play/App Store upload, backend deployment and live data migration are distinct actions, not implied by compiling the candidate. Do not label a debug APK as a published production application.
