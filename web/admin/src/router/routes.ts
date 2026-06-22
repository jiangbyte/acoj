import type { Component } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import { RouterView } from 'vue-router'

import {
  isPageResource,
  isRouteResource,
  normalizeComponentPath,
  normalizePath,
  sortRouteResources,
} from '@/router/utils'
import type { ResourceTreeNode, RouteBuildResult, RouteMenuItem } from '@/types/route'
import { getRouteTitleKey } from '@/utils/i18n'

const viewModules = import.meta.glob<Component>('@/views/**/*.vue')

function loadViewComponent(component?: string | null) {
  const componentPath = normalizeComponentPath(component)
  if (!componentPath) {
    return undefined
  }

  const directoryPath = componentPath.endsWith('.vue') ? componentPath.slice(0, -4) : componentPath
  return viewModules[`/src/views${componentPath}`] || viewModules[`/src/views${directoryPath}/index.vue`]
}

function getRoutePath(node: ResourceTreeNode) {
  return normalizePath(node.path) || node.code
}

function getRouteMeta(node: ResourceTreeNode, path: string) {
  return {
    title: node.name,
    titleKey: getRouteTitleKey(node.code),
    description: node.description || undefined,
    icon: node.icon || undefined,
    requiresAuth: true,
    hideInMenu: !node.is_visible,
    keepAlive: node.is_cache,
    affixTab: node.is_affix,
    withoutTab: Boolean(node.href),
    resourceId: node.id,
    resourceCode: node.code,
    href: node.href || undefined,
    sort: node.sort,
    extra: node.extra,
    activeMenu: node.href ? undefined : path,
  }
}

function filterAsyncRouter(nodes: ResourceTreeNode[]): RouteRecordRaw[] {
  return sortRouteResources(nodes)
    .map((node): RouteRecordRaw | null => {
      if (!isRouteResource(node)) {
        return null
      }

      const path = getRoutePath(node)
      const children = filterAsyncRouter(node.children || [])
      const component = loadViewComponent(node.component)

      if (isPageResource(node) && !node.href && !component) {
        console.warn(`[route] component not found for ${node.code}: ${node.component}`)
        return null
      }

      const route = {
        path,
        name: node.code,
        component: component && !node.href ? component : RouterView,
        redirect: children.length ? node.redirect || children[0]?.path : node.redirect || undefined,
        ...(children.length ? { children } : {}),
        meta: getRouteMeta(node, path),
      }
      return route as RouteRecordRaw
    })
    .filter((item): item is RouteRecordRaw => Boolean(item))
}

function toMenu(route: RouteRecordRaw): RouteMenuItem | null {
  if (route.meta?.hideInMenu) {
    return null
  }

  const children = (route.children || []).map(toMenu).filter((item): item is RouteMenuItem => Boolean(item))
  const href = route.meta?.href ? String(route.meta.href) : undefined
  const key = href || String(route.path || route.name || '')

  return {
    key,
    label: String(route.meta?.title || route.name || route.path),
    labelKey: route.meta?.titleKey ? String(route.meta.titleKey) : undefined,
    icon: route.meta?.icon ? String(route.meta.icon) : undefined,
    href,
    children: children.length ? children : undefined,
  }
}

function flatAsyncRoutes(routes: RouteRecordRaw[], breadcrumb: RouteMenuItem[] = []) {
  const result: RouteRecordRaw[] = []

  routes.forEach((route) => {
    const routeMenu = toMenu(route)
    const nextBreadcrumb = routeMenu ? [...breadcrumb, { ...routeMenu, children: undefined }] : breadcrumb
    const children = route.children || []
    const flatRoute = {
      ...route,
      meta: {
        ...route.meta,
        breadcrumb: nextBreadcrumb,
      },
    }

    delete flatRoute.children
    result.push(flatRoute)

    if (children.length) {
      result.push(...flatAsyncRoutes(children, nextBreadcrumb))
    }
  })

  return result
}

function collectButtonPermissions(nodes: ResourceTreeNode[], result = new Set<string>()) {
  nodes.forEach((node) => {
    if (node.status !== 'ENABLED') {
      return
    }
    if (node.resource_type === 'BUTTON') {
      result.add(node.code)
    }
    collectButtonPermissions(node.children || [], result)
  })
  return [...result].sort()
}

function collectCacheRoutes(nodes: ResourceTreeNode[], result = new Set<string>()) {
  nodes.forEach((node) => {
    if (node.status !== 'ENABLED') {
      return
    }
    if (node.is_cache) {
      result.add(node.code)
    }
    collectCacheRoutes(node.children || [], result)
  })
  return [...result]
}

export function buildLoginRoutes(menu: ResourceTreeNode[]): RouteBuildResult {
  const routeTree = filterAsyncRouter(menu)
  const menus = routeTree.map(toMenu).filter((item): item is RouteMenuItem => Boolean(item))

  return {
    routes: flatAsyncRoutes(routeTree),
    menus,
    button_permissions: collectButtonPermissions(menu),
    cache_routes: collectCacheRoutes(menu),
  }
}
