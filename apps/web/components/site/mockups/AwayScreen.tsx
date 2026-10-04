'use client'
import { Plane } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import m from './mockups.module.css'

export default function AwayScreen() {
  const { ref, step } = useLoopStep(2, 2200)
  const away = step === 1
  return (
    <div ref={ref}>
      <div className={m.appBar}>Going home for Diwali</div>
      <div className={m.list}>
        <div className={m.card}>
          <span className={m.avatar} style={{ background: '#8B5CF6' }}><Plane size="1em" aria-hidden /></span>
          <div><b>I&apos;m away</b><div style={{ fontSize: '.8em', color: '#64748B' }}>Oct 28 – Nov 3</div></div>
          <span className={`${m.toggle} ${away ? m.toggleOn : ''}`} />
        </div>
        <div className={m.card} style={{ fontSize: '.85em', opacity: away ? 1 : .35, transition: 'opacity .4s' }}>Your trash turns move to Ravi and Meera</div>
        <div className={m.card} style={{ fontSize: '.85em', opacity: away ? 1 : .35, transition: 'opacity .4s' }}>Flatmates see you&apos;re away, no chasing</div>
      </div>
    </div>
  )
}
