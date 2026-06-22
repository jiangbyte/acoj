import type { Component } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import { RouterView } from 'vue-router'

import BasicLayout from '@/layouts/BasicLayout.vue'
import { DEFAULT_HOME_PATH } from '@/config/app'
import { INNER_ROUTE_NAMES } from '@/router/inner-routes'
import {
  buildRouteResourceTree,
  isPageResource,
  isRouteResource,
  normalizeComponentPath,
  normalizePath,
  type RouteResourceTreeNode,
} from '@/router/utils'
import type { RouteBuildResult, RouteMenuItem, RouteResource } from '@/types/route'

const viewModules = import.meta.glob<Component>('@/views/**/*.vue')

function firstVisibleRoutePath(route: RouteRecordRaw): string | undefined {
  if (!route.meta?.hideInMenu && route.path) {
    return route.path
  }

  for (const child of route.children ?? []) {
    const childPath = firstVisibleRoutePath(child)
    if (childPath) {
      return childPath
    }
  }

  return undefined
}

function createRoute(node: RouteResourceTreeNode): RouteRecordRaw | null {
  if (!isRouteResource(node)) {
    return null
  }

  const path = normalizePath(node.path)
  if (!path) {
    console.warn(`[route] resource ${node.code} has no path`)
    return null
  }

  const children = node.children.map(createRoute).filter((item): item is RouteRecordRaw => Boolean(item))
  const componentPath = normalizeComponentPath(node.component)
  const component = componentPath ? viewModules[`/src/views${componentPath}`] : undefined

  if (isPageResource(node) && !node.href && !component) {
    console.warn(`[route] component not found for ${node.code}: ${componentPath}`)
    return null
  }

  const routeComponent = component && !node.href ? component : RouterView
  const redirect = children.length ? node.redirect || firstVisibleRoutePath(children[0]) : undefined

  if (node.redirect && !children.length) {
    console.warn(`[route] resource ${node.code} has redirect but no children`)
  }

  const routeMeta = {
    title: node.name,
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
    icon: node.icon,
    href: node.href,
    children: children.length ? children : undefined,
  }
}

export function buildRoutes(resources: RouteResource[]): RouteBuildResult {
  const enabledResources = resources.filter((item) => item.status === 'ENABLED')
  // 后端资源是扁平权限数据，前端在这里一次性转换为路由树、菜单树和按钮权限。
  const tree = buildRouteResourceTree(enabledResources)
  const childRoutes = tree.map(createRoute).filter((item): item is RouteRecordRaw => Boolean(item))
  const menus = tree.map(createMenu).filter((item): item is RouteMenuItem => Boolean(item))

  return {
    routes: [
      {
        path: '/_admin',
        name: INNER_ROUTE_NAMES.AdminRoot,
        component: BasicLayout,
        redirect: DEFAULT_HOME_PATH,
        meta: { title: '管理端', requiresAuth: true, withoutTab: true, icon: "HomeOutlined" },
        children: childRoutes,
      },
    ],
    menus,
    button_permissions: enabledResources
      .filter((item) => item.resource_type === 'BUTTON')
      .map((item) => item.code)
      .sort(),
    cache_routes: enabledResources.filter((item) => item.is_cache).map((item) => item.code),
  }
}
