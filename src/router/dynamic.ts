const modules = import.meta.glob('/src/views/**/*.vue')

export function menusToRoutes(menus: any[]): any[] {
  return menus.map((m: any) => {
    const route: any = {
      path: m.route_path,
      name: m.code,
      redirect: m.redirect_path || undefined,
      meta: {
        title: m.name,
        icon: m.icon,
        hidden: m.is_hidden === 'YES',
        cache: m.is_cache === 'YES',
      },
    }
    if (m.component_path) {
      const fullPath = `/src/views/${m.component_path}.vue`
      route.component = modules[fullPath] || (() => import('@/views/error/404.vue'))
    }
    if (m.children?.length) {
      route.children = menusToRoutes(m.children)
    }
    return route
  })
}
