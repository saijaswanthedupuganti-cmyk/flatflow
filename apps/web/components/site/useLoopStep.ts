'use client'
import { useEffect, useRef, useState } from 'react'
import { useInView } from 'framer-motion'
import { useCalm } from './useCalm'

/** Shortest time the finished frame stays up before a loop replays, so a glance always lands on a complete screen. */
const HOLD_MS = 2400

/**
 * Cycles through 0..steps-1 while the element is on screen. It starts on (and lingers on) the last, finished
 * step, so a phone never first appears half-empty; reduced motion shows only that step.
 */
export function useLoopStep(steps: number, intervalMs: number) {
  const ref = useRef<HTMLDivElement | null>(null)
  const inView = useInView(ref, { amount: 0.3 })
  const reduce = useCalm()
  const [step, setStep] = useState(steps - 1)

  useEffect(() => {
    if (reduce || !inView || steps < 2) return
    const wait = step === steps - 1 ? Math.max(intervalMs, HOLD_MS) : intervalMs
    const id = window.setTimeout(() => setStep(s => (s + 1) % steps), wait)
    return () => window.clearTimeout(id)
  }, [reduce, inView, steps, intervalMs, step])

  return { ref, step: reduce ? steps - 1 : step }
}
