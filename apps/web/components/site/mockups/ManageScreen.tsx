import { CheckSquare, IndianRupee, Receipt, Users, ArrowDownLeft } from 'lucide-react'
import m from './mockups.module.css'

const TILES = [
  { name: 'Tasks', meta: '3 due this week', icon: CheckSquare, bg: '#E6FAF6', fg: '#0B7A6C' },
  { name: 'Expenses', meta: '₹2,340 this month', icon: IndianRupee, bg: '#EEF4FF', fg: '#1D4ED8' },
  { name: 'Bills', meta: '2 due on the 5th', icon: Receipt, bg: '#FFF4E0', fg: '#9A5B00' },
  { name: 'Members', meta: '4 in Sai flat', icon: Users, bg: '#F3EEFF', fg: '#6D28D9' },
]

/** The app's Manage hub: everything the flat shares, one tap away. */
export default function ManageScreen() {
  return (
    <div>
      <div className={m.appBar}>Manage</div>
      <div className={m.list}>
        <div className={m.card} style={{ background: 'linear-gradient(135deg,#1D4ED8,#2563EB)', color: '#fff' }}>
          <span className={m.avatar} style={{ background: 'rgba(255,255,255,.2)' }}><ArrowDownLeft size="1em" aria-hidden /></span>
          <div><div style={{ fontSize: '.75em', opacity: .85 }}>You are owed</div><b style={{ fontSize: '1.3em' }}>₹480</b></div>
        </div>
        <div className={m.mGrid}>
          {TILES.map(t => {
            const Icon = t.icon
            return (
              <div key={t.name} className={m.mTile}>
                <span className={m.mIcon} style={{ background: t.bg, color: t.fg }}><Icon size="1.1em" aria-hidden /></span>
                <b>{t.name}</b>
                <span style={{ fontSize: '.72em', color: '#475569' }}>{t.meta}</span>
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}
