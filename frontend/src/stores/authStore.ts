import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, type LoginRequest, type RegisterRequest, type UserResponse } from '@/api/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserResponse | null>(null)
  const token = ref(localStorage.getItem('accessToken') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const loading = ref(false)

  const isAuthenticated = computed(() => !!token.value)

  async function login(credentials: LoginRequest) {
    loading.value = true
    try {
      const res: any = await authApi.login(credentials)
      const data = res.data
      token.value = data.accessToken
      refreshToken.value = data.refreshToken
      user.value = data.user
      localStorage.setItem('accessToken', data.accessToken)
      localStorage.setItem('refreshToken', data.refreshToken)
      return true
    } finally {
      loading.value = false
    }
  }

  async function register(data: RegisterRequest) {
    loading.value = true
    try {
      const res: any = await authApi.register(data)
      const result = res.data
      token.value = result.accessToken
      refreshToken.value = result.refreshToken
      user.value = result.user
      localStorage.setItem('accessToken', result.accessToken)
      localStorage.setItem('refreshToken', result.refreshToken)
      return true
    } finally {
      loading.value = false
    }
  }

  function logout() {
    user.value = null
    token.value = ''
    refreshToken.value = ''
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  return { user, token, refreshToken, loading, isAuthenticated, login, register, logout }
})