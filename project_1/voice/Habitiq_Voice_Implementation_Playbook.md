# Habitiq Voice Assistant — Implementation Playbook for Claude Code

> **Document Type:** Step-by-Step Build Guide  
> **Scope:** Voice assistant integration into existing Habitiq PWA (Next.js 16 + Firebase)  
> **Prerequisites:** Habitiq v0.1.0 core app running locally  
> **Target:** Production-ready voice feature, 90%+ accuracy, < 2s end-to-end latency  
> **For:** Claude Code — file-by-file implementation instructions  

---

## Table of Contents

1. [Pre-Build Checklist](#1-pre-build-checklist)
2. [Project Structure](#2-project-structure)
3. [Phase 1: Core Speech Hook](#3-phase-1-core-speech-hook)
4. [Phase 2: NLU Engine](#4-phase-2-nlu-engine)
5. [Phase 3: Action Execution](#5-phase-3-action-execution)
6. [Phase 4: UI Components](#6-phase-4-ui-components)
7. [Phase 5: Integration & Polish](#7-phase-5-integration--polish)
8. [Testing & QA](#8-testing--qa)
9. [Deployment Checklist](#9-deployment-checklist)

---

## 1. Pre-Build Checklist

Before writing any voice code, verify these exist in the current codebase:

| Check | Location | Status |
|-------|----------|--------|
| Firebase Auth context | `contexts/AuthContext.tsx` or similar | ☐ |
| Flat data hook | `hooks/useFlat.ts` or `useCurrentFlat()` | ☐ |
| Task completion API | `lib/api/tasks.ts` or Firestore direct | ☐ |
| Expense creation API | `lib/api/expenses.ts` or Firestore direct | ☐ |
| Member list available | `flat.members` array with `displayName` | ☐ |
| Task list available | `flat.tasks` array with `name`, `assignedTo` | ☐ |
| Balance data available | `flat.balances` or computed from expenses | ☐ |
| Navigation bar component | `components/Navigation.tsx` or `BottomNav.tsx` | ☐ |
| Settings page/route | `/settings` or `/nest/settings` | ☐ |
| Zustand store | `store/` directory with state management | ☐ |

**If any are missing:** Build the missing core feature first. Voice cannot exist without flat data.

---

## 2. Project Structure

Create this directory tree under the existing project root:

```
lib/
  voice/
    nlu/
      intentClassifier.ts      # Intent classification engine
      entityExtractor.ts       # Entity extraction (amount, member, task)
      contextResolver.ts       # Flat context cache
    actions/
      actionRouter.ts          # Route intent → action
      completeTask.ts          # COMPLETE_TASK action
      createExpense.ts         # CREATE_EXPENSE action
      queryBalance.ts          # QUERY_BALANCE action
      queryTasks.ts            # QUERY_TASKS action
      queryStatus.ts           # QUERY_STATUS action
      requestSwap.ts           # REQUEST_SWAP action
      createTask.ts            # CREATE_TASK action
      greeting.ts              # GREETING action
      reject.ts                # REJECT action (false positives)
      unknown.ts               # UNKNOWN fallback
    response/
      responseFormatter.ts     # Format text + card responses
    tts/
      speechSynthesis.ts     # Text-to-speech engine
    permissions.ts             # Microphone permission handler
    types.ts                   # Shared voice types
    constants.ts               # Number word maps, thresholds
    utils.ts                   # Levenshtein, fuzzy match, etc.

hooks/
  useVoiceAssistant.ts         # Main voice hook (SpeechRecognition)
  useVoiceContext.ts           # Flat context for voice

components/
  voice/
    VoiceButton.tsx            # Mic button (nav center)
    VoiceOverlay.tsx           # Listening overlay
    WaveformVisualizer.tsx     # Canvas waveform animation
    VoiceResponseCard.tsx      # Response card (slide-up)
    VoiceFallbackModal.tsx     # iOS text-input fallback
    VoiceSettings.tsx          # Settings panel in Nest

__tests__/
  voice/
    intentClassifier.test.ts   # 500+ test cases
    entityExtractor.test.ts    # Amount, member, task extraction
    actionRouter.test.ts       # End-to-end action tests
    useVoiceAssistant.test.ts  # Hook behavior tests
```

---

## 3. Phase 1: Core Speech Hook

**Goal:** `useVoiceAssistant.ts` — browser speech recognition with iOS fallback.

**Estimated time:** 2 hours  
**Files to create:** 3  
**Dependencies:** None (Web Speech API is native)

### Step 3.1: Create `lib/voice/types.ts`

```typescript
export interface VoiceState {
  isListening: boolean;
  isProcessing: boolean;
  transcript: string;
  interimTranscript: string;
  confidence: number;
  error: VoiceError | null;
}

export interface VoiceError {
  code: 'not-supported' | 'no-speech' | 'audio-capture' | 'network' | 'not-allowed' | 'aborted';
  message: string;
  recoverable: boolean;
}

export interface VoiceResult {
  transcript: string;
  confidence: number;
  isFinal: boolean;
}

export interface UseVoiceAssistantReturn {
  state: VoiceState;
  startListening: () => void;
  stopListening: () => void;
  isSupported: boolean;
  lastResult: VoiceResult | null;
}

export type IntentType = 
  | 'COMPLETE_TASK'
  | 'CREATE_EXPENSE'
  | 'QUERY_BALANCE'
  | 'QUERY_TASKS'
  | 'QUERY_STATUS'
  | 'REQUEST_SWAP'
  | 'CREATE_TASK'
  | 'GREETING'
  | 'REJECT'
  | 'UNKNOWN';

export interface ExtractedEntities {
  amount?: number;
  currency?: 'INR' | 'USD' | 'EUR' | 'GBP';
  description?: string;
  task?: string;
  member?: string;
  frequency?: 'daily' | 'weekly' | 'fortnightly' | 'monthly';
  splitType?: 'equal' | 'custom' | 'percentage';
  date?: Date;
  allMembers?: boolean;
}

export interface VoiceAction {
  type: IntentType;
  entities: ExtractedEntities;
  confidence: number;
  originalTranscript: string;
}

export interface ActionResult {
  success: boolean;
  action: string;
  message: string;
  data?: any;
  followUp?: string;
  error?: string;
}

export interface VoiceResponse {
  text: string;
  speak: boolean;
  card?: ResponseCard;
  followUp?: string;
}

export interface ResponseCard {
  type: 'task-complete' | 'expense-added' | 'balance-summary' | 'task-list' | 'error' | 'info';
  title: string;
  subtitle?: string;
  primaryAction?: { label: string; action: string };
  secondaryAction?: { label: string; action: string };
  data?: any;
}
```

### Step 3.2: Create `hooks/useVoiceAssistant.ts`

**Copy the full implementation from the Voice Assistant Specification, Section 3.2.**

**Key requirements:**
- Use `en-IN` language code for Indian English accent support
- Set `maxAlternatives: 3` for better recognition
- Implement 10-second hard timeout
- Implement 1.5-second silence auto-stop after final result
- Never store audio — only transcript
- Handle all 6 SpeechRecognition error codes

### Step 3.3: Create `components/voice/VoiceFallbackModal.tsx`

**Copy the full implementation from the Voice Assistant Specification, Section 3.3.**

**Key requirements:**
- Show only when `!isSupported` (iOS Safari)
- Auto-focus input on open
- Submit on Enter key
- Match voice UI styling (dark theme, rounded cards)
- Dismiss on backdrop tap

**STOP POINT:** Test on Android Chrome and iOS Safari. Verify:
- [ ] Android: Mic opens, listens, returns transcript
- [ ] iOS: Fallback modal opens, text input works
- [ ] Both: 10-second timeout fires
- [ ] Both: Silence auto-stop works

---

## 4. Phase 2: NLU Engine

**Goal:** Intent classification + entity extraction → 90%+ accuracy.

**Estimated time:** 4 hours  
**Files to create:** 5  
**Dependencies:** Phase 1 complete

### Step 4.1: Create `lib/voice/constants.ts`

```typescript
// Number word maps for amount extraction
export const ENGLISH_NUMBER_WORDS: Record<string, number> = {
  'zero': 0, 'one': 1, 'two': 2, 'three': 3, 'four': 4, 'five': 5,
  'six': 6, 'seven': 7, 'eight': 8, 'nine': 9, 'ten': 10,
  'eleven': 11, 'twelve': 12, 'thirteen': 13, 'fourteen': 14, 'fifteen': 15,
  'sixteen': 16, 'seventeen': 17, 'eighteen': 18, 'nineteen': 19,
  'twenty': 20, 'thirty': 30, 'forty': 40, 'fifty': 50,
  'sixty': 60, 'seventy': 70, 'eighty': 80, 'ninety': 90,
  'hundred': 100, 'thousand': 1000, 'lakh': 100000, 'lac': 100000,
  'million': 1000000, 'crore': 10000000
};

export const HINGLISH_NUMBER_WORDS: Record<string, number> = {
  'ek': 1, 'do': 2, 'teen': 3, 'char': 4, 'paanch': 5,
  'chhe': 6, 'saat': 7, 'aath': 8, 'nau': 9, 'das': 10,
  'gyarah': 11, 'barah': 12, 'terah': 13, 'chaudah': 14, 'pandrah': 15,
  'solah': 16, 'satrah': 17, 'atharah': 18, 'unnis': 19,
  'bees': 20, 'tees': 30, 'chalis': 40, 'pachas': 50,
  'saath': 60, 'sattar': 70, 'assi': 80, 'nabbe': 90,
  'sau': 100, 'hazaar': 1000, 'lakh': 100000, 'karod': 10000000
};

export const TELUGU_ENGLISH_NUMBER_WORDS: Record<string, number> = {
  'okati': 1, 'rendu': 2, 'moodu': 3, 'naalugu': 4, 'aidu': 5,
  'aaru': 6, 'eedu': 7, 'enimidi': 8, 'tommidi': 9, 'padi': 10,
  'padakondi': 11, 'panendu': 12, 'padamudu': 13, 'padaharu': 14, 'padiheenu': 15,
  'padaharu': 16, 'padakondi': 17, 'padenimidi': 18, 'patommidi': 19,
  'iravai': 20, 'muppayi': 30, 'nalabai': 40, 'yabhai': 50,
  'aravai': 60, 'debbai': 70, 'enabai': 80, 'tombai': 90,
  'vandala': 100, 'veyyi': 1000, 'laksha': 100000, 'koti': 10000000
};

// Fuzzy match threshold
export const FUZZY_THRESHOLD = 0.75;

// Minimum flat size for full event weight
export const MIN_FLAT_SIZE_FOR_FULL_WEIGHT = 3;

// Reciprocal weight for 2-person flats
export const RECIPROCAL_WEIGHT = 0.5;

// Time decay lambda (half-life ~230 days)
export const TIME_DECAY_LAMBDA = 0.003;
```

### Step 4.2: Create `lib/voice/utils.ts`

```typescript
export function levenshteinDistance(a: string, b: string): number {
  const matrix = Array(b.length + 1).fill(null).map(() => Array(a.length + 1).fill(null));
  for (let i = 0; i <= a.length; i++) matrix[0][i] = i;
  for (let j = 0; j <= b.length; j++) matrix[j][0] = j;
  for (let j = 1; j <= b.length; j++) {
    for (let i = 1; i <= a.length; i++) {
      const indicator = a[i - 1] === b[j - 1] ? 0 : 1;
      matrix[j][i] = Math.min(
        matrix[j][i - 1] + 1,
        matrix[j - 1][i] + 1,
        matrix[j - 1][i - 1] + indicator
      );
    }
  }
  return matrix[b.length][a.length];
}

export function fuzzyMatch(text: string, pattern: string, threshold: number = 0.75): boolean {
  const normalizedText = text.toLowerCase().replace(/[^a-z0-9\s]/g, '');
  const normalizedPattern = pattern.toLowerCase().replace(/[^a-z0-9\s]/g, '');

  if (normalizedText.includes(normalizedPattern)) return true;

  const textWords = normalizedText.split(/\s+/);
  const patternWords = normalizedPattern.split(/\s+/);

  for (const pw of patternWords) {
    let matched = false;
    for (const tw of textWords) {
      const similarity = 1 - (levenshteinDistance(pw, tw) / Math.max(pw.length, tw.length));
      if (similarity >= threshold) {
        matched = true;
        break;
      }
    }
    if (!matched) return false;
  }
  return true;
}

export function findBestFuzzyMatch(text: string, candidates: string[], threshold: number): string | null {
  let bestMatch: string | null = null;
  let bestScore = 0;

  for (const candidate of candidates) {
    if (text.includes(candidate.toLowerCase())) return candidate;

    const textWords = text.split(/\s+/);
    const candidateWords = candidate.toLowerCase().split(/\s+/);
    let totalScore = 0;

    for (const cw of candidateWords) {
      let wordBestScore = 0;
      for (const tw of textWords) {
        const dist = levenshteinDistance(cw, tw);
        const similarity = 1 - (dist / Math.max(cw.length, tw.length));
        if (similarity > wordBestScore) wordBestScore = similarity;
      }
      totalScore += wordBestScore;
    }

    const avgScore = totalScore / candidateWords.length;
    if (avgScore > bestScore && avgScore >= threshold) {
      bestScore = avgScore;
      bestMatch = candidate;
    }
  }

  return bestMatch;
}

export function parseNumberWords(
  text: string, 
  wordMap: Record<string, number>
): number | null {
  const words = text.toLowerCase().match(
    new RegExp(`\b(?:${Object.keys(wordMap).join('|')})\b`, 'g')
  );
  if (!words) return null;

  let total = 0;
  let current = 0;

  for (const word of words) {
    const val = wordMap[word];
    if (val >= 100) {
      if (current === 0) current = 1;
      current *= val;
      total += current;
      current = 0;
    } else {
      current += val;
    }
  }

  return total + current;
}

export function daysBetween(a: Date, b: Date): number {
  return Math.floor((b.getTime() - a.getTime()) / (1000 * 60 * 60 * 24));
}

export function isToday(date: Date): boolean {
  const now = new Date();
  return date.getDate() === now.getDate() && 
         date.getMonth() === now.getMonth() && 
         date.getFullYear() === now.getFullYear();
}
```

### Step 4.3: Create `lib/voice/nlu/intentClassifier.ts`

**Copy the full implementation from the Voice Assistant Specification, Section 4.1.**

**Then apply the Corpus Expansion Patch v2.1:**
- Add all REJECT patterns (Sections 1.1–1.6)
- Add all REQUEST_SWAP indirect patterns (Sections 2.2–2.6)
- Add all COMPLETE_TASK result-state patterns (Sections 4.1–4.4)
- Add all QUERY_BALANCE slang patterns (Sections 5.1–5.4)
- Add all QUERY_STATUS presence patterns (Sections 6.1–6.4)
- Add all CREATE_TASK shorthand patterns (Sections 7.1–7.3)

**STOP POINT:** Run the test suite. Verify:
- [ ] 500+ test cases pass
- [ ] UNKNOWN rate < 15% on test corpus
- [ ] REJECT intent catches 90%+ of false positives

### Step 4.4: Create `lib/voice/nlu/entityExtractor.ts`

**Copy the full implementation from the Voice Assistant Specification, Section 4.2.**

**Then apply the Corpus Expansion Patch v2.1, Section 3:**
- Add `parseNumberWords` calls for English, Hinglish, Telugu
- Add bare-amount regex patterns
- Add amount confirmation flow for number-word fallback

**STOP POINT:** Run entity extraction tests. Verify:
- [ ] "five hundred" → 500
- [ ] "paanch sau" → 500
- [ ] "aidu vandala" → 500
- [ ] "I spent 500 on groceries" → amount: 500
- [ ] "Maine 500 diye" → amount: 500
- [ ] "500 icha" → amount: 500

### Step 4.5: Create `lib/voice/nlu/contextResolver.ts`

**Copy the full implementation from the Voice Assistant Specification, Section 4.3.**

**Key requirements:**
- Cache flat context for 30 seconds (TTL)
- Include: members (with nicknames), tasks, balances, current user
- Refresh asynchronously on cache miss
- Never block voice flow on context load

---

## 5. Phase 3: Action Execution

**Goal:** Execute intents against real Firestore data.

**Estimated time:** 3 hours  
**Files to create:** 9  
**Dependencies:** Phase 2 complete

### Step 5.1: Create `lib/voice/actions/actionRouter.ts`

**Copy the full implementation from the Voice Assistant Specification, Section 5.1.**

### Step 5.2: Create Individual Action Files

For each action, create a file in `lib/voice/actions/`:

| File | Source | Key Requirement |
|------|--------|---------------|
| `completeTask.ts` | Spec Section 5.2 | Disambiguation when multiple tasks match |
| `createExpense.ts` | Spec Section 5.3 | Confirmation for amounts > ₹5,000 |
| `queryBalance.ts` | Spec Section 5.4 | Simplified settlement suggestions |
| `queryTasks.ts` | Spec Section 5.5 | Overdue task highlighting |
| `queryStatus.ts` | Spec Section 5.6 (implied) | OOS return dates |
| `requestSwap.ts` | Spec Section 5.6 | Distressed user templates |
| `createTask.ts` | Spec Section 5.6 | Frequency validation |
| `greeting.ts` | Spec Section 5.6 | Contextual task count |
| `reject.ts` | Patch Section 8.1 | Redirect to valid commands |
| `unknown.ts` | Spec Section 5.6 | Random suggestion rotation |

**STOP POINT:** Test each action with mock flat data. Verify:
- [ ] COMPLETE_TASK: Finds task by fuzzy name, marks complete in Firestore
- [ ] CREATE_EXPENSE: Creates expense document with correct splits
- [ ] QUERY_BALANCE: Reads from `flat.balances` or computes from expenses
- [ ] QUERY_TASKS: Filters by `assignedTo === currentUser.userId`
- [ ] REQUEST_SWAP: Creates swap request document in Firestore
- [ ] All actions: Return `ActionResult` with `message` and optional `data`

---

## 6. Phase 4: UI Components

**Goal:** Voice button, overlay, waveform, response card, settings.

**Estimated time:** 3 hours  
**Files to create:** 6  
**Dependencies:** Phase 3 complete

### Step 6.1: Create `components/voice/WaveformVisualizer.tsx`

**Copy the full implementation from the Voice Assistant Specification, Section 7.2.**

**Key requirements:**
- Canvas-based, 20 bars
- Violet-to-pink gradient
- Real-time animation via `requestAnimationFrame`
- 60 FPS target
- Cleanup on unmount

### Step 6.2: Create `components/voice/VoiceOverlay.tsx`

**Specs:**
- Fixed full-screen overlay, `z-index: 50`
- Backdrop: `bg-black/60 backdrop-blur-sm`
- Center content: "Listening..." + interim transcript + waveform + stop button
- Auto-dismiss on `stopListening()`
- Prevent body scroll when open

### Step 6.3: Create `components/voice/VoiceResponseCard.tsx`

**Copy the full implementation from the Voice Assistant Specification, Section 7.3.**

**Key requirements:**
- Slide-up from bottom (`bottom: 96px` to clear nav)
- Dark card: `bg-[--s2] border border-[--bd] rounded-2xl`
- Header: sparkle icon + "Habitiq Assistant" + X dismiss
- Body: response text + optional action card
- Footer: primary action button + dismiss button
- Auto-dismiss after 8 seconds (unless tapped)
- Swipe down to dismiss

### Step 6.4: Create `components/voice/VoiceButton.tsx`

**Copy the full implementation from the Voice Assistant Specification, Section 7.1.**

**Key requirements:**
- Center nav position (replaces or overlays the "Log" button)
- Three states: idle (violet), listening (red), processing (amber)
- Pulse animation when listening
- Recording dot indicator
- Long-press (500ms) opens manual Log bottom sheet
- Short tap triggers voice
- ARIA label: "Activate voice assistant"

### Step 6.5: Create `components/voice/VoiceSettings.tsx`

**Copy the full implementation from the Voice Assistant Specification, Section 7.4.**

**Key requirements:**
- Toggle: Voice Assistant (enable/disable)
- Toggle: Speak Responses (TTS on/off)
- Toggle: Show Transcript (display what user said)
- Help card: 5 example commands
- Store preferences in `localStorage`
- Nest tab integration: `/nest/settings/voice`

**STOP POINT:** Visual QA. Verify:
- [ ] Button matches dark theme (warm charcoal, not cold blue)
- [ ] Overlay feels native (spring animation, not linear)
- [ ] Response card is readable on small screens (320px width)
- [ ] Settings panel follows existing settings pattern

---

## 7. Phase 5: Integration & Polish

**Goal:** Wire voice into existing navigation, add TTS, permissions, analytics.

**Estimated time:** 2 hours  
**Files to modify:** 3  
**Dependencies:** Phase 4 complete

### Step 7.1: Modify Navigation Bar

**File:** `components/Navigation.tsx` or `BottomNav.tsx`

```typescript
// Replace center "Log" button with VoiceButton
// Keep long-press behavior for manual Log sheet
// Add conditional: if voiceEnabled (from settings), show mic; else show +

const [voiceEnabled] = useLocalStorage('voice-enabled', true);

// Center slot:
{voiceEnabled ? <VoiceButton /> : <LogButton />}
```

### Step 7.2: Create `lib/voice/tts/speechSynthesis.ts`

**Copy the full implementation from the Voice Assistant Specification, Section 6.2.**

**Key requirements:**
- Voice priority: `en-IN` female > `en-IN` > `en-GB` female > `en-GB` > `en-US`
- Rate: 1.2x (faster = less battery)
- Pitch: 0.9 (slightly deeper, calmer)
- Auto-cancel previous speech before new utterance
- Skip if `speak: false` in response
- Skip if user disabled TTS in settings

### Step 7.3: Create `lib/voice/permissions.ts`

**Copy the full implementation from the Voice Assistant Specification, Section 9.2.**

**Key requirements:**
- Check `navigator.permissions.query({ name: 'microphone' })`
- Show custom modal explaining WHY microphone is needed
- Never use browser's default permission prompt (too vague)
- Handle 'denied', 'prompt', and 'granted' states
- Store permission state in memory (not persistent)

### Step 7.4: Add Voice Analytics (Privacy-First)

**File:** `lib/voice/analytics.ts`

```typescript
// Log ONLY intent type + success/failure — never the transcript
export function logVoiceEvent(event: {
  intent: IntentType;
  success: boolean;
  latencyMs: number;
  errorCode?: string;
}) {
  // Send to Firebase Analytics or your analytics endpoint
  // Hash the transcript if you must log it: SHA-256, no reverse lookup
}
```

### Step 7.5: Add Contextual Awareness

**File:** `hooks/useVoiceContext.ts`

```typescript
// Detect current route/screen to set default intent
export function useVoiceContext() {
  const pathname = usePathname();

  if (pathname.includes('/duties')) return { defaultIntent: 'COMPLETE_TASK' };
  if (pathname.includes('/money')) return { defaultIntent: 'CREATE_EXPENSE' };
  if (pathname.includes('/nest')) return { defaultIntent: 'QUERY_STATUS' };
  return { defaultIntent: 'QUERY_TASKS' };
}
```

**STOP POINT:** Full integration test. Verify:
- [ ] Voice button appears in nav bar
- [ ] Tap opens listening overlay
- [ ] Say "kitchen done" → task completes → response card shows → TTS speaks
- [ ] Say "I spent 500 on groceries" → expense created → response card shows
- [ ] Say "how much does Bhanu owe me" → balance shown → TTS speaks
- [ ] iOS fallback: text input works, same flow
- [ ] Settings: toggles persist in localStorage

---

## 8. Testing & QA

### 8.1 Unit Test File: `__tests__/voice/intentClassifier.test.ts`

```typescript
import { classifyIntent } from '@/lib/voice/nlu/intentClassifier';

describe('Intent Classification', () => {
  const testCases = [
    // COMPLETE_TASK — 50 cases
    { input: 'kitchen done', expected: 'COMPLETE_TASK' },
    { input: 'bathroom ho gaya', expected: 'COMPLETE_TASK' },
    { input: 'kitchen aipoyindi', expected: 'COMPLETE_TASK' },
    { input: 'kitchen is clean', expected: 'COMPLETE_TASK' },
    { input: 'dishes are washed', expected: 'COMPLETE_TASK' },

    // CREATE_EXPENSE — 50 cases
    { input: 'i spent 500 on groceries', expected: 'CREATE_EXPENSE' },
    { input: 'maine 500 diye groceries pe', expected: 'CREATE_EXPENSE' },
    { input: '500 icha groceries ki', expected: 'CREATE_EXPENSE' },
    { input: 'five hundred on dinner', expected: 'CREATE_EXPENSE' },
    { input: 'paanch sau diye', expected: 'CREATE_EXPENSE' },

    // QUERY_BALANCE — 30 cases
    { input: 'how much does bhanu owe me', expected: 'QUERY_BALANCE' },
    { input: 'mera balance kitna hai', expected: 'QUERY_BALANCE' },
    { input: 'na balance entha', expected: 'QUERY_BALANCE' },
    { input: 'show me the money', expected: 'QUERY_BALANCE' },
    { input: 'paisa kitna hai', expected: 'QUERY_BALANCE' },

    // QUERY_TASKS — 20 cases
    { input: 'what are my tasks', expected: 'QUERY_TASKS' },
    { input: 'aaj kya karna hai', expected: 'QUERY_TASKS' },
    { input: 'na tasks enti', expected: 'QUERY_TASKS' },

    // QUERY_STATUS — 15 cases
    { input: 'who is home', expected: 'QUERY_STATUS' },
    { input: 'kaun ghar pe hai', expected: 'QUERY_STATUS' },
    { input: 'evaru intlo unnaru', expected: 'QUERY_STATUS' },

    // REQUEST_SWAP — 20 cases
    { input: 'can someone cover my task', expected: 'REQUEST_SWAP' },
    { input: 'main busy hoon', expected: 'REQUEST_SWAP' },
    { input: 'nenu busy', expected: 'REQUEST_SWAP' },
    { input: 'i am sick', expected: 'REQUEST_SWAP' },
    { input: 'meri madad karo', expected: 'REQUEST_SWAP' },

    // CREATE_TASK — 15 cases
    { input: 'add kitchen daily', expected: 'CREATE_TASK' },
    { input: 'naya task kitchen daily', expected: 'CREATE_TASK' },

    // GREETING — 10 cases
    { input: 'hi', expected: 'GREETING' },
    { input: 'hello', expected: 'GREETING' },
    { input: 'namaste', expected: 'GREETING' },

    // REJECT — 20 cases
    { input: 'what time is it', expected: 'REJECT' },
    { input: 'play music', expected: 'REJECT' },
    { input: 'call mom', expected: 'REJECT' },
    { input: 'set alarm', expected: 'REJECT' },
    { input: 'samay kya hai', expected: 'REJECT' },
    { input: 'gaana bajao', expected: 'REJECT' },
    { input: 'call chey', expected: 'REJECT' },
    { input: 'alarm pettu', expected: 'REJECT' },

    // UNKNOWN — 10 cases
    { input: 'random nonsense here', expected: 'UNKNOWN' },
    { input: 'xyz abc 123', expected: 'UNKNOWN' },
  ];

  testCases.forEach(({ input, expected }) => {
    it(`classifies "${input}" as ${expected}`, () => {
      expect(classifyIntent(input)).toBe(expected);
    });
  });
});
```

### 8.2 Performance Test: `__tests__/voice/performance.test.ts`

```typescript
describe('Voice Performance', () => {
  it('classifies intent in < 10ms', () => {
    const start = performance.now();
    classifyIntent('kitchen done');
    const end = performance.now();
    expect(end - start).toBeLessThan(10);
  });

  it('extracts entities in < 20ms', () => {
    const start = performance.now();
    extractEntities('i spent 500 on groceries', mockContext);
    const end = performance.now();
    expect(end - start).toBeLessThan(20);
  });

  it('end-to-end action in < 100ms', async () => {
    const start = performance.now();
    await executeAction(mockAction, mockContext);
    const end = performance.now();
    expect(end - start).toBeLessThan(100);
  });
});
```

### 8.3 QA Checklist (Manual)

| Test | Android Chrome | iOS Safari | Desktop Chrome |
|------|---------------|------------|----------------|
| Mic button visible | ☐ | ☐ | ☐ |
| Tap opens listening | ☐ | ☐ | ☐ |
| "Kitchen done" completes task | ☐ | ☐ | ☐ |
| "I spent 500 on groceries" adds expense | ☐ | ☐ | ☐ |
| "How much does Bhanu owe me" shows balance | ☐ | ☐ | ☐ |
| "What are my tasks" lists tasks | ☐ | ☐ | ☐ |
| "Who is home" shows status | ☐ | ☐ | ☐ |
| "Can someone cover my task" sends swap | ☐ | ☐ | ☐ |
| "What time is it" → REJECT response | ☐ | ☐ | ☐ |
| "Play music" → REJECT response | ☐ | ☐ | ☐ |
| iOS fallback modal works | N/A | ☐ | N/A |
| TTS speaks response | ☐ | ☐ | ☐ |
| Settings toggles work | ☐ | ☐ | ☐ |
| Long-press opens Log sheet | ☐ | ☐ | ☐ |
| Response card dismisses on swipe | ☐ | ☐ | ☐ |
| 10-second timeout fires | ☐ | ☐ | ☐ |
| Silence auto-stop works | ☐ | ☐ | ☐ |

---

## 9. Deployment Checklist

### Pre-Deploy (Local)
- [ ] All 500+ unit tests pass
- [ ] Intent accuracy > 90% on test corpus
- [ ] UNKNOWN rate < 8%
- [ ] REJECT catches > 95% of false positives
- [ ] End-to-end latency < 2 seconds (speech → action → response)
- [ ] No console errors
- [ ] Bundle size increase < 50 KB gzipped
- [ ] No breaking changes to existing features

### Deploy (Vercel)
- [ ] Deploy to preview branch
- [ ] Test on real Android device (Chrome)
- [ ] Test on real iPhone (Safari)
- [ ] Verify Firebase rules allow voice-related writes
- [ ] Monitor error logs for 24 hours

### Post-Deploy (Monitoring)
- [ ] Voice adoption rate (users who try voice / total active)
- [ ] Intent accuracy (real-world, sampled)
- [ ] Fallback rate (UNKNOWN + manual fallback)
- [ ] Average session length (commands per session)
- [ ] Error rate (mic permission, network, timeout)

---

## Appendix A: File Inventory

| # | File Path | Source Document | Lines (est.) |
|---|-----------|-----------------|--------------|
| 1 | `lib/voice/types.ts` | This playbook | 80 |
| 2 | `lib/voice/constants.ts` | This playbook | 60 |
| 3 | `lib/voice/utils.ts` | This playbook | 120 |
| 4 | `lib/voice/nlu/intentClassifier.ts` | Spec §4.1 + Patch §1-7 | 400 |
| 5 | `lib/voice/nlu/entityExtractor.ts` | Spec §4.2 + Patch §3 | 250 |
| 6 | `lib/voice/nlu/contextResolver.ts` | Spec §4.3 | 80 |
| 7 | `lib/voice/actions/actionRouter.ts` | Spec §5.1 | 50 |
| 8 | `lib/voice/actions/completeTask.ts` | Spec §5.2 | 120 |
| 9 | `lib/voice/actions/createExpense.ts` | Spec §5.3 | 150 |
| 10 | `lib/voice/actions/queryBalance.ts` | Spec §5.4 | 100 |
| 11 | `lib/voice/actions/queryTasks.ts` | Spec §5.5 | 80 |
| 12 | `lib/voice/actions/queryStatus.ts` | Spec §5.6 | 80 |
| 13 | `lib/voice/actions/requestSwap.ts` | Spec §5.6 | 100 |
| 14 | `lib/voice/actions/createTask.ts` | Spec §5.6 | 80 |
| 15 | `lib/voice/actions/greeting.ts` | Spec §5.6 | 60 |
| 16 | `lib/voice/actions/reject.ts` | Patch §8.1 | 40 |
| 17 | `lib/voice/actions/unknown.ts` | Spec §5.6 | 50 |
| 18 | `lib/voice/response/responseFormatter.ts` | Spec §6.1 | 100 |
| 19 | `lib/voice/tts/speechSynthesis.ts` | Spec §6.2 | 80 |
| 20 | `lib/voice/permissions.ts` | Spec §9.2 | 80 |
| 21 | `lib/voice/analytics.ts` | This playbook | 30 |
| 22 | `hooks/useVoiceAssistant.ts` | Spec §3.2 | 150 |
| 23 | `hooks/useVoiceContext.ts` | This playbook | 30 |
| 24 | `components/voice/VoiceButton.tsx` | Spec §7.1 | 150 |
| 25 | `components/voice/VoiceOverlay.tsx` | This playbook | 80 |
| 26 | `components/voice/WaveformVisualizer.tsx` | Spec §7.2 | 80 |
| 27 | `components/voice/VoiceResponseCard.tsx` | Spec §7.3 | 120 |
| 28 | `components/voice/VoiceFallbackModal.tsx` | Spec §3.3 | 80 |
| 29 | `components/voice/VoiceSettings.tsx` | Spec §7.4 | 100 |
| 30 | `__tests__/voice/intentClassifier.test.ts` | This playbook | 80 |
| 31 | `__tests__/voice/performance.test.ts` | This playbook | 40 |
| | **TOTAL** | | **~2,500 lines** |

---

## Appendix B: Quick Reference — Intent → Action Mapping

| User Says | Intent | Action | Firestore Operation |
|-----------|--------|--------|---------------------|
| "Kitchen done" | COMPLETE_TASK | `completeTask.ts` | Update `tasks/{taskId}`: `completed: true`, `completedAt: now()` |
| "I spent 500 on groceries" | CREATE_EXPENSE | `createExpense.ts` | Add `expenses/{expenseId}` with splits |
| "How much does Bhanu owe me?" | QUERY_BALANCE | `queryBalance.ts` | Read `balances` or compute from `expenses` |
| "What are my tasks?" | QUERY_TASKS | `queryTasks.ts` | Read `tasks` where `assignedTo == userId` |
| "Who is home?" | QUERY_STATUS | `queryStatus.ts` | Read `members` where `isOOS == false` |
| "Can someone cover my task?" | REQUEST_SWAP | `requestSwap.ts` | Add `swapRequests/{swapId}` |
| "Add kitchen daily" | CREATE_TASK | `createTask.ts` | Add `tasks/{taskId}` with frequency |
| "Hi" | GREETING | `greeting.ts` | No Firestore operation |
| "What time is it?" | REJECT | `reject.ts` | No Firestore operation |
| "Random nonsense" | UNKNOWN | `unknown.ts` | No Firestore operation |

---

*End of Implementation Playbook*
*Start with Phase 1 (types.ts → useVoiceAssistant.ts → VoiceFallbackModal.tsx)*
*Do not proceed to Phase 2 until Phase 1 STOP POINT is verified.*
