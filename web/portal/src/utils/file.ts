export function resolveFileUrl(value?: string | null) {
  if (!value) {
    return undefined
  }
  const rawValue = String(value).trim()
  if (!rawValue) {
    return undefined
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
