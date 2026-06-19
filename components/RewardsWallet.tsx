"use client"
import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Gift, Copy, Check, X, Lock } from 'lucide-react'
import { useRewardsStore, type Reward } from '@/store/useRewardsStore'
import { useAuthStore } from '@/store/useAuthStore'

// ── Scratch Card ──────────────────────────────────────────────────────────────

function RewardCard({ reward, onOpen }: { reward: Reward; onOpen: () => void }) {
  const expires = new Date(reward.expiryDate)
  const isExpired = expires < new Date()
  const daysLeft = Math.ceil((expires.getTime() - Date.now()) / 86400000)

  return (
    <button
      onClick={onOpen}
      className="flex-shrink-0 w-[190px] cursor-pointer active:scale-95 transition-transform"
      style={{ borderRadius: 20 }}
    >
      <div
        className="relative w-full overflow-hidden"
        style={{
          borderRadius: 20,
          height: 120,
          background: reward.isRedeemed
            ? 'linear-gradient(135deg, #1f2937 0%, #111827 100%)'
            : 'linear-gradient(135deg, #1e1b4b 0%, #312e81 55%, #4c1d95 100%)',
        }}
      >
        {/* Shimmer sweep */}
        {!reward.isRedeemed && !isExpired && (
          <motion.div
            className="absolute inset-y-0 w-1/2 pointer-events-none"
            style={{
              background: 'linear-gradient(105deg, transparent 0%, rgba(255,255,255,0.12) 50%, transparent 100%)',
            }}
            initial={{ x: '-100%' }}
            animate={{ x: '400%' }}
            transition={{ duration: 2.2, repeat: Infinity, ease: 'linear', repeatDelay: 1.4 }}
          />
        )}

        <div className="relative flex flex-col h-full p-3.5">
          {/* Top row */}
          <div className="flex items-center justify-between">
            <span
              className="text-[8px] font-bold uppercase tracking-widest"
              style={{ color: 'rgba(165,180,252,0.5)' }}
            >
              Habitiq Reward
            </span>
            <Gift size={10} style={{ color: 'rgba(165,180,252,0.4)' }} />
          </div>

          {/* Brand + code hint */}
          <div className="mt-auto">
            <p className="text-[17px] font-black text-white leading-none">
              {reward.brandName}
            </p>
            <p
              className="text-[10px] font-mono mt-1"
              style={{ color: 'rgba(165,180,252,0.5)' }}
            >
              {reward.isRedeemed ? 'REDEEMED' : '●●●●-●●●● · tap to reveal'}
            </p>
          </div>

          {/* Status pill */}
          <div className="mt-2">
            <span
              className="text-[8.5px] font-bold uppercase px-2 py-0.5 rounded-full"
              style={{
                background: reward.isRedeemed
                  ? 'rgba(255,255,255,0.06)'
                  : isExpired
                  ? 'rgba(239,68,68,0.18)'
                  : 'rgba(86,179,116,0.18)',
                color: reward.isRedeemed
                  ? 'rgba(255,255,255,0.3)'
                  : isExpired
                  ? '#ef4444'
                  : '#56b374',
              }}
            >
              {reward.isRedeemed ? 'Used' : isExpired ? 'Expired' : `${daysLeft}d left`}
            </span>
          </div>
        </div>

        {/* Redeemed overlay */}
        {reward.isRedeemed && (
          <div
            className="absolute inset-0 flex items-center justify-center"
            style={{ background: 'rgba(0,0,0,0.45)', borderRadius: 20 }}
          >
            <Check size={22} style={{ color: 'rgba(255,255,255,0.3)' }} />
          </div>
        )}
      </div>
    </button>
  )
}

// ── Bottom Sheet ──────────────────────────────────────────────────────────────

function RewardBottomSheet({ reward, onClose }: { reward: Reward; onClose: () => void }) {
  const { markRewardRedeemed } = useRewardsStore()
  const { user } = useAuthStore()
  const [copied, setCopied] = useState(false)

  const expires = new Date(reward.expiryDate)
  const isExpired = expires < new Date()

  const handleCopy = async () => {
    if (!user || isExpired || reward.isRedeemed) return
    try {
      await navigator.clipboard.writeText(reward.discountCode)
      setCopied(true)
      markRewardRedeemed(user.uid, reward.id)
      setTimeout(() => setCopied(false), 3000)
    } catch { /* clipboard unavailable */ }
  }

  return (
    <motion.div
      className="fixed inset-0 z-[300] flex items-end"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.18 }}
    >
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      <motion.div
        className="relative w-full max-w-lg mx-auto rounded-t-3xl overflow-hidden"
        style={{ background: '#1a1815' }}
        initial={{ y: '100%' }}
        animate={{ y: 0 }}
        exit={{ y: '100%' }}
        transition={{ duration: 0.32, ease: [0.32, 0.72, 0, 1] }}
      >
        <div className="flex justify-center pt-3 pb-1">
          <div className="w-10 h-1 rounded-full" style={{ background: 'rgba(255,255,255,0.18)' }} />
        </div>

        <div className="px-6 pt-4 pb-10">
          <div className="flex items-start justify-between mb-5">
            <div>
              <p
                className="text-[10px] font-bold uppercase tracking-widest mb-2"
                style={{ color: 'rgba(255,255,255,0.35)' }}
              >
                Reward · Earned
              </p>
              <h2
                className="leading-none tracking-tight text-white"
                style={{ fontSize: 34, fontWeight: 900 }}
              >
                {reward.brandName}
              </h2>
            </div>
            <button
              onClick={onClose}
              className="mt-1 p-2 rounded-full cursor-pointer"
              style={{ background: 'rgba(255,255,255,0.07)' }}
            >
              <X size={15} style={{ color: 'rgba(255,255,255,0.5)' }} />
            </button>
          </div>

          <p
            className="text-[14px] leading-relaxed mb-5"
            style={{ color: 'rgba(255,255,255,0.55)' }}
          >
            {reward.description}
          </p>

          {/* Discount code */}
          <div
            className="flex items-center gap-3 px-4 py-3 rounded-xl mb-5"
            style={{
              background: 'rgba(255,255,255,0.05)',
              border: '1px solid rgba(255,255,255,0.09)',
            }}
          >
            <code
              className="flex-1 font-mono text-sm font-bold tracking-widest"
              style={{ color: 'rgba(255,255,255,0.8)' }}
            >
              {reward.discountCode}
            </code>
            <span
              className="text-[10px] font-bold uppercase"
              style={{ color: isExpired ? 'rgba(255,255,255,0.25)' : '#56b374' }}
            >
              {isExpired ? 'Expired' : 'Active'}
            </span>
          </div>

          <p className="text-[11px] mb-5" style={{ color: 'rgba(255,255,255,0.28)' }}>
            {isExpired
              ? 'This reward has expired.'
              : `Valid until ${expires.toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' })}`}
          </p>

          <motion.button
            onClick={handleCopy}
            disabled={isExpired || reward.isRedeemed}
            whileTap={!isExpired && !reward.isRedeemed ? { scale: 0.97 } : {}}
            className="w-full py-4 rounded-2xl text-base font-extrabold flex items-center justify-center gap-2.5 transition-colors cursor-pointer disabled:cursor-not-allowed"
            style={{
              background: copied
                ? '#16a34a'
                : isExpired || reward.isRedeemed
                ? 'rgba(255,255,255,0.08)'
                : '#7c3aed',
              color: isExpired || reward.isRedeemed ? 'rgba(255,255,255,0.3)' : 'white',
            }}
          >
            {copied
              ? (<><Check size={18} /> Copied ✓</>)
              : (<><Copy size={18} /> Copy Discount Code</>)}
          </motion.button>
        </div>
      </motion.div>
    </motion.div>
  )
}

// ── Rewards Wallet (Profile section — always visible) ────────────────────────

export default function RewardsWallet() {
  const { rewards } = useRewardsStore()
  const [selected, setSelected] = useState<Reward | null>(null)

  const activeCount = rewards.filter(
    r => !r.isRedeemed && new Date(r.expiryDate) > new Date()
  ).length

  return (
    <div>
      {/* Section header */}
      <div className="flex items-center gap-2 mb-3">
        <Gift size={14} className="text-primary" />
        <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest flex-1">
          Rewards Wallet
        </p>
        {activeCount > 0 && (
          <span className="text-[10px] font-extrabold bg-primary/10 text-primary px-2 py-0.5 rounded-full">
            {activeCount} active
          </span>
        )}
      </div>

      {/* Empty state — always rendered when no rewards */}
      {rewards.length === 0 && (
        <div
          className="flex flex-col items-center py-8 gap-3 rounded-2xl"
          style={{ border: '1.5px dashed var(--border)', background: 'var(--card)' }}
        >
          <div
            className="w-12 h-12 rounded-xl flex items-center justify-center"
            style={{ background: 'rgba(79,70,229,0.1)' }}
          >
            <Lock size={20} style={{ color: 'rgba(79,70,229,0.4)' }} />
          </div>
          <div className="text-center px-6">
            <p className="text-[13px] font-bold text-foreground">Your first reward awaits</p>
            <p className="text-[12px] text-muted-foreground mt-1 leading-relaxed">
              Complete any task to earn a gift from our brand partners
            </p>
          </div>
          <div
            className="flex items-center gap-1.5 text-[10px] font-bold"
            style={{ color: 'rgba(79,70,229,0.5)' }}
          >
            <Gift size={10} /> Powered by Habitiq Rewards
          </div>
        </div>
      )}

      {/* Scratch-card carousel */}
      {rewards.length > 0 && (
        <div
          className="flex gap-3 overflow-x-auto pb-1 [&::-webkit-scrollbar]:hidden"
          style={{ scrollbarWidth: 'none' }}
        >
          {rewards.map(r => (
            <RewardCard key={r.id} reward={r} onOpen={() => setSelected(r)} />
          ))}
        </div>
      )}

      <AnimatePresence>
        {selected && (
          <RewardBottomSheet
            key={selected.id}
            reward={selected}
            onClose={() => setSelected(null)}
          />
        )}
      </AnimatePresence>
    </div>
  )
}
