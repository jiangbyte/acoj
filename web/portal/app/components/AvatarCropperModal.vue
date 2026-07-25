<script setup lang="ts">
import { Cropper } from 'vue-advanced-cropper'
import 'vue-advanced-cropper/dist/style.css'

const props = defineProps<{
  open: boolean
  avatar?: string | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  uploaded: []
}>()

const cropperRef = ref<InstanceType<typeof Cropper> | null>(null)
const source = ref('')
const previewUrl = ref('')
const uploading = ref(false)

const modalOpen = computed({
  get: () => props.open,
  set: (v) => emit('update:open', v),
})

watch(modalOpen, (v) => {
  if (!v) resetSource()
})

function openFilePicker() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/jpeg,image/png,image/webp'
  input.onchange = () => {
    const file = input.files?.[0]
    if (!file) return
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      useToast().add({ title: '仅支持 JPG、PNG、WebP', color: 'error' })
      return
    }
    resetSource()
    source.value = URL.createObjectURL(file)
    updatePreview()
  }
  input.click()
}

function updatePreview() {
  requestAnimationFrame(() => {
    const canvas = cropperRef.value?.getResult?.()?.canvas
    previewUrl.value = canvas ? canvas.toDataURL('image/png') : ''
  })
}

function onCropperChange() {
  updatePreview()
}

async function handleUpload() {
  const canvas = cropperRef.value?.getResult?.()?.canvas
  if (!canvas) {
    useToast().add({ title: '请先选择头像图片', color: 'error' })
    return
  }

  uploading.value = true
  try {
    const blob = await new Promise<Blob | null>((resolve) =>
      canvas.toBlob(resolve, 'image/png', 0.92),
    )
    if (!blob) throw new Error('裁剪失败')
    const formData = new FormData()
    formData.append('file', new File([blob], 'avatar.png', { type: 'image/png' }))
    await useHttp().post('/api/v1/portal/user-center/avatar/upload', formData)
    useToast().add({ title: '头像已更新', color: 'success' })
    emit('uploaded')
    modalOpen.value = false
  } catch {
    useToast().add({ title: '上传失败', color: 'error' })
  } finally {
    uploading.value = false
  }
}

function resetSource() {
  if (source.value) URL.revokeObjectURL(source.value)
  source.value = ''
  previewUrl.value = ''
}
</script>

<template>
  <UModal v-model:open="modalOpen">
    <template #title>上传头像</template>

    <template #body>
      <div v-if="source" class="flex gap-4">
        <div class="flex-1 h-80 overflow-hidden rounded-lg bg-muted">
          <Cropper
            ref="cropperRef"
            :src="source"
            :stencil-props="{ aspectRatio: 1 }"
            :canvas="{ width: 320, height: 320 }"
            :style="{ height: '100%', width: '100%' }"
            @change="onCropperChange"
          />
        </div>
        <div class="w-44 flex flex-col items-center justify-center p-4">
          <div class="size-32 rounded-full overflow-hidden shadow-inner">
            <img v-if="previewUrl" :src="previewUrl" alt="" class="h-full w-full object-cover" />
          </div>
          <p class="mt-3 text-sm text-muted">实时预览</p>
        </div>
      </div>
      <div v-else class="min-h-80 flex flex-col items-center justify-center bg-muted rounded-lg">
        <UAvatar :src="avatar ?? undefined" size="2xl" icon="i-lucide-user" />
        <UButton color="primary" variant="outline" class="mt-4" @click="openFilePicker">
          选择图片
        </UButton>
        <p class="mt-2 text-xs text-muted">仅支持 JPG、PNG、WebP 图片</p>
      </div>
    </template>

    <template #footer>
      <div class="flex justify-between items-center w-full">
        <span class="text-xs text-muted">裁剪后以 PNG 格式上传</span>
        <div class="flex items-center gap-2">
          <UButton color="neutral" variant="ghost" @click="modalOpen = false">取消</UButton>
          <UButton v-if="source" color="neutral" variant="outline" @click="openFilePicker"
            >重新选择</UButton
          >
          <UButton :loading="uploading" @click="handleUpload">裁剪并上传</UButton>
        </div>
      </div>
    </template>
  </UModal>
</template>
