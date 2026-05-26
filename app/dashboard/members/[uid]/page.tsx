"use client"
import { use } from 'react'
import { useRouter } from 'next/navigation'
import { useFlatStore } from '@/store/useFlatStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { ArrowLeft, UserCircle, CheckCircle2, Flame, CalendarDays } from 'lucide-react'

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
  
  const { members, tasks, activityLog } = useFlatStore()
  const member = members.find(m => m.uid === uid)

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

  return (
    <div className="space-y-6">
      <Button variant="ghost" className="mb-2 -ml-4 text-muted-foreground" onClick={() => router.back()}>
        <ArrowLeft className="mr-2" size={16} /> Back to Members
      </Button>

      <div className="flex flex-col md:flex-row gap-6 items-start">
        {/* Profile Summary Card */}
        <Card className="w-full md:w-1/3 shadow-md border-none bg-gradient-to-br from-primary/10 to-transparent">
          <CardContent className="p-8 flex flex-col items-center text-center">
            <div className="w-24 h-24 rounded-full flex items-center justify-center text-4xl font-bold bg-primary text-primary-foreground shadow-lg mb-4">
              {member.nickname.charAt(0)}
            </div>
            <h1 className="text-3xl font-bold">{member.nickname}</h1>
            <p className="text-muted-foreground mt-1 capitalize font-medium">{member.role}</p>
            
            <div className="w-full grid grid-cols-2 gap-4 mt-8 pt-8 border-t border-border">
              <div className="flex flex-col items-center">
                <Flame size={24} className="text-orange-500 mb-2" />
                <div className="text-2xl font-bold">{member.reliabilityScore}</div>
                <div className="text-xs text-muted-foreground uppercase tracking-wider">Score</div>
              </div>
              <div className="flex flex-col items-center">
                <CheckCircle2 size={24} className="text-green-500 mb-2" />
                <div className="text-2xl font-bold">{totalCompletionsAllTime}</div>
                <div className="text-xs text-muted-foreground uppercase tracking-wider">Tasks Done</div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Detailed Analytics Table */}
        <Card className="w-full md:w-2/3 shadow-sm">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <CalendarDays size={20} /> Duty Analytics
            </CardTitle>
            <CardDescription>Historical breakdown of completed household duties.</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto">
              <table className="w-full text-sm text-left">
                <thead className="text-xs text-muted-foreground uppercase bg-secondary/50">
                  <tr>
                    <th className="px-4 py-3 rounded-tl-lg">Task Name</th>
                    <th className="px-4 py-3 text-center">This Week</th>
                    <th className="px-4 py-3 text-center">This Month</th>
                    <th className="px-4 py-3 text-center rounded-tr-lg">All Time</th>
                  </tr>
                </thead>
                <tbody>
                  {taskAnalytics.map((analytics, i) => (
                    <tr key={analytics.taskId} className="border-b border-border last:border-0 hover:bg-secondary/20 transition-colors">
                      <td className="px-4 py-4 font-medium">{analytics.name}</td>
                      <td className="px-4 py-4 text-center font-semibold text-primary">{analytics.thisWeek}</td>
                      <td className="px-4 py-4 text-center font-bold">{analytics.thisMonth}</td>
                      <td className="px-4 py-4 text-center text-muted-foreground">{analytics.allTime}</td>
                    </tr>
                  ))}
                  <tr className="bg-primary/5 font-bold">
                    <td className="px-4 py-4 rounded-bl-lg">Total Operations</td>
                    <td className="px-4 py-4 text-center text-primary">{taskAnalytics.reduce((acc, t) => acc + t.thisWeek, 0)}</td>
                    <td className="px-4 py-4 text-center">{totalCompletionsThisMonth}</td>
                    <td className="px-4 py-4 text-center rounded-br-lg">{totalCompletionsAllTime}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
