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
    async init_route_info() {
      return getRouteResources()
    },
    async init_auth_route() {
      this.is_init_auth_route = false
      const resources = await this.init_route_info()
      const result = buildRoutes(resources)

      this.reset_routes()
      result.routes.forEach((route) => router.addRoute(route))

      this.resources = resources
      this.menus = result.menus
      this.button_permissions = result.button_permissions
      this.cache_routes = result.cache_routes
      this.is_init_auth_route = true
    },
    reset_routes() {
      if (router.hasRoute(INNER_ROUTE_NAMES.AdminRoot)) {
        router.removeRoute(INNER_ROUTE_NAMES.AdminRoot)
      }
    },
    reset_route_store() {
      this.reset_routes()
      this.is_init_auth_route = false
      this.resources = []
      this.menus = []
      this.button_permissions = []
      this.cache_routes = []
      this.active_menu = ''
    },
    set_active_menu(key: string) {
      this.active_menu = key
    },
    has_button_permission(key: string) {
      return this.button_permissions.includes(key)
    },
    has_auth_route(path: string) {
      return router.resolve(path).matched.some((route) => route.name === INNER_ROUTE_NAMES.AdminRoot)
    },
  },
})
