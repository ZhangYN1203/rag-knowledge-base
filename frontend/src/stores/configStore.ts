import { defineStore } from 'pinia'
import { ref } from 'vue'
import { configApi, type AiConfig, type AiConfigRequest } from '@/api/types'
import { ElMessage } from 'element-plus'

export const useConfigStore = defineStore('config', () => {
  const config = ref<AiConfig | null>(null)
  const loading = ref(false)

  async function fetchConfig() {
    loading.value = true
    try {
      const res: any = await configApi.getAiConfig()
      config.value = res.data
    } catch {
      ElMessage.error('加载配置失败')
    } finally {
      loading.value = false
    }
  }

  async function updateConfig(data: Partial<AiConfigRequest>) {
    loading.value = true
    try {
      await configApi.updateAiConfig(data as any)
      ElMessage.success('配置更新成功')
      await fetchConfig()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '配置更新失败')
    } finally {
      loading.value = false
    }
  }

  return { config, loading, fetchConfig, updateConfig }
})