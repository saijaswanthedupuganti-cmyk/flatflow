import { db, hasKeys } from '@/lib/firebase'
import { doc, getDoc, updateDoc, arrayUnion } from 'firebase/firestore'

export interface CouponDoc {
  code: string
  durationDays: number       // -1 = forever (sets year 2099)
  maxUses: number            // -1 = unlimited
  usedBy: string[]           // flatIds that redeemed this code
  expiresAt?: string         // ISO date — when the coupon itself expires
  type: 'trial_extend' | 'full_unlock'
}

export type CouponResult =
  | { success: true; durationDays: number; type: CouponDoc['type'] }
  | { success: false; error: string }

export async function validateAndRedeemCoupon(
  flatId: string,
  rawCode: string,
): Promise<CouponResult> {
  const code = rawCode.toUpperCase().trim()
  if (!code) return { success: false, error: 'Please enter a coupon code.' }

  if (!hasKeys || !db) {
    // Mock mode — any code unlocks forever
    return { success: true, durationDays: 365, type: 'full_unlock' }
  }

  const couponRef = doc(db, `coupons/${code}`)
  let couponSnap
  try {
    couponSnap = await getDoc(couponRef)
  } catch {
    return { success: false, error: 'Could not validate the coupon. Check your connection and try again.' }
  }

  if (!couponSnap.exists()) {
    return { success: false, error: 'Invalid coupon code. Please check and try again.' }
  }

  const coupon = couponSnap.data() as CouponDoc

  if (coupon.usedBy?.includes(flatId)) {
    return { success: false, error: 'This coupon has already been used for your flat.' }
  }

  if (coupon.maxUses !== -1 && (coupon.usedBy?.length ?? 0) >= coupon.maxUses) {
    return { success: false, error: 'This coupon has reached its usage limit.' }
  }

  if (coupon.expiresAt && new Date(coupon.expiresAt) < new Date()) {
    return { success: false, error: 'This coupon has expired.' }
  }

  const newEndDate =
    coupon.durationDays === -1
      ? new Date('2099-12-31').toISOString()
      : new Date(Date.now() + coupon.durationDays * 24 * 60 * 60 * 1000).toISOString()

  try {
    await updateDoc(doc(db, `flats/${flatId}`), {
      subscriptionStatus: 'active',
      trialEndDate: newEndDate,
      couponUsed: code,
    })
    await updateDoc(couponRef, { usedBy: arrayUnion(flatId) })
  } catch {
    return { success: false, error: 'Failed to activate the coupon. Please try again.' }
  }

  return { success: true, durationDays: coupon.durationDays, type: coupon.type }
}
