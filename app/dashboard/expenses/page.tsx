"use client"
import { useState, useMemo, useRef, useEffect } from 'react'
import { createPortal } from 'react-dom'
import {
  useFlatStore,
  type Expense, type Settlement, type RecurringBill,
  type BillInstance, type BillInstanceStatus, type MonthCycle,
  type ExpenseCategory, type Currency, type PayerMode, type SplitMethod,
} from '@/store/useFlatStore'
import {
  computeMonthNetBalances, suggestSettlements, computeCarryForward,
  buildMonthSummary, prevMonthKey, nextMonthKey, monthLabel as monthLabelUtil,
  type SuggestedSettlement,
} from '@/lib/settlementUtils'
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
  CalendarCheck, ArrowRight, ChevronRight, ChevronLeft, RotateCcw,
  UserCircle, Repeat2, HelpCircle, Sparkles, History, ChevronDown, ChevronUp,
} from 'lucide-react'

// ── Config ───────────────────────────────────────────────────────────────────

export const CATEGORY_CONFIG: Record<ExpenseCategory, { label: string; emoji: string }> = {
  rent:        { label: 'Rent',               emoji: '🏠' },
  electricity: { label: 'Electricity',        emoji: '⚡' },
  water:       { label: 'Water',              emoji: '💧' },
  internet:    { label: 'Internet',           emoji: '📶' },
  gas:         { label: 'Gas / LPG',          emoji: '🔥' },
  maid:        { label: 'Maid / Housekeeping', emoji: '🧹' },
  cook:        { label: 'Cook',               emoji: '👨‍🍳' },
  gym:         { label: 'Gym',                emoji: '🏋️' },
  grocery:     { label: 'Groceries',          emoji: '🛒' },
  milk:        { label: 'Milk',               emoji: '🥛' },
  ac:          { label: 'AC / Cooling',       emoji: '❄️' },
  maintenance: { label: 'Maintenance',        emoji: '🔧' },
  food:        { label: 'Food',               emoji: '🍽️' },
  household:   { label: 'Household',          emoji: '📦' },
  other:       { label: 'Other',              emoji: '💰' },
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

function daysInCurrentMonth(): number {
  const d = new Date()
  return new Date(d.getFullYear(), d.getMonth() + 1, 0).getDate()
}

function isBillDue(bill: RecurringBill): boolean {
  if (!bill.active) return false
  const today = new Date()
  const effectiveDay = Math.min(bill.billingDay, daysInCurrentMonth())
  return bill.lastGeneratedMonth !== currentMonthKey() && today.getDate() >= effectiveDay
}

function ordinal(n: number): string {
  const s = ['th', 'st', 'nd', 'rd']
  const v = n % 100
  return n + (s[(v - 20) % 10] || s[v] || s[0])
}

// ── Modal shell — React Portal so it renders above all overflow:hidden parents ──

function Modal({ title, onClose, children }: { title: string; onClose: () => void; children: React.ReactNode }) {
  const [mounted, setMounted] = useState(false)
  useEffect(() => { setMounted(true) }, [])
  if (!mounted) return null

  return createPortal(
    <div
      className="fixed inset-0 bg-black/60 z-[200] flex items-end sm:items-center justify-center p-0 sm:p-4"
      onClick={(e) => { if (e.target === e.currentTarget) onClose() }}
    >
      <div
        className="bg-card border border-border rounded-t-2xl sm:rounded-2xl w-full sm:max-w-lg max-h-[92vh] overflow-y-auto shadow-2xl"
        onClick={e => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-5 py-4 border-b border-border sticky top-0 bg-card z-10">
          <h2 className="font-bold text-base">{title}</h2>
          <button onClick={onClose} className="p-1.5 rounded-lg hover:bg-secondary text-muted-foreground transition-colors">
            <X size={17} />
          </button>
        </div>
        <div className="p-5">{children}</div>
      </div>
    </div>,
    document.body,
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
  const [showAdvanced, setShowAdvanced] = useState(false)
  const [form, setForm] = useState({
    category:     (initial?.category ?? 'other') as ExpenseCategory,
    description:  initial?.description ?? '',
    amount:       initial?.amount?.toString() ?? '',
    currency:     (initial?.currency ?? 'INR') as Currency,
    date:         initial?.date ?? todayStr(),
    paidBy:       initial?.paidBy ?? currentUserId,
    splitAmong:   initial?.splitAmong ?? members.map(m => m.uid),
    splitType:    (initial?.splitType ?? 'equal') as 'equal' | 'percent' | 'custom',
    customSplits: Object.fromEntries(members.map(m => [m.uid, initial?.splits?.[m.uid]?.toString() ?? ''])),
    percentSplits:Object.fromEntries(members.map(m => [m.uid, ''])),
    note:         initial?.note ?? '',
    deferToNextMonth: initial?.deferToNextMonth ?? false,
  })

  const totalAmount   = parseFloat(form.amount) || 0
  const selectedCount = form.splitAmong.length
  const equalShare    = selectedCount > 0 ? totalAmount / selectedCount : 0
  const customTotal   = form.splitAmong.reduce((s, uid) => s + (parseFloat(form.customSplits[uid]) || 0), 0)
  const customOk      = Math.abs(customTotal - totalAmount) <= 0.5
  const totalPercent  = form.splitAmong.reduce((s, uid) => s + (parseFloat(form.percentSplits[uid]) || 0), 0)
  const percentOk     = Math.abs(totalPercent - 100) <= 1

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
      setError('Custom splits must add up to the total.'); return
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

  const QUICK_CATS: ExpenseCategory[] = ['food', 'grocery', 'household', 'gas', 'maid', 'other']
  const avatarColors = ['bg-indigo-600', 'bg-violet-500', 'bg-slate-500', 'bg-teal-600', 'bg-orange-500', 'bg-rose-500']

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-[2px]" onClick={onClose} />
      <div className="relative bg-white dark:bg-[#1A202C] rounded-t-[24px] sm:rounded-[24px] w-full sm:max-w-[480px] shadow-[0px_10px_40px_-10px_rgba(0,0,0,0.20)] flex flex-col max-h-[92dvh]">

        {/* Header */}
        <div className="flex items-center justify-between px-6 pt-6 pb-5 border-b border-black/[0.08] shrink-0">
          <h2 className="text-[20px] font-semibold text-[#141b2b] dark:text-white leading-7">
            {initial ? 'Edit Split' : 'Add a Split'}
          </h2>
          <button onClick={onClose} className="w-8 h-8 rounded-full flex items-center justify-center hover:bg-[#f1f3ff] dark:hover:bg-white/10 transition-colors cursor-pointer">
            <X size={14} className="text-[#777587]" />
          </button>
        </div>

        {/* Scrollable body */}
        <div className="overflow-y-auto flex-1 px-6 py-6 space-y-6">

          {/* Quick categories */}
          <div className="space-y-2.5">
            <label className="block text-[11px] font-semibold text-[#464555] dark:text-gray-400 uppercase tracking-[0.6px]">Category</label>
            <div className="flex gap-2 flex-wrap">
              {QUICK_CATS.map(cat => {
                const cfg = CATEGORY_CONFIG[cat]
                const sel = form.category === cat
                return (
                  <button
                    key={cat}
                    onClick={() => setForm(f => ({ ...f, category: cat, description: f.description || cfg.label }))}
                    className={[
                      'flex items-center gap-1.5 px-3 py-2 rounded-full text-[13px] font-medium transition-all cursor-pointer',
                      sel ? 'bg-[#3525cd] text-white' : 'bg-[#e9edff] text-[#3525cd] hover:bg-[#dde2ff]',
                    ].join(' ')}
                  >
                    <span className="text-[15px] leading-none">{cfg.emoji}</span>
                    {cfg.label}
                  </button>
                )
              })}
            </div>
          </div>

          {/* Description */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-[#464555] dark:text-gray-400">What was it for?</label>
            <input
              value={form.description}
              onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
              placeholder="e.g. Dinner, Groceries, Cab…"
              maxLength={80}
              className="w-full bg-[#f1f3ff] dark:bg-white/[0.08] rounded-lg px-4 py-[13px] text-base text-[#141b2b] dark:text-white placeholder-[#777587] border-0 outline-none focus:ring-2 focus:ring-[#3525cd]/25"
            />
          </div>

          {/* Amount + Currency */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-[#464555] dark:text-gray-400">Total Amount</label>
            <div className="flex gap-2">
              <div className="relative flex-1">
                <span className="absolute left-4 top-1/2 -translate-y-1/2 text-[#777587] text-sm font-medium pointer-events-none">
                  {CURRENCY_SYMBOLS[form.currency]}
                </span>
                <input
                  type="number" min="0" placeholder="0"
                  value={form.amount}
                  onChange={e => setForm(f => ({ ...f, amount: e.target.value }))}
                  className="w-full bg-[#f1f3ff] dark:bg-white/[0.08] rounded-lg pl-8 pr-4 py-[13px] text-base text-[#141b2b] dark:text-white placeholder-[#777587] border-0 outline-none focus:ring-2 focus:ring-[#3525cd]/25"
                />
              </div>
              <select
                value={form.currency}
                onChange={e => setForm(f => ({ ...f, currency: e.target.value as Currency }))}
                className="bg-[#f1f3ff] dark:bg-white/[0.08] rounded-lg px-3 py-[13px] text-sm font-semibold text-[#141b2b] dark:text-white border-0 outline-none focus:ring-2 focus:ring-[#3525cd]/25 cursor-pointer"
              >
                {CURRENCIES.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>
          </div>

          {/* Paid by + Date */}
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-[#464555] dark:text-gray-400">Paid by</label>
              <select
                value={form.paidBy}
                onChange={e => setForm(f => ({ ...f, paidBy: e.target.value }))}
                className="w-full bg-[#f1f3ff] dark:bg-white/[0.08] rounded-lg px-4 py-[13px] text-sm text-[#141b2b] dark:text-white border-0 outline-none focus:ring-2 focus:ring-[#3525cd]/25 cursor-pointer"
              >
                {members.map(m => (
                  <option key={m.uid} value={m.uid}>
                    {m.uid === currentUserId ? m.nickname + ' (you)' : m.nickname}
                  </option>
                ))}
              </select>
            </div>
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-[#464555] dark:text-gray-400">Date</label>
              <input
                type="date"
                value={form.date}
                onChange={e => setForm(f => ({ ...f, date: e.target.value }))}
                className="w-full bg-[#f1f3ff] dark:bg-white/[0.08] rounded-lg px-4 py-[13px] text-sm text-[#141b2b] dark:text-white border-0 outline-none focus:ring-2 focus:ring-[#3525cd]/25 cursor-pointer"
              />
            </div>
          </div>

          {/* Split among */}
          <div className="space-y-2.5">
            <div className="flex items-center justify-between">
              <label className="text-xs font-semibold text-[#464555] dark:text-gray-400">Split among</label>
              <button
                onClick={() => setForm(f => ({
                  ...f,
                  splitAmong: f.splitAmong.length === members.length ? [] : members.map(m => m.uid),
                }))}
                className="text-xs font-semibold text-[#3525cd] cursor-pointer hover:opacity-70 transition-opacity"
              >
                {form.splitAmong.length === members.length ? 'Deselect All' : 'Select All'}
              </button>
            </div>
            <div className="bg-[#f1f3ff] dark:bg-white/[0.05] rounded-[12px] p-4 space-y-3">
              {members.map((m, idx) => {
                const checked = form.splitAmong.includes(m.uid)
                return (
                  <button
                    key={m.uid}
                    onClick={() => toggleMember(m.uid)}
                    className="flex items-center gap-3 w-full cursor-pointer"
                  >
                    <div className={[
                      'w-6 h-6 rounded-[4px] border-2 flex items-center justify-center shrink-0 transition-all',
                      checked ? 'bg-[#3525cd] border-[#3525cd]' : 'bg-white dark:bg-white/10 border-gray-300 dark:border-white/20',
                    ].join(' ')}>
                      {checked && (
                        <svg width="11" height="8" viewBox="0 0 11 8" fill="none">
                          <path d="M1 4L4 7L10 1" stroke="white" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                      )}
                    </div>
                    <div className={['w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold text-white shrink-0', avatarColors[idx % avatarColors.length]].join(' ')}>
                      {m.nickname.charAt(0).toUpperCase()}
                    </div>
                    <div className="flex-1 flex items-center justify-between min-w-0">
                      <span className="text-[15px] text-[#141b2b] dark:text-white">
                        {m.uid === currentUserId ? m.nickname + ' (you)' : m.nickname}
                      </span>
                      {checked && totalAmount > 0 && form.splitType === 'equal' && (
                        <span className="text-[13px] font-bold text-[#3525cd]">
                          {CURRENCY_SYMBOLS[form.currency]}{equalShare % 1 === 0 ? equalShare.toFixed(0) : equalShare.toFixed(2)}
                        </span>
                      )}
                    </div>
                  </button>
                )
              })}
            </div>
            {selectedCount > 0 && totalAmount > 0 && (
              <p className="text-[12px] text-[#777587] text-center">
                Equal split &mdash; each pays <span className="font-bold text-[#3525cd]">{CURRENCY_SYMBOLS[form.currency]}{equalShare % 1 === 0 ? equalShare.toFixed(0) : equalShare.toFixed(2)}</span>
              </p>
            )}
          </div>

          {/* Advanced options toggle */}
          <button
            onClick={() => setShowAdvanced(a => !a)}
            className="flex items-center gap-2 text-[13px] font-semibold text-[#777587] hover:text-[#464555] transition-colors cursor-pointer w-full"
          >
            <div className={['w-4 h-4 rounded border border-current flex items-center justify-center transition-transform', showAdvanced ? 'rotate-45' : ''].join(' ')}>
              <Plus size={10} />
            </div>
            {showAdvanced ? 'Hide advanced options' : 'Advanced: custom splits, note, defer'}
          </button>

          {showAdvanced && (
            <div className="space-y-5 border-t border-black/[0.06] pt-5">

              {/* Split type */}
              {selectedCount > 0 && totalAmount > 0 && (
                <div className="space-y-3">
                  <label className="block text-xs font-semibold text-[#464555] dark:text-gray-400">Split method</label>
                  <div className="flex gap-2">
                    {(['equal', 'percent', 'custom'] as const).map(t => (
                      <button
                        key={t}
                        onClick={() => setForm(f => ({ ...f, splitType: t }))}
                        className={[
                          'flex-1 py-2 rounded-lg text-xs font-bold transition-all cursor-pointer',
                          form.splitType === t ? 'bg-[#3525cd] text-white' : 'bg-[#e9edff] text-[#3525cd] hover:bg-[#dde2ff]',
                        ].join(' ')}
                      >
                        {t === 'equal' ? 'Equal' : t === 'percent' ? 'By %' : 'Custom'}
                      </button>
                    ))}
                  </div>

                  {form.splitType === 'percent' && (
                    <div className="space-y-2">
                      {form.splitAmong.map(uid => {
                        const m = members.find(x => x.uid === uid)
                        const pct = parseFloat(form.percentSplits[uid]) || 0
                        const share = (pct / 100) * totalAmount
                        return (
                          <div key={uid} className="flex items-center gap-2">
                            <span className="text-sm font-medium text-[#141b2b] dark:text-white w-24 truncate shrink-0">
                              {uid === currentUserId ? m?.nickname + ' (you)' : m?.nickname ?? uid}
                            </span>
                            <div className="relative flex-1">
                              <input
                                type="number" placeholder="0" min="0" max="100"
                                value={form.percentSplits[uid]}
                                onChange={e => setForm(f => ({ ...f, percentSplits: { ...f.percentSplits, [uid]: e.target.value } }))}
                                className="w-full bg-[#f1f3ff] dark:bg-white/[0.08] rounded-lg px-4 pr-7 py-2 text-sm text-[#141b2b] dark:text-white border-0 outline-none focus:ring-2 focus:ring-[#3525cd]/25"
                              />
                              <span className="absolute right-2.5 top-1/2 -translate-y-1/2 text-xs text-[#777587]">%</span>
                            </div>
                            {pct > 0 && (
                              <span className="text-xs font-bold text-[#3525cd] w-16 text-right shrink-0">
                                {CURRENCY_SYMBOLS[form.currency]}{share.toFixed(share % 1 === 0 ? 0 : 2)}
                              </span>
                            )}
                          </div>
                        )
                      })}
                      <div className={['text-xs px-3 py-2 rounded-lg font-medium', percentOk ? 'bg-emerald-50 text-emerald-700' : 'bg-orange-50 text-orange-700'].join(' ')}>
                        {percentOk ? 'Percentages add up to 100%' : 'Total: ' + totalPercent.toFixed(0) + '% of 100%'}
                      </div>
                    </div>
                  )}

                  {form.splitType === 'custom' && (
                    <div className="space-y-2">
                      {form.splitAmong.map(uid => {
                        const m = members.find(x => x.uid === uid)
                        return (
                          <div key={uid} className="flex items-center gap-2">
                            <span className="text-sm font-medium text-[#141b2b] dark:text-white w-24 truncate shrink-0">
                              {uid === currentUserId ? m?.nickname + ' (you)' : m?.nickname ?? uid}
                            </span>
                            <div className="relative flex-1">
                              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-xs text-[#777587]">{CURRENCY_SYMBOLS[form.currency]}</span>
                              <input
                                type="number" placeholder="0" min="0"
                                value={form.customSplits[uid]}
                                onChange={e => setForm(f => ({ ...f, customSplits: { ...f.customSplits, [uid]: e.target.value } }))}
                                className="w-full bg-[#f1f3ff] dark:bg-white/[0.08] rounded-lg pl-7 pr-4 py-2 text-sm text-[#141b2b] dark:text-white border-0 outline-none focus:ring-2 focus:ring-[#3525cd]/25"
                              />
                            </div>
                          </div>
                        )
                      })}
                      <div className={['text-xs px-3 py-2 rounded-lg font-medium', customOk ? 'bg-emerald-50 text-emerald-700' : 'bg-orange-50 text-orange-700'].join(' ')}>
                        {customOk ? 'Splits add up correctly' : 'Total: ' + CURRENCY_SYMBOLS[form.currency] + customTotal.toFixed(2) + ' of ' + CURRENCY_SYMBOLS[form.currency] + totalAmount.toFixed(2)}
                      </div>
                    </div>
                  )}
                </div>
              )}

              {/* Note */}
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-[#464555] dark:text-gray-400">Note (optional)</label>
                <input
                  placeholder="Any details…"
                  value={form.note}
                  onChange={e => setForm(f => ({ ...f, note: e.target.value }))}
                  maxLength={200}
                  className="w-full bg-[#f1f3ff] dark:bg-white/[0.08] rounded-lg px-4 py-[13px] text-sm text-[#141b2b] dark:text-white placeholder-[#777587] border-0 outline-none focus:ring-2 focus:ring-[#3525cd]/25"
                />
              </div>

              {/* Defer to next month */}
              {form.paidBy === currentUserId && (
                <div className="bg-[#e9edff] dark:bg-[#3525cd]/15 rounded-[12px] p-4 flex gap-4">
                  <div className="shrink-0 pt-0.5">
                    <button
                      onClick={() => setForm(f => ({ ...f, deferToNextMonth: !f.deferToNextMonth }))}
                      className={['relative w-11 h-6 rounded-full transition-colors cursor-pointer', form.deferToNextMonth ? 'bg-[#3525cd]' : 'bg-gray-300 dark:bg-white/20'].join(' ')}
                    >
                      <span className={['absolute top-[2px] w-5 h-5 bg-white rounded-full shadow transition-transform', form.deferToNextMonth ? 'translate-x-[22px]' : 'translate-x-[2px]'].join(' ')} />
                    </button>
                  </div>
                  <div>
                    <p className="text-[15px] font-semibold text-[#141b2b] dark:text-white leading-6">Add to next month&apos;s bill</p>
                    <p className="text-sm text-[#464555] dark:text-gray-400 mt-1 leading-[22px]">
                      {form.deferToNextMonth ? "Collected with next month’s bills" : 'Collect from flatmates right away'}
                    </p>
                  </div>
                </div>
              )}
            </div>
          )}

          {error && (
            <div className="flex items-center gap-2 p-3 rounded-xl bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-900">
              <AlertCircle size={14} className="text-red-600 shrink-0" />
              <p className="text-xs font-semibold text-red-700 dark:text-red-300">{error}</p>
            </div>
          )}

        </div>

        {/* Footer */}
        <div className="px-6 pb-6 pt-4 border-t border-black/[0.08] shrink-0">
          <button
            onClick={handleSave}
            disabled={saving || !form.description.trim() || totalAmount <= 0 || form.splitAmong.length === 0}
            className="w-full flex items-center justify-center gap-2 bg-[#3525cd] hover:bg-[#2b1eb5] disabled:opacity-40 disabled:cursor-not-allowed text-white font-semibold py-3.5 rounded-full shadow-md transition-all cursor-pointer"
          >
            {saving
              ? <><RefreshCw size={14} className="animate-spin" /> Saving...</>
              : <><Check size={14} /> {initial ? 'Save Changes' : 'Add Split'}</>}
          </button>
        </div>

      </div>
    </div>,
    document.body,
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
  expense: Expense
  members: { uid: string; nickname: string }[]
  currentUserId: string
  canDelete: boolean
  onDelete: (id: string) => void
}) {
  const [expanded, setExpanded] = useState(false)
  const cfg         = CATEGORY_CONFIG[expense.category]
  const payer       = members.find(m => m.uid === expense.paidBy)
  const isYouPayer  = expense.paidBy === currentUserId
  const myShare     = expense.splits[currentUserId] ?? 0
  const notInSplit  = !expense.splitAmong.includes(currentUserId)
  const getBack     = isYouPayer ? expense.amount - myShare : 0

  return (
    <div className="rounded-[18px] bg-card border border-border/50 shadow-[0px_2px_8px_rgba(0,0,0,0.05)] overflow-hidden">
      <button
        onClick={() => setExpanded(e => !e)}
        className="w-full p-4 text-left hover:bg-secondary/20 transition-colors cursor-pointer"
      >
        <div className="flex items-center gap-3">
          {/* Category bubble */}
          <div className="w-11 h-11 rounded-[14px] bg-[#f1f3ff] dark:bg-white/[0.08] flex items-center justify-center text-xl shrink-0">
            {cfg?.emoji ?? '💰'}
          </div>

          {/* Main content */}
          <div className="flex-1 min-w-0">
            <div className="flex items-start justify-between gap-2">
              <p className="text-[#141b2b] dark:text-foreground text-[14px] font-semibold leading-tight truncate">
                {expense.description}
                {expense.deferToNextMonth && (
                  <span className="ml-1.5 text-[10px] font-extrabold bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-300 px-1.5 py-0.5 rounded-full align-middle">DEFERRED</span>
                )}
              </p>
              <p className="text-[#141b2b] dark:text-foreground text-[15px] font-bold shrink-0 leading-tight">
                {formatAmount(expense.amount, expense.currency)}
              </p>
            </div>

            <div className="flex items-center justify-between mt-1.5 gap-2">
              <div className="flex items-center gap-1.5 min-w-0">
                <span className="text-[11px] font-semibold text-[#777587] dark:text-muted-foreground truncate">
                  {isYouPayer ? 'You paid' : (payer?.nickname ?? '…') + ' paid'}
                </span>
                <span className="text-[#d0d2d8] shrink-0">&middot;</span>
                <span className="text-[11px] text-[#999CA1] dark:text-muted-foreground shrink-0">
                  {new Date(expense.date + 'T00:00:00').toLocaleDateString('en-IN', { day: 'numeric', month: 'short' })}
                </span>
              </div>

              {/* Your share chip */}
              {!notInSplit && (
                <span className={[
                  'text-[11px] font-bold px-2.5 py-0.5 rounded-full shrink-0',
                  isYouPayer && getBack > 0
                    ? 'bg-emerald-50 dark:bg-emerald-900/20 text-emerald-600 dark:text-emerald-400'
                    : !isYouPayer
                    ? 'bg-[rgba(235,152,106,0.14)] text-[#c85a2a] dark:text-orange-300'
                    : 'bg-secondary text-muted-foreground',
                ].join(' ')}>
                  {isYouPayer && getBack > 0
                    ? '+' + formatAmount(getBack, expense.currency)
                    : !isYouPayer
                    ? '-' + formatAmount(myShare, expense.currency)
                    : 'You paid all'}
                </span>
              )}
              {notInSplit && (
                <span className="text-[11px] text-[#999CA1] dark:text-muted-foreground shrink-0 italic">not included</span>
              )}
            </div>
          </div>
        </div>
      </button>

      {/* Expanded split details */}
      {expanded && (
        <div className="border-t border-border/40 px-4 pb-4 pt-3 space-y-2">
          {expense.splitAmong.map(uid => {
            const m     = members.find(x => x.uid === uid)
            const share = expense.splits[uid] ?? 0
            const isPayer = uid === expense.paidBy
            return (
              <div key={uid} className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-6 h-6 rounded-full bg-[#e9edff] dark:bg-white/10 flex items-center justify-center text-[10px] font-bold text-[#3525cd] shrink-0">
                    {(m?.nickname ?? '?').charAt(0).toUpperCase()}
                  </div>
                  <span className="text-[12px] font-medium text-[#464555] dark:text-gray-400">
                    {uid === currentUserId ? (m?.nickname ?? uid) + ' (you)' : m?.nickname ?? uid}
                    {isPayer && <span className="ml-1 text-[10px] font-bold text-[#3525cd]">paid</span>}
                  </span>
                </div>
                <span className="text-[13px] font-bold text-[#141b2b] dark:text-foreground">
                  {formatAmount(share, expense.currency)}
                </span>
              </div>
            )
          })}
          {expense.note && (
            <p className="text-xs text-[#777587] italic border-t border-border/40 pt-2">
              &ldquo;{expense.note}&rdquo;
            </p>
          )}
          {canDelete && (
            <button
              onClick={() => onDelete(expense.id)}
              className="flex items-center gap-1.5 text-xs text-red-500 hover:text-red-600 font-semibold mt-1 cursor-pointer transition-colors"
            >
              <Trash2 size={12} /> Remove
            </button>
          )}
        </div>
      )}
    </div>
  )
}
// ── Monthly Bill Form Modal ───────────────────────────────────────────────────

const PAYER_MODE_CONFIG: Record<PayerMode, { label: string; desc: string; icon: React.ReactNode }> = {
  rotation: { label: 'Rotates',      desc: 'Members take turns paying each month', icon: <Repeat2 size={13} /> },
  fixed:    { label: 'Fixed payer',  desc: 'Same person pays every month',         icon: <UserCircle size={13} /> },
  manual:   { label: 'Choose each month', desc: 'Admin picks payer when generating', icon: <HelpCircle size={13} /> },
}

const SPLIT_METHOD_CONFIG: Record<SplitMethod, { label: string; desc: string }> = {
  equal:   { label: 'Equal',       desc: 'Split equally among participants' },
  percent: { label: 'By %',        desc: 'Each person pays a fixed percentage' },
  custom:  { label: 'Custom ₹',    desc: 'Set a fixed amount per person' },
}

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
    name:          initial?.name ?? '',
    category:      (initial?.category ?? 'other') as ExpenseCategory,
    isVariable:    initial?.isVariable ?? false,
    amount:        initial?.amount != null ? initial.amount.toString() : '',
    billingDay:    initial?.billingDay?.toString() ?? '1',
    rotationQueue: initial?.rotationQueue ?? members.map(m => m.uid),
    active:        initial?.active ?? true,
  })

  const toggleMember = (uid: string) => setForm(f => ({
    ...f,
    rotationQueue: f.rotationQueue.includes(uid)
      ? f.rotationQueue.filter(id => id !== uid)
      : [...f.rotationQueue, uid],
  }))

  const selectAll = () => setForm(f => ({
    ...f,
    rotationQueue: f.rotationQueue.length === members.length ? [] : members.map(m => m.uid),
  }))

  const handleSave = async () => {
    if (!form.name.trim()) return
    if (!form.isVariable && (!parseFloat(form.amount) || parseFloat(form.amount) <= 0)) return
    if (form.rotationQueue.length === 0) return
    setSaving(true)
    await onSave({
      name:          form.name.trim(),
      category:      form.category,
      isVariable:    form.isVariable,
      amount:        form.isVariable ? null : parseFloat(form.amount),
      currency:      (initial?.currency ?? 'INR') as Currency,
      billingDay:    Math.min(31, Math.max(1, parseInt(form.billingDay) || 1)),
      rotationQueue: form.rotationQueue,
      participants:  form.rotationQueue,
      payerMode:     (initial?.payerMode ?? 'rotation') as PayerMode,
      fixedPayerUid: initial?.fixedPayerUid,
      splitMethod:   (initial?.splitMethod ?? 'equal') as SplitMethod,
      percentSplits: initial?.percentSplits,
      customSplits:  initial?.customSplits,
      active:        form.active,
      createdBy:     currentUserId,
    })
    onClose()
  }

  const PRESET_CATEGORIES: ExpenseCategory[] = [
    'maid', 'cook', 'maintenance', 'electricity', 'water', 'internet', 'gym',
  ]

  const billingDayOptions = [1, 5, 7, 10, 15, 20, 25, 28]

  const memberAvatarColors = ['bg-indigo-600', 'bg-violet-500', 'bg-slate-500', 'bg-teal-600', 'bg-orange-500']

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-[2px]" onClick={onClose} />
      <div className="relative bg-white dark:bg-[#1A202C] rounded-t-[24px] sm:rounded-[24px] w-full sm:max-w-[440px] shadow-[0px_10px_40px_-10px_rgba(0,0,0,0.18)] flex flex-col max-h-[92dvh]">

        {/* Header */}
        <div className="flex items-center justify-between px-6 pt-6 pb-5 border-b border-black/[0.08] shrink-0">
          <h2 className="text-[20px] font-semibold text-[#141b2b] dark:text-white leading-7">
            {initial ? 'Edit Fixed Bill' : 'Add New Fixed Bill'}
          </h2>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full flex items-center justify-center hover:bg-[#f1f3ff] dark:hover:bg-white/10 transition-colors cursor-pointer"
          >
            <X size={14} className="text-[#777587] dark:text-gray-400" />
          </button>
        </div>

        {/* Scrollable body */}
        <div className="overflow-y-auto flex-1 px-6 py-6 space-y-7">

          {/* Bill Name */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-[#464555] dark:text-gray-400">Bill Name</label>
            <input
              value={form.name}
              onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
              placeholder="e.g. Internet Bill"
              maxLength={50}
              className="w-full bg-[#f1f3ff] dark:bg-white/[0.08] rounded-lg px-4 py-4 text-base text-[#141b2b] dark:text-white placeholder-[#777587] border-0 outline-none focus:ring-2 focus:ring-[#3525cd]/25"
            />
          </div>

          {/* Quick Presets */}
          <div className="space-y-2.5">
            <label className="block text-[11px] font-semibold text-[#464555] dark:text-gray-400 uppercase tracking-[0.6px]">Quick Presets</label>
            <div className="flex flex-wrap gap-2">
              {PRESET_CATEGORIES.map(cat => {
                const cfg = CATEGORY_CONFIG[cat]
                const isSelected = form.category === cat
                return (
                  <button
                    key={cat}
                    onClick={() => setForm(f => ({ ...f, category: cat, name: f.name || cfg.label }))}
                    className={[
                      'px-[14px] py-[7px] rounded-full text-sm font-medium tracking-[0.14px] transition-all cursor-pointer',
                      isSelected ? 'bg-[#3525cd] text-white' : 'bg-[#e9edff] text-[#3525cd] hover:bg-[#dde2ff]',
                    ].join(' ')}
                  >
                    {cfg.label}
                  </button>
                )
              })}
            </div>
          </div>

          {/* Fixed Amount */}
          {!form.isVariable && (
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-[#464555] dark:text-gray-400">Fixed Amount (Rs.)</label>
              <input
                type="number"
                min="0"
                value={form.amount}
                onChange={e => setForm(f => ({ ...f, amount: e.target.value }))}
                placeholder="0.00"
                className="w-full bg-[#f1f3ff] dark:bg-white/[0.08] rounded-lg px-4 py-[13px] text-base text-[#141b2b] dark:text-white placeholder-[#777587] border-0 outline-none focus:ring-2 focus:ring-[#3525cd]/25"
              />
            </div>
          )}

          {/* Billing Cycle + Due Date */}
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-[#464555] dark:text-gray-400">Billing Cycle</label>
              <div className="bg-[#f1f3ff] dark:bg-white/[0.08] rounded-lg px-4 py-[13px] text-base text-[#141b2b] dark:text-white select-none">
                Monthly
              </div>
              <p className="text-[11px] font-semibold italic text-[#777587] leading-4">
                Designed for long-term stability.
              </p>
            </div>
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-[#464555] dark:text-gray-400">Due Date</label>
              <div className="relative">
                <select
                  value={form.billingDay}
                  onChange={e => setForm(f => ({ ...f, billingDay: e.target.value }))}
                  className="w-full appearance-none bg-[#f1f3ff] dark:bg-white/[0.08] rounded-lg pl-10 pr-4 py-[13px] text-base text-[#141b2b] dark:text-white border-0 outline-none focus:ring-2 focus:ring-[#3525cd]/25 cursor-pointer"
                >
                  {billingDayOptions.map(d => (
                    <option key={d} value={d.toString()}>{ordinal(d)} of month</option>
                  ))}
                  {!billingDayOptions.includes(parseInt(form.billingDay)) && (
                    <option value={form.billingDay}>{ordinal(parseInt(form.billingDay))} of month</option>
                  )}
                </select>
                <CalendarCheck size={16} className="absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none text-[#777587]" />
              </div>
            </div>
          </div>

          {/* Assign Roommates */}
          <div className="space-y-2.5">
            <div className="flex items-center justify-between">
              <label className="text-xs font-semibold text-[#464555] dark:text-gray-400">Assign Roommates</label>
              <button
                onClick={selectAll}
                className="text-xs font-semibold text-[#3525cd] cursor-pointer hover:opacity-70 transition-opacity"
              >
                {form.rotationQueue.length === members.length ? 'Deselect All' : 'Select All'}
              </button>
            </div>
            <div className="bg-[#f1f3ff] dark:bg-white/[0.05] rounded-[12px] p-4 space-y-4">
              {members.map((m, idx) => {
                const checked = form.rotationQueue.includes(m.uid)
                return (
                  <button
                    key={m.uid}
                    onClick={() => toggleMember(m.uid)}
                    className="flex items-center gap-4 w-full cursor-pointer"
                  >
                    <div className={[
                      'w-6 h-6 rounded-[4px] border-2 flex items-center justify-center shrink-0 transition-all',
                      checked ? 'bg-[#3525cd] border-[#3525cd]' : 'bg-white dark:bg-white/10 border-gray-300 dark:border-white/20',
                    ].join(' ')}>
                      {checked && (
                        <svg width="11" height="8" viewBox="0 0 11 8" fill="none">
                          <path d="M1 4L4 7L10 1" stroke="white" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                      )}
                    </div>
                    <div className={['w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold shrink-0', memberAvatarColors[idx % memberAvatarColors.length]].join(' ')}>
                      <span className="text-white">{m.nickname.charAt(0).toUpperCase()}</span>
                    </div>
                    <span className="text-base text-[#141b2b] dark:text-white">
                      {m.uid === currentUserId ? m.nickname + ' (you)' : m.nickname}
                    </span>
                  </button>
                )
              })}
            </div>
          </div>

          {/* Confirm amount toggle */}
          <div className="bg-[#e9edff] dark:bg-[#3525cd]/15 rounded-[12px] p-4 flex gap-4">
            <div className="shrink-0 pt-0.5">
              <button
                onClick={() => setForm(f => ({ ...f, isVariable: !f.isVariable }))}
                className={[
                  'relative w-11 h-6 rounded-full transition-colors cursor-pointer',
                  form.isVariable ? 'bg-[#3525cd]' : 'bg-gray-300 dark:bg-white/20',
                ].join(' ')}
              >
                <span className={[
                  'absolute top-[2px] w-5 h-5 bg-white rounded-full shadow transition-transform',
                  form.isVariable ? 'translate-x-[22px]' : 'translate-x-[2px]',
                ].join(' ')} />
              </button>
            </div>
            <div>
              <p className="text-[15px] font-semibold text-[#141b2b] dark:text-white leading-6">
                Confirm amount before settlement
              </p>
              <p className="text-sm text-[#464555] dark:text-gray-400 mt-1 leading-[22px]">
                Habitiq will ask you to verify the amount 2 days before the due date to handle minor fluctuations (like electricity) while keeping the base cost fixed.
              </p>
            </div>
          </div>

        </div>

        {/* Footer */}
        <div className="flex items-center justify-end gap-4 px-6 pt-5 pb-6 border-t border-black/[0.08] shrink-0">
          <button
            onClick={onClose}
            className="px-6 py-3 text-sm font-medium text-[#3525cd] hover:opacity-70 transition-opacity cursor-pointer"
          >
            Cancel
          </button>
          <button
            onClick={handleSave}
            disabled={saving || !form.name.trim() || (!form.isVariable && !parseFloat(form.amount)) || form.rotationQueue.length === 0}
            className="flex items-center gap-2 bg-[#3525cd] hover:bg-[#2b1eb5] disabled:opacity-40 text-white text-sm font-medium px-6 py-3 rounded-full shadow-md transition-all cursor-pointer disabled:cursor-not-allowed"
          >
            {saving ? <RefreshCw size={14} className="animate-spin" /> : <Check size={14} />}
            <span>{saving ? 'Saving...' : initial ? 'Save Changes' : 'Add to Household Bills'}</span>
          </button>
        </div>

      </div>
    </div>,
    document.body,
  )
}
// ── Generate Bills Modal ─────────────────────────────────────────────────────

function GenerateBillsModal({
  dueBills, members, currentUserId, onGenerate, onClose,
}: {
  dueBills: RecurringBill[]
  members: { uid: string; nickname: string }[]
  currentUserId: string
  onGenerate: (billId: string, amount?: number, manualPayerUid?: string) => Promise<void>
  onClose: () => void
}) {
  const [amounts, setAmounts] = useState<Record<string, string>>({})
  const [manualPayers, setManualPayers] = useState<Record<string, string>>(() =>
    Object.fromEntries(dueBills.filter(b => b.payerMode === 'manual').map(b => [b.id, b.rotationQueue[0] ?? '']))
  )
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
      await onGenerate(bill.id, bill.isVariable ? amount : undefined, manualPayers[bill.id])
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
          // Determine displayed payer based on payerMode
          const payerUid = bill.payerMode === 'fixed' && bill.fixedPayerUid
            ? bill.fixedPayerUid
            : bill.payerMode === 'manual'
              ? (manualPayers[bill.id] || bill.rotationQueue[0])
              : bill.rotationQueue[bill.currentPayerIndex % bill.rotationQueue.length]
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

              {/* Manual payer: show selector */}
              {bill.payerMode === 'manual' && isSelected && !isDone && (
                <div className="px-4 pb-3 border-t border-border/50 pt-3">
                  <Label className="text-xs font-bold text-muted-foreground mb-1.5 block">Who pays this month?</Label>
                  <div className="flex flex-wrap gap-2">
                    {bill.rotationQueue.map(uid => {
                      const m = members.find(x => x.uid === uid)
                      return (
                        <button key={uid} onClick={() => setManualPayers(p => ({ ...p, [bill.id]: uid }))}
                          className={`px-3 py-1.5 rounded-full text-xs font-bold border transition-all ${
                            manualPayers[bill.id] === uid ? 'bg-emerald-600 text-white border-emerald-600' : 'bg-secondary text-muted-foreground border-border'
                          }`}>
                          {uid === currentUserId ? `${m?.nickname} (you)` : m?.nickname ?? uid}
                        </button>
                      )
                    })}
                  </div>
                </div>
              )}

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

// ── Month-End Settlement Modal ────────────────────────────────────────────────

type SettlementChoice = { confirmed: boolean; type: 'month_end' | 'rent_adjustment'; note: string }

function MonthEndModal({
  month, members, currentUserId, prevCycle, expenses, billInstances, settlements,
  onClose, onConfirm,
}: {
  month: string
  members: { uid: string; nickname: string }[]
  currentUserId: string
  prevCycle: MonthCycle | null
  expenses: Expense[]
  billInstances: BillInstance[]
  settlements: Settlement[]
  onClose: () => void
  onConfirm: (
    confirmed: { fromUserId: string; toUserId: string; amount: number; type: 'month_end' | 'rent_adjustment'; note?: string }[],
    summary: { totalBillsINR: number; totalExpensesINR: number; totalSettledINR: number; netBalances: Record<string, number> },
    carryForwardOut: Record<string, number> | null,
  ) => Promise<void>
}) {
  const carryForwardIn = prevCycle?.carryForwardOut?.balances ?? null
  const summary = useMemo(
    () => buildMonthSummary(month, expenses, billInstances, settlements, carryForwardIn),
    [month, expenses, billInstances, settlements, carryForwardIn],
  )
  const suggestions = useMemo(
    () => suggestSettlements(summary.netBalances),
    [summary.netBalances],
  )

  const [step, setStep] = useState<'overview' | 'settlements' | 'confirm'>('overview')
  const [choices, setChoices] = useState<Record<number, SettlementChoice>>(() =>
    Object.fromEntries(suggestions.map((_, i) => [i, { confirmed: true, type: 'month_end', note: '' }]))
  )
  const [saving, setSaving] = useState(false)

  const confirmedList = suggestions.filter((_, i) => choices[i]?.confirmed)
  const carryForwardOut = useMemo(
    () => computeCarryForward(summary.netBalances, confirmedList),
    [summary.netBalances, confirmedList],
  )

  const totalConfirmed = confirmedList.reduce((s, c) => s + c.amount, 0)

  const nick = (uid: string) => {
    if (uid === currentUserId) return 'You'
    return members.find(m => m.uid === uid)?.nickname ?? uid
  }

  async function handleConfirm() {
    setSaving(true)
    const confirmed = suggestions
      .map((s, i) => ({ ...s, type: choices[i]?.type ?? 'month_end', note: choices[i]?.note }))
      .filter((_, i) => choices[i]?.confirmed)
    await onConfirm(confirmed, summary, carryForwardOut)
    onClose()
  }

  return (
    <Modal title={`Close ${monthLabelUtil(month)}`} onClose={onClose}>
      {/* Step indicator */}
      <div className="flex items-center gap-1.5 mb-5">
        {(['overview', 'settlements', 'confirm'] as const).map((s, i) => (
          <div key={s} className="flex items-center gap-1.5">
            <div className={`w-6 h-6 rounded-full flex items-center justify-center text-[11px] font-extrabold transition-colors ${
              step === s ? 'bg-primary text-primary-foreground' :
              (['overview', 'settlements', 'confirm'].indexOf(step) > i) ? 'bg-emerald-500 text-white' :
              'bg-secondary text-muted-foreground'
            }`}>{['overview', 'settlements', 'confirm'].indexOf(step) > i ? <Check size={11} /> : i + 1}</div>
            <span className={`text-xs font-semibold hidden sm:block ${step === s ? 'text-foreground' : 'text-muted-foreground'}`}>
              {s === 'overview' ? 'Summary' : s === 'settlements' ? 'Settlements' : 'Confirm'}
            </span>
            {i < 2 && <ChevronRight size={12} className="text-muted-foreground/40 shrink-0" />}
          </div>
        ))}
      </div>

      {/* ── Screen 1: Overview ── */}
      {step === 'overview' && (
        <div className="space-y-4">
          {/* Carry-forward banner */}
          {carryForwardIn && Object.keys(carryForwardIn).length > 0 && (
            <div className="flex items-start gap-3 p-3.5 rounded-xl border border-brand-200 dark:border-brand-900/50 bg-brand-50 dark:bg-brand-900/10">
              <RotateCcw size={15} className="text-brand-600 dark:text-brand-400 mt-0.5 shrink-0" />
              <div>
                <p className="text-xs font-bold text-brand-700 dark:text-brand-300">Carry-forward from {monthLabelUtil(prevMonthKey(month))}</p>
                <div className="flex flex-wrap gap-x-3 mt-0.5">
                  {Object.entries(carryForwardIn).map(([uid, amt]) => (
                    <span key={uid} className={`text-xs font-medium ${amt > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'}`}>
                      {nick(uid)}: {amt > 0 ? '+' : ''}{formatAmount(amt, 'INR')}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* Blocker: variable bills pending */}
          {summary.pendingVariableBills > 0 && (
            <div className="flex items-start gap-3 p-3.5 rounded-xl border border-amber-200 dark:border-amber-800 bg-amber-50 dark:bg-amber-950/30">
              <AlertCircle size={15} className="text-amber-600 dark:text-amber-400 mt-0.5 shrink-0" />
              <p className="text-xs font-semibold text-amber-700 dark:text-amber-300">
                {summary.pendingVariableBills} variable bill{summary.pendingVariableBills > 1 ? 's' : ''} still need amounts entered. Enter them in Monthly Bills before closing.
              </p>
            </div>
          )}

          {/* Month totals */}
          <div className="grid grid-cols-2 gap-2">
            {[
              ['Recurring Bills', summary.totalBillsINR],
              ['Shared Expenses', summary.totalExpensesINR],
              ['Already Settled', summary.totalSettledINR],
              ['Net Unsettled', summary.totalBillsINR + summary.totalExpensesINR - summary.totalSettledINR],
            ].map(([label, val]) => (
              <div key={label as string} className="p-3 rounded-xl bg-secondary/50 border border-border/50">
                <p className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground mb-0.5">{label as string}</p>
                <p className={`text-base font-extrabold ${label === 'Net Unsettled' && (val as number) > 0 ? 'text-amber-600 dark:text-amber-400' : ''}`}>
                  {formatAmount(val as number, 'INR')}
                </p>
              </div>
            ))}
          </div>

          {/* Net balances per person */}
          {Object.keys(summary.netBalances).length > 0 && (
            <div>
              <p className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground mb-2">Net position this month</p>
              <div className="space-y-1.5">
                {Object.entries(summary.netBalances)
                  .sort(([, a], [, b]) => b - a)
                  .map(([uid, bal]) => (
                    <div key={uid} className="flex justify-between items-center px-3 py-2 rounded-lg bg-secondary/40">
                      <span className="text-sm font-medium">{nick(uid)}</span>
                      <span className={`text-sm font-extrabold ${bal > 0 ? 'text-emerald-600 dark:text-emerald-400' : bal < 0 ? 'text-red-500' : 'text-muted-foreground'}`}>
                        {bal > 0 ? '+' : ''}{formatAmount(bal, 'INR')}
                      </span>
                    </div>
                  ))}
              </div>
            </div>
          )}

          <div className="flex gap-2 pt-1">
            <Button variant="outline" className="flex-1" onClick={onClose}>Cancel</Button>
            <Button
              className="flex-1 font-bold"
              disabled={suggestions.length === 0 && !carryForwardIn}
              onClick={() => setStep(suggestions.length > 0 ? 'settlements' : 'confirm')}
            >
              {suggestions.length > 0 ? 'Review Settlements' : 'Close with no transfers'} <ChevronRight size={14} className="ml-1" />
            </Button>
          </div>
        </div>
      )}

      {/* ── Screen 2: Settlements ── */}
      {step === 'settlements' && (
        <div className="space-y-4">
          <p className="text-sm text-muted-foreground">
            Confirm which transfers will be recorded. Unconfirmed amounts carry to {monthLabelUtil(nextMonthKey(month))}.
          </p>

          <div className="space-y-3">
            {suggestions.map((s, i) => {
              const ch = choices[i] ?? { confirmed: true, type: 'month_end', note: '' }
              return (
                <div key={i} className={`rounded-xl border transition-all overflow-hidden ${ch.confirmed ? 'border-primary/40 bg-primary/5' : 'border-border opacity-50'}`}>
                  <div className="flex items-center gap-3 p-3.5">
                    {/* Confirm toggle */}
                    <button
                      onClick={() => setChoices(c => ({ ...c, [i]: { ...ch, confirmed: !ch.confirmed } }))}
                      className={`w-5 h-5 rounded border-2 flex items-center justify-center shrink-0 transition-colors ${ch.confirmed ? 'bg-primary border-primary' : 'border-muted-foreground/40'}`}
                    >
                      {ch.confirmed && <Check size={11} className="text-white" strokeWidth={3} />}
                    </button>

                    {/* Transfer arrow */}
                    <div className="flex-1 flex items-center gap-2 min-w-0">
                      <span className="text-sm font-bold truncate">{nick(s.fromUserId)}</span>
                      <ArrowRight size={14} className="text-muted-foreground shrink-0" />
                      <span className="text-sm font-bold truncate">{nick(s.toUserId)}</span>
                    </div>

                    {/* Amount */}
                    <span className="text-base font-extrabold shrink-0">{formatAmount(s.amount, 'INR')}</span>
                  </div>

                  {ch.confirmed && (
                    <div className="px-3.5 pb-3 space-y-2 border-t border-border/40 pt-3">
                      {/* Type toggle */}
                      <div className="flex gap-1.5">
                        {(['month_end', 'rent_adjustment'] as const).map(t => (
                          <button key={t}
                            onClick={() => setChoices(c => ({ ...c, [i]: { ...ch, type: t } }))}
                            className={`flex-1 py-1.5 rounded-lg text-xs font-bold border transition-all ${
                              ch.type === t ? 'bg-primary text-primary-foreground border-primary' : 'bg-secondary text-muted-foreground border-border'
                            }`}>
                            {t === 'month_end' ? 'Cash / UPI' : 'Rent Adjustment'}
                          </button>
                        ))}
                      </div>
                      {ch.type === 'rent_adjustment' && (
                        <p className="text-[11px] text-muted-foreground">
                          ₹{s.amount} deducted from {nick(s.fromUserId)}&apos;s rent next month instead of cash transfer.
                        </p>
                      )}
                      <input
                        placeholder="Note (optional)"
                        maxLength={80}
                        value={ch.note}
                        onChange={e => setChoices(c => ({ ...c, [i]: { ...ch, note: e.target.value } }))}
                        className="w-full h-8 px-3 rounded-lg border border-border bg-background text-xs font-medium outline-none focus:border-primary/60"
                      />
                    </div>
                  )}
                </div>
              )
            })}
          </div>

          {/* Carry-forward preview */}
          {carryForwardOut && Object.keys(carryForwardOut).length > 0 && (
            <div className="p-3 rounded-xl border border-amber-200 dark:border-amber-800 bg-amber-50 dark:bg-amber-950/20">
              <p className="text-[11px] font-bold uppercase tracking-wider text-amber-700 dark:text-amber-400 mb-1">Carries to {monthLabelUtil(nextMonthKey(month))}</p>
              <div className="flex flex-wrap gap-x-3">
                {Object.entries(carryForwardOut).map(([uid, amt]) => (
                  <span key={uid} className={`text-xs font-semibold ${amt > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-500'}`}>
                    {nick(uid)}: {amt > 0 ? '+' : ''}{formatAmount(amt, 'INR')}
                  </span>
                ))}
              </div>
            </div>
          )}

          <div className="flex gap-2 pt-1">
            <Button variant="outline" onClick={() => setStep('overview')}><ChevronLeft size={14} className="mr-1" /> Back</Button>
            <Button className="flex-1 font-bold" onClick={() => setStep('confirm')}>
              Review & Close <ChevronRight size={14} className="ml-1" />
            </Button>
          </div>
        </div>
      )}

      {/* ── Screen 3: Confirm ── */}
      {step === 'confirm' && (
        <div className="space-y-4">
          <div className="p-4 rounded-xl border border-border bg-secondary/30 space-y-2">
            <p className="text-sm font-bold">{monthLabelUtil(month)} — final summary</p>
            <div className="space-y-1">
              <div className="flex justify-between text-xs">
                <span className="text-muted-foreground">Settlements to record</span>
                <span className="font-bold">{confirmedList.length}</span>
              </div>
              <div className="flex justify-between text-xs">
                <span className="text-muted-foreground">Total transferred</span>
                <span className="font-bold">{formatAmount(totalConfirmed, 'INR')}</span>
              </div>
              {carryForwardOut && Object.keys(carryForwardOut).length > 0 && (
                <div className="flex justify-between text-xs">
                  <span className="text-muted-foreground">Carry to {monthLabelUtil(nextMonthKey(month))}</span>
                  <span className="font-bold text-amber-600 dark:text-amber-400">Yes</span>
                </div>
              )}
              {(!carryForwardOut || Object.keys(carryForwardOut).length === 0) && (
                <div className="flex justify-between text-xs">
                  <span className="text-muted-foreground">All balances</span>
                  <span className="font-bold text-emerald-600 dark:text-emerald-400">Fully settled ✓</span>
                </div>
              )}
            </div>
          </div>

          <p className="text-xs text-muted-foreground">
            This will close {monthLabelUtil(month)} permanently. All confirmed settlements will be recorded and the month will be locked.
          </p>

          <div className="flex gap-2 pt-1">
            <Button variant="outline" onClick={() => setStep(suggestions.length > 0 ? 'settlements' : 'overview')}>
              <ChevronLeft size={14} className="mr-1" /> Back
            </Button>
            <Button
              className="flex-1 font-bold bg-emerald-600 hover:bg-emerald-700 text-white"
              onClick={handleConfirm} disabled={saving}
            >
              {saving ? 'Closing…' : `Close ${monthLabelUtil(month)}`}
            </Button>
          </div>
        </div>
      )}
    </Modal>
  )
}

// ── Quick Setup Modal — bulk-create standard recurring bills ─────────────────

const QUICK_TEMPLATES: { category: ExpenseCategory; name: string; isVariable: boolean; typical: number | null; emoji: string }[] = [
  { category: 'rent',        name: 'Rent',             isVariable: false, typical: 15000, emoji: '🏠' },
  { category: 'electricity', name: 'Electricity',      isVariable: true,  typical: null,  emoji: '⚡' },
  { category: 'water',       name: 'Water',            isVariable: true,  typical: null,  emoji: '💧' },
  { category: 'internet',    name: 'Internet / WiFi',  isVariable: false, typical: 799,   emoji: '📶' },
  { category: 'maid',        name: 'Maid',             isVariable: false, typical: 2000,  emoji: '🧹' },
  { category: 'gas',         name: 'Gas / LPG',        isVariable: true,  typical: null,  emoji: '🔥' },
  { category: 'maintenance', name: 'Maintenance',      isVariable: true,  typical: null,  emoji: '🔧' },
  { category: 'ac',          name: 'AC / Cooling',     isVariable: true,  typical: null,  emoji: '❄️' },
]

function QuickSetupModal({
  members, currentUserId, onSave, onClose,
}: {
  members: { uid: string; nickname: string }[]
  currentUserId: string
  onSave: (bills: Array<Omit<RecurringBill, 'id' | 'createdAt' | 'currentPayerIndex' | 'lastGeneratedMonth'>>) => Promise<void>
  onClose: () => void
}) {
  const [selected, setSelected] = useState<Set<string>>(new Set(['rent', 'electricity', 'internet']))
  const [amounts, setAmounts] = useState<Record<string, string>>(
    Object.fromEntries(QUICK_TEMPLATES.filter(t => !t.isVariable && t.typical).map(t => [t.category, String(t.typical)]))
  )
  const [payerModes, setPayerModes] = useState<Record<string, PayerMode>>(
    Object.fromEntries(QUICK_TEMPLATES.map(t => [t.category, 'rotation' as PayerMode]))
  )
  const [saving, setSaving] = useState(false)

  const allParticipants = members.map(m => m.uid)

  async function handleCreate() {
    const bills = QUICK_TEMPLATES
      .filter(t => selected.has(t.category))
      .map(t => ({
        name: t.name,
        category: t.category,
        isVariable: t.isVariable,
        amount: t.isVariable ? null : (parseFloat(amounts[t.category]) || null),
        currency: 'INR' as Currency,
        billingDay: 1,
        rotationQueue: allParticipants,
        participants: allParticipants,
        payerMode: payerModes[t.category],
        splitMethod: 'equal' as SplitMethod,
        active: true,
        createdBy: currentUserId,
      }))
    if (bills.length === 0) return
    setSaving(true)
    await onSave(bills)
    onClose()
  }

  return (
    <Modal title="Quick Bill Setup" onClose={onClose}>
      <div className="space-y-4">
        <p className="text-sm text-muted-foreground">
          Select the bills that apply to your flat. You can edit any of them individually after setup.
        </p>

        <div className="space-y-2">
          {QUICK_TEMPLATES.map(t => {
            const on = selected.has(t.category)
            return (
              <div key={t.category}
                className={`rounded-xl border transition-all overflow-hidden ${on ? 'border-primary/40' : 'border-border opacity-60'}`}>
                <div className="flex items-center gap-3 p-3">
                  {/* Toggle */}
                  <button onClick={() => setSelected(s => {
                    const n = new Set(s)
                    n.has(t.category) ? n.delete(t.category) : n.add(t.category)
                    return n
                  })} className={`w-5 h-5 rounded border-2 flex items-center justify-center shrink-0 transition-colors ${on ? 'bg-primary border-primary' : 'border-muted-foreground/40'}`}>
                    {on && <Check size={11} className="text-white" strokeWidth={3} />}
                  </button>

                  <span className="text-xl shrink-0">{t.emoji}</span>

                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-bold">{t.name}</p>
                    <p className="text-[11px] text-muted-foreground">{t.isVariable ? 'Variable — enter each month' : 'Fixed monthly'}</p>
                  </div>

                  {/* Amount (fixed only) */}
                  {!t.isVariable && on && (
                    <div className="relative w-24 shrink-0">
                      <span className="absolute left-2 top-1/2 -translate-y-1/2 text-xs text-muted-foreground">₹</span>
                      <input type="number" placeholder="0" min="0"
                        className="w-full h-8 pl-5 pr-2 rounded-lg border border-border bg-background text-xs font-bold outline-none focus:border-primary/60"
                        value={amounts[t.category] ?? ''}
                        onChange={e => setAmounts(a => ({ ...a, [t.category]: e.target.value }))} />
                    </div>
                  )}
                </div>

                {/* Payer mode (when selected) */}
                {on && (
                  <div className="px-3 pb-3 flex items-center gap-1.5">
                    <span className="text-[11px] text-muted-foreground shrink-0">Paid by:</span>
                    {(['rotation', 'fixed', 'manual'] as PayerMode[]).map(mode => (
                      <button key={mode} onClick={() => setPayerModes(p => ({ ...p, [t.category]: mode }))}
                        className={`px-2 py-1 rounded-lg text-[10px] font-bold border transition-all ${
                          payerModes[t.category] === mode ? 'bg-primary text-primary-foreground border-primary' : 'bg-secondary text-muted-foreground border-border'
                        }`}>
                        {mode === 'rotation' ? 'Rotates' : mode === 'fixed' ? 'Fixed' : 'Manual'}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            )
          })}
        </div>

        <p className="text-[11px] text-muted-foreground">
          All bills will split equally among all {members.length} members. Configure individual splits after setup.
        </p>

        <div className="flex gap-2 pt-1">
          <Button variant="outline" className="flex-1" onClick={onClose}>Cancel</Button>
          <Button className="flex-1 font-bold" onClick={handleCreate} disabled={saving || selected.size === 0}>
            {saving ? 'Creating…' : `Create ${selected.size} bill${selected.size !== 1 ? 's' : ''}`}
          </Button>
        </div>
      </div>
    </Modal>
  )
}

// ── Fixed Bills Breakdown Modal ───────────────────────────────────────────────

function FixedBillsBreakdownModal({
  members,
  currentUserId,
  recurringBills,
  billInstances,
  monthKey,
  onClose,
}: {
  members: { uid: string; nickname: string }[]
  currentUserId: string
  recurringBills: RecurringBill[]
  billInstances: BillInstance[]
  monthKey: string
  onClose: () => void
}) {
  const activeBills = recurringBills.filter(b => b.active)

  const avatarColors = ['bg-indigo-600', 'bg-violet-500', 'bg-slate-500', 'bg-teal-600', 'bg-orange-500', 'bg-rose-500']

  const totalCommitment = activeBills.reduce((s, b) => {
    const inst = billInstances.find(bi => bi.templateId === b.id && bi.month === monthKey)
    return s + (inst?.amount ?? b.amount ?? 0)
  }, 0)

  const memberBreakdowns = members.map((m, idx) => {
    const myBills = activeBills.filter(b => {
      const participants = b.participants?.length ? b.participants : b.rotationQueue
      return participants.includes(m.uid)
    })
    const billLines = myBills.map(b => {
      const inst = billInstances.find(bi => bi.templateId === b.id && bi.month === monthKey)
      const participants = b.participants?.length ? b.participants : b.rotationQueue
      const total = inst?.amount ?? b.amount ?? 0
      const share = participants.length > 0 ? total / participants.length : total
      const isPaid = inst?.status === 'paid'
      return { bill: b, inst, share, isPaid, cfg: CATEGORY_CONFIG[b.category] }
    })
    const totalShare = billLines.reduce((s, l) => s + l.share, 0)
    const allPaid = billLines.length > 0 && billLines.every(l => l.isPaid)
    const anyPaid = billLines.some(l => l.isPaid)
    const status = allPaid ? 'paid' : 'pending'
    const activeBillsCount = billLines.filter(l => l.inst).length
    return { member: m, idx, billLines, totalShare, status, activeBillsCount, totalBills: myBills.length }
  })

  const nextDue = activeBills.map(b => b.billingDay).sort((a, b) => a - b)
  const today = new Date().getDate()
  const nextBillingDay = nextDue.find(d => d >= today) ?? nextDue[0]

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-[2px]" onClick={onClose} />
      <div className="relative bg-white dark:bg-[#1A202C] rounded-t-[24px] sm:rounded-[24px] w-full sm:max-w-[520px] shadow-[0px_10px_40px_-10px_rgba(0,0,0,0.20)] flex flex-col max-h-[92dvh]">

        {/* Header */}
        <div className="flex items-center justify-between px-6 pt-6 pb-4 border-b border-black/[0.08] shrink-0">
          <div>
            <p className="text-[11px] font-semibold text-[#777587] dark:text-gray-400 uppercase tracking-widest">
              Expenses &rsaquo; Fixed Bills
            </p>
            <h2 className="text-[20px] font-bold text-[#141b2b] dark:text-white leading-tight mt-0.5">
              Resident Breakdown
            </h2>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full flex items-center justify-center hover:bg-[#f1f3ff] dark:hover:bg-white/10 transition-colors cursor-pointer shrink-0"
          >
            <X size={14} className="text-[#777587] dark:text-gray-400" />
          </button>
        </div>

        {/* Scrollable body */}
        <div className="overflow-y-auto flex-1 px-6 py-5 space-y-5">

          {/* Two summary cards */}
          <div className="grid grid-cols-2 gap-3">
            {/* Total bills card — indigo */}
            <div className="bg-[#3525cd] rounded-[20px] p-5 relative overflow-hidden shadow-[0px_10px_15px_-3px_rgba(53,37,205,0.25)]">
              <div className="absolute top-[-12px] right-[-12px] w-20 h-20 rounded-full bg-white/10" />
              <div className="absolute top-4 right-4 w-10 h-10 rounded-full bg-white/10" />
              <p className="text-white/75 text-[11px] font-semibold uppercase tracking-widest relative">
                Total Bills
              </p>
              <p className="text-white text-[28px] font-extrabold leading-tight mt-1 relative">
                {formatAmount(totalCommitment, 'INR')}
              </p>
              <div className="flex gap-2 mt-3 relative flex-wrap">
                <span className="bg-white/20 backdrop-blur-sm text-white text-[10px] font-bold px-2.5 py-1 rounded-full flex items-center gap-1">
                  <RefreshCw size={9} />
                  {activeBills.length} active
                </span>
                {nextBillingDay != null && (
                  <span className="bg-white/20 backdrop-blur-sm text-white text-[10px] font-bold px-2.5 py-1 rounded-full flex items-center gap-1">
                    <CalendarCheck size={9} />
                    Due {ordinal(nextBillingDay)}
                  </span>
                )}
              </div>
            </div>

            {/* Household size card — light */}
            <div className="bg-[#f1f3ff] dark:bg-white/[0.06] rounded-[20px] p-5 flex flex-col justify-between border border-[rgba(199,196,216,0.3)]">
              <div>
                <p className="text-[#464555] dark:text-gray-400 text-[12px] font-medium">Household Size</p>
                <p className="text-[#141b2b] dark:text-white text-[22px] font-bold mt-1 leading-tight">
                  {members.length} Residents
                </p>
              </div>
              <div className="flex items-center gap-1 mt-3">
                {members.slice(0, 5).map((m, i) => (
                  <div
                    key={m.uid}
                    className={['w-8 h-8 rounded-full border-2 border-white dark:border-[#1A202C] flex items-center justify-center text-[11px] font-bold text-white shrink-0', avatarColors[i % avatarColors.length]].join(' ')}
                    style={{ marginLeft: i > 0 ? '-8px' : '0' }}
                  >
                    {m.nickname.charAt(0).toUpperCase()}
                  </div>
                ))}
                {members.length > 5 && (
                  <div className="w-8 h-8 rounded-full border-2 border-white dark:border-[#1A202C] bg-[#e9edff] flex items-center justify-center text-[10px] font-bold text-[#3525cd] shrink-0" style={{ marginLeft: '-8px' }}>
                    +{members.length - 5}
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Per-resident breakdown */}
          <div>
            <p className="text-[13px] font-bold text-[#141b2b] dark:text-white mb-3">Household Contribution Breakdown</p>
            <div className="space-y-3">
              {memberBreakdowns.map(({ member, idx, billLines, totalShare, status, activeBillsCount, totalBills }) => (
                <div key={member.uid} className="bg-[#f1f3ff] dark:bg-white/[0.05] rounded-[20px] border border-[rgba(199,196,216,0.25)] overflow-hidden">
                  {/* Person header */}
                  <div className="flex items-center justify-between px-5 pt-5 pb-4 border-b border-[rgba(199,196,216,0.3)]">
                    <div className="flex items-center gap-3">
                      <div className={['w-11 h-11 rounded-full flex items-center justify-center text-base font-bold text-white shrink-0', avatarColors[idx % avatarColors.length]].join(' ')}>
                        {member.nickname.charAt(0).toUpperCase()}
                      </div>
                      <div>
                        <p className="text-[#141b2b] dark:text-white text-[16px] font-semibold leading-tight">
                          {member.uid === currentUserId ? member.nickname + ' (you)' : member.nickname}
                        </p>
                        <p className="text-[#464555] dark:text-gray-400 text-[12px] font-medium mt-0.5">
                          {activeBillsCount}/{totalBills} bills active
                        </p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="text-[#141b2b] dark:text-white text-[22px] font-bold leading-tight">
                        {totalShare > 0 ? formatAmount(totalShare, 'INR') : '--'}
                      </p>
                      <span className={[
                        'inline-flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded-full mt-1',
                        status === 'paid'
                          ? 'bg-[rgba(53,37,205,0.1)] text-[#3525cd]'
                          : 'bg-[rgba(186,26,26,0.08)] text-[#ba1a1a]',
                      ].join(' ')}>
                        {status === 'paid' ? <Check size={9} /> : <AlertCircle size={9} />}
                        {status === 'paid' ? 'Paid' : 'Pending'}
                      </span>
                    </div>
                  </div>

                  {/* Bill lines */}
                  {billLines.length > 0 && (
                    <div className="px-5 py-3 space-y-0">
                      {billLines.map(({ bill, share, isPaid, cfg }) => (
                        <div key={bill.id} className={['flex items-center justify-between py-2.5', isPaid ? '' : ''].join(' ')}>
                          <div className="flex items-center gap-3">
                            <div className="w-8 h-8 rounded-full bg-[#dce2f7] dark:bg-white/10 flex items-center justify-center text-sm shrink-0">
                              {cfg?.emoji}
                            </div>
                            <p className="text-[#141b2b] dark:text-white text-[14px]">{bill.name}</p>
                          </div>
                          <p className="text-[#141b2b] dark:text-white text-[14px] font-medium">
                            {share > 0 ? formatAmount(share, 'INR') : 'Variable'}
                          </p>
                        </div>
                      ))}
                    </div>
                  )}
                  {billLines.length === 0 && (
                    <div className="px-5 py-4">
                      <p className="text-[#777587] dark:text-gray-500 text-[13px] italic">Not assigned to any active bills</p>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>

        </div>

        {/* Footer */}
        <div className="px-6 pb-6 pt-4 border-t border-black/[0.08] shrink-0">
          <button
            onClick={onClose}
            className="w-full bg-[#3525cd] hover:bg-[#2b1eb5] text-white text-sm font-semibold py-3 rounded-full transition-colors cursor-pointer"
          >
            Done
          </button>
        </div>

      </div>
    </div>,
    document.body,
  )
}

// ── Main Page ─────────────────────────────────────────────────────────────────

export default function ExpensesPage() {
  const {
    name: flatName,
    expenses, settlements, recurringBills, billInstances, monthCycles, members,
    addExpense, deleteExpense, addSettlement,
    createRecurringBill, updateRecurringBill, deleteRecurringBill,
    generateBill, generateAllDueBills, confirmBillAmount, editBillInstanceAmount, markBillPaid, skipBillInstance, deleteBillInstance,
    createRecurringBill: _createSingle, bulkCreateRecurringBills, closeMonth,
  } = useFlatStore()
  const { user } = useAuthStore()

  const [showAddExpense, setShowAddExpense] = useState(false)
  const [showAddBill, setShowAddBill] = useState(false)
  const [editBill, setEditBill] = useState<RecurringBill | null>(null)
  const [settleTarget, setSettleTarget] = useState<{ userId: string; amount: number; currency: Currency } | null>(null)
  const [showGenerate, setShowGenerate] = useState(false)
  const [showMonthEnd, setShowMonthEnd] = useState(false)
  const [showQuickSetup, setShowQuickSetup] = useState(false)
  const [expandedHistory, setExpandedHistory] = useState<Set<string>>(new Set())
  const [showBreakdown, setShowBreakdown] = useState(false)
  const hasAutoOpened = useRef(false)

  // Month cycle helpers
  const currentCycle = monthCycles.find(mc => mc.month === currentMonthKey())
  const prevCycle = monthCycles.find(mc => mc.month === prevMonthKey(currentMonthKey()))
  const carryForwardIn = prevCycle?.carryForwardOut?.balances ?? null
  const isCurrentMonthClosed = currentCycle?.status === 'closed'

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


  // Derived balance totals for the two stat cards
  const totalYouOwe = balances
    .filter(b => b.amount < 0 && b.currency === 'INR')
    .reduce((s, b) => s + Math.abs(b.amount), 0)
  const totalOwedToYou = balances
    .filter(b => b.amount > 0 && b.currency === 'INR')
    .reduce((s, b) => s + b.amount, 0)

  const netUnsettled = useMemo(() => {
    return balances.reduce((sum, b) => sum + (b.amount < 0 && b.currency === 'INR' ? Math.abs(b.amount) : 0), 0)
  }, [balances])

  const [activeTab, setActiveTab] = useState<'daily' | 'bills'>('daily')
  const [expandedBillId, setExpandedBillId] = useState<string | null>(null)
  const [confirmDeleteBillId, setConfirmDeleteBillId] = useState<string | null>(null)

  const nick = (uid: string) => members.find(m => m.uid === uid)?.nickname ?? uid

  const thisMonthExpensesTotal = useMemo(() => {
    const key = currentMonthKey()
    return sortedExpenses
      .filter(e => e.date.startsWith(key) && e.currency === 'INR')
      .reduce((s, e) => s + e.amount, 0)
  }, [sortedExpenses])

  const sevenDayBars = useMemo(() => {
    const today = new Date()
    return Array.from({ length: 7 }, (_, i) => {
      const d = new Date(today)
      d.setDate(today.getDate() - (6 - i))
      const dayStr = d.toISOString().slice(0, 10)
      const total = sortedExpenses
        .filter(e => e.date === dayStr && e.currency === 'INR')
        .reduce((s, e) => s + e.amount, 0)
      const dayLabel = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'][d.getDay()]
      return { dayStr, total, dayLabel }
    })
  }, [sortedExpenses])

  const maxBar = useMemo(() => Math.max(1, ...sevenDayBars.map(b => b.total)), [sevenDayBars])

  const totalMonthlyCommitment = useMemo(
    () => recurringBills.filter(b => b.active && b.amount).reduce((s, b) => s + (b.amount ?? 0), 0),
    [recurringBills],
  )

  const nextBillingDay = useMemo(() => {
    const days = recurringBills
      .filter(b => b.active)
      .map(b => b.billingDay)
      .sort((a, b) => a - b)
    if (!days.length) return null
    const today = new Date().getDate()
    return days.find(d => d >= today) ?? days[0]
  }, [recurringBills])

  const myMonthlyShare = useMemo(() =>
    recurringBills.filter(b => b.active && b.amount).reduce((s, b) => {
      const participants = b.participants?.length ? b.participants : b.rotationQueue
      if (!participants.includes(currentUserId)) return s
      return s + (b.amount ?? 0) / Math.max(participants.length, 1)
    }, 0),
    [recurringBills, currentUserId]
  )

  return (
    <div className="space-y-4 max-w-2xl">

      {/* Header */}
      <div className="pt-1">
        <p className="text-xs font-medium text-[#999CA1] dark:text-muted-foreground">
          {flatName || 'My Flat'} &middot; {monthLabel(currentMonthKey())}
        </p>
        <h1 className="text-[22px] font-bold text-[#021328] dark:text-foreground tracking-tight mt-0.5 leading-tight">
          Expenses Hub
        </h1>
      </div>

      {/* Alerts */}
      {dueBills.length > 0 && isAdmin && (
        <div className="flex items-center gap-3 p-3 rounded-2xl border border-amber-200 dark:border-amber-700 bg-amber-50 dark:bg-amber-950/30">
          <Zap size={14} className="text-amber-600 shrink-0" />
          <p className="text-xs font-bold text-amber-800 dark:text-amber-200 flex-1 min-w-0 truncate">
            {dueBills.length} bill{dueBills.length > 1 ? 's' : ''} due &mdash; {dueBills.map(b => b.name).join(', ')}
          </p>
          <button
            onClick={() => setShowGenerate(true)}
            className="shrink-0 bg-[#3786FB] hover:bg-[#2672e6] text-white text-[11px] font-bold px-3 py-1 rounded-lg transition-colors cursor-pointer"
          >
            Generate
          </button>
        </div>
      )}
      {isNearMonthEnd && pendingVariableBills.length > 0 && (
        <div className="flex items-center gap-3 p-3 rounded-2xl border border-blue-200 dark:border-blue-700 bg-blue-50 dark:bg-blue-950/30">
          <AlertCircle size={14} className="text-blue-600 shrink-0" />
          <p className="text-xs font-bold text-blue-800 dark:text-blue-200 flex-1 min-w-0 truncate">
            Month ending &mdash; confirm {pendingVariableBills.map(b => b.name).join(', ')}
          </p>
          {isAdmin && (
            <button
              onClick={() => setShowGenerate(true)}
              className="shrink-0 text-[#3786FB] text-[11px] font-bold px-3 py-1 rounded-lg border border-[#3786FB] hover:bg-blue-50 transition-colors cursor-pointer"
            >
              Enter
            </button>
          )}
        </div>
      )}

      {/* Hero settlement card */}
      <div
        className="rounded-[20px] overflow-hidden shadow-[0px_7px_15px_0px_rgba(0,0,0,0.14)]"
        style={{ background: isCurrentMonthClosed ? '#4CAF82' : netUnsettled > 0 ? '#EB986A' : '#4CAF82' }}
      >
        <div className="p-5 pb-4">
          <div className="flex items-start justify-between">
            <div className="flex-1 min-w-0">
              <p className="text-white/80 text-[11px] font-semibold uppercase tracking-widest">
                {isCurrentMonthClosed ? 'Month Closed' : netUnsettled > 0 ? 'Settlement Pending' : 'All Settled'}
              </p>
              <p className="text-white text-3xl font-extrabold mt-2 leading-none">
                {formatAmount(netUnsettled, 'INR')}
              </p>
              <p className="text-white/75 text-xs mt-1.5">
                {sortedExpenses.filter(e => e.date.startsWith(currentMonthKey())).length} transactions this month
              </p>
            </div>
            <div className="shrink-0 flex flex-col items-end gap-2">
              {isCurrentMonthClosed && (
                <span className="text-[10px] font-bold bg-white/25 text-white px-2.5 py-1 rounded-lg">Closed</span>
              )}
              {isAdmin && !isCurrentMonthClosed && (
                <button
                  onClick={() => setShowMonthEnd(true)}
                  className="bg-white/25 hover:bg-white/35 text-white text-[11px] font-bold px-3 py-1.5 rounded-lg transition-colors cursor-pointer flex items-center gap-1"
                >
                  <CalendarCheck size={11} />
                  {netUnsettled > 0 ? 'Finalize' : 'Close month'}
                </button>
              )}
            </div>
          </div>
        </div>
        <div className="bg-[rgba(26,32,44,0.75)] backdrop-blur-sm px-5 py-4">
          <div className="flex items-center justify-between mb-3">
            <p className="text-[#CCCDD0] text-xs font-medium">Your balance</p>
            {balances.length === 0 && (
              <span className="text-[10px] font-bold text-emerald-400">All squared up</span>
            )}
          </div>
          {balances.length > 0 ? (
            <div className="space-y-2.5">
              {balances.map(b => {
                const m = members.find(x => x.uid === b.userId)
                const isOwed = b.amount > 0
                return (
                  <div key={b.userId + b.currency} className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <div className="w-7 h-7 rounded-full bg-white/15 flex items-center justify-center text-[11px] font-bold text-white shrink-0">
                        {(m?.nickname ?? '?').charAt(0).toUpperCase()}
                      </div>
                      <div>
                        <p className="text-white text-xs font-semibold leading-none">{m?.nickname ?? '...'}</p>
                        <p className="text-[#999CA1] text-[10px] mt-0.5">{isOwed ? 'owes you' : 'you owe'}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className={['text-sm font-extrabold', isOwed ? 'text-emerald-400' : 'text-[#FF8C69]'].join(' ')}>
                        {formatAmount(Math.abs(b.amount), b.currency)}
                      </span>
                      {!isOwed && (
                        <button
                          onClick={() => setSettleTarget({ userId: b.userId, amount: Math.abs(b.amount), currency: b.currency })}
                          className="bg-[#3786FB] hover:bg-[#2672e6] text-white text-[10px] font-bold px-2.5 py-0.5 rounded-full transition-colors cursor-pointer"
                        >
                          Settle
                        </button>
                      )}
                    </div>
                  </div>
                )
              })}
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Check size={13} className="text-emerald-400" />
              <p className="text-white text-xs font-semibold">No pending balances this month</p>
            </div>
          )}
        </div>
      </div>

      {/* Two dark navy stat cards */}
      <div className="grid grid-cols-2 gap-3">
        <div className="bg-[#1A202C] rounded-[20px] shadow-[0px_7px_15px_0px_rgba(0,0,0,0.14)] p-4 min-h-[110px]">
          <div className="flex items-start justify-between">
            <p className="text-[#999CA1] text-sm font-medium">You owe</p>
            <ArrowUpRight size={14} className={totalYouOwe > 0 ? 'text-[#FF8C69]' : 'text-[#4D515B]'} />
          </div>
          <p className="text-white text-2xl font-bold mt-2 leading-none">{formatAmount(totalYouOwe, 'INR')}</p>
          <p className="text-[#4D515B] text-[11px] mt-2">
            {balances.filter(b => b.amount < 0).length} {balances.filter(b => b.amount < 0).length === 1 ? 'person' : 'people'}
          </p>
        </div>
        <div className="bg-[#1A202C] rounded-[20px] shadow-[0px_7px_15px_0px_rgba(0,0,0,0.14)] p-4 min-h-[110px]">
          <div className="flex items-start justify-between">
            <p className="text-[#999CA1] text-sm font-medium">Owed to you</p>
            <ArrowDownLeft size={14} className={totalOwedToYou > 0 ? 'text-emerald-400' : 'text-[#4D515B]'} />
          </div>
          <p className="text-white text-2xl font-bold mt-2 leading-none">{formatAmount(totalOwedToYou, 'INR')}</p>
          <p className="text-[#4D515B] text-[11px] mt-2">
            {balances.filter(b => b.amount > 0).length} {balances.filter(b => b.amount > 0).length === 1 ? 'person' : 'people'}
          </p>
        </div>
      </div>

      {/* Tab switcher */}
      <div className="flex gap-1 p-1 bg-secondary/70 dark:bg-secondary/50 rounded-xl">
        <button
          onClick={() => setActiveTab('daily')}
          className={['flex-1 flex flex-col items-center justify-center gap-0.5 py-2 rounded-lg text-sm font-bold transition-all cursor-pointer',
            activeTab === 'daily' ? 'bg-[#3786FB] text-white shadow-sm' : 'text-muted-foreground hover:text-foreground',
          ].join(' ')}
        >
          <div className="flex items-center gap-1.5">
            Daily Splits
            {netUnsettled > 0 && activeTab !== 'daily' && <span className="w-1.5 h-1.5 bg-red-500 rounded-full shrink-0" />}
          </div>
          {thisMonthExpensesTotal > 0 && (
            <span className={['text-[11px] font-semibold leading-none', activeTab === 'daily' ? 'text-white/75' : 'text-muted-foreground/60'].join(' ')}>
              {formatAmount(thisMonthExpensesTotal, 'INR')}
            </span>
          )}
        </button>
        <button
          onClick={() => setActiveTab('bills')}
          className={['flex-1 flex flex-col items-center justify-center gap-0.5 py-2 rounded-lg text-sm font-bold transition-all cursor-pointer',
            activeTab === 'bills' ? 'bg-[#3786FB] text-white shadow-sm' : 'text-muted-foreground hover:text-foreground',
          ].join(' ')}
        >
          <div className="flex items-center gap-1.5">
            Fixed Bills
            {dueBills.length > 0 && activeTab !== 'bills' && <span className="w-1.5 h-1.5 bg-amber-500 rounded-full shrink-0" />}
          </div>
          {totalMonthlyCommitment > 0 && (
            <span className={['text-[11px] font-semibold leading-none', activeTab === 'bills' ? 'text-white/75' : 'text-muted-foreground/60'].join(' ')}>
              {formatAmount(totalMonthlyCommitment, 'INR')}
            </span>
          )}
        </button>
      </div>

      {/* DAILY SPLITS TAB */}
      {activeTab === 'daily' && (
        <div className="space-y-4">
          {carryForwardIn && Object.keys(carryForwardIn).length > 0 && (
            <div className="flex items-center gap-2.5 p-3.5 rounded-2xl border border-border bg-secondary/30">
              <RotateCcw size={13} className="text-muted-foreground shrink-0" />
              <p className="text-xs text-muted-foreground">
                Carry-forward from {monthLabelUtil(prevMonthKey(currentMonthKey()))} included &mdash;{' '}
                {Object.entries(carryForwardIn).map(([uid, amt]) => (
                  <span key={uid} className={['font-semibold', amt > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-500'].join(' ')}>
                    {nick(uid)}: {amt > 0 ? '+' : ''}{formatAmount(amt, 'INR')}{' '}
                  </span>
                ))}
              </p>
            </div>
          )}

          {sortedExpenses.length > 0 && (
            <div className="p-4 rounded-[20px] border border-border bg-card shadow-sm">
              <div className="flex items-center justify-between mb-3">
                <p className="text-xs font-bold text-[#999CA1] dark:text-muted-foreground uppercase tracking-wider">7-Day Activity</p>
                {thisMonthExpensesTotal > 0 && (
                  <p className="text-xs font-bold text-[#021328] dark:text-foreground">{formatAmount(thisMonthExpensesTotal, 'INR')} this month</p>
                )}
              </div>
              <div className="flex items-end gap-1" style={{ height: '48px' }}>
                {sevenDayBars.map((bar, i) => (
                  <div key={i} className="flex-1 flex flex-col items-center gap-1 h-full justify-end">
                    <div
                      className="w-full rounded-sm transition-all"
                      style={{
                        height: Math.max(3, Math.round((bar.total / maxBar) * 36)) + 'px',
                        backgroundColor: bar.total > 0 ? '#3786FB' : 'hsl(var(--secondary))',
                        opacity: bar.total > 0 ? 1 : 0.5,
                      }}
                    />
                    <span className="text-[9px] text-[#999CA1] dark:text-muted-foreground font-medium leading-none">{bar.dayLabel}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          <section>
            <div className="flex items-center gap-2 mb-3">
              <Receipt size={14} className="text-muted-foreground" />
              <h2 className="text-sm font-bold text-[#021328] dark:text-foreground">Shared Expenses</h2>
              {sortedExpenses.length > 0 && (
                <span className="text-xs font-bold text-[#999CA1] bg-secondary px-2 py-0.5 rounded-full">{sortedExpenses.length}</span>
              )}
              <button
                onClick={() => setShowAddExpense(true)}
                className="ml-auto flex items-center gap-1 text-xs font-bold text-[#3786FB] hover:opacity-75 transition-opacity cursor-pointer"
              >
                <Plus size={12} /> Add
              </button>
            </div>

            {sortedExpenses.length === 0 ? (
              <Card className="border-dashed border-2">
                <CardContent className="flex flex-col items-center justify-center p-12 text-center">
                  <Inbox size={36} className="text-muted-foreground/25 mb-3" />
                  <p className="font-bold text-base text-muted-foreground">No shared expenses yet</p>
                  <p className="text-sm text-muted-foreground/60 mt-1 max-w-xs mb-4">
                    Record groceries, takeout, or any shared cost and split it automatically.
                  </p>
                  <Button onClick={() => setShowAddExpense(true)} className="font-bold bg-[#3786FB] hover:bg-[#2672e6]">
                    <Plus size={14} className="mr-1.5" /> Add Expense
                  </Button>
                </CardContent>
              </Card>
            ) : (
              <div className="space-y-6">
                {grouped.map(([monthKey, monthExpenses]) => {
                  const monthTotal = monthExpenses.reduce((s, e) => s + (e.currency === 'INR' ? e.amount : 0), 0)
                  return (
                    <div key={monthKey}>
                      <div className="flex items-center justify-between mb-2">
                        <p className="text-xs font-extrabold text-[#999CA1] dark:text-muted-foreground uppercase tracking-widest">{monthLabel(monthKey)}</p>
                        {monthTotal > 0 && <p className="text-xs font-bold text-[#021328] dark:text-foreground">{formatAmount(monthTotal, 'INR')}</p>}
                      </div>
                      <div className="space-y-2">
                        {monthExpenses.map(exp => (
                          <ExpenseRow key={exp.id} expense={exp} members={members} currentUserId={currentUserId}
                            canDelete={exp.createdBy === currentUserId || !!isAdmin} onDelete={deleteExpense} />
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

      {/* FIXED BILLS TAB */}
      {activeTab === 'bills' && (
        <div className="space-y-4">
          <div className="grid grid-cols-3 gap-2">
            {[
              { label: 'Total Bills', value: totalMonthlyCommitment > 0 ? formatAmount(totalMonthlyCommitment, 'INR') : '--', sub: recurringBills.filter(b => b.active).length + ' active bills', dark: true },
              { label: 'Your Share', value: myMonthlyShare > 0 ? formatAmount(myMonthlyShare, 'INR') : '--', sub: 'your obligation', dark: true },
              { label: 'Next Due', value: nextBillingDay != null ? ordinal(nextBillingDay) : '--', sub: 'next billing day', dark: false },
            ].map(({ label, value, sub, dark }) => (
              <div key={label} className={['p-3 rounded-[16px] shadow-sm', dark ? 'bg-[#1A202C]' : 'bg-card border border-border'].join(' ')}>
                <p className={['text-[10px] font-bold uppercase tracking-wider', dark ? 'text-[#999CA1]' : 'text-muted-foreground'].join(' ')}>{label}</p>
                <p className={['text-sm font-extrabold mt-1 leading-none', dark ? 'text-white' : 'text-[#021328] dark:text-foreground'].join(' ')}>{value}</p>
                <p className={['text-[10px] mt-0.5', dark ? 'text-[#4D515B]' : 'text-muted-foreground'].join(' ')}>{sub}</p>
              </div>
            ))}
          </div>

          {/* View Resident Breakdown button — visible to everyone */}
          {recurringBills.filter(b => b.active).length > 0 && (
            <button
              onClick={() => setShowBreakdown(true)}
              className="w-full flex items-center justify-between bg-[#f1f3ff] dark:bg-white/[0.05] hover:bg-[#e9edff] dark:hover:bg-white/[0.08] border border-[rgba(199,196,216,0.4)] rounded-[16px] px-4 py-3.5 transition-colors cursor-pointer"
            >
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-full bg-[#3525cd] flex items-center justify-center shrink-0">
                  <LayoutList size={13} className="text-white" />
                </div>
                <div className="text-left">
                  <p className="text-[#141b2b] dark:text-white text-[13px] font-bold">View Resident Breakdown</p>
                  <p className="text-[#777587] dark:text-gray-400 text-[11px] mt-0.5">See how bills are split per person</p>
                </div>
              </div>
              <ChevronRight size={16} className="text-[#3525cd] shrink-0" />
            </button>
          )}

          {isAdmin && (
            <div className="flex gap-2">
              {recurringBills.length === 0 && (
                <Button size="sm" variant="outline" className="font-semibold flex-1" onClick={() => setShowQuickSetup(true)}>
                  <Sparkles size={13} className="mr-1" /> Quick Setup
                </Button>
              )}
              {dueBills.filter(b => b.payerMode !== 'manual' && !b.isVariable).length > 0 && (
                <Button size="sm" className="font-semibold flex-1 bg-[#3786FB] hover:bg-[#2672e6] text-white" onClick={() => generateAllDueBills(currentMonthKey())}>
                  <Zap size={13} className="mr-1" /> Generate All
                </Button>
              )}
              <Button size="sm" className="font-semibold bg-[#3525cd] hover:bg-[#2b1eb5] text-white" onClick={() => setShowAddBill(true)}>
                <Plus size={13} className="mr-1" /> Add Fixed Bill
              </Button>
            </div>
          )}

          {recurringBills.filter(b => b.active).length === 0 ? (
            <Card className="border-dashed border-2">
              <CardContent className="flex flex-col items-center justify-center p-12 text-center">
                <RefreshCw size={36} className="text-muted-foreground/25 mb-3" />
                <p className="font-bold text-base text-muted-foreground">No monthly bills yet</p>
                <p className="text-sm text-muted-foreground/60 mt-1 max-w-xs mb-5">
                  Add rent, electricity, WiFi - they rotate automatically each month.
                </p>
                {isAdmin && (
                  <div className="flex gap-2">
                    <Button variant="outline" className="font-semibold" onClick={() => setShowQuickSetup(true)}>
                      <Sparkles size={14} className="mr-1.5" /> Quick Setup
                    </Button>
                    <Button className="font-bold bg-[#3525cd] hover:bg-[#2b1eb5] text-white" onClick={() => setShowAddBill(true)}>
                      <Plus size={14} className="mr-1.5" /> Add Fixed Bill
                    </Button>
                  </div>
                )}
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-3">
              {recurringBills.filter(b => b.active).map(bill => {
                const instance = billInstances.find(bi => bi.templateId === bill.id && bi.month === currentMonthKey()) ?? null
                const cfg = CATEGORY_CONFIG[bill.category]
                const isSettled = instance?.status === 'paid'
                const payerUid = bill.payerMode === 'fixed' && bill.fixedPayerUid
                  ? bill.fixedPayerUid
                  : bill.rotationQueue[bill.currentPayerIndex % bill.rotationQueue.length]
                const isYouPayer = payerUid === currentUserId
                const billParticipants = bill.participants?.length ? bill.participants : bill.rotationQueue
                const perPersonAmt = (instance?.amount ?? bill.amount)
                  ? (instance?.amount ?? bill.amount ?? 0) / Math.max(billParticipants.length, 1)
                  : null

                const isExpanded = expandedBillId === bill.id

                return (
                  <div key={bill.id} className="rounded-[20px] shadow-[0px_7px_15px_0px_rgba(0,0,0,0.10)] overflow-hidden bg-card border border-border/50">

                    {/* ── Clickable header row ─────────────────────── */}
                    <button
                      className="w-full flex items-center gap-3 p-4 text-left hover:bg-secondary/20 transition-colors cursor-pointer"
                      onClick={() => setExpandedBillId(isExpanded ? null : bill.id)}
                    >
                      <div className={['w-10 h-10 rounded-[12px] flex items-center justify-center text-xl shrink-0',
                        isSettled ? 'bg-emerald-50 dark:bg-emerald-900/20' : 'bg-[#EEF5FF] dark:bg-secondary',
                      ].join(' ')}>
                        {cfg?.emoji}
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 flex-wrap">
                          <p className="text-sm font-bold text-[#021328] dark:text-foreground">{bill.name}</p>
                          <span className={['text-[10px] font-extrabold px-2 py-0.5 rounded-full shrink-0',
                            isSettled ? 'bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-300'
                            : !instance ? 'bg-amber-50 dark:bg-amber-900/20 text-amber-700 dark:text-amber-400'
                            : instance.status === 'split_generated' ? 'bg-[#EEF5FF] text-[#3786FB]'
                            : 'bg-secondary text-muted-foreground',
                          ].join(' ')}>
                            {isSettled ? 'Fully Settled' : !instance ? 'Due' : instance.status === 'split_generated' ? 'Split Ready' : instance.status === 'overdue' ? 'Overdue' : instance.status === 'skipped' ? 'Skipped' : 'Pending'}
                          </span>
                        </div>
                        <p className="text-[11px] text-[#999CA1] dark:text-muted-foreground mt-0.5">
                          {bill.payerMode === 'rotation' ? 'Rotating · ' : bill.payerMode === 'fixed' ? 'Fixed · ' : 'Manual · '}
                          {isYouPayer ? 'You pay this month' : (members.find(m => m.uid === payerUid)?.nickname ?? '...') + ' pays'}
                          {' · '}{ordinal(bill.billingDay)} every month
                        </p>
                      </div>
                      <div className="flex items-center gap-2 shrink-0">
                        <div className="text-right">
                          {instance?.amount ? (
                            <p className="text-base font-extrabold text-[#021328] dark:text-foreground">{formatAmount(instance.amount, instance.currency)}</p>
                          ) : bill.amount ? (
                            <p className="text-base font-extrabold text-[#021328] dark:text-foreground">{formatAmount(bill.amount, bill.currency)}</p>
                          ) : (
                            <p className="text-xs text-[#999CA1] italic">Variable</p>
                          )}
                          {perPersonAmt != null && billParticipants.length > 1 && (
                            <p className="text-[10px] text-[#999CA1]">{formatAmount(perPersonAmt, instance?.currency ?? bill.currency)}/person</p>
                          )}
                        </div>
                        <ChevronDown size={14} className={['text-[#999CA1] transition-transform shrink-0', isExpanded ? 'rotate-180' : ''].join(' ')} />
                      </div>
                    </button>

                    {/* ── Expanded details ─────────────────────────── */}
                    {isExpanded && (
                      <>
                        {/* Billing details strip */}
                        <div className="grid grid-cols-3 gap-0 border-t border-border/60 bg-secondary/20">
                          <div className="px-4 py-3 border-r border-border/40">
                            <p className="text-[10px] font-bold text-[#999CA1] uppercase tracking-wider">Amount</p>
                            <p className="text-sm font-extrabold text-[#021328] dark:text-foreground mt-0.5">
                              {instance?.amount ? formatAmount(instance.amount, instance.currency)
                                : bill.amount ? formatAmount(bill.amount, bill.currency)
                                : 'Variable'}
                            </p>
                            {perPersonAmt != null && billParticipants.length > 1 && (
                              <p className="text-[10px] text-[#999CA1]">{formatAmount(perPersonAmt, instance?.currency ?? bill.currency)} each</p>
                            )}
                          </div>
                          <div className="px-4 py-3 border-r border-border/40">
                            <p className="text-[10px] font-bold text-[#999CA1] uppercase tracking-wider">Billing</p>
                            <p className="text-sm font-extrabold text-[#021328] dark:text-foreground mt-0.5">{ordinal(bill.billingDay)}</p>
                            <p className="text-[10px] text-[#999CA1]">every month</p>
                          </div>
                          <div className="px-4 py-3">
                            <p className="text-[10px] font-bold text-[#999CA1] uppercase tracking-wider">Payer</p>
                            <p className="text-sm font-extrabold text-[#021328] dark:text-foreground mt-0.5 truncate">
                              {bill.payerMode === 'fixed' ? (members.find(m => m.uid === bill.fixedPayerUid)?.nickname ?? 'Fixed') : bill.payerMode === 'rotation' ? 'Rotating' : 'Manual'}
                            </p>
                            <p className="text-[10px] text-[#999CA1]">
                              {bill.payerMode !== 'fixed' ? 'Now: ' + (members.find(m => m.uid === payerUid)?.nickname ?? '...') : bill.isVariable ? 'variable amt' : 'fixed amt'}
                            </p>
                          </div>
                        </div>

                        {/* Participants grid */}
                        {billParticipants.length > 0 && (
                          <div className={['grid border-t border-border/60', billParticipants.length === 1 ? 'grid-cols-1' : 'grid-cols-2'].join(' ')}>
                            {billParticipants.map((uid, idx) => (
                              <div key={uid} className={['flex items-center gap-2 px-4 py-2.5 bg-card',
                                idx % 2 === 0 && idx === billParticipants.length - 1 && billParticipants.length > 1 ? 'col-span-2' : '',
                                idx > 1 ? 'border-t border-border/40' : '',
                              ].join(' ')}>
                                <div className="w-6 h-6 rounded-full bg-[#EEF5FF] dark:bg-secondary flex items-center justify-center text-[10px] font-bold text-[#3786FB] shrink-0">
                                  {nick(uid).charAt(0).toUpperCase()}
                                </div>
                                <div className="flex-1 min-w-0">
                                  <p className="text-xs font-semibold text-[#021328] dark:text-foreground truncate">{nick(uid)}</p>
                                  {perPersonAmt != null && (
                                    <p className="text-[10px] text-[#999CA1]">{formatAmount(perPersonAmt, instance?.currency ?? bill.currency)}</p>
                                  )}
                                </div>
                                <span className={['text-[9px] font-extrabold px-1.5 py-0.5 rounded-full shrink-0',
                                  isSettled ? 'bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-300' : 'bg-[#EEF5FF] dark:bg-secondary text-[#999CA1]',
                                ].join(' ')}>
                                  {isSettled ? 'Settled' : 'Unpaid'}
                                </span>
                              </div>
                            ))}
                          </div>
                        )}

                        {/* Admin actions */}
                        {isAdmin && (
                          <div className="border-t border-border/60">
                            {confirmDeleteBillId === bill.id ? (
                              <div className="flex items-center justify-between px-4 py-2.5 bg-red-50 dark:bg-red-950/20">
                                <p className="text-xs font-semibold text-red-700 dark:text-red-400">Delete &ldquo;{bill.name}&rdquo;? Cannot be undone.</p>
                                <div className="flex items-center gap-3">
                                  <button
                                    onClick={() => setConfirmDeleteBillId(null)}
                                    className="text-xs font-semibold text-muted-foreground hover:text-foreground transition-colors cursor-pointer"
                                  >
                                    Cancel
                                  </button>
                                  <button
                                    onClick={async () => {
                                      setConfirmDeleteBillId(null)
                                      setExpandedBillId(null)
                                      await deleteRecurringBill(bill.id)
                                    }}
                                    className="text-xs font-bold text-red-600 hover:text-red-700 transition-colors cursor-pointer"
                                  >
                                    Yes, Delete
                                  </button>
                                </div>
                              </div>
                            ) : (
                              <div className="flex">
                                {!instance && bill.active && (
                                  <button
                                    onClick={() => {
                                      if (bill.isVariable || bill.payerMode === 'manual') setShowGenerate(true)
                                      else generateBill(bill.id)
                                    }}
                                    className="flex-1 flex items-center justify-center gap-1.5 py-2.5 text-xs font-bold text-amber-600 dark:text-amber-400 hover:bg-amber-50 dark:hover:bg-amber-950/20 transition-colors cursor-pointer"
                                  >
                                    <Play size={11} /> Generate
                                  </button>
                                )}
                                {instance?.status === 'split_generated' && (isYouPayer || isAdmin) && (
                                  <button
                                    onClick={() => markBillPaid(instance.id)}
                                    className="flex-1 flex items-center justify-center gap-1.5 py-2.5 text-xs font-bold text-[#3786FB] hover:bg-[#EEF5FF] dark:hover:bg-blue-950/20 transition-colors cursor-pointer"
                                  >
                                    <Check size={11} /> Mark Paid
                                  </button>
                                )}
                                <button
                                  onClick={() => setEditBill(bill)}
                                  className="flex items-center justify-center gap-1 px-4 py-2.5 text-xs font-semibold text-[#999CA1] hover:text-foreground hover:bg-secondary transition-colors border-l border-border/60 cursor-pointer"
                                >
                                  <Pencil size={11} /> Edit
                                </button>
                                <button
                                  onClick={() => setConfirmDeleteBillId(bill.id)}
                                  className="flex items-center justify-center px-4 py-2.5 text-red-400 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-950/20 transition-colors border-l border-border/60 cursor-pointer"
                                  title="Delete bill"
                                >
                                  <Trash2 size={11} />
                                </button>
                              </div>
                            )}
                          </div>
                        )}
                      </>
                    )}
                  </div>
                )
              })}
            </div>
          )}

          {monthCycles.filter(mc => mc.status === 'closed').length > 0 && (
            <div className="space-y-2 pt-2">
              <div className="flex items-center gap-2">
                <History size={13} className="text-muted-foreground" />
                <p className="text-xs font-bold uppercase tracking-wider text-[#999CA1] dark:text-muted-foreground">Past Months</p>
              </div>
              {monthCycles.filter(mc => mc.status === 'closed').map(mc => {
                const expanded = expandedHistory.has(mc.month)
                const grandTotal = mc.totalBillsINR + mc.totalExpensesINR
                const hasCf = mc.carryForwardOut && Object.keys(mc.carryForwardOut.balances).length > 0
                return (
                  <div key={mc.month} className="rounded-[16px] shadow-sm overflow-hidden bg-card border border-border/50">
                    <button
                      onClick={() => setExpandedHistory(s => {
                        const n = new Set(s); n.has(mc.month) ? n.delete(mc.month) : n.add(mc.month); return n
                      })}
                      className="w-full flex items-center gap-3 p-3.5 hover:bg-secondary/30 transition-colors text-left cursor-pointer">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 flex-wrap">
                          <p className="text-sm font-bold text-[#021328] dark:text-foreground">{monthLabelUtil(mc.month)}</p>
                          <span className="text-[10px] font-extrabold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full">Closed</span>
                          {hasCf && <span className="text-[10px] font-extrabold text-amber-700 bg-amber-50 px-2 py-0.5 rounded-full">Carry-fwd</span>}
                        </div>
                        <p className="text-xs text-[#999CA1] dark:text-muted-foreground mt-0.5">
                          {formatAmount(grandTotal, 'INR')} total &middot; {formatAmount(mc.totalSettledINR, 'INR')} settled
                        </p>
                      </div>
                      {expanded ? <ChevronUp size={14} className="text-[#999CA1] shrink-0" /> : <ChevronDown size={14} className="text-[#999CA1] shrink-0" />}
                    </button>
                    {expanded && (
                      <div className="px-4 pb-4 border-t border-border/40 pt-3 space-y-3">
                        <div className="grid grid-cols-3 gap-2">
                          {[['Bills', formatAmount(mc.totalBillsINR, 'INR')], ['Expenses', formatAmount(mc.totalExpensesINR, 'INR')], ['Settled', formatAmount(mc.totalSettledINR, 'INR')]].map(([l, v]) => (
                            <div key={l} className="p-2.5 rounded-[12px] bg-secondary/50">
                              <p className="text-[10px] font-bold text-[#999CA1] uppercase tracking-wider">{l}</p>
                              <p className="text-sm font-extrabold text-[#021328] dark:text-foreground mt-0.5">{v}</p>
                            </div>
                          ))}
                        </div>
                        {hasCf && (
                          <div className="p-3 rounded-[12px] border border-amber-200 bg-amber-50/50">
                            <p className="text-[11px] font-bold text-amber-700 mb-1">Carried to {monthLabelUtil(mc.carryForwardOut!.toMonth)}</p>
                            <div className="flex flex-wrap gap-x-3">
                              {Object.entries(mc.carryForwardOut!.balances).map(([uid, amt]) => (
                                <span key={uid} className={['text-xs font-semibold', amt > 0 ? 'text-emerald-600' : 'text-red-500'].join(' ')}>
                                  {nick(uid)}: {amt > 0 ? '+' : ''}{formatAmount(amt, 'INR')}
                                </span>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                )
              })}
            </div>
          )}
        </div>
      )}

      {/* Modals */}
      {showAddExpense && (
        <ExpenseModal members={members} currentUserId={currentUserId} onSave={addExpense} onClose={() => setShowAddExpense(false)} />
      )}
      {showAddBill && (
        <MonthlyBillModal members={members} currentUserId={currentUserId} onSave={_createSingle} onClose={() => setShowAddBill(false)} />
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
      {showQuickSetup && (
        <QuickSetupModal members={members} currentUserId={currentUserId}
          onSave={bulkCreateRecurringBills} onClose={() => setShowQuickSetup(false)} />
      )}
      {showGenerate && dueBills.length > 0 && (
        <GenerateBillsModal dueBills={dueBills} members={members} currentUserId={currentUserId}
          onGenerate={generateBill} onClose={() => setShowGenerate(false)} />
      )}
      {showMonthEnd && (
        <MonthEndModal
          month={currentMonthKey()}
          members={members}
          currentUserId={currentUserId}
          prevCycle={prevCycle ?? null}
          expenses={expenses}
          billInstances={billInstances}
          settlements={settlements}
          onClose={() => setShowMonthEnd(false)}
          onConfirm={async (confirmed, summaryData, cfOut) => {
            await closeMonth(currentMonthKey(), confirmed, summaryData, cfOut)
          }}
        />
      )}
      {showBreakdown && (
        <FixedBillsBreakdownModal
          members={members}
          currentUserId={currentUserId}
          recurringBills={recurringBills}
          billInstances={billInstances}
          monthKey={currentMonthKey()}
          onClose={() => setShowBreakdown(false)}
        />
      )}
    </div>
  )
}
