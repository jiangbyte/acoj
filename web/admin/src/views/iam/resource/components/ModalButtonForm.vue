<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { deptApi, resourceApi } from '@/api'
import { createRequiredRule, toNullableString } from '@/utils'
import { computed, reactive, ref } from 'vue'
import ModalPermissionSelector from './ModalPermissionSelector.vue'

const emit = defineEmits<{
  saved: []
}>()

const formRef = ref<FormInst | null>(null)
const permissionSelectorRef = ref<any>(null)
const defaultFormData = {
  id: '',
  parent_id: '',
  code: '',
  name: '',
  permission_key: '',
  data_scope: 'ALL',
  custom_scope_dept_ids: [] as string[],
  sort: 99,
  status: 'ENABLED',
  description: '',
}
const state = reactive({
  showModal: false,
  submitLoading: false,
  treeLoading: false,
  parent: {} as any,
  deptTree: [] as any[],
  formModel: { ...defaultFormData },
})

const modalTitle = computed(() => {
  const action = state.formModel.id
    ? 'Edit Button'
    : 'Add Button'
  return state.parent?.name
    ? `${action} - ${state.parent.name}`
    : action
})
const rules = computed<FormRules>(() => ({
  code: createRequiredRule('Resource Code', 'input'),
  name: createRequiredRule('Resource Name', 'input'),
  permission_key: createRequiredRule('Permission Key', 'change'),
  data_scope: createRequiredRule('Data Scope', 'change'),
  status: createRequiredRule('Status', 'change'),
}))

async function openModal(parent: any, row?: any) {
  state.parent = parent ?? {}
  state.formModel = {
    ...defaultFormData,
    ...row,
    id: row?.id ?? '',
    parent_id: parent?.id ?? row?.parent_id ?? '',
    permission_key: row?.permission_key ?? '',
    data_scope: row?.data_scope ?? 'ALL',
    custom_scope_dept_ids: row?.custom_scope_dept_ids ?? [],
    sort: Number(row?.sort ?? 99),
    status: row?.status ?? 'ENABLED',
    description: row?.description ?? row?.permission_description ?? '',
  }
  state.showModal = true
  await fetchDeptTree()
}

async function fetchDeptTree() {
  state.treeLoading = true
  try {
    const response = await deptApi.tree().catch(() => ({ data: [] }))
    state.deptTree = response.data ?? []
  } finally {
    state.treeLoading = false
  }
}

function closeModal() {
  state.showModal = false
  state.submitLoading = false
}

function openPermissionSelector() {
  permissionSelectorRef.value?.openModal(state.formModel.permission_key)
}

function handlePermissionSelected(permission: any) {
  state.formModel.permission_key = permission.permission_key
  if (!state.formModel.description && permission.name !== permission.permission_key) {
    state.formModel.description = permission.name
  }
}

async function submitForm() {
  await formRef.value?.validate()
  state.submitLoading = true
  try {
    const payload = {
      parent_id: state.formModel.parent_id,
      code: state.formModel.code.trim(),
      name: state.formModel.name.trim(),
      permission_key: state.formModel.permission_key.trim(),
      data_scope: state.formModel.data_scope,
      custom_scope_dept_ids:
        state.formModel.data_scope === 'CUSTOM' ? state.formModel.custom_scope_dept_ids : [],
      sort: Number(state.formModel.sort ?? 99),
      status: state.formModel.status,
      description: toNullableString(state.formModel.description),
    }

    if (state.formModel.id) {
      await resourceApi.buttonUpdate({
        ...payload,
        id: state.formModel.id,
      })
      window.$message.success('Updated successfully')
    } else {
      await resourceApi.buttonCreate(payload)
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
    style="width: 680px"
    :segmented="{ content: true, action: true }"
  >
    <NSpin :show="state.treeLoading">
      <NForm
        ref="formRef"
        :model="state.formModel"
        :rules="rules"
        label-placement="left"
        label-width="120"
        :disabled="state.submitLoading"
      >
        <NFormItem :label="'Resource Name'" path="name">
          <NInput v-model:value="state.formModel.name" />
        </NFormItem>
        <NFormItem :label="'Resource Code'" path="code">
          <NInput v-model:value="state.formModel.code" />
        </NFormItem>
        <NFormItem :label="'Permission Key'" path="permission_key">
          <NInputGroup>
            <NInput v-model:value="state.formModel.permission_key" readonly />
            <NButton type="primary" secondary @click="openPermissionSelector">
              {{ 'Select Permission' }}
            </NButton>
          </NInputGroup>
        </NFormItem>
        <NFormItem :label="'Data Scope'" path="data_scope">
          <DictSelect v-model="state.formModel.data_scope" dict-code="DATA_SCOPE" />
        </NFormItem>
        <NFormItem
          v-if="state.formModel.data_scope === 'CUSTOM'"
          :label="'Custom Departments'"
          path="custom_scope_dept_ids"
        >
          <NTreeSelect
            v-model:value="state.formModel.custom_scope_dept_ids"
            multiple
            cascade
            checkable
            clearable
            filterable
            :options="state.deptTree"
            key-field="id"
            label-field="name"
            children-field="children"
          />
        </NFormItem>
        <NFormItem :label="'Sort'" path="sort">
          <NInputNumber v-model:value="state.formModel.sort" class="w-full" :min="0" />
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

  <ModalPermissionSelector ref="permissionSelectorRef" @selected="handlePermissionSelected" />
</template>
