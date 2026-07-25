import axios, { type AxiosInstance, type AxiosResponse } from 'axios'
import { AxiosError, isAxiosError } from 'axios'

// --- Type augmentations ---

declare module 'axios' {
  interface AxiosRequestConfig {
    /** 跳过携带 token，用于登录等无需认证的接口 */
    addToken?: boolean
    /** 跳过自动错误提示，由调用方自行处理 */
    skipErrorMessage?: boolean
    /** 自定义错误消息，优先级高于后端返回的 message */
    customErrorMessage?: string
  }
  interface AxiosResponse {
    /** 被解包前的原始响应数据 */
    rawData?: unknown
  }
}

// --- Constants ---

const loginPath = '/auth/login'
let isHandlingUnauthorized = false

const httpStatusMessageMap: Record<number, string> = {
  400: '请求参数错误',
  401: '登录已过期，请重新登录',
  403: '无权访问',
  404: '资源不存在',
  422: '校验失败',
  500: '服务器错误',
  502: '网关错误',
  503: '服务不可用',
  504: '网关超时',
}

// --- Backend response types ---

interface ApiResponse<T = unknown> {
  code: number
  message?: string
  data: T
}

export class ApiResponseError<T = unknown> extends Error {
  readonly apiCode: number
  readonly apiData: T
  readonly rawData: ApiResponse<T>

  constructor(response: ApiResponse<T>) {
    super(response.message || `请求失败，错误码 ${response.code}`)
    this.name = 'ApiResponseError'
    this.apiCode = response.code
    this.apiData = response.data
    this.rawData = response
  }
}

// --- Type guards ---

function isRecord(data: unknown): data is Record<string, unknown> {
  return typeof data === 'object' && data !== null
}

function isApiResponse(data: unknown): data is ApiResponse {
  return isRecord(data) && typeof data.code === 'number'
}

// --- Response unwrapping ---

function unwrapResponseData(response: AxiosResponse) {
  if (isApiResponse(response.data)) {
    if (response.data.code !== 200) {
      throw new ApiResponseError(response.data)
    }
    return response.data.data
  }
  return response.data
}

// --- Error helpers ---

function isUnauthorizedError(error: AxiosError) {
  return error.response?.status === 401 || getApiCode(error) === 401
}

function getApiCode(error: AxiosError) {
  const apiCode = (error as Record<string, any>).apiCode
  if (typeof apiCode === 'number') {
    return apiCode
  }

  const responseData = error.response?.data
  if (isRecord(responseData) && typeof responseData.code === 'number') {
    return responseData.code
  }

  const rawData = error.response?.rawData
  if (isRecord(rawData) && typeof rawData.code === 'number') {
    return rawData.code
  }

  return undefined
}

function getResponseMessage(data: unknown) {
  if (isRecord(data) && typeof data.message === 'string') {
    return data.message
  }
  return undefined
}

function getErrorMessage(error: AxiosError) {
  const customErrorMessage = error.config?.customErrorMessage
  if (customErrorMessage) {
    return customErrorMessage
  }

  const responseMessage = getResponseMessage(error.response?.data)
  if (responseMessage) {
    return responseMessage
  }

  const status = error.response?.status
  if (status) {
    return httpStatusMessageMap[status] ?? `请求失败(${status})`
  }

  return 'Network error. Please try again later.'
}

function showErrorMessage(error: AxiosError) {
  if (error.config?.skipErrorMessage) {
    return
  }

  const message = getErrorMessage(error)
  if (message && import.meta.client) {
    try {
      const toast = useToast()
      toast.add({ title: '请求失败', description: message, color: 'error' })
    } catch {
      // 不在 Nuxt 上下文时静默跳过
    }
  }
}

function handleUnauthorizedError(error: AxiosError) {
  if (isHandlingUnauthorized) {
    return
  }

  isHandlingUnauthorized = true

  // 显示登录过期提示（仅客户端）
  if (import.meta.client) {
    const message = getErrorMessage(error)
    if (message) {
      try {
        const toast = useToast()
        toast.add({ title: '登录过期', description: message, color: 'error' })
      } catch {
        // 静默跳过
      }
    }
  }

  void redirectToLogin().finally(() => {
    window.setTimeout(() => {
      isHandlingUnauthorized = false
    }, 1000)
  })
}

async function redirectToLogin() {
  // 清除 token cookie
  try {
    const tokenCookie = useCookie('token')
    tokenCookie.value = null
  } catch {
    // 不在 Nuxt 上下文时跳过
  }

  // 用当前路径构造重定向，已在登录页则不添加 redirect
  try {
    const currentRoute = useRoute()
    if (currentRoute.path.startsWith('/auth')) {
      if (currentRoute.path !== loginPath) {
        await navigateTo(loginPath)
      }
      return
    }

    await navigateTo({
      path: loginPath,
      query: { redirect: currentRoute.fullPath },
    })
  } catch {
    // 后备方案：直接跳转
    window.location.href = loginPath
  }
}

function handleHttpError(error: AxiosError) {
  if (isUnauthorizedError(error) && error.config?.addToken !== false) {
    handleUnauthorizedError(error)
    return Promise.reject(error)
  }

  showErrorMessage(error)
  return Promise.reject(error)
}

// --- Error normalization ---

function toAxiosResponseError(error: unknown, response: AxiosResponse) {
  if (isAxiosError(error)) {
    return error
  }

  return AxiosError.from(
    toError(error),
    AxiosError.ERR_BAD_RESPONSE,
    response.config,
    response.request,
    response,
    getErrorCustomProps(error),
  )
}

function toAxiosError(error: unknown) {
  if (isAxiosError(error)) {
    return error
  }

  return AxiosError.from(toError(error))
}

function toError(error: unknown) {
  if (error instanceof Error) {
    return error
  }

  return new Error(String(error ?? 'Unknown Error'))
}

function getErrorCustomProps(error: unknown) {
  if (typeof error !== 'object' || error === null) {
    return undefined
  }

  const props = Object.fromEntries(Object.entries(error))
  return Object.keys(props).length > 0 ? props : undefined
}

// --- HTTP client factory ---

function createHttpClient(): AxiosInstance {
  const http = axios.create({
    timeout: 10000,
  })

  // 请求拦截器：取 runtimeConfig 设置 baseURL，携带 token
  http.interceptors.request.use((config) => {
    try {
      config.baseURL = useRuntimeConfig().public.apiBaseUrl || '/api'
    } catch {
      config.baseURL = config.baseURL || '/api'
    }

    if (config.addToken !== false) {
      try {
        const token = useCookie<string>('token').value
        if (token) {
          config.headers.set('Authorization', token)
        }
      } catch {
        // 不在 Nuxt 上下文时跳过
      }
    }

    return config
  })

  // 响应拦截器：解包 data + 统一错误处理
  http.interceptors.response.use(
    (response) => {
      try {
        response.rawData = response.data
        response.data = unwrapResponseData(response)
      } catch (e: unknown) {
        throw toAxiosResponseError(e, response)
      }
      return response
    },
    (error: unknown) => {
      const axiosError = toAxiosError(error)
      return handleHttpError(axiosError)
    },
  )

  return http
}

// --- Singleton and export ---

const http = createHttpClient()

/** 组件中使用（Nuxt 自动导入） */
export function useHttp(): AxiosInstance {
  return http
}

/** API 模块中显式导入 */
export { http }
