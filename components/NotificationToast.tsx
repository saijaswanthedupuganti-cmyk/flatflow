"use client"
import { useEffect, useRef, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { CheckCircle2, X } from 'lucide-react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { timeAgo } from '@/lib/rotationEngine'

interface ToastItem {
  id: string
  message: string
  sub: string
  timestamp: string
}

const TASK_EMOJIS: Record<string, string> = {
  garbage: '🗑️',
  cleaning: '🧹',
  kitchen: '🍳',
  groceries: '🛒',
  laundry: '👕',
  maintenance: '🔧',
  other: '📋',
}

export default function NotificationToast() {
  const { activityLog, members, tasks } = useFlatStore()
  const { user } = useAuthStore()
  const [toasts, setToasts] = useState<ToastItem[]>([])
  // Track the timestamp of the last activity we've already processed
  const lastSeenRef = useRef<string | null>(null)
  const initialLoadRef = useRef(true)

  useEffect(() => {
    if (activityLog.length === 0) return

    // On the very first load, just record what's already there — don't toast old items
    if (initialLoadRef.current) {
      lastSeenRef.current = activityLog[0]?.timestamp ?? null
      initialLoadRef.current = false
      return
    }

    // Find activities newer than what we last saw
    const newActivities = activityLog.filter(a => {
      if (!lastSeenRef.current) return false
      return (
        new Date(a.timestamp) > new Date(lastSeenRef.current) &&
        a.action === 'completed_task' &&
        a.userId !== user?.uid  // don't notify yourself
      )
    })

    if (newActivities.length > 0) {
      // Update the last seen pointer
      lastSeenRef.current = activityLog[0].timestamp

      newActivities.forEach(activity => {
        const member = members.find(m => m.uid === activity.userId)
        const taskName = activity.details.replace('completed ', '')
        const task = tasks.find(t => t.name === taskName)
        const emoji = task ? (TASK_EMOJIS[task.type] ?? '✅') : '✅'

        const toast: ToastItem = {
          id: activity.id,
          message: `${emoji} ${member?.nickname ?? 'Someone'} completed ${taskName}`,
          sub: timeAgo(activity.timestamp),
          timestamp: activity.timestamp,
        }

        setToasts(prev => [toast, ...prev].slice(0, 4)) // max 4 toasts

        // Auto-dismiss after 6 seconds
        setTimeout(() => {
          setToasts(prev => prev.filter(t => t.id !== toast.id))
        }, 6000)
      })
    }
  }, [activityLog, members, tasks, user?.uid])

  const dismiss = (id: string) => {
    setToasts(prev => prev.filter(t => t.id !== id))
  }

  return (
    <div className="fixed top-4 right-4 z-[100] flex flex-col gap-2 pointer-events-none">
      <AnimatePresence>
        {toasts.map(toast => (
          <motion.div
            key={toast.id}
            initial={{ opacity: 0, x: 60, scale: 0.95 }}
            animate={{ opacity: 1, x: 0, scale: 1 }}
            exit={{ opacity: 0, x: 60, scale: 0.95 }}
            transition={{ type: 'spring', stiffness: 300, damping: 28 }}
            className="pointer-events-auto"
          >
            <div className="bg-card border border-border shadow-xl rounded-xl px-4 py-3 flex items-start gap-3 min-w-[280px] max-w-[340px]">
              <div className="mt-0.5 shrink-0">
                <CheckCircle2 size={20} className="text-green-500" />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold leading-snug">{toast.message}</p>
                <p className="text-xs text-muted-foreground mt-0.5">{toast.sub}</p>
              </div>
              <button
                onClick={() => dismiss(toast.id)}
                className="shrink-0 text-muted-foreground hover:text-foreground mt-0.5"
              >
                <X size={14} />
              </button>
            </div>
          </motion.div>
        ))}
      </AnimatePresence>
    </div>
  )
}
