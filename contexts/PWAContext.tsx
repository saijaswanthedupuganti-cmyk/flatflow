"use client"
import { createContext, useContext, useEffect, useState, useRef } from 'react'

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>
}

interface PWAContextType {
  canInstall: boolean
  isInstalled: boolean
  isIOS: boolean
  triggerInstall: () => Promise<boolean>
}

const PWAContext = createContext<PWAContextType>({
  canInstall: false,
  isInstalled: false,
  isIOS: false,
  triggerInstall: async () => false,
})

export function PWAProvider({ children }: { children: React.ReactNode }) {
  const [canInstall, setCanInstall] = useState(false)
  const [isInstalled, setIsInstalled] = useState(false)
  const [isIOS, setIsIOS] = useState(false)
  const promptRef = useRef<BeforeInstallPromptEvent | null>(null)

  useEffect(() => {
    const standalone =
      window.matchMedia('(display-mode: standalone)').matches ||
      (navigator as unknown as { standalone?: boolean }).standalone === true
    setIsInstalled(standalone)
    if (standalone) return

    const ios =
      /iPad|iPhone|iPod/.test(navigator.userAgent) &&
      !(window as unknown as Record<string, unknown>)['MSStream']
    setIsIOS(!!ios)

    const onPrompt = (e: Event) => {
      e.preventDefault()
      promptRef.current = e as BeforeInstallPromptEvent
      setCanInstall(true)
    }
    const onInstalled = () => {
      setIsInstalled(true)
      setCanInstall(false)
      promptRef.current = null
    }

    window.addEventListener('beforeinstallprompt', onPrompt)
    window.addEventListener('appinstalled', onInstalled)
    return () => {
      window.removeEventListener('beforeinstallprompt', onPrompt)
      window.removeEventListener('appinstalled', onInstalled)
    }
  }, [])

  async function triggerInstall(): Promise<boolean> {
    if (!promptRef.current) return false
    await promptRef.current.prompt()
    const { outcome } = await promptRef.current.userChoice
    if (outcome === 'accepted') {
      setIsInstalled(true)
      setCanInstall(false)
      promptRef.current = null
      return true
    }
    return false
  }

  return (
    <PWAContext.Provider value={{ canInstall, isInstalled, isIOS, triggerInstall }}>
      {children}
    </PWAContext.Provider>
  )
}

export function usePWA() {
  return useContext(PWAContext)
}
