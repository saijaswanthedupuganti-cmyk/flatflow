"use client"
import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/useAuthStore'
import { useFlatStore } from '@/store/useFlatStore'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  Home, LogOut, Shield, ShieldCheck, Users, Star, ArrowRight,
  Copy, Check, LogIn, Plus, Building2, ChevronRight,
} from 'lucide-react'

export default function ProfilePage() {
  const router = useRouter()
  const { user, allFlats, flatId: authFlatId, switchFlat, logout } = useAuthStore()
  const { members, name: currentFlatName, flatId, resetFlatData, initFirestoreListeners } = useFlatStore()

  const [switching, setSwitching] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)

  const currentMember = members.find(m => m.uid === user?.uid)
  const isAdminInCurrentFlat = currentMember?.role === 'admin'

  const activeMembers   = members.filter(m => m.status !== 'inactive')
  const admins          = members.filter(m => m.role === 'admin')
  const regularMembers  = members.filter(m => m.role === 'member' && m.status !== 'inactive')
  const otherFlats      = allFlats.filter(f => f.id !== authFlatId)

  const handleSwitchFlat = async (newFlatId: string) => {
    if (newFlatId === authFlatId || switching) return
    setSwitching(newFlatId)
    try {
      resetFlatData()
      await switchFlat(newFlatId)
      initFirestoreListeners(newFlatId)
      router.push('/dashboard')
    } finally {
      setSwitching(null)
    }
  }

  const handleLogout = async () => {
    resetFlatData()
    await logout()
    router.push('/')
  }

  const handleCopyCode = () => {
    if (!flatId) return
    navigator.clipboard.writeText(flatId).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    })
  }

  const reliabilityColor =
    (currentMember?.reliabilityScore ?? 100) >= 80 ? 'text-green-600 dark:text-green-400' :
    (currentMember?.reliabilityScore ?? 100) >= 50 ? 'text-yellow-600 dark:text-yellow-400' :
    'text-red-500'

  return (
    <div className="space-y-6 max-w-2xl">

      {/* ── Header ──────────────────────────────────────────────────── */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">My Profile</h1>
        <p className="text-muted-foreground mt-0.5 text-sm">Your account, your flats, your role.</p>
      </div>

      {/* ── Personal Info ────────────────────────────────────────────── */}
      <Card className="shadow-sm">
        <CardContent className="p-6">
          <div className="flex items-center gap-5">
            <div className="w-20 h-20 rounded-full bg-primary flex items-center justify-center text-3xl font-extrabold text-white shrink-0">
              {user?.displayName?.charAt(0)?.toUpperCase() || 'U'}
            </div>
            <div className="min-w-0 flex-1">
              <h2 className="text-2xl font-extrabold truncate">{user?.displayName || 'User'}</h2>
              <p className="text-sm text-muted-foreground truncate mt-0.5">{user?.email}</p>
              <div className="flex flex-wrap items-center gap-2 mt-2">
                <span className={`flex items-center gap-1 text-xs font-bold px-2.5 py-1 rounded-full ${
                  isAdminInCurrentFlat
                    ? 'bg-primary/10 text-primary'
                    : 'bg-secondary text-muted-foreground'
                }`}>
                  <Shield size={11} />
                  {isAdminInCurrentFlat ? 'Admin' : 'Member'}
                </span>
                <span className={`flex items-center gap-1 text-xs font-bold px-2.5 py-1 rounded-full ${
                  currentMember?.status === 'available'
                    ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400'
                    : currentMember?.status === 'out_of_station'
                    ? 'bg-orange-100 dark:bg-orange-900/30 text-orange-700 dark:text-orange-400'
                    : 'bg-secondary text-muted-foreground'
                }`}>
                  {currentMember?.status?.replace(/_/g, ' ') ?? 'Unknown'}
                </span>
              </div>
            </div>
          </div>

          {/* Stats row */}
          <div className="grid grid-cols-3 gap-3 mt-5 pt-5 border-t border-border">
            <div className="text-center">
              <p className={`text-2xl font-extrabold ${reliabilityColor}`}>{currentMember?.reliabilityScore ?? 100}</p>
              <p className="text-[11px] text-muted-foreground font-semibold mt-0.5">Reliability</p>
            </div>
            <div className="text-center">
              <p className="text-2xl font-extrabold">{allFlats.length}</p>
              <p className="text-[11px] text-muted-foreground font-semibold mt-0.5">Flats</p>
            </div>
            <div className="text-center">
              <p className="text-2xl font-extrabold text-primary">{isAdminInCurrentFlat ? allFlats.length : 0}</p>
              <p className="text-[11px] text-muted-foreground font-semibold mt-0.5">Admin Of</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* ── Current Flat Details ──────────────────────────────────────── */}
      <div>
        <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-3">
          Current Flat
        </p>

        <Card className="shadow-sm border-primary/20">
          <CardHeader className="pb-3 px-5 pt-5">
            <div className="flex items-center justify-between gap-3">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-primary flex items-center justify-center shrink-0">
                  <Home size={16} className="text-white" />
                </div>
                <div>
                  <CardTitle className="text-base">{currentFlatName || 'My Flat'}</CardTitle>
                  <p className="text-[11px] text-muted-foreground font-mono mt-0.5">{flatId}</p>
                </div>
              </div>
              <span className={`text-[10px] font-extrabold px-2.5 py-1 rounded-full ${
                isAdminInCurrentFlat ? 'bg-primary/10 text-primary' : 'bg-secondary text-muted-foreground'
              }`}>
                {isAdminInCurrentFlat ? 'ADMIN' : 'MEMBER'}
              </span>
            </div>
          </CardHeader>
          <CardContent className="px-5 pb-5 space-y-4">

            {/* Member count */}
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Users size={14} />
              <span><strong className="text-foreground">{activeMembers.length}</strong> active members</span>
              {admins.length > 0 && (
                <span>· <strong className="text-primary">{admins.length}</strong> admin{admins.length > 1 ? 's' : ''}</span>
              )}
            </div>

            {/* Member list */}
            <div className="space-y-2">
              {/* Admins first */}
              {admins.map(m => (
                <div key={m.uid} className={`flex items-center gap-3 p-3 rounded-xl border ${
                  m.uid === user?.uid ? 'border-primary/30 bg-primary/5' : 'border-border bg-secondary/20'
                }`}>
                  <div className={`w-9 h-9 rounded-full flex items-center justify-center text-sm font-bold shrink-0 ${
                    m.uid === user?.uid ? 'bg-primary text-white' : 'bg-secondary text-foreground'
                  }`}>
                    {m.nickname.charAt(0).toUpperCase()}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-1.5">
                      <p className="text-sm font-semibold truncate">{m.nickname}</p>
                      {m.uid === user?.uid && <span className="text-[9px] font-bold text-primary bg-primary/10 px-1.5 py-0.5 rounded-full">YOU</span>}
                    </div>
                    <p className="text-[11px] text-muted-foreground capitalize">{m.status.replace(/_/g, ' ')}</p>
                  </div>
                  <div className="flex items-center gap-1.5 shrink-0">
                    <ShieldCheck size={12} className="text-primary" />
                    <span className="text-[10px] font-bold text-primary">Admin</span>
                    {m.status === 'out_of_station' && (
                      <span className="text-[9px] font-bold bg-orange-100 dark:bg-orange-900/30 text-orange-600 px-1.5 py-0.5 rounded-full">OOS</span>
                    )}
                  </div>
                </div>
              ))}
              {/* Regular members */}
              {regularMembers.map(m => (
                <div key={m.uid} className={`flex items-center gap-3 p-3 rounded-xl border ${
                  m.uid === user?.uid ? 'border-primary/30 bg-primary/5' : 'border-border bg-secondary/20'
                }`}>
                  <div className={`w-9 h-9 rounded-full flex items-center justify-center text-sm font-bold shrink-0 ${
                    m.uid === user?.uid ? 'bg-primary text-white' : 'bg-secondary text-foreground'
                  }`}>
                    {m.nickname.charAt(0).toUpperCase()}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-1.5">
                      <p className="text-sm font-semibold truncate">{m.nickname}</p>
                      {m.uid === user?.uid && <span className="text-[9px] font-bold text-primary bg-primary/10 px-1.5 py-0.5 rounded-full">YOU</span>}
                    </div>
                    <p className="text-[11px] text-muted-foreground capitalize">{m.status.replace(/_/g, ' ')}</p>
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    <div className="flex items-center gap-1">
                      <Star size={10} className="text-muted-foreground" />
                      <span className="text-[11px] font-bold text-muted-foreground">{m.reliabilityScore}</span>
                    </div>
                    {m.status === 'out_of_station' && (
                      <span className="text-[9px] font-bold bg-orange-100 dark:bg-orange-900/30 text-orange-600 px-1.5 py-0.5 rounded-full">OOS</span>
                    )}
                  </div>
                </div>
              ))}
            </div>

            {/* Invite code (admin only) */}
            {isAdminInCurrentFlat && (
              <div className="pt-2 border-t border-border space-y-2">
                <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">Invite Code</p>
                <div className="flex items-center gap-3 bg-secondary/50 border border-border rounded-xl px-4 py-3">
                  <code className="text-lg font-mono font-bold text-primary tracking-widest flex-1">{flatId || '—'}</code>
                  <button onClick={handleCopyCode}
                    className="flex items-center gap-1.5 text-xs font-semibold text-muted-foreground hover:text-foreground transition-colors shrink-0">
                    {copied ? <><Check size={13} className="text-green-500" /> Copied</> : <><Copy size={13} /> Copy</>}
                  </button>
                </div>
              </div>
            )}

            {/* Manage Flat link (admin only) */}
            {isAdminInCurrentFlat && (
              <button
                onClick={() => router.push('/dashboard/manage-flat')}
                className="w-full flex items-center gap-3 px-4 py-3 rounded-xl bg-primary/5 hover:bg-primary/10 border border-primary/20 transition-colors text-left"
              >
                <Building2 size={16} className="text-primary shrink-0" />
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-bold text-primary">Manage Flat</p>
                  <p className="text-[11px] text-muted-foreground">Join mode, invite settings, NPS feedback</p>
                </div>
                <ChevronRight size={14} className="text-primary shrink-0" />
              </button>
            )}
          </CardContent>
        </Card>
      </div>

      {/* ── Other Flats ──────────────────────────────────────────────── */}
      {otherFlats.length > 0 && (
        <div>
          <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-3">
            Other Flats
          </p>
          <div className="space-y-2">
            {otherFlats.map(flat => (
              <Card key={flat.id} className="shadow-sm">
                <CardContent className="p-4">
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-xl bg-secondary flex items-center justify-center shrink-0">
                      <Home size={14} className="text-muted-foreground" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-semibold truncate">{flat.name || flat.id}</p>
                      <p className="text-[11px] text-muted-foreground font-mono truncate">{flat.id}</p>
                    </div>
                    <Button
                      size="sm"
                      variant="outline"
                      className="shrink-0 gap-1.5 text-xs"
                      disabled={switching === flat.id}
                      onClick={() => handleSwitchFlat(flat.id)}
                    >
                      {switching === flat.id ? 'Switching…' : <><ArrowRight size={12} /> Switch</>}
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      )}

      {/* ── Join / Create ─────────────────────────────────────────────── */}
      <div className="flex gap-2">
        <Button variant="outline" size="sm"
          className="flex-1 gap-1.5 text-blue-600 border-blue-200 hover:bg-blue-50 dark:text-blue-400 dark:border-blue-800 dark:hover:bg-blue-950/30"
          onClick={() => router.push('/onboarding?addFlat=1&mode=join')}>
          <LogIn size={14} /> Join a flat
        </Button>
        <Button variant="outline" size="sm"
          className="flex-1 gap-1.5 text-green-600 border-green-200 hover:bg-green-50 dark:text-green-400 dark:border-green-800 dark:hover:bg-green-950/30"
          onClick={() => router.push('/onboarding?addFlat=1&mode=create')}>
          <Plus size={14} /> Create a flat
        </Button>
      </div>

      {/* ── Sign Out ─────────────────────────────────────────────────── */}
      <Card className="shadow-sm">
        <CardContent className="p-4">
          <Button
            variant="outline"
            className="w-full h-11 text-destructive border-destructive/40 hover:bg-destructive/10 hover:border-destructive font-semibold gap-2"
            onClick={handleLogout}
          >
            <LogOut size={16} />
            Sign Out
          </Button>
        </CardContent>
      </Card>
    </div>
  )
}
