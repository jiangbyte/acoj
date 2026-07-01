<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { messageApi } from '@/api'
import { createRequiredRule } from '@/utils'
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const emit = defineEmits<{
  saved: []
}>()

const { t } = useI18n()
const formRef = ref<FormInst | null>(null)
const state = reactive({
  showModal: false,
  submitLoading: false,
  formModel: {
    thread_id: '',
    content: '',
  },
})

const rules = computed<FormRules>(() => ({
  content: createRequiredRule(t, t('resource.message.message.content'), 'input'),
}))

function openModal(threadId: string) {
  state.formModel.thread_id = threadId
  state.formModel.content = ''
  state.showModal = true
}

function closeModal() {
  state.showModal = false
  state.submitLoading = false
}

async function submitForm() {
  await formRef.value?.validate()
  state.submitLoading = true
  try {
    await messageApi.sendSystemMessage({
      thread_id: state.formModel.thread_id,
      content: state.formModel.content,
      sender_name: 'System',
    })
    window.$message.success(t('resource.message.message.send_success'))
    closeModal()
    emit('saved')
  } finally {
    state.submitLoading = false
  }
}

defineExpose({ openModal })
</script>

<template>
  <NModal
    v-model:show="state.showModal"
    preset="card"
    draggable
    :mask-closable="false"
    :title="t('resource.message.message.send_system')"
    style="width: 640px"
    :segmented="{ content: true, action: true }"
  >
    <NForm
      ref="formRef"
      :model="state.formModel"
      :rules="rules"
      label-placement="left"
      label-width="100"
      :disabled="state.submitLoading"
    >
      <NFormItem :label="t('resource.message.message.thread_id')" path="thread_id">
        <NInput v-model:value="state.formModel.thread_id" disabled />
      </NFormItem>
      <NFormItem :label="t('resource.message.message.content')" path="content">
        <NInput
          v-model:value="state.formModel.content"
          type="textarea"
          :autosize="{ minRows: 4, maxRows: 8 }"
        />
      </NFormItem>
    </NForm>

    <template #action>
      <NSpace justify="end" align="center">
        <NButton @click="closeModal">{{ t('common.cancel') }}</NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">
          {{ t('common.confirm') }}
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>
