'use client'
import { KeyRound, Check } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import m from './mockups.module.css'

const CODE = 'FLAT-7KQ2'

/**
 * Join a flat: the invite code is typed, the flat is found, and because this flat needs approval the
 * request goes to the admin. Every state builds on the last, so the final (reduced-motion) frame shows it all.
 */
export default function JoinScreen() {
  // 0 empty · 1-9 typing · 10 flat found · 11 request sent (final)
  const { ref, step } = useLoopStep(CODE.length + 4, 420)
  const typed = CODE.slice(0, Math.min(step, CODE.length))
  const found = step >= CODE.length + 1
  const sent = step >= CODE.length + 2
  return (
    <div ref={ref} className={m.page}>
      <div className={m.label} style={{ color: '#0F766E' }}>Personalised setup</div>
      <div className={m.h1} style={{ marginTop: '2%' }}>Join a flat</div>
      <div className={m.sub}>Enter the invite shared by your flat admin</div>

      <div className={m.field}>
        <span className={m.fieldLabel}>Invite code</span>
        <span className={`${m.fieldBox} ${m.outline}`}>
          <KeyRound size="1.1em" aria-hidden style={{ color: '#50615E', flexShrink: 0 }} />
          {typed ? <b className={m.code}>{typed}</b> : <span className={m.muted}>FLAT-A3B9</span>}
        </span>
      </div>

      <div className={`${m.found} ${m.outline}`} style={{ opacity: found ? 1 : 0, transform: found ? 'none' : 'translateY(6px)' }}>
        <div className={m.label}>Flat found</div>
        <div className={m.foundTop}>
          <div><div className={m.flatTitle}>Sunrise Heights 4B</div><div className={m.muted}>4 members</div></div>
          <span className={m.pill}>Active</span>
        </div>
        <div className={m.muted}>Admin approval is required. You&apos;ll join after they accept.</div>
      </div>

      <div className={m.joinBtn} style={{ opacity: found ? 1 : 0.35 }}>
        <span className={sent ? m.off : ''}>Request to join</span>
        <span className={sent ? '' : m.off}><Check size="1em" strokeWidth={3} aria-hidden />Request sent</span>
      </div>
      <div className={m.notice} style={{ opacity: sent ? 1 : 0, transition: 'opacity 300ms var(--or-ease)' }}>
        Join request sent. The flat admin will approve you.
      </div>
    </div>
  )
}
