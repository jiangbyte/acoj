import axios, { AxiosError, AxiosHeaders } from 'axios'
import type { AxiosResponse } from 'axios'

import { DEFAULT_BACKEND_OPTIONS, DEFAULT_HTTP_OPTIONS } from './config'
import {
  handleBusinessError,
  handleNetworkError,
  handleResponseError,
  parseApiResponse,
} from './handle'
import type {
  BackendConfig,
  BackendOptions,
  HttpClient,
  HttpInstanceOptions,
  InternalHttpRequestConfig,
} from './types'

export type { BackendOptions, HttpInstanceOptions }

const VISITOR_META = { authRole: 'visitor' } as const

function isUnauthorized(response: AxiosResponse, payload?: { code?: number | string } | null) {
  return response.status === 401 || payload?.code === 401
}

function isVisitorRequest(config: InternalHttpRequestConfig) {
  return config.meta?.authRole === VISITOR_META.authRole
}

function setExpiredMeta(config: InternalHttpRequestConfig) {
  config.meta = {
    ...config.meta,
    isExpired: true,
  }
}

function setHeader(config: InternalHttpRequestConfig, key: string, value: string) {
  config.headers = AxiosHeaders.from(config.headers)
  config.headers.set(key, value)
}

function serializeFormData(data: unknown) {
  if (data instanceof URLSearchParams) {
    return data.toString()
  }
  if (typeof data === 'string') {
    return data
  }
  const params = new URLSearchParams()
  Object.entries((data || {}) as Record<string, unknown>).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      params.append(key, String(value))
    }
  })
  return params.toString()
}

async function handleAuthExpired(response: AxiosResponse, hooks: HttpInstanceOptions['hooks']) {
  const config = response.config as InternalHttpRequestConfig
  if (isVisitorRequest(config) || config.meta?.isExpired) {
    return
  }

  if (isUnauthorized(response, parseApiResponse(response))) {
    setExpiredMeta(config)
    await hooks?.onAuthExpired?.()
  }
}

function recoverNetworkError(hooks: HttpInstanceOptions['hooks']) {
  return Promise.reject(handleNetworkError(hooks))
}

export function createAxiosInstance(options: HttpInstanceOptions, backendOptions?: BackendOptions): HttpClient {
  const backendConfig: BackendConfig = {
    ...DEFAULT_BACKEND_OPTIONS,
    ...backendOptions,
  }
  const axiosOptions = {
    ...DEFAULT_HTTP_OPTIONS,
    ...options,
  }
  const hooks = axiosOptions.hooks

  const instance = axios.create({
    baseURL: axiosOptions.baseURL,
    timeout: axiosOptions.timeout,
    validateStatus: () => true,
  })

  instance.interceptors.request.use(async (config) => {
    const httpConfig = config as InternalHttpRequestConfig
    const token = hooks?.getToken?.()
    if (token) {
      setHeader(httpConfig, 'Authorization', token)
    }

    if (httpConfig.meta?.isFormPost) {
      setHeader(httpConfig, 'Content-Type', 'application/x-www-form-urlencoded')
      httpConfig.data = serializeFormData(httpConfig.data)
    }

    if (httpConfig.meta?.isBlob) {
      httpConfig.responseType = 'blob'
    }

    await options.beforeRequest?.(httpConfig)
    return httpConfig
  })

  instance.interceptors.response.use(
    async (response) => {
      await handleAuthExpired(response, hooks)

      if (response.status === 200) {
        const config = response.config as InternalHttpRequestConfig
        if (config.meta?.isBlob) {
          return response.data
        }

        const apiData = parseApiResponse(response)
        if (!apiData) {
          return null
        }

        const result = apiData as unknown as Record<string, unknown>
        if (result[backendConfig.codeKey] === backendConfig.successCode) {
          return result[backendConfig.dataKey]
        }

        const errorResult = handleBusinessError(result, backendConfig, hooks)
        return Promise.reject(errorResult)
      }

      const errorResult = handleResponseError(response, hooks)
      return Promise.reject(errorResult)
    },
    (error: AxiosError): Promise<never> => {
      if (error.response) {
        return Promise.reject(handleResponseError(error.response, hooks))
      }
      return recoverNetworkError(hooks)
    },
  )

  return instance as HttpClient
}
