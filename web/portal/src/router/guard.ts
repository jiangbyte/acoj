import type { RouteLocationNormalized, Router } from 'vue-router'
import { useAuthStore, useRouteStore } from '@/stores'
import { getRouteTitle } from '@/stores/route'
import { isDictLoaded, refreshDict, syncDictTree } from '@/utils/dict'

// 浏览器标题后缀，来自应用环境配置。
const appTitle = import.meta.env.VITE_APP_TITLE

// 首页路径只用于跳转和默认重定向，不用于覆盖资源表里的 path 字段。
const homePath = import.meta.env.VITE_HOME_PATH
const loginPath = '/auth/login'
const publicRoutePaths = parsePublicRoutePaths(import.meta.env.VITE_PUBLIC_ROUTE_PATHS)

/**
 * 注册全局路由守卫。
 *
 * 这里集中处理外链跳转、授权路由初始化、标签页同步、菜单高亮和页面标题等路由副作用。
 */
export function setupRouterGuard(router: Router) {
  router.beforeEach(async (to) => {
    const authStore = useAuthStore()
    const routeStore = useRouteStore()
    syncDictTree()

    // 资源配置了 href 时视为外链。打开新窗口后阻止当前路由继续跳转。
    if (to.meta.href) {
      window.open(to.meta.href)
      return false
    }

    window.$loadingBar?.start()

    const isLogin = authStore.isLogin

    // 已登录用户访问认证页时，直接回到 redirect 或首页。
    if (isAuthRoute(to) && isLogin) {
      return { path: getRedirectPath(to) ?? homePath, replace: true }
    }

    // portal 资源是公开资源，非认证页都需要先注册动态路由和菜单。
    if (!isAuthRoute(to) && !routeStore.isInitAuthRoute) {
      try {
        await Promise.all([routeStore.initAuthRoute(), refreshDict()])
        if (isFallbackRoute(to)) {
          return {
            path: to.fullPath,
            replace: true,
            query: to.query,
            hash: to.hash,
          }
        }
      } catch {
        if (isLogin) {
          authStore.resetSession()
        }
        if (!isPublicRoute(to)) {
          return {
            path: loginPath,
            replace: true,
            query: {
              redirect: to.fullPath,
            },
          }
        }
      }
    }

    // 认证页、错误页和配置公开页不触发登录校验。
    if (isPublicRoute(to)) {
      return
    }

    // 未登录访问后台页面时跳转到登录页，并保留原目标地址。
    if (!isLogin) {
      return {
        path: loginPath,
        query: {
          redirect: to.fullPath,
        },
      }
    }

    if (!isDictLoaded()) {
      await refreshDict()
    }
  })

  // 路由完成后更新浏览器标题并结束顶部加载条。
  router.afterEach((to) => {
    document.title = to.meta.name ? `${getRouteTitle(to)} - ${appTitle}` : appTitle
    window.$loadingBar?.finish()
  })

  // 路由解析异常时给出加载条错误状态，避免 loading 一直停留。
  router.onError(() => {
    window.$loadingBar?.error()
  })
}

// 判断当前路由是否命中了 pathMatch 兜底路由。
function isFallbackRoute(route: RouteLocationNormalized) {
  return route.matched.some((item) => item.path.includes(':pathMatch'))
}

function isAuthRoute(route: RouteLocationNormalized) {
  return route.path.startsWith('/auth')
}

function isPublicRoute(route: RouteLocationNormalized) {
  return (
    isAuthRoute(route) ||
    route.name === 'not-found' ||
    route.meta.public === true ||
    publicRoutePaths.has(route.path)
  )
}

function getRedirectPath(route: RouteLocationNormalized) {
  const redirect = route.query.redirect
  if (typeof redirect !== 'string' || redirect.startsWith('/auth')) {
    return undefined
  }
  return redirect
}

function parsePublicRoutePaths(value?: string) {
  return new Set(
    (value ?? '')
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean),
  )
}
