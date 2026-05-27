"use client"
import { useState, useEffect } from 'react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Copy, Check, Sun, Moon, Shield, User, Home, Info, LogOut } from 'lucide-react'
import { useRouter } from 'next/navigation'

export default function SettingsPage() {
  const router = useRouter()
  const { flatId, name, members } = useFlatStore()
  const { user, logout } = useAuthStore()

  const handleLogout = async () => {
    await logout()
    router.push('/')
  }

  const currentMember = members.find(m => m.uid === user?.uid)
  const isAdmin = currentMember?.role === 'admin'

  const [copied, setCopied] = useState(false)
  const [isDark, setIsDark] = useState(false)

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

  const activeMembers = members.filter(m => m.status !== 'inactive')

  return (
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
              <p className="text-lg font-semibold">{name || 'Bachelor Pad'}</p>
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

      {/* Sign Out — shown on mobile (sidebar logout is desktop-only) */}
      <Card className="shadow-sm border-destructive/30 md:hidden">
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
  )
}
