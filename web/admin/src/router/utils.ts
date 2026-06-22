import type { RouteResource } from '@/types/route'

export interface RouteResourceTreeNode extends RouteResource {
  children: RouteResourceTreeNode[]
}

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

export function buildRouteResourceTree(resources: RouteResource[]) {
  const nodeMap = new Map<string, RouteResourceTreeNode>()
  const roots: RouteResourceTreeNode[] = []

  sortRouteResources(resources).forEach((resource) => {
    nodeMap.set(resource.id, { ...resource, children: [] })
  })

  nodeMap.forEach((node) => {
    if (node.parent_id && nodeMap.has(node.parent_id)) {
      nodeMap.get(node.parent_id)?.children.push(node)
      return
    }
    roots.push(node)
  })

  nodeMap.forEach((node) => {
    node.children = sortRouteResources(node.children)
  })

  return sortRouteResources(roots)
}
