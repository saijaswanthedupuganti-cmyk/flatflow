import type { ExtractedEntities } from '../nlu/entityExtractor'
import type { VoiceFlatContext } from '../nlu/contextResolver'
import type { IntentType } from '../nlu/intentClassifier'
import type { Expense, Task } from '@/store/useFlatStore'

export type { ExtractedEntities, VoiceFlatContext, IntentType }

export interface ActionResult {
  success: boolean
  action: string
  message: string
  data?: unknown
  followUp?: string
  error?: string
}

export interface VoiceAction {
  type: IntentType
  entities: ExtractedEntities
  confidence: number
  originalTranscript: string
}

// Minimal Zustand store interface — only what voice actions need
export interface VoiceStoreActions {
  markTaskCompleted: (taskId: string, userId: string) => Promise<void>
  addExpense: (data: Omit<Expense, 'id' | 'createdAt'>) => Promise<void>
  createSwapRequest: (taskId: string, fromUserId: string, toUserId: string, isAutomatic?: boolean) => Promise<void>
  createTask: (taskData: Omit<Task, 'taskId' | 'status' | 'lastCompletedAt'>, adminId: string) => Promise<void>
}
