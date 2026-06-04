import { create } from 'zustand'

// Firestore rejects undefined field values — strip them before any write
function fs<T extends object>(obj: T): T {
  return Object.fromEntries(Object.entries(obj).filter(([, v]) => v !== undefined)) as T
}
import { completeTask, getNextAssignee } from '../lib/rotationEngine'
import { db, hasKeys } from '@/lib/firebase'
import { useAuthStore } from './useAuthStore'
import {
  collection, doc, onSnapshot, updateDoc, setDoc, deleteDoc, getDoc, getDocs,
  query, orderBy, limit,
} from 'firebase/firestore'
import {
  leaveFlatService,
  kickMemberService,
  transferAdminService,
  deleteEntireFlatService,
  updateFlatName as updateFlatNameService,
  approveJoinRequestService,
  rejectJoinRequestService,
  setFlatJoinMode as setFlatJoinModeService,
} from '@/lib/flatService'

export interface Member {
  uid: string
  nickname: string
  role: 'admin' | 'member'
  status: 'available' | 'busy' | 'out_of_station' | 'inactive'
  outOfStationDates?: { start: Date; end: Date }
  reliabilityScore: number
  joinedAt: Date
}

export interface Task {
  taskId: string
  name: string
  type: string
  priority: 'high' | 'medium' | 'low'
  frequency: 'daily' | 'weekly' | 'monthly' | 'custom' | 'one_time'
  currentAssignedUserId: string
  queueOrder: string[]
  status: 'pending' | 'completed' | 'overdue' | 'paused'
  dueDate: string
  lastCompletedAt: string
  groupId?: string    // shared ID for all jobs in the same group task
  groupName?: string  // display name of the parent group (e.g. "Sunday Cleaning")
}

export interface Activity {
  id: string
  timestamp: string
  userId: string
  action: 'completed_task' | 'skipped_task' | 'status_change' | 'overdue_alert' | 'transferred_task' | 'system_override' | 'swap_requested' | 'swap_resolved' | 'task_created' | 'task_deleted' | 'task_edited' | 'returned_early' | 'expense_added' | 'expense_deleted' | 'settlement_added' | 'bill_generated' | 'bill_paid' | 'bill_skipped'
  details: string
  /** Admin can soft-hide entries without deleting them */
  hidden?: boolean
}

export interface JoinRequest {
  id: string
  uid: string
  nickname: string
  email: string
  status: 'pending' | 'approved' | 'rejected'
  createdAt: string
}

export interface SwapRequest {
  id: string
  taskId: string
  fromUserId: string
  toUserId: string
  status: 'pending' | 'accepted' | 'rejected'
  read: boolean
  createdAt: string
  isAutomatic?: boolean
  isOOSRequest?: boolean   // part of a "going out of station" batch
}

export type ExpenseCategory =
  | 'rent' | 'electricity' | 'water' | 'internet' | 'gas'
  | 'maid' | 'grocery' | 'milk' | 'ac'
  | 'maintenance' | 'food' | 'household' | 'other'

export type Currency = 'INR' | 'USD' | 'EUR' | 'GBP' | 'AED' | 'SGD' | 'AUD'

export interface Expense {
  id: string
  description: string
  amount: number
  currency: Currency
  paidBy: string
  splitAmong: string[]
  splitType: 'equal' | 'percent' | 'custom'
  splits: Record<string, number>  // userId → their share amount
  category: ExpenseCategory
  date: string              // yyyy-MM-dd
  month?: string            // yyyy-MM — which billing cycle this belongs to
  note?: string
  deferToNextMonth?: boolean
  createdAt: string
  createdBy: string
}

export interface RecurringBill {
  id: string
  name: string
  category: ExpenseCategory
  amount: number | null    // null = variable (e.g. electricity)
  currency: Currency
  billingDay: number       // 1–28, day of month to generate
  rotationQueue: string[]  // payer rotation — who physically pays landlord/utility
  currentPayerIndex: number
  participants?: string[]  // who splits the cost — defaults to rotationQueue if unset
  isVariable: boolean
  active: boolean
  createdBy: string
  createdAt: string
  lastGeneratedMonth: string  // 'yyyy-MM' or '' — prevents double-generation
}

// ── Bill Instance — transactional layer for recurring bills ────────────────────
// One instance is created per template per month when the bill is generated.
// Templates (RecurringBill) are configurations. Instances are the transactions.

export type BillInstanceStatus =
  | 'pending'          // variable bill: billing day arrived, amount not yet entered
  | 'split_generated'  // amount confirmed, splits computed, members notified
  | 'paid'             // payer confirmed payment to landlord / utility company
  | 'overdue'          // past due date and not yet paid
  | 'skipped'          // admin explicitly skipped this bill for this month

export interface BillInstance {
  id: string
  templateId: string       // RecurringBill.id
  month: string            // yyyy-MM

  // Snapshot from template at generation time
  name: string
  category: ExpenseCategory
  currency: Currency

  amount: number | null    // null for variable until entered

  paidBy: string           // uid of who pays upfront (from payer rotation)
  participants: string[]   // uid[] of who splits this bill
  splits: Record<string, number> | null  // null until split_generated

  status: BillInstanceStatus

  dueDate: string          // yyyy-MM-dd
  paidAt: string | null    // when payer confirmed payment
  skippedReason: string | null
  generatedAt: string
  generatedBy: string
}

export interface Settlement {
  id: string
  fromUserId: string        // who paid
  toUserId: string          // who received
  amount: number
  currency: Currency
  note?: string
  date: string
  month?: string            // yyyy-MM — which billing cycle this covers
  type?: 'immediate' | 'month_end' | 'carry_forward' | 'rent_adjustment'
  createdAt: string
}

// ── Month Cycle — the backbone of the monthly lifecycle ───────────────────────
// One document per flat per month. Created when the month is first accessed.
// Closed by admin at month-end after all settlements are confirmed.

export type MonthCycleStatus = 'open' | 'closing' | 'closed'

export interface MonthCycle {
  id: string                // flatId_YYYY-MM
  flatId: string
  month: string             // YYYY-MM
  status: MonthCycleStatus

  openedAt: string
  closedAt: string | null

  // Aggregates written on close
  totalBillsINR: number
  totalExpensesINR: number
  totalSettledINR: number
  netBalances: Record<string, number>  // uid → net (positive = owed to uid)

  // Carry-forward links
  carryForwardIn:  { fromMonth: string; balances: Record<string, number> } | null
  carryForwardOut: { toMonth: string;   balances: Record<string, number> } | null

  createdBy: string
}

interface FlatState {
  flatId: string | null
  name: string | null
  joinMode: 'auto' | 'approval'
  members: Member[]
  tasks: Task[]
  activityLog: Activity[]
  swapRequests: SwapRequest[]
  joinRequests: JoinRequest[]
  expenses: Expense[]
  settlements: Settlement[]
  recurringBills: RecurringBill[]
  billInstances: BillInstance[]
  monthCycles: MonthCycle[]
  isSynced: boolean

  // Initialization
  initFirestoreListeners: (flatId: string) => void
  resetFlatData: () => void
  /** Mock mode: add a new member directly to local state */
  addMemberMock: (uid: string, nickname: string, email: string, role: 'admin' | 'member') => void

  // Logic Actions
  markTaskCompleted: (taskId: string, userId: string, completionDate?: string) => Promise<void>
  changeMemberStatus: (userId: string, status: Member['status']) => Promise<void>
  returnEarly: (userId: string) => Promise<void>
  checkOverdueTasks: () => Promise<void>
  transferTask: (taskId: string, fromUserId: string, toUserId: string) => Promise<void>
  manuallyAssignTask: (taskId: string, targetUserId: string, adminId: string) => Promise<void>
  createTask: (taskData: Omit<Task, 'taskId' | 'status' | 'lastCompletedAt'> & { lastCompletedAt?: string }, adminId: string) => Promise<void>
  editTask: (taskId: string, changes: Partial<Pick<Task, 'name' | 'type' | 'priority' | 'frequency' | 'dueDate'>>, adminId: string) => Promise<void>
  deleteTask: (taskId: string, adminId: string) => Promise<void>
  createSwapRequest: (taskId: string, fromUserId: string, toUserId: string, isAutomatic?: boolean, isOOSRequest?: boolean) => Promise<void>
  addExpense: (data: Omit<Expense, 'id' | 'createdAt'>) => Promise<void>
  deleteExpense: (expenseId: string) => Promise<void>
  addSettlement: (data: Omit<Settlement, 'id' | 'createdAt'>) => Promise<void>
  deleteSettlement: (settlementId: string) => Promise<void>
  createRecurringBill: (data: Omit<RecurringBill, 'id' | 'createdAt' | 'currentPayerIndex' | 'lastGeneratedMonth'>) => Promise<void>
  updateRecurringBill: (billId: string, changes: Partial<Pick<RecurringBill, 'name' | 'amount' | 'billingDay' | 'rotationQueue' | 'participants' | 'isVariable' | 'active' | 'currency'>>) => Promise<void>
  deleteRecurringBill: (billId: string) => Promise<void>
  generateBill: (billId: string, amount?: number) => Promise<void>
  confirmBillAmount: (instanceId: string, amount: number) => Promise<void>
  editBillInstanceAmount: (instanceId: string, amount: number) => Promise<void>
  markBillPaid: (instanceId: string) => Promise<void>
  skipBillInstance: (instanceId: string, reason?: string) => Promise<void>
  deleteBillInstance: (instanceId: string) => Promise<void>
  closeMonth: (
    month: string,
    confirmedSettlements: { fromUserId: string; toUserId: string; amount: number; type: 'month_end' | 'rent_adjustment'; note?: string }[],
    summary: { totalBillsINR: number; totalExpensesINR: number; totalSettledINR: number; netBalances: Record<string, number> },
    carryForwardOut: Record<string, number> | null,
  ) => Promise<void>
  resolveSwapRequest: (requestId: string, status: 'accepted' | 'rejected') => Promise<void>
  markSwapRequestRead: (requestId: string) => Promise<void>
  addActivity: (activity: Omit<Activity, 'id' | 'timestamp'>) => Promise<void>
  toggleActivityHidden: (id: string) => Promise<void>

  // Flat Settings
  renameFlatAction: (newName: string) => Promise<void>
  setJoinMode: (mode: 'auto' | 'approval') => Promise<void>

  // Join Requests (approval mode)
  approveJoinRequest: (requestId: string) => Promise<void>
  rejectJoinRequest: (requestId: string) => Promise<void>

  // Membership Management
  leaveFlat: (uid: string) => Promise<{ nextFlatId: string | null }>
  kickMember: (targetUid: string, adminId: string) => Promise<void>
  transferAdmin: (newAdminUid: string, currentAdminUid: string) => Promise<void>
  deleteFlat: (adminUid: string) => Promise<void>
}

const yesterday = new Date();
yesterday.setDate(yesterday.getDate() - 1);
const tomorrow = new Date();
tomorrow.setDate(tomorrow.getDate() + 1);

// FAKE DATA (Fallback if no Firebase)
const MOCK_MEMBERS: Member[] = [
  { uid: 'u1', nickname: 'Sai', role: 'admin', status: 'available', reliabilityScore: 95, joinedAt: new Date() },
  { uid: 'u2', nickname: 'Rahul', role: 'member', status: 'available', reliabilityScore: 82, joinedAt: new Date() },
  { uid: 'u3', nickname: 'Arjun', role: 'member', status: 'available', reliabilityScore: 90, joinedAt: new Date() },
  { uid: 'u4', nickname: 'Kiran', role: 'member', status: 'out_of_station', reliabilityScore: 88, joinedAt: new Date() },
]

const MOCK_TASKS: Task[] = [
  { taskId: 't1', name: 'Garbage Duty', type: 'garbage', priority: 'high', frequency: 'daily', currentAssignedUserId: 'u2', queueOrder: ['u1', 'u2', 'u3', 'u4'], status: 'pending', dueDate: tomorrow.toISOString(), lastCompletedAt: yesterday.toISOString() },
  { taskId: 't2', name: 'Bathroom Cleaning', type: 'cleaning', priority: 'medium', frequency: 'weekly', currentAssignedUserId: 'u1', queueOrder: ['u1', 'u3', 'u4', 'u2'], status: 'overdue', dueDate: yesterday.toISOString(), lastCompletedAt: new Date(Date.now() - 86400000 * 8).toISOString() },
  { taskId: 't3', name: 'Kitchen Cleaning', type: 'kitchen', priority: 'medium', frequency: 'weekly', currentAssignedUserId: 'u2', queueOrder: ['u2', 'u3', 'u4', 'u1'], status: 'pending', dueDate: tomorrow.toISOString(), lastCompletedAt: new Date().toISOString() },
  { taskId: 't4', name: 'Grocery Run', type: 'groceries', priority: 'low', frequency: 'weekly', currentAssignedUserId: 'u3', queueOrder: ['u3', 'u4', 'u1', 'u2'], status: 'pending', dueDate: tomorrow.toISOString(), lastCompletedAt: yesterday.toISOString() },
]

const MOCK_EXPENSES: Expense[] = [
  {
    // Rent logged as ad-hoc expense (not yet migrated to a BillInstance)
    id: 'ex1', description: 'June Rent', amount: 32000, currency: 'INR',
    paidBy: 'u1', splitAmong: ['u1', 'u2', 'u3', 'u4'], splitType: 'equal',
    splits: { u1: 8000, u2: 8000, u3: 8000, u4: 8000 },
    category: 'rent', date: '2026-06-01', month: '2026-06',
    createdAt: new Date(2026, 5, 1).toISOString(), createdBy: 'u1',
  },
  {
    id: 'ex2', description: 'Groceries run', amount: 1800, currency: 'INR',
    paidBy: 'u2', splitAmong: ['u1', 'u2', 'u3', 'u4'], splitType: 'equal',
    splits: { u1: 450, u2: 450, u3: 450, u4: 450 },
    category: 'grocery', date: '2026-06-05', month: '2026-06',
    createdAt: new Date(2026, 5, 5).toISOString(), createdBy: 'u2',
  },
  // WiFi (ex3) removed — now represented by BillInstance bi1 to avoid double-counting
]

const MOCK_RECURRING_BILLS: RecurringBill[] = [
  {
    id: 'rb1', name: 'Room Rent', category: 'rent', amount: 20000, currency: 'INR',
    billingDay: 1, rotationQueue: ['u1', 'u2', 'u3', 'u4'], currentPayerIndex: 0,
    participants: ['u1', 'u2', 'u3', 'u4'],
    isVariable: false, active: true, createdBy: 'u1',
    createdAt: new Date(2026, 5, 1).toISOString(), lastGeneratedMonth: '2026-05',
  },
  {
    // WiFi already generated for June — shows split_generated instance
    id: 'rb2', name: 'WiFi', category: 'internet', amount: 800, currency: 'INR',
    billingDay: 1, rotationQueue: ['u1', 'u2', 'u3', 'u4'], currentPayerIndex: 2,
    participants: ['u1', 'u2', 'u3', 'u4'],
    isVariable: false, active: true, createdBy: 'u1',
    createdAt: new Date(2026, 5, 1).toISOString(), lastGeneratedMonth: '2026-06',
  },
  {
    id: 'rb3', name: 'Water Bill', category: 'water', amount: 600, currency: 'INR',
    billingDay: 1, rotationQueue: ['u1', 'u2', 'u3', 'u4'], currentPayerIndex: 2,
    participants: ['u1', 'u2', 'u3', 'u4'],
    isVariable: false, active: true, createdBy: 'u1',
    createdAt: new Date(2026, 5, 1).toISOString(), lastGeneratedMonth: '2026-05',
  },
  {
    // Electricity variable: generated for June but amount pending
    id: 'rb4', name: 'Electricity', category: 'electricity', amount: null, currency: 'INR',
    billingDay: 10, rotationQueue: ['u1', 'u2', 'u3', 'u4'], currentPayerIndex: 0,
    participants: ['u1', 'u2', 'u3', 'u4'],
    isVariable: true, active: true, createdBy: 'u1',
    createdAt: new Date(2026, 5, 1).toISOString(), lastGeneratedMonth: '2026-06',
  },
]

const MOCK_MONTH_CYCLES: MonthCycle[] = [
  {
    // May 2026 — closed, with carry-forward into June
    id: 'FLAT-1234_2026-05',
    flatId: 'FLAT-1234',
    month: '2026-05',
    status: 'closed',
    openedAt: new Date(2026, 4, 1).toISOString(),
    closedAt: new Date(2026, 5, 1).toISOString(),
    totalBillsINR: 21400,
    totalExpensesINR: 3200,
    totalSettledINR: 24150,
    netBalances: { u1: 0, u2: 0, u3: 0, u4: 0 },
    carryForwardIn: null,
    carryForwardOut: {
      toMonth: '2026-06',
      balances: { u1: 450, u2: -300, u3: -150, u4: 0 },
    },
    createdBy: 'u1',
  },
]

const MOCK_BILL_INSTANCES: BillInstance[] = [
  {
    // WiFi — June 2026, splits computed, Arjun paying this month
    id: 'bi1', templateId: 'rb2', month: '2026-06',
    name: 'WiFi', category: 'internet', currency: 'INR',
    amount: 800,
    paidBy: 'u3',
    participants: ['u1', 'u2', 'u3', 'u4'],
    splits: { u1: 200, u2: 200, u3: 200, u4: 200 },
    status: 'split_generated',
    dueDate: '2026-06-01',
    paidAt: null, skippedReason: null,
    generatedAt: new Date(2026, 5, 1).toISOString(),
    generatedBy: 'u1',
  },
  {
    // Electricity — June 2026, variable: amount not yet entered
    id: 'bi2', templateId: 'rb4', month: '2026-06',
    name: 'Electricity', category: 'electricity', currency: 'INR',
    amount: null,
    paidBy: 'u1',
    participants: ['u1', 'u2', 'u3', 'u4'],
    splits: null,
    status: 'pending',
    dueDate: '2026-06-10',
    paidAt: null, skippedReason: null,
    generatedAt: new Date(2026, 5, 10).toISOString(),
    generatedBy: 'u1',
  },
]

// ── Pure helper: compute per-person bill splits with mid-month proration ──────
function computeBillSplits(
  amount: number,
  participants: string[],
  members: Member[],
  today: Date,
): Record<string, number> {
  const yr = today.getFullYear()
  const mo = today.getMonth() + 1
  const monthStart = new Date(yr, mo - 1, 1)
  const totalDays = new Date(yr, mo, 0).getDate()

  const weights: Record<string, number> = {}
  for (const uid of participants) {
    const member = members.find(m => m.uid === uid)
    if (!member) { weights[uid] = 1; continue }
    const joined = member.joinedAt instanceof Date
      ? member.joinedAt
      : new Date(member.joinedAt as unknown as string)
    weights[uid] = (!joined || isNaN(joined.getTime()) || joined <= monthStart)
      ? 1
      : Math.max(0, (totalDays - joined.getDate() + 1) / totalDays)
  }

  const totalWeight = Object.values(weights).reduce((s, w) => s + w, 0)
  return Object.fromEntries(
    participants.map(uid => {
      const w = weights[uid] ?? 1
      const share = totalWeight > 0
        ? Math.round((amount * w / totalWeight) * 100) / 100
        : Math.round((amount / participants.length) * 100) / 100
      return [uid, share]
    })
  )
}

// Module-level unsubscribers — persists across state resets
let unsubscribers: (() => void)[] = []

export const useFlatStore = create<FlatState>((set, get) => ({
  flatId: hasKeys ? null : 'FLAT-1234',
  name: hasKeys ? null : 'Bachelor Pad',
  joinMode: 'auto',
  members: hasKeys ? [] : MOCK_MEMBERS,
  tasks: hasKeys ? [] : MOCK_TASKS,
  activityLog: [],
  swapRequests: [],
  joinRequests: [],
  expenses: hasKeys ? [] : MOCK_EXPENSES,
  settlements: [],
  recurringBills: hasKeys ? [] : MOCK_RECURRING_BILLS,
  billInstances: hasKeys ? [] : MOCK_BILL_INSTANCES,
  monthCycles: hasKeys ? [] : MOCK_MONTH_CYCLES,
  isSynced: false,

  resetFlatData: () => {
    // Unsubscribe all active Firestore listeners
    unsubscribers.forEach(unsub => unsub())
    unsubscribers = []
    set({
      flatId: null,
      name: null,
      joinMode: 'auto',
      members: [],
      tasks: [],
      activityLog: [],
      swapRequests: [],
      joinRequests: [],
      expenses: [],
      settlements: [],
      recurringBills: [],
      billInstances: [],
      monthCycles: [],
      isSynced: false,
    })
  },

  addMemberMock: (uid, nickname, email, role) => {
    const existing = get().members.find(m => m.uid === uid)
    if (existing) return
    const newMember: Member = {
      uid, nickname, role,
      status: 'available',
      reliabilityScore: 100,
      joinedAt: new Date(),
    }
    // Add to members list AND append to the end of every task's rotation queue
    set(s => ({
      members: [...s.members, newMember],
      tasks: s.tasks.map(t => ({
        ...t,
        queueOrder: t.queueOrder.includes(uid)
          ? t.queueOrder                        // already in queue — skip
          : [...t.queueOrder, uid],             // append to end of circle
      })),
    }))
  },

  initFirestoreListeners: (flatId) => {
    // Unsubscribe old listeners before subscribing to new flat
    unsubscribers.forEach(unsub => unsub())
    unsubscribers = []

    if (!hasKeys) {
      set({ isSynced: true, flatId })
      return
    }

    // Reset data for clean switch
    set({ flatId, name: null, joinMode: 'auto', members: [], tasks: [], activityLog: [], swapRequests: [], joinRequests: [], expenses: [], settlements: [], recurringBills: [], billInstances: [], monthCycles: [], isSynced: false })

    // FLAT DOC LISTENER — to get flat name + joinMode
    const unsub0 = onSnapshot(doc(db, `flats/${flatId}`), (snap) => {
      if (snap.exists()) {
        const data = snap.data()
        set({ name: data.name || null, joinMode: (data.joinMode as 'auto' | 'approval') || 'auto' })
      }
    }, (err) => console.warn('Flat doc listener error:', err))

    // TASKS LISTENER
    const unsub1 = onSnapshot(collection(db, `flats/${flatId}/tasks`), (snapshot) => {
      const tasks: Task[] = []
      snapshot.forEach((doc) => tasks.push(doc.data() as Task))
      set({ tasks })
    }, (err) => console.warn('Tasks listener error:', err))

    // MEMBERS LISTENER
    const unsub2 = onSnapshot(collection(db, `flats/${flatId}/members`), (snapshot) => {
      const members: Member[] = []
      snapshot.forEach((doc) => members.push(doc.data() as Member))
      set({ members })
    }, (err) => console.warn('Members listener error:', err))

    // SWAP REQUESTS LISTENER
    const unsub3 = onSnapshot(collection(db, `flats/${flatId}/swapRequests`), (snapshot) => {
      const swapRequests: SwapRequest[] = []
      snapshot.forEach((doc) => swapRequests.push(doc.data() as SwapRequest))
      set({ swapRequests: swapRequests.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()) })
    }, (err) => console.warn('SwapRequests listener error:', err))

    // ACTIVITY LISTENER (most recent 50, sorted server-side so we never fetch the whole collection)
    const unsub4 = onSnapshot(
      query(collection(db, `flats/${flatId}/activityLog`), orderBy('timestamp', 'desc'), limit(50)),
      (snapshot) => {
        const activityLog: Activity[] = []
        snapshot.forEach((doc) => activityLog.push(doc.data() as Activity))
        set({ activityLog })
      },
      (err) => console.warn('ActivityLog listener error:', err)
    )

    // JOIN REQUESTS LISTENER
    const unsub5 = onSnapshot(collection(db, `flats/${flatId}/joinRequests`), (snapshot) => {
      const joinRequests: JoinRequest[] = []
      snapshot.forEach((doc) => joinRequests.push(doc.data() as JoinRequest))
      set({ joinRequests: joinRequests.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()) })
    }, (err) => console.warn('JoinRequests listener error:', err))

    // EXPENSES LISTENER (most recent 100, sorted by date)
    const unsub6 = onSnapshot(
      query(collection(db, `flats/${flatId}/expenses`), orderBy('date', 'desc'), limit(100)),
      (snapshot) => {
        const expenses: Expense[] = []
        snapshot.forEach((doc) => expenses.push(doc.data() as Expense))
        set({ expenses })
      },
      (err) => console.warn('Expenses listener error:', err)
    )

    // SETTLEMENTS LISTENER
    const unsub7 = onSnapshot(
      query(collection(db, `flats/${flatId}/settlements`), orderBy('date', 'desc'), limit(100)),
      (snapshot) => {
        const settlements: Settlement[] = []
        snapshot.forEach((doc) => settlements.push(doc.data() as Settlement))
        set({ settlements })
      },
      (err) => console.warn('Settlements listener error:', err)
    )

    // RECURRING BILLS LISTENER
    const unsub8 = onSnapshot(collection(db, `flats/${flatId}/recurringBills`), (snapshot) => {
      const recurringBills: RecurringBill[] = []
      snapshot.forEach((doc) => recurringBills.push(doc.data() as RecurringBill))
      set({ recurringBills })
    }, (err) => console.warn('RecurringBills listener error:', err))

    // BILL INSTANCES LISTENER
    const unsub9 = onSnapshot(
      query(collection(db, `flats/${flatId}/billInstances`), orderBy('generatedAt', 'desc'), limit(200)),
      (snapshot) => {
        const billInstances: BillInstance[] = []
        snapshot.forEach((doc) => billInstances.push(doc.data() as BillInstance))
        set({ billInstances })
      },
      (err) => console.warn('BillInstances listener error:', err)
    )

    // MONTH CYCLES LISTENER — last 6 months
    const unsub10 = onSnapshot(
      query(collection(db, `flats/${flatId}/monthCycles`), orderBy('month', 'desc'), limit(6)),
      (snapshot) => {
        const monthCycles: MonthCycle[] = []
        snapshot.forEach((doc) => monthCycles.push(doc.data() as MonthCycle))
        set({ monthCycles })
      },
      (err) => console.warn('MonthCycles listener error:', err)
    )

    unsubscribers = [unsub0, unsub1, unsub2, unsub3, unsub4, unsub5, unsub6, unsub7, unsub8, unsub9, unsub10]
    set({ isSynced: true })
  },

  addActivity: async (activityData) => {
    const newActivity: Activity = {
      ...activityData,
      id: crypto.randomUUID(),
      timestamp: new Date().toISOString()
    }
    if (hasKeys && get().flatId) {
      await setDoc(doc(db, `flats/${get().flatId}/activityLog/${newActivity.id}`), newActivity)
    } else {
      set((state) => ({ activityLog: [newActivity, ...state.activityLog] }))
    }
  },

  toggleActivityHidden: async (id) => {
    const state = get()
    const entry = state.activityLog.find(a => a.id === id)
    if (!entry) return
    const newHidden = !entry.hidden
    if (hasKeys && state.flatId) {
      await updateDoc(doc(db, `flats/${state.flatId}/activityLog/${id}`), { hidden: newHidden })
    } else {
      set(s => ({ activityLog: s.activityLog.map(a => a.id === id ? { ...a, hidden: newHidden } : a) }))
    }
  },

  markTaskCompleted: async (taskId, userId, completionDate?) => {
    const state = get()
    const taskIndex = state.tasks.findIndex(t => t.taskId === taskId)
    if (taskIndex === -1) return
    const task = state.tasks[taskIndex]
    const completedAt = completionDate ? new Date(completionDate) : undefined

    // Always fetch the freshest member availability directly from Firestore so the
    // rotation never lands on an out-of-station member due to stale Zustand state.
    let freshMembers = state.members
    if (hasKeys && state.flatId) {
      try {
        const snap = await getDocs(collection(db, `flats/${state.flatId}/members`))
        freshMembers = snap.docs.map(d => d.data() as Member)
      } catch {
        freshMembers = state.members
      }
    }

    const updatedTask = completeTask(task, freshMembers, completedAt)

    if (hasKeys && state.flatId) {
      await setDoc(doc(db, `flats/${state.flatId}/tasks/${taskId}`), updatedTask)
    } else {
      const newTasks = [...state.tasks]
      newTasks[taskIndex] = updatedTask
      set({ tasks: newTasks })
    }

    const dateLabel = completionDate
      ? ` on ${new Date(completionDate).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })}`
      : ''
    await get().addActivity({ userId, action: 'completed_task', details: `completed ${task.name}${dateLabel}` })

    // Belt-and-suspenders: if the new assignee is still out-of-station, re-rotate.
    // This handles the edge case where freshMembers itself was incomplete.
    const newAssigneeId = updatedTask.currentAssignedUserId
    if (newAssigneeId) {
      const newAssignee = freshMembers.find(m => m.uid === newAssigneeId)
      if (newAssignee && (newAssignee.status === 'out_of_station' || newAssignee.status === 'inactive')) {
        const skipNew = freshMembers.map(m =>
          m.uid === newAssigneeId ? { ...m, status: 'out_of_station' as const } : m
        )
        const nextAvailable = getNextAssignee(updatedTask, skipNew)
        if (nextAvailable) {
          await get().transferTask(taskId, newAssigneeId, nextAvailable)
        } else {
          if (hasKeys && state.flatId) {
            await updateDoc(doc(db, `flats/${state.flatId}/tasks/${taskId}`), { status: 'paused' })
          } else {
            set(s => ({ tasks: s.tasks.map(t => t.taskId === taskId ? { ...t, status: 'paused' as const } : t) }))
          }
        }
      }
    }
  },

  changeMemberStatus: async (userId, status) => {
    const state = get()
    if (hasKeys && state.flatId) {
      await updateDoc(doc(db, `flats/${state.flatId}/members/${userId}`), { status })
    } else {
      const newMembers = state.members.map(m => m.uid === userId ? { ...m, status } : m)
      set({ members: newMembers })
    }
    // Use 'system' when a different user's client triggers this (e.g. OOS auto-trigger
    // after accepting a swap request) — rules only allow writing activity under own UID or 'system'.
    const currentUid = useAuthStore.getState().user?.uid
    const activityUserId = currentUid === userId ? userId : 'system'
    await get().addActivity({ userId: activityUserId, action: 'status_change', details: `marked as ${status.replace('_', ' ')}` })

    if (status === 'out_of_station') {
      const currentState = get()
      // Apply the status change locally so getNextAssignee skips this user immediately
      const membersWithStatus = currentState.members.map(m =>
        m.uid === userId ? { ...m, status: 'out_of_station' as const } : m
      )
      // Skip tasks that were already handed off via the OOS swap-request flow —
      // those tasks have a pending or accepted isOOSRequest for this user, meaning
      // they are being transferred through the request mechanism, not a direct transfer.
      const assignedTasks = currentState.tasks.filter(t => {
        if (t.currentAssignedUserId !== userId) return false
        if (t.status !== 'pending' && t.status !== 'overdue') return false
        const handledByOOSRequest = currentState.swapRequests.some(
          r => r.taskId === t.taskId &&
               r.fromUserId === userId &&
               r.isOOSRequest &&
               (r.status === 'pending' || r.status === 'accepted')
        )
        return !handledByOOSRequest
      })
      for (const task of assignedTasks) {
        const nextUserId = getNextAssignee(task, membersWithStatus)
        if (nextUserId) {
          // Directly transfer — task must not stay with an out-of-station member
          await get().transferTask(task.taskId, userId, nextUserId)
        } else {
          // Everyone is out — pause so the task stops accumulating overdue
          if (hasKeys && currentState.flatId) {
            await updateDoc(doc(db, `flats/${currentState.flatId}/tasks/${task.taskId}`), { status: 'paused' })
          } else {
            set(s => ({ tasks: s.tasks.map(t => t.taskId === task.taskId ? { ...t, status: 'paused' as const } : t) }))
          }
          await get().addActivity({ userId: 'system', action: 'system_override', details: `paused ${task.name} — all members out of station` })
        }
      }
    }
  },

  returnEarly: async (userId) => {
    await get().changeMemberStatus(userId, 'available')
    await get().addActivity({ userId, action: 'returned_early', details: `returned early and rejoined queue` })

    const state = get()
    for (const task of state.tasks) {
      if (task.status === 'paused' && task.queueOrder.includes(userId)) {
        const updatedTask = { ...task, status: 'pending' as const, currentAssignedUserId: userId }
        if (hasKeys && state.flatId) {
          await setDoc(doc(db, `flats/${state.flatId}/tasks/${task.taskId}`), updatedTask)
        } else {
          set(s => ({ tasks: s.tasks.map(t => t.taskId === task.taskId ? updatedTask : t) }))
        }
      }
    }
  },

  checkOverdueTasks: async () => {
    const state = get()
    const now = new Date()
    for (const task of state.tasks) {
      if (task.status === 'pending' && new Date(task.dueDate) < now) {
        if (hasKeys && state.flatId) {
          await updateDoc(doc(db, `flats/${state.flatId}/tasks/${task.taskId}`), { status: 'overdue' })
        } else {
          set(s => ({ tasks: s.tasks.map(t => t.taskId === task.taskId ? { ...t, status: 'overdue' } : t) }))
        }
        await get().addActivity({ userId: 'system', action: 'overdue_alert', details: `marked ${task.name} as overdue` })
      }
    }
  },

  transferTask: async (taskId, fromUserId, toUserId) => {
    const state = get()
    const task = state.tasks.find(t => t.taskId === taskId)
    const toUser = state.members.find(m => m.uid === toUserId)
    if (!task || !toUser) return

    if (hasKeys && state.flatId) {
      await updateDoc(doc(db, `flats/${state.flatId}/tasks/${taskId}`), { currentAssignedUserId: toUserId })
    } else {
      set(s => ({ tasks: s.tasks.map(t => t.taskId === taskId ? { ...t, currentAssignedUserId: toUserId } : t) }))
    }
    await get().addActivity({ userId: fromUserId, action: 'transferred_task', details: `transferred ${task.name} to ${toUser.nickname}` })
  },

  manuallyAssignTask: async (taskId, targetUserId, adminId) => {
    const state = get()
    const task = state.tasks.find(t => t.taskId === taskId)
    const toUser = state.members.find(m => m.uid === targetUserId)
    if (!task || !toUser) return

    if (hasKeys && state.flatId) {
      await updateDoc(doc(db, `flats/${state.flatId}/tasks/${taskId}`), { status: 'pending', currentAssignedUserId: targetUserId })
    } else {
      set(s => ({ tasks: s.tasks.map(t => t.taskId === taskId ? { ...t, status: 'pending', currentAssignedUserId: targetUserId } : t) }))
    }
    await get().addActivity({ userId: adminId, action: 'system_override', details: `manually assigned ${task.name} to ${toUser.nickname}` })
  },

  createTask: async (taskData, adminId) => {
    const newTask: Task = {
      ...taskData,
      taskId: 't-' + crypto.randomUUID(),
      status: 'pending',
      lastCompletedAt: taskData.lastCompletedAt ?? new Date().toISOString(),
    }
    if (hasKeys && get().flatId) {
      await setDoc(doc(db, `flats/${get().flatId}/tasks/${newTask.taskId}`), newTask)
    } else {
      set(state => ({ tasks: [...state.tasks, newTask] }))
    }
    await get().addActivity({ userId: adminId, action: 'task_created', details: `created a new task: ${newTask.name}` })
  },

  editTask: async (taskId, changes, adminId) => {
    const state = get()
    const task = state.tasks.find(t => t.taskId === taskId)
    if (!task) return

    if (hasKeys && state.flatId) {
      await updateDoc(doc(db, `flats/${state.flatId}/tasks/${taskId}`), changes)
    } else {
      set(s => ({ tasks: s.tasks.map(t => t.taskId === taskId ? { ...t, ...changes } : t) }))
    }

    // Build a readable summary of what changed
    const changed: string[] = []
    if (changes.name && changes.name !== task.name) changed.push(`name → "${changes.name}"`)
    if (changes.priority && changes.priority !== task.priority) changed.push(`priority → ${changes.priority}`)
    if (changes.frequency && changes.frequency !== task.frequency) changed.push(`frequency → ${changes.frequency}`)
    if (changes.type && changes.type !== task.type) changed.push(`type → ${changes.type}`)
    if (changes.dueDate) changed.push(`due date updated`)

    await get().addActivity({
      userId: adminId,
      action: 'task_edited',
      details: `edited ${task.name}${changed.length ? ': ' + changed.join(', ') : ''}`,
    })
  },

  deleteTask: async (taskId, adminId) => {
    const state = get()
    const task = state.tasks.find(t => t.taskId === taskId)
    if (!task) return

    if (hasKeys && state.flatId) {
      await deleteDoc(doc(db, `flats/${state.flatId}/tasks/${taskId}`))
    } else {
      set(state => ({
        tasks: state.tasks.filter(t => t.taskId !== taskId),
        swapRequests: state.swapRequests.filter(r => r.taskId !== taskId)
      }))
    }
    await get().addActivity({ userId: adminId, action: 'task_deleted', details: `deleted the task: ${task.name}` })
  },

  createSwapRequest: async (taskId, fromUserId, toUserId, isAutomatic = false, isOOSRequest = false) => {
    const state = get()
    const exists = state.swapRequests.find(r => r.taskId === taskId && r.status === 'pending')
    if (exists) return

    const newRequest: SwapRequest = {
      id: crypto.randomUUID(),
      taskId, fromUserId, toUserId,
      status: 'pending',
      read: false,
      createdAt: new Date().toISOString(),
      isAutomatic,
      isOOSRequest,
    }

    if (hasKeys && state.flatId) {
      await setDoc(doc(db, `flats/${state.flatId}/swapRequests/${newRequest.id}`), newRequest)
    } else {
      set({ swapRequests: [newRequest, ...state.swapRequests] })
    }

    const task = state.tasks.find(t => t.taskId === taskId)
    const toUser = state.members.find(m => m.uid === toUserId)
    if (task && toUser) {
      await get().addActivity({ userId: fromUserId, action: 'swap_requested', details: `requested ${toUser.nickname} to cover ${task.name}` })
    }
  },

  addExpense: async (data) => {
    const newExpense: Expense = { ...data, id: crypto.randomUUID(), createdAt: new Date().toISOString() }
    if (hasKeys && get().flatId) {
      await setDoc(doc(db, `flats/${get().flatId}/expenses/${newExpense.id}`), fs(newExpense))
    } else {
      set(s => ({ expenses: [newExpense, ...s.expenses] }))
    }
    await get().addActivity({
      userId: data.createdBy,
      action: 'expense_added',
      details: `added expense: ${data.description}`,
    })
  },

  deleteExpense: async (expenseId) => {
    if (hasKeys && get().flatId) {
      await deleteDoc(doc(db, `flats/${get().flatId}/expenses/${expenseId}`))
    } else {
      set(s => ({ expenses: s.expenses.filter(e => e.id !== expenseId) }))
    }
  },

  addSettlement: async (data) => {
    const newSettlement: Settlement = { ...data, id: crypto.randomUUID(), createdAt: new Date().toISOString() }
    if (hasKeys && get().flatId) {
      await setDoc(doc(db, `flats/${get().flatId}/settlements/${newSettlement.id}`), fs(newSettlement))
    } else {
      set(s => ({ settlements: [newSettlement, ...s.settlements] }))
    }
    const receiver = get().members.find(m => m.uid === data.toUserId)
    await get().addActivity({
      userId: data.fromUserId,
      action: 'settlement_added',
      details: `settled up with ${receiver?.nickname ?? '…'}`,
    })
  },

  deleteSettlement: async (settlementId) => {
    if (hasKeys && get().flatId) {
      await deleteDoc(doc(db, `flats/${get().flatId}/settlements/${settlementId}`))
    } else {
      set(s => ({ settlements: s.settlements.filter(s => s.id !== settlementId) }))
    }
  },

  createRecurringBill: async (data) => {
    const newBill: RecurringBill = {
      ...data,
      id: crypto.randomUUID(),
      currentPayerIndex: 0,
      lastGeneratedMonth: '',
      createdAt: new Date().toISOString(),
    }
    if (hasKeys && get().flatId) {
      await setDoc(doc(db, `flats/${get().flatId}/recurringBills/${newBill.id}`), fs(newBill))
    } else {
      set(s => ({ recurringBills: [...s.recurringBills, newBill] }))
    }
  },

  updateRecurringBill: async (billId, changes) => {
    if (hasKeys && get().flatId) {
      await updateDoc(doc(db, `flats/${get().flatId}/recurringBills/${billId}`), changes)
    } else {
      set(s => ({ recurringBills: s.recurringBills.map(b => b.id === billId ? { ...b, ...changes } : b) }))
    }
  },

  deleteRecurringBill: async (billId) => {
    if (hasKeys && get().flatId) {
      await deleteDoc(doc(db, `flats/${get().flatId}/recurringBills/${billId}`))
    } else {
      set(s => ({ recurringBills: s.recurringBills.filter(b => b.id !== billId) }))
    }
  },

  generateBill: async (billId, amount?) => {
    const state = get()
    const bill = state.recurringBills.find(b => b.id === billId)
    const uid = useAuthStore.getState().user?.uid
    if (!bill || !uid) return

    const today = new Date()
    const yr = today.getFullYear()
    const mo = today.getMonth() + 1
    const currentMonth = `${yr}-${String(mo).padStart(2, '0')}`
    const dueDate = `${currentMonth}-${String(bill.billingDay).padStart(2, '0')}`
    const payer = bill.rotationQueue[bill.currentPayerIndex % bill.rotationQueue.length]

    // participants: who splits — falls back to rotationQueue for backward compat
    const participants = bill.participants ?? bill.rotationQueue

    // For fixed bills or when amount is explicitly provided, compute splits immediately
    const finalAmount = bill.isVariable ? (amount ?? null) : (bill.amount ?? null)

    let splits: Record<string, number> | null = null
    let status: BillInstanceStatus = 'pending'

    if (finalAmount !== null && finalAmount > 0) {
      splits = computeBillSplits(finalAmount, participants, state.members, today)
      status = 'split_generated'
    }

    const instance: BillInstance = {
      id: crypto.randomUUID(),
      templateId: billId,
      month: currentMonth,
      name: bill.name,
      category: bill.category,
      currency: bill.currency,
      amount: finalAmount,
      paidBy: payer,
      participants,
      splits,
      status,
      dueDate,
      paidAt: null,
      skippedReason: null,
      generatedAt: new Date().toISOString(),
      generatedBy: uid,
    }

    if (hasKeys && state.flatId) {
      await setDoc(doc(db, `flats/${state.flatId}/billInstances/${instance.id}`), fs(instance))
    } else {
      set(s => ({ billInstances: [...s.billInstances, instance] }))
    }

    // Advance payer rotation and record generation month
    const nextIndex = (bill.currentPayerIndex + 1) % bill.rotationQueue.length
    if (hasKeys && state.flatId) {
      await updateDoc(doc(db, `flats/${state.flatId}/recurringBills/${billId}`), {
        currentPayerIndex: nextIndex,
        lastGeneratedMonth: currentMonth,
      })
    } else {
      set(s => ({
        recurringBills: s.recurringBills.map(b => b.id === billId
          ? { ...b, currentPayerIndex: nextIndex, lastGeneratedMonth: currentMonth }
          : b)
      }))
    }

    const monthName = today.toLocaleDateString('en-IN', { month: 'long', year: 'numeric' })
    await get().addActivity({
      userId: uid,
      action: 'bill_generated',
      details: `generated ${bill.name} bill for ${monthName}`,
    })
  },

  confirmBillAmount: async (instanceId, amount) => {
    const state = get()
    const instance = state.billInstances.find(b => b.id === instanceId)
    const uid = useAuthStore.getState().user?.uid
    if (!instance || !uid || amount <= 0) return

    const splits = computeBillSplits(amount, instance.participants, state.members, new Date())
    const changes = { amount, splits, status: 'split_generated' as BillInstanceStatus }

    if (hasKeys && state.flatId) {
      await updateDoc(doc(db, `flats/${state.flatId}/billInstances/${instanceId}`), changes)
    } else {
      set(s => ({ billInstances: s.billInstances.map(b => b.id === instanceId ? { ...b, ...changes } : b) }))
    }
    await get().addActivity({
      userId: uid,
      action: 'bill_generated',
      details: `confirmed ${instance.name} amount: ₹${amount}`,
    })
  },

  markBillPaid: async (instanceId) => {
    const state = get()
    const instance = state.billInstances.find(b => b.id === instanceId)
    const uid = useAuthStore.getState().user?.uid
    if (!instance || !uid) return

    const changes = { status: 'paid' as BillInstanceStatus, paidAt: new Date().toISOString() }
    if (hasKeys && state.flatId) {
      await updateDoc(doc(db, `flats/${state.flatId}/billInstances/${instanceId}`), changes)
    } else {
      set(s => ({ billInstances: s.billInstances.map(b => b.id === instanceId ? { ...b, ...changes } : b) }))
    }
    await get().addActivity({
      userId: uid,
      action: 'bill_paid',
      details: `marked ${instance.name} as paid to landlord/provider`,
    })
  },

  skipBillInstance: async (instanceId, reason?) => {
    const state = get()
    const instance = state.billInstances.find(b => b.id === instanceId)
    const uid = useAuthStore.getState().user?.uid
    if (!instance || !uid) return

    const changes = { status: 'skipped' as BillInstanceStatus, skippedReason: reason ?? null }
    if (hasKeys && state.flatId) {
      await updateDoc(doc(db, `flats/${state.flatId}/billInstances/${instanceId}`), changes)
    } else {
      set(s => ({ billInstances: s.billInstances.map(b => b.id === instanceId ? { ...b, ...changes } : b) }))
    }
    await get().addActivity({
      userId: uid,
      action: 'bill_skipped',
      details: `skipped ${instance.name}${reason ? `: ${reason}` : ''}`,
    })
  },

  editBillInstanceAmount: async (instanceId, amount) => {
    const state = get()
    const instance = state.billInstances.find(b => b.id === instanceId)
    const uid = useAuthStore.getState().user?.uid
    if (!instance || !uid || amount <= 0) return
    const splits = computeBillSplits(amount, instance.participants, state.members, new Date())
    const changes = { amount, splits, status: 'split_generated' as BillInstanceStatus }
    if (hasKeys && state.flatId) {
      await updateDoc(doc(db, `flats/${state.flatId}/billInstances/${instanceId}`), changes)
    } else {
      set(s => ({ billInstances: s.billInstances.map(b => b.id === instanceId ? { ...b, ...changes } : b) }))
    }
    await get().addActivity({ userId: uid, action: 'bill_generated', details: `edited ${instance.name} amount to ${amount}` })
  },

  deleteBillInstance: async (instanceId) => {
    const state = get()
    const instance = state.billInstances.find(b => b.id === instanceId)
    const uid = useAuthStore.getState().user?.uid
    if (!instance || !uid) return
    if (hasKeys && state.flatId) {
      await deleteDoc(doc(db, `flats/${state.flatId}/billInstances/${instanceId}`))
      // Reset so the bill shows as DUE again and can be re-generated
      await updateDoc(doc(db, `flats/${state.flatId}/recurringBills/${instance.templateId}`), {
        lastGeneratedMonth: '',
      })
    } else {
      set(s => ({
        billInstances: s.billInstances.filter(b => b.id !== instanceId),
        recurringBills: s.recurringBills.map(b =>
          b.id === instance.templateId ? { ...b, lastGeneratedMonth: '' } : b
        ),
      }))
    }
    await get().addActivity({ userId: uid, action: 'bill_skipped', details: `deleted and reset ${instance.name} — can be regenerated` })
  },

  closeMonth: async (month, confirmedSettlements, summary, carryForwardOut) => {
    const state = get()
    const uid = useAuthStore.getState().user?.uid
    if (!uid) return

    const today = new Date().toISOString()
    const nextMonth = (() => {
      const [y, m] = month.split('-').map(Number)
      const n = new Date(y, m, 1)
      return `${n.getFullYear()}-${String(n.getMonth() + 1).padStart(2, '0')}`
    })()

    // Get carry-forward from previous month (already in state or look up)
    const prevMonth = (() => {
      const [y, m] = month.split('-').map(Number)
      const p = new Date(y, m - 2, 1)
      return `${p.getFullYear()}-${String(p.getMonth() + 1).padStart(2, '0')}`
    })()
    const prevCycle = state.monthCycles.find(mc => mc.month === prevMonth)
    const carryForwardIn = prevCycle?.carryForwardOut ?? null

    // Write confirmed settlements
    for (const s of confirmedSettlements) {
      await get().addSettlement({
        fromUserId: s.fromUserId,
        toUserId: s.toUserId,
        amount: s.amount,
        currency: 'INR',
        note: s.note ?? undefined,
        date: today.split('T')[0],
        month,
        type: s.type,
      })
    }

    // Write/update MonthCycle doc
    const cycleId = `${state.flatId}_${month}`
    const cycle: MonthCycle = {
      id: cycleId,
      flatId: state.flatId ?? '',
      month,
      status: 'closed',
      openedAt: state.monthCycles.find(mc => mc.month === month)?.openedAt ?? today,
      closedAt: today,
      totalBillsINR: summary.totalBillsINR,
      totalExpensesINR: summary.totalExpensesINR,
      totalSettledINR: summary.totalSettledINR + confirmedSettlements.reduce((s, c) => s + c.amount, 0),
      netBalances: summary.netBalances,
      carryForwardIn: carryForwardIn ? { fromMonth: prevMonth, balances: carryForwardIn.balances ?? carryForwardIn } : null,
      carryForwardOut: carryForwardOut ? { toMonth: nextMonth, balances: carryForwardOut } : null,
      createdBy: uid,
    }

    if (hasKeys && state.flatId) {
      await setDoc(doc(db, `flats/${state.flatId}/monthCycles/${cycleId}`), fs(cycle))
    } else {
      set(s => ({
        monthCycles: [
          cycle,
          ...s.monthCycles.filter(mc => mc.month !== month),
        ],
      }))
    }

    const label = new Date(Number(month.split('-')[0]), Number(month.split('-')[1]) - 1, 1)
      .toLocaleDateString('en-IN', { month: 'long', year: 'numeric' })
    await get().addActivity({
      userId: uid,
      action: 'settlement_added',
      details: `closed ${label} — ${confirmedSettlements.length} settlement${confirmedSettlements.length !== 1 ? 's' : ''} recorded${carryForwardOut ? ', balance carried forward' : ''}`,
    })
  },

  resolveSwapRequest: async (requestId, status) => {
    const state = get()
    const request = state.swapRequests.find(r => r.id === requestId)
    if (!request) return

    // Optimistic local update so subsequent checks in this function see the new status
    set(s => ({ swapRequests: s.swapRequests.map(r => r.id === requestId ? { ...r, status } : r) }))
    if (hasKeys && state.flatId) {
      await updateDoc(doc(db, `flats/${state.flatId}/swapRequests/${requestId}`), { status })
    }

    const task = state.tasks.find(t => t.taskId === request.taskId)
    const fromUser = state.members.find(m => m.uid === request.fromUserId)

    if (status === 'accepted' && task && fromUser) {
      await get().transferTask(request.taskId, request.fromUserId, request.toUserId)
      await get().addActivity({ userId: request.toUserId, action: 'swap_resolved', details: `accepted to cover ${task.name} for ${fromUser.nickname}` })

      // Auto-OOS: if this was part of a going-out-of-station batch, check if all OOS
      // requests from this user are now accepted. If so, mark them out of station.
      if (request.isOOSRequest) {
        // Re-read state — the optimistic update above means this request is now 'accepted'
        const fresh = get()
        const stillPending = fresh.swapRequests.filter(
          r => r.fromUserId === request.fromUserId &&
               r.isOOSRequest === true &&
               r.status === 'pending'  // current request is already 'accepted' in local state
        )
        if (stillPending.length === 0) {
          await get().changeMemberStatus(request.fromUserId, 'out_of_station')
        }
      }
    } else if (status === 'rejected' && task && fromUser) {
      await get().addActivity({ userId: request.toUserId, action: 'swap_resolved', details: `declined to cover ${task.name} for ${fromUser.nickname}` })
    }
  },

  markSwapRequestRead: async (requestId) => {
    const state = get()
    // Optimistic update — hide banner immediately without waiting for Firestore listener
    set(s => ({ swapRequests: s.swapRequests.map(r => r.id === requestId ? { ...r, read: true } : r) }))
    if (hasKeys && state.flatId) {
      await updateDoc(doc(db, `flats/${state.flatId}/swapRequests/${requestId}`), { read: true })
    }
  },

  // ── Flat Settings ──────────────────────────────────────────────────────────

  renameFlatAction: async (newName) => {
    const { flatId } = get()
    if (!flatId) return
    if (hasKeys) {
      await updateFlatNameService(flatId, newName)
    } else {
      set({ name: newName })
    }
  },

  setJoinMode: async (mode) => {
    const { flatId } = get()
    if (!flatId) return
    if (hasKeys) {
      await setFlatJoinModeService(flatId, mode)
    } else {
      set({ joinMode: mode })
    }
  },

  approveJoinRequest: async (requestId) => {
    const state = get()
    if (!state.flatId) return
    const req = state.joinRequests.find(r => r.id === requestId)
    if (!req) return
    // Optimistic update
    set(s => ({ joinRequests: s.joinRequests.map(r => r.id === requestId ? { ...r, status: 'approved' as const } : r) }))
    await approveJoinRequestService(state.flatId, requestId, req.uid, req.nickname, req.email)
    await get().addActivity({ userId: req.uid, action: 'status_change', details: `${req.nickname} was approved to join the flat` })
  },

  rejectJoinRequest: async (requestId) => {
    const state = get()
    if (!state.flatId) return
    const req = state.joinRequests.find(r => r.id === requestId)
    if (!req) return
    set(s => ({ joinRequests: s.joinRequests.map(r => r.id === requestId ? { ...r, status: 'rejected' as const } : r) }))
    await rejectJoinRequestService(state.flatId, requestId)
  },

  // ── Membership Management ──────────────────────────────────────────────────

  leaveFlat: async (uid) => {
    const state = get()
    if (!state.flatId) return { nextFlatId: null }

    if (!hasKeys) {
      // Mock mode — just reset
      get().resetFlatData()
      return { nextFlatId: null }
    }

    const result = await leaveFlatService(uid, state.flatId)
    get().resetFlatData()
    return result
  },

  kickMember: async (targetUid, adminId) => {
    const state = get()
    if (!state.flatId) return

    if (!hasKeys) {
      // Mock mode — just remove from local state
      set(s => ({
        members: s.members.filter(m => m.uid !== targetUid),
        tasks: s.tasks.map(t => ({
          ...t,
          queueOrder: t.queueOrder.filter(uid => uid !== targetUid),
          currentAssignedUserId: t.currentAssignedUserId === targetUid
            ? t.queueOrder.find(uid => uid !== targetUid) || ''
            : t.currentAssignedUserId,
        }))
      }))
      return
    }

    await kickMemberService(adminId, targetUid, state.flatId)
    // Firestore listeners will update members in real-time
  },

  transferAdmin: async (newAdminUid, currentAdminUid) => {
    const state = get()
    if (!state.flatId) return

    if (!hasKeys) {
      set(s => ({
        members: s.members.map(m => ({
          ...m,
          role: m.uid === newAdminUid ? 'admin'
              : m.uid === currentAdminUid ? 'member'
              : m.role
        }))
      }))
      return
    }

    await transferAdminService(currentAdminUid, newAdminUid, state.flatId)
    // Firestore listener will update members
  },

  deleteFlat: async (adminUid) => {
    const state = get()
    if (!state.flatId) return

    if (!hasKeys) {
      get().resetFlatData()
      return
    }

    await deleteEntireFlatService(adminUid, state.flatId)
    get().resetFlatData()
  },
}))
