<template>
  <div :class="['message', msg.role]">
    <div class="avatar">
      {{ msg.role === 'user' ? '我' : 'AI' }}
    </div>
    <div class="message-body">
      <div class="message-content" v-html="renderMarkdown(msg.content)"></div>
      <div v-if="msg.isStreaming && msg.content" class="cursor"></div>
      <div v-if="msg.isStreaming && !msg.content" class="typing-indicator">
        <span></span><span></span><span></span>
      </div>
      <div v-if="msg.sources && msg.sources.length > 0" class="references">
        <div class="ref-toggle" @click="showRefs = !showRefs">
          查看引用来源 ({{ msg.sources.length }})
          <span class="arrow">{{ showRefs ? '▼' : '▶' }}</span>
        </div>
        <div v-if="showRefs" class="ref-list">
          <div v-for="(src, i) in msg.sources" :key="i" class="ref-item">
            <div class="ref-title">[{{ i + 1 }}] {{ src.title }}</div>
            <div class="ref-excerpt">{{ src.excerpt }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { renderMarkdown } from '@/utils/markdown'

defineProps<{
  msg: {
    role: 'user' | 'assistant'
    content: string
    sources?: Array<{ title: string; excerpt: string; similarity: number }>
    isStreaming?: boolean
  }
}>()

const showRefs = ref(false)
</script>

<style scoped>
.message {
  display: flex;
  gap: 16px;
  padding: 20px;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.message.user .avatar {
  background: #52c41a;
}

.message-body {
  max-width: 70%;
}

.message-content {
  padding: 14px 18px;
  border-radius: 12px;
  line-height: 1.7;
  font-size: 14px;
  word-wrap: break-word;
  background: #f0f0f0;
  color: #333;
}

.message.user .message-content {
  background: #667eea;
  color: white;
}

.message.user .message-content :deep(pre) {
  background: rgba(0, 0, 0, 0.2);
}

.message-content :deep(pre) {
  background: #1e1e2e;
  color: #cdd6f4;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
  font-size: 13px;
}

.message-content :deep(code) {
  font-family: 'Cascadia Code', 'Fira Code', monospace;
}

.message-content :deep(p) {
  margin: 4px 0;
}

.message-content :deep(ul), .message-content :deep(ol) {
  padding-left: 20px;
  margin: 4px 0;
}

.cursor {
  display: inline-block;
  width: 2px;
  height: 18px;
  background: #667eea;
  margin-left: 2px;
  animation: blink 1s infinite;
  vertical-align: text-bottom;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 14px 18px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #667eea;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.references {
  margin-top: 8px;
}

.ref-toggle {
  font-size: 13px;
  color: #667eea;
  cursor: pointer;
  padding: 4px 0;
  user-select: none;
}

.ref-toggle:hover {
  color: #764ba2;
}

.arrow {
  font-size: 10px;
  margin-left: 4px;
}

.ref-list {
  margin-top: 6px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

.ref-item {
  padding: 10px 12px;
  border-bottom: 1px solid #f0f0f0;
}

.ref-item:last-child {
  border-bottom: none;
}

.ref-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.ref-excerpt {
  font-size: 12px;
  color: #888;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>