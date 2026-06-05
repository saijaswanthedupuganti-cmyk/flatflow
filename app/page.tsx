'use client'

import dynamic from 'next/dynamic'
import { useEffect, useRef, useState, ReactNode } from 'react'
import { useRouter } from 'next/navigation'
import { Plus_Jakarta_Sans } from 'next/font/google'
import { motion, useMotionValue, useSpring, useTransform } from 'framer-motion'
import { ContainerScroll } from '@/components/ui/container-scroll-animation'
import { useAuthStore } from '@/store/useAuthStore'
import { hasKeys } from '@/lib/firebase'
import {
  RotateCcw, Zap, Receipt, ArrowLeftRight, ClipboardList, Globe,
  Check, ArrowRight, Shield, Users, BarChart2, X, Star,
} from 'lucide-react'
import TestimonialSlider, { type Testimonial } from '@/components/ui/testimonial-slider'
import { AnimatedTabs, type Tab } from '@/components/ui/animated-tabs'

const HeroCanvas = dynamic(() => import('@/components/HeroCanvas'), {
  ssr: false,
  loading: () => <div className="w-full h-full bg-[#050510]" />,
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
      transform: v ? 'translateY(0)' : 'translateY(28px)',
      transition: `opacity .7s cubic-bezier(.22,1,.36,1) ${delay}ms, transform .7s cubic-bezier(.22,1,.36,1) ${delay}ms`,
    }}>{children}</div>
  )
}

// ── 3D tilt card ──────────────────────────────────────────────────────────────
function Card3D({ children, className = '', intensity = 12, glowColor = 'rgba(124,58,237,0.2)' }: {
  children: ReactNode; className?: string; intensity?: number; glowColor?: string
}) {
  const mx = useMotionValue(0)
  const my = useMotionValue(0)
  const rx = useSpring(useTransform(my, [-0.5, 0.5], [intensity, -intensity]), { stiffness: 350, damping: 28 })
  const ry = useSpring(useTransform(mx, [-0.5, 0.5], [-intensity, intensity]), { stiffness: 350, damping: 28 })
  const glow = useTransform([mx, my], ([x, y]: number[]) =>
    `radial-gradient(circle at ${(x + 0.5) * 100}% ${(y + 0.5) * 100}%, ${glowColor} 0%, transparent 68%)`)
  const shine = useTransform([mx, my], ([x, y]: number[]) =>
    `radial-gradient(circle at ${(x + 0.5) * 100}% ${(y + 0.5) * 100}%, rgba(255,255,255,0.05) 0%, transparent 55%)`)
  const onMove = (e: React.MouseEvent<HTMLDivElement>) => {
    const r = e.currentTarget.getBoundingClientRect()
    mx.set((e.clientX - r.left) / r.width - 0.5)
    my.set((e.clientY - r.top) / r.height - 0.5)
  }
  return (
    <motion.div onMouseMove={onMove} onMouseLeave={() => { mx.set(0); my.set(0) }}
      style={{ rotateX: rx, rotateY: ry, transformStyle: 'preserve-3d', perspective: 1000 }}
      className={`relative ${className}`}>
      <motion.div className="absolute inset-0 rounded-2xl pointer-events-none z-10" style={{ background: glow }} />
      <motion.div className="absolute inset-0 rounded-2xl pointer-events-none z-10" style={{ background: shine }} />
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
        className="flex items-center justify-center gap-2.5 bg-white text-gray-900 font-semibold h-11 px-4 rounded-xl hover:bg-gray-50 active:scale-[0.98] transition-all text-sm disabled:opacity-50 cursor-pointer">
        <GIcon s={16} /> Continue with Google
      </button>
      <div className="flex items-center gap-2"><div className="flex-1 h-px bg-white/[0.07]" /><span className="text-white/25 text-[11px]">or</span><div className="flex-1 h-px bg-white/[0.07]" /></div>
      <form onSubmit={onEmail} className="flex flex-col gap-2">
        {mode === 'up' && <input type="text" placeholder="Your nickname" value={nick} maxLength={30} onChange={e => setNick(e.target.value)} className={`${inp} h-10`} />}
        <input type="email" placeholder="Email address" value={email} maxLength={254} onChange={e => setEmail(e.target.value)} className={`${inp} h-10`} required />
        <input type="password" placeholder="Password" value={pw} minLength={6} maxLength={128} onChange={e => setPw(e.target.value)} className={`${inp} h-10`} required />
        <button type="submit" disabled={loading} className="h-11 rounded-xl bg-gradient-to-r from-violet-600 to-indigo-600 text-white font-semibold hover:opacity-90 active:scale-[0.98] transition-all text-sm disabled:opacity-50 mt-0.5 cursor-pointer">
          {loading ? 'Please wait…' : mode === 'in' ? 'Sign In' : 'Create Account'}
        </button>
      </form>
      <button onClick={() => { setMode(m => m === 'in' ? 'up' : 'in'); setErr('') }} className="text-white/30 text-xs text-center hover:text-white/55 transition-colors cursor-pointer">
        {mode === 'in' ? "No account? Sign up free" : 'Already have one? Sign in'}
      </button>
      {!hasKeys && (
        <div className="flex gap-2 pt-1 border-t border-white/[0.05]">
          <button onClick={loginAsAdminMock} className="flex-1 text-[11px] bg-amber-500/15 text-amber-300 h-8 rounded-lg cursor-pointer">Mock Admin</button>
          <button onClick={loginAsMemberMock} className="flex-1 text-[11px] bg-amber-500/15 text-amber-300 h-8 rounded-lg cursor-pointer">Mock Member</button>
        </div>
      )}
    </div>
  )

  return (
    <div className="flex items-center gap-2">
      {err && <p className="text-red-400 text-[11px] max-w-[140px] leading-tight">{err}</p>}
      <button onClick={onGoogle} disabled={loading}
        className="flex items-center gap-1.5 bg-white text-gray-900 font-semibold h-8 px-3 rounded-lg hover:bg-gray-50 active:scale-[0.98] transition-all text-xs shrink-0 disabled:opacity-50 cursor-pointer">
        <GIcon s={13} /> <span className="hidden lg:inline">Google</span>
      </button>
      <span className="text-white/15 shrink-0">·</span>
      <form onSubmit={onEmail} className="flex items-center gap-1.5">
        {mode === 'up' && <input type="text" placeholder="Nickname" value={nick} maxLength={30} onChange={e => setNick(e.target.value)} className={`${inp} h-8 w-24 text-xs`} />}
        <input type="email" placeholder="Email" value={email} maxLength={254} onChange={e => setEmail(e.target.value)} className={`${inp} h-8 w-36 text-xs`} required />
        <input type="password" placeholder="Password" value={pw} minLength={6} maxLength={128} onChange={e => setPw(e.target.value)} className={`${inp} h-8 w-28 text-xs`} required />
        <button type="submit" disabled={loading} className="h-8 px-3 rounded-lg bg-gradient-to-r from-violet-600 to-indigo-600 text-white text-xs font-semibold hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-50 shrink-0 cursor-pointer">
          {loading ? '…' : mode === 'in' ? 'Sign In' : 'Sign Up'}
        </button>
      </form>
      <button onClick={() => { setMode(m => m === 'in' ? 'up' : 'in'); setErr('') }} className="text-white/25 text-[11px] underline shrink-0 hover:text-white/45 transition-colors hidden xl:block cursor-pointer">
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
    <header className="sticky top-0 z-50 border-b border-white/[0.05] bg-[#050510]/90 backdrop-blur-xl">
      <div className="max-w-[1300px] mx-auto px-6 h-[60px] flex items-center gap-6">
        <a href="#" className="flex items-center gap-2.5 shrink-0">
          <div className="w-8 h-8 rounded-[10px] bg-gradient-to-br from-violet-600 to-indigo-600 flex items-center justify-center" style={{ transform: 'rotate(8deg)', boxShadow: '0 4px 16px rgba(124,58,237,0.5)' }}>
            <span className="text-white font-black text-sm" style={{ transform: 'rotate(-8deg)', display: 'block' }}>H</span>
          </div>
          <span className="text-white font-bold text-[15px] tracking-tight">Habitiq</span>
        </a>
        <nav className="hidden lg:flex items-center gap-7 flex-1 justify-center">
          {[['#features','Features'],['#compare','Compare'],['#how-it-works','How it works'],['#get-started','Get started']].map(([h,l]) => (
            <a key={h} href={h} className="text-white/40 hover:text-white/80 text-sm font-medium transition-colors duration-200">{l}</a>
          ))}
        </nav>
        <div className="hidden md:flex items-center ml-auto"><AuthPanel /></div>
        <button className="md:hidden ml-auto bg-gradient-to-r from-violet-600 to-indigo-600 text-white text-sm font-semibold h-9 px-4 rounded-xl cursor-pointer"
          onClick={() => setMob(o => !o)}>Sign In</button>
      </div>
      {mob && <div className="md:hidden border-t border-white/[0.05] bg-[#0a0a14]"><AuthPanel compact /></div>}
    </header>
  )
}

// ── Hero ──────────────────────────────────────────────────────────────────────
function Hero() {
  const [scroll, setScroll] = useState(0)
  useEffect(() => {
    const h = () => setScroll(Math.min(window.scrollY / 500, 1))
    window.addEventListener('scroll', h, { passive: true })
    return () => window.removeEventListener('scroll', h)
  }, [])

  return (
    <section className="relative w-full bg-[#050510] overflow-hidden" style={{ minHeight: 'calc(100vh - 60px)' }}>
      {/* Dot grid */}
      <div className="absolute inset-0 pointer-events-none" style={{
        backgroundImage: 'radial-gradient(circle, rgba(124,58,237,0.1) 1px, transparent 1px)',
        backgroundSize: '36px 36px',
      }} />

      {/* Aurora blobs */}
      <div className="absolute top-[-15%] left-[-8%] w-[650px] h-[650px] rounded-full pointer-events-none"
        style={{ background: 'radial-gradient(circle, rgba(124,58,237,0.22) 0%, transparent 70%)', filter: 'blur(80px)' }} />
      <div className="absolute bottom-[-10%] right-[5%] w-[450px] h-[450px] rounded-full pointer-events-none"
        style={{ background: 'radial-gradient(circle, rgba(79,70,229,0.18) 0%, transparent 70%)', filter: 'blur(70px)' }} />

      {/* Background canvas on right */}
      <div className="absolute inset-0 pointer-events-none" style={{ opacity: 1 - scroll * 0.8 }}>
        <HeroCanvas />
      </div>

      {/* Left gradient mask */}
      <div className="absolute inset-0 pointer-events-none" style={{
        background: 'linear-gradient(105deg, #050510 0%, #050510 32%, rgba(5,5,16,0.94) 50%, rgba(5,5,16,0.3) 68%, transparent 100%)',
      }} />
      {/* Bottom fade */}
      <div className="absolute bottom-0 left-0 right-0 h-48 pointer-events-none" style={{ background: 'linear-gradient(to top, #050510, transparent)' }} />

      {/* Content */}
      <div className="relative z-10 max-w-[1300px] mx-auto px-6 flex flex-col lg:flex-row items-center gap-16"
        style={{ minHeight: 'calc(100vh - 60px)', paddingTop: '72px', paddingBottom: '72px', opacity: 1 - scroll * 1.2, transform: `translateY(${scroll * 40}px)` }}>

        {/* Left: Text */}
        <div className="flex-1 max-w-[560px] text-center lg:text-left">
          <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.55, delay: 0.1 }}
            className="inline-flex items-center gap-2 mb-7 px-3.5 py-1.5 rounded-full border border-violet-500/30 bg-violet-500/10">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
            <span className="text-violet-300 text-[11px] font-bold uppercase tracking-[0.13em]">Live trial · Free · Made in India</span>
          </motion.div>

          <motion.h1 initial={{ opacity: 0, y: 24 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.7, delay: 0.18 }}
            className="font-extrabold text-white tracking-tighter leading-[1.01] mb-6"
            style={{ fontSize: 'clamp(3.4rem, 6vw, 5.5rem)' }}>
            Run your flat<br />
            <span style={{ background: 'linear-gradient(130deg,#c4b5fd 0%,#a78bfa 40%,#818cf8 75%,#6366f1 100%)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', backgroundClip: 'text' }}>
              without drama.
            </span>
          </motion.h1>

          <motion.p initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.65, delay: 0.3 }}
            className="text-white/52 text-xl leading-relaxed mb-9 max-w-[460px] font-medium mx-auto lg:mx-0">
            Duties rotate automatically. Bills split fairly. No WhatsApp arguments — just a flat that runs itself.
          </motion.p>

          <motion.div initial={{ opacity: 0, y: 14 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6, delay: 0.42 }}
            className="flex flex-wrap gap-3 mb-10 justify-center lg:justify-start">
            <a href="#get-started"
              className="inline-flex items-center gap-2.5 bg-gradient-to-r from-violet-600 to-indigo-600 text-white font-semibold text-sm h-12 px-7 rounded-2xl hover:opacity-92 active:scale-[0.97] transition-all cursor-pointer"
              style={{ boxShadow: '0 8px 32px rgba(124,58,237,0.5)' }}>
              Get Started Free <ArrowRight size={15} />
            </a>
            <a href="#how-it-works"
              className="inline-flex items-center gap-2 border border-white/[0.13] text-white/60 font-medium text-sm h-12 px-6 rounded-2xl hover:border-white/28 hover:text-white/85 transition-all cursor-pointer">
              See how it works
            </a>
          </motion.div>

          {/* Avatar social proof */}
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.6, delay: 0.58 }}
            className="flex items-center gap-4 justify-center lg:justify-start">
            <div className="flex -space-x-2.5">
              {[['S','#7c3aed'],['R','#4f46e5'],['P','#6366f1'],['A','#8b5cf6'],['M','#a78bfa']].map(([l, c], i) => (
                <div key={i} className="w-9 h-9 rounded-full border-2 border-[#050510] flex items-center justify-center text-xs font-bold text-white"
                  style={{ background: c }}>
                  {l}
                </div>
              ))}
            </div>
            <div>
              <div className="flex gap-0.5 mb-1">
                {[...Array(5)].map((_, j) => <Star key={j} size={11} fill="#a78bfa" className="text-violet-400" />)}
              </div>
              <p className="text-white/32 text-xs font-medium">Trusted by 20+ flats in India</p>
            </div>
          </motion.div>
        </div>

        {/* Right: Floating mockup frame */}
        <div className="flex-1 flex justify-center lg:justify-end max-w-[490px] w-full hidden lg:flex">
          <motion.div initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.9, delay: 0.35 }}
            className="relative w-full max-w-[420px]">
            {/* Floating card: expense */}
            <motion.div animate={{ y: [-5, 5, -5] }} transition={{ duration: 3.5, repeat: Infinity, ease: 'easeInOut' }}
              className="absolute -left-10 top-14 z-20 bg-[#12122a]/85 border border-white/10 rounded-2xl px-4 py-3 backdrop-blur-xl shadow-2xl min-w-[140px]">
              <p className="text-[11px] text-white/40 font-medium">This month</p>
              <p className="text-[1.15rem] font-extrabold text-white leading-tight">₹4,850</p>
              <p className="text-[10px] text-emerald-400 font-bold mt-0.5 flex items-center gap-1">
                <span>↓ 12% vs last month</span>
              </p>
            </motion.div>

            {/* Floating card: duty */}
            <motion.div animate={{ y: [5, -5, 5] }} transition={{ duration: 4, repeat: Infinity, ease: 'easeInOut', delay: 0.9 }}
              className="absolute -right-8 bottom-24 z-20 bg-[#12122a]/85 border border-white/10 rounded-2xl px-4 py-3 backdrop-blur-xl shadow-2xl min-w-[130px]">
              <p className="text-[11px] text-white/40 font-medium">Rahul&apos;s turn</p>
              <p className="text-sm font-bold text-white mt-0.5">Vacuum Living Room</p>
              <div className="flex items-center gap-1.5 mt-1.5">
                <span className="w-1.5 h-1.5 rounded-full bg-amber-400 animate-pulse" />
                <p className="text-[10px] text-amber-400 font-bold">Due today</p>
              </div>
            </motion.div>

            {/* Browser chrome frame */}
            <div className="rounded-2xl overflow-hidden shadow-[0_24px_80px_rgba(124,58,237,0.3),0_0_0_1px_rgba(124,58,237,0.15)]"
              style={{ background: '#0d0d1f' }}>
              <div className="flex items-center gap-2.5 px-4 py-3 border-b border-white/[0.06] bg-[#111126]">
                <div className="flex gap-1.5">
                  <div className="w-2.5 h-2.5 rounded-full bg-[#ff5f57]" />
                  <div className="w-2.5 h-2.5 rounded-full bg-[#febc2e]" />
                  <div className="w-2.5 h-2.5 rounded-full bg-[#28c840]" />
                </div>
                <div className="flex-1 h-5 rounded-md bg-white/[0.04] flex items-center px-2.5">
                  <span className="text-white/18 text-[10px]">garbage-liart.vercel.app</span>
                </div>
              </div>
              <AppMockup />
            </div>
          </motion.div>
        </div>
      </div>

      {/* Scroll cue */}
      <motion.div animate={{ opacity: [0.25, 0.75, 0.25] }} transition={{ duration: 2.5, repeat: Infinity }}
        className="absolute bottom-8 left-1/2 -translate-x-1/2 flex flex-col items-center gap-1.5 pointer-events-none z-10">
        <div className="w-[1.5px] h-10 rounded-full" style={{ background: 'linear-gradient(to bottom, rgba(124,58,237,0.7), transparent)' }} />
      </motion.div>
    </section>
  )
}

// ── Marquee ───────────────────────────────────────────────────────────────────
const MQ = ['Duty Rotation','Expense Splitting','Real-Time Sync','Swap Requests','Audit Trail','Fair for All','Bill Splitting','Multi-Currency','Auto-Assignment','Zero Arguments']
function Marquee() {
  const items = [...MQ, ...MQ]
  return (
    <div className="border-y border-white/[0.05] py-4 overflow-hidden" style={{ background: 'rgba(255,255,255,0.02)' }}>
      <div className="flex" style={{ animation: 'mq 32s linear infinite', width: 'max-content' }}>
        {items.map((t, i) => (
          <div key={i} className="flex items-center gap-4 shrink-0 px-5">
            <span className="w-1 h-1 rounded-full bg-violet-500/55" />
            <span className="text-white/28 text-[11px] font-bold uppercase tracking-[0.15em] whitespace-nowrap">{t}</span>
          </div>
        ))}
      </div>
    </div>
  )
}

// ── App Mockup ────────────────────────────────────────────────────────────────
function AppMockup() {
  const tasks = [
    { name: 'Bathroom Cleaning', person: 'Rahul', status: 'overdue', due: 'Overdue 1d' },
    { name: 'Vacuum Living Room', person: 'Priya', status: 'today',   due: 'Due today' },
    { name: 'Take out Trash',    person: 'Ankit', status: 'soon',    due: 'Tomorrow' },
    { name: 'Kitchen Dishes',    person: 'Meera', status: 'done',    due: 'Done' },
    { name: 'Grocery Run',       person: 'Sai',   status: 'soon',    due: 'In 2 days' },
  ]
  const sl: Record<string, string> = {
    overdue: 'border-l-red-500 bg-red-500/[0.04]',
    today:   'border-l-amber-400 bg-amber-400/[0.04]',
    soon:    'border-l-violet-500 bg-violet-500/[0.04]',
    done:    'border-l-emerald-500 bg-emerald-500/[0.04] opacity-55',
  }
  const bl: Record<string, string> = {
    overdue: 'text-red-400 bg-red-500/10',
    today:   'text-amber-400 bg-amber-400/10',
    soon:    'text-violet-400 bg-violet-500/10',
    done:    'text-emerald-400 bg-emerald-500/10',
  }
  return (
    <div className="h-full w-full bg-[#0d0d1f] flex flex-col" style={{ minHeight: '380px' }}>
      <div className="flex items-center justify-between px-5 py-3.5 border-b border-white/[0.06] bg-[#111126]">
        <div className="flex items-center gap-2.5">
          <div className="w-6 h-6 rounded-lg bg-gradient-to-br from-violet-600 to-indigo-600 flex items-center justify-center" style={{ transform: 'rotate(8deg)' }}>
            <span className="text-white font-black text-[10px]" style={{ transform: 'rotate(-8deg)', display: 'block' }}>H</span>
          </div>
          <span className="text-white font-bold text-sm">Koramangala 4BHK</span>
        </div>
        <div className="flex -space-x-1.5">
          {[['S','#7c3aed'],['R','#4f46e5'],['P','#6366f1'],['A','#8b5cf6']].map(([l,c], i) => (
            <div key={i} className="w-6 h-6 rounded-full border border-[#0d0d1f] flex items-center justify-center text-[9px] font-bold text-white" style={{ background: c }}>{l}</div>
          ))}
        </div>
      </div>
      <div className="grid grid-cols-3 gap-2.5 px-4 py-3 border-b border-white/[0.05]">
        {[['4','Active'],['1','Overdue'],['Auto','Rotating']].map(([v,l]) => (
          <div key={l} className="bg-white/[0.04] rounded-xl px-2 py-2 text-center">
            <p className="text-white font-bold text-sm">{v}</p>
            <p className="text-white/30 text-[9px] font-medium mt-0.5">{l}</p>
          </div>
        ))}
      </div>
      <div className="flex-1 px-4 py-3 space-y-2">
        {tasks.map(t => (
          <div key={t.name} className={`border-l-[3px] rounded-xl px-3.5 py-2.5 flex items-center gap-3 ${sl[t.status]}`}>
            <div className="flex-1 min-w-0">
              <p className={`text-xs font-semibold leading-tight ${t.status === 'done' ? 'line-through text-white/30' : 'text-white'}`}>{t.name}</p>
              <p className="text-white/30 text-[10px] mt-0.5">{t.person}&apos;s turn</p>
            </div>
            <span className={`text-[9px] font-bold px-2 py-0.5 rounded-full shrink-0 ${bl[t.status]}`}>{t.due}</span>
          </div>
        ))}
      </div>
      <div className="flex items-center justify-around px-4 py-3 border-t border-white/[0.06] bg-[#111126]">
        {[['Home','violet'],['Stats',''],['Swaps',''],['Bills',''],['Settings','']].map(([l, a]) => (
          <button key={l} className={`flex flex-col items-center gap-0.5 ${a ? 'text-violet-400' : 'text-white/22'} cursor-pointer`}>
            <span className="text-[9px] font-semibold">{l}</span>
          </button>
        ))}
      </div>
    </div>
  )
}

// ── Product Showcase ──────────────────────────────────────────────────────────
function ProductShowcase() {
  return (
    <section className="bg-[#050510] overflow-hidden">
      <ContainerScroll
        titleComponent={
          <div className="mb-6">
            <span className="inline-block mb-4 px-3.5 py-1.5 rounded-full bg-violet-500/10 border border-violet-500/25 text-violet-400 text-[11px] font-bold uppercase tracking-[0.12em]">
              The product
            </span>
            <h2 className="text-4xl sm:text-5xl font-extrabold text-white tracking-tighter leading-tight">
              Everything your flat needs,<br />
              <span style={{ background: 'linear-gradient(130deg,#c4b5fd,#a78bfa,#6366f1)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', backgroundClip: 'text' }}>
                in one place.
              </span>
            </h2>
            <p className="text-white/40 text-lg mt-4 max-w-lg mx-auto font-medium">
              Real-time duties, expense splitting, swap requests — automated for every flatmate.
            </p>
          </div>
        }
      >
        <AppMockup />
      </ContainerScroll>
    </section>
  )
}

// ── Problems ──────────────────────────────────────────────────────────────────
const PROBS = [
  { icon: <Users size={28} className="text-red-400" />, t:'"I already did it last time"', b:'No record. No accountability. Resentment builds week by week until someone moves out or stops talking.' },
  { icon: <ClipboardList size={28} className="text-orange-400" />, t:'The WhatsApp graveyard', b:'Every duty reminder buried under 400 memes. Nobody saw it. Nobody acted. The same conversation next week.' },
  { icon: <BarChart2 size={28} className="text-rose-400" />, t:'One person carries everyone', b:'The responsible flatmate burns out managing everything. The flat suffers. Friendships get weird.' },
]

function Problems() {
  return (
    <section className="py-28 px-6 bg-[#050510] relative overflow-hidden">
      <div className="absolute top-20 left-10 w-96 h-96 rounded-full opacity-[0.045] pointer-events-none"
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
              <Card3D className="h-full" glowColor="rgba(239,68,68,0.12)">
                <div className="h-full bg-white/[0.025] border border-white/[0.07] rounded-2xl p-7 group cursor-default">
                  <div className="w-12 h-12 rounded-xl bg-red-500/10 border border-red-500/15 flex items-center justify-center mb-5">{p.icon}</div>
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

// ── Feature Showcase (AnimatedTabs) ──────────────────────────────────────────
const FEATURE_TABS: Tab[] = [
  {
    id: 'rotation',
    label: 'Duty Rotation',
    content: (
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 w-full h-full">
        <img
          src="https://images.unsplash.com/photo-1484154218962-a197022b5858?q=80&w=2074&auto=format&fit=crop&ixlib=rb-4.0.3"
          alt="Shared flat kitchen — duty rotation"
          className="rounded-xl w-full h-52 object-cover !m-0 shadow-[0_0_32px_rgba(124,58,237,0.2)] border border-white/[0.06]"
        />
        <div className="flex flex-col gap-3 justify-center">
          <div className="inline-flex items-center gap-2 px-2.5 py-1 rounded-full bg-violet-500/15 border border-violet-500/25 w-fit">
            <span className="w-1.5 h-1.5 rounded-full bg-violet-400" />
            <span className="text-violet-300 text-[11px] font-bold uppercase tracking-wider">Auto-assigned</span>
          </div>
          <h3 className="text-xl font-extrabold text-white leading-snug !m-0">
            Duties rotate.<br />Nobody decides.
          </h3>
          <p className="text-sm text-white/50 leading-relaxed">
            Every flatmate gets their turn automatically. Mark a task done and the next person in queue is assigned — instantly, fairly, with no arguments.
          </p>
          <ul className="space-y-1.5 mt-1">
            {['Zero manual scheduling','Skips out-of-station members','Full history & audit trail'].map(f => (
              <li key={f} className="flex items-center gap-2 text-xs text-white/45 font-medium">
                <span className="w-4 h-4 rounded-full bg-violet-500/20 flex items-center justify-center shrink-0">
                  <svg width="8" height="6" viewBox="0 0 9 7" fill="none"><path d="M1 3.5L3 5.5L8 1" stroke="#a78bfa" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round"/></svg>
                </span>
                {f}
              </li>
            ))}
          </ul>
        </div>
      </div>
    ),
  },
  {
    id: 'expenses',
    label: 'Bill Splitting',
    content: (
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 w-full h-full">
        <img
          src="https://images.unsplash.com/photo-1554224155-6726b3ff858f?q=80&w=2072&auto=format&fit=crop&ixlib=rb-4.0.3"
          alt="Expense splitting and bills"
          className="rounded-xl w-full h-52 object-cover !m-0 shadow-[0_0_32px_rgba(16,185,129,0.18)] border border-white/[0.06]"
        />
        <div className="flex flex-col gap-3 justify-center">
          <div className="inline-flex items-center gap-2 px-2.5 py-1 rounded-full bg-emerald-500/15 border border-emerald-500/25 w-fit">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
            <span className="text-emerald-300 text-[11px] font-bold uppercase tracking-wider">Split fairly</span>
          </div>
          <h3 className="text-xl font-extrabold text-white leading-snug !m-0">
            Bills split.<br />No awkwardness.
          </h3>
          <p className="text-sm text-white/50 leading-relaxed">
            Log electricity, groceries, rent. Split equally, by %, or custom per person. Everyone sees exactly who owes what — settle with one tap.
          </p>
          <ul className="space-y-1.5 mt-1">
            {['Equal · % · Custom splits','INR, USD, EUR + 4 more','One-tap settle-up'].map(f => (
              <li key={f} className="flex items-center gap-2 text-xs text-white/45 font-medium">
                <span className="w-4 h-4 rounded-full bg-emerald-500/20 flex items-center justify-center shrink-0">
                  <svg width="8" height="6" viewBox="0 0 9 7" fill="none"><path d="M1 3.5L3 5.5L8 1" stroke="#34d399" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round"/></svg>
                </span>
                {f}
              </li>
            ))}
          </ul>
        </div>
      </div>
    ),
  },
  {
    id: 'swaps',
    label: 'Swap System',
    content: (
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 w-full h-full">
        <img
          src="https://images.unsplash.com/photo-1521737711867-e3b97375f902?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3"
          alt="Flatmates using swap system"
          className="rounded-xl w-full h-52 object-cover !m-0 shadow-[0_0_32px_rgba(59,130,246,0.18)] border border-white/[0.06]"
        />
        <div className="flex flex-col gap-3 justify-center">
          <div className="inline-flex items-center gap-2 px-2.5 py-1 rounded-full bg-blue-500/15 border border-blue-500/25 w-fit">
            <span className="w-1.5 h-1.5 rounded-full bg-blue-400" />
            <span className="text-blue-300 text-[11px] font-bold uppercase tracking-wider">Formal flow</span>
          </div>
          <h3 className="text-xl font-extrabold text-white leading-snug !m-0">
            Can&apos;t do it today?<br />Swap, don&apos;t argue.
          </h3>
          <p className="text-sm text-white/50 leading-relaxed">
            Travelling or busy? Request a duty swap. Your flatmate gets notified, accepts or declines. Task moves officially. No WhatsApp drama needed.
          </p>
          <ul className="space-y-1.5 mt-1">
            {['Accept / decline notifications','Audit log records every swap','WhatsApp stays for memes'].map(f => (
              <li key={f} className="flex items-center gap-2 text-xs text-white/45 font-medium">
                <span className="w-4 h-4 rounded-full bg-blue-500/20 flex items-center justify-center shrink-0">
                  <svg width="8" height="6" viewBox="0 0 9 7" fill="none"><path d="M1 3.5L3 5.5L8 1" stroke="#60a5fa" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round"/></svg>
                </span>
                {f}
              </li>
            ))}
          </ul>
        </div>
      </div>
    ),
  },
  {
    id: 'analytics',
    label: 'Reliability Score',
    content: (
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 w-full h-full">
        <img
          src="https://images.unsplash.com/photo-1551288049-bebda4e38f71?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3"
          alt="Analytics and reliability scores"
          className="rounded-xl w-full h-52 object-cover !m-0 shadow-[0_0_32px_rgba(245,158,11,0.18)] border border-white/[0.06]"
        />
        <div className="flex flex-col gap-3 justify-center">
          <div className="inline-flex items-center gap-2 px-2.5 py-1 rounded-full bg-amber-500/15 border border-amber-500/25 w-fit">
            <span className="w-1.5 h-1.5 rounded-full bg-amber-400" />
            <span className="text-amber-300 text-[11px] font-bold uppercase tracking-wider">Data-driven</span>
          </div>
          <h3 className="text-xl font-extrabold text-white leading-snug !m-0">
            Facts, not feelings.<br />Scores don&apos;t lie.
          </h3>
          <p className="text-sm text-white/50 leading-relaxed">
            Every flatmate has a reliability score based on real completion data. No more "I always do more than you." The numbers settle it.
          </p>
          <ul className="space-y-1.5 mt-1">
            {['Completion rate per member','On-time vs overdue breakdown','6-month history grid'].map(f => (
              <li key={f} className="flex items-center gap-2 text-xs text-white/45 font-medium">
                <span className="w-4 h-4 rounded-full bg-amber-500/20 flex items-center justify-center shrink-0">
                  <svg width="8" height="6" viewBox="0 0 9 7" fill="none"><path d="M1 3.5L3 5.5L8 1" stroke="#fbbf24" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round"/></svg>
                </span>
                {f}
              </li>
            ))}
          </ul>
        </div>
      </div>
    ),
  },
]

function FeatureShowcase() {
  return (
    <section className="py-28 px-6 bg-[#050510] relative overflow-hidden">
      {/* Background glow */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[500px] pointer-events-none opacity-[0.06]"
        style={{ background: 'radial-gradient(ellipse, #7c3aed, transparent)', filter: 'blur(80px)' }} />

      <div className="max-w-[1120px] mx-auto relative z-10">
        <Reveal className="text-center mb-14">
          <span className="inline-block mb-4 px-3.5 py-1.5 rounded-full bg-violet-500/10 border border-violet-500/25 text-violet-400 text-[11px] font-bold uppercase tracking-[0.12em]">See it in action</span>
          <h2 className="text-4xl sm:text-[3.25rem] font-extrabold text-white tracking-tighter leading-tight">
            Every feature your flat needs,<br />
            <span style={{ background: 'linear-gradient(130deg,#c4b5fd,#a78bfa,#6366f1)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', backgroundClip: 'text' }}>
              one tap to explore.
            </span>
          </h2>
          <p className="text-white/38 text-lg mt-4 max-w-lg mx-auto font-medium">
            Switch between features — duties, bills, swaps, analytics. All built for Indian flatmates.
          </p>
        </Reveal>

        <Reveal delay={80} className="flex justify-center">
          <AnimatedTabs
            tabs={FEATURE_TABS}
            defaultTab="rotation"
            className="w-full max-w-3xl"
          />
        </Reveal>
      </div>
    </section>
  )
}

// ── Bridge ────────────────────────────────────────────────────────────────────
function Bridge() {
  return (
    <div className="relative overflow-hidden border-y border-white/[0.04]"
      style={{ background: 'linear-gradient(135deg,#0b0920 0%,#0f0d27 50%,#0b0920 100%)' }}>
      <div className="absolute inset-0 pointer-events-none"
        style={{ background: 'radial-gradient(ellipse 70% 80% at 50% 50%,rgba(124,58,237,0.09) 0%,transparent 100%)' }} />
      <Reveal className="max-w-3xl mx-auto text-center py-24 px-6">
        <p className="text-3xl sm:text-[2.75rem] font-extrabold text-white tracking-tighter leading-tight mb-5">
          There&apos;s a simpler way —{' '}
          <span style={{ background:'linear-gradient(90deg,#c4b5fd,#818cf8)', WebkitBackgroundClip:'text', WebkitTextFillColor:'transparent', backgroundClip:'text' }}>
            a system that runs itself.
          </span>
        </p>
        <p className="text-white/38 text-lg font-medium max-w-md mx-auto">No manager. No nagging. No whiteboard nobody updates. Just fairness, on autopilot.</p>
      </Reveal>
    </div>
  )
}

// ── Features ──────────────────────────────────────────────────────────────────
const FEATS = [
  {
    icon: <RotateCcw size={22} />, color: 'rgba(124,58,237,0.18)', border: 'rgba(124,58,237,0.25)', iconColor: '#a78bfa',
    l:'Rotation Engine', t:'Auto-assigns every duty, every time',
    b:'A rotation queue per task. Mark done → next person assigned instantly. Nobody decides. The system is the authority.',
    hi:'Zero manual scheduling',
  },
  {
    icon: <Zap size={22} />, color: 'rgba(234,179,8,0.14)', border: 'rgba(234,179,8,0.2)', iconColor: '#fbbf24',
    l:'Real-Time Sync', t:'Everyone sees everything instantly',
    b:'When Rahul marks Kitchen done at 7pm, every flatmate\'s screen updates in under a second. No refresh. No "did you do it?" texts.',
    hi:'Firestore live updates',
  },
  {
    icon: <Receipt size={22} />, color: 'rgba(16,185,129,0.14)', border: 'rgba(16,185,129,0.2)', iconColor: '#34d399',
    l:'Expense Splitting', t:'Split bills without the awkwardness',
    b:'Log groceries, electricity, rent. Split equally, by %, or custom per person. Everyone sees who owes whom. Settle with one tap.',
    hi:'Equal · % · Custom split',
  },
  {
    icon: <ArrowLeftRight size={22} />, color: 'rgba(59,130,246,0.14)', border: 'rgba(59,130,246,0.2)', iconColor: '#60a5fa',
    l:'Swap System', t:'Cover duties without the drama',
    b:'Travelling? Request a swap. Flatmate gets notified, accepts or declines. Task moves. Audit log records it. WhatsApp stays for memes.',
    hi:'Formal request flow',
  },
  {
    icon: <Shield size={22} />, color: 'rgba(236,72,153,0.14)', border: 'rgba(236,72,153,0.2)', iconColor: '#f472b6',
    l:'Audit Trail', t:'Every action permanently recorded',
    b:'Who did what. When. Whether it was on time. Six months later you can still check. No disputes. No "I don\'t remember." Just facts.',
    hi:'All members can see',
  },
  {
    icon: <Globe size={22} />, color: 'rgba(20,184,166,0.14)', border: 'rgba(20,184,166,0.2)', iconColor: '#2dd4bf',
    l:'Multi-Currency', t:'Works for international flatmates',
    b:'Log expenses in INR, USD, EUR, AED, SGD and more. Perfect for flats with people from different countries. 7 currencies supported.',
    hi:'7 currencies supported',
  },
]

function Features() {
  return (
    <section id="features" className="py-28 px-6 bg-[#050510] relative overflow-hidden">
      <div className="absolute bottom-20 right-10 w-[500px] h-[500px] rounded-full opacity-[0.04] pointer-events-none"
        style={{ background: 'radial-gradient(circle,#4338ca,transparent)', filter: 'blur(90px)' }} />
      <div className="max-w-[1120px] mx-auto">
        <Reveal className="text-center mb-20">
          <span className="inline-block mb-4 px-3.5 py-1.5 rounded-full bg-violet-500/10 border border-violet-500/25 text-violet-400 text-[11px] font-bold uppercase tracking-[0.12em]">Features</span>
          <h2 className="text-4xl sm:text-[3.25rem] font-extrabold text-white tracking-tighter">Everything your flat needs.</h2>
          <p className="text-white/35 text-lg mt-4 max-w-md mx-auto font-medium">Set it up once. Let it handle the rest, forever.</p>
        </Reveal>
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
          {FEATS.map((f, i) => (
            <Reveal key={f.l} delay={i * 65}>
              <Card3D className="h-full" intensity={9} glowColor={f.color}>
                <div className="h-full bg-white/[0.025] border border-white/[0.07] rounded-2xl p-7 group cursor-default hover:-translate-y-0.5 transition-transform duration-200">
                  <div className="w-12 h-12 rounded-xl flex items-center justify-center mb-5 shrink-0"
                    style={{ background: f.color, border: `1px solid ${f.border}`, color: f.iconColor }}>
                    {f.icon}
                  </div>
                  <span className="text-[11px] font-bold uppercase tracking-[0.12em]" style={{ color: f.iconColor }}>{f.l}</span>
                  <h3 className="text-white font-bold text-[1.05rem] mt-1.5 mb-2.5 leading-snug">{f.t}</h3>
                  <p className="text-white/38 text-sm leading-relaxed mb-5">{f.b}</p>
                  <div className="inline-flex items-center gap-1.5 rounded-full px-3 py-1" style={{ background: f.color, border: `1px solid ${f.border}` }}>
                    <Check size={9} style={{ color: f.iconColor }} />
                    <span className="text-[11px] font-semibold" style={{ color: f.iconColor }}>{f.hi}</span>
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

// ── Compare ───────────────────────────────────────────────────────────────────
const COMPARE_ROWS = [
  { label: 'Auto duty rotation',      habitiq: true, whatsapp: false, splitwise: false },
  { label: 'Shared expense splitting', habitiq: true, whatsapp: false, splitwise: true  },
  { label: 'Swap request system',     habitiq: true, whatsapp: false, splitwise: false },
  { label: 'Real-time sync',          habitiq: true, whatsapp: true,  splitwise: false },
  { label: 'Audit trail',             habitiq: true, whatsapp: false, splitwise: false },
  { label: 'Flat-specific context',   habitiq: true, whatsapp: false, splitwise: false },
]

function Compare() {
  return (
    <section id="compare" className="py-28 px-6 bg-[#050510] relative overflow-hidden">
      <div className="max-w-[860px] mx-auto">
        <Reveal className="text-center mb-16">
          <span className="inline-block mb-4 px-3.5 py-1.5 rounded-full bg-white/[0.04] border border-white/[0.08] text-white/35 text-[11px] font-bold uppercase tracking-[0.12em]">Why Habitiq</span>
          <h2 className="text-4xl sm:text-[3rem] font-extrabold text-white tracking-tighter">Purpose-built for shared living.</h2>
          <p className="text-white/35 text-lg mt-4 max-w-md mx-auto font-medium">WhatsApp is for chatting. Splitwise is for money. Neither runs your flat.</p>
        </Reveal>

        <Reveal delay={80}>
          <div className="rounded-2xl border border-white/[0.08] overflow-hidden bg-white/[0.015]">
            {/* Header row */}
            <div className="grid grid-cols-4 border-b border-white/[0.06] bg-white/[0.03]">
              <div className="px-6 py-4" />
              {[
                { name: 'Habitiq', highlight: true },
                { name: 'WhatsApp', highlight: false },
                { name: 'Splitwise', highlight: false },
              ].map(col => (
                <div key={col.name} className={`px-4 py-4 text-center ${col.highlight ? 'bg-violet-500/10 border-x border-violet-500/20' : ''}`}>
                  <p className={`text-sm font-bold ${col.highlight ? 'text-violet-300' : 'text-white/40'}`}>{col.name}</p>
                  {col.highlight && <p className="text-[10px] text-violet-400/70 font-medium mt-0.5">Built for flats</p>}
                </div>
              ))}
            </div>
            {/* Data rows */}
            {COMPARE_ROWS.map((row, i) => (
              <div key={row.label} className={`grid grid-cols-4 border-b border-white/[0.04] last:border-0 ${i % 2 === 0 ? '' : 'bg-white/[0.015]'}`}>
                <div className="px-6 py-3.5 flex items-center">
                  <p className="text-sm text-white/55 font-medium">{row.label}</p>
                </div>
                {[row.habitiq, row.whatsapp, row.splitwise].map((has, j) => (
                  <div key={j} className={`px-4 py-3.5 flex items-center justify-center ${j === 0 ? 'bg-violet-500/[0.06] border-x border-violet-500/[0.12]' : ''}`}>
                    {has
                      ? <div className={`w-6 h-6 rounded-full flex items-center justify-center ${j === 0 ? 'bg-violet-500/20' : 'bg-white/[0.06]'}`}>
                          <Check size={12} className={j === 0 ? 'text-violet-400' : 'text-white/35'} />
                        </div>
                      : <div className="w-6 h-6 rounded-full bg-white/[0.03] flex items-center justify-center">
                          <X size={11} className="text-white/18" />
                        </div>
                    }
                  </div>
                ))}
              </div>
            ))}
          </div>
        </Reveal>
      </div>
    </section>
  )
}

// ── How it works ──────────────────────────────────────────────────────────────
const STEPS = [
  { n:'01', t:'Create your flat', b:'Sign up, name your flat, get a 6-digit invite code. Done in 45 seconds.', color: '#7c3aed' },
  { n:'02', t:'Flatmates join',   b:'Share the invite code. Everyone joins with one tap. Auto-added to all queues.', color: '#4f46e5' },
  { n:'03', t:'System runs itself', b:'Duties rotate automatically. Bills split fairly. Nobody manages anything.', color: '#6366f1' },
]

function HowItWorks() {
  return (
    <section id="how-it-works" className="py-28 px-6 bg-[#080815]">
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
              <Card3D intensity={7} className="text-center">
                <div className="bg-white/[0.02] border border-white/[0.06] rounded-2xl p-8">
                  <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl text-white font-black text-base mb-6"
                    style={{ background: `linear-gradient(135deg, ${s.color}, #4338ca)`, boxShadow: `0 8px 28px ${s.color}55` }}>
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
    <section className="py-20 px-6 relative overflow-hidden"
      style={{ background: 'linear-gradient(135deg, #0d0b22 0%, #0f0d2a 50%, #0d0b22 100%)' }}>
      <div className="absolute inset-0 pointer-events-none"
        style={{ background: 'radial-gradient(ellipse 60% 80% at 50% 50%, rgba(124,58,237,0.07) 0%, transparent 100%)' }} />
      <div className="max-w-[1120px] mx-auto relative z-10">
        <Reveal>
          <div className="grid grid-cols-3 gap-6 sm:gap-12 text-center">
            {[
              { n:'50M+', l:'People in shared accommodation in India', color: '#a78bfa' },
              { n:'< 2 min', l:'Average flat setup time', color: '#818cf8' },
              { n:'100%', l:'Free during trial — no card needed', color: '#c4b5fd' },
            ].map(({ n, l, color }) => (
              <div key={l}>
                <p className="font-extrabold tracking-tighter mb-2"
                  style={{ fontSize:'clamp(2rem,4vw,3.25rem)', background:`linear-gradient(135deg, ${color}, #6366f1)`, WebkitBackgroundClip:'text', WebkitTextFillColor:'transparent', backgroundClip:'text' }}>
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
const HABITIQ_TESTIMONIALS: Testimonial[] = [
  {
    id: 1,
    quote: "Finally something that doesn't need one person to run it. Set up in 5 minutes, haven't argued about chores since.",
    name: 'Koramangala Flat',
    username: 'Flat of 4 · Bengaluru',
    avatar: '',
    avatarColor: '#7c3aed',
    avatarInitial: 'K',
  },
  {
    id: 2,
    quote: "The expense splitting is the best part. Everyone can see exactly what they owe. No more awkward money conversations.",
    name: 'Banjara Hills PG',
    username: 'PG residents · Hyderabad',
    avatar: '',
    avatarColor: '#4f46e5',
    avatarInitial: 'B',
  },
  {
    id: 3,
    quote: "The swap request feature saved my life during exam week. Just tap, flatmate accepts, done. Zero drama.",
    name: 'Student Flat',
    username: 'Flat of 3 · Pune',
    avatar: '',
    avatarColor: '#6366f1',
    avatarInitial: 'P',
  },
  {
    id: 4,
    quote: "Rotation engine is genius. My flatmates stopped fighting about who does dishes. It just works.",
    name: 'Indiranagar House',
    username: 'House of 5 · Bengaluru',
    avatar: '',
    avatarColor: '#8b5cf6',
    avatarInitial: 'I',
  },
  {
    id: 5,
    quote: "Added our electricity and WiFi bills. Now everyone pays on time because they can see it clearly.",
    name: 'HSR Layout Flat',
    username: 'Flat of 4 · Bengaluru',
    avatar: '',
    avatarColor: '#a78bfa',
    avatarInitial: 'H',
  },
]

function Testimonials() {
  return (
    <section className="py-20 bg-[#050510] relative overflow-hidden">
      {/* Subtle glow */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[700px] h-[400px] pointer-events-none opacity-[0.05]"
        style={{ background: 'radial-gradient(ellipse, #7c3aed, transparent)', filter: 'blur(60px)' }} />

      <div className="relative z-10">
        <Reveal className="text-center mb-14 px-6">
          <span className="inline-block mb-4 px-3.5 py-1.5 rounded-full bg-white/[0.04] border border-white/[0.08] text-white/30 text-[11px] font-bold uppercase tracking-[0.12em]">Early users</span>
          <h2 className="text-4xl font-extrabold text-white tracking-tighter">Real flats. Real results.</h2>
          <p className="text-white/35 text-lg mt-3 font-medium">From Bengaluru to Pune — flatmates who stopped arguing.</p>
        </Reveal>
        <TestimonialSlider
          testimonials={HABITIQ_TESTIMONIALS}
          className="bg-transparent"
        />
      </div>
    </section>
  )
}

// ── Get started ───────────────────────────────────────────────────────────────
function GetStarted() {
  return (
    <section id="get-started" className="py-28 px-6 relative overflow-hidden" style={{ background:'linear-gradient(180deg,#0a0818 0%,#050510 100%)' }}>
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[800px] h-[400px] opacity-[0.07] pointer-events-none"
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
            <div className="space-y-3.5">
              {['Unlimited duty rotation & expense splitting','Real-time sync across all flatmates','Swap system, activity log, analytics','Works on any phone — no app install needed'].map(item => (
                <div key={item} className="flex items-center gap-3">
                  <div className="w-5 h-5 rounded-full flex items-center justify-center shrink-0" style={{ background:'rgba(124,58,237,0.22)', border:'1px solid rgba(124,58,237,0.3)' }}>
                    <Check size={10} className="text-violet-400" />
                  </div>
                  <span className="text-white/50 text-sm font-medium">{item}</span>
                </div>
              ))}
            </div>
          </Reveal>
          <Reveal delay={110}>
            <Card3D intensity={5} glowColor="rgba(124,58,237,0.14)" className="w-full">
              <div className="rounded-2xl border border-white/[0.09] overflow-hidden"
                style={{ background:'rgba(255,255,255,0.02)', boxShadow:'0 0 0 1px rgba(124,58,237,0.1), 0 24px 72px rgba(124,58,237,0.12)' }}>
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
    <footer className="border-t border-white/[0.05] bg-[#050510] pt-14 pb-8 px-6">
      <div className="max-w-[1120px] mx-auto">
        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-10 mb-12">
          {/* Brand */}
          <div className="lg:col-span-2">
            <div className="flex items-center gap-2.5 mb-4">
              <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-violet-600 to-indigo-600 flex items-center justify-center" style={{ transform:'rotate(8deg)', boxShadow:'0 4px 12px rgba(124,58,237,0.4)' }}>
                <span className="text-white font-black text-xs" style={{ transform:'rotate(-8deg)', display:'block' }}>H</span>
              </div>
              <span className="text-white font-bold text-[15px] tracking-tight">Habitiq</span>
            </div>
            <p className="text-white/28 text-sm leading-relaxed max-w-xs font-medium">
              The operating system for shared flats in India. Duties rotate. Bills split. Flatmates stay happy.
            </p>
            <p className="text-white/16 text-xs mt-5 font-medium">Made with care in India 🇮🇳</p>
          </div>
          {/* Product */}
          <div>
            <p className="text-white/40 text-[11px] font-bold uppercase tracking-[0.12em] mb-4">Product</p>
            <div className="space-y-3">
              {[['#features','Features'],['#how-it-works','How it works'],['#compare','Compare'],['#get-started','Get started']].map(([h,l]) => (
                <a key={h} href={h} className="block text-white/28 text-sm hover:text-white/55 transition-colors">{l}</a>
              ))}
            </div>
          </div>
          {/* Company */}
          <div>
            <p className="text-white/40 text-[11px] font-bold uppercase tracking-[0.12em] mb-4">Company</p>
            <div className="space-y-3">
              {[['#','About'],['#','Privacy'],['#','Contact']].map(([h,l]) => (
                <a key={l} href={h} className="block text-white/28 text-sm hover:text-white/55 transition-colors">{l}</a>
              ))}
            </div>
          </div>
        </div>
        <div className="border-t border-white/[0.05] pt-6 flex flex-col sm:flex-row items-center justify-between gap-3">
          <p className="text-white/15 text-xs">© 2026 Habitiq. All rights reserved.</p>
          <p className="text-white/15 text-xs">Shared living, managed.</p>
        </div>
      </div>
    </footer>
  )
}

// ── Page ──────────────────────────────────────────────────────────────────────
export default function LandingPage() {
  return (
    <div className={`${jakarta.className} min-h-screen bg-[#050510] antialiased`}>
      <style>{`
        @keyframes mq { from { transform:translateX(0) } to { transform:translateX(-50%) } }
      `}</style>
      <Header />
      <main>
        <Hero />
        <Marquee />
        <ProductShowcase />
        <FeatureShowcase />
        <Problems />
        <Bridge />
        <Features />
        <Compare />
        <HowItWorks />
        <Stats />
        <Testimonials />
        <GetStarted />
      </main>
      <Footer />
    </div>
  )
}
