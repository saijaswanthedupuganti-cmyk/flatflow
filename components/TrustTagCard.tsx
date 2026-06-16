"use client"
import { useState, useEffect } from 'react'
import { Star, Shield, ChevronRight, ChevronDown, Info } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { db, hasKeys } from '@/lib/firebase'
import { collection, getDocs } from 'firebase/firestore'
import { computeTrustTag } from '@/lib/trustComputation'
import type { TrustTag } from '@/lib/discoveryTypes'

// ── Consent helpers (localStorage — device-scoped, fine for Phase 1) ──────────
type ConsentChoice = 'in' | 'out'
const consentKey = (uid: string) => `habitiq_rating_consent_${uid}`

function readConsent(uid: string): ConsentChoice | null {
  if (typeof window === 'undefined') return null
  const v = localStorage.getItem(consentKey(uid))
  return v === 'in' || v === 'out' ? v : null
}

function writeConsent(uid: string, choice: ConsentChoice) {
  localStorage.setItem(consentKey(uid), choice)
}

// ── Tier visual config ────────────────────────────────────────────────────────
const TIER = {
  0: {
    bg:   'bg-secondary',
    pill: 'bg-secondary text-muted-foreground',
    bar:  'bg-muted-foreground/40',
    icon: <Shield size={16} className="text-muted-foreground" />,
  },
  1: {
    bg:   'bg-violet-50 dark:bg-violet-900/20',
    pill: 'bg-violet-100 dark:bg-violet-900/40 text-violet-700 dark:text-violet-300',
    bar:  'bg-violet-500',
    icon: <Star size={16} className="text-violet-600 dark:text-violet-400" />,
  },
  2: {
    bg:   'bg-indigo-50 dark:bg-indigo-900/20',
    pill: 'bg-indigo-100 dark:bg-indigo-900/40 text-indigo-700 dark:text-indigo-300',
    bar:  'bg-indigo-500',
    icon: <Star size={16} className="text-indigo-600 dark:text-indigo-400" fill="currentColor" />,
  },
} as const

interface Props {
  uid: string
  flatId: string
  joinedAt: string | Date
}

export default function TrustTagCard({ uid, flatId, joinedAt }: Props) {
  const [consent, setConsent]       = useState<ConsentChoice | null>(null)
  const [tag, setTag]               = useState<TrustTag | null>(null)
  const [loading, setLoading]       = useState(false)
  const [showReasons, setShowReasons] = useState(false)

  // Hydrate consent from localStorage after mount (avoids SSR mismatch)
  useEffect(() => { setConsent(readConsent(uid)) }, [uid])

  // Fetch + compute whenever the user opts in
  useEffect(() => {
    if (consent !== 'in' || !flatId || !uid) return
    let cancelled = false
    setLoading(true)
    ;(async () => {
      try {
        let rawEvents: unknown[] = []
        if (hasKeys && db) {
          const snap = await getDocs(collection(db, `flats/${flatId}/behavioralEvents`))
          rawEvents = snap.docs.map(d => d.data())
        }
        if (cancelled) return
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        setTag(computeTrustTag(uid, flatId, rawEvents as any, joinedAt))
      } catch {
        /* non-critical — tag stays null, card shows nothing */
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => { cancelled = true }
  }, [consent, uid, flatId, joinedAt])

  const optIn  = () => { writeConsent(uid, 'in');  setConsent('in')  }
  const optOut = () => { writeConsent(uid, 'out'); setConsent('out'); setTag(null) }

  // ── Consent prompt ─────────────────────────────────────────────────────────
  if (consent === null) {
    return (
      <Card className="shadow-sm border-violet-200 dark:border-violet-800/40">
        <CardContent className="p-5 space-y-4">
          <div className="flex gap-3">
            <div className="w-9 h-9 rounded-xl bg-violet-100 dark:bg-violet-900/40 flex items-center justify-center shrink-0">
              <Star size={16} className="text-violet-600 dark:text-violet-400" />
            </div>
            <div className="space-y-1">
              <p className="font-bold text-sm">Your Habitiq Rating</p>
              <p className="text-xs text-muted-foreground leading-relaxed">
                We can compute a private rating using your in-app activity: chore on-time rate and
                settlement history. It is shown <strong>only to you</strong> — never to other members
                or the public. You can opt out at any time.
              </p>
              <div className="flex flex-col gap-1 pt-1 text-xs text-muted-foreground">
                <span className="flex items-center gap-1.5">
                  <span className="w-1.5 h-1.5 rounded-full bg-violet-500 shrink-0" />
                  Chore completion history (last 90 days)
                </span>
                <span className="flex items-center gap-1.5">
                  <span className="w-1.5 h-1.5 rounded-full bg-violet-500 shrink-0" />
                  Expense settlement history
                </span>
              </div>
            </div>
          </div>
          <div className="flex gap-2">
            <Button
              size="sm"
              className="flex-1 bg-violet-600 hover:bg-violet-700 text-white cursor-pointer transition-colors duration-150"
              onClick={optIn}
            >
              Turn on rating
            </Button>
            <Button
              size="sm"
              variant="outline"
              className="flex-1 cursor-pointer transition-colors duration-150"
              onClick={optOut}
            >
              Not now
            </Button>
          </div>
        </CardContent>
      </Card>
    )
  }

  // ── Opted out ──────────────────────────────────────────────────────────────
  if (consent === 'out') {
    return (
      <Card className="shadow-sm">
        <CardContent className="p-5">
          <div className="flex items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-secondary flex items-center justify-center shrink-0">
                <Shield size={16} className="text-muted-foreground" />
              </div>
              <div>
                <p className="font-bold text-sm">Habitiq Rating</p>
                <p className="text-xs text-muted-foreground mt-0.5">Unrated · opted out</p>
              </div>
            </div>
            <button
              onClick={optIn}
              className="text-xs text-violet-600 dark:text-violet-400 font-semibold hover:underline cursor-pointer"
            >
              Turn on
            </button>
          </div>
        </CardContent>
      </Card>
    )
  }

  // ── Opted in: loading skeleton ─────────────────────────────────────────────
  if (loading || !tag) {
    return (
      <Card className="shadow-sm">
        <CardContent className="p-5 space-y-3 animate-pulse">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-secondary shrink-0" />
            <div className="space-y-2 flex-1">
              <div className="h-3.5 bg-secondary rounded w-2/5" />
              <div className="h-2.5 bg-secondary rounded w-1/3" />
            </div>
          </div>
          <div className="h-1.5 bg-secondary rounded w-full" />
          <div className="h-2.5 bg-secondary rounded w-1/2" />
        </CardContent>
      </Card>
    )
  }

  // ── Opted in: tag card ─────────────────────────────────────────────────────
  const t = TIER[tag.tier]
  const pct = Math.round(tag.choreOnTimeRate * 100)

  return (
    <Card className="shadow-sm">
      <CardContent className="p-5 space-y-4">

        {/* Header */}
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-3">
            <div className={`w-9 h-9 rounded-xl ${t.bg} flex items-center justify-center shrink-0`}>
              {t.icon}
            </div>
            <div>
              <p className="font-bold text-sm">Habitiq Rating</p>
              <span className={`inline-flex items-center text-xs font-bold px-2 py-0.5 rounded-full mt-0.5 ${t.pill}`}>
                {tag.label}
              </span>
            </div>
          </div>
          <button
            onClick={() => setShowReasons(v => !v)}
            className="flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground cursor-pointer transition-colors duration-150"
            aria-label="Show reasons for this rating"
          >
            <Info size={13} />
            Why?
            {showReasons
              ? <ChevronDown size={13} />
              : <ChevronRight size={13} />
            }
          </button>
        </div>

        {/* Progress bar */}
        <div className="space-y-1.5">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-muted-foreground">Chores on time</span>
            <span className="text-xs font-bold tabular-nums">{pct}%</span>
          </div>
          <div
            className="w-full h-1.5 rounded-full bg-secondary overflow-hidden"
            role="progressbar"
            aria-valuenow={pct}
            aria-valuemin={0}
            aria-valuemax={100}
          >
            <div
              className={`h-full rounded-full ${t.bar} transition-all duration-300`}
              style={{ width: `${pct}%` }}
            />
          </div>
          <div className="flex items-center justify-between text-[11px] text-muted-foreground">
            <span>{tag.totalCompletions} chore{tag.totalCompletions !== 1 ? 's' : ''} logged (90 days)</span>
            <span>{tag.tenureDays}d on Habitiq</span>
          </div>
        </div>

        {/* Reasons panel */}
        {showReasons && (
          <div className={`rounded-xl p-3.5 space-y-1.5 ${t.bg}`}>
            {tag.reasons.map((r, i) => (
              <p key={i} className="text-xs font-medium text-foreground/80 flex gap-2">
                <span className="shrink-0 mt-px">·</span>
                <span>{r}</span>
              </p>
            ))}
          </div>
        )}

        {/* Opt-out */}
        <button
          onClick={optOut}
          className="text-[11px] text-muted-foreground/50 hover:text-muted-foreground cursor-pointer transition-colors duration-150"
        >
          Opt out of rating
        </button>

      </CardContent>
    </Card>
  )
}
