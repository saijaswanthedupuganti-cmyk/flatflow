"use client"
import { useEffect } from 'react'
import Link from 'next/link'
import { useRouter, usePathname } from 'next/navigation'
import {
  LayoutDashboard, ClipboardList, Users, Settings,
  BarChart3, CalendarDays, Info, ChevronRight, ShieldCheck, Repeat2,
} from 'lucide-react'
import { useAuthStore } from '@/store/useAuthStore'
import { useFlatStore } from '@/store/useFlatStore'
import NotificationToast from '@/components/NotificationToast'
import FlatSwitcher from '@/components/FlatSwitcher'

const NAV_ITEMS = {
  main: [
    { href: '/dashboard', label: 'Dashboard', icon: LayoutDashboard, exact: true, color: 'text-blue-500', bg: 'bg-blue-500/10' },
    { href: '/dashboard/analytics', label: 'Analytics', icon: BarChart3, exact: false, color: 'text-purple-500', bg: 'bg-purple-500/10' },
    { href: '/dashboard/calendar', label: 'Calendar', icon: CalendarDays, exact: false, color: 'text-green-500', bg: 'bg-green-500/10' },
    { href: '/dashboard/swaps', label: 'Swaps', icon: Repeat2, exact: false, color: 'text-violet-500', bg: 'bg-violet-500/10' },
  ],
  admin: [
    { href: '/dashboard/tasks', label: 'Tasks & Rotation', icon: ClipboardList, exact: false, color: 'text-orange-500', bg: 'bg-orange-500/10' },
    { href: '/dashboard/members', label: 'Members', icon: Users, exact: false, color: 'text-pink-500', bg: 'bg-pink-500/10' },
  ],
  general: [
    { href: '/dashboard/settings', label: 'Settings', icon: Settings, exact: false, color: 'text-muted-foreground', bg: 'bg-secondary' },
    { href: '/dashboard/about', label: 'About', icon: Info, exact: false, color: 'text-muted-foreground', bg: 'bg-secondary' },
  ],
}

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const pathname = usePathname()
  const { user } = useAuthStore()
  const { members, tasks, swapRequests, initFirestoreListeners, isSynced, name: flatName } = useFlatStore()
  const { flatId: authFlatId } = useAuthStore()

  const currentUser = members.find(m => m.uid === user?.uid)
  const isAdmin = currentUser?.role === 'admin'

  useEffect(() => {
    if (user && authFlatId && !isSynced) {
      initFirestoreListeners(authFlatId)
    }
  }, [user, authFlatId, isSynced, initFirestoreListeners])

  if (!user) return null

  const overdueTasks    = tasks.filter(t => t.status === 'overdue').length
  const pendingSwaps    = swapRequests.filter(r => r.toUserId === user?.uid && r.status === 'pending').length

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

  const MobileNavLink = ({ href, icon: Icon, label, exact }: typeof NAV_ITEMS.main[0]) => {
    const isActive = exact ? pathname === href : pathname.startsWith(href)
    return (
      <Link href={href} className={`flex flex-col items-center gap-1 px-3 py-1 rounded-lg transition-colors ${isActive ? 'text-primary' : 'text-muted-foreground'}`}>
        <Icon size={22} />
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
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 bg-primary rounded-xl flex items-center justify-center shadow-sm">
              <span className="text-sm font-extrabold text-white">H</span>
            </div>
            <div>
              <h2 className="text-base font-extrabold text-foreground tracking-tight leading-none">Habitiq</h2>
              <p className="text-[10px] text-muted-foreground font-medium mt-0.5">{flatName || authFlatId || 'Loading…'}</p>
            </div>
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
              {NAV_ITEMS.admin.map(item => <NavLink key={item.href} {...item} />)}
            </>
          )}

          <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest px-3 mt-3 mb-1">General</p>
          {NAV_ITEMS.general.map(item => <NavLink key={item.href} {...item} />)}
        </nav>

        {/* User Footer — FlatSwitcher first, user info below */}
        <div className="p-3 border-t border-border/60 space-y-2">
          {/* Flat switcher (flat name, switch, sign out) */}
          <FlatSwitcher />
          {/* User info */}
          <div className="flex items-center gap-3 px-2 py-2 rounded-xl bg-secondary/50">
            <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-white font-bold text-sm shrink-0">
              {user?.displayName?.charAt(0)?.toUpperCase() || 'U'}
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-sm font-semibold truncate leading-tight">{user?.displayName || 'User'}</p>
              <div className="flex items-center gap-1 mt-0.5">
                <ShieldCheck size={11} className={isAdmin ? 'text-primary' : 'text-muted-foreground'} />
                <p className="text-[11px] text-muted-foreground capitalize">{isAdmin ? 'Admin' : 'Member'}</p>
              </div>
            </div>
          </div>
        </div>
      </aside>

      {/* ── Main Content ─────────────────────────────── */}
      <main className="flex-1 overflow-y-auto pb-20 md:pb-0">
        <div className="p-6 lg:p-8 max-w-6xl mx-auto">
          {children}
        </div>
      </main>

      {/* ── Mobile Bottom Nav ─────────────────────────── */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-card/95 backdrop-blur border-t border-border/60 flex justify-around items-center px-2 py-2 z-50">
        <MobileNavLink {...NAV_ITEMS.main[0]} />
        <MobileNavLink {...NAV_ITEMS.main[1]} />
        {/* Swaps with badge */}
        <Link href="/dashboard/swaps" className={`flex flex-col items-center gap-1 px-3 py-1 rounded-lg transition-colors relative ${pathname.startsWith('/dashboard/swaps') ? 'text-primary' : 'text-muted-foreground'}`}>
          <Repeat2 size={22} />
          <span className="text-[10px] font-semibold">Swaps</span>
          {pendingSwaps > 0 && (
            <span className="absolute -top-0.5 right-1 bg-violet-500 text-white text-[9px] font-extrabold px-1 py-0.5 rounded-full min-w-[16px] text-center leading-none">
              {pendingSwaps}
            </span>
          )}
        </Link>
        {isAdmin
          ? <MobileNavLink {...NAV_ITEMS.admin[0]} />
          : <MobileNavLink {...NAV_ITEMS.general[1]} />}
        <MobileNavLink {...NAV_ITEMS.general[0]} />
      </nav>
    </div>
  )
}
