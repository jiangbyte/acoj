import type { Component, VNodeChild } from 'vue'

import * as AntIcons from '@ant-design/icons-vue'
import { h } from 'vue'

export function getIconComponent(icon?: unknown) {
  if (!icon) {
    return undefined
  }
  const iconComponent = AntIcons[String(icon) as keyof typeof AntIcons]
  return iconComponent as Component | undefined
}

export function renderIcon(icon?: unknown): VNodeChild | undefined {
  const iconComponent = getIconComponent(icon)
  return iconComponent ? h(iconComponent) : undefined
}
