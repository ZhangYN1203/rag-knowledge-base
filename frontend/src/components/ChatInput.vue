<template>
  <div class="input-area">
    <div class="input-wrapper">
      <textarea
        ref="textareaRef"
        v-model="message"
        :disabled="disabled"
        placeholder="输入您的问题... (Enter 发送，Shift+Enter 换行)"
        @keydown.enter.exact="handleSend"
        @keydown.shift.enter="handleNewLine"
        rows="1"
      ></textarea>
      <div class="input-actions">
        <el-button
          v-if="!isStreaming"
          type="primary"
          :disabled="!message.trim() || disabled"
          @click="handleSend"
          class="send-btn"
        >
          发送
        </el-button>
        <el-button
          v-else
          type="danger"
          @click="$emit('stop')"
          class="stop-btn"
        >
          停止
        </el-button>
      </div>
    </div>
    <div class="input-hint">
      RAG 智能知识库 · 基于向量检索的问答系统
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'

const props = defineProps<{
  disabled: boolean
  isStreaming: boolean
}>()

const emit = defineEmits<{
  send: [message: string]
  stop: []
}>()

const message = ref('')
const textareaRef = ref<HTMLTextAreaElement>()

function autoResize() {
  nextTick(() => {
    const el = textareaRef.value
    if (el) {
      el.style.height = 'auto'
      el.style.height = Math.min(el.scrollHeight, 200) + 'px'
    }
  })
}

function handleSend() {
  if (!message.value.trim() || props.disabled) return
  emit('send', message.value.trim())
  message.value = ''
  autoResize()
}

function handleNewLine() {
  // Let the default Shift+Enter behavior work (newline)
  autoResize()
}
</script>

<style scoped>
.input-area {
  padding: 16px 24px 12px;
  border-top: 1px solid #e8e8e8;
  background: white;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  padding: 8px 8px 8px 16px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.input-wrapper:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.15);
}

textarea {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  outline: none;
  font-family: inherit;
  max-height: 200px;
  min-height: 24px;
  padding: 8px 0;
}

textarea:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.input-actions {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.send-btn, .stop-btn {
  border-radius: 10px;
  min-width: 72px;
}

.input-hint {
  text-align: center;
  padding: 8px 0 0;
  font-size: 12px;
  color: #999;
}
</style>