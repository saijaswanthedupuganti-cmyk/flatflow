import {
  Check, Repeat, ArrowLeftRight, Plane, SlidersHorizontal, MessageCircle, ReceiptText, Megaphone,
  CalendarCheck, KeyRound, ShieldAlert, Bookmark, Mic, Crown, Fingerprint, Flag, Ban, Search, Bell,
} from 'lucide-react'
import Section, { Reveal } from '../Section'
import f from './features.module.css'

type Side = 'flat' | 'discover' | 'app'
type Tile = { key: string; size: 'lg' | 'wide' | 'sm'; side: Side; icon: typeof Check; title: string; body: string; ui: React.ReactNode }

const av = (l: string, cls = '') => <span className={`${f.av} ${cls}`}>{l}</span>

/**
 * Every tile is a feature that ships in the Android app today (inventory and proof files are listed in the
 * redesign report). Tiles alternate between running the flat and Discover so neither side dominates.
 */
const TILES: Tile[] = [
  {
    key: 'tasks', size: 'lg', side: 'flat', icon: Repeat, title: 'Chores that take turns',
    body: 'Daily, weekly, fortnightly or monthly. Tick a task off and the next flatmate is up. Anyone marked away is skipped.',
    ui: (
      <div className={f.uiTasks}>
        <div className={f.freq}>{['Daily', 'Weekly', 'Fortnightly', 'Monthly'].map((x, i) => <span key={x} className={i === 1 ? f.freqOn : ''}>{x}</span>)}</div>
        <div className={f.taskCard}>
          <span className={f.tick}><Check size={12} strokeWidth={3} aria-hidden /></span>
          <div><b>Take out the trash</b><small>Weekly rotation</small></div>
        </div>
        <div className={f.rotation}>
          <span className={f.rotRing} aria-hidden />
          {av('Y', f.avMint)}{av('R', f.avRose)}{av('M')}{av('A', f.avBlue)}
        </div>
        <p className={f.uiCaption}>Next up passes along the queue</p>
      </div>
    ),
  },
  {
    key: 'discover', size: 'lg', side: 'discover', icon: Search, title: 'Find a flat, or a flatmate',
    body: 'Browse rooms posted by people who live there, or people looking for a room. Rent per head, area and habits up front.',
    ui: (
      <div className={f.uiDiscover}>
        <div className={f.seg}><span className={f.segThumb} aria-hidden /><span>Find a flat</span><span>Find a person</span></div>
        <div className={f.cards}>
          <div className={f.listing}>
            <div><b>Lakeview Residency 2A</b><small>Madhapur, Hyderabad</small></div>
            <span className={f.rent}>₹9,000<small>per head</small></span>
          </div>
          <div className={f.listing}>
            {av('MN')}
            <div><b>Meera Nair</b><small>Looking in Madhapur · Budget ₹9,000</small></div>
          </div>
        </div>
        <div className={f.tagRow}><span>Early bird</span><span>Near metro</span><span>Vegetarian</span></div>
      </div>
    ),
  },
  {
    key: 'swap', size: 'sm', side: 'flat', icon: ArrowLeftRight, title: 'Swap a turn',
    body: 'Ask a flatmate to cover a task. They accept or decline in the app.',
    ui: (
      <div className={f.uiSwap}>
        <div className={f.banner}><ArrowLeftRight size={14} aria-hidden /><b>1 pending swap request</b><span>Review</span></div>
      </div>
    ),
  },
  {
    key: 'away', size: 'sm', side: 'flat', icon: Plane, title: 'Away mode',
    body: 'Going home for a while? Your flatmates see you are away and the rotation skips you.',
    ui: <div className={f.uiAway}><div><b>I&apos;m away</b><small>Oct 28 to Nov 3</small></div><span className={f.toggle} aria-hidden /></div>,
  },
  {
    key: 'filters', size: 'sm', side: 'discover', icon: SlidersHorizontal, title: 'Filters that matter',
    body: 'Area, budget, who it suits, and lifestyle tags like vegetarian or work from home.',
    ui: (
      <div className={f.uiFilters}>
        {['Vegetarian', 'Work from home', 'Furnished', 'Quiet'].map((x, i) => (
          <span key={x} className={f.fchip} style={{ '--d': `${i * 0.6}s` } as React.CSSProperties}><Check size={11} strokeWidth={3} aria-hidden />{x}</span>
        ))}
      </div>
    ),
  },
  {
    key: 'chat', size: 'sm', side: 'discover', icon: MessageCircle, title: 'Connect, then chat',
    body: 'Send a request. Once it is accepted, message each other inside Habitiq.',
    ui: (
      <div className={f.uiChat}>
        <span className={f.bubble}>Is the room still free?</span>
        <span className={`${f.bubble} ${f.mine}`}>Yes, come and see it Saturday.</span>
      </div>
    ),
  },
  {
    key: 'split', size: 'wide', side: 'flat', icon: ReceiptText, title: 'Split it, then settle up',
    body: 'Split with everyone or only the people involved, equally or by custom amounts. See who owes whom and record each settlement.',
    ui: (
      <div className={f.uiSplit}>
        <div className={f.splitWho}>
          <span className={f.small}>Split with</span>
          <div className={f.whoRow}>{av('Y', f.avMint)}{av('R', f.avRose)}{av('M')}<span className={f.out}>{av('A', f.avBlue)}</span></div>
          <span className={f.small}>₹1,200 · ₹400 each</span>
        </div>
        <div className={f.owe}>
          <div>{av('R', f.avRose)}<span><b>You owe Rahul</b><small>Record a settlement</small></span><b>₹850</b></div>
          <div>{av('M')}<span><b>Meera owes you</b><small>Mark received</small></span><b>₹840.50</b></div>
        </div>
      </div>
    ),
  },
  {
    key: 'post', size: 'wide', side: 'discover', icon: Megaphone, title: 'Post a vacancy, or say you are looking',
    body: 'Any flatmate can draft your flat’s vacancy; your admin reviews it before it goes live, and you get a notification either way. Looking for a room? Post that instead.',
    ui: (
      <div className={f.uiPost}>
        <div className={f.postCard}>
          <small>Vacancy</small><b>Room in Sunrise Heights 4B</b>
          <span className={f.status}><span className={f.stPending}>Waiting for admin review</span><span className={f.stLive}><Check size={12} strokeWidth={3} aria-hidden />Approved, live on Discover</span></span>
        </div>
        <div className={f.note}><Bell size={14} aria-hidden />Notified when it is approved or declined</div>
      </div>
    ),
  },
  {
    key: 'bills', size: 'sm', side: 'flat', icon: CalendarCheck, title: 'Monthly bills',
    body: 'Rent, electricity and wifi split into shares. Mark them paid, then close the month.',
    ui: (
      <div className={f.uiBills}>
        {['Rent', 'Electricity', 'Wifi'].map((b, i) => (
          <div key={b} style={{ '--d': `${i * 0.7}s` } as React.CSSProperties}><span>{b}</span><span className={f.paid}>Paid</span></div>
        ))}
      </div>
    ),
  },
  {
    key: 'invite', size: 'sm', side: 'flat', icon: KeyRound, title: 'Invite code and approvals',
    body: 'Share a code to bring flatmates in. Flats that need approval let the admin accept each join request.',
    ui: <div className={f.uiInvite}><span className={f.code}>FLAT-7KQ2</span><span className={f.approve}>Approve</span></div>,
  },
  {
    key: 'safety', size: 'sm', side: 'discover', icon: ShieldAlert, title: 'Safety, report and block',
    body: 'Safety tips before viewings. Report a listing or person, or block them so they cannot contact you.',
    ui: <div className={f.uiSafety}><p>Keep chats in Habitiq. Never pay before viewing.</p><div><span><Flag size={12} aria-hidden />Report</span><span><Ban size={12} aria-hidden />Block</span></div></div>,
  },
  {
    key: 'saved', size: 'sm', side: 'discover', icon: Bookmark, title: 'Saved flats',
    body: 'Keep a private shortlist on your phone before you connect.',
    ui: (
      <div className={f.uiSaved}>
        <div><Bookmark size={13} aria-hidden className={f.saveOn} /><span>Sunrise Heights 4B</span><small>₹8,500</small></div>
        <div><Bookmark size={13} aria-hidden className={f.saveOn} /><span>Lakeview Residency 2A</span><small>₹9,000</small></div>
      </div>
    ),
  },
  {
    key: 'voice', size: 'wide', side: 'app', icon: Mic, title: 'Talk or type to your flat',
    body: 'English with Hindi or Telugu words works. Every change shows as a card first and saves only when you say yes. Odd AI reply? Report it in one tap.',
    ui: (
      <div className={f.uiVoice}>
        <span className={f.orb} aria-hidden><Mic size={18} aria-hidden /></span>
        <div className={f.voiceText}>
          <span className={f.said}>&ldquo;Kirana 450 kharcha&rdquo;</span>
          <span className={f.confirm}><ReceiptText size={14} aria-hidden />Groceries · ₹450 · Split with everyone<b>Save</b></span>
        </div>
      </div>
    ),
  },
  {
    key: 'members', size: 'sm', side: 'flat', icon: Crown, title: 'Members and admin',
    body: 'If the admin leaves, the longest-standing member takes over and their tasks pass on.',
    ui: (
      <div className={f.uiMembers}>
        <span className={f.crown} aria-hidden><Crown size={12} aria-hidden /></span>
        {av('A', f.avBlue)}{av('R', f.avRose)}{av('M')}
      </div>
    ),
  },
  {
    key: 'lock', size: 'sm', side: 'app', icon: Fingerprint, title: 'Biometric lock',
    body: 'Use your fingerprint or face to open Habitiq.',
    ui: <div className={f.uiLock}><Fingerprint size={34} strokeWidth={1.5} aria-hidden /><span className={f.scan} aria-hidden /></div>,
  },
]

const SIDE_LABEL: Record<Side, string> = { flat: 'Flat', discover: 'Discover', app: 'Whole app' }

export default function Features() {
  return (
    <Section id="features" tone="white" className={f.section} eyebrow="Everything in the app"
      title="Everything your flat needs, in one place."
      lede="Running the flat and finding the next flatmate get the same care. Every tile below is in the Android app today.">
      <ul className={f.grid}>
        {TILES.map((t, i) => {
          const Icon = t.icon
          return (
            <li key={t.key} className={`${f.tile} ${f[t.size]}`} data-side={t.side} data-feature={t.key}>
              <Reveal className={f.tileInner} delay={(i % 4) * 0.05}>
                <div className={f.ui} aria-hidden>{t.ui}</div>
                <div className={f.copy}>
                  <span className={f.kicker}><Icon size={14} strokeWidth={2} aria-hidden />{SIDE_LABEL[t.side]}</span>
                  <h3>{t.title}</h3>
                  <p>{t.body}</p>
                </div>
              </Reveal>
            </li>
          )
        })}
      </ul>
    </Section>
  )
}
