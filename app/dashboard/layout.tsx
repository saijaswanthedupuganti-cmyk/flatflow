"use client"
import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useRouter, usePathname } from 'next/navigation'
import {
  LayoutDashboard, ClipboardList, Users,
  Lightbulb, Info, ChevronRight, ShieldCheck, Repeat2, ChevronDown, Receipt,
  Plus, X, RefreshCw,
} from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import { useAuthStore } from '@/store/useAuthStore'
import { useFlatStore } from '@/store/useFlatStore'
import NotificationToast from '@/components/NotificationToast'
import FlatSwitcher from '@/components/FlatSwitcher'

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
    { href: '/dashboard/about', label: 'About', icon: Info, exact: false, color: 'text-muted-foreground', bg: 'bg-secondary' },
  ],
}

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const pathname = usePathname()
  const { user, allFlats, flatId: authFlatId, logout } = useAuthStore()
  const { members, tasks, swapRequests, joinRequests, initFirestoreListeners, isSynced, name: flatName, resetFlatData, wasKicked, clearWasKicked } = useFlatStore()

  const currentUser = members.find(m => m.uid === user?.uid)
  const isAdmin = currentUser?.role === 'admin'

  const [showQuickAdd, setShowQuickAdd] = useState(false)

  useEffect(() => {
    if (user && authFlatId && !isSynced) {
      initFirestoreListeners(authFlatId)
    }
  }, [user, authFlatId, isSynced, initFirestoreListeners])

  useEffect(() => { setShowQuickAdd(false) }, [pathname])

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
      <Link href={href} className={`flex flex-col items-center gap-1 px-3 py-1 rounded-lg transition-colors ${isActive ? 'text-primary' : 'text-muted-foreground'}`}>
        <div className="relative">
          <Icon size={22} />
          {badge ? (
            <span className="absolute -top-1 -right-1.5 bg-violet-500 text-white text-[9px] font-extrabold w-4 h-4 rounded-full flex items-center justify-center leading-none">
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

      {/* ── Sidebar ─────────────────────────────────── */}
      <aside className="hidden md:flex flex-col w-64 bg-card border-r border-border/60 shadow-sm">

        {/* Logo */}
        <div className="px-4 pt-4 pb-3 border-b border-border/60">
          <div className="flex flex-col gap-1">
            <img
              src="/habitiq-logo.svg"
              alt="Habitiq"
              className="h-7 w-auto object-contain object-left dark:brightness-0 dark:invert"
            />
            <p className="text-[10px] text-muted-foreground font-medium">{flatName || authFlatId || 'Loading…'}</p>
          </div>
          {overdueTasks > 0 && (
            <div className="mt-3 flex items-center gap-2 bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-900 rounded-lg px-3 py-1.5">
              <span className="w-2 h-2 bg-red-500 rounded-full animate-pulse shrink-0" />
              <p className="text-xs font-semibold text-red-600 dark:text-red-400">{overdueTasks} overdue task{overdueTasks > 1 ? 's' : ''}</p>
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
                <NavLink
                  key={item.href}
                  {...item}
                  badge={item.href === '/dashboard/members' && pendingJoins > 0 ? pendingJoins : undefined}
                />
              ))}
            </>
          )}

          <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest px-3 mt-3 mb-1">General</p>
          {NAV_ITEMS.general.map(item => <NavLink key={item.href} {...item} />)}
        </nav>

        {/* User Footer */}
        <div className="p-3 border-t border-border/60 space-y-2">
          <FlatSwitcher />

          {/* Profile — navigates to full profile page */}
          <Link
            href="/dashboard/profile"
            className={`flex items-center gap-3 px-2 py-2 rounded-xl transition-colors ${
              pathname === '/dashboard/profile'
                ? 'bg-primary text-primary-foreground'
                : 'bg-secondary/50 hover:bg-secondary'
            }`}
          >
            <div className={`w-8 h-8 rounded-full flex items-center justify-center text-white font-bold text-sm shrink-0 ${
              pathname === '/dashboard/profile' ? 'bg-white/20' : 'bg-primary'
            }`}>
              {user?.displayName?.charAt(0)?.toUpperCase() || 'U'}
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-sm font-semibold truncate leading-tight">{user?.displayName || 'User'}</p>
              <div className="flex items-center gap-1 mt-0.5">
                <ShieldCheck size={11} className={pathname === '/dashboard/profile' ? 'text-primary-foreground/70' : isAdmin ? 'text-primary' : 'text-muted-foreground'} />
                <p className={`text-[11px] capitalize ${pathname === '/dashboard/profile' ? 'text-primary-foreground/70' : 'text-muted-foreground'}`}>
                  {isAdmin ? 'Admin' : 'Member'}
                </p>
              </div>
            </div>
            <ChevronDown size={13} className={`shrink-0 transition-transform -rotate-90 ${pathname === '/dashboard/profile' ? 'text-primary-foreground/60' : 'text-muted-foreground/60'}`} />
          </Link>
        </div>
      </aside>

      {/* ── Main Content ─────────────────────────────── */}
      <main className="flex-1 overflow-y-auto pb-20 md:pb-0">
        <div className="p-6 lg:p-8 max-w-6xl mx-auto">
          {children}
        </div>
      </main>

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

              {/* FAB button */}
              <motion.button
                onClick={() => setShowQuickAdd(v => !v)}
                whileTap={{ scale: 0.9 }}
                className="relative w-14 h-14 rounded-full flex items-center justify-center -mt-6 border-4 border-card z-50"
                style={{
                  background: showQuickAdd
                    ? 'linear-gradient(135deg, #374151, #1f2937)'
                    : 'linear-gradient(135deg, #7c3aed, #4f46e5)',
                  boxShadow: showQuickAdd
                    ? '0 4px 16px rgba(0,0,0,0.4)'
                    : '0 4px 24px rgba(124,58,237,0.55), 0 0 0 0 rgba(124,58,237,0)',
                }}
              >
                {/* Pulse ring — only when closed */}
                {!showQuickAdd && (
                  <motion.span
                    className="absolute inset-0 rounded-full bg-primary"
                    animate={{ scale: [1, 1.6, 1.6], opacity: [0.5, 0, 0] }}
                    transition={{ duration: 2.2, repeat: Infinity, ease: 'easeOut' }}
                  />
                )}
                <motion.div
                  animate={{ rotate: showQuickAdd ? 45 : 0 }}
                  transition={{ type: 'spring', stiffness: 340, damping: 22 }}
                >
                  <Plus size={24} className="text-white" />
                </motion.div>
                {pendingSwaps > 0 && !showQuickAdd && (
                  <span className="absolute -top-0.5 -right-0.5 bg-violet-500 text-white text-[9px] font-extrabold w-4 h-4 rounded-full flex items-center justify-center leading-none z-10">
                    {pendingSwaps}
                  </span>
                )}
              </motion.button>

              <span className="text-[10px] font-semibold text-muted-foreground mt-0.5">Quick Add</span>
            </div>
          )
        })()}

        {/* 4 — Tasks (admin + member) */}
        <MobileNavLink {...NAV_ITEMS.admin[0]} badge={pendingSwaps > 0 ? pendingSwaps : undefined} />

        {/* 5 — Profile */}
        {(() => {
          const isActive = pathname.startsWith('/dashboard/profile')
          return (
            <Link href="/dashboard/profile" className={`flex flex-col items-center gap-1 px-3 py-1 rounded-lg transition-colors ${isActive ? 'text-primary' : 'text-muted-foreground'}`}>
              <div className={`w-[22px] h-[22px] rounded-full flex items-center justify-center text-[10px] font-extrabold shrink-0 ${isActive ? 'bg-primary text-white' : 'bg-secondary text-foreground'}`}>
                {user?.displayName?.charAt(0)?.toUpperCase() || 'U'}
              </div>
              <span className="text-[10px] font-semibold">Profile</span>
            </Link>
          )
        })()}
      </nav>
    </div>
  )
}
