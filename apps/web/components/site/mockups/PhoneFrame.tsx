import m from './mockups.module.css'

export default function PhoneFrame({ label, className = '', children }: { label: string; className?: string; children: React.ReactNode }) {
  return (
    <div role="img" aria-label={label} className={`${m.phone} ${className}`}>
      <div className={m.screen} aria-hidden>
        <div className={m.notch} />
        {children}
      </div>
    </div>
  )
}
