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
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
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

const isMobile = ref(false)
onMounted(() => {
  const mql = window.matchMedia('(max-width: 767px)')
  isMobile.value = mql.matches
  const handler = (e: MediaQueryListEvent) => { isMobile.value = e.matches }
  mql.addEventListener('change', handler)
  onBeforeUnmount(() => mql.removeEventListener('change', handler))
})

const drawerWidth = computed(() => (isMobile.value ? '100%' : (props.width ?? 560)))

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
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const result = await props.onSubmit(props.form)
    if (result === false || result?.success === false) return
    message.success('保存成功')
    emit('success')
    handleClose()
  } finally {
    submitLoading.value = false
  }
}
</script>
