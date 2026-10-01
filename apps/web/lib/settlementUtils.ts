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
  pendingVariableBills: number   // blocker count: variable instances still pending
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
 * Sources (in order applied):
 *   1. carry-forward from previous month
 *   2. bill instances (generated recurring bills)
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

  // 2. Bill instances for this month (recurring bills that were generated)
  for (const instance of billInstances) {
    if (instance.month !== month) continue
    if (instance.status === 'skipped') continue
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
 * Summarises a month: totals and blocker count.
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

  for (const b of billInstances) {
    if (b.month !== month) continue
    if (b.status === 'skipped') continue
    if (b.status === 'pending') { pendingVariableBills++; continue }
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

  return { totalBillsINR, totalExpensesINR, totalSettledINR, netBalances, pendingVariableBills }
}
