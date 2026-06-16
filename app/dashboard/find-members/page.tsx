"use client"
import { useState, useEffect, useRef, useMemo } from 'react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  UserPlus, Copy, Check, Link2, MapPin, BedDouble, Users,
  Pencil, Save, X, Bell, CheckCircle2, ChevronDown, ChevronUp,
  Eye, EyeOff, Hash, Search, Home, Smile, Dumbbell, Coffee,
  Banknote, Laptop, Clock, Sparkles, UserCheck, ShieldOff,
  Leaf, Cigarette, Wine, PawPrint, GraduationCap, Briefcase,
  Flame, Sun, Moon, TrendingUp, Building2, ScanSearch, ArrowRight,
  Loader2, CalendarClock, MessageSquare, Star,
} from 'lucide-react'
import type {
  VacancyListing, LifestyleTag, SeekerProfile,
  FlatSnapshot, EnrichedJoinRequest, MoveInTiming,
} from '@/lib/discoveryTypes'
import { fetchDiscoveryTags, ensureDiscoveryTag, slugifyTag } from '@/lib/discoveryTagService'
import type { DiscoveryTag } from '@/lib/discoveryTypes'
import {
  fetchSeekerProfile, saveSeekerProfile,
  fetchActiveFlats, raiseJoinRequest,
} from '@/lib/seekerService'

// ── Lifestyle tag config ──────────────────────────────────────────────────────
const TAG_CATEGORIES: {
  label: string
  tags: { id: LifestyleTag; label: string; icon: React.ReactNode }[]
}[] = [
  {
    label: 'Work & Schedule',
    tags: [
      { id: 'it_professional', label: 'IT / Tech',      icon: <Laptop size={11} /> },
      { id: 'finance',         label: 'Finance',         icon: <Banknote size={11} /> },
      { id: 'student',         label: 'Student',         icon: <GraduationCap size={11} /> },
      { id: 'freelancer',      label: 'Freelancer',      icon: <Briefcase size={11} /> },
      { id: 'wfh',             label: 'Works from home', icon: <Home size={11} /> },
      { id: 'night_shift',     label: 'Night shifts',    icon: <Moon size={11} /> },
      { id: 'nine_to_five',    label: '9 to 5',          icon: <Clock size={11} /> },
    ],
  },
  {
    label: 'Social & Personality',
    tags: [
      { id: 'early_bird',    label: 'Early riser',  icon: <Sun size={11} /> },
      { id: 'night_owl',     label: 'Night owl',    icon: <Moon size={11} /> },
      { id: 'homebody',      label: 'Homebody',     icon: <Home size={11} /> },
      { id: 'social',        label: 'Social',       icon: <Smile size={11} /> },
      { id: 'fitness_freak', label: 'Fitness freak',icon: <Dumbbell size={11} /> },
    ],
  },
  {
    label: 'Diet & Habits',
    tags: [
      { id: 'vegetarian', label: 'Vegetarian', icon: <Leaf size={11} /> },
      { id: 'jain',       label: 'Jain',       icon: <Coffee size={11} /> },
      { id: 'non_veg',    label: 'Non-veg OK', icon: <Flame size={11} /> },
      { id: 'no_smoking', label: 'No smoking', icon: <Cigarette size={11} /> },
      { id: 'no_drinking',label: 'No drinking',icon: <Wine size={11} /> },
    ],
  },
  {
    label: 'Flat Vibe',
    tags: [
      { id: 'neat_freak',     label: 'Neat & tidy',   icon: <Sparkles size={11} /> },
      { id: 'guest_friendly', label: 'Guest-friendly', icon: <UserCheck size={11} /> },
      { id: 'minimal_guests', label: 'Minimal guests', icon: <ShieldOff size={11} /> },
      { id: 'pets_ok',        label: 'Pets OK',        icon: <PawPrint size={11} /> },
      { id: 'no_pets',        label: 'No pets',        icon: <ShieldOff size={11} /> },
    ],
  },
]
const ALL_LIFESTYLE_TAGS = TAG_CATEGORIES.flatMap(c => c.tags)

const POPULAR_HASHTAGS = [
  'TCS', 'Infosys', 'Wipro', 'Cognizant', 'Accenture', 'HCL', 'Tech Mahindra',
  'Amazon', 'Google', 'Microsoft', 'Deloitte', 'IBM', 'Capgemini',
  'Hitech City', 'Electronic City', 'Whitefield', 'Gachibowli', 'Cyber City',
  'Hinjewadi', 'Manyata Tech Park', 'Koramangala', 'HSR Layout', 'Sarjapur Road',
]

const CURRENCIES = ['INR', 'USD', 'EUR', 'GBP', 'AED', 'SGD', 'AUD']
const CURRENCY_SYMBOLS: Record<string, string> = {
  INR: '₹', USD: '$', EUR: '€', GBP: '£', AED: 'د.إ', SGD: 'S$', AUD: 'A$',
}

// Darker, richer gradients for flat card headers (Airbnb dark photo feel)
const FLAT_GRADIENTS = [
  ['from-slate-700 to-slate-900',   'bg-slate-800'],
  ['from-blue-700 to-indigo-900',   'bg-blue-800'],
  ['from-emerald-700 to-teal-900',  'bg-emerald-800'],
  ['from-violet-700 to-purple-900', 'bg-violet-800'],
  ['from-orange-600 to-red-800',    'bg-orange-700'],
  ['from-pink-600 to-rose-800',     'bg-pink-700'],
  ['from-cyan-700 to-blue-900',     'bg-cyan-800'],
  ['from-amber-600 to-orange-800',  'bg-amber-700'],
]

// Lighter, warm gradients for person cards
const PERSON_GRADIENTS = [
  'from-violet-500 to-purple-700',
  'from-blue-500 to-cyan-600',
  'from-emerald-500 to-teal-700',
  'from-orange-500 to-amber-600',
  'from-pink-500 to-rose-600',
  'from-indigo-500 to-blue-600',
  'from-red-400 to-orange-600',
  'from-teal-500 to-green-600',
]

function flatGradient(name: string) {
  const n = name.split('').reduce((a, c) => a + c.charCodeAt(0), 0)
  return FLAT_GRADIENTS[n % FLAT_GRADIENTS.length]
}
function personGradient(uid: string) {
  const n = uid.split('').reduce((a, c) => a + c.charCodeAt(0), 0)
  return PERSON_GRADIENTS[n % PERSON_GRADIENTS.length]
}

const DEFAULT_VACANCY: VacancyListing = {
  active: false, bedsAvailable: 1, rentPerHead: null,
  currency: 'INR', city: '', area: '',
  existingMembersGender: null, preferredGender: 'any',
  lifestyle: [], customTags: [], about: '', updatedAt: '',
}

type SeekerDraft = {
  city: string; area: string; lifestyle: LifestyleTag[]; customTags: string[]; about: string; active: boolean
}
const DEFAULT_SEEKER: SeekerDraft = {
  city: '', area: '', lifestyle: [], customTags: [], about: '', active: false,
}

function listingScore(v: VacancyListing) {
  const checks = [
    { label: 'City',           done: !!v.city.trim() },
    { label: 'Area',           done: !!v.area.trim() },
    { label: 'Rent per head',  done: !!v.rentPerHead },
    { label: 'Lifestyle tags', done: v.lifestyle.length > 0 },
    { label: 'Hashtags',       done: (v.customTags ?? []).length > 0 },
    { label: 'About the flat', done: !!v.about.trim() },
  ]
  const done = checks.filter(c => c.done).length
  return { score: Math.round((done / checks.length) * 100), missing: checks.filter(c => !c.done).map(c => c.label) }
}

const GENDER_LABEL: Record<string, string> = { all_male: 'All male', all_female: 'All female', mixed: 'Mixed' }

const MOVE_IN_OPTIONS: { val: MoveInTiming; label: string; desc: string }[] = [
  { val: 'asap',       label: 'ASAP',       desc: 'Ready to move immediately' },
  { val: 'this_month', label: 'This month',  desc: 'Within the next 4 weeks' },
  { val: 'next_month', label: 'Next month',  desc: '1–2 months from now' },
  { val: 'flexible',   label: 'Flexible',   desc: "I'm exploring options" },
]

// ── Section divider ───────────────────────────────────────────────────────────
function SectionLabel({ label }: { label: string }) {
  return (
    <div className="flex items-center gap-3 pt-1">
      <span className="text-[10px] font-extrabold text-muted-foreground uppercase tracking-widest whitespace-nowrap">{label}</span>
      <div className="flex-1 h-px bg-border" />
    </div>
  )
}

// ── Hashtag input ─────────────────────────────────────────────────────────────
function HashtagInput({ selected, onChange, uid }: {
  selected: string[]
  onChange: (tags: string[]) => void
  uid: string
}) {
  const [query, setQuery] = useState('')
  const [globalTags, setGlobalTags] = useState<DiscoveryTag[]>([])
  const [focused, setFocused] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)

  useEffect(() => { fetchDiscoveryTags().then(setGlobalTags) }, [])

  const allKnown = Array.from(
    new Map(
      [...POPULAR_HASHTAGS.map(l => ({ id: slugifyTag(l), label: l })), ...globalTags]
        .map(t => [t.id, t])
    ).values()
  )
  const q = query.trim().replace(/^#/, '')
  const selectedSlugs = selected.map(s => slugifyTag(s))
  const suggestions = q.length > 0
    ? allKnown.filter(t => t.label.toLowerCase().includes(q.toLowerCase()) && !selectedSlugs.includes(t.id))
    : allKnown.filter(t => !selectedSlugs.includes(t.id)).slice(0, 18)
  const exactSlug = slugifyTag(q)
  const isNew = q.length > 1 && !allKnown.some(t => t.id === exactSlug) && !selectedSlugs.includes(exactSlug)
  const dropdownItems = isNew ? [...suggestions, { id: exactSlug, label: q, isNew: true }] : suggestions

  const addTag = async (label: string) => {
    if (selected.map(s => s.toLowerCase()).includes(label.toLowerCase())) return
    ensureDiscoveryTag(label, uid).then(() => fetchDiscoveryTags().then(setGlobalTags))
    onChange([...selected, label])
    setQuery('')
    inputRef.current?.focus()
  }
  const removeTag = (label: string) => onChange(selected.filter(s => s !== label))

  return (
    <div className="space-y-3">
      {selected.length > 0 && (
        <div className="flex flex-wrap gap-1.5">
          {selected.map(tag => (
            <span key={tag} className="flex items-center gap-1 px-2.5 py-1 bg-primary text-white rounded-full text-xs font-semibold">
              <Hash size={10} />{tag}
              <button onClick={() => removeTag(tag)} className="ml-0.5 hover:opacity-75 cursor-pointer transition-opacity"><X size={10} /></button>
            </span>
          ))}
        </div>
      )}
      <div className="relative">
        <Search size={13} className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground pointer-events-none" />
        <input ref={inputRef} value={query}
          onChange={e => setQuery(e.target.value)}
          onFocus={() => setFocused(true)}
          onBlur={() => setTimeout(() => setFocused(false), 150)}
          onKeyDown={e => { if (e.key === 'Enter' && q.length > 1) { e.preventDefault(); addTag(dropdownItems[0]?.label ?? q) } }}
          placeholder="Search or type a tag… e.g. TCS, Hitech City"
          className="w-full h-9 pl-8 pr-3 rounded-xl border border-border bg-background text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
        {focused && dropdownItems.length > 0 && (
          <div className="absolute top-full left-0 right-0 mt-1 bg-card border border-border rounded-xl shadow-lg z-20 max-h-48 overflow-y-auto">
            {dropdownItems.map(t => (
              <button key={t.id} onMouseDown={() => addTag(t.label)}
                className="w-full flex items-center gap-2 px-3 py-2 text-sm hover:bg-secondary/60 transition-colors text-left cursor-pointer">
                <Hash size={11} className="text-muted-foreground shrink-0" />
                <span className="font-medium flex-1">{t.label}</span>
                {'isNew' in t && t.isNew && <span className="text-[10px] font-semibold text-muted-foreground/60 shrink-0">new</span>}
              </button>
            ))}
          </div>
        )}
      </div>
      {!focused && selected.length < 10 && (
        <div className="flex flex-wrap gap-1.5">
          {POPULAR_HASHTAGS.filter(t => !selected.map(s => s.toLowerCase()).includes(t.toLowerCase())).slice(0, 14).map(tag => (
            <button key={tag} onClick={() => addTag(tag)}
              className="flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold border border-dashed border-border text-muted-foreground hover:border-primary hover:text-primary transition-all cursor-pointer">
              <Hash size={9} />{tag}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}

// ── Airbnb-style flat listing card ────────────────────────────────────────────
function FlatListingCard({
  flat, onRequest, requested, requesting, isNearby,
}: {
  flat: FlatSnapshot
  onRequest: () => void
  requested: boolean
  requesting: boolean
  isNearby: boolean
}) {
  const { vacancy } = flat
  const sym = CURRENCY_SYMBOLS[vacancy.currency] ?? vacancy.currency
  const location = [vacancy.area, vacancy.city].filter(Boolean).join(', ')
  const [gradClass] = flatGradient(flat.flatName)
  const initial = flat.flatName.charAt(0).toUpperCase()

  return (
    <div className="rounded-2xl border border-border bg-card overflow-hidden flex flex-col shadow-sm hover:shadow-xl transition-shadow duration-300 group cursor-default">

      {/* ── Visual header (Airbnb "photo" area) ── */}
      <div className={`relative h-36 bg-gradient-to-br ${gradClass} overflow-hidden select-none`}>
        {/* Decorative large initial */}
        <span className="absolute -right-4 -bottom-4 text-[120px] font-black text-white/8 leading-none pointer-events-none">
          {initial}
        </span>
        {/* Subtle dot grid overlay */}
        <div className="absolute inset-0"
          style={{ backgroundImage: 'radial-gradient(circle, rgba(255,255,255,0.07) 1px, transparent 1px)', backgroundSize: '20px 20px' }} />

        {/* Badges */}
        <div className="absolute top-3 left-3 flex gap-1.5">
          {isNearby && (
            <span className="flex items-center gap-1 text-[10px] font-extrabold px-2 py-1 rounded-full bg-emerald-500/90 backdrop-blur-sm text-white shadow-sm">
              <Star size={9} fill="currentColor" /> Near you
            </span>
          )}
        </div>
        <span className="absolute top-3 right-3 text-[10px] font-extrabold px-2 py-1 rounded-full bg-white/15 backdrop-blur-sm text-white">
          Open
        </span>

        {/* Bottom info strip */}
        <div className="absolute bottom-0 left-0 right-0 px-4 pb-3 pt-8 bg-gradient-to-t from-black/50 to-transparent">
          <div className="flex items-end justify-between gap-2">
            <div className="min-w-0">
              <p className="font-extrabold text-white text-base leading-tight truncate">{flat.flatName}</p>
              {location && (
                <div className="flex items-center gap-1 text-white/80 text-[11px] mt-0.5">
                  <MapPin size={9} /><span className="truncate">{location}</span>
                </div>
              )}
            </div>
            {vacancy.rentPerHead && (
              <div className="shrink-0 text-right">
                <p className="text-white font-extrabold text-lg leading-none">{sym}{vacancy.rentPerHead.toLocaleString()}</p>
                <p className="text-white/70 text-[10px]">per month</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* ── Card body ── */}
      <div className="p-4 flex flex-col gap-3 flex-1">

        {/* Quick stats */}
        <div className="flex items-center flex-wrap gap-x-3 gap-y-1 text-xs text-muted-foreground">
          <span className="flex items-center gap-1 font-semibold">
            <BedDouble size={12} />{vacancy.bedsAvailable} bed{vacancy.bedsAvailable !== 1 ? 's' : ''} open
          </span>
          <span className="text-border">·</span>
          <span className="flex items-center gap-1 font-semibold">
            <Users size={12} />{flat.memberCount} living{vacancy.existingMembersGender ? ` (${GENDER_LABEL[vacancy.existingMembersGender].toLowerCase()})` : ''}
          </span>
          {vacancy.preferredGender !== 'any' && (
            <>
              <span className="text-border">·</span>
              <span className="font-semibold text-violet-600 dark:text-violet-400 capitalize">{vacancy.preferredGender} preferred</span>
            </>
          )}
        </div>

        {/* Lifestyle tags */}
        {vacancy.lifestyle.length > 0 && (
          <div className="flex flex-wrap gap-1.5">
            {vacancy.lifestyle.slice(0, 5).map(tag => {
              const cfg = ALL_LIFESTYLE_TAGS.find(t => t.id === tag)
              return cfg ? (
                <span key={tag} className="flex items-center gap-1 px-2.5 py-1 bg-primary/10 text-primary rounded-full text-[11px] font-semibold">
                  {cfg.icon}{cfg.label}
                </span>
              ) : null
            })}
            {vacancy.lifestyle.length > 5 && (
              <span className="px-2.5 py-1 bg-secondary text-muted-foreground rounded-full text-[11px] font-semibold">+{vacancy.lifestyle.length - 5} more</span>
            )}
          </div>
        )}

        {/* Hashtags */}
        {(vacancy.customTags ?? []).length > 0 && (
          <div className="flex flex-wrap gap-1.5">
            {(vacancy.customTags ?? []).slice(0, 4).map(tag => (
              <span key={tag} className="flex items-center gap-0.5 px-2.5 py-1 bg-secondary/80 border border-border rounded-full text-[11px] font-semibold text-muted-foreground">
                <Hash size={9} />{tag}
              </span>
            ))}
          </div>
        )}

        {/* About */}
        {vacancy.about && (
          <p className="text-xs text-muted-foreground leading-relaxed line-clamp-2">{vacancy.about}</p>
        )}

        {/* CTA */}
        <div className="mt-auto pt-1">
          {requested ? (
            <div className="w-full flex items-center justify-center gap-2 py-2.5 rounded-xl bg-emerald-100 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-400 font-bold text-sm">
              <Check size={15} /> Request sent
            </div>
          ) : (
            <Button className="w-full font-bold gap-2 cursor-pointer group-hover:bg-primary/90 transition-colors" onClick={onRequest} disabled={requesting}>
              {requesting ? <Loader2 size={14} className="animate-spin" /> : <UserPlus size={14} />}
              {requesting ? 'Sending…' : 'Request to join'}
              {!requesting && <ArrowRight size={13} className="ml-auto" />}
            </Button>
          )}
        </div>
      </div>
    </div>
  )
}

// ── Airbnb-style person card (admin sees who wants to join) ───────────────────
function PersonCard({
  request, onApprove, onReject,
}: {
  request: EnrichedJoinRequest
  onApprove: () => void
  onReject: () => void
}) {
  const gradient = personGradient(request.uid)
  const initial = (request.nickname || request.email || 'U').charAt(0).toUpperCase()
  const lifestyle = request.lifestyle ?? []
  const customTags = request.customTags ?? []
  const location = [request.area, request.city].filter(Boolean).join(', ')

  const MOVE_IN_LABEL: Record<string, string> = {
    asap: 'ASAP', this_month: 'This month', next_month: 'Next month', flexible: 'Flexible',
  }

  return (
    <div className="rounded-2xl border border-border bg-card overflow-hidden flex flex-col shadow-sm hover:shadow-lg transition-shadow duration-300">

      {/* ── Gradient header ── */}
      <div className={`relative h-20 bg-gradient-to-br ${gradient} overflow-hidden`}>
        <span className="absolute -right-2 -bottom-3 text-[96px] font-black text-white/8 leading-none pointer-events-none select-none">
          {initial}
        </span>
        {/* Move-in badge */}
        {request.moveIn && (
          <span className="absolute top-2.5 right-2.5 flex items-center gap-1 text-[9px] font-extrabold px-2 py-1 rounded-full bg-black/20 backdrop-blur-sm text-white">
            <CalendarClock size={9} />{MOVE_IN_LABEL[request.moveIn] ?? request.moveIn}
          </span>
        )}
      </div>

      {/* Avatar floating below header */}
      <div className="px-4 -mt-5 mb-0">
        <div className={`w-10 h-10 rounded-2xl bg-gradient-to-br ${gradient} border-2 border-card flex items-center justify-center text-white font-extrabold text-lg shadow-md`}>
          {initial}
        </div>
      </div>

      {/* ── Body ── */}
      <div className="px-4 pt-2 pb-4 flex flex-col gap-3 flex-1">
        <div>
          <p className="font-extrabold text-sm">{request.nickname || request.email}</p>
          {location && (
            <div className="flex items-center gap-1 mt-0.5 text-[11px] text-muted-foreground">
              <MapPin size={9} />Looking in {location}
            </div>
          )}
        </div>

        {lifestyle.length > 0 && (
          <div className="flex flex-wrap gap-1">
            {lifestyle.slice(0, 5).map(tag => {
              const cfg = ALL_LIFESTYLE_TAGS.find(t => t.id === tag)
              return cfg ? (
                <span key={tag} className="flex items-center gap-1 px-2 py-0.5 bg-primary/10 text-primary rounded-full text-[11px] font-semibold">
                  {cfg.icon}{cfg.label}
                </span>
              ) : null
            })}
            {lifestyle.length > 5 && (
              <span className="px-2 py-0.5 bg-secondary rounded-full text-[11px] text-muted-foreground font-semibold">+{lifestyle.length - 5}</span>
            )}
          </div>
        )}

        {customTags.length > 0 && (
          <div className="flex flex-wrap gap-1">
            {customTags.slice(0, 3).map(tag => (
              <span key={tag} className="flex items-center gap-0.5 px-2 py-0.5 bg-secondary border border-border rounded-full text-[11px] font-semibold text-muted-foreground">
                <Hash size={9} />{tag}
              </span>
            ))}
          </div>
        )}

        {request.about && (
          <p className="text-[11px] text-muted-foreground leading-relaxed line-clamp-2 italic">"{request.about}"</p>
        )}

        {request.message && (
          <div className="flex gap-2 p-2.5 rounded-xl bg-secondary/50 border border-border">
            <MessageSquare size={12} className="text-muted-foreground shrink-0 mt-0.5" />
            <p className="text-[11px] text-muted-foreground leading-relaxed line-clamp-2">{request.message}</p>
          </div>
        )}

        {!location && lifestyle.length === 0 && customTags.length === 0 && !request.about && (
          <p className="text-[11px] text-muted-foreground italic">No profile filled in yet.</p>
        )}

        {/* Actions */}
        <div className="mt-auto pt-1 flex gap-2">
          <Button size="sm"
            className="flex-1 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs cursor-pointer gap-1"
            onClick={onApprove}>
            <CheckCircle2 size={12} /> Approve
          </Button>
          <Button size="sm" variant="outline"
            className="flex-1 border-destructive/40 text-destructive hover:bg-destructive/10 font-bold text-xs cursor-pointer gap-1"
            onClick={onReject}>
            <X size={12} /> Decline
          </Button>
        </div>
      </div>
    </div>
  )
}

// ── Join request modal ────────────────────────────────────────────────────────
function JoinRequestModal({
  flat, onClose, onSubmit, submitting,
}: {
  flat: FlatSnapshot
  onClose: () => void
  onSubmit: (moveIn: MoveInTiming, message: string) => void
  submitting: boolean
}) {
  const [moveIn, setMoveIn] = useState<MoveInTiming>('flexible')
  const [message, setMessage] = useState('')
  const [gradClass] = flatGradient(flat.flatName)
  const initial = flat.flatName.charAt(0).toUpperCase()
  const location = [flat.vacancy.area, flat.vacancy.city].filter(Boolean).join(', ')

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-end sm:items-center justify-center p-4"
      onClick={e => { if (e.target === e.currentTarget) onClose() }}>
      <div className="w-full max-w-md bg-card rounded-2xl shadow-2xl border border-border overflow-hidden animate-in slide-in-from-bottom-4 duration-300">

        {/* Modal header — matches flat card */}
        <div className={`relative h-20 bg-gradient-to-br ${gradClass} overflow-hidden`}>
          <span className="absolute -right-2 -bottom-4 text-[96px] font-black text-white/8 leading-none pointer-events-none select-none">{initial}</span>
          <div className="relative z-10 flex items-center gap-3 px-5 h-full">
            <div className="w-9 h-9 rounded-xl bg-white/20 backdrop-blur-sm flex items-center justify-center text-white font-extrabold text-lg">
              {initial}
            </div>
            <div className="min-w-0">
              <p className="font-extrabold text-white text-sm truncate">{flat.flatName}</p>
              {location && <p className="text-white/70 text-xs flex items-center gap-1"><MapPin size={9} />{location}</p>}
            </div>
            <button onClick={onClose} className="ml-auto text-white/70 hover:text-white cursor-pointer transition-colors shrink-0">
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Form */}
        <div className="p-5 space-y-5">
          <div>
            <p className="font-extrabold text-lg">Introduce yourself</p>
            <p className="text-xs text-muted-foreground mt-0.5">The admin will review your profile and get back to you.</p>
          </div>

          {/* Move-in timing */}
          <div className="space-y-2">
            <p className="text-xs font-bold flex items-center gap-1.5">
              <CalendarClock size={13} className="text-muted-foreground" /> When are you looking to move?
            </p>
            <div className="grid grid-cols-2 gap-2">
              {MOVE_IN_OPTIONS.map(({ val, label, desc }) => (
                <button key={val} onClick={() => setMoveIn(val)}
                  className={`py-2.5 px-3 rounded-xl border text-left transition-all cursor-pointer ${
                    moveIn === val
                      ? 'border-primary bg-primary text-white'
                      : 'border-border bg-background hover:border-primary/50'
                  }`}>
                  <p className={`text-xs font-extrabold ${moveIn === val ? 'text-white' : 'text-foreground'}`}>{label}</p>
                  <p className={`text-[10px] mt-0.5 ${moveIn === val ? 'text-white/70' : 'text-muted-foreground'}`}>{desc}</p>
                </button>
              ))}
            </div>
          </div>

          {/* Message */}
          <div className="space-y-2">
            <p className="text-xs font-bold flex items-center gap-1.5">
              <MessageSquare size={13} className="text-muted-foreground" />
              Message to admin
              <span className="font-normal text-muted-foreground">(optional)</span>
            </p>
            <textarea maxLength={150} rows={2}
              placeholder="e.g. I work at TCS Hitech City, vegetarian, looking for a quiet place…"
              value={message} onChange={e => setMessage(e.target.value)}
              className="w-full rounded-xl border border-border bg-background px-3 py-2.5 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-primary/30 leading-relaxed" />
            <p className="text-[10px] text-right text-muted-foreground">{message.length}/150</p>
          </div>

          <div className="flex gap-2 pt-1">
            <Button className="flex-1 font-bold gap-2 cursor-pointer" onClick={() => onSubmit(moveIn, message)} disabled={submitting}>
              {submitting ? <Loader2 size={14} className="animate-spin" /> : <UserPlus size={14} />}
              {submitting ? 'Sending…' : 'Send request'}
            </Button>
            <Button variant="outline" className="cursor-pointer px-4" onClick={onClose} disabled={submitting}>
              Cancel
            </Button>
          </div>
        </div>
      </div>
    </div>
  )
}

// ── Main page ─────────────────────────────────────────────────────────────────
export default function FindMembersPage() {
  const {
    vacancy, members, joinRequests, flatId, name: flatName,
    updateVacancy, approveJoinRequest, rejectJoinRequest,
  } = useFlatStore()
  const { user } = useAuthStore()

  const currentMember = members.find(m => m.uid === user?.uid)
  const isAdmin = currentMember?.role === 'admin'
  const pendingJoins = joinRequests.filter(r => r.status === 'pending') as EnrichedJoinRequest[]

  const [tab, setTab] = useState<'your-flat' | 'find-flat'>('your-flat')

  // ── Your Flat tab state ───────────────────────────────────────────────────
  const [editing, setEditing] = useState(false)
  const [draft, setDraft] = useState<VacancyListing>(vacancy ?? DEFAULT_VACANCY)
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState('')
  const [copied, setCopied] = useState(false)
  const [showPreview, setShowPreview] = useState(false)
  const [showImprove, setShowImprove] = useState(false)

  useEffect(() => { if (vacancy && !editing) setDraft(vacancy) }, [vacancy, editing])

  const handleSaveListing = async () => {
    setSaving(true); setSaveError('')
    try { await updateVacancy(draft); setEditing(false) }
    catch { setSaveError('Failed to save. Try again.') }
    finally { setSaving(false) }
  }

  const handleCopyLink = () => {
    if (!flatId) return
    const link = `${window.location.origin}/onboarding?mode=join&code=${flatId}`
    navigator.clipboard.writeText(link).then(() => { setCopied(true); setTimeout(() => setCopied(false), 2000) })
  }

  // ── Find a Flat tab state ─────────────────────────────────────────────────
  const [seekerProfile, setSeekerProfile] = useState<SeekerProfile | null>(null)
  const [seekerDraft, setSeekerDraft] = useState<SeekerDraft>({ ...DEFAULT_SEEKER })
  const [editingSeeker, setEditingSeeker] = useState(false)
  const [savingSeeker, setSavingSeeker] = useState(false)

  const [activeFlats, setActiveFlats] = useState<FlatSnapshot[]>([])
  const [loadingFlats, setLoadingFlats] = useState(false)
  const [requestedFlats, setRequestedFlats] = useState<Set<string>>(new Set())

  // Join request modal
  const [joinTarget, setJoinTarget] = useState<FlatSnapshot | null>(null)
  const [submittingRequest, setSubmittingRequest] = useState(false)

  // Search
  const [searchQuery, setSearchQuery] = useState('')

  useEffect(() => {
    if (!user?.uid) return
    fetchSeekerProfile(user.uid).then(p => {
      if (p) {
        setSeekerProfile(p)
        setSeekerDraft({ city: p.city, area: p.area, lifestyle: p.lifestyle, customTags: p.customTags, about: p.about, active: p.active })
      }
    })
  }, [user?.uid])

  useEffect(() => {
    if (tab !== 'find-flat') return
    setLoadingFlats(true)
    fetchActiveFlats(flatId ?? undefined).then(flats => {
      setActiveFlats(flats); setLoadingFlats(false)
    })
  }, [tab, flatId])

  // Sort + filter flats: city-matched first, then search filter
  const displayedFlats = useMemo(() => {
    const seekerCity = seekerProfile?.city?.toLowerCase() ?? ''
    let list = [...activeFlats]
    if (seekerCity) list.sort((a, b) => {
      const aM = a.vacancy.city.toLowerCase() === seekerCity
      const bM = b.vacancy.city.toLowerCase() === seekerCity
      return aM === bM ? 0 : aM ? -1 : 1
    })
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase()
      list = list.filter(f =>
        f.flatName.toLowerCase().includes(q) ||
        f.vacancy.city.toLowerCase().includes(q) ||
        f.vacancy.area.toLowerCase().includes(q)
      )
    }
    return list
  }, [activeFlats, seekerProfile?.city, searchQuery])

  const handleToggleSeeking = async () => {
    if (!user?.uid) return
    const newActive = !(seekerProfile?.active ?? false)
    const profile: SeekerProfile = {
      uid: user.uid,
      displayName: user.displayName ?? user.email?.split('@')[0] ?? 'User',
      city: seekerProfile?.city ?? '',
      area: seekerProfile?.area ?? '',
      lifestyle: seekerProfile?.lifestyle ?? [],
      customTags: seekerProfile?.customTags ?? [],
      about: seekerProfile?.about ?? '',
      active: newActive,
      updatedAt: new Date().toISOString(),
    }
    await saveSeekerProfile(profile)
    setSeekerProfile(profile)
    if (newActive && !seekerProfile?.city) setEditingSeeker(true)
  }

  const handleSaveSeeker = async () => {
    if (!user?.uid) return
    setSavingSeeker(true)
    const profile: SeekerProfile = {
      uid: user.uid,
      displayName: user.displayName ?? user.email?.split('@')[0] ?? 'User',
      city: seekerDraft.city, area: seekerDraft.area,
      lifestyle: seekerDraft.lifestyle, customTags: seekerDraft.customTags,
      about: seekerDraft.about,
      active: seekerProfile?.active ?? true,
      updatedAt: new Date().toISOString(),
    }
    await saveSeekerProfile(profile)
    setSeekerProfile(profile)
    setEditingSeeker(false)
    setSavingSeeker(false)
  }

  const handleJoinSubmit = async (moveIn: MoveInTiming, message: string) => {
    if (!joinTarget || !user?.uid || !user.email) return
    setSubmittingRequest(true)
    const nickname = currentMember?.nickname ?? user.displayName ?? user.email.split('@')[0]
    await raiseJoinRequest(joinTarget.flatId, user.uid, user.email, nickname, {
      city: seekerProfile?.city, area: seekerProfile?.area,
      lifestyle: seekerProfile?.lifestyle, customTags: seekerProfile?.customTags,
      about: seekerProfile?.about, moveIn, message,
    })
    setRequestedFlats(prev => new Set([...prev, joinTarget.flatId]))
    setSubmittingRequest(false)
    setJoinTarget(null)
  }

  const currentListing = vacancy ?? DEFAULT_VACANCY
  const { score, missing } = listingScore(currentListing)
  const listingEmpty = !currentListing.city && !currentListing.area && !currentListing.about
    && !currentListing.rentPerHead && currentListing.lifestyle.length === 0

  return (
    <>
      {/* ── Join request modal ── */}
      {joinTarget && (
        <JoinRequestModal
          flat={joinTarget}
          onClose={() => setJoinTarget(null)}
          onSubmit={handleJoinSubmit}
          submitting={submittingRequest}
        />
      )}

      <div className="space-y-5 max-w-2xl">

        {/* ── Header ───────────────────────────────────────────── */}
        <div>
          <h1 className="text-3xl font-extrabold tracking-tight">Flat Board</h1>
          <p className="text-muted-foreground mt-1 text-sm">Manage your flat's vacancy · or find a new flat for yourself.</p>
        </div>

        {/* ── Tab toggle ──────────────────────────────────────────── */}
        <div className="flex gap-1 p-1 bg-secondary/60 rounded-2xl">
          <button onClick={() => setTab('your-flat')}
            className={`flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-sm font-bold transition-all cursor-pointer ${tab === 'your-flat' ? 'bg-card shadow-sm text-foreground' : 'text-muted-foreground hover:text-foreground'}`}>
            <Building2 size={15} />
            My Vacancy
            {pendingJoins.length > 0 && (
              <span className="bg-primary text-white text-[10px] font-extrabold px-1.5 py-0.5 rounded-full leading-none">{pendingJoins.length}</span>
            )}
          </button>
          <button onClick={() => setTab('find-flat')}
            className={`flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-sm font-bold transition-all cursor-pointer ${tab === 'find-flat' ? 'bg-card shadow-sm text-foreground' : 'text-muted-foreground hover:text-foreground'}`}>
            <ScanSearch size={15} />
            Find a Flat
            {seekerProfile?.active && <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />}
          </button>
        </div>

        {/* ═══════════ TAB 1: FOR YOUR FLAT ════════════════════════ */}
        {tab === 'your-flat' && (
          <div className="space-y-5">

            {/* Vacancy toggle */}
            {isAdmin && (
              <Card className="shadow-sm">
                <CardContent className="p-5">
                  <div className="flex items-center justify-between gap-4">
                    <div className="flex items-center gap-3">
                      <div className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 ${vacancy?.active ? 'bg-emerald-100 dark:bg-emerald-900/30' : 'bg-secondary'}`}>
                        <UserPlus size={18} className={vacancy?.active ? 'text-emerald-600 dark:text-emerald-400' : 'text-muted-foreground'} />
                      </div>
                      <div>
                        <p className="font-bold text-sm">{vacancy?.active ? 'Actively looking for members' : 'Not looking right now'}</p>
                        <p className="text-xs text-muted-foreground mt-0.5">
                          {vacancy?.active ? `${currentListing.bedsAvailable} bed${currentListing.bedsAvailable !== 1 ? 's' : ''} available · listing is live` : 'Toggle on to activate your listing'}
                        </p>
                      </div>
                    </div>
                    <button onClick={() => updateVacancy({ active: !vacancy?.active })}
                      className={`relative w-12 h-6 rounded-full transition-colors duration-200 cursor-pointer shrink-0 ${vacancy?.active ? 'bg-emerald-500' : 'bg-border'}`}
                      role="switch" aria-checked={vacancy?.active ?? false}>
                      <span className={`absolute top-0.5 w-5 h-5 rounded-full bg-white shadow-sm transition-transform duration-200 ${vacancy?.active ? 'translate-x-6' : 'translate-x-0.5'}`} />
                    </button>
                  </div>
                </CardContent>
              </Card>
            )}

            {/* Listing editor card */}
            <Card className="shadow-sm">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between gap-2">
                  <div>
                    <CardTitle className="text-base">Vacancy Listing</CardTitle>
                    <CardDescription className="text-xs mt-0.5">
                      {isAdmin ? "Describe your flat and who you're looking for." : "Your flat's current listing."}
                    </CardDescription>
                  </div>
                  {isAdmin && !editing && (
                    <Button size="sm" variant="outline" className="h-8 gap-1.5 text-xs font-semibold cursor-pointer"
                      onClick={() => { setDraft({ ...DEFAULT_VACANCY, ...vacancy }); setEditing(true) }}>
                      <Pencil size={13} /> Edit
                    </Button>
                  )}
                </div>
              </CardHeader>
              <CardContent>
                {editing ? (
                  <div className="space-y-6">
                    <SectionLabel label="Where is the flat?" />
                    <div className="grid grid-cols-2 gap-3">
                      <div className="space-y-1.5">
                        <p className="text-xs font-semibold text-foreground/70">City</p>
                        <Input placeholder="e.g. Bengaluru" value={draft.city} onChange={e => setDraft(d => ({ ...d, city: e.target.value }))} className="h-9 text-sm" />
                      </div>
                      <div className="space-y-1.5">
                        <p className="text-xs font-semibold text-foreground/70">Area</p>
                        <Input placeholder="e.g. Koramangala" value={draft.area} onChange={e => setDraft(d => ({ ...d, area: e.target.value }))} className="h-9 text-sm" />
                      </div>
                    </div>

                    <SectionLabel label="What's available?" />
                    <div className="space-y-4">
                      <div className="space-y-2">
                        <p className="text-xs font-semibold text-foreground/70">Beds available</p>
                        <div className="flex gap-2">
                          {[1, 2, 3, 4].map(n => (
                            <button key={n} onClick={() => setDraft(d => ({ ...d, bedsAvailable: n }))}
                              className={`w-12 h-10 rounded-xl border-2 text-sm font-bold transition-all cursor-pointer ${draft.bedsAvailable === n ? 'border-primary bg-primary text-white' : 'border-border bg-background hover:border-primary/50'}`}>{n}</button>
                          ))}
                        </div>
                      </div>
                      <div className="space-y-2">
                        <p className="text-xs font-semibold text-foreground/70">Rent per head <span className="font-normal text-muted-foreground">(optional)</span></p>
                        <div className="flex gap-2">
                          <select value={draft.currency} onChange={e => setDraft(d => ({ ...d, currency: e.target.value }))}
                            className="h-9 rounded-xl border border-border bg-background px-2.5 text-sm font-semibold focus:outline-none focus:ring-2 focus:ring-primary/30 cursor-pointer">
                            {CURRENCIES.map(c => <option key={c} value={c}>{c}</option>)}
                          </select>
                          <Input type="number" min={0} placeholder="e.g. 8000"
                            value={draft.rentPerHead ?? ''}
                            onChange={e => setDraft(d => ({ ...d, rentPerHead: e.target.value ? Number(e.target.value) : null }))}
                            className="h-9 text-sm flex-1" />
                        </div>
                      </div>
                    </div>

                    <SectionLabel label="People" />
                    <div className="grid grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <p className="text-xs font-semibold text-foreground/70">Current flatmates <span className="font-normal text-muted-foreground">({members.length})</span></p>
                        <div className="space-y-1.5">
                          {([{ val: 'all_male', label: 'All male' }, { val: 'all_female', label: 'All female' }, { val: 'mixed', label: 'Mixed' }] as const).map(({ val, label }) => (
                            <button key={val} onClick={() => setDraft(d => ({ ...d, existingMembersGender: d.existingMembersGender === val ? null : val }))}
                              className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-xl border text-sm font-medium text-left transition-all cursor-pointer ${draft.existingMembersGender === val ? 'border-primary bg-primary/5 text-primary' : 'border-border text-muted-foreground hover:border-primary/40 hover:text-foreground'}`}>
                              <span className={`w-3.5 h-3.5 rounded-full border-2 shrink-0 transition-colors ${draft.existingMembersGender === val ? 'border-primary bg-primary' : 'border-muted-foreground/40'}`} />
                              {label}
                            </button>
                          ))}
                        </div>
                      </div>
                      <div className="space-y-2">
                        <p className="text-xs font-semibold text-foreground/70">Looking for</p>
                        <div className="space-y-1.5">
                          {([{ val: 'any', label: 'Anyone' }, { val: 'male', label: 'Male' }, { val: 'female', label: 'Female' }] as const).map(({ val, label }) => (
                            <button key={val} onClick={() => setDraft(d => ({ ...d, preferredGender: val }))}
                              className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-xl border text-sm font-medium text-left transition-all cursor-pointer ${draft.preferredGender === val ? 'border-primary bg-primary/5 text-primary' : 'border-border text-muted-foreground hover:border-primary/40 hover:text-foreground'}`}>
                              <span className={`w-3.5 h-3.5 rounded-full border-2 shrink-0 transition-colors ${draft.preferredGender === val ? 'border-primary bg-primary' : 'border-muted-foreground/40'}`} />
                              {label}
                            </button>
                          ))}
                        </div>
                      </div>
                    </div>

                    <SectionLabel label="Lifestyle" />
                    <div className="space-y-5">
                      {TAG_CATEGORIES.map(cat => (
                        <div key={cat.label} className="space-y-2">
                          <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">{cat.label}</p>
                          <div className="flex flex-wrap gap-2">
                            {cat.tags.map(({ id, label, icon }) => {
                              const sel = draft.lifestyle.includes(id)
                              return (
                                <button key={id}
                                  onClick={() => setDraft(d => ({ ...d, lifestyle: d.lifestyle.includes(id) ? d.lifestyle.filter(t => t !== id) : [...d.lifestyle, id] }))}
                                  className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold border transition-all cursor-pointer ${sel ? 'bg-primary border-primary text-white' : 'bg-background border-border text-muted-foreground hover:border-primary/50 hover:text-foreground'}`}>
                                  {icon}{label}
                                </button>
                              )
                            })}
                          </div>
                        </div>
                      ))}
                    </div>

                    <SectionLabel label="Company & Location Tags" />
                    <HashtagInput selected={draft.customTags ?? []} onChange={tags => setDraft(d => ({ ...d, customTags: tags }))} uid={user?.uid ?? ''} />

                    <SectionLabel label="Tell them more" />
                    <textarea maxLength={200} rows={3}
                      placeholder="e.g. Quiet 3BHK near metro. We cook together on weekends."
                      value={draft.about} onChange={e => setDraft(d => ({ ...d, about: e.target.value }))}
                      className="w-full rounded-xl border border-border bg-background px-3 py-2.5 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-primary/30 leading-relaxed" />
                    <p className="text-[11px] text-muted-foreground text-right -mt-4">{draft.about.length}/200</p>

                    {saveError && <p className="text-sm text-destructive font-medium">{saveError}</p>}
                    <div className="flex gap-2 pt-2">
                      <Button className="flex-1 font-semibold gap-2 cursor-pointer" onClick={handleSaveListing} disabled={saving}>
                        <Save size={14} />{saving ? 'Saving…' : 'Save listing'}
                      </Button>
                      <Button variant="outline" className="px-4 cursor-pointer" onClick={() => { setDraft(vacancy ?? DEFAULT_VACANCY); setEditing(false); setSaveError('') }} disabled={saving}>
                        <X size={14} />
                      </Button>
                    </div>
                  </div>
                ) : (
                  /* Read-only listing view */
                  <div className="space-y-4">
                    {listingEmpty ? (
                      <div className="py-10 text-center space-y-2">
                        <div className="w-12 h-12 rounded-2xl bg-secondary mx-auto flex items-center justify-center">
                          <MapPin size={20} className="text-muted-foreground" />
                        </div>
                        <p className="text-sm font-semibold text-muted-foreground">{isAdmin ? 'No listing yet' : 'Listing not set up'}</p>
                        <p className="text-xs text-muted-foreground">{isAdmin ? 'Click Edit to describe your flat.' : 'Ask your admin to fill in the details.'}</p>
                      </div>
                    ) : (
                      <>
                        {(currentListing.city || currentListing.area) && (
                          <div className="flex items-center gap-2"><MapPin size={14} className="text-muted-foreground shrink-0" /><span className="text-sm font-semibold">{[currentListing.area, currentListing.city].filter(Boolean).join(', ')}</span></div>
                        )}
                        <div className="flex flex-wrap gap-2">
                          <span className="flex items-center gap-1.5 bg-secondary/60 rounded-xl px-3 py-2 text-sm font-semibold"><BedDouble size={14} className="text-muted-foreground" />{currentListing.bedsAvailable} bed{currentListing.bedsAvailable !== 1 ? 's' : ''} open</span>
                          {currentListing.rentPerHead && <span className="flex items-center gap-1 bg-secondary/60 rounded-xl px-3 py-2 text-sm font-semibold"><span className="text-muted-foreground font-bold">{CURRENCY_SYMBOLS[currentListing.currency]}</span>{currentListing.rentPerHead.toLocaleString()}/mo</span>}
                          <span className="flex items-center gap-1.5 bg-secondary/60 rounded-xl px-3 py-2 text-sm font-semibold"><Users size={14} className="text-muted-foreground" />{members.length} members{currentListing.existingMembersGender ? ` · ${GENDER_LABEL[currentListing.existingMembersGender]}` : ''}</span>
                          {currentListing.preferredGender !== 'any' && <span className="flex items-center gap-1.5 bg-violet-50 dark:bg-violet-900/20 rounded-xl px-3 py-2 text-sm font-semibold text-violet-700 dark:text-violet-300 capitalize"><UserPlus size={14} />Looking for {currentListing.preferredGender}</span>}
                        </div>
                        {currentListing.lifestyle.length > 0 && (
                          <div className="flex flex-wrap gap-1.5">
                            {currentListing.lifestyle.map(tag => { const cfg = ALL_LIFESTYLE_TAGS.find(t => t.id === tag); return cfg ? <span key={tag} className="flex items-center gap-1.5 px-2.5 py-1 bg-primary/10 text-primary rounded-full text-xs font-semibold">{cfg.icon}{cfg.label}</span> : null })}
                          </div>
                        )}
                        {(currentListing.customTags ?? []).length > 0 && (
                          <div className="flex flex-wrap gap-1.5">
                            {(currentListing.customTags ?? []).map(tag => <span key={tag} className="flex items-center gap-1 px-2.5 py-1 bg-secondary border border-border rounded-full text-xs font-semibold text-muted-foreground"><Hash size={10} />{tag}</span>)}
                          </div>
                        )}
                        {currentListing.about && <p className="text-sm text-muted-foreground leading-relaxed">{currentListing.about}</p>}
                      </>
                    )}
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Improve card */}
            {isAdmin && !editing && !listingEmpty && score < 100 && (
              <Card className="shadow-sm border-amber-200 dark:border-amber-800/40">
                <button onClick={() => setShowImprove(v => !v)}
                  className="w-full flex items-center justify-between px-5 py-4 cursor-pointer hover:bg-amber-50/50 dark:hover:bg-amber-950/10 transition-colors rounded-xl">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-lg bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center shrink-0">
                      <TrendingUp size={15} className="text-amber-600 dark:text-amber-400" />
                    </div>
                    <div className="text-left">
                      <p className="text-sm font-bold">Want to improve your listing?</p>
                      <p className="text-xs text-muted-foreground mt-0.5">{score}% complete · {missing.length} detail{missing.length !== 1 ? 's' : ''} missing</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 shrink-0">
                    <div className="relative w-8 h-8">
                      <svg viewBox="0 0 32 32" className="w-8 h-8 -rotate-90">
                        <circle cx="16" cy="16" r="13" fill="none" stroke="currentColor" strokeWidth="3" className="text-secondary" />
                        <circle cx="16" cy="16" r="13" fill="none" stroke="currentColor" strokeWidth="3"
                          strokeDasharray={`${2 * Math.PI * 13}`}
                          strokeDashoffset={`${2 * Math.PI * 13 * (1 - score / 100)}`}
                          className="text-amber-500 transition-all duration-500" strokeLinecap="round" />
                      </svg>
                      <span className="absolute inset-0 flex items-center justify-center text-[9px] font-extrabold text-amber-600 dark:text-amber-400">{score}</span>
                    </div>
                    {showImprove ? <ChevronUp size={15} className="text-muted-foreground" /> : <ChevronDown size={15} className="text-muted-foreground" />}
                  </div>
                </button>
                {showImprove && (
                  <div className="px-5 pb-5 pt-0 border-t border-amber-200 dark:border-amber-800/40 space-y-2">
                    <p className="text-xs text-muted-foreground pt-3">These details help attract better matches:</p>
                    {missing.map(item => (
                      <div key={item} className="flex items-center gap-3 p-3 rounded-xl border border-border bg-background">
                        <div className="w-5 h-5 rounded-full border-2 border-amber-400 shrink-0" />
                        <span className="text-sm font-medium flex-1">{item}</span>
                        <button onClick={() => { setDraft({ ...DEFAULT_VACANCY, ...vacancy }); setEditing(true); setShowImprove(false) }}
                          className="text-xs font-semibold text-primary hover:underline cursor-pointer shrink-0">Add</button>
                      </div>
                    ))}
                  </div>
                )}
              </Card>
            )}

            {/* Incoming requests — person cards */}
            {isAdmin && pendingJoins.length > 0 && (
              <div className="space-y-4">
                <div className="flex items-center gap-2">
                  <Bell size={14} className="text-primary" />
                  <span className="text-sm font-bold">People who want to join</span>
                  <span className="bg-primary text-white text-[10px] font-extrabold px-1.5 py-0.5 rounded-full">{pendingJoins.length}</span>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  {pendingJoins.map(req => (
                    <PersonCard key={req.id} request={req}
                      onApprove={() => approveJoinRequest(req.id)}
                      onReject={() => rejectJoinRequest(req.id)} />
                  ))}
                </div>
              </div>
            )}

            {/* Share invite */}
            <Card className="shadow-sm">
              <CardHeader className="pb-3">
                <CardTitle className="text-base flex items-center gap-2"><Link2 size={15} className="text-primary" /> Share Invite</CardTitle>
                <CardDescription className="text-xs">Send this link to invite someone directly, bypassing the listing.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="flex items-center gap-2 bg-secondary/50 border border-border rounded-xl px-3 py-2.5">
                  <code className="text-xs font-mono text-muted-foreground flex-1 min-w-0 truncate">
                    {typeof window !== 'undefined' ? `${window.location.origin}/onboarding?mode=join&code=${flatId}` : `/onboarding?mode=join&code=${flatId}`}
                  </code>
                  <button onClick={handleCopyLink} className="text-muted-foreground hover:text-foreground transition-colors shrink-0 cursor-pointer">
                    {copied ? <Check size={15} className="text-emerald-500" /> : <Copy size={15} />}
                  </button>
                </div>
                <Button variant="outline" className="w-full gap-2 text-sm font-semibold cursor-pointer" onClick={handleCopyLink}>
                  {copied ? <Check size={14} className="text-emerald-500" /> : <Copy size={14} />}
                  {copied ? 'Link copied!' : 'Copy invite link'}
                </Button>
              </CardContent>
            </Card>

            {/* Preview */}
            <Card className="shadow-sm">
              <button onClick={() => setShowPreview(v => !v)}
                className="w-full flex items-center justify-between px-5 py-4 cursor-pointer hover:bg-secondary/30 transition-colors rounded-xl">
                <div className="flex items-center gap-2">
                  {showPreview ? <EyeOff size={15} className="text-muted-foreground" /> : <Eye size={15} className="text-muted-foreground" />}
                  <span className="text-sm font-semibold">Preview — how new joiners see this listing</span>
                </div>
                {showPreview ? <ChevronUp size={15} className="text-muted-foreground" /> : <ChevronDown size={15} className="text-muted-foreground" />}
              </button>
              {showPreview && flatName && (
                <CardContent className="px-5 pb-5 pt-0 border-t border-border">
                  <div className="mt-4">
                    <FlatListingCard
                      flat={{ flatId: flatId ?? '', flatName, memberCount: members.length, vacancy: currentListing }}
                      onRequest={() => {}}
                      requested={false}
                      requesting={false}
                      isNearby={false}
                    />
                  </div>
                  <p className="text-[11px] text-muted-foreground text-center mt-3">This is how your flat appears to potential flatmates.</p>
                </CardContent>
              )}
            </Card>
          </div>
        )}

        {/* ═══════════ TAB 2: FIND A FLAT ══════════════════════════ */}
        {tab === 'find-flat' && (
          <div className="space-y-5">

            {/* ── Hero search section ────────────────────────────── */}
            <div className="relative rounded-2xl overflow-hidden bg-gradient-to-br from-primary to-blue-700 p-6">
              {/* Decorative bubbles */}
              <div className="absolute -right-10 -top-10 w-48 h-48 rounded-full bg-white/5 pointer-events-none" />
              <div className="absolute -left-6 -bottom-8 w-36 h-36 rounded-full bg-white/5 pointer-events-none" />
              <div className="absolute right-12 bottom-0 w-24 h-24 rounded-full bg-white/5 pointer-events-none" />

              <div className="relative z-10 space-y-4">
                <div>
                  <p className="text-2xl font-extrabold text-white leading-tight">Find your<br />next flat.</p>
                  <p className="text-white/70 text-sm mt-1">
                    {activeFlats.length > 0 || loadingFlats
                      ? loadingFlats ? 'Searching…' : `${activeFlats.length} flat${activeFlats.length !== 1 ? 's' : ''} with openings`
                      : 'Discover flats that match your lifestyle.'}
                  </p>
                </div>

                {/* Search bar */}
                <div className="flex gap-2">
                  <div className="flex-1 flex items-center gap-2 bg-white/95 rounded-xl px-3 h-11 shadow-sm">
                    <Search size={14} className="text-muted-foreground shrink-0" />
                    <input
                      value={searchQuery}
                      onChange={e => setSearchQuery(e.target.value)}
                      placeholder="City or area… e.g. Bengaluru, Koramangala"
                      className="text-sm flex-1 bg-transparent outline-none text-foreground placeholder:text-muted-foreground" />
                    {searchQuery && (
                      <button onClick={() => setSearchQuery('')} className="text-muted-foreground hover:text-foreground cursor-pointer shrink-0 transition-colors">
                        <X size={14} />
                      </button>
                    )}
                  </div>
                </div>
              </div>
            </div>

            {/* ── "I'm looking" compact card ─────────────────────── */}
            <Card className={`shadow-sm border-2 transition-colors duration-200 ${seekerProfile?.active ? 'border-emerald-400 dark:border-emerald-600' : 'border-border'}`}>
              <CardContent className="p-4 space-y-3">
                <div className="flex items-center justify-between gap-4">
                  <div className="flex items-center gap-3">
                    <div className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 ${seekerProfile?.active ? 'bg-emerald-100 dark:bg-emerald-900/30' : 'bg-secondary'}`}>
                      <ScanSearch size={16} className={seekerProfile?.active ? 'text-emerald-600 dark:text-emerald-400' : 'text-muted-foreground'} />
                    </div>
                    <div>
                      <p className="font-bold text-sm">{seekerProfile?.active ? "Profile is live" : "Let admins find you"}</p>
                      <p className="text-xs text-muted-foreground mt-0.5">
                        {seekerProfile?.active
                          ? seekerProfile.city ? `Looking in ${[seekerProfile.area, seekerProfile.city].filter(Boolean).join(', ')}` : 'Turn on your profile to be discoverable'
                          : 'Toggle on to make your profile visible to flat admins'}
                      </p>
                    </div>
                  </div>
                  <button onClick={handleToggleSeeking}
                    className={`relative w-12 h-6 rounded-full transition-colors duration-200 cursor-pointer shrink-0 ${seekerProfile?.active ? 'bg-emerald-500' : 'bg-border'}`}
                    role="switch" aria-checked={seekerProfile?.active ?? false}>
                    <span className={`absolute top-0.5 w-5 h-5 rounded-full bg-white shadow-sm transition-transform duration-200 ${seekerProfile?.active ? 'translate-x-6' : 'translate-x-0.5'}`} />
                  </button>
                </div>

                {/* Profile summary / editor */}
                {seekerProfile?.active && (
                  <>
                    <div className="border-t border-border pt-3">
                      {editingSeeker ? (
                        <div className="space-y-5">
                          <SectionLabel label="Where are you looking?" />
                          <div className="grid grid-cols-2 gap-3">
                            <div className="space-y-1.5">
                              <p className="text-xs font-semibold text-foreground/70">City</p>
                              <Input placeholder="e.g. Bengaluru" value={seekerDraft.city} onChange={e => setSeekerDraft(d => ({ ...d, city: e.target.value }))} className="h-9 text-sm" />
                            </div>
                            <div className="space-y-1.5">
                              <p className="text-xs font-semibold text-foreground/70">Area <span className="font-normal text-muted-foreground">(optional)</span></p>
                              <Input placeholder="e.g. Koramangala" value={seekerDraft.area} onChange={e => setSeekerDraft(d => ({ ...d, area: e.target.value }))} className="h-9 text-sm" />
                            </div>
                          </div>

                          <SectionLabel label="My lifestyle" />
                          <div className="space-y-4">
                            {TAG_CATEGORIES.map(cat => (
                              <div key={cat.label} className="space-y-2">
                                <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">{cat.label}</p>
                                <div className="flex flex-wrap gap-1.5">
                                  {cat.tags.map(({ id, label, icon }) => {
                                    const sel = seekerDraft.lifestyle.includes(id)
                                    return (
                                      <button key={id}
                                        onClick={() => setSeekerDraft(d => ({ ...d, lifestyle: d.lifestyle.includes(id) ? d.lifestyle.filter(t => t !== id) : [...d.lifestyle, id] }))}
                                        className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold border transition-all cursor-pointer ${sel ? 'bg-primary border-primary text-white' : 'bg-background border-border text-muted-foreground hover:border-primary/50 hover:text-foreground'}`}>
                                        {icon}{label}
                                      </button>
                                    )
                                  })}
                                </div>
                              </div>
                            ))}
                          </div>

                          <SectionLabel label="Company & Location Tags" />
                          <HashtagInput selected={seekerDraft.customTags ?? []} onChange={tags => setSeekerDraft(d => ({ ...d, customTags: tags }))} uid={user?.uid ?? ''} />

                          <SectionLabel label="About me" />
                          <textarea maxLength={200} rows={2}
                            placeholder="e.g. Looking for a quiet flat near Hitech City. Work 9-5 at TCS."
                            value={seekerDraft.about} onChange={e => setSeekerDraft(d => ({ ...d, about: e.target.value }))}
                            className="w-full rounded-xl border border-border bg-background px-3 py-2.5 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-primary/30 leading-relaxed" />

                          <div className="flex gap-2">
                            <Button className="flex-1 font-semibold gap-2 cursor-pointer" size="sm" onClick={handleSaveSeeker} disabled={savingSeeker}>
                              <Save size={13} />{savingSeeker ? 'Saving…' : 'Save profile'}
                            </Button>
                            <Button variant="outline" size="sm" className="px-4 cursor-pointer" onClick={() => setEditingSeeker(false)} disabled={savingSeeker}><X size={13} /></Button>
                          </div>
                        </div>
                      ) : (
                        <div className="flex items-start justify-between gap-2">
                          <div className="space-y-1.5 flex-1 min-w-0">
                            {seekerProfile.lifestyle.length > 0 && (
                              <div className="flex flex-wrap gap-1">
                                {seekerProfile.lifestyle.slice(0, 4).map(tag => {
                                  const cfg = ALL_LIFESTYLE_TAGS.find(t => t.id === tag)
                                  return cfg ? <span key={tag} className="flex items-center gap-1 px-2 py-0.5 bg-primary/10 text-primary rounded-full text-[11px] font-semibold">{cfg.icon}{cfg.label}</span> : null
                                })}
                                {seekerProfile.lifestyle.length > 4 && <span className="px-2 py-0.5 bg-secondary rounded-full text-[11px] text-muted-foreground font-semibold">+{seekerProfile.lifestyle.length - 4}</span>}
                              </div>
                            )}
                            {(seekerProfile.customTags ?? []).length > 0 && (
                              <div className="flex flex-wrap gap-1">
                                {(seekerProfile.customTags ?? []).map(tag => <span key={tag} className="flex items-center gap-0.5 px-2 py-0.5 bg-secondary border border-border rounded-full text-[11px] font-semibold text-muted-foreground"><Hash size={9} />{tag}</span>)}
                              </div>
                            )}
                            {!seekerProfile.city && seekerProfile.lifestyle.length === 0 && !seekerProfile.about && (
                              <p className="text-xs text-muted-foreground">No profile details yet — add your city and lifestyle to stand out.</p>
                            )}
                          </div>
                          <button onClick={() => setEditingSeeker(true)} className="shrink-0 flex items-center gap-1 text-xs font-bold text-primary hover:underline cursor-pointer">
                            <Pencil size={11} /> Edit
                          </button>
                        </div>
                      )}
                    </div>
                  </>
                )}

                {/* Off-state CTA */}
                {!seekerProfile?.active && !editingSeeker && (
                  <p className="text-xs text-muted-foreground">
                    Your profile stays private until you turn this on. You can still browse and request to join flats below.
                  </p>
                )}
              </CardContent>
            </Card>

            {/* ── Flat listings grid ─────────────────────────────── */}
            <div className="space-y-4">
              {searchQuery && (
                <p className="text-xs text-muted-foreground">
                  {displayedFlats.length} result{displayedFlats.length !== 1 ? 's' : ''} for "{searchQuery}"
                </p>
              )}

              {loadingFlats ? (
                <div className="flex items-center justify-center py-16 gap-2 text-muted-foreground">
                  <Loader2 size={18} className="animate-spin" />
                  <span className="text-sm font-medium">Finding flats…</span>
                </div>
              ) : displayedFlats.length === 0 ? (
                <div className="py-16 text-center space-y-3">
                  <div className="w-14 h-14 rounded-2xl bg-secondary mx-auto flex items-center justify-center">
                    <Building2 size={24} className="text-muted-foreground" />
                  </div>
                  <div>
                    <p className="text-base font-bold text-muted-foreground">
                      {searchQuery ? 'No flats match your search' : 'No open flats right now'}
                    </p>
                    <p className="text-xs text-muted-foreground mt-1">
                      {searchQuery ? 'Try a different city or area name.' : 'Check back later — listings update as vacancies open up.'}
                    </p>
                  </div>
                  {searchQuery && (
                    <Button variant="outline" size="sm" className="cursor-pointer" onClick={() => setSearchQuery('')}>Clear search</Button>
                  )}
                </div>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                  {displayedFlats.map(flat => (
                    <FlatListingCard
                      key={flat.flatId}
                      flat={flat}
                      requested={requestedFlats.has(flat.flatId)}
                      requesting={false}
                      isNearby={!!seekerProfile?.city && flat.vacancy.city.toLowerCase() === seekerProfile.city.toLowerCase()}
                      onRequest={() => setJoinTarget(flat)}
                    />
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </>
  )
}
