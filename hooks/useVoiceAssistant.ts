"use client"
import { useState, useCallback, useRef, useEffect } from 'react'

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
  'no-speech':     { code: 'no-speech',     message: "Didn't hear anything — try again?",          recoverable: true  },
  'audio-capture': { code: 'audio-capture', message: 'Microphone not found',                        recoverable: false },
  'not-allowed':   { code: 'not-allowed',   message: 'Microphone access denied — enable in settings', recoverable: true },
  'network':       { code: 'network',       message: 'Connection issue — try again',                recoverable: true  },
  'aborted':       { code: 'aborted',       message: 'Cancelled',                                   recoverable: true  },
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

  const isSupported = typeof window !== 'undefined' &&
    ('SpeechRecognition' in window || 'webkitSpeechRecognition' in window)

  const clearTimers = () => {
    if (silenceTimerRef.current) clearTimeout(silenceTimerRef.current)
    if (hardTimerRef.current)    clearTimeout(hardTimerRef.current)
  }

  const stopListening = useCallback(() => {
    clearTimers()
    if (recognitionRef.current) {
      try { recognitionRef.current.stop() } catch { /* already stopped */ }
      recognitionRef.current = null
    }
  }, [])

  const startListening = useCallback(() => {
    if (!isSupported) {
      setState(prev => ({ ...prev, status: 'error', error: { code: 'not-supported', message: 'Voice is not supported on this device', recoverable: false } }))
      return
    }

    // Stop any ongoing session first
    stopListening()

    const SpeechRecognitionAPI = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
    const recognition = new SpeechRecognitionAPI()

    recognition.continuous      = true
    recognition.interimResults  = true
    recognition.lang            = 'en-IN'
    recognition.maxAlternatives = 3

    // Domain grammar hints — improves accuracy for flat vocabulary
    try {
      const GrammarList = (window as any).SpeechGrammarList || (window as any).webkitSpeechGrammarList
      if (GrammarList) {
        const grammar = new GrammarList()
        grammar.addFromString(
          '#JSGF V1.0; grammar habitiq; ' +
          'public <task> = kitchen | bathroom | cleaning | dishes | garbage | laundry | cooking | dusting | sweeping | mopping; ' +
          'public <action> = done | completed | finished | add | spend | spent | split | pay | paid | owe | how much | what | who; ' +
          'public <currency> = rupees | rupee | rs | bucks;',
          1
        )
        recognition.grammars = grammar
      }
    } catch { /* grammar not supported — continue without */ }

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

      setState(prev => ({
        ...prev,
        transcript:        finalTranscript   || prev.transcript,
        interimTranscript,
        confidence:        maxConfidence     || prev.confidence,
      }))

      // Schedule stop 1.5s after the last final result
      if (finalTranscript) {
        clearTimers()
        silenceTimerRef.current = setTimeout(() => {
          if (recognitionRef.current) recognitionRef.current.stop()
        }, 1500)
      }
    }

    recognition.onerror = (event: any) => {
      clearTimers()
      const voiceError = ERROR_MAP[event.error] ?? { code: 'network' as const, message: 'Something went wrong', recoverable: true }
      setState(prev => ({ ...prev, status: 'error', error: voiceError }))
      recognitionRef.current = null
    }

    recognition.onend = () => {
      clearTimers()
      recognitionRef.current = null
      setState(prev => {
        if (prev.transcript && transcriptCbRef.current) {
          transcriptCbRef.current({ transcript: prev.transcript, confidence: prev.confidence })
          return { ...prev, status: 'processing' }
        }
        // No transcript — back to idle (or keep error if already errored)
        return prev.status === 'error' ? prev : { ...prev, status: 'idle' }
      })
    }

    recognitionRef.current = recognition
    recognition.start()

    // Hard stop after 10 seconds regardless
    hardTimerRef.current = setTimeout(() => {
      if (recognitionRef.current) recognitionRef.current.stop()
    }, 10_000)
  }, [isSupported, stopListening])

  const reset = useCallback(() => {
    stopListening()
    setState(IDLE)
  }, [stopListening])

  // Register transcript callback — called by VoiceButton once NLU is wired (Sprint 2)
  const onTranscript = useCallback((cb: (result: VoiceResult) => void) => {
    transcriptCbRef.current = cb
  }, [])

  // Cleanup on unmount
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
