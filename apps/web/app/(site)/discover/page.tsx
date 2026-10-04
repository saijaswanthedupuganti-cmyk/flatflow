import type { Metadata } from 'next'
import PageHero from '@/components/site/PageHero'
import Discover from '@/components/site/home/Discover'
import Trust from '@/components/site/home/Trust'
import FinalCta from '@/components/site/home/FinalCta'
import DownloadButton from '@/components/site/DownloadButton'

export const metadata: Metadata = { title: 'Discover flats and flatmates', description: 'Find a room or a flatmate in your city, with approximate locations and private contact details until you both accept.' }

export default function DiscoverPage() {
  return (
    <main>
      <PageHero eyebrow="Discover" title="Find your flat or your flatmate."
        lede="Rooms posted by the people who live there, and seekers who share their area, budget and habits. You choose who to talk to.">
        <DownloadButton showMeta />
      </PageHero>
      <Discover />
      <Trust />
      <FinalCta hasPhoto={false} download={<DownloadButton variant="light" />} />
    </main>
  )
}
