import type { Metadata } from 'next'
import { Plus_Jakarta_Sans } from 'next/font/google'
import '@/components/site/tokens.css'
import SmoothScroll from '@/components/site/SmoothScroll'

const jakarta = Plus_Jakarta_Sans({ subsets: ['latin'], weight: ['600', '700', '800'], variable: '--font-jakarta', display: 'swap' })

export const metadata: Metadata = {
  title: { default: 'Oddroof — Your flat, sorted', template: '%s · Oddroof' },
  description: 'Oddroof keeps a shared flat running: fair task rotation, shared expenses and bills, away mode, a voice assistant, and safe flat and flatmate discovery.',
}

export default function SiteLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className={`or-site ${jakarta.variable}`}>
      <SmoothScroll>{children}</SmoothScroll>
    </div>
  )
}
