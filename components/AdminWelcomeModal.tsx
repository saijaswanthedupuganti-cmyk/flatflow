"use client"
import { useState, useEffect } from 'react'
import { createPortal } from 'react-dom'
import { Home, ClipboardList, Receipt, RefreshCw, Users, X, Loader2, AlertTriangle, Crown, Sparkles, ArrowRight, ChevronRight } from 'lucide-react'
import { useFlatStore } from '@/store/useFlatStore'
import { validateAndRedeemCoupon } from '@/lib/couponService'

const STORAGE_KEY = (flatId: string) => `habitiq_admin_welcome_v1_${flatId}`

const FEATURES = [
  { icon: ClipboardList, label: 'Create & assign tasks', sub: 'Chore rotation, deadlines, tracking' },
  { icon: Receipt,       label: 'Track shared expenses', sub: 'Splits, balances, settlements' },
  { icon: RefreshCw,     label: 'Manage monthly bills',  sub: 'Rent, electricity, wifi — automated' },
  { icon: Users,         label: 'Up to 3 flats',          sub: 'Switch between your homes easily' },
]

interface Props {
  flatId: string
  onClose: () => void
}

export default function AdminWelcomeModal({ flatId, onClose }: Props) {
  const [mounted, setMounted] = useState(false)
  const [code, setCode] = useState('HAB-WELCOME')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [celebrated, setCelebrated] = useState(false)

  useEffect(() => { setMounted(true) }, [])

  const dismiss = () => {
    try { localStorage.setItem(STORAGE_KEY(flatId), '1') } catch { /* ignore */ }
    onClose()
  }

  const handleClaim = async () => {
    const trimmed = code.trim().toUpperCase()
    if (!trimmed) return
    setLoading(true)
    setError('')
    const result = await validateAndRedeemCoupon(flatId, trimmed)
    setLoading(false)
    if (result.success) {
      try { localStorage.setItem(STORAGE_KEY(flatId), '1') } catch { /* ignore */ }
      setCelebrated(true)
    } else {
      setError(result.error)
    }
  }

  if (!mounted) return null

  const modal = celebrated ? (
    /* ── Success screen ── */
    <div
      className="fixed inset-0 z-[400] flex items-center justify-center p-4"
      style={{ background: 'rgba(0,0,0,0.88)', backdropFilter: 'blur(14px)' }}
    >
      <div
        className="w-full max-w-sm rounded-[28px] overflow-hidden text-center"
        style={{
          background: 'linear-gradient(160deg, #0a1f14 0%, #0d2010 100%)',
          border: '1px solid rgba(16,185,129,0.25)',
        }}
      >
        <div className="h-px w-full" style={{ background: 'linear-gradient(90deg, transparent, #10b981, transparent)' }} />
        <div className="px-6 pt-10 pb-6 space-y-5">
          <div className="flex flex-col items-center gap-3">
            <div
              className="w-20 h-20 rounded-full flex items-center justify-center"
              style={{ background: 'linear-gradient(135deg, #059669, #34d399)', boxShadow: '0 0 40px rgba(16,185,129,0.4)' }}
            >
              <Crown size={36} className="text-white" />
            </div>
            <div>
              <p className="text-[22px] font-black text-white">You&apos;re Premium! 👑</p>
              <p className="text-[13px] text-emerald-400 font-semibold mt-1">90 days of full access — enjoy!</p>
            </div>
          </div>
          <p className="text-[13px] text-white/60 leading-relaxed">
            Create tasks, track expenses, manage bills. Your flat is fully unlocked.
          </p>
          <button
            onClick={onClose}
            className="w-full py-3.5 rounded-xl font-bold text-[14px] text-white cursor-pointer"
            style={{ background: 'linear-gradient(135deg, #059669, #10b981)', boxShadow: '0 4px 20px rgba(16,185,129,0.4)' }}
          >
            Let&apos;s go →
          </button>
        </div>
        <div className="h-px w-full" style={{ background: 'linear-gradient(90deg, transparent, rgba(16,185,129,0.3), transparent)' }} />
      </div>
    </div>
  ) : (
    /* ── Welcome screen ── */
    <div
      className="fixed inset-0 z-[400] flex items-end sm:items-center justify-center p-0 sm:p-4"
      style={{ background: 'rgba(0,0,0,0.75)', backdropFilter: 'blur(10px)' }}
    >
      <div
        className="w-full sm:max-w-md rounded-t-[28px] sm:rounded-[28px] overflow-hidden"
        style={{ background: 'var(--card)', border: '1px solid var(--border)' }}
      >
        {/* Header */}
        <div
          className="relative px-5 pt-8 pb-10 flex flex-col items-center text-center overflow-hidden"
          style={{ background: 'linear-gradient(160deg, #1e1b4b 0%, #312e81 50%, #0f172a 100%)' }}
        >
          {/* Glow */}
          <div className="absolute inset-0 pointer-events-none" style={{ background: 'radial-gradient(ellipse at 50% 0%, rgba(99,102,241,0.25) 0%, transparent 65%)' }} />

          <button
            onClick={dismiss}
            className="absolute top-4 right-4 p-1.5 rounded-full cursor-pointer transition-colors"
            style={{ background: 'rgba(255,255,255,0.08)' }}
          >
            <X size={14} className="text-white/50" />
          </button>

          {/* Icon */}
          <div
            className="relative z-10 w-16 h-16 rounded-2xl flex items-center justify-center mb-4"
            style={{ background: 'linear-gradient(135deg, #4f46e5, #7c3aed)', boxShadow: '0 8px 32px rgba(79,70,229,0.5)' }}
          >
            <Home size={28} className="text-white" />
          </div>

          <div className="relative z-10 space-y-1.5">
            <div className="flex items-center justify-center gap-2">
              <p className="text-[22px] font-black text-white leading-tight">Your flat is ready! 🎉</p>
            </div>
            <p className="text-[13px] text-indigo-200/70 leading-relaxed max-w-xs">
              Unlock the full experience free — create tasks, track expenses, and manage bills with your flatmates.
            </p>
          </div>

          {/* Free badge */}
          <div
            className="relative z-10 mt-4 flex items-center gap-1.5 px-3 py-1.5 rounded-full"
            style={{ background: 'rgba(52,211,153,0.15)', border: '1px solid rgba(52,211,153,0.3)' }}
          >
            <Sparkles size={11} className="text-emerald-400" />
            <span className="text-[11px] font-extrabold text-emerald-400">90 days FREE — no credit card</span>
          </div>
        </div>

        {/* Feature list */}
        <div className="-mt-5 mx-4 rounded-2xl overflow-hidden" style={{ background: 'var(--card)', border: '1px solid var(--border)', boxShadow: '0 4px 20px rgba(0,0,0,0.08)' }}>
          <div className="px-4 pt-4 pb-2 grid grid-cols-2 gap-2">
            {FEATURES.map(({ icon: Icon, label, sub }) => (
              <div key={label} className="flex items-start gap-2.5 p-2.5 rounded-xl" style={{ background: 'var(--secondary)', opacity: 0.9 }}>
                <div className="w-8 h-8 rounded-lg flex items-center justify-center shrink-0" style={{ background: 'var(--primary)', opacity: 0.12 }}>
                  <Icon size={15} className="text-primary" />
                </div>
                <div className="min-w-0">
                  <p className="text-[11px] font-bold text-foreground leading-snug">{label}</p>
                  <p className="text-[10px] text-muted-foreground mt-0.5 leading-snug">{sub}</p>
                </div>
              </div>
            ))}
          </div>

          {/* Also note for members */}
          <div className="mx-3 mb-3 flex items-center gap-2 px-3 py-2 rounded-xl" style={{ background: 'rgba(99,102,241,0.06)', border: '1px dashed rgba(99,102,241,0.2)' }}>
            <Users size={12} className="text-primary shrink-0" />
            <p className="text-[11px] text-muted-foreground leading-snug">
              <span className="font-semibold text-foreground">Everyone in your flat</span> gets full Premium access once activated.
            </p>
          </div>

          {/* Coupon input */}
          <div className="px-4 pb-4 space-y-3">
            <div>
              <label className="text-[10px] font-extrabold text-muted-foreground uppercase tracking-widest mb-1.5 block">
                Your free coupon — ready to use
              </label>
              <div className="flex gap-2">
                <input
                  type="text"
                  value={code}
                  onChange={e => { setCode(e.target.value.toUpperCase()); setError('') }}
                  onKeyDown={e => e.key === 'Enter' && handleClaim()}
                  placeholder="COUPON-CODE"
                  maxLength={32}
                  className="flex-1 px-3 py-2.5 text-sm font-mono tracking-widest rounded-xl border border-border bg-background focus:outline-none focus:ring-2 focus:ring-primary/40 uppercase placeholder:text-muted-foreground/30"
                />
                <button
                  onClick={handleClaim}
                  disabled={loading || !code.trim()}
                  className="px-4 py-2.5 rounded-xl text-sm font-bold text-white disabled:opacity-50 cursor-pointer transition-all active:scale-95 flex items-center gap-2 whitespace-nowrap"
                  style={{ background: 'linear-gradient(135deg, #4f46e5, #7c3aed)', boxShadow: '0 4px 16px rgba(79,70,229,0.35)' }}
                >
                  {loading ? <Loader2 size={14} className="animate-spin" /> : <ArrowRight size={14} />}
                  {loading ? 'Activating…' : 'Claim Free'}
                </button>
              </div>
              {error && (
                <p className="text-xs text-red-500 flex items-center gap-1.5 mt-1.5">
                  <AlertTriangle size={11} />
                  {error}
                </p>
              )}
            </div>

            <button
              onClick={dismiss}
              className="w-full py-2 text-[12px] text-muted-foreground hover:text-foreground transition-colors cursor-pointer flex items-center justify-center gap-1"
            >
              Explore first, activate later <ChevronRight size={12} />
            </button>
          </div>
        </div>

        <div className="h-5" />
      </div>
    </div>
  )

  return createPortal(modal, document.body)
}

// Helper: should we show the welcome modal?
export function shouldShowAdminWelcome(flatId: string): boolean {
  try {
    return !localStorage.getItem(STORAGE_KEY(flatId))
  } catch {
    return false
  }
}
