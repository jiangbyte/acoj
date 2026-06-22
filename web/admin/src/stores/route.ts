import { defineStore } from 'pinia'

import { router } from '@/router'
import { INNER_ROUTE_NAMES } from '@/router/inner-routes'
import { buildRoutes } from '@/router/routes'
import { getRouteResources } from '@/apis/route'
import type { RouteMenuItem, RouteResource } from '@/types/route'

export const useRouteStore = defineStore('route', {
  state: () => ({
    is_init_auth_route: false,
    resources: [] as RouteResource[],
    menus: [] as RouteMenuItem[],
    button_permissions: [] as string[],
    cache_routes: [] as string[],
    active_menu: '',
    dynamic_route_names: [] as string[],
  }),
  getters: {
    firstAvailablePath: (state) => {
      const findPath = (items: RouteMenuItem[]): string => {
        for (const item of items) {
          if (item.href) {
            continue
          }
          if (item.children?.length) {
            const childPath = findPath(item.children)
            if (childPath) {
              return childPath
            }
          }
          if (item.key.startsWith('/')) {
            return item.key
          }
        }
        return ''
      }

      return findPath(state.menus)
    },
  },
  actions: {
    async fetchMenu() {
      return getRouteResources()
    },
    async loadMenu() {
      const resources = await this.fetchMenu()
      const result = buildRoutes(resources)
      this.resources = resources
      this.menus = result.menus
      this.button_permissions = result.button_permissions
      this.cache_routes = result.cache_routes
      return result.routes
    },
    async refreshMenu() {
      this.is_init_auth_route = false
      const routes = await this.loadMenu()
      this.reset_routes()
      routes.forEach((route) => {
        router.addRoute(INNER_ROUTE_NAMES.AdminRoot, route)
        if (route.name) {
          this.dynamic_route_names.push(String(route.name))
        }
      })
      this.is_init_auth_route = true
    },
    async init_auth_route() {
      await this.refreshMenu()
    },
    reset_routes() {
      this.dynamic_route_names.forEach((name) => {
        if (router.hasRoute(name)) {
          router.removeRoute(name)
        }
      })
      this.dynamic_route_names = []
    },
    reset_route_store() {
      this.reset_routes()
      this.is_init_auth_route = false
      this.resources = []
      this.menus = []
      this.button_permissions = []
      this.cache_routes = []
      this.active_menu = ''
      this.dynamic_route_names = []
    },
    set_active_menu(key: string) {
      this.active_menu = key
    },
    has_button_permission(key: string) {
      return this.button_permissions.includes(key)
    },
    get_accessible_path(path: string) {
      return this.has_auth_route(path) ? path : this.firstAvailablePath || '/403'
    },
    has_auth_route(path: string) {
      const resolved = router.resolve(path)
      return resolved.matched.some((route) => route.name === INNER_ROUTE_NAMES.AdminRoot) &&
        resolved.name !== INNER_ROUTE_NAMES.NotFound
    },
  },
})
