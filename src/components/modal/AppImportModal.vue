<template>
  <a-modal
    :open="open"
    title="导入数据"
    :width="520"
    :confirm-loading="uploading"
    destroy-on-close
    @ok="handleUpload"
    @cancel="handleClose"
  >
    <div v-if="templateText" class="mb-4 text-sm text-[var(--header-text,#000000d9)]">
      <span>{{ templateText }}</span>
      <a-button
        type="link"
        size="small"
        :loading="templateLoading"
        @click="emit('downloadTemplate')"
      >
        下载模板
      </a-button>
    </div>

    <a-upload-dragger
      ref="uploadRef"
      :before-upload="beforeUpload"
      :show-upload-list="true"
      :max-count="1"
      accept=".xls,.xlsx"
      :file-list="fileList"
      @remove="fileList = []"
    >
      <p class="ant-upload-drag-icon">
        <InboxOutlined />
      </p>
      <p class="ant-upload-text">点击或拖拽文件到此区域上传</p>
      <p class="ant-upload-hint">仅支持 .xls 或 .xlsx 格式</p>
    </a-upload-dragger>

    <div v-if="result" class="mt-4">
      <a-alert
        :type="result.success ? 'success' : 'warning'"
        show-icon
        :message="`${result.success ? '导入成功' : '导入完成'}`"
        :description="result.message"
      />
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { InboxOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

defineProps<{
  open: boolean
  templateText?: string
  templateLoading?: boolean
}>()

const emit = defineEmits<{
  close: []
  downloadTemplate: []
  upload: [file: File]
}>()

const uploading = ref(false)
const fileList = ref<any[]>([])
const result = ref<{ success: boolean; message: string } | null>(null)

function beforeUpload(file: File) {
  const isExcel = file.name.endsWith('.xls') || file.name.endsWith('.xlsx')
  if (!isExcel) {
    message.error('仅支持 .xls 或 .xlsx 格式')
    return false
  }
  fileList.value = [file]
  return false
}

async function handleUpload() {
  if (fileList.value.length === 0) {
    message.warning('请先选择文件')
    return
  }
  result.value = null
  uploading.value = true
  emit('upload', fileList.value[0])
}

function handleClose() {
  fileList.value = []
  result.value = null
  emit('close')
}

defineExpose({
  setResult: (r: { success: boolean; message: string }) => {
    result.value = r
    uploading.value = false
  },
})
</script>
