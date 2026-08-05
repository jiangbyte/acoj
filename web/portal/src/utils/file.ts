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

export function isImageFile(
  file?:
    | string
    | null
    | {
        type?: string | null
        name?: string | null
        content_type?: string | null
        url?: string | null
      },
) {
  if (!file) return false
  if (typeof file === 'string') {
    return /\.(png|jpe?g|gif|webp|bmp|svg)(\?|$)/i.test(file)
  }
  const type = file.type || file.content_type || ''
  if (type.startsWith('image/')) return true
  return /\.(png|jpe?g|gif|webp|bmp|svg)(\?|$)/i.test(file.name || file.url || '')
}
