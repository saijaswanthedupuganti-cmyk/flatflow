import { contextCache } from '../nlu/contextResolver'
import type { ExtractedEntities } from '../nlu/entityExtractor'
import type { VoiceFlatContext } from '../nlu/contextResolver'
import type { ActionResult, VoiceStoreActions } from './types'

export async function executeCompleteTask(
  entities: ExtractedEntities,
  context: VoiceFlatContext,
  store: Pick<VoiceStoreActions, 'markTaskCompleted'>,
): Promise<ActionResult> {
  const { currentUser, tasks } = context

  // Resolve target task
  let taskId = entities.taskId
  let taskName = entities.task

  if (!taskId) {
    // No task matched from transcript — find current user's pending tasks
    const myTasks = tasks.filter(
      t => t.currentAssignedUserId === currentUser.uid &&
           (t.status === 'pending' || t.status === 'overdue'),
    )

    if (myTasks.length === 0) {
      return {
        success: false,
        action: 'COMPLETE_TASK',
        message: "You have no pending tasks right now.",
      }
    }

    if (myTasks.length === 1) {
      taskId   = myTasks[0].id
      taskName = myTasks[0].name
    } else {
      // Disambiguation needed — list options
      const names = myTasks.map(t => t.name).join(', ')
      return {
        success: false,
        action: 'COMPLETE_TASK',
        message: `You have ${myTasks.length} tasks pending: ${names}. Which one did you complete?`,
        followUp: 'Which task?',
      }
    }
  }

  const task = tasks.find(t => t.id === taskId)
  if (!task) {
    return {
      success: false,
      action: 'COMPLETE_TASK',
      message: `Couldn't find task "${taskName ?? taskId}". Try saying the full task name.`,
      error: 'task_not_found',
    }
  }

  await store.markTaskCompleted(task.id, currentUser.uid)
  contextCache.invalidate(context.flatId)

  return {
    success: true,
    action: 'COMPLETE_TASK',
    message: `${task.name} — done! ✓`,
    data: {
      taskId:      task.id,
      taskName:    task.name,
      completedBy: currentUser.nickname,
    },
  }
}
