<template>
  <div :class="['message', msg.role, { error: hasError }]">
    <div class="avatar">
      {{ msg.role === 'user' ? 'U' : 'AI' }}
    </div>
    <div class="message-body">
      <div class="message-label">{{ msg.role === 'user' ? '你' : 'AI 助手' }}</div>
      <div class="message-content-wrapper">
        <div class="message-content" v-html="renderMarkdown(msg.content)"></div>
        <div v-if="msg.isStreaming && msg.content" class="cursor"></div>
        <div v-if="msg.isStreaming && !msg.content" class="typing-indicator">
          <span></span><span></span><span></span>
        </div>
        <div v-if="hasError" class="error-indicator">
          <span class="error-text">生成失败</span>
          <button class="retry-btn" @click="$emit('retry')">重试</button>
        </div>
      </div>
      <div v-if="msg.sources && msg.sources.length > 0" class="references">
        <div class="ref-toggle" @click="showRefs = !showRefs">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
          </svg>
          查看引用来源 ({{ msg.sources.length }})
          <span class="arrow">{{ showRefs ? '▼' : '▶' }}</span>
        </div>
        <transition name="refs-fade">
          <div v-if="showRefs" class="ref-list">
            <div v-for="(src, i) in msg.sources" :key="i" class="ref-item">
              <div class="ref-title">[{{ i + 1 }}] {{ src.title }}</div>
              <div class="ref-excerpt">{{ src.excerpt }}</div>
            </div>
          </div>
        </transition>
      </div>
      <div v-if="!msg.isStreaming && msg.role === 'assistant'" class="message-actions">
        <button class="action-btn" @click="copyMessage" :title="copied ? '已复制' : '复制'">
          <svg v-if="!copied" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
            <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
          </svg>
          <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
          {{ copied ? '已复制' : '复制' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { renderMarkdown } from '@/utils/markdown'

const props = defineProps<{
  msg: {
    role: 'user' | 'assistant'
    content: string
    sources?: Array<{ title: string; excerpt: string; similarity: number }>
    isStreaming?: boolean
    isError?: boolean
  }
}>()

defineEmits<{
  retry: []
}>()

const showRefs = ref(false)
const copied = ref(false)

const hasError = computed(() => props.msg.isError || props.msg.content.startsWith('错误:'))

async function copyMessage() {
  try {
    await navigator.clipboard.writeText(props.msg.content)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  } catch {
    copied.value = false
  }
}
</script>

<style scoped>
.message {
  display: flex;
  gap: 14px;
  padding: 20px 24px;
  animation: slideIn 0.3s ease-out;
  max-width: 864px;
  margin: 0 auto;
  width: 100%;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(8px); }
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
  font-weight: 700;
  flex-shrink: 0;
  letter-spacing: 0.5px;
}

.message.user .avatar {
  background: linear-gradient(135deg, #52c41a, #389e0d);
}

.message-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  font-weight: 500;
}

.message.user .message-label {
  text-align: right;
}

.message-body {
  max-width: 75%;
  min-width: 0;
}

.message.user .message-body {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-content-wrapper {
  width: 100%;
}

.message-content {
  padding: 14px 18px;
  border-radius: 16px;
  line-height: 1.7;
  font-size: 14px;
  word-wrap: break-word;
  background: white;
  color: #333;
  border: 1px solid #eee;
}

.message.user .message-content-wrapper {
  display: flex;
  flex-direction: row-reverse;
  align-items: flex-end;
  gap: 4px;
}

.message.user .message-content {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-bottom-right-radius: 4px;
}

.message-content :deep(pre) {
  background: #1e1e2e;
  color: #cdd6f4;
  padding: 14px;
  border-radius: 10px;
  overflow-x: auto;
  margin: 10px 0;
  font-size: 13px;
}

.message.user .message-content :deep(pre) {
  background: rgba(0, 0, 0, 0.25);
}

.message-content :deep(code) {
  font-family: 'Cascadia Code', 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
}

.message-content :deep(p) {
  margin: 4px 0;
}

.message-content :deep(ul),
.message-content :deep(ol) {
  padding-left: 20px;
  margin: 6px 0;
}

.message-content :deep(blockquote) {
  border-left: 3px solid #667eea;
  padding-left: 12px;
  margin: 8px 0;
  color: #666;
}

.message-content :deep(a) {
  color: #667eea;
}

.message.user .message-content :deep(a) {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: underline;
}

.message-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 8px 0;
  font-size: 13px;
}

.message-content :deep(th),
.message-content :deep(td) {
  border: 1px solid #ddd;
  padding: 8px 12px;
  text-align: left;
}

.message-content :deep(th) {
  background: #f5f5f5;
  font-weight: 600;
}

.message-content :deep(h1),
.message-content :deep(h2),
.message-content :deep(h3),
.message-content :deep(h4) {
  margin: 12px 0 6px;
  color: #1a1a2e;
}

.message-content :deep(h1) { font-size: 20px; }
.message-content :deep(h2) { font-size: 18px; }
.message-content :deep(h3) { font-size: 16px; }

.message.user .message-content :deep(h1),
.message.user .message-content :deep(h2),
.message.user .message-content :deep(h3),
.message.user .message-content :deep(h4) {
  color: white;
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
  background: white;
  border: 1px solid #eee;
  border-radius: 16px;
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
  margin-top: 10px;
}

.ref-toggle {
  font-size: 13px;
  color: #667eea;
  cursor: pointer;
  padding: 6px 0;
  user-select: none;
  display: flex;
  align-items: center;
  gap: 6px;
}

.ref-toggle:hover {
  color: #764ba2;
}

.arrow {
  font-size: 10px;
}

.ref-list {
  margin-top: 6px;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  overflow: hidden;
  background: white;
}

.refs-fade-enter-active,
.refs-fade-leave-active {
  transition: all 0.2s ease;
}

.refs-fade-enter-from,
.refs-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.ref-item {
  padding: 12px 14px;
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
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.message-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.message:hover .message-actions {
  opacity: 1;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  font-size: 12px;
  color: #888;
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  color: #667eea;
  background: #f0f0ff;
  border-color: #667eea;
}

.error-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  padding: 8px 12px;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 8px;
}

.error-text {
  font-size: 13px;
  color: #ff4d4f;
}

.retry-btn {
  padding: 4px 12px;
  font-size: 12px;
  color: white;
  background: #ff4d4f;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}

.retry-btn:hover {
  background: #ff7875;
}

.message.error .message-content {
  background: #fff9f9;
  border-color: #ffccc7;
}

.message.error .avatar {
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
}
</style>