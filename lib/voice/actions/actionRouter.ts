import type { IntentType } from '../nlu/intentClassifier'
import type { ExtractedEntities } from '../nlu/entityExtractor'
import type { VoiceFlatContext } from '../nlu/contextResolver'
import type { ActionResult, VoiceStoreActions } from './types'

import { executeCompleteTask } from './completeTask'
import { executeCreateExpense } from './createExpense'
import { executeQueryBalance } from './queryBalance'
import { executeQueryTasks } from './queryTasks'
import { executeQueryStatus } from './queryStatus'
import { executeRequestSwap } from './requestSwap'
import { executeCreateTask } from './createTask'
import { executeGreeting } from './greeting'
import { executeUnknown } from './unknown'

/**
 * Routes a classified intent to the appropriate action executor.
 *
 * @param intent     — classified intent type
 * @param entities   — extracted entities from the transcript
 * @param context    — pre-built flat context (members, tasks, balances)
 * @param store      — minimal Zustand store methods needed for writes
 * @param transcript — original transcript (for error messages)
 */
export async function routeVoiceAction(
  intent: IntentType,
  entities: ExtractedEntities,
  context: VoiceFlatContext,
  store: VoiceStoreActions,
  transcript?: string,
): Promise<ActionResult> {
  switch (intent) {
    case 'COMPLETE_TASK':
      return executeCompleteTask(entities, context, store)

    case 'CREATE_EXPENSE':
      return executeCreateExpense(entities, context, store)

    case 'QUERY_BALANCE':
      return executeQueryBalance(entities, context)

    case 'QUERY_TASKS':
      return executeQueryTasks(entities, context)

    case 'QUERY_STATUS':
      return executeQueryStatus(entities, context)

    case 'REQUEST_SWAP':
      return executeRequestSwap(entities, context, store)

    case 'CREATE_TASK':
      return executeCreateTask(entities, context, store)

    case 'GREETING':
      return executeGreeting(context)

    case 'UNKNOWN':
    default:
      return executeUnknown(transcript)
  }
}
