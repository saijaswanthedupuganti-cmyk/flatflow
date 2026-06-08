"use client"
import { useState, useMemo } from 'react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  BarChart3, Trophy, TrendingUp, CheckCircle2, Users, Star,
  ChevronLeft, ChevronRight, CalendarDays, Clock,
} from 'lucide-react'

const TASK_EMOJIS: Record<string, string> = {
  garbage: '🗑️', cleaning: '🧹', kitchen: '🍳',
  groceries: '🛒', laundry: '👕', maintenance: '🔧', other: '📋',
}

const WEEKDAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
const MONTHS = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
]

function toDateKey(d: Date) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

export default function InsightsPage() {
  const [activeTab, setActiveTab] = useState<'calendar' | 'stats'>('calendar')

  const { members, tasks, activityLog } = useFlatStore()
  const { user } = useAuthStore()

  const today = new Date()
  const todayKey = toDateKey(today)

  // ── Shared month navigation (both tabs stay in sync) ──────────────────────
  const [viewYear, setViewYear] = useState(today.getFullYear())
  const [viewMonth, setViewMonth] = useState(today.getMonth())

  const prevMonth = () => {
    if (viewMonth === 0) { setViewYear(y => y - 1); setViewMonth(11) }
    else setViewMonth(m => m - 1)
  }
  const nextMonth = () => {
    if (viewMonth === 11) { setViewYear(y => y + 1); setViewMonth(0) }
    else setViewMonth(m => m + 1)
  }

  // ── Stats tab filter type ─────────────────────────────────────────────────
  const [filterType, setFilterType] = useState<'monthly' | 'all_time'>('monthly')

  // ── Calendar state ────────────────────────────────────────────────────────
  const [selectedDay, setSelectedDay] = useState<string | null>(todayKey)
  const [memberFilter, setMemberFilter] = useState<string>('all')

  // ── Completion map ────────────────────────────────────────────────────────
  const completionMap = useMemo(() => {
    const map: Record<string, typeof activityLog> = {}
    activityLog.forEach(a => {
      if (a.action !== 'completed_task') return
      const key = toDateKey(new Date(a.timestamp))
      if (!map[key]) map[key] = []
      map[key].push(a)
    })
    return map
  }, [activityLog])

  // ── Due-date map ──────────────────────────────────────────────────────────
  const dueDateMap = useMemo(() => {
    const map: Record<string, typeof tasks> = {}
    tasks.forEach(task => {
      if (!task.dueDate) return
      const key = toDateKey(new Date(task.dueDate))
      if (!map[key]) map[key] = []
      map[key].push(task)
    })
    return map
  }, [tasks])

  // ── Calendar grid ─────────────────────────────────────────────────────────
  const calendarDays = useMemo(() => {
    const firstDay    = new Date(viewYear, viewMonth, 1)
    const startOffset = (firstDay.getDay() + 6) % 7
    const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate()
    const daysInPrev  = new Date(viewYear, viewMonth, 0).getDate()
    const days: { date: Date; isCurrentMonth: boolean }[] = []
    for (let i = startOffset - 1; i >= 0; i--)
      days.push({ date: new Date(viewYear, viewMonth - 1, daysInPrev - i), isCurrentMonth: false })
    for (let d = 1; d <= daysInMonth; d++)
      days.push({ date: new Date(viewYear, viewMonth, d), isCurrentMonth: true })
    const remaining = (7 - (days.length % 7)) % 7
    for (let d = 1; d <= remaining; d++)
      days.push({ date: new Date(viewYear, viewMonth + 1, d), isCurrentMonth: false })
    return days
  }, [viewYear, viewMonth])

  // ── Selected day detail ───────────────────────────────────────────────────
  const selectedActivities = useMemo(() => {
    if (!selectedDay) return []
    const all = completionMap[selectedDay] || []
    return memberFilter === 'all' ? all : all.filter(a => a.userId === memberFilter)
  }, [selectedDay, completionMap, memberFilter])

  const selectedDueTasks = useMemo(() => {
    if (!selectedDay) return []
    const all = dueDateMap[selectedDay] || []
    return memberFilter === 'all'
      ? all
      : all.filter(t => t.currentAssignedUserId === memberFilter)
  }, [selectedDay, dueDateMap, memberFilter])

  // ── Monthly stats (calendar summary row) ─────────────────────────────────
  const monthStats = useMemo(() => {
    const prefix = `${viewYear}-${String(viewMonth + 1).padStart(2, '0')}`
    const monthActivities = activityLog.filter(a =>
      a.action === 'completed_task' && a.timestamp.startsWith(prefix)
    )
    const byMember: Record<string, number> = {}
    members.forEach(m => { byMember[m.uid] = 0 })
    monthActivities.forEach(a => { byMember[a.userId] = (byMember[a.userId] || 0) + 1 })
    const top = Object.entries(byMember).sort((a, b) => b[1] - a[1])[0]
    return {
      total:      monthActivities.length,
      activeDays: new Set(monthActivities.map(a => toDateKey(new Date(a.timestamp)))).size,
      byMember,
      topMember:  top ? members.find(m => m.uid === top[0]) : null,
      topCount:   top?.[1] || 0,
    }
  }, [activityLog, viewYear, viewMonth, members])

  // ── Stats tab: filtered completions ──────────────────────────────────────
  const filteredActivities = useMemo(() =>
    activityLog.filter(a => {
      if (a.action !== 'completed_task') return false
      if (filterType === 'all_time') return true
      const d = new Date(a.timestamp)
      return d.getFullYear() === viewYear && d.getMonth() === viewMonth
    }),
    [activityLog, viewYear, viewMonth, filterType]
  )

  // ── Per-member grid data (stats tab) ──────────────────────────────────────
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

  const totalCompletions = filteredActivities.length
  const topPerformer = gridData.reduce(
    (best, row) => (row.totalCount > best.totalCount ? row : best),
    gridData[0] ?? { member: null, totalCount: 0 }
  )
  const maxCount = Math.max(...gridData.map(r => r.totalCount), 1)
  const avgReliability = members.length
    ? Math.round(members.reduce((s, m) => s + (m.reliabilityScore ?? 100), 0) / members.length)
    : 100
  const statsActiveDays = new Set(
    filteredActivities.map(a => new Date(a.timestamp).toDateString())
  ).size

  const cellColour = (n: number) => {
    if (n === 0) return 'bg-secondary/40 text-muted-foreground'
    if (n === 1) return 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400'
    if (n <= 3)  return 'bg-green-200 dark:bg-green-800/40 text-green-800 dark:text-green-300'
    return             'bg-green-400/60 dark:bg-green-700/50 text-green-900 dark:text-green-200 font-extrabold'
  }

  const periodLabel = filterType === 'all_time' ? 'All Time' : `${MONTHS[viewMonth]} ${viewYear}`

  return (
    <div className="space-y-6 max-w-5xl">

      {/* ── Header ───────────────────────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
        <div>
          <p className="text-xs font-bold text-muted-foreground uppercase tracking-widest mb-1">Flat</p>
          <h1 className="text-3xl font-extrabold tracking-tight">Insights</h1>
          <p className="text-muted-foreground mt-1 text-sm">
            Track completions, due dates, and flat performance.
          </p>
        </div>
        <div className="flex bg-secondary/80 border border-border/60 rounded-xl p-1 shrink-0 w-fit">
          <button
            onClick={() => setActiveTab('calendar')}
            className={`px-4 py-1.5 rounded-lg text-sm font-semibold transition-all ${
              activeTab === 'calendar'
                ? 'bg-primary text-white shadow-sm'
                : 'text-muted-foreground hover:text-foreground'
            }`}
          >
            Calendar
          </button>
          <button
            onClick={() => setActiveTab('stats')}
            className={`px-4 py-1.5 rounded-lg text-sm font-semibold transition-all ${
              activeTab === 'stats'
                ? 'bg-primary text-white shadow-sm'
                : 'text-muted-foreground hover:text-foreground'
            }`}
          >
            Stats
          </button>
        </div>
      </div>

      {/* ═══════════════════ CALENDAR TAB ════════════════════════════ */}
      {activeTab === 'calendar' && (
        <div className="space-y-5">
          {/* Monthly summary row */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            <Card className="shadow-sm border-border/60">
              <CardContent className="p-3 sm:p-4 flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-green-500/10 flex items-center justify-center shrink-0">
                  <CheckCircle2 size={18} className="text-green-500" />
                </div>
                <div className="min-w-0">
                  <p className="text-xl font-bold">{monthStats.total}</p>
                  <p className="text-[11px] text-muted-foreground">Completions</p>
                </div>
              </CardContent>
            </Card>
            <Card className="shadow-sm border-border/60">
              <CardContent className="p-3 sm:p-4 flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-blue-500/10 flex items-center justify-center shrink-0">
                  <CalendarDays size={18} className="text-blue-500" />
                </div>
                <div className="min-w-0">
                  <p className="text-xl font-bold">{monthStats.activeDays}</p>
                  <p className="text-[11px] text-muted-foreground">Active Days</p>
                </div>
              </CardContent>
            </Card>
            <Card className="shadow-sm border-border/60">
              <CardContent className="p-3 sm:p-4 flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-primary/10 flex items-center justify-center shrink-0">
                  <TrendingUp size={18} className="text-primary" />
                </div>
                <div className="min-w-0">
                  <p className="text-xl font-bold">{memberFilter === 'all' ? members.length : 1}</p>
                  <p className="text-[11px] text-muted-foreground">Contributors</p>
                </div>
              </CardContent>
            </Card>
            <Card className="shadow-sm border-border/60">
              <CardContent className="p-3 sm:p-4 flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-orange-500/10 flex items-center justify-center shrink-0">
                  <Users size={18} className="text-orange-500" />
                </div>
                <div className="min-w-0">
                  <p className="text-base font-bold truncate">{monthStats.topMember?.nickname || '—'}</p>
                  <p className="text-[11px] text-muted-foreground truncate">
                    {monthStats.topCount > 0 ? `${monthStats.topCount} tasks · Top` : 'No data yet'}
                  </p>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Calendar grid + day detail */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
            <div className="lg:col-span-2">
              <Card className="shadow-sm border-border/60">
                <CardHeader className="pb-3 px-4">
                  <div className="flex items-center justify-between gap-2">
                    <CardTitle className="text-lg font-bold">{MONTHS[viewMonth]} {viewYear}</CardTitle>
                    <div className="flex items-center gap-1.5">
                      <select
                        value={memberFilter}
                        onChange={e => setMemberFilter(e.target.value)}
                        className="text-xs bg-secondary border border-border rounded-lg px-2 py-1.5 font-medium hidden sm:block"
                      >
                        <option value="all">All members</option>
                        {members.map(m => <option key={m.uid} value={m.uid}>{m.nickname}</option>)}
                      </select>
                      <Button variant="outline" size="icon" className="h-8 w-8" onClick={prevMonth}>
                        <ChevronLeft size={15} />
                      </Button>
                      <Button
                        variant="outline" size="sm" className="h-8 px-2.5 text-xs font-semibold"
                        onClick={() => { setViewYear(today.getFullYear()); setViewMonth(today.getMonth()); setSelectedDay(todayKey) }}
                      >
                        Today
                      </Button>
                      <Button variant="outline" size="icon" className="h-8 w-8" onClick={nextMonth}>
                        <ChevronRight size={15} />
                      </Button>
                    </div>
                  </div>
                  <select
                    value={memberFilter}
                    onChange={e => setMemberFilter(e.target.value)}
                    className="sm:hidden mt-2 w-full text-xs bg-secondary border border-border rounded-lg px-2 py-2 font-medium"
                  >
                    <option value="all">All members</option>
                    {members.map(m => <option key={m.uid} value={m.uid}>{m.nickname}</option>)}
                  </select>
                </CardHeader>

                <CardContent className="p-0 pb-4">
                  <div className="grid grid-cols-7 px-3 mb-1">
                    {WEEKDAYS.map(d => (
                      <div key={d} className="text-center text-[10px] font-bold text-muted-foreground py-1.5 uppercase tracking-wider">
                        {d}
                      </div>
                    ))}
                  </div>

                  <div className="grid grid-cols-7 gap-1 px-3">
                    {calendarDays.map(({ date, isCurrentMonth }, i) => {
                      const key = toDateKey(date)
                      const completions = (completionMap[key] || []).filter(
                        a => memberFilter === 'all' || a.userId === memberFilter
                      )
                      const dueTasks = (dueDateMap[key] || []).filter(
                        t => memberFilter === 'all' || t.currentAssignedUserId === memberFilter
                      )
                      const isToday    = key === todayKey
                      const isSelected = key === selectedDay
                      const isFuture   = date > today

                      return (
                        <button
                          key={i}
                          onClick={() => isCurrentMonth && setSelectedDay(key)}
                          disabled={!isCurrentMonth}
                          className={[
                            'relative flex flex-col items-center justify-start p-1 rounded-xl min-h-[48px] sm:min-h-[56px] text-xs transition-all',
                            !isCurrentMonth ? 'opacity-20 cursor-default' : 'cursor-pointer hover:bg-secondary/70',
                            isSelected && isCurrentMonth ? 'ring-2 ring-primary bg-primary/5' : '',
                            isToday && !isSelected ? 'ring-2 ring-primary/40 bg-primary/5' : '',
                          ].join(' ')}
                        >
                          <span className={[
                            'w-6 h-6 flex items-center justify-center rounded-full text-xs font-bold mb-0.5',
                            isToday        ? 'bg-primary text-white' :
                            isCurrentMonth ? 'text-foreground'       : 'text-muted-foreground',
                          ].join(' ')}>
                            {date.getDate()}
                          </span>
                          <div className="flex items-center gap-0.5 justify-center flex-wrap">
                            {completions.length > 0 && completions.length <= 2
                              ? Array.from({ length: completions.length }).map((_, di) => (
                                  <span key={`c${di}`} className="w-1.5 h-1.5 rounded-full bg-green-500" />
                                ))
                              : completions.length > 2
                              ? <span className="text-[9px] font-bold text-green-600 bg-green-100 dark:bg-green-900/40 dark:text-green-400 px-1 rounded leading-tight">
                                  {completions.length}
                                </span>
                              : null}
                            {isFuture && dueTasks.length > 0 && dueTasks.length <= 2
                              ? Array.from({ length: dueTasks.length }).map((_, di) => (
                                  <span key={`d${di}`} className="w-1.5 h-1.5 rounded-full bg-blue-400" />
                                ))
                              : isFuture && dueTasks.length > 2
                              ? <span className="text-[9px] font-bold text-blue-600 bg-blue-100 dark:bg-blue-900/40 dark:text-blue-400 px-1 rounded leading-tight">
                                  {dueTasks.length}
                                </span>
                              : null}
                            {!isFuture && dueTasks.length > 0 && isToday && (
                              <span className="w-1.5 h-1.5 rounded-full bg-orange-400" />
                            )}
                          </div>
                        </button>
                      )
                    })}
                  </div>

                  <div className="flex items-center gap-3 sm:gap-5 px-4 mt-3 pt-3 border-t border-border/60 flex-wrap">
                    <div className="flex items-center gap-1.5 text-[11px] text-muted-foreground">
                      <span className="w-2 h-2 rounded-full bg-green-500 shrink-0" /> Completed
                    </div>
                    <div className="flex items-center gap-1.5 text-[11px] text-muted-foreground">
                      <span className="w-2 h-2 rounded-full bg-blue-400 shrink-0" /> Due
                    </div>
                    <div className="flex items-center gap-1.5 text-[11px] text-muted-foreground">
                      <span className="w-2 h-2 rounded-full bg-orange-400 shrink-0" /> Due today
                    </div>
                    <div className="flex items-center gap-1.5 text-[11px] text-muted-foreground">
                      <span className="w-2 h-2 rounded-full bg-primary shrink-0" /> Today
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Right panel: day detail + per-member mini bars */}
            <div className="space-y-4">
              <Card className="shadow-sm border-border/60">
                <CardHeader className="pb-3 px-4">
                  <CardTitle className="text-sm font-bold">
                    {selectedDay
                      ? new Date(selectedDay + 'T12:00:00').toLocaleDateString('en-GB', { weekday: 'long', day: 'numeric', month: 'long' })
                      : 'Select a day'}
                  </CardTitle>
                  <CardDescription className="text-xs">
                    {selectedActivities.length > 0 || selectedDueTasks.length > 0
                      ? [
                          selectedActivities.length > 0 && `${selectedActivities.length} completed`,
                          selectedDueTasks.length > 0 && `${selectedDueTasks.length} due`,
                        ].filter(Boolean).join(' · ')
                      : 'Nothing recorded or due'}
                  </CardDescription>
                </CardHeader>
                <CardContent className="px-4 pb-4 space-y-4">
                  {selectedActivities.length > 0 && (
                    <div className="space-y-2">
                      <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">Completed</p>
                      <div className="space-y-2">
                        {selectedActivities.map(activity => {
                          const member   = members.find(m => m.uid === activity.userId)
                          const taskName = activity.details.replace(/^completed /, '').replace(/ on .+$/, '')
                          const task     = tasks.find(t => t.name === taskName)
                          const emoji    = task ? (TASK_EMOJIS[task.type] ?? '✅') : '✅'
                          const time     = new Date(activity.timestamp).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })
                          return (
                            <div key={activity.id} className="flex items-start gap-2.5 p-2.5 rounded-xl bg-green-50 dark:bg-green-900/10 border border-green-200/60 dark:border-green-800/40">
                              <span className="text-base mt-0.5 shrink-0">{emoji}</span>
                              <div className="flex-1 min-w-0">
                                <p className="text-sm font-semibold leading-tight">{taskName}</p>
                                <p className="text-[11px] text-muted-foreground mt-0.5">
                                  by <span className="font-medium text-foreground">{member?.nickname || 'Unknown'}</span>
                                </p>
                              </div>
                              <span className="text-[11px] text-muted-foreground font-mono shrink-0">{time}</span>
                            </div>
                          )
                        })}
                      </div>
                    </div>
                  )}

                  {selectedDueTasks.length > 0 && (
                    <div className="space-y-2">
                      <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">Due</p>
                      <div className="space-y-2">
                        {selectedDueTasks.map(task => {
                          const assignee = members.find(m => m.uid === task.currentAssignedUserId)
                          const emoji    = TASK_EMOJIS[task.type] ?? '📋'
                          const isPast   = new Date(task.dueDate) < today
                          return (
                            <div key={task.taskId} className={`flex items-start gap-2.5 p-2.5 rounded-xl border ${
                              isPast
                                ? 'bg-red-50 dark:bg-red-900/10 border-red-200/60 dark:border-red-800/40'
                                : 'bg-blue-50 dark:bg-blue-900/10 border-blue-200/60 dark:border-blue-800/40'
                            }`}>
                              <span className="text-base mt-0.5 shrink-0">{emoji}</span>
                              <div className="flex-1 min-w-0">
                                <p className="text-sm font-semibold leading-tight">{task.name}</p>
                                <p className="text-[11px] text-muted-foreground mt-0.5 flex items-center gap-1">
                                  <Clock size={10} />
                                  <span>{assignee?.nickname || 'Unassigned'}</span>
                                  {isPast && <span className="text-red-500 font-semibold">· overdue</span>}
                                </p>
                              </div>
                            </div>
                          )
                        })}
                      </div>
                    </div>
                  )}

                  {selectedActivities.length === 0 && selectedDueTasks.length === 0 && (
                    <div className="flex flex-col items-center justify-center py-8 text-center">
                      <CalendarDays size={30} className="text-muted-foreground/30 mb-3" />
                      <p className="text-sm font-medium text-muted-foreground">Nothing here</p>
                      <p className="text-xs text-muted-foreground mt-0.5">No completions or due tasks on this day</p>
                    </div>
                  )}
                </CardContent>
              </Card>

              <Card className="shadow-sm border-border/60">
                <CardHeader className="pb-3 px-4">
                  <CardTitle className="text-sm font-bold">This Month · Per Person</CardTitle>
                </CardHeader>
                <CardContent className="px-4 pb-4">
                  <div className="space-y-3">
                    {members.map(m => {
                      const count = monthStats.byMember[m.uid] || 0
                      const max   = Math.max(...Object.values(monthStats.byMember), 1)
                      const pct   = Math.round((count / max) * 100)
                      return (
                        <div key={m.uid}>
                          <div className="flex items-center justify-between text-xs mb-1">
                            <div className="flex items-center gap-2">
                              <div className="w-5 h-5 rounded-full bg-primary/15 flex items-center justify-center text-primary font-bold text-[10px] shrink-0">
                                {m.nickname.charAt(0)}
                              </div>
                              <span className="font-medium">{m.nickname}</span>
                              {m.uid === user?.uid && (
                                <span className="text-[10px] text-primary font-semibold">(you)</span>
                              )}
                            </div>
                            <span className="font-bold tabular-nums">{count}</span>
                          </div>
                          <div className="w-full bg-secondary rounded-full h-1.5">
                            <div
                              className={`h-1.5 rounded-full transition-all duration-500 ${count > 0 ? 'bg-green-500' : 'bg-border'}`}
                              style={{ width: `${pct}%` }}
                            />
                          </div>
                        </div>
                      )
                    })}
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
      )}

      {/* ═══════════════════ STATS TAB ═══════════════════════════════ */}
      {activeTab === 'stats' && (
        <div className="space-y-8">

          {/* Filter controls */}
          <div className="flex items-center gap-2 flex-wrap">
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
                <button onClick={prevMonth} className="p-1 hover:bg-secondary rounded-lg transition-colors">
                  <ChevronLeft size={16} />
                </button>
                <span className="text-sm font-semibold w-36 text-center">{MONTHS[viewMonth]} {viewYear}</span>
                <button onClick={nextMonth} className="p-1 hover:bg-secondary rounded-lg transition-colors">
                  <ChevronRight size={16} />
                </button>
              </div>
            )}
          </div>

          {/* Summary cards */}
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
                <div className="text-3xl font-extrabold">{statsActiveDays}</div>
                <p className="text-xs text-muted-foreground mt-1">days with completions</p>
              </CardContent>
            </Card>
          </div>

          {/* Duty Completion Grid */}
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
                      : `${MONTHS[viewMonth]} ${viewYear} — cells show number of completions`}
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
                    {gridData.map(row => (
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

          {/* Completion breakdown bars */}
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

          {/* Reliability scores */}
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
                .map(m => {
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
      )}
    </div>
  )
}
