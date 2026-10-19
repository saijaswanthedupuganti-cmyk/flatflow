import { Home, CalendarDays, Plus, SearchCheck, User } from 'lucide-react'
import m from './mockups.module.css'

type Tab = 'home' | 'manage' | 'discover' | 'profile'

/** The app's floating bottom bar: Home · Manage · teal + · Discover · Profile. */
export function AppNav({ active }: { active: Tab }) {
  const item = (id: Tab, label: string, Icon: typeof Home) => (
    <span className={`${m.navItem} ${active === id ? m.navOn : ''}`}><Icon strokeWidth={2} aria-hidden />{label}</span>
  )
  return (
    <div className={m.nav}>
      {item('home', 'Home', Home)}
      {item('manage', 'Manage', CalendarDays)}
      <span className={m.navPlus}><Plus size="2em" strokeWidth={2.2} aria-hidden /></span>
      {item('discover', 'Discover', SearchCheck)}
      {item('profile', 'Profile', User)}
    </div>
  )
}

/** Small isometric house, after the illustration on the app's listing cards. */
export function HouseIllustration() {
  return (
    <svg viewBox="0 0 120 80" aria-hidden>
      <ellipse cx="60" cy="74" rx="44" ry="3.5" style={{ fill: 'var(--or-line)' }} />
      <path d="M38 40h44v32H38z" style={{ fill: 'var(--or-surface)' }} />
      <path d="M82 40l14-8v34l-14 6z" style={{ fill: 'var(--or-line)' }} />
      <path d="M30 42 56 16l30 18-4 6H38z" style={{ fill: 'var(--or-navy)' }} />
      <path d="M56 16l14-8 30 18-14 8z" style={{ fill: 'var(--or-navy-deep)' }} />
      <rect x="52" y="52" width="10" height="20" rx="2" style={{ fill: 'var(--or-navy-deep)' }} />
      <circle cx="60" cy="62" r="1" style={{ fill: 'var(--or-action-bright)' }} />
      <rect x="66" y="48" width="9" height="7" style={{ fill: 'var(--or-info-subtle)' }} />
      <path d="M86 46l6-3v7l-6 3z" style={{ fill: 'var(--or-info-subtle)' }} />
      <ellipse cx="26" cy="64" rx="2.6" ry="6" style={{ fill: 'var(--or-action)' }} /><ellipse cx="31" cy="64" rx="2.6" ry="6" style={{ fill: 'var(--or-action-bright)' }} />
      <path d="M25 69h8l-1 5h-6z" style={{ fill: 'var(--or-control-border)' }} />
      <ellipse cx="98" cy="66" rx="2.2" ry="5" style={{ fill: 'var(--or-action)' }} /><ellipse cx="103" cy="66" rx="2.2" ry="5" style={{ fill: 'var(--or-action-bright)' }} />
      <path d="M97 70h7l-1 4h-5z" style={{ fill: 'var(--or-control-border)' }} />
    </svg>
  )
}

/** Clipboard with ticks (Manage › Tasks card). */
export function ClipboardIllustration() {
  return (
    <svg viewBox="0 0 40 40" aria-hidden>
      <rect x="9" y="6" width="22" height="30" rx="2" style={{ fill: 'var(--or-surface-subtle)' }} />
      <rect x="14" y="3" width="12" height="5" rx="1.5" style={{ fill: 'var(--or-navy)' }} />
      <circle cx="14" cy="15" r="1.6" style={{ fill: 'var(--or-action)' }} /><rect x="17.5" y="14" width="10" height="2" rx="1" style={{ fill: 'var(--or-line)' }} />
      <circle cx="14" cy="21" r="1.6" style={{ fill: 'var(--or-action)' }} /><rect x="17.5" y="20" width="10" height="2" rx="1" style={{ fill: 'var(--or-line)' }} />
      <circle cx="14" cy="27" r="1.6" style={{ fill: 'var(--or-control-border)' }} /><rect x="17.5" y="26" width="10" height="2" rx="1" style={{ fill: 'var(--or-line)' }} />
      <circle cx="30" cy="34" r="1.6" style={{ fill: 'var(--or-control-border)' }} />
    </svg>
  )
}

/** Receipt with a plant (Manage › Expenses card). */
export function ReceiptIllustration() {
  return (
    <svg viewBox="0 0 40 40" aria-hidden>
      <rect x="8" y="5" width="18" height="30" rx="2" style={{ fill: 'var(--or-surface-subtle)' }} />
      <rect x="11" y="11" width="11" height="1.8" rx=".9" style={{ fill: 'var(--or-line)' }} />
      <rect x="11" y="16" width="11" height="1.8" rx=".9" style={{ fill: 'var(--or-line)' }} />
      <rect x="11" y="21" width="8" height="1.8" rx=".9" style={{ fill: 'var(--or-line)' }} />
      <rect x="11" y="28" width="13" height="2" rx="1" style={{ fill: 'var(--or-action)' }} />
      <ellipse cx="29" cy="28" rx="1.4" ry="3.2" style={{ fill: 'var(--or-action)' }} /><ellipse cx="32" cy="28" rx="1.4" ry="3.2" style={{ fill: 'var(--or-action-bright)' }} />
      <path d="M28 31h5l-.6 3h-3.8z" style={{ fill: 'var(--or-control-border)' }} />
    </svg>
  )
}
