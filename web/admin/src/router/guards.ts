import type { Router } from 'vue-router'

import { DEFAULT_HOME_PATH } from '@/config/app'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { useRouteStore } from '@/stores/route'
import { useUserStore } from '@/stores/user'
import { t } from '@/i18n'
import { translateWithFallback } from '@/utils/i18n'

export function setupRouterGuards(router: Router) {
  router.beforeEach(async (to) => {
    const auth = useAuthStore()
    const user = useUserStore()
    const routeStore = useRouteStore()

    if (to.path === '/auth/login' && auth.isAuthenticated) {
      return {
        path: DEFAULT_HOME_PATH,
        replace: true,
      }
    }

    if (to.meta.href) {
      window.open(String(to.meta.href), '_blank', 'noopener,noreferrer')
      return false
    }

    const needsAuth = to.meta.requiresAuth || to.path === '/' || to.name === 'NotFound'

    if (needsAuth && !auth.isAuthenticated) {
      return {
        path: '/auth/login',
        query: { redirect: to.fullPath },
      }
    }

    if (
      auth.isAuthenticated &&
      !routeStore.is_init_auth_route &&
      to.path !== '/auth/login' &&
      to.path !== '/403' &&
      to.path !== '/500'
    ) {
      try {
        // 首次进入受保护页面时拉取用户与资源，避免刷新后动态路由丢失。
        await user.ensureMe()
        await routeStore.init_auth_route()
      } catch (error) {
        console.error('[router] 初始化认证路由失败', error)
        return {
          path: '/500',
          query: { redirect: to.fullPath },
          replace: true,
        }
      }

      const fallbackPath = routeStore.has_auth_route(DEFAULT_HOME_PATH)
        ? DEFAULT_HOME_PATH
        : routeStore.firstAvailablePath
      const isAuthorizedTarget = routeStore.has_auth_route(to.path)

      if (to.path === '/') {
        return {
          path: fallbackPath || '/403',
          query: to.query,
          hash: to.hash,
          replace: true,
        }
      }

      if (to.name === 'NotFound' && isAuthorizedTarget) {
        return {
          path: to.path,
          query: to.query,
          hash: to.hash,
          replace: true,
        }
      }

      if (to.name === 'NotFound' || !isAuthorizedTarget) {
        return {
          path: fallbackPath || '/403',
          replace: true,
        }
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
