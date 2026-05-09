import { h } from 'vue'
import * as Icons from '@ant-design/icons-vue'

function resolveIcon(name: string): any {
  const pascal = name
    .split(/[-_]/)
    .map(s => s.charAt(0).toUpperCase() + s.slice(1))
    .join('')
  return (Icons as any)[pascal + 'Outlined'] || (Icons as any)[pascal]
}

export function menuToItems(menus: any[]): any[] {
  return menus.filter((m: any) => !m.is_hidden || m.is_hidden === 'NO').map((m: any) => ({
    key: m.route_path,
    icon: m.icon ? () => h(resolveIcon(m.icon)) : undefined,
    label: m.name,
    children: m.children?.length ? menuToItems(m.children) : undefined,
  }))
}
