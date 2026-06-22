import type { ActionResult } from './types'

const SUGGESTIONS = [
  "Try: 'kitchen done', 'I spent 500 on groceries', or 'what are my tasks'.",
  "Try: 'who is home', 'how much does Rahul owe me', or 'swap my task'.",
  "I understand flat tasks, expenses, and balances. Try: 'bathroom done' or 'add 300 for milk'.",
  "Say something like 'mark kitchen done' or 'I paid 500 for groceries'.",
]

let suggestionIndex = 0

export async function executeUnknown(transcript?: string): Promise<ActionResult> {
  const suggestion = SUGGESTIONS[suggestionIndex % SUGGESTIONS.length]
  suggestionIndex++

  return {
    success: false,
    action: 'UNKNOWN',
    message: `Didn't catch that. ${suggestion}`,
    data: { transcript },
    error: 'unknown_intent',
  }
}
