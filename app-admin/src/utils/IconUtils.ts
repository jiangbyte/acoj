import type { ButtonProps } from 'naive-ui'
import { NButton, NIcon, NPopconfirm } from 'naive-ui'
import { Icon } from '@iconify/vue/offline'

/**
 * 按图标名渲染 Naive Icon
 */
export function iconCreate(icon?: string, props?: import('naive-ui').IconProps) {
  if (!icon)
    return

  return h(NIcon, props, { default: () => h(Icon, { icon }) })
}

/**
 * 返回可用于组件 props 的图标渲染函数
 */
export function iconRender(icon?: string, props?: import('naive-ui').IconProps) {
  if (!icon)
    return

  return () => iconCreate(icon, props)
}

/** 表格操作列常用图标按钮类型 */
export type ActionIconKind =
  | 'edit'
  | 'detail'
  | 'delete'
  | 'permission'
  | 'assign'
  | 'role'
  | 'problems'
  | 'similarity'
  | 'testcase'
  | 'stats'
  | 'view'

const ACTION_ICON_PRESET: Record<ActionIconKind, { icon: string, type?: ButtonProps['type'], title: string }> = {
  edit: { icon: 'icon-park-outline:edit', type: 'primary', title: 'Edit' },
  detail: { icon: 'icon-park-outline:preview-open', title: 'Details' },
  delete: { icon: 'icon-park-outline:delete', type: 'error', title: 'Delete' },
  permission: { icon: 'icon-park-outline:permissions', type: 'warning', title: 'Bind Permissions' },
  assign: { icon: 'icon-park-outline:setting-config', type: 'warning', title: 'Resource Access' },
  role: { icon: 'icon-park-outline:people', type: 'warning', title: 'Role Assignment' },
  problems: { icon: 'icon-park-outline:list-view', type: 'info', title: 'Problem Management' },
  similarity: { icon: 'icon-park-outline:branch-one', type: 'warning', title: 'Similarity Check' },
  testcase: { icon: 'icon-park-outline:file-code', type: 'info', title: 'Test Cases' },
  stats: { icon: 'icon-park-outline:chart-histogram', type: 'primary', title: 'Statistics' },
  view: { icon: 'icon-park-outline:preview-open', type: 'primary', title: 'View Details' },
}

/**
 * 渲染圆形图标操作按钮（title 作为原生悬停提示）
 */
export function actionIconBtn(options: {
  kind: ActionIconKind
  title?: string
  type?: ButtonProps['type']
  disabled?: boolean
  onClick?: (e: MouseEvent) => void
}) {
  // 1. 合并预设与入参，保证图标/类型/文案一致
  const preset = ACTION_ICON_PRESET[options.kind]
  const title = options.title ?? preset.title

  // 2. 输出仅含图标的圆形按钮，节省操作列宽度
  return h(NButton, {
    size: 'small',
    circle: true,
    title,
    type: options.type ?? preset.type,
    disabled: options.disabled,
    onClick: options.onClick,
  }, {
    icon: () => iconCreate(preset.icon),
  })
}

/**
 * 删除确认：图标按钮 + Popconfirm；disabled 时仅渲染禁用按钮，不弹确认。
 */
export function actionDeleteBtn(
  onConfirm: () => void | Promise<void>,
  title = 'Delete',
  options?: { disabled?: boolean },
) {
  if (options?.disabled) {
    return actionIconBtn({ kind: 'delete', title, disabled: true })
  }
  return h(NPopconfirm, {
    onPositiveClick: onConfirm,
  }, {
    default: () => 'Confirm delete',
    trigger: () => actionIconBtn({ kind: 'delete', title }),
  })
}
