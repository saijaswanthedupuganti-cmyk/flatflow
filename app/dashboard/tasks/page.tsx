"use client"
import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  Plus, ArrowRight, ArrowDown, AlertTriangle, PauseCircle, Clock, Edit2,
  Trash2, ClipboardList, CheckCircle2, X, CalendarDays, Repeat2, Users, Zap,
  ChevronDown, ChevronRight, Inbox, ArrowDownLeft, ArrowUpRight, Check,
} from 'lucide-react'
import { getPriorityWeight, getTaskDateInfo } from '@/lib/rotationEngine'
import { useSubscription } from '@/hooks/useSubscription'
import SubscriptionUpsell from '@/components/SubscriptionUpsell'

const TASK_EMOJIS: Record<string, string> = {
  garbage: '🗑️', cleaning: '🧹', kitchen: '🍳', groceries: '🛒',
  laundry: '👕', maintenance: '🔧', other: '📋',
}

const PRIORITY_CONFIG = {
  high:   { label: 'High',   bg: 'bg-red-100 dark:bg-red-900/30',       text: 'text-red-600 dark:text-red-400',       dot: 'bg-red-500',    border: 'border-l-red-500' },
  medium: { label: 'Medium', bg: 'bg-yellow-100 dark:bg-yellow-900/30', text: 'text-yellow-700 dark:text-yellow-400', dot: 'bg-yellow-500', border: 'border-l-yellow-500' },
  low:    { label: 'Low',    bg: 'bg-green-100 dark:bg-green-900/30',   text: 'text-green-700 dark:text-green-400',   dot: 'bg-green-500',  border: 'border-l-green-400' },
}

const FREQ_CONFIG = {
  daily:    { label: 'Daily',    bg: 'bg-orange-100 dark:bg-orange-900/30', text: 'text-orange-700 dark:text-orange-400' },
  weekly:   { label: 'Weekly',   bg: 'bg-blue-100 dark:bg-blue-900/30',    text: 'text-blue-700 dark:text-blue-400' },
  monthly:  { label: 'Monthly',  bg: 'bg-purple-100 dark:bg-purple-900/30', text: 'text-purple-700 dark:text-purple-400' },
  one_time: { label: 'One-time', bg: 'bg-teal-100 dark:bg-teal-900/30',    text: 'text-teal-700 dark:text-teal-400' },
}

export default function TasksPage() {
  const { tasks, members, swapRequests, manuallyAssignTask, createTask, editTask, deleteTask, resolveSwapRequest } = useFlatStore()
  const { user } = useAuthStore()
  const { can } = useSubscription()
  const [showUpsell, setShowUpsell] = useState(false)

  const [selectedAssigneeId, setSelectedAssigneeId]   = useState<string>('')
  const [overridingTaskId, setOverridingTaskId]       = useState<string | null>(null)
  const [editingTaskId, setEditingTaskId]             = useState<string | null>(null)
  const [editName, setEditName]                       = useState('')
  const [editType, setEditType]                       = useState('')
  const [editPriority, setEditPriority]               = useState<'high' | 'medium' | 'low'>('medium')
  const [editFreq, setEditFreq]                       = useState<'daily' | 'weekly' | 'monthly' | 'one_time'>('weekly')
  const [editDueDate, setEditDueDate]                 = useState('')
  const [isCreating, setIsCreating]                   = useState(false)
  const [newTaskName, setNewTaskName]                 = useState('')
  const [newTaskType, setNewTaskType]                 = useState('cleaning')
  const [newTaskPriority, setNewTaskPriority]         = useState<'high' | 'medium' | 'low'>('medium')
  const [newTaskFreq, setNewTaskFreq]                 = useState<'daily' | 'weekly' | 'monthly'>('weekly')
  const [newTaskDueDate, setNewTaskDueDate]           = useState(
    new Date(Date.now() + 86_400_000).toISOString().split('T')[0]  // tomorrow
  )
  const [newTaskAssignee, setNewTaskAssignee]         = useState<string>('')
  const [selectedMembers, setSelectedMembers]         = useState<string[]>([])

  // ── Type selector + Group Task ───────────────────────────────────────────
  const [showTypeSelector, setShowTypeSelector]       = useState(false)
  const [isGroupCreating, setIsGroupCreating]         = useState(false)
  const [groupTaskName, setGroupTaskName]             = useState('')
  const [groupTaskDueDate, setGroupTaskDueDate]       = useState(new Date().toISOString().split('T')[0])
  const [groupTaskPriority, setGroupTaskPriority]     = useState<'high' | 'medium' | 'low'>('medium')
  const [groupTaskType, setGroupTaskType]             = useState('cleaning')
  const [groupSubtasks, setGroupSubtasks]             = useState([
    { id: '1', name: '', assigneeId: '' },
    { id: '2', name: '', assigneeId: '' },
  ])
  const [groupIsRecurring, setGroupIsRecurring]       = useState(false)
  const [groupFreq, setGroupFreq]                     = useState<'daily' | 'weekly' | 'monthly'>('weekly')
  const [groupStartDate, setGroupStartDate]           = useState(new Date().toISOString().split('T')[0])

  // ── Temp Task ────────────────────────────────────────────────────────────
  const [isTempCreating, setIsTempCreating]           = useState(false)
  const [tempTaskName, setTempTaskName]               = useState('')
  const [tempTaskAssignee, setTempTaskAssignee]       = useState('')
  const [tempTaskDueDate, setTempTaskDueDate]         = useState(new Date().toISOString().split('T')[0])
  const [tempTaskPriority, setTempTaskPriority]       = useState<'high' | 'medium' | 'low'>('medium')
  const [tempTaskType, setTempTaskType]               = useState('other')
  const [useCustomDate, setUseCustomDate]             = useState(false)
  const [newTaskStartDate, setNewTaskStartDate]       = useState(
    new Date().toISOString().split('T')[0]   // today yyyy-MM-dd
  )
  const [expandedTaskId, setExpandedTaskId] = useState<string | null>(null)
  const [showSwapSheet, setShowSwapSheet]   = useState(false)

  // Auto-open task creation when navigated here from the Quick Add FAB
  useEffect(() => {
    if (typeof window !== 'undefined' && new URLSearchParams(window.location.search).get('add') === '1') {
      if (!can('create_task')) { setShowUpsell(true) }
      else { setShowTypeSelector(true) }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const adminId       = user?.uid || 'u1'
  const isAdmin       = members.find(m => m.uid === user?.uid)?.role === 'admin'
  const activeMembers = members.filter(m => m.status !== 'inactive')
  // For manual override: only show members who are currently available or busy
  const assignableMembers = members.filter(m => m.status === 'available' || m.status === 'busy')

  const freqIntervalMs =
    newTaskFreq === 'daily'  ? 86_400_000 :
    newTaskFreq === 'weekly' ? 604_800_000 :
    /* monthly */              2_592_000_000

  const handleCreate = () => {
    if (!newTaskName.trim() || selectedMembers.length === 0) return
    const startDate = (useCustomDate && newTaskStartDate)
      ? new Date(newTaskStartDate + 'T00:00:00')
      : new Date()
    createTask({
      name: newTaskName,
      type: newTaskType,
      priority: newTaskPriority,
      frequency: newTaskFreq,
      currentAssignedUserId: selectedMembers[0],
      queueOrder: selectedMembers,
      dueDate: new Date(startDate.getTime() + freqIntervalMs).toISOString(),
      lastCompletedAt: startDate.toISOString(),
    }, adminId)
    setIsCreating(false)
    setNewTaskName('')
    setSelectedMembers(activeMembers.map(m => m.uid))
    setUseCustomDate(false)
    setNewTaskStartDate(new Date().toISOString().split('T')[0])
  }

  const toggleMember = (uid: string) =>
    setSelectedMembers(prev =>
      prev.includes(uid) ? prev.filter(id => id !== uid) : [...prev, uid]
    )

  const handleDelete = (taskId: string, name: string) => {
    if (confirm(`Delete "${name}" permanently?`)) deleteTask(taskId, adminId)
  }

  const handleManualAssign = (taskId: string) => {
    if (selectedAssigneeId) {
      manuallyAssignTask(taskId, selectedAssigneeId, adminId)
      setOverridingTaskId(null)
      setSelectedAssigneeId('')
    }
  }

  const handleCreateGroupTask = async () => {
    const valid = groupSubtasks.filter(s => s.name.trim() && s.assigneeId)
    if (!groupTaskName.trim() || valid.length === 0) return
    const groupId = crypto.randomUUID()
    const now = new Date().toISOString()

    for (const sub of valid) {
      if (groupIsRecurring) {
        // Recurring group task: assigned person is first in queue, rest of flat as OOS fallback
        const freqMs = groupFreq === 'daily' ? 86_400_000 : groupFreq === 'weekly' ? 604_800_000 : 2_592_000_000
        const start = new Date(groupStartDate + 'T00:00:00')
        const queueOrder = [
          sub.assigneeId,
          ...activeMembers.filter(m => m.uid !== sub.assigneeId).map(m => m.uid),
        ]
        await createTask({
          name: sub.name.trim(),
          type: groupTaskType,
          priority: groupTaskPriority,
          frequency: groupFreq,
          currentAssignedUserId: sub.assigneeId,
          queueOrder,
          dueDate: new Date(start.getTime() + freqMs).toISOString(),
          lastCompletedAt: start.toISOString(),
          groupId,
          groupName: groupTaskName.trim(),
        }, adminId)
      } else {
        // One-time group task: assigned to one person, done once
        await createTask({
          name: sub.name.trim(),
          type: groupTaskType,
          priority: groupTaskPriority,
          frequency: 'one_time',
          currentAssignedUserId: sub.assigneeId,
          queueOrder: [sub.assigneeId],
          dueDate: new Date(groupTaskDueDate + 'T23:59:59').toISOString(),
          lastCompletedAt: now,
          groupId,
          groupName: groupTaskName.trim(),
        }, adminId)
      }
    }

    setIsGroupCreating(false)
    setGroupTaskName('')
    setGroupTaskDueDate(new Date().toISOString().split('T')[0])
    setGroupTaskPriority('medium')
    setGroupTaskType('cleaning')
    setGroupSubtasks([{ id: '1', name: '', assigneeId: '' }, { id: '2', name: '', assigneeId: '' }])
    setGroupIsRecurring(false)
    setGroupFreq('weekly')
  }

  const handleCreateTempTask = async () => {
    if (!tempTaskName.trim() || !tempTaskAssignee) return
    await createTask({
      name: tempTaskName.trim(),
      type: tempTaskType,
      priority: tempTaskPriority,
      frequency: 'one_time',
      currentAssignedUserId: tempTaskAssignee,
      queueOrder: [tempTaskAssignee],
      dueDate: new Date(tempTaskDueDate + 'T23:59:59').toISOString(),
      lastCompletedAt: new Date().toISOString(),
    }, adminId)
    setIsTempCreating(false)
    setTempTaskName('')
    setTempTaskAssignee('')
    setTempTaskDueDate(new Date().toISOString().split('T')[0])
    setTempTaskPriority('medium')
    setTempTaskType('other')
  }

  const overdueCount     = tasks.filter(t => t.status === 'overdue').length
  const pendingSwapCount = swapRequests.filter(r => r.status === 'pending').length
  const sortedTasks  = [...tasks].sort((a, b) => {
    if (a.status === 'overdue' && b.status !== 'overdue') return -1
    if (b.status === 'overdue' && a.status !== 'overdue') return 1
    return getPriorityWeight(b.priority) - getPriorityWeight(a.priority)
  })

  // Computed preview of next-due date for custom calendar
  const customDuePreview = newTaskStartDate
    ? new Date(new Date(newTaskStartDate + 'T00:00:00').getTime() + freqIntervalMs)
        .toLocaleDateString('en-GB', { weekday: 'short', day: 'numeric', month: 'short' })
    : null

  // ── Member view (non-admin) ─────────────────────────────────────────────
  if (!isAdmin) {
    const myReceived = swapRequests
      .filter(r => r.toUserId === adminId)
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    const mySent = swapRequests
      .filter(r => r.fromUserId === adminId)
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())

    return (
      <div className="space-y-5 max-w-5xl">

        {/* Header */}
        <div className="flex items-start justify-between gap-3">
          <div>
            <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">Tasks & Rotation</h1>
            <p className="text-muted-foreground mt-0.5 text-sm hidden sm:block">Flat duties and who&apos;s up next.</p>
          </div>
          {overdueCount > 0 && (
            <div className="flex items-center gap-1.5 bg-red-100 dark:bg-red-900/30 border border-red-200 dark:border-red-800 rounded-xl px-2.5 py-1.5 mt-1 shrink-0">
              <span className="w-1.5 h-1.5 rounded-full bg-red-500 animate-pulse" />
              <span className="text-[11px] font-bold text-red-600 dark:text-red-400">
                {overdueCount}<span className="hidden sm:inline"> overdue</span>
              </span>
            </div>
          )}
        </div>

        {/* Swap Requests button → bottom sheet */}
        <button
          onClick={() => setShowSwapSheet(true)}
          className="w-full flex items-center gap-3 px-4 py-3.5 rounded-2xl border transition-all active:scale-[0.98] bg-violet-50 dark:bg-violet-950/20 border-violet-200 dark:border-violet-900/40"
        >
          <div className="w-9 h-9 rounded-full bg-violet-100 dark:bg-violet-900/30 flex items-center justify-center shrink-0">
            <Repeat2 size={18} className="text-violet-600 dark:text-violet-400" />
          </div>
          <div className="flex-1 min-w-0 text-left">
            <p className="text-sm font-bold text-foreground">Swap Requests</p>
            <p className="text-[11px] text-muted-foreground">
              {pendingSwapCount > 0
                ? `${pendingSwapCount} pending swap${pendingSwapCount > 1 ? 's' : ''} — tap to view`
                : 'No pending swaps right now'}
            </p>
          </div>
          {pendingSwapCount > 0 && (
            <span className="bg-violet-500 text-white text-[10px] font-extrabold px-2 py-0.5 rounded-full shrink-0">
              {pendingSwapCount}
            </span>
          )}
          <ChevronRight size={15} className="text-muted-foreground shrink-0" />
        </button>

        {/* Compact task cards */}
        <div className="space-y-2">
          {sortedTasks.map(task => {
            const isExpanded    = expandedTaskId === task.taskId
            const dateInfo      = getTaskDateInfo(task)
            const pCfg          = PRIORITY_CONFIG[task.priority] ?? PRIORITY_CONFIG.medium
            const fCfg          = FREQ_CONFIG[task.frequency as keyof typeof FREQ_CONFIG] ?? FREQ_CONFIG.weekly
            const emoji         = TASK_EMOJIS[task.type] ?? '📋'
            const assignee      = members.find(m => m.uid === task.currentAssignedUserId)
            const isMyTask      = task.currentAssignedUserId === adminId
            const isOneTimeDone = task.frequency === 'one_time' && task.status === 'completed'

            return (
              <div
                key={task.taskId}
                onClick={() => setExpandedTaskId(isExpanded ? null : task.taskId)}
                className={`rounded-2xl border-l-4 ${pCfg.border} border border-border/60 bg-card shadow-sm cursor-pointer select-none transition-all active:scale-[0.99] overflow-hidden ${isOneTimeDone ? 'opacity-60' : ''}`}
              >
                {/* Collapsed row */}
                <div className="flex items-center gap-3 px-4 py-3">
                  <span className="text-xl shrink-0">{emoji}</span>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <p className="text-sm font-bold truncate">{task.name}</p>
                      {isMyTask && !isOneTimeDone && (
                        <span className="text-[10px] font-extrabold bg-primary text-primary-foreground px-1.5 py-0.5 rounded-full shrink-0">YOU</span>
                      )}
                      {task.status === 'overdue' && !isOneTimeDone && (
                        <span className="text-[10px] font-bold bg-red-100 dark:bg-red-900/40 text-red-600 dark:text-red-400 px-1.5 py-0.5 rounded-full shrink-0">
                          {dateInfo.overdueDays}d overdue
                        </span>
                      )}
                      {isOneTimeDone && (
                        <span className="text-[10px] font-bold bg-teal-100 dark:bg-teal-900/40 text-teal-700 dark:text-teal-400 px-1.5 py-0.5 rounded-full shrink-0">Done</span>
                      )}
                    </div>
                    <p className="text-[11px] text-muted-foreground mt-0.5">
                      {assignee?.nickname ?? '—'} · Due {dateInfo.dueDateFormatted}
                    </p>
                  </div>
                  <ChevronDown
                    size={15}
                    className={`text-muted-foreground shrink-0 transition-transform duration-200 ${isExpanded ? 'rotate-180' : ''}`}
                  />
                </div>

                {/* Expanded details */}
                {isExpanded && (
                  <div className="px-4 pb-4 border-t border-border/40">
                    <div className="flex items-center gap-1.5 pt-3 flex-wrap">
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${fCfg.bg} ${fCfg.text}`}>{fCfg.label}</span>
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${pCfg.bg} ${pCfg.text}`}>● {pCfg.label}</span>
                      <span className="text-[11px] text-muted-foreground flex items-center gap-1">
                        <Clock size={10} />Due {dateInfo.dueDateFormatted}
                      </span>
                    </div>

                    {task.frequency !== 'one_time' ? (
                      <div className="mt-3">
                        <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-2">Rotation Queue</p>
                        <div className="flex flex-wrap gap-1.5">
                          {task.queueOrder.map((uid) => {
                            const m         = members.find(x => x.uid === uid)
                            const isCurrent = uid === task.currentAssignedUserId
                            if (!m) return null
                            return (
                              <div key={uid} className={`flex items-center gap-1.5 px-2.5 py-1.5 rounded-xl border-2 transition-all ${
                                isCurrent
                                  ? 'bg-primary border-primary text-white shadow-sm'
                                  : 'bg-card border-border text-muted-foreground'
                              }`}>
                                <span className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold shrink-0 ${
                                  isCurrent ? 'bg-white/20 text-white' : 'bg-secondary text-foreground'
                                }`}>
                                  {m.nickname.charAt(0)}
                                </span>
                                <span className="text-xs font-semibold">{m.nickname}</span>
                                {isCurrent && (
                                  <span className="text-[8px] font-extrabold bg-white/20 text-white px-1 py-0.5 rounded-full">NOW</span>
                                )}
                                {m.status === 'out_of_station' && (
                                  <PauseCircle size={10} className="text-orange-400" />
                                )}
                              </div>
                            )
                          })}
                        </div>
                      </div>
                    ) : (
                      <div className="mt-3">
                        <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-2">Assigned To</p>
                        {assignee && (
                          <div className={`inline-flex items-center gap-2 px-3 py-2 rounded-xl border-2 ${
                            isOneTimeDone
                              ? 'bg-teal-50 dark:bg-teal-950/30 border-teal-200 dark:border-teal-800 text-teal-700 dark:text-teal-300'
                              : 'bg-primary border-primary text-white shadow-sm'
                          }`}>
                            <span className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${
                              isOneTimeDone ? 'bg-teal-200 dark:bg-teal-800 text-teal-700' : 'bg-white/20 text-white'
                            }`}>
                              {assignee.nickname.charAt(0)}
                            </span>
                            <span className="text-sm font-semibold">{assignee.nickname}</span>
                            {isOneTimeDone
                              ? <span className="text-[10px] font-bold bg-teal-200 dark:bg-teal-800 text-teal-700 px-1.5 py-0.5 rounded-full">DONE</span>
                              : <span className="text-[10px] font-bold bg-white/20 text-white px-1.5 py-0.5 rounded-full">PENDING</span>
                            }
                          </div>
                        )}
                      </div>
                    )}

                    <p className="text-[11px] text-muted-foreground mt-3">
                      Last done: <span className="font-semibold text-foreground">{dateInfo.lastCompletedFormatted}</span>
                    </p>
                  </div>
                )}
              </div>
            )
          })}

          {tasks.length === 0 && (
            <div className="flex flex-col items-center justify-center py-16 text-center text-muted-foreground">
              <ClipboardList size={44} className="mb-4 opacity-25" />
              <p className="text-base font-bold">No tasks yet</p>
              <p className="text-sm mt-1">Your admin hasn&apos;t created any tasks yet.</p>
            </div>
          )}
        </div>

        {/* Swap Requests Bottom Sheet */}
        {showSwapSheet && (
          <div className="fixed inset-0 z-50 flex items-end justify-center" onClick={() => setShowSwapSheet(false)}>
            <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" />
            <div
              className="relative w-full max-w-lg bg-card rounded-t-2xl border border-border shadow-2xl max-h-[80vh] flex flex-col"
              onClick={e => e.stopPropagation()}
            >
              <div className="flex justify-center pt-3 pb-1 shrink-0">
                <div className="w-10 h-1 rounded-full bg-border" />
              </div>
              <div className="flex items-center justify-between px-5 py-3 shrink-0">
                <div className="flex items-center gap-2">
                  <Repeat2 size={16} className="text-violet-500" />
                  <h2 className="text-base font-bold">Swap Requests</h2>
                  {pendingSwapCount > 0 && (
                    <span className="text-[10px] font-extrabold bg-violet-500 text-white px-1.5 py-0.5 rounded-full">{pendingSwapCount}</span>
                  )}
                </div>
                <button onClick={() => setShowSwapSheet(false)} className="p-1.5 rounded-lg text-muted-foreground hover:text-foreground hover:bg-secondary">
                  <X size={16} />
                </button>
              </div>

              <div className="overflow-y-auto px-5 pb-8 space-y-5 flex-1">
                {/* Received */}
                {myReceived.length > 0 && (
                  <div>
                    <div className="flex items-center gap-2 mb-2">
                      <ArrowDownLeft size={13} className="text-violet-500" />
                      <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">Received</p>
                      <span className="text-[10px] font-bold bg-secondary text-muted-foreground px-1.5 py-0.5 rounded-full">{myReceived.length}</span>
                    </div>
                    <div className="space-y-2">
                      {myReceived.map(req => {
                        const task     = tasks.find(t => t.taskId === req.taskId)
                        const fromUser = members.find(m => m.uid === req.fromUserId)
                        return (
                          <div key={req.id} className={`flex items-center gap-3 p-3 rounded-xl border ${
                            req.status === 'pending'
                              ? 'border-violet-300 dark:border-violet-700 bg-violet-50 dark:bg-violet-950/30'
                              : 'border-border bg-card'
                          }`}>
                            <div className="w-8 h-8 rounded-full bg-violet-100 dark:bg-violet-900/30 flex items-center justify-center text-sm font-bold text-violet-600 dark:text-violet-400 shrink-0">
                              {fromUser?.nickname?.charAt(0) ?? '?'}
                            </div>
                            <div className="flex-1 min-w-0">
                              <p className="text-sm font-semibold truncate">{fromUser?.nickname ?? 'Someone'}</p>
                              <p className="text-[11px] text-muted-foreground truncate">{task?.name ?? 'Unknown task'}</p>
                            </div>
                            {req.status === 'pending' ? (
                              <div className="flex gap-1.5 shrink-0">
                                <button
                                  onClick={e => { e.stopPropagation(); resolveSwapRequest(req.id, 'accepted') }}
                                  className="w-8 h-8 rounded-full bg-green-100 dark:bg-green-900/30 flex items-center justify-center text-green-600 dark:text-green-400 hover:bg-green-200 dark:hover:bg-green-900/50 transition-colors"
                                >
                                  <Check size={14} />
                                </button>
                                <button
                                  onClick={e => { e.stopPropagation(); resolveSwapRequest(req.id, 'rejected') }}
                                  className="w-8 h-8 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center text-red-600 dark:text-red-400 hover:bg-red-200 dark:hover:bg-red-900/50 transition-colors"
                                >
                                  <X size={14} />
                                </button>
                              </div>
                            ) : (
                              <span className={`text-[11px] font-bold px-2 py-0.5 rounded-full shrink-0 ${
                                req.status === 'accepted'
                                  ? 'bg-green-100 text-green-700 dark:bg-green-900/40 dark:text-green-300'
                                  : 'bg-red-100 text-red-700 dark:bg-red-900/40 dark:text-red-300'
                              }`}>
                                {req.status === 'accepted' ? 'Accepted' : 'Declined'}
                              </span>
                            )}
                          </div>
                        )
                      })}
                    </div>
                  </div>
                )}

                {/* Sent */}
                {mySent.length > 0 && (
                  <div>
                    <div className="flex items-center gap-2 mb-2">
                      <ArrowUpRight size={13} className="text-blue-500" />
                      <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">Sent</p>
                      <span className="text-[10px] font-bold bg-secondary text-muted-foreground px-1.5 py-0.5 rounded-full">{mySent.length}</span>
                    </div>
                    <div className="space-y-2">
                      {mySent.map(req => {
                        const task   = tasks.find(t => t.taskId === req.taskId)
                        const toUser = members.find(m => m.uid === req.toUserId)
                        return (
                          <div key={req.id} className="flex items-center gap-3 p-3 rounded-xl border border-border bg-card">
                            <div className="w-8 h-8 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center text-sm font-bold text-blue-600 dark:text-blue-400 shrink-0">
                              {toUser?.nickname?.charAt(0) ?? '?'}
                            </div>
                            <div className="flex-1 min-w-0">
                              <p className="text-sm font-semibold truncate">→ {toUser?.nickname ?? 'Someone'}</p>
                              <p className="text-[11px] text-muted-foreground truncate">{task?.name ?? 'Unknown task'}</p>
                            </div>
                            <span className={`text-[11px] font-bold px-2 py-0.5 rounded-full shrink-0 ${
                              req.status === 'pending'  ? 'bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300' :
                              req.status === 'accepted' ? 'bg-green-100 text-green-700 dark:bg-green-900/40 dark:text-green-300' :
                                                          'bg-red-100 text-red-700 dark:bg-red-900/40 dark:text-red-300'
                            }`}>
                              {req.status === 'pending' ? 'Pending' : req.status === 'accepted' ? 'Accepted' : 'Declined'}
                            </span>
                          </div>
                        )
                      })}
                    </div>
                  </div>
                )}

                {/* Empty */}
                {myReceived.length === 0 && mySent.length === 0 && (
                  <div className="flex flex-col items-center justify-center py-12 text-center text-muted-foreground">
                    <Inbox size={36} className="mb-3 opacity-25" />
                    <p className="font-bold">No swap history yet</p>
                    <p className="text-sm mt-1 opacity-70">Swap requests will appear here.</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

      </div>
    )
  }

  // ── Admin view ──────────────────────────────────────────────────────────────
  return (
    <div className="space-y-6 max-w-5xl">

      {/* ── Page header ───────────────────────────────────────────────── */}
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-0.5">Admin</p>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">Tasks & Rotation</h1>
          <p className="text-muted-foreground mt-0.5 text-sm hidden sm:block">
            Define duties, manage queues, and override assignments.
          </p>
        </div>

        <div className="flex items-center gap-2 shrink-0 mt-1">
          {overdueCount > 0 && (
            <div className="flex items-center gap-1.5 bg-red-100 dark:bg-red-900/30 border border-red-200 dark:border-red-800 rounded-xl px-2.5 py-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-red-500 animate-pulse" />
              <span className="text-[11px] font-bold text-red-600 dark:text-red-400">
                {overdueCount}<span className="hidden sm:inline"> overdue</span>
              </span>
            </div>
          )}
          <Button
            onClick={() => {
              if (isCreating) { setIsCreating(false); setSelectedMembers([]) }
              else if (isGroupCreating) { setIsGroupCreating(false) }
              else if (isTempCreating) { setIsTempCreating(false) }
              else if (!can('create_task')) { setShowUpsell(true) }
              else { setShowTypeSelector(true) }
            }}
            variant={isCreating || isGroupCreating || isTempCreating ? 'outline' : 'default'}
            size="sm"
            className="font-semibold"
          >
            {isCreating || isGroupCreating || isTempCreating
              ? <><X size={14} className="mr-1" />Cancel</>
              : <><Plus size={14} className="mr-1" />New Task</>}
          </Button>
        </div>
      </div>

      {/* ── Mobile-only: Swap Requests shortcut ──────────────────────────── */}
      <Link
        href="/dashboard/swaps"
        className="md:hidden flex items-center gap-3 px-4 py-3.5 rounded-2xl border transition-all active:scale-[0.98] bg-violet-50 dark:bg-violet-950/20 border-violet-200 dark:border-violet-900/40"
      >
        <div className="w-9 h-9 rounded-full bg-violet-100 dark:bg-violet-900/30 flex items-center justify-center shrink-0">
          <Repeat2 size={18} className="text-violet-600 dark:text-violet-400" />
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-bold text-foreground">Swap Requests</p>
          <p className="text-[11px] text-muted-foreground">
            {pendingSwapCount > 0
              ? `${pendingSwapCount} pending swap${pendingSwapCount > 1 ? 's' : ''} — tap to view`
              : 'No pending swaps right now'}
          </p>
        </div>
        {pendingSwapCount > 0 && (
          <span className="bg-violet-500 text-white text-[10px] font-extrabold px-2 py-0.5 rounded-full shrink-0">
            {pendingSwapCount}
          </span>
        )}
        <ArrowRight size={15} className="text-muted-foreground shrink-0" />
      </Link>

      {/* ── Create form ───────────────────────────────────────────────── */}
      {isCreating && (
        <Card className="border-primary/30 shadow-md">
          <CardHeader className="border-b border-border/60 pb-4 px-4 sm:px-5">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-xl bg-primary/10 flex items-center justify-center shrink-0">
                <Plus size={16} className="text-primary" />
              </div>
              <div>
                <CardTitle className="text-sm font-bold">Create New Task</CardTitle>
                <CardDescription className="text-xs mt-0.5">Define a duty and its rotation cycle.</CardDescription>
              </div>
            </div>
          </CardHeader>

          <CardContent className="p-4 sm:p-5 space-y-4">

            {/* Row 1 — Name spans full width; Type in second col on mobile */}
            <div className="grid grid-cols-2 sm:grid-cols-2 lg:grid-cols-4 gap-3">

              {/* Task Name — spans 2 cols on mobile */}
              <div className="col-span-2 lg:col-span-1 space-y-1">
                <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Name</label>
                <input
                  type="text"
                  value={newTaskName}
                  onChange={e => setNewTaskName(e.target.value)}
                  placeholder="e.g., Clean Balcony"
                  className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
                />
              </div>

              {/* Type */}
              <div className="space-y-1">
                <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Type</label>
                <select
                  value={newTaskType}
                  onChange={e => setNewTaskType(e.target.value)}
                  className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm"
                >
                  <option value="cleaning">🧹 Cleaning</option>
                  <option value="garbage">🗑️ Garbage</option>
                  <option value="kitchen">🍳 Kitchen</option>
                  <option value="groceries">🛒 Groceries</option>
                  <option value="laundry">👕 Laundry</option>
                  <option value="maintenance">🔧 Maintenance</option>
                  <option value="other">📋 Other</option>
                </select>
              </div>

              {/* Frequency */}
              <div className="space-y-1">
                <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Frequency</label>
                <select
                  value={newTaskFreq}
                  onChange={e => setNewTaskFreq(e.target.value as any)}
                  className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm"
                >
                  <option value="daily">Daily</option>
                  <option value="weekly">Weekly</option>
                  <option value="monthly">Monthly</option>
                </select>
              </div>

              {/* Priority */}
              <div className="space-y-1">
                <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Priority</label>
                <select
                  value={newTaskPriority}
                  onChange={e => setNewTaskPriority(e.target.value as any)}
                  className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm"
                >
                  <option value="high">🔴 High</option>
                  <option value="medium">🟡 Medium</option>
                  <option value="low">🟢 Low</option>
                </select>
              </div>
            </div>

            {/* ── Custom Start Date ─────────────────────────────────── */}
            <div className="rounded-xl border border-border/60 bg-secondary/30 overflow-hidden">
              <div className="flex items-center justify-between gap-3 px-4 py-3">
                <div className="flex items-center gap-2 min-w-0">
                  <CalendarDays size={14} className="text-primary shrink-0" />
                  <div className="min-w-0">
                    <p className="text-xs font-bold text-foreground leading-tight">Custom Start Date</p>
                    <p className="text-[11px] text-muted-foreground leading-tight mt-0.5 truncate">
                      {useCustomDate ? 'Due date calculated from chosen date' : 'Cycle begins today by default'}
                    </p>
                  </div>
                </div>
                <button type="button" onClick={() => setUseCustomDate(v => !v)}
                  className={`relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors focus:outline-none ${useCustomDate ? 'bg-primary' : 'bg-border'}`}
                  aria-checked={useCustomDate} role="switch">
                  <span className={`inline-block h-4 w-4 transform rounded-full bg-white shadow transition-transform ${useCustomDate ? 'translate-x-6' : 'translate-x-1'}`} />
                </button>
              </div>
              {useCustomDate && (
                <div className="border-t border-border/60 px-4 py-3 space-y-2">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">When was it last done / started?</label>
                  <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                    <input type="date" value={newTaskStartDate}
                      max={new Date().toISOString().split('T')[0]}
                      onChange={e => setNewTaskStartDate(e.target.value)}
                      className="w-full sm:w-auto bg-background border border-border rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
                    {customDuePreview && (
                      <p className="text-xs text-muted-foreground">
                        Next due: <span className="font-semibold text-foreground">{customDuePreview}</span>
                      </p>
                    )}
                  </div>
                </div>
              )}
            </div>

            {/* Rotation Participants */}
            <div className="space-y-2">
              <div className="flex items-center justify-between gap-2">
                <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Rotation Participants</label>
                <span className="text-[11px] text-muted-foreground">
                  {selectedMembers.length === activeMembers.length
                    ? 'All members included — tap to exclude'
                    : `${selectedMembers.length} of ${activeMembers.length} selected`}
                </span>
              </div>
              <div className="flex flex-wrap gap-2">
                {activeMembers.map(m => (
                  <button key={m.uid} onClick={() => toggleMember(m.uid)}
                    className={`flex items-center gap-1.5 px-3 py-2 rounded-xl text-sm font-semibold transition-all border ${
                      selectedMembers.includes(m.uid)
                        ? 'bg-primary text-white border-primary shadow-sm'
                        : 'bg-background text-muted-foreground border-border hover:bg-secondary line-through opacity-60'
                    }`}>
                    <span className={`w-5 h-5 rounded-full flex items-center justify-center text-[11px] font-bold shrink-0 ${
                      selectedMembers.includes(m.uid) ? 'bg-white/20 text-white' : 'bg-secondary text-muted-foreground'
                    }`}>{m.nickname.charAt(0)}</span>
                    {m.nickname}
                    {selectedMembers.includes(m.uid) && (
                      <span className="w-4 h-4 rounded-full bg-white/20 flex items-center justify-center text-[10px] font-bold shrink-0">
                        {selectedMembers.indexOf(m.uid) + 1}
                      </span>
                    )}
                  </button>
                ))}
              </div>
              {selectedMembers.length > 0 && (
                <p className="text-xs text-muted-foreground">
                  Order: {selectedMembers.map(uid => members.find(m => m.uid === uid)?.nickname).filter(Boolean).join(' → ')}
                </p>
              )}
            </div>

            <Button onClick={handleCreate} disabled={!newTaskName.trim() || selectedMembers.length === 0} className="w-full font-bold">
              <ClipboardList size={15} className="mr-2" />
              Create Task & Start Rotation
            </Button>
          </CardContent>
        </Card>
      )}

      {/* ── Type selector modal ──────────────────────────────────────── */}
      {showTypeSelector && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
          <div className="bg-card border border-border rounded-2xl shadow-xl w-full max-w-md p-6">
            <div className="flex items-start justify-between mb-5">
              <div>
                <h3 className="font-bold text-lg">What type of task?</h3>
                <p className="text-sm text-muted-foreground mt-0.5">Choose how this task should work.</p>
              </div>
              <button onClick={() => setShowTypeSelector(false)} className="text-muted-foreground hover:text-foreground p-1 rounded-md hover:bg-secondary">
                <X size={18} />
              </button>
            </div>
            <div className="space-y-3">

              {/* Option 1 — Recurring Duty */}
              <button
                onClick={() => { setShowTypeSelector(false); setSelectedMembers(activeMembers.map(m => m.uid)); setIsCreating(true) }}
                className="w-full flex items-start gap-4 p-4 rounded-xl border-2 border-border hover:border-primary/50 hover:bg-primary/5 text-left transition-all group"
              >
                <div className="w-11 h-11 rounded-xl bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center shrink-0">
                  <Repeat2 size={20} className="text-blue-600 dark:text-blue-400" />
                </div>
                <div className="flex-1 min-w-0 pt-0.5">
                  <p className="font-bold text-sm group-hover:text-primary transition-colors">Recurring Duty</p>
                  <p className="text-xs text-muted-foreground mt-1 leading-relaxed">
                    Rotates between members — daily, weekly, or monthly. Skips people who are out of station automatically.
                  </p>
                  <p className="text-[11px] text-blue-600 dark:text-blue-400 font-semibold mt-1.5">
                    e.g. Garbage duty → Sai this week, Rahul next week
                  </p>
                </div>
              </button>

              {/* Option 2 — Group Task */}
              <button
                onClick={() => { setShowTypeSelector(false); setIsGroupCreating(true) }}
                className="w-full flex items-start gap-4 p-4 rounded-xl border-2 border-border hover:border-teal-500/50 hover:bg-teal-50/50 dark:hover:bg-teal-950/20 text-left transition-all group"
              >
                <div className="w-11 h-11 rounded-xl bg-teal-100 dark:bg-teal-900/30 flex items-center justify-center shrink-0">
                  <Users size={20} className="text-teal-600 dark:text-teal-400" />
                </div>
                <div className="flex-1 min-w-0 pt-0.5">
                  <p className="font-bold text-sm group-hover:text-teal-600 dark:group-hover:text-teal-400 transition-colors">Group Task</p>
                  <p className="text-xs text-muted-foreground mt-1 leading-relaxed">
                    Split one job across people — each gets their own part. Can be one-time or repeat every week/month.
                  </p>
                  <p className="text-[11px] text-teal-600 dark:text-teal-400 font-semibold mt-1.5">
                    e.g. Sunday Cleaning → Sai: kitchen · Rahul: bedroom · Priya: hall
                  </p>
                </div>
              </button>

              {/* Option 3 — Temp Task */}
              <button
                onClick={() => { setShowTypeSelector(false); setIsTempCreating(true) }}
                className="w-full flex items-start gap-4 p-4 rounded-xl border-2 border-border hover:border-amber-500/50 hover:bg-amber-50/50 dark:hover:bg-amber-950/20 text-left transition-all group"
              >
                <div className="w-11 h-11 rounded-xl bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center shrink-0">
                  <Zap size={20} className="text-amber-600 dark:text-amber-400" />
                </div>
                <div className="flex-1 min-w-0 pt-0.5">
                  <p className="font-bold text-sm group-hover:text-amber-600 dark:group-hover:text-amber-400 transition-colors">Temp Task</p>
                  <p className="text-xs text-muted-foreground mt-1 leading-relaxed">
                    Assign one quick job to one person. When they mark it done, it&apos;s closed — no rotation, no repeat.
                  </p>
                  <p className="text-[11px] text-amber-600 dark:text-amber-400 font-semibold mt-1.5">
                    e.g. Buy vegetables today → Sai → done ✓
                  </p>
                </div>
              </button>

            </div>
          </div>
        </div>
      )}

      {/* ── Temp Task form ────────────────────────────────────────────── */}
      {isTempCreating && (
        <Card className="border-amber-300 dark:border-amber-700 shadow-md">
          <CardHeader className="border-b border-border/60 pb-4 px-4 sm:px-5">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-xl bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center shrink-0">
                <Zap size={16} className="text-amber-600 dark:text-amber-400" />
              </div>
              <div>
                <CardTitle className="text-sm font-bold">Temp Task</CardTitle>
                <CardDescription className="text-xs mt-0.5">
                  One person, one job. Done once and closed — no rotation, no repeat.
                </CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent className="p-4 sm:p-5 space-y-4">
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
              <div className="col-span-2 space-y-1">
                <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Task</label>
                <input
                  autoFocus
                  type="text"
                  value={tempTaskName}
                  onChange={e => setTempTaskName(e.target.value)}
                  placeholder="e.g., Buy vegetables"
                  maxLength={50}
                  className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-amber-500/30"
                />
              </div>
              <div className="space-y-1">
                <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Type</label>
                <select value={tempTaskType} onChange={e => setTempTaskType(e.target.value)}
                  className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm">
                  <option value="other">📋 Other</option>
                  <option value="groceries">🛒 Groceries</option>
                  <option value="cleaning">🧹 Cleaning</option>
                  <option value="kitchen">🍳 Kitchen</option>
                  <option value="garbage">🗑️ Garbage</option>
                  <option value="laundry">👕 Laundry</option>
                  <option value="maintenance">🔧 Maintenance</option>
                </select>
              </div>
              <div className="space-y-1">
                <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Priority</label>
                <select value={tempTaskPriority} onChange={e => setTempTaskPriority(e.target.value as 'high' | 'medium' | 'low')}
                  className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm">
                  <option value="high">🔴 High</option>
                  <option value="medium">🟡 Medium</option>
                  <option value="low">🟢 Low</option>
                </select>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div className="space-y-1">
                <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Due by</label>
                <input type="date" value={tempTaskDueDate}
                  min={new Date().toISOString().split('T')[0]}
                  onChange={e => setTempTaskDueDate(e.target.value)}
                  className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm" />
              </div>
              <div className="space-y-2">
                <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Assign to</label>
                <div className="flex flex-wrap gap-2">
                  {activeMembers.map(m => (
                    <button key={m.uid} onClick={() => setTempTaskAssignee(m.uid)}
                      className={`flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold transition-all border ${
                        tempTaskAssignee === m.uid
                          ? 'bg-amber-500 text-white border-amber-500 shadow-sm'
                          : 'bg-background text-muted-foreground border-border hover:bg-secondary'
                      }`}>
                      <span className={`w-5 h-5 rounded-full flex items-center justify-center text-[11px] font-bold shrink-0 ${
                        tempTaskAssignee === m.uid ? 'bg-white/20 text-white' : 'bg-secondary text-foreground'
                      }`}>{m.nickname.charAt(0)}</span>
                      {m.nickname}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            <Button
              onClick={handleCreateTempTask}
              disabled={!tempTaskName.trim() || !tempTaskAssignee}
              className="w-full font-bold bg-amber-500 hover:bg-amber-600 text-white"
            >
              <Zap size={15} className="mr-2" />
              Assign Task
            </Button>
          </CardContent>
        </Card>
      )}

      {/* ── Group Task form ───────────────────────────────────────────── */}
      {isGroupCreating && (
        <Card className="border-teal-300 dark:border-teal-700 shadow-md">
          <CardHeader className="border-b border-border/60 pb-4 px-4 sm:px-5">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-xl bg-teal-100 dark:bg-teal-900/30 flex items-center justify-center shrink-0">
                <Users size={16} className="text-teal-600 dark:text-teal-400" />
              </div>
              <div>
                <CardTitle className="text-sm font-bold">Create Group Task</CardTitle>
                <CardDescription className="text-xs mt-0.5">
                  Assign different jobs to different people — all due at the same time.
                </CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent className="p-4 sm:p-5 space-y-4">

            {/* One-time / Recurring toggle */}
            <div className="flex items-center gap-3">
              <div className="flex items-center bg-secondary/80 border border-border/60 rounded-xl p-1 gap-0.5">
                <button onClick={() => setGroupIsRecurring(false)}
                  className={`px-3 py-1.5 rounded-lg text-sm font-semibold transition-all ${
                    !groupIsRecurring ? 'bg-card shadow-sm text-foreground' : 'text-muted-foreground hover:text-foreground'
                  }`}>
                  One-time
                </button>
                <button onClick={() => setGroupIsRecurring(true)}
                  className={`px-3 py-1.5 rounded-lg text-sm font-semibold transition-all ${
                    groupIsRecurring ? 'bg-card shadow-sm text-foreground' : 'text-muted-foreground hover:text-foreground'
                  }`}>
                  Recurring
                </button>
              </div>
              {groupIsRecurring && (
                <p className="text-[11px] text-muted-foreground">
                  Repeats on schedule. If someone is out of station, their job moves to the next available person.
                </p>
              )}
            </div>

            {/* Group name + date/frequency + priority */}
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
              <div className="col-span-2 sm:col-span-1 space-y-1">
                <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Group Name</label>
                <input
                  type="text"
                  value={groupTaskName}
                  onChange={e => setGroupTaskName(e.target.value)}
                  placeholder="e.g., Sunday Cleaning"
                  maxLength={50}
                  className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500/30"
                />
              </div>
              {groupIsRecurring ? (
                <>
                  <div className="space-y-1">
                    <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Frequency</label>
                    <select value={groupFreq} onChange={e => setGroupFreq(e.target.value as 'daily' | 'weekly' | 'monthly')}
                      className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm">
                      <option value="daily">Daily</option>
                      <option value="weekly">Weekly</option>
                      <option value="monthly">Monthly</option>
                    </select>
                  </div>
                  <div className="space-y-1">
                    <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Start Date</label>
                    <input type="date" value={groupStartDate}
                      onChange={e => setGroupStartDate(e.target.value)}
                      className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm" />
                  </div>
                </>
              ) : (
                <>
                  <div className="space-y-1">
                    <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Due Date</label>
                    <input type="date" value={groupTaskDueDate}
                      min={new Date().toISOString().split('T')[0]}
                      onChange={e => setGroupTaskDueDate(e.target.value)}
                      className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm" />
                  </div>
                  <div className="space-y-1">
                    <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Priority</label>
                    <select value={groupTaskPriority} onChange={e => setGroupTaskPriority(e.target.value as 'high' | 'medium' | 'low')}
                      className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm">
                      <option value="high">🔴 High</option>
                      <option value="medium">🟡 Medium</option>
                      <option value="low">🟢 Low</option>
                    </select>
                  </div>
                </>
              )}
              {groupIsRecurring && (
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">Priority</label>
                  <select value={groupTaskPriority} onChange={e => setGroupTaskPriority(e.target.value as 'high' | 'medium' | 'low')}
                    className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm">
                    <option value="high">🔴 High</option>
                    <option value="medium">🟡 Medium</option>
                    <option value="low">🟢 Low</option>
                  </select>
                </div>
              )}
            </div>

            {/* Sub-tasks */}
            <div className="space-y-2.5">
              <div>
                <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">
                  Jobs — who does what
                </label>
                <p className="text-[11px] text-muted-foreground mt-0.5">
                  Each row is one person&apos;s job. All are due on the date above.
                </p>
              </div>
              <div className="space-y-2">
                {groupSubtasks.map((sub, idx) => (
                  <div key={sub.id} className="flex items-center gap-2">
                    <span className="text-xs font-bold text-muted-foreground w-5 text-right shrink-0">{idx + 1}.</span>
                    <input
                      type="text"
                      value={sub.name}
                      onChange={e => setGroupSubtasks(prev => prev.map(s => s.id === sub.id ? { ...s, name: e.target.value } : s))}
                      placeholder="What needs doing? (e.g., Clean Kitchen)"
                      maxLength={50}
                      className="flex-1 bg-background border border-border rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500/30"
                    />
                    <select
                      value={sub.assigneeId}
                      onChange={e => setGroupSubtasks(prev => prev.map(s => s.id === sub.id ? { ...s, assigneeId: e.target.value } : s))}
                      className="w-28 sm:w-36 bg-background border border-border rounded-xl px-2 py-2 text-sm shrink-0"
                    >
                      <option value="" disabled>Who?</option>
                      {activeMembers.map(m => (
                        <option key={m.uid} value={m.uid}>{m.nickname}</option>
                      ))}
                    </select>
                    {groupSubtasks.length > 1 && (
                      <button
                        onClick={() => setGroupSubtasks(prev => prev.filter(s => s.id !== sub.id))}
                        className="p-1.5 text-muted-foreground hover:text-destructive hover:bg-destructive/10 rounded-lg transition-colors shrink-0"
                      >
                        <X size={14} />
                      </button>
                    )}
                  </div>
                ))}
              </div>
              <button
                onClick={() => setGroupSubtasks(prev => [...prev, { id: crypto.randomUUID(), name: '', assigneeId: '' }])}
                className="flex items-center gap-1.5 text-sm font-semibold text-teal-600 dark:text-teal-400 hover:text-teal-700 dark:hover:text-teal-300 transition-colors py-1"
              >
                <Plus size={14} /> Add another job
              </button>
            </div>

            <Button
              onClick={handleCreateGroupTask}
              disabled={!groupTaskName.trim() || groupSubtasks.filter(s => s.name.trim() && s.assigneeId).length === 0}
              className="w-full font-bold bg-teal-600 hover:bg-teal-700 text-white"
            >
              <Users size={15} className="mr-2" />
              Create Group Task ({groupSubtasks.filter(s => s.name.trim() && s.assigneeId).length} {groupSubtasks.filter(s => s.name.trim() && s.assigneeId).length === 1 ? 'job' : 'jobs'})
            </Button>
          </CardContent>
        </Card>
      )}

      {/* ── Task cards ────────────────────────────────────────────────── */}
      <div className="space-y-3">
        {sortedTasks.map(task => {
          const isOverriding = overridingTaskId === task.taskId
          const dateInfo     = getTaskDateInfo(task)
          const pCfg         = PRIORITY_CONFIG[task.priority] ?? PRIORITY_CONFIG.medium
          const fCfg         = FREQ_CONFIG[task.frequency as keyof typeof FREQ_CONFIG] ?? FREQ_CONFIG.weekly
          const emoji        = TASK_EMOJIS[task.type] ?? '📋'

          const isOneTimeDone = task.frequency === 'one_time' && task.status === 'completed'

          return (
            <Card key={task.taskId} className={`shadow-sm overflow-hidden border-l-4 ${pCfg.border} ${isOneTimeDone ? 'opacity-60' : ''}`}>
              <CardContent className="p-0">

                {/* ── Card header ──────────────────────────────────────── */}
                <div className="px-4 py-3 border-b border-border/60">

                  {/* Top row: emoji + name + action buttons */}
                  <div className="flex items-start gap-3">
                    <span className="text-xl shrink-0 mt-0.5">{emoji}</span>

                    {/* Name + status badges */}
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-1.5 flex-wrap">
                        <h3 className="text-base font-bold leading-tight truncate">{task.name}</h3>
                        {isOneTimeDone && (
                          <span className="inline-flex items-center gap-1 text-[10px] font-bold bg-teal-100 dark:bg-teal-900/40 text-teal-700 dark:text-teal-400 px-1.5 py-0.5 rounded-full whitespace-nowrap shrink-0">
                            <CheckCircle2 size={9} /> Done
                          </span>
                        )}
                        {task.status === 'overdue' && !isOneTimeDone && (
                          <span className="inline-flex items-center gap-1 text-[10px] font-bold bg-red-100 dark:bg-red-900/40 text-red-600 dark:text-red-400 px-1.5 py-0.5 rounded-full whitespace-nowrap shrink-0">
                            <AlertTriangle size={9} /> {dateInfo.overdueDays}d overdue
                          </span>
                        )}
                        {task.status === 'paused' && (
                          <span className="inline-flex items-center gap-1 text-[10px] font-bold bg-secondary text-muted-foreground px-1.5 py-0.5 rounded-full shrink-0">
                            <PauseCircle size={9} /> Paused
                          </span>
                        )}
                      </div>

                      {/* Frequency · Priority · Due */}
                      <div className="flex items-center gap-1.5 mt-1.5 flex-wrap">
                        <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${fCfg.bg} ${fCfg.text}`}>
                          {fCfg.label}
                        </span>
                        {task.groupName && (
                          <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-teal-100 dark:bg-teal-900/30 text-teal-700 dark:text-teal-400 flex items-center gap-1">
                            <Users size={8} /> {task.groupName}
                          </span>
                        )}
                        <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${pCfg.bg} ${pCfg.text}`}>
                          ● {pCfg.label}
                        </span>
                        <span className="text-[11px] text-muted-foreground flex items-center gap-1">
                          <Clock size={10} />
                          <span>Due {dateInfo.dueDateFormatted}</span>
                        </span>
                      </div>
                    </div>

                    {/* Action buttons — icon on mobile, icon+text on sm+ */}
                    <div className="flex gap-1.5 shrink-0">
                      {/* Edit task */}
                      <Button
                        variant="outline"
                        size="sm"
                        className="border-border/60 font-semibold text-xs h-8 px-2 sm:px-3"
                        onClick={() => {
                          setEditingTaskId(task.taskId)
                          setEditName(task.name)
                          setEditType(task.type)
                          setEditPriority(task.priority)
                          setEditFreq(task.frequency as 'daily' | 'weekly' | 'monthly')
                          setEditDueDate(task.dueDate.split('T')[0])
                        }}
                        title="Edit task"
                      >
                        <Edit2 size={12} className="shrink-0" />
                        <span className="hidden sm:inline ml-1.5">Edit</span>
                      </Button>
                      {/* Override assignee */}
                      <Button
                        variant="outline"
                        size="sm"
                        className="border-border/60 font-semibold text-xs h-8 px-2 sm:px-3"
                        onClick={() => setOverridingTaskId(isOverriding ? null : task.taskId)}
                      >
                        <span className="hidden sm:inline">{isOverriding ? 'Cancel' : 'Override'}</span>
                        <span className="sm:hidden">⇄</span>
                      </Button>
                      <Button
                        variant="outline"
                        size="icon"
                        className="h-8 w-8 border-border/60 text-destructive hover:bg-destructive/10 hover:border-destructive/30 shrink-0"
                        onClick={() => handleDelete(task.taskId, task.name)}
                        title="Delete task"
                      >
                        <Trash2 size={13} />
                      </Button>
                    </div>
                  </div>
                </div>

                {/* ── Body: Queue + override ───────────────────────────── */}
                <div className="px-4 py-3 flex flex-col lg:flex-row gap-4">

                  {/* Queue / Assignment display */}
                  <div className="flex-1 min-w-0">
                    <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-2">
                      {task.frequency === 'one_time' ? 'Assigned To' : 'Rotation Queue'}
                    </p>

                    {task.frequency === 'one_time' ? (
                      /* One-time: show just the single assigned person */
                      (() => {
                        const m = members.find(x => x.uid === task.currentAssignedUserId)
                        if (!m) return null
                        return (
                          <div className={`inline-flex items-center gap-2.5 px-3 py-2.5 rounded-xl border-2 ${
                            isOneTimeDone
                              ? 'bg-teal-50 dark:bg-teal-950/30 border-teal-200 dark:border-teal-800 text-teal-700 dark:text-teal-300'
                              : 'bg-primary border-primary text-white shadow-md'
                          }`}>
                            <div className={`w-7 h-7 rounded-full flex items-center justify-center text-sm font-bold shrink-0 ${
                              isOneTimeDone ? 'bg-teal-200 dark:bg-teal-800 text-teal-700 dark:text-teal-300' : 'bg-white/20 text-white'
                            }`}>
                              {m.nickname.charAt(0)}
                            </div>
                            <span className="font-semibold text-sm">{m.nickname}</span>
                            {isOneTimeDone
                              ? <span className="text-[10px] font-bold bg-teal-200 dark:bg-teal-800 text-teal-700 dark:text-teal-300 px-1.5 py-0.5 rounded-full">DONE</span>
                              : <span className="text-[10px] font-bold bg-white/20 text-white px-1.5 py-0.5 rounded-full">PENDING</span>
                            }
                          </div>
                        )
                      })()
                    ) : (
                      /* Recurring: show full rotation queue with arrows */
                      <div className="flex flex-col sm:flex-row sm:flex-wrap sm:items-center gap-0">
                        {task.queueOrder.map((uid, index) => {
                          const m         = members.find(x => x.uid === uid)
                          const isCurrent = uid === task.currentAssignedUserId
                          if (!m) return null
                          return (
                            <div key={uid} className="flex flex-col sm:flex-row sm:items-center gap-1 sm:gap-2">
                              <div className={`flex flex-row sm:flex-col items-center gap-2 sm:gap-1 sm:justify-center px-3 sm:px-2.5 py-2 rounded-xl border-2 transition-all w-full sm:w-auto sm:min-w-[62px] ${
                                isCurrent
                                  ? 'bg-primary border-primary text-white shadow-md'
                                  : 'bg-card border-border/60 text-muted-foreground'
                              }`}>
                                <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold shrink-0 ${
                                  isCurrent ? 'bg-white/20 text-white' : 'bg-secondary text-foreground'
                                }`}>
                                  {m.nickname.charAt(0)}
                                </div>
                                <span className="text-sm sm:text-[11px] font-semibold leading-tight">{m.nickname}</span>
                                {m.status === 'out_of_station' && (
                                  <PauseCircle size={10} className="text-orange-400 ml-auto sm:ml-0" />
                                )}
                                {isCurrent && (
                                  <span className="text-[8px] font-bold bg-white/20 text-white px-1.5 py-0.5 rounded-full whitespace-nowrap ml-auto sm:ml-0">
                                    NOW
                                  </span>
                                )}
                              </div>
                              {index < task.queueOrder.length - 1 && (
                                <>
                                  <ArrowDown  size={12} className="sm:hidden text-muted-foreground/40 self-center" />
                                  <ArrowRight size={12} className="hidden sm:block text-muted-foreground/40 shrink-0" />
                                </>
                              )}
                            </div>
                          )
                        })}
                      </div>
                    )}
                    <p className="text-[11px] text-muted-foreground mt-2">
                      Last done: <span className="font-semibold text-foreground">{dateInfo.lastCompletedFormatted}</span>
                    </p>
                  </div>

                  {/* Override panel */}
                  {isOverriding && (
                    <div className="lg:w-60 bg-secondary/40 border border-border/60 rounded-xl p-4 space-y-3">
                      <div className="flex items-center gap-2">
                        <Edit2 size={12} className="text-primary" />
                        <p className="text-xs font-bold uppercase tracking-wider text-primary">Admin Override</p>
                      </div>
                      <select
                        value={selectedAssigneeId}
                        onChange={e => setSelectedAssigneeId(e.target.value)}
                        className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm"
                      >
                        <option value="" disabled>Select member</option>
                        {assignableMembers.map(sub => (
                          <option key={sub.uid} value={sub.uid}>
                            {sub.nickname}
                          </option>
                        ))}
                      </select>
                      <div className="flex gap-2">
                        <Button
                          className="flex-1 font-bold text-xs"
                          onClick={() => handleManualAssign(task.taskId)}
                          disabled={!selectedAssigneeId}
                          size="sm"
                        >
                          <CheckCircle2 size={12} className="mr-1.5" /> Assign
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="flex-1 text-xs"
                          onClick={() => setOverridingTaskId(null)}
                        >
                          Cancel
                        </Button>
                      </div>
                    </div>
                  )}
                </div>

              </CardContent>
            </Card>
          )
        })}

        {tasks.length === 0 && (
          <Card className="border-dashed border-2 border-border/40 bg-transparent shadow-none">
            <CardContent className="flex flex-col items-center justify-center py-16 px-6 text-center text-muted-foreground">
              <ClipboardList size={44} className="mb-4 opacity-25" />
              <p className="text-base font-bold">No Tasks Yet</p>
              <p className="text-sm mt-1">Tap "New Task" above to create your first duty rotation.</p>
            </CardContent>
          </Card>
        )}
      </div>

      {/* ── Edit Task Modal ───────────────────────────────────────────── */}
      {editingTaskId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
          <div className="bg-card border border-border rounded-2xl shadow-xl w-full max-w-md overflow-hidden">
            <div className="flex items-center justify-between px-5 pt-5 pb-4 border-b border-border">
              <h3 className="font-bold text-base">Edit Task</h3>
              <button onClick={() => setEditingTaskId(null)} className="text-muted-foreground hover:text-foreground p-1">
                <X size={16} />
              </button>
            </div>
            <div className="px-5 py-4 space-y-3">
              <div className="space-y-1">
                <label className="text-xs font-bold text-muted-foreground">Task Name</label>
                <input
                  type="text"
                  maxLength={50}
                  value={editName}
                  onChange={e => setEditName(e.target.value)}
                  className="w-full bg-background border border-border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
                />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <label className="text-xs font-bold text-muted-foreground">Type</label>
                  <select value={editType} onChange={e => setEditType(e.target.value)}
                    className="w-full bg-background border border-border rounded-lg px-3 py-2 text-sm">
                    {['cleaning','kitchen','garbage','groceries','laundry','maintenance','other'].map(t => (
                      <option key={t} value={t}>{t.charAt(0).toUpperCase()+t.slice(1)}</option>
                    ))}
                  </select>
                </div>
                <div className="space-y-1">
                  <label className="text-xs font-bold text-muted-foreground">Priority</label>
                  <select value={editPriority} onChange={e => setEditPriority(e.target.value as 'high' | 'medium' | 'low')}
                    className="w-full bg-background border border-border rounded-lg px-3 py-2 text-sm">
                    <option value="high">High</option>
                    <option value="medium">Medium</option>
                    <option value="low">Low</option>
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <label className="text-xs font-bold text-muted-foreground">Frequency</label>
                  <select value={editFreq} onChange={e => setEditFreq(e.target.value as 'daily' | 'weekly' | 'monthly' | 'one_time')}
                    className="w-full bg-background border border-border rounded-lg px-3 py-2 text-sm">
                    <option value="daily">Daily</option>
                    <option value="weekly">Weekly</option>
                    <option value="monthly">Monthly</option>
                    <option value="one_time">One-time</option>
                  </select>
                </div>
                <div className="space-y-1">
                  <label className="text-xs font-bold text-muted-foreground">Due Date</label>
                  <input
                    type="date"
                    value={editDueDate}
                    onChange={e => setEditDueDate(e.target.value)}
                    className="w-full bg-background border border-border rounded-lg px-3 py-2 text-sm"
                  />
                </div>
              </div>
            </div>
            <div className="px-5 pb-5 flex gap-2">
              <Button variant="outline" className="flex-1" onClick={() => setEditingTaskId(null)}>Cancel</Button>
              <Button
                className="flex-1 font-bold"
                disabled={!editName.trim()}
                onClick={async () => {
                  await editTask(editingTaskId, {
                    name: editName.trim(),
                    type: editType,
                    priority: editPriority,
                    frequency: editFreq,
                    dueDate: editDueDate ? new Date(editDueDate).toISOString() : undefined,
                  }, adminId)
                  setEditingTaskId(null)
                }}
              >
                Save Changes
              </Button>
            </div>
          </div>
        </div>
      )}

      {showUpsell && (
        <SubscriptionUpsell feature="create_task" onClose={() => setShowUpsell(false)} />
      )}
    </div>
  )
}
