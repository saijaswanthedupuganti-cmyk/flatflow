"use client"
import { useState, useEffect } from 'react'
import { Download, X, Share } from 'lucide-react'
import { usePWA } from '@/contexts/PWAContext'

const NEXT_PROMPT_KEY = 'habitiq_pwa_next_prompt'

export default function PWAInstallPrompt() {
  const { canInstall, isInstalled, isIOS, triggerInstall } = usePWA()
  const [show, setShow] = useState(false)
  const [showIOSSteps, setShowIOSSteps] = useState(false)

  useEffect(() => {
    if (isInstalled) return
    if (!canInstall && !isIOS) return
    try {
      const next = localStorage.getItem(NEXT_PROMPT_KEY)
      if (!next || Date.now() >= parseInt(next)) {
        const t = setTimeout(() => setShow(true), 1500)
        return () => clearTimeout(t)
      }
    } catch { /* ignore */ }
  }, [canInstall, isInstalled, isIOS])

  const snooze = () => {
    setShow(false)
    setShowIOSSteps(false)
    try {
      const ms = (1 + Math.random()) * 24 * 60 * 60 * 1000
      localStorage.setItem(NEXT_PROMPT_KEY, String(Math.round(Date.now() + ms)))
    } catch { /* ignore */ }
  }

  const handleInstall = async () => {
    if (isIOS) { setShowIOSSteps(true); return }
    const ok = await triggerInstall()
    if (!ok) snooze()
  }

  if (!show || isInstalled) return null

  return (
    <div className="fixed bottom-20 md:bottom-6 left-4 right-4 md:left-auto md:right-6 md:w-80 z-[90] animate-in slide-in-from-bottom-4 duration-300">
      <div className="bg-card border border-border rounded-2xl shadow-2xl overflow-hidden">
        {!showIOSSteps ? (
          <>
            <div className="flex items-start gap-3 p-4">
              <div className="w-10 h-10 rounded-xl bg-primary flex items-center justify-center shrink-0 shadow-sm">
                <span className="text-white font-extrabold text-base">H</span>
              </div>
              <div className="flex-1 min-w-0">
                <p className="font-bold text-sm leading-tight">Install Habitiq</p>
                <p className="text-xs text-muted-foreground mt-0.5 leading-relaxed">
                  Add to home screen for a full app experience — works offline, instant access.
                </p>
              </div>
              <button
                onClick={snooze}
                className="text-muted-foreground hover:text-foreground shrink-0 mt-0.5 p-0.5 rounded-md hover:bg-secondary transition-colors"
                aria-label="Dismiss"
              >
                <X size={15} />
              </button>
            </div>
            <div className="flex border-t border-border/60">
              <button
                onClick={snooze}
                className="flex-1 py-2.5 text-xs font-semibold text-muted-foreground hover:bg-secondary/50 transition-colors cursor-pointer"
              >
                Later
              </button>
              <button
                onClick={handleInstall}
                className="flex-1 py-2.5 text-xs font-bold text-primary hover:bg-primary/5 transition-colors border-l border-border/60 flex items-center justify-center gap-1.5 cursor-pointer"
              >
                <Download size={12} /> Install
              </button>
            </div>
          </>
        ) : (
          <>
            <div className="flex items-center justify-between px-4 pt-4 pb-2">
              <p className="text-sm font-bold">Add to Home Screen</p>
              <button
                onClick={snooze}
                className="text-muted-foreground hover:text-foreground p-0.5 rounded hover:bg-secondary transition-colors"
              >
                <X size={15} />
              </button>
            </div>
            <ol className="px-4 pb-2 space-y-3 list-none">
              <li className="flex items-start gap-2.5">
                <span className="w-5 h-5 rounded-full bg-primary text-white text-[10px] font-extrabold flex items-center justify-center shrink-0 mt-0.5">1</span>
                <p className="text-xs text-muted-foreground leading-relaxed">
                  Tap the <Share size={11} className="inline mx-0.5 -mt-0.5" />{' '}
                  <strong>Share</strong> button at the bottom of Safari
                </p>
              </li>
              <li className="flex items-start gap-2.5">
                <span className="w-5 h-5 rounded-full bg-primary text-white text-[10px] font-extrabold flex items-center justify-center shrink-0 mt-0.5">2</span>
                <p className="text-xs text-muted-foreground leading-relaxed">
                  Scroll down and tap <strong>&ldquo;Add to Home Screen&rdquo;</strong>
                </p>
              </li>
              <li className="flex items-start gap-2.5">
                <span className="w-5 h-5 rounded-full bg-primary text-white text-[10px] font-extrabold flex items-center justify-center shrink-0 mt-0.5">3</span>
                <p className="text-xs text-muted-foreground leading-relaxed">
                  Tap <strong>&ldquo;Add&rdquo;</strong> in the top-right corner
                </p>
              </li>
            </ol>
            <div className="px-4 pb-4">
              <button
                onClick={snooze}
                className="w-full py-2.5 text-xs font-bold bg-primary text-primary-foreground rounded-xl hover:opacity-90 transition-opacity cursor-pointer"
              >
                Done
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
