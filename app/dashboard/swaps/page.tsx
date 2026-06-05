"use client"
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { timeAgo } from '@/lib/rotationEngine'
import { Bell, Send, Check, X, CheckCircle2, XCircle, Clock, Inbox, ArrowDownLeft, ArrowUpRight } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'

export default function SwapsPage() {
  const { swapRequests, members, tasks, resolveSwapRequest } = useFlatStore()
  const { user } = useAuthStore()

  const currentUserId = user?.uid || 'u1'

  // All requests addressed TO me (received), newest first
  const received = [...swapRequests]
    .filter(r => r.toUserId === currentUserId)
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())

  // All requests sent BY me (sent), newest first
  const sent = [...swapRequests]
    .filter(r => r.fromUserId === currentUserId)
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())

  const pendingReceivedCount = received.filter(r => r.status === 'pending').length
  const hasAny = received.length > 0 || sent.length > 0

  const StatusBadge = ({ status }: { status: 'pending' | 'accepted' | 'rejected' }) => {
    if (status === 'pending')  return <span className="text-[11px] font-bold bg-blue-100   text-blue-700   dark:bg-blue-900/40   dark:text-blue-300   px-2.5 py-1 rounded-full shrink-0">Pending</span>
    if (status === 'accepted') return <span className="text-[11px] font-bold bg-green-100  text-green-700  dark:bg-green-900/40  dark:text-green-300  px-2.5 py-1 rounded-full shrink-0">Accepted</span>
    return                            <span className="text-[11px] font-bold bg-red-100    text-red-700    dark:bg-red-900/40    dark:text-red-300    px-2.5 py-1 rounded-full shrink-0">Declined</span>
  }

  return (
    <div className="space-y-6 max-w-5xl">

      {/* ── Header ────────────────────────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight">Swap Requests</h1>
          <p className="text-muted-foreground mt-1 text-sm">
            Coverage requests you&apos;ve sent and received, newest first.
          </p>
        </div>
        {/* Summary chips */}
        {hasAny && (
          <div className="flex gap-2 shrink-0">
            <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-violet-50 dark:bg-violet-900/20 border border-violet-200 dark:border-violet-800">
              <ArrowDownLeft size={13} className="text-violet-500" />
              <span className="text-xs font-bold text-violet-700 dark:text-violet-300">{received.length} received</span>
              {pendingReceivedCount > 0 && (
                <span className="bg-violet-500 text-white text-[10px] font-extrabold px-1.5 py-0.5 rounded-full">{pendingReceivedCount}</span>
              )}
            </div>
            <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800">
              <ArrowUpRight size={13} className="text-blue-500" />
              <span className="text-xs font-bold text-blue-700 dark:text-blue-300">{sent.length} sent</span>
            </div>
          </div>
        )}
      </div>

      {/* ── Empty state ───────────────────────────────────────────────── */}
      {!hasAny && (
        <Card className="border-dashed border-2">
          <CardContent className="flex flex-col items-center justify-center p-16 text-center">
            <Inbox size={44} className="text-muted-foreground/25 mb-4" />
            <p className="font-bold text-lg text-muted-foreground">No swap history yet</p>
            <p className="text-sm text-muted-foreground/60 mt-1 max-w-xs">
              When you or a flatmate uses the Swap button on a task card, all requests will appear here.
            </p>
          </CardContent>
        </Card>
      )}

      {/* ── Side-by-side grid: Received | Sent ───────────────────────── */}
      {hasAny && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 items-start">

          {/* ── Received ──────────────────────────────────────────────── */}
          <section>
            <div className="flex items-center gap-2 mb-3">
              <ArrowDownLeft size={16} className="text-violet-500" />
              <h2 className="text-base font-bold">Received</h2>
              <span className="text-xs font-bold text-muted-foreground bg-secondary px-2 py-0.5 rounded-full">{received.length}</span>
              {pendingReceivedCount > 0 && (
                <span className="text-[10px] font-extrabold bg-violet-500 text-white px-1.5 py-0.5 rounded-full">{pendingReceivedCount} pending</span>
              )}
            </div>

            {received.length === 0 ? (
              <div className="flex flex-col items-center justify-center p-8 rounded-xl border border-dashed border-border text-center">
                <Inbox size={28} className="text-muted-foreground/25 mb-2" />
                <p className="text-sm text-muted-foreground">No requests received yet</p>
              </div>
            ) : (
              <div className="space-y-2">
                {received.map(req => {
                  const task     = tasks.find(t => t.taskId === req.taskId)
                  const fromUser = members.find(m => m.uid === req.fromUserId)
                  const isPending = req.status === 'pending'

                  return (
                    <div
                      key={req.id}
                      className={`flex flex-col gap-3 p-4 rounded-xl border transition-all ${
                        isPending
                          ? 'border-violet-300 dark:border-violet-700 bg-violet-50 dark:bg-violet-950/30'
                          : 'border-border bg-card'
                      }`}
                    >
                      <div className="flex items-start gap-3">
                        {/* Icon */}
                        <div className={`w-9 h-9 rounded-lg flex items-center justify-center shrink-0 ${
                          isPending ? 'bg-violet-100 dark:bg-violet-900/50' : 'bg-secondary'
                        }`}>
                          {isPending
                            ? <Bell size={15} className="text-violet-600 dark:text-violet-400" />
                            : req.status === 'accepted'
                              ? <CheckCircle2 size={15} className="text-green-600" />
                              : <XCircle size={15} className="text-red-500" />}
                        </div>

                        {/* Text */}
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-semibold leading-snug">
                            {req.isAutomatic ? (
                              <>
                                <span className="font-extrabold">{fromUser?.nickname ?? 'Someone'}</span>
                                {' '}is out of station — you&apos;re next for{' '}
                                <span className="font-extrabold">{task?.name ?? 'a task'}</span>
                              </>
                            ) : (
                              <>
                                <span className="font-extrabold">{fromUser?.nickname ?? 'Someone'}</span>
                                {' '}asked you to cover{' '}
                                <span className="font-extrabold">{task?.name ?? 'a task'}</span>
                              </>
                            )}
                          </p>
                          <p className="text-xs text-muted-foreground mt-0.5">{timeAgo(req.createdAt)}</p>
                        </div>

                        {!isPending && <StatusBadge status={req.status} />}
                      </div>

                      {/* Action buttons on their own row when pending */}
                      {isPending && (
                        <div className="flex gap-2">
                          <Button size="sm" className="flex-1 bg-green-600 hover:bg-green-700 text-white font-bold"
                            onClick={() => resolveSwapRequest(req.id, 'accepted')}>
                            <Check size={13} className="mr-1" /> Accept
                          </Button>
                          <Button size="sm" variant="outline"
                            className="flex-1 border-violet-300 text-violet-700 dark:text-violet-300 font-bold"
                            onClick={() => resolveSwapRequest(req.id, 'rejected')}>
                            <X size={13} className="mr-1" /> Decline
                          </Button>
                        </div>
                      )}
                    </div>
                  )
                })}
              </div>
            )}
          </section>

          {/* ── Sent ──────────────────────────────────────────────────── */}
          <section>
            <div className="flex items-center gap-2 mb-3">
              <ArrowUpRight size={16} className="text-blue-500" />
              <h2 className="text-base font-bold">Sent</h2>
              <span className="text-xs font-bold text-muted-foreground bg-secondary px-2 py-0.5 rounded-full">{sent.length}</span>
            </div>

            {sent.length === 0 ? (
              <div className="flex flex-col items-center justify-center p-8 rounded-xl border border-dashed border-border text-center">
                <Inbox size={28} className="text-muted-foreground/25 mb-2" />
                <p className="text-sm text-muted-foreground">No requests sent yet</p>
              </div>
            ) : (
              <div className="space-y-2">
                {sent.map(req => {
                  const task   = tasks.find(t => t.taskId === req.taskId)
                  const toUser = members.find(m => m.uid === req.toUserId)

                  return (
                    <div
                      key={req.id}
                      className="flex items-start gap-3 p-4 rounded-xl border border-border bg-card"
                    >
                      {/* Icon */}
                      <div className="w-9 h-9 rounded-lg bg-secondary flex items-center justify-center shrink-0">
                        {req.status === 'pending'
                          ? <Clock size={15} className="text-blue-500" />
                          : req.status === 'accepted'
                            ? <CheckCircle2 size={15} className="text-green-600" />
                            : <XCircle size={15} className="text-red-500" />}
                      </div>

                      {/* Text */}
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-semibold leading-snug">
                          You asked <span className="font-extrabold">{toUser?.nickname ?? 'someone'}</span>
                          {' '}to cover <span className="font-extrabold">{task?.name ?? 'a task'}</span>
                        </p>
                        <p className="text-xs text-muted-foreground mt-0.5">{timeAgo(req.createdAt)}</p>
                      </div>

                      <StatusBadge status={req.status} />
                    </div>
                  )
                })}
              </div>
            )}
          </section>

        </div>
      )}
    </div>
  )
}
