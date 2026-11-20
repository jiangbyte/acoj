/**
 * 解析登录/注册后的安全跳转路径（仅允许站内相对路径）。
 */
export function resolveAuthRedirect(raw: unknown, fallback = '/'): string {
  if (typeof raw !== 'string' || !raw) {
    return fallback
  }
  const path = raw.trim()
  if (!path.startsWith('/') || path.startsWith('//') || path.includes('://')) {
    return fallback
  }
  if (
    path === '/login'
    || path.startsWith('/login?')
    || path === '/register'
    || path.startsWith('/register?')
  ) {
    return fallback
  }
  return path
}
