import type { ProxyOptions } from 'vite'

/**
 * 解析 Vite Proxy 配置
 *
 * 环境变量：
 *   VITE_PROXY_TARGET   — 代理目标地址，默认 http://localhost:8081
 *   VITE_PROXY_PREFIXES — 需要代理的路径前缀，逗号分隔，默认 /api
 *
 * 示例：
 *   VITE_PROXY_TARGET=http://localhost:8081
 *   VITE_PROXY_PREFIXES=/api,/uploads,/ws
 */
export function resolveProxy(env: Record<string, string>): Record<string, ProxyOptions> {
  const target = env.VITE_PROXY_TARGET || 'http://localhost:8081'
  const prefixes = (env.VITE_PROXY_PREFIXES || '/api').split(',').map(s => s.trim()).filter(Boolean)

  return Object.fromEntries(
    prefixes.map(prefix => [
      prefix,
      {
        target,
        changeOrigin: true,
        secure: false,
        ...(prefix === '/ws' ? { ws: true } : {}),
      },
    ])
  )
}
