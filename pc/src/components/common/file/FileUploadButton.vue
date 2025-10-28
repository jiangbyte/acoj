<!-- <script lang="ts" setup>
import { NUpload } from 'naive-ui'
import type { UploadCustomRequestOptions, UploadFileInfo } from 'naive-ui'
import { useFileFetch } from '@/composables/v1'
import { watch } from 'vue'

const props = defineProps<{
  modelValue: string // v-model 绑定的值
  isImage?: boolean // 是否为图片上传 (made optional since not used)
  moltiple?: boolean // 是否为多文件上传 (made optional since not used)
  buttontext?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', url: string): void
  (e: 'success', url: string): void
  (e: 'error', error?: Error): void
}>()

const { uploadFile } = useFileFetch()
const fileUrl = ref(props.modelValue) // Initialize with modelValue
const fileList = ref<UploadFileInfo[]>([])

// 监听 modelValue 变化，构建回显数据
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    fileList.value = [{
      id: Date.now().toString(),
      name: newVal.split('/').pop() || 'file',
      status: 'finished',
      url: newVal,
    }]
  }
  else {
    fileList.value = []
  }
}, { immediate: true })

async function FileUploadRequest({
  file,
  onFinish,
  onError,
}: UploadCustomRequestOptions) {
  try {
    const formData = new FormData()
    formData.append('file', file.file as File)
    const { data, success } = await uploadFile(formData)
    console.log(data)

    if (success) {
      window.$message.success('上传成功')
    }

    if (!data?.url) {
      throw new Error('No URL returned from upload')
    }

    fileUrl.value = data.url
    emit('update:modelValue', data.url)
    emit('success', data.url)
    onFinish()
  }
  catch (error) {
    console.error('Upload failed:', error)
    emit('error', error instanceof Error ? error : new Error('Upload failed'))
    onError()
  }
}

function handleFinish({
  file,
  event,
}: {
  file: UploadFileInfo
  event?: ProgressEvent
}) {
  const s = { file, event }
//   console.log('Upload finished:', file, event)
}

function handleError({
  file,
  event,
}: {
  file: UploadFileInfo
  event?: ProgressEvent
}) {
  const s = { file, event }
  //   console.error('Upload error:', file, event)
  emit('error', new Error('Upload failed'))
}
</script>

<template>
  <NUpload
    v-if="isImage"
    :multiple="false"
    :show-file-list="false"
    :custom-request="FileUploadRequest"
    @finish="handleFinish"
    @error="handleError"
  >
    <n-button type="primary">
      {{ props.buttontext }}
    </n-button>
  </NUpload>
</template>

<style>

</style> -->

<script lang="ts" setup>
import { NButton, NProgress, NUpload, useMessage } from 'naive-ui'
import type { UploadCustomRequestOptions, UploadFileInfo } from 'naive-ui'
import { useFileFetch } from '@/composables/v1'

const props = defineProps<{
  modelValue: string // v-model 绑定的值
  isImage?: boolean // 是否为图片上传
  moltiple?: boolean // 是否为多文件上传
  buttontext?: string
  type?: 'primary' | 'default' | 'info' | 'success' | 'warning' | 'error'
  color?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', url: string): void
  (e: 'success', url: string): void
  (e: 'error', error?: Error): void
}>()

const { uploadFile } = useFileFetch()
const fileUrl = ref(props.modelValue)
const fileList = ref<UploadFileInfo[]>([])
const uploadProgress = ref<number>(0)
const isUploading = ref<boolean>(false)
const message = useMessage()

// 监听 modelValue 变化，构建回显数据
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    fileList.value = [{
      id: Date.now().toString(),
      name: newVal.split('/').pop() || 'file',
      status: 'finished',
      url: newVal,
    }]
  }
  else {
    fileList.value = []
  }
}, { immediate: true })

// 模拟进度更新
function simulateProgress() {
  uploadProgress.value = 0
  isUploading.value = true

  const interval = setInterval(() => {
    uploadProgress.value += Math.random() * 15
    if (uploadProgress.value >= 95) {
      clearInterval(interval)
    }
  }, 200)

  return interval
}

async function FileUploadRequest({
  file,
  onFinish,
  onError,
  onProgress,
}: UploadCustomRequestOptions) {
  const progressInterval = simulateProgress()

  try {
    const formData = new FormData()
    formData.append('file', file.file as File)

    // 模拟上传进度
    const progressTimer = setInterval(() => {
      if (uploadProgress.value < 95) {
        onProgress({ percent: uploadProgress.value })
      }
    }, 100)

    const { data, success } = await uploadFile(formData)

    // 完成进度
    clearInterval(progressInterval)
    clearInterval(progressTimer)
    uploadProgress.value = 100
    onProgress({ percent: 100 })

    // 延迟一下让进度条完成
    setTimeout(() => {
      isUploading.value = false
      uploadProgress.value = 0
    }, 500)

    if (success) {
      message.success('上传成功')
    }

    if (!data?.url) {
      throw new Error('No URL returned from upload')
    }

    fileUrl.value = data.url
    emit('update:modelValue', data.url)
    emit('success', data.url)
    onFinish()
  }
  catch (error) {
    clearInterval(progressInterval)
    isUploading.value = false
    uploadProgress.value = 0
    console.error('Upload failed:', error)
    message.error('上传失败')
    emit('error', error instanceof Error ? error : new Error('Upload failed'))
    onError()
  }
}

function handleFinish({
  file,
  event,
}: {
  file: UploadFileInfo
  event?: ProgressEvent
}) {
  console.log('Upload finished:', file, event)
}

function handleError({
  file,
  event,
}: {
  file: UploadFileInfo
  event?: ProgressEvent
}) {
  console.error('Upload error:', file, event)
  message.error('上传失败')
  emit('error', new Error('Upload failed'))
}

// 使用 h 函数创建自定义按钮组件
const CustomUploadButton = computed(() => {
  return h('div', { class: 'flex flex-col items-center gap-2' }, [
    h(
      NButton,
      {
        type: props.type,
        color: props.color,
        disabled: isUploading.value,
        class: 'w-full',
      },
      {
        default: () => props.buttontext || '上传文件',
      },
    ),
    // 上传时显示进度条
    isUploading.value
      ? h(
          NProgress,
          {
            type: 'line',
            percentage: uploadProgress.value,
            height: 4,
            borderRadius: 2,
            class: 'w-full max-w-40',
            showIndicator: false,
          },
        )
      : null,
  ])
})
</script>

<template>
  <div class="flex items-center">
    <NUpload
      :multiple="moltiple"
      :show-file-list="false"
      :custom-request="FileUploadRequest"
      :disabled="isUploading"
      @finish="handleFinish"
      @error="handleError"
    >
      <!-- 使用自定义渲染的按钮 -->
      <component :is="CustomUploadButton" />
    </NUpload>

    <!-- 显示上传状态 -->
    <!-- <div v-if="isUploading" class="mt-2 text-xs text-gray-500 text-center">
      上传中... {{ Math.round(uploadProgress) }}%
    </div> -->
  </div>
</template>

<style scoped>
.upload-container {
  @apply inline-block;
}

:deep(.n-upload-trigger) {
  @apply w-full;
}
</style>
