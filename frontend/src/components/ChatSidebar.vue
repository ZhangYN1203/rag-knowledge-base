<template>
  <div class="sidebar">
    <div class="sidebar-header">
      <h2>RAG 智能知识库</h2>
    </div>

    <div class="new-chat-btn">
      <el-button type="primary" @click="$emit('newConversation')" style="width: 100%">
        + 新对话
      </el-button>
    </div>

    <div class="conversation-list">
      <div
        v-for="conv in conversations"
        :key="conv.conversationId"
        :class="['conversation-item', { active: conv.conversationId === activeId }]"
        @click="$emit('selectConversation', conv.conversationId)"
      >
        <div class="conv-title">{{ conv.title }}</div>
        <div class="conv-meta">
          <span class="conv-time">{{ formatRelativeTime(conv.lastMessageAt) }}</span>
          <el-button
            text
            type="danger"
            size="small"
            class="delete-btn"
            @click.stop="$emit('deleteConversation', conv.conversationId)"
          >
            删除
          </el-button>
        </div>
      </div>
      <div v-if="conversations.length === 0" class="empty-hint">
        暂无对话记录
      </div>
    </div>

    <div class="sidebar-footer">
      <router-link to="/documents" class="nav-link">
        <el-button text style="width: 100%; justify-content: flex-start">知识库管理</el-button>
      </router-link>
      <router-link to="/templates" class="nav-link">
        <el-button text style="width: 100%; justify-content: flex-start">Prompt 模板</el-button>
      </router-link>
      <router-link to="/config" class="nav-link">
        <el-button text style="width: 100%; justify-content: flex-start">模型配置</el-button>
      </router-link>
      <el-button text type="danger" @click="$emit('logout')" style="width: 100%; justify-content: flex-start; margin-top: 4px;">
        退出登录
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ConversationSummary } from '@/api/types'
import { formatRelativeTime } from '@/utils/markdown'

defineProps<{
  conversations: ConversationSummary[]
  activeId: string | null
}>()

defineEmits<{
  newConversation: []
  selectConversation: [id: string]
  deleteConversation: [id: string]
  logout: []
}>()
</script>

<style scoped>
.sidebar {
  width: 280px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #1a1a2e;
  color: white;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h2 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.new-chat-btn {
  padding: 12px;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px;
}

.conversation-item {
  padding: 12px;
  margin: 4px 0;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.conversation-item:hover,
.conversation-item.active {
  background: rgba(255, 255, 255, 0.1);
}

.conv-title {
  font-size: 14px;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.2s;
}

.conversation-item:hover .delete-btn {
  opacity: 1;
}

.empty-hint {
  text-align: center;
  color: rgba(255, 255, 255, 0.4);
  padding: 20px;
  font-size: 13px;
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.nav-link {
  text-decoration: none;
  color: inherit;
}
</style>