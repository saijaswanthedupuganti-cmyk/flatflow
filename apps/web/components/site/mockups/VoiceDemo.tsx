'use client'
import { AnimatePresence, motion } from 'framer-motion'
import Glass from '../Glass'
import VoiceOrb from './VoiceOrb'
import { useLoopStep } from '../useLoopStep'
import { EASE } from '../motion'
import h from '../home/home.module.css'

const PHRASE = 'paid Ravi 300 for the cylinder'
const TYPE_STEPS = PHRASE.length
// Steps 0..TYPE_STEPS type the phrase, then the card holds ~3 s (45 × 70 ms) so it can be read; the last step
// (reduced-motion final state) shows both.
const TOTAL = TYPE_STEPS + 45

export default function VoiceDemo() {
  const { ref, step } = useLoopStep(TOTAL, 70)
  const typed = PHRASE.slice(0, Math.min(step, TYPE_STEPS))
  const showCard = step >= TYPE_STEPS
  return (
    <div ref={ref} className={h.voiceStage}>
      <div className={h.voiceGlow} />
      <VoiceOrb size={120} listening={!showCard} />
      <p className={h.transcript} aria-live="off">
        {typed}{!showCard && <span className={h.caret} aria-hidden />}
      </p>
      <AnimatePresence>
        {showCard && (
          <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0 }}
            transition={{ duration: 0.4, ease: EASE }} style={{ width: '100%', display: 'grid', justifyItems: 'center' }}>
            <Glass tier="photo" className={h.confirm}>
              <b>Record ₹300 you paid Ravi</b>
              <span style={{ opacity: .8, fontSize: 14 }}>Say “yes” or tap Save</span>
              <div className={h.confirmBtns}>
                <span style={{ background: 'rgba(255,255,255,.15)' }}>Cancel</span>
                <span style={{ background: 'var(--or-blue)' }}>Save</span>
              </div>
            </Glass>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}
