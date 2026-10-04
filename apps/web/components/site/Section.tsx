'use client'
import { motion, useReducedMotion } from 'framer-motion'
import { fadeUp } from './motion'
import s from './site.module.css'

const tones = { canvas: s.toneCanvas, white: s.toneWhite, dark: s.toneDark }

export function Reveal({ children, delay = 0, className }: { children: React.ReactNode; delay?: number; className?: string }) {
  const reduce = useReducedMotion()
  // framer-motion's reducedMotion only skips transforms; opacity would still start at 0. Render the final state instead.
  if (reduce) return <div className={className}>{children}</div>
  return (
    <motion.div className={className} variants={fadeUp} initial="hidden" whileInView="show"
      viewport={{ once: true, amount: 0.25 }} transition={{ delay }}>
      {children}
    </motion.div>
  )
}

export default function Section({ id, tone = 'canvas', eyebrow, title, lede, children, className = '' }:
  { id?: string; tone?: keyof typeof tones; eyebrow?: string; title?: string; lede?: string; children?: React.ReactNode; className?: string }) {
  return (
    <section id={id} className={`${s.section} ${tones[tone]} ${className}`}>
      <div className={s.inner}>
        {(eyebrow || title || lede) && (
          <Reveal>
            {eyebrow && <p className={s.eyebrow}>{eyebrow}</p>}
            {title && <h2 className={s.title}>{title}</h2>}
            {lede && <p className={s.lede}>{lede}</p>}
          </Reveal>
        )}
        {children}
      </div>
    </section>
  )
}
