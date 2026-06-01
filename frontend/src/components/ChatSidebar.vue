<template>
  <div class="sidebar">
    <div class="sidebar-header">
      <div class="logo">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M12 2L2 7l10 5 10-5-10-5z"/>
          <path d="M2 17l10 5 10-5"/>
          <path d="M2 12l10 5 10-5"/>
        </svg>
        <span>RAG 知识库</span>
      </div>
    </div>

    <div class="new-chat-btn">
      <el-button type="primary" @click="$emit('newConversation')" class="new-btn">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"/>
          <line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        新对话
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
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        暂无对话记录
      </div>
    </div>

    <div class="sidebar-footer">
      <router-link to="/documents" class="nav-link">
        <el-button text class="nav-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
          </svg>
          知识库管理
        </el-button>
      </router-link>
      <router-link to="/templates" class="nav-link">
        <el-button text class="nav-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
          </svg>
          Prompt 模板
        </el-button>
      </router-link>
      <router-link to="/config" class="nav-link">
        <el-button text class="nav-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="3"/>
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
          </svg>
          模型配置
        </el-button>
      </router-link>
      <el-button text type="danger" @click="$emit('logout')" class="nav-btn logout-btn">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
          <polyline points="16 17 21 12 16 7"/>
          <line x1="21" y1="12" x2="9" y2="12"/>
        </svg>
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
  border-right: 1px solid rgba(255, 255, 255, 0.05);
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 700;
  color: white;
}

.new-chat-btn {
  padding: 12px;
}

.new-btn {
  width: 100%;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 40px;
  font-weight: 500;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px;
}

.conversation-item {
  padding: 12px;
  margin: 2px 0;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s;
}

.conversation-item:hover,
.conversation-item.active {
  background: rgba(255, 255, 255, 0.08);
}

.conversation-item.active {
  background: rgba(102, 126, 234, 0.2);
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
  color: rgba(255, 255, 255, 0.4);
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.15s;
  color: rgba(255, 100, 100, 0.7);
}

.conversation-item:hover .delete-btn {
  opacity: 1;
}

.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.3);
  padding: 32px 20px;
  font-size: 13px;
}

.sidebar-footer {
  padding: 8px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-link {
  text-decoration: none;
  color: inherit;
}

.nav-btn {
  width: 100%;
  justify-content: flex-start;
  gap: 8px;
  color: rgba(255, 255, 255, 0.7) !important;
  border-radius: 8px;
  padding: 10px !important;
  height: auto !important;
  font-size: 13px;
}

.nav-btn:hover {
  background: rgba(255, 255, 255, 0.08) !important;
  color: white !important;
}

.logout-btn {
  margin-top: 2px;
}
</style>