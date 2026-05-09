import { createAlova } from 'alova'
import adapterFetch from 'alova/fetch'
import VueHook from 'alova/vue'
import { BACKEND_CONFIG } from './config'
import { useAuthStore } from '@/store/auth'

const baseURL = import.meta.env.VITE_API_BASE_URL as string || '/api'

export const request = createAlova({
  statesHook: VueHook,
  requestAdapter: adapterFetch(),
  baseURL,
  timeout: 15000,
  cacheFor: null,
  beforeRequest(method) {
    const authStore = useAuthStore()
    if (authStore.token) {
      method.config.headers = {
        ...method.config.headers,
        Authorization: authStore.token,
      }
    }
  },
  responded: {
    onSuccess: async (response) => {
      const { status } = response
      if (status === 200) {
        const data = await response.json()
        if (data[BACKEND_CONFIG.codeKey] === BACKEND_CONFIG.successCode) {
          return { isSuccess: true, data: data[BACKEND_CONFIG.dataKey] }
        }
        return { isSuccess: false, message: data[BACKEND_CONFIG.msgKey], data: data[BACKEND_CONFIG.dataKey] }
      }
      if (status === 401) {
        const authStore = useAuthStore()
        authStore.logout()
        return { isSuccess: false, message: '登录已过期', data: null }
      }
      return { isSuccess: false, message: `请求失败(${status})`, data: null }
    },
    onError: (error) => {
      return { isSuccess: false, message: error.message || '网络错误', data: null }
    },
  },
})
