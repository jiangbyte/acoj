/** 请求的相关类型 */
declare namespace Service {
  /** 后端接口返回的数据结构配置 */
  interface BackendConfig {
    /** 业务状态码字段名 */
    codeKey: string
    /** 数据字段名 */
    dataKey: string
    /** 消息字段名 */
    msgKey: string
    /** 业务成功状态码 */
    successCode: number
    /** 业务成功标识字段名（可选） */
    successKey?: string
  }

  /** 统一响应结果（拦截器标准化后的格式，包含后端原始字段） */
  interface ResponseResult<T = any> {
    /** 请求是否成功 */
    success: boolean
    /** 错误类型（成功时为 null） */
    errorType: 'Response Error' | 'Business Error' | null
    /** 业务状态码 */
    code: number | string
    /** 提示信息 */
    message: string
    /** 响应数据 */
    data: T
    /** 后端原始字段（可索引） */
    [key: string]: any
  }

  /** 分页响应（后端 data 字段内的结构） */
  interface PageResult<T = any> {
    records: T[]
    total: number
    page: number
    size: number
    pages: number
  }
}
