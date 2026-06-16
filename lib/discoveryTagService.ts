import { db, hasKeys } from '@/lib/firebase'
import { collection, getDocs, setDoc, doc } from 'firebase/firestore'
import type { DiscoveryTag } from './discoveryTypes'

export function slugifyTag(label: string): string {
  return label.toLowerCase().trim().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '')
}

export async function fetchDiscoveryTags(): Promise<DiscoveryTag[]> {
  if (!hasKeys || !db) return []
  try {
    const snap = await getDocs(collection(db, 'discoveryTags'))
    return snap.docs.map(d => d.data() as DiscoveryTag)
  } catch {
    return []
  }
}

// Creates a tag globally if it doesn't already exist (merge: true = safe to call repeatedly)
export async function ensureDiscoveryTag(label: string, uid: string): Promise<DiscoveryTag> {
  const id = slugifyTag(label)
  const tag: DiscoveryTag = {
    id,
    label: label.trim(),
    createdBy: uid,
    createdAt: new Date().toISOString(),
  }
  if (hasKeys && db) {
    await setDoc(doc(db, 'discoveryTags', id), tag, { merge: true })
  }
  return tag
}
