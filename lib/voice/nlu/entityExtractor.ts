import type { VoiceFlatContext } from './contextResolver'
import { levenshtein } from './intentClassifier'
import type { ExpenseCategory } from '@/store/useFlatStore'

// ─── Types ────────────────────────────────────────────────────────────────────
export interface ExtractedEntities {
  amount?:      number
  currency?:    'INR' | 'USD' | 'EUR' | 'GBP'
  description?: string
  category?:    ExpenseCategory
  task?:        string    // matched task name from flat
  taskId?:      string    // matched task id
  member?:      string    // matched member nickname
  memberUid?:   string    // matched member uid
  frequency?:   'daily' | 'weekly' | 'fortnightly' | 'monthly'
  splitType?:   'equal' | 'custom'
  allMembers?:  boolean
  date?:        string    // yyyy-MM-dd
}

// ─── Amount extraction ────────────────────────────────────────────────────────
// Word-to-number table
const WORD_NUMS: Record<string, number> = {
  zero:0,one:1,two:2,three:3,four:4,five:5,six:6,seven:7,eight:8,nine:9,
  ten:10,eleven:11,twelve:12,thirteen:13,fourteen:14,fifteen:15,
  sixteen:16,seventeen:17,eighteen:18,nineteen:19,
  twenty:20,thirty:30,forty:40,fifty:50,sixty:60,seventy:70,eighty:80,ninety:90,
  hundred:100,thousand:1000,lakh:100000,lac:100000,crore:10000000,
}

// Regex: number words — \b ensures we don't match "one" inside "done"
const NUM_WORD_RE = new RegExp(
  `\\b(?:${Object.keys(WORD_NUMS).join('|')})(?:\\s+(?:${Object.keys(WORD_NUMS).join('|')}))*\\b`,
  'gi'
)

function parseWordNumber(phrase: string): number {
  const words = phrase.toLowerCase().match(/[a-z]+/g) ?? []
  let total   = 0
  let current = 0

  for (const word of words) {
    const val = WORD_NUMS[word]
    if (val === undefined) continue

    if (val === 100) {
      current = (current === 0 ? 1 : current) * 100
    } else if (val >= 1000) {
      // Handle "fifty-two hundred" → 52 × 100 already done before this block
      total  += (current === 0 ? 1 : current) * val
      current = 0
    } else {
      current += val
    }
  }
  return total + current
}

export function extractAmount(text: string): { amount: number; currency: 'INR' } | null {
  const t = text.toLowerCase()

  // 1. Explicit currency symbol / prefix (₹, rs, inr)
  const currencyRe = /(?:₹|rs\.?|rupees?|rupay|inr)\s*(\d[\d,]*(?:\.\d{1,2})?)/i
  const m1 = t.match(currencyRe)
  if (m1) {
    const n = parseFloat(m1[1].replace(/,/g, ''))
    if (n > 0 && n < 100_000_000) return { amount: n, currency: 'INR' }
  }

  // 2. Suffix currency (500 rupees, 500 rs, 500 bucks)
  const suffixRe = /(\d[\d,]*(?:\.\d{1,2})?)\s*(?:rs\.?|rupees?|rupay|bucks|money)/i
  const m2 = t.match(suffixRe)
  if (m2) {
    const n = parseFloat(m2[1].replace(/,/g, ''))
    if (n > 0 && n < 100_000_000) return { amount: n, currency: 'INR' }
  }

  // 3. k-notation: 1.5k, 2k, 1.2K
  const kRe = /(\d+(?:\.\d+)?)\s*k(?:\b|$)/i
  const m3 = t.match(kRe)
  if (m3) {
    const n = parseFloat(m3[1]) * 1000
    if (n > 0 && n < 100_000_000) return { amount: Math.round(n), currency: 'INR' }
  }

  // 4. Lakh notation: 1.5 lakh, 2L, 1.5L
  const lakhRe = /(\d+(?:\.\d+)?)\s*(?:lakh|lac|l\b)/i
  const m4 = t.match(lakhRe)
  if (m4) {
    const n = parseFloat(m4[1]) * 100_000
    if (n > 0 && n < 100_000_000) return { amount: Math.round(n), currency: 'INR' }
  }

  // 5. Word-form numbers: "five hundred", "one thousand two hundred"
  //    Also handles Indian colloquial: "fifty two hundred" → 52 × 100 = 5200
  const wordMatches = t.match(NUM_WORD_RE)
  if (wordMatches) {
    for (const phrase of wordMatches) {
      const words = phrase.toLowerCase().split(/\s+/)

      // Detect "N hundred" where N > 9 (Indian colloquial like "fifty two hundred")
      // Skip if prefix contains multipliers (thousand/lakh/crore) — those are standard forms
      const multiplierWords = new Set(['thousand', 'lakh', 'lac', 'crore'])
      const hundredIdx = words.lastIndexOf('hundred')
      if (hundredIdx > 0) {
        const prefix = words.slice(0, hundredIdx)
        const hasMultiplier = prefix.some(w => multiplierWords.has(w))
        if (!hasMultiplier) {
          const prefixVal = parseWordNumber(prefix.join(' '))
          if (prefixVal > 9) {
            // e.g. "fifty two hundred" → 52 × 100 = 5200
            const n = prefixVal * 100
            if (n > 0 && n < 100_000_000) return { amount: n, currency: 'INR' }
          }
        }
      }

      const n = parseWordNumber(phrase)
      if (n > 0 && n < 100_000_000) return { amount: n, currency: 'INR' }
    }
  }

  // 6. Bare numeric (>= 50, context-sensitive)
  //    Only extract if near an expense-related keyword
  const expenseKeywords = /spent|paid|pay|spend|add|split|bill|cost|bought|liya|kharcha|paisa/i
  if (expenseKeywords.test(t)) {
    const bareRe = /\b(\d{2,}(?:,\d{3})*(?:\.\d{1,2})?)\b/g
    let bestMatch: number | null = null
    let mBare: RegExpExecArray | null
    while ((mBare = bareRe.exec(t)) !== null) {
      const n = parseFloat(mBare[1].replace(/,/g, ''))
      if (n >= 50 && n < 100_000_000) {
        bestMatch = n
        break
      }
    }
    if (bestMatch !== null) return { amount: bestMatch, currency: 'INR' }
  }

  return null
}

// ─── Member extraction ────────────────────────────────────────────────────────
export function extractMember(
  text: string,
  context: Pick<VoiceFlatContext, 'members'>
): { nickname: string; uid: string } | null {
  const t = text.toLowerCase()

  for (const m of context.members) {
    const name = m.nickname.toLowerCase()
    // Exact whole-word match (avoid "said" matching "Sai")
    if (new RegExp(`\\b${name}\\b`).test(t)) return { nickname: m.nickname, uid: m.uid }
  }

  // Fuzzy — allow 1-char edit distance per 4 chars of name
  let best: { nickname: string; uid: string; score: number } | null = null

  for (const m of context.members) {
    const name     = m.nickname.toLowerCase()
    const words    = t.split(/\s+/)
    const maxDist  = Math.max(1, Math.floor(name.length / 4))

    for (const word of words) {
      if (Math.abs(word.length - name.length) > maxDist + 1) continue
      const dist = levenshtein(name, word)
      if (dist <= maxDist) {
        const score = 1 - dist / name.length
        if (!best || score > best.score) {
          best = { nickname: m.nickname, uid: m.uid, score }
        }
      }
    }
  }

  return best ? { nickname: best.nickname, uid: best.uid } : null
}

// ─── Task extraction ──────────────────────────────────────────────────────────
export function extractTask(
  text: string,
  context: Pick<VoiceFlatContext, 'tasks'>
): { name: string; id: string } | null {
  const t = text.toLowerCase()

  // Exact substring first
  for (const task of context.tasks) {
    if (t.includes(task.name.toLowerCase())) return { name: task.name, id: task.id }
  }

  // Fuzzy word-level match
  let best: { name: string; id: string; score: number } | null = null

  for (const task of context.tasks) {
    const taskWords = task.name.toLowerCase().split(/\s+/)
    const textWords = t.split(/\s+/)
    let totalScore  = 0

    for (const tw of taskWords) {
      if (tw.length < 3) continue
      let wordBest = 0
      for (const wd of textWords) {
        const sim = 1 - levenshtein(tw, wd) / Math.max(tw.length, wd.length)
        if (sim > wordBest) wordBest = sim
      }
      totalScore += wordBest
    }

    const avg = totalScore / Math.max(1, taskWords.filter(w => w.length >= 3).length)
    if (avg >= 0.72 && (!best || avg > best.score)) {
      best = { name: task.name, id: task.id, score: avg }
    }
  }

  return best ? { name: best.name, id: best.id } : null
}

// ─── Category detection ───────────────────────────────────────────────────────
const CATEGORY_KEYWORDS: Record<ExpenseCategory, string[]> = {
  rent:        ['rent', 'house rent', 'monthly rent'],
  electricity: ['electricity', 'electric bill', 'light bill', 'current bill', 'eb'],
  water:       ['water', 'water bill'],
  internet:    ['internet', 'wifi', 'broadband', 'jio', 'airtel'],
  gas:         ['gas', 'cylinder', 'lpg', 'cooking gas'],
  maid:        ['maid', 'bai', 'cleaner', 'housekeeper'],
  cook:        ['cook', 'chef', 'cooking'],
  gym:         ['gym', 'fitness', 'workout'],
  grocery:     ['grocery', 'groceries', 'vegetables', 'veggie', 'sabzi', 'kirana', 'dmart', 'bigbasket'],
  milk:        ['milk', 'dudh', 'dairy'],
  ac:          ['ac', 'air conditioner', 'aircon', 'ac bill'],
  maintenance: ['maintenance', 'repair', 'plumber', 'electrician', 'carpenter'],
  food:        ['food', 'dinner', 'lunch', 'breakfast', 'swiggy', 'zomato', 'restaurant', 'pizza', 'khana'],
  household:   ['household', 'house', 'cleaning supplies', 'detergent', 'soap'],
  other:       [],
}

export function detectCategory(text: string): ExpenseCategory {
  const t = text.toLowerCase()
  for (const [cat, keywords] of Object.entries(CATEGORY_KEYWORDS) as [ExpenseCategory, string[]][]) {
    if (cat === 'other') continue
    if (keywords.some(k => t.includes(k))) return cat
  }
  return 'other'
}

// ─── Description extraction ───────────────────────────────────────────────────
// Strips known entities from the transcript and returns the meaningful remainder.
function extractDescription(
  text: string,
  entities: Partial<ExtractedEntities>
): string | undefined {
  let cleaned = text.toLowerCase()

  // Remove currency expressions
  cleaned = cleaned.replace(/(?:₹|rs\.?|rupees?|rupay|inr)\s*\d[\d,.]*/gi, '')
  cleaned = cleaned.replace(/\d[\d,.]*\s*(?:rs\.?|rupees?|rupay|bucks|k\b|lakh|lac)/gi, '')
  cleaned = cleaned.replace(/\b\d{2,}\b/g, '')

  // Remove member name
  if (entities.member) {
    cleaned = cleaned.replace(new RegExp(entities.member.toLowerCase(), 'gi'), '')
  }

  // Remove task name
  if (entities.task) {
    cleaned = cleaned.replace(new RegExp(entities.task.toLowerCase(), 'gi'), '')
  }

  // Remove common filler words
  const fillers = [
    'done','completed','finished','add','spent','paid','split','share','for',
    'on','i','we','the','a','an','is','was','have','had','do','did','will',
    'would','can','could','should','my','our','it','that','this','to','from',
    'with','and','or','but','so','then','please','just','okay','ok','yep','yes',
    'some','also','too','karo','kar','do','de','dena','lena','gaya','kiya',
  ]
  const fillerRe = new RegExp(`\\b(${fillers.join('|')})\\b`, 'gi')
  cleaned = cleaned.replace(fillerRe, ' ')

  return cleaned.trim().replace(/\s+/g, ' ').slice(0, 100) || undefined
}

// ─── Full entity extraction ────────────────────────────────────────────────────
export function extractEntities(text: string, context: VoiceFlatContext): ExtractedEntities {
  const entities: ExtractedEntities = {}
  const t = text.toLowerCase()

  // Amount
  const amountResult = extractAmount(text)
  if (amountResult) {
    entities.amount   = amountResult.amount
    entities.currency = amountResult.currency
  }

  // Member
  const memberResult = extractMember(text, context)
  if (memberResult) {
    entities.member    = memberResult.nickname
    entities.memberUid = memberResult.uid
  }

  // Task
  const taskResult = extractTask(text, context)
  if (taskResult) {
    entities.task   = taskResult.name
    entities.taskId = taskResult.id
  }

  // Category (for expenses)
  entities.category = detectCategory(text)

  // Frequency
  if (/\b(?:daily|every\s*day|each\s*day|roz)\b/i.test(t))          entities.frequency = 'daily'
  else if (/\b(?:weekly|every\s*week|each\s*week|har\s*hafte)\b/i.test(t)) entities.frequency = 'weekly'
  else if (/\b(?:biweekly|fortnightly|every\s*two\s*weeks)\b/i.test(t))    entities.frequency = 'fortnightly'
  else if (/\b(?:monthly|every\s*month|each\s*month|har\s*mahine)\b/i.test(t)) entities.frequency = 'monthly'

  // Split type
  if (/\b(?:equally|equal|everyone|all\s*of\s*us|all\s*members|sab|sab\s*mein|barabar)\b/i.test(t)) {
    entities.splitType = 'equal'
    entities.allMembers = true
  } else if (/\b(?:split\s*with|share\s*with|divide\s*with|with)\b/i.test(t) && entities.member) {
    entities.splitType = 'equal'
  }

  // Date (relative)
  const today = new Date()
  if (/\b(?:today|aaj)\b/i.test(t)) {
    entities.date = today.toISOString().split('T')[0]
  } else if (/\b(?:yesterday)\b/i.test(t)) {
    const d = new Date(today); d.setDate(d.getDate() - 1)
    entities.date = d.toISOString().split('T')[0]
  } else if (/\b(?:tomorrow)\b/i.test(t)) {
    const d = new Date(today); d.setDate(d.getDate() + 1)
    entities.date = d.toISOString().split('T')[0]
  } else if (/\b(?:kal)\b/i.test(t)) {
    // "kal" = yesterday OR tomorrow in Hindi — default to yesterday
    const d = new Date(today); d.setDate(d.getDate() - 1)
    entities.date = d.toISOString().split('T')[0]
  }

  // Description (what remains after removing known entities)
  entities.description = extractDescription(text, entities)

  return entities
}
