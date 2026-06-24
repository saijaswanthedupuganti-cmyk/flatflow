"use client"
import { useCallback, useRef } from 'react'
import { Mic } from 'lucide-react'
import { motion } from 'framer-motion'

interface Props {
  onTap: () => void
  onLongPress?: () => void
  size: 'fab' | 'sidebar'
  isListening?: boolean
  isProcessing?: boolean
  enabled?: boolean
}

const LONG_PRESS_MS = 500

export default function VoiceButton({ onTap, onLongPress, size, isListening, isProcessing, enabled = true }: Props) {
  const timerRef           = useRef<ReturnType<typeof setTimeout> | null>(null)
  const longPressedRef     = useRef(false)

  const handlePointerDown = useCallback((e: React.PointerEvent<HTMLDivElement>) => {
    if (!enabled) return
    // Capture the pointer so pointerup fires here even if the finger drifts (critical for mobile)
    e.currentTarget.setPointerCapture(e.pointerId)
    longPressedRef.current = false
    timerRef.current = setTimeout(() => {
      longPressedRef.current = true
      onLongPress?.()
    }, LONG_PRESS_MS)
  }, [enabled, onLongPress])

  const handlePointerUp = useCallback((e: React.PointerEvent<HTMLDivElement>) => {
    if (timerRef.current) {
      clearTimeout(timerRef.current)
      timerRef.current = null
    }
    // Do not call onTap here. onClick will handle it so we keep a native user-gesture.
    // Delay clearing longPressedRef so onClick knows if this was a long press.
    setTimeout(() => {
      longPressedRef.current = false
    }, 0)
  }, [])

  const handleClick = useCallback((e: React.MouseEvent<HTMLDivElement>) => {
    if (enabled) e.stopPropagation()
    if (!enabled) return
    if (!longPressedRef.current) {
      onTap()
    }
  }, [enabled, onTap])

  const handlePointerLeave = useCallback(() => {
    if (timerRef.current) {
      clearTimeout(timerRef.current)
      timerRef.current = null
    }
  }, [])

  const handlePointerCancel = useCallback(() => {
    if (timerRef.current) {
      clearTimeout(timerRef.current)
      timerRef.current = null
    }
    longPressedRef.current = false
  }, [])

  if (size === 'sidebar') {
    return (
      <button
        onClick={enabled ? onTap : undefined}
        className="w-full flex items-center gap-2.5 px-3 py-2.5 rounded-xl transition-all cursor-pointer group"
        style={{
          background: isListening
            ? 'linear-gradient(135deg, rgba(124,58,237,0.15), rgba(79,70,229,0.1))'
            : 'rgba(124,58,237,0.06)',
          border: isListening
            ? '1px solid rgba(124,58,237,0.5)'
            : '1px solid rgba(124,58,237,0.2)',
        }}
        aria-label="Start voice assistant"
      >
        <div
          className="relative w-7 h-7 rounded-lg flex items-center justify-center shrink-0 transition-all"
          style={{
            background: isListening
              ? 'linear-gradient(135deg, #7c3aed, #4f46e5)'
              : 'rgba(124,58,237,0.15)',
            boxShadow: isListening ? '0 0 12px rgba(124,58,237,0.4)' : 'none',
          }}
        >
          {isListening && (
            <span className="absolute inset-0 rounded-lg bg-violet-500 animate-ping opacity-20" />
          )}
          <Mic size={14} className={isListening ? 'text-white' : 'text-violet-400'} />
        </div>
        <span className={`flex-1 text-sm font-medium text-left transition-colors ${isListening ? 'text-violet-400' : 'text-muted-foreground group-hover:text-foreground'}`}>
          {isListening ? 'Listening…' : isProcessing ? 'Thinking…' : 'Ask Habitiq…'}
        </span>
        {(isListening || isProcessing) && (
          <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse shrink-0" />
        )}
      </button>
    )
  }

  // size === 'fab'
  return (
    <>
      {(isListening || isProcessing) && (
        <motion.span
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          className="absolute -top-1 -right-1 w-4 h-4 bg-red-500 rounded-full z-[53] pointer-events-none border-2 border-card"
        >
          <span className="absolute inset-0 rounded-full bg-red-500 animate-ping opacity-75" />
        </motion.span>
      )}
      {/* Invisible overlay — captures taps (voice) and long-presses (petals) */}
      <div
        className="absolute inset-0 rounded-full z-[52] cursor-pointer outline-none select-none"
        onPointerDown={handlePointerDown}
        onPointerUp={handlePointerUp}
        onPointerLeave={handlePointerLeave}
        onPointerCancel={handlePointerCancel}
        onContextMenu={e => e.preventDefault()}
        onClick={handleClick}
        style={{ touchAction: 'none', WebkitTapHighlightColor: 'transparent' }}
        aria-label="Tap to ask Habitiq, hold for quick add"
        role="button"
      />
    </>
  )
}
