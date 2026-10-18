'use client'
import { useEffect, useRef, useState } from 'react'
import { motion, useInView } from 'framer-motion'
import { RefreshCw, IndianRupee, Receipt, Plane } from 'lucide-react'
import Section from '../Section'
import Glass from '../Glass'
import PhoneFrame from '../mockups/PhoneFrame'
import TasksScreen from '../mockups/TasksScreen'
import ExpenseScreen from '../mockups/ExpenseScreen'
import BillsScreen from '../mockups/BillsScreen'
import AwayScreen from '../mockups/AwayScreen'
import { EASE } from '../motion'
import h from './home.module.css'

const STEPS = [
  { title: 'Tasks rotate fairly', body: 'Set a chore once. Oddroof hands it to the next person every time, and skips whoever is away.', icon: RefreshCw, color: 'var(--or-teal)', Screen: TasksScreen, label: 'Tasks: the trash turn is done and passes to the next flatmate' },
  { title: 'Expenses split themselves', body: 'Add what you paid. Everyone sees their share and who owes whom, without a spreadsheet.', icon: IndianRupee, color: 'var(--or-blue)', Screen: ExpenseScreen, label: 'Expenses: your balance and what each flatmate owes or is owed' },
  { title: 'Bills and month close', body: 'Rent, electricity and wifi in one list. Mark them paid and close the month with everyone settled.', icon: Receipt, color: 'var(--or-navy)', Screen: BillsScreen, label: 'Expenses, monthly bills: rent, electricity and wifi being marked paid' },
  { title: 'Away mode', body: 'Going home for a festival? Switch on away and your turns pass on until you are back.', icon: Plane, color: 'var(--or-teal)', Screen: AwayScreen, label: 'Going away: pause your turns while you are out of town' },
] as const

function Step({ i, onActive, children }: { i: number; onActive: (i: number) => void; children: React.ReactNode }) {
  const ref = useRef<HTMLElement>(null)
  const inView = useInView(ref, { amount: 0.6 })
  useEffect(() => { if (inView) onActive(i) }, [inView, i, onActive])
  return <article ref={ref} data-story-step={i}>{children}</article>
}

export default function PinnedStory() {
  const [active, setActive] = useState(0)
  return (
    <Section id="how-it-works" tone="canvas" eyebrow="How it works"
      title="Everything a shared flat argues about, settled."
      lede="Four everyday jobs, handled the same fair way for everyone in the flat.">
      <div className={h.story}>
        <div className={h.storySteps}>
          {STEPS.map((s, i) => {
            const Icon = s.icon
            return (
              <Step key={s.title} i={i} onActive={setActive}>
                <Glass tier="canvas" className={`${h.step} ${active === i ? h.stepActive : ''}`}>
                  <span className={h.stepDot} style={{ background: s.color }}><Icon size={20} aria-hidden /></span>
                  <h3>{s.title}</h3>
                  <p>{s.body}</p>
                  <div className={h.stepPhone}><PhoneFrame label={s.label}><s.Screen /></PhoneFrame></div>
                </Glass>
              </Step>
            )
          })}
        </div>
        <div className={h.storyPhoneCol}>
          <div className={h.storyPhoneSticky} data-active-screen={String(active)}>
            <div className={h.storyPhone}>
              <PhoneFrame label={STEPS[active].label}>
                {/* All four screens stay mounted and crossfade in place: no blank phone between steps. */}
                <div className={h.storyStack}>
                  {STEPS.map((st, i) => (
                    <motion.div key={st.title} className={h.storyLayer} initial={false}
                      animate={{ opacity: active === i ? 1 : 0, scale: active === i ? 1 : 0.98 }}
                      transition={{ duration: 0.24, ease: EASE }}
                      style={{ zIndex: active === i ? 1 : 0, pointerEvents: active === i ? 'auto' : 'none' }}>
                      <st.Screen />
                    </motion.div>
                  ))}
                </div>
              </PhoneFrame>
            </div>
            <ol className={h.storyRail} aria-label="Progress">
              {STEPS.map((st, i) => <li key={st.title} data-on={active === i ? '' : undefined} aria-current={active === i ? 'step' : undefined}><span className="sr-only">{st.title}</span></li>)}
            </ol>
          </div>
        </div>
      </div>
    </Section>
  )
}
