import { useRouterStore, useTabStore, useTokenStore } from '@/stores'
import type { Router } from 'vue-router'

export function setupRouterGuard(router: Router) {
  const tokenStore = useTokenStore()
  const routerStore = useRouterStore()
  const tabStore = useTabStore()

  const publicRoutes = ['/', '/home', '/problems', '/sets', '/status', '/ranking', '/login', '/register', '/forget']

  router.beforeEach(async (to, _from, next) => {
    // 外部链接
    if (to.meta.extraLink) {
      if (to.meta.extraLinkEnabled) {
        window.open(to.meta.extraLink as string)
      }
      return // 终止导航
    }

    // 加载条
    window.$loadingBar?.start()

    // 检查是否是公开路由
    const isPublicRoute = publicRoutes.includes(to.path)

    // 公开路径直接放行
    if (isPublicRoute) {
      next()
      return
    }

    // 登录页直接放行
    if (to.name === 'login') {
      next()
      return
    }

    // 未登录且非公开路径 → 跳转到登录页
    if (!tokenStore.isLogined) {
      const redirect = to.name === '404' ? undefined : to.fullPath
      next({ path: '/login', query: { redirect } })
      return
    }

    // 已登录时访问登录页 → 重定向到首页
    if (to.name === 'login' && tokenStore.isLogined) {
      next({ path: '/' })
      return
    }

    // 其他情况放行
    next()
  })

  router.beforeResolve((to) => {
    routerStore.setActiveMenu(to.path)
    tabStore.addTab(to)
    tabStore.setCurrent(to.path)
  })

  router.afterEach((to) => {
    document.title = `${to.meta.title}`
    window.$loadingBar?.finish()
  })
}
