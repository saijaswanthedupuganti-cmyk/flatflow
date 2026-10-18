import type { Variants } from 'framer-motion'

/** Ease-out for entrances and state changes; DRAWER for sheets that slide in from an edge. */
export const EASE: [number, number, number, number] = [0.23, 1, 0.32, 1]
export const DRAWER: [number, number, number, number] = [0.32, 0.72, 0, 1]
export const DUR = { enter: 0.5, quick: 0.2 } as const

export const fadeUp: Variants = {
  hidden: { opacity: 0, y: 16 },
  // Delay comes in through `custom` (a variant transition would override a transition prop).
  show: (delay: number = 0) => ({ opacity: 1, y: 0, transition: { duration: DUR.enter, ease: EASE, delay } }),
}

export function staggerKids(gap = 0.06): Variants {
  return { hidden: {}, show: { transition: { staggerChildren: gap } } }
}
