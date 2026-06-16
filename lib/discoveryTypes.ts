export type BehavioralEventType =
  | 'task_completed'
  | 'settlement_completed'
  | 'member_joined'
  | 'member_left'

/**
 * Immutable event log for computing discovery trust tags.
 * Stored at /flats/{flatId}/behavioralEvents/{eventId}.
 * Never trimmed (unlike activityLog which is capped at 50).
 */
export interface BehavioralEvent {
  id: string
  flatId: string
  uid: string              // whose behavior this records
  type: BehavioralEventType
  timestamp: string        // ISO

  // task_completed
  taskId?: string
  taskName?: string
  onTime?: boolean         // true if completionDate <= task.dueDate
  daysLate?: number        // 0 if onTime, positive integer if late

  // settlement_completed
  settlementAmount?: number
  currency?: string
  monthCycleId?: string    // which cycle this settlement closes
  daysAfterClose?: number  // days since month cycle was closed (0 = same day)
}

// ── Vacancy listing ───────────────────────────────────────────────────────────

export type LifestyleTag =
  // Work & schedule
  | 'it_professional' | 'finance' | 'student' | 'freelancer'
  | 'wfh' | 'night_shift' | 'nine_to_five'
  // Social & personality
  | 'early_bird' | 'night_owl' | 'homebody' | 'social' | 'fitness_freak'
  // Diet & habits
  | 'vegetarian' | 'jain' | 'non_veg' | 'no_smoking' | 'no_drinking'
  // Flat vibe
  | 'neat_freak' | 'guest_friendly' | 'minimal_guests' | 'pets_ok' | 'no_pets'

export interface VacancyListing {
  active: boolean            // flat is actively looking for new members
  bedsAvailable: number      // 1–4
  rentPerHead: number | null // rough per-person rent (null = not disclosed)
  currency: string           // 'INR' | 'USD' etc.
  city: string
  area: string               // locality / neighbourhood
  existingMembersGender: 'all_male' | 'all_female' | 'mixed' | null
  preferredGender: 'any' | 'male' | 'female'
  lifestyle: LifestyleTag[]
  customTags: string[]       // user-created hashtags e.g. ["TCS","Hitech City"]; globally shared
  about: string              // short blurb about the flat (max 200 chars)
  updatedAt: string          // ISO
}

// ── Global shared hashtag (stored at /discoveryTags/{slug}) ──────────────────
export interface DiscoveryTag {
  id: string      // slug e.g. "tcs", "hitech-city"
  label: string   // display label e.g. "TCS", "Hitech City"
  createdBy: string
  createdAt: string
}

// ── Seeker profile (stored at /seekerProfiles/{uid}) ─────────────────────────
// Any logged-in user — whether already in a flat or not — can post this to say
// "I'm looking for a flat." Admins with vacancies can discover these profiles.
export interface SeekerProfile {
  uid: string
  displayName: string
  city: string
  area: string
  lifestyle: LifestyleTag[]
  customTags: string[]
  about: string
  active: boolean
  updatedAt: string
}

// Enriched join request — stored alongside the base JoinRequest fields so the
// admin card can display the full seeker story without a secondary lookup.
export type MoveInTiming = 'asap' | 'this_month' | 'next_month' | 'flexible'

export interface EnrichedJoinRequest {
  id: string
  uid: string
  nickname: string
  email: string
  status: 'pending' | 'approved' | 'rejected'
  createdAt: string
  // seeker profile snapshot (optional — present when raised via Discover flow)
  city?: string
  area?: string
  lifestyle?: LifestyleTag[]
  customTags?: string[]
  about?: string
  moveIn?: MoveInTiming
  message?: string
}

// Snapshot of a flat used for browsing in the Discover tab.
export interface FlatSnapshot {
  flatId: string
  flatName: string
  memberCount: number
  vacancy: VacancyListing
}

/**
 * Tiered trust tag computed from a member's BehavioralEvent history.
 * Tier 0 = neutral default, never penalizing.
 */
export type TrustTier = 0 | 1 | 2

export interface TrustTag {
  uid: string
  flatId: string
  tier: TrustTier
  label: 'New to Habitiq' | 'Reliable' | 'Habitiq Trusted'
  reasons: string[]        // plain-language explanations shown to the user
  computedAt: string       // ISO — when this tag was last computed
  choreOnTimeRate: number  // 0–1
  settlementRate: number   // 0–1
  tenureDays: number
  totalCompletions: number
}
