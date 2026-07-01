import type { AxiosInstance } from 'axios'

declare module 'axios' {
  interface AxiosRequestConfig {
    // 是否自动携带本地 token。传 false 可用于登录、刷新 token 等无需认证的接口。
    addToken?: boolean

    // 是否跳过统一错误消息提示。适合调用方自行处理错误反馈的请求。
    skipErrorMessage?: boolean

    // 自定义错误消息，优先级高于后端返回的 message。
    customErrorMessage?: string
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
