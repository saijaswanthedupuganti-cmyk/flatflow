'use client'
import { useSyncExternalStore } from 'react'
import { useReducedMotion } from 'framer-motion'

const subscribe = () => () => {}
const getHydrated = () => true
const getServerHydrated = () => false

/**
 * Reduced-motion preference that is safe to branch rendering on: false during the server render and the
 * hydration pass (the server can't know the preference), the real value right after.
 */
export function useCalm(): boolean {
  const reduce = useReducedMotion()
  const hydrated = useSyncExternalStore(subscribe, getHydrated, getServerHydrated)
  return Boolean(reduce) && hydrated
}
