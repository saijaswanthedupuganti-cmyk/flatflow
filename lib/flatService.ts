/**
 * flatService.ts
 * Handles flat creation and joining logic (Firebase + mock mode).
 */
import { db, hasKeys } from '@/lib/firebase'
import {
  doc, getDoc, setDoc, collection, getDocs, serverTimestamp
} from 'firebase/firestore'

export interface FlatProfile {
  flatId: string
  role: 'admin' | 'member'
}

/** Generate a human-readable flat ID like "FLAT-A3B9" */
export function generateFlatId(): string {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  let code = 'FLAT-'
  for (let i = 0; i < 4; i++) {
    code += chars[Math.floor(Math.random() * chars.length)]
  }
  return code
}

/** Read user's flat profile from Firestore. Returns null if not found. */
export async function getUserFlatProfile(uid: string): Promise<FlatProfile | null> {
  if (!hasKeys || !db) return null
  try {
    const snap = await getDoc(doc(db, `users/${uid}`))
    if (snap.exists()) {
      const data = snap.data()
      if (data.flatId) return { flatId: data.flatId, role: data.role || 'member' }
    }
  } catch (e) {
    console.error('getUserFlatProfile error:', e)
  }
  return null
}

/** Check if a flat ID exists in Firestore. */
export async function flatExists(flatId: string): Promise<boolean> {
  if (!hasKeys || !db) return false
  try {
    const snap = await getDoc(doc(db, `flats/${flatId}`))
    return snap.exists()
  } catch (e) {
    console.error('flatExists error:', e)
    return false
  }
}

/**
 * Create a brand-new flat. The caller becomes the admin.
 * Returns the new flatId.
 */
export async function createFlat(params: {
  uid: string
  nickname: string
  email: string
  flatName: string
}): Promise<string> {
  const { uid, nickname, email, flatName } = params

  if (!hasKeys || !db) {
    // Mock mode — use the pre-seeded flat ID so mock data loads correctly
    return 'FLAT-1234'
  }

  let flatId = generateFlatId()

  // Ensure uniqueness (retry up to 5 times)
  for (let i = 0; i < 5; i++) {
    if (!(await flatExists(flatId))) break
    flatId = generateFlatId()
  }

  // Write flat document
  await setDoc(doc(db, `flats/${flatId}`), {
    name: flatName,
    adminUid: uid,
    createdAt: serverTimestamp(),
  })

  // Write admin member document
  const memberData = {
    uid,
    nickname,
    email,
    role: 'admin',
    status: 'available',
    reliabilityScore: 100,
    joinedAt: new Date().toISOString(),
  }
  await setDoc(doc(db, `flats/${flatId}/members/${uid}`), memberData)

  // Write user profile linking to flat
  await setDoc(doc(db, `users/${uid}`), {
    flatId,
    role: 'admin',
    nickname,
    email,
  }, { merge: true })

  return flatId
}

/**
 * Join an existing flat with an invite code.
 * Returns true on success, false if flat not found.
 */
export async function joinFlat(params: {
  uid: string
  nickname: string
  email: string
  flatId: string
}): Promise<{ success: boolean; error?: string }> {
  const { uid, nickname, email, flatId } = params

  if (!hasKeys || !db) {
    // Mock mode — accept any code, treat as joining FLAT-1234
    return { success: true }
  }

  if (!(await flatExists(flatId))) {
    return { success: false, error: 'Flat not found. Check the invite code and try again.' }
  }

  // Add member
  const memberData = {
    uid,
    nickname,
    email,
    role: 'member',
    status: 'available',
    reliabilityScore: 100,
    joinedAt: new Date().toISOString(),
  }
  await setDoc(doc(db, `flats/${flatId}/members/${uid}`), memberData)

  // Link user to flat
  await setDoc(doc(db, `users/${uid}`), {
    flatId,
    role: 'member',
    nickname,
    email,
  }, { merge: true })

  return { success: true }
}
