import Image from 'next/image'
import { Reveal } from '../Section'
import { FINAL_PHOTO } from '@/lib/site/photos'
import h from './home.module.css'

export default function FinalCta({ hasPhoto, download }: { hasPhoto: boolean; download: React.ReactNode }) {
  return (
    <section id="get-oddroof" className={`${h.final} or-golden`}>
      {hasPhoto && <Image src={FINAL_PHOTO} alt="" fill sizes="100vw" style={{ objectFit: 'cover' }} />}
      <div className={h.heroScrim} style={{ background: 'rgba(14,10,28,.55)' }} />
      <Reveal className={h.finalInner}>
        <h2>Make your flat feel like home.</h2>
        <p style={{ maxWidth: '40ch', margin: 0, color: 'rgba(255,255,255,.9)' }}>Free for every flatmate. Set up your flat in two minutes.</p>
        {download}
      </Reveal>
    </section>
  )
}
