import { defineStore } from 'pinia'
import { ref } from 'vue'
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
  const messages = ref<Message[]>([])
  const isStreaming = ref(false)
  const loadingHistory = ref(false)

  async function sendMessage(content: string) {
    if (!content.trim() || isStreaming.value) return

    isStreaming.value = true

    messages.value.push({ role: 'user', content: content.trim() })

    const assistantMsg: Message = { role: 'assistant', content: '', isStreaming: true }
    messages.value.push(assistantMsg)
    // 生成期间显示 loading（打字动画），完成后再填充内容

    try {
      const res: any = await chatApi.chat({
        message: content.trim(),
        conversationId: currentConversationId.value || undefined
      })

      const data = res.data
      assistantMsg.content = data.answer || ''
      assistantMsg.sources = data.sources || []
      currentConversationId.value = data.conversationId
    } catch (error: any) {
      const status = error.response?.status
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
      } else if (status === 500) {
        ElMessage.error('服务器内部错误，请稍后重试')
      } else {
        ElMessage.error('请求失败')
      }
      assistantMsg.content = '抱歉，发生了错误，请重试。'
    } finally {
      assistantMsg.isStreaming = false
      isStreaming.value = false
      // Non-blocking refresh
      loadConversations().catch(() => {})
    }
  }

  function cancelStream() {
    // 非流式模式下取消无操作（等待当前请求完成）
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
    messages.value = []

    try {
      const res: any = await chatApi.getHistory(conversationId)
      const history: any[] = res.data || []
      for (const msg of history) {
        messages.value.push({
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
    messages.value = []
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

  function getLastUserMessage(): string | null {
    for (let i = messages.value.length - 1; i >= 0; i--) {
      if (messages.value[i].role === 'user') {
        return messages.value[i].content
      }
    }
    return null
  }

  return {
    conversations, currentConversationId, messages, isStreaming, loadingHistory,
    sendMessage, cancelStream, loadConversations, loadHistory, newConversation, deleteConversation, getLastUserMessage
  }
})