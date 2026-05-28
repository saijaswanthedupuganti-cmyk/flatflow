"use client"
import { useState } from 'react'
import { X, Star, Send } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { submitNps } from '@/lib/npsService'

interface Props {
  uid: string
  nickname: string
  flatId: string
  onDone: () => void
}

const SCORE_COLORS: Record<number, string> = {
  0: 'bg-red-500 text-white border-red-500',
  1: 'bg-red-400 text-white border-red-400',
  2: 'bg-red-400 text-white border-red-400',
  3: 'bg-orange-500 text-white border-orange-500',
  4: 'bg-orange-400 text-white border-orange-400',
  5: 'bg-orange-400 text-white border-orange-400',
  6: 'bg-yellow-500 text-white border-yellow-500',
  7: 'bg-yellow-400 text-white border-yellow-400',
  8: 'bg-lime-500 text-white border-lime-500',
  9: 'bg-green-500 text-white border-green-500',
  10: 'bg-green-600 text-white border-green-600',
}
const UNSELECTED = 'bg-background border-border text-foreground hover:border-primary/50 hover:bg-primary/5'

function scoreLabel(score: number | null) {
  if (score === null) return ''
  if (score <= 6) return '😞 Detractor — you have concerns'
  if (score <= 8) return '😐 Passive — fairly satisfied'
  return '😊 Promoter — you love it!'
}

export default function NPSBanner({ uid, nickname, flatId, onDone }: Props) {
  const [selected, setSelected] = useState<number | null>(null)
  const [comment, setComment] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [submitted, setSubmitted] = useState(false)

  const handleDismiss = () => {
    if (typeof window !== 'undefined') {
      localStorage.setItem(`nps_dismissed_${uid}`, '1')
    }
    onDone()
  }

  const handleSubmit = async () => {
    if (selected === null) return
    setSubmitting(true)
    await submitNps({ uid, nickname, flatId, score: selected, comment })
    if (typeof window !== 'undefined') {
      localStorage.setItem(`nps_submitted_${uid}`, '1')
    }
    setSubmitted(true)
    setSubmitting(false)
    setTimeout(onDone, 2200)
  }

  if (submitted) {
    return (
      <div className="rounded-2xl border-2 border-green-300 dark:border-green-700 bg-green-50 dark:bg-green-950/30 p-5 flex items-center gap-4">
        <div className="w-10 h-10 rounded-xl bg-green-100 dark:bg-green-900/50 flex items-center justify-center shrink-0">
          <Star size={18} className="text-green-600 dark:text-green-400 fill-current" />
        </div>
        <div>
          <p className="font-bold text-sm text-green-800 dark:text-green-200">Thanks for your feedback! 🎉</p>
          <p className="text-xs text-green-700 dark:text-green-400 mt-0.5">It helps us make FlatFlow better for everyone.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="rounded-2xl border-2 border-primary/20 bg-primary/5 dark:bg-primary/10 p-5 space-y-4">
      {/* Header */}
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-3">
          <div className="w-9 h-9 rounded-xl bg-primary/10 flex items-center justify-center shrink-0 mt-0.5">
            <Star size={16} className="text-primary" />
          </div>
          <div>
            <p className="text-xs font-bold text-primary uppercase tracking-widest mb-0.5">Quick Question</p>
            <p className="text-sm font-semibold leading-snug">
              How likely are you to recommend FlatFlow to a friend?
            </p>
          </div>
        </div>
        <button
          onClick={handleDismiss}
          className="text-muted-foreground hover:text-foreground p-1 shrink-0 mt-0.5"
          aria-label="Dismiss"
        >
          <X size={15} />
        </button>
      </div>

      {/* Score picker */}
      <div className="space-y-2">
        <div className="grid grid-cols-11 gap-1">
          {Array.from({ length: 11 }, (_, i) => (
            <button
              key={i}
              onClick={() => setSelected(i)}
              className={`h-9 rounded-lg border-2 text-xs font-bold transition-all ${
                selected === i ? SCORE_COLORS[i] : UNSELECTED
              }`}
            >
              {i}
            </button>
          ))}
        </div>
        <div className="flex justify-between">
          <span className="text-[10px] text-muted-foreground">Not at all likely</span>
          <span className="text-[10px] text-muted-foreground">Extremely likely</span>
        </div>
        {selected !== null && (
          <p className="text-xs font-semibold text-center text-muted-foreground">{scoreLabel(selected)}</p>
        )}
      </div>

      {/* Comment */}
      {selected !== null && (
        <div className="space-y-1">
          <label className="text-xs font-bold text-muted-foreground">
            {selected <= 6 ? "What could we improve?" : selected <= 8 ? "What would make it a 10?" : "What do you love most?"}
            <span className="font-normal ml-1">(optional)</span>
          </label>
          <textarea
            value={comment}
            onChange={e => setComment(e.target.value)}
            maxLength={300}
            rows={2}
            placeholder="Your thoughts..."
            className="w-full bg-background border border-border rounded-lg px-3 py-2 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-primary/30"
          />
        </div>
      )}

      {/* Actions */}
      <div className="flex gap-2 justify-end">
        <button
          onClick={handleDismiss}
          className="text-xs text-muted-foreground hover:text-foreground px-3 py-1.5 transition-colors"
        >
          Maybe later
        </button>
        <Button
          size="sm"
          disabled={selected === null || submitting}
          onClick={handleSubmit}
          className="gap-1.5 font-bold text-xs px-4"
        >
          <Send size={12} />
          {submitting ? 'Sending…' : 'Submit'}
        </Button>
      </div>
    </div>
  )
}
