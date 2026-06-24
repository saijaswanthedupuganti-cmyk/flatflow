import type { Expense, BillInstance, Settlement } from '@/store/useFlatStore'

export interface SuggestedSettlement {
  fromUserId: string
  toUserId: string
  amount: number
}

export interface MonthSummary {
  totalBillsINR: number
  totalExpensesINR: number
  totalSettledINR: number
  netBalances: Record<string, number>
  pendingVariableBills: number   // variable instances still pending (no amount entered)
  unpaidBillsCount: number       // bills split_generated but not yet marked paid to vendor
}

// ── Per-member bill summary for the collector view ────────────────────────────
export interface MemberBillSummary {
  totalShare: number        // sum of all bill splits owed by this member this month
  contributions: number     // full amount this member paid to vendors from pocket (includes own share)
  settled: number           // amount already paid to/from the collector via settlements
  outstanding: number       // totalShare - contributions - settled (positive = owes collector)
}

// ── Helpers ────────────────────────────────────────────────────────────────────

export function currentMonthKey(): string {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

export function prevMonthKey(month: string): string {
  const [y, m] = month.split('-').map(Number)
  const prev = new Date(y, m - 2, 1)
  return `${prev.getFullYear()}-${String(prev.getMonth() + 1).padStart(2, '0')}`
}

export function nextMonthKey(month: string): string {
  const [y, m] = month.split('-').map(Number)
  const next = new Date(y, m, 1)
  return `${next.getFullYear()}-${String(next.getMonth() + 1).padStart(2, '0')}`
}

export function monthLabel(yyyymm: string): string {
  const [y, m] = yyyymm.split('-')
  return new Date(Number(y), Number(m) - 1, 1).toLocaleDateString('en-IN', {
    month: 'long', year: 'numeric',
  })
}

// ── Core computation ───────────────────────────────────────────────────────────

/**
 * Computes net INR balances for a specific month.
 *   positive → uid is owed money by others
 *   negative → uid owes money to others
 *
 * IMPORTANT: Bill instances only enter the balance calculation when status === 'paid'.
 * Creating a bill does NOT create a credit entry for the designated handler.
 * Credit is only given once the vendor payment is confirmed (markBillPaid).
 *
 * Sources (in order applied):
 *   1. carry-forward from previous month
 *   2. paid bill instances (status === 'paid') — payer credited, participants debited
 *   3. ad-hoc expenses
 *   4. settlements already recorded this month (reduces debt)
 *
 * Foreign-currency items are excluded — month-end settlement is INR only.
 * Expense.month field is used first; falls back to date prefix if unset.
 */
export function computeMonthNetBalances(
  month: string,
  expenses: Expense[],
  billInstances: BillInstance[],
  settlements: Settlement[],
  carryForwardIn: Record<string, number> | null,
): Record<string, number> {
  const net: Record<string, number> = {}

  const bump = (uid: string, delta: number) => {
    net[uid] = (net[uid] ?? 0) + delta
  }

  // 1. Carry-forward from previous month
  if (carryForwardIn) {
    for (const [uid, amount] of Object.entries(carryForwardIn)) {
      if (Math.abs(amount) >= 0.5) bump(uid, amount)
    }
  }

  // 2. Paid bill instances — only credit the actual vendor payer, never the designated handler
  for (const instance of billInstances) {
    if (instance.month !== month) continue
    if (instance.status !== 'paid') continue  // creating a bill ≠ paying a bill
    if (!instance.splits || instance.currency !== 'INR') continue
    for (const uid of instance.participants) {
      if (uid === instance.paidBy) continue
      const owes = instance.splits[uid] ?? 0
      if (owes <= 0) continue
      bump(instance.paidBy, owes)
      bump(uid, -owes)
    }
  }

  // 3. Ad-hoc expenses for this month
  for (const expense of expenses) {
    const expMonth = expense.month ?? expense.date.substring(0, 7)
    if (expMonth !== month) continue
    if (expense.deferToNextMonth) continue
    if (expense.currency !== 'INR') continue
    for (const uid of expense.splitAmong) {
      if (uid === expense.paidBy) continue
      const owes = expense.splits[uid] ?? 0
      if (owes <= 0) continue
      bump(expense.paidBy, owes)
      bump(uid, -owes)
    }
  }

  // 4. Settlements already recorded for this month (subtract from debt)
  for (const settlement of settlements) {
    if (settlement.currency !== 'INR') continue
    const settMonth = settlement.month ?? settlement.date.substring(0, 7)
    if (settMonth !== month) continue
    bump(settlement.fromUserId, settlement.amount)
    bump(settlement.toUserId, -settlement.amount)
  }

  return net
}

/**
 * Per-member bill summary for the collector's settlement view.
 *
 * For each member in the flat this month:
 *   totalShare    = sum of their bill splits (all non-skipped instances)
 *   contributions = net vendor payment credit (prepaid bills they personally paid,
 *                   excluding their own share — what they effectively covered for the room)
 *   settled       = amount already paid to/from the collector via settlement records
 *   outstanding   = totalShare - contributions - settled
 *                   positive → member owes collector
 *                   negative → collector owes member
 *
 * This is separate from computeMonthNetBalances. It is used by the collector
 * to see who has paid and who still owes, regardless of bill payment status.
 */
export function computeMemberBillSummary(
  month: string,
  billInstances: BillInstance[],
  settlements: Settlement[],
  collectorUid: string,
): Record<string, MemberBillSummary> {
  const result: Record<string, MemberBillSummary> = {}

  const ensure = (uid: string) => {
    if (!result[uid]) result[uid] = { totalShare: 0, contributions: 0, settled: 0, outstanding: 0 }
    return result[uid]
  }

  // Accumulate each member's bill share (all non-skipped instances)
  for (const instance of billInstances) {
    if (instance.month !== month) continue
    if (instance.status === 'skipped') continue
    if (!instance.splits || instance.currency !== 'INR') continue

    for (const uid of instance.participants) {
      const share = instance.splits[uid] ?? 0
      if (share > 0) ensure(uid).totalShare += share
    }

    // If someone paid the vendor, credit the full amount they paid from pocket.
    // Their own share is already in totalShare above, so contributions = full amount
    // ensures outstanding = 0 for a payer whose share equals what they paid.
    if (instance.status === 'paid' && instance.amount != null && instance.amount > 0) {
      ensure(instance.paidBy).contributions += instance.amount
    }
  }

  // Apply settlements between members and the collector
  for (const s of settlements) {
    if (s.currency !== 'INR') continue
    const settMonth = s.month ?? s.date.substring(0, 7)
    if (settMonth !== month) continue

    if (s.toUserId === collectorUid) {
      // Member paid collector → increases their settled amount
      ensure(s.fromUserId).settled += s.amount
    } else if (s.fromUserId === collectorUid) {
      // Collector paid member (credit back) → reduces settled
      ensure(s.toUserId).settled -= s.amount
    }
  }

  // Compute final outstanding for each member
  for (const uid in result) {
    const r = result[uid]
    r.outstanding = Math.round(r.totalShare - r.contributions - r.settled)
  }

  return result
}

/**
 * Greedy minimum cash flow: produces at most N-1 transfers for N people.
 * Pairs the largest debtor with the largest creditor on each iteration.
 */
export function suggestSettlements(
  netBalances: Record<string, number>,
): SuggestedSettlement[] {
  const people = Object.entries(netBalances).map(([uid, amt]) => ({ uid, amt }))
  const debtors   = people.filter(p => p.amt < -0.5).sort((a, b) => a.amt - b.amt)
  const creditors = people.filter(p => p.amt >  0.5).sort((a, b) => b.amt - a.amt)

  const result: SuggestedSettlement[] = []
  let di = 0, ci = 0

  while (di < debtors.length && ci < creditors.length) {
    const d = debtors[di]
    const c = creditors[ci]
    const payment = Math.min(Math.abs(d.amt), c.amt)
    if (payment >= 0.5) {
      result.push({ fromUserId: d.uid, toUserId: c.uid, amount: Math.round(payment) })
    }
    d.amt += payment
    c.amt -= payment
    if (Math.abs(d.amt) < 0.5) di++
    if (c.amt < 0.5) ci++
  }

  return result
}

/**
 * After some suggested settlements are confirmed and others are skipped,
 * computes what net balances remain (will become carry-forward).
 * Returns null if all balances are zero.
 */
export function computeCarryForward(
  netBalances: Record<string, number>,
  confirmed: SuggestedSettlement[],
): Record<string, number> | null {
  const remaining: Record<string, number> = { ...netBalances }
  for (const s of confirmed) {
    remaining[s.fromUserId] = (remaining[s.fromUserId] ?? 0) + s.amount
    remaining[s.toUserId]   = (remaining[s.toUserId]   ?? 0) - s.amount
  }
  const nonZero = Object.fromEntries(
    Object.entries(remaining).filter(([, v]) => Math.abs(v) >= 0.5),
  )
  return Object.keys(nonZero).length > 0 ? nonZero : null
}

/**
 * Summarises a month: totals and blocker counts.
 * unpaidBillsCount counts split_generated instances that haven't been marked paid yet.
 * These should be resolved before closing the month.
 */
export function buildMonthSummary(
  month: string,
  expenses: Expense[],
  billInstances: BillInstance[],
  settlements: Settlement[],
  carryForwardIn: Record<string, number> | null,
): MonthSummary {
  let totalBillsINR = 0
  let totalExpensesINR = 0
  let totalSettledINR = 0
  let pendingVariableBills = 0
  let unpaidBillsCount = 0

  for (const b of billInstances) {
    if (b.month !== month) continue
    if (b.status === 'skipped') continue
    if (b.status === 'pending') { pendingVariableBills++; continue }
    if (b.status === 'split_generated' || b.status === 'overdue') unpaidBillsCount++
    if (b.currency === 'INR' && b.amount) totalBillsINR += b.amount
  }
  for (const e of expenses) {
    const m = e.month ?? e.date.substring(0, 7)
    if (m !== month || e.deferToNextMonth || e.currency !== 'INR') continue
    totalExpensesINR += e.amount
  }
  for (const s of settlements) {
    const m = s.month ?? s.date.substring(0, 7)
    if (m !== month || s.currency !== 'INR') continue
    totalSettledINR += s.amount
  }

  const netBalances = computeMonthNetBalances(
    month, expenses, billInstances, settlements, carryForwardIn,
  )

  return { totalBillsINR, totalExpensesINR, totalSettledINR, netBalances, pendingVariableBills, unpaidBillsCount }
}
