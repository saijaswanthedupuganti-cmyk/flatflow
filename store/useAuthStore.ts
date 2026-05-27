import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { auth, hasKeys } from '@/lib/firebase'
import {
  GoogleAuthProvider,
  signInWithPopup,
  signInWithRedirect,
  getRedirectResult,
  browserPopupRedirectResolver,
  signOut as firebaseSignOut,
  onAuthStateChanged,
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
  updateProfile,
} from 'firebase/auth'
import { getUserFlatProfile } from '@/lib/flatService'

export interface AuthUser {
  uid: string
  displayName: string
  email: string
  photoURL?: string
}

interface AuthState {
  user: AuthUser | null
  isLoading: boolean
  flatId: string | null
  flatChecked: boolean
  redirectError: string | null

  loginWithGoogle: () => Promise<void>
  loginWithEmail: (email: string, pass: string) => Promise<void>
  signUpWithEmail: (email: string, pass: string, nickname: string) => Promise<void>
  loginAsAdminMock: () => void
  loginAsMemberMock: () => void
  logout: () => Promise<void>
  initAuthListener: () => void
  setFlatId: (flatId: string) => void
  refreshFlatId: () => Promise<void>
  clearRedirectError: () => void
}

/** True when running on a mobile browser — popups are blocked on iOS/Android */
function isMobileBrowser(): boolean {
  if (typeof navigator === 'undefined') return false
  return /iPhone|iPad|iPod|Android|webOS|BlackBerry|IEMobile|Opera Mini/i.test(
    navigator.userAgent
  )
}

function googleErrorMessage(code: string): string {
  if (code.includes('unauthorized-domain'))
    return 'Google sign-in is not enabled for this domain.'
  if (code.includes('account-exists-with-different-credential'))
    return 'An account already exists with this email. Try email/password instead.'
  if (code.includes('network-request-failed'))
    return 'Network error. Check your connection and try again.'
  if (code.includes('popup-closed-by-user') || code.includes('cancelled-popup-request'))
    return '' // user dismissed — show nothing
  return 'Google sign-in failed. Please try again.'
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      isLoading: true,
      flatId: null,
      flatChecked: false,
      redirectError: null,

      loginWithGoogle: async () => {
        if (!hasKeys) {
          alert('Firebase is not configured. Use mock login instead.')
          return
        }
        const provider = new GoogleAuthProvider()
        // Only request email + basic profile (name + photo). No extra permissions.
        provider.addScope('email')
        provider.addScope('profile')

        if (isMobileBrowser()) {
          // Mobile: full-page redirect — popups are blocked on iOS/Android.
          // Pass browserPopupRedirectResolver explicitly so Firebase uses the
          // correct resolver; this fixes redirect result processing on Next.js.
          await signInWithRedirect(auth, provider, browserPopupRedirectResolver)
        } else {
          // Desktop: popup is more reliable and gives better UX.
          await signInWithPopup(auth, provider, browserPopupRedirectResolver)
        }
      },

      loginWithEmail: async (email, pass) => {
        if (!hasKeys) {
          alert('Firebase is not configured. Use mock login instead.')
          return
        }
        await signInWithEmailAndPassword(auth, email, pass)
      },

      signUpWithEmail: async (email, pass, nickname) => {
        if (!hasKeys) {
          alert('Firebase is not configured. Use mock login instead.')
          return
        }
        const cred = await createUserWithEmailAndPassword(auth, email, pass)
        if (cred.user) {
          await updateProfile(cred.user, { displayName: nickname })
          set({
            user: {
              uid: cred.user.uid,
              displayName: nickname,
              email: cred.user.email || '',
              photoURL: cred.user.photoURL || undefined,
            },
          })
        }
      },

      loginAsAdminMock: () =>
        set({
          user: { uid: 'u1', displayName: 'Sai', email: 'sai@gmail.com' },
          flatId: 'FLAT-1234',
          flatChecked: true,
          isLoading: false,
        }),

      loginAsMemberMock: () =>
        set({
          user: { uid: 'u2', displayName: 'Rahul', email: 'rahul@gmail.com' },
          flatId: 'FLAT-1234',
          flatChecked: true,
          isLoading: false,
        }),

      logout: async () => {
        if (hasKeys && auth.currentUser) await firebaseSignOut(auth)
        set({ user: null, flatId: null, flatChecked: false, redirectError: null })
      },

      setFlatId: (flatId) => set({ flatId, flatChecked: true }),
      clearRedirectError: () => set({ redirectError: null }),

      refreshFlatId: async () => {
        const user = get().user
        if (!user) return
        if (!hasKeys) { set({ flatId: 'FLAT-1234', flatChecked: true }); return }
        const profile = await getUserFlatProfile(user.uid)
        set({ flatId: profile?.flatId ?? null, flatChecked: true })
      },

      initAuthListener: () => {
        if (!hasKeys) {
          set({ isLoading: false, flatChecked: true })
          return
        }

        // Process any pending Google redirect result (mobile sign-in).
        // Must pass the same browserPopupRedirectResolver used in signInWithRedirect.
        getRedirectResult(auth, browserPopupRedirectResolver)
          .then((result) => {
            if (result?.user) {
              console.log('Google redirect sign-in succeeded:', result.user.displayName)
              // onAuthStateChanged will fire automatically — no extra state update needed
            }
          })
          .catch((err: unknown) => {
            console.error('Google redirect error:', err)
            const code = (err as { code?: string })?.code ?? ''
            const msg = googleErrorMessage(code)
            if (msg) set({ redirectError: msg })
          })

        onAuthStateChanged(auth, async (firebaseUser) => {
          if (firebaseUser) {
            set({
              user: {
                uid: firebaseUser.uid,
                displayName: firebaseUser.displayName || 'User',
                email: firebaseUser.email || '',
                photoURL: firebaseUser.photoURL || undefined,
              },
              isLoading: false,
            })
            const profile = await getUserFlatProfile(firebaseUser.uid)
            set({ flatId: profile?.flatId ?? null, flatChecked: true })
          } else {
            set({ user: null, flatId: null, flatChecked: true, isLoading: false })
          }
        })
      },
    }),
    {
      name: 'flatflow-auth',
      partialize: (state) => ({
        user: state.user,
        flatId: state.flatId,
        flatChecked: state.flatChecked,
      }),
    }
  )
)
