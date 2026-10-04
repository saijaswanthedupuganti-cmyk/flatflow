import m from './mockups.module.css'

/** Phone with a brushed-metal edge, side buttons and a glass glare that drifts slowly across the screen. */
export default function PhoneFrame({ label, className = '', children }: { label: string; className?: string; children: React.ReactNode }) {
  return (
    <div role="img" aria-label={label} className={`${m.phone} ${className}`}>
      <span className={m.btnPower} aria-hidden />
      <span className={m.btnVolume} aria-hidden />
      <div className={m.screen} aria-hidden>
        <div className={m.notch} />
        {children}
        <span className={m.glare} />
      </div>
    </div>
  )
}
