// Implementation note.
interface IResult<T> {
  code: string
  data?: T
  message: string
  success: boolean
}

// Implementation note.
interface IListResult<T> {
  current: number
  pages: number
  records: T[]
  size: number
  total: number
}

// Implementation note.
interface IPageResult<T> {
  code: string
  data?: IListResult<T>
  message: string
  success: boolean
}

// Implementation note.
interface IOption<T> {
  value: T
  label: string
  children?: IOption<T>[]
}

// Implementation note.
interface IOptionResult<T> {
  code: string
  data?: IOption<T>[]
  message: string
  success: boolean
}
