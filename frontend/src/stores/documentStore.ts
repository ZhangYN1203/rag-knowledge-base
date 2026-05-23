import { defineStore } from 'pinia'
import { ref } from 'vue'
import { documentApi, type Document } from '@/api/types'
import { ElMessage } from 'element-plus'

export const useDocumentStore = defineStore('document', () => {
  const documents = ref<Document[]>([])
  const loading = ref(false)

  async function fetchDocuments() {
    loading.value = true
    try {
      const res: any = await documentApi.getList()
      documents.value = res.data || []
    } catch {
      ElMessage.error('加载文档列表失败')
    } finally {
      loading.value = false
    }
  }

  async function uploadDocument(file: File, category: string) {
    try {
      await documentApi.upload(file, category)
      ElMessage.success('上传成功')
      await fetchDocuments()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '上传失败')
    }
  }

  async function removeDocument(id: number) {
    try {
      await documentApi.delete(id)
      ElMessage.success('删除成功')
      await fetchDocuments()
    } catch {
      ElMessage.error('删除失败')
    }
  }

  return { documents, loading, fetchDocuments, uploadDocument, removeDocument }
})