import axios, { type CreateAxiosDefaults } from 'axios'
import { handleHttpError, unwrapResponseData } from './handle'
import { setupTokenInterceptor } from './request-interceptors'
import { setupResponseInterceptors } from './response-interceptors'

export { ApiResponseError } from './handle'

/**
 * 创建项目 HTTP 客户端。
 *
 * 所有 axios 实例统一挂载 token 请求拦截器和响应解包拦截器，避免业务侧重复处理。
 */
export function createHttp(config?: CreateAxiosDefaults) {
  const http = axios.create(config)

  setupTokenInterceptor(http)

  setupResponseInterceptors(http, {
    unwrapResponseData,
    handleError: handleHttpError,
  })

  return http
}
