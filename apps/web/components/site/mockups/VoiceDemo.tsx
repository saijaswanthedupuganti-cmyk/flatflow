'use client'
import { motion } from 'framer-motion'
import { ReceiptText, X, Check } from 'lucide-react'
import VoiceOrb from './VoiceOrb'
import { useLoopStep } from '../useLoopStep'
import { EASE } from '../motion'
import h from '../home/home.module.css'
import m from './mockups.module.css'

export const PHRASE = 'I just spent 500 on groceries'
const TICK = 70
const TYPED = PHRASE.length          // steps 0..TYPED type the phrase
const PRESS = TYPED + 30             // card holds ~2.1 s, then Save is pressed
const SAVED = PRESS + 2              // ~140 ms press, then the saved state
const TOTAL = SAVED + 40             // saved holds ~2.8 s; the last step is also the reduced-motion state

/** One loop of the real voice flow: transcript types in → confirm card → Save pressed → saved. */
export function useVoiceLoop() {
  const { ref, step } = useLoopStep(TOTAL, TICK)
  return {
    ref,
    typed: PHRASE.slice(0, Math.min(step, TYPED)),
    listening: step < TYPED,
    showCard: step >= TYPED,
    pressing: step >= PRESS && step < SAVED,
    saved: step >= SAVED,
  }
}

/** The confirm card the app shows before anything is saved. */
export function VoiceCard({ show, pressing, saved }: { show: boolean; pressing: boolean; saved: boolean }) {
  return (
    <motion.div style={{ width: '100%', display: 'grid', justifyItems: 'center', gap: '1em' }} initial={false}
      animate={show ? { opacity: 1, y: 0 } : { opacity: 0, y: 12 }}
      transition={{ duration: show ? 0.3 : 0.2, ease: EASE }}>
      <div className={m.vCard}>
        <div className={m.vHead}>
          <span className={m.vIcon}><ReceiptText size="1.3em" aria-hidden /></span>
          <div><b>Groceries</b><span>Paid by You</span></div>
        </div>
        <div className={m.vAmt}>₹500</div>
        <div className={m.vSplit}>
          <span className={m.vAvs}><span>Y</span><span>R</span><span>P</span><span>A</span></span>
          Split with everyone (4)
        </div>
      </div>
      <div className={m.vCaption}>
        <span style={{ opacity: saved ? 0 : 1 }}>Tap Save to keep it.</span>
        <span style={{ opacity: saved ? 1 : 0 }}>Saved ₹500 for Groceries.</span>
      </div>
      <div className={m.vBtns}>
        <span className={`${m.vBtn} ${m.vCancel}`}><X size="1.1em" aria-hidden />Cancel</span>
        <motion.span className={`${m.vBtn} ${m.vSave}`} animate={{ scale: pressing ? 0.97 : 1 }} transition={{ duration: 0.12, ease: EASE }}>
          <span className={m.vSaveIdle} style={{ opacity: saved ? 0 : 1 }}><Check size="1.1em" aria-hidden />Save</span>
          <span className={m.vSaveDone} style={{ opacity: saved ? 1 : 0 }}><Check size="1.1em" aria-hidden />Saved</span>
        </motion.span>
      </div>
    </motion.div>
  )
}

/** Home › Voice section: orb with wave ribbons, the spoken phrase, then the confirm card. */
export default function VoiceDemo() {
  const { ref, ...v } = useVoiceLoop()
  return (
    <div ref={ref} className={h.voiceStage} role="img"
      aria-label={`Voice demo: you say "${PHRASE}", Habitiq shows a Groceries expense of ₹500 paid by you, split with everyone (4), and saves it when you tap Save.`}>
      <div className={h.voiceGlow} aria-hidden />
      <div className={h.voiceOrbRow} aria-hidden><VoiceOrb size={120} listening={v.listening} ribbons /></div>
      <p className={h.transcript} aria-hidden>
        {v.typed ? <>&ldquo;{v.typed}{v.listening ? <span className={h.caret} /> : '”'}</> : <span className={h.caret} />}
      </p>
      <div className={h.voiceCardWrap} aria-hidden><VoiceCard show={v.showCard} pressing={v.pressing} saved={v.saved} /></div>
    </div>
  )
}
