<!-- Author: Charlie -->
<!-- 私有桶预签名 URL 常被 CSP 拦截；有 fileId 时走同源 download → blob 预览 -->

<script setup lang="ts">
import { fileApi } from '@/api'
import { onBeforeUnmount, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    fileId?: string | number | null
    /** 同源或 data/blob 时可直接用；跨域外链仅作无 fileId 时的兜底 */
    src?: string | null
    alt?: string
    width?: number | string
    height?: number | string
    objectFit?: 'fill' | 'contain' | 'cover' | 'none' | 'scale-down'
  }>(),
  {
    alt: '',
    objectFit: 'cover',
  },
)

const previewSrc = ref('')
const loading = ref(false)

function isSafeDirectSrc(value?: string | null) {
  const raw = String(value || '').trim()
  if (!raw) {
    return false
  }
  if (/^(data:|blob:)/i.test(raw)) {
    return true
  }
  try {
    return new URL(raw, window.location.href).origin === window.location.origin
  } catch {
    return false
  }
}

function revokePreview() {
  if (previewSrc.value.startsWith('blob:')) {
    URL.revokeObjectURL(previewSrc.value)
  }
  previewSrc.value = ''
}

async function loadPreview() {
  revokePreview()
  const id = String(props.fileId ?? '').trim()
  if (id) {
    loading.value = true
    try {
      const response = await fileApi.download(id)
      const blob = response.data
      if (blob instanceof Blob && blob.size > 0) {
        previewSrc.value = URL.createObjectURL(blob)
        return
      }
    } catch {
      // 回退直链
    } finally {
      loading.value = false
    }
  }
  if (isSafeDirectSrc(props.src)) {
    previewSrc.value = String(props.src)
  }
}

watch(
  () => [props.fileId, props.src] as const,
  () => {
    void loadPreview()
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  revokePreview()
})
</script>

<template>
  <NSpin
    :show="loading"
    size="small"
  >
    <NImage
      v-if="previewSrc"
      :src="previewSrc"
      :alt="alt"
      :width="width"
      :height="height"
      :object-fit="objectFit"
    />
    <span
      v-else
      class="safe-storage-image__empty"
    >-</span>
  </NSpin>
</template>

<style scoped>
.safe-storage-image__empty {
  color: var(--text-color-3);
  font-size: 12px;
}
</style>
