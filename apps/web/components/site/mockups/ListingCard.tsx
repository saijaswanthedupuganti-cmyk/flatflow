import { MapPin, BadgeCheck } from 'lucide-react'
import h from '../home/home.module.css'

export type Listing = { area: string; city: string; rent: number; room: string; tags: string[]; tone: string }

export default function ListingCard({ listing }: { listing: Listing }) {
  return (
    <div className={h.listingInner}>
      <div className={h.listingPhoto} style={{ background: listing.tone }} />
      <div className={h.listingBody}>
        <b style={{ fontSize: 18 }}>₹{listing.rent.toLocaleString('en-IN')}<span style={{ fontWeight: 500, fontSize: 14, color: 'var(--or-text-2)' }}> /head</span></b>
        <span style={{ display: 'flex', gap: 6, alignItems: 'center', color: 'var(--or-text-2)', fontSize: 14 }}><MapPin size={14} aria-hidden />{listing.area}, {listing.city}</span>
        <span style={{ display: 'flex', gap: 6, alignItems: 'center', fontSize: 13, color: '#0B7A6C' }}><BadgeCheck size={14} aria-hidden />{listing.room} · admin approved</span>
        <div className={h.listingTags}>{listing.tags.map(t => <span key={t}>{t}</span>)}</div>
      </div>
    </div>
  )
}
