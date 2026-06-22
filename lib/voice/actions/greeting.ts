import type { VoiceFlatContext } from '../nlu/contextResolver'
import type { ActionResult } from './types'

const GREETINGS = [
  "Hey {name}!",
  "Hi {name}!",
  "Hello {name}!",
  "Hey there, {name}!",
]

export async function executeGreeting(
  context: VoiceFlatContext,
): Promise<ActionResult> {
  const { currentUser, tasks } = context

  // Count current user's pending tasks
  const myPending = tasks.filter(
    t => t.currentAssignedUserId === currentUser.uid &&
         (t.status === 'pending' || t.status === 'overdue'),
  )
  const overdueCount = myPending.filter(t => t.status === 'overdue').length

  // Rotate greeting based on uid hash to keep it varied
  const idx = currentUser.uid.charCodeAt(0) % GREETINGS.length
  const greeting = GREETINGS[idx].replace('{name}', currentUser.nickname)

  let taskLine: string
  if (myPending.length === 0) {
    taskLine = "All clear — no tasks pending."
  } else if (overdueCount > 0) {
    taskLine = `You have ${myPending.length} task${myPending.length !== 1 ? 's' : ''} (${overdueCount} overdue).`
  } else {
    taskLine = `You have ${myPending.length} task${myPending.length !== 1 ? 's' : ''} pending.`
  }

  return {
    success: true,
    action: 'GREETING',
    message: `${greeting} ${taskLine}`,
    data: {
      pendingCount: myPending.length,
      overdueCount,
    },
  }
}
