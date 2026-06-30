import axios from 'axios'

const api = axios.create({
  baseURL: '',
  withCredentials: true
})

// Response interceptor for error handling
api.interceptors.response.use(
  response => response,
  error => {
    const status = error.response?.status
    const message = error.response?.data?.message

    if (status === 401) {
      // Unauthorized - redirect to login
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }

    return Promise.reject(new Error(message || (status === 401 ? '请先登录或账号密码错误' : '操作失败')))
  }
)

export default api
