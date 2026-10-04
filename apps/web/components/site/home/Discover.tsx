import { Search, UserPlus, Lock, BadgeCheck, MessageCircle } from 'lucide-react'
import Section, { Reveal } from '../Section'
import Glass from '../Glass'
import SiteButton from '../SiteButton'
import PhoneFrame from '../mockups/PhoneFrame'
import DiscoverFlatScreen from '../mockups/DiscoverFlatScreen'
import DiscoverPeopleScreen from '../mockups/DiscoverPeopleScreen'
import h from './home.module.css'

const JOURNEYS = [
  {
    title: 'Find a flat', icon: Search, color: 'var(--or-blue)', Screen: DiscoverFlatScreen,
    label: 'A flat listing with rent per head, beds available, lifestyle tags and a connect button',
    points: ['Rooms posted by people who live there', 'Filter by area, budget and lifestyle', 'See how the flat runs before you connect'],
  },
  {
    title: 'Find a flatmate', icon: UserPlus, color: 'var(--or-teal)', Screen: DiscoverPeopleScreen,
    label: 'People looking for a room, with area, budget and habits',
    points: ['Seekers share area, budget and habits', 'Any flatmate can post your vacancy', 'It goes live once your admin approves'],
  },
] as const

export default function Discover() {
  return (
    <Section id="discover" tone="white" eyebrow="Discover"
      title="Find your next flat, or your next flatmate."
      lede="Discover connects people with rooms and rooms with people, with privacy built in from the first tap.">
      <div className={h.journeyGrid}>
        {JOURNEYS.map((j, i) => {
          const Icon = j.icon
          return (
            <Reveal key={j.title} delay={i * 0.08}>
              <Glass tier="canvas" className={h.journeyCard}>
                <div className={h.journeyPhone} data-listing=""><PhoneFrame label={j.label}><j.Screen /></PhoneFrame></div>
                <div>
                  <span className={h.stepDot} style={{ background: j.color }}><Icon size={20} aria-hidden /></span>
                  <h3 className={h.journeyTitle}>{j.title}</h3>
                  <ul className={h.journeyList}>
                    {j.points.map(p => <li key={p}><BadgeCheck size={16} color={j.color} aria-hidden />{p}</li>)}
                  </ul>
                </div>
              </Glass>
            </Reveal>
          )
        })}
      </div>
      <div className={h.discoverFoot}>
        <p className={h.privacyNote}><Lock size={16} aria-hidden />Approximate location only. Contact details stay hidden until you both accept.</p>
        <p className={h.privacyNote}><MessageCircle size={16} aria-hidden />Talk inside the app first, share numbers only when you are ready.</p>
        <div><SiteButton href="/discover" variant="primary">Explore Discover</SiteButton></div>
      </div>
    </Section>
  )
}
