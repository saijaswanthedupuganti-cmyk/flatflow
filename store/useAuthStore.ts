import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { auth, hasKeys } from '@/lib/firebase'
import {
  GoogleAuthProvider,
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
        // Only request the minimum scopes: email + basic profile (name + photo).
        // Do NOT add extra scopes — we don't need calendar, drive, contacts, etc.
        provider.addScope('email')
        provider.addScope('profile')
        // Use full-page redirect on ALL devices — more reliable than popup
        // across browsers (avoids third-party cookie blocks, popup-closed errors).
        await signInWithRedirect(auth, provider)
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

        // Process the Google redirect result on every page load (desktop + mobile).
        // If the user just came back from Google OAuth this will resolve with their
        // credential; otherwise it resolves with null and we do nothing.
        getRedirectResult(auth)
          .then((result) => {
            if (result?.user) {
              // onAuthStateChanged fires automatically with the signed-in user —
              // no extra state update needed here.
              console.log('Google sign-in via redirect succeeded:', result.user.displayName)
            }
          })
          .catch((err: unknown) => {
            console.error('Google redirect error:', err)
            // Surface the error so the user isn't left staring at a blank login page
            const code: string = (err as { code?: string })?.code ?? ''
            let message = 'Google sign-in failed. Please try again.'
            if (code.includes('unauthorized-domain')) {
              message = 'Google sign-in is not enabled for this domain yet.'
            } else if (code.includes('account-exists-with-different-credential')) {
              message = 'An account already exists with this email. Try signing in with email/password instead.'
            } else if (code.includes('network-request-failed')) {
              message = 'Network error. Check your connection and try again.'
            } else if (code.includes('cancelled-popup-request') || code.includes('popup-closed-by-user')) {
              // These can occasionally fire during a redirect flow on some browsers — ignore them
              return
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
