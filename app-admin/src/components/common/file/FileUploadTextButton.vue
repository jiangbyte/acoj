<script lang="ts" setup>
import { NButton, NProgress } from 'naive-ui'
import { useFileFetch } from '@/composables/v1'
import ImageCropModal from './ImageCropModal.vue'

const props = defineProps<{
  modelValue: string
  isImage?: boolean
  moltiple?: boolean
  buttontext?: string
  type?: 'primary' | 'default' | 'info' | 'success' | 'warning' | 'error'
  color?: string
  /** 图片大小上限（MB），默认 5 */
  maxSizeMb?: number
  /** 开启裁剪（头像建议开启） */
  enableCrop?: boolean
  /** 裁剪宽高比，默认 1 */
  cropAspect?: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', url: string): void
  (e: 'success', url: string, displayUrl?: string): void
  (e: 'error', error?: Error): void
}>()

const { uploadFile } = useFileFetch()
const inputRef = ref<HTMLInputElement | null>(null)
const uploadProgress = ref(0)
const isUploading = ref(false)
const cropShow = ref(false)
const cropSource = ref<File | null>(null)

const acceptTypes = computed(() => (props.isImage === false ? undefined : 'image/jpeg,image/png,image/gif,image/webp'))
const maxBytes = computed(() => (props.maxSizeMb ?? 5) * 1024 * 1024)

function openFileDialog() {
  if (isUploading.value)
    return
  inputRef.value?.click()
}

/**
 * 选文件：校验 →（可选）1:1 裁剪 → 上传
 */
async function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const files = input.files
  if (!files || files.length === 0)
    return

  const raw = files[0]
  input.value = ''

  if (props.isImage !== false && !raw.type.startsWith('image/')) {
    window.$message?.error('请上传图片文件')
    emit('error', new Error('invalid type'))
    return
  }
  if (raw.size > maxBytes.value) {
    window.$message?.error(`图片大小不能超过 ${props.maxSizeMb ?? 5}MB`)
    emit('error', new Error('file too large'))
    return
  }

  if (props.enableCrop) {
    cropSource.value = raw
    cropShow.value = true
    return
  }

  await doUpload(raw)
}

async function onCropConfirm(file: File) {
  cropSource.value = null
  await doUpload(file)
}

function onCropCancel() {
  cropSource.value = null
}

/**
 * 实际上传并回传对象名 / 访问 URL
 */
async function doUpload(raw: File) {
  isUploading.value = true
  uploadProgress.value = 20
  try {
    const formData = new FormData()
    formData.append('file', raw)
    const { data, success, message: errMsg } = await uploadFile(formData)
    uploadProgress.value = 100

    if (!success || (!data?.filename && !data?.url)) {
      throw new Error(errMsg || '上传失败：未返回文件信息')
    }

    const objectName = data.filename || data.url
    const displayUrl = data.url || objectName
    emit('update:modelValue', objectName)
    emit('success', objectName, displayUrl)
    window.$message?.success('上传成功')
  }
  catch (error) {
    console.error('Upload failed:', error)
    window.$message?.error(error instanceof Error ? error.message : '上传失败')
    emit('error', error instanceof Error ? error : new Error('Upload failed'))
  }
  finally {
    isUploading.value = false
    uploadProgress.value = 0
  }
}
</script>

<template>
  <div class="inline-flex flex-col items-stretch gap-2">
    <input
      ref="inputRef"
      type="file"
      style="display: none"
      :accept="acceptTypes"
      :multiple="!!moltiple"
      @change="onFileChange"
    >
    <NButton
      text
      :type="type || 'primary'"
      :color="color"
      :loading="isUploading"
      :disabled="isUploading"
      @click="openFileDialog"
    >
      {{ buttontext || '上传文件' }}
    </NButton>
    <NProgress
      v-if="isUploading"
      type="line"
      :percentage="uploadProgress"
      :height="4"
      :border-radius="2"
      class="w-full max-w-40"
      :show-indicator="false"
    />
    <ImageCropModal
      v-model:show="cropShow"
      :source-file="cropSource"
      :aspect-ratio="cropAspect || 1"
      @confirm="onCropConfirm"
      @cancel="onCropCancel"
    />
  </div>
</template>
