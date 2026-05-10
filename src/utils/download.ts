export function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

export function getFilenameFromHeaders(headers: Headers, fallback: string): string {
  const disposition = headers.get('content-disposition')
  if (disposition) {
    const match = disposition.match(/filename\*?=(?:UTF-8'')?([^;\s]+)/i)
    if (match) return decodeURIComponent(match[1])
  }
  return fallback
}
