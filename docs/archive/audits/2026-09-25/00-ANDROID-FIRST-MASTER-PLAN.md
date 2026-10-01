# Habitiq — Android-first product and implementation blueprint

Updated 25 September 2026. Read this first. This consolidates the owner's requirements and the accompanying audit into an execution contract. Android is the primary implementation currently present in the repository. Responsive desktop/mobile web and the planned native iOS app must express the same product while retaining appropriate platform behavior. See [the iOS native handoff](07-IOS-NATIVE-HANDOFF.md).

**Status:** architecture and implementation plan, not a release certificate. Existing behavior, source findings and proposed improvements are distinguished below. Full Android runtime validation is still outstanding.

## 1. Product outcome

People should be able to find a shared home or flatmate, connect safely, and run their household fairly through tasks and expenses. The product succeeds when people finish these jobs without guessing where to go, losing their progress or misunderstanding a balance. Attractive screens alone do not establish success.

| User situation | Job to finish | Observable completion |
| --- | --- | --- |
| Looking for a home | Find a relevant vacancy and request contact | Relevant detail viewed and a persisted request with visible status |
| Looking for a flatmate | Find a person or publish an actual vacancy | Correct post published or connection request sent |
| Person advertising their search | Publish and maintain a looking profile | Saved public preview agrees with stored fields and visibility |
| New household admin | Create a flat and invite others | One flat exists, admin reaches Home, invite can be used |
| Invited member | Join the correct flat | Membership confirmed, or a clearly explained pending request |
| Household member | Know and complete their responsibility | Completion saved and the correct next assignment displayed |
| Member unavailable | Arrange supported away/swap behavior | Persisted status and explained impact on assignments |
| Person paying a shared cost | Record cost and understand shares | Expense and correct balances visible to permitted members |
| Monthly bill payer/collector | Generate, track and close obligations | Correct bill state and traceable history without duplicate debt |
| Account owner | Manage identity, privacy and membership | Edits persist and their public/private visibility is clear |

A Discovery connection is not a booking, payment, or household membership. Joining remains a separate invitation/approval flow.

## 2. Confirmed architecture and evidence

Current source checked for this consolidation:

- `android/app/src/main/kotlin/habitiq/app/ui/AppShell.kt`: Home, Discover, Manage, Profile with a central Add action already exist.
- `android/app/src/main/kotlin/habitiq/app/ui/ManageFlatHub.kt`: Tasks and Expenses are existing child areas; Monthly Bills can be reached through the same owner from hub or Expenses. Flat settings is explicitly separate.
- `android/app/src/main/kotlin/habitiq/app/discover/DiscoverDomain.kt`: separate find-flat/find-flatmate intents, vacancy/looking post types, connection states and qualitative trust vocabulary.
- `android/app/src/main/kotlin/habitiq/app/discover/DiscoverFlags.kt`: two-sided discovery and connections enabled in source; distance intelligence disabled for Phase 2.
- `project_1/DISCOVERY_WORKING_SPEC.md`: approximate location, request acceptance before conversation, no automatic membership, and no invented matching/trust percentage. This is intended behavior, not evidence that every runtime path passes.

The broader source findings and web observations remain in document 01. Historical product documents describe intent; they do not override the owner's current direction or prove feature completeness. Preserve newer implemented features when an older document calls them future work.

## 3. Information architecture

```text
Website → Rooms / Flatmates / Run your flat → relevant detail or sign-in intent
Account resolution → retained intent / setup / pending membership / Home

Home: personal next actions + household overview
Discover
  Find a home → vacancy results → vacancy detail → request connection
  Find a flatmate → person results → person detail → request connection
  Requests / Conversations → accepted conversation → report / block
  My posts → create / preview / edit / pause / resume / close
Manage
  Tasks → My tasks / All tasks → task detail / rotation / swaps / away
  Expenses → balances / daily expenses / settlements / monthly bills / history
    Monthly bills → amount / shares / collection / period history
    Review and close month: admin-only
Profile
  Account → personal identity / preferences / account lifecycle
  Discovery profile → public identity / visibility and consent
  My flat → members / invites / switch flat / admin flat settings
Add: permitted actions, not a fifth destination
```

Use one owner per function. Shortcuts to Bills, Tasks or Requests must lead to that owner rather than duplicate implementations. Preserve existing route aliases and deep links during migration.

### Home is broader than task management

For an active household member, show selected flat, next responsibility, clearly scoped financial position, actionable requests, upcoming duties/bills and a short activity section. Do not fill Home with the entire Tasks screen. For someone still looking for housing, propose a discovery-oriented Home with saved context, their post and requests; this depends on resolving the no-flat policy below. Pending applicants see their actual request state, not a fictitious household dashboard.

### Desktop adaptation

Keep destination names and feature ownership identical. Desktop uses a sidebar, bounded content and a two-column Home where useful; phone uses bottom navigation and stacked priorities. Keyboard navigation, visible focus, dialog dismissal and readable tables are required on desktop. Android uses system back, safe areas, keyboard-aware forms and platform-appropriate sheets. Do not simply stretch a phone screen or reproduce desktop sidebars on phones.

## 4. Complete flow contracts

Each row describes required completion, including recovery. Existing features must be checked against the contract rather than rebuilt from their name.

| ID | Journey | Required path and recovery | Acceptance evidence |
| --- | --- | --- | --- |
| A01 | Sign in/signup | Entry intent → labelled form → submit → account resolution → correct destination. Preserve non-secret inputs on failure; support recovery and Google cancellation. | Fresh/returning account; valid/invalid credentials; cancelled provider; failure/retry |
| A02 | Create flat | Minimum supported details → create once → invite or skip → Home. Distinguish initial setup from explicit second flat. | Repeated tap and restart do not duplicate the flat |
| A03 | Join | Invite/code → preview → join/request → joined/pending. Retain code through login and show rejected/invalid/already-member states. | Admin approval and member receipt checked across sessions |
| A04 | Switch flat | Choose permitted flat → clear stale content → subscribe/load → new context. Recheck permission at mutation time. | No former-flat content or writes after switch/removal |
| D01 | Find home | Select mode → city/locality and supported filters → results → detail → request → visible pending state. Preserve filters/back position. | Relevant, empty, loading and failed searches; request persists |
| D02 | Find flatmate | Select people mode → supported preferences → person detail → request. Keep people fields distinct from room fields. | No booking controls, room ratings or invented profile attributes |
| D03 | Publish vacancy | Eligible admin → supported vacancy fields → preview/public visibility → publish → My posts. | Stored values match detail; permissions checked; duplicate submit safe |
| D04 | Publish looking post | Eligible account → supported search/profile fields → preview → publish → edit/pause/resume/close. | Public/private fields respected; updates visible after restart |
| D05 | Connection request | Request → pending → recipient accepts/declines → accepted contact. Repeated requests and blocked users handled explicitly. | Sender cannot accept their own request; unauthorized transitions rejected |
| D06 | Conversation | Accepted connection → authorized message query → send → persisted message. Failure must not look like an empty conversation. | Two test accounts; reconnect; duplicate/retry; unread behavior only if supported |
| D07 | Safety | Detail/chat → report or block → confirmation → appropriate access/result change. | Block checked by data permissions; report does not invent a trust penalty |
| T01 | Complete task | My tasks → detail or direct complete → saving → persisted result/next assignee. | Correct recurring, one-time and group-task behavior using existing rules |
| T02 | Swap | Assigned task → eligible person → request → accepted/declined/cancelled where supported → assignment refreshed. | Two-member flow; stale/duplicate request and authorization checks |
| T03 | Away | Dates/return input → effect explanation → save → status → return. | Already-overdue duties and all-members-away follow existing rotation contract |
| T04 | Admin task management | Create/edit recurrence and members → preview consequence → save; override/delete secondary. | Role checks, invalid recurrence, removed member, interrupted save |
| E01 | Daily expense | Description → amount/currency → payer/date/participants → split preview → save → balances/history. | Equal/custom split, rounding, invalid values, duplicate tap, edited/deleted member |
| E02 | Settlement | Person → currency/direction → amount → record → confirmation/history. | Partial payment and permitted overpayment policy; never implies bank transfer |
| E03 | Monthly bill | Bill setup → due instance → variable amount/payer if needed → generate shares → collection → history. | No repeated instance; payer/collector distinction; skipped and late cases |
| E04 | Month close | Admin review → unresolved amounts/carry forward explanation → confirmation → closed history. | No lost/duplicated debt; repeated close safe; closed edits obey policy |
| P01 | Identity/preferences | Account or public profile → edit → validation → save → correct visibility. | Persistence, large text, avatar failure, no fabricated required fields |
| P02 | Membership/account lifecycle | Leave/remove/transfer/delete → consequences → valid ownership handling → persisted outcome → correct destination. | Multi-flat membership, last admin and partial backend failure explicitly tested |

## 5. Discovery without Google Maps

Current-release discovery must work using supported city and locality fields, text filters and honest location descriptions. Show price with its period/currency and only available preference data. Unknown is not the same as a mismatch. Keep filter state when opening and returning from a detail.

No exact-address exposure, travel-time estimates, fake kilometer labels, map placeholders that look broken or permission prompts for unused location. A zero-results screen offers clear-filter/change-area actions; service failure offers Retry and preserves criteria.

Phase 2 may add Maps/Places and distance features. Keep provider-specific work behind a location service boundary and feature flag. Future work needs consent, approximate-location handling, key restrictions, quota/failure behavior and a usable text fallback. Do not add unsupported location schema or request API credentials during this design pass. Maps availability is not a dependency for completing the current release's supported journeys.

## 6. State and permission model

Represent authentication, profile loading, active household membership and discovery eligibility separately. A fetch error must not become 'no flat'. Resolve public/legal routes independently of membership. Validate internal return destinations. Preserve form progress where safe across transient interruption; never retain passwords as drafts.

| Actor | Allowed capability baseline |
| --- | --- |
| Signed-out visitor | Public information and supported browsing; account required for protected action |
| Signed-in seeker without a flat | Proposed looking profile and connections; final eligibility must reconcile onboarding policy |
| Pending join applicant | Request status and safe exit; no member data until approved |
| Active member | Own duties, permitted expenses, household visibility, own profile and supported discovery |
| Admin | Member management, authorized task/bill/month controls and household vacancy publishing |
| Removed/blocked participant | Access revoked at data layer; safe explanation and valid remaining destinations |

UI hiding is not permission enforcement. Backend rules and transaction/query behavior must match each contract. Do not introduce a new generic global store to hide contradictions between existing owners.

## 7. Data and implementation boundaries

Keep UI → screen state/ViewModel → existing repository/store → persisted source of truth. Web and Android need equivalent business outcomes, not shared UI code. Shared contract fixtures should capture money calculations, currency separation, exclusions, rotation rules and connection transitions.

Existing owners include `DiscoveryRepository`, Android auth/flat ViewModels, `MessagingRepository`, Android settlement helpers and web `lib/expenseUtils.ts`. Reopen current source before each packet. Treat source discrepancies as work to verify; a renamed screen cannot repair them.

Mutations need visible saving, confirmed success and recoverable failure. Prevent repeated tap duplicates and address backend idempotency where required. Unsubscribe previous-household listeners. Do not declare success from an optimistic display alone. Model partial cleanup failures explicitly for account deletion.

## 8. Forms, visual system and motion

Use the original Habitiq direction in document 06 across website, login and application. Retain the current brand foundation while changing distinctive reference-site compositions. Design complete screen states before styling isolated cards.

Forms require persistent labels, appropriate keyboards/autofill, readable errors, valid defaults and clear submit outcomes. Progressive disclosure should hide optional complexity, not essential amounts, recipients or consequences. A form's required fields must map to supported persistence.

Motion supports context and feedback: brief destination/state transitions, sheet entry/exit and restrained loading. Avoid decorative looping scenes on daily-use Home. Preserve reduced-motion behavior, stable layout and interruptible controls. Use the implemented loader as a starting point, not proof that motion everywhere is audited.

Before broad coding, produce phone and desktop reference screens for website/auth/Home/Discover detail/Manage/Tasks/Expenses/Profile plus Android-specific sheets and error states. Document 04 supplies initial measurements; these are proposed targets until rendered and inspected.

## 9. Usability validation: can people actually finish?

Source review establishes structure and potential faults. It does not establish that real users find a flow easy. Run these observed tasks with representative seekers, members and admins on Android; repeat critical navigation/form tasks on desktop.

1. Find a vacancy within a stated budget/locality and explain how to contact the household.
2. Find a potential flatmate, send a request and distinguish pending from accepted.
3. Publish a looking post, identify what others see and pause it.
4. Join a household from an invite and explain a pending approval.
5. Find today's duty, complete it and identify who is next.
6. Request a swap or mark an absence and explain its effect.
7. Add a shared expense and explain who owes whom.
8. Find the current monthly bill, inspect its shares and record the supported collection action.
9. Switch households and locate the correct members/settings.

Record unassisted completion, critical errors, backtracking, time on task and the person's explanation of the outcome. Time is diagnostic; do not invent a universal speed target. Proposed acceptance: all critical scripted paths pass; no observed critical misunderstanding of money, membership or public visibility remains unresolved. Report participant count and limitations; a small usability round is not statistical proof.

## 10. Ordered implementation and delivery gates

| Stage | Deliverable | Exit evidence |
| --- | --- | --- |
| 0 | Isolated mock/test setup and current capability inventory | Reproducible entry for member/admin; no live customer data in tests |
| 1 | Account, intent and membership routing | A01–A04 pass, including failure/restart cases |
| 2 | Approved original visual system and complete shell/Home designs | Cohesive reference screens, feature destination map, phone/desktop review |
| 3 | Android shell, Home and Manage improvements | Existing tasks/expenses reachable; role and system-back checks |
| 4 | Tasks and financial completion paths | T01–T04 and E01–E04; shared calculation fixtures and concurrency checks |
| 5 | Discovery profiles/posts/connections/chat | D01–D07, truthful data, permission/query checks; no Maps dependency |
| 6 | Profile/lifecycle and web continuity | P01–P02; responsive navigation, retained links and consistent identity |
| 7 | Integrated usability, accessibility and release QA | Device evidence, fixes retested, signed Android build/install and release checklist |

Stages describe integration order. Resolve the verified high-risk financial and message-rule findings before relying on affected features. Use implementation packets 0–8 in document 03 for narrower execution; do not give a cheaper model a vague 'redesign everything' instruction.

Each task handoff must include: user outcome, current owner/files, evidence, exact change, preserved behavior, allowed roles, loading/empty/error states, validation, phone/desktop differences, acceptance cases and proof returned. Stop and report when persistence, permissions or a required business decision contradict the proposed UI.

## 11. What 'ready to use' requires

- No critical blocked completion paths in the matrix above.
- Correct balances and task assignment behavior, with backend permissions verified using controlled accounts/emulators as appropriate.
- Android build/install, cold start, process death, background/resume, system back, keyboard, denied permissions, connectivity loss and recovery tested on supported devices.
- TalkBack, large text, touch targets, contrast and reduced motion inspected. Web keyboard/focus and mobile layout checked.
- Current signed-in and signed-out screens captured at agreed dimensions; no obsolete theme reappears after login.
- A real-content or explicitly labelled demonstration policy for Discovery; no fabricated reviews, distances or verification claims.
- Release notes, configuration requirements and rollback steps documented. Deployment and store publication remain separate actions.

These are release gates, not checks already passed. Document 05 records what was actually verified so far; there was no connected Android device during the initial audit.

## 12. Remaining decisions and current limits

1. **No-flat discovery:** recommend permitting a seeker account without creating a fictitious household. The older onboarding requirement conflicts with this; resolve eligibility before implementing that branch. Other household work can proceed.
2. **Native onboarding theme:** propose reconciling it with the light/coral product through the reference screens; the current dark treatment is an intentional source exception.
3. **Desktop delivery:** responsive web/PWA is the current planned counterpart. A separate desktop executable is additional scope.
4. **Financial edge policies:** confirm unsupported overpayment, recipient confirmation and closed-period correction behavior against existing business rules rather than inventing answers during a visual task.

This master plan consolidates the architecture and execution scope. It does not claim all source lines, every screen state or Android runtime behavior has been examined. The next implementation must keep an evidence ledger, replace assumptions with verified behavior and close the stated release gates before calling the end product ready.
