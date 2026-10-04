'use client'
import Link from 'next/link'
import { useEffect, useRef, useState } from 'react'
import { ChevronDown, Menu, X, Download, Home } from 'lucide-react'
import { AnimatePresence, motion, useMotionValueEvent, useScroll } from 'framer-motion'
import Glass from './Glass'
import { NAV_LINKS, PRODUCT_LINKS } from '@/lib/site/routes'
import { release } from '@/lib/site/release'
import s from './site.module.css'
import { EASE } from './motion'

export default function Navbar() {
  const { scrollY } = useScroll()
  const [scrolled, setScrolled] = useState(false)
  const [productsOpen, setProductsOpen] = useState(false)
  const [sheetOpen, setSheetOpen] = useState(false)
  const productsRef = useRef<HTMLDivElement>(null)
  useMotionValueEvent(scrollY, 'change', v => setScrolled(v > 40))

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') { setProductsOpen(false); setSheetOpen(false) } }
    const onClick = (e: MouseEvent) => { if (!productsRef.current?.contains(e.target as Node)) setProductsOpen(false) }
    window.addEventListener('keydown', onKey)
    window.addEventListener('mousedown', onClick)
    return () => { window.removeEventListener('keydown', onKey); window.removeEventListener('mousedown', onClick) }
  }, [])

  return (
    <div className={s.navWrap}>
      <Glass tier="photo" as="nav" aria-label="Main" className={`${s.nav} ${scrolled ? s.navScrolled : ''}`}>
        <Link href="/" className={s.logo} aria-label="Oddroof home"><Home size={22} aria-hidden />Oddroof</Link>
        <div className={s.navLinks}>
          <div ref={productsRef} style={{ position: 'relative' }}>
            <button className={s.navTrigger} aria-expanded={productsOpen} onClick={() => setProductsOpen(o => !o)}>
              Products <ChevronDown size={16} aria-hidden />
            </button>
            <AnimatePresence>
              {productsOpen && (
                <motion.div initial={{ opacity: 0, y: -6 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -6 }}
                  transition={{ duration: 0.2, ease: EASE }}>
                  <Glass tier="canvas" className={s.menu}>
                    {PRODUCT_LINKS.map(p => (
                      <Link key={p.href} href={p.href} className={s.menuItem} onClick={() => setProductsOpen(false)}>
                        {p.label}<small>{p.blurb}</small>
                      </Link>
                    ))}
                  </Glass>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
          {NAV_LINKS.map(l => <Link key={l.href} href={l.href} className={s.navLink}>{l.label}</Link>)}
        </div>
        <a href={release.url} download className={`${s.btn} ${s.primary} ${s.navCta}`} data-download="">
          <Download size={18} aria-hidden />Download
        </a>
        <button className={s.burger} aria-label="Open menu" onClick={() => setSheetOpen(true)}><Menu size={22} aria-hidden /></button>
      </Glass>

      <AnimatePresence>
        {sheetOpen && (
          <motion.div className={s.sheet} style={{ pointerEvents: 'auto' }} initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            onClick={() => setSheetOpen(false)}>
            <motion.div role="dialog" aria-modal="true" aria-label="Menu" className={s.sheetPanel}
              initial={{ x: 40 }} animate={{ x: 0 }} exit={{ x: 40 }} transition={{ duration: 0.25, ease: EASE }}
              onClick={e => e.stopPropagation()}>
              <button className={s.burger} style={{ marginLeft: 'auto' }} aria-label="Close menu" onClick={() => setSheetOpen(false)}><X size={22} aria-hidden /></button>
              {PRODUCT_LINKS.map(p => <Link key={p.href} href={p.href} onClick={() => setSheetOpen(false)}>{p.label}</Link>)}
              {NAV_LINKS.map(l => <Link key={l.href} href={l.href} onClick={() => setSheetOpen(false)}>{l.label}</Link>)}
              <a href={release.url} download className={`${s.btn} ${s.primary}`} style={{ marginTop: 16, border: 0 }} data-download="">
                <Download size={18} aria-hidden />Download for Android
              </a>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}
