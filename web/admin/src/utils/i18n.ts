import { t } from '@/i18n'

const routeTitleKeys: Record<string, string> = {
  Root: 'routes.Root',
  AdminRoot: 'routes.AdminRoot',
  Login: 'routes.Login',
  Forbidden: 'routes.Forbidden',
  ServerError: 'routes.ServerError',
  NotFound: 'routes.NotFound',
}

export function getRouteTitleKey(code?: string | number | null) {
  if (!code) {
    return undefined
  }
  const routeCode = String(code)
  return routeTitleKeys[routeCode] || `routes.${routeCode.toUpperCase()}`
}

export function translateWithFallback(key?: string, fallback = '') {
  if (!key) {
    return fallback
  }
  const translated = t(key)
  return translated === key ? fallback || key : translated
}
