import { NIcon } from 'naive-ui'
import { Icon } from '@iconify/vue/offline'
/**
  * Implementation details.
 */
export function iconCreate(icon?: string, props?: import('naive-ui').IconProps) {
  if (!icon)
    return

  return h(NIcon, props, { default: () => h(Icon, { icon }) })
}

/**
  * Implementation details.
 */
export function iconRender(icon?: string, props?: import('naive-ui').IconProps) {
  if (!icon)
    return

  return () => iconCreate(icon, props)
}
