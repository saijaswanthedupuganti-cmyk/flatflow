"use client"
import { useState, useMemo } from 'react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { formatDateTime, timeAgo } from '@/lib/rotationEngine'
import { Activity as ActivityIcon, CheckCircle2, RefreshCw, ArrowLeftRight, AlertTriangle, Settings, Eye, EyeOff, ChevronDown, Pencil, Receipt } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'

// ── Action type groupings ────────────────────────────────────────────────
const ACTION_GROUPS = {
  all:      { label: 'All',           actions: null },
  tasks:    { label: 'Task Completed', actions: ['completed_task', 'skipped_task'] },
  transfer: { label: 'Transfers & Swaps', actions: ['transferred_task', 'swap_requested', 'swap_resolved'] },
  status:   { label: 'Status Changes', actions: ['status_change', 'returned_early'] },
  expenses: { label: 'Expenses',       actions: ['expense_added', 'expense_deleted', 'settlement_added'] },
  bills:    { label: 'Bills',          actions: ['bill_generated', 'bill_paid', 'bill_skipped', 'bill_edited', 'bill_deleted'] },
  system:   { label: 'System',        actions: ['overdue_alert', 'task_created', 'task_deleted', 'system_override'] },
} as const

type GroupKey = keyof typeof ACTION_GROUPS

function dotColour(action: string, isSystem: boolean) {
  if (isSystem)                                           return 'bg-orange-500'
  if (action === 'completed_task')                        return 'bg-green-500'
  if (action === 'skipped_task')                          return 'bg-red-500'
  if (action === 'status_change' || action === 'returned_early') return 'bg-blue-500'
  if (['transferred_task','swap_requested','swap_resolved'].includes(action)) return 'bg-purple-500'
  if (action === 'expense_added' || action === 'settlement_added') return 'bg-teal-500'
  if (action === 'expense_deleted')                       return 'bg-red-400'
  if (['bill_generated','bill_paid','bill_edited'].includes(action)) return 'bg-amber-500'
  if (action === 'bill_deleted')                          return 'bg-red-400'
  return 'bg-muted-foreground'
}

function GroupIcon({ action, isSystem }: { action: string; isSystem: boolean }) {
  const cls = 'shrink-0'
  if (isSystem)                return <AlertTriangle  size={14} className={`${cls} text-orange-500`} />
  if (action === 'completed_task') return <CheckCircle2 size={14} className={`${cls} text-green-500`} />
  if (['transferred_task','swap_requested','swap_resolved'].includes(action))
                               return <ArrowLeftRight size={14} className={`${cls} text-purple-500`} />
  if (action === 'status_change' || action === 'returned_early')
                               return <RefreshCw      size={14} className={`${cls} text-blue-500`} />
  if (['expense_added','expense_deleted','settlement_added'].includes(action))
                               return <Receipt        size={14} className={`${cls} text-teal-500`} />
  if (['bill_generated','bill_paid','bill_skipped','bill_edited','bill_deleted'].includes(action))
                               return <Receipt        size={14} className={`${cls} text-amber-500`} />
  return                              <Settings       size={14} className={`${cls} text-muted-foreground`} />
}

export default function ActivityPage() {
  const { activityLog, members, toggleActivityHidden, editCompletionDate } = useFlatStore()
  const { user } = useAuthStore()

  const currentMember = members.find(m => m.uid === (user?.uid || 'u1'))
  const isAdmin       = currentMember?.role === 'admin'

  const [group,          setGroup]          = useState<GroupKey>('all')
  const [memberFilter,   setMemberFilter]   = useState<string>('all')
  const [showHidden,     setShowHidden]     = useState(false)
  const [editingId,      setEditingId]      = useState<string | null>(null)
  const [editDate,       setEditDate]       = useState('')
  const [editSaving,     setEditSaving]     = useState(false)

  function startEdit(id: string, timestamp: string) {
    const ts = new Date(timestamp)
    const yyyy = ts.getFullYear()
    const mm   = String(ts.getMonth() + 1).padStart(2, '0')
    const dd   = String(ts.getDate()).padStart(2, '0')
    setEditDate(`${yyyy}-${mm}-${dd}`)
    setEditingId(id)
  }

  async function saveEdit(activityId: string) {
    if (!editDate) return
    setEditSaving(true)
    await editCompletionDate(activityId, editDate)
    setEditSaving(false)
    setEditingId(null)
  }

  const filtered = useMemo(() => {
    const groupDef = ACTION_GROUPS[group]
    return activityLog.filter(a => {
      if (!showHidden && a.hidden)                                          return false
      if (groupDef.actions && !groupDef.actions.includes(a.action as never)) return false
      if (memberFilter !== 'all' && a.userId !== memberFilter)              return false
      return true
    })
  }, [activityLog, group, memberFilter, showHidden])

  const hiddenCount = activityLog.filter(a => a.hidden).length

  return (
    <div className="space-y-6 max-w-3xl">

      {/* ── Header ──────────────────────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-3">
        <div>
          <p className="text-xs font-bold text-muted-foreground uppercase tracking-widest mb-1">
            <a href="/dashboard" className="hover:text-foreground transition-colors">Dashboard</a>
            <span className="mx-1.5">›</span>Activity
          </p>
          <h1 className="text-2xl font-extrabold tracking-tight flex items-center gap-2">
            <ActivityIcon size={22} className="text-primary" />
            Activity Log
          </h1>
          <p className="text-muted-foreground text-sm mt-1">
            {filtered.length} event{filtered.length !== 1 ? 's' : ''} · full flat history
          </p>
        </div>

        {isAdmin && hiddenCount > 0 && (
          <button
            onClick={() => setShowHidden(v => !v)}
            className="flex items-center gap-1.5 text-xs font-semibold text-muted-foreground hover:text-foreground border border-border rounded-lg px-3 py-1.5 transition-colors shrink-0"
          >
            {showHidden ? <><Eye size={13} /> Hide hidden ({hiddenCount})</> : <><EyeOff size={13} /> Show hidden ({hiddenCount})</>}
          </button>
        )}
      </div>

      {/* ── Filters ─────────────────────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row gap-3">
        {/* Action type tabs */}
        <div className="flex flex-wrap gap-1.5 flex-1">
          {(Object.entries(ACTION_GROUPS) as [GroupKey, typeof ACTION_GROUPS[GroupKey]][]).map(([key, cfg]) => (
            <button
              key={key}
              onClick={() => setGroup(key)}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold border transition-all ${
                group === key
                  ? 'bg-primary text-white border-primary shadow-sm'
                  : 'bg-background text-muted-foreground border-border hover:bg-secondary/80'
              }`}
            >
              {cfg.label}
            </button>
          ))}
        </div>

        {/* Member filter */}
        <div className="relative shrink-0">
          <select
            value={memberFilter}
            onChange={e => setMemberFilter(e.target.value)}
            className="appearance-none bg-background border border-border rounded-xl pl-3 pr-8 py-1.5 text-xs font-semibold text-foreground focus:outline-none focus:ring-2 focus:ring-primary/30 cursor-pointer"
          >
            <option value="all">All members</option>
            <option value="system">System</option>
            {members.map(m => (
              <option key={m.uid} value={m.uid}>{m.nickname}</option>
            ))}
          </select>
          <ChevronDown size={12} className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground pointer-events-none" />
        </div>
      </div>

      {/* ── Timeline ────────────────────────────────────────────────── */}
      <Card className="shadow-sm">
        <CardContent className="p-6">
          {filtered.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-16 text-center">
              <ActivityIcon size={36} className="text-muted-foreground/25 mb-3" />
              <p className="font-semibold text-muted-foreground">No activity found</p>
              <p className="text-sm text-muted-foreground/60 mt-1">Try a different filter.</p>
            </div>
          ) : (
            <div className="space-y-0 border-l-2 border-muted ml-3 pl-5 relative">
              {filtered.map((activity, idx) => {
                const isSystem  = activity.userId === 'system'
                const userObj   = members.find(m => m.uid === activity.userId)
                const actDate   = new Date(activity.timestamp)
                const isLast    = idx === filtered.length - 1

                return (
                  <div
                    key={activity.id}
                    className={`relative group ${isLast ? '' : 'pb-5'} ${activity.hidden ? 'opacity-40' : ''}`}
                  >
                    {/* Timeline dot */}
                    <div className={`absolute -left-[25px] top-1 w-3.5 h-3.5 rounded-full border-2 border-card ${dotColour(activity.action, isSystem)}`} />

                    <div className="flex items-start justify-between gap-3">
                      <div className="flex items-start gap-2 min-w-0">
                        <GroupIcon action={activity.action} isSystem={isSystem} />
                        <div className="min-w-0">
                          <p className="text-sm leading-snug">
                            <span className="font-semibold">{isSystem ? 'System' : userObj?.nickname || 'Someone'}</span>{' '}
                            {activity.details}.
                          </p>
                          <p className="text-xs text-muted-foreground mt-0.5" title={formatDateTime(actDate)}>
                            {timeAgo(activity.timestamp)} · {actDate.toLocaleDateString('en-GB', { weekday: 'short', day: 'numeric', month: 'short' })} at {actDate.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}
                          </p>
                          {/* Inline date editor */}
                          {activity.action === 'completed_task' && editingId === activity.id && (
                            <div className="flex items-center gap-2 mt-2">
                              <input
                                type="date"
                                value={editDate}
                                max={new Date().toISOString().slice(0, 10)}
                                onChange={e => setEditDate(e.target.value)}
                                className="text-xs border border-border rounded-lg px-2 py-1 bg-background focus:outline-none focus:ring-1 focus:ring-primary/30"
                              />
                              <button
                                onClick={() => saveEdit(activity.id)}
                                disabled={editSaving || !editDate}
                                className="text-xs font-semibold text-primary hover:text-primary/80 disabled:opacity-40 transition-colors"
                              >
                                {editSaving ? 'Saving…' : 'Save'}
                              </button>
                              <button
                                onClick={() => setEditingId(null)}
                                className="text-xs font-semibold text-muted-foreground hover:text-foreground transition-colors"
                              >
                                Cancel
                              </button>
                            </div>
                          )}
                        </div>
                      </div>

                      {/* Action buttons */}
                      <div className="flex items-center gap-1 shrink-0 mt-0.5">
                        {/* Edit completion date — own entries or admin */}
                        {activity.action === 'completed_task' && (activity.userId === user?.uid || isAdmin) && editingId !== activity.id && (
                          <button
                            onClick={() => startEdit(activity.id, activity.timestamp)}
                            className="opacity-0 group-hover:opacity-100 text-muted-foreground/50 hover:text-muted-foreground transition-opacity"
                            title="Edit completion date"
                          >
                            <Pencil size={13} />
                          </button>
                        )}
                        {/* Admin hide toggle */}
                        {isAdmin && (
                          <button
                            onClick={() => toggleActivityHidden(activity.id)}
                            className="opacity-0 group-hover:opacity-100 text-muted-foreground/50 hover:text-muted-foreground transition-opacity"
                            title={activity.hidden ? 'Unhide' : 'Hide'}
                          >
                            {activity.hidden ? <Eye size={13} /> : <EyeOff size={13} />}
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
