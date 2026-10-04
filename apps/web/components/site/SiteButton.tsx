import Link from 'next/link'
import s from './site.module.css'

export default function SiteButton({ href, variant = 'primary', icon, download, children, ...rest }:
  { href: string; variant?: 'primary' | 'ghost' | 'light' | 'soft'; icon?: React.ReactNode; download?: boolean } & Omit<React.AnchorHTMLAttributes<HTMLAnchorElement>, 'href'>) {
  const cls = `${s.btn} ${s[variant]}`
  if (download || href.startsWith('http') || href.endsWith('.apk')) {
    return <a href={href} className={cls} download={download ? '' : undefined} {...rest}>{icon}{children}</a>
  }
  return <Link href={href} className={cls} {...rest}>{icon}{children}</Link>
}
