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
        @input="autoResize"
        rows="1"
      ></textarea>
      <div class="input-actions">
        <span class="char-count" :class="{ warning: message.length > 2000 }">
          {{ message.length > 2000 ? `${message.length}/2000` : '' }}
        </span>
        <el-button
          v-if="!isStreaming"
          type="primary"
          :disabled="!message.trim() || disabled"
          @click="handleSend"
          class="send-btn"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="22" y1="2" x2="11" y2="13"/>
            <polygon points="22 2 15 22 11 13 2 9 22 2"/>
          </svg>
          发送
        </el-button>
        <el-button
          v-else
          type="danger"
          @click="$emit('stop')"
          class="stop-btn"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="6" y="6" width="12" height="12"/>
          </svg>
          停止
        </el-button>
      </div>
    </div>
    <div class="input-hint">
      <span>Enter 发送 · Shift+Enter 换行</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'

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
  if (message.value.length > 2000) {
    return
  }
  emit('send', message.value.trim())
  message.value = ''
  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
    }
  })
}

function handleNewLine() {
  nextTick(() => autoResize())
}

watch(() => props.disabled, (disabled) => {
  if (!disabled) {
    textareaRef.value?.focus()
  }
})
</script>

<style scoped>
.input-area {
  padding: 12px 24px 16px;
  background: white;
}

.input-wrapper {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  padding: 8px 8px 8px 18px;
  transition: border-color 0.2s, box-shadow 0.2s;
  max-width: 864px;
  margin: 0 auto;
  width: 100%;
}

.input-wrapper:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12);
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
  color: #333;
}

textarea::placeholder {
  color: #bbb;
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

.char-count {
  font-size: 11px;
  color: #bbb;
  padding-bottom: 8px;
  min-width: 50px;
  text-align: right;
  transition: color 0.2s;
}

.char-count.warning {
  color: #ff4d4f;
  font-weight: 500;
}

.send-btn {
  border-radius: 10px;
  min-width: 80px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.stop-btn {
  border-radius: 10px;
  min-width: 80px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.input-hint {
  text-align: center;
  padding: 8px 0 0;
  font-size: 11px;
  color: #bbb;
}
</style>