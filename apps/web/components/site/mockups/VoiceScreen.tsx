import VoiceOrb from './VoiceOrb'
import m from './mockups.module.css'

/** The app's full-screen voice overlay, sized for a phone frame. */
export default function VoiceScreen() {
  return (
    <div className={m.vScreen}>
      <VoiceOrb size={88} />
      <div className={m.vTitle}>I&apos;m listening…</div>
      <div style={{ fontSize: '.8em', opacity: .75 }}>Tell me what you need.</div>
      <div className={m.vChips}>
        <span>What do I need to do today?</span>
        <span>Add a task for Rahul</span>
        <span>Who&apos;s on garbage duty?</span>
        <span>Split an expense</span>
      </div>
    </div>
  )
}
