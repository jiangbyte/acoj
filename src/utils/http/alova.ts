import { message } from 'ant-design-vue'
import { createAlova } from 'alova'
import { createServerTokenAuthentication } from 'alova/client'
import adapterFetch from 'alova/fetch'
import VueHook from 'alova/vue'
import type { VueHookType } from 'alova/vue'
import { DEFAULT_ALOVA_OPTIONS, DEFAULT_BACKEND_OPTIONS } from './config'
import { handleBusinessError, handleResponseError } from './handle'
import { useAuthStore } from '@/store'

const { onAuthRequired } = createServerTokenAuthentication<VueHookType>({
  assignToken: (method) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      method.config.headers.Authorization = `${authStore.token}`
    }
  },
})

export function createAlovaInstance(
  baseURL: string,
  backendConfig?: Partial<Service.BackendConfig>,
) {
  const bc = { ...DEFAULT_BACKEND_OPTIONS, ...backendConfig }

  return createAlova({
    statesHook: VueHook,
    requestAdapter: adapterFetch(),
    baseURL,
    timeout: DEFAULT_ALOVA_OPTIONS.timeout,
    cacheFor: null,

    beforeRequest: onAuthRequired(() => {}),

    responded: {
      onSuccess: async (response, method) => {
        const { status } = response

        if (status === 200) {
          if (method.meta?.isBlob) return response.blob()

          const apiData = await response.json()
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
          authStore.logout()
        }

        return handleResponseError(response)
      },
      onError: ((error: any, method: any) => {
        const msg = error.message || '网络错误'
        console.error(`[${method.type}] ${method.url}:`, msg)
        message.error(msg)
        return { success: false, errorType: null, code: 0, message: msg, data: null }
      }) as (error: any, method: any) => void,
    },
  })
}
