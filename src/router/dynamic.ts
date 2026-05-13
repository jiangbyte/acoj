const modules = import.meta.glob('/src/views/**/*.vue')

export function menusToRoutes(menus: any[]): any[] {
  const sorted = [...menus].sort((a, b) => (a.sort_code ?? 0) - (b.sort_code ?? 0))

  return sorted
    .filter((m: any) => m.type !== 'BUTTON')
    .map((m: any) => {
      const meta: Record<string, any> = {
        title: m.name,
        icon: m.icon,
        type: m.type,
        cache: m.is_cache === 'YES',
        affix: m.is_affix === 'YES',
        breadcrumb: m.is_breadcrumb !== 'NO',
        visible: m.is_visible !== 'NO',
      }

      if (m.type === 'EXTERNAL_LINK' && m.external_url) {
        meta.href = m.external_url
        return {
          path: m.route_path,
          name: m.code,
          meta,
        }
      }

      const route: any = {
        path: m.route_path,
        name: m.code,
        redirect: m.redirect_path || undefined,
        meta,
      }

      if (m.component_path) {
        const fullPath = `/src/views/${m.component_path}.vue`
        route.component = modules[fullPath] || (() => import('@/views/error/404.vue'))
      }

      if (m.children?.length && m.is_visible !== 'NO') {
        route.children = menusToRoutes(m.children)
      }

      return route
    })
}
