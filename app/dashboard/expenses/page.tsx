"use client"
import { useState, useMemo, useRef, useEffect } from 'react'
import {
  useFlatStore,
  type Expense, type Settlement, type RecurringBill,
  type BillInstance, type BillInstanceStatus,
  type ExpenseCategory, type Currency,
} from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { computeBalances, formatAmount, type Balance } from '@/lib/expenseUtils'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Plus, Receipt, ArrowUpRight, ArrowDownLeft, Trash2, X,
  Wallet, Inbox, Check, AlertCircle, RefreshCw, Pencil,
  Zap, Play, PauseCircle, LayoutList, CircleDollarSign, Clock,
} from 'lucide-react'

// ── Config ───────────────────────────────────────────────────────────────────

export const CATEGORY_CONFIG: Record<ExpenseCategory, { label: string; emoji: string }> = {
  rent:        { label: 'Rent',         emoji: '🏠' },
  electricity: { label: 'Electricity',  emoji: '⚡' },
  water:       { label: 'Water',        emoji: '💧' },
  internet:    { label: 'WiFi / Net',   emoji: '📶' },
  gas:         { label: 'Gas / LPG',    emoji: '🔥' },
  maid:        { label: 'Maid / Help',  emoji: '🧹' },
  grocery:     { label: 'Groceries',    emoji: '🛒' },
  milk:        { label: 'Milk',         emoji: '🥛' },
  ac:          { label: 'AC / Cooling', emoji: '❄️' },
  maintenance: { label: 'Maintenance',  emoji: '🔧' },
  food:        { label: 'Food',         emoji: '🍽️' },
  household:   { label: 'Household',    emoji: '📦' },
  other:       { label: 'Other',        emoji: '💰' },
}

const CURRENCIES: Currency[] = ['INR', 'USD', 'EUR', 'GBP', 'AED', 'SGD', 'AUD']
const CURRENCY_SYMBOLS: Record<Currency, string> = {
  INR: '₹', USD: '$', EUR: '€', GBP: '£', AED: 'AED ', SGD: 'S$', AUD: 'A$',
}

function todayStr() { return new Date().toISOString().split('T')[0] }

function currentMonthKey() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

function monthLabel(yyyymm: string) {
  const [y, m] = yyyymm.split('-')
  return new Date(Number(y), Number(m) - 1, 1).toLocaleDateString('en-IN', { month: 'long', year: 'numeric' })
}

function isBillDue(bill: RecurringBill): boolean {
  if (!bill.active) return false
  const today = new Date()
  return bill.lastGeneratedMonth !== currentMonthKey() && today.getDate() >= bill.billingDay
}

function ordinal(n: number): string {
  const s = ['th', 'st', 'nd', 'rd']
  const v = n % 100
  return n + (s[(v - 20) % 10] || s[v] || s[0])
}

// ── Modal shell ──────────────────────────────────────────────────────────────

function Modal({ title, onClose, children }: { title: string; onClose: () => void; children: React.ReactNode }) {
  return (
    <div className="fixed inset-0 bg-black/60 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
      <div className="bg-card border border-border rounded-t-2xl sm:rounded-2xl w-full sm:max-w-lg max-h-[92vh] overflow-y-auto shadow-2xl">
        <div className="flex items-center justify-between px-5 py-4 border-b border-border sticky top-0 bg-card z-10">
          <h2 className="font-bold text-base">{title}</h2>
          <button onClick={onClose} className="p-1.5 rounded-lg hover:bg-secondary text-muted-foreground transition-colors">
            <X size={17} />
          </button>
        </div>
        <div className="p-5">{children}</div>
      </div>
    </div>
  )
}

// ── Your Balance Summary ─────────────────────────────────────────────────────

function BalanceSummary({
  balances, members, onSettle,
}: {
  balances: Balance[]
  members: { uid: string; nickname: string }[]
  onSettle: (userId: string, amount: number, currency: Currency) => void
}) {
  if (balances.length === 0) {
    return (
      <div className="flex items-center gap-3 p-4 rounded-xl bg-green-50 dark:bg-green-950/30 border border-green-200 dark:border-green-900">
        <Check size={18} className="text-green-600 dark:text-green-400 shrink-0" />
        <p className="text-sm font-semibold text-green-700 dark:text-green-300">All squared up — no outstanding balances.</p>
      </div>
    )
  }

  return (
    <div className="space-y-2">
      {balances.filter(b => b.amount < 0).map(b => {
        const member = members.find(m => m.uid === b.userId)
        return (
          <div key={`${b.userId}-${b.currency}`}
            className="flex items-center gap-3 p-4 rounded-xl border border-orange-200 dark:border-orange-900 bg-orange-50 dark:bg-orange-950/30">
            <div className="w-9 h-9 rounded-full bg-orange-100 dark:bg-orange-900/50 flex items-center justify-center shrink-0">
              <ArrowUpRight size={16} className="text-orange-600 dark:text-orange-400" />
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-xs font-bold text-orange-700 dark:text-orange-400 uppercase tracking-wide">You owe</p>
              <p className="text-sm font-bold text-orange-900 dark:text-orange-100">{member?.nickname ?? '…'}</p>
              <p className="text-xl font-extrabold text-orange-700 dark:text-orange-300">{formatAmount(b.amount, b.currency)}</p>
            </div>
            <Button size="sm" className="bg-orange-600 hover:bg-orange-700 text-white font-bold shrink-0"
              onClick={() => onSettle(b.userId, Math.abs(b.amount), b.currency)}>
              Settle Up
            </Button>
          </div>
        )
      })}
      {balances.filter(b => b.amount > 0).map(b => {
        const member = members.find(m => m.uid === b.userId)
        return (
          <div key={`${b.userId}-${b.currency}`}
            className="flex items-center gap-3 p-4 rounded-xl border border-green-200 dark:border-green-900 bg-green-50 dark:bg-green-950/30">
            <div className="w-9 h-9 rounded-full bg-green-100 dark:bg-green-900/50 flex items-center justify-center shrink-0">
              <ArrowDownLeft size={16} className="text-green-600 dark:text-green-400" />
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-xs font-bold text-green-700 dark:text-green-400 uppercase tracking-wide">Owes you</p>
              <p className="text-sm font-bold text-green-900 dark:text-green-100">{member?.nickname ?? '…'}</p>
              <p className="text-xl font-extrabold text-green-700 dark:text-green-300">{formatAmount(b.amount, b.currency)}</p>
            </div>
          </div>
        )
      })}
    </div>
  )
}

// ── Add / Edit Expense Modal ─────────────────────────────────────────────────

function ExpenseModal({
  members, currentUserId, initial, onSave, onClose,
}: {
  members: { uid: string; nickname: string }[]
  currentUserId: string
  initial?: Partial<Expense>
  onSave: (data: Omit<Expense, 'id' | 'createdAt'>) => Promise<void>
  onClose: () => void
}) {
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [form, setForm] = useState({
    category: (initial?.category ?? 'other') as ExpenseCategory,
    description: initial?.description ?? '',
    amount: initial?.amount?.toString() ?? '',
    currency: (initial?.currency ?? 'INR') as Currency,
    date: initial?.date ?? todayStr(),
    paidBy: initial?.paidBy ?? currentUserId,
    splitAmong: initial?.splitAmong ?? members.map(m => m.uid),
    splitType: (initial?.splitType ?? 'equal') as 'equal' | 'percent' | 'custom',
    customSplits: Object.fromEntries(members.map(m => [m.uid, initial?.splits?.[m.uid]?.toString() ?? ''])),
    percentSplits: Object.fromEntries(members.map(m => [m.uid, ''])),
    note: initial?.note ?? '',
    deferToNextMonth: initial?.deferToNextMonth ?? false,
  })

  const totalAmount = parseFloat(form.amount) || 0
  const selectedCount = form.splitAmong.length
  const equalShare = selectedCount > 0 ? totalAmount / selectedCount : 0
  const customTotal = form.splitAmong.reduce((s, uid) => s + (parseFloat(form.customSplits[uid]) || 0), 0)
  const customOk = Math.abs(customTotal - totalAmount) <= 0.5
  const totalPercent = form.splitAmong.reduce((s, uid) => s + (parseFloat(form.percentSplits[uid]) || 0), 0)
  const percentOk = Math.abs(totalPercent - 100) <= 1

  const toggleMember = (uid: string) => setForm(f => ({
    ...f,
    splitAmong: f.splitAmong.includes(uid) ? f.splitAmong.filter(id => id !== uid) : [...f.splitAmong, uid],
  }))

  const handleSave = async () => {
    setError('')
    if (!form.description.trim()) { setError('Add a description.'); return }
    if (!totalAmount || totalAmount <= 0) { setError('Enter a valid amount.'); return }
    if (form.splitAmong.length === 0) { setError('Select at least one person to split with.'); return }
    if (form.splitType === 'custom' && !customOk) {
      setError(`Custom splits must add up to ${CURRENCY_SYMBOLS[form.currency]}${totalAmount}.`); return
    }
    if (form.splitType === 'percent' && !percentOk) {
      setError('Percentages must add up to 100%.'); return
    }

    let splits: Record<string, number>
    if (form.splitType === 'equal') {
      splits = Object.fromEntries(form.splitAmong.map(uid => [uid, equalShare]))
    } else if (form.splitType === 'percent') {
      splits = Object.fromEntries(form.splitAmong.map(uid => [uid,
        Math.round((parseFloat(form.percentSplits[uid]) || 0) / 100 * totalAmount * 100) / 100
      ]))
    } else {
      splits = Object.fromEntries(form.splitAmong.map(uid => [uid, parseFloat(form.customSplits[uid]) || 0]))
    }

    setSaving(true)
    await onSave({
      description: form.description.trim(),
      amount: totalAmount, currency: form.currency,
      paidBy: form.paidBy, splitAmong: form.splitAmong,
      splitType: form.splitType, splits, category: form.category,
      date: form.date, note: form.note.trim() || undefined,
      deferToNextMonth: form.deferToNextMonth ? true : undefined,
      createdBy: currentUserId,
    })
    onClose()
  }

  return (
    <Modal title="Add Shared Expense" onClose={onClose}>
      <div className="space-y-5">
        {/* Category grid */}
        <div>
          <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-2 block">Category</Label>
          <div className="grid grid-cols-4 gap-1.5">
            {(Object.entries(CATEGORY_CONFIG) as [ExpenseCategory, { label: string; emoji: string }][]).map(([key, { label, emoji }]) => (
              <button key={key}
                onClick={() => setForm(f => ({ ...f, category: key, description: f.description || label }))}
                className={`flex flex-col items-center gap-1 p-2 rounded-xl border text-center transition-all ${
                  form.category === key
                    ? 'border-primary bg-primary/10 text-primary'
                    : 'border-border bg-secondary/30 text-muted-foreground hover:text-foreground'
                }`}>
                <span className="text-lg leading-none">{emoji}</span>
                <span className="text-[10px] font-semibold leading-tight">{label}</span>
              </button>
            ))}
          </div>
        </div>

        <div>
          <Label htmlFor="desc" className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-1.5 block">Description</Label>
          <Input id="desc" placeholder="e.g. June Electricity Bill" value={form.description}
            onChange={e => setForm(f => ({ ...f, description: e.target.value }))} maxLength={80} />
        </div>

        <div className="grid grid-cols-3 gap-2">
          <div className="col-span-2">
            <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-1.5 block">Amount</Label>
            <Input type="number" placeholder="0" min="0" value={form.amount}
              onChange={e => setForm(f => ({ ...f, amount: e.target.value }))} />
          </div>
          <div>
            <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-1.5 block">Currency</Label>
            <select value={form.currency} onChange={e => setForm(f => ({ ...f, currency: e.target.value as Currency }))}
              className="w-full h-9 rounded-md border border-input bg-background px-2 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-ring">
              {CURRENCIES.map(c => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-2">
          <div>
            <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-1.5 block">Date</Label>
            <Input type="date" value={form.date} onChange={e => setForm(f => ({ ...f, date: e.target.value }))} />
          </div>
          <div>
            <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-1.5 block">Paid by</Label>
            <select value={form.paidBy} onChange={e => setForm(f => ({ ...f, paidBy: e.target.value }))}
              className="w-full h-9 rounded-md border border-input bg-background px-2 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-ring">
              {members.map(m => <option key={m.uid} value={m.uid}>{m.uid === currentUserId ? `${m.nickname} (you)` : m.nickname}</option>)}
            </select>
          </div>
        </div>

        <div>
          <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-2 block">Split among</Label>
          <div className="flex flex-wrap gap-2">
            {members.map(m => (
              <button key={m.uid} onClick={() => toggleMember(m.uid)}
                className={`px-3 py-1.5 rounded-full text-xs font-bold border transition-all ${
                  form.splitAmong.includes(m.uid)
                    ? 'bg-primary text-primary-foreground border-primary'
                    : 'bg-secondary text-muted-foreground border-border'
                }`}>
                {m.uid === currentUserId ? `${m.nickname} (you)` : m.nickname}
              </button>
            ))}
          </div>
        </div>

        {selectedCount > 0 && totalAmount > 0 && (
          <div>
            <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-2 block">Split type</Label>
            <div className="flex gap-2 mb-3">
              {(['equal', 'percent', 'custom'] as const).map(t => (
                <button key={t} onClick={() => setForm(f => ({ ...f, splitType: t }))}
                  className={`flex-1 py-2 rounded-xl border text-xs font-bold transition-all ${
                    form.splitType === t ? 'bg-primary text-primary-foreground border-primary' : 'bg-secondary text-muted-foreground border-border'
                  }`}>
                  {t === 'equal' ? 'Equal' : t === 'percent' ? 'By %' : 'Custom'}
                </button>
              ))}
            </div>

            {form.splitType === 'equal' && (
              <p className="text-xs text-muted-foreground bg-secondary/60 rounded-lg px-3 py-2">
                Each person pays{' '}
                <span className="font-bold text-foreground">
                  {CURRENCY_SYMBOLS[form.currency]}{equalShare % 1 === 0 ? equalShare.toFixed(0) : equalShare.toFixed(2)}
                </span>
              </p>
            )}

            {form.splitType === 'percent' && (
              <div className="space-y-2">
                {form.splitAmong.map(uid => {
                  const m = members.find(x => x.uid === uid)
                  const pct = parseFloat(form.percentSplits[uid]) || 0
                  const share = (pct / 100) * totalAmount
                  return (
                    <div key={uid} className="flex items-center gap-2">
                      <span className="text-sm font-medium w-24 truncate shrink-0">
                        {uid === currentUserId ? `${m?.nickname} (you)` : m?.nickname ?? uid}
                      </span>
                      <div className="relative flex-1">
                        <Input type="number" placeholder="0" min="0" max="100" className="pr-7"
                          value={form.percentSplits[uid]}
                          onChange={e => setForm(f => ({ ...f, percentSplits: { ...f.percentSplits, [uid]: e.target.value } }))} />
                        <span className="absolute right-2.5 top-1/2 -translate-y-1/2 text-xs text-muted-foreground">%</span>
                      </div>
                      {pct > 0 && (
                        <span className="text-xs font-bold text-muted-foreground w-16 text-right shrink-0">
                          {CURRENCY_SYMBOLS[form.currency]}{share.toFixed(share % 1 === 0 ? 0 : 2)}
                        </span>
                      )}
                    </div>
                  )
                })}
                <div className={`text-xs px-3 py-2 rounded-lg font-medium ${percentOk ? 'bg-green-50 dark:bg-green-950/30 text-green-700 dark:text-green-300' : 'bg-orange-50 dark:bg-orange-950/30 text-orange-700 dark:text-orange-300'}`}>
                  {percentOk ? '✓ Percentages add up to 100%' : `Total: ${totalPercent.toFixed(0)}% of 100%`}
                </div>
              </div>
            )}

            {form.splitType === 'custom' && (
              <div className="space-y-2">
                {form.splitAmong.map(uid => {
                  const m = members.find(x => x.uid === uid)
                  return (
                    <div key={uid} className="flex items-center gap-2">
                      <span className="text-sm font-medium w-24 truncate shrink-0">
                        {uid === currentUserId ? `${m?.nickname} (you)` : m?.nickname ?? uid}
                      </span>
                      <div className="relative flex-1">
                        <span className="absolute left-2.5 top-1/2 -translate-y-1/2 text-xs text-muted-foreground">{CURRENCY_SYMBOLS[form.currency]}</span>
                        <Input type="number" placeholder="0" min="0" className="pl-6"
                          value={form.customSplits[uid]}
                          onChange={e => setForm(f => ({ ...f, customSplits: { ...f.customSplits, [uid]: e.target.value } }))} />
                      </div>
                    </div>
                  )
                })}
                <div className={`text-xs px-3 py-2 rounded-lg font-medium ${customOk ? 'bg-green-50 dark:bg-green-950/30 text-green-700 dark:text-green-300' : 'bg-orange-50 dark:bg-orange-950/30 text-orange-700 dark:text-orange-300'}`}>
                  {customOk ? '✓ Splits add up correctly' : `Total: ${CURRENCY_SYMBOLS[form.currency]}${customTotal.toFixed(2)} of ${CURRENCY_SYMBOLS[form.currency]}${totalAmount.toFixed(2)}`}
                </div>
              </div>
            )}
          </div>
        )}

        <div>
          <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-1.5 block">Note (optional)</Label>
          <Input placeholder="Any details…" value={form.note}
            onChange={e => setForm(f => ({ ...f, note: e.target.value }))} maxLength={200} />
        </div>

        {/* Defer to next month — only shown to the payer */}
        {form.paidBy === currentUserId && (
          <div className="flex items-center gap-3 p-3 rounded-xl border border-border bg-secondary/30">
            <button onClick={() => setForm(f => ({ ...f, deferToNextMonth: !f.deferToNextMonth }))}
              className={`w-10 h-6 rounded-full transition-colors shrink-0 ${form.deferToNextMonth ? 'bg-primary' : 'bg-border'}`}>
              <div className={`w-4 h-4 rounded-full bg-white shadow mx-1 transition-transform ${form.deferToNextMonth ? 'translate-x-4' : 'translate-x-0'}`} />
            </button>
            <div>
              <p className="text-sm font-semibold">Add to next month&apos;s bill</p>
              <p className="text-xs text-muted-foreground">
                {form.deferToNextMonth
                  ? 'Will appear with next month\'s bills, not collected immediately'
                  : 'Collect from flatmates right away'}
              </p>
            </div>
          </div>
        )}

        {error && (
          <div className="flex items-center gap-2 p-3 rounded-xl bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-900">
            <AlertCircle size={14} className="text-red-600 shrink-0" />
            <p className="text-xs font-semibold text-red-700 dark:text-red-300">{error}</p>
          </div>
        )}
        <div className="flex gap-2 pt-1">
          <Button variant="outline" className="flex-1" onClick={onClose}>Cancel</Button>
          <Button className="flex-1 font-bold" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving…' : 'Add Expense'}
          </Button>
        </div>
      </div>
    </Modal>
  )
}

// ── Settle Up Modal ──────────────────────────────────────────────────────────

function SettleUpModal({
  preToUserId, preAmount, preCurrency, members, currentUserId, onSettle, onClose,
}: {
  preToUserId: string; preAmount: number; preCurrency: Currency
  members: { uid: string; nickname: string }[]
  currentUserId: string
  onSettle: (data: Omit<Settlement, 'id' | 'createdAt'>) => Promise<void>
  onClose: () => void
}) {
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState({
    toUserId: preToUserId,
    amount: preAmount > 0 ? preAmount.toFixed(preCurrency === 'INR' ? 0 : 2) : '',
    currency: preCurrency,
    note: '', date: todayStr(),
  })
  const toMember = members.find(m => m.uid === form.toUserId)

  const handleSave = async () => {
    const amt = parseFloat(form.amount)
    if (!amt || amt <= 0) return
    setSaving(true)
    await onSettle({ fromUserId: currentUserId, toUserId: form.toUserId, amount: amt, currency: form.currency, note: form.note.trim() || undefined, date: form.date })
    onClose()
  }

  return (
    <Modal title="Settle Up" onClose={onClose}>
      <div className="space-y-5">
        <div className="p-4 rounded-xl bg-secondary/50 text-center">
          <p className="text-sm text-muted-foreground">You&apos;re paying</p>
          <p className="text-2xl font-extrabold mt-1">{toMember?.nickname ?? '…'}</p>
        </div>
        <div className="grid grid-cols-3 gap-2">
          <div className="col-span-2">
            <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-1.5 block">Amount</Label>
            <Input type="number" placeholder="0" min="0" value={form.amount}
              onChange={e => setForm(f => ({ ...f, amount: e.target.value }))} />
          </div>
          <div>
            <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-1.5 block">Currency</Label>
            <select value={form.currency} onChange={e => setForm(f => ({ ...f, currency: e.target.value as Currency }))}
              className="w-full h-9 rounded-md border border-input bg-background px-2 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-ring">
              {CURRENCIES.map(c => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
        </div>
        <div>
          <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-1.5 block">Date</Label>
          <Input type="date" value={form.date} onChange={e => setForm(f => ({ ...f, date: e.target.value }))} />
        </div>
        <div>
          <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-1.5 block">Note (optional)</Label>
          <Input placeholder="e.g. Paid via UPI" value={form.note}
            onChange={e => setForm(f => ({ ...f, note: e.target.value }))} maxLength={100} />
        </div>
        <div className="flex gap-2 pt-1">
          <Button variant="outline" className="flex-1" onClick={onClose}>Cancel</Button>
          <Button className="flex-1 font-bold bg-green-600 hover:bg-green-700 text-white" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving…' : 'Mark as Paid'}
          </Button>
        </div>
      </div>
    </Modal>
  )
}

// ── Expense Row ──────────────────────────────────────────────────────────────

function ExpenseRow({ expense, members, currentUserId, canDelete, onDelete }: {
  expense: Expense; members: { uid: string; nickname: string }[]
  currentUserId: string; canDelete: boolean; onDelete: (id: string) => void
}) {
  const [expanded, setExpanded] = useState(false)
  const cfg = CATEGORY_CONFIG[expense.category]
  const payer = members.find(m => m.uid === expense.paidBy)
  const isYou = expense.paidBy === currentUserId
  const myShare = expense.splits[currentUserId]

  return (
    <div className={`border rounded-xl bg-card overflow-hidden ${expense.deferToNextMonth ? 'border-blue-200 dark:border-blue-900' : 'border-border'}`}>
      <button onClick={() => setExpanded(e => !e)}
        className="w-full flex items-center gap-3 p-4 text-left hover:bg-secondary/30 transition-colors">
        <div className="w-10 h-10 rounded-xl bg-secondary flex items-center justify-center shrink-0 text-xl">{cfg?.emoji ?? '💰'}</div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-1.5 flex-wrap">
            <p className="text-sm font-semibold truncate">{expense.description}</p>
            {expense.deferToNextMonth && (
              <span className="text-[10px] font-extrabold bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-300 px-1.5 py-0.5 rounded-full shrink-0">DEFERRED</span>
            )}
          </div>
          <p className="text-xs text-muted-foreground mt-0.5">
            {isYou ? 'You paid' : `${payer?.nickname ?? '…'} paid`}
            {' · '}
            {new Date(expense.date + 'T00:00:00').toLocaleDateString('en-IN', { day: 'numeric', month: 'short' })}
          </p>
        </div>
        <div className="text-right shrink-0">
          <p className="text-sm font-extrabold">{formatAmount(expense.amount, expense.currency)}</p>
          {myShare != null && (
            <p className={`text-xs font-medium mt-0.5 ${isYou ? 'text-green-600 dark:text-green-400' : 'text-muted-foreground'}`}>
              {isYou
                ? `you get back ${formatAmount(expense.amount - myShare, expense.currency)}`
                : `your share: ${formatAmount(myShare, expense.currency)}`}
            </p>
          )}
        </div>
      </button>
      {expanded && (
        <div className="px-4 pb-4 border-t border-border/50 pt-3 space-y-2">
          {expense.deferToNextMonth && (
            <p className="text-xs text-blue-600 dark:text-blue-400 font-semibold">Collecting with next month&apos;s bills</p>
          )}
          {expense.splitAmong.map(uid => {
            const m = members.find(x => x.uid === uid)
            return (
              <div key={uid} className="flex justify-between items-center text-xs">
                <span className="font-medium text-muted-foreground">
                  {uid === currentUserId ? `${m?.nickname} (you)` : m?.nickname ?? uid}
                  {uid === expense.paidBy && ' · paid'}
                </span>
                <span className="font-bold">{formatAmount(expense.splits[uid] ?? 0, expense.currency)}</span>
              </div>
            )
          })}
          {expense.note && <p className="text-xs text-muted-foreground italic border-t border-border/50 pt-2">&ldquo;{expense.note}&rdquo;</p>}
          {canDelete && (
            <button onClick={() => onDelete(expense.id)}
              className="flex items-center gap-1.5 text-xs text-red-500 hover:text-red-600 font-semibold mt-1">
              <Trash2 size={12} /> Delete expense
            </button>
          )}
        </div>
      )}
    </div>
  )
}

// ── Monthly Bill Form Modal ───────────────────────────────────────────────────

function MonthlyBillModal({
  members, currentUserId, initial, onSave, onClose,
}: {
  members: { uid: string; nickname: string }[]
  currentUserId: string
  initial?: RecurringBill
  onSave: (data: Omit<RecurringBill, 'id' | 'createdAt' | 'currentPayerIndex' | 'lastGeneratedMonth'>) => Promise<void>
  onClose: () => void
}) {
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState({
    name: initial?.name ?? '',
    category: (initial?.category ?? 'other') as ExpenseCategory,
    isVariable: initial?.isVariable ?? false,
    amount: initial?.amount != null ? initial.amount.toString() : '',
    currency: (initial?.currency ?? 'INR') as Currency,
    billingDay: initial?.billingDay?.toString() ?? '1',
    rotationQueue: initial?.rotationQueue ?? members.map(m => m.uid),
    active: initial?.active ?? true,
  })

  const toggleMember = (uid: string) => setForm(f => ({
    ...f,
    rotationQueue: f.rotationQueue.includes(uid) ? f.rotationQueue.filter(id => id !== uid) : [...f.rotationQueue, uid],
  }))

  const handleSave = async () => {
    if (!form.name.trim()) return
    if (!form.isVariable && (!parseFloat(form.amount) || parseFloat(form.amount) <= 0)) return
    if (form.rotationQueue.length === 0) return
    setSaving(true)
    await onSave({
      name: form.name.trim(),
      category: form.category,
      isVariable: form.isVariable,
      amount: form.isVariable ? null : parseFloat(form.amount),
      currency: form.currency,
      billingDay: Math.min(28, Math.max(1, parseInt(form.billingDay) || 1)),
      rotationQueue: form.rotationQueue,
      active: form.active,
      createdBy: currentUserId,
    })
    onClose()
  }

  const firstPayerUid = form.rotationQueue[0]
  const firstPayer = members.find(m => m.uid === firstPayerUid)
  const perPersonAmt = parseFloat(form.amount) > 0 && form.rotationQueue.length > 0
    ? parseFloat(form.amount) / form.rotationQueue.length
    : 0

  return (
    <Modal title={initial ? 'Edit Monthly Bill' : 'New Monthly Bill'} onClose={onClose}>
      <div className="space-y-5">
        {/* Category */}
        <div>
          <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-2 block">Category</Label>
          <div className="grid grid-cols-4 gap-1.5">
            {(Object.entries(CATEGORY_CONFIG) as [ExpenseCategory, { label: string; emoji: string }][]).map(([key, { label, emoji }]) => (
              <button key={key}
                onClick={() => setForm(f => ({ ...f, category: key, name: f.name || label }))}
                className={`flex flex-col items-center gap-1 p-2 rounded-xl border text-center transition-all ${
                  form.category === key ? 'border-primary bg-primary/10 text-primary' : 'border-border bg-secondary/30 text-muted-foreground hover:text-foreground'
                }`}>
                <span className="text-lg leading-none">{emoji}</span>
                <span className="text-[10px] font-semibold leading-tight">{label}</span>
              </button>
            ))}
          </div>
        </div>

        <div>
          <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-1.5 block">Bill name</Label>
          <Input placeholder="e.g. Rent, Electricity, Water" value={form.name}
            onChange={e => setForm(f => ({ ...f, name: e.target.value }))} maxLength={50} />
        </div>

        {/* Variable toggle */}
        <div className="flex items-center gap-3 p-3 rounded-xl border border-border bg-secondary/30">
          <button onClick={() => setForm(f => ({ ...f, isVariable: !f.isVariable }))}
            className={`w-10 h-6 rounded-full transition-colors shrink-0 ${form.isVariable ? 'bg-primary' : 'bg-border'}`}>
            <div className={`w-4 h-4 rounded-full bg-white shadow mx-1 transition-transform ${form.isVariable ? 'translate-x-4' : 'translate-x-0'}`} />
          </button>
          <div>
            <p className="text-sm font-semibold">{form.isVariable ? 'Variable amount' : 'Fixed amount'}</p>
            <p className="text-xs text-muted-foreground">
              {form.isVariable
                ? 'Amount changes each month — enter it when generating (e.g. electricity meter reading)'
                : 'Same amount every month — stays until you change it (e.g. rent)'}
            </p>
          </div>
        </div>

        {!form.isVariable && (
          <div className="grid grid-cols-3 gap-2">
            <div className="col-span-2">
              <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-1.5 block">Amount per month</Label>
              <Input type="number" placeholder="0" min="0" value={form.amount}
                onChange={e => setForm(f => ({ ...f, amount: e.target.value }))} />
            </div>
            <div>
              <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-1.5 block">Currency</Label>
              <select value={form.currency} onChange={e => setForm(f => ({ ...f, currency: e.target.value as Currency }))}
                className="w-full h-9 rounded-md border border-input bg-background px-2 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-ring">
                {CURRENCIES.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>
          </div>
        )}

        <div>
          <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-1.5 block">Generate on the</Label>
          <div className="flex items-center gap-2">
            <Input type="number" min="1" max="28" className="w-20" value={form.billingDay}
              onChange={e => setForm(f => ({ ...f, billingDay: e.target.value }))} />
            <span className="text-sm text-muted-foreground">of each month</span>
          </div>
          <p className="text-xs text-muted-foreground mt-1">After this date, the bill appears as due for that month.</p>
        </div>

        <div>
          <Label className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-2 block">Split among</Label>
          <p className="text-xs text-muted-foreground mb-2">Bill is split equally among all selected members.</p>
          <div className="flex flex-wrap gap-2">
            {members.map(m => (
              <button key={m.uid} onClick={() => toggleMember(m.uid)}
                className={`px-3 py-1.5 rounded-full text-xs font-bold border transition-all ${
                  form.rotationQueue.includes(m.uid)
                    ? 'bg-primary text-primary-foreground border-primary'
                    : 'bg-secondary text-muted-foreground border-border'
                }`}>
                {m.uid === currentUserId ? `${m.nickname} (you)` : m.nickname}
              </button>
            ))}
          </div>
          {perPersonAmt > 0 && (
            <p className="text-xs text-muted-foreground mt-2">
              Each person pays: <span className="font-bold text-foreground">{CURRENCY_SYMBOLS[form.currency]}{perPersonAmt % 1 === 0 ? perPersonAmt.toFixed(0) : perPersonAmt.toFixed(2)}</span>
            </p>
          )}
          {form.rotationQueue.length > 0 && firstPayer && (
            <p className="text-xs text-muted-foreground mt-1">
              Pays this month: <span className="font-semibold text-foreground">{firstPayerUid === currentUserId ? `${firstPayer.nickname} (you)` : firstPayer.nickname}</span>
              {' '}· advances automatically each month
            </p>
          )}
        </div>

        <div className="flex gap-2 pt-1">
          <Button variant="outline" className="flex-1" onClick={onClose}>Cancel</Button>
          <Button className="flex-1 font-bold" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving…' : initial ? 'Save Changes' : 'Create Bill'}
          </Button>
        </div>
      </div>
    </Modal>
  )
}

// ── Generate Bills Modal ─────────────────────────────────────────────────────

function GenerateBillsModal({
  dueBills, members, onGenerate, onClose,
}: {
  dueBills: RecurringBill[]
  members: { uid: string; nickname: string }[]
  onGenerate: (billId: string, amount?: number) => Promise<void>
  onClose: () => void
}) {
  const [amounts, setAmounts] = useState<Record<string, string>>({})
  const [selected, setSelected] = useState<Set<string>>(new Set(dueBills.map(b => b.id)))
  const [generating, setGenerating] = useState(false)
  const [done, setDone] = useState<Set<string>>(new Set())

  const toggleSelected = (billId: string) => setSelected(s => {
    const next = new Set(s)
    next.has(billId) ? next.delete(billId) : next.add(billId)
    return next
  })

  const runningTotal = dueBills.reduce((sum, bill) => {
    if (!selected.has(bill.id) || done.has(bill.id)) return sum
    const amt = bill.isVariable ? (parseFloat(amounts[bill.id]) || 0) : (bill.amount ?? 0)
    return sum + amt
  }, 0)

  const selectedBills = dueBills.filter(b => selected.has(b.id) && !done.has(b.id))

  const canGenerate = selectedBills.length > 0 && selectedBills.every(b =>
    (!b.isVariable && b.amount && b.amount > 0) || (b.isVariable && parseFloat(amounts[b.id]) > 0)
  )

  const handleGenerate = async () => {
    setGenerating(true)
    for (const bill of selectedBills) {
      const amount = bill.isVariable ? parseFloat(amounts[bill.id]) : bill.amount
      if (!amount || amount <= 0) continue
      await onGenerate(bill.id, bill.isVariable ? amount : undefined)
      setDone(d => new Set([...d, bill.id]))
    }
    setGenerating(false)
    onClose()
  }

  return (
    <Modal title={`Generate ${monthLabel(currentMonthKey())} Bills`} onClose={onClose}>
      <div className="space-y-3">
        <p className="text-sm text-muted-foreground">
          Confirm the amounts below. Each bill is split equally among all members and added to the expense history.
        </p>

        {dueBills.map(bill => {
          const cfg = CATEGORY_CONFIG[bill.category]
          const payerUid = bill.rotationQueue[bill.currentPayerIndex % bill.rotationQueue.length]
          const payer = members.find(m => m.uid === payerUid)
          const isDone = done.has(bill.id)
          const isSelected = selected.has(bill.id)
          const enteredAmt = bill.isVariable ? parseFloat(amounts[bill.id]) || 0 : (bill.amount ?? 0)
          const perPerson = bill.rotationQueue.length > 0 ? enteredAmt / bill.rotationQueue.length : 0

          return (
            <div key={bill.id}
              className={`rounded-xl border transition-all overflow-hidden ${
                isDone ? 'border-green-300 dark:border-green-800 opacity-60' :
                isSelected ? 'border-primary' : 'border-border opacity-50'
              }`}>
              <button
                onClick={() => !isDone && toggleSelected(bill.id)}
                disabled={isDone}
                className={`w-full flex items-center gap-3 p-4 text-left transition-colors ${
                  isDone ? 'bg-green-50 dark:bg-green-950/30' :
                  isSelected ? 'bg-primary/5' : 'bg-card hover:bg-secondary/30'
                }`}>
                <div className={`w-5 h-5 rounded-md border-2 flex items-center justify-center shrink-0 transition-all ${
                  isDone ? 'bg-green-500 border-green-500' :
                  isSelected ? 'bg-primary border-primary' : 'border-muted-foreground/40'
                }`}>
                  {(isDone || isSelected) && <Check size={11} className="text-white" strokeWidth={3} />}
                </div>

                <span className="text-xl shrink-0">{cfg?.emoji}</span>

                <div className="flex-1 min-w-0 text-left">
                  <p className="font-bold text-sm">{bill.name}</p>
                  <p className="text-xs text-muted-foreground">
                    Paid by <span className="font-semibold text-foreground">{payer?.nickname ?? '…'}</span>
                    {!bill.isVariable && bill.amount ? ` · ${formatAmount(bill.amount, bill.currency)} total` : ''}
                    {perPerson > 0 && ` · ${formatAmount(perPerson, bill.currency)} per person`}
                  </p>
                </div>

                {!bill.isVariable && bill.amount ? (
                  <span className="text-sm font-extrabold shrink-0">{formatAmount(bill.amount, bill.currency)}</span>
                ) : null}
              </button>

              {bill.isVariable && isSelected && !isDone && (
                <div className="px-4 pb-4 border-t border-border/50 pt-3">
                  <Label className="text-xs font-bold text-muted-foreground mb-1.5 block">
                    Enter actual amount for {monthLabel(currentMonthKey())}
                  </Label>
                  <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-sm font-medium text-muted-foreground">
                      {CURRENCY_SYMBOLS[bill.currency]}
                    </span>
                    <Input type="number" placeholder="0" min="0" className="pl-7"
                      value={amounts[bill.id] ?? ''}
                      onChange={e => setAmounts(a => ({ ...a, [bill.id]: e.target.value }))} />
                  </div>
                  {parseFloat(amounts[bill.id]) > 0 && bill.rotationQueue.length > 0 && (
                    <p className="text-xs text-muted-foreground mt-1.5">
                      Each person pays: <span className="font-bold text-foreground">
                        {formatAmount(parseFloat(amounts[bill.id]) / bill.rotationQueue.length, bill.currency)}
                      </span>
                    </p>
                  )}
                </div>
              )}
            </div>
          )
        })}

        {runningTotal > 0 && (
          <div className="rounded-xl border border-primary/30 bg-primary/5 p-4 space-y-1">
            <div className="flex justify-between items-center">
              <span className="text-sm font-bold">Total this month</span>
              <span className="text-lg font-extrabold">{formatAmount(runningTotal, 'INR')}</span>
            </div>
            <p className="text-xs text-muted-foreground">
              {selected.size - done.size} bill{selected.size - done.size !== 1 ? 's' : ''} · split equally among all members
            </p>
          </div>
        )}

        <div className="flex gap-2 pt-1">
          <Button variant="outline" className="flex-1" onClick={onClose}>Cancel</Button>
          <Button className="flex-1 font-bold" onClick={handleGenerate} disabled={generating || !canGenerate}>
            {generating ? 'Generating…' : `Generate (${selectedBills.length})`}
          </Button>
        </div>
      </div>
    </Modal>
  )
}

// ── Bill Instance Card ────────────────────────────────────────────────────────
// Shows the current month's generated instance for a recurring bill template.

const INSTANCE_STATUS_CONFIG: Record<BillInstanceStatus, {
  label: string; bg: string; text: string; dot: string
}> = {
  pending:         { label: 'Amount Needed', bg: 'bg-amber-100 dark:bg-amber-900/30', text: 'text-amber-700 dark:text-amber-300', dot: 'bg-amber-500' },
  split_generated: { label: 'Split Ready',   bg: 'bg-brand-100 dark:bg-brand-900/20', text: 'text-brand-700 dark:text-brand-400', dot: 'bg-brand-600' },
  paid:            { label: 'Paid',           bg: 'bg-emerald-100 dark:bg-emerald-900/30', text: 'text-emerald-700 dark:text-emerald-300', dot: 'bg-emerald-500' },
  overdue:         { label: 'Overdue',        bg: 'bg-red-100 dark:bg-red-900/30', text: 'text-red-700 dark:text-red-400', dot: 'bg-red-500' },
  skipped:         { label: 'Skipped',        bg: 'bg-secondary', text: 'text-muted-foreground', dot: 'bg-muted-foreground' },
}

function BillInstanceCard({
  instance, members, currentUserId, isAdmin, onConfirmAmount, onMarkPaid, onSkip,
}: {
  instance: BillInstance
  members: { uid: string; nickname: string }[]
  currentUserId: string
  isAdmin: boolean
  onConfirmAmount: (instanceId: string, amount: number) => void
  onMarkPaid: (instanceId: string) => void
  onSkip: (instanceId: string) => void
}) {
  const [showAmountInput, setShowAmountInput] = useState(false)
  const [amountInput, setAmountInput] = useState('')
  const cfg = INSTANCE_STATUS_CONFIG[instance.status]
  const payer = members.find(m => m.uid === instance.paidBy)
  const isYouPayer = instance.paidBy === currentUserId
  const myShare = instance.splits?.[currentUserId]

  return (
    <div className="rounded-xl border border-border/50 bg-secondary/20 overflow-hidden">
      {/* Status header */}
      <div className="flex items-center justify-between px-4 py-2.5 border-b border-border/40">
        <div className="flex items-center gap-2">
          <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${cfg.dot}`} />
          <span className={`text-[11px] font-bold uppercase tracking-wider ${cfg.text}`}>
            {cfg.label}
          </span>
        </div>
        <div className="flex items-center gap-2">
          {/* Payer can mark paid; admin can skip */}
          {instance.status === 'split_generated' && isYouPayer && (
            <button
              onClick={() => onMarkPaid(instance.id)}
              className="flex items-center gap-1 text-[11px] font-bold text-emerald-600 dark:text-emerald-400 hover:opacity-75 transition-opacity"
            >
              <Check size={11} /> Mark paid
            </button>
          )}
          {instance.status !== 'skipped' && instance.status !== 'paid' && isAdmin && (
            <button
              onClick={() => onSkip(instance.id)}
              className="text-[11px] font-semibold text-muted-foreground hover:text-foreground transition-colors"
            >
              Skip
            </button>
          )}
        </div>
      </div>

      {/* Body */}
      <div className="px-4 py-3">
        {/* Amount + payer */}
        <div className="flex items-center justify-between gap-3 mb-2">
          <div>
            <p className="text-xs text-muted-foreground">
              {isYouPayer ? 'You pay' : `${payer?.nickname ?? '…'} pays`}
            </p>
            {instance.amount !== null ? (
              <p className="text-base font-extrabold tracking-tight">
                {formatAmount(instance.amount, instance.currency)}
              </p>
            ) : (
              <p className="text-sm font-semibold text-muted-foreground italic">Amount not entered</p>
            )}
          </div>
          {myShare != null && myShare > 0 && (
            <div className="text-right">
              <p className="text-xs text-muted-foreground">Your share</p>
              <p className={`text-sm font-extrabold ${isYouPayer ? 'text-emerald-600 dark:text-emerald-400' : ''}`}>
                {formatAmount(myShare, instance.currency)}
              </p>
            </div>
          )}
        </div>

        {/* Per-person breakdown (when splits ready) */}
        {instance.splits && instance.status !== 'skipped' && (
          <div className="grid grid-cols-2 gap-1 mt-2">
            {instance.participants.map(uid => {
              const m = members.find(x => x.uid === uid)
              const share = instance.splits![uid] ?? 0
              const isPayer = uid === instance.paidBy
              return (
                <div key={uid} className={`flex justify-between items-center rounded-lg px-2.5 py-1.5 text-xs ${isPayer ? 'bg-emerald-50 dark:bg-emerald-900/20' : 'bg-secondary/50'}`}>
                  <span className={`font-medium truncate ${isPayer ? 'text-emerald-700 dark:text-emerald-300' : 'text-muted-foreground'}`}>
                    {uid === currentUserId ? `${m?.nickname} (you)` : m?.nickname ?? uid}
                    {isPayer && ' ·  pays'}
                  </span>
                  <span className={`font-bold ml-2 shrink-0 ${isPayer ? 'text-emerald-700 dark:text-emerald-300' : ''}`}>
                    {formatAmount(share, instance.currency)}
                  </span>
                </div>
              )
            })}
          </div>
        )}

        {/* Enter amount for pending variable bill */}
        {instance.status === 'pending' && isAdmin && (
          <div className="mt-3">
            {!showAmountInput ? (
              <button
                onClick={() => setShowAmountInput(true)}
                className="flex items-center gap-1.5 text-xs font-bold text-amber-600 dark:text-amber-400 hover:opacity-75 transition-opacity"
              >
                <Clock size={12} /> Enter this month&apos;s amount
              </button>
            ) : (
              <div className="flex items-center gap-2">
                <div className="relative flex-1">
                  <span className="absolute left-2.5 top-1/2 -translate-y-1/2 text-xs text-muted-foreground">₹</span>
                  <input
                    type="number" placeholder="0" min="0" autoFocus
                    className="w-full h-8 pl-6 pr-3 rounded-lg border border-border bg-background text-sm font-medium outline-none focus:border-primary/60"
                    value={amountInput}
                    onChange={e => setAmountInput(e.target.value)}
                  />
                </div>
                <button
                  onClick={() => {
                    const amt = parseFloat(amountInput)
                    if (amt > 0) { onConfirmAmount(instance.id, amt); setShowAmountInput(false) }
                  }}
                  className="h-8 px-3 rounded-lg bg-primary text-primary-foreground text-xs font-bold hover:opacity-90 transition-opacity"
                >
                  Confirm
                </button>
                <button onClick={() => setShowAmountInput(false)} className="text-muted-foreground hover:text-foreground">
                  <X size={14} />
                </button>
              </div>
            )}
          </div>
        )}

        {/* Skipped reason */}
        {instance.status === 'skipped' && instance.skippedReason && (
          <p className="text-xs text-muted-foreground mt-1 italic">{instance.skippedReason}</p>
        )}
      </div>
    </div>
  )
}

// ── Monthly Bill Row ──────────────────────────────────────────────────────────

function MonthlyBillRow({
  bill, members, currentUserId, isAdmin, currentInstance,
  onEdit, onDelete, onGenerate, onConfirmAmount, onMarkPaid, onSkip,
}: {
  bill: RecurringBill; members: { uid: string; nickname: string }[]
  currentUserId: string; isAdmin: boolean
  currentInstance: BillInstance | null
  onEdit: () => void; onDelete: () => void; onGenerate: () => void
  onConfirmAmount: (instanceId: string, amount: number) => void
  onMarkPaid: (instanceId: string) => void
  onSkip: (instanceId: string) => void
}) {
  const cfg = CATEGORY_CONFIG[bill.category]
  const currentPayerUid = bill.rotationQueue[bill.currentPayerIndex % bill.rotationQueue.length]
  const currentPayer = members.find(m => m.uid === currentPayerUid)
  const due = isBillDue(bill)
  const perPerson = !bill.isVariable && bill.amount && bill.rotationQueue.length > 0
    ? bill.amount / bill.rotationQueue.length
    : null

  return (
    <div className={`p-4 rounded-xl border bg-card transition-all ${due ? 'border-amber-300 dark:border-amber-700' : 'border-border'}`}>
      <div className="flex items-start gap-3">
        <div className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 text-xl ${due ? 'bg-amber-100 dark:bg-amber-900/30' : 'bg-secondary'}`}>
          {cfg?.emoji}
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <p className="font-bold text-sm">{bill.name}</p>
            {due && (
              <span className="text-[10px] font-extrabold bg-amber-100 dark:bg-amber-900/40 text-amber-700 dark:text-amber-300 px-2 py-0.5 rounded-full">DUE</span>
            )}
            {!bill.active && (
              <span className="text-[10px] font-extrabold bg-secondary text-muted-foreground px-2 py-0.5 rounded-full">PAUSED</span>
            )}
          </div>

          <p className="text-xs text-muted-foreground mt-0.5">
            {bill.isVariable ? 'Variable amount' : formatAmount(bill.amount!, bill.currency)}
            {' · '}{ordinal(bill.billingDay)} of each month
            {' · '}{bill.rotationQueue.length} members
          </p>

          {perPerson !== null && (
            <p className="text-xs font-semibold text-foreground mt-0.5">
              Each person pays: {formatAmount(perPerson, bill.currency)}/month
            </p>
          )}
          {bill.isVariable && (
            <p className="text-xs font-semibold text-muted-foreground mt-0.5">Amount entered each month</p>
          )}

          <p className="text-xs text-muted-foreground mt-1.5">
            Paying this month:{' '}
            <span className="font-semibold text-foreground">
              {currentPayerUid === currentUserId ? `${currentPayer?.nickname} (you)` : currentPayer?.nickname ?? '…'}
            </span>
          </p>
        </div>
      </div>

      {/* This month's generated instance */}
      {currentInstance && (
        <div className="mt-3 pt-3 border-t border-border/40">
          <p className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground mb-2 flex items-center gap-1.5">
            <CircleDollarSign size={11} /> {monthLabel(currentMonthKey())}
          </p>
          <BillInstanceCard
            instance={currentInstance}
            members={members}
            currentUserId={currentUserId}
            isAdmin={isAdmin}
            onConfirmAmount={onConfirmAmount}
            onMarkPaid={onMarkPaid}
            onSkip={onSkip}
          />
        </div>
      )}

      {isAdmin && (
        <div className="flex gap-2 mt-3 pt-3 border-t border-border/50">
          {due && bill.active && (
            <Button size="sm" className="bg-amber-500 hover:bg-amber-600 text-white font-bold" onClick={onGenerate}>
              <Play size={12} className="mr-1" /> Generate
            </Button>
          )}
          <Button size="sm" variant="outline" className="font-semibold" onClick={onEdit}>
            <Pencil size={12} className="mr-1" /> Edit
          </Button>
          <Button size="sm" variant="outline" className="font-semibold"
            onClick={() => useFlatStore.getState().updateRecurringBill(bill.id, { active: !bill.active })}>
            {bill.active ? <><PauseCircle size={12} className="mr-1" /> Pause</> : <><Play size={12} className="mr-1" /> Resume</>}
          </Button>
          <button onClick={onDelete} className="ml-auto text-xs text-red-500 hover:text-red-600 font-semibold flex items-center gap-1">
            <Trash2 size={12} /> Delete
          </button>
        </div>
      )}
    </div>
  )
}

// ── Main Page ─────────────────────────────────────────────────────────────────

export default function ExpensesPage() {
  const {
    expenses, settlements, recurringBills, billInstances, members,
    addExpense, deleteExpense, addSettlement,
    createRecurringBill, updateRecurringBill, deleteRecurringBill,
    generateBill, confirmBillAmount, markBillPaid, skipBillInstance,
  } = useFlatStore()
  const { user } = useAuthStore()

  const [tab, setTab] = useState<'expenses' | 'recurring'>('expenses')
  const [showAddExpense, setShowAddExpense] = useState(false)
  const [showAddBill, setShowAddBill] = useState(false)
  const [editBill, setEditBill] = useState<RecurringBill | null>(null)
  const [settleTarget, setSettleTarget] = useState<{ userId: string; amount: number; currency: Currency } | null>(null)
  const [showGenerate, setShowGenerate] = useState(false)
  const hasAutoOpened = useRef(false)

  const currentUserId = user?.uid || 'u1'
  const currentUser = members.find(m => m.uid === currentUserId)
  const isAdmin = currentUser?.role === 'admin'

  const balances = useMemo(
    () => computeBalances(currentUserId, expenses, settlements, billInstances),
    [currentUserId, expenses, settlements, billInstances],
  )

  const sortedExpenses = useMemo(() => [...expenses].sort((a, b) => b.date.localeCompare(a.date)), [expenses])

  const grouped = useMemo(() => {
    const groups: Record<string, Expense[]> = {}
    for (const exp of sortedExpenses) {
      const key = exp.date.substring(0, 7)
      if (!groups[key]) groups[key] = []
      groups[key].push(exp)
    }
    return Object.entries(groups).sort((a, b) => b[0].localeCompare(a[0]))
  }, [sortedExpenses])

  const dueBills = useMemo(() => recurringBills.filter(isBillDue), [recurringBills])

  // Auto-open generate modal for admins when bills are due
  useEffect(() => {
    if (isAdmin && dueBills.length > 0 && !hasAutoOpened.current) {
      hasAutoOpened.current = true
      setShowGenerate(true)
    }
  }, [isAdmin, dueBills.length]) // eslint-disable-line react-hooks/exhaustive-deps

  // 2-day month-end warning for variable bills not yet confirmed
  const isNearMonthEnd = useMemo(() => {
    const today = new Date()
    const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0).getDate()
    return (lastDay - today.getDate()) < 2
  }, [])

  const pendingVariableBills = useMemo(() =>
    recurringBills.filter(b => b.active && b.isVariable && b.lastGeneratedMonth !== currentMonthKey()),
  [recurringBills])

  const thisMonthTotal = useMemo(() => {
    const key = currentMonthKey()
    return (grouped.find(([k]) => k === key)?.[1] ?? [])
      .reduce((sum, e) => sum + (e.currency === 'INR' ? e.amount : 0), 0)
  }, [grouped])

  const thisMonthMemberShares = useMemo(() => {
    const key = currentMonthKey()
    const monthExpenses = grouped.find(([k]) => k === key)?.[1] ?? []
    const shares: Record<string, number> = {}
    for (const exp of monthExpenses) {
      if (exp.currency !== 'INR') continue
      for (const uid of exp.splitAmong) {
        shares[uid] = (shares[uid] ?? 0) + (exp.splits[uid] ?? 0)
      }
    }
    return shares
  }, [grouped])

  const thisMonthExpenseCount = useMemo(() => {
    const key = currentMonthKey()
    return grouped.find(([k]) => k === key)?.[1]?.length ?? 0
  }, [grouped])

  // Category spend summary for current month
  const categorySpend = useMemo(() => {
    const key = currentMonthKey()
    const monthExpenses = grouped.find(([k]) => k === key)?.[1] ?? []
    const spend: Partial<Record<ExpenseCategory, number>> = {}
    for (const exp of monthExpenses) {
      if (exp.currency !== 'INR') continue
      spend[exp.category] = (spend[exp.category] ?? 0) + exp.amount
    }
    return (Object.entries(spend) as [ExpenseCategory, number][])
      .filter(([, amt]) => amt > 0)
      .sort(([, a], [, b]) => b - a)
  }, [grouped])

  const fixedMonthlyTotal = useMemo(() =>
    recurringBills.filter(b => b.active && !b.isVariable && b.currency === 'INR')
      .reduce((sum, b) => sum + (b.amount ?? 0), 0),
  [recurringBills])

  const variableBillNames = useMemo(() =>
    recurringBills.filter(b => b.active && b.isVariable).map(b => b.name),
  [recurringBills])

  const typicalMemberCount = useMemo(() => {
    const active = recurringBills.filter(b => b.active)
    if (active.length === 0) return members.length
    return Math.round(active.reduce((s, b) => s + b.rotationQueue.length, 0) / active.length)
  }, [recurringBills, members])

  return (
    <div className="space-y-6 max-w-3xl">

      {/* ── Header ────────────────────────────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight">Bills & Expenses</h1>
          <p className="text-muted-foreground mt-1 text-sm">Monthly bills + shared expenses, all in one place.</p>
        </div>
        <div className="flex gap-2 shrink-0">
          {thisMonthTotal > 0 && tab === 'expenses' && (
            <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800">
              <Receipt size={13} className="text-amber-600 dark:text-amber-400" />
              <span className="text-xs font-bold text-amber-700 dark:text-amber-300">{formatAmount(thisMonthTotal, 'INR')} this month</span>
            </div>
          )}
          {tab === 'expenses'
            ? <Button onClick={() => setShowAddExpense(true)} className="font-bold"><Plus size={15} className="mr-1.5" /> Add Expense</Button>
            : isAdmin && <Button onClick={() => setShowAddBill(true)} className="font-bold"><Plus size={15} className="mr-1.5" /> New Bill</Button>
          }
        </div>
      </div>

      {/* ── Tabs ──────────────────────────────────────────────────────────── */}
      <div className="flex gap-1 p-1 bg-secondary/50 rounded-xl w-fit">
        {([
          ['expenses', 'Shared Expenses', Receipt],
          ['recurring', 'Monthly Bills', RefreshCw],
        ] as const).map(([key, label, Icon]) => (
          <button key={key} onClick={() => setTab(key)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-bold transition-all ${
              tab === key ? 'bg-card shadow-sm text-foreground' : 'text-muted-foreground hover:text-foreground'
            }`}>
            <Icon size={14} />
            {label}
            {key === 'recurring' && dueBills.length > 0 && (
              <span className="bg-amber-500 text-white text-[10px] font-extrabold px-1.5 py-0.5 rounded-full min-w-[18px] text-center">{dueBills.length}</span>
            )}
          </button>
        ))}
      </div>

      {/* ── Due Bills Banner ──────────────────────────────────────────────── */}
      {dueBills.length > 0 && isAdmin && tab === 'expenses' && (
        <div className="flex items-center gap-3 p-4 rounded-xl border border-amber-300 dark:border-amber-700 bg-amber-50 dark:bg-amber-950/30">
          <Zap size={18} className="text-amber-600 dark:text-amber-400 shrink-0" />
          <div className="flex-1 min-w-0">
            <p className="text-sm font-bold text-amber-800 dark:text-amber-200">
              {dueBills.length} monthly bill{dueBills.length > 1 ? 's' : ''} due for {monthLabel(currentMonthKey())}
            </p>
            <p className="text-xs text-amber-700 dark:text-amber-400 mt-0.5">
              {dueBills.map(b => b.name).join(', ')}
            </p>
          </div>
          <Button size="sm" className="bg-amber-600 hover:bg-amber-700 text-white font-bold shrink-0"
            onClick={() => setShowGenerate(true)}>
            Generate
          </Button>
        </div>
      )}

      {/* ── 2-day month-end reminder for variable bills ───────────────────── */}
      {isNearMonthEnd && pendingVariableBills.length > 0 && (
        <div className="flex items-center gap-3 p-4 rounded-xl border border-blue-300 dark:border-blue-700 bg-blue-50 dark:bg-blue-950/30">
          <AlertCircle size={18} className="text-blue-600 dark:text-blue-400 shrink-0" />
          <div className="flex-1 min-w-0">
            <p className="text-sm font-bold text-blue-800 dark:text-blue-200">Month ending soon — confirm these bills</p>
            <p className="text-xs text-blue-700 dark:text-blue-400 mt-0.5">
              {pendingVariableBills.map(b => b.name).join(', ')} — enter this month&apos;s amount before the month closes
            </p>
          </div>
          {isAdmin && (
            <Button size="sm" variant="outline" className="shrink-0 font-bold border-blue-300 text-blue-700 hover:bg-blue-50"
              onClick={() => setShowGenerate(true)}>
              Enter
            </Button>
          )}
        </div>
      )}

      {/* ── SHARED EXPENSES TAB ──────────────────────────────────────────── */}
      {tab === 'expenses' && (
        <div className="space-y-8">

          {/* ── Monthly Statement ──────────────────────────────────────────── */}
          {thisMonthTotal > 0 && (
            <section>
              <Card className="border-primary/20 bg-gradient-to-br from-primary/5 via-card to-card overflow-hidden">
                <CardContent className="p-5">
                  <div className="flex items-start justify-between gap-4 mb-4">
                    <div>
                      <p className="text-xs font-bold uppercase tracking-wider text-muted-foreground">{monthLabel(currentMonthKey())}</p>
                      <p className="text-3xl font-extrabold mt-1 tracking-tight">{formatAmount(thisMonthTotal, 'INR')}</p>
                      <p className="text-sm text-muted-foreground mt-0.5">total flat spend this month</p>
                    </div>
                    <div className="text-right shrink-0">
                      <p className="text-xs text-muted-foreground uppercase tracking-wide font-bold">Expenses</p>
                      <p className="text-2xl font-extrabold">{thisMonthExpenseCount}</p>
                    </div>
                  </div>

                  {/* Per-person share breakdown */}
                  {Object.keys(thisMonthMemberShares).length > 0 && (
                    <div>
                      <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider mb-2">Each person&apos;s share</p>
                      <div className="grid grid-cols-2 gap-1.5">
                        {Object.entries(thisMonthMemberShares)
                          .sort(([, a], [, b]) => b - a)
                          .map(([uid, share]) => {
                            const member = members.find(m => m.uid === uid)
                            const isYou = uid === currentUserId
                            return (
                              <div key={uid}
                                className={`flex justify-between items-center rounded-lg px-3 py-2 ${isYou ? 'bg-primary/10 border border-primary/20' : 'bg-secondary/60'}`}>
                                <span className={`text-xs font-semibold ${isYou ? 'text-primary' : ''}`}>
                                  {isYou ? `${member?.nickname} (you)` : member?.nickname ?? uid}
                                </span>
                                <span className={`text-xs font-extrabold ${isYou ? 'text-primary' : ''}`}>
                                  {formatAmount(share, 'INR')}
                                </span>
                              </div>
                            )
                          })}
                      </div>
                    </div>
                  )}

                  {/* Category spend summary */}
                  {categorySpend.length > 0 && (
                    <div className="mt-4">
                      <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider mb-2">By category</p>
                      <div className="space-y-1.5">
                        {categorySpend.map(([cat, amt]) => {
                          const cfg = CATEGORY_CONFIG[cat]
                          const pct = thisMonthTotal > 0 ? (amt / thisMonthTotal) * 100 : 0
                          return (
                            <div key={cat} className="flex items-center gap-2">
                              <span className="text-sm shrink-0">{cfg?.emoji}</span>
                              <div className="flex-1 min-w-0">
                                <div className="flex justify-between items-center mb-0.5">
                                  <span className="text-xs font-semibold">{cfg?.label}</span>
                                  <span className="text-xs font-bold">{formatAmount(amt, 'INR')}</span>
                                </div>
                                <div className="h-1 bg-secondary rounded-full overflow-hidden">
                                  <div className="h-full bg-primary/60 rounded-full" style={{ width: `${pct}%` }} />
                                </div>
                              </div>
                            </div>
                          )
                        })}
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>
            </section>
          )}

          {/* ── Your Balance ─────────────────────────────────────────────── */}
          <section>
            <div className="flex items-center gap-2 mb-3">
              <Wallet size={15} className="text-muted-foreground" />
              <h2 className="text-sm font-bold uppercase tracking-wider text-muted-foreground">Your Balance</h2>
            </div>
            <BalanceSummary balances={balances} members={members}
              onSettle={(uid, amt, cur) => setSettleTarget({ userId: uid, amount: amt, currency: cur })} />
          </section>

          {/* ── History ─────────────────────────────────────────────────── */}
          <section>
            <div className="flex items-center gap-2 mb-3">
              <LayoutList size={15} className="text-muted-foreground" />
              <h2 className="text-sm font-bold uppercase tracking-wider text-muted-foreground">History</h2>
              {sortedExpenses.length > 0 && (
                <span className="text-xs font-bold text-muted-foreground bg-secondary px-2 py-0.5 rounded-full">{sortedExpenses.length}</span>
              )}
            </div>

            {sortedExpenses.length === 0 ? (
              <Card className="border-dashed border-2">
                <CardContent className="flex flex-col items-center justify-center p-16 text-center">
                  <Inbox size={44} className="text-muted-foreground/25 mb-4" />
                  <p className="font-bold text-lg text-muted-foreground">No expenses yet</p>
                  <p className="text-sm text-muted-foreground/60 mt-1 max-w-xs">
                    Set up monthly bills in the Monthly Bills tab, or add a shared expense here.
                  </p>
                </CardContent>
              </Card>
            ) : (
              <div className="space-y-6">
                {grouped.map(([monthKey, monthExpenses]) => {
                  const monthTotal = monthExpenses.reduce((s, e) => s + (e.currency === 'INR' ? e.amount : 0), 0)
                  return (
                    <div key={monthKey}>
                      <div className="flex items-center justify-between mb-2">
                        <p className="text-xs font-extrabold text-muted-foreground uppercase tracking-widest">{monthLabel(monthKey)}</p>
                        {monthTotal > 0 && <p className="text-xs font-bold text-muted-foreground">{formatAmount(monthTotal, 'INR')} total</p>}
                      </div>
                      <div className="space-y-2">
                        {monthExpenses.map(exp => (
                          <ExpenseRow key={exp.id} expense={exp} members={members} currentUserId={currentUserId}
                            canDelete={exp.createdBy === currentUserId || isAdmin} onDelete={deleteExpense} />
                        ))}
                      </div>
                    </div>
                  )
                })}
              </div>
            )}
          </section>
        </div>
      )}

      {/* ── MONTHLY BILLS TAB ────────────────────────────────────────────── */}
      {tab === 'recurring' && (
        <div className="space-y-4">
          {dueBills.length > 0 && isAdmin && (
            <div className="flex items-center gap-3 p-4 rounded-xl border border-amber-300 dark:border-amber-700 bg-amber-50 dark:bg-amber-950/30">
              <Zap size={18} className="text-amber-600 shrink-0" />
              <p className="text-sm font-bold text-amber-800 dark:text-amber-200 flex-1">
                {dueBills.length} bill{dueBills.length > 1 ? 's' : ''} ready to generate for {monthLabel(currentMonthKey())}
              </p>
              <Button size="sm" className="bg-amber-600 hover:bg-amber-700 text-white font-bold shrink-0"
                onClick={() => setShowGenerate(true)}>
                Generate
              </Button>
            </div>
          )}

          {/* Monthly summary card */}
          {recurringBills.filter(b => b.active).length > 0 && (
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
              <div className="p-4 rounded-xl border border-border bg-card">
                <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider mb-1">Fixed/month</p>
                <p className="text-xl font-extrabold">{formatAmount(fixedMonthlyTotal, 'INR')}</p>
                <p className="text-xs text-muted-foreground mt-0.5">
                  {recurringBills.filter(b => b.active && !b.isVariable).length} bills
                </p>
              </div>
              <div className="p-4 rounded-xl border border-border bg-card">
                <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider mb-1">Per person</p>
                <p className="text-xl font-extrabold">
                  {typicalMemberCount > 0 ? formatAmount(fixedMonthlyTotal / typicalMemberCount, 'INR') : '—'}
                </p>
                <p className="text-xs text-muted-foreground mt-0.5">{typicalMemberCount} members avg</p>
              </div>
              <div className="p-4 rounded-xl border border-border bg-card col-span-2 sm:col-span-1">
                <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider mb-1">Variable</p>
                {variableBillNames.length > 0 ? (
                  <p className="text-sm font-semibold leading-snug">{variableBillNames.join(', ')}</p>
                ) : (
                  <p className="text-sm text-muted-foreground">None</p>
                )}
                <p className="text-xs text-muted-foreground mt-0.5">entered on generate</p>
              </div>
            </div>
          )}

          {recurringBills.length === 0 ? (
            <Card className="border-dashed border-2">
              <CardContent className="flex flex-col items-center justify-center p-16 text-center">
                <RefreshCw size={44} className="text-muted-foreground/25 mb-4" />
                <p className="font-bold text-lg text-muted-foreground">No monthly bills set up</p>
                <p className="text-sm text-muted-foreground/60 mt-1 max-w-xs">
                  Add rent, electricity, water, and other bills that repeat every month.
                </p>
                {isAdmin && (
                  <Button className="mt-5 font-bold" onClick={() => setShowAddBill(true)}>
                    <Plus size={15} className="mr-1.5" /> Add first bill
                  </Button>
                )}
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-3">
              {recurringBills.map(bill => {
                const currentInstance = billInstances.find(
                  b => b.templateId === bill.id && b.month === currentMonthKey()
                ) ?? null
                return (
                  <MonthlyBillRow
                    key={bill.id} bill={bill} members={members}
                    currentUserId={currentUserId} isAdmin={!!isAdmin}
                    currentInstance={currentInstance}
                    onEdit={() => setEditBill(bill)}
                    onDelete={() => deleteRecurringBill(bill.id)}
                    onGenerate={() => {
                      if (bill.isVariable) setShowGenerate(true)
                      else generateBill(bill.id)
                    }}
                    onConfirmAmount={confirmBillAmount}
                    onMarkPaid={markBillPaid}
                    onSkip={(id) => skipBillInstance(id)}
                  />
                )
              })}
            </div>
          )}
        </div>
      )}

      {/* ── Modals ────────────────────────────────────────────────────────── */}
      {showAddExpense && (
        <ExpenseModal members={members} currentUserId={currentUserId} onSave={addExpense} onClose={() => setShowAddExpense(false)} />
      )}
      {showAddBill && (
        <MonthlyBillModal members={members} currentUserId={currentUserId} onSave={createRecurringBill} onClose={() => setShowAddBill(false)} />
      )}
      {editBill && (
        <MonthlyBillModal members={members} currentUserId={currentUserId} initial={editBill}
          onSave={async (data) => { await updateRecurringBill(editBill.id, data) }}
          onClose={() => setEditBill(null)} />
      )}
      {settleTarget && (
        <SettleUpModal preToUserId={settleTarget.userId} preAmount={settleTarget.amount} preCurrency={settleTarget.currency}
          members={members} currentUserId={currentUserId} onSettle={addSettlement} onClose={() => setSettleTarget(null)} />
      )}
      {showGenerate && dueBills.length > 0 && (
        <GenerateBillsModal dueBills={dueBills} members={members} onGenerate={generateBill} onClose={() => setShowGenerate(false)} />
      )}
    </div>
  )
}
