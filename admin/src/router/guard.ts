import { useRouterStore, useTabStore, useTokenStore } from '@/stores'
import type { Router } from 'vue-router'

export function setupRouterGuard(router: Router) {
  const tokenStore = useTokenStore()
  const routerStore = useRouterStore()
  const tabStore = useTabStore()

  router.beforeEach(async (to, from, next) => {
    console.log('导航守卫触发:', {
      to: to.path,
      from: from.path,
      isLogined: tokenStore.isLogined,
      isInit: routerStore.isInit,
      rowRoutesLength: routerStore.rowRoutes?.length,
    })
    // 外部链接
    if (to.meta.extraLink) {
      if (to.meta.extraLink && to.meta.extraLinkEnabled) {
        window.open(to.meta.extraLink as string)
      }
      return
    }

    // 加载条
    window.$loadingBar?.start()

    // 鉴权
    if (!tokenStore.isLogined) {
      if (to.name === 'login') {
        next()
      }

      if (to.name !== 'login') {
        const redirect = to.name === '404' ? undefined : to.fullPath
        next({ path: '/login', query: { redirect } })
      }
      return false
    }

    // 路由初始化
    if (!routerStore.isInit) {
      try {
        await routerStore.initRouter()

        if (routerStore.rowRoutes.length === 0 || routerStore.menus.length === 0) {
          tokenStore.$reset()
          next({ path: '/login' })
        }

        if (to.name === '404') {
          next({
            path: to.fullPath,
            replace: true,
            query: to.query,
            hash: to.hash,
          })
          return false
        }
      }
      catch (error) {
        console.error('路由初始化失败:', error)
        tokenStore.$reset()
        next({ path: '/login' })
        return false
      }
    }

    // 已登录状态下访问登录页，重定向到首页
    if (to.name === 'login') {
      next({ path: '/' })
      return false
    }

    next()
  })
  router.beforeResolve((to) => {
    routerStore.setActiveMenu(to.path)
    tabStore.addTab(to)
    tabStore.setCurrent(to.path)
  })
  router.afterEach((to) => {
    // 修改网页标题
    document.title = `${to.meta.title}`
    // 结束 loadingBar
    window.$loadingBar?.finish()
  })
}
