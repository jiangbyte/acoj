export function formatDateTime(value?: string | null) {
  if (!value) {
    return '-'
  }
  return String(value)
    .replace('T', ' ')
    .replace(/\.\d+Z?$/, '')
    .replace(/Z$/, '')
}

export function displayValue(value: unknown) {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  if (typeof value === 'boolean') {
    return value ? '是' : '否'
  }
  if (Array.isArray(value)) {
    return value.length ? value.join(', ') : '-'
  }
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return String(value)
}

export function compactText(value: unknown, length = 72) {
  const text = displayValue(value)
  return text.length > length ? `${text.slice(0, length)}...` : text
}

export function resolveFileUrl(value?: string | null) {
  if (!value) {
    return ''
  }
  const rawValue = String(value).trim()
  if (!rawValue) {
    return ''
  }
  if (/^(https?:|data:|blob:)/i.test(rawValue)) {
    return rawValue
  }
  const baseURL = import.meta.env.VITE_API_URL || ''
  if (!baseURL) {
    return rawValue
  }
  return `${baseURL.replace(/\/$/, '')}/${rawValue.replace(/^\//, '')}`
}
