'use client'

import { useEffect, useRef, useState, ReactNode } from 'react'
import { motion } from 'framer-motion'
import { Navbar, AuthForm } from '@/components/ui/navbar'
import { Check, ArrowRight, Star, X } from 'lucide-react'
import TestimonialSlider, { type Testimonial } from '@/components/ui/testimonial-slider'

// ── Scroll reveal ──────────────────────────────────────────────────────────────
function useReveal() {
  const ref = useRef<HTMLDivElement>(null)
  const [v, setV] = useState(false)
  useEffect(() => {
    const el = ref.current; if (!el) return
    const obs = new IntersectionObserver(
      ([e]) => { if (e.isIntersecting) { setV(true); obs.disconnect() } },
      { threshold: 0.08 }
    )
    obs.observe(el)
    return () => obs.disconnect()
  }, [])
  return { ref, v }
}

function Reveal({ children, delay = 0, className = '' }: {
  children: ReactNode; delay?: number; className?: string
}) {
  const { ref, v } = useReveal()
  return (
    <div ref={ref} className={className} style={{
      opacity: v ? 1 : 0,
      transform: v ? 'none' : 'translateY(20px)',
      transition: `opacity .65s cubic-bezier(.22,1,.36,1) ${delay}ms, transform .65s cubic-bezier(.22,1,.36,1) ${delay}ms`,
    }}>{children}</div>
  )
}

// ── Design tokens ──────────────────────────────────────────────────────────────
const BG = '#09090B'
const SURFACE = '#111116'
const BORDER = 'rgba(255,255,255,0.07)'
const BLUE = '#3b82f6'
const BLUE_DIM = 'rgba(59,130,246,0.1)'
const BLUE_GLOW = 'rgba(59,130,246,0.22)'

// ── App preview (hero mockup) ──────────────────────────────────────────────────
function AppPreview() {
  const tasks = [
    { name: 'Bathroom Cleaning', person: 'Rahul', status: 'overdue', label: 'Overdue 1d' },
    { name: 'Vacuum Living Room', person: 'Priya', status: 'today', label: 'Due today' },
    { name: 'Take out Trash', person: 'Ankit', status: 'soon', label: 'Tomorrow' },
    { name: 'Kitchen Dishes', person: 'Meera', status: 'done', label: 'Done ✓' },
  ]
  const s: Record<string, { bar: string; bg: string; text: string }> = {
    overdue: { bar: '#ef4444', bg: 'rgba(239,68,68,0.08)',  text: '#f87171' },
    today:   { bar: '#f59e0b', bg: 'rgba(245,158,11,0.08)', text: '#fbbf24' },
    soon:    { bar: BLUE,      bg: BLUE_DIM,                text: '#60a5fa' },
    done:    { bar: '#10b981', bg: 'rgba(16,185,129,0.08)', text: '#34d399' },
  }
  return (
    <div style={{ background: '#0c0c10', minHeight: '300px' }}>
      <div className="flex items-center justify-between px-5 py-3 border-b" style={{ borderColor: BORDER, background: SURFACE }}>
        <div className="flex items-center gap-2.5">
          <div className="w-6 h-6 rounded-lg flex items-center justify-center" style={{ background: 'linear-gradient(135deg,#2563eb,#3b82f6)' }}>
            <span className="text-white font-black text-[9px]">H</span>
          </div>
          <span className="text-white font-semibold text-sm">Koramangala 4BHK</span>
        </div>
        <div className="flex -space-x-1.5">
          {[['S','#1d4ed8'],['R','#2563eb'],['P','#3b82f6'],['A','#60a5fa']].map(([l,c],i) => (
            <div key={i} className="w-6 h-6 rounded-full border-2 flex items-center justify-center text-[9px] font-bold text-white"
              style={{ background: c, borderColor: SURFACE }}>{l}</div>
          ))}
        </div>
      </div>
      <div className="grid grid-cols-3 gap-2 px-4 py-3 border-b" style={{ borderColor: 'rgba(255,255,255,0.04)' }}>
        {[['4','Active'],['1','Overdue'],['Auto','Rotating']].map(([v,l]) => (
          <div key={l} className="rounded-xl px-2 py-2.5 text-center" style={{ background: 'rgba(255,255,255,0.03)' }}>
            <p className="text-white font-bold text-sm">{v}</p>
            <p className="text-[9px] mt-0.5 font-medium" style={{ color: 'rgba(255,255,255,0.28)' }}>{l}</p>
          </div>
        ))}
      </div>
      <div className="px-4 py-3 space-y-2">
        {tasks.map(t => {
          const c = s[t.status]
          return (
            <div key={t.name} className="flex items-center gap-3 rounded-xl px-3.5 py-2.5 border-l-2"
              style={{ background: c.bg, borderLeftColor: c.bar }}>
              <div className="flex-1 min-w-0">
                <p className="text-xs font-semibold leading-tight"
                  style={{ color: t.status === 'done' ? 'rgba(255,255,255,0.28)' : 'white', textDecoration: t.status === 'done' ? 'line-through' : 'none' }}>
                  {t.name}
                </p>
                <p className="text-[10px] mt-0.5" style={{ color: 'rgba(255,255,255,0.28)' }}>{t.person}&apos;s turn</p>
              </div>
              <span className="text-[9px] font-bold px-2 py-0.5 rounded-full shrink-0" style={{ background: c.bg, color: c.text }}>{t.label}</span>
            </div>
          )
        })}
      </div>
      <div className="flex items-center justify-around px-4 py-3 border-t" style={{ borderColor: BORDER, background: SURFACE }}>
        {[['Home', true],['Expenses', false],['Swaps', false],['Bills', false]].map(([l, active]) => (
          <button key={l as string} className="flex flex-col items-center gap-0.5 text-[9px] font-semibold cursor-pointer"
            style={{ color: active ? BLUE : 'rgba(255,255,255,0.2)' }}>
            <span className="w-4 h-0.5 rounded-full mb-0.5" style={{ background: active ? BLUE : 'transparent' }} />
            {l}
          </button>
        ))}
      </div>
    </div>
  )
}

// ── Hero ──────────────────────────────────────────────────────────────────────
function Hero() {
  return (
    <section className="relative w-full overflow-hidden" style={{ background: BG, minHeight: '100svh' }}>
      <div className="absolute inset-0 pointer-events-none" style={{
        backgroundImage: `linear-gradient(rgba(255,255,255,0.026) 1px,transparent 1px),linear-gradient(90deg,rgba(255,255,255,0.026) 1px,transparent 1px)`,
        backgroundSize: '52px 52px',
      }} />
      <div className="absolute pointer-events-none" style={{
        top: '-25%', left: '25%', width: '800px', height: '700px',
        background: `radial-gradient(ellipse, ${BLUE_GLOW} 0%, transparent 60%)`,
        filter: 'blur(70px)',
      }} />

      <div className="relative z-10 max-w-[1240px] mx-auto px-6 flex flex-col lg:flex-row items-center gap-16"
        style={{ minHeight: '100svh', paddingTop: '88px', paddingBottom: '72px' }}>

        {/* Left */}
        <div className="flex-1 max-w-[560px] text-center lg:text-left">
          <motion.div
            initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.45 }}
            className="inline-flex items-center gap-2 mb-7 px-3 py-1.5 rounded-full border"
            style={{ borderColor: 'rgba(59,130,246,0.28)', background: BLUE_DIM }}>
            <span className="w-1.5 h-1.5 rounded-full animate-pulse" style={{ background: '#34d399' }} />
            <span className="text-[11px] font-semibold uppercase tracking-[0.11em]" style={{ color: '#60a5fa' }}>
              Free trial · No card needed
            </span>
          </motion.div>

          <motion.h1
            initial={{ opacity: 0, y: 22 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.7, delay: 0.08 }}
            className="font-bold text-white leading-[1.01] mb-5"
            style={{ fontSize: 'clamp(2.8rem, 5.5vw, 5rem)', letterSpacing: '-0.03em', fontFamily: 'var(--font-inter)' }}>
            Your flat, finally<br />
            <span style={{
              background: 'linear-gradient(135deg, #93c5fd 0%, #3b82f6 45%, #1d4ed8 100%)',
              WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', backgroundClip: 'text',
            }}>running itself.</span>
          </motion.h1>

          <motion.p
            initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.65, delay: 0.2 }}
            className="text-lg leading-relaxed mb-9 max-w-[440px] mx-auto lg:mx-0"
            style={{ color: 'rgba(255,255,255,0.46)', fontFamily: 'var(--font-inter)' }}>
            Duties rotate automatically. Bills split fairly. No WhatsApp arguments — just a flat that works.
          </motion.p>

          <motion.div
            initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6, delay: 0.3 }}
            className="flex flex-wrap gap-3 mb-10 justify-center lg:justify-start">
            <a href="#get-started"
              className="inline-flex items-center gap-2 text-white font-semibold text-sm h-11 px-6 rounded-xl active:scale-[0.97] transition-all cursor-pointer"
              style={{ background: BLUE, boxShadow: `0 0 0 1px rgba(59,130,246,0.4), 0 8px 24px rgba(59,130,246,0.32)` }}>
              Get Started Free <ArrowRight size={14} />
            </a>
            <a href="#features"
              className="inline-flex items-center gap-2 text-sm h-11 px-5 rounded-xl border transition-all cursor-pointer"
              style={{ borderColor: BORDER, color: 'rgba(255,255,255,0.48)', fontFamily: 'var(--font-inter)' }}>
              See how it works
            </a>
          </motion.div>

          <motion.div
            initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.6, delay: 0.46 }}
            className="flex items-center gap-3.5 justify-center lg:justify-start">
            <div className="flex -space-x-2">
              {[['S','#1d4ed8'],['R','#2563eb'],['P','#3b82f6'],['A','#60a5fa'],['M','#93c5fd']].map(([l,c],i) => (
                <div key={i} className="w-8 h-8 rounded-full border-2 flex items-center justify-center text-[10px] font-bold text-white"
                  style={{ background: c, borderColor: BG }}>{l}</div>
              ))}
            </div>
            <div>
              <div className="flex gap-0.5 mb-1">
                {[...Array(5)].map((_,j) => <Star key={j} size={10} fill={BLUE} style={{ color: BLUE }} />)}
              </div>
              <p className="text-xs" style={{ color: 'rgba(255,255,255,0.28)' }}>Trusted by 20+ flats in India</p>
            </div>
          </motion.div>
        </div>

        {/* Right: mockup */}
        <div className="flex-1 hidden lg:flex justify-end max-w-[500px] w-full">
          <motion.div
            initial={{ opacity: 0, y: 32 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.9, delay: 0.28 }}
            className="relative w-full max-w-[440px]">
            <motion.div animate={{ y: [-4, 4, -4] }} transition={{ duration: 3.5, repeat: Infinity, ease: 'easeInOut' }}
              className="absolute -left-10 top-12 z-20 rounded-2xl px-4 py-3 backdrop-blur-xl shadow-2xl"
              style={{ background: 'rgba(17,17,22,0.92)', border: `1px solid ${BORDER}`, minWidth: '140px' }}>
              <p className="text-[10px] font-medium" style={{ color: 'rgba(255,255,255,0.35)' }}>This month</p>
              <p className="text-[1.1rem] font-bold text-white leading-tight">₹4,850</p>
              <p className="text-[10px] font-bold mt-0.5" style={{ color: '#34d399' }}>↓ 12% vs last</p>
            </motion.div>
            <motion.div animate={{ y: [4, -4, 4] }} transition={{ duration: 4, repeat: Infinity, ease: 'easeInOut', delay: 1 }}
              className="absolute -right-8 bottom-20 z-20 rounded-2xl px-4 py-3 backdrop-blur-xl shadow-2xl"
              style={{ background: 'rgba(17,17,22,0.92)', border: `1px solid ${BORDER}`, minWidth: '130px' }}>
              <p className="text-[10px] font-medium" style={{ color: 'rgba(255,255,255,0.35)' }}>Rahul&apos;s turn</p>
              <p className="text-sm font-semibold text-white mt-0.5">Vacuum Living Room</p>
              <div className="flex items-center gap-1.5 mt-1.5">
                <span className="w-1.5 h-1.5 rounded-full animate-pulse" style={{ background: '#f59e0b' }} />
                <p className="text-[10px] font-bold" style={{ color: '#f59e0b' }}>Due today</p>
              </div>
            </motion.div>
            <div className="rounded-2xl overflow-hidden" style={{
              boxShadow: `0 0 0 1px ${BORDER}, 0 32px 80px rgba(0,0,0,0.7), 0 0 60px rgba(59,130,246,0.1)`,
            }}>
              <div className="flex items-center gap-2.5 px-4 py-3 border-b" style={{ background: SURFACE, borderColor: BORDER }}>
                <div className="flex gap-1.5">
                  <div className="w-2.5 h-2.5 rounded-full bg-[#ff5f57]" />
                  <div className="w-2.5 h-2.5 rounded-full bg-[#febc2e]" />
                  <div className="w-2.5 h-2.5 rounded-full bg-[#28c840]" />
                </div>
                <div className="flex-1 h-5 rounded-md flex items-center px-2.5" style={{ background: 'rgba(255,255,255,0.04)' }}>
                  <span className="text-[10px]" style={{ color: 'rgba(255,255,255,0.18)' }}>habitiq.app</span>
                </div>
              </div>
              <AppPreview />
            </div>
          </motion.div>
        </div>
      </div>
    </section>
  )
}

// ── Feature strip ─────────────────────────────────────────────────────────────
const STRIP = ['Duty Rotation','Expense Splitting','Real-Time Sync','Swap Requests','Audit Trail','Bill Splitting','Settlement Tracking','Fair Rotation','Auto-Assignment']

function FeatureStrip() {
  const items = [...STRIP, ...STRIP]
  return (
    <div className="overflow-hidden border-y" style={{ borderColor: 'rgba(255,255,255,0.05)', background: 'rgba(255,255,255,0.01)', padding: '12px 0' }}>
      <div className="flex" style={{ animation: 'mq 30s linear infinite', width: 'max-content' }}>
        {items.map((t, i) => (
          <div key={i} className="flex items-center gap-3.5 shrink-0 px-4">
            <span className="w-1 h-1 rounded-full" style={{ background: BLUE, opacity: 0.4 }} />
            <span className="text-[11px] font-semibold uppercase tracking-[0.14em] whitespace-nowrap"
              style={{ color: 'rgba(255,255,255,0.2)' }}>{t}</span>
          </div>
        ))}
      </div>
    </div>
  )
}

// ── Feature mockups ───────────────────────────────────────────────────────────
function DutyMockup() {
  return (
    <div className="rounded-2xl overflow-hidden" style={{ background: SURFACE, border: `1px solid ${BORDER}` }}>
      <div className="px-5 py-4 border-b flex items-center justify-between" style={{ borderColor: BORDER }}>
        <span className="text-sm font-semibold text-white">Today&apos;s Duties</span>
        <span className="text-[11px] font-medium px-2.5 py-1 rounded-full" style={{ background: BLUE_DIM, color: '#60a5fa' }}>Auto-rotating</span>
      </div>
      <div className="p-4 space-y-2.5">
        {[
          { task: 'Clean Bathrooms',  person: 'Rahul', avatar: 'R', color: '#2563eb', done: false },
          { task: 'Mop Kitchen Floor', person: 'Priya', avatar: 'P', color: '#3b82f6', done: true  },
          { task: 'Take Out Trash',   person: 'Ankit', avatar: 'A', color: '#1d4ed8', done: false },
          { task: 'Grocery Run',      person: 'Sai',   avatar: 'S', color: '#60a5fa', done: true  },
        ].map(item => (
          <div key={item.task} className="flex items-center gap-3 p-3 rounded-xl" style={{ background: 'rgba(255,255,255,0.03)' }}>
            <div className="w-7 h-7 rounded-full flex items-center justify-center text-[10px] font-bold text-white shrink-0"
              style={{ background: item.color }}>{item.avatar}</div>
            <div className="flex-1 min-w-0">
              <p className="text-xs font-medium text-white"
                style={{ opacity: item.done ? 0.3 : 1, textDecoration: item.done ? 'line-through' : 'none' }}>{item.task}</p>
              <p className="text-[10px] mt-0.5" style={{ color: 'rgba(255,255,255,0.3)' }}>{item.person}&apos;s turn</p>
            </div>
            <div className="w-5 h-5 rounded-full border flex items-center justify-center"
              style={{ background: item.done ? 'rgba(16,185,129,0.15)' : 'transparent', borderColor: item.done ? '#10b981' : 'rgba(255,255,255,0.15)' }}>
              {item.done && <Check size={10} className="text-emerald-400" />}
            </div>
          </div>
        ))}
      </div>
      <div className="px-5 py-3 border-t flex items-center justify-between" style={{ borderColor: 'rgba(255,255,255,0.04)' }}>
        <span className="text-xs" style={{ color: 'rgba(255,255,255,0.28)' }}>Rotates automatically tomorrow</span>
        <span className="text-[11px] font-semibold cursor-pointer" style={{ color: BLUE }}>View history →</span>
      </div>
    </div>
  )
}

function ExpenseMockup() {
  return (
    <div className="rounded-2xl overflow-hidden" style={{ background: SURFACE, border: `1px solid ${BORDER}` }}>
      <div className="px-5 py-4 border-b flex items-center justify-between" style={{ borderColor: BORDER }}>
        <span className="text-sm font-semibold text-white">Expenses · June</span>
        <span className="text-[11px] font-semibold" style={{ color: '#34d399' }}>₹12,400 total</span>
      </div>
      <div className="p-4 space-y-2.5">
        {[
          { label: 'Electricity Bill',    paid: 'Rahul', amount: '₹3,200', split: '₹800 × 4', settled: true  },
          { label: 'Monthly Groceries',   paid: 'Priya', amount: '₹4,100', split: '₹1,025 × 4', settled: false },
          { label: 'WiFi Recharge',       paid: 'Sai',   amount: '₹1,200', split: '₹300 × 4', settled: false },
        ].map(item => (
          <div key={item.label} className="p-3 rounded-xl" style={{ background: 'rgba(255,255,255,0.025)' }}>
            <div className="flex items-center justify-between mb-2">
              <p className="text-xs font-semibold text-white">{item.label}</p>
              <p className="text-xs font-bold text-white">{item.amount}</p>
            </div>
            <div className="flex items-center justify-between">
              <p className="text-[10px]" style={{ color: 'rgba(255,255,255,0.3)' }}>Paid by {item.paid} · {item.split}</p>
              <button className="text-[10px] font-semibold px-2.5 py-1 rounded-full cursor-pointer"
                style={{ background: item.settled ? 'rgba(16,185,129,0.12)' : BLUE_DIM, color: item.settled ? '#34d399' : '#60a5fa' }}>
                {item.settled ? 'Settled ✓' : 'Settle'}
              </button>
            </div>
          </div>
        ))}
      </div>
      <div className="px-5 py-3 border-t" style={{ borderColor: 'rgba(255,255,255,0.04)' }}>
        <div className="flex items-center gap-2">
          <div className="flex-1 h-1.5 rounded-full overflow-hidden" style={{ background: 'rgba(255,255,255,0.06)' }}>
            <div className="h-full rounded-full" style={{ width: '33%', background: '#10b981' }} />
          </div>
          <span className="text-[10px]" style={{ color: 'rgba(255,255,255,0.32)' }}>1 of 3 settled</span>
        </div>
      </div>
    </div>
  )
}

function SwapMockup() {
  return (
    <div className="rounded-2xl overflow-hidden" style={{ background: SURFACE, border: `1px solid ${BORDER}` }}>
      <div className="px-5 py-4 border-b" style={{ borderColor: BORDER }}>
        <span className="text-sm font-semibold text-white">Swap Requests</span>
      </div>
      <div className="p-4 space-y-3">
        <div className="p-4 rounded-xl" style={{ background: 'rgba(59,130,246,0.06)', border: `1px solid rgba(59,130,246,0.18)` }}>
          <div className="flex items-start gap-3 mb-3">
            <div className="w-8 h-8 rounded-full flex items-center justify-center text-[10px] font-bold text-white shrink-0"
              style={{ background: '#2563eb' }}>R</div>
            <div>
              <p className="text-xs font-semibold text-white">Rahul wants to swap</p>
              <p className="text-[10px] mt-0.5" style={{ color: 'rgba(255,255,255,0.38)' }}>Clean Bathrooms → Vacuum Living Room</p>
              <p className="text-[10px] mt-0.5" style={{ color: 'rgba(255,255,255,0.26)' }}>Jun 15 → Jun 17</p>
            </div>
          </div>
          <div className="flex gap-2">
            <button className="flex-1 h-7 rounded-lg text-[11px] font-semibold text-white cursor-pointer"
              style={{ background: BLUE }}>Accept</button>
            <button className="h-7 px-3 rounded-lg text-[11px] font-semibold cursor-pointer border"
              style={{ borderColor: BORDER, color: 'rgba(255,255,255,0.38)' }}>Decline</button>
          </div>
        </div>
        <div className="p-3 rounded-xl" style={{ background: 'rgba(16,185,129,0.06)', border: '1px solid rgba(16,185,129,0.14)' }}>
          <div className="flex items-center gap-2.5">
            <div className="w-6 h-6 rounded-full flex items-center justify-center text-[9px] font-bold text-white shrink-0"
              style={{ background: '#0d9488' }}>P</div>
            <div className="flex-1">
              <p className="text-[11px] font-medium text-white">Priya&apos;s swap accepted · Jun 10</p>
              <p className="text-[10px]" style={{ color: 'rgba(255,255,255,0.3)' }}>Kitchen Dishes ↔ Grocery Run</p>
            </div>
            <span className="text-[10px] font-bold" style={{ color: '#34d399' }}>✓ Done</span>
          </div>
        </div>
        <div className="p-3 rounded-xl" style={{ background: 'rgba(255,255,255,0.02)' }}>
          <div className="flex items-center gap-2.5">
            <div className="w-6 h-6 rounded-full flex items-center justify-center text-[9px] font-bold text-white shrink-0"
              style={{ background: '#4f46e5' }}>A</div>
            <div className="flex-1">
              <p className="text-[11px] font-medium text-white">Ankit requested swap · Jun 8</p>
              <p className="text-[10px]" style={{ color: 'rgba(255,255,255,0.3)' }}>Take Out Trash ↔ Cook Dinner</p>
            </div>
            <span className="text-[10px] font-bold" style={{ color: 'rgba(255,255,255,0.3)' }}>Declined</span>
          </div>
        </div>
      </div>
    </div>
  )
}

// ── Feature sections ──────────────────────────────────────────────────────────
const FEATURES = [
  {
    badge: 'Duty Rotation',
    headline: 'Nobody has to manage the rota.',
    body: 'Add your household tasks once. Set the rotation. Habitiq handles the rest — automatically assigning the next person in queue when a task is completed. Fair, transparent, zero arguments.',
    bullets: ['Skips out-of-town members', 'Full completion history & audit trail', 'Admin can adjust dates retroactively'],
    color: BLUE,
    Mockup: DutyMockup,
    flip: false,
  },
  {
    badge: 'Expense Splitting',
    headline: 'Bills split. Balances clear.',
    body: 'Log any expense, pick who paid, choose your split. Habitiq calculates who owes what — and lets everyone settle with one tap. No spreadsheets. No awkward reminder texts.',
    bullets: ['Equal, %, or custom splits', 'Multiple currencies incl. INR', 'One-tap settlement with history'],
    color: '#10b981',
    Mockup: ExpenseMockup,
    flip: true,
  },
  {
    badge: 'Swap Requests',
    headline: "Can't make it? Just swap.",
    body: "Travelling or busy? Send a swap request. Your flatmate gets notified, accepts or declines. The system records it officially. No group-chat chaos, no misremembered promises.",
    bullets: ['Formal accept / decline flow', 'Audit log records every swap', 'Works across time zones'],
    color: '#a78bfa',
    Mockup: SwapMockup,
    flip: false,
  },
]

function Features() {
  return (
    <section id="features" className="py-24 px-6" style={{ background: BG }}>
      <div className="max-w-[1200px] mx-auto space-y-32">
        {FEATURES.map((f) => (
          <Reveal key={f.badge}>
            <div className={`flex flex-col ${f.flip ? 'lg:flex-row-reverse' : 'lg:flex-row'} items-center gap-12 lg:gap-20`}>
              <div className="flex-1 max-w-[480px]">
                <span className="inline-block mb-5 text-[11px] font-bold uppercase tracking-[0.14em] px-3 py-1.5 rounded-full"
                  style={{ color: f.color, background: `${f.color}18`, border: `1px solid ${f.color}2e` }}>
                  {f.badge}
                </span>
                <h2 className="font-bold text-white leading-[1.06] mb-5"
                  style={{ fontSize: 'clamp(1.9rem, 3.5vw, 2.75rem)', letterSpacing: '-0.025em', fontFamily: 'var(--font-inter)' }}>
                  {f.headline}
                </h2>
                <p className="text-base leading-relaxed mb-8" style={{ color: 'rgba(255,255,255,0.42)', fontFamily: 'var(--font-inter)' }}>
                  {f.body}
                </p>
                <ul className="space-y-3">
                  {f.bullets.map(b => (
                    <li key={b} className="flex items-center gap-3">
                      <div className="w-5 h-5 rounded-full flex items-center justify-center shrink-0"
                        style={{ background: `${f.color}20`, border: `1px solid ${f.color}35` }}>
                        <Check size={10} style={{ color: f.color }} />
                      </div>
                      <span className="text-sm font-medium" style={{ color: 'rgba(255,255,255,0.48)' }}>{b}</span>
                    </li>
                  ))}
                </ul>
              </div>
              <div className="flex-1 w-full max-w-[500px]">
                <f.Mockup />
              </div>
            </div>
          </Reveal>
        ))}
      </div>
    </section>
  )
}

// ── Compare ───────────────────────────────────────────────────────────────────
const COMPARE_ROWS = [
  { label: 'Auto duty rotation',    h: true,  w: false, s: false },
  { label: 'Expense splitting',      h: true,  w: false, s: true  },
  { label: 'Swap request system',   h: true,  w: false, s: false },
  { label: 'Real-time sync',        h: true,  w: true,  s: false },
  { label: 'Activity audit trail',  h: true,  w: false, s: false },
  { label: 'Fixed bill management', h: true,  w: false, s: false },
]

function Compare() {
  return (
    <section id="compare" className="py-24 px-6" style={{ background: '#0c0c10' }}>
      <div className="max-w-[860px] mx-auto">
        <Reveal className="text-center mb-14">
          <span className="inline-block mb-4 text-[11px] font-bold uppercase tracking-[0.13em] px-3 py-1.5 rounded-full"
            style={{ color: 'rgba(255,255,255,0.28)', background: 'rgba(255,255,255,0.04)', border: `1px solid ${BORDER}` }}>
            Why Habitiq
          </span>
          <h2 className="font-bold text-white tracking-tight mb-4"
            style={{ fontSize: 'clamp(1.8rem, 3.5vw, 2.75rem)', letterSpacing: '-0.025em', fontFamily: 'var(--font-inter)' }}>
            Purpose-built for shared living.
          </h2>
          <p className="text-base font-medium max-w-md mx-auto" style={{ color: 'rgba(255,255,255,0.32)' }}>
            WhatsApp is for chatting. Splitwise is for money. Neither runs your flat.
          </p>
        </Reveal>
        <Reveal delay={80}>
          <div className="rounded-2xl overflow-hidden" style={{ border: `1px solid ${BORDER}`, background: 'rgba(255,255,255,0.015)' }}>
            <div className="grid grid-cols-4 border-b" style={{ borderColor: BORDER, background: 'rgba(255,255,255,0.02)' }}>
              <div className="px-6 py-4" />
              {[{ name: 'Habitiq', hl: true }, { name: 'WhatsApp', hl: false }, { name: 'Splitwise', hl: false }].map(col => (
                <div key={col.name} className="px-4 py-4 text-center"
                  style={col.hl ? { background: `${BLUE}0e`, borderLeft: `1px solid ${BLUE}22`, borderRight: `1px solid ${BLUE}22` } : {}}>
                  <p className="text-sm font-bold" style={{ color: col.hl ? '#60a5fa' : 'rgba(255,255,255,0.32)' }}>{col.name}</p>
                  {col.hl && <p className="text-[10px] font-medium mt-0.5" style={{ color: 'rgba(96,165,250,0.55)' }}>Built for flats</p>}
                </div>
              ))}
            </div>
            {COMPARE_ROWS.map((row, i) => (
              <div key={row.label} className="grid grid-cols-4 border-b last:border-0"
                style={{ borderColor: 'rgba(255,255,255,0.04)', background: i % 2 === 1 ? 'rgba(255,255,255,0.01)' : 'transparent' }}>
                <div className="px-6 py-3.5 flex items-center">
                  <p className="text-sm font-medium" style={{ color: 'rgba(255,255,255,0.48)' }}>{row.label}</p>
                </div>
                {[{ has: row.h, hl: true }, { has: row.w, hl: false }, { has: row.s, hl: false }].map((cell, j) => (
                  <div key={j} className="px-4 py-3.5 flex items-center justify-center"
                    style={cell.hl ? { background: `${BLUE}08`, borderLeft: `1px solid ${BLUE}16`, borderRight: `1px solid ${BLUE}16` } : {}}>
                    {cell.has
                      ? <div className="w-6 h-6 rounded-full flex items-center justify-center"
                          style={{ background: cell.hl ? `${BLUE}22` : 'rgba(255,255,255,0.07)' }}>
                          <Check size={11} style={{ color: cell.hl ? BLUE : 'rgba(255,255,255,0.32)' }} />
                        </div>
                      : <div className="w-6 h-6 rounded-full flex items-center justify-center" style={{ background: 'rgba(255,255,255,0.025)' }}>
                          <X size={10} style={{ color: 'rgba(255,255,255,0.15)' }} />
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
function HowItWorks() {
  const steps = [
    { n: '01', t: 'Create your flat', b: 'Sign up, name your flat, get a 6-digit invite code. Done in 45 seconds.' },
    { n: '02', t: 'Flatmates join', b: 'Share the invite code. Everyone joins in one tap and auto-joins all rotation queues.' },
    { n: '03', t: 'System takes over', b: 'Duties rotate automatically. Bills split fairly. Nobody has to manage anything.' },
  ]
  return (
    <section id="how-it-works" className="py-24 px-6" style={{ background: BG }}>
      <div className="max-w-[1120px] mx-auto">
        <Reveal className="text-center mb-16">
          <span className="inline-block mb-4 text-[11px] font-bold uppercase tracking-[0.13em] px-3 py-1.5 rounded-full"
            style={{ color: '#34d399', background: 'rgba(52,211,153,0.1)', border: '1px solid rgba(52,211,153,0.2)' }}>
            Setup
          </span>
          <h2 className="font-bold text-white tracking-tight"
            style={{ fontSize: 'clamp(1.8rem, 3.5vw, 2.75rem)', letterSpacing: '-0.025em', fontFamily: 'var(--font-inter)' }}>
            Up and running in 3 minutes.
          </h2>
        </Reveal>
        <div className="grid md:grid-cols-3 gap-4 relative">
          <div className="hidden md:block absolute"
            style={{ top: '36px', left: 'calc(16.66% + 44px)', right: 'calc(16.66% + 44px)', height: '1px',
              background: `linear-gradient(90deg, transparent, ${BLUE}45 30%, ${BLUE}45 70%, transparent)` }} />
          {steps.map((s, i) => (
            <Reveal key={s.n} delay={i * 90}>
              <div className="rounded-2xl p-7 text-center" style={{ background: SURFACE, border: `1px solid ${BORDER}` }}>
                <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl text-white font-black text-sm mb-5"
                  style={{ background: `linear-gradient(135deg,${BLUE},#1d4ed8)`, boxShadow: `0 8px 24px ${BLUE}40` }}>
                  {s.n}
                </div>
                <h3 className="text-white font-semibold text-base mb-2.5">{s.t}</h3>
                <p className="text-sm leading-relaxed max-w-[210px] mx-auto" style={{ color: 'rgba(255,255,255,0.36)' }}>{s.b}</p>
              </div>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  )
}

// ── Testimonials ──────────────────────────────────────────────────────────────
const TESTIMONIALS: Testimonial[] = [
  { id: 1, quote: "Finally something that doesn't need one person to manage everything. Set up in 5 minutes, haven't argued about chores since.", name: 'Koramangala Flat', username: 'Flat of 4 · Bengaluru', avatar: '', avatarColor: '#1d4ed8', avatarInitial: 'K' },
  { id: 2, quote: "The expense splitting is the best part. Everyone sees exactly what they owe. No more awkward money conversations.", name: 'Banjara Hills PG', username: 'PG residents · Hyderabad', avatar: '', avatarColor: '#2563eb', avatarInitial: 'B' },
  { id: 3, quote: "The swap request feature saved my life during exam week. Just tap, flatmate accepts, done. Zero drama.", name: 'Student Flat', username: 'Flat of 3 · Pune', avatar: '', avatarColor: '#3b82f6', avatarInitial: 'P' },
  { id: 4, quote: "Rotation engine is genius. My flatmates stopped fighting about who does dishes. It just works.", name: 'Indiranagar House', username: 'House of 5 · Bengaluru', avatar: '', avatarColor: '#60a5fa', avatarInitial: 'I' },
  { id: 5, quote: "Added our electricity and WiFi bills. Now everyone pays on time because they can see it clearly.", name: 'HSR Layout Flat', username: 'Flat of 4 · Bengaluru', avatar: '', avatarColor: '#93c5fd', avatarInitial: 'H' },
]

function Testimonials() {
  return (
    <section className="py-20 relative overflow-hidden" style={{ background: '#0c0c10' }}>
      <div className="pointer-events-none absolute inset-0"
        style={{ background: `radial-gradient(ellipse 55% 55% at 50% 50%, ${BLUE_GLOW} 0%, transparent 80%)`, opacity: 0.3 }} />
      <div className="relative z-10">
        <Reveal className="text-center mb-12 px-6">
          <span className="inline-block mb-4 text-[11px] font-bold uppercase tracking-[0.13em] px-3 py-1.5 rounded-full"
            style={{ color: 'rgba(255,255,255,0.26)', background: 'rgba(255,255,255,0.04)', border: `1px solid ${BORDER}` }}>
            Early users
          </span>
          <h2 className="font-bold text-white tracking-tight"
            style={{ fontSize: 'clamp(1.8rem, 3.5vw, 2.5rem)', letterSpacing: '-0.025em', fontFamily: 'var(--font-inter)' }}>
            Real flats. Real results.
          </h2>
          <p className="mt-3 text-base font-medium" style={{ color: 'rgba(255,255,255,0.3)' }}>From Bengaluru to Pune — flatmates who stopped arguing.</p>
        </Reveal>
        <TestimonialSlider testimonials={TESTIMONIALS} className="bg-transparent" />
      </div>
    </section>
  )
}

// ── FAQ ───────────────────────────────────────────────────────────────────────
const FAQS = [
  { q: 'Is Habitiq free?', a: 'Yes, completely free. No credit card required. All features — duty rotation, expense splitting, bill tracking, and settlements — are available at no cost during our trial phase.' },
  { q: 'How is it different from Splitwise?', a: "Splitwise only tracks money. Habitiq also manages flat operations — automated duty rotation, monthly fixed bill management, and a swap request system. It's a complete shared living platform, not just an expense splitter." },
  { q: 'How does duty rotation work?', a: 'Define household tasks (cleaning, cooking, trash), set the flatmates in queue, and Habitiq assigns them automatically. When someone marks a task done, the next person is assigned instantly. Swap requests let members trade days with an official record.' },
  { q: 'Do I need to download an app?', a: 'No. Habitiq works directly in your phone\'s browser. You can add it to your home screen as a PWA for a native app experience. No App Store or Play Store needed — flatmates join instantly from any device.' },
  { q: 'Which cities is Habitiq used in?', a: "Bengaluru, Hyderabad, Pune, Mumbai, Delhi, Chennai, Noida, and beyond. Especially popular in tech cities with large student and working professional communities in shared accommodation." },
]

function FAQ() {
  const [open, setOpen] = useState<number | null>(null)
  return (
    <section id="faq" className="py-24 px-6" style={{ background: BG }}>
      <div className="max-w-[720px] mx-auto">
        <Reveal className="text-center mb-14">
          <span className="inline-block mb-4 text-[11px] font-bold uppercase tracking-[0.13em] px-3 py-1.5 rounded-full"
            style={{ color: '#60a5fa', background: BLUE_DIM, border: `1px solid rgba(59,130,246,0.22)` }}>
            FAQ
          </span>
          <h2 className="font-bold text-white tracking-tight"
            style={{ fontSize: 'clamp(1.8rem, 3.5vw, 2.75rem)', letterSpacing: '-0.025em', fontFamily: 'var(--font-inter)' }}>
            Common questions.
          </h2>
        </Reveal>
        <div className="space-y-2">
          {FAQS.map((faq, i) => (
            <Reveal key={i} delay={i * 30}>
              <div className="rounded-2xl overflow-hidden" style={{ border: `1px solid ${BORDER}`, background: 'rgba(255,255,255,0.018)' }}>
                <button
                  onClick={() => setOpen(open === i ? null : i)}
                  className="w-full flex items-center justify-between gap-4 px-6 py-5 text-left cursor-pointer hover:bg-white/[0.025] transition-colors">
                  <span className="text-sm font-semibold leading-snug" style={{ color: 'rgba(255,255,255,0.8)' }}>{faq.q}</span>
                  <span className={`shrink-0 w-5 h-5 rounded-full flex items-center justify-center transition-transform duration-200 ${open === i ? 'rotate-45' : ''}`}
                    style={{ border: `1px solid ${BORDER}` }}>
                    <svg width="9" height="9" viewBox="0 0 9 9" fill="none">
                      <path d="M4.5 1v7M1 4.5h7" stroke="rgba(255,255,255,0.38)" strokeWidth="1.4" strokeLinecap="round"/>
                    </svg>
                  </span>
                </button>
                {open === i && (
                  <div className="px-6 pb-5">
                    <p className="text-sm leading-relaxed" style={{ color: 'rgba(255,255,255,0.4)' }}>{faq.a}</p>
                  </div>
                )}
              </div>
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
    <section id="get-started" className="py-24 px-6 relative overflow-hidden" style={{ background: '#0c0c10' }}>
      <div className="absolute pointer-events-none"
        style={{ top: '-40%', left: '50%', transform: 'translateX(-50%)', width: '800px', height: '600px',
          background: `radial-gradient(ellipse, ${BLUE_GLOW} 0%, transparent 65%)`, filter: 'blur(50px)', opacity: 0.5 }} />
      <div className="relative z-10 max-w-[1120px] mx-auto grid lg:grid-cols-2 gap-16 items-start">
        <Reveal>
          <span className="inline-block mb-5 text-[11px] font-bold uppercase tracking-[0.13em] px-3 py-1.5 rounded-full"
            style={{ color: '#60a5fa', background: BLUE_DIM, border: `1px solid rgba(59,130,246,0.22)` }}>
            Start free
          </span>
          <h2 className="font-bold text-white leading-tight mb-5"
            style={{ fontSize: 'clamp(2rem, 4vw, 3rem)', letterSpacing: '-0.025em', fontFamily: 'var(--font-inter)' }}>
            Your flat deserves<br />a fair system.
          </h2>
          <p className="text-base font-medium mb-10 leading-relaxed max-w-sm" style={{ color: 'rgba(255,255,255,0.36)' }}>
            Create a flat or join your flatmates. Free. No card. Under 2 minutes.
          </p>
          <ul className="space-y-3">
            {[
              'Unlimited duty rotation & expense splitting',
              'Real-time sync across all flatmates',
              'Swap system, activity log, analytics',
              'Works on any phone — no app install needed',
            ].map(item => (
              <li key={item} className="flex items-center gap-3">
                <div className="w-5 h-5 rounded-full flex items-center justify-center shrink-0"
                  style={{ background: `${BLUE}22`, border: `1px solid ${BLUE}35` }}>
                  <Check size={10} style={{ color: BLUE }} />
                </div>
                <span className="text-sm font-medium" style={{ color: 'rgba(255,255,255,0.46)' }}>{item}</span>
              </li>
            ))}
          </ul>
        </Reveal>
        <Reveal delay={100}>
          <div className="rounded-2xl overflow-hidden"
            style={{ border: `1px solid ${BORDER}`, background: 'rgba(255,255,255,0.02)', boxShadow: `0 0 0 1px ${BLUE}16, 0 24px 64px rgba(0,0,0,0.5)` }}>
            <div className="px-7 pt-6 pb-3 border-b" style={{ borderColor: BORDER }}>
              <p className="text-white font-semibold text-base">Create your account</p>
              <p className="text-sm mt-0.5" style={{ color: 'rgba(255,255,255,0.28)' }}>Join your flatmates in under a minute.</p>
            </div>
            <AuthForm inline />
          </div>
        </Reveal>
      </div>
    </section>
  )
}

// ── Footer ────────────────────────────────────────────────────────────────────
function Footer() {
  return (
    <footer className="pt-12 pb-8 px-6 border-t" style={{ borderColor: 'rgba(255,255,255,0.05)', background: BG }}>
      <div className="max-w-[1120px] mx-auto">
        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-10 mb-12">
          <div className="lg:col-span-2">
            <img src="/habitiq-logo.svg" alt="Habitiq" className="h-7 w-auto brightness-0 invert mb-4" />
            <p className="text-sm leading-relaxed max-w-xs font-medium" style={{ color: 'rgba(255,255,255,0.24)' }}>
              The operating system for shared flats in India. Duties rotate. Bills split. Flatmates stay happy.
            </p>
            <p className="text-xs mt-5" style={{ color: 'rgba(255,255,255,0.14)' }}>Made with care in India</p>
          </div>
          <div>
            <p className="text-[11px] font-bold uppercase tracking-[0.12em] mb-4" style={{ color: 'rgba(255,255,255,0.32)' }}>Product</p>
            <div className="space-y-3">
              {[['#features','Features'],['#how-it-works','How it works'],['#compare','Compare'],['#get-started','Get started']].map(([h,l]) => (
                <a key={h} href={h} className="block text-sm hover:text-white/55 transition-colors" style={{ color: 'rgba(255,255,255,0.24)' }}>{l}</a>
              ))}
            </div>
          </div>
          <div>
            <p className="text-[11px] font-bold uppercase tracking-[0.12em] mb-4" style={{ color: 'rgba(255,255,255,0.32)' }}>Company</p>
            <div className="space-y-3">
              {[['#','About'],['/privacy','Privacy'],['/terms','Terms'],['mailto:hello@habitiq.in','Contact']].map(([h,l]) => (
                <a key={l} href={h} className="block text-sm hover:text-white/55 transition-colors" style={{ color: 'rgba(255,255,255,0.24)' }}>{l}</a>
              ))}
            </div>
          </div>
        </div>
        <div className="border-t pt-6 flex flex-col sm:flex-row items-center justify-between gap-3" style={{ borderColor: 'rgba(255,255,255,0.04)' }}>
          <p className="text-xs" style={{ color: 'rgba(255,255,255,0.14)' }}>© 2026 Habitiq. All rights reserved.</p>
          <p className="text-xs" style={{ color: 'rgba(255,255,255,0.14)' }}>Shared living, managed.</p>
        </div>
      </div>
    </footer>
  )
}

// ── Page ──────────────────────────────────────────────────────────────────────
export default function LandingPage() {
  return (
    <div className="min-h-screen antialiased" style={{ background: BG, fontFamily: 'var(--font-inter)' }}>
      <style>{`@keyframes mq { from { transform: translateX(0) } to { transform: translateX(-50%) } }`}</style>
      <Navbar />
      <main>
        <Hero />
        <FeatureStrip />
        <Features />
        <Compare />
        <HowItWorks />
        <Testimonials />
        <FAQ />
        <GetStarted />
      </main>
      <Footer />
    </div>
  )
}
