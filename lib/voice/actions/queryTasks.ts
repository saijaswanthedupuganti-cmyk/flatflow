import type { ExtractedEntities } from '../nlu/entityExtractor'
import type { VoiceFlatContext } from '../nlu/contextResolver'
import type { ActionResult } from './types'

export async function executeQueryTasks(
  entities: ExtractedEntities,
  context: VoiceFlatContext,
): Promise<ActionResult> {
  const { currentUser, tasks } = context

  // Filter to tasks assigned to the current user
  const myTasks = tasks.filter(t => t.currentAssignedUserId === currentUser.uid)

  if (myTasks.length === 0) {
    return {
      success: true,
      action: 'QUERY_TASKS',
      message: "You have no tasks assigned right now. Enjoy the break!",
      data: { tasks: [] },
    }
  }

  // Sort: overdue first, then pending, then by due date
  const sorted = [...myTasks].sort((a, b) => {
    const weight = (status: string) =>
      status === 'overdue' ? 0 : status === 'pending' ? 1 : 2
    if (weight(a.status) !== weight(b.status)) return weight(a.status) - weight(b.status)
    return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime()
  })

  const overdue = sorted.filter(t => t.status === 'overdue')
  const pending = sorted.filter(t => t.status === 'pending')

  const lines: string[] = []

  if (overdue.length > 0) {
    const names = overdue.map(t => t.name).join(', ')
    lines.push(`${overdue.length} overdue: ${names}`)
  }

  if (pending.length > 0) {
    const names = pending.slice(0, 3).map(t => t.name).join(', ')
    const extra = pending.length > 3 ? ` and ${pending.length - 3} more` : ''
    lines.push(`${pending.length} pending: ${names}${extra}`)
  }

  const total = myTasks.length
  const summary = total === 1 ? '1 task' : `${total} tasks`
  const message = lines.join('. ') + '.'

  return {
    success: true,
    action: 'QUERY_TASKS',
    message,
    data: {
      tasks: sorted.map(t => ({
        id:        t.id,
        name:      t.name,
        status:    t.status,
        dueDate:   t.dueDate,
        frequency: t.frequency,
      })),
      totalCount:   total,
      overdueCount: overdue.length,
      pendingCount: pending.length,
    },
  }
}
