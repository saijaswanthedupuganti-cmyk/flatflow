import { addDoc, collection } from 'firebase/firestore'
import { db, hasKeys } from '@/lib/firebase'

export type BehavioralEvent = {
  flatId: string
  uid: string
  type: 'task_completed' | 'settlement_completed'
  taskId?: string
  taskName?: string
  onTime?: boolean
  daysLate?: number
  settlementAmount?: number
  currency?: string
  monthCycleId?: string
  daysAfterClose?: number
}

/** Trust signal for discovery. Failures are caught by the caller and never block the action. */
export async function addBehavioralEvent(flatId: string, event: BehavioralEvent) {
  if (!hasKeys || !db) return
  await addDoc(collection(db, `flats/${flatId}/behavioralEvents`), {
    ...event,
    createdAt: new Date().toISOString(),
  })
}
