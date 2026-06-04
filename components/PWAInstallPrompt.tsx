"use client"
import { useState, useEffect } from 'react'
import { Download, X } from 'lucide-react'
import { Button } from '@/components/ui/button'

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>
}

export default function PWAInstallPrompt() {
  const [prompt, setPrompt] = useState<BeforeInstallPromptEvent | null>(null)
  const [dismissed, setDismissed] = useState(false)

  useEffect(() => {
    try {
      if (localStorage.getItem('habitiq-pwa-dismissed') === 'true') {
        setDismissed(true)
        return
      }
    } catch { /* ignore */ }

    const handler = (e: Event) => {
      e.preventDefault()
      setPrompt(e as BeforeInstallPromptEvent)
    }
    window.addEventListener('beforeinstallprompt', handler)
    return () => window.removeEventListener('beforeinstallprompt', handler)
  }, [])

  const handleInstall = async () => {
    if (!prompt) return
    await prompt.prompt()
    const { outcome } = await prompt.userChoice
    if (outcome === 'accepted') setPrompt(null)
  }

  const handleDismiss = () => {
    try { localStorage.setItem('habitiq-pwa-dismissed', 'true') } catch { /* ignore */ }
    setDismissed(true)
    setPrompt(null)
  }

  if (!prompt || dismissed) return null

  return (
    <div className="fixed bottom-20 md:bottom-6 left-4 right-4 md:left-auto md:right-6 md:w-80 z-40 bg-card border border-border rounded-2xl shadow-xl p-4 flex items-start gap-3 animate-in slide-in-from-bottom-4 duration-300">
      <div className="w-10 h-10 rounded-xl bg-primary flex items-center justify-center shrink-0 shadow-sm">
        <span className="text-white font-extrabold text-base">H</span>
      </div>
      <div className="flex-1 min-w-0">
        <p className="font-bold text-sm">Install Habitiq</p>
        <p className="text-xs text-muted-foreground mt-0.5 leading-relaxed">
          Add to home screen for a full app experience — no browser bar.
        </p>
        <Button size="sm" className="mt-2.5 h-8 text-xs gap-1.5 w-full" onClick={handleInstall}>
          <Download size={12} />
          Install App
        </Button>
      </div>
      <button
        onClick={handleDismiss}
        className="text-muted-foreground hover:text-foreground shrink-0 mt-0.5 p-0.5 rounded-md hover:bg-secondary transition-colors"
        aria-label="Dismiss"
      >
        <X size={15} />
      </button>
    </div>
  )
}
