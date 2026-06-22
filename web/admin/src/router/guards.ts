import type { Router } from 'vue-router'

import { DEFAULT_HOME_PATH } from '@/config/app'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { useRouteStore } from '@/stores/route'
import { useUserStore } from '@/stores/user'
import { t } from '@/i18n'
import { translateWithFallback } from '@/utils/i18n'

const publicPaths = ['/auth/login', '/403', '/500']

export function setupRouterGuards(router: Router) {
  router.beforeEach(async (to) => {
    const auth = useAuthStore()
    const user = useUserStore()
    const routeStore = useRouteStore()

    if (to.path === '/auth/login' && auth.isAuthenticated) {
      try {
        await user.ensureMe()
        await routeStore.refreshMenu()
      } catch (error) {
        console.error('[router] 初始化认证路由失败', error)
        return {
          path: '/500',
          query: { redirect: to.fullPath },
          replace: true,
        }
      }
      return {
        path: routeStore.get_accessible_path(DEFAULT_HOME_PATH),
        replace: true,
      }
    }

    if (to.meta.href) {
      window.open(String(to.meta.href), '_blank', 'noopener,noreferrer')
      return false
    }

    const needsAuth = !publicPaths.includes(to.path)

    if (needsAuth && !auth.isAuthenticated) {
      return {
        path: '/auth/login',
        query: { redirect: to.fullPath },
      }
    }

    if (
      auth.isAuthenticated &&
      !routeStore.is_init_auth_route &&
      needsAuth
    ) {
      try {
        await user.ensureMe()
        await routeStore.refreshMenu()
      } catch (error) {
        console.error('[router] 初始化认证路由失败', error)
        return {
          path: '/500',
          query: { redirect: to.fullPath },
          replace: true,
        }
      }

      if (!routeStore.firstAvailablePath) {
        return { path: '/403', replace: true }
      }

      return {
        path: to.path,
        query: to.query,
        hash: to.hash,
        replace: true,
      }
    }

    if (auth.isAuthenticated && to.path === '/') {
      return {
        path: routeStore.get_accessible_path(DEFAULT_HOME_PATH),
        replace: true,
      }
    }

    return true
  })

  router.beforeResolve((to) => {
    const app = useAppStore()
    const routeStore = useRouteStore()
    routeStore.set_active_menu(String(to.meta.activeMenu || to.path))
    app.addVisitedTab(to)
    app.setCurrentTab(to.fullPath)
  })

  router.afterEach((to) => {
    document.title = `${translateWithFallback(to.meta.titleKey, String(to.meta.title || t('app.admin')))} - ${import.meta.env.VITE_APP_TITLE}`
  })
}
