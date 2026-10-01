/**
 * Replaces MonthlyBillModal with the Figma-styled version.
 * Run: node C:/garbage/patch-modal.js
 */
const fs = require('fs');
const filePath = 'C:/garbage/app/dashboard/expenses/page.tsx';
let txt = fs.readFileSync(filePath, 'utf8');

// Normalize to LF for processing
const hadCRLF = txt.includes('\r\n');
if (hadCRLF) txt = txt.replace(/\r\n/g, '\n');

const OLD_START = `function MonthlyBillModal({
  members, currentUserId, initial, onSave, onClose,
}: {
  members: { uid: string; nickname: string }[]
  currentUserId: string
  initial?: RecurringBill
  onSave: (data: Omit<RecurringBill, 'id' | 'createdAt' | 'currentPayerIndex' | 'lastGeneratedMonth'>) => Promise<void>
  onClose: () => void
}) {`;

const startIdx = txt.indexOf(OLD_START);
if (startIdx === -1) { console.error('OLD_START not found'); process.exit(1); }

// Find "// ── Generate Bills Modal" comment which immediately follows
const endMarker = '\n// ──';
const endIdx = txt.indexOf(endMarker, startIdx);
if (endIdx === -1) { console.error('endMarker not found'); process.exit(1); }

const NEW_MODAL = `function MonthlyBillModal({
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
    'maid', 'rent', 'maintenance', 'electricity', 'water', 'internet', 'gas', 'grocery', 'milk',
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
                Habitiq will ask you to verify the actual amount before the due date — useful for bills like electricity or water that vary monthly.
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
}`;

txt = txt.slice(0, startIdx) + NEW_MODAL + txt.slice(endIdx);

// Restore CRLF if original had it
if (hadCRLF) txt = txt.replace(/\n/g, '\r\n');

fs.writeFileSync(filePath, txt, 'utf8');
console.log('Modal patched. Lines:', txt.split('\n').length);
