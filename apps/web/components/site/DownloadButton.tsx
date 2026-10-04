import { Download } from 'lucide-react'
import SiteButton from './SiteButton'
import { release, releaseMeta } from '@/lib/site/release'
import s from './site.module.css'

export default function DownloadButton({ variant = 'primary', showMeta = false, label = 'Download for Android' }:
  { variant?: 'primary' | 'light'; showMeta?: boolean; label?: string }) {
  return (
    <div>
      <SiteButton href={release.url} variant={variant} download data-download=""
        icon={<Download size={20} aria-hidden />} aria-label={`${label}, ${releaseMeta()}`}>
        {label}
      </SiteButton>
      {showMeta && <p className={s.meta} data-download-meta="">{releaseMeta()}</p>}
    </div>
  )
}
