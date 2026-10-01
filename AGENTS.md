<!-- BEGIN:nextjs-agent-rules -->
# This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` before writing any code. Heed deprecation notices.
<!-- END:nextjs-agent-rules -->

# Habitiq repository rules

Before changing product behavior, read `docs/MASTER.md`, `docs/architecture/REPOSITORY_STRUCTURE.md`, and the relevant application README.

- Treat `apps/web`, `apps/android`, and future `apps/ios` as separate clients of shared Firebase contracts in `backend/firebase`.
- Android is the current native delivery target. iOS is planned but has no implementation in this repository.
- Preserve the existing working tree. Do not delete, move, or rewrite unrelated product work.
- Never commit `.env.local`, Android keystores, `keystore.properties`, local service backups, or credentials.
- A successful build is not release approval. Use dated candidate artifacts until device, Firebase, accessibility, and signing gates pass.
- Current implementation evidence lives in `docs/delivery/2026-09-26-implementation/execution-status.md` until a newer dated verification record replaces it.
- Run web commands through the root npm workspace. Before web code changes, resolve and read the relevant installed Next.js guide under `node_modules/next/dist/docs/`.
