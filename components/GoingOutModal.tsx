"use client"
import { useState } from 'react'
import { X, MapPinOff, ArrowRight, AlertTriangle } from 'lucide-react'
import { Button } from '@/components/ui/button'
import type { Task, Member } from '@/store/useFlatStore'

interface Props {
  assignedTasks: Task[]
  availableMembers: Member[]   // people who can take over (excludes the going-out user)
  onConfirm: (assignments: Record<string, string>) => void
  onClose: () => void
}

export default function GoingOutModal({ assignedTasks, availableMembers, onConfirm, onClose }: Props) {
  const [selections, setSelections] = useState<Record<string, string>>({})
  const [loading, setLoading] = useState(false)

  const allAssigned = assignedTasks.every(t => !!selections[t.taskId])

  const handleConfirm = async () => {
    if (!allAssigned) return
    setLoading(true)
    await onConfirm(selections)
    setLoading(false)
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
      <div className="bg-card border border-border rounded-2xl shadow-xl w-full max-w-md space-y-0 overflow-hidden">

        {/* Header */}
        <div className="flex items-start justify-between px-5 pt-5 pb-4 border-b border-border">
          <div className="flex items-start gap-3">
            <div className="w-9 h-9 rounded-xl bg-orange-100 dark:bg-orange-900/30 flex items-center justify-center shrink-0">
              <AlertTriangle size={16} className="text-orange-600 dark:text-orange-400" />
            </div>
            <div>
              <h3 className="font-bold text-base leading-tight">You have assigned tasks</h3>
              <p className="text-sm text-muted-foreground mt-0.5">
                Hand off {assignedTasks.length} task{assignedTasks.length !== 1 ? 's' : ''} before going out of station.
              </p>
            </div>
          </div>
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground ml-2 shrink-0 p-1">
            <X size={16} />
          </button>
        </div>

        {/* Task list */}
        <div className="px-5 py-4 space-y-3 max-h-72 overflow-y-auto">
          {assignedTasks.map(task => (
            <div key={task.taskId} className="space-y-1.5">
              <div className="flex items-center gap-2">
                <span className={`w-2 h-2 rounded-full shrink-0 ${task.status === 'overdue' ? 'bg-red-500' : 'bg-blue-500'}`} />
                <p className="text-sm font-semibold truncate">{task.name}</p>
                <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded-full shrink-0 ${
                  task.status === 'overdue'
                    ? 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
                    : 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400'
                }`}>
                  {task.status}
                </span>
              </div>
              <div className="flex items-center gap-2 pl-4">
                <ArrowRight size={12} className="text-muted-foreground shrink-0" />
                <select
                  value={selections[task.taskId] || ''}
                  onChange={e => setSelections(s => ({ ...s, [task.taskId]: e.target.value }))}
                  className="flex-1 bg-background border border-border rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
                >
                  <option value="" disabled>Select who covers this</option>
                  {availableMembers.map(m => (
                    <option key={m.uid} value={m.uid}>{m.nickname}</option>
                  ))}
                  {availableMembers.length === 0 && (
                    <option value="" disabled>No available members</option>
                  )}
                </select>
              </div>
            </div>
          ))}
        </div>

        {/* Footer */}
        <div className="px-5 pb-5 pt-3 border-t border-border flex gap-2">
          <Button variant="outline" className="flex-1" onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button
            className="flex-1 font-bold bg-orange-600 hover:bg-orange-700 text-white"
            disabled={!allAssigned || availableMembers.length === 0 || loading}
            onClick={handleConfirm}
          >
            <MapPinOff size={14} className="mr-1.5" />
            {loading ? 'Transferring…' : 'Transfer & Go Out'}
          </Button>
        </div>

        {availableMembers.length === 0 && (
          <p className="px-5 pb-4 text-xs text-destructive font-medium">
            No available members to hand tasks to. Ask a flatmate to mark themselves available first.
          </p>
        )}
      </div>
    </div>
  )
}
