export type MicPermissionState = 'granted' | 'denied' | 'prompt' | 'not-supported'

export async function getMicPermissionState(): Promise<MicPermissionState> {
  if (typeof navigator === 'undefined') return 'not-supported'
  if (!navigator.mediaDevices?.getUserMedia) return 'not-supported'

  try {
    const result = await navigator.permissions.query({ name: 'microphone' as PermissionName })
    return result.state as MicPermissionState
  } catch {
    // permissions.query not supported (some browsers) — return prompt so we try getUserMedia
    return 'prompt'
  }
}

export async function requestMicPermission(): Promise<MicPermissionState> {
  const current = await getMicPermissionState()
  if (current === 'not-supported') return 'not-supported'

  // ALWAYS request getUserMedia, even if current === 'granted'.
  // Chromium has a bug on localhost where SpeechRecognition fails silently with not-allowed
  // unless the audio hardware stream is explicitly unlocked immediately before starting.
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    // Release the stream immediately — we just needed the permission
    stream.getTracks().forEach(t => t.stop())
    return 'granted'
  } catch (err: any) {
    if (err?.name === 'NotAllowedError' || err?.name === 'PermissionDeniedError') {
      return 'denied'
    }
    return 'prompt'
  }
}

// Check if SpeechRecognition is available in this browser
export function isSpeechRecognitionSupported(): boolean {
  if (typeof window === 'undefined') return false
  return 'SpeechRecognition' in window || 'webkitSpeechRecognition' in window
}
