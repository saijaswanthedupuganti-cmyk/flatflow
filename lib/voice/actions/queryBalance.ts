import type { ExtractedEntities } from '../nlu/entityExtractor'
import type { VoiceFlatContext } from '../nlu/contextResolver'
import type { ActionResult } from './types'

export async function executeQueryBalance(
  entities: ExtractedEntities,
  context: VoiceFlatContext,
): Promise<ActionResult> {
  const { currentUser, members, balances } = context

  // Helper: get nickname from uid
  const nickname = (uid: string) =>
    members.find(m => m.uid === uid)?.nickname ?? uid

  // If a specific member was mentioned, show balance with that member only
  if (entities.memberUid) {
    const amt = balances[entities.memberUid] ?? 0
    const name = entities.member ?? nickname(entities.memberUid)

    if (Math.abs(amt) < 0.5) {
      return {
        success: true,
        action: 'QUERY_BALANCE',
        message: `You and ${name} are settled up. `,
        data: { memberId: entities.memberUid, amount: 0 },
      }
    }

    const sym = '₹'
    if (amt > 0) {
      return {
        success: true,
        action: 'QUERY_BALANCE',
        message: `${name} owes you ${sym}${Math.round(amt)}.`,
        data: { memberId: entities.memberUid, amount: amt, direction: 'owed_to_you' },
      }
    } else {
      return {
        success: true,
        action: 'QUERY_BALANCE',
        message: `You owe ${name} ${sym}${Math.round(Math.abs(amt))}.`,
        data: { memberId: entities.memberUid, amount: amt, direction: 'you_owe' },
      }
    }
  }

  // Show all non-zero balances
  const nonZero = Object.entries(balances).filter(([, amt]) => Math.abs(amt) >= 0.5)

  if (nonZero.length === 0) {
    return {
      success: true,
      action: 'QUERY_BALANCE',
      message: "All settled up! No outstanding balances.",
      data: { balances: {} },
    }
  }

  const sym = '₹'
  const lines: string[] = []
  let totalOwedToYou = 0
  let totalYouOwe    = 0

  for (const [uid, amt] of nonZero) {
    const name = nickname(uid)
    if (amt > 0) {
      lines.push(`${name} owes you ${sym}${Math.round(amt)}`)
      totalOwedToYou += amt
    } else {
      lines.push(`You owe ${name} ${sym}${Math.round(Math.abs(amt))}`)
      totalYouOwe += Math.abs(amt)
    }
  }

  // Build summary suffix
  const net = Math.round(totalOwedToYou - totalYouOwe)
  const netLine = net > 0
    ? ` Net: you're up ${sym}${net}.`
    : net < 0
      ? ` Net: you're down ${sym}${Math.abs(net)}.`
      : ' Net: break even.'

  return {
    success: true,
    action: 'QUERY_BALANCE',
    message: lines.join('. ') + '.' + netLine,
    data: {
      balances,
      totalOwedToYou: Math.round(totalOwedToYou),
      totalYouOwe:    Math.round(totalYouOwe),
      net,
    },
  }
}
