import { create } from 'zustand'
import { db, hasKeys } from '@/lib/firebase'
import {
  collection, doc, onSnapshot, updateDoc, query, orderBy,
} from 'firebase/firestore'
import { onRewardUnlocked, type RewardPayload } from '@/lib/rewardSignal'

export interface Reward extends RewardPayload {}

interface RewardsState {
  rewards: Reward[]
  pendingUnlockReward: Reward | null
  clearPendingUnlock: () => void
  initRewardsListener: (uid: string) => () => void
  markRewardRedeemed: (uid: string, rewardId: string) => Promise<void>
}

const LS_KEY = 'habitiq_rewards'

function loadFromLS(): Reward[] {
  if (typeof window === 'undefined') return []
  try { return JSON.parse(localStorage.getItem(LS_KEY) ?? '[]') } catch { return [] }
}

function saveToLS(rewards: Reward[]) {
  if (typeof window === 'undefined') return
  try { localStorage.setItem(LS_KEY, JSON.stringify(rewards)) } catch {}
}

let rewardsUnsub: (() => void) | null = null

export const useRewardsStore = create<RewardsState>((set, get) => {
  // Register callback — fires for both mock mode and Firestore fallback
  onRewardUnlocked((reward) => {
    set(s => {
      const updated = [reward, ...s.rewards.filter(r => r.id !== reward.id)]
      saveToLS(updated)
      return { rewards: updated, pendingUnlockReward: reward }
    })
  })

  return {
    rewards: loadFromLS(), // seed from localStorage immediately
    pendingUnlockReward: null,

    clearPendingUnlock: () => set({ pendingUnlockReward: null }),

    initRewardsListener: (uid) => {
      if (rewardsUnsub) { rewardsUnsub(); rewardsUnsub = null }
      if (!hasKeys || !uid) return () => {}

      let initialized = false
      let knownCount = 0

      const unsub = onSnapshot(
        query(collection(db, `users/${uid}/rewards`), orderBy('unlockedAt', 'desc')),
        (snap) => {
          const fsRewards: Reward[] = []
          snap.forEach(d => fsRewards.push(d.data() as Reward))

          if (!initialized) {
            initialized = true
            knownCount = fsRewards.length
            // Merge Firestore + localStorage (Firestore wins on conflict)
            const lsRewards = loadFromLS()
            const merged = [...fsRewards]
            lsRewards.forEach(lr => {
              if (!merged.find(r => r.id === lr.id)) merged.push(lr)
            })
            merged.sort((a, b) => new Date(b.unlockedAt).getTime() - new Date(a.unlockedAt).getTime())
            saveToLS(merged)
            set({ rewards: merged })
          } else if (fsRewards.length > knownCount) {
            knownCount = fsRewards.length
            saveToLS(fsRewards)
            set({ rewards: fsRewards, pendingUnlockReward: fsRewards[0] })
          } else {
            knownCount = fsRewards.length
            saveToLS(fsRewards)
            set({ rewards: fsRewards })
          }
        },
        (err) => {
          console.warn('Rewards listener error:', err)
          // Firestore unavailable — localStorage data already loaded as initial state
        }
      )

      rewardsUnsub = unsub
      return unsub
    },

    markRewardRedeemed: async (uid, rewardId) => {
      const updated = get().rewards.map(r => r.id === rewardId ? { ...r, isRedeemed: true } : r)
      saveToLS(updated)
      set({ rewards: updated })
      if (hasKeys && uid) {
        try {
          await updateDoc(doc(db, `users/${uid}/rewards/${rewardId}`), { isRedeemed: true })
        } catch { /* non-critical */ }
      }
    },
  }
})
