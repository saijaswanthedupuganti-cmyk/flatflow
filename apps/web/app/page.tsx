'use client'

import { useEffect, useMemo, useState, type ReactNode } from 'react'
import Image from 'next/image'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { AnimatePresence, motion } from 'framer-motion'
import {
  ArrowRight,
  BadgeCheck,
  Calendar,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  GraduationCap,
  Heart,
  Home,
  Menu,
  Minus,
  Moon,
  PawPrint,
  Plus,
  ReceiptIndianRupee,
  Search,
  Sun,
  UserRound,
  UsersRound,
  X,
} from 'lucide-react'
import { AuthForm } from '@/components/ui/navbar'
import { useAuthStore } from '@/store/useAuthStore'
import styles from './landing.module.css'

const INK = '#111c1b'
const BODY = '#52615e'
const MUTED = '#52615e'
const HAIRLINE = '#e7efed'
const STRONG = '#f3f7f6'
const RAUSCH = '#0f766e'
const RAUSCH_ACTIVE = '#0d6b64'
const ELEVATION =
  'rgba(0,0,0,0.02) 0px 0px 0px 1px, rgba(0,0,0,0.04) 0px 2px 6px 0px, rgba(0,0,0,0.1) 0px 4px 8px 0px'

type Mode = 'rooms' | 'people' | 'flat'
type Category = 'all' | 'sunny' | 'campus' | 'quiet' | 'pets' | 'short' | 'verified'

type Listing = {
  id: string
  kind: 'rooms' | 'people'
  title: string
  meta: string
  place: string
  city: string
  when: string
  price: string
  unit: string
  rating: string
  badge?: string
  category: Category[]
  photos: string[]
  beds: string
  about: string
  amenities: string[]
  host: string
  hostNote: string
  reviews: { name: string; date: string; text: string }[]
}

const photo = (id: string) =>
  `https://images.unsplash.com/${id}?auto=format&fit=crop&w=1200&q=70`

const LISTINGS: Listing[] = [
  {
    id: 'banjara',
    kind: 'rooms',
    title: 'Banjara Hills',
    meta: 'Room in a 3-bed flat',
    place: 'Road No. 12, Hyderabad',
    city: 'Hyderabad',
    when: '1 Oct – 30 Mar',
    price: '₹14,500',
    unit: 'month',
    rating: '4.92',
    badge: 'Flat favorite',
    category: ['sunny', 'verified', 'quiet'],
    photos: [
      photo('photo-1502672260266-1c1ef2d93688'),
      photo('photo-1560448204-e02f11c3d0e2'),
      photo('photo-1554995207-c18c203602cb'),
    ],
    beds: '1 of 3 beds open',
    about: 'A bright third-floor flat with a real kitchen and a chore board that already runs. Garbage, mopping, and dishes rotate every Sunday. Bills land in one place.',
    amenities: ['Chores already rotating', 'Split rent and utilities', 'Two flatmates, both working', 'Washer in the flat', 'Quiet after 11'],
    host: 'Kiran',
    hostNote: 'Replies within a day · 2 years in this flat',
    reviews: [
      { name: 'Meera', date: 'August 2026', text: 'I moved in on a Sunday and the rotation was already written on the fridge. Nobody had to explain the rules twice.' },
      { name: 'Arjun', date: 'May 2026', text: 'Rent share was exact. The electricity bill did not turn into a group-chat argument.' },
    ],
  },
  {
    id: 'koramangala',
    kind: 'rooms',
    title: 'Koramangala',
    meta: 'Room in a 2-bed flat',
    place: '5th Block, Bengaluru',
    city: 'Bengaluru',
    when: '15 Oct – open',
    price: '₹18,000',
    unit: 'month',
    rating: '4.81',
    badge: 'Flat favorite',
    category: ['campus', 'sunny', 'verified'],
    photos: [
      photo('photo-1522708323590-d24dbb6b0267'),
      photo('photo-1600210492486-724fe5c67fb0'),
      photo('photo-1600585154340-be6161a56a0c'),
    ],
    beds: '1 of 2 beds open',
    about: 'A compact flat two stops from the forum. One desk by the window, a small balcony, and a flatmate who cooks most weeknights.',
    amenities: ['Near the metro', 'Desk and chair', 'Weekly cleaning rotation', 'Pet on approval', 'Fiber already installed'],
    host: 'Ananya',
    hostNote: 'Steady flatmate · responds in a few hours',
    reviews: [
      { name: 'Dev', date: 'July 2026', text: 'The listing matched the flat. Sun in the morning, quiet by ten, and the rotation app was already on the phone they handed me.' },
    ],
  },
  {
    id: 'andheri',
    kind: 'rooms',
    title: 'Andheri West',
    meta: 'Room in a 4-bed flat',
    place: 'Lokhandwala, Mumbai',
    city: 'Mumbai',
    when: '1 Nov – 31 Jan',
    price: '₹22,000',
    unit: 'month',
    rating: '4.74',
    category: ['short', 'campus'],
    photos: [
      photo('photo-1493809842364-78817add7ffb'),
      photo('photo-1600607687939-ce8a6c25118c'),
      photo('photo-1616594039964-ae9021a400a0'),
    ],
    beds: '1 of 4 beds open',
    about: 'A short stay while someone is home for a family month. Four people, one cleaner rotation, and a clear end date so nobody is stuck.',
    amenities: ['Short stay welcome', 'Four flatmates', 'AC in the room', 'Cook visits weekdays', 'Swap requests allowed'],
    host: 'Rahul',
    hostNote: 'Hosting a temporary bed · replies same day',
    reviews: [
      { name: 'Sara', date: 'June 2026', text: 'I needed ten weeks, not a year. The dates were on the listing and they stayed true.' },
    ],
  },
  {
    id: 'kothrud',
    kind: 'rooms',
    title: 'Kothrud',
    meta: 'Room in a 3-bed house',
    place: 'Paud Road, Pune',
    city: 'Pune',
    when: '20 Oct – open',
    price: '₹11,000',
    unit: 'month',
    rating: '4.88',
    badge: 'Verified flat',
    category: ['quiet', 'pets', 'verified'],
    photos: [
      photo('photo-1600566753086-00f18fb6b3ea'),
      photo('photo-1554995207-c18c203602cb'),
      photo('photo-1600210492486-724fe5c67fb0'),
    ],
    beds: '1 of 3 beds open',
    about: 'A ground-floor house with a small yard. One resident cat. Evenings are for reading, not speakers.',
    amenities: ['Cat in the house', 'Ground floor', 'Quiet house rule', 'Bills split in the app', 'Parking for one bike'],
    host: 'Neha',
    hostNote: 'Steady flatmate · 14 months here',
    reviews: [
      { name: 'Imran', date: 'April 2026', text: 'The cat is part of the deal and they said so. The house is actually quiet.' },
    ],
  },
  {
    id: 'hsr',
    kind: 'rooms',
    title: 'HSR Layout',
    meta: 'Room in a 3-bed flat',
    place: 'Sector 2, Bengaluru',
    city: 'Bengaluru',
    when: '5 Nov – open',
    price: '₹16,200',
    unit: 'month',
    rating: '4.69',
    category: ['sunny', 'campus'],
    photos: [
      photo('photo-1618221195710-dd6b41faaea6'),
      photo('photo-1600607687939-ce8a6c25118c'),
      photo('photo-1560448204-e02f11c3d0e2'),
    ],
    beds: '1 of 3 beds open',
    about: 'Sector 2, a ten-minute walk to the cafes. The flat keeps a shared grocery list and settles it every Sunday night.',
    amenities: ['Sunday settlement', 'Shared groceries', 'Two working professionals', 'Balcony', 'Geyser in the bathroom'],
    host: 'Vikram',
    hostNote: 'Replies within a day',
    reviews: [
      { name: 'Leela', date: 'March 2026', text: 'Grocery settlement on Sunday is the reason I stayed. No one is “about to pay.”' },
    ],
  },
  {
    id: 'powai',
    kind: 'rooms',
    title: 'Powai',
    meta: 'Room in a 2-bed flat',
    place: 'Hiranandani, Mumbai',
    city: 'Mumbai',
    when: '1 Dec – open',
    price: '₹27,500',
    unit: 'month',
    rating: '4.95',
    badge: 'Flat favorite',
    category: ['verified', 'quiet', 'sunny'],
    photos: [
      photo('photo-1600585154340-be6161a56a0c'),
      photo('photo-1493809842364-78817add7ffb'),
      photo('photo-1616594039964-ae9021a400a0'),
    ],
    beds: '1 of 2 beds open',
    about: 'A lake-side flat with one other person. Work-from-home days, a real desk, and a rule that calls happen in the bedroom.',
    amenities: ['Desk for work', 'Lake walk nearby', 'Verified flat', 'One flatmate', 'Inverter backup'],
    host: 'Aisha',
    hostNote: 'Steady flatmate · replies in an hour',
    reviews: [
      { name: 'Nikhil', date: 'September 2026', text: 'I visited on a Tuesday afternoon. It was as quiet as the listing said.' },
    ],
  },
  {
    id: 'meera',
    kind: 'people',
    title: 'Meera S.',
    meta: 'Looking in Bengaluru',
    place: 'Indiranagar or Koramangala',
    city: 'Bengaluru',
    when: 'Moving mid-October',
    price: '₹20,000',
    unit: 'budget',
    rating: '4.9',
    badge: 'Verified',
    category: ['quiet', 'campus', 'verified'],
    photos: [photo('photo-1494790108377-be9c29b29330')],
    beds: 'Product designer',
    about: 'Early sleeper, cooks four nights a week, and wants a flat where the chore list is written down. Happy to take garbage week first.',
    amenities: ['Asleep by 11', 'Cooks', 'Non-smoker', 'Works from the flat twice a week', 'Has references'],
    host: 'Meera',
    hostNote: 'Profile verified · looking since September',
    reviews: [
      { name: 'Old flat, JP Nagar', date: '2025', text: 'Meera took her weeks without being asked. Left the kitchen the way she found it.' },
    ],
  },
  {
    id: 'arjun',
    kind: 'people',
    title: 'Arjun P.',
    meta: 'Looking in Hyderabad',
    place: 'Madhapur or Gachibowli',
    city: 'Hyderabad',
    when: 'Moving 1 November',
    price: '₹15,000',
    unit: 'budget',
    rating: '4.7',
    category: ['campus', 'short'],
    photos: [photo('photo-1500648767791-00dcc994a43e')],
    beds: 'Software engineer',
    about: 'In the office four days. Fine with a short overlap while a current flatmate gives notice. Keeps weekends for family, not parties.',
    amenities: ['Office most days', 'No parties', 'Can overlap a month', 'Pays on the 1st', 'Has a bike'],
    host: 'Arjun',
    hostNote: 'Looking for a six-month start',
    reviews: [
      { name: 'Flat in Kondapur', date: '2026', text: 'Paid on the first. Quiet on calls. Took the swap when he travelled.' },
    ],
  },
  {
    id: 'leela',
    kind: 'people',
    title: 'Leela R.',
    meta: 'Looking in Pune',
    place: 'Kothrud or Karve Nagar',
    city: 'Pune',
    when: 'Moving late October',
    price: '₹12,000',
    unit: 'budget',
    rating: '4.86',
    badge: 'Verified',
    category: ['pets', 'quiet', 'verified'],
    photos: [photo('photo-1438761681033-6461ffad8d80')],
    beds: 'Graduate student',
    about: 'Brings a small dog on weekends only — the dog lives with her parents otherwise. Studies late, uses headphones, and likes a written bill split.',
    amenities: ['Dog on weekends', 'Headphones after 10', 'Student budget', 'Will share groceries', 'Non-smoker'],
    host: 'Leela',
    hostNote: 'Profile verified',
    reviews: [
      { name: 'Hostel, Pune', date: '2025', text: 'The dog thing was disclosed before anyone met. That was the whole point.' },
    ],
  },
  {
    id: 'imran',
    kind: 'people',
    title: 'Imran K.',
    meta: 'Looking in Mumbai',
    place: 'Andheri or Powai',
    city: 'Mumbai',
    when: 'Moving December',
    price: '₹25,000',
    unit: 'budget',
    rating: '4.78',
    category: ['sunny', 'verified'],
    photos: [photo('photo-1507003211169-0a1dd7228f2d')],
    beds: 'Architect',
    about: 'Wants light, a table big enough for drawings, and flatmates who settle money in the app instead of a notebook.',
    amenities: ['Needs a work table', 'Non-smoker', 'Cooks on Sundays', 'Travels one week a month', 'Verified'],
    host: 'Imran',
    hostNote: 'Can start 1 December',
    reviews: [
      { name: 'Studio share, Bandra', date: '2024', text: 'Left the table clear. Paid the drawing-lamp share without a reminder.' },
    ],
  },
]

const CATEGORIES: { id: Category; label: string; icon: typeof Sun }[] = [
  { id: 'sunny', label: 'Sunny rooms', icon: Sun },
  { id: 'campus', label: 'Near work', icon: GraduationCap },
  { id: 'quiet', label: 'Quiet flats', icon: Moon },
  { id: 'pets', label: 'Pets okay', icon: PawPrint },
  { id: 'short', label: 'Short stay', icon: Calendar },
  { id: 'verified', label: 'Verified', icon: BadgeCheck },
]

const CITIES = [
  ['Hyderabad', 'Shared rooms'],
  ['Bengaluru', 'Shared rooms'],
  ['Mumbai', 'Shared rooms'],
  ['Pune', 'Shared rooms'],
  ['Delhi', 'Coming soon'],
  ['Chennai', 'Coming soon'],
]

const WEEK = [
  ['Garbage', 'Kiran', 'Sun'],
  ['Mopping', 'You', 'Sun'],
  ['Dishes', 'Ananya', 'Daily'],
  ['Groceries', 'Shared', 'Sat'],
]

export default function LandingPage() {
  const router = useRouter()
  const user = useAuthStore((s) => s.user)
  const [mode, setMode] = useState<Mode>('rooms')
  const [category, setCategory] = useState<Category>('all')
  const [where, setWhere] = useState('')
  const [who, setWho] = useState(1)
  const [moveIn, setMoveIn] = useState<number | null>(null)
  const [segment, setSegment] = useState<'where' | 'when' | 'who' | null>(null)
  const [saved, setSaved] = useState<string[]>([])
  const [selected, setSelected] = useState<string | null>(null)
  const [photoIndex, setPhotoIndex] = useState<Record<string, number>>({})
  const [authOpen, setAuthOpen] = useState(false)
  const [menuOpen, setMenuOpen] = useState(false)
  const [searchOpen, setSearchOpen] = useState(false)

  useEffect(() => {
    if (user) router.push('/dashboard')
  }, [user, router])

  const visible = useMemo(() => {
    const q = where.trim().toLowerCase()
    return LISTINGS.filter((item) => {
      if (mode === 'flat') return false
      if (item.kind !== mode) return false
      if (category !== 'all' && !item.category.includes(category)) return false
      if (!q) return true
      return `${item.title} ${item.place} ${item.city} ${item.meta}`.toLowerCase().includes(q)
    })
  }, [mode, category, where])

  const active = LISTINGS.find((item) => item.id === selected) ?? null
  const moveLabel = moveIn ? `Oct ${moveIn}` : 'Add dates'

  function openListing(id: string) {
    setSelected(id)
    setSegment(null)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  return (
    <div
      className="min-h-[100dvh] bg-[#f6f3ef] text-[#222]"
      style={{ fontFamily: "var(--font-inter), Circular, -apple-system, system-ui, Roboto, 'Helvetica Neue', sans-serif" }}
    >
      <header className={styles.header}>
        <div className={styles.headerInner}>
          <Link href="/" className={styles.wordmark} aria-label="Habitiq home">
            habitiq<span>.</span>
          </Link>

          <nav className="mx-auto hidden items-center gap-8 md:flex" aria-label="Products">
            {(
              [
                ['rooms', 'Rooms'],
                ['people', 'Flatmates'],
                ['flat', 'Your flat'],
              ] as const
            ).map(([id]) => {
              const on = mode === id
              return (
                <button
                  key={id}
                  type="button"
                  onClick={() => {
                    setMode(id)
                    setSelected(null)
                    setCategory('all')
                  }}
                  className={styles.navLink}
                  style={{ color: on ? INK : MUTED }}
                >
                  {id === 'rooms' ? 'Find a home' : id === 'people' ? 'Find flatmates' : 'Run your flat'}
                  {on && (
                    <motion.span layoutId="active-navigation" className={styles.navUnderline} />
                  )}
                </button>
              )
            })}
          </nav>

          <div className="ml-auto flex items-center gap-2">
            <button
              type="button"
              onClick={() => setAuthOpen(true)}
              className={styles.listButton}
            >
              List your place
            </button>
            <button
              type="button"
              onClick={() => setAuthOpen(true)}
              className={styles.signInButton}
            >
              <UserRound size={16} strokeWidth={2} />
              Sign in
            </button>
            <button
              type="button"
              className="inline-flex h-10 w-10 items-center justify-center rounded-full border md:hidden"
              style={{ borderColor: HAIRLINE }}
              aria-label="Open menu"
              onClick={() => setMenuOpen(true)}
            >
              <Menu size={18} />
            </button>
          </div>
        </div>
      </header>

      <main>
        {active ? (
          <ListingDetail
            listing={active}
            saved={saved.includes(active.id)}
            onBack={() => setSelected(null)}
            onSave={() =>
              setSaved((prev) =>
                prev.includes(active.id) ? prev.filter((id) => id !== active.id) : [...prev, active.id],
              )
            }
            onContact={() => setAuthOpen(true)}
          />
        ) : (
          <>
            {mode !== 'flat' && (
              <>
                <LandingHero
                  onExplore={() => {
                    setMode('rooms')
                    setSelected(null)
                    document.getElementById('discover-results')?.scrollIntoView({ behavior: 'smooth' })
                  }}
                  onLearn={() => {
                    setMode('flat')
                    setSelected(null)
                    window.setTimeout(() => document.getElementById('how')?.scrollIntoView({ behavior: 'smooth' }), 0)
                  }}
                  onChoose={(nextMode) => {
                    setMode(nextMode)
                    setSelected(null)
                    if (nextMode !== 'flat') {
                      window.setTimeout(() => {
                        document.getElementById('discover-results')?.scrollIntoView({ behavior: 'smooth' })
                      }, 0)
                    }
                  }}
                />

                <div id="discover-results" className="mx-auto flex w-full max-w-[1280px] scroll-mt-24 flex-col items-center px-6 pt-10 lg:px-10">
                  <button
                    type="button"
                    className="flex h-12 w-full max-w-[850px] items-center gap-3 rounded-full border px-5 text-left md:hidden"
                    style={{ borderColor: HAIRLINE }}
                    onClick={() => setSearchOpen(true)}
                  >
                    <Search size={16} />
                    <span>
                      <span className="block text-[14px] font-medium">{where || 'Where to'}</span>
                      <span className="block text-[12px]" style={{ color: MUTED }}>
                        {moveLabel} · {who} {who === 1 ? 'person' : 'people'}
                      </span>
                    </span>
                  </button>

                  <SearchBar
                    className="hidden md:flex"
                    where={where}
                    setWhere={setWhere}
                    who={who}
                    setWho={setWho}
                    moveIn={moveIn}
                    setMoveIn={setMoveIn}
                    segment={segment}
                    setSegment={setSegment}
                    onSearch={() => setSegment(null)}
                  />
                </div>

                <div className="mx-auto mt-6 max-w-[1280px] px-6 lg:px-10">
                  <div className="flex gap-8 overflow-x-auto border-b pb-3" style={{ borderColor: '#ebebeb' }}>
                    <CategoryButton
                      label="All"
                      active={category === 'all'}
                      onClick={() => setCategory('all')}
                    />
                    {CATEGORIES.map((item) => (
                      <CategoryButton
                        key={item.id}
                        label={item.label}
                        icon={item.icon}
                        active={category === item.id}
                        onClick={() => setCategory(item.id)}
                      />
                    ))}
                  </div>
                </div>

                <section className="mx-auto max-w-[1280px] px-6 pt-8 pb-16 lg:px-10">
                  <div className="flex flex-col justify-between gap-3 md:flex-row md:items-end">
                    <div>
                      <p className="text-[12px] font-bold tracking-[0.12em] uppercase" style={{ color: RAUSCH }}>Explore the experience</p>
                      <h2 className="mt-2 text-[28px] leading-[1.2] font-bold tracking-[-0.035em]">
                        {mode === 'rooms' ? 'Homes that already know how to work' : 'Meet people who want the same kind of home'}
                      </h2>
                    </div>
                    <p className="max-w-[420px] text-[13px] leading-[1.45]" style={{ color: MUTED }}>
                      Illustrative listings for the product preview. Sign in to view current posts from Habitiq members.
                    </p>
                  </div>
                  <p className="mt-1 text-[16px]" style={{ color: BODY }}>
                    {visible.length === 0
                      ? 'Nothing matches that search. Try another city.'
                      : `${visible.length} ${mode === 'rooms' ? 'open beds' : 'profiles'} · ${where || 'India'}`}
                  </p>

                  {visible.length === 0 ? (
                    <EmptyState onClear={() => { setWhere(''); setCategory('all') }} />
                  ) : (
                    <div className="mt-6 grid grid-cols-1 gap-x-6 gap-y-10 sm:grid-cols-2 xl:grid-cols-4">
                      {visible.map((item) => (
                        <ListingCard
                          key={item.id}
                          listing={item}
                          photoIndex={photoIndex[item.id] ?? 0}
                          saved={saved.includes(item.id)}
                          onOpen={() => openListing(item.id)}
                          onSave={() =>
                            setSaved((prev) =>
                              prev.includes(item.id) ? prev.filter((id) => id !== item.id) : [...prev, item.id],
                            )
                          }
                          onPhoto={(dir) =>
                            setPhotoIndex((prev) => {
                              const current = prev[item.id] ?? 0
                              const next = (current + dir + item.photos.length) % item.photos.length
                              return { ...prev, [item.id]: next }
                            })
                          }
                        />
                      ))}
                    </div>
                  )}
                </section>
              </>
            )}

            {mode === 'flat' && <YourFlat onStart={() => setAuthOpen(true)} />}

            <section className="border-t" style={{ borderColor: '#ebebeb' }}>
              <div className="mx-auto max-w-[1280px] px-6 py-16 lg:px-10">
                <h2 className="text-[22px] leading-[1.18] font-medium tracking-[-0.44px]">
                  Inspiration for the next flat
                </h2>
                <div className="mt-6 grid grid-cols-2 gap-x-6 gap-y-8 md:grid-cols-3 lg:grid-cols-6">
                  {CITIES.map(([city, label]) => (
                    <button
                      key={city}
                      type="button"
                      className="text-left"
                      onClick={() => {
                        if (label === 'Coming soon') return
                        setMode('rooms')
                        setWhere(city)
                        setSelected(null)
                        window.scrollTo({ top: 0, behavior: 'smooth' })
                      }}
                    >
                      <span className="block text-[16px] font-semibold">{city}</span>
                      <span className="mt-0.5 block text-[14px]" style={{ color: MUTED }}>{label}</span>
                    </button>
                  ))}
                </div>
              </div>
            </section>
          </>
        )}
      </main>

      <footer className="border-t bg-white" style={{ borderColor: HAIRLINE }}>
        <div className="mx-auto grid max-w-[1280px] gap-10 px-6 py-12 sm:grid-cols-3 lg:px-20">
          {[
            ['Support', [['#how', 'How a flat runs'], ['mailto:hello@habitiq.app', 'Contact'], ['/privacy', 'Privacy']]],
            ['Hosting', [['#', 'List a room'], ['#', 'Find a flatmate'], ['#', 'House rules']]],
            ['Habitiq', [['/terms', 'Terms'], ['/privacy', 'Privacy'], ['mailto:hello@habitiq.app', 'hello@habitiq.app']]],
          ].map(([head, links]) => (
            <div key={head as string}>
              <p className="text-[16px] font-medium">{head as string}</p>
              <ul className="mt-4 space-y-3">
                {(links as [string, string][]).map(([href, label]) => (
                  <li key={label}>
                    <a href={href} className="text-[14px] underline-offset-2 hover:underline" style={{ color: INK }}>
                      {label}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
        <div className="border-t" style={{ borderColor: HAIRLINE }}>
          <div className="mx-auto flex max-w-[1280px] flex-wrap items-center justify-between gap-3 px-6 py-4 lg:px-20">
            <p className="text-[13px]" style={{ color: MUTED }}>© 2026 Habitiq</p>
            <p className="text-[13px]" style={{ color: MUTED }}>English (IN) · INR</p>
          </div>
        </div>
      </footer>

      <AnimatePresence>
        {searchOpen && (
          <motion.div
            className="fixed inset-0 z-50 bg-white md:hidden"
            initial={{ y: 24, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            exit={{ y: 24, opacity: 0 }}
          >
            <div className="flex items-center justify-between px-4 py-3">
              <button type="button" aria-label="Close search" onClick={() => setSearchOpen(false)} className="flex h-10 w-10 items-center justify-center rounded-full" style={{ background: STRONG }}>
                <X size={18} />
              </button>
              <p className="text-[16px] font-semibold">Search</p>
              <span className="w-10" />
            </div>
            <div className="px-4">
              <SearchBar
                stacked
                where={where}
                setWhere={setWhere}
                who={who}
                setWho={setWho}
                moveIn={moveIn}
                setMoveIn={setMoveIn}
                segment={segment ?? 'where'}
                setSegment={setSegment}
                onSearch={() => { setSearchOpen(false); setSegment(null) }}
              />
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      <AnimatePresence>
        {menuOpen && (
          <motion.div className="fixed inset-0 z-50 bg-black/50 md:hidden" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} onClick={() => setMenuOpen(false)}>
            <motion.div
              className="absolute inset-x-0 bottom-0 rounded-t-[32px] bg-white px-6 pt-4 pb-8"
              initial={{ y: 40 }}
              animate={{ y: 0 }}
              exit={{ y: 40 }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="mx-auto mb-4 h-1 w-10 rounded-full" style={{ background: HAIRLINE }} />
              {(
                [
                  ['rooms', 'Rooms'],
                  ['people', 'Flatmates'],
                  ['flat', 'Your flat'],
                ] as const
              ).map(([id, label]) => (
                <button
                  key={id}
                  type="button"
                  className="flex h-14 w-full items-center border-b text-left text-[16px] font-semibold"
                  style={{ borderColor: '#ebebeb' }}
                  onClick={() => { setMode(id); setSelected(null); setMenuOpen(false) }}
                >
                  {label}
                </button>
              ))}
              <button
                type="button"
                className="mt-4 h-12 w-full rounded-[8px] text-[16px] font-medium text-white"
                style={{ background: RAUSCH }}
                onClick={() => { setMenuOpen(false); setAuthOpen(true) }}
              >
                Continue
              </button>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      <AnimatePresence>
        {authOpen && (
          <motion.div
            className="fixed inset-0 z-[60] flex items-end justify-center bg-black/50 p-4 sm:items-center"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setAuthOpen(false)}
          >
            <motion.div
              className="w-full max-w-[400px] overflow-hidden rounded-[14px] bg-[#222] pt-3"
              style={{ boxShadow: ELEVATION }}
              initial={{ y: 16, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              exit={{ y: 16, opacity: 0 }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="flex justify-end px-3">
                <button
                  type="button"
                  aria-label="Close"
                  onClick={() => setAuthOpen(false)}
                  className="flex h-8 w-8 items-center justify-center rounded-full text-white"
                >
                  <X size={16} />
                </button>
              </div>
              <AuthForm inline onClose={() => setAuthOpen(false)} />
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}

function LandingHero({
  onExplore,
  onLearn,
  onChoose,
}: {
  onExplore: () => void
  onLearn: () => void
  onChoose: (mode: Mode) => void
}) {
  const choices: Array<{
    mode: Mode
    label: string
    detail: string
    icon: typeof Home
    tone: string
  }> = [
    { mode: 'rooms', label: 'Find a home', detail: 'Browse shared homes', icon: Home, tone: '#0f766e' },
    { mode: 'people', label: 'Find a flatmate', detail: 'Match on living habits', icon: UsersRound, tone: '#0f8b7b' },
    { mode: 'flat', label: 'Manage my flat', detail: 'Tasks and expenses', icon: CheckCircle2, tone: '#9a5b32' },
  ]

  return (
    <section className={styles.hero} aria-labelledby="hero-heading">
      <div className={styles.heroGlow} aria-hidden="true" />
      <div className={styles.heroGrid}>
        <motion.div
          className={styles.heroCopy}
          initial={false}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.55, ease: [0.22, 1, 0.36, 1] }}
        >
          <p className={styles.eyebrow}>
            <span className={styles.eyebrowIcon}><Home size={15} strokeWidth={2.2} /></span>
            Shared living, made human
          </p>
          <h1 id="hero-heading" className={styles.heroTitle}>
            Find your people.<br />
            <span>Run your home.</span>
          </h1>
          <p className={styles.heroText}>
            Discover a compatible shared home, then keep tasks and expenses fair in one calm place.
          </p>
          <div className={styles.heroActions}>
            <button type="button" className={styles.primaryCta} onClick={onExplore}>
              Explore homes <ArrowRight size={18} aria-hidden="true" />
            </button>
            <button type="button" className={styles.secondaryCta} onClick={onLearn}>
              How Habitiq works
            </button>
          </div>
          <div className={styles.promiseRow} aria-label="Product principles">
            <span><CheckCircle2 size={16} /> Clear responsibilities</span>
            <span><CheckCircle2 size={16} /> Fair shared costs</span>
          </div>
        </motion.div>

        <motion.div
          className={styles.heroVisual}
          initial={false}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          transition={{ duration: 0.7, delay: 0.08, ease: [0.22, 1, 0.36, 1] }}
        >
          <Image
            src={photo('photo-1600210492486-724fe5c67fb0')}
            alt="A warm, sunlit shared living room"
            fill
            priority
            sizes="(max-width: 900px) 92vw, 54vw"
            className={styles.heroImage}
          />
          <div className={styles.imageShade} aria-hidden="true" />
          <div className={styles.previewLabel}>Product preview</div>
          <motion.div
            className={`${styles.floatCard} ${styles.taskCard}`}
            animate={{ y: [0, -5, 0] }}
            transition={{ duration: 5, repeat: Infinity, ease: 'easeInOut' }}
          >
            <span className={styles.cardIcon}><CheckCircle2 size={20} /></span>
            <span><small>Kitchen</small><strong>Your turn today</strong></span>
            <span className={styles.doneDot}>Done</span>
          </motion.div>
          <motion.div
            className={`${styles.floatCard} ${styles.expenseCard}`}
            animate={{ y: [0, 5, 0] }}
            transition={{ duration: 5.8, repeat: Infinity, ease: 'easeInOut' }}
          >
            <span className={`${styles.cardIcon} ${styles.expenseIcon}`}><ReceiptIndianRupee size={20} /></span>
            <span><small>Groceries</small><strong>Split equally</strong></span>
            <strong className={styles.amount}>₹1,240</strong>
          </motion.div>
          <div className={styles.visualNote}>
            <span className={styles.avatarStack} aria-hidden="true"><i>A</i><i>K</i><i>R</i></span>
            <span><strong>One home, one shared rhythm</strong><small>Tasks, costs, and people together</small></span>
          </div>
        </motion.div>
      </div>

      <motion.div
        className={styles.choiceBar}
        initial={false}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.55, delay: 0.22, ease: [0.22, 1, 0.36, 1] }}
        aria-label="Choose your Habitiq journey"
      >
        {choices.map(({ mode, label, detail, icon: Icon, tone }) => (
          <button key={mode} type="button" className={styles.choice} onClick={() => onChoose(mode)}>
            <span className={styles.choiceIcon} style={{ color: tone, background: `${tone}12` }}><Icon size={22} /></span>
            <span><strong>{label}</strong><small>{detail}</small></span>
            <ArrowRight size={18} className={styles.choiceArrow} aria-hidden="true" />
          </button>
        ))}
      </motion.div>
    </section>
  )
}

function CategoryButton({
  label,
  icon: Icon,
  active,
  onClick,
}: {
  label: string
  icon?: typeof Sun
  active: boolean
  onClick: () => void
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="flex shrink-0 flex-col items-center gap-2 pb-2 text-[14px] font-medium"
      style={{
        color: active ? INK : MUTED,
        borderBottom: active ? `2px solid ${INK}` : '2px solid transparent',
        opacity: active ? 1 : 0.72,
      }}
    >
      {Icon ? <Icon size={24} strokeWidth={1.5} /> : <span className="h-6" />}
      {label}
    </button>
  )
}

function SearchBar({
  where,
  setWhere,
  who,
  setWho,
  moveIn,
  setMoveIn,
  segment,
  setSegment,
  onSearch,
  className = '',
  stacked = false,
}: {
  where: string
  setWhere: (v: string) => void
  who: number
  setWho: (v: number) => void
  moveIn: number | null
  setMoveIn: (v: number | null) => void
  segment: 'where' | 'when' | 'who' | null
  setSegment: (v: 'where' | 'when' | 'who' | null) => void
  onSearch: () => void
  className?: string
  stacked?: boolean
}) {
  const days = Array.from({ length: 31 }, (_, i) => i + 1)
  return (
    <div className={`relative w-full max-w-[850px] ${className}`}>
      <div
        className={stacked ? 'flex flex-col gap-3' : 'flex h-16 items-center rounded-full border bg-white pr-2 pl-2'}
        style={stacked ? undefined : { borderColor: HAIRLINE, boxShadow: ELEVATION }}
      >
        <Segment
          stacked={stacked}
          label="Where"
          active={segment === 'where'}
          onClick={() => setSegment(segment === 'where' ? null : 'where')}
          value={where || 'Search cities'}
        />
        {!stacked && <span className="h-8 w-px" style={{ background: HAIRLINE }} />}
        <Segment
          stacked={stacked}
          label="Move-in"
          active={segment === 'when'}
          onClick={() => setSegment(segment === 'when' ? null : 'when')}
          value={moveIn ? `October ${moveIn}` : 'Add dates'}
        />
        {!stacked && <span className="h-8 w-px" style={{ background: HAIRLINE }} />}
        <Segment
          stacked={stacked}
          label="Who"
          active={segment === 'who'}
          onClick={() => setSegment(segment === 'who' ? null : 'who')}
          value={`${who} ${who === 1 ? 'person' : 'people'}`}
        />
        <button
          type="button"
          aria-label="Search"
          onClick={onSearch}
          className={stacked ? 'mt-2 flex h-12 w-full items-center justify-center gap-2 rounded-full text-[16px] font-medium text-white' : 'ml-2 flex h-12 w-12 shrink-0 items-center justify-center rounded-full text-white'}
          style={{ background: RAUSCH }}
          onMouseDown={(e) => { e.currentTarget.style.background = RAUSCH_ACTIVE }}
          onMouseUp={(e) => { e.currentTarget.style.background = RAUSCH }}
        >
          <Search size={18} strokeWidth={2.5} />
          {stacked && 'Search'}
        </button>
      </div>

      <AnimatePresence>
        {segment === 'where' && (
          <Popover>
            <label className="text-[14px] font-medium" style={{ color: MUTED }} htmlFor="where-input">Where</label>
            <input
              id="where-input"
              autoFocus
              value={where}
              onChange={(e) => setWhere(e.target.value)}
              placeholder="Hyderabad, Bengaluru, Mumbai…"
              className="mt-2 h-14 w-full rounded-[8px] border px-3 text-[16px] outline-none"
              style={{ borderColor: HAIRLINE, color: INK }}
              onFocus={(e) => { e.currentTarget.style.borderColor = INK; e.currentTarget.style.borderWidth = '2px' }}
              onBlur={(e) => { e.currentTarget.style.borderColor = HAIRLINE; e.currentTarget.style.borderWidth = '1px' }}
            />
            <div className="mt-4 grid grid-cols-2 gap-2">
              {['Hyderabad', 'Bengaluru', 'Mumbai', 'Pune'].map((city) => (
                <button
                  key={city}
                  type="button"
                  className="rounded-[8px] px-3 py-3 text-left text-[14px] font-medium hover:bg-[#f7f7f7]"
                  onClick={() => { setWhere(city); setSegment('when') }}
                >
                  {city}
                </button>
              ))}
            </div>
          </Popover>
        )}
        {segment === 'when' && (
          <Popover>
            <p className="text-[16px] font-semibold">October 2026</p>
            <div className="mt-4 grid grid-cols-7 gap-1 text-center text-[12px]" style={{ color: MUTED }}>
              {['S', 'M', 'T', 'W', 'T', 'F', 'S'].map((d, i) => <span key={`${d}${i}`}>{d}</span>)}
            </div>
            <div className="mt-2 grid grid-cols-7 gap-1">
              {Array.from({ length: 4 }).map((_, i) => <span key={i} />)}
              {days.map((day) => {
                const on = moveIn === day
                return (
                  <button
                    key={day}
                    type="button"
                    onClick={() => setMoveIn(day)}
                    className="flex h-10 w-10 items-center justify-center rounded-full text-[14px]"
                    style={{ background: on ? INK : 'transparent', color: on ? '#fff' : INK }}
                  >
                    {day}
                  </button>
                )
              })}
            </div>
            <button type="button" className="mt-3 text-[14px] underline" onClick={() => setMoveIn(null)}>Clear dates</button>
          </Popover>
        )}
        {segment === 'who' && (
          <Popover>
            <div className="flex items-center justify-between gap-6">
              <div>
                <p className="text-[16px] font-medium">People</p>
                <p className="text-[14px]" style={{ color: MUTED }}>How many will share the flat</p>
              </div>
              <div className="flex items-center gap-3">
                <RoundStep label="Fewer people" disabled={who <= 1} onClick={() => setWho(Math.max(1, who - 1))} icon="minus" />
                <span className="w-4 text-center text-[16px]">{who}</span>
                <RoundStep label="More people" disabled={who >= 6} onClick={() => setWho(Math.min(6, who + 1))} icon="plus" />
              </div>
            </div>
          </Popover>
        )}
      </AnimatePresence>
    </div>
  )
}

function Segment({
  label,
  value,
  active,
  onClick,
  stacked,
}: {
  label: string
  value: string
  active: boolean
  onClick: () => void
  stacked: boolean
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={stacked ? 'w-full rounded-[14px] border px-4 py-3 text-left' : 'min-w-0 flex-1 rounded-full px-6 py-2 text-left'}
      style={{
        background: active ? '#fff' : 'transparent',
        boxShadow: active && !stacked ? ELEVATION : undefined,
        borderColor: stacked ? HAIRLINE : undefined,
      }}
    >
      <span className="block text-[12px] font-bold">{label}</span>
      <span className="block truncate text-[14px]" style={{ color: value.startsWith('Search') || value.startsWith('Add') ? MUTED : INK }}>
        {value}
      </span>
    </button>
  )
}

function Popover({ children }: { children: ReactNode }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: 8 }}
      className="absolute top-[72px] left-0 z-30 w-full rounded-[32px] bg-white p-6 md:w-[380px]"
      style={{ boxShadow: ELEVATION }}
    >
      {children}
    </motion.div>
  )
}

function RoundStep({
  onClick,
  disabled,
  label,
  icon,
}: {
  onClick: () => void
  disabled: boolean
  label: string
  icon: 'plus' | 'minus'
}) {
  return (
    <button
      type="button"
      aria-label={label}
      disabled={disabled}
      onClick={onClick}
      className="flex h-8 w-8 items-center justify-center rounded-full border disabled:opacity-30"
      style={{ borderColor: '#c1c1c1' }}
    >
      {icon === 'plus' ? <Plus size={14} /> : <Minus size={14} />}
    </button>
  )
}

function ListingCard({
  listing,
  photoIndex,
  saved,
  onOpen,
  onSave,
  onPhoto,
}: {
  listing: Listing
  photoIndex: number
  saved: boolean
  onOpen: () => void
  onSave: () => void
  onPhoto: (dir: number) => void
}) {
  const [hover, setHover] = useState(false)
  const src = listing.photos[photoIndex] ?? listing.photos[0]
  return (
    <article
      onMouseEnter={() => setHover(true)}
      onMouseLeave={() => setHover(false)}
    >
      <div className="relative">
        <button type="button" onClick={onOpen} className="block w-full text-left">
          <Image
            src={src}
            alt={`${listing.title}, ${listing.place}`}
            width={800}
            height={800}
            className="aspect-square w-full object-cover"
            style={{ borderRadius: 14 }}
          />
        </button>
        <button
          type="button"
          aria-label={saved ? 'Remove from saved' : 'Save'}
          aria-pressed={saved}
          onClick={onSave}
          className="absolute top-3 right-3 flex h-8 w-8 items-center justify-center"
          style={{ filter: 'drop-shadow(0 1px 2px rgba(0,0,0,0.45))' }}
        >
          <Heart size={22} strokeWidth={2} fill={saved ? RAUSCH : 'rgba(0,0,0,0.5)'} color="#fff" />
        </button>
        {hover && listing.photos.length > 1 && (
          <>
            <button type="button" aria-label="Previous photo" onClick={() => onPhoto(-1)} className="absolute top-1/2 left-3 flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-full bg-white" style={{ boxShadow: ELEVATION }}>
              <ChevronLeft size={16} />
            </button>
            <button type="button" aria-label="Next photo" onClick={() => onPhoto(1)} className="absolute top-1/2 right-3 flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-full bg-white" style={{ boxShadow: ELEVATION }}>
              <ChevronRight size={16} />
            </button>
          </>
        )}
        {listing.photos.length > 1 && (
          <div className="absolute bottom-3 left-0 flex w-full justify-center gap-1">
            {listing.photos.map((_, i) => (
              <span key={i} className="h-1.5 w-1.5 rounded-full" style={{ background: i === photoIndex ? '#fff' : 'rgba(255,255,255,0.55)' }} />
            ))}
          </div>
        )}
      </div>
      <button type="button" onClick={onOpen} className="mt-3 block w-full text-left">
        <div className="flex items-start justify-between gap-3">
          <p className="text-[15px] font-semibold leading-[1.25]">{listing.title}</p>
        </div>
        <p className="mt-0.5 text-[14px]" style={{ color: MUTED }}>{listing.meta}</p>
        <p className="text-[14px]" style={{ color: MUTED }}>{listing.when}</p>
        <p className="mt-1 text-[14px]">
          <span className="font-semibold">{listing.price}</span> {listing.unit}
        </p>
      </button>
    </article>
  )
}

function EmptyState({ onClear }: { onClear: () => void }) {
  return (
    <div className="mt-10 max-w-md rounded-[14px] border p-6" style={{ borderColor: HAIRLINE }}>
      <p className="text-[16px] font-semibold">No flats in that search</p>
      <p className="mt-2 text-[14px] leading-[1.43]" style={{ color: BODY }}>
        Try Hyderabad, Bengaluru, Mumbai, or Pune, or clear the filter.
      </p>
      <button type="button" onClick={onClear} className="mt-4 h-12 rounded-[8px] border px-6 text-[16px] font-medium" style={{ borderColor: INK }}>
        Clear search
      </button>
    </div>
  )
}

function ListingDetail({
  listing,
  saved,
  onBack,
  onSave,
  onContact,
}: {
  listing: Listing
  saved: boolean
  onBack: () => void
  onSave: () => void
  onContact: () => void
}) {
  return (
    <div className="mx-auto max-w-[1080px] px-6 py-6 lg:px-8">
      <button type="button" onClick={onBack} className="mb-4 flex h-8 w-8 items-center justify-center rounded-full" style={{ background: STRONG }} aria-label="Back">
        <ChevronLeft size={18} />
      </button>
      <div className="grid grid-cols-1 gap-2 overflow-hidden md:grid-cols-2" style={{ borderRadius: 14 }}>
        <Image src={listing.photos[0]} alt="" width={1200} height={900} className="aspect-[4/3] h-full w-full object-cover md:aspect-auto md:min-h-[420px]" />
        <div className="hidden grid-cols-1 gap-2 md:grid">
          {(listing.photos[1] ? [listing.photos[1], listing.photos[2] ?? listing.photos[0]] : []).map((src) => (
            <Image key={src} src={src} alt="" width={1200} height={800} className="h-full max-h-[206px] w-full object-cover" />
          ))}
        </div>
      </div>

      <div className="mt-8 grid items-start gap-12 lg:grid-cols-[1.7fr_1fr]">
        <div>
          <div className="flex items-start justify-between gap-4">
            <div>
              <h1 className="text-[22px] leading-[1.18] font-medium tracking-[-0.44px]">
                {listing.meta} in {listing.title}
              </h1>
              <p className="mt-1 text-[16px]" style={{ color: BODY }}>{listing.place} · {listing.beds}</p>
            </div>
            <button type="button" onClick={onSave} className="flex items-center gap-2 text-[14px] underline" aria-pressed={saved}>
              <Heart size={16} fill={saved ? RAUSCH : 'none'} color={saved ? RAUSCH : INK} />
              {saved ? 'Saved' : 'Save'}
            </button>
          </div>

          <p className="mt-8 max-w-[62ch] text-[16px] leading-[1.5]" style={{ color: BODY }}>{listing.about}</p>

          <h2 className="mt-10 text-[21px] font-bold">What this place offers</h2>
          <ul className="mt-2 border-t" style={{ borderColor: '#ebebeb' }}>
            {listing.amenities.map((row) => (
              <li key={row} className="border-b py-3 text-[16px]" style={{ borderColor: '#ebebeb' }}>{row}</li>
            ))}
          </ul>

          <div className="mt-10 rounded-[14px] border p-6" style={{ borderColor: HAIRLINE }}>
            <p className="text-[16px] font-semibold">{listing.host}</p>
            <p className="mt-1 text-[14px]" style={{ color: MUTED }}>{listing.hostNote}</p>
            <button
              type="button"
              onClick={onContact}
              className="mt-4 h-12 rounded-[8px] border px-6 text-[16px] font-medium"
              style={{ borderColor: INK }}
            >
              Contact
            </button>
          </div>
        </div>

        <aside className="lg:sticky lg:top-24">
          <div className="rounded-[14px] border bg-white p-6" style={{ borderColor: HAIRLINE, boxShadow: ELEVATION }}>
            <p className="text-[21px] font-bold">
              {listing.price} <span className="text-[16px] font-normal" style={{ color: BODY }}>{listing.unit}</span>
            </p>
            <div className="mt-4 overflow-hidden rounded-[8px] border" style={{ borderColor: HAIRLINE }}>
              <div className="grid grid-cols-2">
                <div className="border-r px-3 py-3" style={{ borderColor: HAIRLINE }}>
                  <p className="text-[10px] font-bold tracking-[0.3px]">MOVE-IN</p>
                  <p className="text-[14px]">{listing.when}</p>
                </div>
                <div className="px-3 py-3">
                  <p className="text-[10px] font-bold tracking-[0.3px]">FLAT</p>
                  <p className="text-[14px]">{listing.beds}</p>
                </div>
              </div>
            </div>
            <button
              type="button"
              onClick={onContact}
              className="mt-4 h-12 w-full rounded-[8px] text-[16px] font-medium text-white"
              style={{ background: RAUSCH }}
              onMouseDown={(e) => { e.currentTarget.style.background = RAUSCH_ACTIVE }}
              onMouseUp={(e) => { e.currentTarget.style.background = RAUSCH }}
            >
              Contact about this listing
            </button>
            <p className="mt-3 text-center text-[14px]" style={{ color: MUTED }}>Sign in to send a connection request.</p>
            <div className="mt-4 space-y-3 border-t pt-4 text-[14px]" style={{ borderColor: '#ebebeb' }}>
              <div className="flex justify-between"><span>Rent share</span><span>{listing.price}</span></div>
              <div className="flex justify-between"><span>Habitiq</span><span>Free</span></div>
              <div className="flex justify-between border-t pt-3 font-semibold" style={{ borderColor: HAIRLINE }}>
                <span>Due monthly</span><span>{listing.price}</span>
              </div>
            </div>
          </div>
        </aside>
      </div>

      <div className="fixed inset-x-0 bottom-0 z-30 flex items-center justify-between border-t bg-white px-4 py-3 lg:hidden" style={{ borderColor: HAIRLINE }}>
        <div>
          <p className="text-[16px] font-semibold">{listing.price} {listing.unit}</p>
          <p className="text-[13px]" style={{ color: MUTED }}>{listing.when}</p>
        </div>
        <button type="button" onClick={onContact} className="h-12 rounded-[8px] px-6 text-[16px] font-medium text-white" style={{ background: RAUSCH }}>
          Contact
        </button>
      </div>
    </div>
  )
}

function YourFlat({ onStart }: { onStart: () => void }) {
  return (
    <section id="how" className="mx-auto grid max-w-[1080px] items-start gap-12 px-6 py-16 lg:grid-cols-[1.4fr_0.9fr] lg:px-8">
      <div>
        <p className="text-[12px] font-bold tracking-[0.32px] uppercase" style={{ color: MUTED }}>Your flat</p>
        <h1 className="mt-2 max-w-[18ch] text-[28px] leading-[1.43] font-bold">
          The flat runs before anyone argues about whose turn it is.
        </h1>
        <p className="mt-4 max-w-[58ch] text-[16px] leading-[1.5]" style={{ color: BODY }}>
          Habitiq is the board on the fridge, except it rotates, swaps, and settles the bill. Rooms and flatmates live on the same page as the week ahead.
        </p>
        <ul className="mt-8 border-t" style={{ borderColor: '#ebebeb' }}>
          {[
            ['Duties rotate', 'Garbage, mopping, dishes. The next name is already written.'],
            ['Bills settle', 'Rent, wifi, groceries. One number per person, not a notebook.'],
            ['Swaps are requests', 'Going away does not dump the week on whoever answers the chat.'],
          ].map(([title, body]) => (
            <li key={title} className="border-b py-4" style={{ borderColor: '#ebebeb' }}>
              <p className="text-[16px] font-semibold">{title}</p>
              <p className="mt-1 text-[16px] leading-[1.5]" style={{ color: BODY }}>{body}</p>
            </li>
          ))}
        </ul>
      </div>
      <aside className="rounded-[14px] border bg-white p-6 lg:sticky lg:top-24" style={{ borderColor: HAIRLINE, boxShadow: ELEVATION }}>
        <p className="text-[21px] font-bold">This week</p>
        <p className="mt-1 text-[14px]" style={{ color: MUTED }}>A 3-person flat · Hyderabad</p>
        <ul className="mt-4">
          {WEEK.map(([task, name, when]) => (
            <li key={task} className="flex items-center justify-between border-b py-3 text-[16px]" style={{ borderColor: '#ebebeb' }}>
              <span>
                <span className="block font-medium">{task}</span>
                <span className="text-[14px]" style={{ color: MUTED }}>{when}</span>
              </span>
              <span>{name}</span>
            </li>
          ))}
        </ul>
        <button
          type="button"
          onClick={onStart}
          className="mt-5 h-12 w-full rounded-[8px] text-[16px] font-medium text-white"
          style={{ background: RAUSCH }}
          onMouseDown={(e) => { e.currentTarget.style.background = RAUSCH_ACTIVE }}
          onMouseUp={(e) => { e.currentTarget.style.background = RAUSCH }}
        >
          Continue
        </button>
        <p className="mt-3 text-center text-[14px]" style={{ color: MUTED }}>Free while the flat is in trial.</p>
      </aside>
    </section>
  )
}
