import Glass from '@/components/site/Glass'
import DownloadButton from '@/components/site/DownloadButton'
import PhoneFrame from '@/components/site/mockups/PhoneFrame'
import HomeScreen from '@/components/site/mockups/HomeScreen'

export default function HomePage() {
  return (
    <main>
      <h1>Your flat, sorted.</h1>
      <Glass tier="canvas"><DownloadButton showMeta /></Glass>
      <div style={{ width: 'min(320px, calc(100vw - 48px))', margin: '120px auto' }}>
        <PhoneFrame label="Oddroof home screen showing today's tasks being completed"><HomeScreen /></PhoneFrame>
      </div>
    </main>
  )
}
