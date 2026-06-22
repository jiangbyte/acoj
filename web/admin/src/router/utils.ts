import type { RouteResource } from '@/types/route'

export function normalizePath(path?: string | null) {
  if (!path) {
    return ''
  }
  return path.startsWith('/') ? path : `/${path}`
}

export function normalizeComponentPath(component?: string | null) {
  if (!component) {
    return ''
  }

  const withSlash = component.startsWith('/') ? component : `/${component}`
  return withSlash.endsWith('.vue') ? withSlash : `${withSlash}.vue`
}

export function isRouteResource(resource: RouteResource) {
  return resource.resource_type === 'CATALOG' || resource.resource_type === 'MENU' || resource.resource_type === 'PAGE'
}

export function isPageResource(resource: RouteResource) {
  return resource.resource_type === 'MENU' || resource.resource_type === 'PAGE'
}

export function sortRouteResources<T extends Pick<RouteResource, 'sort' | 'id'>>(items: T[]) {
  return [...items].sort((a, b) => a.sort - b.sort || a.id.localeCompare(b.id))
}
