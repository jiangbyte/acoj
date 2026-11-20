import { useRouterStore, useTabStore, useTokenStore } from '@/stores'
import type { Router } from 'vue-router'

/** 可匿名访问的路由前缀（含详情子路径） */
const PUBLIC_ROUTE_PREFIXES = [
  '/home',
  '/problems',
  '/sets',
  '/status',
  '/ranking',
  '/notice',
  '/user',
  '/contests',
  '/groups',
  '/login',
  '/register',
]

function isPublicRoute(path: string) {
  if (path === '/' || path === '')
    return true
  return PUBLIC_ROUTE_PREFIXES.some(prefix => path === prefix || path.startsWith(`${prefix}/`))
}

export function setupRouterGuard(router: Router) {
  const tokenStore = useTokenStore()
  const routerStore = useRouterStore()
  const tabStore = useTabStore()

  router.beforeEach(async (to, _from, next) => {
    // 1. 外链
    if (to.meta.extraLink) {
      if (to.meta.extraLinkEnabled) {
        window.open(to.meta.extraLink as string)
      }
      next(false)
      return
    }

    // 2. 加载条
    window.$loadingBar?.start()

    // 3. 公开路由直接放行
    if (isPublicRoute(to.path)) {
      // 已登录访问登录/注册 → 回首页
      if ((to.name === 'login' || to.name === 'register') && tokenStore.isLogined) {
        next({ path: '/' })
        return
      }
      next()
      return
    }

    // 4. 未登录访问受保护页 → 登录并带回跳
    if (!tokenStore.isLogined) {
      const redirect = to.name === '404' ? undefined : to.fullPath
      next({ path: '/login', query: redirect ? { redirect } : undefined })
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
