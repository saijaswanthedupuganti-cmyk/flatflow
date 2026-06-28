"use client"
import { useEffect, useRef, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { CheckCircle2, X } from 'lucide-react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { timeAgo } from '@/lib/rotationEngine'
import { getMemberColorByIndex } from '@/lib/memberColors'

interface ToastItem {
  id: string
  message: string
  sub: string
  timestamp: string
  memberIndex?: number
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
  const lastSeenRef = useRef<string | null>(null)
  const initialLoadRef = useRef(true)

  useEffect(() => {
    if (activityLog.length === 0) return

    if (initialLoadRef.current) {
      lastSeenRef.current = activityLog[0]?.timestamp ?? null
      initialLoadRef.current = false
      return
    }

    const newActivities = activityLog.filter(a => {
      if (!lastSeenRef.current) return false
      return (
        new Date(a.timestamp) > new Date(lastSeenRef.current) &&
        a.action === 'completed_task' &&
        a.userId !== user?.uid
      )
    })

    if (newActivities.length > 0) {
      lastSeenRef.current = activityLog[0].timestamp

      newActivities.forEach(activity => {
        const member = members.find(m => m.uid === activity.userId)
        const memberIndex = member ? members.filter(m => m.status !== 'inactive').indexOf(member) : 0
        const taskName = activity.details.replace('completed ', '')
        const task = tasks.find(t => t.name === taskName)
        const emoji = task ? (TASK_EMOJIS[task.type] ?? '✅') : '✅'

        const toast: ToastItem = {
          id: activity.id,
          message: `${emoji} ${member?.nickname ?? 'Someone'} completed ${taskName}`,
          sub: timeAgo(activity.timestamp),
          timestamp: activity.timestamp,
          memberIndex,
        }

        setToasts(prev => [toast, ...prev].slice(0, 4))

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
        {toasts.map(toast => {
          const memberColor = getMemberColorByIndex(toast.memberIndex ?? 0)
          return (
            <motion.div
              key={toast.id}
              initial={{ opacity: 0, x: 60, scale: 0.92 }}
              animate={{ opacity: 1, x: 0, scale: 1 }}
              exit={{ opacity: 0, x: 60, scale: 0.92, transition: { duration: 0.18 } }}
              transition={{ type: 'spring', stiffness: 320, damping: 26 }}
              className="pointer-events-auto"
            >
              <div
                className="hiq-toast hiq-toast-success"
                style={{ borderLeftColor: memberColor.hex }}
              >
                <div
                  className="hiq-toast-icon"
                  style={{
                    background: `${memberColor.hex}18`,
                    color: memberColor.hex,
                  }}
                >
                  <CheckCircle2 size={16} />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold leading-snug">{toast.message}</p>
                  <p className="text-xs text-muted-foreground mt-0.5">{toast.sub}</p>
                </div>
                <button
                  onClick={() => dismiss(toast.id)}
                  className="shrink-0 text-muted-foreground hover:text-foreground transition-colors mt-0.5"
                >
                  <X size={14} />
                </button>
              </div>
            </motion.div>
          )
        })}
      </AnimatePresence>
    </div>
  )
}
