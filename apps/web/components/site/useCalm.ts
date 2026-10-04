'use client'
import { useEffect, useState } from 'react'
import { useReducedMotion } from 'framer-motion'

/**
 * Reduced-motion preference that is safe to branch rendering on: false during the server render and the
 * hydration pass (the server can't know the preference), the real value right after.
 */
export function useCalm(): boolean {
  const reduce = useReducedMotion()
  const [hydrated, setHydrated] = useState(false)
  useEffect(() => setHydrated(true), [])
  return Boolean(reduce) && hydrated
}
