"use client"
import { useEffect, useRef } from 'react'

interface Props {
  isActive: boolean
  width?: number
  height?: number
}

const BAR_COUNT = 20

export default function WaveformVisualizer({ isActive, width = 200, height = 36 }: Props) {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const frameRef  = useRef<number>(0)
  const tRef      = useRef(0)

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    const dpr   = window.devicePixelRatio ?? 1
    canvas.width  = width  * dpr
    canvas.height = height * dpr
    canvas.style.width  = `${width}px`
    canvas.style.height = `${height}px`
    ctx.scale(dpr, dpr)

    const gap  = 2
    const barW = (width - gap * (BAR_COUNT - 1)) / BAR_COUNT
    const minH = 3
    const maxH = height - 4

    function draw() {
      if (!ctx) return
      ctx.clearRect(0, 0, width, height)

      if (!isActive) {
        frameRef.current = 0
        return
      }

      tRef.current += 0.055

      for (let i = 0; i < BAR_COUNT; i++) {
        const phase = i * 0.42
        const n =
          Math.sin(tRef.current       + phase)        * 0.48 +
          Math.sin(tRef.current * 1.7 + phase * 1.3)  * 0.32 +
          Math.sin(tRef.current * 2.5 + phase * 0.75) * 0.20

        const h = minH + ((n + 1) / 2) * (maxH - minH)
        const x = i * (barW + gap)
        const y = (height - h) / 2

        const grad = ctx.createLinearGradient(x, y, x, y + h)
        grad.addColorStop(0, 'rgba(167, 139, 250, 0.95)')
        grad.addColorStop(0.5, 'rgba(139, 92, 246, 0.80)')
        grad.addColorStop(1, 'rgba(109, 40, 217, 0.45)')
        ctx.fillStyle = grad

        ctx.beginPath()
        const r = Math.min(barW / 2, h / 2)
        ctx.roundRect(x, y, barW, h, r)
        ctx.fill()
      }

      frameRef.current = requestAnimationFrame(draw)
    }

    frameRef.current = requestAnimationFrame(draw)

    return () => {
      if (frameRef.current) {
        cancelAnimationFrame(frameRef.current)
        frameRef.current = 0
      }
    }
  }, [isActive, width, height])

  return (
    <canvas
      ref={canvasRef}
      style={{ display: 'block', imageRendering: 'crisp-edges' }}
      aria-hidden="true"
    />
  )
}
