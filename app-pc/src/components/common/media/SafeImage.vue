<script lang="ts" setup>
import { useSafeMediaSrc } from '@/composables/useSafeMediaSrc'

defineOptions({ inheritAttrs: false, name: 'SafeImage' })

const props = withDefaults(defineProps<{
  src?: string | null
  alt?: string
  width?: string | number
  height?: string | number
  objectFit?: 'fill' | 'contain' | 'cover' | 'none' | 'scale-down'
  /** 占位图标边长 */
  iconSize?: number
  round?: boolean
  /** 加载成功后使用 NImage，支持点击预览 */
  previewable?: boolean
}>(), {
  alt: '',
  objectFit: 'cover',
  iconSize: 28,
  round: false,
  previewable: false,
})

const attrs = useAttrs()
const { displaySrc } = useSafeMediaSrc(() => props.src)

const sizeStyle = computed(() => {
  const toCss = (value?: string | number) => {
    if (value == null || value === '')
      return undefined
    return typeof value === 'number' ? `${value}px` : value
  }
  return {
    width: toCss(props.width),
    height: toCss(props.height),
  }
})

/** class/style 已单独合并，其余透传（含点击事件） */
const forwardAttrs = computed(() => {
  const { class: _c, style: _s, ...rest } = attrs as Record<string, unknown>
  return rest
})
</script>

<template>
  <div
    class="safe-image"
    :class="[
      round ? 'safe-image--round' : '',
      attrs.class,
    ]"
    :style="[sizeStyle, attrs.style as Record<string, string> | undefined]"
    v-bind="forwardAttrs"
  >
    <!-- 未就绪：图标占位，避免破图 -->
    <div v-if="!displaySrc" class="safe-image__placeholder">
      <NIcon :size="iconSize" :depth="3">
        <IconParkOutlineAddPicture />
      </NIcon>
    </div>
    <!-- 预览模式：加载成功后交给 NImage -->
    <NImage
      v-else-if="previewable"
      :src="displaySrc"
      :width="width"
      :height="height"
      :object-fit="objectFit"
      :alt="alt"
      class="safe-image__media"
    />
    <!-- 普通模式：成功后替换为正式图片 -->
    <img
      v-else
      :src="displaySrc"
      :alt="alt"
      class="safe-image__media"
      :style="{ objectFit }"
    >
  </div>
</template>

<style scoped>
.safe-image {
  position: relative;
  display: grid;
  overflow: hidden;
  background: var(--n-color-embedded, rgba(0, 0, 0, 0.04));
  vertical-align: middle;
  box-sizing: border-box;
  place-items: stretch;
}

.safe-image--round {
  border-radius: 9999px;
}

.safe-image > * {
  grid-area: 1 / 1;
  width: 100%;
  height: 100%;
}

.safe-image__placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 4rem;
  color: var(--n-text-color-3, rgba(0, 0, 0, 0.35));
}

.safe-image__media {
  display: block;
  object-fit: cover;
}
</style>
