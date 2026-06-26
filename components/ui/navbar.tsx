"use client"

import { useState, useEffect, useRef } from "react"
import { motion, AnimatePresence, useMotionValue, useSpring, LayoutGroup } from "framer-motion"
import { useRouter } from "next/navigation"
import { useAuthStore } from "@/store/useAuthStore"
import { hasKeys } from "@/lib/firebase"
import {
  Bot, Search, ShieldCheck, Gauge,
  ChevronDown, ArrowRight, X,
} from "lucide-react"
import { cn } from "@/lib/utils"

// ── Google Icon ──────────────────────────────────────────────────────────────
function GIcon({ s = 16 }: { s?: number }) {
  return (
    <svg width={s} height={s} viewBox="0 0 48 48" fill="none" aria-hidden="true">
      <path d="M47.53 24.56c0-1.64-.15-3.22-.42-4.74H24v8.97h13.22c-.57 3.07-2.3 5.67-4.9 7.41v6.16h7.93c4.64-4.27 7.28-10.57 7.28-17.8z" fill="#4285F4"/>
      <path d="M24 48c6.64 0 12.21-2.2 16.28-5.96l-7.93-6.16c-2.2 1.48-5.02 2.35-8.35 2.35-6.42 0-11.86-4.33-13.8-10.15H2.02v6.36C6.08 42.63 14.42 48 24 48z" fill="#34A853"/>
      <path d="M10.2 28.08A14.93 14.93 0 0 1 9.1 24c0-1.41.24-2.79.65-4.08v-6.36H2.02A23.97 23.97 0 0 0 0 24c0 3.87.93 7.53 2.57 10.77l7.63-6.69z" fill="#FBBC05"/>
      <path d="M24 9.55c3.62 0 6.86 1.24 9.42 3.68l7.07-7.07C36.2 2.24 30.63 0 24 0 14.42 0 6.08 5.37 2.57 13.23l8.13 6.35C12.14 13.88 17.58 9.55 24 9.55z" fill="#EA4335"/>
    </svg>
  )
}

// ── Nav Item Definitions ────────────────────────────────────────────────────
const NAV_ITEMS = [
  { label: "Features",     href: "#features",    hasDropdown: true  },
  { label: "Compare",      href: "#compare",     hasDropdown: false },
  { label: "How it works", href: "#how-it-works",hasDropdown: false },
  { label: "Pricing",      href: "#get-started", hasDropdown: false },
] as const

const DROPDOWN_FEATURES = [
  {
    icon: <Bot size={17} className="text-violet-400" />,
    label: "AI Matching",
    desc:  "Finds compatible flatmates using behavioral patterns",
    bg:    "rgba(124,58,237,0.13)",
    border:"rgba(124,58,237,0.22)",
  },
  {
    icon: <Search size={17} className="text-blue-400" />,
    label: "Smart Search",
    desc:  "Natural language flat search across verified listings",
    bg:    "rgba(59,130,246,0.13)",
    border:"rgba(59,130,246,0.22)",
  },
  {
    icon: <ShieldCheck size={17} className="text-emerald-400" />,
    label: "Verified Profiles",
    desc:  "ID-backed profiles with trust scores and reviews",
    bg:    "rgba(16,185,129,0.13)",
    border:"rgba(16,185,129,0.22)",
  },
  {
    icon: <Gauge size={17} className="text-amber-400" />,
    label: "Compatibility Score",
    desc:  "Dynamic scores based on habits, schedule and lifestyle",
    bg:    "rgba(245,158,11,0.13)",
    border:"rgba(245,158,11,0.22)",
  },
]

// ── Magnetic Button ──────────────────────────────────────────────────────────
interface MagButtonProps {
  children: React.ReactNode
  className?: string
  style?: React.CSSProperties
  onClick?: () => void
  disabled?: boolean
  type?: "button" | "submit" | "reset"
  "aria-label"?: string
}
function MagButton({ children, className, onClick, style, disabled, type = "button", ...rest }: MagButtonProps) {
  const x = useMotionValue(0)
  const y = useMotionValue(0)
  const sx = useSpring(x, { stiffness: 350, damping: 22 })
  const sy = useSpring(y, { stiffness: 350, damping: 22 })

  const onMove = (e: React.MouseEvent<HTMLButtonElement>) => {
    const r = e.currentTarget.getBoundingClientRect()
    x.set((e.clientX - r.left - r.width  / 2) * 0.2)
    y.set((e.clientY - r.top  - r.height / 2) * 0.2)
  }

  return (
    <motion.button
      type={type}
      style={{ x: sx, y: sy, ...style }}
      onMouseMove={onMove}
      onMouseLeave={() => { x.set(0); y.set(0) }}
      whileTap={{ scale: 0.95 }}
      onClick={onClick}
      disabled={disabled}
      className={cn("cursor-pointer", className)}
      {...rest}
    >
      {children}
    </motion.button>
  )
}

// ── Auth Form (dropdown + inline variants) ───────────────────────────────────
export function AuthForm({ onClose, inline = false }: { onClose?: () => void; inline?: boolean }) {
  const {
    loginWithGoogle, loginWithEmail, signUpWithEmail, resetPassword,
    loginAsAdminMock, loginAsMemberMock,
    redirectError, clearRedirectError,
  } = useAuthStore()

  const [mode, setMode]           = useState<"in" | "up" | "reset">("in")
  const [email, setEmail]         = useState("")
  const [pw, setPw]               = useState("")
  const [nick, setNick]           = useState("")
  const [err, setErr]             = useState("")
  const [loading, setLoading]     = useState(false)
  const [resetSent, setResetSent] = useState(false)

  useEffect(() => {
    if (redirectError) { setErr(redirectError); clearRedirectError?.() }
  }, [redirectError, clearRedirectError])

  function toMsg(e: unknown) {
    const c = (e as { code?: string })?.code ?? ""
    if (c.includes("user-not-found") || c.includes("wrong-password") || c.includes("invalid-credential"))
      return "Incorrect email or password."
    if (c.includes("email-already-in-use")) return "Account already exists."
    if (c.includes("weak-password"))        return "Password must be 6+ characters."
    if (c.includes("popup-closed") || c.includes("cancelled-popup")) return ""
    return "Authentication failed. Try again."
  }

  async function onEmail(e: React.FormEvent) {
    e.preventDefault(); setErr(""); setLoading(true)
    try {
      if (mode === "in") {
        await loginWithEmail(email, pw)
      } else {
        if (!nick.trim()) { setErr("Nickname required."); setLoading(false); return }
        await signUpWithEmail(email, pw, nick.trim())
      }
    } catch (err) {
      const m = toMsg(err); if (m) setErr(m)
    } finally { setLoading(false) }
  }

  async function onGoogle() {
    setErr(""); setLoading(true)
    try { await loginWithGoogle() } catch (err) { const m = toMsg(err); if (m) setErr(m); setLoading(false) }
  }

  async function onReset(e: React.FormEvent) {
    e.preventDefault(); setErr(""); setLoading(true)
    try {
      await resetPassword(email.trim())
      setResetSent(true)
    } catch (err) {
      const c = (err as { code?: string })?.code ?? ""
      if (c.includes("user-not-found")) {
        // Don't leak whether the email exists — show success anyway
        setResetSent(true)
      } else {
        setErr("Failed to send reset email. Try again.")
      }
    } finally { setLoading(false) }
  }

  const inp = [
    "w-full h-10 px-3.5 rounded-xl text-sm text-white",
    "bg-white/[0.06] border border-white/[0.1]",
    "placeholder:text-white/25 outline-none",
    "focus:border-violet-500/50 focus:bg-violet-900/10",
    "transition-all duration-200",
  ].join(" ")

  const closeButton = !inline && onClose && (
    <button
      onClick={onClose}
      className="w-7 h-7 rounded-lg flex items-center justify-center text-white/30 hover:text-white hover:bg-white/[0.08] transition-all cursor-pointer shrink-0"
      aria-label="Close"
    >
      <X size={13} />
    </button>
  )

  const formBody = mode === "reset" ? (
    <>
      {/* Reset password header */}
      <div className="flex items-center justify-between mb-4">
        <div>
          <p className="text-white font-semibold text-[15px] leading-tight">Reset password</p>
          <p className="text-white/35 text-[11px] mt-0.5">
            {resetSent ? "Check your inbox" : "We'll email you a reset link"}
          </p>
        </div>
        {closeButton}
      </div>

      {resetSent ? (
        <div className="flex flex-col items-center text-center gap-3 py-2">
          <div className="w-10 h-10 rounded-2xl bg-emerald-500/15 border border-emerald-500/25 flex items-center justify-center">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#34d399" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M20 6L9 17l-5-5"/>
            </svg>
          </div>
          <p className="text-white/70 text-[12.5px] leading-relaxed max-w-[220px]">
            If an account exists for <span className="text-white font-medium">{email}</span>, a reset link is on its way.
          </p>
          <button
            onClick={() => { setMode("in"); setResetSent(false); setErr("") }}
            className="text-violet-400 text-[11.5px] hover:text-violet-300 transition-colors cursor-pointer mt-1"
          >
            ← Back to sign in
          </button>
        </div>
      ) : (
        <>
          <AnimatePresence>
            {err && (
              <motion.p
                initial={{ opacity: 0, y: -4, height: 0 }}
                animate={{ opacity: 1, y: 0, height: "auto" }}
                exit={{ opacity: 0, y: -4, height: 0 }}
                className="text-red-400 text-xs font-medium mb-3 px-3 py-2 bg-red-500/10 border border-red-500/20 rounded-xl overflow-hidden"
              >
                {err}
              </motion.p>
            )}
          </AnimatePresence>
          <form onSubmit={onReset} className="flex flex-col gap-2">
            <input
              type="email"
              placeholder="Your email address"
              value={email}
              maxLength={254}
              onChange={e => setEmail(e.target.value)}
              className={inp}
              required
              autoFocus
            />
            <MagButton
              type="submit"
              disabled={loading}
              className="relative overflow-hidden w-full h-10 rounded-xl font-semibold text-white text-sm mt-0.5 disabled:opacity-50"
              style={{ background: "linear-gradient(135deg, #7c3aed, #4f46e5)", boxShadow: "0 4px 16px rgba(124,58,237,0.35)" }}
            >
              <span className="relative z-10">{loading ? "Sending…" : "Send Reset Link"}</span>
            </MagButton>
          </form>
          <button
            onClick={() => { setMode("in"); setErr("") }}
            className="w-full text-white/28 text-[11.5px] text-center hover:text-white/55 transition-colors cursor-pointer mt-3"
          >
            ← Back to sign in
          </button>
        </>
      )}
    </>
  ) : (
    <>
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <div>
          <p className="text-white font-semibold text-[15px] leading-tight">
            {mode === "in" ? "Welcome back" : "Create account"}
          </p>
          <p className="text-white/35 text-[11px] mt-0.5">
            {mode === "in" ? "Sign in to your Habitiq account" : "Start managing your flat today"}
          </p>
        </div>
        {closeButton}
      </div>

      {/* Error */}
      <AnimatePresence>
        {err && (
          <motion.p
            initial={{ opacity: 0, y: -4, height: 0 }}
            animate={{ opacity: 1, y: 0, height: "auto" }}
            exit={{ opacity: 0, y: -4, height: 0 }}
            className="text-red-400 text-xs font-medium mb-3 px-3 py-2 bg-red-500/10 border border-red-500/20 rounded-xl overflow-hidden"
          >
            {err}
          </motion.p>
        )}
      </AnimatePresence>

      {/* Google button */}
      <button
        onClick={onGoogle}
        disabled={loading}
        className="w-full flex items-center justify-center gap-2.5 bg-white text-gray-900 font-semibold h-10 px-4 rounded-xl hover:bg-gray-50 active:scale-[0.98] transition-all text-sm disabled:opacity-50 cursor-pointer mb-3"
      >
        <GIcon s={15} /> Continue with Google
      </button>

      {/* Divider */}
      <div className="flex items-center gap-2.5 mb-3">
        <div className="flex-1 h-px bg-white/[0.07]" />
        <span className="text-white/20 text-[11px] font-medium">or</span>
        <div className="flex-1 h-px bg-white/[0.07]" />
      </div>

      {/* Email form */}
      <form onSubmit={onEmail} className="flex flex-col gap-2">
        {mode === "up" && (
          <input type="text" placeholder="Your nickname" value={nick} maxLength={30}
            onChange={e => setNick(e.target.value)} className={inp} />
        )}
        <input type="email" placeholder="Email address" value={email} maxLength={254}
          onChange={e => setEmail(e.target.value)} className={inp} required />
        <div className="relative">
          <input type="password" placeholder="Password" value={pw} minLength={6} maxLength={128}
            onChange={e => setPw(e.target.value)} className={inp} required />
          {mode === "in" && (
            <button
              type="button"
              onClick={() => { setMode("reset"); setErr("") }}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-white/30 text-[11px] hover:text-violet-400 transition-colors cursor-pointer whitespace-nowrap"
            >
              Forgot?
            </button>
          )}
        </div>

        <MagButton
          type="submit"
          disabled={loading}
          className="relative overflow-hidden w-full h-10 rounded-xl font-semibold text-white text-sm mt-0.5 disabled:opacity-50"
          style={{ background: "linear-gradient(135deg, #7c3aed, #4f46e5)", boxShadow: "0 4px 16px rgba(124,58,237,0.35)" }}
        >
          <span className="relative z-10">
            {loading ? "Please wait…" : mode === "in" ? "Sign In" : "Create Account"}
          </span>
        </MagButton>
      </form>

      {/* Mode toggle */}
      <button
        onClick={() => { setMode(m => (m === "in" ? "up" : "in")); setErr("") }}
        className="w-full text-white/28 text-[11.5px] text-center hover:text-white/55 transition-colors cursor-pointer mt-3"
      >
        {mode === "in" ? "No account? Sign up free →" : "Already have one? Sign in →"}
      </button>

      {/* Dev mock buttons */}
      {!hasKeys && (
        <div className="flex gap-2 mt-3 pt-3 border-t border-white/[0.05]">
          <button onClick={loginAsAdminMock}
            className="flex-1 text-[11px] bg-amber-500/15 hover:bg-amber-500/22 text-amber-300 h-8 rounded-xl transition-colors cursor-pointer">
            Mock Admin
          </button>
          <button onClick={loginAsMemberMock}
            className="flex-1 text-[11px] bg-amber-500/15 hover:bg-amber-500/22 text-amber-300 h-8 rounded-xl transition-colors cursor-pointer">
            Mock Member
          </button>
        </div>
      )}
    </>
  )

  if (inline) {
    return <div className="px-5 pb-5">{formBody}</div>
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: -10, scale: 0.96 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      exit={{ opacity: 0, y: -8, scale: 0.96 }}
      transition={{ type: "spring", stiffness: 300, damping: 28 }}
      className="absolute right-0 top-full mt-3 w-[330px] z-50"
      style={{
        background: "rgba(8,6,22,0.97)",
        backdropFilter: "blur(28px)",
        border: "1px solid rgba(255,255,255,0.1)",
        borderRadius: "20px",
        boxShadow: "0 24px 64px rgba(0,0,0,0.7), 0 0 0 0.5px rgba(124,58,237,0.15)",
        padding: "20px",
      }}
    >
      {/* Top glow line */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-32 h-px"
        style={{ background: "linear-gradient(90deg, transparent, rgba(124,58,237,0.7), transparent)" }} />
      {formBody}
    </motion.div>
  )
}

// ── Features Dropdown ────────────────────────────────────────────────────────
function FeaturesDropdown({ onClose }: { onClose: () => void }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: -12, scale: 0.95 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      exit={{ opacity: 0, y: -10, scale: 0.96 }}
      transition={{ type: "spring", stiffness: 320, damping: 26 }}
      className="absolute left-1/2 -translate-x-1/2 top-full mt-3 w-[400px] z-50"
      style={{
        background: "rgba(8,6,22,0.97)",
        backdropFilter: "blur(28px)",
        border: "1px solid rgba(255,255,255,0.09)",
        borderRadius: "20px",
        boxShadow: "0 24px 64px rgba(0,0,0,0.7), 0 0 0 0.5px rgba(124,58,237,0.1)",
        padding: "12px",
      }}
    >
      {/* Top glow line */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-24 h-px"
        style={{ background: "linear-gradient(90deg, transparent, rgba(124,58,237,0.6), transparent)" }} />

      <div className="grid grid-cols-2 gap-1.5">
        {DROPDOWN_FEATURES.map((f, i) => (
          <motion.a
            key={f.label}
            href="#features"
            onClick={onClose}
            initial={{ opacity: 0, y: 6 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: i * 0.045, type: "spring", stiffness: 300, damping: 28 }}
            whileHover={{ y: -2 }}
            className="group relative flex flex-col gap-3 p-3.5 rounded-2xl cursor-pointer transition-all duration-200"
            style={{ background: "rgba(255,255,255,0.02)", border: "1px solid rgba(255,255,255,0.06)" }}
            onMouseEnter={(e) => {
              (e.currentTarget as HTMLElement).style.background = "rgba(255,255,255,0.05)"
              ;(e.currentTarget as HTMLElement).style.borderColor = f.border
            }}
            onMouseLeave={(e) => {
              (e.currentTarget as HTMLElement).style.background = "rgba(255,255,255,0.02)"
              ;(e.currentTarget as HTMLElement).style.borderColor = "rgba(255,255,255,0.06)"
            }}
          >
            {/* Icon */}
            <div className="w-8 h-8 rounded-xl flex items-center justify-center shrink-0"
              style={{ background: f.bg, border: `1px solid ${f.border}` }}>
              {f.icon}
            </div>
            {/* Text */}
            <div>
              <div className="flex items-center gap-1 mb-1">
                <p className="text-white text-[12.5px] font-semibold leading-tight">{f.label}</p>
                <ArrowRight
                  size={10}
                  className="text-white/0 group-hover:text-white/45 transition-all duration-200 translate-x-[-2px] group-hover:translate-x-0"
                />
              </div>
              <p className="text-white/38 text-[11px] leading-relaxed">{f.desc}</p>
            </div>
          </motion.a>
        ))}
      </div>

      {/* Footer link */}
      <div className="mt-2 pt-2 border-t border-white/[0.05] px-1">
        <a href="#features" onClick={onClose}
          className="flex items-center justify-between px-2 py-2 rounded-xl text-white/35 hover:text-white/65 text-[11.5px] font-medium transition-colors cursor-pointer group">
          <span>View all features</span>
          <ArrowRight size={11} className="group-hover:translate-x-0.5 transition-transform" />
        </a>
      </div>
    </motion.div>
  )
}

// ── Mobile Menu ──────────────────────────────────────────────────────────────
function MobileMenu({
  open, onClose, onSignIn,
}: {
  open: boolean
  onClose: () => void
  onSignIn: () => void
}) {
  useEffect(() => {
    if (!open) return
    const prev = document.body.style.overflow
    document.body.style.overflow = "hidden"
    return () => { document.body.style.overflow = prev }
  }, [open])

  useEffect(() => {
    const h = (e: KeyboardEvent) => { if (e.key === "Escape") onClose() }
    window.addEventListener("keydown", h)
    return () => window.removeEventListener("keydown", h)
  }, [onClose])

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.22 }}
          className="fixed inset-0 z-40 flex flex-col"
          style={{ background: "rgba(5,5,16,0.98)", backdropFilter: "blur(28px)" }}
          role="dialog"
          aria-modal="true"
          aria-label="Navigation"
        >
          {/* Aurora blob */}
          <div className="absolute top-[-20%] left-[-10%] w-[500px] h-[500px] rounded-full pointer-events-none"
            style={{ background: "radial-gradient(circle, rgba(124,58,237,0.12) 0%, transparent 70%)", filter: "blur(80px)" }} />

          {/* Close */}
          <div className="flex items-center justify-between px-5 pt-5 pb-2">
            <a href="#" className="flex items-center gap-2.5" onClick={onClose}>
              <img
                src="/habitiq-app-icon.png"
                alt="Habitiq"
                className="w-8 h-8 rounded-[9px] object-cover"
                style={{ boxShadow: "0 0 16px rgba(124,58,237,0.4)" }}
              />
              <span className="text-white font-bold text-[15px] tracking-tight">Habitiq</span>
            </a>
            <motion.button
              onClick={onClose}
              whileTap={{ scale: 0.9 }}
              className="w-10 h-10 rounded-2xl flex items-center justify-center cursor-pointer"
              style={{ background: "rgba(255,255,255,0.06)", border: "1px solid rgba(255,255,255,0.1)" }}
              aria-label="Close menu"
            >
              <X size={17} className="text-white/60" />
            </motion.button>
          </div>

          {/* Nav links */}
          <nav className="flex-1 flex flex-col justify-center px-5 gap-1">
            {NAV_ITEMS.map((item, i) => (
              <motion.a
                key={item.href}
                href={item.href}
                onClick={onClose}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.06 + i * 0.07, type: "spring", stiffness: 300, damping: 28 }}
                className="group flex items-center justify-between py-4 border-b cursor-pointer"
                style={{ borderColor: "rgba(255,255,255,0.06)" }}
              >
                <span className="text-white/70 text-[1.65rem] font-semibold tracking-tight group-hover:text-white transition-colors">
                  {item.label}
                </span>
                <div className="w-8 h-8 rounded-xl flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                  style={{ background: "rgba(124,58,237,0.15)", border: "1px solid rgba(124,58,237,0.25)" }}>
                  <ArrowRight size={14} className="text-violet-400" />
                </div>
              </motion.a>
            ))}
          </nav>

          {/* CTA buttons */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.38, type: "spring", stiffness: 260, damping: 28 }}
            className="p-5 pb-8 flex flex-col gap-3"
          >
            <button
              onClick={() => { onClose(); setTimeout(onSignIn, 300) }}
              className="w-full h-12 rounded-2xl font-semibold text-[15px] text-white/65 hover:text-white transition-all cursor-pointer"
              style={{
                background: "rgba(255,255,255,0.05)",
                border: "1px solid rgba(255,255,255,0.12)",
              }}
            >
              Sign In
            </button>
            <button
              onClick={onClose}
              className="relative overflow-hidden w-full h-12 rounded-2xl font-semibold text-[15px] text-white cursor-pointer"
              style={{
                background: "linear-gradient(135deg, #7c3aed, #4f46e5)",
                boxShadow: "0 8px 28px rgba(124,58,237,0.45)",
              }}
            >
              Get Started <ArrowRight size={14} className="inline ml-1.5 mb-0.5" />
            </button>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}

// ── Navbar (main export) ─────────────────────────────────────────────────────
export function Navbar() {
  const { user }  = useAuthStore()
  const router    = useRouter()

  const [scrolled,      setScrolled]      = useState(false)
  const [hoveredNav,    setHoveredNav]    = useState<string | null>(null)
  const [featuresOpen,  setFeaturesOpen]  = useState(false)
  const [authOpen,      setAuthOpen]      = useState(false)
  const [mobileOpen,    setMobileOpen]    = useState(false)

  const featuresRef = useRef<HTMLDivElement>(null)
  const authRef     = useRef<HTMLDivElement>(null)

  useEffect(() => { if (user) router.push("/dashboard") }, [user, router])

  useEffect(() => {
    const h = () => setScrolled(window.scrollY > 24)
    window.addEventListener("scroll", h, { passive: true })
    h()
    return () => window.removeEventListener("scroll", h)
  }, [])

  // Close dropdowns on outside click
  useEffect(() => {
    function outside(e: MouseEvent) {
      if (featuresRef.current && !featuresRef.current.contains(e.target as Node))
        setFeaturesOpen(false)
      if (authRef.current && !authRef.current.contains(e.target as Node))
        setAuthOpen(false)
    }
    document.addEventListener("mousedown", outside)
    return () => document.removeEventListener("mousedown", outside)
  }, [])

  return (
    <>
      {/* ─────────────────────────────────── Fixed Header ── */}
      <header
        className="fixed left-0 right-0 z-50 flex justify-center pointer-events-none"
        style={{
          top: 0,
          paddingTop:   scrolled ? "10px" : "16px",
          paddingLeft:  "16px",
          paddingRight: "16px",
          transition:   "padding 500ms cubic-bezier(0.32,0.72,0,1)",
        }}
      >
        <div className="w-full max-w-[1400px] pointer-events-auto">
          <motion.nav
            role="navigation"
            aria-label="Main navigation"
            className="relative flex items-center justify-between rounded-2xl"
            animate={{
              height:      scrolled ? 58 : 68,
              paddingLeft: scrolled ? 18 : 24,
              paddingRight:scrolled ? 18 : 24,
            }}
            style={{
              background:       scrolled ? "rgba(5,5,16,0.88)" : "rgba(5,5,16,0.35)",
              border:           scrolled ? "1px solid rgba(255,255,255,0.09)" : "1px solid rgba(255,255,255,0.05)",
              boxShadow:        scrolled
                ? "0 8px 40px rgba(0,0,0,0.55), 0 1px 0 rgba(255,255,255,0.05) inset, 0 0 0 0.5px rgba(124,58,237,0.08)"
                : "0 2px 12px rgba(0,0,0,0.2)",
              backdropFilter:        scrolled ? "blur(28px) saturate(180%)" : "blur(14px)",
              WebkitBackdropFilter:  scrolled ? "blur(28px) saturate(180%)" : "blur(14px)",
              transition:       "background 500ms ease, border 500ms ease, box-shadow 500ms ease, backdrop-filter 500ms ease",
            }}
            transition={{ duration: 0.5, ease: [0.32, 0.72, 0, 1] }}
          >

            {/* ── Logo ────────────────────────────────────────── */}
            <a href="#" className="flex items-center gap-2.5 shrink-0 group cursor-pointer">
              <div className="relative">
                <motion.div
                  className="absolute inset-0 rounded-[10px] pointer-events-none"
                  animate={{ opacity: [0.4, 0.75, 0.4] }}
                  transition={{ duration: 3, repeat: Infinity, ease: "easeInOut" }}
                  style={{ boxShadow: "0 0 20px rgba(124,58,237,0.5)" }}
                />
                <img
                  src="/habitiq-app-icon.png"
                  alt="Habitiq"
                  className="w-9 h-9 rounded-[10px] object-cover relative z-10"
                  style={{ boxShadow: "0 4px 14px rgba(124,58,237,0.45)" }}
                />
              </div>
              <span className="text-white font-bold text-[15px] tracking-tight group-hover:text-white/90 transition-colors">
                Habitiq
              </span>
            </a>

            {/* ── Desktop Nav ─────────────────────────────────── */}
            <nav className="hidden lg:flex items-center gap-0.5 absolute left-1/2 -translate-x-1/2">
              <LayoutGroup id="nav">
                {NAV_ITEMS.map(item => {
                  const isF = item.hasDropdown
                  return (
                    <div
                      key={item.label}
                      ref={isF ? featuresRef : undefined}
                      className="relative"
                      onMouseEnter={() => { setHoveredNav(item.label); if (isF) setFeaturesOpen(true) }}
                      onMouseLeave={() => { setHoveredNav(null); if (isF) setFeaturesOpen(false) }}
                    >
                      {/* Sliding hover pill */}
                      {hoveredNav === item.label && (
                        <motion.div
                          layoutId="nav-pill"
                          className="absolute inset-0 rounded-xl pointer-events-none"
                          style={{ background: "rgba(255,255,255,0.07)" }}
                          transition={{ type: "spring", stiffness: 420, damping: 34 }}
                        />
                      )}

                      <a
                        href={isF ? undefined : item.href}
                        onClick={isF ? (e) => { e.preventDefault(); setFeaturesOpen(v => !v) } : undefined}
                        className="relative z-10 flex items-center gap-1 px-3.5 py-2 text-[13px] font-medium text-white/50 hover:text-white/90 transition-colors duration-150 cursor-pointer select-none"
                        tabIndex={0}
                        onKeyDown={(e) => {
                          if (e.key === "Enter" || e.key === " ") {
                            e.preventDefault()
                            if (isF) setFeaturesOpen(v => !v)
                            else window.location.hash = item.href
                          }
                        }}
                        aria-haspopup={isF ? "true" : undefined}
                        aria-expanded={isF ? featuresOpen : undefined}
                      >
                        {item.label}
                        {isF && (
                          <motion.span
                            animate={{ rotate: featuresOpen ? 180 : 0 }}
                            transition={{ duration: 0.2, ease: "easeInOut" }}
                            className="flex items-center"
                          >
                            <ChevronDown size={12} className="text-white/30 mt-[1px]" />
                          </motion.span>
                        )}
                      </a>

                      {/* Features dropdown */}
                      {isF && (
                        <AnimatePresence>
                          {featuresOpen && (
                            <FeaturesDropdown onClose={() => setFeaturesOpen(false)} />
                          )}
                        </AnimatePresence>
                      )}
                    </div>
                  )
                })}
              </LayoutGroup>
            </nav>

            {/* ── Right CTAs ──────────────────────────────────── */}
            <div className="hidden md:flex items-center gap-2 shrink-0">
              {/* Sign In */}
              <div ref={authRef} className="relative">
                <MagButton
                  onClick={() => setAuthOpen(v => !v)}
                  className="relative h-9 px-4 rounded-xl text-[13px] font-semibold text-white/55 hover:text-white border border-white/[0.08] hover:border-white/20 hover:bg-white/[0.06] transition-all duration-200"
                  aria-expanded={authOpen}
                  aria-haspopup="true"
                >
                  Sign In
                </MagButton>

                <AnimatePresence>
                  {authOpen && <AuthForm onClose={() => setAuthOpen(false)} />}
                </AnimatePresence>
              </div>

              {/* Get Started */}
              <MagButton
                onClick={() => { setAuthOpen(true) }}
                className="relative overflow-hidden group h-9 px-4 rounded-xl text-[13px] font-semibold text-white"
                style={{
                  background:  "linear-gradient(135deg, #7c3aed, #4f46e5)",
                  boxShadow:   "0 0 20px rgba(124,58,237,0.35), 0 4px 12px rgba(0,0,0,0.3)",
                }}
              >
                <span className="relative z-10 flex items-center gap-1.5">
                  Get Started
                  <ArrowRight
                    size={13}
                    className="group-hover:translate-x-0.5 transition-transform duration-200"
                  />
                </span>
                {/* Shine sweep */}
                <motion.div
                  className="absolute inset-0 pointer-events-none -skew-x-12"
                  style={{ background: "linear-gradient(90deg, transparent, rgba(255,255,255,0.18), transparent)" }}
                  initial={{ x: "-200%" }}
                  whileHover={{ x: "200%" }}
                  transition={{ duration: 0.55, ease: "easeInOut" }}
                />
              </MagButton>
            </div>

            {/* ── Mobile Hamburger ────────────────────────────── */}
            <button
              className="lg:hidden w-10 h-10 flex flex-col items-center justify-center gap-[5px] cursor-pointer ml-2"
              onClick={() => setMobileOpen(true)}
              aria-label="Open navigation menu"
              aria-expanded={mobileOpen}
            >
              <motion.span
                className="block h-[1.5px] rounded-full bg-white/55"
                animate={{ width: mobileOpen ? 20 : 20 }}
              />
              <motion.span
                className="block h-[1.5px] rounded-full bg-white/35"
                animate={{ width: mobileOpen ? 20 : 14 }}
                style={{ alignSelf: "flex-end" }}
              />
            </button>
          </motion.nav>
        </div>
      </header>

      {/* ─────────────────────────────────────── Mobile Menu ── */}
      <MobileMenu
        open={mobileOpen}
        onClose={() => setMobileOpen(false)}
        onSignIn={() => setAuthOpen(true)}
      />

      {/* ─────────────────── Spacer so content doesn't hide ── */}
      <div style={{ height: 84 }} />
    </>
  )
}
