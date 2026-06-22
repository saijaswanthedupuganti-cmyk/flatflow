import type { ExtractedEntities } from '../nlu/entityExtractor'
import type { VoiceFlatContext } from '../nlu/contextResolver'
import type { ActionResult } from './types'

export async function executeQueryStatus(
  entities: ExtractedEntities,
  context: VoiceFlatContext,
): Promise<ActionResult> {
  const { members, currentUser } = context

  // If a specific member was mentioned, return their status
  if (entities.memberUid) {
    const member = members.find(m => m.uid === entities.memberUid)
    const name   = entities.member ?? member?.uid ?? 'that person'

    if (!member) {
      return {
        success: false,
        action: 'QUERY_STATUS',
        message: `Couldn't find member "${name}" in the flat.`,
        error: 'member_not_found',
      }
    }

    const statusText = member.isOOS ? 'out of station' : 'home'
    return {
      success: true,
      action: 'QUERY_STATUS',
      message: `${member.uid === currentUser.uid ? 'You are' : `${member.uid === currentUser.uid ? 'You' : name} is`} ${statusText}.`,
      data: { memberId: member.uid, nickname: member.uid, isOOS: member.isOOS },
    }
  }

  // Show flat-wide status
  const home = members.filter(m => !m.isOOS)
  const oos  = members.filter(m => m.isOOS)

  if (oos.length === 0) {
    const names = home.map(m => m.nickname).join(', ')
    return {
      success: true,
      action: 'QUERY_STATUS',
      message: `Everyone is home. ${names}.`,
      data: { home: home.map(m => m.uid), oos: [] },
    }
  }

  if (home.length === 0) {
    const names = oos.map(m => m.nickname).join(', ')
    return {
      success: true,
      action: 'QUERY_STATUS',
      message: `Everyone is out of station: ${names}.`,
      data: { home: [], oos: oos.map(m => m.uid) },
    }
  }

  const homeNames = home.map(m => m.nickname).join(', ')
  const oosNames  = oos.map(m => m.nickname).join(', ')

  return {
    success: true,
    action: 'QUERY_STATUS',
    message: `Home: ${homeNames}. Out of station: ${oosNames}.`,
    data: {
      home: home.map(m => m.uid),
      oos:  oos.map(m => m.uid),
    },
  }
}
