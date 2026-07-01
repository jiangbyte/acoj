<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { messageApi } from '@/api'
import { createRequiredRule, toNullableString } from '@/utils'
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const emit = defineEmits<{
  saved: []
}>()

const { t } = useI18n()
const formRef = ref<FormInst | null>(null)
const defaultFormData = {
  title: '',
  content: '',
  content_type: 'TEXT',
  priority: 'NORMAL',
  target_scope: 'SPECIFIC',
  target_account_type: null as string | null,
  target_account_id: null as string | null,
  status: 'PENDING',
  due_at: null as string | null,
}
const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: { ...defaultFormData },
})

const modalTitle = computed(() =>
  state.dataId ? t('resource.message.todo.edit_todo') : t('resource.message.todo.add_todo'),
)

const rules = computed<FormRules>(() => ({
  title: createRequiredRule(t, t('resource.message.todo.title_field'), 'input'),
  priority: createRequiredRule(t, t('resource.message.todo.priority'), 'change'),
  target_scope: createRequiredRule(t, t('resource.message.todo.target_scope'), 'change'),
  status: createRequiredRule(t, t('common.often.status'), 'change'),
}))

async function openModal(id?: string) {
  state.dataId = id ?? null
  state.formModel = { ...defaultFormData }
  state.showModal = true
  if (id) {
    await fetchDetail(id)
  }
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await messageApi.todoDetail({ id })
    state.formModel = Object.assign({}, defaultFormData, response.data)
  } finally {
    state.loading = false
  }
}

function closeModal() {
  state.showModal = false
  state.submitLoading = false
}

async function submitForm() {
  await formRef.value?.validate()
  state.submitLoading = true
  try {
    const payload = {
      ...state.formModel,
      target_account_type: toNullableString(state.formModel.target_account_type),
      target_account_id: toNullableString(state.formModel.target_account_id),
      due_at: toNullableString(state.formModel.due_at),
    }
    if (state.dataId) {
      await messageApi.updateTodo({ ...payload, id: state.dataId })
      window.$message.success(t('common.often.update_success'))
    } else {
      await messageApi.createTodo(payload)
      window.$message.success(t('common.often.create_success'))
    }
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
    :title="modalTitle"
    style="width: 720px"
    :segmented="{ content: true, action: true }"
  >
    <NSpin :show="state.loading">
      <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
        <NForm
          ref="formRef"
          :model="state.formModel"
          :rules="rules"
          label-placement="left"
          label-width="120"
          :disabled="state.loading || state.submitLoading"
        >
          <NFormItem :label="t('resource.message.todo.title_field')" path="title">
            <NInput v-model:value="state.formModel.title" />
          </NFormItem>
          <NFormItem :label="t('resource.message.todo.content')" path="content">
            <NInput
              v-model:value="state.formModel.content"
              type="textarea"
              :autosize="{ minRows: 4, maxRows: 8 }"
            />
          </NFormItem>
          <NFormItem :label="t('resource.message.todo.priority')" path="priority">
            <DictSelect v-model="state.formModel.priority" dict-code="TODO_PRIORITY" />
          </NFormItem>
          <NFormItem :label="t('resource.message.todo.target_scope')" path="target_scope">
            <DictSelect v-model="state.formModel.target_scope" dict-code="MESSAGE_TARGET_SCOPE" />
          </NFormItem>
          <NFormItem :label="t('resource.message.todo.target_account_type')" path="target_account_type">
            <DictSelect v-model="state.formModel.target_account_type" dict-code="ACCOUNT_TYPE" />
          </NFormItem>
          <NFormItem :label="t('resource.message.todo.target_account_id')" path="target_account_id">
            <NInput v-model:value="state.formModel.target_account_id" clearable />
          </NFormItem>
          <NFormItem :label="t('common.often.status')" path="status">
            <DictSelect v-model="state.formModel.status" dict-code="TODO_STATUS" type="radio" />
          </NFormItem>
        </NForm>
      </NScrollbar>
    </NSpin>

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
