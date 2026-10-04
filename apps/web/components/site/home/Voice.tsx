import { Languages, ShieldCheck, Zap } from 'lucide-react'
import Section, { Reveal } from '../Section'
import Glass from '../Glass'
import VoiceDemo from '../mockups/VoiceDemo'
import h from './home.module.css'

const POINTS = [
  { icon: Languages, title: 'Talk the way you talk', body: 'English mixed with Hindi or Telugu works. “Kirana 450 kharcha” is fine.' },
  { icon: ShieldCheck, title: 'Nothing saves without you', body: 'Every change shows as a card first. Say yes or tap Save.' },
  { icon: Zap, title: 'Quick answers', body: 'Ask what you owe, whose turn it is, or what is due today.' },
]

export default function Voice() {
  return (
    <Section id="voice" tone="dark" eyebrow="Voice assistant" title="Just say it."
      lede="Tap the orb and tell Oddroof what happened. It understands, shows you the result, and saves when you agree.">
      <div className={h.voiceGrid}>
        <VoiceDemo />
        <div className={h.voicePoints}>
          {POINTS.map((p, i) => {
            const Icon = p.icon
            return (
              <Reveal key={p.title} delay={i * 0.08}>
                <Glass tier="dark" className={h.voicePoint}>
                  <Icon size={22} color="#9DB8FF" aria-hidden />
                  <div><b>{p.title}</b><p style={{ margin: '4px 0 0', color: '#C9D4E8' }}>{p.body}</p></div>
                </Glass>
              </Reveal>
            )
          })}
        </div>
      </div>
    </Section>
  )
}
