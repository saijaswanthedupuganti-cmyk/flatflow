"use client"
import { motion, AnimatePresence } from 'framer-motion'
import { Mic, MicOff, Wifi, X, RefreshCw } from 'lucide-react'

export type MicModalMode = 'first-use' | 'blocked' | 'network'

interface Props {
  isOpen: boolean
  mode?: MicModalMode
  onAllow: () => void   // called synchronously — caller starts recognition immediately
  onDismiss: () => void
  onTypeInstead?: () => void
}

export default function MicPermissionModal({ isOpen, mode = 'first-use', onAllow, onDismiss, onTypeInstead }: Props) {
  const handleAllow = () => {
    if (mode === 'first-use') localStorage.setItem('habitiq-mic-perm', 'asked')
    onAllow()
  }

  const isBlocked = mode === 'blocked'
  const isNetwork = mode === 'network'

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

              {/* Icon */}
              <div className="relative flex items-center justify-center mt-2">
                <motion.div
                  className="absolute rounded-full"
                  style={{
                    inset: -14,
                    background: isBlocked
                      ? 'rgba(239,68,68,0.1)'
                      : isNetwork
                      ? 'rgba(245,158,11,0.1)'
                      : 'rgba(124,58,237,0.12)',
                    border: isBlocked
                      ? '1px solid rgba(239,68,68,0.25)'
                      : isNetwork
                      ? '1px solid rgba(245,158,11,0.25)'
                      : '1px solid rgba(124,58,237,0.25)',
                  }}
                  animate={{ scale: [1, 1.18, 1], opacity: [0.7, 0.2, 0.7] }}
                  transition={{ duration: 2.2, repeat: Infinity, ease: 'easeInOut' }}
                />
                <div
                  className="w-16 h-16 rounded-full flex items-center justify-center"
                  style={{
                    background: isBlocked
                      ? 'linear-gradient(135deg, #ef4444, #dc2626)'
                      : isNetwork
                      ? 'linear-gradient(135deg, #f59e0b, #d97706)'
                      : 'linear-gradient(135deg, #7c3aed, #4f46e5)',
                  }}
                >
                  {isBlocked ? (
                    <MicOff size={28} className="text-white" />
                  ) : isNetwork ? (
                    <Wifi size={28} className="text-white" />
                  ) : (
                    <Mic size={28} className="text-white" />
                  )}
                </div>
              </div>

              {/* Content */}
              {mode === 'first-use' && (
                <div>
                  <p className="text-base font-bold text-foreground">Use your voice</p>
                  <p className="text-sm text-muted-foreground mt-1.5 leading-relaxed max-w-xs">
                    Say things like <span className="text-foreground/70 font-medium">"Add ₹500 for groceries"</span>,{' '}
                    <span className="text-foreground/70 font-medium">"Kitchen done"</span>, or{' '}
                    <span className="text-foreground/70 font-medium">"What are my tasks?"</span>
                  </p>
                </div>
              )}

              {mode === 'blocked' && (
                <div className="w-full text-left space-y-4">
                  <div className="text-center">
                    <p className="text-base font-bold text-foreground">Mic blocked for this site</p>
                    <p className="text-sm text-muted-foreground mt-1 leading-relaxed">
                      Chrome blocked mic access for Habitiq. This is separate from your Android mic permission — here&apos;s how to fix it:
                    </p>
                  </div>

                  {/* Step-by-step */}
                  <div
                    className="rounded-2xl p-4 space-y-3"
                    style={{ background: 'var(--secondary)', border: '1px solid var(--border)' }}
                  >
                    {[
                      { n: '1', text: 'Tap the 🔒 lock icon in Chrome\'s address bar' },
                      { n: '2', text: 'Tap "Permissions" or "Site settings"' },
                      { n: '3', text: 'Tap "Microphone"' },
                      { n: '4', text: 'Change from "Block" to "Allow"' },
                      { n: '5', text: 'Come back and tap the mic again' },
                    ].map(({ n, text }) => (
                      <div key={n} className="flex items-start gap-3">
                        <span
                          className="w-6 h-6 rounded-full flex items-center justify-center text-[11px] font-extrabold shrink-0 mt-0.5"
                          style={{ background: 'rgba(124,58,237,0.15)', color: '#a78bfa' }}
                        >
                          {n}
                        </span>
                        <p className="text-sm text-foreground/80 leading-snug">{text}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {mode === 'network' && (
                <div className="text-center space-y-2">
                  <p className="text-base font-bold text-foreground">Speech recognition needs internet</p>
                  <p className="text-sm text-muted-foreground leading-relaxed max-w-xs">
                    Android Chrome sends your voice to Google to convert it to text. Check your connection, disable any VPN, and try again.
                  </p>
                </div>
              )}

              {/* CTAs */}
              <div className="w-full flex flex-col gap-2.5">
                {isBlocked ? (
                  <>
                    <button
                      onClick={handleAllow}
                      className="w-full py-3.5 rounded-2xl text-sm font-semibold text-white flex items-center justify-center gap-2 transition-all cursor-pointer"
                      style={{ background: 'linear-gradient(135deg, #7c3aed, #4f46e5)' }}
                    >
                      <RefreshCw size={15} />
                      I&apos;ve allowed it — try again
                    </button>
                    {onTypeInstead && (
                      <button
                        onClick={onTypeInstead}
                        className="w-full py-3 rounded-2xl text-sm font-medium text-muted-foreground hover:text-foreground transition-colors cursor-pointer"
                      >
                        Type a command instead
                      </button>
                    )}
                  </>
                ) : isNetwork ? (
                  <>
                    <button
                      onClick={handleAllow}
                      className="w-full py-3.5 rounded-2xl text-sm font-semibold text-white flex items-center justify-center gap-2 transition-all cursor-pointer"
                      style={{ background: 'linear-gradient(135deg, #7c3aed, #4f46e5)' }}
                    >
                      <RefreshCw size={15} />
                      Try again
                    </button>
                    {onTypeInstead && (
                      <button
                        onClick={onTypeInstead}
                        className="w-full py-3 rounded-2xl text-sm font-medium text-muted-foreground hover:text-foreground transition-colors cursor-pointer"
                      >
                        Type a command instead
                      </button>
                    )}
                  </>
                ) : (
                  <>
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
                  </>
                )}
              </div>

              {mode === 'first-use' && (
                <p className="text-[11px] text-muted-foreground/50 leading-relaxed">
                  Your browser will ask for microphone permission once.
                </p>
              )}
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  )
}
