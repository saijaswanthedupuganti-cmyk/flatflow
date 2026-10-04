import type { Metadata } from 'next'
import PageHero from '@/components/site/PageHero'
import Voice from '@/components/site/home/Voice'
import FinalCta from '@/components/site/home/FinalCta'
import DownloadButton from '@/components/site/DownloadButton'

export const metadata: Metadata = { title: 'Voice assistant', description: 'Tell Oddroof what happened in your own words. It shows the result and saves when you agree.' }

export default function VoicePage() {
  return (
    <main>
      <PageHero eyebrow="Voice assistant" title="Your flat's voice assistant."
        lede="Add an expense, mark a task done or ask what you owe, by just saying it. English with Hindi or Telugu mixed in is fine.">
        <DownloadButton showMeta />
      </PageHero>
      <Voice />
      <FinalCta hasPhoto={false} download={<DownloadButton variant="light" />} />
    </main>
  )
}
