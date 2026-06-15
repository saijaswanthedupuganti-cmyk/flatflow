import { useFlatStore } from '@/store/useFlatStore'

export type GatedFeature =
  | 'create_task'
  | 'create_flat'
  | 'add_expense'
  | 'create_bill'

export interface SubscriptionInfo {
  /** true = trial active or coupon applied — full access */
  isActive: boolean
  /** true = trial ended, no coupon yet */
  isExpired: boolean
  /** Days remaining in trial (0 when expired) */
  daysLeft: number
  /** Whether this specific feature is allowed */
  can: (feature: GatedFeature) => boolean
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
      isExpired: false,
      daysLeft: 30,
      can: () => true,
    }
  }

  const isActive = subscription.status === 'trial' || subscription.status === 'active'
  const isExpired = subscription.status === 'expired'
  const daysLeft = daysRemaining(subscription.trialEndDate)

  const BLOCKED_WHEN_EXPIRED: GatedFeature[] = [
    'create_task',
    'create_flat',
    'create_bill',
  ]

  return {
    isActive,
    isExpired,
    daysLeft,
    can: (feature) => {
      if (!isExpired) return true
      return !BLOCKED_WHEN_EXPIRED.includes(feature)
    },
  }
}
