import type { AxiosInstance } from 'axios'

declare module 'axios' {
  interface AxiosRequestConfig {
    // 是否自动携带本地 token。传 false 可用于登录、刷新 token 等无需认证的接口。
    addToken?: boolean
  }
}

/**
 * 注册 token 请求拦截器。
 *
 * 默认从 localStorage.token 读取认证信息并写入 Authorization 请求头。
 */
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
