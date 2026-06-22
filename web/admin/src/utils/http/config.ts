import { t } from '@/i18n'

export const DEFAULT_HTTP_OPTIONS = {
  timeout: 15 * 1000,
} as const

export const DEFAULT_BACKEND_OPTIONS = {
  codeKey: 'code',
  dataKey: 'data',
  msgKey: 'message',
  successCode: 200,
} as const

export const ERROR_STATUS: Record<number | 'default', string> = {
  default: '请求失败，请稍后重试',
  400: '请求参数错误',
  401: '登录状态已失效',
  403: '没有权限访问该资源',
  404: '请求资源不存在',
  405: '请求方法不允许',
  408: '请求超时',
  500: '服务器异常',
  502: '网关异常',
  503: '服务暂不可用',
  504: '网关超时',
}

export const ERROR_NO_TIP_STATUS: Array<number | string> = []

export function getErrorStatusMessage(status?: number | string) {
  const key = status && status in ERROR_STATUS ? status : 'default'
  return t(`http.${key}`)
}
