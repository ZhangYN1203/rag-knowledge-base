<template>
  <div class="templates-container">
    <div class="header">
      <h1>Prompt 模板管理</h1>
      <div class="header-actions">
        <router-link to="/"><el-button type="primary" size="small">返回聊天</el-button></router-link>
        <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
      </div>
    </div>

    <div class="content">
      <el-card class="toolbar">
        <el-button type="primary" @click="showCreateDialog">创建模板</el-button>
        <el-select v-model="categoryFilter" placeholder="分类筛选" clearable style="width: 150px; margin-left: 12px;" @change="loadTemplates">
          <el-option label="默认" value="default" />
          <el-option label="RAG" value="rag" />
          <el-option label="通用" value="general" />
        </el-select>
      </el-card>

      <el-card>
        <el-table :data="templateStore.templates" style="width: 100%" v-loading="templateStore.loading">
          <el-table-column prop="name" label="名称" min-width="150" />
          <el-table-column prop="category" label="分类" width="100" />
          <el-table-column label="内容预览" min-width="300">
            <template #default="{ row }">
              <div class="content-preview">{{ row.content.substring(0, 100) }}{{ row.content.length > 100 ? '...' : '' }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="usageCount" label="使用次数" width="80" />
          <el-table-column prop="updatedAt" label="更新时间" width="160">
            <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑模板' : '创建模板'" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="模板唯一名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" style="width: 100%">
            <el-option label="默认" value="default" />
            <el-option label="RAG" value="rag" />
            <el-option label="通用" value="general" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="模板描述" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="模板内容，使用 {{variable}} 语法定义变量" />
        </el-form-item>
        <el-form-item v-if="form.content" label="变量">
          <div class="variable-preview">
            <el-tag v-for="v in extractedVars" :key="v" style="margin: 2px">{{ v }}</el-tag>
            <span v-if="extractedVars.length === 0" class="no-vars">未检测到变量</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTemplateStore } from '@/stores/templateStore'

const router = useRouter()
const templateStore = useTemplateStore()
const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const categoryFilter = ref('')
const formRef = ref()

const form = ref({
  name: '',
  category: 'default',
  content: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  content: [{ required: true, message: '请输入模板内容', trigger: 'blur' }]
}

const extractedVars = computed(() => {
  const matches = form.value.content.match(/\{\{(\w+)\}\}/g)
  if (!matches) return []
  return [...new Set(matches.map(m => m.slice(2, -2)))]
})

function formatTime(dateStr: string) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

function showCreateDialog() {
  isEditing.value = false
  editingId.value = null
  form.value = { name: '', category: 'default', content: '', description: '' }
  dialogVisible.value = true
}

function showEditDialog(row: any) {
  isEditing.value = true
  editingId.value = row.id
  form.value = {
    name: row.name,
    category: row.category,
    content: row.content,
    description: row.description || ''
  }
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (isEditing.value && editingId.value) {
      await templateStore.updateTemplate(editingId.value, form.value)
    } else {
      await templateStore.createTemplate(form.value)
    }
    dialogVisible.value = false
  } catch {
    // Error handled in store
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await templateStore.deleteTemplate(id)
  } catch {
    // Error handled in store
  }
}

async function loadTemplates() {
  await templateStore.fetchTemplates(categoryFilter.value || undefined)
}

const handleLogout = () => {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  router.push('/login')
}

onMounted(() => {
  loadTemplates()
})
</script>

<style scoped>
.templates-container {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 20px;
}

.header {
  max-width: 1200px;
  margin: 0 auto 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 { font-size: 24px; color: #333; }
.header-actions { display: flex; gap: 10px; }

.content { max-width: 1200px; margin: 0 auto; display: flex; flex-direction: column; gap: 16px; }

.toolbar { display: flex; align-items: center; }

.content-preview {
  color: #888;
  font-size: 13px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.variable-preview {
  min-height: 28px;
}

.no-vars { color: #999; font-size: 13px; }
</style>