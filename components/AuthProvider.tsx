"use client"
import { useEffect } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { useAuthStore } from '@/store/useAuthStore'

export default function AuthProvider({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const pathname = usePathname()
  const initAuthListener = useAuthStore((s) => s.initAuthListener)
  const isLoading = useAuthStore((s) => s.isLoading)
  const user = useAuthStore((s) => s.user)
  const flatId = useAuthStore((s) => s.flatId)
  const flatChecked = useAuthStore((s) => s.flatChecked)

  useEffect(() => {
    initAuthListener()
  }, [initAuthListener])

  // Once auth is resolved, handle routing
  useEffect(() => {
    if (isLoading) return
    if (!flatChecked) return

    const isAuthPage = pathname === '/'
    const isOnboarding = pathname === '/onboarding'
    const isDashboard = pathname.startsWith('/dashboard')

    if (!user) {
      // Not logged in — send to login page
      if (!isAuthPage) router.push('/')
    } else if (!flatId) {
      // Logged in but not in a flat yet — send to onboarding
      if (!isOnboarding) router.push('/onboarding')
    } else {
      // Logged in and in a flat — redirect away from login page only.
      // Do NOT redirect from /onboarding — the user may be adding a second flat.
      if (isAuthPage) router.push('/dashboard')
    }
  }, [isLoading, user, flatId, flatChecked, pathname, router])

  // Show loading if: auth not resolved yet, OR user is logged in but we haven't checked their flat yet
  const stillChecking = isLoading || (!!user && !flatChecked)

  if (stillChecking) {
    return (
      <div className="h-screen w-full flex items-center justify-center bg-background text-foreground">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 bg-primary rounded-xl flex items-center justify-center animate-pulse">
            <span className="text-lg font-extrabold text-white">F</span>
          </div>
          <p className="text-muted-foreground text-sm font-medium">Loading FlatFlow…</p>
        </div>
      </div>
    )
  }

  return <>{children}</>
}
