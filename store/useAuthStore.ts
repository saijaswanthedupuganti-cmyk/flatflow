import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { auth, hasKeys } from '@/lib/firebase'
import {
  GoogleAuthProvider,
  signInWithPopup,
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
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      isLoading: true,
      flatId: null,
      flatChecked: false,

      loginWithGoogle: async () => {
        if (!hasKeys) {
          alert('Firebase is not configured! Use the mock login buttons instead.')
          return
        }
        const provider = new GoogleAuthProvider()
        await signInWithPopup(auth, provider)
        // onAuthStateChanged will fire and call refreshFlatId
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
        set({ user: null, flatId: null, flatChecked: false })
      },

      setFlatId: (flatId) => set({ flatId, flatChecked: true }),

      refreshFlatId: async () => {
        const user = get().user
        if (!user) return
        if (!hasKeys) {
          // Mock mode — always use FLAT-1234
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
      // Only persist what's needed for mock mode state
      partialize: (state) => ({
        user: state.user,
        flatId: state.flatId,
        flatChecked: state.flatChecked,
      }),
    }
  )
)
