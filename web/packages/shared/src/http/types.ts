import type { AxiosInstance, AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T | null
}

export type RequestErrorType = 'Response Error' | 'Business Error' | 'Network Error' | null
export type RequestCode = string | number

export interface RequestErrorResult {
  errorType: RequestErrorType
  code: RequestCode
  message: string
  data: unknown
}

export interface HttpInstanceHooks {
  getToken?: () => string | null | undefined
  onAuthExpired?: () => void | Promise<void>
  onError?: (error: RequestErrorResult) => void
  onNetworkError?: (error: RequestErrorResult) => void
}

export interface RequestMeta {
  authRole?: 'visitor' | string
  isExpired?: boolean
  isFormPost?: boolean
  isBlob?: boolean
}

export interface HttpRequestConfig<D = unknown> extends AxiosRequestConfig<D> {
  meta?: RequestMeta
}

export interface InternalHttpRequestConfig<D = unknown> extends InternalAxiosRequestConfig<D> {
  meta?: RequestMeta
}

export interface HttpInstanceOptions {
  baseURL: string
  timeout?: number
  beforeRequest?: (config: InternalHttpRequestConfig) => void | Promise<void>
  hooks?: HttpInstanceHooks
}

export interface BackendOptions {
  codeKey?: string
  dataKey?: string
  msgKey?: string
  successCode?: number | string
}

export type BackendConfig = Required<BackendOptions>

export interface HttpClient extends Omit<AxiosInstance, 'request' | 'get' | 'delete' | 'head' | 'options' | 'post' | 'put' | 'patch'> {
  request<T = unknown, D = unknown>(config: HttpRequestConfig<D>): Promise<T>
  get<T = unknown, D = unknown>(url: string, config?: HttpRequestConfig<D>): Promise<T>
  delete<T = unknown, D = unknown>(url: string, config?: HttpRequestConfig<D>): Promise<T>
  head<T = unknown, D = unknown>(url: string, config?: HttpRequestConfig<D>): Promise<T>
  options<T = unknown, D = unknown>(url: string, config?: HttpRequestConfig<D>): Promise<T>
  post<T = unknown, D = unknown>(url: string, data?: D, config?: HttpRequestConfig<D>): Promise<T>
  put<T = unknown, D = unknown>(url: string, data?: D, config?: HttpRequestConfig<D>): Promise<T>
  patch<T = unknown, D = unknown>(url: string, data?: D, config?: HttpRequestConfig<D>): Promise<T>
}
