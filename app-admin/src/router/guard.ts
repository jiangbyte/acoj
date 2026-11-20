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

    // 1. For external links, open a new window and cancel this navigation
    if (parameters?.href_outter) {
      window.open(parameters.href_outter as string)
      next(false)
      return
    }

    // 2. Top loading bar
    window.$loadingBar?.start()

    // 3. When signed out, allow only login and preserve the return URL
    if (!tokenStore.isLogined) {
      if (to.name === 'login') {
        next()
        return
      }
      const redirect = to.name === '404' ? undefined : to.fullPath
      next({ path: '/login', query: redirect ? { redirect } : undefined })
      return
    }

    // 4. On first entry, load dynamic routes and menus; sign out on failure
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

        // Rematch the current URL after registering dynamic routes
        next({ ...to, replace: true })
        return
      }
      catch (error) {
        console.error('Route initialization failed:', error)
        tokenStore.$reset()
        next({ path: '/login' })
        return
      }
    }

    // 5. Redirect authenticated users from login to the home page
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
