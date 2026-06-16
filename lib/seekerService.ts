import { db, hasKeys } from '@/lib/firebase'
import { collection, doc, getDoc, getDocs, setDoc } from 'firebase/firestore'
import type { SeekerProfile, FlatSnapshot, VacancyListing, LifestyleTag } from './discoveryTypes'

export async function fetchSeekerProfile(uid: string): Promise<SeekerProfile | null> {
  if (!hasKeys || !db) return null
  try {
    const snap = await getDoc(doc(db, 'seekerProfiles', uid))
    return snap.exists() ? (snap.data() as SeekerProfile) : null
  } catch { return null }
}

export async function saveSeekerProfile(profile: SeekerProfile): Promise<void> {
  if (!hasKeys || !db) return
  await setDoc(doc(db, 'seekerProfiles', profile.uid), profile)
}

export async function fetchActiveSeekers(): Promise<SeekerProfile[]> {
  if (!hasKeys || !db) return []
  try {
    const snap = await getDocs(collection(db, 'seekerProfiles'))
    return snap.docs.map(d => d.data() as SeekerProfile).filter(s => s.active)
  } catch { return [] }
}

export async function fetchActiveFlats(excludeFlatId?: string): Promise<FlatSnapshot[]> {
  if (!hasKeys || !db) return []
  try {
    const snap = await getDocs(collection(db, 'flats'))
    return snap.docs
      .filter(d => d.id !== excludeFlatId && d.data().vacancy?.active === true)
      .map(d => ({
        flatId: d.id,
        flatName: d.data().name ?? 'Unnamed Flat',
        memberCount: d.data().memberCount ?? 0,
        vacancy: d.data().vacancy as VacancyListing,
      }))
  } catch { return [] }
}

export async function raiseJoinRequest(
  flatId: string,
  uid: string,
  email: string,
  nickname: string,
  seekerSnapshot?: {
    city?: string
    area?: string
    lifestyle?: LifestyleTag[]
    customTags?: string[]
    about?: string
    moveIn?: string
    message?: string
  },
): Promise<void> {
  if (!hasKeys || !db) return
  const ref = doc(collection(db, `flats/${flatId}/joinRequests`))
  const payload: Record<string, unknown> = {
    id: ref.id,
    uid,
    email,
    nickname,
    status: 'pending',
    createdAt: new Date().toISOString(),
  }
  if (seekerSnapshot) {
    if (seekerSnapshot.city)           payload.city       = seekerSnapshot.city
    if (seekerSnapshot.area)           payload.area       = seekerSnapshot.area
    if (seekerSnapshot.lifestyle?.length) payload.lifestyle = seekerSnapshot.lifestyle
    if (seekerSnapshot.customTags?.length) payload.customTags = seekerSnapshot.customTags
    if (seekerSnapshot.about)          payload.about      = seekerSnapshot.about
    if (seekerSnapshot.moveIn)         payload.moveIn     = seekerSnapshot.moveIn
    if (seekerSnapshot.message)        payload.message    = seekerSnapshot.message
  }
  await setDoc(ref, payload)
}
