# Habitiq Design System

**Version 2.0.0 · 1 October 2026**  
**Product:** Habitiq — shared living, thoughtfully organised.  
**Platforms:** Android first; iOS adaptation defined; responsive native layouts supported.  
**Owners:** Sai Jaswanth — product and design; Bhanu Kalyan — engineering.  
**Status:** Implementation specification. Replaces the visual foundations of `MASTER.md` v1.1. It does not establish that the proposed UI has shipped or passed device testing.

**Navigation:** [Brand](#2-brand-foundations) · [Product contract](#3-product-contract-preserve-before-polishing) · [Tokens](#4-token-architecture-and-implementation-contract) · [Color](#5-color-architecture) · [Typography](#6-typography) · [Layout](#7-space-layout-and-shape) · [Components](#10-component-contracts) · [Accessibility](#14-accessibility-requirements) · [Platform adapters](#15-platform-adapters) · [Governance](#16-governance-and-migration) · [QA](#17-qa-and-release-acceptance)

## 1. Purpose and authority

Habitiq helps people share a home with less coordination: understand whose turn it is, handle shared expenses, stay informed, and discover potential flatmates with appropriate privacy. The product should feel composed, welcoming, and dependable during an ordinary day in a shared flat.

This document defines one visual and interaction system for that experience. “Premium” means clear priorities, legible information, accurate feedback, consistent details, and respect for the person using the app. It is not a pricing tier or a promise of new features.

### 1.1 Source register

All six supplied Markdown documents, the supplied coral/teal reference, and the subsequently supplied 60–30–10 UI-principles image were reviewed. The specifications report an August 2026 APK audit; their implementation claims have not been independently checked against a repository in this work.

| Source | Authority in this system |
|---|---|
| `MASTER.md`, v1.1, June 2026 | Prior brand and UI baseline. Its indigo/violet palette, forced-dark entry screens, opacity-based text, web-first sizing, and glow treatments are superseded here. |
| `HABITIQ_ONBOARDING_MASTER.md`, audit 23 August 2026 | Authentication, three intents, create/join, approval waiting, restart recovery, schema and membership constraints. |
| `HABITIQ_MANAGE_FLAT_MASTER.md`, audit 23 August 2026 | Home/Manage separation; Tasks and Expenses behavior; roles; rotation, swaps, away, bills, settlements, month close. |
| `HABITIQ_PROFILE_MASTER.md`, audit 23 August 2026 | Account/Discovery identity separation, initial avatars, current settings, no-flat and multi-flat states. |
| `DISCOVERY_WORKING_SPEC.md`, 23 August 2026 | Current two-sided Discovery scope, privacy, post types, qualitative trust, connection lifecycle, flags. |
| `Habitiq_5_Year_Strategic_Plan.md`, v0.3.0-strategy.1 | Shared-living purpose, retention priorities, India-first context, future expansion and regression scenarios. Roadmap features are not current capabilities. |
| `WhatsApp Image 2026-10-01 at 9.25.43 PM.jpeg` | Visual direction: coral `#FF6B5A` and teal `#14B8A6`, explicitly printed in the image. It supplies a palette reference, not an app layout, logo, typeface, or licensed illustration asset. |
| `WhatsApp Image 2026-10-01 at 9.39.42 PM.jpeg` | User-requested UI principle: 60% neutral base, 30% supporting treatment, 10% accent. Applied as the screen-composition default in §5.5, together with the practical hierarchy and layout rules in §2.5. The example's grayscale colors and banking content are not Habitiq requirements. |

### 1.2 Precedence and resolved conflicts

1. Preserve repository-enforced permissions, data integrity, and documented product behavior. If implementation and product specifications differ, record the conflict before changing business logic.
2. The four focused product specifications govern feature behavior. The current Discovery working specification explicitly supersedes the older strategy’s later-phase placement of Discovery.
3. This document governs visual foundations, component presentation, accessibility, language, and platform adaptation. It replaces older visual references such as “deep canvas, violet, Inter” where they conflict with this system.
4. Strategy supplies direction, not evidence of delivered features. Native Android is already described as Kotlin/Compose; the earlier React Native roadmap is not a mandate to change the stack.
5. The Manage Flat source contains a historical “missing” audit and a same-day “implemented” update. Treat the target behavior and later update as intent; inspect the current APK before planning migration. References there to §42 are stale; use its actual §08 implementation order.

| Conflict | Resolution |
|---|---|
| Existing indigo/violet versus the supplied coral/teal reference | Adopt teal as the functional primary and coral as the expressive secondary. Exact reference colors remain immutable brand primitives; accessible functional shades vary by theme. |
| Forced-dark onboarding versus a complete light/dark product | Use the operating system’s appearance consistently across authentication, onboarding, and the app. Do not create a Profile appearance setting; the current Profile spec excludes it. |
| Original strategy proposes numeric compatibility and reliability leaderboards | Current Discovery uses qualitative signals; Profile and Home have no numeric trust or reliability card. No percentages, rankings of people, or invented thresholds. |
| Strategy proposes photo proof, task packs, payments, offline writes, subscriptions, and operator dashboards | Keep them out of current screens until supported and explicitly scoped. Design-system extensibility does not authorize product expansion. |
| Discovery has authenticated browsing capability but onboarding limits entry | Preserve the three-intent onboarding model. Find a flatmate creates a flat first. Do not add an Explore-without-a-flat route. |
| Profile prohibits new settings while this system defines accessibility and localization | Support operating-system capabilities and adaptable layouts. Do not invent language, notification, billing, or per-field privacy preferences. |

**Normative language:** MUST is a release requirement; SHOULD is the default with a documented exception; MAY is optional within current product scope.

## 2. Brand foundations

### 2.1 Brand platform

| Element | Definition |
|---|---|
| Name | **Habitiq**, in title case. The name combines Habit and IQ; do not imply undocumented AI capabilities. |
| Product descriptor | Shared living, thoughtfully organised. |
| Established tagline | **Smart living, managed.** Retain where a tagline is needed. |
| Promise | Make responsibilities, shared money, and household coordination easier to understand and act on. |
| Primary audience | People sharing flats and PG accommodation in Indian Tier 1 and Tier 2 cities; designs must support different reading fluencies, budgets, devices, and household routines. |
| Immediate jobs | “What is mine to do?”, “What do I owe?”, “What changed?”, “How do I find someone compatible?” |
| Character | Calm, capable, considerate, lightly warm. |
| Emotional outcome | More clarity and less need to chase one another. |
| Future relevance | Components can extend to operators and other countries without making today’s consumer app resemble an enterprise console. |

Do not repeat unverified market-size, retention, price, or revenue claims from strategy in product UI. “Your flat, on autopilot” is campaign language, not a guarantee that every household action happens automatically.

### 2.2 Principles that determine decisions

| Principle | Required expression | Review question |
|---|---|---|
| Responsibility is immediately clear | Task owner, due date, money direction, and current flat are visible where they affect an action. | Can the person tell what is theirs? |
| Fairness is observable | Explain rotation and show financial breakdowns using actual engine results. | Is the result understandable without implying a new rule? |
| Calm does not conceal urgency | Pair compact status language with a restrained color/icon cue. | Is an overdue item obvious without shaming its owner? |
| Warmth has a purpose | Coral appears in a small identity detail, a welcome moment, or optional illustration. | Does this warmth help the screen, or compete with the task? |
| Every action has a truthful result | Pending, saved, failed, and uncertain outcomes are distinct. | Could this screen claim success before the server confirms it? |
| Complexity appears when needed | Show balance before ledger, task before rotation details, flat summary before admin tools. | Can the first screen answer the primary question? |
| Familiarity reduces effort | Follow Android and iOS navigation, text scaling, keyboard, and assistive-technology conventions. | Does a person need to learn a custom gesture? |

### 2.3 Visual signature

Use soft mineral neutrals, precise teal controls, a restrained coral accent, generous but useful spacing, and strong typographic hierarchy. Dark mode uses green-neutral charcoal with clearly stepped surfaces. The visual identity comes from the relationship between those elements, not from decorating every component.

A typical screen should read as a continuous composition: title, useful summary, grouped content, next action. Avoid enclosing every sentence in a separate rounded card. Reserve the largest amount styling for an actual balance and the strongest filled control for the next useful action.

### 2.4 Identity assets and imagery

- Preserve the existing approved Habitiq mark if available. No logo asset was supplied in this review, so this document does not certify or replace its geometry. Use a plain “Habitiq” text wordmark in prototypes until the approved asset is available.
- Around the wordmark, keep clear space of at least half its rendered cap height. Do not compress, stretch, add extrusion, or apply a glow. Validate minimum legibility at the actual export size; do not infer an app icon from the wordmark.
- Use a one-color mark in `text.primary` or `text.brand`; full-color assets require both theme variants and a monochrome fallback. App/store icons require platform-specific assets and review.
- The reference’s 3D lettering, cursive subtitle, lighting, and social-media chrome are not product UI treatments.
- Current account avatars use initials. Do not suggest that a photo can be uploaded. Discovery may display an existing permitted `photoUrl` only when the current image pipeline supports it; otherwise use initials.
- If product photography is later supported, use authentic shared homes and inclusive everyday situations. Never imply that a stock interior represents an actual listing.
- Empty-state art is optional, small, and secondary to explanation and action. It must not delay content, contain essential text, or invent people, properties, or activity.

### 2.5 UI principles as concrete design rules

Apply these rules to every screen and component review. They are implementation choices for Habitiq, not claims that a formula alone makes an interface professional.

| Principle | Habitiq rule | Practical check |
|---|---|---|
| Visual hierarchy | Establish one primary answer, supporting context, and the next useful action in that order. Use size, weight, position, and space before adding another color. | In Expenses, “You owe” and the amount read before transaction history. |
| Proximity | Keep related items closer than unrelated groups: 8–12 units within a relationship, 24–32 between subjects. | A field label, field, and error read as one group; the next field is clearly separate. |
| Alignment | Use shared start edges and type baselines. Align amounts consistently and actions predictably. | Titles, task names, and descriptions follow the same content edge; no arbitrary offsets. |
| Similarity | Things that behave alike share shape, color role, labeling, and interaction. | Every primary action uses the same semantic button, including in sheets. |
| Common region | Use a card or inset surface when several items form one meaningful unit. Space alone is enough for simpler grouping. | A person balance and its contributing expenses belong together; every line does not need its own card. |
| Figure and background | Separate actionable content from its surroundings through surface, boundary, and type contrast. | Fields remain identifiable in both themes without relying on a faint shadow. |
| Recognition | Keep visible names, current flat, selection state, and contextual information where they affect decisions. | A settlement confirmation repeats person, currency, amount, and direction. |
| Choice and progressive disclosure | Present the choices required for the current decision; reveal advanced controls when relevant. | Three onboarding intents; custom splits after selection; admin actions in detail overflow. |
| Reach and target size | Make frequent actions easy to reach and at least 48 units to tap. Never make the only completion or dismissal control a tiny icon. | Complete, Apply filters, and sheet Close are independently reachable above system insets. |
| Consistency with the platform | Use familiar back, selection, keyboard, sheet, and share behavior. | Android Back and iOS swipe-back preserve a sensible return path. |
| Feedback and error prevention | Show immediate interaction feedback, prevent duplicate requests, and distinguish pending from confirmed results. | Creating a flat cannot be triggered repeatedly while its first request is in flight. |
| Restraint | Apply the 60–30–10 composition and remove decoration that competes with the primary question. | A task list reads as tasks and responsibilities, not as a collection of equally loud colored panels. |

Review in this order: correct user task → correct information → clear hierarchy → grouping/alignment → spacing/type → color balance → interaction states → accessibility. A failure earlier in the sequence cannot be repaired with additional visual polish.

## 3. Product contract: preserve before polishing

### 3.1 Navigation and information architecture

The app has four destinations and a contextual create action: **Home · Manage · Discover · Profile**, with the existing center **+** retained on Android. The plus is an action, not a fifth destination. Its exact placement must preserve the existing shell’s behavior and allow sufficient touch space.

| Surface | Primary purpose | Required behavior |
|---|---|---|
| Home | What is happening today? | Greeting, today’s tasks, complete if assigned to the current user, pending joins/swaps appropriate to role, recent activity, Discovery teaser. No duplicate Tasks/Expenses admin toggle. |
| Manage | What do I need to do or pay? | “Manage Flat” hub with Tasks and Expenses summaries. Back from either returns to the hub. The legacy `AppTab.TASKS` enum may remain; its visible label is Manage. |
| Discover | Find a flat or a flatmate | Distinct intents and post types, detail before connection, request inbox, in-app conversation, My posts. |
| Profile | Who am I in Habitiq? | Personal identity and navigation to My Flat, Discovery profile, existing Preferences, and Sign out. |

The Android center action is context-sensitive: Manage hub → Add task for admins, Add expense, and Bills entry; Tasks → Add task for admins; Expenses → Add expense; Discover → Create Discovery post. Bills entry does not imply bill-creation permission. Hide task creation for members. Preserve the verified existing Home/Profile action behavior; if no valid action exists there, omit the control in that context rather than fabricate a workflow. Keep destination positions stable.

Use a visible label when an action opens a menu (“Create”) and a specific accessible name once its purpose is direct (“Add expense”). Tab labels remain visible. Preserve each destination’s scroll position and back stack where platform behavior permits. Switching flats resets flat-scoped content and listeners before displaying the next flat.

### 3.2 Onboarding state and routing

| Account state | Destination / feedback |
|---|---|
| Unauthenticated | Existing Google or email/password authentication. |
| Authenticated; account state loading | Neutral progress surface; no dashboard or intent chooser flash. |
| Account/active-flat lookup fails | Retry state. A failed lookup is not evidence of no flat. |
| Authenticated; valid `activeFlatId` | Home for that flat. |
| Authenticated; no active flat | Three-intent chooser, unless displaying a verified pending-join state. |
| Join request awaits approval; no active flat | Pending confirmation; no Home. On restart, recover from existing request/account state rather than a local success flag. Missing lookup support is an implementation gap, not a new field to invent. |
| Multiple flats | Home for the active flat; switcher under My Flat. |
| Last flat left | Intent chooser. Leaving one of several selects the next flat through existing repository behavior. |

**Intent screen title:** “What brings you to Habitiq?”  
**Support:** “Choose what you want to do first. You can change this later.”

| Intent | Supporting copy | Outcome |
|---|---|---|
| Manage my flat | Organise tasks, expenses and everyday flat life. | Existing create wizard → optional invite/share → Home. |
| Find a flatmate | Find people and flats that fit you. | “Create your flat first” explanation → create wizard → Discover / Find a flatmate. This maps to the source’s “Find a person” view. |
| Join an existing flat | Already have an invite code? Join your flat. | Code lookup → preview → Join flat or Request to join. |

Use three full-width intent rows/cards with a title, one short explanation, and directional affordance. Do not add a carousel, “just exploring,” a profile questionnaire, or an arbitrary progress percentage.

The create wizard collects only supported name, type, and optional location. Do not collect household size, company, college, hometown, or profession. The flat supports at most eight members; that is a capacity rule, not an onboarding field. Invite code equals the flat document ID. Sharing is skippable; no task template factory or mandatory invitation count.

Before Find a flatmate creation, explain: “Your flat gives people context about who they’re joining and how the household works.” Creating the flat must not publish a vacancy automatically.

Join trims and normalizes the invite code according to the existing repository contract, looks up the flat, and previews name, member count, and approval mode. The server transaction remains authoritative if the flat becomes full between preview and join. Preserve not-found, full, already-member, network, pending, and accepted states.

Guard Create while a request is in flight and when an active flat already exists. On restart or an uncertain response, reconcile the account state before offering a new creation attempt. Do not claim that a presentation-layer guard alone guarantees cross-device idempotency.

### 3.3 Tasks and household responsibility

- Members enter **My Tasks**; admins can use **All Tasks** with create controls. My Tasks and All Tasks are the two main views. Overdue is a filter or priority within a view, not the default destination.
- Task order within a card: name → due language → assignee/You → status. Frequency, next person, and rotation detail appear on expansion or in a sheet.
- Complete an assigned task in one tap. Show “Completing…” and reconcile with the repository. Do not ask for a routine confirmation, celebrate before success, or offer Undo if the engine has no reversal.
- Overdue responsibility remains with the responsible member until completion. Rendering must never silently move it to someone else.
- Rotation displays **Now / Next / Then** from engine output, with “Away” markers. Do not display `rotationQueue[0]` as if it necessarily means the current assignee.
- “I’m away” uses the existing GoingAway flow. Away skipping and return position follow the engine. All members away produces a paused task with no valid assignee, not an exception or invented assignment.
- A swap starts from the relevant task (“Can’t do this?”). Preserve pending, accepted, declined, and transfer behavior and the existing review sheet. No separate top-level Swaps destination.
- Admin edit, delete, and override live in task detail overflow. Members must not see those controls. Preserve completion and swap authorization from the repository, not just visible role labels.
- Keep completed tasks quieter or filtered from My Tasks. Do not introduce a reliability dashboard, shame-oriented streak, or public leaderboard.

### 3.4 Expenses, settlements, and bills

The first Expenses screen shows the person’s financial position: **You owe**, **You’re owed**, or **All settled**, with month/currency context. If both outgoing and incoming balances exist, show both directions and their breakdown. Do not hide obligations behind an ambiguous net total or call the household settled because two directions cancel numerically.

Expand by person to show contributing expenses and the appropriate **Settle** or **Mark received** action. A settlement records an event in Habitiq; it is not an in-app bank transfer. Do not label it “Pay now,” show a payment-provider success screen, or imply money moved automatically.

Add expense asks for what, amount, paid by, and equal/custom split. Reveal custom amounts only when selected. Preserve decimal precision, rounding, permission rules, and the existing split calculation. Identify a split mismatch with the actual remaining or excess amount.

Keep Daily splits and Monthly bills as separate concepts. Bill states use **Upcoming / Shares ready / Paid / Skipped**, mapped to existing engine states. `paidBy` and `collectorId` are different: label them **Paid by** and **Collecting shares**. The payer does not collect from themselves. Collector and self-mark actions follow `BillsRepository` permissions.

**Review & close month** is an admin action in an overflow or secondary section. Show the period, outstanding amounts, and the actual consequence before confirmation. Do not expose it on the Manage hub. Carry-forwards, repeat-close behavior, and the closed-period policy follow the existing implementation.

Display currencies separately until verified conversion support exists. Never add INR and USD together or invent an exchange rate. Store and calculate using the existing monetary model; token formatting does not alter arithmetic.

### 3.5 Profile and identity

First-screen order is **identity header → My Flat → Discovery → Preferences → Account**.

| Area | Allowed content | Exclusions |
|---|---|---|
| Identity header | Initial avatar, name, email, Edit profile. | Trust cards, progress percentages, admin chrome. |
| Edit profile | Full name edit through `updateProfile`; email read-only. | Fake photo picker, editable Auth email without a supported workflow. |
| My Flat | Current name, role, member count; members with role and You; invite/share; admin Manage Flat; secondary activity; leave confirmation; multi-flat switcher. | Member admin controls, reliability values. |
| No flat | “You haven’t joined a flat yet.” → Continue setup. | Blank Home or a second conflicting Create/Join flow. |
| Discovery profile | Existing looking-post city, lookingIn, budget, bio, gender, lifestyle tags, and active visibility. Existing repository handles identity fields. | Company, college, hometown, age, profession, commute UI, per-field visibility. |
| Preferences | Existing biometric lock and account deletion in a danger section. | Invented notification matrix, appearance switch, language menu, billing. |
| Account | Sign out with confirmation; existing Auth flow. | Deleting membership as a side effect of ordinary sign-out. |

Account identity and Discovery identity are separate forms with explicit explanations. “Visible as a looking post” maps to `active`; it does not control every individual field. Explain what is publicly shown without implying that the account email is part of Discovery.

Map the stored looking-post gender strings `any`, `male`, `female`, `other` to product-approved inclusive labels. Do not relabel `any` as “Prefer not to say” unless its actual meaning supports that mapping. In Profile this field describes the person; preference filters belong in Discover. Record ambiguous enum meaning as a schema/copy dependency rather than silently changing data.

Leave flat and Delete account remain distinct actions. The strategy’s last-admin transfer, final-member deletion, and unsettled-balance rules require confirmation against current repositories before copy describes their consequences. The system supplies confirmation patterns; it does not decide those policies.

### 3.6 Discovery, consent, and trust

**Find a flat** and **Find a flatmate** are different intents. Vacancy and looking posts have different schemas and card anatomies. Standardize “Find a flatmate” as the visible person-search label; retain the existing internal `DiscoverMode` mapping.

| Contract | Required presentation |
|---|---|
| Vacancy fields | Use only existing city, area, rent per head, currency, available beds, preferences, description, and supported lifestyle/custom tags. Publishing is admin-only. |
| Looking post fields | Use supported display name, optional existing photo, city/lookingIn, budget, bio, gender, lifestyle tags, and active state. |
| Location | City/area only. Never extract an exact address from stray data. Create-flat pincode, landmark, or map pin is not a Discovery coordinate. |
| Compatibility | Specific overlapping preferences and contextual explanations. No score, percentage, AI claim, or invented personal attribute. |
| Trust | `Unrated` when consent is off; `New to Habitiq` as a neutral label; `Habitiq member` for an existing-flat listing where supported. No “Verified,” “Safe,” “Reliable,” or “Plus” tier without documented criteria. |
| Sorting | Explain supported order: location → budget → room → preference overlap. Trust is only a small tie-breaker and never an exclusion. Do not expose fictional numerical weights. |
| Contact | Request → accept/decline → in-app conversation. No phone, WhatsApp, or email on Discovery surfaces, including after acceptance. |
| Membership | A connection or match does not join a flat. Invite code/join approval remains a separate membership flow. |
| Safety | Report and block are reachable on detail/conversation. A report does not change a trust tag. Do not invent enforcement results or moderation response times. |
| Availability | Reflect Published/Paused/Closed and existing active state where supported. A closed listing must not appear open because of stale decorative copy. |

Preserve the current flags: `ENABLED`, `FIND_FLATMATE`, `LOOKING_POSTS`, `CONNECTION_REQUESTS`, `QUALITATIVE_TRUST`, and `DISTANCE_INTELLIGENCE=false`. Hide disabled-feature entry points; do not show inert buttons advertising unshipped features.

Supported journey: browse/search/grouped filters → detail → connection request → inbox decision → conversation; My posts supports pause/resume/close and creation. Requests use `REQUEST_SENT`, `ACCEPTED`, `DECLINED`, and `BLOCKED`. Do not invent Cancel request, a viewing scheduler, a `MATCHED` backend event, or an exact-address reveal.

Distance/commute intelligence remains Phase 2. No kilometre values, travel times, Maps SDK, company/college matching, or use of reserved `commuteAnchors`/`discoveryApproxLocation` in the current UI. Connection/report collections and their rules are additive only in the Discovery scope; deploying their security rules is a prerequisite for dependent production UI.

## 4. Token architecture and implementation contract

### 4.1 Three layers

**Primitive → semantic → component.** Primitives hold values; semantics express purpose and theme; components bind purpose to a specific control. This structure separates brand evolution from product behavior and makes theme changes traceable to one source.

| Layer | Example | Consumers |
|---|---|---|
| Primitive | `hq.ref.color.teal.700` | Theme definitions only. |
| Semantic | `hq.sys.color.action.primary.bg` | Shared styles and component aliases. |
| Component | `hq.comp.button.primary.bg` | Button implementation. |

Use lowercase dot paths in documentation. Dots express nesting, not literal JSON key characters in a DTCG export. Color fields use `bg`, `fg`, `border`; states use `hover`, `pressed`, `selected`, `disabled`, `focus`. Avoid `new`, `premium`, `greenButton`, screen names, or raw Tailwind palette names in semantic tokens.

The JSON registry below is the canonical, executable design-system registry for this document. It is a deliberately compact **Habitiq registry**, not a claim of complete DTCG conformance. `ref` contains primitive hex values; each `themes` value is a path into `ref.color`. Select the theme, resolve its aliases, then generate platform values. No alias should be guessed at runtime.

For exchange tools, transform colors to DTCG `$type: "color"` with an sRGB `colorSpace`, normalized `components`, and `alpha`; preserve aliases using `{...}` syntax. Use typed dimensions, durations, and font properties in generated files. See the [DTCG format](https://www.designtokens.org/tr/2025.10/format/) and [color module](https://www.designtokens.org/tr/2025.10/color/). Do not hand-maintain a second palette.

### 4.2 Canonical registry

```json
{
  "schema": "habitiq.design-registry.v1",
  "version": "2.0.0",
  "ref": {
    "color": {
      "neutral": {
        "0": "#FFFFFF", "25": "#F7F8F5", "50": "#F2F7F4",
        "100": "#EEF2EF", "200": "#D7DFD9", "300": "#C0CDC6",
        "400": "#A3B3AE", "500": "#788781", "550": "#788B83",
        "600": "#606D67", "650": "#52605D", "700": "#34473E",
        "750": "#25332F", "800": "#202D28", "850": "#18231F",
        "900": "#10201E", "950": "#101815"
      },
      "teal": {
        "50": "#F0FDFA", "100": "#DDF5EE", "200": "#99F6E4",
        "300": "#5EEAD4", "400": "#2DD4BF", "500": "#14B8A6",
        "600": "#0D9488", "700": "#0F766E", "800": "#115E59",
        "900": "#134E4A", "950": "#153B33"
      },
      "coral": {
        "50": "#FFF1EE", "100": "#FFE0D9", "200": "#FFC3B8",
        "300": "#FFA89C", "400": "#FF8878", "500": "#FF6B5A",
        "600": "#D94E3D", "700": "#A9392B", "800": "#852F25",
        "900": "#612A23", "950": "#3B2321"
      },
      "red": { "50": "#FFF0F3", "300": "#FFB3C1", "700": "#B4233C", "800": "#8F1C30", "900": "#741A29", "950": "#381C26" },
      "amber": { "50": "#FFF5DC", "300": "#FFD27A", "700": "#8A5100", "950": "#352B18" },
      "green": { "50": "#EAF6ED", "300": "#8FD8A7", "700": "#21643B", "950": "#192E22" },
      "blue": { "50": "#EEF4FF", "300": "#A8CAFF", "700": "#245BA8", "950": "#1C2A40" },
      "violet": { "300": "#CAB5EF", "700": "#7652A5" },
      "ochre": { "700": "#946000" }
    }
  },
  "themes": {
    "light": {
      "canvas": "neutral.25", "surface.base": "neutral.0", "surface.raised": "neutral.0", "surface.subtle": "neutral.100",
      "surface.inverse": "neutral.850", "text.primary": "neutral.850", "text.secondary": "neutral.650", "text.muted": "neutral.600",
      "text.inverse": "neutral.50", "text.brand": "teal.700", "icon.default": "neutral.650",
      "border.subtle": "neutral.200", "border.control": "neutral.500", "focus": "teal.700",
      "action.primary.bg": "teal.700", "action.primary.fg": "neutral.0", "action.primary.hover": "teal.800", "action.primary.pressed": "teal.900",
      "action.secondary.bg": "surface.base", "action.secondary.fg": "teal.700", "action.secondary.hover": "teal.50", "action.secondary.pressed": "teal.100",
      "action.tertiary.fg": "teal.700", "action.tertiary.hover": "teal.50", "action.tertiary.pressed": "teal.100",
      "action.danger.bg": "red.700", "action.danger.fg": "neutral.0", "action.danger.hover": "red.800", "action.danger.pressed": "red.900",
      "disabled.bg": "neutral.100", "disabled.fg": "neutral.600", "disabled.border": "neutral.200",
      "selected.bg": "teal.100", "selected.fg": "teal.900", "selected.border": "teal.700",
      "warm.bg": "coral.50", "warm.fg": "coral.700", "brand.teal": "teal.500", "brand.coral": "coral.500",
      "status.info.bg": "blue.50", "status.info.fg": "blue.700",
      "status.success.bg": "green.50", "status.success.fg": "green.700",
      "status.warning.bg": "amber.50", "status.warning.fg": "amber.700",
      "status.danger.bg": "red.50", "status.danger.fg": "red.700",
      "chart.1": "teal.700", "chart.2": "blue.700", "chart.3": "violet.700", "chart.4": "ochre.700"
    },
    "dark": {
      "canvas": "neutral.950", "surface.base": "neutral.850", "surface.raised": "neutral.800", "surface.subtle": "neutral.750",
      "surface.inverse": "neutral.50", "text.primary": "neutral.50", "text.secondary": "neutral.300", "text.muted": "neutral.400",
      "text.inverse": "neutral.850", "text.brand": "teal.300", "icon.default": "neutral.300",
      "border.subtle": "neutral.700", "border.control": "neutral.550", "focus": "teal.300",
      "action.primary.bg": "teal.300", "action.primary.fg": "neutral.900", "action.primary.hover": "teal.200", "action.primary.pressed": "teal.400",
      "action.secondary.bg": "surface.base", "action.secondary.fg": "teal.300", "action.secondary.hover": "teal.950", "action.secondary.pressed": "neutral.750",
      "action.tertiary.fg": "teal.300", "action.tertiary.hover": "teal.950", "action.tertiary.pressed": "neutral.750",
      "action.danger.bg": "red.300", "action.danger.fg": "red.950", "action.danger.hover": "red.50", "action.danger.pressed": "red.300",
      "disabled.bg": "neutral.750", "disabled.fg": "neutral.400", "disabled.border": "neutral.700",
      "selected.bg": "teal.950", "selected.fg": "teal.200", "selected.border": "teal.300",
      "warm.bg": "coral.950", "warm.fg": "coral.300", "brand.teal": "teal.500", "brand.coral": "coral.500",
      "status.info.bg": "blue.950", "status.info.fg": "blue.300",
      "status.success.bg": "green.950", "status.success.fg": "green.300",
      "status.warning.bg": "amber.950", "status.warning.fg": "amber.300",
      "status.danger.bg": "red.950", "status.danger.fg": "red.300",
      "chart.1": "teal.300", "chart.2": "blue.300", "chart.3": "violet.300", "chart.4": "amber.300"
    }
  },
  "metrics": {
    "space": { "0": 0, "1": 4, "2": 8, "3": 12, "4": 16, "5": 20, "6": 24, "8": 32, "10": 40, "12": 48, "16": 64 },
    "radius": { "none": 0, "small": 8, "control": 12, "card": 20, "sheet": 28, "pill": 9999 },
    "border": { "hairline": 1, "selected": 2, "focus": 2, "focusOffset": 2 },
    "size": { "target": 48, "button": 52, "input": 56, "row": 56, "rowTwoLine": 72, "fab": 56, "iconSmall": 16, "iconMedium": 20, "iconDefault": 24, "avatarSmall": 32, "avatarMedium": 40, "avatarLarge": 64 },
    "layout": { "gutterCompact": 16, "gutterStandard": 20, "gutterMedium": 24, "gutterExpanded": 32, "formMax": 480, "detailMax": 600, "contentMax": 1200, "breakMedium": 600, "breakExpanded": 840 },
    "opacity": { "scrimLight": 0.40, "scrimDark": 0.64, "pressedOverlay": 0.08 },
    "durationMs": { "none": 0, "instant": 80, "fast": 120, "standard": 180, "enter": 240, "exit": 180, "large": 300 },
    "easing": { "standard": [0.2, 0, 0, 1], "enter": [0, 0, 0.2, 1], "exit": [0.4, 0, 1, 1], "linear": [0, 0, 1, 1] },
    "layer": { "content": 0, "sticky": 10, "navigation": 20, "scrim": 30, "modal": 40, "transient": 50 }
  }
}
```

**Resolver rule:** A theme entry normally points to `ref.color`. The single intentional semantic alias `action.secondary.bg → surface.base` resolves within the selected theme. Reject unknown references and cycles. Expand all theme keys under `hq.sys.color`. Both themes MUST export the same semantic keys. Metrics expand under `hq.ref`; semantic/component metrics below reference them. The registry’s dotted theme keys are a compact lookup convention; split them into nested keys for DTCG output.

Spatial values are **logical units**, mapped to Android dp and iOS pt, never physical pixels. Type sizes map to Android sp and scalable iOS text styles. A web token export uses rem for text and scalable layout, with the root at 16px; it is an adapter, not the primary platform. Keep colors in sRGB and avoid platform-specific wide-gamut drift for these tokens.

## 5. Color architecture

### 5.1 Brand, action, and status are separate

**Teal `#14B8A6`** is the exact primary brand anchor. **Coral `#FF6B5A`** is the exact secondary brand anchor. These communicate the requested warm/fresh direction as a design choice; no universal psychological effect is claimed.

Actions use semantic teal shades selected for contrast, not the unmodified brand swatch. Coral gives warmth to a small welcome detail, an optional illustration, or a brand moment. It is not the universal CTA color and is never reused as “error” or “unpaid.” Avoid placing a coral decorative highlight beside an error badge where their meanings could merge.

Neutral surfaces occupy most of a working screen. Follow the supplied **60–30–10 rule** as the default composition described in §5.5. Use color for identity, selection, action, state, and useful comparison. Never use the brand anchors as small text on white.

### 5.2 Surface hierarchy

| Role | Light | Dark | Rule |
|---|---|---|---|
| Canvas | `#F7F8F5` | `#101815` | Continuous page background; no gradient. |
| Base | `#FFFFFF` | `#18231F` | Cards, fields, navigation surfaces. |
| Raised | `#FFFFFF` | `#202D28` | Dialogs, sheets, menus above page content. |
| Subtle | `#EEF2EF` | `#25332F` | Inset summaries, quiet grouping, disabled fill. |
| Selected | `#DDF5EE` | `#153B33` | Selected row/chip/navigation indicator, always with an additional cue. |
| Inverse | `#18231F` | `#F2F7F4` | Rare inverse message surface, paired only with `text.inverse`. |

Do not apply a global tint or invert the light theme mechanically. Dark surfaces step lighter with elevation; readable text and boundaries provide depth. System bars must match the adjacent surface with appropriate light/dark system icons. Avoid a bright flash while appearance or authentication state initializes.

Use `border.subtle` only for decorative separation and grouping where it is not necessary to identify a control. Use `border.control` for editable fields, unselected choice controls, and other boundaries needed to understand interaction. A quiet border is not an accessible input boundary by itself.

### 5.3 Contrast contract

All informative text, including timestamps, placeholders, secondary labels, and badges, targets **at least 4.5:1** against its actual background. This deliberately avoids relying on a “small caption” exception. Essential non-text indicators and control boundaries target at least **3:1** against adjacent colors. Disabled elements still use readable tokens, although inactive controls are exempt from WCAG contrast requirements. The distinction and text thresholds follow [WCAG contrast guidance](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html).

| Foreground / background | Calculated contrast | Approved use |
|---|---:|---|
| White / teal `#0F766E` | 5.47:1 | Light primary button. |
| `#10201E` / teal `#5EEAD4` | 11.38:1 | Dark primary button. |
| `#52605D` / canvas `#F7F8F5` | 6.18:1 | Light secondary text. |
| `#A3B3AE` / raised-subtle `#25332F` | 6.04:1 | Dark metadata. |
| `#788781` / white | 3.76:1 | Light control boundary. |
| `#788B83` / `#25332F` | 3.65:1 | Dark control boundary. |
| `#B4233C` / `#FFF0F3` | 5.86:1 | Light error text. |
| `#FFB3C1` / `#381C26` | 9.15:1 | Dark error text. |
| `#A9392B` / `#FFF1EE` | 5.77:1 | Light coral-tinted brand note. |
| `#FFA89C` / `#3B2321` | 7.82:1 | Dark coral-tinted brand note. |
| White / brand teal `#14B8A6` | 2.49:1 | **Not approved for text.** |
| White / brand coral `#FF6B5A` | 2.80:1 | **Not approved for text.** |

Ratios use the WCAG sRGB luminance calculation for opaque swatches. They do not certify a rendered screen, transparency, disabled overlays, photographs, or an entire application. Recompute any new pairing; do not infer approval from a palette family.

### 5.4 State mapping

| Product state | Semantic role | Additional indicator |
|---|---|---|
| Due today | Warning | Clock + “Due today.” |
| Overdue | Danger | Alert/clock + “Was due yesterday” or explicit date. |
| Due soon | Info | Calendar + date. |
| Completed | Success | Check + “Done.” |
| Away | Neutral | Away icon + “Away.” |
| Task paused, everyone away | Neutral | Pause icon + explanation. |
| Pending join/connection/swap | Info | Pending label; warning only when a real issue needs attention. |
| Selected destination/filter | Selected | Indicator, check, or selected accessibility state. |
| Trust tag | Neutral | Exact qualitative label and explanation; no success shield implying verification. |
| You owe / You’re owed | Primary text | Explicit direction and person/currency; never red/green alone. |

### 5.5 The 60–30–10 composition rule

Use the user's reference to balance the visual weight of each ordinary screen: **approximately 60% neutral base, 30% supporting treatment, and 10% accent**. These proportions describe perceived composition, not an exact pixel-count requirement. They guide a first-pass layout and the final visual review; readability, actual content, and required feedback take precedence over forcing a quota.

| Share | Role in the reference | Habitiq implementation |
|---|---|---|
| **60% — base** | Neutral foundation | `canvas`, breathing space, and quiet large areas. Light: mineral off-white. Dark: green-neutral charcoal. This is the continuous visual ground. |
| **30% — support** | Secondary structure and content | `surface.base`, `surface.subtle`, readable text, ordinary icons, boundaries, grouped rows, and cards. Support distinguishes content without competing with the main action. |
| **10% — accent** | Attention and emphasis | Teal actions, selected indicators, active links, a small coral brand detail, and necessary semantic state cues. Accents are concentrated around decisions and important information. |

**“Secondary” in this composition rule is not “secondary brand color.”** The 30% supporting treatment is mostly neutral structure and readable content. It does not mean covering 30% of every screen in coral. Teal and coral share the accent allowance with semantic status colors; do not add a second 10% for each color.

Within the accent area, functional teal normally has the strongest presence. Coral is a smaller expressive detail and may be absent from a dense task, expense, or error screen. Do not introduce ornamental coral merely to reach a ratio. A welcome composition may use more coral within the accent area, while a task list needs room for genuine status indicators. Status must remain truthful even when several items are overdue.

Dark mode follows the same hierarchy using dark neutral surfaces and brighter accessible accents. Do not convert the 30% support area into low-opacity gray text. Text keeps its contrast independently of the visual balance.

| Screen | Base and support composition | Accent placement |
|---|---|---|
| Onboarding intent | Calm canvas, prominent title, three orderly neutral intent cards. | Small coral identity detail; teal interaction/selection. No oversized colored hero. |
| Home | Neutral summary and task/activity groups, with enough separation to scan. | Primary useful action, active navigation, compact genuine statuses. |
| Manage hub | Canvas around two clear Tasks/Expenses cards; summaries carry hierarchy. | Restrained teal affordances and relevant overdue cue. |
| Tasks | Consistent neutral rows/cards, aligned assignee and due information. | Complete action, active view, small status badges; no full red cards. |
| Expenses | Amount typography and whitespace establish priority; person rows form support. | Settle/Mark received entry, selected period/view; no red/green debt wash. |
| Discover | Neutral cards, legible location/budget hierarchy, quiet filters. | Active filter/intent and connection action; trust labels stay neutral. |
| Profile | Identity header and grouped neutral navigation rows. | Edit/active controls; destructive emphasis only inside the relevant confirmation. |

**Review method:** look at the screen at reduced scale, then in grayscale. The primary information and action should remain easy to locate. If every card, icon, or heading competes, reduce accent fills and strengthen spacing/type hierarchy. Check the normal-size screen again for text contrast, target size, and status meaning. Do not claim that an image-analysis percentage is a usability score.

## 6. Typography

### 6.1 Font strategy

Android uses bundled **Inter** for the product UI: Regular 400, Medium 500, Semibold 600, Bold 700. Preserve the existing Inter direction while improving scale and spacing. Bundle the licensed font assets; do not depend on a network font fetch. Use the platform sans-serif fallback if unavailable. Check the delivered font’s license and include its required notice.

iOS uses the **system font through native text styles**, with Dynamic Type and weight mapping below. Do not bundle an extracted SF font or force Android metrics onto iOS. The brand remains consistent through color, hierarchy, proportion, and component behavior. Invite codes use the platform monospaced font; amounts use the body font’s tabular numerals. Plus Jakarta Sans and Geist Mono are not required dependencies for the mobile app.

### 6.2 Semantic type tokens

The table is normative at default text size. Android columns are sp; iOS uses the named scalable system style, with a default-size reference rather than a fixed point override. Tracking is in em for Android and remains native on iOS unless testing demonstrates a need.

| `hq.sys.type.*` | Android size / line / weight / tracking | iOS style / weight | Use |
|---|---|---|---|
| `display` | 32 / 40 / 600 / -0.02 | largeTitle / semibold | Intent title or a genuinely primary balance. At most one dominant block per screen. |
| `title.large` | 28 / 36 / 600 / -0.015 | title1 / semibold | Main screen title. |
| `title.medium` | 22 / 28 / 600 / -0.01 | title2 / semibold | Sheet or major section heading. |
| `title.small` | 18 / 24 / 600 / 0 | title3 / semibold | Card/section heading. |
| `body.large` | 16 / 24 / 400 / 0 | body / regular | Primary reading, fields, explanations; native iOS default body is 17pt. |
| `body.medium` | 14 / 20 / 400 / 0 | subheadline / regular | Secondary descriptions and compact supporting information. |
| `label.large` | 16 / 24 / 600 / 0 | body / semibold | Main buttons. |
| `label.medium` | 14 / 20 / 500 / 0 | subheadline / medium | Filters and field labels. |
| `label.small` | 12 / 16 / 500 / 0.01 | caption1 / medium | Badges, tab labels, short metadata. Never the only explanation of a critical state. |
| `code` | 16 / 24 / 500 / 0.04 | body, monospaced / medium | Invite code; selectable/copyable. |
| `amount` | 32 / 40 / 600 / -0.02 | largeTitle, monospaced digits / semibold | Primary balance, paired with a direction label. |

Use 400 for reading, 500 for compact UI labels, 600 for hierarchy, and 700 only for occasional short emphasis. Do not manufacture weight with a duplicate shadow or outline. Sentence case is the default; no letter-spaced all-caps eyebrow above every heading. “You” is preferable to shouting “YOU.”

### 6.3 Scaling and text behavior

- Use sp without overriding the user’s font scale on Android. Use native Dynamic Type on iOS; custom text must scale with the corresponding style. [Apple’s accessibility guidance](https://developer.apple.com/design/human-interface-guidelines/accessibility) recommends adaptable type and system accessibility support.
- Content height is intrinsic. Buttons, rows, tabs, and sheets grow when text grows. The dimensions in this system are minima, not clipping boxes.
- At large accessibility sizes, stack amount and action, place metadata below titles, change horizontal controls to vertical rows, and allow full-height scrollable sheets. Do not shrink text to preserve a screenshot.
- Task names may use two lines in a compact list and expose the full name in details and semantics. Essential amounts, errors, primary action labels, and account-state explanations must not be truncated.
- Use tabular numerals in aligned amounts; preserve currency symbols and locale grouping. Do not force all prose into monospaced text.
- Keep prose to roughly 35–60 characters per line where space allows. Start-align labels and descriptions. Center only brief welcome, empty, or confirmation compositions.
- Respect script shaping and fallback fonts for future Indian languages. Do not apply Latin tracking to Devanagari, Telugu, or Tamil. Test glyph baselines and multi-line line-height before enabling a locale.

## 7. Space, layout, and shape

### 7.1 Spacing semantics

Use the registry’s 4-unit scale. One- and two-unit strokes are exceptions for borders and optical alignment, not new layout increments.

| Semantic token | Primitive | Purpose |
|---|---|---|
| `hq.sys.space.inline` | `space.2` = 8 | Icon-to-label, badge internal gap. |
| `hq.sys.space.related` | `space.3` = 12 | Closely related controls/rows. |
| `hq.sys.space.component` | `space.4` = 16 | Card padding at small widths; list item breathing room. |
| `hq.sys.space.card` | `space.5` = 20 | Standard feature/task card padding. |
| `hq.sys.space.group` | `space.6` = 24 | Between logical content groups. |
| `hq.sys.space.section` | `space.8` = 32 | Major section separation. |
| `hq.sys.space.screenEnd` | `space.6` = 24 | End-of-content breathing room before navigation/insets. |

Increase space to indicate a change of subject, not to fill an empty screen. Keep title-to-description tighter than description-to-action. Align first text baselines and amount columns; center icon artwork optically within its box.

### 7.2 Adaptive native grid

Breakpoints use the **available application window width** in logical units, not device model or physical screen pixels.

| Width | Grid / gutter | Content behavior |
|---|---|---|
| Below 360 | Four columns / 16 | One column of content; compact margins, full-size targets. Essential text wraps. |
| 360–599 | Four columns / 20 | Main phone layout; one content column, full-width primary forms. |
| 600–839 | Eight columns / 24 | Centered forms max 480; detail max 600; two cards only when reading order remains clear. |
| 840 and above | Twelve columns / 32 | Max content width 1200; eligible list/detail split. Navigation can use a platform rail/sidebar while preserving destinations. |

Use 16-unit grid gutters between columns. Do not squeeze a mobile form into two columns merely because the device is landscape. Tablet/foldable support must consider hinges and window resizing; no control may straddle an occlusion.

Respect status-bar, navigation-bar, cutout, gesture, and keyboard insets. Sticky action areas use their own opaque surface and separator when content scrolls beneath. Scroll content must include padding equal to the actual sticky area plus safe inset; never use a guessed fixed bottom offset.

When the keyboard opens, keep the active field, validation message, and next relevant action reachable. A multiline form scrolls; it does not get vertically compressed. Preserve text and selection across rotation and a recoverable navigation change.

### 7.3 Radius and nested geometry

| Token | Value | Use |
|---|---:|---|
| `radius.none` | 0 | Edge-to-edge lists and dividers. |
| `radius.small` | 8 | Small status badges, inset elements. |
| `radius.control` | 12 | Buttons, text fields, interactive option rows. |
| `radius.card` | 20 | Main cards, intent cards, contextual summaries. |
| `radius.sheet` | 28 | Top corners of Android sheets; native iOS presentation takes precedence. |
| `radius.pill` | 9999 | Avatar circles, chips, segmented indicators where appropriate. |

Keep geometry consistent by function. Avoid a pill button inside a pill card inside a pill page. Nested corners should follow the outer curve with the actual inset; do not force a larger inner radius than the container can accommodate.

### 7.4 Elevation and layers

| `hq.sys.elevation.*` | Light shadow specification, logical units | Dark treatment |
|---|---|---|
| `none` | None | None. |
| `floating` | x 0, y 2, blur 8, spread 0, `#10201E` at 8% | `surface.raised` + subtle border; no glow. |
| `overlay` | x 0, y 8, blur 24, spread 0, `#10201E` at 14% | `surface.raised` + subtle border + scrim. |

Ordinary task cards and list rows use no shadow. A sheet’s scrim is black at 40% in light and 64% in dark; use the native platform equivalent if required. Do not blur the entire application behind overlays. Exact shadow rendering may differ across native frameworks; match visual hierarchy, not a CSS-only blur algorithm.

Only one blocking modal at a time. The system keyboard and platform surfaces remain above app-controlled content. A nonblocking snackbar must not cover the action being acknowledged or escape a modal’s accessibility scope.

## 8. Iconography and interaction affordances

Use **Lucide-style outlined symbols** as the Android product icon family, supplied as reviewed vector assets compatible with Compose. The old “Lucide React everywhere” instruction does not apply to native code. Use a 24 × 24 viewbox and consistent 2-unit strokes with rounded caps. Do not add a library solely to render a few icons; preserve licenses and maintain a named asset manifest.

| Context | Artwork size | Interaction area |
|---|---:|---:|
| Inline metadata | 16 | Noninteractive unless paired with a separate target. |
| Button leading/trailing | 20 | Button’s complete area. |
| Toolbar, bottom navigation, standalone action | 24 | At least 48 × 48. |
| Empty-state symbol | 40–48 | Decorative unless explicitly actionable. |

| Meaning | Preferred symbol |
|---|---|
| Home | House |
| Manage | List/check or household-management symbol used consistently |
| Discover | Search/compass; one chosen mapping throughout |
| Profile | Person/circle |
| Task done | Check/check-circle |
| Expense | Receipt |
| Away | Suitcase |
| Share | Platform-native share symbol |
| Overflow | Platform-native more menu |
| Report | Flag |
| Block | Ban |

iOS may use SF Symbols for standard navigation and system actions with semantically equivalent weights and optical sizes. Keep product-specific symbols and meanings aligned. Directional arrows mirror in RTL where appropriate; financial signs, logos, and non-directional objects do not automatically mirror.

Pair unfamiliar symbols with labels. An icon-only control needs an action-specific accessible name, such as “More options for Kitchen cleanup.” Decorative icons are hidden from screen-reader traversal. Avoid duplicate announcements of an icon and its adjacent label. Do not use emoji as navigation, status, or control icons; user-authored task names may contain emoji.

## 9. Motion, feedback, and haptics

Motion shows cause, location, and state. It must never delay an action, conceal a failure, or imply that an unconfirmed write succeeded.

| Event | Duration / easing token | Behavior |
|---|---|---|
| Press feedback | 80ms / standard | Native indication or subtle surface change; avoid shrinking text-heavy controls. |
| Color/focus/selection | 120ms / standard | A short state transition; focused controls stay visibly focused. |
| Local expansion | 180ms / standard | Reveal details without moving the selected task offscreen. |
| Sheet entrance | 240ms / enter | Platform-native transition preferred. |
| Sheet exit | 180ms / exit | Return focus to the invoking control. |
| Significant in-place layout change | Up to 300ms / standard | Preserve reading position; skip if the content is already offscreen. |
| Navigation | Native platform transition | Do not replace Android Back or iOS swipe-back with custom choreography. |
| Loading | Native progress behavior | Indeterminate only while completion is unknown; never fake a percentage. |

No endless pulsing, bouncing balances, scroll-reveal delays, cascading entrance animations for every list item, parallax, or confetti after routine chores. Real-time updates must not continually reorder items while the person is interacting; preserve the active item and expose new state without losing context.

Respect platform reduced-motion/animation settings. In reduced motion, remove scale, slide, and animated layout travel; update immediately or use a brief opacity transition of at most 80ms when supported. Replace skeleton shimmer with static placeholders. Essential native loading progress remains understandable with a text status.

Use native haptic patterns sparingly for a confirmed completion or selection when useful and supported. Respect device settings and capabilities. No custom vibration sequences, automatic repeated pulses for overdue work, or haptic-only information. Never buzz on every form keystroke.

## 10. Component contracts

Every interactive component must define value, role, permission, default/pressed/focused/disabled/loading/error states where applicable, accessibility name and state, theme behavior, large-text layout, and recovery. Presentation cannot grant a permission that the repository denies.

### 10.1 Shared component tokens

Color references below abbreviate `hq.sys.color.*`. Metric references abbreviate `hq.ref.*`. Component paths expand under `hq.comp`.

| Component token | Value / alias |
|---|---|
| `button.primary.bg / fg / hover / pressed` | `action.primary.bg / fg / hover / pressed` respectively |
| `button.secondary.bg / fg / hover / pressed` | `action.secondary.bg / fg / hover / pressed` respectively |
| `button.secondary.border` | `selected.border` |
| `button.tertiary.fg / hover / pressed` | `action.tertiary.fg / hover / pressed`; background transparent at rest |
| `button.danger.bg / fg / hover / pressed` | `action.danger.bg / fg / hover / pressed` respectively |
| `button.disabled.bg / fg / border` | `disabled.bg / fg / border` respectively |
| `button.minHeight / paddingX / gap / radius` | `size.button` 52 / `space.5` 20 / `space.2` 8 / `radius.control` 12 |
| `button.label` | `hq.sys.type.label.large` |
| `field.bg / fg / placeholder / border` | `surface.base / text.primary / text.muted / border.control` |
| `field.focusBorder / errorBorder / disabledBg` | `focus / status.danger.fg / disabled.bg` |
| `field.minHeight / paddingX / radius` | `size.input` 56 / `space.4` 16 / `radius.control` 12 |
| `field.value / label / helper` | `hq.sys.type.body.large / label.medium / body.medium` |
| `card.bg / border / radius / padding` | `surface.base / border.subtle / radius.card` 20 / `space.5` 20 |
| `row.bg / pressedBg / minHeight / gap` | `surface.base / surface.subtle / size.row` 56 / `space.3` 12 |
| `chip.bg / fg / border` | `surface.base / text.secondary / border.control` |
| `chip.selectedBg / selectedFg / selectedBorder` | `selected.bg / selected.fg / selected.border` |
| `chip.visualMinHeight / target / radius` | 32 / `size.target` 48 / `radius.pill` |
| `badge.radius / paddingX / paddingY / label` | `radius.small` 8 / `space.2` 8 / `space.1` 4 / `hq.sys.type.label.small` |
| `navigation.bg / fg / selectedBg / selectedFg` | `surface.base / text.secondary / selected.bg / selected.fg` |
| `navigation.android.contentMinHeight / target` | 80 / `size.target` 48; add the actual bottom system inset once |
| `appBar.android.contentMinHeight / iconTarget` | 56 / `size.target` 48; add the actual top system inset once |
| `sheet.bg / radius / padding` | `surface.raised / radius.sheet` 28 / `space.5` 20 |
| `avatar.bg / fg` | `selected.bg / selected.fg` |
| `focus.color / width / offset` | `focus / border.focus` 2 / `border.focusOffset` 2 |

The numeric chip, Android navigation, and app-bar minima are component-specific metrics and should be exported at the named `hq.comp.*` paths; they do not replace the 48-unit hit target. Component dimensions grow with text. Border widths are 1 at rest and 2 for selected/focused emphasis; allocate space so the state change does not shift content.

### 10.2 Buttons and contextual create

Use one visually dominant filled action per decision region. Primary is teal; secondary is outlined; tertiary is a labeled low-emphasis action; danger is reserved for an actual destructive confirmation. Routine completion is a primary action, not danger.

Default buttons are 52 units minimum with a 48-unit minimum width. Icon-only actions use a 48-unit square target. The center create action uses a 56-unit visual container when the shell can accommodate it. Avoid floating controls that overlap the last list item.

| State | Required rendering and behavior |
|---|---|
| Default | Explicit verb: Create flat, Join flat, Save changes, Add expense. |
| Hover | Token variant for pointer devices only; no sticky touch hover. |
| Pressed | Token variant/native indication. Dark danger may retain its base fill and add the 8% foreground indication from `opacity.pressedOverlay`. |
| Focus | Visible 2-unit focus indicator with 2-unit surface-colored offset; never erased by hover/pressed/error. |
| Disabled | Dedicated disabled colors and semantic disabled state. Explain the unmet requirement near the action if it is not evident. |
| Loading | Keep width stable; progress icon plus meaningful label, such as “Creating…”; block duplicate activation. Do not fade the entire control. |
| Failure | Restore the actionable label, preserve input, and place the error near the affected region with retry. |
| Success | Navigate or update the relevant state after authoritative confirmation. Avoid a toast if the result is already clear. |

Focus is an independent layer, not a lower-priority state that disappears during loading or error. Do not disable an entire form merely because its submit request is in flight; protect fields whose edits would invalidate that request, with clear progress feedback.

### 10.3 Text fields and form structure

Fields have persistent labels, optional helper text, value/placeholder, and an adjacent validation message. A placeholder is an example, not the only label. Mark optional fields “Optional”; do not rely on an unexplained asterisk for required fields.

Use the appropriate keyboard, capitalization, autofill, and IME action. Invite codes can use uppercase visual formatting while preserving repository normalization. Email/password fields retain supported autofill, paste, password-manager behavior, and a labeled reveal-password control. Amount fields accept the locale’s decimal entry and validate against the actual currency precision.

Validate on submit and after a touched field loses focus; do not show an error before a person has had a chance to enter information. Clear an error when the corrected value is valid, not simply on the next keystroke. On failed submission, focus the first invalid field and make every error discoverable in reading order.

For asynchronous checks such as invite lookup, distinguish “Checking…” from “Not found.” A network failure must not become field-invalid styling. Read-only email remains legible and selectable, with an explanation if needed. Read-only is not disabled.

### 10.4 Choice controls, filters, and switches

- Radio rows represent a single mutually exclusive choice; checkboxes represent independent selections; switches represent a supported setting whose change can take effect immediately.
- The row can expand the target, but it must create one semantic control, not duplicate row and switch announcements.
- A selected chip uses selected fill, border, and check/state. Unselected chips keep a visible control boundary. “Clear filters” resets the draft or applied filters as explicitly labeled.
- Grouped filter sheets show current selections and **Show results** / **Reset**. Changes remain draft until applied unless the existing screen clearly uses immediate filtering. Back dismisses drafts predictably.
- Avoid horizontal chip strips that hide every selected filter offscreen. Wrap short collections; use a filter summary and sheet for larger sets.
- Biometric lock uses the existing preference and platform capability. Unsupported or unenrolled states explain the requirement; no simulated successful biometric enrollment.

### 10.5 Cards, list rows, and inline expansion

Cards group a coherent object or decision. A task row inside a task section does not need a second shadowed container. Use the same leading alignment for heading, description, and list content.

If a card opens details, give it an appropriate label and affordance. Independent actions inside it, such as Complete or More, retain separate non-overlapping targets. Do not make nested buttons trigger the parent navigation action.

Expansion preserves the item’s position and selected state. The expanded region contains frequency, rotation, and contextual actions; it does not introduce another full dashboard. A sheet or existing detail screen is allowed where richer content requires it. The close/back action returns to the invoking task and prior scroll position.

### 10.6 Task card and rotation strip

An ordinary task card contains name, when, assignee, a concise status, and Complete only if authorized. Use a small status badge rather than a full red background or a permanent red left rail on every item. Show an absolute due date in detail when relative language could be ambiguous.

During completion, keep the card stable and announce progress. On success, show Done and the new engine-supplied assignment where applicable; update the list without making the next focused item vanish. On failure, preserve the original responsibility and offer retry. If the server may already have committed, refresh before blindly repeating a write.

The rotation strip shows names in Now / Next / Then columns or stacked rows at large text. Mark Away with text and an icon. Expose the same sequence as a screen-reader description. When all members are away, show “Paused while everyone is away” and a neutral explanation; do not draw a fictional next member.

### 10.7 Balance summary, person balance, and settlement

Balance hierarchy: direction label → amount and currency → period → actionable person breakdown. Use `amount` only for the primary summary; individual rows use readable body/title text with tabular digits.

A person balance names the person and direction. “You owe Rahul ₹850” is clearer than a signed number beside an avatar. On settlement, show counterparty, amount, currency, and whether the person is recording a payment they made or money they received. Use **Record settlement** or **Mark received** in the confirmation where it prevents payment ambiguity; retain **Settle** as the concise entry label.

Partial amounts and maximums follow the verified repository behavior. The source strategy leaves over-settlement behavior unresolved; do not choose “cap” or “block” in the design layer. Show no two-sided-confirmation lifecycle until its backend is implemented. Duplicate taps, uncertain responses, and realtime updates require reconciliation with the ledger.

### 10.8 Discovery cards and detail

Vacancy card order: flat/listing title if supported → city/area → rent per head with currency → beds available → concise preferences → optional neutral trust tag. Looking card order: display name/initial → looking-in area → budget with currency → short bio/lifestyle → optional neutral trust tag. Omit absent optional data rather than invent a value or display “undefined.”

A compatibility explanation can say “Shared preference: quiet evenings” only when both underlying preferences exist. Never display “98% match,” distance, a verified shield, or a current-household membership badge for a seeker whose data does not support it.

Details carry the connection action; avoid multiple indistinguishable chat buttons on browse cards. Request submission shows Sent after confirmation. Inbox presents sender context and Accept/Decline with equal clarity. Accepted opens the existing in-app messaging path. Handle listing closed, request already handled, unavailable user, permission loss, and blocked state without a misleading live CTA.

### 10.9 Messages and safety actions

Conversation uses distinct outgoing/incoming surfaces with high-contrast text, readable timestamps, and actual supported delivery state only. Do not invent read receipts, typing indicators, media upload, or message deletion.

Show a concise safety banner: “Keep conversations in Habitiq. Share personal details only when you’re comfortable.” UI must not automatically reveal contact information or introduce external contact buttons. Report and Block remain available in a labeled menu. Report confirmation acknowledges submission only; it must not promise a trust change, ban, or review deadline. A block updates the UI after backend confirmation according to existing behavior.

### 10.10 Sheets, dialogs, and destructive decisions

Use a bottom sheet for contextual detail, filters, and short choices. Use a full screen for long forms and processes that need a persistent back path. Use a dialog for one consequential decision, not for ordinary task completion.

Sheets have a clear heading, accessible close/back behavior, scrollable body, and actions reachable above the keyboard/safe area. A drag handle is optional and decorative; dismissal cannot require dragging. If unsaved changes would be lost, use the platform-standard discard confirmation only after edits exist.

Destructive dialogs name the object, explain the verified consequence, and use a specific final action: Delete task, Leave flat, Delete account. Cancel is easy to reach. Avoid ambiguous “Yes,” preselected destructive actions, or typing a name for every minor deletion. Do not promise recovery unless it exists.

For irreversible account deletion, preserve existing reauthentication/error handling and explain the actual data/membership effect after verifying the implementation. Do not place account deletion on the first Profile screen.

### 10.11 Feedback and empty states

| Pattern | Use | Requirements |
|---|---|---|
| Inline helper/error | Field- or action-specific information | Close to cause; explicit recovery; screen-reader association. |
| Section error | One region cannot load | Preserve unaffected content and offer region retry. |
| Persistent banner | Offline/stale/permission issue | Remains until resolved; never uses a fleeting toast for essential information. |
| Snackbar | Short, noncritical confirmation | Readable duration; avoid repeating on listener reconnect. Actions must remain available elsewhere if time-sensitive dismissal would block access. |
| Skeleton | Known initial content shape | Matches real layout; hides fake content from accessibility; one concise loading announcement. |
| Empty state | Confirmed successful query has no items | State why, give one useful next step appropriate to role. |

Empty examples: member tasks → “No tasks assigned to you.” Admin tasks → “No tasks yet.” / Create task. Discovery filters → “No results for these filters.” / Adjust filters. No flat → Continue setup. A loading failure must never masquerade as “No tasks” or “All settled.”

### 10.12 App shell, identity, and navigation rows

Android's bottom navigation has a minimum 80-unit content area, with system inset added separately. A 56-unit center create control fits within that area without covering labels. Four destination targets remain at least 48 units wide; allocate the remaining space evenly and test the full layout at 320 units. The selected destination has an indicator, label, and selected semantic state. A badge uses an actual supported count; cap visual text only when the full count remains accessible.

The standard Android app bar is at least 56 units high before the top inset; a large screen title can occupy a separate content row. Use one back action in a nested screen and an explicit screen title. Do not add redundant back and close actions unless they have distinct understandable destinations. Large text may increase both bar and navigation content height; never clip a destination label to preserve a fixed bar.

Profile's 64-unit initial avatar supports the identity header. Member rows use 32- or 40-unit avatars, name, role, and optional You marker. Use the same neutral/selected avatar palette consistently; avatar color is not a trust or role score. Initials derive from the display name with Unicode-aware handling and a generic person fallback when the name is absent. The full accessible name remains the person's name, not the initial letters.

Navigation rows use a 56-unit minimum height, or 72 units for title plus supporting text, and grow with content. A disclosure chevron means opening another view; a toggle means changing a setting; a danger label means a consequential action. Do not reuse one ambiguous row treatment for all three. On iOS, native bars and row conventions take precedence over Android-only height tokens while maintaining the shared target and text requirements.

## 11. System states and resilience

### 11.1 Shared state model

Use typed view states instead of independent booleans that can create impossible combinations:

```text
Read: idle → loading → content | empty | error
Refresh: content → refreshing(existing content) → content | error(existing content)
Write: idle → submitting → confirmed | failed | outcomeUnknown
Access: authorized | unauthenticated | permissionLost
Connectivity: online | offline | reconnecting
```

These are UI states; they are not a new Firestore schema. Keep domain states such as task overdue, join pending, and listing paused separate from request-loading state.

### 11.2 Recovery rules

| Situation | Required response |
|---|---|
| Cold load | Show a stable loading scaffold; do not expose a dashboard for an unverified membership. |
| Refresh fails | Retain known content, mark it stale if appropriate, and offer retry. |
| Offline | Identify offline status. Allow reading cached data only if the existing client supports it and clearly distinguishes freshness. Do not show successful writes without confirmation. |
| App killed during create/join | Re-read account/membership/request state before routing or retrying. |
| Write outcome unknown | Explain “We couldn’t confirm this yet”; reconcile the specific object before resubmitting a potentially duplicate action. |
| Permission removed | Stop affected listeners/actions, refresh membership, and route safely. Never briefly expose another flat’s stale data. |
| Two devices edit/change a task | Reflect authoritative results and explain a conflict when the user’s requested action no longer applies. Do not overwrite silently from stale UI. |
| Empty result | Use an empty state only after a successful query. |
| Partial Discovery data | Render available safe fields; suppress missing optional attributes. An invalid/missing required field produces a controlled unavailable state. |
| Theme/window/font changes | Preserve current input, active destination, and visible task context. |

Do not add offline-write queues or background-sync promises as a visual feature. If an SDK locally emits pending writes, distinguish pending from server-confirmed success using supported metadata. Hide raw Firebase exception codes; retain useful diagnostics in the existing logging system without placing private data in user-visible errors.

## 12. Data visualization and numerical clarity

Current product priority is actionable summaries and breakdowns. Analytics is later in the Manage spec. The rules here prepare a consistent vocabulary; they do not authorize charts, scoring, or analytics endpoints that do not exist.

### 12.1 Choose the simplest representation

| Question | Preferred presentation | Avoid |
|---|---|---|
| What do I owe, and to whom? | Labeled amount + person rows + contributing expenses. | Pie chart of debt, unexplained signed total. |
| Who is responsible next? | Now / Next / Then sequence. | Circular wheel suggesting equal current ownership. |
| How many tasks need my attention? | Count + named status; link to actual list. | Completion percentage without a denominator. |
| Future comparison over categories | Horizontal bars with direct labels and values. | 3D bars, decoration, truncated baseline. |
| Future trend over time | Line with visible time range, units, and missing-data gaps. | Smoothed invented points or a claim of prediction. |

### 12.2 Chart tokens and accessibility

`chart.1–4` provide teal, blue, violet, and ochre in light; pale teal, blue, violet, and amber in dark. Use them as categorical identities, not good/bad rankings. Maintain the same category-to-token mapping across filters and themes.

On approved base/canvas chart surfaces, series marks must meet at least 3:1 against the plot background. Adjacent colored fills may not contrast sufficiently with one another: separate them, add visible outlines, or use texture/markers. Direct labels and an accessible value list are required; color alone is insufficient. More than four simultaneous categories should use a table or deliberate grouping with an explanation, not an unreviewed rainbow palette.

Use primary text for data values and secondary text for axes. Decorative grid lines can use `border.subtle`; grid lines must never be the only way to interpret a value. Bars begin at zero unless a clearly labeled domain-specific exception is reviewed. Never encode trustworthiness, reliability, gender, or debt through a prestige color hierarchy.

### 12.3 Formats

- Format with locale-aware APIs. English India examples: ₹1,200 and ₹1,20,000; include currency code where a symbol is ambiguous or currencies are mixed.
- Show sufficient decimal precision to explain the actual ledger. Do not hide a nonzero paisa balance as “All settled.” Avoid animated counting of monetary values.
- Keep missing, zero, and not-yet-loaded distinct. Use “Not available” for unavailable data, not `0` or an unexplained dash.
- Relative task dates can aid scanning; detail exposes the actual date. Use unambiguous dates such as “1 Oct 2026,” not “01/10/26” in mixed-locale contexts.
- Use the product’s existing due-date/timezone semantics. This document does not change the date at which a task becomes overdue. Display a timezone when it affects interpretation.
- Invite codes are selectable and copyable, with “Invite code copied” confirmation. Do not truncate the only usable copy of the code.

## 13. Content and voice

Habitiq speaks like a considerate, capable flatmate: short, specific, and direct. State the action or outcome, then the useful context. Use Indian English spelling in the current English UI: organise, cancelled. Localize complete messages rather than stitching translated fragments around names and amounts.

### 13.1 Vocabulary

| Use | Avoid |
|---|---|
| Flat, flatmate, member, admin | Workspace, tenant, operator, user entity in ordinary consumer screens. |
| Manage Flat (screen), Manage (tab) | Task administration console. |
| My Tasks / All Tasks | Personal workload module. |
| I’m away / Away | OOS in user-facing copy. |
| You owe / You’re owed / All settled | Debit exposure, settlement liability, green/red unlabeled totals. |
| Monthly bills / Shares ready | Instances, `split_generated`. |
| Find a flat / Find a flatmate | Two differently named tabs with the same unexplained result list. |
| Looking post / Vacancy | Generic profile/listing labels that conceal which type is being edited. |
| Request sent / Waiting for approval | Joined, matched, or welcome-home copy before membership exists. |

### 13.2 Copy patterns

| Situation | Preferred copy |
|---|---|
| Create succeeds | “Your flat is ready.” / Share invite code / I’ll do this later. |
| Join pending | “Request sent. You can join once the flat admin approves it.” |
| Invalid invite | “We couldn’t find that flat. Check the code and try again.” |
| Flat full | “This flat is full. Ask the admin for help.” |
| Already a member | “You’re already a member of this flat.” |
| Invite lookup network failure | “Couldn’t check the code. Try again.” |
| Task overdue | “Was due yesterday” with the responsible person. |
| Completion fails | “Couldn’t complete this task. Try again.” Use an uncertain-outcome message if commitment is unknown. |
| Everyone away | “Paused while everyone is away.” |
| Discovery visibility | “When this is on, your looking post appears in Find a flatmate.” |
| Report recorded | “Report submitted.” Add next steps only if the moderation process is documented. |
| Sign out confirmation | “Sign out of Habitiq?” / Cancel / Sign out. |

Use specific verbs and sentence case. Avoid “Oops!”, blame, guilt, urgency without a deadline, exaggerated delight, “100% safe,” “AI-powered” without capability evidence, or “Payment successful” for a manual record. A due task is a shared responsibility, not a moral judgment.

Every status message should answer what happened and, when necessary, what the person can do. Do not expose collection names, IDs, exception codes, or internal states as instructions. Invite codes are the deliberate exception because they enable joining.

### 13.3 Localization readiness

Reserve 30–40% text expansion in ordinary controls as a planning allowance, then test real translations. Use plural resources for members, tasks, days, and people; never assume English concatenation. Preserve Unicode names. Mirror layout through platform RTL support, and test amount/code direction in otherwise RTL sentences. Language support must not appear in Profile before the product supports it.

## 14. Accessibility requirements

Accessibility is a component requirement and a release gate. WCAG 2.2 AA is the baseline reference for applicable web-equivalent criteria; native assistive technology and platform conventions are additional requirements. This document is not a claim of application conformance.

1. **Touch:** all app-controlled interactive targets are at least 48 × 48 logical units on both platforms. This common product standard exceeds the familiar 44-point iOS minimum and follows [Android’s 48dp guidance](https://developer.android.com/guide/topics/ui/accessibility/apps). Targets must not overlap; a small visible chip can sit within a larger target.
2. **Contrast:** use the approved semantic pairs; every informative text element meets 4.5:1, and essential boundaries/indicators meet 3:1. Never lower text opacity to establish hierarchy. [Non-text contrast guidance](https://www.w3.org/WAI/WCAG22/Understanding/non-text-contrast.html) applies to essential visual cues.
3. **Scaling:** test Android at 100%, 130%, and 200% font size plus enlarged display settings; test iOS at default and the largest accessibility Dynamic Type sizes. Reflow rather than clip or suppress controls.
4. **Names and roles:** every actionable control has an accurate name, role, value, and state. Selected tabs, toggles, expanded cards, read-only fields, and loading actions expose those states.
5. **Traversal:** reading/focus order follows the visual task. Group a card’s descriptive content logically while keeping independent actions separately reachable. Announce task name, due state, and assignee without repeating decorative badges.
6. **Focus:** keyboard, switch, and screen-reader focus remains visible and is not obscured by sticky bars or the keyboard. Opening a modal moves focus into it; closing returns it to the invoker or a sensible surviving neighbor.
7. **Announcements:** use polite updates for task completion and refreshed results. Use urgent alerts only when immediate action is necessary. Avoid repeated announcements on every realtime snapshot.
8. **Errors:** associate inline errors with their fields; focus invalid input after submit. Provide a recoverable action and preserve data. No color-only validation.
9. **Gestures:** every swipe, drag, long press, or gesture-only action has a visible/tappable alternative. Native Back and iOS interactive back continue to work.
10. **Authentication:** allow paste, autofill, and password managers. Do not add memory puzzles or manual transcription barriers to ordinary authentication. Preserve existing authentication methods.
11. **Motion:** respect system settings; no flashing feedback, required animation, or haptic-only communication.
12. **Timing:** essential errors and required actions persist. Noncritical transient feedback must be readable with accessibility settings; core recovery cannot depend on tapping a disappearing toast.

Verify with TalkBack on Android and VoiceOver on iOS, including modal boundaries, connection actions, currency reading, invite-code copying, and the outcome after a task disappears from My Tasks. Automated scans support, but do not replace, these checks.

## 15. Platform adapters

### 15.1 Android now

Preserve the current Kotlin/Compose repositories and navigation architecture. Build a `HabitiqTheme` wrapper exposing resolved colors, typography, dimensions, and component styles. Keep additional roles such as warm accent, task status, and chart colors in a typed extension rather than assigning misleading meanings to standard Material roles.

| Compose/Material role | Habitiq role |
|---|---|
| `primary` / `onPrimary` | `action.primary.bg` / `action.primary.fg` |
| `primaryContainer` / `onPrimaryContainer` | `selected.bg` / `selected.fg` |
| `background` / `onBackground` | `canvas` / `text.primary` |
| `surface` / `onSurface` | `surface.base` / `text.primary` |
| `surfaceContainerHigh` | `surface.raised` |
| `surfaceVariant` / `onSurfaceVariant` | `surface.subtle` / `text.secondary` |
| `outline` / `outlineVariant` | `border.control` / `border.subtle` |
| `error` / `onError` | `action.danger.bg` / `action.danger.fg` |
| `errorContainer` / `onErrorContainer` | `status.danger.bg` / `status.danger.fg` |

Fill other required library roles deliberately within the adapter and test any Material component that consumes them. Do not let an unconfigured default introduce purple or arbitrary dynamic colors. Wallpaper-derived dynamic color is off by default to preserve these brand and contrast pairings; adopting it later requires a complete reviewed role mapping.

Use native text selection, sheets, pickers, share behavior, accessibility semantics, and minimum touch target APIs. Preserve Back/predictive-back behavior supported by the current app. Use state restoration for presentation and form drafts where safe; authoritative membership and money state still comes from existing repositories.

A generated Android name maps `hq.sys.color.action.primary.bg` to a typed property such as `HabitiqColors.actionPrimaryBg`. Generate `Color(0xFF0F766E)` for sRGB `#0F766E`; do not accidentally interpret hex as RGBA. Font, density, and window adaptation belong in the theme/layout layer, not in repository logic.

### 15.2 iOS later

Use the same semantic names, content hierarchy, privacy rules, and domain state model. Adapt typography to native system styles, overlays to native sheets/dialogs, and navigation to iOS conventions. Share semantics rather than Kotlin implementation details.

Keep four primary destinations. The contextual create action remains available, but may move to the native toolbar instead of a nonstandard fifth tab. Do not turn the create action into a selected destination. Use native share sheets, date pickers, keyboard avoidance, VoiceOver, Reduce Motion, increased contrast, and Dynamic Type. Respect swipe-back and safe-area behavior.

Generate semantic color assets with light/dark variants and Swift properties that reference those names. Avoid choosing colors ad hoc from `UIColor` defaults. Platform components may use their own shape or transition when necessary for familiarity; record the exception and retain hierarchy, accessibility, and brand color roles.

### 15.3 Shared implementation boundaries

The system does not require React Native, a web view, a new component library, a new backend collection, or a new animation engine. Use the existing architecture and expose a thin token adapter. Website/marketing redesign is outside this document’s implementation scope.

For design tooling, create one primitive collection, one semantic color collection with Light/Dark modes, and component properties for state/size/variant. Typography and spacing should mirror the named tokens. Screen frames are compositions of shared components, not the source of independent colors or dimensions.

## 16. Governance and migration

### 16.1 Ownership and change control

Sai owns product language, visual intent, and design approval. Bhanu owns adapter correctness, data/permission integrity, performance, and release readiness. A visual change that changes field meaning, feature availability, persistence, permissions, or membership must be treated as a product/engineering change, not a token edit.

Version this system independently of the app. **Major** versions remove/rename semantics or materially alter behavior/identity; **minor** versions add compatible components/tokens; **patch** versions clarify or fix without changing the intended contract. Keep a dated change log and a migration note for breaking changes.

Every new token needs a purpose, layer, type, both-theme values when applicable, approved foreground/background pairing, and a real consumer. Every new component needs anatomy, variants, states, permission contract, accessibility behavior, and a large-text example. Reject one-off tokens named after a single screen when an existing semantic role fits.

### 16.2 Migration from `MASTER.md`

| Legacy item | Replacement | Migration caution |
|---|---|---|
| `--primary`, `bg-brand-600`, indigo primary | `action.primary.bg` and companion foreground/state tokens | Replace complete role pairs, not a global hex substitution. |
| Violet/indigo decorative gradients | Neutral surfaces with a restrained coral/teal brand detail | Remove gradient/glow dependency from functional controls. |
| `--background`, `--card`, dark surface guesses | `canvas`, `surface.base`, `surface.raised`, `surface.subtle` | Check navigation, dialogs, fields, and system bars together. |
| `text-white/35`, `text-white/25` | `text.muted` or `text.secondary` | Check every label against its actual surface. |
| One destructive color for every theme/use | `action.danger.*` plus `status.danger.*` | Button foreground differs from error text roles. |
| 10–11px bold badges | `label.small` minimum and scalable padding | Ensure labels grow and retain full meaning. |
| 44px button rule labeled “WCAG” | 48-unit target standard, 52-unit main button | Touch targets are platform/product decisions, not a blanket statement of WCAG minimum. |
| One radius for every control | Functional radius scale | Update shared components before individual screens. |
| Lucide React assumed in native | Reviewed vectors / native platform symbols | Preserve meanings and accessibility labels. |
| Forced dark Login/Onboarding | System-resolved theme | No new Profile appearance setting. |
| Desktop-first breakpoint rules | Native window-width classes | Retain usable narrow-window and large-text layouts. |

Migration sequence:

1. Inventory current theme definitions, components, navigation, source versions, and behavior tests. Record any specification/backend mismatch.
2. Add the token registry and platform adapter; verify light/dark contrast, font scaling, system bars, and focus before migrating screens.
3. Migrate shared controls, fields, rows, sheets, and status components. Keep business repositories untouched.
4. Migrate authentication/onboarding while preserving routing, pending approval, retry, and duplicate-create guards.
5. Migrate Home and Manage hub; then task list/completion, details, away, swaps, admin controls; then balance-first expenses, splits, settlements, bills, collection, month close, and activity. Analytics remains last and out of scope until supported.
6. Migrate Profile with identity separation and existing settings; then Discovery with its own flags, data boundaries, and connection/report rules.
7. Run role, state, theme, accessibility, and business-regression checks. Roll out through the existing controlled release process.
8. Remove deprecated visual aliases only after all consumers and snapshot examples are migrated. Preserve source documents as historical/product references.

Temporary aliases may map old semantic names to new ones for one migration cycle. Do not leave both old and new themes active on different screens. Aliases require an owner and removal milestone.

### 16.3 Definition of ready and done

**Ready:** the component or screen has a supported job, known data sources, permission contract, complete state list, responsive behavior, and agreed token mapping. Unavailable fields are identified before a polished mockup encourages implementation of a fictional feature.

**Done:** implementation uses shared tokens; both themes work; accessibility and role/state cases pass; repository behavior is preserved; screenshots and test results identify the app build/device/configuration; source and design-library versions match. A beautiful static frame is not sufficient evidence.

## 17. QA and release acceptance

### 17.1 Visual and interaction matrix

| Dimension | Required coverage |
|---|---|
| Android width | 320, 360, 390/412 logical widths; landscape; at least one 600+ and one 840+ window if supported. |
| Hardware | A real lower/mid-range Android device and a representative larger phone; emulator coverage alone is insufficient for touch/keyboard/performance. |
| Themes | Light and dark on every core screen, every overlay, system bars, keyboard transition, skeleton, error, and disabled state. |
| Type | 100%, 130%, 200%; long names/task titles; large amounts; multiline validation; enlarged display size. |
| Access | TalkBack, external keyboard/switch traversal where supported, reduced motion, visible focus, non-overlapping targets. |
| Content | Zero/one/eight members, no photo, missing optional data, empty versus failed query, long invite code, mixed currencies, Unicode names. |
| Network | First load failure, offline, reconnect, stale cache, write timeout, permission rejection, realtime change while a sheet is open. |
| Roles | Admin, member, expense creator, non-creator, collector, self-marking member, no-flat user, multi-flat user, removed member. |
| iOS release later | Small and large iPhone, light/dark, VoiceOver, largest accessibility type, Reduce Motion, keyboard, safe areas, native back/sheets. |

### 17.2 Product regression acceptance

- [ ] Google/email users with no flat reach the three intents; existing active-flat users reach Home without an onboarding flash.
- [ ] A failed active-flat lookup offers retry; it never creates the appearance of a new account.
- [ ] Manage my flat → create → skip invitation → Home. Find a flatmate → explanation → create → Discover, without auto-publishing.
- [ ] Join preview handles not found, full, already member, and approval. Pending approval never routes to Home.
- [ ] Double-tap Create is guarded; restart after create reconciles to Home; uncertain outcomes do not offer blind duplicate creation.
- [ ] Leave/switch uses existing membership behavior; no content or listeners leak across flats.
- [ ] Members enter My Tasks; only authorized users see task creation/edit/delete/override.
- [ ] One confirmed completion advances rotation once; two-device completion does not produce duplicate advancement in the engine.
- [ ] Overdue remains assigned; away skip and return position match the engine; all-away pauses without a crash.
- [ ] Swaps retain pending/accept/decline/transfer behavior and only authorized response actions.
- [ ] Expenses opens with understandable direction and person breakdown; balances agree with existing web/native calculations.
- [ ] Equal/custom splits preserve rounding; invalid totals cannot save. A ₹1,000 three-way split still sums to exactly ₹1,000.
- [ ] Settlement/received records are not presented as transfers; partial and uncertain outcomes preserve ledger integrity.
- [ ] Bills preserve payer/collector distinction, self-mark rules, generated expense behavior, skipped state, and admin-only mutation.
- [ ] Month close remains secondary; repeated close cannot double carry-forward; closed-period behavior remains unchanged.
- [ ] Currencies remain separated without verified conversion; no nonzero rounded balance is falsely labeled settled.
- [ ] Profile edits account name only, keeps email read-only, uses initial avatar, and separates Discovery visibility from account identity.
- [ ] Sign out, biometric preference, no-flat, multiple-flat, and deletion failure/reauthentication paths remain usable.
- [ ] Discovery exposes no exact address, distance, private flat records, account email, external contact action, or invented demographic fields.
- [ ] Vacancy and looking posts retain their flags and roles; connect → decision → conversation works without joining the flat.
- [ ] Trust tags have no invented threshold or numeric score. Reporting does not silently alter trust.
- [ ] Post paused/closed, blocked user, request already handled, revoked access, and flag-disabled states suppress invalid actions.

### 17.3 Technical and visual gates

- Registry parses; all aliases resolve without cycles; Light/Dark semantic keys match; no duplicate generated platform names.
- Contrast checks cover text pairs, all button states, input boundaries, selection/focus, statuses, and chart marks on their approved backgrounds. Review actual composited pixels when transparency is used.
- No uncontrolled raw colors, spacing, or typography in app components. Exceptions such as a supplied logo asset are recorded.
- Screen captures show no clipping, overflow, inaccessible bottom controls, misleading ellipses, layout jumps, or mixed legacy/new styles.
- Navigation, keyboard, permissions, realtime listeners, and error mapping remain functional. No accidental schema or business-math change is included in a visual migration.
- Scrolling and common transitions remain responsive on the chosen real device; profile frame timing on actual task/expense lists. Do not declare performance from a static mockup.
- Production connection/report rules are deployed and tested before enabling dependent Discovery clients. Permission enforcement is tested at the backend, not merely by hiding UI.

**Verification status for this document:** the canonical registry parses; both themes contain the same 51 semantic color keys; all aliases resolve; 136 foreground/background checks meet their specified text or non-text thresholds. The six source specifications and both visual references are reconciled here. These are document-level checks. No Android APK, iOS build, rendered screen, or live backend was tested while authoring this file. The release checks above remain implementation acceptance criteria.

## 18. Anti-patterns

| Reject | Replace with |
|---|---|
| White text on raw coral/teal brand anchors | Approved functional shades and foreground pairs. |
| Faint white-opacity captions in dark mode | Explicit accessible secondary/muted text tokens. |
| Coral used for both celebration and debt/error | Separate brand warmth and semantic status roles. |
| Indigo/violet controls surviving on isolated screens | One complete semantic theme migration. |
| Gradient buttons, neon shadows, glass panels over financial data | Solid controls, readable surfaces, restrained elevation. |
| Everything in a floating card | Continuous layout with grouping at meaningful boundaries. |
| Tiny badges, fixed-height clipped labels, icon-only unfamiliar actions | Scalable type, growing containers, clear labels. |
| A central plus that always opens Add task | Context-specific authorized creation. |
| Profile as an admin/settings dump | Identity-first hierarchy and contextual My Flat controls. |
| Account and Discovery edits combined | Separate forms and visibility explanations. |
| Fake upload, settings, payment, location, or AI capabilities | Supported fields and truthful available actions. |
| Numeric trust, compatibility percentages, person leaderboards | Qualitative, explainable signals with current consent rules. |
| Match treated as membership | Separate connection and invite/join flows. |
| Empty state displayed on network failure | Explicit error/retry or stale-content state. |
| Optimistic success for unknown financial/membership outcomes | Pending feedback and authoritative reconciliation. |
| Auto-reassigning an overdue task in the UI | Engine-derived responsibility and state. |
| Routine task completion behind a confirmation dialog | One-tap completion with clear progress and recovery. |
| Unlabeled red/green balances or combined currencies | Explicit direction, counterparty, and currency. |
| Public-shaming overdue copy | Neutral due language and an actionable next step. |
| Color-only selection or trust badges that imply safety | Additional state cues and exact supported language. |
| Blocking animation, bouncing numbers, repeated success toasts | Short purposeful feedback after confirmed outcomes. |
| Platform imitation that breaks native Back, text size, or sheets | Shared brand semantics with native interaction conventions. |
| New backend rules smuggled into a visual refactor | Separate, explicit product/engineering decisions. |

## 19. Change log

| Version | Date | Change |
|---|---|---|
| 2.0.0 | 1 October 2026 | Complete mobile-first revamp of `MASTER.md` v1.1. Introduces coral/teal identity, the requested 60–30–10 composition and practical UI-principle rules, accessible light/dark semantics, native type/layout adapters, three-layer tokens, role-aware components, explicit source precedence, privacy and behavior preservation, governance, and release acceptance. |

**System standard:** every screen should make the next useful action clear, every state should tell the truth, and every visual decision should support life in a shared home.
