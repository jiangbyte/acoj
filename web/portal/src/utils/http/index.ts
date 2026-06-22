import { createAxiosInstance } from '@hei/shared'
import type { HttpClient, HttpRequestConfig } from '@hei/shared'

type RequestMethod = 'get' | 'post' | 'formdata' | (string & {})
type RequestValue = object

export const request = createAxiosInstance({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  hooks: {
    getToken: () => {
      const raw = localStorage.getItem('hei-portal-auth')
      if (!raw) {
        return ''
      }
      try {
        const data = JSON.parse(raw) as { token?: string }
        return data.token || ''
      } catch {
        return ''
      }
    },
    onAuthExpired: () => {
      localStorage.removeItem('hei-portal-auth')
      window.dispatchEvent(new CustomEvent('portal-auth-expired'))
    },
  },
})

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
        params: value,
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

export const baseRequest = createBaseRequest(request)
