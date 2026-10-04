'use client'
import { motion } from 'framer-motion'
import { useCalm } from '../useCalm'
import m from './mockups.module.css'

const HEIGHTS = [0.45, 0.8, 1, 0.7, 0.5]

export default function VoiceOrb({ size = 120, listening = true }: { size?: number; listening?: boolean }) {
  const reduce = useCalm()
  const live = listening && !reduce
  return (
    <motion.div className={m.orb} style={{ width: size, height: size }}
      animate={live ? { scale: [1, 1.04, 1] } : undefined}
      transition={live ? { duration: 2.4, repeat: Infinity, ease: 'easeInOut' } : undefined}>
      <span className={m.orbRing} />
      <div className={m.bars} style={{ width: '42%' }}>
        {HEIGHTS.map((h, i) => (
          <motion.span key={i} className={m.bar} style={{ scaleY: h }}
            animate={live ? { scaleY: [h, Math.max(0.25, 1.2 - h), h] } : undefined}
            transition={live ? { duration: 0.9 + i * 0.12, repeat: Infinity, ease: 'easeInOut' } : undefined} />
        ))}
      </div>
    </motion.div>
  )
}
