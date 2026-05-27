"use client"
import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { Button } from "@/components/ui/button"
import { useAuthStore } from '@/store/useAuthStore'
import { ShieldCheck, Mail, Loader2 } from 'lucide-react'
import { hasKeys } from '@/lib/firebase'

export default function Home() {
  const router = useRouter()
  const { user, loginWithGoogle, loginWithEmail, signUpWithEmail, loginAsAdminMock, loginAsMemberMock, redirectError, clearRedirectError } = useAuthStore()

  const [isSignUp, setIsSignUp] = useState(false)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [nickname, setNickname] = useState('')
  const [error, setError] = useState('')
  const [googleLoading, setGoogleLoading] = useState(false)

  useEffect(() => {
    if (user) {
      router.push('/dashboard')
    }
  }, [user, router])

  // Show any error that came back from the Google redirect flow (mobile sign-in)
  useEffect(() => {
    if (redirectError) {
      setError(redirectError)
      clearRedirectError()
    }
  }, [redirectError, clearRedirectError])

  /** Maps Firebase error codes to safe, user-friendly messages.
   *  We intentionally avoid revealing whether an email exists (prevents enumeration). */
  function getAuthErrorMessage(err: unknown): string {
    const code = (err as { code?: string })?.code ?? ''
    if (code.includes('user-not-found') || code.includes('wrong-password') || code.includes('invalid-credential')) {
      return 'Invalid email or password.'
    }
    if (code.includes('email-already-in-use')) {
      return 'An account with this email already exists.'
    }
    if (code.includes('weak-password')) {
      return 'Password must be at least 6 characters.'
    }
    if (code.includes('too-many-requests')) {
      return 'Too many attempts. Please wait a few minutes and try again.'
    }
    if (code.includes('network-request-failed')) {
      return 'Network error. Check your internet connection.'
    }
    if (code.includes('unauthorized-domain')) {
      return 'Google sign-in is not enabled for this domain yet. Please use email/password login for now.'
    }
    if (code.includes('account-exists-with-different-credential')) {
      return 'An account already exists with this email. Try signing in with email/password instead.'
    }
    if (code.includes('popup-closed-by-user') || code.includes('cancelled-popup-request')) {
      return '' // User dismissed — not an error to show
    }
    return 'Authentication failed. Please try again.'
  }

  const handleGoogleLogin = async () => {
    setError('')
    setGoogleLoading(true)
    try {
      await loginWithGoogle()
      // Page will navigate away to Google — googleLoading stays true until then
    } catch (err) {
      console.error("Google login failed:", err)
      const msg = getAuthErrorMessage(err)
      if (msg) setError(msg)
      setGoogleLoading(false)
    }
  }

  const handleEmailAuth = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      if (isSignUp) {
        if (!nickname.trim()) {
          setError('Nickname is required')
          return
        }
        await signUpWithEmail(email, password, nickname)
      } else {
        await loginWithEmail(email, password)
      }
    } catch (err: unknown) {
      console.error("Auth error:", err)
      setError(getAuthErrorMessage(err))
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-secondary/30 p-4">
      <div className="max-w-md w-full bg-card rounded-2xl shadow-xl overflow-hidden border border-border">
        <div className="bg-primary/10 p-8 flex flex-col items-center justify-center text-center">
          <div className="w-16 h-16 bg-primary rounded-2xl flex items-center justify-center mb-6 shadow-lg rotate-3">
            <span className="text-3xl font-extrabold text-white">F</span>
          </div>
          <h1 className="text-3xl font-extrabold text-primary tracking-tight">FlatFlow</h1>
          <p className="text-muted-foreground mt-2 font-medium">Shared living, perfectly balanced.</p>
        </div>
        
        <div className="p-8 space-y-6">
          <div className="text-center">
            <h2 className="text-xl font-bold mb-1">{isSignUp ? 'Create an Account' : 'Welcome Home'}</h2>
            <p className="text-sm text-muted-foreground">{isSignUp ? 'Join your flatmates today.' : 'Sign in to sync your duties'}</p>
          </div>

          <form onSubmit={handleEmailAuth} className="space-y-4">
            {error && (
              <div className="bg-destructive/10 text-destructive text-sm p-3 rounded border border-destructive/20 text-center font-bold">
                {error}
              </div>
            )}
            
            {isSignUp && (
              <div className="space-y-1">
                <label className="text-sm font-bold text-muted-foreground">Nickname</label>
                <input
                  type="text"
                  required
                  maxLength={30}
                  value={nickname}
                  onChange={(e) => setNickname(e.target.value)}
                  placeholder="e.g. Arjun"
                  className="w-full bg-background border border-input rounded-md px-3 py-2 text-sm"
                />
              </div>
            )}

            <div className="space-y-1">
              <label className="text-sm font-bold text-muted-foreground">Email Address</label>
              <input
                type="email"
                required
                maxLength={254}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="roommate@example.com"
                className="w-full bg-background border border-input rounded-md px-3 py-2 text-sm"
              />
            </div>

            <div className="space-y-1">
              <label className="text-sm font-bold text-muted-foreground">Password</label>
              <input
                type="password"
                required
                minLength={6}
                maxLength={128}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full bg-background border border-input rounded-md px-3 py-2 text-sm"
              />
            </div>

            <Button type="submit" className="w-full h-12 text-base font-bold">
              <Mail size={18} className="mr-2" />
              {isSignUp ? 'Sign Up' : 'Sign In'}
            </Button>
            
            <div className="text-center text-sm">
              <button 
                type="button" 
                onClick={() => setIsSignUp(!isSignUp)}
                className="text-primary font-bold hover:underline"
              >
                {isSignUp ? 'Already have an account? Sign in' : "Don't have an account? Sign up"}
              </button>
            </div>
          </form>

          <div className="relative pt-2">
            <div className="absolute inset-0 flex items-center"><span className="w-full border-t border-border" /></div>
            <div className="relative flex justify-center text-xs uppercase"><span className="bg-card px-2 text-muted-foreground">Or</span></div>
          </div>

          <div className="space-y-4">
            <Button
              type="button"
              disabled={googleLoading}
              className="w-full h-12 text-base font-bold bg-white text-black hover:bg-gray-100 border border-gray-300 disabled:opacity-70"
              onClick={handleGoogleLogin}
            >
              {googleLoading
                ? <><Loader2 size={18} className="mr-2 animate-spin" /> Connecting…</>
                : <><img src="/google-icon.svg" className="w-5 h-5 mr-3" alt="Google" />Continue with Google</>
              }
            </Button>
            
            {!hasKeys && (
              <>
                <div className="relative mt-4">
                  <div className="absolute inset-0 flex items-center"><span className="w-full border-t border-border" /></div>
                  <div className="relative flex justify-center text-xs uppercase"><span className="bg-card px-2 text-muted-foreground">Local Mock Mode</span></div>
                </div>
                <div className="flex gap-2">
                  <Button 
                    type="button"
                    className="flex-1 h-10 text-sm font-bold bg-secondary text-secondary-foreground hover:bg-secondary/80"
                    onClick={loginAsAdminMock}
                  >
                    Mock Admin
                  </Button>
                  <Button 
                    type="button"
                    className="flex-1 h-10 text-sm font-bold bg-secondary text-secondary-foreground hover:bg-secondary/80"
                    onClick={loginAsMemberMock}
                  >
                    Mock Roommate
                  </Button>
                </div>
              </>
            )}
          </div>
          
          <div className="pt-6 border-t border-border flex items-center justify-center gap-2 text-sm text-muted-foreground">
            <ShieldCheck size={16} className={hasKeys ? "text-green-500" : "text-orange-500"} />
            <span>{hasKeys ? 'Secure Firebase Authentication' : 'Running in Local Mock Mode'}</span>
          </div>
        </div>
      </div>
    </div>
  )
}
