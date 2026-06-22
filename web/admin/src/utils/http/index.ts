import { createAxiosInstance } from './axios'

export { createAxiosInstance }
export type {
  ApiResponse,
  BackendOptions,
  HttpClient,
  HttpInstanceOptions,
  HttpRequestConfig,
  InternalHttpRequestConfig,
  RequestErrorResult,
  RequestMeta,
  ResponseResult,
} from './types'

export const request = createAxiosInstance({
  baseURL: import.meta.env.VITE_API_BASE_URL,
})

export const blankInstance = createAxiosInstance({
  baseURL: '',
})

export const blankRequest = blankInstance
