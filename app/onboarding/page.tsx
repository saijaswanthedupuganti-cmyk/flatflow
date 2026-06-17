"use client"
import { useState, Suspense, useEffect } from 'react'
import { motion } from 'framer-motion'
import { useRouter, useSearchParams } from 'next/navigation'
import { useAuthStore } from '@/store/useAuthStore'
import { useFlatStore } from '@/store/useFlatStore'
import { createFlat, joinFlat, getFlatJoinMode, requestToJoinFlat, flatExists } from '@/lib/flatService'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Home, ArrowLeft, ArrowRight, Loader2, X, Clock, AlertTriangle, Check, Lock, Wallet, RefreshCw, Bell, BarChart2, Users, Shield, Heart, Zap, UserPlus, User } from 'lucide-react'
import { useSubscription } from '@/hooks/useSubscription'
import SubscriptionUpsell from '@/components/SubscriptionUpsell'

type Step = 'choose' | 'create' | 'join' | 'pending'

type ExitRecord = { flatId: string; flatName: string; exitedAt: string; reason: 'left' | 'removed' }

function OnboardingContent() {
  const router = useRouter()
  const searchParams = useSearchParams()

  // When `addFlat=1`, the user is already logged in and just wants to add another flat
  const isAddingFlat = searchParams.get('addFlat') === '1'
  const initialMode = searchParams.get('mode') // 'create' | 'join'

  const { user, setFlatId, logout, addFlatToState, allFlatIds } = useAuthStore()
  const { initFirestoreListeners, addMemberMock } = useFlatStore()
  const { isActive, maxFlats } = useSubscription()
  const [showUpsell, setShowUpsell] = useState(false)

  const prefillCode = searchParams.get('code') || ''

  const [step, setStep] = useState<Step>(() => {
    if (initialMode === 'create') return 'create'
    if (initialMode === 'join' || prefillCode) return 'join'
    return 'choose'
  })
  const [flatName, setFlatName] = useState('')
  const [exitRecords, setExitRecords] = useState<ExitRecord[]>([])

  useEffect(() => {
    try {
      const raw = localStorage.getItem('habitiq_flat_exits')
      if (raw) setExitRecords(JSON.parse(raw))
    } catch { /* ignore */ }
  }, [])

  const dismissExit = (flatId: string) => {
    const updated = exitRecords.filter(e => e.flatId !== flatId)
    setExitRecords(updated)
    try {
      localStorage.setItem('habitiq_flat_exits', JSON.stringify(updated))
    } catch { /* ignore */ }
  }
  const [nickname, setNickname] = useState(user?.displayName || '')
  const [inviteCode, setInviteCode] = useState(prefillCode.toUpperCase())
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  if (!user) return null

  // ── Choose screen ─────────────────────────────────────────────────────────
  if (step === 'choose') {
    return (
      <>
        {/* ── Desktop: full-screen 50/50 split ──────────────────────────────── */}
        <div className="fixed inset-0 z-50 select-none hidden lg:flex" style={{ background: '#050816' }}>

          {/* Welcome header — centered top */}
          <div className="absolute top-6 left-1/2 -translate-x-1/2 z-30 flex flex-col items-center gap-1.5 text-center pointer-events-none">
            <img src="/habitiq-logo.svg" alt="Habitiq" className="h-5 w-auto mb-0.5" style={{ filter: 'brightness(0) invert(1)', opacity: 0.85 }} />
            <h1 className="font-black text-[22px] tracking-tight leading-none" style={{ color: '#FFFFFF' }}>
              Welcome to <span style={{ color: '#FF6B00' }}>HabitIQ</span>
            </h1>
            <p className="text-[11px]" style={{ color: 'rgba(168,176,197,0.5)' }}>
              Split expenses. Manage chores. Run your shared home.
            </p>
          </div>

          {/* Close — addFlat mode */}
          {isAddingFlat && (
            <button
              onClick={() => router.back()}
              className="absolute top-6 right-6 z-30 w-8 h-8 flex items-center justify-center rounded-full cursor-pointer transition-all"
              style={{ background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.1)', color: 'rgba(255,255,255,0.45)' }}
            >
              <X size={13} />
            </button>
          )}

          {/* Exit notifications */}
          {exitRecords.length > 0 && (
            <div className="absolute top-16 left-1/2 -translate-x-1/2 z-30 space-y-1 w-96">
              {exitRecords.map(exit => (
                <div key={exit.flatId} className="flex items-center gap-3 rounded-xl px-3 py-2" style={{ background: 'rgba(69,26,3,0.8)', border: '1px solid rgba(146,64,14,0.4)' }}>
                  <AlertTriangle size={12} className="text-amber-500 shrink-0" />
                  <p className="text-[11px] text-amber-300/80 flex-1 min-w-0">
                    {exit.reason === 'removed' ? 'Removed from' : 'Left'} <strong>{exit.flatName}</strong>
                  </p>
                  <button onClick={() => dismissExit(exit.flatId)} className="text-amber-600/60 hover:text-amber-400 transition-colors cursor-pointer shrink-0">
                    <X size={11} />
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* ── CREATE — left half ────────────────────────────────────────────── */}
          <div className="relative w-1/2 h-full cursor-pointer overflow-hidden group" onClick={() => setStep('create')}>
            <img
              src="/onboard-create.png" alt=""
              className="absolute inset-0 w-full h-full object-cover transition-transform duration-700 ease-out group-hover:scale-[1.04]"
              style={{ objectPosition: 'center 18%' }}
            />
            <div className="absolute inset-0" style={{ background: 'linear-gradient(to top, rgba(8,6,4,0.97) 0%, rgba(8,6,4,0.82) 30%, rgba(8,6,4,0.35) 60%, rgba(8,6,4,0.5) 100%)' }} />
            <div className="absolute inset-y-0 right-0 w-24" style={{ background: 'linear-gradient(to right, transparent, rgba(5,8,22,0.45))' }} />
            <div className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-500" style={{ background: 'rgba(255,107,0,0.06)' }} />
            <div className="absolute inset-0 z-10 flex flex-col justify-end px-14 pb-16">
              <div className="w-14 h-14 rounded-2xl mb-6 flex items-center justify-center" style={{ background: 'rgba(255,107,0,0.15)', border: '1.5px solid rgba(255,107,0,0.4)' }}>
                <Home size={26} style={{ color: '#FF6B00' }} />
              </div>
              <h2 className="font-black text-[46px] leading-[1.04] tracking-tight mb-4 text-white">
                Your Flat.<br />
                <span style={{ color: '#FF6B00' }}>Your Rules.</span>
              </h2>
              <p className="text-[14px] leading-relaxed mb-8" style={{ color: 'rgba(255,255,255,0.42)', maxWidth: 340 }}>
                Become the admin. Invite your roommates, set up chore rotation and bills — your shared home, on autopilot.
              </p>
              <div className="space-y-3.5 mb-10">
                {([
                  [<Users size={14} key="u" />, 'Invite roommates with a code'],
                  [<RefreshCw size={14} key="r" />, 'Auto-rotate chores fairly'],
                  [<Wallet size={14} key="w" />, 'Split and track every expense'],
                ] as [React.ReactNode, string][]).map(([icon, label], i) => (
                  <div key={i} className="flex items-center gap-3">
                    <span style={{ color: '#FF6B00' }}>{icon}</span>
                    <span className="text-[13px] font-medium" style={{ color: 'rgba(255,255,255,0.72)' }}>{label}</span>
                  </div>
                ))}
              </div>
              <button
                onClick={e => { e.stopPropagation(); setStep('create') }}
                className="w-full h-[56px] rounded-2xl font-black text-[15px] text-white flex items-center justify-center gap-2.5 cursor-pointer transition-shadow duration-300"
                style={{ background: '#FF6B00', boxShadow: '0 4px 20px rgba(255,107,0,0.4)' }}
                onMouseEnter={e => { (e.currentTarget as HTMLButtonElement).style.boxShadow = '0 8px 36px rgba(255,107,0,0.6)' }}
                onMouseLeave={e => { (e.currentTarget as HTMLButtonElement).style.boxShadow = '0 4px 20px rgba(255,107,0,0.4)' }}
              >
                Create New Flat <ArrowRight size={17} />
              </button>
            </div>
          </div>

          {/* Center divider */}
          <div className="absolute left-1/2 top-0 bottom-0 w-px z-20" style={{ background: 'rgba(255,255,255,0.08)' }} />

          {/* ── JOIN — right half ─────────────────────────────────────────────── */}
          <div className="relative w-1/2 h-full cursor-pointer overflow-hidden group" onClick={() => setStep('join')}>
            <img
              src="/onboard-join.png" alt=""
              className="absolute inset-0 w-full h-full object-cover transition-transform duration-700 ease-out group-hover:scale-[1.04]"
              style={{ objectPosition: 'center center' }}
            />
            <div className="absolute inset-0" style={{ background: 'linear-gradient(to top, rgba(4,3,14,0.97) 0%, rgba(4,3,14,0.82) 30%, rgba(4,3,14,0.35) 60%, rgba(4,3,14,0.5) 100%)' }} />
            <div className="absolute inset-y-0 left-0 w-24" style={{ background: 'linear-gradient(to left, transparent, rgba(5,8,22,0.45))' }} />
            <div className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-500" style={{ background: 'rgba(124,92,255,0.06)' }} />
            <div className="absolute inset-0 z-10 flex flex-col justify-end px-14 pb-16">
              <div className="w-14 h-14 rounded-2xl mb-6 flex items-center justify-center" style={{ background: 'rgba(124,92,255,0.15)', border: '1.5px solid rgba(124,92,255,0.4)' }}>
                <Users size={26} style={{ color: '#7C5CFF' }} />
              </div>
              <h2 className="font-black text-[46px] leading-[1.04] tracking-tight mb-4 text-white">
                Join Your<br />
                <span style={{ color: '#7C5CFF' }}>Crew.</span>
              </h2>
              <p className="text-[14px] leading-relaxed mb-8" style={{ color: 'rgba(255,255,255,0.42)', maxWidth: 340 }}>
                Have an invite code? Walk right in. Expenses, chores, bills — already set up and waiting for you.
              </p>
              <div className="space-y-3.5 mb-10">
                {([
                  [<ArrowRight size={14} key="a" />, 'Step in instantly — no setup'],
                  [<Wallet size={14} key="w2" />, 'See balances and shared expenses'],
                  [<Bell size={14} key="b" />, 'Stay synced in real-time'],
                ] as [React.ReactNode, string][]).map(([icon, label], i) => (
                  <div key={i} className="flex items-center gap-3">
                    <span style={{ color: '#7C5CFF' }}>{icon}</span>
                    <span className="text-[13px] font-medium" style={{ color: 'rgba(255,255,255,0.72)' }}>{label}</span>
                  </div>
                ))}
              </div>
              <button
                onClick={e => { e.stopPropagation(); setStep('join') }}
                className="w-full h-[56px] rounded-2xl font-black text-[15px] text-white flex items-center justify-center gap-2.5 cursor-pointer transition-shadow duration-300"
                style={{ background: '#7C5CFF', boxShadow: '0 4px 20px rgba(124,92,255,0.4)' }}
                onMouseEnter={e => { (e.currentTarget as HTMLButtonElement).style.boxShadow = '0 8px 36px rgba(124,92,255,0.6)' }}
                onMouseLeave={e => { (e.currentTarget as HTMLButtonElement).style.boxShadow = '0 4px 20px rgba(124,92,255,0.4)' }}
              >
                Enter Invite Code <ArrowRight size={17} />
              </button>
            </div>
          </div>

          {/* Sign out — bottom center */}
          {!isAddingFlat && (
            <div className="absolute bottom-7 left-1/2 -translate-x-1/2 z-30">
              <button
                onClick={() => logout().then(() => router.push('/'))}
                className="text-[10px] hover:opacity-60 transition-opacity cursor-pointer"
                style={{ color: 'rgba(255,255,255,0.18)' }}
              >
                Sign out · use a different account
              </button>
            </div>
          )}
        </div>

        {/* ── Mobile: scrollable column ──────────────────────────────────────── */}
        <div className="fixed inset-0 z-50 select-none overflow-y-auto lg:hidden" style={{ background: '#050816' }}>
          <div className="min-h-full flex flex-col">

          {/* Top bar */}
          <div className="shrink-0 flex justify-end items-center px-4 h-11">
            {isAddingFlat && (
              <button
                onClick={() => router.back()}
                className="w-8 h-8 flex items-center justify-center rounded-full cursor-pointer transition-all"
                style={{ background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.1)', color: 'rgba(255,255,255,0.45)' }}
              >
                <X size={13} />
              </button>
            )}
          </div>

          {/* Exit notifications */}
          {exitRecords.length > 0 && (
            <div className="shrink-0 px-4 pb-2 space-y-1 max-w-[1100px] mx-auto w-full">
              {exitRecords.map(exit => (
                <div key={exit.flatId} className="flex items-center gap-3 rounded-xl px-3 py-2" style={{ background: 'rgba(69,26,3,0.8)', border: '1px solid rgba(146,64,14,0.4)' }}>
                  <AlertTriangle size={12} className="text-amber-500 shrink-0" />
                  <p className="text-[11px] text-amber-300/80 flex-1 min-w-0">
                    {exit.reason === 'removed' ? 'Removed from' : 'Left'} <strong>{exit.flatName}</strong>
                  </p>
                  <button onClick={() => dismissExit(exit.flatId)} className="text-amber-600/60 hover:text-amber-400 transition-colors cursor-pointer shrink-0">
                    <X size={11} />
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* Hero */}
          <div className="shrink-0 text-center px-4 pt-6 pb-5">
            <img src="/habitiq-logo.svg" alt="HabitIQ" className="h-6 w-auto mx-auto mb-3" style={{ filter: 'brightness(0) invert(1)', opacity: 0.9 }} />
            <motion.h1
              initial={{ opacity: 0, y: 14 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3 }}
              className="font-black tracking-tight leading-none"
              style={{ color: '#FFFFFF', fontSize: 'clamp(26px, 7vw, 36px)' }}
            >
              Welcome to <span style={{ color: '#FF6B00' }}>HabitIQ</span>
            </motion.h1>
            <p className="text-[13px] mt-2 leading-relaxed" style={{ color: 'rgba(168,176,197,0.55)' }}>
              Split expenses. Manage chores.<br />Run your shared home.
            </p>
          </div>

          {/* Cards */}
          <div className="px-4 lg:flex-1 lg:min-h-0 lg:pb-5">
            <div className="flex flex-col lg:flex-row lg:h-full lg:max-w-[1100px] lg:mx-auto lg:gap-4">

              {/* ── CREATE card ─────────────────────────────────────── */}
              <motion.div
                initial={{ opacity: 0, y: 24 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.35, delay: 0.15 }}
                whileHover={{ scale: 1.015 }}
                whileTap={{ scale: 0.98 }}
                className="rounded-[20px] overflow-hidden cursor-pointer flex flex-col lg:flex-1 lg:relative"
                style={{ background: '#0D1326', border: '1px solid rgba(255,107,0,0.28)', boxShadow: '0 8px 40px rgba(0,0,0,0.5)' }}
                onClick={() => setStep('create')}
              >
                {/* ── DESKTOP: full-bleed image + glass panel at bottom ── */}
                <>
                  {/* Full-bleed image — desktop only */}
                  <div className="hidden lg:block absolute inset-0">
                    <img
                      src="/onboard-create.png"
                      alt=""
                      className="w-full h-full object-cover"
                      style={{ objectPosition: 'center 20%' }}
                    />
                    {/* Bottom-up dark fade so glass panel has clean backdrop */}
                    <div className="absolute inset-0" style={{ background: 'linear-gradient(to top, rgba(5,8,22,0.97) 0%, rgba(5,8,22,0.7) 35%, rgba(5,8,22,0.2) 65%, transparent 100%)' }} />
                    {/* Subtle vignette on sides */}
                    <div className="absolute inset-0" style={{ background: 'linear-gradient(to right, rgba(5,8,22,0.35) 0%, transparent 25%, transparent 75%, rgba(5,8,22,0.35) 100%)' }} />
                  </div>

                  {/* Glass content panel — desktop only, anchored at bottom */}
                  <div className="hidden lg:flex absolute bottom-0 left-0 right-0 z-10 flex-col px-6 pt-5 pb-5"
                    style={{ backdropFilter: 'blur(24px)', WebkitBackdropFilter: 'blur(24px)', background: 'rgba(5,8,22,0.52)', borderTop: '1px solid rgba(255,255,255,0.09)' }}>
                    {/* Icon + headline */}
                    <div className="flex items-start gap-4 mb-4">
                      <div className="w-11 h-11 rounded-xl shrink-0 flex items-center justify-center" style={{ background: 'rgba(255,107,0,0.2)', border: '1.5px solid rgba(255,107,0,0.45)' }}>
                        <Home size={20} style={{ color: '#FF6B00' }} />
                      </div>
                      <div>
                        <h2 className="font-black text-[22px] leading-tight tracking-tight" style={{ color: '#FFFFFF' }}>
                          Your Flat. <span style={{ color: '#FF6B00' }}>Your Rules.</span>
                        </h2>
                        <p className="text-[12px] mt-0.5" style={{ color: 'rgba(255,255,255,0.5)' }}>
                          Create your shared home and manage everything.
                        </p>
                      </div>
                    </div>
                    {/* Product preview mini-cards */}
                    <div className="grid grid-cols-3 gap-2 mb-4">
                      {[
                        { icon: <Wallet size={11} />, label: 'Expense Split', value: '₹4,560', sub: '4 people' },
                        { icon: <RefreshCw size={11} />, label: 'Chore Rotation', value: 'Kitchen', sub: 'Next: Rahul' },
                        { icon: <Users size={11} />, label: 'Flat Members', value: '4 Active', sub: '+ Add more' },
                      ].map((c, i) => (
                        <div key={i} className="px-3 py-2.5 rounded-xl" style={{ background: 'rgba(255,255,255,0.07)', border: '1px solid rgba(255,255,255,0.1)' }}>
                          <div className="flex items-center gap-1.5 mb-1.5">
                            <span style={{ color: '#FF6B00' }}>{c.icon}</span>
                            <p className="text-[9px] font-semibold uppercase tracking-wide" style={{ color: 'rgba(255,255,255,0.38)' }}>{c.label}</p>
                          </div>
                          <p className="font-black text-[13px] leading-tight" style={{ color: '#FFFFFF' }}>{c.value}</p>
                          <p className="text-[9px] mt-0.5" style={{ color: 'rgba(255,255,255,0.35)' }}>{c.sub}</p>
                        </div>
                      ))}
                    </div>
                    {/* CTA */}
                    <button
                      onClick={e => { e.stopPropagation(); setStep('create') }}
                      className="w-full h-[50px] rounded-2xl font-black text-[15px] text-white flex items-center justify-center gap-2 cursor-pointer transition-opacity hover:opacity-90"
                      style={{ background: '#FF6B00', boxShadow: '0 4px 24px rgba(255,107,0,0.5)' }}
                    >
                      Create New Flat <ArrowRight size={16} />
                    </button>
                  </div>
                </>

                {/* ── MOBILE: left content + right illustration ── */}
                <div className="lg:hidden">
                  {/* Body with side image */}
                  <div className="relative min-h-[210px]">
                    {/* Illustration — right half */}
                    <div className="absolute inset-y-0 right-0 w-[48%] overflow-hidden">
                      <img
                        src="/onboard-create.png"
                        alt=""
                        className="w-full h-full object-cover"
                        style={{ objectPosition: 'center 15%' }}
                      />
                      <div className="absolute inset-0" style={{ background: 'linear-gradient(to right, #0D1326 0%, rgba(13,19,38,0.9) 15%, rgba(13,19,38,0.2) 55%, transparent 100%)' }} />
                    </div>
                    {/* Left content */}
                    <div className="relative z-10 px-5 pt-5 pb-5">
                      <div className="w-10 h-10 rounded-xl mb-3.5 flex items-center justify-center" style={{ background: 'rgba(255,107,0,0.18)', border: '1.5px solid rgba(255,107,0,0.38)' }}>
                        <Home size={18} style={{ color: '#FF6B00' }} />
                      </div>
                      <h2 className="font-black text-[22px] leading-[1.15] tracking-tight mb-1.5" style={{ color: '#FFFFFF' }}>
                        Create <span style={{ color: '#FF6B00' }}>New Flat</span>
                      </h2>
                      <p className="text-[12px] leading-relaxed mb-4" style={{ color: 'rgba(255,255,255,0.48)', maxWidth: 175 }}>
                        Become the admin. Set up your shared home.
                      </p>
                      <div className="space-y-2.5">
                        {([
                          [<Users size={14} key="u" />,     'Invite roommates'],
                          [<RefreshCw size={14} key="r" />, 'Setup chores'],
                          [<Wallet size={14} key="w" />,    'Track expenses'],
                        ] as [React.ReactNode, string][]).map(([icon, label], i) => (
                          <div key={i} className="flex items-center gap-2.5">
                            <span style={{ color: '#FF6B00' }}>{icon}</span>
                            <span className="text-[13px] font-medium" style={{ color: 'rgba(255,255,255,0.82)' }}>{label}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                  {/* Mobile CTA */}
                  <div className="px-4 pb-4 pt-0">
                    <button
                      onClick={e => { e.stopPropagation(); setStep('create') }}
                      className="w-full h-[52px] rounded-2xl font-black text-[15px] text-white flex items-center justify-center gap-2 cursor-pointer transition-opacity hover:opacity-90"
                      style={{ background: '#FF6B00', boxShadow: '0 4px 24px rgba(255,107,0,0.45)' }}
                    >
                      Create New Flat <ArrowRight size={16} />
                    </button>
                  </div>
                </div>
              </motion.div>

              {/* OR divider — mobile horizontal */}
              <div className="flex items-center gap-3 py-4 lg:hidden shrink-0">
                <div className="flex-1 h-px" style={{ background: 'rgba(255,255,255,0.08)' }} />
                <div className="w-9 h-9 rounded-full flex items-center justify-center shrink-0" style={{ background: '#0D1326', border: '1px solid rgba(255,255,255,0.12)' }}>
                  <span className="font-black text-[11px] tracking-widest" style={{ color: 'rgba(255,255,255,0.35)' }}>OR</span>
                </div>
                <div className="flex-1 h-px" style={{ background: 'rgba(255,255,255,0.08)' }} />
              </div>
              {/* OR divider — desktop vertical */}
              <div className="hidden lg:flex flex-col items-center justify-center w-8 shrink-0">
                <div className="flex-1 w-px" style={{ background: 'rgba(255,255,255,0.07)' }} />
                <div className="w-7 h-7 rounded-full flex items-center justify-center my-3 shrink-0" style={{ background: '#050816', border: '1px solid rgba(255,255,255,0.12)' }}>
                  <span className="font-black text-[8px] tracking-widest" style={{ color: 'rgba(255,255,255,0.3)' }}>OR</span>
                </div>
                <div className="flex-1 w-px" style={{ background: 'rgba(255,255,255,0.07)' }} />
              </div>

              {/* ── JOIN card ────────────────────────────────────────── */}
              <motion.div
                initial={{ opacity: 0, y: 24 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.35, delay: 0.25 }}
                whileHover={{ scale: 1.015 }}
                whileTap={{ scale: 0.98 }}
                className="rounded-[20px] overflow-hidden cursor-pointer flex flex-col lg:flex-1 lg:relative"
                style={{ background: '#0D1326', border: '1px solid rgba(124,92,255,0.28)', boxShadow: '0 8px 40px rgba(0,0,0,0.5)' }}
                onClick={() => setStep('join')}
              >
                {/* ── DESKTOP: full-bleed image + glass panel at bottom ── */}
                <>
                  <div className="hidden lg:block absolute inset-0">
                    <img
                      src="/onboard-join.png"
                      alt=""
                      className="w-full h-full object-cover"
                      style={{ objectPosition: 'center center' }}
                    />
                    <div className="absolute inset-0" style={{ background: 'linear-gradient(to top, rgba(5,8,22,0.97) 0%, rgba(5,8,22,0.7) 35%, rgba(5,8,22,0.2) 65%, transparent 100%)' }} />
                    <div className="absolute inset-0" style={{ background: 'linear-gradient(to right, rgba(5,8,22,0.35) 0%, transparent 25%, transparent 75%, rgba(5,8,22,0.35) 100%)' }} />
                  </div>

                  <div className="hidden lg:flex absolute bottom-0 left-0 right-0 z-10 flex-col px-6 pt-5 pb-5"
                    style={{ backdropFilter: 'blur(24px)', WebkitBackdropFilter: 'blur(24px)', background: 'rgba(5,8,22,0.52)', borderTop: '1px solid rgba(255,255,255,0.09)' }}>
                    <div className="flex items-start gap-4 mb-4">
                      <div className="w-11 h-11 rounded-xl shrink-0 flex items-center justify-center" style={{ background: 'rgba(124,92,255,0.2)', border: '1.5px solid rgba(124,92,255,0.45)' }}>
                        <Users size={20} style={{ color: '#7C5CFF' }} />
                      </div>
                      <div>
                        <h2 className="font-black text-[22px] leading-tight tracking-tight" style={{ color: '#FFFFFF' }}>
                          Join Your <span style={{ color: '#7C5CFF' }}>Crew.</span>
                        </h2>
                        <p className="text-[12px] mt-0.5" style={{ color: 'rgba(255,255,255,0.5)' }}>
                          Have an invite code? Join instantly.
                        </p>
                      </div>
                    </div>
                    <div className="grid grid-cols-3 gap-2 mb-4">
                      {[
                        { icon: <ArrowRight size={11} />, label: 'Join Instantly', value: 'Invite Code', sub: 'Tap & done' },
                        { icon: <BarChart2 size={11} />, label: 'Full Access', value: 'Expenses', sub: 'Chores + bills' },
                        { icon: <Bell size={11} />, label: 'Stay Synced', value: 'Real-time', sub: 'Notifications' },
                      ].map((c, i) => (
                        <div key={i} className="px-3 py-2.5 rounded-xl" style={{ background: 'rgba(255,255,255,0.07)', border: '1px solid rgba(255,255,255,0.1)' }}>
                          <div className="flex items-center gap-1.5 mb-1.5">
                            <span style={{ color: '#7C5CFF' }}>{c.icon}</span>
                            <p className="text-[9px] font-semibold uppercase tracking-wide" style={{ color: 'rgba(255,255,255,0.38)' }}>{c.label}</p>
                          </div>
                          <p className="font-black text-[13px] leading-tight" style={{ color: '#FFFFFF' }}>{c.value}</p>
                          <p className="text-[9px] mt-0.5" style={{ color: 'rgba(255,255,255,0.35)' }}>{c.sub}</p>
                        </div>
                      ))}
                    </div>
                    <button
                      onClick={e => { e.stopPropagation(); setStep('join') }}
                      className="w-full h-[50px] rounded-2xl font-black text-[15px] text-white flex items-center justify-center gap-2 cursor-pointer transition-opacity hover:opacity-90"
                      style={{ background: '#7C5CFF', boxShadow: '0 4px 24px rgba(124,92,255,0.5)' }}
                    >
                      Enter Invite Code <ArrowRight size={16} />
                    </button>
                  </div>
                </>

                {/* ── MOBILE: left content + right illustration ── */}
                <div className="lg:hidden">
                  <div className="relative min-h-[210px]">
                    <div className="absolute inset-y-0 right-0 w-[48%] overflow-hidden">
                      <img
                        src="/onboard-join.png"
                        alt=""
                        className="w-full h-full object-cover"
                        style={{ objectPosition: 'center center' }}
                      />
                      <div className="absolute inset-0" style={{ background: 'linear-gradient(to right, #0D1326 0%, rgba(13,19,38,0.9) 15%, rgba(13,19,38,0.2) 55%, transparent 100%)' }} />
                    </div>
                    <div className="relative z-10 px-5 pt-5 pb-5">
                      <div className="w-10 h-10 rounded-xl mb-3.5 flex items-center justify-center" style={{ background: 'rgba(124,92,255,0.18)', border: '1.5px solid rgba(124,92,255,0.38)' }}>
                        <Users size={18} style={{ color: '#7C5CFF' }} />
                      </div>
                      <h2 className="font-black text-[22px] leading-[1.15] tracking-tight mb-1.5" style={{ color: '#FFFFFF' }}>
                        Join <span style={{ color: '#7C5CFF' }}>Existing Flat</span>
                      </h2>
                      <p className="text-[12px] leading-relaxed mb-4" style={{ color: 'rgba(255,255,255,0.48)', maxWidth: 175 }}>
                        Have an invite code? Join your crew instantly.
                      </p>
                      <div className="space-y-2.5">
                        {([
                          [<Wallet size={14} key="w" />,    'Access expenses'],
                          [<Check size={14} key="c" />,     'View chores'],
                          [<Bell size={14} key="b" />,      'Stay synced'],
                        ] as [React.ReactNode, string][]).map(([icon, label], i) => (
                          <div key={i} className="flex items-center gap-2.5">
                            <span style={{ color: '#7C5CFF' }}>{icon}</span>
                            <span className="text-[13px] font-medium" style={{ color: 'rgba(255,255,255,0.82)' }}>{label}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                  <div className="px-4 pb-4 pt-0">
                    <button
                      onClick={e => { e.stopPropagation(); setStep('join') }}
                      className="w-full h-[52px] rounded-2xl font-black text-[15px] text-white flex items-center justify-center gap-2 cursor-pointer transition-opacity hover:opacity-90"
                      style={{ background: '#7C5CFF', boxShadow: '0 4px 24px rgba(124,92,255,0.45)' }}
                    >
                      Enter Invite Code <ArrowRight size={16} />
                    </button>
                  </div>
                </div>
              </motion.div>

            </div>
          </div>

          {/* Trust section — matches reference exactly */}
          <div className="shrink-0 px-4 pt-7 pb-5">
            {/* Avatars + star rating */}
            <div className="flex items-center gap-4 mb-4">
              <div className="flex items-center shrink-0">
                {([['S','#7c3aed'],['R','#ea580c'],['P','#16a34a'],['K','#2563eb'],['M','#d97706']] as [string,string][]).map(([l,c],i) => (
                  <div key={i} className="w-9 h-9 rounded-full flex items-center justify-center text-[10px] font-bold text-white shrink-0"
                    style={{ background: c, border: '2.5px solid #050816', marginLeft: i === 0 ? 0 : -10, zIndex: i }}>
                    {l}
                  </div>
                ))}
              </div>
              <div>
                <div className="flex items-center gap-1">
                  {[0,1,2,3,4].map(i => <span key={i} style={{ color: '#F59E0B', fontSize: 16 }}>★</span>)}
                </div>
                <p className="text-[12px] font-semibold mt-0.5" style={{ color: 'rgba(255,255,255,0.65)' }}>
                  Loved by 10,000+ shared homes
                </p>
              </div>
            </div>

            {/* Badges */}
            <div className="flex items-center justify-between pt-4" style={{ borderTop: '1px solid rgba(255,255,255,0.06)' }}>
              {([
                [<Shield size={14} key="s" />,  'Secure & Private'],
                [<Users size={14} key="u" />,   'Built for Roommates'],
                [<Heart size={14} key="h" />,   'Made for India'],
              ] as [React.ReactNode, string][]).map(([icon, label], i) => (
                <div key={i} className="flex items-center gap-1.5">
                  <span style={{ color: 'rgba(255,255,255,0.32)' }}>{icon}</span>
                  <span className="text-[11px]" style={{ color: 'rgba(255,255,255,0.42)' }}>{label}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Sign out */}
          {!isAddingFlat && (
            <div className="shrink-0 pb-7 flex justify-center">
              <button
                onClick={() => logout().then(() => router.push('/'))}
                className="text-[10px] hover:opacity-60 transition-opacity cursor-pointer"
                style={{ color: 'rgba(255,255,255,0.18)' }}
              >
                Sign out · use a different account
              </button>
            </div>
          )}

        </div>
      </div>
      {showUpsell && <SubscriptionUpsell feature="create_flat" onClose={() => setShowUpsell(false)} />}
      </>
    )
  }

  const handleCreateFlat = async () => {
    if (!flatName.trim() || !nickname.trim()) return
    if (isAddingFlat) {
      if (allFlatIds.length >= maxFlats) {
        if (isActive) {
          setError(`You've reached the maximum of ${maxFlats} flats. Remove a flat first to create a new one.`)
        } else {
          setShowUpsell(true)
        }
        return
      }
    }
    setLoading(true)
    setError('')
    try {
      const flatId = await createFlat({
        uid: user.uid,
        nickname: nickname.trim(),
        email: user.email,
        flatName: flatName.trim(),
      })
      if (isAddingFlat) {
        addFlatToState(flatId, flatName.trim())
        initFirestoreListeners(flatId)
      } else {
        addMemberMock(user.uid, nickname.trim(), user.email, 'admin')
        setFlatId(flatId)
        initFirestoreListeners(flatId)
      }
      router.push('/dashboard')
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Failed to create flat. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleJoinFlat = async () => {
    const code = inviteCode.trim().toUpperCase()
    if (!code || !nickname.trim()) return
    if (isAddingFlat) {
      if (allFlatIds.length >= maxFlats) {
        if (isActive) {
          setError(`You've reached the maximum of ${maxFlats} flats. Leave a flat first to join a new one.`)
        } else {
          setShowUpsell(true)
        }
        return
      }
    }
    setLoading(true)
    setError('')
    try {
      // Validate code first — give a clear message before anything else runs
      const codeExists = await flatExists(code)
      if (!codeExists) {
        setError("That invite code doesn't match any flat. Double-check it with your flatmate and try again.")
        setLoading(false)
        return
      }

      // Check if this flat requires approval before joining
      const joinMode = await getFlatJoinMode(code)
      if (joinMode === 'approval') {
        const req = await requestToJoinFlat({ uid: user.uid, nickname: nickname.trim(), email: user.email, flatId: code })
        if (!req.success) { setError(req.error || 'Failed to send request.'); setLoading(false); return }
        setStep('pending')
        setLoading(false)
        return
      }

      const result = await joinFlat({
        uid: user.uid,
        nickname: nickname.trim(),
        email: user.email,
        flatId: code,
      })
      if (!result.success) {
        setError(result.error || 'Failed to join flat.')
        setLoading(false)
        return
      }
      if (isAddingFlat) {
        addFlatToState(code, result.flatName ?? code)
        initFirestoreListeners(code)
      } else {
        addMemberMock(user.uid, nickname.trim(), user.email, 'member')
        setFlatId(code)
        initFirestoreListeners(code)
      }
      router.push('/dashboard')
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Failed to join flat. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  // ── Full-screen create form ────────────────────────────────────────────────
  if (step === 'create') {
    const cardBase = {
      background: 'rgba(14,11,8,0.82)',
      border: '1px solid rgba(255,255,255,0.1)',
      borderRadius: 14,
      backdropFilter: 'blur(14px)',
      boxShadow: '0 8px 32px rgba(0,0,0,0.5), inset 0 1px 0 rgba(255,255,255,0.07)',
      padding: '13px 15px',
    }
    return (
      <>

      {/* ── Desktop ── */}
      <div className="fixed inset-0 z-50 hidden lg:flex flex-row" style={{ background: '#0C0A08' }}>

        {/* LEFT: Visual story panel */}
        <div className="relative overflow-hidden lg:w-[56%] shrink-0">
          <img src="/onboard-create.png" alt="" className="absolute inset-0 w-full h-full object-cover" style={{ objectPosition: 'center 18%' }} />
          <div className="absolute inset-0" style={{ background: 'linear-gradient(to bottom, rgba(8,6,4,0.32) 0%, rgba(8,6,4,0.16) 28%, rgba(8,6,4,0.2) 54%, rgba(8,6,4,0.88) 75%, rgba(8,6,4,0.97) 100%)' }} />
          <div className="absolute inset-0" style={{ background: 'rgba(80,30,0,0.1)' }} />

          <button onClick={() => setStep('choose')} className="absolute top-5 left-5 z-20 flex items-center gap-1.5 text-white/38 hover:text-white/70 text-[12px] font-semibold transition-colors cursor-pointer">
            <ArrowLeft size={13} /> Back
          </button>

          {/* Floating card: Expense Split */}
          <motion.div className="absolute z-20 hidden lg:block" style={{ top: '13%', left: 28, width: 184 }}
            animate={{ y: [0, -6, 0] }} transition={{ duration: 4, repeat: Infinity, ease: 'easeInOut' }}>
            <div style={cardBase}>
              <div className="flex items-center gap-2 mb-2">
                <div className="w-6 h-6 rounded-lg flex items-center justify-center shrink-0" style={{ background: 'rgba(249,115,22,0.14)' }}>
                  <Wallet size={12} style={{ color: '#f97316' }} />
                </div>
                <span className="text-[11px] font-semibold" style={{ color: 'rgba(255,255,255,0.4)' }}>Expense Split</span>
              </div>
              <p className="font-black text-[22px] leading-none" style={{ color: '#4ade80' }}>₹4,560</p>
              <p className="text-[11px] mt-1" style={{ color: 'rgba(255,255,255,0.3)' }}>Groceries · 4 people</p>
              <div className="flex items-center mt-2">
                {([['S','#7c3aed'],['R','#ea580c'],['P','#16a34a'],['K','#2563eb']] as [string,string][]).map(([l,c],i) => (
                  <div key={i} className="w-[21px] h-[21px] rounded-full flex items-center justify-center text-[7px] font-bold text-white"
                    style={{ background: c, border: '1.5px solid rgba(14,11,8,0.92)', marginLeft: i === 0 ? 0 : -5, zIndex: i }}>
                    {l}
                  </div>
                ))}
                <div className="w-[21px] h-[21px] rounded-full flex items-center justify-center text-[7px] font-bold"
                  style={{ background: 'rgba(255,255,255,0.08)', border: '1.5px solid rgba(14,11,8,0.92)', marginLeft: -5, color: 'rgba(255,255,255,0.4)' }}>
                  +1
                </div>
              </div>
            </div>
          </motion.div>

          {/* Floating card: Chores */}
          <motion.div className="absolute z-20 hidden lg:block" style={{ top: '17%', right: 28, width: 163 }}
            animate={{ y: [0, -5, 0] }} transition={{ duration: 3.5, repeat: Infinity, ease: 'easeInOut', delay: 0.7 }}>
            <div style={cardBase}>
              <div className="flex items-center gap-2 mb-2">
                <div className="w-6 h-6 rounded-lg flex items-center justify-center shrink-0" style={{ background: 'rgba(167,139,250,0.14)' }}>
                  <RefreshCw size={11} style={{ color: '#a78bfa' }} />
                </div>
                <span className="text-[11px] font-semibold" style={{ color: 'rgba(255,255,255,0.4)' }}>Chores</span>
              </div>
              <p className="text-white font-bold text-[13px] leading-tight">Kitchen Cleaning</p>
              <div className="flex items-center gap-1.5 mt-2">
                <div className="w-[18px] h-[18px] rounded-full flex items-center justify-center text-[7px] font-bold text-white shrink-0" style={{ background: '#ea580c' }}>R</div>
                <span className="text-[11px]" style={{ color: 'rgba(255,255,255,0.3)' }}>Next: Rahul</span>
              </div>
            </div>
          </motion.div>

          {/* Floating card: Due Reminder */}
          <motion.div className="absolute z-20 hidden lg:block" style={{ bottom: '42%', left: 24, width: 168 }}
            animate={{ y: [0, -7, 0] }} transition={{ duration: 4.5, repeat: Infinity, ease: 'easeInOut', delay: 1.3 }}>
            <div style={cardBase}>
              <div className="flex items-center gap-2 mb-1.5">
                <Bell size={12} style={{ color: 'rgba(255,255,255,0.35)' }} />
                <span className="text-[11px] font-semibold" style={{ color: 'rgba(255,255,255,0.4)' }}>Due Reminder</span>
              </div>
              <p className="text-white font-bold text-[13px]">Electricity Bill</p>
              <p className="text-[12px] font-semibold mt-0.5" style={{ color: '#fb923c' }}>Due in 3 days</p>
            </div>
          </motion.div>

          {/* Floating card: Flat Balance */}
          <motion.div className="absolute z-20 hidden lg:block" style={{ bottom: '40%', right: 24, width: 153 }}
            animate={{ y: [0, -5, 0] }} transition={{ duration: 3.8, repeat: Infinity, ease: 'easeInOut', delay: 1.9 }}>
            <div style={cardBase}>
              <div className="flex items-center gap-2 mb-2">
                <div className="w-6 h-6 rounded-lg flex items-center justify-center shrink-0" style={{ background: 'rgba(74,222,128,0.12)' }}>
                  <BarChart2 size={12} style={{ color: '#4ade80' }} />
                </div>
                <span className="text-[11px] font-semibold" style={{ color: 'rgba(255,255,255,0.4)' }}>Flat Balance</span>
              </div>
              <p className="font-black text-[22px] leading-none" style={{ color: '#4ade80' }}>₹2,340</p>
              <p className="text-[11px] mt-1" style={{ color: 'rgba(255,255,255,0.3)' }}>Total Balance</p>
            </div>
          </motion.div>

          {/* Bottom: Headline + icon strip */}
          <div className="absolute bottom-0 left-0 right-0 z-20 px-7 pb-6">
            <h2 className="text-white font-black text-[28px] lg:text-[32px] tracking-tight leading-tight">
              Create the home<br/>
              <span style={{ color: '#f97316' }}>everyone shares.</span>
            </h2>
            <p className="mt-1.5 text-[12px] leading-relaxed" style={{ color: 'rgba(255,255,255,0.36)' }}>
              Track expenses, rotate chores, manage bills — together.
            </p>
            <div className="hidden lg:flex items-stretch mt-4 pt-4" style={{ borderTop: '1px solid rgba(255,255,255,0.06)' }}>
              {[
                { icon: <Wallet size={13} />, label: 'Split expenses', sub: 'fairly' },
                { icon: <RefreshCw size={13} />, label: 'Rotate chores', sub: 'automatically' },
                { icon: <Bell size={13} />, label: 'Manage bills', sub: 'together' },
                { icon: <BarChart2 size={13} />, label: 'Stay organized', sub: 'always' },
              ].map((item, i) => (
                <div key={i} className="flex-1 flex flex-col items-center gap-1.5 px-1" style={{ borderRight: i < 3 ? '1px solid rgba(255,255,255,0.07)' : 'none' }}>
                  <div className="w-7 h-7 rounded-full flex items-center justify-center" style={{ background: 'rgba(249,115,22,0.12)' }}>
                    <span style={{ color: '#f97316' }}>{item.icon}</span>
                  </div>
                  <p className="text-white text-[10px] font-bold text-center leading-tight">{item.label}</p>
                  <p className="text-[9px] text-center" style={{ color: 'rgba(255,255,255,0.26)' }}>{item.sub}</p>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* RIGHT: Form */}
        <div className="flex-1 flex flex-col justify-center px-7 py-8 overflow-y-auto" style={{ background: '#0F0D0A' }}>
          <div className="w-full max-w-[340px] mx-auto space-y-5">

            <div className="space-y-1.5">
              <p className="font-bold text-[14px]" style={{ color: '#f97316' }}>
                Welcome, {user.displayName?.split(' ')[0] ?? 'there'}! 👋
              </p>
              <h3 className="text-white font-black text-[26px] tracking-tight leading-none">Create Your Flat</h3>
              <p className="text-[13px] leading-relaxed" style={{ color: 'rgba(255,255,255,0.3)' }}>
                Name your shared space. You can invite roommates after.
              </p>
            </div>

            {error && (
              <div className="text-red-400 text-[13px] px-4 py-3 rounded-2xl" style={{ background: 'rgba(239,68,68,0.07)', border: '1px solid rgba(239,68,68,0.14)' }}>
                {error}
              </div>
            )}

            <div className="space-y-3">
              {/* Nickname — read-only if profile already has a name, editable otherwise */}
              <div className="space-y-1.5">
                <label className="text-[10px] font-black uppercase tracking-[0.16em]" style={{ color: 'rgba(255,255,255,0.26)' }}>Your Nickname</label>
                {user.displayName ? (
                  <div className="relative flex items-center rounded-2xl pl-10 pr-10 py-3.5" style={{ background: 'rgba(255,255,255,0.04)', border: '1px solid rgba(255,255,255,0.06)' }}>
                    <User size={14} className="absolute left-4" style={{ color: 'rgba(255,255,255,0.18)' }} />
                    <span className="text-[14px]" style={{ color: 'rgba(255,255,255,0.5)' }}>{user.displayName}</span>
                    <div className="absolute right-3 w-5 h-5 rounded-full flex items-center justify-center" style={{ background: '#16a34a' }}>
                      <Check size={11} className="text-white" />
                    </div>
                  </div>
                ) : (
                  <div className="relative">
                    <User size={14} className="absolute left-4 top-1/2 -translate-y-1/2 pointer-events-none" style={{ color: 'rgba(255,255,255,0.2)' }} />
                    <input
                      type="text" maxLength={30} value={nickname}
                      onChange={e => setNickname(e.target.value)}
                      placeholder="e.g. Sai"
                      className="w-full rounded-2xl pl-10 pr-10 py-3.5 text-[14px] text-white placeholder-white/18 focus:outline-none transition-[border-color]"
                      style={{ background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.08)' }}
                      onFocus={e => (e.currentTarget.style.borderColor = 'rgba(249,115,22,0.45)')}
                      onBlur={e => (e.currentTarget.style.borderColor = 'rgba(255,255,255,0.08)')}
                    />
                    {nickname.trim() && (
                      <div className="absolute right-3 top-1/2 -translate-y-1/2 w-5 h-5 rounded-full flex items-center justify-center pointer-events-none" style={{ background: '#16a34a' }}>
                        <Check size={11} className="text-white" />
                      </div>
                    )}
                  </div>
                )}
              </div>
              <div className="space-y-1.5">
                <label className="text-[10px] font-black uppercase tracking-[0.16em]" style={{ color: 'rgba(255,255,255,0.26)' }}>Flat Name</label>
                <div className="relative">
                  <Home size={14} className="absolute left-4 top-1/2 -translate-y-1/2 pointer-events-none" style={{ color: 'rgba(255,255,255,0.2)' }} />
                  <input
                    type="text" maxLength={50} value={flatName}
                    onChange={e => setFlatName(e.target.value)}
                    placeholder="e.g. The Boys Apartment"
                    className="w-full rounded-2xl pl-10 pr-10 py-3.5 text-[14px] text-white placeholder-white/18 focus:outline-none transition-[border-color]"
                    style={{ background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.08)' }}
                    onFocus={e => (e.currentTarget.style.borderColor = 'rgba(249,115,22,0.45)')}
                    onBlur={e => (e.currentTarget.style.borderColor = 'rgba(255,255,255,0.08)')}
                  />
                  {flatName.trim() && (
                    <button type="button" onClick={() => setFlatName('')} className="absolute right-3 top-1/2 -translate-y-1/2 w-5 h-5 rounded-full flex items-center justify-center cursor-pointer" style={{ background: 'rgba(255,255,255,0.1)' }}>
                      <X size={10} className="text-white/50" />
                    </button>
                  )}
                </div>
              </div>
            </div>

            <button
              disabled={!flatName.trim() || !nickname.trim() || loading}
              onClick={handleCreateFlat}
              className="w-full h-12 rounded-2xl font-black text-[15px] text-white flex items-center justify-center gap-2.5 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer transition-opacity"
              style={{ background: '#F97316', boxShadow: '0 4px 24px rgba(249,115,22,0.4)' }}
            >
              {loading
                ? <><Loader2 size={18} className="animate-spin" /> Creating...</>
                : <>Create Flat <ArrowRight size={16} /></>
              }
            </button>

            <div className="space-y-2.5">
              {[
                { Icon: Zap, label: 'Takes less than 30 seconds' },
                { Icon: UserPlus, label: 'Invite roommates after' },
                { Icon: Shield, label: 'Your data is secure and private' },
              ].map(({ Icon, label }, i) => (
                <div key={i} className="flex items-center gap-3">
                  <div className="w-[30px] h-[30px] rounded-xl flex items-center justify-center shrink-0" style={{ background: 'rgba(249,115,22,0.12)' }}>
                    <Icon size={13} style={{ color: '#f97316' }} />
                  </div>
                  <p className="text-[13px]" style={{ color: 'rgba(255,255,255,0.5)' }}>{label}</p>
                </div>
              ))}
            </div>

            <div className="flex items-center gap-3 rounded-2xl px-4 py-3.5" style={{ background: 'rgba(249,115,22,0.07)', border: '1px solid rgba(249,115,22,0.12)' }}>
              <div className="w-9 h-9 rounded-xl flex items-center justify-center shrink-0" style={{ background: 'rgba(249,115,22,0.15)' }}>
                <Lock size={16} style={{ color: '#f97316' }} />
              </div>
              <p className="text-[12px] leading-relaxed" style={{ color: 'rgba(255,255,255,0.45)' }}>
                You'll be the <span className="text-white font-semibold">admin</span> and can manage everything in your flat.
              </p>
            </div>
          </div>
        </div>

      </div>

      {/* ── Mobile: continuation of choice screen ── */}
      <div className="fixed inset-0 z-50 flex flex-col overflow-y-auto lg:hidden" style={{ background: '#0C0A08' }}>

        <button onClick={() => setStep('choose')} className="absolute top-5 left-5 z-20 w-9 h-9 rounded-full flex items-center justify-center cursor-pointer" style={{ background: 'rgba(255,255,255,0.08)' }}>
          <ArrowLeft size={16} className="text-white/60" />
        </button>

        {/* Hero strip — same warm apartment image, continuity with choice screen */}
        <div className="relative overflow-hidden shrink-0" style={{ height: 170 }}>
          <img src="/onboard-create.png" alt="" className="absolute inset-0 w-full h-full object-cover" style={{ objectPosition: 'center 18%' }} />
          <div className="absolute inset-0" style={{ background: 'linear-gradient(to bottom, rgba(8,6,4,0.22) 0%, rgba(8,6,4,0.08) 35%, rgba(8,6,4,0.72) 75%, rgba(8,6,4,0.97) 100%)' }} />
          <div className="absolute inset-0" style={{ background: 'rgba(80,30,0,0.08)' }} />
        </div>

        <div className="flex flex-col px-5 pt-5 pb-10">
          {/* Headline — mirrors the choice screen card text */}
          <h2 className="text-white font-black tracking-tight leading-[1.1]" style={{ fontSize: 'clamp(26px,7vw,32px)' }}>
            Your Flat.<br /><span style={{ color: '#FF6B00' }}>Your Rules.</span>
          </h2>
          <p className="text-[13px] mt-2 mb-6 leading-relaxed" style={{ color: 'rgba(168,176,197,0.5)' }}>
            Create your shared home and manage everything from one place.
          </p>

          {error && (
            <div className="text-red-400 text-[13px] px-4 py-3 rounded-2xl mb-4" style={{ background: 'rgba(239,68,68,0.07)', border: '1px solid rgba(239,68,68,0.14)' }}>
              {error}
            </div>
          )}

          <div className="space-y-1.5 mb-4">
            <label className="text-[10px] font-black uppercase tracking-[0.16em]" style={{ color: 'rgba(255,255,255,0.26)' }}>Flat Name</label>
            <div className="relative">
              <Home size={14} className="absolute left-4 top-1/2 -translate-y-1/2 pointer-events-none" style={{ color: 'rgba(255,255,255,0.2)' }} />
              <input
                type="text" maxLength={50} value={flatName}
                onChange={e => setFlatName(e.target.value)}
                placeholder="The Boys Apartment"
                className="w-full rounded-2xl pl-10 pr-10 py-3.5 text-[14px] text-white placeholder-white/18 focus:outline-none transition-[border-color]"
                style={{ background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.09)' }}
                onFocus={e => (e.currentTarget.style.borderColor = 'rgba(249,115,22,0.5)')}
                onBlur={e => (e.currentTarget.style.borderColor = 'rgba(255,255,255,0.09)')}
              />
              {flatName.trim() && (
                <button type="button" onClick={() => setFlatName('')} className="absolute right-3 top-1/2 -translate-y-1/2 w-5 h-5 rounded-full flex items-center justify-center cursor-pointer" style={{ background: 'rgba(255,255,255,0.1)' }}>
                  <X size={10} className="text-white/50" />
                </button>
              )}
            </div>
          </div>

          <button
            disabled={!flatName.trim() || loading}
            onClick={handleCreateFlat}
            className="w-full h-[52px] rounded-2xl font-black text-[15px] text-white flex items-center justify-center gap-2.5 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer transition-opacity mb-7"
            style={{ background: '#FF6B00', boxShadow: '0 4px 20px rgba(255,107,0,0.35)' }}
          >
            {loading
              ? <><Loader2 size={18} className="animate-spin" /> Creating...</>
              : <>Create Flat <ArrowRight size={16} /></>
            }
          </button>

          <div className="space-y-3 mb-6">
            {['Takes less than 30 seconds', 'Invite roommates later', 'You become the admin'].map((t, i) => (
              <div key={i} className="flex items-center gap-2.5">
                <Check size={13} className="shrink-0" style={{ color: '#f97316' }} />
                <p className="text-[13px]" style={{ color: 'rgba(255,255,255,0.45)' }}>{t}</p>
              </div>
            ))}
          </div>

          <p className="text-[11px] text-center" style={{ color: 'rgba(255,255,255,0.2)' }}>🔒 Your data is secure and private</p>
        </div>
      </div>

      {showUpsell && <SubscriptionUpsell feature="create_flat" onClose={() => setShowUpsell(false)} />}
      </>
    )
  }

  // ── Full-screen join form ──────────────────────────────────────────────────
  if (step === 'join') {
    const cardBase = {
      background: 'rgba(10,8,18,0.82)',
      border: '1px solid rgba(255,255,255,0.1)',
      borderRadius: 14,
      backdropFilter: 'blur(14px)',
      boxShadow: '0 8px 32px rgba(0,0,0,0.5), inset 0 1px 0 rgba(255,255,255,0.07)',
      padding: '13px 15px',
    }
    return (
      <>

      {/* ── Desktop ── */}
      <div className="fixed inset-0 z-50 hidden lg:flex flex-row" style={{ background: '#080810' }}>

        {/* LEFT: Visual story panel */}
        <div className="relative overflow-hidden lg:w-[56%] shrink-0">
          <img src="/onboard-join.png" alt="" className="absolute inset-0 w-full h-full object-cover" style={{ objectPosition: 'center center' }} />
          <div className="absolute inset-0" style={{ background: 'linear-gradient(to bottom, rgba(4,3,14,0.32) 0%, rgba(4,3,14,0.16) 28%, rgba(4,3,14,0.2) 54%, rgba(4,3,14,0.88) 75%, rgba(4,3,14,0.97) 100%)' }} />
          <div className="absolute inset-0" style={{ background: 'rgba(30,10,80,0.12)' }} />

          <button onClick={() => setStep('choose')} className="absolute top-5 left-5 z-20 flex items-center gap-1.5 text-white/38 hover:text-white/70 text-[12px] font-semibold transition-colors cursor-pointer">
            <ArrowLeft size={13} /> Back
          </button>

          {/* Floating card: Recent Activity */}
          <motion.div className="absolute z-20 hidden lg:block" style={{ top: '13%', left: 28, width: 190 }}
            animate={{ y: [0, -6, 0] }} transition={{ duration: 4, repeat: Infinity, ease: 'easeInOut' }}>
            <div style={cardBase}>
              <div className="flex items-center gap-2 mb-2">
                <div className="w-6 h-6 rounded-lg flex items-center justify-center shrink-0" style={{ background: 'rgba(74,222,128,0.14)' }}>
                  <Wallet size={12} style={{ color: '#4ade80' }} />
                </div>
                <span className="text-[11px] font-semibold" style={{ color: 'rgba(255,255,255,0.4)' }}>Activity</span>
              </div>
              <p className="text-white font-bold text-[13px] leading-tight">Rahul paid ₹1,200</p>
              <p className="text-[11px] mt-1" style={{ color: 'rgba(255,255,255,0.3)' }}>Electricity · Just now</p>
              <div className="flex items-center gap-1.5 mt-2">
                {([['R','#ea580c'],['P','#7c3aed'],['K','#16a34a']] as [string,string][]).map(([l,c],i) => (
                  <div key={i} className="w-[20px] h-[20px] rounded-full flex items-center justify-center text-[7px] font-bold text-white"
                    style={{ background: c, border: '1.5px solid rgba(10,8,18,0.92)', marginLeft: i === 0 ? 0 : -4 }}>
                    {l}
                  </div>
                ))}
                <span className="text-[10px] ml-1" style={{ color: 'rgba(255,255,255,0.3)' }}>+2 more</span>
              </div>
            </div>
          </motion.div>

          {/* Floating card: Chore Assigned */}
          <motion.div className="absolute z-20 hidden lg:block" style={{ top: '17%', right: 28, width: 166 }}
            animate={{ y: [0, -5, 0] }} transition={{ duration: 3.5, repeat: Infinity, ease: 'easeInOut', delay: 0.7 }}>
            <div style={cardBase}>
              <div className="flex items-center gap-2 mb-2">
                <div className="w-6 h-6 rounded-lg flex items-center justify-center shrink-0" style={{ background: 'rgba(167,139,250,0.14)' }}>
                  <RefreshCw size={11} style={{ color: '#a78bfa' }} />
                </div>
                <span className="text-[11px] font-semibold" style={{ color: 'rgba(255,255,255,0.4)' }}>Chore Assigned</span>
              </div>
              <p className="text-white font-bold text-[13px] leading-tight">Vacuum Living Room</p>
              <p className="text-[11px] mt-1" style={{ color: 'rgba(255,255,255,0.3)' }}>Tomorrow · Your turn</p>
            </div>
          </motion.div>

          {/* Floating card: Bill Reminder */}
          <motion.div className="absolute z-20 hidden lg:block" style={{ bottom: '42%', left: 24, width: 168 }}
            animate={{ y: [0, -7, 0] }} transition={{ duration: 4.5, repeat: Infinity, ease: 'easeInOut', delay: 1.3 }}>
            <div style={cardBase}>
              <div className="flex items-center gap-2 mb-1.5">
                <Bell size={12} style={{ color: 'rgba(255,255,255,0.35)' }} />
                <span className="text-[11px] font-semibold" style={{ color: 'rgba(255,255,255,0.4)' }}>Bill Reminder</span>
              </div>
              <p className="text-white font-bold text-[13px]">Internet Bill</p>
              <p className="text-[12px] font-semibold mt-0.5" style={{ color: '#fb923c' }}>Due in 5 days</p>
            </div>
          </motion.div>

          {/* Floating card: Settled */}
          <motion.div className="absolute z-20 hidden lg:block" style={{ bottom: '40%', right: 24, width: 158 }}
            animate={{ y: [0, -5, 0] }} transition={{ duration: 3.8, repeat: Infinity, ease: 'easeInOut', delay: 1.9 }}>
            <div style={cardBase}>
              <div className="flex items-center gap-2 mb-2">
                <div className="w-6 h-6 rounded-lg flex items-center justify-center shrink-0" style={{ background: 'rgba(74,222,128,0.12)' }}>
                  <Users size={12} style={{ color: '#4ade80' }} />
                </div>
                <span className="text-[11px] font-semibold" style={{ color: 'rgba(255,255,255,0.4)' }}>Flatmates</span>
              </div>
              <div className="flex items-center gap-1.5 mb-1">
                {([['R','#ea580c'],['P','#7c3aed'],['K','#16a34a']] as [string,string][]).map(([l,c],i) => (
                  <div key={i} className="w-[22px] h-[22px] rounded-full flex items-center justify-center text-[8px] font-bold text-white"
                    style={{ background: c, border: '1.5px solid rgba(10,8,18,0.92)', marginLeft: i === 0 ? 0 : -5 }}>
                    {l}
                  </div>
                ))}
              </div>
              <p className="text-[11px]" style={{ color: 'rgba(255,255,255,0.3)' }}>3 members active</p>
            </div>
          </motion.div>

          {/* Bottom: Headline + icon strip */}
          <div className="absolute bottom-0 left-0 right-0 z-20 px-7 pb-6">
            <h2 className="text-white font-black text-[28px] lg:text-[32px] tracking-tight leading-tight">
              Join your flat.<br/>
              <span style={{ color: '#a78bfa' }}>Everything is waiting.</span>
            </h2>
            <p className="mt-1.5 text-[12px] leading-relaxed" style={{ color: 'rgba(255,255,255,0.36)' }}>
              Expenses, chores, bills — all set up. You just walk in.
            </p>
            <div className="hidden lg:flex items-stretch mt-4 pt-4" style={{ borderTop: '1px solid rgba(255,255,255,0.06)' }}>
              {[
                { icon: <Wallet size={13} />, label: 'See who owes', sub: 'what' },
                { icon: <RefreshCw size={13} />, label: 'Chores ready', sub: 'assigned' },
                { icon: <Bell size={13} />, label: 'Bills split', sub: 'fairly' },
                { icon: <BarChart2 size={13} />, label: 'Always in', sub: 'the loop' },
              ].map((item, i) => (
                <div key={i} className="flex-1 flex flex-col items-center gap-1.5 px-1" style={{ borderRight: i < 3 ? '1px solid rgba(255,255,255,0.07)' : 'none' }}>
                  <div className="w-7 h-7 rounded-full flex items-center justify-center" style={{ background: 'rgba(167,139,250,0.12)' }}>
                    <span style={{ color: '#a78bfa' }}>{item.icon}</span>
                  </div>
                  <p className="text-white text-[10px] font-bold text-center leading-tight">{item.label}</p>
                  <p className="text-[9px] text-center" style={{ color: 'rgba(255,255,255,0.26)' }}>{item.sub}</p>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* RIGHT: Form */}
        <div className="flex-1 flex flex-col justify-center px-7 py-8 overflow-y-auto" style={{ background: '#0C0A14' }}>
          <div className="w-full max-w-[340px] mx-auto space-y-5">

            <div className="space-y-1.5">
              <p className="font-bold text-[14px]" style={{ color: '#a78bfa' }}>
                Welcome, {user.displayName?.split(' ')[0] ?? 'there'}! 👋
              </p>
              <h3 className="text-white font-black text-[26px] tracking-tight leading-none">Join Your Flat</h3>
              <p className="text-[13px] leading-relaxed" style={{ color: 'rgba(255,255,255,0.3)' }}>
                Get the invite code from your flatmate and step right in.
              </p>
            </div>

            {error && (
              <div className="text-red-400 text-[13px] px-4 py-3 rounded-2xl" style={{ background: 'rgba(239,68,68,0.07)', border: '1px solid rgba(239,68,68,0.14)' }}>
                {error}
              </div>
            )}

            <div className="space-y-3">
              {/* Nickname — read-only if profile already has a name */}
              <div className="space-y-1.5">
                <label className="text-[10px] font-black uppercase tracking-[0.16em]" style={{ color: 'rgba(255,255,255,0.26)' }}>Your Nickname</label>
                {user.displayName ? (
                  <div className="relative flex items-center rounded-2xl pl-10 pr-10 py-3.5" style={{ background: 'rgba(255,255,255,0.04)', border: '1px solid rgba(255,255,255,0.06)' }}>
                    <User size={14} className="absolute left-4" style={{ color: 'rgba(255,255,255,0.18)' }} />
                    <span className="text-[14px]" style={{ color: 'rgba(255,255,255,0.5)' }}>{user.displayName}</span>
                    <div className="absolute right-3 w-5 h-5 rounded-full flex items-center justify-center" style={{ background: '#16a34a' }}>
                      <Check size={11} className="text-white" />
                    </div>
                  </div>
                ) : (
                  <div className="relative">
                    <User size={14} className="absolute left-4 top-1/2 -translate-y-1/2 pointer-events-none" style={{ color: 'rgba(255,255,255,0.2)' }} />
                    <input
                      type="text" maxLength={30} value={nickname}
                      onChange={e => setNickname(e.target.value)}
                      placeholder="e.g. Rahul"
                      className="w-full rounded-2xl pl-10 pr-10 py-3.5 text-[14px] text-white placeholder-white/18 focus:outline-none transition-[border-color]"
                      style={{ background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.08)' }}
                      onFocus={e => (e.currentTarget.style.borderColor = 'rgba(124,58,237,0.5)')}
                      onBlur={e => (e.currentTarget.style.borderColor = 'rgba(255,255,255,0.08)')}
                    />
                    {nickname.trim() && (
                      <div className="absolute right-3 top-1/2 -translate-y-1/2 w-5 h-5 rounded-full flex items-center justify-center pointer-events-none" style={{ background: '#16a34a' }}>
                        <Check size={11} className="text-white" />
                      </div>
                    )}
                  </div>
                )}
              </div>
              <div className="space-y-1.5">
                <label className="text-[10px] font-black uppercase tracking-[0.16em]" style={{ color: 'rgba(255,255,255,0.26)' }}>Invite Code</label>
                <input
                  type="text" value={inviteCode}
                  onChange={e => setInviteCode(e.target.value.toUpperCase())}
                  placeholder="FLAT-A3B9"
                  className="w-full rounded-2xl px-4 py-3.5 text-[15px] text-white placeholder-white/18 focus:outline-none font-mono tracking-widest text-center transition-[border-color]"
                  style={{ background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.08)' }}
                  onFocus={e => (e.currentTarget.style.borderColor = 'rgba(124,58,237,0.5)')}
                  onBlur={e => (e.currentTarget.style.borderColor = 'rgba(255,255,255,0.08)')}
                />
              </div>
            </div>

            <button
              disabled={!inviteCode.trim() || !nickname.trim() || loading}
              onClick={handleJoinFlat}
              className="w-full h-12 rounded-2xl font-black text-[15px] text-white flex items-center justify-center gap-2.5 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer transition-opacity"
              style={{ background: '#7C3AED', boxShadow: '0 4px 24px rgba(124,58,237,0.4)' }}
            >
              {loading
                ? <><Loader2 size={18} className="animate-spin" /> Joining...</>
                : <>Join Flat <ArrowRight size={16} /></>
              }
            </button>

            <div className="space-y-2.5">
              {[
                { Icon: Zap, label: 'Step in instantly — no setup' },
                { Icon: Users, label: 'Your flatmates are waiting' },
                { Icon: Shield, label: 'Your data is secure and private' },
              ].map(({ Icon, label }, i) => (
                <div key={i} className="flex items-center gap-3">
                  <div className="w-[30px] h-[30px] rounded-xl flex items-center justify-center shrink-0" style={{ background: 'rgba(124,58,237,0.12)' }}>
                    <Icon size={13} style={{ color: '#a78bfa' }} />
                  </div>
                  <p className="text-[13px]" style={{ color: 'rgba(255,255,255,0.5)' }}>{label}</p>
                </div>
              ))}
            </div>

            <div className="flex items-center gap-3 rounded-2xl px-4 py-3.5" style={{ background: 'rgba(124,58,237,0.07)', border: '1px solid rgba(124,58,237,0.12)' }}>
              <div className="w-9 h-9 rounded-xl flex items-center justify-center shrink-0" style={{ background: 'rgba(124,58,237,0.15)' }}>
                <Users size={16} style={{ color: '#a78bfa' }} />
              </div>
              <p className="text-[12px] leading-relaxed" style={{ color: 'rgba(255,255,255,0.45)' }}>
                You'll join as a <span className="text-white font-semibold">member</span> — all expenses and chores are already set up.
              </p>
            </div>
          </div>
        </div>

      </div>

      {/* ── Mobile: continuation of choice screen ── */}
      <div className="fixed inset-0 z-50 flex flex-col overflow-y-auto lg:hidden" style={{ background: '#080810' }}>

        <button onClick={() => setStep('choose')} className="absolute top-5 left-5 z-20 w-9 h-9 rounded-full flex items-center justify-center cursor-pointer" style={{ background: 'rgba(255,255,255,0.08)' }}>
          <ArrowLeft size={16} className="text-white/60" />
        </button>

        {/* Hero strip — same purple atmosphere, continuity with choice screen */}
        <div className="relative overflow-hidden shrink-0" style={{ height: 170 }}>
          <img src="/onboard-join.png" alt="" className="absolute inset-0 w-full h-full object-cover" style={{ objectPosition: 'center center' }} />
          <div className="absolute inset-0" style={{ background: 'linear-gradient(to bottom, rgba(4,3,14,0.22) 0%, rgba(4,3,14,0.08) 35%, rgba(4,3,14,0.72) 75%, rgba(4,3,14,0.97) 100%)' }} />
          <div className="absolute inset-0" style={{ background: 'rgba(30,10,80,0.1)' }} />
        </div>

        <div className="flex flex-col px-5 pt-5 pb-10">
          {/* Headline — mirrors the choice screen card text */}
          <h2 className="text-white font-black tracking-tight leading-[1.1]" style={{ fontSize: 'clamp(26px,7vw,32px)' }}>
            Your Crew.<br /><span style={{ color: '#a78bfa' }}>Already Here.</span>
          </h2>
          <p className="text-[13px] mt-2 mb-6 leading-relaxed" style={{ color: 'rgba(168,176,197,0.5)' }}>
            Enter your invite code and join your shared home instantly.
          </p>

          {error && (
            <div className="text-red-400 text-[13px] px-4 py-3 rounded-2xl mb-4" style={{ background: 'rgba(239,68,68,0.07)', border: '1px solid rgba(239,68,68,0.14)' }}>
              {error}
            </div>
          )}

          <div className="space-y-3 mb-4">
            <div className="space-y-1.5">
              <label className="text-[10px] font-black uppercase tracking-[0.16em]" style={{ color: 'rgba(255,255,255,0.26)' }}>Your Nickname</label>
              <div className="relative">
                <User size={14} className="absolute left-4 top-1/2 -translate-y-1/2 pointer-events-none" style={{ color: 'rgba(255,255,255,0.2)' }} />
                <input
                  type="text" maxLength={30} value={nickname}
                  onChange={e => setNickname(e.target.value)}
                  placeholder="e.g. Rahul"
                  className="w-full rounded-2xl pl-10 pr-10 py-3.5 text-[14px] text-white placeholder-white/18 focus:outline-none transition-[border-color]"
                  style={{ background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.09)' }}
                  onFocus={e => (e.currentTarget.style.borderColor = 'rgba(124,92,255,0.5)')}
                  onBlur={e => (e.currentTarget.style.borderColor = 'rgba(255,255,255,0.09)')}
                />
                {nickname.trim() && (
                  <div className="absolute right-3 top-1/2 -translate-y-1/2 w-5 h-5 rounded-full flex items-center justify-center pointer-events-none" style={{ background: '#16a34a' }}>
                    <Check size={11} className="text-white" />
                  </div>
                )}
              </div>
            </div>
            <div className="space-y-1.5">
              <label className="text-[10px] font-black uppercase tracking-[0.16em]" style={{ color: 'rgba(255,255,255,0.26)' }}>Invite Code</label>
              <input
                type="text" value={inviteCode}
                onChange={e => setInviteCode(e.target.value.toUpperCase())}
                placeholder="HBTQ-4K29"
                className="w-full rounded-2xl px-4 py-3.5 text-[15px] text-white placeholder-white/18 focus:outline-none font-mono tracking-widest text-center transition-[border-color]"
                style={{ background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.09)' }}
                onFocus={e => (e.currentTarget.style.borderColor = 'rgba(124,92,255,0.5)')}
                onBlur={e => (e.currentTarget.style.borderColor = 'rgba(255,255,255,0.09)')}
              />
            </div>
          </div>

          <button
            disabled={!inviteCode.trim() || !nickname.trim() || loading}
            onClick={handleJoinFlat}
            className="w-full h-[52px] rounded-2xl font-black text-[15px] text-white flex items-center justify-center gap-2.5 disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer transition-opacity mb-7"
            style={{ background: '#7C5CFF', boxShadow: '0 4px 20px rgba(124,92,255,0.35)' }}
          >
            {loading
              ? <><Loader2 size={18} className="animate-spin" /> Joining...</>
              : <>Join Flat <ArrowRight size={16} /></>
            }
          </button>

          <div className="space-y-3 mb-6">
            {['Step in instantly — no setup', 'Expenses and chores ready', 'Your flatmates are waiting'].map((t, i) => (
              <div key={i} className="flex items-center gap-2.5">
                <Check size={13} className="shrink-0" style={{ color: '#a78bfa' }} />
                <p className="text-[13px]" style={{ color: 'rgba(255,255,255,0.45)' }}>{t}</p>
              </div>
            ))}
          </div>

          <p className="text-[11px] text-center" style={{ color: 'rgba(255,255,255,0.2)' }}>🔒 Your data is secure and private</p>
        </div>
      </div>

      {showUpsell && <SubscriptionUpsell feature="create_flat" onClose={() => setShowUpsell(false)} />}
      </>
    )
  }

  // ── Pending approval screen ───────────────────────────────────────────────
  return (
    <div className="min-h-screen flex items-center justify-center bg-secondary/30 p-4">
      <Card className="w-full max-w-md shadow-md border-primary/30">
        <CardContent className="p-8 flex flex-col items-center text-center gap-4">
          <div className="w-14 h-14 bg-orange-100 dark:bg-orange-900/30 rounded-2xl flex items-center justify-center">
            <Clock size={28} className="text-orange-600 dark:text-orange-400 animate-pulse" />
          </div>
          <div>
            <h2 className="text-xl font-bold">Request Sent</h2>
            <p className="text-sm text-muted-foreground mt-2 max-w-xs">
              The admin needs to approve your request before you can access the flat. You&apos;ll be able to sign in once approved.
            </p>
          </div>
          <Button variant="outline" onClick={() => router.push('/')} className="mt-2">
            Back to Sign In
          </Button>
        </CardContent>
      </Card>
      {showUpsell && <SubscriptionUpsell feature="create_flat" onClose={() => setShowUpsell(false)} />}
    </div>
  )
}

export default function OnboardingPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 size={32} className="animate-spin text-primary" />
      </div>
    }>
      <OnboardingContent />
    </Suspense>
  )
}
