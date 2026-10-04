import type { Metadata } from 'next'
import PageHero from '@/components/site/PageHero'
import Section from '@/components/site/Section'
import FinalCta from '@/components/site/home/FinalCta'
import DownloadButton from '@/components/site/DownloadButton'
import s from '@/components/site/site.module.css'

export const metadata: Metadata = { title: 'About', description: 'Oddroof is built in India to make shared living calmer: finding the right flat and flatmates, then running the flat fairly.' }

export default function AboutPage() {
  return (
    <main>
      <PageHero eyebrow="About" title="Shared living, without the chasing."
        lede="Oddroof is built in India for people who share flats and PGs." />
      <Section id="story" tone="white" eyebrow="Why we built it" title="Two problems, one app.">
        <div className={s.prose} style={{ marginTop: 24 }}>
          <p>Finding a room or a flatmate you can trust is hard, and once you move in, the flat runs on a WhatsApp group that never quite keeps up.</p>
          <p>Oddroof brings both together: discover flats and flatmates with privacy built in, then share chores, bills and money in one calm place.</p>
          <p>Questions, ideas or press: <a href="mailto:hello@habitiq.app">hello@habitiq.app</a></p>
        </div>
      </Section>
      <FinalCta hasPhoto={false} download={<DownloadButton variant="light" />} />
    </main>
  )
}
