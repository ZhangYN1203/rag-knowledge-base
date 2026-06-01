<template>
  <div class="chat-layout">
    <ChatSidebar
      :conversations="chatStore.conversations"
      :activeId="chatStore.currentConversationId"
      @newConversation="chatStore.newConversation()"
      @selectConversation="chatStore.loadHistory"
      @deleteConversation="chatStore.deleteConversation"
      @logout="handleLogout"
    />

    <div class="main-area">
      <div class="chat-header">
        <span class="header-title">RAG 智能知识库</span>
        <span class="header-subtitle">基于向量检索的 AI 问答系统</span>
      </div>

      <!-- Empty state -->
      <div v-show="chatStore.messages.length === 0 && !chatStore.loadingHistory" class="empty-state">
        <div class="empty-icon">
          <svg width="72" height="72" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            <circle cx="12" cy="10" r="1" fill="currentColor"/>
            <circle cx="16" cy="10" r="1" fill="currentColor"/>
            <circle cx="8" cy="10" r="1" fill="currentColor"/>
          </svg>
        </div>
        <h2>欢迎使用 RAG 智能知识库</h2>
        <p class="empty-desc">上传文档构建知识库，基于 AI 大模型智能问答</p>
        <div class="feature-list">
          <div class="feature-item">
            <span class="feature-badge">01</span>
            <div class="feature-text">
              <strong>多格式文档支持</strong>
              <span>PDF、Word、TXT 一键上传</span>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-badge">02</span>
            <div class="feature-text">
              <strong>智能向量检索</strong>
              <span>精准匹配相关文档片段</span>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-badge">03</span>
            <div class="feature-text">
              <strong>流式实时输出</strong>
              <span>AI 逐字生成，所见即所得</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Messages -->
      <div v-show="chatStore.messages.length > 0 || chatStore.loadingHistory" class="messages-container" ref="messagesRef">
        <div v-if="chatStore.loadingHistory" class="loading-history">
          <el-icon class="is-loading"><Loading /></el-icon> 加载中...
        </div>
        <ChatMessage
          v-for="(msg, index) in chatStore.messages"
          :key="index"
          :msg="msg"
          @retry="handleRetry"
        />
      </div>

      <ChatInput
        :disabled="chatStore.loadingHistory"
        :isStreaming="chatStore.isStreaming"
        @send="chatStore.sendMessage"
        @stop="chatStore.cancelStream"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, watch, nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { useChatStore } from '@/stores/chatStore'
import { useAuthStore } from '@/stores/authStore'
import ChatSidebar from '@/components/ChatSidebar.vue'
import ChatMessage from '@/components/ChatMessage.vue'
import ChatInput from '@/components/ChatInput.vue'

const router = useRouter()
const chatStore = useChatStore()
const authStore = useAuthStore()
const messagesRef = ref<HTMLElement>()

const handleLogout = () => {
  chatStore.newConversation()
  authStore.logout()
  router.push('/login')
}

function handleRetry() {
  const lastUserMessage = chatStore.getLastUserMessage()
  if (lastUserMessage) {
    chatStore.messages.splice(0, chatStore.messages.length)
    chatStore.sendMessage(lastUserMessage)
  }
}

// Scroll to bottom when streaming completes
watch(
  () => chatStore.isStreaming,
  async (streaming) => {
    if (!streaming && chatStore.messages.length > 0) {
      await nextTick()
      if (messagesRef.value) {
        messagesRef.value.scrollTop = messagesRef.value.scrollHeight
      }
    }
  }
)

// Throttled auto-scroll during streaming (every ~200ms)
let scrollTimer: ReturnType<typeof setTimeout> | null = null
watch(
  () => chatStore.messages[chatStore.messages.length - 1]?.content,
  async () => {
    if (!chatStore.isStreaming) return
    if (scrollTimer) clearTimeout(scrollTimer)
    scrollTimer = setTimeout(async () => {
      await nextTick()
      if (messagesRef.value) {
        messagesRef.value.scrollTop = messagesRef.value.scrollHeight
      }
    }, 200)
  }
)

onMounted(() => {
  chatStore.loadConversations()
})
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: 100vh;
  background: #f0f2f5;
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  position: relative;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.header-subtitle {
  font-size: 13px;
  color: #999;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.loading-history {
  text-align: center;
  padding: 40px;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
}

.empty-icon {
  margin-bottom: 24px;
  color: #667eea;
  opacity: 0.6;
}

.empty-state h2 {
  font-size: 22px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 600;
}

.empty-desc {
  color: #888;
  margin-bottom: 40px;
  font-size: 15px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.feature-badge {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
}

.feature-text {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.feature-text strong {
  font-size: 14px;
  color: #333;
}

.feature-text span {
  font-size: 13px;
  color: #888;
}
</style>