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
      <ellipse cx="60" cy="74" rx="44" ry="3.5" fill="#CFE3DD" />
      <path d="M38 40h44v32H38z" fill="#EFE6D6" />
      <path d="M82 40l14-8v34l-14 6z" fill="#E0D3BC" />
      <path d="M30 42 56 16l30 18-4 6H38z" fill="#1F6F62" />
      <path d="M56 16l14-8 30 18-14 8z" fill="#2BA08C" />
      <rect x="52" y="52" width="10" height="20" rx="2" fill="#1F5C52" />
      <circle cx="60" cy="62" r="1" fill="#E8B54A" />
      <rect x="66" y="48" width="9" height="7" fill="#BFE7E0" />
      <path d="M86 46l6-3v7l-6 3z" fill="#BFE7E0" />
      <ellipse cx="26" cy="64" rx="2.6" ry="6" fill="#5FA77D" /><ellipse cx="31" cy="64" rx="2.6" ry="6" fill="#7CBF93" />
      <path d="M25 69h8l-1 5h-6z" fill="#E5604E" />
      <ellipse cx="98" cy="66" rx="2.2" ry="5" fill="#5FA77D" /><ellipse cx="103" cy="66" rx="2.2" ry="5" fill="#7CBF93" />
      <path d="M97 70h7l-1 4h-5z" fill="#E5604E" />
    </svg>
  )
}

/** Clipboard with ticks (Manage › Tasks card). */
export function ClipboardIllustration() {
  return (
    <svg viewBox="0 0 40 40" aria-hidden>
      <rect x="9" y="6" width="22" height="30" rx="2" fill="#FBF3E6" />
      <rect x="14" y="3" width="12" height="5" rx="1.5" fill="#1F6F62" />
      <circle cx="14" cy="15" r="1.6" fill="#14B8A6" /><rect x="17.5" y="14" width="10" height="2" rx="1" fill="#E8DCC6" />
      <circle cx="14" cy="21" r="1.6" fill="#14B8A6" /><rect x="17.5" y="20" width="10" height="2" rx="1" fill="#E8DCC6" />
      <circle cx="14" cy="27" r="1.6" fill="#D9CDB6" /><rect x="17.5" y="26" width="10" height="2" rx="1" fill="#E8DCC6" />
      <circle cx="30" cy="34" r="1.6" fill="#E5604E" />
    </svg>
  )
}

/** Receipt with a plant (Manage › Expenses card). */
export function ReceiptIllustration() {
  return (
    <svg viewBox="0 0 40 40" aria-hidden>
      <rect x="8" y="5" width="18" height="30" rx="2" fill="#FBF3E6" />
      <rect x="11" y="11" width="11" height="1.8" rx=".9" fill="#E8DCC6" />
      <rect x="11" y="16" width="11" height="1.8" rx=".9" fill="#E8DCC6" />
      <rect x="11" y="21" width="8" height="1.8" rx=".9" fill="#E8DCC6" />
      <rect x="11" y="28" width="13" height="2" rx="1" fill="#14B8A6" />
      <ellipse cx="29" cy="28" rx="1.4" ry="3.2" fill="#5FA77D" /><ellipse cx="32" cy="28" rx="1.4" ry="3.2" fill="#7CBF93" />
      <path d="M28 31h5l-.6 3h-3.8z" fill="#E5604E" />
    </svg>
  )
}
