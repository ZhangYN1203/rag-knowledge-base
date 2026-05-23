import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useDocumentStore } from '@/stores/documentStore'
import { useTemplateStore } from '@/stores/templateStore'
import { useConfigStore } from '@/stores/configStore'
import { documentApi, templateApi, configApi } from '@/api/types'

vi.mock('@/api/types', () => ({
  documentApi: {
    getList: vi.fn(),
    upload: vi.fn(),
    delete: vi.fn(),
  },
  templateApi: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
  },
  configApi: {
    getAiConfig: vi.fn(),
    updateAiConfig: vi.fn(),
  }
}))

describe('documentStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('should fetch documents', async () => {
    vi.mocked(documentApi.getList).mockResolvedValue({
      data: [{ id: 1, title: 'doc1', filename: 'f1', processed: true }]
    })

    const store = useDocumentStore()
    await store.fetchDocuments()

    expect(store.documents).toHaveLength(1)
    expect(store.documents[0].title).toBe('doc1')
  })

  it('should upload a document', async () => {
    vi.mocked(documentApi.upload).mockResolvedValue({})
    vi.mocked(documentApi.getList).mockResolvedValue({ data: [] })

    const store = useDocumentStore()
    const file = new File(['test'], 'test.txt', { type: 'text/plain' })
    await store.uploadDocument(file, 'general')

    expect(documentApi.upload).toHaveBeenCalledWith(file, 'general')
  })

  it('should remove a document', async () => {
    vi.mocked(documentApi.delete).mockResolvedValue({})
    vi.mocked(documentApi.getList).mockResolvedValue({ data: [] })

    const store = useDocumentStore()
    await store.removeDocument(1)

    expect(documentApi.delete).toHaveBeenCalledWith(1)
  })
})

describe('templateStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('should fetch templates', async () => {
    vi.mocked(templateApi.list).mockResolvedValue({
      data: [{ id: 1, name: 't1', content: 'c1' }]
    })

    const store = useTemplateStore()
    await store.fetchTemplates()

    expect(store.templates).toHaveLength(1)
    expect(store.templates[0].name).toBe('t1')
  })

  it('should filter templates by category', async () => {
    vi.mocked(templateApi.list).mockResolvedValue({ data: [] })

    const store = useTemplateStore()
    await store.fetchTemplates('rag')

    expect(templateApi.list).toHaveBeenCalledWith('rag')
  })

  it('should create a template', async () => {
    vi.mocked(templateApi.create).mockResolvedValue({})
    vi.mocked(templateApi.list).mockResolvedValue({ data: [] })

    const store = useTemplateStore()
    await store.createTemplate({ name: 'new', content: 'hello {{name}}' })

    expect(templateApi.create).toHaveBeenCalledWith({ name: 'new', content: 'hello {{name}}' })
  })

  it('should delete a template', async () => {
    vi.mocked(templateApi.delete).mockResolvedValue({})
    vi.mocked(templateApi.list).mockResolvedValue({ data: [] })

    const store = useTemplateStore()
    await store.deleteTemplate(1)

    expect(templateApi.delete).toHaveBeenCalledWith(1)
  })
})

describe('configStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('should fetch config', async () => {
    vi.mocked(configApi.getAiConfig).mockResolvedValue({
      data: { provider: 'ollama', chatModel: 'qwen2:7b', temperature: 0.7, maxTokens: 2048 }
    })

    const store = useConfigStore()
    await store.fetchConfig()

    expect(store.config?.provider).toBe('ollama')
    expect(store.config?.chatModel).toBe('qwen2:7b')
  })

  it('should update config', async () => {
    vi.mocked(configApi.updateAiConfig).mockResolvedValue({})
    vi.mocked(configApi.getAiConfig).mockResolvedValue({ data: { provider: 'ollama' } })

    const store = useConfigStore()
    await store.updateConfig({ provider: 'ollama', temperature: 0.8 })

    expect(configApi.updateAiConfig).toHaveBeenCalledWith({ provider: 'ollama', temperature: 0.8 })
  })
})