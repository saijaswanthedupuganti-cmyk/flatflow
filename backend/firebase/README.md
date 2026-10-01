# Firebase backend policy

This folder owns Habitiq's shared Firestore and Storage policy files. The root [`../../firebase.json`](../../firebase.json) references them, so Firebase CLI commands continue to run from the repository root.

- `firestore.rules` — authorization and validation.
- `firestore.indexes.json` — checked-in index definition.
- `storage.rules` — Storage authorization and validation.

Treat these files as cross-client production code. Validate web and Android compatibility before deployment.
