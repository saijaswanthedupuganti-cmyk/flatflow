# Habitiq implementation handoff — 26 September 2026

Status: PLAN ONLY. Product source, backend deployments, and APKs were not changed in this planning pass.

Prepared against working tree based on commit `52b4a1a118c7a481e166561e71918d389a55ea5e`. The tree contains substantial uncommitted work. Commit identity alone is not the baseline: compare the paths and SHA-256 values in [source snapshot](evidence/source-snapshot.csv) before executing a packet.

## Outcome

Deliver one calm, coherent Habitiq experience, with the user's exact teal palette, preserved supplied artwork, consistent system insets and components, truthful async states, and complete supported journeys. Android is the first implementation target. Responsive web is a separate client with the same conceptual rules. iOS is a future native delivery, not a claim that a Windows-produced APK supports iPhone.

The next model must execute bounded packets, not interpret “make everything professional” as permission to redesign the product or replace its backend. A successful build is necessary but insufficient: publication, permissions, photo rendering, system back, small screens, and accessibility need their own evidence.

## Read order and handoff

1. [Evidence, conflicts, and release blockers](01-EVIDENCE-AND-DECISIONS.md)
2. [Exact design-system contract](02-DESIGN-SYSTEM.md)
3. [Ordered implementation packets](03-IMPLEMENTATION-PACKETS.md)
4. [Screen, flow, state, and data contracts](04-FLOWS-AND-DATA.md)
5. [Icon, splash, loader, and motion specification](05-BRAND-AND-MOTION.md)
6. [QA cases and release gates](06-QA-AND-RELEASE.md)
7. [Web and future iOS handoff](07-PLATFORM-CONTINUITY.md)
8. [Copy-ready next-model instructions](08-EXECUTOR-PROMPT.md)
9. [Requirement-to-packet coverage](09-REQUIREMENT-TRACEABILITY.md)

All paths in the packet tables are relative to the repository root. `K` means `apps/android/app/src/main/kotlin/habitiq/app`, `U` means `K/ui`, and `DS` means `U/theme`. These aliases are a reading aid, not directories to create.

## What is binding

- Latest direct user palette is binding. Replace conflicting coral/violet UI color ownership; keep the existing shared owners and migrate their consumers.
- Existing functioning task, rotation, expense, bill, membership and auth behavior must survive. Security rules define server enforcement, not the full desired product behavior.
- Earlier explicit product IA remains four roots: Home, Discover, Manage, Profile. Manage has Tasks and Expenses; Expenses contains Daily splits and Monthly bills.
- One attached QA gate lists six roots (adding Post and Messages), contradicting the earlier strict four-root contract and current implementation. A clarification was requested. Until the user explicitly selects six, retain the current four; do not change navigation on an inferred answer. Messages is a secondary Discover destination. Record any later answer in the decision log before dependent work.
- Post is contextual. Exactly one creation entry per active context. My posts belongs under Profile and can be reached from Discover. No universal plus menu.
- Supplied assets are preserved. The no-gradients UI rule does not authorize flattening gradients already baked into the supplied artwork.
- Google Maps expansion, fabricated ratings/verification, percentage compatibility, payments/booking, a new REST backend, a desktop executable, and app-store publication are not this correction pass.

## Reconciliation with the 25 September plan

This packet set is the active delta to `docs/archive/audits/2026-09-25/`, not a competing product specification. Use older documents for historical product/rule/finance questions and recheck their claims before acting. This plan supersedes their coral visual direction, unresolved light/violet onboarding exception, and global-add suggestion. Their Android/web/iOS separation and regression principles remain useful. Do not copy the older runtime screenshots into a new QA report as current evidence.

## Execution sequence

| Packet | Outcome | Requires |
|---|---|---|
| P00 | Reproducible baseline, fixture accounts, screen/state ledger | None |
| P01 | Single teal token owner; no accidental legacy palette | P00 |
| P02 | Insets, standardized headers, focused-flow shell and back behavior | P01 |
| P03 | Shared controls, chip semantics, readable states | P01–P02 |
| P04 | Exact artwork, white launch, restrained loader/motion | P01–P03 |
| P05 | Household/Home/Profile/auth presentation and truthful setup | P02–P04 |
| P06 | Discover cards, readers, filters, and preview agree | P03, schema inventory from P00 |
| P07 | Durable posting drafts and real photo lifecycle | P02–P03, P06 projection contract |
| P08 | Confirmed requests, chat permissions, management states | P03, P06–P07 |
| P09 | Matching responsive web corrections | Android contracts accepted; web baseline |
| P10 | Integrated device, backend, accessibility and regression gate | All shipped packets |

Do not start another packet with an unexplained failed check. If platform access prevents a test, mark it BLOCKED and continue only independent work. Do not use blocked evidence as a pass. Each packet has exact owners and observable acceptance in document 03. Mark substeps individually when a packet exceeds one context window.

## Completion meaning

“Implemented” = code and focused checks exist. “Verified” = recorded runtime evidence covers the specified case. “Release-ready” = all applicable gates in document 06 pass. “Deployed” = separately observed environment deployment. “iOS ready” requires a real Apple build, signing and device validation; a plan does not meet it.

Previous conversation reports included unit-test and APK successes. This pass did not rerun them. It found remaining source-level gaps, so the previous APK must not be described as having passed this new contract.

## Delivery artifacts for the executor

Maintain `execution-status.md` with packet, changed paths, check results, evidence paths, open blockers, and next action. Save screenshots/videos/log summaries under `evidence/runtime/` with platform/device/API/viewport/font-scale/state identifiers. Produce release notes listing configuration still needed, exact APK SHA-256, and test limitations. Do not overwrite `Habitiq-FINAL-debug.apk` with an unverified build and imply “final”; use a dated candidate filename until the gate passes.

No numeric delivery-time promise is made: backend fixtures, actual device access, and unresolved data contracts determine effort. Foundation packets are smaller; photo lifecycle, state restoration and backend rules are the high-risk work and require stronger review even if a lower-cost model writes the code.

## Planning-delivery validation

The 10 planning documents have no missing local links. All 232 source files in the captured snapshot still match their hashes after this planning pass. See [validation record](evidence/plan-validation.txt). This checks the handoff and source preservation, not app behavior; runtime/build/device tests remain unrun in this pass.
