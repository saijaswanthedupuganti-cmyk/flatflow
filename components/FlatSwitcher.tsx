"use client"
import { useState, useRef, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Check, ChevronDown, Plus, LogIn, LogOut, Home } from 'lucide-react'
import { useAuthStore } from '@/store/useAuthStore'
import { useFlatStore } from '@/store/useFlatStore'

export default function FlatSwitcher() {
  const router = useRouter()
  const { user, flatId, allFlats, switchFlat, logout } = useAuthStore()
  const { initFirestoreListeners, resetFlatData, isSynced } = useFlatStore()

  const [open, setOpen] = useState(false)
  const [switching, setSwitching] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  // Close dropdown when clicking outside
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const currentFlat = allFlats.find(f => f.id === flatId)
  const otherFlats = allFlats.filter(f => f.id !== flatId)

  const handleSwitch = async (newFlatId: string) => {
    if (newFlatId === flatId || switching) return
    setSwitching(true)
    setOpen(false)
    try {
      resetFlatData()
      await switchFlat(newFlatId)
      initFirestoreListeners(newFlatId)
      router.push('/dashboard')
    } finally {
      setSwitching(false)
    }
  }

  const handleLogout = async () => {
    setOpen(false)
    resetFlatData()
    await logout()
    router.push('/')
  }

  const flatName = currentFlat?.name ?? flatId ?? 'My Flat'

  return (
    <div className="relative" ref={ref}>
      {/* Trigger button */}
      <button
        onClick={() => setOpen(o => !o)}
        className="w-full flex items-center gap-2.5 px-3 py-2 rounded-xl bg-secondary/50 hover:bg-secondary transition-colors text-sm"
        aria-expanded={open}
        aria-haspopup="listbox"
      >
        <div className="w-6 h-6 rounded-md bg-primary/20 flex items-center justify-center shrink-0">
          <Home size={12} className="text-primary" />
        </div>
        <span className="flex-1 text-left font-semibold truncate">{switching ? 'Switching…' : flatName}</span>
        <ChevronDown
          size={14}
          className={`text-muted-foreground shrink-0 transition-transform duration-150 ${open ? 'rotate-180' : ''}`}
        />
      </button>

      {/* Dropdown */}
      {open && (
        <div className="absolute bottom-full left-0 right-0 mb-1 bg-card border border-border rounded-xl shadow-lg overflow-hidden z-50">

          {/* Current flat */}
          {currentFlat && (
            <div className="px-3 pt-3 pb-1">
              <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-1.5">Current Flat</p>
              <div className="flex items-center gap-2.5 px-2 py-2 rounded-lg bg-primary/8">
                <div className="w-6 h-6 rounded-md bg-primary flex items-center justify-center shrink-0">
                  <Home size={11} className="text-white" />
                </div>
                <span className="flex-1 text-sm font-semibold truncate">{currentFlat.name}</span>
                <Check size={13} className="text-primary shrink-0" />
              </div>
            </div>
          )}

          {/* Other flats */}
          {otherFlats.length > 0 && (
            <div className="px-3 pt-2 pb-1">
              <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-1.5">Switch To</p>
              {otherFlats.map(flat => (
                <button
                  key={flat.id}
                  onClick={() => handleSwitch(flat.id)}
                  className="w-full flex items-center gap-2.5 px-2 py-2 rounded-lg hover:bg-secondary/80 transition-colors text-left"
                >
                  <div className="w-6 h-6 rounded-md bg-secondary flex items-center justify-center shrink-0">
                    <Home size={11} className="text-muted-foreground" />
                  </div>
                  <span className="flex-1 text-sm font-medium truncate">{flat.name}</span>
                </button>
              ))}
            </div>
          )}

          {/* Add flat options */}
          <div className="px-3 pt-2 pb-2 border-t border-border/60 mt-1 space-y-0.5">
            <button
              onClick={() => { setOpen(false); router.push('/onboarding?addFlat=1&mode=join') }}
              className="w-full flex items-center gap-2.5 px-2 py-2 rounded-lg hover:bg-secondary/80 transition-colors text-left"
            >
              <div className="w-6 h-6 rounded-md bg-blue-500/15 flex items-center justify-center shrink-0">
                <LogIn size={11} className="text-blue-500" />
              </div>
              <span className="text-sm font-medium text-blue-600 dark:text-blue-400">Join another flat</span>
            </button>
            <button
              onClick={() => { setOpen(false); router.push('/onboarding?addFlat=1&mode=create') }}
              className="w-full flex items-center gap-2.5 px-2 py-2 rounded-lg hover:bg-secondary/80 transition-colors text-left"
            >
              <div className="w-6 h-6 rounded-md bg-green-500/15 flex items-center justify-center shrink-0">
                <Plus size={11} className="text-green-500" />
              </div>
              <span className="text-sm font-medium text-green-600 dark:text-green-400">Create another flat</span>
            </button>
          </div>

          {/* Sign out */}
          <div className="px-3 pb-3 border-t border-border/60 pt-2">
            <button
              onClick={handleLogout}
              className="w-full flex items-center gap-2.5 px-2 py-2 rounded-lg hover:bg-destructive/10 transition-colors text-left group"
            >
              <div className="w-6 h-6 rounded-md bg-secondary flex items-center justify-center shrink-0 group-hover:bg-destructive/20">
                <LogOut size={11} className="text-muted-foreground group-hover:text-destructive" />
              </div>
              <span className="text-sm font-medium text-muted-foreground group-hover:text-destructive">Sign Out</span>
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
