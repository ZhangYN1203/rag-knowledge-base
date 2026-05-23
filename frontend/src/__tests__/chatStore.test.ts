import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useChatStore } from '@/stores/chatStore'
import { chatApi } from '@/api/types'

vi.mock('@/api/types', () => ({
  chatApi: {
    getConversations: vi.fn(),
    getHistory: vi.fn(),
    delete: vi.fn(),
  }
}))

describe('chatStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('should start with empty state', () => {
    const store = useChatStore()
    expect(store.conversations).toEqual([])
    expect(store.messages).toEqual([])
    expect(store.currentConversationId).toBeNull()
    expect(store.isStreaming).toBe(false)
  })

  it('should load conversations', async () => {
    const mockConversations = [
      { conversationId: '1', title: 'Chat 1', messageCount: 2, createdAt: new Date().toISOString(), lastMessageAt: new Date().toISOString() }
    ]
    vi.mocked(chatApi.getConversations).mockResolvedValue({ data: mockConversations })

    const store = useChatStore()
    await store.loadConversations()

    expect(store.conversations).toHaveLength(1)
    expect(store.conversations[0].title).toBe('Chat 1')
  })

  it('should load history', async () => {
    const mockHistory = {
      data: [
        { role: 'user', content: 'Hello' },
        { role: 'assistant', content: 'Hi there!' }
      ]
    }
    vi.mocked(chatApi.getHistory).mockResolvedValue(mockHistory)

    const store = useChatStore()
    await store.loadHistory('conv-1')

    expect(store.currentConversationId).toBe('conv-1')
    expect(store.messages).toHaveLength(2)
    expect(store.messages[0].role).toBe('user')
    expect(store.messages[0].content).toBe('Hello')
    expect(store.messages[1].content).toBe('Hi there!')
  })

  it('should start a new conversation', () => {
    const store = useChatStore()
    store.currentConversationId = 'old-conv'
    store.messages.push({ role: 'user', content: 'hi' })

    store.newConversation()

    expect(store.currentConversationId).toBeNull()
    expect(store.messages).toHaveLength(0)
  })

  it('should delete conversation', async () => {
    const store = useChatStore()
    store.conversations = [
      { conversationId: '1', title: 'C1', messageCount: 2, createdAt: '', lastMessageAt: '' },
      { conversationId: '2', title: 'C2', messageCount: 1, createdAt: '', lastMessageAt: '' }
    ]
    vi.mocked(chatApi.delete).mockResolvedValue({})

    await store.deleteConversation('1')

    expect(store.conversations).toHaveLength(1)
    expect(store.conversations[0].conversationId).toBe('2')
  })

  it('should cancel streaming', () => {
    const store = useChatStore()
    store.cancelStream()
    expect(store.isStreaming).toBe(false)
  })
})