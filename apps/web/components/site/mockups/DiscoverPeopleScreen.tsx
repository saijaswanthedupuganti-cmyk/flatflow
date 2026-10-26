'use client'
import { motion } from 'framer-motion'
import { Search, SlidersHorizontal, Check, ChevronRight, Info } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import { EASE } from '../motion'
import m from './mockups.module.css'

const PEOPLE = [
  { initials: 'MN', name: 'Meera Nair', where: 'Looking in Madhapur, Hyderabad · Budget ₹9,000', bio: 'Product designer, early riser, cooks on weekends. Looking for a calm, tidy flat.' },
  { initials: 'R', name: 'Rahul', where: 'Looking in Hyderabad', bio: '' },
]

/**
 * Discover › Find a person. Loop mirrors the flat side: a filter switches on, seekers stagger in, and
 * Connect turns into "Request sent". The last step is also the reduced-motion state.
 */
export default function DiscoverPeopleScreen() {
  const { ref, step } = useLoopStep(5, 1100)
  const on = step >= 1
  const sent = step >= 3
  return (
    <div ref={ref} className={m.page} style={{ paddingTop: '14%' }}>
      <div className={m.seg}><span>Find a flat</span><span className={m.segOn}>Find a person</span></div>
      <div className={m.searchRow}>
        <span className={`${m.search} ${m.outline}`}><Search size="1.1em" aria-hidden />Search people or area</span>
        <span className={m.filters}><SlidersHorizontal size="1.1em" aria-hidden />Filters<span className={`${m.badge} ${on ? '' : m.badgeOff}`}>1</span></span>
      </div>
      <div className={m.chips}>
        <span className={m.chip}><span>Male</span></span>
        <span className={`${m.chip} ${on ? m.chipOn : ''}`}><Check strokeWidth={3} aria-hidden /><span>Female</span></span>
        <span className={m.chip}><span>Work from home</span></span>
        <span className={m.chip}><span>Vegetarian</span></span>
      </div>
      <div className={m.resultsHead}><b>People looking nearby</b><span className={m.link}>My posts</span></div>
      {PEOPLE.map((p, i) => (
        <motion.div key={p.name} className={`${m.pCard} ${m.outline}`} initial={false}
          animate={on ? { opacity: 1, y: 0 } : { opacity: 0, y: 8 }}
          transition={{ duration: 0.22, ease: EASE, delay: on && step === 1 ? 0.16 + i * 0.06 : 0 }}>
          <div className={m.pHead}>
            <span className={m.av}>{p.initials}</span>
            <div style={{ flex: 1 }}><div className={m.pName}>{p.name}</div><div className={m.muted}>{p.where}</div></div>
            <ChevronRight size="1em" aria-hidden style={{ flexShrink: 0 }} />
          </div>
          <div className={m.newTag}><Info size="1.1em" aria-hidden />New to Habitiq <span className={m.link}>Why?</span></div>
          {p.bio && <div className={m.bio}>{p.bio}</div>}
          <div className={m.connect}>
            <span className={i === 0 && sent ? m.off : ''}>Connect</span>
            {i === 0 && <span className={sent ? '' : m.off}><Check size="1em" strokeWidth={3} aria-hidden />Request sent</span>}
          </div>
        </motion.div>
      ))}
    </div>
  )
}
