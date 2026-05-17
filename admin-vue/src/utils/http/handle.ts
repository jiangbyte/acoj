import { message } from 'ant-design-vue'
import { ERROR_STATUS, ERROR_NO_TIP_STATUS } from './config'

/**
 * 处理HTTP请求错误（非200状态码）
 */
export function handleResponseError(response: Response): Service.ResponseResult {
  const msg = ERROR_STATUS[response.status] || ERROR_STATUS.default
  message.error(msg)
  return {
    success: false,
    errorType: 'Response Error',
    code: response.status,
    message: msg,
    data: null,
  }
}

/**
 * 处理业务错误（后端返回的业务code不匹配successCode）
 */
export function handleBusinessError(
  data: Record<string, any>,
  bc: Service.BackendConfig = {
    codeKey: 'code',
    dataKey: 'data',
    msgKey: 'message',
    successCode: 200,
  }
): Service.ResponseResult {
  const msg = data[bc.msgKey] || '业务异常'
  if (!ERROR_NO_TIP_STATUS.includes(Number(data[bc.codeKey]))) {
    message.error(msg)
  }
  return {
    ...data,
    success: false,
    errorType: 'Business Error',
    code: data[bc.codeKey],
    message: msg,
    data: data[bc.dataKey],
  }
}
