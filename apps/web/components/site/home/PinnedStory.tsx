import { KeyRound, Repeat, ArrowLeftRight, ReceiptText, HandCoins, CalendarCheck } from 'lucide-react'
import Section, { Reveal } from '../Section'
import PhoneFrame from '../mockups/PhoneFrame'
import JoinScreen from '../mockups/JoinScreen'
import TasksScreen from '../mockups/TasksScreen'
import AwayScreen from '../mockups/AwayScreen'
import AddExpenseScreen from '../mockups/AddExpenseScreen'
import ExpenseScreen from '../mockups/ExpenseScreen'
import BillsScreen from '../mockups/BillsScreen'
import h from './home.module.css'

/** The flat-management loop in the order a flat lives it. Every claim maps to a screen in the Android app. */
const STEPS = [
  {
    title: 'Start a flat or join one', icon: KeyRound, Screen: JoinScreen,
    body: 'Create your flat and share its invite code. If your flat asks for approval, the admin lets each new member in.',
    details: ['Invite code', 'Admin approval'],
    label: 'Join a flat: an invite code is entered, the flat is found and a join request goes to the admin',
  },
  {
    title: 'Chores rotate on their own', icon: Repeat, Screen: TasksScreen,
    body: 'Set a task once and choose how often it repeats. When it is done, the turn passes to the next flatmate.',
    details: ['Daily', 'Weekly', 'Fortnightly', 'Monthly'],
    label: 'Tasks: the trash turn is ticked off and passes to the next flatmate',
  },
  {
    title: 'Swap a turn, or go away', icon: ArrowLeftRight, Screen: AwayScreen,
    body: 'Ask a flatmate to cover a task. Heading home? Mark yourself away and the rotation skips you until you are back.',
    details: ['Swap requests', 'Away mode'],
    label: 'Going away: switch on away and your turns pass to the rest of the flat',
  },
  {
    title: 'Add an expense, split it your way', icon: ReceiptText, Screen: AddExpenseScreen,
    body: 'Log what you paid, then split it with everyone or only the people involved, equally or by custom amounts.',
    details: ['Equal split', 'Custom split'],
    label: 'Add expense: 1,200 rupees of groceries split equally, one flatmate unticked so each share becomes 400',
  },
  {
    title: 'See who owes whom, then settle up', icon: HandCoins, Screen: ExpenseScreen,
    body: 'Balances break down person by person. Record a settlement when you pay someone back. Habitiq keeps the record and never moves money.',
    details: ['Record a settlement', 'Mark received'],
    label: 'Expenses: your balance and what each flatmate owes or is owed',
  },
  {
    title: 'Close the month on bills', icon: CalendarCheck, Screen: BillsScreen,
    body: 'Rent, electricity and wifi repeat every month and split into shares. Mark each one paid, then close the month.',
    details: ['Monthly bills', 'Close month'],
    label: 'Monthly bills: rent, electricity and wifi being marked paid',
  },
] as const

export default function PinnedStory() {
  return (
    <Section id="how-it-works" tone="canvas" className={h.storySection} eyebrow="How it works"
      title="From move-in to month-end, one fair loop."
      lede="Six everyday jobs in the order your flat meets them. Each one is a real screen in the app, running below.">
      <ol className={h.flow} aria-label="How Habitiq runs a flat">
        {STEPS.map((s, i) => {
          const Icon = s.icon
          return (
            <li key={s.title} className={h.flowItem} data-story-step={i} style={{ '--i': i } as React.CSSProperties}>
              <Reveal className={h.flowCard} delay={(i % 2) * 0.08}>
                <div className={h.flowText}>
                  <span className={h.flowNum} aria-hidden>{i + 1}</span>
                  <span className={h.flowIcon}><Icon size={20} strokeWidth={1.75} aria-hidden /></span>
                  <h3>{s.title}</h3>
                  <p>{s.body}</p>
                  <ul className={h.flowTags} aria-label="In the app">
                    {s.details.map(d => <li key={d}>{d}</li>)}
                  </ul>
                </div>
                <div className={h.flowPhone}><PhoneFrame label={s.label}><s.Screen /></PhoneFrame></div>
              </Reveal>
            </li>
          )
        })}
      </ol>
    </Section>
  )
}
