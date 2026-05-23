import { describe, it, expect } from 'vitest'
import { renderMarkdown, formatRelativeTime, formatFileSize } from '@/utils/markdown'

describe('renderMarkdown', () => {
  it('should return empty string for empty input', () => {
    expect(renderMarkdown('')).toBe('')
    expect(renderMarkdown(null as unknown as string)).toBe('')
    expect(renderMarkdown(undefined as unknown as string)).toBe('')
  })

  it('should render plain text', () => {
    const result = renderMarkdown('Hello world')
    expect(result).toContain('Hello world')
  })

  it('should render bold text', () => {
    const result = renderMarkdown('**bold**')
    expect(result).toContain('<strong>bold</strong>')
  })

  it('should render code blocks with language class', () => {
    const result = renderMarkdown('```python\nprint("hello")\n```')
    expect(result).toContain('class="language-python"')
    expect(result).toContain('print')
  })

  it('should render inline code', () => {
    const result = renderMarkdown('Use `code` here')
    expect(result).toContain('<code>code</code>')
  })

  it('should handle multiple lines with breaks', () => {
    const result = renderMarkdown('Line 1\nLine 2')
    expect(result).toContain('Line 1')
    expect(result).toContain('Line 2')
  })
})

describe('formatRelativeTime', () => {
  it('should return "刚刚" for recent dates', () => {
    const now = new Date().toISOString()
    expect(formatRelativeTime(now)).toBe('刚刚')
  })

  it('should return minutes ago', () => {
    const past = new Date(Date.now() - 5 * 60000).toISOString()
    expect(formatRelativeTime(past)).toBe('5 分钟前')
  })

  it('should return hours ago', () => {
    const past = new Date(Date.now() - 3 * 3600000).toISOString()
    expect(formatRelativeTime(past)).toBe('3 小时前')
  })

  it('should return days ago', () => {
    const past = new Date(Date.now() - 4 * 86400000).toISOString()
    expect(formatRelativeTime(past)).toBe('4 天前')
  })

  it('should return date for older dates', () => {
    const past = new Date('2024-01-01').toISOString()
    const result = formatRelativeTime(past)
    expect(result).toBe('2024/1/1')
  })
})

describe('formatFileSize', () => {
  it('should format bytes', () => {
    expect(formatFileSize(500)).toBe('500 B')
  })

  it('should format KB', () => {
    expect(formatFileSize(2048)).toBe('2.0 KB')
  })

  it('should format MB', () => {
    expect(formatFileSize(5 * 1024 * 1024)).toBe('5.0 MB')
  })

  it('should handle edge case of 0 bytes', () => {
    expect(formatFileSize(0)).toBe('0 B')
  })
})