import { classifyIntent } from '@/lib/voice/nlu/intentClassifier'
import { extractEntities } from '@/lib/voice/nlu/entityExtractor'
import { routeVoiceAction } from '@/lib/voice/actions/actionRouter'

// Mock context for testing
const mockContext = {
  flatId: 'flat-123',
  flatName: 'Test Flat',
  currentUid: 'user-1',
  members: [
    { uid: 'user-1', nickname: 'Bhanu', email: 'bhanu@test.com', status: 'available', reliabilityScore: 100 },
    { uid: 'user-2', nickname: 'Sai', email: 'sai@test.com', status: 'available', reliabilityScore: 100 }
  ],
  tasks: [
    { id: 'task-1', name: 'Kitchen', assignedTo: 'user-1', status: 'pending', emoji: '🍳', frequency: 'daily' },
    { id: 'task-2', name: 'Bathroom', assignedTo: 'user-2', status: 'pending', emoji: '🚽', frequency: 'weekly' }
  ],
  expenses: []
}

describe('Voice Performance', () => {
  it('classifies intent in < 10ms', () => {
    const start = performance.now()
    classifyIntent('kitchen done')
    const end = performance.now()
    expect(end - start).toBeLessThan(10)
  })

  it('extracts entities in < 20ms', () => {
    const start = performance.now()
    extractEntities('i spent 500 on groceries', mockContext)
    const end = performance.now()
    expect(end - start).toBeLessThan(20)
  })

  it('end-to-end action in < 100ms', async () => {
    const start = performance.now()
    
    // Simulate end-to-end voice processing flow
    const intent = classifyIntent('kitchen done')
    const entities = extractEntities('kitchen done', mockContext)
    
    await routeVoiceAction(intent, entities, mockContext, {
      markTaskCompleted: async () => {},
      addExpense: async () => {},
      createSwapRequest: async () => {},
      createTask: async () => {}
    }, 'kitchen done')

    const end = performance.now()
    expect(end - start).toBeLessThan(100)
  })
})
