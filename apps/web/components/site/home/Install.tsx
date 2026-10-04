import Section from '../Section'
import { release } from '@/lib/site/release'
import h from './home.module.css'

export default function Install() {
  const released = new Date(release.releasedOn).toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' })
  const steps = [
    { title: 'Download the APK', body: `Tap Download on your Android phone. The file is about ${release.sizeMb} MB.` },
    { title: 'Allow the install', body: 'Android asks once to allow installs from your browser. Tap Settings, allow, then go back.' },
    { title: 'Open Oddroof', body: 'Sign in with Google, then create your flat or join one with an invite code.' },
  ]
  return (
    <Section id="install" tone="white" eyebrow="Install" title="Up and running in a minute."
      lede="Oddroof is free and available as a direct download for Android.">
      <ol className={h.steps}>
        {steps.map(st => (
          <li key={st.title}>
            <b>{st.title}</b>
            <p style={{ margin: '6px 0 0', color: 'var(--or-text-2)' }}>{st.body}</p>
          </li>
        ))}
      </ol>
      <p className={h.installNote}>Version {release.version} · released {released} · Android {release.minAndroid} or newer. Android shows a warning for any app installed outside the Play Store; this is expected for a direct download.</p>
    </Section>
  )
}
