import { EyeOff, MapPin, Trash2, Scale } from 'lucide-react'
import Section, { Reveal } from '../Section'
import Glass from '../Glass'
import SiteButton from '../SiteButton'
import h from './home.module.css'

const TILES = [
  { icon: EyeOff, color: 'var(--or-blue)', title: 'Contact stays private', body: 'Phone numbers and emails stay hidden until you accept a connection.' },
  { icon: MapPin, color: 'var(--or-teal)', title: 'Approximate location only', body: 'Listings show the area, never the exact address.' },
  { icon: Trash2, color: 'var(--or-coral)', title: 'Your data, your call', body: 'Delete your account and your data from the app at any time.' },
  { icon: Scale, color: 'var(--or-violet)', title: 'Built for India\'s DPDP Act', body: 'A named grievance contact and clear rules on what we keep and why.' },
]

export default function Trust() {
  return (
    <Section id="trust" tone="canvas" eyebrow="Privacy & trust" title="Built to be trusted in your home."
      lede="Sharing a flat means sharing a lot. Oddroof keeps what is private, private.">
      <div className={h.trustGrid}>
        {TILES.map((t, i) => {
          const Icon = t.icon
          return (
            <Reveal key={t.title} delay={i * 0.07}>
              <Glass tier="canvas" className={h.trustTile} data-trust-tile="">
                <span className={h.trustIcon} style={{ background: t.color }}><Icon size={20} aria-hidden /></span>
                <b>{t.title}</b>
                <p style={{ margin: 0, color: 'var(--or-text-2)' }}>{t.body}</p>
              </Glass>
            </Reveal>
          )
        })}
      </div>
      <div style={{ marginTop: 32 }}><SiteButton href="/privacy-and-safety" variant="light" style={{ border: '1px solid var(--or-line)' }}>Read how we protect you</SiteButton></div>
    </Section>
  )
}
