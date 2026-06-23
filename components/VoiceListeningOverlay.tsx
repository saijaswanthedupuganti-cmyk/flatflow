"use client"
import { useEffect } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { X } from 'lucide-react'
import WaveformVisualizer from './WaveformVisualizer'
import type { VoiceState } from '@/hooks/useVoiceAssistant'
import type { VoiceResponse } from '@/lib/voice/response/responseFormatter'

interface Props {
  state: VoiceState
  onStop: () => void
  response?: VoiceResponse | null
  onDismissResponse?: () => void
}

export default function VoiceListeningOverlay({ state, onStop, response, onDismissResponse }: Props) {
  const isResponding = !!response
  // Overlay only shows while actively listening or processing — NOT during response.
  // Response display is handled by VoiceResponseCard (non-blocking floating card).
  // Keeping the overlay up during isResponding blocks all tap targets behind it.
  const visible = state.status === 'listening' || state.status === 'processing'

  // Close overlay if window loses focus (e.g. tab switch) while listening
  useEffect(() => {
    if (!visible) return
    const handleBlur = () => onStop()
    window.addEventListener('blur', handleBlur)
    return () => window.removeEventListener('blur', handleBlur)
  }, [visible, onStop])

  const handleClose = () => { onStop() }

  return (
    <AnimatePresence>
      {visible && (
        <motion.div
          key="voice-overlay"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.2 }}
          className="fixed inset-0 z-[200] flex items-center justify-center"
          style={{ background: 'rgba(12,11,15,0.55)', backdropFilter: 'blur(10px)' }}
          onClick={e => { if (e.target === e.currentTarget) handleClose() }}
        >
          {/* Floating center card */}
          <motion.div
            initial={{ scale: 0.82, opacity: 0, y: 24 }}
            animate={{ scale: 1, opacity: 1, y: 0 }}
            exit={{ scale: 0.88, opacity: 0, y: 12 }}
            transition={{ type: 'spring', stiffness: 380, damping: 28 }}
            className="relative flex flex-col items-center gap-5 rounded-[28px]"
            style={{
              background: 'rgba(26,24,32,0.97)',
              border: isResponding
                ? '1px solid rgba(124,58,237,0.38)'
                : '1px solid rgba(124,58,237,0.22)',
              boxShadow: '0 32px 80px rgba(0,0,0,0.65), 0 0 0 1px rgba(255,255,255,0.03), inset 0 1px 0 rgba(255,255,255,0.04)',
              width: 'min(340px, 88vw)',
              padding: '36px 32px 28px',
            }}
            onClick={e => e.stopPropagation()}
          >
            {/* Close button */}
            <button
              onClick={handleClose}
              className="absolute top-4 right-4 w-8 h-8 rounded-full flex items-center justify-center transition-all cursor-pointer hover:bg-white/10"
              style={{ background: 'rgba(255,255,255,0.05)', color: 'rgba(255,255,255,0.35)' }}
              aria-label="Close"
            >
              <X size={13} />
            </button>

            {/* Orb */}
            <div className="relative flex items-center justify-center" style={{ width: 120, height: 120 }}>

              {/* Ambient glow */}
              <motion.div
                className="absolute rounded-full"
                style={{
                  inset: -20,
                  background: isResponding
                    ? 'radial-gradient(circle, rgba(124,58,237,0.5) 0%, rgba(99,102,241,0.25) 45%, transparent 70%)'
                    : 'radial-gradient(circle, rgba(124,58,237,0.45) 0%, rgba(79,70,229,0.2) 45%, transparent 70%)',
                  filter: 'blur(18px)',
                }}
                animate={
                  isResponding
                    ? { scale: [1, 1.2, 1, 1.12, 1], opacity: [0.5, 0.75, 0.5, 0.7, 0.5] }
                    : state.status === 'listening'
                    ? { scale: [1, 1.35, 1], opacity: [0.6, 0.9, 0.6] }
                    : { scale: 1, opacity: 0.35 }
                }
                transition={{ duration: isResponding ? 3 : 2.4, repeat: Infinity, ease: 'easeInOut' }}
              />

              {/* Morphing orb */}
              <motion.div
                className="relative overflow-hidden"
                style={{ width: 120, height: 120 }}
                animate={
                  isResponding ? {
                    borderRadius: '50%',
                    scale: [1, 1.05, 1, 1.03, 1],
                  } : state.status === 'listening' ? {
                    borderRadius: [
                      '50%',
                      '46% 54% 52% 48% / 48% 46% 54% 52%',
                      '53% 47% 46% 54% / 52% 54% 46% 48%',
                      '48% 52% 54% 46% / 54% 48% 52% 46%',
                      '50%',
                    ],
                    scale: [1, 1.045, 0.97, 1.03, 1],
                  } : {
                    borderRadius: '50%',
                    scale: [1, 1.015, 1],
                  }
                }
                transition={
                  isResponding ? {
                    scale: { duration: 3, repeat: Infinity, ease: 'easeInOut' },
                  } : state.status === 'listening' ? {
                    borderRadius: { duration: 4, repeat: Infinity, ease: 'easeInOut' },
                    scale: { duration: 2.4, repeat: Infinity, ease: 'easeInOut' },
                  } : {
                    scale: { duration: 2.2, repeat: Infinity, ease: 'easeInOut' },
                  }
                }
              >
                {/* Base gradient */}
                <div
                  className="absolute inset-0"
                  style={{
                    background: isResponding
                      ? 'linear-gradient(135deg, #6d28d9 0%, #4338ca 35%, #7c3aed 65%, #4f46e5 100%)'
                      : 'linear-gradient(135deg, #7c3aed 0%, #4f46e5 35%, #a855f7 65%, #6366f1 100%)',
                  }}
                />

                {/* Rotating color layer */}
                <motion.div
                  className="absolute inset-0"
                  animate={{ rotate: 360 }}
                  transition={{
                    duration: isResponding ? 3.5 : state.status === 'processing' ? 1.8 : 5,
                    repeat: Infinity,
                    ease: 'linear',
                  }}
                  style={{
                    background: isResponding
                      ? 'conic-gradient(from 0deg, rgba(139,92,246,0.9) 0%, rgba(79,70,229,0.35) 30%, rgba(109,40,217,0.95) 55%, rgba(99,102,241,0.4) 80%, rgba(139,92,246,0.9) 100%)'
                      : 'conic-gradient(from 0deg, rgba(168,85,247,0.85) 0%, rgba(79,70,229,0.3) 30%, rgba(124,58,237,0.95) 55%, rgba(99,102,241,0.35) 80%, rgba(168,85,247,0.85) 100%)',
                  }}
                />

                {/* Moving shimmer */}
                <motion.div
                  className="absolute inset-0"
                  animate={{
                    background: [
                      'radial-gradient(circle at 28% 28%, rgba(255,255,255,0.28) 0%, transparent 55%)',
                      'radial-gradient(circle at 72% 68%, rgba(255,255,255,0.28) 0%, transparent 55%)',
                      'radial-gradient(circle at 32% 72%, rgba(255,255,255,0.22) 0%, transparent 55%)',
                      'radial-gradient(circle at 68% 28%, rgba(255,255,255,0.28) 0%, transparent 55%)',
                      'radial-gradient(circle at 28% 28%, rgba(255,255,255,0.28) 0%, transparent 55%)',
                    ],
                  }}
                  transition={{ duration: isResponding ? 3 : 5, repeat: Infinity, ease: 'easeInOut' }}
                />
              </motion.div>

              {/* Processing ring — spins fast when thinking */}
              {state.status === 'processing' && (
                <motion.div
                  className="absolute rounded-full"
                  style={{
                    inset: -5,
                    border: '2px solid transparent',
                    borderTopColor: '#a78bfa',
                    borderBottomColor: '#6366f1',
                  }}
                  animate={{ rotate: 360 }}
                  transition={{ duration: 1, repeat: Infinity, ease: 'linear' }}
                />
              )}

              {/* Responding — soft ring pulses outward */}
              {isResponding && (
                <motion.div
                  className="absolute rounded-full"
                  style={{ inset: -8, border: '1.5px solid rgba(167,139,250,0.35)' }}
                  animate={{ scale: [1, 1.1, 1], opacity: [0.5, 0.15, 0.5] }}
                  transition={{ duration: 2.5, repeat: Infinity, ease: 'easeInOut' }}
                />
              )}
            </div>

            {/* Status label */}
            <div className="flex items-center gap-2">
              {state.status === 'listening' && (
                <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse shrink-0" />
              )}
              <p
                className="text-[11px] font-bold uppercase tracking-[0.14em]"
                style={{ color: state.status === 'listening' ? '#a78bfa' : 'rgba(167,139,250,0.6)' }}
              >
                {state.status === 'listening' ? 'Listening…' : 'Thinking…'}
              </p>
            </div>

            {/* Content area */}
            <div className="w-full text-center" style={{ minHeight: 64 }}>
              <AnimatePresence mode="wait">
                {state.status === 'listening' && (state.interimTranscript || state.transcript) ? (
                  <motion.p
                    key="transcript"
                    initial={{ opacity: 0, y: 4 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0 }}
                    className="text-[17px] font-semibold leading-snug"
                    style={{ color: '#f4f3f8' }}
                  >
                    {state.interimTranscript || state.transcript}
                  </motion.p>
                ) : state.status === 'listening' ? (
                  <motion.div
                    key="prompt"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="space-y-1"
                  >
                    <p className="text-[15px]" style={{ color: 'rgba(244,243,248,0.35)', fontStyle: 'italic' }}>
                      Speak now…
                    </p>
                    <p className="text-[11px]" style={{ color: 'rgba(244,243,248,0.18)' }}>
                      e.g. "Kitchen done" · "Add 500 for groceries"
                    </p>
                  </motion.div>
                ) : state.transcript ? (
                  <motion.p
                    key="processing"
                    initial={{ opacity: 0, y: 4 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="text-[16px] font-medium leading-snug"
                    style={{ color: 'rgba(244,243,248,0.65)' }}
                  >
                    "{state.transcript}"
                  </motion.p>
                ) : null}
              </AnimatePresence>
            </div>

            {/* Waveform — listening only */}
            <AnimatePresence>
              {state.status === 'listening' && !isResponding && (
                <motion.div
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: 36 }}
                  exit={{ opacity: 0, height: 0 }}
                  transition={{ duration: 0.22 }}
                >
                  <WaveformVisualizer isActive={state.status === 'listening'} width={200} height={36} />
                </motion.div>
              )}
            </AnimatePresence>

            {/* Action hint */}
            {state.status === 'listening' && (
              <motion.button
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.5 }}
                onClick={onStop}
                className="text-[12px] cursor-pointer transition-colors hover:text-white/50"
                style={{ color: 'rgba(255,255,255,0.22)' }}
              >
                Tap to stop
              </motion.button>
            )}

            <p
              className="text-[11px] text-center leading-relaxed"
              style={{ color: 'rgba(255,255,255,0.18)' }}
            >
              "Kitchen done" · "Add 500 for groceries" · "How much does Bhanu owe?"
            </p>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
