import { createAlova } from 'alova'
import VueHook from 'alova/vue'
import adapterFetch from 'alova/fetch'
import { createDiscreteApi } from 'naive-ui'
import { useTokenStore } from '@/stores'

// 生产镜像默认空串：走当前页面同源，由 nginx 反代 /api
const gateway = import.meta.env.VITE_GATEWAY || ''

// Implementation note.
const { message } = createDiscreteApi(['message'])

/** Implementation details. */
let unauthorizedHandling = false

/**
  * Implementation details.
 */
function isUnauthorized(status: number, jsonData?: Record<string, any>) {
  return status === 401 || String(jsonData?.code) === '401'
}

/**
  * Implementation details.
 */
async function handleUnauthorized() {
  // Implementation note.
  if (unauthorizedHandling)
    return
  unauthorizedHandling = true

  // Implementation note.
  const tokenStore = useTokenStore()
  const hadToken = !!tokenStore.getToken
  tokenStore.resetToken()
  const tip = hadToken ? 'Your session has expired. Please sign in again' : 'Please sign in first'

  try {
    // Implementation note.
    const { router } = await import('@/router')
    const current = router.currentRoute.value
    const onAuthPage = current.name === 'login' || current.name === 'register'
      || current.path === '/login' || current.path === '/register'

    // Implementation note.
    if (onAuthPage) {
      message.error(tip)
      return
    }

    // Implementation note.
    message.error(tip)
    const redirect = current.name === '404' ? undefined : current.fullPath
    await router.replace({ path: '/login', query: redirect ? { redirect } : undefined })
  }
  finally {
    window.setTimeout(() => {
      unauthorizedHandling = false
    }, 1500)
  }
}

function showErrorMessage(jsonData: Record<string, any>) {
  const text = jsonData.message || jsonData.error || 'Request failed'
  if (jsonData.code) {
    message.error(`${text} (${jsonData.code})`)
  }
  else {
    message.error(text)
  }
}

const alovaInstance = createAlova({
  statesHook: VueHook,
  requestAdapter: adapterFetch(),
  cacheFor: null,
  baseURL: gateway,
  timeout: 60 * 1000,

  beforeRequest(method) {
    // Implementation note.
    const tokenStore = useTokenStore()
    const token = tokenStore.getToken
    if (token) {
      method.config.headers.Authorization = token
    }

    // Implementation note.
    if (typeof FormData !== 'undefined' && method.data instanceof FormData) {
      delete method.config.headers['Content-Type']
      delete method.config.headers['content-type']
    }
    // Implementation note.
    else if (method.meta?.isFormPost) {
      method.config.headers['Content-Type'] = 'application/x-www-form-urlencoded'
      method.data = new URLSearchParams(method.data as URLSearchParams).toString()
    }
  },

  responded: {
    onSuccess: async (response, method) => {
      // Implementation note.
      const { status } = response
      let jsonData: Record<string, any> = {}
      try {
        jsonData = await response.clone().json()
      }
      catch {
        jsonData = {}
      }

      // Implementation note.
      if (isUnauthorized(status, jsonData)) {
        await handleUnauthorized()
        return jsonData
      }

      // Implementation note.
      if (status === 200) {
        if (method.meta?.isBlob) {
          return response.blob()
        }
        if (jsonData.code === '200' || jsonData.success === true) {
          return jsonData
        }
        showErrorMessage(jsonData)
        return jsonData
      }

      // Implementation note.
      showErrorMessage(jsonData)
      return jsonData
    },

    onError: async (error, _method) => {
      if (!unauthorizedHandling) {
        message.error(error?.message || 'Network error. Please try again later')
      }
    },

    onComplete: async (_method) => {},
  },
})

export const request = alovaInstance
