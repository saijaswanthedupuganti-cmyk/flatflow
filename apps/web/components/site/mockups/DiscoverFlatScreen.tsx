import { ArrowLeft, Heart, MapPin, BadgeCheck, ShieldCheck } from 'lucide-react'
import m from './mockups.module.css'

/** Flat listing detail, after the app's Discover listing screen. */
export default function DiscoverFlatScreen() {
  return (
    <div style={{ height: '100%', background: '#fff' }}>
      <div className={`${m.dPhoto} or-golden`}>
        <div className={m.dPhotoChips}><span><ArrowLeft size="1.1em" aria-hidden /></span><span><Heart size="1.1em" aria-hidden /></span></div>
      </div>
      <div className={m.dSheet}>
        <div className={m.dTitle}>Private room in a calm 3BHK</div>
        <div className={m.dMeta}><MapPin size="1em" aria-hidden />Gachibowli, Hyderabad</div>
        <div className={m.dMeta} style={{ color: '#0B7A6C' }}><BadgeCheck size="1em" aria-hidden />Posted by a flatmate · admin approved</div>
        <div className={m.dTiles}>
          <div className={m.dTile}><b>₹8,500</b>per head</div>
          <div className={m.dTile}><b>1 bed</b>available</div>
          <div className={m.dTile}><b>3</b>flatmates</div>
        </div>
        <div className={m.dChips}><span>Vegetarian</span><span>Quiet evenings</span><span>Near metro</span></div>
        <div className={m.dCta}>Connect with this flat</div>
        <div className={m.dSafe}><ShieldCheck size="1em" aria-hidden />Area only. Contacts stay hidden until you both accept.</div>
      </div>
    </div>
  )
}
