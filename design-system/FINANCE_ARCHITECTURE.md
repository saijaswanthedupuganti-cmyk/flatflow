# Habitiq — Finance Module Architecture

> **Version:** 1.0 | **June 2026**
> **Status:** Architecture lock — no implementation starts without reading this.
> **Principle:** Model the flat as a monthly operating entity, not a transaction log.

---

## The Core Mental Model

```
Flat
 ├── Rooms                    ← physical spaces, optional
 ├── Members                  ← people + lifecycle events
 ├── Bill Templates           ← what repeats every month (NEVER transactions)
 ├── Monthly Cycle            ← the backbone: one entity per month
 │    ├── Bill Instances      ← template snapshotted into this month
 │    ├── Shared Expenses     ← ad-hoc spend logged this month
 │    ├── Settlements         ← who paid whom this month
 │    ├── Carry-Forward In    ← unpaid balance from last month
 │    └── Carry-Forward Out   ← unpaid balance going to next month
 └── Reports                  ← closed-month summaries
```

Everything flows through the monthly cycle. A month can be **open** (current) or **closed** (locked, immutable). Settlements reference a month. Carry-forward links two consecutive months.

---

## 1. Entity Map

### 1a. Room (optional, Phase 2)

Rooms let the flat model unequal rent. Without rooms, rent splits equally.

```
Room
  id:           string
  name:         string            "Master Bedroom", "Room 2", "Room 3"
  type:         master | standard | shared
  capacity:     number            1 = private, 2+ = shared room
  baseRent:     number | null     override amount; null = use proportional
  occupants:    uid[]             current occupants
```

**Rule:** If a room has 2 occupants, its `baseRent` is divided between them. If `baseRent` is null, rent is divided proportionally (master gets more, small room gets less — admin defines the split).

---

### 1b. Bill Template (RecurringBill — enhanced)

Templates are configurations. They never hold money.

```
RecurringBill (template)
  id:               string
  name:             string            "Rent", "Electricity", "WiFi"
  category:         ExpenseCategory
  type:             fixed | variable
  amount:           number | null     null for variable
  currency:         Currency
  
  participants:     uid[] | 'all'     WHO this bill applies to
  splitMethod:      equal             for now; room-based in Phase 2
  
  payerRotation:    uid[]             who physically pays the landlord/utility
  currentPayerIndex: number
  
  billingDay:       number            day of month to generate
  status:           active | paused | archived
  
  lastGeneratedMonth: string | null   YYYY-MM
  createdBy:        uid
  createdAt:        timestamp
```

**Key distinction — participants vs payerRotation:**
- `participants` = who SPLITS the cost (who is responsible for this bill)
- `payerRotation` = who PAYS upfront to the landlord/company

These can differ. Example: WiFi is paid by A (rotation), but split among A, B, C only (D has their own connection). A pays the bill, then B and C owe A their shares.

---

### 1c. MonthCycle — the backbone

One document per flat per month. This is what makes monthly lifecycle work.

```
MonthCycle
  id:               string            flatId_YYYY-MM
  flatId:           string
  month:            string            YYYY-MM
  status:           open | closing | closed
  
  openedAt:         timestamp
  closedAt:         timestamp | null
  
  totalBillsINR:    number            sum of all bill instances
  totalExpensesINR: number            sum of all shared expenses
  totalSettledINR:  number            sum of settlements this month
  
  netBalances:      { [uid]: number } positive = owed to you, negative = you owe
  
  carryForwardIn:   { fromMonth: string, balances: { [uid]: number } } | null
  carryForwardOut:  { toMonth: string, balances: { [uid]: number } } | null
  
  memberEvents:     MemberEvent[]     joins/leaves/room changes this month
  
  createdBy:        uid               who opened this month (auto or admin)
```

**Month status transitions:**
```
open → closing → closed
         ↑
   Admin initiates month-end settlement.
   'closing' = settlement in progress.
   'closed' = locked, immutable. Carry-forward written.
```

---

### 1d. BillInstance — transactional layer

When a template is generated for a month, it creates a BillInstance. This is the transaction.

```
BillInstance
  id:               string
  templateId:       string            points to RecurringBill
  month:            string            YYYY-MM
  
  name:             string            snapshot of template name at generation
  category:         ExpenseCategory   snapshot
  amount:           number            actual for variable; from template for fixed
  currency:         Currency
  
  participants:     uid[]             snapshot of who splits this
  paidBy:           uid               who advanced the money
  splits:           { [uid]: amount } computed at generation
  
  status:           pending | entered | split_generated | paid | overdue | skipped
  
  dueDate:          timestamp
  paidAt:           timestamp | null
  skippedReason:    string | null
  
  generatedAt:      timestamp
  generatedBy:      uid
```

**Bill status state machine:**
```
pending           ← bill is due but amount not entered (variable bills)
    ↓
entered           ← amount confirmed (variable) or auto for fixed
    ↓
split_generated   ← per-person splits computed, members notified
    ↓
paid              ← payer confirms payment made
    ↓
overdue           ← past due date, not yet paid (cron/trigger sets this)

Any state → skipped  ← admin explicitly skips this bill this month
```

---

### 1e. Expense (enhanced, existing)

Mostly good. Key additions:

```
Expense (existing fields +)
  month:            string            YYYY-MM  ← NEW: which cycle this belongs to
  paidBy:           uid | { [uid]: amount }    ← support multiple payers
  deferToSettlement: boolean          renamed from deferToNextMonth, clearer intent
  linkedBillInstanceId: string | null ← if this expense was created from a bill instance
```

**Multiple payers:** When `paidBy` is an object, each entry is a co-payer with their contribution. Example: Sai paid ₹500 and Rahul paid ₹500 toward the same ₹1000 grocery run. The debt computation handles this.

---

### 1f. Settlement (enhanced, existing)

```
Settlement (existing fields +)
  month:            string            YYYY-MM  ← which cycle this settles
  type:             immediate | month_end | carry_forward | rent_adjustment
  coveredExpenses:  string[]          expense IDs this settles (for month_end)
  coveredBills:     string[]          bill instance IDs this settles
  note:             string | null
```

**Settlement types:**

| Type | When used | Description |
|------|-----------|-------------|
| `immediate` | Anytime | Pay now, single debt |
| `month_end` | Month closing | Bulk settlement of all month's debts |
| `carry_forward` | Month closing | Unpaid balance moved to next month |
| `rent_adjustment` | Month closing | Expense offset against rent rather than cash |

---

### 1g. CarryForward (computed, stored on MonthCycle)

Not a separate collection — stored as fields on MonthCycle. When month closes:

1. Compute net balances for the month (bills + expenses + settlements)
2. Any non-zero balance → write as `carryForwardOut` on closing month
3. Next month's MonthCycle reads it as `carryForwardIn`
4. Carry-forward is included in that month's balance computation

---

### 1h. MemberEvent (lifecycle tracking)

```
MemberEvent
  id:               string
  flatId:           string
  uid:              string
  type:             joined | left | room_changed | reactivated | went_oos | returned
  month:            string            YYYY-MM
  date:             timestamp         exact date of event
  fromRoomId:       string | null
  toRoomId:         string | null
  prorationFactor:  number            0.0–1.0, days active / days in month
  notes:            string | null
```

**Proration formula:**
```
joined on day D of month with T total days:
  factor = (T - D + 1) / T

left on day D:
  factor = D / T

Example: joins June 15 (30-day month):
  factor = (30 - 15 + 1) / 30 = 16/30 = 0.533
  → pays 53.3% of equal share for that month
```

---

## 2. Firestore Schema

```
/flats/{flatId}
  ...existing fields...
  billingCycleDay:  number    1–28, when each month's cycle starts
  baseCurrency:     Currency  INR by default

  /members/{uid}              ← existing
  /rooms/{roomId}             ← NEW Phase 2
  /tasks/{taskId}             ← existing
  /swapRequests/{id}          ← existing
  /activityLog/{id}           ← existing
  /npsResponses/{id}          ← existing
  /joinRequests/{id}          ← existing
  
  /recurringBills/{id}        ← enhanced (participants, splitMethod)
  
  /monthCycles/{YYYY-MM}      ← NEW (one per month)
  
  /billInstances/{id}         ← NEW (generated from templates into cycles)
  
  /expenses/{id}              ← enhanced (month field, multiple payers)
  
  /settlements/{id}           ← enhanced (type, month, coveredExpenses)
  
  /memberEvents/{id}          ← NEW (lifecycle tracking)
```

---

## 3. Monthly Cycle Flow

### Opening a Month

Triggered automatically when `billingCycleDay` arrives OR admin opens manually.

```
1. Create MonthCycle { month: YYYY-MM, status: open }
2. Copy carryForwardOut from previous month → carryForwardIn
3. For each active RecurringBill:
   - If billingDay ≤ today → create BillInstance { status: 'pending' (variable) or 'entered' (fixed) }
4. Notify admin: "X bills ready for this month"
```

### During the Month

- Variable bills: admin enters amount → status moves to `entered` → splits computed → `split_generated`
- Ad-hoc expenses: logged against this month's cycle
- Settlements: can happen anytime (immediate type)
- Bill payer confirms payment → `paid`

### Closing a Month (Month-End Settlement)

Admin-initiated. This is the key flow the user described.

```
1. MonthCycle.status → 'closing'
2. Compute net balances:
   a. For each BillInstance: credit paidBy, debit each participant by their split
   b. For each Expense: credit paidBy, debit each splitAmong member
   c. Add carryForwardIn balances
   d. Subtract any settlements already recorded this month
3. Run debt simplification algorithm → minimum transfer set
4. Present to admin: "Suggested settlements for [month]"
5. Admin reviews and confirms (or edits)
6. Mark each as 'rent_adjustment' or 'month_end' settlement
7. Any remaining unpaid → write carryForwardOut
8. MonthCycle.status → 'closed'
9. Write MonthlyReport summary
```

### Rent Adjustment Flow

This is the key feature the user described:

```
Example:
  Sai bought groceries ₹3000 (expense logged)
  Sai's rent share this month = ₹5000

At month-end settlement:
  Instead of Sai collecting ₹3000 from flatmates:
  → Sai pays only ₹2000 rent this month
  → The ₹3000 grocery expense is "adjusted" against rent
  → Settlement type = rent_adjustment
  → No cash changes hands for the groceries
```

How it works in the system:
1. During closing flow, show "Adjust against rent?" option per expense
2. If selected: the expense becomes a negative deduction from that member's rent obligation
3. Payer effectively pays less rent; the bill still shows full amount

---

## 4. Debt Simplification Algorithm

For N members, the naive approach generates O(N²) transfers. The simplified approach generates at most N-1.

```
Algorithm (Greedy Minimum Cash Flow):

1. Compute net balance for each member (positive = owed to them, negative = they owe)
2. Sort into two lists: creditors (positive) and debtors (negative)
3. While both lists non-empty:
   a. Take largest creditor (C) and largest debtor (D)
   b. Transfer = min(C.balance, abs(D.balance))
   c. Record settlement: D pays C the transfer amount
   d. Reduce C.balance by transfer, reduce D.balance by transfer
   e. Remove if zero
4. Result: list of minimum settlements

Example:
  A: +500, B: -200, C: +100, D: -400
  → B pays A ₹200, D pays A ₹300, D pays C ₹100
  → 3 transfers instead of 6
```

This runs in O(N log N). For 8 members max, it's instant.

---

## 5. What Exists vs What Needs Building

### What's Already Good — Keep As-Is

| Feature | Where | Notes |
|---------|-------|-------|
| RecurringBill template | `useFlatStore.ts` | Add participants field, split methods |
| Expense logging | `expenses/page.tsx` | Add month field, multiple payers |
| Balance computation | `expenseUtils.ts` | Extend to be month-scoped |
| Bill generation modal | `expenses/page.tsx` | Keep UX, change what it creates |
| Category system | `expenses/page.tsx` | Good, keep exactly as-is |
| Currency system | `expenses/page.tsx` | Good, keep exactly as-is |
| Defer to next month | `Expense.deferToNextMonth` | Rename to deferToSettlement |

### What Needs New Architecture

| Gap | Current state | What to build |
|-----|--------------|---------------|
| Monthly cycle | No concept | `MonthCycle` entity + Firestore doc |
| Bill status lifecycle | just active/inactive | 6-state machine on BillInstance |
| Bill instances | generateBill() → Expense | BillInstance as separate entity |
| Month-end settlement | No concept | Closing flow + debt simplification |
| Carry-forward | No concept | CarryForward field on MonthCycle |
| Rent adjustment | `deferToNextMonth` (partial) | Full rent_adjustment settlement type |
| Participant lists | rotationQueue only | Separate participants + payerRotation |
| Multiple payers | Single paidBy | Object paidBy |
| Member lifecycle | No concept | MemberEvent + prorationFactor |

### What to NOT Build Yet (architecture supports it, don't implement)

- Rooms / room-based rent (Phase 2)
- Proration for mid-month joins (Phase 2)
- Guest tracking (Phase 2)
- Security deposit (Phase 3)
- Household budget (Phase 3)
- Fine system (Phase 3)
- Advance payments (Phase 3)

---

## 6. Build Order (Finance Module Phases)

### Phase 2a — Foundation (build this next)

1. Add `month` field to Expense and Settlement (non-breaking, migration optional)
2. Add `participants` field to RecurringBill (replaces rotationQueue for split logic)
3. Create `MonthCycle` Firestore collection (lightweight, one doc per month)
4. Create `BillInstance` collection (separate from Expense)
5. Update `generateBill()` to create BillInstance instead of Expense
6. Add BillInstance status lifecycle (UI: show status badge per bill)

### Phase 2b — Month-End Flow

7. Month-end settlement trigger (admin-initiated)
8. Debt simplification algorithm (`lib/settlementUtils.ts`)
9. Month close flow (compute, suggest, confirm, lock)
10. Carry-forward display on next month's balance view
11. Rent adjustment flow in settlement modal

### Phase 2c — History & Reports

12. Monthly report view (closed months as read-only history)
13. Per-month balance view (what was owed, what was settled)
14. Export as PDF/CSV (Phase 3 but architect for it now)

---

## 7. Exceptions and Edge Cases

| Scenario | How to handle |
|----------|--------------|
| Admin generates a bill but nobody pays | BillInstance stays `split_generated`. At month-end, debt shows in balances. Not an error. |
| Variable bill never entered | Stays `pending`. Month-end flow warns admin. Can skip with reason. |
| Member leaves mid-month | Leave event recorded. Balance settled at month-end or immediately. Tasks reassigned (existing logic). Future months: they don't appear in any bills. |
| Same member in two flats | Each flat has independent MonthCycle. Balances don't cross. This is correct — already handled by multi-flat architecture. |
| Month closed but dispute arises | Closed months are immutable. Admin can manually add a correction expense in next month and mark note "correction for [previous month]". |
| Electricity bill varies but nobody entered it | Month-end: system surfaces unconfirmed variable bills before allowing close. Close is blocked until all active variable bills are either entered or skipped. |
| All members settle before month-end | Month-end close still runs (compute → all balances zero → confirm → close). No harm. |
| Partial payment | Settlement records partial amount. Remaining shows in outstanding balance. Carry-forward if month closes. |
| Rent adjustment makes net balance negative | Handled: if adjustment > owed, member gets carry-forward credit into next month. |
| Two people share a room | Room capacity = 2. Room rent divided by 2. Each person's share is room_rent / 2. Phase 2. |

---

## 8. What the Current UI Gets Right

These screens are well-designed and mostly correct — they need data model changes, not UX redesigns:

- **Bill row with "DUE" badge** → becomes BillInstance status badge
- **Generate modal** → becomes "Enter amount + confirm" for BillInstance
- **Balance summary** → extends to show month-scoped + carry-forward
- **Expense list grouped by month** → stays, just gets month header with total
- **Defer to next month toggle** → becomes "Add to month-end settlement" (same concept, better name)
- **Category grid** → stays exactly as-is

---

## 9. Revenue Model Alignment

The `category` field on expenses is the foundation for the ad-targeting revenue model described in the project docs. This architecture preserves it:

- Every BillInstance has a `category` (from template)
- Every Expense has a `category`
- MonthCycle aggregates `categoryTotals: { [category]: number }` for analytics

This feeds directly into: electricity → energy brand ads, grocery → FMCG ads, etc.

**Rule:** Category granularity must be maintained at the individual transaction level. Never aggregate categories prematurely. The revenue model depends on it.

---

*Architecture by: Venkata Sai Jaswanth E · June 2026*
*Source: `C:\garbage\design-system\FINANCE_ARCHITECTURE.md`*
*Next step: Review → approve → start with Phase 2a*
