"use client"
import { useState, useMemo } from 'react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { ChevronLeft, ChevronRight, CheckCircle2, AlertTriangle, CalendarDays, Users, TrendingUp } from 'lucide-react'

const TASK_EMOJIS: Record<string, string> = {
  garbage: '🗑️', cleaning: '🧹', kitchen: '🍳',
  groceries: '🛒', laundry: '👕', maintenance: '🔧', other: '📋',
}

const WEEKDAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
const MONTHS = ['January','February','March','April','May','June','July','August','September','October','November','December']

function toDateKey(d: Date) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

export default function CalendarPage() {
  const { activityLog, members, tasks } = useFlatStore()
  const { user } = useAuthStore()

  const today = new Date()
  const [viewYear, setViewYear] = useState(today.getFullYear())
  const [viewMonth, setViewMonth] = useState(today.getMonth())
  const [selectedDay, setSelectedDay] = useState<string | null>(toDateKey(today))
  const [memberFilter, setMemberFilter] = useState<string>('all')

  // Build a map: dateKey → list of completed activities
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

  // Build calendar grid days
  const calendarDays = useMemo(() => {
    const firstDay = new Date(viewYear, viewMonth, 1)
    // ISO week: Mon=0 … Sun=6
    const startOffset = (firstDay.getDay() + 6) % 7
    const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate()
    const daysInPrevMonth = new Date(viewYear, viewMonth, 0).getDate()

    const days: { date: Date; isCurrentMonth: boolean }[] = []

    // Previous month overflow
    for (let i = startOffset - 1; i >= 0; i--) {
      days.push({ date: new Date(viewYear, viewMonth - 1, daysInPrevMonth - i), isCurrentMonth: false })
    }
    // Current month
    for (let d = 1; d <= daysInMonth; d++) {
      days.push({ date: new Date(viewYear, viewMonth, d), isCurrentMonth: true })
    }
    // Next month fill to complete last row
    const remaining = (7 - (days.length % 7)) % 7
    for (let d = 1; d <= remaining; d++) {
      days.push({ date: new Date(viewYear, viewMonth + 1, d), isCurrentMonth: false })
    }
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

  // Selected day details (with member filter)
  const selectedActivities = useMemo(() => {
    if (!selectedDay) return []
    const all = completionMap[selectedDay] || []
    if (memberFilter === 'all') return all
    return all.filter(a => a.userId === memberFilter)
  }, [selectedDay, completionMap, memberFilter])

  // Monthly stats
  const monthStats = useMemo(() => {
    const prefix = `${viewYear}-${String(viewMonth + 1).padStart(2, '0')}`
    const monthActivities = activityLog.filter(a =>
      a.action === 'completed_task' && a.timestamp.startsWith(prefix)
    )
    const byMember: Record<string, number> = {}
    members.forEach(m => { byMember[m.uid] = 0 })
    monthActivities.forEach(a => {
      byMember[a.userId] = (byMember[a.userId] || 0) + 1
    })
    const topMember = Object.entries(byMember).sort((a, b) => b[1] - a[1])[0]
    return {
      total: monthActivities.length,
      activeDays: new Set(monthActivities.map(a => toDateKey(new Date(a.timestamp)))).size,
      byMember,
      topMember: topMember ? members.find(m => m.uid === topMember[0]) : null,
      topCount: topMember?.[1] || 0,
    }
  }, [activityLog, viewYear, viewMonth, members])

  const todayKey = toDateKey(today)

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Task Calendar</h1>
        <p className="text-muted-foreground mt-1">Track every completion by date. Click any day to see the details.</p>
      </div>

      {/* Monthly Summary Row */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <Card className="shadow-sm border-border/60">
          <CardContent className="p-4 flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-green-500/10 flex items-center justify-center shrink-0">
              <CheckCircle2 size={20} className="text-green-500" />
            </div>
            <div>
              <p className="text-2xl font-bold">{monthStats.total}</p>
              <p className="text-xs text-muted-foreground">Completions</p>
            </div>
          </CardContent>
        </Card>
        <Card className="shadow-sm border-border/60">
          <CardContent className="p-4 flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-blue-500/10 flex items-center justify-center shrink-0">
              <CalendarDays size={20} className="text-blue-500" />
            </div>
            <div>
              <p className="text-2xl font-bold">{monthStats.activeDays}</p>
              <p className="text-xs text-muted-foreground">Active Days</p>
            </div>
          </CardContent>
        </Card>
        <Card className="shadow-sm border-border/60">
          <CardContent className="p-4 flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center shrink-0">
              <TrendingUp size={20} className="text-primary" />
            </div>
            <div>
              <p className="text-2xl font-bold">{memberFilter === 'all' ? members.length : 1}</p>
              <p className="text-xs text-muted-foreground">Contributors</p>
            </div>
          </CardContent>
        </Card>
        <Card className="shadow-sm border-border/60">
          <CardContent className="p-4 flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-orange-500/10 flex items-center justify-center shrink-0">
              <Users size={20} className="text-orange-500" />
            </div>
            <div>
              <p className="text-lg font-bold truncate">{monthStats.topMember?.nickname || '—'}</p>
              <p className="text-xs text-muted-foreground">{monthStats.topCount > 0 ? `${monthStats.topCount} tasks · Top performer` : 'No data yet'}</p>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Calendar Grid */}
        <div className="lg:col-span-2">
          <Card className="shadow-sm border-border/60">
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <CardTitle className="text-xl font-bold">{MONTHS[viewMonth]} {viewYear}</CardTitle>
                <div className="flex items-center gap-1">
                  {/* Member filter */}
                  <select
                    value={memberFilter}
                    onChange={e => setMemberFilter(e.target.value)}
                    className="text-xs bg-secondary border border-border rounded-lg px-2 py-1.5 mr-2 font-medium"
                  >
                    <option value="all">All members</option>
                    {members.map(m => (
                      <option key={m.uid} value={m.uid}>{m.nickname}</option>
                    ))}
                  </select>
                  <Button variant="outline" size="icon" className="h-8 w-8" onClick={prevMonth}>
                    <ChevronLeft size={16} />
                  </Button>
                  <Button variant="outline" size="sm" className="h-8 px-3 text-xs font-semibold" onClick={() => { setViewYear(today.getFullYear()); setViewMonth(today.getMonth()); setSelectedDay(todayKey) }}>
                    Today
                  </Button>
                  <Button variant="outline" size="icon" className="h-8 w-8" onClick={nextMonth}>
                    <ChevronRight size={16} />
                  </Button>
                </div>
              </div>
            </CardHeader>
            <CardContent className="p-0 pb-4">
              {/* Weekday headers */}
              <div className="grid grid-cols-7 mb-1 px-3">
                {WEEKDAYS.map(d => (
                  <div key={d} className="text-center text-[11px] font-bold text-muted-foreground py-2 uppercase tracking-wider">{d}</div>
                ))}
              </div>
              {/* Day cells */}
              <div className="grid grid-cols-7 gap-1 px-3">
                {calendarDays.map(({ date, isCurrentMonth }, i) => {
                  const key = toDateKey(date)
                  const dayActivities = (completionMap[key] || []).filter(
                    a => memberFilter === 'all' || a.userId === memberFilter
                  )
                  const count = dayActivities.length
                  const isToday = key === todayKey
                  const isSelected = key === selectedDay
                  const isFuture = date > today

                  return (
                    <button
                      key={i}
                      onClick={() => isCurrentMonth && setSelectedDay(key)}
                      disabled={!isCurrentMonth}
                      className={`
                        relative flex flex-col items-center justify-start p-1.5 rounded-xl min-h-[52px] text-xs transition-all
                        ${!isCurrentMonth ? 'opacity-25 cursor-default' : 'cursor-pointer hover:bg-secondary/80'}
                        ${isSelected && isCurrentMonth ? 'ring-2 ring-primary bg-primary/5' : ''}
                        ${isToday && !isSelected ? 'ring-2 ring-primary/40 bg-primary/5' : ''}
                      `}
                    >
                      <span className={`
                        w-6 h-6 flex items-center justify-center rounded-full text-xs font-bold mb-1
                        ${isToday ? 'bg-primary text-white' : isCurrentMonth ? 'text-foreground' : 'text-muted-foreground'}
                      `}>
                        {date.getDate()}
                      </span>

                      {count > 0 && (
                        <div className="flex flex-wrap gap-0.5 justify-center">
                          {count <= 3 ? (
                            Array.from({ length: count }).map((_, di) => (
                              <span key={di} className="w-1.5 h-1.5 rounded-full bg-green-500" />
                            ))
                          ) : (
                            <span className="text-[10px] font-bold text-green-600 bg-green-100 dark:bg-green-900/40 dark:text-green-400 px-1 rounded">
                              {count}
                            </span>
                          )}
                        </div>
                      )}

                      {isFuture && isCurrentMonth && count === 0 && (
                        <span className="w-1.5 h-1.5 rounded-full bg-border mt-0.5" />
                      )}
                    </button>
                  )
                })}
              </div>

              {/* Legend */}
              <div className="flex items-center gap-4 px-4 mt-4 pt-3 border-t border-border/60">
                <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
                  <span className="w-2 h-2 rounded-full bg-green-500" /> Completed
                </div>
                <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
                  <span className="w-2 h-2 rounded-full bg-primary" /> Today
                </div>
                <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
                  <span className="w-2 h-2 rounded-full border-2 border-primary/40" /> Selected
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Day Detail Panel */}
        <div className="space-y-4">
          <Card className="shadow-sm border-border/60 h-fit">
            <CardHeader className="pb-3">
              <CardTitle className="text-base font-bold">
                {selectedDay
                  ? new Date(selectedDay + 'T12:00:00').toLocaleDateString('en-GB', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })
                  : 'Select a day'}
              </CardTitle>
              <CardDescription>
                {selectedActivities.length > 0
                  ? `${selectedActivities.length} task${selectedActivities.length > 1 ? 's' : ''} completed`
                  : 'No completions recorded'}
              </CardDescription>
            </CardHeader>
            <CardContent>
              {selectedActivities.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-8 text-center">
                  <CalendarDays size={32} className="text-muted-foreground/40 mb-3" />
                  <p className="text-sm text-muted-foreground font-medium">No tasks completed</p>
                  <p className="text-xs text-muted-foreground mt-1">on this day</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {selectedActivities.map(activity => {
                    const member = members.find(m => m.uid === activity.userId)
                    const taskName = activity.details.replace('completed ', '')
                    const task = tasks.find(t => t.name === taskName)
                    const emoji = task ? (TASK_EMOJIS[task.type] ?? '✅') : '✅'
                    const time = new Date(activity.timestamp).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })

                    return (
                      <div key={activity.id} className="flex items-start gap-3 p-3 rounded-xl bg-secondary/40 border border-border/40">
                        <span className="text-xl">{emoji}</span>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-semibold">{taskName}</p>
                          <p className="text-xs text-muted-foreground mt-0.5">
                            by <span className="font-medium text-foreground">{member?.nickname || 'Unknown'}</span>
                          </p>
                        </div>
                        <span className="text-xs text-muted-foreground font-mono shrink-0">{time}</span>
                      </div>
                    )
                  })}
                </div>
              )}
            </CardContent>
          </Card>

          {/* Per-member month breakdown */}
          <Card className="shadow-sm border-border/60">
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-bold">This Month · Per Person</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {members.map(m => {
                  const count = monthStats.byMember[m.uid] || 0
                  const max = Math.max(...Object.values(monthStats.byMember), 1)
                  const pct = Math.round((count / max) * 100)
                  return (
                    <div key={m.uid}>
                      <div className="flex items-center justify-between text-xs mb-1">
                        <div className="flex items-center gap-2">
                          <div className="w-5 h-5 rounded-full bg-primary/20 flex items-center justify-center text-primary font-bold text-[10px]">
                            {m.nickname.charAt(0)}
                          </div>
                          <span className="font-medium">{m.nickname}</span>
                          {m.uid === user?.uid && <span className="text-[10px] text-primary font-semibold">(you)</span>}
                        </div>
                        <span className="font-bold">{count}</span>
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
