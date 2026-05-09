import { defineStore } from 'pinia'
import { router } from '@/router'
import { menusToRoutes } from '@/router/dynamic'

interface RouteState {
  menus: any[]
  isInitAuthRoute: boolean
}

export const useRouteStore = defineStore('route', {
  state: (): RouteState => ({
    menus: [],
    isInitAuthRoute: false,
  }),
  actions: {
    setMenus(menus: any[]) {
      this.menus = menus
    },
    async initAuthRoute() {
      const routes = menusToRoutes(this.menus)
      routes.forEach((route: any) => {
        if (!router.hasRoute(route.name)) {
          router.addRoute('layout', route)
        }
      })
      this.isInitAuthRoute = true
    },
    reset() {
      this.menus = []
      this.isInitAuthRoute = false
    },
  },
})
