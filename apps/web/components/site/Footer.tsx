import Link from 'next/link'
import { PRODUCT_LINKS } from '@/lib/site/routes'
import s from './site.module.css'

// Column labels are h2: the footer sits after the page's h1, so h3 would skip a level.
// Inline style mirrors the old `.footer h3` rule in site.module.css.
const colHeading = { color: '#fff', fontSize: 14, letterSpacing: '.12em', textTransform: 'uppercase', margin: '0 0 12px' } as const

export default function Footer() {
  return (
    <footer className={s.footer}>
      <div className={s.footerGrid}>
        <div>
          <p className={s.logo} style={{ color: '#fff' }}>Oddroof</p>
          <p style={{ maxWidth: '36ch', marginTop: 12 }}>A calmer shared flat: fair tasks, clear money, and safe ways to find your next flatmate.</p>
        </div>
        <nav aria-labelledby="footer-products">
          <h2 id="footer-products" style={colHeading}>Products</h2>
          {PRODUCT_LINKS.map(p => <div key={p.href}><Link href={p.href}>{p.label}</Link></div>)}
        </nav>
        <nav aria-labelledby="footer-trust">
          <h2 id="footer-trust" style={colHeading}>Trust</h2>
          <div><Link href="/privacy-and-safety">Privacy &amp; safety</Link></div>
          <div><Link href="/privacy">Privacy policy</Link></div>
          <div><Link href="/terms">Terms</Link></div>
          <div><Link href="/safety">Safety</Link></div>
          <div><Link href="/delete-account">Delete your account</Link></div>
        </nav>
        <nav aria-labelledby="footer-company">
          <h2 id="footer-company" style={colHeading}>Company</h2>
          <div><Link href="/about">About</Link></div>
          <div><a href="mailto:hello@habitiq.app">hello@habitiq.app</a></div>
        </nav>
      </div>
      <p className={s.footerBottom}>© 2026 Oddroof. Made in India.</p>
    </footer>
  )
}
