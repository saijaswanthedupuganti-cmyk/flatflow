import type { IntentType } from './actions/types'

export interface VoiceAnalyticsEvent {
  intent: IntentType
  success: boolean
  latencyMs: number
  errorCode?: string
}

/**
 * Logs voice assistant usage events.
 * Note: We never log the actual transcript for privacy reasons.
 * Only the classified intent, success/failure status, and latency are recorded.
 */
export function logVoiceEvent(event: VoiceAnalyticsEvent): void {
  if (typeof window === 'undefined') return

  // In a real production environment, this would send to Firebase Analytics
  // For example:
  // import { logEvent } from 'firebase/analytics'
  // import { analytics } from '@/lib/firebase'
  // if (analytics) logEvent(analytics, 'voice_interaction', event)

  // Fallback to console for debugging
  if (process.env.NODE_ENV === 'development') {
    console.log(`[Voice Analytics] Intent: ${event.intent} | Success: ${event.success} | Latency: ${Math.round(event.latencyMs)}ms`)
  }
}
