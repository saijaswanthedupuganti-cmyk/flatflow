import QRCode from 'qrcode'
import { release } from '@/lib/site/release'
import { BRAND } from './brand'

/** Server-rendered QR so a desktop visitor can scan the APK link with their phone. */
export default async function DownloadQr({ size = 132 }: { size?: number }) {
  const abs = new URL(release.url, 'https://habitiq.app').toString()
  const svg = await QRCode.toString(abs, { type: 'svg', margin: 1, color: { dark: BRAND.navy, light: BRAND.surface } })
  return (
    <figure style={{ margin: 0, width: size }} data-download-qr={abs}>
      <div style={{ width: size, height: size, borderRadius: 16, overflow: 'hidden', background: 'var(--or-surface)' }}
        role="img" aria-label="QR code to download Habitiq for Android"
        dangerouslySetInnerHTML={{ __html: svg }} />
      <figcaption style={{ fontSize: 12, marginTop: 8, opacity: .85 }}>Scan to download on your phone</figcaption>
    </figure>
  )
}
