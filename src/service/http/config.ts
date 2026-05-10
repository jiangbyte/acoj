export const DEFAULT_ALOVA_OPTIONS = {
  timeout: 15 * 1000,
}

export const DEFAULT_BACKEND_OPTIONS: Service.BackendConfig = {
  codeKey: 'code',
  dataKey: 'data',
  msgKey: 'message',
  successCode: 200,
}

export const ERROR_STATUS: Record<string, string> = {
  default: '请求失败',
  400: '请求参数错误',
  401: '登录已过期，请重新登录',
  403: '无权限访问',
  404: '请求资源不存在',
  405: '请求方法不允许',
  408: '请求超时',
  500: '服务器内部错误',
  501: '服务未实现',
  502: '网关错误',
  503: '服务不可用',
  504: '网关超时',
}

export const ERROR_NO_TIP_STATUS = [10000]
