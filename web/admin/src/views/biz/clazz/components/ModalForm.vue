<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { ojClazzApi } from '@/api'
import { createRequiredRule } from '@/utils'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{
  saved: []
}>()

const formRef = ref<FormInst | null>(null)
const defaultFormData: Record<string, any> = {
  code: '',
  name: '',
  summary: '',
  visibility: 'PRIVATE',
}
const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: { ...defaultFormData },
})

const modalTitle = computed(() => state.dataId ? '编辑班级' : '新增班级')
const rules = computed<FormRules>(() => ({
  code: [createRequiredRule('班级编码', 'input')],
  name: [createRequiredRule('班级名称', 'input')],
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
    const response = await ojClazzApi.detail({ id })
    const data = response.data ?? {}
    state.formModel = {
      code: data.code ?? '',
      name: data.name ?? '',
      summary: data.summary ?? '',
      visibility: data.visibility ?? 'PRIVATE',
    }
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
      summary: state.formModel.summary || null,
    }
    if (state.dataId) {
      await ojClazzApi.update({ ...payload, id: state.dataId })
      window.$message.success('更新成功')
    } else {
      await ojClazzApi.create(payload)
      window.$message.success('创建成功')
    }
    emit('saved')
    closeModal()
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
        <NForm ref="formRef" :model="state.formModel" :rules="rules" label-placement="left" label-width="110" :disabled="state.loading || state.submitLoading">
          <NFormItem label="班级编码" path="code">
            <NInput v-model:value="state.formModel.code" :disabled="!!state.dataId" />
          </NFormItem>
          <NFormItem label="班级名称" path="name">
            <NInput v-model:value="state.formModel.name" />
          </NFormItem>
          <NFormItem label="简介" path="summary">
            <NInput v-model:value="state.formModel.summary" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
          </NFormItem>
          <NFormItem label="可见性" path="visibility">
            <NSelect
              v-model:value="state.formModel.visibility"
              :options="[
                { label: '公开', value: 'PUBLIC' },
                { label: '私有', value: 'PRIVATE' },
              ]"
            />
          </NFormItem>
        </NForm>
      </NScrollbar>
    </NSpin>
    <template #action>
      <NSpace justify="end">
        <NButton @click="closeModal">取消</NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">确认</NButton>
      </NSpace>
    </template>
  </NModal>
</template>
