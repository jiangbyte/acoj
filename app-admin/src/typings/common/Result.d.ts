// Common response interface
interface IResult<T> {
  code: string
  data?: T
  message: string
  success: boolean
}

// Paginated list response
interface IListResult<T> {
  current: number
  pages: number
  records: T[]
  size: number
  total: number
}

// Paginated response
interface IPageResult<T> {
  code: string
  data?: IListResult<T>
  message: string
  success: boolean
}

// Option type definition
interface IOption<T> {
  value: T
  label: string
  children?: IOption<T>[]
}

// Option response
interface IOptionResult<T> {
  code: string
  data?: IOption<T>[]
  message: string
  success: boolean
}
