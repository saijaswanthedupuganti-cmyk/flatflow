'use client'
import { Check, ReceiptText } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import m from './mockups.module.css'

const MEMBERS = ['You', 'Rahul', 'Meera', 'Anita']

/**
 * Add expense: ₹1,200 of groceries, equal split. The loop unticks Anita (she wasn't part of it), so the
 * per-person share moves from ₹300 to ₹400. The final, reduced-motion frame is the "some people" split.
 */
export default function AddExpenseScreen() {
  const { ref, step } = useLoopStep(3, 1500)
  const anitaOut = step >= 1
  const count = anitaOut ? 3 : 4
  return (
    <div ref={ref} className={m.page}>
      <div className={m.h1}>Add expense</div>

      <div className={m.field}>
        <span className={m.fieldLabel}>What was it for?</span>
        <span className={`${m.fieldBox} ${m.outline}`}><ReceiptText size="1.1em" aria-hidden style={{ color: 'var(--or-text-2)', flexShrink: 0 }} />Groceries</span>
      </div>
      <div className={m.field}>
        <span className={m.fieldLabel}>Amount (₹)</span>
        <span className={`${m.fieldBox} ${m.outline}`}><b className={m.code}>1,200</b></span>
      </div>

      <div className={m.fieldLabel} style={{ marginTop: '4.5%' }}>Paid by</div>
      <div className={m.chips} style={{ marginTop: '2%' }}>
        <span className={`${m.chip} ${m.chipOn}`}><Check strokeWidth={3} aria-hidden /><span>You</span></span>
        <span className={m.chip}><span>Rahul</span></span>
        <span className={m.chip}><span>Meera</span></span>
      </div>

      <div className={m.fieldLabel} style={{ marginTop: '4.5%' }}>Split</div>
      <div className={m.chips} style={{ marginTop: '2%' }}>
        <span className={`${m.chip} ${m.chipOn}`}><Check strokeWidth={3} aria-hidden /><span>Equal split</span></span>
        <span className={m.chip}><span>Custom split</span></span>
      </div>
      <div className={m.muted} style={{ marginTop: '3%' }}>Shared equally between everyone checked below.</div>

      <div className={`${m.checkList} ${m.outline}`}>
        {MEMBERS.map(n => {
          const on = !(n === 'Anita' && anitaOut)
          return (
            <div key={n} className={m.checkRow}>
              <span className={`${m.box} ${on ? m.boxOn : ''}`}><Check size="0.9em" strokeWidth={3} aria-hidden /></span>
              <span style={{ flex: 1, opacity: on ? 1 : 0.5, transition: 'opacity 200ms var(--or-ease)' }}>{n}</span>
              <span className={m.share} style={{ opacity: on ? 1 : 0 }}>₹{1200 / count}</span>
            </div>
          )
        })}
      </div>
    </div>
  )
}
