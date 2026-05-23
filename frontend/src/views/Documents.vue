<template>
  <div class="documents-container">
    <div class="header">
      <h1>知识库管理</h1>
      <div class="actions">
        <router-link to="/">
          <el-button type="primary" size="small">返回聊天</el-button>
        </router-link>
        <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
      </div>
    </div>

    <div class="content">
      <el-card class="upload-card">
        <template #header>上传文档</template>
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
            拖拽文件到此处或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 txt/pdf/docx 格式，最大 50MB
            </div>
          </template>
        </el-upload>
      </el-card>

      <el-card class="list-card">
        <template #header>我的文档</template>
        <el-table :data="documents" style="width: 100%">
          <el-table-column prop="title" label="文件名" />
          <el-table-column prop="category" label="分类" width="100" />
          <el-table-column prop="fileSize" label="大小" width="100">
            <template #default="{ row }">
              {{ formatFileSize(row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column prop="chunkCount" label="分块数" width="80" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.processed ? 'success' : 'warning'">
                {{ row.processed ? '已处理' : '处理中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="danger" size="small" @click="handleDelete(row.id)">
                删除
              </el-button>
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
import { documentApi } from '@/api/types'

const router = useRouter()
const token = ref(localStorage.getItem('accessToken') || '')
const documents = ref<any[]>([])

const formatFileSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const loadDocuments = async () => {
  try {
    const res = await documentApi.getList()
    documents.value = res.data || []
  } catch (error: any) {
    ElMessage.error('加载文档列表失败')
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
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  router.push('/login')
}

onMounted(() => {
  loadDocuments()
})
</script>

<style scoped>
.documents-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.header {
  max-width: 1200px;
  margin: 0 auto 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  color: white;
  font-size: 28px;
}

.actions {
  display: flex;
  gap: 10px;
}

.content {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  gap: 20px;
}

.upload-card, .list-card {
  background: white;
}
</style>
