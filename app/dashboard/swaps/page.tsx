"use client"
import { useState } from 'react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { timeAgo } from '@/lib/rotationEngine'
import { Bell, Check, X, CheckCircle2, XCircle, Clock, Inbox, ArrowDownLeft, ArrowUpRight, Users } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'

export default function SwapsPage() {
  const { swapRequests, members, tasks, resolveSwapRequest } = useFlatStore()
  const { user } = useAuthStore()

  const currentUserId = user?.uid || 'u1'
  const isAdmin = members.find(m => m.uid === currentUserId)?.role === 'admin'
  const [adminView, setAdminView] = useState<'mine' | 'all'>('mine')

  // My swap requests — newest first
  const received = [...swapRequests]
    .filter(r => r.toUserId === currentUserId)
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())

  const sent = [...swapRequests]
    .filter(r => r.fromUserId === currentUserId)
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())

  // All flat swaps for admin
  const allSwaps = [...swapRequests]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())

  const pendingReceivedCount = received.filter(r => r.status === 'pending').length
  const hasAny = received.length > 0 || sent.length > 0

  const totalAccepted = swapRequests.filter(r =>
    (r.fromUserId === currentUserId || r.toUserId === currentUserId) && r.status === 'accepted'
  ).length
  const totalRejected = swapRequests.filter(r =>
    (r.fromUserId === currentUserId || r.toUserId === currentUserId) && r.status === 'rejected'
  ).length

  const StatusBadge = ({ status }: { status: 'pending' | 'accepted' | 'rejected' }) => {
    if (status === 'pending')  return <span className="text-[11px] font-bold bg-blue-100   text-blue-700   dark:bg-blue-900/40   dark:text-blue-300   px-2.5 py-1 rounded-full shrink-0">Pending</span>
    if (status === 'accepted') return <span className="text-[11px] font-bold bg-green-100  text-green-700  dark:bg-green-900/40  dark:text-green-300  px-2.5 py-1 rounded-full shrink-0">Accepted</span>
    return                            <span className="text-[11px] font-bold bg-red-100    text-red-700    dark:bg-red-900/40    dark:text-red-300    px-2.5 py-1 rounded-full shrink-0">Declined</span>
  }

  return (
    <div className="space-y-6 max-w-5xl">

      {/* ── Header ────────────────────────────────────────────────────── */}
      <div className="space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
          <div>
            <h1 className="text-2xl font-extrabold tracking-tight">Swap Requests</h1>
            <p className="text-muted-foreground mt-1 text-sm">
              Coverage requests you&apos;ve sent and received.
            </p>
          </div>

          {/* Admin toggle */}
          {isAdmin && (
            <div className="flex items-center bg-secondary rounded-[10px] p-0.5 gap-0.5 shrink-0">
              <button
                onClick={() => setAdminView('mine')}
                className={`px-3 py-1.5 rounded-[8px] text-xs font-bold transition-all ${
                  adminView === 'mine' ? 'bg-card text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                My Swaps
              </button>
              <button
                onClick={() => setAdminView('all')}
                className={`px-3 py-1.5 rounded-[8px] text-xs font-bold transition-all flex items-center gap-1.5 ${
                  adminView === 'all' ? 'bg-card text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                <Users size={11} /> All Swaps
                {swapRequests.length > 0 && (
                  <span className="bg-violet-500 text-white text-[9px] font-extrabold px-1.5 py-0.5 rounded-full leading-none">
                    {swapRequests.length}
                  </span>
                )}
              </button>
            </div>
          )}
        </div>

        {/* 4-stat chips — my swaps view only */}
        {adminView === 'mine' && hasAny && (
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
            <div className="flex items-center gap-2.5 px-3 py-2.5 rounded-xl bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800">
              <ArrowUpRight size={15} className="text-blue-500 shrink-0" />
              <div>
                <p className="text-[10px] font-bold uppercase tracking-wider text-blue-600 dark:text-blue-400">Sent</p>
                <p className="text-xl font-extrabold text-blue-700 dark:text-blue-300 leading-none mt-0.5">{sent.length}</p>
              </div>
            </div>
            <div className="flex items-center gap-2.5 px-3 py-2.5 rounded-xl bg-violet-50 dark:bg-violet-900/20 border border-violet-200 dark:border-violet-800">
              <ArrowDownLeft size={15} className="text-violet-500 shrink-0" />
              <div>
                <p className="text-[10px] font-bold uppercase tracking-wider text-violet-600 dark:text-violet-400">Received</p>
                <p className="text-xl font-extrabold text-violet-700 dark:text-violet-300 leading-none mt-0.5">
                  {received.length}
                  {pendingReceivedCount > 0 && (
                    <span className="ml-1.5 text-[11px] font-extrabold bg-violet-500 text-white px-1.5 py-0.5 rounded-full align-middle">
                      {pendingReceivedCount}
                    </span>
                  )}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2.5 px-3 py-2.5 rounded-xl bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800">
              <CheckCircle2 size={15} className="text-green-500 shrink-0" />
              <div>
                <p className="text-[10px] font-bold uppercase tracking-wider text-green-600 dark:text-green-400">Accepted</p>
                <p className="text-xl font-extrabold text-green-700 dark:text-green-300 leading-none mt-0.5">{totalAccepted || '—'}</p>
              </div>
            </div>
            <div className="flex items-center gap-2.5 px-3 py-2.5 rounded-xl bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800">
              <XCircle size={15} className="text-red-500 shrink-0" />
              <div>
                <p className="text-[10px] font-bold uppercase tracking-wider text-red-600 dark:text-red-400">Declined</p>
                <p className="text-xl font-extrabold text-red-700 dark:text-red-300 leading-none mt-0.5">{totalRejected || '—'}</p>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* ── Admin: All Swaps view ─────────────────────────────────────── */}
      {isAdmin && adminView === 'all' && (
        <div>
          {allSwaps.length === 0 ? (
            <Card className="border-dashed border-2">
              <CardContent className="flex flex-col items-center justify-center p-16 text-center">
                <Inbox size={44} className="text-muted-foreground/25 mb-4" />
                <p className="font-bold text-lg text-muted-foreground">No swaps in this flat yet</p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-2">
              {allSwaps.map(req => {
                const task     = tasks.find(t => t.taskId === req.taskId)
                const fromUser = members.find(m => m.uid === req.fromUserId)
                const toUser   = members.find(m => m.uid === req.toUserId)
                const isMe     = req.fromUserId === currentUserId || req.toUserId === currentUserId
                const isPending = req.status === 'pending'

                return (
                  <div
                    key={req.id}
                    className={`flex flex-col sm:flex-row sm:items-center gap-3 p-4 rounded-xl border transition-all ${
                      isPending && req.toUserId === currentUserId
                        ? 'border-violet-300 dark:border-violet-700 bg-violet-50 dark:bg-violet-950/30'
                        : isMe
                          ? 'border-primary/20 bg-primary/5'
                          : 'border-border bg-card'
                    }`}
                  >
                    {/* Icon */}
                    <div className={`w-9 h-9 rounded-lg flex items-center justify-center shrink-0 ${
                      isPending ? 'bg-blue-100 dark:bg-blue-900/40' : req.status === 'accepted' ? 'bg-green-100 dark:bg-green-900/40' : 'bg-red-100 dark:bg-red-900/40'
                    }`}>
                      {isPending
                        ? <Clock size={15} className="text-blue-500" />
                        : req.status === 'accepted'
                          ? <CheckCircle2 size={15} className="text-green-600" />
                          : <XCircle size={15} className="text-red-500" />}
                    </div>

                    {/* Text */}
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-semibold leading-snug">
                        <span className="font-extrabold">{fromUser?.nickname ?? '?'}</span>
                        <span className="text-muted-foreground mx-1">→</span>
                        <span className="font-extrabold">{toUser?.nickname ?? '?'}</span>
                        <span className="text-muted-foreground"> · </span>
                        <span>{task?.name ?? 'Unknown task'}</span>
                      </p>
                      <p className="text-xs text-muted-foreground mt-0.5 flex items-center gap-1.5">
                        {timeAgo(req.createdAt)}
                        {req.isAutomatic && <span className="text-[10px] font-bold bg-orange-100 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400 px-1.5 py-0.5 rounded-full">Auto</span>}
                        {isMe && <span className="text-[10px] font-bold bg-primary/10 text-primary px-1.5 py-0.5 rounded-full">You</span>}
                      </p>
                    </div>

                    <div className="flex items-center gap-2 shrink-0">
                      <StatusBadge status={req.status} />
                      {/* Accept/decline for pending requests to me, even in all-swaps view */}
                      {isPending && req.toUserId === currentUserId && (
                        <div className="flex gap-1.5">
                          <Button size="sm" className="bg-green-600 hover:bg-green-700 text-white font-bold h-7 px-2.5"
                            onClick={() => resolveSwapRequest(req.id, 'accepted')}>
                            <Check size={12} />
                          </Button>
                          <Button size="sm" variant="outline"
                            className="border-border font-bold h-7 px-2.5"
                            onClick={() => resolveSwapRequest(req.id, 'rejected')}>
                            <X size={12} />
                          </Button>
                        </div>
                      )}
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </div>
      )}

      {/* ── My Swaps view ─────────────────────────────────────────────── */}
      {adminView === 'mine' && (
        <>
          {/* Empty state */}
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

          {/* Side-by-side grid: Received | Sent */}
          {hasAny && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 items-start">

              {/* ── Received ──────────────────────────────────────────── */}
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
                            <div className={`w-9 h-9 rounded-lg flex items-center justify-center shrink-0 ${
                              isPending ? 'bg-violet-100 dark:bg-violet-900/50' : 'bg-secondary'
                            }`}>
                              {isPending
                                ? <Bell size={15} className="text-violet-600 dark:text-violet-400" />
                                : req.status === 'accepted'
                                  ? <CheckCircle2 size={15} className="text-green-600" />
                                  : <XCircle size={15} className="text-red-500" />}
                            </div>

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

              {/* ── Sent ──────────────────────────────────────────────── */}
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
                          <div className="w-9 h-9 rounded-lg bg-secondary flex items-center justify-center shrink-0">
                            {req.status === 'pending'
                              ? <Clock size={15} className="text-blue-500" />
                              : req.status === 'accepted'
                                ? <CheckCircle2 size={15} className="text-green-600" />
                                : <XCircle size={15} className="text-red-500" />}
                          </div>

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
        </>
      )}
    </div>
  )
}
