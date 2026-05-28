import { db, hasKeys } from '@/lib/firebase'
import { collection, doc, setDoc, getDocs } from 'firebase/firestore'

export interface NpsResponse {
  id: string
  uid: string
  nickname: string
  flatId: string
  score: number
  comment: string
  createdAt: string
}

export async function submitNps(params: {
  uid: string
  nickname: string
  flatId: string
  score: number
  comment: string
}): Promise<void> {
  if (!hasKeys || !db) return
  const id = crypto.randomUUID()
  await setDoc(doc(db, `flats/${params.flatId}/npsResponses/${id}`), {
    id,
    uid: params.uid,
    nickname: params.nickname,
    flatId: params.flatId,
    score: params.score,
    comment: params.comment,
    createdAt: new Date().toISOString(),
  })
}

export async function getNpsResponses(flatId: string): Promise<NpsResponse[]> {
  if (!hasKeys || !db) return MOCK_NPS_RESPONSES
  const snap = await getDocs(collection(db, `flats/${flatId}/npsResponses`))
  return snap.docs
    .map(d => d.data() as NpsResponse)
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
}

const MOCK_NPS_RESPONSES: NpsResponse[] = [
  { id: 'n1', uid: 'u2', nickname: 'Rahul', flatId: 'FLAT-1234', score: 9, comment: 'Really smooth, saves arguments about chores!', createdAt: new Date(Date.now() - 2 * 86400000).toISOString() },
  { id: 'n2', uid: 'u3', nickname: 'Arjun', flatId: 'FLAT-1234', score: 8, comment: 'Love the swap feature', createdAt: new Date(Date.now() - 5 * 86400000).toISOString() },
]
