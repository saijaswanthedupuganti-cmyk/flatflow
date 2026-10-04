'use client'
import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import PhoneFrame from '../mockups/PhoneFrame'
import DiscoverFlatScreen from '../mockups/DiscoverFlatScreen'
import DiscoverPeopleScreen from '../mockups/DiscoverPeopleScreen'
import HomeScreen from '../mockups/HomeScreen'
import VoiceScreen from '../mockups/VoiceScreen'
import { useCalm } from '../useCalm'
import { EASE } from '../motion'
import h from './home.module.css'

// Discovery and flat management get equal billing: two cards each, discovery first.
const CARDS = [
  { name: 'Find a flat', label: 'Discover flats: a room listing with rent, lifestyle and a connect button', Screen: DiscoverFlatScreen },
  { name: 'Find a flatmate', label: 'Discover flatmates: people looking for a room nearby', Screen: DiscoverPeopleScreen },
  { name: 'Run your flat', label: 'Flat home screen: today\'s tasks being completed', Screen: HomeScreen },
  { name: 'Just say it', label: 'Voice assistant listening for a request', Screen: VoiceScreen },
] as const

// Where a card sits relative to the front one: 0 front, 1 right, 2 back, 3 left.
const SLOTS = [
  { x: '0%', scale: 1, rotate: 0, opacity: 1, z: 4 },
  { x: '26%', scale: 0.84, rotate: 6, opacity: 0.85, z: 3 },
  { x: '0%', scale: 0.74, rotate: 0, opacity: 0, z: 1 },
  { x: '-26%', scale: 0.84, rotate: -6, opacity: 0.85, z: 2 },
]

export default function HeroDeck() {
  const calm = useCalm()
  const [active, setActive] = useState(0)
  const [paused, setPaused] = useState(false)

  useEffect(() => {
    if (calm || paused) return
    const id = window.setInterval(() => setActive(a => (a + 1) % CARDS.length), 3400)
    return () => window.clearInterval(id)
  }, [calm, paused])

  return (
    <div className={h.deck} onMouseEnter={() => setPaused(true)} onMouseLeave={() => setPaused(false)}>
      <div className={h.deckStage} data-deck-active={String(active)}>
        {CARDS.map((c, i) => {
          const slot = SLOTS[(i - active + CARDS.length) % CARDS.length]
          return (
            <motion.div key={c.name} className={h.deckCard} data-deck-card="" aria-label={c.label}
              aria-hidden={slot.z !== 4}
              initial={false}
              animate={{ x: slot.x, scale: slot.scale, rotate: slot.rotate, opacity: slot.opacity }}
              transition={{ duration: 0.7, ease: EASE }}
              style={{ zIndex: slot.z }}>
              <PhoneFrame label={c.label}><c.Screen /></PhoneFrame>
            </motion.div>
          )
        })}
      </div>
      <div className={h.deckDots} role="tablist" aria-label="Choose a feature">
        {CARDS.map((c, i) => (
          <button key={c.name} role="tab" aria-selected={i === active} className={`${h.deckDot} ${i === active ? h.deckDotOn : ''}`}
            onClick={() => { setActive(i); setPaused(true) }}>
            {c.name}
          </button>
        ))}
      </div>
    </div>
  )
}
