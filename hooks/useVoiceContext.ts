"use client"
import { usePathname } from 'next/navigation'
import type { IntentType } from '@/lib/voice/actions/types'

export function useVoiceContext(): { defaultIntent: IntentType } {
  const pathname = usePathname() || ''

  // Fallback defaults based on the active screen to improve NLU confidence
  if (pathname.includes('/tasks') || pathname.includes('/duties')) {
    return { defaultIntent: 'COMPLETE_TASK' }
  }
  if (pathname.includes('/expenses') || pathname.includes('/money')) {
    return { defaultIntent: 'CREATE_EXPENSE' }
  }
  if (pathname.includes('/dashboard') || pathname.includes('/nest')) {
    return { defaultIntent: 'QUERY_STATUS' }
  }
  
  return { defaultIntent: 'QUERY_TASKS' }
}
