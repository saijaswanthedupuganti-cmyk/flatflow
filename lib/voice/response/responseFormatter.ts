import type { ActionResult } from '../actions/types'

export interface VoiceResponse {
  text: string
  speak: boolean
  card: ResponseCard
}

export interface ResponseCard {
  type: 'success' | 'error' | 'info'
  action: string
  title: string
  subtitle?: string
  canUndo: boolean
  undoData?: unknown
}

const ACTION_ICONS: Record<string, string> = {
  COMPLETE_TASK:  '✓',
  CREATE_EXPENSE: '₹',
  QUERY_BALANCE:  '⚖',
  QUERY_TASKS:    '📋',
  QUERY_STATUS:   '👥',
  REQUEST_SWAP:   '↔',
  CREATE_TASK:    '+',
  GREETING:       '👋',
  UNKNOWN:        '?',
}

export function formatResponse(result: ActionResult): VoiceResponse {
  const cardType: ResponseCard['type'] =
    result.success ? 'success'
    : result.error === 'unknown_intent' ? 'info'
    : 'error'

  const canUndo =
    result.success &&
    result.action === 'CREATE_EXPENSE' &&
    typeof result.data === 'object' &&
    result.data !== null &&
    (result.data as Record<string, unknown>).canUndo === true

  // Build subtitle: prefer followUp, otherwise derive from data
  let subtitle = result.followUp
  if (!subtitle && result.error === 'task_not_found') subtitle = 'Try saying the full task name'
  if (!subtitle && result.error === 'no_available_member') subtitle = 'Everyone is out of station'
  if (!subtitle && !result.success && result.action === 'CREATE_EXPENSE') subtitle = 'Say: "I spent 500 on groceries"'

  return {
    text: result.message,
    speak: true, // always speak — user must hear a response, not just see a card
    card: {
      type: cardType,
      action: result.action,
      title: result.message,
      subtitle,
      canUndo,
      undoData: canUndo ? result.data : undefined,
    },
  }
}

export function getActionIcon(action: string): string {
  return ACTION_ICONS[action] ?? '✦'
}
