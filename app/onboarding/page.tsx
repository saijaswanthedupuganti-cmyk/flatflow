"use client"
import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/useAuthStore'
import { useFlatStore } from '@/store/useFlatStore'
import { createFlat, joinFlat } from '@/lib/flatService'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Home, LogIn, Sparkles, ArrowLeft, Loader2 } from 'lucide-react'

type Step = 'choose' | 'create' | 'join'

export default function OnboardingPage() {
  const router = useRouter()
  const { user, setFlatId, logout } = useAuthStore()
  const { initFirestoreListeners, addMemberMock } = useFlatStore()

  const [step, setStep] = useState<Step>('choose')
  const [flatName, setFlatName] = useState('')
  const [nickname, setNickname] = useState(user?.displayName || '')
  const [inviteCode, setInviteCode] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  if (!user) return null

  const handleCreateFlat = async () => {
    if (!flatName.trim() || !nickname.trim()) return
    setLoading(true)
    setError('')
    try {
      const flatId = await createFlat({
        uid: user.uid,
        nickname: nickname.trim(),
        email: user.email,
        flatName: flatName.trim(),
      })
      // In mock mode, add this user to the store members
      addMemberMock(user.uid, nickname.trim(), user.email, 'admin')
      setFlatId(flatId)
      initFirestoreListeners(flatId)
      router.push('/dashboard')
    } catch (e: any) {
      setError(e.message || 'Failed to create flat. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleJoinFlat = async () => {
    const code = inviteCode.trim().toUpperCase()
    if (!code || !nickname.trim()) return
    setLoading(true)
    setError('')
    try {
      const result = await joinFlat({
        uid: user.uid,
        nickname: nickname.trim(),
        email: user.email,
        flatId: code,
      })
      if (!result.success) {
        setError(result.error || 'Failed to join flat.')
        setLoading(false)
        return
      }
      // In mock mode, add this user to the store members
      addMemberMock(user.uid, nickname.trim(), user.email, 'member')
      setFlatId(code)
      initFirestoreListeners(code)
      router.push('/dashboard')
    } catch (e: any) {
      setError(e.message || 'Failed to join flat. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-secondary/30 p-4">
      <div className="w-full max-w-md space-y-4">

        {/* Header */}
        <div className="text-center mb-6">
          <div className="w-14 h-14 bg-primary rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg rotate-3">
            <span className="text-2xl font-extrabold text-white">F</span>
          </div>
          <h1 className="text-3xl font-extrabold text-primary">FlatFlow</h1>
          <p className="text-muted-foreground mt-1">Welcome, <strong>{user.displayName}</strong>! Let's set up your home.</p>
        </div>

        {step === 'choose' && (
          <div className="grid gap-4">
            <button
              className="group text-left p-6 bg-card border-2 border-border hover:border-primary hover:bg-primary/5 rounded-2xl transition-all shadow-sm"
              onClick={() => setStep('create')}
            >
              <div className="flex items-start gap-4">
                <div className="w-12 h-12 bg-primary/10 group-hover:bg-primary/20 rounded-xl flex items-center justify-center shrink-0 transition-colors">
                  <Sparkles size={22} className="text-primary" />
                </div>
                <div>
                  <h2 className="text-lg font-bold">Create a New Flat</h2>
                  <p className="text-sm text-muted-foreground mt-0.5">
                    Start fresh. You'll become the admin and get a unique invite code to share with roommates.
                  </p>
                </div>
              </div>
            </button>

            <button
              className="group text-left p-6 bg-card border-2 border-border hover:border-primary hover:bg-primary/5 rounded-2xl transition-all shadow-sm"
              onClick={() => setStep('join')}
            >
              <div className="flex items-start gap-4">
                <div className="w-12 h-12 bg-secondary group-hover:bg-primary/10 rounded-xl flex items-center justify-center shrink-0 transition-colors">
                  <LogIn size={22} className="text-muted-foreground group-hover:text-primary transition-colors" />
                </div>
                <div>
                  <h2 className="text-lg font-bold">Join an Existing Flat</h2>
                  <p className="text-sm text-muted-foreground mt-0.5">
                    Have an invite code from your roommate? Enter it here to join their flat.
                  </p>
                </div>
              </div>
            </button>

            <div className="pt-2 text-center">
              <button
                onClick={() => logout().then(() => router.push('/'))}
                className="text-sm text-muted-foreground hover:text-destructive transition-colors"
              >
                Sign out and use a different account
              </button>
            </div>
          </div>
        )}

        {step === 'create' && (
          <Card className="shadow-md border-primary/30">
            <CardHeader>
              <button onClick={() => setStep('choose')} className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground mb-2 transition-colors">
                <ArrowLeft size={14} /> Back
              </button>
              <CardTitle className="flex items-center gap-2"><Home size={20} className="text-primary" /> Create Your Flat</CardTitle>
              <CardDescription>You'll be the admin. Invite roommates with the code we generate.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {error && (
                <div className="bg-destructive/10 text-destructive text-sm p-3 rounded border border-destructive/20 font-medium">
                  {error}
                </div>
              )}
              <div className="space-y-1">
                <label className="text-sm font-bold text-muted-foreground">Your Nickname</label>
                <input
                  type="text"
                  value={nickname}
                  onChange={(e) => setNickname(e.target.value)}
                  placeholder="e.g. Sai"
                  className="w-full bg-background border border-input rounded-md px-3 py-2 text-sm"
                />
              </div>
              <div className="space-y-1">
                <label className="text-sm font-bold text-muted-foreground">Flat Name</label>
                <input
                  type="text"
                  value={flatName}
                  onChange={(e) => setFlatName(e.target.value)}
                  placeholder="e.g. Bachelor Pad, Room 42"
                  className="w-full bg-background border border-input rounded-md px-3 py-2 text-sm"
                />
              </div>
              <Button
                className="w-full h-12 font-bold"
                disabled={!flatName.trim() || !nickname.trim() || loading}
                onClick={handleCreateFlat}
              >
                {loading ? <><Loader2 size={18} className="mr-2 animate-spin" /> Creating...</> : 'Create Flat & Continue'}
              </Button>
            </CardContent>
          </Card>
        )}

        {step === 'join' && (
          <Card className="shadow-md border-border">
            <CardHeader>
              <button onClick={() => setStep('choose')} className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground mb-2 transition-colors">
                <ArrowLeft size={14} /> Back
              </button>
              <CardTitle className="flex items-center gap-2"><LogIn size={20} className="text-primary" /> Join a Flat</CardTitle>
              <CardDescription>Enter the invite code your roommate shared with you.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {error && (
                <div className="bg-destructive/10 text-destructive text-sm p-3 rounded border border-destructive/20 font-medium">
                  {error}
                </div>
              )}
              <div className="space-y-1">
                <label className="text-sm font-bold text-muted-foreground">Your Nickname</label>
                <input
                  type="text"
                  value={nickname}
                  onChange={(e) => setNickname(e.target.value)}
                  placeholder="e.g. Rahul"
                  className="w-full bg-background border border-input rounded-md px-3 py-2 text-sm"
                />
              </div>
              <div className="space-y-1">
                <label className="text-sm font-bold text-muted-foreground">Invite Code</label>
                <input
                  type="text"
                  value={inviteCode}
                  onChange={(e) => setInviteCode(e.target.value.toUpperCase())}
                  placeholder="e.g. FLAT-A3B9"
                  className="w-full bg-background border border-input rounded-md px-3 py-2 text-sm font-mono tracking-widest text-center text-lg"
                />
              </div>
              <Button
                className="w-full h-12 font-bold"
                disabled={!inviteCode.trim() || !nickname.trim() || loading}
                onClick={handleJoinFlat}
              >
                {loading ? <><Loader2 size={18} className="mr-2 animate-spin" /> Joining...</> : 'Join Flat & Continue'}
              </Button>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}
