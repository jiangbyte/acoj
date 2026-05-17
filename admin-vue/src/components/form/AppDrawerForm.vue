<template>
  <a-drawer
    :open="open"
    :title="title"
    :width="drawerWidth"
    :footer-style="{ textAlign: 'right' }"
    destroy-on-close
    @close="handleClose"
  >
    <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
      <slot :form="form" />
    </a-form>
    <template #footer>
      <a-space>
        <a-button @click="handleClose">取消</a-button>
        <a-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ submitText || '保存' }}
        </a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAppStore } from '@/store'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  title: string
  form: any
  rules?: Record<string, any[]>
  width?: number
  showSubmit?: boolean
  submitText?: string
  onSubmit: (form: any) => Promise<any>
}>()

const appStore = useAppStore()
const drawerWidth = computed(() => (appStore.isMobile ? '100%' : (props.width ?? 560)))

const emit = defineEmits<{
  close: []
  success: []
}>()

const submitLoading = ref(false)
const formRef = ref()

function handleClose() {
  formRef.value?.resetFields()
  emit('close')
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return

  submitLoading.value = true
  const result = await props.onSubmit(props.form)
  if (result === false || result?.success === false) {
    submitLoading.value = false
    return
  }
  message.success('保存成功')
  emit('success')
  handleClose()
  submitLoading.value = false
}
</script>
