/**
 * Resolve the post-login safe redirect path (only relative internal paths are allowed)。
 */
export function resolveAuthRedirect(raw: unknown, fallback = '/'): string {
  if (typeof raw !== 'string' || !raw) {
    return fallback
  }
  const path = raw.trim()
  if (!path.startsWith('/') || path.startsWith('//') || path.includes('://')) {
    return fallback
  }
  if (path === '/login' || path.startsWith('/login?')) {
    return fallback
  }
  return path
}
