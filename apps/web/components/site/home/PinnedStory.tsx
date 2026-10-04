'use client'
import { useEffect, useRef, useState } from 'react'
import { AnimatePresence, motion, useInView } from 'framer-motion'
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
  { title: 'Tasks rotate fairly', body: 'Set a chore once. Oddroof hands it to the next person every time, and skips whoever is away.', icon: RefreshCw, color: 'var(--or-teal)', Screen: TasksScreen, label: 'Trash rotation moving between flatmates' },
  { title: 'Expenses split themselves', body: 'Add what you paid. Everyone sees their share and who owes whom, without a spreadsheet.', icon: IndianRupee, color: 'var(--or-blue)', Screen: ExpenseScreen, label: 'A ₹480 grocery expense split four ways' },
  { title: 'Bills and month close', body: 'Rent, electricity and wifi in one list. Mark them paid and close the month with everyone settled.', icon: Receipt, color: 'var(--or-amber)', Screen: BillsScreen, label: 'Monthly bills being marked paid' },
  { title: 'Away mode', body: 'Going home for a festival? Switch on away and your turns pass on until you are back.', icon: Plane, color: 'var(--or-violet)', Screen: AwayScreen, label: 'Away mode switched on, turns passed to flatmates' },
] as const

function Step({ i, onActive, children }: { i: number; onActive: (i: number) => void; children: React.ReactNode }) {
  const ref = useRef<HTMLElement>(null)
  const inView = useInView(ref, { amount: 0.6 })
  useEffect(() => { if (inView) onActive(i) }, [inView, i, onActive])
  return <article ref={ref} data-story-step={i}>{children}</article>
}

export default function PinnedStory() {
  const [active, setActive] = useState(0)
  const Current = STEPS[active].Screen
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
                <Glass tier="canvas" className={h.step}>
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
            <PhoneFrame label={STEPS[active].label}>
              <AnimatePresence mode="wait">
                <motion.div key={active} style={{ height: '100%' }}
                  initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -16 }}
                  transition={{ duration: 0.35, ease: EASE }}>
                  <Current />
                </motion.div>
              </AnimatePresence>
            </PhoneFrame>
          </div>
        </div>
      </div>
    </Section>
  )
}
