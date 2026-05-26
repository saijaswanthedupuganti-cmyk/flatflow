"use client"
import { useState } from 'react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  Plus, ArrowRight, AlertTriangle, PauseCircle, Clock, Edit2,
  Trash2, ClipboardList, ChevronDown, CheckCircle2, X,
} from 'lucide-react'
import { getPriorityWeight, getTaskDateInfo } from '@/lib/rotationEngine'

const TASK_EMOJIS: Record<string, string> = {
  garbage: '🗑️', cleaning: '🧹', kitchen: '🍳', groceries: '🛒',
  laundry: '👕', maintenance: '🔧', other: '📋',
}

const PRIORITY_CONFIG = {
  high:   { label: 'High',   bg: 'bg-red-100 dark:bg-red-900/30',    text: 'text-red-600 dark:text-red-400',    dot: 'bg-red-500',    border: 'border-l-red-500' },
  medium: { label: 'Medium', bg: 'bg-yellow-100 dark:bg-yellow-900/30', text: 'text-yellow-700 dark:text-yellow-400', dot: 'bg-yellow-500', border: 'border-l-yellow-500' },
  low:    { label: 'Low',    bg: 'bg-green-100 dark:bg-green-900/30', text: 'text-green-700 dark:text-green-400',  dot: 'bg-green-500',  border: 'border-l-green-400' },
}

const FREQ_CONFIG = {
  daily:   { label: 'Daily',   bg: 'bg-orange-100 dark:bg-orange-900/30', text: 'text-orange-700 dark:text-orange-400' },
  weekly:  { label: 'Weekly',  bg: 'bg-blue-100 dark:bg-blue-900/30',   text: 'text-blue-700 dark:text-blue-400' },
  monthly: { label: 'Monthly', bg: 'bg-purple-100 dark:bg-purple-900/30', text: 'text-purple-700 dark:text-purple-400' },
}

export default function TasksPage() {
  const { tasks, members, manuallyAssignTask, createTask, deleteTask } = useFlatStore()
  const { user } = useAuthStore()

  const [selectedAssigneeId, setSelectedAssigneeId] = useState<string>('')
  const [overridingTaskId, setOverridingTaskId] = useState<string | null>(null)
  const [isCreating, setIsCreating] = useState(false)
  const [newTaskName, setNewTaskName] = useState('')
  const [newTaskType, setNewTaskType] = useState('cleaning')
  const [newTaskPriority, setNewTaskPriority] = useState<'high' | 'medium' | 'low'>('medium')
  const [newTaskFreq, setNewTaskFreq] = useState<'daily' | 'weekly' | 'monthly'>('weekly')
  const [selectedMembers, setSelectedMembers] = useState<string[]>([])

  const adminId = user?.uid || 'u1'
  const activeMembers = members.filter(m => m.status !== 'inactive')

  const handleCreate = () => {
    if (!newTaskName.trim() || selectedMembers.length === 0) return
    createTask({
      name: newTaskName,
      type: newTaskType,
      priority: newTaskPriority,
      frequency: newTaskFreq,
      currentAssignedUserId: selectedMembers[0],
      queueOrder: selectedMembers,
      dueDate: new Date(Date.now() + (newTaskFreq === 'daily' ? 86400000 : 604800000)).toISOString(),
    }, adminId)
    setIsCreating(false)
    setNewTaskName('')
    setSelectedMembers([])
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
  const sortedTasks = [...tasks].sort((a, b) => {
    if (a.status === 'overdue' && b.status !== 'overdue') return -1
    if (b.status === 'overdue' && a.status !== 'overdue') return 1
    return getPriorityWeight(b.priority) - getPriorityWeight(a.priority)
  })

  return (
    <div className="space-y-8 max-w-5xl">

      {/* ── Header ──────────────────────────────────────────────────── */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <p className="text-xs font-bold text-muted-foreground uppercase tracking-widest mb-1">Admin</p>
          <h1 className="text-3xl font-extrabold tracking-tight">Tasks & Rotation</h1>
          <p className="text-muted-foreground mt-1 text-sm">
            Define duties, manage queues, and override assignments.
          </p>
        </div>
        <div className="flex items-center gap-3 shrink-0">
          {overdueCount > 0 && (
            <div className="flex items-center gap-1.5 bg-red-100 dark:bg-red-900/30 border border-red-200 dark:border-red-800 rounded-xl px-3 py-2">
              <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse" />
              <span className="text-xs font-bold text-red-600 dark:text-red-400">{overdueCount} overdue</span>
            </div>
          )}
          <Button
            onClick={() => setIsCreating(v => !v)}
            variant={isCreating ? 'outline' : 'default'}
            className="font-semibold"
          >
            {isCreating ? (
              <><X size={15} className="mr-1.5" /> Cancel</>
            ) : (
              <><Plus size={15} className="mr-1.5" /> New Task</>
            )}
          </Button>
        </div>
      </div>

      {/* ── Create Form ─────────────────────────────────────────────── */}
      {isCreating && (
        <Card className="border-primary/40 bg-primary/3 shadow-md">
          <CardHeader className="border-b border-border/60 pb-4">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-primary/10 flex items-center justify-center">
                <Plus size={18} className="text-primary" />
              </div>
              <div>
                <CardTitle className="text-base">Create New Task</CardTitle>
                <CardDescription className="text-xs mt-0.5">Define a duty and its rotation cycle.</CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent className="p-5 space-y-5">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              {/* Name */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-muted-foreground uppercase tracking-wider">Task Name</label>
                <input
                  type="text"
                  value={newTaskName}
                  onChange={e => setNewTaskName(e.target.value)}
                  placeholder="e.g., Clean Balcony"
                  className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
                />
              </div>
              {/* Type */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-muted-foreground uppercase tracking-wider">Type</label>
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
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-muted-foreground uppercase tracking-wider">Frequency</label>
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
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-muted-foreground uppercase tracking-wider">Priority</label>
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

            {/* Participants */}
            <div className="space-y-2">
              <label className="text-xs font-bold text-muted-foreground uppercase tracking-wider flex items-center justify-between">
                <span>Rotation Participants</span>
                <span className="normal-case font-medium">{selectedMembers.length} selected — drag to reorder not yet available</span>
              </label>
              <div className="flex flex-wrap gap-2">
                {activeMembers.map(m => (
                  <button
                    key={m.uid}
                    onClick={() => toggleMember(m.uid)}
                    className={`flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold transition-all border ${
                      selectedMembers.includes(m.uid)
                        ? 'bg-primary text-white border-primary shadow-sm'
                        : 'bg-background text-foreground border-border hover:bg-secondary'
                    }`}
                  >
                    <span className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${
                      selectedMembers.includes(m.uid) ? 'bg-white/20' : 'bg-primary/10 text-primary'
                    }`}>
                      {m.nickname.charAt(0)}
                    </span>
                    {m.nickname}
                    {selectedMembers.includes(m.uid) && (
                      <span className="w-4 h-4 rounded-full bg-white/20 flex items-center justify-center text-xs font-bold">
                        {selectedMembers.indexOf(m.uid) + 1}
                      </span>
                    )}
                  </button>
                ))}
              </div>
              {selectedMembers.length > 0 && (
                <p className="text-xs text-muted-foreground">
                  Rotation order: {selectedMembers.map(uid => members.find(m => m.uid === uid)?.nickname).filter(Boolean).join(' → ')}
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

      {/* ── Task Cards ──────────────────────────────────────────────── */}
      <div className="space-y-4">
        {sortedTasks.map(task => {
          const currentAssignee = members.find(m => m.uid === task.currentAssignedUserId)
          const isOverriding = overridingTaskId === task.taskId
          const dateInfo = getTaskDateInfo(task)
          const pCfg = PRIORITY_CONFIG[task.priority] ?? PRIORITY_CONFIG.medium
          const fCfg = FREQ_CONFIG[task.frequency as keyof typeof FREQ_CONFIG] ?? FREQ_CONFIG.weekly
          const emoji = TASK_EMOJIS[task.type] ?? '📋'

          return (
            <Card key={task.taskId} className={`shadow-sm overflow-hidden border-l-4 ${pCfg.border}`}>
              <CardContent className="p-0">
                {/* Card header bar */}
                <div className="px-5 py-4 flex items-start justify-between gap-4 border-b border-border/60">
                  <div className="flex items-center gap-3 min-w-0">
                    <span className="text-2xl shrink-0">{emoji}</span>
                    <div className="min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <h3 className="text-lg font-bold leading-tight">{task.name}</h3>
                        {task.status === 'overdue' && (
                          <span className="flex items-center gap-1 text-[11px] font-bold bg-red-100 dark:bg-red-900/40 text-red-600 dark:text-red-400 px-2 py-0.5 rounded-full">
                            <AlertTriangle size={10} /> {dateInfo.overdueDays}d overdue
                          </span>
                        )}
                        {task.status === 'paused' && (
                          <span className="flex items-center gap-1 text-[11px] font-bold bg-secondary text-muted-foreground px-2 py-0.5 rounded-full">
                            <PauseCircle size={10} /> Paused
                          </span>
                        )}
                      </div>
                      <div className="flex items-center gap-2 mt-1.5 flex-wrap">
                        <span className={`text-[11px] font-bold px-2.5 py-0.5 rounded-full ${fCfg.bg} ${fCfg.text}`}>
                          {fCfg.label}
                        </span>
                        <span className={`text-[11px] font-bold px-2.5 py-0.5 rounded-full ${pCfg.bg} ${pCfg.text}`}>
                          <span className="mr-0.5">●</span> {pCfg.label} priority
                        </span>
                        <span className="text-xs text-muted-foreground flex items-center gap-1">
                          <Clock size={11} /> Due {dateInfo.dueDateFormatted}
                        </span>
                      </div>
                    </div>
                  </div>
                  {/* Admin actions */}
                  <div className="flex gap-2 shrink-0">
                    <Button
                      variant="outline"
                      size="sm"
                      className="border-border/60 font-semibold text-xs"
                      onClick={() => setOverridingTaskId(isOverriding ? null : task.taskId)}
                    >
                      <Edit2 size={12} className="mr-1.5" />
                      {isOverriding ? 'Cancel' : 'Override'}
                    </Button>
                    <Button
                      variant="outline"
                      size="icon"
                      className="border-border/60 text-destructive hover:bg-destructive/10 hover:border-destructive/30"
                      onClick={() => handleDelete(task.taskId, task.name)}
                      title="Delete Task"
                    >
                      <Trash2 size={14} />
                    </Button>
                  </div>
                </div>

                {/* Queue + override */}
                <div className="px-5 py-4 flex flex-col lg:flex-row gap-4">
                  {/* Queue visualization */}
                  <div className="flex-1">
                    <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-3">Rotation Queue</p>
                    <div className="flex items-center gap-2 flex-wrap">
                      {task.queueOrder.map((uid, index) => {
                        const m = members.find(x => x.uid === uid)
                        const isCurrent = uid === task.currentAssignedUserId
                        if (!m) return null
                        return (
                          <div key={uid} className="flex items-center gap-2">
                            <div className={`relative flex flex-col items-center gap-1 px-3 py-2 rounded-xl border-2 transition-all min-w-[70px] ${
                              isCurrent
                                ? 'bg-primary border-primary text-white shadow-md'
                                : 'bg-card border-border/60 text-muted-foreground'
                            }`}>
                              <div className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold ${
                                isCurrent ? 'bg-white/20 text-white' : 'bg-secondary text-foreground'
                              }`}>
                                {m.nickname.charAt(0)}
                              </div>
                              <span className="text-xs font-semibold">{m.nickname}</span>
                              {m.status === 'out_of_station' && (
                                <PauseCircle size={10} className="absolute -top-1.5 -right-1.5 text-orange-400 bg-card rounded-full" />
                              )}
                              {isCurrent && (
                                <span className="absolute -top-2 left-1/2 -translate-x-1/2 text-[9px] font-bold bg-primary text-white px-1.5 py-0.5 rounded-full whitespace-nowrap">
                                  NOW
                                </span>
                              )}
                            </div>
                            {index < task.queueOrder.length - 1 && (
                              <ArrowRight size={14} className="text-muted-foreground/50 shrink-0" />
                            )}
                          </div>
                        )
                      })}
                    </div>
                    <p className="text-xs text-muted-foreground mt-3">
                      Last done: <span className="font-semibold text-foreground">{dateInfo.lastCompletedFormatted}</span>
                    </p>
                  </div>

                  {/* Override panel */}
                  {isOverriding && (
                    <div className="lg:w-64 bg-secondary/40 border border-border/60 rounded-xl p-4 space-y-3">
                      <div className="flex items-center gap-2">
                        <Edit2 size={13} className="text-primary" />
                        <p className="text-xs font-bold uppercase tracking-wider text-primary">Admin Override</p>
                      </div>
                      <select
                        value={selectedAssigneeId}
                        onChange={e => setSelectedAssigneeId(e.target.value)}
                        className="w-full bg-background border border-border rounded-xl px-3 py-2.5 text-sm"
                      >
                        <option value="" disabled>Select member</option>
                        {activeMembers.map(sub => (
                          <option key={sub.uid} value={sub.uid}>
                            {sub.nickname}{sub.status === 'out_of_station' ? ' (Absent)' : ''}
                          </option>
                        ))}
                      </select>
                      <div className="flex gap-2">
                        <Button
                          className="flex-1 font-bold"
                          onClick={() => handleManualAssign(task.taskId)}
                          disabled={!selectedAssigneeId}
                          size="sm"
                        >
                          <CheckCircle2 size={13} className="mr-1.5" /> Force Assign
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="flex-1"
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
            <CardContent className="flex flex-col items-center justify-center p-16 text-muted-foreground">
              <ClipboardList size={48} className="mb-4 opacity-30" />
              <p className="text-lg font-bold">No Tasks Yet</p>
              <p className="text-sm mt-1">Click "New Task" above to create your first duty rotation.</p>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}
