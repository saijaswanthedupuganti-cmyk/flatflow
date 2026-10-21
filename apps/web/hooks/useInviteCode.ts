'use client'

import { useEffect, useState } from 'react'
import { getInviteCode } from '@/lib/flatService'
import { useAuthStore } from '@/store/useAuthStore'

/**
 * The current flat's invite code (not its id: flat ids are visible in Discover listings).
 * Null while loading or in mock mode, where there is no code to share.
 */
export function useInviteCode(flatId: string | null | undefined): string | null {
  const uid = useAuthStore((s) => s.user?.uid)
  const [code, setCode] = useState<{ flatId: string; code: string } | null>(null)

  useEffect(() => {
    if (!flatId || !uid) return
    let cancelled = false
    getInviteCode(flatId, uid)
      .then((c) => { if (!cancelled && c) setCode({ flatId, code: c }) })
      .catch(() => {})
    return () => { cancelled = true }
  }, [flatId, uid])

  return code && code.flatId === flatId ? code.code : null
}
