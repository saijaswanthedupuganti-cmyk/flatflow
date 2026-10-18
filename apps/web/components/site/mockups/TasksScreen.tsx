'use client'
import { Check, Clock, ChevronRight } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import m from './mockups.module.css'

/** The app's Tasks tab. Loop: today's rotation task is ticked off and the turn passes to the next flatmate. */
export default function TasksScreen() {
  // 0 idle · 1 ticked · 2 turn handed to Rahul (final, also the reduced-motion state)
  const { ref, step } = useLoopStep(3, 1500)
  const done = step >= 1
  const passed = step >= 2
  return (
    <div ref={ref} className={m.page}>
      <div className={m.h1}>Tasks</div>
      <div className={m.todoRow}>
        <div><div className={m.label}>Your tasks</div><div className={m.todoBig}>3 to do</div></div>
        <span className={m.overduePill}>1 overdue</span>
      </div>
      <div className={m.tabs}>
        <span className={m.tabOn}>My tasks <span className={m.count}>3</span></span>
        <span>All tasks <span className={m.count}>7</span></span>
      </div>

      <div className={`${m.group} ${m.label}`}><span>Overdue</span><span>1</span></div>
      <div className={`${m.tCard} ${m.tCardLate} ${m.outline}`}>
        <span className={m.box} />
        <div className={m.tBody}><span className={m.taskName}>Kitchen cleanup</span><span className={m.taskDue}>Was due yesterday</span><span className={m.tag}>OVERDUE</span></div>
        <span className={m.who}><span className={`${m.av} ${m.avMint}`}>Y</span>YOU</span>
      </div>

      <div className={`${m.group} ${m.label}`}><span>Today</span><span>1</span></div>
      <div className={`${m.tCard} ${m.outline}`}>
        <span className={`${m.box} ${done ? m.boxOn : ''}`}><Check size="0.9em" strokeWidth={3} aria-hidden /></span>
        <div className={m.tBody}><span className={m.taskName}>Take out the trash</span><span className={m.taskDue}>Due today</span><span className={m.rot}><Clock size="1em" aria-hidden />Rotation</span></div>
        <span className={`${m.who} ${m.whoSwap}`}>
          <span className={passed ? m.off : ''}><span className={`${m.av} ${m.avMint}`}>Y</span>YOU</span>
          <span className={passed ? '' : m.off}><span className={`${m.av} ${m.avRose}`}>R</span>RAHUL</span>
        </span>
      </div>

      <div className={`${m.group} ${m.label}`}><span>Upcoming</span><span>1</span></div>
      <div className={`${m.tCard} ${m.outline}`}>
        <span className={m.box} />
        <div className={m.tBody}><span className={m.taskName}>Deep clean the bathroom and balcony before the landlord visit</span><span className={m.taskDue}>Due Oct 6</span><span className={m.rot}><Clock size="1em" aria-hidden />Rotation</span></div>
        <span className={m.who}><span className={`${m.av} ${m.avMint}`}>Y</span>YOU</span>
      </div>

      <div className={`${m.awayCard} ${m.outline}`}>
        <span className={m.awayIcon}><Clock size="1.2em" aria-hidden /></span>
        <div className={m.tBody}><span className={m.taskName}>Going away?</span><span className={m.taskDue}>Pause your turns while you&apos;re out of town.</span></div>
        <ChevronRight size="1em" aria-hidden style={{ flexShrink: 0 }} />
      </div>
    </div>
  )
}
