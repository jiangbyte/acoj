<!--
  Author: Charlie

  题目参考答案新增/编辑弹窗。
-->
<script setup lang="ts">
import type { FormInst, FormRules, SelectOption } from 'naive-ui'
import { MonacoEditor } from '@/components/editor'
import { ojProblemSolutionApi } from '@/api'
import { createRequiredRule, mapOjLanguageToMonaco } from '@/utils'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{
  saved: []
}>()

const formRef = ref<FormInst | null>(null)

const defaultFormData: Record<string, any> = {
  language: '',
  source: '',
  is_default: false,
  status: 'ENABLED',
  remark: '',
}

const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  problemId: '',
  languageOptions: [] as SelectOption[],
  formModel: normalizeFormData(),
})

const modalTitle = computed(() => (state.dataId ? '编辑参考答案' : '新增参考答案'))
const monacoLanguage = computed(() => mapOjLanguageToMonaco(state.formModel.language))
const rules = computed<FormRules>(() => ({
  language: [createRequiredRule('语言', 'change')],
  source: [createRequiredRule('源码', 'input')],
  status: [createRequiredRule('状态', 'change')],
}))

async function openModal(options: {
  problemId: string
  id?: string
  languageOptions?: SelectOption[]
  defaultLanguage?: string
}) {
  state.problemId = options.problemId
  state.dataId = options.id ?? null
  state.languageOptions = options.languageOptions ?? []
  state.formModel = normalizeFormData({
    language: options.defaultLanguage || '',
  })
  state.showModal = true
  if (options.id) {
    await fetchDetail(options.id)
  }
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await ojProblemSolutionApi.detail({ id })
    state.formModel = normalizeFormData(response.data ?? {})
  } finally {
    state.loading = false
  }
}

function normalizeFormData(data: Record<string, any> = {}): Record<string, any> {
  return {
    ...defaultFormData,
    ...data,
    language: data.language || defaultFormData.language,
    source: data.source ?? '',
    is_default: !!data.is_default,
    status: data.status || defaultFormData.status,
    remark: data.remark ?? '',
  }
}

function normalizeSubmitData(data: Record<string, any>) {
  return {
    problem_id: state.problemId,
    language: data.language,
    source: data.source,
    is_default: !!data.is_default,
    status: data.status,
    remark: data.remark || null,
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
    const payload = normalizeSubmitData(state.formModel)
    if (state.dataId) {
      await ojProblemSolutionApi.update({ ...payload, id: state.dataId })
      window.$message.success('更新成功')
    } else {
      await ojProblemSolutionApi.create(payload)
      window.$message.success('创建成功')
    }
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
    :width="760"
    :mask-closable="false"
  >
    <NSpin :show="state.loading">
      <NForm
        ref="formRef"
        :model="state.formModel"
        :rules="rules"
        label-placement="left"
        label-width="100"
        :disabled="state.loading || state.submitLoading"
      >
        <NFormItem
          label="语言"
          path="language"
        >
          <NSelect
            v-if="state.languageOptions.length"
            v-model:value="state.formModel.language"
            :options="state.languageOptions"
            filterable
            tag
            :disabled="!!state.dataId"
          />
          <NInput
            v-else
            v-model:value="state.formModel.language"
            placeholder="如 cpp17"
            :disabled="!!state.dataId"
          />
        </NFormItem>
        <NFormItem
          label="状态"
          path="status"
        >
          <DictSelect
            v-model="state.formModel.status"
            dict-code="COMMON_STATUS"
            :clearable="false"
          />
        </NFormItem>
        <NFormItem
          label="默认答案"
          path="is_default"
        >
          <NSwitch v-model:value="state.formModel.is_default" />
        </NFormItem>
        <NFormItem
          label="备注"
          path="remark"
        >
          <NInput v-model:value="state.formModel.remark" />
        </NFormItem>
        <NFormItem
          label="源码"
          path="source"
          label-placement="top"
        >
          <MonacoEditor
            v-model:value="state.formModel.source"
            :language="monacoLanguage"
            :height="360"
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
