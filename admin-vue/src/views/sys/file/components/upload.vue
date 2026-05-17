<template>
  <a-modal
    :open="visible"
    title="文件上传"
    :width="520"
    :confirm-loading="uploading"
    destroy-on-close
    @ok="handleUpload"
    @cancel="handleClose"
  >
    <a-form layout="vertical" class="mt-2">
      <a-form-item label="存储引擎">
        <DictSelect
          v-model="engine"
          type-code="FILE_ENGINE"
          placeholder="选择存储引擎（默认使用系统配置）"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="选择文件">
        <a-upload-dragger
          :before-upload="beforeUpload"
          :file-list="fileList"
          :max-count="1"
          @remove="fileList = []"
        >
          <p class="ant-upload-drag-icon">
            <InboxOutlined />
          </p>
          <p class="ant-upload-text">单击或拖动文件到此区域进行上传</p>
          <p class="ant-upload-hint">支持单个文件上传</p>
        </a-upload-dragger>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { InboxOutlined } from '@ant-design/icons-vue'
import { useUploader } from 'alova/client'
import DictSelect from '@/components/form/DictSelect.vue'
import { uploadFile } from '@/api/file'

const emit = defineEmits<{ success: [] }>()

const visible = ref(false)
const uploading = ref(false)
const engine = ref<string | undefined>(undefined)
const fileList = ref<any[]>([])

const { upload, appendFiles, removeFiles } = useUploader(
  ({ file }) => uploadFile(file, engine.value),
  { limit: 1 }
)

async function beforeUpload(file: File) {
  removeFiles()
  fileList.value = [file as any]
  await appendFiles({ file, name: file.name })
  return false
}

async function handleUpload() {
  if (fileList.value.length === 0) return
  uploading.value = true
  await upload()
  emit('success')
  handleClose()
  uploading.value = false
}

function handleClose() {
  visible.value = false
  fileList.value = []
  engine.value = undefined
  removeFiles()
}

function doOpen() {
  visible.value = true
}

defineExpose({ doOpen })
</script>
