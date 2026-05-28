"use client"
import { useState } from 'react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  Plus, ArrowRight, ArrowDown, AlertTriangle, PauseCircle, Clock, Edit2,
  Trash2, ClipboardList, CheckCircle2, X, CalendarDays,
} from 'lucide-react'
import { getPriorityWeight, getTaskDateInfo } from '@/lib/rotationEngine'

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
  daily:   { label: 'Daily',   bg: 'bg-orange-100 dark:bg-orange-900/30', text: 'text-orange-700 dark:text-orange-400' },
  weekly:  { label: 'Weekly',  bg: 'bg-blue-100 dark:bg-blue-900/30',    text: 'text-blue-700 dark:text-blue-400' },
  monthly: { label: 'Monthly', bg: 'bg-purple-100 dark:bg-purple-900/30', text: 'text-purple-700 dark:text-purple-400' },
}

export default function TasksPage() {
  const { tasks, members, manuallyAssignTask, createTask, deleteTask } = useFlatStore()
  const { user } = useAuthStore()

  const [selectedAssigneeId, setSelectedAssigneeId]   = useState<string>('')
  const [overridingTaskId, setOverridingTaskId]       = useState<string | null>(null)
  const [isCreating, setIsCreating]                   = useState(false)
  const [newTaskName, setNewTaskName]                 = useState('')
  const [newTaskType, setNewTaskType]                 = useState('cleaning')
  const [newTaskPriority, setNewTaskPriority]         = useState<'high' | 'medium' | 'low'>('medium')
  const [newTaskFreq, setNewTaskFreq]                 = useState<'daily' | 'weekly' | 'monthly'>('weekly')
  const [selectedMembers, setSelectedMembers]         = useState<string[]>([])
  const [useCustomDate, setUseCustomDate]             = useState(false)
  const [newTaskStartDate, setNewTaskStartDate]       = useState(
    new Date().toISOString().split('T')[0]   // today yyyy-MM-dd
  )

  const adminId       = user?.uid || 'u1'
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

    const dueDate = new Date(startDate.getTime() + freqIntervalMs).toISOString()

    createTask({
      name: newTaskName,
      type: newTaskType,
      priority: newTaskPriority,
      frequency: newTaskFreq,
      currentAssignedUserId: selectedMembers[0],
      queueOrder: selectedMembers,
      dueDate,
      lastCompletedAt: startDate.toISOString(),
    }, adminId)

    setIsCreating(false)
    setNewTaskName('')
    setSelectedMembers(activeMembers.map(m => m.uid))  // keep all members ready for next task
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

  const overdueCount = tasks.filter(t => t.status === 'overdue').length
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
              if (!isCreating) {
                // Pre-select ALL active members so every task includes everyone by default
                setSelectedMembers(activeMembers.map(m => m.uid))
              } else {
                setSelectedMembers([])
              }
              setIsCreating(v => !v)
            }}
            variant={isCreating ? 'outline' : 'default'}
            size="sm"
            className="font-semibold"
          >
            {isCreating
              ? <><X size={14} className="mr-1" />Cancel</>
              : <><Plus size={14} className="mr-1" />New Task</>}
          </Button>
        </div>
      </div>

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

            {/* ── Custom Calendar ──────────────────────────────────────── */}
            <div className="rounded-xl border border-border/60 bg-secondary/30 overflow-hidden">
              {/* Toggle row */}
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
                <button
                  type="button"
                  onClick={() => setUseCustomDate(v => !v)}
                  className={`relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors focus:outline-none ${
                    useCustomDate ? 'bg-primary' : 'bg-border'
                  }`}
                  aria-checked={useCustomDate}
                  role="switch"
                >
                  <span className={`inline-block h-4 w-4 transform rounded-full bg-white shadow transition-transform ${
                    useCustomDate ? 'translate-x-6' : 'translate-x-1'
                  }`} />
                </button>
              </div>

              {/* Expanded date picker */}
              {useCustomDate && (
                <div className="border-t border-border/60 px-4 py-3 space-y-2">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">
                    When was it last done / started?
                  </label>
                  <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                    <input
                      type="date"
                      value={newTaskStartDate}
                      max={new Date().toISOString().split('T')[0]}
                      onChange={e => setNewTaskStartDate(e.target.value)}
                      className="w-full sm:w-auto bg-background border border-border rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
                    />
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
                <label className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">
                  Rotation Participants
                </label>
                <span className="text-[11px] text-muted-foreground">
                  {selectedMembers.length === activeMembers.length
                    ? 'All members included — tap to exclude'
                    : `${selectedMembers.length} of ${activeMembers.length} selected`}
                </span>
              </div>

              <div className="flex flex-wrap gap-2">
                {activeMembers.map(m => (
                  <button
                    key={m.uid}
                    onClick={() => toggleMember(m.uid)}
                    className={`flex items-center gap-1.5 px-3 py-2 rounded-xl text-sm font-semibold transition-all border ${
                      selectedMembers.includes(m.uid)
                        ? 'bg-primary text-white border-primary shadow-sm'
                        : 'bg-background text-muted-foreground border-border hover:bg-secondary line-through opacity-60'
                    }`}
                  >
                    <span className={`w-5 h-5 rounded-full flex items-center justify-center text-[11px] font-bold shrink-0 ${
                      selectedMembers.includes(m.uid) ? 'bg-white/20 text-white' : 'bg-secondary text-muted-foreground'
                    }`}>
                      {m.nickname.charAt(0)}
                    </span>
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

            <Button
              onClick={handleCreate}
              disabled={!newTaskName.trim() || selectedMembers.length === 0}
              className="w-full font-bold"
            >
              <ClipboardList size={15} className="mr-2" />
              Create Task & Start Rotation
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

          return (
            <Card key={task.taskId} className={`shadow-sm overflow-hidden border-l-4 ${pCfg.border}`}>
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
                        {task.status === 'overdue' && (
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
                      <Button
                        variant="outline"
                        size="sm"
                        className="border-border/60 font-semibold text-xs h-8 px-2 sm:px-3"
                        onClick={() => setOverridingTaskId(isOverriding ? null : task.taskId)}
                      >
                        <Edit2 size={12} className="shrink-0" />
                        <span className="hidden sm:inline ml-1.5">{isOverriding ? 'Cancel' : 'Override'}</span>
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

                  {/* Rotation queue — horizontal scroll on mobile */}
                  <div className="flex-1 min-w-0">
                    <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-2">
                      Rotation Queue
                    </p>
                    {/* Queue — vertical on mobile, horizontal on sm+ */}
                    <div className="flex flex-col sm:flex-row sm:flex-wrap sm:items-center gap-0">
                      {task.queueOrder.map((uid, index) => {
                        const m         = members.find(x => x.uid === uid)
                        const isCurrent = uid === task.currentAssignedUserId
                        if (!m) return null
                        return (
                          <div key={uid} className="flex flex-col sm:flex-row sm:items-center gap-1 sm:gap-2">
                            {/* Member card — row on mobile, column on sm+ */}
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
                            {/* Arrow: ↓ on mobile, → on desktop */}
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
    </div>
  )
}
