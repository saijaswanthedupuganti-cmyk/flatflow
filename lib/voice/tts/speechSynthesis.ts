class VoiceSynthesizer {
  private synth: SpeechSynthesis | null = null
  private preferredVoice: SpeechSynthesisVoice | null = null
  private enabled = true

  private get isAvailable(): boolean {
    return typeof window !== 'undefined' && 'speechSynthesis' in window
  }

  init(): void {
    if (!this.isAvailable) return
    this.synth = window.speechSynthesis

    const pick = () => {
      const voices = this.synth!.getVoices()
      this.preferredVoice =
        voices.find(v => v.lang === 'en-IN' && /female/i.test(v.name)) ??
        voices.find(v => v.lang === 'en-IN') ??
        voices.find(v => v.lang === 'en-GB' && /female/i.test(v.name)) ??
        voices.find(v => v.lang === 'en-GB') ??
        voices.find(v => v.lang.startsWith('en-US') && /female/i.test(v.name)) ??
        voices.find(v => v.lang.startsWith('en')) ??
        voices[0] ??
        null
    }

    if (this.synth.getVoices().length > 0) {
      pick()
    } else {
      this.synth.onvoiceschanged = pick
    }
  }

  speak(text: string, opts?: { rate?: number; pitch?: number }): void {
    if (!this.enabled || !this.synth) return

    this.synth.cancel()

    const utterance      = new SpeechSynthesisUtterance()
    utterance.voice      = this.preferredVoice
    utterance.lang       = 'en-IN'
    utterance.rate       = opts?.rate  ?? 1.15   // slightly faster — feels more natural
    utterance.pitch      = opts?.pitch ?? 1.0
    utterance.volume     = 1.0
    // Trim long responses — card shows full text
    utterance.text = text.length > 150 ? text.slice(0, 148) + '…' : text

    this.synth.speak(utterance)
  }

  // Must be called synchronously inside a user-gesture handler (click/tap).
  // Primes the audio context so async speak() calls work on iOS/Android.
  unlock(): void {
    if (!this.isAvailable) return
    if (!this.synth) this.init()
    try {
      const u = new SpeechSynthesisUtterance(' ')
      u.volume = 0
      u.rate   = 10
      this.synth!.cancel()
      this.synth!.speak(u)
      setTimeout(() => this.synth?.cancel(), 50)
    } catch { /* ignore */ }
  }

  stop(): void {
    this.synth?.cancel()
  }

  setEnabled(v: boolean): void {
    this.enabled = v
    if (!v) this.stop()
  }

  isSpeaking(): boolean {
    return this.synth?.speaking ?? false
  }
}

// Singleton — init on first import
export const tts = new VoiceSynthesizer()

// Must be called client-side (e.g. in useEffect or onClick)
export function initTTS(): void {
  tts.init()
}
