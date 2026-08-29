<!-- <script lang="ts" setup>
import { NUpload } from 'naive-ui'
import type { UploadCustomRequestOptions, UploadFileInfo } from 'naive-ui'
import { useFileFetch } from '@/composables/v1'

const props = defineProps<{
  modelValue?: string // v-model 绑定的值
  isImage?: boolean // 是否为图片上传 (made optional since not used)
  moltiple?: boolean // 是否为多文件上传 (made optional since not used)
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
    :multiple="props.moltiple"
    :default-file-list="fileList"
    :max="props.moltiple ? 10 : 1"
    :custom-request="FileUploadRequest"
    list-type="image-card"
    @finish="handleFinish"
    @error="handleError"
  >
    <NIcon v-if="!props.modelValue">
      <IconParkOutlineAddPicture />
    </NIcon>
  </NUpload>
</template>

<style>

</style> -->

<script lang="ts" setup>
import { NButton, NIcon, NImage, NProgress, NSpace, NUpload } from 'naive-ui'
import type { UploadCustomRequestOptions, UploadFileInfo } from 'naive-ui'
import { useFileFetch } from '@/composables/v1'

const props = defineProps<{
  modelValue?: string
  isImage?: boolean
  moltiple?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', url: string): void
  (e: 'success', url: string): void
  (e: 'error', error?: Error): void
}>()

const { uploadFile } = useFileFetch()
const fileUrl = ref(props.modelValue)
const fileList = ref<UploadFileInfo[]>([])
const uploadProgress = ref<Record<string, number>>({})
const uploadingFiles = ref<Set<string>>(new Set())

// 监听 modelValue 变化，构建回显数据（库中为对象名，接口回显可能是可访问 URL）
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    const isHttp = newVal.startsWith('http://') || newVal.startsWith('https://')
    const displayUrl = isHttp
      ? newVal
      : (fileUrl.value && String(fileUrl.value).includes(newVal) ? fileUrl.value : newVal)
    fileUrl.value = displayUrl
    fileList.value = [{
      id: Date.now().toString(),
      name: newVal.split('/').pop() || 'file',
      status: 'finished',
      url: displayUrl,
    }]
  }
  else {
    fileList.value = []
    fileUrl.value = undefined
  }
}, { immediate: true })

// 模拟上传进度
function simulateUploadProgress(fileId: string, onProgress?: (percent: number) => void) {
  return new Promise<void>((resolve) => {
    let progress = 0
    const interval = setInterval(() => {
      progress += Math.random() * 15
      if (progress >= 100) {
        progress = 100
        clearInterval(interval)
        uploadProgress.value[fileId] = progress
        if (onProgress)
          onProgress(progress)
        resolve()
      }
      else {
        uploadProgress.value[fileId] = progress
        if (onProgress)
          onProgress(progress)
      }
    }, 200)
  })
}

async function FileUploadRequest({
  file,
  onFinish,
  onError,
  onProgress,
}: UploadCustomRequestOptions) {
  const fileId = file.id || Date.now().toString()
  uploadingFiles.value.add(fileId)

  try {
    // 开始模拟上传进度
    await simulateUploadProgress(fileId, (percent) => {
      if (onProgress) {
        onProgress({ percent })
      }
    })

    // 实际文件上传
    const formData = new FormData()
    formData.append('file', file.file as File)
    const { data, success } = await uploadFile(formData)

    if (success && (data?.filename || data?.url)) {
      window.$message?.success('上传成功')
      const objectName = data.filename || data.url
      fileUrl.value = data.url || objectName
      fileList.value = [{
        id: Date.now().toString(),
        name: objectName.split('/').pop() || 'file',
        status: 'finished',
        url: fileUrl.value,
      }]
      emit('update:modelValue', objectName)
      emit('success', objectName)
      onFinish()
    }
    else {
      throw new Error('上传失败：未返回有效的URL')
    }
  }
  catch (error) {
    console.error('Upload failed:', error)
    const errorMsg = error instanceof Error ? error : new Error('Upload failed')
    window.$message?.error(`上传失败：${errorMsg.message}`)
    emit('error', errorMsg)
    onError()
  }
  finally {
    uploadingFiles.value.delete(fileId)
    delete uploadProgress.value[fileId]
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
  emit('error', new Error('Upload failed'))
}

function handleRemove(file: UploadFileInfo) {
  fileList.value = fileList.value.filter(item => item.id !== file.id)
  if (fileUrl.value === file.url) {
    fileUrl.value = undefined
    emit('update:modelValue', '')
  }
}

// 自定义渲染文件列表
function renderFileList({ fileList }: { fileList: UploadFileInfo[] }) {
  return h('div', { class: 'flex flex-wrap gap-4 p-4' }, fileList.map((file) => {
    const isUploading = uploadingFiles.value.has(file.id!)
    const progress = uploadProgress.value[file.id!] || 0

    return h('div', {
      key: file.id,
      class: 'relative border rounded-lg p-3 w-48 bg-white shadow-sm',
    }, [
      // 文件信息
      h('div', { class: 'flex items-center gap-2 mb-2' }, [
        h('div', { class: 'flex-1 min-w-0' }, [
          h('div', {
            class: 'text-sm font-medium truncate',
            title: file.name,
          }, file.name),
          h('div', { class: 'text-xs text-gray-500' }, file.status === 'finished'
            ? '上传完成'
            : file.status === 'error'
              ? '上传失败'
              : isUploading ? '上传中...' : '等待上传'),
        ]),
      ]),

      // 进度条
      isUploading && h(NProgress, {
        type: 'line',
        percentage: progress,
        height: 6,
        class: 'mb-2',
      }),

      // 图片预览或文件图标
      props.isImage && file.url
        ? h(NImage, {
            src: file.url,
            width: '100%',
            height: '80px',
            class: 'object-cover rounded mb-2',
            previewDisabled: true,
          })
        : h('div', {
            class: 'w-full h-20 bg-gray-100 rounded flex items-center justify-center mb-2',
          }, [
            h(NIcon, { size: 24, class: 'text-gray-400' }, () => '📄'),
          ]),

      // 操作按钮
      h(NSpace, { class: 'flex justify-end' }, [
        file.status === 'error' && h(NButton, {
          size: 'small',
          type: 'primary',
          onClick: () => {
            // 重新上传逻辑
            console.log('Retry upload:', file)
          },
        }, () => '重试'),

        h(NButton, {
          size: 'small',
          type: 'error',
          onClick: () => handleRemove(file),
        }, () => '删除'),
      ]),
    ])
  }))
}
</script>

<template>
  <div class="upload-container">
    <NUpload
      :multiple="props.moltiple"
      :default-file-list="fileList"
      :max="props.moltiple ? 10 : 1"
      :custom-request="FileUploadRequest"
      :list-type="isImage ? 'image-card' : 'text'"
      :show-file-list="true"
      @finish="handleFinish"
      @error="handleError"
      @remove="handleRemove"
    >
      <template v-if="isImage">
        <div class="upload-trigger image-trigger">
          <NIcon size="24" class="text-gray-400">
            <IconParkOutlineAddPicture />
          </NIcon>
          <div class="mt-2 text-sm text-gray-500">
            点击上传图片
          </div>
        </div>
      </template>
      <template v-else>
        <div class="upload-trigger file-trigger">
          <NIcon size="24" class="text-gray-400">
            <IconParkOutlineUpload />
          </NIcon>
          <div class="mt-2 text-sm text-gray-500">
            点击上传文件
          </div>
        </div>
      </template>

      <!-- 自定义文件列表渲染 -->
      <template #file-list="{ fileList }">
        <render-file-list :file-list="fileList" />
      </template>
    </NUpload>

    <!-- 上传状态概览 -->
    <!-- <div v-if="uploadingFiles.size > 0" class="upload-status mt-4 p-3 bg-blue-50 rounded-lg">
      <div class="text-sm font-medium text-blue-800 mb-2">
        上传中 ({{ uploadingFiles.size }} 个文件)
      </div>
      <div v-for="fileId in Array.from(uploadingFiles)" :key="fileId" class="mb-2">
        <div class="flex justify-between text-xs text-blue-700 mb-1">
          <span>文件 {{ fileId.slice(-4) }}</span>
          <span>{{ Math.round(uploadProgress[fileId] || 0) }}%</span>
        </div>
        <NProgress
          type="line"
          :percentage="uploadProgress[fileId] || 0"
          height="4px"
          status="success"
        />
      </div>
    </div> -->
  </div>
</template>

<style scoped>
.upload-container {
  @apply w-full;
}

.upload-trigger {
  @apply flex flex-col items-center justify-center p-8 border-2 border-dashed border-gray-300 rounded-lg cursor-pointer transition-colors duration-200;
}

.upload-trigger:hover {
  @apply border-blue-500 bg-blue-50;
}

.image-trigger {
  @apply w-32 h-32;
}

.file-trigger {
  @apply w-full py-12;
}

.upload-status {
  @apply border border-blue-200;
}
</style>
