import axios, { type CreateAxiosDefaults } from 'axios'
import { setupTokenInterceptor } from './request-interceptors'
import { setupResponseInterceptors } from './response-interceptors'

/**
 * 后端统一响应结构。
 *
 * 当前约定 code=200 表示业务成功，data 才是业务真正需要的数据。
 */
interface ApiResponse<T = unknown> {
  code: number
  message?: string
  data: T
}

/**
 * 业务响应错误。
 *
 * HTTP 请求本身成功，但后端业务 code 非 200 时抛出该错误，便于调用方区分网络错误和业务错误。
 */
export class ApiResponseError<T = unknown> extends Error {
  // 后端业务状态码。
  readonly apiCode: number

  // 后端返回的业务 data，失败时也可能携带补充信息。
  readonly apiData: T

  // 完整原始业务响应体。
  readonly rawData: ApiResponse<T>

  constructor(response: ApiResponse<T>) {
    super(response.message || `Request failed with code ${response.code}`)
    this.name = 'ApiResponseError'
    this.apiCode = response.code
    this.apiData = response.data
    this.rawData = response
  }
}

/**
 * 创建项目 HTTP 客户端。
 *
 * 所有 axios 实例统一挂载 token 请求拦截器和响应解包拦截器，避免业务侧重复处理。
 */
export function createHttp(config?: CreateAxiosDefaults) {
  const http = axios.create(config)

  setupTokenInterceptor(http)

  setupResponseInterceptors(http, {
    // 统一解包后端响应：ApiResponse 成功时返回 data，失败时抛 ApiResponseError。
    unwrapResponseData(response) {
      if (isApiResponse(response.data)) {
        if (response.data.code !== 200) {
          throw new ApiResponseError(response.data)
        }
        return response.data.data
      }
      return response.data
    },

    // 当前只做错误标准化，不在工具层吞掉异常。
    handleError(error) {
      return Promise.reject(error)
    },
  })

  return http
}

// 判断数据是否符合后端统一响应结构。这里只要求存在数字 code，保持兼容性。
function isApiResponse(data: unknown): data is ApiResponse {
  return isRecord(data) && typeof data.code === 'number'
}

// unknown 到普通对象的基础类型保护，避免直接访问空值或原始类型属性。
function isRecord(data: unknown): data is Record<string, unknown> {
  return typeof data === 'object' && data !== null
}
