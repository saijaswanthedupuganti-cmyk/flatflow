"use client"
import { useEffect, useState } from 'react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { getPriorityWeight, getTaskUrgency, getTimeCycleContext, getTaskDateInfo, formatDateTime, timeAgo } from '@/lib/rotationEngine'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { CheckCircle2, Clock, Flame, AlertTriangle, AlertCircle, ArrowUpCircle, Repeat, Inbox, Check, X, Copy, Share2 } from 'lucide-react'

export default function DashboardPage() {
  const { members, tasks, activityLog, swapRequests, markTaskCompleted, checkOverdueTasks, returnEarly, createSwapRequest, resolveSwapRequest, markSwapRequestRead } = useFlatStore()
  const { user } = useAuthStore()
  const [swappingTaskId, setSwappingTaskId] = useState<string | null>(null)
  const [selectedSubstituteId, setSelectedSubstituteId] = useState<string>('')
  const [showInvite, setShowInvite] = useState(false)
  const [inviteCopied, setInviteCopied] = useState(false)
  // Admin can switch between their own tasks and the org overview
  const [adminView, setAdminView] = useState<'mine' | 'org'>('org')
  const { flatId } = useFlatStore()

  useEffect(() => {
    checkOverdueTasks()
  }, [checkOverdueTasks])

  const currentUser = user || { uid: 'u1', displayName: 'Sai' }
  const currentMember = members.find(m => m.uid === currentUser.uid)
  const isOutOfStation = currentMember?.status === 'out_of_station'
  const isAdmin = currentMember?.role === 'admin'

  // Get active tasks assigned to current user, sorted by Priority
  const userTasks = tasks
    .filter(t => t.currentAssignedUserId === currentUser.uid && (t.status === 'pending' || t.status === 'overdue'))
    .sort((a, b) => {
      if (a.status === 'overdue' && b.status !== 'overdue') return -1;
      if (b.status === 'overdue' && a.status !== 'overdue') return 1;
      return getPriorityWeight(b.priority) - getPriorityWeight(a.priority);
    })

  // Pending incoming requests to the current user
  const incomingRequests = swapRequests.filter(r => r.toUserId === currentUser.uid && r.status === 'pending')
  const myResolvedRequests = swapRequests.filter(r => r.fromUserId === currentUser.uid && r.status !== 'pending' && !r.read)

  // Available substitutes
  const availableSubstitutes = members.filter(m => 
    m.uid !== currentUser.uid && 
    m.status !== 'out_of_station' && 
    m.status !== 'inactive'
  )


  const upNext = tasks.map(task => {
    const currentAssignee = task.currentAssignedUserId
    const currentIndex = task.queueOrder.indexOf(currentAssignee)
    const nextIndex = (currentIndex + 1) % task.queueOrder.length
    const nextUserId = task.queueOrder[nextIndex]
    
    return {
      taskName: task.name,
      priority: task.priority,
      person: members.find(m => m.uid === nextUserId) || members[0]
    }
  })

  const getUrgencyStyles = (task: any) => {
    const urgency = getTaskUrgency(task)
    if (urgency === 'overdue') return {
      bg: 'bg-gradient-to-br from-red-600 to-red-800',
      text: 'text-white',
      badge: 'bg-red-900/50 text-red-100 border-red-400',
      icon: <AlertTriangle size={20} />,
      label: '⚠️ Past Due'
    }
    if (urgency === 'warning') return {
      bg: 'bg-gradient-to-br from-orange-500 to-amber-600',
      text: 'text-white',
      badge: 'bg-orange-900/50 text-orange-100 border-orange-400',
      icon: <Clock size={20} />,
      label: 'Nearing Deadline'
    }
    return {
      bg: 'bg-gradient-to-br from-primary to-blue-600',
      text: 'text-white',
      badge: 'bg-blue-900/50 text-blue-100 border-blue-400',
      icon: <Clock size={20} />,
      label: 'Due soon'
    }
  }

  const handleCopyInvite = () => {
    if (!flatId) return
    navigator.clipboard.writeText(flatId).then(() => {
      setInviteCopied(true)
      setTimeout(() => setInviteCopied(false), 2000)
    })
  }

  const handleTransferRequest = (taskId: string) => {
    if (!selectedSubstituteId) return;
    createSwapRequest(taskId, currentUser.uid, selectedSubstituteId);
    setSwappingTaskId(null);
    setSelectedSubstituteId('');
  }

  const now = new Date()
  const greeting = now.getHours() < 12 ? 'Good morning' : now.getHours() < 17 ? 'Good afternoon' : 'Good evening'

  return (
    <div className="space-y-6">
      {/* ── Page Header ── */}
      <div className="flex flex-col md:flex-row md:items-start justify-between gap-4">
        <div>
          <p className="text-sm font-medium text-muted-foreground mb-1">{greeting} 👋</p>
          <h1 className="text-3xl font-extrabold tracking-tight leading-tight">
            {isAdmin
              ? adminView === 'org' ? 'Flat Overview' : currentUser.displayName
              : currentUser.displayName}
          </h1>
          <p className="text-muted-foreground mt-1 text-sm">
            {isAdmin
              ? adminView === 'org' ? 'Global duty roster and flat management.' : 'Your personal duties in the rotation.'
              : 'Your duties and flat activity for today.'}
          </p>
        </div>
        <div className="flex items-center gap-2 flex-wrap shrink-0">
          {isAdmin && (
            <div className="flex items-center bg-secondary/80 border border-border/60 rounded-xl p-1 gap-0.5">
              <button
                onClick={() => setAdminView('mine')}
                className={`px-3 py-1.5 rounded-lg text-sm font-semibold transition-all ${
                  adminView === 'mine' ? 'bg-primary text-white shadow-sm' : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                My Tasks
              </button>
              <button
                onClick={() => setAdminView('org')}
                className={`px-3 py-1.5 rounded-lg text-sm font-semibold transition-all ${
                  adminView === 'org' ? 'bg-primary text-white shadow-sm' : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                Org View
              </button>
            </div>
          )}
          {isAdmin && !isOutOfStation && (
            <Button
              size="sm"
              variant="outline"
              className="border-border/60 font-semibold"
              onClick={() => setShowInvite(!showInvite)}
            >
              <Share2 size={14} className="mr-1.5" /> Invite
            </Button>
          )}
        </div>
      </div>

      {/* Invite Panel */}
      {showInvite && (
        <Card className="border-primary/30 bg-primary/5 shadow-sm">
          <CardContent className="p-5 flex flex-col md:flex-row items-start md:items-center gap-4 justify-between">
            <div>
              <p className="font-bold text-primary">Share this invite code with your roommates</p>
              <p className="text-sm text-muted-foreground mt-0.5">They can enter this on the Join page to get added to your flat.</p>
            </div>
            <div className="flex items-center gap-3 bg-card border border-border rounded-xl px-4 py-2.5 shadow-sm">
              <code className="text-xl font-mono font-bold text-primary tracking-widest">
                {flatId || 'FLAT-1234'}
              </code>
              <Button size="sm" variant="outline" onClick={handleCopyInvite} className="gap-1.5 shrink-0">
                {inviteCopied ? <><Check size={14} className="text-green-500" /> Copied!</> : <><Copy size={14} /> Copy</>}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        
        {!isAdmin && incomingRequests.length > 0 && (
          <div className="col-span-1 md:col-span-3 space-y-4 mb-4">
            <h2 className="text-xl font-bold flex items-center gap-2">
              <Inbox size={20} /> Action Required ({incomingRequests.length})
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {incomingRequests.map(req => {
                const task = tasks.find(t => t.taskId === req.taskId)
                const fromUser = members.find(m => m.uid === req.fromUserId)
                if (!task || !fromUser) return null;

                return (
                  <Card key={req.id} className="border-purple-200 bg-purple-50 shadow-sm">
                    <CardContent className="p-4 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                      <div>
                        <div className="font-bold text-purple-900">{fromUser.nickname} requested a swap</div>
                        <p className="text-sm text-purple-700">Can you cover <strong>{task.name}</strong> for them?</p>
                      </div>
                      <div className="flex gap-2 w-full md:w-auto">
                        <Button size="sm" className="bg-purple-600 hover:bg-purple-700 text-white flex-1 md:flex-none" onClick={() => resolveSwapRequest(req.id, 'accepted')}>
                          <Check size={16} className="mr-1" /> Accept
                        </Button>
                        <Button size="sm" variant="outline" className="text-purple-700 border-purple-200 hover:bg-purple-100 flex-1 md:flex-none" onClick={() => resolveSwapRequest(req.id, 'rejected')}>
                          <X size={16} className="mr-1" /> Decline
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                )
              })}
            </div>
          </div>
        )}

        {/* SWAP REQUEST ALERTS (Resolved Responses) */}
        {myResolvedRequests.length > 0 && (
          <div className="col-span-1 md:col-span-3 space-y-3">
            {myResolvedRequests.map(req => {
              const task = tasks.find(t => t.taskId === req.taskId)
              const targetUser = members.find(m => m.uid === req.toUserId)
              if (!task || !targetUser) return null;
              
              const isAccepted = req.status === 'accepted'

              return (
                <div key={req.id} className={`flex flex-col md:flex-row items-center justify-between p-4 rounded-xl border shadow-sm ${
                  isAccepted ? 'bg-green-500/10 border-green-500/20 text-green-700 dark:text-green-400' : 'bg-red-500/10 border-red-500/20 text-red-700 dark:text-red-400'
                }`}>
                  <div className="flex items-center gap-3">
                    {isAccepted ? <CheckCircle2 className="w-6 h-6 text-green-500" /> : <X className="w-6 h-6 text-red-500" />}
                    <div>
                      <h3 className="font-bold">Swap Request {isAccepted ? 'Accepted' : 'Declined'}</h3>
                      <p className="text-sm opacity-90">
                        {targetUser.nickname} has <strong>{req.status}</strong> your request to cover <strong>{task.name}</strong>.
                      </p>
                    </div>
                  </div>
                  <Button 
                    variant="outline" 
                    size="sm"
                    className={`mt-3 md:mt-0 ${isAccepted ? 'border-green-500/50 hover:bg-green-500/20' : 'border-red-500/50 hover:bg-red-500/20'}`}
                    onClick={() => markSwapRequestRead(req.id)}
                  >
                    Dismiss
                  </Button>
                </div>
              )
            })}
          </div>
        )}

        {(isAdmin ? adminView === 'mine' : false) && isOutOfStation ? (
          <Card className="col-span-1 md:col-span-3 bg-gradient-to-br from-orange-500 to-amber-600 text-white shadow-md">
            <CardContent className="p-8 flex flex-col md:flex-row items-center justify-between gap-6">
              <div>
                <h2 className="text-2xl font-bold">You are marked as Out of Station</h2>
                <p className="text-orange-100 mt-1">The smart rotation engine is currently skipping your duties.</p>
              </div>
              <Button size="lg" variant="secondary" className="font-bold text-orange-600 w-full md:w-auto" onClick={() => returnEarly(currentUser.uid)}>
                I'm Back (Return Early)
              </Button>
            </CardContent>
          </Card>
        ) : !isAdmin && isOutOfStation ? (
          <Card className="col-span-1 md:col-span-3 bg-gradient-to-br from-orange-500 to-amber-600 text-white shadow-md">
            <CardContent className="p-8 flex flex-col md:flex-row items-center justify-between gap-6">
              <div>
                <h2 className="text-2xl font-bold">You are marked as Out of Station</h2>
                <p className="text-orange-100 mt-1">The smart rotation engine is currently skipping your duties.</p>
              </div>
              <Button size="lg" variant="secondary" className="font-bold text-orange-600 w-full md:w-auto" onClick={() => returnEarly(currentUser.uid)}>
                I'm Back (Return Early)
              </Button>
            </CardContent>
          </Card>
        ) : (!isAdmin || adminView === 'mine') && !isOutOfStation ? (
          <div className="col-span-1 md:col-span-3">
            <div className="flex items-center justify-between mb-4 mt-2">
              <h2 className="text-xl font-bold">Your Pending Duties</h2>
              <span className="bg-primary/10 text-primary px-3 py-1 rounded-full text-sm font-bold">
                {userTasks.length} Assigned
              </span>
            </div>

            {userTasks.length === 0 ? (
               <Card className="bg-secondary/30 border-dashed border-2">
                 <CardContent className="flex flex-col items-center justify-center p-12">
                   <CheckCircle2 size={48} className="mb-4 text-green-500 opacity-80" />
                   <div className="text-2xl font-bold">All clear!</div>
                   <p className="text-muted-foreground">You have no pending tasks right now.</p>
                 </CardContent>
               </Card>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {userTasks.map((task) => {
                  const style = getUrgencyStyles(task)
                  const dateInfo = getTaskDateInfo(task)
                  const isSwapping = swappingTaskId === task.taskId
                  const hasPendingRequest = swapRequests.some(r => r.taskId === task.taskId && r.status === 'pending')

                  return (
                    <Card key={task.taskId} className={`${style.bg} ${style.text} border-none shadow-md transition-all hover:scale-[1.02]`}>
                      <CardHeader className="pb-1">
                        <CardTitle className="text-base font-medium flex items-center justify-between">
                          <span className="flex items-center gap-2">
                            {style.icon}
                            <span className="text-xs font-bold uppercase tracking-wider opacity-80">
                              {task.frequency} duty
                            </span>
                          </span>
                          {task.priority === 'high' && (
                            <span className={`text-[10px] px-2 py-0.5 rounded uppercase font-bold tracking-wider border flex items-center gap-1 ${style.badge}`}>
                              <ArrowUpCircle size={12} /> High
                            </span>
                          )}
                        </CardTitle>
                      </CardHeader>
                      <CardContent>
                        {/* Task name */}
                        <div className="text-2xl font-bold mt-1 leading-tight">{task.name}</div>

                        {/* Overdue accountability block */}
                        {dateInfo.isOverdue ? (
                          <div className="mt-3 bg-black/20 rounded-lg p-3 border border-white/20 space-y-1">
                            <p className="text-xs font-extrabold uppercase tracking-wider text-white/90">
                              ⚠️ {dateInfo.overdueLabel}
                            </p>
                            <p className="text-xs text-white/75">
                              Was due: <span className="font-bold">{dateInfo.originalDueFormatted}</span>
                            </p>
                            <p className="text-xs text-white/75">
                              Complete it now → next person starts a fresh {task.frequency} cycle from today
                            </p>
                          </div>
                        ) : (
                          <div className="mt-2 space-y-1">
                            <p className="text-xs text-white/80 font-medium">
                              {dateInfo.cycleLabel}
                            </p>
                            <p className="text-xs text-white/70">
                              Due: <span className="font-bold">{dateInfo.dueDateFormatted}</span>
                            </p>
                          </div>
                        )}

                        {/* Last completed info */}
                        <p className="text-[11px] text-white/60 mt-1">
                          Last done: {dateInfo.lastCompletedFormatted}
                        </p>

                        {/* Action area */}
                        <div className="mt-4">
                          {hasPendingRequest ? (
                            <div className="bg-background/20 p-3 rounded-lg border border-background/20 text-center">
                              <Clock className="mx-auto mb-2 opacity-80" size={22} />
                              <p className="text-sm font-bold text-white">Swap Request Pending</p>
                              <p className="text-xs text-white/70">Waiting for roommate to accept</p>
                            </div>
                          ) : isSwapping ? (
                            <div className="bg-background/20 p-3 rounded-lg border border-background/20 space-y-3">
                              <p className="text-xs font-bold uppercase tracking-wider text-white">Request Swap</p>
                              <select
                                value={selectedSubstituteId}
                                onChange={(e) => setSelectedSubstituteId(e.target.value)}
                                className="w-full bg-background text-foreground border-none rounded-md px-3 py-2 text-sm"
                              >
                                <option value="" disabled>Select roommate</option>
                                {availableSubstitutes.map(sub => (
                                  <option key={sub.uid} value={sub.uid}>{sub.nickname}</option>
                                ))}
                                {availableSubstitutes.length === 0 && (
                                  <option value="none" disabled>No one available</option>
                                )}
                              </select>
                              <div className="flex gap-2">
                                <Button
                                  variant="secondary"
                                  className="flex-1 font-bold text-foreground"
                                  onClick={() => handleTransferRequest(task.taskId)}
                                  disabled={!selectedSubstituteId}
                                >
                                  Send Request
                                </Button>
                                <Button
                                  variant="outline"
                                  className="flex-1 bg-transparent border-white/30 text-white hover:bg-white/10"
                                  onClick={() => setSwappingTaskId(null)}
                                >
                                  Cancel
                                </Button>
                              </div>
                            </div>
                          ) : (
                            <div className="flex gap-2">
                              <Button
                                variant="secondary"
                                className="flex-1 font-bold shadow-sm text-foreground"
                                onClick={() => markTaskCompleted(task.taskId, currentUser.uid)}
                              >
                                <CheckCircle2 size={16} className="mr-1.5" />
                                Done
                              </Button>
                              <Button
                                variant="outline"
                                size="icon"
                                className="bg-transparent border-white/30 text-white hover:bg-white/10 hover:text-white shrink-0"
                                onClick={() => setSwappingTaskId(task.taskId)}
                                title="Ask someone to cover this duty"
                              >
                                <Repeat size={18} />
                              </Button>
                            </div>
                          )}
                        </div>
                      </CardContent>
                    </Card>
                  )
                })}
              </div>
            )}
          </div>
        ) : adminView === 'org' ? (
          <div className="col-span-1 md:col-span-3">
            <h2 className="text-xl font-bold mb-4 mt-2">Active Roster (This Week)</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              {tasks.map(task => {
                const assignee = members.find(m => m.uid === task.currentAssignedUserId)
                const dateInfo = getTaskDateInfo(task)
                return (
                  <Card key={task.taskId} className={`shadow-sm overflow-hidden ${dateInfo.isOverdue ? 'border-red-200 dark:border-red-900' : ''}`}>
                    <CardHeader className="pb-2">
                      <CardTitle className="text-sm font-medium text-muted-foreground flex items-center justify-between">
                        <span>{task.name}</span>
                        {dateInfo.isOverdue && (
                          <span className="text-[10px] bg-red-100 dark:bg-red-900/50 text-red-600 dark:text-red-400 px-1.5 py-0.5 rounded font-bold">
                            {dateInfo.overdueDays}d late
                          </span>
                        )}
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center font-bold text-primary shrink-0">
                          {assignee?.nickname.charAt(0) || '?'}
                        </div>
                        <div className="min-w-0">
                          <div className="font-bold">{assignee?.nickname || 'Unassigned'}</div>
                          <span className={`text-xs font-bold uppercase tracking-wider ${
                            task.status === 'overdue' ? 'text-red-500' : 'text-blue-500'
                          }`}>
                            {task.status}
                          </span>
                        </div>
                      </div>
                      <div className="mt-3 pt-3 border-t border-border space-y-1">
                        <p className="text-xs text-muted-foreground">
                          Due: <span className="font-semibold text-foreground">{dateInfo.dueDateFormatted}</span>
                        </p>
                        <p className="text-xs text-muted-foreground">
                          Last done: <span className="font-semibold text-foreground">{dateInfo.lastCompletedFormatted}</span>
                        </p>
                        <p className="text-xs text-muted-foreground">{dateInfo.cycleLabel}</p>
                      </div>
                    </CardContent>
                  </Card>
                )
              })}
            </div>
          </div>
        ) : null}
      </div>
      
      {/* ── Stats Row ── */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mt-8 pt-6 border-t border-border/60">
        {(!isAdmin || adminView === 'mine') && (
          <Card className="shadow-sm border-border/60 col-span-1">
            <CardContent className="p-4">
              <div className="flex items-center gap-2 mb-2">
                <Flame size={16} className="text-orange-500" />
                <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Reliability</span>
              </div>
              <div className="text-3xl font-extrabold">{currentMember?.reliabilityScore ?? 100}</div>
              <div className="w-full bg-secondary rounded-full h-1.5 mt-2">
                <div className="h-1.5 rounded-full bg-orange-400 transition-all" style={{ width: `${currentMember?.reliabilityScore ?? 100}%` }} />
              </div>
            </CardContent>
          </Card>
        )}
        <Card className="shadow-sm border-border/60 col-span-1">
          <CardContent className="p-4">
            <div className="flex items-center gap-2 mb-2">
              <CheckCircle2 size={16} className="text-green-500" />
              <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Flat Health</span>
            </div>
            <div className="text-3xl font-extrabold">
              {Math.round((tasks.filter(t => t.status !== 'overdue').length / (tasks.length || 1)) * 100)}%
            </div>
            <p className="text-xs text-muted-foreground mt-1">{tasks.filter(t => t.status === 'overdue').length} overdue</p>
          </CardContent>
        </Card>
        <Card className="shadow-sm border-border/60 col-span-1">
          <CardContent className="p-4">
            <div className="flex items-center gap-2 mb-2">
              <AlertCircle size={16} className="text-blue-500" />
              <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Total Tasks</span>
            </div>
            <div className="text-3xl font-extrabold">{tasks.length}</div>
            <p className="text-xs text-muted-foreground mt-1">{members.filter(m => m.status === 'available').length} members active</p>
          </CardContent>
        </Card>
        <Card className="shadow-sm border-border/60 col-span-1">
          <CardContent className="p-4">
            <div className="flex items-center gap-2 mb-2">
              <Inbox size={16} className="text-purple-500" />
              <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Pending Swaps</span>
            </div>
            <div className="text-3xl font-extrabold">
              {swapRequests.filter(r => r.status === 'pending').length}
            </div>
            <p className="text-xs text-muted-foreground mt-1">swap requests open</p>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
        {isAdmin && (
          <Card className="shadow-sm border-border">
            <CardHeader>
              <CardTitle>Up Next in Queue</CardTitle>
              <CardDescription>Who's doing what next</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {upNext.map((item, i) => (
                  <div key={i} className="flex items-center justify-between p-3 bg-secondary/50 rounded-lg">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-700 font-bold">
                        {item.person.nickname.charAt(0)}
                      </div>
                      <div>
                        <div className="font-semibold flex items-center gap-2">
                          {item.person.nickname}
                          {item.priority === 'high' && <AlertCircle size={12} className="text-red-500" />}
                        </div>
                        <div className="text-xs text-muted-foreground">{item.taskName}</div>
                      </div>
                    </div>
                    <div className="text-sm font-medium text-muted-foreground">Up Next</div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}

        <Card className={`shadow-sm border-border ${!isAdmin ? 'lg:col-span-2' : ''}`}>
          <CardHeader>
            <CardTitle>Recent Activity</CardTitle>
            <CardDescription>System of record</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4 border-l-2 border-muted ml-3 pl-4 relative">
              {activityLog.slice(0, 8).map((activity) => {
                const isCompleted = activity.action === 'completed_task'
                const isStatus = activity.action === 'status_change'
                const isSkipped = activity.action === 'skipped_task'
                const isTransfer = activity.action === 'transferred_task' || activity.action === 'swap_resolved'
                const isSystem = activity.userId === 'system'
                const userObj = members.find(m => m.uid === activity.userId)
                const actDate = new Date(activity.timestamp)

                return (
                  <div key={activity.id} className="relative">
                    <div className={`absolute -left-[21px] top-1 w-3 h-3 rounded-full border-2 border-card ${
                      isSystem ? 'bg-orange-500' :
                      isCompleted ? 'bg-green-500' :
                      isStatus ? 'bg-blue-500' :
                      isSkipped ? 'bg-red-500' :
                      isTransfer ? 'bg-purple-500' : 'bg-green-500'
                    }`} />
                    <p className="text-sm">
                      <span className="font-semibold">{isSystem ? 'System' : userObj?.nickname || 'Someone'}</span>{' '}
                      {activity.details}.
                    </p>
                    <p className="text-xs text-muted-foreground mt-0.5" title={formatDateTime(actDate)}>
                      {timeAgo(activity.timestamp)} · {actDate.toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })} {actDate.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}
                    </p>
                  </div>
                )
              })}
              {activityLog.length === 0 && (
                <p className="text-sm text-muted-foreground">No recent activity.</p>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
