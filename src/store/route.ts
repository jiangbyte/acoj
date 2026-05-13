import { defineStore } from 'pinia'
import { router } from '@/router'
import { menusToRoutes } from '@/router/dynamic'

interface RouteState {
  menus: any[]
  isInitAuthRoute: boolean
  cacheRoutes: string[]
}

export const useRouteStore = defineStore('route', {
  state: (): RouteState => ({
    menus: [],
    isInitAuthRoute: false,
    cacheRoutes: [],
  }),
  actions: {
    setMenus(menus: any[]) {
      this.menus = menus
    },
    async initAuthRoute() {
      this.isInitAuthRoute = false
      try {
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

        routes.forEach((route: any) => {
          if (!router.hasRoute(route.name)) {
            router.addRoute('root', route)
          }
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
    },
  },
})
