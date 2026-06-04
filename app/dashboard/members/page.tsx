"use client"
import { useState, useEffect } from 'react'
import { useFlatStore, Member } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  Users, MapPinOff, CheckCircle2, ShieldCheck, Star, UserMinus,
  X, Filter, Bell, Copy, Check, Link, Lock, UserCheck,
  Pencil, Save, MessageSquare, TrendingUp,
} from 'lucide-react'
import GoingOutModal from '@/components/GoingOutModal'
import { getNpsResponses, type NpsResponse } from '@/lib/npsService'

function KickDialog({ member, onClose, onConfirm, loading, error }: {
  member: Member | null; onClose: () => void; onConfirm: () => void; loading: boolean; error: string
}) {
  if (!member) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
      <div className="bg-card border border-border rounded-2xl shadow-xl w-full max-w-sm p-6 space-y-4">
        <div className="flex items-start justify-between">
          <div>
            <h3 className="font-bold text-lg">Remove {member.nickname}?</h3>
            <p className="text-sm text-muted-foreground mt-1">They will lose all access. Tasks reassigned automatically.</p>
          </div>
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground ml-2"><X size={18} /></button>
        </div>
        {error && <p className="text-sm text-destructive font-medium">{error}</p>}
        <div className="flex gap-3 pt-1">
          <Button variant="outline" className="flex-1" onClick={onClose} disabled={loading}>Cancel</Button>
          <Button className="flex-1 font-semibold bg-destructive hover:bg-destructive/90 text-white" onClick={onConfirm} disabled={loading}>
            {loading ? 'Removing…' : `Remove ${member.nickname}`}
          </Button>
        </div>
      </div>
    </div>
  )
}

const GRADIENTS = [
  'from-blue-500 to-indigo-600', 'from-violet-500 to-purple-700',
  'from-cyan-500 to-blue-600',   'from-orange-500 to-amber-600',
  'from-emerald-500 to-teal-600','from-pink-500 to-rose-600',
]
const STATUS_CONFIG = {
  available:      { label: 'Available',      dot: 'bg-green-500',  text: 'text-green-600 dark:text-green-400' },
  out_of_station: { label: 'Out of Station', dot: 'bg-orange-500', text: 'text-orange-600 dark:text-orange-400' },
  busy:           { label: 'Busy',           dot: 'bg-blue-500',   text: 'text-blue-600 dark:text-blue-400' },
  inactive:       { label: 'Inactive',       dot: 'bg-muted-foreground', text: 'text-muted-foreground' },
}

export default function MembersPage() {
  const {
    members, tasks, joinRequests, changeMemberStatus, createSwapRequest,
    approveJoinRequest, rejectJoinRequest, kickMember,
    name: flatName, flatId, joinMode, renameFlatAction, setJoinMode,
  } = useFlatStore()
  const { user } = useAuthStore()

  const currentUserId   = user?.uid || 'u1'
  const currentMember   = members.find(m => m.uid === currentUserId)
  const isAdmin         = currentMember?.role === 'admin'
  const flatDisplayName = flatName || 'My Flat'

  const [showActiveOnly, setShowActiveOnly]   = useState(false)
  const [goingOutTarget, setGoingOutTarget]   = useState<Member | null>(null)
  const [kickTarget, setKickTarget]           = useState<Member | null>(null)
  const [kickLoading, setKickLoading]         = useState(false)
  const [kickError, setKickError]             = useState('')
  const [copied, setCopied]                   = useState(false)
  const [copiedLink, setCopiedLink]           = useState(false)
  const [editingName, setEditingName]         = useState(false)
  const [draftName, setDraftName]             = useState('')
  const [nameLoading, setNameLoading]         = useState(false)
  const [nameError, setNameError]             = useState('')
  const [npsResponses, setNpsResponses]       = useState<NpsResponse[]>([])

  useEffect(() => {
    if (isAdmin && flatId) getNpsResponses(flatId).then(setNpsResponses)
  }, [isAdmin, flatId])

  const toggleStatus = (member: Member) => {
    if (member.status !== 'out_of_station') {
      const isSelf    = member.uid === currentUserId
      const pending   = tasks.filter(t => t.currentAssignedUserId === member.uid && (t.status === 'pending' || t.status === 'overdue'))
      if (isSelf && pending.length > 0) { setGoingOutTarget(member); return }
    }
    changeMemberStatus(member.uid, member.status === 'out_of_station' ? 'available' : 'out_of_station')
  }

  const handleKickConfirm = async () => {
    if (!kickTarget || !user) return
    setKickLoading(true); setKickError('')
    try { await kickMember(kickTarget.uid, user.uid); setKickTarget(null) }
    catch (e: unknown) { setKickError(e instanceof Error ? e.message : 'Failed.') }
    finally { setKickLoading(false) }
  }

  const handleCopyCode = () => {
    if (!flatId) return
    navigator.clipboard.writeText(flatId).then(() => { setCopied(true); setTimeout(() => setCopied(false), 2000) })
  }
  const handleCopyLink = () => {
    const link = `${window.location.origin}/onboarding?mode=join&code=${flatId}`
    navigator.clipboard.writeText(link).then(() => { setCopiedLink(true); setTimeout(() => setCopiedLink(false), 2000) })
  }
  const handleSaveName = async () => {
    const trimmed = draftName.trim()
    if (!trimmed || trimmed === flatDisplayName) { setEditingName(false); return }
    setNameLoading(true); setNameError('')
    try { await renameFlatAction(trimmed); setEditingName(false) }
    catch (e: unknown) { setNameError(e instanceof Error ? e.message : 'Failed.') }
    finally { setNameLoading(false) }
  }

  const getAssigned = (uid: string) => tasks.filter(t => t.currentAssignedUserId === uid && (t.status === 'pending' || t.status === 'overdue')).length
  const getQueued   = (uid: string) => tasks.filter(t => t.queueOrder.includes(uid)).length
  const available   = members.filter(m => m.status === 'available').length
  const absent      = members.filter(m => m.status === 'out_of_station').length
  const avgScore    = members.length ? Math.round(members.reduce((s, m) => s + (m.reliabilityScore ?? 100), 0) / members.length) : 100
  const topPerformer = [...members].sort((a, b) => (b.reliabilityScore ?? 100) - (a.reliabilityScore ?? 100))[0]
  const visibleMembers = members.filter(m => !showActiveOnly || m.status === 'available' || m.status === 'busy')
  const pendingJoins   = isAdmin ? joinRequests.filter(r => r.status === 'pending') : []

  return (
    <>
      <KickDialog member={kickTarget} onClose={() => { setKickTarget(null); setKickError('') }}
        onConfirm={handleKickConfirm} loading={kickLoading} error={kickError} />

      <div className="space-y-8 max-w-3xl">

        {/* ── Page header ─────────────────────────────────────────────── */}
        <div>
          {isAdmin && <p className="text-xs font-bold text-muted-foreground uppercase tracking-widest mb-1">Admin</p>}
          <h1 className="text-3xl font-extrabold tracking-tight">Members</h1>
          <p className="text-muted-foreground mt-1 text-sm">
            {isAdmin ? 'Your flatmates and flat settings.' : 'Your flatmates and their current status.'}
          </p>
        </div>

        {/* ═══════════════════════════════════════════════════════════════ */}
        {/* SECTION 1 — MEMBERS LIST                                        */}
        {/* ═══════════════════════════════════════════════════════════════ */}

        {/* Join requests (admin) */}
        {pendingJoins.length > 0 && (
          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <Bell size={14} className="text-violet-500" />
              <span className="text-sm font-bold">Join Requests</span>
              <span className="bg-violet-500 text-white text-[10px] font-extrabold px-1.5 py-0.5 rounded-full">{pendingJoins.length}</span>
            </div>
            {pendingJoins.map(req => (
              <div key={req.id} className="flex flex-col sm:flex-row sm:items-center gap-3 p-4 rounded-xl border-2 border-violet-200 dark:border-violet-800 bg-violet-50 dark:bg-violet-950/30">
                <div className="w-9 h-9 rounded-full bg-violet-200 dark:bg-violet-800 flex items-center justify-center text-sm font-bold text-violet-700 dark:text-violet-200 shrink-0">
                  {req.nickname.charAt(0).toUpperCase()}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold">{req.nickname}</p>
                  <p className="text-xs text-muted-foreground truncate">{req.email}</p>
                </div>
                <div className="flex gap-2 shrink-0">
                  <Button size="sm" className="bg-green-600 hover:bg-green-700 text-white font-bold px-3" onClick={() => approveJoinRequest(req.id)}>
                    <CheckCircle2 size={13} className="mr-1" /> Approve
                  </Button>
                  <Button size="sm" variant="outline" className="border-destructive/40 text-destructive hover:bg-destructive/10 font-bold px-3" onClick={() => rejectJoinRequest(req.id)}>
                    <X size={13} className="mr-1" /> Reject
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Stats row */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          <Card className="shadow-sm border-border/60">
            <CardContent className="p-4">
              <div className="flex items-center gap-2 mb-1"><Users size={14} className="text-blue-500" /><span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider">Total</span></div>
              <p className="text-2xl font-extrabold">{members.length}</p>
              <p className="text-[11px] text-muted-foreground">flatmates</p>
            </CardContent>
          </Card>
          <Card className="shadow-sm border-border/60">
            <CardContent className="p-4">
              <div className="flex items-center gap-2 mb-1"><CheckCircle2 size={14} className="text-green-500" /><span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider">Available</span></div>
              <p className="text-2xl font-extrabold text-green-600 dark:text-green-400">{available}</p>
              <p className="text-[11px] text-muted-foreground">{absent} absent</p>
            </CardContent>
          </Card>
          <Card className="shadow-sm border-border/60">
            <CardContent className="p-4">
              <div className="flex items-center gap-2 mb-1"><Star size={14} className="text-orange-500" /><span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider">Avg Score</span></div>
              <p className="text-2xl font-extrabold">{avgScore}</p>
              <div className="w-full bg-secondary rounded-full h-1.5 mt-1.5">
                <div className="h-1.5 rounded-full bg-orange-400" style={{ width: `${avgScore}%` }} />
              </div>
            </CardContent>
          </Card>
          <Card className="shadow-sm border-border/60">
            <CardContent className="p-4">
              <div className="flex items-center gap-2 mb-1"><TrendingUp size={14} className="text-purple-500" /><span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider">Top</span></div>
              <p className="text-lg font-extrabold truncate">{topPerformer?.nickname ?? '—'}</p>
              <p className="text-[11px] text-muted-foreground">{topPerformer?.reliabilityScore ?? 100} score</p>
            </CardContent>
          </Card>
        </div>

        {/* Filter bar */}
        <div className="flex items-center justify-between">
          <p className="text-sm font-semibold text-muted-foreground">
            {visibleMembers.length} of {members.length} flatmate{members.length !== 1 ? 's' : ''}
          </p>
          <button onClick={() => setShowActiveOnly(v => !v)}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-sm font-bold border transition-all ${
              showActiveOnly ? 'bg-green-600 text-white border-green-600' : 'bg-background text-muted-foreground border-border hover:bg-secondary/80'
            }`}>
            <Filter size={13} /> Active Only
          </button>
        </div>

        {/* ── Compact member list ──────────────────────────────────────── */}
        <Card className="shadow-sm overflow-hidden">
          <div className="divide-y divide-border">
            {visibleMembers.map((member, i) => {
              const sCfg    = STATUS_CONFIG[member.status as keyof typeof STATUS_CONFIG] ?? STATUS_CONFIG.available
              const score   = member.reliabilityScore ?? 100
              const isSelf  = member.uid === currentUserId
              const canKick = isAdmin && !isSelf
              const assigned = getAssigned(member.uid)
              const queued   = getQueued(member.uid)
              const scoreColour = score >= 80 ? 'text-green-600 dark:text-green-400' : score >= 50 ? 'text-yellow-500' : 'text-red-500'

              return (
                <div key={member.uid} className={`flex items-center gap-3 px-4 py-3 ${member.status === 'out_of_station' ? 'opacity-70' : ''}`}>

                  {/* Avatar */}
                  <div className={`w-10 h-10 rounded-full bg-gradient-to-br ${GRADIENTS[i % GRADIENTS.length]} flex items-center justify-center text-white font-bold text-sm shrink-0`}>
                    {member.nickname.charAt(0)}
                  </div>

                  {/* Name + meta */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-1.5 flex-wrap">
                      <span className="text-sm font-bold">{member.nickname}</span>
                      {isSelf && <span className="text-[9px] font-bold text-primary bg-primary/10 px-1.5 py-0.5 rounded-full leading-none">You</span>}
                      {member.role === 'admin' && <ShieldCheck size={12} className="text-primary" />}
                    </div>
                    <div className="flex items-center gap-2 mt-0.5 flex-wrap">
                      <span className={`flex items-center gap-1 text-[10px] font-semibold ${sCfg.text}`}>
                        <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${sCfg.dot}`} />
                        {sCfg.label}
                      </span>
                      <span className="text-[10px] text-muted-foreground">·</span>
                      <span className={`text-[10px] font-bold ${scoreColour}`}>★ {score}</span>
                      {assigned > 0 && (
                        <>
                          <span className="text-[10px] text-muted-foreground">·</span>
                          <span className="text-[10px] text-muted-foreground">{assigned} due</span>
                        </>
                      )}
                      {queued > 0 && (
                        <>
                          <span className="text-[10px] text-muted-foreground">·</span>
                          <span className="text-[10px] text-muted-foreground">{queued} in queue</span>
                        </>
                      )}
                    </div>
                  </div>

                  {/* Actions */}
                  {(isAdmin || isSelf) && (
                    <div className="flex items-center gap-1.5 shrink-0">
                      <Button
                        size="sm"
                        variant="outline"
                        className={`h-8 text-xs font-semibold px-2.5 ${
                          member.status === 'out_of_station'
                            ? 'border-green-500/40 text-green-600 hover:bg-green-50 dark:hover:bg-green-950/30'
                            : 'border-orange-400/40 text-orange-600 hover:bg-orange-50 dark:hover:bg-orange-950/30'
                        }`}
                        onClick={() => toggleStatus(member)}
                      >
                        {member.status === 'out_of_station' ? 'Back' : 'Out'}
                      </Button>
                      {canKick && (
                        <Button size="sm" variant="outline"
                          className="h-8 w-8 p-0 border-destructive/30 text-destructive hover:bg-destructive hover:text-white shrink-0"
                          onClick={() => { setKickError(''); setKickTarget(member) }}
                          title={`Remove ${member.nickname}`}>
                          <UserMinus size={13} />
                        </Button>
                      )}
                    </div>
                  )}
                </div>
              )
            })}
          </div>
        </Card>

        {/* ═══════════════════════════════════════════════════════════════ */}
        {/* SECTION 2 — MANAGE FLAT (admin only, below member list)         */}
        {/* ═══════════════════════════════════════════════════════════════ */}
        {isAdmin && (
          <div className="space-y-5 pt-2">
            <div className="flex items-center gap-3 pb-2 border-b border-border">
              <div>
                <h2 className="text-lg font-extrabold tracking-tight">Manage Flat</h2>
                <p className="text-xs text-muted-foreground mt-0.5">Flat settings — rename, invite, join mode, feedback.</p>
              </div>
            </div>

            {/* Flat name + members summary */}
            <Card className="shadow-sm">
              <CardContent className="p-5">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                  <div className="space-y-1">
                    <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">Flat Name</p>
                    {editingName ? (
                      <div className="space-y-1.5">
                        <div className="flex items-center gap-2">
                          <input autoFocus type="text" maxLength={50} value={draftName}
                            onChange={e => setDraftName(e.target.value)}
                            onKeyDown={e => { if (e.key === 'Enter') handleSaveName(); if (e.key === 'Escape') setEditingName(false) }}
                            className="flex-1 bg-background border border-primary rounded-lg px-3 py-1.5 text-sm font-semibold focus:outline-none focus:ring-2 focus:ring-primary/30" />
                          <button onClick={handleSaveName} disabled={nameLoading || !draftName.trim()}
                            className="p-1.5 rounded-lg bg-primary text-white hover:bg-primary/90 disabled:opacity-50"><Save size={14} /></button>
                          <button onClick={() => setEditingName(false)}
                            className="p-1.5 rounded-lg bg-secondary hover:bg-secondary/80"><X size={14} /></button>
                        </div>
                        {nameError && <p className="text-xs text-destructive">{nameError}</p>}
                      </div>
                    ) : (
                      <div className="flex items-center gap-2 group">
                        <p className="text-xl font-bold">{flatDisplayName}</p>
                        <button onClick={() => { setDraftName(flatDisplayName); setNameError(''); setEditingName(true) }}
                          className="opacity-0 group-hover:opacity-100 p-1 rounded-md text-muted-foreground hover:text-primary hover:bg-primary/10 transition-all">
                          <Pencil size={13} />
                        </button>
                      </div>
                    )}
                    <p className="text-[11px] text-muted-foreground font-mono">{flatId}</p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">Members</p>
                    <p className="text-xl font-bold">{members.filter(m => m.status !== 'inactive').length} active</p>
                    <div className="flex flex-wrap gap-1">
                      {members.filter(m => m.status !== 'inactive').slice(0, 6).map(m => (
                        <span key={m.uid} className="text-[10px] font-semibold bg-secondary px-2 py-0.5 rounded-full">
                          {m.nickname}{m.role === 'admin' && ' ★'}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Invite + Join Mode */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Card className="shadow-sm">
                <CardHeader className="pb-3">
                  <CardTitle className="text-sm flex items-center gap-2"><Users size={15} className="text-primary" /> Invite Roommates</CardTitle>
                  <CardDescription className="text-xs">Share code or link to add people.</CardDescription>
                </CardHeader>
                <CardContent className="space-y-2">
                  <div className="flex items-center gap-2 bg-secondary/50 border border-border rounded-xl px-3 py-2.5">
                    <code className="text-lg font-mono font-bold text-primary tracking-widest flex-1 min-w-0 truncate">{flatId || '—'}</code>
                    <button onClick={handleCopyCode} className="text-muted-foreground hover:text-foreground transition-colors shrink-0" title="Copy code">
                      {copied ? <Check size={15} className="text-green-500" /> : <Copy size={15} />}
                    </button>
                  </div>
                  <button onClick={handleCopyLink}
                    className="w-full flex items-center gap-2 px-3 py-2 bg-primary/5 hover:bg-primary/10 border border-primary/20 rounded-xl transition-colors">
                    <Link size={12} className="text-primary shrink-0" />
                    <span className="text-xs font-semibold text-primary flex-1 text-left truncate">
                      {copiedLink ? 'Link copied!' : 'Copy invite link'}
                    </span>
                    {copiedLink ? <Check size={12} className="text-green-500" /> : <Copy size={12} className="text-primary" />}
                  </button>
                </CardContent>
              </Card>

              <Card className="shadow-sm">
                <CardHeader className="pb-3">
                  <CardTitle className="text-sm flex items-center gap-2"><UserCheck size={15} className="text-primary" /> Join Mode</CardTitle>
                  <CardDescription className="text-xs">How new roommates enter your flat.</CardDescription>
                </CardHeader>
                <CardContent className="space-y-2">
                  <button onClick={() => setJoinMode('auto')}
                    className={`w-full flex items-center gap-3 p-3 rounded-xl border-2 text-left transition-all ${joinMode === 'auto' ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/40'}`}>
                    <UserCheck size={15} className={joinMode === 'auto' ? 'text-primary' : 'text-muted-foreground'} />
                    <div>
                      <p className={`text-xs font-bold ${joinMode === 'auto' ? 'text-primary' : ''}`}>Auto Join</p>
                      <p className="text-[10px] text-muted-foreground">Anyone with the code joins instantly</p>
                    </div>
                  </button>
                  <button onClick={() => setJoinMode('approval')}
                    className={`w-full flex items-center gap-3 p-3 rounded-xl border-2 text-left transition-all ${joinMode === 'approval' ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/40'}`}>
                    <Lock size={15} className={joinMode === 'approval' ? 'text-primary' : 'text-muted-foreground'} />
                    <div>
                      <p className={`text-xs font-bold ${joinMode === 'approval' ? 'text-primary' : ''}`}>Approval Only</p>
                      <p className="text-[10px] text-muted-foreground">You must approve each request</p>
                    </div>
                  </button>
                </CardContent>
              </Card>
            </div>

            {/* NPS */}
            <Card className="shadow-sm">
              <CardHeader>
                <CardTitle className="flex items-center gap-2"><Star size={20} className="text-primary" /> Member Feedback</CardTitle>
                <CardDescription>Net Promoter Score — how likely are members to recommend Habitiq?</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                {npsResponses.length === 0 ? (
                  <p className="text-sm text-muted-foreground">No responses yet. Members are prompted after 7 days.</p>
                ) : (() => {
                  const avg        = npsResponses.reduce((s, r) => s + r.score, 0) / npsResponses.length
                  const promoters  = npsResponses.filter(r => r.score >= 9).length
                  const passives   = npsResponses.filter(r => r.score >= 7 && r.score <= 8).length
                  const detractors = npsResponses.filter(r => r.score <= 6).length
                  const nps        = Math.round(((promoters - detractors) / npsResponses.length) * 100)
                  return (
                    <>
                      <div className="grid grid-cols-2 gap-3">
                        <div className="p-4 rounded-xl bg-primary/5 border border-primary/20 text-center">
                          <p className="text-3xl font-extrabold text-primary">{nps > 0 ? '+' : ''}{nps}</p>
                          <p className="text-xs font-bold text-muted-foreground mt-1">NPS Score</p>
                        </div>
                        <div className="p-4 rounded-xl bg-secondary/50 border border-border text-center">
                          <p className="text-3xl font-extrabold">{avg.toFixed(1)}</p>
                          <p className="text-xs font-bold text-muted-foreground mt-1">Avg / 10</p>
                        </div>
                      </div>
                      <div className="flex gap-2 text-xs">
                        <div className="flex-1 text-center p-2 rounded-lg bg-green-50 dark:bg-green-950/30 border border-green-200 dark:border-green-800">
                          <p className="font-extrabold text-green-700 dark:text-green-400">{promoters}</p>
                          <p className="text-green-600 dark:text-green-500 font-semibold">Promoters</p>
                        </div>
                        <div className="flex-1 text-center p-2 rounded-lg bg-yellow-50 dark:bg-yellow-950/30 border border-yellow-200 dark:border-yellow-800">
                          <p className="font-extrabold text-yellow-700 dark:text-yellow-400">{passives}</p>
                          <p className="text-yellow-600 dark:text-yellow-500 font-semibold">Passives</p>
                        </div>
                        <div className="flex-1 text-center p-2 rounded-lg bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-800">
                          <p className="font-extrabold text-red-700 dark:text-red-400">{detractors}</p>
                          <p className="text-red-600 dark:text-red-500 font-semibold">Detractors</p>
                        </div>
                      </div>
                      <div className="space-y-2">
                        <p className="text-xs font-bold text-muted-foreground uppercase tracking-widest">Responses</p>
                        {npsResponses.map(r => (
                          <div key={r.id} className="flex items-start gap-3 p-3 rounded-xl border border-border bg-secondary/20">
                            <div className={`w-9 h-9 rounded-xl flex items-center justify-center text-sm font-extrabold shrink-0 ${
                              r.score >= 9 ? 'bg-green-100 text-green-700 dark:bg-green-900/40 dark:text-green-300'
                              : r.score >= 7 ? 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/40 dark:text-yellow-300'
                              : 'bg-red-100 text-red-700 dark:bg-red-900/40 dark:text-red-300'
                            }`}>{r.score}</div>
                            <div className="flex-1 min-w-0">
                              <div className="flex items-center justify-between gap-2">
                                <p className="text-sm font-semibold">{r.nickname}</p>
                                <p className="text-[10px] text-muted-foreground shrink-0">
                                  {new Date(r.createdAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })}
                                </p>
                              </div>
                              {r.comment && (
                                <p className="text-xs text-muted-foreground mt-0.5 flex items-start gap-1">
                                  <MessageSquare size={10} className="shrink-0 mt-0.5" />{r.comment}
                                </p>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    </>
                  )
                })()}
              </CardContent>
            </Card>
          </div>
        )}
      </div>

      {goingOutTarget && (
        <GoingOutModal
          assignedTasks={tasks.filter(t => t.currentAssignedUserId === goingOutTarget.uid && (t.status === 'pending' || t.status === 'overdue'))}
          availableMembers={members.filter(m => m.uid !== goingOutTarget.uid && m.status !== 'out_of_station' && m.status !== 'inactive')}
          onClose={() => setGoingOutTarget(null)}
          onConfirm={async (assignments) => {
            for (const [taskId, toUserId] of Object.entries(assignments)) {
              await createSwapRequest(taskId, goingOutTarget.uid, toUserId, false, true)
            }
            setGoingOutTarget(null)
          }}
        />
      )}
    </>
  )
}
