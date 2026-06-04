import { Member, Task } from '../store/useFlatStore'

/**
 * Calculates the next assigned user for a task, considering their availability.
 * If everyone is out of station, returns null indicating the queue should pause.
 */
export function getNextAssignee(task: Task, members: Member[]): string | null {
  const currentAssigneeId = task.currentAssignedUserId
  const queue = task.queueOrder

  if (queue.length === 0) return currentAssigneeId

  const currentIndex = queue.indexOf(currentAssigneeId)
  if (currentIndex === -1) return queue[0]

  let nextIndex = currentIndex
  let attempts = 0

  while (attempts < queue.length) {
    nextIndex = (nextIndex + 1) % queue.length
    const nextUserId = queue[nextIndex]
    const member = members.find(m => m.uid === nextUserId)

    // Check if the next person is available or busy (we only skip out_of_station or inactive)
    if (member && (member.status === 'available' || member.status === 'busy')) {
      return nextUserId
    }

    attempts++
  }

  // Everyone is out of station or inactive
  return null
}

/**
 * Processes a task completion event.
 *
 * Key rule: the next due date is calculated from the completion date (not from
 * the original due date), so overdue days are absorbed by the person who was
 * responsible — the next person always gets a FULL fresh cycle.
 *
 * @param completionDate - Optional. Defaults to now. Pass a past date when the
 *   user marks a task done retroactively (e.g. "we did this on Saturday").
 */
export function completeTask(task: Task, members: Member[], completionDate?: Date): Task {
  const nextAssignee = getNextAssignee(task, members)
  // Use the provided date or fall back to now
  const completedAt = completionDate && completionDate <= new Date() ? completionDate : new Date()

  // Next due date starts from completedAt, not from the original due date.
  // One-time tasks are marked done permanently — no rotation, no new due date
  if (task.frequency === 'one_time') {
    return {
      ...task,
      status: 'completed',
      lastCompletedAt: completedAt.toISOString(),
    }
  }

  const nextDueDate = new Date(completedAt)
  if (task.frequency === 'daily') nextDueDate.setDate(nextDueDate.getDate() + 1)
  else if (task.frequency === 'weekly') nextDueDate.setDate(nextDueDate.getDate() + 7)
  else if ((task.frequency as string) === 'fortnightly') nextDueDate.setDate(nextDueDate.getDate() + 14)
  else if (task.frequency === 'monthly') nextDueDate.setDate(nextDueDate.getDate() + 30)
  else nextDueDate.setDate(nextDueDate.getDate() + 7) // fallback for 'custom'

  if (!nextAssignee) {
    return {
      ...task,
      status: 'paused',
      lastCompletedAt: completedAt.toISOString(),
    }
  }

  return {
    ...task,
    status: 'pending',
    currentAssignedUserId: nextAssignee,
    lastCompletedAt: completedAt.toISOString(),
    dueDate: nextDueDate.toISOString(),
  }
}

/** Priority sorting weight */
export function getPriorityWeight(priority: Task['priority']): number {
  switch (priority) {
    case 'high': return 3
    case 'medium': return 2
    case 'low': return 1
    default: return 0
  }
}

/**
 * Calculates how urgent a task is based on its due date.
 */
export function getTaskUrgency(task: Task): 'normal' | 'warning' | 'overdue' {
  if (task.status === 'overdue') return 'overdue'
  if (task.status === 'completed' || task.status === 'paused') return 'normal'

  const now = new Date()
  const dueDate = new Date(task.dueDate)
  const diffHours = (dueDate.getTime() - now.getTime()) / (1000 * 60 * 60)

  if (diffHours < 0) return 'overdue'

  if (task.frequency === 'one_time') {
    if (diffHours <= 24) return 'warning'
  } else if (task.frequency === 'daily') {
    if (diffHours <= 8) return 'warning'
  } else if (task.frequency === 'weekly' || (task.frequency as string) === 'fortnightly') {
    if (diffHours <= 48) return 'warning'
  } else {
    if (diffHours <= 24) return 'warning'
  }

  return 'normal'
}

/** Format a date as "Mon, Jun 2" */
export function formatDate(date: Date): string {
  return date.toLocaleDateString('en-GB', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
  })
}

/** Format a date as "Mon, Jun 2 at 3:45 PM" */
export function formatDateTime(date: Date): string {
  return date.toLocaleDateString('en-GB', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
  })
}

/** How long ago was a date, as a friendly string */
export function timeAgo(dateStr: string | Date): string {
  const date = typeof dateStr === 'string' ? new Date(dateStr) : dateStr
  const seconds = Math.floor((new Date().getTime() - date.getTime()) / 1000)
  if (seconds < 60) return 'just now'
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m ago`
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}h ago`
  if (seconds < 86400 * 2) return 'yesterday'
  return `${Math.floor(seconds / 86400)}d ago`
}

/**
 * Full date info for a task — used in task cards to show real calendar dates.
 */
export interface TaskDateInfo {
  /** Cycle day label: "Day 5 of 7" */
  cycleLabel: string
  /** Actual due date: "Thu, Jun 5" */
  dueDateFormatted: string
  /** Last completed date+time: "Mon, May 26 at 2:30 PM" */
  lastCompletedFormatted: string
  /** Days overdue (0 if not overdue) */
  overdueDays: number
  /** "2 days overdue — still your responsibility" */
  overdueLabel: string
  /** Original due date was: "Thu, May 29" */
  originalDueFormatted: string
  isOverdue: boolean
}

export function getTaskDateInfo(task: Task): TaskDateInfo {
  const now = new Date()
  const dueDate = new Date(task.dueDate)
  const lastCompleted = new Date(task.lastCompletedAt)

  // One-time tasks: no cycle, show due date only
  if (task.frequency === 'one_time') {
    const dueDate = new Date(task.dueDate)
    const diffMs = dueDate.getTime() - now.getTime()
    const isOverdue = diffMs < 0 || task.status === 'overdue'
    const overdueDays = isOverdue ? Math.ceil(Math.abs(diffMs) / 86400000) : 0
    return {
      cycleLabel: task.status === 'completed' ? 'Done' : 'One-time',
      dueDateFormatted: formatDate(dueDate),
      lastCompletedFormatted: formatDateTime(new Date(task.lastCompletedAt)),
      overdueDays,
      overdueLabel: isOverdue && task.status !== 'completed'
        ? `${overdueDays} day${overdueDays === 1 ? '' : 's'} overdue`
        : '',
      originalDueFormatted: formatDate(dueDate),
      isOverdue: isOverdue && task.status !== 'completed',
    }
  }

  const cycleLengthMs =
    task.frequency === 'daily' ? 86400000 :
    task.frequency === 'weekly' ? 604800000 :
    (task.frequency as string) === 'fortnightly' ? 1209600000 :
    task.frequency === 'monthly' ? 2592000000 : 604800000
  const cycleDays = Math.round(cycleLengthMs / 86400000)

  const elapsedMs = now.getTime() - lastCompleted.getTime()
  const elapsedDays = Math.floor(elapsedMs / 86400000)

  const diffMs = dueDate.getTime() - now.getTime()
  const isOverdue = diffMs < 0 || task.status === 'overdue'
  const overdueDays = isOverdue ? Math.ceil(Math.abs(diffMs) / 86400000) : 0

  const cycleDay = Math.min(elapsedDays + 1, cycleDays + overdueDays)
  const cycleLabel = isOverdue
    ? `Day ${cycleDay} of ${cycleDays} (${overdueDays}d overdue)`
    : `Day ${Math.min(elapsedDays + 1, cycleDays)} of ${cycleDays}`

  const overdueLabel = isOverdue
    ? `${overdueDays} day${overdueDays === 1 ? '' : 's'} overdue — still your responsibility`
    : ''

  return {
    cycleLabel,
    dueDateFormatted: formatDate(dueDate),
    lastCompletedFormatted: formatDateTime(lastCompleted),
    overdueDays,
    overdueLabel,
    originalDueFormatted: formatDate(dueDate),
    isOverdue,
  }
}

/**
 * Legacy helper — kept for backward compat in analytics/tasks pages.
 */
export function getTimeCycleContext(task: Task): string {
  if (task.status === 'completed') return 'Completed'
  if (task.status === 'paused') return 'Paused'
  const info = getTaskDateInfo(task)
  return info.isOverdue ? `OVERDUE by ${info.overdueDays}d` : info.cycleLabel
}
