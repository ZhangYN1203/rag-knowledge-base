import api from './index'

// ---- Auth Types ----
export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email: string
}

export interface UserResponse {
  id: number
  username: string
  email: string
  role: string
  createdAt: string
}

// ---- Chat Types ----
export interface ChatRequest {
  message: string
  conversationId?: string
}

export interface ChatResponse {
  conversationId: string
  answer: string
  sources: ReferenceSource[]
  modelName: string
  tokenUsage: TokenUsage | null
  createdAt: string
}

export interface ReferenceSource {
  title: string
  excerpt: string
  similarity: number
}

export interface TokenUsage {
  promptTokens: number
  completionTokens: number
  totalTokens: number
}

export interface ConversationSummary {
  conversationId: string
  title: string
  createdAt: string
  lastMessageAt: string
  messageCount: number
}

// ---- Document Types ----
export interface Document {
  id: number
  title: string
  filename: string
  extension: string
  category: string
  fileSize: number
  chunkCount: number
  processed: boolean
  createdAt: string
}

// ---- Prompt Template Types ----
export interface PromptTemplate {
  id: number
  name: string
  category: string
  content: string
  description: string
  variables: string
  usageCount: number
  createdAt: string
  updatedAt: string
}

export interface PromptTemplateRequest {
  name: string
  content: string
  category?: string
  description?: string
  variables?: string
}

// ---- Config Types ----
export interface AiConfig {
  provider: 'ollama' | 'openai'
  chatModel: string
  embeddingModel: string
  baseUrl: string
  temperature: number
  maxTokens: number
}

export interface AiConfigRequest {
  provider: 'ollama' | 'openai'
  chatModel?: string
  embeddingModel?: string
  apiKey?: string
  baseUrl?: string
  temperature?: number
  maxTokens?: number
}

// ---- API Endpoints ----
export const authApi = {
  login: (data: LoginRequest) => api.post('/auth/login', data),
  register: (data: RegisterRequest) => api.post('/auth/register', data),
  refreshToken: (refreshToken: string) => api.post('/auth/refresh', null, { params: { refreshToken } })
}

export const chatApi = {
  chat: (data: ChatRequest) => api.post('/chat', data),
  getHistory: (conversationId: string) => api.get(`/chat/history/${conversationId}`),
  getConversations: () => api.get('/chat/conversations'),
  delete: (conversationId: string) => api.delete(`/chat/history/${conversationId}`)
}

export const documentApi = {
  upload: (file: File, category: string) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('category', category)
    return api.post('/documents', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  getList: () => api.get('/documents'),
  delete: (id: number) => api.delete(`/documents/${id}`)
}

export const templateApi = {
  list: (category?: string) => api.get('/templates', { params: { category } }),
  getById: (id: number) => api.get(`/templates/${id}`),
  create: (data: PromptTemplateRequest) => api.post('/templates', data),
  update: (id: number, data: PromptTemplateRequest) => api.put(`/templates/${id}`, data),
  delete: (id: number) => api.delete(`/templates/${id}`),
  render: (id: number, variables: Record<string, string>) => api.post(`/templates/render/${id}`, variables)
}

export const configApi = {
  getAiConfig: () => api.get('/config/ai'),
  updateAiConfig: (data: AiConfigRequest) => api.put('/config/ai', data)
}