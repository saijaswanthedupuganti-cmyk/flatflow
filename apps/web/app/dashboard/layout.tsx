"use client"
import { useEffect } from 'react'
import Link from 'next/link'
import Image from 'next/image'
import { useRouter, usePathname } from 'next/navigation'
import {
  LayoutDashboard, ClipboardList, Users,
  Lightbulb, Info, ChevronRight, ShieldCheck, Repeat2, ChevronDown, Receipt,
  Search, UserRound,
} from 'lucide-react'
import { useAuthStore } from '@/store/useAuthStore'
import { useFlatStore } from '@/store/useFlatStore'
import NotificationToast from '@/components/NotificationToast'
import FlatSwitcher from '@/components/FlatSwitcher'
import SubscriptionGate from '@/components/SubscriptionGate'

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

type NavItem = typeof NAV_ITEMS.main[number]

function MobileNavLink({ item, pathname, badge }: { item: NavItem; pathname: string; badge?: number }) {
  const { href, icon: Icon, label, exact } = item
  const isActive = exact ? pathname === href : pathname.startsWith(href)
  return (
    <Link href={href} className={`flex min-h-12 min-w-16 flex-col items-center justify-center gap-1 rounded-xl px-3 py-1 transition-colors ${isActive ? 'bg-primary/10 text-primary' : 'text-muted-foreground'}`}>
      <div className="relative">
        <Icon size={22} />
        {badge ? <span className="absolute -right-2 -top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-primary px-1 text-[9px] font-bold text-white">{badge}</span> : null}
      </div>
      <span className="text-[10px] font-semibold">{label}</span>
    </Link>
  )
}

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const pathname = usePathname()
  const { user, flatId: authFlatId } = useAuthStore()
  const { members, tasks, swapRequests, joinRequests, initFirestoreListeners, isSynced, name: flatName, wasKicked, clearWasKicked } = useFlatStore()

  const currentUser = members.find(m => m.uid === user?.uid)
  const isAdmin = currentUser?.role === 'admin'

  useEffect(() => {
    if (user && authFlatId && !isSynced) {
      initFirestoreListeners(authFlatId)
    }
  }, [user, authFlatId, isSynced, initFirestoreListeners])

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

  return (
    <div className="flex h-screen bg-secondary/20">
      <NotificationToast />

      {/* ── Sidebar ─────────────────────────────────── */}
      <aside className="hidden md:flex flex-col w-64 bg-card border-r border-border/60 shadow-sm">

        {/* Logo */}
        <div className="px-4 pt-4 pb-3 border-b border-border/60">
          <div className="flex flex-col gap-1">
            <Image
              src="/habitiq-logo.svg"
              alt="Habitiq"
              width={95}
              height={31}
              loading="eager"
              className="object-contain object-left dark:brightness-0 dark:invert"
              style={{ height: '28px', width: 'auto' }}
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
          <SubscriptionGate>
            {children}
          </SubscriptionGate>
        </div>
      </main>

      {/* Mobile roots follow the shared Home · Discover · Manage · Profile contract. */}
      <nav aria-label="Primary" className="fixed bottom-0 left-0 right-0 z-50 flex items-center justify-around border-t border-border/60 bg-card/95 px-2 py-2 backdrop-blur md:hidden">
        <MobileNavLink item={{ ...NAV_ITEMS.main[0], label: 'Home' }} pathname={pathname} />
        <MobileNavLink item={{ href: '/', label: 'Discover', icon: Search, exact: true, color: 'text-primary', bg: 'bg-primary/10' }} pathname={pathname} />
        <MobileNavLink item={{ href: '/dashboard/tasks', label: 'Manage', icon: ClipboardList, exact: false, color: 'text-primary', bg: 'bg-primary/10' }} pathname={pathname} badge={pendingSwaps > 0 ? pendingSwaps : undefined} />
        <MobileNavLink item={{ href: '/dashboard/profile', label: 'Profile', icon: UserRound, exact: false, color: 'text-primary', bg: 'bg-primary/10' }} pathname={pathname} />
      </nav>
    </div>
  )
}
