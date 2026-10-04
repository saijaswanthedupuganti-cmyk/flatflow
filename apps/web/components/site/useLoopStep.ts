'use client'
import { useEffect, useRef, useState } from 'react'
import { useInView } from 'framer-motion'
import { useCalm } from './useCalm'

/** Cycles 0..steps-1 every intervalMs while the element is on screen; final step when motion is reduced. */
export function useLoopStep(steps: number, intervalMs: number) {
  const ref = useRef<HTMLDivElement | null>(null)
  const inView = useInView(ref, { amount: 0.3 })
  const reduce = useCalm()
  const [step, setStep] = useState(0)

  useEffect(() => {
    if (reduce || !inView || steps < 2) return
    const id = window.setInterval(() => setStep(s => (s + 1) % steps), intervalMs)
    return () => window.clearInterval(id)
  }, [reduce, inView, steps, intervalMs])

  return { ref, step: reduce ? steps - 1 : step }
}
