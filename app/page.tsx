'use client'

import dynamic from 'next/dynamic'
import { useEffect, useRef, useState, useCallback, ReactNode } from 'react'
import { useRouter } from 'next/navigation'
import { Plus_Jakarta_Sans } from 'next/font/google'
import { motion, useMotionValue, useSpring, useTransform } from 'framer-motion'
import { ContainerScroll } from '@/components/ui/container-scroll-animation'
import { useAuthStore } from '@/store/useAuthStore'
import { hasKeys } from '@/lib/firebase'

const HeroCanvas = dynamic(() => import('@/components/HeroCanvas'), {
  ssr: false,
  loading: () => <div className="w-full h-full bg-[#0a0a0a]" />,
})

const jakarta = Plus_Jakarta_Sans({
  subsets: ['latin'],
  weight: ['400', '500', '600', '700', '800'],
  display: 'swap',
})

// ── Scroll reveal ─────────────────────────────────────────────────────────────
function useReveal() {
  const ref = useRef<HTMLDivElement>(null)
  const [v, setV] = useState(false)
  useEffect(() => {
    const el = ref.current; if (!el) return
    const obs = new IntersectionObserver(([e]) => { if (e.isIntersecting) { setV(true); obs.disconnect() } }, { threshold: 0.1 })
    obs.observe(el)
    return () => obs.disconnect()
  }, [])
  return { ref, v }
}

function Reveal({ children, delay = 0, className = '' }: { children: ReactNode; delay?: number; className?: string }) {
  const { ref, v } = useReveal()
  return (
    <div ref={ref} className={className} style={{
      opacity: v ? 1 : 0,
      transform: v ? 'translateY(0)' : 'translateY(32px)',
      transition: `opacity .75s cubic-bezier(.22,1,.36,1) ${delay}ms, transform .75s cubic-bezier(.22,1,.36,1) ${delay}ms`,
    }}>{children}</div>
  )
}

// ── 3D tilt card (Framer Motion) ──────────────────────────────────────────────
function Card3D({ children, className = '', intensity = 12, glowColor = 'rgba(124,58,237,0.2)' }: {
  children: ReactNode; className?: string; intensity?: number; glowColor?: string
}) {
  const mx = useMotionValue(0)
  const my = useMotionValue(0)
  const rx = useSpring(useTransform(my, [-0.5, 0.5], [intensity, -intensity]), { stiffness: 350, damping: 28 })
  const ry = useSpring(useTransform(mx, [-0.5, 0.5], [-intensity, intensity]), { stiffness: 350, damping: 28 })
  const glow = useTransform([mx, my], ([x, y]: number[]) =>
    `radial-gradient(circle at ${(x + 0.5) * 100}% ${(y + 0.5) * 100}%, ${glowColor} 0%, transparent 68%)`
  )
  const shine = useTransform([mx, my], ([x, y]: number[]) =>
    `radial-gradient(circle at ${(x + 0.5) * 100}% ${(y + 0.5) * 100}%, rgba(255,255,255,0.06) 0%, transparent 55%)`
  )

  function onMove(e: React.MouseEvent<HTMLDivElement>) {
    const r = e.currentTarget.getBoundingClientRect()
    mx.set((e.clientX - r.left) / r.width - 0.5)
    my.set((e.clientY - r.top) / r.height - 0.5)
  }
  function onLeave() { mx.set(0); my.set(0) }

  return (
    <motion.div
      onMouseMove={onMove} onMouseLeave={onLeave}
      style={{ rotateX: rx, rotateY: ry, transformStyle: 'preserve-3d', perspective: 1000 }}
      className={`relative ${className}`}
    >
      {/* Glow layer */}
      <motion.div className="absolute inset-0 rounded-2xl pointer-events-none z-10" style={{ background: glow }} />
      {/* Shine layer */}
      <motion.div className="absolute inset-0 rounded-2xl pointer-events-none z-10" style={{ background: shine }} />
      {/* Content lifted in Z */}
      <div style={{ transform: 'translateZ(10px)' }} className="relative z-20">{children}</div>
    </motion.div>
  )
}

// ── Google icon ───────────────────────────────────────────────────────────────
function GIcon({ s = 16 }: { s?: number }) {
  return (
    <svg width={s} height={s} viewBox="0 0 48 48" fill="none">
      <path d="M47.53 24.56c0-1.64-.15-3.22-.42-4.74H24v8.97h13.22c-.57 3.07-2.3 5.67-4.9 7.41v6.16h7.93c4.64-4.27 7.28-10.57 7.28-17.8z" fill="#4285F4"/>
      <path d="M24 48c6.64 0 12.21-2.2 16.28-5.96l-7.93-6.16c-2.2 1.48-5.02 2.35-8.35 2.35-6.42 0-11.86-4.33-13.8-10.15H2.02v6.36C6.08 42.63 14.42 48 24 48z" fill="#34A853"/>
      <path d="M10.2 28.08A14.93 14.93 0 0 1 9.1 24c0-1.41.24-2.79.65-4.08v-6.36H2.02A23.97 23.97 0 0 0 0 24c0 3.87.93 7.53 2.57 10.77l7.63-6.69z" fill="#FBBC05"/>
      <path d="M24 9.55c3.62 0 6.86 1.24 9.42 3.68l7.07-7.07C36.2 2.24 30.63 0 24 0 14.42 0 6.08 5.37 2.57 13.23l8.13 6.35C12.14 13.88 17.58 9.55 24 9.55z" fill="#EA4335"/>
    </svg>
  )
}

// ── Auth panel ────────────────────────────────────────────────────────────────
function AuthPanel({ compact = false }: { compact?: boolean }) {
  const { loginWithGoogle, loginWithEmail, signUpWithEmail,
    loginAsAdminMock, loginAsMemberMock, redirectError, clearRedirectError } = useAuthStore()
  const [mode, setMode] = useState<'in'|'up'>('in')
  const [email, setEmail] = useState(''); const [pw, setPw] = useState(''); const [nick, setNick] = useState('')
  const [err, setErr] = useState(''); const [loading, setLoading] = useState(false)

  useEffect(() => { if (redirectError) { setErr(redirectError); clearRedirectError?.() } }, [redirectError, clearRedirectError])

  function msg(e: unknown) {
    const c = (e as { code?: string })?.code ?? ''
    if (c.includes('user-not-found') || c.includes('wrong-password') || c.includes('invalid-credential')) return 'Incorrect email or password.'
    if (c.includes('email-already-in-use')) return 'Account already exists.'
    if (c.includes('weak-password')) return 'Password must be 6+ characters.'
    if (c.includes('popup-closed-by-user') || c.includes('cancelled-popup-request')) return ''
    return 'Authentication failed. Try again.'
  }

  async function onEmail(e: React.FormEvent) {
    e.preventDefault(); setErr(''); setLoading(true)
    try {
      if (mode === 'in') await loginWithEmail(email, pw)
      else { if (!nick.trim()) { setErr('Nickname required.'); setLoading(false); return }; await signUpWithEmail(email, pw, nick.trim()) }
    } catch (e) { const m = msg(e); if (m) setErr(m) } finally { setLoading(false) }
  }
  async function onGoogle() {
    setErr(''); setLoading(true)
    try { await loginWithGoogle() } catch (e) { const m = msg(e); if (m) setErr(m); setLoading(false) }
  }

  const inp = 'bg-white/[0.06] border border-white/10 text-white placeholder:text-white/25 rounded-xl px-3 outline-none focus:border-violet-500/60 focus:bg-violet-900/10 transition-all duration-200 text-sm'

  if (compact) return (
    <div className="p-5 flex flex-col gap-3">
      {err && <p className="text-red-400 text-xs font-medium">{err}</p>}
      <button onClick={onGoogle} disabled={loading}
        className="flex items-center justify-center gap-2.5 bg-white text-gray-900 font-semibold h-11 px-4 rounded-xl hover:bg-gray-50 active:scale-[0.98] transition-all text-sm disabled:opacity-50">
        <GIcon s={16} /> Continue with Google
      </button>
      <div className="flex items-center gap-2"><div className="flex-1 h-px bg-white/[0.07]" /><span className="text-white/25 text-[11px]">or</span><div className="flex-1 h-px bg-white/[0.07]" /></div>
      <form onSubmit={onEmail} className="flex flex-col gap-2">
        {mode === 'up' && <input type="text" placeholder="Your nickname" value={nick} maxLength={30} onChange={e => setNick(e.target.value)} className={`${inp} h-10`} />}
        <input type="email" placeholder="Email address" value={email} maxLength={254} onChange={e => setEmail(e.target.value)} className={`${inp} h-10`} required />
        <input type="password" placeholder="Password" value={pw} minLength={6} maxLength={128} onChange={e => setPw(e.target.value)} className={`${inp} h-10`} required />
        <button type="submit" disabled={loading} className="h-11 rounded-xl bg-gradient-to-r from-violet-600 to-indigo-600 text-white font-semibold hover:opacity-90 active:scale-[0.98] transition-all text-sm disabled:opacity-50 mt-0.5">
          {loading ? 'Please wait…' : mode === 'in' ? 'Sign In' : 'Create Account'}
        </button>
      </form>
      <button onClick={() => { setMode(m => m === 'in' ? 'up' : 'in'); setErr('') }} className="text-white/30 text-xs text-center hover:text-white/55 transition-colors">
        {mode === 'in' ? "No account? Sign up" : 'Already have one? Sign in'}
      </button>
      {!hasKeys && (
        <div className="flex gap-2 pt-1 border-t border-white/[0.05]">
          <button onClick={loginAsAdminMock} className="flex-1 text-[11px] bg-amber-500/15 text-amber-300 h-8 rounded-lg">Mock Admin</button>
          <button onClick={loginAsMemberMock} className="flex-1 text-[11px] bg-amber-500/15 text-amber-300 h-8 rounded-lg">Mock Member</button>
        </div>
      )}
    </div>
  )

  return (
    <div className="flex items-center gap-2">
      {err && <p className="text-red-400 text-[11px] max-w-[140px] leading-tight">{err}</p>}
      <button onClick={onGoogle} disabled={loading}
        className="flex items-center gap-1.5 bg-white text-gray-900 font-semibold h-8 px-3 rounded-lg hover:bg-gray-50 active:scale-[0.98] transition-all text-xs shrink-0 disabled:opacity-50">
        <GIcon s={13} /> <span className="hidden lg:inline">Google</span>
      </button>
      <span className="text-white/15 shrink-0">·</span>
      <form onSubmit={onEmail} className="flex items-center gap-1.5">
        {mode === 'up' && <input type="text" placeholder="Nickname" value={nick} maxLength={30} onChange={e => setNick(e.target.value)} className={`${inp} h-8 w-24 text-xs`} />}
        <input type="email" placeholder="Email" value={email} maxLength={254} onChange={e => setEmail(e.target.value)} className={`${inp} h-8 w-36 text-xs`} required />
        <input type="password" placeholder="Password" value={pw} minLength={6} maxLength={128} onChange={e => setPw(e.target.value)} className={`${inp} h-8 w-28 text-xs`} required />
        <button type="submit" disabled={loading} className="h-8 px-3 rounded-lg bg-gradient-to-r from-violet-600 to-indigo-600 text-white text-xs font-semibold hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-50 shrink-0">
          {loading ? '…' : mode === 'in' ? 'Sign In' : 'Sign Up'}
        </button>
      </form>
      <button onClick={() => { setMode(m => m === 'in' ? 'up' : 'in'); setErr('') }} className="text-white/25 text-[11px] underline shrink-0 hover:text-white/45 transition-colors hidden xl:block">
        {mode === 'in' ? 'New?' : 'Sign in'}
      </button>
    </div>
  )
}

// ── Header ────────────────────────────────────────────────────────────────────
function Header() {
  const { user } = useAuthStore()
  const router = useRouter()
  const [mob, setMob] = useState(false)
  useEffect(() => { if (user) router.push('/dashboard') }, [user, router])
  return (
    <header className="sticky top-0 z-50 border-b border-white/[0.05] bg-[#0a0a0a]/90 backdrop-blur-xl">
      <div className="max-w-[1300px] mx-auto px-6 h-[60px] flex items-center gap-6">
        <a href="#" className="flex items-center gap-2.5 shrink-0">
          <div className="w-8 h-8 rounded-[10px] bg-gradient-to-br from-violet-600 to-indigo-600 flex items-center justify-center" style={{ transform: 'rotate(8deg)', boxShadow: '0 4px 16px rgba(124,58,237,0.45)' }}>
            <span className="text-white font-black text-sm" style={{ transform: 'rotate(-8deg)', display: 'block' }}>H</span>
          </div>
          <span className="text-white font-bold text-[15px] tracking-tight">Habitiq</span>
        </a>
        <nav className="hidden lg:flex items-center gap-7 flex-1 justify-center">
          {[['#features','Features'],['#how-it-works','How it works'],['#get-started','Get started']].map(([h,l]) => (
            <a key={h} href={h} className="text-white/40 hover:text-white/75 text-sm font-medium transition-colors duration-200">{l}</a>
          ))}
        </nav>
        <div className="hidden md:flex items-center ml-auto"><AuthPanel /></div>
        <button className="md:hidden ml-auto bg-gradient-to-r from-violet-600 to-indigo-600 text-white text-sm font-semibold h-9 px-4 rounded-xl"
          onClick={() => setMob(o => !o)}>Sign In</button>
      </div>
      {mob && <div className="md:hidden border-t border-white/[0.05] bg-[#0d0d0d]"><AuthPanel compact /></div>}
    </header>
  )
}

// ── Hero — left text, right 3D ────────────────────────────────────────────────
function Hero() {
  const [scroll, setScroll] = useState(0)
  useEffect(() => {
    const h = () => setScroll(Math.min(window.scrollY / 500, 1))
    window.addEventListener('scroll', h, { passive: true })
    return () => window.removeEventListener('scroll', h)
  }, [])

  return (
    <section className="relative w-full bg-[#0a0a0a] overflow-hidden" style={{ minHeight: 'calc(100vh - 60px)' }}>
      {/* 3D canvas — full bleed, RIGHT side is where it shows */}
      <div className="absolute inset-0" style={{ opacity: 1 - scroll * 0.7 }}>
        <HeroCanvas />
      </div>

      {/* Dark gradient covering LEFT side — keeps text readable */}
      <div className="absolute inset-0 pointer-events-none" style={{
        background: 'linear-gradient(105deg, #0a0a0a 0%, #0a0a0a 35%, rgba(10,10,10,0.92) 52%, rgba(10,10,10,0.3) 70%, transparent 100%)'
      }} />
      {/* Bottom fade */}
      <div className="absolute bottom-0 left-0 right-0 h-48 pointer-events-none" style={{ background: 'linear-gradient(to top, #0a0a0a, transparent)' }} />

      {/* Content — left aligned */}
      <div className="relative z-10 max-w-[1300px] mx-auto px-6 flex items-center" style={{ minHeight: 'calc(100vh - 60px)', paddingTop: '80px', paddingBottom: '80px' }}>
        <div className="max-w-[580px]" style={{ opacity: 1 - scroll * 1.5, transform: `translateY(${scroll * 50}px)` }}>

          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6, delay: 0.1 }}
            className="inline-flex items-center gap-2 mb-6 px-3.5 py-1.5 rounded-full border border-violet-500/30 bg-violet-500/10">
            <span className="w-1.5 h-1.5 rounded-full bg-violet-400 animate-pulse" />
            <span className="text-violet-300 text-[11px] font-bold uppercase tracking-[0.13em]">Made for Indian flatmates · Free trial</span>
          </motion.div>

          <motion.h1 initial={{ opacity: 0, y: 24 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.7, delay: 0.2 }}
            className="font-extrabold text-white tracking-tighter leading-[1.02] mb-5"
            style={{ fontSize: 'clamp(3rem, 5.5vw, 4.75rem)' }}>
            Your flat,<br />
            <span style={{ background: 'linear-gradient(130deg,#a78bfa 0%,#818cf8 45%,#6366f1 100%)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', backgroundClip: 'text' }}>
              on autopilot.
            </span>
          </motion.h1>

          <motion.p initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.7, delay: 0.35 }}
            className="text-white/50 text-lg leading-relaxed mb-8 max-w-[460px] font-medium">
            Duties rotate automatically. Bills split fairly. No WhatsApp arguments. Habitiq is the operating system for your shared flat.
          </motion.p>

          <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6, delay: 0.5 }}
            className="flex flex-wrap gap-3 mb-8">
            <a href="#get-started"
              className="inline-flex items-center gap-2 bg-gradient-to-r from-violet-600 to-indigo-600 text-white font-semibold text-sm h-12 px-7 rounded-2xl hover:opacity-90 active:scale-[0.97] transition-all"
              style={{ boxShadow: '0 8px 32px rgba(124,58,237,0.45)' }}>
              Get Started Free
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M3 7h8M8 4l3 3-3 3" stroke="white" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/></svg>
            </a>
            <a href="#features"
              className="inline-flex items-center gap-2 border border-white/[0.12] text-white/60 font-medium text-sm h-12 px-6 rounded-2xl hover:border-white/25 hover:text-white/80 transition-all">
              How it works
            </a>
          </motion.div>

          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.6, delay: 0.7 }}
            className="flex items-center gap-5">
            {[['🔄','Auto rotation'],['⚡','Real-time'],['🤝','Swap system']].map(([e,l]) => (
              <div key={l} className="flex items-center gap-1.5">
                <span className="text-base">{e}</span>
                <span className="text-white/30 text-xs font-medium">{l}</span>
              </div>
            ))}
          </motion.div>
        </div>
      </div>

      {/* Scroll cue */}
      <motion.div animate={{ opacity: [0.3, 0.8, 0.3] }} transition={{ duration: 2.5, repeat: Infinity }}
        className="absolute bottom-8 left-1/2 -translate-x-1/2 flex flex-col items-center gap-1.5 pointer-events-none z-10">
        <div className="w-[1.5px] h-12 rounded-full" style={{ background: 'linear-gradient(to bottom, rgba(124,58,237,0.7), transparent)' }} />
      </motion.div>
    </section>
  )
}

// ── Marquee ───────────────────────────────────────────────────────────────────
const MQ = ['Duty Rotation','Expense Splitting','Real-Time Sync','Swap Requests','Audit Trail','Fair for All','Bill Splitting','Multi-Currency','Auto-Assignment','Zero Arguments']
function Marquee() {
  const items = [...MQ, ...MQ]
  return (
    <div className="border-y border-white/[0.05] py-4 overflow-hidden bg-[#0c0c0c]">
      <div className="flex" style={{ animation: 'mq 32s linear infinite', width: 'max-content' }}>
        {items.map((t, i) => (
          <div key={i} className="flex items-center gap-4 shrink-0 px-5">
            <span className="w-1 h-1 rounded-full bg-violet-500/50" />
            <span className="text-white/30 text-[11px] font-bold uppercase tracking-[0.15em] whitespace-nowrap">{t}</span>
          </div>
        ))}
      </div>
    </div>
  )
}

// ── Product Showcase (ContainerScroll) ───────────────────────────────────────
function AppMockup() {
  const tasks = [
    { name: 'Bathroom Cleaning', person: 'Rahul', status: 'overdue',  emoji: '🚿', due: 'Overdue 1d' },
    { name: 'Vacuum Living Room', person: 'Priya', status: 'today',   emoji: '🧹', due: 'Due today' },
    { name: 'Take out Trash',    person: 'Ankit', status: 'soon',    emoji: '🗑️', due: 'Tomorrow' },
    { name: 'Kitchen Dishes',    person: 'Meera', status: 'done',    emoji: '🍽️', due: 'Done ✓' },
    { name: 'Grocery Run',       person: 'Sai',   status: 'soon',    emoji: '🛒', due: 'In 2 days' },
  ]
  const statusColor: Record<string, string> = {
    overdue: 'border-l-red-500 bg-red-500/5',
    today:   'border-l-amber-400 bg-amber-400/5',
    soon:    'border-l-violet-500 bg-violet-500/5',
    done:    'border-l-emerald-500 bg-emerald-500/5 opacity-60',
  }
  const badgeColor: Record<string, string> = {
    overdue: 'text-red-400 bg-red-500/10',
    today:   'text-amber-400 bg-amber-400/10',
    soon:    'text-violet-400 bg-violet-500/10',
    done:    'text-emerald-400 bg-emerald-500/10',
  }

  return (
    <div className="h-full w-full bg-[#0f0f0f] rounded-xl overflow-hidden flex flex-col">
      {/* App topbar */}
      <div className="flex items-center justify-between px-5 py-3.5 border-b border-white/[0.07] bg-[#141414]">
        <div className="flex items-center gap-2.5">
          <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-violet-600 to-indigo-600 flex items-center justify-center" style={{ transform: 'rotate(8deg)' }}>
            <span className="text-white font-black text-xs" style={{ transform: 'rotate(-8deg)', display: 'block' }}>H</span>
          </div>
          <span className="text-white font-bold text-sm">Koramangala 4BHK</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded-full bg-violet-500/20 border border-violet-500/30 flex items-center justify-center">
            <span className="text-violet-300 text-[10px] font-bold">S</span>
          </div>
          <div className="w-6 h-6 rounded-full bg-indigo-500/20 border border-indigo-500/30 flex items-center justify-center">
            <span className="text-indigo-300 text-[10px] font-bold">R</span>
          </div>
          <div className="w-6 h-6 rounded-full bg-emerald-500/20 border border-emerald-500/30 flex items-center justify-center">
            <span className="text-emerald-300 text-[10px] font-bold">P</span>
          </div>
          <div className="w-6 h-6 rounded-full bg-amber-500/20 border border-amber-500/30 flex items-center justify-center">
            <span className="text-amber-300 text-[10px] font-bold">A</span>
          </div>
        </div>
      </div>

      {/* Stats row */}
      <div className="grid grid-cols-3 gap-3 px-5 py-3 border-b border-white/[0.05]">
        {[['4','Active duties'],['1','Overdue'],['🔄','Auto-rotating']].map(([v,l]) => (
          <div key={l} className="bg-white/[0.04] rounded-xl px-3 py-2 text-center">
            <p className="text-white font-bold text-base">{v}</p>
            <p className="text-white/35 text-[10px] font-medium mt-0.5">{l}</p>
          </div>
        ))}
      </div>

      {/* Task list */}
      <div className="flex-1 overflow-hidden px-5 py-3 space-y-2.5">
        {tasks.map(t => (
          <div key={t.name} className={`border-l-[3px] rounded-xl px-4 py-3 flex items-center gap-3 transition-all ${statusColor[t.status]}`}>
            <span className="text-xl shrink-0">{t.emoji}</span>
            <div className="flex-1 min-w-0">
              <p className={`text-sm font-semibold leading-tight ${t.status === 'done' ? 'line-through text-white/35' : 'text-white'}`}>{t.name}</p>
              <p className="text-white/35 text-xs mt-0.5">{t.person}&apos;s turn</p>
            </div>
            <span className={`text-[10px] font-bold px-2 py-1 rounded-full shrink-0 ${badgeColor[t.status]}`}>{t.due}</span>
          </div>
        ))}
      </div>

      {/* Bottom nav */}
      <div className="flex items-center justify-around px-4 py-3 border-t border-white/[0.07] bg-[#141414]">
        {[['🏠','Home'],['📊','Stats'],['🔄','Swaps'],['⚡','Expenses'],['⚙️','Settings']].map(([e,l]) => (
          <button key={l} className={`flex flex-col items-center gap-1 ${l === 'Home' ? 'text-violet-400' : 'text-white/25'}`}>
            <span className="text-base">{e}</span>
            <span className="text-[9px] font-semibold">{l}</span>
          </button>
        ))}
      </div>
    </div>
  )
}

function ProductShowcase() {
  return (
    <section className="bg-[#0a0a0a] overflow-hidden">
      <ContainerScroll
        titleComponent={
          <div className="mb-6">
            <span className="inline-block mb-4 px-3.5 py-1.5 rounded-full bg-violet-500/10 border border-violet-500/25 text-violet-400 text-[11px] font-bold uppercase tracking-[0.12em]">
              The product
            </span>
            <h2 className="text-4xl sm:text-5xl font-extrabold text-white tracking-tighter leading-tight">
              Everything your flat needs,<br />
              <span style={{ background: 'linear-gradient(130deg,#a78bfa,#818cf8,#6366f1)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', backgroundClip: 'text' }}>
                in one place.
              </span>
            </h2>
            <p className="text-white/40 text-lg mt-4 max-w-lg mx-auto font-medium">
              Real-time duties, expense splitting, swap requests — all managed automatically.
            </p>
          </div>
        }
      >
        <AppMockup />
      </ContainerScroll>
    </section>
  )
}

// ── Problem ───────────────────────────────────────────────────────────────────
const PROBS = [
  { e:'😤', t:'"I already did it last time"', b:'No record. No accountability. Resentment builds week by week until someone moves out or stops talking.' },
  { e:'💬', t:'The WhatsApp graveyard',       b:'Every duty reminder buried under 400 memes. Nobody saw it. Nobody acted. The same conversation next week.' },
  { e:'🥲', t:'One person carries everyone', b:'The responsible flatmate burns out managing everything. The flat suffers. Friendships get weird.' },
]

function Problems() {
  return (
    <section className="py-28 px-6 bg-[#0a0a0a] relative overflow-hidden">
      {/* Floating bg shape */}
      <div className="absolute top-20 left-10 w-96 h-96 rounded-full opacity-[0.04] pointer-events-none"
        style={{ background: 'radial-gradient(circle, #7c3aed, transparent)', filter: 'blur(80px)' }} />
      <div className="max-w-[1120px] mx-auto">
        <Reveal className="text-center mb-16">
          <span className="inline-block mb-4 px-3.5 py-1.5 rounded-full bg-red-500/10 border border-red-500/20 text-red-400 text-[11px] font-bold uppercase tracking-[0.12em]">The problem</span>
          <h2 className="text-4xl sm:text-[3.25rem] font-extrabold text-white tracking-tighter leading-tight">Every flat has these fights.</h2>
          <p className="text-white/35 text-lg mt-4 max-w-md mx-auto font-medium">Not because your flatmates are bad. Because there&apos;s no system.</p>
        </Reveal>
        <div className="grid sm:grid-cols-3 gap-4">
          {PROBS.map((p, i) => (
            <Reveal key={p.t} delay={i * 100}>
              <Card3D className="h-full" glowColor="rgba(239,68,68,0.15)">
                <div className="h-full bg-white/[0.025] border border-white/[0.07] rounded-2xl p-7 group cursor-default">
                  <div className="text-4xl mb-5">{p.e}</div>
                  <h3 className="text-white font-bold text-base mb-3 leading-snug">{p.t}</h3>
                  <p className="text-white/38 text-sm leading-relaxed">{p.b}</p>
                </div>
              </Card3D>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  )
}

// ── Bridge ────────────────────────────────────────────────────────────────────
function Bridge() {
  return (
    <div className="relative overflow-hidden border-y border-white/[0.04]" style={{ background: 'linear-gradient(135deg,#0d0b1f 0%,#0f0d23 50%,#0d0b1f 100%)' }}>
      <div className="absolute inset-0 pointer-events-none" style={{ background: 'radial-gradient(ellipse 70% 80% at 50% 50%,rgba(124,58,237,0.07) 0%,transparent 100%)' }} />
      <Reveal className="max-w-3xl mx-auto text-center py-24 px-6">
        <p className="text-3xl sm:text-[2.6rem] font-extrabold text-white tracking-tighter leading-tight">
          There&apos;s a simpler way —{' '}
          <span style={{ background:'linear-gradient(90deg,#a78bfa,#6366f1)', WebkitBackgroundClip:'text', WebkitTextFillColor:'transparent', backgroundClip:'text' }}>
            a system that runs itself.
          </span>
        </p>
        <p className="text-white/38 text-lg mt-5 font-medium max-w-lg mx-auto">No manager. No nagging. No whiteboard that nobody updates. Just fairness, automated.</p>
      </Reveal>
    </div>
  )
}

// ── Features ──────────────────────────────────────────────────────────────────
const FEATS = [
  { e:'🔄', l:'Rotation Engine',  t:'Auto-assigns every duty, every time',       b:'Habitiq maintains a rotation queue per task. Mark done → next person assigned automatically. Nobody decides. The system is the authority.',         hi:'Zero manual scheduling' },
  { e:'⚡', l:'Real-Time Sync',   t:'Everyone sees everything instantly',         b:'When Rahul marks Kitchen done at 7pm, every flatmate\'s screen updates in under a second. No refresh. No "did you do it?" texts ever again.',         hi:'Firestore live updates' },
  { e:'💸', l:'Expense Splitting',t:'Split bills without the awkwardness',        b:'Log shared expenses — groceries, electricity, rent. Split equally, by %, or custom amount per person. Everyone sees who owes whom. Settle with one tap.',hi:'Equal · % · Custom split' },
  { e:'🔁', l:'Swap System',      t:'Swap duties without the drama',              b:'Travelling? Request a swap. Flatmate gets notified, accepts or declines. Task moves. Audit log records it. WhatsApp stays for memes only.',            hi:'Formal request flow' },
  { e:'📋', l:'Audit Trail',      t:'Every action permanently recorded',          b:'Who did what. When. Whether it was on time. Six months later you can still check. No disputes. No "I don\'t remember". Just facts.',                   hi:'All members can see' },
  { e:'🌍', l:'Multi-Currency',   t:'Works for international flatmates',          b:'Log expenses in INR, USD, EUR, AED, SGD and more. Auto-converts to flat\'s base currency. Perfect for flats with people from different countries.',     hi:'7 currencies supported' },
]

function Features() {
  return (
    <section id="features" className="py-28 px-6 bg-[#0a0a0a] relative overflow-hidden">
      <div className="absolute bottom-20 right-10 w-[500px] h-[500px] rounded-full opacity-[0.035] pointer-events-none"
        style={{ background: 'radial-gradient(circle,#4338ca,transparent)', filter: 'blur(90px)' }} />
      <div className="max-w-[1120px] mx-auto">
        <Reveal className="text-center mb-20">
          <span className="inline-block mb-4 px-3.5 py-1.5 rounded-full bg-violet-500/10 border border-violet-500/25 text-violet-400 text-[11px] font-bold uppercase tracking-[0.12em]">Features</span>
          <h2 className="text-4xl sm:text-[3.25rem] font-extrabold text-white tracking-tighter">Everything your flat needs.</h2>
          <p className="text-white/35 text-lg mt-4 max-w-md mx-auto font-medium">Set it up once. Let it handle the rest, forever.</p>
        </Reveal>
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
          {FEATS.map((f, i) => (
            <Reveal key={f.t} delay={i * 70}>
              <Card3D className="h-full" intensity={10}>
                <div className="h-full bg-white/[0.025] border border-white/[0.07] rounded-2xl p-7 group cursor-default">
                  <div className="w-12 h-12 rounded-xl flex items-center justify-center text-2xl mb-5 shrink-0"
                    style={{ background: 'rgba(124,58,237,0.14)', border: '1px solid rgba(124,58,237,0.2)' }}>
                    {f.e}
                  </div>
                  <span className="text-violet-400 text-[11px] font-bold uppercase tracking-[0.12em]">{f.l}</span>
                  <h3 className="text-white font-bold text-[1.05rem] mt-1.5 mb-2.5 leading-snug">{f.t}</h3>
                  <p className="text-white/38 text-sm leading-relaxed mb-5">{f.b}</p>
                  <div className="inline-flex items-center gap-1.5 bg-violet-500/10 border border-violet-500/20 rounded-full px-3 py-1">
                    <svg width="9" height="7" viewBox="0 0 9 7" fill="none"><path d="M1 3.5L3 5.5L8 1" stroke="#a78bfa" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round"/></svg>
                    <span className="text-violet-300/80 text-[11px] font-semibold">{f.hi}</span>
                  </div>
                </div>
              </Card3D>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  )
}

// ── How it works ──────────────────────────────────────────────────────────────
const STEPS = [
  { n:'01', t:'Create your flat', b:'Sign up, name your flat, get a 6-digit invite code. 45 seconds.' },
  { n:'02', t:'Flatmates join',   b:'Share the code. Everyone joins with one tap. Auto-added to all queues.' },
  { n:'03', t:'System runs itself',b:'Duties rotate automatically. Bills split. Nobody has to manage anything.' },
]

function HowItWorks() {
  return (
    <section id="how-it-works" className="py-28 px-6 bg-[#0d0d0d]">
      <div className="max-w-[1120px] mx-auto">
        <Reveal className="text-center mb-20">
          <span className="inline-block mb-4 px-3.5 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-[11px] font-bold uppercase tracking-[0.12em]">Setup</span>
          <h2 className="text-4xl sm:text-[3.25rem] font-extrabold text-white tracking-tighter">Up and running in 3 minutes.</h2>
        </Reveal>
        <div className="grid md:grid-cols-3 gap-6 relative">
          <div className="hidden md:block absolute top-[28px] left-[calc(16.66%+36px)] right-[calc(16.66%+36px)] h-px"
            style={{ background: 'linear-gradient(90deg,transparent,rgba(124,58,237,0.5) 30%,rgba(124,58,237,0.5) 70%,transparent)' }} />
          {STEPS.map((s, i) => (
            <Reveal key={s.n} delay={i * 120}>
              <Card3D intensity={8} className="text-center">
                <div className="bg-white/[0.02] border border-white/[0.06] rounded-2xl p-8">
                  <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl text-white font-black text-base mb-6 relative z-10"
                    style={{ background: 'linear-gradient(135deg,#7c3aed,#4338ca)', boxShadow: '0 8px 28px rgba(124,58,237,0.4)' }}>
                    {s.n}
                  </div>
                  <h3 className="text-white font-bold text-lg mb-2.5">{s.t}</h3>
                  <p className="text-white/38 text-sm leading-relaxed max-w-[220px] mx-auto">{s.b}</p>
                </div>
              </Card3D>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  )
}

// ── Stats ─────────────────────────────────────────────────────────────────────
function Stats() {
  return (
    <section className="py-20 px-6 border-y border-white/[0.04] bg-[#0a0a0a]">
      <div className="max-w-[1120px] mx-auto">
        <Reveal>
          <div className="grid grid-cols-3 gap-8 text-center">
            {[['10M+','People in shared accommodation in India'],['< 2 min','Average flat setup time'],['100%','Free during trial — no card']].map(([n,l]) => (
              <div key={l}>
                <p className="font-extrabold tracking-tighter mb-2"
                  style={{ fontSize:'clamp(2rem,4vw,3rem)', background:'linear-gradient(135deg,#a78bfa,#818cf8)', WebkitBackgroundClip:'text', WebkitTextFillColor:'transparent', backgroundClip:'text' }}>
                  {n}
                </p>
                <p className="text-white/30 text-xs sm:text-sm font-medium max-w-[180px] mx-auto leading-snug">{l}</p>
              </div>
            ))}
          </div>
        </Reveal>
      </div>
    </section>
  )
}

// ── Testimonials ──────────────────────────────────────────────────────────────
const TESTI = [
  { q:"Finally something that doesn't need one person to run it. Set up in 5 minutes, haven't argued about chores since.", w:'Flat of 4, Koramangala' },
  { q:"The expense splitting is the best part. Everyone can see exactly what they owe. No more awkward money conversations.", w:'PG residents, Banjara Hills' },
  { q:"The swap request feature saved my life during exam week. Just tap, flatmate accepts, done.", w:'Student flat, Pune' },
]

function Testimonials() {
  return (
    <section className="py-28 px-6 bg-[#0a0a0a]">
      <div className="max-w-[1120px] mx-auto">
        <Reveal className="text-center mb-16">
          <span className="inline-block mb-4 px-3.5 py-1.5 rounded-full bg-white/[0.04] border border-white/[0.08] text-white/30 text-[11px] font-bold uppercase tracking-[0.12em]">Early users</span>
          <h2 className="text-4xl font-extrabold text-white tracking-tighter">Real flats. Real results.</h2>
        </Reveal>
        <div className="grid md:grid-cols-3 gap-4">
          {TESTI.map((t, i) => (
            <Reveal key={t.w} delay={i * 90}>
              <Card3D className="h-full" intensity={9} glowColor="rgba(167,139,250,0.12)">
                <div className="h-full bg-white/[0.025] border border-white/[0.07] rounded-2xl p-7">
                  <div className="flex gap-0.5 mb-5">
                    {[...Array(5)].map((_,j) => (
                      <svg key={j} width="13" height="13" viewBox="0 0 13 13" fill="#7c3aed"><path d="M6.5 1l1.5 3.2 3.5.5-2.5 2.4.6 3.5L6.5 9 3.4 10.6l.6-3.5L1.5 4.7l3.5-.5L6.5 1z"/></svg>
                    ))}
                  </div>
                  <p className="text-white/65 text-sm leading-relaxed mb-5 font-medium">&ldquo;{t.q}&rdquo;</p>
                  <p className="text-white/22 text-[11px] font-bold uppercase tracking-wider">— {t.w}</p>
                </div>
              </Card3D>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  )
}

// ── Get started ───────────────────────────────────────────────────────────────
function GetStarted() {
  return (
    <section id="get-started" className="py-28 px-6 relative overflow-hidden" style={{ background:'linear-gradient(180deg,#0d0d0d 0%,#0a0a0a 100%)' }}>
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[700px] h-[400px] opacity-[0.06] pointer-events-none"
        style={{ background:'radial-gradient(ellipse,#7c3aed,transparent)', filter:'blur(60px)' }} />
      <div className="max-w-[1120px] mx-auto">
        <div className="grid lg:grid-cols-2 gap-16 items-start">
          <Reveal>
            <span className="inline-block mb-5 px-3.5 py-1.5 rounded-full bg-violet-500/10 border border-violet-500/25 text-violet-400 text-[11px] font-bold uppercase tracking-[0.12em]">Start free</span>
            <h2 className="text-4xl sm:text-5xl font-extrabold text-white tracking-tighter leading-tight mb-5">
              Your flat deserves<br />a fair system.
            </h2>
            <p className="text-white/38 text-lg font-medium mb-10 leading-relaxed max-w-sm">
              Create a flat or join your flatmates. Free. No card. Under 2 minutes.
            </p>
            <div className="space-y-4">
              {['Unlimited duty rotation & expense splitting','Real-time sync across all flatmates','Swap system, activity log, analytics','Works on any phone — no app install'].map(item => (
                <div key={item} className="flex items-center gap-3">
                  <div className="w-5 h-5 rounded-full flex items-center justify-center shrink-0" style={{ background:'rgba(124,58,237,0.22)', border:'1px solid rgba(124,58,237,0.3)' }}>
                    <svg width="9" height="7" viewBox="0 0 9 7" fill="none"><path d="M1 3.5L3 5.5L8 1" stroke="#a78bfa" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round"/></svg>
                  </div>
                  <span className="text-white/50 text-sm font-medium">{item}</span>
                </div>
              ))}
            </div>
          </Reveal>
          <Reveal delay={110}>
            <Card3D intensity={6} glowColor="rgba(124,58,237,0.15)" className="w-full">
              <div className="rounded-2xl border border-white/[0.09] overflow-hidden"
                style={{ background:'rgba(255,255,255,0.025)', boxShadow:'0 0 0 1px rgba(124,58,237,0.1), 0 24px 72px rgba(124,58,237,0.12)' }}>
                <div className="px-7 pt-7 pb-3 border-b border-white/[0.06]">
                  <p className="text-white font-bold text-lg">Create your account</p>
                  <p className="text-white/30 text-sm mt-1">Join your flatmates in under a minute.</p>
                </div>
                <AuthPanel compact />
              </div>
            </Card3D>
          </Reveal>
        </div>
      </div>
    </section>
  )
}

// ── Footer ────────────────────────────────────────────────────────────────────
function Footer() {
  return (
    <footer className="border-t border-white/[0.05] bg-[#0a0a0a] py-10 px-6">
      <div className="max-w-[1120px] mx-auto flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-2.5">
          <div className="w-6 h-6 rounded-md bg-gradient-to-br from-violet-600 to-indigo-600 flex items-center justify-center">
            <span className="text-white font-black text-[10px]">H</span>
          </div>
          <span className="text-white/45 font-semibold text-sm">Habitiq — Shared living, managed.</span>
        </div>
        <div className="flex items-center gap-5">
          <a href="#" className="text-white/22 text-xs hover:text-white/45 transition-colors">Privacy</a>
          <a href="#" className="text-white/22 text-xs hover:text-white/45 transition-colors">About</a>
          <span className="text-white/18 text-xs">© 2026 Habitiq · Made in India 🇮🇳</span>
        </div>
      </div>
    </footer>
  )
}

// ── Page ──────────────────────────────────────────────────────────────────────
export default function LandingPage() {
  return (
    <div className={`${jakarta.className} min-h-screen bg-[#0a0a0a] antialiased`}>
      <style>{`
        @keyframes mq { from { transform:translateX(0) } to { transform:translateX(-50%) } }
      `}</style>
      <Header />
      <main>
        <Hero />
        <Marquee />
        <ProductShowcase />
        <Problems />
        <Bridge />
        <Features />
        <HowItWorks />
        <Stats />
        <Testimonials />
        <GetStarted />
      </main>
      <Footer />
    </div>
  )
}
