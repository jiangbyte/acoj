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
  /** Maximum image size in MB; default is 5 */
  maxSizeMb?: number
  /** Enable cropping (recommended for avatars) */
  enableCrop?: boolean
  /** Crop aspect ratio; default is 1 */
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
 * Select a file, validate it, optionally crop to 1:1, then upload
 */
async function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const files = input.files
  if (!files || files.length === 0)
    return

  const raw = files[0]
  input.value = ''

  if (props.isImage !== false && !raw.type.startsWith('image/')) {
    window.$message?.error('Please upload an image')
    emit('error', new Error('invalid type'))
    return
  }
  if (raw.size > maxBytes.value) {
    window.$message?.error(`Image size cannot exceed ${props.maxSizeMb ?? 5}MB`)
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
 * Upload and return the object name or access URL
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
      throw new Error(errMsg || 'Upload failed: no file information returned')
    }

    const objectName = data.filename || data.url
    const displayUrl = data.url || objectName
    emit('update:modelValue', objectName)
    emit('success', objectName, displayUrl)
    window.$message?.success('Uploaded successfully')
  }
  catch (error) {
    console.error('Upload failed:', error)
    window.$message?.error(error instanceof Error ? error.message : 'Upload failed')
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
      {{ buttontext || 'Upload File' }}
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
