import axios, { type CreateAxiosDefaults } from 'axios'
import { setupTokenInterceptor } from './request-interceptors'
import { setupResponseInterceptors } from './response-interceptors'

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
    super(response.message || `Request failed with code ${response.code}`)
    this.name = 'ApiResponseError'
    this.apiCode = response.code
    this.apiData = response.data
    this.rawData = response
  }
}

export function createHttp(config?: CreateAxiosDefaults) {
  const http = axios.create(config)

  setupTokenInterceptor(http)

  setupResponseInterceptors(http, {
    unwrapResponseData(response) {
      if (isApiResponse(response.data)) {
        if (response.data.code !== 200) {
          throw new ApiResponseError(response.data)
        }
        return response.data.data
      }
      return response.data
    },

    handleError(error) {
      return Promise.reject(error)
    },
  })

  return http
}

function isApiResponse(data: unknown): data is ApiResponse {
  return isRecord(data) && typeof data.code === 'number'
}

function isRecord(data: unknown): data is Record<string, unknown> {
  return typeof data === 'object' && data !== null
}
