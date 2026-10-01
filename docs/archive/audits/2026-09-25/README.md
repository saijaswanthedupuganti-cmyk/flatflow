# Habitiq: product experience audit and implementation handoff

Prepared 25 September 2026 against working tree based on `52b4a1a118c7a481e166561e71918d389a55ea5e`.

**Recommendation: make the website, sign-in and household dashboard one recognizable Habitiq experience, preserving the latest features.** Use the clarity of the recent Airbnb-inspired direction while developing an original composition and identity. Fix account routing and real-data gaps alongside that work; visual consistency alone cannot resolve them.

This audit follows your request to improve the thinking, architecture, flows, forms, visual design, and motion. The supplied blue login screenshots are historical improvement references, not approved pixel targets. The current web login is a dark modal, and the native application has a separate Compose implementation.

## Read in this order

**Start with [the Android-first master blueprint](00-ANDROID-FIRST-MASTER-PLAN.md).** It consolidates the complete product jobs, discovery/person flows, Manage → Tasks/Expenses, deferred Maps scope, implementation stages and release gates. The documents below provide supporting detail and evidence.

1. [Verified findings and evidence](01-audit.md)
2. [Target information architecture and flow contracts](02-architecture-and-flows.md)
3. [Implementation packets for the next model](03-implementation-packets.md)
4. [Visual and motion specification](04-visual-specification.md)
5. [Acceptance and verification matrix](05-verification.md)
6. [Original brand direction and dashboard continuity](06-brand-and-dashboard-continuity.md) — incorporates your latest requirements; read this before implementing visual changes.
7. [Native iOS implementation handoff](07-IOS-NATIVE-HANDOFF.md) — maps the same product contracts to SwiftUI without claiming an unbuilt iOS binary.
8. [Condition and Firestore persistence matrix](08-CONDITION-AND-PERSISTENCE-MATRIX.md) — the use-case ledger for success, failure, permissions, restart, offline and duplicate actions.

## What matters first

| Priority | Outcome | Why it matters |
| --- | --- | --- |
| P0 | Real listings and truthful trust signals | Current public cards present hardcoded people, ratings and reviews without identifying them as sample content. |
| P0 | One reliable route after authentication | Invitation, discovery, account loading and no-flat states need explicit routing; errors must not mean “new account.” |
| P0 | Consistent balances across clients | Native personal balances combine amounts without currency separation and omit exclusions used by the web calculation. |
| P0 | Working, protected conversations | Native message queries and checked-in rules disagree; errors become empty conversations. |
| P1 | One navigation model | “Manage Flat” currently means household tasks/expenses on Android and admin settings on web. |
| P1 | Accessible forms and deliberate overlays | The web auth form has no persistent field labels or password recovery; several custom overlays lack dialog behavior. |
| P2 | Consistent visual polish | Shared spacing, type, restrained status treatment, and motion must follow the corrected flows. |

P0 here means resolve before presenting the affected feature as reliable to users. It is not a claim that a production incident was reproduced.

## Change implemented during this audit

You separately asked to improve the loading animation. That request was implemented in `components/HabitiqLoadingScreen.tsx` and its CSS module, consumed by `components/AuthProvider.tsx`.

- Stable centered logo, cropped within its existing transparent asset bounds.
- White mark on the current coral brand tile; restrained shadow.
- A slim indeterminate progress indicator instead of orbiting particles.
- Reduced-motion variant uses opacity without translation.
- Screen-reader loading status; no artificial delay added to authentication.
- Focused lint check passed. Desktop 1280 × 720 and mobile 390 × 844 were visually inspected; mobile document width was 390 px with no horizontal overflow.

Authentication and routing logic were not changed by that visual request.

## Evidence and limits

The working tree already contained extensive web and Android edits. Those edits were treated as the current implementation, not overwritten or reverted. `evidence/source-inventory.csv` records 166 source files with line counts and hashes; inventory does **not** mean every line was manually reviewed.

The review combines saved product specifications, targeted source tracing, supplied screenshots, and local web inspection. A copied app running with its existing mock mode was used for web interaction checks. A ₹100 equal split across four mock members was entered successfully; the resulting receivable increased by ₹75. No production records were edited.

No Android device was connected. Native screen behavior and backend issues are source findings pending device/emulator confirmation. Historical APK QA reports were not treated as current passes. Real Google authentication, rules deployment, multi-device concurrency, offline recovery, production data correctness and complete device coverage remain unverified.

**This is an actionable audit and design handoff, not a claim that the entire product has passed a pixel-perfect or line-by-line certification.** Pixel acceptance requires approved target screens for each state and device; the existing login screenshots cannot define targets for the whole application.

## Product decisions to settle before implementation

These are narrow decisions, not reasons to restart the project:

- **Discovery without a flat:** the August onboarding spec requires creating a flat before finding a flatmate; the newer web homepage invites people to find rooms before joining anything. Recommended direction: a seeker can browse and maintain a looking profile without inventing a household; publishing a vacancy still requires household admin rights. This is a proposed product change, not an existing agreed contract.
- **Visual identity:** use an original Habitiq expression of the current coral/light direction consistently from website through dashboard. Document 06 defines differentiation and continuity. Native dark onboarding is an existing intentional exception to reconcile in the proposed screen set.
- **Desktop application:** treat responsive web plus installed PWA as the current desktop scope. A separate Windows/macOS executable, and native iOS, are new delivery scopes, not implied existing implementations.
- **Discovery availability:** either connect real supported data or visibly label the public experience as a demonstration. Do not ship an apparently operational booking marketplace backed by constants.

The remaining packets can proceed independently of these decisions where stated. A lower-cost model should execute one packet at a time and return evidence before moving on.
