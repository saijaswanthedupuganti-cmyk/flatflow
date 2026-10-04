'use client'
import { RefreshCw } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import m from './mockups.module.css'

const PEOPLE = [{ n: 'Sai', c: '#2F6BFF' }, { n: 'Ravi', c: '#14B8A6' }, { n: 'Meera', c: '#8B5CF6' }, { n: 'Arjun', c: '#F5A524' }]
const DAYS = ['Mon', 'Tue', 'Wed', 'Thu']

export default function TasksScreen() {
  const { ref, step } = useLoopStep(PEOPLE.length, 1600)
  return (
    <div ref={ref}>
      <div className={m.appBar}>Trash rotation</div>
      <div className={m.list}>
        {DAYS.map((d, i) => {
          const p = PEOPLE[(i + step) % PEOPLE.length]
          return (
            <div key={d} className={m.card}>
              <span className={m.avatar} style={{ background: p.c, transition: 'background-color .4s' }}>{p.n[0]}</span>
              <div><b>{p.n}</b><div style={{ fontSize: '.8em', color: '#64748B' }}>{d}</div></div>
              {i === 0 && <span className={m.chip} style={{ background: '#E6FAF6', color: '#0B7A6C' }}>Today</span>}
            </div>
          )
        })}
        <div className={m.card} style={{ color: '#475569', fontSize: '.85em' }}><RefreshCw size="1.1em" aria-hidden />Rotates every day, skips anyone away</div>
      </div>
    </div>
  )
}
