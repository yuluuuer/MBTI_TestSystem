import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const loading = ref(false)

  const isLoggedIn = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function fetchMe() {
    loading.value = true
    try {
      const res = await api.get('/api/auth/me')
      user.value = res.data.user
    } catch {
      user.value = null
    } finally {
      loading.value = false
    }
  }

  async function login(email, password) {
    const res = await api.post('/api/auth/login', { email, password })
    if (res.data.ok) {
      user.value = res.data.user
      return true
    }
    throw new Error(res.data.message || '登录失败')
  }

  async function register(email, password, profile) {
    const res = await api.post('/api/auth/register', { email, password, ...profile })
    if (res.data.ok) {
      user.value = res.data.user
      return true
    }
    throw new Error(res.data.message || '注册失败')
  }

  async function logout() {
    await api.post('/api/auth/logout')
    user.value = null
  }

  return { user, loading, isLoggedIn, isAdmin, fetchMe, login, register, logout }
})
