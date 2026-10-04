import Glass from '@/components/site/Glass'
import DownloadButton from '@/components/site/DownloadButton'

export default function HomePage() {
  return (
    <main>
      <h1>Your flat, sorted.</h1>
      <Glass tier="canvas"><DownloadButton showMeta /></Glass>
    </main>
  )
}
