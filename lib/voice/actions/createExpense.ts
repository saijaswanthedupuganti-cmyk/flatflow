import { contextCache } from '../nlu/contextResolver'
import type { ExtractedEntities } from '../nlu/entityExtractor'
import type { VoiceFlatContext } from '../nlu/contextResolver'
import type { ActionResult, VoiceStoreActions } from './types'
import type { Expense } from '@/store/useFlatStore'

export async function executeCreateExpense(
  entities: ExtractedEntities,
  context: VoiceFlatContext,
  store: Pick<VoiceStoreActions, 'addExpense'>,
): Promise<ActionResult> {
  const { currentUser, members } = context

  // Amount is required
  if (!entities.amount || entities.amount <= 0) {
    return {
      success: false,
      action: 'CREATE_EXPENSE',
      message: "How much did you spend? Say the amount.",
      followUp: 'How much?',
    }
  }

  const amount = entities.amount
  const currency: Expense['currency'] = entities.currency ?? 'INR'

  // Determine who splits the expense
  let splitAmong: string[]

  if (entities.memberUid && !entities.allMembers) {
    // Specified a single member → split between payer and that member only
    splitAmong = [currentUser.uid, entities.memberUid].filter(
      (uid, i, arr) => arr.indexOf(uid) === i,  // deduplicate
    )
  } else {
    // Default: split among all flat members
    splitAmong = members.map(m => m.uid)
  }

  // Equal splits (remainder to last person)
  const perPerson = Math.floor((amount / splitAmong.length) * 100) / 100
  const splits: Record<string, number> = {}
  let allocated = 0
  splitAmong.forEach((uid, i) => {
    if (i === splitAmong.length - 1) {
      splits[uid] = Math.round((amount - allocated) * 100) / 100
    } else {
      splits[uid] = perPerson
      allocated   += perPerson
    }
  })

  const today   = new Date()
  const date    = entities.date ?? today.toISOString().split('T')[0]
  const month   = date.substring(0, 7)

  // Build a readable description
  const description = buildDescription(entities)

  const expenseData: Omit<Expense, 'id' | 'createdAt'> = {
    description,
    amount,
    currency,
    paidBy:     currentUser.uid,
    splitAmong,
    splitType:  'equal',
    splits,
    category:   entities.category ?? 'other',
    date,
    month,
    createdBy:  currentUser.uid,
  }

  await store.addExpense(expenseData)
  contextCache.invalidate(context.flatId)

  // Build confirmation message
  const sym         = currency === 'INR' ? '₹' : currency + ' '
  const perPersonFmt = `${sym}${Math.round(perPerson)}`
  const splitDesc   = splitAmong.length > 1
    ? `, split ${splitAmong.length} ways (${perPersonFmt} each)`
    : ''

  return {
    success: true,
    action: 'CREATE_EXPENSE',
    message: `Added ${sym}${amount} for ${description}${splitDesc}.`,
    data: {
      amount,
      currency,
      description,
      splitAmong,
      splits,
      category: expenseData.category,
      // UI layer uses this to show an undo card for 5 seconds
      canUndo: true,
    },
    followUp: 'Undo anytime from the Money tab.',
  }
}

function buildDescription(entities: ExtractedEntities): string {
  // Use explicit description if meaningful
  if (entities.description && entities.description.length > 2) {
    // Capitalise first letter
    return entities.description.charAt(0).toUpperCase() + entities.description.slice(1)
  }
  // Fall back to category name
  if (entities.category && entities.category !== 'other') {
    const MAP: Record<string, string> = {
      grocery: 'Groceries', food: 'Food', rent: 'Rent',
      electricity: 'Electricity', water: 'Water Bill',
      internet: 'Internet', gas: 'Gas', maid: 'Maid',
      cook: 'Cook', gym: 'Gym', milk: 'Milk', ac: 'AC',
      maintenance: 'Maintenance', household: 'Household',
    }
    return MAP[entities.category] ?? entities.category
  }
  return 'Voice expense'
}
