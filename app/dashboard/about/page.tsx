"use client"
import { ExternalLink, Link2, Zap, Shield, RefreshCw, Bell, BarChart3, CalendarDays, Layers } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'

const TEAM = [
  {
    name: 'Venkata Sai Jaswanth E',
    nickname: 'Sai Jaswanth',
    role: 'UI/UX Designer & Co-founder',
    bio: 'Passionate about crafting intuitive digital experiences. Leads the design vision for Habitiq — from wireframes to pixel-perfect interfaces that make shared living effortless.',
    avatar: 'SJ',
    color: 'from-blue-500 to-indigo-600',
    linkedin: 'https://www.linkedin.com/in/venkata-sai-jaswanth-e/',
    skills: ['UI/UX Design', 'Product Design', 'Figma', 'User Research'],
  },
  {
    name: 'Upputuri Bhanu Kalyan',
    nickname: 'Bhanu Kalyan',
    role: 'Full-Stack Developer & Co-founder',
    bio: 'Brings ideas to life through clean, scalable code. Architected Habitiq\'s real-time backend, rotation engine, and the seamless Firebase integration that keeps every flatmate in sync.',
    avatar: 'BK',
    color: 'from-violet-500 to-purple-700',
    linkedin: 'https://www.linkedin.com/in/upputuri-bhanu-kalyan/',
    skills: ['Next.js', 'Firebase', 'TypeScript', 'System Design'],
  },
]

const FEATURES = [
  { icon: RefreshCw,    color: 'text-blue-500',   bg: 'bg-blue-500/10',   title: 'Smart Rotation',      desc: 'Auto-assigns duties fairly. Skips absent flatmates, resumes seamlessly when they return.' },
  { icon: Bell,         color: 'text-green-500',  bg: 'bg-green-500/10',  title: 'Live Notifications',  desc: 'Real-time toasts when any flatmate completes a task — everyone stays accountable.' },
  { icon: CalendarDays, color: 'text-orange-500', bg: 'bg-orange-500/10', title: 'Calendar Tracking',   desc: 'Visual history of every task completion, filterable by member and month.' },
  { icon: Shield,       color: 'text-red-500',    bg: 'bg-red-500/10',    title: 'Overdue Accountability', desc: 'Overdue tasks stay with the responsible person until completed. No shortcuts.' },
  { icon: RefreshCw,    color: 'text-purple-500', bg: 'bg-purple-500/10', title: 'Swap Requests',       desc: 'Request a teammate to cover your duty. Accept or decline — all within the app.' },
  { icon: BarChart3,    color: 'text-indigo-500', bg: 'bg-indigo-500/10', title: 'Analytics',           desc: 'Monthly duty completion grids, reliability scores, and per-member breakdowns.' },
]


export default function AboutPage() {
  return (
    <div className="space-y-12 max-w-4xl">

      {/* ── Hero ─────────────────────────────────────── */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-primary via-blue-600 to-indigo-700 p-8 md:p-12 text-white shadow-xl">
        <div className="absolute inset-0 opacity-10" style={{ backgroundImage: 'radial-gradient(circle at 20% 50%, white 1px, transparent 1px), radial-gradient(circle at 80% 20%, white 1px, transparent 1px)', backgroundSize: '40px 40px' }} />
        <div className="relative z-10">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-12 h-12 bg-white/20 rounded-2xl flex items-center justify-center backdrop-blur-sm">
              <span className="text-2xl font-extrabold">H</span>
            </div>
            <div>
              <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">Habitiq</h1>
              <p className="text-blue-100 text-sm font-medium">v0.1.0 · Open Beta</p>
            </div>
          </div>
          <p className="text-lg sm:text-xl font-semibold text-white/95 leading-relaxed max-w-2xl">
            Shared living, perfectly balanced.
          </p>
          <p className="text-blue-100 mt-3 max-w-2xl leading-relaxed text-sm sm:text-base">
            Habitiq is a smart household duty management app built for bachelors and flatmates who want a fair, automated, and transparent chore rotation system — no arguments, no forgotten tasks, no excuses.
          </p>
          <div className="flex flex-wrap gap-3 mt-6">
            <div className="flex items-center gap-1.5 bg-white/15 backdrop-blur-sm border border-white/20 rounded-full px-3 py-1.5 text-xs font-semibold">
              <Zap size={12} /> Real-time sync
            </div>
            <div className="flex items-center gap-1.5 bg-white/15 backdrop-blur-sm border border-white/20 rounded-full px-3 py-1.5 text-xs font-semibold">
              <Shield size={12} /> Firebase-powered
            </div>
            <div className="flex items-center gap-1.5 bg-white/15 backdrop-blur-sm border border-white/20 rounded-full px-3 py-1.5 text-xs font-semibold">
              <Layers size={12} /> Mobile-first
            </div>
          </div>
        </div>
      </div>

      {/* ── Team ─────────────────────────────────────── */}
      <div>
        <div className="mb-6">
          <h2 className="text-2xl font-bold tracking-tight">Meet the Team</h2>
          <p className="text-muted-foreground mt-1">Two builders who got tired of arguing about who cleaned the kitchen last.</p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {TEAM.map(person => (
            <Card key={person.name} className="shadow-sm border-border/60 overflow-hidden hover:shadow-md transition-shadow">
              <CardContent className="p-0">
                {/* Gradient header */}
                <div className={`bg-gradient-to-r ${person.color} p-5 sm:p-6`}>
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="w-12 h-12 sm:w-14 sm:h-14 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center text-white text-lg sm:text-xl font-extrabold shadow-sm shrink-0">
                        {person.avatar}
                      </div>
                      <div className="text-white min-w-0">
                        <h3 className="font-bold text-base sm:text-lg leading-snug break-words">{person.name}</h3>
                        <p className="text-white/80 text-xs sm:text-sm font-medium mt-0.5">{person.role}</p>
                      </div>
                    </div>
                    <a
                      href={person.linkedin}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="w-9 h-9 bg-white/20 hover:bg-white/30 rounded-xl flex items-center justify-center text-white transition-colors shrink-0"
                      title="LinkedIn Profile"
                    >
                      <Link2 size={16} />
                    </a>
                  </div>
                </div>
                {/* Body */}
                <div className="p-5">
                  <p className="text-sm text-muted-foreground leading-relaxed">{person.bio}</p>
                  <div className="flex flex-wrap gap-2 mt-4">
                    {person.skills.map(skill => (
                      <span key={skill} className="text-xs bg-secondary border border-border/60 rounded-full px-2.5 py-1 font-medium text-foreground">
                        {skill}
                      </span>
                    ))}
                  </div>
                  <a
                    href={person.linkedin}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center gap-1.5 mt-4 text-xs font-semibold text-primary hover:underline"
                  >
                    <Link2 size={13} /> View on LinkedIn <ExternalLink size={11} />
                  </a>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>

      {/* ── Features ──────────────────────────────────── */}
      <div>
        <div className="mb-6">
          <h2 className="text-2xl font-bold tracking-tight">What Habitiq Does</h2>
          <p className="text-muted-foreground mt-1">Every feature built from a real flatmate frustration.</p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {FEATURES.map((f, i) => (
            <div key={i} className="p-4 rounded-xl border border-border/60 bg-card hover:bg-secondary/20 transition-colors">
              <div className={`w-9 h-9 rounded-xl ${f.bg} flex items-center justify-center mb-3`}>
                <f.icon size={17} className={f.color} />
              </div>
              <h3 className="font-semibold text-sm mb-1">{f.title}</h3>
              <p className="text-xs text-muted-foreground leading-relaxed">{f.desc}</p>
            </div>
          ))}
        </div>
      </div>

      {/* ── Footer ────────────────────────────────────── */}
      <div className="border-t border-border/60 pt-6 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <p className="text-sm font-semibold">Habitiq v0.1.0</p>
          <p className="text-xs text-muted-foreground mt-0.5">
            Built with ☕, a very messy kitchen, and{' '}
            <a href="https://claude.ai/code" target="_blank" rel="noopener noreferrer" className="text-primary font-semibold hover:underline">
              Claude Code
            </a>.
          </p>
        </div>
        <div className="flex items-center gap-4">
          {TEAM.map(p => (
            <a key={p.name} href={p.linkedin} target="_blank" rel="noopener noreferrer"
              className="flex items-center gap-1.5 text-xs text-muted-foreground hover:text-primary transition-colors font-medium"
            >
              <Link2 size={13} /> {p.nickname}
            </a>
          ))}
        </div>
      </div>
    </div>
  )
}
