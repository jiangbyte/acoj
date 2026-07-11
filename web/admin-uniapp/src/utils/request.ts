import { clearSessionStorage, getToken } from './session'

export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: Record<string, any>
  addToken?: boolean
  skipErrorMessage?: boolean
  header?: Record<string, string>
}

export class ApiResponseError extends Error {
  code?: number
  statusCode?: number
  raw?: unknown

  constructor(
    message: string,
    code?: number,
    statusCode?: number,
    raw?: unknown
  ) {
    super(message)
    this.name = 'ApiResponseError'
    this.code = code
    this.statusCode = statusCode
    this.raw = raw
  }
}

const baseURL = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '')

export function request<T = any>(url: string, options: RequestOptions = {}) {
  return new Promise<T>((resolve, reject) => {
    const token = getToken()
    const header: Record<string, string> = {
      ...(options.header ?? {}),
    }

    if (options.addToken !== false && token) {
      header.Authorization = token
    }

    uni.request({
      url: `${baseURL}${url}`,
      method: options.method ?? 'GET',
      data: cleanData(options.data ?? {}),
      header,
      success(response) {
        const statusCode = response.statusCode
        if (statusCode === 401) {
          clearSessionStorage()
          reject(
            new ApiResponseError(
              '登录已过期',
              undefined,
              statusCode,
              response.data
            )
          )
          return
        }
        if (statusCode < 200 || statusCode >= 300) {
          const message =
            readMessage(response.data) || `请求失败(${statusCode})`
          showError(message, options.skipErrorMessage)
          reject(
            new ApiResponseError(message, undefined, statusCode, response.data)
          )
          return
        }

        const body = response.data as any
        if (body && typeof body === 'object' && 'code' in body) {
          if (body.code !== 0 && body.code !== 200) {
            const message = body.message || '业务处理失败'
            showError(message, options.skipErrorMessage)
            reject(new ApiResponseError(message, body.code, statusCode, body))
            return
          }
          resolve(body.data as T)
          return
        }

        resolve(body as T)
      },
      fail(error) {
        const message = error.errMsg || '网络请求失败'
        showError(message, options.skipErrorMessage)
        reject(new ApiResponseError(message, undefined, undefined, error))
      },
    })
  })
}

export const http = {
  get<T = any>(
    url: string,
    data?: Record<string, any>,
    options?: RequestOptions
  ) {
    return request<T>(url, { ...(options ?? {}), method: 'GET', data })
  },
  post<T = any>(
    url: string,
    data?: Record<string, any>,
    options?: RequestOptions
  ) {
    return request<T>(url, { ...(options ?? {}), method: 'POST', data })
  },
}

function cleanData(data: Record<string, any>) {
  return Object.fromEntries(
    Object.entries(data).filter(
      ([, value]) => value !== undefined && value !== ''
    )
  )
}

function readMessage(data: unknown) {
  if (!data || typeof data !== 'object') {
    return ''
  }
  return String((data as any).message ?? (data as any).detail ?? '')
}

function showError(message: string, skip?: boolean) {
  if (skip) {
    return
  }
  uni.showToast({
    title: message,
    icon: 'none',
  })
}
