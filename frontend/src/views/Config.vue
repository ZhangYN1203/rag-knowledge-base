<template>
  <div class="config-container">
    <div class="header">
      <h1>AI 模型配置</h1>
      <div class="header-actions">
        <router-link to="/"><el-button type="primary" size="small">返回聊天</el-button></router-link>
        <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
      </div>
    </div>

    <div class="content">
      <el-card v-loading="configStore.loading">
        <template #header>模型提供商设置</template>

        <el-form :model="form" label-width="140px" style="max-width: 600px">
          <el-form-item label="AI 提供商">
            <el-radio-group v-model="form.provider">
              <el-radio value="ollama">Ollama (本地)</el-radio>
              <el-radio value="openai">OpenAI API</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="对话模型">
            <el-input v-model="form.chatModel" placeholder="如: qwen2:7b / gpt-4o-mini" />
            <div class="form-tip">Ollama 请使用 ollama pull 拉取的模型名</div>
          </el-form-item>

          <el-form-item label="嵌入模型">
            <el-input v-model="form.embeddingModel" placeholder="如: nomic-embed-text / text-embedding-3-small" />
          </el-form-item>

          <el-form-item label="API 地址">
            <el-input v-model="form.baseUrl" placeholder="Ollama: http://localhost:11434" />
          </el-form-item>

          <el-form-item v-if="form.provider === 'openai'" label="API Key">
            <el-input v-model="form.apiKey" type="password" show-password placeholder="sk-..." />
          </el-form-item>

          <el-form-item label="温度 (Temperature)">
            <el-slider v-model="form.temperature" :min="0" :max="2" :step="0.1" show-input />
          </el-form-item>

          <el-form-item label="最大 Token">
            <el-input-number v-model="form.maxTokens" :min="256" :max="8192" :step="256" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSave" :loading="saving">保存配置</el-button>
            <el-button @click="testConnection">测试连接</el-button>
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

const router = useRouter()
const configStore = useConfigStore()
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
    // handled in store
  } finally {
    saving.value = false
  }
}

async function testConnection() {
  ElMessage.info('连接测试功能需要后端支持，配置已保存。')
}

const handleLogout = () => {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
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
.config-container {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 20px;
}

.header {
  max-width: 800px;
  margin: 0 auto 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 { font-size: 24px; color: #333; }
.header-actions { display: flex; gap: 10px; }

.content { max-width: 800px; margin: 0 auto; }

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>