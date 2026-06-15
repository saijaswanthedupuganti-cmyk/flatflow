/**
 * flatService.ts
 * Handles flat creation, joining, and membership management (Firebase + mock mode).
 */
import { db, hasKeys } from '@/lib/firebase'
import {
  doc, getDoc, setDoc, updateDoc, deleteDoc, getDocs,
  collection, serverTimestamp, arrayUnion, arrayRemove, writeBatch,
  runTransaction, increment,
} from 'firebase/firestore'

export interface FlatProfile {
  activeFlatId: string
  flatIds: string[]
}

export interface FlatInfo {
  id: string
  name: string
}

/** Generate a human-readable flat ID like "FLAT-A3B9" using cryptographically secure randomness */
export function generateFlatId(): string {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  const bytes = new Uint8Array(4)
  crypto.getRandomValues(bytes)
  let code = 'FLAT-'
  for (let i = 0; i < 4; i++) {
    code += chars[bytes[i] % chars.length]
  }
  return code
}

/** Read user's flat profile from Firestore. Handles both old (flatId) and new (activeFlatId + flatIds[]) schema. */
export async function getUserFlatProfile(uid: string): Promise<FlatProfile | null> {
  if (!hasKeys || !db) return null
  try {
    const snap = await getDoc(doc(db, `users/${uid}`))
    if (snap.exists()) {
      const data = snap.data()
      // New schema
      if (data.activeFlatId) {
        return {
          activeFlatId: data.activeFlatId,
          flatIds: Array.isArray(data.flatIds) ? data.flatIds : [data.activeFlatId],
        }
      }
      // Old schema — migrate on read
      if (data.flatId) {
        await updateDoc(doc(db, `users/${uid}`), {
          activeFlatId: data.flatId,
          flatIds: [data.flatId],
        })
        return { activeFlatId: data.flatId, flatIds: [data.flatId] }
      }
    }
  } catch (e) {
    console.error('getUserFlatProfile error:', e)
  }
  return null
}

/** Fetch flat name + ID for each flatId. Silently skips any that error. */
export async function getFlatsInfo(flatIds: string[]): Promise<FlatInfo[]> {
  if (!hasKeys || !db || flatIds.length === 0) return []
  const results: FlatInfo[] = []
  await Promise.all(
    flatIds.map(async (flatId) => {
      try {
        const snap = await getDoc(doc(db, `flats/${flatId}`))
        results.push({ id: flatId, name: snap.exists() ? (snap.data().name || flatId) : flatId })
      } catch {
        results.push({ id: flatId, name: flatId })
      }
    })
  )
  // Preserve original order
  return results.sort((a, b) => flatIds.indexOf(a.id) - flatIds.indexOf(b.id))
}

/** Check if a flat ID exists in Firestore. */
export async function flatExists(flatId: string): Promise<boolean> {
  if (!hasKeys || !db) return false
  try {
    const snap = await getDoc(doc(db, `flats/${flatId}`))
    return snap.exists()
  } catch {
    return false
  }
}

/** Verify whether a user is still a member of a flat (handles kicked users). */
export async function verifyMembership(uid: string, flatId: string): Promise<boolean> {
  if (!hasKeys || !db) return true
  try {
    const snap = await getDoc(doc(db, `flats/${flatId}/members/${uid}`))
    return snap.exists()
  } catch {
    return false // Permission denied = kicked
  }
}

/** Update the user's activeFlatId in Firestore. */
export async function setActiveFlatId(uid: string, flatId: string | null): Promise<void> {
  if (!hasKeys || !db) return
  await updateDoc(doc(db, `users/${uid}`), { activeFlatId: flatId })
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

  if (!hasKeys || !db) return 'FLAT-1234'

  let flatId = generateFlatId()
  for (let i = 0; i < 5; i++) {
    if (!(await flatExists(flatId))) break
    flatId = generateFlatId()
  }

  // Flat document — memberCount starts at 1 (the admin)
  const trialEndDate = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString()
  await setDoc(doc(db, `flats/${flatId}`), {
    name: flatName,
    adminUid: uid,
    createdAt: serverTimestamp(),
    memberCount: 1,
    subscriptionStatus: 'trial',
    trialEndDate,
  })

  // Admin member document
  await setDoc(doc(db, `flats/${flatId}/members/${uid}`), {
    uid,
    nickname,
    email,
    role: 'admin',
    status: 'available',
    reliabilityScore: 100,
    joinedAt: new Date().toISOString(),
  })

  // User profile — new multi-flat schema
  await setDoc(doc(db, `users/${uid}`), {
    activeFlatId: flatId,
    flatIds: arrayUnion(flatId),
    email,
  }, { merge: true })

  return flatId
}

/**
 * Join an existing flat with an invite code.
 * Uses a transaction so the member-count check, member creation, and
 * counter increment are all atomic — prevents race conditions around the 8-member cap.
 */
/** Returns the join mode for a flat ('auto' or 'approval'). */
export async function getFlatJoinMode(flatId: string): Promise<'auto' | 'approval'> {
  if (!hasKeys || !db) return 'auto'
  const snap = await getDoc(doc(db, `flats/${flatId}`))
  if (!snap.exists()) return 'auto'
  return (snap.data().joinMode as 'auto' | 'approval') ?? 'auto'
}

/** Set join mode on a flat (admin only). */
export async function setFlatJoinMode(flatId: string, mode: 'auto' | 'approval'): Promise<void> {
  if (!hasKeys || !db) return
  await updateDoc(doc(db, `flats/${flatId}`), { joinMode: mode })
}

/**
 * Submit a join request (used when the flat is in 'approval' mode).
 * Returns the request ID so the caller can track it.
 */
export async function requestToJoinFlat(params: {
  uid: string; nickname: string; email: string; flatId: string
}): Promise<{ success: boolean; error?: string; requestId?: string }> {
  const { uid, nickname, email, flatId } = params
  if (!hasKeys || !db) return { success: true, requestId: 'mock' }

  if (!(await flatExists(flatId))) {
    return { success: false, error: 'Flat not found. Check the invite code and try again.' }
  }

  // Check not already a member or pending
  const memberSnap = await getDoc(doc(db, `flats/${flatId}/members/${uid}`))
  if (memberSnap.exists()) return { success: false, error: 'You are already a member of this flat.' }

  const requestId = crypto.randomUUID()
  await setDoc(doc(db, `flats/${flatId}/joinRequests/${requestId}`), {
    id: requestId,
    uid, nickname, email,
    status: 'pending',
    createdAt: new Date().toISOString(),
  })
  return { success: true, requestId }
}

/** Admin approves a join request — calls joinFlat for the user. */
export async function approveJoinRequestService(
  flatId: string,
  requestId: string,
  uid: string,
  nickname: string,
  email: string
): Promise<{ success: boolean; error?: string }> {
  const result = await joinFlat({ uid, nickname, email, flatId })
  if (!result.success) return result
  if (hasKeys && db) {
    await updateDoc(doc(db, `flats/${flatId}/joinRequests/${requestId}`), { status: 'approved' })
  }
  return { success: true }
}

/** Admin rejects a join request. */
export async function rejectJoinRequestService(flatId: string, requestId: string): Promise<void> {
  if (!hasKeys || !db) return
  await updateDoc(doc(db, `flats/${flatId}/joinRequests/${requestId}`), { status: 'rejected' })
}

export async function joinFlat(params: {
  uid: string
  nickname: string
  email: string
  flatId: string
}): Promise<{ success: boolean; error?: string; flatName?: string }> {
  const { uid, nickname, email, flatId } = params

  if (!hasKeys || !db) return { success: true }

  // Quick existence check before opening a transaction (cheaper read)
  if (!(await flatExists(flatId))) {
    return { success: false, error: 'Flat not found. Check the invite code and try again.' }
  }

  const firestoreDb = db // capture for use inside the transaction callback
  let flatName: string | undefined

  try {
    await runTransaction(firestoreDb, async (transaction) => {
      const flatRef   = doc(firestoreDb, `flats/${flatId}`)
      const memberRef = doc(firestoreDb, `flats/${flatId}/members/${uid}`)
      const userRef   = doc(firestoreDb, `users/${uid}`)

      // All reads must happen before any writes in a Firestore transaction
      const [flatSnap, memberSnap] = await Promise.all([
        transaction.get(flatRef),
        transaction.get(memberRef),
      ])

      if (!flatSnap.exists()) throw new Error('FLAT_NOT_FOUND')
      flatName = flatSnap.data().name as string

      // Default to 0 for flats created before memberCount was introduced
      const memberCount: number = flatSnap.data().memberCount ?? 0
      if (memberCount >= 8) throw new Error('FLAT_FULL')

      if (memberSnap.exists()) throw new Error('ALREADY_MEMBER')

      // ── Writes ──────────────────────────────────────────────────────────────
      transaction.set(memberRef, {
        uid,
        nickname,
        email,
        role: 'member',
        status: 'available',
        reliabilityScore: 100,
        joinedAt: new Date().toISOString(),
      })

      // Atomically increment the counter on the flat document
      transaction.update(flatRef, { memberCount: increment(1) })

      // User profile — multi-flat schema
      transaction.set(userRef, {
        activeFlatId: flatId,
        flatIds: arrayUnion(flatId),
        email,
      }, { merge: true })
    })
  } catch (e) {
    const msg = (e as Error).message
    if (msg === 'FLAT_NOT_FOUND') return { success: false, error: 'Flat not found. Check the invite code and try again.' }
    if (msg === 'FLAT_FULL')      return { success: false, error: 'This flat is full (maximum 8 members).' }
    if (msg === 'ALREADY_MEMBER') return { success: false, error: 'You are already a member of this flat.' }
    throw e // unexpected error — let the caller handle it
  }

  // Append the new member to the end of every existing task's rotation queue
  await addMemberToTaskQueues(flatId, uid)

  // Activity log — record the join event
  const logId = crypto.randomUUID()
  await setDoc(doc(db, `flats/${flatId}/activityLog/${logId}`), {
    id: logId,
    userId: uid,
    action: 'status_change',
    details: `${nickname} joined the flat and was added to all task rotations`,
    timestamp: new Date().toISOString(),
  })

  return { success: true, flatName }
}

/** Update the display name of a flat (admin only). The flat ID never changes. */
export async function updateFlatName(flatId: string, newName: string): Promise<void> {
  if (!hasKeys || !db) return
  await updateDoc(doc(db, `flats/${flatId}`), { name: newName })
}

// ── Membership Management ────────────────────────────────────────────────────

/**
 * Append a newly joined member to the end of every task's rotation queue.
 * Called right after joinFlat() succeeds so the new person is immediately
 * part of the circle for all existing tasks.
 */
async function addMemberToTaskQueues(flatId: string, newUid: string): Promise<void> {
  if (!hasKeys || !db) return
  const taskSnaps = await getDocs(collection(db, `flats/${flatId}/tasks`))
  if (taskSnaps.empty) return
  const batch = writeBatch(db)
  taskSnaps.forEach((taskDoc) => {
    const queue: string[] = taskDoc.data().queueOrder || []
    // Guard: skip if already in queue (shouldn't happen, but safe)
    if (!queue.includes(newUid)) {
      batch.update(taskDoc.ref, { queueOrder: [...queue, newUid] })
    }
  })
  await batch.commit()
}

/**
 * Reassign all tasks owned by a leaving/kicked member to the next person in queue.
 * Also removes them from all task queueOrders.
 */
async function reassignMemberTasks(flatId: string, leavingUid: string): Promise<void> {
  if (!hasKeys || !db) return
  const taskSnaps = await getDocs(collection(db, `flats/${flatId}/tasks`))
  if (taskSnaps.empty) return
  const batch = writeBatch(db)
  taskSnaps.forEach((taskDoc) => {
    const task = taskDoc.data()
    const oldQueue: string[] = task.queueOrder || []
    const leavingIndex = oldQueue.indexOf(leavingUid)
    const newQueue = oldQueue.filter((uid: string) => uid !== leavingUid)
    const update: Record<string, unknown> = { queueOrder: newQueue }
    if (task.currentAssignedUserId === leavingUid) {
      // Assign to the person who was NEXT after the leaving member in the original
      // queue. After filtering, that person now sits at `leavingIndex` (or wraps
      // to 0 if the leaving member was last in the queue).
      update.currentAssignedUserId = newQueue.length > 0
        ? newQueue[leavingIndex % newQueue.length]
        : ''
    }
    batch.update(taskDoc.ref, update)
  })
  await batch.commit()
}

/** Cancel pending swap requests that involve a leaving/kicked member. */
async function cancelMemberSwapRequests(flatId: string, leavingUid: string): Promise<void> {
  if (!hasKeys || !db) return
  const swapSnaps = await getDocs(collection(db, `flats/${flatId}/swapRequests`))
  if (swapSnaps.empty) return
  const batch = writeBatch(db)
  swapSnaps.forEach((swapDoc) => {
    const swap = swapDoc.data()
    if (swap.status === 'pending' && (swap.fromUserId === leavingUid || swap.toUserId === leavingUid)) {
      batch.update(swapDoc.ref, { status: 'rejected' })
    }
  })
  await batch.commit()
}

/**
 * Member voluntarily leaves a flat.
 * Returns the nextFlatId to switch to (or null if no flats remain).
 */
export async function leaveFlatService(uid: string, flatId: string): Promise<{ nextFlatId: string | null }> {
  if (!hasKeys || !db) return { nextFlatId: null }

  const userSnap = await getDoc(doc(db, `users/${uid}`))
  const userData = userSnap.exists() ? userSnap.data() : {}
  const currentFlatIds: string[] = Array.isArray(userData.flatIds) ? userData.flatIds : [flatId]
  const remainingFlatIds = currentFlatIds.filter(id => id !== flatId)
  const nextFlatId = remainingFlatIds.length > 0 ? remainingFlatIds[0] : null

  const memberSnap = await getDoc(doc(db, `flats/${flatId}/members/${uid}`))
  const memberName = memberSnap.exists() ? memberSnap.data().nickname : 'A member'

  await reassignMemberTasks(flatId, uid)
  await cancelMemberSwapRequests(flatId, uid)

  // Activity log entry
  const logId = crypto.randomUUID()
  await setDoc(doc(db, `flats/${flatId}/activityLog/${logId}`), {
    id: logId,
    userId: 'system',
    action: 'status_change',
    details: `${memberName} left the flat`,
    timestamp: new Date().toISOString(),
  })

  // Decrement the flat's member counter FIRST (while isMember() is still true),
  // then remove the member doc. Order matters for Firestore security rules.
  await updateDoc(doc(db, `flats/${flatId}`), { memberCount: increment(-1) })
  await deleteDoc(doc(db, `flats/${flatId}/members/${uid}`))

  // Update user profile
  await updateDoc(doc(db, `users/${uid}`), {
    flatIds: arrayRemove(flatId),
    activeFlatId: nextFlatId,
  })

  return { nextFlatId }
}

/**
 * Admin removes a member from the flat (kick).
 * The kicked user will be redirected to onboarding next time they open the app.
 */
export async function kickMemberService(adminUid: string, targetUid: string, flatId: string): Promise<void> {
  if (!hasKeys || !db) return

  const memberSnap = await getDoc(doc(db, `flats/${flatId}/members/${targetUid}`))
  const memberName = memberSnap.exists() ? memberSnap.data().nickname : 'A member'

  const adminSnap = await getDoc(doc(db, `flats/${flatId}/members/${adminUid}`))
  const adminName = adminSnap.exists() ? adminSnap.data().nickname : 'Admin'

  await reassignMemberTasks(flatId, targetUid)
  await cancelMemberSwapRequests(flatId, targetUid)

  // Activity log
  const logId = crypto.randomUUID()
  await setDoc(doc(db, `flats/${flatId}/activityLog/${logId}`), {
    id: logId,
    userId: adminUid,
    action: 'status_change',
    details: `${adminName} removed ${memberName} from the flat`,
    timestamp: new Date().toISOString(),
  })

  // Delete member doc and decrement counter atomically — if either fails, neither
  // commits, so memberCount never gets stuck at a value that prevents future joins.
  const kickBatch = writeBatch(db)
  kickBatch.delete(doc(db, `flats/${flatId}/members/${targetUid}`))
  kickBatch.update(doc(db, `flats/${flatId}`), { memberCount: increment(-1) })
  await kickBatch.commit()

  // Best-effort: update kicked user's profile (may fail due to Firestore auth rules)
  try {
    await updateDoc(doc(db, `users/${targetUid}`), {
      flatIds: arrayRemove(flatId),
    })
  } catch {
    // Expected: admin can't write another user's doc. The user will be
    // auto-corrected next time they open the app (membership verification in initAuthListener).
  }
}

/**
 * Transfer the admin role to another member.
 * Call this before the current admin leaves the flat.
 */
export async function transferAdminService(currentAdminUid: string, newAdminUid: string, flatId: string): Promise<void> {
  if (!hasKeys || !db) return
  const batch = writeBatch(db)
  batch.update(doc(db, `flats/${flatId}/members/${currentAdminUid}`), { role: 'member' })
  batch.update(doc(db, `flats/${flatId}/members/${newAdminUid}`), { role: 'admin' })
  batch.update(doc(db, `flats/${flatId}`), { adminUid: newAdminUid })
  await batch.commit()
}

/**
 * Delete an entire flat and all its subcollections.
 * Only called when the admin is the last remaining member.
 */
export async function deleteEntireFlatService(adminUid: string, flatId: string): Promise<void> {
  if (!hasKeys || !db) return

  const subcollections = ['members', 'tasks', 'activityLog', 'swapRequests', 'joinRequests', 'npsResponses', 'expenses', 'settlements', 'recurringBills', 'billInstances', 'monthCycles']
  for (const sub of subcollections) {
    const snaps = await getDocs(collection(db, `flats/${flatId}/${sub}`))
    if (!snaps.empty) {
      const batch = writeBatch(db)
      snaps.forEach(d => batch.delete(d.ref))
      await batch.commit()
    }
  }

  await deleteDoc(doc(db, `flats/${flatId}`))

  // Update admin's user profile
  await updateDoc(doc(db, `users/${adminUid}`), {
    flatIds: arrayRemove(flatId),
    activeFlatId: null,
  })
}
