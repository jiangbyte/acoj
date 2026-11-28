import { createAlova } from 'alova'
import VueHook from 'alova/vue'
import adapterFetch from 'alova/fetch'
import { createDiscreteApi } from 'naive-ui'
import { useTokenStore } from '@/stores'

// 生产镜像默认空串：走当前页面同源，由 nginx 反代 /api
const gateway = import.meta.env.VITE_GATEWAY || ''

// Message toast
const { message } = createDiscreteApi(['message'])

/** Unauthorized-session lock that prevents duplicate toasts and redirects from concurrent 401 responses */
let unauthorizedHandling = false

/**
 * Check whether the business or HTTP response is unauthorized (the backend uses code 401)
 */
function isUnauthorized(status: number, jsonData?: Record<string, any>) {
  return status === 401 || String(jsonData?.code) === '401'
}

/**
 * Clear the local session, show one message, and redirect to login unless already there
 */
async function handleUnauthorized() {
  // 1. Ignore while handling another request to prevent duplicate messages
  if (unauthorizedHandling)
    return
  unauthorizedHandling = true

  // 2. Clear the token and distinguish signed-out from expired-session messages
  const tokenStore = useTokenStore()
  const hadToken = !!tokenStore.getToken
  tokenStore.resetToken()
  const tip = hadToken ? 'Session expired. Please sign in again' : 'Please sign in first'

  // 3. Show one message
  message.error(tip)

  // 4. Lazy-load the router to avoid circular store and utility dependencies; redirect unless on login
  try {
    const { router } = await import('@/router')
    const current = router.currentRoute.value
    if (current.name !== 'login' && current.path !== '/login') {
      const redirect = current.name === '404' ? undefined : current.fullPath
      await router.replace({ path: '/login', query: redirect ? { redirect } : undefined })
    }
  }
  finally {
    // Keep the lock briefly after redirecting to absorb in-flight 401 responses
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
    // 1. Add Authorization only when a token exists to avoid invalid authentication
    const tokenStore = useTokenStore()
    const token = tokenStore.getToken
    if (token) {
      method.config.headers.Authorization = token
    }

    // 2. For FormData uploads, remove Content-Type so the browser adds the multipart boundary
    if (typeof FormData !== 'undefined' && method.data instanceof FormData) {
      delete method.config.headers['Content-Type']
      delete method.config.headers['content-type']
    }
    // 3. Convert the form submission content type
    else if (method.meta?.isFormPost) {
      method.config.headers['Content-Type'] = 'application/x-www-form-urlencoded'
      method.data = new URLSearchParams(method.data as URLSearchParams).toString()
    }
  },

  responded: {
    onSuccess: async (response, method) => {
      // 1. Pass SSE responses through without JSON parsing
      const url = String(method.url || '')
      const sseApiPattern = /\/api\/.*?sse/
      const isMatch = sseApiPattern.test(url)
      const hasApi = url.includes('/api/')
      const hasSse = url.includes('/sse')
      if (isMatch || (hasApi && hasSse)) {
        return response
      }

      // 2. Parse the response body
      const { status } = response
      let jsonData: Record<string, any> = {}
      try {
        jsonData = await response.clone().json()
      }
      catch {
        jsonData = {}
      }

      // 3. For an unauthorized or expired session, sign out and redirect without another toast
      if (isUnauthorized(status, jsonData)) {
        await handleUnauthorized()
        return jsonData
      }

      // 4. Successful response
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

      // 5. Other HTTP failures
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
