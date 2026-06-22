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

  // Refs that mirror in-flight recognition session data — safe to read outside setState
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
      try { recognitionRef.current.stop() } catch { /* already stopped */ }
      recognitionRef.current = null
    }
  }, [])

  const startListening = useCallback(() => {
    if (!isSupported) {
      setState(prev => ({ ...prev, status: 'error', error: { code: 'not-supported', message: 'Voice is not supported on this device', recoverable: false } }))
      return
    }

    stopListening()

    const SpeechRecognitionAPI = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
    const recognition = new SpeechRecognitionAPI()

    recognition.continuous      = false  // single-utterance: auto-stops after speech pause
    recognition.interimResults  = true
    recognition.lang            = 'en-US' // en-US has broadest coverage; works well with Indian English
    recognition.maxAlternatives = 1

    // Reset per-session capture refs
    capturedTranscriptRef.current = ''
    capturedConfidenceRef.current = 0

    // Track whether onerror already fired so onend doesn't duplicate the TTS feedback
    let hadError = false

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

      if (finalTranscript) {
        capturedTranscriptRef.current += finalTranscript
        capturedConfidenceRef.current  = maxConfidence
        // Schedule stop 1.5s after the last final result
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
      const voiceError = ERROR_MAP[event.error] ?? { code: 'network' as const, message: 'Something went wrong', recoverable: true }
      setState(prev => ({ ...prev, status: 'error', error: voiceError }))
      // Speak error feedback — TTS calls are outside setState (safe)
      if (event.error === 'no-speech') {
        tts.speak("I didn't catch that. Try again.")
      } else if (event.error === 'not-allowed') {
        tts.speak("Microphone access is blocked. Please allow it in your browser settings.")
      } else if (event.error !== 'aborted') {
        tts.speak("Something went wrong. Tap the mic and try again.")
      }
    }

    recognition.onend = () => {
      clearTimers()
      recognitionRef.current = null

      const captured    = capturedTranscriptRef.current
      const confidence  = capturedConfidenceRef.current
      capturedTranscriptRef.current = ''
      capturedConfidenceRef.current = 0

      if (captured) {
        // Have a transcript — hand off to NLU processor and show "thinking" state
        setState(prev => ({ ...prev, status: 'processing', interimTranscript: '' }))
        // Call the processor callback OUTSIDE setState (safe side effect)
        transcriptCbRef.current?.({ transcript: captured, confidence: confidence || 0.8 })
        return
      }

      // No transcript and no error already handled — user didn't speak
      setState(prev => prev.status === 'error' ? prev : { ...prev, status: 'idle', interimTranscript: '' })
      if (!hadError) {
        tts.speak("I didn't hear anything. Tap the mic and say your command.")
      }
    }

    // Show overlay immediately — don't wait for onstart
    setState({ status: 'listening', transcript: '', interimTranscript: '', confidence: 0, error: null })

    recognitionRef.current = recognition
    try {
      recognition.start()
    } catch {
      setState(prev => ({ ...prev, status: 'idle' }))
      recognitionRef.current = null
      return
    }

    // Hard stop after 12 seconds regardless
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
