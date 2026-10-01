# Visual direction and pixel acceptance specification

This is a proposed measurable design contract for the implementation pass. It does not claim that new approved Figma artboards already exist.

## Direction

Keep Habitiq calm, useful and recognizably about shared living. Use the current warm canvas and coral action color as the starting point. Following the owner's clarification, develop an original task-led homepage composition instead of preserving a recognizable copy of the marketplace reference. Images and content must honestly represent real inventory or a labelled demonstration. Carry the same identity through sign-in and dashboard. See [the updated brand and dashboard brief](06-brand-and-dashboard-continuity.md) for layout, navigation and migration requirements.

Current owners to reuse:

| Property | Current owner / values | Instruction |
| --- | --- | --- |
| Web canvas and text | `app/globals.css` `--background`, `--foreground` | Consume semantic variables, not page-local replacements. |
| Web primary | `--primary` #e4533d light / #ff6b55 dark | Keep active/navigation actions coherent; check text contrast before using white small text. |
| Native light system | `HqLightColors` -> `FigmaColors`; primary #E4533D, background #F6F3EF | Preserve semantic component owners. |
| Native onboarding | `HqDarkColors` -> `HabitiqBrand` | Intentional exception documented in source; no automatic palette migration. |
| Web typography | Inter / `--font-inter`; mono for codes | UI uses Inter; do not add a third font family. |
| Native typography | `HqType` | Map approved roles to existing sizes before adding tokens. |
| Native spacing/touch | `HqSpacing`, `HqTouchTarget` | Reuse; verify large text and keyboard behavior. |
| Logos | `public/habitiq-logo.svg`, `habitiq-icon-mark.png` | Actual mark is padded purple raster; loader now crops it in SVG and displays a white silhouette. Do not redesign logo geometry. |

The June master contains indigo defaults and outdated dark-only rules. Reconcile it after approval; never treat old comments as proof current coral is wrong.

## Proposed layout measurements

Numbers below are proposed targets, not measurements of every current screen.

| Surface | Phone | Tablet / desktop |
| --- | --- | --- |
| Main content | 16 px horizontal gutters; 16 px card gaps | 24–32 px gutters; centered content up to existing 1152 px dashboard width |
| Auth | Full-width within 16–24 px margins; scrollable with keyboard | 400–440 px form panel; visible title and close action |
| Cards | 16 px padding; 12–16 px radius | 20–24 px padding; same semantic radius |
| Input/action targets | 44 px minimum product target, prefer 48 px primary form controls | Same comfortable input height; compact secondary controls only with adequate hit area |
| Body/inputs | 16 px default; 14 px secondary; avoid essential 9–11 px copy | 14–16 px body; numeric data large enough to scan |
| Titles | 22–24 px primary; 18–20 px section | 24–28 px page; use hierarchy instead of excessive weight |
| Bottom nav | Four short labels; Add has accessible name | Same destinations in sidebar, Add as labelled button |
| Sheets | Header + scrollable body + reachable action area, safe-area inset | Centered dialog for forms, bounded height, independent content scrolling |

Use one spacing rhythm (4/8/12/16/24/32). Do not multiply tiny pills, gradients and shadows to create hierarchy. Prefer a border and readable spacing for ordinary cards. Color must not be the only status signal.

## Screen-specific targets

### Sign in and signup

- Place a modest brand mark above a clear title; the form is the main content.
- Persistent labels; supporting password requirement visible before submit.
- One primary submit; Google remains a clearly different method; a divider needs readable copy but should not dominate.
- Password recovery and mode switch are real, readable actions.
- Error text near its input; service error above submit; busy controls do not jump in width.
- At 390 × 844 and 320 × 568, every control must be reachable through normal scrolling with the keyboard open. “Everything above the fold” is not a requirement that justifies unreadably small inputs.
- Current historical screenshots: brand panel occupies roughly the upper 220 px of the centered card; fields are about 38 px tall; signup's lower content lies below the 720 px crop. These are observations of supplied images, not proof of clipping in the current app.

### Home

- Greeting, current household and role context; primary action is the user's next duty.
- Balance summary is concise and labels its scope. No discrepancy between net summary and explanatory rows.
- Due/overdue tasks use calm surface + small status emphasis. Avoid a full red card for routine overdue household work.
- Pending approvals/swaps appear when actionable. Empty or zero metrics should not occupy the same weight as a task.
- Rotation detail and history are secondary; simplify decorative night scenery and avoid a large greeting consuming the first screen.

### Manage / Tasks

- Hub: Tasks with own count/overdue count; Expenses with clearly scoped position.
- Task row hierarchy: task name -> due statement -> assignee -> completion action -> supporting status.
- Details reveal queue/recurrence/history. Admin Edit/Override/Delete sit behind overflow with explicit labels.
- Use consistent “My tasks”, “All tasks”, “I'm away”, “Request a swap.” Avoid mixing duties/tasks/roster/organisation without purpose.

### Expenses

- Position first: You owe / You're owed / All settled, with currency and scope.
- Then daily splits and monthly bills. An overdue bill does not force a form open.
- Person rows give direction and amount. “Record payment” and “Mark received” clarify different actions.
- Add form: description/amount/payer/participants first; supported category and date remain easy to edit; advanced split controls progressive.
- Outstanding, monthly totals and historical transactions have distinct labels. A month header must not imply every number is month-filtered.
- Admin month close sits in a secondary menu, with review and recovery contract defined before visual styling.

### Discover

- Different room vs person card structures; both use real structured data.
- Room: image/fallback, approximate area, rent/currency, actual availability, short household context.
- Person: permitted identity, looking area, budget/currency, real compatibility traits; avoid borrowing hotel star ratings.
- Detail primary action is Connect/request, not Reserve unless a genuine booking process is implemented.
- State labels: New to Habitiq, Unrated, Habitiq member only with their actual basis. Do not treat a block/report count as a trust score.
- Keep filter chips readable and restorable; unsupported date/occupancy filters must not simulate working search.

### Profile

- Account identity and Edit profile first.
- My flat, Discovery identity, Preferences, Sign out as distinct groups.
- Role belongs to current household, not the person's global identity.
- Reliability, household statistics and admin tools should not overwhelm personal controls.
- Danger actions are secondary, clearly explained and confirmable; do not hide the consequence behind generic “Continue.”

## Motion specification

Purpose is feedback or state indication, not constant decoration.

| Interaction | Proposal | Reduced motion |
| --- | --- | --- |
| Loading | Existing new 2 s linear indeterminate bar; stable logo/text | 2 s opacity cycle; no translation |
| Press | 100–150 ms color/opacity feedback; minimal or no scale | Color/opacity only |
| Menu/tab state | 150–200 ms, no layout jump | Immediate state or short fade |
| Sheet entry | 200–250 ms transform/opacity with existing approved ease | Fade only |
| Form error | Appear without shifting unrelated fields dramatically | Immediate/fade; no shake |
| Task completion | Brief state acknowledgement, preserve focus and next item | Text/icon confirmation |
| Add button | Stable action affordance | Same; no permanent pulse |

Use CSS for predetermined loading motion, existing Motion only where already appropriate for interruptible interaction. No animation delay before enabling a usable screen. Never display invented progress percentages. Native animations must respect system animator settings and retain task completion clarity.

## Pixel comparison contract

Before implementation, create/approve one target per surface and key state, at minimum:

- Sign-in, signup, reset, error and loading.
- New-user intent, create, join preview and pending approval.
- Member Home, admin Home, Manage hub, task detail, away and swap sheet.
- Expense overview, equal/custom form, partial settlement, monthly bill and close review.
- Discovery room/person results, empty, detail, request, inbox/chat and blocked state.
- Profile, edit account, household switcher, leave/transfer and deletion failure.

Reference widths: 390 px phone and 1280 px desktop. Robustness checks: 320, 360, 768, 1024 and 1440 px; short height, landscape and large text. Approved designs must specify exact typography, spacing, asset crop and data fixture. Do not claim exact matching against an unapproved imagined target.

Capture same fixture, viewport, zoom, fonts and animation state for comparisons. Review overlay/diff and the full screen visually. Accept no unintended overflow, clipped text, overlapping actions, substituted copy or missing states. Exclude nondeterministic date/text/animation areas only if documented; never hide layout defects behind broad screenshot masks.

## Asset and content handling

Keep source photos and avatar permissions attributable. External placeholder photography must remain labelled sample content. Provide neutral image-failure and no-photo states. No image generation is needed to correct routing or form architecture. New illustrations should come after the content and screen hierarchy are approved, not serve as a substitute for working flows.
