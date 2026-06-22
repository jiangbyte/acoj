import type { Component } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import { RouterView } from 'vue-router'

import {
  buildRouteResourceTree,
  isPageResource,
  isRouteResource,
  normalizeComponentPath,
  normalizePath,
  type RouteResourceTreeNode,
} from '@/router/utils'
import type { RouteBuildResult, RouteMenuItem, RouteResource } from '@/types/route'
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

function createRoute(node: RouteResourceTreeNode, parentPath = ''): RouteRecordRaw | null {
  if (!isRouteResource(node)) {
    return null
  }

  const normalizedPath = normalizePath(node.path)
  const path = parentPath && normalizedPath.startsWith(`${parentPath}/`)
    ? normalizedPath.slice(parentPath.length + 1)
    : normalizedPath
  if (!path) {
    console.warn(`[route] resource ${node.code} has no path`)
    return null
  }

  const children = node.children.map((child) => createRoute(child, normalizedPath)).filter((item): item is RouteRecordRaw => Boolean(item))
  const component = loadViewComponent(node.component)

  if (isPageResource(node) && !node.href && !component) {
    console.warn(`[route] component not found for ${node.code}: ${node.component}`)
    return null
  }

  const routeComponent = component && !node.href ? component : RouterView
  const redirect = children.length ? node.redirect || children[0]?.path : undefined

  if (node.redirect && !children.length) {
    console.warn(`[route] resource ${node.code} has redirect but no children`)
  }

  const routeMeta = {
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
    activeMenu: node.href ? undefined : normalizedPath,
  }

  if (children.length) {
    return {
      path,
      name: node.code,
      component: routeComponent,
      redirect,
      children,
      meta: routeMeta,
    }
  }

  return {
    path,
    name: node.code,
    component: routeComponent,
    meta: routeMeta,
  }
}

function createMenu(node: RouteResourceTreeNode): RouteMenuItem | null {
  if (!isRouteResource(node) || !node.is_visible) {
    return null
  }

  const children = node.children.map(createMenu).filter((item): item is RouteMenuItem => Boolean(item))
  const path = normalizePath(node.path)
  const key = node.href || node.redirect || path || node.code

  return {
    key,
    label: node.name,
    labelKey: getRouteTitleKey(node.code),
    icon: node.icon,
    href: node.href,
    children: children.length ? children : undefined,
  }
}

export function buildRoutes(resources: RouteResource[]): RouteBuildResult {
  const enabledResources = resources.filter((item) => item.status === 'ENABLED')
  const tree = buildRouteResourceTree(enabledResources)
  const childRoutes = tree.map((node) => createRoute(node)).filter((item): item is RouteRecordRaw => Boolean(item))
  const menus = tree.map(createMenu).filter((item): item is RouteMenuItem => Boolean(item))

  return {
    routes: childRoutes,
    menus,
    button_permissions: enabledResources
      .filter((item) => item.resource_type === 'BUTTON')
      .map((item) => item.code)
      .sort(),
    cache_routes: enabledResources.filter((item) => item.is_cache).map((item) => item.code),
  }
}
