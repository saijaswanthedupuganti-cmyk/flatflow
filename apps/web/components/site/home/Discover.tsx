'use client'
import { motion } from 'framer-motion'
import { Search, UserPlus, Lock } from 'lucide-react'
import Section from '../Section'
import Glass from '../Glass'
import SiteButton from '../SiteButton'
import ListingCard, { type Listing } from '../mockups/ListingCard'
import { EASE } from '../motion'
import h from './home.module.css'

const LISTINGS: Listing[] = [
  { area: 'Gachibowli', city: 'Hyderabad', rent: 8500, room: 'Private room', tags: ['Near metro', 'Vegetarian', 'Quiet'], tone: 'linear-gradient(135deg,#F2B565,#C9773A)' },
  { area: 'Koramangala', city: 'Bengaluru', rent: 11000, room: 'Shared room', tags: ['Balcony', 'Pet friendly'], tone: 'linear-gradient(135deg,#A8C4FF,#5B8CFF)' },
  { area: 'Baner', city: 'Pune', rent: 7200, room: 'Private room', tags: ['Gym nearby', 'Early birds'], tone: 'linear-gradient(135deg,#9BE7DC,#14B8A6)' },
]
const FAN = [{ r: -9, x: -62 }, { r: 0, x: 0 }, { r: 9, x: 62 }]

export default function Discover() {
  return (
    <Section id="discover" tone="white" eyebrow="Discover"
      title="Find your next flat, or your next flatmate."
      lede="Rooms are posted by people who live there, approved by their flat admin. Seekers share what they are looking for. You choose who to talk to.">
      <div className={h.discoverGrid}>
        <div className={h.fan} aria-label="Example flat listings" role="group">
          {LISTINGS.map((l, i) => (
            <motion.div key={l.area} data-listing="" className={h.listing}
              initial={{ rotate: 0, x: 0, opacity: 0 }}
              whileInView={{ rotate: FAN[i].r, x: FAN[i].x, opacity: 1 }}
              viewport={{ once: true, amount: 0.4 }}
              transition={{ duration: 0.8, ease: EASE, delay: i * 0.08 }}
              style={{ zIndex: i === 1 ? 3 : 1 }}>
              <ListingCard listing={l} />
            </motion.div>
          ))}
        </div>
        <div className={h.journeys}>
          <Glass tier="canvas" className={h.journey}><b style={{ display: 'flex', gap: 8, alignItems: 'center' }}><Search size={18} color="var(--or-blue)" aria-hidden />Looking for a room</b><p style={{ margin: '6px 0 0', color: 'var(--or-text-2)' }}>Filter by area, budget and lifestyle. See how a flat runs before you connect.</p></Glass>
          <Glass tier="canvas" className={h.journey}><b style={{ display: 'flex', gap: 8, alignItems: 'center' }}><UserPlus size={18} color="var(--or-teal)" aria-hidden />Filling a room</b><p style={{ margin: '6px 0 0', color: 'var(--or-text-2)' }}>Any flatmate can post the vacancy. It goes live once your admin approves.</p></Glass>
          <p className={h.privacyNote}><Lock size={16} aria-hidden />Approximate location only. Contact details stay hidden until you accept.</p>
          <div><SiteButton href="/discover" variant="primary">Explore Discover</SiteButton></div>
        </div>
      </div>
    </Section>
  )
}
