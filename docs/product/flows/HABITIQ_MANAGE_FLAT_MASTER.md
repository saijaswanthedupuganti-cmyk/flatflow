# HABITIQ — MANAGE FLAT MASTER SPEC

Drop into Cursor: **Read this first. Inspect the APK. Preserve web/native business logic. Fix the mobile experience.**

This is **not** a request to rebuild Tasks, Expenses, rotation, settlements, bills, or Firestore.

Web + existing Kotlin repositories are the **source of truth**. Your job is the product layer:

> Preserve logic. Simplify interaction. Reduce hierarchy. Surface important states. Hide complexity until needed.

Do **not** design around dumping every field on screen. Design around:

| User question | Surface |
|---|---|
| What do I have to do? | Tasks → My Tasks |
| What do I owe? | Expenses → Balance |
| What's happening? | Home dashboard |
| What can I control? | Manage Flat (admin contextual) |

---

## 00 — HARD RULES

1. Do not reinterpret `RotationEngine`, `settlementUtils` / `SettlementUtils`, expense split math, bill `paidBy` vs `collectorId`, month close, swap transfer, or OOS skip.
2. Web mapping (native-app architecture):  
   `rotationEngine.ts → TasksRepository`  
   `expenseUtils.ts → ExpensesRepository`  
   `settlementUtils.ts → SettlementsRepository`
3. Firestore collections stay as they are. Additive UI only.
4. Do not bring back the old Reliability Score dashboard card.
5. Do not invent Google Maps / Discovery commute here.
6. Members must not get Create / Edit / Delete / Override task controls (Firestore already forbids member task create/delete).
7. An overdue task stays on the responsible member until completed — never silently reassigned away.
8. Everyone-OOS must not crash (`RotationEngine.getNextAssignee` returns null → task pauses).
9. Work in the order in §42. Compile after each unit.

**Implemented in APK (23 Aug 2026, same day):** Manage tab is **Manage Flat** hub (Tasks / Expenses cards with my-responsibility summaries). Home no longer has admin Tasks/Expenses toggle. `+` Add Task / Add Expense / Bills land in Manage Flat, not Home. Members default to **My Tasks**. Expenses opens **balance-first** with Settle / Received. Business logic still `RotationEngine` / `SettlementUtils` / existing repositories — not rewritten.

Still later in §42: card expansion instead of full detail page, bill user-language, buried month close, analytics last.

---

## 01 — CURRENT APK AUDIT (23 Aug 2026)

Inspected live. Do not assume a clean slate.

### Navigation (today)

```text
HOME | DISCOVER | (+) | TASKS | PROFILE
```

Native architecture wanted:

```text
Dashboard | Flat Management | Discover | Profile
```

**Gap:** Tasks is a top-level tab. Expenses live on **admin Home** via `AdminHomeMode` (Manage Tasks / Expenses toggle) **and** `ExpensesScreen` / `BillsScreen` overlays. Members see a thin expenses card on Home, not a first-class Manage Flat.

### Logic that already exists (reuse)

| Capability | Where |
|---|---|
| Complete + rotate | `RotationEngine.completeTask` via `TasksRepository` / `FlatViewModel.completeTask` |
| Frequencies daily/weekly/fortnightly/monthly | `RotationEngine` next due |
| Overdue | `effectiveTaskStatus` / `isTaskOverdue` |
| OOS skip | `getNextAssignee` skips `out_of_station` |
| Swaps pending/accept/decline + transfer | `FlatViewModel.respondToSwap` |
| Going away | `GoingAwayScreen` |
| Create task (admin) | `createTask` + create-task wizard overlays |
| Equal/custom expense | `addExpense` |
| Bidirectional settle | `recordManualSettlement` / `recordMarkReceived` |
| Recurring bills, generate, mark paid, collector | `BillsScreen` + `BillsRepository` |
| Month close | `closeMonth` + `MonthCycle` |
| Net balances | `computeMonthNetBalances` |

### UI that is incomplete vs this spec

| Desired | Current |
|---|---|
| Manage Flat hub (Tasks vs Expenses only) | Missing — Tasks tab dumps `TasksScreen`; expenses split across Home admin toggle |
| My Tasks default for members | Filter chips All / Mine / Overdue, default **All** |
| Compact card → expand details | Card opens **full** `TaskDetailScreen` |
| Due date on card | Missing on list card |
| Balance-first expenses | List of expenses first; balances as a text list; **no Settle on that list** |
| Monthly bills language Upcoming/Paid | Technical Bills / Instances / Settle tabs |
| Close month buried | Exists in bills flow, easy to miss or over-expose |
| Home = “what’s happening” | Admin Home is a second management dashboard |

---

## 02 — TARGET NAVIGATION

Keep the center **+** button.

```text
HOME          — dashboard (today, activity). Not the management console.
MANAGE        — umbrella: choose Tasks or Expenses
DISCOVER      — discovery system (separate spec)
PROFILE       — later master spec
```

`AppTab.TASKS` may keep the enum name for saveable state; **label = Manage**.

When user taps Manage:

```text
Manage Flat
Keep your shared life organised.

[ Tasks     3 assigned · 1 overdue ]
[ Expenses  You owe ₹850           ]
```

Then drill in. Back returns to this hub.

**+ on Manage**

- Hub: Add Task (admin) / Add Expense / Bills
- Inside Tasks: Add Task if admin
- Inside Expenses: Add Expense
- Discover tab stays Discovery-create (existing)

Do not put Close Month on this hub.

---

## 03 — HOME VS MANAGE

**Home** answers: What’s happening in my flat today?

Show: greeting, today’s tasks (read + complete if mine), pending join/swap counts, recent activity, discover teaser.

**Do not** keep `AdminManageToggle` (Manage Tasks | Expenses) on Home. That duplicates Manage Flat and overloads Dashboard.

**Manage Flat** answers: What do I need to do / pay / control?

---

## 04 — TASKS UX

### Member default: My Tasks

Question: “What do I need to do?”

Toggle: `[ My Tasks ] [ All Tasks ]`  
Overdue can be a chip or sorted to top of My Tasks — do not make Overdue the default home.

### Card hierarchy

1. Name  
2. When (`Due today` / `Was due yesterday`)  
3. Who (`YOU` badge vs assignee name)  
4. Status (Overdue quieter than screaming red)  
5. Supporting: next up, frequency — after tap

**Complete:** one tap on card if mine. No “Are you sure?”. Button shows Completing… on failure retry.

**Details:** prefer expand or bottom sheet. Existing `TaskDetailScreen` is allowed until a sheet exists; do not add more stack layers (Rotation page, Swap confirmation page).

**Rotation:** NOW / NEXT / THEN + OOS marker. Never `rotationQueue[0]`.

**OOS:** “I’m away” from Tasks (`GoingAwayScreen` already). Copy: tasks skip you temporarily. Re-enter same queue position (engine already does not send OOS members to the back).

**Swaps:** from the task (“Can’t do this?”), not a Swaps module. Keep `SwapReviewSheet`.

**Admin Flat view:** All Tasks + Create (FAB or +). Override/Edit/Delete behind ⋯ on detail, not on every card.

**Completed:** quieter section or filtered out of My Tasks (current filter already hides completed in Mine).

**Empty**

- Member: No tasks assigned to you.  
- Admin: No tasks yet. [Create Task]

**Analytics:** P2. Do not rebuild Reliability Score.

---

## 05 — EXPENSES UX

### Open with position, not a ledger

```text
You owe ₹850 to 2 people     OR  You're owed ₹1,200     OR  ✓ All settled
This month · Daily splits · Monthly bills
```

Tap balance → expand per person + contributing expenses + **Settle** / **Mark received**.

Person filter: tap a name → transactions involving them (P1 if not in first pass).

### Daily splits

Add Expense: what / amount / paid by / equal vs custom (custom hidden until selected). Finish in seconds.

List: today/recent. Tap → split breakdown. Edit/delete per existing creator/admin rules.

### Monthly bills (separate mental model)

Not a grocery row.

User language: Upcoming / Shares ready / Paid / Skipped — **never** `split_generated`.

Show payer vs collector separately (`paidBy` vs `collectorId`).

Collection checkmarks: collector marks others; member self-mark; payer does not collect from self — **preserve BillsRepository permissions**.

### Month close (admin, not dominant)

Overflow or bottom: Review & Close. Warn if outstanding. Port `closeMonth` as-is.

### Activity

Secondary tab or footer. Not above balances.

---

## 06 — PERMISSIONS (DO NOT REINVENT)

| Action | Admin | Member |
|---|---|---|
| Create/edit/delete task | Yes | No |
| Override assignment | Yes | No |
| Complete assigned | Yes | Yes |
| Swap request / respond | Yes | Yes (own) |
| OOS | Yes | Yes |
| Add expense | Yes | Yes |
| Edit/delete expense | Creator or admin | Creator (own) |
| Recurring bill CRUD | Yes | No |
| Collect marks | collector / rules | self where allowed |
| Close month | Yes | No |
| Manage members / vacancy | Manage Flat settings in Profile today | No |

---

## 07 — FIREBASE / DATA (NO NEW COLLECTIONS FOR THIS UX)

Use existing: `tasks`, `expenses`, `settlements`, `recurringBills`, `billInstances`, `monthCycles`, `members`, `swapRequests`, `activityLog`.

Do not query member-only collections from Discovery. Do not put reliabilityScore on Discovery.

---

## 08 — IMPLEMENTATION ORDER (FORCE THIS)

1. Manage Flat hub + nav label  
2. Tasks — My Tasks default  
3. Complete on card + due/overdue copy  
4. Details (existing screen OK)  
5. OOS entry from Tasks  
6. Swap sheet  
7. Admin All Tasks + create  
8. Expenses overview (balance first)  
9. Expand balances  
10. Add Expense  
11. Expense details  
12. Settle / mark received  
13. Monthly Bills (existing BillsScreen, humanize labels if touching)  
14. Collection  
15. Month close (buried)  
16. Activity  
17. Analytics (last)

---

## 09 — ACCEPTANCE (PHONE TEST)

### Tasks

- [ ] Member opens Manage → Tasks and sees **their** tasks first  
- [ ] Complete in one tap; queue advances  
- [ ] Overdue is obvious without panic UI  
- [ ] I’m away / OOS reachable from Tasks  
- [ ] Swap request + accept/decline  
- [ ] Member cannot create/delete tasks  
- [ ] Admin can create task  

### Expenses

- [ ] Balance readable in &lt;3 seconds  
- [ ] Add expense quickly  
- [ ] Settle or mark received  
- [ ] Monthly bills reachable without being the first screen  

### Admin / Home

- [ ] Home is not a second expense console  
- [ ] Close month not on the hub  
- [ ] Tasks/expenses/settlements still match web after UI change  

### Regression

- [ ] Discover unchanged  
- [ ] Auth / join / invite unchanged  

---

## 10 — CODE MAP

| Piece | Path |
|---|---|
| Nav | `ui/AppShell.kt` |
| Shell wiring | `HabitiqApp.kt` |
| Hub (new) | `ui/ManageFlatHub.kt` |
| Tasks list | `ui/TasksScreen.kt` |
| Task detail | `ui/figma/TaskDetailScreen.kt` |
| Create task | `ui/figma/CreateTaskScreens.kt` |
| OOS | `ui/figma/GoingAwayScreen.kt` |
| Swaps | `SwapReviewSheet` in `TasksScreen.kt` |
| Expenses | `ui/ExpensesScreen.kt` |
| Bills | `ui/BillsScreen.kt` |
| Rotation | `apps/android/app/src/main/kotlin/habitiq/app/lib/RotationEngine.kt` |
| Settlements math | `apps/android/app/src/main/kotlin/habitiq/app/lib/SettlementUtils.kt` |
| Home (must stay dashboard) | `ui/FigmaHomeScreen.kt` |

---

## 11 — FINAL LINE FOR THE AGENT

> Do not design the application around what data exists. Design the interface around what the user needs to accomplish. The system already can do this. Make a normal person understand how to do it immediately.
