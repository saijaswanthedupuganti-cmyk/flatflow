import type { ExtractedEntities } from '../nlu/entityExtractor'
import type { VoiceFlatContext } from '../nlu/contextResolver'
import type { ActionResult, VoiceStoreActions } from './types'

export async function executeRequestSwap(
  entities: ExtractedEntities,
  context: VoiceFlatContext,
  store: Pick<VoiceStoreActions, 'createSwapRequest'>,
): Promise<ActionResult> {
  const { currentUser, tasks, members } = context

  // Resolve target task
  let taskId   = entities.taskId
  let taskName = entities.task

  if (!taskId) {
    // Find current user's most urgent pending/overdue task
    const myTasks = tasks.filter(
      t => t.currentAssignedUserId === currentUser.uid &&
           (t.status === 'pending' || t.status === 'overdue'),
    )

    if (myTasks.length === 0) {
      return {
        success: false,
        action: 'REQUEST_SWAP',
        message: "You have no pending tasks to swap.",
      }
    }

    // Sort: overdue first, then earliest due date
    myTasks.sort((a, b) => {
      if (a.status === 'overdue' && b.status !== 'overdue') return -1
      if (b.status === 'overdue' && a.status !== 'overdue') return 1
      return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime()
    })

    taskId   = myTasks[0].id
    taskName = myTasks[0].name
  }

  const task = tasks.find(t => t.id === taskId)
  if (!task) {
    return {
      success: false,
      action: 'REQUEST_SWAP',
      message: `Couldn't find task "${taskName ?? taskId}".`,
      error: 'task_not_found',
    }
  }

  // Find a swap target — first available (non-OOS) member who isn't the current user
  // If user specified a member, prefer them
  let targetMember = entities.memberUid
    ? members.find(m => m.uid === entities.memberUid && !m.isOOS && m.uid !== currentUser.uid)
    : null

  if (!targetMember) {
    targetMember = members.find(m => m.uid !== currentUser.uid && !m.isOOS) ?? null
  }

  if (!targetMember) {
    return {
      success: false,
      action: 'REQUEST_SWAP',
      message: "Everyone else is out of station. No one to swap with right now.",
      error: 'no_available_member',
    }
  }

  await store.createSwapRequest(task.id, currentUser.uid, targetMember.uid, true)

  const targetName = entities.member ?? targetMember.nickname

  return {
    success: true,
    action: 'REQUEST_SWAP',
    message: `Swap request sent to ${targetName} for ${task.name}.`,
    data: {
      taskId:     task.id,
      taskName:   task.name,
      toUserId:   targetMember.uid,
      toNickname: targetName,
    },
  }
}
