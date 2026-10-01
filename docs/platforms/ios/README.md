# iOS platform — future work

No iOS application exists yet. [`../../../apps/ios/`](../../../apps/ios/) is an explicit placeholder so future contributors do not confuse Android or the responsive web app with an iOS deliverable.

The intended client is native SwiftUI. It may reuse product rules, Firebase field semantics, design tokens, fixtures, and acceptance cases—but not Android Compose UI, APKs, keystores, or `google-services.json`.

Required foundations include macOS/Xcode, Apple bundle/team identity, signing and provisioning, a separate Firebase iOS registration, auth callbacks, privacy declarations, APNs if notifications are used, simulator/device coverage, accessibility checks, archive validation, and TestFlight installation.

Detailed historical handoffs:

- [`../../delivery/2026-09-26-implementation/07-PLATFORM-CONTINUITY.md`](../../delivery/2026-09-26-implementation/07-PLATFORM-CONTINUITY.md)
- [`../../archive/audits/2026-09-25/07-IOS-NATIVE-HANDOFF.md`](../../archive/audits/2026-09-25/07-IOS-NATIVE-HANDOFF.md)

Until Apple tooling and those gates pass, describe iOS as planned or in development—not shipped.
