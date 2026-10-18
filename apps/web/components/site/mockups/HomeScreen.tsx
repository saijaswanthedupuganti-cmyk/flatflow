'use client'
import Image from 'next/image'
import { Bell, Users, House, ChevronRight, Check, IndianRupee } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import Logo from '../Logo'
import { AppNav } from './AppParts'
import { HERO_BG } from '@/lib/site/photos'
import m from './mockups.module.css'

const TASKS = [
  { name: 'Kitchen cleanup', due: 'Was due yesterday', late: true },
  { name: 'Take out the trash', due: 'Due today', late: false },
  { name: 'Pay the wifi bill together', due: 'Due Oct 4', late: false },
]

/** The app's Home tab: photo hero, flat card, tasks/money split card and today's tasks being ticked off. */
export default function HomeScreen() {
  const { ref, step } = useLoopStep(TASKS.length + 2, 1600)
  return (
    <div ref={ref} style={{ height: '100%', background: '#F6F8F8', position: 'relative' }}>
      <div className={m.homeHero}>
        <Image src={HERO_BG} alt="" fill sizes="300px" style={{ objectFit: 'cover', objectPosition: '30% center' }} />
        <div className={m.topRow}>
          <Logo height="1.15em" decorative />
          <span className={m.circ}><Bell size="1.1em" aria-hidden /></span>
          <span className={m.circ}><Users size="1.1em" aria-hidden /></span>
          <span className={`${m.circ} ${m.circMe}`}>S</span>
        </div>
        <div className={m.date}>SUN, 18 OCTOBER</div>
        <div className={m.greet}>Good afternoon,<br />Sai <span aria-hidden>👋</span></div>
        <div className={m.heroSub}>1 task overdue. A calm look at Sunrise Heights 4B today.</div>
        <div className={m.gCard}>
          <span className={m.houseTile}><House size="1.2em" aria-hidden /></span>
          <div style={{ minWidth: 0 }}>
            <div style={{ fontSize: '.55em', letterSpacing: '.16em', opacity: .9 }}>YOUR FLAT</div>
            <div className={m.flatName}>Sunrise Heights 4B</div>
          </div>
          <ChevronRight size="1em" aria-hidden style={{ flexShrink: 0 }} />
          <div style={{ marginLeft: 'auto', display: 'grid', justifyItems: 'center', gap: '.2em' }}>
            <div className={m.stack} style={{ marginLeft: '.45em' }}>
              <span style={{ background: '#E6F6F3', color: '#0F766E' }}>S</span>
              <span style={{ background: '#F3E8D8', color: '#7A5A2E' }}>A</span>
              <span style={{ background: '#FCE8E6', color: '#B4372B' }}>R</span>
              <span style={{ background: 'rgba(255,255,255,.2)', color: '#fff' }}>+1</span>
            </div>
            <span style={{ fontSize: '.6em' }}>4 members</span>
          </div>
        </div>
        <div className={`${m.gCard} ${m.split}`}>
          <div>
            <span className={m.splitIcon} style={{ background: '#2BB39C' }}><Check size="1em" aria-hidden /></span>
            <div><div className={m.splitBig}>3</div><div style={{ fontSize: '.7em', fontWeight: 600 }}>Tasks for you</div><div className={m.splitSmall}>Due soon</div></div>
          </div>
          <div>
            <span className={m.splitIcon} style={{ border: '1px solid rgba(255,255,255,.4)' }}><IndianRupee size="1em" aria-hidden /></span>
            <div><div className={m.splitBig}>You owe<br />₹850</div><div className={m.splitSmall}>To 1 person</div></div>
          </div>
        </div>
      </div>
      <div className={m.todayPanel}>
        <div className={m.panelHead}><span>Today&apos;s tasks</span><span className={m.linkTeal}>See all →</span></div>
        {TASKS.map((t, i) => {
          const done = step > i
          return (
            <div key={t.name} className={`${m.taskRow} ${m.outline}`}>
              <span className={`${m.box} ${done ? m.boxOn : ''}`}><Check size="0.9em" strokeWidth={3} aria-hidden /></span>
              <div>
                <div className={m.taskName}>{t.name}</div>
                <div className={`${m.taskDue} ${t.late && !done ? m.late : ''}`}>{t.due}</div>
              </div>
              <span className={m.pill}>YOU</span>
            </div>
          )
        })}
      </div>
      <AppNav active="home" />
    </div>
  )
}
