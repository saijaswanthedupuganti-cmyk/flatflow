import Image from 'next/image'
import { Reveal } from '../Section'
import PlayStoreSoon from '../PlayStoreSoon'
import s from '../site.module.css'
import { FINAL_PHOTO } from '@/lib/site/photos'
import h from './home.module.css'

export default function FinalCta({ hasPhoto, download }: { hasPhoto: boolean; download: React.ReactNode }) {
  return (
    <section id="get-oddroof" className={`${h.final} or-golden`}>
      {hasPhoto && <Image src={FINAL_PHOTO} alt="" fill sizes="100vw" style={{ objectFit: 'cover' }} />}
      <div className={h.heroScrim} style={{ background: 'rgba(10,18,34,.45)' }} />
      <Reveal className={h.finalInner}>
        <h2>Make your flat feel like home.</h2>
        <p style={{ maxWidth: '40ch', margin: 0, color: 'rgba(255,255,255,.9)' }}>Free for every flatmate. Create your flat or join one with an invite code.</p>
        <div className={s.storeRow} style={{ justifyContent: 'center' }}>{download}<PlayStoreSoon onDark /></div>
      </Reveal>
    </section>
  )
}
