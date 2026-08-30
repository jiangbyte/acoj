/** Author: Charlie */

/** 外观偏好默认值 */
export type FormStyle = 'drawer' | 'modal'

export const APPEARANCE_DEFAULTS = {
  primaryColor: '#1677FF',
  roundedStyle: false,
  formStyle: 'drawer' as FormStyle,
  showWatermark: true,
  showBreadcrumb: true,
  showTabbar: true,
  accordionMenu: true,
  grayMode: false,
} as const

/** 圆角：关 = 0（方正稳重）；开 = 6px */
export const BORDER_RADIUS_ON = '6px'
export const BORDER_RADIUS_OFF = '0'

/** 主题色预设 */
export const THEME_COLOR_PRESETS: { key: string; color: string }[] = [
  { key: '薄暮', color: '#F5222D' },
  { key: '火山', color: '#FA541C' },
  { key: '胭脂粉', color: '#EB2F96' },
  { key: '日暮', color: '#FAAD14' },
  { key: '明青', color: '#13C2C2' },
  { key: '极光绿', color: '#52C41A' },
  { key: '深绿', color: '#009688' },
  { key: '拂晓蓝（默认）', color: '#1677FF' },
  { key: '极客蓝', color: '#2F54EB' },
  { key: '酱紫', color: '#722ED1' },
  { key: '主题黑', color: '#001529' },
]
