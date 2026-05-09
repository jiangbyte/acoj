import type { Router } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useRouteStore } from '@/store/route'

export function setupRouterGuard(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    const authStore = useAuthStore()
    const routeStore = useRouteStore()

    if (to.name === 'login' || to.name === 'register') {
      if (authStore.isLogin) {
        next({ path: '/' })
        return
      }
      next()
      return
    }

    if (!authStore.isLogin) {
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }

    if (!routeStore.isInitAuthRoute) {
      if (!routeStore.menus.length) {
        await authStore.loadMenusAndPermissions()
      }
      if (routeStore.menus.length) {
        await routeStore.initAuthRoute()
        next({ ...to, replace: true })
        return
      }
    }

    next()
  })
}
