"use client"
import { useState, useEffect } from 'react'
import { createPortal } from 'react-dom'
import {
  X, Ticket, ShieldCheck, Loader2, AlertTriangle,
  ClipboardList, LayoutDashboard, Receipt, CheckCircle2, Lock,
} from 'lucide-react'
import { useFlatStore } from '@/store/useFlatStore'
import { validateAndRedeemCoupon } from '@/lib/couponService'
import type { GatedFeature } from '@/hooks/useSubscription'

/* ── Per-feature copy ─────────────────────────────────────────────────────── */
const FEATURE_COPY: Record<GatedFeature, { title: string; subtitle: string }> = {
  create_task: {
    title: 'Task creation needs a subscription',
    subtitle: 'Your free trial has ended. Enter a coupon to unlock tasks, rotation, and full admin control.',
  },
  create_flat: {
    title: 'You\'ve reached your flat limit',
    subtitle: 'Free access covers 1 flat. Subscribe to create up to 3 flats and join 3 more — 6 total.',
  },
  add_expense: {
    title: 'Adding expenses needs a subscription',
    subtitle: 'Your free trial has ended. Enter a coupon to unlock expense tracking and bill splits.',
  },
  create_bill: {
    title: 'Recurring bills need a subscription',
    subtitle: 'Your free trial has ended. Enter a coupon to unlock recurring bills and monthly close.',
  },
}

const FREE_FEATURES = [
  { icon: CheckCircle2, label: 'Mark tasks as complete' },
  { icon: LayoutDashboard, label: 'View dashboard & activity' },
  { icon: Receipt, label: 'View expense balances' },
  { icon: ShieldCheck, label: '1 flat included for free' },
]

const PREMIUM_FEATURES = [
  { icon: ClipboardList, label: 'Create & manage tasks' },
  { icon: Receipt, label: 'Add expenses & bills' },
  { icon: ShieldCheck, label: 'Full admin controls' },
  { icon: LayoutDashboard, label: 'Multiple flats' },
]

/* ── Confetti config ─────────────────────────────────────────────────────── */
const CONFETTI_PIECES = [
  { left: '4%',  size: 8,  color: '#3786FB', delay: 0,    dur: 2.8, round: false },
  { left: '9%',  size: 6,  color: '#F59E0B', delay: 0.35, dur: 2.4, round: true  },
  { left: '15%', size: 10, color: '#10B981', delay: 0.1,  dur: 3.1, round: false },
  { left: '21%', size: 7,  color: '#EF4444', delay: 0.65, dur: 2.6, round: true  },
  { left: '27%', size: 9,  color: '#8B5CF6', delay: 0.2,  dur: 2.9, round: false },
  { left: '33%', size: 6,  color: '#F97316', delay: 0.5,  dur: 2.5, round: false },
  { left: '40%', size: 8,  color: '#06B6D4', delay: 0.3,  dur: 3.2, round: true  },
  { left: '46%', size: 7,  color: '#EC4899', delay: 0.75, dur: 2.7, round: false },
  { left: '53%', size: 9,  color: '#3786FB', delay: 0.15, dur: 2.4, round: true  },
  { left: '59%', size: 6,  color: '#F59E0B', delay: 0.45, dur: 3.0, round: false },
  { left: '65%', size: 8,  color: '#10B981', delay: 0.25, dur: 2.6, round: false },
  { left: '71%', size: 7,  color: '#8B5CF6', delay: 0.6,  dur: 2.8, round: true  },
  { left: '77%', size: 10, color: '#EF4444', delay: 0.05, dur: 2.5, round: false },
  { left: '83%', size: 6,  color: '#F97316', delay: 0.55, dur: 3.1, round: true  },
  { left: '89%', size: 8,  color: '#06B6D4', delay: 0.1,  dur: 2.7, round: false },
  { left: '95%', size: 7,  color: '#EC4899', delay: 0.4,  dur: 2.4, round: false },
]

/* ── Coupon form ─────────────────────────────────────────────────────────── */
function CouponForm({ flatId, onSuccess }: { flatId: string; onSuccess: (days: number) => void }) {
  const [code, setCode] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleRedeem = async () => {
    if (!code.trim()) return
    setLoading(true)
    setError('')
    const result = await validateAndRedeemCoupon(flatId, code)
    setLoading(false)
    if (result.success) {
      onSuccess(result.durationDays)
    } else {
      setError(result.error)
    }
  }

  return (
    <div className="space-y-2.5">
      <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
        Enter coupon code
      </label>
      <div className="flex gap-2">
        <input
          type="text"
          value={code}
          onChange={e => { setCode(e.target.value.toUpperCase()); setError('') }}
          onKeyDown={e => e.key === 'Enter' && handleRedeem()}
          placeholder="XXXX-XXXX"
          maxLength={32}
          className="flex-1 px-3 py-2.5 text-sm font-mono tracking-widest rounded-xl border border-border bg-background focus:outline-none focus:ring-2 focus:ring-primary/40 placeholder:text-muted-foreground/30 uppercase"
        />
        <button
          onClick={handleRedeem}
          disabled={loading || !code.trim()}
          className="px-4 py-2.5 rounded-xl bg-primary text-primary-foreground text-sm font-bold disabled:opacity-50 hover:bg-primary/90 active:scale-95 transition-all cursor-pointer flex items-center gap-2 whitespace-nowrap"
        >
          {loading ? <Loader2 size={14} className="animate-spin" /> : <Ticket size={14} />}
          {loading ? 'Checking…' : 'Apply'}
        </button>
      </div>
      {error && (
        <p className="text-xs text-red-500 flex items-center gap-1.5">
          <AlertTriangle size={11} />
          {error}
        </p>
      )}
    </div>
  )
}

/* ── Celebration view ────────────────────────────────────────────────────── */
function CelebrationView({ days, onClose }: { days: number; onClose: () => void }) {
  const isForever = days === -1 || days > 365
  const accessLabel = isForever ? 'Lifetime access activated' : `${days} days of full access`

  return (
    <div
      className="fixed inset-0 z-[350] flex items-center justify-center p-4 overflow-hidden"
      style={{ background: 'rgba(0,0,0,0.82)', backdropFilter: 'blur(10px)' }}
    >
      {/* Confetti rain */}
      {CONFETTI_PIECES.map((c, i) => (
        <div
          key={i}
          className="fixed top-0 pointer-events-none"
          style={{
            left: c.left,
            width: c.size,
            height: c.round ? c.size : Math.round(c.size * 1.65),
            background: c.color,
            borderRadius: c.round ? '50%' : '2px',
            animation: `confetti-fall ${c.dur}s ${c.delay}s ease-in infinite,
                        confetti-sway ${c.dur * 1.4}s ${c.delay}s ease-in-out infinite`,
          }}
        />
      ))}

      {/* Card */}
      <div
        className="relative w-full max-w-sm bg-card rounded-[28px] shadow-2xl border border-border overflow-hidden"
        style={{ animation: 'celebration-card-in 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) both' }}
      >
        {/* Emerald gradient header */}
        <div className="relative bg-gradient-to-br from-emerald-500 via-teal-500 to-emerald-600 px-6 pt-10 pb-16 flex flex-col items-center">

          {/* Close */}
          <button
            onClick={onClose}
            className="absolute top-4 right-4 p-1.5 rounded-full bg-white/15 hover:bg-white/25 text-white transition-colors cursor-pointer"
            aria-label="Close"
          >
            <X size={16} />
          </button>

          {/* Pulse rings */}
          <div
            className="absolute w-24 h-24 rounded-full bg-white/25"
            style={{ animation: 'ring-expand 1.8s ease-out 0.3s infinite' }}
          />
          <div
            className="absolute w-24 h-24 rounded-full bg-white/15"
            style={{ animation: 'ring-expand 1.8s ease-out 0.75s infinite' }}
          />

          {/* Icon */}
          <div
            className="relative z-10 w-20 h-20 rounded-full bg-white shadow-2xl flex items-center justify-center"
            style={{ animation: 'unlock-pop 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) 0.1s both' }}
          >
            <ShieldCheck size={38} className="text-emerald-500" />
          </div>
        </div>

        {/* Floating content card */}
        <div
          className="-mt-6 mx-4 bg-card border border-border rounded-2xl shadow-lg px-5 pt-5 pb-4 mb-4 space-y-4"
          style={{ animation: 'stagger-up 0.5s ease-out 0.2s both' }}
        >
          {/* Heading */}
          <div className="text-center space-y-1">
            <p
              className="text-[22px] font-black text-foreground leading-tight"
              style={{ animation: 'stagger-up 0.4s ease-out 0.3s both' }}
            >
              You&apos;re unlocked!
            </p>
            <p
              className="text-[13px] font-semibold text-emerald-600 dark:text-emerald-400"
              style={{ animation: 'stagger-up 0.4s ease-out 0.38s both' }}
            >
              {accessLabel}
            </p>
          </div>

          {/* Feature list */}
          <div className="space-y-2.5">
            {PREMIUM_FEATURES.map(({ label }, i) => (
              <div
                key={label}
                className="flex items-center gap-3"
                style={{ animation: `stagger-up 0.35s ease-out ${0.45 + i * 0.07}s both` }}
              >
                <div className="w-6 h-6 rounded-full bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center shrink-0">
                  <CheckCircle2 size={13} className="text-emerald-600 dark:text-emerald-400" />
                </div>
                <span className="text-[13px] font-medium">{label}</span>
              </div>
            ))}
          </div>

          {/* CTA */}
          <button
            onClick={onClose}
            className="w-full py-3.5 rounded-xl bg-emerald-500 hover:bg-emerald-600 active:scale-[0.98] text-white font-bold text-[14px] transition-all cursor-pointer shadow-[0_4px_16px_rgba(16,185,129,0.35)]"
            style={{ animation: 'stagger-up 0.4s ease-out 0.75s both' }}
          >
            Start exploring →
          </button>

          <p
            className="text-center text-[11px] text-muted-foreground pb-1"
            style={{ animation: 'stagger-up 0.4s ease-out 0.82s both' }}
          >
            All features are now active
          </p>
        </div>
      </div>
    </div>
  )
}

/* ── Main export ──────────────────────────────────────────────────────────── */
interface Props {
  feature: GatedFeature
  onClose: () => void
}

export default function SubscriptionUpsell({ feature, onClose }: Props) {
  const flatId = useFlatStore(s => s.flatId)
  const [mounted, setMounted] = useState(false)
  const [celebrated, setCelebrated] = useState(false)
  const [unlockedDays, setUnlockedDays] = useState(0)

  useEffect(() => { setMounted(true) }, [])
  if (!mounted || !flatId) return null

  if (celebrated) {
    return createPortal(
      <CelebrationView days={unlockedDays} onClose={onClose} />,
      document.body
    )
  }

  const copy = FEATURE_COPY[feature]

  const modal = (
    <div
      className="fixed inset-0 z-[300] flex items-center justify-center p-4 bg-black/65 backdrop-blur-sm"
      onClick={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div className="w-full max-w-md bg-card border border-border rounded-3xl shadow-2xl overflow-hidden">

        {/* Top band with lock icon */}
        <div className="relative bg-gradient-to-br from-violet-600 to-indigo-600 px-6 pt-8 pb-10">
          <button
            onClick={onClose}
            className="absolute top-4 right-4 p-1.5 rounded-full bg-white/10 hover:bg-white/20 text-white transition-colors cursor-pointer"
            aria-label="Close"
          >
            <X size={16} />
          </button>
          <div className="w-12 h-12 rounded-2xl bg-white/15 flex items-center justify-center mb-4">
            <Lock size={22} className="text-white" />
          </div>
          <h2 className="text-lg font-bold text-white leading-snug">{copy.title}</h2>
          <p className="text-sm text-white/70 mt-1">{copy.subtitle}</p>
        </div>

        {/* Feature comparison */}
        <div className="-mt-4 mx-5 bg-card border border-border rounded-2xl shadow-sm px-4 py-4 space-y-3">
          <div>
            <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-2">
              What you can do now
            </p>
            <div className="space-y-1.5">
              {FREE_FEATURES.map(({ icon: Icon, label }) => (
                <div key={label} className="flex items-center gap-2.5 text-sm text-muted-foreground">
                  <Icon size={14} className="text-emerald-500 shrink-0" />
                  {label}
                </div>
              ))}
            </div>
          </div>
          <div className="border-t border-dashed border-border" />
          <div>
            <p className="text-[10px] font-bold text-primary/80 uppercase tracking-widest mb-2">
              Unlock with a coupon
            </p>
            <div className="space-y-1.5">
              {PREMIUM_FEATURES.map(({ icon: Icon, label }) => (
                <div key={label} className="flex items-center gap-2.5 text-sm font-medium">
                  <Icon size={14} className="text-primary shrink-0" />
                  {label}
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Coupon input + actions */}
        <div className="px-5 py-5 space-y-4">
          <CouponForm
            flatId={flatId}
            onSuccess={(days) => {
              setUnlockedDays(days)
              setCelebrated(true)
            }}
          />
          <div className="flex items-center gap-3">
            <div className="flex-1 h-px bg-border" />
            <span className="text-xs text-muted-foreground">or</span>
            <div className="flex-1 h-px bg-border" />
          </div>
          <div className="flex flex-col gap-2">
            <a
              href="https://wa.me/917981756963?text=Hi!%20I%20need%20a%20Habitiq%20coupon%20code."
              target="_blank"
              rel="noopener noreferrer"
              className="w-full py-2.5 rounded-xl border border-border text-sm font-semibold text-center hover:bg-secondary/60 transition-colors cursor-pointer"
            >
              Get a coupon on WhatsApp
            </a>
            <button
              onClick={onClose}
              className="w-full py-2 text-xs text-muted-foreground hover:text-foreground transition-colors cursor-pointer"
            >
              Continue with limited access
            </button>
          </div>
        </div>

      </div>
    </div>
  )

  return createPortal(modal, document.body)
}
