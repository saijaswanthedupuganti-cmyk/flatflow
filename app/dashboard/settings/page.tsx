"use client"
import { useState, useEffect } from 'react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  Sun, Moon, Shield, User, Info,
  LogOut, AlertTriangle, DoorOpen, ShieldCheck, X,
} from 'lucide-react'
import { useRouter } from 'next/navigation'

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
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground ml-2 shrink-0">
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

export default function SettingsPage() {
  const router = useRouter()
  const { name, members, leaveFlat, transferAdmin, deleteFlat } = useFlatStore()
  const { user, logout } = useAuthStore()

  const currentMember = members.find(m => m.uid === user?.uid)
  const isAdmin = currentMember?.role === 'admin'
  const otherMembers = members.filter(m => m.uid !== user?.uid)
  const hasOtherMembers = otherMembers.length > 0

  const [isDark, setIsDark] = useState(false)
  const [showLeaveConfirm, setShowLeaveConfirm] = useState(false)
  const [showTransferAdmin, setShowTransferAdmin] = useState(false)
  const [showDeleteFlat, setShowDeleteFlat] = useState(false)
  const [selectedNewAdmin, setSelectedNewAdmin] = useState('')
  const [leaveLoading, setLeaveLoading] = useState(false)
  const [leaveError, setLeaveError] = useState('')

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

  const handleLeaveClick = () => {
    setLeaveError('')
    if (isAdmin && hasOtherMembers) {
      setSelectedNewAdmin(otherMembers[0]?.uid || '')
      setShowTransferAdmin(true)
    } else if (isAdmin && !hasOtherMembers) {
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
      const { nextFlatId } = await leaveFlat(user.uid)
      if (nextFlatId) {
        router.push('/dashboard')
      } else {
        await logout()
        router.push('/onboarding')
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
      await transferAdmin(selectedNewAdmin, user.uid)
      const { nextFlatId } = await leaveFlat(user.uid)
      setShowTransferAdmin(false)
      if (nextFlatId) {
        router.push('/dashboard')
      } else {
        await logout()
        router.push('/onboarding')
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

  const flatDisplayName = name || 'My Flat'

  return (
    <>
      {/* Dialogs */}
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
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Settings</h1>
          <p className="text-muted-foreground mt-1">Your personal preferences and account.</p>
        </div>

        {/* ── Profile ──────────────────────────────────────────────────── */}
        <Card className="shadow-sm">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <User size={20} className="text-primary" />
              Your Profile
            </CardTitle>
            <CardDescription>Your account details and current status.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-5">
            <div className="flex items-center gap-4">
              <div className="w-16 h-16 rounded-full bg-primary flex items-center justify-center text-2xl font-bold text-white shrink-0">
                {user?.displayName?.charAt(0)?.toUpperCase() || 'U'}
              </div>
              <div className="min-w-0">
                <p className="text-xl font-bold truncate">{user?.displayName || 'User'}</p>
                <p className="text-sm text-muted-foreground truncate">{user?.email}</p>
                <div className="flex items-center gap-1 mt-1">
                  <Shield size={13} className={isAdmin ? 'text-primary' : 'text-muted-foreground'} />
                  <span className={`text-xs font-semibold capitalize ${isAdmin ? 'text-primary' : 'text-muted-foreground'}`}>
                    {isAdmin ? 'Admin' : 'Member'} · {flatDisplayName}
                  </span>
                </div>
              </div>
            </div>

            <div className="divide-y divide-border rounded-xl border border-border overflow-hidden">
              <div className="flex items-center justify-between px-4 py-3">
                <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Status</span>
                <span className={`text-sm font-semibold capitalize ${
                  currentMember?.status === 'available' ? 'text-green-600 dark:text-green-400' :
                  currentMember?.status === 'out_of_station' ? 'text-orange-500' :
                  'text-muted-foreground'
                }`}>
                  {currentMember?.status?.replace(/_/g, ' ') ?? '—'}
                </span>
              </div>
              <div className="flex items-center justify-between px-4 py-3">
                <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Reliability Score</span>
                <span className="text-sm font-bold text-primary">{currentMember?.reliabilityScore ?? 100}</span>
              </div>
            </div>

            <div className="pt-2 border-t border-border">
              <Button
                variant="outline"
                className="w-full h-11 text-destructive border-destructive/40 hover:bg-destructive/10 hover:border-destructive font-semibold gap-2"
                onClick={async () => { await logout(); router.push('/') }}
              >
                <LogOut size={16} />
                Sign Out
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* ── Appearance ───────────────────────────────────────────────── */}
        <Card className="shadow-sm">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              {isDark ? <Moon size={20} className="text-primary" /> : <Sun size={20} className="text-primary" />}
              Appearance
            </CardTitle>
            <CardDescription>Choose your preferred theme.</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex items-center justify-between">
              <div>
                <p className="font-semibold">Dark Mode</p>
                <p className="text-sm text-muted-foreground">Switch between light and dark theme.</p>
              </div>
              <button
                onClick={toggleDark}
                className={`relative inline-flex h-7 w-12 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 ${
                  isDark ? 'bg-primary' : 'bg-secondary border border-border'
                }`}
              >
                <span className={`inline-block h-5 w-5 transform rounded-full bg-white shadow-md transition-transform ${
                  isDark ? 'translate-x-6' : 'translate-x-1'
                }`} />
              </button>
            </div>
            <div className="grid grid-cols-2 gap-3 mt-4">
              <button
                onClick={() => { if (isDark) toggleDark() }}
                className={`flex items-center gap-3 p-4 rounded-xl border-2 transition-all ${
                  !isDark ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/50'
                }`}
              >
                <Sun size={20} className={!isDark ? 'text-primary' : 'text-muted-foreground'} />
                <div className="text-left">
                  <p className={`font-semibold text-sm ${!isDark ? 'text-primary' : ''}`}>Light</p>
                  <p className="text-xs text-muted-foreground">Clean & bright</p>
                </div>
              </button>
              <button
                onClick={() => { if (!isDark) toggleDark() }}
                className={`flex items-center gap-3 p-4 rounded-xl border-2 transition-all ${
                  isDark ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/50'
                }`}
              >
                <Moon size={20} className={isDark ? 'text-primary' : 'text-muted-foreground'} />
                <div className="text-left">
                  <p className={`font-semibold text-sm ${isDark ? 'text-primary' : ''}`}>Dark</p>
                  <p className="text-xs text-muted-foreground">Easy on the eyes</p>
                </div>
              </button>
            </div>
          </CardContent>
        </Card>

        {/* ── About ────────────────────────────────────────────────────── */}
        <Card className="shadow-sm border-dashed">
          <CardContent className="p-6">
            <div className="flex items-start gap-3">
              <Info size={18} className="text-muted-foreground mt-0.5 shrink-0" />
              <div>
                <p className="font-semibold text-sm">Habitiq v0.1.0</p>
                <p className="text-xs text-muted-foreground mt-1">
                  Shared living, perfectly balanced. Built for flatmates who want a fair, automated chore rotation system.
                </p>
                {isAdmin && (
                  <p className="text-xs text-primary font-semibold mt-2">
                    Manage your flat — invite code, join mode, feedback — in <strong>Manage Flat</strong> (Admin section).
                  </p>
                )}
              </div>
            </div>
          </CardContent>
        </Card>

        {/* ── Danger Zone ──────────────────────────────────────────────── */}
        <Card className="shadow-sm border-destructive/30">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-destructive">
              <AlertTriangle size={20} />
              Danger Zone
            </CardTitle>
            <CardDescription>
              Actions below apply to <span className="font-semibold text-foreground">{flatDisplayName}</span> and cannot be undone.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex items-center justify-between p-4 rounded-xl bg-destructive/5 border border-destructive/20">
              <div>
                <p className="font-semibold text-sm">Leave <span className="text-primary">{flatDisplayName}</span></p>
                <p className="text-xs text-muted-foreground mt-0.5">
                  {isAdmin && hasOtherMembers
                    ? 'You must transfer admin role before leaving.'
                    : isAdmin && !hasOtherMembers
                    ? 'You are the only member — leaving will permanently delete this flat.'
                    : 'Your tasks will be reassigned to the next person in rotation.'}
                </p>
              </div>
              <Button
                variant="outline"
                size="sm"
                className="border-destructive/40 text-destructive hover:bg-destructive hover:text-white shrink-0 ml-4 gap-1.5"
                onClick={handleLeaveClick}
              >
                <DoorOpen size={14} />
                Leave
              </Button>
            </div>

            {isAdmin && (
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
