'use client'
import { Keyboard, X } from 'lucide-react'
import Logo from '../Logo'
import VoiceOrb from './VoiceOrb'
import { useVoiceLoop, VoiceCard } from './VoiceDemo'
import m from './mockups.module.css'

/** The app's full-screen voice overlay, in a phone: listening orb, transcript, confirm card, Save. */
export default function VoiceScreen() {
  const { ref, ...v } = useVoiceLoop()
  return (
    <div ref={ref} className={m.vScreen}>
      <div className={m.vTop}>
        <Logo height="1.2em" decorative />
        <span style={{ display: 'flex', gap: '.5em' }}><i><Keyboard size="1.1em" /></i><i><X size="1.1em" /></i></span>
      </div>
      <div style={{ width: '100%', marginTop: '6%' }}><VoiceOrb size="6.5em" listening={v.listening} ribbons /></div>
      <div className={m.vQuote}>{v.typed ? `“${v.typed}${v.listening ? '' : '”'}` : 'I’m listening…'}</div>
      <div style={{ width: '100%', fontSize: '.86em' }}><VoiceCard show={v.showCard} pressing={v.pressing} saved={v.saved} /></div>
    </div>
  )
}
