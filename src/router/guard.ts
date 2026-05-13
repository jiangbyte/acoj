import type { Router } from 'vue-router'
import { useAppStore, useAuthStore, useRouteStore } from '@/store'

const HOME_PATH = (import.meta.env.VITE_HOME_PATH as string) || '/dashboard'
let _retryingNotFound = false

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
        next({ path: to.path, query: to.query, hash: to.hash, replace: true })
        return
      }
      // 动态路由初始化失败（如 token 过期），跳转登录页
      if (!authStore.isLogin) {
        next({ name: 'login', query: { redirect: to.fullPath } })
        return
      }
    }

    // 动态路由已初始化但导航命中了 not-found（如 login() 提前初始化路由后），重新触发解析
    if (routeStore.isInitAuthRoute && to.name === 'not-found' && !_retryingNotFound) {
      _retryingNotFound = true
      next({ path: to.fullPath, query: to.query, hash: to.hash, replace: true })
      return
    }
    if (to.name !== 'not-found') {
      _retryingNotFound = false
    }

    next()
  })
}
