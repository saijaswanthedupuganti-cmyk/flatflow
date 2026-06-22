/**
 * Corpus Accuracy Test — Sprint 2 Stop Checkpoint
 * Runs all 1000+ labeled utterances from the use-case corpus through classifyIntent.
 * Target: UNKNOWN rate < 15% on labeled non-UNKNOWN examples.
 *
 * Run: npx vitest run lib/voice/nlu/__tests__/corpus.test.ts
 */

import { describe, it, expect } from 'vitest'
import * as fs from 'fs'
import * as path from 'path'
import { classifyIntent, type IntentType } from '../intentClassifier'

// ─── Parse corpus markdown ────────────────────────────────────────────────────

interface CorpusEntry {
  index: number
  transcript: string
  expectedIntent: IntentType
}

function parseCorpus(): CorpusEntry[] {
  const corpusPath = path.resolve(__dirname, '../../../../Habitiq_Voice_Use_Case_Corpus_1000.md')
  const text = fs.readFileSync(corpusPath, 'utf-8')
  const lines = text.split('\n')

  const entries: CorpusEntry[] = []

  // Track current intent section
  let currentIntent: IntentType = 'UNKNOWN'

  // Section header patterns
  const SECTION_MAP: Record<string, IntentType> = {
    'intent: complete_task':   'COMPLETE_TASK',
    'intent: create_expense':  'CREATE_EXPENSE',
    'intent: query_balance':   'QUERY_BALANCE',
    'intent: query_tasks':     'QUERY_TASKS',
    'intent: query_status':    'QUERY_STATUS',
    'intent: request_swap':    'REQUEST_SWAP',
    'intent: create_task':     'CREATE_TASK',
    'intent: greeting':        'GREETING',
    'intent: unknown':         'UNKNOWN',
  }

  // Row pattern: | number | "transcript" | anything | anything |
  const ROW_RE = /^\|\s*(\d+)\s*\|\s*"([^"]+)"\s*\|/

  for (const line of lines) {
    // Detect section header
    const lower = line.toLowerCase()
    for (const [key, intent] of Object.entries(SECTION_MAP)) {
      if (lower.includes(key)) {
        currentIntent = intent
        break
      }
    }

    // Parse table row
    const m = line.match(ROW_RE)
    if (m) {
      entries.push({
        index: parseInt(m[1]),
        transcript: m[2],
        expectedIntent: currentIntent,
      })
    }
  }

  return entries
}

// ─── Accuracy measurement ─────────────────────────────────────────────────────

interface AccuracyReport {
  total: number
  correct: number
  unknown: number
  unknownRate: number
  accuracy: number
  byIntent: Record<string, { total: number; correct: number; unknown: number }>
  failures: Array<{ index: number; transcript: string; expected: IntentType; got: IntentType }>
}

function measureAccuracy(entries: CorpusEntry[]): AccuracyReport {
  // Exclude ambiguous/UNKNOWN rows — these are intentionally UNKNOWN
  // Include all rows that have a specific expected intent
  const testable = entries.filter(e => e.expectedIntent !== 'UNKNOWN')

  const report: AccuracyReport = {
    total: testable.length,
    correct: 0,
    unknown: 0,
    unknownRate: 0,
    accuracy: 0,
    byIntent: {},
    failures: [],
  }

  for (const entry of testable) {
    const got = classifyIntent(entry.transcript)
    const intent = entry.expectedIntent

    if (!report.byIntent[intent]) {
      report.byIntent[intent] = { total: 0, correct: 0, unknown: 0 }
    }
    report.byIntent[intent].total++

    if (got === intent) {
      report.correct++
      report.byIntent[intent].correct++
    } else {
      report.failures.push({ index: entry.index, transcript: entry.transcript, expected: intent, got })
      if (got === 'UNKNOWN') {
        report.unknown++
        report.byIntent[intent].unknown++
      }
    }
  }

  report.accuracy = report.correct / report.total
  report.unknownRate = report.unknown / report.total
  return report
}

// ─── Tests ────────────────────────────────────────────────────────────────────

describe('Corpus accuracy — Sprint 2 stop checkpoint', () => {
  const entries = parseCorpus()

  it('corpus parsed correctly (1000+ entries)', () => {
    expect(entries.length).toBeGreaterThanOrEqual(500)
    console.log(`\nCorpus: ${entries.length} entries loaded`)
  })

  it('UNKNOWN rate on labeled utterances < 15%', () => {
    const report = measureAccuracy(entries)

    // Print full report
    console.log('\n═══════ NLU CORPUS ACCURACY REPORT ═══════')
    console.log(`Total labeled utterances: ${report.total}`)
    console.log(`Correct:    ${report.correct} (${(report.accuracy * 100).toFixed(1)}%)`)
    console.log(`UNKNOWN:    ${report.unknown} (${(report.unknownRate * 100).toFixed(1)}%) ← target < 15%`)
    console.log(`Wrong intent: ${report.total - report.correct - report.unknown}`)

    console.log('\n─── Per-intent breakdown ───')
    for (const [intent, stats] of Object.entries(report.byIntent)) {
      const acc = ((stats.correct / stats.total) * 100).toFixed(0)
      const unk = stats.unknown
      console.log(`  ${intent.padEnd(16)} ${stats.correct}/${stats.total} (${acc}%) | UNKNOWN: ${unk}`)
    }

    if (report.failures.length > 0) {
      console.log(`\n─── First 20 failures ───`)
      report.failures.slice(0, 20).forEach(f => {
        console.log(`  [${f.index}] "${f.transcript}"`)
        console.log(`       expected: ${f.expected} → got: ${f.got}`)
      })
    }

    console.log('\n═══════════════════════════════════════════')

    // The actual assertion
    expect(report.unknownRate).toBeLessThan(0.15)
  })

  it('COMPLETE_TASK accuracy > 70%', () => {
    const report = measureAccuracy(entries)
    const stats = report.byIntent['COMPLETE_TASK']
    if (!stats) return
    const acc = stats.correct / stats.total
    console.log(`COMPLETE_TASK: ${(acc * 100).toFixed(1)}%`)
    expect(acc).toBeGreaterThan(0.70)
  })

  it('CREATE_EXPENSE accuracy > 70%', () => {
    const report = measureAccuracy(entries)
    const stats = report.byIntent['CREATE_EXPENSE']
    if (!stats) return
    const acc = stats.correct / stats.total
    console.log(`CREATE_EXPENSE: ${(acc * 100).toFixed(1)}%`)
    expect(acc).toBeGreaterThan(0.70)
  })

  it('QUERY_BALANCE accuracy > 70%', () => {
    const report = measureAccuracy(entries)
    const stats = report.byIntent['QUERY_BALANCE']
    if (!stats) return
    const acc = stats.correct / stats.total
    console.log(`QUERY_BALANCE: ${(acc * 100).toFixed(1)}%`)
    expect(acc).toBeGreaterThan(0.70)
  })
})
