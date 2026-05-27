import { create } from 'zustand'
import { completeTask } from '../lib/rotationEngine'
import { db, hasKeys } from '@/lib/firebase'
import {
  collection, doc, onSnapshot, updateDoc, setDoc, deleteDoc, getDoc
} from 'firebase/firestore'
import {
  leaveFlatService,
  kickMemberService,
  transferAdminService,
  deleteEntireFlatService,
  updateFlatName as updateFlatNameService,
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
  frequency: 'daily' | 'weekly' | 'monthly' | 'custom'
  currentAssignedUserId: string
  queueOrder: string[]
  status: 'pending' | 'completed' | 'overdue' | 'paused'
  dueDate: string
  lastCompletedAt: string
}

export interface Activity {
  id: string
  timestamp: string
  userId: string
  action: 'completed_task' | 'skipped_task' | 'status_change' | 'overdue_alert' | 'transferred_task' | 'system_override' | 'swap_requested' | 'swap_resolved' | 'task_created' | 'task_deleted' | 'returned_early'
  details: string
  /** Admin can soft-hide entries without deleting them */
  hidden?: boolean
}

export interface SwapRequest {
  id: string
  taskId: string
  fromUserId: string
  toUserId: string
  status: 'pending' | 'accepted' | 'rejected'
  read: boolean
  createdAt: string
}

interface FlatState {
  flatId: string | null
  name: string | null
  members: Member[]
  tasks: Task[]
  activityLog: Activity[]
  swapRequests: SwapRequest[]
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
  createTask: (taskData: Omit<Task, 'taskId' | 'status' | 'lastCompletedAt'>, adminId: string) => Promise<void>
  deleteTask: (taskId: string, adminId: string) => Promise<void>
  createSwapRequest: (taskId: string, fromUserId: string, toUserId: string) => Promise<void>
  resolveSwapRequest: (requestId: string, status: 'accepted' | 'rejected') => Promise<void>
  markSwapRequestRead: (requestId: string) => Promise<void>
  addActivity: (activity: Omit<Activity, 'id' | 'timestamp'>) => Promise<void>
  toggleActivityHidden: (id: string) => Promise<void>

  // Flat Settings
  renameFlatAction: (newName: string) => Promise<void>

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

// Module-level unsubscribers — persists across state resets
let unsubscribers: (() => void)[] = []

export const useFlatStore = create<FlatState>((set, get) => ({
  flatId: hasKeys ? null : 'FLAT-1234',
  name: hasKeys ? null : 'Bachelor Pad',
  members: hasKeys ? [] : MOCK_MEMBERS,
  tasks: hasKeys ? [] : MOCK_TASKS,
  activityLog: [],
  swapRequests: [],
  isSynced: false,

  resetFlatData: () => {
    // Unsubscribe all active Firestore listeners
    unsubscribers.forEach(unsub => unsub())
    unsubscribers = []
    set({
      flatId: null,
      name: null,
      members: [],
      tasks: [],
      activityLog: [],
      swapRequests: [],
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
    set(s => ({ members: [...s.members, newMember] }))
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
    set({ flatId, name: null, members: [], tasks: [], activityLog: [], swapRequests: [], isSynced: false })

    // FLAT DOC LISTENER — to get flat name
    const unsub0 = onSnapshot(doc(db, `flats/${flatId}`), (snap) => {
      if (snap.exists()) set({ name: snap.data().name || null })
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

    // ACTIVITY LISTENER (last 50)
    const unsub4 = onSnapshot(collection(db, `flats/${flatId}/activityLog`), (snapshot) => {
      const activityLog: Activity[] = []
      snapshot.forEach((doc) => activityLog.push(doc.data() as Activity))
      set({ activityLog: activityLog.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()).slice(0, 50) })
    }, (err) => console.warn('ActivityLog listener error:', err))

    unsubscribers = [unsub0, unsub1, unsub2, unsub3, unsub4]
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
    // Parse the optional completion date — reject future dates
    const completedAt = completionDate ? new Date(completionDate) : undefined
    const updatedTask = completeTask(task, state.members, completedAt)

    if (hasKeys && state.flatId) {
      await setDoc(doc(db, `flats/${state.flatId}/tasks/${taskId}`), updatedTask)
    } else {
      const newTasks = [...state.tasks]
      newTasks[taskIndex] = updatedTask
      set({ tasks: newTasks })
    }

    // Include the actual date in the activity entry so the log is accurate
    const dateLabel = completionDate
      ? ` on ${new Date(completionDate).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })}`
      : ''
    await get().addActivity({ userId, action: 'completed_task', details: `completed ${task.name}${dateLabel}` })
  },

  changeMemberStatus: async (userId, status) => {
    const state = get()
    if (hasKeys && state.flatId) {
      await updateDoc(doc(db, `flats/${state.flatId}/members/${userId}`), { status })
    } else {
      const newMembers = state.members.map(m => m.uid === userId ? { ...m, status } : m)
      set({ members: newMembers })
    }
    await get().addActivity({ userId, action: 'status_change', details: `marked as ${status.replace('_', ' ')}` })
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
      lastCompletedAt: new Date().toISOString()
    }
    if (hasKeys && get().flatId) {
      await setDoc(doc(db, `flats/${get().flatId}/tasks/${newTask.taskId}`), newTask)
    } else {
      set(state => ({ tasks: [...state.tasks, newTask] }))
    }
    await get().addActivity({ userId: adminId, action: 'task_created', details: `created a new task: ${newTask.name}` })
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

  createSwapRequest: async (taskId, fromUserId, toUserId) => {
    const state = get()
    const exists = state.swapRequests.find(r => r.taskId === taskId && r.status === 'pending')
    if (exists) return

    const newRequest: SwapRequest = {
      id: crypto.randomUUID(),
      taskId, fromUserId, toUserId,
      status: 'pending',
      read: false,
      createdAt: new Date().toISOString()
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

  resolveSwapRequest: async (requestId, status) => {
    const state = get()
    const request = state.swapRequests.find(r => r.id === requestId)
    if (!request) return

    if (hasKeys && state.flatId) {
      await updateDoc(doc(db, `flats/${state.flatId}/swapRequests/${requestId}`), { status })
    } else {
      set(s => ({ swapRequests: s.swapRequests.map(r => r.id === requestId ? { ...r, status } : r) }))
    }

    const task = state.tasks.find(t => t.taskId === request.taskId)
    const fromUser = state.members.find(m => m.uid === request.fromUserId)

    if (status === 'accepted' && task && fromUser) {
      await get().transferTask(request.taskId, request.fromUserId, request.toUserId)
      await get().addActivity({ userId: request.toUserId, action: 'swap_resolved', details: `accepted to cover ${task.name} for ${fromUser.nickname}` })
    } else if (status === 'rejected' && task && fromUser) {
      await get().addActivity({ userId: request.toUserId, action: 'swap_resolved', details: `declined to cover ${task.name} for ${fromUser.nickname}` })
    }
  },

  markSwapRequestRead: async (requestId) => {
    const state = get()
    if (hasKeys && state.flatId) {
      await updateDoc(doc(db, `flats/${state.flatId}/swapRequests/${requestId}`), { read: true })
    } else {
      set(s => ({ swapRequests: s.swapRequests.map(r => r.id === requestId ? { ...r, read: true } : r) }))
    }
  },

  // ── Flat Settings ──────────────────────────────────────────────────────────

  renameFlatAction: async (newName) => {
    const { flatId } = get()
    if (!flatId) return
    if (hasKeys) {
      await updateFlatNameService(flatId, newName)
      // The flat doc onSnapshot listener will update `name` in state automatically
    } else {
      set({ name: newName })
    }
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
