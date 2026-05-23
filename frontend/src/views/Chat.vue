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
      <!-- Empty state -->
      <div v-if="chatStore.messages.length === 0 && !chatStore.loadingHistory" class="empty-state">
        <div class="empty-icon">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#667eea" stroke-width="1.5">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
        </div>
        <h2>RAG 智能知识库问答系统</h2>
        <p class="empty-desc">上传文档构建知识库，基于 AI 大模型智能问答</p>
        <div class="feature-list">
          <div class="feature-item">
            <span class="feature-icon">📄</span>
            <span>支持 PDF、Word、TXT 文档上传</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">🔍</span>
            <span>向量检索 + AI 生成精准回答</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">⚡</span>
            <span>流式输出，实时显示生成过程</span>
          </div>
        </div>
      </div>

      <!-- Messages -->
      <div v-else class="messages-container" ref="messagesRef">
        <div v-if="chatStore.loadingHistory" class="loading-history">
          <el-icon class="is-loading"><Loading /></el-icon> 加载中...
        </div>
        <ChatMessage
          v-for="(msg, index) in chatStore.messages"
          :key="index"
          :msg="msg"
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
import ChatSidebar from '@/components/ChatSidebar.vue'
import ChatMessage from '@/components/ChatMessage.vue'
import ChatInput from '@/components/ChatInput.vue'

const router = useRouter()
const chatStore = useChatStore()
const messagesRef = ref<HTMLElement>()

const handleLogout = () => {
  chatStore.newConversation()
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  router.push('/login')
}

// Auto scroll to bottom when messages change
watch(
  () => chatStore.messages.length,
  async () => {
    await nextTick()
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  }
)

// Also scroll during streaming
watch(
  () => chatStore.messages[chatStore.messages.length - 1]?.content,
  async () => {
    await nextTick()
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
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
  background: #f5f5f5;
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
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
  margin-bottom: 16px;
}

.empty-state h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.empty-desc {
  color: #888;
  margin-bottom: 32px;
  font-size: 15px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #555;
}

.feature-icon {
  font-size: 20px;
  width: 32px;
  text-align: center;
}
</style>