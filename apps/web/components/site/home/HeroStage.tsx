'use client'
import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import { Home, Users, CheckCircle2, IndianRupee } from 'lucide-react'
import PhoneFrame from '../mockups/PhoneFrame'
import HomeScreen from '../mockups/HomeScreen'
import DiscoverFlatScreen from '../mockups/DiscoverFlatScreen'
import ManageScreen from '../mockups/ManageScreen'
import { useCalm } from '../useCalm'
import { EASE } from '../motion'
import h from './home.module.css'

/** How far Discover and Manage step out from behind Home, by screen width. */
function useSideOffset() {
  const [side, setSide] = useState(150)
  useEffect(() => {
    const pick = () => setSide(window.innerWidth < 640 ? 62 : window.innerWidth < 1024 ? 104 : 150)
    pick()
    window.addEventListener('resize', pick)
    return () => window.removeEventListener('resize', pick)
  }, [])
  return side
}

// One continuous composition: reveal once, then the whole group breathes on a slow loop.
const REVEAL = { duration: 1.6, ease: EASE }
const FLOAT = { duration: 6, ease: 'easeInOut' as const, repeat: Infinity, delay: 4 }

const CHIPS = [
  { icon: Home, cls: h.chipA, drift: { x: [0, 6, 0], y: [0, -8, 0] }, dur: 9 },
  { icon: Users, cls: h.chipB, drift: { x: [0, -7, 0], y: [0, 6, 0] }, dur: 11 },
  { icon: CheckCircle2, cls: h.chipC, drift: { x: [0, 5, 0], y: [0, 10, 0] }, dur: 10 },
  { icon: IndianRupee, cls: h.chipD, drift: { x: [0, -6, 0], y: [0, -6, 0] }, dur: 12 },
]

export default function HeroStage() {
  const calm = useCalm()
  const side = useSideOffset()
  const out = (dir: -1 | 1) => ({ x: dir * side, y: dir === -1 ? -15 : 10, rotate: dir * 5, opacity: 1, scale: 1 })
  const tucked = { x: 0, y: 0, rotate: 0, opacity: 0, scale: 0.94 }

  return (
    <div className={h.stage} aria-label="Oddroof app: Home in front, Discover and Manage behind" role="group">
      <div className={h.stageGlow} aria-hidden />

      {/* Discover, behind Home on the left */}
      <motion.div className={`${h.slot} ${h.slotSide}`} data-hero-phone="discover" style={{ zIndex: 2 }}
        initial={calm ? false : tucked} animate={out(-1)} transition={{ ...REVEAL, delay: calm ? 0 : 1.0 }}>
        <motion.div animate={calm ? undefined : { y: [0, -10, 0], rotate: [0, -0.5, 0] }} transition={FLOAT}>
          <PhoneFrame label="Discover: a room listing with rent, beds and a connect button"><DiscoverFlatScreen /></PhoneFrame>
        </motion.div>
        <motion.span className={h.contact} aria-hidden animate={calm ? undefined : { scaleX: [1, 0.92, 1], opacity: [0.3, 0.22, 0.3] }} transition={FLOAT} />
      </motion.div>

      {/* Manage, behind Home on the right */}
      <motion.div className={`${h.slot} ${h.slotSide}`} data-hero-phone="manage" style={{ zIndex: 1 }}
        initial={calm ? false : tucked} animate={out(1)} transition={{ ...REVEAL, delay: calm ? 0 : 1.5 }}>
        <motion.div animate={calm ? undefined : { y: [0, -5, 0], rotate: [0, 0.5, 0] }} transition={FLOAT}>
          <PhoneFrame label="Manage: tasks, expenses, bills and members for the flat"><ManageScreen /></PhoneFrame>
        </motion.div>
        <motion.span className={h.contact} aria-hidden animate={calm ? undefined : { scaleX: [1, 0.95, 1], opacity: [0.3, 0.25, 0.3] }} transition={FLOAT} />
      </motion.div>

      {/* Home, the hero phone */}
      <div className={`${h.slot} ${h.slotHome}`} data-hero-phone="home" style={{ zIndex: 3 }}>
        <motion.div animate={calm ? undefined : { y: [0, -6, 0] }} transition={FLOAT}>
          <PhoneFrame label="Oddroof home screen with today's tasks being completed"><HomeScreen /></PhoneFrame>
        </motion.div>
        <motion.span className={`${h.contact} ${h.contactHome}`} aria-hidden animate={calm ? undefined : { scaleX: [1, 0.94, 1], opacity: [0.42, 0.32, 0.42] }} transition={FLOAT} />
      </div>

      {/* Translucent curved glass the phones stand on */}
      <div className={h.glassFloor} aria-hidden />

      {CHIPS.map(({ icon: Icon, cls, drift, dur }, i) => (
        <motion.span key={i} className={`${h.chip} ${cls}`} aria-hidden
          initial={calm ? false : { opacity: 0 }} animate={calm ? { opacity: 1 } : { opacity: 1, ...drift }}
          transition={calm ? undefined : { opacity: { duration: 1, delay: 2.6 + i * 0.15 }, x: { duration: dur, repeat: Infinity, ease: 'easeInOut' }, y: { duration: dur, repeat: Infinity, ease: 'easeInOut' } }}>
          <Icon size={18} aria-hidden />
        </motion.span>
      ))}
    </div>
  )
}
