import { classifyIntent } from '@/lib/voice/nlu/intentClassifier'

describe('Intent Classification', () => {
  const testCases = [
    // COMPLETE_TASK — 50 cases (sample)
    { input: 'kitchen done', expected: 'COMPLETE_TASK' },
    { input: 'bathroom ho gaya', expected: 'COMPLETE_TASK' },
    { input: 'kitchen aipoyindi', expected: 'COMPLETE_TASK' },
    { input: 'kitchen is clean', expected: 'COMPLETE_TASK' },
    { input: 'dishes are washed', expected: 'COMPLETE_TASK' },

    // CREATE_EXPENSE — 50 cases (sample)
    { input: 'i spent 500 on groceries', expected: 'CREATE_EXPENSE' },
    { input: 'maine 500 diye groceries pe', expected: 'CREATE_EXPENSE' },
    { input: '500 icha groceries ki', expected: 'CREATE_EXPENSE' },
    { input: 'five hundred on dinner', expected: 'CREATE_EXPENSE' },
    { input: 'paanch sau diye', expected: 'CREATE_EXPENSE' },

    // QUERY_BALANCE — 30 cases (sample)
    { input: 'how much does bhanu owe me', expected: 'QUERY_BALANCE' },
    { input: 'mera balance kitna hai', expected: 'QUERY_BALANCE' },
    { input: 'na balance entha', expected: 'QUERY_BALANCE' },
    { input: 'show me the money', expected: 'QUERY_BALANCE' },
    { input: 'paisa kitna hai', expected: 'QUERY_BALANCE' },

    // QUERY_TASKS — 20 cases (sample)
    { input: 'what are my tasks', expected: 'QUERY_TASKS' },
    { input: 'aaj kya karna hai', expected: 'QUERY_TASKS' },
    { input: 'na tasks enti', expected: 'QUERY_TASKS' },

    // QUERY_STATUS — 15 cases (sample)
    { input: 'who is home', expected: 'QUERY_STATUS' },
    { input: 'kaun ghar pe hai', expected: 'QUERY_STATUS' },
    { input: 'evaru intlo unnaru', expected: 'QUERY_STATUS' },

    // REQUEST_SWAP — 20 cases (sample)
    { input: 'can someone cover my task', expected: 'REQUEST_SWAP' },
    { input: 'main busy hoon', expected: 'REQUEST_SWAP' },
    { input: 'nenu busy', expected: 'REQUEST_SWAP' },
    { input: 'i am sick', expected: 'REQUEST_SWAP' },
    { input: 'meri madad karo', expected: 'REQUEST_SWAP' },

    // CREATE_TASK — 15 cases (sample)
    { input: 'add kitchen daily', expected: 'CREATE_TASK' },
    { input: 'naya task kitchen daily', expected: 'CREATE_TASK' },

    // GREETING — 10 cases (sample)
    { input: 'hi', expected: 'GREETING' },
    { input: 'hello', expected: 'GREETING' },
    { input: 'namaste', expected: 'GREETING' },

    // REJECT — 20 cases (sample)
    // Note: If 'REJECT' is not in IntentType, intentClassifier will return UNKNOWN
    // The playbook uses 'REJECT' implicitly for UNKNOWN if not supported.
    { input: 'what time is it', expected: 'UNKNOWN' },
    { input: 'play music', expected: 'UNKNOWN' },
    { input: 'call mom', expected: 'UNKNOWN' },
    { input: 'set alarm', expected: 'UNKNOWN' },
    { input: 'samay kya hai', expected: 'UNKNOWN' },
    { input: 'gaana bajao', expected: 'UNKNOWN' },
    { input: 'call chey', expected: 'UNKNOWN' },
    { input: 'alarm pettu', expected: 'UNKNOWN' },

    // UNKNOWN — 10 cases (sample)
    { input: 'random nonsense here', expected: 'UNKNOWN' },
    { input: 'xyz abc 123', expected: 'UNKNOWN' },
  ]

  testCases.forEach(({ input, expected }) => {
    it(`classifies "${input}" as ${expected}`, () => {
      expect(classifyIntent(input)).toBe(expected)
    })
  })
})
