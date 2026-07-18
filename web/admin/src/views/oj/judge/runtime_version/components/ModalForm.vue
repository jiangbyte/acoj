<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { ojJudgeApi } from '@/api'
import { createRequiredRule } from '@/utils'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{ saved: [] }>()

const formRef = ref<FormInst | null>(null)
const defaultFormData = {
  judge_node_id: '',
  language_id: '',
  runtime_name: '',
  runtime_version: '',
  priority: 0,
}

const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: { ...defaultFormData },
})

const modalTitle = computed(() => (state.dataId ? '编辑运行时版本' : '新增运行时版本'))
const rules = computed<FormRules>(() => ({
  judge_node_id: createRequiredRule('judge_node_id', 'input'),
  language_id: createRequiredRule('language_id', 'input'),
  runtime_name: createRequiredRule('runtime_name', 'input'),
  priority: createRequiredRule('priority', 'input'),
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
    const response = await ojJudgeApi.runtimeVersion.detail({ id })
    const data = response.data ?? {}
    state.formModel = {
      ...defaultFormData,
      ...data,
      runtime_version: data.runtime_version ?? '',
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
      runtime_version: nullableString(state.formModel.runtime_version),
    }
    if (state.dataId) {
      await ojJudgeApi.runtimeVersion.update({ ...payload, id: state.dataId })
      window.$message.success('更新成功')
    } else {
      await ojJudgeApi.runtimeVersion.create(payload)
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

defineExpose({ openModal })
</script>

<template>
  <NModal v-model:show="state.showModal" preset="card" draggable :mask-closable="false" :title="modalTitle" style="width: 620px" :segmented="{ content: true, action: true }">
    <NSpin :show="state.loading">
      <NForm ref="formRef" :model="state.formModel" :rules="rules" label-placement="left" label-width="150" :disabled="state.loading || state.submitLoading">
        <NFormItem label="judge_node_id" path="judge_node_id"><NInput v-model:value="state.formModel.judge_node_id" /></NFormItem>
        <NFormItem label="language_id" path="language_id"><NInput v-model:value="state.formModel.language_id" /></NFormItem>
        <NFormItem label="runtime_name" path="runtime_name"><NInput v-model:value="state.formModel.runtime_name" /></NFormItem>
        <NFormItem label="runtime_version" path="runtime_version"><NInput v-model:value="state.formModel.runtime_version" /></NFormItem>
        <NFormItem label="priority" path="priority"><NInputNumber v-model:value="state.formModel.priority" class="w-full" :min="0" /></NFormItem>
      </NForm>
    </NSpin>
    <template #action>
      <NSpace justify="end">
        <NButton @click="state.showModal = false">取消</NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">确认</NButton>
      </NSpace>
    </template>
  </NModal>
</template>
