import { db, hasKeys } from '@/lib/firebase'
import { collection, getDocs, query, where } from 'firebase/firestore'

export interface RewardTemplate {
  id: string
  brandName: string
  discountCode: string
  description: string   // Plain-language instructions shown to the user
  isActive: boolean
  expiryDays: number    // Days from earning until the coupon expires
  category?: string     // Future: match to user's top expense category (food, grooming, electronics, personal_care)
}

const LS_POOL_KEY = 'habitiq_reward_pool'
const LS_POOL_TTL_KEY = 'habitiq_reward_pool_ttl'
const CACHE_TTL_MS = 6 * 60 * 60 * 1000 // re-fetch from Firestore every 6 hours

// Fallback pool — active when Firestore /rewardPool is unavailable or empty.
// Future: selection will be profile-based (expense category → matching brand).
// Current: uniformly random across all active entries.
const FALLBACK_POOL: RewardTemplate[] = [
  {
    id: 'beardo-20',
    brandName: 'Beardo',
    discountCode: 'HABITIQ-BEARDO-20',
    description:
      'Paste this code at checkout on beardo.com to get 20% off your entire order. Valid on all grooming products. Single-use code.',
    isActive: true,
    expiryDays: 30,
    category: 'grooming',
  },
  {
    id: 'swiggy-50',
    brandName: 'Swiggy',
    discountCode: 'HABITIQ-SWIGGY-50',
    description:
      'Apply this code on the Swiggy app at checkout to get ₹50 off your next order above ₹199. Valid on all restaurants. Single-use code.',
    isActive: true,
    expiryDays: 15,
    category: 'food',
  },
  {
    id: 'boat-15',
    brandName: 'boAt',
    discountCode: 'HABITIQ-BOAT-15',
    description:
      'Paste this code at checkout on boat-lifestyle.com for 15% off any boAt earphones, headphones, or wearables. Single-use code.',
    isActive: true,
    expiryDays: 30,
    category: 'electronics',
  },
  {
    id: 'mamaearth-10',
    brandName: 'Mamaearth',
    discountCode: 'HABITIQ-MAMA-10',
    description:
      'Paste this code at checkout on mamaearth.in for 10% off your order on skincare, haircare, or personal care products. Single-use code.',
    isActive: true,
    expiryDays: 30,
    category: 'personal_care',
  },
]

function loadPoolFromLS(): RewardTemplate[] | null {
  if (typeof window === 'undefined') return null
  try {
    const ttl = parseInt(localStorage.getItem(LS_POOL_TTL_KEY) ?? '0', 10)
    if (Date.now() - ttl > CACHE_TTL_MS) return null
    const raw = localStorage.getItem(LS_POOL_KEY)
    return raw ? JSON.parse(raw) : null
  } catch { return null }
}

function savePoolToLS(pool: RewardTemplate[]) {
  if (typeof window === 'undefined') return
  try {
    localStorage.setItem(LS_POOL_KEY, JSON.stringify(pool))
    localStorage.setItem(LS_POOL_TTL_KEY, String(Date.now()))
  } catch {}
}

function pickRandom<T>(arr: T[]): T {
  return arr[Math.floor(Math.random() * arr.length)]
}

/**
 * Returns one active reward template.
 * Priority: (1) localStorage cache → (2) Firestore /rewardPool → (3) hardcoded fallback.
 *
 * To update coupon codes: add/edit documents in Firebase Console → /rewardPool.
 * Each document: { brandName, discountCode, description, isActive, expiryDays }
 * Users see new codes automatically within 6 hours (cache TTL).
 */
export async function getActiveRewardTemplate(): Promise<RewardTemplate> {
  // 1. Use localStorage cache if fresh
  const cached = loadPoolFromLS()
  if (cached && cached.length > 0) return pickRandom(cached)

  // 2. Fetch from Firestore /rewardPool
  if (hasKeys) {
    try {
      const snap = await getDocs(
        query(collection(db, 'rewardPool'), where('isActive', '==', true))
      )
      const pool: RewardTemplate[] = []
      snap.forEach(d => pool.push({ id: d.id, ...d.data() } as RewardTemplate))
      if (pool.length > 0) {
        savePoolToLS(pool)
        return pickRandom(pool)
      }
    } catch { /* Firestore unavailable — use fallback */ }
  }

  // 3. Hardcoded fallback — filter by isActive so deactivated brands are never picked
  const activePool = FALLBACK_POOL.filter(r => r.isActive)
  if (activePool.length === 0) return FALLBACK_POOL[0] // last-resort: return first regardless
  return pickRandom(activePool)
}
