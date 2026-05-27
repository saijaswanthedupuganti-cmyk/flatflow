import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { auth, hasKeys } from '@/lib/firebase'
import {
  GoogleAuthProvider,
  signInWithPopup,
  signInWithRedirect,
  getRedirectResult,
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
  /** flatId for the flat this user belongs to. null = not yet in a flat */
  flatId: string | null
  /** True once we've checked Firestore for the user's flat (prevents flicker) */
  flatChecked: boolean
  /**
   * Auth error surfaced from redirect flow (e.g. unauthorized domain).
   * The login page reads this and shows it to the user, then clears it.
   */
  redirectError: string | null

  loginWithGoogle: () => Promise<void>
  loginWithEmail: (email: string, pass: string) => Promise<void>
  signUpWithEmail: (email: string, pass: string, nickname: string) => Promise<void>
  loginAsAdminMock: () => void
  loginAsMemberMock: () => void
  logout: () => Promise<void>
  initAuthListener: () => void
  /** Called after flat creation / join to set the flatId in store */
  setFlatId: (flatId: string) => void
  /** Re-check the user's flat profile from Firestore */
  refreshFlatId: () => Promise<void>
  /** Clear the redirectError once the login page has displayed it */
  clearRedirectError: () => void
}

/** True when running on a mobile browser — popups are blocked on mobile */
function isMobileBrowser(): boolean {
  if (typeof navigator === 'undefined') return false
  return /iPhone|iPad|iPod|Android|webOS|BlackBerry|IEMobile|Opera Mini/i.test(
    navigator.userAgent
  )
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
          alert('Firebase is not configured! Use the mock login buttons instead.')
          return
        }
        const provider = new GoogleAuthProvider()
        if (isMobileBrowser()) {
          // Mobile: full-page redirect (popups are blocked on iOS/Android)
          await signInWithRedirect(auth, provider)
        } else {
          // Desktop: popup (better UX)
          await signInWithPopup(auth, provider)
        }
      },

      loginWithEmail: async (email, pass) => {
        if (!hasKeys) {
          alert('Firebase is not configured! Use the mock login buttons instead.')
          return
        }
        await signInWithEmailAndPassword(auth, email, pass)
      },

      signUpWithEmail: async (email, pass, nickname) => {
        if (!hasKeys) {
          alert('Firebase is not configured! Use the mock login buttons instead.')
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
        if (hasKeys && auth.currentUser) {
          await firebaseSignOut(auth)
        }
        set({ user: null, flatId: null, flatChecked: false, redirectError: null })
      },

      setFlatId: (flatId) => set({ flatId, flatChecked: true }),

      clearRedirectError: () => set({ redirectError: null }),

      refreshFlatId: async () => {
        const user = get().user
        if (!user) return
        if (!hasKeys) {
          set({ flatId: 'FLAT-1234', flatChecked: true })
          return
        }
        const profile = await getUserFlatProfile(user.uid)
        set({ flatId: profile?.flatId ?? null, flatChecked: true })
      },

      initAuthListener: () => {
        if (!hasKeys) {
          // Mock mode: mark auth as resolved so AuthProvider can enforce routing
          set({ isLoading: false, flatChecked: true })
          return
        }

        // Handle the result when returning from a mobile Google redirect
        getRedirectResult(auth)
          .then((result) => {
            if (result?.user) {
              // onAuthStateChanged fires automatically after this — no extra work needed
              console.log('Google redirect sign-in succeeded:', result.user.displayName)
            }
          })
          .catch((err: unknown) => {
            console.error('Google redirect error:', err)
            // Surface the error to the login page so users aren't left in silence
            const code: string = (err as { code?: string })?.code ?? ''
            let message = 'Google sign-in failed. Please try again.'
            if (code.includes('unauthorized-domain')) {
              message = 'This domain is not authorised for Google sign-in. Add it to Firebase → Authentication → Authorised Domains.'
            } else if (code.includes('account-exists-with-different-credential')) {
              message = 'An account already exists with this email using a different sign-in method.'
            } else if (code.includes('network-request-failed')) {
              message = 'Network error. Check your internet connection and try again.'
            }
            set({ redirectError: message })
          })

        onAuthStateChanged(auth, async (firebaseUser) => {
          if (firebaseUser) {
            const authUser: AuthUser = {
              uid: firebaseUser.uid,
              displayName: firebaseUser.displayName || 'User',
              email: firebaseUser.email || '',
              photoURL: firebaseUser.photoURL || undefined,
            }
            set({ user: authUser, isLoading: false })

            // Check if this user is already in a flat
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
