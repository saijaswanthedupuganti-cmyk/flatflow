# Security policy

## Repository secrets

Never commit:

- `apps/web/.env.local` or production environment values;
- `.env*.check` deployment snapshots;
- `apps/android/*.keystore`;
- `apps/android/keystore.properties` or `local.properties`;
- backup copies of `google-services.json`;
- service accounts, signing passwords, production user data, or exported tokens.

The removed historical `.env.prod.check` and `.env.vercel.check` files included a Vercel OIDC token field. Because those files were previously tracked, the token should be treated as exposed and rotated/revoked through the provider if it was ever valid.

## Firebase

Firebase web API identifiers are client configuration, not authorization. Actual access control belongs in:

- [`../../backend/firebase/firestore.rules`](../../backend/firebase/firestore.rules)
- [`../../backend/firebase/storage.rules`](../../backend/firebase/storage.rules)

Rules are shared production code. Test authenticated, unauthorized, cross-flat, and legacy-record behavior before deployment.

## Signing

The Android release keystore is an irreplaceable delivery identity. Store an encrypted backup outside the repository with controlled access. Do not use debug signing as a silent fallback for a production release.

## Reporting

Report vulnerabilities privately to the maintainers. Do not place exploitable details, production data, or credentials in a public issue.
