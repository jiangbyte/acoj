<script lang="ts" setup>
import { NUpload } from 'naive-ui'
import type { UploadCustomRequestOptions, UploadFileInfo } from 'naive-ui'
import { useDataProblemFetch } from '@/composables/v1'
import { watch } from 'vue'

const props = defineProps<{
  modelValue: string // v-model 绑定的值
  buttontext?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', url: string): void
  (e: 'success', url: string): void
  (e: 'error', error?: Error): void
}>()

const isLoading = ref(false)
const { dataProblemImport } = useDataProblemFetch()
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
    isLoading.value = true
    formData.append('file', file.file as File)
    window.$message.warning('操作中，请稍等')
    const { success } = await dataProblemImport(formData)

    if (success) {
      window.$message.success('导入成功')
      emit('success', 'OK')
    }

    // if (!data?.url) {
    //   throw new Error('No URL returned from upload')
    // }

    isLoading.value = false
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
    :multiple="false"
    :show-file-list="false"
    :custom-request="FileUploadRequest"
    @finish="handleFinish"
    @error="handleError"
  >
    <n-button type="primary" :loading="isLoading">
      {{ props.buttontext }}
    </n-button>
  </NUpload>
</template>

<style>

</style>
