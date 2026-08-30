/** Author: Charlie */

/** 资源页面布局选项（与路由 helper 约定：null/default 为后台布局，fullscreen 为全屏页）。 */
export const RESOURCE_LAYOUT_OPTIONS = [
  { label: '默认布局', value: 'default' },
  { label: '全屏布局', value: 'fullscreen' },
] as const

export type ResourceLayoutValue = (typeof RESOURCE_LAYOUT_OPTIONS)[number]['value']

export function normalizeResourceLayout(layout: string | null | undefined): ResourceLayoutValue {
  const value = String(layout ?? '').trim()
  return value === 'fullscreen' ? 'fullscreen' : 'default'
}

export function serializeResourceLayout(layout: string | null | undefined): string | null {
  const value = String(layout ?? '').trim()
  if (!value || value === 'default') {
    return null
  }
  return value
}

export function resourceLayoutLabel(layout: string | null | undefined): string {
  return RESOURCE_LAYOUT_OPTIONS.find((item) => item.value === normalizeResourceLayout(layout))?.label
    ?? String(layout ?? '—')
}
