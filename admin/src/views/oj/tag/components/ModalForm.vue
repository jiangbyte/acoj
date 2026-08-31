<!--
  Author: Charlie

  OJ 标签表单弹窗。
-->
<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { ojTagApi } from '@/api'
import { createRequiredRule } from '@/utils'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{
  saved: []
}>()

const formRef = ref<FormInst | null>(null)
const defaultFormData: Record<string, any> = {
  name: '',
  status: 'ENABLED',
}
const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: normalizeFormData(),
})

const modalTitle = computed(() => (state.dataId ? '编辑标签' : '新增标签'))
const rules = computed<FormRules>(() => ({
  name: [createRequiredRule('名称', 'input')],
  status: [createRequiredRule('状态', 'change')],
}))

async function openModal(id?: string, defaults: Partial<typeof defaultFormData> = {}) {
  state.dataId = id ?? null
  state.formModel = normalizeFormData(defaults)
  state.showModal = true
  if (id) {
    await fetchDetail(id)
  }
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await ojTagApi.detail({ id })
    state.formModel = normalizeFormData(response.data ?? {})
  } finally {
    state.loading = false
  }
}

function normalizeFormData(data: Record<string, any> = {}): Record<string, any> {
  return {
    ...defaultFormData,
    ...data,
    status: data.status || defaultFormData.status,
  }
}

function normalizeSubmitData(data: Record<string, any>): Record<string, any> {
  return {
    name: data.name,
    status: data.status,
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
      await ojTagApi.update({ ...payload, id: state.dataId })
      window.$message.success('更新成功')
    } else {
      await ojTagApi.create(payload)
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
    :width="560"
    :mask-closable="false"
  >
    <NSpin :show="state.loading">
      <NForm
        ref="formRef"
        :model="state.formModel"
        :rules="rules"
        label-placement="left"
        label-width="80"
        :disabled="state.loading || state.submitLoading"
      >
        <NFormItem
          label="名称"
          path="name"
        >
          <NInput
            v-model:value="state.formModel.name"
            placeholder="标签名称"
          />
        </NFormItem>
        <NFormItem
          label="状态"
          path="status"
        >
          <DictSelect
            v-model="state.formModel.status"
            dict-code="COMMON_STATUS"
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
