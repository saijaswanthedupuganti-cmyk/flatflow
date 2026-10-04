import s from './site.module.css'

/** Golden-hour band that opens every inner site page. */
export default function PageHero({ eyebrow, title, lede, children }: { eyebrow: string; title: string; lede: string; children?: React.ReactNode }) {
  return (
    <section id="page-hero" className={`${s.pageHero} or-golden`}>
      <div className={s.pageHeroScrim} />
      <div className={s.pageHeroInner}>
        <p className={s.eyebrow}>{eyebrow}</p>
        <h1>{title}</h1>
        <p className={s.pageLede}>{lede}</p>
        {children && <div style={{ marginTop: 28 }}>{children}</div>}
      </div>
    </section>
  )
}
