"use client"
import { useEffect } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { useAuthStore } from '@/store/useAuthStore'

/* ─── Animated Loading Screen ───────────────────────────────────────────────
   Logo-inspired: interlocking panels slide together (echoing the app icon),
   then three violet dots orbit the assembled mark.
──────────────────────────────────────────────────────────────────────────── */
const LOADING_CSS = `
  @keyframes hiq-reveal {
    0%   { opacity: 0; transform: scale(0.55) rotate(-6deg); }
    65%  { opacity: 1; transform: scale(1.06) rotate(1.5deg); }
    100% { opacity: 1; transform: scale(1) rotate(0deg); }
  }
  @keyframes hiq-panel-tl {
    0%   { opacity: 0; transform: translate(-28px,-18px) rotate(-10deg); }
    70%  { opacity: 0.8; transform: translate(3px,2px) rotate(1deg); }
    100% { opacity: 1; transform: translate(0,0) rotate(0deg); }
  }
  @keyframes hiq-panel-br {
    0%   { opacity: 0; transform: translate(28px,18px) rotate(10deg); }
    70%  { opacity: 0.8; transform: translate(-3px,-2px) rotate(-1deg); }
    100% { opacity: 1; transform: translate(0,0) rotate(0deg); }
  }
  @keyframes hiq-panel-tr {
    0%   { opacity: 0; transform: translate(20px,-22px) rotate(8deg); }
    70%  { opacity: 0.7; transform: translate(-2px,2px) rotate(-1deg); }
    100% { opacity: 1; transform: translate(0,0) rotate(0deg); }
  }
  @keyframes hiq-ring {
    0%, 100% { opacity: 0.14; transform: scale(1); }
    50%       { opacity: 0.32; transform: scale(1.03); }
  }
  @keyframes hiq-glow {
    0%, 100% { opacity: 0.45; }
    50%       { opacity: 0.85; }
  }
  @keyframes hiq-orbit {
    from { transform: rotate(0deg) translateX(76px) rotate(0deg); }
    to   { transform: rotate(360deg) translateX(76px) rotate(-360deg); }
  }
  @keyframes hiq-shimmer {
    0%   { transform: translateX(-120%) skewX(-18deg); }
    100% { transform: translateX(220%) skewX(-18deg); }
  }
  @keyframes hiq-fade-up {
    from { opacity: 0; transform: translateY(10px); }
    to   { opacity: 1; transform: translateY(0); }
  }
`

function HabitiqLoadingScreen() {
  return (
    <div style={{ height:'100vh', width:'100%', display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', gap:'32px', background:'#08080C' }}>
      <style dangerouslySetInnerHTML={{ __html: LOADING_CSS }} />

      {/* ── Animation Stage (190 × 190) ── */}
      <div style={{ position:'relative', width:190, height:190 }}>

        {/* Ambient glow blob */}
        <div style={{
          position:'absolute', inset:0,
          borderRadius:'50%',
          background:'radial-gradient(circle, rgba(124,58,237,0.32) 0%, transparent 68%)',
          filter:'blur(24px)',
          animation:'hiq-glow 2.6s ease-in-out infinite',
        }} />

        {/* Orbit ring */}
        <div style={{
          position:'absolute', inset:22,
          borderRadius:'50%',
          border:'1.5px solid rgba(124,58,237,0.2)',
          animation:'hiq-ring 2.6s ease-in-out infinite',
        }} />

        {/* ── Interlocking background panels (logo motif) ── */}
        {/* Top-left panel */}
        <div style={{
          position:'absolute', top:'18%', left:'10%',
          width:'44%', height:'40%',
          borderRadius:14,
          background:'linear-gradient(135deg,rgba(167,139,250,0.14),rgba(124,58,237,0.08))',
          border:'1px solid rgba(167,139,250,0.22)',
          animation:'hiq-panel-tl 0.9s cubic-bezier(0.34,1.56,0.64,1) 0.05s both',
        }} />
        {/* Bottom-right panel */}
        <div style={{
          position:'absolute', bottom:'16%', right:'10%',
          width:'44%', height:'40%',
          borderRadius:14,
          background:'linear-gradient(135deg,rgba(124,58,237,0.12),rgba(79,70,229,0.18))',
          border:'1px solid rgba(124,58,237,0.22)',
          animation:'hiq-panel-br 0.9s cubic-bezier(0.34,1.56,0.64,1) 0.1s both',
        }} />
        {/* Top-right panel (middle layer) */}
        <div style={{
          position:'absolute', top:'22%', right:'14%',
          width:'32%', height:'28%',
          borderRadius:10,
          background:'rgba(167,139,250,0.07)',
          border:'1px solid rgba(167,139,250,0.14)',
          animation:'hiq-panel-tr 0.9s cubic-bezier(0.34,1.56,0.64,1) 0.15s both',
        }} />

        {/* ── App icon — centre, appears last ── */}
        <div style={{
          position:'absolute', top:'50%', left:'50%',
          transform:'translate(-50%,-50%)',
          animation:'hiq-reveal 0.95s cubic-bezier(0.34,1.56,0.64,1) 0.28s both',
        }}>
          <div style={{ position:'relative', overflow:'hidden', borderRadius:20, boxShadow:'0 10px 48px rgba(109,40,217,0.55), 0 2px 10px rgba(0,0,0,0.25)' }}>
            <img
              src="/habitiq-app-icon.png"
              alt="Habitiq"
              style={{ width:76, height:76, objectFit:'cover', display:'block' }}
            />
            {/* One-shot shimmer sweep */}
            <div style={{
              position:'absolute', inset:0,
              background:'linear-gradient(90deg,transparent,rgba(255,255,255,0.22),transparent)',
              animation:'hiq-shimmer 1.2s ease-out 0.7s both',
              pointerEvents:'none',
            }} />
          </div>
        </div>

        {/* ── Three orbiting dots (120° apart) ── */}
        {([
          { delay:'0s',       color:'#8B5CF6' },
          { delay:'-0.733s',  color:'#7C3AED' },
          { delay:'-1.467s',  color:'#A78BFA' },
        ] as const).map(({ delay, color }, i) => (
          <div key={i} style={{
            position:'absolute',
            top:'calc(50% - 5px)', left:'calc(50% - 5px)',
            animation:'hiq-orbit 2.2s linear infinite',
            animationDelay: delay,
          }}>
            <div style={{ width:10, height:10, borderRadius:'50%', background:color, boxShadow:`0 0 12px ${color}BB` }} />
          </div>
        ))}
      </div>

      {/* ── Brand + tagline ── */}
      <div style={{ textAlign:'center', animation:'hiq-fade-up 0.55s ease-out 0.85s both' }}>
        <p style={{ fontSize:'17px', fontWeight:'700', color:'white', letterSpacing:'-0.02em', marginBottom:'5px' }}>Habitiq</p>
        <p style={{ fontSize:'13px', color:'rgba(255,255,255,0.32)', fontWeight:'500' }}>Getting your flat ready…</p>
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

  if (stillChecking) {
    return <HabitiqLoadingScreen />
  }

  return <>{children}</>
}
