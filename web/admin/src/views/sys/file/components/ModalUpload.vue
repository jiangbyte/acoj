<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import FileUpload from '@/components/upload/FileUpload.vue'
import { createRequiredRule } from '@/utils'
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { dictList } from '@/utils/dict'

const emit = defineEmits<{
  saved: []
  uploaded: [file: any]
}>()

const fallbackStorageProviderOptions = [
  { label: '本地存储', value: 'local' },
  { label: 'MinIO', value: 'minio' },
  { label: 'Amazon S3', value: 's3' },
  { label: '阿里云 OSS', value: 'oss' },
]

const formRef = ref<FormInst | null>(null)
const uploadRef = ref<any>(null)

const storageProviderOptions = computed(() => {
  const options = dictList('STORAGE_PROVIDER')
  return options.length ? options : fallbackStorageProviderOptions
})

const defaultStorageProvider = computed(() => String(storageProviderOptions.value[0]?.value || 'local'))

const state = reactive({
  showModal: false,
  submitLoading: false,
  formModel: {
    file: null as File | null,
    storage_provider: '',
  },
})

const rules = computed<FormRules>(() => ({
  file: {
    validator: () => !!state.formModel.file,
    message: '请选择文件',
    trigger: 'change',
  },
  storage_provider: createRequiredRule('存储提供商', 'change'),
}))

function openModal() {
  resetForm()
  state.showModal = true
}

function resetForm() {
  state.formModel = {
    file: null,
    storage_provider: defaultStorageProvider.value,
  }
  uploadRef.value?.clear?.()
  formRef.value?.restoreValidation()
}

function handleUpdateShow(show: boolean) {
  if (show) {
    state.showModal = true
    return
  }
  closeModal()
}

function closeModal() {
  if (state.submitLoading) {
    window.$message.warning('文件正在上传，请等待上传完成')
    return
  }
  state.showModal = false
}

function handleFileSelected() {
  formRef.value?.restoreValidation()
}

async function submitForm() {
  await formRef.value?.validate()
  const file = state.formModel.file
  if (!file) {
    return
  }
  state.submitLoading = true
  try {
    const uploaded = await uploadRef.value?.upload?.()
    emit('uploaded', uploaded)
    emit('saved')
    state.showModal = false
  } finally {
    state.submitLoading = false
  }
}

function handleBeforeUnload(event: BeforeUnloadEvent) {
  if (!state.submitLoading) {
    return
  }
  event.preventDefault()
  event.returnValue = ''
}

onBeforeRouteLeave(() => {
  if (!state.submitLoading) {
    return true
  }
  return window.confirm('文件正在上传，离开页面将中断上传，确认离开?')
})

onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

defineExpose({
  openModal,
})
</script>

<template>
  <NModal
    :show="state.showModal"
    preset="card"
    draggable
    :mask-closable="false"
    :close-on-esc="!state.submitLoading"
    :closable="!state.submitLoading"
    :title="'上传文件'"
    style="width: 560px"
    :segmented="{ content: true, action: true }"
    @update:show="handleUpdateShow"
  >
    <NForm
      ref="formRef"
      :model="state.formModel"
      :rules="rules"
      label-placement="left"
      label-width="110"
      :disabled="state.submitLoading"
    >
      <NFormItem :label="'存储提供商'" path="storage_provider">
        <NSelect
          v-model:value="state.formModel.storage_provider"
          :options="storageProviderOptions"
          :disabled="state.submitLoading"
        />
      </NFormItem>
      <NFormItem :label="'文件上传'" path="file">
        <FileUpload
          ref="uploadRef"
          v-model:file="state.formModel.file"
          mode="upload"
          upload-variant="dragger"
          :auto-upload="false"
          :storage-provider="state.formModel.storage_provider"
          :button-text="'点击或拖拽文件到此处'"
          :compact="false"
          @selected="handleFileSelected"
          @cleared="handleFileSelected"
        />
      </NFormItem>
    </NForm>

    <template #action>
      <NSpace justify="end" align="center">
        <NButton :disabled="state.submitLoading" @click="closeModal">
          取消
        </NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">
          确认上传
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>
