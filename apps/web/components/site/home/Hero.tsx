import Image from 'next/image'
import SiteButton from '../SiteButton'
import HeroDeck from './HeroDeck'
import { HERO_PHOTO } from '@/lib/site/photos'
import h from './home.module.css'

export default function Hero({ download, qr, hasPhoto }: { download: React.ReactNode; qr: React.ReactNode; hasPhoto: boolean }) {
  return (
    <section id="hero" className={h.hero}>
      <div className={`${h.heroBg} or-golden`}>
        {hasPhoto && <Image src={HERO_PHOTO} alt="" fill priority sizes="100vw" />}
      </div>
      <div className={h.heroScrim} />
      <div className={h.heroInner}>
        {/* CSS entrance, not JS: the headline is the page's largest paint and must not wait for hydration. */}
        <div className={h.heroCopy}>
          <h1 className={h.enter} style={{ animationDelay: '0ms' }}>Find your flat. Run it together.</h1>
          <p className={`${h.sub} ${h.enter}`} style={{ animationDelay: '80ms' }}>
            Discover rooms and flatmates you can trust, then share chores, bills and money without the WhatsApp chaos.
          </p>
          <div className={`${h.ctaRow} ${h.enter}`} style={{ animationDelay: '160ms' }}>
            {download}
            <SiteButton href="#discover" variant="ghost">Explore Discover</SiteButton>
          </div>
          <div className={`${h.qr} ${h.enter}`} style={{ animationDelay: '240ms' }}>{qr}</div>
        </div>
        <div className={h.enter} style={{ animationDelay: '200ms' }}>
          <HeroDeck />
        </div>
      </div>
    </section>
  )
}
