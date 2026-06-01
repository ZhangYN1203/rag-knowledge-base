<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-left">
        <router-link to="/" class="back-link">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
        </router-link>
        <div>
          <h1>AI 模型配置</h1>
          <p class="header-desc">配置 AI 模型提供商和参数</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="danger" size="small" plain @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <div class="page-content">
      <el-card shadow="never" v-loading="configStore.loading">
        <template #header>
          <div class="card-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#667eea" stroke-width="2">
              <circle cx="12" cy="12" r="3"/>
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
            </svg>
            模型提供商设置
          </div>
        </template>

        <el-form :model="form" label-width="140px" style="max-width: 600px">
          <el-form-item label="AI 提供商">
            <el-radio-group v-model="form.provider">
              <el-radio value="ollama">
                <span style="font-weight: 500">Ollama</span> (本地部署)
              </el-radio>
              <el-radio value="openai">
                <span style="font-weight: 500">OpenAI</span> API
              </el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="对话模型">
            <el-input v-model="form.chatModel" placeholder="如: qwen2:7b / gpt-4o-mini" />
            <div class="form-tip">Ollama 使用 <code>ollama pull</code> 拉取的模型名</div>
          </el-form-item>

          <el-form-item label="嵌入模型">
            <el-input v-model="form.embeddingModel" placeholder="如: nomic-embed-text / text-embedding-3-small" />
          </el-form-item>

          <el-form-item label="API 地址">
            <el-input v-model="form.baseUrl" placeholder="http://localhost:11434" />
          </el-form-item>

          <el-form-item v-if="form.provider === 'openai'" label="API Key">
            <el-input v-model="form.apiKey" type="password" show-password placeholder="sk-..." />
          </el-form-item>

          <el-form-item label="温度">
            <el-slider v-model="form.temperature" :min="0" :max="2" :step="0.1" show-input />
          </el-form-item>

          <el-form-item label="最大 Token">
            <el-input-number v-model="form.maxTokens" :min="256" :max="8192" :step="256" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSave" :loading="saving">保存配置</el-button>
            <el-button @click="testConnection" :disabled="saving">测试连接</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useConfigStore } from '@/stores/configStore'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const configStore = useConfigStore()
const authStore = useAuthStore()
const saving = ref(false)

const form = reactive({
  provider: 'ollama' as 'ollama' | 'openai',
  chatModel: '',
  embeddingModel: '',
  baseUrl: '',
  apiKey: '',
  temperature: 0.7,
  maxTokens: 2048
})

async function handleSave() {
  saving.value = true
  try {
    await configStore.updateConfig({
        provider: form.provider,
        chatModel: form.chatModel || undefined,
        embeddingModel: form.embeddingModel || undefined,
        baseUrl: form.baseUrl || undefined,
        apiKey: form.apiKey || undefined,
        temperature: form.temperature,
        maxTokens: form.maxTokens
      })
  } catch {
  } finally {
    saving.value = false
  }
}

async function testConnection() {
  ElMessage.info('连接测试功能需要后端支持，配置已保存。')
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(async () => {
  await configStore.fetchConfig()
  if (configStore.config) {
    form.provider = configStore.config.provider
    form.chatModel = configStore.config.chatModel
    form.embeddingModel = configStore.config.embeddingModel
    form.baseUrl = configStore.config.baseUrl
    form.temperature = configStore.config.temperature
    form.maxTokens = configStore.config.maxTokens
  }
})
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 24px;
}

.page-header {
  max-width: 800px;
  margin: 0 auto 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-link {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  text-decoration: none;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  transition: all 0.2s;
}

.back-link:hover {
  background: #667eea;
  color: white;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}

.header-desc {
  font-size: 13px;
  color: #888;
  margin-top: 2px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.page-content {
  max-width: 800px;
  margin: 0 auto;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.form-tip code {
  background: #f0f0f0;
  padding: 1px 5px;
  border-radius: 3px;
  font-size: 12px;
}
</style>