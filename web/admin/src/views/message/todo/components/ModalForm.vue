<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { messageApi } from '@/api'
import { createRequiredRule, formatDateTime, toNullableString } from '@/utils'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{
  saved: []
}>()

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
  state.dataId ? '编辑 待办' : '新增 待办',
)

const rules = computed<FormRules>(() => ({
  title: createRequiredRule('标题', 'input'),
  priority: createRequiredRule('优先级', 'change'),
  target_scope: createRequiredRule('目标范围', 'change'),
  status: createRequiredRule('状态', 'change'),
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
    state.formModel = Object.assign({}, defaultFormData, response.data, {
      due_at: formatDateTime(response.data?.due_at, ''),
    })
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
      window.$message.success('更新成功')
    } else {
      await messageApi.createTodo(payload)
      window.$message.success('创建成功')
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
          <NFormItem :label="'标题'" path="title">
            <NInput v-model:value="state.formModel.title" />
          </NFormItem>
          <NFormItem :label="'内容'" path="content">
            <NInput
              v-model:value="state.formModel.content"
              type="textarea"
              :autosize="{ minRows: 4, maxRows: 8 }"
            />
          </NFormItem>
          <NFormItem :label="'优先级'" path="priority">
            <DictSelect v-model="state.formModel.priority" dict-code="TODO_PRIORITY" />
          </NFormItem>
          <NFormItem :label="'目标范围'" path="target_scope">
            <DictSelect v-model="state.formModel.target_scope" dict-code="MESSAGE_TARGET_SCOPE" />
          </NFormItem>
          <NFormItem
            :label="'目标账号类型'"
            path="target_account_type"
          >
            <DictSelect v-model="state.formModel.target_account_type" dict-code="ACCOUNT_TYPE" />
          </NFormItem>
          <NFormItem :label="'目标账号ID'" path="target_account_id">
            <NInput v-model:value="state.formModel.target_account_id" clearable />
          </NFormItem>
          <NFormItem :label="'状态'" path="status">
            <DictSelect v-model="state.formModel.status" dict-code="TODO_STATUS" type="radio" />
          </NFormItem>
        </NForm>
      </NScrollbar>
    </NSpin>

    <template #action>
      <NSpace justify="end" align="center">
        <NButton @click="closeModal">取消</NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">
          确认
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>
