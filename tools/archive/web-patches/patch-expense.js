/**
 * Replaces ExpenseModal and ExpenseRow with Splitwise-style redesigns.
 * Run: node C:/garbage/patch-expense.js
 */
const fs = require('fs');
const filePath = 'C:/garbage/app/dashboard/expenses/page.tsx';
let txt = fs.readFileSync(filePath, 'utf8');
const hadCRLF = txt.includes('\r\n');
if (hadCRLF) txt = txt.replace(/\r\n/g, '\n');

// ─── 1. Replace ExpenseModal ──────────────────────────────────────────────────
const MODAL_START = 'function ExpenseModal({\n  members, currentUserId, initial, onSave, onClose,\n}: {';
const MODAL_END   = '\n// ── Settle Up Modal';
const ms = txt.indexOf(MODAL_START);
if (ms === -1) { console.error('ExpenseModal start not found'); process.exit(1); }
const me = txt.indexOf(MODAL_END, ms);
if (me === -1) { console.error('ExpenseModal end not found'); process.exit(1); }

const NEW_MODAL = `function ExpenseModal({
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
                      {form.deferToNextMonth ? 'Collected with next month\'s bills' : 'Collect from flatmates right away'}
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
}`;

txt = txt.slice(0, ms) + NEW_MODAL + txt.slice(me);

// ─── 2. Replace ExpenseRow ────────────────────────────────────────────────────
const ROW_START = 'function ExpenseRow({ expense, members, currentUserId, canDelete, onDelete }: {';
const ROW_END   = '\n// ── Monthly Bill Form Modal';
const rs = txt.indexOf(ROW_START);
if (rs === -1) { console.error('ExpenseRow start not found'); process.exit(1); }
const re = txt.indexOf(ROW_END, rs);
if (re === -1) { console.error('ExpenseRow end not found'); process.exit(1); }

const NEW_ROW = `function ExpenseRow({ expense, members, currentUserId, canDelete, onDelete }: {
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
}`;

txt = txt.slice(0, rs) + NEW_ROW + txt.slice(re);

// ─── Write file ───────────────────────────────────────────────────────────────
if (hadCRLF) txt = txt.replace(/\n/g, '\r\n');
fs.writeFileSync(filePath, txt, 'utf8');
console.log('Patched. Lines:', txt.split('\n').length);
