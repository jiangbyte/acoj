export function getDomain(url: string): string {
  try {
    return new URL(url).hostname.replace('www.', '')
  } catch {
    return url
  }
}

export function getFaviconUrl(url: string): string {
  try {
    const { origin } = new URL(url)
    return `${origin}/favicon.ico`
  } catch {
    return ''
  }
}

export function getFileName(url: string): string {
  const segments = url.replace(/\/+$/, '').split('/')
  return decodeURIComponent(segments[segments.length - 1] || '')
}
