'use client'
import { useRef } from 'react'
import { motion, useScroll, useTransform, useReducedMotion, type MotionValue } from 'framer-motion'
import { CheckCircle2, IndianRupee, Receipt } from 'lucide-react'
import Glass from '../Glass'
import h from '../home/home.module.css'

const BUBBLES = [
  { text: 'Who bought milk?', x: '-38%', y: '-150%', mine: false },
  { text: 'Whose turn for trash today??', x: '30%', y: '-170%', mine: true },
  { text: 'Wifi bill ₹1,200, pay me pls', x: '-44%', y: '60%', mine: false },
  { text: 'I cleaned the kitchen last time', x: '36%', y: '90%', mine: true },
  { text: 'Who owes who now?', x: '-6%', y: '-230%', mine: false },
]

function Bubble({ b, progress, i }: { b: typeof BUBBLES[number]; progress: MotionValue<number>; i: number }) {
  const reduce = useReducedMotion()
  const opacity = useTransform(progress, [0, 0.45 + i * 0.03, 0.7], [1, 1, reduce ? 1 : 0])
  const scale = useTransform(progress, [0.4, 0.7], [1, reduce ? 1 : 0.6])
  return (
    <motion.div className={`${h.bubble} ${b.mine ? h.bubbleMine : ''}`}
      style={{ left: '50%', top: '50%', translateX: b.x, translateY: b.y, opacity, scale }}>
      {b.text}
    </motion.div>
  )
}

export default function ChatBubbles() {
  const ref = useRef<HTMLDivElement>(null)
  const reduce = useReducedMotion()
  const { scrollYProgress } = useScroll({ target: ref, offset: ['start end', 'end start'] })
  const cardOpacity = useTransform(scrollYProgress, [0.45, 0.65], [reduce ? 1 : 0, 1])
  const cardY = useTransform(scrollYProgress, [0.45, 0.65], [reduce ? 0 : 30, 0])
  return (
    <div ref={ref} className={h.problemStage}>
      {BUBBLES.map((b, i) => <Bubble key={b.text} b={b} progress={scrollYProgress} i={i} />)}
      <motion.div style={{ opacity: cardOpacity, y: cardY }} className={h.resolved}>
        <Glass tier="canvas" style={{ padding: 24 }}>
          <p style={{ fontWeight: 800, fontSize: 18, margin: 0 }}>One place for the whole flat</p>
          <div className={h.resolvedRow}><CheckCircle2 color="var(--or-teal)" aria-hidden />Trash today: <b>Ravi</b></div>
          <div className={h.resolvedRow}><IndianRupee color="var(--or-blue)" aria-hidden />Milk ₹60 · split 4 ways</div>
          <div className={h.resolvedRow} style={{ borderBottom: 0 }}><Receipt color="var(--or-amber)" aria-hidden />Wifi ₹1,200 · ₹300 each</div>
        </Glass>
      </motion.div>
    </div>
  )
}
