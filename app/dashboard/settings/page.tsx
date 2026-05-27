"use client"
import { useState, useEffect } from 'react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  Copy, Check, Sun, Moon, Shield, User, Home, Info,
  LogOut, AlertTriangle, DoorOpen, Trash2, ShieldCheck, X
} from 'lucide-react'
import { useRouter } from 'next/navigation'
import FlatSwitcher from '@/components/FlatSwitcher'

// ── Simple dialog overlay ──────────────────────────────────────────────────
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
  const { flatId, name, members, leaveFlat, transferAdmin, deleteFlat } = useFlatStore()
  const { user, logout, allFlats } = useAuthStore()

  const handleLogout = async () => {
    await logout()
    router.push('/')
  }

  const currentMember = members.find(m => m.uid === user?.uid)
  const isAdmin = currentMember?.role === 'admin'
  const otherMembers = members.filter(m => m.uid !== user?.uid)
  const hasOtherMembers = otherMembers.length > 0

  const [copied, setCopied] = useState(false)
  const [isDark, setIsDark] = useState(false)

  // Leave flat dialog states
  const [showLeaveConfirm, setShowLeaveConfirm] = useState(false)
  const [showTransferAdmin, setShowTransferAdmin] = useState(false)
  const [showDeleteFlat, setShowDeleteFlat] = useState(false)
  const [selectedNewAdmin, setSelectedNewAdmin] = useState('')
  const [leaveLoading, setLeaveLoading] = useState(false)
  const [leaveError, setLeaveError] = useState('')

  // Sync dark mode from <html> class on mount
  useEffect(() => {
    setIsDark(document.documentElement.classList.contains('dark'))
  }, [])

  const toggleDark = () => {
    const newDark = !isDark
    setIsDark(newDark)
    if (newDark) {
      document.documentElement.classList.add('dark')
      localStorage.setItem('flatflow-theme', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      localStorage.setItem('flatflow-theme', 'light')
    }
  }

  const handleCopy = () => {
    if (!flatId) return
    navigator.clipboard.writeText(flatId).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    })
  }

  const handleLeaveClick = () => {
    setLeaveError('')
    if (isAdmin && hasOtherMembers) {
      // Must transfer admin first
      setSelectedNewAdmin(otherMembers[0]?.uid || '')
      setShowTransferAdmin(true)
    } else if (isAdmin && !hasOtherMembers) {
      // Last member — will delete flat
      setShowDeleteFlat(true)
    } else {
      // Regular member — simple confirm
      setShowLeaveConfirm(true)
    }
  }

  /** Regular member leaves */
  const handleConfirmLeave = async () => {
    if (!user) return
    setLeaveLoading(true)
    setLeaveError('')
    try {
      const { nextFlatId } = await leaveFlat(user.uid)
      if (nextFlatId) {
        // useAuthStore will be updated by FlatSwitcher or we push to dashboard
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

  /** Admin transfers role then leaves */
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

  /** Admin deletes entire flat (last member) */
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

  const activeMembers = members.filter(m => m.status !== 'inactive')
  const flatDisplayName = name || 'My Flat'

  return (
    <>
      {/* Dialogs */}
      <ConfirmDialog
        open={showLeaveConfirm}
        onClose={() => setShowLeaveConfirm(false)}
        title={`Leave ${flatDisplayName}?`}
        description="You will lose access to this flat. Your tasks will be reassigned. You can join again with an invite code."
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

      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Settings</h1>
          <p className="text-muted-foreground mt-1">Manage your flat and personal preferences.</p>
        </div>

        {/* Flat Info */}
        <Card className="shadow-sm">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Home size={20} className="text-primary" />
              Flat Information
            </CardTitle>
            <CardDescription>Details about your shared home.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-1">
                <p className="text-sm font-bold text-muted-foreground">Flat Name</p>
                <p className="text-lg font-semibold">{flatDisplayName}</p>
              </div>
              <div className="space-y-1">
                <p className="text-sm font-bold text-muted-foreground">Total Members</p>
                <p className="text-lg font-semibold">{activeMembers.length} roommates</p>
              </div>
            </div>

            {isAdmin && (
              <div className="pt-4 border-t border-border">
                <p className="text-sm font-bold text-muted-foreground mb-2">Invite Code</p>
                <p className="text-xs text-muted-foreground mb-3">
                  Share this code with roommates so they can join your flat.
                </p>
                <div className="flex items-center gap-3 bg-secondary/50 border border-border rounded-xl px-4 py-3">
                  <code className="text-lg font-mono font-bold text-primary tracking-widest flex-1">
                    {flatId || 'FLAT-1234'}
                  </code>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={handleCopy}
                    className="shrink-0 gap-2"
                  >
                    {copied ? <><Check size={14} className="text-green-500" /> Copied!</> : <><Copy size={14} /> Copy</>}
                  </Button>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Profile */}
        <Card className="shadow-sm">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <User size={20} className="text-primary" />
              Your Profile
            </CardTitle>
            <CardDescription>Your account information in this flat.</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex items-center gap-4">
              <div className="w-16 h-16 rounded-full bg-primary/20 flex items-center justify-center text-2xl font-bold text-primary">
                {user?.displayName?.charAt(0) || 'U'}
              </div>
              <div className="space-y-1">
                <p className="text-xl font-bold">{user?.displayName || 'User'}</p>
                <p className="text-sm text-muted-foreground">{user?.email}</p>
                <div className="flex items-center gap-1 mt-1">
                  <Shield size={14} className={isAdmin ? 'text-primary' : 'text-muted-foreground'} />
                  <span className={`text-xs font-semibold uppercase tracking-wider ${isAdmin ? 'text-primary' : 'text-muted-foreground'}`}>
                    {isAdmin ? 'Admin' : 'Member'}
                  </span>
                </div>
              </div>
            </div>

            {currentMember && (
              <div className="grid grid-cols-2 gap-4 mt-6 pt-6 border-t border-border">
                <div className="bg-secondary/40 rounded-xl p-4 text-center">
                  <p className="text-3xl font-extrabold text-primary">{currentMember.reliabilityScore}</p>
                  <p className="text-xs text-muted-foreground mt-1 uppercase tracking-wider font-medium">Reliability Score</p>
                </div>
                <div className="bg-secondary/40 rounded-xl p-4 text-center">
                  <p className="text-3xl font-extrabold capitalize">{currentMember.status.replace('_', ' ')}</p>
                  <p className="text-xs text-muted-foreground mt-1 uppercase tracking-wider font-medium">Current Status</p>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Flat Switcher — mobile only (desktop has it in sidebar) */}
        <Card className="shadow-sm md:hidden">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Home size={20} className="text-primary" />
              Switch Flat
            </CardTitle>
            <CardDescription>
              {allFlats.length > 1
                ? `You are in ${allFlats.length} flats. Tap to switch.`
                : 'Join or create another flat.'}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <FlatSwitcher />
          </CardContent>
        </Card>

        {/* Appearance */}
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

        {/* About */}
        <Card className="shadow-sm border-dashed">
          <CardContent className="p-6">
            <div className="flex items-start gap-3">
              <Info size={18} className="text-muted-foreground mt-0.5 shrink-0" />
              <div>
                <p className="font-semibold text-sm">FlatFlow v0.1.0</p>
                <p className="text-xs text-muted-foreground mt-1">
                  Shared living, perfectly balanced. Built for bachelors and flatmates who want a fair, automated chore rotation system.
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* ── Danger Zone ─────────────────────────────────────────────── */}
        <Card className="shadow-sm border-destructive/30">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-destructive">
              <AlertTriangle size={20} />
              Danger Zone
            </CardTitle>
            <CardDescription>
              These actions are permanent and cannot be undone.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {/* Leave flat */}
            <div className="flex items-center justify-between p-4 rounded-xl bg-destructive/5 border border-destructive/20">
              <div>
                <p className="font-semibold text-sm">Leave this flat</p>
                <p className="text-xs text-muted-foreground mt-0.5">
                  {isAdmin && hasOtherMembers
                    ? 'You must transfer admin role before leaving.'
                    : isAdmin && !hasOtherMembers
                    ? 'You are the only member — leaving will delete this flat.'
                    : 'Your tasks will be reassigned to the next person.'}
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

            {/* Admin-only: shows transfer + delete hint */}
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

        {/* Sign Out — shown on mobile (sidebar has FlatSwitcher with logout on desktop) */}
        <Card className="shadow-sm border-border md:hidden">
          <CardContent className="p-4">
            <Button
              variant="outline"
              className="w-full h-12 text-destructive border-destructive/40 hover:bg-destructive/10 hover:border-destructive font-semibold gap-2"
              onClick={handleLogout}
            >
              <LogOut size={18} />
              Sign Out
            </Button>
          </CardContent>
        </Card>
      </div>
    </>
  )
}
