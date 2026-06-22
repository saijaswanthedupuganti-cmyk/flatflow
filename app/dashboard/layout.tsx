"use client"
import { useCallback, useEffect, useRef, useState } from 'react'
import Link from 'next/link'
import { useRouter, usePathname, useSearchParams } from 'next/navigation'
import {
  LayoutDashboard, ClipboardList, Users,
  Lightbulb, Info, ChevronRight, ShieldCheck, Repeat2, ChevronDown, Receipt,
  Mic, X, RefreshCw, UserPlus, Crown, AlertTriangle, Ticket,
} from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import { useAuthStore } from '@/store/useAuthStore'
import { useFlatStore } from '@/store/useFlatStore'
import NotificationToast from '@/components/NotificationToast'
import FlatSwitcher from '@/components/FlatSwitcher'
import SubscriptionGate from '@/components/SubscriptionGate'
import { useSubscription } from '@/hooks/useSubscription'
import SubscriptionUpsell from '@/components/SubscriptionUpsell'
import AdminWelcomeModal, { shouldShowAdminWelcome } from '@/components/AdminWelcomeModal'
import RewardUnlockModal from '@/components/RewardUnlockModal'
import { useRewardsStore } from '@/store/useRewardsStore'
import VoiceButton from '@/components/VoiceButton'
import VoiceListeningOverlay from '@/components/VoiceListeningOverlay'
import VoiceResponseCard from '@/components/VoiceResponseCard'
import VoiceFallbackModal from '@/components/VoiceFallbackModal'
import MicPermissionModal from '@/components/MicPermissionModal'
import { useVoiceAssistant } from '@/hooks/useVoiceAssistant'
import { useVoiceProcessor } from '@/hooks/useVoiceProcessor'
import { requestMicPermission } from '@/lib/voice/permissions'
import { initTTS, tts } from '@/lib/voice/tts/speechSynthesis'

const NAV_ITEMS = {
  main: [
    { href: '/dashboard', label: 'Dashboard', icon: LayoutDashboard, exact: true, color: 'text-blue-500', bg: 'bg-blue-500/10' },
    { href: '/dashboard/insights', label: 'Insights', icon: Lightbulb, exact: false, color: 'text-purple-500', bg: 'bg-purple-500/10' },
    { href: '/dashboard/expenses', label: 'Expenses', icon: Receipt, exact: false, color: 'text-amber-500', bg: 'bg-amber-500/10' },
    { href: '/dashboard/swaps', label: 'Swaps', icon: Repeat2, exact: false, color: 'text-violet-500', bg: 'bg-violet-500/10' },
  ],
  admin: [
    { href: '/dashboard/tasks', label: 'Tasks & Rotation', icon: ClipboardList, exact: false, color: 'text-orange-500', bg: 'bg-orange-500/10' },
  ],
  general: [
    { href: '/dashboard/members', label: 'Members', icon: Users, exact: false, color: 'text-pink-500', bg: 'bg-pink-500/10' },
    ...(process.env.NEXT_PUBLIC_DISCOVERY_ENABLED === 'true' ? [
      { href: '/dashboard/find-members', label: 'Flat Board', icon: UserPlus, exact: false, color: 'text-teal-500', bg: 'bg-teal-500/10' },
    ] : []),
    { href: '/dashboard/about', label: 'About', icon: Info, exact: false, color: 'text-muted-foreground', bg: 'bg-secondary' },
  ],
}

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const pathname = usePathname()
  const searchParams = useSearchParams()
  const { user, allFlats, flatId: authFlatId, logout } = useAuthStore()
  const { members, tasks, swapRequests, joinRequests, initFirestoreListeners, isSynced, name: flatName, resetFlatData, wasKicked, clearWasKicked } = useFlatStore()

  const currentUser = members.find(m => m.uid === user?.uid)
  const isAdmin = currentUser?.role === 'admin'

  const { isPremium, isExpired } = useSubscription()
  const { initRewardsListener } = useRewardsStore()
  const subscription = useFlatStore(s => s.subscription)
  const [showQuickAdd, setShowQuickAdd] = useState(false)
  const [showLayoutUpsell, setShowLayoutUpsell] = useState(false)
  const [showAdminWelcome, setShowAdminWelcome] = useState(false)
  const [voiceEnabled, setVoiceEnabled] = useState(() => {
    if (typeof window === 'undefined') return true
    return localStorage.getItem('habitiq-voice') !== 'false'
  })
  const [showFallback, setShowFallback] = useState(false)
  const [showMicPermission, setShowMicPermission] = useState(false)

  const voice     = useVoiceAssistant()
  const processor = useVoiceProcessor()

  // Keep a mutable ref so the onTranscript callback always calls the latest process fn
  const processRef = useRef(processor.process)
  useEffect(() => { processRef.current = processor.process }, [processor.process])

  // Register transcript handler once at layout level
  useEffect(() => {
    voice.onTranscript(({ transcript }) => { processRef.current(transcript) })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Reset voice state once the NLU processor finishes
  const processorWasActive = useRef(false)
  useEffect(() => {
    if (processorWasActive.current && !processor.isProcessing) voice.reset()
    processorWasActive.current = processor.isProcessing
  }, [processor.isProcessing, voice.reset])

  const startVoice = useCallback(() => {
    initTTS()
    voice.startListening()
  }, [voice.startListening])

  const handleVoiceTap = useCallback(async () => {
    if (!voiceEnabled) return
    if (!voice.isSupported) { setShowFallback(true); return }
    // Check if mic permission was already granted in a previous session
    const stored = typeof window !== 'undefined' ? localStorage.getItem('habitiq-mic-perm') : null
    if (stored !== null) {
      startVoice()
    } else {
      setShowMicPermission(true)
    }
  }, [voiceEnabled, voice.isSupported, startVoice])

  // Multi-flat warning: user is in > 1 flat but subscription is expired
  const multiFlat = allFlats.length > 1
  const showMultiFlatWarning = multiFlat && isExpired

  // Fresh admin welcome: only fires when arriving from flat creation (?new=1)
  useEffect(() => {
    if (
      searchParams.get('new') === '1' &&
      isAdmin &&
      authFlatId &&
      !isPremium &&
      shouldShowAdminWelcome(authFlatId)
    ) {
      const t = setTimeout(() => {
        setShowAdminWelcome(true)
        // Remove ?new=1 from URL without re-render so it doesn't re-trigger on refresh
        router.replace('/dashboard')
      }, 900)
      return () => clearTimeout(t)
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams, isAdmin, authFlatId, isPremium])

  useEffect(() => {
    if (user && authFlatId && !isSynced) {
      initFirestoreListeners(authFlatId)
    }
  }, [user, authFlatId, isSynced, initFirestoreListeners])

  useEffect(() => { setShowQuickAdd(false) }, [pathname])

  // Rewards listener — per-user, persists across flat switches
  useEffect(() => {
    if (!user?.uid) return
    const unsub = initRewardsListener(user.uid)
    return unsub
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.uid])

  useEffect(() => {
    if (wasKicked) {
      clearWasKicked()
      router.push('/onboarding?kicked=1')
    }
  }, [wasKicked, clearWasKicked, router])

  if (!user) return null

  const overdueTasks    = tasks.filter(t => t.status === 'overdue').length
  const pendingSwaps    = swapRequests.filter(r => r.status === 'pending').length
  const pendingJoins    = isAdmin ? joinRequests.filter(r => r.status === 'pending').length : 0

  const NavLink = ({ href, label, icon: Icon, exact, color, bg, badge }: typeof NAV_ITEMS.main[0] & { badge?: number }) => {
    const isActive = exact ? pathname === href : pathname.startsWith(href)
    return (
      <Link
        href={href}
        className={`group flex items-center gap-3 px-3 py-2 rounded-xl text-sm font-medium transition-all duration-150 ${
          isActive
            ? 'bg-primary text-primary-foreground shadow-sm'
            : 'text-muted-foreground hover:text-foreground hover:bg-secondary/80'
        }`}
      >
        <span className={`w-7 h-7 rounded-lg flex items-center justify-center shrink-0 transition-all ${
          isActive ? 'bg-white/20' : `${bg}`
        }`}>
          <Icon size={15} className={isActive ? 'text-white' : color} />
        </span>
        <span className="flex-1">{label}</span>
        {badge ? (
          <span className="bg-violet-500 text-white text-[10px] font-extrabold px-1.5 py-0.5 rounded-full min-w-[18px] text-center">
            {badge}
          </span>
        ) : isActive ? (
          <ChevronRight size={14} className="opacity-60" />
        ) : null}
      </Link>
    )
  }

  const MobileNavLink = ({ href, icon: Icon, label, exact, badge }: typeof NAV_ITEMS.main[0] & { badge?: number }) => {
    const isActive = exact ? pathname === href : pathname.startsWith(href)
    return (
      <Link href={href} className={`relative flex flex-col items-center gap-1 px-3 py-1 transition-colors ${isActive ? 'text-primary' : 'text-muted-foreground'}`}>
        {isActive && (
          <span className="absolute -top-2 left-1/2 -translate-x-1/2 w-6 h-[3px] rounded-full bg-primary" />
        )}
        <div className={`relative p-1.5 rounded-xl transition-colors ${isActive ? 'bg-primary/10' : ''}`}>
          <Icon size={20} />
          {badge ? (
            <span className="absolute -top-0.5 -right-0.5 bg-violet-500 text-white text-[9px] font-extrabold w-4 h-4 rounded-full flex items-center justify-center leading-none">
              {badge}
            </span>
          ) : null}
        </div>
        <span className="text-[10px] font-semibold">{label}</span>
      </Link>
    )
  }

  return (
    <div className="flex h-screen bg-secondary/20">
      <NotificationToast />
      <RewardUnlockModal />

      {/* ── Sidebar ─────────────────────────────────── */}
      <aside className="hidden md:flex flex-col w-64 bg-card border-r border-border/60 shadow-sm">

        {/* Logo */}
        <div className={`px-4 pt-4 pb-3 border-b ${isPremium ? 'border-amber-300/30 dark:border-amber-600/20' : 'border-border/60'}`}
          style={isPremium ? { background: 'linear-gradient(180deg, rgba(251,191,36,0.04) 0%, transparent 100%)' } : undefined}
        >
          <div className="flex items-center justify-between gap-2">
            <div className="flex flex-col gap-1 min-w-0">
              <img
                src="/habitiq-logo.svg"
                alt="Habitiq"
                className="h-7 w-auto object-contain object-left dark:brightness-0 dark:invert"
              />
              <p className="text-[10px] text-muted-foreground font-medium truncate">{flatName || authFlatId || 'Loading…'}</p>
            </div>
            {isPremium && (
              <div className="flex items-center gap-1 px-2 py-1 rounded-full shrink-0" style={{ background: 'linear-gradient(135deg, rgba(251,191,36,0.15), rgba(245,158,11,0.1))', border: '1px solid rgba(251,191,36,0.3)' }}>
                <Crown size={10} style={{ color: '#f59e0b' }} />
                <span className="text-[9px] font-extrabold tracking-widest" style={{ color: '#d97706' }}>PREMIUM</span>
              </div>
            )}
          </div>
          {overdueTasks > 0 && (
            <div className="mt-3 flex items-center gap-2 bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-900 rounded-lg px-3 py-1.5">
              <span className="w-2 h-2 bg-red-500 rounded-full animate-pulse shrink-0" />
              <p className="text-xs font-semibold text-red-600 dark:text-red-400">{overdueTasks} overdue task{overdueTasks > 1 ? 's' : ''}</p>
            </div>
          )}
          {showMultiFlatWarning && (
            <div className="mt-3 rounded-xl overflow-hidden" style={{ border: '1px solid rgba(239,68,68,0.3)', background: 'rgba(239,68,68,0.06)' }}>
              <div className="flex items-start gap-2 px-3 py-2.5">
                <AlertTriangle size={13} className="text-red-500 shrink-0 mt-0.5" />
                <div className="flex-1 min-w-0">
                  <p className="text-[11px] font-bold text-red-600 dark:text-red-400 leading-snug">You&apos;re in {allFlats.length} flats</p>
                  <p className="text-[10px] text-red-500/80 dark:text-red-400/70 mt-0.5 leading-snug">Premium required to stay in multiple groups.</p>
                </div>
              </div>
              <button
                onClick={() => setShowLayoutUpsell(true)}
                className="w-full flex items-center justify-center gap-1.5 py-2 text-[10px] font-extrabold text-white transition-colors cursor-pointer"
                style={{ background: 'linear-gradient(135deg, #dc2626, #b91c1c)' }}
              >
                <Ticket size={10} /> Activate Premium
              </button>
            </div>
          )}
        </div>

        {/* Nav */}
        <nav className="flex-1 px-3 py-3 space-y-0.5 overflow-y-auto">
          <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest px-3 mb-1">Main</p>
          {NAV_ITEMS.main.map(item => (
            <NavLink
              key={item.href}
              {...item}
              badge={item.href === '/dashboard/swaps' && pendingSwaps > 0 ? pendingSwaps : undefined}
            />
          ))}

          {isAdmin && (
            <>
              <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest px-3 mt-3 mb-1">Admin</p>
              {NAV_ITEMS.admin.map(item => (
                <NavLink key={item.href} {...item} />
              ))}
            </>
          )}

          <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest px-3 mt-3 mb-1">General</p>
          {NAV_ITEMS.general.map(item => (
            <NavLink
              key={item.href}
              {...item}
              badge={item.href === '/dashboard/members' && pendingJoins > 0 ? pendingJoins : undefined}
            />
          ))}
        </nav>

        {/* Voice button — desktop sidebar */}
        {voiceEnabled && (
          <div className="px-3 pb-1">
            <VoiceButton
              size="sidebar"
              onTap={handleVoiceTap}
              isListening={voice.state.status === 'listening'}
              isProcessing={voice.state.status === 'processing' || processor.isProcessing}
              enabled={voiceEnabled}
            />
          </div>
        )}

        {/* User Footer */}
        <div className="p-3 border-t border-border/60 space-y-2">
          <FlatSwitcher />

          {/* Profile — navigates to full profile page */}
          <Link
            href="/dashboard/profile"
            className={`flex items-center gap-3 px-2 py-2 rounded-xl transition-all ${
              pathname === '/dashboard/profile'
                ? 'bg-primary text-primary-foreground'
                : isPremium
                ? 'bg-secondary/50 hover:bg-secondary'
                : 'bg-secondary/50 hover:bg-secondary'
            }`}
            style={isPremium && pathname !== '/dashboard/profile' ? {
              border: '1px solid rgba(251,191,36,0.25)',
              background: 'linear-gradient(135deg, rgba(251,191,36,0.06), rgba(245,158,11,0.04))',
            } : undefined}
          >
            {/* Avatar with crown badge for premium */}
            <div className="relative shrink-0">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center text-white font-bold text-sm ${
                pathname === '/dashboard/profile' ? 'bg-white/20' : 'bg-primary'
              }`}>
                {user?.displayName?.charAt(0)?.toUpperCase() || 'U'}
              </div>
              {isPremium && pathname !== '/dashboard/profile' && (
                <div className="absolute -top-1 -right-1 w-4 h-4 rounded-full flex items-center justify-center" style={{ background: 'linear-gradient(135deg, #f59e0b, #d97706)', boxShadow: '0 0 6px rgba(245,158,11,0.6)' }}>
                  <Crown size={8} className="text-white" />
                </div>
              )}
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-sm font-semibold truncate leading-tight">{user?.displayName || 'User'}</p>
              <div className="flex items-center gap-1 mt-0.5">
                {isPremium && pathname !== '/dashboard/profile' ? (
                  <>
                    <Crown size={10} style={{ color: '#f59e0b' }} />
                    <p className="text-[11px] font-bold" style={{ color: '#d97706' }}>Premium</p>
                  </>
                ) : (
                  <>
                    <ShieldCheck size={11} className={pathname === '/dashboard/profile' ? 'text-primary-foreground/70' : isAdmin ? 'text-primary' : 'text-muted-foreground'} />
                    <p className={`text-[11px] capitalize ${pathname === '/dashboard/profile' ? 'text-primary-foreground/70' : 'text-muted-foreground'}`}>
                      {isAdmin ? 'Admin' : 'Member'}
                    </p>
                  </>
                )}
              </div>
            </div>
            <ChevronDown size={13} className={`shrink-0 transition-transform -rotate-90 ${pathname === '/dashboard/profile' ? 'text-primary-foreground/60' : 'text-muted-foreground/60'}`} />
          </Link>
        </div>
      </aside>

      {/* ── Main Content ─────────────────────────────── */}
      <main className="flex-1 overflow-y-auto pb-20 md:pb-0">
        {/* Mobile multi-flat warning banner */}
        {showMultiFlatWarning && (
          <div className="md:hidden flex items-center gap-3 px-4 py-3" style={{ background: 'linear-gradient(135deg, #7f1d1d, #991b1b)', borderBottom: '1px solid rgba(239,68,68,0.3)' }}>
            <AlertTriangle size={14} className="text-red-300 shrink-0" />
            <p className="flex-1 text-[12px] font-semibold text-red-100 leading-snug">
              You&apos;re in {allFlats.length} flats — <span className="font-extrabold">Premium required</span> to keep multi-group access.
            </p>
            <button
              onClick={() => setShowLayoutUpsell(true)}
              className="shrink-0 text-[10px] font-extrabold px-2.5 py-1.5 rounded-full text-white cursor-pointer"
              style={{ background: 'rgba(255,255,255,0.15)', border: '1px solid rgba(255,255,255,0.2)' }}
            >
              Activate
            </button>
          </div>
        )}
        <div className="p-6 lg:p-8 max-w-6xl mx-auto">
          <SubscriptionGate>
            {children}
          </SubscriptionGate>
        </div>
      </main>

      {showLayoutUpsell && <SubscriptionUpsell feature="create_flat" onClose={() => setShowLayoutUpsell(false)} />}

      {showAdminWelcome && authFlatId && (
        <AdminWelcomeModal flatId={authFlatId} onClose={() => setShowAdminWelcome(false)} />
      )}

      {/* Voice — single instance at layout level covers all pages */}
      <VoiceListeningOverlay
        state={voice.state}
        onStop={() => { voice.stopListening(); voice.reset() }}
        response={processor.response}
        onDismissResponse={processor.clearResponse}
      />
      <VoiceResponseCard
        response={processor.response}
        onDismiss={processor.clearResponse}
      />
      <VoiceFallbackModal
        isOpen={showFallback}
        onClose={() => setShowFallback(false)}
        onSubmit={(text) => { void processor.process(text); setShowFallback(false) }}
      />
      <MicPermissionModal
        isOpen={showMicPermission}
        onAllow={() => { setShowMicPermission(false); startVoice() }}
        onDismiss={() => setShowMicPermission(false)}
      />

      {/* ── Mobile Bottom Nav ─────────────────────────── */}
      {/* 5 slots: Dashboard · Expenses · [+] FAB · Tasks(admin)/Members · Settings */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-card/95 backdrop-blur border-t border-border/60 flex justify-around items-center px-2 py-2 z-50">
        {/* 1 — Dashboard */}
        <MobileNavLink {...NAV_ITEMS.main[0]} />

        {/* 2 — Expenses */}
        <MobileNavLink {...NAV_ITEMS.main[2]} />

        {/* 3 — Radial FAB (center) */}
        {(() => {
          const adminPetals = [
            { id: 'task',  label: 'Task',  Icon: ClipboardList, bg: 'bg-violet-600', glow: 'rgba(124,58,237,0.55)', href: '/dashboard/tasks?add=1',                   x: -58, y: -70 },
            { id: 'split', label: 'Split', Icon: Receipt,        bg: 'bg-[#3786FB]', glow: 'rgba(55,134,251,0.55)', href: '/dashboard/expenses?add=1',                x:   0, y: -90 },
            { id: 'bill',  label: 'Bill',  Icon: RefreshCw,      bg: 'bg-amber-500', glow: 'rgba(245,158,11,0.55)', href: '/dashboard/expenses?tab=bills&add=1',       x:  58, y: -70 },
          ]
          const memberPetals = [
            { id: 'split', label: 'Split', Icon: Receipt, bg: 'bg-[#3786FB]', glow: 'rgba(55,134,251,0.55)', href: '/dashboard/expenses?add=1', x: 0, y: -90 },
          ]
          const petals = isAdmin ? adminPetals : memberPetals

          return (
            <div className="relative flex flex-col items-center" style={{ zIndex: 51 }}>

              {/* Backdrop */}
              <AnimatePresence>
                {showQuickAdd && (
                  <motion.div
                    key="backdrop"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    transition={{ duration: 0.18 }}
                    className="fixed inset-0 z-40 bg-black/40 backdrop-blur-[3px]"
                    onClick={() => setShowQuickAdd(false)}
                  />
                )}
              </AnimatePresence>

              {/* Petal items — all start at FAB center, animate outward */}
              <AnimatePresence>
                {showQuickAdd && petals.map((petal, i) => (
                  <motion.div
                    key={petal.id}
                    className="absolute z-50 flex flex-col items-center gap-1 pointer-events-auto"
                    style={{ bottom: 28, left: '50%', marginLeft: -22 }}
                    initial={{ x: 0, y: 0, scale: 0, opacity: 0 }}
                    animate={{ x: petal.x, y: petal.y, scale: 1, opacity: 1 }}
                    exit={{ x: 0, y: 0, scale: 0, opacity: 0 }}
                    transition={{ type: 'spring', stiffness: 420, damping: 22, delay: i * 0.06 }}
                  >
                    <Link href={petal.href} onClick={() => setShowQuickAdd(false)}>
                      <motion.div
                        whileTap={{ scale: 0.88 }}
                        className={`w-11 h-11 rounded-full ${petal.bg} flex items-center justify-center`}
                        style={{ boxShadow: `0 6px 22px ${petal.glow}` }}
                      >
                        <petal.Icon size={18} className="text-white" />
                      </motion.div>
                    </Link>
                    <motion.span
                      initial={{ opacity: 0, y: 4 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: i * 0.06 + 0.12 }}
                      className="text-[9px] font-extrabold text-white drop-shadow-md whitespace-nowrap px-1.5 py-0.5 rounded-full bg-black/50 backdrop-blur-sm"
                    >
                      {petal.label}
                    </motion.span>
                  </motion.div>
                ))}
              </AnimatePresence>

              {/* FAB button — mic icon; tap = voice, long-press = petals (VoiceButton owns all interactions) */}
              <motion.button
                className="relative w-14 h-14 rounded-full flex items-center justify-center -mt-6 border-4 border-card z-50 outline-none select-none"
                style={{
                  WebkitTapHighlightColor: 'transparent',
                  background: (voice.state.status === 'listening' || voice.state.status === 'processing')
                    ? 'linear-gradient(135deg, #4f46e5, #7c3aed)'
                    : showQuickAdd
                    ? 'linear-gradient(135deg, #374151, #1f2937)'
                    : 'linear-gradient(135deg, #7c3aed, #4f46e5)',
                  boxShadow: (voice.state.status === 'listening')
                    ? '0 4px 28px rgba(124,58,237,0.75)'
                    : showQuickAdd
                    ? '0 4px 16px rgba(0,0,0,0.4)'
                    : '0 4px 24px rgba(124,58,237,0.55)',
                }}
              >
                {/* Pulse ring — only when idle and closed */}
                {!showQuickAdd && voice.state.status === 'idle' && (
                  <motion.span
                    className="absolute inset-0 rounded-full bg-primary"
                    animate={{ scale: [1, 1.6, 1.6], opacity: [0.5, 0, 0] }}
                    transition={{ duration: 2.2, repeat: Infinity, ease: 'easeOut' }}
                  />
                )}
                <Mic size={22} className="text-white" />

                {/* Voice overlay — short tap = voice, long-press (500ms) = petals */}
                <VoiceButton
                  size="fab"
                  onTap={handleVoiceTap}
                  onLongPress={() => setShowQuickAdd(v => !v)}
                  isListening={voice.state.status === 'listening'}
                  isProcessing={voice.state.status === 'processing' || processor.isProcessing}
                  enabled={voiceEnabled}
                />
              </motion.button>

              <span className="text-[10px] font-semibold text-muted-foreground mt-0.5">
                {voice.state.status === 'listening' ? 'Listening…' : voice.state.status === 'processing' ? 'Thinking…' : 'Voice'}
              </span>
            </div>
          )
        })()}

        {/* 4 — Tasks (admin + member) */}
        <MobileNavLink {...NAV_ITEMS.admin[0]} badge={pendingSwaps > 0 ? pendingSwaps : undefined} />

        {/* 5 — Profile (admin + member) */}
        {(() => {
          const isActive = pathname.startsWith('/dashboard/profile')
          return (
            <Link href="/dashboard/profile" className={`relative flex flex-col items-center gap-1 px-3 py-1 transition-colors ${isActive ? 'text-primary' : 'text-muted-foreground'}`}>
              {isActive && (
                <span className="absolute -top-2 left-1/2 -translate-x-1/2 w-6 h-[3px] rounded-full bg-primary" />
              )}
              <div className={`relative p-1.5 rounded-xl transition-colors ${isActive ? 'bg-primary/10' : ''}`}>
                <div className={`w-[20px] h-[20px] rounded-full flex items-center justify-center text-[10px] font-extrabold shrink-0 ${isActive ? 'bg-primary text-white' : 'bg-secondary text-foreground'}`}>
                  {user?.displayName?.charAt(0)?.toUpperCase() || 'U'}
                </div>
                {isPremium && !isActive && (
                  <div className="absolute -top-0.5 -right-0.5 w-3.5 h-3.5 rounded-full flex items-center justify-center" style={{ background: 'linear-gradient(135deg, #f59e0b, #d97706)' }}>
                    <Crown size={7} className="text-white" />
                  </div>
                )}
              </div>
              <span className="text-[10px] font-semibold">{isPremium ? 'Premium' : 'Profile'}</span>
            </Link>
          )
        })()}
      </nav>
    </div>
  )
}
