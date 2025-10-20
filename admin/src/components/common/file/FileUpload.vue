<script lang="ts" setup>
import { NUpload } from 'naive-ui'
import type { UploadCustomRequestOptions, UploadFileInfo } from 'naive-ui'
import { useFileFetch } from '@/composables/v1'
import { watch } from 'vue'

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

</style>
