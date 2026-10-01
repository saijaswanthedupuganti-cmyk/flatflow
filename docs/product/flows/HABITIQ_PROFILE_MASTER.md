# HABITIQ — PROFILE MASTER SPEC

Drop into Cursor: **Inspect Auth, `users/{uid}`, membership, `activeFlatId`, admin/member, and `seekerProfiles` before changing UI. Do not create Firebase fields to satisfy a mockup. If the design wants data the schema does not have, stop and list the gap.**

Profile answers: **Who am I in Habitiq?**  
It is a personal control center, not an admin dashboard and not a SaaS settings dump.

---

## 00 — HARD RULES

1. Split **account identity** (who is this login) from **Discovery identity** (what others may see when finding a person). Never one giant Edit form.
2. Do not invent: company, college, hometown, age, profession, per-field visibility, completeness %, Trust Score, notification preference matrix, appearance, language, billing.
3. Do not put Manage Flat admin chrome on the first Profile screen.
4. Do not show a Reliability / Trust percentage on Profile.
5. Route from Firebase: no flat is a valid state (onboarding). Multiple `flatIds` → switcher.
6. Friendly errors only.

---

## 01 — AUDIT (23 Aug 2026)

### Account identity (exists)

| Source | Fields |
|---|---|
| Firebase Auth | `uid`, `email`, `displayName`, `photoUrl` (Google; **no Storage upload in APK**) |
| `users/{uid}` | `email`, `displayName`, `activeFlatId`, `flatIds[]` |
| Write | `UsersRepository.updateProfile(uid, displayName)` |

**Gap:** no in-app “change photo” pipeline (no Coil, no Storage). Show Google `photoUrl` only if we add an image loader later; v1 uses **initial avatar**. Email is Auth-tied → **read-only**.

### Flat membership (exists)

`flats/{id}` + `members/{uid}` role `admin` | `member`. `FlatViewModel.isAdmin`, `flatInfo`, `members`, leave → next `activeFlatId` or onboarding.

Invite code = flat document id. Share helper exists.

### Discovery profile (exists — different collection)

`seekerProfiles/{uid}`: `displayName`, `photoUrl`, `city`, `lookingIn`, `budget`, `bio`, `gender`, `lifestyleTags`, `active`, `createdAt`, optional `commuteAnchors` (Phase 2, unused).

Visibility today = **`active` looking post**, not per-field flags.

**Gaps (do not fake):** company, college, hometown, profession, age, commute UI, field-level privacy.

Gender on looking posts currently `any | male | female | other`. Profile copy must treat this as **your information**, not “who is acceptable.” Matching filters stay in Discover.

### Preferences that exist

- `AppPreferences` biometric lock
- FCM exists; **no** user notification preference document
- Account deletion: `SettingsViewModel.deleteAccount` (Firestore then Auth) — **keep**, buried under Preferences / danger zone, not the first screen

### Trust

Do **not** surface `members.reliabilityScore` on Profile. Members list may show nickname + role + You, not a score.

### Current APK Profile (replace)

One scrolling page: email, inline name field, Members, Activity, Manage vacancy, Settings, Leave, Sign out. Mixes admin, discovery, and account. That is the problem.

---

## 02 — FIRST SCREEN IA (v1)

```text
Header (avatar initial, name, email, Edit profile)
My Flat     → current name, role, member count  (or empty CTA)
Discovery   → My Discovery Profile (looking post fields only)
Preferences → existing Settings (biometric + delete account)
Account     → Sign out (confirm)
```

No completeness bar. No trust card. No notification row until a preference model exists.

---

## 03 — EDIT PROFILE (account only)

- Full name → `updateProfile`
- Email read-only
- Photo: not a fake picker. Copy: photo comes from Google Sign-In when present.

---

## 04 — MY FLAT

Tap current flat:

- Name, role, members (You badge)
- Invite: share (admin especially; members can still share if they have the code)
- Admin: Manage Flat (existing overlay — vacancy/join mode stay there)
- Activity log: secondary, not first screen
- Leave with existing confirm
- Multiple flats: Switch flat

No flat:

> You haven’t joined a flat yet.  
> [ Continue setup ] → onboarding intent chooser

---

## 05 — DISCOVERY PROFILE

Reuse `updateSeekerProfile` / `setSeekerActive`.

Show/edit only: city, lookingIn, budget, bio, gender (inclusive labels mapped to existing strings), lifestyle tags, **visible as looking post** (`active`).

Explain: this is what people see in Find a person — not your account email.

---

## 06 — SIGN OUT

Confirm → existing `signOut()` → Login. Session cleared via Auth.

---

## 07 — STATES

Loading skeleton on first Profile paint while `FlatViewModel.loading`.  
Error + Try again → `refresh()`.  
Never dump Firestore messages.

---

## 08 — IMPLEMENTATION ORDER

Done in this pass: audit → shell → edit account → my flat pane → discovery entry → preferences link → sign out confirm → empty/multi-flat/error.

Not this pass: photo upload, company/college, trust badge, notification matrix, per-field visibility.
