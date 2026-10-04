'use client'
import { Zap, Wifi, Home as HomeIcon } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import m from './mockups.module.css'

const BILLS = [
  { n: 'Rent', a: '₹48,000', icon: HomeIcon, c: '#8B5CF6' },
  { n: 'Electricity', a: '₹2,140', icon: Zap, c: '#F5A524' },
  { n: 'Wifi', a: '₹1,200', icon: Wifi, c: '#2F6BFF' },
]

export default function BillsScreen() {
  const { ref, step } = useLoopStep(BILLS.length + 1, 1300)
  return (
    <div ref={ref}>
      <div className={m.appBar}>October bills</div>
      <div className={m.list}>
        {BILLS.map((b, i) => {
          const paid = step > i
          const Icon = b.icon
          return (
            <div key={b.n} className={m.card}>
              <span className={m.avatar} style={{ background: b.c }}><Icon size="1em" aria-hidden /></span>
              <div><b>{b.n}</b><div style={{ fontSize: '.8em', color: '#64748B' }}>{b.a}</div></div>
              <span className={m.chip} style={{ background: paid ? '#E6FAF6' : '#FFF4E0', color: paid ? '#0B7A6C' : '#9A5B00' }}>{paid ? 'Paid' : 'Due 5th'}</span>
            </div>
          )
        })}
        <div className={m.card} style={{ fontSize: '.85em' }}>{step >= BILLS.length ? 'Month closed. Everyone is settled.' : 'Close the month when all bills are in'}</div>
      </div>
    </div>
  )
}
