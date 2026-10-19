'use client'
import { Zap, Wifi, House } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import { ExpensesTop } from './ExpenseScreen'
import m from './mockups.module.css'

const BILLS = [
  { n: 'Rent', a: '₹48,000', icon: House },
  { n: 'Electricity', a: '₹2,140', icon: Zap },
  { n: 'Wifi', a: '₹1,200', icon: Wifi },
]

/** Expenses › Monthly bills: each bill is marked paid in turn, then the month closes. */
export default function BillsScreen() {
  const { ref, step } = useLoopStep(BILLS.length + 1, 1300)
  return (
    <div ref={ref} className={m.page}>
      <ExpensesTop tab="monthly" />
      <div className={m.breakTitle}>October bills</div>
      {BILLS.map((b, i) => {
        const paid = step > i
        const Icon = b.icon
        return (
          <div key={b.n} className={m.bill}>
            <span className={m.billIcon}><Icon size="1.1em" aria-hidden /></span>
            <div><div className={m.taskName}>{b.n}</div><div className={m.muted}>{b.a}</div></div>
            <span className={m.state}>
              <span className={`${m.pill} ${m.duePill} ${paid ? m.off : ''}`}>Due 5th</span>
              <span className={`${m.pill} ${m.paidPill} ${paid ? '' : m.off}`}>Paid</span>
            </span>
          </div>
        )
      })}
      <div className={m.notice}>{step >= BILLS.length ? 'Month closed. Everyone is settled.' : 'Close the month when all bills are in.'}</div>
    </div>
  )
}
