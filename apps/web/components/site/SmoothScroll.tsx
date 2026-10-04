'use client'
import { ReactLenis } from 'lenis/react'
import 'lenis/dist/lenis.css'
import { MotionConfig, useReducedMotion } from 'framer-motion'

export default function SmoothScroll({ children }: { children: React.ReactNode }) {
  const reduce = useReducedMotion()
  const content = reduce ? <>{children}</> : (
    <ReactLenis root options={{ lerp: 0.1, smoothWheel: true, syncTouch: false }}>{children}</ReactLenis>
  )
  return <MotionConfig reducedMotion="user">{content}</MotionConfig>
}
