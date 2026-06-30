import type { RouteRecordRaw } from 'vue-router'
import { RouterLink } from 'vue-router'
import { h } from 'vue'
import Layout from '@/layouts/index.vue'
import { renderIcon } from '@/utils/icon'
import { translateLocale } from '@/utils/i18n'

// 能参与前端路由体系的资源类型。按钮、动作、接口分组只用于权限控制，不生成页面路由。
const routeResourceTypes: AppRoute.ResourceType[] = ['CATALOG', 'MENU', 'PAGE']

// 能点击跳转的资源类型。目录只承担分组作用，不直接渲染 RouterLink。
const clickableResourceTypes: AppRoute.ResourceType[] = ['MENU', 'PAGE']

/**
 * 根据资源列表生成 Vue Router 动态路由。
 *
 * 后端资源字段 component 存的是 views 下的相对组件路径，例如 /dashboard/index.vue；
 * 这里通过 import.meta.glob 建立组件映射，再把 MENU/PAGE 资源转换成真实组件路由。
 */
export function createRoutes(resources: AppRoute.RowRoute[]): RouteRecordRaw {
  let resultRoutes = standardizedRoutes(resources)

  // Vite 会在构建时静态分析 glob，只有存在于 src/views 下的页面组件能被加载。
  const modules = import.meta.glob('@/views/**/*.vue')
  resultRoutes = resultRoutes.map((item) => {
    const resourceComponent = item.meta.component
    if (isClickableResource(item.meta.resource_type) && resourceComponent && !item.redirect) {
      item.component = modules[`/src/views${resourceComponent}`] as RouteRecordRaw['component']
    }
    return item
  })

  resultRoutes = arrayToTree(resultRoutes)

  // 所有授权页面都挂在 appRoot 下，统一使用后台 Layout。
  const appRootRoute: RouteRecordRaw = {
    path: '/appRoot',
    name: 'appRoot',
    redirect: import.meta.env.VITE_HOME_PATH,
    component: Layout,
    meta: {},
    children: [],
  }

  setRedirect(resultRoutes)
  appRootRoute.children = resultRoutes as unknown as RouteRecordRaw[]

  return appRootRoute
}

/**
 * 根据资源列表生成侧边菜单。
 *
 * 菜单只展示 is_visible=true 的资源；隐藏页面仍可生成路由，但不会出现在侧边菜单中。
 */
export function createMenus(resources: AppRoute.RowRoute[]): AppRoute.MenuOption[] {
  const visibleMenus = standardizedRoutes(resources).filter((route) => route.meta.is_visible)
  return arrayToTree(transformRoutesToMenus(visibleMenus))
}

/**
 * 生成 keep-alive 的 include 列表。
 *
 * Vue keep-alive 按组件 name 匹配；当前项目约定资源 code 同时作为 route.name。
 */
export function generateCacheRoutes(resources: AppRoute.RowRoute[]) {
  return resources
    .filter((resource) => isRouteResource(resource) && resource.is_cache)
    .map((resource) => resource.code)
}

/**
 * 计算当前应该高亮的菜单路径。
 *
 * 如果当前页面本身可见且可点击，直接高亮它自己；
 * 如果当前页面是隐藏页面，则沿 parent_id 向上查找最近的可见父级资源。
 */
export function getActiveMenuPath(resources: AppRoute.RowRoute[], path: string) {
  const routeResources = resources.filter(isRouteResource)
  const current = routeResources.find((resource) => resource.path === path)

  if (!current) {
    return path
  }

  if (current.is_visible && isClickableResource(current.resource_type) && current.path) {
    return current.path
  }

  const resourceMap = new Map(routeResources.map((resource) => [resource.id, resource]))
  let parentId = current.parent_id

  while (parentId) {
    const parent = resourceMap.get(parentId)
    if (!parent) {
      break
    }
    if (parent.is_visible && parent.path) {
      return parent.path
    }
    parentId = parent.parent_id
  }

  return current.path ?? path
}

/**
 * 判断资源是否能进入前端路由系统。
 *
 * 只有启用、有 path、且类型属于 CATALOG/MENU/PAGE 的资源才会被转换。
 */
export function isRouteResource(resource: AppRoute.RowRoute) {
  return (
    resource.status === 'ENABLED' &&
    Boolean(resource.path) &&
    routeResourceTypes.includes(resource.resource_type)
  )
}

/**
 * 判断资源类型是否能点击跳转。
 *
 * CATALOG 目录需要出现在菜单树里，但不应该生成 RouterLink。
 */
export function isClickableResource(resourceType?: AppRoute.ResourceType) {
  return Boolean(resourceType && clickableResourceTypes.includes(resourceType))
}

/**
 * 标准化资源为内部路由节点。
 *
 * route.name 使用资源 code，meta 保留完整 SysResource 字段，避免旧字段和资源字段混用。
 */
function standardizedRoutes(resources: AppRoute.RowRoute[]) {
  return resources.filter(isRouteResource).map((resource) => {
    const route: AppRoute.Route = {
      id: resource.id,
      parent_id: resource.parent_id,
      code: resource.code,
      name: resource.code,
      locale_key: resource.locale_key,
      resource_type: resource.resource_type,
      module_id: resource.module_id,
      module_id_name: resource.module_id_name,
      path: resource.path!,
      redirect: resource.redirect ?? undefined,
      icon: resource.icon,
      href: resource.href,
      sort: resource.sort,
      is_visible: resource.is_visible,
      is_cache: resource.is_cache,
      is_affix: resource.is_affix,
      status: resource.status,
      description: resource.description,
      meta: { ...resource },
    }

    return route
  })
}

/**
 * 把内部路由节点转换为菜单节点。
 *
 * 菜单 key 使用 path，保证 Naive UI 菜单选中态和路由路径保持一致。
 */
function transformRoutesToMenus(routes: AppRoute.Route[]): AppRoute.MenuOption[] {
  return routes
    .sort((a, b) => (a.meta.sort ?? 99) - (b.meta.sort ?? 99))
    .map((item) => {
      const label = () => translateLocale(item.meta.locale_key, item.meta.name ?? String(item.name))
      const menu: AppRoute.MenuOption = {
        key: item.path,
        label: isClickableResource(item.meta.resource_type)
          ? () =>
              h(
                RouterLink,
                {
                  to: {
                    path: item.path,
                  },
                },
                { default: label },
              )
          : label,
        icon: item.meta.icon ? renderIcon(item.meta.icon) : undefined,
      }

      // 轻量菜单类型额外保留资源 id/parent_id，供 arrayToTree 组装父子关系。
      Reflect.set(menu, 'id', item.id)
      Reflect.set(menu, 'parent_id', item.parent_id)

      return menu
    })
}

/**
 * 为目录节点补默认重定向。
 *
 * 后端未配置 redirect 时，自动跳转到第一个可见子节点；子节点按 sort 升序选择。
 */
function setRedirect(routes: AppRoute.Route[]) {
  routes.forEach((route) => {
    if (!route.children?.length) {
      return
    }

    if (!route.redirect) {
      const visibleChildren = route.children.filter((child) => child.meta.is_visible)
      const target = [...visibleChildren].sort(
        (a, b) => (a.meta.sort ?? 99) - (b.meta.sort ?? 99),
      )[0]

      if (target) {
        route.redirect = target.path
      }
    }

    setRedirect(route.children)
  })
}

/**
 * 把扁平资源列表转换成树。
 *
 * 使用 id/parent_id 建立父子关系；找不到父节点的资源会被视为根节点，避免异常数据导致菜单丢失。
 */
function arrayToTree<T extends { id?: string; parent_id?: string | null; children?: T[] }>(
  items: T[],
) {
  const nodeMap = new Map<string, T>()
  const tree: T[] = []

  items.forEach((item) => {
    if (item.id !== undefined) {
      nodeMap.set(item.id, item)
    }
  })

  items.forEach((item) => {
    if (item.parent_id === null || item.parent_id === undefined || !nodeMap.has(item.parent_id)) {
      tree.push(item)
      return
    }

    const parent = nodeMap.get(item.parent_id)
    if (!parent) {
      tree.push(item)
      return
    }

    parent.children = parent.children ?? []
    parent.children.push(item)
  })

  return tree
}
