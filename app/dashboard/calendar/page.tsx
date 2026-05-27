"use client"
import { useState, useMemo } from 'react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { ChevronLeft, ChevronRight, CheckCircle2, CalendarDays, Users, TrendingUp, Clock } from 'lucide-react'

const TASK_EMOJIS: Record<string, string> = {
  garbage: '🗑️', cleaning: '🧹', kitchen: '🍳',
  groceries: '🛒', laundry: '👕', maintenance: '🔧', other: '📋',
}

const WEEKDAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
const MONTHS   = ['January','February','March','April','May','June','July','August','September','October','November','December']

function toDateKey(d: Date) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

export default function CalendarPage() {
  const { activityLog, members, tasks } = useFlatStore()
  const { user } = useAuthStore()

  const today      = new Date()
  const [viewYear, setViewYear]       = useState(today.getFullYear())
  const [viewMonth, setViewMonth]     = useState(today.getMonth())
  const [selectedDay, setSelectedDay] = useState<string | null>(toDateKey(today))
  const [memberFilter, setMemberFilter] = useState<string>('all')

  /* ── Completion map: dateKey → completed_task activities ── */
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

  /* ── Due-date map: dateKey → Task[] ─────────────────────── */
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

  /* ── Calendar grid ───────────────────────────────────────── */
  const calendarDays = useMemo(() => {
    const firstDay      = new Date(viewYear, viewMonth, 1)
    const startOffset   = (firstDay.getDay() + 6) % 7   // Mon = 0
    const daysInMonth   = new Date(viewYear, viewMonth + 1, 0).getDate()
    const daysInPrev    = new Date(viewYear, viewMonth, 0).getDate()

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

  const prevMonth = () => {
    if (viewMonth === 0) { setViewYear(y => y - 1); setViewMonth(11) }
    else setViewMonth(m => m - 1)
  }
  const nextMonth = () => {
    if (viewMonth === 11) { setViewYear(y => y + 1); setViewMonth(0) }
    else setViewMonth(m => m + 1)
  }

  /* ── Selected day detail ─────────────────────────────────── */
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

  /* ── Monthly stats ───────────────────────────────────────── */
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

  const todayKey = toDateKey(today)

  return (
    <div className="space-y-5 max-w-5xl">

      {/* ── Page header ────────────────────────────────────────── */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">Task Calendar</h1>
        <p className="text-muted-foreground mt-0.5 text-sm hidden sm:block">
          Track completions and upcoming due dates. Tap any day to see details.
        </p>
      </div>

      {/* ── Monthly summary row ────────────────────────────────── */}
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

      {/* ── Main grid ──────────────────────────────────────────── */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">

        {/* Calendar */}
        <div className="lg:col-span-2">
          <Card className="shadow-sm border-border/60">

            {/* Nav row */}
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
              {/* Mobile member filter */}
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
              {/* Weekday headers */}
              <div className="grid grid-cols-7 px-3 mb-1">
                {WEEKDAYS.map(d => (
                  <div key={d} className="text-center text-[10px] font-bold text-muted-foreground py-1.5 uppercase tracking-wider">
                    {d}
                  </div>
                ))}
              </div>

              {/* Day cells */}
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
                        !isCurrentMonth    ? 'opacity-20 cursor-default'               : 'cursor-pointer hover:bg-secondary/70',
                        isSelected && isCurrentMonth ? 'ring-2 ring-primary bg-primary/5'  : '',
                        isToday && !isSelected       ? 'ring-2 ring-primary/40 bg-primary/5' : '',
                      ].join(' ')}
                    >
                      {/* Date number */}
                      <span className={[
                        'w-6 h-6 flex items-center justify-center rounded-full text-xs font-bold mb-0.5',
                        isToday      ? 'bg-primary text-white'         :
                        isCurrentMonth ? 'text-foreground'             : 'text-muted-foreground',
                      ].join(' ')}>
                        {date.getDate()}
                      </span>

                      {/* Dots row */}
                      <div className="flex items-center gap-0.5 justify-center flex-wrap">
                        {/* Green dots = completions */}
                        {completions.length > 0 && completions.length <= 2
                          ? Array.from({ length: completions.length }).map((_, di) => (
                              <span key={`c${di}`} className="w-1.5 h-1.5 rounded-full bg-green-500" />
                            ))
                          : completions.length > 2
                          ? <span className="text-[9px] font-bold text-green-600 bg-green-100 dark:bg-green-900/40 dark:text-green-400 px-1 rounded leading-tight">
                              {completions.length}
                            </span>
                          : null}

                        {/* Blue dots = due tasks */}
                        {isFuture && dueTasks.length > 0 && dueTasks.length <= 2
                          ? Array.from({ length: dueTasks.length }).map((_, di) => (
                              <span key={`d${di}`} className="w-1.5 h-1.5 rounded-full bg-blue-400" />
                            ))
                          : isFuture && dueTasks.length > 2
                          ? <span className="text-[9px] font-bold text-blue-600 bg-blue-100 dark:bg-blue-900/40 dark:text-blue-400 px-1 rounded leading-tight">
                              {dueTasks.length}
                            </span>
                          : null}

                        {/* Today with due tasks */}
                        {!isFuture && dueTasks.length > 0 && isToday && (
                          <span className="w-1.5 h-1.5 rounded-full bg-orange-400" />
                        )}
                      </div>
                    </button>
                  )
                })}
              </div>

              {/* Legend */}
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

        {/* Right panel */}
        <div className="space-y-4">

          {/* Day detail */}
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

              {/* Completed tasks */}
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

              {/* Due tasks */}
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

              {/* Empty state */}
              {selectedActivities.length === 0 && selectedDueTasks.length === 0 && (
                <div className="flex flex-col items-center justify-center py-8 text-center">
                  <CalendarDays size={30} className="text-muted-foreground/30 mb-3" />
                  <p className="text-sm font-medium text-muted-foreground">Nothing here</p>
                  <p className="text-xs text-muted-foreground mt-0.5">No completions or due tasks on this day</p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Per-member month breakdown */}
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
  )
}
