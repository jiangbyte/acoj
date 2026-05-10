import type { Router } from 'vue-router'
import { useAppStore, useAuthStore, useRouteStore } from '@/store'

const HOME_PATH = import.meta.env.VITE_HOME_PATH as string || '/dashboard'

export function setupRouterGuard(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    // 外链处理：直接打开新窗口并拦截导航
    if (to.meta?.href) {
      window.open(to.meta.href as string)
      next(false)
      return
    }

    const appStore = useAppStore()
    const authStore = useAuthStore()
    const routeStore = useRouteStore()

    // 根路由处理：已登录跳首页，未登录跳登录
    if (to.name === 'root') {
      if (authStore.isLogin) {
        next({ path: HOME_PATH, replace: true })
        return
      }
      next({ name: 'login', query: { redirect: '/' } })
      return
    }

    // 登录/注册页
    if (to.name === 'login' || to.name === 'register') {
      if (authStore.isLogin) {
        next({ path: HOME_PATH })
        return
      }
      next()
      return
    }

    // 未登录重定向到登录
    if (!authStore.isLogin) {
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }

    // 动态路由初始化（显示加载过渡）
    if (!routeStore.isInitAuthRoute) {
      appStore.setLoading(true)
      try {
        if (!routeStore.menus.length) {
          await authStore.loadMenusAndPermissions()
        }
        if (routeStore.menus.length) {
          await routeStore.initAuthRoute()
        }
      } finally {
        appStore.setLoading(false)
      }
      if (routeStore.isInitAuthRoute) {
        next({ path: to.fullPath, replace: true })
        return
      }
    }

    next()
  })
}
