import type { BehavioralEvent, TrustTag, TrustTier } from './discoveryTypes'

const WINDOW_DAYS = 90

/**
 * Pure function — no side effects, no Firebase. Safe to call anywhere.
 *
 * Turns a member's raw BehavioralEvent history into a tiered TrustTag.
 * Tier thresholds (Option A, approved by product):
 *   0 "New to Habitiq"  — < 30 days tenure OR < 10 completions in window
 *   1 "Reliable"        — ≥ 30 days + ≥ 10 completions + ≥ 75% on-time + settlementRate ≥ 0.75
 *   2 "Habitiq Trusted" — ≥ 90 days + ≥ 20 completions + ≥ 90% on-time + settlementRate ≥ 0.90
 *
 * Settlement rate Phase 1 proxy:
 *   - No settlement events at all → neutral (rate = 1.0, no penalty for new users)
 *   - Has late events (daysAfterClose > 14) → rate penalized proportionally
 */
export function computeTrustTag(
  uid: string,
  flatId: string,
  allEvents: BehavioralEvent[],
  joinedAt: string | Date,
): TrustTag {
  const now = Date.now()
  const windowStart = now - WINDOW_DAYS * 86400000
  const joinedAtMs = (typeof joinedAt === 'string' ? new Date(joinedAt) : joinedAt).getTime()
  const tenureDays = Number.isFinite(joinedAtMs)
    ? Math.max(0, Math.floor((now - joinedAtMs) / 86400000))
    : 0

  // Filter to this member's events inside the rolling window
  const events = allEvents.filter(
    e => e.uid === uid && new Date(e.timestamp).getTime() >= windowStart
  )

  // ── Chore on-time rate ─────────────────────────────────────────────────────
  const taskEvents = events.filter(e => e.type === 'task_completed')
  const totalCompletions = taskEvents.length
  const onTimeCount = taskEvents.filter(e => e.onTime === true).length
  const choreOnTimeRate = totalCompletions > 0 ? onTimeCount / totalCompletions : 0

  // ── Settlement rate (Phase 1 proxy) ───────────────────────────────────────
  const settlementEvents = events.filter(e => e.type === 'settlement_completed')
  const lateSettlements = settlementEvents.filter(e => (e.daysAfterClose ?? 0) > 14)
  const settlementRate = settlementEvents.length === 0
    ? 1   // neutral — no data means no penalty
    : (settlementEvents.length - lateSettlements.length) / settlementEvents.length

  // ── Tier gates ─────────────────────────────────────────────────────────────
  let tier: TrustTier = 0

  const passesMinGate = tenureDays >= 30 && totalCompletions >= 10

  if (passesMinGate) {
    if (
      tenureDays >= 90 &&
      totalCompletions >= 20 &&
      choreOnTimeRate >= 0.90 &&
      settlementRate >= 0.90
    ) {
      tier = 2
    } else if (choreOnTimeRate >= 0.75 && settlementRate >= 0.75) {
      tier = 1
    }
  }

  // ── Plain-language reasons (always explainable) ────────────────────────────
  const reasons: string[] = []
  const pct = Math.round(choreOnTimeRate * 100)

  if (tier === 2) {
    reasons.push(`${pct}% of chores completed on time in the last 90 days`)
    reasons.push(`${tenureDays} days active on Habitiq`)
    if (settlementEvents.length > 0) reasons.push('Settlements consistently on time')
  } else if (tier === 1) {
    reasons.push(`${pct}% chore on-time rate (need 90%+ for Trusted)`)
    reasons.push(`${tenureDays} day${tenureDays === 1 ? '' : 's'} on Habitiq`)
    if (totalCompletions < 20) reasons.push(`${totalCompletions} of 20 completions needed for Trusted`)
  } else {
    if (!passesMinGate) {
      if (tenureDays < 30)
        reasons.push(`${tenureDays} day${tenureDays === 1 ? '' : 's'} active — rating unlocks after 30 days`)
      else
        reasons.push(`${totalCompletions} chore${totalCompletions === 1 ? '' : 's'} completed — need 10 to unlock rating`)
    } else {
      reasons.push(`${pct}% chore on-time rate — need 75%+ for Reliable`)
    }
  }

  if (lateSettlements.length > 0) {
    reasons.push('Some settlements were late — settle within 14 days of month close to improve')
  }

  const LABELS: Record<TrustTier, TrustTag['label']> = {
    0: 'New to Habitiq',
    1: 'Reliable',
    2: 'Habitiq Trusted',
  }

  return {
    uid,
    flatId,
    tier,
    label: LABELS[tier],
    reasons,
    computedAt: new Date().toISOString(),
    choreOnTimeRate,
    settlementRate,
    tenureDays,
    totalCompletions,
  }
}
