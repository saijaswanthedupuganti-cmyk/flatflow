import type { Member, Task, Expense } from '@/store/useFlatStore'

// ─── FlatContext type ──────────────────────────────────────────────────────────
export interface VoiceFlatContext {
  flatId:      string
  flatName:    string
  activeFlatId: string   // for multi-flat safety (Phase 2)
  currentUser: {
    uid:      string
    nickname: string
  }
  members: Array<{
    uid:      string
    nickname: string
    role:     'admin' | 'member'
    isOOS:    boolean
  }>
  tasks: Array<{
    id:                  string
    name:                string
    frequency:           string
    currentAssignedUserId: string
    dueDate:             string
    status:              string
  }>
  // Precomputed net balances: uid → net amount relative to currentUser
  // Positive = they owe currentUser. Negative = currentUser owes them.
  balances: Record<string, number>
}

// ─── Build context from Zustand store values ──────────────────────────────────
// Called inside a React component/hook where store is accessible.
export function buildVoiceContext(opts: {
  flatId:      string
  flatName:    string
  currentUid:  string
  members:     Member[]
  tasks:       Task[]
  expenses:    Expense[]
}): VoiceFlatContext {
  const { flatId, flatName, currentUid, members, tasks, expenses } = opts

  const currentMember = members.find(m => m.uid === currentUid)

  // Compute net balances from expenses
  const balances: Record<string, number> = {}

  for (const expense of expenses) {
    const splits = expense.splits ?? {}
    if (expense.paidBy === currentUid) {
      // Others owe currentUser their share
      for (const [uid, share] of Object.entries(splits)) {
        if (uid === currentUid) continue
        balances[uid] = (balances[uid] ?? 0) + share
      }
    } else if (currentUid in splits) {
      // currentUser owes the payer their share
      const share = splits[currentUid]
      balances[expense.paidBy] = (balances[expense.paidBy] ?? 0) - share
    }
  }

  return {
    flatId,
    flatName,
    activeFlatId: flatId,
    currentUser: {
      uid:      currentUid,
      nickname: currentMember?.nickname ?? 'You',
    },
    members: members.map(m => ({
      uid:      m.uid,
      nickname: m.nickname,
      role:     m.role,
      isOOS:    m.status === 'out_of_station',
    })),
    tasks: tasks.map(t => ({
      id:                    t.taskId,
      name:                  t.name,
      frequency:             t.frequency,
      currentAssignedUserId: t.currentAssignedUserId,
      dueDate:               t.dueDate,
      status:                t.status,
    })),
    balances,
  }
}

// ─── ContextCache — 30s TTL + write-invalidation ──────────────────────────────
class ContextCache {
  private store = new Map<string, { ctx: VoiceFlatContext; at: number }>()
  private readonly TTL = 30_000

  get(flatId: string): VoiceFlatContext | null {
    const entry = this.store.get(flatId)
    if (!entry) return null
    if (Date.now() - entry.at > this.TTL) {
      this.store.delete(flatId)
      return null
    }
    return entry.ctx
  }

  set(flatId: string, ctx: VoiceFlatContext): void {
    this.store.set(flatId, { ctx, at: Date.now() })
  }

  // Call after any write action (completeTask, createExpense, etc.)
  invalidate(flatId: string): void {
    this.store.delete(flatId)
  }

  clear(): void {
    this.store.clear()
  }
}

export const contextCache = new ContextCache()
