import type { Metadata } from 'next'
import Link from 'next/link'
import PageHero from '@/components/site/PageHero'
import Section from '@/components/site/Section'
import s from '@/components/site/site.module.css'

export const metadata: Metadata = {
  title: { absolute: 'Delete your Habitiq account' },
  description: 'How to delete your Habitiq account in the app or by email, and what is deleted or kept afterwards.',
  alternates: { canonical: '/delete-account' },
}

const listStyle = { margin: '0 0 16px', paddingLeft: '1.4em' } as const
const itemStyle = { marginBottom: 8 } as const

export default function DeleteAccountPage() {
  return (
    <main>
      <PageHero eyebrow="Your account" title="Delete your Habitiq account"
        lede="You can delete your account yourself in the app, or ask us to do it by email." />

      <Section id="in-the-app" tone="white" title="Delete it in the app">
        <div className={s.prose} style={{ marginTop: 24 }}>
          <ol style={listStyle}>
            <li style={itemStyle}>Open Habitiq and tap the <strong>Profile</strong> tab.</li>
            <li style={itemStyle}>Under Preferences, tap <strong>Privacy &amp; Security</strong>.</li>
            <li style={itemStyle}>Under Danger zone, tap <strong>Delete account</strong>.</li>
            <li style={itemStyle}>Read the confirmation and tap <strong>Delete account</strong> again.</li>
          </ol>
          <p>Your account is deleted straight away and you are signed out. This can&apos;t be undone.</p>
        </div>
      </Section>

      <Section id="by-email" tone="canvas" title="Or ask us by email">
        <div className={s.prose} style={{ marginTop: 24 }}>
          <p>
            If you can&apos;t use the app, email{' '}
            <a href="mailto:hello@habitiq.app?subject=Delete%20my%20account">hello@habitiq.app</a>{' '}
            from the email address you use to sign in to Habitiq, with the subject &quot;Delete my account&quot;.
            We reply within 30 days.
          </p>
        </div>
      </Section>

      <Section id="what-happens" tone="white" title="What is deleted and what is kept">
        <div className={s.prose} style={{ marginTop: 24 }}>
          <ul style={listStyle}>
            <li style={itemStyle}>Your sign-in account is deleted and you are removed from every flat you&apos;re in.</li>
            <li style={itemStyle}>Your tasks pass to the next person in each rotation.</li>
            <li style={itemStyle}>
              Your personal data, including your seeker profile, photos, connections and the messages you sent,
              is removed within 30 days.
            </li>
            <li style={itemStyle}>
              Your flat&apos;s history may keep anonymised references, for example &quot;a member completed
              Kitchen&quot;, so the record stays accurate for your flatmates.
            </li>
            <li style={itemStyle}>Reports about abuse are kept for up to 2 years after review.</li>
            <li style={itemStyle}>We may keep records the law requires us to keep.</li>
          </ul>
          <p>
            Read the full detail in section 6 of our <Link href="/privacy">privacy policy</Link>. Questions:{' '}
            <a href="mailto:hello@habitiq.app">hello@habitiq.app</a>
          </p>
        </div>
      </Section>
    </main>
  )
}
