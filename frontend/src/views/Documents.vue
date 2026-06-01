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
          <h1>知识库管理</h1>
          <p class="header-desc">管理您的文档，构建个性化知识库</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="danger" size="small" plain @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <div class="page-content">
      <el-card shadow="never" class="upload-card">
        <template #header>
          <div class="card-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#667eea" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="17 8 12 3 7 8"/>
              <line x1="12" y1="3" x2="12" y2="15"/>
            </svg>
            上传文档
          </div>
        </template>
        <el-upload
          drag
          :auto-upload="true"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :headers="{ Authorization: `Bearer ${token}` }"
          action="/api/documents"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 txt / pdf / docx 格式，最大 50MB
            </div>
          </template>
        </el-upload>
      </el-card>

      <el-card shadow="never" class="list-card">
        <template #header>
          <div class="card-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#667eea" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
            我的文档
          </div>
        </template>
        <el-table :data="documents" style="width: 100%" v-loading="loading" empty-text="暂无文档">
          <el-table-column prop="title" label="文件名" min-width="200">
            <template #default="{ row }">
              <div class="file-cell">
                <svg v-if="row.extension === '.pdf'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#e74c3c" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
                </svg>
                <svg v-else-if="row.extension === '.docx'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#3498db" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
                <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#888" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
                <span>{{ row.title }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="category" label="分类" width="100" />
          <el-table-column prop="fileSize" label="大小" width="100">
            <template #default="{ row }">
              {{ formatFileSize(row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column prop="chunkCount" label="分块" width="70" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.processed ? 'success' : 'warning'" size="small">
                {{ row.processed ? '已处理' : '处理中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" size="small" text @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { documentApi, type Document } from '@/api/types'
import { useAuthStore } from '@/stores/authStore'
import { formatFileSize } from '@/utils/markdown'

const router = useRouter()
const authStore = useAuthStore()
const token = ref(localStorage.getItem('accessToken') || '')
const documents = ref<Document[]>([])
const loading = ref(false)

const loadDocuments = async () => {
  loading.value = true
  try {
    const res = await documentApi.getList()
    documents.value = res.data || []
  } catch (error: any) {
    ElMessage.error('加载文档列表失败')
  } finally {
    loading.value = false
  }
}

const handleUploadSuccess = () => {
  ElMessage.success('上传成功')
  loadDocuments()
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

const handleDelete = async (id: number) => {
  try {
    await documentApi.delete(id)
    ElMessage.success('删除成功')
    loadDocuments()
  } catch (error: any) {
    ElMessage.error('删除失败')
  }
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadDocuments()
})
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 24px;
}

.page-header {
  max-width: 1200px;
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
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  gap: 20px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.upload-card :deep(.el-card__body) {
  padding: 20px;
}

.file-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.file-cell span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>