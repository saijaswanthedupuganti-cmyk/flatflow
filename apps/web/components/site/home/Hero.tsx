'use client'
import Image from 'next/image'
import { useRef } from 'react'
import { motion, useScroll, useTransform, useReducedMotion } from 'framer-motion'
import { CheckCircle2, IndianRupee } from 'lucide-react'
import Glass from '../Glass'
import SiteButton from '../SiteButton'
import PhoneFrame from '../mockups/PhoneFrame'
import HomeScreen from '../mockups/HomeScreen'
import VoiceOrb from '../mockups/VoiceOrb'
import { EASE, fadeUp, staggerKids } from '../motion'
import { HERO_PHOTO } from '@/lib/site/photos'
import h from './home.module.css'

export default function Hero({ download, qr, hasPhoto }: { download: React.ReactNode; qr: React.ReactNode; hasPhoto: boolean }) {
  const ref = useRef<HTMLElement>(null)
  const reduce = useReducedMotion()
  const { scrollYProgress } = useScroll({ target: ref, offset: ['start start', 'end start'] })
  const tilt = useTransform(scrollYProgress, [0, 1], [0, reduce ? 0 : -8])
  const lift = useTransform(scrollYProgress, [0, 1], [0, reduce ? 0 : -60])

  return (
    <section id="hero" ref={ref} className={h.hero}>
      <div className={`${h.heroBg} or-golden`}>
        {hasPhoto && <Image src={HERO_PHOTO} alt="" fill priority sizes="100vw" />}
      </div>
      <div className={h.heroScrim} />
      <div className={h.heroInner}>
        <motion.div className={h.heroCopy} variants={staggerKids(0.08)} initial="hidden" animate="show">
          <motion.h1 variants={fadeUp}>Your flat, sorted.</motion.h1>
          <motion.p variants={fadeUp} className={h.sub}>
            Fair turns for chores, clear money between flatmates, and a voice assistant that just gets it done.
          </motion.p>
          <motion.div variants={fadeUp} className={h.ctaRow}>
            {download}
            <SiteButton href="#how-it-works" variant="ghost">See how it works</SiteButton>
          </motion.div>
          <motion.div variants={fadeUp} className={h.qr}>{qr}</motion.div>
        </motion.div>

        {/* Outer layer follows scroll (tilt/lift); inner layer plays the one-time entrance, so the two y values never fight. */}
        <motion.div className={h.heroPhone} style={{ rotate: tilt, y: lift }}>
          <motion.div initial={{ opacity: 0, y: 40 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.9, ease: EASE, delay: 0.2 }}>
            <PhoneFrame label="Oddroof home screen showing today's tasks being completed"><HomeScreen /></PhoneFrame>
          </motion.div>
          <motion.div className={h.floatA}
            initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.7, ease: EASE, delay: 0.7 }}>
            <Glass tier="photo" className={h.floatCard}><CheckCircle2 size={18} aria-hidden />Trash: Ravi&apos;s turn next</Glass>
          </motion.div>
          <motion.div className={h.floatB}
            initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.7, ease: EASE, delay: 0.85 }}>
            <Glass tier="photo" className={h.floatCard}><IndianRupee size={18} aria-hidden />₹480 split 4 ways</Glass>
          </motion.div>
          <div className={h.heroOrb}><VoiceOrb size={96} /></div>
        </motion.div>
      </div>
    </section>
  )
}
