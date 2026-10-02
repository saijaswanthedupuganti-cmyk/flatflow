# Firebase backend policy

This folder owns Habitiq's shared Firestore and Storage policy files. The root [`../../firebase.json`](../../firebase.json) references them, so Firebase CLI commands continue to run from the repository root.

- `firestore.rules` — authorization and validation.
- `firestore.indexes.json` — checked-in index definition.
- `storage.rules` — Storage authorization and validation.
- `functions/` — Cloud Functions (Node 22, TypeScript). `purgeDeletedUser` runs when an Auth account
  is deleted and removes the user's doc, blocks, seeker profile, Discover connections and sent
  messages (DPDP erasure). Clients can't delete messages or connections, so this is the only path.
  Deploy: `firebase deploy --only functions --project garbage-f79f7` from the repo root.

Treat these files as cross-client production code. Validate web and Android compatibility before deployment.
