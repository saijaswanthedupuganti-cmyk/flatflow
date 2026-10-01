"use client"
import { useEffect } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { useAuthStore } from '@/store/useAuthStore'

import HabitiqLoadingScreen from '@/components/HabitiqLoadingScreen'

/* ─── Auth Provider ──────────────────────────────────────────────────────── */
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
    const isPublicPage = pathname === '/privacy' || pathname === '/terms'

    if (!user) {
      // Not logged in — send to login page (public pages bypass this)
      if (!isAuthPage && !isPublicPage) router.push('/')
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
  const isPublicShell = pathname === '/' || pathname === '/privacy' || pathname === '/terms'

  // Public pages must remain useful when Firebase is slow or unavailable.
  // Authenticated visitors are still redirected as soon as the listener resolves.
  if (stillChecking && !isPublicShell) {
    return <HabitiqLoadingScreen />
  }

  return <>{children}</>
}
