<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { ojJudgeApi } from '@/api'
import { createRequiredRule, formatDateTime } from '@/utils'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{ saved: [] }>()

const formRef = ref<FormInst | null>(null)
const defaultFormData = {
  name: '',
  auth_key_hash: '',
  status: 'ENABLED',
  online: false,
  tier: 1,
  last_ip: '',
  last_heartbeat_at: '',
  load: null as number | null,
  supported_languages: '[]',
  supported_modes: '[]',
  description: '',
  extra: '{}',
}

const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: { ...defaultFormData },
})

const modalTitle = computed(() => (state.dataId ? '编辑判题节点' : '新增判题节点'))
const rules = computed<FormRules>(() => ({
  name: createRequiredRule('name', 'input'),
  auth_key_hash: createRequiredRule('auth_key_hash', 'input'),
  status: createRequiredRule('status', 'change'),
  tier: createRequiredRule('tier', 'input'),
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
    const response = await ojJudgeApi.node.detail({ id })
    const data = response.data ?? {}
    state.formModel = {
      ...defaultFormData,
      ...data,
      last_ip: data.last_ip ?? '',
      last_heartbeat_at: formatDateTime(data.last_heartbeat_at, ''),
      load: data.load ?? null,
      supported_languages: JSON.stringify(data.supported_languages ?? [], null, 2),
      supported_modes: JSON.stringify(data.supported_modes ?? [], null, 2),
      description: data.description ?? '',
      extra: JSON.stringify(data.extra ?? {}, null, 2),
    }
  } finally {
    state.loading = false
  }
}

async function submitForm() {
  await formRef.value?.validate()
  state.submitLoading = true
  try {
    const payload = {
      ...state.formModel,
      last_ip: nullableString(state.formModel.last_ip),
      last_heartbeat_at: nullableString(state.formModel.last_heartbeat_at),
      description: nullableString(state.formModel.description),
      supported_languages: parseJson(state.formModel.supported_languages, []),
      supported_modes: parseJson(state.formModel.supported_modes, []),
      extra: parseJson(state.formModel.extra, {}),
    }
    if (state.dataId) {
      await ojJudgeApi.node.update({ ...payload, id: state.dataId })
      window.$message.success('更新成功')
    } else {
      await ojJudgeApi.node.create(payload)
      window.$message.success('创建成功')
    }
    state.showModal = false
    emit('saved')
  } finally {
    state.submitLoading = false
  }
}

function nullableString(value: string) {
  const trimmed = value.trim()
  return trimmed ? trimmed : null
}

function parseJson(value: string, fallback: any) {
  try {
    return JSON.parse(value || JSON.stringify(fallback))
  } catch {
    return fallback
  }
}

defineExpose({ openModal })
</script>

<template>
  <NModal v-model:show="state.showModal" preset="card" draggable :mask-closable="false" :title="modalTitle" style="width: 760px" :segmented="{ content: true, action: true }">
    <NSpin :show="state.loading">
      <NScrollbar class="max-h-[min(640px,calc(100vh-260px))] pr-16px">
        <NForm ref="formRef" :model="state.formModel" :rules="rules" label-placement="left" label-width="170" :disabled="state.loading || state.submitLoading">
          <NGrid :cols="2" :x-gap="16">
            <NGi><NFormItem label="name" path="name"><NInput v-model:value="state.formModel.name" /></NFormItem></NGi>
            <NGi><NFormItem label="auth_key_hash" path="auth_key_hash"><NInput v-model:value="state.formModel.auth_key_hash" /></NFormItem></NGi>
            <NGi><NFormItem label="status" path="status"><NSelect v-model:value="state.formModel.status" :options="['ENABLED','DISABLED','BLOCKED'].map(value => ({ label: value, value }))" /></NFormItem></NGi>
            <NGi><NFormItem label="online" path="online"><NSwitch v-model:value="state.formModel.online" /></NFormItem></NGi>
            <NGi><NFormItem label="tier" path="tier"><NInputNumber v-model:value="state.formModel.tier" class="w-full" :min="0" /></NFormItem></NGi>
            <NGi><NFormItem label="load" path="load"><NInputNumber v-model:value="state.formModel.load" class="w-full" clearable /></NFormItem></NGi>
            <NGi><NFormItem label="last_ip" path="last_ip"><NInput v-model:value="state.formModel.last_ip" /></NFormItem></NGi>
            <NGi><NFormItem label="last_heartbeat_at" path="last_heartbeat_at"><NInput v-model:value="state.formModel.last_heartbeat_at" /></NFormItem></NGi>
          </NGrid>
          <NFormItem label="supported_languages" path="supported_languages"><NInput v-model:value="state.formModel.supported_languages" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" /></NFormItem>
          <NFormItem label="supported_modes" path="supported_modes"><NInput v-model:value="state.formModel.supported_modes" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" /></NFormItem>
          <NFormItem label="description" path="description"><NInput v-model:value="state.formModel.description" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }" /></NFormItem>
          <NFormItem label="extra" path="extra"><NInput v-model:value="state.formModel.extra" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" /></NFormItem>
        </NForm>
      </NScrollbar>
    </NSpin>
    <template #action>
      <NSpace justify="end">
        <NButton @click="state.showModal = false">取消</NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">确认</NButton>
      </NSpace>
    </template>
  </NModal>
</template>
