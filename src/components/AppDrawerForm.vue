<template>
  <a-drawer
    :open="open"
    :title="title"
    :width="width"
    @close="handleClose"
    :footerStyle="{ textAlign: 'right' }"
  >
    <a-form :model="form" layout="vertical" ref="formRef">
      <slot :form="form" />
    </a-form>
    <template #footer>
      <a-space>
        <a-button @click="handleClose">取消</a-button>
        <a-button type="primary" :loading="submitLoading" @click="handleSubmit" v-if="showSubmit">
          {{ submitText }}
        </a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  open: boolean
  title: string
  form: any
  width?: number
  showSubmit?: boolean
  submitText?: string
  onSubmit: (form: any) => Promise<any>
}>()

const emit = defineEmits<{
  close: []
  success: []
}>()

const submitLoading = ref(false)
const formRef = ref()

function handleClose() {
  emit('close')
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    const result = await props.onSubmit(props.form)
    if (result !== false) {
      emit('success')
      handleClose()
    }
  } finally {
    submitLoading.value = false
  }
}
</script>
