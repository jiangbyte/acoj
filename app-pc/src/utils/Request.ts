import { createAlova } from 'alova'
import VueHook from 'alova/vue'
import adapterFetch from 'alova/fetch'
import { createDiscreteApi } from 'naive-ui'
import { useTokenStore } from '@/stores'

const gateway = import.meta.env.VITE_GATEWAY

// message 弹窗
const { message } = createDiscreteApi(['message'])

/** 未登录/过期处理锁，避免并发 401 连弹 toast、重复跳转 */
let unauthorizedHandling = false

/**
 * 判断业务/HTTP 是否未登录或无权限（后端统一 code=401）
 */
function isUnauthorized(status: number, jsonData?: Record<string, any>) {
  return status === 401 || String(jsonData?.code) === '401'
}

/**
 * 清理本地登录态，提示一次并跳转登录页（登录/注册页除外）
 */
async function handleUnauthorized() {
  // 1. 已在处理中则直接忽略，防并发请求刷屏
  if (unauthorizedHandling)
    return
  unauthorizedHandling = true

  // 2. 清理 token，并区分「未登录」与「登录过期」文案
  const tokenStore = useTokenStore()
  const hadToken = !!tokenStore.getToken
  tokenStore.resetToken()
  const tip = hadToken ? '登录过期，请重新登录' : '请先登录'

  try {
    // 3. 懒加载 router，避免与 stores/utils 循环依赖
    const { router } = await import('@/router')
    const current = router.currentRoute.value
    const onAuthPage = current.name === 'login' || current.name === 'register'
      || current.path === '/login' || current.path === '/register'

    // 4. 已在登录相关页：只提示，不再跳转
    if (onAuthPage) {
      message.error(tip)
      return
    }

    // 5. 提示一次并跳转登录，带回跳地址
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
  const text = jsonData.message || jsonData.error || '请求失败'
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
    // 1. 仅在有 token 时附加 Authorization
    const tokenStore = useTokenStore()
    const token = tokenStore.getToken
    if (token) {
      method.config.headers.Authorization = token
    }

    // 2. FormData 上传：必须去掉 Content-Type，由浏览器自动带 multipart boundary
    if (typeof FormData !== 'undefined' && method.data instanceof FormData) {
      delete method.config.headers['Content-Type']
      delete method.config.headers['content-type']
    }
    // 3. 表单提交内容类型转换
    else if (method.meta?.isFormPost) {
      method.config.headers['Content-Type'] = 'application/x-www-form-urlencoded'
      method.data = new URLSearchParams(method.data as URLSearchParams).toString()
    }
  },

  responded: {
    onSuccess: async (response, method) => {
      // 1. 解析响应体
      const { status } = response
      let jsonData: Record<string, any> = {}
      try {
        jsonData = await response.clone().json()
      }
      catch {
        jsonData = {}
      }

      // 2. 未登录/过期：统一登出并跳登录
      if (isUnauthorized(status, jsonData)) {
        await handleUnauthorized()
        return jsonData
      }

      // 3. 成功响应
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

      // 4. 其他 HTTP 失败
      showErrorMessage(jsonData)
      return jsonData
    },

    onError: async (error, _method) => {
      if (!unauthorizedHandling) {
        message.error(error?.message || '网络异常，请稍后重试')
      }
    },

    onComplete: async (_method) => {},
  },
})

export const request = alovaInstance
