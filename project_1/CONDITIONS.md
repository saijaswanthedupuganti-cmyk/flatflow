# Habitiq — Core Business Logic / Conditions Reference

> **Purpose:** This is a pure business-logic extraction — trigger → precondition checks → calculation → resulting state — for the core systems: Rotation Engine, Swap System, Expenses/Bills, Settlement, Subscription/Trial-Hold, and Rewards. It intentionally omits Habitiq's specific information architecture (Next.js routes, Firestore paths, Zustand store shape, UI components) so the *conditions* can be re-implemented in a different app with a different architecture. Where a data shape is unavoidable (e.g. "queue is an ordered array"), it's called out because the algorithm depends on that shape, not because the IA must match.
>
> Extracted directly from source: `lib/rotationEngine.ts`, `lib/expenseUtils.ts`, `lib/settlementUtils.ts`, `lib/flatService.ts`, `store/useFlatStore.ts`, `hooks/useSubscription.ts`, `lib/couponService.ts`, `lib/rewardPool.ts`, `lib/rewardSignal.ts`, `store/useRewardsStore.ts`.
> **Last synced:** 2026-07-03
>
> Companion doc: `PERMISSIONS.md` covers *who* can trigger these (admin vs member vs subscription-gated); this doc covers *what happens* once triggered.

---

## 1. Rotation Engine

### 1.1 Data shape required
- Each **task** has: `queueOrder` (ordered array of member IDs — this order IS the rotation), `currentAssignedUserId`, `status` (pending | overdue | completed | paused), `frequency` (daily | weekly | fortnightly | monthly | one_time | custom), `dueDate`, `lastCompletedAt`.
- Each **member** has: `status` (available | busy | out_of_station | inactive).

### 1.2 Condition: Who is next in the queue (`getNextAssignee`)
```
input: task, members
if queue.length == 0 → return currentAssignedUserId (no-op, nothing to rotate)
currentIndex = index of currentAssignedUserId in queue
if currentAssignedUserId not found in queue → return queue[0]  (repair: reset to head)

loop up to queue.length times, advancing index circularly (nextIndex = (nextIndex+1) % queue.length):
    candidate = queue[nextIndex]
    if candidate's member.status is 'available' OR 'busy' → return candidate   (busy still counts as available for rotation — only OOS/inactive are skipped)
if loop exhausts without a hit → return null   (everyone OOS/inactive: queue must pause)
```
**Key condition:** only `out_of_station` and `inactive` are skip conditions. `busy` is NOT skipped.

### 1.3 Condition: Task completion (`completeTask`)
```
input: task, members, completionDate? (optional — retroactive completion)

completedAt = completionDate if provided AND completionDate <= now, else now
nextAssignee = getNextAssignee(task, members)   // computed BEFORE mutating task

if task.frequency == 'one_time':
    → status = 'completed', lastCompletedAt = completedAt
    → STOP (no rotation, no new due date, permanent)

nextDueDate = completedAt + {
    daily: 1 day, weekly: 7 days, fortnightly: 14 days, monthly: 30 days,
    anything else (custom/unrecognized): 7 days (fallback)
}
```
**Critical rule:** `nextDueDate` is computed from **completion date**, never from the original due date. This means: if a task is 3 days overdue and someone completes it late, the *next* person still gets a full fresh cycle (e.g. full 7 days for weekly) — the overdue days are absorbed entirely by the person who was late, never inherited by the next person.

```
if nextAssignee is null (everyone OOS/inactive):
    → status = 'paused', lastCompletedAt = completedAt, currentAssignedUserId UNCHANGED, dueDate UNCHANGED
else:
    → status = 'pending', currentAssignedUserId = nextAssignee, lastCompletedAt = completedAt, dueDate = nextDueDate
```

### 1.4 Side effects that MUST fire atomically with completion (not part of the pure function, but required conditions in the caller)
1. **Stale swap cleanup:** any `pending` swap request for this task is force-rejected the moment the task completes — otherwise the newly-assigned person sees a phantom "swap pending" block on a task that already rotated to them.
2. **Race protection:** re-fetch the freshest member availability (not cached state) before computing the real rotation target, to avoid rotating onto someone who went OOS a moment ago. An "early" optimistic assignment is shown to the UI instantly, then corrected once fresh data returns if it differs.
3. **Double-rotate guard:** after committing the new assignee, re-check if THAT person is also OOS/inactive (edge case: stale data). If so, immediately re-run rotation skipping them too, or pause if nobody is left.
4. **Reward eligibility (anti-abuse) — two independent gates, both must pass:**
   - **Gate A (no retroactive rewards):** reward only issued if `completedAt` is within the last 2 hours of "now" (i.e., not a backdated/retroactive completion).
   - **Gate B (cooldown):** reward only issued if the last reward for this device was more than 24 hours ago (tracked via a last-reward timestamp key).
   - Both gates independent of rotation logic — completion always rotates; reward is a bonus on top, gated separately.

### 1.5 Condition: Overdue detection (`checkOverdueTasks`, typically run on a poll/cron)
```
for each task where status == 'pending' AND dueDate < now:
    → status = 'overdue'
```
Note: overdue tasks stay assigned to the SAME person — overdue does not trigger reassignment. Only completion or going-OOS reassigns.

### 1.6 Condition: Member goes out-of-station (`changeMemberStatus` → 'out_of_station')
```
set member.status = 'out_of_station'

for each task where:
    currentAssignedUserId == this member
    AND status in ('pending', 'overdue')
    AND NOT already being handled by an accepted/pending OOS-linked swap request for this exact task+user:

    nextUserId = getNextAssignee(task, membersWithThisUserMarkedOOS)
    if nextUserId found → directly transfer task to nextUserId (currentAssignedUserId updated, status/dueDate untouched — this is a reassignment, not a completion)
    else (everyone OOS) → status = 'paused'
```
**Condition for exclusion:** a task is skipped from this bulk-reassignment sweep if the member already has a pending/accepted swap request flagged `isOOSRequest = true` for that specific task — because that swap flow is already handling the handoff explicitly (see §2.3).

### 1.7 Condition: Member returns early (`returnEarly`)
```
set member.status = 'available'
for each task where status == 'paused' AND queueOrder includes this member:
    nextUserId = getNextAssignee(task, membersWithThisUserMarkedAvailable)
    if nextUserId found → status = 'pending', currentAssignedUserId = nextUserId
    else → skip (still nobody available)
```
Note: does NOT blindly reassign everything back to the returner — uses the same rotation function, so whoever is next in the actual queue order gets it (could be someone else who was already available).

### 1.8 Condition: Manual admin override (`manuallyAssignTask`)
```
admin picks any target member directly → status = 'pending', currentAssignedUserId = target
```
No rotation-order validation — admin override bypasses queue order entirely (escape hatch).

### 1.9 Condition: Member joins the flat mid-cycle
```
for every existing task:
    if new member's uid not already in queueOrder → append to END of queueOrder
```
New members never get inserted mid-queue or retroactively assigned current tasks — they only enter future rotations from the tail.

### 1.10 Condition: Member leaves / is kicked (`reassignMemberTasks`)
```
for every task:
    leavingIndex = index of leaving member in OLD queueOrder
    newQueue = OLD queueOrder with leaving member removed

    if task.currentAssignedUserId == leaving member:
        // the person who was NEXT in the old queue now occupies `leavingIndex` in the
        // filtered array (or wraps to 0 if leaving member was last) — assign to them
        currentAssignedUserId = newQueue[leavingIndex % newQueue.length]   (newQueue.length must be > 0, else set to empty/unassigned)

    queueOrder = newQueue   (unconditionally, even if this task wasn't assigned to the leaver)
```
This guarantees the rotation position is preserved (whoever was "next" stays next) rather than jumping back to queue head.
Also on leave/kick: all pending swap requests involving the leaving member (as either party) are force-rejected.

---

## 2. Swap Request System

### 2.1 Data shape
Swap request: `taskId, fromUserId, toUserId, status (pending|accepted|rejected), read (bool), isAutomatic (bool), isOOSRequest (bool), createdAt`.

### 2.2 Condition: Creating a swap request
```
if a PENDING swap request already exists for this exact taskId → reject (no duplicate pending requests per task)
else → create new request, status = 'pending'
```

### 2.3 Condition: Resolving a swap request (accept/decline)
```
PERMISSION GATE: only request.toUserId (the designated recipient) may resolve it.
  Not the requester, not an admin, not anyone else. Enforced at the write layer, not just UI.

if resolved 'accepted':
    → transfer the task: currentAssignedUserId = toUserId (task's queueOrder position is untouched —
      this is a lateral handoff, not a rotation-order change)
    → IF this was flagged isOOSRequest:
          re-check ALL pending isOOSRequest swaps from this same fromUserId across all their tasks
          IF none remain pending (all accepted) → automatically flip fromUserId's status to 'out_of_station'
          (this is the mechanism for a "batch going-out" flow: user requests swaps for every task
           they currently hold, and only once ALL are covered do they actually go OOS)

if resolved 'rejected':
    → task stays exactly as-is, no reassignment triggered automatically
```

### 2.4 Condition: Cancelling a swap request
```
PERMISSION GATE: only request.fromUserId (the original requester) may cancel.
only allowed if status is still 'pending' (cannot cancel an already-resolved request)
→ status = 'rejected'
```

### 2.5 Condition: Swap requests auto-invalidated (not user-initiated)
- Any pending swap for a task is force-rejected the instant that task is completed (§1.4).
- Any pending swap involving a member who leaves/is kicked is force-rejected (§1.10).

---

## 3. Expenses — Daily Splits (ad-hoc, direct pairwise balances)

### 3.1 Data shape
Expense: `paidBy, splitAmong[] (uids), splits{uid→amount}, currency, amount, deferToNextMonth?, billInstanceId?`.
Settlement: `fromUserId, toUserId, amount, currency`.

### 3.2 Condition: What counts toward "Daily Splits" balances
```
EXCLUDE expense if deferToNextMonth == true
EXCLUDE expense if billInstanceId is set (that's bill money, tracked separately — see §4)
```

### 3.3 Calculation: Net pairwise balance for `currentUser` (`computeBalances`)
Balances are **direct pairwise per-currency**, NOT a simplified debt graph (no min-cash-flow here — that only happens at month-close, §5).
```
for each qualifying expense:
    if paidBy == currentUser:
        for each uid in splitAmong (excluding self):
            owed = splits[uid]
            if owed > 0: bump(currency, uid, +owed)      // uid owes currentUser
    else if currentUser is in splitAmong:
        myShare = splits[currentUser]
        if myShare > 0: bump(currency, paidBy, -myShare)  // currentUser owes paidBy

for each settlement:
    if fromUserId == currentUser: bump(currency, toUserId, +amount)   // I paid them → reduces what they owe me... 
       // (semantically: recorded as currentUser having paid toUserId, which nets against existing debt)
    if toUserId == currentUser: bump(currency, fromUserId, -amount)

DUST THRESHOLD: any resulting balance with |amount| < 0.5 (currency units) is dropped entirely — not shown, not carried.
Amounts are rounded to whole units in the final output.
```
**Sign convention:** positive = they owe the current user; negative = current user owes them.

---

## 4. Recurring Bills & Member-Submitted Bills

### 4.1 Data shape
RecurringBill (template): `payerMode (rotation|fixed|manual), rotationQueue[], currentPayerIndex, fixedPayerUid?, participants[] (defaults to rotationQueue), isVariable, amount?, splitMethod (equal|percent|custom), percentSplits?, customSplits?, recurrenceType (monthly|every_n_days|every_n_months|yearly), recurrenceIntervalValue?, billingDay, lastGeneratedMonth, lastGeneratedAt, active, collectorId?`.
BillInstance: `templateId, month, amount, paidBy, participants[], splits{}, status (pending|split_generated|paid|skipped|member_submitted), collectedFrom{uid→bool}, submittedBy/generatedBy, isOneTime?`.

### 4.2 Condition: Who pays this cycle (`generateBill`)
```
if payerMode == 'fixed'  → payer = fixedPayerUid
if payerMode == 'manual' → payer = explicitly supplied uid at generation time
if payerMode == 'rotation' (default) → payer = rotationQueue[currentPayerIndex % rotationQueue.length]
                                        → AFTER generating, currentPayerIndex advances by 1 (mod queue length)
                                        → index does NOT advance for fixed/manual modes
```

### 4.3 Condition: Is this bill due for generation? (`generateAllDueBills`, `isNonMonthlyDue`)
```
MONTHLY bills: due if lastGeneratedMonth != targetMonth AND today's date-of-month >= billingCycleDay
  (billingCycleDay is clamped to [1, last day of current month] — protects Feb 30 etc.)

NON-MONTHLY bills (never generated yet → always due; otherwise, days since lastGeneratedAt):
  every_n_days:   daysSinceLast >= recurrenceIntervalValue (default 30)
  yearly:         daysSinceLast >= 365
  every_n_months: calendar-month difference >= recurrenceIntervalValue (default 1)

Never double-generate: skip if an instance for (templateId, targetMonth) already exists.
```

### 4.4 Calculation: Splitting the bill amount (`computeBillSplits` — the prorated join-date algorithm)
This is the default 'equal' split method, and it is **NOT** a flat equal split — it prorates by how much of the current month each participant has actually been a member for:
```
for each participant:
    if their joinedAt <= start of this month → weight = 1  (full month, full share)
    else → weight = (daysInMonth - joinDayOfMonth + 1) / daysInMonth   (fraction of month remaining from join date)
    (if joinedAt is missing/invalid → weight = 1, fail open to full share rather than zero)

totalWeight = sum of all weights
each participant's share = round(amount * theirWeight / totalWeight, 2 decimals)
  (if totalWeight is somehow 0, fall back to flat equal split amount/participantCount)
```
**Condition:** this proration ONLY applies to the default 'equal' split method.
- `splitMethod == 'percent'` → share = amount * (percentSplits[uid] / 100), independent of join date.
- `splitMethod == 'custom'` → share = customSplits[uid] directly, no calculation at all.

### 4.5 Condition: Bill instance status lifecycle
```
'pending'          → variable-amount bill generated but amount not yet entered (no splits computed yet)
'split_generated'  → amount known, splits computed, awaiting vendor payment confirmation
'paid'             → admin confirmed the vendor was actually paid (markBillPaid) — THIS is the only
                      status that feeds into month-end net balance calculations (§5.2). Creating or
                      generating a bill is NOT the same as it counting toward balances.
'skipped'          → excluded from this month entirely, with optional reason
'member_submitted' → a member (not admin) submitted a bill they personally paid; NOT yet admin-approved;
                      excluded from month totals and from per-member bill summaries until approved
```

### 4.6 Condition: Member-submitted bill flow (`addOneTimeBillInstance` → `approveMemberBill` / `rejectMemberBill`)
```
1. Any member (who personally paid) can submit: name, amount, category, participants, splitMethod.
   → status = 'member_submitted' immediately (splits ARE computed at submission time using
     the member's chosen splitMethod, but the instance doesn't count toward balances yet).

2. Admin reviews. Admin MAY override amount, payer, and/or name before deciding.

3a. APPROVE:
    finalAmount = admin override ?? original submitted amount
    finalPaidBy = admin override ?? original submitter-declared payer
    IF admin changed amount OR payer → splits are recalculated as a flat EQUAL split
      across all participants (proration and custom/percent splits are NOT reapplied here —
      approval always falls back to equal split when amount/payer changes)
    IF admin did NOT change amount/payer → original splits are kept as-is
    → status = 'paid' (goes straight to paid, skipping 'split_generated' — because the member
      already paid the vendor out of pocket at submission time)

3b. REJECT:
    → instance is deleted entirely (not soft-deleted/skipped — hard delete, never enters the pipeline)
```

### 4.7 Condition: Editing a live recurring bill's participant list (`updateRecurringBill`)
```
if participants[] changes on the template:
    for every instance of this template still in status 'pending' or 'split_generated'
    (i.e. not yet paid/skipped):
        newParticipants = the updated list
        equalShare = round(amount / newParticipants.length)
        splits = flat equal split, with the LAST participant absorbing the rounding remainder
                 (amount - equalShare*(n-1)) so totals always sum exactly to `amount`
```
Already-paid or skipped instances are never retroactively touched.

### 4.8 Condition: Collection tracking (separate from payment status)
`collectedFrom{uid→bool}` is independent of bill status — it tracks whether the **collector** has physically received each participant's share, regardless of whether the vendor has been paid. A bill can be `paid` (vendor got money) while `collectedFrom` is still incomplete (some flatmates haven't reimbursed the collector yet).

---

## 5. Settlement & Month-Close

### 5.1 Condition: What is INR-only vs multi-currency
Daily Splits and per-user pairwise balances (§3) support 7 currencies. **Month-end close and settlement suggestion (§5.2 onward) are INR-only** — any non-INR expense/bill/settlement is silently excluded from month-end totals and carry-forward.

### 5.2 Calculation: Net month balance (`computeMonthNetBalances`)
Applied in this exact order (order matters because later steps can partially offset earlier ones):
```
1. Start from carryForwardIn (previous month's unresolved balances, if any)
2. For each PAID bill instance in this month:
     for each participant except the payer: bump(payer, +theirShare); bump(participant, -theirShare)
   (bills only enter here at 'paid' status — never 'split_generated' or earlier)
3. For each ad-hoc Daily Splits expense in this month (not deferred, INR only):
     for each participant except payer: bump(payer, +theirShare); bump(participant, -theirShare)
4. For each settlement recorded in this month:
     bump(fromUserId, +amount); bump(toUserId, -amount)   (reduces net debt)

Sign convention: positive net = this person is owed money overall; negative = owes money overall.
```

### 5.3 Calculation: Per-member bill summary for the collector (`computeMemberBillSummary`)
Distinct from §5.2 — this is "who still owes the collector," independent of vendor-paid status:
```
EXCLUDE bill instances with status 'skipped' or 'member_submitted' (unapproved) from this entirely.

for each qualifying instance:
    totalShare[participant] += their split amount  (accumulated across ALL such instances this month)
    if instance.status == 'paid': contributions[instance.paidBy] += the FULL bill amount
       (not just their own share — crediting the whole out-of-pocket vendor payment)

for each settlement this month:
    if paid TO the collector → settled[payer] += amount
    if paid BY the collector (refund) → settled[recipient] -= amount

outstanding[member] = round(totalShare - contributions - settled)
    positive → member still owes the collector
    negative → collector owes the member back
```

### 5.4 Calculation: Greedy minimum settlement suggestions (`suggestSettlements`)
Standard greedy min-cash-flow, produces at most N-1 transfers for N people:
```
debtors   = people with net < -0.5, sorted most-negative first
creditors = people with net > +0.5, sorted most-positive first

while both lists have remaining entries:
    payment = min(|debtor.amount|, creditor.amount)
    if payment >= 0.5 → record transfer(debtor → creditor, round(payment))
    debit both sides by `payment`
    advance past any party now within 0.5 of zero
```

### 5.5 Condition: Carry-forward computation (`computeCarryForward`)
```
start from full netBalances
for each CONFIRMED (not skipped) suggested settlement: apply it (debtor += amount, creditor -= amount)
drop any resulting balance with |amount| < 0.5
if nothing remains → carry-forward is null (month fully settled)
else → the remainder becomes next month's carryForwardIn
```
**Important:** only *confirmed* settlements reduce the carry-forward. Any suggested settlement the admin skips at close-time rolls forward untouched into next month.

### 5.6 Condition: Blockers surfaced before month can be closed (`buildMonthSummary`)
```
pendingVariableBills = count of instances still in 'pending' (variable amount not yet entered)
unpaidBillsCount     = count of instances in 'split_generated' or 'overdue' (split known, vendor not yet marked paid)
```
Both are advisory counts meant to be resolved before closing — the system computes them but does not appear to hard-block closing on them (soft warning, not enforced gate, based on what's in these files).

---

## 6. Membership Cap & Join (interacts with rotation + expenses)

### 6.1 Condition: 8-member hard cap, enforced transactionally
```
inside a single atomic transaction (read-then-write, not check-then-write across two calls):
    read current memberCount
    if memberCount >= 8 → abort, error FLAT_FULL
    if requesting uid already has a member doc → abort, error ALREADY_MEMBER
    else → create member doc, increment memberCount by 1, update user's flat list
```
Doing the count-check and the write in one transaction is the condition that prevents a race where two people join simultaneously and both pass a stale count check.

### 6.2 Condition: Leave vs Kick — ordering matters for security rules
```
LEAVE (self-service): reassign tasks (§1.10) → cancel own pending swaps → log activity →
    decrement memberCount FIRST → THEN delete member doc → update leaver's own user profile
    (decrement-then-delete ordering specifically preserves "is this uid still a member" checks
     during the decrement step, before the doc disappears)

KICK (admin-initiated): reassign tasks (§1.10) → cancel target's pending swaps → log activity →
    delete member doc AND decrement memberCount together in ONE atomic batch (not two sequential
    calls like leave) — if either fails, neither commits, so the counter can never get stuck
    inconsistent with actual membership.
    → best-effort update of the kicked user's own profile is attempted but allowed to fail
      silently (a non-admin generally cannot write another user's profile — the kicked user's
      client self-corrects this on next app open via a membership-verification check).
```

### 6.3 Condition: Admin transfer must precede admin leaving
Implied by the service split (`transferAdminService` is a separate call from `leaveFlatService`): an admin who wants to leave a flat with other members remaining must transfer the admin role to someone else FIRST (separate explicit action), then leave as a regular member. Leaving is never allowed to leave a flat with zero admins while members remain — that invariant is maintained by requiring the transfer as a precondition, not by any check inside leave itself.

### 6.4 Condition: Last member leaving deletes the whole flat
Only reachable when the leaving/admin user is the sole remaining member — deletes the flat document plus every subcollection (members, tasks, activity log, swaps, join requests, NPS responses, expenses, settlements, recurring bills, bill instances, month cycles) rather than leaving an orphaned empty flat.

---

## 7. Subscription & Trial-Hold Lifecycle

### 7.1 Data shape
Flat-level fields: `subscriptionStatus` (trial | active | expired), `trialEndDate` (ISO — despite the name, this field doubles as the general "access held until" date for BOTH trial and coupon-activated periods), `couponUsed?`.
Derived client-side (never stored): `isActive`, `isPremium`, `isExpired`, `daysLeft`, `maxFlats`, `can(feature)`.

### 7.2 Condition: The three real states, and how "expired" is actually derived
```
stored status ∈ { trial, active, expired }   — but 'expired' is rarely read directly from storage.
effective status is COMPUTED on every read:
    if stored status == 'trial' AND trialEndDate has passed (< now) → effective status = 'expired'
    else → effective status = stored status (as-is)
```
**Key condition:** nothing ever writes `'expired'` into storage as a background job — there's no cron flipping the flag. Expiry is a pure function of "is trial and is the hold-until date in the past," recomputed live every time the flat document is read. A flat can sit at stored status `'trial'` forever; whether it *behaves* as expired depends entirely on the clock at read-time.

### 7.3 Condition: The trial hold window itself
```
NEW flat created → subscriptionStatus = 'trial', trialEndDate = now + 30 days   (the initial hold window)

Two backfill/migration cases extend a fresh HOLD from the moment they're first read, not from
original creation — both exist to avoid instantly locking out pre-existing flats when this
system was introduced or changed:
  Case A — flat predates the subscription system entirely (no subscriptionStatus field at all):
      → grant subscriptionStatus = 'trial', trialEndDate = now + 90 days  (written back to storage
        on first read, by whichever client — admin or member — has write access at that moment)
  Case B — flat carries the legacy sentinel couponUsed == 'LEGACY_FREE' (an old unlimited-access
      backfill from before this model existed):
      → re-grant subscriptionStatus = 'trial', trialEndDate = now + 90 days, clear couponUsed to null
```
**Condition:** both backfill cases are self-healing and one-shot — once the write succeeds, the flat has a real `trialEndDate` and neither branch fires again on subsequent reads (the `if (!rawStatus)` / `couponUsed === 'LEGACY_FREE'` checks no longer match).
**Note:** the backfill write is best-effort — wrapped so that if the current user (e.g. a non-admin member without flat-write permission) can't perform it, the app still proceeds using the computed value locally without blocking on the write.

### 7.4 Condition: What "expired" actually blocks (`can(feature)`)
```
GATED_FEATURES = { create_task, create_flat, add_expense, create_bill }

can(feature):
    if NOT expired → true  (always allowed regardless of feature)
    if expired → true UNLESS feature is in GATED_FEATURES

isActive  = status is 'trial' OR 'active'      (both count as "in good standing")
isPremium = status is 'active' specifically     (i.e., a coupon was redeemed — distinct badge/treatment)
isExpired = status is 'expired' (post-computation, per §7.2)
```
**Condition — what's explicitly NOT gated even when expired:** reading any existing data, completing tasks, resolving swaps, recording settlements, marking bills paid/collected, viewing balances. Only the four *creation-of-new-obligations* actions above are blocked. This is a deliberate "view-only, not a lockout" design — existing commitments keep functioning; only new ones stop.

### 7.5 Calculation: Days remaining (`daysRemaining`)
```
if no trialEndDate stored at all → default to 30 (optimistic fallback, treated as if fresh)
else → daysLeft = ceil((trialEndDate - now) / 1 day), floored at 0 (never negative)
```

### 7.6 Condition: Coupon redemption (`validateAndRedeemCoupon`) — how a hold is lifted or extended
```
normalize code: uppercase + trim; empty → reject immediately

CHECK BUILT-IN CODES FIRST (hardcoded, no DB doc required — always work if typed correctly):
    each built-in code has a fixed durationDays and a type ('trial_extend' | 'full_unlock')
    durationDays == -1 is the "forever" sentinel → sets trialEndDate to a hardcoded 2099 date
    otherwise → trialEndDate = now + durationDays
    → ALWAYS writes subscriptionStatus = 'active' (even for a 'trial_extend'-typed code —
      the type field is metadata only; every successful redemption flips status to 'active')

IF NOT a built-in code, look up a Firestore coupon document by code:
    not found → reject: invalid code
    flatId already present in coupon's usedBy[] → reject: already used for this flat
    maxUses != -1 AND usedBy.length >= maxUses → reject: usage limit reached
    expiresAt is set AND in the past → reject: coupon itself has expired
    else → compute new trialEndDate same as built-in path (durationDays or 2099-forever sentinel)
    → write subscriptionStatus='active', trialEndDate=newEndDate, couponUsed=code on the FLAT
    → append this flatId to the coupon doc's usedBy[] array (one mutation, tracks consumption
      across possibly-multiple flats sharing a multi-use code)
```
**Condition:** redemption is a flat-level action, not a per-user one — once any eligible person (admin — see PERMISSIONS.md §5) redeems, the entire flat and every member on it gets `active` status simultaneously.
**Condition:** a code can never be "used up" partway — either all the checks above pass and status flips atomically, or none of the writes happen at all.

### 7.7 Condition: `maxFlats` cap by state
```
status == 'active' (premium/coupon redeemed) → maxFlats = 3
status == 'trial' or 'expired' or no subscription data at all → maxFlats = 1
```
This caps how many flats a single user account may belong to (as creator or joiner) at once — independent of role, and independent of any single flat's own member cap (the 8-member cap in CONDITIONS.md §6.1 is per-flat; `maxFlats` is per-user across all their flats).

---

## 8. Rewards Wallet

### 8.1 Data shape
Reward instance (per-user): `id, brandName, discountCode, description, unlockedAt, isRedeemed, expiryDate`.
Reward template (pool entry): `brandName, discountCode, description, isActive, expiryDays, category?`.

### 8.2 Condition: When a reward is even considered (fires only on task completion, never standalone)
Reward issuance is a side effect bolted onto task completion (CONDITIONS.md §1.4) — there is no separate "claim a reward" action. Both gates below must pass, evaluated in this order, and a failure at either gate means NO reward is issued for that completion (silently — the task still completes normally either way; reward logic is wrapped so it can never block or fail the completion itself):
```
GATE A — not retroactive:
    isRecentCompletion = (no explicit completionDate was passed, i.e. "now") 
                         OR (now - completedAt < 2 hours)
    → backdated completions older than 2h from actual submission time never earn a reward,
      even though the task itself still completes and rotates normally.

GATE B — cooldown, per-device (not per-user-account, not per-flat):
    lastRewardAt = read from a local-device timestamp key
    rewardCooldownPassed = (now - lastRewardAt) > 24 hours
    → this is a device-local cooldown (stored client-side), not server-enforced — a user
      switching devices resets the cooldown for that new device.

if GATE A and GATE B both true → issue a reward; else → no reward, no error, no user-facing signal.
```

### 8.3 Calculation: Which reward is picked (`getActiveRewardTemplate`)
```
priority order:
  1. localStorage cache of the reward pool, IF fresher than 6 hours → pick uniformly at random from it
  2. else, fetch active pool entries (isActive == true) from the shared/global reward catalog →
     cache them locally (resets the 6h TTL) → pick uniformly at random
  3. else (catalog unreachable/empty) → fall back to a small hardcoded pool, itself filtered to
     isActive entries → pick uniformly at random
     → if even the hardcoded pool has zero active entries (all manually disabled) → last-resort:
       return the first hardcoded entry regardless of its isActive flag (never returns nothing)
```
**Condition:** selection is uniformly random across whatever pool is in play — no weighting by category, user history, or expense patterns yet (category field exists on templates but is unused for matching today — reserved for a future personalization pass).
**Condition:** the reward pool itself is edited out-of-band (by whoever administers the global catalog, not by any in-app flat admin action) — no in-app flow in this codebase creates/edits pool entries; it's a read-only catalog from the client's perspective.

### 8.4 Condition: Constructing the reward instance at unlock time
```
new reward = {
    id: freshly generated
    brandName, discountCode, description: copied from the chosen template
    unlockedAt: now
    isRedeemed: false
    expiryDate: now + template.expiryDays
}
→ device cooldown timestamp is updated to now (regardless of whether the Firestore write below
  succeeds — the cooldown is enforced client-side and must advance even offline)
→ reward is surfaced to the UI immediately via a local signal (not dependent on network)
→ best-effort sync to persistent per-user storage is attempted separately and allowed to fail
  silently (never blocks the reward from showing locally)
```

### 8.5 Condition: Redemption (`markRewardRedeemed`)
```
the ONLY mutation ever permitted on an existing reward record is flipping isRedeemed: false → true.
No other field (brandName, discountCode, expiryDate, etc.) can change after issuance, by design —
a reward, once earned, is a fixed historical record; only its "have I used this code" flag moves.
Redemption is self-serve — always the reward's own owner, never an admin acting on someone else's.
```
**Condition:** redeeming does NOT check `expiryDate` — the app lets a user mark an expired reward as redeemed with no gate (expiry appears to be informational/UI-only in this codebase, not an enforced block on redemption).

### 8.6 Condition: Multi-device reconciliation
```
on first load of the rewards listener for a user:
    merge [server-synced rewards] + [this device's local-only rewards not yet on the server]
    server copy wins on any id collision
    result becomes the new local cache

on subsequent updates:
    if the server list is now LONGER than what this device last knew → treat the newest entry as
    a fresh unlock and surface the "reward unlocked" celebration UI for it (covers the case where
    a reward was earned on a DIFFERENT device/session and should still be celebrated here)
    if same length or shorter → just resync the cache silently, no celebration replay
```

---

## 9. Summary Table — Trigger → What Recalculates

| Trigger | Rotation affected? | Balances affected? | Notes |
|---|---|---|---|
| Task marked complete | ✅ advances to next available in queue | — | Pending swaps on that task force-rejected |
| Member goes OOS | ✅ all their pending/overdue tasks reassigned | — | Skips tasks already covered by an OOS-linked swap |
| Member returns | ✅ paused tasks resume to correct next-in-queue | — | Not necessarily back to the returner |
| Swap accepted | ✅ lateral reassignment, queue order untouched | — | May auto-trigger OOS status once all linked swaps accepted |
| Member joins | ✅ appended to tail of every queue | — | Never mid-inserted |
| Member leaves/kicked | ✅ removed from queues, position-preserving reassignment | — | Pending swaps involving them force-rejected |
| Daily Split expense added/edited/deleted | — | ✅ immediate pairwise recompute | Excludes deferred + bill-linked expenses |
| Bill instance marked 'paid' | — | ✅ enters month-end net balance | Not before — generation ≠ payment |
| Bill template participants edited | — | ✅ recomputes splits on live (non-final) instances only | Paid/skipped instances untouched |
| Settlement recorded | — | ✅ reduces both pairwise and month-net debt | |
| Month closed | — | ✅ produces carry-forward for next month | Only confirmed settlements reduce it |

| Trigger | Gating affected? | Reward affected? | Notes |
|---|---|---|---|
| Trial-hold window elapses (trialEndDate < now) | ✅ create_task/create_flat/add_expense/create_bill become blocked | — | Computed live on read, nothing writes 'expired' in the background |
| Coupon redeemed (built-in or Firestore doc) | ✅ un-gates all four features, flips status to 'active' | — | Flat-wide effect — every member benefits immediately |
| Task completed | — | ✅ evaluates both reward gates (non-retroactive + 24h cooldown) | Never blocks or fails the completion itself |
| Reward redeemed by user | — | ✅ flips isRedeemed only | No other reward field can change after issuance |
