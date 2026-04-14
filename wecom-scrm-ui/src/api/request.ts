import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  paramsSerializer: {
    indexes: null // serialize arrays as tagIds=a&tagIds=b (no brackets)
  }
})

request.interceptors.request.use(
  config => {
    // Attach token if needed
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    const currentCorpId = localStorage.getItem('currentCorpId')
    if (currentCorpId) {
      config.headers['X-Corp-Id'] = currentCorpId
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    const errorMessage = error.response?.data?.message || error.message || 'API Request Failed'
    
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      // Only redirect if not already on login page
      if (!window.location.pathname.endsWith('/login')) {
        window.location.href = '/login'
      }
    }
    
    ElMessage.error(errorMessage)
    return Promise.reject(error)
  }
)

export default request
