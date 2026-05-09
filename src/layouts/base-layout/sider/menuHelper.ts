import { h } from 'vue'
import * as Icons from '@ant-design/icons-vue'

export function menuToItems(menus: any[]): any[] {
  return menus.filter((m: any) => !m.is_hidden || m.is_hidden === 'NO').map((m: any) => ({
    key: m.route_path,
    icon: m.icon ? () => h((Icons as any)[m.icon]) : undefined,
    label: m.name,
    children: m.children?.length ? menuToItems(m.children) : undefined,
  }))
}
