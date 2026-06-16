"use client"
import { ExternalLink, Link2, RefreshCw, Bell, BarChart3, CalendarDays, Shield, ArrowUpRight } from 'lucide-react'

const TEAM = [
  {
    name: 'Venkata Sai Jaswanth E',
    role: 'Founder',
    bio: 'Leads the design vision for Habitiq — from concept to interface. Focused on making shared living feel effortless through thoughtful product decisions.',
    avatar: 'SJ',
    linkedin: 'https://www.linkedin.com/in/venkata-sai-jaswanth-e/',
  },
  {
    name: 'Upputuri Bhanu Kalyan',
    role: 'Co-founder & Engineer',
    bio: "Built the real-time backend, rotation engine, and Firebase integration that powers every flatmate's experience inside Habitiq.",
    avatar: 'BK',
    linkedin: 'https://www.linkedin.com/in/upputuri-bhanu-kalyan/',
  },
]

const FEATURES = [
  { icon: RefreshCw,    title: 'Smart Rotation',         desc: 'Duties auto-assign in order. Out-of-station members are skipped; they rejoin the queue when they return.' },
  { icon: Bell,         title: 'Live Notifications',     desc: 'Real-time updates when any flatmate completes a task. Everyone stays in the loop automatically.' },
  { icon: Shield,       title: 'Overdue Accountability', desc: 'Overdue tasks stay with the responsible person until done. No task silently disappears.' },
  { icon: RefreshCw,    title: 'Swap Requests',          desc: 'Need someone to cover? Send a request. They accept or decline — tracked and transparent.' },
  { icon: CalendarDays, title: 'Calendar View',          desc: 'See every task completion laid out by date. Filter by member or month.' },
  { icon: BarChart3,    title: 'Analytics',              desc: 'Reliability scores and duty history per member, so contribution is visible to everyone.' },
]

export default function AboutPage() {
  return (
    <div className="max-w-2xl space-y-14">

      {/* ── Product intro ────────────────────────────────── */}
      <div className="space-y-4">
        <div>
          <p className="text-xs font-semibold text-muted-foreground uppercase tracking-widest mb-2">About</p>
          <h1 className="text-3xl font-extrabold tracking-tight">Habitiq</h1>
          <p className="text-muted-foreground text-sm mt-1">v0.1.0 · Open Beta</p>
        </div>
        <p className="text-base text-foreground leading-relaxed">
          Habitiq is a household duty management app for flatmates who want a fair, automated rotation — no arguments, no forgotten tasks, no manual tracking.
        </p>
        <p className="text-sm text-muted-foreground leading-relaxed">
          Built for people who live together and want a simple system that just works. Assign duties once, let the rotation handle the rest.
        </p>
      </div>

      {/* ── Divider ──────────────────────────────────────── */}
      <hr className="border-border/60" />

      {/* ── Founders ─────────────────────────────────────── */}
      <div className="space-y-6">
        <div>
          <h2 className="text-lg font-bold tracking-tight">Founders</h2>
          <p className="text-sm text-muted-foreground mt-0.5">The people behind Habitiq.</p>
        </div>

        <div className="space-y-6">
          {TEAM.map(person => (
            <div key={person.name} className="flex items-start gap-4">
              {/* Avatar */}
              <div className="w-11 h-11 rounded-full bg-secondary border border-border flex items-center justify-center text-sm font-bold text-foreground shrink-0">
                {person.avatar}
              </div>
              {/* Info */}
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between gap-2">
                  <div>
                    <p className="font-semibold text-sm leading-tight">{person.name}</p>
                    <p className="text-xs text-muted-foreground mt-0.5">{person.role}</p>
                  </div>
                  <a
                    href={person.linkedin}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground transition-colors shrink-0"
                  >
                    LinkedIn <ArrowUpRight size={11} />
                  </a>
                </div>
                <p className="text-sm text-muted-foreground mt-2 leading-relaxed">{person.bio}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* ── Divider ──────────────────────────────────────── */}
      <hr className="border-border/60" />

      {/* ── Features ─────────────────────────────────────── */}
      <div className="space-y-6">
        <div>
          <h2 className="text-lg font-bold tracking-tight">What it does</h2>
          <p className="text-sm text-muted-foreground mt-0.5">Core features, and why they exist.</p>
        </div>

        <div className="space-y-5">
          {FEATURES.map((f, i) => (
            <div key={i} className="flex items-start gap-3">
              <f.icon size={15} className="text-muted-foreground mt-0.5 shrink-0" />
              <div>
                <p className="text-sm font-semibold">{f.title}</p>
                <p className="text-sm text-muted-foreground mt-0.5 leading-relaxed">{f.desc}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* ── Contact ──────────────────────────────────────── */}
      <div className="space-y-3">
        <div>
          <h2 className="text-lg font-bold tracking-tight">Contact & Support</h2>
          <p className="text-sm text-muted-foreground mt-0.5">Got feedback, a bug, or a question?</p>
        </div>
        <a
          href="mailto:hello@habitiq.app"
          className="flex items-center gap-3 px-4 py-3 rounded-xl border border-border bg-secondary/40 hover:bg-secondary/80 transition-colors group"
        >
          <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center shrink-0">
            <ArrowUpRight size={15} className="text-primary" />
          </div>
          <div>
            <p className="text-sm font-semibold">hello@habitiq.app</p>
            <p className="text-xs text-muted-foreground">We read every message. Usually respond within 24 hours.</p>
          </div>
        </a>
      </div>

      {/* ── Footer ───────────────────────────────────────── */}
      <div className="border-t border-border/60 pt-6 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <p className="text-xs text-muted-foreground">
          Habitiq v0.1.0 · Built with{' '}
          <a href="https://claude.ai/code" target="_blank" rel="noopener noreferrer" className="underline underline-offset-2 hover:text-foreground transition-colors">
            Claude Code
          </a>
        </p>
        <div className="flex items-center gap-4">
          {TEAM.map(p => (
            <a
              key={p.name}
              href={p.linkedin}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground transition-colors"
            >
              <Link2 size={11} /> {p.name.split(' ').slice(0, 2).join(' ')}
              <ExternalLink size={10} className="ml-0.5" />
            </a>
          ))}
        </div>
      </div>
    </div>
  )
}
