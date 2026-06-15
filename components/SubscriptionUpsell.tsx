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
    title: 'Creating tasks needs a subscription',
    subtitle: 'Unlock task creation, rotation management, and full admin control.',
  },
  create_flat: {
    title: 'Adding a second flat needs a subscription',
    subtitle: 'Subscribe to manage multiple flats from the same account.',
  },
  add_expense: {
    title: 'Adding expenses needs a subscription',
    subtitle: 'Unlock expense tracking, bill splits, and balance management.',
  },
  create_bill: {
    title: 'Recurring bills need a subscription',
    subtitle: 'Unlock recurring bills, rent tracking, and monthly close.',
  },
}

/* ── What you get for free vs. subscribed ─────────────────────────────────── */
const FREE_FEATURES = [
  { icon: CheckCircle2, label: 'Mark tasks as complete' },
  { icon: LayoutDashboard, label: 'View your dashboard & activity' },
  { icon: Receipt, label: 'View expense balances' },
]

const PREMIUM_FEATURES = [
  { icon: ClipboardList, label: 'Create & manage tasks' },
  { icon: Receipt, label: 'Add expenses & bills' },
  { icon: ShieldCheck, label: 'Full admin controls' },
  { icon: LayoutDashboard, label: 'Multiple flats' },
]

/* ── Coupon redemption inside the modal ───────────────────────────────────── */
function CouponForm({ flatId, onSuccess }: { flatId: string; onSuccess: () => void }) {
  const [code, setCode] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  const handleRedeem = async () => {
    if (!code.trim()) return
    setLoading(true)
    setError('')
    const result = await validateAndRedeemCoupon(flatId, code)
    setLoading(false)
    if (result.success) {
      setSuccess(true)
      setTimeout(onSuccess, 1400)
    } else {
      setError(result.error)
    }
  }

  if (success) {
    return (
      <div className="flex items-center gap-2 justify-center py-3 text-emerald-600 dark:text-emerald-400 font-semibold text-sm">
        <ShieldCheck size={18} />
        All features unlocked!
      </div>
    )
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
          {loading
            ? <Loader2 size={14} className="animate-spin" />
            : <Ticket size={14} />
          }
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

/* ── Main export ──────────────────────────────────────────────────────────── */
interface Props {
  feature: GatedFeature
  onClose: () => void
}

export default function SubscriptionUpsell({ feature, onClose }: Props) {
  const flatId = useFlatStore(s => s.flatId)
  const [mounted, setMounted] = useState(false)
  useEffect(() => { setMounted(true) }, [])
  if (!mounted || !flatId) return null

  const copy = FEATURE_COPY[feature]

  const modal = (
    <div
      className="fixed inset-0 z-[300] flex items-center justify-center p-4 bg-black/65 backdrop-blur-sm"
      onClick={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div className="w-full max-w-md bg-card border border-border rounded-3xl shadow-2xl overflow-hidden">

        {/* ── Top band with lock icon ── */}
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

        {/* ── Feature comparison ── */}
        <div className="-mt-4 mx-5 bg-card border border-border rounded-2xl shadow-sm px-4 py-4 space-y-3">

          {/* Free column */}
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

          {/* Premium column */}
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

        {/* ── Coupon input + actions ── */}
        <div className="px-5 py-5 space-y-4">
          <CouponForm flatId={flatId} onSuccess={onClose} />

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
