"use client"
import { useState } from 'react'
import { useFlatStore, Member } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  Users, MapPinOff, CheckCircle2, ShieldCheck, Star,
  ClipboardList, UserCheck, UserX, TrendingUp, UserMinus, X, Filter,
} from 'lucide-react'

// ── Inline confirm dialog ────────────────────────────────────────────────
interface KickDialogProps {
  member: Member | null
  onClose: () => void
  onConfirm: () => void
  loading: boolean
  error: string
}
function KickDialog({ member, onClose, onConfirm, loading, error }: KickDialogProps) {
  if (!member) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
      <div className="bg-card border border-border rounded-2xl shadow-xl w-full max-w-sm p-6 space-y-4">
        <div className="flex items-start justify-between">
          <div>
            <h3 className="font-bold text-lg">Remove {member.nickname}?</h3>
            <p className="text-sm text-muted-foreground mt-1">
              They will lose all access to this flat. Their tasks will be reassigned automatically.
            </p>
          </div>
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground ml-2 shrink-0">
            <X size={18} />
          </button>
        </div>
        {error && <p className="text-sm text-destructive font-medium">{error}</p>}
        <div className="flex gap-3 pt-1">
          <Button variant="outline" className="flex-1" onClick={onClose} disabled={loading}>Cancel</Button>
          <Button
            className="flex-1 font-semibold bg-destructive hover:bg-destructive/90 text-white"
            onClick={onConfirm}
            disabled={loading}
          >
            {loading ? 'Removing…' : `Remove ${member.nickname}`}
          </Button>
        </div>
      </div>
    </div>
  )
}

export default function MembersPage() {
  const { members, tasks, changeMemberStatus, kickMember } = useFlatStore()
  const { user } = useAuthStore()

  const currentUserId = user?.uid || 'u1'
  const currentMember = members.find(m => m.uid === currentUserId)
  const isAdmin = currentMember?.role === 'admin'

  const [showActiveOnly, setShowActiveOnly] = useState(false)
  const [kickTarget, setKickTarget] = useState<Member | null>(null)
  const [kickLoading, setKickLoading] = useState(false)
  const [kickError, setKickError] = useState('')

  const toggleStatus = (member: Member) => {
    const newStatus = member.status === 'out_of_station' ? 'available' : 'out_of_station'
    changeMemberStatus(member.uid, newStatus)
  }

  const handleKickConfirm = async () => {
    if (!kickTarget || !user) return
    setKickLoading(true)
    setKickError('')
    try {
      await kickMember(kickTarget.uid, user.uid)
      setKickTarget(null)
    } catch (e: unknown) {
      setKickError(e instanceof Error ? e.message : 'Failed to remove member. Please try again.')
    } finally {
      setKickLoading(false)
    }
  }

  const getAssignedTaskCount = (uid: string) =>
    tasks.filter(t => t.currentAssignedUserId === uid && (t.status === 'pending' || t.status === 'overdue')).length

  const getQueueCount = (uid: string) =>
    tasks.filter(t => t.queueOrder.includes(uid)).length

  const available  = members.filter(m => m.status === 'available').length
  const absent     = members.filter(m => m.status === 'out_of_station').length
  const avgScore   = members.length
    ? Math.round(members.reduce((s, m) => s + (m.reliabilityScore ?? 100), 0) / members.length)
    : 100
  const topPerformer = [...members].sort(
    (a, b) => (b.reliabilityScore ?? 100) - (a.reliabilityScore ?? 100)
  )[0]

  const STATUS_CONFIG = {
    available:       { label: 'Available',        icon: CheckCircle2, text: 'text-green-600 dark:text-green-400',   bg: 'bg-green-100 dark:bg-green-900/30',   dot: 'bg-green-500' },
    out_of_station:  { label: 'Out of Station',   icon: MapPinOff,    text: 'text-orange-600 dark:text-orange-400', bg: 'bg-orange-100 dark:bg-orange-900/30', dot: 'bg-orange-500' },
    busy:            { label: 'Busy',             icon: UserCheck,    text: 'text-blue-600 dark:text-blue-400',     bg: 'bg-blue-100 dark:bg-blue-900/30',     dot: 'bg-blue-500' },
    inactive:        { label: 'Inactive',         icon: UserX,        text: 'text-muted-foreground',                bg: 'bg-secondary',                         dot: 'bg-muted-foreground' },
  }

  const AVATAR_COLOURS = [
    'from-blue-500 to-indigo-600',
    'from-violet-500 to-purple-700',
    'from-cyan-500 to-blue-600',
    'from-orange-500 to-amber-600',
    'from-emerald-500 to-teal-600',
    'from-pink-500 to-rose-600',
  ]

  return (
    <>
      <KickDialog
        member={kickTarget}
        onClose={() => { setKickTarget(null); setKickError('') }}
        onConfirm={handleKickConfirm}
        loading={kickLoading}
        error={kickError}
      />

      <div className="space-y-8 max-w-4xl">

        {/* ── Header ─────────────────────────────────────────────────── */}
        <div>
          <p className="text-xs font-bold text-muted-foreground uppercase tracking-widest mb-1">Admin</p>
          <h1 className="text-3xl font-extrabold tracking-tight">Members</h1>
          <p className="text-muted-foreground mt-1 text-sm">
            Manage roommate statuses and availability in the rotation.
          </p>
        </div>

        {/* ── Summary stats ───────────────────────────────────────────── */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          <Card className="shadow-sm border-border/60">
            <CardContent className="p-4">
              <div className="flex items-center gap-2 mb-2">
                <Users size={15} className="text-blue-500" />
                <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Total</span>
              </div>
              <div className="text-3xl font-extrabold">{members.length}</div>
              <p className="text-xs text-muted-foreground mt-1">flatmates</p>
            </CardContent>
          </Card>
          <Card className="shadow-sm border-border/60">
            <CardContent className="p-4">
              <div className="flex items-center gap-2 mb-2">
                <CheckCircle2 size={15} className="text-green-500" />
                <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Available</span>
              </div>
              <div className="text-3xl font-extrabold text-green-600 dark:text-green-400">{available}</div>
              <p className="text-xs text-muted-foreground mt-1">{absent} absent</p>
            </CardContent>
          </Card>
          <Card className="shadow-sm border-border/60">
            <CardContent className="p-4">
              <div className="flex items-center gap-2 mb-2">
                <Star size={15} className="text-orange-500" />
                <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Avg Score</span>
              </div>
              <div className="text-3xl font-extrabold">{avgScore}</div>
              <div className="w-full bg-secondary rounded-full h-1.5 mt-2">
                <div className="h-1.5 rounded-full bg-orange-400" style={{ width: `${avgScore}%` }} />
              </div>
            </CardContent>
          </Card>
          <Card className="shadow-sm border-border/60">
            <CardContent className="p-4">
              <div className="flex items-center gap-2 mb-2">
                <TrendingUp size={15} className="text-purple-500" />
                <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Top Member</span>
              </div>
              <div className="text-xl font-extrabold truncate">{topPerformer?.nickname ?? '—'}</div>
              <p className="text-xs text-muted-foreground mt-1">{topPerformer?.reliabilityScore ?? 100} score</p>
            </CardContent>
          </Card>
        </div>

        {/* ── Member Cards ────────────────────────────────────────────── */}
        {/* Filter bar */}
        <div className="flex items-center justify-between">
          <p className="text-sm font-semibold text-muted-foreground">
            {members.length} flatmate{members.length !== 1 ? 's' : ''}
            {showActiveOnly && <span className="ml-1 text-green-600 dark:text-green-400">· showing active only</span>}
          </p>
          <button
            onClick={() => setShowActiveOnly(v => !v)}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-sm font-bold border transition-all ${
              showActiveOnly
                ? 'bg-green-600 text-white border-green-600 shadow-sm'
                : 'bg-background text-muted-foreground border-border hover:bg-secondary/80'
            }`}
          >
            <Filter size={13} />
            Active Only
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {members
            .filter(m => !showActiveOnly || m.status === 'available' || m.status === 'busy')
            .map((member, i) => {
            const sCfg = STATUS_CONFIG[member.status as keyof typeof STATUS_CONFIG] ?? STATUS_CONFIG.available
            const assignedTasks = getAssignedTaskCount(member.uid)
            const queueTasks = getQueueCount(member.uid)
            const score = member.reliabilityScore ?? 100
            const scoreColour = score >= 80 ? 'text-green-600 dark:text-green-400' : score >= 50 ? 'text-yellow-600 dark:text-yellow-400' : 'text-red-600 dark:text-red-400'
            const barColour = score >= 80 ? 'bg-green-500' : score >= 50 ? 'bg-yellow-500' : 'bg-red-500'
            const gradientColour = AVATAR_COLOURS[i % AVATAR_COLOURS.length]
            const isSelf = member.uid === currentUserId
            const canKick = isAdmin && !isSelf // Admin can kick anyone except themselves

            return (
              <Card key={member.uid} className={`shadow-sm overflow-hidden transition-shadow hover:shadow-md ${
                member.status === 'out_of_station' ? 'opacity-80' : ''
              }`}>
                {/* Gradient Header */}
                <div className={`bg-gradient-to-r ${gradientColour} px-5 py-4 flex items-center justify-between`}>
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center text-white text-xl font-extrabold shadow-sm">
                      {member.nickname.charAt(0)}
                    </div>
                    <div className="text-white">
                      <div className="flex items-center gap-2">
                        <h3 className="font-bold text-lg leading-tight">{member.nickname}</h3>
                        {isSelf && (
                          <span className="text-[10px] font-bold bg-white/20 text-white px-2 py-0.5 rounded-full">You</span>
                        )}
                      </div>
                      <div className="flex items-center gap-1.5 mt-0.5">
                        <ShieldCheck size={12} className={member.role === 'admin' ? 'text-white' : 'text-white/60'} />
                        <span className="text-xs text-white/80 font-medium capitalize">{member.role}</span>
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-white/15 border border-white/25 text-white text-xs font-semibold">
                    <span className={`w-2 h-2 rounded-full ${sCfg.dot}`} />
                    {sCfg.label}
                  </div>
                </div>

                {/* Body */}
                <CardContent className="p-5 space-y-4">
                  {/* Reliability score */}
                  <div>
                    <div className="flex items-center justify-between mb-1.5">
                      <span className="text-xs font-semibold text-muted-foreground flex items-center gap-1">
                        <Star size={11} /> Reliability Score
                      </span>
                      <span className={`text-sm font-extrabold ${scoreColour}`}>{score}</span>
                    </div>
                    <div className="w-full bg-secondary rounded-full h-2">
                      <div className={`h-2 rounded-full transition-all ${barColour}`} style={{ width: `${score}%` }} />
                    </div>
                  </div>

                  {/* Task info */}
                  <div className="grid grid-cols-2 gap-3">
                    <div className="bg-secondary/50 rounded-xl px-3 py-2.5">
                      <div className="flex items-center gap-1.5 mb-0.5">
                        <ClipboardList size={12} className="text-blue-500" />
                        <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider">Assigned</span>
                      </div>
                      <span className="text-xl font-extrabold">{assignedTasks}</span>
                      <p className="text-[11px] text-muted-foreground">active duties</p>
                    </div>
                    <div className="bg-secondary/50 rounded-xl px-3 py-2.5">
                      <div className="flex items-center gap-1.5 mb-0.5">
                        <Users size={12} className="text-purple-500" />
                        <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider">In Queue</span>
                      </div>
                      <span className="text-xl font-extrabold">{queueTasks}</span>
                      <p className="text-[11px] text-muted-foreground">rotations</p>
                    </div>
                  </div>

                  {/* Status toggle — shown for admins (all members) or for yourself */}
                  {(isAdmin || isSelf) && (
                    <div className={`grid gap-2 ${(isAdmin && canKick) ? 'grid-cols-2' : 'grid-cols-1'}`}>
                      <Button
                        size="default"
                        className={`font-bold text-sm h-11 transition-all ${
                          member.status === 'out_of_station'
                            ? 'bg-green-600 hover:bg-green-700 text-white border-0 shadow-sm'
                            : 'bg-orange-50 dark:bg-orange-900/20 border-2 border-orange-400 dark:border-orange-600 text-orange-700 dark:text-orange-300 hover:bg-orange-100 dark:hover:bg-orange-900/40'
                        }`}
                        onClick={() => toggleStatus(member)}
                      >
                        {member.status === 'out_of_station' ? (
                          <><CheckCircle2 size={15} className="mr-2" /> I&apos;m Back — Available</>
                        ) : (
                          <><MapPinOff size={15} className="mr-2" /> Going Out of Station</>
                        )}
                      </Button>

                      {isAdmin && canKick && (
                        <Button
                          variant="outline"
                          className="font-semibold text-sm border-destructive/30 text-destructive hover:bg-destructive hover:text-white"
                          onClick={() => { setKickError(''); setKickTarget(member) }}
                        >
                          <UserMinus size={14} className="mr-1.5" /> Remove
                        </Button>
                      )}
                    </div>
                  )}
                </CardContent>
              </Card>
            )
          })}
        </div>
      </div>
    </>
  )
}
