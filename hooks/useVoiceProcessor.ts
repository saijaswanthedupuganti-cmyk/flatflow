"use client"
import { useState, useCallback } from 'react'
import { useFlatStore } from '@/store/useFlatStore'
import { useAuthStore } from '@/store/useAuthStore'
import { buildVoiceContext, contextCache } from '@/lib/voice/nlu/contextResolver'
import { classifyIntent } from '@/lib/voice/nlu/intentClassifier'
import { extractEntities } from '@/lib/voice/nlu/entityExtractor'
import { routeVoiceAction } from '@/lib/voice/actions/actionRouter'
import { formatResponse, type VoiceResponse } from '@/lib/voice/response/responseFormatter'
import { tts } from '@/lib/voice/tts/speechSynthesis'

export type { VoiceResponse }

export function useVoiceProcessor() {
  const [response, setResponse]       = useState<VoiceResponse | null>(null)
  const [isProcessing, setIsProcessing] = useState(false)

  const { members, tasks, expenses, name: flatName, isSynced, markTaskCompleted, addExpense, createSwapRequest, createTask } = useFlatStore()
  const { user, flatId } = useAuthStore()

  const process = useCallback(async (transcript: string) => {
    if (!user?.uid || !flatId) return

    setIsProcessing(true)
    try {
      // Always build fresh context when the store is synced and has real data.
      // The cache is only used as a fallback when the store isn't ready yet so we
      // never act on stale (empty) members/tasks from a previous render cycle.
      const cached = contextCache.get(flatId)
      const storeHasData = isSynced && (members.length > 0 || tasks.length > 0)
      let context = (storeHasData || !cached) ? null : cached

      if (!context) {
        context = buildVoiceContext({
          flatId,
          flatName: flatName ?? '',
          currentUid: user.uid,
          members,
          tasks,
          expenses,
        })
        if (storeHasData) contextCache.set(flatId, context)
      }

      // NLU
      const intent   = classifyIntent(transcript)
      const entities = extractEntities(transcript, context)

      // Execute action
      const result = await routeVoiceAction(intent, entities, context, {
        markTaskCompleted,
        addExpense,
        createSwapRequest,
        createTask,
      }, transcript)

      // Format for UI + TTS
      const voiceResponse = formatResponse(result)
      setResponse(voiceResponse)

      if (voiceResponse.speak) {
        tts.speak(voiceResponse.text)
      }
    } catch (err) {
      console.error('[VoiceProcessor]', err)
      tts.speak("Something went wrong. Please try again.")
      setResponse({
        text: "Something went wrong. Please try again.",
        speak: true,
        card: { type: 'error', action: 'UNKNOWN', title: 'Error — please try again', canUndo: false },
      })
    } finally {
      setIsProcessing(false)
    }
  }, [user, flatId, flatName, isSynced, members, tasks, expenses, markTaskCompleted, addExpense, createSwapRequest, createTask])

  const clearResponse = useCallback(() => setResponse(null), [])

  return { process, response, isProcessing, clearResponse }
}
