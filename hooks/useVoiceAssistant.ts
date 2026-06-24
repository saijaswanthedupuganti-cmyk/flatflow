"use client"
import { useState, useCallback, useRef, useEffect } from 'react'
import { tts } from '@/lib/voice/tts/speechSynthesis'

export type VoiceStatus = 'idle' | 'listening' | 'processing' | 'responding' | 'error'

export interface VoiceError {
  code: 'not-supported' | 'no-speech' | 'audio-capture' | 'network' | 'not-allowed' | 'aborted'
  message: string
  recoverable: boolean
}

export interface VoiceState {
  status: VoiceStatus
  transcript: string
  interimTranscript: string
  confidence: number
  error: VoiceError | null
}

export interface VoiceResult {
  transcript: string
  confidence: number
}

interface UseVoiceAssistantReturn {
  state: VoiceState
  startListening: () => void
  stopListening: () => void
  reset: () => void
  isSupported: boolean
  onTranscript: (cb: (result: VoiceResult) => void) => void
}

const ERROR_MAP: Record<string, VoiceError> = {
  'no-speech':     { code: 'no-speech',     message: "Didn't hear anything — try again",           recoverable: true  },
  'audio-capture': { code: 'audio-capture', message: 'Microphone not found',                        recoverable: false },
  'not-allowed':   { code: 'not-allowed',   message: 'Microphone blocked — allow it in settings',   recoverable: true  },
  'network':       { code: 'network',       message: 'Network error — check your connection',        recoverable: true  },
  'aborted':       { code: 'aborted',       message: 'Cancelled',                                    recoverable: true  },
}

const IDLE: VoiceState = {
  status: 'idle', transcript: '', interimTranscript: '', confidence: 0, error: null,
}

export function useVoiceAssistant(): UseVoiceAssistantReturn {
  const [state, setState] = useState<VoiceState>(IDLE)
  const recognitionRef    = useRef<any>(null)
  const silenceTimerRef   = useRef<ReturnType<typeof setTimeout> | null>(null)
  const hardTimerRef      = useRef<ReturnType<typeof setTimeout> | null>(null)
  const transcriptCbRef   = useRef<((r: VoiceResult) => void) | null>(null)

  const capturedTranscriptRef = useRef('')
  const capturedConfidenceRef = useRef(0)

  const isSupported = typeof window !== 'undefined' &&
    ('SpeechRecognition' in window || 'webkitSpeechRecognition' in window)

  const clearTimers = () => {
    if (silenceTimerRef.current) clearTimeout(silenceTimerRef.current)
    if (hardTimerRef.current)    clearTimeout(hardTimerRef.current)
  }

  const stopListening = useCallback(() => {
    clearTimers()
    if (recognitionRef.current) {
      const rec = recognitionRef.current
      recognitionRef.current = null
      rec.onstart = null
      rec.onresult = null
      rec.onerror = null
      rec.onend = null
      try { rec.abort() } catch { /* ignore */ }
    }
  }, [])

  // IMPORTANT: startListening must be SYNCHRONOUS — no async/await before recognition.start().
  // Chrome (desktop and Android) gates SpeechRecognition behind the user-gesture context.
  // Any await before recognition.start() breaks that context, causing silent not-allowed.
  // The browser's own "Allow microphone?" dialog fires from recognition.start() natively,
  // exactly like ChatGPT/Gemini — no getUserMedia pre-call needed.
  const startListening = useCallback(() => {
    if (!isSupported) {
      setState(prev => ({
        ...prev,
        status: 'error',
        error: { code: 'not-supported', message: 'Voice not supported on this device', recoverable: false },
      }))
      return
    }

    stopListening()

    setState({ status: 'listening', transcript: '', interimTranscript: '', confidence: 0, error: null })

    const SpeechRecognitionAPI = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
    const recognition = new SpeechRecognitionAPI()

    recognition.continuous      = false
    recognition.interimResults  = true
    recognition.lang            = 'en-US'
    recognition.maxAlternatives = 1

    capturedTranscriptRef.current = ''
    capturedConfidenceRef.current = 0

    let hadError = false

    try {
      const GrammarList = (window as any).SpeechGrammarList || (window as any).webkitSpeechGrammarList
      if (GrammarList) {
        const g = new GrammarList()
        g.addFromString(
          '#JSGF V1.0; grammar habitiq; ' +
          'public <task> = kitchen | bathroom | cleaning | dishes | garbage | laundry | cooking | dusting | sweeping | mopping; ' +
          'public <action> = done | completed | finished | add | spend | spent | split | pay | paid | owe | how much | what | who; ' +
          'public <currency> = rupees | rupee | rs | bucks;',
          1,
        )
        recognition.grammars = g
      }
    } catch { /* grammar hint not supported — fine */ }

    recognition.onstart = () => {
      setState({ status: 'listening', transcript: '', interimTranscript: '', confidence: 0, error: null })
    }

    recognition.onresult = (event: any) => {
      let finalTranscript   = ''
      let interimTranscript = ''
      let maxConfidence     = 0

      for (let i = event.resultIndex; i < event.results.length; i++) {
        const result = event.results[i]
        const best   = result[0]
        if (result.isFinal) {
          finalTranscript += best.transcript
          maxConfidence    = Math.max(maxConfidence, best.confidence ?? 0.8)
        } else {
          interimTranscript += best.transcript
        }
      }

      if (finalTranscript) {
        capturedTranscriptRef.current += finalTranscript
        capturedConfidenceRef.current  = maxConfidence
        clearTimers()
        silenceTimerRef.current = setTimeout(() => {
          if (recognitionRef.current) recognitionRef.current.stop()
        }, 1500)
      }

      setState(prev => ({
        ...prev,
        transcript:        capturedTranscriptRef.current || prev.transcript,
        interimTranscript,
        confidence:        maxConfidence || prev.confidence,
      }))
    }

    recognition.onerror = (event: any) => {
      hadError = true
      clearTimers()
      recognitionRef.current = null

      const voiceError = ERROR_MAP[event.error] ?? {
        code: 'network' as const,
        message: 'Something went wrong',
        recoverable: true,
      }
      setState(prev => ({ ...prev, status: 'error', error: voiceError }))

      if (event.error === 'not-allowed' || event.error === 'audio-capture') {
        tts.speak('Microphone access is blocked. Please allow microphone in your browser settings.')
        setTimeout(() => setState(prev => prev.status === 'error' ? IDLE : prev), 5000)
      } else if (event.error === 'no-speech') {
        tts.speak("Didn't catch that. Tap the mic and try again.")
        setTimeout(() => setState(prev => prev.status === 'error' ? IDLE : prev), 3000)
      } else if (event.error === 'network') {
        tts.speak('Network error. Check your connection and try again.')
        setTimeout(() => setState(prev => prev.status === 'error' ? IDLE : prev), 3000)
      } else if (event.error !== 'aborted') {
        tts.speak('Something went wrong. Tap the mic to try again.')
        setTimeout(() => setState(prev => prev.status === 'error' ? IDLE : prev), 3000)
      }
    }

    recognition.onend = () => {
      clearTimers()
      recognitionRef.current = null

      const captured   = capturedTranscriptRef.current
      const confidence = capturedConfidenceRef.current
      capturedTranscriptRef.current = ''
      capturedConfidenceRef.current = 0

      if (captured) {
        setState(prev => ({ ...prev, status: 'processing', interimTranscript: '' }))
        transcriptCbRef.current?.({ transcript: captured, confidence: confidence || 0.8 })
        return
      }

      setState(prev => prev.status === 'error' ? prev : { ...prev, status: 'idle', interimTranscript: '' })
      if (!hadError) {
        tts.speak("I didn't hear anything. Tap the mic and say your command.")
      }
    }

    recognitionRef.current = recognition

    // Called synchronously inside user gesture — browser shows native permission dialog if needed.
    try {
      recognition.start()
    } catch {
      setState(IDLE)
      recognitionRef.current = null
    }

    hardTimerRef.current = setTimeout(() => {
      if (recognitionRef.current) recognitionRef.current.stop()
    }, 12_000)
  }, [isSupported, stopListening])

  const reset = useCallback(() => {
    stopListening()
    setState(IDLE)
  }, [stopListening])

  const onTranscript = useCallback((cb: (result: VoiceResult) => void) => {
    transcriptCbRef.current = cb
  }, [])

  useEffect(() => {
    return () => {
      clearTimers()
      if (recognitionRef.current) {
        try { recognitionRef.current.abort() } catch { /* ignore */ }
      }
    }
  }, [])

  return { state, startListening, stopListening, reset, isSupported, onTranscript }
}
