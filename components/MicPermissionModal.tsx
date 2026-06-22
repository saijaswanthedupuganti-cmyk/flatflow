"use client"
import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Mic, X } from 'lucide-react'
import { requestMicPermission } from '@/lib/voice/permissions'

interface Props {
  isOpen: boolean
  onGranted: () => void
  onDismiss: () => void
}

export default function MicPermissionModal({ isOpen, onGranted, onDismiss }: Props) {
  const [asking, setAsking] = useState(false)
  const [denied, setDenied] = useState(false)

  const handleAllow = async () => {
    setAsking(true)
    const result = await requestMicPermission()
    setAsking(false)
    if (result === 'granted') {
      localStorage.setItem('habitiq-mic-perm', 'granted')
      onGranted()
    } else {
      setDenied(true)
    }
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

            <div className="px-6 pt-4 pb-8 flex flex-col items-center text-center gap-5">
              {/* Close */}
              <button
                onClick={onDismiss}
                className="absolute top-5 right-5 p-2 rounded-xl hover:bg-secondary transition-colors cursor-pointer"
              >
                <X size={16} className="text-muted-foreground" />
              </button>

              {/* Mic icon with pulse ring */}
              <div className="relative flex items-center justify-center mt-2">
                <motion.div
                  className="absolute rounded-full"
                  style={{ inset: -14, background: 'rgba(124,58,237,0.12)', border: '1px solid rgba(124,58,237,0.25)' }}
                  animate={{ scale: [1, 1.15, 1], opacity: [0.6, 0.2, 0.6] }}
                  transition={{ duration: 2.2, repeat: Infinity, ease: 'easeInOut' }}
                />
                <div
                  className="w-16 h-16 rounded-full flex items-center justify-center"
                  style={{ background: 'linear-gradient(135deg, #7c3aed, #4f46e5)' }}
                >
                  <Mic size={28} className="text-white" />
                </div>
              </div>

              {denied ? (
                <>
                  <div>
                    <p className="text-base font-bold text-foreground">Microphone blocked</p>
                    <p className="text-sm text-muted-foreground mt-1 leading-relaxed">
                      Enable microphone access in your browser settings, then try again.
                    </p>
                  </div>
                  <button
                    onClick={onDismiss}
                    className="w-full py-3.5 rounded-2xl text-sm font-semibold text-foreground transition-colors cursor-pointer"
                    style={{ background: 'var(--secondary)' }}
                  >
                    Got it
                  </button>
                </>
              ) : (
                <>
                  <div>
                    <p className="text-base font-bold text-foreground">Allow microphone access</p>
                    <p className="text-sm text-muted-foreground mt-1.5 leading-relaxed max-w-xs">
                      Habitiq uses your mic to hear commands like "Add ₹500 for groceries" or "Kitchen done" — hands-free.
                    </p>
                  </div>

                  <div className="w-full flex flex-col gap-2.5">
                    <button
                      onClick={handleAllow}
                      disabled={asking}
                      className="w-full py-3.5 rounded-2xl text-sm font-semibold text-white transition-all cursor-pointer disabled:opacity-60"
                      style={{ background: 'linear-gradient(135deg, #7c3aed, #4f46e5)' }}
                    >
                      {asking ? 'Requesting access…' : 'Allow microphone'}
                    </button>
                    <button
                      onClick={onDismiss}
                      className="w-full py-3 rounded-2xl text-sm font-medium text-muted-foreground transition-colors hover:text-foreground cursor-pointer"
                    >
                      Not now
                    </button>
                  </div>

                  <p className="text-[11px] text-muted-foreground/60 leading-relaxed max-w-xs">
                    Your voice is processed on-device. Nothing is stored or sent to any server.
                  </p>
                </>
              )}
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  )
}
