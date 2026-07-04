<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import FileUpload from '@/components/upload/FileUpload.vue'
import { messageApi } from '@/api'
import { createRequiredRule } from '@/utils'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{
  saved: []
}>()

const formRef = ref<FormInst | null>(null)
const state = reactive({
  showModal: false,
  submitLoading: false,
  formModel: {
    thread_id: '',
    content: '',
    attachments: [] as any[],
  },
})

const rules = computed<FormRules>(() => ({
  content: createRequiredRule('Content', 'input'),
}))

function openModal(threadId: string) {
  state.formModel.thread_id = threadId
  state.formModel.content = ''
  state.formModel.attachments = []
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
      attachments: state.formModel.attachments,
    })
    window.$message.success('Sent successfully')
    closeModal()
    emit('saved')
  } finally {
    state.submitLoading = false
  }
}

defineExpose({ openModal })

function appendAttachment(file: any) {
  state.formModel.attachments.push({
    name: file.original_name || file.object_name || file.url,
    url: file.url || file.object_name,
    content_type: file.content_type || null,
    size: file.size ?? null,
    sort: state.formModel.attachments.length,
  })
}

function removeAttachment(index: number) {
  state.formModel.attachments.splice(index, 1)
  state.formModel.attachments.forEach((item, itemIndex) => {
    item.sort = itemIndex
  })
}
</script>

<template>
  <NModal
    v-model:show="state.showModal"
    preset="card"
    draggable
    :mask-closable="false"
    :title="'Send System Message'"
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
      <NFormItem :label="'Thread ID'" path="thread_id">
        <NInput v-model:value="state.formModel.thread_id" disabled />
      </NFormItem>
      <NFormItem :label="'Content'" path="content">
        <NInput
          v-model:value="state.formModel.content"
          type="textarea"
          :autosize="{ minRows: 4, maxRows: 8 }"
        />
      </NFormItem>
      <NFormItem :label="'Attachments'">
        <NFlex vertical class="w-full">
          <FileUpload compact @uploaded="appendAttachment" />
          <NList v-if="state.formModel.attachments.length" bordered>
            <NListItem v-for="(item, index) in state.formModel.attachments" :key="`${item.url}-${index}`">
              <NThing :title="item.name" :description="item.content_type || undefined">
                <template #header-extra>
                  <NButton size="small" text type="error" @click="removeAttachment(index)">
                    {{ 'Delete' }}
                  </NButton>
                </template>
              </NThing>
            </NListItem>
          </NList>
        </NFlex>
      </NFormItem>
    </NForm>

    <template #action>
      <NSpace justify="end" align="center">
        <NButton @click="closeModal">{{ 'Cancel' }}</NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">
          {{ 'Confirm' }}
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>
