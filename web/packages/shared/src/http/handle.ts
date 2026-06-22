import { ERROR_NO_TIP_STATUS, ERROR_STATUS } from './config'
import type { AxiosResponse } from 'axios'
import type {
  ApiResponse,
  BackendConfig,
  HttpInstanceHooks,
  RequestCode,
  RequestErrorResult,
  ResponseResult,
} from './types'

export function parseApiResponse(response: AxiosResponse) {
  const data = response.data
  if (!data || typeof data !== 'object' || data instanceof Blob) {
    return null
  }
  return data as ApiResponse<unknown>
}

export function handleResponseError(response: AxiosResponse, hooks?: HttpInstanceHooks): RequestErrorResult {
  const error = {
    errorType: 'Response Error',
    code: response.status,
    message: ERROR_STATUS[response.status] || response.statusText || ERROR_STATUS.default,
    data: null,
  } satisfies RequestErrorResult

  showError(error, hooks)
  return error
}

export function handleBusinessError(
  data: Record<string, unknown>,
  config: BackendConfig,
  hooks?: HttpInstanceHooks,
): RequestErrorResult {
  const error = {
    errorType: 'Business Error',
    code: (data[config.codeKey] as RequestCode | undefined) || 0,
    message: String(data[config.msgKey] || '请求失败'),
    data: data[config.dataKey] ?? null,
  } satisfies RequestErrorResult

  showError(error, hooks)
  return error
}

export function handleNetworkError(hooks?: HttpInstanceHooks): RequestErrorResult {
  const result = {
    errorType: 'Network Error',
    code: 'NETWORK_ERROR',
    message: '网络连接失败，请检查后端服务是否可用',
    data: null,
  } satisfies RequestErrorResult

  hooks?.onNetworkError?.(result)
  return result
}

export function handleServiceResult<T>(data: T, isSuccess = true): ResponseResult<T | null> {
  if (!isSuccess) {
    const error = data as RequestErrorResult
    return {
      isSuccess,
      errorType: error.errorType,
      code: error.code,
      message: error.message,
      data: null,
    }
  }

  return {
    isSuccess,
    errorType: null,
    code: 200,
    message: 'success',
    data,
  }
}

export function showError(error: RequestErrorResult, hooks?: HttpInstanceHooks) {
  if (ERROR_NO_TIP_STATUS.includes(error.code)) {
    return
  }

  hooks?.onError?.(error)
}
