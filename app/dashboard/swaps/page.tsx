"use client"
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { timeAgo } from '@/lib/rotationEngine'
import { Bell, Send, Check, X, CheckCircle2, XCircle, Clock, Inbox } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'

export default function SwapsPage() {
  const { swapRequests, members, tasks, resolveSwapRequest, markSwapRequestRead } = useFlatStore()
  const { user } = useAuthStore()

  const currentUserId = user?.uid || 'u1'

  const incoming = swapRequests.filter(
    r => r.toUserId === currentUserId && r.status === 'pending'
  )
  const outgoingPending = swapRequests.filter(
    r => r.fromUserId === currentUserId && r.status === 'pending'
  )
  const outgoingResolved = swapRequests.filter(
    r => r.fromUserId === currentUserId && r.status !== 'pending' && !r.read
  )

  const isEmpty = incoming.length === 0 && outgoingPending.length === 0 && outgoingResolved.length === 0

  return (
    <div className="space-y-8">

      {/* Header */}
      <div>
        <h1 className="text-2xl font-extrabold tracking-tight">Swap Requests</h1>
        <p className="text-muted-foreground mt-1 text-sm">
          Review incoming coverage requests and track the ones you've sent.
        </p>
      </div>

      {/* Empty state */}
      {isEmpty && (
        <Card className="border-dashed border-2">
          <CardContent className="flex flex-col items-center justify-center p-16">
            <Inbox size={40} className="text-muted-foreground/30 mb-4" />
            <p className="font-bold text-lg text-muted-foreground">All clear</p>
            <p className="text-sm text-muted-foreground/60 mt-1 text-center">
              No pending swap requests right now. Use the Swap button on a task card to ask a flatmate to cover your duty.
            </p>
          </CardContent>
        </Card>
      )}

      {/* ── Incoming Requests (action required) ────────────────────────── */}
      {incoming.length > 0 && (
        <section>
          <div className="flex items-center gap-2 mb-4">
            <Bell size={17} className="text-violet-500" />
            <h2 className="text-base font-bold">Requests for You</h2>
            <span className="bg-violet-500 text-white text-xs font-extrabold px-2 py-0.5 rounded-full">
              {incoming.length}
            </span>
          </div>

          <div className="space-y-3">
            {incoming.map(req => {
              const task     = tasks.find(t => t.taskId === req.taskId)
              const fromUser = members.find(m => m.uid === req.fromUserId)
              if (!task || !fromUser) return null

              return (
                <div
                  key={req.id}
                  className="flex flex-col sm:flex-row sm:items-center gap-4 p-4 rounded-2xl border-2 border-violet-300 dark:border-violet-700 bg-violet-50 dark:bg-violet-950/40 shadow-sm"
                >
                  {/* Icon */}
                  <div className="w-10 h-10 rounded-xl bg-violet-100 dark:bg-violet-900/50 flex items-center justify-center shrink-0">
                    <Bell size={18} className="text-violet-600 dark:text-violet-400 animate-bounce" />
                  </div>

                  {/* Info */}
                  <div className="flex-1 min-w-0">
                    <p className="text-[11px] font-extrabold text-violet-500 dark:text-violet-400 uppercase tracking-widest mb-0.5">
                      {req.isAutomatic ? 'Auto-Assigned — Action Required' : 'Swap Request — Action Required'}
                    </p>
                    <p className="text-sm font-semibold text-violet-900 dark:text-violet-100">
                      {req.isAutomatic ? (
                        <>
                          <span className="font-extrabold">{fromUser.nickname}</span> is out of station —
                          you&apos;re next in queue for <span className="font-extrabold">{task.name}</span>.
                        </>
                      ) : (
                        <>
                          <span className="font-extrabold">{fromUser.nickname}</span> asked you to cover{' '}
                          <span className="font-extrabold">{task.name}</span>.
                        </>
                      )}
                    </p>
                    <p className="text-xs text-violet-500/70 dark:text-violet-400/60 mt-1">
                      {timeAgo(req.createdAt)}
                    </p>
                  </div>

                  {/* Actions */}
                  <div className="flex gap-2 shrink-0">
                    <Button
                      size="sm"
                      className="bg-green-600 hover:bg-green-700 text-white font-bold px-4"
                      onClick={() => resolveSwapRequest(req.id, 'accepted')}
                    >
                      <Check size={14} className="mr-1.5" /> Accept
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      className="border-violet-300 text-violet-700 dark:text-violet-300 hover:bg-violet-100 dark:hover:bg-violet-900/40 font-bold px-4"
                      onClick={() => resolveSwapRequest(req.id, 'rejected')}
                    >
                      <X size={14} className="mr-1.5" /> Decline
                    </Button>
                  </div>
                </div>
              )
            })}
          </div>
        </section>
      )}

      {/* ── Outgoing Pending (waiting for response) ─────────────────────── */}
      {outgoingPending.length > 0 && (
        <section>
          <div className="flex items-center gap-2 mb-4">
            <Clock size={17} className="text-blue-500" />
            <h2 className="text-base font-bold">Awaiting Response</h2>
            <span className="bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300 text-xs font-extrabold px-2 py-0.5 rounded-full">
              {outgoingPending.length}
            </span>
          </div>

          <div className="space-y-3">
            {outgoingPending.map(req => {
              const task   = tasks.find(t => t.taskId === req.taskId)
              const toUser = members.find(m => m.uid === req.toUserId)
              if (!task || !toUser) return null

              return (
                <div
                  key={req.id}
                  className="flex flex-col sm:flex-row sm:items-center gap-4 p-4 rounded-2xl border border-border bg-card shadow-sm"
                >
                  <div className="w-10 h-10 rounded-xl bg-blue-100 dark:bg-blue-900/40 flex items-center justify-center shrink-0">
                    <Send size={16} className="text-blue-600 dark:text-blue-400" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-[11px] font-extrabold text-blue-500 uppercase tracking-widest mb-0.5">
                      Waiting for Response
                    </p>
                    <p className="text-sm font-semibold">
                      You asked <span className="font-extrabold">{toUser.nickname}</span> to cover{' '}
                      <span className="font-extrabold">{task.name}</span>.
                    </p>
                    <p className="text-xs text-muted-foreground mt-1">{timeAgo(req.createdAt)}</p>
                  </div>
                  <span className="text-xs font-bold bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300 px-3 py-1 rounded-full shrink-0">
                    Pending
                  </span>
                </div>
              )
            })}
          </div>
        </section>
      )}

      {/* ── Resolved Responses (to acknowledge) ─────────────────────────── */}
      {outgoingResolved.length > 0 && (
        <section>
          <div className="flex items-center gap-2 mb-4">
            <CheckCircle2 size={17} className="text-muted-foreground" />
            <h2 className="text-base font-bold">Responses</h2>
          </div>

          <div className="space-y-3">
            {outgoingResolved.map(req => {
              const task   = tasks.find(t => t.taskId === req.taskId)
              const toUser = members.find(m => m.uid === req.toUserId)
              if (!task || !toUser) return null

              const accepted = req.status === 'accepted'

              return (
                <div
                  key={req.id}
                  className={`flex flex-col sm:flex-row sm:items-center gap-4 p-4 rounded-2xl border shadow-sm ${
                    accepted
                      ? 'bg-green-50 dark:bg-green-950/30 border-green-200 dark:border-green-800'
                      : 'bg-red-50 dark:bg-red-950/30 border-red-200 dark:border-red-800'
                  }`}
                >
                  <div className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 ${
                    accepted ? 'bg-green-100 dark:bg-green-900/50' : 'bg-red-100 dark:bg-red-900/50'
                  }`}>
                    {accepted
                      ? <CheckCircle2 size={18} className="text-green-600 dark:text-green-400" />
                      : <XCircle    size={18} className="text-red-500  dark:text-red-400"   />}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className={`text-[11px] font-extrabold uppercase tracking-widest mb-0.5 ${
                      accepted ? 'text-green-600 dark:text-green-400' : 'text-red-500 dark:text-red-400'
                    }`}>
                      {accepted ? 'Accepted' : 'Declined'}
                    </p>
                    <p className="text-sm font-semibold">
                      <span className="font-extrabold">{toUser.nickname}</span>{' '}
                      {accepted ? 'agreed to cover' : 'declined to cover'}{' '}
                      <span className="font-extrabold">{task.name}</span>.
                    </p>
                    <p className="text-xs text-muted-foreground mt-1">{timeAgo(req.createdAt)}</p>
                  </div>
                  <Button
                    size="sm"
                    variant="outline"
                    className={`shrink-0 ${
                      accepted
                        ? 'border-green-300 dark:border-green-700 hover:bg-green-100 dark:hover:bg-green-900/30'
                        : 'border-red-300 dark:border-red-700 hover:bg-red-100 dark:hover:bg-red-900/30'
                    }`}
                    onClick={() => markSwapRequestRead(req.id)}
                  >
                    Dismiss
                  </Button>
                </div>
              )
            })}
          </div>
        </section>
      )}
    </div>
  )
}
