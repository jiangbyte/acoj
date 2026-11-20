// import { SERVICE_BASE_URL } from '@/config'
import { createServerTokenAuthentication } from 'alova/client'
import { createAlova } from 'alova'
import VueHook from 'alova/vue'
import adapterFetch from 'alova/fetch'
import { createDiscreteApi } from 'naive-ui'
import { useTokenStore } from '@/stores'
import type { VueHookType } from 'alova/vue'

const gateway = import.meta.env.VITE_GATEWAY

// message 弹窗
const { message } = createDiscreteApi(['message'])

/**
 * token 认证拦截器
 */
const { onAuthRequired, onResponseRefreshToken } = createServerTokenAuthentication<VueHookType>({
  // 服务端判定Token是否过期
  refreshTokenOnSuccess: {
    // 401 未认证过期
    isExpired: (response, method) => {
      const isExpired = method.meta && method.meta.isExpired
      return response.status === 401 && !isExpired
    },

    handler: async (_response, method) => {
      if (!method.meta) {
        method.meta = {
          isExpired: true,
        }
      }
      else {
        method.meta.isExpired = true
      }

      message.error('登录过期，请重新登录')

      // 不刷新token
      const tokenStore = useTokenStore()
      tokenStore.resetToken()

      // 不刷新token
    },
  },

  // 附加Token
  assignToken: (method) => {
    const tokenStore = useTokenStore()
    method.config.headers.Authorization = tokenStore.getToken
  },
})

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

  beforeRequest: onAuthRequired((method) => {
    if (method.meta?.isFormPost) {
      method.config.headers['Content-Type'] = 'application/x-www-form-urlencoded'
      method.data = new URLSearchParams(method.data as URLSearchParams).toString()
    }
  }),

  responded: onResponseRefreshToken({
    onSuccess: async (response, method) => {
      const { status } = response

      // 获取json数据
      const jsonData = await response.clone().json()

      if (status === 200) {
        if (method.meta?.isBlob) {
          return response.blob()
        }

        // 如果code 是 401 马上执行本地登出
        if (jsonData.code === '401') {
          message.error(jsonData.message || '登录过期，请重新登录')
          // 登出
          const tokenStore = useTokenStore()
          tokenStore.resetToken()
          return jsonData
        }

        // 获取json数据中的code字段，判断是否为成功状态，默认为"200"
        if (jsonData.code === '200' || jsonData.success === true) {
          return jsonData
        }

        // 业务请求失败：展示后端 message
        showErrorMessage(jsonData)
        return jsonData
      }

      // 接口请求失败
      showErrorMessage(jsonData)
      return jsonData
    },

    onError: async (error, _method) => {
      message.error(error?.message || '网络异常，请稍后重试')
    },

    onComplete: async (_method) => {},
  }),
})

export const request = alovaInstance
