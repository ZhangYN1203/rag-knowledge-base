import { marked } from 'marked'

// Configure marked for GFM and line breaks
marked.setOptions({
  breaks: true,
  gfm: true
})

// Extend renderer for code blocks
const renderer = new marked.Renderer()

renderer.code = (code: string, infostring: string | undefined) => {
  const language = infostring || ''
  const escaped = code
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  return `<pre><code class="language-${language}">${escaped}</code></pre>`
}

export function renderMarkdown(text: string): string {
  if (!text) return ''
  const result = marked.parse(text)
  return typeof result === 'string' ? result : ''
}

export function formatRelativeTime(dateStr: string): string {
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMin = Math.floor(diffMs / 60000)
  const diffHour = Math.floor(diffMs / 3600000)
  const diffDay = Math.floor(diffMs / 86400000)

  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin} 分钟前`
  if (diffHour < 24) return `${diffHour} 小时前`
  if (diffDay < 7) return `${diffDay} 天前`
  return date.toLocaleDateString('zh-CN')
}

export function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}