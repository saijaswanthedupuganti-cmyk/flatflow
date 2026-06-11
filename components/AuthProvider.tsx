"use client"
import { useEffect } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { useAuthStore } from '@/store/useAuthStore'

/* ─── Animated Loading Screen ───────────────────────────────────────────────
   Three dots orbit the logo (rotation = duty-rotation theme).
   All animations are CSS @keyframes injected via a <style> tag so we don't
   need to touch tailwind.config — zero extra dependencies.
──────────────────────────────────────────────────────────────────────────── */
const LOADING_CSS = `
  @keyframes ff-logo-reveal {
    from { opacity: 0; transform: scale(0.72); }
    to   { opacity: 1; transform: scale(1); }
  }
  @keyframes ff-orbit {
    from { transform: rotate(0deg)   translateX(60px) rotate(0deg); }
    to   { transform: rotate(360deg) translateX(60px) rotate(-360deg); }
  }
  @keyframes ff-fade-up {
    from { opacity: 0; transform: translateY(10px); }
    to   { opacity: 1; transform: translateY(0); }
  }
  @keyframes ff-ring-pulse {
    0%, 100% { opacity: 0.18; }
    50%       { opacity: 0.40; }
  }
  @keyframes ff-dot-trail {
    0%, 100% { opacity: 0.35; }
    50%       { opacity: 1; }
  }
`

function HabitiqLoadingScreen() {
  return (
    <div className="h-screen w-full flex flex-col items-center justify-center gap-6 bg-background text-foreground">
      <style dangerouslySetInnerHTML={{ __html: LOADING_CSS }} />

      {/* ── Orbit Stage (140 × 140) ── */}
      <div style={{ position: 'relative', width: 140, height: 140 }}>

        {/* Faint orbit ring */}
        <div style={{
          position: 'absolute',
          inset: 10,                        /* 120×120 → radius 60 */
          borderRadius: '50%',
          border: '1.5px solid rgba(109,40,217,0.25)',
          animation: 'ff-ring-pulse 2.4s ease-in-out infinite',
        }} />

        {/* Icon mark — centred in the orbit stage */}
        <div style={{
          position: 'absolute',
          top: '50%', left: '50%',
          transform: 'translate(-50%, -50%)',
          animation: 'ff-logo-reveal 0.75s cubic-bezier(0.34,1.56,0.64,1) both',
        }}>
          <div style={{
            width: 68, height: 68,
            borderRadius: 18,
            background: 'linear-gradient(135deg, #7c3aed, #4338ca)',
            boxShadow: '0 8px 32px rgba(109,40,217,0.42), 0 2px 8px rgba(0,0,0,0.12)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <img
              src="/habitiq-icon-mark.png"
              alt=""
              style={{ width: 44, height: 44, objectFit: 'contain' }}
            />
          </div>
        </div>

        {/* ── Three orbiting dots (evenly spaced at 120° offsets) ── */}
        {([
          { delay: '0s',      color: '#7c3aed' },   /* violet-600  — 0°   */
          { delay: '-0.667s', color: '#6d28d9' },   /* violet-700  — 120° */
          { delay: '-1.333s', color: '#8b5cf6' },   /* violet-500  — 240° */
        ] as const).map(({ delay, color }, i) => (
          <div
            key={i}
            style={{
              position: 'absolute',
              top:  'calc(50% - 4.5px)',
              left: 'calc(50% - 4.5px)',
              animation: 'ff-orbit 2s linear infinite',
              animationDelay: delay,
            }}
          >
            <div style={{
              width: 9, height: 9,
              borderRadius: '50%',
              background: color,
              boxShadow: `0 0 8px ${color}99`,
              animation: 'ff-dot-trail 2s linear infinite',
              animationDelay: delay,
            }} />
          </div>
        ))}
      </div>

      {/* ── Tagline ── */}
      <div style={{ animation: 'ff-fade-up 0.6s ease-out 0.80s both' }}>
        <p className="text-sm font-medium text-muted-foreground">
          Getting your flat ready…
        </p>
      </div>
    </div>
  )
}

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
    return <HabitiqLoadingScreen />
  }

  return <>{children}</>
}
