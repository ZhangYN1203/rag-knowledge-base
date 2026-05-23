import { defineStore } from 'pinia'
import { ref } from 'vue'
import { templateApi, type PromptTemplate } from '@/api/types'
import { ElMessage } from 'element-plus'

export const useTemplateStore = defineStore('template', () => {
  const templates = ref<PromptTemplate[]>([])
  const loading = ref(false)

  async function fetchTemplates(category?: string) {
    loading.value = true
    try {
      const res: any = await templateApi.list(category)
      templates.value = res.data || []
    } catch {
      ElMessage.error('加载模板列表失败')
    } finally {
      loading.value = false
    }
  }

  async function createTemplate(data: { name: string; content: string; category?: string; description?: string; variables?: string }) {
    try {
      await templateApi.create(data)
      ElMessage.success('创建成功')
      await fetchTemplates()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '创建失败')
    }
  }

  async function updateTemplate(id: number, data: { name: string; content: string; category?: string; description?: string; variables?: string }) {
    try {
      await templateApi.update(id, data)
      ElMessage.success('更新成功')
      await fetchTemplates()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '更新失败')
    }
  }

  async function deleteTemplate(id: number) {
    try {
      await templateApi.delete(id)
      ElMessage.success('删除成功')
      await fetchTemplates()
    } catch {
      ElMessage.error('删除失败')
    }
  }

  return { templates, loading, fetchTemplates, createTemplate, updateTemplate, deleteTemplate }
})