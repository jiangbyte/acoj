// import { router } from '@/router'
// import type { MenuOption } from 'naive-ui'
// import { type RouteRecordRaw, RouterLink } from 'vue-router'
// import { clone, min, omit, pick } from 'radash'
// import Layout from '@/layouts/index.vue'
// import { arrayTree, iconRender } from '@/utils'
// import { useSysMenu } from '@/composables'
//
// interface RoutesState {
//   isInit: boolean
//   menus: MenuOption[]
//   rowRoutes: SiteRoute.RowRoute[]
//   activeMenu: string | null
//   cacheRoutes: string[]
// }
//
// export const useRouterStore = defineStore('route-store', {
//   state: (): RoutesState => {
//     return {
//       isInit: false,
//       activeMenu: '',
//       menus: [],
//       rowRoutes: [],
//       cacheRoutes: [],
//     }
//   },
//   actions: {
//     async initAuthRoute() {
//       const { getRawMenus } = useSysMenu()
//       // 动态获取菜单
//       const { data, success } = await getRawMenus()
//       if (success && data) {
//         return data
//       }
//     },
//     async initRouter() {
//       this.isInit = false
//
//       const rowRoutes = await this.initAuthRoute()
//       if (!rowRoutes) {
//         return
//       }
//
//       this.rowRoutes = rowRoutes
//
//       const routes = createRoutes(rowRoutes)
//       router.addRoute(routes)
//
//       this.menus = createMenus(rowRoutes)
//
//       this.cacheRoutes = generateCacheRoutes(rowRoutes)
//
//       this.isInit = true
//     },
//     resetRouteStore() {
//       this.resetRoutes()
//       this.$reset()
//     },
//     resetRoutes() {
//       if (router.hasRoute('webroot')) {
//         router.removeRoute('webroot')
//       }
//     },
//     setActiveMenu(key: string) {
//       this.activeMenu = key
//     },
//   },
// })
//
// const metaFields: SiteRoute.MetaKeys[] = [
//   'title',
//   'sort',
//   'icon',
//   'keepAlive',
//   'extraLink',
//   'activeMenu',
//   'withoutTab',
//   'menuType',
//   'parameter',
//   'pined',
//   'visible',
// ]
//
// function standardizedRoutes(route: SiteRoute.RowRoute[]) {
//   return clone(route).map((i) => {
//     const route = omit(i, metaFields)
//     Reflect.set(route, 'meta', pick(i, metaFields))
//     return route
//   }) as SiteRoute.Route[]
// }
//
// function createRoutes(routes: SiteRoute.RowRoute[]): RouteRecordRaw {
//   let resultRouter = standardizedRoutes(routes)
//
//   const modules = import.meta.glob('@/views/**/*.vue')
//   resultRouter = resultRouter.map((item: SiteRoute.Route) => {
//     if (item.componentPath && !item.redirect)
//       item.component = modules[`/src/views${item.componentPath}`]
//     return item
//   })
//
//   resultRouter = arrayTree(resultRouter) as SiteRoute.Route[]
//
//   const webrootRoute: RouteRecordRaw = {
//     path: '/webroot',
//     name: 'webroot',
//     redirect: '/index',
//     component: Layout,
//     meta: {
//       title: '系统',
//       icon: 'icon-park-outline:web-page',
//     },
//     children: [],
//   }
//
//   setRedirect(resultRouter)
//   webrootRoute.children = resultRouter as unknown as RouteRecordRaw[]
//
//   return webrootRoute
// }
//
// function setRedirect(routes: SiteRoute.Route[]) {
//   routes.forEach((route) => {
//     if (route.children) {
//       if (!route.redirect) {
//         const visibleChilds = route.children.filter(child => child.meta.visible)
//         if (visibleChilds.length > 1) {
//           let target = visibleChilds[0]
//           const orderChilds = visibleChilds.filter(child => Number(child.meta.sort))
//           if (orderChilds.length > 0)
//             target = min(orderChilds, i => Number(i.meta.sort)!) as SiteRoute.Route
//           if (target)
//             route.redirect = target.path
//         }
//       }
//
//       setRedirect(route.children)
//     }
//   })
// }
//
// function createMenus(userRoutes: SiteRoute.RowRoute[]) {
//   const resultMenus = standardizedRoutes(userRoutes)
//   const visibleMenus = resultMenus.filter(route => route.meta.visible)
//   return arrayTree(routesToMenus(visibleMenus))
// }
//
// function routesToMenus(userRoutes: SiteRoute.Route[]) {
//   return userRoutes
//     .sort((a, b) => {
//       if (a.meta && a.meta.sort && b.meta && b.meta.sort)
//         return Number(a.meta.sort) - Number(b.meta.sort)
//       else if (a.meta && a.meta.sort)
//         return -1
//       else if (b.meta && b.meta.sort)
//         return 1
//       else return 0
//     })
//     .map((item) => {
//       const target: MenuOption = {
//         id: Number(item.id),
//         pid: Number(item.pid),
//         label:
//         (!item.meta.menuType || Number(item.meta.menuType) === 1)
//           ? () => h(RouterLink, {
//               to: {
//                 path: item.path,
//               },
//             }, {
//               default: () => item.meta.title,
//             })
//           : () => item.meta.title,
//         key: item.path,
//         icon: item.meta.icon ? iconRender(item.meta.icon) : undefined,
//       }
//       return target
//     })
// }
//
// function generateCacheRoutes(routes: SiteRoute.RowRoute[]) {
//   return routes
//     .filter(i => i.keepAlive)
//     .map(i => i.name)
// }

import { router } from '@/router'
import type { MenuOption } from 'naive-ui'
import { RouterLink } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { clone, min, omit, pick } from 'radash'
import Layout from '@/layouts/index.vue'
import { arrayTree, iconRender } from '@/utils'
import { useSysMenuFetch } from '@/composables/v1'

interface RoutesState {
  isInit: boolean
  menus: MenuOption[]
  rowRoutes: SiteRoute.RowRoute[]
  activeMenu: string | null
  cacheRoutes: string[]
}

const metaFields: SiteRoute.MetaKeys[] = [
  'title',
  'sort',
  'icon',
  'keepAlive',
  'extraLink',
  'activeMenu',
  'withoutTab',
  'menuType',
  'parameter',
  'pined',
  'visible',
  'parameters',
]

export const useRouterStore = defineStore('route-store', {
  state: (): RoutesState => ({
    isInit: false,
    activeMenu: '',
    menus: [],
    rowRoutes: [],
    cacheRoutes: [],
  }),

  actions: {
    async initAuthRoute() {
      const { data, success } = await useSysMenuFetch().sysMenuAuthList()
      // return success && data ? data : rawMenus
      console.log('认证菜单-->', data)
      return success && data ? data : []
      // return rawMenus
    },

    async initRouter() {
      this.isInit = false
      const rowRoutes = await this.initAuthRoute()
      if (!rowRoutes || rowRoutes.length === 0) {
        console.error('获取菜单数据失败')
        return
      }

      this.rowRoutes = rowRoutes

      // router.addRoute(createRoutes(rowRoutes))
      const routes = createRoutes(rowRoutes)
      router.addRoute(routes)

      // 验证路由是否添加成功
      console.log('路由添加后的路由表:', router.getRoutes())

      this.menus = createMenus(rowRoutes)
      this.cacheRoutes = rowRoutes.filter((i: any) => i.keepAlive).map((i: any) => i.name)
      this.isInit = true
    },

    resetRouteStore() {
      this.resetRoutes()
      this.$reset()
    },

    resetRoutes() {
      if (router.hasRoute('webroot'))
        router.removeRoute('webroot')
    },

    setActiveMenu(key: string) {
      this.activeMenu = key
    },
  },
})

function standardizedRoutes(routes: SiteRoute.RowRoute[]) {
  return clone(routes).map(i => ({ ...omit(i, metaFields), meta: pick(i, metaFields) })) as SiteRoute.Route[]
}

function createRoutes(routes: SiteRoute.RowRoute[]): RouteRecordRaw {
  const modules = import.meta.glob('@/views/**/*.vue')
  const resultRouter = arrayTree(
    standardizedRoutes(routes).map(item => ({
      ...item,
      component: item.componentPath && !item.redirect
        ? modules[`/src/views${item.componentPath}`]
        : undefined,
    })),
  ) as SiteRoute.Route[]

  console.log('标准化后的路由表:', resultRouter)

  setRedirect(resultRouter)

  return {
    path: '/webroot',
    name: 'webroot',
    redirect: '/index',
    component: Layout,
    meta: { title: '系统', icon: 'icon-park-outline:web-page' },
    children: resultRouter as unknown as RouteRecordRaw[],
  }
}

function setRedirect(routes: SiteRoute.Route[]) {
  routes.forEach((route) => {
    if (route.children?.length) {
      console.log(route.meta.title, '有子路由:', route.children)
      if (!route.redirect) {
        console.log(route.meta.title, '无跳转')
        const visibleChilds = route.children.filter(child => child.meta.visible)
        // if (visibleChilds.length > 1) {
        //   const orderChilds = visibleChilds.filter(child => Number(child.meta.sort))
        //   const target = orderChilds.length
        //     ? min(orderChilds, i => Number(i.meta.sort)!)
        //     : visibleChilds[0]
        //   if (target)
        //     route.redirect = target.path
        // }
        // 修改这里：当有至少一个可见子菜单时就应该设置重定向
        if (visibleChilds.length >= 1) {
          let target: SiteRoute.Route | null = visibleChilds[0]

          // 如果有多个可见子菜单，按排序选择第一个
          if (visibleChilds.length > 1) {
            // const orderChilds = visibleChilds.filter(child => Number(child.meta.sort))
            // target = orderChilds.length
            //   ? min(orderChilds, i => Number(i.meta.sort)!)
            //   : visibleChilds[0]
            const orderChilds = visibleChilds.filter(child => Number(child.meta.sort))
            const sortedTarget = orderChilds.length
              ? min(orderChilds, i => Number(i.meta.sort)!)
              : visibleChilds[0]

            // 确保 target 不是 null
            if (sortedTarget) {
              target = sortedTarget
            }
          }

          if (target) {
            route.redirect = target.path
            console.log(route.meta.title, '设置重定向到:', target.path)
          }
        }
        else {
          console.log(route.meta.title, '没有可见的子菜单，无法设置重定向')
        }
      }
      console.log(route.meta.title, '最终跳转:', route.redirect)
      setRedirect(route.children)
    }
  })
}

function createMenus(userRoutes: SiteRoute.RowRoute[]) {
  // return arrayTree(
  //   standardizedRoutes(userRoutes)
  //     .filter(route => route.meta.visible)
  //     .sort((a, b) => (Number(a.meta.sort) || 0) - (Number(b.meta.sort) || 0))
  //     .map(item => ({
  //       id: Number(item.id),
  //       pid: Number(item.pid),
  //       label: createLabel(item),
  //       key: item.path,
  //       icon: item.meta.icon ? iconRender(item.meta.icon) : undefined,
  //     })),
  // )

  const fullTree = arrayTree(
    standardizedRoutes(userRoutes)
      .sort((a, b) => (Number(a.meta.sort) || 0) - (Number(b.meta.sort) || 0))
      .map(item => ({
        id: Number(item.id),
        pid: Number(item.pid),
        label: createLabel(item),
        key: item.path,
        icon: item.meta.icon ? iconRender(item.meta.icon) : undefined,
        meta: item.meta, // 保留meta信息用于过滤
        path: item.path, // 保留path信息
      })),
  )

  // 递归过滤：如果父级不可见，子级也要隐藏
  const filterVisibleMenus = (menus: any[]): any[] => {
    return menus
      .filter((menu) => {
        // 如果当前菜单不可见，直接过滤掉
        if (!menu.meta.visible) {
          return false
        }

        // 如果有子菜单，递归处理
        if (menu.children) {
          menu.children = filterVisibleMenus(menu.children)
        }

        return true
      })
      .map(menu => ({
        // 只返回菜单需要的字段，去除meta等额外字段
        id: menu.id,
        pid: menu.pid,
        label: menu.label,
        key: menu.key,
        icon: menu.icon,
        children: menu.children,
      }))
  }

  return filterVisibleMenus(fullTree)
}

function createLabel(item: SiteRoute.Route) {
  return (!item.meta.menuType || Number(item.meta.menuType) === 1)
    ? () => h(RouterLink, { to: { path: item.path } }, () => item.meta.title)
    : () => item.meta.title
}
