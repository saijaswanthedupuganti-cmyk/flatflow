// Decoupled signal so useFlatStore can trigger a reward unlock
// without importing useRewardsStore (which would create a circular dependency).

export interface RewardPayload {
  id: string
  brandName: string
  discountCode: string
  description: string
  unlockedAt: string
  isRedeemed: boolean
  expiryDate: string
}

type Callback = (reward: RewardPayload) => void
let _cb: Callback | null = null

export function onRewardUnlocked(cb: Callback) { _cb = cb }
export function emitRewardUnlocked(reward: RewardPayload) { _cb?.(reward) }
