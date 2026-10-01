# Screen, flow, state and persistence contracts

These contracts combine confirmed source findings with proposed fixes. They are not a claim that every branch works today. The screen ledger in P00 must include actual route/component names, source path, role, fixture, state, screenshot and check result. Do not satisfy a flow with isolated mock screens.

## Information architecture

| Root | Primary job | Secondary destinations | Contextual creation |
|---|---|---|---|
| Home | Understand current household and next actions; no-flat users see relevant discovery/setup choices | Household details, invites, members, attention items | Relevant create/join household action only for no-flat context |
| Discover | Find a place or find a person | Search/filter, results, details/profile, request, inbox/chat, own posts | Post vacancy / post yourself according to eligibility |
| Manage | Keep an existing household organized | Tasks; Expenses → Daily splits / Monthly bills → Collections/rotation as already supported | Add task within Tasks; add expense within Daily splits; add bill within Monthly bills |
| Profile | Manage identity/preferences/account | My posts, settings and existing account functions | Edit profile; manage existing posts |

Four roots are the working contract; the six-root attachment conflict remains explicitly pending. “Post” is an action, not an assumed fifth tab. “Messages” remains Discover's secondary destination. Existing web routes require mapping, not blind deletion. Main labels should be consistent across clients, while platform navigation need not be identical.

## Entry, authentication and onboarding

| Situation | Required behavior | Completion evidence |
|---|---|---|
| Fresh signed-out launch | White launch → auth/welcome without false progress; optional onboarding can be skipped | Cold-launch recording |
| Returning authenticated user | Resolve user and household; do not flash another user's data or force onboarding repeatedly | Session restore and account-switch test |
| Email/password form | Labelled fields, show/hide password, correct autofill/keyboard, inline validation, submit busy/error | Invalid credentials, offline and successful sign-in |
| Provider sign-in | Preserve currently implemented provider SDK behavior and cancellation | Cancel/return/expired session; no invented Apple/OTP flow |
| Profile save | Save confirmed before completion/navigation; retry retains values | Forced write failure then successful retry |
| Choose goal | Find place/person/create/join map to distinct existing supported actions | All four branches; back and resume |
| Search preferences | Pass through and retain entered preferences; persist only through agreed profile/preferences owner | Results reflect setup choices; relaunch behavior documented |
| Create household | Establish household/membership, then Manage; vacancy publishing remains separate | Actual household and membership read back |
| Join household | Validate invite; pending/accepted/rejected/full/expired shown as supported | No access before backend authorization |

Never claim email verified, profile complete or personalized matches just because the user reached the last screen. Do not gate harmless browsing on optional profile details unless an existing security/product requirement demands it. Any new authentication method requires a separate backend/configuration contract.

## Household management

- Tasks: preserve existing recurring/group/temporary task behavior, assignment/rotation, completion and permissions. References are UX guidance, not authority to change scheduling rules. Test overdue/today/upcoming boundaries in the household timezone, reassignment, removed members, no eligible assignee and repeated completion.
- Expenses: preserve payer/split/settlement semantics. Format INR using locale-aware decimal handling; never show `toInt()` as a replacement for amount formatting. Equal/custom splits must reconcile exactly under the existing money representation; specify rounding at the calculation boundary, not in a view. Use ₹1,200.50 and a three-person non-even split as fixtures.
- Monthly bills: remain inside Expenses. Preserve collections and payer rotation. Test due date boundaries, month/year rollover, recurrence edit, skipped/removed payer and partial collection. A payment recorded in the app is not proof of external money transfer.
- Membership: permission changes, removal and sign-out immediately invalidate inaccessible actions. A cached admin button cannot authorize writes. Invitations and joins must not expose household data prematurely.
- Resource states are independent: tasks can load while expenses fail. Never convert listener exceptions into `emptyList()` and then present “nothing due” or “all settled.” Display last successful data with an explicit stale/offline indicator if permitted, or show an error with retry.
- Every asynchronous request captures user/household identity and generation. Ignore stale callbacks after a switch, and do not copy updated data into whichever flat happens to be active when the callback completes.

## Discovery and social flows

| Flow | Sequence | Recovery and truthfulness |
|---|---|---|
| Find place | Entry → location/search → filters → results → details → request → confirmation → accepted chat | Preserve filters/scroll; explicit zero results; request success waits for backend |
| Find person | Entry → search/preferences → people → profile → request → confirmation → accepted chat | No fake matches, history or verification; blocked/declined state respected |
| Existing-flat vacancy | Eligible admin → type → details/photos/room/preferences → preview → publish → My listing → responses | Persistent draft, real photos, success after write/read-back; no independent property model silently added |
| Post yourself | Eligible user → preferences/profile form → preview → publish → My posts | Correct post type/ID on edit; all fields retained on failed save |
| Respond | Inbox → request details → accept/decline/block → permitted chat | Per-item pending/error; duplicate prevention; unrelated user denied |
| Chat | Accepted relationship → history → compose → send confirmation/error | Unsent text retained, duplicate guard, relationship revocation respected |
| Viewing discussion | Message suggesting date/time, if currently implemented | Label as proposed; no calendar/booking confirmation without backend support |

`ConnectionStatus` contains more values than rules necessarily allow. Enum presence alone does not authorize a transition. Current rules permit recipient transitions from pending to accepted/declined/blocked and accepted to blocked; recheck exact source before implementation. UI must derive actions from both role and persisted status. A connection cannot be created with spoofed sender identity or used to message an unrelated recipient.

## Posting state machine

Proposed client operation states:

`editing → validating → uploading → saving → confirmed`

Validation failure returns to the offending field. Upload failure retains draft and successful staged files where safe. Save failure retains uploaded references for reconciliation/retry. A confirmed record write advances to success; a local callback starting a coroutine does not. A cancellation/network timeout can have an unknown outcome: reconcile the stable operation/post ID before offering a fresh create.

Persisted product statuses remain the existing schema. Audit every repository/rules transition. Published → paused and paused → published are offered only when supported. End/close, remove, expire and match are different outcomes; do not map all inactive states to Resume. Current seeker representation derives status from `active` only: either expose honest pause/resume semantics or add explicit lifecycle support across readers/writers/rules before offering End. Never pretend it already distinguishes closed from paused.

### Validation contract

Proposals below constrain the implementation; reconcile stricter existing backend limits in P00 rather than weakening them.

| Field | Rule | Error placement |
|---|---|---|
| City/area | Required for a discoverable listing; canonical stored value plus display label; no silent change on back | Below field |
| Room type | Explicit supported enum, not inferred from bed count | Selector group |
| Rent | Positive finite amount, correct per-person/per-month unit; parse with existing monetary contract | Amount field |
| Deposit | Optional; when entered, finite and nonnegative | Deposit field |
| Available rooms/beds | Positive bounded value using existing domain limit; do not equate with occupancy | Numeric field |
| Budget minimum/maximum | Nonnegative, minimum ≤ maximum; missing stays missing, not arbitrary default | Both fields/group |
| Availability date | Valid date with defined timezone/date-only semantics; no opaque unparsed free text | Date field |
| Description | Preserve current 600-character vacancy limit unless actual backend differs; trim outer whitespace; accessible counter | Multiline field |
| Preferences/tags | Supported canonical values, deduplicated; derived tags reflect actual data | Group; unknown legacy values preserved safely |
| Photos | Maximum 8 is current UI policy; accepted image MIME, positive size and strictly below current 10 MiB rules limit; guard decode dimensions | Per item + summary |
| Messages | Enforce actual server limit (current message rules ≤2,000 characters); connection-intro limit inspected separately | Composer |

No minimum of three photos is imposed by the reference alone; the supplied flow image conflicts with the currently optional-photo implementation. Keep no-photo publication valid unless the owner explicitly changes that requirement. Do not claim an optional field is required in helper copy. Any practical upper monetary bound requires agreement with schema and business rules; do not invent it merely to fit a screenshot.

### Photo and draft contract

Draft identity: account UID + household ID where relevant + post type + post ID or stable draft ID. Draft includes step, entered values, existing remote references, ordered pending assets, cover identity, dirty state and operation ID. Store no auth credentials. On logout remove sensitive drafts and temporary assets according to documented retention; warn before user-requested discard only when changes exist.

An image item has a stable client ID, source reference, local staged path if needed, upload state/progress, remote storage path/URL after upload, and user order. Cover is an item identity, not an array index that shifts under removal. Removing a cover deterministically selects the next remaining image and updates preview. Existing photos and new images use one ordered list. Gallery selections append/dedupe up to capacity; cancellation preserves prior choices.

Use app-private staged copies or persisted URI permissions for durable access. Handle limited photo access, denied permission, unsupported HEIC/decoder cases and large images explicitly. Full-resolution camera uses a safe content URI. Do not use `TakePicturePreview` thumbnails as listing originals. Downsampling/compression policy must bound memory while retaining useful room detail; inspect actual images before choosing export dimensions. Metadata/location handling should be explicit; avoid publishing accidental GPS metadata.

Object upload and Firestore update are separate operations, not a transaction across services. Stable object paths/operation IDs make retry reconcilable. Keep old references until the new document update succeeds. Remove only unreferenced, owned objects; interrupted cleanup becomes a retryable maintenance item. Photo deletion must never delete another listing's shared asset. Do not print download tokens or private URLs in public logs.

### Schema compatibility and privacy

Current vacancy writer and private `VacancyData` contain `photoUrls`, `roomType`, deposit, availability, furnishing, amenities and other added preferences; current public `VacancyListing`/parser omit important new fields. P06 must close this reader/writer gap before P07 is considered complete. Update filter/ranking consumers and web types together; use nullable/unknown for absent legacy room type rather than a false private-room default.

Inventory every existing field before changing a map. Preserve unknown additive fields when editing from either client. Store canonical data independent of labels and format it at the view layer. For conflicts use a version/update timestamp precondition or transaction appropriate to the existing SDK and schema; report conflict and offer reload/reapply, not silent overwrite.

Current flat documents are broadly readable by signed-in users under the reviewed rules. “Approximate location only” is a presentation flag, not proof private fields are unreadable. P00 must inspect actual data shape. If sensitive exact address/private member fields share public records, design a public discovery projection plus authorized private document, migrate safely and update all consumers/rules/tests as a separately reviewed subpacket. Do not deploy a UI privacy promise before backend enforcement. Firebase rules are not field-redaction filters. [Firebase rules behavior](https://firebase.google.com/docs/rules/rules-behavior)

## Required screen ledger coverage

Include welcome/auth/reset/provider return; goal choice and each branch; no-flat Home; populated Home; Manage; task list/detail/create/edit/complete; expense list/detail/create/split/settlement/history; monthly bill list/detail/create/collections/rotation; members/invite; Discover entry/search/filter/place results/person results/empty/error; listing/profile details; each posting step and preview; upload/save/success/failure; My posts/lifecycle/response lists; request pending/error/success; inbox/chat; Profile/settings; every existing dialog, permission prompt and supported notification destination. Mark absent capability as NOT IMPLEMENTED, never as a passed hidden screen.
