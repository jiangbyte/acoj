import { createAxiosInstance } from '@hei/shared'
import type { HttpClient, HttpInstanceOptions, HttpRequestConfig, RequestErrorResult } from '@hei/shared'
import { message } from 'ant-design-vue'

import { t } from '@/i18n'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { useRouteStore } from '@/stores/route'
import { useUserStore } from '@/stores/user'

type RequestMethod = 'get' | 'post' | 'formdata' | (string & {})
type RequestValue = object

function handleAuthExpired() {
  const auth = useAuthStore()
  if (!auth.token) {
    return
  }

  auth.clearSession()
  useUserStore().clear()
  useRouteStore().reset_route_store()
  useAppStore().clearAllTabs()
  message.warning(t('http.unauthorizedRelogin'))
}

function showError(error: RequestErrorResult) {
  message.error(error.message)
}

function showNetworkError(error: RequestErrorResult) {
  message.warning(error.message)
}

function compactParams(value: RequestValue) {
  return Object.fromEntries(
    Object.entries(value).filter(([, item]) => item !== undefined && item !== null && item !== ''),
  )
}

function createAdminRequest(options: HttpInstanceOptions) {
  return createAxiosInstance({
    ...options,
    hooks: {
      ...options.hooks,
      getToken: () => useAuthStore().token,
      onAuthExpired: handleAuthExpired,
      onError: showError,
      onNetworkError: showNetworkError,
    },
  })
}

function createBaseRequest(client: HttpClient) {
  return function baseRequest<T = unknown>(
    url: string,
    value: RequestValue = {},
    method: RequestMethod = 'post',
    options: HttpRequestConfig = {},
  ): Promise<T> {
    if (method === 'get') {
      return client.get<T>(url, {
        ...options,
        params: compactParams(value),
      })
    }

    if (method === 'post') {
      return client.post<T>(url, value, options)
    }

    if (method === 'formdata') {
      return client.post<T>(url, value, {
        ...options,
        meta: {
          ...options.meta,
          isFormPost: true,
        },
      })
    }

    return client.request<T>({
      ...options,
      url,
      method,
      data: value,
    })
  }
}

export const request = createAdminRequest({
  baseURL: import.meta.env.VITE_API_BASE_URL,
})

export const blankRequest = createAdminRequest({
  baseURL: '',
})

export const baseRequest = createBaseRequest(request)
