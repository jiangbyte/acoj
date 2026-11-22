import type { AvatarProps } from 'naive-ui'
import SafeAvatar from '@/components/common/media/SafeAvatar.vue'
import SafeImage from '@/components/common/media/SafeImage.vue'

/** 表格 / render 函数中安全渲染头像 */
export function renderSafeAvatar(props: {
  src?: string | null
  size?: AvatarProps['size']
  round?: boolean
  bordered?: boolean
  color?: string
  objectFit?: AvatarProps['objectFit']
  class?: string
}) {
  return h(SafeAvatar, props)
}

/** 表格 / render 函数中安全渲染封面图 */
export function renderSafeImage(props: {
  src?: string | null
  alt?: string
  width?: string | number
  height?: string | number
  objectFit?: 'fill' | 'contain' | 'cover' | 'none' | 'scale-down'
  iconSize?: number
  round?: boolean
  previewable?: boolean
  class?: string
}) {
  return h(SafeImage, props)
}
