import s from './site.module.css'

/** The Oddroof wordmark (house mark + name), drawn in currentColor via a mask so it adapts to any surface. */
export default function Logo({ height = 24, className = '', decorative = false }: { height?: number | string; className?: string; decorative?: boolean }) {
  const h = typeof height === 'number' ? `${height}px` : height
  return (
    <span className={`${s.wordmark} ${className}`} style={{ height: h, width: `calc(${h} * 4.8)` }}
      role={decorative ? undefined : 'img'} aria-label={decorative ? undefined : 'Oddroof'} aria-hidden={decorative || undefined} />
  )
}
