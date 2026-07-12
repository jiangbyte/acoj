export { formatDateTime, normalizeBackendTime, normalizeDateTime } from './time'

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
