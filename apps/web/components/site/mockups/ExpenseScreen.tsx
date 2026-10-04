'use client'
import { useLoopStep } from '../useLoopStep'
import m from './mockups.module.css'

const SHARES = [{ n: 'Sai (paid)', v: 120 }, { n: 'Ravi', v: 120 }, { n: 'Meera', v: 120 }, { n: 'Arjun', v: 120 }]

export default function ExpenseScreen() {
  const { ref, step } = useLoopStep(SHARES.length + 1, 900)
  return (
    <div ref={ref}>
      <div className={m.appBar}>Groceries</div>
      <div className={m.list}>
        <div className={m.card} style={{ display: 'block' }}>
          <div style={{ color: '#64748B', fontSize: '.8em' }}>Paid by Sai</div>
          <div className={m.big}>₹480</div>
          <div className={m.split}>
            {SHARES.map((s, i) => (
              <div key={s.n} className={m.splitRow} style={{ opacity: step > i ? 1 : 0.25, transition: 'opacity .3s' }}>
                <span>{s.n}</span><b>₹{s.v}</b>
              </div>
            ))}
          </div>
        </div>
        <div className={m.card} style={{ fontSize: '.85em' }}>Ravi, Meera and Arjun each owe Sai ₹120</div>
      </div>
    </div>
  )
}
