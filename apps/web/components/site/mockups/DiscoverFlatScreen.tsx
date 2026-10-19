'use client'
import { motion } from 'framer-motion'
import { Info, Search, SlidersHorizontal, Check, ShieldCheck } from 'lucide-react'
import { useLoopStep } from '../useLoopStep'
import { EASE } from '../motion'
import { HouseIllustration } from './AppParts'
import m from './mockups.module.css'

const LISTINGS = [
  { title: 'Sunrise Heights 4B', area: 'Gachibowli, Hyderabad', rent: '₹8,500', tags: ['Quiet evenings', 'Vegetarian', 'No smoking'], members: 2 },
  { title: 'Lakeview Residency 2A', area: 'Madhapur, Hyderabad', rent: '₹9,000', tags: ['Early bird', 'Near metro'], members: 3 },
]

/**
 * Discover › Find a flat. Loop: the Hyderabad filter switches on, matching listings stagger in, and
 * Connect turns into "Request sent". The last step (also the reduced-motion state) shows the sent request.
 */
export default function DiscoverFlatScreen() {
  // 0 nothing filtered · 1 chip on + cards in · 2 hold · 3 request sent · 4 hold
  const { ref, step } = useLoopStep(5, 1100)
  const on = step >= 1
  const sent = step >= 3
  return (
    <div ref={ref} className={m.page} style={{ paddingTop: '14%' }}>
      <div className={m.seg}><span className={m.segOn}>Find a flat</span><span>Find a person</span></div>
      <div className={m.safety}><Info size="1.4em" aria-hidden /><span>Never pay rent before viewing in person.</span><span>Safety tips</span></div>
      <div className={m.searchRow}>
        <span className={`${m.search} ${m.outline}`}><Search size="1.1em" aria-hidden />Hyderabad</span>
        <span className={m.filters}><SlidersHorizontal size="1.1em" aria-hidden />Filters<span className={`${m.badge} ${on ? '' : m.badgeOff}`}>2</span></span>
      </div>
      <div className={m.chips}>
        <span className={`${m.chip} ${on ? m.chipOn : ''}`}><Check strokeWidth={3} aria-hidden /><span>Hyderabad</span></span>
        <span className={m.chip}><span>Bangalore</span></span>
        <span className={m.chip}><span>Chennai</span></span>
        <span className={m.chip}><span>Delhi</span></span>
      </div>
      <div className={m.resultsHead}><b>Places for you</b><span className={m.link}>My posts</span></div>
      <div className={`${m.ofCount} ${m.muted} ${on ? '' : m.off}`}>2 of 2 flats</div>
      {LISTINGS.map((l, i) => (
        <motion.div key={l.title} className={`${m.listing} ${m.outline}`} initial={false}
          animate={on ? { opacity: 1, y: 0 } : { opacity: 0, y: 8 }}
          transition={{ duration: 0.22, ease: EASE, delay: on && step === 1 ? 0.16 + i * 0.06 : 0 }}>
          <div className={m.illus}><HouseIllustration /></div>
          <div className={m.lBody}>
            <div className={m.lTop}>
              <div><div className={m.lTitle}>{l.title}</div><div className={m.muted}>{l.area}</div></div>
              <div className={m.price}>{l.rent}<small>per head</small></div>
            </div>
            <div className={m.muted}>Private room · Apartment · Any gender</div>
            <div className={m.tags}>{l.tags.map(t => <span key={t}>{t}</span>)}</div>
            <div className={m.host}>
              <span>Hosted by an Oddroof member · {l.members} in flat</span>
              <b><ShieldCheck size="1.1em" aria-hidden />Oddroof member</b>
            </div>
            {i === 0 && (
              <div className={m.connect}>
                <span className={`${m.connectBtn} ${sent ? m.off : ''}`}>Connect with this flat</span>
                <span className={`${m.connectDone} ${sent ? '' : m.off}`}><Check size="1em" strokeWidth={3} aria-hidden />Request sent</span>
              </div>
            )}
          </div>
        </motion.div>
      ))}
    </div>
  )
}
