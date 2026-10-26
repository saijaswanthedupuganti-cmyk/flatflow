import s from './site.module.css'

/** The Habitiq logo: the teal H mark in colour, with the name drawn in currentColor so it adapts to any surface. */
export default function Logo({ height = 24, className = '', decorative = false }: { height?: number | string; className?: string; decorative?: boolean }) {
  const h = typeof height === 'number' ? `${height}px` : height
  return (
    <span className={`${s.wordmark} ${className}`} style={{ height: h, fontSize: `calc(${h} * 0.82)` }}
      role={decorative ? undefined : 'img'} aria-label={decorative ? undefined : 'Habitiq'} aria-hidden={decorative || undefined}>
      {/* eslint-disable-next-line @next/next/no-img-element */}
      <img src="/brand/habitiq-mark.png" alt="" width={199} height={256} className={s.wordmarkMark} />
      <span>Habitiq</span>
    </span>
  )
}
