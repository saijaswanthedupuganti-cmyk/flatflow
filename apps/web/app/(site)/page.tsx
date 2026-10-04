import fs from 'node:fs'
import path from 'node:path'
import Hero from '@/components/site/home/Hero'
import Problem from '@/components/site/home/Problem'
import PinnedStory from '@/components/site/home/PinnedStory'
import Voice from '@/components/site/home/Voice'
import Discover from '@/components/site/home/Discover'
import Trust from '@/components/site/home/Trust'
import Install from '@/components/site/home/Install'
import FinalCta from '@/components/site/home/FinalCta'
import DownloadButton from '@/components/site/DownloadButton'
import DownloadQr from '@/components/site/DownloadQr'
import { FINAL_PHOTO, HERO_PHOTO } from '@/lib/site/photos'

const hasPhoto = (p: string) => fs.existsSync(path.join(process.cwd(), 'public', p))

export default function HomePage() {
  return (
    <main>
      <Hero download={<DownloadButton showMeta />} qr={<DownloadQr />} hasPhoto={hasPhoto(HERO_PHOTO)} />
      <Problem />
      <PinnedStory />
      <Voice />
      <Discover />
      <Trust />
      <Install />
      <FinalCta hasPhoto={hasPhoto(FINAL_PHOTO)} download={<DownloadButton variant="light" />} />
    </main>
  )
}
