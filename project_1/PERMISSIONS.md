# Habitiq — Roles & Permissions Reference

> **Purpose:** Companion to `CONDITIONS.md`. That doc covers *what happens* (rotation math, split calculations, settlement logic). This doc covers *who is allowed to trigger it* — the admin/member permission split, every enforcement checkpoint in the system, and the separate (independent) subscription-gating axis. Like CONDITIONS.md, this is written as portable rules, not tied to Habitiq's specific Firestore/Next.js implementation — though the security-rule source is called out because it's the ground-truth enforcement layer, and it doesn't miss anything already captured in CONDITIONS.md.
>
> Extracted directly from `firestore.rules` (the actual enforcement layer — not just UI hints) plus `store/useFlatStore.ts` and `lib/flatService.ts`.
> **Last synced:** 2026-07-03

---

## 1. The Two Roles

Every member of a flat has exactly one role: **admin** or **member**. A flat always has exactly one admin at a time (see §7 for how that invariant is maintained). Role lives on the member record itself, not on the user account — the same person can be admin of one flat and a plain member of another (multi-flat support).

There is no third role (no "moderator," no read-only observer) — the entire permission model is a binary split, with a handful of narrow **self-ownership carve-outs** layered on top (a member can always act on their *own* data even where admin would normally be required).

---

## 2. Full Permission Matrix

Legend: **A** = admin only · **M** = any member · **Self** = the member acting on their own record/creation · ✅ = allowed · — = not allowed at all.

### 2.1 Tasks
| Action | Admin | Member |
|---|---|---|
| Read task list | ✅ | ✅ |
| Create task | ✅ | — |
| Delete task | ✅ | — |
| Edit task (any field, incl. reassignment) | ✅ | ✅ |
| Mark task complete | ✅ | ✅ |
| Manual override (assign to anyone, bypass queue) | ✅ | — (only admin has this escape hatch — see CONDITIONS.md §1.8) |

**Notable asymmetry:** task *update* is wide open to any member (needed for "mark complete" to work for everyone), but *create/delete* is admin-only. This is intentional — day-to-day task execution is member-driven; task *definition* is admin-governed.

### 2.2 Swap Requests
| Action | Admin | Member |
|---|---|---|
| Read swap requests | ✅ | ✅ |
| Create a swap request | ✅ | ✅ (any member, for their own tasks) |
| Accept / decline a request | Only if **you are the designated recipient** (`toUserId`) — admin has NO special override power here | Same rule |
| Withdraw (cancel) your own pending request | Only if **you are the original requester** (`fromUserId`) | Same rule |
| Mark a request "read" (dismiss notification) | Self only | Self only |
| Delete a swap request | ✅ (cleanup only — flat deletion, kick cleanup) | — |

**Key point:** swap resolution is one of the few places admin has **zero** special power — it's purely a self-ownership check (`toUserId == me` / `fromUserId == me`). An admin cannot force-accept or force-reject someone else's swap.

### 2.3 Expenses (Daily Splits)
| Action | Admin | Member |
|---|---|---|
| Read expenses | ✅ | ✅ |
| Create an expense | ✅ | ✅ (any member; must be creating as themselves — `createdBy == me`) |
| Edit an expense | ✅ (any expense) | Self only (`createdBy == me`) |
| Delete an expense | ✅ (any expense) | Self only (`createdBy == me`) |

### 2.4 Settlements
| Action | Admin | Member |
|---|---|---|
| Read settlements | ✅ | ✅ |
| Record a settlement | ✅ | ✅ — either the **payer** or the **receiver** can record it (needed so a creditor can log "Mark Received" on behalf of a debtor who hasn't opened the app) |
| Edit a settlement | — | — (nobody can — settlements are immutable once created) |
| Delete a settlement | ✅ (any) | Self only, and only if you were the payer or receiver on it |

### 2.5 Recurring Bills (templates)
| Action | Admin | Member |
|---|---|---|
| Read bill templates | ✅ | ✅ |
| Create / edit / delete a template | ✅ | — |

Fully admin-governed — members never touch the recurring bill *template* (amount, participants, payer rotation, collector).

### 2.6 Bill Instances (a single month's generated bill)
| Action | Admin | Member |
|---|---|---|
| Read instances | ✅ | ✅ |
| Generate an instance from a template | ✅ (`generatedBy == self`) | — |
| Submit a one-time "I paid this bill" instance | — (admin technically could, but this path exists for members) | ✅ — only if `isOneTime == true` AND you declare yourself as both `generatedBy` and `paidBy` (you can only submit a bill *you personally* paid) |
| Approve / reject a member-submitted bill | ✅ | — |
| Edit amount / skip / any field | ✅ (unrestricted) | — |
| Mark bill as paid (vendor confirmed) | ✅, OR the declared **payer** (`paidBy == me`) — but a payer's update is restricted to only `status`, `paidAt`, `collectedFrom` fields, nothing else | — otherwise |
| Update collection tracking (`collectedFrom`) | ✅ | The designated **collector** (`collectorId == me`) can update it. Separately, **any member** may update the `collectedFrom` field — but ⚠️ **the rule only restricts which field is touched, not which uid's entry inside it changes.** In practice any member can flip any other member's collection flag, not just their own; "self-mark only" is a UI convention, not a rules-layer guarantee. |
| Delete an instance | ✅ | — |

**Notable layered permission:** `collectedFrom` has three independent paths to the same field — admin (any), the collector (any entry), and any member (their own entry only). This is the only field in the entire schema with three separate grant paths.

### 2.7 Members (the flat's roster)
| Action | Admin | Member |
|---|---|---|
| Read member list | ✅ | ✅ (plus: even a non-member can read their *own* potential doc, so join-flow can check "am I already in?") |
| Join (create own member doc) | — (join is member-initiated) | ✅ — self-only, and only while flat has < 8 members |
| Update own profile (nickname, etc.) | ✅ (own doc) | ✅ (own doc) |
| Update *another* member's status field only | ✅ | ✅ — any member may flip another member's `status` field (and ONLY that field) — this is what makes the swap-accept → auto-OOS mechanism work across two different people's sessions (CONDITIONS.md §2.3) |
| Update any other field on another member's doc | ✅ | — |
| Kick a member | ✅ | — |
| Delete own membership (leave) | ✅ (own doc) | ✅ (own doc) |

### 2.8 Flat Document (settings)
| Action | Admin | Member |
|---|---|---|
| Read flat doc (incl. to validate invite codes) | ✅ | ✅ (any authenticated user, even non-members) |
| Create a flat | ✅ (creating it makes you admin) | n/a |
| Update most fields (name, join mode, billing cycle day, etc.) | ✅ | — |
| Increment `memberCount` by exactly 1 | (covered by admin's general update right) | ✅ — any authenticated user may do this **and only this**, as part of joining |
| Decrement `memberCount` by exactly 1 | (covered) | ✅ — any current member may do this **and only this**, as part of leaving (must happen before their member doc is deleted) |
| Delete the flat | ✅ | — |

**Design note:** `memberCount` is the one field on the flat doc where the rules deliberately punch a narrow hole through the "admin-only" wall — because join/leave are member-initiated actions that must adjust a shared counter without granting general flat-edit rights.

### 2.9 Activity Log
| Action | Admin | Member |
|---|---|---|
| Read | ✅ | ✅ |
| Create an entry | ✅ | ✅ — but the `userId` on the entry must be either your own UID or the literal string `'system'` (used for cross-session automated entries like the auto-OOS trigger) |
| Update an entry (e.g. soft-hide) | ✅ | — |
| Delete | ✅ | — |

### 2.10 Join Requests (approval-mode flats only)
| Action | Admin | Member (non-member requester) |
|---|---|---|
| Submit a join request | n/a | ✅ — any authenticated user, for themselves only |
| Read all pending requests | ✅ | — |
| Read your own request's status | — | ✅ (requester only) |
| Approve / reject / delete | ✅ | — |

### 2.11 Month Cycles (settlement lifecycle)
| Action | Admin | Member |
|---|---|---|
| Read (incl. carry-forward history) | ✅ | ✅ |
| Open / close a month | ✅ | — |
| Delete | — | — (nobody — permanent history) |

### 2.12 NPS Responses
| Action | Admin | Member |
|---|---|---|
| Read all responses | ✅ | — (not even your own — one-way submission) |
| Submit (create your own) | ✅ (as a member) | ✅ (`uid == me`) — note: the rule only checks the new doc's `uid`, it does not check for an existing response from that uid first. "Once per member" is a UI convention (e.g. hide the banner after submitting); nothing at the rules layer stops multiple submissions if called directly. |
| Update / delete | — | — |

### 2.13 Rewards Wallet
| Action | Admin | Member |
|---|---|---|
| Read own rewards | Self only | Self only |
| Create own reward record | Self only (system writes under completer's UID) | Self only |
| Update | Self only, and **only** the `isRedeemed` field — nothing else about a reward can be mutated after issuance | Same |
| Delete | — | — |

### 2.14 Reward Pool / Coupons (global catalogs, not per-flat)
| Action | Admin (flat-level) | Anyone authenticated |
|---|---|---|
| Read reward pool / coupons | — (not role-gated, just auth-gated) | ✅ |
| Write reward pool | — | — (Firebase Console / Admin SDK only — no client, admin or otherwise, can touch this) |
| "Redeem" a coupon | — | ✅ — but the only permitted mutation is appending exactly one flatId to the `usedBy` array; nothing else on the coupon doc can change |

---

## 3. Escalation Points — Where a Member Can Reach Actions Normally Reserved for Admin

These are the deliberate exceptions, worth listing on their own because they're easy to miss if you only read "admin-only" at a glance:

1. **Own expenses/settlements they created or are party to** — full edit/delete rights, no admin needed.
2. **Any member can flip another member's `status` field** (not just their own) — required for the cross-device auto-OOS trigger after a swap acceptance.
3. **Any member can submit a one-time bill they personally paid** — bypasses the "recurring bills are admin-only" rule entirely, but only for bills where they are simultaneously the submitter and the payer.
4. **The declared payer on a bill instance** can mark it paid and touch collection fields — even though they didn't generate the instance and aren't the admin.
5. **The designated collector** (a role assignable per-bill, independent of admin/member) can update collection tracking on instances they collect for.
6. **Any member can mark a `collectedFrom` entry** — intended as self-serve ("I've paid the collector back"), but the underlying rule doesn't actually scope this to the member's own entry (see §2.6 note) — worth deciding whether to tighten this if re-implemented with real per-key authorization (e.g. a REST API can check `entryKey == request.user.id` in a way Firestore's flat field-diff check cannot).
7. **Joining and leaving** touch the shared `memberCount` field on the flat document (normally admin-only-editable) through a narrow single-field carve-out.

---

## 4. The Admin Invariant

A flat always has exactly one admin. This is not enforced by a single rule — it's an emergent property of how the write paths are shaped:
- Creating a flat makes you its admin (only path to becoming admin without a transfer).
- Transfer-admin is a distinct, explicit action (`transferAdminService`) — separate from leaving.
- Leaving as an admin does **not** auto-promote anyone. The expected flow is: transfer role first, then leave (CONDITIONS.md §6.3). Nothing in the rules layer stops an admin from leaving without transferring — that invariant is a convention enforced by the app's UI/flow, not by the security rules themselves. **Worth flagging if porting to a new app: decide whether to make this a hard rule-level constraint rather than relying on UI discipline.**
- If the admin is the last member standing and leaves, the entire flat and all subcollections are deleted rather than left adminless (CONDITIONS.md §6.4).

---

## 5. Subscription Gating — A Second, Independent Permission Axis

This is orthogonal to role. A user's ability to act is gated by **both** role AND the flat's subscription state at once — role answers "are you allowed to do this kind of action," subscription answers "is this flat currently allowed to do this action at all."

| Subscription state | What's blocked | Who sees what |
|---|---|---|
| `trial` (first 30 days from flat creation) | Nothing — full access | Everyone |
| `active` (coupon redeemed) | Nothing — full access | Crown/PREMIUM badge shown |
| `expired` (trial ran out, no coupon redeemed) | Gated actions: create task, add expense, create bill, create an additional flat. Existing data remains fully viewable (view-only mode, not a lockout) | **Admin** sees a modal to enter a coupon directly. **Member** sees a passive hint ("ask your admin for a coupon") — members cannot redeem a coupon themselves even though they can view everything |
| Any state | `maxFlats`: 1 for trial/expired, 3 for active/premium — caps how many flats a single user can belong to as a creator/joiner, independent of role | Applies per-user, not per-flat |

**Interaction with §2:** an admin who is otherwise allowed to create a task can still be blocked by subscription gating if the flat is expired. Conversely, a member who submits a member-paid bill (§2.6, escalation #3) is unaffected by admin/member role but IS still subject to the same expired-flat gate on bill-related actions.

---

## 6. Enforcement Layers — Read Before Porting to a New App

There are two layers doing permission enforcement in Habitiq, and they must stay in lockstep:
1. **Firestore security rules** (`firestore.rules`) — the actual, unbypassable enforcement. Everything in the tables above is sourced from this file, not from UI behavior.
2. **App-level checks** (React components, Zustand actions) — UX convenience only (hiding buttons, showing modals). These mirror the rules but are NOT the security boundary — a user could bypass the UI entirely and the rules would still hold.

**Porting implication:** if the other application uses a different backend (REST API, different DB), the permission matrix in §2 is the part to carry over as-is — but it needs to be re-implemented as *server-side authorization checks*, not left as UI-only conditionals. Habitiq's own security audit history (see vault doc §6b) exists specifically because early versions of several of these rules were missing or under-scoped (e.g. "any member could create/delete tasks" was a fixed HIGH-severity finding) — worth treating that audit list as a checklist of permission mistakes already made once, so the new app doesn't repeat them.

**Known gaps found while verifying this doc against `firestore.rules` (2026-07-03):** Firestore's rule language can only check *which top-level field* changed on a write, not *which key inside a map* changed. Two places rely on map-key-level self-ownership that the rules can't actually express:
1. `billInstances.collectedFrom` (§2.6) — any member can flip any other member's collection flag, not just their own.
2. `npsResponses` (§2.12) — nothing stops the same uid from submitting more than one response.

Neither is a money-movement risk (collection tracking is a bookkeeping flag; NPS is a survey), so low severity — but if the new app's backend *can* express per-key authorization (a REST API checking `entryKey === req.user.id` can, where Firestore's `affectedKeys()` cannot), this is a chance to close both gaps outright rather than carry them forward.

---

## 7. Cross-Reference

- Calculation/condition logic for what these actions *do* once permitted → `CONDITIONS.md`
- Security audit history (bugs found in this exact permission model) → main vault doc §6b
- Subscription tiers and pricing → main vault doc §11
