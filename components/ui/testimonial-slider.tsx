"use client"
import React, { useState, useEffect, useRef } from 'react'
import { motion } from 'framer-motion'
import { ChevronLeft, ChevronRight, Quote } from 'lucide-react'
import Image from 'next/image'

export interface Testimonial {
  id: number
  quote: string
  name: string
  username: string
  avatar: string
  /** Optional: colored initial circle when avatar fails or is omitted */
  avatarColor?: string
  avatarInitial?: string
}

const DEFAULT_TESTIMONIALS: Testimonial[] = [
  {
    id: 1,
    quote: "Impressed by the professionalism and attention to detail.",
    name: "Guy Hawkins",
    username: "@guyhawkins",
    avatar: "https://images.unsplash.com/photo-1633332755192-727a05c4013d?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3",
  },
  {
    id: 2,
    quote: "A seamless experience from start to finish. Highly recommend!",
    name: "Karla Lynn",
    username: "@karlalynn8",
    avatar: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3",
  },
  {
    id: 3,
    quote: "Reliable and trustworthy. Made my life so much easier!",
    name: "Jane Cooper",
    username: "@janecooper",
    avatar: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3",
  },
  {
    id: 4,
    quote: "The level of service exceeded my expectations. Will definitely come back.",
    name: "Robert Chen",
    username: "@robertchen",
    avatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3",
  },
  {
    id: 5,
    quote: "An innovative approach that truly solved my problems.",
    name: "Sarah Miller",
    username: "@sarahmiller",
    avatar: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3",
  },
]

const getVisibleCount = (width: number): number => {
  if (width >= 1280) return 3
  if (width >= 768) return 2
  return 1
}

interface TestimonialSliderProps {
  testimonials?: Testimonial[]
  className?: string
}

const TestimonialSlider: React.FC<TestimonialSliderProps> = ({
  testimonials = DEFAULT_TESTIMONIALS,
  className = '',
}) => {
  const [currentIndex, setCurrentIndex] = useState(0)
  const [windowWidth, setWindowWidth] = useState(
    typeof window !== 'undefined' ? window.innerWidth : 1024
  )
  const [isAutoPlaying, setIsAutoPlaying] = useState(true)
  const autoPlayRef = useRef<ReturnType<typeof setInterval> | null>(null)
  const [direction, setDirection] = useState(0)
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (typeof window === 'undefined') return
    const handleResize = () => {
      const newWidth = window.innerWidth
      setWindowWidth(newWidth)
      const oldVisible = getVisibleCount(windowWidth)
      const newVisible = getVisibleCount(newWidth)
      if (oldVisible !== newVisible) {
        const maxIdx = testimonials.length - newVisible
        if (currentIndex > maxIdx) setCurrentIndex(Math.max(0, maxIdx))
      }
    }
    window.addEventListener('resize', handleResize)
    return () => window.removeEventListener('resize', handleResize)
  }, [windowWidth, currentIndex, testimonials.length])

  useEffect(() => {
    if (!isAutoPlaying) return
    autoPlayRef.current = setInterval(() => {
      const visibleCount = getVisibleCount(windowWidth)
      const maxIndex = testimonials.length - visibleCount
      if (currentIndex >= maxIndex) {
        setDirection(-1)
        setCurrentIndex(prev => prev - 1)
      } else if (currentIndex <= 0) {
        setDirection(1)
        setCurrentIndex(prev => prev + 1)
      } else {
        setCurrentIndex(prev => prev + direction)
      }
    }, 4000)
    return () => { if (autoPlayRef.current) clearInterval(autoPlayRef.current) }
  }, [isAutoPlaying, currentIndex, windowWidth, direction, testimonials.length])

  const visibleCount = getVisibleCount(windowWidth)
  const maxIndex = testimonials.length - visibleCount
  const canGoNext = currentIndex < maxIndex
  const canGoPrev = currentIndex > 0

  const pauseAutoPlay = () => {
    setIsAutoPlaying(false)
    setTimeout(() => setIsAutoPlaying(true), 8000)
  }

  const goNext = () => {
    if (!canGoNext) return
    setDirection(1)
    setCurrentIndex(prev => Math.min(prev + 1, maxIndex))
    pauseAutoPlay()
  }

  const goPrev = () => {
    if (!canGoPrev) return
    setDirection(-1)
    setCurrentIndex(prev => Math.max(prev - 1, 0))
    pauseAutoPlay()
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const handleDragEnd = (_event: any, info: { offset: { x: number } }) => {
    const threshold = 30
    if (info.offset.x < -threshold && canGoNext) goNext()
    else if (info.offset.x > threshold && canGoPrev) goPrev()
  }

  return (
    <div className={`px-4 py-8 sm:py-16 overflow-hidden ${className}`}>
      <div className="max-w-6xl mx-auto">
        <div className="relative" ref={containerRef}>
          {/* Nav buttons */}
          <div className="flex justify-center sm:justify-end sm:absolute sm:-top-16 right-0 space-x-2 mb-4 sm:mb-0">
            <motion.button
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.95 }}
              onClick={goPrev}
              disabled={!canGoPrev}
              className={`p-2 rounded-full transition-all duration-200 cursor-pointer ${
                canGoPrev
                  ? 'bg-white/[0.06] border border-white/10 hover:bg-white/10 text-white/70 hover:text-white shadow-lg'
                  : 'bg-white/[0.02] border border-white/[0.05] text-white/20 cursor-not-allowed'
              }`}
              aria-label="Previous testimonial"
            >
              <ChevronLeft size={18} />
            </motion.button>
            <motion.button
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.95 }}
              onClick={goNext}
              disabled={!canGoNext}
              className={`p-2 rounded-full transition-all duration-200 cursor-pointer ${
                canGoNext
                  ? 'bg-white/[0.06] border border-white/10 hover:bg-white/10 text-white/70 hover:text-white shadow-lg'
                  : 'bg-white/[0.02] border border-white/[0.05] text-white/20 cursor-not-allowed'
              }`}
              aria-label="Next testimonial"
            >
              <ChevronRight size={18} />
            </motion.button>
          </div>

          {/* Slider track */}
          <div className="overflow-hidden relative px-2 sm:px-0">
            <motion.div
              className="flex"
              animate={{ x: `-${currentIndex * (100 / visibleCount)}%` }}
              transition={{ type: 'spring', stiffness: 70, damping: 20 }}
            >
              {testimonials.map(testimonial => (
                <motion.div
                  key={testimonial.id}
                  className={`flex-shrink-0 p-2 ${
                    visibleCount === 3 ? 'w-1/3' : visibleCount === 2 ? 'w-1/2' : 'w-full'
                  }`}
                  drag="x"
                  dragConstraints={{ left: 0, right: 0 }}
                  dragElastic={0.2}
                  onDragEnd={handleDragEnd}
                  whileHover={{ y: -6 }}
                  whileTap={{ scale: 0.98, cursor: 'grabbing' }}
                  style={{ cursor: 'grab' }}
                >
                  <motion.div
                    className="relative overflow-hidden rounded-2xl p-6 h-full bg-white/[0.04] border border-white/[0.08] shadow-xl"
                    style={{ backdropFilter: 'blur(12px)' }}
                    whileHover={{
                      boxShadow: '0 20px 40px -8px rgba(124,58,237,0.25), 0 0 0 1px rgba(124,58,237,0.15)',
                      borderColor: 'rgba(124,58,237,0.3)',
                    }}
                  >
                    {/* Background quote icon */}
                    <div className="absolute -top-3 -left-3 opacity-[0.08] pointer-events-none">
                      <Quote size={windowWidth < 640 ? 48 : 64} className="text-violet-400" />
                    </div>

                    <div className="relative z-10 h-full flex flex-col">
                      {/* Stars */}
                      <div className="flex gap-0.5 mb-4">
                        {[...Array(5)].map((_, j) => (
                          <svg key={j} width="12" height="12" viewBox="0 0 13 13" fill="#7c3aed">
                            <path d="M6.5 1l1.5 3.2 3.5.5-2.5 2.4.6 3.5L6.5 9 3.4 10.6l.6-3.5L1.5 4.7l3.5-.5L6.5 1z"/>
                          </svg>
                        ))}
                      </div>

                      {/* Quote */}
                      <p className="text-sm sm:text-base text-white/65 font-medium mb-6 leading-relaxed flex-1">
                        &ldquo;{testimonial.quote}&rdquo;
                      </p>

                      {/* Author */}
                      <div className="pt-4 border-t border-white/[0.07]">
                        <div className="flex items-center gap-3">
                          <div className="relative flex-shrink-0">
                            {testimonial.avatarColor && testimonial.avatarInitial ? (
                              /* Colored initial fallback */
                              <div
                                className="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold text-white border-2 border-white/[0.08]"
                                style={{ background: testimonial.avatarColor }}
                              >
                                {testimonial.avatarInitial}
                              </div>
                            ) : (
                              <Image
                                width={40}
                                height={40}
                                src={testimonial.avatar}
                                alt={testimonial.name}
                                className="w-10 h-10 rounded-full object-cover border-2 border-white/[0.1]"
                              />
                            )}
                            {/* Pulse ring */}
                            <motion.div
                              className="absolute inset-0 rounded-full"
                              style={{ background: 'rgba(124,58,237,0.3)' }}
                              animate={{ scale: [1, 1.4], opacity: [0.4, 0] }}
                              transition={{ duration: 2, repeat: Infinity, repeatDelay: 1.5 }}
                            />
                          </div>
                          <div>
                            <h4 className="font-bold text-sm text-white leading-tight">{testimonial.name}</h4>
                            <p className="text-white/35 text-xs mt-0.5">{testimonial.username}</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </motion.div>
                </motion.div>
              ))}
            </motion.div>
          </div>

          {/* Dot indicators */}
          <div className="flex justify-center mt-7 gap-1.5">
            {Array.from({ length: testimonials.length - visibleCount + 1 }, (_, index) => (
              <motion.button
                key={index}
                onClick={() => { setCurrentIndex(index); pauseAutoPlay() }}
                className="relative focus:outline-none cursor-pointer"
                whileHover={{ scale: 1.3 }}
                whileTap={{ scale: 0.85 }}
                aria-label={`Go to testimonial ${index + 1}`}
              >
                <motion.div
                  className="w-2 h-2 rounded-full"
                  animate={{ background: index === currentIndex ? '#7c3aed' : 'rgba(255,255,255,0.2)' }}
                  transition={{ duration: 0.3 }}
                />
                {index === currentIndex && (
                  <motion.div
                    className="absolute inset-0 rounded-full"
                    style={{ background: 'rgba(124,58,237,0.4)' }}
                    animate={{ scale: [1, 2.2], opacity: [0.8, 0] }}
                    transition={{ duration: 1.5, repeat: Infinity }}
                  />
                )}
              </motion.button>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

export default TestimonialSlider
