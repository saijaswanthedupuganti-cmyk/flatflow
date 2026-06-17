"use client"
import { useState, useEffect } from 'react'
import { createPortal } from 'react-dom'
import { Ticket, ShieldCheck, Clock, X, Loader2, AlertTriangle } from 'lucide-react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { validateAndRedeemCoupon } from '@/lib/couponService'

/* ── Trial math helpers ───────────────────────────────────────────────────── */
function daysRemaining(isoDate: string | null): number {
  if (!isoDate) return 30
  const diff = new Date(isoDate).getTime() - Date.now()
  return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)))
}

/* ── Coupon input — shared between admin modal and member modal ────────────── */
function CouponInput({ flatId, onSuccess }: { flatId: string; onSuccess: () => void }) {
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
      setTimeout(onSuccess, 1200)
    } else {
      setError(result.error)
    }
  }

  if (success) {
    return (
      <div className="flex items-center gap-2 justify-center py-3 text-emerald-600 dark:text-emerald-400 font-semibold text-sm">
        <ShieldCheck size={18} />
        Access unlocked! Loading your flat…
      </div>
    )
  }

  return (
    <div className="space-y-2">
      <div className="flex gap-2">
        <input
          type="text"
          value={code}
          onChange={e => { setCode(e.target.value.toUpperCase()); setError('') }}
          onKeyDown={e => e.key === 'Enter' && handleRedeem()}
          placeholder="ENTER CODE"
          maxLength={32}
          className="flex-1 px-3 py-2.5 text-sm font-mono tracking-widest rounded-xl border border-border bg-background focus:outline-none focus:ring-2 focus:ring-primary/40 placeholder:text-muted-foreground/40 uppercase"
        />
        <button
          onClick={handleRedeem}
          disabled={loading || !code.trim()}
          className="px-4 py-2.5 rounded-xl bg-primary text-primary-foreground text-sm font-semibold disabled:opacity-50 hover:bg-primary/90 transition-colors cursor-pointer flex items-center gap-2 whitespace-nowrap"
        >
          {loading ? <Loader2 size={15} className="animate-spin" /> : <Ticket size={15} />}
          {loading ? 'Checking…' : 'Unlock'}
        </button>
      </div>
      {error && (
        <p className="text-xs text-red-500 flex items-center gap-1.5">
          <AlertTriangle size={12} />
          {error}
        </p>
      )}
    </div>
  )
}

/* ── Expired modal for the admin ──────────────────────────────────────────── */
function AdminExpiredModal({ flatId, onUnlocked }: { flatId: string; onUnlocked: () => void }) {
  const [dismissed, setDismissed] = useState(false)

  if (dismissed) return null

  return createPortal(
    <div className="fixed inset-0 z-[200] flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
      <div className="w-full max-w-sm bg-card border border-border rounded-3xl shadow-2xl overflow-hidden">

        {/* Header */}
        <div className="relative px-7 pt-7 pb-5 flex flex-col items-center text-center gap-3">
          <button
            onClick={() => setDismissed(true)}
            className="absolute top-4 right-4 p-1.5 rounded-full text-muted-foreground hover:text-foreground hover:bg-secondary transition-colors cursor-pointer"
            aria-label="Dismiss"
          >
            <X size={16} />
          </button>
          <div className="w-14 h-14 rounded-2xl bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center">
            <Clock size={26} className="text-amber-600 dark:text-amber-400" />
          </div>
          <div>
            <h2 className="text-lg font-bold">Free trial ended</h2>
            <p className="text-sm text-muted-foreground mt-1">
              Enter a coupon to unlock task creation, expenses, and bills for your flat.
            </p>
          </div>
        </div>

        {/* Coupon input */}
        <div className="px-7 pb-4">
          <CouponInput flatId={flatId} onSuccess={onUnlocked} />
        </div>

        {/* Free code hint */}
        <div className="mx-7 mb-5 flex items-center gap-2.5 px-3 py-2.5 rounded-xl" style={{ background: 'rgba(99,102,241,0.06)', border: '1px dashed rgba(99,102,241,0.25)' }}>
          <Ticket size={13} className="text-primary shrink-0" />
          <p className="text-[12px] text-muted-foreground leading-snug">
            Try <span className="font-extrabold text-foreground font-mono tracking-widest">HAB-WELCOME</span> — free 90-day access for everyone.
          </p>
        </div>

        <button
          onClick={() => setDismissed(true)}
          className="w-full py-3 border-t border-border text-xs text-muted-foreground hover:text-foreground hover:bg-secondary/50 transition-colors cursor-pointer"
        >
          View flat in read-only mode
        </button>
      </div>
    </div>,
    document.body,
  )
}

/* ── Expired notice for members (non-admin) ───────────────────────────────── */
function MemberExpiredModal({ flatId, adminName, onUnlocked }: { flatId: string; adminName: string; onUnlocked: () => void }) {
  const [showCoupon, setShowCoupon] = useState(false)

  return createPortal(
    <div className="fixed inset-0 z-[200] flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
      <div className="w-full max-w-sm bg-card border border-border rounded-3xl shadow-2xl p-7 space-y-5">

        <div className="flex flex-col items-center text-center gap-3">
          <div className="w-14 h-14 rounded-2xl bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center">
            <Clock size={26} className="text-amber-600 dark:text-amber-400" />
          </div>
          <div>
            <h2 className="text-lg font-bold">Free trial ended</h2>
            <p className="text-sm text-muted-foreground mt-1">
              Ask <span className="font-semibold text-foreground">{adminName}</span> to enter a coupon code to unlock your flat.
            </p>
          </div>
        </div>

        {!showCoupon ? (
          <button
            onClick={() => setShowCoupon(true)}
            className="w-full py-2.5 rounded-xl border border-border text-sm font-medium hover:bg-secondary/60 transition-colors cursor-pointer"
          >
            I have a coupon code
          </button>
        ) : (
          <CouponInput flatId={flatId} onSuccess={onUnlocked} />
        )}

        <div className="flex items-center gap-2 px-3 py-2.5 rounded-xl" style={{ background: 'rgba(99,102,241,0.06)', border: '1px dashed rgba(99,102,241,0.25)' }}>
          <Ticket size={12} className="text-primary shrink-0" />
          <p className="text-[11px] text-muted-foreground leading-snug">
            Have a code? Try <span className="font-extrabold text-foreground font-mono">HAB-WELCOME</span> for free 90-day access.
          </p>
        </div>
        <p className="text-center text-xs text-muted-foreground">
          You can still view your flat data while the trial is expired.
        </p>
      </div>
    </div>,
    document.body,
  )
}

/* ── Trial warning banner (≤7 days left) ─────────────────────────────────── */
function TrialBanner({ daysLeft, flatId, isAdmin }: { daysLeft: number; flatId: string; isAdmin: boolean }) {
  const [dismissed, setDismissed] = useState(false)
  const [showCoupon, setShowCoupon] = useState(false)

  if (dismissed) return null

  return (
    <div className="mx-auto max-w-6xl px-0 mb-4">
      <div className="bg-amber-50 dark:bg-amber-950/30 border border-amber-200 dark:border-amber-800/50 rounded-2xl px-4 py-3 flex items-center gap-3">
        <Clock size={16} className="text-amber-600 dark:text-amber-400 shrink-0" />
        <p className="text-sm text-amber-800 dark:text-amber-300 flex-1">
          <span className="font-semibold">
            {daysLeft === 0 ? 'Trial ends today' : `${daysLeft} day${daysLeft === 1 ? '' : 's'} left on your trial`}
          </span>
          {isAdmin ? ' — enter a coupon code to keep access.' : ' — ask your admin to enter a coupon code.'}
        </p>
        {isAdmin && !showCoupon && (
          <button
            onClick={() => setShowCoupon(true)}
            className="shrink-0 text-xs font-semibold text-amber-700 dark:text-amber-400 underline underline-offset-2 cursor-pointer hover:text-amber-900 transition-colors"
          >
            Enter code
          </button>
        )}
        <button
          onClick={() => setDismissed(true)}
          className="shrink-0 p-1 rounded-lg hover:bg-amber-100 dark:hover:bg-amber-900/40 text-amber-500 transition-colors cursor-pointer"
          aria-label="Dismiss"
        >
          <X size={14} />
        </button>
      </div>
      {showCoupon && (
        <div className="mt-2 px-1">
          <CouponInput flatId={flatId} onSuccess={() => setDismissed(true)} />
        </div>
      )}
    </div>
  )
}

/* ── Main export — wrap any page content ─────────────────────────────────── */
export default function SubscriptionGate({ children }: { children: React.ReactNode }) {
  const flatId = useFlatStore(s => s.flatId)
  const subscription = useFlatStore(s => s.subscription)
  const members = useFlatStore(s => s.members)
  const { user } = useAuthStore()
  const [mounted, setMounted] = useState(false)

  useEffect(() => { setMounted(true) }, [])

  const currentMember = members.find(m => m.uid === user?.uid)
  const isAdmin = currentMember?.role === 'admin'
  const adminMember = members.find(m => m.role === 'admin')

  // Existing flats without subscription data: treat as active (legacy grace)
  // subscription is null until the flat doc listener fires
  const status = subscription?.status ?? null
  const daysLeft = daysRemaining(subscription?.trialEndDate ?? null)
  const showWarning = status === 'trial' && daysLeft <= 7
  const isExpired = status === 'expired'

  const handleUnlocked = () => {
    // The onSnapshot listener will pick up the Firestore change and update status
  }

  return (
    <>
      {/* Trial warning banner — injected above page content */}
      {mounted && !isExpired && showWarning && flatId && (
        <TrialBanner daysLeft={daysLeft} flatId={flatId} isAdmin={isAdmin} />
      )}

      {/* Expired gate — portal modal */}
      {mounted && isExpired && flatId && (
        isAdmin
          ? <AdminExpiredModal flatId={flatId} onUnlocked={handleUnlocked} />
          : <MemberExpiredModal
              flatId={flatId}
              adminName={adminMember?.nickname ?? 'your admin'}
              onUnlocked={handleUnlocked}
            />
      )}

      {children}
    </>
  )
}
