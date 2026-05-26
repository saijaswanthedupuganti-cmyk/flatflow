"use client"
import { useState, useMemo } from 'react'
import { useFlatStore } from '@/store/useFlatStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { BarChart3, Trophy, TrendingUp, CheckCircle2, Users, Star, ChevronLeft, ChevronRight } from 'lucide-react'

const TASK_EMOJIS: Record<string, string> = {
  garbage: '🗑️', cleaning: '🧹', kitchen: '🍳', groceries: '🛒',
  laundry: '👕', maintenance: '🔧', other: '📋',
}

export default function AnalyticsPage() {
  const { members, tasks, activityLog } = useFlatStore()

  const currentDate = new Date()
  const [selectedYear, setSelectedYear] = useState<number>(currentDate.getFullYear())
  const [selectedMonth, setSelectedMonth] = useState<number>(currentDate.getMonth())
  const [filterType, setFilterType] = useState<'monthly' | 'all_time'>('monthly')

  const months = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December',
  ]

  const navigateMonth = (dir: -1 | 1) => {
    let m = selectedMonth + dir
    let y = selectedYear
    if (m < 0)  { m = 11; y-- }
    if (m > 11) { m = 0;  y++ }
    setSelectedMonth(m)
    setSelectedYear(y)
  }

  // ── Filtered completions ────────────────────────────────────────────────
  const filteredActivities = useMemo(() =>
    activityLog.filter(a => {
      if (a.action !== 'completed_task') return false
      if (filterType === 'all_time') return true
      const d = new Date(a.timestamp)
      return d.getFullYear() === selectedYear && d.getMonth() === selectedMonth
    }),
    [activityLog, selectedYear, selectedMonth, filterType]
  )

  // ── Per-member breakdown ────────────────────────────────────────────────
  const gridData = useMemo(() =>
    members.map(member => {
      const completions = filteredActivities.filter(a => a.userId === member.uid)
      const taskCounts: Record<string, number> = {}
      let totalCount = 0
      tasks.forEach(task => {
        const n = completions.filter(c => c.details.includes(task.name)).length
        taskCounts[task.taskId] = n
        totalCount += n
      })
      return { member, taskCounts, totalCount }
    }),
    [members, tasks, filteredActivities]
  )

  // ── Summary stats ───────────────────────────────────────────────────────
  const totalCompletions = filteredActivities.length
  const topPerformer = gridData.reduce(
    (best, row) => (row.totalCount > best.totalCount ? row : best),
    gridData[0] ?? { member: null, totalCount: 0 }
  )
  const maxCount = Math.max(...gridData.map(r => r.totalCount), 1)
  const avgReliability = members.length
    ? Math.round(members.reduce((s, m) => s + (m.reliabilityScore ?? 100), 0) / members.length)
    : 100
  const activeDays = new Set(
    filteredActivities.map(a => new Date(a.timestamp).toDateString())
  ).size

  // ── Cell colour helper ──────────────────────────────────────────────────
  const cellColour = (n: number) => {
    if (n === 0) return 'bg-secondary/40 text-muted-foreground'
    if (n === 1) return 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400'
    if (n <= 3)  return 'bg-green-200 dark:bg-green-800/40 text-green-800 dark:text-green-300'
    return             'bg-green-400/60 dark:bg-green-700/50 text-green-900 dark:text-green-200 font-extrabold'
  }

  const periodLabel = filterType === 'all_time'
    ? 'All Time'
    : `${months[selectedMonth]} ${selectedYear}`

  return (
    <div className="space-y-8 max-w-5xl">

      {/* ── Header ─────────────────────────────────────────────────── */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <p className="text-xs font-bold text-muted-foreground uppercase tracking-widest mb-1">Accountability</p>
          <h1 className="text-3xl font-extrabold tracking-tight">Analytics</h1>
          <p className="text-muted-foreground mt-1 text-sm">
            Track task completions and reliability across the flat.
          </p>
        </div>

        {/* Filter controls */}
        <div className="flex items-center gap-2 shrink-0">
          <div className="flex bg-secondary/80 border border-border/60 rounded-xl p-1">
            <button
              onClick={() => setFilterType('monthly')}
              className={`px-3 py-1.5 rounded-lg text-sm font-semibold transition-all ${filterType === 'monthly' ? 'bg-primary text-white shadow-sm' : 'text-muted-foreground hover:text-foreground'}`}
            >
              Monthly
            </button>
            <button
              onClick={() => setFilterType('all_time')}
              className={`px-3 py-1.5 rounded-lg text-sm font-semibold transition-all ${filterType === 'all_time' ? 'bg-primary text-white shadow-sm' : 'text-muted-foreground hover:text-foreground'}`}
            >
              All Time
            </button>
          </div>

          {filterType === 'monthly' && (
            <div className="flex items-center gap-1 bg-card border border-border/60 rounded-xl px-2 py-1.5 shadow-sm">
              <button onClick={() => navigateMonth(-1)} className="p-1 hover:bg-secondary rounded-lg transition-colors">
                <ChevronLeft size={16} />
              </button>
              <span className="text-sm font-semibold w-36 text-center">{months[selectedMonth]} {selectedYear}</span>
              <button onClick={() => navigateMonth(1)} className="p-1 hover:bg-secondary rounded-lg transition-colors">
                <ChevronRight size={16} />
              </button>
            </div>
          )}
        </div>
      </div>

      {/* ── Summary cards ──────────────────────────────────────────── */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <Card className="shadow-sm border-border/60">
          <CardContent className="p-4">
            <div className="flex items-center gap-2 mb-2">
              <CheckCircle2 size={16} className="text-green-500" />
              <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Completions</span>
            </div>
            <div className="text-3xl font-extrabold">{totalCompletions}</div>
            <p className="text-xs text-muted-foreground mt-1">{periodLabel}</p>
          </CardContent>
        </Card>

        <Card className="shadow-sm border-border/60">
          <CardContent className="p-4">
            <div className="flex items-center gap-2 mb-2">
              <Trophy size={16} className="text-yellow-500" />
              <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Top Performer</span>
            </div>
            <div className="text-xl font-extrabold truncate">
              {topPerformer?.member?.nickname ?? '—'}
            </div>
            <p className="text-xs text-muted-foreground mt-1">{topPerformer?.totalCount ?? 0} completions</p>
          </CardContent>
        </Card>

        <Card className="shadow-sm border-border/60">
          <CardContent className="p-4">
            <div className="flex items-center gap-2 mb-2">
              <Star size={16} className="text-orange-500" />
              <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Avg Reliability</span>
            </div>
            <div className="text-3xl font-extrabold">{avgReliability}</div>
            <div className="w-full bg-secondary rounded-full h-1.5 mt-2">
              <div className="h-1.5 rounded-full bg-orange-400 transition-all" style={{ width: `${avgReliability}%` }} />
            </div>
          </CardContent>
        </Card>

        <Card className="shadow-sm border-border/60">
          <CardContent className="p-4">
            <div className="flex items-center gap-2 mb-2">
              <TrendingUp size={16} className="text-blue-500" />
              <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Active Days</span>
            </div>
            <div className="text-3xl font-extrabold">{activeDays}</div>
            <p className="text-xs text-muted-foreground mt-1">days with completions</p>
          </CardContent>
        </Card>
      </div>

      {/* ── Completion Grid ─────────────────────────────────────────── */}
      <Card className="shadow-sm border-border/60 overflow-hidden">
        <CardHeader className="border-b border-border/60 bg-secondary/20">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center">
              <BarChart3 size={16} className="text-primary" />
            </div>
            <div>
              <CardTitle className="text-base">Duty Completion Grid</CardTitle>
              <CardDescription className="text-xs mt-0.5">
                {filterType === 'all_time'
                  ? 'All recorded task completions'
                  : `${months[selectedMonth]} ${selectedYear} — cells show number of completions`}
              </CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead>
                <tr className="bg-secondary/30">
                  <th className="px-5 py-3.5 text-xs font-bold text-muted-foreground uppercase tracking-wider border-b border-r border-border/60 whitespace-nowrap">
                    <div className="flex items-center gap-2">
                      <Users size={13} /> Member
                    </div>
                  </th>
                  {tasks.map(task => (
                    <th key={task.taskId} className="px-4 py-3.5 text-xs font-bold text-muted-foreground text-center border-b border-border/60 whitespace-nowrap">
                      <span className="mr-1">{TASK_EMOJIS[task.type] ?? '📋'}</span>
                      {task.name}
                    </th>
                  ))}
                  <th className="px-5 py-3.5 text-xs font-bold text-primary text-center border-b border-border/60 bg-primary/5 whitespace-nowrap">
                    Total
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/40">
                {gridData.map((row, ri) => (
                  <tr key={row.member.uid} className="hover:bg-secondary/20 transition-colors">
                    <td className="px-5 py-4 border-r border-border/60">
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-full bg-primary/15 text-primary flex items-center justify-center font-extrabold text-sm shrink-0">
                          {row.member.nickname.charAt(0)}
                        </div>
                        <div className="min-w-0">
                          <div className="font-semibold text-sm leading-tight truncate">{row.member.nickname}</div>
                          <div className="text-[11px] text-muted-foreground capitalize">{row.member.role}</div>
                        </div>
                      </div>
                    </td>
                    {tasks.map(task => {
                      const n = row.taskCounts[task.taskId] ?? 0
                      return (
                        <td key={task.taskId} className="px-4 py-4 text-center">
                          {n > 0 ? (
                            <span className={`inline-flex items-center justify-center w-8 h-8 rounded-lg text-sm font-bold ${cellColour(n)}`}>
                              {n}
                            </span>
                          ) : (
                            <span className="inline-flex items-center justify-center w-8 h-8 rounded-lg text-muted-foreground/30 text-lg">—</span>
                          )}
                        </td>
                      )
                    })}
                    <td className="px-5 py-4 text-center bg-primary/5">
                      <span className={`inline-flex items-center justify-center w-10 h-10 rounded-xl font-extrabold text-base ${
                        row.totalCount > 0 ? 'bg-primary/10 text-primary' : 'text-muted-foreground/40'
                      }`}>
                        {row.totalCount || '—'}
                      </span>
                    </td>
                  </tr>
                ))}
                {gridData.length === 0 && (
                  <tr>
                    <td colSpan={tasks.length + 2} className="px-6 py-12 text-center text-muted-foreground">
                      No completions recorded for this period.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>

      {/* ── Per-member bars ─────────────────────────────────────────── */}
      <div>
        <div className="flex items-center gap-2 mb-4">
          <TrendingUp size={18} className="text-primary" />
          <h2 className="text-lg font-bold">Completion Breakdown</h2>
          <span className="text-xs text-muted-foreground font-medium ml-1">— {periodLabel}</span>
        </div>
        <div className="space-y-3">
          {[...gridData]
            .sort((a, b) => b.totalCount - a.totalCount)
            .map((row, i) => {
              const pct = maxCount > 0 ? (row.totalCount / maxCount) * 100 : 0
              const barColors = ['bg-primary', 'bg-violet-500', 'bg-cyan-500', 'bg-orange-500']
              return (
                <div key={row.member.uid} className="flex items-center gap-4">
                  <div className="w-8 h-8 rounded-full bg-secondary flex items-center justify-center font-bold text-sm shrink-0 text-muted-foreground">
                    {i + 1}
                  </div>
                  <div className="w-28 shrink-0">
                    <div className="font-semibold text-sm truncate">{row.member.nickname}</div>
                    <div className="text-[11px] text-muted-foreground capitalize">{row.member.role}</div>
                  </div>
                  <div className="flex-1 bg-secondary/60 rounded-full h-3 overflow-hidden">
                    <div
                      className={`h-3 rounded-full transition-all duration-500 ${barColors[i % barColors.length]}`}
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                  <div className="w-12 text-right font-extrabold text-sm">{row.totalCount}</div>
                </div>
              )
            })}
        </div>
      </div>

      {/* ── Reliability scores ──────────────────────────────────────── */}
      <Card className="shadow-sm border-border/60">
        <CardHeader className="border-b border-border/60 pb-4">
          <div className="flex items-center gap-2">
            <Star size={18} className="text-orange-500" />
            <CardTitle className="text-base">Reliability Scores</CardTitle>
          </div>
          <CardDescription className="text-xs mt-1">
            Based on on-time task completions. Drops when tasks go overdue.
          </CardDescription>
        </CardHeader>
        <CardContent className="p-5 space-y-4">
          {[...members]
            .sort((a, b) => (b.reliabilityScore ?? 100) - (a.reliabilityScore ?? 100))
            .map((m, i) => {
              const score = m.reliabilityScore ?? 100
              const colour = score >= 80 ? 'bg-green-500' : score >= 50 ? 'bg-yellow-500' : 'bg-red-500'
              const textColour = score >= 80 ? 'text-green-600 dark:text-green-400' : score >= 50 ? 'text-yellow-600 dark:text-yellow-400' : 'text-red-600 dark:text-red-400'
              return (
                <div key={m.uid} className="flex items-center gap-4">
                  <div className="w-9 h-9 rounded-full bg-primary/15 text-primary flex items-center justify-center font-bold text-sm shrink-0">
                    {m.nickname.charAt(0)}
                  </div>
                  <div className="w-28 shrink-0">
                    <div className="font-semibold text-sm">{m.nickname}</div>
                    <div className="text-[11px] text-muted-foreground capitalize">{m.role}</div>
                  </div>
                  <div className="flex-1 bg-secondary/60 rounded-full h-2.5 overflow-hidden">
                    <div className={`h-2.5 rounded-full transition-all duration-500 ${colour}`} style={{ width: `${score}%` }} />
                  </div>
                  <div className={`w-12 text-right font-extrabold text-sm ${textColour}`}>{score}</div>
                </div>
              )
            })}
        </CardContent>
      </Card>
    </div>
  )
}
