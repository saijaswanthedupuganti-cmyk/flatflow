import type { Expense, Settlement, BillInstance, Currency } from '@/store/useFlatStore'

export interface Balance {
  userId: string
  amount: number     // positive = they owe you; negative = you owe them
  currency: Currency
}

/**
 * Computes the current user's net balances using global debt simplification.
 *
 * Sources:
 *   - Expenses (ad-hoc shared spend)
 *   - BillInstances with status split_generated or paid (recurring bills)
 *   - Settlements (recorded payments)
 *
 * Debt chains are collapsed: if A owes B ₹500 and B owes C ₹500,
 * the result is A pays C directly — B has zero net obligation.
 * Deferred expenses are excluded from immediate balance calculation.
 */
export function computeBalances(
  currentUserId: string,
  expenses: Expense[],
  settlements: Settlement[],
  billInstances: BillInstance[] = [],
): Balance[] {
  // net[currency][uid] > 0 → uid is a net creditor (others owe uid)
  // net[currency][uid] < 0 → uid is a net debtor (uid owes others)
  const net: Record<string, Record<string, number>> = {}

  const bump = (currency: string, uid: string, delta: number) => {
    if (!net[currency]) net[currency] = {}
    net[currency][uid] = (net[currency][uid] ?? 0) + delta
  }

  // ── Ad-hoc expenses ────────────────────────────────────────────────────────
  for (const expense of expenses) {
    if (expense.deferToNextMonth) continue
    const { paidBy, splitAmong, splits, currency } = expense
    for (const uid of splitAmong) {
      if (uid === paidBy) continue
      const owes = splits[uid] ?? 0
      if (owes <= 0) continue
      bump(currency, paidBy, owes)
      bump(currency, uid, -owes)
    }
  }

  // ── Bill instances (recurring bills that have been generated) ──────────────
  // Only include instances where splits are computed (split_generated or paid).
  for (const instance of billInstances) {
    if (instance.status !== 'split_generated' && instance.status !== 'paid') continue
    if (!instance.splits) continue
    const { paidBy, participants, splits, currency } = instance
    for (const uid of participants) {
      if (uid === paidBy) continue
      const owes = splits[uid] ?? 0
      if (owes <= 0) continue
      bump(currency, paidBy, owes)
      bump(currency, uid, -owes)
    }
  }

  // ── Settlements ────────────────────────────────────────────────────────────
  for (const { fromUserId, toUserId, amount, currency } of settlements) {
    bump(currency, fromUserId, amount)
    bump(currency, toUserId, -amount)
  }

  // ── Minimum cash flow simplification per currency ──────────────────────────
  // Greedily pair the largest debtor with the largest creditor, producing at
  // most N-1 transfers for N people (vs the naive O(N²) pairwise approach).
  const result: Balance[] = []

  for (const [currency, balances] of Object.entries(net)) {
    const people = Object.entries(balances).map(([uid, amt]) => ({ uid, amt }))
    const debtors   = people.filter(p => p.amt < -0.5).sort((a, b) => a.amt - b.amt)
    const creditors = people.filter(p => p.amt >  0.5).sort((a, b) => b.amt - a.amt)

    let di = 0, ci = 0
    while (di < debtors.length && ci < creditors.length) {
      const d = debtors[di]
      const c = creditors[ci]
      const payment = Math.min(Math.abs(d.amt), c.amt)

      if (payment >= 0.5) {
        if (d.uid === currentUserId) {
          result.push({ userId: c.uid, amount: -Math.round(payment), currency: currency as Currency })
        } else if (c.uid === currentUserId) {
          result.push({ userId: d.uid, amount: Math.round(payment), currency: currency as Currency })
        }
      }

      d.amt += payment
      c.amt -= payment
      if (Math.abs(d.amt) < 0.5) di++
      if (c.amt < 0.5) ci++
    }
  }

  return result
}

const CURRENCY_SYMBOLS: Record<Currency, string> = {
  INR: '₹', USD: '$', EUR: '€', GBP: '£', AED: 'AED ', SGD: 'S$', AUD: 'A$',
}

export function formatAmount(amount: number, currency: Currency): string {
  const sym = CURRENCY_SYMBOLS[currency] ?? ''
  const locale = currency === 'INR' ? 'en-IN' : 'en-US'
  const decimals = currency === 'INR' ? 0 : 2
  return sym + new Intl.NumberFormat(locale, {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(Math.abs(amount))
}
