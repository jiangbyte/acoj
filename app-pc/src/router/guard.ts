import { useRouterStore, useTabStore, useTokenStore } from '@/stores'
import type { Router } from 'vue-router'

/** Implementation details. */
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
    // Implementation note.
    if (to.meta.extraLink) {
      if (to.meta.extraLinkEnabled) {
        window.open(to.meta.extraLink as string)
      }
      next(false)
      return
    }

    // Implementation note.
    window.$loadingBar?.start()

    // Implementation note.
    if (isPublicRoute(to.path)) {
      // Implementation note.
      if ((to.name === 'login' || to.name === 'register') && tokenStore.isLogined) {
        next({ path: '/' })
        return
      }
      next()
      return
    }

    // Implementation note.
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
