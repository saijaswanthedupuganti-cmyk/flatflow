# Target architecture and user-flow contracts

Status: design proposal for the next implementation pass. This does not silently supersede accepted business rules.

## The product promise

Habitiq helps people find a compatible shared home and run it fairly. Finding a place and operating a household are related journeys, but they require different identities, permissions and success measures. A listing is not a membership, a connection is not a booking, and recording a settlement is not transferring money.

Use three product contexts:

1. **Public website:** explain the product, show supported discovery content, let people begin a clear task, provide help and policies.
2. **Personal account:** authenticate, maintain account identity, manage looking profile and connections if the approved no-flat policy allows them.
3. **Active household:** tasks, expenses, bills, members and household settings, always scoped to a selected flat.

Do not require someone looking for a room to invent a household unless the product deliberately keeps the August onboarding restriction. That policy decision is the only blocked branch below; household-management improvements do not need to wait for it.

## Navigation map

```text
Public website
  Rooms / Flatmates       -> real search, listing/person details
  Manage your flat        -> explanation -> login / create / join
  Sign in                 -> account resolution -> retained destination
  Help / Contact / Terms / Privacy

Authenticated application
  Home                    -> today's tasks, requests, balance summary, activity
  Manage                  -> Tasks | Expenses
    Tasks                 -> My tasks | All tasks -> details / swaps / away
    Expenses              -> balances | daily splits | monthly bills | history
      Monthly bills       -> upcoming / shares ready / paid / skipped
      Review & close      -> admin-only, secondary action
  Discover                -> Find a flat | Find a flatmate
    Detail                -> request connection -> inbox -> conversation
    My posts              -> create / edit / pause / close
  Profile                 -> account identity | my flat | discovery identity | preferences
    My flat               -> members | invite | switch flat | flat settings
    Flat settings         -> admin-only settings, approvals, membership lifecycle
  Add (action, not tab)    -> context- and role-aware action sheet
```

On phones, use four labelled destinations and the existing center Add action. Preserve native visual slot order if needed: Home, Discover, Add, Manage, Profile. On desktop, show the same destination names in a sidebar; persistent household switcher at the top, account/preferences at the bottom. A wider viewport must not remove the member's task entry.

### Existing routes to preserve while reorganizing

| Current web route | Target role | Migration behavior |
| --- | --- | --- |
| `/` | Public website | Stop unconditional redirect once real authenticated discovery is supported; retain “Open my flat.” |
| `/login` | Dedicated auth entry or documented compatibility alias | Currently returns null then relies on guard; replace deliberately, not by adding a second unrelated form. |
| `/join` | Compatibility invitation entry | Forward query/code to the same onboarding join flow. |
| `/onboarding` | First household setup / explicit add-flat mode | Preserve code, create/join intent, pending approval and safe back destination. |
| `/dashboard` | Home | Keep short personal summary and direct actions. |
| `/dashboard/tasks` | Manage → Tasks | Member default My tasks; admin All tasks available. |
| `/dashboard/expenses` | Manage → Expenses | Keep deep-link `add`/bill intent working; make primary balance scope explicit. |
| `/dashboard/manage-flat` | Flat settings | Rename user-facing heading, preserve old URL until aliases are tested. Do not reuse it as a general hub without migration. |
| `/dashboard/profile` | Personal profile | Separate account and Discovery edits. |
| `/dashboard/settings` | Preferences compatibility route | Delegate to one settings owner; preserve old bookmarks. |
| `/dashboard/members` and `/members/[uid]` | My flat → Members | Keep removal/transfer contextual and role checked. |
| `/dashboard/swaps` | Task requests compatibility view | Preserve deep links, but introduce swaps from relevant task/context. |
| `/dashboard/insights` | Secondary household insights | Calendar/stats below primary duties. |
| `/dashboard/analytics`, `/dashboard/calendar` | Existing aliases | Both already redirect to Insights; preserve. |
| `/dashboard/activity`, `/dashboard/about` | Secondary history/help | Reachable through household/help without primary-nav overload. |
| `/privacy`, `/terms` | Public information | Always readable independent of sign-in and household state. |

New web discovery routes are an implementation proposal; inspect available data and query permissions before choosing their exact path structure.

## Account state table

| Condition | Screen / action | Must not happen |
| --- | --- | --- |
| Auth unresolved | Loader; after a defined delay show recovery guidance | Infinite unexplained animation, fake percentages |
| Signed out, informational route | Requested public page | Forced account creation |
| Signed out, protected invite/contact/action | Sign in with retained internal intent | Lost code, lost listing, redirect to unrelated task |
| Signed in, profile loading | Loading account | Empty account flashed before read completes |
| Profile read fails | Retry with retained intent; optional sign out | Treating failure as `activeFlatId == null` |
| Confirmed no flat | Intent chooser or personal Discovery per policy decision | Blank household dashboard |
| Join request pending | Pending view with flat name, next step and safe exit | Membership assumed before approval |
| Active membership | Selected household | Previous household's data visible while switching |
| Active flat removed / kicked | Explain membership change; select remaining flat or setup | Silent reset, forbidden content left visible |
| Explicit Add another flat | Dedicated create/join intent | Initial-onboarding duplicate guard preventing legitimate addition |
| Multiple households | Persist selected permitted household; switcher | Stale listeners or cross-flat mutation |

Internal return destinations must be validated against allowed routes. Do not accept arbitrary external return URLs.

## Complete flow specifications

### Authentication and recovery

Entry -> sign in or create account -> validate -> busy state -> auth result -> profile resolution -> retained destination.

Email sign-in: persistent Email and Password labels, email keyboard/autofill, show/hide password, Forgot password, one primary submit. Signup collects only fields that actually persist (web nickname is supported; native current account flow may collect a different minimum). Do not add company/college/age to satisfy a visual reference.

Validation: required values, format, password requirement before submit; friendly field error connected to field; general service errors announced once. Preserve email after failure. Never log a password. Clear secret inputs when auth session is intentionally dismissed. Google cancellation returns to usable controls; redirect progress is distinct from ordinary form submission.

Forgot password: explain email destination, allow correction, submit once, generic confirmation, retry failure. Avoid confirming whether an address exists. Real reset delivery must be tested only with a designated test account.

### Create household

Manage my flat -> name/type -> optional location supported by schema -> create once -> success with invite code -> Share or Skip -> Home.

Keep location optional as specified. Explain why location is requested if it will support Discovery; never publish a vacancy automatically. Disable duplicate submission and enforce idempotency at the appropriate persistence layer. A profile-read failure blocks the existing-flat check rather than allowing creation. After process death, use the persisted household, not local navigation memory.

### Join household

Invite URL or manual code -> normalize -> lookup -> preview name, count, join policy -> join/request -> joined or pending.

Not found, full, already member, invalid format, offline and permission errors need different useful messages. An invitation is not accepted until the transaction succeeds. Approval requests must survive restart and update when accepted/rejected; cancellation needs a real supported backend operation before adding a Cancel request button.

### Everyday tasks

Manage -> My tasks -> due task -> Complete -> optimistic/busy state -> confirmed next assignment or recoverable failure. Keep overdue responsibility on the assigned person. If everybody is away, show Paused with reason. Show queue as current, next, later with away members clearly identified. Do not infer current assignee from queue index zero.

Task -> Can't do this? -> select eligible person -> request -> pending -> accepted/declined -> refreshed assignment. No extra full-screen module is required for an ordinary response. Admin override is a distinct privileged action with explicit consequence; a member's request is not an override.

Away -> start/end or supported return input -> explain skipped future turns -> save -> visible status -> return. Validate ordering and explain impact on already-overdue work. Preserve the documented rotation algorithm.

### Daily expenses

Expenses -> Add expense -> description, amount/currency, payer, date, participants -> equal split preview -> save -> updated balances/history. Advanced custom splits and note stay collapsed. A repeated tap must not duplicate the expense.

Error cases: zero/negative/non-finite amount, no participants, custom total mismatch, missing payer, deleted member, closed month, permission denied, offline/retry. Keep entered data on recoverable failure. For mixed currencies, show separate balances with currency labels; never sum rupees and dollars into one net total.

Settle -> person, direction, amount and currency -> record payment/receipt -> confirm -> balance updates. Say “Record payment” or “Mark received” rather than implying a bank transfer. Show existing recipient-confirmation limitations honestly. Validate partial payment and overpayment against agreed policy.

### Monthly bills

Expenses -> Monthly bills -> upcoming bill -> confirm variable amount/payer -> generate shares -> collection status -> paid/skipped/history. Bill payer and collector are different roles and must remain separate in UI and data.

Review & close month is admin-only and secondary: summary -> unresolved balances -> carry-forward explanation -> confirm -> closed period. No silent debt erasure, duplicated recurring instances or edits to closed history. Deep links should open the requested bill while retaining the selected household.

### Discovery and contact

Find a flat / Find a flatmate -> supported filters -> relevant card -> stable detail -> context-specific connection request -> sent -> inbox decision -> accepted conversation.

Use truthful availability, approximate location, currency, room type, compatibility from real fields and qualitative trust with explained basis. No fake numerical scores, travel times, reviews or employer/college badges. Unrated and New to Habitiq are neutral states. Showing household operations requires the approved public snapshot rather than member-only queries.

Decline, block, expired request, removed listing, own listing and already-connected states need explicit UI. Report is not an automatic public trust downgrade. Match/accept does not join a household; joining remains its own invite/approval flow.

### Profile and lifecycle

Profile -> account name/email -> Edit supported account fields. Discovery identity has a separate editor and visibility explanation. My flat contains invite, members, role, switcher and admin-only settings. Preferences expose only real persisted preferences. Keep destructive actions separated from routine editing.

Leave -> explain consequences -> transfer admin if required -> confirm -> cleanup -> next permitted household or setup. Delete account -> reauthenticate if needed -> resolve admin/multi-flat/debt/post/message retention policy -> confirmed cleanup -> auth deletion -> signed-out state. Failed cleanup must be recoverable, not reported as success.

## Role and state matrix

| Action | Signed out | Signed in / no flat | Member | Admin |
| --- | --- | --- | --- | --- |
| Public info | Yes | Yes | Yes | Yes |
| Browse Discovery | Per approved public-data policy | Product decision | Yes | Yes |
| Looking profile / connect | Authenticate | Product decision | Supported account action | Supported account action |
| Publish vacancy | No | No | No | Current household only |
| My tasks / complete | No | No | Assigned task | Assigned or documented override |
| Create/edit/delete task | No | No | No | Yes |
| Add expense | No | No | Yes | Yes |
| Edit/remove expense | No | No | Own, according to rules | According to rules |
| Bill configuration / close month | No | No | No | Yes |
| Collection status | No | No | Self or collector per rules | Per rules |
| Household settings / membership | No | No | Own leave + allowed invite | Admin controls |

UI gating improves clarity; backend authorization remains mandatory. Verify this matrix against checked-in and deployed rules rather than assuming documentation is current.
