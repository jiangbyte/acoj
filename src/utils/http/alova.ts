import { message } from 'ant-design-vue'
import { createAlova } from 'alova'
import { createServerTokenAuthentication } from 'alova/client'
import adapterFetch from 'alova/fetch'
import { xhrRequestAdapter } from '@alova/adapter-xhr'
import VueHook from 'alova/vue'
import type { VueHookType } from 'alova/vue'
import { DEFAULT_ALOVA_OPTIONS, DEFAULT_BACKEND_OPTIONS } from './config'
import { handleBusinessError, handleResponseError } from './handle'
import { useAuthStore } from '@/store'

const { onAuthRequired } = createServerTokenAuthentication<VueHookType>({
  assignToken: method => {
    const authStore = useAuthStore()
    if (authStore.token) {
      method.config.headers.Authorization = `${authStore.token}`
    }
  },
})

function createAlovaInstance(
  baseURL: string,
  adapter: any,
  backendConfig?: Partial<Service.BackendConfig>
) {
  const bc = { ...DEFAULT_BACKEND_OPTIONS, ...backendConfig }

  return createAlova({
    statesHook: VueHook,
    requestAdapter: adapter,
    baseURL,
    timeout: DEFAULT_ALOVA_OPTIONS.timeout,
    cacheFor: null,

    beforeRequest: onAuthRequired(() => {}),

    responded: {
      onSuccess: async (response, method) => {
        const { status } = response

        if (status === 200) {
          // Fetch adapter returns Response object, XHR adapter returns { status, data, headers }
          if (method.meta?.isBlob) {
            return typeof response.blob === 'function' ? response.blob() : (response as any).data
          }

          const apiData = typeof response.json === 'function' ? await response.json() : (response as any).data
          if (apiData[bc.codeKey] === bc.successCode) {
            return {
              ...apiData,
              success: true,
              data: apiData[bc.dataKey],
            }
          }

          return handleBusinessError(apiData, bc)
        }

        if (status === 401) {
          const authStore = useAuthStore()
          const wasLogin = authStore.isLogin
          authStore.logout()
          if (wasLogin) {
            import('@/router').then(({ router }) => {
              const name = router.currentRoute.value.name
              if (name && name !== 'login') {
                router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
              }
            })
          }
        }

        return handleResponseError(response)
      },
      onError: (error: any, method: any) => {
        const msg = error.message || '网络错误'
        console.error(`[${method.type}] ${method.url}:`, msg)
        message.error(msg)
      },
    },
  })
}

export function createDefaultAlovaInstance(
  baseURL: string,
  backendConfig?: Partial<Service.BackendConfig>
) {
  return createAlovaInstance(baseURL, adapterFetch(), backendConfig)
}

export function createUploadAlovaInstance(
  baseURL: string,
  backendConfig?: Partial<Service.BackendConfig>
) {
  return createAlovaInstance(baseURL, xhrRequestAdapter(), backendConfig)
}
