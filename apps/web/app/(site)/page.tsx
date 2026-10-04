import fs from 'node:fs'
import path from 'node:path'
import Hero from '@/components/site/home/Hero'
import { HERO_PHOTO } from '@/lib/site/photos'
import DownloadButton from '@/components/site/DownloadButton'
import DownloadQr from '@/components/site/DownloadQr'

const hasPhoto = (p: string) => fs.existsSync(path.join(process.cwd(), 'public', p))

export default function HomePage() {
  return (
    <main>
      <Hero download={<DownloadButton showMeta />} qr={<DownloadQr />} hasPhoto={hasPhoto(HERO_PHOTO)} />
      {/* Sections from Tasks 6–10 are inserted here in order: Problem, PinnedStory, Voice, Discover, Trust, Install, FinalCta */}
    </main>
  )
}
