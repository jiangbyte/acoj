<script lang="ts" setup>
import { NButton, NModal, NSlider, NSpace, NText } from 'naive-ui'

const props = withDefaults(defineProps<{
  show: boolean
  /** 待裁剪的本地图片 */
  sourceFile: File | null
  /** 宽高比，默认 1:1 */
  aspectRatio?: number
  /** 输出边长（正方形），默认 512 */
  outputSize?: number
}>(), {
  aspectRatio: 1,
  outputSize: 512,
})

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'confirm', file: File): void
  (e: 'cancel'): void
}>()

const viewportRef = ref<HTMLDivElement | null>(null)
const imgRef = ref<HTMLImageElement | null>(null)
const objectUrl = ref('')
const naturalW = ref(0)
const naturalH = ref(0)
const scale = ref(1)
const minScale = ref(1)
const offsetX = ref(0)
const offsetY = ref(0)
const dragging = ref(false)
const dragStart = ref({ x: 0, y: 0, ox: 0, oy: 0 })
const confirming = ref(false)

const viewportSize = 320

watch(() => [props.show, props.sourceFile] as const, async ([show, file]) => {
  if (!show || !file) {
    revokeUrl()
    return
  }
  revokeUrl()
  objectUrl.value = URL.createObjectURL(file)
  await nextTick()
  await waitImage()
  resetTransform()
})

onUnmounted(() => revokeUrl())

function revokeUrl() {
  if (objectUrl.value) {
    URL.revokeObjectURL(objectUrl.value)
    objectUrl.value = ''
  }
}

function waitImage() {
  return new Promise<void>((resolve) => {
    const img = imgRef.value
    if (!img) {
      resolve()
      return
    }
    if (img.complete && img.naturalWidth) {
      naturalW.value = img.naturalWidth
      naturalH.value = img.naturalHeight
      resolve()
      return
    }
    img.onload = () => {
      naturalW.value = img.naturalWidth
      naturalH.value = img.naturalHeight
      resolve()
    }
  })
}

/** 初始缩放：短边铺满正方形视口 */
function resetTransform() {
  if (!naturalW.value || !naturalH.value)
    return
  const fit = viewportSize / Math.min(naturalW.value, naturalH.value)
  minScale.value = fit
  scale.value = fit
  offsetX.value = (viewportSize - naturalW.value * fit) / 2
  offsetY.value = (viewportSize - naturalH.value * fit) / 2
}

function clampOffset() {
  const w = naturalW.value * scale.value
  const h = naturalH.value * scale.value
  // 图片必须盖住整个正方形裁剪区
  if (w >= viewportSize) {
    offsetX.value = Math.min(0, Math.max(viewportSize - w, offsetX.value))
  }
  else {
    offsetX.value = (viewportSize - w) / 2
  }
  if (h >= viewportSize) {
    offsetY.value = Math.min(0, Math.max(viewportSize - h, offsetY.value))
  }
  else {
    offsetY.value = (viewportSize - h) / 2
  }
}

watch(scale, () => {
  clampOffset()
})

function onPointerDown(e: PointerEvent) {
  dragging.value = true
  dragStart.value = { x: e.clientX, y: e.clientY, ox: offsetX.value, oy: offsetY.value }
  ;(e.currentTarget as HTMLElement).setPointerCapture(e.pointerId)
}

function onPointerMove(e: PointerEvent) {
  if (!dragging.value)
    return
  offsetX.value = dragStart.value.ox + (e.clientX - dragStart.value.x)
  offsetY.value = dragStart.value.oy + (e.clientY - dragStart.value.y)
  clampOffset()
}

function onPointerUp() {
  dragging.value = false
}

function onWheel(e: WheelEvent) {
  e.preventDefault()
  const delta = e.deltaY > 0 ? -0.05 : 0.05
  scale.value = Math.min(minScale.value * 4, Math.max(minScale.value, scale.value + delta * minScale.value))
}

function close() {
  emit('update:show', false)
  emit('cancel')
}

/**
 * 按当前视口导出 1:1 图片文件
 */
async function confirmCrop() {
  if (!imgRef.value || !naturalW.value)
    return
  confirming.value = true
  try {
    const size = props.outputSize
    const canvas = document.createElement('canvas')
    canvas.width = size
    canvas.height = size
    const ctx = canvas.getContext('2d')
    if (!ctx)
      throw new Error('canvas unavailable')

    // 视口坐标 → 原图像素
    const sx = -offsetX.value / scale.value
    const sy = -offsetY.value / scale.value
    const sw = viewportSize / scale.value
    const sh = viewportSize / scale.value

    ctx.fillStyle = '#ffffff'
    ctx.fillRect(0, 0, size, size)
    ctx.drawImage(imgRef.value, sx, sy, sw, sh, 0, 0, size, size)

    const blob = await new Promise<Blob | null>(resolve => canvas.toBlob(resolve, 'image/jpeg', 0.92))
    if (!blob)
      throw new Error('导出失败')

    const base = (props.sourceFile?.name || 'avatar').replace(/\.[^.]+$/, '')
    const file = new File([blob], `${base}-crop.jpg`, { type: 'image/jpeg' })
    emit('confirm', file)
    emit('update:show', false)
  }
  catch (error) {
    console.error(error)
    window.$message?.error(error instanceof Error ? error.message : '裁剪失败')
  }
  finally {
    confirming.value = false
  }
}
</script>

<template>
  <NModal
    :show="show"
    preset="card"
    title="裁剪头像（1:1）"
    style="width: 420px"
    :mask-closable="false"
    @update:show="(v) => { if (!v) close() }"
  >
    <NSpace vertical :size="16">
      <NText depth="3">
        拖动调整位置，滚轮或滑块缩放；确认后按 1:1 裁剪上传
      </NText>
      <div
        ref="viewportRef"
        class="crop-viewport"
        :style="{ width: `${viewportSize}px`, height: `${viewportSize}px` }"
        @pointerdown="onPointerDown"
        @pointermove="onPointerMove"
        @pointerup="onPointerUp"
        @pointercancel="onPointerUp"
        @wheel.prevent="onWheel"
      >
        <img
          v-if="objectUrl"
          ref="imgRef"
          :src="objectUrl"
          class="crop-image"
          draggable="false"
          :style="{
            width: `${naturalW * scale}px`,
            height: `${naturalH * scale}px`,
            transform: `translate(${offsetX}px, ${offsetY}px)`,
          }"
        >
        <div class="crop-mask" />
      </div>
      <div class="px-2">
        <NText depth="3" class="mb-1 block">
          缩放
        </NText>
        <NSlider
          v-model:value="scale"
          :min="minScale"
          :max="minScale * 4"
          :step="minScale * 0.01"
        />
      </div>
    </NSpace>
    <template #footer>
      <NSpace justify="end">
        <NButton @click="close">
          取消
        </NButton>
        <NButton type="primary" :loading="confirming" @click="confirmCrop">
          确认裁剪
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>

<style scoped>
.crop-viewport {
  position: relative;
  margin: 0 auto;
  overflow: hidden;
  border-radius: 8px;
  background: #111;
  touch-action: none;
  cursor: grab;
  user-select: none;
}
.crop-viewport:active {
  cursor: grabbing;
}
.crop-image {
  position: absolute;
  left: 0;
  top: 0;
  max-width: none;
  pointer-events: none;
}
.crop-mask {
  pointer-events: none;
  position: absolute;
  inset: 0;
  box-shadow: inset 0 0 0 2px rgba(24, 160, 88, 0.9);
  border-radius: 8px;
}
</style>
