"use client"
import { useEffect } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Gift, X, ArrowRight } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { useRewardsStore } from '@/store/useRewardsStore'

export default function RewardUnlockModal() {
  const router = useRouter()
  const { pendingUnlockReward, clearPendingUnlock } = useRewardsStore()

  useEffect(() => {
    if (!pendingUnlockReward) return
    const t = setTimeout(clearPendingUnlock, 8000)
    return () => clearTimeout(t)
  }, [pendingUnlockReward, clearPendingUnlock])

  const handleView = () => {
    clearPendingUnlock()
    router.push('/dashboard/profile')
  }

  return (
    <AnimatePresence>
      {pendingUnlockReward && (
        <motion.div
          className="fixed inset-0 z-[400] flex items-center justify-center px-5"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.18 }}
        >
          {/* Backdrop */}
          <div
            className="absolute inset-0 bg-black/75 backdrop-blur-md"
            onClick={clearPendingUnlock}
          />

          {/* Card */}
          <motion.div
            className="relative w-full max-w-xs overflow-hidden rounded-3xl text-center"
            style={{ background: '#1a1815', border: '1px solid rgba(255,255,255,0.08)' }}
            initial={{ opacity: 0, scale: 0.72, y: 24 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.88, y: 16 }}
            transition={{ type: 'spring', stiffness: 380, damping: 28 }}
          >
            {/* Dismiss */}
            <button
              onClick={clearPendingUnlock}
              className="absolute top-4 right-4 z-10 p-1.5 rounded-full cursor-pointer"
              style={{ background: 'rgba(255,255,255,0.07)' }}
            >
              <X size={13} style={{ color: 'rgba(255,255,255,0.4)' }} />
            </button>

            {/* Confetti header */}
            <div className="text-2xl pt-7 pb-1 select-none tracking-widest" aria-hidden>
              🎊 🎁 🎉
            </div>

            {/* Icon */}
            <div
              className="w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mt-3 mb-4"
              style={{ background: 'rgba(86,179,116,0.15)' }}
            >
              <Gift size={28} style={{ color: '#56b374' }} />
            </div>

            {/* Copy */}
            <p
              className="text-[11px] font-bold uppercase tracking-widest"
              style={{ color: 'rgba(255,255,255,0.3)' }}
            >
              Task Complete!
            </p>
            <h2
              className="leading-tight text-white px-5 mt-1.5"
              style={{ fontSize: 26, fontWeight: 900 }}
            >
              {pendingUnlockReward.brandName} reward earned!
            </h2>
            <p
              className="text-[12px] mt-2 px-8 leading-relaxed"
              style={{ color: 'rgba(255,255,255,0.42)' }}
            >
              {pendingUnlockReward.description}
            </p>

            {/* CTAs */}
            <div className="flex flex-col gap-2.5 px-5 pt-5 pb-7 mt-1">
              <button
                onClick={handleView}
                className="w-full py-3.5 rounded-2xl font-extrabold text-[14px] text-white flex items-center justify-center gap-2 cursor-pointer"
                style={{ background: '#7c3aed' }}
              >
                View Reward <ArrowRight size={15} />
              </button>
              <button
                onClick={clearPendingUnlock}
                className="w-full py-3 rounded-2xl font-bold text-[12px] cursor-pointer"
                style={{ color: 'rgba(255,255,255,0.35)', background: 'rgba(255,255,255,0.05)' }}
              >
                Later
              </button>
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
