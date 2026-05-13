import { defineStore } from 'pinia'
import { router } from '@/router'
import { menusToRoutes } from '@/router/dynamic'

interface RouteState {
  menus: any[]
  isInitAuthRoute: boolean
  cacheRoutes: string[]
  authRouteNames: string[]
}

export const useRouteStore = defineStore('route', {
  state: (): RouteState => ({
    menus: [],
    isInitAuthRoute: false,
    cacheRoutes: [],
    authRouteNames: [],
  }),
  actions: {
    setMenus(menus: any[]) {
      this.menus = menus
    },
    async initAuthRoute() {
      this.isInitAuthRoute = false
      try {
        // Remove previously added auth routes so re-init picks up changes
        this.authRouteNames.forEach(name => {
          try {
            router.removeRoute(name)
          } catch {
            /* already removed */
          }
        })
        this.authRouteNames = []

        const routes = menusToRoutes(this.menus)
        const cacheNames: string[] = []

        function collectCache(routes: any[]) {
          routes.forEach((route: any) => {
            if (route.meta?.cache) {
              cacheNames.push(route.name)
            }
            if (route.children) {
              collectCache(route.children)
            }
          })
        }
        collectCache(routes)

        // Collect all route names (including nested children) for future cleanup
        function collectNames(routes: any[]): string[] {
          const names: string[] = []
          routes.forEach(route => {
            if (route.name) names.push(route.name)
            if (route.children) names.push(...collectNames(route.children))
          })
          return names
        }
        this.authRouteNames = collectNames(routes)

        routes.forEach((route: any) => {
          router.addRoute('root', route)
        })

        this.cacheRoutes = cacheNames
        this.isInitAuthRoute = true
      } catch (error) {
        this.isInitAuthRoute = false
        throw error
      }
    },
    reset() {
      this.menus = []
      this.isInitAuthRoute = false
      this.cacheRoutes = []
      this.authRouteNames = []
    },
  },
})
