import type { RouteLocationNormalized, Router } from 'vue-router'
import { useRouteStore, useTabStore } from '@/stores'

// 浏览器标题后缀，来自应用环境配置。
const appTitle = import.meta.env.VITE_APP_TITLE

// 首页路径只用于跳转和默认重定向，不用于覆盖资源表里的 path 字段。
const homePath = import.meta.env.VITE_HOME_PATH

/**
 * 注册全局路由守卫。
 *
 * 这里集中处理外链跳转、授权路由初始化、标签页同步、菜单高亮和页面标题等路由副作用。
 */
export function setupRouterGuard(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    const routeStore = useRouteStore()

    // 资源配置了 href 时视为外链。打开新窗口后阻止当前路由继续跳转。
    if (to.meta.href) {
      window.open(to.meta.href)
      next(false)
      return
    }

    window.$loadingBar?.start()

    // 根路径只做入口转发，真正首页由 VITE_HOME_PATH 控制。
    if (to.name === 'root') {
      next({ path: homePath, replace: true })
      return
    }

    // 首次进入系统时注册授权路由。注册完成后，如果当前命中的是 404 兜底路由，需要重新匹配一次目标路径。
    if (!routeStore.isInitAuthRoute) {
      try {
        await routeStore.initAuthRoute()
        if (isFallbackRoute(to)) {
          next({
            path: to.fullPath,
            replace: true,
            query: to.query,
            hash: to.hash,
          })
          return
        }
      } catch {
        next({ name: 'not-found', replace: true })
        return
      }
    }

    next()
  })

  // 路由解析完成前同步 UI 状态：侧边菜单高亮、标签页新增、当前标签记录。
  router.beforeResolve((to) => {
    const routeStore = useRouteStore()
    const tabStore = useTabStore()

    routeStore.setCurrentMenuPath(to.path)
    tabStore.addTab(to)
    tabStore.setCurrentTab(to.fullPath)
  })

  // 路由完成后更新浏览器标题并结束顶部加载条。
  router.afterEach((to) => {
    document.title = to.meta.name ? `${to.meta.name} - ${appTitle}` : appTitle
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
