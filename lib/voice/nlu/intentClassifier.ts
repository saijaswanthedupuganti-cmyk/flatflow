// ─── Levenshtein distance ────────────────────────────────────────────────────
function levenshtein(a: string, b: string): number {
  const m = a.length, n = b.length
  // Use two rows instead of full matrix — O(min(m,n)) space
  let prev = Array.from({ length: n + 1 }, (_, j) => j)
  let curr = new Array(n + 1).fill(0)
  for (let i = 1; i <= m; i++) {
    curr[0] = i
    for (let j = 1; j <= n; j++) {
      curr[j] = a[i - 1] === b[j - 1]
        ? prev[j - 1]
        : 1 + Math.min(prev[j], curr[j - 1], prev[j - 1])
    }
    ;[prev, curr] = [curr, prev]
  }
  return prev[n]
}

// ─── Fuzzy match ─────────────────────────────────────────────────────────────
// Returns true if every word in `pattern` has a close-enough match in `text`.
function fuzzyMatch(text: string, pattern: string, threshold = 0.78): boolean {
  if (!pattern) return false
  if (text.includes(pattern)) return true

  const tWords = text.split(/\s+/).filter(Boolean)
  const pWords = pattern.split(/\s+/).filter(Boolean)
  if (pWords.length === 0) return false

  for (const pw of pWords) {
    if (pw.length < 3) {
      // Short words: require exact whole-word match
      if (!tWords.includes(pw)) return false
      continue
    }
    let matched = false
    for (const tw of tWords) {
      if (tw.length < 2) continue
      const sim = 1 - levenshtein(pw, tw) / Math.max(pw.length, tw.length)
      if (sim >= threshold) { matched = true; break }
    }
    if (!matched) return false
  }
  return true
}

// ─── Intent types ─────────────────────────────────────────────────────────────
export type IntentType =
  | 'COMPLETE_TASK'
  | 'CREATE_EXPENSE'
  | 'QUERY_BALANCE'
  | 'QUERY_TASKS'
  | 'QUERY_STATUS'
  | 'REQUEST_SWAP'
  | 'CREATE_TASK'
  | 'GREETING'
  | 'UNKNOWN'

interface IntentPattern {
  intent: IntentType
  priority: number
  triggers: string[]   // phrases — fuzzy matched against transcript
}

// ─── Intent patterns (priority descending) ───────────────────────────────────
// Hinglish phrases are inlined with English — top 20 most common included.
const INTENT_PATTERNS: IntentPattern[] = [
  {
    intent: 'COMPLETE_TASK',
    priority: 10,
    triggers: [
      // English
      'done', 'completed', 'finished', 'i did', 'i have done', 'i completed',
      'i finished', 'task done', 'chore done', 'duty done', 'mark done',
      'mark as done', 'mark complete', 'mark as complete',
      'i did it', 'just finished', 'all done', 'wrapped up',
      // Past-tense domestic action verbs — strong completion signals
      'cleaned', 'i cleaned', 'washed', 'i washed', 'mopped', 'swept',
      'dusted', 'i dusted', 'vacuumed', 'ironed', 'i ironed',
      'watered', 'i watered', 'organized', 'i organized',
      'scrubbed', 'took out', 'i took out', 'threw out',
      'made dinner', 'made lunch', 'made breakfast', 'made food', 'cooked',
      'pressed', 'segregated', 'serviced', 'refilled', 'i filled',
      'changed the', 'i changed', 'removed', 'fluffed',
      'is ready', 'is done', 'prepared', 'wiped', 'changed',
      'i called', 'visited', 'returned', 'purchased', 'done for today',
      'trash out', 'garbage out', 'all chores done', 'chores done',
      // Hinglish
      'ho gaya', 'hogaya', 'kar diya', 'complete kar diya', 'ho gayi',
      'khatam', 'khatam ho gaya', 'ho chuka', 'kar di', 'nipta diya',
      'lag gayi', 'lag gaya', 'dho liye', 'phenk diya', 'le aaya',
      'saaf kar diya', 'saaf kar di', 'change kar diya', 'kar liye',
      // Telugu-English
      'aipoyindi', 'chesesa', 'aipoyaru', 'chesa', 'kadigesa',
    ],
  },
  {
    intent: 'CREATE_EXPENSE',
    priority: 9,
    triggers: [
      // English
      'i spent', 'spent', 'i paid', 'paid', 'add expense', 'add money',
      'add bill', 'log expense', 'split', 'i bought', 'bought',
      'purchased', 'purchase', 'expense', 'divide', 'share the cost',
      'split the bill', 'split it', 'split equally',
      // Hinglish
      'kharcha', 'paisa diya', 'maine kharida', 'khareeda', 'liya', 'liye',
      'kharcha kiya', 'paisa lagaya', 'diye', 'pe kharcha', 'ke liye diye',
      'add kar', 'add kar do', 'bill pay kiya',
      // Telugu-English
      'icha', 'ki icha', 'ki add chey', 'pay chesa', 'ki kharchu', 'kharchu',
    ],
  },
  {
    intent: 'QUERY_BALANCE',
    priority: 8,
    triggers: [
      // English
      'how much', 'balance', 'who owes', 'who owes me', 'what do i owe',
      'how much do i owe', 'how much does', 'do i owe', 'owe me',
      'what is my balance', 'net balance', 'settled', 'are we settled',
      'how much is left', 'total owed', 'show me the money',
      'where is my money', 'who has to pay me', 'do i owe anyone',
      'am i in the green', 'am i in the red', 'what is the damage',
      'how deep am i', 'ledger', 'balance sheet', 'financial status',
      'net worth', 'total dues', 'total inflow', 'total outflow',
      'debt report', 'credit report', 'who is the richest', 'who owes the most',
      'show all balances', 'show balance',
      // Hinglish
      'kitna', 'kitne', 'kitna dena', 'kitna lena', 'balance kitna',
      'paisa kitna', 'kitna baaki', 'mujhe kitna',
      // Telugu-English
      'entha', 'ivvali', 'entha baki', 'entha undi', 'entha ivvali',
      'nannu entha', 'ki entha', 'settled aa', 'baki undi',
    ],
  },
  {
    intent: 'QUERY_TASKS',
    priority: 8,
    triggers: [
      // English
      'what are my tasks', 'what tasks', 'my tasks', 'my duties',
      'pending tasks', 'any tasks', 'what do i need to do',
      'what should i do', 'today tasks', 'what is due', 'whats due',
      'what is pending', 'remaining tasks', 'overdue tasks', 'what is left',
      'what is late', 'what did i miss', 'task list', 'task count',
      'task summary', 'quick status', 'what should i do first',
      'priority task', 'urgent tasks', 'what did i complete',
      'how many tasks', 'how busy am i', 'what is my workload',
      'show my tasks', 'show all duties', 'what is on my plate',
      // Hinglish
      'kya karna hai', 'kaam kya hai', 'mera kaam', 'task kya hai',
      'kya baaki hai', 'aaj kya karna', 'mere tasks', 'mere duties',
      'pending task hai', 'next task', 'task due', 'kitne tasks',
      'schedule kya', 'kya complete kiya', 'kya miss ho gaya',
      // Telugu-English
      'na tasks', 'em cheyali', 'em baki', 'na duties', 'pending tasks unnaya',
      'em due undi', 'task enti', 'na task list', 'em complete',
      'na plate', 'na workload', 'enni tasks',
    ],
  },
  {
    intent: 'REQUEST_SWAP',
    priority: 9,
    triggers: [
      // English — checked before QUERY_TASKS to avoid "my task" ambiguity
      'swap', 'cover for me', 'cover my', 'take over', 'can someone cover',
      'swap request', 'trade', 'exchange task', 'cover my task',
      'help with my task', 'someone else do',
      // More English swap patterns
      'i need help', 'i am busy', 'i am sick', 'i am not feeling well',
      'can someone take', 'someone do my', 'take my place',
      'i need a day off', 'i am overloaded', 'i am traveling',
      'i am going out', 'parents are visiting', 'have an exam',
      'working late', 'someone cover',
      // Hinglish
      'swap kar do', 'cover kar do', 'koi kar do', 'mera kaam kar do',
      'cover kar dega', 'koi cover kar de', 'help chahiye',
      'main busy hoon', 'main bahar ja raha', 'main bimaar hoon',
      'mera task koi', 'koi mera task', 'meri jagah', 'meri duty',
      // Telugu-English
      'evaru chestaru', 'chestada', 'cover cheyandi', 'help kavali',
      'nenu busy', 'lo help', 'evaru chestadu',
    ],
  },
  {
    intent: 'CREATE_TASK',
    priority: 6,
    triggers: [
      // English
      'add task', 'new task', 'create task', 'make task', 'set up task',
      'add duty', 'new duty', 'create duty', 'schedule task',
      'daily task', 'weekly task', 'monthly task', 'fortnightly task',
      'task daily', 'task weekly', 'task monthly',
      // Hinglish
      'task banao', 'kaam add karo', 'naya task',
      'daily task add', 'weekly task add', 'monthly task',
      // Telugu-English
      'task add chey', 'task create chey',
    ],
  },
  {
    intent: 'QUERY_STATUS',
    priority: 5,
    triggers: [
      // English
      'who is home', 'who is out', 'out of station', 'who is oos',
      'who is available', 'where is', 'is anyone home', 'who is around',
      'flat status', 'member status', 'who is present', 'who is absent',
      'who is traveling', 'who is away', 'who is in the flat',
      'is everyone home', 'is anyone away', 'when is', 'coming back',
      'return date',
      // Hinglish
      'kahan hai', 'ghar pe hai', 'bahar gaya', 'kaun hai ghar pe',
      // Telugu-English
      'evaru intlo', 'evaru bayata', 'intlo unnadu', 'bayata unnadu',
      'intlo unnara', 'ekkada unnadu', 'eppudu vastadu', 'evaru oos',
    ],
  },
  {
    intent: 'GREETING',
    priority: 1,
    triggers: [
      'hi', 'hello', 'hey', 'good morning', 'good evening', 'good afternoon',
      'good night', 'sup', 'yo', 'hola',
      // Hinglish
      'namaste', 'namaskar', 'kem cho',
      // Greetings + common English variations
      'hey there', 'hi there', 'hello there', 'hey buddy', 'hi mate',
      'howdy', 'greetings', 'salutations', 'what is up', 'whats up',
      'how are you', 'how is it going', 'hey friend', 'hi friend',
      'hello friend', 'hey mate', 'hello mate',
    ],
  },
]

// Sort by priority descending (once, at module load)
const SORTED_PATTERNS = [...INTENT_PATTERNS].sort((a, b) => b.priority - a.priority)

// ─── classifyIntent ───────────────────────────────────────────────────────────
export function classifyIntent(transcript: string): IntentType {
  const normalised = transcript.toLowerCase().trim().replace(/[^a-z0-9\s]/g, ' ').replace(/\s+/g, ' ')

  for (const { intent, triggers } of SORTED_PATTERNS) {
    for (const trigger of triggers) {
      if (fuzzyMatch(normalised, trigger)) return intent
    }
  }
  return 'UNKNOWN'
}

// ─── Export helpers for testing ───────────────────────────────────────────────
export { levenshtein, fuzzyMatch }
