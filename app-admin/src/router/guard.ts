import { useRouterStore, useTabStore, useTokenStore } from '@/stores'
import type { Router } from 'vue-router'

function parseJson(json: string) {
  if (json === 'null' || json === 'undefined') {
    return null
  }
  try {
    return JSON.parse(json)
  }
  catch (e) {
    console.error(e)
    return null
  }
}

export function setupRouterGuard(router: Router) {
  const tokenStore = useTokenStore()
  const routerStore = useRouterStore()
  const tabStore = useTabStore()

  router.beforeEach(async (to, _from, next) => {
    const parameters = parseJson(String(to.meta.parameters))

    // 1. 外链：打开新窗口并取消本次导航
    if (parameters?.href_outter) {
      window.open(parameters.href_outter as string)
      next(false)
      return
    }

    // 2. 顶部加载条
    window.$loadingBar?.start()

    // 3. 未登录：仅放行登录页，其余跳转登录并带回跳地址
    if (!tokenStore.isLogined) {
      if (to.name === 'login') {
        next()
        return
      }
      const redirect = to.name === '404' ? undefined : to.fullPath
      next({ path: '/login', query: redirect ? { redirect } : undefined })
      return
    }

    // 4. 首次进入：拉取动态路由/菜单，失败则登出
    if (!routerStore.isInit) {
      try {
        await routerStore.initRouter()

        if (routerStore.rowRoutes.length === 0 || routerStore.menus.length === 0) {
          tokenStore.$reset()
          next({ path: '/login' })
          return
        }

        if (to.name === '404') {
          next({
            path: to.fullPath,
            replace: true,
            query: to.query,
            hash: to.hash,
          })
          return
        }

        // 动态路由刚注册完，需重新匹配当前地址
        next({ ...to, replace: true })
        return
      }
      catch (error) {
        console.error('路由初始化失败:', error)
        tokenStore.$reset()
        next({ path: '/login' })
        return
      }
    }

    // 5. 已登录访问登录页 → 回首页
    if (to.name === 'login') {
      next({ path: '/' })
      return
    }

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
