import { useFlatStore } from '@/store/useFlatStore'

export type GatedFeature =
  | 'create_task'
  | 'create_flat'
  | 'add_expense'
  | 'create_bill'

export interface SubscriptionInfo {
  /** true = trial active or coupon applied — full access */
  isActive: boolean
  /** true = coupon redeemed (status === 'active') — show crown/premium treatment */
  isPremium: boolean
  /** true = trial ended, no coupon yet */
  isExpired: boolean
  /** Days remaining in trial (0 when expired) */
  daysLeft: number
  /** Whether this specific feature is allowed */
  can: (feature: GatedFeature) => boolean
  /** Max total flats (created + joined). Free/trial = 1, Premium = 6 */
  maxFlats: number
}

function daysRemaining(isoDate: string | null): number {
  if (!isoDate) return 30
  const diff = new Date(isoDate).getTime() - Date.now()
  return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)))
}

export function useSubscription(): SubscriptionInfo {
  const subscription = useFlatStore(s => s.subscription)

  // Legacy flats (no subscription field yet) → treat as active
  if (!subscription) {
    return {
      isActive: true,
      isPremium: false,
      isExpired: false,
      daysLeft: 30,
      can: () => true,
      maxFlats: 1,
    }
  }

  const isActive = subscription.status === 'trial' || subscription.status === 'active'
  const isPremium = subscription.status === 'active'
  const isExpired = subscription.status === 'expired'
  const daysLeft = daysRemaining(subscription.trialEndDate)

  // Premium (active coupon) = 3 flats; free/trial/expired = 1
  const maxFlats = subscription.status === 'active' ? 3 : 1

  const BLOCKED_WHEN_EXPIRED: GatedFeature[] = [
    'create_task',
    'create_flat',
    'add_expense',
    'create_bill',
  ]

  return {
    isActive,
    isPremium,
    isExpired,
    daysLeft,
    maxFlats,
    can: (feature) => {
      if (!isExpired) return true
      return !BLOCKED_WHEN_EXPIRED.includes(feature)
    },
  }
}
