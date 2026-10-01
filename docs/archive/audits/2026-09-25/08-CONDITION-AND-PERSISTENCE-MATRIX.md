# Habitiq condition and persistence matrix

Updated 25 September 2026. This is the acceptance ledger for “works in every condition.” It distinguishes implemented handling from verification still required. A row is not complete because a screen exists; completion requires persistence, permission enforcement, restart behavior and visible recovery.

## State vocabulary

Every asynchronous feature must distinguish: initial loading, populated, legitimate empty, offline/service failure, permission failure, mutation in progress, mutation rejected, success confirmed by the source of truth, and stale/removed access. Do not convert an error to an empty list. Do not leave a form before Firestore confirms a successful write.

## Identity and household

| Use case | Required conditions | Current checkpoint |
| --- | --- | --- |
| Email/Google sign in | invalid input, wrong credentials, cancelled provider, offline, profile-read failure, retained invite/action | Existing implementation; device/Firebase verification pending |
| Password reset | invalid/blank email, generic confirmation, offline retry | Existing UI path; live delivery pending |
| Create flat | duplicate tap, existing membership, interruption, optional location | Existing persistence; transaction/restart verification pending |
| Join flat | malformed/not-found/full/already member, automatic vs approval, duplicate request, rejection | Error copy unit tested; multi-account Firestore verification pending |
| Pending join | restart, approval/rejection while open, safe exit | Implemented state; device verification pending |
| Switch flat | clear stale content, permission revoked, listener failure, restart selection | Existing implementation; stale-listener tests pending |
| Leave/kick/transfer/delete | last admin, multiple flats, reassignment, partial cleanup failure | Existing paths; destructive controlled-account tests pending |

## Home, Tasks and Expenses

| Use case | Required conditions | Current checkpoint |
| --- | --- | --- |
| Home actions | task/expense/request/discovery shortcuts reach one feature owner | Implemented and compiled |
| Home data | no duties, overdue, many requests, long names, multiple currencies, failed listeners | Partial; runtime and currency presentation pending |
| Task completion | one-time/recurring/group, overdue carry, repeated tap, offline, removed assignee | Existing repositories; contract fixtures still required |
| Rotation/swap/away | requester/recipient roles, decline, stale request, all away, return, admin override | Existing flows; multi-user tests pending |
| Expense split | invalid amount, no participants, rounding, custom mismatch, repeated submit, deleted member | Existing form/repository; cross-client calculation parity pending |
| Settlement | payer/receiver direction, partial/overpayment policy, currency, repeated record | Existing feature; policy and concurrency tests pending |
| Monthly bills | duplicate month instance, variable amount, payer vs collector, skip/late, close/carry forward | Existing feature; transaction/idempotency checks pending |

## Discovery and messaging

| Use case | Required conditions | Current checkpoint |
| --- | --- | --- |
| Vacancy post | admin only, required city/area/rent/beds, preview, publish failure, restart visibility | Validation and wait-for-Firestore success implemented; emulator/device verification pending |
| Looking post | city/areas/budget validation, publish failure, edit/pause/resume/restart | Validation and wait-for-Firestore success implemented; verification pending |
| Browsing | loading/empty/error, preserved filters, blocked profiles, truthful approximate location | Existing states; device verification pending; Maps intentionally deferred |
| Connection request | self, duplicate, reverse-direction duplicate, blocked pair, offline, accepted/declined | Deterministic duplicate guard and blocked-pair rules implemented locally; rules tests pending |
| Connection transition | only recipient accepts/declines; participants may block; no arbitrary reopen | Local Firestore rule state machine implemented; not deployed |
| Conversation | accepted connection only, participant-only queries, send failure retains draft, restart order | Participant-scoped queries, connection-bound messages and retained failed draft implemented; emulator verification pending |
| Report/block | reporter ownership, immutable report, block affects future contact | Local persistence/rules present; end-to-end tests pending |
| Location | locality search without fake distance; denied/offline behavior when future Maps is enabled | Current release excludes Maps; Phase 2 contract documented |

## Persistence proof required before release

For each write feature, test with controlled accounts against the Firebase Emulator Suite or a dedicated non-production project:

1. Perform the mutation and wait for confirmed success.
2. Terminate and relaunch the app; verify the same state reloads.
3. Sign in as the affected second actor and verify permitted visibility/action.
4. Attempt the same operation as an unauthorized actor and expect permission denial.
5. Repeat the action rapidly and verify no duplicate document or financial/task effect.
6. Interrupt connectivity before and during the action; verify retained input and understandable retry.
7. Remove membership/block the actor while the screen is open; verify listeners and actions lose access safely.

Never run destructive fixtures against customer data. Rules checked into this repository are not proven deployed rules. Capture emulator command, test identities, rule version/hash and result evidence.

## Release closure

The Android app compiles after the latest Discovery hardening. The final unit run passed 26 tests across five suites with no failures, errors or skips, and the debug APK was rebuilt. A debug APK is not a production-ready release. Close the remaining rows with device, emulator, accessibility and multi-account evidence. iOS remains governed by document 07 and requires macOS/Xcode implementation and verification.
