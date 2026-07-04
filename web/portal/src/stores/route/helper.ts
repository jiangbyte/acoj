import type { RouteRecordRaw } from 'vue-router'
import { RouterLink } from 'vue-router'
import { h } from 'vue'
import { renderIcon } from '@/utils/icon'

// 能参与前端路由体系的资源类型。按钮、动作、接口分组只用于权限控制，不生成页面路由。
const routeResourceTypes: AppRoute.ResourceType[] = ['CATALOG', 'MENU', 'PAGE']

// 能点击跳转的资源类型。目录只承担分组作用，不直接渲染 RouterLink。
const clickableResourceTypes: AppRoute.ResourceType[] = ['MENU', 'PAGE']

/**
 * 根据资源列表生成 portal 动态子路由。
 *
 * 动态资源会被追加到 routes.inner.ts 中的 portalRoot 下；静态路由始终保留。
 */
export function createRoutes(resources: AppRoute.RowRoute[]): RouteRecordRaw[] {
  const modules = import.meta.glob('@/views/**/*.vue')
  const routes = standardizedRoutes(resources)
    .map((item) => {
      const resourceComponent = item.meta.component

      if (isClickableResource(item.meta.resource_type) && !item.redirect && resourceComponent) {
        const component = modules[`/src/views${resourceComponent}`] as RouteRecordRaw['component']
        if (!component) {
          return null
        }
        item.component = component
      }

      return item
    })
    .filter((item): item is AppRoute.Route => Boolean(item))

  const resultRoutes = arrayToTree(routes)
  setRedirect(resultRoutes)

  return resultRoutes as unknown as RouteRecordRaw[]
}

/**
 * 根据资源列表生成 portal 导航菜单。
 *
 * 菜单只展示 is_visible=true 的资源；隐藏页面仍可生成路由，但不会出现在导航中。
 */
export function createMenus(resources: AppRoute.RowRoute[]): AppRoute.MenuOption[] {
  const visibleMenus = standardizedRoutes(resources).filter((route) => route.meta.is_visible)
  return arrayToTree(transformRoutesToMenus(visibleMenus))
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
      const label = () => item.meta.name ?? String(item.name)
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
 * 使用 id/parent_id 建立父子关系；找不到父节点的资源会被视为根节点，避免异常数据导致导航丢失。
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
