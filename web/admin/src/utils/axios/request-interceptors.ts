import type { AxiosInstance } from 'axios'

declare module 'axios' {
  interface AxiosRequestConfig {
    /**
     * 是否需要携带 token
     * @default true
     */
    addToken?: boolean
  }
}

export function setupTokenInterceptor(http: AxiosInstance) {
  http.interceptors.request.use((config) => {
    if (config.addToken !== false) {
      const token = localStorage.getItem('token')
      if (token) {
        config.headers.set('Authorization', token)
      }
    }
    return config
  })
}
