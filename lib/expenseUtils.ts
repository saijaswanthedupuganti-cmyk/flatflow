import type { Expense, Settlement, BillInstance, Currency } from '@/store/useFlatStore'

export interface Balance {
  userId: string
  amount: number     // positive = they owe you; negative = you owe them
  currency: Currency
}

/**
 * Computes the current user's net balances as DIRECT pairwise amounts.
 *
 * For each person the current user has transacted with, we return a single
 * net number: positive = they owe you, negative = you owe them.
 * No debt-chain simplification — balances always reflect actual transactions.
 *
 * Sources: expenses, bill instances (split_generated or paid), settlements.
 * Deferred expenses are excluded.
 */
export function computeBalances(
  currentUserId: string,
  expenses: Expense[],
  settlements: Settlement[],
  billInstances: BillInstance[] = [],
): Balance[] {
  // pair[currency][uid] > 0 → uid owes currentUser
  // pair[currency][uid] < 0 → currentUser owes uid
  const pair: Record<string, Record<string, number>> = {}

  const bump = (currency: string, uid: string, delta: number) => {
    if (!pair[currency]) pair[currency] = {}
    pair[currency][uid] = (pair[currency][uid] ?? 0) + delta
  }

  // ── Ad-hoc expenses ────────────────────────────────────────────────────────
  for (const expense of expenses) {
    if (expense.deferToNextMonth) continue
    const { paidBy, splitAmong, splits, currency } = expense
    if (paidBy === currentUserId) {
      for (const uid of splitAmong) {
        if (uid === currentUserId) continue
        const owes = splits[uid] ?? 0
        if (owes > 0) bump(currency, uid, owes)
      }
    } else if (splitAmong.includes(currentUserId)) {
      const myShare = splits[currentUserId] ?? 0
      if (myShare > 0) bump(currency, paidBy, -myShare)
    }
  }

  // ── Bill instances ─────────────────────────────────────────────────────────
  for (const instance of billInstances) {
    if (instance.status !== 'split_generated' && instance.status !== 'paid') continue
    if (!instance.splits) continue
    const { paidBy, participants, splits, currency } = instance
    if (paidBy === currentUserId) {
      for (const uid of participants) {
        if (uid === currentUserId) continue
        const owes = splits[uid] ?? 0
        if (owes > 0) bump(currency, uid, owes)
      }
    } else if (participants.includes(currentUserId)) {
      const myShare = splits[currentUserId] ?? 0
      if (myShare > 0) bump(currency, paidBy, -myShare)
    }
  }

  // ── Settlements ────────────────────────────────────────────────────────────
  for (const { fromUserId, toUserId, amount, currency } of settlements) {
    if (fromUserId === currentUserId) {
      bump(currency, toUserId, amount)       // currentUser paid toUser → reduces debt
    } else if (toUserId === currentUserId) {
      bump(currency, fromUserId, -amount)    // fromUser paid currentUser → reduces what they owe
    }
  }

  // ── Flatten to Balance[] ───────────────────────────────────────────────────
  const result: Balance[] = []
  for (const [currency, pairs] of Object.entries(pair)) {
    for (const [uid, amount] of Object.entries(pairs)) {
      if (Math.abs(amount) >= 0.5) {
        result.push({ userId: uid, amount: Math.round(amount), currency: currency as Currency })
      }
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
