'use client'
import { motion } from 'framer-motion'
import { useCalm } from '../useCalm'
import m from './mockups.module.css'

const HEIGHTS = [0.45, 0.8, 1, 0.7, 0.5]

// Interlaced sine strands that taper at both ends, like the app's listening ribbons.
const W = 100, H = 40
const STRANDS = Array.from({ length: 6 }, (_, k) => {
  const amp = 7 + (k % 3) * 3.5
  const phase = k * 1.05
  let d = ''
  for (let x = 0; x <= W; x += 2) {
    const env = Math.pow(Math.sin((Math.PI * x) / W), 1.3) // tapers to a point at both ends
    const y = H / 2 + amp * env * Math.sin(x * 0.19 + phase)
    d += `${x === 0 ? 'M' : 'L'}${x} ${y.toFixed(2)}`
  }
  return d
})

function Ribbon({ live, flip }: { live: boolean; flip?: boolean }) {
  return (
    <svg className={m.ribbon} viewBox={`0 0 ${W} ${H}`} preserveAspectRatio="none" aria-hidden
      style={flip ? { transform: 'scaleX(-1)' } : undefined}>
      <defs>
        <linearGradient id="or-ribbon" x1="0" x2="1">
          <stop offset="0" style={{ stopColor: 'var(--or-action-bright)', stopOpacity: 0.2 }} />
          <stop offset="1" style={{ stopColor: 'var(--or-action-bright)', stopOpacity: 0.95 }} />
        </linearGradient>
      </defs>
      {STRANDS.map((d, i) => (
        <motion.path key={i} d={d} fill="none" stroke="url(#or-ribbon)" strokeWidth={0.6} vectorEffect="non-scaling-stroke"
          initial={false}
          animate={live ? { scaleY: [1, 0.45 + (i % 3) * 0.15, 1], opacity: 1 } : { scaleY: 0.75, opacity: 0.7 }}
          transition={live ? { duration: 1.1 + i * 0.17, repeat: Infinity, ease: 'easeInOut' } : { duration: 0.3, ease: [0.23, 1, 0.32, 1] }} />
      ))}
    </svg>
  )
}

/** The voice orb: teal (press-me) sphere, two rings, bars that move while listening; optional wave ribbons either side. */
export default function VoiceOrb({ size = 120, listening = true, ribbons = false }: { size?: number | string; listening?: boolean; ribbons?: boolean }) {
  const reduce = useCalm()
  const live = listening && !reduce
  const orb = (
    <motion.div className={m.orb} style={{ width: size, height: size }}
      animate={live ? { scale: [1, 1.04, 1] } : { scale: 1 }}
      transition={live ? { duration: 2.4, repeat: Infinity, ease: 'easeInOut' } : { duration: 0.3 }}>
      <span className={m.orbRing2} />
      <span className={m.orbRing} />
      <div className={m.bars} style={{ width: '42%' }}>
        {HEIGHTS.map((h, i) => (
          <motion.span key={i} className={m.bar} style={{ scaleY: h }}
            animate={live ? { scaleY: [h, Math.max(0.25, 1.2 - h), h] } : { scaleY: h }}
            transition={live ? { duration: 0.9 + i * 0.12, repeat: Infinity, ease: 'easeInOut' } : { duration: 0.3 }} />
        ))}
      </div>
    </motion.div>
  )
  if (!ribbons) return orb
  return (
    <div className={m.orbRow}>
      <Ribbon live={live} />
      {orb}
      <Ribbon live={live} flip />
    </div>
  )
}
