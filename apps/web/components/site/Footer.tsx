import Link from 'next/link'
import { PRODUCT_LINKS } from '@/lib/site/routes'
import s from './site.module.css'

export default function Footer() {
  return (
    <footer className={s.footer}>
      <div className={s.footerGrid}>
        <div>
          <p className={s.logo} style={{ color: '#fff' }}>Oddroof</p>
          <p style={{ maxWidth: '36ch', marginTop: 12 }}>A calmer shared flat: fair tasks, clear money, and safe ways to find your next flatmate.</p>
        </div>
        <div>
          <h3>Products</h3>
          {PRODUCT_LINKS.map(p => <div key={p.href}><Link href={p.href}>{p.label}</Link></div>)}
        </div>
        <div>
          <h3>Trust</h3>
          <div><Link href="/privacy-and-safety">Privacy &amp; safety</Link></div>
          <div><Link href="/privacy">Privacy policy</Link></div>
          <div><Link href="/terms">Terms</Link></div>
          <div><Link href="/safety">Safety</Link></div>
        </div>
        <div>
          <h3>Company</h3>
          <div><Link href="/about">About</Link></div>
          <div><a href="mailto:hello@habitiq.app">hello@habitiq.app</a></div>
        </div>
      </div>
      <p className={s.footerBottom}>© 2026 Oddroof. Made in India.</p>
    </footer>
  )
}
