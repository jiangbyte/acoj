import { defineStore } from 'pinia'
import { router } from '@/router'
import { staticRoutes } from '@/router/routes.static'
import { $t, routeI18nKey } from '@/utils/i18n'
import {
  createMenus,
  createRoutes,
  generateCacheRoutes,
  getActiveMenuPath,
} from './route/helper'

type TranslateFn = (key: string, fallback: string) => string

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

  // 当前需要高亮的菜单路径。隐藏页面会回退到最近的可见父级菜单。
  currentMenuPath: string | null

  // keep-alive include 列表，值为 route.name；当前使用资源 code 作为 route.name。
  cacheRoutes: string[]
}

// 当前 store 没有 getters；显式给空类型可以降低 Pinia 的复杂类型推断。
type RouteGetters = Record<string, never>

interface RouteActions {
  // 重置路由 store，同时移除已注册的 appRoot 动态路由。
  resetRouteStore: () => void

  // 只移除动态路由，不清理 store 内其它状态。
  resetRoutes: () => void

  // 根据当前访问路径计算侧边菜单高亮路径。
  setCurrentMenuPath: (path: string) => void

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
      currentMenuPath: null,
      cacheRoutes: [],
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
       * 移除动态添加的应用根路由。
       *
       * appRoot 是 createRoutes 生成的后台布局根节点，所有授权页面都挂在它下面。
       */
      resetRoutes() {
        if (router.hasRoute('appRoot')) {
          router.removeRoute('appRoot')
        }
      },

      /**
       * 设置当前菜单高亮路径。
       *
       * 对隐藏页面，例如详情页，会通过资源父级关系向上查找可见菜单。
       */
      setCurrentMenuPath(path: string) {
        this.currentMenuPath = getActiveMenuPath(this.rowRoutes, path)
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
       * 3. 根据资源生成 Vue Router 路由并注册；
       * 4. 根据资源生成侧边菜单和 keep-alive 缓存列表。
       */
      async initAuthRoute() {
        this.isInitAuthRoute = false

        const rowRoutes = await this.initRouteInfo()
        this.rowRoutes = rowRoutes

        if (router.hasRoute('appRoot')) {
          router.removeRoute('appRoot')
        }

        router.addRoute(createRoutes(rowRoutes))
        this.menus = createMenus(rowRoutes)
        this.cacheRoutes = generateCacheRoutes(rowRoutes)
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
export function getRouteTitle(
  route: {
    name?: string | symbol | null
    path?: string
    meta: {
      name?: string
    }
  },
  translate: TranslateFn = (key, fallback) => $t(key, fallback),
) {
  const fallback = route.meta.name ?? String(route.name ?? route.path)
  return translate(routeI18nKey(route.name), fallback)
}

// 动态路由接口占位。当前没有接入后端资源接口，所以仍返回静态资源数据。
async function fetchUserRoutes(): Promise<AppRoute.RowRoute[]> {
  return staticRoutes
}
