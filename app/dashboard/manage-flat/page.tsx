"use client"
import { useState, useEffect } from 'react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  Copy, Check, Building2, Link, UserCheck, Lock, Pencil,
  Save, X, Star, MessageSquare, Users, ShieldCheck,
} from 'lucide-react'
import { useRouter } from 'next/navigation'
import { getNpsResponses, type NpsResponse } from '@/lib/npsService'

export default function ManageFlatPage() {
  const router = useRouter()
  const { flatId, name, joinMode, members, renameFlatAction, setJoinMode } = useFlatStore()
  const { user } = useAuthStore()

  const currentMember = members.find(m => m.uid === user?.uid)
  const isAdmin = currentMember?.role === 'admin'

  const [copied, setCopied] = useState(false)
  const [copiedLink, setCopiedLink] = useState(false)
  const [editingName, setEditingName] = useState(false)
  const [draftName, setDraftName] = useState('')
  const [nameLoading, setNameLoading] = useState(false)
  const [nameError, setNameError] = useState('')
  const [npsResponses, setNpsResponses] = useState<NpsResponse[]>([])

  useEffect(() => {
    if (isAdmin && flatId) {
      getNpsResponses(flatId).then(setNpsResponses)
    }
  }, [isAdmin, flatId])

  // Redirect non-admins
  if (!isAdmin) {
    return (
      <div className="flex flex-col items-center justify-center py-24 text-center text-muted-foreground">
        <ShieldCheck size={40} className="mb-4 opacity-30" />
        <p className="font-semibold">Admin only</p>
        <p className="text-sm mt-1">Only flat admins can manage flat settings.</p>
        <Button variant="outline" size="sm" className="mt-4" onClick={() => router.push('/dashboard')}>
          Go to Dashboard
        </Button>
      </div>
    )
  }

  const flatDisplayName = name || 'My Flat'
  const activeMembers = members.filter(m => m.status !== 'inactive')

  const handleCopyCode = () => {
    if (!flatId) return
    navigator.clipboard.writeText(flatId).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    })
  }

  const handleCopyLink = () => {
    const link = `${window.location.origin}/onboarding?mode=join&code=${flatId}`
    navigator.clipboard.writeText(link).then(() => {
      setCopiedLink(true)
      setTimeout(() => setCopiedLink(false), 2000)
    })
  }

  const handleStartEditName = () => {
    setDraftName(flatDisplayName)
    setNameError('')
    setEditingName(true)
  }

  const handleSaveName = async () => {
    const trimmed = draftName.trim()
    if (!trimmed || trimmed === flatDisplayName) { setEditingName(false); return }
    setNameLoading(true)
    setNameError('')
    try {
      await renameFlatAction(trimmed)
      setEditingName(false)
    } catch (e: unknown) {
      setNameError(e instanceof Error ? e.message : 'Failed to rename. Try again.')
    } finally {
      setNameLoading(false)
    }
  }

  return (
    <div className="space-y-6 max-w-2xl">
      <div>
        <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-0.5">Admin</p>
        <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">Manage Flat</h1>
        <p className="text-muted-foreground mt-0.5 text-sm">
          Invite members, rename your flat, and control how people join.
        </p>
      </div>

      {/* ── Flat Overview ──────────────────────────────────────────────── */}
      <Card className="shadow-sm">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Building2 size={20} className="text-primary" />
            Flat Details
          </CardTitle>
          <CardDescription>Basic info about your flat.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {/* Flat name */}
            <div className="space-y-1.5">
              <p className="text-sm font-bold text-muted-foreground">Flat Name</p>
              {editingName ? (
                <div className="space-y-2">
                  <div className="flex items-center gap-2">
                    <input
                      autoFocus
                      type="text"
                      maxLength={50}
                      value={draftName}
                      onChange={e => setDraftName(e.target.value)}
                      onKeyDown={e => { if (e.key === 'Enter') handleSaveName(); if (e.key === 'Escape') setEditingName(false) }}
                      className="flex-1 bg-background border border-primary rounded-lg px-3 py-1.5 text-sm font-semibold focus:outline-none focus:ring-2 focus:ring-primary/30"
                    />
                    <button onClick={handleSaveName} disabled={nameLoading || !draftName.trim()}
                      className="p-1.5 rounded-lg bg-primary text-white hover:bg-primary/90 disabled:opacity-50 transition-colors">
                      <Save size={14} />
                    </button>
                    <button onClick={() => setEditingName(false)}
                      className="p-1.5 rounded-lg bg-secondary hover:bg-secondary/80 transition-colors">
                      <X size={14} />
                    </button>
                  </div>
                  {nameError && <p className="text-xs text-destructive">{nameError}</p>}
                </div>
              ) : (
                <div className="flex items-center gap-2 group">
                  <p className="text-lg font-semibold">{flatDisplayName}</p>
                  <button onClick={handleStartEditName}
                    className="opacity-0 group-hover:opacity-100 p-1 rounded-md text-muted-foreground hover:text-primary hover:bg-primary/10 transition-all">
                    <Pencil size={13} />
                  </button>
                </div>
              )}
              <p className="text-[11px] text-muted-foreground">
                ID: <span className="font-mono">{flatId}</span>
              </p>
            </div>
            <div className="space-y-1">
              <p className="text-sm font-bold text-muted-foreground">Members</p>
              <p className="text-lg font-semibold">{activeMembers.length} active</p>
              <div className="flex flex-wrap gap-1 mt-1">
                {activeMembers.slice(0, 5).map(m => (
                  <span key={m.uid} className="text-[10px] font-semibold bg-secondary px-2 py-0.5 rounded-full">
                    {m.nickname}
                    {m.role === 'admin' && ' ★'}
                  </span>
                ))}
                {activeMembers.length > 5 && (
                  <span className="text-[10px] text-muted-foreground px-1">+{activeMembers.length - 5} more</span>
                )}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* ── Invite ────────────────────────────────────────────────────── */}
      <Card className="shadow-sm">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Users size={20} className="text-primary" />
            Invite Roommates
          </CardTitle>
          <CardDescription>Share the code or link to add people to your flat.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          {/* Invite code */}
          <div className="flex items-center gap-3 bg-secondary/50 border border-border rounded-xl px-4 py-3">
            <code className="text-xl font-mono font-bold text-primary tracking-widest flex-1">
              {flatId || 'FLAT-1234'}
            </code>
            <Button size="sm" variant="outline" onClick={handleCopyCode} className="shrink-0 gap-2">
              {copied ? <><Check size={14} className="text-green-500" /> Copied!</> : <><Copy size={14} /> Code</>}
            </Button>
          </div>

          {/* Invite link */}
          <button
            onClick={handleCopyLink}
            className="w-full flex items-center gap-2 px-4 py-2.5 bg-primary/5 hover:bg-primary/10 border border-primary/20 rounded-xl transition-colors"
          >
            <Link size={13} className="text-primary shrink-0" />
            <span className="text-xs font-semibold text-primary flex-1 text-left truncate">
              {copiedLink ? 'Link copied!' : 'Copy invite link'}
            </span>
            {copiedLink ? <Check size={13} className="text-green-500 shrink-0" /> : <Copy size={13} className="text-primary shrink-0" />}
          </button>
        </CardContent>
      </Card>

      {/* ── Join Mode ──────────────────────────────────────────────────── */}
      <Card className="shadow-sm">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <UserCheck size={20} className="text-primary" />
            Join Mode
          </CardTitle>
          <CardDescription>Control how new roommates enter your flat.</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-2">
            <button onClick={() => setJoinMode('auto')}
              className={`flex items-center gap-2 p-3 rounded-xl border-2 text-left transition-all ${
                joinMode === 'auto' ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/50'
              }`}>
              <UserCheck size={16} className={joinMode === 'auto' ? 'text-primary' : 'text-muted-foreground'} />
              <div>
                <p className={`text-xs font-bold ${joinMode === 'auto' ? 'text-primary' : ''}`}>Auto Join</p>
                <p className="text-[10px] text-muted-foreground">Anyone with the code joins instantly</p>
              </div>
            </button>
            <button onClick={() => setJoinMode('approval')}
              className={`flex items-center gap-2 p-3 rounded-xl border-2 text-left transition-all ${
                joinMode === 'approval' ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/50'
              }`}>
              <Lock size={16} className={joinMode === 'approval' ? 'text-primary' : 'text-muted-foreground'} />
              <div>
                <p className={`text-xs font-bold ${joinMode === 'approval' ? 'text-primary' : ''}`}>Approval Only</p>
                <p className="text-[10px] text-muted-foreground">You approve each request</p>
              </div>
            </button>
          </div>
        </CardContent>
      </Card>

      {/* ── NPS / Member Feedback ─────────────────────────────────────── */}
      <Card className="shadow-sm">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Star size={20} className="text-primary" />
            Member Feedback
          </CardTitle>
          <CardDescription>
            Net Promoter Score — how likely are members to recommend Habitiq?
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {npsResponses.length === 0 ? (
            <p className="text-sm text-muted-foreground">No responses yet. Members will be prompted after 7 days.</p>
          ) : (() => {
            const avg = npsResponses.reduce((s, r) => s + r.score, 0) / npsResponses.length
            const promoters  = npsResponses.filter(r => r.score >= 9).length
            const passives   = npsResponses.filter(r => r.score >= 7 && r.score <= 8).length
            const detractors = npsResponses.filter(r => r.score <= 6).length
            const npsScore   = Math.round(((promoters - detractors) / npsResponses.length) * 100)
            return (
              <>
                <div className="grid grid-cols-2 gap-3">
                  <div className="p-4 rounded-xl bg-primary/5 border border-primary/20 text-center">
                    <p className="text-3xl font-extrabold text-primary">{npsScore > 0 ? '+' : ''}{npsScore}</p>
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
                            <MessageSquare size={10} className="shrink-0 mt-0.5" />
                            {r.comment}
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
  )
}
