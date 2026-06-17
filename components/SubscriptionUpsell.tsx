"use client"
import { useState, useEffect } from 'react'
import { createPortal } from 'react-dom'
import {
  X, Ticket, ShieldCheck, Loader2, AlertTriangle,
  ClipboardList, LayoutDashboard, Receipt, CheckCircle2, Lock, Crown, Sparkles,
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
    subtitle: 'Free access covers 1 flat. Subscribe to create and manage up to 3 flats.',
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
  const accessLabel = `${days} days of Premium`

  return (
    <div
      className="fixed inset-0 z-[350] flex items-center justify-center p-4 overflow-hidden"
      style={{ background: 'rgba(0,0,0,0.88)', backdropFilter: 'blur(14px)' }}
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
        className="relative w-full max-w-sm rounded-[32px] shadow-[0_32px_80px_rgba(0,0,0,0.6)] overflow-hidden"
        style={{
          background: 'linear-gradient(160deg, #0f1a0f 0%, #0a1f14 50%, #0d2010 100%)',
          border: '1px solid rgba(16,185,129,0.25)',
          animation: 'celebration-card-in 0.55s cubic-bezier(0.34, 1.56, 0.64, 1) both',
        }}
      >
        {/* Gold shimmer line at top */}
        <div className="h-px w-full" style={{ background: 'linear-gradient(90deg, transparent, #10b981, #34d399, #10b981, transparent)' }} />

        {/* Header */}
        <div className="relative px-6 pt-8 pb-14 flex flex-col items-center overflow-hidden">
          {/* Glow blob */}
          <div className="absolute -top-10 left-1/2 -translate-x-1/2 w-48 h-48 rounded-full pointer-events-none" style={{ background: 'radial-gradient(circle, rgba(16,185,129,0.18) 0%, transparent 70%)' }} />

          {/* Pulse rings */}
          <div className="absolute w-28 h-28 rounded-full" style={{ border: '1px solid rgba(52,211,153,0.25)', animation: 'ring-expand 2s ease-out 0.3s infinite' }} />
          <div className="absolute w-28 h-28 rounded-full" style={{ border: '1px solid rgba(52,211,153,0.12)', animation: 'ring-expand 2s ease-out 0.8s infinite' }} />

          {/* Crown icon */}
          <div
            className="relative z-10 w-22 h-22 flex flex-col items-center justify-center"
            style={{ animation: 'unlock-pop 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) 0.1s both' }}
          >
            <div className="w-20 h-20 rounded-full flex items-center justify-center shadow-[0_0_40px_rgba(16,185,129,0.5)]" style={{ background: 'linear-gradient(145deg, #059669, #10b981, #34d399)' }}>
              <Crown size={36} className="text-white" style={{ filter: 'drop-shadow(0 2px 6px rgba(0,0,0,0.3))' }} />
            </div>
          </div>

          {/* Premium badge */}
          <div
            className="mt-4 flex items-center gap-1.5 px-3 py-1 rounded-full"
            style={{ background: 'rgba(52,211,153,0.12)', border: '1px solid rgba(52,211,153,0.25)', animation: 'stagger-up 0.4s ease-out 0.35s both' }}
          >
            <Sparkles size={10} className="text-emerald-400" />
            <span className="text-[10px] font-extrabold text-emerald-400 tracking-widest uppercase">Premium Activated</span>
          </div>

          {/* Close */}
          <button
            onClick={onClose}
            className="absolute top-4 right-4 p-1.5 rounded-full transition-colors cursor-pointer"
            style={{ background: 'rgba(255,255,255,0.07)' }}
            aria-label="Close"
          >
            <X size={15} className="text-white/50" />
          </button>
        </div>

        {/* Floating content */}
        <div
          className="-mt-8 mx-4 rounded-2xl px-5 pt-5 pb-4 mb-4 space-y-4"
          style={{
            background: 'rgba(255,255,255,0.04)',
            border: '1px solid rgba(255,255,255,0.07)',
            animation: 'stagger-up 0.5s ease-out 0.2s both',
          }}
        >
          {/* Heading */}
          <div className="text-center space-y-0.5" style={{ animation: 'stagger-up 0.4s ease-out 0.3s both' }}>
            <p className="text-[24px] font-black text-white leading-tight">
              Welcome to Premium 🎉
            </p>
            <p className="text-[13px] font-semibold text-emerald-400">
              {accessLabel} unlocked
            </p>
          </div>

          {/* Feature list */}
          <div className="space-y-2">
            {PREMIUM_FEATURES.map(({ label }, i) => (
              <div
                key={label}
                className="flex items-center gap-3"
                style={{ animation: `stagger-up 0.35s ease-out ${0.45 + i * 0.07}s both` }}
              >
                <div className="w-5 h-5 rounded-full flex items-center justify-center shrink-0" style={{ background: 'rgba(16,185,129,0.18)' }}>
                  <CheckCircle2 size={12} className="text-emerald-400" />
                </div>
                <span className="text-[13px] font-medium text-white/80">{label}</span>
              </div>
            ))}
          </div>

          {/* CTA */}
          <button
            onClick={onClose}
            className="w-full py-3.5 rounded-xl font-bold text-[14px] text-white transition-all cursor-pointer active:scale-[0.98]"
            style={{
              background: 'linear-gradient(135deg, #059669, #10b981)',
              boxShadow: '0 4px 24px rgba(16,185,129,0.4)',
              animation: 'stagger-up 0.4s ease-out 0.75s both',
            }}
          >
            Start using Premium →
          </button>

          <p
            className="text-center text-[11px] pb-1"
            style={{ color: 'rgba(255,255,255,0.3)', animation: 'stagger-up 0.4s ease-out 0.82s both' }}
          >
            All features are now active for your flat
          </p>
        </div>

        {/* Gold shimmer line at bottom */}
        <div className="h-px w-full mb-0" style={{ background: 'linear-gradient(90deg, transparent, rgba(16,185,129,0.4), transparent)' }} />
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
          {/* Free welcome code hint */}
          <div className="flex items-center gap-2.5 px-3 py-2.5 rounded-xl" style={{ background: 'rgba(99,102,241,0.07)', border: '1px dashed rgba(99,102,241,0.25)' }}>
            <Ticket size={13} className="text-primary shrink-0" />
            <p className="text-[12px] text-muted-foreground leading-snug">
              Try <span className="font-extrabold text-foreground tracking-widest font-mono">HAB-WELCOME</span> — free for all early users. Unlocks 90 days.
            </p>
          </div>
          <button
            onClick={onClose}
            className="w-full py-2 text-xs text-muted-foreground hover:text-foreground transition-colors cursor-pointer"
          >
            Continue with limited access
          </button>
        </div>

      </div>
    </div>
  )

  return createPortal(modal, document.body)
}
