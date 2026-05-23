import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/authStore'
import { authApi } from '@/api/types'

vi.mock('@/api/types', () => ({
  authApi: {
    login: vi.fn(),
    register: vi.fn(),
  }
}))

describe('authStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('should start with unauthenticated state', () => {
    const store = useAuthStore()
    expect(store.isAuthenticated).toBe(false)
    expect(store.user).toBeNull()
    expect(store.token).toBe('')
  })

  it('should load token from localStorage', () => {
    localStorage.setItem('accessToken', 'test-token')
    const store = useAuthStore()
    expect(store.token).toBe('test-token')
    expect(store.isAuthenticated).toBe(true)
  })

  it('should login successfully', async () => {
    const mockResponse = {
      data: {
        accessToken: 'new-token',
        refreshToken: 'new-refresh',
        user: { id: 1, username: 'testuser', email: 'test@test.com', role: 'USER' }
      }
    }
    vi.mocked(authApi.login).mockResolvedValue(mockResponse)

    const store = useAuthStore()
    const result = await store.login({ username: 'testuser', password: 'password' })

    expect(result).toBe(true)
    expect(store.token).toBe('new-token')
    expect(store.isAuthenticated).toBe(true)
    expect(store.user?.username).toBe('testuser')
    expect(localStorage.getItem('accessToken')).toBe('new-token')
  })

  it('should register successfully', async () => {
    const mockResponse = {
      data: {
        accessToken: 'reg-token',
        refreshToken: 'reg-refresh',
        user: { id: 1, username: 'newuser', email: 'new@test.com', role: 'USER' }
      }
    }
    vi.mocked(authApi.register).mockResolvedValue(mockResponse)

    const store = useAuthStore()
    const result = await store.register({ username: 'newuser', password: 'password', email: 'new@test.com' })

    expect(result).toBe(true)
    expect(store.token).toBe('reg-token')
    expect(store.user?.username).toBe('newuser')
  })

  it('should logout and clear state', () => {
    localStorage.setItem('accessToken', 'test-token')
    localStorage.setItem('refreshToken', 'test-refresh')

    const store = useAuthStore()
    store.logout()

    expect(store.token).toBe('')
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(localStorage.getItem('accessToken')).toBeNull()
    expect(localStorage.getItem('refreshToken')).toBeNull()
  })
})