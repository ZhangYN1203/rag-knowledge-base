import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import { chatApi, type ConversationSummary, type ReferenceSource } from '@/api/types'
import { ElMessage } from 'element-plus'

interface Message {
  role: 'user' | 'assistant'
  content: string
  sources?: ReferenceSource[]
  isStreaming?: boolean
}

export const useChatStore = defineStore('chat', () => {
  const conversations = ref<ConversationSummary[]>([])
  const currentConversationId = ref<string | null>(null)
  const messages = reactive<Message[]>([])
  const isStreaming = ref(false)
  const loadingHistory = ref(false)

  let abortController: AbortController | null = null
  let streamCancelled = false
  let streamDone = false
  let currentAssistantMsg: Message | null = null

  async function sendMessage(content: string) {
    if (!content.trim() || isStreaming.value) return

    messages.push({ role: 'user', content: content.trim() })

    isStreaming.value = true
    streamCancelled = false
    streamDone = false
    currentAssistantMsg = null

    const assistantMsg: Message = reactive({ role: 'assistant', content: '', isStreaming: true })
    messages.push(assistantMsg)
    currentAssistantMsg = assistantMsg

    abortController = new AbortController()

    try {
      const params = new URLSearchParams({ message: content.trim() })
      if (currentConversationId.value) {
        params.append('conversationId', currentConversationId.value)
      }

      const response = await fetch(`/api/chat/stream?${params.toString()}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('accessToken')}`
        },
        signal: abortController.signal
      })

      if (!response.ok) throw new Error(`HTTP ${response.status}`)

      const reader = response.body!.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      let currentEvent = ''

      while (true) {
        if (streamCancelled || streamDone) break

        const { done, value } = await reader.read()
        if (done) break
        if (streamCancelled || streamDone) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('event:')) {
            currentEvent = line.slice(6).trim()
          } else if (line.startsWith('data:')) {
            const data = line.slice(5).trim()
            handleEvent(currentEvent, data)
          }
        }
      }
    } catch (error: any) {
      if (error.name !== 'AbortError') {
        if (currentAssistantMsg) currentAssistantMsg.content = '抱歉，发生了错误，请重试。'
        ElMessage.error('请求失败')
      }
    } finally {
      if (currentAssistantMsg) currentAssistantMsg.isStreaming = false
      isStreaming.value = false
      currentAssistantMsg = null
      abortController = null
      streamCancelled = false
      await loadConversations().catch(() => {})
    }
  }

  function handleEvent(event: string, data: string) {
    const msg = currentAssistantMsg
    if (!msg) return

    switch (event) {
      case 'token':
        msg.content += data
        break
      case 'references':
        try {
          msg.sources = JSON.parse(data)
        } catch { /* ignore parse errors */ }
        break
      case 'done':
        currentConversationId.value = data.replace(/"/g, '')
        streamDone = true
        break
      case 'error':
        msg.content = '错误: ' + data
        break
    }
  }

  function cancelStream() {
    streamCancelled = true
    if (abortController) {
      abortController.abort()
      abortController = null
    }
  }

  async function loadConversations() {
    try {
      const res: any = await chatApi.getConversations()
      conversations.value = res.data || []
    } catch { /* silent */ }
  }

  async function loadHistory(conversationId: string) {
    loadingHistory.value = true
    currentConversationId.value = conversationId
    messages.length = 0

    try {
      const res: any = await chatApi.getHistory(conversationId)
      const history: any[] = res.data || []
      for (const msg of history) {
        messages.push({
          role: msg.role as 'user' | 'assistant',
          content: msg.content
        })
      }
    } catch {
      ElMessage.error('加载历史记录失败')
    } finally {
      loadingHistory.value = false
    }
  }

  function newConversation() {
    currentConversationId.value = null
    messages.length = 0
    currentAssistantMsg = null
  }

  async function deleteConversation(conversationId: string) {
    try {
      await chatApi.delete(conversationId)
      conversations.value = conversations.value.filter(c => c.conversationId !== conversationId)
      if (currentConversationId.value === conversationId) {
        newConversation()
      }
    } catch {
      ElMessage.error('删除失败')
    }
  }

  return {
    conversations, currentConversationId, messages, isStreaming, loadingHistory,
    sendMessage, cancelStream, loadConversations, loadHistory, newConversation, deleteConversation
  }
})