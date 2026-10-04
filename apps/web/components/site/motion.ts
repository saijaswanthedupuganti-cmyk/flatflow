import type { Variants } from 'framer-motion'

export const EASE: [number, number, number, number] = [0.22, 1, 0.36, 1]
export const DUR = { enter: 0.7, quick: 0.2 } as const

export const fadeUp: Variants = {
  hidden: { opacity: 0, y: 24 },
  show: { opacity: 1, y: 0, transition: { duration: DUR.enter, ease: EASE } },
}

export function staggerKids(gap = 0.07): Variants {
  return { hidden: {}, show: { transition: { staggerChildren: gap } } }
}
