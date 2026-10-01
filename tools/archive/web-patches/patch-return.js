const fs = require('fs');
const filePath = 'C:/garbage/app/dashboard/expenses/page.tsx';
let txt = fs.readFileSync(filePath, 'utf8');
const hadCRLF = txt.includes('\r\n');
if (hadCRLF) txt = txt.replace(/\r\n/g, '\n');

// Find the ExpensesPage's top-level return( — indented exactly 2 spaces
const returnLine = '  return (';
const startIdx = txt.lastIndexOf('\n' + returnLine + '\n    <div className="space-y-8 max-w-3xl">');
if (startIdx === -1) { console.error('return start not found'); process.exit(1); }
const actualStart = startIdx + 1; // skip the leading \n

// The function body ends at the last `\n}` in the file
const endIdx = txt.lastIndexOf('\n}') + 2;

const NEW_RETURN = `  // Derived balance totals for the two stat cards
  const totalYouOwe = balances
    .filter(b => b.amount < 0 && b.currency === 'INR')
    .reduce((s, b) => s + Math.abs(b.amount), 0)
  const totalOwedToYou = balances
    .filter(b => b.amount > 0 && b.currency === 'INR')
    .reduce((s, b) => s + b.amount, 0)

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
          className={['flex-1 flex items-center justify-center gap-1.5 py-2.5 rounded-lg text-sm font-bold transition-all cursor-pointer',
            activeTab === 'daily' ? 'bg-[#3786FB] text-white shadow-sm' : 'text-muted-foreground hover:text-foreground',
          ].join(' ')}
        >
          Daily Splits
          {netUnsettled > 0 && activeTab !== 'daily' && <span className="w-1.5 h-1.5 bg-red-500 rounded-full shrink-0" />}
        </button>
        <button
          onClick={() => setActiveTab('bills')}
          className={['flex-1 flex items-center justify-center gap-1.5 py-2.5 rounded-lg text-sm font-bold transition-all cursor-pointer',
            activeTab === 'bills' ? 'bg-[#3786FB] text-white shadow-sm' : 'text-muted-foreground hover:text-foreground',
          ].join(' ')}
        >
          Fixed Bills
          {dueBills.length > 0 && activeTab !== 'bills' && <span className="w-1.5 h-1.5 bg-amber-500 rounded-full shrink-0" />}
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
              { label: 'Total Monthly', value: totalMonthlyCommitment > 0 ? formatAmount(totalMonthlyCommitment, 'INR') : '--', sub: recurringBills.filter(b => b.active).length + ' active', dark: true },
              { label: 'Per Person', value: totalMonthlyCommitment > 0 && members.length > 0 ? formatAmount(totalMonthlyCommitment / members.length, 'INR') : '--', sub: 'equal split', dark: true },
              { label: 'Next Due', value: nextBillingDay != null ? ordinal(nextBillingDay) : '--', sub: 'billing day', dark: false },
            ].map(({ label, value, sub, dark }) => (
              <div key={label} className={['p-3 rounded-[16px] shadow-sm', dark ? 'bg-[#1A202C]' : 'bg-card border border-border'].join(' ')}>
                <p className={['text-[10px] font-bold uppercase tracking-wider', dark ? 'text-[#999CA1]' : 'text-muted-foreground'].join(' ')}>{label}</p>
                <p className={['text-sm font-extrabold mt-1 leading-none', dark ? 'text-white' : 'text-[#021328] dark:text-foreground'].join(' ')}>{value}</p>
                <p className={['text-[10px] mt-0.5', dark ? 'text-[#4D515B]' : 'text-muted-foreground'].join(' ')}>{sub}</p>
              </div>
            ))}
          </div>

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
              <Button size="sm" variant="outline" className="font-semibold" onClick={() => setShowAddBill(true)}>
                <Plus size={13} className="mr-1" /> Add Bill
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
                    <Button className="font-bold bg-[#3786FB] hover:bg-[#2672e6] text-white" onClick={() => setShowAddBill(true)}>
                      <Plus size={14} className="mr-1.5" /> Add Bill
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

                return (
                  <div key={bill.id} className="rounded-[20px] shadow-[0px_7px_15px_0px_rgba(0,0,0,0.10)] overflow-hidden bg-card border border-border/50">
                    <div className="flex items-center gap-3 p-4">
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
                          {bill.payerMode === 'rotation' ? 'Rotating - ' : bill.payerMode === 'fixed' ? 'Fixed - ' : 'Manual - '}
                          {isYouPayer ? 'You pay this month' : (members.find(m => m.uid === payerUid)?.nickname ?? '...') + ' pays'}
                        </p>
                      </div>
                      <div className="text-right shrink-0">
                        {instance?.amount ? (
                          <p className="text-base font-extrabold text-[#021328] dark:text-foreground">{formatAmount(instance.amount, instance.currency)}</p>
                        ) : bill.amount ? (
                          <p className="text-base font-extrabold text-[#021328] dark:text-foreground">{formatAmount(bill.amount, bill.currency)}</p>
                        ) : (
                          <p className="text-xs text-[#999CA1] italic">Variable</p>
                        )}
                      </div>
                    </div>

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

                    {isAdmin && (
                      <div className="flex border-t border-border/60">
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
                        {instance?.status === 'split_generated' && isYouPayer && (
                          <button
                            onClick={() => markBillPaid(instance.id)}
                            className="flex-1 flex items-center justify-center gap-1.5 py-2.5 text-xs font-bold text-[#3786FB] hover:bg-[#EEF5FF] dark:hover:bg-blue-950/20 transition-colors cursor-pointer"
                          >
                            <Check size={11} /> Mark Paid
                          </button>
                        )}
                        <button
                          onClick={() => setEditBill(bill)}
                          className="px-4 flex items-center justify-center text-xs text-[#999CA1] hover:bg-secondary transition-colors border-l border-border/60 cursor-pointer"
                        >
                          <Pencil size={11} />
                        </button>
                      </div>
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
    </div>
  )
}`;

txt = txt.slice(0, actualStart) + NEW_RETURN + (hadCRLF ? '\r\n' : '\n');
if (hadCRLF) txt = txt.replace(/\n/g, '\r\n');
fs.writeFileSync(filePath, txt, 'utf8');
console.log('Return patched. Lines:', txt.split('\n').length);
