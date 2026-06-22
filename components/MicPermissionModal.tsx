"use client"
import { motion, AnimatePresence } from 'framer-motion'
import { Mic, X } from 'lucide-react'

interface Props {
  isOpen: boolean
  onAllow: () => void   // called synchronously — caller starts recognition immediately
  onDismiss: () => void
}

export default function MicPermissionModal({ isOpen, onAllow, onDismiss }: Props) {
  // Synchronous — no await, preserves the browser user-gesture context so
  // SpeechRecognition.start() is allowed to run without being blocked by Chrome.
  const handleAllow = () => {
    localStorage.setItem('habitiq-mic-perm', 'asked')
    onAllow()
  }

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          <motion.div
            key="mic-backdrop"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-[210] bg-black/60 backdrop-blur-sm"
            onClick={onDismiss}
          />
          <motion.div
            key="mic-sheet"
            initial={{ y: '100%' }}
            animate={{ y: 0 }}
            exit={{ y: '100%' }}
            transition={{ type: 'spring', stiffness: 380, damping: 32 }}
            className="fixed bottom-0 left-0 right-0 z-[211] rounded-t-3xl pb-safe"
            style={{ background: 'var(--card)', borderTop: '1px solid var(--border)' }}
          >
            <div className="flex justify-center pt-3 pb-1">
              <div className="w-10 h-1 rounded-full bg-border" />
            </div>

            <div className="px-6 pt-4 pb-8 flex flex-col items-center text-center gap-5 relative">
              <button
                onClick={onDismiss}
                className="absolute top-0 right-0 p-2 rounded-xl hover:bg-secondary transition-colors cursor-pointer"
              >
                <X size={16} className="text-muted-foreground" />
              </button>

              {/* Pulsing mic icon */}
              <div className="relative flex items-center justify-center mt-2">
                <motion.div
                  className="absolute rounded-full"
                  style={{ inset: -14, background: 'rgba(124,58,237,0.12)', border: '1px solid rgba(124,58,237,0.25)' }}
                  animate={{ scale: [1, 1.18, 1], opacity: [0.7, 0.2, 0.7] }}
                  transition={{ duration: 2.2, repeat: Infinity, ease: 'easeInOut' }}
                />
                <div
                  className="w-16 h-16 rounded-full flex items-center justify-center"
                  style={{ background: 'linear-gradient(135deg, #7c3aed, #4f46e5)' }}
                >
                  <Mic size={28} className="text-white" />
                </div>
              </div>

              <div>
                <p className="text-base font-bold text-foreground">Use your voice</p>
                <p className="text-sm text-muted-foreground mt-1.5 leading-relaxed max-w-xs">
                  Say things like <span className="text-foreground/70 font-medium">"Add ₹500 for groceries"</span>,{' '}
                  <span className="text-foreground/70 font-medium">"Kitchen done"</span>, or{' '}
                  <span className="text-foreground/70 font-medium">"What are my tasks?"</span>
                </p>
              </div>

              <div className="w-full flex flex-col gap-2.5">
                <button
                  onClick={handleAllow}
                  className="w-full py-3.5 rounded-2xl text-sm font-semibold text-white transition-all cursor-pointer"
                  style={{ background: 'linear-gradient(135deg, #7c3aed, #4f46e5)' }}
                >
                  Continue
                </button>
                <button
                  onClick={onDismiss}
                  className="w-full py-3 rounded-2xl text-sm font-medium text-muted-foreground hover:text-foreground transition-colors cursor-pointer"
                >
                  Not now
                </button>
              </div>

              <p className="text-[11px] text-muted-foreground/50 leading-relaxed">
                Your browser will ask for microphone permission once.
              </p>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  )
}
