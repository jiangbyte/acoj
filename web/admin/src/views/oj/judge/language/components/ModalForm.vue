<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { ojJudgeApi } from '@/api'
import { createRequiredRule } from '@/utils'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{ saved: [] }>()

const formRef = ref<FormInst | null>(null)
const defaultFormData = {
  key: '',
  name: '',
  short_name: '',
  common_name: '',
  ace_mode: '',
  pygments: '',
  extension: '',
  template: '',
  compile_command: '',
  run_command: '',
  status: 'ENABLED',
  extra: '{}',
}

const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: { ...defaultFormData },
})

const modalTitle = computed(() => (state.dataId ? '编辑语言配置' : '新增语言配置'))
const rules = computed<FormRules>(() => ({
  key: createRequiredRule('key', 'input'),
  name: createRequiredRule('name', 'input'),
  status: createRequiredRule('status', 'change'),
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
    const response = await ojJudgeApi.language.detail({ id })
    const data = response.data ?? {}
    state.formModel = {
      ...defaultFormData,
      ...data,
      short_name: data.short_name ?? '',
      common_name: data.common_name ?? '',
      ace_mode: data.ace_mode ?? '',
      pygments: data.pygments ?? '',
      extension: data.extension ?? '',
      template: data.template ?? '',
      compile_command: data.compile_command ?? '',
      run_command: data.run_command ?? '',
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
      short_name: nullableString(state.formModel.short_name),
      common_name: nullableString(state.formModel.common_name),
      ace_mode: nullableString(state.formModel.ace_mode),
      pygments: nullableString(state.formModel.pygments),
      extension: nullableString(state.formModel.extension),
      template: nullableString(state.formModel.template),
      compile_command: nullableString(state.formModel.compile_command),
      run_command: nullableString(state.formModel.run_command),
      extra: parseJson(state.formModel.extra),
    }
    if (state.dataId) {
      await ojJudgeApi.language.update({ ...payload, id: state.dataId })
      window.$message.success('更新成功')
    } else {
      await ojJudgeApi.language.create(payload)
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

function parseJson(value: string) {
  try {
    return JSON.parse(value || '{}')
  } catch {
    return {}
  }
}

defineExpose({ openModal })
</script>

<template>
  <NModal v-model:show="state.showModal" preset="card" draggable :mask-closable="false" :title="modalTitle" style="width: 820px" :segmented="{ content: true, action: true }">
    <NSpin :show="state.loading">
      <NScrollbar class="max-h-[min(680px,calc(100vh-260px))] pr-16px">
        <NForm ref="formRef" :model="state.formModel" :rules="rules" label-placement="left" label-width="150" :disabled="state.loading || state.submitLoading">
          <NGrid :cols="2" :x-gap="16">
            <NGi><NFormItem label="key" path="key"><NInput v-model:value="state.formModel.key" /></NFormItem></NGi>
            <NGi><NFormItem label="name" path="name"><NInput v-model:value="state.formModel.name" /></NFormItem></NGi>
            <NGi><NFormItem label="short_name" path="short_name"><NInput v-model:value="state.formModel.short_name" /></NFormItem></NGi>
            <NGi><NFormItem label="common_name" path="common_name"><NInput v-model:value="state.formModel.common_name" /></NFormItem></NGi>
            <NGi><NFormItem label="ace_mode" path="ace_mode"><NInput v-model:value="state.formModel.ace_mode" /></NFormItem></NGi>
            <NGi><NFormItem label="pygments" path="pygments"><NInput v-model:value="state.formModel.pygments" /></NFormItem></NGi>
            <NGi><NFormItem label="extension" path="extension"><NInput v-model:value="state.formModel.extension" /></NFormItem></NGi>
            <NGi><NFormItem label="status" path="status"><NSelect v-model:value="state.formModel.status" :options="['ENABLED','DISABLED'].map(value => ({ label: value, value }))" /></NFormItem></NGi>
          </NGrid>
          <NFormItem label="template" path="template"><NInput v-model:value="state.formModel.template" type="textarea" :autosize="{ minRows: 4, maxRows: 8 }" /></NFormItem>
          <NFormItem label="compile_command" path="compile_command"><NInput v-model:value="state.formModel.compile_command" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }" /></NFormItem>
          <NFormItem label="run_command" path="run_command"><NInput v-model:value="state.formModel.run_command" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }" /></NFormItem>
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
