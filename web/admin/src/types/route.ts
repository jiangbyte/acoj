import type { RouteRecordRaw } from 'vue-router'

export type ResourceType = 'CATALOG' | 'MENU' | 'PAGE' | 'BUTTON' | 'ACTION' | 'API_GROUP'

export type ResourceStatus = 'ENABLED' | 'DISABLED'

export interface RouteResource {
  id: string
  parent_id: string | null
  code: string
  name: string
  resource_type: ResourceType
  module: string | null
  path: string | null
  component: string | null
  redirect: string | null
  icon: string | null
  href: string | null
  sort: number
  is_visible: boolean
  is_cache: boolean
  is_affix: boolean
  status: ResourceStatus
  description: string | null
  extra: Record<string, unknown>
  created_at: string
  created_by: string | null
  updated_at: string
  updated_by: string | null
}

export interface ResourceTreeNode extends RouteResource {
  children?: ResourceTreeNode[]
}

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    description?: string
    icon?: string
    requiresAuth?: boolean
    permission?: string
    hideInMenu?: boolean
    keepAlive?: boolean
    hideHeader?: boolean
    affixTab?: boolean
    activeMenu?: string
    withoutTab?: boolean
    resourceId?: string
    resourceCode?: string
    href?: string
    sort?: number
    extra?: Record<string, unknown>
  }
}

export interface RouteMenuItem {
  key: string
  label: string
  icon?: string | null
  href?: string | null
  children?: RouteMenuItem[]
}

export interface RouteBuildResult {
  routes: RouteRecordRaw[]
  menus: RouteMenuItem[]
  button_permissions: string[]
  cache_routes: string[]
}
