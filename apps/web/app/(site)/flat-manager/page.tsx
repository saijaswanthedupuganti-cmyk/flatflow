import type { Metadata } from 'next'
import PageHero from '@/components/site/PageHero'
import Problem from '@/components/site/home/Problem'
import PinnedStory from '@/components/site/home/PinnedStory'
import FinalCta from '@/components/site/home/FinalCta'
import DownloadButton from '@/components/site/DownloadButton'

export const metadata: Metadata = { title: 'Flat manager', description: 'Fair task rotation, shared expenses, monthly bills and away mode for shared flats.' }

export default function FlatManagerPage() {
  return (
    <main>
      <PageHero eyebrow="Flat manager" title="Run your flat, fairly."
        lede="Chores that rotate on their own, expenses that split themselves, bills in one place, and away mode for when you travel.">
        <DownloadButton showMeta />
      </PageHero>
      <Problem />
      <PinnedStory />
      <FinalCta hasPhoto={false} download={<DownloadButton variant="light" />} />
    </main>
  )
}
