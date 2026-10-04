import type { Metadata } from 'next'
import Link from 'next/link'
import { ArrowRight } from 'lucide-react'
import PageHero from '@/components/site/PageHero'
import Trust from '@/components/site/home/Trust'
import Section from '@/components/site/Section'
import s from '@/components/site/site.module.css'

export const metadata: Metadata = { title: 'Privacy & safety', description: 'How Oddroof protects your contact details, location and data.' }

export default function PrivacyHubPage() {
  return (
    <main>
      <PageHero eyebrow="Privacy & safety" title="How we protect you."
        lede="Plain answers on what Oddroof shows, what it keeps private, and how to remove your data." />
      <Trust />
      <Section id="policies" tone="white" eyebrow="The full detail" title="Policies and help">
        <ul className={s.linkList}>
          <li><Link href="/privacy">Privacy policy <ArrowRight size={18} aria-hidden /></Link></li>
          <li><Link href="/terms">Terms of use <ArrowRight size={18} aria-hidden /></Link></li>
          <li><Link href="/safety">Safety guidelines <ArrowRight size={18} aria-hidden /></Link></li>
          <li><a href="mailto:hello@habitiq.app">Grievance contact: hello@habitiq.app <ArrowRight size={18} aria-hidden /></a></li>
        </ul>
      </Section>
    </main>
  )
}
