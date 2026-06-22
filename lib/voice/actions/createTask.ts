import type { ExtractedEntities } from '../nlu/entityExtractor'
import type { VoiceFlatContext } from '../nlu/contextResolver'
import type { ActionResult, VoiceStoreActions } from './types'
import type { Task } from '@/store/useFlatStore'

const FREQUENCY_DAYS: Record<string, number> = {
  daily: 1, weekly: 7, fortnightly: 14, monthly: 30,
}

export async function executeCreateTask(
  entities: ExtractedEntities,
  context: VoiceFlatContext,
  store: Pick<VoiceStoreActions, 'createTask'>,
): Promise<ActionResult> {
  const { currentUser, members } = context

  // Extract task name from description (cleaned transcript remnant) or task entity
  const rawName = extractTaskName(entities)

  if (!rawName || rawName.length < 2) {
    return {
      success: false,
      action: 'CREATE_TASK',
      message: "What should I name the task? Say something like 'add kitchen daily'.",
      followUp: 'Task name?',
    }
  }

  const frequency = entities.frequency ?? 'weekly'
  const days      = FREQUENCY_DAYS[frequency] ?? 7

  // Due date: X days from now
  const dueDate = new Date()
  dueDate.setDate(dueDate.getDate() + days)

  // All members in rotation queue
  const allUids = members.map(m => m.uid)

  const taskData: Omit<Task, 'taskId' | 'status' | 'lastCompletedAt'> = {
    name:                  rawName,
    type:                  'other',
    priority:              'medium',
    frequency:             frequency as Task['frequency'],
    currentAssignedUserId: currentUser.uid,
    queueOrder:            allUids,
    dueDate:               dueDate.toISOString(),
  }

  await store.createTask(taskData, currentUser.uid)

  const freqLabel: Record<string, string> = {
    daily: 'daily', weekly: 'weekly', fortnightly: 'fortnightly', monthly: 'monthly',
  }

  return {
    success: true,
    action: 'CREATE_TASK',
    message: `Created "${rawName}" — ${freqLabel[frequency] ?? frequency}, assigned to you.`,
    data: {
      taskName:  rawName,
      frequency,
      assignedTo: currentUser.nickname,
    },
  }
}

// Extract a clean task name from entities.
// Priority: description (stripped of frequency words) → task entity (if new)
function extractTaskName(entities: ExtractedEntities): string | undefined {
  const FREQ_WORDS = /\b(daily|weekly|monthly|fortnightly|every\s+day|every\s+week|every\s+month)\b/gi
  const CRUD_WORDS = /\b(add|create|new|make|set\s+up|schedule|task|duty|naya|banao|karo)\b/gi

  if (entities.description && entities.description.length > 1) {
    const cleaned = entities.description
      .replace(FREQ_WORDS, '')
      .replace(CRUD_WORDS, '')
      .trim()
      .replace(/\s+/g, ' ')
    if (cleaned.length >= 2) {
      return cleaned.charAt(0).toUpperCase() + cleaned.slice(1)
    }
  }

  // Fallback: use matched task name (may be an existing one being re-added)
  if (entities.task && entities.task.length >= 2) {
    return entities.task
  }

  return undefined
}
