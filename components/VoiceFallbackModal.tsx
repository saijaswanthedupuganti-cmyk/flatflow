"use client"
import { useEffect, useRef, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { X, Send, Mic } from 'lucide-react'

interface Props {
  isOpen: boolean
  onClose: () => void
  onSubmit: (text: string) => void
  onRetryMic?: () => void
}

const EXAMPLES = [
  'Kitchen done',
  'Add 500 for groceries, split it',
  'How much does Bhanu owe me?',
  'What are my tasks?',
]

export default function VoiceFallbackModal({ isOpen, onClose, onSubmit, onRetryMic }: Props) {
  const [text, setText] = useState('')
  const inputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (isOpen) {
      setText('')
      setTimeout(() => inputRef.current?.focus(), 150)
    }
  }, [isOpen])

  const handleSubmit = () => {
    const trimmed = text.trim()
    if (!trimmed) return
    onSubmit(trimmed)
    setText('')
    onClose()
  }

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          {/* Backdrop */}
          <motion.div
            key="fallback-backdrop"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-[190] bg-black/50 backdrop-blur-sm"
            onClick={onClose}
          />

          {/* Sheet */}
          <motion.div
            key="fallback-sheet"
            initial={{ y: '100%' }}
            animate={{ y: 0 }}
            exit={{ y: '100%' }}
            transition={{ type: 'spring', stiffness: 380, damping: 32 }}
            className="fixed bottom-0 left-0 right-0 z-[191] rounded-t-3xl pb-safe"
            style={{ background: 'var(--card)', borderTop: '1px solid var(--border)' }}
          >
            {/* Handle */}
            <div className="flex justify-center pt-3 pb-1">
              <div className="w-10 h-1 rounded-full bg-border" />
            </div>

            <div className="px-5 pt-3 pb-6">
              {/* Header */}
              <div className="flex items-center justify-between mb-4">
                <div>
                  <p className="font-bold text-base text-foreground">Type a command</p>
                  <p className="text-xs text-muted-foreground mt-0.5">Voice not supported on this device</p>
                </div>
                <button onClick={onClose} className="p-2 rounded-xl hover:bg-secondary transition-colors cursor-pointer">
                  <X size={18} className="text-muted-foreground" />
                </button>
              </div>

              {/* Input */}
              <div className="flex gap-2.5 items-center">
                <input
                  ref={inputRef}
                  type="text"
                  value={text}
                  onChange={e => setText(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && handleSubmit()}
                  placeholder='e.g. "Kitchen done" or "Add 500 for groceries"'
                  className="flex-1 rounded-xl px-4 py-3 text-sm text-foreground placeholder:text-muted-foreground/60 focus:outline-none focus:ring-2 focus:ring-primary/30"
                  style={{ background: 'var(--secondary)', border: '1px solid var(--border)' }}
                />
                
                {onRetryMic && (
                  <button
                    onClick={() => {
                      onClose()
                      onRetryMic()
                    }}
                    className="w-11 h-11 rounded-xl flex items-center justify-center shrink-0 border border-border text-muted-foreground hover:text-foreground hover:bg-secondary transition-colors cursor-pointer"
                  >
                    <Mic size={18} />
                  </button>
                )}

                <button
                  onClick={handleSubmit}
                  disabled={!text.trim()}
                  className="w-11 h-11 rounded-xl flex items-center justify-center shrink-0 transition-all cursor-pointer disabled:opacity-40"
                  style={{ background: 'linear-gradient(135deg, #7c3aed, #4f46e5)' }}
                >
                  <Send size={16} className="text-white" />
                </button>
              </div>

              {/* Example pills */}
              <div className="mt-4">
                <p className="text-[11px] font-semibold text-muted-foreground uppercase tracking-widest mb-2.5">Examples</p>
                <div className="flex flex-wrap gap-2">
                  {EXAMPLES.map(ex => (
                    <button
                      key={ex}
                      onClick={() => { setText(ex); inputRef.current?.focus() }}
                      className="text-xs px-3 py-1.5 rounded-full border border-border text-muted-foreground hover:text-foreground hover:border-primary/40 transition-colors cursor-pointer"
                    >
                      {ex}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  )
}
