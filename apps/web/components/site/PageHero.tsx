import PageHeroPhone from './PageHeroPhone'
import s from './site.module.css'

/** Navy-to-blue band that opens every inner site page; product pages add their live app screen on the right. */
export default function PageHero({ eyebrow, title, lede, children }: { eyebrow: string; title: string; lede: string; children?: React.ReactNode }) {
  return (
    <section id="page-hero" className={`${s.pageHero} or-golden`}>
      <div className={s.pageHeroScrim} />
      <div className={s.pageHeroInner}>
        <div>
          <p className={`${s.eyebrow} ${s.phEnter}`} style={{ animationDelay: '0ms' }}>{eyebrow}</p>
          <h1 className={s.phEnter} style={{ animationDelay: '0ms' }}>{title}</h1>
          <p className={`${s.pageLede} ${s.phEnter}`} style={{ animationDelay: '80ms' }}>{lede}</p>
          {children && <div className={s.phEnter} style={{ marginTop: 28, animationDelay: '160ms' }}>{children}</div>}
        </div>
        <PageHeroPhone className={`${s.pageHeroPhone} ${s.phEnter}`} />
      </div>
    </section>
  )
}
