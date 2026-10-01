# Next-model execution instructions

Copy the text below into the implementation task. It authorizes code work for that future run; this planning run did not edit product source.

---

Implement the Habitiq correction plan in `docs/delivery/2026-09-26-implementation/README.md` and documents 01–07. Work from the repository root. Read the plan and relevant repository instructions before editing. Start with P00, then execute dependency order P01–P10. Do not replace this with another general redesign plan or a set of disconnected demo screens.

Use the exact user-supplied teal design tokens in document 02. Preserve the artwork in `assets/brand/HABITIQAPP.svg`; document 05 explains that it wraps a raster image. Do not trace/recolor it, introduce a second palette, flatten its embedded gradients or invent an alternative logo.

Use the current working tree, including existing uncommitted work. Compare against `evidence/source-snapshot.csv`, inspect relevant changes and preserve unrelated edits. Do not reset, revert or regenerate whole screens blindly. Existing task/rotation/expense/bill/membership/auth behavior must survive.

The working navigation contract is Home, Discover, Manage, Profile. A six-tab attachment conflicts with that contract; check whether the user has answered the decision recorded in document 01. Until explicitly changed, keep four. Contextual creation only; Manage contains Tasks and Expenses, and Monthly bills stays inside Expenses. Preserve supported existing web routes through a documented mapping.

Execute one bounded packet at a time. For each packet:

1. Read its current source owners and related callers/tests. If evidence has drifted, update the evidence/decision note rather than coding against stale assumptions.
2. Record concrete substeps and affected paths in `execution-status.md`. Use existing architecture/components. Read installed Next.js guides before any Next code changes.
3. Implement the packet and meaningful regression checks. Keep data migrations/rules changes explicit and coordinated across readers/writers. Never substitute fake success, local-only data, hard-coded recent searches, fabricated trust/ratings or decorative stock photos for a functioning backend path.
4. Run focused checks and inspect runtime screens/states on an available test environment. Use synthetic fixtures and emulator/test backend for mutations; do not publish listings or message real people for QA. Do not expose `.env`, tokens or private records in logs.
5. Record changed files, commands, observed outcomes, screenshots/videos, remaining blockers and exact next action. A failed or blocked check is not a pass. Fix packet regressions before dependent work; independent work may continue with the blocker explicitly tracked.

Prioritize the confirmed blockers: listing writer/reader mismatch for photos and room type; premature request success; durable draft/photo upload lifecycle; truthful profile/setup completion; multi-household stale state; error-to-empty conversion; post lifecycle action mismatch; privacy promise versus actual access rules. The source evidence is in document 01 and behavioral contracts in document 04.

Do not claim all use cases passed after a build. Execute document 06 case ledger against the delivered candidate. Inspect full mobile/desktop renders, small screen, large text, keyboard, system Back, system bars, empty/error/offline states and reduced motion. Test money, task, bill and membership regressions. Treat exact-address exposure or wrong-household writes as release blockers.

P09 covers existing web continuity. If full Discover parity is absent, implement it only as explicit additional subpackets with the same contracts; never call it a CSS-only fix or silently mark parity done. iOS remains a future native phase under document 07; do not claim an APK is iOS-ready, create a nonfunctional wrapper, or claim signing/device QA without access and evidence.

Do not request permission for routine reversible code fixes already authorized by this task. If a real unresolved decision blocks dependent work, ask one focused question and continue independent work. Follow actual tool/filesystem approval requirements for restricted operations. Do not deploy backend rules, migrate live data, publish stores, or externally share source as an incidental build step.

At the end, deliver a concise outcome with links to the completed status/test ledger, candidate artifact and hash, screenshots, remaining limitations and platform/deployment status. Use a dated candidate APK until release gates pass. Never label the entire product “complete,” “production-ready,” or “all-platform verified” if required cases remain unrun or blocked.

If context is running low, save a precise checkpoint with current packet/substep, changed paths, last checks, outstanding defects and next commands; continue from that checkpoint rather than restarting or asserting completion.

---

## Suggested execution-status skeleton

Create this only when implementation starts; it is intentionally not prefilled with success claims.

| Packet/substep | Status | Changed paths | Checks and evidence | Blocker/decision | Next action |
|---|---|---|---|---|---|
| P00 | NOT STARTED | — | — | Confirm environment and fixtures | Read current source and baseline |

Allowed statuses: NOT STARTED, IN PROGRESS, IMPLEMENTED / UNVERIFIED, VERIFIED, BLOCKED. Record release and deployment status separately. Every VERIFIED row needs linked evidence. Another model can continue the same ledger without repeating completed work or losing unresolved conditions.
