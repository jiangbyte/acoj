import { defineStore } from 'pinia'
import { router } from '@/router'
import { resourceApi } from '@/api'
import { staticRoutes } from '@/router/routes.static'
import { createMenus, createRoutes } from './route/helper'

/**
 * 路由 store 状态。
 *
 * 这里保存的是前端运行期需要的路由派生数据：原始资源列表、侧边菜单、缓存组件名等。
 * 后端返回的授权资源不要在组件里重复加工，统一通过 route/helper.ts 转换。
 */
interface RouteState {
  // 授权路由是否已经初始化完成；路由守卫依赖它避免重复注册动态路由。
  isInitAuthRoute: boolean

  // 侧边栏菜单数据，由 SysResource 资源列表转换而来。
  menus: AppRoute.MenuOption[]

  // 原始资源列表，字段与后端 SysResource 保持一致。
  rowRoutes: AppRoute.RowRoute[]

  // 动态追加到 portalRoot 下的顶级路由名，用于退出登录或权限变化时移除。
  dynamicRouteNames: string[]
}

// 当前 store 没有 getters；显式给空类型可以降低 Pinia 的复杂类型推断。
type RouteGetters = Record<string, never>

interface RouteActions {
  // 重置路由 store，同时移除已注册的 portal 动态路由。
  resetRouteStore: () => void

  // 只移除动态路由，不清理 store 内其它状态。
  resetRoutes: () => void

  // 加载授权资源。static 模式返回本地静态资源，dynamic 模式后续接后端接口。
  initRouteInfo: () => Promise<AppRoute.RowRoute[]>

  // 初始化授权路由、菜单和缓存路由列表。
  initAuthRoute: () => Promise<void>
}

/**
 * 授权路由 store。
 *
 * 这里使用 defineStore 显式泛型，是为了避免 Pinia 自动推断深层递归类型时触发
 * “Type instantiation is excessively deep and possibly infinite”。
 */
export const useRouteStore = defineStore<'route-store', RouteState, RouteGetters, RouteActions>(
  'route-store',
  {
    state: (): RouteState => ({
      isInitAuthRoute: false,
      menus: [],
      rowRoutes: [],
      dynamicRouteNames: [],
    }),
    actions: {
      /**
       * 完整重置路由模块。
       *
       * 常用于退出登录、切换账号、权限变化后重新初始化路由。
       */
      resetRouteStore() {
        this.resetRoutes()
        this.$reset()
      },

      /**
       * 移除动态添加到 portalRoot 下的顶级路由。
       */
      resetRoutes() {
        this.dynamicRouteNames.forEach((name) => {
          if (router.hasRoute(name)) {
            router.removeRoute(name)
          }
        })
        this.dynamicRouteNames = []
      },

      /**
       * 加载当前用户可访问的资源列表。
       *
       * 当前 dynamic 分支仍然返回静态数据，后续接入接口时只需要替换 fetchUserRoutes。
       */
      async initRouteInfo() {
        if (import.meta.env.VITE_ROUTE_LOAD_MODE === 'dynamic') {
          return fetchUserRoutes()
        }

        return staticRoutes
      },

      /**
       * 初始化授权路由信息。
       *
       * 执行顺序：
       * 1. 加载资源列表；
       * 2. 移除旧的动态路由，防止重复注册；
       * 3. 根据资源生成 Vue Router 子路由并追加到 portalRoot；
       * 4. 根据资源生成 portal 导航菜单。
       */
      async initAuthRoute() {
        this.isInitAuthRoute = false

        const rowRoutes = await this.initRouteInfo()
        this.rowRoutes = rowRoutes

        this.resetRoutes()

        const routes = createRoutes(rowRoutes)
        routes.forEach((route) => {
          router.addRoute('portalRoot', route)
          if (route.name) {
            this.dynamicRouteNames.push(String(route.name))
          }
        })

        this.menus = createMenus(rowRoutes)
        this.isInitAuthRoute = true
      },
    },
  },
)

/**
 * 获取路由展示标题。
 *
 * 资源路由展示后端资源 name；非资源内部路由则回退到 route.name 或 path。
 */
export function getRouteTitle(route: {
  name?: string | symbol | null
  path?: string
  meta: {
    name?: string
  }
}) {
  return route.meta.name ?? String(route.name ?? route.path)
}

// 动态路由接口占位。当前没有接入后端资源接口，所以仍返回静态资源数据。
async function fetchUserRoutes(): Promise<AppRoute.RowRoute[]> {
  const response = await resourceApi.current()
  return response.data ?? []
}
