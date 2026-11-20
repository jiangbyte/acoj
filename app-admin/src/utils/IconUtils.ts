import { NIcon } from 'naive-ui'
import { Icon } from '@iconify/vue'
/**
 * 根据 icon 名称渲染图标
 */
export function iconCreate(icon?: string, props?: import('naive-ui').IconProps) {
  if (!icon)
    return

  return h(NIcon, props, { default: () => h(Icon, { icon }) })
}

/**
 *  根据 icon 名称渲染图标
 */
export function iconRender(icon?: string, props?: import('naive-ui').IconProps) {
  if (!icon)
    return

  return () => iconCreate(icon, props)
}
