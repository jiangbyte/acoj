<!--
  Author: Charlie

  OJ 执行机运维编辑弹窗（节点由沙箱心跳自注册，不可新建）。
-->
<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { ojJudgeNodeApi } from '@/api'
import { wireFields } from '@/utils/wire'
import { createRequiredRule } from '@/utils'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{
  saved: []
}>()

const formRef = ref<FormInst | null>(null)

const defaultFormData: Record<string, any> = {
  code: '',
  name: '',
  base_url: '',
  signing_enabled: true,
  signing_secret_cipher: '',
  admin_status: 'ENABLED',
  weight: 100,
  priority: 100,
  max_concurrency: 4,
  supported_languages: [] as string[],
  extra: {} as Record<string, unknown>,
  runtime_status: '',
  last_heartbeat_at: '',
}
const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: normalizeFormData(),
})

const modalTitle = computed(() => '编辑执行机')
const rules = computed<FormRules>(() => ({
  name: [createRequiredRule('名称', 'input')],
  base_url: [createRequiredRule('地址', 'input')],
  admin_status: [createRequiredRule('管理状态', 'change')],
  weight: [
    {
      validator: () =>
        typeof state.formModel.weight === 'number' && Number.isFinite(state.formModel.weight),
      message: '请输入权重',
      trigger: ['input', 'blur'],
    },
  ],
  priority: [
    {
      validator: () =>
        typeof state.formModel.priority === 'number' && Number.isFinite(state.formModel.priority),
      message: '请输入优先级',
      trigger: ['input', 'blur'],
    },
  ],
}))

async function openModal(id: string) {
  if (!id) {
    return
  }
  state.dataId = id
  state.formModel = normalizeFormData()
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await ojJudgeNodeApi.detail({ id })
    state.formModel = normalizeFormData(response.data ?? {})
  } finally {
    state.loading = false
  }
}

function normalizeFormData(data: Record<string, any> = {}): Record<string, any> {
  return {
    ...defaultFormData,
    ...data,
    ...wireFields(
      data,
      {
        signing_enabled: 'bool',
        weight: 'int',
        priority: 'int',
        max_concurrency: 'int',
      },
      defaultFormData,
    ),
    admin_status: data.admin_status || defaultFormData.admin_status,
    signing_secret_cipher: data.signing_secret_cipher ?? '',
    supported_languages: Array.isArray(data.supported_languages) ? data.supported_languages : [],
    extra: data.extra && typeof data.extra === 'object' ? data.extra : {},
  }
}

function normalizeSubmitData(data: Record<string, any>): Record<string, any> {
  return {
    code: data.code,
    name: data.name,
    base_url: data.base_url,
    signing_enabled: data.signing_enabled !== false,
    signing_secret_cipher: data.signing_secret_cipher || null,
    admin_status: data.admin_status,
    weight: data.weight,
    priority: data.priority,
    max_concurrency: data.max_concurrency,
    supported_languages: Array.isArray(data.supported_languages) ? data.supported_languages : [],
    extra: data.extra && typeof data.extra === 'object' ? data.extra : {},
  }
}

function closeModal() {
  state.showModal = false
  state.submitLoading = false
}

async function submitForm() {
  await formRef.value?.validate()
  if (!state.dataId) {
    return
  }
  state.submitLoading = true
  try {
    const payload = normalizeSubmitData(state.formModel)
    await ojJudgeNodeApi.update({ ...payload, id: state.dataId })
    window.$message.success('更新成功')
    emit('saved')
    closeModal()
  } finally {
    state.submitLoading = false
  }
}

defineExpose({
  openModal,
})
</script>

<template>
  <HeiFormContainer
    v-model:show="state.showModal"
    :title="modalTitle"
    :width="720"
    :mask-closable="false"
  >
    <NSpin :show="state.loading">
      <NForm
        ref="formRef"
        :model="state.formModel"
        :rules="rules"
        label-placement="left"
        label-width="120"
        :disabled="state.loading || state.submitLoading"
      >
        <NFormItem label="编码">
          <NInput
            :value="state.formModel.code"
            disabled
          />
        </NFormItem>
        <NFormItem
          label="地址"
          path="base_url"
        >
          <NInput
            v-model:value="state.formModel.base_url"
            placeholder="如 http://127.0.0.1:8080（心跳仅写入初始值，请按实际可达地址维护）"
          />
        </NFormItem>
        <NFormItem label="运行状态">
          <NInput
            :value="state.formModel.runtime_status"
            disabled
          />
        </NFormItem>
        <NFormItem label="最近心跳">
          <NInput
            :value="state.formModel.last_heartbeat_at"
            disabled
          />
        </NFormItem>
        <NFormItem label="最大并发">
          <NInputNumber
            :value="state.formModel.max_concurrency"
            class="w-full"
            disabled
          />
        </NFormItem>
        <NFormItem
          label="名称"
          path="name"
        >
          <NInput
            v-model:value="state.formModel.name"
            placeholder="展示名"
          />
        </NFormItem>
        <NFormItem
          label="管理状态"
          path="admin_status"
        >
          <DictSelect
            v-model="state.formModel.admin_status"
            dict-code="OJ_JUDGE_ADMIN_STATUS"
            :clearable="false"
          />
        </NFormItem>
        <NFormItem
          label="权重"
          path="weight"
        >
          <NInputNumber
            v-model:value="state.formModel.weight"
            class="w-full"
          />
        </NFormItem>
        <NFormItem
          label="优先级"
          path="priority"
        >
          <NInputNumber
            v-model:value="state.formModel.priority"
            class="w-full"
          />
        </NFormItem>
      </NForm>
    </NSpin>

    <template #action>
      <NSpace justify="end">
        <NButton @click="closeModal">
          取消
        </NButton>
        <NButton
          type="primary"
          :loading="state.submitLoading"
          @click="submitForm"
        >
          确认
        </NButton>
      </NSpace>
    </template>
  </HeiFormContainer>
</template>
