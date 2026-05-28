"use client"
import { use } from 'react'
import { useRouter } from 'next/navigation'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { ArrowLeft, CheckCircle2, Flame, CalendarDays, ShieldCheck, Mail, Home, Activity } from 'lucide-react'

// Simple helper to check if two dates are in the same month/year
const isSameMonth = (d1: Date, d2: Date) => 
  d1.getMonth() === d2.getMonth() && d1.getFullYear() === d2.getFullYear()

// Simple helper to check if two dates are in the same week (approximate)
const isSameWeek = (d1: Date, d2: Date) => {
  const diffTime = Math.abs(d2.getTime() - d1.getTime());
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)); 
  return diffDays <= 7 && Math.abs(d1.getDay() - d2.getDay()) <= 7;
}

export default function MemberProfilePage({ params }: { params: Promise<{ uid: string }> }) {
  const router = useRouter()
  const { uid } = use(params)

  const { members, tasks, activityLog, name: flatName, flatId } = useFlatStore()
  const { user, allFlats } = useAuthStore()
  const member = members.find(m => m.uid === uid)
  const isSelf = user?.uid === uid

  if (!member) {
    return (
      <div className="flex flex-col items-center justify-center h-64">
        <h2 className="text-2xl font-bold">Member not found</h2>
        <Button onClick={() => router.back()} className="mt-4">Go Back</Button>
      </div>
    )
  }

  const now = new Date()

  // Calculate detailed analytics per task
  const taskAnalytics = tasks.map(task => {
    // Filter activities where this member completed THIS specific task
    const completions = activityLog.filter(a => 
      a.userId === uid && 
      a.action === 'completed_task' && 
      a.details.includes(task.name)
    )

    const thisWeek = completions.filter(c => isSameWeek(new Date(c.timestamp), now)).length
    const thisMonth = completions.filter(c => isSameMonth(new Date(c.timestamp), now)).length
    const allTime = completions.length

    return {
      taskId: task.taskId,
      name: task.name,
      thisWeek,
      thisMonth,
      allTime
    }
  })

  const totalCompletionsThisMonth = taskAnalytics.reduce((acc, t) => acc + t.thisMonth, 0)
  const totalCompletionsAllTime = taskAnalytics.reduce((acc, t) => acc + t.allTime, 0)

  const statusLabel = member.status.replace(/_/g, ' ')
  const statusColour =
    member.status === 'available'      ? 'text-green-600 dark:text-green-400' :
    member.status === 'out_of_station' ? 'text-orange-500' :
    member.status === 'busy'           ? 'text-blue-500'   : 'text-muted-foreground'

  return (
    <div className="space-y-6 max-w-3xl">

      {/* Back */}
      <button onClick={() => router.back()} className="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground transition-colors">
        <ArrowLeft size={15} /> Back to Members
      </button>

      {/* ── Profile card ── */}
      <Card className="shadow-sm">
        <CardContent className="p-6">
          {/* Header row */}
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 rounded-full bg-primary flex items-center justify-center text-2xl font-bold text-white shrink-0">
              {member.nickname.charAt(0).toUpperCase()}
            </div>
            <div className="min-w-0">
              <h1 className="text-2xl font-extrabold tracking-tight leading-tight">{member.nickname}</h1>
              <div className="flex items-center gap-1.5 mt-0.5">
                <ShieldCheck size={13} className={member.role === 'admin' ? 'text-primary' : 'text-muted-foreground'} />
                <span className={`text-xs font-semibold capitalize ${member.role === 'admin' ? 'text-primary' : 'text-muted-foreground'}`}>
                  {member.role}
                </span>
                {isSelf && (
                  <span className="text-[10px] font-bold bg-primary/10 text-primary px-2 py-0.5 rounded-full ml-1">You</span>
                )}
              </div>
            </div>
          </div>

          {/* Detail rows */}
          <div className="mt-5 divide-y divide-border rounded-xl border border-border overflow-hidden">
            {isSelf && user?.email && (
              <div className="flex items-center gap-3 px-4 py-3">
                <Mail size={14} className="text-muted-foreground shrink-0" />
                <span className="text-xs text-muted-foreground w-24 shrink-0">Email</span>
                <span className="text-sm font-medium truncate">{user.email}</span>
              </div>
            )}
            <div className="flex items-center gap-3 px-4 py-3">
              <Activity size={14} className="text-muted-foreground shrink-0" />
              <span className="text-xs text-muted-foreground w-24 shrink-0">Status</span>
              <span className={`text-sm font-semibold capitalize ${statusColour}`}>{statusLabel}</span>
            </div>
            <div className="flex items-center gap-3 px-4 py-3">
              <Flame size={14} className="text-orange-500 shrink-0" />
              <span className="text-xs text-muted-foreground w-24 shrink-0">Reliability</span>
              <span className="text-sm font-bold text-primary">{member.reliabilityScore ?? 100}</span>
            </div>
            <div className="flex items-center gap-3 px-4 py-3">
              <CheckCircle2 size={14} className="text-green-500 shrink-0" />
              <span className="text-xs text-muted-foreground w-24 shrink-0">Tasks done</span>
              <span className="text-sm font-semibold">{totalCompletionsAllTime} all time</span>
            </div>
            <div className="flex items-center gap-3 px-4 py-3">
              <Home size={14} className="text-muted-foreground shrink-0" />
              <span className="text-xs text-muted-foreground w-24 shrink-0">
                {isSelf && allFlats.length > 1 ? 'Flats' : 'Flat'}
              </span>
              <span className="text-sm font-medium truncate">
                {isSelf && allFlats.length > 1
                  ? allFlats.map(f => f.name || f.id).join(', ')
                  : flatName || flatId || '—'}
              </span>
            </div>
            <div className="flex items-center gap-3 px-4 py-3">
              <CalendarDays size={14} className="text-muted-foreground shrink-0" />
              <span className="text-xs text-muted-foreground w-24 shrink-0">Joined</span>
              <span className="text-sm font-medium">
                {member.joinedAt
                  ? new Date(member.joinedAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })
                  : '—'}
              </span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* ── Duty analytics ── */}
      <Card className="shadow-sm">
        <CardHeader className="pb-3">
          <CardTitle className="flex items-center gap-2 text-base">
            <CalendarDays size={16} /> Duty History
          </CardTitle>
          <CardDescription>Breakdown of completed household duties.</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="text-xs text-muted-foreground uppercase bg-secondary/50">
                <tr>
                  <th className="px-4 py-3 rounded-tl-lg">Task</th>
                  <th className="px-4 py-3 text-center">This Week</th>
                  <th className="px-4 py-3 text-center">This Month</th>
                  <th className="px-4 py-3 text-center rounded-tr-lg">All Time</th>
                </tr>
              </thead>
              <tbody>
                {taskAnalytics.map(a => (
                  <tr key={a.taskId} className="border-b border-border last:border-0 hover:bg-secondary/20 transition-colors">
                    <td className="px-4 py-3 font-medium">{a.name}</td>
                    <td className="px-4 py-3 text-center font-semibold text-primary">{a.thisWeek}</td>
                    <td className="px-4 py-3 text-center font-bold">{a.thisMonth}</td>
                    <td className="px-4 py-3 text-center text-muted-foreground">{a.allTime}</td>
                  </tr>
                ))}
                <tr className="bg-primary/5 font-bold">
                  <td className="px-4 py-3 rounded-bl-lg">Total</td>
                  <td className="px-4 py-3 text-center text-primary">{taskAnalytics.reduce((s, t) => s + t.thisWeek, 0)}</td>
                  <td className="px-4 py-3 text-center">{totalCompletionsThisMonth}</td>
                  <td className="px-4 py-3 text-center rounded-br-lg">{totalCompletionsAllTime}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
