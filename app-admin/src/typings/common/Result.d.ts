// 通用返回接口
interface IResult<T> {
  code: string
  data?: T
  message: string
  success: boolean
}

// 分页列表返回结果
interface IListResult<T> {
  current: number
  pages: number
  records: T[]
  size: number
  total: number
}

// 分页返回结果
interface IPageResult<T> {
  code: string
  data?: IListResult<T>
  message: string
  success: boolean
}

// 选项类型定义
interface IOption<T> {
  value: T
  label: string
  children?: IOption<T>[]
}

// 选项结果返回
interface IOptionResult<T> {
  code: string
  data?: IOption<T>[]
  message: string
  success: boolean
}
