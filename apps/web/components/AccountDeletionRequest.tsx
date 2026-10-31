'use client'

import { useEffect, useRef, useState } from 'react'
import { GoogleAuthProvider, onAuthStateChanged, signInWithEmailAndPassword, signInWithPopup, signOut, type User } from 'firebase/auth'
import { getFunctions, httpsCallable } from 'firebase/functions'
import { app, auth, hasKeys } from '@/lib/firebase'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

export default function AccountDeletionRequest() {
  const [user, setUser] = useState<User | null>(null)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [done, setDone] = useState(false)
  const dialog = useRef<HTMLDialogElement>(null)
  useEffect(() => {
    if (auth && hasKeys) return onAuthStateChanged(auth, setUser)
  }, [])
  async function login(google: boolean) {
    if (!auth || busy) return
    setBusy(true); setError('')
    try {
      if (google) await signInWithPopup(auth, new GoogleAuthProvider())
      else await signInWithEmailAndPassword(auth, email.trim(), password)
      setPassword('')
    } catch { setError('Sign-in did not finish. Check your connection and credentials, then retry.') }
    finally { setBusy(false) }
  }
  async function requestDeletion() {
    if (!app || !auth || busy) return
    setBusy(true); setError('')
    try {
      await httpsCallable(getFunctions(app, 'asia-south1'), 'requestAccountDeletion')({})
      dialog.current?.close(); setDone(true); await signOut(auth)
    } catch (failure) {
      const code = (failure as { code?: string }).code
      setError(code === 'functions/failed-precondition'
        ? 'For account protection, sign out and sign in again, then request deletion.'
        : 'Could not confirm the deletion request. Please retry; do not assume it was completed.')
      dialog.current?.close()
    } finally { setBusy(false) }
  }
  if (!hasKeys || !auth) return <p>Online account deletion is unavailable in this preview. Use the app or the support contact below.</p>
  if (done) return <p role="status">Your deletion request was accepted. You are signed out. Server cleanup continues even if you close this page.</p>
  return <div className="max-w-md space-y-4">
    <p>Sign in to the account you want to delete. This uses the same deletion service as the Android app.</p>
    {user ? <>
      <p>Signed in as <strong>{user.email}</strong></p>
      <Button disabled={busy} variant="destructive" onClick={() => dialog.current?.showModal()}>Request account deletion</Button>
      <Button disabled={busy} variant="outline" onClick={() => signOut(auth)}>Sign out</Button>
    </> : <form className="space-y-3" onSubmit={event => { event.preventDefault(); void login(false) }}>
      <Button type="button" disabled={busy} onClick={() => void login(true)}>Continue with Google</Button>
      <label className="block" htmlFor="deletion-email">Email address</label>
      <Input id="deletion-email" type="email" autoComplete="email" required value={email} onChange={e => setEmail(e.target.value)} />
      <label className="block" htmlFor="deletion-password">Password</label>
      <Input id="deletion-password" type="password" autoComplete="current-password" required value={password} onChange={e => setPassword(e.target.value)} />
      <Button type="submit" disabled={busy}>Sign in with email</Button>
    </form>}
    {busy && <p role="status">Please wait…</p>}
    {error && <p role="alert" className="text-red-700">{error}</p>}
    <dialog ref={dialog} aria-labelledby="deletion-title" className="m-auto max-w-md rounded-xl border p-6 backdrop:bg-black/40">
      <h3 id="deletion-title" className="text-xl font-semibold">Permanently delete this account?</h3>
      <p className="my-4">You will leave your flats and lose access. Shared accounting history remains available to remaining members. This cannot be undone.</p>
      <div className="flex flex-wrap gap-3">
        <Button disabled={busy} variant="outline" onClick={() => dialog.current?.close()}>Cancel</Button>
        <Button disabled={busy} variant="destructive" onClick={() => void requestDeletion()}>Request deletion</Button>
      </div>
    </dialog>
  </div>
}
