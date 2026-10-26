'use client'
import { ShieldCheck } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import m from './mockups.module.css'

/** Title, dark-teal balance card and the Daily splits / Monthly bills tabs, shared by both Expenses mockups. */
export function ExpensesTop({ tab }: { tab: 'daily' | 'monthly' }) {
  return (
    <>
      <div className={m.h1}>Expenses</div>
      <div className={m.balance}>
        <div className={m.label}>Your balance</div>
        <div className={m.balBig}>You owe ₹850</div>
        <div className={m.balSmall}>To 1 person</div>
        <div className={m.balBig}>You&apos;re owed ₹1,240.50</div>
        <div className={m.balSmall}>From 2 people</div>
      </div>
      <div className={m.tabs}>
        <span className={tab === 'daily' ? m.tabOn : ''}>Daily splits</span>
        <span className={tab === 'monthly' ? m.tabOn : ''}>Monthly bills</span>
      </div>
    </>
  )
}

const PEOPLE = [
  { n: 'Rahul', rel: 'You owe', amt: '₹850', cls: '', acts: ['Details', 'Record a settlement'] },
  { n: 'Meera', rel: 'Owes you', amt: '₹840.50', cls: '', acts: ['Mark received'] },
  { n: 'Anita', rel: 'Owes you', amt: '₹400', cls: m.avRose, acts: ['Mark received'] },
]

/** Expenses › Daily splits: the per-person breakdown fills in, one flatmate at a time. */
export default function ExpenseScreen() {
  const { ref, step } = useLoopStep(PEOPLE.length + 1, 1000)
  return (
    <div ref={ref} className={m.page}>
      <ExpensesTop tab="daily" />
      <div className={m.breakTitle}>Breakdown</div>
      {PEOPLE.map((p, i) => (
        <div key={p.n} className={m.person} style={{ opacity: step > i ? 1 : 0.2 }}>
          <div className={m.personTop}>
            <span className={`${m.av} ${p.cls}`}>{p.n[0]}</span>
            <div><b>{p.n}</b><div className={m.muted}>{p.rel}</div></div>
            <span className={m.amt}>{p.amt}</span>
          </div>
          <div className={m.personActs}>{p.acts.map(a => <span key={a} className={m.linkTeal}>{a}</span>)}</div>
        </div>
      ))}
      <div className={m.notice}><ShieldCheck size="1.3em" aria-hidden style={{ flexShrink: 0 }} />Habitiq records settlements but does not process payments.</div>
    </div>
  )
}
