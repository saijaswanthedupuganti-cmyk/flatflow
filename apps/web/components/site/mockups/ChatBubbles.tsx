'use client'
import { motion } from 'framer-motion'
import { CheckCircle2, IndianRupee, Receipt, ArrowRight } from 'lucide-react'
import { useCalm } from '../useCalm'
import { EASE } from '../motion'
import Glass from '../Glass'
import h from '../home/home.module.css'

const BUBBLES = [
  { text: 'Who bought milk?', who: 'Rahul', mine: false },
  { text: 'Whose turn for trash today??', who: '', mine: true },
  { text: 'Wifi bill ₹1,200, pay me pls', who: 'Meera', mine: false },
  { text: 'I cleaned the kitchen last time', who: '', mine: true },
  { text: 'Who owes who now?', who: 'Anita', mine: false },
]

function ResolvedCard() {
  return (
    <Glass tier="canvas" style={{ padding: 24 }}>
      <p style={{ fontWeight: 800, fontSize: 18, margin: 0 }}>One place for the whole flat</p>
      <div className={h.resolvedRow}><CheckCircle2 color="var(--or-success)" aria-hidden />Trash today: <b>Rahul</b></div>
      <div className={h.resolvedRow}><IndianRupee color="var(--or-navy)" aria-hidden />Milk ₹60 · split 4 ways</div>
      <div className={h.resolvedRow} style={{ borderBottom: 0 }}><Receipt color="var(--or-navy)" aria-hidden />Wifi ₹1,200 · ₹300 each</div>
    </Glass>
  )
}

/** The flat chat on one side, the same jobs sorted in Habitiq on the other. Readable at rest; messages arrive one by one. */
export default function ChatBubbles() {
  const reduce = useCalm()
  const settleAt = BUBBLES.length * 0.12 + 0.15
  return (
    <div className={h.problemStage}>
      <div className={h.chatPanel}>
        <div className={h.chatHead}><span className={h.chatAvatar} aria-hidden>4B</span><div><b>Flat 4B</b><small>4 members</small></div></div>
        {BUBBLES.map((b, i) => (
          <motion.div key={b.text} data-bubble="" className={`${h.bubble} ${b.mine ? h.bubbleMine : ''}`}
            initial={reduce ? false : { opacity: 0, y: 8 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true, amount: 0.6 }}
            transition={{ duration: 0.25, ease: EASE, delay: i * 0.12 }}>
            {b.who && <small className={h.bubbleWho}>{b.who}</small>}
            {b.text}
          </motion.div>
        ))}
      </div>
      <span className={h.problemArrow} aria-hidden><ArrowRight size={20} /></span>
      <motion.div className={h.resolved} initial={reduce ? false : { opacity: 0, y: 12 }} whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, amount: 0.4 }} transition={{ duration: 0.4, ease: EASE, delay: settleAt }}>
        <ResolvedCard />
      </motion.div>
    </div>
  )
}
