import { db, hasKeys } from '@/lib/firebase'
import { doc, setDoc, collection } from 'firebase/firestore'
import type { BehavioralEvent } from './discoveryTypes'

/**
 * Write one immutable behavioral event to Firestore.
 * No-ops silently in mock mode (no Firebase keys).
 * Always call inside try/catch — this is non-critical infrastructure
 * and must never block or break the main feature it's hooked into.
 */
export async function addBehavioralEvent(
  flatId: string,
  event: Omit<BehavioralEvent, 'id' | 'timestamp'>
): Promise<void> {
  if (!hasKeys || !db) return
  const id = crypto.randomUUID()
  const ref = doc(collection(db, `flats/${flatId}/behavioralEvents`), id)
  await setDoc(ref, { ...event, id, timestamp: new Date().toISOString() })
}
