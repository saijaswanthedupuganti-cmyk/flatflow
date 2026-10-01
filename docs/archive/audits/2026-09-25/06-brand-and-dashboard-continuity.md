# One Habitiq experience: original identity and dashboard continuity

Updated 25 September 2026 with the owner's additional direction. This document governs the next design pass alongside the verified audit. It is a design proposal, not a claim that the redesigned screens have already been implemented.

## The clarified brief

The recent Airbnb-inspired website establishes the desired level of clarity and polish. Signing in currently exposes a differently organized and styled household product. Keep the latest working capabilities and close that experience gap. At the same time, Habitiq must have its own recognizable composition and identity; merely changing a reference site's colors or rounding is insufficient differentiation.

Use references for general usability principles: readable hierarchy, clear choices, useful photography and predictable navigation. Do not reproduce another brand's distinctive page composition, wording, icons, illustrations or motion. This is a creative direction, not a legal clearance or a guarantee about copyright. Keep asset provenance and licenses in the implementation handoff.

## What stays, what changes

| Keep and strengthen | Redesign deliberately |
| --- | --- |
| Habitiq name and existing mark | Repeated marketplace layout patterns that make the site immediately resemble the reference |
| Warm light canvas, coral emphasis, readable typography | Page-local palettes, dark web auth modal and unrelated dashboard treatments |
| Real task rotation, swaps, away status and admin controls | Their hierarchy and placement; routine actions should not compete with management controls |
| Existing expenses, settlement records, recurring bills and month lifecycle | Presentation, labels and navigation; preserve financial rules and validate client parity separately |
| Implemented native Manage hub, account/profile and household switching | Inconsistent destination names and account-versus-household ownership |
| Supported discovery and connection capabilities | Unsupported booking language, invented trust metrics and sample cards presented as real inventory |

## Habitiq's own visual idea

Design around **a shared home that runs fairly**. The identifying experience should be a clear household overview, a useful next action and visible shared responsibilities. Accommodation browsing is one journey within that product.

- Use a compact Habitiq header with simple text destinations and a clear account action. Avoid making an oversized floating accommodation-search capsule the signature of every page.
- Give the website a task-led introduction: Find a home, Find a flatmate, Run your flat. Confirm the no-flat discovery policy before wiring these entry points; the visual choice must not silently change onboarding rules.
- Show a small, truthful preview of household life: a task assignment, a shared expense and an upcoming bill. Any sample content must be labelled. This gives the website a composition specific to Habitiq rather than a copied booking catalogue.
- Keep room and person cards distinct. Rooms prioritize price period, locality, availability and supported amenities; people prioritize their stated preferences and supported profile details. Do not carry review stars and booking urgency into surfaces without corresponding real data.
- Use the existing mark, calm surfaces, a consistent border/radius family and coral reserved for action. Develop original illustrations only if needed; do not add decorative assets simply to fill space.
- Use restrained motion that confirms progress and state changes. The implemented loader is the baseline: stable identity, small progress movement, no orbiting decoration.

The goal is targeted structural differentiation while retaining useful current work, not a total rebuild or a new feature list.

## Website → authentication → home

Carry the same mark, typography roles, semantic colors, controls and writing style through all three stages. Authentication should feel like a short step inside Habitiq. Use a light, clearly labelled form, a real recovery path and accessible modal or full-page behavior. Mobile keyboard and scrolling behavior take precedence over keeping every control above the fold.

After login, resolve account and household state before selecting a screen:

| Entry and confirmed state | Destination |
| --- | --- |
| General sign-in, active household | Home for the selected household |
| Protected task, bill or other internal link | Original permitted destination in its household |
| Invitation | Join preview or existing pending request, retaining its code |
| Discovery contact | Original person/listing and permitted next step, subject to the agreed discovery policy |
| No household | Appropriate create/join path; personal discovery only if approved |
| Account fetch failed | Retry state; never pretend the person is a new user |

The public website and authenticated Home serve different jobs, so they need related components and identity rather than identical layouts. Do not send an existing member back through setup simply to show a new design.

## Dashboard information architecture

Use the existing native direction as the common destination vocabulary: **Home, Discover, Manage, Profile**, plus **Add** as an action. Keep Discover honest about availability. Do not introduce a dead destination while the data path is unresolved.

On mobile, retain four labelled destinations around the central Add action. On desktop, use the same names in a compact sidebar and place Add as a labelled action. The header owns the household switcher and page context; Profile owns personal preferences. Do not add a notification bell unless real notifications, unread state and a destination exist.

### Home, in reading order

1. **Household context:** selected flat, concise greeting and switcher. Avoid the large scenic header; time-of-day wording must agree.
2. **Your next responsibility:** the most relevant due task with its real completion action. If there is no task, show a quiet positive state and the next scheduled duty when known.
3. **Your money:** clearly scoped amount to pay or receive, with currency and a link to the detailed explanation. Separate currencies; do not infer a combined balance.
4. **Needs your response:** swap requests, supported payment confirmations and admin join approvals, only when actionable. Show role-appropriate actions rather than empty counters.
5. **Coming up:** a short view of upcoming duties and bills, with links into their owners.
6. **Recent household activity:** a small secondary list; full history and Insights remain reachable below primary work.

On desktop, put responsibilities in the larger column and balances/requests in the smaller column. On phones, stack in the order above. Avoid duplicating entire Tasks and Expenses screens on Home. Counts and dates must come from existing authoritative state, not invented display fixtures.

### Manage

Tasks and Expenses are the two clear entry points. Tasks defaults to My tasks for members; admins can reach All tasks. Task details expose queue, recurrence, swaps and history. Admin edit, override and delete actions are secondary and explicit.

Expenses retains daily splits, settlements, monthly bills, history and admin review/close. Do not automatically open a bill-generation dialog when someone only wants to inspect their balances. Flat settings is a separate administrative destination, not another meaning of Manage.

### Profile

Separate personal account identity, discovery identity, household membership and preferences. Keep member management and invites under My flat with role checks. Avoid unsubstantiated reliability percentages. Preserve existing supported fields and explain profile visibility instead of inventing new required personal data.

### Add

Open a compact action sheet with permitted actions such as Add expense and supported task creation. Context can prioritize an action, but labels and outcomes remain stable. A member must not see an admin-only creation path as usable. Publishing a vacancy appears only when its actual permissions and persistence are supported.

## States are part of the design

Every main destination requires loading, empty, populated, recoverable failure and permission-denied designs. Household switching must clear stale household content. Pending membership is its own state. Long names, large amounts, multiple currencies, overdue duties and many requests must fit without truncating essential meaning.

Match web and native semantics rather than forcing identical pixels: native sheets, system back, keyboard behavior and accessibility should follow their platform. Reconcile the currently intentional dark native onboarding with the new continuous identity through a small proposed screen set before implementing a palette migration.

## Concrete execution order

1. Inventory existing feature entrances and role restrictions using the route map in document 02. Record where every current capability will move before removing navigation.
2. Produce a small reference set: public homepage, sign-in, member Home, admin Home and Manage, each in phone and desktop form where applicable. Include at least one empty/error state and a native onboarding comparison. Use Habitiq content and assets.
3. Review the set as one journey for brand continuity and originality. Compare composition, header/search treatment, cards, copy and motion against the reference; reject a result that differs only in color.
4. Establish shared tokens and core controls, then migrate the authenticated shell and Home. Keep business logic owned by its existing stores/repositories.
5. Migrate Tasks, Expenses and Profile one at a time. Preserve deep links and verify member/admin behavior after each change.
6. Bring the public page and auth into the same components, while resolving retained login intent through packet 1. Native uses its existing semantic component owners with equivalent approved roles.
7. Complete the verification matrix and capture matching viewport screenshots. Financial/query corrections retain their dedicated packets and checks; a visual migration cannot certify them.

## Acceptance for the next model

- The full sign-in journey visibly belongs to Habitiq, with no abrupt return to a legacy dashboard theme.
- The homepage has a distinct task-led composition and truthful content, not a recolored copy of the inspiration.
- Every existing supported feature has a documented reachable destination; no feature disappears for visual simplicity.
- Member and admin Home prioritize personal action; privileged tools remain permission checked.
- The same main destination names appear across web/mobile/native, with intentional platform adaptations documented.
- Phone and desktop screenshots show clear hierarchy, usable controls, long-content handling and no accidental horizontal overflow.
- The handoff distinguishes implemented behavior, proposed designs and still-unverified native/backend behavior.

Read this document with packets 1, 3 and 7. It refines their destination and visual brief; it does not authorize deployment or declare the broader redesign complete.
