'use client'
import { Home, Check, IndianRupee, Users, AudioLines } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import m from './mockups.module.css'

const TASKS = [
  { name: 'Take out the trash', who: 'You · today' },
  { name: 'Buy drinking water', who: 'Ravi · today' },
  { name: 'Clean the kitchen', who: 'Meera · tomorrow' },
]

export default function HomeScreen() {
  const { ref, step } = useLoopStep(TASKS.length + 1, 1800)
  return (
    <div ref={ref} style={{ height: '100%' }}>
      <div className={`${m.homeHero} or-golden`}>
        <div className={m.brandRow}><Home size="1.1em" aria-hidden />Oddroof</div>
        <div className={m.date}>SAT, 3 OCTOBER</div>
        <div className={m.greet}>Good evening,<br />Sai</div>
      </div>
      <div className={m.glassCard} style={{ marginTop: '-14%' }}>
        <span className={m.tile} style={{ background: 'rgba(20,184,166,.85)' }}><Users size="1.2em" aria-hidden /></span>
        <div><div className={m.muted} style={{ color: 'rgba(255,255,255,.8)' }}>YOUR FLAT</div><b>Sai flat</b></div>
        <span style={{ marginLeft: 'auto', fontSize: '.8em' }}>5 members</span>
      </div>
      <div className={m.glassCard}>
        <span className={m.tile} style={{ background: 'rgba(255,255,255,.25)' }}><IndianRupee size="1.2em" aria-hidden /></span>
        <div><b>All settled</b><div style={{ fontSize: '.8em', opacity: .85 }}>No dues</div></div>
      </div>
      <div className={m.panel}>
        <div className={m.panelTitle}><span>Today&apos;s tasks</span><span style={{ color: '#0B7A6C', fontSize: '.8em' }}>See all</span></div>
        {TASKS.map((t, i) => (
          <div key={t.name} className={m.taskRow}>
            <span className={`${m.check} ${step > i ? m.checkOn : ''}`}>{step > i && <Check size="0.9em" aria-hidden />}</span>
            <div><div style={{ fontWeight: 600, textDecoration: step > i ? 'line-through' : 'none' }}>{t.name}</div><div className={m.muted}>{t.who}</div></div>
          </div>
        ))}
      </div>
      <div className={m.navBar}>
        <span>Home</span><span>Tasks</span>
        <span className={m.navMic}><AudioLines size="1.3em" aria-hidden /></span>
        <span>Discover</span><span>Profile</span>
      </div>
    </div>
  )
}
