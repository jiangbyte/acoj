import { h } from 'vue'
import * as Icons from '@ant-design/icons-vue'

/**
 * 根据图标名称解析为 ant-design-vue 图标组件对象。
 * 支持 "user"、"user-outlined"、"user-setting" 等格式。
 */
export function resolveIcon(name: string) {
  const pascal = name
    .replace(/[-_]/g, ' ')
    .replace(/\b\w/g, c => c.toUpperCase())
    .replace(/\s/g, '')
  const key = pascal.endsWith('Outlined') ? pascal : pascal + 'Outlined'
  return (Icons as any)[key] || null
}

/** 解析为 render 函数（用于 AMenu items 的 icon 字段） */
export function resolveIconRender(name?: string) {
  if (!name) return undefined
  const icon = resolveIcon(name)
  return icon ? () => h(icon) : undefined
}
