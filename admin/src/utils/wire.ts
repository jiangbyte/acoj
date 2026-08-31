/** Author: Charlie */

/** HTTP JSON wire 标量类型（后端序列化为字符串）。 */
export type WireScalar = 'bool' | 'int' | 'float'

type WireFieldsResult<S extends Record<string, WireScalar>> = {
  [K in keyof S]: S[K] extends 'bool' ? boolean : number
}

/** 将 wire 布尔字符串转为 boolean；已是 boolean 时原样返回。 */
export function wireBool(value: unknown, defaultValue = false): boolean {
  if (value === null || value === undefined || value === '') {
    return defaultValue
  }
  if (typeof value === 'boolean') {
    return value
  }
  if (typeof value === 'number') {
    return value !== 0
  }
  if (typeof value === 'string') {
    const text = value.trim().toLowerCase()
    if (text === 'true' || text === '1' || text === 'yes') return true
    if (text === 'false' || text === '0' || text === 'no') return false
    return defaultValue
  }
  return defaultValue
}

/** 将 wire 整数字符串转为 number；已是 number 时原样返回。 */
export function wireInt(value: unknown, fallback?: number): number {
  if (typeof value === 'number') {
    if (!Number.isFinite(value)) {
      if (fallback !== undefined) return fallback
      throw new Error(`Invalid wire int: ${value}`)
    }
    return value
  }
  if (value === null || value === undefined || value === '') {
    if (fallback !== undefined) return fallback
    throw new Error(`Invalid wire int: ${value}`)
  }
  const n = Number(value)
  if (!Number.isFinite(n)) {
    if (fallback !== undefined) return fallback
    throw new Error(`Invalid wire int: ${value}`)
  }
  return n
}

/** 将 wire 浮点字符串转为 number；已是 number 时原样返回。 */
export function wireFloat(value: unknown, fallback?: number): number {
  if (typeof value === 'number') {
    if (!Number.isFinite(value)) {
      if (fallback !== undefined) return fallback
      throw new Error(`Invalid wire float: ${value}`)
    }
    return value
  }
  if (value === null || value === undefined || value === '') {
    if (fallback !== undefined) return fallback
    throw new Error(`Invalid wire float: ${value}`)
  }
  const n = Number(value)
  if (!Number.isFinite(n)) {
    if (fallback !== undefined) return fallback
    throw new Error(`Invalid wire float: ${value}`)
  }
  return n
}

/**
 * 显式转换单个 wire 标量；空值回退到 defaultValue。
 *
 * @example wireValue(data.sort, 'int', 0)
 */
export function wireValue<T extends boolean | number>(
  value: unknown,
  type: WireScalar,
  defaultValue: T,
): T {
  if (value === null || value === undefined || value === '') {
    return defaultValue
  }
  switch (type) {
    case 'bool':
      return wireBool(value, defaultValue as boolean) as T
    case 'int':
      return wireInt(value, defaultValue as number) as T
    case 'float':
      return wireFloat(value, defaultValue as number) as T
  }
}

/**
 * 显式批量转换 API 响应中的 wire 标量字段。
 *
 * @example wireFields(data, { sort: 'int', is_builtin: 'bool' }, defaultFormData)
 */
export function wireFields<D extends Record<string, unknown>, S extends Record<string, WireScalar>>(
  data: Record<string, unknown> | null | undefined,
  schema: S,
  defaults: D,
): WireFieldsResult<S> {
  const source = data ?? {}
  const result = {} as WireFieldsResult<S>
  for (const key of Object.keys(schema) as Array<keyof S & string>) {
    const type = schema[key]
    result[key] = wireValue(source[key], type, defaults[key] as boolean & number) as WireFieldsResult<S>[typeof key]
  }
  return result
}

/** 将 PageData meta 字符串转为数字，供 UI 分页组件使用。 */
export function readPageMeta(
  data: {
    current?: string | number
    size?: string | number
    total?: string | number
    pages?: string | number
  },
  fallback: { current?: number; size?: number } = {},
): { current: number; size: number; total: number; pages?: number } {
  const toPageInt = (value: string | number | undefined, defaultValue: number) => {
    if (value === undefined || value === '') return defaultValue
    return wireInt(value, defaultValue)
  }

  return {
    current: toPageInt(data.current, fallback.current ?? 1),
    size: toPageInt(data.size, fallback.size ?? 20),
    total: toPageInt(data.total, 0),
    pages: data.pages !== undefined && data.pages !== '' ? wireInt(data.pages, 0) : undefined,
  }
}

export function stringifyScalars(value: unknown): unknown {
  if (value === null || value === undefined) {
    return value
  }
  if (typeof value === 'boolean') {
    return value ? 'true' : 'false'
  }
  if (typeof value === 'number') {
    return Number.isFinite(value) ? String(value) : value
  }
  if (typeof value === 'string') {
    return value
  }
  if (Array.isArray(value)) {
    return value.map((item) => stringifyScalars(item))
  }
  if (typeof value === 'object') {
    if (value instanceof FormData || value instanceof Blob || value instanceof ArrayBuffer) {
      return value
    }
    const result: Record<string, unknown> = {}
    for (const [key, item] of Object.entries(value as Record<string, unknown>)) {
      result[key] = stringifyScalars(item)
    }
    return result
  }
  return value
}
