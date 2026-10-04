import { MapPin, Wallet } from 'lucide-react'
import m from './mockups.module.css'

const PEOPLE = [
  { n: 'Meera', c: '#8B5CF6', area: 'Madhapur', budget: '₹9,000', tags: ['Early riser', 'Vegetarian'], fit: 'Good fit' },
  { n: 'Arjun', c: '#F5A524', area: 'Kondapur', budget: '₹8,000', tags: ['Works from home', 'Tidy'], fit: 'Same area' },
  { n: 'Rahul', c: '#14B8A6', area: 'Gachibowli', budget: '₹7,500', tags: ['Gym', 'Night owl'], fit: 'In budget' },
]

/** Flatmate seekers list, after the app's Discover "Find a person" tab. */
export default function DiscoverPeopleScreen() {
  return (
    <div>
      <div className={m.appBar}>Discover</div>
      <div className={m.pTabs}><span>Find a flat</span><span className={m.on}>Find a person</span></div>
      <div className={m.list}>
        {PEOPLE.map(p => (
          <div key={p.n} className={m.pCard}>
            <div className={m.pHead}>
              <span className={m.pAvatar} style={{ background: p.c }}>{p.n[0]}</span>
              <div><b>{p.n}</b><div style={{ fontSize: '.75em', color: '#475569', display: 'flex', gap: '.3em', alignItems: 'center' }}><MapPin size="1em" aria-hidden />{p.area}</div></div>
              <span className={m.pMatch}>{p.fit}</span>
            </div>
            <div style={{ fontSize: '.75em', color: '#475569', display: 'flex', gap: '.3em', alignItems: 'center' }}><Wallet size="1em" aria-hidden />Budget {p.budget}</div>
            <div className={m.dChips} style={{ marginTop: 0 }}>{p.tags.map(t => <span key={t}>{t}</span>)}</div>
          </div>
        ))}
      </div>
    </div>
  )
}
