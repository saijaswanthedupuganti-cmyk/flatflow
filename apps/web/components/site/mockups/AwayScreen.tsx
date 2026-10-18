'use client'
import { Clock, ChevronRight } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import m from './mockups.module.css'

/** Tasks › Going away?: switch on away and your turns pass to the rest of the flat. */
export default function AwayScreen() {
  const { ref, step } = useLoopStep(2, 2200)
  const away = step === 1
  const fade = { opacity: away ? 1 : 0.35, transition: 'opacity 300ms var(--or-ease)' }
  return (
    <div ref={ref} className={m.page}>
      <div className={m.h1}>Tasks</div>
      <div className={`${m.awayCard} ${m.outline}`} style={{ marginTop: '8%' }}>
        <span className={m.awayIcon}><Clock size="1.2em" aria-hidden /></span>
        <div className={m.tBody}><span className={m.taskName}>Going away?</span><span className={m.taskDue}>Pause your turns while you&apos;re out of town.</span></div>
        <ChevronRight size="1em" aria-hidden style={{ flexShrink: 0 }} />
      </div>
      <div className={`${m.awayCard} ${m.outline}`}>
        <div className={m.tBody}><span className={m.taskName}>I&apos;m away</span><span className={m.taskDue}>Oct 28 – Nov 3</span></div>
        <span className={`${m.toggle} ${away ? m.toggleOn : ''}`} />
      </div>
      <div className={`${m.awayCard} ${m.outline}`} style={fade}>
        <span className={m.taskDue}>Your turns pass to Rahul and Anita until you&apos;re back.</span>
      </div>
      <div className={`${m.awayCard} ${m.outline}`} style={fade}>
        <span className={m.taskDue}>Flatmates see you&apos;re away, so nobody chases you.</span>
      </div>
    </div>
  )
}
