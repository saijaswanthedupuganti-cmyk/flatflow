# Firebase setup

Habitiq’s web and Android clients share Firebase Auth, Firestore, and Storage contracts.

## Web application

1. Register a Firebase web app.
2. Enable the required authentication providers.
3. Create Firestore and Storage in the intended region.
4. Copy [`../../apps/web/.env.local.example`](../../apps/web/.env.local.example) to `apps/web/.env.local` and fill in the project’s public web configuration.
5. Add `localhost` and the production domains to Firebase Authentication’s authorized domains.

Run the web client from the repository root:

```powershell
npm run dev
```

## Android application

Register the Android application ID `habitiq.app`, place the correct Firebase configuration at `apps/android/app/google-services.json`, and register only the SHA fingerprints required for the intended debug or release key.

Never copy the Android configuration or keystores into a future iOS app. iOS requires a separate Firebase registration and Apple signing setup.

## Rules and indexes

The Firebase CLI configuration remains at the repository root:

```text
firebase.json
backend/firebase/firestore.rules
backend/firebase/firestore.indexes.json
backend/firebase/storage.rules
```

Authenticate and select the intended Firebase project before deployment. Review and validate the rule diff first, then run the required deploy command from the repository root. Repository organization does not authorize a production deployment.
