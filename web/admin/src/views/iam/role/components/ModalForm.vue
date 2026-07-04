<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { roleApi } from '@/api'
import { createRequiredRule, toNullableString } from '@/utils'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{
  saved: []
}>()

const formRef = ref<FormInst | null>(null)
const defaultFormData = {
  code: '',
  name: '',
  category: null as string | null,
  scope_type: null as string | null,
  owner_dept_id: '',
  sort: 0,
  status: 'ENABLED',
  is_builtin: false,
  description: '',
  extra: {},
}
const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: { ...defaultFormData },
})

const modalTitle = computed(() =>
  state.dataId ? 'Edit Role' : 'Add Role',
)

const rules = computed<FormRules>(() => ({
  code: createRequiredRule('Role Code', 'input'),
  name: createRequiredRule('Role Name', 'input'),
  category: createRequiredRule('Role Category', 'change'),
  scope_type: createRequiredRule('Scope Type', 'change'),
  status: createRequiredRule('Status', 'change'),
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
    const response = await roleApi.detail({ id })
    state.formModel = Object.assign({}, defaultFormData, response.data, {
      owner_dept_id: response.data?.owner_dept_id ?? '',
      description: response.data?.description ?? '',
      extra: response.data?.extra ?? {},
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
      code: state.formModel.code.trim(),
      name: state.formModel.name.trim(),
      owner_dept_id: toNullableString(state.formModel.owner_dept_id),
      sort: Number(state.formModel.sort ?? 0),
      is_builtin: Boolean(state.formModel.is_builtin),
      description: toNullableString(state.formModel.description),
      extra: state.formModel.extra ?? {},
    }

    if (state.dataId) {
      await roleApi.update({
        ...payload,
        id: state.dataId,
      })
      window.$message.success('Updated successfully')
    } else {
      await roleApi.create(payload)
      window.$message.success('Created successfully')
    }

    closeModal()
    emit('saved')
  } finally {
    state.submitLoading = false
  }
}

defineExpose({
  openModal,
})
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
          label-width="110"
          :disabled="state.loading || state.submitLoading"
        >
          <NFormItem :label="'Role Code'" path="code">
            <NInput v-model:value="state.formModel.code" />
          </NFormItem>
          <NFormItem :label="'Role Name'" path="name">
            <NInput v-model:value="state.formModel.name" />
          </NFormItem>
          <NFormItem :label="'Role Category'" path="category">
            <DictSelect v-model="state.formModel.category" dict-code="SYS_BIZ_CATEGORY" />
          </NFormItem>
          <NFormItem :label="'Scope Type'" path="scope_type">
            <DictSelect v-model="state.formModel.scope_type" dict-code="ROLE_SCOPE_TYPE" />
          </NFormItem>
          <NFormItem :label="'Owner Department ID'" path="owner_dept_id">
            <NInput v-model:value="state.formModel.owner_dept_id" />
          </NFormItem>
          <NFormItem :label="'Sort'" path="sort">
            <NInputNumber v-model:value="state.formModel.sort" class="w-full" :min="0" />
          </NFormItem>
          <NFormItem :label="'Builtin Role'" path="is_builtin">
            <NSwitch v-model:value="state.formModel.is_builtin" />
          </NFormItem>
          <NFormItem :label="'Status'" path="status">
            <DictSelect v-model="state.formModel.status" dict-code="COMMON_STATUS" type="radio" />
          </NFormItem>
          <NFormItem :label="'Description'" path="description">
            <NInput
              v-model:value="state.formModel.description"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 5 }"
            />
          </NFormItem>
        </NForm>
      </NScrollbar>
    </NSpin>

    <template #action>
      <NSpace justify="end" align="center">
        <NButton @click="closeModal">
          {{ 'Cancel' }}
        </NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">
          {{ 'Confirm' }}
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>
