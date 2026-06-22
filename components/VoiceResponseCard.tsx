"use client"
import { useEffect, useRef, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { X, RotateCcw } from 'lucide-react'
import type { VoiceResponse } from '@/lib/voice/response/responseFormatter'
import { getActionIcon } from '@/lib/voice/response/responseFormatter'

const AUTO_DISMISS_MS = 8_000
const UNDO_WINDOW_MS  = 5_000

interface Props {
  response: VoiceResponse | null
  onDismiss: () => void
  onUndo?: (undoData: unknown) => void
}

const CARD_STYLES = {
  success: {
    border: 'rgba(124,58,237,0.3)',
    bg: 'rgba(124,58,237,0.08)',
    iconBg: 'rgba(124,58,237,0.15)',
  },
  error: {
    border: 'rgba(239,68,68,0.3)',
    bg: 'rgba(239,68,68,0.06)',
    iconBg: 'rgba(239,68,68,0.15)',
  },
  info: {
    border: 'rgba(59,130,246,0.3)',
    bg: 'rgba(59,130,246,0.06)',
    iconBg: 'rgba(59,130,246,0.15)',
  },
}

export default function VoiceResponseCard({ response, onDismiss, onUndo }: Props) {
  const [undoExpired, setUndoExpired] = useState(false)
  const dismissRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const undoRef    = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    if (!response) return
    setUndoExpired(false)

    dismissRef.current = setTimeout(onDismiss, AUTO_DISMISS_MS)

    if (response.card.canUndo) {
      undoRef.current = setTimeout(() => setUndoExpired(true), UNDO_WINDOW_MS)
    }

    return () => {
      if (dismissRef.current) clearTimeout(dismissRef.current)
      if (undoRef.current) clearTimeout(undoRef.current)
    }
  // onDismiss is stable (useCallback from parent)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [response])

  const handleUndo = () => {
    if (response?.card.undoData && onUndo) onUndo(response.card.undoData)
    onDismiss()
  }

  return (
    <AnimatePresence>
      {response && (
        <motion.div
          key="voice-response-card"
          initial={{ opacity: 0, y: 24, scale: 0.96 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          exit={{ opacity: 0, y: 16, scale: 0.97 }}
          transition={{ type: 'spring', stiffness: 340, damping: 26 }}
          className="fixed z-[60] left-3 right-3 bottom-[88px] md:left-auto md:right-5 md:bottom-6 md:w-80"
        >
          <div
            className="rounded-2xl p-4 shadow-xl"
            style={{
              background: CARD_STYLES[response.card.type].bg,
              border: `1px solid ${CARD_STYLES[response.card.type].border}`,
              backdropFilter: 'blur(20px)',
            }}
          >
            {/* Header row */}
            <div className="flex items-start gap-3">
              <div
                className="w-8 h-8 rounded-xl flex items-center justify-center shrink-0 text-base leading-none"
                style={{ background: CARD_STYLES[response.card.type].iconBg }}
              >
                {getActionIcon(response.card.action)}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-foreground leading-snug">
                  {response.card.title}
                </p>
                {response.card.subtitle && (
                  <p className="text-xs text-muted-foreground mt-0.5 leading-snug">
                    {response.card.subtitle}
                  </p>
                )}
              </div>
              <button
                onClick={onDismiss}
                className="shrink-0 w-6 h-6 rounded-full flex items-center justify-center text-muted-foreground hover:text-foreground hover:bg-secondary/80 transition-colors cursor-pointer"
              >
                <X size={13} />
              </button>
            </div>

            {/* Undo row — only for CREATE_EXPENSE within 5s window */}
            {response.card.canUndo && !undoExpired && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                className="mt-3 pt-3 border-t border-border/40 flex items-center justify-between gap-3 overflow-hidden"
              >
                <div className="flex-1 min-w-0">
                  <motion.div
                    className="h-[2px] rounded-full bg-violet-500/60"
                    initial={{ width: '100%' }}
                    animate={{ width: '0%' }}
                    transition={{ duration: UNDO_WINDOW_MS / 1000, ease: 'linear' }}
                  />
                  <p className="text-[10px] text-muted-foreground mt-1">Undo within 5s</p>
                </div>
                <button
                  onClick={handleUndo}
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold text-violet-400 cursor-pointer transition-colors hover:bg-violet-500/10"
                  style={{ border: '1px solid rgba(124,58,237,0.3)' }}
                >
                  <RotateCcw size={11} />
                  Undo
                </button>
              </motion.div>
            )}
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
