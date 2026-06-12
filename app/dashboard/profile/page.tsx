"use client"
import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/useAuthStore'
import { useFlatStore } from '@/store/useFlatStore'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  Home, LogOut, Shield, ShieldCheck, Users, Star, ArrowRight,
  Copy, Check, LogIn, Plus, Building2, ChevronRight,
  Sun, Moon, AlertTriangle, DoorOpen, X, Download, Smartphone, Share, CheckCircle2, Info,
} from 'lucide-react'
import { usePWA } from '@/contexts/PWAContext'

interface DialogProps {
  open: boolean
  onClose: () => void
  title: string
  description: string
  confirmLabel: string
  confirmVariant?: 'destructive' | 'default'
  onConfirm: () => void
  loading?: boolean
  children?: React.ReactNode
}
function ConfirmDialog({ open, onClose, title, description, confirmLabel, confirmVariant = 'destructive', onConfirm, loading, children }: DialogProps) {
  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
      <div className="bg-card border border-border rounded-2xl shadow-xl w-full max-w-sm p-6 space-y-4">
        <div className="flex items-start justify-between">
          <div className="space-y-1">
            <h3 className="font-bold text-lg">{title}</h3>
            <p className="text-sm text-muted-foreground">{description}</p>
          </div>
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground ml-2 shrink-0 cursor-pointer">
            <X size={18} />
          </button>
        </div>
        {children && <div>{children}</div>}
        <div className="flex gap-3 pt-1">
          <Button variant="outline" className="flex-1" onClick={onClose} disabled={loading}>Cancel</Button>
          <Button
            className={`flex-1 font-semibold ${confirmVariant === 'destructive' ? 'bg-destructive hover:bg-destructive/90 text-white' : ''}`}
            onClick={onConfirm}
            disabled={loading}
          >
            {loading ? 'Please wait…' : confirmLabel}
          </Button>
        </div>
      </div>
    </div>
  )
}

export default function ProfilePage() {
  const router = useRouter()
  const { user, allFlats, flatId: authFlatId, switchFlat, logout } = useAuthStore()
  const { members, name: currentFlatName, flatId, resetFlatData, initFirestoreListeners, leaveFlat, transferAdmin, deleteFlat } = useFlatStore()

  const { canInstall, isInstalled, isIOS, triggerInstall } = usePWA()

  const [switching, setSwitching] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)
  const [isDark, setIsDark] = useState(false)
  const [showIOSSteps, setShowIOSSteps] = useState(false)
  const [showLeaveConfirm, setShowLeaveConfirm] = useState(false)
  const [showTransferAdmin, setShowTransferAdmin] = useState(false)
  const [showDeleteFlat, setShowDeleteFlat] = useState(false)
  const [selectedNewAdmin, setSelectedNewAdmin] = useState('')
  const [leaveLoading, setLeaveLoading] = useState(false)
  const [leaveError, setLeaveError] = useState('')

  const currentMember = members.find(m => m.uid === user?.uid)
  const isAdminInCurrentFlat = currentMember?.role === 'admin'
  const otherMembers = members.filter(m => m.uid !== user?.uid)
  const hasOtherMembers = otherMembers.length > 0
  const activeMembers = members.filter(m => m.status !== 'inactive')
  const admins = members.filter(m => m.role === 'admin')
  const regularMembers = members.filter(m => m.role === 'member' && m.status !== 'inactive')
  const otherFlats = allFlats.filter(f => f.id !== authFlatId)
  const flatDisplayName = currentFlatName || 'My Flat'

  useEffect(() => {
    setIsDark(document.documentElement.classList.contains('dark'))
  }, [])

  const toggleDark = () => {
    const newDark = !isDark
    setIsDark(newDark)
    if (newDark) {
      document.documentElement.classList.add('dark')
      localStorage.setItem('habitiq-theme', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      localStorage.setItem('habitiq-theme', 'light')
    }
  }

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

  const handleLeaveClick = () => {
    setLeaveError('')
    if (isAdminInCurrentFlat && hasOtherMembers) {
      setSelectedNewAdmin(otherMembers[0]?.uid || '')
      setShowTransferAdmin(true)
    } else if (isAdminInCurrentFlat && !hasOtherMembers) {
      setShowDeleteFlat(true)
    } else {
      setShowLeaveConfirm(true)
    }
  }

  const handleConfirmLeave = async () => {
    if (!user) return
    setLeaveLoading(true)
    setLeaveError('')
    try {
      try {
        const exits = JSON.parse(localStorage.getItem('habitiq_flat_exits') ?? '[]')
        exits.push({ flatId: authFlatId, flatName: currentFlatName ?? 'Your Flat', exitedAt: new Date().toISOString(), reason: 'left' })
        localStorage.setItem('habitiq_flat_exits', JSON.stringify(exits))
      } catch { /* ignore */ }
      const { nextFlatId } = await leaveFlat(user.uid)
      if (nextFlatId) {
        router.push('/dashboard')
      } else {
        await logout()
        router.push('/onboarding?left=1')
      }
    } catch (e: unknown) {
      setLeaveError(e instanceof Error ? e.message : 'Failed to leave flat. Please try again.')
    } finally {
      setLeaveLoading(false)
    }
  }

  const handleTransferAndLeave = async () => {
    if (!user || !selectedNewAdmin) return
    setLeaveLoading(true)
    setLeaveError('')
    try {
      try {
        const exits = JSON.parse(localStorage.getItem('habitiq_flat_exits') ?? '[]')
        exits.push({ flatId: authFlatId, flatName: currentFlatName ?? 'Your Flat', exitedAt: new Date().toISOString(), reason: 'left' })
        localStorage.setItem('habitiq_flat_exits', JSON.stringify(exits))
      } catch { /* ignore */ }
      await transferAdmin(selectedNewAdmin, user.uid)
      const { nextFlatId } = await leaveFlat(user.uid)
      setShowTransferAdmin(false)
      if (nextFlatId) {
        router.push('/dashboard')
      } else {
        await logout()
        router.push('/onboarding?left=1')
      }
    } catch (e: unknown) {
      setLeaveError(e instanceof Error ? e.message : 'Failed to transfer admin. Please try again.')
    } finally {
      setLeaveLoading(false)
    }
  }

  const handleDeleteFlat = async () => {
    if (!user) return
    setLeaveLoading(true)
    setLeaveError('')
    try {
      await deleteFlat(user.uid)
      setShowDeleteFlat(false)
      await logout()
      router.push('/onboarding')
    } catch (e: unknown) {
      setLeaveError(e instanceof Error ? e.message : 'Failed to delete flat. Please try again.')
    } finally {
      setLeaveLoading(false)
    }
  }

  const reliabilityColor =
    (currentMember?.reliabilityScore ?? 100) >= 80 ? 'text-green-600 dark:text-green-400' :
    (currentMember?.reliabilityScore ?? 100) >= 50 ? 'text-yellow-600 dark:text-yellow-400' :
    'text-red-500'

  return (
    <>
      <ConfirmDialog
        open={showLeaveConfirm}
        onClose={() => setShowLeaveConfirm(false)}
        title={`Leave ${flatDisplayName}?`}
        description="You will lose access to this flat. Your tasks will be reassigned. You can rejoin with an invite code."
        confirmLabel="Leave Flat"
        onConfirm={handleConfirmLeave}
        loading={leaveLoading}
      >
        {leaveError && <p className="text-sm text-destructive font-medium">{leaveError}</p>}
      </ConfirmDialog>

      <ConfirmDialog
        open={showTransferAdmin}
        onClose={() => setShowTransferAdmin(false)}
        title="Transfer Admin Role"
        description="You must choose a new admin before you can leave the flat."
        confirmLabel="Transfer & Leave"
        onConfirm={handleTransferAndLeave}
        loading={leaveLoading}
      >
        <div className="space-y-2">
          <label className="text-sm font-semibold">New Admin</label>
          <select
            className="w-full bg-background border border-input rounded-lg px-3 py-2 text-sm"
            value={selectedNewAdmin}
            onChange={e => setSelectedNewAdmin(e.target.value)}
          >
            {otherMembers.map(m => (
              <option key={m.uid} value={m.uid}>{m.nickname}</option>
            ))}
          </select>
          {leaveError && <p className="text-sm text-destructive font-medium">{leaveError}</p>}
        </div>
      </ConfirmDialog>

      <ConfirmDialog
        open={showDeleteFlat}
        onClose={() => setShowDeleteFlat(false)}
        title="Delete Flat Permanently?"
        description={`You are the only member of "${flatDisplayName}". Leaving will permanently delete this flat, all tasks, and all history. This cannot be undone.`}
        confirmLabel="Delete Flat"
        onConfirm={handleDeleteFlat}
        loading={leaveLoading}
      >
        {leaveError && <p className="text-sm text-destructive font-medium">{leaveError}</p>}
      </ConfirmDialog>

      <div className="space-y-6 max-w-2xl">

        {/* ── Header ── */}
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">Profile & Settings</h1>
          <p className="text-muted-foreground mt-0.5 text-sm">Your account, flats, and preferences.</p>
        </div>

        {/* ── Personal Info ── */}
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
                    isAdminInCurrentFlat ? 'bg-primary/10 text-primary' : 'bg-secondary text-muted-foreground'
                  }`}>
                    <Shield size={11} />
                    {isAdminInCurrentFlat ? 'Admin' : 'Member'}
                  </span>
                  <span className={`flex items-center gap-1 text-xs font-bold px-2.5 py-1 rounded-full ${
                    currentMember?.status === 'available' ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400' :
                    currentMember?.status === 'out_of_station' ? 'bg-orange-100 dark:bg-orange-900/30 text-orange-700 dark:text-orange-400' :
                    'bg-secondary text-muted-foreground'
                  }`}>
                    {currentMember?.status?.replace(/_/g, ' ') ?? 'Unknown'}
                  </span>
                </div>
              </div>
            </div>

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

        {/* ── Current Flat ── */}
        <div>
          <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-3">Current Flat</p>
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
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Users size={14} />
                <span><strong className="text-foreground">{activeMembers.length}</strong> active members</span>
                {admins.length > 0 && (
                  <span>· <strong className="text-primary">{admins.length}</strong> admin{admins.length > 1 ? 's' : ''}</span>
                )}
              </div>

              <div className="space-y-2">
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

              {isAdminInCurrentFlat && (
                <div className="pt-2 border-t border-border space-y-2">
                  <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">Invite Code</p>
                  <div className="flex items-center gap-3 bg-secondary/50 border border-border rounded-xl px-4 py-3">
                    <code className="text-lg font-mono font-bold text-primary tracking-widest flex-1">{flatId || '—'}</code>
                    <button onClick={handleCopyCode}
                      className="flex items-center gap-1.5 text-xs font-semibold text-muted-foreground hover:text-foreground transition-colors shrink-0 cursor-pointer">
                      {copied ? <><Check size={13} className="text-green-500" /> Copied</> : <><Copy size={13} /> Copy</>}
                    </button>
                  </div>
                </div>
              )}

              {isAdminInCurrentFlat && (
                <button
                  onClick={() => router.push('/dashboard/manage-flat')}
                  className="w-full flex items-center gap-3 px-4 py-3 rounded-xl bg-primary/5 hover:bg-primary/10 border border-primary/20 transition-colors text-left cursor-pointer"
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

        {/* ── Other Flats ── */}
        {otherFlats.length > 0 && (
          <div>
            <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-3">Other Flats</p>
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
                      <Button size="sm" variant="outline" className="shrink-0 gap-1.5 text-xs"
                        disabled={switching === flat.id} onClick={() => handleSwitchFlat(flat.id)}>
                        {switching === flat.id ? 'Switching…' : <><ArrowRight size={12} /> Switch</>}
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        )}

        {/* ── Join / Create ── */}
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

        {/* ── Appearance ── */}
        <Card className="shadow-sm">
          <CardHeader className="pb-3">
            <CardTitle className="flex items-center gap-2 text-base">
              {isDark ? <Moon size={18} className="text-primary" /> : <Sun size={18} className="text-primary" />}
              Appearance
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center justify-between">
              <div>
                <p className="font-semibold text-sm">Dark Mode</p>
                <p className="text-xs text-muted-foreground mt-0.5">Switch between light and dark theme.</p>
              </div>
              <button
                onClick={toggleDark}
                className={`relative inline-flex h-7 w-12 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 cursor-pointer ${
                  isDark ? 'bg-primary' : 'bg-secondary border border-border'
                }`}
              >
                <span className={`inline-block h-5 w-5 transform rounded-full bg-white shadow-md transition-transform ${
                  isDark ? 'translate-x-6' : 'translate-x-1'
                }`} />
              </button>
            </div>
          </CardContent>
        </Card>

        {/* ── Install App ── */}
        <Card className="shadow-sm">
          <CardHeader className="pb-3">
            <CardTitle className="flex items-center gap-2 text-base">
              <Smartphone size={18} />
              Install App
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {isInstalled ? (
              <div className="flex items-center gap-2.5 px-4 py-3 rounded-xl bg-green-50 dark:bg-green-950/20 border border-green-200 dark:border-green-900">
                <CheckCircle2 size={16} className="text-green-600 dark:text-green-400 shrink-0" />
                <p className="text-sm font-semibold text-green-700 dark:text-green-400">App installed ✓</p>
              </div>
            ) : isIOS ? (
              <div className="space-y-3">
                {!showIOSSteps ? (
                  <button onClick={() => setShowIOSSteps(true)}
                    className="w-full flex items-center justify-center gap-2 py-2.5 rounded-xl bg-primary text-primary-foreground text-sm font-bold hover:opacity-90 transition-opacity cursor-pointer">
                    <Download size={15} /> Add to Home Screen
                  </button>
                ) : (
                  <div className="space-y-3">
                    <ol className="space-y-3 list-none">
                      <li className="flex items-start gap-2.5">
                        <span className="w-5 h-5 rounded-full bg-primary text-white text-[10px] font-extrabold flex items-center justify-center shrink-0 mt-0.5">1</span>
                        <p className="text-sm text-muted-foreground leading-relaxed">
                          Tap the <Share size={13} className="inline mx-0.5 -mt-0.5" /> <strong>Share</strong> button at the bottom of Safari
                        </p>
                      </li>
                      <li className="flex items-start gap-2.5">
                        <span className="w-5 h-5 rounded-full bg-primary text-white text-[10px] font-extrabold flex items-center justify-center shrink-0 mt-0.5">2</span>
                        <p className="text-sm text-muted-foreground leading-relaxed">Scroll down and tap <strong>&ldquo;Add to Home Screen&rdquo;</strong></p>
                      </li>
                      <li className="flex items-start gap-2.5">
                        <span className="w-5 h-5 rounded-full bg-primary text-white text-[10px] font-extrabold flex items-center justify-center shrink-0 mt-0.5">3</span>
                        <p className="text-sm text-muted-foreground leading-relaxed">Tap <strong>&ldquo;Add&rdquo;</strong> in the top-right corner</p>
                      </li>
                    </ol>
                    <button onClick={() => setShowIOSSteps(false)}
                      className="text-xs font-semibold text-muted-foreground hover:text-foreground transition-colors cursor-pointer">
                      Close instructions
                    </button>
                  </div>
                )}
              </div>
            ) : canInstall ? (
              <button onClick={() => triggerInstall()}
                className="w-full flex items-center justify-center gap-2 py-2.5 rounded-xl bg-primary text-primary-foreground text-sm font-bold hover:opacity-90 transition-opacity cursor-pointer">
                <Download size={15} /> Install Habitiq
              </button>
            ) : (
              <div className="flex items-center gap-2.5 px-4 py-3 rounded-xl bg-secondary/60 border border-border">
                <Smartphone size={15} className="text-muted-foreground shrink-0" />
                <p className="text-xs text-muted-foreground">Open in Chrome (Android) or Safari (iOS) to install.</p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* ── About ── */}
        <Card className="shadow-sm border-dashed">
          <CardContent className="p-5">
            <div className="flex items-start gap-3">
              <Info size={16} className="text-muted-foreground mt-0.5 shrink-0" />
              <div>
                <p className="font-semibold text-sm">Habitiq v0.1.0</p>
                <p className="text-xs text-muted-foreground mt-1">
                  Shared living, perfectly balanced. Built for flatmates who want a fair, automated chore rotation system.
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* ── Sign Out ── */}
        <Card className="shadow-sm">
          <CardContent className="p-4">
            <Button
              variant="outline"
              className="w-full h-11 text-destructive border-destructive/40 hover:bg-destructive/10 hover:border-destructive font-semibold gap-2"
              onClick={handleLogout}
            >
              <LogOut size={16} /> Sign Out
            </Button>
          </CardContent>
        </Card>

        {/* ── Danger Zone ── */}
        <Card className="shadow-sm border-destructive/30">
          <CardHeader className="pb-2">
            <CardTitle className="flex items-center gap-2 text-destructive text-base">
              <AlertTriangle size={18} /> Danger Zone
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex items-center justify-between p-4 rounded-xl bg-destructive/5 border border-destructive/20">
              <div>
                <p className="font-semibold text-sm">Leave <span className="text-primary">{flatDisplayName}</span></p>
                <p className="text-xs text-muted-foreground mt-0.5">
                  {isAdminInCurrentFlat && hasOtherMembers
                    ? 'You must transfer admin role before leaving.'
                    : isAdminInCurrentFlat && !hasOtherMembers
                    ? 'You are the only member — leaving will permanently delete this flat.'
                    : 'Your tasks will be reassigned to the next person in rotation.'}
                </p>
              </div>
              <Button variant="outline" size="sm"
                className="border-destructive/40 text-destructive hover:bg-destructive hover:text-white shrink-0 ml-4 gap-1.5"
                onClick={handleLeaveClick}>
                <DoorOpen size={14} /> Leave
              </Button>
            </div>

            {isAdminInCurrentFlat && (
              <div className="flex items-start gap-3 px-4 py-3 rounded-xl bg-secondary/50 border border-border">
                <ShieldCheck size={15} className="text-primary mt-0.5 shrink-0" />
                <p className="text-xs text-muted-foreground">
                  <span className="font-semibold text-foreground">Admin tip: </span>
                  To remove a member, go to the <strong>Members</strong> page and use the Remove button on their card.
                </p>
              </div>
            )}
          </CardContent>
        </Card>

      </div>
    </>
  )
}
