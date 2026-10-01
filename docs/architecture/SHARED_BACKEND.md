# Shared Firebase contract

Web and Android currently use the same Firebase backend. Future iOS must follow the same field, permission, and lifecycle contracts.

| Concern | Owner |
|---|---|
| Firebase CLI mapping | `firebase.json` |
| Firestore authorization | `backend/firebase/firestore.rules` |
| Firestore indexes | `backend/firebase/firestore.indexes.json` |
| Storage authorization | `backend/firebase/storage.rules` |
| Web readers/writers | `apps/web/lib/`, `apps/web/store/` |
| Android readers/writers | Android repositories under `apps/android/app/src/` |

Rules are production code. A UI change does not authorize a rule or schema change. Test producer/reader compatibility in both active clients before deployment.
