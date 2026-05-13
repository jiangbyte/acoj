import { resolveIconRender } from '@/utils'

export function menuToItems(menus: any[]): any[] {
  const sorted = [...menus].sort((a, b) => (a.sort_code ?? 0) - (b.sort_code ?? 0))

  return sorted
    .filter((m: any) => {
      if (m.type === 'BUTTON') return false
      return m.is_visible !== 'NO'
    })
    .map((m: any) => ({
      key: m.route_path,
      icon: resolveIconRender(m.icon),
      label: m.name,
      children: m.children?.length ? menuToItems(m.children) : undefined,
    }))
}
