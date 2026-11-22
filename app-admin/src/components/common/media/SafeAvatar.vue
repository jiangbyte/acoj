<script lang="ts" setup>
import type { AvatarProps } from 'naive-ui'
import { useSafeMediaSrc } from '@/composables/useSafeMediaSrc'

defineOptions({ name: 'SafeAvatar' })

const props = withDefaults(defineProps<{
  src?: string | null
  size?: AvatarProps['size']
  round?: boolean
  bordered?: boolean
  color?: string
  objectFit?: AvatarProps['objectFit']
  fallbackSrc?: string
}>(), {
  round: false,
})

const { displaySrc } = useSafeMediaSrc(() => props.src)

/** 根据 size 估算占位图标边长 */
const iconSize = computed(() => {
  const size = props.size
  if (typeof size === 'number')
    return Math.max(14, Math.round(size * 0.55))
  if (size === 'small')
    return 14
  if (size === 'large')
    return 22
  return 18
})
</script>

<template>
  <!-- 无图：默认槽展示用户图标；有图：失败/加载中走 fallback、placeholder -->
  <NAvatar
    :src="displaySrc"
    :size="size"
    :round="round"
    :bordered="bordered"
    :color="color"
    :object-fit="objectFit"
    :fallback-src="fallbackSrc"
  >
    <template v-if="!displaySrc">
      <NIcon :size="iconSize">
        <IconParkOutlineUser />
      </NIcon>
    </template>
    <template v-if="displaySrc" #fallback>
      <NIcon :size="iconSize">
        <IconParkOutlineUser />
      </NIcon>
    </template>
    <template v-if="displaySrc" #placeholder>
      <NIcon :size="iconSize">
        <IconParkOutlineUser />
      </NIcon>
    </template>
  </NAvatar>
</template>
