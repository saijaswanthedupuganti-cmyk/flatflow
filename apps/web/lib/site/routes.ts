export const PRODUCT_LINKS = [
  { href: '/flat-manager', label: 'Flat manager', blurb: 'Tasks, expenses, bills and away mode' },
  { href: '/discover', label: 'Discover', blurb: 'Find a flat or a flatmate' },
  { href: '/voice', label: 'Voice assistant', blurb: 'Just say it' },
] as const

export const NAV_LINKS = [
  { href: '/privacy-and-safety', label: 'Privacy' },
  { href: '/about', label: 'About' },
] as const

export const SITE_PATHS = [
  '/', '/flat-manager', '/discover', '/voice', '/privacy-and-safety', '/about',
  '/privacy', '/terms', '/safety',
] as const

export function isSitePath(pathname: string | null): boolean {
  if (!pathname) return false
  const clean = pathname.length > 1 ? pathname.replace(/\/+$/, '') : pathname
  return (SITE_PATHS as readonly string[]).includes(clean)
}
